package cn.wudi.spider.http;

import cn.wudi.spider.cache.Cache;
import cn.wudi.spider.http.cookie.HttpCookieStore;
import cn.wudi.spider.http.cookie.JavaNetCookieJar;
import cn.wudi.spider.http.internal.Pair;
import cn.wudi.spider.mime.ContentType;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import javax.net.ssl.SSLHandshakeException;
import lombok.Getter;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.ConnectionSpec;
import okhttp3.CookieJar;
import okhttp3.HttpUrl;
import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.ResponseBody;
import okhttp3.internal.Util;
import okio.Buffer;

/**
 * 基于Okhttp的IClient实现
 *
 * @author tusu
 */
public class OkClient implements IClient {

  private static final List<ConnectionSpec> DEFAULT_CONNECTION_SPECS = Util.immutableList(
      ConnectionSpec.COMPATIBLE_TLS, ConnectionSpec.CLEARTEXT);

  @Nullable
  private final Cache cache;
  @Nullable
  private final String session;
  private final ProxyFactory proxyFactory;
  private final boolean verbose;
  private final boolean insecureSkipVerify;
  private final HttpCookieStore cookieStore;
  private final CompositeClientConfigurer configurers;
  @Getter
  private OkHttpClient okHttpClient;

  private OkClient(Builder builder) {
    this.cache = builder.cache;
    this.session = builder.session;
    this.proxyFactory = builder.proxyFactory;
    this.verbose = builder.verbose;
    this.insecureSkipVerify = builder.insecureSkipVerify;
    this.configurers = builder.configurers;

    this.cookieStore = CookiePersistor.loadCookieStore(cache, builder.cookieStore, session);

    // Init client
    OkHttpClient.Builder clientBuilder;
    List<Interceptor> lastInterceptor = null;
    if (builder.okHttpClient == null) {
      clientBuilder = new OkHttpClient.Builder();
    } else {
      clientBuilder = builder.okHttpClient.newBuilder();
      List<Interceptor> interceptors = clientBuilder.interceptors();
      // 过滤初始化的拦截器
      if (interceptors.size() != 0) {
        lastInterceptor = interceptors.stream()
            .filter(interceptor -> interceptor.getClass().getAnnotation(Init.class) == null)
            .collect(
                Collectors.toList());
        interceptors.clear();
      }
    }

    clientBuilder
        // cookieJar
        .cookieJar(new JavaNetCookieJar(cookieStore))
        // 兼容不安全的TLS协议
        .connectionSpecs(DEFAULT_CONNECTION_SPECS)
        // 任务完成后保存Cookie
        .addInterceptor(new CookieInterceptor(session, cache, cookieStore));

    // 是否输出日志
    With.verbose(this.verbose).config(clientBuilder);
    // 跳过ssl校验
    With.insecureSkipVerify(this.insecureSkipVerify).config(clientBuilder);
    // http代理
    With.httpProxyFactory(this.proxyFactory).config(clientBuilder);

    // 将上一次的拦截器载入
    if (lastInterceptor != null) {
      lastInterceptor.forEach(clientBuilder::addInterceptor);
    }

    // 扩展
    if (configurers != null) {
      configurers.config(clientBuilder);
    }

    this.okHttpClient = clientBuilder.build();
  }

  /**
   * 根据Request构建Url
   *
   * @param request request
   * @return url
   */
  private static HttpUrl buildUrl(Request request) {
    List<Pair<String, Pair<String, Boolean>>> queryParams = request.getQuerys();
    HttpUrl url = request.getUrl();
    if (queryParams == null) {
      return url;
    }
    HttpUrl.Builder builder = url.newBuilder();
    String charset = request.getCharset();
    queryParams.forEach((Pair<String, Pair<String, Boolean>> pair) -> {
      String key = pair.left();
      String value = pair.right().left();
      boolean encoded = !pair.right().right();
      if (encoded) {
        try {
          key = URLEncoder.encode(key, charset);
          value = URLEncoder.encode(value, charset);
        } catch (UnsupportedEncodingException e) {
          throw new HttpException(e);
        }
      }
      builder.addEncodedQueryParameter(key, value);
    });
    return builder.build();
  }

  @Override
  public HttpCookieStore cookieStore() {
    return cookieStore;
  }

  @Override
  public CookieJar cookieJar() {
    return okHttpClient.cookieJar();
  }

  @Override
  public Cache cache() {
    return cache;
  }

  @Override
  public String session() {
    return session;
  }

  @Override
  public Boolean insecureSkipVerify() {
    return insecureSkipVerify;
  }

  @Override
  public ClientConfigurer configurer() {
    return this.configurers;
  }

  @Override
  public ProxyFactory proxyFactory() {
    return proxyFactory;
  }

  @Override
  public Builder builder() {
    return new Builder()
        .cache(cache)
        .session(session)
        .proxy(proxyFactory)
        .insecureSkipVerify(insecureSkipVerify)
        .verbose(verbose)
        .client(okHttpClient)
        .cookieStore(cookieStore)
        .configurers(configurers);
  }

  @Override
  public Response send(Request request) throws HttpException {
    // --------Response
    okhttp3.Response okResponse;
    try {
      okResponse = getCall(request).execute();
      return getResponse(request, okResponse);
    } catch (IOException e) {
      throw handleIOException(e);
    }
  }

  @Override
  public CompletableFuture<Response> sendAsync(Request request) {
    CompletableFuture<Response> future = new CompletableFuture<>();
    Call call = getCall(request);
    //noinspection NullableProblems
    call.enqueue(new Callback() {
      @Override
      public void onFailure(Call call, IOException e) {
        future.completeExceptionally(handleIOException(e));
      }

      @Override
      public void onResponse(Call call, okhttp3.Response response) {
        Response resp;
        try {
          resp = getResponse(request, response);
        } catch (IOException e) {
          future.completeExceptionally(handleIOException(e));
          return;
        }
        future.complete(resp);
      }
    });
    return future;
  }

  /**
   * 根据Request构造Call
   *
   * @param request Request
   * @return Call
   */
  private Call getCall(Request request) {
    // --------Cookie
    this.preCookie(request);
    // --------Client
    OkHttpClient okHttpClient = this.buildClient(request);
    // --------Request
    okhttp3.Request req = this.buildRequest(request);
    return okHttpClient.newCall(req);
  }

  /**
   * 预处理Cookie
   *
   * @param request request
   */
  private void preCookie(Request request) {
    if (request.getClearCookie()) {
      this.cookieStore.removeAll();
    } else {
      this.cookieStore.remove(request.getUrl(), request.getRemovedCookies());
    }
    this.cookieStore.add(request.getUrl(), request.getAddedCookies());
  }

  private OkHttpClient buildClient(Request request) {
    ClientOption newClientOption = new ClientOption()
        .setFollowRedirects(request.getFollowRedirects())
        .setTimeout(request.getTimeout())
        .setHttpProxy(request.getHttpProxy());

    // 对比是否发生变化
    if (newClientOption.equals(ClientOption.DEFAULT)) {
      return okHttpClient;
    }

    OkHttpClient.Builder builder = this.okHttpClient.newBuilder();

    // 重定向
    builder.followRedirects(newClientOption.getFollowRedirects());
    // 设置timeout
    With.timeout(newClientOption.getTimeout()).config(builder);
    // Http代理
    With.httpProxyUrl(newClientOption.getHttpProxy()).config(builder);

    return builder.build();
  }

  private okhttp3.Request buildRequest(Request request) {
    okhttp3.Request.Builder reqBuilder = new okhttp3.Request.Builder();

    // 构造URL地址
    reqBuilder.url(buildUrl(request));

    // 构造Header
    if (request.getHeaders() != null) {
      reqBuilder.headers(request.getHeaders().build());
    }

    MediaType mediaType;
    ContentType contentType = request.getContentType();
    if (contentType.isValid()) {
      String contentTypeStr = contentType.toString();
      mediaType = MediaType.get(contentTypeStr);
      reqBuilder.header("Content-Type", contentTypeStr);
    } else {
      mediaType = null;
    }

    // 无body
    for (String s : Constant.NO_BODY) {
      if (s.equals(request.getMethod().toUpperCase())) {
        return reqBuilder.method(s, null).build();
      }
    }

    // body
    Buffer body;
    if (request.getBody() == null) {
      body = new Buffer();
    } else {
      body = request.getBody();
    }
    // form
    List<Pair<String, Pair<String, Boolean>>> forms = request.getForms();
    if (forms != null) {
      Buffer bf = HttpHelper.urlEncode(forms, request.getCharset());
      body.write(bf, bf.size());
    }
    // build body
    int pdu = request.getPDU();
    reqBuilder.method(request.getMethod(), new HttpRequestBody(mediaType, body, pdu));
    return reqBuilder.build();
  }

  /**
   * Response body is non-null, Need to mind {@link okhttp3.Response#body()}
   */
  private Response getResponse(Request request, okhttp3.Response okResponse) throws IOException {
    // 读取Response内容到bytes中
    ResponseBody body = okResponse.body();
    assert body != null;
    byte[] bytes;
    try {
      bytes = body.bytes();
    } finally {
      body.close();
    }
    //创建爬虫的返回结果
    return Response.builder()
        .headers(okResponse.headers())
        .body(bytes)
        .code(okResponse.code())
        .url(okResponse.request().url())
        .charset(request.getCharset())
        .request(request)
        .build();
  }

  private HttpException handleIOException(IOException e) {
    if (e instanceof SSLHandshakeException) {
      return new HttpException(HttpException.SSL_EXCEPTION, e);
    }
    if (e.getMessage() != null && e.getMessage().length() > 0) {
      String errorMsg = e.getMessage();

      if (errorMsg.contains("timed out") || errorMsg.toLowerCase().contains("timeout")) {
        return new HttpException(HttpException.TIMEOUT_EXCEPTION, e);
      }
      if (errorMsg.contains("Failed to connect")) {
        return new HttpException(HttpException.CONNECT_FAIL_EXCEPTION, e);
      }
    }
    return new HttpException(HttpException.UNKNOWN_EXCEPTION, e);
  }

  public static class Builder implements IClient.Builder {

    @Nullable
    private Cache cache;
    @Nullable
    private String session;
    private ProxyFactory proxyFactory;
    private boolean verbose = true;
    private boolean insecureSkipVerify = false;
    @Nullable
    private HttpCookieStore cookieStore;
    private OkHttpClient okHttpClient;
    @Nullable
    private CompositeClientConfigurer configurers;

    private Builder client(OkHttpClient okHttpClient) {
      this.okHttpClient = okHttpClient;
      return this;
    }

    private Builder configurers(CompositeClientConfigurer configurers) {
      if (configurers == null) {
        return this;
      }
      this.configurers = new CompositeClientConfigurer(configurers);
      return this;
    }

    @Override
    public Builder addConfigurer(ClientConfigurer configurer) {
      if (this.configurers == null) {
        this.configurers = new CompositeClientConfigurer();
      }
      this.configurers.addConfigurer(configurer);
      return this;
    }

    @Override
    public Builder cookieStore(HttpCookieStore cookieStore) {
      this.cookieStore = cookieStore;
      return this;
    }

    @Override
    public Builder verbose(boolean verbose) {
      this.verbose = verbose;
      return this;
    }

    @Override
    public Builder proxy(ProxyFactory proxyFactory) {
      this.proxyFactory = proxyFactory;
      return this;
    }

    @Override
    public Builder cache(Cache cache) {
      this.cache = cache;
      return this;
    }

    @Override
    public Builder session(String session) {
      this.session = session;
      return this;
    }

    @Override
    public Builder insecureSkipVerify(Boolean insecureSkipVerify) {
      this.insecureSkipVerify = insecureSkipVerify;
      return this;
    }

    @Override
    public IClient build() {
      return new OkClient(this);
    }
  }
}

package cn.wudi.spider.http;

import cn.wudi.spider.http.internal.Pair;
import cn.wudi.spider.mime.ContentType;
import com.alibaba.fastjson.JSON;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import javax.annotation.Nullable;
import lombok.Getter;
import lombok.NonNull;
import lombok.ToString;
import okhttp3.Cookie;
import okhttp3.Headers;
import okhttp3.HttpUrl;
import okio.Buffer;

/**
 * @author tusu
 */
@Getter
@ToString
public class Request {

  /**
   * Http HttpProxy url
   */
  private HttpUrl httpProxy;
  /**
   * Http addedCookies
   */
  private List<Cookie> addedCookies;
  /**
   * Http removedCookies
   */
  private List<Cookie> removedCookies;
  /**
   * 发送本次请求前是否清除cookie
   */
  private Boolean clearCookie = false;
  /**
   * 是否自动跳转
   */
  private Boolean followRedirects = ClientOption.FOLLOW_REDIRECTS;
  /**
   * 超时10_000ms 即10s
   */
  private long timeout;
  /**
   * Content-Type
   */
  private ContentType contentType = new ContentType();
  /**
   * Protocol Data Unit
   */
  private int PDU = -1;
  /**
   * 目标url
   */
  private HttpUrl url;
  /**
   * Http Theme e.g. GET|POST|...
   */
  private String method;
  /**
   * Http headers
   */
  private Headers.Builder headers;
  /**
   * Http body
   */
  private Buffer body;
  /**
   * Http query auto-urlEncode
   */
  private List<Pair<String, Pair<String, Boolean>>> querys;
  /**
   * Http form  auto-urlEncode
   */
  private List<Pair<String, Pair<String, Boolean>>> forms;
  /**
   * 额外的信息, 用于打日志或者其他
   */
  private Map<String, Object> extra;
  /**
   * 发送任务的client
   */
  private transient IClient client;

  public Request POST() {
    return this.method("POST");
  }

  public Request GET() {
    return this.method("GET");
  }

  public Request DELETE() {
    return this.method("DELETE");
  }

  public Request PUT() {
    return this.method("GET");
  }

  public Request PATCH() {
    return this.method("PATCH");
  }

  public Request OPTIONS() {
    return this.method("OPTIONS");
  }

  public Request HEAD() {
    return this.method("HEAD");
  }

  /**
   * Http请求的类型
   *
   * @param method GET or POST etc.
   * @return Request
   */
  public Request method(String method) {
    this.method = method;
    return this;
  }

  /**
   * 是否自动跳转
   *
   * @param followRedirects true or false
   * @return Request
   */
  public Request followRedirects(Boolean followRedirects) {
    this.followRedirects = followRedirects;
    return this;
  }

  /**
   * 当前Request的发送者
   *
   * @param client IClient
   * @return Request
   */
  public Request client(IClient client) {
    this.client = client;
    return this;
  }

  /**
   * Socket的pdu
   *
   * @param pdu tcp单包大小
   * @return Request
   */
  public Request pdu(int pdu) {
    this.PDU = pdu;
    return this;
  }

  /**
   * 请求的url
   *
   * @see #url(HttpUrl)
   */
  public Request url(String url) {
    this.url = HttpUrl.parse(url);
    return this;
  }

  /**
   * 请求的{@link HttpUrl}
   *
   * @param url httpUrl
   * @return Request
   */
  public Request url(HttpUrl url) {
    this.url = url;
    return this;
  }

  /**
   * 请求的body, {@link Buffer}类型
   *
   * @param body Buffer类型，推荐使用这种
   * @return Request
   */
  public Request body(@NonNull Buffer body) {
    this.body = body;
    return this;
  }

  /**
   * Http请求的body
   *
   * @param body 字节
   * @return Request
   */
  public Request body(@NonNull byte[] body) {
    this.body = new Buffer().write(body);
    return this;
  }

  /**
   * 序列化设置的obj到body中
   *
   * @param obj 可以被json序列化的对象
   * @return Request
   */
  public Request json(Object obj) {
    if (this.body == null) {
      // content-type会覆盖
      this.head("Content-Type", "application/json");
    }
    return this.body(JSON.toJSONBytes(obj));
  }

  /**
   * 添加表头(e.g. head("Content-Type", "application/json;charset=UTF-8"))
   *
   * @param name 请求头的name
   * @param value 请求头的value
   * @return Request
   */
  public Request head(String name, String value) {
    if (this.headers == null) {
      this.headers = new Headers.Builder();
    }
    return addHead(name, value);
  }

  /**
   * 添加表头(e.g. head("Content-Type:application/json;charset=UTF-8"))
   *
   * @param line 单行请求头
   * @return Request
   */
  public Request head(@NonNull String line) {
    if (this.headers == null) {
      this.headers = new Headers.Builder();
    }
    HttpHelper.parsedLine(line, ":", this::addHead);
    return this;
  }

  /**
   * 添加表头(e.g. head("Content-Type:application/json;charset=UTF-8\nContent-Type:application/json;charset=UTF-8"))
   *
   * @param lines 请求头
   * @return Request
   */
  public Request heads(@NonNull String lines) {
    if (this.headers == null) {
      this.headers = new Headers.Builder();
    }
    HttpHelper.parsedLines(lines, this::addHead);
    return this;
  }

  /**
   * Request's charset，如果存在Content-Type，将会添加在Content-Type中
   *
   * @param charset utf-8
   * @return Request
   */
  public Request charset(String charset) {
    this.contentType.setCharset(charset);
    return this;
  }

  /**
   * 检查Content-Type
   *
   * @param name name
   * @param value value
   * @return Request
   */
  private Request addHead(String name, String value) {
    // 拦截content-type类型
    if ("content-type".equalsIgnoreCase(name)) {
      ContentType contentType = ContentType.parse(value);
      if (contentType == null) {
        throw new IllegalArgumentException("Can not parse Content-Type: " + value);
      }
      // 如果解析出来的ContentType.charset为空则使用原本的charset
      if (contentType.getCharset() == null) {
        contentType.setCharset(this.contentType.getCharset());
      }
      this.contentType = contentType;
      return this;
    }
    this.headers.add(name, value);
    return this;
  }

  /**
   * 添加一些通用的header
   */
  public Request common() {
    return this.head("Accept",
        "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8")
        .head("Accept-Language", "zh-CN,zh;q=0.8,ja;q=0.6,zh-TW;q=0.4,en;q=0.2")
        .userAgent();
  }

  public Request userAgent() {
    return this.head("User-Agent",
        "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_13_4) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/65.0.3325.181 Safari/537.36");
  }

  /**
   * Add a query from chrome parsed(urldecoded) query. (e.g. queryParsed("name: foo") i.e.
   * query("name", "foo"))
   *
   * @param line one line at chrome parsed query
   * @return Request
   */
  public Request queryParsed(@NonNull String line) {
    HttpHelper.parsedLine(line, ":", this::query);
    return this;
  }

  /**
   * Add multiple query from chrome parsed(urldecoded) query. (e.g. queryParsed("name: foo\ngender:
   * male") i.e. query("name", "foo").query("gender", "male"))
   *
   * @param lines multiple line at chrome parsed query
   * @return Request
   */
  public Request querysParsed(@NonNull String lines) {
    HttpHelper.parsedLines(lines, this::query);
    return this;
  }

  /**
   * 解析source内容添加到querys中(e.g. query("foo=1&bar=2") i.e. foo=1&bar=2), 可以从chrome Headers中找到
   *
   * @param source url的参数部分
   * @return Request
   */
  public Request query(String source) {
    String[] split = source.split("&");
    for (String s : split) {
      HttpHelper.parsedLine(s, "=", (name, value) -> query(name, value, true));
    }
    return this;
  }

  /**
   * (e.g. query("foo", "1").query("bar", "2") i.e. foo=1&bar=2)
   *
   * @param name name
   * @param value value
   * @return Request
   */
  public Request query(@Nullable String name, @Nullable String value) {
    return query(name, value, false);
  }

  /**
   * @param encoded 是否已经编码
   * @see #form(String, String)
   */
  public Request query(@Nullable String name, @Nullable String value, Boolean encoded) {
    return query(name, value, encoded, false);
  }

  /**
   * @param encoded 是否已经编码
   * @param single 是否需要删除之前存在的
   * @see #form(String, String)
   */
  public Request query(@Nullable String name, @Nullable String value, boolean encoded,
      boolean single) {
    if (this.querys == null) {
      this.querys = new ArrayList<>();
      this.head("Content-Type", "application/x-www-form-urlencoded");
    }
    HttpHelper.urlencodeParameter(this.querys, name, value, encoded, single);
    return this;
  }

  /**
   * 如果已经存在name则删除，然后再添加保证唯一
   *
   * @see #query(String, String)
   */
  public Request singleQuery(@Nullable String name, @Nullable String value) {
    return singleQuery(name, value, false);
  }

  /**
   * 参考{@link #singleQuery(String, String)}
   *
   * @param encoded 是否已经编码
   * @see #query(String, String)
   */
  public Request singleQuery(@Nullable String name, @Nullable String value, Boolean encoded) {
    return query(name, value, encoded, true);
  }

  /**
   * form 指Http UrlEncode后的Body部分Set form(e.g. form("foo", "1").form("bar", "2") i.e. foo=1&bar=2)
   *
   * @param name name
   * @param value value
   */
  public Request form(@Nullable String name, @Nullable String value) {
    return form(name, value, false);
  }

  /**
   * @param encoded 是否已经编码
   * @see #form(String, String)
   */
  public Request form(@Nullable String name, @Nullable String value, Boolean encoded) {
    return form(name, value, encoded, false);
  }

  /**
   * 参考{@link #form(String, String)}
   *
   * @param encoded 是否已经编码
   * @param single 是否需要删除之前存在的
   */
  public Request form(@Nullable String name, @Nullable String value, boolean encoded,
      boolean single) {
    if (this.forms == null) {
      this.forms = new ArrayList<>();
      this.head("Content-Type", "application/x-www-form-urlencoded");
    }
    HttpHelper.urlencodeParameter(this.forms, name, value, encoded, single);
    return this;
  }

  /**
   * 参考{@link #form(String, String)} 如果已经存在name则删除，然后再添加保证唯一
   */
  public Request singleForm(@Nullable String name, @Nullable String value) {
    return singleForm(name, value, false);
  }

  /**
   * 参考{@link #singleForm(String, String)} 是否已经编码
   */
  public Request singleForm(@Nullable String name, @Nullable String value, Boolean encoded) {
    return form(name, value, encoded, true);
  }

  /**
   * Add a form from chrome parsed(urldecoded) form.(e.g. formParsed("Content-Encoding: gzip"))
   *
   * @param line one line at chrome parsed form
   * @return Request
   */
  public Request formParsed(@NonNull String line) {
    HttpHelper.parsedLine(line, ":", this::form);
    return this;
  }

  /**
   * Add multiple form from chrome parsed(urldecoded) form.(e.g. formsParsed("Content-Encoding:
   * gzip\nContent-Type: application/javascript"))
   *
   * @param lines multiple line at chrome parsed query
   * @return Request
   */
  public Request formsParsed(@NonNull String lines) {
    HttpHelper.parsedLines(lines, this::form);
    return this;
  }

  /**
   * 解析source内容添加到forms中(e.g. form("foo=1&bar=2") i.e. foo=1&bar=2, 可以从chrome Headers中找到)
   *
   * @param source body的参数部分
   * @return Request
   */
  public Request form(String source) {
    String[] split = source.split("&");
    for (String s : split) {
      HttpHelper.parsedLine(s, "=", (name, value) -> form(name, value, true));
    }
    return this;
  }

  /**
   * 本次请求是否先清除cookie
   *
   * @param clearCookie true or false
   * @return Request
   */
  public Request clearCookie(boolean clearCookie) {
    this.clearCookie = clearCookie;
    return this;
  }

  /**
   * 删除当前host对应的cookie
   *
   * @param name 对应的cookie名称
   * @return Request
   */
  public Request removeCookie(String name) {
    return removeCookie(new Cookie.Builder().name(name).value("").domain(this.url.host()).build());
  }

  /**
   * 删除当前host对应的cookie
   *
   * @param cookie cookie
   * @return Request
   */
  public Request removeCookie(Cookie cookie) {
    if (this.removedCookies == null) {
      this.removedCookies = new ArrayList<>();
    }
    this.removedCookies.add(cookie);
    return this;

  }

  /**
   * 在当前host下添加cookie
   *
   * @param name name
   * @param value value
   * @return Request
   */
  public Request addCookie(String name, String value) {
    return addCookie(new Cookie.Builder().name(name).value(value).domain(this.url.host()).build());
  }

  /**
   * 添加cookie
   *
   * @param cookie cookie
   * @return Request
   */
  public Request addCookie(Cookie cookie) {
    if (this.addedCookies == null) {
      this.addedCookies = new ArrayList<>();
    }
    this.addedCookies.add(cookie);
    return this;
  }

  /**
   * 为client设置HttpProxy
   *
   * @param httpProxy 对应的httpUrl，包含地址以及basic校验
   * @return Request
   */
  public Request httpProxy(HttpUrl httpProxy) {
    this.httpProxy = httpProxy;
    return this;
  }

  /**
   * 设置超时时间
   *
   * @param timeout ms
   * @return Request
   */
  public Request timeout(int timeout) {
    this.timeout = timeout;
    return this;
  }

  /**
   * 额外的自定义信息
   */
  public Request extra(Map<String, Object> extra) {
    this.extra = extra;
    return this;
  }

  /**
   * 任务超时后重试
   *
   * @param times 重试次数
   * @return Response
   */
  public Response retryExecute(int times) throws HttpException {
    RuntimeException exception = null;
    for (int i = 0; i < times; i++) {
      try {
        return this.send();
      } catch (HttpException e) {
        if (e.getCode() == HttpException.TIMEOUT_EXCEPTION
            || e.getCode() == HttpException.SSL_EXCEPTION) {
          exception = e;
          continue;
        }
        // 代理异常切换代理
        if (e.getCode() == HttpException.PROXY_EXCEPTION) {
          if (this.client.proxyFactory() == null) {
            throw e;
          }
          exception = e;
          this.httpProxy = this.client.proxyFactory().get();
          continue;
        }
        throw e;
      }
    }
    throw new HttpException(exception);
  }

  /**
   * 发送该Request
   *
   * @return Response
   * @throws HttpException 读写超时等异常
   */
  public Response send() throws HttpException {
    return this.client.send(this);
  }

  /**
   * 异步发送该Request
   *
   * @return 异步对象
   */
  public CompletableFuture<Response> sendAsync() {
    return this.client.sendAsync(this);
  }

  /**
   * 包作用域仅用于构建client
   *
   * @return charset
   */
  String getCharset() {
    if (contentType.getCharset() == null) {
      return "utf-8";
    }
    return contentType.getCharset();
  }
}

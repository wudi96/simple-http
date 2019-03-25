package cn.wudi.spider.http;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.Proxy.Type;
import java.net.ProxySelector;
import java.net.SocketAddress;
import java.net.URI;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import okhttp3.Credentials;
import okhttp3.HttpUrl;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;

/**
 * @author tusu.
 */
public class With {

  private static final Interceptor LOG_INTERCEPTOR = new LogInterceptor();

  static ClientConfigurer httpProxyFactory(ProxyFactory proxyFactory) {
    return (builder -> {
      if (proxyFactory == null) {
        return;
      }
      HttpUrl httpUrl = proxyFactory.get();
      if (httpUrl == null) {
        return;
      }
      configHttpProxy(builder, httpUrl);
    });
  }

  static ClientConfigurer verbose(boolean verbose) {
    return (builder -> {
      if (!verbose) {
        return;
      }
      builder.addInterceptor(LOG_INTERCEPTOR);
    });
  }

  /**
   * 是否跳过证书
   */
  static ClientConfigurer insecureSkipVerify(boolean insecureSkipVerify) {
    return (builder -> {
      if (!insecureSkipVerify) {
        return;
      }
      builder
          .hostnameVerifier(SSLSocketClient.getHostnameVerifier())
          .sslSocketFactory(SSLSocketClient.getSSLSocketFactory(),
              SSLSocketClient.getTrustManager());
    });
  }

  static ClientConfigurer timeout(long timeout) {
    return (builder -> {
      if (timeout == 0) {
        return;
      }
      builder
          .connectTimeout(timeout, TimeUnit.MILLISECONDS)
          .readTimeout(timeout, TimeUnit.MILLISECONDS)
          .writeTimeout(timeout, TimeUnit.MILLISECONDS);
    });
  }

  /**
   * 设置代理
   */
  static ClientConfigurer httpProxyUrl(HttpUrl proxyUrl) {
    return builder -> {
      if (proxyUrl == null) {
        return;
      }
      configHttpProxy(builder, proxyUrl);
    };
  }

  private static void configHttpProxy(OkHttpClient.Builder builder, HttpUrl proxyUrl) {
    // 设置代理
    builder.proxySelector(new ProxySelector() {
      @Override
      public List<Proxy> select(URI uri) {
        return Collections.singletonList(new Proxy(Type.HTTP,
            new InetSocketAddress(proxyUrl.host(), proxyUrl.port())));
      }

      @Override
      public void connectFailed(URI uri, SocketAddress sa, IOException ioe) {
        throw new HttpException(HttpException.PROXY_EXCEPTION, "Proxy is unavailable", ioe);
      }
    });
    // 代理basic校验
    if (!"".equals(proxyUrl.username()) && !"".equals(proxyUrl.password())) {
      builder.proxyAuthenticator((route, response) -> response.request().newBuilder()
          .header("Proxy-Authorization", Credentials
              .basic(proxyUrl.username(), proxyUrl.password()))
          .build());
    }
  }
}

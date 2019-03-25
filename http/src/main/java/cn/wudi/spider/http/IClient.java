package cn.wudi.spider.http;

import cn.wudi.spider.cache.Cache;
import cn.wudi.spider.http.cookie.HttpCookieStore;
import java.util.concurrent.CompletableFuture;
import okhttp3.CookieJar;

/**
 * Created by tusu on 2017/9/12.
 *
 * HttpClient的抽象
 */
public interface IClient {

  /**
   * 生成默认client
   */
  static IClient newClient() {
    return new OkClient.Builder().build();
  }

  /**
   * 生成默认client.Builder
   */
  static Builder newBuilder() {
    return new OkClient.Builder();
  }

  /**
   * 生成一个基于当前client的builder
   */
  Builder builder();

  /**
   * 发送请求并返回结果
   */
  Response send(Request request) throws HttpException;

  /**
   * 异步请求
   */
  CompletableFuture<Response> sendAsync(Request request);

  /**
   * 获得当前client的工厂
   */
  ProxyFactory proxyFactory();

  /**
   * 获取Client的CookieStore
   *
   * @return cookieStore
   */
  HttpCookieStore cookieStore();

  /**
   * 获得当前client的cookieJar
   */
  @Deprecated
  CookieJar cookieJar();

  /**
   * 获得当前client的缓存
   */
  Cache cache();

  /**
   * 获取当前client的session
   */
  String session();

  /**
   * 是否忽略TLS
   */
  Boolean insecureSkipVerify();

  /**
   * @return 扩展项
   */
  default ClientConfigurer configurer() {
    return null;
  }

  /**
   * 释放资源
   */
  default void close() {
  }

  interface Builder {

    /**
     * 允许使用自定义CookieStore来变更Cookie策略
     *
     * @param cookieStore cookieJar
     * @return this
     */
    Builder cookieStore(HttpCookieStore cookieStore);

    /**
     * Set 是否输出日志
     */
    Builder verbose(boolean verbose);

    /**
     * Set Http代理工厂
     */
    Builder proxy(ProxyFactory proxyFactory);

    /**
     * Set 缓存，session的cookie
     */
    Builder cache(Cache cache);

    /**
     * Set 当前client的session
     */
    Builder session(String session);

    /**
     * Set 是否忽略TLS
     */
    Builder insecureSkipVerify(Boolean insecureSkipVerify);

    /**
     * 设置扩展
     *
     * @param configurer 配置项
     */
    @Deprecated
    default Builder configurer(ClientConfigurer configurer) {
      return addConfigurer(configurer);
    }

    default Builder addConfigurer(ClientConfigurer configurer) {
      return this;
    }

    /**
     * 根据配置构建IClient
     */
    IClient build();
  }
}

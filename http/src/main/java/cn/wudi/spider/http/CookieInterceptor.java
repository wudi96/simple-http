package cn.wudi.spider.http;

import cn.wudi.spider.cache.Cache;
import cn.wudi.spider.http.cookie.HttpCookieStore;
import java.io.IOException;
import okhttp3.Interceptor;
import okhttp3.Response;

/**
 * 当请求完成后持久化cookie
 *
 * @author tusu.
 */
@Init
public class CookieInterceptor implements Interceptor {

  private String session;
  private Cache cache;
  private HttpCookieStore cookieStore;

  public CookieInterceptor(String session, Cache cache,
      HttpCookieStore cookieStore) {
    this.session = session;
    this.cache = cache;
    this.cookieStore = cookieStore;
  }

  @Override
  public Response intercept(Chain chain) throws IOException {
    Response response = chain.proceed(chain.request());
    CookiePersistor.saveCookieStore(cache, cookieStore, session);
    return response;
  }
}

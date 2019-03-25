package cn.wudi.spider.http.cookie;

import java.util.List;
import okhttp3.Cookie;
import okhttp3.CookieJar;
import okhttp3.HttpUrl;

/**
 * @author tusu
 */
public class JavaNetCookieJar implements CookieJar {

  private final HttpCookieStore cookieStore;

  public JavaNetCookieJar(HttpCookieStore cookieStore) {
    this.cookieStore = cookieStore;
  }

  @Override
  public void saveFromResponse(HttpUrl url, List<Cookie> cookies) {
    cookieStore.add(url, cookies);
  }

  @Override
  public List<Cookie> loadForRequest(HttpUrl url) {
    return cookieStore.get(url);
  }
}
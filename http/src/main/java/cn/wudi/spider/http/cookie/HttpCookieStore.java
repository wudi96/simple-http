package cn.wudi.spider.http.cookie;

import java.util.List;
import javax.annotation.Nullable;
import okhttp3.Cookie;
import okhttp3.HttpUrl;

/**
 * @author tusu
 */
public interface HttpCookieStore {

  /**
   * 添加Cookie
   *
   * @param url Set-Cookie url
   * @param cookies Set-Cookie cookies
   */
  void add(HttpUrl url, @Nullable List<Cookie> cookies);

  /**
   * 删除Cookie
   *
   * @param url Set-Cookie url
   * @param cookies Set-Cookie cookies
   */
  void remove(HttpUrl url, @Nullable List<Cookie> cookies);

  /**
   * 删除cookie
   *
   * @param url Cookie url
   */
  List<Cookie> get(HttpUrl url);

  /**
   * 获取所有Cookie
   *
   * @return 所有Cookie
   */
  List<Cookie> getAll();

  /**
   * 清空cookie
   */
  void removeAll();
}

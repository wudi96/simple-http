package cn.wudi.spider.http;

import cn.wudi.spider.cache.Cache;
import cn.wudi.spider.http.cookie.HttpCookieStore;
import cn.wudi.spider.http.cookie.StrictHttpCookieStore;

class CookiePersistor {

  private final static String PREFIX = "ok:";

  static HttpCookieStore loadCookieStore(Cache cache, HttpCookieStore cookieStore,
      String session) {
    // 无缓存时
    if (cache == null) {
      // cookieStore为空
      if (cookieStore == null) {
        return new StrictHttpCookieStore();
      }
      return cookieStore;
    }

    // 缓存中读取
    String cookieStr = cache.get(PREFIX + session);
    // 缓存数据无效
    if (cookieStr == null || cookieStr.trim().length() == 0) {
      return new StrictHttpCookieStore();
    }
    // 反序列化
    return StrictHttpCookieStore.deserialize(cookieStr);
  }

  static void saveCookieStore(Cache cache, HttpCookieStore cookieStore, String session) {
    if (cache == null) {
      return;
    }
    cache.set(PREFIX + session, StrictHttpCookieStore.serialize(cookieStore));
  }
}

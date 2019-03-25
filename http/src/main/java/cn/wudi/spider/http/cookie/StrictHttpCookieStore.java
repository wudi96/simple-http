package cn.wudi.spider.http.cookie;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.alibaba.fastjson.parser.ParserConfig;
import com.alibaba.fastjson.serializer.SerializeConfig;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import okhttp3.Cookie;
import okhttp3.HttpUrl;

/**
 * @author tusu.
 */
public class StrictHttpCookieStore implements HttpCookieStore {

  private static ParserConfig parserConfig = new ParserConfig();
  private static SerializeConfig serializeConfig = new SerializeConfig();

  static {
    parserConfig.putDeserializer(Cookie.class, new CookieDeserializer());
    serializeConfig.put(Cookie.class, new CookieSerializer());
  }

  private final Map<String, Cookie> cookies;

  public StrictHttpCookieStore() {
    cookies = new ConcurrentHashMap<>();
  }

  private StrictHttpCookieStore(
      Map<String, Cookie> cookies) {
    this.cookies = cookies;
  }

  private static boolean isCookieExpired(Cookie cookie) {
    return cookie.expiresAt() < System.currentTimeMillis();
  }

  private static String createCookieKey(Cookie cookie) {
    return (cookie.secure() ? "https" : "http") + "://" + cookie.domain() + cookie.path() + "|"
        + cookie.name();
  }

  public static HttpCookieStore deserialize(String serialize) {
    Type type = new TypeReference<ConcurrentHashMap<String, Cookie>>() {
    }.getType();
    Object o = JSON.parseObject(serialize, type, parserConfig);
    return new StrictHttpCookieStore((Map<String, Cookie>) o);
  }

  public static String serialize(HttpCookieStore httpCookieStore) {
    return JSON.toJSONString(((StrictHttpCookieStore) httpCookieStore).cookies, serializeConfig);
  }

  static void sortCookiesByPath(List<Cookie> cookies) {
    if (cookies.size() > 1) {
      // 根据path排序
      cookies.sort((o1, o2) -> o1.path().compareTo(o2.path()) * -1);
    }
  }

  private void add(Cookie cookie) {
    cookies.put(createCookieKey(cookie), cookie);
  }

  private void remove(Cookie cookie) {
    cookies.remove(createCookieKey(cookie), cookie);
  }

  @Override
  public void add(HttpUrl url, List<Cookie> cookies) {
    if (cookies == null) {
      return;
    }
    cookies.forEach(this::add);
  }

  @Override
  public void remove(HttpUrl url, List<Cookie> cookies) {
    if (cookies == null) {
      return;
    }
    cookies.forEach(this::remove);
  }

  @Override
  public List<Cookie> get(HttpUrl url) {
    List<Cookie> validCookies = new ArrayList<>();
    for (Iterator<Entry<String, Cookie>> it = cookies.entrySet().iterator();
        it.hasNext(); ) {
      Entry<String, Cookie> entry = it.next();
      Cookie cookie = entry.getValue();
      if (isCookieExpired(cookie)) {
        it.remove();
      } else if (cookie.matches(url)) {
        validCookies.add(cookie);
      }
    }
    sortCookiesByPath(validCookies);
    return validCookies;
  }

  @Override
  public List<Cookie> getAll() {
    return new ArrayList<>(cookies.values());
  }

  @Override
  public void removeAll() {
    cookies.clear();
  }
}

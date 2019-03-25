package cn.wudi.spider.http.cookie;

import java.util.ArrayList;
import java.util.List;
import okhttp3.Cookie;
import okhttp3.Cookie.Builder;
import okhttp3.HttpUrl;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * @author tusu.
 */
public class StrictHttpCookieStoreTest {

  private HttpCookieStore cookieStore;
  private List<Cookie> cookies;
  private Cookie cookie;

  @Before
  public void init() {
    cookieStore = new StrictHttpCookieStore();
    cookies = new ArrayList<>();
    cookie = new Builder().name("tusu").value("1").domain("localhost").build();
    cookies.add(cookie);
    cookieStore.add(null, cookies);
  }

  @Test
  public void deserialize() {
    String string = StrictHttpCookieStore.serialize(cookieStore);
    Assert.assertEquals(string,
        "{\"http://localhost/|tusu\":{\"name\":\"tusu\",\"value\":\"1\",\"expiresAt\":253402300799999,\"domain\":\"localhost\",\"path\":\"/\",\"secure\":false,\"httpOnly\":false,\"hostOnly\":false}}");
  }

  @Test
  public void remove() {
    cookieStore.remove(null, cookies);
    List<Cookie> all = cookieStore.getAll();
    Assert.assertEquals(0, all.size());
  }

  @Test
  public void get() {
    List<Cookie> cookies = cookieStore.get(HttpUrl.parse("http://localhost"));
    Cookie cookie1 = cookies.get(0);
    Assert.assertSame(this.cookie, cookie1);
  }

  @Test
  public void removeAll() {
    cookieStore.removeAll();
    List<Cookie> all = cookieStore.getAll();
    Assert.assertEquals(0, all.size());
  }

  @Test
  public void sortCookiesByPath() {
    Cookie priority = new Builder().name("tusu").value("priority").domain("localhost")
        .path("/priority").build();
    cookies.add(priority);
    StrictHttpCookieStore.sortCookiesByPath(cookies);
    Assert.assertEquals(cookies.get(0).value(), "priority");
  }
}
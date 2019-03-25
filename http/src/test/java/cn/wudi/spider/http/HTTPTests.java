package cn.wudi.spider.http;

import com.alibaba.fastjson.JSONObject;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.Proxy.Type;
import java.util.Arrays;
import javax.annotation.Nullable;
import okhttp3.Authenticator;
import okhttp3.Credentials;
import okhttp3.HttpUrl;
import okhttp3.Route;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class HTTPTests {

  private String url;
  private IClient client;
  private Request request;

  @Before
  public void init() {
    url = "http://www.baidu.com";
    client = IClient.newClient();
    request = new Request().POST().url(url)
        // headers
        .head("User-Agent: tusu")
        .heads("Content-Language: zh-CN\n"
            + "Content-Type: text/html; charset=utf-8")
        // get params
        // get source
        .query("name=query&")
        // get name value
        .query("name0", "tusu")
        // get parsed
        .queryParsed("name1: luorigong")
        // gets parsed
        .querysParsed("name2: yansui\nname3: hugu")
        // post params
        // post source
        .form("name=form&")
        // post name value
        .form("name0", "tusu")
        // post parsed
        .formParsed("name1: luorigong")
        // posts parsed
        .formsParsed("name2: yansui\nname3: hugu")
        // cookie
        .addCookie("name", "tusu");
  }

  /**
   * 同步请求
   */
  @Test
  public void sync() {
    Response response = client.send(request);
    String string = response.string();
    Assert.assertTrue(string, string.contains(url));
  }

  /**
   * 异步请求
   */
  @Test
  public void async() {
    client.sendAsync(request)
        .whenComplete((response, throwable) -> Assert.assertTrue(response.string().contains(url)));
  }

  @Test
  public void proxy() {
    String proxyUrl = getOneProxyUrl();
    client = client.builder().proxy(
        () -> HttpUrl.parse("http://" + proxyUrl).newBuilder().username("fs_kq_10086_app")
            .password("fs_kq_pwd_7758258").build()).build();
    sync();
  }

  @Test
  public void proxySelector() {
    client = client.builder()
        .addConfigurer(builder -> builder.proxySelector(new SimpleProxySelector(
            Arrays.asList(parseProxy("127.0.0.1:80"), parseProxy(getOneProxyUrl()))))
            .proxyAuthenticator(new Authenticator() {
              @Nullable
              @Override
              public okhttp3.Request authenticate(@Nullable Route route,
                  okhttp3.Response response) {
                return response.request().newBuilder().header("Proxy-Authorization",
                    Credentials.basic("fs_kq_10086_app", "fs_kq_pwd_7758258")).build();
              }
            })).build();
    sync();
  }

  @Test(expected = HttpException.class)
  public void proxySelectorError() {
    client = client.builder()
        .addConfigurer(builder -> builder
            .proxySelector(new SimpleProxySelector(
                Arrays.asList(parseProxy("127.0.0.1:80"), parseProxy("127.0.0.1:81")))))
        .build();
    client.send(request);
  }

  private String getOneProxyUrl() {
    JSONObject json = client.send(new Request().GET()
        .url("https://www.wudi.cn/crawler/proxy/getProxy?token=test-one-common-TT")).json(
        JSONObject.class);
    return json.getJSONArray("data").getString(0);
  }

  private Proxy parseProxy(String address) {
    String[] strings = address.split(":");
    return new Proxy(Type.HTTP, new InetSocketAddress(strings[0], Integer.parseInt(strings[1])));
  }

  /**
   * HEAD send
   */
  @Test
  public void head() {
    Response response = client.send(new Request().HEAD().url(url));
    Assert.assertEquals(0, response.string().length());
  }
}

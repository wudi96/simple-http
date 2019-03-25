<p align="center">
    <a href="http://git.caimi-inc.com/spider-java/simple-http"><img src="http://git.caimi-inc.com/spider-java/spider-spring-boot/uploads/bbe51be66b07248fe48c959f0913fcc5/simple-http.jpg" width="325"/></a>
</p>
<p align="center">基于 <code>Java8</code> + <code>微调后的Okhttp</code>😋</p>
<p align="center">
    🔥 <a href="#快速开始">快速入门</a>
</p>

<p align="center">
    <a href="http://git.caimi-inc.com/spider-java/simple-http/wikis/home"><img src="https://img.shields.io/badge/wiki-passing-green.svg"></a>
    <img src="https://img.shields.io/badge/version-0.5.0-blue.svg">
</p>

***

## 快速开始

`Maven` 配置：

创建一个基础的 `Maven` 工程

```xml
<dependencies>
+    <dependency>
+        <groupId>cn.wudi.spider</groupId>
+        <artifactId>http</artifactId>
+        <version>${revision}</version>
+    </dependency>
</dependencies>
```

`HTTPTests.java`

```java
package cn.wudi.spider.http;

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
    Assert.assertTrue(response.string().contains(url));
  }

  /**
   * 异步请求
   */
  @Test
  public void async() {
    client.sendAsync(request).whenComplete((response, throwable) -> Assert.assertTrue(response.string().contains(url)));
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
```
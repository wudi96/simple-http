package cn.wudi.spider.mime;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author tusu.
 */
public class ContentTypeTest {

  @Test
  public void charset() {
    ContentType contentType = ContentType.parse("text/html; charset=utf-8");
    Assert.assertEquals("text", contentType.getType());
    Assert.assertEquals("html", contentType.getSubtype());
    Assert.assertEquals("utf-8", contentType.getCharset());
    Assert.assertNull(contentType.getBoundary());
  }

  @Test
  public void boundary() {
    ContentType contentType = ContentType.parse("multipart/form-data; boundary=something");
    Assert.assertEquals("multipart", contentType.getType());
    Assert.assertEquals("form-data", contentType.getSubtype());
    Assert.assertNull(contentType.getCharset());
    Assert.assertEquals("something", contentType.getBoundary());
  }
}
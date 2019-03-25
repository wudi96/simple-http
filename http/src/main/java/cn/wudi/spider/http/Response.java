package cn.wudi.spider.http;

import com.alibaba.fastjson.JSON;
import java.io.UnsupportedEncodingException;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import okhttp3.Headers;
import okhttp3.HttpUrl;

/**
 * @author tusu
 */
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Accessors(fluent = true)
public class Response {

  /**
   * {@link #string()}的编码格式
   */
  private String charset;
  /**
   * Response code e.g. 200
   */
  private int code;
  /**
   * Response Header
   */
  private Headers headers;
  /**
   * Response Body
   */
  private byte[] body;
  /**
   * 当前的url
   */
  private HttpUrl url;

  /**
   * Last Request
   */
  private Request request;

  /**
   * Base Class<T> JSON.parse Response.string()
   */
  public <T> T json(Class<T> clazz) {
    return JSON.parseObject(string(), clazz);
  }

  /**
   * 根据charset转换body为string
   *
   * @param charset 编码
   * @return string
   */
  private String string(String charset) {
    try {
      return new String(body, charset);
    } catch (UnsupportedEncodingException e) {
      throw new HttpException(e);
    }
  }

  /**
   * 默认使用{@link Request#getCharset}的charset 如果需要手动更改则{@link Response#charset(String)}
   *
   * @see Response#string(String)
   */
  public String string() throws HttpException {
    return string(charset);
  }

  /**
   * Response string's charset
   *
   * @param charset response.body的编码
   * @return Response
   */
  public Response charset(String charset) {
    this.charset = charset;
    return this;
  }
}

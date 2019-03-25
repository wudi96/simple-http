package cn.wudi.spider.http;

/**
 * Created by tusu on 2017/9/12.
 */
public class HttpException extends RuntimeException {

  public static final int UNKNOWN_EXCEPTION = 1;
  public static final int TIMEOUT_EXCEPTION = 2;
  public static final int PROXY_EXCEPTION = 3;
  public static final int SSL_EXCEPTION = 4;
  public static final int CONNECT_FAIL_EXCEPTION = 5;

  private int code;

  public HttpException() {
    super();
  }

  public HttpException(String message, Throwable cause) {
    super(message, cause);
  }

  public HttpException(String message) {
    super(message);
  }

  public HttpException(Throwable cause) {
    super(cause);
  }

  public HttpException(int code) {
    super();
    this.code = code;
  }

  public HttpException(int code, String message, Throwable cause) {
    super(message, cause);
    this.code = code;
  }

  public HttpException(int code, String message) {
    super(message);
    this.code = code;
  }

  public HttpException(int code, Throwable cause) {
    super(cause);
    this.code = code;
  }

  public int getCode() {
    return code;
  }

  public void setCode(int code) {
    this.code = code;
  }
}

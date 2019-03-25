package cn.wudi.spider.http;

import java.util.Objects;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import okhttp3.HttpUrl;

/**
 * 所有中会影响到Client构建的参数选项，用于对比是否发生过变化，如果发生变化则重新生成client
 *
 * @author tusu.
 */
@Setter
@Getter
@Accessors(chain = true)
public class ClientOption {

  public static final Boolean FOLLOW_REDIRECTS = true;

  public static final ClientOption DEFAULT = new ClientOption();

  private HttpUrl httpProxy;

  private Long timeout;

  private Boolean followRedirects = FOLLOW_REDIRECTS;

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    ClientOption that = (ClientOption) o;

    if (!Objects.equals(httpProxy, that.httpProxy)) {
      return false;
    }
    if (!Objects.equals(timeout, that.timeout)) {
      return false;
    }
    return Objects.equals(followRedirects, that.followRedirects);
  }

  @Override
  public int hashCode() {
    int result = httpProxy != null ? httpProxy.hashCode() : 0;
    result = 31 * result + (timeout != null ? timeout.hashCode() : 0);
    result = 31 * result + (followRedirects != null ? followRedirects.hashCode() : 0);
    return result;
  }
}

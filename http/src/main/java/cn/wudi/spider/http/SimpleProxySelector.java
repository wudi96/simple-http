package cn.wudi.spider.http;

import java.io.IOException;
import java.net.Proxy;
import java.net.ProxySelector;
import java.net.SocketAddress;
import java.net.URI;
import java.util.List;

/**
 * @author tusu.
 */
public class SimpleProxySelector extends ProxySelector {

  private List<Proxy> proxies;
  private int i;

  public SimpleProxySelector(List<Proxy> proxies) {
    this.proxies = proxies;
  }

  @Override
  public List<Proxy> select(URI uri) {
    return proxies;
  }

  @Override
  public void connectFailed(URI uri, SocketAddress sa, IOException ioe) {
    i++;
    if (i == proxies.size()) {
      throw new HttpException(HttpException.PROXY_EXCEPTION, "Proxy is unavailable", ioe);
    }
  }
}

package cn.wudi.spider.http;

import java.util.ArrayList;
import java.util.List;
import okhttp3.OkHttpClient.Builder;

/**
 * @author tusu.
 */
public class CompositeClientConfigurer implements ClientConfigurer {

  private List<ClientConfigurer> configurers = new ArrayList<>();

  public CompositeClientConfigurer() {
  }

  public CompositeClientConfigurer(CompositeClientConfigurer compositeClientConfigurer) {
    configurers.addAll(compositeClientConfigurer.configurers);
  }

  public CompositeClientConfigurer addConfigurer(ClientConfigurer configurer) {
    this.configurers.add(configurer);
    return this;
  }

  public CompositeClientConfigurer clear() {
    this.configurers.clear();
    return this;
  }

  @Override
  public void config(Builder builder) {
    for (ClientConfigurer configurer : configurers) {
      configurer.config(builder);
    }
  }
}

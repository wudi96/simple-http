package cn.wudi.spider.http;

import okhttp3.OkHttpClient;

/**
 * Client扩展
 *
 * @author tusu.
 */
public interface ClientConfigurer extends Configurer<OkHttpClient.Builder> {

}

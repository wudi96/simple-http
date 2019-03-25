package cn.wudi.spider.http;

import okhttp3.HttpUrl;

/**
 * @author tusu
 */
public interface ProxyFactory {

  HttpUrl get();
}

package cn.wudi.spider.http;

/**
 * @author tusu.
 */
public interface Configurer<T> {

  void config(T obj);
}

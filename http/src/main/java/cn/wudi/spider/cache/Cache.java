package cn.wudi.spider.cache;

import com.alibaba.fastjson.JSON;
import javax.annotation.Nullable;

/**
 * Created by tusu on 2017/9/12.
 */
public interface Cache {

  @Nullable
  String get(String key);

  void set(String key, String value);

  default <T> T get(String key, Class<T> clazz, boolean local) {
    return null;
  }

  default void set(String key, Object value) {
  }

  class LocalCache implements Cache {

    protected com.github.benmanes.caffeine.cache.Cache<String, Object> cache;

    public LocalCache(com.github.benmanes.caffeine.cache.Cache<String, Object> cache) {
      this.cache = cache;
    }

    @Nullable
    @Override
    public String get(String key) {
      Object o = cache.getIfPresent(key);
      if (o == null) {
        return null;
      }
      if (o instanceof String) {
        return (String) o;
      }
      return JSON.toJSONString(o);
    }

    @Override
    public void set(String key, String value) {
      cache.put(key, value);
    }

    @Override
    public <T> T get(String key, Class<T> clazz, boolean local) {
      Object o = cache.getIfPresent(key);
      if (o == null || !clazz.isAssignableFrom(o.getClass())) {
        return null;
      }
      //noinspection unchecked
      return (T) o;
    }

    @Override
    public void set(String key, Object value) {
      cache.put(key, value);
    }
  }
}

package cn.wudi.spider.mime;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.TimeUnit;
import javax.annotation.Nullable;
import org.apache.commons.io.IOUtils;

/**
 * @author tusu
 */
public class MimeType {

  private static JSONObject extensions;
  private static Cache<String, MimeType> cache = Caffeine.newBuilder()
      .maximumSize(128)
      .expireAfterAccess(1L, TimeUnit.MINUTES)
      .build();

  static {
    define("standard.json", "other.json");
  }

  private final ContentType contentType;
  private final String extension;

  private MimeType(ContentType contentType, String extension) {
    this.contentType = contentType;
    this.extension = extension;
  }

  /**
   * 解析Content-Type
   *
   * @param string application/json; charset=utf-8
   * @return type: application, subtype: json, charset: utf-8, extension: json
   */
  @Nullable
  public static MimeType parse(String string) {
    // 缓存中存在则直接返回
    MimeType v = cache.getIfPresent(string);
    if (v != null) {
      return v;
    }
    ContentType contentType = ContentType.parse(string);
    if (contentType == null) {
      return null;
    }

    String suffix = MimeType.lookupSuffix(contentType.getType(), contentType.getSubtype());
    MimeType mimeType = new MimeType(contentType, suffix);
    // 存入缓存
    cache.put(string, mimeType);
    return mimeType;
  }

  /**
   * 根据Content-Type返回文件extension
   *
   * @param type application
   * @param subtype json
   * @return json
   */
  public static String lookupSuffix(String type, String subtype) {
    int index = subtype.indexOf("+");
    if (index != -1 && index != subtype.length() - 1) {
      return subtype.substring(index, subtype.length() - 1);
    }
    JSONArray extensionsJSONArray = extensions
        .getJSONArray(type + "/" + subtype);
    if (extensionsJSONArray == null || extensionsJSONArray.size() == 0) {
      return "";
    }
    String suffix = extensionsJSONArray.getString(0);
    if (suffix == null) {
      return "";
    }
    return suffix;
  }

  private static void define(String... paths) {
    ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
    for (String path : paths) {
      try (InputStream inputStream = contextClassLoader.getResourceAsStream(path)) {
        define(
            JSON.parseObject(new String(IOUtils.readFully(inputStream, inputStream.available()))),
            true);
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
  }

  public static void define(JSONObject json, boolean force) {
    if (extensions == null) {
      extensions = json;
      return;
    }
    json.forEach((key, value) -> {
      JSONArray extensionsJSONArray = extensions.getJSONArray(key);
      if (extensionsJSONArray == null || force) {
        extensions.put(key, value);
        return;
      }
      extensionsJSONArray.addAll((JSONArray) value);
    });
  }

  public String type() {
    return contentType.getType();
  }

  public String subtype() {
    return contentType.getSubtype();
  }

  @Nullable
  public String charset() {
    return contentType.getCharset();
  }

  @Nullable
  public String boundary() {
    return contentType.getBoundary();
  }

  public String extension() {
    return extension;
  }
}

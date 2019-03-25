package cn.wudi.spider.http.cookie;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.parser.DefaultJSONParser;
import com.alibaba.fastjson.parser.JSONToken;
import com.alibaba.fastjson.parser.deserializer.ObjectDeserializer;
import java.lang.reflect.Type;
import okhttp3.Cookie.Builder;

/**
 * @author tusu.
 */
public class CookieDeserializer implements ObjectDeserializer {

  @Override
  public <T> T deserialze(DefaultJSONParser parser, Type type, Object fieldName) {
    JSONObject object = parser.parseObject();

    Builder builder = new Builder();
    String domain = object.getString("domain");
    builder.name(object.getString("name"))
        .value(object.getString("value"))
        .expiresAt(object.getLong("expiresAt"))
        .domain(domain)
        .path(object.getString("path"));
    if (object.getBooleanValue("secure")) {
      builder.secure();
    }
    if (object.getBooleanValue("httpOnly")) {
      builder.httpOnly();
    }
    if (object.getBooleanValue("hostOnly")) {
      builder.hostOnlyDomain(domain);
    }
    //noinspection unchecked
    return (T) builder.build();
  }

  @Override
  public int getFastMatchToken() {
    return JSONToken.LBRACE;
  }
}
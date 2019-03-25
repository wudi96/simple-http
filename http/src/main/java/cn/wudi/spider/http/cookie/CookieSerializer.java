package cn.wudi.spider.http.cookie;

import com.alibaba.fastjson.serializer.JSONSerializer;
import com.alibaba.fastjson.serializer.ObjectSerializer;
import com.alibaba.fastjson.serializer.SerializeWriter;
import okhttp3.Cookie;

/**
 * @author tusu.
 */
public class CookieSerializer implements ObjectSerializer {

  @Override
  public void write(JSONSerializer serializer, Object object, Object fieldName,
      java.lang.reflect.Type fieldType, int features) {
    SerializeWriter out = serializer.getWriter();
    if (object == null) {
      out.writeNull();
      return;
    }
    Cookie cookie = (Cookie) object;
    out.write("{");
    out.writeString("name", ':');
    out.writeString(cookie.name());
    out.writeFieldValue(',', "value", cookie.value());
    out.writeFieldValue(',', "expiresAt", cookie.expiresAt());
    out.writeFieldValue(',', "domain", cookie.domain());
    out.writeFieldValue(',', "path", cookie.path());
    out.writeFieldValue(',', "secure", cookie.secure());
    out.writeFieldValue(',', "httpOnly", cookie.httpOnly());
    out.writeFieldValue(',', "hostOnly", cookie.hostOnly());
    out.write("}");
  }
}

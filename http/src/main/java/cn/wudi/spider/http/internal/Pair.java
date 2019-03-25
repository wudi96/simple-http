package cn.wudi.spider.http.internal;


import com.alibaba.fastjson.annotation.JSONType;
import com.alibaba.fastjson.serializer.JSONSerializer;
import com.alibaba.fastjson.serializer.ObjectSerializer;
import com.alibaba.fastjson.serializer.SerializeWriter;

/**
 * @author tusu
 */
@JSONType(serializer = Pair.SL.class)
public class Pair<L, R> {

  private final L left;
  private final R right;

  public Pair(L left, R right) {
    this.left = left;
    this.right = right;
  }

  public L left() {
    return left;
  }

  public R right() {
    return right;
  }

  @Override
  public String toString() {
    return "{\"left\":" + left + "," +
        "\"right\":" + right + '}';
  }

  public static class SL implements ObjectSerializer {

    @Override
    public void write(JSONSerializer serializer, Object object, Object fieldName,
        java.lang.reflect.Type fieldType, int features) {
      SerializeWriter out = serializer.getWriter();
      if (object == null) {
        serializer.getWriter().writeNull();
        return;
      }
      out.write(object.toString());
    }
  }
}

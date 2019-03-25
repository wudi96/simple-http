package cn.wudi.spider.http;

import cn.wudi.spider.http.internal.Pair;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;
import java.util.function.Consumer;
import javax.annotation.Nullable;
import okio.Buffer;

/**
 * @author tusu.
 */
public class HttpHelper {

  /**
   * Request Method: GET or ie: UTF-8
   *
   * @param line Chrome parsed parameter
   * @param delim `:` or `=`
   * @param func Trim name and value as result send to func
   */
  static void parsedLine(String line, String delim, FuncNameAndValue func) {
    int index = line.indexOf(delim);
    if (index == -1) {
      throw new IllegalArgumentException("Unexpected " + line);
    }
    func.accept(line.substring(0, index).trim(), line.substring(index + 1).trim());
  }

  /**
   * @param lines Chrome parsed multiple line
   * @param func Trim name and value as result send to func
   * @see HttpHelper#parsedLine(String, String, FuncNameAndValue) multiple line
   */
  static void parsedLines(String lines, FuncNameAndValue func) {
    for (String line : lines.split("\n")) {
      String[] split = line.split(":", 2);
      if (split.length < 2) {
        func.accept(split[0].trim(), "");
        continue;
      }
      func.accept(split[0].trim(), split[1].trim());
    }
  }

  /**
   * 将http urlencode类型的参数处理后传入list
   *
   * @param list querys or forms
   * @param name name
   * @param value value
   * @param encoded isEncoded
   * @param single needSingleTon
   */
  static void urlencodeParameter(List<Pair<String, Pair<String, Boolean>>> list,
      @Nullable String name, @Nullable String value, Boolean encoded, Boolean single) {
    if (name == null) {
      return;
    }
    if (value == null) {
      value = "";
    }
    // 获取pairs
    if (single) {
      for (int i = 0; i < list.size(); i++) {
        Pair<String, Pair<String, Boolean>> nv = list.get(i);
        if (nv.left().equals(name)) {
          list.remove(nv);
          i--;
        }
      }
    }
    list.add(new Pair<>(name, new Pair<>(value, encoded)));
  }

  static Buffer urlEncode(List<Pair<String, Pair<String, Boolean>>> params, String charset) {
    Buffer buffer = new Buffer();
    join(
        (Pair<String, Pair<String, Boolean>> pair) -> {
          String key = pair.left();
          String value = pair.right().left();
          if (!pair.right().right()) {
            try {
              key = URLEncoder.encode(key, charset);
              value = URLEncoder.encode(value, charset);
            } catch (UnsupportedEncodingException e) {
              throw new HttpException(e);
            }
          }
          buffer.write(key.getBytes()).writeByte('=').write(value.getBytes());
        },
        () -> buffer.writeByte('&'),
        params
    );
    return buffer;
  }

  /**
   * join
   *
   * @param r1 遍历list
   * @param r2 r1之后调用
   */
  public static <T> void join(Consumer<T> r1, Runnable r2, List<T> list) {
    int size = list.size();
    switch (size) {
      case 0:
        return;
      case 1:
        r1.accept(list.get(0));
        return;
      default:
        r1.accept(list.get(0));
        for (int i = 1; i < size; i++) {
          r2.run();
          r1.accept(list.get(i));
        }
    }
  }
}

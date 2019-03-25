package cn.wudi.spider.mime;

import cn.wudi.spider.text.StringTokenizer;
import java.util.Locale;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * https://developer.mozilla.org/en-US/docs/Web/HTTP/Headers/Content-Type
 *
 * @author tusu.
 */
@Getter
@Setter
@Accessors(chain = true)
public class ContentType {

  private static final String SLASH = "/";
  private static final String SEMICOLON = ";";
  private static final String EQUALS = "=";
  @NonNull
  private String type;
  @NonNull
  private String subtype;
  private String charset;
  private String boundary;

  public static ContentType parse(String string) {
    if (string == null) {
      return null;
    }
    StringTokenizer tokenizer = new StringTokenizer(string.trim(), SLASH + SEMICOLON + EQUALS,
        true);

    // If contentType = ""
    if (!tokenizer.hasMoreTokens()) {
      return null;
    }
    String type = tokenizer.nextToken().trim().toLowerCase(Locale.US);

    // If contentType = "application"
    if (!tokenizer.hasMoreTokens()) {
      return null;
    }

    String slash = tokenizer.nextToken().trim();
    // If contentType = "application;"
    if (!SLASH.equals(slash)) {
      return null;
    }

    // If contentType = "application/json
    if (!tokenizer.hasMoreTokens()) {
      return null;
    }

    String subtype = tokenizer.nextToken().trim().toLowerCase(Locale.US);
    if (!tokenizer.hasMoreTokens()) {
      return new ContentType().setType(type).setSubtype(subtype);
    }

    String semicolon = tokenizer.nextToken().trim();
    // If contentType = "application/json=" it's invalid! But we're lenient
    if (!SEMICOLON.equals(semicolon)) {
      return new ContentType().setType(type).setSubtype(subtype);
    }

    // If contentType = "application/json;"
    if (!tokenizer.hasMoreTokens()) {
      return new ContentType().setType(type).setSubtype(subtype);
    }
    String key = tokenizer.nextToken().trim().toLowerCase();

    // If contentType = "application/json; charset=utf-8"
    if ("charset".equals(key)) {
      String value = parseValue(tokenizer);
      if (value != null) {
        return new ContentType().setType(type).setSubtype(subtype)
            .setCharset(value.toLowerCase(Locale.US));
      }
      return new ContentType().setType(type).setSubtype(subtype);
    }

    // If contentType = "application/json; boundary=3d6b6a416f9b5"
    if ("boundary".equals(key)) {
      String value = parseValue(tokenizer);
      if (value != null) {
        return new ContentType().setType(type).setSubtype(subtype).setBoundary(value);
      }
      return new ContentType().setType(type).setSubtype(subtype);
    }

    return new ContentType().setType(type).setSubtype(subtype);
  }

  /**
   * key=value
   *
   * @param tokenizer StringTokenizer
   * @return value
   */
  private static String parseValue(StringTokenizer tokenizer) {
    if (!tokenizer.hasMoreTokens()) {
      return null;
    }
    String equals = tokenizer.nextToken().trim();
    if (!"=".equals(equals)) {
      return null;
    }
    if (!tokenizer.hasMoreTokens()) {
      return null;
    }
    return tokenizer.nextToken().trim();
  }

  @Override
  public String toString() {
    StringBuilder stringBuilder = new StringBuilder()
        .append(type)
        .append("/")
        .append(subtype);
    // 必须写在上面，charset总是存在
    if (boundary != null) {
      stringBuilder.append("; boundary=").append(boundary);
      return stringBuilder.toString();
    }
    if (charset != null) {
      stringBuilder.append("; charset=").append(charset);
      return stringBuilder.toString();
    }
    return stringBuilder.toString();
  }

  public boolean isValid() {
    return type != null && subtype != null;
  }
}

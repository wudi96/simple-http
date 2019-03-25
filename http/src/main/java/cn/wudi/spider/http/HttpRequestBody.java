package cn.wudi.spider.http;

import java.io.IOException;
import javax.annotation.Nullable;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import okio.Buffer;
import okio.BufferedSink;

/**
 * @author tusu.
 */
public class HttpRequestBody extends RequestBody {

  private MediaType mediaType;
  private Buffer body;
  private int pdu;

  public HttpRequestBody(MediaType mediaType, Buffer body, int pdu) {
    this.mediaType = mediaType;
    this.body = body;
    this.pdu = pdu;
  }

  @Nullable
  @Override
  public MediaType contentType() {
    return mediaType;
  }

  @Override
  public long contentLength() {
    return body.size();
  }

  @Override
  public void writeTo(BufferedSink sink) throws IOException {
    if (pdu != -1) {
      long written = 0;
      long length = body.size();
      while (written < length) {
        long len = written + pdu > length ? length - written : pdu;
        sink.write(body, len);
        written += pdu;
        sink.flush();
      }
      return;
    }
    body.copyTo(sink.buffer(), 0L, body.size());
  }
}

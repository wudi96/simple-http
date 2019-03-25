package cn.wudi.spider.http;

import java.io.IOException;
import lombok.extern.slf4j.Slf4j;
import okhttp3.Interceptor;
import okhttp3.Response;

/**
 * 当任务完成后输出时间日志
 *
 * @author tusu.
 */
@Slf4j
@Init
public class LogInterceptor implements Interceptor {

  @Override
  public Response intercept(Chain chain) throws IOException {
    okhttp3.Request request = chain.request();
    long time = System.currentTimeMillis();
    okhttp3.Response response = chain.proceed(request);
    log.info("[method:" + request.method() + "] " +
        "[url:" + request.url() + "] " +
        "[time:" + (System.currentTimeMillis() - time) + "]");
    return response;
  }
}

package cn.wudi.spider.http;

import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

/**
 * @author tusu
 */
public class SSLSocketClient {

  private static final X509Certificate[] X_509_CERTIFICATES = {};
  private static final X509TrustManager X_509_TRUST_MANAGER = new X509TrustManager() {
    @Override
    public void checkClientTrusted(X509Certificate[] chain, String authType) {
    }

    @Override
    public void checkServerTrusted(X509Certificate[] chain, String authType) {
    }

    @Override
    public X509Certificate[] getAcceptedIssuers() {
      return X_509_CERTIFICATES;
    }
  };
  private static final TrustManager[] TRUST_MANAGERS = {X_509_TRUST_MANAGER};

  public static SSLSocketFactory getSSLSocketFactory() {
    try {
      SSLContext sslContext = SSLContext.getInstance("SSL");
      sslContext.init(null, TRUST_MANAGERS, null);
      return sslContext.getSocketFactory();
    } catch (NoSuchAlgorithmException | KeyManagementException e) {
      throw new RuntimeException(e);
    }
  }

  public static X509TrustManager getTrustManager() {
    return X_509_TRUST_MANAGER;
  }

  public static HostnameVerifier getHostnameVerifier() {
    return (s, sslSession) -> true;
  }
}
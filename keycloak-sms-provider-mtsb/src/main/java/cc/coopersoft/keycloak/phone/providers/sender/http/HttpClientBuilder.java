package cc.coopersoft.keycloak.phone.providers.sender.http;

import lombok.NonNull;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManager;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManagerBuilder;
import org.apache.hc.client5.http.ssl.NoopHostnameVerifier;
import org.apache.hc.client5.http.ssl.SSLConnectionSocketFactory;
import org.apache.hc.core5.ssl.SSLContexts;
import org.apache.hc.core5.util.Timeout;
import org.jboss.logging.Logger;

import javax.net.ssl.SSLContext;

public class HttpClientBuilder {

    private static final Logger logger = Logger.getLogger(HttpClientBuilder.class);
    private static final int COMMON_TIMEOUT = 10;

    @NonNull
    public static CloseableHttpClient build() {
        SSLConnectionSocketFactory sslSocketFactory = getSslConnectionSocketFactory();
        PoolingHttpClientConnectionManager connectionManager = PoolingHttpClientConnectionManagerBuilder.create()
                .setSSLSocketFactory(sslSocketFactory)
                .setMaxConnTotal(50)
                .setMaxConnPerRoute(10)
                .build();
        RequestConfig config = getRequestConfig();
        return HttpClients.custom()
                .setConnectionManager(connectionManager)
                .setDefaultRequestConfig(config)
                .evictExpiredConnections()
                .evictIdleConnections(Timeout.ofSeconds(10))
                .build();
    }

    @NonNull
    private static RequestConfig getRequestConfig() {
        //noinspection deprecation
        return RequestConfig.custom()
                .setConnectTimeout(Timeout.ofSeconds(COMMON_TIMEOUT))
                .setConnectionRequestTimeout(Timeout.ofSeconds(COMMON_TIMEOUT))
                .setResponseTimeout(Timeout.ofSeconds(COMMON_TIMEOUT))
                .build();
    }

    @NonNull
    private static SSLConnectionSocketFactory getSslConnectionSocketFactory() {
        try {
            SSLContext sslContext = SSLContexts.custom()
                    .loadTrustMaterial(null, (chain, authType) -> true) // доверяем всем сертификатам
                    .build();

            return new SSLConnectionSocketFactory(
                    sslContext,
                    NoopHostnameVerifier.INSTANCE // игнор hostname
            );
        } catch (Exception e) {
            logger.error("Ошибка при создании SSLConnectionSocketFactory", e);
            throw new RuntimeException("SSL init failed", e);
        }
    }
}

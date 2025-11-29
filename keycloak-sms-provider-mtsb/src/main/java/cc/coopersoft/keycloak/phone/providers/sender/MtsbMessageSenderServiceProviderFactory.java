package cc.coopersoft.keycloak.phone.providers.sender;

import cc.coopersoft.keycloak.phone.providers.spi.messagesender.MessageSenderService;
import cc.coopersoft.keycloak.phone.providers.spi.messagesender.MessageSenderServiceProviderFactory;
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
import org.keycloak.Config;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;

import javax.net.ssl.SSLContext;
import java.io.IOException;

public class MtsbMessageSenderServiceProviderFactory implements MessageSenderServiceProviderFactory {

    private static final Logger logger = Logger.getLogger(MtsbMessageSenderServiceProviderFactory.class);
    private CloseableHttpClient httpClient;
    private static final int COMMON_TIMEOUT = 10;

    @Override
    public MessageSenderService create(KeycloakSession keycloakSession) {
        return new MtsbSmsSenderService(keycloakSession, httpClient);
    }

    @Override
    public void init(Config.Scope config) {
        //this.baseUrl = config.get("baseUrl");
        httpClient = getCloseableHttpClient();
    }

    @Override
    public void postInit(KeycloakSessionFactory keycloakSessionFactory) {
    }

    @Override
    public void close() {
        try {
            httpClient.close();
        } catch (IOException e) {
            // логирование
        }
    }

    @Override
    public String getId() {
        return "mtsb";
    }

    @NonNull
    private CloseableHttpClient getCloseableHttpClient() {
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
    private RequestConfig getRequestConfig() {
        //noinspection deprecation
        return RequestConfig.custom()
                .setConnectTimeout(Timeout.ofSeconds(COMMON_TIMEOUT))
                .setConnectionRequestTimeout(Timeout.ofSeconds(COMMON_TIMEOUT))
                .setResponseTimeout(Timeout.ofSeconds(COMMON_TIMEOUT))
                .build();
    }

    @NonNull
    private SSLConnectionSocketFactory getSslConnectionSocketFactory() {
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

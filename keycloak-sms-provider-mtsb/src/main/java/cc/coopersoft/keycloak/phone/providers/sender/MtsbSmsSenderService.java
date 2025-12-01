package cc.coopersoft.keycloak.phone.providers.sender;

import cc.coopersoft.keycloak.phone.providers.sender.dto.SmsDto;
import cc.coopersoft.keycloak.phone.providers.spi.messagesender.FullSmsSenderAbstractService;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.jboss.logging.Logger;
import org.jetbrains.annotations.NotNull;
import org.keycloak.connections.httpclient.HttpClientProvider;
import org.keycloak.models.KeycloakSession;
import org.keycloak.util.JsonSerialization;

import java.net.URI;
import java.nio.charset.StandardCharsets;

public class MtsbSmsSenderService extends FullSmsSenderAbstractService {

    private static final Logger logger = Logger.getLogger(MtsbSmsSenderService.class);
    private final CloseableHttpClient httpClient;

    public MtsbSmsSenderService(KeycloakSession session) {
        super(session);
        this.httpClient = session.getProvider(HttpClientProvider.class)
                .getHttpClient();
    }

    @Override
    public void sendMessage(String phoneNumber) {
        logger.info(String.format("Sending to: %s ", phoneNumber));
        logger.info("Build request dto");
        SmsDto request = SmsDto.builder()
                .sms("test")
                .build();
        HttpPost post = createPostRequest(request);
        try {
            HttpResponse httpResponse = httpClient.execute(post);
            logger.info("Post запрос выполнен");
            logger.info(httpResponse.toString());
        } catch (Exception e) {
            logger.error("Ошибка при выполнении запроса или парсинге ответа", e);
            throw new RuntimeException(e);
        }
    }

    @NotNull
    private static HttpPost createPostRequest(SmsDto request) {
        try {
            final URI uri = new URIBuilder("https://host.docker.internal:8983/sms")
                    .setCharset(StandardCharsets.UTF_8)
                    .build();
            HttpPost post = new HttpPost(uri);
            final String requestData = JsonSerialization.writeValueAsString(request);
            post.setEntity(new StringEntity(requestData, StandardCharsets.UTF_8));
            final String logMessage = String.format("Url post запроса: %s,\nHeaders запроса: %s\nBody запроса: %s",
                    uri.toString(),
                    null,
                    requestData);
            logger.info(logMessage);
            return post;
        } catch (Exception e) {
            logger.error("Ошибка при формировании POST запроса", e);
            throw new RuntimeException(e);
        }
    }

    @Override
    public void close() {
    }

}

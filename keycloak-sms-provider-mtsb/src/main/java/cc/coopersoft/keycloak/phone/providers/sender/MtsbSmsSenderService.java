package cc.coopersoft.keycloak.phone.providers.sender;

import cc.coopersoft.keycloak.phone.providers.representations.TokenCodeRepresentation;
import cc.coopersoft.keycloak.phone.providers.sender.dto.SmsRequestDto;
import cc.coopersoft.keycloak.phone.providers.sender.dto.SmsResponseDto;
import cc.coopersoft.keycloak.phone.providers.spi.messagesender.FullSmsSenderAbstractService;
import cc.coopersoft.keycloak.phone.providers.spi.phoneverify.PhoneVerificationCodeProvider;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;
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

    private PhoneVerificationCodeProvider getTokenCodeService() {
        return session.getProvider(PhoneVerificationCodeProvider.class);
    }

    public MtsbSmsSenderService(KeycloakSession session) {
        super(session);
        this.httpClient = session.getProvider(HttpClientProvider.class)
                .getHttpClient();
    }

    @Override
    public void sendMessage(String phoneNumber) {
        logger.info(String.format("Sending SMS to: %s ", phoneNumber));

        SmsRequestDto request = SmsRequestDto.builder()
                .sms("test")
                .build();

        HttpPost post = createPostRequest(request);

        try {
            HttpResponse httpResponse = httpClient.execute(post);

            int status = httpResponse.getStatusLine().getStatusCode();
            String body = EntityUtils.toString(httpResponse.getEntity(), StandardCharsets.UTF_8);

            logger.infof("HTTP status: %s", status);
            logger.infof("HTTP body: %s", body);

            SmsResponseDto dto = JsonSerialization.readValue(body, SmsResponseDto.class);

            session.setAttribute("REQUEST_ID", dto.getRequestId());

        } catch (Exception e) {
            logger.error("Error on executing or parsing response", e);
            throw new RuntimeException(e);
        }
    }

    @NotNull
    private static HttpPost createPostRequest(SmsRequestDto request) {
        try {
            final URI uri = new URIBuilder("https://host.docker.internal:8983/sms")
                    .setCharset(StandardCharsets.UTF_8)
                    .build();
            HttpPost post = new HttpPost(uri);
            final String requestData = JsonSerialization.writeValueAsString(request);
            post.setEntity(new StringEntity(requestData, StandardCharsets.UTF_8));
            final String logMessage = String.format("POST Url: %s,\nHeaders: %s\nBody: %s",
                    uri.toString(),
                    null,
                    requestData);
            logger.info(logMessage);
            return post;
        } catch (Exception e) {
            logger.error("Error on creating POST request", e);
            throw new RuntimeException(e);
        }
    }

    @Override
    public void close() {
    }

}

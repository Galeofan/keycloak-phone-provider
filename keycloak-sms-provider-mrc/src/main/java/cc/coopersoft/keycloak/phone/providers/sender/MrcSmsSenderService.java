package cc.coopersoft.keycloak.phone.providers.sender;

import cc.coopersoft.keycloak.phone.providers.config.GraviteeIntegrationConfig;
import cc.coopersoft.keycloak.phone.providers.exception.BuildRequestException;
import cc.coopersoft.keycloak.phone.providers.exception.ExternalOtpValidationException;
import cc.coopersoft.keycloak.phone.providers.exception.MessageSendException;
import cc.coopersoft.keycloak.phone.providers.exception.RequestExecutionException;
import cc.coopersoft.keycloak.phone.providers.sender.dto.SmsRequestDto;
import cc.coopersoft.keycloak.phone.providers.sender.dto.SmsResponseDto;
import cc.coopersoft.keycloak.phone.providers.spi.messagesender.FullSmsSenderAbstractService;
import org.apache.http.Header;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;
import org.jboss.logging.Logger;
import org.jetbrains.annotations.NotNull;
import org.keycloak.Config;
import org.keycloak.connections.httpclient.HttpClientProvider;
import org.keycloak.models.KeycloakSession;
import org.keycloak.util.JsonSerialization;
import org.keycloak.utils.StringUtil;

import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.keycloak.utils.MediaType.APPLICATION_JSON;

public class MrcSmsSenderService extends FullSmsSenderAbstractService {

    private final Config.Scope config;

    private static final Logger logger = Logger.getLogger(MrcSmsSenderService.class);
    private static final String GRAVITEE_KEY_HDR_NAME = "x-gravitee-api-key";
    private static final String REQUEST_ID_HDR_NAME = "x-request-id";
    private final CloseableHttpClient httpClient;

    public MrcSmsSenderService(KeycloakSession session, Config.Scope config) {
        super(session);
        this.config = config;
        this.httpClient = session.getProvider(HttpClientProvider.class)
                .getHttpClient();
    }

    @Override
    public void sendMessage(String phoneNumber) throws MessageSendException {
        logger.info(String.format("Sending SMS to: %s ", phoneNumber));

        try {
            SmsRequestDto requestBody = buildSmsRequestDto(phoneNumber);
            HttpPost postRequest = buildPostRequest(requestBody);

            HttpResponseWrapper httpResponse = executeRequest(postRequest);

            logResponse(httpResponse);

            if (httpResponse.status() != HttpStatus.SC_OK) {
                throw new ExternalOtpValidationException("Unexpected status: " + httpResponse.status());
            }

            SmsResponseDto responseDto = JsonSerialization.readValue(httpResponse.body(), SmsResponseDto.class);

            if (StringUtil.isBlank(responseDto.getOtpId())) {
                throw new MessageSendException("Error response structure");
            }

            session.setAttribute("OTP_ID", responseDto.getOtpId());
        } catch (RequestExecutionException e) {
            throw new MessageSendException("Error execution request", e.getCause());
        } catch (ExternalOtpValidationException e) {
            throw new MessageSendException("Bad status code", e.getCause());
        } catch (IOException e) {
            throw new MessageSendException("Error parse response", e.getCause());
        } catch (Exception e) {
            throw new MessageSendException("Error SMS sending", e.getCause());
        }
    }

    @NotNull
    private SmsRequestDto buildSmsRequestDto(String phoneNumber) {
        return SmsRequestDto.builder()
                .phoneNumber(phoneNumber)
                .build();
    }

    @NotNull
    private HttpPost buildPostRequest(SmsRequestDto request) throws BuildRequestException {
        try {
            GraviteeIntegrationConfig.SmsConfig cfg = GraviteeIntegrationConfig.smsFrom(config, session);
            final URI uri = new URIBuilder(cfg.smsSendUrl())
                    .setCharset(StandardCharsets.UTF_8)
                    .build();
            HttpPost postRequest = new HttpPost(uri);
            postRequest.setHeader(HttpHeaders.CONTENT_TYPE, APPLICATION_JSON);
            postRequest.setHeader(GRAVITEE_KEY_HDR_NAME, cfg.apiKey());
            postRequest.setHeader(REQUEST_ID_HDR_NAME, UUID.randomUUID().toString());
            final String requestBody = JsonSerialization.writeValueAsPrettyString(request);
            postRequest.setEntity(new StringEntity(requestBody, StandardCharsets.UTF_8));
            logger.info("""
                    
                    ----------[HTTP POST Request]----------
                    Url: {}
                    Headers: {}
                    Body:
                    {}""".replace("{}", "%s")
                    .formatted(uri, Arrays.toString(postRequest.getAllHeaders()), requestBody));
            return postRequest;
        } catch (Exception e) {
            logger.error("Error on creating POST request", e);
            throw new BuildRequestException(e.getMessage(), e.getCause());
        }
    }

    @NotNull
    private HttpResponseWrapper executeRequest(HttpPost post) throws RequestExecutionException, ExternalOtpValidationException {
        try (CloseableHttpResponse response = httpClient.execute(post)) {
            int status = response.getStatusLine().getStatusCode();
            String body = EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);
            return new HttpResponseWrapper(status, body, response.getAllHeaders());
        } catch (IOException e) {
            throw new RequestExecutionException("Error to execute request", e.getCause());
        }
    }

    private void logResponse(HttpResponseWrapper response) {
        String headers = Arrays.stream(response.headers())
                .map(h -> h.getName() + ":" + h.getValue())
                .collect(Collectors.joining(", "));

        logger.info("""
                
                ----------[HTTP POST Response]----------
                Status: {}
                Headers: {}
                Body:
                {}""".replace("{}", "%s")
                .formatted(response.status(), headers, response.body()));
    }

    @Override
    public void close() {
    }

    private record HttpResponseWrapper(int status, String body, Header[] headers) {
    }
}

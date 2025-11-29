package cc.coopersoft.keycloak.phone.providers.sender;

import cc.coopersoft.keycloak.phone.providers.exception.MessageSendException;
import cc.coopersoft.keycloak.phone.providers.sender.http.HttpClientBuilder;
import cc.coopersoft.keycloak.phone.providers.spi.messagesender.FullSmsSenderAbstractService;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.jboss.logging.Logger;
import org.keycloak.models.KeycloakSession;

import java.io.IOException;
import java.util.Random;

public class MtsbSmsSenderService extends FullSmsSenderAbstractService {

    private static final Logger logger = Logger.getLogger(MtsbSmsSenderService.class);
    private final CloseableHttpClient httpClient;

    public MtsbSmsSenderService(KeycloakSession session) {
        super(session);
        this.httpClient = HttpClientBuilder.build();
    }

    @Override
    public void sendMessage(String phoneNumber) throws MessageSendException {
//        HttpPost = getPostMethod();
//        httpClient.execute();

        // here you call the method for sending messages
        logger.info(String.format("Sending to: %s ", phoneNumber));

    }

    @Override
    public void close() {
        try {
            httpClient.close();
        } catch (IOException e) {
            logger.error(e.getMessage(),e);
        }
    }

//    private HttpPost
}

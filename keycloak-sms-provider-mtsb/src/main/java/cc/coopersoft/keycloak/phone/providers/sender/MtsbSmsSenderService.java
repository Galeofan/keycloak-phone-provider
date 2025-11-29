package cc.coopersoft.keycloak.phone.providers.sender;

import cc.coopersoft.keycloak.phone.providers.exception.MessageSendException;
import cc.coopersoft.keycloak.phone.providers.spi.messagesender.FullSmsSenderAbstractService;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.jboss.logging.Logger;
import org.keycloak.models.KeycloakSession;

import java.util.Random;

public class MtsbSmsSenderService extends FullSmsSenderAbstractService {

    private static final Logger logger = Logger.getLogger(MtsbSmsSenderService.class);
    private final CloseableHttpClient httpClient;

    public MtsbSmsSenderService(KeycloakSession session, CloseableHttpClient httpClient) {
        super(session);
        this.httpClient = httpClient;
    }

    @Override
    public void sendMessage(String phoneNumber) throws MessageSendException {
//        HttpPost = getPostMethod();
//        httpClient.execute();

        // here you call the method for sending messages
        logger.info(String.format("Sending to: %s ", phoneNumber));

        // simulate a failure
        if (new Random().nextInt(10) % 5 == 0) {
            throw new MessageSendException(500, "MSG0042", "Insufficient credits to send message");
        }
    }

    @Override
    public void close() {
    }

//    private HttpPost
}

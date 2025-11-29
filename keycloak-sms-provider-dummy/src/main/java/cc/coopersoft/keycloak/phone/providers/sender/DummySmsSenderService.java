package cc.coopersoft.keycloak.phone.providers.sender;

import cc.coopersoft.keycloak.phone.providers.exception.MessageSendException;
import cc.coopersoft.keycloak.phone.providers.spi.messagesender.FullSmsSenderAbstractService;
import org.jboss.logging.Logger;
import org.keycloak.models.KeycloakSession;

import java.util.Random;

public class DummySmsSenderService extends FullSmsSenderAbstractService {

    private static final Logger logger = Logger.getLogger(DummySmsSenderService.class);

    public DummySmsSenderService(KeycloakSession session) {
        super(session);
    }

    @Override
    public void sendMessage(String phoneNumber) throws MessageSendException {

        // here you call the method for sending messages
        logger.info(String.format("Sending sms to: %s", phoneNumber));

        // simulate a failure
        if (new Random().nextInt(10) % 5 == 0) {
            throw new MessageSendException(500, "MSG0042", "Insufficient credits to send message");
        }
    }

    @Override
    public void close() {
    }
}

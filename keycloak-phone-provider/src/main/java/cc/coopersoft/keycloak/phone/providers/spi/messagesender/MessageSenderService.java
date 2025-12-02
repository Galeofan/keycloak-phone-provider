package cc.coopersoft.keycloak.phone.providers.spi.messagesender;

import cc.coopersoft.keycloak.phone.providers.exception.MessageSendException;
import org.keycloak.provider.Provider;


/**
 * SMS, Voice, APP
 */
public interface MessageSenderService extends Provider {

    void sendSmsMessage(String phoneNumber) throws MessageSendException;
}

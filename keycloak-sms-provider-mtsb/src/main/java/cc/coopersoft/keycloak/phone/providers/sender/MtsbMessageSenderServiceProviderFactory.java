package cc.coopersoft.keycloak.phone.providers.sender;

import cc.coopersoft.keycloak.phone.providers.spi.messagesender.MessageSenderService;
import cc.coopersoft.keycloak.phone.providers.spi.messagesender.MessageSenderServiceProviderFactory;
import org.jboss.logging.Logger;
import org.keycloak.Config;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;

public class MtsbMessageSenderServiceProviderFactory implements MessageSenderServiceProviderFactory {

    private static final Logger logger = Logger.getLogger(MtsbMessageSenderServiceProviderFactory.class);

    @Override
    public MessageSenderService create(KeycloakSession keycloakSession) {
        return new MtsbSmsSenderService(keycloakSession);
    }

    @Override
    public void init(Config.Scope config) {
        //this.baseUrl = config.get("baseUrl");
    }

    @Override
    public void postInit(KeycloakSessionFactory keycloakSessionFactory) {
    }

    @Override
    public void close() {
    }

    @Override
    public String getId() {
        return "mtsb";
    }

}

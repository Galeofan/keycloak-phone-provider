package cc.coopersoft.keycloak.phone.providers.sender;

import cc.coopersoft.keycloak.phone.providers.spi.messagesender.MessageSenderService;
import cc.coopersoft.keycloak.phone.providers.spi.messagesender.MessageSenderServiceProviderFactory;
import org.keycloak.Config;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;

public class MrcMessageSenderServiceProviderFactory implements MessageSenderServiceProviderFactory {

    private Config.Scope config;

    @Override
    public MessageSenderService create(KeycloakSession keycloakSession) {
        return new MrcSmsSenderService(keycloakSession, config);
    }

    @Override
    public void init(Config.Scope config) {
        this.config = config;
    }

    @Override
    public void postInit(KeycloakSessionFactory keycloakSessionFactory) {
    }

    @Override
    public void close() {
    }

    @Override
    public String getId() {
        return "mrc";
    }

}

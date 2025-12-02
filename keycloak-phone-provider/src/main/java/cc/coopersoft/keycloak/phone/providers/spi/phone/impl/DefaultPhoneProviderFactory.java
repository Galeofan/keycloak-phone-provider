package cc.coopersoft.keycloak.phone.providers.spi.phone.impl;

import cc.coopersoft.keycloak.phone.providers.spi.phone.PhoneProvider;
import cc.coopersoft.keycloak.phone.providers.spi.phone.PhoneProviderFactory;
import org.keycloak.Config.Scope;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;

public class DefaultPhoneProviderFactory implements PhoneProviderFactory {

    private Scope config;

    @Override
    public PhoneProvider create(KeycloakSession session) {
        return new DefaultPhoneProvider(session, config);
    }

    @Override
    public void init(Scope config) {
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
        return "default";
    }
}

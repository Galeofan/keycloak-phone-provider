package cc.coopersoft.keycloak.phone.providers.spi.phoneverify.impl;

import cc.coopersoft.keycloak.phone.providers.spi.phoneverify.PhoneVerificationCodeProvider;
import cc.coopersoft.keycloak.phone.providers.spi.phoneverify.PhoneVerificationCodeProviderFactory;
import org.keycloak.Config;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;

public class DefaultPhoneVerificationCodeProviderFactory implements PhoneVerificationCodeProviderFactory {

    @Override
    public PhoneVerificationCodeProvider create(KeycloakSession session) {
        return new DefaultPhoneVerificationCodeProvider(session);
    }

    @Override
    public void init(Config.Scope scope) {
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

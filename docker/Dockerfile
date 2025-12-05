FROM quay.io/keycloak/keycloak:26.2.3

# main provider
COPY providers/keycloak-phone-provider.jar /opt/keycloak/providers/

#theme
COPY providers/keycloak-phone-provider-theme.jar /opt/keycloak/providers/

#dummy provider
#COPY providers/keycloak-sms-provider-dummy.jar /opt/keycloak/providers/

#mtsb provider
COPY providers/keycloak-sms-provider-mtsb.jar /opt/keycloak/providers/

RUN mkdir /opt/keycloak/certs

RUN /opt/keycloak/bin/kc.sh build

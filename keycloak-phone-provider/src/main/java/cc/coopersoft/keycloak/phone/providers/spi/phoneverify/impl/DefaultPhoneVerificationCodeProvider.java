package cc.coopersoft.keycloak.phone.providers.spi.phoneverify.impl;

import cc.coopersoft.keycloak.phone.Utils;
import cc.coopersoft.keycloak.phone.authentication.requiredactions.ConfigSmsOtpRequiredAction;
import cc.coopersoft.keycloak.phone.authentication.requiredactions.UpdatePhoneNumberRequiredAction;
import cc.coopersoft.keycloak.phone.credential.PhoneOtpCredentialModel;
import cc.coopersoft.keycloak.phone.credential.PhoneOtpCredentialProvider;
import cc.coopersoft.keycloak.phone.credential.PhoneOtpCredentialProviderFactory;
import cc.coopersoft.keycloak.phone.providers.constants.TokenCodeType;
import cc.coopersoft.keycloak.phone.providers.exception.PhoneNumberInvalidException;
import cc.coopersoft.keycloak.phone.providers.jpa.TokenCode;
import cc.coopersoft.keycloak.phone.providers.representations.TokenCodeRepresentation;
import cc.coopersoft.keycloak.phone.providers.spi.phoneverify.PhoneVerificationCodeProvider;
import cc.coopersoft.keycloak.phone.providers.spi.phoneverify.impl.dto.OtpRequestDto;
import cc.coopersoft.keycloak.phone.providers.spi.phoneverify.impl.dto.OtpResponseDto;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;
import org.jboss.logging.Logger;
import org.jetbrains.annotations.NotNull;
import org.keycloak.connections.httpclient.HttpClientProvider;
import org.keycloak.connections.jpa.JpaConnectionProvider;
import org.keycloak.credential.CredentialModel;
import org.keycloak.credential.CredentialProvider;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;
import org.keycloak.services.validation.Validation;
import org.keycloak.util.JsonSerialization;

import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.TemporalType;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.ForbiddenException;
import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class DefaultPhoneVerificationCodeProvider implements PhoneVerificationCodeProvider {

    private static final Logger logger = Logger.getLogger(DefaultPhoneVerificationCodeProvider.class);
    private final KeycloakSession session;
    private final CloseableHttpClient httpClient;

    DefaultPhoneVerificationCodeProvider(KeycloakSession session) {
        this.session = session;
        if (getRealm() == null) {
            throw new IllegalStateException("The service cannot accept a session without a realm in its context.");
        }
        this.httpClient = session.getProvider(HttpClientProvider.class)
                .getHttpClient();
    }

    private EntityManager getEntityManager() {
        return session.getProvider(JpaConnectionProvider.class).getEntityManager();
    }

    private RealmModel getRealm() {
        return session.getContext().getRealm();
    }

    @Override
    public TokenCodeRepresentation ongoingProcess(String phoneNumber, TokenCodeType tokenCodeType) {

        try {
            String resultPhoneNumber = Utils.canonicalizePhoneNumber(session, phoneNumber);
            TokenCode entity = getEntityManager()
                    .createNamedQuery("ongoingProcess", TokenCode.class)
                    .setParameter("realmId", getRealm().getId())
                    .setParameter("phoneNumber", resultPhoneNumber)
                    .setParameter("now", new Date(), TemporalType.TIMESTAMP)
                    .setParameter("type", tokenCodeType.name())
                    .getSingleResult();

            TokenCodeRepresentation tokenCodeRepresentation = new TokenCodeRepresentation();

            tokenCodeRepresentation.setId(entity.getId());
            tokenCodeRepresentation.setPhoneNumber(entity.getPhoneNumber());
            tokenCodeRepresentation.setRequestId(entity.getCode());
            tokenCodeRepresentation.setType(entity.getType());
            tokenCodeRepresentation.setCreatedAt(entity.getCreatedAt());
            tokenCodeRepresentation.setExpiresAt(entity.getExpiresAt());
            tokenCodeRepresentation.setConfirmed(entity.getConfirmed());

            return tokenCodeRepresentation;
        } catch (NoResultException e) {
            return null;
        } catch (PhoneNumberInvalidException e) {
            logger.warn("Invalid number: " + phoneNumber);
            throw new BadRequestException("Phone number is invalid");
        }
    }

    @Override
    public boolean isAbusing(String phoneNumber, TokenCodeType tokenCodeType,
            String sourceAddr, int sourceHourMaximum, int targetHourMaximum) {

        Date oneHourAgo = new Date(System.currentTimeMillis() - TimeUnit.HOURS.toMillis(1));

        if (targetHourMaximum > 0) {
            long targetCount = (getEntityManager()
                    .createNamedQuery("processesSinceTarget", Long.class)
                    .setParameter("realmId", getRealm().getId())
                    .setParameter("phoneNumber", phoneNumber)
                    .setParameter("date", oneHourAgo, TemporalType.TIMESTAMP)
                    .setParameter("type", tokenCodeType.name())
                    .getSingleResult());
            if (targetCount > targetHourMaximum)
                return true;
        }

        if (sourceHourMaximum > 0) {
            long sourceCount = (getEntityManager()
                    .createNamedQuery("processesSinceSource", Long.class)
                    .setParameter("realmId", getRealm().getId())
                    .setParameter("addr", sourceAddr)
                    .setParameter("date", oneHourAgo, TemporalType.TIMESTAMP)
                    .setParameter("type", tokenCodeType.name())
                    .getSingleResult());
            if (sourceCount > sourceHourMaximum)
                return true;
        }

        return false;
    }

    @Override
    public void persistCode(TokenCodeRepresentation tokenCode, TokenCodeType tokenCodeType, int tokenExpiresIn) {

        TokenCode entity = new TokenCode();
        Instant now = Instant.now();

        entity.setId(tokenCode.getId());
        entity.setRealmId(getRealm().getId());
        entity.setPhoneNumber(tokenCode.getPhoneNumber());
        entity.setCode(tokenCode.getRequestId());
        entity.setType(tokenCodeType.name());
        entity.setCreatedAt(Date.from(now));
        entity.setExpiresAt(Date.from(now.plusSeconds(tokenExpiresIn)));
        entity.setConfirmed(tokenCode.getConfirmed());
        if (session.getContext().getConnection() != null) {
            entity.setIp(session.getContext().getConnection().getRemoteAddr());
            entity.setPort(session.getContext().getConnection().getRemotePort());
            entity.setHost(session.getContext().getConnection().getRemoteHost());
        }

        getEntityManager().persist(entity);
    }

    @Override
    public void validateCode(UserModel user, String phoneNumber, String code) {
        validateCode(user, phoneNumber, code, TokenCodeType.VERIFY);
    }

    @Override
    public void validateCode(UserModel user, String phoneNumber, String code, TokenCodeType tokenCodeType) {

        logger.info(String.format("Validating %s type [phone: %s, code: %s]", tokenCodeType, phoneNumber, code));

        TokenCodeRepresentation tokenCode = ongoingProcess(phoneNumber, tokenCodeType);
        if (tokenCode == null)
            throw new BadRequestException(String.format("There is no valid ongoing %s process", tokenCodeType.label));

        if (!validateOtpExternal(tokenCode.getRequestId(), code)) {
            throw new ForbiddenException("Code does not match with expected value");
        }

        if (user != null) {
            logger.infof("User %s correctly answered %s code",
                    user.getId(), tokenCodeType);
        } else {
            logger.infof("Anonymous user correctly answered %s code (registration flow)",
                    tokenCodeType);
        }

        tokenValidated(user, phoneNumber, tokenCode.getId(), TokenCodeType.OTP.equals(tokenCodeType));

        if (TokenCodeType.OTP.equals(tokenCodeType) && user != null) {
            updateUserOTPCredential(user, phoneNumber, tokenCode.getRequestId());
        }
    }

    public boolean validateOtpExternal(String requestId, String code) {
        logger.info("Sending OTP request");

        OtpRequestDto request = OtpRequestDto.builder()
                .code(code)
                .session(requestId)
                .build();

        HttpPost post = createPostRequest(request);

        try {
            HttpResponse httpResponse = httpClient.execute(post);

            int status = httpResponse.getStatusLine().getStatusCode();
            String body = EntityUtils.toString(httpResponse.getEntity(), StandardCharsets.UTF_8);

            logger.infof("HTTP status: %s", status);
            logger.infof("HTTP body: %s", body);

            OtpResponseDto dto = JsonSerialization.readValue(body, OtpResponseDto.class);

            return dto.getResult().equalsIgnoreCase("success");
        } catch (Exception e) {
            logger.error("Error on executing or parsing response", e);
            throw new RuntimeException(e);
        }
    }

    @NotNull
    private static HttpPost createPostRequest(OtpRequestDto request) {
        try {
            final URI uri = new URIBuilder("https://host.docker.internal:8983/otp")
                    .setCharset(StandardCharsets.UTF_8)
                    .build();
            HttpPost post = new HttpPost(uri);
            final String requestData = JsonSerialization.writeValueAsString(request);
            post.setEntity(new StringEntity(requestData, StandardCharsets.UTF_8));
            final String logMessage = String.format("Url post запроса: %s,\nHeaders запроса: %s\nBody запроса: %s",
                    uri.toString(),
                    null,
                    requestData);
            logger.info(logMessage);
            return post;
        } catch (Exception e) {
            logger.error("Error on creating POST request", e);
            throw new RuntimeException(e);
        }
    }

    @Override
    public void tokenValidated(UserModel user, String phoneNumber, String tokenCodeId, boolean isOTP) {

        if (user == null) {
            validateProcess(tokenCodeId, null);
            return;
        }

        boolean updateUserPhoneNumber = !isOTP;
        if (isOTP) {
            updateUserPhoneNumber = PhoneOtpCredentialModel.getSmsOtpCredentialData(user)
                    .map(PhoneOtpCredentialModel.SmsOtpCredentialData::getPhoneNumber)
                    .map(pn -> pn.equals(phoneNumber))
                    .orElse(false);

        }

        if (updateUserPhoneNumber) {
            if (!Utils.isDuplicatePhoneAllowed(session)) {
                session.users()
                        .searchForUserByUserAttributeStream(session.getContext().getRealm(), "phoneNumber", phoneNumber)
                        .filter(u -> !u.getId().equals(user.getId()))
                        .forEach(u -> {
                            logger.info(String.format("User %s also has phone number %s. Un-verifying.", u.getId(),
                                    phoneNumber));
                            u.setSingleAttribute("phoneNumberVerified", "false");

                            u.addRequiredAction(UpdatePhoneNumberRequiredAction.PROVIDER_ID);

                            // remove otp Credentials
                            u.credentialManager()
                                    .getStoredCredentialsByTypeStream(PhoneOtpCredentialModel.TYPE)
                                    .filter(c -> {
                                        try {
                                            PhoneOtpCredentialModel.SmsOtpCredentialData credentialData = JsonSerialization
                                                    .readValue(c.getCredentialData(),
                                                            PhoneOtpCredentialModel.SmsOtpCredentialData.class);
                                            if (Validation.isBlank(credentialData.getPhoneNumber())) {
                                                return true;
                                            }
                                            return credentialData.getPhoneNumber()
                                                    .equals(user.getFirstAttribute("phoneNumber"));
                                        } catch (IOException e) {
                                            logger.warn("Unknown format Otp Credential", e);
                                            return true;
                                        }
                                    })
                                    .map(CredentialModel::getId)
                                    .collect(Collectors.toList())
                                    .forEach(id -> u.credentialManager().removeStoredCredentialById(id));
                        });
            }
            user.setSingleAttribute("phoneNumberVerified", "true");
            user.setSingleAttribute("phoneNumber", phoneNumber);

            user.removeRequiredAction(UpdatePhoneNumberRequiredAction.PROVIDER_ID);
        }

        validateProcess(tokenCodeId, user);

    }

    @Override
    public void validateProcess(String tokenCodeId, UserModel user) {
        TokenCode entity = getEntityManager().find(TokenCode.class, tokenCodeId);
        entity.setConfirmed(true);
        entity.setByWhom(user != null ? user.getId() : null);
        getEntityManager().persist(entity);
    }

    private void updateUserOTPCredential(UserModel user, String phoneNumber, String code) {
        user.removeRequiredAction(ConfigSmsOtpRequiredAction.PROVIDER_ID);
        PhoneOtpCredentialProvider ocp = (PhoneOtpCredentialProvider) session.getProvider(CredentialProvider.class,
                PhoneOtpCredentialProviderFactory.PROVIDER_ID);
        if (ocp.isConfiguredFor(getRealm(), user, PhoneOtpCredentialModel.TYPE)) {
            var credentialData = new PhoneOtpCredentialModel.SmsOtpCredentialData(phoneNumber,
                    Utils.getOtpExpires(session));
            PhoneOtpCredentialModel.updateOtpCredential(user, credentialData, code);
        }
    }

    @Override
    public void close() {
    }
}

package cc.coopersoft.keycloak.phone.providers.config;

import org.jetbrains.annotations.NotNull;
import org.keycloak.Config;
import org.keycloak.models.KeycloakSession;

public final class GraviteeIntegrationConfig {
    private final String smsSendUrl;
    private final String otpVerifyUrl;
    private final String apiKey;

    private GraviteeIntegrationConfig(String smsSendUrl, String otpVerifyUrl, String apiKey) {
        this.smsSendUrl = smsSendUrl;
        this.otpVerifyUrl = otpVerifyUrl;
        this.apiKey = apiKey;
    }

    public String smsSendUrl() {
        return smsSendUrl;
    }

    public String otpVerifyUrl() {
        return otpVerifyUrl;
    }

    public String apiKey() {
        return apiKey;
    }

    public static SmsConfig smsFrom(Config.Scope config, KeycloakSession session) {
        String realm = session.getContext().getRealm().getName();
        String smsUrl = required(config, realm, "sms-send-url");
        String apiKey = requiredEnv();
        return new SmsConfig(smsUrl, apiKey);
    }

    public record SmsConfig(String smsSendUrl, String apiKey) {}

    public static OtpConfig otpFrom(Config.Scope config, KeycloakSession session) {
        String realm = session.getContext().getRealm().getName();
        String otpUrl = required(config, realm, "otp-verify-url");
        String apiKey = requiredEnv();
        return new OtpConfig(otpUrl, apiKey);
    }

    public record OtpConfig(String otpVerifyUrl, String apiKey) {}

    @NotNull
    private static String requiredEnv() {
        String apiKey = System.getenv("GRAVITEE_API_KEY");
        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalStateException("Missing env GRAVITEE_API_KEY");
        }
        return apiKey;
    }

    private static String required(org.keycloak.Config.Scope config, String realm, String key) {
        String v = config.get(realm + "-" + key);
        if (v == null || v.isBlank()) {
            v = config.get(key);
        }
        if (v == null || v.isBlank()) {
            throw new IllegalStateException("Missing config: " + key + " (realm=" + realm + ")");
        }
        return v.trim();
    }
}

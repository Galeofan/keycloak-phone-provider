package cc.coopersoft.keycloak.phone.providers.exception;

import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class ExternalOtpValidationException extends RuntimeException {

    private final Integer statusCode = -1;
    private String errorCode = "";
    private final String errorMessage;

    public ExternalOtpValidationException(String message) {
        super(message);
        this.errorMessage = message;
    }

    public ExternalOtpValidationException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
        this.errorMessage = message;
    }

    public ExternalOtpValidationException(String message, Throwable cause) {
        super(message, cause);
        this.errorMessage = message;
    }

}

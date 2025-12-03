package cc.coopersoft.keycloak.phone.providers.exception;

import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class BuildRequestException extends RuntimeException {

    private final Integer statusCode = -1;
    private String errorCode = "";
    private final String errorMessage;

    public BuildRequestException(String errorMessage) {
        super(errorMessage);
        this.errorMessage = errorMessage;
    }

    public BuildRequestException(String errorCode, String errorMessage) {
        super(errorMessage);
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
    }

    public BuildRequestException(String errorMessage, Throwable cause) {
        super(errorMessage, cause);
        this.errorMessage = errorMessage;
    }
}

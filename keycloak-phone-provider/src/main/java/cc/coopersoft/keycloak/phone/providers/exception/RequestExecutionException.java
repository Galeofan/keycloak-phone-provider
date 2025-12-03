package cc.coopersoft.keycloak.phone.providers.exception;

import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class RequestExecutionException extends RuntimeException {

    private final Integer statusCode = -1;
    private String errorCode = "";
    private final String errorMessage;

    public RequestExecutionException(String errorMessage) {
        super(errorMessage);
        this.errorMessage = errorMessage;
    }

    public RequestExecutionException(String errorCode, String errorMessage) {
        super(errorMessage);
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
    }

    public RequestExecutionException(String errorMessage, Throwable cause) {
        super(errorMessage, cause);
        this.errorMessage = errorMessage;
    }
}

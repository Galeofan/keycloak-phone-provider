//package cc.coopersoft.keycloak.phone.providers.sender.exception;
//
//import lombok.Data;
//import lombok.EqualsAndHashCode;
//
//@EqualsAndHashCode(callSuper = true)
//@Data
//public class SmsSendingException extends RuntimeException {
//
//    private final Integer statusCode = -1;
//    private String errorCode = "";
//    private final String errorMessage;
//
//    public SmsSendingException(String message) {
//        super(message);
//        this.errorMessage = message;
//    }
//
//    public SmsSendingException(String errorCode, String message) {
//        super(message);
//        this.errorCode = errorCode;
//        this.errorMessage = message;
//    }
//
//    public SmsSendingException(String message, Throwable cause) {
//        super(message, cause);
//        this.errorMessage = message;
//    }
//
//}

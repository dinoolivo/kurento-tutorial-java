package org.kurento.tutorial.one2onecall.exception;


public class AppException extends RuntimeException{
    
    private InternalErrorCodes errorCode;

    public AppException(InternalErrorCodes errorCode,String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public AppException(InternalErrorCodes errorCode,String message,Throwable t) {
        super(message,t);
        this.errorCode = errorCode;
    }
    
    public AppException(InternalErrorCodes errorCode,Throwable t) {
        super(t);
        this.errorCode = errorCode;
    }
    
    
    public InternalErrorCodes getErrorCode() {
        return errorCode;
    }
    
    
}

package org.kurento.tutorial.one2onecall.exception;

import org.kurento.tutorial.one2onecall.codes.StatusCode;

public enum InternalErrorCodes {
    
    EMPTY_USERNAME(4001,"empty username",StatusCode.BAD_REQUEST),
    USER_ALREADY_REGISTERED(4002,"user already registered",StatusCode.BAD_REQUEST),
    CALLEE_BUSY(4003,"cannot call a busy user",StatusCode.BAD_REQUEST),
    USER_DOES_NOT_EXIST(4004,"user does not exists",StatusCode.BAD_REQUEST),
    USER_STATUS_NOT_SUPPORTED(4005,"user status not supported",StatusCode.BAD_REQUEST),
    CALL_REFUSED(4006,"call refused",StatusCode.BAD_REQUEST),
    UNHANDLED_ERROR(5001,"unexpected server error",StatusCode.INTERNAL_SERVER_ERROR);
    
    private final int code;
    private final StatusCode status;
    private final String message;
    
    private InternalErrorCodes(int code,String message,StatusCode status){
        this.code = code;
        this.message = message;
        this.status = status;
    }

    public int getCode() {
        return code;
    }

    public StatusCode getStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }

    
    
    @Override
    public String toString(){
        return String.valueOf(code);
    }
    
}

package org.kurento.tutorial.one2onecall;

import org.kurento.tutorial.one2onecall.exception.AppException;
import org.kurento.tutorial.one2onecall.exception.InternalErrorCodes;


public enum ResponseCallStatus {
    ACCEPTED,
    REFUSED,
    NOT_ANSWERED;
    
    public static ResponseCallStatus fromString(String responseCallStatusStr){
        switch(responseCallStatusStr){
            case "ACCEPTED": return ACCEPTED;
            case "REFUSED": return REFUSED;
            case "NOT_ANSWERED": return NOT_ANSWERED;
            default: throw new AppException(InternalErrorCodes.RESPONSE_CALL_STATUS_NOT_SUPPORTED, "not supported response call status "+responseCallStatusStr);              
        }
    }
}

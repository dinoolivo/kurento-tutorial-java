package org.kurento.tutorial.one2onecall;

import org.kurento.tutorial.one2onecall.exception.AppException;
import org.kurento.tutorial.one2onecall.exception.InternalErrorCodes;


public enum UserCallStatus {
    AVAILABLE,
    BUSY,
    DISCONNECTED;
    

    public static UserCallStatus fromStr(String userCallStatusStr){
        switch(userCallStatusStr){
            case "AVAILABLE": return AVAILABLE;
            case "BUSY": return BUSY;
            default: throw new AppException(InternalErrorCodes.USER_STATUS_NOT_SUPPORTED, "not supported user status "+userCallStatusStr);
        }
    }
}

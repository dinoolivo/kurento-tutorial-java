package org.kurento.tutorial.one2onecall;

import org.kurento.tutorial.one2onecall.exception.AppException;
import org.kurento.tutorial.one2onecall.exception.InternalErrorCodes;


public enum UserCallStatus {
    AVAILABLE("Available"),
    BUSY("Busy");
    
    private final String strStatus;
    
    private UserCallStatus(String strStatus){
        this.strStatus = strStatus;
    }
    
    @Override
    public String toString(){
        return strStatus;
    }
    
    public static UserCallStatus fromStr(String userCallStatusStr){
        switch(userCallStatusStr){
            case "Available": return AVAILABLE;
            case "Busy": return BUSY;
            default: throw new AppException(InternalErrorCodes.USER_STATUS_NOT_SUPPORTED, "not supported user status "+userCallStatusStr);
        }
    }
}

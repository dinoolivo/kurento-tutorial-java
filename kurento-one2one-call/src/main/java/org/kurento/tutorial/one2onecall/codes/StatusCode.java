
package org.kurento.tutorial.one2onecall.codes;


public enum StatusCode {
    
    OK(200),
    BAD_REQUEST(400),
    INTERNAL_SERVER_ERROR(500);
    
    
    private final int statusCode;
    
    private StatusCode(int statusCode){
        this.statusCode = statusCode;
    }
    
    public int intValue(){
        return statusCode;
    }
    
    @Override
    public String toString(){
        return String.valueOf(statusCode);
    }
}

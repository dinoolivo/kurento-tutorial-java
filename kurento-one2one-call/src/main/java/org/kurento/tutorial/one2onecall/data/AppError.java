package org.kurento.tutorial.one2onecall.data;


public class AppError {
    
    private String message;
    private String description;
    private int code;

    public AppError(String message, int code) {
        this.message = message;
        this.code = code;
    }
    
    public AppError(String message,String description, int code) {
        this.message = message;
        this.code = code;
        this.description = description;
    }
   
    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

}

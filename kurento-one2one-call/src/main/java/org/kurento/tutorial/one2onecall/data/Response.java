package org.kurento.tutorial.one2onecall.data;

import org.kurento.tutorial.one2onecall.codes.StatusCode;
import org.kurento.tutorial.one2onecall.utils.SerializationUtils;

public class Response<T> {

    private String method;
    private int status;
    private T response;
    private AppError error;
    
    
    private Response(ResponseBuilder<T> responseBuilder){
        
        if(responseBuilder.error == null && responseBuilder.response == null){
            throw new IllegalStateException("Response response and error fields are both not initialized.");
        }
        
        this.method = responseBuilder.method;
        this.status = responseBuilder.status;
        this.response = responseBuilder.response;
        this.error = responseBuilder.error;
    }
    
    public Response(){
    
    }

    public String getMethod() {
        return method;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public T getResponse() {
        return response;
    }

    public AppError getError() {
        return error;
    }

    public void setError(AppError error) {
        this.error = error;
    }
    
    public String toJsonStr(){
        return SerializationUtils.toJson(this);
    }


    public static class ResponseBuilder<T> {

        private String method;
        private int status;
        private T response;
        private AppError error;
        
        
        public ResponseBuilder(String method){
            this.method = method;
        }
        
        public ResponseBuilder status(int status){
            this.status = status;
            return this;
        }
        
        public ResponseBuilder response(T response){
            this.response = response;
            return this;
        }
        
        public ResponseBuilder error(AppError error){
            this.error = error;
            return this;
        }
        
        public Response build(){
            return new Response<>(this);
        }
        
     
    }

}

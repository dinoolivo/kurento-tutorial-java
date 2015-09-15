
package org.kurento.tutorial.one2onecall.data;

import org.kurento.tutorial.one2onecall.utils.SerializationUtils;


public class Request<T> {
    
    private String method;
    private T arguments;

    public Request(String method, T arguments) {
        this.method = method;
        this.arguments = arguments;
    }

    public Request() {
    }
    
    

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public T getArguments() {
        return arguments;
    }

    public void setArguments(T arguments) {
        this.arguments = arguments;
    }
    
    public String toJsonStr(){
        return SerializationUtils.toJson(this);
    }
    
}

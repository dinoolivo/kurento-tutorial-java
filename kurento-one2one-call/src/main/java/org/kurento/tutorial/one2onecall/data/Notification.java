package org.kurento.tutorial.one2onecall.data;

import com.google.gson.Gson;


public class Notification<C> {
    
    private transient static final Gson gson = new Gson(); 
    
    private String target;
    private C content;

    public Notification(String target, C content) {
        this.target = target;
        this.content = content;
    }

    public String getTarget() {
        return target;
    }

    public void setTarget(String target) {
        this.target = target;
    }

    public C getContent() {
        return content;
    }

    public void setContent(C content) {
        this.content = content;
    }
    
    public String toJsonStr(){
        return gson.toJson(this);
    }
    
}

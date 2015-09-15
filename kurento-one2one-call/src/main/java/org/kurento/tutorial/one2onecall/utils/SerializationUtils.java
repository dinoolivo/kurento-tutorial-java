package org.kurento.tutorial.one2onecall.utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.kurento.tutorial.one2onecall.UserCallStatus;


public class SerializationUtils {
    
    private static final Gson gson = new GsonBuilder().registerTypeAdapter(UserCallStatus.class, new UserStatusEnumAdapter()).create();
    
    
    public static final String toJson(Object obj){
        return gson.toJson(obj);
    }
}

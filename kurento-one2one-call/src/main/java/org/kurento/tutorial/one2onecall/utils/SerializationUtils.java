package org.kurento.tutorial.one2onecall.utils;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.lang.reflect.Type;
import org.kurento.tutorial.one2onecall.UserCallStatus;
import org.kurento.tutorial.one2onecall.data.Request;
import org.kurento.tutorial.one2onecall.data.Response;


public class SerializationUtils {
    
    private static final Gson gson = new GsonBuilder().registerTypeAdapter(UserCallStatus.class, new UserStatusEnumAdapter()).create();
    
    
    public static final String toJson(Object obj){
        return gson.toJson(obj);
    }
    
    public static final <T> Request<T> decodeRequest(String jsonMessageStr,Type reqType){
        return gson.fromJson(jsonMessageStr, reqType);
    }
    
    public static final <T> Response<T> decodeResponse(String jsonMessageStr,Type reqType){
        return gson.fromJson(jsonMessageStr, reqType);
    }
}

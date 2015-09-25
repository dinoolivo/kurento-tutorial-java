package org.kurento.tutorial.one2onecall;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import org.kurento.tutorial.one2onecall.data.Notification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;


@Service
public class NotificationService {
    
    private static final String NOTIFICATION_TAG = "notification";
    private static final Logger log = LoggerFactory.getLogger(NotificationService.class);
    private final ConcurrentHashMap<String, UserSession> usersByName = new ConcurrentHashMap<>();
    
    
    public void register(UserSession userSession){
        usersByName.put(userSession.getUser().getUsername(), userSession);
    }
    
    public void delete(UserSession userSession){
        usersByName.remove(userSession.getUser().getUsername(), userSession);
    }
    
    public <C> void notifyAll(String target,C content){
        Notification<C> notification = new Notification<>(target, content);
        usersByName.forEach((username,userSession) -> notify(username,notification));
    }
    
    public <C> void notify(String username,String target,C content){
        notify(username, new Notification<>(target, content));
    }
    
    public <C> void notify(String username,Notification<C> notification){
        try {
            usersByName.get(username).sendMessage(NOTIFICATION_TAG,notification);
        } catch (IOException ex) {
            log.error("failed to notify user "+username, ex);
        }
    }
    
}

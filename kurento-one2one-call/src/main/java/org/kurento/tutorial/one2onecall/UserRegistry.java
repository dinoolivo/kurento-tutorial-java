/*
 * (C) Copyright 2014 Kurento (http://kurento.org/)
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser General Public License
 * (LGPL) version 2.1 which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl-2.1.html
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 */
package org.kurento.tutorial.one2onecall;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import org.kurento.jsonrpc.Session;
import org.kurento.tutorial.one2onecall.data.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;


/**
 * Map of users registered in the system. This class has a concurrent hash map
 * to store users, using its name as key in the map.
 *
 * @author Boni Garcia (bgarcia@gsyc.es)
 * @author Micael Gallego (micael.gallego@gmail.com)
 * @since 4.3.1
 */
public class UserRegistry {
    
    @Autowired
    private NotificationService notificationService;

    private static final Logger log = LoggerFactory.getLogger(UserRegistry.class);
    private final ConcurrentHashMap<String, UserSession> usersByName = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, UserSession> usersBySessionId = new ConcurrentHashMap<>();

    public void register(UserSession userSession) {
        usersByName.put(userSession.getUser().getUsername(), userSession);
        usersBySessionId.put(userSession.getSession().getSessionId(), userSession);
        
        notificationService.register(userSession);
        
        log.debug(String.format("user %s registered. Sending notification to all users..",userSession.getUser().getUsername()));
        notificationService.notifyAll(NotificationKeywords.USER_LIST, userSession.getUser());
    }
    
    public UserSession getBySession(Session session) {
        return usersBySessionId.get(session.getSessionId());
    }
    
    public UserSession removeBySession(Session session) {
        final UserSession userSession = getBySession(session);
        if (userSession != null) {
            usersByName.remove(userSession.getUser().getUsername());
            usersBySessionId.remove(session.getSessionId());
            
            notificationService.delete(userSession);
            changeUserStatus(userSession, UserCallStatus.DISCONNECTED);
        }
        return userSession;
    }
    
    public void changeUserStatus(Session session,UserCallStatus userCallStatus){
        UserSession userSession = this.usersBySessionId.get(session.getSessionId());
        changeUserStatus(userSession, userCallStatus);
    }
    
    public void changeUserStatus(UserSession userSession,UserCallStatus userCallStatus){
        userSession.getUser().setStatus(userCallStatus);
        notificationService.notifyAll(NotificationKeywords.USER_LIST, userSession.getUser());
    }
    

    public UserSession getByName(String name) {
        return usersByName.get(name);
    }

  
    public boolean exists(String name) {
        return usersByName.containsKey(name);
    }
    
    public void changeUserStatus(String username,UserCallStatus userCallStatus){
        this.usersByName.get(username).getUser().setStatus(userCallStatus);
    }
    
   
    public List<User> getUsersList() {
        return usersByName.values().stream().map(userSession -> userSession.getUser())
                .collect(Collectors.toList());
    }

}

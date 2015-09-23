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

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import org.kurento.tutorial.one2onecall.codes.StatusCode;
import org.kurento.tutorial.one2onecall.data.Request;
import org.kurento.tutorial.one2onecall.data.Response;
import org.kurento.tutorial.one2onecall.data.User;
import org.kurento.tutorial.one2onecall.data.UserList;
import org.slf4j.LoggerFactory;

import org.springframework.web.socket.WebSocketSession;

/**
 * Map of users registered in the system. This class has a concurrent hash map
 * to store users, using its name as key in the map.
 *
 * @author Boni Garcia (bgarcia@gsyc.es)
 * @author Micael Gallego (micael.gallego@gmail.com)
 * @since 4.3.1
 */
public class UserRegistry {

    private static final org.slf4j.Logger log = LoggerFactory.getLogger(UserRegistry.class);
    private final ConcurrentHashMap<String, UserSession> usersByName = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, UserSession> usersBySessionId = new ConcurrentHashMap<>();

    public void register(UserSession userSession) {
        usersByName.put(userSession.getUser().getUsername(), userSession);
        usersBySessionId.put(userSession.getSession().getId(), userSession);
        
        log.debug("user #{} registered. Sending notification to all users..",userSession.getUser().getUsername());
        //send notification to all users
        notifyUsers(getUsrListUpdate());
    }

    public UserSession getByName(String name) {
        return usersByName.get(name);
    }

    public UserSession getBySession(WebSocketSession session) {
        return usersBySessionId.get(session.getId());
    }

    public boolean exists(String name) {
        return usersByName.containsKey(name);
    }
    
    public void changeUserStatus(String username,UserCallStatus userCallStatus){
        this.usersByName.get(username).getUser().setStatus(userCallStatus);
        
        notifyUsers(getUsrListUpdate());
    }
    
    public void changeUserStatus(WebSocketSession session,UserCallStatus userCallStatus){
        this.usersBySessionId.get(session.getId()).getUser().setStatus(userCallStatus);
        
        notifyUsers(getUsrListUpdate());
    }

    public UserSession removeBySession(WebSocketSession session) {
        final UserSession userSession = getBySession(session);
        if (userSession != null) {
            usersByName.remove(userSession.getUser().getUsername());
            usersBySessionId.remove(session.getId());
            
            //send notification to all users
            notifyUsers(getUsrListUpdate());
        }
        return userSession;
    }

    public List<User> getUsersList() {
        return usersByName.values().stream().map(userSession -> userSession.getUser())
                .collect(Collectors.toList());
    }
    
    
    //temporary 
    private String getUsrListUpdate(){
        return new Response.ResponseBuilder<>(WsEndpoints.USRS_LIST)
                                                    .status(StatusCode.OK.intValue())
                                                    .response(new UserList(getUsersList())).build().toJsonStr();
    }

    public void notifyUsers(String message) {
        usersByName.values().forEach(userStatus -> {
            try {
                userStatus.sendMessage(message);
            } catch (IOException ex) {
                log.error("failed to notify user " + userStatus.getUser().getUsername(), ex);
            }
        });
    }

    public void notifyUser(String username, String message) {
        try {
            usersByName.get(username).sendMessage(message);
        } catch (IOException ex) {
            log.error("failed to notify user " + username, ex);
        }
    }

}

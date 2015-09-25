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

import java.io.IOException;

import org.kurento.jsonrpc.Session;
import org.kurento.tutorial.one2onecall.data.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * User session.
 * 
 * @author Boni Garcia (bgarcia@gsyc.es)
 * @author Micael Gallego (micael.gallego@gmail.com)
 * @since 4.3.1
 */
public class UserSession {

	private static final Logger log = LoggerFactory.getLogger(UserSession.class);

	private final User user;
	private final Session session;
        
        private CurrentCall currentCall;

	

	public UserSession(Session session, User user) {
		this.session = session;
		this.user = user;
	}

	public Session getSession() {
		return session;
	}

	public User getUser() {
		return user;
	}

        public CurrentCall getCurrentCall() {
            return currentCall;
        }
        
        public void createNewCallRequest(String usernameTo,String sdpOffer,NotificationService notificationService) {
            this.currentCall = new CurrentCall(user.getUsername(), usernameTo, sdpOffer,notificationService);
        }

        public void setCurrentCall(CurrentCall currentCall) {
            this.currentCall = currentCall;
        }
        
        
        public void endCall() throws Exception{
            if(currentCall!=null){
                currentCall.clear();
                currentCall.sendStopCommunicationToOtherPeer(user.getUsername());
            }else{
                log.error("No active calls to stop");
            }
        }
        
        public void clearCurrentCall(){
            currentCall.clear();
            currentCall = null;
        }
        
	
	public void sendMessage(String message) throws IOException {
		log.debug(String.format("Sending message to user '%s': %s", user.getUsername(), message));
                synchronized(session){
                    session.sendNotification(message);
                }
	}
        
        public void sendMessage(String message,Object o) throws IOException {
		log.debug(String.format("Sending message to user '%s': %s", user.getUsername(), message));
                synchronized(session){
                    session.sendNotification(message,o);
                }
	}

	public String getSessionId() {
		return session.getSessionId();
	}
        
}

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
package org.kurento.tutorial.one2manycall;

import java.io.IOException;

import org.kurento.client.IceCandidate;
import org.kurento.client.WebRtcEndpoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import com.google.gson.JsonObject;

/**
 * User session.
 * 
 * @author Boni Garcia (bgarcia@gsyc.es)
 * @since 5.0.0
 */
public class UserSession {

	private static final Logger log = LoggerFactory
			.getLogger(UserSession.class);

	private final WebSocketSession session;
	private WebRtcEndpoint webRtcEndpoint;

	public UserSession(WebSocketSession session) {
		this.session = session;
	}

	public WebSocketSession getSession() {
		return session;
	}

	public void sendMessage(JsonObject message) throws IOException {
		log.debug("Sending message from user with session Id '{}': {}",
				session.getId(), message);
		session.sendMessage(new TextMessage(message.toString()));
	}

	public WebRtcEndpoint getWebRtcEndpoint() {
		return webRtcEndpoint;
	}

	public void setWebRtcEndpoint(WebRtcEndpoint webRtcEndpoint) {
		this.webRtcEndpoint = webRtcEndpoint;
	}

	public void addCandidate(IceCandidate i) {
		webRtcEndpoint.addIceCandidate(i);
	}
}

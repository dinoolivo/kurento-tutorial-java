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
import org.kurento.client.IceCandidate;
import org.kurento.client.KurentoClient;
import org.kurento.client.MediaPipeline;
import org.kurento.client.WebRtcEndpoint;

/**
 * Media Pipeline (WebRTC endpoints, i.e. Kurento Media Elements) and
 * connections for the 1 to 1 video communication.
 * 
 * @author Boni Garcia (bgarcia@gsyc.es)
 * @author Micael Gallego (micael.gallego@gmail.com)
 * @since 4.3.1
 */
public class CallMediaPipeline {

	private MediaPipeline pipeline;
	private WebRtcEndpoint callerWebRtcEP;
	private WebRtcEndpoint calleeWebRtcEP;

	public CallMediaPipeline(KurentoClient kurento,List<IceCandidate> iceCandidateFromList,List<IceCandidate> iceCandidateToList) {
		try {
			this.pipeline = kurento.createMediaPipeline();
			this.callerWebRtcEP = new WebRtcEndpoint.Builder(pipeline).build();
			this.calleeWebRtcEP = new WebRtcEndpoint.Builder(pipeline).build();

			this.callerWebRtcEP.connect(this.calleeWebRtcEP);
			this.calleeWebRtcEP.connect(this.callerWebRtcEP);
                        
                        iceCandidateFromList.forEach(iceCandidate -> callerWebRtcEP.addIceCandidate(iceCandidate));
                        iceCandidateToList.forEach(iceCandidate -> calleeWebRtcEP.addIceCandidate(iceCandidate));
                        
		} catch (Throwable t) {
			releaseMediaPipeline();
		}
	}
        
        public void addCallerIceCandidate(IceCandidate e) {
                callerWebRtcEP.addIceCandidate(e);
	}
        
        public void addCalleeIceCandidate(IceCandidate e) {
                calleeWebRtcEP.addIceCandidate(e);
	}

	public String generateSdpAnswerForCaller(String sdpOffer) {
		return callerWebRtcEP.processOffer(sdpOffer);
	}

	public String generateSdpAnswerForCallee(String sdpOffer) {
		return calleeWebRtcEP.processOffer(sdpOffer);
	}

	public void release() {
            releaseMediaPipeline();
	}

	public WebRtcEndpoint getCallerWebRtcEP() {
		return callerWebRtcEP;
	}

	public WebRtcEndpoint getCalleeWebRtcEP() {
		return calleeWebRtcEP;
	}
        
        private void releaseMediaPipeline(){
            if (pipeline != null) {
			pipeline.release();
            }
            pipeline = null;
        }

}

package org.kurento.tutorial.one2onecall;

import java.util.LinkedList;
import java.util.List;
import org.kurento.client.EventListener;
import org.kurento.client.IceCandidate;
import org.kurento.client.MediaElement;
import org.kurento.client.MediaPipeline;
import org.kurento.client.OnIceCandidateEvent;
import org.kurento.client.WebRtcEndpoint;

public class CallUser {

    private WebRtcEndpoint webRtcEP;
    private String sdpOffer;
    
    private final UserSession userSession;
    private final List<MediaElement> pipelineElements;
    private List<IceCandidate> iceCandidateList;

    public CallUser(UserSession userSession) {
        this.userSession = userSession;
        this.pipelineElements = new LinkedList<>();
        this.iceCandidateList = new LinkedList<>();
    }

    public void buildWebRTCEndpoint(MediaPipeline mediaPipeline) {
        this.webRtcEP = new WebRtcEndpoint.Builder(mediaPipeline).build();
        iceCandidateList.forEach(iceCandidate -> webRtcEP.addIceCandidate(iceCandidate));
        iceCandidateList = null;
    }

    public void addIceCandidate(IceCandidate e) {
        if(webRtcEP != null)
            webRtcEP.addIceCandidate(e);
        else
            iceCandidateList.add(e);
    }

    public String generateSdpAnswer() {
        return webRtcEP.processOffer(sdpOffer);
    }

    public String getSdpOffer() {
        return sdpOffer;
    }

    public void setSdpOffer(String sdpOffer) {
        this.sdpOffer = sdpOffer;
    }
    
    public void gatherCandidates(){
        this.webRtcEP.gatherCandidates();
    }
    
    public void connect(CallUser callUser){
        connect(callUser.webRtcEP);
    }
    
    
    public void connect(MediaElement mediaElement){
        pipelineElements.add(0,mediaElement); //head insertion
        
        if(pipelineElements.size() > 1){
          this.webRtcEP.disconnect(pipelineElements.get(1));
          pipelineElements.get(0).connect(pipelineElements.get(1));
        }
        this.webRtcEP.connect(pipelineElements.get(0));
    }
    
    
    
    public void disconnect(MediaElement mediaElement){

        int mediaElemIndex = pipelineElements.indexOf(mediaElement);
        MediaElement previous;
        MediaElement next;
        
        if(mediaElemIndex < 0 )
            throw new IllegalArgumentException("Cannot disconnect media element. Media element not found in user pipeline elements");
        else if(mediaElemIndex == pipelineElements.size() -1)
            throw new IllegalArgumentException("Cannot disconnect last media element.");
        else if(mediaElemIndex == 0){
            previous = this.webRtcEP;
            next = pipelineElements.get(1);
        }else{
            previous = pipelineElements.get(mediaElemIndex-1);
            next = pipelineElements.get(mediaElemIndex+1);
        }
        previous.disconnect(mediaElement);
        mediaElement.disconnect(next);
        previous.connect(next);
        pipelineElements.remove(mediaElemIndex);
    }

    public UserSession getUserSession() {
        return userSession;
    }
    
    public void addOnIceCandidateListener(EventListener<OnIceCandidateEvent> el){
        this.webRtcEP.addOnIceCandidateListener(el);
    }
    
}

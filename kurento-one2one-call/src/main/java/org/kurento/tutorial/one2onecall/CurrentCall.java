package org.kurento.tutorial.one2onecall;

import com.google.gson.JsonObject;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.kurento.client.Filter;
import org.kurento.client.IceCandidate;
import org.kurento.client.KurentoClient;
import org.kurento.client.MediaElement;
import org.kurento.client.MediaPipeline;
import org.kurento.client.OnIceCandidateEvent;
import org.kurento.jsonrpc.JsonUtils;
import org.kurento.tutorial.one2onecall.utils.JsonFields;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CurrentCall {

    private static final Logger log = LoggerFactory.getLogger(CurrentCall.class);

    private static final String INCOMING_CALL = "incomingCall";
    private static final String OVERLAY_PREFIX = "OVERLAY_";
    
    
    private final CallUser callUserFrom;
    private final CallUser callUserTo;
    private final String usernameFrom;
    private final String usernameTo;
    
    
    private MediaPipeline pipeline;
    
    private final NotificationService notificationService;
    private final OverlayManager overlayManager;

    public CurrentCall(UserSession from, UserSession to, String fromSdpOffer,NotificationService notificationService,OverlayManager overlayManager) {
        this.callUserFrom = new CallUser(from);
        this.callUserFrom.setSdpOffer(fromSdpOffer);
        this.callUserTo = new CallUser(to);
        this.notificationService = notificationService;
        this.overlayManager = overlayManager;
        this.usernameFrom = callUserFrom.getUserSession().getUser().getUsername();
        this.usernameTo = callUserTo.getUserSession().getUser().getUsername();
    }

    public void startCall(KurentoClient kurento) throws Exception {       
        log.info(String.format("Called startCall from %s to %s",usernameFrom,usernameTo));
        
        try{
            //media pipeline creation
            this.pipeline = kurento.createMediaPipeline();
            callUserFrom.buildWebRTCEndpoint(pipeline);
            callUserTo.buildWebRTCEndpoint(pipeline);

            callUserFrom.connect(callUserTo);
            callUserTo.connect(callUserFrom);

            callUserTo.addOnIceCandidateListener(event -> onIceCandidate(usernameTo, event));
            String toSdpAnswer = callUserTo.generateSdpAnswer();

            callUserFrom.addOnIceCandidateListener(event -> onIceCandidate(usernameFrom, event));
            String fromSdpAnswer = callUserFrom.generateSdpAnswer();

            JsonObject startCommunication = new JsonObject();
            startCommunication.addProperty(JsonFields.Call.SDP_ANSWER, toSdpAnswer);

            log.debug(String.format("Sending start communication to %s",usernameTo));
            notificationService.notify(usernameTo, JsonFields.Call.START_COMMUNICATION, startCommunication);

            callUserTo.gatherCandidates();

            JsonObject response = new JsonObject();
            response.addProperty(JsonFields.RESPONSE, ResponseCallStatus.ACCEPTED.toString());
            response.addProperty(JsonFields.Call.SDP_ANSWER, fromSdpAnswer);

            log.debug(String.format("Sending call response to %s",usernameFrom));
            notificationService.notify(usernameFrom, JsonFields.Call.CALL_RESPONSE, response);

            callUserFrom.gatherCandidates();
        }catch(Throwable t){
            log.error("Error while starting call!",t);
            clear();
        }
    }
    
    public void setCallUserToSdpOffer(String sdpOffer){
        callUserTo.setSdpOffer(sdpOffer);
    }
    
    
    public void addOverlay2OtherUser(String username,int overlayId){
        if(username.equals(usernameTo))
            addOverlay2User(callUserFrom, overlayId);
        else
            addOverlay2User(callUserTo, overlayId);
    }
    
    public void removeOverlay2OtherUser(String username,int overlayId){
        if(username.equals(usernameTo))
            removeOverlay2User(callUserFrom, overlayId);
        else
            removeOverlay2User(callUserTo, overlayId);
    }
    
    public void addOverlay2User(CallUser userCall,int overlayId){
        Map<String,Object> sessionAttributes = userCall.getUserSession().getSession().getAttributes();
        
        if(sessionAttributes.containsKey(OVERLAY_PREFIX+overlayId)){
            throw new IllegalArgumentException(String.format("user %s has already added overlay with id %d",userCall.getUserSession().getUser().getUsername(),overlayId));
        }
        
        Filter overlay = overlayManager.getFilterFromOverlayId(pipeline, overlayId);
        sessionAttributes.put(OVERLAY_PREFIX+overlayId, overlay);
        userCall.connect(overlay);
    }
    
    public void removeOverlay2User(CallUser userCall,int overlayId){
        Map<String,Object> sessionAttributes = userCall.getUserSession().getSession().getAttributes();
        
        if(!sessionAttributes.containsKey(OVERLAY_PREFIX+overlayId)){
            throw new IllegalArgumentException(String.format("no overlay with id %d found for user %s",overlayId,userCall.getUserSession().getUser().getUsername()));
        }
        userCall.disconnect((MediaElement)sessionAttributes.get(OVERLAY_PREFIX+overlayId));
    }
    
    
    /**
     * Send stop communication to the other user of the call
     * @param username
     * @throws Exception 
     */
    public void sendStopCommunicationToOtherPeer(String username) throws Exception{
        if(username.equals(usernameFrom))
            sendStopCommunication(usernameTo);
        else
            sendStopCommunication(usernameFrom);
    }
   
    
    public void addIceCandidateFromMessage(String username,JsonObject jsonMessage){
        JsonObject candidate = jsonMessage.get(JsonFields.Ice.CANDIDATE).getAsJsonObject();
        IceCandidate iceCandidate = new IceCandidate(candidate.get(JsonFields.Ice.CANDIDATE).getAsString(),
                                             candidate.get(JsonFields.Ice.SDP_MID).getAsString(),
                                             candidate.get(JsonFields.Ice.SDP_MLINE_INDEX).getAsInt());
        if(username.equals(usernameFrom)){
            callUserFrom.addIceCandidate(iceCandidate);
        }else{
            callUserTo.addIceCandidate(iceCandidate);
        }
    }
    
    public void sendCallRejected(ResponseCallStatus responseCallStatus) throws Exception{
            JsonObject response = new JsonObject();
            response.addProperty(JsonFields.RESPONSE, responseCallStatus.toString());
            notificationService.notify(usernameFrom, JsonFields.Call.CALL_RESPONSE,response);
    }
    
    public void sendStopCommunication(String username) throws Exception{
            notificationService.notify(username, JsonFields.Call.STOP_COMMUNICATION, "");
    }
    
    public void clear(){
        if(pipeline != null){
            pipeline.release();
            pipeline = null;
        }   
    }

    private void onIceCandidate(String username, OnIceCandidateEvent event) {
        JsonObject content = new JsonObject();
        content.add(JsonFields.Ice.CANDIDATE, JsonUtils.toJsonObject(event.getCandidate()));
        try {
            notificationService.notify(username, JsonFields.Ice.CANDIDATE_METHOD, content);
        } catch (Exception e) {
            log.debug("Failed to send Ice Candidate to user " + username, e.getMessage());
        }
    }

    void sendCallRequestToCallee() {
        JsonObject jo = new JsonObject();
        jo.addProperty(JsonFields.Call.FROM, usernameFrom);
        notificationService.notify(usernameTo, INCOMING_CALL,jo);
    }

    public String getUsernameFrom() {
        return usernameFrom;
    }

    public String getUsernameTo() {
        return usernameTo;
    }

}

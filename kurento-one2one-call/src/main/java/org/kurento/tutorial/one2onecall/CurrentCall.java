package org.kurento.tutorial.one2onecall;

import com.google.gson.JsonObject;
import java.util.LinkedList;
import java.util.List;
import org.kurento.client.IceCandidate;
import org.kurento.client.KurentoClient;
import org.kurento.client.OnIceCandidateEvent;
import org.kurento.jsonrpc.JsonUtils;
import org.kurento.tutorial.one2onecall.utils.JsonFields;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CurrentCall {

    private static final Logger log = LoggerFactory.getLogger(CurrentCall.class);

    private static final String INCOMING_CALL = "incomingCall";
    
    private final String usernameFrom;
    private final String usernameTo;
    private String fromSdpOffer;
    private String toSdpOffer;

    private CallMediaPipeline pipeline;
    
    private final List<IceCandidate> iceCandidateFromList = new LinkedList<>();
    private final List<IceCandidate> iceCandidateToList = new LinkedList<>();
    
    private final NotificationService notificationService;
    private final OverlayManager overlayManager;

    public CurrentCall(String from, String to, String fromSdpOffer,NotificationService notificationService,OverlayManager overlayManager) {
        this.usernameFrom = from;
        this.usernameTo = to;
        this.fromSdpOffer = fromSdpOffer;
        this.notificationService = notificationService;
        this.overlayManager = overlayManager;
    }

    public void startCall(KurentoClient kurento) throws Exception {
        log.info(String.format("Called startCall from %s to %s",usernameFrom,usernameTo));
       
        pipeline = new CallMediaPipeline(kurento,iceCandidateFromList,iceCandidateToList,overlayManager);

        pipeline.getCalleeWebRtcEP().addOnIceCandidateListener(event -> onIceCandidate(usernameTo, event));
        String toSdpAnswer = pipeline.generateSdpAnswerForCallee(toSdpOffer);

        pipeline.getCallerWebRtcEP().addOnIceCandidateListener(event -> onIceCandidate(usernameFrom, event));
        String fromSdpAnswer = pipeline.generateSdpAnswerForCaller(fromSdpOffer);

        JsonObject startCommunication = new JsonObject();
        startCommunication.addProperty(JsonFields.Call.SDP_ANSWER, toSdpAnswer);

        log.debug(String.format("Sending start communication to %s",usernameTo));
        notificationService.notify(usernameTo, JsonFields.Call.START_COMMUNICATION, startCommunication);
        
        
        pipeline.getCalleeWebRtcEP().gatherCandidates();

        JsonObject response = new JsonObject();
        response.addProperty(JsonFields.RESPONSE, ResponseCallStatus.ACCEPTED.toString());
        response.addProperty(JsonFields.Call.SDP_ANSWER, fromSdpAnswer);

        log.debug(String.format("Sending call response to %s",usernameFrom));
        notificationService.notify(usernameFrom, JsonFields.Call.CALL_RESPONSE, response);
      
        pipeline.getCallerWebRtcEP().gatherCandidates();
    }
    
    public void addOverlay2User(String username,int overlayId){
        if(username.equals(usernameFrom))
            pipeline.addOverlayElementOnCaller(overlayId);
        else
            pipeline.addOverlayElementOnCallee(overlayId);
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
            if(pipeline!=null)
                pipeline.addCallerIceCandidate(iceCandidate);
            else
                iceCandidateFromList.add(iceCandidate);
        }else{
            if(pipeline!=null)
                pipeline.addCalleeIceCandidate(iceCandidate);
            else
                iceCandidateToList.add(iceCandidate);
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

    public String getFromSdpOffer() {
        return fromSdpOffer;
    }

    public void setFromSdpOffer(String fromSdpOffer) {
        this.fromSdpOffer = fromSdpOffer;
    }

    public String getToSdpOffer() {
        return toSdpOffer;
    }

    public void setToSdpOffer(String toSdpOffer) {
        this.toSdpOffer = toSdpOffer;
    }

    public String getUsernameFrom() {
        return usernameFrom;
    }

    public String getUsernameTo() {
        return usernameTo;
    } 
    
}

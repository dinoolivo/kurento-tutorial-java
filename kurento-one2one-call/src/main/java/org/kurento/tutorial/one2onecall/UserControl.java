
package org.kurento.tutorial.one2onecall;

import com.google.gson.JsonObject;
import org.kurento.client.KurentoClient;
import org.kurento.jsonrpc.Session;
import org.kurento.jsonrpc.Transaction;
import org.kurento.jsonrpc.message.Request;
import org.kurento.tutorial.one2onecall.data.User;
import org.kurento.tutorial.one2onecall.exception.AppException;
import org.kurento.tutorial.one2onecall.exception.InternalErrorCodes;
import org.kurento.tutorial.one2onecall.utils.JsonFields;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;


public class UserControl {
    
    @Autowired
    private UserRegistry registry;
    
    @Autowired
    private KurentoClient kurento;
    
    @Autowired
    private NotificationService notificationService;
    
    @Autowired
    private OverlayManager overlayManager;
 
    private static final Logger log = LoggerFactory.getLogger(UserControl.class);
    
    public void register(Transaction transaction, Request<JsonObject> request) throws Exception {
        String username = request.getParams().get(JsonFields.User.USERNAME).getAsString();

        if (username.isEmpty()) {
            throw new AppException(InternalErrorCodes.EMPTY_USERNAME, "empty username is not allowed");
        } else if (registry.getBySession(transaction.getSession()) != null) {
            throw new AppException(InternalErrorCodes.USER_ALREADY_REGISTERED, String.format("user with username %s has already an open session as %s", username, registry.getBySession(transaction.getSession()).getUser().getUsername()));
        } else if (registry.exists(username)) {
            throw new AppException(InternalErrorCodes.USER_ALREADY_REGISTERED, String.format("user with username %s is already registered", username));
        }

        UserSession newUserSession = new UserSession(transaction.getSession(), new User(username, UserCallStatus.AVAILABLE));
        registry.register(newUserSession);

        transaction.sendVoidResponse();
    }
    
    
    public void removeUserSession(Session session){
        registry.removeBySession(session);
    }
    
    public void addOverlayFilter2Call(Transaction transaction, Request<JsonObject> request){
        int overlayId = request.getParams().get(JsonFields.Call.OVERLAY_ID).getAsInt();
        
        UserSession user = registry.getBySession(transaction.getSession());
        
        user.getCurrentCall().addOverlay2OtherUser(user.getUser().getUsername(), overlayId);
    }
    
    public void removeOverlayFilter2Call(Transaction transaction, Request<JsonObject> request){
        int overlayId = request.getParams().get(JsonFields.Call.OVERLAY_ID).getAsInt();
        
        UserSession user = registry.getBySession(transaction.getSession());
        
        user.getCurrentCall().removeOverlay2OtherUser(user.getUser().getUsername(), overlayId);
    }
    
    
    public void changeUserStatus(Transaction transaction, Request<JsonObject> request) throws Exception {
        UserCallStatus userStatus = UserCallStatus.fromStr(request.getParams().get(JsonFields.User.STATUS).getAsString());
        
        log.debug("receiving user status " + userStatus.toString());
        registry.changeUserStatus(transaction.getSession(), userStatus);
        
        transaction.sendVoidResponse();
    }
    
    public void sendUsrList(Transaction transaction, Request<JsonObject> request) throws Exception {
        transaction.sendResponse(registry.getUsersList());
    }
    
    
    public void call(Transaction transaction, Request<JsonObject> request) throws Exception {
        
        String to = request.getParams().get(JsonFields.Call.TO).getAsString();
        String sdpOffer = request.getParams().get(JsonFields.Call.SDP_OFFER).getAsString();
        
        UserSession caller = registry.getBySession(transaction.getSession());
        String from = caller.getUser().getUsername();
        if (registry.exists(to)) {
            UserSession callee = registry.getByName(to);
            if (callee.getUser().getStatus() == UserCallStatus.BUSY) {
                throw new AppException(InternalErrorCodes.CALLEE_BUSY, String.format("user %s is busy so he cannot be called", to));
            }
            
            //if incoming call request is sent with success set params
            caller.createNewCallRequest(callee,sdpOffer,notificationService,overlayManager);
            callee.setCurrentCall(caller.getCurrentCall());

            caller.getCurrentCall().sendCallRequestToCallee();
           
            //avoid that other users call 
            registry.changeUserStatus(caller, UserCallStatus.BUSY);
            registry.changeUserStatus(callee, UserCallStatus.BUSY);
            
        } else {
            throw new AppException(InternalErrorCodes.USER_DOES_NOT_EXIST, String.format("User %s does not exists or disconnected so he cannot be called", to));
        }
    }
    
    public void incomingCallResponse(Transaction transaction, Request<JsonObject> request) throws Exception {
        
        final UserSession called = registry.getBySession(transaction.getSession());
        final UserSession caller = registry.getByName(called.getCurrentCall().getUsernameFrom());
        
        ResponseCallStatus responseCallStatus = ResponseCallStatus.fromString(request.getParams().get(JsonFields.Call.CALL_RESPONSE).getAsString());
        
        if(responseCallStatus == ResponseCallStatus.ACCEPTED){
            log.debug("Accepted call from '{}' to '{}'", caller.getUser().getUsername(), called.getUser().getUsername());
            //TODO check if missing field sdpOffer
            
            try{
                caller.getCurrentCall().setCallUserToSdpOffer(request.getParams().get(JsonFields.Call.SDP_OFFER).getAsString());
                caller.getCurrentCall().startCall(kurento);
            }catch(Exception e){
                log.error("failed to start call",e);
                CurrentCall currentCall = caller.getCurrentCall();
                
                currentCall.sendCallRejected(ResponseCallStatus.REFUSED);
                currentCall.sendStopCommunication(called.getUser().getUsername());
                
                
                caller.clearCurrentCall();
                called.clearCurrentCall();
            }
        }else{
             caller.getCurrentCall().sendCallRejected(responseCallStatus);
        }
        
    }
    
    public void onIceCandidate(Transaction transaction, Request<JsonObject> request){
        UserSession currUser = registry.getBySession(transaction.getSession());
        if(currUser.getCurrentCall()!=null)
            currUser.getCurrentCall().addIceCandidateFromMessage(currUser.getUser().getUsername(), request.getParams());
        else
            throw new AppException(InternalErrorCodes.CALL_DOES_NOT_EXIST, String.format("No available call for user "+currUser.getUser().getUsername()));
    }
    
    public void stop(Transaction transaction) throws Exception{
        UserSession userSession = registry.getBySession(transaction.getSession());
        String from = userSession.getCurrentCall().getUsernameFrom();
        String to = userSession.getCurrentCall().getUsernameTo();
        registry.changeUserStatus(from, UserCallStatus.AVAILABLE);
        registry.changeUserStatus(to, UserCallStatus.AVAILABLE);
        userSession.endCall();
    }
    
}

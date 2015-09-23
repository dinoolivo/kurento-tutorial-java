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

import com.google.common.reflect.TypeToken;
import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;

import org.kurento.client.EventListener;
import org.kurento.client.KurentoClient;
import org.kurento.client.OnIceCandidateEvent;
import org.kurento.jsonrpc.JsonUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import java.lang.reflect.Type;
import org.kurento.tutorial.one2onecall.codes.StatusCode;
import org.kurento.tutorial.one2onecall.data.AppError;
import org.kurento.tutorial.one2onecall.data.CallArgs;
import org.kurento.tutorial.one2onecall.data.IncomingCallReq;
import org.kurento.tutorial.one2onecall.data.Request;
import org.kurento.tutorial.one2onecall.data.Response;
import org.kurento.tutorial.one2onecall.data.SdpAnswer;
import org.kurento.tutorial.one2onecall.data.User;
import org.kurento.tutorial.one2onecall.data.UserList;
import org.kurento.tutorial.one2onecall.exception.AppException;
import org.kurento.tutorial.one2onecall.exception.InternalErrorCodes;
import org.kurento.tutorial.one2onecall.exception.WSUtils;
import org.kurento.tutorial.one2onecall.utils.SerializationUtils;
import org.kurento.tutorial.one2onecall.utils.UserStatusEnumAdapter;

/**
 * Protocol handler for 1 to 1 video call communication.
 *
 * @author Boni Garcia (bgarcia@gsyc.es)
 * @author Micael Gallego (micael.gallego@gmail.com)
 * @since 4.3.1
 */
public class CallHandler extends TextWebSocketHandler {

    private static final Logger log = LoggerFactory.getLogger(CallHandler.class);
    private static final Gson gson = new GsonBuilder().create();

    private final ConcurrentHashMap<String, CallMediaPipeline> pipelines = new ConcurrentHashMap<>();

    @Autowired
    private KurentoClient kurento;

    @Autowired
    private UserRegistry registry;

    @Override
    public void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {

        JsonObject jsonMessage = gson.fromJson(message.getPayload(), JsonObject.class);
        log.debug("Incoming message is: {}", jsonMessage);

        String messagePayload = message.getPayload();

        switch (jsonMessage.get(JsonConsts.METHOD).getAsString()) {
            case WsEndpoints.REGISTER:
                WSUtils.manageIfException(session, WsEndpoints.REGISTER, () -> register(session, messagePayload));
                break;
            case WsEndpoints.USRS_LIST:
                WSUtils.manageIfException(session, WsEndpoints.USRS_LIST, () -> sendUsrList(session));
                break;

            case WsEndpoints.CALL:
                WSUtils.manageIfException(session, WsEndpoints.CALL, () -> call(session, messagePayload));
                break;

            case WsEndpoints.INCOMING_CALL:
                WSUtils.manageIfException(session, WsEndpoints.INCOMING_CALL, () -> incomingCallResponse(session, messagePayload));
                break;
            case WsEndpoints.CHANGE_USER_STATUS:
                WSUtils.manageIfException(session, WsEndpoints.CHANGE_USER_STATUS, () -> changeUserStatus(session, messagePayload));
                break;
            /*    
             case "onIceCandidate": {
             JsonObject candidate = jsonMessage.get("candidate").getAsJsonObject();
             if (userSession != null) {
             IceCandidate cand = new IceCandidate(candidate.get("candidate").getAsString(),
             candidate.get("sdpMid").getAsString(), candidate.get("sdpMLineIndex").getAsInt());
             userSession.addCandidate(cand);
             }
             break;
             }
            
             case "stop":
             stop(session);
             break;*/
            default:
                break;
        }
    }

    public boolean isNewUser(WebSocketSession session) {
        return registry.getBySession(session) == null;
    }

    private void changeUserStatus(WebSocketSession session, String jsonMessageStr) throws IOException {
        Request<User> request = SerializationUtils.decodeRequest(jsonMessageStr, new TypeToken<Request<User>>() {
        }.getType());
        log.debug("receiving user status " + request.getArguments().getStatus().toString());
        registry.changeUserStatus(session, request.getArguments().getStatus());
        Response response = new Response.ResponseBuilder<>(WsEndpoints.CHANGE_USER_STATUS).status(StatusCode.OK.intValue()).response("").build();
        registry.getBySession(session).sendMessage(response.toJsonStr());
    }

    private void sendUsrList(WebSocketSession session) throws IOException {
        Response<UserList> response = new Response.ResponseBuilder<>(WsEndpoints.USRS_LIST)
                .status(StatusCode.OK.intValue())
                .response(new UserList(registry.getUsersList())).build();
        WSUtils.sendMessage(session, response.toJsonStr());
    }

    private void register(WebSocketSession session, String jsonMessageStr) throws IOException {
        Request<User> request = SerializationUtils.decodeRequest(jsonMessageStr, new TypeToken<Request<User>>() {
        }.getType());
        String username = request.getArguments().getUsername();

        if (username.isEmpty()) {
            throw new AppException(InternalErrorCodes.EMPTY_USERNAME, "empty username is not allowed");
        } else if (registry.getBySession(session) != null) {
            throw new AppException(InternalErrorCodes.USER_ALREADY_REGISTERED, String.format("user with username %s has already an open session as %s", username, registry.getBySession(session).getUser().getUsername()));
        } else if (registry.exists(username)) {
            throw new AppException(InternalErrorCodes.USER_ALREADY_REGISTERED, String.format("user with username %s is already registered", username));
        }

        UserSession newUser = new UserSession(session, new User(username, UserCallStatus.AVAILABLE));
        registry.register(newUser);

        Response response = new Response.ResponseBuilder<>(WsEndpoints.REGISTER).status(StatusCode.OK.intValue()).response("").build();
        newUser.sendMessage(response.toJsonStr());
    }

    private void call(WebSocketSession session, String jsonMessageStr) throws IOException {
        Request<CallArgs> request = SerializationUtils.decodeRequest(jsonMessageStr, new TypeToken<Request<CallArgs>>() {
        }.getType());
        UserSession caller = registry.getBySession(session);
        String from = caller.getUser().getUsername();
        String to = request.getArguments().getTo();
        if (registry.exists(to)) {
            UserSession callee = registry.getByName(to);
            if (callee.getUser().getStatus() == UserCallStatus.BUSY) {
                throw new AppException(InternalErrorCodes.CALLEE_BUSY, String.format("user %s is busy so he cannot be called", to));
            }

            caller.setSdpOffer(request.getArguments().getSdpOffer());
            caller.setCallingTo(to);

            //request incoming call
            Request<IncomingCallReq> icReq = new Request<>(WsEndpoints.INCOMING_CALL, new IncomingCallReq(from));
            callee.sendMessage(icReq.toJsonStr());
            callee.setCallingFrom(from);

        } else {
            throw new AppException(InternalErrorCodes.USER_DOES_NOT_EXIST, String.format("User %s does not exists or disconnected so he cannot be called", to));
        }
    }

    private void incomingCallResponse(final WebSocketSession session, String jsonMessageStr) throws IOException {
        Response<SdpAnswer> icResponse = SerializationUtils.decodeResponse(jsonMessageStr, new TypeToken<Request<SdpAnswer>>() {
        }.getType());

        final UserSession called = registry.getBySession(session);
        final UserSession callee = registry.getByName(called.getCallingFrom());

        String from = called.getCallingFrom();
        String to = callee.getUser().getUsername();

        Response responseRefused = new Response.ResponseBuilder<>(WsEndpoints.CALL).status(StatusCode.BAD_REQUEST.intValue())
                .error(new AppError(InternalErrorCodes.CALL_REFUSED.getMessage(),
                                String.format("call to user %s was refused", called.getUser().getUsername()),
                                InternalErrorCodes.CALL_REFUSED.getCode()))
                .build();

        if (icResponse.getStatus() == StatusCode.OK.intValue()) {
            log.debug("Accepted call from '{}' to '{}'", from, to);
            CallMediaPipeline pipeline = null;
            try {

            } catch (Throwable t) {
                log.error(t.getMessage(), t);

                if (pipeline != null) {
                    pipeline.release();
                }

                pipelines.remove(called.getSessionId());
                pipelines.remove(callee.getSessionId());
                WSUtils.sendMessage(callee.getSession(), responseRefused.toJsonStr());
                throw new AppException(InternalErrorCodes.UNHANDLED_ERROR, String.format("failed to perform the call between user %s and user %s", from, to));
            }

        } else {
            WSUtils.sendMessage(callee.getSession(), responseRefused.toJsonStr());
        }

    }

    private CallMediaPipeline startCall(final UserSession called, final UserSession callee, String calledSdpOffer) {
        CallMediaPipeline pipeline = new CallMediaPipeline(kurento);

        callee.setWebRtcEndpoint(pipeline.getCalleeWebRtcEP());
        pipeline.getCalleeWebRtcEP().addOnIceCandidateListener(event -> {
            JsonObject iceCandidateJo = new JsonObject();
            iceCandidateJo.add("candidate", JsonUtils.toJsonObject(event.getCandidate()));
            Request<JsonObject> iceReq = new Request<>(WsEndpoints.ICE_CANDIDATE, iceCandidateJo);
            try {
                synchronized (callee.getSession()) {
                    WSUtils.sendMessage(callee.getSession(), iceReq.toJsonStr());
                }
            } catch (IOException e) {
                log.debug(e.getMessage());
            }
        });
        //continuare da qua

        return pipeline;
    }

    private void incomingCallResponse(final UserSession callee, JsonObject jsonMessage) throws IOException {
        String callResponse = jsonMessage.get("callResponse").getAsString();
        String from = jsonMessage.get("from").getAsString();
        final UserSession calleer = registry.getByName(from);
        String to = calleer.getCallingTo();

        if ("accept".equals(callResponse)) {
            log.debug("Accepted call from '{}' to '{}'", from, to);

            CallMediaPipeline pipeline = null;
            try {
                pipeline = new CallMediaPipeline(kurento);
                pipelines.put(calleer.getSessionId(), pipeline);
                pipelines.put(callee.getSessionId(), pipeline);

                String calleeSdpOffer = jsonMessage.get("sdpOffer").getAsString();
                callee.setWebRtcEndpoint(pipeline.getCalleeWebRtcEP());
                pipeline.getCalleeWebRtcEP().addOnIceCandidateListener(new EventListener<OnIceCandidateEvent>() {
                    @Override
                    public void onEvent(OnIceCandidateEvent event) {
                        JsonObject response = new JsonObject();
                        response.addProperty("id", "iceCandidate");
                        response.add("candidate", JsonUtils.toJsonObject(event.getCandidate()));
                        try {
                            synchronized (callee.getSession()) {
                                callee.getSession().sendMessage(new TextMessage(response.toString()));
                            }
                        } catch (IOException e) {
                            log.debug(e.getMessage());
                        }
                    }
                });

                String calleeSdpAnswer = pipeline.generateSdpAnswerForCallee(calleeSdpOffer);
                String callerSdpOffer = registry.getByName(from).getSdpOffer();
                calleer.setWebRtcEndpoint(pipeline.getCallerWebRtcEP());
                pipeline.getCallerWebRtcEP().addOnIceCandidateListener(new EventListener<OnIceCandidateEvent>() {

                    @Override
                    public void onEvent(OnIceCandidateEvent event) {
                        JsonObject response = new JsonObject();
                        response.addProperty("id", "iceCandidate");
                        response.add("candidate", JsonUtils.toJsonObject(event.getCandidate()));
                        try {
                            synchronized (calleer.getSession()) {
                                calleer.getSession().sendMessage(new TextMessage(response.toString()));
                            }
                        } catch (IOException e) {
                            log.debug(e.getMessage());
                        }
                    }
                });

                String callerSdpAnswer = pipeline.generateSdpAnswerForCaller(callerSdpOffer);

                JsonObject startCommunication = new JsonObject();
                startCommunication.addProperty("id", "startCommunication");
                startCommunication.addProperty("sdpAnswer", calleeSdpAnswer);

                synchronized (callee) {
                    callee.sendMessage(startCommunication);
                }

                pipeline.getCalleeWebRtcEP().gatherCandidates();

                JsonObject response = new JsonObject();
                response.addProperty("id", "callResponse");
                response.addProperty("response", "accepted");
                response.addProperty("sdpAnswer", callerSdpAnswer);

                synchronized (calleer) {
                    calleer.sendMessage(response);
                }

                pipeline.getCallerWebRtcEP().gatherCandidates();

            } catch (Throwable t) {
                log.error(t.getMessage(), t);

                if (pipeline != null) {
                    pipeline.release();
                }

                pipelines.remove(calleer.getSessionId());
                pipelines.remove(callee.getSessionId());

                JsonObject response = new JsonObject();
                response.addProperty("id", "callResponse");
                response.addProperty("response", "rejected");
                calleer.sendMessage(response);

                response = new JsonObject();
                response.addProperty("id", "stopCommunication");
                callee.sendMessage(response);
            }

        } else {
            JsonObject response = new JsonObject();
            response.addProperty("id", "callResponse");
            response.addProperty("response", "rejected");
            calleer.sendMessage(response);
        }
    }

    public void stop(WebSocketSession session) throws IOException {
        String sessionId = session.getId();
        if (pipelines.containsKey(sessionId)) {
            pipelines.get(sessionId).release();
            CallMediaPipeline pipeline = pipelines.remove(sessionId);
            pipeline.release();

            // Both users can stop the communication. A 'stopCommunication'
            // message will be sent to the other peer.
            UserSession stopperUser = registry.getBySession(session);
            UserSession stoppedUser = (stopperUser.getCallingFrom() != null)
                    ? registry.getByName(stopperUser.getCallingFrom()) : registry.getByName(stopperUser.getCallingTo());

            JsonObject message = new JsonObject();
            message.addProperty("id", "stopCommunication");
            stoppedUser.sendMessage(message);
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        registry.removeBySession(session);
    }

}

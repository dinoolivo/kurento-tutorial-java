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

import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.factory.annotation.Autowired;
import com.google.gson.JsonObject;
import org.kurento.jsonrpc.DefaultJsonRpcHandler;
import org.kurento.jsonrpc.Session;
import org.kurento.jsonrpc.Transaction;
import org.kurento.jsonrpc.message.Request;
import org.kurento.tutorial.one2onecall.exception.AppException;
import org.kurento.tutorial.one2onecall.exception.InternalErrorCodes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



public class CallHandler extends DefaultJsonRpcHandler<JsonObject>  {

    private static final Logger log = LoggerFactory.getLogger(CallHandler.class);
    
    private final ConcurrentHashMap<String, CallMediaPipeline> pipelines = new ConcurrentHashMap<>();

    @Autowired
    private UserControl userControl;
    
    
    @Override
    public void handleRequest(Transaction transaction, Request<JsonObject> request) throws Exception {
        
        try{
            switch (request.getMethod()) {
                case WsEndpoints.REGISTER:
                    userControl.register(transaction, request);
                    break;
                case WsEndpoints.USRS_LIST:
                    userControl.sendUsrList(transaction, request);
                    break;
                case WsEndpoints.CHANGE_USER_STATUS:
                    userControl.changeUserStatus(transaction, request);
                    break;
                case WsEndpoints.CALL:
                    userControl.call(transaction, request);
                    break;
                case WsEndpoints.ADD_OVERLAY:
                    userControl.addOverlayFilter2Call(transaction, request);
                    break;
                case WsEndpoints.INCOMING_CALL_RESPONSE:
                    userControl.incomingCallResponse(transaction, request);
                    break;
                case WsEndpoints.ICE_CANDIDATE:
                    userControl.onIceCandidate(transaction, request);
                    break;
                case WsEndpoints.STOP:
                    userControl.stop(transaction);
                    break;
                default: log.error("Unrecognized request method "+request.getMethod());
                
            }
        }catch(Exception e){
            log.error("Error on handling request method "+request.getMethod(),e);
            InternalErrorCodes iec;

            if (e instanceof AppException) {
                AppException ae = (AppException) e;
                iec = ae.getErrorCode();
            } else {
                iec = InternalErrorCodes.UNHANDLED_ERROR;
            }
            transaction.sendError(iec.getCode(),iec.getMessage(),e.getMessage());
        }
    }
    
    

   
    @Override
    public void afterConnectionClosed(Session session, String status) throws Exception {
        userControl.removeUserSession(session);
    }

    /*
    {
	"id" : 1,
	"error" : {
		"code" : 4001,
		"message" : "empty username",
		"data" : "empty username is not allowed",
		"sessionId" : "akmi26qe21cjaae91t45bvcr3k"
	},
	"jsonrpc" : "2.0",
	"data" : {}
}

{"id":1,"jsonrpc":"2.0","result":{"sessionId":"qq6v7l2kgpsdbmpj20rkec0nuk"}}


{"method":"notification","params":{"target":"userList","content":{"username":"fgd","status":"AVAILABLE"}},"jsonrpc":"2.0"}
    */

    

    

}

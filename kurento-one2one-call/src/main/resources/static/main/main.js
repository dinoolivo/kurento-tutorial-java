(function () {

    var o2oMain = angular.module("o2o.main", []);

    var STATUS_AVAILABLE = "AVAILABLE";
    var STATUS_BUSY = "BUSY";
    var STATUS_DISCONNECTED = "DISCONNECTED";

    o2oMain.controller("MainCtrl", ["$log", "WsService", "UserService", "$rootScope", function ($log, WsService, UserService, $rootScope) {

            var mrg = this;

            var userDict = {};

            var idUsrList = 2;
            var idUChangeUsrStatus = 3;
            //temp---------------------
            var idIceCandidate = 4;
            var idCall = 5;
            var incomingCallId = 6;
            var stopId = 7;
            var incomingCallResponseId=8;
            var webRtcPeer;
            //-------------------------

            mrg.currentUser = UserService.getUser();
            mrg.userStatuses = [STATUS_AVAILABLE, STATUS_BUSY];
            mrg.currStatus = mrg.currentUser.status;
            mrg.callUser = null;

            mrg.error = {};

            var usrListReq = {
                id: idUsrList,
                method: "userList",
                params: {}
            };

            var changeUsrStatusReq = {
                id: idUChangeUsrStatus,
                method: "changeUserStatus",
                params: {}
            }

            mrg.users = {};

            mrg.setLabelClassFromStatus = function (status) {
                var c = "label ";
                if (status == STATUS_AVAILABLE) {
                    c += 'label-success';
                } else {
                    c += 'label-danger';
                }
                return c;
            }

            mrg.changeUserStatus = function (userStatus) {
                changeUsrStatusReq.params = {status: userStatus}
                $log.debug("sending " + JSON.stringify(changeUsrStatusReq));
                UserService.setStatus(userStatus);
                WsService.sendMessage(JSON.stringify(changeUsrStatusReq));
            }

            mrg.showCallPanel = function (usr) {
                mrg.callUser = usr;
            }

            WsService.sendMessage(JSON.stringify(usrListReq));

            function setUsers(users) {
                users.forEach(function (user) {
                    userDict[user.username] = user;
                });
                mrg.users.lst = users;
            }

            WsService.registerHandler(function (messageStr) {
                $log.debug("received message from server:");
                $log.debug(messageStr);
                var message = JSON.parse(messageStr);
                if (message.id)
                    manageResponse(message);
                else if (message.method === "notification")
                    manageNotification(message);
                else
                    $log.error("unrecognized message " + messageStr);
            });

            function manageResponse(message) {
                switch (message.id) {
                    case idUsrList:
                        $log.debug("userList!");
                        $log.debug(JSON.stringify(message));
                        $rootScope.$apply(function () {
                            setUsers(message.result.value);
                        });
                        break;
                    case idUChangeUsrStatus:
                        if (message.error) {
                            $rootScope.$apply(function () {
                                mrg.error.message = message.error.data
                            });
                        }
                        break;
                    
                    default:
                        $log.debug("unrecognized id " + message.id);
                }
            }

            function manageNotification(message) {
                switch (message.params.target) {
                    case "userList":
                        $log.debug("userList change notification!");

                        $rootScope.$apply(function () {
                            var user = message.params.content;
                            if (userDict[user.username]) {
                                var idx = mrg.users.lst.indexOf(userDict[user.username]);
                                mrg.users.lst.splice(idx, 1);
                            } else {
                                userDict[user.username] = user;
                            }
                            if (user.status != STATUS_DISCONNECTED)
                                mrg.users.lst.push(user);
                        });
                        break;
                    //temp------------------------------------------
                    case "incomingCall":
                        incomingCall(message.params.content.from);
                        break;
                    case "iceCandidate":
                        webRtcPeer.addIceCandidate(message.params.content.candidate, function(error) {
                            if (error)
                                return $log.error('Error adding candidate: ' + error);
                        });
                        break;
                    case 'callResponse':
                        callResponse(message.params.content);
                        break;
                    case "startCommunication":
                        startCommunication(message.params.content);
                        break;
                    case 'stopCommunication':
                        $log.info('Communication ended by remote peer');
                        stop(true);
                        break;
                    //----------------------------------------------

                    default:
                        $log.debug("unrecognized target " + message.params.target);
                }
            }

            //SOLO DI PROVA POI VA SISTEMATO BENE
            //-----------------------------------------------------------
            var videoInput = document.getElementById('videoInput');
            var videoOutput = document.getElementById('videoOutput');
            var dest={};
            
            mrg.call = function (to) {
                dest.to = to;
                var options = {
                    localVideo: videoInput,
                    remoteVideo: videoOutput,
                    onicecandidate: onIceCandidate,
                    onerror: onError
                }
                webRtcPeer = new kurentoUtils.WebRtcPeer.WebRtcPeerSendrecv(options,
                        function (error) {
                            if (error) {
                                return $log.error(error);
                            }
                            webRtcPeer.generateOffer(onOfferCall);
                        });

            }

            function onIceCandidate(candidate) {
                $log.debug("Local candidate" + JSON.stringify(candidate));

                var message = {
                    id: idIceCandidate,
                    method: 'iceCandidate',
                    params:{
                        candidate: candidate
                    }  
                };
                $log.debug("Sending ice message" + JSON.stringify(message));
                WsService.sendMessage(JSON.stringify(message));
            }

            function onError() {
                $log.error("errore!!!!!!");
            }

            function onOfferCall(error, offerSdp) {
                if (error)
                    return $log.error('Error generating the offer');
                $log.debug('Invoking SDP offer callback function');
                var message = {
                    id:idCall,
                    method: 'call',
                    params:{
                        to: dest.to.username,
                        sdpOffer: offerSdp
                    }  
                };
                $log.debug("Sending call request message" + JSON.stringify(message));
                WsService.sendMessage(JSON.stringify(message));
            }
            
            function incomingCall(from){
                if (confirm('User ' + from
			+ ' is calling you. Do you accept the call?')) {
                    mrg.callUser = {username: from};
                    var options = {
                            localVideo : videoInput,
                            remoteVideo : videoOutput,
                            onicecandidate : onIceCandidate,
                            onerror : onError
                    }
                    webRtcPeer = new kurentoUtils.WebRtcPeer.WebRtcPeerSendrecv(options,
                                    function(error) {
                                        $log.info("Try to generate offer...");
                                            if (error) {
                                                    return $log.error(error);
                                            }
                                            webRtcPeer.generateOffer(onOfferIncomingCall);
                                    });

                } else {
                        var response = {
                                id: incomingCallId,
                                method : 'incomingCall',
                                params:{
                                    callResponse : 'REFUSED',
                                }
                        };
                        WsService.sendMessage(JSON.stringify(response));
                        stop();
                }
            }
            
            function stop(received) {
                if (webRtcPeer) {
                        webRtcPeer.dispose();
                        webRtcPeer = null;

                        if (!received) {
                            var message = {
                                    id : stopId,
                                    method: 'stop'
                            }
                            WsService.sendMessage(JSON.stringify(message));
                        }
                }
            }
            
            function onOfferIncomingCall(error, offerSdp) {
                if (error)
                        return console.error("Error generating the offer");
                var response = {
                        id: incomingCallResponseId,
                        method : 'incomingCallResponse',
                        params:{
                            callResponse : 'ACCEPTED',
                            sdpOffer : offerSdp
                        }      
                };
                $log.debug("sending incoming call response message "+JSON.stringify(response))
                WsService.sendMessage(JSON.stringify(response));
            }
            
            function startCommunication(message) {
                webRtcPeer.processAnswer(message.sdpAnswer, function(error) {
                        if (error){
                           $log.error("error on processing sdpAnswer of startcommunication!");
                           return $log.error(error);
                        }
                });
            }
            
            function callResponse(message) {
                if (message.response != 'ACCEPTED') {
                    $log.info('Call not accepted by peer. Closing call');
                    stop();
                } else {
                    webRtcPeer.processAnswer(message.sdpAnswer, function(error) {
                            if (error){
                                $log.error("error on processing sdpAnswer of callResponse");
                                return $log.error(error);
                            }
                    });
                }
            }


        }]);
})();



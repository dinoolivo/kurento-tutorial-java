(function () {

    var o2oMain = angular.module("o2o.main");

    o2oMain.directive('ngCall', function () {
        return {
            restrict: 'AE',
            scope: {
               userCall: '='
            },
            templateUrl: '/directives/call/call.html',
            controller: CallController,
            controllerAs: 'cc',
            bindToController: true
        }
    });

    CallController.$inject = ['$scope', '$log', 'WsService'];

    
    //call ids
    var ICE_CADIDATE = {id: 4, label: "iceCandidate"};
    var CALL = {id: 5, label: "call"};
    var INCOMING_CALL = {id: 6, label: "incomingCall"};
    var STOP = {id: 7, label: "stop"};
    var INCOMING_CALL_RESPONSE = {id: 8, label: "incomingCallResponse"};
    var CALL_RESPONSE = {id: 9, label: "callResponse"};
    var START_COMMUNICATION = {id: 10, label: "startCommunication"};
    var STOP_COMMUNICATION = {id: 11, label: "stopCommunication"};

    var webRtcPeer;
    
    var CALL_ACCEPTED = "ACCEPTED";
    var CALL_REJECTED = "REFUSED";
    

    function CallController($scope, $log, WsService) {
        
        var videoInput = document.getElementById('videoInput');
        var videoOutput = document.getElementById('videoOutput');
        
        var callActions ={
            "iceCandidate": onReceivedIceCandidate,
            "incomingCall": incomingCall,
            "startCommunication": startCommunication,
            "stopCommunication":stop,
            "callResponse":callResponse
        };
        
        var cc = this;
       
        var dest = {};
        
        $scope.$watch('cc.userCall', function (newUserCall) {
            $log.debug("New Value for userCall!");
            $log.debug(JSON.stringify(newUserCall));
            //$scope.$apply(function(){});
        });

        WsService.registerHandler(callWsHandler);
        
        cc.call = call;
        cc.stopCall = stop;
        
        cc.onCall = false;

        function callWsHandler(messageStr) {
            $log.debug("received message from server:");
            $log.debug(messageStr);
            var message = JSON.parse(messageStr);
            if(isNotification(message)){
                if(callActions[message.params.target])
                    callActions[message.params.target](message.params.content);
                else
                    $log.warn("target not recognized "+ message.params.target);
            }
        }
        
        function isNotification(message){
            return !message.id && message.method == "notification";
        }

        function call(to) {
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
            cc.onCall = true;

        }

        function onIceCandidate(candidate) {
            var message = {
                id: ICE_CADIDATE.id,
                method: ICE_CADIDATE.label,
                params: {
                    candidate: candidate
                }
            };
            $log.debug("Sending local ice candidate " + JSON.stringify(message));
            WsService.sendMessage(JSON.stringify(message));
        }

        function onError(error) {
            $log.error(error);
        }

        function onReceivedIceCandidate(params) {
            var candidate = params.candidate;
            webRtcPeer.addIceCandidate(candidate, function (error) {
                if (error)
                    return $log.error('Error adding candidate: ' + error);
            });
        }

        function onOfferCall(error, offerSdp) {
            if (error)
                return $log.error('Error generating the offer');
            $log.debug('Invoking SDP offer callback function');
            var message = {
                id: CALL.id,
                method: CALL.label,
                params: {
                    to: dest.to.username,
                    sdpOffer: offerSdp
                }
            };
            var messageStr = JSON.stringify(message);
            $log.debug("Sending call request message" + messageStr);
            WsService.sendMessage(messageStr);
        }

        function incomingCall(params) {
            var from = params.from;
            cc.userCall = {username: from};
            if (confirm('User ' + from
                    + ' is calling you. Do you accept the call?')) {
                var options = {
                    localVideo: videoInput,
                    remoteVideo: videoOutput,
                    onicecandidate: onIceCandidate,
                    onerror: onError
                }
                webRtcPeer = new kurentoUtils.WebRtcPeer.WebRtcPeerSendrecv(options,
                        function (error) {
                            $log.info("Try to generate offer...");
                            if (error) {
                                return $log.error(error);
                            }
                            webRtcPeer.generateOffer(onOfferIncomingCall);
                        });
                cc.onCall = true;

            } else {
                cc.userCall = null;
                var response = {
                    id: INCOMING_CALL.id,
                    method: INCOMING_CALL.label,
                    params: {
                        callResponse: CALL_REJECTED,
                    }
                };
                WsService.sendMessage(JSON.stringify(response));
                stop();
            }
        }

        function stop(remote) {
            cc.onCall = false;
            
            if (webRtcPeer) {
                webRtcPeer.dispose();
                webRtcPeer = null;
                $log.debug("webRtcPeer esiste");
                
                if (!remote) { //if the call is stopped by this user and not the remote one send the stop message to the server
                    $log.debug("sending stop message to the server");
                    var message = {
                        id: STOP.id,
                        method: STOP.label
                    }
                    WsService.sendMessage(JSON.stringify(message));
                }
            }
        }

        function onOfferIncomingCall(error, offerSdp) {
            if (error)
                return $log.error("Error generating the offer "+JSON.stringify(error));
            var response = {
                id: INCOMING_CALL_RESPONSE.id,
                method: INCOMING_CALL_RESPONSE.label,
                params: {
                    callResponse: CALL_ACCEPTED,
                    sdpOffer: offerSdp
                }
            };
            $log.debug("sending incoming call response message " + JSON.stringify(response))
            WsService.sendMessage(JSON.stringify(response));
        }

        function startCommunication(message) {
            webRtcPeer.processAnswer(message.sdpAnswer, function (error) {
                if (error) {
                    $log.error("error on processing sdpAnswer of startcommunication!");
                    return $log.error(error);
                }
            });
        }

        function callResponse(message) {
            if (message.response != CALL_ACCEPTED) {
                $log.info('Call not accepted by peer. Closing call');
                stop();
            } else {
                webRtcPeer.processAnswer(message.sdpAnswer, function (error) {
                    if (error) {
                        $log.error("error on processing sdpAnswer of callResponse");
                        return $log.error(error);
                    }
                });
            }
        }
    }

    

})();


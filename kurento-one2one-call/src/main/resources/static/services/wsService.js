(function () {


    var o2o = angular.module("o2o");

    
    
    o2o.factory("WsService",['$location',function($location){
            
            var ws = new WebSocket("ws://"+ $location.host()+":"+ $location.port()+ "/call");
            var wsHandlers = [];
            return {
                getWs: function(){
                    return ws;
                },
                registerHandler: function(handler){
                    wsHandlers.push(handler);
                    ws.onmessage = function(message){
                        wsHandlers.forEach(function(currHandler){
                            currHandler(message.data);
                        });   
                    }
                },
                deleteHandler:function(handler){
                    var idx = wsHandlers.indexOf(handler);
                    wsHandlers.splice(idx,1);
                },
                sendMessage: function(message){
                    ws.send(message);
                }
            }
    }]);
    
})();



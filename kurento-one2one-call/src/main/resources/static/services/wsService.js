(function () {


    var o2o = angular.module("o2o");

    
    
    o2o.factory("WsService",['$location',function($location){
            
            var ws = new WebSocket("ws://"+ $location.host()+":"+ $location.port()+ "/call");
            
            return {
                getWs: function(){
                    return ws;
                },
                registerHandler: function(handler){
                    ws.onmessage = function(message){
                        handler(message.data);
                    }
                },
                sendMessage: function(message){
                    ws.send(message);
                }
            }
    }]);
    
})();



(function () {

    var o2oRegister = angular.module("o2o.register", ["ngRoute"]);

    
    
    o2oRegister.controller("RegisterController",["$log","WsService","UserService","$location",function($log,WsService,UserService,$location){
            var rg = this;
            //var ws = WsService.getWs();
            
            var data = {
                method: "register",
                arguments:{}
            }
            rg.error={};
            rg.user ={};
            
            rg.displayIfError =function(){
                if(rg.error.length > 0){
                    $log.debug("error contain something");
                    return 'showed'
                }
                return 'hidden';
            }
            
            rg.register = function(user){
                data.arguments = user;
                var message =JSON.stringify(data);
                $log.debug(message);
                WsService.sendMessage(message);
            }
            
            WsService.registerHandler(function(messageStr){
                $log.debug("received message from server:");
                $log.debug(messageStr);
                var message = JSON.parse(messageStr);
                switch(message.method){
                    case "register":
                            if(message.status == 200){
                                $log.debug("User registration succeded");
                                UserService.setUsername(rg.user.username);
                                UserService.setStatus("Available");
                                $location.path("main")
                            }else{
                                rg.error.message = message.error.message;
                            }
                            break;
                    default: $log.debug("unrecognized method "+message.method);
                }
                
            });
    }]);
    
})();



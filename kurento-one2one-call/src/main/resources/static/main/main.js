(function () {

    var o2oMain = angular.module("o2o.main", []);
    
    var STATUS_AVAILABLE = "Available";
    var STATUS_BUSY = "Busy";

    o2oMain.controller("MainCtrl",["$log","WsService","UserService","$rootScope",function($log,WsService,UserService,$rootScope){
            
            var mrg = this;
            
            mrg.currentUser = UserService.getUser();
            mrg.userStatuses = [STATUS_AVAILABLE,STATUS_BUSY];
            mrg.currStatus = mrg.currentUser.status;
            
            var usrListReq = {
                method: "userList",
                arguments:{}
            };
            
            var changeUsrStatusReq={
                method: "changeUserStatus",
                arguments:{}
            }
            
            mrg.users = {};
            
            mrg.setLabelClassFromStatus = function(status){
                var c = "label ";
                if(status == STATUS_AVAILABLE){
                    c+= 'label-success';
                }else{
                    c+= 'label-danger';
                }
                return c;
            }
            
            mrg.changeUserStatus=function(userStatus){
                changeUsrStatusReq.arguments={status:userStatus}
                $log.debug("sending "+JSON.stringify(changeUsrStatusReq));
                UserService.setStatus(userStatus);
                WsService.sendMessage(JSON.stringify(changeUsrStatusReq));
            }
            
            mrg.showCP = false;
            mrg.showCallPanel= function(usr){
                mrg.showCP = true;
            }
            
            WsService.sendMessage(JSON.stringify(usrListReq));
            
            function setUsers(users){
                mrg.users.lst = users;
            }

            WsService.registerHandler(function(messageStr){
                $log.debug("received message from server:");
                $log.debug(messageStr);
                var message = JSON.parse(messageStr);
                switch(message.method){
                    case "userList": 
                                $log.debug("userList!");
                                $log.debug(JSON.stringify(message));
                                $rootScope.$apply(function(){setUsers(message.response.users);});
                                break;
                    default: $log.debug("unrecognized method "+message.method);
                }
            });
            
    }]);
})();



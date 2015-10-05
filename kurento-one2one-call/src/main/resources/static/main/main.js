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
                users.forEach(function (user,index,object) {
                    if(user.username == UserService.getUser().username)
                       object.splice(index, 1); 
                    else    
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
                            if(user.username != UserService.getUser().username){
                                if (userDict[user.username]) {
                                    var idx = mrg.users.lst.indexOf(userDict[user.username]);
                                    mrg.users.lst.splice(idx, 1);
                                } else {
                                    userDict[user.username] = user;
                                }
                                if (user.status != STATUS_DISCONNECTED)
                                    mrg.users.lst.push(user);
                            }
                        });
                        break;
                    default:
                        $log.debug("unrecognized target " + message.params.target);
                }
            }

        }]);
})();



(function () {

    var o2oRegister = angular.module("o2o.register", ["ngRoute"]);



    o2oRegister.controller("RegisterController", ["$log", "WsService", "UserService", "$location", "$rootScope",
        function ($log, WsService, UserService, $location, $rootScope) {
            var rg = this;
            //var ws = WsService.getWs();
            var registerId = 1;
            var statusAvailable = "AVAILABLE";

            var data = {
                id: registerId,
                method: "register",
                params: {}
            }
            rg.error = {};
            rg.user = {};

            rg.displayIfError = function () {
                if (rg.error.length > 0) {
                    $log.debug("error contain something");
                    return 'showed'
                }
                return 'hidden';
            }

            rg.register = function (user) {
                data.params = user;
                var message = JSON.stringify(data);
                $log.debug(message);
                WsService.sendMessage(message);
            }

            function setError(message) {
                rg.error.message = message.error.message;
            }

            WsService.registerHandler(registerHandler);

            function registerHandler(messageStr) {
                $log.debug("received message from server:");
                $log.debug(messageStr);
                var message = JSON.parse(messageStr);
                switch (message.id) {
                    case registerId:
                        if (!message.error) {
                            $log.debug("User registration succeded");
                            UserService.setUsername(rg.user.username);
                            UserService.setStatus(statusAvailable);
                            $log.debug("changing root to main..");
                            $rootScope.$apply(function () {
                                WsService.deleteHandler(registerHandler);
                                $location.path('/main');
                            });
                        } else {
                            $log.debug("Registration error!");
                            setError(message);
                        }
                        break;
                    default:
                        $log.debug("unrecognized method " + message.method);
                }
            }
        }]);

})();



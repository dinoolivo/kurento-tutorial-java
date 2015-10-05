(function () {


    var o2o = angular.module("o2o");



    o2o.factory("UserService", ['$log', function ($log) {
            var user = {};

            var STATUS_AVAILABLE_STR = "AVAILABLE";
            var STATUS_BUSY_STR = "BUSY";
            var STATUS_DISCONNECTED_STR = "DISCONNECTED";

            return {
                setUsername: function (username) {
                    user.username = username;
                },
                setStatus: function (status) {
                    user.status = status;
                },
                getUser: function () {
                    return user;
                },
                getStatuses:function(){
                    return [STATUS_AVAILABLE_STR, STATUS_BUSY_STR];
                },
                STATUS_AVAILABLE: function(){ return STATUS_AVAILABLE_STR;},
                STATUS_BUSY: function(){ return STATUS_BUSY_STR;}             
            }
        }]);

})();



(function () {


    var o2o = angular.module("o2o");

    
    
    o2o.factory("UserService",['$log',function($log){
            var user={};
            
            return {
                setUsername: function(username){
                    user.username = username;
                },
                setStatus:function(status){
                    user.status = status;
                },
                getUser: function(){
                    return user;
                }
            }
    }]);
    
})();



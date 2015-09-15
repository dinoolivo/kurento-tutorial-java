(function () {

    var o2oMain = angular.module("o2o.main", []);

    o2oMain.controller("MainController",["$log","WsService","UserService",function($log,WsService,UserService){
            var mrg = this;
            
            mrg.currentUser = UserService.getUser();
    }]);
})();



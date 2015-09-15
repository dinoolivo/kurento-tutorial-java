(function () {

    var config = {
        wsPath: "/call"
    };


    var o2oRegister = angular.module("o2o");

    
    
    o2oRegister.controller("ConfigService",[function(){
            return {
                getConfig: function(){
                    return config;
                }
            }
    }]);
    
})();




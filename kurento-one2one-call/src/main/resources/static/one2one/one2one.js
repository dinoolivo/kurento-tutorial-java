(function () {

    var o2o = angular.module("o2o", ["o2o.register","o2o.main"]);

    o2o.config(function ($routeProvider) {

        $routeProvider
                .when('/main', {
                    templateUrl: '/main/main.html',
                    controller: 'MainCtrl',
                    controllerAs: 'mrg',
                    resolve: {
                        factory: checkRouting
                    }
                })
                .when('/register', {
                    templateUrl: '/register/register.html',
                    controller: 'RegisterController',
                    controllerAs: 'rg'
                })
                .otherwise({
                    redirectTo: '/register'
                })
                ;
    });
    
    function checkRouting($location,UserService,$log){
        $log.debug("entro in checkRouting!");
        $log.debug("UserService.getUser().length "+Object.keys(UserService.getUser()).length);
        
        if(Object.keys(UserService.getUser()).length > 0)
            return true;
        
        $location.path("/register");
        return false;
    }
})();



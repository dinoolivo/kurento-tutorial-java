(function () {

    var o2o = angular.module("o2o", ["o2o.register","o2o.main"]);

    o2o.config(function ($routeProvider) {

        $routeProvider
                .when('/main', {
                    templateUrl: '/main/main.html',
                    controller: 'MainCtrl',
                    controllerAs: 'mrg'
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
})();



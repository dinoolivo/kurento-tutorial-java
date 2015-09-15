(function () {

    var o2o = angular.module("o2o", ["o2o.register"]);

    o2o.config(function ($routeProvider) {

        $routeProvider
                .when('/main', {
                    templateUrl: '/main/main.html',
                    controller: 'MainController',
                    controllerAs: 'mrg'
                })
                .otherwise({
                    templateUrl: '../register/register.html',
                    controller: 'RegisterController',
                    controllerAs: 'rg'
                })
                ;
    });
})();



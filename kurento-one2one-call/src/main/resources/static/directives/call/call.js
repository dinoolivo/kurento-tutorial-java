(function () {

    var o2oMain = angular.module("o2o.main");

    o2oMain.directive('ngCall', function () {
        return {
            restrict: 'A',
            templateUrl: 'call.html'
        }
    });

})();


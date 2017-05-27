var paginationController = angular.module('paginationController', []);

paginationController.controller('PaginationController', [ '$scope',
    function ($scope) {

        $scope.init = function(numFound, offset, size ){
            $scope.numFound = numFound;
            $scope.offset = offset || 1;
            $scope.size = size;
            $scope.currentPage = $scope.offset/$scope.size;
        }

        $scope.currentPage;
        $scope.maxSize = 4;

    }]);
/**
 * Created by naveensnair on 30/05/17.
 */
var approvalServices = angular.module('approvalServices', []);
approvalServices.factory('approvalService', ['$http', function ($http) {
    return {
        fetch: function (urlStr) {
            var promise = $http({method: 'GET', url: urlStr});
            return promise;
        },
        fetchP: function (data, urlStr) {
            var promise = $http({method: 'POST', data: data, url: urlStr});
            return promise;
        },
        createOrderApproval: function(data) {
            return this.fetchP(data, "/s2/api/entities/materials/");
        }
    }
}]);


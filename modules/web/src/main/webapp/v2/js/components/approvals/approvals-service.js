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
        fetchPU: function (data, urlStr) {
            var promise = $http({method: 'PUT', data: data, url: urlStr});
            return promise;
        },
        createApproval: function(data) {
            return this.fetchP(data, '/s2/api/order-approvals');
        },
        getApprovals: function(offset, size, entityId, orderId, reqStatus, expiry, reqType, reqId, aprId, domainId){
            offset = typeof offset !== 'undefined' ? offset : 0;
            size = typeof size !== 'undefined' ? size : 50;
            var urlStr = '/s2/api/order-approvals/?offset=' + offset + "&size=" + size;
            if(checkNotNullEmpty(entityId)) {
                urlStr = urlStr + "&entity_id=" + entityId;
            }
            if(checkNotNullEmpty(reqStatus)) {
                urlStr = urlStr + "&status=" + reqStatus;
            }
            if(checkNotNullEmpty(expiry)) {
                urlStr = urlStr + "&expiring_in=" + expiry*60;
            }
            if(checkNotNullEmpty(reqType)) {
                urlStr = urlStr + "&type=" + reqType;
            }
            if(checkNotNullEmpty(reqId)) {
                urlStr = urlStr + "&requester_id=" + reqId;
            }
            if(checkNotNullEmpty(aprId)) {
                urlStr = urlStr + "&approver_id=" + aprId;
            }
            if(checkNotNullEmpty(domainId)) {
                urlStr = urlStr + "&domainId=" + domainId;
            }
            if(checkNotNullEmpty(orderId)) {
                urlStr = urlStr + "&order_id=" + orderId;
            }
            urlStr = urlStr + "&embed=order_meta";

            return this.fetch(urlStr);
        },
        fetchApproval: function(approvalId) {
            return this.fetch("/s2/api/order-approvals/" + approvalId);
        },
        updateApprovalStatus: function(approvalId, approval) {
            return this.fetchPU(approval, "/s2/api/order-approvals/" + approvalId + "/status")
        }
    }
}]);


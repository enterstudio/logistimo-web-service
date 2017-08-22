/*
 * Copyright © 2017 Logistimo.
 *
 * This file is part of Logistimo.
 *
 * Logistimo software is a mobile & web platform for supply chain management and remote temperature monitoring in
 * low-resource settings, made available under the terms of the GNU Affero General Public License (AGPL).
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General
 * Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any
 * later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Affero General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Affero General Public License along with this program.  If not, see
 * <http://www.gnu.org/licenses/>.
 *
 * You can be released from the requirements of the license by purchasing a commercial license. To know more about
 * the commercial license, please contact us at opensource@logistimo.com
 */

var approvalControllers = angular.module('approvalControllers', []);
approvalControllers.controller('ApprovalDetailCtrl', ['$scope', 'approvalService', 'ordService', 'APPROVAL', '$uibModal',
    function ($scope, approvalService, ordService, APPROVAL, $uibModal) {
        $scope.init = function () {
            $scope.approval = {};
            $scope.reqeusted = false;
            $scope.cancelled = false;
            $scope.rejected = false;
            $scope.approved = false;
            $scope.expired = false;
            $scope.configApprovers = false;
            $scope.request = false;
            $scope.cancelApproval = false;
            $scope.reject = false;
            $scope.approve = false;
            $scope.isApprover = false;
            $scope.latest = false;
            $scope.openApprovalDetail = true;
            if(checkNotNullEmpty($scope.typeCount)) {
                $scope.openApprovalDetail = false;
            }

        };
        $scope.init();

        $scope.fetchApproverDetails = function () {
            $scope.loading = true;
            var orderId = undefined;
            if(checkNotNullEmpty($scope.order.id)) {
                orderId = $scope.order.id;
            } else if(checkNotNullEmpty($scope.order.orderId)) {
                orderId = $scope.order.orderId;
            }
            ordService.fetchPrimaryApprovers(orderId).then(function (data) {
                $scope.approval.approvers = data.data;
                $scope.checkApprover();
            }).catch(function (errorMsg) {
                $scope.showErrorMsg(errorMsg);
            }).finally(function () {
                $scope.loading = false;
            });
        };

        $scope.checkApprover = function () {
            if (checkNotNullEmpty($scope.approval.approvers)) {
                $scope.approval.approvers.forEach(function (data) {
                    if (data.user_id == $scope.curUser) {
                        $scope.isApprover = true;
                    }
                });
            } else if (checkNotNullEmpty($scope.order.approver) && $scope.approval.status.status == 'pn') {
                if ($scope.order.approver.aty == 0 && $scope.approval.active_approver_type == 'pr') {
                    $scope.isApprover = true;
                } else if ($scope.order.approver.aty == 1 && $scope.approval.active_approver_type == 'sc') {
                    $scope.isApprover = true;
                }
            }
            if ($scope.approval.approval_type == '1') {
                $scope.type = 'p';
            } else if ($scope.approval.approval_type == '2') {
                $scope.type = 's';
            } else if ($scope.approval.approval_type == '0') {
                $scope.type = 't';
            }
        };

        $scope.setMessageCount = function(count) {
            $scope.messageCnt = count;
        };

        $scope.fetchApproval = function () {
            if (checkNotNullEmpty($scope.id)) {
                $scope.loading = true;
                approvalService.fetchApproval($scope.id).then(function (data) {
                    $scope.approval = data.data;
                    $scope.checkApprover();
                    if ($scope.isApprover) {
                        if($scope.approval.status.status == 'pn') {
                            $scope.reject = true;
                            $scope.approve = true;
                        }
                    } else {
                        if($scope.approval.status.status == 'pn') {
                            $scope.cancelApproval = true;
                        }
                    }
                    if ($scope.approval.status.status == 'rj') {
                        $scope.rejected = true;
                        $scope.request = true;
                    } else if ($scope.approval.status.status == 'ap') {
                        $scope.approved = true;
                    } else if ($scope.approval.status.status == 'ex') {
                        $scope.expired = true;
                        $scope.request = true;
                    } else if($scope.approval.status.status == 'cn') {
                        $scope.cancelled = true;
                        $scope.request = true;
                    }
                    if($scope.request == true) {
                        $scope.fetchApproverDetails();
                    }
                    $scope.latest = $scope.approval.latest;
                }).catch(function (errorMsg) {
                    $scope.showErrorMsg(errorMsg);
                }).finally(function () {
                    $scope.loading = false;
                });
            } else {
                $scope.request = true;
                $scope.latest = true;
                $scope.fetchApproverDetails();
                if(checkNotNullEmpty($scope.order.approver)) {
                    $scope.isApprover = true;
                }
            }
        };
        $scope.fetchApproval();

        $scope.openApproval = function (data) {
            $scope.action = data;
            $scope.modalInstance = $uibModal.open({
                templateUrl: 'views/orders/approval-request.html',
                scope: $scope,
                keyboard: false,
                backdrop: 'static'
            });
        };

        $scope.requestApproval = function () {
            if (checkNotNullEmpty($scope.order)) {
                $scope.buildApprovalRequest();
                $scope.showLoading();
                approvalService.createApproval($scope.approval).then(function (data) {
                    $scope.orderApproval = data.data;
                    $scope.showSuccess("Approval created successfully");
                }).catch(function error(msg) {
                    $scope.showErrorMsg(msg);
                }).finally(function () {
                    if(checkNotNullEmpty($scope.order.id)) {
                        $scope.fetchOrder();
                    } else {
                        $scope.fetch();
                    }
                    $scope.hideLoading();
                })

            }
        };

        $scope.buildStatusApproval = function (msg, status) {
            $scope.approval = {};
            if (checkNotNullEmpty($scope.id)) {
                var status = {status: status, updated_by: $scope.curUser, message: msg};
                $scope.approval = {};
                $scope.approval = status;
            }
        };


        $scope.updateApprovalStatus = function (status) {
            if (checkNotNullEmpty($scope.approval) && checkNotNullEmpty(status)) {
                $scope.buildStatusApproval($scope.approval.msg, status);
                $scope.loading = true;
                approvalService.updateApprovalStatus($scope.id, $scope.approval).then(function (data) {
                    $scope.approval = data.data;
                }).catch(function error(msg) {
                    if (checkNotNullEmpty(msg.data.code) && msg.data.code == "AS007") {
                        var message = "";
                        if (status == APPROVAL.CANCELLED) {
                            message = $scope.resourceBundle['approval.invalid.transition.cancel']
                        } else if (status == APPROVAL.REJECTED) {
                            message = $scope.resourceBundle['approval.invalid.transition.reject']
                        } else if (status == APPROVAL.APPROVED) {
                            message = $scope.resourceBundle['approval.invalid.transition.approve']
                        }
                        msg.data.message = message + " " + $scope.resourceBundle['order.refresh']
                    }
                    $scope.showErrorMsg(msg);
                }).finally(function () {
                    if(checkNotNullEmpty($scope.order.id)) {
                        $scope.fetchOrder();
                    } else {
                        $scope.fetch();
                    }

                    $scope.loading = false;
                });
            }
        };

        $scope.buildApprovalRequest = function () {
            var msg = $scope.approval.msg;
            $scope.approval = {};
            if (checkNotNullEmpty($scope.order)) {
                var orderId = undefined;
                var type = undefined;
                if(checkNotNullEmpty($scope.order.id)) {
                    orderId = $scope.order.id;
                } else if(checkNotNullEmpty($scope.order.orderId)) {
                    orderId = $scope.order.orderId;
                }
                if(checkNotNullEmpty($scope.order.oty)){
                    type = $scope.order.oty;
                } else if(checkNotNullEmpty($scope.order.type)) {
                    type = $scope.order.type;
                }
                $scope.approval.order_id = orderId;
                $scope.approval.message = msg;
                $scope.approval.requester_id = $scope.curUser;
                $scope.approval.order_type = type;
                var type = 0;
                if ($scope.type == 'p') {
                    type = 1;
                } else if ($scope.type == 's') {
                    type = 2;
                } else {
                    type = 0;
                }
                $scope.approval.approval_type = type;
            }
        };

        $scope.proceed = function (status) {
            if (status == 'rq') {
                $scope.requestApproval();
            } else {
                if((status == 'cn' || status == 'rj') && checkNullEmpty($scope.approval.msg)) {
                    $scope.showWarning($scope.resourceBundle['comment.required']);
                    return;
                }
                $scope.updateApprovalStatus(status)
            }
            $scope.enableScroll();
            $scope.modalInstance.dismiss('cancel');
        };
        $scope.cancel = function () {
            $scope.approval.msg = "";
            $scope.action = "";
            $scope.enableScroll();
            $scope.modalInstance.dismiss('cancel');
        };
    }]);

approvalControllers.controller('ApprovalsCtrl', ['$scope', 'approvalService', 'ordService', 'userService', 'entityService', 'requestContext', '$location',
    function ($scope, approvalService, ordService, userService, entityService, requestContext, $location) {
        $scope.wparams = [["eid", "entity.id"], ["oid", "ordId"], ["rs", "reqStatus"], ["exp", "exp"], ["rt", "reqType"], ["req", "reqId"], ["apr", "aprId"], ["s", "size"], ["o", "offset"]];
        $scope.localFilters = ['entity', 'orderId', 'reqStatus', 'reqType', 'reqId', 'aprId', 'exp'];
        $scope.filterMethods = ['applyFilter'];
        ListingController.call(this, $scope, requestContext, $location);
        $scope.init = function (firstTimeInit) {
            $scope.ordApr = {entity: "", orderId: "", reqType: "", reqStatus: "", reqId: "", aprId: ""};
            $scope.today = formatDate2Url(new Date());
            if (firstTimeInit) {
                $scope.showMore = false;
            }
            $scope.approvals = [];
            $scope.showApproval = [];
            $scope.orderId = $scope.ordId = requestContext.getParam("oid") || "";
            $scope.reqStatus = requestContext.getParam("rs") || "";
            $scope.exp = requestContext.getParam("exp") || "";
            $scope.reqType = requestContext.getParam("rt");
            $scope.reqId = requestContext.getParam("req");
            $scope.aprId = requestContext.getParam("apr");
            var size = requestContext.getParam("s");
            if (size) {
                $scope.size = size;
            }
            var offset = requestContext.getParam("o");
            if (offset) {
                $scope.offset = offset;
            }
            if (checkNotNullEmpty(requestContext.getParam("eid"))) {
                if (checkNullEmpty($scope.entity) || $scope.entity.id != parseInt(requestContext.getParam("eid"))) {
                    $scope.entity = {id: parseInt(requestContext.getParam("eid")), nm: ""};
                }

            }
        };

        $scope.setActiveApprovers = function () {
            if (checkNotNullEmpty($scope.filtered)) {
                var expires_at = "";
                $scope.filtered.forEach(function (data) {
                    if (data.status.status == 'pn') {
                        if (checkNotNullEmpty(data.approvers)) {
                            var activeApprovers = [];
                            data.approvers.forEach(function (approver) {
                                if (approver.approver_status == 'ac') {
                                    activeApprovers.push(approver);
                                    if(checkNullEmpty(expires_at) && approver.user_id == $scope.curUser) {
                                        expires_at = approver.expires_at;
                                    }
                                }
                            });
                            data.approvers = activeApprovers;
                            if(checkNotNullEmpty(expires_at)) {
                                data.expires_at = expires_at;
                            }
                        }
                    }
                });
            }
        };


        $scope.fetch = function () {
            $scope.showLoading();
            approvalService.getApprovals($scope.offset, $scope.size, $scope.entity ? $scope.entity.id : undefined, $scope.ordId, $scope.reqStatus,
                $scope.exp, $scope.reqType, $scope.reqId, $scope.aprId, $scope.domainId).then(function (data) {
                    $scope.filtered = data.data.content;
                    $scope.setActiveApprovers();
                    $scope.setPagedResults(data.data);
                }).catch(function error(msg) {
                    $scope.showErrorMsg(msg);
                }).finally(function () {
                    $scope.hideLoading();
                    $scope.loading = false;
                })
        };
        $scope.init(true);
        $scope.fetch();
        $scope.getSuggestions = function (text, type) {
            if (checkNotNullEmpty(text)) {
                return ordService.getIdSuggestions(text, type, $scope.reqType).then(function (data) {
                    return data.data;
                }).catch(function (errorMsg) {
                    $scope.showErrorMsg(errorMsg);
                });
            }
        };
        $scope.goToRequester = function (requester) {
            if (checkNotNullEmpty(requester)) {
                $scope.tempReqId = requester;
            }
        };
        $scope.goToApprover = function (approver) {
            if (checkNotNullEmpty(approver)) {
                $scope.tempAprId = approver;
            }
        };
        $scope.showOrder = function (orderId) {
            if (checkNotNullEmpty(orderId)) {
                $scope.tempOrdId = orderId;
            }
        };

        $scope.applyFilter = function () {
            $scope.aprId = $scope.tempAprId;
            $scope.reqId = $scope.tempReqId;
            $scope.ordId = $scope.tempOrdId;
        };
        $scope.getFilteredApprovers = function (userId) {
            return ordService.fetchApprovers(userId).then(function (data) {
                return (checkNotNullEmpty(data.data) ? data.data : "")
            }).catch(function error(msg) {
                $scope.showErrorMsg(msg);
            });
        };
        $scope.getFilteredRequesters = function (userId) {
            return ordService.fetchRequesters(userId).then(function (data) {
                return (checkNotNullEmpty(data.data) ? data.data : "")
            }).catch(function error(msg) {
                $scope.showErrorMsg(msg);
            });
        };

        $scope.resetFilters = function () {
            $scope.entity = undefined;
            $scope.ordId = $scope.orderId = $scope.tempOrdId = undefined;
            $scope.reqStatus = "";
            $scope.exp = undefined;
            $scope.reqType = "";
            $scope.reqId = $scope.tempReqId = undefined;
            $scope.aprId = $scope.tempAprId = undefined;

        };

        $scope.toggle = function (index) {
            $scope.showApproval[index] = !$scope.showApproval[index];
        };

    }
]);


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

var entGrpControllers = angular.module('entGrpControllers', []);
entGrpControllers.controller('EntGrpCtrl', ['$scope', 'entGrpService', 'entityService', 'requestContext', '$location',
    function ($scope, entGrpService, entityService, requestContext, $location) {
        $scope.wparams = [ ["o", "offset"], ["s", "size"],["egid","entGrpId"]];
        $scope.entGrps;
        $scope.loading = false;

        $scope.init = function () {
            $scope.search = {};
            $scope.entGrpId = requestContext.getParam("egid") || "";
        };
        $scope.init();
        $scope.edit = false;

        ListingController.call(this, $scope, requestContext, $location);
        $scope.fetch = function () {
            $scope.loading = true;
            $scope.showLoading();
            entGrpService.getEntGrps($scope.offset, $scope.size).then(function (data) {
                $scope.entGrps = data.data;
                $scope.populateUsers();
                $scope.setResults($scope.entGrps);
            }).catch(function error(msg) {
                $scope.showErrorMsg(msg);
                $scope.setResults(null);
            }).finally(function (){
                $scope.loading = false;
                $scope.hideLoading();
            });
        };
        $scope.fetch();
        $scope.getFiltered = function () {
            var list = [];
            if ($scope.entGrps != null) {
                for (var item in $scope.entGrps.results) {
                    var entGrp = $scope.entGrps.results[item];
                    list.push(entGrp);
                }
            }
            $scope.filtered = list;
            return $scope.filtered;
        };
        $scope.selectAll = function (newval) {
            $scope.getFiltered();
            for (var item in $scope.filtered) {
                $scope.filtered[item]['selected'] = newval;
            }
        };
        $scope.populateUsers = function () {
            if ($scope.entGrps != null) {
                if (checkNotNullEmpty($scope.entGrps.uid)) {
                    $scope.entGrps.user = [];
                    $scope.entGrps.user[0] = $scope.entGrps.uid;
                }
            }
        };

        $scope.editPoolGroup = function (groupId) {
            if (checkNotNullEmpty(groupId)) {
                $scope.edit = !$scope.edit;
                $scope.groupId = groupId;
            }
            if(!$scope.edit){
                $scope.fetch(); //Reload form after update.
            }
        };
        $scope.deletePoolGroup = function () {
            if(!confirm($scope.resourceBundle['removepoolgroupconfirmmsg'] + '?')){
                return;
            }
            var poolGroups = "";
            $scope.getFiltered();
            if ($scope.filtered != null) {
                for (var item in $scope.filtered) {
                    if ($scope.filtered[item].selected) {
                        poolGroups = poolGroups.concat($scope.filtered[item].id).concat(',');
                    }
                }
                poolGroups = stripLastComma(poolGroups);
                if (poolGroups == "") {
                    $scope.showWarning($scope.resourceBundle['selectitemtoremovemsg']);
                } else {
                    entGrpService.deletePoolGroup(poolGroups).then(function (data) {
                        $scope.fetch();
                        $scope.success = true;
                        $scope.showSuccess(data.data);
                    }).catch(function error(msg) {
                        $scope.showErrorMsg(msg);
                    });
                }
            }
        };

    }
]);
entGrpControllers.controller('EntGrpEditCtrl', ['$scope', 'entGrpService','requestContext',
    function ($scope, entGrpService,requestContext) {
        $scope.uVisited = {};
        $scope.entGrps = {};
        $scope.cancel = false;
        $scope.loading = false;
        if(checkNullEmpty($scope.groupId)) {
            $scope.groupId = requestContext.getParam("egid");
        }
        $scope.init = function(edit){
            $scope.edit = edit;


        }
        $scope.createPoolGroup = function () {
            if ($scope.entGrps != null) {
                $scope.getUser();
                $scope.action = "add";
                $scope.loading = true;
                $scope.showLoading();
                entGrpService.setEntGrps($scope.entGrps, $scope.action).then(function (data) {
                    $scope.resetEntity();
                    $scope.showSuccess(data.data);
                }).catch(function error(msg) {
                    $scope.showErrorMsg(msg);
                }).finally(function (){
                    $scope.loading = false;
                    $scope.hideLoading();
                });
            }
        };
        $scope.getUser = function () {
            if ($scope.entGrps.user != null) {
                $scope.entGrps.uid = "";
                $scope.entGrps.uid = $scope.entGrps.user.id;
            }
        };
        $scope.setAllVisited = function(){
            $scope.uVisited = {};
            $scope.uVisited.entGrpNm=true;
            $scope.uVisited.entGrpOwner=true;
        };
        $scope.resetEntity = function () {
            $scope.uVisited = {};
            $scope.entGrps = {};
        };

        $scope.validate = function() {
            if(checkNotNullEmpty($scope.entGrps.nm) && checkNotNullEmpty($scope.entGrps.user)){
                return true;
            }
            $scope.showFormError();
            return false;
        };

        $scope.setUserVisited = function() {
            $scope.uVisited.entGrpOwner = true;
        };
        $scope.getPoolGroup = function () {
            $scope.loading = true;
            $scope.showLoading();
            entGrpService.getPoolGroup($scope.groupId).then(function (data) {
                $scope.entGrps = data.data;
                $scope.setUserId();
                $scope.setEntities();
                $scope.edit = false;
            }).catch(function error(msg) {
                $scope.showErrorMsg(msg);
            }).finally(function (){
                $scope.loading = false;
                $scope.hideLoading();
            });
        };
        if(checkNotNullEmpty($scope.groupId)) {
            $scope.getPoolGroup();
        }
        $scope.setUserId = function () {
            if ($scope.entGrps != null) {
                if (checkNotNullEmpty($scope.entGrps.uid)) {
                    $scope.entGrps.user = {'text': $scope.entGrps.uid, 'id': $scope.entGrps.uid};
                }
            }
        };
        $scope.setEntities = function () {
            if ($scope.entGrps != null) {
                if ($scope.entGrps.ent != null) {
                    $scope.entGrps.entities = $scope.entGrps.ent;
                    $scope.entGrps.ent = [];
                    for (var i = 0; i < $scope.entGrps.entities.length; i++) {
                        $scope.entGrps.ent[i] = {
                            'text': $scope.entGrps.entities[i].nm,
                            'id': $scope.entGrps.entities[i].id
                        };
                    }

                }
            }
        };
        $scope.updatePoolGroup = function () {
            if ($scope.entGrps != null) {
                $scope.getUser();
                $scope.action = "update";
                $scope.loading = true;
                $scope.showLoading();
                entGrpService.setEntGrps($scope.entGrps, $scope.action).then(function (data) {
                    $scope.cancelEditing();
                    $scope.success = data.data;
                    $scope.showSuccess(data.data);
                }).catch(function error(msg) {
                    $scope.showErrorMsg(msg);
                }).finally(function (){
                    $scope.loading = false;
                    $scope.hideLoading();
                });
            }
        };
        $scope.cancelEditing = function () {
            $scope.editPoolGroup($scope.groupId);//clear form
        };

        $scope.back = function(){
            $scope.resetFilters();
        }
    }
]);
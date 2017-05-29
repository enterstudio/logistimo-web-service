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

/**
 * Created by mohan on 02/12/14.
 */
var actControllers = angular.module('actControllers', []);
actControllers.controller('AccountsListController', ['$scope', 'actServices', 'entityService', 'requestContext', '$location',
    function ($scope, actServices, entityService, requestContext, $location) {
        $scope.wparams = [["eid", "entity.id"], ["year", "yr"], ["sb", "sb"], ["o", "offset"], ["s", "size"]];
        ListingController.call(this, $scope, requestContext, $location);
        $scope.init = function (firstTimeInit) {
            if (checkNotNullEmpty(requestContext.getParam("eid"))) {
                if (checkNullEmpty($scope.entity) || $scope.entity.id != parseInt(requestContext.getParam("eid"))) {
                    $scope.entity = {id: parseInt(requestContext.getParam("eid")), nm: ""};
                }
            }else{
                if(firstTimeInit && checkNotNullEmpty($scope.defaultEntityId)){
                    $scope.entity = {id: $scope.defaultEntityId, nm: ""};
                }else{
                    $scope.entity = null;
                }
            }
            if(!firstTimeInit) {
                if (checkNotNullEmpty(requestContext.getParam("year"))) {
                    $scope.yr = parseInt(requestContext.getParam("year"));
                } else {
                    $scope.yr = null;
                }
                if (checkNotNullEmpty(requestContext.getParam("sb"))) {
                    $scope.sb = requestContext.getParam("sb");
                } else {
                    $scope.sb = null;
                }
            }
            $scope.loading = true;
        };

        $scope.showLoading();
        $scope.fetch = function () {
            $scope.noDataFound = true;
            $scope.actDet = {};
            if (checkNotNullEmpty($scope.entity)) {
                $scope.loading = true;
                $scope.showLoading();
                actServices.getActDetails($scope.entity.id, $scope.yr, $scope.subview, $scope.sb, $scope.offset, $scope.size).then(function (data) {
                    $scope.actDet = data.data.results;
                    if (checkNotNullEmpty($scope.actDet)) {
                        $scope.noDataFound = false;
                        $scope.tot = 0;
                        $scope.actDet.forEach(function (det) {
                            if(!isNaN(det.npay)) {
                                $scope.tot = parseFloat($scope.tot) + parseFloat(det.npay);
                            }
                        });
                    }
                    $scope.setResults(data.data);
                }).catch(function error(msg) {
                    $scope.showErrorMsg(msg);
                }).finally(function(){
                    $scope.loading = false;
                    $scope.hideLoading();
                });
            }
        };
        actServices.getAccountConfig().then(function (data) {
            $scope.act = data.data;
            if (checkNullEmpty($scope.yr) && checkNotNullEmpty($scope.act)) {
                $scope.yr = $scope.act.curyear;
                $scope.sb = 'pyb';
            }
            $scope.init(true);
            $scope.fetch();
        }).catch(function error(msg) {
            $scope.showErrorMsg(msg);
        }).finally(function(){
            $scope.loading = false;
            $scope.hideLoading();
        });
        $scope.resetFilters = function(){
            $scope.entity = "";
            $scope.yr = $scope.act.curyear;
            $scope.sb = 'pyb';
        }
    }
]);
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
 * Created by yuvaraj on 12/07/16.
 */
var handlingUnitControllers = angular.module('handlingUnitControllers', []);

handlingUnitControllers.controller('AddHUController', ['$scope', 'handlingUnitService', 'matService', 'requestContext',
    function ($scope, handlingUnitService, matService, requestContext) {
        $scope.init = function () {
            $scope.hu = {};
            $scope.huId = requestContext.getParam("handlingUnitId");

            if (checkNotNullEmpty($scope.huId)) {
                $scope.showLoading();
                handlingUnitService.getDetail($scope.huId).then(function (data) {
                    $scope.hu = data.data;
                    $scope.qty = $scope.hu.contents[0].quantity ? $scope.hu.contents[0].quantity : '';
                    $scope.materials = {mId: $scope.hu.contents[0].cntId, mnm: $scope.hu.contents[0].cntName};
                }).catch(function error(msg) {
                    $scope.showErrorMsg(msg);
                }).finally(function () {
                    $scope.hideLoading();
                });
            }
        };
        $scope.init();

        function validate() {
            if (checkNullEmpty($scope.hu.name)) {
                $scope.showWarning("Name is mandatory");
                return false;
            } else if (checkNullEmptyObject($scope.materials)) {
                $scope.showWarning("Contents is mandatory");
                return false;
            } else if (checkNullEmpty($scope.qty)) {
                $scope.showWarning("Quantity is mandatory");
                return false;
            }
            return true;
        }

        $scope.createHandlingUnit = function () {
            if (validate()) {
                $scope.showLoading();
                $scope.populateContents();
                handlingUnitService.create($scope.hu).then(function (data) {
                    $scope.showSuccess(data.data);
                    reset();
                }).catch(function error(msg) {
                    $scope.showErrorMsg(msg);
                }).finally(function () {
                    $scope.hideLoading();
                });
            }
        };

        function reset() {
            $scope.hu = {};
            $scope.materials = undefined;
            $scope.qty = undefined;
        }

        $scope.populateContents = function () {
            $scope.hu.contents = [];
            if (checkNotNullEmpty($scope.materials)) {
                $scope.contentModel = {};
                $scope.contentModel.id = $scope.materials.mId;
                $scope.contentModel.cntId = $scope.materials.mId;
                $scope.contentModel.dId = $scope.materials.dId;
                $scope.contentModel.quantity = $scope.qty;
                $scope.contentModel.ty = 0; // todo: material:0. Need to do handling unit:1
                $scope.hu.contents.push($scope.contentModel);
            }
        };

        $scope.updateHandlingUnit = function () {
            if (validate()) {
                $scope.showLoading();
                $scope.populateContents();
                handlingUnitService.update($scope.hu).then(function (data) {
                    $scope.$back();
                    $scope.showSuccess(data.data);
                }).catch(function error(msg) {
                    $scope.showErrorMsg(msg);
                }).finally(function () {
                    $scope.hideLoading();
                });
            }
        };

        $scope.getFilteredMaterial = function (query) {
            return matService.getDomainMaterials(query, undefined, undefined, undefined, undefined, true).then(function (data) {
                var list = [];
                for (var j in data.data.results) {
                    list.push(data.data.results[j]);
                }
                return list;
            })
        }
    }
]);

handlingUnitControllers.controller('HUListController', ['$scope', 'handlingUnitService', 'requestContext', '$location',
    function ($scope, handlingUnitService, requestContext, $location) {
        $scope.wparams = [["search", "search.mnm"]];
        ListingController.call(this, $scope, requestContext, $location);
        $scope.init = function () {
            $scope.search = {};
            $scope.search.mnm = requestContext.getParam("search") || "";
            $scope.search.key = $scope.search.mnm;
        };
        $scope.init();
        $scope.fetch = function () {
            $scope.loading = true;
            $scope.showLoading();
            handlingUnitService.getAll($scope.offset, $scope.size, $scope.search.mnm).then(function (data) {
                $scope.huList = data.data;
                $scope.setResults(data.data);
                $scope.noData = checkNullEmpty(data.data.results);
            }).catch(function error(msg) {
                $scope.showErrorMsg(msg);
                $scope.setResults(null);
            }).finally(function () {
                $scope.loading = false;
                $scope.hideLoading();
            });
        };
        $scope.fetch();
        $scope.searchHU = function () {
            if ($scope.search.mnm != $scope.search.key) {
                $scope.search.mnm = $scope.search.key;
            }
        };
        $scope.reset = function () {
            $scope.search = {};
            $scope.search.key = $scope.search.mnm = "";
        };
    }
]);

handlingUnitControllers.controller('HUDetailController', ['$scope', 'handlingUnitService', 'requestContext',
    function ($scope, handlingUnitService, requestContext) {
        $scope.init = function () {
            $scope.huDetail = undefined;
            $scope.huId = requestContext.getParam("handlingUnitId");

            $scope.loading = true;
            $scope.showLoading();
            handlingUnitService.getDetail($scope.huId).then(function (data) {
                $scope.huDetail = data.data;
            }).catch(function error(msg) {
                $scope.showErrorMsg(msg);
            }).finally(function () {
                $scope.loading = false;
                $scope.hideLoading();
            });
        };
        $scope.init();
    }
]);
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
 * @author Mohan Raja
 */
var widgetControllers = angular.module('widgetControllers', []);
widgetControllers.controller('AddWidgetController', ['$scope', 'widgetService',
    function ($scope, widgetService) {
        $scope.wDesc = '';
        $scope.create = function () {
            if (checkNullEmpty($scope.wid.nm)) {
                $scope.showWarning("Widget Name cannot be blank.");
                return;
            }
            $scope.showLoading();
            var data = {nm: $scope.wid.nm, desc: $scope.wid.desc, config: $scope.wid};
            widgetService.create(data).then(function (data) {
                $scope.showSuccess(data.data);
                $scope.resetWidgetConfig();
                $scope.wid.nm = '';
                $scope.wid.desc = '';
            }).catch(function error(msg) {
                $scope.showErrorMsg(msg);
            }).finally(function () {
                $scope.hideLoading();
            });
        };
        WidgetConfigController.call(this,$scope);
    }
]);

widgetControllers.controller('EditWidgetController', ['$scope', 'widgetService', 'requestContext',
    function ($scope, widgetService, requestContext) {
        $scope.wId = requestContext.getParam("wid");
        $scope.showLoading();
        widgetService.get($scope.wId).then(function (data) {
            $scope.wid = data.data;
        }).catch(function error(msg) {
            $scope.showErrorMsg(msg);
        }).finally(function () {
            $scope.hideLoading();
        });

        $scope.setName = function () {
            $scope.descedit = false;
            $scope.enm = $scope.wid.nm;
            $scope.nmedit = true;
        };
        $scope.updateName = function () {
            $scope.nmedit = false;
            $scope.updatingName = true;
            var data = {ty: 'nm', id: $scope.wid.wId, val: $scope.enm};
            widgetService.update(data).then(function () {
                $scope.wid.nm = $scope.enm;
            }).catch(function error(msg) {
                $scope.showErrorMsg(msg);
            }).finally(function () {
                $scope.updatingName = false;
            });
        };
        $scope.cancelName = function () {
            $scope.nmedit = false;
        };
        $scope.setDesc = function () {
            $scope.nmedit = false;
            $scope.edesc = $scope.wid.desc;
            $scope.descedit = true;
        };
        $scope.updateDesc = function () {
            $scope.descedit = false;
            $scope.updatingDesc = true;
            var data = {ty: 'desc', id: $scope.wid.wId, val: $scope.edesc};
            widgetService.update(data).then(function () {
                $scope.wid.desc = $scope.edesc;
            }).catch(function error(msg) {
                $scope.showErrorMsg(msg);
            }).finally(function () {
                $scope.updatingDesc = false;
            });
        };
        $scope.cancelDesc = function () {
            $scope.descedit = false;
        };
    }
]);

widgetControllers.controller('ConfigWidgetController', ['$scope', 'widgetService', 'requestContext',
    function ($scope, widgetService, requestContext) {
        $scope.widId = requestContext.getParam("wid");
        WidgetConfigController.call(this,$scope);
        $scope.showLoading();
        widgetService.getConfig($scope.widId).then(function (data) {
            $scope.wid = data.data;
            if ($scope.wid.nop == 0) {
                $scope.wid.nop = 6;
            }
            if (checkNullEmpty($scope.wid.fq)) {
                $scope.wid.fq = 'd';
            }
            if (checkNotNullEmpty($scope.wid.agTy)) {
                $scope.setAggregations($scope.wid.agTy, true);
            }
            $scope.setChartTypeOptions('a', true);
        }).catch(function error(msg) {
            $scope.showErrorMsg(msg);
        }).finally(function () {
            $scope.hideLoading();
        });

        $scope.saveWidgetConfig = function () {
            $scope.showLoading();
            widgetService.saveConfig($scope.wid).then(function (data) {
                $scope.showSuccess(data.data);
            }).catch(function error(msg) {
                $scope.showErrorMsg(msg);
            }).finally(function () {
                $scope.hideLoading();
            });
        };

        $scope.resetPeriod = function(){
            $scope.wid.nop = 6;
        }
    }
]);
widgetControllers.controller('ViewWidgetController', ['$scope', 'widgetService',
    function ($scope, widgetService) {
        $scope.showLoading();
        $scope.loading = true;
        widgetService.getData($scope.wData.wid).then(function (data) {
            $scope.wid = data.data;
            $scope.wid.copt = JSON.parse($scope.wid.opt);
            $scope.wid.cType = $scope.wid.ty;
            $scope.wid.cdata = getFCData($scope.wid.data, 0);
            $scope.wid.wd = $scope.wData.cx - 12;
            $scope.wid.ht = $scope.wData.cy - 2;
        }).catch(function error(msg) {
            $scope.showErrorMsg(msg);
        }).finally(function () {
            $scope.hideLoading();
            $scope.loading = false;
        });
    }
]);
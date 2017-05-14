
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
 * Created by vani on 27/10/15.
 */
var exportControllers = angular.module('exportControllers', []);
exportControllers.controller('ExportListController', ['$scope', '$location', 'exportService', 'domainCfgService', 'requestContext',
    function ($scope, $location, exportService, domainCfgService, requestContext) {
        ListingController.call(this, $scope, requestContext, $location);
        $scope.init = function(type){
            $scope.type = type;
            if ($scope.type=='customreport')
                $scope.sae=true;
            else
                $scope.sae = false; // Show all exports in the domain

            $scope.fetch($scope.sae);
        };
        $scope.fetch = function (allExports) {
            $scope.loading = true;
            $scope.fetchSize = 30;
            $scope.showLoading();
            $scope.allExports = allExports;
            exportService.getExportJobList($scope.offset, $scope.fetchSize, $scope.type, $scope.allExports).then(function (data) {
                $scope.exportJobs = data.data;
                $scope.setResults($scope.exportJobs);
                $scope.hideLoading();
                $scope.noExportJobs = checkNullEmpty($scope.exportJobs.results);
                fixTable();
            }).catch(function error(msg) {
                $scope.showErrorMsg(msg);
                $scope.setResults(null);
            }).finally(function(){
                $scope.loading = false;
                $scope.hideLoading();
            });
        };
        $scope.getCustomReportTemplateNames = function() {
            domainCfgService.getCustomReports().then(function (data) {
                $scope.crp = data.data.config;
                if ($scope.crp != null && $scope.crp.length > 0) {
                    $scope.crt = true;
                }else{
                    $scope.crt = false;
                }
            });
        };
        $scope.showAllExportsInDomain = function() {
            $scope.fetch($scope.sae);
        };
    }
]);
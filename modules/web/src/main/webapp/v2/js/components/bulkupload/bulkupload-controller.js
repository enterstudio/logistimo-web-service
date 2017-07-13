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
 * Created by mohan raja on 08/01/15.
 */

var blkUpControllers = angular.module('blkUpControllers', []);

blkUpControllers.controller('BulkUploadController',['$scope','blkUpService','exportService',
    function($scope,blkUpService,exportService){
        $scope.fileData = '';
        if($scope.uploadType == 'users'){
            $scope.helpFile = 'uploadusersinbulk.htm';
        }else if($scope.uploadType == 'kiosks'){
            $scope.helpFile = 'uploadentitiesinbulk.htm';
        }else if($scope.uploadType == 'materials'){
            $scope.helpFile = 'uploadmaterialsinbulk.htm';
        }
        $scope.uploadURL = function() {
            $scope.uploading = true;
            blkUpService.uploadURL($scope.us.ty).then(function(data) {
                $scope.actionURL = cleanupString(data.data.toString().replace(/"/g,''));
                $scope.uploading = false;
                $scope.urlLoaded = true;
            }).catch(function error(msg) {
                $scope.showErrorMsg(msg);
            });
        };

        $scope.uploadStatus = function(initCall) {
            $scope.loading = true;
            $scope.serr = false;
            $scope.showLoading();
            blkUpService.uploadStatus($scope.uploadType).then(function (data) {
                $scope.us = data.data;
                if(initCall){
                    $scope.uploadURL();
                }
                $scope.fileData = '';
            }).catch(function error(msg) {
                $scope.showErrorMsg(msg);
            }).finally(function(){
                $scope.loading = false;
                $scope.hideLoading();
            });
        };
        $scope.uploadStatus(true);

        $scope.viewErrors = function() {
            $scope.serr = !$scope.serr;
        };

        /*$scope.downloadFile = function(key) {
            $scope.downloading = true;
            exportService.downloadFile(key).then(function(data) {
                $scope.actionURL = data.data;
            });
        };*/
        $scope.uploadPostUrl = function(){
            if($scope.fileData.size > 10 * 1024 * 1024 ) { // Max file size to upload 10 MB
                $scope.showWarning("Maximum file size is 10MB.");
                return;
            }
            var uploadName = "";
            $scope.showLoading();
            blkUpService.uploadPostUrl($scope.actionURL,$scope.fileData, $scope.us.ty).then(function(data){
                $scope.uploadStatus();
                $scope.uploadURL();
                if($scope.uploadType == 'kiosks') {
                    uploadName = $scope.resourceBundle['kiosks.lowercase'];
                } else {
                    uploadName = $scope.uploadType;
                }
                $scope.showSuccess($scope.resourceBundle['bulkupload.successfullyscheduled'] + " " + uploadName);
            }).catch(function error(msg) {
                $scope.showErrorMsg(msg);
            }).finally(function(){
                $scope.hideLoading();
            });
        };

        $scope.validate = function(){
            if(checkNullEmpty($scope.fileData)) {
                $scope.showWarning($scope.resourceBundle['upload.csvdata']);
                return false;
            }
            var filename = $scope.fileData.name.split(".");
            var ext = filename[filename.length - 1];
            if(ext != 'csv'){
                $scope.showWarning($scope.resourceBundle['upload.csvdata']);
                return false;
            }
            $scope.uploadTypeMessage = "";
            if($scope.uploadType == 'kiosks'){
                $scope.uploadTypeMessage = $scope.resourceBundle['kiosks.lowercase'];
            }else{
                $scope.uploadTypeMessage = $scope.uploadType;
            }
            if (!confirm($scope.resourceBundle['bulkupload.containcsvdata'] + ' ' + $scope.uploadTypeMessage + '?')) {
                return;
            }
            return true;
        }
    }
]);

blkUpControllers.controller('CheckUploadController',['$scope','blkUpService',function($scope,blkUpService){
    blkUpService.getManualUploadStatus().then(function(data){
        $scope.manualUpload = data.data;
    }).catch(function error(msg) {
        $scope.showErrorMsg(msg);
    });
}]);

blkUpControllers.controller('ViewBulkUploadController', ['$scope','requestContext','$location','blkUpService','exportService',
    function ($scope,requestContext,$location,blkUpService,exportService) {
        $scope.wparams = [
            ["from", "from","",formatDate2Url],
            ["to", "to","",formatDate2Url],
            ["eid", "entity.id"],
            ["o", "offset"],
            ["s", "size"]
        ];
        $scope.localFilters = ['entity', 'from', 'to'];
        $scope.init = function () {
            $scope.from = parseUrlDate(requestContext.getParam("from")) || "";
            $scope.to = parseUrlDate(requestContext.getParam("to")) || "";
            if (checkNotNullEmpty(requestContext.getParam("eid"))) {
                if(checkNullEmpty($scope.entity) || $scope.entity.id != parseInt(requestContext.getParam("eid"))) {
                    $scope.entity = {id: parseInt(requestContext.getParam("eid")), nm: ""};
                }
            }else{
                $scope.entity = null;
            }
        };
        $scope.init();
        ListingController.call(this, $scope, requestContext, $location);
        $scope.fetch = function () {
            $scope.loading = true;
            $scope.showLoading();
            blkUpService.getUploadTransactions(checkNotNullEmpty($scope.entity)?$scope.entity.id:"", formatDate($scope.from), formatDate($scope.to), $scope.offset, $scope.size).then(function (data) {
                $scope.filtered = data.data.results;
                $scope.setResults(data.data);
            }).catch(function error(msg) {
                $scope.showErrorMsg(msg);
                $scope.filtered = null;
                $scope.setResults(null);
            }).finally(function (){
                $scope.loading = false;
                $scope.hideLoading();
            });
        };
        $scope.fetch();
        $scope.resetFilters = function(){
            $location.$$search = {};
            $location.$$compose();
        };
    }
]);
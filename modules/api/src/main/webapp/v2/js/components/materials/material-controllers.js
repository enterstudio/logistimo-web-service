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

var matControllers = angular.module('matControllers', []);
matControllers.controller('MaterialDetailMenuController', ['$scope', 'matService', 'mediaService','requestContext','$timeout',
    function ($scope, matService,mediaService, requestContext, $timeout) {
        $scope.materialId = requestContext.getParam("materialId");
        $scope.material;
        $scope.edit = false;
        $scope.loading = true;
        $scope.loadimage = true;
        $scope.showLoading();
        $scope.materialImages = [];
        $scope.imageData='';
        $scope.doAddImage=false;
        $scope.ext='';
        matService.get($scope.materialId).then(function (data) {
            $scope.batchEnabled = "Disabled";
            $scope.material = data.data;
            if(checkNotNullEmpty($scope.material.b)){
                if($scope.material.b == "yes"){
                    $scope.batchEnabled = "Enabled";
                }else{
                    $scope.batchEnabled = "Disabled";
                }
            }
            if (checkNotNullEmpty($scope.material.tgs)) {
                $scope.material.tgObjs = [];
                for (var i = 0; i < $scope.material.tgs.length; i++) {
                    $scope.material.tgObjs.push({"id": $scope.material.tgs[i], "text": $scope.material.tgs[i]});
                }
            }
            $scope.getDomainKeyImages($scope.materialId);
        }).catch(function error(msg) {
            $scope.showErrorMsg(msg);
        }).finally(function (){
            $scope.loading = false;
            $scope.hideLoading();
        });


        $scope.isNotNullEmpty = function (val) {
            return typeof val !== 'undefined' && val != null && val != "";
        };
        $scope.editMaterial = function () {
            $scope.edit = true;
        };

        $scope.getDomainKeyImages = function(materialId){
            $scope.loadimage = true;
            mediaService.getDomainkeyImages(materialId).then(function(data) {
                if (checkNotNullEmpty(data.data)) {
                    $scope.materialImages = data.data.items;
                    $scope.doAddImage = false;
                }
            }).catch(function error(msg) {
                $scope.showErrorMsg(msg);
            }).finally(function(){
                $timeout(function(){
                    $scope.loadimage = false;
                },2000);
            });
        };

        $scope.validateImage =function(){
            if(checkNullEmpty($scope.imageData)){
                $scope.showWarning($scope.resourceBundle['image.upload.empty.warning']);
                return false;
            }
            var filetype = $scope.imageData.filetype.split("/");
            $scope.ext=filetype[filetype.length - 1];
            if($scope.ext !='png' && $scope.ext !='jpg' && $scope.ext !='jpeg'){
                $scope.showWarning($scope.resourceBundle['image.upload.warning']);
                return false;
            }

            var size = $scope.imageData.filesize;
            if(size > 5000000){
                $scope.showWarning ($scope.resourceBundle['uploadsizemessage']);
                return false;
            }
            return true;
        };
        $scope.uploadImage = function(){
            $scope.showLoading();
            mediaService.uploadImage($scope.ext,$scope.materialId,$scope.imageData.base64).then(function(){
                    $scope.showSuccess($scope.resourceBundle['image.upload.success']);
                    $scope.getDomainKeyImages($scope.materialId);
                    angular.element('#matFileupload').val(null);
            }).catch(function error(msg){
                $scope.showErrorMsg(msg)
            }).finally(function(){
                $scope.imageData='';
                $scope.hideLoading();
            });
        };
        $scope.removeImage = function(id){
            if (!confirm($scope.resourceBundle['image.removed.warning'] + "?")) {
                return;
            }
            $scope.showLoading();
            mediaService.removeImage(id).then(function(){
                $scope.showSuccess($scope.resourceBundle['image.delete.success']);
                $scope.getDomainKeyImages($scope.materialId);
            }).catch(function error(msg){
                $scope.showErrorMsg(msg)
            }).finally(function(){
                $scope.hideLoading();
            });

        };
        $scope.addImage = function(){
            if(!(checkNullEmpty($scope.materialImages) || $scope.materialImages.length ==0 )){
                if($scope.materialImages.length >=5)
                {
                    $scope.showWarning($scope.resourceBundle['image.upload.maximum.images']);
                    $scope.doAddImage=false;
                    return;
                }
            }
            $scope.doAddImage=true;
        };
        $scope.cancel = function(){
            $scope.doAddImage=false;
            $scope.imageData='';
        };
    }
]);
matControllers.controller('MaterialListController', ['$scope', 'matService', 'domainCfgService', 'requestContext', '$location', 'exportService',
    function ($scope, matService, domainCfgService, requestContext, $location, exportService) {
        $scope.wparams = [["mtag", "mtag"], ["search", "search.mnm"]];
        $scope.selectAll = function (newval) {
            for (var item in $scope.filtered) {
                $scope.filtered[item]['selected'] = newval;
            }
        };

        $scope.descText = "This vial handling unit contains 10 units of BCG (dose)";

        $scope.init = function () {
            $scope.materials = {};
            $scope.search = {};
            $scope.mtag = requestContext.getParam("mtag") || "";
            $scope.search.mnm = requestContext.getParam("search") || "";
            $scope.search.key = $scope.search.mnm;
            $scope.selAll = false;
        };
        $scope.init();
        ListingController.call(this, $scope, requestContext, $location);
        $scope.matListType = $scope.matListType || "all";
        $scope.filtered = {};
        $scope.entityMetadata = [{'title': 'Sl.No.', 'field': 'sno'}, {'title': 'Material', 'field': 'mnm'},
            {'title': 'Min', 'field': 'reord'}, {'title': "Max", 'field': 'max'},
            {'title': 'Consumption Rates [Daily]', 'field': 'crMnl'}, {'title': "Retailer's Price", 'field': 'rp'},
            {'title': 'Tax', 'field': 'tx'}, {'title': 'Temperature Min.', 'field': 'tmn'},
            {'title': 'Temperature Max', 'field': 'tmx'}];
        $scope.metadata = [{'title': 'Sl.No.', 'field': 'sno'}, {'title': 'Material', 'field': 'mnm'},
            {'title': 'Description', 'field': 'dsc'}, {'title': "Tags", 'field': 'tgs'},
            {'title': 'Retailer\'s price', 'field': 'rp'}, {'title': "MSRP", 'field': 'msrp'},
            {'title': 'Batch', 'field': 'b'},
            {'title': 'Last Updated', 'field': 't'}];
        $scope.memetadata = [{'title': 'Sl.No.', 'field': 'sno'}, {'title': 'Material', 'field': 'mnm'},
            {'title': 'Min', 'field': 'min'}, {'title': 'Max', 'field': 'max'},
            { 'title': "Retailer's Price", 'filed': 'rp'}];
        $scope.fetch = function () {
            $scope.defaultCur = "";
            $scope.loading = true;
            $scope.showLoading();
            matService.getDomainMaterials($scope.search.mnm, $scope.mtag, $scope.offset, $scope.size).then(function (data) {
                $scope.materials = data.data.results;
                $scope.setResults(data.data);
                if(checkNotNullEmpty($scope.defaultCurrency)){
                    $scope.defaultCur = $scope.defaultCurrency;
                }
                $scope.loading = false;
                $scope.hideLoading();
                fixTable();
                $scope.noMaterialsFound = checkNullEmpty($scope.materials);
            }).catch(function error(msg) {
                $scope.setResults(null);
                $scope.showErrorMsg(msg);
            }).finally(function (){
                $scope.loading = false;
                $scope.hideLoading();
            });
        };
        $scope.fetch();

        $scope.deleteMaterials = function () {
            var materials = "";
            for (var item in $scope.filtered) {
                if ($scope.filtered[item].selected) {
                    if($scope.currentDomain == $scope.filtered[item].sdid ){
                        materials = materials.concat($scope.filtered[item].mId).concat(',');
                    }
                }
            }
            materials = stripLastComma(materials);
            if (materials == "") {
                $scope.showWarning($scope.resourceBundle['selectitemtoremovemsg']);
            } else {
                if (!confirm($scope.resourceBundle.removematerialconfirmmsg)) {
                    return;
                }
                matService.deleteMaterials(materials).then(function (data) {
                    $scope.selAll = false;
                    $scope.showSuccess(data.data);
                    $scope.fetch();
                }).catch(function error(msg) {
                    $scope.showErrorMsg(msg);
                });
            }
        };
        $scope.searchMaterial = function () {
            if($scope.search.mnm != $scope.search.key){
                $scope.search.mnm = $scope.search.key;
            }
        };
        $scope.reset = function() {
            $scope.search = {};
            $scope.search.key = $scope.search.mnm = "";
            $scope.mtag = "";
        };
    }
]);
matControllers.controller('AddMatController', ['$scope', '$route', 'matService', 'domainCfgService', 'configService', 'requestContext','$uibModal',
    function ($scope, $route, matService, domainCfgService, configService, requestContext,$uibModal) {
        $scope.material = {};
        $scope.material.dispInfo = "yes";
        $scope.vErrors = {};
        $scope.isMatCollapsed = false;
        $scope.isPriCollapsed = false;
        $scope.isTempCollapsed = false;
        $scope.loading = false;
        $scope.edit = false;
        $scope.editMaterialId = requestContext.getParam("mid");
        $scope.metadata = [{'title': 'Sl.No.', 'field': 'sno'}, {'title': 'Material', 'field': 'mnm'},
            {'title': 'Description', 'field': 'dsc'}, {'title': "Tags", 'field': 'tgs'},
            {'title': 'Retailer\'s price', 'field': 'rp'}, {'title': "MSRP", 'field': 'msrp'},
            {'title': 'Batch', 'field': 'b'}, {'title': 'Actions'},
            {'title': 'Status'}];

        $scope.updateTags = function (action) {
            if ($scope.material != null) {
                if(action == 'add' || action == 'update'){
                    $scope.material.tgs = [];
                    if(checkNotNullEmpty($scope.material.tgObjs)){
                        for (var i = 0; i < $scope.material.tgObjs.length; i++) {
                            $scope.material.tgs.push($scope.material.tgObjs[i].text);
                        }
                    }
                } else if(action == 'fetch'){
                    $scope.material.tgObjs = [];
                    if(checkNotNullEmpty($scope.material.tgs)){
                        for (i = 0; i < $scope.material.tgs.length; i++) {
                            $scope.material.tgObjs.push({"id": $scope.material.tgs[i], "text": $scope.material.tgs[i]});
                        }
                    }
                }
            }
        };
        $scope.createMaterial = function () {
            if ($scope.material != null) {
                if($scope.material.tm == "yes"){
                    if(checkNullEmpty($scope.material.tmin) || checkNullEmpty($scope.material.tmax)){
                        $scope.showWarning($scope.resourceBundle['temperature.invalid']);
                        return;
                    }else if($scope.material.tmin == $scope.material.tmax){
                        $scope.showWarning($scope.resourceBundle['temperature.equal']);
                        return;
                    }else if($scope.material.tmin > $scope.material.tmax){
                        $scope.showWarning($scope.resourceBundle['temperature.mgm']);
                        return;
                    }
                }
                $scope.updateTags("add");
                $scope.loading = true;
                $scope.showLoading();
                matService.createMaterial($scope.material).then(function (data) {
                    $scope.resetMaterial();
                    $scope.showSuccess(data.data);
                }).catch(function error(msg) {
                    $scope.showErrorMsg(msg);
                }).finally(function (){
                    $scope.loading = false;
                    $scope.hideLoading();
                });
            }
        };
        $scope.updateMaterial = function () {
            if ($scope.material != null) {
                if($scope.material.tm == "yes"){
                    if(checkNullEmpty($scope.material.tmin) || checkNullEmpty($scope.material.tmax)){
                        $scope.showWarning($scope.resourceBundle['temperature.invalid']);
                        return;
                    }else if( parseInt($scope.material.tmin) === parseInt($scope.material.tmax)){
                        $scope.showWarning($scope.resourceBundle['temperature.equal']);
                        return;
                    }else if(parseInt($scope.material.tmin)  > parseInt($scope.material.tmax)){
                        $scope.showWarning($scope.resourceBundle['temperature.mgm']);
                        return;
                    }
                }else{
                    $scope.material.tmin = 0;
                    $scope.material.tmax = 0;
                }
                if(checkNullEmpty($scope.material.rp)) {
                    $scope.material.rp = 0;
                }
                if(checkNullEmpty($scope.material.msrp)) {
                    $scope.material.msrp = 0;
                }
                $scope.updateTags("update");
                $scope.loading = true;
                $scope.showLoading();
                matService.update($scope.material).then(function (data) {
                    if (checkNotNullEmpty(data.data)) {
                        $scope.$back();
                        $scope.showSuccess(data.data);
                    } else {
                        var stockViewsUrl = "#/inventory/?mid=" + $scope.material.mId + '&onlyNZStk=true';
                        var errorMsg = undefined;
                        if ($scope.material.b == 'no') {
                            errorMsg = $scope.resourceBundle['material.batch.management.disable.warning'];
                        } else if ($scope.material.b == 'yes') {
                            errorMsg = $scope.resourceBundle['material.batch.management.enable.warning'];
                        }
                        var viewInvLink = '<a href="' + stockViewsUrl + '"' + ' target="' + '_blank">' + $scope.resourceBundle['view.nonzero.inventory.items'] + '</a>';
                        errorMsg += '<br/><br/>' + viewInvLink;
                        $scope.showBatchMgmtUpdateWarning(errorMsg);
                        if ($scope.material.b == 'yes') {
                            $scope.material.b = 'no';
                        } else if ($scope.material.b == 'no') {
                            $scope.material.b = 'yes';
                        }
                    }
                }).catch(function error(msg) {
                    $scope.showErrorMsg(msg);
                }).finally(function (){
                    $scope.loading = false;
                    $scope.hideLoading();
                });
                $scope.showBatchMgmtUpdateWarning = function(errorMsg) {
                    var title;
                    if ($scope.material.b == 'yes') {
                        title = $scope.resourceBundle['enable.batch'] + ' ' + $scope.resourceBundle['on'];
                    } else {
                        title = $scope.resourceBundle['disable.batch'] + ' ' + $scope.resourceBundle['on'];
                    }
                    $scope.modalInstance = $uibModal.open({
                        template: '<div class="modal-header ws">' +
                        '<h3 class="modal-title">' + title + ' <bold>' + $scope.material.mnm + '</bold></h3>' +
                        '</div>' +
                        '<div class="modal-body ws">' +
                        '<p>' + errorMsg + '</p>' +
                        '</div>' +
                        '<div class="modal-footer ws">' +
                        '<button class="btn btn-primary" ng-click="dismissModal()">' + $scope.resourceBundle['close'] + '</button>' +
                        '</div>',
                        scope: $scope
                    });
                };
                $scope.dismissModal = function() {
                    $scope.modalInstance.dismiss('cancel');
                };
            }
        };
        $scope.resetMaterial = function () {
            $scope.material = {};
            $scope.setDomainDefault();
            $scope.material.dispInfo = "Yes";
            $scope.uVisited.mnm =false;
        };
        $scope.collapsed = function (attr) {
            if (attr == 'mat') {
                return $scope.isMatCollapsed = !$scope.isMatCollapsed;
            } else if (attr == 'price') {
                return $scope.isPriCollapsed = !$scope.isPriCollapsed;
            } else {
                return $scope.isTempCollapsed = !$scope.isTempCollapsed;
            }
        };
        if (checkNotNullEmpty($scope.editMaterialId)) {
            $scope.edit = true;
        }
        $scope.setDomainDefault = function () {
            if (checkNotNullEmpty($scope.defaultCurrency)) {
                $scope.material.cur = $scope.defaultCurrency;
            }
        };
        $scope.finished = function(type){
            if (!$scope.edit && type == "cur") {
                $scope.setDomainDefault();
            }
        };

        if ($scope.edit) {
            matService.get($scope.editMaterialId).then(function (data) {
                $scope.material = data.data;
                if($scope.material.dty == "bn") {
                    $scope.material.dty = "yes";
                } else {
                    $scope.material.dty = "no";
                }
                $scope.updateTags("fetch");
            }).catch(function error(msg) {
                $scope.showErrorMsg(msg);
            });
            $scope.isMatCollapsed = true;
            $scope.isPriCollapsed = true;
            $scope.isTempCollapsed = true;
        }
        CurrencyController.call(this, $scope, configService);

        $scope.checkMaterialAvailability = function (mnm) {
            if ($scope.edit) {
                $scope.uVisited.mnm = false;
            } else {
                $scope.idVerified = false;
                if(checkNotNullEmpty(mnm)) {
                    matService.checkMaterialAvailability(mnm).then(function (data) {
                        $scope.idStatus = (data.data == true);
                        $scope.idVerified = true;
                    }).catch(function error(msg) {
                        $scope.showErrorMsg(msg);
                    });
                }
            }
        };
    }
]);
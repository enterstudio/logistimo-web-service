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

var entityControllers = angular.module('entityControllers', []);
entityControllers.controller('EntityDetailMenuController', ['$scope', 'entityService', 'mapService', 'domainCfgService', 'requestContext','configService','mediaService',
    'dashboardService','$sce','INVENTORY','$timeout',
    function ($scope, entityService, mapService, domainCfgService, requestContext,configService,mediaService,dashboardService,$sce,INVENTORY,$timeout) {
        var renderContext = requestContext.getRenderContext("setup.entities.detail","entityId");
        var mapColors, mapRange, invPieColors, invPieOrder, entPieColors, entPieOrder, tempPieColors, tempPieOrder;
        $scope.entityId = requestContext.getParam("entityId");
        $scope.entity = undefined;
        $scope.ost = false;
        $scope.loading = true;
        $scope.loadimage = true;
        $scope.lmap = angular.copy($scope.map);
        $scope.showLoading();
        $scope.entityImages = [];
        $scope.ext='';
        $scope.doAddImage=false;
        $scope.imageData='';
        LocationController.call(this, $scope, configService);
        $scope.dLoading = true;

        $scope.fetchEntity = function(){
            entityService.get($scope.entityId).then(function (data) {
                $scope.entity = data.data;
                $scope.subview = renderContext.getNextSection();
                $scope.address = $scope.addressStr($scope.entity);
                $scope.getMaterialStats($scope.entityId);
                $scope.getFilteredEntGrps();
                $scope.lmap = mapService.updateEntityMap($scope.entity);
                if (checkNullEmpty($scope.lmap.center)) {
                    $scope.lmap.center = {latitude: 0, longitude: 0};
                }
                $scope.getDomainKeyImages($scope.entityId);
            }).catch(function error(msg) {
                $scope.showErrorMsg(msg);
            }).finally(function (){
                $scope.loading = false;
                $scope.hideLoading();
            });
        };

        $scope.$watch('cnff',function(newval) {
            if(checkNotNullEmpty(newval)) {
                $scope.hideAllRelActions = $scope.iMan && !$scope.cnff.enableCs && !$scope.cnff.enableVs;
            }
        });

        var count = 0;
        var mTag;
        var excludeTempSt;
        domainCfgService.getDashboardCfg().then(function(data) {
            var exStatusList = {"Normal":"tn","Low":"tl","High":"th","Unknown":"tu"};
            count++;
            $scope.mTag = [];
            mTag = data.data.dmtg;
            if(checkNotNullEmpty(mTag)) {
                mTag.forEach(function (data) {
                    $scope.mTag.push({'text': data, 'id': data});
                });
                $scope.mTag = constructModel($scope.mTag);
                mTag = mTag.toString();
            }

            excludeTempSt = data.data.exts;
            if(checkNotNullEmpty(data.data.exts)) {
                $scope.exTempState = [];
                data.data.exts.forEach(function (data) {
                    $scope.exTempState.push({'text': data, 'id': exStatusList[data]});
                });
            }
        }).catch(function error(msg) {
            $scope.showErrorMsg(msg);
        }).finally(function() {
            if(count >1) {
                $scope.getDashboardData();
            }
        });
        domainCfgService.getSystemDashboardConfig().then(function (data) {
            count++;
            var dConfig = angular.fromJson(data.data);
            mapColors = dConfig.mc;
            $scope.mc = mapColors;
            mapRange = dConfig.mr;
            $scope.mr = mapRange;
            invPieColors = dConfig.pie.ic;
            invPieOrder = dConfig.pie.io;
            entPieColors = dConfig.pie.ec;
            entPieOrder = dConfig.pie.eo;
            tempPieOrder = dConfig.pie.to;
            tempPieColors = dConfig.pie.tc;
            if( $scope.tempOnlyAU ) {
                $scope.mapEvent = tempPieOrder[0];
            } else {
                $scope.mapEvent = invPieOrder[0];
            }
        }).catch(function error(msg) {
            $scope.showErrorMsg(msg);
        }).finally(function() {
            if(count >1) {
                $scope.getDashboardData();
            }
        });

        $scope.getDomainKeyImages = function(entityId){
            $scope.loadimage = true;
            mediaService.getDomainkeyImages(entityId).then(function(data) {
                if (checkNotNullEmpty(data.data)) {
                    $scope.entityImages = data.data.items;
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

        $scope.getFilteredEntGrps = function () {
            if ($scope.entity != null) {
                for (var item in $scope.entity.entGrps) {
                    $scope.entity.pgs = $scope.entity.entGrps[item].name;
                }
            }
            return $scope.entity;
        };

        $scope.getMaterialStats = function (entityId) {
            entityService.getMaterialStats(entityId, $scope.tag).then(function (data) {
                $scope.materialStats = data.data;
                $scope.populateMaterialStats();
                $scope.populateMonthlyUsageStats(entityId);
            }).catch(function error(msg) {
                $scope.showErrorMsg(msg);
            });
        };
        $scope.populateMaterialStats = function () {
            if ($scope.materialStats != null) {
                $scope.entity.oos = $scope.materialStats['oos'];
                if ($scope.entity.oos > 0) {
                    $scope.ost = true;
                }
            }
        };
        $scope.populateMonthlyUsageStats = function (entityId) {
            entityService.getMonthlyUsageStats(entityId).then(function (data) {
                $scope.monthlyStats = data.data;
            }).catch(function error(msg) {
                $scope.showErrorMsg(msg);
            });
        };
        $scope.$on("requestContextChanged", function () {
            if (!renderContext.isChangeRelevant()) {
                return;
            }
            if ($scope.entityId != requestContext.getParam("entityId")) {
                $scope.entity = undefined;
                $scope.entityId = requestContext.getParam("entityId");
                $scope.fetchEntity();
            }else{
                $scope.subview = renderContext.getNextSection();
            }

        });

        $scope.addressStr = function (inAddress) {
            var address = "";
            if (inAddress != null) {
                address = concatIfNotEmpty(address, inAddress.str);
                address = concatIfNotEmpty(address, inAddress.ct);
                address = concatIfNotEmpty(address, inAddress.tlk);
                address = concatIfNotEmpty(address, inAddress.ds);
                address = concatIfNotEmpty(address, inAddress.st);
                address = concatIfNotEmpty(address, $scope.getCountryNameByCode(inAddress.ctr));
                address = concatIfNotEmpty(address, inAddress.zip);
                address = stripLastComma(address);
            }
            return address;
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
            mediaService.uploadImage($scope.ext,$scope.entityId,$scope.imageData.base64).then(function(){
                $scope.showSuccess($scope.resourceBundle['image.upload.success']);
                $scope.getDomainKeyImages($scope.entityId);
                angular.element('#entityFileupload').val(null);
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
                $scope.getDomainKeyImages($scope.entityId);
            }).catch(function error(msg){
                $scope.showErrorMsg(msg)
            }).finally(function(){
                $scope.hideLoading();
            });

        };

        $scope.addImage = function(){
            if(!(checkNullEmpty($scope.entityImages) || $scope.entityImages.length ==0 )){
                if($scope.entityImages.length >=5)
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
        $scope.getDashboardData = function() {
            var eid = $scope.entityId;
            dashboardService.getEntInvData(eid, $scope.mTag).then(function (data) {
                $scope.dashboardData = data.data;
                constructPieData($scope.dashboardData);
            }).catch(function error(msg) {
                $scope.showErrorMsg(msg);
            }).finally(function () {
                $scope.dLoading = false;
            });
        };

        function constructPieData(data) {
            if (checkNotNullEmpty(data)) {
                $scope.pieData = [];
                $scope.pieData[0] = constructPie(data.invDomain, invPieColors, invPieOrder, "p1", $scope.resourceBundle['inventory'] + " " + $scope.resourceBundle['order.items']);
                $scope.pieData[1] = constructPie(data.tempDomain, tempPieColors, tempPieOrder, "p2", "assets", $scope.exTempState);
                $scope.pOpt = {
                    "showLabels": 0,
                    "showPercentValues": 1,
                    "showPercentInToolTip": 0,
                    "showZeroPies": 1,
                    "enableSmartLabels": 1,
                    "labelDistance": -10,
                    "toolTipSepChar": ": ",
                    "theme": "fint",
                    "enableRotation": 0,
                    "enableMultiSlicing": 0,
                    "labelFontSize": 14,
                    "useDataPlotColorForLabels": 1,
                    "showLegend": 1
                };
                $scope.pieOpt = [];
                $scope.entDashboardData = [];
                $scope.pieOpt[0] = angular.copy($scope.pOpt);
                $scope.p1caption = "Inventory";
                $scope.pieOpt[1] = angular.copy($scope.pOpt);
                $scope.p2caption = "Temperature";
                constructInvMetaData(data.invDomain);
                constructTempMetaData(data.tempDomain);
            }
        }
        function constructInvMetaData(data) {
            if (checkNotNullEmpty(data)) {
                var availableData = data['n'] || 0;
                availableData += (data['201'] || 0);
                availableData += (data['202'] || 0);
                data['200'] = data['200'] || 0;
                var total = availableData + data['200'];
                if(total > 0) {
                    if(availableData > 0) {
                        $scope.entDashboardData[0] = ((availableData / total) * 100).toFixed(2);
                        $scope.invNrmlTT = $scope.resourceBundle['available'] + ": " + availableData + " of " + total + " " + $scope.resourceBundle['inventory'] + " " + $scope.resourceBundle['order.items'];
                    }
                    if(data['200'] > 0) {
                        $scope.entDashboardData[1] = ((data['200'] / total) * 100).toFixed(2);
                        $scope.invZeroTT = $scope.resourceBundle['report.zerostock'] + ": " + data['200'] + " of " + total + " " + $scope.resourceBundle['inventory'] + " " + $scope.resourceBundle['order.items'];
                    }
                } else {
                    $scope.hideInvData = true;
                }
            }
        }

        function constructTempMetaData(data) {
            if (checkNotNullEmpty(data)) {
                data['tn'] = data['tn'] || 0;
                data['th'] = data['th'] || 0;
                data['tl'] = data['tl'] || 0;
                data['tu'] = data['tu'] || 0;
                var total = data['tn'] + data['th'] + data['tl'] + data['tu'];
                if(total > 0) {
                    var tempStatus = {};
                    tempStatus['tn'] = $scope.resourceBundle['normal'];
                    tempStatus['tl'] = $scope.resourceBundle['low'];
                    tempStatus['th'] = $scope.resourceBundle['high.uppercase'];
                    tempStatus['tu'] = $scope.resourceBundle['status.unknown'];
                    var index = 2;
                    $scope.tempTooltip = {};
                    for(var key in tempStatus) {
                        if((checkNullEmpty(excludeTempSt) || !excludeTempSt.includes(tempStatus[key])) && data[key] > 0) {
                            $scope.entDashboardData[index] = ((data[key] / total) * 100).toFixed(2);
                            $scope.tempTooltip[key] = tempStatus[key] + ": " + data[key] + " of " + total + " asset(s)";
                        }
                        index++;
                    }
                } else {
                    $scope.hideTempData = true;
                }
            }
        }
        function constructPie(data, color, order, pieId, label, ets) {
            if(checkNotNullEmpty(ets)) {
                ets.forEach(function(exStatus){
                    delete data[exStatus.id];
                });
            }
            var d = [];
            var iEntEvent = false;
            var iTempEvent = false;
            var isOneDataAvailable = false;
            for (var or in order) {
                var dd = order[or];

                if(checkNotNullEmpty(ets)){
                    ets.forEach(function(ele){
                        if(ele.id == dd){
                            dd = undefined;
                        }
                    });
                }

                var o = {};
                if (dd == INVENTORY.stock.STOCKOUT) {
                    o.label = "Zero stock";
                    o.color = color[0];
                } else if (dd == INVENTORY.stock.UNDERSTOCK) {
                    o.label = "Min";
                    o.color = color[1];
                } else if (dd == INVENTORY.stock.OVERSTOCK) {
                    o.label = "Max";
                    o.color = color[2];
                } else if (dd == "n") {
                    o.label = "Normal";
                    o.color = color[3];
                } else if (dd == "a") {
                    o.label = "Active";
                    o.color = color[0];
                    iEntEvent = true;
                } else if (dd == "i") {
                    o.label = "Inactive";
                    o.color = color[1];
                    iEntEvent = true;
                } else if (dd == "tn") {
                    o.label = "Normal";
                    o.color = color[0];
                    iTempEvent = true;
                } else if (dd == "tl") {
                    o.label = "Low";
                    o.color = color[1];
                    iTempEvent = true;
                } else if (dd == "th") {
                    o.label = "High";
                    o.color = color[2];
                    iTempEvent = true;
                } else if (dd == "tu") {
                    o.label = "Unknown";
                    o.color = color[3];
                    iTempEvent = true;
                } else {
                    o.label = dd;
                }
                if (dd == $scope.mapEvent) {
                    o.isSliced = 1;
                }
                o.value = data[dd] || 0;
                o.toolText = "$label: $value of $unformattedSum" + " " + label;
                if (data[dd]) {
                    isOneDataAvailable = true;
                }
                o.showValue = o.value > 0 ? 1 : 0;
                d.push(o);
            }
            var subCaption = undefined;
            var tSubCaption = undefined;
            if(checkNotNullEmpty(mTag) && checkNullEmpty(ets)) {
                $scope.subcaption = "<b> Material tag(s): </b>" + mTag ;
            }
            if(checkNotNullEmpty(ets) && checkNotNullEmpty(excludeTempSt)) {
                $scope.tsubcaption = "<b> Exclude temperature state(s): </b>" + excludeTempSt;
            }
            var pSubCaption = undefined;
            if (pieId == "p1") {
                $scope.p1subCaption = $sce.trustAsHtml(subCaption || '&nbsp;');
                $scope.pSubCaption = $sce.trustAsHtml(pSubCaption || '&nbsp;');
            } else if (pieId == "p2") {
                $scope.p2subCaption = $sce.trustAsHtml(tSubCaption || '&nbsp;');
            }
            return isOneDataAvailable ? d : [];
        }

        $scope.fetchEntity();

    }
]);
entityControllers.controller('EntityListController', ['$scope', 'entityService', 'domainCfgService', 'mapService', 'requestContext', '$location', 'exportService',
    function ($scope, entityService, domainCfgService, mapService, requestContext, $location, exportService) {
        $scope.wparams = [["etag", "etag"], ["search", "search.nm"], ["o", "offset"], ["s", "size"]];
        $scope.edit = false;
        $scope.success = false;
        $scope.loading = false;
        $scope.selectAll = function (newval) {
            for (var item in $scope.filtered) {
                $scope.filtered[item]['selected'] = newval;
            }
        };
        $scope.lmap = angular.copy($scope.map);
        $scope.entities=undefined;
        $scope.init = function () {
            $scope.etag = requestContext.getParam("etag") || "";
            $scope.search = {nm:""};
            $scope.search.key = $scope.search.nm = requestContext.getParam("search") || "";
            $scope.vw = requestContext.getParam("vw") || 't';
            $scope.offset = requestContext.getParam("o") || 0;
            $scope.size = requestContext.getParam("s") || 50;
            $scope.selAll = false;
        };
        $scope.init();
        ListingController.call(this, $scope, requestContext, $location);
        $scope.$watch("vw",function(){
            if($scope.vw == 'm'){
                $scope.search = {nm:""};
            }
        });
        $scope.fetch = function () {
            $scope.loading = true;
            $scope.showLoading();
            entityService.getAll($scope.offset, $scope.size,$scope.etag, $scope.search.nm).then(function (data) {
                $scope.entities = data.data;
                $scope.setResults($scope.entities);
                $scope.getFiltered();
                fixTable();
            }).catch(function error(msg) {
                $scope.setResults(null);
                $scope.showErrorMsg(msg);
            }).finally(function () {
                $scope.loading = false;
                $scope.hideLoading();
            });
        };
        $scope.fetch();
        $scope.deleteEntities = function () {
            var entities = "";
            for (var item in $scope.filtered) {
                if ($scope.filtered[item].selected) {
                    entities = entities.concat($scope.filtered[item].id).concat(',');
                }
            }
            entities = stripLastComma(entities);
            if (entities == "") {
                $scope.showWarning($scope.resourceBundle['selectitemtoremovemsg']);
            } else {
                if (!confirm($scope.resourceBundle.removekioskconfirmmsg + '?')) {
                    return;
                }
                $scope.showLoading();
                entityService.deleteEntities(entities).then(function (data) {
                    $scope.showSuccess(data.data);
                    $scope.fetch();
                }).catch(function error(msg) {
                    $scope.showErrorMsg(msg);
                }).finally(function() {
                    $scope.hideLoading();
                });
            }
        };
        $scope.selectedEntities = function () {
            var entities = "";
            for (var item in $scope.filtered) {
                if ($scope.filtered[item].selected) {
                    if($scope.currentDomain == $scope.filtered[item].sdid){
                        entities = entities.concat($scope.filtered[item].id).concat(',');
                    }
                }
            }
            return stripLastComma(entities);
        };
        $scope.getFiltered = function () {
            var list = [];
            if ($scope.entities != null) {
                for (var item in $scope.entities.results) {
                    var entity = $scope.entities.results[item];
                    if (checkNotNullEmpty($scope.search.nm) && entity.nm.toLowerCase().indexOf($scope.search.nm.toLowerCase()) == -1) {
                        continue;
                    }
                    list.push(entity);
                }
            }
            $scope.filtered = list;
            mapService.convertLnLt($scope.filtered, $scope.lmap);
            return $scope.filtered;
        };
        $scope.metadata = [{'title': 'Sl.No.', 'field': 'sno','class':'text-center'}, { 'title': 'Entity Name', 'field': 'nm'},
            {'title': 'Location', 'field': 'stk'}, {'title': 'Tags', 'field': 'tgs'},
            {'title': 'Registered On', 'field': 'ts'}, {'title': 'Shortcuts'}];

        $scope.addOrRemoveMaterials = function () {
            var selectedEntities = $scope.selectedEntities();
            if(checkNullEmpty(selectedEntities)){
                if($scope.iSU){
                    if (!confirm($scope.resourceBundle.addremoveallentities)) {
                        return;
                    }
                }else{
                    if (!confirm($scope.resourceBundle.addmatallentities)) {
                        return;
                    }
                }
                $location.path('/setup/entities/all/materials').search({});
            } else {
                $location.path('/setup/entities/all/materials').search({eids: selectedEntities});
            }
        };
        $scope.searchEntity = function () {
            if($scope.search.nm != $scope.search.key) {
                $scope.search.nm = $scope.search.key;
            }
        };
        $scope.reset = function() {
            $scope.etag = "";
            $scope.search = {};
        };
    }
]);

entityControllers.controller('MoveEntityController', ['$scope', 'entityService', 'domainCfgService', 'mapService', 'requestContext', '$location',
    function ($scope, entityService, domainCfgService, mapService, requestContext, $location) {
        $scope.wparams = [["etag", "etag"], ["search", "search.nm"], ["o", "offset"], ["s", "size"]];
        $scope.allEntity = false;
        $scope.selectAll = function (newval, force) {
            if(checkNotNullEmpty($scope.entities)){
                $scope.entities.forEach(function(data){
                    if(!data.nCheck || force) {
                        data.selected = newval;
                    }
                });
            }
        };
        $scope.searchEntity = function () {
            if($scope.search.nm != $scope.search.key){
                $scope.search.nm = $scope.search.key;
            }
        };
        $scope.mSelectAll = function (newval) {
            if(checkNotNullEmpty($scope.mEntities)){
                $scope.mEntities.forEach(function(data){
                    data.selected = newval;
                });
            }
        };
        $scope.entities=undefined;
        $scope.etag="";
        $scope.mEntities=[];
        $scope.destDomainId=undefined;
        $scope.allDestDomainId=undefined;
        $scope.init = function () {
            $scope.offset = requestContext.getParam("o") || 0;
            $scope.size = requestContext.getParam("s") || 50;
            $scope.etag = requestContext.getParam("etag") || "";
            $scope.search = {nm:""};
            $scope.search.key = $scope.search.nm = requestContext.getParam("search") || "";
            $scope.selAll = false;
            $scope.mSelAll = false;
            $scope.moveType = "move";
        };
        $scope.init();
        ListingController.call(this, $scope, requestContext, $location);
        $scope.fetch = function () {
            $scope.loading = true;
            $scope.showLoading();
            entityService.getDomainEntities($scope.offset, $scope.size, $scope.etag, $scope.search.nm).then(function (data) {
                $scope.entities = data.data.results;
                setCheckBox();
                $scope.setResults(data.data);
            }).catch(function error(msg) {
                $scope.showErrorMsg(msg);
            }).finally(function () {
                $scope.loading = false;
                $scope.hideLoading();
            });
        };
        $scope.fetch();
        function setCheckBox() {
            if (checkNotNullEmpty($scope.entities)) {
                $scope.entities.forEach(function (data) {
                    if (checkNotNullEmpty($scope.mEntities)) {
                        data.nCheck = $scope.mEntities.some(function (ndata) {
                            if (data.id == ndata.id) {
                                return true;
                            }
                        });
                    } else {
                        data.nCheck = false;
                    }
                });
                $scope.selAll = false;
                $scope.mSelAll = false;
                $scope.selectAll(false, true);
                $scope.mSelectAll(false);
            }
        }
        function getSelectedEntities(entityList) {
            var entities =[];
            if(checkNotNullEmpty(entityList)){
                entityList.forEach(function(data){
                    if(data.selected){
                        entities.push(angular.copy(data));
                    }
                });
            }
            return entities;
        }
        function getMoveEntityIds() {
            var entities = "";
            if(checkNotNullEmpty($scope.mEntities)){
                $scope.mEntities.forEach(function(data){
                    entities = entities.concat(data.id).concat(',');
                });
            }
            return stripLastComma(entities);
        }
        $scope.moveRight = function(){
            var ent = getSelectedEntities($scope.entities);
            $scope.mEntities = $scope.mEntities.concat(ent);
            setCheckBox();
            $scope.entityMoved=true;
        };
        $scope.moveLeft = function(){
            var ent = getSelectedEntities($scope.mEntities);
            var eIds = [];
            if(checkNotNullEmpty(ent)) {
                ent.forEach(function (d) {
                    eIds.push(d.id);
                });
                $scope.mEntities = $scope.mEntities.filter(function (x) {
                    return eIds.indexOf(x.id) < 0
                });
                setCheckBox();
            }
        };

        function validate() {
            if(!$scope.allEntity && checkNullEmpty($scope.destDomainId)) {
                $scope.showWarning("Please select target domain");
                return false;
            }
            if($scope.allEntity && checkNullEmpty($scope.allDestDomainId)){
                $scope.showWarning("Please select target domain");
                return false;
            }
            if(!$scope.allEntity && (checkNullEmpty($scope.mEntities) || $scope.mEntities.size == 0)) {
                $scope.showWarning("Please move at least 1 " + $scope.resourceBundle['kiosk.lowercase'] + " from source list to target list");
                return false;
            }
            return true;
        }

        $scope.moveEntity = function() {
            if(validate()) {
                $scope.showLoading();
                var dest;
                var entIds;
                if ($scope.allEntity) {
                    dest = $scope.allDestDomainId.id;
                    entIds = 'all';
                } else {
                    dest = $scope.destDomainId.id;
                    entIds = getMoveEntityIds();
                }
                entityService.moveEntity(entIds, dest).then(function (data) {
                    if (data.data.indexOf("Some users") == 1) {
                        $scope.showError(data.data);
                    } else {
                        $scope.showSuccess(data.data);
                        $scope.entityMoved = false;
                    }
                }).catch(function error(msg) {
                    $scope.showErrorMsg(msg);
                }).finally(function () {
                    $scope.hideLoading();
                });
            }
        };
        $scope.addEntity = function() {
            if(validate()) {
                $scope.showLoading();
                var dest;
                var entIds;
                if ($scope.allEntity) {
                    dest = $scope.allDestDomainId.id;
                    entIds = 'all';
                } else {
                    dest = $scope.destDomainId.id;
                    entIds = getMoveEntityIds();
                }
                entityService.domainUpdate(entIds, dest, "add").then(function (data) {
                    $scope.showSuccess(data.data);
                    $scope.entityMoved = false;
                }).catch(function error(msg) {
                    $scope.showErrorMsg(msg);
                }).finally(function () {
                    $scope.hideLoading();
                });
            }
        };
        $scope.reset = function(){
            $scope.etag = "";
            $scope.search = {};
        };
        $scope.switchTable = function(){
            if($scope.allEntity) {
                $scope.destDomainId=undefined;
                $scope.mEntities=[];
                setCheckBox();
                $scope.entityMoved=true;
            } else {
                $scope.allDestDomainId=undefined;
                $scope.entityMoved=true;
            }
        }
    }
]);

entityControllers.controller('EntityMapListController', ['$scope', 'entityService', 'domainCfgService', 'mapService', 'requestContext', '$location','uiGmapGoogleMapApi',
    function ($scope, entityService, domainCfgService, mapService, requestContext, $location, uiGmapGoogleMapApi) {
        $scope.wparams = [["etag", "letag"], ["o", "offset"], ["s", "size"]];
        $scope.loading = false;
        $scope.mOffset = 0;
        $scope.mSize = 50;
        $scope.lmap = angular.copy($scope.map);
        $scope.entities=undefined;
        $scope.noCluster = false;
        $scope.fetchNow = true;

        $scope.init = function (firstTimeInit) {
            $scope.vw = requestContext.getParam("vw") || 't';
            if(firstTimeInit){
                if ($location.$$search.o) {
                    delete $location.$$search.o;
                    $scope.fetchNow = false;
                }
                if ($location.$$search.tag) {
                    delete $location.$$search.tag;
                    $scope.fetchNow = false;
                }
                $location.$$compose();
            }
            $scope.offset = requestContext.getParam("o") || 0;
            $scope.size = requestContext.getParam("s") || 50;
            tag = requestContext.getParam("etag") || "";
            if(tag != $scope.letag){
                $scope.entities = {};
                $scope.mSize = $scope.size;
                $scope.mOffset = 0;
                $scope.letag = tag;
            }
            if($scope.mSize != $scope.size){
                $scope.mSize = $scope.size;
                $scope.mOffset = 0;
                $scope.entities = {};
            }
        };
        $scope.init(true);
        ListingController.call(this, $scope, requestContext, $location);
        $scope.fetch = function () {
            $scope.loading = true;
            $scope.showLoading();

            if (checkNotNullEmpty($scope.entities) && checkNotNullEmpty($scope.entities.results)) {
                $scope.mOffset = parseInt($scope.mOffset) + parseInt($scope.mSize);
            }
            entityService.getAll($scope.mOffset, $scope.mSize,$scope.letag).then(function (data) {
                if (checkNullEmpty($scope.entities) || checkNullEmpty($scope.entities.results)) {
                    $scope.entities = data.data;
                } else {
                    if(checkNotNullEmpty(data.data.results)) {
                        $scope.entities.results = $scope.entities.results.concat(data.data.results);
                    }
                }
                $scope.setResults($scope.entities);
                $scope.filtered = $scope.getFiltered();
            }).catch(function error(msg) {
                $scope.setResults(null);
                $scope.showErrorMsg(msg);
            }).finally(function () {
                $scope.loading = false;
                $scope.hideLoading();
            });
        };
        if($scope.fetchNow){
            uiGmapGoogleMapApi.then(function(){
                $scope.fetch();
            });
        }

        $scope.getFiltered = function () {
            var list = $scope.entities.results;
            var fList = [];
            for(var i in list){
                var item = list[i];
                if(item.lt != 0 && item.ln != 0){
                    var mapModel = {id:item.id,nm:item.nm,loc:", "+item.loc};
                    mapModel.lt = mapModel.latitude = item.lt;
                    mapModel.ln = mapModel.longitude = item.ln;
                    mapModel.tu = $scope.templateUrl;
                    mapModel.tp = {item: {id:item.id,nm:item.nm,loc:mapModel.loc,ct:item.ct,st:item.st}};
                    mapModel.options = {title:mapModel.nm+mapModel.loc};
                    mapModel.show = false;
                    mapModel.onClick = function() {
                        mapModel.show = !mapModel.show;
                    };
                    fList.push(mapModel);
                }
            }
            mapService.convertLnLt(fList, $scope.lmap);
            return fList;
        };

        $scope.reset = function () {
            $scope.letag = "";
            $scope.search = {};
        };

    }
]);

entityControllers.controller('AddEntController', ['$scope', '$uibModal','$route', 'entityService', 'domainCfgService', 'configService', 'mapService', 'requestContext', 'OPTIMIZER', 'INVENTORY','$window','$location','PATTERNS',
    function ($scope, $uibModal, $route, entityService, domainCfgService, configService, mapService, requestContext, OPTIMIZER, INVENTORY, $window, $location, PATTERNS) {
        $scope.lmap = angular.copy($scope.map);
        $scope.lmap.zoom = 1;
        $scope.edit = false;
        $scope.loading = false;
        $scope.editEntityId = requestContext.getParam("eid");
        $scope.entity = {tx: 0.0, be: true};
        $scope.uVisited = {usrs:false};
        $scope.latitudePattern = new RegExp(PATTERNS.LATITUDE);
        $scope.longitudePattern = new RegExp(PATTERNS.LONGITUDE);
        $scope.taxPattern = new RegExp(PATTERNS.TAX);
        $scope.setUserVisited = function() {
            $scope.uVisited.usrs = true;
        };
        $scope.defineWatchers = function () {
            $scope.$watch("entity.ctr", function (newval, oldval) {
                if (newval != oldval) {
                    $scope.entity.st = "";
                    $scope.entity.ds = "";
                    $scope.entity.tlk = "";
                }
            });
            $scope.$watch("entity.st", function (newval, oldval) {
                if (newval != oldval) {
                    $scope.entity.ds = "";
                    $scope.entity.tlk = "";
                }
            });
            $scope.$watch("entity.ds", function (newval, oldval) {
                if (newval != oldval) {
                    $scope.entity.tlk = "";
                }
            });
            $scope.$watch("entity.inv", function(newVal, oldVal) {
                if(newVal != oldVal && newVal == INVENTORY.models.SQ) {
                    $scope.entity.sl = 85;
                }
            });
        };
        $scope.setManualGeo = function(){
            if(checkNotNullEmpty($scope.entity.lt) && checkNotNullEmpty($scope.entity.ln)){
                $scope.setMarker($scope.entity.lt,$scope.entity.ln,true);
            }
        };
        $scope.tags = {};
        $scope.oc = {};
        $scope.serviceLevels = [];
        $scope.isEOQ = false;

        for (var i = 99; i >= 65; i--) {
            $scope.serviceLevels.push(i);
        }
        $scope.setDefaults = function(){
            $scope.setCountry($scope.ctr);
            $scope.entity.ctr = $scope.ctr;
            $scope.entity.cur = $scope.cur;
            $scope.entity.st = $scope.st;
            if ($scope.isEOQ) {
                $scope.entity.inv = INVENTORY.models.SQ;
                $scope.entity.sl = 85;
            } else {
                $scope.entity.inv = INVENTORY.models.NONE;
            }
            $scope.setState($scope.st);
            //addTestData();
        };
        $scope.setDomainDefault = function (callback) {
            $scope.loading = true;
            $scope.showLoading();
            domainCfgService.get().then(function (data) {
                var dc = data.data;
                if (checkNotNullEmpty(dc.currency)) {
                    $scope.cur = dc.currency;
                }
                $scope.ctr = dc.country;
                $scope.st = dc.state;
                $scope.tags = checkNotNullEmpty(dc.ktags) ? dc.ktags.split(",") : "";
                $scope.oc = dc.optimizerConfig;
                $scope.isEOQ = $scope.oc.compute == OPTIMIZER.compute.EOQ;
                if (!$scope.edit) {
                    $scope.setDefaults();
                    if(callback) {
                        callback();
                    }
                }
            }).catch(function error(msg) {
                $scope.showErrorMsg(msg);
            }).finally(function (){
                $scope.loading = false;
                $scope.hideLoading();
            });
        };
        if (!$scope.edit) {
            $scope.setDomainDefault(function() {
                $scope.defineWatchers();
            });
        }
        if (checkNotNullEmpty($scope.editEntityId)) {
            $scope.edit = true;
            entityService.get($scope.editEntityId).then(function (data) {
                $scope.entity = data.data;
                $scope.updateTags("fetch");
                $scope.updateUsrs();
                $scope.setCountry($scope.entity.ctr);
                $scope.setState($scope.entity.st);
                $scope.setDistrict($scope.entity.ds);
                $scope.setMapObj($scope.entity,true);
                $scope.setMarker($scope.entity.lt,$scope.entity.ln,true);
                $scope.defineWatchers();
            }).catch(function error(msg) {
                $scope.showErrorMsg(msg);
            });
        }
        function checkLatLong(){
            if(checkNullEmpty($scope.entity.lt)){
                $scope.entity.lt = 0;
            }
            if(checkNullEmpty($scope.entity.ln)){
                $scope.entity.ln = 0;
            }
        }
        $scope.createEntity = function () {
            if ($scope.entity != null) {
                $scope.updateTags("add");
                $scope.loading = true;
                $scope.showLoading();
                checkLatLong();
                entityService.createEntity($scope.entity).then(function (data) {
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
        $scope.updateEntity = function () {
            if ($scope.entity != null) {
                $scope.updateTags("update");
                $scope.loading = true;
                $scope.showLoading();
                checkLatLong();
                entityService.update($scope.entity).then(function (data) {
                    if (checkNotNullEmpty(data.data)) {
                        $scope.$back();
                        $scope.showSuccess(data.data);
                    } else {
                        var stockViewsUrl = "#/inventory/?eid=" + $scope.entity.id + '&matType=1&onlyNZStk=true';
                        var errorMsg = undefined;
                        if (!$scope.entity.be) {
                            errorMsg = $scope.resourceBundle['entity.batch.management.disable.warning'];
                        } else if ($scope.entity.be) {
                            errorMsg = $scope.resourceBundle['entity.batch.management.enable.warning'];
                        }
                        var viewInvLink = '<a href="' + stockViewsUrl + '"' + ' target="' + '_blank">' + $scope.resourceBundle['view.nonzero.inventory.items'] + '</a>';
                        errorMsg += '<br/><br/>' + viewInvLink;
                        $scope.showBatchMgmtUpdateWarning(errorMsg);
                        $scope.entity.be = !$scope.entity.be;
                    }
                }).catch(function error(msg) {
                    $scope.showErrorMsg(msg);
                }).finally(function (){
                    $scope.loading = false;
                    $scope.hideLoading();
                });
            }
        };
        $scope.setAllVisited = function(){
            $scope.uVisited = {};
            $scope.uVisited.id=true;
            $scope.uVisited.ctr=true;
            $scope.uVisited.st=true;
            $scope.uVisited.vil=true;
            $scope.uVisited.usrs=true;
        };
        $scope.resetEntity = function () {
            $scope.uVisited = {};
            $scope.lmap = angular.copy($scope.map);
            $scope.entity = {tx: 0.0, be: true};
            $scope.setDefaults();
        };
        $scope.updateTags = function (action) {
            if ($scope.entity != null) {
                if(action == 'add' || action == 'update'){
                    $scope.entity.tgs = [];
                    if(checkNotNullEmpty($scope.entity.tgObjs)){
                        for (var i = 0; i < $scope.entity.tgObjs.length; i++) {
                            $scope.entity.tgs.push($scope.entity.tgObjs[i].text);
                        }
                    }
                } else if(action == 'fetch'){
                    $scope.entity.tgObjs = [];
                    if(checkNotNullEmpty($scope.entity.tgs)){
                        for (i = 0; i < $scope.entity.tgs.length; i++) {
                            $scope.entity.tgObjs.push({"id": $scope.entity.tgs[i], "text": $scope.entity.tgs[i]});
                        }
                    }
                }
            }
        };
        $scope.updateUsrs = function () {
            if ($scope.entity != null) {
                if (checkNotNullEmpty($scope.entity.usrs)) {
                    $scope.entity.users = $scope.entity.usrs;
                    $scope.entity.usrs = [];
                    for (var i = 0; i < $scope.entity.users.length; i++) {
                        $scope.entity.usrs.push({"id": $scope.entity.users[i].id, "text": $scope.entity.users[i].fnm+' ['+$scope.entity.users[i].id+']'});
                    }
                }
            }
        };
        $scope.setMapObj = function (results,isInitLoad) {
            if(results.length == 1) {
                var center = trimGeo(results[0].geometry.location);
                $scope.lmap = {center: {latitude: center.ltt, longitude: center.lgt}, zoom: 8};
                $scope.entity.lt = center.ltt;
                $scope.entity.ln = center.lgt;
                var latlngs = [{latitude: center.ltt, longitude: center.lgt, id:0, options : {title:results[0].formatted_address + '\n(' + center.ltt +', '+center.lgt +')' , draggable: true}}];
            } else {
                $scope.lmap = angular.copy($scope.map);
                latlngs = [];
                for(var i=0;i<results.length;i++){
                    var loc = trimGeo(results[i].geometry.location);
                    latlngs.push({latitude:loc.ltt,longitude: loc.lgt, id:i, options : {title:results[i].formatted_address + '\n(' + loc.ltt +', '+loc.lgt +')' , draggable: true}});
                }
            }
            $scope.lmap.markers = latlngs;
            $scope.events= {
                dragend: function (marker) {
                    $scope.setGeo(marker,true);
                }
            };
            mapService.convertLnLt(latlngs, $scope.lmap);
            if(!isInitLoad) {
                $scope.$apply();
            }
        };
        $scope.setMarker = function(lat,lng,isWatch){
            $scope.lmap = {center: {latitude: lat, longitude: lng}, zoom: 8};
            $scope.entity.lt = lat;
            $scope.entity.ln = lng;
            var latlngs = [{latitude: lat, longitude: lng, id: 0, options : {title: lat +', '+lng , draggable: true}}];
            $scope.lmap.markers = latlngs;
            mapService.convertLnLt(latlngs, $scope.lmap);
            if(!isWatch){
                $scope.$apply();
            }
        };
        $scope.setGeo = function(marker,noConfirm){
            if(noConfirm!=true) {
                if (!confirm($scope.resourceBundle['geocodes.confirmmessage'] + "?")) {
                    return;
                }
            }
            var pos = trimGeo(marker.getPosition());
            $scope.setMarker(pos.ltt,pos.lgt);
        };

        $scope.getGeoCodes = function () {
            var addressStr = constructAddressStr($scope.entity);
            if (addressStr != "") {
                mapService.getLatLong(addressStr, $scope.setMapObj);
            }
        };
        $scope.checkEntityAvailability = function (enm) {
            if ($scope.edit) {
                $scope.uVisited.id = false;
            } else {
                $scope.idVerified = false;
                if(checkNotNullEmpty(enm)) {
                    entityService.checkEntityAvailability(enm).then(function (data) {
                        $scope.idStatus = (data.data == true);
                        $scope.idVerified = true;
                    }).catch(function error(msg) {
                        $scope.showErrorMsg(msg);
                    });
                }
            }
        };
        $scope.validate = function(form){
            if(!$scope.idStatus && form.$valid && checkNotNullEmpty($scope.entity.usrs)){
                return true;
            }else{
                $scope.showFormError();
                return false;
            }
        };
        $scope.validateGeo = function() {
            if(checkNullEmpty($scope.entity.ctr) || checkNullEmpty($scope.entity.st) || checkNullEmpty($scope.entity.ct)) {
                $scope.invalidGeo = true;
                return false;
            }
            $scope.invalidGeo = false;
            return true;
        };
        $scope.showBatchMgmtUpdateWarning = function(errorMsg) {
            var title;
            if ($scope.entity.be) {
                title = $scope.resourceBundle['enable.batch'] + ' ' + $scope.resourceBundle['on'];
            } else {
                title = $scope.resourceBundle['disable.batch'] + ' ' + $scope.resourceBundle['on'];
            }
            $scope.modalInstance = $uibModal.open({
                template: '<div class="modal-header ws">' +
                '<h3 class="modal-title">' + title + ' ' + '<bold>' + $scope.entity.nm + '</bold></h3>' +
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
        LocationController.call(this, $scope, configService);
        CurrencyController.call(this, $scope, configService);
    }
]);
entityControllers.controller('AddMaterialsController', ['$scope', 'matService', 'entityService', 'domainCfgService', 'requestContext', '$location', 'OPTIMIZER',
    function ($scope, matService, entityService, domainCfgService, requestContext, $location, OPTIMIZER) {
        $scope.wparams = [["tag", "tag"], ["o", "offset"], ["s", "size"]];
        $scope.entityLabel = "";
        $scope.selectAll = function (newval) {
            for (var item in $scope.filtered) {
                $scope.filtered[item]['selected'] = newval;
            }
        };
        $scope.isWatched = false;
        $scope.showLoading();
        domainCfgService.getInventoryCfg().then(function (data) {
            $scope.invCfg = data.data;
        }).catch(function error(msg) {
            $scope.showErrorMsg(msg);
        }).finally(function (data){
            $scope.hideLoading();
        });
        $scope.showLoading();
        domainCfgService.getOptimizerCfg().then(function (data) {
            $scope.isEOQ = data.data.compute == OPTIMIZER.compute.EOQ;
        }).catch(function error(msg) {
            $scope.showErrorMsg(msg);
        }).finally(function (data){
            $scope.hideLoading();
        });
        $scope.showLoading();
        domainCfgService.getMaterialTagsCfg().then(function (data) {
            $scope.tags = data.data.tags;
        }).catch(function error(msg) {
            $scope.showErrorMsg(msg);
        }).finally(function (data){
            $scope.hideLoading();
        });
        $scope.getInvModel = function (invModel) {
            if (checkNullEmpty(invModel)) {
                return $scope.resourceBundle['inventorypolicy.none'];
            } else {
                return $scope.resourceBundle['inventorypolicy.' + invModel];
            }
        };
        $scope.back = function () {
            $scope.tag = "";
            $scope.add = false;
            $scope.edit = false;
            $scope.success = false;
            $scope.selAll = false;
            if ($scope.isAdd) {
                $scope.isAdd = false;
                $scope.fetch();
            } else if ($scope.isBulk) {
                $location.path('/setup/entities');
            } else if ($scope.isEdit) {
                $location.path('/setup/entities/detail/' + $scope.entity.id + '/materials');
                $scope.isEdit = false;
            } else {
                $scope.fetch();
            }
        };
        $scope.isInit = false;
        $scope.init = function () {
            $scope.materials = {};
            $scope.exRow = [];
            $scope.expand = [];
            $scope.search = {mnm: ""};
            $scope.tag = requestContext.getParam("tag") || "";
            if (!$scope.isInit) {
                $scope.entities = [];
                var eids = requestContext.getParam("eids");
                if (checkNotNullEmpty(eids)) {
                    $scope.entities = requestContext.getParam("eids").split(",");
                    $scope.isBulk = true;
                } else if (checkNullEmpty($scope.matListType)) {
                    $scope.isBulk = true;
                } else {
                    $scope.isBulk = false;
                }
                $scope.selAll = false;
                $scope.serviceLevels = [];
                for (var i = 99; i >= 65; i--) {
                    $scope.serviceLevels.push(i);
                }
            }
            if($scope.entities == 0){
                $scope.entityLabel = "all";
            }else {
                $scope.entityLabel = $scope.entities.length;
            }
            $scope.isInit = true;
        };
        $scope.init();
        ListingController.call(this, $scope, requestContext, $location);
        $scope.matListType = $scope.matListType || "all";
        $scope.materials;
        $scope.filtered;
        $scope.tags;
        $scope.success = false;
        $scope.loading = false;
        $scope.inv = {};
        $scope.loadEntityMaterials = function (entityId) {
            $scope.loading = true;
            $scope.showLoading();
            entityService.getMaterials(entityId, $scope.tag,$scope.offset,$scope.size).then(function (data) {
                var entMaterials = data.data.results;
                if ($scope.isBulk || $scope.isAdd) {
                    if (checkNotNull(entMaterials)) {
                        var mMap = {};
                        entMaterials.forEach(function (material) {
                            mMap[material.mId] = material;
                        });
                        var finalMaterials = [];
                        if (checkNotNull($scope.materials.results)) {
                            $scope.materials.results.forEach(function (material) {
                                var entMat = mMap["" + material.mId];
                                var sno;
                                if (checkNotNull(entMat)) {
                                    sno = material.sno;
                                    material = entMat;
                                    material.isAdded = true;
                                    material.sno = sno;
                                } else {
                                    material.isAdded = false;
                                    material.im = $scope.entity.inv;
                                    material.sl = $scope.entity.sl;
                                }
                                finalMaterials.push(material);
                            });
                        }
                        $scope.materials.results = finalMaterials;
                    }
                } else {
                    entMaterials.forEach(function (material) {
                        material.isAdded = true;
                    });
                    $scope.materials = data.data;
                    $scope.setResults($scope.materials);
                }
            }).catch(function error(msg) {
                $scope.setResults(null);
                $scope.showErrorMsg(msg);
            }).finally(function(){
                $scope.loading = false;
                $scope.hideLoading();
            });
        };
        $scope.loadEntity = function () {
            var isEntity = checkNotNull($scope.entity);
            if (isEntity || $scope.entities.length == 1) {
                var entityId = isEntity ? $scope.entity.id : $scope.entities[0];
                if (!isEntity) {
                    $scope.showLoading();
                    entityService.get(entityId).then(function (data) {
                        $scope.entity = data.data;
                        //$scope.loadEntityMaterials(entityId);
                    }).catch(function error(msg) {
                        $scope.showErrorMsg(msg);
                        $scope.loading = false;
                    }).finally(function(){
                        $scope.hideLoading();
                    });
                } else {
                    //$scope.loadEntityMaterials(entityId);
                }
            }
        };
        $scope.loadDomainMaterials = function () {
            $scope.showLoading();
            var checkEntityId = undefined;
            if (checkNotNull($scope.entity) || (checkNotNull($scope.entities) && $scope.entities.length == 1)) {
                checkEntityId = checkNotNull($scope.entity) ? $scope.entity.id : $scope.entities[0];
                $scope.loadEntity();
            }
            matService.getDomainMaterials(null,$scope.tag, $scope.offset, $scope.size,checkEntityId).then(function (data) {
                $scope.materials = data.data;
                var i = 0;
                for (var material in $scope.materials.results) {
                    var matItem = $scope.materials.results[material];
                    if (checkNullEmpty(matItem.reord)) {
                        matItem.reord = 0;
                    }
                    if (checkNullEmpty(matItem.max)) {
                        matItem.max = 0;
                    }
                    if (checkNullEmpty(matItem.crMnl)) {
                        matItem.crMnl = 0;
                    }
                    if(matItem.b == 'yes'){
                        matItem.be = true;
                    }
                    if(matItem.dty == 'bn'){
                        matItem.b = 'yes';
                    }else{
                        matItem.b = 'no';
                    }

                    if (checkNullEmpty(matItem.rp)) {
                        matItem.rp = 0;
                    }
                    if (checkNullEmpty(matItem.tx)) {
                        matItem.tx = 0;
                    }
                    if(!matItem.isAdded && matItem.im == 'sq'){
                        matItem.sl = 85;
                    }
                    $scope.expand[i++] = false;
                }
                $scope.setResults($scope.materials);
                $scope.loading = false;
            }).catch(function error(msg) {
                $scope.setResults(null);
                $scope.loading = false;
                $scope.showErrorMsg(msg);
            }).finally(function(){
                $scope.hideLoading();
            });
        };
        $scope.fetch = function () {
            $scope.loading = true;
            $scope.selAll = false;
            if ($scope.isBulk || $scope.isAdd) {
                $scope.loadDomainMaterials();
            } else {
                if (checkNotNullEmpty($scope.entity)) {
                    $scope.entities = [$scope.entity.id];
                    $scope.loadEntityMaterials($scope.entity.id);
                } else {
                    if (!$scope.isWatched) {
                        $scope.isWatched = true;
                        $scope.$watch("entity", function () {
                            $scope.fetch();
                        });
                    }
                }
            }
        };
        $scope.fetch();
        $scope.refresh = function(){
            $scope.init();
            $scope.fetch();
        };
        $scope.addNewMaterials = function () {
            $scope.loading = true;
            $scope.isAdd = true;
            $scope.tag = "";
            $scope.loadDomainMaterials();
            $scope.exRow = [];
            $scope.expand = [];
        };
        $scope.editMaterials = function () {
            $scope.expand = [];
            $scope.exRow = [];
            var i = 0;
            $scope.materials.results.forEach(function (item) {
                if (item.selected) {
                    item.isAdded = false;
                    i++;
                }
            });
            if (i == 0) {
                $scope.showWarning($scope.resourceBundle['filters.message']);
                $scope.isEdit = false;
            } else {
                $scope.isEdit = true;
            }
        };
        $scope.saveEditMaterials = function () {
            var materials = [];
            for (var item in $scope.filtered) {
                if ($scope.filtered[item].selected) {
                    $scope.filtered[item] = $scope.correctMaterialData($scope.filtered[item]);
                    if(Number($scope.filtered[item].reord) != 0 && Number($scope.filtered[item].max) != 0 && (Number($scope.filtered[item].reord) >= Number($scope.filtered[item].max))) {
                        $scope.showWarning("Min is greater than or equal to max");
                        return;
                    }
                    materials.push($scope.filtered[item]);
                }
            }
            if (materials.length == 0) {
                $scope.showWarning($scope.resourceBundle['filters.message']);
                return;
            }
            $scope.loading = true;
            $scope.showLoading();
            entityService.editMaterials($scope.entity.id, materials).then(function (data) {
                $scope.edit = true;
                $scope.add = false;
                $scope.isEdit = false;
                $scope.showSuccess($scope.resourceBundle['order.mat.success']);
                $scope.back();
            }).catch(function error(msg) {
                $scope.showErrorMsg(msg);
            }).finally(function (){
                $scope.loading = false;
                $scope.hideLoading();
            });
        };
        $scope.cancelEditMaterials = function () {
            $scope.materials.results.forEach(function (item) {
                item.isAdded = true;
            });
            $scope.isEdit = false;
            $scope.refresh();
        };
        $scope.removeMaterials = function () {
            var materials = [];
            for (var item in $scope.filtered) {
                if ($scope.filtered[item].selected) {
                    materials.push($scope.filtered[item]);
                }
            }
            if (materials.length == 0) {
                $scope.showWarning($scope.resourceBundle['selectitemtoremovemsg']);
            } else {
                if (!confirm($scope.resourceBundle.removematerialfromentityconfirmmsg)) {
                    return;
                }
                $scope.loading = true;
                $scope.showLoading();
                entityService.removeMaterials(materials, $scope.entities).then(function (data) {
                    $scope.add = false;
                    $scope.edit = false;
                    $scope.materialNum = materials.length;
                    $scope.entityNum = $scope.entities.length;
                    $scope.loading = false;
                    if(data.data.indexOf("Partially") == 1){
                        $scope.showWarning(data.data);
                    } else {
                        $scope.showSuccess(data.data);
                    }
                    $scope.back();
                }).catch(function error(msg) {
                    $scope.loading = false;
                    $scope.showErrorMsg(msg);
                }).finally(function(){
                    $scope.hideLoading();
                });
            }
        };
        $scope.check = false;
        $scope.addMaterialsToEntity = function () {
            var materialList = [];
            var numMatSelected = 0;
            for (var item in $scope.filtered) {
                if($scope.maxInValid.length > 0 && $scope.maxInValid[item] == true) {
                    $scope.showWarning("Min cannot be greater than or equal to Max.");
                    return;
                } else if($scope.filtered[item].selected){
                    numMatSelected++;
                    if(!$scope.filtered[item].isAdded){
                        materialList.push($scope.correctMaterialData($scope.filtered[item]));
                    }
                }
            }
            if (materialList.length == 0 && numMatSelected == 0) {
                $scope.showWarning($scope.resourceBundle['selectitemtoaddmsg']);
            } else if(materialList.length == 0 && numMatSelected > 0){
                $scope.showWarning($scope.resourceBundle['material.already.added']);
            }else {
                $scope.loading = true;
                $scope.showLoading();
                entityService.addMaterials(materialList, $scope.entities, $scope.check).then(function (data) {
                    $scope.add = true;
                    $scope.edit = false;
                    $scope.entityNum = $scope.entities.length;
                    $scope.materialNum = materialList.length;
                    $scope.isAdd = false;
                    if(checkNotNullEmpty($scope.offset)){
                        $scope.offset = 0;
                    }
                    if(data.data.indexOf("Added ") == 1){
                        $scope.showWarning(data.data);
                    } else {
                        $scope.showSuccess(data.data)
                    }
                    $scope.back();
                }).catch(function error(msg) {
                    $scope.isAdd = false;
                    $scope.showErrorMsg(msg);
                }).finally(function (){
                    $scope.loading = false;
                    $scope.hideLoading();
                });
            }
        };
        $scope.maxInValid = [];
        $scope.isMinMaxInvalid = function (reord, max, index) {
            reord = parseInt(reord || 0, 10);
            max = parseInt(max || 0, 10);
            $scope.maxInValid[index] = (reord > max) || (reord > 0 && reord == max);
        };
        $scope.toggleTemperature = function (invItem) {
            invItem.showTemp = !invItem.showTemp;
        };
        $scope.toggle = function (index) {
            $scope.filtered[index].showTemp = false;
        };
        $scope.correctMaterialData = function(material){
            if(checkNullEmpty(material.reord))
                material.reord = 0;

            if(checkNullEmpty(material.max))
                material.max = 0;

            if(checkNullEmpty(material.rp))
                material.rp = 0;

            if(checkNullEmpty(material.crMnl))
                material.crMnl = 0;

            if(checkNullEmpty(material.tx))
                material.tx = 0;

            return material;
        };
        $scope.select = function (index) {
            $scope.expand[index] = !$scope.expand[index];
            $scope.exRow[index] = $scope.expand[index] ? 'history' : '';
        };
        $scope.toggle = function (index) {
            $scope.select(index);
        };
    }
]);
entityControllers.controller('RelationAddController', ['$scope', 'entityService', 'requestContext', '$location', function ($scope, entityService, requestContext, $location) {
    $scope.reset = function () {
        $scope.rel = {};
        $scope.rel.linkType = "c";
        $scope.rel.entityId = $scope.entityId;
    };
    $scope.reset();
    $scope.wparams = [["o", "offset"], ["s", "size"]];
    ListingController.call(this, $scope, requestContext, $location);
    $scope.getCustomers = function (callback) {
        $scope.entityLinks = [];
        $scope.showLoading();
        entityService.getCustomers($scope.entityId).then(function (data) {
            $scope.entityLinks = data.data.results;
            $scope.selAll = false;
            if (typeof callback != 'undefined') {
                callback();
            }
        }).catch(function error(msg) {
            $scope.showErrorMsg(msg);
        }).finally(function() {
            $scope.hideLoading();
        });
    };
    $scope.getVendors = function (callback) {
        $scope.entityLinks = [];
        $scope.showLoading();
        entityService.getVendors($scope.entityId).then(function (data) {
            $scope.entityLinks = data.data.results;
            $scope.selAll = false;
            if (typeof callback != 'undefined') {
                callback();
            }
        }).catch(function error(msg) {
            $scope.showErrorMsg(msg);
        }).finally(function() {
            $scope.hideLoading();
        });
    };
    $scope.fetchLinks = function () {
        $scope.loading = true;
        if ($scope.rel.linkType === "c") {
            $scope.getCustomers($scope.resetLinks);
        } else if ($scope.rel.linkType === "v") {
            $scope.getVendors($scope.resetLinks);
        }
    };
    $scope.setLink = function () {
        if(checkNotNullEmpty($scope.filtered)){
            $scope.filtered.forEach(function (item) {
                if ($scope.entityId == item.id) {
                    item['linked'] = true;
                    $scope.entityName = item.nm;
                } else {
                    if(checkNotNullEmpty($scope.entityLinks)) {
                        $scope.entityLinks.some(function (link) {
                            if (link.id === item.id) {
                                item['linked'] = true;
                                return true;
                            }
                        });
                    }
                }
            });
        }
    };
    $scope.resetLinks = function () {
        $scope.filtered = angular.copy($scope.entities);
        $scope.setLink();
        $scope.loading = false;
        $scope.enm = undefined;
        $scope.eTag = undefined;
    };
    $scope.fetch = function () {
        $scope.loading = true;
        $scope.showLoading();
        entityService.getAll($scope.offset, $scope.size, $scope.tag, null, null, null, $scope.linkedEntityId).then(function (data) {
            $scope.entities = data.data.results;
            $scope.filtered = angular.copy($scope.entities);
            $scope.setResults(data.data);
            $scope.setLink();
            $scope.loading = false;
        }).catch(function error(msg) {
            $scope.setResults(null);
            $scope.loading = false;
            $scope.showErrorMsg(msg);
        }).finally(function(){
            $scope.hideLoading();
        });
    };
    $scope.init = function () {
        $scope.getCustomers($scope.fetch);
    };
    $scope.init();
    $scope.selectAll = function (newval) {
        $scope.filtered.forEach(function (item) {
            if (!item.linked) {
                item['selected'] = newval;
            }
        });
    };
    $scope.$watch("enm", function(newVal, oldVal) {
        if(newVal != oldVal) {
            if(checkNullEmpty(newVal)) {
                $scope.linkedEntityId = undefined;
                $scope.offset = 0;
            } else {
                $scope.linkedEntityId = $scope.enm.id;
                $scope.eTag = $scope.tag = undefined;
            }
            $scope.fetch();
        }
    });
    $scope.$watch("eTag", function (newVal, oldVal) {
       if(newVal != oldVal) {
           if(checkNullEmpty(newVal)) {
               $scope.tag = undefined;
               $scope.offset = 0;
           } else {
               $scope.tag = $scope.eTag;
               $scope.enm = $scope.linkedEntityId = undefined;
           }
           $scope.fetch();
       }
    });
    $scope.resetFilter = function() {
        $scope.enm = undefined;
        $scope.eTag = undefined;
    };
    $scope.addEntityLinks = function (linkIds) {
        if(checkNullEmpty($scope.entityLinks)) {
            $scope.entityLinks = [];
        }
        linkIds.forEach(function (linkid) {
            $scope.entityLinks.push({id: linkid});
        });
    };
    $scope.update = function () {
        var linkedIds = [];
        $scope.filtered.forEach(function (entity) {
            if (entity.selected) {
                linkedIds.push(entity.id);
            }
        });
        if (linkedIds.length === 0) {
            $scope.showWarning($scope.resourceBundle['select.entity.relation.add']);
            return;
        }
        $scope.rel.linkIds = linkedIds;
        $scope.showLoading();
        entityService.addRelation($scope.rel).then(function (data) {
            $scope.addEntityLinks($scope.rel.linkIds);
            $scope.reset();
            $scope.resetLinks();
            $scope.showSuccess(data.data)
        }).catch(function error(msg) {
            $scope.showErrorMsg(msg);
        }).finally(function (){
            $scope.hideLoading();
        });
    }
}]);
entityControllers.controller('RelationListController', ['$rootScope','$scope', 'entityService', 'domainCfgService', 'requestContext', '$location','$window', function ($rootScope,$scope, entityService, domainCfgService, requestContext, $location,$window) {
    ListingController.call(this, $scope, requestContext, $location);
    $scope.linkType = 'c';
    $scope.rData = undefined;
    $scope.loading = false;
    $scope.searchkey = undefined;
    $scope.mSize = requestContext.getParam("s") || 50;
    var size = $scope.mSize;
    var offset = 0;
    function setPageParams() {
        if($scope.mSize != $scope.size){
            $scope.mSize = $scope.size;
            size = $scope.mSize;
            $scope.mOffset = 0;
            $scope.rData = undefined;
        }
        if(checkNotNullEmpty($scope.rData)){
            offset = parseInt(offset) + parseInt(size);
        }
        if(checkNotNullEmpty($scope.allRTags)){
            size = undefined;
            offset = undefined;
        }
    }
    $scope.getCustomers = function () {
        $scope.showLoading();
        setPageParams();
        entityService.getCustomers($scope.entityId,size,offset, $scope.searchkey, $scope.linkedEntityId, $scope.entityTag).then(function (data) {
            setData(data.data);
        }).catch(function error(msg) {
            $scope.showErrorMsg(msg);
        }).finally(function(){
            searchCount = 0;
            $scope.searchRelations = true;
            $scope.hideLoading();
        });
    };
    $scope.getVendors = function () {
        $scope.showLoading();
        setPageParams();
        entityService.getVendors($scope.entityId,size,offset,$scope.searchkey, $scope.linkedEntityId, $scope.entityTag).then(function (data) {
            setData(data.data);
        }).catch(function error(msg) {
            $scope.showErrorMsg(msg);
        }).finally(function(){
            searchCount = 0;
            $scope.searchRelations = true;
            $scope.hideLoading();
        });
    };
    $scope.setOriginalCopy = function(data,resetAll) {
        $scope.orData = angular.copy(data);
        if(resetAll){
            $scope.rData = angular.copy(data);
        }
        $scope.$broadcast("orDataChanged");
    };
    function setData(data) {
        $scope.countTags = {};
        if (checkNotNullEmpty(data.results)) {
            var append = false;
            if (checkNotNullEmpty($scope.rData)) {
                $scope.rData = angular.copy($scope.rData).concat(data.results);
                append = true;
            } else {
                $scope.rData = data.results;
            }
            $scope.setResults(data);
            if ($scope.rData != undefined) {
                addOrderSno($scope.rData);
            }
            if (append) {
                appendOriginalCopy(data.results);
            } else {
                $scope.setOriginalCopy($scope.rData);
            }
            $scope.getFilteredData();
        } else {
            $scope.filtered = undefined;
            $scope.setResults(null);
        }
    }
    function appendOriginalCopy(data) {
        $scope.orData = $scope.orData.concat(data);
        addOrderSno($scope.orData,true);
        $scope.$broadcast("orDataAppend");
    }
    function addOrderSno(rData,isOrig) {
        setSnoByTags(rData,isOrig);
        rData.forEach(function (data) {
            if(!isOrig && data.osno == 0) {
                data.osno = data.sno;
            }
        });
        if(!isOrig){
            setCountByTags();
        }
    }
    function checkTagExist(tag) {
        return $scope.allRTags.some(function (data) {
            return tag == data;
        });
    }
    function setSnoByTags(rData,isOrig) {
        var tagIndex = {};
        var isEmptyRoot = checkNullEmpty($scope.allRTags);
        rData.forEach(function (data) {
            if(isEmptyRoot || !checkTagExist(data.rt)) {
                data.rt = '--notag--';
            }
            if (checkNullEmpty(tagIndex[data.rt])) {
                tagIndex[data.rt] = 1;
            }
            if(!isOrig && data.osno == 0) {
                data.sno = tagIndex[data.rt];
            }
            tagIndex[data.rt] = tagIndex[data.rt] + 1;
        });
    }
    function setCountByTags() {
        $scope.countTags = {};
        $scope.rData.forEach(function (data) {
            var rt = checkNullEmpty(data.rt) ? "--notag--" : data.rt;
            if (checkNullEmpty($scope.countTags[rt])) {
                $scope.countTags[rt] = 1;
            } else {
                $scope.countTags[rt] = $scope.countTags[rt] + 1;
            }
        });
        if($scope.countTags['--notag--'] > 0){
            $scope.tag = '--notag--';
        } else {
            if(checkNotNullEmpty($scope.allRTags)) {
                $scope.allRTags.some(function (rt) {
                    if (checkNotNullEmpty($scope.countTags[rt])) {
                        $scope.tag = rt;
                        return true;
                    }
                });
            }
        }
    }
    $scope.init = function (isFirstInit) {
        $scope.vw = requestContext.getParam("vw") || 't';
        $scope.tag = '--notag--';
        $scope.showLoading();
        domainCfgService.getRouteTagsCfg().then(function (data) {
            $scope.allRTags = data.data.tags;
            if(isFirstInit) {
                if(requestContext.getParam("o") > 0){
                    $scope.offset = 0;
                } else {
                    $scope.fetch();
                }
            }
        }).catch(function error(msg) {
            $scope.showErrorMsg(msg);
        }).finally(function(){
            $scope.hideLoading();
        });
        $scope.getKioskLinkCounts();
    };
    $scope.checkPermission = function(data) {
        return !(checkNotNullEmpty(data) && data > 0);
    };

    $scope.getKioskLinkCounts = function() {
        $scope.showLoading();
        entityService.getLinksCount($scope.entityId,$scope.searchkey,$scope.linkedEntityId,$scope.entityTag).then(function (data) {
            var counts = data.data.replace(/"/g, "").split(",");
            if(checkNullEmpty($scope.entityTag) && checkNullEmpty($scope.searchkey)) {
                $scope.customerCount = counts[0];
                $scope.vendorCount = counts[1];
            } else if(checkNotNullEmpty($scope.searchkey) || checkNotNullEmpty($scope.entityTag)) {
                if($scope.linkType == 'c') {
                    $scope.customerCount = counts[0];
                } else if($scope.linkType == 'v') {
                    $scope.vendorCount = counts[1];
                }
            }
        }).catch(function error(msg) {
            $scope.showErrorMsg(msg);
        }).finally(function(){
            $scope.hideLoading();
        });
    };
    $scope.init(true);
    $scope.resetFetch = function(){
        $scope.rData = undefined;
        $scope.mOffset = 0;
        $scope.filtered = [];
        $scope.eTag = $scope.entityTag = $scope.searchkey = undefined;
        if ($scope.offset == 0) {
            $scope.fetch();
        }
        $scope.offset = 0;
        offset = 0;
    };
    $scope.fetch = function() {
        searchCount++;
        $scope.hideDeleteRel = false;
        if ($scope.linkType === "c") {
            $scope.getCustomers();
            if($scope.iMan && checkNotNullEmpty($scope.cnff.et)){
                if(!$scope.cnff.enableCs){
                    $scope.hideDeleteRel = true;
                }
            }
        } else if ($scope.linkType === "v") {
            $scope.getVendors();
            if($scope.iMan && checkNotNullEmpty($scope.cnff.et)){
                if(!$scope.cnff.enableVs){
                    $scope.hideDeleteRel = true;
                }
            }
        }
        $scope.getKioskLinkCounts();
    };
    $scope.fetchLinks = function (type) {
        if($scope.linkType != type) {
            $scope.linkType = type;
            $scope.resetFetch();
        }
    };
    $scope.removeEntityRelation = function () {
        var linkIds = [];
        $scope.rData.forEach(function (data) {
            if (data.selected) {
                linkIds.push(data.lid);
            }
        });
        if(linkIds.length == 0){
            $scope.showWarning($scope.resourceBundle['entity.remove.select'] + ".");
            return;
        }
        if (!confirm($scope.resourceBundle.removerelationconfirmmsg + '?')) {
            return;
        }
        $scope.showLoading();
        entityService.removeEntityRelation(linkIds).then(function (data) {
            $scope.resetFetch();
            $scope.showSuccess(data.data);
        }).catch(function error(msg) {
            $scope.showErrorMsg(msg);
        }).finally(function(){
            $scope.hideLoading();
        });
    };
    $scope.switchVw = function(sourceVw) {
        if (confirm($scope.resourceBundle['changes.lost'])) {
            $scope.rData = angular.copy($scope.orData);
        } else {
            $scope.vw = sourceVw;
        }
    };
    var searchCount = 0;
    $scope.searchData = function() {
       if(checkNotNullEmpty($scope.searchkey)) {
           $scope.eTag = $scope.entityTag = undefined;
       } else {
           $scope.searchkey = undefined;
           offset = 0;
       }
        searchCount++;
        $scope.searchEntity();
    };
    $scope.$watch("eTag", function(newVal, oldVal) {
        if(newVal != oldVal) {
            if(checkNullEmpty(newVal)) {
                $scope.entityTag = undefined;
                offset = 0;
            } else {
                $scope.entityTag = newVal;
                $scope.searchkey = undefined;
            }
            searchCount++;
            $scope.searchEntity();
        }
    });
    $scope.$watch("tag", function(newVal, oldVal) {
        if(newVal != oldVal) {
            $scope.getFilteredData();
        }
    });

    $scope.searchEntity = function() {
        $scope.rData = undefined;
        if ( checkNullEmpty($scope.searchkey) ) {
            $scope.searchKey = undefined;
            $scope.hideReorder = false;
        } else {
            $scope.hideReorder = true;
        }
        if(searchCount < 2) {
            if ($scope.linkType == 'c') {
                $scope.getCustomers();
            } else {
                $scope.getVendors();
            }
            $scope.getKioskLinkCounts();
        }
    };
    $scope.goToDetail = function(nv){
        $window.open("#/setup/entities/detail/" + nv.id,'_blank');
    };
    $scope.filtered = [];

    $scope.getFilteredData= function () {
        $scope.filtered = [];
        angular.forEach($scope.rData,function(data){
            if(data.rt == $scope.tag) {
                $scope.filtered.push(data);
            }
        });
        sortByKey($scope.filtered,'osno');
        $scope.numFound = $scope.filtered.length;
    };
}]);

entityControllers.controller('EntityApproversController',['$scope','entityService',
    function($scope,entityService){
        $scope.init = function(){
            $scope.eapr = {pap:[], sap:[], pas:[], sas:[]};
        };

        $scope.getFilteredUsers = function() {

            if(checkNotNullEmpty($scope.eapr.pap)) {
                var pap = $scope.eapr.pap;
                $scope.eapr.pap = [];
                for(var i=0; i< pap.length; i++) {
                    $scope.eapr.pap.push({"id": pap[i].id, "text": pap[i].fnm+' ['+pap[i].id+']'});
                }
            }

            if(checkNotNullEmpty($scope.eapr.sap)) {
                var sas = $scope.eapr.sap;
                $scope.eapr.sap = [];
                for(var i=0; i< sas.length; i++) {
                    $scope.eapr.sap.push({"id": sas[i].id, "text": sas[i].fnm+' ['+sas[i].id+']'});
                }
            }

            if(checkNotNullEmpty($scope.eapr.pas)) {
                var pap = $scope.eapr.pas;
                $scope.eapr.pas = [];
                for(var i=0; i< pap.length; i++) {
                    $scope.eapr.pas.push({"id": pap[i].id, "text": pap[i].fnm+' ['+pap[i].id+']'});
                }
            }

            if(checkNotNullEmpty($scope.eapr.sas)) {
                var sas = $scope.eapr.sas;
                $scope.eapr.sas = [];
                for(var i=0; i< sas.length; i++) {
                    $scope.eapr.sas.push({"id": sas[i].id, "text": sas[i].fnm+' ['+sas[i].id+']'});
                }
            }
        };

        function validateApprovers() {
            if($scope.ipa && (checkNullEmpty($scope.eapr.pap))) {
                $scope.showWarning("Primary approvers not configured for purchases order.");
                $scope.continue = false;
                return;
            }
            if($scope.isa && (checkNullEmpty($scope.eapr.pas))) {
                $scope.showWarning("Primary approvers are not configured for sales order.");
                $scope.continue = false;
                return;
            }
        }

        $scope.getApprovers = function() {
            if(checkNotNullEmpty($scope.entityId)) {
                $scope.showLoading();
                entityService.getApprovers($scope.entityId).then(function(data) {
                    $scope.eapr = data.data;
                    $scope.getFilteredUsers($scope.eapr);
                }).catch(function error(msg) {
                    $scope.showError(msg);
                }).finally(function() {
                    $scope.hideLoading();
                })
            }
        };
        $scope.getApprovers();

        $scope.setApprovers = function() {
            $scope.continue = true;
            if(checkNotNullEmpty($scope.entityId)) {
                validateApprovers();
                $scope.getFilteredUsers();
                if($scope.continue) {
                    $scope.eapr.entityId = $scope.entityId;
                    $scope.showLoading();
                    entityService.setApprovers($scope.eapr).then(function (data) {
                        $scope.showSuccess(data.data);
                    }).catch(function error(msg) {
                        $scope.showErrorMsg(msg);
                    }).finally(function(){
                        $scope.hideLoading();
                        $scope.getApprovers();
                    });
                }
            }
        }
    }]);

entityControllers.controller('RelationPermissionController',['$scope','entityService',
    function($scope,entityService){
        $scope.perm = {};
        $scope.getPermission = function() {
            $scope.showLoading();
            entityService.getPermission($scope.entityId).then(function(data){
                $scope.perm = data.data;
                $scope.perm.cPerm = $scope.perm.cPerm.toString();
                $scope.perm.vPerm = $scope.perm.vPerm.toString();
            }).catch(function error(msg) {
                $scope.showErrorMsg(msg)
            }).finally(function() {
                $scope.hideLoading();
            });
        };
        $scope.getPermission();
        $scope.setPermission = function() {
            $scope.perm.eid = $scope.entityId;
            $scope.showLoading();
            entityService.setPermission($scope.perm).then(function(data){
                $scope.showSuccess(data.data);
            }).catch(function error(msg) {
                $scope.showErrorMsg(msg)
            }).finally(function() {
                $scope.hideLoading();
            });
        };
    }
]);
entityControllers.controller('RelationListTableController', ['$rootScope','$scope', 'entityService', function ($rootScope,$scope, entityService) {
    $scope.selectAll = function (newVal) {
        if(checkNotNullEmpty($scope.filtered)) {
            $scope.filtered.forEach(function (item) {
                item.selected = newVal;
            });
        }
    };
    function resetOrderSno() {
        var index = 1;
        if(checkNotNullEmpty($scope.filtered)) {
            $scope.filtered.forEach(function (data) {
                data.osno = index++;
                data.ri = data.osno;
            });
        }
    }
    $scope.resetAllEditFlags = function () {
        $scope.filtered.forEach(function (data) {
            data.cedit = false;
            data.dedit = false;
            data.redit = false;
        });
    };
    //Re-Order
    $scope.setPosition = function (index) {
        $scope.resetAllEditFlags();
        $scope.filtered[index].esno = '';
        $scope.filtered[index].redit = true;
    };
    $scope.updatePosition = function (index) {
        $scope.filtered[index].redit = false;
        var no = $scope.filtered[index].esno;
        if (checkNotNullEmpty(no)) {
            var nIndex = parseInt(no) - 1;
            if (nIndex >= $scope.filtered.length) {
                $scope.moveToPos(index, $scope.filtered.length - 1);
            } else if (nIndex <= 0) {
                $scope.moveToPos(index, 0);
            } else {
                $scope.moveToPos(index, nIndex);
            }
            $scope.routeUpdated = true;
        }
    };
    $scope.cancelPosition = function (index) {
        $scope.filtered[index].redit = false;
    };
    //Description
    $scope.setDesc = function (index) {
        $scope.resetAllEditFlags();
        $scope.filtered[index].edesc = $scope.filtered[index].desc;
        $scope.filtered[index].dedit = true;
    };
    $scope.updateDesc = function (index) {
        $scope.filtered[index].dedit = false;
        var relationData = {
            entityId: $scope.entityId,
            linkIds: [$scope.filtered[index].lid],
            cl: $scope.filtered[index].cl,
            desc:  $scope.filtered[index].edesc,
            linkType: $scope.linkType
        };
        $scope.filtered[index].updatingEntityDesc = true;
        entityService.updateEntityRelation(relationData).then(function(){
            $scope.filtered[index].desc = $scope.filtered[index].edesc;
        }).catch(function error(msg) {
            $scope.showErrorMsg(msg);
        }).finally(function(){
            $scope.filtered[index].updatingEntityDesc = false;
        });
    };
    $scope.cancelDesc = function (index) {
        $scope.filtered[index].dedit = false;
    };
    //Credit Limit
    $scope.setCredit = function (index) {
        $scope.resetAllEditFlags();
        $scope.filtered[index].ecl = $scope.filtered[index].cl;
        $scope.filtered[index].cedit = true;
    };
    $scope.updateCredit = function (index) {
        $scope.filtered[index].cedit = false;
        var relationData = {
            entityId: $scope.entityId,
            linkIds: [$scope.filtered[index].lid],
            cl: $scope.filtered[index].ecl||0,
            desc:  $scope.filtered[index].desc,
            linkType: $scope.linkType
        };
        $scope.filtered[index].updatingEntityCredit = true;
        entityService.updateEntityRelation(relationData).then(function(){
            $scope.filtered[index].cl = $scope.filtered[index].ecl;
        }).catch(function error(msg) {
            $scope.showErrorMsg(msg);
        }).finally(function(){
            $scope.filtered[index].updatingEntityCredit = false;
        });
    };
    $scope.cancelCredit = function (index) {
        $scope.filtered[index].cedit = false;
    };
    $scope.reorder = function (index) {
        var newIndex = $scope.filtered[index].sno - 1;
        $scope.moveToPos(index, newIndex);
    };
    $scope.rowHighlight = function (index) {
        $scope.filtered[index].rhc = true;
        setTimeout(function () {
            $scope.$apply(function () {
                $scope.filtered[index].rhc = false;
            });
        }, 1500);
    };
    $scope.moveUp = function (index) {
        if (index > 0) {
            var t = $scope.filtered[index];
            $scope.filtered[index] = $scope.filtered[index - 1];
            $scope.filtered[index - 1] = t;
            $scope.rowHighlight(index - 1);
            resetOrderSno();
            $scope.routeUpdated = true;
        }
    };
    $scope.moveDown = function (index) {
        if (index < $scope.filtered.length - 1) {
            var t = $scope.filtered[index];
            $scope.filtered[index] = $scope.filtered[index + 1];
            $scope.filtered[index + 1] = t;
            $scope.rowHighlight(index + 1);
            resetOrderSno();
            $scope.routeUpdated = true;
        }
    };
    $scope.moveToPos = function (index, newIndex) {
        if (index > newIndex) { //move up
            var t = $scope.filtered[index];
            for (var i = index; i > newIndex; i--) {
                $scope.filtered[i] = $scope.filtered[i - 1];
            }
            $scope.filtered[newIndex] = t;
        } else { //move down
            t = $scope.filtered[index];
            for (i = index; i < newIndex; i++) {
                $scope.filtered[i] = $scope.filtered[i + 1];
            }
            $scope.filtered[newIndex] = t;
        }
        $scope.rowHighlight(newIndex);
        resetOrderSno();
    };
    $scope.updateEntityOrder = function () {
        var isRTAvailable = checkNotNullEmpty($scope.allRTags);
        $scope.showLoading();
        entityService.updateEntityOrder($scope.rData, $scope.linkType, $scope.entityId, isRTAvailable).then(function (data) {
            //setSnoByOrderSno();
            $scope.routeUpdated = false;
            //$scope.setOriginalCopy($scope.rData);
            $scope.resetFetch();
            $scope.showSuccess(data.data);
        }).catch(function error(msg) {
            $scope.showErrorMsg(msg);
        }).finally(function(){
            $scope.hideLoading();
        });
    };
    $scope.$watch("vw", function (newVal) {
        if(newVal == 'm' && $scope.routeUpdated) {
            $scope.switchVw('t');
        }
    });
}]);
entityControllers.controller('EntityRouteMapCtrl', ['$scope', 'mapService', 'entityService','domainCfgService','invService','trnService',
    function ($scope, mapService, entityService, domainCfgService, invService, trnService) {
        $scope.mrData = angular.copy($scope.orData);
        function trimData(data) {
            var d = {};
            d.lt = data.lt;
            d.ln = data.ln;
            d.nm = data.nm;
            d.loc = data.loc;
            d.sno = data.sno;
            d.osno = data.osno;
            d.id = data.id;
            d.ri = data.ri;
            d.rt = data.rt;
            return d;
        }
        function trimNoGeoEntity() {
            $scope.noGeoData = [];
            var noGeoCnt = [];
            var lmrData = [];
            $scope.mrData.forEach(function(data) {
                data = trimData(data);
                if(data.lt == 0 && data.ln == 0){
                    $scope.noGeoData.push(data);
                    if(checkNullEmpty(noGeoCnt[data.rt])){
                        noGeoCnt[data.rt] = 1;
                    }else{
                        noGeoCnt[data.rt] += 1;
                    }
                } else {
                    if(checkNotNullEmpty(noGeoCnt[data.rt])) {
                        data.osno = data.osno - noGeoCnt[data.rt];
                    }
                    lmrData.push(data);
                }
            });
            $scope.mrData = lmrData;
        }
        trimNoGeoEntity();
        $scope.lmap = angular.copy($scope.map);
        $scope.lmap.options = {scrollwheel: false};
        $scope.lmap.control = {};
        var lineCoordinates = [];
        var lineCoordinatesIndex = [];
        var lineSymbol = undefined;
        $scope.today = new Date();
        $scope.addLineCoord = function (marker,action) {
            if(!$scope.showActual && (checkNullEmpty($scope.allRTags) || $scope.tag != '--notag--')) {
                var markerCoord = marker.model.osno - 1;
                var ind = -1;
                var exists = lineCoordinatesIndex.some(function (coord) {
                    ind++;
                    return markerCoord === coord;
                });
                if (exists) {
                    var con = confirm($scope.resourceBundle['routingremovefromroutemsg']);
                    if (!con) {
                        return;
                    }
                    var sl = 1;
                    if (lineCoordinates[0] === lineCoordinates[1]) {
                        sl = 2;
                    }
                    lineCoordinates.splice(ind, sl);
                    $scope.mFiltered[lineCoordinatesIndex[ind]].icon = null;
                    lineCoordinatesIndex.splice(ind, sl);
                    if (lineCoordinates.length === 1) {
                        lineCoordinates.push(lineCoordinates[0]);
                        lineCoordinatesIndex.push(lineCoordinatesIndex[0]);
                    }
                } else {
                    marker.position = new google.maps.LatLng(marker.model.lt, marker.model.ln);
                    if (lineCoordinates.length === 0) {
                        lineCoordinates.push(marker.getPosition());
                        lineCoordinatesIndex.push(marker.model.osno - 1);
                    }
                    if (lineCoordinates[0] === lineCoordinates[1]) {
                        lineCoordinates.splice(1, 1);
                        lineCoordinatesIndex.splice(1, 1);
                    }
                    lineCoordinates.push(marker.getPosition());
                    lineCoordinatesIndex.push(marker.model.osno - 1);
                }
                $scope.setMarkerIcons();
                if (checkNotNullEmpty($scope.lmap.control.getGMap)) {
                    $scope.drawPolyline();
                }
                if (action == 'click') {
                    $scope.routeUpdated = true;
                }
            }
        };
        $scope.setMarkerIcons = function () {
            var ind = 1;
            if (lineCoordinates.length != 0 && (lineCoordinatesIndex[0] === lineCoordinatesIndex[1])) {
                $scope.mFiltered[lineCoordinatesIndex[0]].icon = mapService.getMarkerIcon(1);
            } else {
                lineCoordinatesIndex.forEach(function (index) {
                    $scope.mFiltered[index].icon = mapService.getMarkerIcon(ind++);
                });
            }
        };
        function clearPolyline(){
            if (checkNotNullEmpty($scope.pline)) {
                $scope.pline.setMap(null);
            }
        }
        $scope.drawPolyline = function () {
            clearPolyline();
            $scope.pline = new google.maps.Polyline({
                path: lineCoordinates,
                strokeColor: '#0000FF',
                icons: [{
                    icon: lineSymbol,
                    offset: '100%'
                }],
                map: $scope.lmap.control.getGMap()
            });
        };
        $scope.$on("orDataChanged", function () {
            $scope.mrData = angular.copy($scope.orData);
            trimNoGeoEntity();
            $scope.filterDataByTag($scope.tag);
        });
        $scope.$on("orDataAppend", function () {
            $scope.mrData = angular.copy($scope.orData);
            trimNoGeoEntity();
            updateOsno();
            $scope.filterDataByTag($scope.tag);
        });
        function updateOsno() {
            $scope.setOrderSno();
            $scope.mFiltered.forEach(function(mf){
                $scope.mrData.some(function(md){
                    if(md.id == mf.id){
                        md.osno = mf.osno;
                        return true;
                    }
                });
            });
        }
        $scope.filterDataByTag = function (newTag) {
            $scope.showMap = false;
            clearPolyline();
            var isActual = $scope.showActual;
            if($scope.showActual) {
                $scope.hideActualRoute();
            }
            $scope.mFiltered = [];
            $scope.mrData.forEach(function (data) {
                if (data.rt == newTag) {
                    $scope.mFiltered[data.osno - 1] = data;
                    $scope.mFiltered[data.osno - 1].options = {title: data.nm + ", " + data.loc};
                }
            });
            lineCoordinates = [];
            lineCoordinatesIndex = [];
            if(checkNullEmpty($scope.allRTags) || newTag != '--notag--') {
                $scope.mFiltered.forEach(function (data) {
                    if(data.ri != 2147483647) {
                        var lt = data.lt || 0;
                        var ln = data.ln || 0;
                        var marker = new google.maps.Marker({
                            position: new google.maps.LatLng(lt, ln),
                            model: {osno: data.osno,lt:lt,ln:ln}
                        });
                        $scope.addLineCoord(marker);
                    }
                });
            }
            if(isActual && $scope.mFiltered.length > 0) {
                addActualRoute();
            } else {
                mapService.convertLnLt($scope.mFiltered, $scope.lmap);
            }
            //For pagination
            $scope.filtered = $scope.mFiltered;
            $scope.numFound = $scope.$parent.numFound;
            setTimeout(function () {
                $scope.$apply(function () {
                    $scope.showMap = true;
                });
            }, 200);
        };
        $scope.setOrderSno = function (newVal, callback) {
            var ind = 1;
            if(lineCoordinates.length != 0 && (lineCoordinatesIndex[0] === lineCoordinatesIndex[1])){
                $scope.mFiltered[lineCoordinatesIndex[0]].osno = ind++;
                $scope.mFiltered[lineCoordinatesIndex[0]].ri = ind - 1;
            } else {
                lineCoordinatesIndex.forEach(function (index) {
                    $scope.mFiltered[index].osno = ind++;
                    $scope.mFiltered[index].ri = ind - 1;
                });
            }
            if (ind <= $scope.mFiltered.length) {
                var li = 0;
                $scope.mFiltered.forEach(function (data) {
                    if (lineCoordinatesIndex.indexOf(li++) == -1) {
                        data.osno = ind++;
                        data.ri = 2147483647;
                    }
                });
            }
            if (checkNotNullEmpty(callback)) {
                callback(newVal);
            }
        };
        $scope.$watch('lmap.control.getGMap', function (newVal, oldVal) {
            if (checkNotNull(newVal)) {
                if(checkNullEmpty(lineSymbol)) {
                    lineSymbol = mapService.getCloseArrowSymbol();
                }
                $scope.drawPolyline();
                if($scope.showActual) {
                    $scope.drawActualPolyline();
                }
            }
        });
        $scope.$watch('tag', function (newVal) {
            if(checkNullEmpty($scope.mFiltered)){
                $scope.filterDataByTag(newVal);
            }else {
                $scope.setOrderSno(newVal, $scope.filterDataByTag);
            }
        });
        $scope.updateEntityOrderViaMap = function () {
            var isRTAvailable = checkNotNullEmpty($scope.allRTags);
            $scope.update = function () {
                $scope.showLoading();
                entityService.updateEntityOrder($scope.mrData, $scope.linkType, $scope.entityId, isRTAvailable).then(function (data) {
                    $scope.routeUpdated = false;
                    $scope.resetFetch();
                    $scope.showSuccess(data.data);
                }).catch(function error(msg) {
                    $scope.showErrorMsg(msg);
                }).finally(function(){
                    $scope.hideLoading();
                });
            };
            $scope.setOrderSno('', $scope.update);
        };
        $scope.$watch("vw", function (newVal) {
            if(newVal == 't' && $scope.routeUpdated) {
                $scope.switchVw('m');
            }
        });
        domainCfgService.getRouteType().then(function(data){
            $scope.arType = data.data.replace(/"/g,'')
        });
        $scope.almap = angular.copy($scope.map);
        $scope.almap.options = {scrollwheel: false};
        $scope.almap.control = {};

        function addDaysToDate(date,days){
            return new Date(new Date().setDate(date.getDate() + days));
        }
        var actualLineCoordinates = [];
        var dt = new Date();
        $scope.from = addDaysToDate(dt,-7);
        $scope.to = dt;

        $scope.drawActualPolyline = function () {
            $scope.apline = new google.maps.Polyline({
                path: actualLineCoordinates,
                strokeColor: '#FF0000',
                icons: [{
                    icon: lineSymbol,
                    offset: '100%'
                }],
                map: $scope.lmap.control.getGMap()
            });
        };
        $scope.actualRouteStartIndex = undefined;
        var originLoc = undefined;
        var destinationLoc = undefined;
        var wayPoints = [];
        $scope.showActualRoute = function() {
            if(checkNullEmpty($scope.userId)){
                $scope.showWarning($scope.resourceBundle['user.actual.route']);
                return;
            }
            var service;
            if($scope.arType == 'orders') {
                service = invService
            } else {
                service = trnService;
            }
            service.getActualRoute($scope.userId, formatDate2Url($scope.from), formatDate2Url(addDaysToDate($scope.to, 1))).then(function (data) {
                $scope.actualRoute = data.data;
                addActualRoute(true);
            }).catch(function error(msg) {
                $scope.showErrorMsg(msg);
            });
        };
        function addActualRoute(redrawRoute) {
            $scope.actualRouteStartIndex = $scope.mFiltered.length;
            var ind = 1;
            for (var i = $scope.actualRoute.length - 1; i >= 0; i--) {
                var d = $scope.actualRoute[i];
                var location = new google.maps.LatLng(d.latitude, d.longitude);
                actualLineCoordinates.push(location);
                d.sno = 'a' + ind;
                d.icon = mapService.getMarkerIcon(ind++, "FFFF00");
                d.options = {title: d.title};
                $scope.mFiltered.push(d);
                if(i == $scope.actualRoute.length-1){
                    originLoc = location;
                }else if(i == 0){
                    destinationLoc = location;
                } else {
                    if(wayPoints.length < 8){
                        wayPoints.push( { location: location, stopover: true } );
                    }
                }
            }
            mapService.convertLnLt($scope.mFiltered, $scope.lmap);
            $scope.drawActualPolyline();
            $scope.showRoute = checkNotNullEmpty(originLoc) && checkNotNullEmpty(destinationLoc);
            if(redrawRoute) {
                if (checkNotNull($scope.almap.control.getGMap)) {
                    drawRoute();
                }
            }
            $scope.showActual = true;
        }
        $scope.$watch('almap.control.getGMap', function (newVal) {
            if (checkNotNull(newVal)) {
                drawRoute();
            }
        });
        function drawRoute () {
            $scope.distance = undefined;
            $scope.placeVisited = undefined;
            var directionsDisplay = new google.maps.DirectionsRenderer();
            directionsDisplay.setMap($scope.almap.control.getGMap());
            var directionsService = new google.maps.DirectionsService();
            if(checkNotNullEmpty(originLoc) && checkNotNullEmpty(destinationLoc)) {
                var request = {
                    origin: originLoc,
                    destination: destinationLoc,
                    waypoints: wayPoints,
                    travelMode: google.maps.TravelMode.DRIVING
                };
                directionsService.route(request, function (response, status) {
                    if (status == google.maps.DirectionsStatus.OK) {
                        directionsDisplay.setDirections(response);
                        var dist = 0;
                        response.routes[0].legs.forEach(function (data) {
                            dist += parseInt(data.distance.value);
                        });
                        $scope.distance = dist / 1000 + ' km(s)';
                        $scope.placeVisited = response.routes[0].legs.length + 1;
                    } else if (status == google.maps.DirectionsStatus.ZERO_RESULTS) {
                        $scope.showRoute = false;
                    }
                });
            }
        }
        $scope.hideActualRoute = function() {
            $scope.showActual = false;
            if (checkNotNullEmpty($scope.apline)) {
                $scope.apline.setMap(null);
            }
            $scope.mFiltered.splice($scope.actualRouteStartIndex,$scope.mFiltered.length - $scope.actualRouteStartIndex);
            $scope.actualRouteStartIndex = undefined;
            originLoc = undefined;
            destinationLoc = undefined;
            actualLineCoordinates = [];
            wayPoints = [];
        }
    }
]);
entityControllers.controller('EntityRelationEditController', ['$scope', 'entityService', 'domainCfgService', 'requestContext', '$location',
    function ($scope, entityService, domainCfgService, requestContext, $location) {
        $scope.linkType = 'c';
        $scope.leftFiltered = [];
        $scope.rightFiltered = [];
        function checkTagExist(tag){
            return $scope.allRTags.some(function (data) {
                return tag == data;
            });
        }
        function setSnoByTags() {
            var tagIndex = {};
            var isEmptyRoot = checkNullEmpty($scope.allRTags);
            $scope.rData.forEach(function (data) {
                if(isEmptyRoot || !checkTagExist(data.rt)) {
                    data.rt = '--notag--';
                }
                if (checkNullEmpty(tagIndex[data.rt])) {
                    tagIndex[data.rt] = 1;
                }
                data.sno = tagIndex[data.rt];
                tagIndex[data.rt] = tagIndex[data.rt] + 1;
            });
        }
        function addOrderSno() {
            setSnoByTags();
            $scope.rData.forEach(function (data) {
                data.osno = data.sno;
            });
            setCountByTags();
        }
        function setCountByTags() {
            $scope.countTags = {};
            $scope.rData.forEach(function (data) {
                var rt = checkNullEmpty(data.rt) ? "--notag--" : data.rt;
                if (checkNullEmpty($scope.countTags[rt])) {
                    $scope.countTags[rt] = 1;
                } else {
                    $scope.countTags[rt] = $scope.countTags[rt] + 1;
                }
            });
        }
        $scope.getCustomers = function () {
            entityService.getCustomers($scope.entityId).then(function (data) {
                $scope.rData = data.data.results;
                addOrderSno();
            }).catch(function error(msg) {
                $scope.showErrorMsg(msg);
            });
        };
        $scope.getVendors = function () {
            entityService.getVendors($scope.entityId).then(function (data) {
                $scope.rData = data.data.results;
                addOrderSno();
            }).catch(function error(msg) {
                $scope.showErrorMsg(msg);
            });
        };
        $scope.init = function () {
            $scope.showLoading();
            entityService.get($scope.entityId).then(function (data) {
                $scope.entityName = data.data.nm;
            }).catch(function error(msg) {
                $scope.showErrorMsg(msg);
            }).finally(function(){
                $scope.hideLoading();
            });
            $scope.showLoading();
            domainCfgService.getRouteTagsCfg().then(function (data) {
                $scope.allRTags = data.data.tags;
                if (checkNotNullEmpty($scope.allRTags) && checkNotNullEmpty($scope.allRTags[0])) {
                    $scope.tag = $scope.allRTags[0];
                }
                $scope.fetchList();
            }).catch(function error(msg) {
                $scope.showErrorMsg(msg);
            }).finally(function(){
                $scope.hideLoading();
            });
            $scope.showLoading();
            entityService.getLinksCount($scope.entityId).then(function (data) {
                var counts = data.data.replace(/"/g, "").split(",");
                $scope.customerCount = counts[0];
                $scope.vendorCount = counts[1];
            }).catch(function error(msg) {
                $scope.showErrorMsg(msg);
            }).finally(function(){
                $scope.hideLoading();
            });
        };
        $scope.init();
        ListingController.call(this, $scope, requestContext, $location);
        $scope.fetchLinks = function (type) {
            if ($scope.linkType != type) {
                $scope.linkType = type;
                if (type === "c") {
                    $scope.getCustomers();
                } else if (type === "v") {
                    $scope.getVendors();
                }
            }
        };
        $scope.fetchList = function(){
          if(($scope.iMan && ($scope.cnff.et.indexOf('csts')== -1))){
              $scope.linkType = "v";
              $scope.getVendors();
          }else{
              $scope.linkType = "c";
              $scope.getCustomers();
          }
        };
        $scope.selectAll = function (newVal, side) {
            if (side === 'l') {
                $scope.leftFiltered.forEach(function (item) {
                    item.selected = newVal;
                });
            } else if (side === 'r') {
                $scope.rightFiltered.forEach(function (item) {
                    item.selected = newVal;
                });
            }
        };
        $scope.moveRight = function () {
            var osnoIndex = $scope.rightFiltered.length + 1;
            $scope.leftFiltered.forEach(function (item) {
                if (item.selected) {
                    item.rt = $scope.tag;
                    item.osno = osnoIndex++;
                    item.selected = false;
                    item.hc = true;
                    $scope.routeUpdated = true;
                }
            });
        };
        $scope.moveLeft = function () {
            $scope.rightFiltered.forEach(function (item) {
                if (item.selected) {
                    item.rt = "--notag--";
                    item.selected = false;
                    item.hc = false;
                    $scope.routeUpdated = true;
                }
            });
            resetOrderSno();
        };
        function resetOrderSno() {
            var index = 1;
            $scope.rightFiltered.forEach(function (data) {
                if (data.rt != "--notag--") {
                    data.osno = index++;
                }
            });
        }
        $scope.moveUp = function (index) {
            if (index > 0) {
                var t = $scope.rightFiltered[index];
                $scope.rightFiltered[index] = $scope.rightFiltered[index - 1];
                $scope.rightFiltered[index - 1] = t;
                $scope.rowHighlight(index - 1);
                resetOrderSno();
                $scope.routeUpdated = true;
            }
        };
        $scope.moveDown = function (index) {
            if (index < $scope.rightFiltered.length - 1) {
                var t = $scope.rightFiltered[index];
                $scope.rightFiltered[index] = $scope.rightFiltered[index + 1];
                $scope.rightFiltered[index + 1] = t;
                $scope.rowHighlight(index + 1);
                resetOrderSno();
                $scope.routeUpdated = true;
            }
        };
        $scope.rowHighlight = function (index) {
            $scope.rightFiltered[index].rhc = true;
            setTimeout(function () {
                $scope.$apply(function () {
                    $scope.rightFiltered[index].rhc = false;
                });
            }, 1500);
        };
        $scope.moveToPos = function (index, newIndex) {
            if (index > newIndex) { //move up
                var t = $scope.rightFiltered[index];
                for (var i = index; i > newIndex; i--) {
                    $scope.rightFiltered[i] = $scope.rightFiltered[i - 1];
                }
                $scope.rightFiltered[newIndex] = t;
            } else { //move down
                t = $scope.rightFiltered[index];
                for (i = index; i < newIndex; i++) {
                    $scope.rightFiltered[i] = $scope.rightFiltered[i + 1];
                }
                $scope.rightFiltered[newIndex] = t;
            }
            $scope.rowHighlight(newIndex);
            resetOrderSno();
        };
        $scope.setPosition = function (index) {
            $scope.rightFiltered[index].esno = '';
            $scope.resetAllEditFlags();
            $scope.rightFiltered[index].edit = true;
        };
        $scope.resetAllEditFlags = function () {
            $scope.rightFiltered.forEach(function (data) {
                data.edit = false;
            });
        };
        $scope.cancelPosition = function (index) {
            $scope.rightFiltered[index].edit = false;
        };
        $scope.updatePosition = function (index) {
            $scope.rightFiltered[index].edit = false;
            var no = $scope.rightFiltered[index].esno;
            if (checkNotNullEmpty(no)) {
                var nIndex = parseInt(no) - 1;
                if (nIndex >= $scope.rightFiltered.length) {
                    $scope.moveToPos(index, $scope.rightFiltered.length - 1);
                } else if (nIndex <= 0) {
                    $scope.moveToPos(index, 0);
                } else {
                    $scope.moveToPos(index, nIndex);
                }
                $scope.routeUpdated = true;
            }
        };
        function setRouteIndexToOsno(){
            $scope.rData.forEach(function(data){
                data.ri = data.osno;
            });
        }
        $scope.updateEntityOrder = function () {
            var isRTAvailable = checkNotNullEmpty($scope.allRTags);
            setRouteIndexToOsno();
            $scope.showLoading();
            entityService.updateEntityOrder($scope.rData, $scope.linkType, $scope.entityId, isRTAvailable).then(function (data) {
                setCountByTags();
                $scope.routeUpdated = false;
                $scope.showSuccess(data.data);
            }).catch(function error(msg) {
                $scope.showErrorMsg(msg);
            }).finally(function(){
                $scope.hideLoading();
            });
        };
    }
]);
entityControllers.controller('StockBoardController', ['$scope', 'entityService',
    function($scope, entityService) {
        $scope.sb = {};
        $scope.sb.add = false;
        $scope.saveStockBoard = function () {
            if ($scope.sb != null) {
                $scope.sb.kid = $scope.entityId;
                if(checkNotNullEmpty($scope.sb.kid)){
                    $scope.showLoading();
                    entityService.setStockBoardConfig($scope.sb).then(function (data) {
                        $scope.showSuccess(data.data);
                    }).catch(function err(msg) {
                        $scope.showErrorMsg(msg);
                    }).finally(function(data){
                        $scope.hideLoading();
                    });
                }
            }
        };
        $scope.getStockBoard = function () {
            $scope.showLoading();
            entityService.getStockBoard($scope.entityId).then(function (data) {
                $scope.sb = data.data;
            }).catch(function err(msg) {
                $scope.showErrorMsg(msg);
            }).finally(function(data){
                $scope.hideLoading();
            });
        };
        $scope.getStockBoard();

    }
]);
entityControllers.controller('EntityDomainCtrl', ['$scope', 'entityService',
    function($scope, entityService) {
        $scope.newDomains = undefined;
        $scope.getDomainDetails=function(){
            $scope.showLoading();
            entityService.getDomainData($scope.entityId).then(function (data) {
                $scope.entDomains = data.data;
            }).catch(function err(msg) {
                $scope.showErrorMsg(msg);
            }).finally(function () {
                $scope.hideLoading();
            });
        };
        $scope.selectAll = function (newval, force) {
            if(checkNotNullEmpty($scope.entDomains)){
                $scope.entDomains.forEach(function(data){
                    if(data.isRemovable || force) {
                        data.selected = newval;
                    }
                });
            }
        };
        $scope.init = function () {
            $scope.getDomainDetails();
        };
        $scope.init();
        $scope.addDomains = function () {
            var domainIds = "";
            if (checkNotNullEmpty($scope.newDomains)) {
                $scope.newDomains.forEach(function (domain) {
                    domainIds += domain.id + ",";
                });
                if (domainIds.length > 0) {
                    domainIds = domainIds.slice(0, -1);
                }
            }
            if (domainIds.length == 0) {
                $scope.showWarning("Please choose domains to add.");
                return;
            }
            $scope.showLoading();
            entityService.domainUpdate($scope.entityId, domainIds, "add").then(function (data) {
                $scope.showSuccess(data.data);
                $scope.newDomains=undefined;
            }).catch(function err(msg) {
                $scope.showErrorMsg(msg);
            }).finally(function () {
                $scope.hideLoading();
            });
        };
        $scope.removeDomains = function () {
            var domainIds = "";
            if (checkNotNullEmpty($scope.entDomains)) {
                $scope.entDomains.forEach(function (domain) {
                    if (domain.selected) {
                        domainIds += domain.did + ",";
                    }
                });
                if (domainIds.length > 0) {
                    domainIds = domainIds.slice(0, -1);
                }
            }
            if (domainIds.length == 0) {
                $scope.showWarning("Please select domains to remove.");
                return;
            }
            $scope.showLoading();
            entityService.domainUpdate($scope.entityId, domainIds, "remove").then(function (data) {
                $scope.showSuccess(data.data);
            }).catch(function err(msg) {
                $scope.showErrorMsg(msg);
            }).finally(function () {
                $scope.hideLoading();
            });
        };
    }
]);

entityControllers.controller('EntityHierarchyController', ['$scope','$location','entityService','requestContext',
    function ($scope,$location,entityService) {
        $scope.init = function () {
            $scope.hr = {};
            $scope.showNetwork = false;
            $scope.defaultLevel = 2;
            $scope.selectLevel = [];
            $scope.maxLabelLength = 0;
            $scope.loading=false;
        };
        $scope.init();
        $scope.fetchHierarchy = function() {
            $scope.showLoading();
            $scope.loading=true;
            entityService.getNetworkView($scope.currentDomain).then(function (data) {
                $scope.hr = data.data;
                if (checkNotNullEmpty($scope.hr)) {
                    if (checkNotNullEmpty($scope.hr.network) && checkNotNullEmpty($scope.hr.network.maxlevel)) {
                        $scope.maxLevel = $scope.hr.network.maxlevel;
                        $scope.hr.sl = Math.min($scope.maxLevel, $scope.defaultLevel);
                        if ($scope.hr.sl > 0) {
                            $scope.showNetwork = true;
                            $scope.initLoad = true;
                            $scope.renderHierarchy();
                            $scope.initLoad = false;
                        }
                    }
                }
            }).catch(function err(msg) {
                $scope.showErrorMsg(msg);
            }).finally(function () {
                $scope.hideLoading();
                $scope.loading=false;
            })
        };
        $scope.fetchHierarchy();

        $scope.renderHierarchy = function() {
            $scope.initLoad = true;
            $scope.maxLabelLength = renderD3Tree($scope.hr, $scope.initLoad, $scope.resourceBundle['Customers']);
            $scope.initLoad = false;
        }
    }
]);



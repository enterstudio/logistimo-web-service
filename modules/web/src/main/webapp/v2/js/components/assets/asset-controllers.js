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

var assetControllers = angular.module('assetControllers', []);

assetControllers.controller("AssetMainController", ['$scope', function($scope){

}]);

assetControllers.controller("AddAssetController", ['$scope', '$route', 'assetService', 'entityService', 'requestContext', '$location',
    function($scope, $route, assetService, entityService, requestContext, $location){
        $scope.loading = false;
        $scope.uVisited = {};
        $scope.mcs = {};
        $scope.assetData = {};
        $scope.asset = {pushDeviceConfig: 1};
        $scope.sensorConfigOverrideKeys = [{grp:'comm', key: 'tmpNotify'}, {grp:'comm', key: 'incExcNotify'}, {grp:'comm', key: 'statsNotify'},
            {grp:'comm', key: 'devAlrmsNotify'}, {grp:'comm', key: 'tmpAlrmsNotify'},
            {grp:'comm', key: 'samplingInt'}, {grp:'comm', key: 'pushInt'},
            {grp:'highAlarm', key: 'temp'}, {grp:'highAlarm', key: 'dur'},
            {grp:'lowAlarm', key: 'temp'}, {grp:'lowAlarm', key: 'dur'},
            {grp:'highWarn', key: 'temp'}, {grp:'highWarn', key: 'dur'},
            {grp:'lowWarn', key: 'temp'}, {grp:'lowWarn', key: 'dur'},
            {grp:'notf', key: 'dur'}, {grp:'notf', key: 'num'}];
        $scope.configCommunicationChannel = [{id: 0, value: "SMS"}, {id: 1, value: "Internet"}, {id: 2, value:"Failover"}];
        $scope.assetCapacityMetrics = [{id: "Litres", value: "Litres"}];
        $scope.editConfig = false;
        $scope.editAssetId = requestContext.getParam("aid");
        $scope.edit = false;
        var errorCount = 0;
        $scope.currentYear = new Date().getFullYear();

        $scope.init = function(){

        };

        $scope.$watch("$scope.assetConfig", function(){
            if(checkNotNullEmpty($scope.editAssetId)){
                $scope.edit = true;
                $scope.editAll = false;
                $scope.showLoading();
                assetService.getAsset($scope.editAssetId).then(function(data){
                    $scope.asset = data.data;
                    $scope.asset.typ = $scope.asset.typ + "";
                    $scope.asset.ovId = $scope.asset.vId;
                    $scope.asset.owners = $scope.asset.ons;
                    $scope.asset.maintainers = $scope.asset.mts;
                    $scope.asset.kiosk = $scope.asset.entity;
                    $scope.currentAsset = $scope.assetConfig.assets[data.data.typ];
                    $scope.currentManu = $scope.currentAsset.mcs[data.data.vId];
                    $scope.updateSensors();
                    if($scope.currentAsset.at == 2){
                        $scope.showLoading();
                        assetService.getAssetRelations($scope.asset.vId, encodeURIParam($scope.asset.dId, true)).then(function (data) {
                            if($scope.getObjectLength(data.data) == 0){
                                $scope.editAll = true;
                            }
                        }).finally(function () {
                            $scope.hideLoading();
                        })
                    }
                }).catch(function(msg){
                    $scope.showErrorMsg(msg);
                }).finally(function(){
                    $scope.hideLoading();
                });
            }
        });

        $scope.range = function(min, max, step){
            step = step || 1;
            var input = [];
            for (var i = min; i <= max; i += step) input.push(i);
            return input;
        };

        $scope.updateAssetSel = function(value){
            $scope.asset.sns = null;
            //$scope.asset.meta != undefined && $scope.asset.meta.dev != undefined ? $scope.asset.meta.dev.mdl = undefined : undefined;
            $scope.currentAsset = $scope.assetConfig.assets[value];
            if(checkNotNullEmpty($scope.currentAsset)){
                if(checkNotNullEmpty($scope.asset.meta) && checkNotNullEmpty($scope.asset.meta.dev) && checkNotNullEmpty($scope.asset.meta.dev.mdl)){
                    $scope.asset.meta.dev.mdl = undefined;
                }
                $scope.uVisited.vId = false;
                $scope.uVisited.devMdl = false;
                if($scope.currentAsset.iGe){
                    $scope.config = angular.copy($scope.assetConfig.config);
                }
                var makeDefault = 0;
                for(var item in $scope.currentAsset.mcs){
                    if($scope.currentAsset.mcs[item].iC){
                        $scope.asset.vId = $scope.currentAsset.mcs[item].id;
                        makeDefault ++;
                    }
                }
                if(makeDefault != 1){
                    $scope.asset.vId = undefined;
                }else{
                    $scope.updateManufacturer($scope.asset.vId);
                }
            }
        };

        $scope.updateManufacturer = function(value){
            $scope.currentManu = $scope.currentAsset.mcs[value];
            $scope.uVisited.devMdl = false;
            if(checkNotNullEmpty($scope.asset.meta) && checkNotNullEmpty($scope.asset.meta.dev) && checkNotNullEmpty($scope.asset.meta.dev.mdl)){
                $scope.asset.meta.dev.mdl = undefined;
            }else{
                checkNotNullEmpty($scope.asset.meta) ? $scope.asset.meta.dev = {} : $scope.asset.meta = {dev: {}};
            }
            if(checkNotNullEmpty($scope.currentManu)){
                var makeDefault = 0;
                for(var item in $scope.currentManu.model){
                    $scope.asset.meta.dev.mdl = $scope.currentManu.model[item].name;
                    makeDefault ++;
                }
                if(makeDefault != 1){
                    $scope.asset.meta.dev.mdl = undefined;
                }else{
                    $scope.updateSensors();
                }
            }
        };

        $scope.updateSensors = function(){
            $scope.asset.sns = [];
            if(checkNotNullEmpty($scope.asset.meta) && checkNotNullEmpty($scope.asset.meta.dev) && checkNotNullEmpty($scope.asset.meta.dev.mdl)){
                var sns = $scope.currentManu.model && $scope.currentManu.model[$scope.asset.meta.dev.mdl] ? $scope.currentManu.model[$scope.asset.meta.dev.mdl].sns : undefined;
                if(checkNotNullEmpty(sns)){
                    $scope.asset.sns = [];
                    for(var item in sns){
                        $scope.asset.sns.push({"sId": sns[item].name, "cd": sns[item].cd});
                    }
                }
                $scope.updateSensorConfig();
            }
        };

        $scope.updateSensorConfig = function(){
            //Updating device configuration for multi sensor devices
            $scope.sensorConfig = {};
            if($scope.currentAsset.iGe){
                if(checkNotNullEmpty(checkNotNullEmpty($scope.config))){
                    angular.forEach($scope.asset.sns,function(sensor, index){
                        var isConfigAvailable = false;
                        if(checkNotNullEmpty($scope.config.sensors) && $scope.config.sensors.length > 0){
                            angular.forEach($scope.config.sensors, function(sensorConfig){
                                if(sensorConfig.sId == sensor.sId){
                                    isConfigAvailable = true;
                                    var tmpSensorConfig = angular.copy(sensorConfig);
                                    tmpSensorConfig.useDefault = false;
                                    angular.forEach($scope.sensorConfigOverrideKeys, function(value){
                                        if(checkNullEmpty(tmpSensorConfig[value.grp])){
                                            tmpSensorConfig[value.grp] = {};
                                            tmpSensorConfig[value.grp][value.key] = "";
                                        }
                                        if(checkNullEmpty(tmpSensorConfig[value.grp][value.key]) && checkStrictNotNullEmpty($scope.config[value.grp][value.key])){
                                            tmpSensorConfig[value.grp][value.key] = $scope.config[value.grp][value.key];
                                        }
                                    });
                                    $scope.sensorConfig[sensor.sId] = tmpSensorConfig;
                                }
                            });
                        }

                        if(!isConfigAvailable){
                            var tmpSensorConfig = {sId: sensor.sId, useDefault: true};
                            angular.forEach($scope.sensorConfigOverrideKeys, function(value){
                                if(checkNullEmpty(tmpSensorConfig[value.grp])){
                                    tmpSensorConfig[value.grp] = {};
                                    tmpSensorConfig[value.grp][value.key] = "";
                                }
                                if(checkNotNullEmpty($scope.config[value.grp]) && checkNotNullEmpty($scope.config[value.grp][value.key])){
                                    tmpSensorConfig[value.grp][value.key] = $scope.config[value.grp][value.key];
                                }
                            });
                            $scope.sensorConfig[sensor.sId] = tmpSensorConfig;
                        }
                        if(index == 0){
                            $scope.currentSns = sensor.sId;
                            $scope.currentSnsConfig = $scope.sensorConfig[$scope.currentSns];
                        }
                    });
                }
            }
        };

        $scope.resetCurrentSensorConfig = function(){
            if($scope.currentSnsConfig.useDefault){
                var tmpSensorConfig = {sId: $scope.currentSns, useDefault: true};
                angular.forEach($scope.sensorConfigOverrideKeys, function(value){
                    if(checkNullEmpty(tmpSensorConfig[value.grp])){
                        tmpSensorConfig[value.grp] = {};
                        tmpSensorConfig[value.grp][value.key] = "";
                    }
                    if(checkNotNullEmpty($scope.config[value.grp]) && checkNotNullEmpty($scope.config[value.grp][value.key])){
                        tmpSensorConfig[value.grp][value.key] = $scope.config[value.grp][value.key];
                    }
                });
                $scope.sensorConfig[$scope.currentSns] = tmpSensorConfig;
                $scope.currentSnsConfig = tmpSensorConfig;
            }
        };

        $scope.setCurrentSensorConfig = function(value){
            $scope.currentSns = value;
            $scope.currentSnsConfig = $scope.sensorConfig[$scope.currentSns];
        };

        $scope.resetConfig = function () {
            $scope.config = angular.copy($scope.assetConfig.config);
            $scope.updateSensorConfig();
        };

        $scope.constructSensorConfig = function(){
            $scope.config.sensors = [];
            $scope.sensorConfig[$scope.currentSns] = $scope.currentSnsConfig;
            if(checkNotNullEmpty($scope.sensorConfig)){
                angular.forEach($scope.sensorConfig, function(sensor){
                    if(!sensor.useDefault) {
                        var tmpSensorConfig = {sId: sensor.sId};
                        angular.forEach($scope.sensorConfigOverrideKeys, function(value){
                            if(checkStrictNotNullEmpty(sensor[value.grp][value.key])
                                && $scope.config[value.grp][value.key] != undefined
                                && sensor[value.grp][value.key] != $scope.config[value.grp][value.key]){
                                if(checkNullEmpty(tmpSensorConfig[value.grp])){
                                    tmpSensorConfig[value.grp] = {};
                                }
                                tmpSensorConfig[value.grp][value.key] = sensor[value.grp][value.key];
                            }
                        });
                        $scope.config.sensors.push(tmpSensorConfig);
                    }
                });
            }
        };
        var errorMsg;
        $scope.validateAssetMetaData = function(isSerialValidation, isEdit) {
            if(isSerialValidation && isEdit) {
                return true;
            }
            setAssetData(isSerialValidation);
            var isMonitoredAsset = ($scope.currentAsset.at == 2);
            if(isMonitoredAsset && checkNotNullEmpty($scope.assetData.dataFormat)) {
                var regex = new RegExp($scope.assetData.dataFormat);
                if (!regex.test($scope.assetData.deviceId)) {
                    errorMsg = $scope.resourceBundle[$scope.assetData.message].concat("\n").concat($scope.assetData.messageDescription);
                    if(isSerialValidation) {
                        $scope.asset.serror = errorMsg;
                    } else {
                        $scope.asset.merror = errorMsg;
                    }
                    return false;
                }
            }
            return true;
        };
        function setAssetData(isSerialValidation) {
            if(isSerialValidation) {
                $scope.asset.serror = '';
                $scope.assetData.dataFormat = $scope.currentManu.serialFormat;
                $scope.assetData.deviceId = $scope.asset.dId;
                $scope.assetData.message = 'serialno.format';
                $scope.assetData.messageDescription = $scope.currentManu.serialFormatDescription;
            } else {
                $scope.asset.merror = '';
                $scope.assetData.dataFormat = $scope.currentManu.modelFormat;
                $scope.assetData.deviceId = $scope.asset.meta.dev.mdl;
                $scope.assetData.message = 'modelno.format';
                $scope.assetData.messageDescription = $scope.currentManu.modelFormatDescription;
            }
        }

        $scope.constructAsset = function(){
            $scope.serror = false;
            $scope.merror = false;
            errorCount = 0;
            if(!($scope.validateAssetMetaData(true,$scope.edit) && $scope.validateAssetMetaData())){
                return false;
            }
            if(checkNotNullEmpty($scope.asset.kiosk) && checkNotNullEmpty($scope.asset.kiosk.id)){
                $scope.asset.kId = $scope.asset.kiosk.id;
            }/*else{
             $scope.showWarning("Please enter/select entity from list");
             return;
             }*/

            if(checkNotNullEmpty($scope.currentAsset)){
                $scope.asset.typ = $scope.currentAsset.id;
            }
            if(checkNotNullEmpty($scope.asset.owners)){
                $scope.asset.ons = [];
                for(var item in $scope.asset.owners){
                    $scope.asset.ons.push($scope.asset.owners[item].id);
                }
                $scope.asset.owners = undefined;
            }else{
                $scope.asset.ons = null;
                $scope.asset.owners = undefined;
            }
            if(checkNotNullEmpty($scope.asset.maintainers)){
                $scope.asset.mts = [];
                for(var item in $scope.asset.maintainers){
                    $scope.asset.mts.push($scope.asset.maintainers[item].id);
                }
                $scope.asset.maintainers = undefined;
            }else{
                $scope.asset.mts = null;
                $scope.asset.maintainers = undefined;
            }

            if($scope.currentAsset.iGe && checkNotNullEmpty($scope.config)){
                $scope.constructSensorConfig();
                $scope.asset.config = $scope.config;
            }
            if($scope.asset.pushDeviceConfig == 2){
                $scope.asset.pc = true;
            }else{
                $scope.asset.pc = false;
            }

            $scope.asset.kiosk = undefined;
            $scope.asset.entity = undefined;

            if(checkNotNullEmpty($scope.asset.meta) && checkNotNullEmpty($scope.asset.meta.cc) && checkNullEmpty($scope.asset.meta.cc.met)){
                $scope.asset.meta.cc.met = "";
            }
            return true;
        };

        $scope.createAsset = function(){
            if(!$scope.constructAsset()) {
                $scope.showErrorMsg($scope.resourceBundle['form.error']);
                return true;
            }
            $scope.loading = true;
            $scope.showLoading();
            assetService.createAsset($scope.asset).then(function(data){
                $scope.showSuccess(data.data);
                $scope.asset = {pushDeviceConfig: 1};
            }).catch(function(err){
                $scope.showErrorMsg(err, true);
            }).finally(function () {
                $scope.loading = false;
                $scope.hideLoading();
                $scope.uVisited = {};
            });
        };

        $scope.updateAsset = function(){
            if(!$scope.constructAsset()) {
                $scope.showErrorMsg($scope.resourceBundle['form.error']);
                return;
            }
            $scope.loading = true;
            $scope.showLoading();
            assetService.updateAsset($scope.asset).then(function(data){
                $scope.showSuccess(data.data);
                if($scope.asset.ovId == $scope.asset.vId){
                    $scope.$back();
                }else{
                    $location.path("setup/assets/");
                }
            }).catch(function(err){
                $scope.showErrorMsg(err, true);
            }).finally(function () {
                $scope.loading = false;
                $scope.hideLoading();
                $scope.uVisited = {};
            });
        };

        $scope.setOwnersVisited = function() {
            $scope.uVisited.ons = true;
        };
        $scope.setMaintainersVisited = function() {
            $scope.uVisited.mns = true;
        };
        $scope.getFilteredEntity = function (text) {
            $scope.loadingEntityMaterials = true;
            return entityService.getFilteredEntity(text.toLowerCase(), true).then(function (data) {
                $scope.loadingEntityMaterials = false;
                return data.data.results;
            }).catch(function error(msg) {
                $scope.showErrorMsg(msg);
            });
        };

        $scope.setAllVisited = function(){
            $scope.uVisited.at = true;
            $scope.uVisited.vId = true;
            $scope.uVisited.devMdl = true;
            $scope.uVisited.dId = true;
            $scope.uVisited.kId = true;
            if($scope.currentAsset){
                if($scope.currentAsset.iGe){
                    $scope.uVisited.simPhn = true;
                    $scope.uVisited.altSimPhn = true;
                }else{
                    $scope.uVisited.aCapacity = true;
                    $scope.uVisited.aCapacityMetric = true;
                }
            }
        };

        $scope.showValidationWarning = function(){
            $scope.showWarning($scope.resourceBundle['mandatory.fields'])
        };

        $scope.checkFormValidity = function() {
            $scope.invalidStatusUrl = false;
            $scope.sensorMsg = "";
            $scope.required = false;
            var invalidSensorMsg = [];
            $scope.sensorConfigEnabled = false;
            for(item in $scope.sensorConfig) {
                if($scope.sensorConfig[item].comm.statsNotify) {
                    $scope.sensorConfigEnabled = true;
                    invalidSensorMsg.push(item);
                }
            }
            if(invalidSensorMsg.length > 0) {
                invalidSensorMsg.sort();
                $scope.sensorMsg = "Enable stats push is enabled in sensor: " + invalidSensorMsg;
            }
            if($scope.config.comm.statsNotify || $scope.sensorConfigEnabled) {
                if(checkNullEmpty($scope.config.comm.statsUrl)) {
                    $scope.invalidStatusUrl = true;
                    $scope.required = true;
                }
            }
            return $scope.invalidStatusUrl;
        };

        $scope.init();
    }
]);

assetControllers.controller("AssetListingController", ['$scope', '$route', 'assetService', 'requestContext', '$location', 'exportService',
    function($scope, $route, assetService, requestContext, $location, exportService){
        $scope.wparams = [["etag", "etag"], ["search", "search.nm"], ["o", "offset"], ["s", "size"], ["at", "assetTypeFilter"]];
        $scope.filtered = [];
        $scope.loading = false;
        $scope.assetTypeFilter = 0;

        ListingController.call(this, $scope, requestContext, $location);
        $scope.selectAll = function (newval) {
            for (var item in $scope.filtered) {
                $scope.filtered[item]['selected'] = newval;
            }
        };

        $scope.init = function(){
            $scope.etag = requestContext.getParam("etag") || "";
            $scope.search = {nm:""};
            $scope.search.key = $scope.search.nm = requestContext.getParam("search") || "";
            $scope.vw = requestContext.getParam("vw") || 't';
            $scope.offset = requestContext.getParam("o") || 0;
            $scope.size = requestContext.getParam("s") || 50;
            $scope.assetTypeFilter = requestContext.getParam("at") || 0;
            $scope.selAll = false;
        };

        $scope.init();
        $scope.fetch = function(){
            $scope.loading = true;
            $scope.showLoading();
            //Key search and type should be separate.
            if(checkNotNullEmpty($scope.search.key)){
                $scope.assetTypeFilter = 0;
            }
            assetService.getAssetsByKeyword($scope.search.key, $scope.assetTypeFilter, $scope.size, $scope.offset).then(function(data){
                $scope.setResults(data.data);
                $scope.filtered = data.data.results;
            }).finally(function(){
                $scope.loading = false;
                $scope.hideLoading();
                fixTable();
            });
        };

        $scope.updateTypeFilter = function(value){
            $scope.assetTypeFilter = value;
            $scope.currentAsset = $scope.assetConfig.assets[value];
        };

        $scope.deleteAssets = function(){
            var finalItems = {data: []};
            for (var item in $scope.filtered) {
                if($scope.filtered[item]['selected']){
                    finalItems.data.push({dId: $scope.filtered[item].dId, vId: $scope.filtered[item].vId});
                }
            }

            if (finalItems.data.length == 0) {
                $scope.showWarning($scope.resourceBundle['selectitemtoremovemsg']);
            } else {
                if (!confirm($scope.resourceBundle['remove.asset.msg'] + '?')) {
                    return;
                }
                $scope.showLoading();
                assetService.deleteAsset(finalItems).then(function (data) {
                    $scope.showSuccess(data.data);
                    $scope.fetch();
                }).catch(function (msg) {
                    $scope.showErrorMsg(msg);
                }).finally(function () {
                    $scope.hideLoading();
                });
            }
        };

        $scope.fetch();

        $scope.searchAsset = function () {
            if($scope.search.nm != $scope.search.key) {
                $scope.search.nm = $scope.search.key;
            }
        };
        $scope.reset = function() {
            $scope.etag = "";
            $scope.search = {};
            $scope.assetTypeFilter = 0;
        };
    }
]);

assetControllers.controller('AssetsDetailsListingController', ['$scope', '$location', '$window', 'assetService', 'entityService', 'domainCfgService', 'requestContext','ASSET',
    function ($scope, $location, $window, assetService, entityService, domainCfgService, requestContext, ASSET) {
        $scope.assets = "";
        $scope.domainId = "";
        $scope.deviceId = 0;
        $scope.vendorId = 0;
        $scope.currentFilter = 0;
        $scope.duration = 0;
        $scope.params = $location.path();
        $scope.at_mg = [];
        $scope.at_md = [];
        $scope.filters = [{"value": "0", "displayValue": "All"},
            {"value": "1", "displayValue": "Temperature alarms", "type": 2},
            {"value": "2", "displayValue": "Device alarms", "type": 1},
            {"value": "3", "displayValue": "No data"},
            {"value": "4", "displayValue": "Normal items"}];
        $scope.filterDur = [{"index":"0","factorValue": "1", "displayValue": "Minutes"},
            {"index":"1","factorValue": "60", "displayValue": "Hours"},
            {"index":"2","factorValue": "1440", "displayValue": "Days"},
            {"index":"3","factorValue": "10080", "displayValue": "Weeks"},
            {"index":"4","factorValue": "43200", "displayValue": "Months"}];
        $scope.localFilters = ['entity', 'duration'];
        $scope.filterMethods = ['filterResults', 'onDurationFilterChange'];
        $scope.currentFilter = 0;
        $scope.tempCurrentFilter = 0;
        $scope.assetTypeFilter = ['md'];
        $scope.assetTypeFilterGroup = 0;
        $scope.durSortOrder = true;
        $scope.currentFilterDuration = 0;
        $scope.tempCurrentFilterDuration = 0;
        $scope.location = "";
        $scope.locType = "";
        $scope.deviceConfig = {};
        $scope.filtered = {};
        $scope.assetWSFilter = 0;
        $scope.tempAssetWSFilter = 0;
        ListingController.call(this, $scope, requestContext, $location);
        $scope.mparams = ["did", "vid", "alrm", "dur", "dtype", "eid", "o", "s", "at", "ws","awr"];

        $scope.assetRelationFilters = [{id:0,data:"All assets"},{id:1,data:"Assets with relationships"},
            {id:2,data:"Assets without relationships"}];
        $scope.awrDisplay = $scope.assetRelationFilters[0].data;
        $scope.tempAwrDisplay = $scope.assetRelationFilters[0].data;
        $scope.aDurationDisplay = 1;
        $scope.tempADurationDisplay = 1;
        $scope.awr = 0;
        $scope.tempAwr = 0;
        $scope.$watch("entity.id", watchfn("eid", "", $location, $scope));
        $scope.$watch("deviceId", watchfn("did", "", $location, $scope));
        $scope.$watch("vendorId", watchfn("vid", "", $location, $scope));
        $scope.$watch("currentFilter", watchfn("alrm", "", $location, $scope));
        $scope.$watch("multiAssetTypeFilter", watchfn("at", "", $location, $scope));
        $scope.$watch("currentFilterDuration", watchfn("dur", "", $location, $scope));
        $scope.$watch("aDurationDisplay", watchfn("dtype", "", $location, $scope));
        $scope.$watch("assetWSFilter", watchfn("ws", "", $location, $scope));
        $scope.$watch("awr", watchfn("awr", "", $location, $scope));

        $scope.getAlarmType = function (type, stat) {
            return $scope.alarmType[type + "-" + stat];
        };

        $scope.getSensorColorCode = function (vendorId, model, sensorId) {
            var colorCode = "#000";
            if (checkNotNullEmpty($scope.assetConfig) && $scope.assetConfig.assets[1].mcs[vendorId]
                && $scope.assetConfig.assets[1].mcs[vendorId].model && $scope.assetConfig.assets[1].mcs[vendorId].model[model]
                && $scope.assetConfig.assets[1].mcs[vendorId].model[model].sns && $scope.assetConfig.assets[1].mcs[vendorId].model[model].sns[sensorId]) {
                colorCode = $scope.assetConfig.assets[1].mcs[vendorId].model[model].sns[sensorId].cd;
            }

            return colorCode;
        };

        $scope.getMonitoringPointName = function (monitoringPointId,assetType) {
            var currentAssetConfig = $scope.assetConfig.assets[assetType];
            if (checkNotNullEmpty(currentAssetConfig) && checkNotNullEmpty(currentAssetConfig.mps) && checkNotNullEmpty(currentAssetConfig.mps[monitoringPointId])) {
                return currentAssetConfig.mps[monitoringPointId].name;
            }
            return "N/A";
        };

        $scope.getFilteredAssets = function (text) {
            $scope.loadingAssets = true;
            return assetService.getFilteredAssets(text.toLowerCase()).then(function (data) {
                $scope.loadingAssets = false;
                return data.data.results;
            }).catch(function error(msg) {
                $scope.showErrorMsg(msg);
            });
        };

        $scope.assetOnSelect = function (item) {
            $location.path('assets/detail/' + item.vId + '/' + encodeURIParam(item.dId, true));
        };

        function setAssetTypeFilter(data) {
            $scope.assetTypeFilter = [];
            for (var d in data) {
                $scope.assetTypeFilter.push(d);
            }
        }

        function getFilteredResults(origdata, ASSET){
            if(checkNotNullEmpty(origdata) && checkNotNullEmpty(origdata.results)) {
                origdata.results.forEach(function(data) {
                    var temporaryArray = [];
                    if(data.typ == ASSET.TEMPERATURE_LOGGER) {
                        if(checkNotNullEmpty(data.alrm)) {
                            data.alrm.forEach(function(newdata) {
                                if(newdata.typ != ASSET.EXTERNAL_SENSOR_ALARM_TYPE && newdata.typ != ASSET.ACTIVITY_ALARM_TYPE) {
                                    temporaryArray.push(newdata);
                                } else if(checkNotNullEmpty(newdata.sId)){
                                    temporaryArray.push(newdata);
                                }
                            });
                        }
                    } else {
                        if(checkNotNullEmpty(data.tmp)) {
                            data.tmp.forEach(function(newdata) {
                                if(checkNotNullEmpty(newdata.mpId)) {
                                    temporaryArray.push(newdata);
                                }
                            });
                        }
                    }
                    if(temporaryArray.length > 0){
                        if(data.typ == ASSET.TEMPERATURE_LOGGER) {
                            data.alrm = temporaryArray;
                        } else {
                            data.tmp = temporaryArray;
                        }
                    }
                });
            }

            return origdata;
        }

        function constructAssetTypeValue() {
            if (checkNotNullEmpty($scope.assetTypeFilter) && $scope.assetTypeFilter.length == 1 && $scope.assetTypeFilter[0] == 'md') {
                $scope.assetTypeFilterText = ['All monitored assets'];
                setAssetTypeFilter($scope.mAssetFilters.md);
            } else if (checkNotNullEmpty($scope.assetTypeFilter) && $scope.assetTypeFilter.length == 1 && $scope.assetTypeFilter[0] == 'mg') {
                $scope.assetTypeFilterText = ['All monitoring assets'];
                setAssetTypeFilter($scope.mAssetFilters.mg);
            } else {
                var allMd = true;
                for(md in $scope.mAssetFilters.md) {
                    if($scope.assetTypeFilter.indexOf(md) == -1) {
                        allMd = false;
                        break;
                    }
                }
                if(allMd) {
                    $scope.assetTypeFilterText = ['All monitored assets'];
                } else {
                    var allMg = true;
                    for(mg in $scope.mAssetFilters.mg) {
                        if($scope.assetTypeFilter.indexOf(mg) == -1) {
                            allMg = false;
                            break;
                        }
                    }
                    if(allMg) {
                        $scope.assetTypeFilterText = ['All monitoring assets'];
                    }
                }
                if(!allMd && !allMg) {
                    $scope.assetTypeFilterText = [];
                    angular.forEach($scope.assetTypeFilter,function(t){
                        $scope.assetTypeFilterText.push($scope.assetFilters[t].dV);
                    });
                }
            }
            setTypeFilter();
        }
        function setTypeFilter() {
            if(checkNotNullEmpty($scope.assetTypeFilter)) {
                $scope.at_md.splice(0,$scope.at_md.length);
                $scope.at_mg.splice(0,$scope.at_mg.length);
                angular.forEach($scope.assetTypeFilter,function(ty){
                    if(checkNotNullEmpty($scope.mAssetFilters.md[ty])) {
                        $scope.at_md.push(ty);
                    }else if(checkNotNullEmpty($scope.mAssetFilters.mg[ty])) {
                        $scope.at_mg.push(ty);
                    }
                });
            }
        }
        $scope.selectAll = function(type) {
            $scope.toggleCB(type);
            $scope.assetTypeFilter = [type];
            constructAssetTypeValue();
        };
        $scope.toggleFilter = function(force) {
            var show;
            if (force != undefined) {
                $scope.showAFilter = force;
                show = force;
            } else {
                $scope.showAFilter = !$scope.showAFilter;
                show = $scope.showAFilter;
            }
            var d = document.getElementById('filter_a');
            if (show) {
                d.style.top = '100%';
                d.style.opacity = '100';
                d.style.zIndex = '3';
            } else {
                d.style.top = '0';
                d.style.opacity = '0';
                d.style.zIndex = '-1';
            }
        };
        $scope.toggleCB = function (type) {
            if (type == 'md') {
                $scope.at_mg.splice(0,$scope.at_mg.length)
            } else if (type == 'mg') {
                $scope.at_md.splice(0,$scope.at_md.length)
            }
        };
        $scope.applyFilter = function () {
            if(!checkNullEmptyObject($scope.at_mg)) {
                $scope.assetTypeFilter = angular.copy($scope.at_mg);
                $scope.assetTypeFilterGroup = 1;
            } else if(!checkNullEmptyObject($scope.at_md)) {
                $scope.assetTypeFilter = angular.copy($scope.at_md);
                $scope.assetTypeFilterGroup = 0;
            }
            if(checkNotNullEmpty($scope.assetTypeFilter)) {
                $scope.multiAssetTypeFilter = $scope.assetTypeFilter.join(",");
            }
            $scope.multiAssetTypeFilterText = angular.copy($scope.assetTypeFilterText);
            $scope.toggleFilter();
        };
        $scope.setAssetRelFilter = function(relType){
            $scope.tempAwr = relType;
            $scope.tempAwrDisplay = $scope.assetRelationFilters[relType].data;
        };
        $scope.init = function (firstTimeInit) {
            $scope.loading = true;
            $scope.showLoading();
            if((checkNotNullEmpty(requestContext.getParam("eid")) && requestContext.getParam("eid") == 0)
                || ($scope.filterMin && checkNotNullEmpty($scope.entityId) && $scope.entityId != 0 && checkNullEmpty(requestContext.getParam("eid")))){
                //do nothing
            }else if (checkNotNullEmpty(requestContext.getParam("eid"))) {
                if (checkNullEmpty($scope.entity) || $scope.entity.id != requestContext.getParam("eid")) {
                    $scope.entity = {id: parseInt(requestContext.getParam("eid")), nm: ""};
                }
                $scope.entityId = parseInt(requestContext.getParam("eid"));
            }else if(firstTimeInit && checkNotNullEmpty($scope.defaultEntityId)){
                $location.$$search.eid = $scope.defaultEntityId;
                $location.$$compose();
                $scope.entityId = $scope.defaultEntityId;
                $scope.entity = {id: $scope.entityId, nm: ""};
            }else{
                $scope.entityId = 0;
            }

            if (checkNotNullEmpty(requestContext.getParam("did"))) {
                $scope.deviceId = requestContext.getParam("did");
            } else{
                $scope.deviceId = 0;
            }

            if (checkNotNullEmpty(requestContext.getParam("vid"))) {
                $scope.vendorId = requestContext.getParam("vid")    ;
            } else{
                $scope.vendorId = 0;
            }

            if (checkNotNullEmpty(requestContext.getParam("alrm"))) {
                $scope.currentFilter = parseInt(requestContext.getParam("alrm"));
                $scope.tempCurrentFilter = parseInt(requestContext.getParam("alrm"));
            } else {
                $scope.currentFilter = 0;
                $scope.tempCurrentFilter = 0;
            }

            if (checkNotNullEmpty(requestContext.getParam("dur"))) {
                $scope.currentFilterDuration = parseInt(requestContext.getParam("dur"));
                $scope.tempCurrentFilterDuration = parseInt(requestContext.getParam("dur"));
                $scope.duration = $scope.currentFilterDuration;
            } else{
                $scope.currentFilterDuration = 0;
                $scope.duration = $scope.currentFilterDuration;
            }

            if(checkNotNullEmpty(requestContext.getParam("dtype"))) {
                $scope.aDurationDisplay = parseInt(requestContext.getParam("dtype"));
                $scope.tempADurationDisplay = parseInt(requestContext.getParam("dtype"));
            } else{
                $scope.aDurationDisplay = 1;
                $scope.tempADurationDisplay = 1;
            }

            if (checkNotNullEmpty(requestContext.getParam("loc"))) {
                $scope.location = requestContext.getParam("loc")    ;
            }else{
                $scope.location = "";
            }

            if (checkNotNullEmpty(requestContext.getParam("ltype"))) {
                $scope.locType = requestContext.getParam("ltype");
            }

            if (checkNotNullEmpty(requestContext.getParam("at"))) {
                $scope.assetTypeFilter = requestContext.getParam("at").split(",");
            }

            if(checkNotNullEmpty(requestContext.getParam("ws"))){
                $scope.assetWSFilter = parseInt(requestContext.getParam("ws"));
                $scope.tempAssetWSFilter = parseInt(requestContext.getParam("ws"));
            }
            if(checkNotNullEmpty(requestContext.getParam("awr"))){
                $scope.awr = parseInt(requestContext.getParam("awr"));
                $scope.tempAwr = parseInt(requestContext.getParam("awr"));
                $scope.awrDisplay = $scope.assetRelationFilters[parseInt(requestContext.getParam("awr"))].data;
                $scope.tempAwrDisplay = $scope.assetRelationFilters[parseInt(requestContext.getParam("awr"))].data;
            }

            $scope.getAssets(true);
        };

        var renderContext = requestContext.getRenderContext(requestContext.getAction(), $scope.mparams);
        $scope.$on("requestContextChanged", function () {
            if (!renderContext.isChangeRelevant()) {
                return;
            }
            $scope.init();
        });

        $scope.fetch = function(){

        };

        $scope.getAssets = function(init){
            var eid = $scope.entityId == '0' ? null : $scope.entityId;
            constructAssetTypeValue();
            $scope.multiAssetTypeFilterText = angular.copy($scope.assetTypeFilterText);
            var at = checkNotNullEmpty($scope.assetTypeFilter)?$scope.assetTypeFilter.join(","):undefined;
            assetService.getAssetsInDetail(eid, at, $scope.assetWSFilter, $scope.currentFilter,
                $scope.duration*$scope.filterDur[$scope.aDurationDisplay].factorValue, $scope.location, $scope.offset, $scope.size, $scope.awr).then(function (data) {
                    $scope.alarmType = {
                        "0-1": $scope.resourceBundle['device.noconnection'],
                        "1-1": $scope.resourceBundle['sensor.noconnection'],
                        "2-1": $scope.resourceBundle['battery.warning'],
                        "2-2": $scope.resourceBundle['battery.alarm'],
                        "2-3": $scope.resourceBundle['battery.charging'],
                        "3": $scope.resourceBundle['firmware.error'],
                        "4-1": $scope.resourceBundle['temperature.device.inactive'],
                        "5-1": $scope.resourceBundle['power.outage']
                    };

                    $scope.temperatureAlarmType = {
                        "1": $scope.resourceBundle['temperature.state.excursion'],
                        "2": $scope.resourceBundle['temperature.state.warning'],
                        "3": $scope.resourceBundle['temperature.alert']
                    };

                    if(data.data != null && data.data.results != null){
                        $scope.assets = getFilteredResults(data.data,ASSET);
                        $scope.filtered = $scope.assets.results;
                        if(checkNotNullEmpty($scope.filtered)) {
                            $scope.currentAsset = $scope.assetConfig.assets[$scope.filtered[0].typ];
                        }
                        $scope.setResults($scope.assets);
                    }else{
                        $scope.assets = {results:[]};
                        $scope.setResults(null);
                    }
                }).catch(function error(msg) {
                    $scope.showErrorMsg(msg);
                }).finally(function(){
                    $scope.loading = false;
                    $scope.hideLoading();
                    fixTable();
                });
        };

        $scope.onDurationFilterChange = function () {
            $scope.currentFilterDuration = $scope.duration;
        };

        $scope.onDurationTypeChange = function (valDurType) {
            $scope.tempADurationDisplay = valDurType;
        };

        $scope.onAlarmFilterChange = function(value){
            $scope.tempCurrentFilter = value;
            if ($scope.tempCurrentFilter == 0 || $scope.tempCurrentFilter == 4)
                $scope.tempCurrentFilterDuration = 0;
        };

        $scope.updateWorkingStatusFilter = function(value){
            $scope.tempAssetWSFilter = value;
        };

        $scope.filterResults = function () {
            $scope.assetWSFilter = angular.copy($scope.tempAssetWSFilter);
            $scope.currentFilter = angular.copy($scope.tempCurrentFilter);
            $scope.currentFilterDuration = angular.copy($scope.tempCurrentFilterDuration);
            $scope.awr = angular.copy($scope.tempAwr);
            $scope.awrDisplay = angular.copy($scope.tempAwrDisplay);
            $scope.aDurationDisplay = angular.copy($scope.tempADurationDisplay);

        }

        $scope.resetFilters = function(){
            $location.$$search = {};
            $location.$$compose();
            $scope.device = null;
            $scope.vendor = null;
            if(!$scope.filterMin){
                $scope.entityId = 0;
                $scope.entity = null;
            }
            $scope.assetTypeFilter = ['md'];
            $scope.assetTypeFilterGroup = 0;
            $scope.awrDisplay = $scope.assetRelationFilters[0].data;
            $scope.tempAwrDisplay = $scope.assetRelationFilters[0].data;
            $scope.aDurationDisplay = 1;
            $scope.tempADurationDisplay = 1;
            $scope.awr = 0;
            $scope.tempAwr = 0;
            $scope.multiAssetTypeFilter = undefined;
            $scope.assetWSFilter = 0;
            $scope.tempAssetWSFilter = 0;
            $scope.assetId = "";
            $scope.tempCurrentFilter = 0;
            $scope.tempCurrentFilterDuration = 0;
        };
        $scope.init(true);
    }]);


assetControllers.controller('AssetDetailsController', ['$injector', '$scope', '$location', '$window', '$filter', 'assetService', 'mapService', '$timeout', 'requestContext', 'domainCfgService','configService','ASSET',
    function ($injector, $scope, $location, $window, $filter, assetService, mapService, $timeout, requestContext, domainCfgService, configService, ASSET) {
        $scope.deviceId = requestContext.getParam("deviceId");
        $scope.vendorId = requestContext.getParam("vendorId");
        $scope.entity = {};
        $scope.numFound = 2;
        $scope.currentTempPollCount = 0;
        $scope.currentTime = 0;
        $scope.prevDay = 0;
        $scope.configPushPullRequest = {};
        $scope.vendorMapping = {};
        $scope.deviceConfiguredStatus = true;
        $scope.entityInformation = {};
        $scope.configCommunicationChannel = [{id: 0, value: "SMS"}, {id: 1, value: "Internet"}, {id: 2, value: "Failover"}];
        $scope.assetCapacityMetrics = [{id: "Litres", value: "Litres"}];
        $scope.configCommChannelArray = ["SMS", "Internet", "Failover"];
        $scope.loadingInvItems = true;
        $scope.loadCounter = 0;
        $scope.editWS = false;
        $scope.assetRelations = {initial: true};
        $scope.assetInfoCounter = 0;
        $scope.defaultMPId = 2;

        $scope.map = {center: {latitude: 0, longitude: 0}, zoom: 8};
        $scope.marker = {
            id: 0,
            options: {draggable: true}
        };
        $scope.showWeather = false;

        var dt = new Date();
        $scope.today = parseUrlDate(new Date(dt.getFullYear(), dt.getMonth(), dt.getDate()));

        LanguageController.call(this, $scope, configService);
        TimezonesControllerWithOffset.call(this, $scope, configService);
        LocationController.call(this, $scope, configService);

        $scope.getAlarmType = function(type, stat){
            return $scope.alarmType[type + "-" + stat];
        };

        $scope.getSensorColorCode = function(vendorId, model, sensorId){
            var colorCode = "#000";
            if(checkNotNullEmpty($scope.assetConfig) && $scope.assetConfig.assets[1].mcs[vendorId]
                && $scope.assetConfig.assets[1].mcs[vendorId].model && $scope.assetConfig.assets[1].mcs[vendorId].model[model]
                && $scope.assetConfig.assets[1].mcs[vendorId].model[model].sns && $scope.assetConfig.assets[1].mcs[vendorId].model[model].sns[sensorId]){
                colorCode = $scope.assetConfig.assets[1].mcs[vendorId].model[model].sns[sensorId].cd;
            }

            return colorCode;
        };

        $scope.getMonitoringPointName = function(monitoringPointId){
            if(checkNotNullEmpty($scope.currentAsset)){
                try{
                    if($scope.currentAsset.at == 2 && checkNotNullEmpty($scope.currentAsset.mps) && checkNotNullEmpty($scope.currentAsset.mps[monitoringPointId])) {
                        return $scope.currentAsset.mps[monitoringPointId].name;
                    }else if($scope.currentAsset.at == 1 && checkNotNullEmpty($scope.relatedAsset)){
                        var relatedAssetConfig = $scope.assetConfig.assets[$scope.relatedAsset.typ];
                        if(checkNotNullEmpty(relatedAssetConfig) && checkNotNullEmpty(relatedAssetConfig.mps) && checkNotNullEmpty(relatedAssetConfig.mps[monitoringPointId])){
                            return relatedAssetConfig.mps[monitoringPointId].name;
                        }
                    }
                }catch(error){
                    console.log(error);
                }
            }
            return "N/A";
        };

        $scope.updateCurrentAsset = function(value){
            $scope.currentAsset = $scope.assetConfig.assets[value];
            $scope.updateManufacturer();
        };

        $scope.updateManufacturer = function(){
            $scope.currentManu = $scope.currentAsset.mcs[$scope.vendorId];
        };

        $scope.fetchData = function () {
            $scope.getAssetDetails();
            //$scope.currentTempReqStatus = 'temperature.request';
        };

        function checkAssetInfoAvailability() {
            if(checkNotNullEmpty($scope.currentAsset)) {
                if(checkNotNullEmpty($scope.currentAsset.an)) {
                    $scope.assetDetails.info = true;
                }
            } else if(checkNotNullEmpty($scope.assetDetails)) {
                if(checkNotNullEmpty($scope.assetDetails.dId)){
                    $scope.assetDetails.info = true;
                } else if(checkNotNullEmpty($scope.assetDetails.meta) && checkNotNullEmpty($scope.assetDetails.meta.dev)) {
                    if(checkNotNullEmpty($scope.assetDetails.meta.dev.mdl) || checkNotNullEmpty($scope.assetDetails.meta.dev.yom)) {
                        $scope.assetDetails.info = true;
                    }
                }
            } else if(checkNotNullEmpty($scope.currentManu)) {
                $scope.assetDetails.info = true;
            }
        }

        function checkAssetGSMAvailability() {
            if(checkNotNullEmpty($scope.currentAsset) && $scope.currentAsset.iGe) {
                if(checkNotNullEmpty($scope.assetDetails) && checkNotNullEmpty($scope.assetDetails.meta)){
                    if(checkNotNullEmpty($scope.assetDetails.meta.dev) && (checkNotNullEmpty($scope.assetDetails.meta.dev.dVr) || checkNotNullEmpty($scope.assetDetails.meta.dev.mVr) || checkNotNullEmpty($scope.assetDetails.meta.dev.imei) || checkNotNullEmpty($scope.assetDetails.meta.dev.mdlD))){
                        $scope.assetDetails.gsmInfo = true;
                    } else if(checkNotNullEmpty($scope.assetDetails.meta.gsm)){
                        if(checkNotNullEmpty($scope.assetDetails.meta.gsm.sim) && (checkNotNullEmpty($scope.assetDetails.meta.gsm.sim.phn) || checkNotNullEmpty($scope.assetDetails.meta.gsm.sim.sid) || checkNotNullEmpty($scope.assetDetails.meta.gsm.sim.np))) {
                            $scope.assetDetails.gsmInfo = true;
                        } else if(checkNotNullEmpty($scope.assetDetails.meta.gsm.altSim) && (checkNotNullEmpty($scope.assetDetails.meta.gsm.altSim.phn) || checkNotNullEmpty($scope.assetDetails.meta.gsm.altSim.sid) || checkNotNullEmpty($scope.assetDetails.meta.gsm.altSim.np))) {
                            $scope.assetDetails.gsmInfo = true;
                        }
                    }
                }
            }
        }

        $scope.getAssetRelations = function(){
            $scope.showLoading();
            $scope.loadingAr = true;
            assetService.getAssetRelations($scope.vendorId, $scope.deviceId).then(function (data) {
                $scope.assetRelations = data.data;
            }).catch(function error(msg) {
                $scope.showErrorMsg(msg);
            }).finally(function (){
                $scope.loadingAr = false;
                $scope.hideLoading();
            });
        };

        $scope.getAssetRelationsForMonitoringAsset = function () {
            $scope.showLoading();
            $scope.loadingAr = true;
            assetService.getAssetRelations($scope.vendorId, $scope.deviceId).then(function (data) {
                $scope.relatedAsset = data.data;
                $scope.assetRelations = {};
                if(checkNotNullEmpty($scope.relatedAsset) && checkNotNullEmpty($scope.relatedAsset.vId) && checkNotNullEmpty($scope.relatedAsset.dId)){
                    assetService.getAssetRelations($scope.relatedAsset.vId,encodeURIParam($scope.relatedAsset.dId,true)).then(function (data) {
                        $scope.assetRelations = data.data;
                    }).catch(function error(msg) {
                        $scope.showErrorMsg(msg);
                    }).finally(function (){
                        $scope.loadingAr = false;
                        $scope.hideLoading();
                    });
                }else{
                    $scope.relatedAsset = {};
                }
            }).catch(function error(msg) {
                $scope.showErrorMsg(msg);
            }).finally(function (){
                $scope.loadingAr = false;
                $scope.hideLoading();
            });
        };

        $scope.getAssetDetails = function () {
            $scope.showLoading();
            assetService.getAssetDetails($scope.vendorId, $scope.deviceId).then(function (data) {
                //Creating for
                $scope.alarmType = {
                    "0-1": $scope.resourceBundle['device.noconnection'],
                    "1-1": $scope.resourceBundle['sensor.noconnection'],
                    "2-1": $scope.resourceBundle['battery.warning'],
                    "2-2": $scope.resourceBundle['battery.alarm'],
                    "2-3": $scope.resourceBundle['battery.charging'],
                    "3": $scope.resourceBundle['firmware.error'],
                    "4-1": $scope.resourceBundle['temperature.device.inactive'],
                    "5-1": $scope.resourceBundle['power.outage']
                };

                $scope.temperatureAlarmType = {
                    "1": $scope.resourceBundle['temperature.state.excursion'],
                    "2": $scope.resourceBundle['temperature.state.warning'],
                    "3": $scope.resourceBundle['temperature.alert']
                };

                $scope.assetDetails = getFilteredResults(data.data, ASSET);
                if(checkNotNullEmpty($scope.assetDetails.cfg)) {
                    if ($scope.assetDetails.cfg.st == 1) {
                        $scope.tempData = $scope.resourceBundle['config.device.pull'];
                    } else if ($scope.assetDetails.cfg.st == 2) {
                        $scope.tempData = $scope.resourceBundle['temperature.device.configured'];
                    } else if ($scope.assetDetails.cfg.st == 3) {
                        $scope.tempData = $scope.resourceBundle['config.device.requested'];
                    }
                }
                if(checkNotNullEmpty($scope.assetDetails) && checkNotNullEmpty($scope.assetDetails.typ)){
                    $scope.updateCurrentAsset($scope.assetDetails.typ);
                    if(checkNotNullEmpty($scope.assetDetails.entity)){
                        $scope.map = mapService.updateEntityMap($scope.assetDetails.entity);
                        $scope.marker.coords = mapService.updateEntityMap($scope.assetDetails.entity).center;
                    }
                    $scope.workingStatus = ($scope.assetDetails.ws.st).toString();
                    if($scope.currentAsset.at == 2){
                        $scope.defaultMPId = $scope.currentAsset.dMp;
                        $scope.getAssetRelations();
                    }else{
                        $scope.getAssetRelationsForMonitoringAsset();
                    }
                    checkAssetInfoAvailability();
                    checkAssetGSMAvailability();
                }
            }).catch(function error(msg) {
                $scope.showErrorMsg(msg);
            }).finally(function (){
                $scope.loadingInvItems = false;
                $scope.hideLoading();
            });
        };

        function getFilteredResults(origdata, ASSET){
            if(checkNotNullEmpty(origdata)) {
                var temporaryArray = [];
                if(origdata.typ == ASSET.TEMPERATURE_LOGGER) {
                    if(checkNotNullEmpty(origdata.alrm)) {
                        origdata.alrm.forEach(function(data) {
                            if(data.typ != ASSET.EXTERNAL_SENSOR_ALARM_TYPE && data.typ != ASSET.ACTIVITY_ALARM_TYPE) {
                                temporaryArray.push(data);
                            } else if(checkNotNullEmpty(data.sId)){
                                temporaryArray.push(data);
                            }
                        });
                    }
                } else {
                    if(checkNotNullEmpty(origdata.tmp)) {
                        origdata.tmp.forEach(function(data) {
                            if(checkNotNullEmpty(data.mpId)) {
                                temporaryArray.push(data);
                            }
                        });
                    }
                }
                if(temporaryArray.length > 0){
                    if(origdata.typ == ASSET.TEMPERATURE_LOGGER) {
                        origdata.alrm = temporaryArray;
                    } else {
                        origdata.tmp = temporaryArray;
                    }
                }
            }
            return origdata;
        }

        $scope.getCurrentTemp = function () {
            $scope.deviceDetails[0].tmpStatus = 0;
            $scope.currentTempReqStatus = 'temperature.requesting';
            tempService.getCurrentTemp($scope.vendorId, $scope.deviceId).then(function () {
                $scope.currentTempReqStatus = 'temperature.wait';
                $timeout(function () {
                    $scope.currentTempReqStatus = 'temperature.fetch';
                    $scope.currentTempPollCount = 0;
                    $scope.poll();
                }, 30000);
            }).catch(function () {
                $scope.currentTempReqStatus = 'request.fail';
                $scope.deviceDetails[0].tmpStatus = 1;
                console.log("Get current temp failed.");
            });
        };

        $scope.stopPoll = function () {
            $scope.currentTempPollCount = 10;
            $scope.deviceDetails[0].tmpStatus = 1;
            $scope.currentTempReqStatus = 'temperature.request';
        };

        $scope.poll = function () {
            $scope.currentTempReqStatus = 'temperature.fetch';
            assetService.getDeviceInfo($scope.vendorId, $scope.deviceId).then(function (data) {
                if (checkNotNullEmpty(data.data) && checkNotNullEmpty(data.data.tmp) && (checkNullEmpty($scope.deviceInfo.tmp) || data.data.tmp.tmp != $scope.deviceInfo.tmp.tmp)) {
                    $scope.deviceInfo = data.data;
                    $scope.deviceDetails[0].tmpStatus = 1;
                    $scope.currentTempReqStatus = 'temperature.received';
                } else {
                    $timeout(function () {
                        if ($scope.currentTempPollCount < 10)
                            $scope.poll();
                        else{
                            $scope.deviceDetails[0].tmpStatus = 1;
                            $scope.currentTempReqStatus = 'temperature.not.received';
                        }

                        $scope.currentTempPollCount++;
                    }, 30000);
                }
            }).catch(function error(msg) {
                $scope.showErrorMsg(msg);
            });
        };

        $scope.updateWS = function(){
            if(checkNullEmpty($scope.workingStatus)) {
                $scope.showWarning("Please select status");
                return;
            }
            $scope.editWS = false;
            $scope.assetDetails.ws.st = $scope.workingStatus;
            $scope.updateDeviceMeta();
        };

        $scope.updateDeviceMeta = function () {
            $scope.showLoading();

            if(checkNotNullEmpty($scope.assetDetails.meta) && checkNotNullEmpty($scope.assetDetails.meta.cc) && checkNullEmpty($scope.assetDetails.meta.cc.met)){
                $scope.assetDetails.meta.cc.met = "";
            }
            //Updating fields
            $scope.asset = {
                'id': $scope.assetDetails.id,
                'dId': $scope.assetDetails.dId,
                'vId': $scope.assetDetails.vId,
                'tags': $scope.assetDetails.tags,
                'meta': $scope.assetDetails.meta,
                'sns': $scope.assetDetails.sns,
                'typ': $scope.assetDetails.typ,
                'ws': $scope.assetDetails.ws
            };

            if(checkNotNullEmpty($scope.assetDetails.entity)){
                $scope.asset.kId = $scope.assetDetails.entity.id;
            }

            if(checkNotNullEmpty($scope.assetDetails.ons)){
                $scope.asset.ons = [];
                for(var i = 0; i<$scope.assetDetails.ons.length; i++){
                    $scope.asset.ons.push($scope.assetDetails.ons[i].id);
                }
            }
            if(checkNotNullEmpty($scope.assetDetails.mts)){
                $scope.asset.mts = [];
                for(var i = 0; i<$scope.assetDetails.mts.length; i++){
                    $scope.asset.mts.push($scope.assetDetails.mts[i].id);
                }
            }

            assetService.updateAsset($scope.asset).then(function (data) {
                $scope.getAssetDetails();
                $scope.infoEditable = false;
                $scope.showSuccess(data.data);
            }).catch(function () {
                $scope.showError($scope.resourceBundle['deviceinfo.updatefail']);
            }).finally(function(){
                $scope.hideLoading();
            });
        };

        $scope.increaseCounter = function(){
            $scope.assetInfoCounter++;
            return true;
        };

        $scope.fetchData();
    }]);

assetControllers.controller('AssetRelationsController', ['$scope', 'assetService',
    function($scope, assetService){
        $scope.arModel = {relatedAsset: {}, useDefault: true};
        $scope.edit = false;

        $scope.$watch("assetRelations", function(){
            if(checkNullEmpty($scope.assetRelations) || $scope.getObjectLength($scope.assetRelations) == 0){
                $scope.edit = true;
            }
        });

        $scope.getFilteredAssets = function (text) {
            $scope.loadingAssets = true;
            $scope.noResults = false;
            return assetService.getFilteredAssets(text.toLowerCase(), null, 1, false, true).then(function (data) {
                $scope.loadingAssets = false;
                if(checkNullEmpty(data.data.results)){
                    $scope.noResults = true;
                    $scope.noResultMessage = $scope.resourceBundle['asset.relation.message1'] + ' ' + text + ' ' + $scope.resourceBundle['asset.relation.message2'];
                }
                return data.data.results;
            }).catch(function error(msg) {
                $scope.showErrorMsg(msg);
            });
        };

        $scope.updateAssetRelations = function(item){
            if(checkNotNullEmpty(item)){
                $scope.fetchSensorList(item);
                $scope.assetRelations = [];
                if(checkNotNullEmpty($scope.currentAsset.mps)){
                    for(var j in $scope.currentAsset.mps){
                        $scope.assetRelations.push({"dId": item.dId, "vId": item.vId, "sId": $scope.currentAsset.mps[j].sId, "mpId": $scope.currentAsset.mps[j].mpId, "typ": 2});
                    }
                }
            } else {
                $scope.assetRelations = [];
            }
        };

        $scope.fetchSensorList = function (item) {
            var currentManu = $scope.assetConfig.assets[item.typ].mcs[item.vId];
            if(checkNotNullEmpty(currentManu.model) && checkNotNullEmpty(currentManu.model[item.mdl]) && checkNotNullEmpty(currentManu.model[item.mdl].sns)){
                $scope.sns = currentManu.model[item.mdl].sns;
            }
        };

        $scope.get = function(){
            $scope.showLoading();
            $scope.loadingAr = true;
            assetService.getAssetRelations($scope.vendorId, $scope.deviceId).then(function (data) {
                $scope.assetRelations = data.data;
                if(checkNullEmpty($scope.assetRelations) || $scope.getObjectLength($scope.assetRelations) == 0){
                    $scope.edit = true;
                }
                $scope.getAssetDetails();
                $scope.getAssetRelations();
            }).catch(function error(msg) {
                $scope.showErrorMsg(msg);
            }).finally(function (){
                $scope.loadingAr = false;
                $scope.hideLoading();
            });
        };

        $scope.createOrEditAssetRelations = function(){
            if(checkNotNullEmpty($scope.assetRelations)){
                $scope.showLoading();
                $scope.loadingAr = true;
                var tmpAssetRelations = [];
                angular.forEach($scope.assetRelations, function (value, key) {
                    tmpAssetRelations.push(value);
                });
                $scope.postData = {data:[{dId: $scope.assetDetails.dId, vId: $scope.assetDetails.vId, ras: tmpAssetRelations}]};
                assetService.createAssetRelationships($scope.postData).then(function(data){
                    if($scope.edit){
                        $scope.showSuccess("Asset relationship updated successfully.");
                    }else{
                        $scope.showSuccess("Asset relationship created successfully.");
                    }
                    $scope.get();
                }).catch(function(err){
                    $scope.showErrorMsg(err);
                }).finally(function(){
                    if($scope.edit){
                        $scope.edit = !$scope.edit;
                    }
                    $scope.loadingAr = false;
                    $scope.hideLoading();
                });
            }else{
                $scope.showWarning("Please enter valid details");
            }
        };

        $scope.editRelations = function(){
            $scope.edit = true;
            $scope.assetRelationsCopy = angular.copy($scope.assetRelations);
            if(checkNotNullEmpty($scope.assetRelations) && checkNotNullEmpty($scope.assetRelations[$scope.defaultMPId])){
                $scope.showLoading();
                assetService.getAssetDetails($scope.assetRelations[$scope.defaultMPId].vId, $scope.assetRelations[$scope.defaultMPId].dId).then(function(data){
                    $scope.arModel.relAsset = {dId: $scope.assetRelations[$scope.defaultMPId].dId, vId: $scope.assetRelations[$scope.defaultMPId].vId};
                    $scope.fetchSensorList(data.data);
                }).finally(function(){
                    $scope.hideLoading();
                });
            }
        };

        $scope.deleteRelations = function(){
            if (!confirm($scope.resourceBundle['remove.asset.relation.msg'] + '?')) {
                return;
            }

            if(checkNotNullEmpty($scope.assetRelations)){
                $scope.showLoading();
                $scope.loadingAr = true;
                var tmpAssetRelations = [];
                $scope.postData = {data:[{dId: $scope.assetDetails.dId, vId: $scope.assetDetails.vId, ras: tmpAssetRelations}]};
                assetService.createAssetRelationships($scope.postData, true).then(function(data){
                    $scope.showSuccess("Asset relationship deleted successfully.");
                    $scope.arModel.relAsset = undefined;
                    $scope.assetRelations = [];
                    $scope.get();
                }).catch(function(err){
                    $scope.showErrorMsg(err);
                }).finally(function(){
                    if($scope.edit){
                        $scope.edit = !$scope.edit;
                    }
                    $scope.loadingAr = false;
                    $scope.hideLoading();
                });
            }
        };

        $scope.cancelEdit = function(){
            $scope.edit = false;
            $scope.assetRelations = angular.copy($scope.assetRelationsCopy);
            if( checkNotNullEmpty($scope.assetRelationsCopy) ) {
                $scope.arModel.relAsset = {dId: $scope.assetRelations[$scope.defaultMPId].dId, vId: $scope.assetRelations[$scope.defaultMPId].vId};
            } else {
                $scope.arModel.relAsset = undefined;
                $scope.edit = true;
            }
        }
    }
]);

assetControllers.controller('AssetSummaryController', ['$scope', 'requestContext', 'assetService', '$location', '$filter','$timeout',
    function($scope, requestContext, assetService, $location, $filter, $timeout){
        $scope.recentAlerts = "";
        $scope.categoryJson = [];
        $scope.tempJson = [];
        $scope.size = 10;
        $scope.deviceAlertLoading = true;
        $scope.deviceTempLoading = true;
        $scope.wparams = [["tday","tday","",formatDate2Url]];
        $scope.currentMpId = 2;
        $scope.currentSensor = 'B';
        $scope.tday = parseUrlDate(requestContext.getParam("tday"));
        $scope.offsetChanged = false;
        $scope.firstInit = true;
        var dt = new Date();
        var today = parseUrlDate(new Date(dt.getFullYear(), dt.getMonth(), dt.getDate()));
        $scope.pg = {
            type: "line",
            height: "225",
            width: "100%",
            options: {
                "caption": "Power availability",
                "exportEnabled": 1,
                "theme": "fint",
                "showLegend": 1,
                "labelDisplay": "rotate",
                "xAxisName": "Time",
                "showValues": "0",
                "yAxisMaxValue": 1,
                "yAxisMinValue": 0,
                "showyAxisValue": 0,
                "xAxisLineColor" : "#f56954",
                "divLineAlpha": 0,
                "labelStep": 10,
                "lineThickness": "1.5",
                "drawAnchors": 1,
                "anchorRadius": "0.5",
                "canvasBgColor": "#F0F0F0",
                "bgColor": "#F0F0F0",
                "canvasBgAlpha": 0,
                "canvasBorderThickness": 0,
                "exportFileName": "power_availability" + "_" + $scope.vendorId + "_" + FormatDate_DD_MM_YYYY(today)
            },
            legends: [
                {
                    color: "#00a65a",
                    name: "Available"
                },
                {
                    color: "#f56954",
                    name: "Outage"
                },
                {
                    color: "#D3D3D3",
                    name: "Unknown"
                }
            ],
            "trendlines": {
                "line": [
                    {
                        "startvalue": "1",
                        "color": "#00a65a",
                        "displayvalue": "Available",
                        "valueOnRight" : "0"
                    },
                    {
                        "startvalue": "0",
                        "color": "#f56954",
                        "displayvalue": "Outage",
                        "valueOnRight" : "0"
                    }
                ]
            }
        };
        ListingController.call(this, $scope, requestContext, $location);
        $scope.init = function(){};

        $scope.fetch = function(){
            if($scope.offsetChanged){
                $scope.getRecentAlerts();
            }else{
                $scope.getTemperature();
            }
            $scope.offsetChanged = false;
        };

        $scope.$watch("offset", function () {
            if(!$scope.firstInit){
                $scope.offsetChanged = true;
            }
        });

        $scope.$watch("currentAsset", function(){
            if(checkNotNullEmpty($scope.currentAsset) && checkNullEmpty($scope.assetRelations.initial))
                $scope.initialFetch();
        });

        $scope.$watch("assetRelations", function(){
            if(checkNotNullEmpty($scope.assetRelations)) {
                $scope.currentRelatedAsset = $scope.assetRelations[$scope.currentMpId];
            }
            if(checkNotNullEmpty($scope.currentAsset) && checkNullEmpty($scope.assetRelations.initial))
                $scope.initialFetch();
        });

        $scope.initialFetch = function(){
            if($scope.firstInit){
                if ($scope.currentAsset.at == 1 && checkNotNullEmpty($scope.currentManu.model[$scope.assetDetails.meta.dev.mdl].dS)) {
                    $scope.currentSensor = $scope.currentManu.model[$scope.assetDetails.meta.dev.mdl].dS;
                    $scope.getRecentAlerts();
                    $scope.refreshTemperature();
                    $scope.firstInit = false;
                }else if(checkNotNullEmpty($scope.currentAsset.dMp)){
                    $timeout(function () {
                        $scope.currentMpId = $scope.currentAsset.dMp;
                        $scope.getRecentAlerts();
                        $scope.refreshTemperature();
                        $scope.firstInit = false;
                    }, 300);
                }
            }
        };

        $scope.getRecentAlerts = function () {
            $scope.recentAlerts = {};
            $scope.recentAlertsLoading = true;
            var offset = ($scope.offset / $scope.size) + 1;
            assetService.getRecentAlerts($scope.vendorId, $scope.deviceId, offset, $scope.size).then(function (data) {
                if(checkNotNullEmpty(data.data)){
                    var alertsRec = data.data;
                    alertsRec.numFound = alertsRec.nPages * $scope.size;
                    alertsRec.results = alertsRec.data;
                    $scope.setResults(alertsRec);
                    $scope.recentAlerts =alertsRec.results;
                }
                $scope.recentAlertsLoading = false;
            }).catch(function error(msg) {
                $scope.showErrorMsg(msg);
            });
        };

        $scope.updateTempGraph = function (tdate) {
            if(checkNotNullEmpty($scope.currentAsset)) {
                var samplingInt, mpOrSensor;
                if ($scope.currentAsset.at == 1) {
                    samplingInt = checkNotNullEmpty($scope.assetDetails) && checkNotNullEmpty($scope.assetDetails.meta)
                    && checkNotNullEmpty($scope.assetDetails.meta.int) && checkNotNullEmpty($scope.assetDetails.meta.int.sint)
                        ? $scope.assetDetails.meta.int.sint * 60 : 600;
                    mpOrSensor = $scope.currentSensor;
                } else {
                    samplingInt = checkNotNullEmpty($scope.currentRelatedAsset) && checkNotNullEmpty($scope.currentRelatedAsset.meta)
                    && checkNotNullEmpty($scope.currentRelatedAsset.meta.int) && checkNotNullEmpty($scope.currentRelatedAsset.meta.int.sint)
                        ? $scope.currentRelatedAsset.meta.int.sint * 60 : 600;
                    mpOrSensor = $scope.currentMpId;
                }
                var size = 300;
                $scope.deviceTempLoading = true;
                assetService.getTemperatures($scope.vendorId, $scope.deviceId, mpOrSensor, $scope.currentAsset.at, size, samplingInt, tdate).then(function (data) {
                    if (checkNotNullEmpty(data.data)) {
                        $scope.deviceTemp = data.data;
                        $scope.pg.values = [];
                        if (checkNotNullEmpty($scope.deviceTemp.data) && $scope.deviceTemp.data.length > 0) {
                            angular.forEach($scope.deviceTemp.data, function (value) {
                                $scope.categoryJson.push({"label": value.label});
                                var values = {"label": value.label, "value": value.pwa};
                                if(checkNotNullEmpty(value.pwa)){
                                    if(value.pwa == 1){
                                        values.toolText = value.label + ", Available";
                                    }else if(value.pwa == 0){
                                        values.toolText = value.label + ", Outage";
                                    }
                                }
                                $scope.pg.values.push(values);
                                var tempValue = value.value;
                                if (checkNotNullEmpty(tempValue)) {
                                    tempValue = tempValue < -500 ? -500 : (tempValue > 500 ? 500 : $filter('number')(tempValue, 1));
                                }
                                $scope.tempJson.push({"value": tempValue});
                            });
                        }

                        var maxTmp = 8, minTmp = 2;
                        if ($scope.currentAsset.at == 1) {
                            if($scope.assetDetails.rel && $scope.getObjectLength($scope.assetDetails.rel) > 0){
                                for(var item in $scope.assetDetails.rel){
                                    if($scope.assetDetails.rel[item].sId == $scope.currentSensor){
                                        try{
                                            maxTmp = $scope.assetDetails.rel[item].meta.alarm.high.temp;
                                            minTmp = $scope.assetDetails.rel[item].meta.alarm.low.temp;
                                        }catch(error){
                                            //do nothing
                                        }
                                        break;
                                    }
                                }
                            }else{
                                maxTmp = checkNotNullEmpty($scope.assetDetails) && checkNotNullEmpty($scope.assetDetails.meta)
                                && checkNotNullEmpty($scope.assetDetails.meta.tmp) && checkNotNullEmpty($scope.assetDetails.meta.tmp.max)
                                    ? $scope.assetDetails.meta.tmp.max : 8;

                                minTmp = checkNotNullEmpty($scope.assetDetails) && checkNotNullEmpty($scope.assetDetails.meta)
                                && checkNotNullEmpty($scope.assetDetails.meta.tmp) && checkNotNullEmpty($scope.assetDetails.meta.tmp.min)
                                    ? $scope.assetDetails.meta.tmp.min : 2;
                            }
                        } else {
                            maxTmp = checkNotNullEmpty($scope.currentRelatedAsset) && checkNotNullEmpty($scope.currentRelatedAsset.meta)
                            && checkNotNullEmpty($scope.currentRelatedAsset.meta.tmp) && checkNotNullEmpty($scope.currentRelatedAsset.meta.tmp.max)
                                ? $scope.currentRelatedAsset.meta.tmp.max : 8;

                            minTmp = checkNotNullEmpty($scope.currentRelatedAsset) && checkNotNullEmpty($scope.currentRelatedAsset.meta)
                            && checkNotNullEmpty($scope.currentRelatedAsset.meta.tmp) && checkNotNullEmpty($scope.currentRelatedAsset.meta.tmp.min)
                                ? $scope.currentRelatedAsset.meta.tmp.min : 2;
                        }

                        var deviceName = $scope.vendorId;
                        var deviceId = $scope.assetDetails.dId;

                        var dt = new Date();
                        var today = parseUrlDate(new Date(dt.getFullYear(), dt.getMonth(), dt.getDate()));
                        var dataJson = {
                            "chart": {
                                "xAxisName": "Time",
                                "yAxisName": "Temperature",
                                "numberSuffix": "°C",
                                "showValues": "1",
                                "showBorder": "0",
                                "bgColor": "#ffffff",
                                "paletteColors": "#008ee4",
                                "showCanvasBorder": "0",
                                "showAxisLines": "0",
                                "showAlternateHGridColor": "0",
                                "flatScrollBars": "1",
                                "scrollheight": "5",
                                "showHoverEffect": "1",
                                "exportEnabled": "1",
                                "lineThickness": "2",
                                "theme": "fint",
                                "yAxisMaxValue": parseInt(maxTmp) + 5,
                                "yAxisMinValue": parseInt(minTmp) - 5,
                                "adjustDiv": "0",
                                "numDivLines": "4",
                                "showZeroPlaneValue": "0",
                                "zeroPlaneAlpha": "0",
                                "pixelsPerPoint": "0",
                                "pixelsPerLabel": "30",
                                "labelDisplay": "rotate",
                                "exportFileName": "temperature" + "_" + deviceName + "_" + deviceId + "_" + FormatDate_DD_MM_YYYY(today)
                            },
                            "categories": [
                                {
                                    "category": $scope.categoryJson
                                }
                            ],
                            "dataset": [
                                {
                                    "data": $scope.tempJson
                                }
                            ],
                            "trendlines": {
                                "line": [
                                    {
                                        "startvalue": minTmp,
                                        "color": "#00c0ef",
                                        "valueOnRight": "1"
                                    }, {
                                        "startvalue": maxTmp,
                                        "color": "#ff0000",
                                        "valueOnRight": "1"
                                    }
                                ]
                            }
                        };

                        var noDataMessage = checkNullEmpty(tdate) ? "No data available" : "No data available for " + formatDate(tdate);
                        FusionCharts.ready(function () {
                            var chartReference = FusionCharts("temp-map");
                            if (checkNotNullEmpty(chartReference)) {
                                chartReference.dispose();
                            }
                            var deviceByState = new FusionCharts({
                                type: 'Zoomline',
                                id: 'temp-map',
                                renderAt: 'chart-container',
                                width: '100%',
                                height: '288',
                                dataFormat: 'json',
                                showBorder: '0',
                                dataEmptyMessage: noDataMessage,
                                dataSource: JSON.stringify(dataJson)
                            });
                            deviceByState.render();
                        });
                    }
                }).catch(function error(msg) {
                    console.error(msg);
                }).finally(function () {
                    $scope.deviceTempLoading = false;
                });
            }
        };

        $scope.refreshTemperature = function(){
            $scope.currentRelatedAsset = $scope.assetRelations[$scope.currentMpId];
            $scope.categoryJson = [];
            $scope.tempJson = [];
            $scope.updateTempGraph();
        };

        $scope.getTemperature = function(){
            $scope.categoryJson = [];
            $scope.tempJson = [];
            $scope.updateTempGraph($scope.tday);
        };

        $scope.fetchTemperature = function(){
            $scope.currentRelatedAsset = $scope.assetRelations[$scope.currentMpId];
            $scope.getTemperature();
        };

        $scope.updateMonitoringPositionFilter = function(value){
            $scope.currentMpId = value;
            $scope.fetchTemperature();
        };

        $scope.updateCurrentSensor = function(value){
            $scope.currentSensor = value;
            $scope.fetchTemperature();
        }
    }
]);

assetControllers.controller('AssetConfigController', ['$scope', 'assetService',
    function($scope, assetService){
        $scope.config = {};
        $scope.asset = {};
        $scope.invalidStatusUrl = false;
        $scope.sensorConfigOverrideKeys = [{grp:'comm', key: 'tmpNotify'}, {grp:'comm', key: 'incExcNotify'}, {grp:'comm', key: 'statsNotify'},
            {grp:'comm', key: 'devAlrmsNotify'}, {grp:'comm', key: 'tmpAlrmsNotify'},
            {grp:'comm', key: 'samplingInt'}, {grp:'comm', key: 'pushInt'},
            {grp:'highAlarm', key: 'temp'}, {grp:'highAlarm', key: 'dur'},
            {grp:'lowAlarm', key: 'temp'}, {grp:'lowAlarm', key: 'dur'},
            {grp:'highWarn', key: 'temp'}, {grp:'highWarn', key: 'dur'},
            {grp:'lowWarn', key: 'temp'}, {grp:'lowWarn', key: 'dur'},
            {grp:'notf', key: 'dur'}, {grp:'notf', key: 'num'}];

        $scope.$watch("currentAsset", function(){
            if(checkNotNullEmpty($scope.currentAsset)){
                $scope.getAssetConfig();
            }
        });

        $scope.getAssetConfig = function () {
            $scope.showLoading();
            assetService.getAssetConfig($scope.vendorId, $scope.deviceId).then(function (data) {
                $scope.config = data.data.data;
                if(checkNotNullEmpty($scope.config) && checkNotNullEmpty($scope.config.comm) && checkNotNullEmpty($scope.config.comm.usrPhones)){
                    $scope.usrPhones = $scope.config.comm.usrPhones;
                    $scope.config.comm.usrPhones = $scope.config.comm.usrPhones.toString();
                }
                $scope.updateSensors();
            }).catch(function error(msg) {
                $scope.showErrorMsg(msg);
            }).finally(function(){
                $scope.hideLoading();
            });
        };

        $scope.editConfig = function () {
            if(checkNullEmpty($scope.currentSns) && checkNotNullEmpty($scope.asset.sns)){
                for(var item in $scope.asset.sns){
                    if(checkNullEmpty($scope.currentSns) || $scope.asset.sns[item].sId < $scope.currentSns){
                        $scope.currentSns = $scope.asset.sns[item].sId;
                        $scope.currentSnsConfig = $scope.sensorConfig[$scope.currentSns];
                    }
                }
            }
            $scope.configEditable = true;
            $scope.configCopy = angular.copy($scope.config);
        };

        $scope.submitConfig = function (action) {
            $scope.showLoading();
            var pushConfig = (action == 1);
            if (checkNotNullEmpty($scope.config) && checkNotNullEmpty($scope.config.comm) && checkNotNullEmpty($scope.config.comm.usrPhones)) {
                var usrPhones = $scope.config.comm.usrPhones.split(",");
                $scope.config.comm.usrPhones = [];
                for(var i = 0; i < usrPhones.length ; i++){
                    if(checkNotNullEmpty(usrPhones[i].trim())){
                        $scope.config.comm.usrPhones.push(usrPhones[i]);
                    }
                }
            } else {
                $scope.config.comm.usrPhones = [];
            }
            $scope.usrPhones = $scope.config.comm.usrPhones;
            $scope.constructSensorConfig();
            $scope.configSubmission = {vId: $scope.vendorId, dId: $scope.assetDetails.dId, configuration: $scope.config};
            assetService.updateDeviceConfig($scope.configSubmission, $scope.domainId, pushConfig).then(function () {
                $scope.configEditable = false;
                $scope.showSuccess($scope.resourceBundle['deviceconfig.success']);
                $scope.getAssetConfig();
                $scope.getAssetDetails();
            }).catch(function error(err) {
                if(checkNotNullEmpty(err.data) && checkNotNullEmpty(err.data.message)){
                    $scope.showWarning(JSON.parse(err.data.message).message);
                }else{
                    $scope.showError($scope.resourceBundle['deviceconfig.fail']);
                }
                $scope.cancelConfigEdit();
            }).finally(function(){
                $scope.hideLoading();
            });
        };

        $scope.cancelConfigEdit = function () {
            $scope.configEditable = false;
            $scope.config = $scope.configCopy;
        };

        $scope.updateSensors = function(){
            $scope.asset.sns = [];
            for(var i in $scope.currentManu.model){
                if($scope.assetDetails.meta.dev.mdl == $scope.currentManu.model[i].name && checkNotNullEmpty($scope.currentManu.model[i].sns)){
                    $scope.asset.sns = [];
                    var sns = $scope.currentManu.model[i].sns;
                    for(var j in sns){
                        $scope.asset.sns.push({"sId": sns[j].name, "cd": sns[j].cd});
                    }
                }
            }
            $scope.updateSensorConfig();
        };

        $scope.updateSensorConfig = function(){
            //Updating device configuration for multi sensor devices
            $scope.sensorConfig = {};
            if($scope.currentAsset.iGe){
                if(checkNotNullEmpty(checkNotNullEmpty($scope.config))){
                    angular.forEach($scope.asset.sns,function(sensor, index){
                        var isConfigAvailable = false;
                        if(checkNotNullEmpty($scope.config.sensors) && $scope.config.sensors.length > 0){
                            angular.forEach($scope.config.sensors, function(sensorConfig){
                                if(sensorConfig.sId == sensor.sId){
                                    isConfigAvailable = true;
                                    var tmpSensorConfig = angular.copy(sensorConfig);
                                    tmpSensorConfig.useDefault = false;
                                    angular.forEach($scope.sensorConfigOverrideKeys, function(value){
                                        if(checkNullEmpty(tmpSensorConfig[value.grp])){
                                            tmpSensorConfig[value.grp] = {};
                                            tmpSensorConfig[value.grp][value.key] = "";
                                        }
                                        if(checkNullEmpty(tmpSensorConfig[value.grp][value.key]) && checkStrictNotNullEmpty($scope.config[value.grp][value.key])){
                                            tmpSensorConfig[value.grp][value.key] = $scope.config[value.grp][value.key];
                                        }
                                    });
                                    $scope.sensorConfig[sensor.sId] = tmpSensorConfig;
                                }
                            });
                        }

                        if(!isConfigAvailable){
                            var tmpSensorConfig = {sId: sensor.sId, useDefault: true};
                            angular.forEach($scope.sensorConfigOverrideKeys, function(value){
                                if(checkNullEmpty(tmpSensorConfig[value.grp])){
                                    tmpSensorConfig[value.grp] = {};
                                    tmpSensorConfig[value.grp][value.key] = "";
                                }
                                if(checkNotNullEmpty($scope.config[value.grp]) && checkNotNullEmpty($scope.config[value.grp][value.key])){
                                    tmpSensorConfig[value.grp][value.key] = $scope.config[value.grp][value.key];
                                }
                            });
                            $scope.sensorConfig[sensor.sId] = tmpSensorConfig;
                        }
                        if(isConfigAvailable && (checkNullEmpty($scope.currentSns) || sensor.sId < $scope.currentSns)
                            && checkNotNullEmpty($scope.sensorConfig) && checkNotNullEmpty($scope.sensorConfig[sensor.sId])
                        ){
                            $scope.currentSns = sensor.sId;
                            $scope.currentSnsConfig = $scope.sensorConfig[$scope.currentSns];
                        }
                    });
                }
            }
        };

        $scope.resetCurrentSensorConfig = function(){
            if($scope.currentSnsConfig.useDefault){
                var tmpSensorConfig = {sId: $scope.currentSns, useDefault: true};
                angular.forEach($scope.sensorConfigOverrideKeys, function(value){
                    if(checkNullEmpty(tmpSensorConfig[value.grp])){
                        tmpSensorConfig[value.grp] = {};
                        tmpSensorConfig[value.grp][value.key] = "";
                    }
                    if(checkNotNullEmpty($scope.config[value.grp]) && checkNotNullEmpty($scope.config[value.grp][value.key])){
                        tmpSensorConfig[value.grp][value.key] = $scope.config[value.grp][value.key];
                    }
                });
                $scope.sensorConfig[$scope.currentSns] = tmpSensorConfig;
                $scope.currentSnsConfig = tmpSensorConfig;
            }
        };

        $scope.updateCurrentSns = function(value){
            $scope.currentSns = value;
        };

        $scope.setCurrentSensorConfig = function(value){
            $scope.sensorConfig[$scope.currentSns] = $scope.currentSnsConfig;
            $scope.currentSns = value;
            $scope.currentSnsConfig = $scope.sensorConfig[value];
        };

        $scope.constructSensorConfig = function(){
            $scope.config.sensors = [];
            $scope.sensorConfig[$scope.currentSns] = $scope.currentSnsConfig;
            if(checkNotNullEmpty($scope.sensorConfig)){
                angular.forEach($scope.sensorConfig, function(sensor){
                    if(!sensor.useDefault) {
                        var tmpSensorConfig = {sId: sensor.sId};
                        angular.forEach($scope.sensorConfigOverrideKeys, function(value){
                            if(checkStrictNotNullEmpty(sensor[value.grp][value.key])
                                && $scope.config[value.grp][value.key] != undefined
                                && sensor[value.grp][value.key] != $scope.config[value.grp][value.key]){
                                if(checkNullEmpty(tmpSensorConfig[value.grp])){
                                    tmpSensorConfig[value.grp] = {};
                                }
                                tmpSensorConfig[value.grp][value.key] = sensor[value.grp][value.key];
                            }
                        });
                        $scope.config.sensors.push(tmpSensorConfig);
                    }
                });
            }
        };

        $scope.pushPullConfig = function (type) {
            $scope.configPushPullRequest = {typ: type, vId: $scope.vendorId, dId: $scope.assetDetails.dId};
            if(type == 1)
                $scope.pushingConfig = true;
            else
                $scope.sendingConfigPullRequest = true;
            assetService.pushPullConfig($scope.configPushPullRequest, $scope.domainId).then(function (data) {
                $scope.showSuccess($scope.resourceBundle['config.pushsuccess']);
                $scope.getAssetDetails();
            }).catch(function error(err) {
                if(checkNotNullEmpty(err.data) && checkNotNullEmpty(err.data.message)){
                    $scope.showWarning(JSON.parse(err.data.message).message);
                }else{
                    $scope.showErrorMsg(err, true);
                }
            }).finally(function(data){
                if(type == 1)
                    $scope.pushingConfig = false;
                else
                    $scope.sendingConfigPullRequest = false;
            });
        };

        $scope.checkFormValidity = function() {
            $scope.invalidStatusUrl = false;
            $scope.sensorMsg = "";
            $scope.required = false;
            var invalidSensorMsg = [];
            $scope.sensorConfigEnabled = false;
            for(item in $scope.sensorConfig) {
                if($scope.sensorConfig[item].comm.statsNotify) {
                    $scope.sensorConfigEnabled = true;
                    invalidSensorMsg.push(item);
                }
            }
            if(invalidSensorMsg.length > 0) {
                invalidSensorMsg.sort();
                $scope.sensorMsg = "Enable stats push is enabled in sensor: " + invalidSensorMsg;

            }
            if($scope.config.comm.statsNotify || $scope.sensorConfigEnabled) {
                if(checkNullEmpty($scope.config.comm.statsUrl)) {
                    $scope.invalidStatusUrl = true;
                    $scope.required = true;
                }
            }
            return $scope.invalidStatusUrl;
        }
    }
]);

assetControllers.controller('AssetStatsController', ['$scope', 'requestContext', 'assetService', '$location',
    function($scope, requestContext, assetService, $location){
        $scope.wparams = [["day","day","",formatDate2Url]];
        ListingController.call(this, $scope, requestContext, $location);
        $scope.init = function(){
            $scope.day = parseUrlDate(requestContext.getParam("day"));
            if(checkNullEmpty($scope.day)){
                $scope.day = new Date();
                $scope.day.setDate($scope.day.getDate() - 1);
            }
            $scope.dDate = FormatDate_MMMM_DD_YYYY($scope.day);
        };
        $scope.init();
        $scope.fetch = function() {
            $scope.getStats();
        };

        $scope.getStats = function () {
            $scope.showLoading();
            var generatedDate = new Date($scope.day);
            generatedDate.setHours(0);
            generatedDate.setMinutes(0);
            generatedDate.setSeconds(0);
            generatedDate.setMilliseconds(0);
            var fromDate = generatedDate.getTime()/1000 - generatedDate.getTimezoneOffset() * 60;
            //fromDate = fromDate - offsetInSeconds;
            generatedDate.setDate(generatedDate.getDate() + 1);
            var endDate = generatedDate.getTime()/1000 - generatedDate.getTimezoneOffset() * 60;
            //endDate = endDate - offsetInSeconds;

            assetService.getAssetStats($scope.vendorId, $scope.deviceId, fromDate, endDate).then(function (data) {
                if(checkNotNullEmpty(data.data)){
                    var statsRec = data.data;
                    if (checkNotNullEmpty(statsRec.data) && statsRec.data.length > 0) {
                        $scope.statsLoading = 2;
                        $scope.deviceStats = statsRec.data[0].stats;
                    }else {
                        $scope.statsLoading = 1;
                        $scope.deviceStats = {};
                    }
                }else {
                    $scope.statsLoading = 1;
                    $scope.deviceStats = {};
                }
            }).catch(function(msg) {
                $scope.statsLoading = 1;
                $scope.deviceStats = {};
                $scope.showErrorMsg(msg);
            }).finally(function(){
                $scope.hideLoading();
            });
        };
        $scope.fetch();
    }
]);

assetControllers.controller('AssetsDashboardController', ['$scope', '$location', '$window', 'assetService', 'domainCfgService', 'requestContext','domainService',
    function ($scope, $location, $window, assetService, domainCfgService, requestContext, domainService) {
        $scope.assetTypeFilter = 2;
        $scope.domainId = "";
        $scope.locationMapping = {};
        $scope.topLocation = "";
        $scope.countryCode = "";
        $scope.stateCode = "";
        $scope.tagSummary = {"nDevices": 0, "nAbnormalTemps": 0, "nAbnormalDevices": 0};
        $scope.childTagSummary = {};
        $scope.tagAbnormalDevices = {};
        $scope.loadingTagAbnormalDevices = true;
        $scope.chartOptions = {};
        $scope.colorRange = {};
        $scope.dataJson = [];
        $scope.chartData = {};
        $scope.mapJson = {};
        $scope.selectedLocStats = {"nDevices": 0, "nAbnormalTemps": 0, "nAbnormalDevices": 0, "nIActDevices": 0, "location": "None"};
        $scope.currentFilter = 1;
        $scope.finished = 0;
        $scope.mparams = ["tid", "st", "view"];
        $scope.view = 'map';
        $scope.isMapSupported = true;
        $scope.mapLoading = true;
        var dt = new Date();
        $scope.today = parseUrlDate(new Date(dt.getFullYear(), dt.getMonth(), dt.getDate()));
        $scope.filterResourceBundleKey = ["temperature.alarms", "device.alarms"];
        $scope.firstInit = true;

        $scope.$watch("view", watchfn("view", "", $location, $scope));

        $scope.mapClickListener = function (eventObj, eventArgs) {
            var dataObj = JSON.parse(JSON.stringify($scope.dataJson));
            var isNullLocation = true;
            $scope.$apply(function () {
                $scope.firstInit = false;
                for (var i = 0; i < dataObj.length; i++) {
                    if (checkNotNullEmpty(dataObj[i].id) && dataObj[i].id.toLowerCase() == eventArgs.id) {
                        $scope.selectedLocStats.nDevices = dataObj[i].nDevices;
                        $scope.selectedLocStats.nAbnormalTemps = dataObj[i].nAbnormalTemps;
                        $scope.selectedLocStats.nAbnormalDevices = dataObj[i].nAbnormalDevices;
                        $scope.selectedLocStats.nIActDevices = dataObj[i].nIActDevices;
                        $scope.selectedLocStats.location = dataObj[i].location;
                        isNullLocation = false;
                    }
                }

                if(isNullLocation){
                    $scope.selectedLocStats.nDevices = 0;
                    $scope.selectedLocStats.nAbnormalTemps = 0;
                    $scope.selectedLocStats.nAbnormalDevices = 0;
                    $scope.selectedLocStats.nIActDevices = 0;
                    $scope.selectedLocStats.location = eventArgs.label;
                }

                /*$scope.tagAbnormalDevices = {};
                 $scope.loadingTagAbnormalDevices = true;
                 assetService.getTagSummary($scope.domainId + '.' + eventArgs.label)
                 .then(function (data) {
                 if(checkNotNullEmpty(data.data))
                 $scope.tagAbnormalDevices = JSON.parse(angular.fromJson(data.data));
                 $scope.loadingTagAbnormalDevices = false;
                 }).catch(function error(msg) {
                 $scope.showErrorMsg(msg);
                 });*/
            });
        };

        $scope.getFiltered = function(){
            var filtered = [];
            if(checkNotNullEmpty($scope.tagAbnormalDevices)){
                for(var item in $scope.tagAbnormalDevices.data){
                    if($scope.currentFilter == 1 && $scope.tagAbnormalDevices.data[item].st == 3 && checkNotNullEmpty($scope.tagAbnormalDevices.data[item].tmp)){
                        filtered.push($scope.tagAbnormalDevices.data[item]);
                    } else if($scope.currentFilter == 2 && checkNotNullEmpty($scope.tagAbnormalDevices.data[item].alrm) && $scope.tagAbnormalDevices.data[item].alrm.length > 0){
                        filtered.push($scope.tagAbnormalDevices.data[item]);
                    }
                }
            }
            return filtered;
        };

        $scope.generateValue = function (filter) {
            $scope.dataJson = JSON.parse(JSON.stringify($scope.childTagSummary));
            for (var i = 0; i < $scope.dataJson.length; i++) {
                var tempLoc = $scope.dataJson[i].tag.substr($scope.dataJson[i].tag.lastIndexOf(".") + 1);
                if (tempLoc != "") {
                    angular.forEach($scope.locationMapping.data, function (value, key) {
                        if (key === $scope.countryCode) {
                            angular.forEach(value.states, function (value, key) {
                                $scope.dataJson[i].location = tempLoc;
                                if (key == tempLoc || key == $scope.stateCode) {
                                    if (checkNotNullEmpty($scope.stateCode)) {
                                        angular.forEach(value.districts, function (value, key) {
                                            if (key == tempLoc) {
                                                $scope.dataJson[i].id = value.id;
                                            }
                                        });
                                    } else {
                                        $scope.dataJson[i].id = value.id;
                                        if($scope.checkMapSupport(tempLoc) && $scope.dataJson[i].nDevices != 0)
                                            $scope.dataJson[i].link = "#/assets/dashboard?tid=" + $scope.dataJson[i].tag + "&st=" + tempLoc;
                                    }
                                }
                            });
                        }
                    });

                    if (filter == 3 || $scope.dataJson[i].nDevices == 0)
                        $scope.dataJson[i].value = "";
                    else if (filter == 1)
                        $scope.dataJson[i].value = Math.abs($scope.dataJson[i].nAbnormalDevices / $scope.dataJson[i].nDevices * 100);
                    else if (filter == 2)
                        $scope.dataJson[i].value = Math.abs($scope.dataJson[i].nAbnormalDevices / $scope.dataJson[i].nDevices) * 100;
                }
            }
        };

        $scope.filterChange = function (filter) {
            $scope.currentFilter = filter;
            $scope.loading = true;
            $scope.showLoading();
            var color = "00c0ef";
            var caption = $scope.resourceBundle['temperature.alarms'] + " "
                + $scope.resourceBundle['in'] + " "
                + $scope.topLocation;

            if ($scope.currentFilter == 2) {
                color = "FF6C47";
                caption = $scope.resourceBundle['devices.alarms'] + " "
                    + $scope.resourceBundle['in'] + " "
                    + $scope.topLocation;
            } else if ($scope.currentFilter == 3) {
                caption = $scope.resourceBundle['nodata.in'] + " " + $scope.topLocation;
                color = "8D8D8D";
            }

            $scope.generateValue($scope.currentFilter);

            var chartOptionsObj = JSON.parse(JSON.stringify($scope.chartOptions));
            chartOptionsObj.caption = caption;

            var colorRangeObj = JSON.parse(JSON.stringify($scope.colorRange));
            colorRangeObj.color[0].code = color;

            var chartDataOptions = JSON.parse(JSON.stringify($scope.chartData));
            chartDataOptions.colorrange = colorRangeObj;
            chartDataOptions.map = chartOptionsObj;
            chartDataOptions.data = $scope.dataJson;

            var deviceByState = FusionCharts.items['logistimo-map'];
            deviceByState.setChartData(JSON.stringify(chartDataOptions));
            deviceByState.render();
            $scope.loading = false;
            $scope.hideLoading();
        };

        $scope.checkMapSupport = function(locationValue){
            var isMapSupported = false;
            angular.forEach($scope.locationMapping.support, function (value, key) {
                if(value && value.name && value.name == locationValue){
                    isMapSupported = true;
                }

                if(value.states && value.states.indexOf(locationValue) != -1){
                    isMapSupported = true;
                }
            });
            return isMapSupported;
        };

        $scope.renderMap = function () {
            $scope.isMapSupported = $scope.checkMapSupport($scope.topLocation);
            if($scope.isMapSupported){
                $scope.mapLoading = true;
                $scope.childTagSummary = undefined;
                assetService.getChildTagSummary($scope.domainId)
                    .then(function (data) {
                        $scope.mapLoading = false;
                        if(checkNotNullEmpty(data.data)){
                            $scope.childTagSummary = angular.fromJson(data.data);

                            var color = "00c0ef";
                            var caption = $scope.resourceBundle['temperature.alarm'] + " " + $scope.topLocation;

                            if ($scope.currentFilter == 2) {
                                color = "FF6C47";
                                caption = $scope.resourceBundle['devices.alarm'] + " " + $scope.topLocation;
                            } else if ($scope.currentFilter == 3) {
                                caption = $scope.resourceBundle['nodata.in'] + " " + $scope.topLocation;
                                color = "8D8D8D";
                            }

                            $scope.chartOptions = {
                                "caption": caption,
                                "subcaption": "Current",
                                "entityFillHoverColor": "cccccc",
                                "showLabels": "1",
                                "numberSuffix": "%",
                                "nullEntityColor": "#EEEEEE",
                                "nullEntityAlpha": "100",
                                "hoverOnNull": "0",
                                "exportEnabled": "1",
                                "showCanvasBorder": 0,
                                "exportFileName": "Map" + "_" + $scope.topLocation + "_" + FormatDate_DD_MM_YYYY($scope.today)
                            };

                            $scope.colorRange = {
                                "minvalue": "0",
                                "startlabel": "Normal",
                                "endlabel": "Alert",
                                "code": "FFFFFF",
                                "gradient": "1",
                                "color": [
                                    {
                                        "maxvalue": "100",
                                        "code": color
                                    }
                                ]
                            };

                            $scope.generateValue($scope.currentFilter);

                            $scope.chartData = {
                                "map": $scope.chartOptions,
                                "colorrange": $scope.colorRange,
                                "data": $scope.dataJson
                            };

                            $scope.mapJson = {
                                type: 'maps/' + $scope.topLocation.replace(" ", ""),
                                id: 'logistimo-map',
                                renderAt: 'chart-container',
                                width: '100%',
                                height: '600',
                                dataFormat: 'json',
                                showBorder: '0'
                            };

                            FusionCharts.ready(function () {
                                var chartReference = getChartFromId("logistimo-map");
                                if(checkNotNullEmpty(chartReference)){
                                    chartReference.dispose();
                                }
                                var deviceByState = new FusionCharts({
                                    type: 'maps/' + $scope.topLocation.replace(" ", ""),
                                    id: 'logistimo-map',
                                    renderAt: 'chart-container',
                                    width: '100%',
                                    height: '600',
                                    dataFormat: 'json',
                                    dataSource: JSON.stringify($scope.chartData)
                                });
                                deviceByState.addEventListener("entityClick", $scope.mapClickListener);
                                deviceByState.render();
                            });
                        }
                    }).catch(function error(msg) {
                        console.error(msg);
                    }).finally(function(){
                        $scope.loading = false;
                        $scope.hideLoading();
                    });
            }else{
                $scope.loading = false;
                $scope.hideLoading();
            }
        };

        var renderContext = requestContext.getRenderContext(requestContext.getAction(), $scope.mparams);
        $scope.$on("requestContextChanged", function () {
            if (!renderContext.isChangeRelevant()) {
                return;
            }
            $scope.init();
        });

        $scope.init = function () {
            $scope.loading = true;
            $scope.firstInit = true;
            $scope.showLoading();

            if(checkNotNullEmpty(requestContext.getParam("view"))){
                $scope.view = requestContext.getParam("view");
            }else{
                $scope.view = 'map';
            }

            if (checkNotNullEmpty(requestContext.getParam("tid"))) {
                $scope.domainId = requestContext.getParam("tid");
                if (checkNotNullEmpty(requestContext.getParam("st"))) {
                    $scope.stateCode = requestContext.getParam("st");
                    $scope.topLocation = $scope.stateCode;
                    $scope.fetchData();
                }else {
                    $scope.getDomainLocation();
                }
            } else{
                if(checkNullEmpty($scope.currentDomain)){
                    domainService.getCurrentDomain().then(function (data){
                        $scope.domainId = data.data.dId;
                        $scope.getDomainLocation();
                    });
                }else{
                    $scope.domainId = angular.copy($scope.currentDomain);
                    $scope.getDomainLocation();
                }
            }
        };

        $scope.fetchData = function(){
            $scope.finished = 0;
            $scope.getCountryMapping();
            $scope.getTagSummary();
            //$scope.getTagAbnormalDevices();
        };

        $scope.getDomainLocation = function(){
            assetService.getDomainLocation().then(function( data ){
                $scope.stateCode = data.data.st;
                $scope.topLocation = data.data.tl;
                $scope.countryCode = data.data.cc;
                if(checkNotNullEmpty($scope.stateCode))
                    $scope.domainId = $scope.domainId + "." + $scope.stateCode;
                $scope.fetchData();
            }).finally(function(){
                $scope.loading = false;
                $scope.hideLoading();
            });
        };

        $scope.getCountryMapping = function(){
            domainCfgService.getMapLocationMapping()
                .then(function (data) {
                    if(checkNotNullEmpty(data.data)){
                        $scope.locationMapping = angular.fromJson(data.data);
                    } else{
                        $scope.showError($scope.resourceBundle['location.mapping']);
                    }
                    $scope.finished++;
                    if($scope.finished == 2){
                        $scope.renderMap();
                    }
                }).catch(function error(msg) {
                    $scope.showErrorMsg(msg);
                }).finally(function(){
                    $scope.loading = false;
                    $scope.hideLoading();
                });
        };

        $scope.getTagSummary = function(){
            assetService.getTagSummary($scope.domainId, $scope.assetTypeFilter)
                .then(function (data) {
                    if(checkNotNullEmpty(data.data)){
                        $scope.tagSummary = angular.fromJson(data.data);
                        $scope.selectedLocStats = angular.copy($scope.tagSummary);
                        $scope.selectedLocStats.location = $scope.topLocation;
                    }

                    $scope.finished++;
                    if($scope.finished == 2){
                        $scope.renderMap();
                    }
                }).catch(function error(msg) {
                    $scope.showErrorMsg(msg);
                }).finally(function(){
                    $scope.loading = false;
                    $scope.hideLoading();
                });
        };

        /*$scope.getTagAbnormalDevices = function(){
         assetService.getTagAbnormalDevices($scope.domainId)
         .then(function (data) {
         if(checkNotNullEmpty(data.data))
         $scope.tagAbnormalDevices = JSON.parse(angular.fromJson(data.data));
         $scope.loadingTagAbnormalDevices = false;
         }).catch(function error(msg) {
         $scope.showErrorMsg(msg);
         }).finally(function(){
         $scope.loading = false;
         $scope.hideLoading();
         });
         };*/

        $scope.handleAlertClick = function (type, location) {
            var url = '#/assets/all?alrm=' + type + '&dur=0&&eid=0';
            if(checkNotNullEmpty(location)){
                if(checkNotNullEmpty($scope.stateCode)){
                    location = $scope.stateCode + "." + location;
                }
                url += '&loc=' + location;
            }

            $window.location.href = url;
        };

        $scope.updateTypeFilter = function(value){
            $scope.assetTypeFilter = value;
            $scope.currentAsset = $scope.assetConfig.assets[value];
            if($scope.assetTypeFilter == 1)
                $scope.filterChange(2);
            else if($scope.assetTypeFilter == 2)
                $scope.filterChange(1);

        };

        $scope.init();
    }]);

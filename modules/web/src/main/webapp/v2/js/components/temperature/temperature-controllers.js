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

var tempControllers = angular.module('tempControllers', []);

tempControllers.controller('AddTemperatureDeviceController', ['$scope', 'domainCfgService', 'tempService', 'matService',
    function ($scope, domainCfgService, tempService, matService) {
        $scope.isDirty = false;
        $scope.hasErrors = {};
        $scope.init = function () {
            $scope.removeDevices = [];
            domainCfgService.getTempVendorMap().then(function (data) {
                $scope.vendorNames = data.data;
            }).catch(function error(msg) {
                $scope.showErrorMsg(msg);
            });
            matService.get($scope.mid).then(function (data) {
                $scope.material = data.data;
            }).catch(function error(msg) {
                $scope.showErrorMsg(msg);
            });
        };
        $scope.init();
        $scope.fetch = function () {
            tempService.getMaterialDevices($scope.entityId, $scope.mid).then(function (data) {
                $scope.devices = data.data;
            }).catch(function error(msg) {
                $scope.showErrorMsg(msg);
            });
        };
        $scope.fetch();
        $scope.addRow = function () {
            $scope.devices.push({added: true, min: $scope.material.tmin, max: $scope.material.tmax});
            $scope.hasErrors[$scope.devices.length - 1] = {slno: false, vid: false};
            $scope.isDirty = true;
        };
        $scope.saveDevices = function () {
            if (!$scope.isDirty) {
                $scope.showError($scope.resourceBundle['temperature.nomodification'], true);
                return;
            }
            if ($scope.validateSlNo() && $scope.validateVendor() && $scope.validateMin() && $scope.validateMax()) {
                tempService.saveInvntryItems($scope.entityId, $scope.mid, $scope.devices, $scope.removeDevices).then(function (data) {
                    var resp = data.data;
                    if (resp.success) {
                        $scope.showSuccess($scope.resourceBundle['device.save'] + " " + resp.errMsgs);
                    } else {
                        $scope.showError($scope.resourceBundle['device.fail'] + " " + resp.errMsgs, true);
                    }
                    $scope.toggle($scope.index);
                }).catch(function error(msg) {
                    $scope.showErrorMsg(msg);
                });
            }
        };
        $scope.close = function () {
            if (!$scope.isDirty || confirm($scope.resourceBundle['unsaved.changes'])) {
                $scope.toggle($scope.index);
            }
        };
        $scope.validateSlNo = function () {
            var status = true;
            var slnos = {};
            var index = 1;
            $scope.devices.forEach(function (device) {
                if (!slnos[device.id + ':' + device.vid]) {
                    slnos[device.id + ':' + device.vid] = true;
                    if (device.added) {
                        $scope.hasErrors[index - 1].slno = false;
                    }
                } else {
                    $scope.showError($scope.resourceBundle['device.serialnum'] + " '" + device.id + "' " + $scope.resourceBundle['error.alreadyexists']);
                    $scope.hasErrors[index - 1].slno = true;
                    status = false;
                }
                if (device.added) {
                    if (checkNullEmpty(device.id)) {
                        $scope.showError($scope.resourceBundle['sno.mandatory'] + " " + index);
                        $scope.hasErrors[index - 1].slno = true;
                        status = false;
                    } else {
                        $scope.hasErrors[index - 1].slno = false;
                    }
                }
                index++;
            });
            return status;
        };
        $scope.validateVendor = function () {
            return $scope.validate('vid', "Vendor");
        };
        $scope.validateMin = function () {
            return $scope.validate('min', "Min Temperature");
        };
        $scope.validateMax = function (index) {
            return $scope.validate('max', "Max Temperature");
        };
        $scope.validate = function (prop, desc) {
            var index = 1;
            var status = true;
            $scope.devices.forEach(function (device) {
                if (device.added) {
                    if (checkNullEmpty(device[prop])) {
                        $scope.showError(desc + " " + $scope.resourceBundle['ismandatory'] +  " " + device.id);
                        $scope.hasErrors[index - 1][prop] = true;
                        status = false;
                    } else {
                        $scope.hasErrors[index - 1][prop] = false;
                    }
                    if (prop == 'max') {
                        if (parseFloat(device.min) > parseFloat(device.max)) {
                            $scope.hasErrors[index - 1]['max'] = true;
                            $scope.showError($scope.resourceBundle['minlessmax'] + " " + device.id);
                        }
                    }
                }
                index++;
            });
            return status;
        };
        $scope.deleteRow = function (index) {
            if (!$scope.devices[index].added) {
                $scope.removeDevices.push($scope.devices[index]);
                $scope.isDirty = true;
            }
            $scope.devices.splice(index, 1);
            var added = $scope.removeDevices.length > 0;
            $scope.devices.some(function (item) {
                if (item.added) {
                    added = true;
                }
            });
            $scope.isDirty = !!added;
        };
        $scope.today = formatDate2Url(new Date());
    }
]);
tempControllers.controller('TempDetailCtrl', ['$scope', 'tempService',
    function ($scope, tempService) {
        $scope.loading = true;
        function getColor(tmp, min, max) {
            if (tmp < min || tmp > max) {
                return "red";
            }
            return "green";
        }
        $scope.cHeight = "300";
        $scope.cWidth = "600";
        $scope.cType = "mscombi2d";
        var dt = new Date();
        var today = parseUrlDate(new Date(dt.getFullYear(), dt.getMonth(), dt.getDate()));
        $scope.cOptions = {
            "yAxisName": "Temperature",
            "exportEnabled": '1',
            "theme": "fint",
            "numberSuffix": "°C",
            "yAxisValueDecimals":"0",
            "adjustDiv":"0",
            "numDivLines":"4",
            "showZeroPlaneValue":"0",
            "zeroPlaneAlpha":"0",
            "exportFileName": "temperature" + "_" + $scope.kid + "_" + $scope.mid + "_" + FormatDate_DD_MM_YYYY(today)
        };
        tempService.getTempDetails($scope.kid, $scope.mid).then(function (data) {
            $scope.chartData = data.data;
            if(checkNotNullEmpty($scope.chartData)) {
                $scope.chartData.forEach(function (data) {
                    if (checkNotNull(data.cData)) {
                        var d = JSON.parse(data.cData);
                        if (d.data[0] != undefined) {
                            data.cTemp = d.data[0].tmp;
                            data.cTempClr = getColor(d.data[0].tmp, data.min, data.max);
                            data.time = new Date(d.data[0].time * 1000).toLocaleString(data.ctry);
                            if (checkNotNullEmpty(data) && checkNotNullEmpty(data.cData)) {
                                var jsonData = JSON.parse(data.cData).data;
                                data.cLabel = getFCCategoriesFromTempJSON(jsonData);
                                data.cData = [];
                                data.cData[0] = getFCSeriesFromTempJSON(jsonData, 'line');
                                data.cTrend = getFCTrend(data.min, data.max);
                                data.cOptions = angular.copy($scope.cOptions);
                                data.cOptions.yAxisMinValue = data.min - Math.abs(data.min) * 0.1;
                                if (data.cOptions.yAxisMinValue > 0) {
                                    data.cOptions.yAxisMinValue = 0;
                                }
                                data.cOptions.yAxisMaxValue = data.max * 1.1;
                            }
                        }
                    }
                });
            }
        }).catch(function error(msg) {
            $scope.showErrorMsg(msg);
        }).finally(function(){
            $scope.loading = false;
        });
    }
]);
tempControllers.controller('TempPageSlideController', ['$scope', '$timeout',
    function ($scope, $timeout) {
        $scope.checked = false;
        $scope.vc = false;
        $scope.updateChecked = function () {
            $scope.checked = !$scope.checked;
            $timeout(function () {
                $scope.vc = !$scope.vc;
            }, 600);
        };
        $scope.toggle = function () {
            $scope.updateChecked();
        };
    }
]);


tempControllers.controller('TempDashboardController', ['$scope', '$location', '$window', 'tempService', 'domainCfgService', 'requestContext','domainService',
    function ($scope, $location, $window, tempService, domainCfgService, requestContext, domainService) {
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

                $scope.tagAbnormalDevices = {};
                $scope.loadingTagAbnormalDevices = true;
                tempService.getTagAbnormalDevices($scope.domainId + '.' + eventArgs.label)
                    .then(function (data) {
                        if(checkNotNullEmpty(data.data))
                            $scope.tagAbnormalDevices = angular.fromJson(data.data);
                        $scope.loadingTagAbnormalDevices = false;
                    }).catch(function error(msg) {
                        console.error(msg);
                    });
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
                                            $scope.dataJson[i].link = "#/temperature/dashboard?tid=" + $scope.dataJson[i].tag + "&st=" + tempLoc;
                                    }
                                }
                            });
                        }
                    });

                    if (filter == 3 || $scope.dataJson[i].nDevices == 0)
                        $scope.dataJson[i].value = "";
                    else if (filter == 1)
                        $scope.dataJson[i].value = Math.abs($scope.dataJson[i].nAbnormalTemps / $scope.dataJson[i].nDevices * 100);
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
                tempService.getChildTagSummary($scope.domainId)
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

        $scope.init = function (domainId, topLocation, countryCode, stateCode) {
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
            $scope.getTagAbnormalDevices();
        }

        $scope.getDomainLocation = function(){
            tempService.getDomainLocation().then(function( data ){
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
        }

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
                    console.error(msg);
                }).finally(function(){
                    $scope.loading = false;
                    $scope.hideLoading();
                });
        }

        $scope.getTagSummary = function(){
            tempService.getTagSummary($scope.domainId)
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
                    console.error(msg);
                }).finally(function(){
                    $scope.loading = false;
                    $scope.hideLoading();
                });
        }

        $scope.getTagAbnormalDevices = function(){
            tempService.getTagAbnormalDevices($scope.domainId)
                .then(function (data) {
                    if(checkNotNullEmpty(data.data))
                        $scope.tagAbnormalDevices = angular.fromJson(data.data);
                    $scope.loadingTagAbnormalDevices = false;
                }).catch(function error(msg) {
                    console.error(msg);
                }).finally(function(){
                    $scope.loading = false;
                    $scope.hideLoading();
                });
        }

        $scope.handleAlertClick = function (type, location, locType) {
            var url = '#/assets/all?alrm=' + type + '&dur=0&&eid=0';
            if(checkNotNullEmpty(location)){
                url += '&loc=' + location + '&ltype=' + locType;
            }

            $window.location.href = url;
        };

        $scope.init();
    }]);

tempControllers.controller('TempAssetsController', ['$scope', '$location', '$window', 'tempService', 'entityService', 'domainCfgService', 'requestContext',
    function ($scope, $location, $window, tempService, entityService, domainCfgService, requestContext) {
        $scope.invntryItems = "";
        $scope.domainId = "";
        $scope.entityId = 0;
        $scope.deviceId = 0;
        $scope.vendorId = 0;
        $scope.currentFilter = 0;
        $scope.duration = 0;
        $scope.params = $location.path();
        $scope.filters = [{"value": "0", "displayValue": "All"},
            {"value": "1", "displayValue": "Temperature alarms"},
            {"value": "2", "displayValue": "Device alarms"},
            {"value": "3", "displayValue": "No data"},
            {"value": "4", "displayValue": "Normal items"}];
        $scope.currentFilter = 0;
        $scope.durSortOrder = true;
        $scope.currentFilterDuration = 0;
        $scope.location = "";
        $scope.locType = "";
        $scope.deviceConfig = {};
        $scope.filtered = {};

        DeviceFilterController.call(this, $scope, tempService);
        VendorFilterController.call(this, $scope, tempService);
        $scope.$watch("entity.id", watchfn("eid", "", $location, $scope));
        $scope.$watch("deviceId", watchfn("did", "", $location, $scope));
        $scope.$watch("vendorId", watchfn("vid", "", $location, $scope));
        $scope.$watch("currentFilter", watchfn("cf", "", $location, $scope));
        $scope.$watch("currentFilterDuration", watchfn("dur", "", $location, $scope));

        ListingController.call(this, $scope, requestContext, $location);
        $scope.mparams = ["did", "vid", "cf", "dur","eid", "o", "s"];

        $scope.init = function (firstTimeInit) {
            $scope.loading = true;
            $scope.showLoading();
            $scope.getDeviceConfig();

            if(checkNotNullEmpty(requestContext.getParam("eid")) && requestContext.getParam("eid") == 0){
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

            if (checkNotNullEmpty(requestContext.getParam("cf"))) {
                $scope.currentFilter = parseInt(requestContext.getParam("cf"));
            } else {
                $scope.currentFilter = 0;
            }

            if (checkNotNullEmpty(requestContext.getParam("dur"))) {
                $scope.currentFilterDuration = parseInt(requestContext.getParam("dur"));
                $scope.duration = $scope.currentFilterDuration;
            } else{
                $scope.currentFilterDuration = 0;
                $scope.duration = $scope.currentFilterDuration;
            }

            if (checkNotNullEmpty(requestContext.getParam("loc"))) {
                $scope.location = requestContext.getParam("loc")    ;
            }else{
                $scope.location = "";
            }

            if (checkNotNullEmpty(requestContext.getParam("ltype"))) {
                $scope.locType = requestContext.getParam("ltype")    ;
            }

            $scope.getAssets();
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

        $scope.getAssets = function(){
            var nDeviceId = $scope.deviceId == '0' ? -1 : $scope.deviceId;
            var nEntityId = $scope.entityId == '0' ? -1 : $scope.entityId;
            var nVendorId = $scope.vendorId == '0' ? -1 : $scope.vendorId;
            tempService.getAssets(nEntityId, nDeviceId,
                nVendorId, $scope.currentFilter, $scope.currentFilterDuration, $scope.location, $scope.locType, $scope.offset, $scope.size).then(function (data) {
                    if(data.data != null && data.data.results != null){
                        $scope.invntryItems = data.data;
                        $scope.filtered = $scope.invntryItems.results;
                        $scope.setResults($scope.invntryItems);
                    }else{
                        $scope.invntryItems = {results:[]};
                        $scope.setResults(null);
                    }
                }).catch(function error(msg) {
                    console.error(msg);
                }).finally(function(){
                    $scope.loading = false;
                    $scope.hideLoading();
                });
        }

        $scope.onDurationFilterChange = function () {
            $scope.currentFilterDuration = $scope.duration;
        };

        $scope.onAlarmFilterChange = function(value){
            $scope.currentFilter = value;
            if($scope.currentFilter == 0 || $scope.currentFilter == 4)
                $scope.currentFilterDuration = 0;
        }

        $scope.getDeviceConfig = function () {
            tempService.getDeviceConfig(-1, -1).then(function (data) {
                if(checkNotNullEmpty(data.data)){
                    var configRec = angular.fromJson(data.data);
                    if(configRec.data != undefined)
                        $scope.deviceConfig = configRec.data;
                    else
                        $scope.deviceConfig = configRec;

                    if($scope.deviceConfig.filter != undefined && $scope.duration == -1){
                        if($scope.currentFilter == 1)
                            $scope.duration = $scope.deviceConfig.filter.excursionFilterDuration;
                        else if($scope.currentFilter == 2)
                            $scope.duration = $scope.deviceConfig.filter.deviceAlarmFilterDuration;
                        else if($scope.currentFilter == 3)
                            $scope.duration = $scope.deviceConfig.filter.noDataFilterDuration;
                    }

                    $scope.currentFilterDuration = $scope.duration;
                }
            })
        };

        $scope.resetFilters = function(){
            $location.$$search = {};
            $location.$$compose();
            $scope.entity = null;
            $scope.device = null;
            $scope.vendor = null;
            $scope.entityId = 0;
        };

        $scope.init(true);
    }]);


tempControllers.controller('DeviceProfileController', ['$injector', '$scope', '$location', '$window', '$filter', 'tempService', 'mapService', '$timeout', 'requestContext', 'domainCfgService','configService',
    function ($injector, $scope, $location, $window, $filter, tempService, mapService, $timeout, requestContext, domainCfgService, configService) {
        $scope.deviceId = requestContext.getParam("deviceId");
        $scope.deviceDetails = "";
        $scope.vendorId = requestContext.getParam("vendorId");
        $scope.deviceConfigLoading = true;
        $scope.deviceConfig = {};
        $scope.entity = {};
        $scope.numFound = 2;
        $scope.currentTempPollCount = 0;
        $scope.currentTime = 0;
        $scope.prevDay = 0;
        $scope.configPushPullRequest = {};
        $scope.vendorMapping = {};
        $scope.deviceConfiguredStatus = true;
        $scope.entityInformation = {};
        $scope.configCommunicationChannel = [{id: 0, value: "SMS"}, {id: 1, value: "Internet"}];
        $scope.configCommChannelArray = ["SMS", "Internet"];
        $scope.loadingInvItems = true;
        $scope.loadCounter = 0;

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

        $scope.finished = function () {
            $scope.loadCounter++;
            if ($scope.loadCounter == 5) {
                $scope.getDeviceConfig();
            }
        }

        $scope.fetchData = function () {
            $scope.domainId = angular.copy($scope.currentDomain);
            $scope.getInvntryItems();
            $scope.getVendorMapping();
            $scope.getDeviceInfo();
            $scope.finished();
            $scope.currentTempReqStatus = 'temperature.request';
        };

        $scope.getVendorMapping = function () {
            tempService.getVendorMapping()
                .then(function (data) {
                    if(checkNotNullEmpty(data.data))
                        $scope.vendorMapping = angular.fromJson(data.data);
                }).catch(function err(msg) {

                })
        };

        $scope.getInvntryItems = function () {
            $scope.showLoading();
            tempService.getInvntryItemsByDomain($scope.domainId, $scope.deviceId).then(function (data) {
                if (checkNotNullEmpty(data.data) && data.data.length > 0) {
                    $scope.deviceDetails = data.data;
                    $scope.getEntityLocation();
                }
            }).catch(function error(msg) {
                console.error(msg);
            }).finally(function (){
                $scope.loadingInvItems = false;
                $scope.hideLoading();
            });
        };

        $scope.getEntityLocation = function () {
            $scope.showLoading();
            tempService.getEntityInformation($scope.deviceDetails[0].kId).then(function (data) {
                $scope.entityInformation = data.data;
                $scope.map = mapService.updateEntityMap($scope.entityInformation);
                $scope.marker.coords = mapService.updateEntityMap($scope.entityInformation).center;
            }).catch(function error(msg) {
                console.error(msg);
            }).finally(function (){
                $scope.hideLoading();
            });
        };

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
            tempService.getDeviceInfo($scope.vendorId, $scope.deviceId).then(function (data) {
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
                console.error(msg);
            });
        };

        $scope.getDeviceInfo = function () {
            $scope.showLoading();
            tempService.getDeviceInfo($scope.vendorId, $scope.deviceId).then(function (data) {
                if(checkNotNullEmpty(data.data)){
                    $scope.deviceInfo = data.data;
                }
            }).catch(function error(msg) {
                console.error(msg);
            }).finally(function(data){
                $scope.hideLoading();
            });
        };

        $scope.getDeviceConfig = function () {
            $scope.showLoading();
            tempService.getDeviceConfig($scope.vendorId, $scope.deviceId).then(function (data) {
                if(checkNotNullEmpty(data.data)){
                    var configRec = angular.fromJson(data.data);
                    if(configRec.data != undefined)
                        $scope.deviceConfig = configRec.data;
                    else
                        $scope.deviceConfig = configRec;

                    if(checkNotNullEmpty($scope.deviceConfig) && checkNotNullEmpty($scope.deviceConfig.comm) && checkNotNullEmpty($scope.deviceConfig.comm.usrPhones)){
                        $scope.usrPhones = $scope.deviceConfig.comm.usrPhones;
                        $scope.deviceConfig.comm.usrPhones = $scope.deviceConfig.comm.usrPhones.toString();
                    }
                }
            }).catch(function error(msg) {
                console.error(msg);
            }).finally(function(){
                $scope.hideLoading();
            });
        };

        $scope.fetchData();
    }]);

tempControllers.controller('DeviceSummaryController', ['$scope', 'requestContext', 'tempService', '$location',
    function($scope, requestContext, tempService, $location){
        $scope.vendorId = requestContext.getParam("vendorId");
        $scope.deviceId = requestContext.getParam("deviceId");
        $scope.recentAlerts = "";
        $scope.categoryJson = [];
        $scope.tempJson = [];
        $scope.size = 10;
        $scope.deviceAlertLoading = true;
        $scope.deviceTempLoading = true;
        $scope.wparams = [["tday","tday","",formatDate2Url]];
        $scope.tday = parseUrlDate(requestContext.getParam("tday"));
        if(checkNullEmpty($scope.tday)){
            $scope.tday = new Date();
        }

        $scope.firstInit = true;
        $scope.offsetChanged = false;
        ListingController.call(this, $scope, requestContext, $location);
        $scope.$watch("offset", function () {
            $scope.offsetChanged = true;
            $scope.getRecentAlerts();
        });

        $scope.$watch("deviceConfig", function(){
            if(checkNotNullEmpty($scope.deviceConfig) && checkNotNullEmpty($scope.deviceConfig.comm)){
                if($scope.firstInit){
                    $scope.refreshTemperature();
                    $scope.firstInit = false;
                }else{
                    $scope.getTemperature();
                }
            }
        });

        $scope.fetch = function(){
            if(checkNotNullEmpty($scope.deviceConfig) && checkNotNullEmpty($scope.deviceConfig.comm) && !$scope.firstInit && !$scope.offsetChanged){
                $scope.getTemperature();
            }
            $scope.offsetChanged = false;
        };

        $scope.init = function(){

        };

        $scope.getRecentAlerts = function () {
            $scope.recentAlerts = {};
            $scope.recentAlertsLoading = true;
            var offset = ($scope.offset / $scope.size) + 1;
            tempService.getRecentAlerts($scope.vendorId, $scope.deviceId, offset, $scope.size).then(function (data) {
                if(checkNotNullEmpty(data.data)){
                    var alertsRec = data.data;
                    alertsRec.numFound = alertsRec.nPages * $scope.size;
                    alertsRec.results = alertsRec.data;
                    $scope.setResults(alertsRec);
                    $scope.recentAlerts =alertsRec.results;
                }
                $scope.recentAlertsLoading = false;
            }).catch(function error(msg) {
                console.error(msg);
            });
        };

        $scope.updateTempGraph = function (tdate) {
            var samplingInt = $scope.deviceConfig.comm.samplingInt == undefined ? (5 * 60)
                : ($scope.deviceConfig.comm.samplingInt <= 0 ? (5 * 60) : ($scope.deviceConfig.comm.samplingInt * 60));
            var size = 500;
            $scope.deviceTempLoading = true;
            tempService.getTemperatures($scope.vendorId, $scope.deviceId, size, samplingInt, tdate).then(function (data) {
                if(checkNotNullEmpty(data.data)){
                    $scope.deviceTemp = data.data;
                    if (checkNotNullEmpty($scope.deviceTemp.data) && $scope.deviceTemp.data.length > 0){
                        angular.forEach($scope.deviceTemp.data, function (value) {
                            $scope.categoryJson.push({"label": value.label});
                            $scope.tempJson.push({"value": value.value});
                        });
                    }

                    var maxValue = $scope.deviceDetails[0].max + 5;
                    var minValue = $scope.deviceDetails[0].min > 0 ? 0 : $scope.deviceDetails[0].min - 5;
                    var deviceName = $scope.vendorId;
                    var deviceId = $scope.deviceId;
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
                            "yAxisMaxValue": maxValue,
                            "yAxisMinValue": minValue,
                            "adjustDiv":"0",
                            "numDivLines":"4",
                            "showZeroPlaneValue":"0",
                            "zeroPlaneAlpha":"0",
                            "pixelsPerPoint": "0",
                            "pixelsPerLabel": "30",
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
                                    "startvalue": $scope.deviceDetails[0].min,
                                    "color": "#00c0ef",
                                    "valueOnRight": "1"
                                }, {
                                    "startvalue": $scope.deviceDetails[0].max,
                                    "color": "#ff0000",
                                    "valueOnRight": "1"
                                }
                            ]
                        }
                    };

                    var noDataMessage = checkNullEmpty(tdate) ? "No data available" : "No data available for " + formatDate(tdate);
                    FusionCharts.ready(function () {
                        var chartReference = FusionCharts("temp-map");
                        if(checkNotNullEmpty(chartReference)){
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
            }).finally(function(){
                $scope.deviceTempLoading = false;
            });
        };

        $scope.refreshTemperature = function(){
            $scope.categoryJson = [];
            $scope.tempJson = [];
            $scope.updateTempGraph();
        };

        $scope.getTemperature = function(){
            $scope.categoryJson = [];
            $scope.tempJson = [];
            $scope.updateTempGraph($scope.tday);
        }
    }
]);

tempControllers.controller('DeviceInfoController', ['$scope', 'requestContext', 'tempService',
    function($scope, requestContext, tempService){
        $scope.vendorId = requestContext.getParam("vendorId");
        $scope.deviceId = requestContext.getParam("deviceId");

        $scope.editInfo = function () {
            $scope.deviceInfoCopy = angular.copy($scope.deviceInfo);
            $scope.infoEditable = true;
        };

        $scope.submitDeviceInfo = function () {
            $scope.showLoading();
            $scope.deviceInfoSubmission = {devices: [$scope.deviceInfo]};
            tempService.updateDeviceInfo($scope.deviceInfoSubmission, $scope.domainId).then(function () {
                $scope.infoEditable = false;
                $scope.showSuccess($scope.resourceBundle['deviceinfo.updatesuccess']);
            }).catch(function () {
                $scope.showError($scope.resourceBundle['deviceinfo.updatefail']);
                $scope.cancelDeviceInfoEdit();
            }).finally(function(){
                $scope.hideLoading();
            });
        };

        $scope.cancelDeviceInfoEdit = function () {
            $scope.deviceInfo = angular.copy($scope.deviceInfoCopy);
            $scope.infoEditable = false;
        };
    }
]);

tempControllers.controller('DeviceConfigController', ['$scope', 'requestContext', 'tempService',
    function($scope, requestContext, tempService){
        $scope.vendorId = requestContext.getParam("vendorId");
        $scope.deviceId = requestContext.getParam("deviceId");
        $scope.configEditable = false;

        $scope.editConfig = function () {
            $scope.configEditable = true;
            $scope.deviceConfigCopy = angular.copy($scope.deviceConfig);
        };

        $scope.cancelConfigEdit = function () {
            $scope.configEditable = false;
            $scope.deviceConfig = $scope.deviceConfigCopy;
        };

        $scope.submitConfig = function (action) {
            $scope.showLoading();
            var pushConfig = (action == 1);
            if (checkNotNullEmpty($scope.deviceConfig) && checkNotNullEmpty($scope.deviceConfig.comm) && checkNotNullEmpty($scope.deviceConfig.comm.usrPhones)) {
                var usrPhones = $scope.deviceConfig.comm.usrPhones.split(",");
                $scope.deviceConfig.comm.usrPhones = [];
                for(var i = 0; i < usrPhones.length ; i++){
                    if(checkNotNullEmpty(usrPhones[i].trim())){
                        $scope.deviceConfig.comm.usrPhones.push(usrPhones[i]);
                    }
                }
            } else {
                $scope.deviceConfig.comm.usrPhones = [];
            }
            $scope.usrPhones = $scope.deviceConfig.comm.usrPhones;
            $scope.configSubmission = {vId: $scope.vendorId, dId: $scope.deviceId, configuration: $scope.deviceConfig};
            tempService.updateDeviceConfig($scope.configSubmission, $scope.domainId, pushConfig).then(function () {
                $scope.configEditable = false;
                $scope.showSuccess($scope.resourceBundle['deviceconfig.success']);
                $scope.getDeviceConfig();
            }).catch(function () {
                $scope.showError($scope.resourceBundle['deviceconfig.fail']);
                $scope.cancelConfigEdit();
            }).finally(function(){
                $scope.hideLoading();
            });
        };

        $scope.pushConfiguration = function () {
            $scope.configPushPullRequest = {typ: 1, vId: $scope.vendorId, dId: $scope.deviceId};
            $scope.pushingConfig = true;
            tempService.pushPullConfig($scope.configPushPullRequest, $scope.domainId).then(function (data) {
                $scope.getInvntryItems();
                $scope.showSuccess($scope.resourceBundle['config.pushsuccess']);
            }).catch(function error(msg) {
                $scope.showError($scope.resourceBundle['config.pushfail']);
            }).finally(function(data){
                $scope.pushingConfig = false;
            });
        };

        $scope.sendConfigPullRequest = function () {
            $scope.configPushPullRequest = {typ: 0, vId: $scope.vendorId, dId: $scope.deviceId};
            $scope.sendingConfigPullRequest = true;
            tempService.pushPullConfig($scope.configPushPullRequest, $scope.domainId).then(function (data) {
                $scope.getInvntryItems();
                $scope.showSuccess($scope.resourceBundle['config.pullreqsuccess']);
            }).catch(function error(msg) {
                $scope.showError($scope.resourceBundle['config.pullfail']);
            }).finally(function(data){
                $scope.sendingConfigPullRequest = false;
            });
        };
    }
]);

tempControllers.controller('DeviceStatsController', ['$scope', 'requestContext', 'tempService', '$location',
    function($scope, requestContext, tempService, $location){
        $scope.wparams = [["day","day","",formatDate2Url]];


        ListingController.call(this, $scope, requestContext, $location);

        $scope.init = function(){
            $scope.vendorId = requestContext.getParam("vendorId");
            $scope.deviceId = requestContext.getParam("deviceId");
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
            /*var offsetInSeconds = 0, offset = $scope.domainTzOffset;
             if($scope.deviceConfig.locale && $scope.deviceConfig.locale.tz ) {
             offset = $scope.deviceConfig.locale.tz;
             }
             offsetInSeconds = offset * 60 * 60;*/

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

            tempService.getDeviceStats($scope.vendorId, $scope.deviceId, fromDate, endDate).then(function (data) {
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
            }).catch(function err(msg) {
                $scope.statsLoading = 1;
                $scope.deviceStats = {};
                console.log(msg);
            }).finally(function(){
                $scope.hideLoading();
            });
        };
        $scope.fetch();
    }
]);

tempControllers.controller('DeviceController', ['$scope', '$location', 'tempService', '$timeout', 'requestContext', 'domainCfgService', '$window', 'configService',
    function ($scope, $location, tempService, $timeout, requestContext, domainCfgService, $window, configService) {
        $scope.domainId = "";
        $scope.kioskId = requestContext.getParam("entityId");
        $scope.materialId = requestContext.getParam("materialId");
        $scope.invntryItems = {};
        $scope.deviceRegStatus = ["Pending", "Registered", "Registered", "Failed"];
        $scope.vendorMapping = {};
        $scope.itemToBeRemoved = {};
        $scope.loadingInventry = false;
        $scope.kioskInfo = {n: "N/A"};
        $scope.materialInfo = {};
        $scope.deviceConfig = {};
        $scope.deviceConfigCopy = {};
        $scope.device = {sim: {phn: ""}, pushDeviceConfig: 1, editConfig: false};
        $scope.selAll = false;
        $scope.configCommunicationChannel = [{id: 0, value: "SMS"}, {id: 1, value: "Internet"}];
        $scope.uVisited = {};
        $scope.loadCounter = 0;
        $scope.complete = false;
        //$scope.cfgURLEdited = false;

        /*
         $scope.$watch("device.sid", function () {
         if ($scope.device.svId != undefined && $scope.device.sid != "" && $scope.device.svId != "" && !$scope.cfgURLEdited)
         $scope.deviceConfig.comm.cfgUrl = $scope.deviceConfigCopy.comm.cfgUrl + $scope.device.svId + '/' + $scope.device.sid
         });

         $scope.$watch("device.svId", function () {
         if ($scope.device.sid != undefined && $scope.device.sid != "" && $scope.device.svId != "" && !$scope.cfgURLEdited)
         $scope.deviceConfig.comm.cfgUrl = $scope.deviceConfigCopy.comm.cfgUrl + $scope.device.svId + '/' + $scope.device.sid
         });
         */

        LanguageController.call(this, $scope, configService);
        TimezonesControllerWithOffset.call(this, $scope, configService);
        LocationController.call(this, $scope, configService);
        $scope.init = function ( ) {
            $scope.domainId = angular.copy($scope.currentDomain);
            //$scope.getEntityInformation();
            $scope.getMaterialInformation();
            $scope.getVendorMapping();
            $scope.getInvntryItems();
        };

        $scope.finished = function () {
            $scope.loadCounter++;
            if (!$scope.complete && $scope.loadCounter == 4) {
                $scope.getDeviceConfig();
                $scope.complete = true;
            }
        }

        /*$scope.getEntityInformation = function(){
         tempService.getEntityInformation($scope.kioskId)
         .then(function(data){
         $scope.kioskInfo = data.data;
         }).catch(function err(msg){
         //Do nothing
         });
         };*/

        $scope.getMaterialInformation = function () {
            $scope.loading = true;
            $scope.showLoading();
            tempService.getMaterialInformation($scope.materialId)
                .then(function (data) {
                    $scope.materialInfo = data.data;
                    $scope.loading = false;
                    $scope.hideLoading();
                }).catch(function err(msg) {
                    $scope.loading = false;
                    $scope.hideLoading();
                });
        };

        $scope.getDeviceConfig = function(){
            tempService.getDeviceConfig("-1", "-1").then(function (data) {
                if(checkNotNullEmpty(data.data)){
                    var configRec = angular.fromJson(data.data);
                    if(configRec.data != undefined)
                        $scope.deviceConfig = configRec.data;
                    else
                        $scope.deviceConfig = configRec;
                    $scope.device.min = $scope.deviceConfig.lowAlarm != undefined && $scope.deviceConfig.lowAlarm.temp != undefined ? $scope.deviceConfig.lowAlarm.temp : "";
                    $scope.device.max = $scope.deviceConfig.highAlarm != undefined && $scope.deviceConfig.highAlarm.temp != undefined ? $scope.deviceConfig.highAlarm.temp : "";

                    if(checkNotNullEmpty($scope.deviceConfig) && checkNotNullEmpty($scope.deviceConfig.comm) && checkNotNullEmpty($scope.deviceConfig.comm.usrPhones)){
                        $scope.deviceConfig.comm.usrPhones = $scope.deviceConfig.comm.usrPhones.toString();
                    }
                    $scope.deviceConfigCopy = angular.copy($scope.deviceConfig);
                }
            }).catch(function error(msg) {
                console.error(msg);
            });
        };

        $scope.getInvntryItems = function(){
            $scope.loading = true;
            $scope.showLoading();
            $scope.invntryItems = {};
            tempService.getMaterialDevices($scope.kioskId, $scope.materialId)
                .then(function(data){
                    $scope.loading = false;
                    $scope.hideLoading();
                    $scope.invntryItems = data.data;
                }).catch(function err(msg){
                    $scope.loading = false;
                    $scope.hideLoading();
                })
        };

        $scope.getVendorMapping = function(){
            tempService.getDomainVendorMapping()
                .then(function(data){
                    if(checkNotNullEmpty(data.data)){
                        $scope.vendorMapping = angular.fromJson(data.data);
                        if($scope.vendorMapping.length == 1){
                            $scope.device.vid = $scope.vendorMapping[0].vid;
                        }
                    }
                }).catch(function err(msg){

                });
        };

        $scope.createDevice = function() {
            $scope.loading = true;
            $scope.isFormValid = true;
            $scope.validationMsg = "";
            $scope.showLoading();
            $scope.uVisited = {};
            if (checkNotNullEmpty($scope.deviceConfig)
                && checkNotNullEmpty($scope.deviceConfig.comm)
                && checkNotNullEmpty($scope.deviceConfig.comm.usrPhones)) {
                if(!angular.isArray($scope.deviceConfig.comm.usrPhones)){
                    var usrPhones = $scope.deviceConfig.comm.usrPhones.split(",");
                    $scope.deviceConfig.comm.usrPhones = [];
                    for(var i = 0; i < usrPhones.length ; i++){
                        if(checkNotNullEmpty(usrPhones[i].trim())){
                            $scope.deviceConfig.comm.usrPhones.push(usrPhones[i]);
                        }
                    }
                }
            } else {
                $scope.deviceConfig.comm.usrPhones = [];
            }

            if (checkNotNullEmpty($scope.deviceConfig)
                && checkNotNullEmpty($scope.deviceConfig.comm)){
                if($scope.deviceConfig.comm.statsNotify && checkNullEmpty($scope.deviceConfig.comm.statsUrl)){
                    $scope.isFormValid = false;
                    $scope.uVisited.statsUrl = true;
                    $scope.validationMsg = $scope.resourceBundle['temperature.config.statsurl.required'];
                }
            }

            if($scope.isFormValid){
                $scope.updateInvntryItem = {invItem: $scope.device, config: $scope.deviceConfig};

                var pushConfig = false;
                if($scope.device.pushDeviceConfig == 2){
                    pushConfig = true;
                }
                tempService.saveInvntryItem($scope.kioskId, $scope.materialId, $scope.updateInvntryItem, pushConfig).then(function(data){
                    var resp = data.data;
                    if (resp.success) {
                        $scope.showSuccess("" + resp.errMsgs);
                    } else {
                        $scope.showError("" + resp.errMsgs, true);
                    }

                    $scope.updateInvntryItem = {};
                    $scope.device = {pushDeviceConfig: 1, editConfig: false};
                    if($scope.vendorMapping.length == 1){
                        $scope.device.vid = $scope.vendorMapping[0].vid;
                    }
                    $scope.device.min = $scope.deviceConfig.lowAlarm != undefined && $scope.deviceConfig.lowAlarm.temp != undefined ? $scope.deviceConfig.lowAlarm.temp : "";
                    $scope.device.max = $scope.deviceConfig.highAlarm != undefined && $scope.deviceConfig.highAlarm.temp != undefined ? $scope.deviceConfig.highAlarm.temp : "";
                    $scope.loading = false;
                    $scope.hideLoading();
                    $scope.getInvntryItems();
                }).catch(function(err){
                    $scope.showErrorMsg(err, true);
                    $scope.loading = false;
                    $scope.hideLoading();
                }).finally(function(){
                    $scope.deviceConfig = angular.copy($scope.deviceConfigCopy);
                    if(checkNotNullEmpty($scope.deviceConfig) && checkNotNullEmpty($scope.deviceConfig.comm) && checkNotNullEmpty($scope.deviceConfig.comm.usrPhones)){
                        $scope.deviceConfig.comm.usrPhones = $scope.deviceConfig.comm.usrPhones.toString();
                    }
                });
            }else{
                $scope.showWarning($scope.validationMsg);
                $scope.hideLoading();
            }
        };

        $scope.refreshList = function(){
            $scope.getInvntryItems();
        };

        $scope.selectAll = function(newval){
            angular.forEach($scope.invntryItems, function (value) {
                value.selected = newval;
            });
        };

        $scope.removeDevices = function(){
            var itemsToBeRemoved = [];
            angular.forEach($scope.invntryItems, function (item) {
                if(item.selected)
                    itemsToBeRemoved.push(item);
            });

            if(itemsToBeRemoved.length == 0){
                $scope.showWarning($scope.resourceBundle['item.remove']);
            }else{
                $scope.loading = true;
                $scope.showLoading();
                $scope.selAll = false;
                $scope.itemToBeRemoved = {removeItems: itemsToBeRemoved};
                tempService.saveInvntryItems($scope.kioskId, $scope.materialId, $scope.itemToBeRemoved).then(function(data){
                    var resp = data.data;
                    if (resp.success) {
                        $scope.showSuccess($scope.resourceBundle['device.save'] + " " + resp.errMsgs);
                    } else {
                        $scope.showError($scope.resourceBundle['device.fail'] + " " + resp.errMsgs, true);
                    }
                    $scope.getInvntryItems();
                    $scope.loading = false;
                    $scope.hideLoading();
                }).catch(function(err){
                    $scope.loading = false;
                    $scope.hideLoading();
                    $scope.showError(err, true);
                });
            }
        };

        $scope.cancelCreateDevice = function(){
            $scope.deviceConfig = angular.copy($scope.deviceConfigCopy);
            $scope.device = {pushDeviceConfig: 1, editConfig: false};
            $scope.uVisited = {};
            if($scope.vendorMapping.length == 1){
                $scope.device.svId = $scope.vendorMapping[0].vid;
            }
            $scope.device.min = $scope.deviceConfig.lowAlarm != undefined && $scope.deviceConfig.lowAlarm.temp != undefined ? $scope.deviceConfig.lowAlarm.temp : "";
            $scope.device.max = $scope.deviceConfig.highAlarm != undefined && $scope.deviceConfig.highAlarm.temp != undefined ? $scope.deviceConfig.highAlarm.temp : "";

            $window.location.href = "#/setup/entities/detail/" + $scope.kioskId + "/materials/" + $scope.materialId + "/devices/";
        };

        $scope.configChange = function () {
            $scope.deviceConfig = angular.copy($scope.deviceConfigCopy);
            $scope.device.editConfig = false;
            //$scope.cfgURLEdited = false;
            /*if ($scope.device.svId != undefined && $scope.device.sid != undefined && $scope.device.sid != "" && $scope.device.svId != "")
             $scope.deviceConfig.comm.cfgUrl = $scope.deviceConfigCopy.comm.cfgUrl + $scope.device.svId + '/' + $scope.device.sid*/
        }

        $scope.init();
    }]);

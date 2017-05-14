var lgDashboard = angular.module('lgDashboard', []);

lgDashboard.controller('summaryLoadController', ['$scope', '$location', '$window', 'summaryLoadService',
    function ($scope, $location, $window, summaryLoadService) {
        $scope.domainId = "";
        $scope.locationMapping = {};
        $scope.topLocation = "";
        $scope.countryCode = "";
        $scope.stateCode = "";
        $scope.tagSummary = {"nDevices":0,"nAbnormalTemps":0,"nAbnormalDevices":0};
        $scope.childTagSummary = {};
        $scope.tagAbnormalDevices = {};
        $scope.loadingTagAbnormalDevices = true;
        $scope.chartOptions = {};
        $scope.colorRange = {};
        $scope.dataJson = [];
        $scope.chartData = {};
        $scope.mapJson = {};
        $scope.selectedLocStats = {"nDevices":0,"nAbnormalTemps":0,"nAbnormalDevices":0};
        $scope.currentFilter = 1;

        $scope.mapClickListener = function (eventObj, eventArgs) {
            var dataObj = JSON.parse(JSON.stringify($scope.dataJson));
            $scope.$apply(function(){
                for (var i = 0; i < dataObj.length; i++) {
                    if (dataObj[i].id.toLowerCase() == eventArgs.id){
                        $scope.selectedLocStats.nDevices = dataObj[i].nDevices;
                        $scope.selectedLocStats.nAbnormalTemps = dataObj[i].nAbnormalTemps;
                        $scope.selectedLocStats.nAbnormalDevices = dataObj[i].nAbnormalDevices;
                        $scope.selectedLocStats.location = dataObj[i].location;
                    }
                }

                $scope.tagAbnormalDevices = {};
                $scope.loadingTagAbnormalDevices = true;
                summaryLoadService.getTagAbnormalDevices($scope.domainId + '.' + eventArgs.label)
                    .then(function (data) {
                        $scope.tagAbnormalDevices = data.data;
                        $scope.loadingTagAbnormalDevices = false;
                    }).catch(function error(msg) {
                        console.error(msg);
                    });
            });
        };

        $scope.generateValue = function (filter) {
            $scope.dataJson = JSON.parse(JSON.stringify($scope.childTagSummary));

            for (var i = 0; i < $scope.dataJson.length; i++) {
                var tempLoc = $scope.dataJson[i].tag.substr($scope.dataJson[i].tag.lastIndexOf(".") + 1);
                if(tempLoc != ""){
                    angular.forEach($scope.locationMapping, function(value, key) {
                        if (key === $scope.countryCode) {
                            angular.forEach(value.states, function(value, key) {
                                $scope.dataJson[i].location = tempLoc;
                                if(key == tempLoc || key == $scope.stateCode){
                                    if($scope.stateCode != "null"){
                                        angular.forEach(value.districts, function(value, key) {
                                            if(key == tempLoc){
                                                $scope.dataJson[i].id = value.id;
                                            }
                                        });
                                    } else{
                                        $scope.dataJson[i].id = value.id;
                                        $scope.dataJson[i].link = "temperature.jsp?tag=" + $scope.dataJson[i].tag + "&st=" + tempLoc;
                                    }
                                }
                            });
                        }
                    });

                    if (filter == 1)
                        $scope.dataJson[i].value = Math.abs($scope.dataJson[i].nAbnormalTemps / $scope.dataJson[i].nDevices * 100);
                    else if (filter == 2)
                        $scope.dataJson[i].value = Math.abs($scope.dataJson[i].nAbnormalDevices / $scope.dataJson[i].nDevices) * 100;
                    else if (filter == 3)
                        $scope.dataJson[i].value = 0;
                }
            }
        };

        $scope.filterChange = function() {
            var color = "00c0ef";
            var caption = "Temperature alarms in " + $scope.topLocation;

            if ($scope.currentFilter == 2) {
                color = "FF6C47";
                caption = "Device alarms in " + $scope.topLocation;
            } else if ($scope.currentFilter == 3) {
                caption = "No data in " + $scope.topLocation;
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
        };

        $scope.renderMap = function () {
            summaryLoadService.getChildTagSummary($scope.domainId)
                .then(function (data) {
                    $scope.childTagSummary = data.data;

                    $scope.chartOptions = {
                        "caption": "Temperature alarms in " + $scope.topLocation,
                        "subcaption": "Current",
                        "entityFillHoverColor": "cccccc",
                        "showLabels": "1",
                        "numberSuffix": "%",
                        "nullEntityColor": "#EEEEEE",
                        "nullEntityAlpha": "100",
                        "hoverOnNull": "0",
                        "exportEnabled": "1"
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
                                "code": "00c0ef"
                            }
                        ]
                    };

                    $scope.generateValue(1);

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
                        var deviceByState = FusionMaps.render($scope.mapJson);
                        deviceByState.addEventListener("entityClick", $scope.mapClickListener);
                        deviceByState.setChartData(JSON.stringify($scope.chartData), 'json');
                        deviceByState.render();
                    });

                }).catch(function error(msg) {
                    console.error(msg);
                });
        };

        $scope.init = function (domainId, topLocation, countryCode, stateCode) {
            $scope.domainId = domainId;
            $scope.topLocation = topLocation;
            $scope.countryCode = countryCode;
            $scope.stateCode = stateCode;

            summaryLoadService.getCountryMapping()
                .then(function (data) {
                    $scope.locationMapping = data.data.data;
                    $scope.renderMap();
                }).catch(function error(msg) {
                    console.error(msg);
                });

            summaryLoadService.getTagSummary($scope.domainId)
                .then(function (data) {
                    $scope.tagSummary = data.data;
                    $scope.selectedLocStats = angular.copy($scope.tagSummary);
                    $scope.selectedLocStats.location = $scope.topLocation;
                }).catch(function error(msg) {
                    console.error(msg);
                });

            summaryLoadService.getTagAbnormalDevices($scope.domainId)
                .then(function (data) {
                    $scope.tagAbnormalDevices = data.data;
                    $scope.loadingTagAbnormalDevices = false;
                }).catch(function error(msg) {
                    console.error(msg);
                });
        };

        $scope.handleAlertClick = function (type, location, locType) {
            $window.location.href = 'temperature.jsp?subview=items&filter=' + type + '&location=' + location + '&loctype=' + locType;
        }
    }]);

lgDashboard.factory('summaryLoadService', [
    '$http',
    function ($http) {
        return {
            getTagSummary: function (domainId) {
                return $http({
                    method: 'GET',
                    url: '/tempmonitoring?a=gettagsummary&tagid=' + domainId
                }).success(function (data, status, headers, config) {
                    return data;
                });
            },

            getChildTagSummary: function (domainId) {
                return $http({
                    method: 'GET',
                    url: '/tempmonitoring?a=getchildtagsummary&tagid=' + domainId
                }).success(function (data, status, headers, config) {
                    return data;
                });
            },

            getTagAbnormalDevices: function (domainId) {
                return $http({
                    method: 'GET',
                    url: '/tempmonitoring?a=gettagabnormaldevices&tagid=' + domainId
                }).success(function (data, status, headers, config) {
                    return data;
                });
            },

            getCountryMapping: function(){
                return $http({
                    method: 'GET',
                    url: '../../js/temp/location-mapping.json'
                }).success(function (data, status, headers, config) {
                    return data;
                });
            }
        }
    }
]);
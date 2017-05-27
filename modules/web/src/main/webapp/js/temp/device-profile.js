var lgTempTab = angular.module('device-profile', ['uiGmapgoogle-maps', 'mapServices', 'ui.bootstrap']);

function ListingController($scope, $location) {
    $scope.offset = 1;

    $scope.numFound = 0;

    $scope.maxFound = 0;

    $scope.resSize = 0;

    $scope.loading = true;

    $scope.size = 5;

    $scope.setResults = function(results){
        if(results!=null) {
            $scope.numFound = results.nPages;
            if(results.data != null ){
                $scope.resSize = results.data.length;
            }else{
                $scope.resSize = 0;
            }
        }else{
            $scope.numFound = 0;
            $scope.resSize = 0;
        }
    };

    $scope.fetchNext = function() {
        $scope.offset = $scope.offset + 1;
        if ($scope.numFound >= 1) {
            //This should never happen coz button is invisible.. added as double protection.
            if ($scope.offset > $scope.numFound) {
                $scope.offset = $scope.numFound; // reset to
                // last page
                if ($scope.offset < 1) {
                    $scope.offset = 1;
                }
            }
        }
    };

    $scope.fetchMore = function() {
        $scope.fetchNext();
    };

    $scope.fetchPrev = function() {
        $scope.offset = $scope.offset - 1;
        if ($scope.offset < 1) {
            $scope.offset = 1;
        }
    };

    $scope.fetchFirst = function() {
        $scope.offset = 1;
    };

    $scope.fetchLast = function() {
        $scope.offset = $scope.numFound;
    };

    $scope.clearOffset = function() {
        $scope.offset = 0;
        if ($location.$$search.o) {
            delete $location.$$search.o;
            $location.$$compose();
        }
    }
}
lgTempTab.controller('DeviceProfileController', ['$injector', '$scope', '$location', '$window', '$filter', 'DeviceProfileService', 'mapService', '$timeout',
    function ($injector, $scope, $location, $window, $filter, DeviceProfileService, mapService, $timeout) {
        $scope.deviceId = -1;
        $scope.deviceDetails = "";
        $scope.deviceInfo = "";
        $scope.deviceConfig = "";
        $scope.vendorId = "";
        $scope.recentAlerts = "";
        $scope.categoryJson = [];
        $scope.tempJson = [];

        $scope.deviceInfoLoading = true;
        $scope.deviceConfigLoading = true;
        $scope.deviceAlertLoading = true;
        $scope.deviceTempLoading = true;
        $scope.entity = {};
        $scope.infoEditable = false;
        $scope.deviceInfoCopy = {};
        $scope.deviceInfoError = "";
        $scope.deviceConfigUpdateError = "";
        $scope.alarmConfigEditable = false;
        $scope.configEditable = false;
        $scope.deviceConfigCopy = {};
        $scope.numFound = 2;
        $scope.recentAlertsLoading = true;
        $scope.currentTempPollCount = 0;
        $scope.currentTime = 0;
        $scope.prevDay = 0;
        $scope.currentTempReqStatus = "Request current temperature";
        $scope.configPushPullRequest = {};
        $scope.vendorMapping = {};
        $scope.deviceConfiguredStatus = true;
        $scope.entityInformation = {};
        $scope.showUser = true;
        $scope.statsSelectedDate = new Date();
        $scope.statsLoading = 0;
        $scope.deviceStats = {};
        $scope.domainTzOffset = 0;

        $scope.map = {center: {latitude: 0, longitude: 0}, zoom: 8};
        $scope.marker = {
            id: 0,
            coords: {
                latitude: 0,
                longitude: 0
            },
            options: { draggable: true }
        };

        $scope.monthNames = [ "Jan", "Feb", "Mar", "Apr", "May", "Jun",
            "Jul", "Aug", "Sep", "Oct", "Nov", "Dec" ];

        $scope.$watch("offset",function(){
            $scope.getRecentAlerts();
        });

        $scope.init = function (domainId, deviceId, vendorId, domainTzOffset) {
            $scope.domainId = domainId;

            if (deviceId != null)
                $scope.deviceId = deviceId;

            if (vendorId != null)
                $scope.vendorId = vendorId;

            $scope.domainTzOffset = domainTzOffset;

            $scope.statsSelectedDate = new Date();
            $scope.statsSelectedDate.setDate($scope.statsSelectedDate.getDate() - 1);

            //$scope.map = { center: { latitude: 0, longitude: 0}, zoom: 8};

            //Inherits Listing.
            $injector.invoke(ListingController, this, {
                $scope: $scope,
                $location: $location
            });

            $scope.getInvntryItems();
            $scope.getVendorMapping();
            $scope.getDeviceInfo();
            $scope.getDeviceConfig();
            $scope.getRecentAlerts();
        };

        $scope.getVendorMapping = function(){
            DeviceProfileService.getVendorMapping()
                .then(function(data){
                    $scope.vendorMapping = data.data;
                }).catch(function err(msg){

                })
        };

        $scope.getInvntryItems = function(){
            DeviceProfileService.getInvntryItems($scope.domainId, $scope.deviceId).then(function (data) {
                if (data.data.length > 0) {
                    $scope.deviceDetails = data.data;
                    $scope.getEntityLocation();
                }
            }).catch(function error(msg) {
                console.error(msg);
            });
        };

        $scope.getEntityLocation = function(){
            DeviceProfileService.getEntityLocation($scope.deviceDetails[0].kId).then(function (data) {
                $scope.entityInformation = data.data;
                $scope.map = mapService.updateEntityMap(data.data);
                $scope.marker.coords = mapService.updateEntityMap(data.data).center;
            }).catch(function error(msg) {
                console.error(msg);
            });
        };

        $scope.getDeviceInfo = function(){
            DeviceProfileService.getDeviceInfo($scope.vendorId, $scope.deviceId).then(function (data) {
                $scope.deviceInfo = data.data;
            }).catch(function error(msg) {
                console.error(msg);
            });
            $scope.deviceInfoLoading = false;
        };

        $scope.getDeviceConfig = function(){
            DeviceProfileService.getDeviceConfig($scope.vendorId, $scope.deviceId).then(function (data) {
                if( data.data.data != undefined) {
                    $scope.deviceConfig = data.data.data;
                }else {
                    $scope.deviceConfig = data.data;
                }
                if ($scope.deviceConfig.comm.usrPhones == undefined || $scope.deviceConfig.comm.usrPhones.length == 0) {
                    $scope.deviceConfig.comm.usrPhones = "None";
                }
                $scope.getStats();
                $scope.updateTempGraph();
            }).catch(function error(msg) {
                console.error(msg);
            });
        };

        $scope.getRecentAlerts = function(){
            $scope.recentAlerts = {};
            $scope.recentAlertsLoading = true;
            DeviceProfileService.getRecentAlerts($scope.vendorId, $scope.deviceId, $scope.offset, $scope.size).then(function (data) {
                $scope.setResults(data.data);
                $scope.recentAlerts = data.data.data;
                $scope.recentAlertsLoading = false;
            }).catch(function error(msg) {
                console.error(msg);
            });
        };

        //Take Epoch in seconds.
        $scope.graphDateLabel = function(inputTime){
            var dateValue = new Date(inputTime * 1000);
            dateValue = $scope.monthNames[dateValue.getMonth()] + " " + (dateValue.getDate() < 10 ? "0" + dateValue.getDate() : dateValue.getDate()) + " "
            + (dateValue.getHours() < 10 ? "0" + dateValue.getHours() : dateValue.getHours())
            + ":" + (dateValue.getMinutes() < 10 ? "0"+dateValue.getMinutes() : dateValue.getMinutes());

            return dateValue;
        };

        $scope.updateTempGraph = function(){
            $scope.currentTime = parseInt(new Date() / 1000);
            $scope.prevDay = parseInt((new Date() - 3 * 24 * 60 * 60 * 1000) / 1000);
            var samplingIntInMins = $scope.deviceConfig.comm.samplingInt == undefined ? 30
                : ($scope.deviceConfig.comm.samplingInt <= 0 ? 30 : $scope.deviceConfig.comm.samplingInt);
            var size = Math.floor(3 * 24 * 60 / samplingIntInMins) + 20;

            DeviceProfileService.getTemperatures($scope.vendorId, $scope.deviceId, $scope.prevDay, $scope.currentTime, size).then(function (data) {
                $scope.deviceTemp = data.data;
                var samplingInt = $scope.deviceConfig.comm.samplingInt == undefined ? (30 * 60)
                    : ($scope.deviceConfig.comm.samplingInt <= 0 ? (30 * 60) : ($scope.deviceConfig.comm.samplingInt * 60));
                var prevTime = $scope.prevDay;

                if($scope.deviceTemp.data != undefined)
                    $scope.deviceTemp.data.reverse();

                angular.forEach($scope.deviceTemp.data, function (value) {
                    if(prevTime != 0 && (value.time - prevTime) > (samplingInt + 30 * 60)){
                        /*var lastValue = angular.copy($scope.tempJson[$scope.tempJson.length - 1]);
                         $scope.tempJson.splice($scope.tempJson.length - 1, 1, {"value": lastValue.value, "dashed": 1});*/
                        var missingDataPoints = (value.time - prevTime) / (samplingInt);
                        for(var i = 1; i < missingDataPoints ; i++){
                            $scope.categoryJson.push({"label": $scope.graphDateLabel(prevTime + i * samplingInt)});
                            $scope.tempJson.push({});
                        }
                    }

                    $scope.categoryJson.push({"label": $scope.graphDateLabel(value.time)});
                    $scope.tempJson.push({"value": value.tmp});
                    prevTime = value.time;
                });

                var missingDataPoints = ($scope.currentTime - prevTime) / (samplingInt);
                for(var i = 1; i < missingDataPoints ; i++){
                    $scope.categoryJson.push({"label": $scope.graphDateLabel(prevTime + i * samplingInt)});
                    $scope.tempJson.push({});
                }

                var dataJson = {
                    "chart": {
                        "subCaption": "Past 3 days",
                        "xAxisName": "Time",
                        "yAxisName": "Temperature",
                        "numberSuffix": " C",
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
                        "slantLabels": 1,
                        "numVisiblePlot": 12,
                        "scrollToEnd": 1
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
                                "startvalue": $scope.deviceDetails[0].tmin,
                                "color": "#00c0ef",
                                "valueOnRight": "1"
                            }, {
                                "startvalue": $scope.deviceDetails[0].tmax,
                                "color": "#ff0000",
                                "valueOnRight": "1"
                            }
                        ]
                    }
                };

                var chartJson = {
                    type: 'ScrollLine2D',
                    id: 'temp-map',
                    renderAt: 'chart-container',
                    width: '100%',
                    height: '280',
                    dataFormat: 'json',
                    showBorder: '0'
                };

                FusionCharts.ready(function () {
                    var deviceByState = FusionMaps.render(chartJson);
                    deviceByState.setChartData(JSON.stringify(dataJson), 'json');
                    deviceByState.render();
                });

            }).catch(function error(msg) {
                console.error(msg);
            });
        };

        $scope.editInfo = function(){
            $scope.deviceInfoCopy = angular.copy($scope.deviceInfo);
            $scope.infoEditable = true;
        };

        $scope.submitDeviceInfo = function() {
            $scope.deviceInfoSubmission = { devices: [$scope.deviceInfo]};
            DeviceProfileService.updateDeviceInfo($scope.deviceInfoSubmission, $scope.domainId).then(function(){
                $scope.infoEditable = false;
            }).catch(function(){
                $scope.deviceInfoError = "Unable to update device info.";
                $scope.cancelDeviceInfoEdit();
            });
        };

        $scope.cancelDeviceInfoEdit = function() {
            $scope.deviceInfo = angular.copy($scope.deviceInfoCopy);
            $scope.infoEditable = false;
        };

        $scope.editConfig = function(){
            $scope.configEditable = true;
            $scope.deviceConfigCopy = angular.copy($scope.deviceConfig);
        };

        $scope.cancelConfigEdit = function() {
            $scope.configEditable = false;
            $scope.deviceConfig = $scope.deviceConfigCopy;
        };

        $scope.submitConfig = function (action) {
            var pushConfig = (action == 1);
            if ($scope.deviceConfig.comm.usrPhones != undefined) {
                if (!angular.isArray($scope.deviceConfig.comm.usrPhones)) {
                    $scope.deviceConfig.comm.usrPhones = [$scope.deviceConfig.comm.usrPhones];
                }
            } else {
                $scope.deviceConfig.comm.usrPhones = [];
            }

            $scope.configSubmission = {vId: $scope.vendorId, dId: $scope.deviceId, configuration: $scope.deviceConfig};
            DeviceProfileService.updateDeviceConfig($scope.configSubmission, $scope.domainId, pushConfig).then(function () {
                $scope.configEditable = false;
                $scope.getInvntryItems();
            }).catch(function(){
                $scope.deviceConfigUpdateError = "Unable to update device config.";
                $scope.cancelConfigEdit();
            });
        };

        $scope.getCurrentTemp = function(){
            $scope.deviceDetails[0].tmpStatus = 0;
            $scope.currentTempReqStatus = "Requesting current temperature";
            DeviceProfileService.getCurrentTemp($scope.vendorId, $scope.deviceId).then(function(){
                $scope.currentTempReqStatus = "Request sent. Waiting for response";
                $timeout(function() {
                    $scope.currentTempReqStatus = "Fetching temperature";
                    $scope.currentTempPollCount = 0;
                    $scope.poll();
                }, 30000);
            }).catch(function(){
                $scope.currentTempReqStatus = "Request failed";
                $scope.deviceDetails[0].tmpStatus = 1;
                console.log("Get current temp failed.");
            });
        };

        $scope.stopPoll = function(){
            $scope.currentTempPollCount = 10;
            $scope.deviceDetails[0].tmpStatus = 1;
            $scope.currentTempReqStatus = "Request current temperature";
        };

        $scope.poll = function(){
            $scope.currentTempReqStatus = "Fetching temperature";
            DeviceProfileService.getInvntryItems($scope.domainId, $scope.deviceId).then(function (data) {
                if (data.data.length > 0 && data.data[0].tut != $scope.deviceDetails[0].tut) {
                    $scope.deviceDetails = data.data;
                    $scope.updateTempGraph();
                    $scope.currentTempReqStatus = "Temperature received";
                } else {
                    $timeout(function() {
                        if($scope.currentTempPollCount < 10)
                            $scope.poll();
                        else
                            $scope.deviceDetails[0].tmpStatus = 1;

                        $scope.currentTempPollCount++;
                    }, 30000);
                }
            }).catch(function error(msg) {
                console.error(msg);
            });
        };

        $scope.pushConfiguration = function(){
            $scope.configPushPullRequest = {typ: 1, vId: $scope.vendorId, dId: $scope.deviceId};
            DeviceProfileService.pushPullConfig($scope.configPushPullRequest, $scope.domainId).then(function (data) {
                $scope.getInvntryItems();
            }).catch(function error(msg) {

            });
        };

        $scope.sendConfigPullRequest = function(){
            $scope.configPushPullRequest = {typ: 0, vId: $scope.vendorId, dId: $scope.deviceId};
            DeviceProfileService.pushPullConfig($scope.configPushPullRequest, $scope.domainId).then(function (data) {
                $scope.getInvntryItems();
            }).catch(function error(msg) {

            });
        }

        $scope.getStats = function(){
            var offsetInSeconds = 0, offset = $scope.domainTzOffset;
            if($scope.deviceConfig.locale && $scope.deviceConfig.locale.tz ) {
                offset = $scope.deviceConfig.locale.tz;
            }
            offsetInSeconds = offset * 60 * 60;

            $scope.statsLoading = 0;
            var generatedDate = new Date($scope.statsSelectedDate);
            generatedDate.setHours(0);
            generatedDate.setMinutes(0);
            generatedDate.setSeconds(0);
            generatedDate.setMilliseconds(0);
            var fromDate = generatedDate.getTime()/1000 - generatedDate.getTimezoneOffset() * 60;
            fromDate = fromDate - offsetInSeconds;
            generatedDate.setDate(generatedDate.getDate() + 1);
            var endDate = generatedDate.getTime()/1000 - generatedDate.getTimezoneOffset() * 60;
            endDate = endDate - offsetInSeconds;

            DeviceProfileService.getDeviceStats($scope.vendorId, $scope.deviceId, fromDate, endDate).then(function(data){
                if(data.data.data.length > 0){
                    $scope.statsLoading = 2;
                    $scope.deviceStats = data.data.data[0].stats;
                }else{
                    $scope.statsLoading = 1;
                    $scope.deviceStats = {};
                }
            }).catch(function err(msg){
                $scope.statsLoading = 1;
                $scope.deviceStats = {};
                console.log(msg);
            })
        }
    }]);

lgTempTab.factory('DeviceProfileService', [
    '$http',
    function ($http) {
        return {

            getInvntryItems: function (domainId, deviceId) {
                return $http({
                    method: 'GET',
                    url: '/tempmonitoring?a=getassets&domainid=' + domainId + '&deviceid=' + deviceId
                }).success(function (data, status, headers, config) {
                    return data;
                });
            },

            getDeviceInfo: function (vendorId, deviceId) {
                return $http({
                    method: 'GET',
                    url: '/tempmonitoring?a=getdeviceinfo&vendorid=' + vendorId + '&deviceid=' + deviceId
                }).success(function (data, status, headers, config) {
                    return data;
                });
            },

            getDeviceConfig: function (vendorId, deviceId) {
                return $http({
                    method: 'GET',
                    url: '/tempmonitoring?a=getdeviceconfig&vendorid=' + vendorId + '&deviceid=' + deviceId
                }).success(function (data, status, headers, config) {
                    return data;
                });
            },

            getRecentAlerts: function (vendorId, deviceId, page, size) {
                return $http({
                    method: 'GET',
                    url: '/tempmonitoring?a=getrecentalerts&vendorid=' + vendorId + '&deviceid=' + deviceId + '&page=' + page + '&size=' + size
                }).success(function (data, status, headers, config) {
                    return data.data;
                });
            },

            getTemperatures: function (vendorId, deviceId, start, end, size) {
                return $http({
                    method: 'GET',
                    url: '/tempmonitoring?a=gettemps&vendorid=' + vendorId + '&deviceid=' + deviceId + '&start=' + start + '&end=' + end + '&size=' + size
                }).success(function (data, status, headers, config) {
                    return data;
                });
            },

            getEntityLocation: function( kioskId ){
                return $http({
                    method: 'GET',
                    url: '/s/data?action=getkioskbyid&type=json&kioskId=' + kioskId
                }).success(function (data, status, headers, config) {
                    return data;
                });
            },

            updateDeviceInfo: function(deviceInfo, domainId){
                return $http({
                    header: {
                        "Content-Type": "Application/json"
                    },
                    method: 'POST',
                    url: '/tempmonitoring?a=updatedeviceinfo&domainId=' + domainId,
                    data: JSON.stringify(deviceInfo)
                }).success(function (data, status, headers, config) {
                    return data;
                });
            },

            updateDeviceConfig: function (deviceConfig, domainId, pushConfig) {
                return $http({
                    header: {
                        "Content-Type": "Application/json"
                    },
                    method: 'POST',
                    url: '/tempmonitoring?a=updatedeviceconfig&domainId=' + domainId + '&pushConfig=' + pushConfig,
                    data: JSON.stringify(deviceConfig)
                }).success(function (data, status, headers, config) {
                    return data;
                });
            },

            getCurrentTemp: function (vendorId, deviceId) {
                return $http({
                    method: 'GET',
                    url: '/tempmonitoring?a=getcurrenttemp&vendorid=' + vendorId + '&deviceid=' + deviceId
                }).success(function (data, status, headers, config) {
                    return data;
                });
            },

            getVendorMapping: function () {
                return $http({
                    method: 'GET',
                    url: '/tempmonitoring?a=getvendormapping'
                }).success(function (data, status, headers, config) {
                    return data;
                });
            },

            pushPullConfig: function (requestData, domainId) {
                return $http({
                    method: 'POST',
                    url: '/tempmonitoring?a=pushconfig&domainid=' + domainId,
                    data: JSON.stringify(requestData)
                }).success(function (data, status, headers, config) {
                    return data;
                });
            },

            getDeviceStats: function (vendorId, deviceId, from, to) {
                return $http({
                    method: 'GET',
                    url: '/tempmonitoring?a=getdevicestats&vendorid=' + vendorId + '&deviceid=' + deviceId + '&from=' + from + '&to=' + to
                }).success(function (data, status, headers, config) {
                    return data;
                });
            }
        }
    }
]);

lgTempTab.directive('datePicker',function (){
    return {
        restrict: 'AE',
        replace: true,
        template: '<div class="datepicker" style="width:100%">' +
        '<input type="text" ng-readonly="true" is-open="opened" class="form-control datepickerreadonly input-sm" placeholder="{{placeHolder}}" ng-focus="open($event)" datepicker-options="{minDate:minDate, maxDate:maxDate}" uib-datepicker-popup="dd/MM/yy" ng-model="dateModel" close-text="Close"/>' +
        '</div>',
        scope: {
            dateModel: '=dateModel',
            placeHolder: '@',
            minDate: '=',
            maxDate: '='
        },
        controller: ['$scope', function($scope) {

            $scope.opened = false;

            $scope.open = function($event) {
                $event.preventDefault();
                $event.stopPropagation();
                $scope.opened = true;
            };

        }],
        link: function(scope, iElement, iAttrs, ctrl) {

        }
    }
});

lgTempTab.directive('ngConfirmClick', [ function() {
    return {
        link : function(scope, element, attr) {
            var msg = attr.ngConfirmClick || "Are you sure?";
            var clickAction = attr.confirmedClick;
            var cancelAction = attr.cancelClick;
            element.bind('click', function(event) {
                if (window.confirm(msg)) {
                    scope.$eval(clickAction)
                } else {
                    scope.$eval(cancelAction)
                }
            });
        }
    };
} ]);

lgTempTab.directive('tooltip', function(){
    return {
        restrict: 'A',
        link: function(scope, element, attrs){
            $(element).hover(function(){
                // on mouseenter
                $(element).tooltip('show');
            }, function(){
                // on mouseleave
                $(element).tooltip('hide');
            });
        }
    };
});

lgTempTab.filter('timeago', function() {
    return function(input, p_allowFuture, p_agoreq) {
        var substitute = function (stringOrFunction, number, strings) {
                var string = $.isFunction(stringOrFunction) ? stringOrFunction(number, dateDifference) : stringOrFunction;
                var value = (strings.numbers && strings.numbers[number]) || number;
                return string.replace(/%d/i, value);
            },
            nowTime = (new Date()).getTime(),
            date = (new Date(input)).getTime(),
            allowFuture = p_allowFuture || false,
            isSuffixReq = p_agoreq || false,
            strings= {
                prefixAgo: null,
                prefixFromNow: null,
                suffixAgo: "ago",
                suffixFromNow: "from now",
                seconds: "less than a minute",
                minute: "about a minute",
                minutes: "%d minutes",
                hour: "about an hour",
                hours: "about %d hours",
                day: "a day",
                days: "%d days",
                month: "about a month",
                months: "%d months",
                year: "about a year",
                years: "%d years"
            },
            dateDifference = nowTime - date,
            words,
            seconds = Math.abs(dateDifference) / 1000,
            minutes = seconds / 60,
            hours = minutes / 60,
            days = hours / 24,
            years = days / 365,
            separator = strings.wordSeparator === undefined ?  " " : strings.wordSeparator;

        // var strings = this.settings.strings;
        if (isSuffixReq) {
            prefix = strings.prefixAgo;
            suffix = strings.suffixAgo;

            if (allowFuture) {
                if (dateDifference < 0) {
                    prefix = strings.prefixFromNow;
                    suffix = strings.suffixFromNow;
                }
            }
        }else{
            prefix = "";
            suffix = "";
        }

        words = seconds < 45 && substitute(strings.seconds, Math.round(seconds), strings) ||
        seconds < 90 && substitute(strings.minute, 1, strings) ||
        minutes < 45 && substitute(strings.minutes, Math.round(minutes), strings) ||
        minutes < 90 && substitute(strings.hour, 1, strings) ||
        hours < 24 && substitute(strings.hours, Math.round(hours), strings) ||
        hours < 42 && substitute(strings.day, 1, strings) ||
        days < 30 && substitute(strings.days, Math.round(days), strings) ||
        days < 45 && substitute(strings.month, 1, strings) ||
        days < 365 && substitute(strings.months, Math.round(days / 30), strings) ||
        years < 1.5 && substitute(strings.year, 1, strings) ||
        substitute(strings.years, Math.round(years), strings);

        return $.trim([prefix, words, suffix].join(separator));
    }
});
var lgDeviceApp = angular.module('device-app', ['ui.bootstrap']);

lgDeviceApp.controller('DeviceController', ['$scope', '$location', 'DeviceService', '$timeout',
    function ($scope, $location, DeviceService, $timeout) {
        $scope.domainId = "";
        $scope.kioskId = "";
        $scope.materialId = "";
        $scope.userId = "";
        $scope.invntryItems = {};
        $scope.deviceRegStatus = ["Pending", "Registered", "Registered", "Failed"];
        $scope.vendorMapping = {};
        $scope.device = {sim: {phn: ""}};
        $scope.deviceRegMsg = "";
        $scope.deviceDeleteMsg = "";
        $scope.itemToBeRemoved = {};
        $scope.loadingInventry = false;
        $scope.kioskInfo = {n: "N/A"};
        $scope.materialInfo = {};
        $scope.alertType = "";
        $scope.deviceConfig = {};
        $scope.deviceConfigCopy = {};
        $scope.pushDeviceConfig = 1;
        $scope.editConfig = false;
        $scope.disableSubmit = false;
        $scope.showDeviceForm = true;
        $scope.showTable = true;
        $scope.selAll = false;
        $scope.configCommunicationChannel = [{id: 0, value: "SMS"}, {id: 1, value: "Internet"}];
        $scope.cfgURLEdited = false;

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

        $scope.init = function (userId, domainId, kioskId, materialId) {
            $scope.userId = userId;
            $scope.domainId = domainId;
            $scope.kioskId = kioskId;
            $scope.materialId = materialId;

            $scope.getDeviceConfig();
            $scope.getEntityInformation();
            $scope.getMaterialInformation();
            $scope.getVendorMapping();
            $scope.getInvntryItems();
        };

        $scope.getEntityInformation = function(){
            DeviceService.getEntityInformation($scope.kioskId)
                .then(function(data){
                    $scope.kioskInfo = data.data;
                }).catch(function err(msg){
                    //Do nothing
                });
        };

        $scope.getMaterialInformation = function () {
            DeviceService.getMaterialInformation($scope.materialId)
                .then(function (data) {
                    $scope.materialInfo = data.data;
                }).catch(function err(msg) {
                    //Do nothing
                });
        };

        $scope.getDeviceConfig = function(){
            DeviceService.getDeviceConfig("logistimo", "createDevice").then(function (data) {
                if( data.data.data != undefined) {
                    $scope.deviceConfig = data.data.data;
                }else {
                    $scope.deviceConfig = data.data;
                }
                $scope.device.tmin = $scope.deviceConfig.lowAlarm != undefined && $scope.deviceConfig.lowAlarm.temp != undefined ? $scope.deviceConfig.lowAlarm.temp : "";
                $scope.device.tmax = $scope.deviceConfig.highAlarm != undefined && $scope.deviceConfig.highAlarm.temp != undefined ? $scope.deviceConfig.highAlarm.temp : "";
                $scope.deviceConfigCopy = angular.copy($scope.deviceConfig);
            }).catch(function error(msg) {
                console.error(msg);
            });
        };

        $scope.getInvntryItems = function(){
            $scope.loadingInventry = true;
            $scope.invntryItems = {};
            DeviceService.getInvntryItems($scope.userId, $scope.kioskId, $scope.materialId)
                .then(function(data){
                    $scope.loadingInventry = false;
                    $scope.invntryItems = data.data.invitems;
                }).catch(function err(msg){

                })
        };

        $scope.getVendorMapping = function(){
            DeviceService.getVendorMapping()
                .then(function(data){
                    $scope.vendorMapping = data.data;
                    if($scope.vendorMapping.length == 1){
                        $scope.device.svId = $scope.vendorMapping[0].vid;
                    }
                }).catch(function err(msg){

                })
        };

        $scope.createDevice = function() {
            $scope.showDeviceForm = false;
            $scope.deviceRegMsg = "";
            document.body.scrollTop = document.documentElement.scrollTop = 0;
            $scope.disableSubmit = true;
            if($scope.pushDeviceConfig == 2) {
                if (!angular.isArray($scope.deviceConfig.comm.usrPhones)) {
                    $scope.deviceConfig.comm.usrPhones = [$scope.deviceConfig.comm.usrPhones];
                }
                $scope.configSubmission = {
                    vId: $scope.device.svId,
                    dId: $scope.device.sid,
                    configuration: $scope.deviceConfig
                };
                $scope.updateInvntryItem = {add: [$scope.device], config: $scope.configSubmission};
            }else{
                $scope.updateInvntryItem = {add: [$scope.device]};
            }

            DeviceService.updateInvntryItems($scope.userId, $scope.kioskId, $scope.materialId, $scope.updateInvntryItem).then(function(data){
                $scope.updateInvntryItem = {};
                $scope.disableSubmit = false;
                $scope.pushDeviceConfig = 1;
                $scope.editConfig = false;
                $scope.device = {};
                if($scope.vendorMapping.length == 1){
                    $scope.device.svId = $scope.vendorMapping[0].vid;
                }
                $scope.device.tmin = $scope.materialInfo.tmin;
                $scope.device.tmax = $scope.materialInfo.tmax;
                $scope.showDeviceForm = true;
                $scope.addDeviceForm.$setPristine();
                $scope.deviceRegMsg = data.data.ms;
                $scope.getAlertType($scope.deviceRegMsg);
                $scope.getInvntryItems();
            }).catch(function(err){
                $scope.showDeviceForm = true;
                $scope.disableSubmit = false;
                $scope.deviceRegMsg = err.data.ms;
                $scope.getAlertType($scope.deviceRegMsg);
            });
            $scope.deviceDeleteMsg = "";
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
                alert("Please selected items to be removed");
            }else{
                $scope.selAll = false;
                $scope.showTable = false;
                $scope.itemToBeRemoved = {remove: itemsToBeRemoved};
                DeviceService.updateInvntryItems($scope.userId, $scope.kioskId, $scope.materialId, $scope.itemToBeRemoved).then(function(data){
                    $scope.showTable = true;
                    $scope.deviceDeleteMsg = data.data.ms;
                    $scope.getAlertType($scope.deviceDeleteMsg);
                    $scope.getInvntryItems();
                }).catch(function(err){
                    $scope.deviceDeleteMsg = err.data.ms;
                    $scope.showTable = true;
                    $scope.getAlertType($scope.deviceDeleteMsg);
                });
                $scope.deviceRegMsg = "";
            }
        };

        $scope.getAlertType = function(value){
            $scope.alertType = value.indexOf('successfully') > -1 ? "alert-success" : "alert-danger";
        };

        $scope.cancelCreateDevice = function(){
            $scope.deviceConfig = angular.copy($scope.deviceConfigCopy);
            $scope.pushDeviceConfig = 1;
            $scope.editConfig = false;
            $scope.device = {};
            if($scope.vendorMapping.length == 1){
                $scope.device.svId = $scope.vendorMapping[0].vid;
            }
            $scope.device.tmin = $scope.materialInfo.tmin;
            $scope.device.tmax = $scope.materialInfo.tmax;
            $scope.addDeviceForm.$setPristine();
            document.body.scrollTop = document.documentElement.scrollTop = 0;
        };

        $scope.configChange = function () {
            $scope.deviceConfig = angular.copy($scope.deviceConfigCopy);
            $scope.editConfig = false;
            $scope.cfgURLEdited = false;
            /*if ($scope.device.svId != undefined && $scope.device.sid != undefined && $scope.device.sid != "" && $scope.device.svId != "")
             $scope.deviceConfig.comm.cfgUrl = $scope.deviceConfigCopy.comm.cfgUrl + $scope.device.svId + '/' + $scope.device.sid*/
        }
    }]);

lgDeviceApp.factory('DeviceService', [
    '$http',
    function ($http) {
        return {
            getInvntryItems: function (userId, kioskId, materialId) {
                return $http({
                    method: 'GET',
                    url: '/i?a=gsni&kid=' + kioskId + '&mid=' + materialId + '&uid=' + userId
                }).success(function (data, status, headers, config) {
                    return data;
                });
            },

            updateInvntryItems: function (userId, kioskId, materialId, deviceInfo) {
                return $http({
                    method: 'POST',
                    url: '/i?a=arsni&kid=' + kioskId + '&mid=' + materialId + '&uid=' + userId,
                    data: JSON.stringify(deviceInfo)
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

            createDevice: function(deviceInfo, domainId, kioskId, materialId){
                return $http({
                    header: {
                        "Content-Type": "Application/json"
                    },
                    method: 'POST',
                    url: '/tempmonitoring?a=register&domainid=' + domainId + '&kioskid=' + kioskId + '&materialid=' + materialId + '&devicestoaddjson=' + JSON.stringify(deviceInfo)
                }).success(function (data, status, headers, config) {
                    return data;
                });
            },

            updateDeviceConfig: function(deviceConfig, domainId){
                return $http({
                    header: {
                        "Content-Type": "Application/json"
                    },
                    method: 'POST',
                    url: '/tempmonitoring?a=updatedeviceconfig&domainId=' + domainId,
                    data: JSON.stringify(deviceConfig)
                }).success(function (data, status, headers, config) {
                    return data;
                });
            },

            getEntityInformation: function( kioskId ){
                return $http({
                    method: 'GET',
                    url: '/s/data?action=getkioskbyid&type=json&kioskId=' + kioskId
                }).success(function (data, status, headers, config) {
                    return data;
                });
            },

            getMaterialInformation: function (materialId) {
                return $http({
                    method: 'GET',
                    url: '/s/data?action=getmaterialbyid&materialId=' + materialId
                }).success(function (data, status, headers, config) {
                    return data;
                });
            }
        }
    }
]);

lgDeviceApp.directive('ngConfirmClick', [ function() {
    return {
        link : function(scope, element, attr) {
            var msg = attr.ngConfirmClick || "Are you sure?";
            var clickAction = attr.confirmedClick;
            element.bind('click', function(event) {
                if (window.confirm(msg)) {
                    scope.$eval(clickAction)
                }
            });
        }
    };
}]);

lgDeviceApp.directive('ngFocusLogi', [function () {
    var FOCUS_CLASS = "ng-focused";
    return {
        restrict: 'A',
        require: 'ngModel',
        link: function (scope, element, attrs, ctrl) {
            ctrl.$focused = false;
            element.bind('focus', function (evt) {
                element.addClass(FOCUS_CLASS);
                scope.$apply(function () {
                    ctrl.$focused = true;
                });
            }).bind('blur', function (evt) {
                element.removeClass(FOCUS_CLASS);
                scope.$apply(function () {
                    ctrl.$focused = false;
                });
            });
        }
    }
}]);
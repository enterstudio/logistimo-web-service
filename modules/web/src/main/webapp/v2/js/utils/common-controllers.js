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

var cmnControllers = angular.module('cmnControllers', []), tag;
cmnControllers.controller('SimpleMenuController', ['$scope', 'requestContext','$location',
    function ($scope, requestContext, $location) {
        $scope.init = function (viewContext) {
            $scope.viewContext = viewContext;
            var renderContext = requestContext.getRenderContext($scope.viewContext);
            $scope.entityId = requestContext.getParam("entityId");
            $scope.materialId = requestContext.getParam("materialId");
            $scope.userId = requestContext.getParam("userId");
            $scope.dbid = requestContext.getParam("dbid");
            $scope.rptid = requestContext.getParam("rptid");
            $scope.subview = renderContext.getNextSection();
            $scope.$on("requestContextChanged", function () {
                if (!renderContext.isChangeRelevant()) {
                    return;
                }
                $scope.subview = renderContext.getNextSection();
            });
        };
    }
]);
 /**
 * Common functionality to choose material
 */
function MaterialFilterController($scope, matService) {
    $scope.materials = {};
    $scope.mid = null;
    $scope.material;
    $scope.setMaterialId = function (material) {
        if (material != null) {
            $scope.mid = material.mId;
        } else {
            $scope.mid = "";
        }
    };
    $scope.checkMaterial = function () {
        if ($scope.material == "") {
            $scope.setMaterialId(null);
        }else if(checkNotNullEmpty($scope.material)) {
            $scope.setMaterialId($scope.material.mId);
        }
    };
    matService.getDomainMaterials(null,null,0,9999).then(function (data) {
        $scope.materials = data.data;
        $scope.updateMaterial();
    }).catch(function error(msg) {
        $scope.showErrorMsg(msg);
    });
    $scope.updateMaterial = function () {
        if ($scope.materials.results != null) {
            var material = _.findWithProperty($scope.materials.results, "mId", $scope.mid);
            if (material != null) {
                $scope.material = material;
            }
        }
    }
}

/**
 * Common functionality to choose device
 */
function DeviceFilterController($scope, tempServices) {
    $scope.devices = {};
    $scope.device;
    $scope.setDeviceId = function (device) {
        if (device != null) {
            $scope.deviceId = device.id;
        } else {
            $scope.deviceId = "";
        }
    };
    $scope.checkDevice = function () {
        if ($scope.device == "") {
            $scope.setDeviceId(null);
        }
    };
    tempServices.getDevices($scope.entityId).then(function (data) {
        $scope.devices = data.data;
        $scope.updateDevices();
    }).catch(function error(msg) {
        $scope.showErrorMsg(msg);
    });
    $scope.updateDevices = function () {
        if ($scope.devices != null) {
            var device = _.findWithProperty($scope.devices, "id", $scope.deviceId);
            if (device != null) {
                $scope.device = device;
            }
        }
    }
}

/**
 * Common functionality to choose device
 */
function VendorFilterController($scope, tempServices) {
    $scope.vendors = {};
    $scope.vendor;
    $scope.setVendorId = function (vendor) {
        if (vendor != null) {
            $scope.vendorId = vendor.vid;
        } else {
            $scope.vendorId = "";
        }
    };
    $scope.checkVendor = function () {
        if ($scope.vendor == "") {
            $scope.setVendorId(null);
        }
    };
    tempServices.getDomainVendorMapping().then(function (data) {
        if(data.data){
            $scope.vendors = angular.fromJson(data.data);
            $scope.updateVendor();
        }
    }).catch(function error(msg) {
        $scope.showErrorMsg(msg);
    });
    $scope.updateVendor = function () {
        if ($scope.vendors != null) {
            var vendor = _.findWithProperty($scope.vendors, "vid", $scope.vendorId);
            if (vendor != null) {
                $scope.vendor = vendor;
            }
        }
    }
}

/**
 * Common functionality to select entity
 */
function EntityFilterController($scope, entityService) {
    $scope.fentities = {};
    $scope.entity;
    $scope.showEntityFilter;
    entityService.getAll().then(function (data) {
        $scope.fentities = data.data;
        $scope.updateEntity();
    }).catch(function error(msg) {
        $scope.showErrorMsg(msg);
    });
    $scope.setEntityId = function (entity) {
        if (entity != null) {
            $scope.entityId = entity.id;
        } else {
            $scope.entityId = "";
        }
    };
    $scope.setShow = function (entity) {
        entity.show = !entity.show;
    };
    $scope.checkEntity = function () {
        if ($scope.entity == "") {
            $scope.setEntityId(null);
        }
    };
    $scope.updateEntity = function () {
        if ($scope.fentities.results != null) {
            var entity = _.findWithProperty($scope.fentities.results, "id", $scope.entityId);
            if (entity != null) {
                $scope.entity = entity;
            } else {
                $scope.entity = null;
            }
        }
    }
}

/**
 * Listing Controller is a base helper Controller for all listable/tabular views in Logistimo
 *  eg: Materials, Inventory, Transactions, Orders
 *  It provides pagination functions, sort, automatic url update on fitlers, and sort by functions
 *  It assumes the scope includes certain parameters
 *  offset - current page offset
 *  numFound
 *  size - page size
 *  init() - to reset  params
 *  fetch() - function to call to fetch on pagination event
 *  wparams - two way .. model updates to url.. url change reflects to model.. [optional] array of pairs [ param-used-in-url, internal-param-name-to-watch ] eg: [["tag","tag"],["search","search.mnm"]];
 *  reqparams -  monitors url change of these parameters.
 *
 * @param $scope
 * @param requestContext
 * @param $location
 */
function ListingController($scope, requestContext, $location, contextChangeCallback) {
    $scope.offset = 0;
    $scope.numFound = 0;
    //$scope.maxFound = 0;
    $scope.resSize = 0;
    $scope.loading = true;
    $scope.size = 50;
    $scope.sortBy = 'sno';
    $scope.sortAsc = false;
    $scope.filtered = [];
    if (checkNotNullEmpty(requestContext)) {
        $scope.listinit = function () {
            $scope.offset = requestContext.getParam("o") || 0;
            $scope.size = requestContext.getParam("s") || 50;

            if(requestContext.getAction() == 'assets.detail.summary')
                $scope.size = 10;
        };
        $scope.listinit();
    }
    $scope.setFiltered = function(data) {
        $scope.filtered = data;
    };
    $scope.setResults = function (results) {
        if (results != null) {
            $scope.numFound = results.numFound;
            if (results.results != null) {
                $scope.resSize = results.results.length;
            } else {
                $scope.resSize = 0;
            }
            /*if ($scope.numFound == -1 && $scope.resSize < $scope.size) {
                $scope.maxFound = parseInt($scope.offset) + parseInt($scope.resSize);
            }
            if ($scope.numFound == -1 && $scope.maxFound > 0) {
                $scope.numFound = $scope.maxFound;
                if ($scope.resSize == 0) {
                    $scope.fetchLast();
                }
            }*/
        } else {
            $scope.numFound = 0;
            $scope.resSize = 0;
        }
    };
    $scope.fetchNext = function () {
        $scope.offset = parseInt($scope.offset) + parseInt($scope.size);
        if ($scope.numFound >= 0) {
            if ($scope.offset >= $scope.numFound) {
                $scope.offset = $scope.numFound - $scope.size; // reset to
                if ($scope.offset < 0) {
                    $scope.offset = 0;
                }
            }
        }
    };
    $scope.fetchMore = function () {
        $scope.fetchNext();
    };
    $scope.fetchPrev = function () {
        $scope.offset = $scope.offset - $scope.size;
        if ($scope.offset < 0) {
            $scope.offset = 0;
        }
    };
    $scope.fetchFirst = function () {
        $scope.offset = 0;

    };
    $scope.fetchLast = function () {
        $scope.offset = Math.floor($scope.numFound / $scope.size)
        * $scope.size;
        if ($scope.offset == $scope.numFound) {
            $scope.offset = $scope.numFound - $scope.size;
        }
    };
    $scope.clearOffset = function () {
        $scope.offset = 0;
        if ($location.$$search.o) {
            delete $location.$$search.o;
            $location.$$compose();
        }
    };
    for (index in $scope.wparams) {
        var param = $scope.wparams[index];
        if (param[0] != "o" && param[0] != "s") {
            $scope.$watch(param[1], watchfn(param[0], param[2], $location, $scope, null, param[3]));
        }
    }
    if(!$scope.noWatch){
        $scope.$watch("offset", watchfn("o", "", $location, $scope, null, null));
        $scope.$watch("size", watchfn("s", "", $location, $scope, null, null));
    }
    /**
     * Utility function abstracted to this class to remove duplication.
     * otherwise not a listing controller function
     */
    $scope.setTag = function (tagName) {
        $scope.tag = tagName;
    };
    $scope.setMTag = function (tagName) {
        $scope.mtag = tagName;
    };
    $scope.setETag = function (tagName) {
        $scope.etag = tagName;
    };
    $scope.setOTag = function (tagName) {
        $scope.otag = tagName;
    };
    /**
     * Utility function abstracted to this class.
     * otherwise not a listing controller function
     */
    $scope.filterTags = function (tag) {
        return function (item) {
            if (tag == "") {
                return true;
            }
            for (var i = 0; i < item.tgs.length; i++) {
                if (item.tgs[i] == tag)
                    return true;
            }
            return false;
        };
    };
    $scope.setSort = function (sortBy) {
        if ($scope.sortBy == sortBy) {
            $scope.sortAsc = !$scope.sortAsc;
        } else {
            $scope.sortBy = sortBy;
            $scope.sortAsc = false;
        }
    };
    $scope.setSortAsc = function (sortAsc) {
        $scope.sortAsc = sortAsc;
    };
    if($scope.noWatch){
        $scope.mparams = [];
    }else{
        $scope.mparams = ["o", "s"];
    }
    for (var index in $scope.wparams) {
        param = $scope.wparams[index];
        if (!$scope.noWatch && param[0] != "o" && param[0] != "s") {
            $scope.mparams.push(param[0]);
        }
    }
    if (checkNotNullEmpty($scope.reqparams)) {
        for (var index in $scope.reqparams) {
            $scope.mparams.push($scope.reqparams[index]);
        }
    }
    if (checkNotNullEmpty(requestContext)) {
        var renderContext = requestContext.getRenderContext(requestContext.getAction(), $scope.mparams);
        $scope.$on("requestContextChanged", function () {
            if (!renderContext.isChangeRelevant()) {
                return;
            }
            $scope.listinit();
            var st = $scope.init();
            if(typeof st === "undefined" || st){
                $scope.fetch();
            }
            if (contextChangeCallback != null) {
                contextChangeCallback();
            }
        });
    }
}
function CurrencyController($scope, configService) {
    $scope.currencies = {};
    $scope.showLoading();
    configService.getCurrencies().then(function (data) {
        $scope.currencies = JSON.parse(data.data);
        if (typeof $scope.finished == "function") {
            $scope.finished("cur");
        }
    }).catch(function error(msg) {
        $scope.showErrorMsg(msg);
    }).finally(function(){
        $scope.hideLoading();
    });
}
function LanguageController($scope, configService) {
    $scope.allLanguages = {};
    $scope.allMobileLanguages = {};
    $scope.showLoading();
    configService.getLanguages().then(function (data) {
        $scope.allLanguages = JSON.parse(data.data);
        if (typeof $scope.finished == "function") {
            $scope.finished("lan");
        }
    }).catch(function error(msg) {
        $scope.showErrorMsg(msg);
    }).finally(function(){
        $scope.hideLoading();
    });
    $scope.showLoading();
    configService.getMobileLanguages().then(function (data) {
        $scope.allMobileLanguages = JSON.parse(data.data);
        if (typeof $scope.finished == "function") {
            $scope.finished("mlan");
        }
    }).catch(function error(msg) {
        $scope.showErrorMsg(msg);
    }).finally(function(){
        $scope.hideLoading();
    });
}
function TimezonesController($scope, configService) {
    $scope.allTimezones = {};
    $scope.showLoading();
    configService.getTimezones().then(function (data) {
        $scope.allTimezones = data.data;
        if (typeof $scope.finished == "function") {
            $scope.finished("time");
        }
    }).finally(function(){
        $scope.hideLoading();
    });
}

function TimezonesControllerKVReversed($scope, configService) {
    $scope.allTimezonesKVReversed = {};
    $scope.showLoading();
    configService.getTimezonesKVReversed().then(function (data) {
        $scope.allTimezonesKVReversed = data.data;
        if (typeof $scope.finished == "function") {
            $scope.finished("time");
        }
    }).finally(function(){
        $scope.hideLoading();
    });
}

function TimezonesControllerWithOffset($scope, configService) {
    $scope.allTimezones = {};
    $scope.showLoading();
    configService.getTimezonesWithOffset().then(function (data) {
        $scope.allTimezones = data.data;
        if (typeof $scope.finished == "function") {
            $scope.finished("time");
        }
    }).finally(function(){
        $scope.hideLoading();
    });
}

function LocationController($scope, configService) {
    $scope.countries = {};
    $scope.states = {};
    $scope.districts = {};
    $scope.country = {};
    $scope.taluks = {};
    $scope.c = null;
    $scope.s = null;
    $scope.showLoading();
    configService.getLocations().then(function (data) {
        $scope.countries = JSON.parse(data.data);
        $scope.countries = $scope.countries.data;
        $scope.sCountries = {};
        for(var c in $scope.countries) {
            $scope.sCountries[$scope.countries[c].name] = $scope.countries[c];
            $scope.sCountries[$scope.countries[c].name].code = c;
        }
        $scope.sCountries = sortObject($scope.sCountries);
        if (typeof $scope.finished == "function") {
            $scope.finished("cou");
        }
        if (checkNotNullEmpty($scope.c)) {
            $scope.setCountry($scope.c);
        }
        if (checkNotNullEmpty($scope.s)) {
            $scope.setState($scope.s);
        }
    }).catch(function error(msg) {
        $scope.showErrorMsg(msg);
    }).finally(function(){
        $scope.hideLoading();
    });;
    $scope.clearAll = function () {
        $scope.states = {};
        $scope.districts = {};
        $scope.taluks = {};
    };
    $scope.setCountry = function (c) {
        if (checkNotNullEmpty(c)) {
            $scope.c = c;
            var country = $scope.countries[c];
            if (checkNotNullEmpty(country)) {
                $scope.country = country;
                $scope.states = country.states;
                $scope.states = sortObject($scope.states);
                $scope.districts = {};
                $scope.taluks = {};
            } else {
                $scope.clearAll();
            }
        } else {
            $scope.clearAll();
        }
    };
    $scope.getCountryCode = function (c) {
        c = c.toLowerCase();
        for (var i in $scope.countries) {
            if ($scope.countries[i].name.toLowerCase() == c) {
                return i;
            }
        }
    };
    $scope.getCountryNameByCode = function (c) {
        c = c.toLowerCase();
        for (var i in $scope.countries) {
            if (i.toLowerCase() == c) {
                return $scope.countries[i].name;
            }
        }
    };
    $scope.setCountryByName = function (c) {
        if (checkNotNullEmpty(c)) {
            var country = $scope.countries[$scope.getCountryCode(c)];
            if (checkNotNullEmpty(country)) {
                $scope.country = country;
                $scope.states = country.states;
                $scope.states = sortObject($scope.states);
                $scope.districts = {};
                $scope.taluks = {};
            } else {
                $scope.clearAll();
            }
        } else {
            $scope.clearAll();
        }
    };
    $scope.setState = function (s) {
        $scope.s = s;
        if (checkNotNullEmpty(s)) {
            var dists = $scope.states[s];
            if (checkNotNullEmpty(dists)) {
                $scope.districts = $scope.states[s].districts;
                $scope.districts = sortObject($scope.districts);
                $scope.taluks = {};
            } else {
                $scope.districts = {};
                $scope.taluks = {};
            }
        } else {
            $scope.districts = {};
            $scope.taluks = {};
        }
    };
    $scope.setDistrict = function (d) {
        if (checkNotNullEmpty(d) && checkNotNullEmpty($scope.districts[d])) {
            $scope.taluks = $scope.districts[d].taluks;
            $scope.taluks = sortObject($scope.taluks);
        } else {
            $scope.taluks = {};
        }
    };
    $scope.filterDistrict = function (term) {
        var data = [];
        term = checkNotNullEmpty(term) ? term.toLowerCase() : "";
        for (var i in $scope.districts) {
            if (term == '' || i.toLowerCase().indexOf(term) >= 0) {
                data.push(i);
            }
        }
        return data;
    };
    $scope.filter = function (term, source,isArray) {
        var data = [];
        term = checkNotNullEmpty(term) ? term.toLowerCase() : "";
        for (var i in source) {
            var text=i;
            if(isArray){
               text=source[i];
            }
            if (term == '' || text.toLowerCase().indexOf(term) >= 0) {
                data.push(text);
            }
        }
        return data;
    };
    $scope.filterCountry = function (term) {
        var data = [];
        term = checkNotNullEmpty(term) ? term.toLowerCase() : "";
        for (var i in $scope.countries) {
            if (term == '' || $scope.countries[i].name.toLowerCase().indexOf(term) >= 0) {
                data.push($scope.countries[i].name);
            }
        }
        return data;
    };
}

function WidgetConfigController($scope) {
    $scope.setAggregations = function (type, noReset) {
        if (type == 'a') {
            $scope.aggrOpt = [['t', 'Transaction'], ['au', 'Active User'], ['ae', 'Active Entity']];
        } else if (type == 'r') {
            $scope.aggrOpt = [['r', 'Revenue']];
        } else if (type == 'o') {
            $scope.aggrOpt = [['o', 'Orders'], ['fo', 'Fulfilled Order'], ['po', 'Pending Order'], ['ort', 'Order Response Time']];
        } else if (type == 'i') {
            $scope.aggrOpt = [['so', 'Stock Out Event'], ['me', 'Min Event'], ['mxe', 'Max Event'], ['rt', 'Replenishment Time']];
        } else {
            $scope.aggrOpt = undefined;
        }
        if (!noReset) {
            $scope.wid.ag = '';
            $scope.setChartTypeOptions('none');
        }
    };

    $scope.setChartTypeOptions = function (type, noReset) {
        type = checkNotNullEmpty(type) ? type : 'a';
        $scope.cTypeOpt = getChartTypeOptions(type);
        if (!noReset) {
            $scope.wid.ty = '';
        }
    };
    function getChartTypeOptions(type) {
        if (type == 'a') {
            return [
                ['bar2d', 'Bar Chart'],
                ['column2d', 'Column Chart'],
                ['line', 'Line Chart'],
                ['area2d', 'Area Chart'],
                ['pie2d', 'Pie Chart'],
                ['m', 'Map'],
                ['t', 'Table']
            ];
        }
        return undefined;
    }
    $scope.resetWidgetConfig = function() {
        $scope.wid = {};
        $scope.wid.nop = 6;
        $scope.wid.fq = 'd';
    };
    $scope.resetWidgetConfig();
}
cmnControllers.controller('NoController', ['$scope', function ($scope) {}]);

cmnControllers.controller('ResetController', ['$scope', function ($scope) {
    $scope.localFilters.forEach(function (filter) {
        addwatch(filter);
        $scope[filter] = angular.copy($scope[filter]);
    });
    function addwatch(filter) {
        $scope.$parent.$watch(filter,function(newValue,oldValue){
            if(newValue!=oldValue){
                $scope[filter] = angular.copy(newValue);
            }
        })
    }
    $scope.applyFilters = function () {
        angular.forEach($scope.localFilters,function (filter) {
            $scope.$parent[filter] = angular.copy($scope[filter]);
        });
        angular.forEach($scope.filterMethods, function(fn) {
            $scope[fn]();
        });
    };
}]);

cmnControllers.factory('focus', function($timeout, $window) {
    return function(id) {
        // timeout makes sure that it is invoked after any other event has been triggered.
        // e.g. click events that need to run before the focus or
        // inputs elements that are in a disabled state but are enabled when those events
        // are triggered.
        $timeout(function() {
            var element = $window.document.getElementById(id);
            if(element)
                element.focus();
        });
    };
});


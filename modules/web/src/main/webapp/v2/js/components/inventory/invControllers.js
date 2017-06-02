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

var invControllers = angular.module('invControllers', []);
invControllers.controller('StockViewsController', ['$scope', 'matService', 'entityService', 'requestContext', '$location', 'exportService','userService','domainCfgService',
    function ($scope, matService, entityService, requestContext, $location, exportService, userService,domainCfgService) {
        $scope.vw = "t";
        $scope.ebf = '';
        $scope.filters = {};
        $scope.mparams = ["eid", "ebf", "mid", "batchno", "etag", "mtag", "vw", "abntype", "dur", "fil",
            "matType","onlyNZStk","state","district","taluk","pdos"];
        $scope.entity = null;
        $scope.material = null;
        $scope.abntype = null;
        $scope.abndurDate = null;
        $scope.matType = "0";
        $scope.onlyNZStk = false;
        $scope.showMore = false;
        $scope.init = function (firstTimeInit) {
            $scope.loading=false;
            $scope.mtag = requestContext.getParam("mtag");
            $scope.etag = requestContext.getParam("etag");
            $scope.vw = requestContext.getParam("vw") || 't';
            $scope.locationSelected = false;
            $scope.loc = {};
            if(checkNotNullEmpty(requestContext.getParam("state"))){
                $scope.loc.state = {};
                $scope.loc.state.label = requestContext.getParam("state") || $scope.loc.state.label;
                $scope.locationSelected=true;
            }
            if(checkNotNullEmpty(requestContext.getParam("district"))){
                $scope.loc.district = {};
                $scope.loc.district.label = requestContext.getParam("district") || $scope.loc.district.label;
                $scope.loc.district.state = ($scope.loc.state!=undefined) ? $scope.loc.state.label : undefined;
                $scope.locationSelected=true;
            }
            if(checkNotNullEmpty(requestContext.getParam("taluk"))){
                $scope.loc.taluk = {};
                $scope.loc.taluk.label = requestContext.getParam("taluk") || $scope.loc.taluk.label;
                $scope.loc.taluk.state = ($scope.loc.state!=undefined) ? $scope.loc.state.label : undefined;
                $scope.loc.taluk.district = ($scope.loc.district!=undefined) ? $scope.loc.district.label : undefined;
                $scope.locationSelected=true;
            }
            if (checkNotNullEmpty(requestContext.getParam("eid"))) {
                if (checkNullEmpty($scope.entity) || $scope.entity.id != requestContext.getParam("eid")) {
                    $scope.entity = {id: parseInt(requestContext.getParam("eid")), nm: ""};
                }
                $scope.vw = "t";
                $scope.matType = requestContext.getParam("matType") ||  '0';
                $scope.onlyNZStk = requestContext.getParam("onlyNZStk") || false;
            } else if(checkNullEmpty(requestContext.getParam("mid"))){
                if(firstTimeInit && checkNotNullEmpty($scope.defaultEntityId)){
                    $location.$$search.eid = $scope.defaultEntityId;
                    $location.$$compose();
                    $scope.entity = {id: $scope.defaultEntityId, nm: ""};
                    $scope.matType = requestContext.getParam("matType") ||  '0';
                    $scope.onlyNZStk = requestContext.getParam("onlyNZStk") || false;
                }else{
                    $scope.entity = null;
                }
            }

            if (checkNotNullEmpty(requestContext.getParam("mid"))) {
                if(checkNullEmpty($scope.material) || $scope.material.mId != parseInt(requestContext.getParam("mid"))){
                    $scope.material = {mId: parseInt(requestContext.getParam("mid")),mnm:""};
                }
                $scope.onlyNZStk = requestContext.getParam("onlyNZStk") || false;
                $scope.pdos = requestContext.getParam("pdos");
                $scope.tpdos = $scope.pdos;
            }else{
                $scope.material = null;
            }
            $scope.ebf = null;
            $scope.bno = null;
            if (checkNotNullEmpty(requestContext.getParam("ebf"))) {
                $scope.ebf = constructDate(requestContext.getParam("ebf"));
                $scope.vw = 't';
                $scope.showMore = $scope.showMore || firstTimeInit;
            } else if (checkNotNullEmpty(requestContext.getParam("batchno"))) {
                $scope.bno = requestContext.getParam("batchno");
                $scope.showMore = $scope.showMore || firstTimeInit;
            }
            $scope.abntype = requestContext.getParam("abntype") || "";
            $scope.abndur = parseInt(requestContext.getParam("dur")) || "";
            if(checkNotNullEmpty($scope.abndur)){
                $scope.onAbnDurationChanged();
            }
            $scope.dur = $scope.abndur;
            if (checkNullEmpty($scope.abntype)) {
                $scope.abndur = null;
                $scope.dur = null;
            }
            $scope.showFullAbnormalStock = false;
            if (checkNotNullEmpty($scope.abntype)) {
                $scope.showFullAbnormalStock = true;
            }
            domainCfgService.getInventoryCfg().then(function(data){
                $scope.dashBoardEnabled = data.data.eidb;
            });
            $scope.batchno = $scope.bno;
            if(firstTimeInit && checkNotNullEmpty($scope.etag) &&
                (checkNotNullEmpty($scope.material) || checkNotNullEmpty($scope.ebf) ||
                    ($scope.vw == 'm' && checkNullEmpty($scope.entity)) || checkNotNullEmpty($scope.abntype))){
                $scope.showMore = true;
            }
            if(firstTimeInit && checkNotNullEmpty($scope.mtag) && (checkNotNullEmpty($scope.entity)
                || checkNotNullEmpty($scope.ebf) || checkNotNullEmpty($scope.abntype))){
                $scope.showMore = true;
            }
            if(firstTimeInit && ((checkNotNullEmpty($scope.matType) && $scope.matType != "0")
                || (checkNotNullEmpty($scope.onlyNZStk) && $scope.onlyNZStk))) {
                $scope.showMore = true;
            }
            if(firstTimeInit && $scope.loc!=undefined &&
                ($scope.loc.state != undefined) || ($scope.loc.district != undefined) || ($scope.loc.taluk != undefined)){
                $scope.showMore = true;
            }
            if(firstTimeInit && checkNotNullEmpty($scope.pdos)){
                $scope.showMore = true;
            }
            if(Object.keys($location.$$search).length == 0 && !firstTimeInit){
                $scope.$broadcast("resetRequest");
            }
        };
        $scope.onAbnDurationChanged = function () {
            if (checkNotNullEmpty($scope.abndur) && checkNotNullEmpty($scope.abntype)) {
                $scope.abndurDate = $scope.abndur;
            } else {
                $scope.abndurDate = null;
            }
            $scope.dur = $scope.abndur;
        };
        $scope.init(true);
        $scope.$watch("entity.id", watchfn("eid", null, $location, $scope, null, null, true, ["abntype", "mtag","pdos"]));
        $scope.$watch("material.mId", watchfn("mid", null, $location, $scope, null, null, true, ["vw", "abntype", "etag", "state", "district", "taluk","pdos"]));
        $scope.$watch("vw", watchfn("vw", 't', $location, $scope, null, null, true,["mid"]));
        $scope.$watch("filters.d", watchfn("fil", null, $location, $scope, null, null, true)); // if no filter is selected
        $scope.$watch("loc.state.label", watchfn("state", null, $location, $scope, null, null, true, ["mid", "etag", "mtag", "dur","batchno","abntype","ebf","district","taluk","pdos"]));
        $scope.$watch("loc.district.label", watchfn("district", null, $location, $scope, null, null, true, ["mid", "etag", "mtag", "dur","batchno","abntype","ebf","state","taluk","pdos"]));
        $scope.$watch("loc.taluk.label", watchfn("taluk", null, $location, $scope, null, null, true, ["mid", "etag", "mtag", "dur","batchno","abntype","ebf","state","district","pdos"]));
        $scope.$watch("batchno", watchfn("batchno", "", $location, $scope));
        $scope.$watch("abntype", watchfn("abntype", "", $location, $scope, null, null, true, ["eid", "mid", "etag", "mtag", "dur", "state", "district", "taluk"]));
        $scope.$watch("dur", watchfn("dur", null, $location, $scope, null, null, true, ["eid", "mid", "etag", "mtag", "abntype", "state", "district", "taluk"]));
        $scope.$watch("etag", watchfn("etag", null, $location, $scope, null, null, true, ["abntype", "mid", "dur","mtag","ebf", "state", "district", "taluk","pdos"]));
        $scope.$watch("mtag", watchfn("mtag", "", $location, $scope, null, null, true, ["abntype", "eid", "dur","etag","ebf", "state", "district", "taluk","pdos"]));
        $scope.$watch("ebf", watchfn("ebf", "", $location, $scope, function () {
        }, function (newVal) {
            return newVal.getFullYear() + "-" + (newVal.getMonth() + 1) + "-" + newVal.getDate()
        }));
        $scope.$watch("etag",function(newval, oldval){
            if(newval+""!=oldval+""){
                if (checkNotNullEmpty($scope.etag) && (checkNotNullEmpty($scope.ebf) || checkNotNullEmpty($scope.abntype))) {
                    $scope.mtag = null;
                }
                if (checkNotNullEmpty($scope.etag) && checkNotNullEmpty($scope.entity)) {
                    $scope.entity = null;
                }
            }
        });
        $scope.$watch("mtag",function(newval, oldval){
            if(newval!=oldval){
                if (checkNotNullEmpty($scope.mtag) && (checkNotNullEmpty($scope.ebf) || checkNotNullEmpty($scope.abntype))) {
                    $scope.etag = null;
                }
                if (checkNotNullEmpty($scope.mtag) && checkNotNullEmpty($scope.material)) {
                    $scope.material = null;
                }
            }
        });
        $scope.$watch("entity.id",function(newval,oldval){
            if(newval!=oldval){
                $scope.matType = "0";
                $scope.onlyNZStk = false;
            }
        });
        $scope.$watch("matType", watchfn("matType", null, $location, $scope, null, null, true, ["eid", "mid", "etag", "mtag", "abntype","onlyNZStk"]));
        $scope.$watch("onlyNZStk", watchfn("onlyNZStk", null, $location, $scope, null, null, true, ["eid", "mid", "etag", "mtag", "abntype","matType","state","district","taluk"]));
        $scope.$watch("pdos", watchfn("pdos", null, $location, $scope, null, null, true, ["eid", "mid", "etag", "mtag","matType","state","district","taluk"]));
        $scope.updateETags = function(etgs){
            $scope.etag = etgs;
        };
        $scope.updateMTags = function(mtgs){
            $scope.mtag = mtgs;
        };
        $scope.updateLoading = function(ld){
            $scope.loading = ld;
        };
        $scope.open = function ($event) {
            $event.preventDefault();
            $event.stopPropagation();
            $scope.opened = true;
        };
        $scope.getData = function(){
            $scope.batchno = $scope.bno;
        };
        $scope.getPDOSData = function(){
            $scope.pdos = $scope.tpdos;
        };
        $scope.checkBno = function(){
            if(checkNullEmpty($scope.bno)){
                $scope.batchno = undefined;
            }
        };
        var renderContext = requestContext.getRenderContext(requestContext.getAction(), $scope.mparams);
        $scope.$on("requestContextChanged", function () {
            if (!renderContext.isChangeRelevant()) {
                return;
            }
            $scope.init();
        });
    }
]);
invControllers.controller('InventoryCtrl', ['$scope', 'invService', 'domainCfgService', 'requestContext', '$location','INVENTORY',
    function ($scope, invService, domainCfgService, requestContext, $location,INVENTORY) {
        //$scope.map = {};
        $scope.wparams = [['alert', 'searchAlert'], ["o", "offset"], ["s", "size"], ["eid", "entityId"],
            ["abntype", "abntype"], ["dur", "dur"], ["mid", "mid"],["matType","matType"],["onlyNZStk","onlyNZStk"],["pdos","pdos"]];
        $scope.reqparams = ["mtag", "etag", "state", "district", "taluk"];
        $scope.init = function () {
            $scope.showFullAbnormalStock = false;
            $scope.tag = requestContext.getParam("mtag") || "";
            $scope.searchAlert = requestContext.getParam("alert") || "";
            $scope.search = {};
            $scope.search.mnm = requestContext.getParam("search") || "";
            $scope.exRow = [];
            $scope.expand = [];
            if(checkNullEmpty($scope.entity)){
                $scope.entityId = null;
            }
            $scope.entityId = requestContext.getParam("eid") || $scope.entityId ;
            $scope.mid = requestContext.getParam("mid") || "";
            $scope.abntype = requestContext.getParam("abntype") || "";
            $scope.abndur = requestContext.getParam("dur") || "";
            if (checkNotNullEmpty($scope.abntype)) {
                $scope.showFullAbnormalStock = true;
            }
            $scope.mtag = requestContext.getParam("mtag") || "";
            $scope.etag = requestContext.getParam("etag");
            $scope.matType = requestContext.getParam("matType") || "0";
            $scope.onlyNZStk = requestContext.getParam("onlyNZStk") || false;
            $scope.pdos = requestContext.getParam("pdos");
            $scope.tpdos = $scope.pdos;
        };

        $scope.init();
        function setConstants() {
            if(checkNotNullEmpty($scope.inventory) && checkNotNullEmpty($scope.inventory.results)){
                $scope.inventory.results.forEach(function(item){
                    $scope.expand[item] = false;
                    if(item.eventType == INVENTORY.stock.STOCKOUT) {
                        item.cColor = '#f2dede'; //FF0000
                        item.cHeading = $scope.resourceBundle['inventory.zerostock'];
                    } else if(item.eventType == INVENTORY.stock.UNDERSTOCK) {
                        item.cColor = '#fcf8e3'; //FFA500
                        item.cHeading = $scope.resourceBundle['inventory.lessthanmin'];
                    } else if(item.eventType == INVENTORY.stock.OVERSTOCK) {
                        item.cColor = '#d9edf7'; //FF00FF
                        item.cHeading = $scope.resourceBundle['inventory.morethanmax'];
                    }
                });
            }
        }
        ListingController.call(this, $scope, requestContext, $location);
        $scope.fetchInv = function () {
            if (checkNotNullEmpty($scope.entityId)) {
                $scope.loading = true;
                invService.getInventory($scope.entityId, $scope.tag, $scope.offset, $scope.size,
                        false, $scope.matType, $scope.onlyNZStk,$scope.pdos).then(function (data) {
                    $scope.inventory = data.data;
                    setConstants();
                    $scope.setResults($scope.inventory);
                    fixTable();
                }).catch(function error(msg) {
                    $scope.showErrorMsg(msg);
                    $scope.setResults(null);
                }).finally(function() {
                    $scope.loading = false;
                });
            }
        };

        $scope.fetchInvByLocation = function () {
            if($scope.locationSelected && checkNullEmpty($scope.entityId)){
                $scope.loading = true;
                invService.getInventoryByLocation($scope.etag, $scope.mtag, $scope.offset, $scope.size, $scope.loc, $scope.pdos).then(function (data) {
                    $scope.inventory = data.data;
                    setConstants();
                    $scope.setResults($scope.inventory);
                    fixTable();
                }).catch(function error(msg) {
                    $scope.showErrorMsg(msg);
                    $scope.setResults(null);
                }).finally(function() {
                    $scope.loading = false;
                });
            }
        };

        $scope.fetchAbnormalInv = function () {
            if (checkNotNullEmpty($scope.showFullAbnormalStock)) {
                $scope.loading = true;
                var filters = {};
                filters.et = $scope.abntype;
                if (checkNotNullEmpty($scope.abndurDate)) {
                    filters.abnBeforeDate = $scope.abndurDate;
                }
                if (checkNotNullEmpty($scope.entityId)) {
                    filters.entityId = $scope.entityId;
                }
                if (checkNotNullEmpty($scope.mid)) {
                    filters.mid = $scope.mid;
                    if (checkNotNullEmpty($scope.bno)) {
                        filters.bno = $scope.bno;
                    }
                }
                if (checkNotNullEmpty($scope.mtag) && checkNullEmpty($scope.mid)) {
                    filters.tt = "mt";
                    filters.t = $scope.mtag;
                } else if (checkNotNullEmpty($scope.etag) && checkNullEmpty($scope.entityId)) {
                    filters.tt = "en";
                    filters.t = $scope.etag;
                }
                if (checkNotNullEmpty($scope.ebf)) {
                    filters.ebf = $scope.ebf;
                }
                filters.loc = $scope.loc;
                invService.getAbnormalInventory(filters, $scope.offset, $scope.size, true).then(function (data) {
                    $scope.inventory = data.data;
                    setConstants();
                    $scope.setResults($scope.inventory);
                    fixTable();
                }).catch(function error(msg) {
                    $scope.showErrorMsg(msg);
                    $scope.setResults(null);
                }).finally(function () {
                    $scope.loading = false;
                });
            }
        };

        $scope.fetch = function () {
            $scope.exRow = [];
            if (checkNotNullEmpty($scope.entityId)) {
                if (!$scope.showFullAbnormalStock) {
                    $scope.loading = true;
                    invService.getInventoryDomainConfig($scope.entityId).then(function (data) {
                        $scope.invCnf = data.data;
                        $scope.fetchInv();
                    }).catch(function error(msg) {
                        $scope.showErrorMsg(msg);
                    });
                } else {
                    // entity filter and abnormal filter not null
                    $scope.loading = true;
                    invService.getInventoryDomainConfig($scope.entityId).then(function (data) {
                        $scope.invCnf = data.data;
                        $scope.fetchAbnormalInv();
                    }).catch(function error(msg) {
                        $scope.showErrorMsg(msg);
                    });
                }
            } else if ($scope.showFullAbnormalStock) {
                if($scope.etag instanceof Array){
                    $scope.updateETags(null);
                    return;
                }
                $scope.fetchAbnormalInv();
            } else if($scope.locationSelected && checkNullEmpty($scope.entityId) &&
                    checkNullEmpty($scope.mid) && checkNullEmpty($scope.abntype) && checkNullEmpty($scope.ebf)){
                if($scope.etag instanceof Array){
                    $scope.updateETags(null);
                    return;
                }
                $scope.fetchInvByLocation();
            }
        };
        $scope.fetch();

        $scope.metadata = [{'title': 'serialnum', 'field': 'sno'}, { 'title': 'material', 'field': 'mnm'}, {'title': 'inventory.currentstock', 'field': 'stk'},
            {'title': 'availability', 'field': 'crMnl'}, {'title': 'min', 'field': 'reord'}, {'title': 'max', 'field': 'max'},
            {'title': 'demandforecast', 'field': 'rvpdmd'}, {'title': 'lastupdated', 'field': 't'}];

        $scope.exRow = [];
        $scope.select = function (index, type) {
            $scope.expand[index] = !$scope.expand[index];
            var empty = '';
            if ($scope.exRow.length == 0) {
                for (var i = 0; i < $scope.inventory.results.length; i++) {
                    $scope.exRow.push(empty);
                }
            }
            $scope.exRow[index] = $scope.exRow[index] === type ? empty : type;
        };
        $scope.toggle = function (index) {
            $scope.select(index);
        };
        if($scope.showFilters){
            $scope.$watch("mtag", watchfn("mtag", "", $location, $scope));
        }
    }
]);
invControllers.controller('MatInventoryCtrl', ['$scope', 'invService', 'domainCfgService', 'mapService', 'requestContext', '$location','INVENTORY',
    function ($scope, invService, domainCfgService, mapService, requestContext, $location,INVENTORY) {
        $scope.wparams = [["etag", "etag"], ["o", "offset"], ["s", "size"], ["mid", "mid"],["vw","vw"],["matType","matType"],["onlyNZStk","onlyNZStk"],["pdos","pdos"]];
        $scope.reqparams = ["state","district","taluk"];
        $scope.filtered = {};
        $scope.expand = [];
        $scope.lmap = angular.copy($scope.map);
        $scope.setShow = function (mapelement) {
            mapelement.show = !mapelement.show;
        };
        $scope.reset = function(){
            $scope.offset = 0;
            $scope.inventory = undefined;
            $scope.filtered = [];
        };
        $scope.fetchNow = true;
        $scope.oldValVw = $scope.vw;
        $scope.init = function (firstTimeInit) {
            if(firstTimeInit){
                if ($location.$$search.o) {
                    delete $location.$$search.o;
                    $scope.fetchNow = false;
                    setConstants();
                }
                $location.$$compose();
            }
            var tag = requestContext.getParam("etag") || "";
            if($scope.etag != tag){
                $scope.etag = tag;
                $scope.reset();
            }
            var mid = requestContext.getParam("mid");
            if (checkNotNullEmpty(mid)) {
                if($scope.mid != mid){
                    $scope.mid = mid;
                    $scope.reset();
                }
            }
            $scope.vw = requestContext.getParam("vw") || "t";
            $scope.matType = requestContext.getParam("matType") || "0";
            $scope.onlyNZStk = requestContext.getParam("onlyNZStk") || false;
            $scope.pdos = requestContext.getParam("pdos");
            $scope.tpdos = $scope.pdos;
        };
        if($scope.fetchNow){
            $scope.init(true);
        }
        function setConstants() {
            if(checkNotNullEmpty($scope.inventory)){
                $scope.inventory.forEach(function(item){
                    if(item.eventType == INVENTORY.stock.STOCKOUT) {
                        item.cColor = '#f2dede'; //FF0000
                        item.cHeading = $scope.resourceBundle['inventory.zerostock'];
                    } else if(item.eventType == INVENTORY.stock.UNDERSTOCK) {
                        item.cColor = '#fcf8e3'; //FFA500
                        item.cHeading = $scope.resourceBundle['inventory.lessthanmin'];
                    } else if(item.eventType == INVENTORY.stock.OVERSTOCK) {
                        item.cColor = '#d9edf7'; //FF00FF
                        item.cHeading = $scope.resourceBundle['inventory.morethanmax'];
                    }
                });
            }
        }
        ListingController.call(this, $scope, requestContext, $location);
        $scope.fetchInv = function () {
            if (checkNotNullEmpty($scope.mid)) {
                $scope.loading = true;
                $scope.showLoading();
                invService.getMaterialInventory($scope.mid, $scope.etag, $scope.offset, $scope.size,$scope.matType,$scope.onlyNZStk,$scope.loc, $scope.pdos).then(function (data) {
                    if($scope.vw == 't') {
                        $scope.inventory = data.data.results;
                    } else {
                        if (checkNullEmpty($scope.inventory)) {
                            $scope.inventory = data.data.results;
                        } else {
                            $scope.inventory = angular.copy($scope.inventory).concat(data.data.results);
                        }
                    }
                    setConstants();
                    $scope.setResults(data.data);
                    var i = 0;
                    for (var item in $scope.inventory) {
                        $scope.setIcon($scope.inventory[item]);
                        $scope.expand[i++] = false;
                    }
                    $scope.filtered = $scope.getFiltered();
                    $scope.loading = false;
                    $scope.hideLoading();
                    fixTable();
                }).catch(function error(msg) {
                    $scope.showErrorMsg(msg);
                    $scope.setResults(null);
                    $scope.loading = false;
                    $scope.hideLoading();
                });
            }
        };
        $scope.$watch("$parent.vw",function(newVal,oldVal) {
            if(newVal != oldVal){
                $scope.reset();
            }
        });
        $scope.$watch("$parent.etag",function(newVal,oldVal) {
            if(newVal != oldVal){
                $scope.reset();
            }
        });
        $scope.fetch = function () {
            $scope.exRow = [];
            if (checkNotNullEmpty($scope.mid)) {
                if($scope.etag instanceof Array){
                    $scope.updateETags(null);
                    return;
                }
                $scope.showLoading();
                invService.getInventoryDomainConfig().then(function (data) {
                    $scope.invCnf = data.data;
                    $scope.fetchInv();
                }).catch(function error(msg) {
                    $scope.showErrorMsg(msg);
                }).finally(function(){
                    $scope.hideLoading();
                });
            }
        };
        $scope.fetch();

        $scope.setIcon = function(invItem){
            if(invItem.event  == 200 ){
                invItem.icon = mapService.getDangerBubbleIcon(invItem.stk);
            }else if(invItem.event  == 202){
                invItem.icon = mapService.getInfoBubbleIcon(invItem.stk);
            }else if(invItem.event  == 201){
                invItem.icon = mapService.getWarnBubbleIcon(invItem.stk);
            }else{
                invItem.icon = mapService.getBubbleIcon(invItem.stk);
            }
        };

        $scope.metadata = [{'title': 'serialnum', 'field': 'sno'}, { 'title': 'kiosk', 'field': 'enm'}, {'title': 'inventory.currentstock', 'field': 'stk'},{'title':''},
            {'title': 'availability', 'field': 'crMnl'}, {'title': 'min', 'field': 'reord'}, {'title': 'max', 'field': 'max'},
            {'title': 'demandforecast', 'field': 'rvpdmd'}, {'title': 'lastupdated', 'field': 't'}];
        domainCfgService.getEntityTagsCfg().then(function (data) {
            $scope.tags = data.data.tags;
        }).catch(function error(msg) {
            $scope.showErrorMsg(msg);
        });
        $scope.setETag = function (tag) {
            $scope.etag = tag;
        };
        $scope.getFiltered = function() {
            var list = $scope.inventory;
            var fList = [];
            if(list != null){
                for(var i in list){
                    var item = list[i];
                    if($scope.vw == 'm'){
                        if(item.lt == 0 && item.ln == 0){
                            continue;
                        }
                        var mapModel = {id:item.kId,nm:item.mnm};
                        mapModel.lt = item.lt;
                        mapModel.ln = item.ln;
                        mapModel.icon = item.icon;
                        mapModel.tu = $scope.templateUrl;
                        mapModel.tp = {item: {id:item.kId,nm:item.enm}};
                        mapModel.options = {title:mapModel.nm};
                        mapModel.show = false;
                        mapModel.onClick = function() {
                            mapModel.show = !mapModel.show;
                        };
                        fList.push(mapModel);
                    }else{
                        fList.push(item);
                    }
                }
            }
            mapService.convertLnLt(fList, $scope.lmap);
            return fList;
        };
        $scope.exRow = [];
        $scope.select = function (index, type) {
            $scope.expand[index] = !$scope.expand[index];
            var empty = '';
            if ($scope.exRow.length == 0) {
                for (var i = 0; i < $scope.inventory.length; i++) {
                    $scope.exRow.push(empty);
                }
            }
            $scope.exRow[index] = $scope.exRow[index] === type ? empty : type;
        };
        $scope.toggle = function (index) {
            $scope.select(index);
        };
    }
]);
invControllers.controller('FullInventoryCtrl', ['$scope', 'invService', 'matService', 'entityService', 'domainCfgService', 'mapService', 'requestContext', '$location','$timeout',
    function ($scope, invService, matService, entityService, domainCfgService, mapService, requestContext, $location,$timeout) {
        //$scope.map = {};
        $scope.inventory = {};
        $scope.entities = $scope.curData =  {results: []};
        $scope.curItem = 0;
        $scope.stopInvFetch = false;
        $scope.fvmtag = null;
        $scope.fvmtag = requestContext.getParam("mtag") || "";
        if(requestContext.getParam("etag") instanceof Array){
            $scope.entityTags = requestContext.getParam("etag") || [];
        }else{
            $location.search('etag',null);
            $scope.updateETags(null);
        }
        $scope.pieOpts = {
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
            //"startingAngle": 90,
            //"slicingDistance": 10,
            "labelFontSize": 14,
            "useDataPlotColorForLabels": 1,
            "showLegend": 1
        };
        $scope.tPieOpts = {
            "showLabels": 0,
            "showValues":0,
            "showPercentValues": 0,
            "showPercentInToolTip": 0,
            "theme": "fint",
            "enableRotation": 0,
            "enableSlicing": 0,
            "enableSmartLabels": 0,
            "animation": 0,
            "pieRadius": 13,
            "doughnutRadius": 4,
            "showToolTip" : 0
        };
        var renderContext = requestContext.getRenderContext(requestContext.getAction());
        var tempPieColors;
        domainCfgService.getSystemDashboardConfig().then(function (data) {
            var dconfig = angular.fromJson(data.data);
            tempPieColors = dconfig.pie.tc;
        });
        $scope.init = function(){
            if(checkNotNullEmpty(requestContext.getParam("o")) && checkNullEmpty($scope.entities.results)){
                $scope.offset = 0;
                if ($location.$$search.o) {
                    delete $location.$$search.o;
                }
                if ($location.$$search.tag) {
                    delete $location.$$search.tag;

                }
                $location.$$compose();
            }
            $scope.maxSize = 100;
            $scope.size = 10;
            var size = requestContext.getParam("s") || 10;
            if($scope.size != size){
                $scope.entities = {results: []};
            }
        };
        $scope.$watch("size",function(newVal, oldVal){
            if(newVal != oldVal){
                $scope.entities = {results: []};
            }
        });
        $scope.fetchControl = true;
        domainCfgService.getInventoryCfg().then( function(data) {
            $scope.inv = data.data;
            $scope.eTgs = [];
            if(checkNotNullEmpty($scope.inv.enTgs) && checkNullEmpty($scope.entityTags)) {
                $scope.entityTags = [];
                $scope.inv.enTgs.forEach(function(data){
                    $scope.eTgs.push({"id" : data, "text" : data});
                });
                $scope.entityTags = angular.copy($scope.inv.enTgs);
            } else if(checkNotNullEmpty($scope.entityTags)){
                $scope.entityTags.forEach(function(data){
                    $scope.eTgs.push({"id" : data, "text" : data});
                });
            }
            $scope.updateETags($scope.entityTags);
            $scope.fetchControl = false;
            $scope.fetch();
        }).catch( function error(msg) {
        });
        $scope.applyFilters = function() {
            $scope.entityTags = [];
            $scope.filtered = [];
            $scope.curData = $scope.entities = {results: []};
            $scope.inventory = {};
            $scope.eLoading = true;
            if($scope.eTgs != null && $scope.eTgs.length > 0) {
                $scope.eTgs.forEach(function(data){
                    $scope.entityTags.push(data.text);
                });
            }
            $scope.$parent.filters.d = undefined;
            if($scope.eTgs.length==0){
                $scope.$parent.filters.d = "n";
                $scope.entityTags = null;
            }
            $scope.updateETags($scope.entityTags);
            $scope.offset = 0;
            $scope.toggleFilter();
            $timeout(function () {
                $scope.fetch();
            }, 500);
        };
        matService.getDomainMaterials(null,null, 0, 300).then(function (data) {
            $scope.materials = data.data;
            $scope.getFilteredMaterials();
        }).catch(function error(msg) {
            $scope.showErrorMsg(msg);
            $scope.loading = false;
        });
        ListingController.call(this, $scope, requestContext, $location);
        $scope.init();
        $scope.getTotalMaterials = function (filtered, materialId) {
            var total = 0;
            if (checkNotNullEmpty(filtered)) {
                for (var item in filtered) {
                    var e = filtered[item];
                    if (checkNotNullEmpty(e[materialId])) {
                        total = total + e[materialId].stk;
                    }
                }
            }
            return total;
        };
        $scope.fetchInventory = function () {
            $scope.maxDataReached = false;
            if ($scope.entities.results.length > $scope.curItem && $scope.filtered.length < $scope.maxSize) {
                var e = $scope.entities.results[$scope.curItem];
                if (!checkNotNullEmpty(e.invFetched)) {
                    $scope.inventory[e.id] = {nm: e.nm, sno: e.sno, f: false, id: e.id, sdid: e.sdid, sdname: e.sdname, add: e.add, eTgs : e.tgs};
                    if (checkNotNullEmpty(e.id)) {
                        invService.getInventory(e.id, null, 0, 300,"true").then(function (data) {
                            var first = true;
                            for (var matItem in data.data.results) {
                                var material = data.data.results[matItem];
                                if(checkNotNullEmpty($scope.inventory) && checkNotNullEmpty($scope.inventory[material.kId])){
                                    $scope.inventory[material.kId][material.mId] = material;
                                    if(first) {
                                        $scope.inventory[material.kId].assetPieData = getTempPieData(material.assets);
                                        $scope.inventory[material.kId].assetPieIconData = getTempPieData(material.assets,true);
                                        first = false;
                                    }
                                    if(data.data.numFound>0){
                                        $scope.inventory[e.id]['f'] = true;
                                    }
                                    $scope.getFiltered();
                                }
                            }
                        }).catch(function error(msg) {
                            $scope.showErrorMsg(msg);
                        }).finally(function(){
                            $scope.curItem = $scope.curItem + 1;
                            if(!$scope.stopInvFetch){
                                $scope.fetchInventory();
                            }
                        });
                    }
                }
            } else {
                $scope.loading = false;
                $scope.eLoading = false;
                $scope.hideLoading();
                if($scope.filtered.length >= $scope.maxSize || $scope.curData.results.length < $scope.size) {
                    $scope.curData.numFound = undefined;
                    $scope.maxDataReached = true;
                    $scope.count = $scope.filtered.length;
                }
                $scope.curItem = 0;
                $scope.setResults($scope.curData);
            }
        };
        $scope.showFilter = false;

        $scope.cancelFilter = function() {
            $scope.toggleFilter();
        };

        $scope.toggleFilter = function() {
            $scope.showFilter = !$scope.showFilter;
            var d = document.getElementById('filter');
            if ($scope.showFilter) {
                d.style.top = '150%';
                d.style.opacity = '100';
                d.style.zIndex = '2';
            } else {
                d.style.top = '0';
                d.style.opacity = '0';
                d.style.zIndex = '-1';
            }
        };
        function getTempPieData(dimData, skipShow) {
            return [{
                value: dimData.tn,
                label: "Normal",
                color: tempPieColors[0],
                showValue: dimData.tn && !skipShow > 0 ? 1 : 0,
                toolText: "$label: $value of $unformattedSum",
                link: 'JavaScript:'
            }, {
                value: dimData.th,
                label: "High",
                color: tempPieColors[2],
                showValue: dimData.th && !skipShow > 0 ? 1 : 0,
                toolText: "$label: $value of $unformattedSum",
                link: 'JavaScript:'
            }, {
                value: dimData.tl,
                label: "Low",
                color: tempPieColors[1],
                showValue: dimData.tl && !skipShow > 0 ? 1 : 0,
                toolText: "$label: $value of $unformattedSum",
                link: 'JavaScript:'
            }, {
                value: dimData.tu,
                label: "Unknown",
                color: tempPieColors[3],
                showValue: dimData.tu && !skipShow > 0 ? 1 : 0,
                toolText: "$label: $value of $unformattedSum",
                link: 'JavaScript:'
            }];
        }
        $scope.fetch = function () {
            if (!$scope.fetchControl) {
                $scope.loading = true;
                $scope.fetchControl = true;
                    entityService.getAll($scope.offset, $scope.size, $scope.entityTags, null, true).then(function (data) {
                        $scope.entities.results = $scope.entities.results.concat(data.data.results);
                        $scope.count = data.data.numFound;
                        if (!$scope.stopInvFetch) {
                            $scope.fetchInventory();
                        }
                        $scope.curData = data.data;
                    }).catch(function error(msg) {
                        $scope.showErrorMsg(msg);
                        $scope.setResults(null);
                    }).finally(function () {
                        $scope.fetchControl = false;
                    });
                }
        };
        $scope.fetch();
        $scope.metadata = [{'title': 'serialnum', 'field': 'sno'}, { 'title': 'material', 'field': 'mnm'}, {'title': 'inventory.currentstock', 'field': 'stk'},
            {'title': 'availability', 'field': 'crMnl'}, {'title': 'min', 'field': 'reord'}, {'title': 'max', 'field': 'max'},
            {'title': 'demandforecast', 'field': 'rvpdmd'}, {'title': 'lastupdated', 'field': 't'}];

        $scope.getFiltered = function () {
            var list = [];
            if ($scope.inventory != null) {
                for (var item in $scope.inventory) {
                    var inv = $scope.inventory[item];
                    if(inv['f']) {
                        list.push(inv);
                    }
                }
                $scope.eLoading = false;
            }
            $scope.filtered = list;
            //mapService.convertLnLt($scope.filtered, $scope.map);
            return list;
        };

        $scope.getFilteredMaterials = function () {
            var list = [];
            if (checkNotNullEmpty($scope.materials) && checkNotNullEmpty($scope.materials.results)) {
                if(checkNullEmpty($scope.fvmtag)){
                    list = $scope.materials.results;
                } else {
                    for (var item in $scope.materials.results) {
                        var mat = $scope.materials.results[item];
                        for (var i in mat.tgs) {
                            if (mat.tgs[i] == $scope.fvmtag) {
                                list.push(mat);
                            }
                        }
                    }
                }
            }
            $scope.filteredMaterials = list;
            $('#runGrid').scrollLeft(0);
            return list;
        };
        $scope.$watch("fvmtag",function(newVal, oldVal){
            if(newVal != oldVal){
                $scope.getFilteredMaterials();
                $scope.$parent.updateMTags(newVal);
            }
        });
        $scope.isEmptyMatEntFilters = function () {
            return !(checkNotNullEmpty($scope.entityId) || checkNotNullEmpty($scope.mid));
        };
        $scope.$on('$destroy', function cleanup() {
            if (!renderContext.isChangeRelevant()) {
                $scope.stopInvFetch = true;
            }
        });
        $scope.$on("resetRequest", function () {
            $scope.eTgs = [];
            if(checkNotNullEmpty($scope.inv) && checkNotNullEmpty($scope.inv.enTgs)){
                $scope.inv.enTgs.forEach(function(data){
                    $scope.eTgs.push({"id" : data, "text" : data});
                });
            }
            $scope.fvmtag = null;
            $scope.applyFilters();
            $scope.showFilter = false;
        });
        $scope.$watch("loading", function(){
            $scope.$parent.updateLoading($scope.loading);
        });
    }
]);
invControllers.controller('SimpleInventoryCtrl', ['$scope', 'invService', 'domainCfgService', 'requestContext', '$location','$rootScope',
    function ($scope, invService, domainCfgService, requestContext, $location,$rootScope) {
        if(checkNullEmpty($scope.resourceBundle)){
            $scope.resourceBundle = $rootScope.resourceBundle;
        }
        $scope.currentDomain = $rootScope.currentDomain;
        $scope.mmd = $scope.$parent.$parent.$parent.$parent.mmd;
        $scope.mmdt = $scope.$parent.$parent.$parent.$parent.mmdt;
        $scope.wparams = [];
        $scope.inventory;
        $scope.noWatch = true;
        $scope.init = function () {}; //Override the parent init fn.
        $scope.init();
        ListingController.call(this, $scope, requestContext, $location);
        $scope.fetch = function () {
            $scope.loading = true;
            $scope.$parent.$parent.$parent.$parent.showLoading();
            if (checkNotNullEmpty($scope.parameter.item.id)) {
                invService.getInventory($scope.parameter.item.id, null, $scope.offset, $scope.size).then(function (data) {
                    $scope.inventory = data.data;
                    $scope.setResults($scope.inventory);
                    fixTable();
                }).catch(function error(msg) {
                    $scope.$parent.$parent.$parent.$parent.showErrorMsg(msg);
                    $scope.setResults(null);
                }).finally(function (){
                    $scope.loading = false;
                    $scope.$parent.$parent.$parent.$parent.hideLoading();
                });
            }
        };
        $scope.fetch();
        $scope.$watch("offset", function(newVal,oldVal){
            if(newVal != oldVal){
                $scope.fetch();
            }
        });


        $scope.getInventoryForMaterial = function(mid){
            if(checkNotNullEmpty(mid)){
                invService.getMaterialInventory(mid).then(function (data){
                  $scope.matInv = data.data;
                }).catch(function error(msg){
                    $scope.$parent.$parent.$parent.$parent.showErrorMsg(msg);
                }).finally(function (){
                    $scope.$parent.$parent.$parent.$parent.hideLoading();
                })
            }
        }
        $scope.metadata = [{'title': 'serialnum', 'field': 'sno'}, { 'title': 'material', 'field': 'mnm'}, {'title': 'inventory.currentstock', 'field': 'stk'},
            {'title': 'min', 'field': 'reord'}, {'title': 'max', 'field': 'max'}, {'title': 'lastupdated', 'field': 't'}];
    }
]);
invControllers.controller('BatchMaterialCtrl', ['$scope', 'invService', 'domainCfgService', 'requestContext', '$location',
    function ($scope, invService, domainCfgService, requestContext, $location) {
        $scope.wparams = [["mtag"], ["etag"], ["ebf"],["batchno"]];
        $scope.reqparams = ["state","district","taluk"];
        ListingController.call(this, $scope, requestContext, $location);
        $scope.ebf = undefined;
        $scope.loading = false;
        $scope.init = function () {
            $scope.bMaterial = {};
            if(requestContext.getParam("mtag")){
                $scope.bMaterial.t = requestContext.getParam("mtag");
                $scope.bMaterial.tt = "mt";
            }
            if(requestContext.getParam("etag")){
                if(requestContext.getParam("etag") instanceof Array){
                    $location.search("etag",null);
                }else{
                    $scope.bMaterial.t = requestContext.getParam("etag");
                    $scope.bMaterial.tt = "en";
                }
            }
            if (checkNotNullEmpty(requestContext.getParam("ebf"))) {

                $scope.ebfLink = requestContext.getParam("ebf");
                $scope.ebf = constructDate($scope.ebfLink);
            }
            if (checkNotNullEmpty(requestContext.getParam("batchno"))) {
                $scope.bMaterial.bno = requestContext.getParam("batchno");
            }
            if (checkNotNullEmpty(requestContext.getParam("mid"))) {
                $scope.bMaterial.mid = requestContext.getParam("mid");
            }
        };

        $scope.init();

        $scope.fetch = function () {
            if(checkNotNullEmpty($scope.ebf)) {
                var edt = $scope.ebf;
                $scope.bMaterial.exp = edt.getDate() + "/" + (edt.getMonth() + 1) + "/" + edt.getFullYear();
            }
            if(checkNotNullEmpty($scope.ebf) || checkNotNullEmpty($scope.bMaterial.bno)) {
                $scope.showLoading();
                invService.getBatchMaterial($scope.bMaterial, $scope.offset, $scope.size, $scope.loc).then(function (data) {
                    $scope.invBatch = data.data;
                    $scope.setResults($scope.invBatch);
                    fixTable();
                }).catch(function error(msg) {
                    $scope.showErrorMsg(msg);
                }).finally(function () {
                    $scope.hideLoading();
                })
            }
        };
        $scope.fetch();
    }
]);
invControllers.controller('BatchDetailCtrl', ['$scope', 'invService','trnService',
    function ($scope, invService, trnService) {
        $scope.showReason = false;
        $scope.transType = "";
        $scope.title = "";
        $scope.today = new Date();
        $scope.minDate = new Date();
        $scope.minDate.setMonth($scope.minDate.getMonth() - 3);

        $scope.setExpiredBatchResults = function(){
            if(checkNotNullEmpty($scope.batchDet)){
                for(var i=0; i<$scope.batchDet.length; i++){
                    if($scope.batchDet[i].isExp){
                        $scope.batchDet[i].showReason = false;
                            $scope.expBatchDet.push($scope.batchDet[i]);
                            $scope.batchDet.splice(i, 1);
                            --i;
                    }
                }
            }
        };
        $scope.getBatchDetails = function(){
            if (checkNotNullEmpty($scope.kid)) {
                $scope.showLoading();
                invService.getBatchDetail($scope.mid, $scope.kid, $scope.showAll).then(function (data) {
                    $scope.batchDet = data.data;
                    if(checkNotNullEmpty($scope.batchDet)){
                        $scope.batchPerm = $scope.batchDet[0].perm;
                    } else {
                        $scope.batchPerm = 0;
                    }
                    $scope.expBatchDet = [];
                    if (checkNullEmpty($scope.batchDet)) {
                        $scope.noDataFound = true;
                    }else{
                        $scope.setExpiredBatchResults();
                    }
                }).catch(function error(msg) {
                    $scope.showErrorMsg(msg);
                }).finally(function () {
                    $scope.hideLoading();
                });
            }
        };
        $scope.getBatchDetails();

        function fetchTransConfig() {
            if (checkNotNullEmpty($scope.kid)) {
                trnService.getTransDomainConfig($scope.kid).then(function (data) {
                    $scope.tranDomainConfig = data.data;
                }).catch(function error(msg) {
                    $scope.showErrorMsg(msg);
                });
            }
        }

        fetchTransConfig();

        $scope.openReason = function(transType, index, tgs){
            $scope.transType = transType;
            if(transType == 'p'){
                $scope.title = "Stock count";
                $scope.atd = $scope.tranDomainConfig.atdp;
            }else{
                $scope.title = "Discard";
                $scope.atd = $scope.tranDomainConfig.atdw;
            }
            $scope.reasons = [];
            $scope.tagReasons = [];
            if(checkNotNullEmpty(tgs)){
                trnService.getReasons(transType, tgs).then(function (data) {
                    $scope.tagReasons = data.data;
                    if(checkNotNullEmpty($scope.tagReasons)) {
                        $scope.reasons = $scope.tagReasons;
                        $scope.expBatchDet[index].showReason = !$scope.expBatchDet[index].showReason;
                        $scope.expBatchDet[index].reason = $scope.expBatchDet[index].showReason? $scope.tagReasons[0]: undefined;
                    }else {
                        setCommonReasons(transType,index);
                    }
                }).catch(function error(msg) {
                    $scope.showErrorMsg(msg);
                });
            }else {
                setCommonReasons(transType,index);
            }
        };

        function setCommonReasons(transType,index){
            if (checkNotNullEmpty(transType)) {
                if (checkNotNullEmpty($scope.tranDomainConfig.reasons) && checkNotNullEmpty($scope.tranDomainConfig.reasons[transType])) {
                    $scope.reasons = $scope.tranDomainConfig.reasons[transType];
                }
                $scope.expBatchDet[index].showReason = !$scope.expBatchDet[index].showReason;
                $scope.expBatchDet[index].reason = $scope.expBatchDet[index].showReason ? $scope.reasons[0]: undefined;
            }
        }


        $scope.constructFinalTransaction = function(index, transType, reason, atd) {
            var ft = {};
            var m = $scope.expBatchDet[index];
            ft['kioskid'] = '' + m.kId;
            ft['transtype'] = transType;
            ft['reason'] = reason;
            ft['materials'] = {};
            if(transType == 'p' || transType == 'w') {
                if(checkNotNullEmpty(atd)) {
                    ft['transactual'] = '' + formatDate(atd);
                } else if(checkNullEmpty(atd) && $scope.atd == 2) {
                    $scope.showWarning("Please select actual date of physical transaction.");
                    return null;
                }
            }
            ft['bmaterials'] = {};
            if(checkNotNullEmpty(m)){
                if(transType == 'p'){
                    m.q = 0;
                }
                ft['bmaterials'][m.mId + "\t" + m.bid] = {
                    q: '' + m.q,
                    e: formatDate(parseUrlDate(m.bexp)),
                    mr: m.bmfnm,
                    r : reason,
                    md: formatDate(parseUrlDate(m.bmfdt))
                };
            }
            return ft;
        };

        $scope.updateTransaction = function(index, transType, reason, atd, tgs){
            var fTransaction = $scope.constructFinalTransaction(index, transType, reason, atd);
            if(checkNullEmpty(fTransaction)) {
                return;
            }
            $scope.showLoading();
            trnService.updateTransaction(fTransaction).then(function (data) {
                if (data.data.indexOf("One or more errors") == 1) {
                    $scope.showWarning(data.data);
                } else {
                    $scope.showSuccess(data.data);
                    $scope.openReason(transType, index, tgs);
                    $scope.getBatchDetails();
                }
            }).catch(function error(msg) {
                $scope.showErrorMsg(msg);
            }).finally(function(){
                $scope.hideLoading();
            });
        };
    }
]);
invControllers.controller('HistDetailCtrl', ['$scope','invService',
    function ($scope,invService) {
        $scope.loading = true;
        $scope.showLoading();
        $scope.fetchStockEvent = function () {
            invService.getInvEventHistory($scope.url).then(function(data){
                    $scope.histData = data.data;
            }).catch(function error(msg) {
                $scope.showErrorMsg(msg);
            }).finally(function () {
                $scope.loading = false;
                $scope.hideLoading();
            });

        };
        $scope.fetchStockEvent();
    }
]);
invControllers.controller('InvMsgCtrl', ['$scope', 'invService',
    function ($scope, invService) {
        $scope.loading = true;
        function constructPredInv(data) {
            $scope.predChartOpt = {};
            $scope.predChartOpt.subCaption = "Inventory trends";
            // todo: colors need to take from system config
            $scope.predChartOpt.paletteColors = "#0075c2,#aaaaaa";
            $scope.predChartOpt.theme = "fint";
            $scope.predChartOpt.labelStep = getLabelStepValue(data.length);
            $scope.predChartOpt.yAxisMaxValue = $scope.max * 1.1;
            $scope.predChartOpt.yAxisName = "Stock quantity";
            $scope.predChartOpt.anchorRadius = 2;
            $scope.predChartOpt.lineDashLen = 1;
            $scope.predChartOpt.lineDashGap = 1;
            $scope.predChartOpt.divLineAlpha = 0;
            $scope.cLabel = getFCCategories(data);
            $scope.predChartData = [];
            $scope.predChartData[0] = getFCSeries(data, 0, "Actual", "line", true, false,'#0075c2',null,null,null,true);
            $scope.predChartData[1] = getFCSeries(data, 1, "Predicted", "line", true, false,'#aaaaaa',null,null,null,true);
            $scope.cLabel = drawVerticalLines($scope.cLabel, $scope.predChartData[1]);
            $scope.trendLines = getFCTrend($scope.min, $scope.max, 0.7);
        }

        function drawVerticalLines(labels, data) {
            var newLbl = [];
            if (checkNotNullEmpty(data) && checkNotNullEmpty(data.data)) {
                var predData = data.data;
                var zeroFound, minFound, todayFound = false;
                var mi = -1;
                for (var i = 0; i < predData.length; i++) {
                    newLbl.push(labels[i]);
                    predData[i].anchorRadius = 0;
                    if(checkNotNullEmpty(predData[i].value)) {
                        predData[i].value = parseInt(predData[i].value).toFixed(0);
                    }
                    if (!todayFound && predData[i].value != "") { // 1st Prediction (Today)
                        newLbl.push({
                            "vline": "true",
                            "linePosition": "0",
                            "label": "Today",
                            "showLabelBorder": 0
                        });
                        todayFound = true;
                    }
                    if (!minFound && predData[i].value != "" && predData[i].value <= $scope.min && $scope.min > 0) { // If less or equal to minimum in prediction
                        mi = i;
                        minFound = true;
                    }
                    if(minFound && $scope.min > 0 && predData[i].value > $scope.min) {
                        minFound = false;
                    }
                    if (!zeroFound && predData[i].value == "0") { // If 0 in prediction
                        predData[i].anchorRadius = 5;
                        predData[i].anchorSides = 3;
                        predData[i].toolText = "Likely to stock out<br/>" + predData[i].toolText;
                        zeroFound = true;
                    }
                    predData[i].dashed = 1;
                }
                if(minFound) {
                    predData[mi].anchorRadius = 5;
                    predData[mi].anchorSides = 3;
                    predData[mi].toolText = "Reorder point<br/>" + predData[mi].toolText;
                }
            }
            return newLbl;
        }
        $scope.fr = [$scope.resourceBundle['days'], $scope.resourceBundle['weeks'], $scope.resourceBundle['months']];
        $scope.showLoading();
        invService.getInventoryByMaterial($scope.kid, $scope.mid).then(function (data) {
            $scope.invMsg = data.data.results[0];
        }).catch(function error(msg) {
            $scope.showErrorMsg(msg);
        }).finally(function() {
            $scope.loading = false;
            $scope.hideLoading();
        });

        $scope.mapLoad = true;
        invService.getPredictiveStock($scope.kid,$scope.mid).then(function(data){
            $scope.predStk = data.data;
            constructPredInv($scope.predStk);
        }).catch(function error(msg) {
            $scope.showErrorMsg(msg);
        }).finally(function() {
            $scope.mapLoad = false;
        });
    }

]);
invControllers.controller('AbnormalStockCtrl', ['$scope', 'invService', 'domainCfgService', 'requestContext', '$location', 'INVENTORY', 'exportService',
    function ($scope, invService, domainCfgService, requestContext, $location, INVENTORY, exportService) {
        $scope.wparams = [["etag","etag"], ["mtag","mtag"],["event","aStock.et","200"]];
        $scope.stopInvFetch = false;
        // Read the dashboard configuration from domain config to get the default material tag to show in the Abnormal Stock view.
        domainCfgService.getDashboardCfg().then(function (data) {
            if (checkNotNullEmpty(data.data.dimtg)) {
                $scope.mtag = data.data.dimtg;
            }
        });

        $scope.setConstants = function(){
            if($scope.aStock.et == INVENTORY.stock.STOCKOUT) {
                $scope.chartColor = '#f2dede'; //FF0000
            } else if($scope.aStock.et == INVENTORY.stock.UNDERSTOCK) {
                $scope.chartColor = '#fcf8e3'; //FFA500
            } else if($scope.aStock.et == INVENTORY.stock.OVERSTOCK) {
                $scope.chartColor = '#d9edf7'; //FF00FF
            }
        };
        $scope.init = function () {
            $scope.sno = 1;
            $scope.resSize = 0;
            $scope.numFound = 0;
            $scope.sortBy = 'sno';
            $scope.sortAsc = false;
            $scope.invAbnormal = undefined;
            $scope.exRow = [];
            $scope.aStock = {};
            $scope.localOffset = 0;
            $scope.size = requestContext.getParam("s") || 50;
            $scope.size = parseInt($scope.size);
            $scope.aStock.et = requestContext.getParam("event")?requestContext.getParam("event"):INVENTORY.stock.STOCKOUT;
            $scope.setConstants();

            if(requestContext.getParam("mtag")) {
                $scope.mtag = $scope.aStock.t = requestContext.getParam("mtag");
                $scope.aStock.tt = "mt";
            } else{
                $scope.mtag = "";
            }
            if(requestContext.getParam("etag")) {
                $scope.etag = $scope.aStock.t = requestContext.getParam("etag");
                $scope.aStock.tt = "en";
            }else{
                $scope.etag = "";
            }
        };
        $scope.init();
        $scope.fetch = function () {
            //Time stamp tracking to handle multiple fetches. Old fetches will be ignored.
            var ts = $scope.ts = new Date().getMilliseconds();
            $scope.loading = true;
            invService.getAbnormalStockDetail($scope.aStock, $scope.localOffset, $scope.size).then(function (data) {
                if (!$scope.stopInvFetch && ts == $scope.ts && checkNotNullEmpty(data.data) && checkNotNullEmpty(data.data.results)) {
                    data.data.results.forEach(function (event) {
                        event.sno = $scope.sno++;
                        event.du = parseFloat(event.du);
                        event.stDt = new Date(event.stDt);
                        event.edDt = new Date(event.edDt);
                    });
                    if (checkNullEmpty($scope.invAbnormal)) {
                        $scope.invAbnormal = data.data;
                        $scope.exRow = [];
                        $scope.setResults($scope.invAbnormal);
                    } else {
                        $scope.invAbnormal.results = $scope.invAbnormal.results.concat(data.data.results);
                        $scope.resSize = $scope.resSize + data.data.results.length;
                    }
                    $scope.localOffset = $scope.localOffset + parseInt($scope.size);
                    fixTable();
                    $scope.fetch();
                } else {
                    $scope.loading = false;
                }
            }).catch(function error(msg) {
                $scope.showErrorMsg(msg);
                $scope.loading = false;
            });
        };
        $scope.fetch();
        ListingController.call(this, $scope, requestContext, $location);
        $scope.exRow = [];
        $scope.select = function (index, type) {
            var empty = '';
            if ($scope.exRow.length == 0) {
                for (var i = 0; i < $scope.invAbnormal.results.length; i++) {
                    $scope.exRow.push(empty);
                }
            }
            $scope.exRow[index] = $scope.exRow[index] === type ? empty : type;
        };
        $scope.toggle = function (index) {
            $scope.select(index);
        };

        $scope.$watch("etag",function(){
            if(checkNotNullEmpty($scope.etag)){
                $scope.mtag = null;
            }
        });
        $scope.$watch("mtag",function(){
            if(checkNotNullEmpty($scope.mtag)){
                $scope.etag = null;
            }
        });
        $scope.$on('$destroy', function cleanup() {
            $scope.stopInvFetch = true;
        });

        $scope.resetFilters = function () {
            $scope.sno = 1;
            $scope.resSize = 0;
            $scope.numFound = 0;
            $scope.loading = false;
            $scope.sortBy = '';
            $scope.sortAsc = false;
            $scope.invAbnormal = undefined;
            $scope.aStock = {};
            $scope.localOffset = 0;
            $scope.exRow = [];
            var fetchNow = false;
            if(($scope.aStock.et == INVENTORY.stock.STOCKOUT || checkNullEmpty($scope.aStock.et)) && $scope.size == 50 && $scope.mtag == "" && $scope.etag == "" ) {
                fetchNow = true
            }
            $scope.aStock.et = INVENTORY.stock.STOCKOUT;
            $scope.size = 50;
            $scope.mtag = "";
            $scope.etag = "";
            $scope.onlyNZStk = false;
            $scope.matType = "0";
            if(fetchNow){
                $scope.fetch();
            }

        }
    }
]);

invControllers.controller('MinMaxHistoryCtrl',['$scope', 'invService',
function($scope, invService) {
   $scope.init = function() {
     $scope.invHis = {};
     $scope.fetchInvHistory();

   };

    $scope.fetchInvHistory = function() {
        $scope.showLoading();
        invService.getInventoryHistory($scope.invid).then(function(data) {
            $scope.invHis = data.data;
        }).catch(function error(msg) {
            $scope.showErrorMsg(msg);
        }).finally(function() {
            $scope.hideLoading();
        })
    };
    $scope.init();
}
]);
invControllers.controller('InvBDCtrl', ['$scope', 'demandService',
    function ($scope, demandService) {
        $scope.init = function () {
            $scope.bdData = {};
            $scope.fetchTransit();
        };
        $scope.fetchTransit = function () {
            $scope.showLoading();
            var otype;
            var includeShippedOrders;
            if($scope.bdType == 'allocation') {
                otype = 'sle';
                includeShippedOrders = false;
            } else if ($scope.bdType == 'transit') {
                otype = 'prc'
                includeShippedOrders = true;
            }
            demandService.getDemandView($scope.kid, $scope.mid, undefined, undefined, otype, true).then(function (data) {
                $scope.bdData = data.data.results;
                for (var i = 0; i < $scope.bdData.length; i++) {
                    if (($scope.bdType == 'transit' && $scope.bdData[i].sq == 0) || ($scope.bdType == 'allocation' && $scope.bdData[i].astk == 0)) {
                        $scope.bdData.splice(i, 1);
                        --i;
                    }
                }
            }).catch(function error(msg) {
                $scope.showErrorMsg(msg);
            }).finally(function () {
                $scope.hideLoading();
            })
        };
        $scope.init();
    }
]);

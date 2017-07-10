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

var demandControllers = angular.module('demandControllers', []);

demandControllers.controller('SimpleEntityDemandListingsCtrl', ['$scope', 'demandService','$rootScope',
    function ($scope, demandService,$rootScope) {
        if(checkNullEmpty($scope.resourceBundle)){
            $scope.resourceBundle = $rootScope.resourceBundle;
        }
        $scope.currentDomain = $rootScope.currentDomain;
        $scope.wparams = [];
        $scope.noWatch = true;
        $scope.init = function () {}; //Override the parent init fn.
        $scope.init();
        $scope.metadata = [{'title': 'serialnum', 'field': 'sno'}, {'title': 'material', 'field': 'nm'},
            {'title': 'quantity', 'field': 'q'}, {'title': 'time', 'field': 'ts'}];
        $scope.demand;
        $scope.setData = function (data) {
            if (data != null) {
                $scope.demand = data;
                $scope.setResults($scope.demand);
            } else {
                $scope.demand = {};
                $scope.setResults(null);
            }
            $scope.loading = false;
        };
        $scope.fetch = function () {
            $scope.$parent.$parent.$parent.$parent.showLoading();
            demandService.getEntityDemand($scope.parameter.item.id, null, null, $scope.offset, $scope.size).then(function (data) {
                $scope.setData(data.data);
            }).catch(function error(msg) {
                $scope.showErrorMsg(msg);
            }).finally(function(){
                $scope.$parent.$parent.$parent.$parent.hideLoading();
            });
        };
        ListingController.call(this, $scope, null, null);
        $scope.fetch();
        $scope.$watch("offset", function(newVal,oldVal){
            if(newVal != oldVal){
                $scope.fetch();
            }
        });
    }
]);
demandControllers.controller('DemandViewsCtrl', ['$scope', 'demandService', 'domainCfgService', 'entityService', 'matService', 'mapService', 'requestContext', '$location',
    function ($scope, demandService, domainCfgService, entityService, matService, mapService, requestContext, $location) {
        $scope.wparams = [["etag","etag"], ["tag","tag"], ["eid","entity.id"], ["mid","material.mId"],["vw","vw"], ["etrn","etrn"], ["otype","otype"]];
        $scope.localFilters = ['entity', 'material', 'etag', 'tag', 'otype', 'etrn'];
        ListingController.call(this, $scope, requestContext, $location);

        $scope.init = function (firstTimeInit) {
            $scope.tag = requestContext.getParam("tag") || "";
            $scope.eftag = $scope.etag = requestContext.getParam("etag") || "";
            $scope.vw = requestContext.getParam("vw") || 't';
            $scope.etrn = requestContext.getParam("etrn") || false;
            $scope.otype = requestContext.getParam("otype") || "sle";
            $scope.showDemand = [];
            if (checkNotNullEmpty(requestContext.getParam("eid"))) {
                if (checkNullEmpty($scope.entity) || $scope.entity.id != requestContext.getParam("eid")) {
                    $scope.entity = {id: parseInt(requestContext.getParam("eid")), nm: ""};
                }
                if(checkNotNullEmpty($scope.vw) && $scope.vw == 'm'){
                    $scope.vw = "";
                }
                if(checkNotNullEmpty($scope.etag)){
                    $scope.etag = "";
                }
            } else if(!$scope.isEnt) {
                if(firstTimeInit && checkNotNullEmpty($scope.defaultEntityId)){
                    $location.$$search.eid = $scope.defaultEntityId;
                    $location.$$compose();
                    $scope.entity = {id: $scope.defaultEntityId, nm: ""};
                }else {
                    $scope.entity = null;
                }
            }

            if (checkNotNullEmpty(requestContext.getParam("mid"))) {
                if(checkNullEmpty($scope.material) || $scope.material.mId != parseInt(requestContext.getParam("mid"))){
                    $scope.material = {mId: parseInt(requestContext.getParam("mid")),mnm:""};
                }
                if(checkNotNullEmpty($scope.tag)){
                    $scope.tag = "";
                }
            }else{
                $scope.material = null;
            }
            if (typeof  $scope.showEntityFilter === 'undefined') {
                $scope.showEntityFilter = true;
            }
        };
        $scope.init(true);

        $scope.select = function (index) {
            $scope.expand[index] = !$scope.expand[index];
            $scope.exRow[index] = $scope.expand[index] ? 'true' : '';
        };

        $scope.toggle = function (index) {
            $scope.showDemand[index] = !$scope.showDemand[index];
        };
        $scope.setData = function (data) {
            if (data != null) {
                $scope.demand = data;
                $scope.setResults($scope.demand);
                fixTable();
            } else {
                $scope.demand = {};
                $scope.setResults(null);
            }
            $scope.filtered = $scope.demand.results;
            $scope.loading = false;
        };

        $scope.fetch = function () {
            $scope.demand = {};
            $scope.loading = true;
            $scope.showLoading();
            var eId = undefined;
            var mId = undefined;
            if(checkNullEmpty($scope.showBackOrder)) {
                $scope.showBackOrder = false;
            }
            if(checkNotNullEmpty($scope.entity)) {
                eId = $scope.entity.id;
            } else if(checkNotNullEmpty($scope.material)) {
                mId = $scope.material.mId;
            }
            demandService.getDemand($scope.etag, $scope.tag, eId, mId, $scope.etrn,$scope.showBackOrder, $scope.offset, $scope.size, $scope.otype).then(function(data) {
                $scope.setData(data.data);
            }).catch(function error(msg) {
                $scope.showErrorMsg(msg);
                $scope.setData(null);
            }).finally(function() {
                $scope.hideLoading();
            });
        };
        $scope.fetch();
    }
]);

demandControllers.controller('DemandTableController', ['$scope', 'demandService','$uibModal','$timeout',
    function ($scope, demandService, $uibModal,$timeout) {
        $scope.invalidPopup = 0;
        var status;
        var sourceCode;
        $scope.allocateData = function () {
            $scope.hideShipments = true;
            $scope.edit = false;
            $scope.demandTable = angular.copy($scope.dItem.allocations);
            $scope.batValues = [];
            if(checkNotNullEmpty($scope.dItem)) {
                if ($scope.dItem.tm) {
                    status = checkNotNullEmpty($scope.tempmatstatus) ? $scope.tempmatstatus : undefined;
                } else {
                    status = checkNotNullEmpty($scope.matstatus) ? $scope.matstatus : undefined;
                }
                if ($scope.dItem.isBa) {
                    sourceCode = $scope.dItem.tm ? 'bmt' : 'bm';
                } else {
                    sourceCode = $scope.dItem.tm ? 'mt' : 'm';
                }
            }
            if(checkNotNullEmpty($scope.demandTable)) {
                $scope.demandTable.forEach(function(data){
                    $scope.batValues.push(data.allocations);
                });
                $scope.hideShipments = $scope.demandTable.some(function(data) {
                    return checkNullEmpty(data.allocations);
                });
            }
            $scope.isBa = checkNotNullEmpty($scope.demandTable[0]) && checkNotNullEmpty($scope.demandTable[0].bid);
            $scope.modalInstance = $uibModal.open({
                templateUrl: 'views/orders/allocations.html',
                scope: $scope,
                keyboard: false,
                backdrop: 'static'
            });
        };
        $scope.toggleAllocate = function() {
            $scope.edit = !$scope.edit;
        };
        function isValid () {
            var index = 0;
            var sourceData = $scope.dItem.tm ? "at" : "a";
            var isInvalid = $scope.demandTable.some(function (data) {
                data.isVisitedStatus = true;
                if(!($scope.validate(data, index, sourceData) && $scope.validateStatus(data, index, sourceCode))) {
                    return true;
                }
                index++;
            });
            if(isInvalid) {
                return false;
            }
            var aoQty = 0;
            if($scope.dItem.isBa) {
                var aQty = 0;
                $scope.demandTable.forEach(function (data) {
                    aoQty += (data.oQty || 0) * 1;
                    aQty += (data.qty || 0) * 1;
                });
            } else {
                aoQty = $scope.demandTable[0].oQty || 0;
                aQty = $scope.demandTable[0].qty || 0;
            }
            var maxAQ = Math.min($scope.item.atpstk, $scope.dItem.yta) + parseInt(aoQty);
            if (aQty > maxAQ) {
                $scope.showWarning("Allocation cannot be greater than available/yet to allocate quantity " + maxAQ);
                return false;
            }
            return true;
        }
        function setAllocationMetaData() {
            $scope.astk = 0;
            $scope.demandTable.forEach(function (data) {
                data.qty = data.qty || 0;
                data.bQty = data.oQty = data.qty;
                if(data.bQty == 0 && checkNotNullEmpty(status)) {
                    data.mst = status[0];
                }
            });
        };
        $scope.proceed = function() {
            if(isValid()) {
                setAllocationMetaData();
                if (checkNotNullEmpty($scope.demandTable)) {
                    var ind = 0;
                    $scope.batValues.forEach(function (data) {
                        $scope.demandTable[ind].allocations = data;
                        ind += 1;
                    });
                }
                $scope.showLoading();
                demandService.allocateQuantity({model: $scope.demandTable}).then(function () {
                    $scope.dItem.allocations = angular.copy($scope.demandTable);
                    $scope.astk = 0;
                    if(checkNotNullEmpty($scope.dItem.allocations)) {
                        $scope.dItem.allocations.forEach(function(d){
                            $scope.astk += parseInt(d.oQty);
                            d.aQty = d.oQty;
                        })
                    }
                    var diff = $scope.dItem.astk - $scope.astk;
                    $scope.dItem.astk = $scope.astk;
                    $scope.dItem.yta = $scope.dItem.q - ($scope.dItem.astk + $scope.dItem.sq);
                    $scope.dItem.atpstk = $scope.dItem.atpstk + diff;
                    $scope.resetAllocatedQuantity(diff);
                    $scope.showSuccess("Allocation done successfully");
                    $scope.cancel();
                }).catch(function (errMsg) {
                    $scope.showErrorMsg(errMsg);
                }).finally(function () {
                    $scope.hideLoading();
                });
            }
        };
        $scope.cancelAllocation = function () {
            var ind = 0;
            $scope.demandTable.forEach(function(data){
                return $scope.hidePop(data,ind++);
            });
            $scope.toggleAllocate();
        };
        $scope.cancel = function () {
            $scope.modalInstance.dismiss('close');
        };
        $scope.clearAllocations = function (type) {
            $scope.showLoading();
            var eid = undefined;
            var oid = undefined;
            var id = undefined;
            if (type == 'falloc') {
                eid = $scope.item.e.id;
                oid = null;
                id = $scope.item.id;
            } else if (type == 'alloc') {
                eid = null;
                oid = $scope.dItem.oid;
                id = $scope.dItem.id;
            }
            demandService.clearAllocations(eid, id, oid, $scope.etrn, $scope.showBackOrder).then(function () {
                $scope.showSuccess("Allocations cleared successfully.");
                if (oid == null) {
                    $scope.fetch();
                } else {
                    $scope.resetAllocatedQuantity($scope.dItem.astk);
                    $scope.dItem.astk = 0;
                    $scope.dItem.yta = $scope.dItem.q - ($scope.dItem.astk + $scope.dItem.sq);
                    var aq = Math.min($scope.data.atpstk, $scope.dItem.yta);
                    $scope.dItem.allocations.forEach(function(data) {
                        data.oQty = data.aQty = 0;
                        if($scope.dItem.isBa) {
                            data.bQty = data.qty = 0;
                            if (checkNotNullEmpty(aq) && aq > 0) {
                                if (aq <= data.batpstk) {
                                    data.qty = aq;
                                } else {
                                    data.qty = data.batpstk;
                                }
                                aq -= data.qty;
                            }
                        } else {
                           data.qty = aq;
                        }
                        if ($scope.dItem.tm) {
                            status = checkNotNullEmpty($scope.tempmatstatus) ? $scope.tempmatstatus[0] : undefined;
                        } else {
                            status = checkNotNullEmpty($scope.matstatus) ? $scope.matstatus[0] : undefined;
                        }
                        data.mst = status;
                    });
                }
            }).catch(function (errMsg) {
                $scope.showErrorMsg(errMsg);
            }).finally(function () {
                $scope.hideLoading();
            });
        };

        $scope.hidePop = function (material, index, source, isAllocate, isStatus) {
            hidePopup($scope, material, source + material.matId, index, $timeout, isAllocate, isStatus);
        };

        $scope.validate = function (material, index, source) {
            var bName = $scope.dItem.isBa ? material.bid : $scope.dItem.nm;
            if (checkNotNullEmpty($scope.dItem.huName) && checkNotNullEmpty($scope.dItem.huQty) && checkNotNullEmpty(material.qty) && material.qty % $scope.dItem.huQty != 0) {
                showPopup($scope, material, source + material.matId, material.qty + " of " + bName + " does not match the multiples of units expected in " +
                    $scope.dItem.huName + ". It should be in multiples of " + $scope.dItem.huQty + " " + bName + ".", index, $timeout);
                return false;
            }
            return true;
        };
        $scope.validateStatus = function(material, index, source) {
            if (checkNotNullEmpty(status) && $scope.transConfig.ism && checkNotNullEmpty(source)) {
                if ($scope.dItem.isBa) {
                    if (material.qty > 0 && checkNullEmpty(material.mst) && material.isVisitedStatus) {
                        showPopup($scope, material, source + material.matId, $scope.resourceBundle['status.required'], index, $timeout, false, true);
                        return false;
                    }
                } else {
                    if (material.qty > 0 && checkNullEmpty($scope.demandTable[0].mst) && $scope.demandTable[0].isVisitedStatus) {
                        showPopup($scope, material, source + material.matId, $scope.resourceBundle['status.required'], index, $timeout, true);
                        return false;
                    }
                }
            }
            return true;
        };
    }
]);

demandControllers.controller('DemandDetailCtrl', ['$scope', 'demandService', 'requestContext', 'ORDER','trnService',
        function($scope, demandService, requestContext, ORDER, trnService) {
            $scope.ORDER = ORDER;
            $scope.wparams = [["kId", "kId"], ["mId", "mId"]];
            $scope.showLoading();
            trnService.getMatStatus("i", false).then(function(data){
                $scope.matstatus = data.data;
            }).catch(function error(msg){
                $scope.showErrorMsg(msg);
            }).finally(function() {
                $scope.hideLoading();
            });
            $scope.showLoading();
            trnService.getMatStatus("i", true).then(function(data) {
                $scope.tempmatstatus = data.data;
            }).catch(function error(msg) {
                $scope.showErrorMsg(msg);
            }).finally(function() {
                $scope.hideLoading();
            });
            $scope.showLoading();
            trnService.getStatusMandatory().then(function(data) {
                $scope.transConfig = data.data;
            }).catch(function error(msg) {
                $scope.showErrorMsg(msg);
            }).finally(function() {
                $scope.hideLoading();
            });
            $scope.init = function() {
                $scope.etrn = requestContext.getParam("etrn");
                $scope.bo = requestContext.getParam("bo");

            };
            if(checkNotNullEmpty($scope.demandDataList)) {
                $scope.data = $scope.demandDataList;
            }
            $scope.setData = function (data) {
                $scope.demandlist = [];
                $scope.demand = [];
                if (data != null) {
                    data.results.forEach(function(detail) {
                        var aq = Math.min($scope.item.atpstk, detail.yta);
                        var status;
                        if(detail.tm) {
                            status = checkNotNullEmpty($scope.tempmatstatus) ? $scope.tempmatstatus[0] : undefined;
                        } else {
                            status = checkNotNullEmpty($scope.matstatus) ? $scope.matstatus[0] : undefined;
                        }
                            if (detail.isBa) {
                                detail.allocations.some(function (detailData) {
                                    detailData.qty = detailData.oQty;
                                    if(detailData.qty == 0) {
                                        if ($scope.item.astk == 0 && checkNotNullEmpty(aq) && aq > 0) {
                                            if (aq <= detailData.batpstk) {
                                                detailData.qty = aq;
                                            } else {
                                                detailData.qty = detailData.batpstk;
                                            }
                                            aq -= detailData.qty;
                                        }
                                        detailData.mst = status;
                                    } else {
                                        detailData.mst = detailData.mst || '';
                                    }
                                });
                            } else {
                                detail.allocations[0].qty = checkNotNullEmpty(detail.allocations[0].oQty) ? detail.allocations[0].oQty : aq;
                                if(checkNullEmpty(detail.allocations[0].oQty)) {
                                    detail.allocations[0].mst = status;
                                } else {
                                    detail.allocations[0].mst = detail.allocations[0].mst || '';
                                }
                            }
                        // separate transfer and non-transfer orders
                        if(detail.oty == 0) {
                            $scope.demand.push(detail);
                        } else {
                            $scope.demandlist.push(detail);
                        }
                    });
                    $scope.setResults(data);
                } else {
                    $scope.setResults(null);
                }
                $scope.loading = false;
                if($scope.otype == 'sle') {
                    $scope.title = $scope.resourceBundle['salesorders'];
                } else if($scope.otype == 'prc') {
                    $scope.title = $scope.resourceBundle['purchaseorders'];
                }
                if ($scope.transRelease) {
                    $scope.tTitle = $scope.resourceBundle['releases']
                } else {
                    $scope.tTitle = $scope.resourceBundle['transfers']
                }
            };
            $scope.resetAllocatedQuantity = function(quantity) {
                $scope.data.astk = $scope.data.astk - quantity;
                $scope.data.atpstk = $scope.data.atpstk + quantity;

            };
            $scope.fetch = function() {
                $scope.demand = {};
                $scope.loading = true;
                $scope.showLoading();
                demandService.getDemandView($scope.kId, $scope.mId, $scope.exTrn, $scope.bo, $scope.oty).then(function(data) {
                    $scope.setData(data.data);
                }).catch(function error(msg) {
                    $scope.showErrorMsg(msg);
                    $scope.setData(null);
                }).finally(function() {
                    $scope.hideLoading();
                    $scope.loading = false;
                });
            };
            $scope.fetch();

        }]);

demandControllers.controller('MatDemandListMapCtrl', ['$scope', 'demandService', 'domainCfgService', 'entityService', 'matService', 'mapService', 'requestContext', '$location',
    function ($scope, demandService, domainCfgService, entityService, matService, mapService, requestContext, $location) {
        $scope.wparams = [["etag", "etag"], ["o", "offset"], ["s", "size"], ["mid", "material.mId"],["vw","vw"]];
        $scope.demand;
        $scope.etag;
        $scope.from;
        $scope.material = null;
        $scope.tag;
        $scope.heatData = [];
        $scope.lmap = angular.copy($scope.map);
        $scope.today = new Date();
        $scope.fetchNow = true;
        $scope.addHeatLayer = function (heatLayer) {
            var pointArray = new google.maps.MVCArray($scope.heatData);
            heatLayer.setData(pointArray);
            heatLayer.set('maxIntensity', 20);
            heatLayer.set('radius', 20);
        };
        $scope.getIcon = function (q) {
            return mapService.getBubbleIcon(q);
        };
        $scope.oldValVw = $scope.vw;
        $scope.init = function (firstTimeInit) {
            if(firstTimeInit){
                if ($location.$$search.o) {
                    delete $location.$$search.o;
                    $scope.fetchNow = false;
                }
                $location.$$compose();
            }
            var tag = requestContext.getParam("etag") || "";
            if($scope.etag != tag){
                $scope.eftag = $scope.etag = tag;
                $scope.demand = {};
            }
            if (checkNotNullEmpty(requestContext.getParam("mid"))) {
                if(checkNullEmpty($scope.material) || $scope.material.mId != requestContext.getParam("mid")){
                    $scope.material = {mId: parseInt(requestContext.getParam("mid")),mnm:""};
                    $scope.demand = {};
                }
            }
        };
        $scope.init(true);
        ListingController.call(this, $scope, requestContext, $location);

        $scope.fetch = function () {
            console.log('fetch called');
            if (checkNotNullEmpty($scope.material)) {
                $scope.loading = true;
                $scope.showLoading();
                var from = new Date();
                from.setDate($scope.today.getDate() - 90);
                demandService.getMaterialDemand($scope.material.mId, formatDate(from), $scope.eftag, $scope.offset, $scope.size,true).then(function (data) {
                    $scope.setResults(data.data);
                    if(checkNotNullEmpty(data.data.results)){
                        if (checkNullEmpty($scope.demand.results)) {
                            $scope.demand.results = data.data.results;
                        } else {
                            $scope.demand.results = angular.copy($scope.demand.results).concat(data.data.results);
                        }

                    }
                    $scope.heatData = [];
                    for (var item in $scope.demand.results) {
                        var myitem = $scope.demand.results[item];
                        if (checkNotNullEmpty(myitem.e)) {
                            $scope.heatData.push({
                                location: new google.maps.LatLng(myitem.e.lt, myitem.e.ln),
                                weight: parseFloat(myitem.q)
                            });
                            myitem.icon = mapService.getBubbleIcon(myitem.q);
                            myitem.options = {title:myitem.e.nm+','+ myitem.e.ct};
                        }
                        myitem.show = true;
                    }
                    $scope.filtered = $scope.getFiltered();
                }).catch(function error(msg) {
                    $scope.showErrorMsg(msg);
                    $scope.setResults(null);
                }).finally(function(){
                    $scope.loading = false;
                    $scope.hideLoading();
                });
            }
        };

        if($scope.fetchNow){
            $scope.fetch();
        }
        $scope.getFiltered = function () {
            var list = [];
            var dupMap = {};
            if ($scope.demand != null) {
                for (var item in $scope.demand.results) {
                    var demand = $scope.demand.results[item];
                    if($scope.vw == 'm' && demand.e.lt == 0 && demand.e.ln == 0){
                        continue;
                    }
                    var km = demand.id +":" +demand.e.id;
                    if(!dupMap[km]){
                        list.push(demand);
                        dupMap[km] = true;
                    }
                }
            }
            mapService.convertLnLt(list, $scope.lmap,'e');
            return list;
        };
        $scope.$watch("$parent.vw",function(newVal,oldVal) {
            if(newVal != oldVal){
                $scope.offset = 0;
                $scope.demand = {};
                $scope.filtered = [];
            }
        });

        $scope.$watch("$parent.etag",function(newVal,oldVal) {
            if(newVal != oldVal){
                $scope.offset = 0;
                $scope.demand = {};
                $scope.filtered = [];
            }
        });
    }
]);

demandControllers.controller('DiscrepanciesListingCtrl', ['$scope', 'demandService', 'domainCfgService', 'entityService', 'matService', 'exportService', 'ordService', 'requestContext', '$location',
    function ($scope, demandService, domainCfgService, entityService, matService, exportService, ordService , requestContext, $location) {
        $scope.wparams = [["etag", "eTag"], ["mtag", "mTag"], ["o", "offset"], ["s", "size"], ["eid", "entity.id"], ["mid", "material.mId"],["oid", "orderId"],["from", "from", "", formatDate2Url],["to", "to", "", formatDate2Url],["dt", "discType"],["otype","oType","sle"], ["etrn", "etrn"]];
        $scope.localFilters = ['entity', 'material', 'discType', 'etrn', 'ordId', 'from', 'to', 'eTag', 'mTag'];
        $scope.demandWithDisc;
        $scope.eTag;
        $scope.mTag;
        $scope.from;
        $scope.to;
        $scope.materialId = null;
        $scope.entityId = null;
        $scope.orderId;
        $scope.status;
        $scope.entity;
        $scope.material;
        $scope.discType;
        $scope.today = formatDate2Url(new Date());

        $scope.init = function (firstTimeInit) {

            $scope.mTag = requestContext.getParam("mtag") || "";
            $scope.eTag = requestContext.getParam("etag") || "";
            $scope.status = requestContext.getParam("st") || "";
            $scope.orderId = $scope.ordId = requestContext.getParam("oid") || undefined;
            $scope.from = parseUrlDate(requestContext.getParam("from")) || "";
            $scope.to = parseUrlDate(requestContext.getParam("to")) || "";
            $scope.discType = requestContext.getParam("dt") || "";
            $scope.oType = requestContext.getParam("otype") || "sle";
            $scope.etrn = requestContext.getParam("etrn") || false;

            if (checkNotNullEmpty(requestContext.getParam("eid"))) {
                if (checkNullEmpty($scope.entity) || $scope.entityId != requestContext.getParam("eid")) {
                    $scope.entity = {id: parseInt(requestContext.getParam("eid")), nm: ""};
                }
            } else if(!$scope.isEnt){
                if(firstTimeInit && checkNotNullEmpty($scope.defaultEntityId)){
                    $location.$$search.eid = $scope.defaultEntityId;
                    $location.$$compose();
                    $scope.entity = {id: $scope.defaultEntityId, nm: ""};
                }else {
                    $scope.entity = null;
                }
            }

            if (checkNotNullEmpty(requestContext.getParam("mid"))) {
                if(checkNullEmpty($scope.material) || $scope.material.mId != parseInt(requestContext.getParam("mid"))){
                    $scope.material = {mId: parseInt(requestContext.getParam("mid")),mnm:""};
                }
            }else{
                $scope.material = null;
            }
            if (checkNotNullEmpty($scope.orderId) || checkNotNullEmpty($scope.from) || checkNotNullEmpty($scope.to) || checkNotNullEmpty($scope.eTag) || checkNotNullEmpty($scope.mTag)) {
                $scope.showMore = true;
            }
            $scope.showColumns();
        };
        $scope.setData = function (data) {
            if (data != null) {
                $scope.demandWithDisc = data;
                $scope.setResults($scope.demandWithDisc);
                $scope.setFiltered($scope.demandWithDisc.results);
                fixTable();
            } else {
                $scope.demandWithDisc = {};
                $scope.setResults(null);
            }
            $scope.loading = false;
        };

        $scope.fetch = function () {
            //Time stamp tracking to handle multiple fetches. Old fetches will be ignored.
            var ts = $scope.ts = new Date().getMilliseconds();
            $scope.demandWithDisc = {};
            $scope.loading = true;
            $scope.showLoading();
            var eid, mid;
            if (checkNotNullEmpty($scope.entity)) {
                eid = $scope.entity.id;
            }
            if(checkNotNullEmpty($scope.material)){
                mid = $scope.material.mId;
            }
            demandService.getDiscrepancies($scope.oType, $scope.etrn, eid, mid, $scope.eTag, $scope.mTag, formatDate($scope.from), formatDate($scope.to), $scope.orderId, $scope.discType, $scope.offset, $scope.size).then(function (data) {
                if(ts == $scope.ts) {
                    $scope.setData(data.data);
                }
            }).catch(function error(msg) {
                $scope.showErrorMsg(msg);
                $scope.setData(null);
            }).finally(function(){
                $scope.hideLoading();
            });

        };
        $scope.$watch("etag",function(newVal){
            if(checkNotNullEmpty(newVal)){
                $scope.mTag = null;
                $scope.entity = undefined;
            }
        });
        $scope.$watch("mtag",function(newVal){
            if(checkNotNullEmpty(newVal)){
                $scope.eTag = null;
            }
        });
        $scope.$watch("entity.id",function(newVal){
            if(checkNotNullEmpty(newVal)){
                // $scope.showMore = true;
                $scope.showColumns();
            }
        });
        $scope.showColumns = function() {
            if (checkNotNullEmpty($scope.entity) && checkNotNullEmpty($scope.entity.id)) {
                if($scope.oType == 'sle') {
                    $scope.showCustomer = true;
                    $scope.showVendor = false;
                } else if ($scope.oType == 'prc'){
                    $scope.showVendor = true;
                    $scope.showCustomer = false;
                }
            } else {
                $scope.showCustomer = true;
                $scope.showVendor = true;
            }
        };
        $scope.$watch("etrn", function(newVal) {
            $scope.etrn = newVal;
        });
        $scope.resetFilters = function() {
            if($scope.showEntityFilter) {
                $scope.entity = undefined;
            }
            $scope.material = undefined;
            $scope.entityId = undefined;
            $scope.materialId = undefined;
            $scope.eTag = undefined;
            $scope.mTag = undefined;
            $scope.from = undefined;
            $scope.to = undefined;
            $scope.orderId = undefined;
            $scope.discType = undefined;
            $scope.oType = "sle";
            $scope.etrn = false;
            $scope.showMore = undefined;
            $scope.showColumns();
        };
        $scope.getSuggestions = function (text, type) {
            if (checkNotNullEmpty(text)) {
                var oty = ($scope.etrn == true ? '1' : undefined); // If exclude transfers is checked, then show both transfer and non transfer order ids in suggestions.
                return ordService.getIdSuggestions(text, type, oty).then(function (data) {
                    return data.data;
                }).catch(function (errorMsg) {
                    $scope.showErrorMsg(errorMsg);
                });
            }
        };

        $scope.init(true);
        ListingController.call(this, $scope, requestContext, $location);
        $scope.fetch();
    }
]);

demandControllers.controller('DiscrepanciesDetailCtrl', ['$scope','$compile','$timeout',
    function ($scope, $compile, $timeout) {
        $scope.isDisplaying = false;
        $scope.showReasonPopup = function() {
            $scope.isDisplaying = true;
        };

        $scope.hideReasonPopup = function() {
            $scope.isDisplaying = false;
        };

    }
]);

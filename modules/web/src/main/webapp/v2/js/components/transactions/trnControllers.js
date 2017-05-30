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

var trnControllers = angular.module('trnControllers', []);
trnControllers.controller('TransactionsCtrl', ['$scope', 'trnService', 'domainCfgService', 'entityService', 'requestContext', '$location', 'exportService',
    function ($scope, trnService, domainCfgService, entityService, requestContext, $location, exportService) {
        $scope.wparams = [
            ["tag", "tag"], ["etag", "etag"], ["type", "type"], ["from", "from", "", formatDate2Url],
            ["to", "to", "", formatDate2Url], ["o", "offset"], ["s", "size"],["batchnm","bid"],
            ["lceid","cust.id"],["lveid","vend.id"],["mid", "material.mId"],["atd","atd"],["rsn","reason"]];
        $scope.today = formatDate2Url(new Date());
        $scope.atd = false;
        $scope.init = function (firstTimeInit) {
            if (typeof  $scope.showEntityFilter === 'undefined') {
                $scope.showEntityFilter = true;
                if(firstTimeInit){
                    $scope.wparams.push(["eid", "entity.id"]);
                }
            }
            if (checkNotNullEmpty(requestContext.getParam("mid"))) {
                if(checkNullEmpty($scope.material) || $scope.material.mId != parseInt(requestContext.getParam("mid"))) {
                    $scope.material = {mId: parseInt(requestContext.getParam("mid")),mnm:""};
                }
            } else {
                $scope.material = null;
            }
            //$scope.searchTag = requestContext.getParam("tag") || "";
            $scope.type = requestContext.getParam("type") || "";
            $scope.tag = requestContext.getParam("tag") || "";
            $scope.etag = requestContext.getParam("etag") || "";
            $scope.from = parseUrlDate(requestContext.getParam("from")) || "";
            $scope.to = parseUrlDate(requestContext.getParam("to")) || "";
            $scope.lEntityId = requestContext.getParam("lceid") || "";
            $scope.batchId = $scope.bid = requestContext.getParam("batchnm") || "";
            $scope.reason = requestContext.getParam("rsn") || "";
            if(checkNotNullEmpty($scope.from) || checkNotNullEmpty($scope.to)) {
                $scope.showMore = true;
            }
            if(checkNullEmpty($scope.lEntityId)) {
                $scope.lEntityId = requestContext.getParam("lveid") || "";
            }
            //$scope.to = parseUrlDate(requestContext.getParam("to")) || "";
            if (checkNotNullEmpty(requestContext.getParam("eid"))) {
                if(checkNullEmpty($scope.entity) || $scope.entity.id != parseInt(requestContext.getParam("eid"))) {
                    $scope.entity = {id: parseInt(requestContext.getParam("eid")), nm: ""};
                }
                if(checkNotNullEmpty(requestContext.getParam("lceid"))) {
                    if (checkNullEmpty($scope.cust) || $scope.cust.id != parseInt(requestContext.getParam("lceid"))) {
                        $scope.cust = {id: parseInt(requestContext.getParam("lceid")), nm: ""};
                    }
                }
                if (checkNotNullEmpty(requestContext.getParam("lveid"))) {
                    if (checkNullEmpty($scope.vend) || $scope.vend.id != parseInt(requestContext.getParam("lveid"))) {
                        $scope.vend = {id: parseInt(requestContext.getParam("lveid")), nm: ""};
                    }
                }
                entityService.getLinksCount(requestContext.getParam("eid")).then(function (data) {
                    var counts = data.data.replace(/"/g, "").split(",");
                    $scope.customerCount = counts[0];
                    $scope.vendorCount = counts[1];
                }).catch(function error(msg) {
                    $scope.showErrorMsg(msg);
                });
            } else if(checkNotNullEmpty(requestContext.getParam("mid"))){
                if(checkNotNullEmpty($scope.material) || $scope.material.mId != parseInt(requestContext.getParam("mid"))) {
                    $scope.material = {mId: parseInt(requestContext.getParam("mid")), mnm: "",b:$scope.material.b}  ;
                }
            } else if($scope.showEntityFilter){
                if(firstTimeInit && checkNotNullEmpty($scope.defaultEntityId)){
                    $location.$$search.eid = $scope.defaultEntityId;
                    $location.$$compose();
                    $scope.entity = {id: $scope.defaultEntityId, nm: ""};
                }else{
                    $scope.entity = null;
                }
            }

        };
        $scope.init(true);
        ListingController.call(this, $scope, requestContext, $location);
        $scope.setData = function (data) {
            if (data != null) {
                $scope.transactions = data;
                $scope.setResults($scope.transactions);
            } else {
                $scope.transactions = {results:[]};
                $scope.setResults(null);
            }
            $scope.loading = false;
            $scope.hideLoading();
            fixTable();

        };
        $scope.fetch = function () {
            $scope.transactions = {results:[]};
            $scope.exRow = [];
            $scope.loading = true;
            $scope.showLoading();
            if($scope.mxE && checkNullEmpty($scope.entity) ){
                $scope.setData(null);
                return;
            }
            var eid, mid;
            if (checkNotNullEmpty($scope.entity)) {
                eid = $scope.entity.id;
            }
            if(checkNotNullEmpty($scope.material)){
                mid = $scope.material.mId;
            }
            trnService.getTransactions(checkNullEmpty(eid)?$scope.etag:"", checkNullEmpty(mid)?$scope.tag:"", formatDate($scope.from), formatDate($scope.to),
                $scope.type, $scope.offset, $scope.size, $scope.bid, $scope.atd, eid, $scope.lEntityId,
                mid,$scope.reason).then(function (data) {
                $scope.setData(data.data);
            }).catch(function error(msg) {
                $scope.setData(null);
                $scope.showErrorMsg(msg);
            });
        };
        $scope.fetch();
        $scope.selectAll = function (newval) {
            for (var item in $scope.filtered) {
                var ty = $scope.filtered[item]['ty'];
                if (ty == 'i' || ty == 'r' || ty == 'w' || ty == 'rt') {
                    $scope.filtered[item]['selected'] = newval;
                }
            }
        };
        $scope.searchBatch = function () {
            if($scope.bid != $scope.batchId){
                $scope.bid = $scope.batchId;
            }
        };
        $scope.undoTransactions = function () {
            var trans = [];
            for (var item in $scope.filtered) {
                if ($scope.filtered[item].selected) {
                    if($scope.currentDomain == $scope.filtered[item].sdid){
                        trans.push($scope.filtered[item].id);
                    }
                }
            }
            if (checkNullEmpty(trans)) {
                $scope.showWarning($scope.resourceBundle['selecttransactionmsg']);
                return;
            }
            if (!confirm($scope.resourceBundle['confirmundotransactionmsg'])) {
                return;
            }
            $scope.showLoading();
            trnService.undoTransactions(trans).then(function (data) {
                $scope.fetch();
                if(data.data.indexOf("Partially Successful") > 0){
                    $scope.showWarning(data.data);
                } else {
                    $scope.showSuccess(data.data)
                }
            }).catch(function error(msg) {
                $scope.showErrorMsg(msg);
            }).finally(function(){
                $scope.hideLoading();
            });
        };
        //to the get actual transaction date check is enabled or not from the inventory config
        domainCfgService.getActualTransDateCheck().then(function(data){
            $scope.hasAtd = data.data;
        });

        domainCfgService.getUniqueTransReasons().then(function(data){
            $scope.reasons = data.data;
        });

        $scope.metadata = [{'title': 'serialnum', 'field': 'sno'}, {'title': 'material', 'field': 'mnm'},
            {'title': 'kiosk', 'field': 'enm'},{'title': 'openingstock', 'field': 'os'}, {'title': 'operation', 'field': 'type'},
            {'title': 'custvend', 'field': 'lknm'}, {'title': 'quantity', 'field': 'q'},
            {'title': 'closingstock', 'field': 'cs'}, {'title': 'updatedon', 'field': 'ts'}, {'title': 'updatedby', 'field': 'unm'}];

        $scope.exRow = [];
        $scope.select = function (index, type) {
            var empty = '';
            if ($scope.exRow.length == 0) {
                for (var i = 0; i < $scope.transactions.results.length; i++) {
                    $scope.exRow.push(empty);
                }
            }
            $scope.exRow[index] = $scope.exRow[index] === type ? empty : type;
        };
        $scope.toggle = function (index) {
            $scope.select(index);
        };

        $scope.updateReason = function(reason){
            $scope.reason = reason;
        };

        $scope.resetFilters = function() {
            if($scope.showEntityFilter) {
                $scope.entity = undefined;
            }
            $scope.material = undefined;
            $scope.type = undefined;
            $scope.from = undefined;
            $scope.to = undefined;
            $scope.etag = undefined;
            $scope.tag = undefined;
            $scope.cust = undefined;
            $scope.vend = undefined;
            $scope.bid = undefined;
            $scope.batchId=$scope.bid;
            $scope.atd=false;
            $scope.reason = undefined;
        }
    }
]);
trnControllers.controller('TransMapCtrl', ['$scope','mapService','uiGmapGoogleMapApi',
    function ($scope,mapService,uiGmapGoogleMapApi) {
        $scope.lmap = angular.copy($scope.map);
        $scope.lmap.options = {scrollwheel: false};
        $scope.lmap.control = {};
        $scope.isToggleMap = checkNotNull($scope.toggleMap);
        if($scope.enMap){
            $scope.ltype = 'e'
        } else if ($scope.type === 'i' || $scope.type === 't' || $scope.type === 'c') {
            $scope.ltype = 'c'
        } else if ($scope.type === 'r') {
            $scope.ltype = 'v'
        }
        $scope.address = '';
        $scope.distance = '';
        $scope.markers = [];
        $scope.loading = true;
        var lCount = 0;
        var latLng;
        // Get the geocodes for the linked kiosk, only if the linked kiosk is geocoded.
        var lklatLng;
        $scope.setMarkers = function () {
            var geocoder = new google.maps.Geocoder();
            $scope.markers = [
                {latitude: $scope.lt, longitude: $scope.ln, id: '0', show: true, add: ''}]; // Add the geo codes of the transaction to markers.
            // Add the lklatLng marker only if it not undefined
            if ( lklatLng ) {
                var lkMarker = {
                    latitude: $scope.lklt,
                    longitude: $scope.lkln,
                    id: '1',
                    show: true,
                    add: '',
                    icon: 'https://www.google.com/intl/en_us/mapfiles/ms/micons/green-dot.png'
                };
                $scope.markers.push(lkMarker);
                mapService.convertLnLt($scope.markers, $scope.lmap);
            }
            function formatAddress(type, add, lt, ln) {
                var address = "<html><b>";
                if (type == "t") {
                    address += "Transaction at: " + add;
                } else if ($scope.enMap) {
                    address += $scope.resourceBundle['kiosk'] + " at: " + add;
                } else {
                    address += ($scope.ltype == 'c' ? $scope.resourceBundle['customer'] + " at: " : $scope.resourceBundle['vendor'] + " at:") + add;
                }
                address += "</b><br>";
                address += lt + "," + ln;
                address += "</html>"
                return address;
            }
            geocoder.geocode({'latLng': latLng}, function (results, status) {
                if (status == google.maps.GeocoderStatus.OK) {
                    if (results[1]) {
                        $scope.address = results[1].formatted_address;
                        $scope.markers[0] = {latitude:$scope.lt,longitude:$scope.ln,id:'0',show:true,add: formatAddress('t',$scope.address,$scope.lt,$scope.ln)};
                    } else if (results[0]) {
                        $scope.address = results[0].formatted_address;
                        $scope.markers[0] = {latitude:$scope.lt,longitude:$scope.ln,id:'0',show:true,add: formatAddress('t',$scope.address,$scope.lt,$scope.ln)};
                    }
                }else if(status == google.maps.GeocoderStatus.ZERO_RESULTS) {
                    $scope.markers[0].show=false;
                }
                mapService.convertLnLt($scope.markers, $scope.lmap);
                loadingCounter();
            });
            // Call geocoder only if lkLatLng is not undefined.
            if ( lklatLng ) {
                geocoder.geocode({'latLng': lklatLng}, function (results, status) {
                    if (status == google.maps.GeocoderStatus.OK) {
                        if (results[1]) {
                            $scope.cAddress = results[1].formatted_address;
                            $scope.markers[1] = {
                                latitude: $scope.lklt,
                                longitude: $scope.lkln,
                                id: '1',
                                show: true,
                                add: formatAddress('c', $scope.cAddress, $scope.lklt, $scope.lkln),
                                icon: 'https://www.google.com/intl/en_us/mapfiles/ms/micons/green-dot.png'
                            };
                        } else if (results[0]) {
                            $scope.cAddress = results[0].formatted_address;
                            $scope.markers[1] = {
                                latitude: $scope.lklt,
                                longitude: $scope.lkln,
                                id: '1',
                                show: true,
                                add: formatAddress('c', $scope.cAddress, $scope.lklt, $scope.lkln),
                                icon: 'https://www.google.com/intl/en_us/mapfiles/ms/micons/green-dot.png'
                            };
                        }
                    } else if (status == google.maps.GeocoderStatus.ZERO_RESULTS) {
                        $scope.markers[1].show = false;
                    }
                    mapService.convertLnLt($scope.markers, $scope.lmap);
                    loadingCounter();
                });
            } else {
                loadingCounter();
            }
        };
        uiGmapGoogleMapApi.then(function(){
            latLng = new google.maps.LatLng($scope.lt, $scope.ln);
            // Get the geocodes for the linked kiosk, only if the linked kiosk is geocoded.
            lklatLng = ( $scope.lklt == 0 && $scope.lkln == 0 ) ? undefined : new google.maps.LatLng($scope.lklt, $scope.lkln);
            $scope.setMarkers();
        });
        $scope.drawRoute = function () {
            // Draw the route only if the lklatLng is not undefined.
            if ( lklatLng ) {
                var directionsDisplay = new google.maps.DirectionsRenderer({suppressMarkers: true});
                directionsDisplay.setMap($scope.lmap.control.getGMap());
                var directionsService = new google.maps.DirectionsService();
                var request = {
                    origin: latLng,
                    destination: lklatLng,
                    travelMode: google.maps.TravelMode.DRIVING
                };
                directionsService.route(request, function (response, status) {
                    if (status == google.maps.DirectionsStatus.OK) {
                        directionsDisplay.setDirections(response);
                        $scope.distance = response.routes[0].legs[0].distance.text;
                    }
                    if (response.routes.length == 0) {
                        var bounds = new google.maps.LatLngBounds();
                        bounds.extend(new google.maps.LatLng($scope.lt, $scope.ln));
                        bounds.extend(new google.maps.LatLng($scope.lklt, $scope.lkln));
                        $scope.lmap.zoom = mapService.getBoundsZoomLevel(bounds, {height: 500, width: 900});
                    }
                    $scope.lmap.zoom -= 1;
                    loadingCounter();
                });
            } else{
                loadingCounter();
            }
        };

        function loadingCounter(){
            if(++lCount == 3){
                $scope.loading = false;
            }
        }
        $scope.$watch('lmap.control.getGMap', function (newVal) {
            if (checkNotNull(newVal)) {
                $scope.drawRoute();
            }
        });
    }
]);
trnControllers.controller('TransactionsFormCtrl', ['$rootScope','$scope', '$uibModal','trnService', 'invService', 'domainCfgService', 'entityService','$timeout',
    function ($rootScope,$scope, $uibModal, trnService, invService, domainCfgService, entityService,$timeout) {
        $scope.invalidPopup = 0;

        $scope.validate = function (material, index, source) {
            if(!material.isBatch && !material.isBinary) {
                material.isVisited = material.isVisited || checkNotNullEmpty(source);
                if(material.isVisited) {
                    if (checkNotNull(material.ind) && checkNullEmpty(material.quantity) || ($scope.transaction.type != 'p' && material.quantity <= 0)) {
                        showPopUP(material, $scope.resourceBundle['invalid.quantity'] + " " + $scope.resourceBundle['for'] + " " + (material.mnm || material.name.mnm), index);
                        return false;
                    }
                    if (checkNotNullEmpty(material.name.huName) && checkNotNullEmpty(material.name.huQty) && checkNotNullEmpty(material.quantity) && material.quantity % material.name.huQty != 0) {
                        showPopUP(material, material.quantity + " of " + material.name.mnm + " does not match the multiples of units expected in " + material.name.huName + ". It should be in multiples of " + material.name.huQty + " " + material.name.mnm + ".", index);
                        return false;
                    }
                }
                if (($scope.transaction.type == 'i' || $scope.transaction.type == 't' || $scope.transaction.type == 'w' ) && material.quantity > material.atpstock) {
                    showPopUP(material, (material.mnm || material.name.mnm) + ' ' + $scope.resourceBundle['quantity'] + ' (' + material.quantity + ') ' + $scope.resourceBundle['cannotexceedstock'] + ' (' + material.atpstock + ')', index);
                    return false;
                }

                if(material.isVisitedStatus) {
                    var status = material.ts ? $scope.tempmatstatus : $scope.matstatus;
                    if(checkNotNullEmpty(status) && checkNullEmpty(material.mst) && $scope.msm) {
                        showPopUP(material, $scope.resourceBundle['status.required'], index, material.ts ? "mt" : "m");
                        return false;
                    }
                }
            }
            return true;
        };

        function showPopUP(mat, msg, index, source) {
            if(checkNullEmpty(source)) {
                $timeout(function () {
                    mat.popupMsg = msg;
                    if (!mat.invalidPopup) {
                        mat.invalidPopup = true;
                        $scope.invalidPopup += 1;
                    }
                    $timeout(function () {
                        source = !mat.isBatch ? "" : "b";
                        $("[id='"+ source + mat.name.mId + index + "']").trigger('showpopup');
                    }, 0);
                }, 0);
            } else {
                $timeout(function () {
                    mat.aPopupMsg = msg;
                    if(!mat.ainvalidPopup) {
                        mat.ainvalidPopup = true;
                        $scope.invalidPopup += 1;
                    }
                    $timeout(function () {
                        $("[id='"+ source + mat.name.mId + index + "']").trigger('showpopup');
                    }, 0);
                }, 0);
            }
        }

        $scope.hidePopup = function(material,index, source){
            if (material.invalidPopup || material.ainvalidPopup) {
                 $scope.invalidPopup = $scope.invalidPopup <= 0 ? 0 : $scope.invalidPopup - 1;
            }
            if(checkNullEmpty(source)) {
                material.invalidPopup = false;
                $timeout(function () {
                    $("[id='"+ material.name.mId + index + "']").trigger('hidepopup');
                }, 0);
            } else if(source == 'm' || source == 'mt') {
                hidePopup($scope, material, source + material.name.mId, index, $timeout, true);
            }
        };

        $scope.transactions_arr = [{value: 'z', displayName: '-- Select Type --', capabilityName: ''},
            {value: 'i', displayName: 'Issues', capabilityName: 'es'},
            {value: 'r', displayName: 'Receipt', capabilityName: 'er'},
            {value: 'p', displayName: 'Stock Count', capabilityName: 'sc'},
            {value: 'w', displayName: 'Discards', capabilityName: 'wa'},
            {value: 't', displayName: 'Transfer', capabilityName: 'ts'}]

        $scope.getAllCapabilities = function () {
            var capabName;
            var i=1;
            if(checkNotNullEmpty($scope.cnff) && checkNotNullEmpty($scope.cnff.tm)){
                while(i<$scope.transactions_arr.length){
                    capabName = $scope.transactions_arr[i].capabilityName;
                    if($scope.cnff.tm.indexOf(capabName) == -1){
                        i++;
                    }else{
                        $scope.transactions_arr.splice(i,1);
                    }
                }
            }
        };

        $scope.getAllCapabilities();

        $scope.offset = 0;
        $scope.size = 50;
        $scope.avMap = {}; //Available Inventory Mapped by material Id.
        $scope.diMap = {};
        $scope.availableInventory = [];
        $scope.tagMaterials = [];
        $scope.stopInvFetch = false;
        $scope.invLoading = false;
        $scope.showDestInv = false;
        $scope.showMinMax = true;
        $scope.today = formatDate2Url(new Date());
        $scope.minDate = new Date();
        $scope.minDate.setMonth($scope.minDate.getMonth() - 3);

        $scope.changeEntity = function(){
            var ent = $scope.transaction.ent;
            resetNoConfirm();
            $scope.transaction.ent = ent;
            $scope.transaction.eent = true;

            if (checkNotNullEmpty($scope.transaction.ent)){
                $scope.loadMaterials = true;
                $scope.getInventory();
                fetchTransConfig();
                $scope.transaction.type = $scope.transactions_arr[0].value;
            }
        };

        $scope.$watch("showDestInv",function(newVal,oldVal){
            if(newVal){
                $scope.getDestInventory();
            }
        });

        $scope.checkPermission = function(kiosk) {
            $scope.showLoading();
            trnService.getPermission($scope.curUser,kiosk.eid,$scope.transactionType).then(function(data) {
                $scope.showDestInvPermission = data.data;
            }).catch(function error(msg){
                $scope.showErrorMsg(msg);
            }).finally(function(){
                $scope.hideLoading();
            });
        };

        function fetchTransConfig() {
            if (checkNotNullEmpty($scope.transaction.ent)) {
                $scope.showLoading();
                trnService.getTransDomainConfig($scope.transaction.ent.id).then(function (data) {
                    $scope.tranDomainConfig = data.data;
                    $scope.trans = $scope.tranDomainConfig.dest;
                }).catch(function error(msg) {
                    $scope.showErrorMsg(msg);
                }).finally(function(){
                    $scope.hideLoading();
                });
            }
        }
        $scope.showLoading();
        trnService.getStatusMandatory().then(function(data) {
            $scope.statusData = data.data;
        }).catch(function error(msg) {
            $scope.showErrorMsg(msg);
        }).finally(function() {
            $scope.hideLoading();
        });
        $scope.getInventory = function () {
            if (checkNotNullEmpty($scope.transaction.ent)) {
                $scope.showLoading();
                var entId = $scope.transaction.ent.id;
                invService.getInventory(entId, null, $scope.offset, $scope.size).then(function (data) {
                    if($scope.stopInvFetch || checkNullEmpty($scope.transaction.ent) || entId != $scope.transaction.ent.id) {
                        return;
                    }
                    var inventory = data.data.results;
                    if(checkNotNullEmpty(inventory) && inventory.length > 0){
                        $scope.availableInventory = $scope.availableInventory.concat(inventory);
                    }
                    if(!$scope.stopInvFetch && checkNotNullEmpty(inventory) && inventory.length == $scope.size){
                        $scope.offset += $scope.size;
                        $scope.getInventory();
                    }else{
                        $scope.loadMaterials = false;
                    }
                    inventory.forEach(function (inv) {
                        inv.tgs.forEach(function (tag) {
                            if (checkNullEmpty($scope.tagMaterials[tag])) {
                                $scope.tagMaterials[tag] = [];
                            }
                            $scope.tagMaterials[tag].push(inv.mId);
                        });

                        $scope.avMap[inv.mId] = inv;

                    });
                    for (var i in $scope.transaction.materials) {
                        var mat = $scope.transaction.materials[i];
                        if(checkNotNullEmpty(mat.name) && checkNotNullEmpty($scope.avMap[mat.name.mId])){
                            $scope.avMap[mat.name.mId].hide = true;
                        }
                    }
                    $scope.loadDInventory(inventory);
                    $scope.invLoading = false;

                }).catch(function error(msg) {
                    $scope.showErrorMsg(msg);
                    $scope.loadMaterials = false;
                    $scope.invLoading = false;
                }).finally(function(){
                    $scope.hideLoading();
                });
            }
        };
        $scope.hasStock = function(data, type) {
            if(checkNotNullEmpty(data)) {
                return !(data.stk == 0 && (type == 'i' || type == 'w' || type == 't'));
            }
        };
        $scope.setTrans = function(){
            if(checkNullEmpty($scope.transaction.entityType)){
                $scope.trans = $scope.tranDomainConfig.dest;
            }else if($scope.transaction.entityType == 'c'){
                $scope.trans = $scope.tranDomainConfig.customers;
            }else if($scope.transaction.entityType == 'v'){
                $scope.trans = $scope.tranDomainConfig.vendors;
            }
        };
        $scope.entityType = "";
        $scope.update = function () {
            if ($scope.timestamp == undefined) {
                $scope.timestamp = new Date().getTime();
            }
            if($scope.atd == '2' && checkNullEmpty($scope.transaction.date)){
                $scope.showWarning($scope.resourceBundle['trn.atd.mandatory']);
                return true;
            }
            var index = 0;
            var invalidQuantity = $scope.transaction.materials.some(function (mat) {
                if(checkNotNullEmpty($scope.transaction.dent) && $scope.transaction.ent.nm === $scope.transaction.dent.enm){
                    $scope.showWarning($scope.resourceBundle['form.error']);
                    return true;
                }
                if(checkNotNullEmpty($scope.exRow[index])){
                    $scope.showWarning($scope.resourceBundle['trn.batch.open'] + ' ' + $scope.transaction.materials[index].name.mnm + '. ' + $scope.resourceBundle['trn.batch.save.cancel']);
                    return true;
                }
                if (!mat.isBatch && !mat.isBinary) {
                    if(!$scope.validate(mat,index,'b')) {
                        return true;
                    }
                }else if(mat.isBatch){
                    var val = mat.bquantity.some(function(bmat){
                        if(checkNotNullEmpty(bmat.quantity)){
                            return true;
                        }
                    });
                    if(!val) {
                        $scope.showWarning($scope.resourceBundle['invalid.quantity'] + " " + $scope.resourceBundle['of'] + " " + (mat.mnm || mat.name.mnm));
                        return true;
                    }
                }
                index+=1;
            });
            var isStatusEmpty = false;
            index = 0;
            $scope.transaction.materials.forEach(function (mat) {
                if(checkNotNullEmpty(mat.name) && !mat.isBatch && !mat.isBinary && mat.quantity != "0") {
                    var status = mat.ts ? $scope.tempmatstatus : $scope.matstatus;
                    if (checkNotNullEmpty(status) && checkNullEmpty(mat.mst) && $scope.msm) {
                        mat.isVisitedStatus = true;
                        !$scope.validate(mat, index, 's');
                        isStatusEmpty = true;
                    }
                }
                index+=1;
            });
            if (!invalidQuantity && !isStatusEmpty) {
                var fTransaction = constructFinalTransaction();
                $scope.showLoading();

                trnService.updateTransaction(fTransaction).then(function (data) {
                    resetNoConfirm(true);
                    if (data.data.indexOf("One or more errors") == 1) {
                        $scope.showWarning(data.data);

                    } else {
                        $scope.showSuccess(data.data);
                    }
                }).catch(function error(msg) {
                    if(msg.status == 504 || msg.status == 404) {
                        // Allow resubmit or cancel.
                        handleTimeout();
                    } else {
                        resetNoConfirm(true);
                        $scope.showErrorMsg(msg);
                    }
                }).finally(function(){
                    $scope.hideLoading();
                });
            }
        };

        function handleTimeout() {
            $scope.modalInstance = $uibModal.open({
                template: '<div class="modal-header ws">' +
                '<h3 class="modal-title">{{resourceBundle["connection.timedout"]}}</h3>' +
                '</div>' +
                '<div class="modal-body ws">' +
                '<p>{{resourceBundle["connection.timedout"]}}.</p>' +
                '</div>' +
                '<div class="modal-footer ws">' +
                '<button class="btn btn-primary" ng-click="update();cancel(false)">{{resourceBundle.resubmit}}</button>' +
                '<button class="btn btn-default" ng-click="cancel(true)">{{resourceBundle.cancel}}</button>' +
                '</div>',
                scope: $scope
            });
        }
        $scope.cancel = function (callReset) {
            if(callReset) {
                resetNoConfirm();
            }
            $scope.modalInstance.dismiss('cancel');
        };

        function constructFinalTransaction() {
            var ft = {};
            ft['kioskid'] = '' + $scope.transaction.ent.id;
            ft['transtype'] = $scope.transaction.type;
            if(checkNotNullEmpty($scope.transaction.dent)) {
                ft['lkioskid'] = '' + $scope.transaction.dent.eid;
            }
            //ft['reason'] = $scope.transaction.reason;
            if(checkNotNullEmpty($scope.transaction.date)) {
                ft['transactual'] = '' + formatDate($scope.transaction.date);
            }
            ft['materials'] = {};
            ft['bmaterials'] = {};
            $scope.transaction.materials.forEach(function (mat) {
                if (mat.isBatch) {
                    mat.bquantity.forEach(function (m) {
                        if (checkNotNullEmpty(m.quantity)) {
                            ft['bmaterials'][m.mId + "\t" + m.bid] = {
                                q: '' + m.quantity,
                                e: m.bexp,
                                mr: m.bmfnm,
                                md: m.bmfdt,
                                r : mat.reason,
                                mst : m.mst
                            };
                        }
                    });
                } else if (mat.isBinary) {
                    ft['materials'][mat.name.mId] = "1";
                } else if (checkNotNull(mat.ind)) {
                    ft['materials'][mat.name.mId] = {q:''+ mat.quantity,
                        r :mat.reason, mst: mat.mst};
                }
            });
            ft['signature'] = $scope.curUser + $scope.timestamp;
            return ft;
        }
        $scope.$watch('transaction.type', function (newVal) {
            if(newVal==$scope.transactions_arr[0].value){return;}
            $scope.entityType = "";
            $scope.validType = checkNotNullEmpty(newVal);
            $scope.reasons = [];
            if (checkNotNullEmpty($scope.transaction.type)) {
                if(checkNotNullEmpty($scope.tranDomainConfig.reasons)) {
                    $scope.reasons = $scope.tranDomainConfig.reasons[$scope.transaction.type];
                    if(checkNotNullEmpty($scope.reasons))
                    {
                        if($scope.reasons.length>0)
                            $scope.showReason=true;
                    }
                }
            }
            if (newVal == 'i' && $scope.tranDomainConfig.noc > 0) {
                $scope.entityType = $scope.resourceBundle['customer'];
            } else if (newVal == 'r' && $scope.tranDomainConfig.nov > 0) {
                $scope.entityType = $scope.resourceBundle['vendor'];
            } else if (newVal == 't') {
                $scope.entityType = "Transfer to";
            }
            if(newVal == 'i'){
                $scope.atd = $scope.tranDomainConfig.atdi;
                $scope.msm = $scope.statusData.ism;
            }else if(newVal =='r'){
                $scope.atd = $scope.tranDomainConfig.atdr;
                $scope.msm = $scope.statusData.rsm;
            }else if(newVal =='p'){
                $scope.atd = $scope.tranDomainConfig.atdp;
                $scope.msm = $scope.statusData.psm;
            }else if(newVal =='w'){
                $scope.atd = $scope.tranDomainConfig.atdw;
                $scope.msm = $scope.statusData.wsm;
            }else if(newVal =='t'){
                $scope.atd = $scope.tranDomainConfig.atdt;
                $scope.msm = $scope.statusData.tsm;
            }
            $scope.showMaterials = true;
            $scope.showLoading();
            trnService.getMatStatus($scope.transaction.type, false).then(function (data) {
                $scope.matstatus = data.data;
            }).catch(function error(msg) {
                $scope.showErrorMsg(msg);
            }).finally(function() {
                $scope.hideLoading();
            });
            $scope.showLoading();
            trnService.getMatStatus($scope.transaction.type, true).then(function(data) {
                $scope.tempmatstatus = data.data;
            }).catch(function error(msg) {
                $scope.showErrorMsg(msg);
            }).finally(function() {
                $scope.hideLoading();
            });
        });
        $scope.$watch('mtag', function (name, oldVal) {
            $scope.availableInventory.forEach(function (inv) {
                inv.tHide = checkNotNullEmpty(name) && (checkNullEmpty($scope.tagMaterials[name]) || !($scope.tagMaterials[name].indexOf(inv.mId) >= 0));
            });
        });
        $scope.deleteRow = function (id) {
            var mIndex = $scope.transaction.materials[id].name.mId;
            if (checkNotNullEmpty(mIndex) && checkNotNullEmpty($scope.avMap[mIndex])) {
                $scope.avMap[mIndex].hide = false;
            }
            $scope.exRow.splice(id,1);
            $scope.hidePopup($scope.transaction.materials[id],id);
            $scope.transaction.materials.splice(id, 1);
            redrawPopup('hide');
        };
        $scope.addRows = function () {
            $scope.transaction.materials.push({"name": "", "stock": ""});
        };
        function resetMaterials() {
            $scope.transaction.materials = [];
            $scope.exRow = [];
            $scope.availableInventory.forEach(function (inv) {
                inv.hide = false;
            });
            $scope.dInv = false;
            $scope.addRows();
        }
        $scope.reset = function (type) {
            var proceed = false;
            if (type === "en") {
                if ($scope.transaction.materials.length == 1 || window.confirm($scope.resourceBundle['entity.editconfirm'])) {
                    proceed = true;
                }
            } else if (type == "ty") {
                if ($scope.transaction.materials.length == 1 || window.confirm($scope.resourceBundle['material.editconfirm'])) {
                    $scope.validType = false;
                    resetMaterials();
                    $scope.transaction.type = $scope.transactions_arr[0].value;
                    $scope.entityType = "";
                    $scope.mtag = "";
                    $scope.atd=undefined;
                    $scope.showReason = false;
                    $scope.invalidPopup = 0;
                }
                return;
            } else {
                if (window.confirm($scope.resourceBundle['clear.all'])) {
                    proceed = true;
                }
            }
            if (proceed) {
                resetNoConfirm();
            }
        };
        function resetNoConfirm(isAdd) {
            $scope.offset = 0;
            $scope.transaction = {};
            $scope.transaction.materials = [];
            $scope.availableInventory = [];
            $scope.avMap = {};
            $scope.tagMaterials = [];
            $scope.entityType = "";
            $scope.showMaterials = false;
            $scope.showDestInv = false;
            $scope.loadMaterials = false;
            $scope.validType = false;
            $scope.exRow = [];
            $scope.submitted = false;
            $scope.dInv = false;
            $scope.diMap = {};
            $scope.tranDomainConfig={};
            $scope.atd=undefined;
            $scope.mtag = "";
            $scope.showReason = false;
            $scope.timestamp = undefined;
            $scope.invalidPopup = 0;
            $scope.transaction.type = $scope.transactions_arr[0].value;
            $scope.addRows();
            if($scope.isEnt) {
                $scope.transaction.ent = $scope.entity;
                if (checkNotNullEmpty($scope.transaction.ent)){
                    $scope.transaction.eent = true;
                    $scope.loadMaterials = true;
                    $scope.getInventory();
                    fetchTransConfig();
                }
            } else if (checkNotNullEmpty($scope.defaultEntityId) && isAdd) {
                entityService.get($scope.defaultEntityId).then(function(data){
                    $scope.transaction.ent = data.data;
                    if (checkNotNullEmpty($scope.transaction.ent)){
                        $scope.transaction.eent = true;
                        $scope.loadMaterials = true;
                        $scope.getInventory();
                        fetchTransConfig();
                    }
                });
            }
        }
        resetNoConfirm(true);
        $scope.getAllCapabilities();

        $scope.getFilteredEntity = function (text) {
            $scope.loadingEntity = true;
            return entityService.getFilteredEntity(text.toLowerCase()).then(function (data) {
                $scope.loadingEntity = false;
                return data.data.results;
            }).finally(function(){
                $scope.loadingEntity = false;
            });
        };
        $scope.getFilteredInvntry = function (text) {
            $scope.fetchingInvntry = true;
            return invService.getInventoryStartsWith($scope.transaction.ent.id,null,text.toLowerCase(),0,10).then(function (data) {
                $scope.fetchingInvntry = false;
                var list = [];
                var map = {};
                for (var i in $scope.transaction.materials) {
                    var mat = $scope.transaction.materials[i];
                    if(checkNotNullEmpty(mat.name)){
                        map[mat.name.mId] = true;
                    }
                }
                if(checkNotNullEmpty(data.data.results)){
                    var trntype = false;
                    if($scope.transaction.type == 'i' || $scope.transaction.type == 'w' || $scope.transaction.type == 't') {
                        trntype = true;
                    }
                    for(var j in data.data.results) {
                        var mat = data.data.results[j];
                        if(!map[mat.mId] && !(trntype && mat.atpstk == 0)){
                            list.push(mat);
                        }
                    }
                }
                return list;
            }).finally(function(){
                $scope.fetchingInvntry = false;
            });
        };
        $scope.addMaterialToList = function (index) {
            if ($scope.validType) {
                var li = $scope.transaction.materials.length - 1;
                var newMat = {
                    "name": $scope.availableInventory[index]
                };
                if (li != $scope.transaction.materials.length) {
                    $scope.transaction.materials[li] = newMat;
                } else {
                    $scope.transaction.materials.push(newMat);
                }
                $scope.availableInventory[index].hide = true;
            }
        };
        $scope.getDestInventory = function () {
            if (checkNullEmpty($scope.dInventory)) {
                $scope.dInvLoading = true;
                $scope.showLoading();
                invService.getInventory($scope.transaction.dent.eid, null,0,999).then(function (data) {
                    $scope.dInventory = data.data.results;
                    $scope.dInv = !$scope.dInv;
                    $scope.diMap = [];
                    $scope.dInventory.forEach(function (di) {
                        $scope.diMap[di.mId] = di;
                    });
                    $scope.loadDInventory($scope.availableInventory);
                    $scope.loadDInventory($scope.transaction.materials,true);
                }).catch(function error(msg) {
                    $scope.showErrorMsg(msg);
                }).finally(function(){
                    $scope.hideLoading();
                });
            }
            if ($scope.dInvLoading == false) {
                $scope.dInv = !$scope.dInv;
            }
            $scope.dInvLoading = false;
        };
        $scope.loadDInventory = function (inventory,isTransMat) {
            inventory.forEach(function (inv) {
                var mid = isTransMat ? inv.name.mId : inv.mId;
                if (!isTransMat || checkNotNullEmpty(inv.name)) {
                    if (checkNotNullEmpty($scope.diMap[mid])) {
                        inv.dInv = $scope.diMap[mid];
                    } else {
                        inv.dInv = {};
                        inv.dInv.stk = "N/A";
                        inv.dInv.event = "-1";
                    }
                    inv.dStock = inv.dInv.stk;
                    inv.devent = inv.dInv.event;
                    if (inv.dInv.stk === "N/A") {
                        inv.dmm = "";
                    } else {
                        inv.dmm = "(" + inv.dInv.reord + ',' + inv.dInv.max + ")";
                    }
                }
            });
        };
        $scope.getAvailableInventoryID = function () {
            var mIds = [];
            $scope.availableInventory.forEach(function (inv) {
                mIds.push(inv.mId);
            });
            return mIds;
        };
        $scope.exRow = [];
        $scope.select = function (index, type) {
            var empty = "";
            if ($scope.exRow.length == 0) {
                for (var i = 0; i < $scope.transaction.materials.length; i++) {
                    $scope.exRow.push(empty);
                }
            }
            $scope.exRow[index] = $scope.exRow[index] === type ? empty : type;
            redrawPopup('hide');
        };

        function redrawPopup(type, source) {
            if (type == 'hide') {
                for (var i = 0; i < $scope.transaction.materials.length - 1; i++) {
                    $scope.hidePopup($scope.transaction.materials[i], i);
                }
                $timeout(function () {
                    redrawPopup('show',source);
                }, 40);
            } else {
                for (i = 0; i < $scope.transaction.materials.length - 1; i++) {
                    $scope.validate($scope.transaction.materials[i], i,source);
                }
            }
        }

        $scope.selectOnType = function (index) {
            var type;
            if($scope.transaction.type === 'p') {
                type = 'stock';
            } else if($scope.transaction.type === 'r') {
                type = 'add';
            }else if($scope.transaction.type === 'i' || $scope.transaction.type === 'w' || $scope.transaction.type === 't') {
                type = 'show';
            }
            $scope.select(index,type);
        };
        $scope.toggle = function (index) {
            $scope.select(index);
        };
        domainCfgService.getMaterialTagsCfg().then(function (data) {
            $scope.tags = data.data.tags;
        }).catch(function error(msg) {
            $scope.showErrorMsg(msg);
        });
        $scope.$on('$destroy', function cleanup() {
            $scope.stopInvFetch = true;
        });
    }
]);
trnControllers.controller('transactions.MaterialController', ['$scope','trnService',
    function ($scope,trnService) {
        $scope.updateBatch = function (batchDet) {
            $scope.material.bquantity = batchDet;
            countTotalBatchQuantities(batchDet);
        };
        $scope.updateNewBatch = function (batchNewDet) {
            $scope.material.bnquantity = batchNewDet;
        };
        function countTotalBatchQuantities(batchDet) {
            var count = 0;
            if(checkNotNullEmpty(batchDet)) {
                batchDet.forEach(function (m) {
                    if (checkNotNullEmpty(m.quantity)) {
                        count = count + parseInt(m.quantity);
                    }
                });
            }
            $scope.material.bquantities = count;
        }
        $scope.$watch('material.name', function (name, oldVal) {
            if (checkNotNull(name) && checkNotNull(name.atpstk)) {
                if(checkNotNullEmpty($scope.avMap[name.mId])){
                    name = $scope.avMap[name.mId];
                    name.hide = true;
                }
                $scope.material.stock = name.stk;
                $scope.material.atpstock = name.atpstk;
                $scope.material.tstk = name.tstk;
                $scope.material.ind = name.sno - 1;
                $scope.material.event = name.event;
                $scope.material.ts = name.ts;
                if (checkNotNull(name.dInv)) {
                    $scope.material.dStock = name.dInv.stk;
                    $scope.material.devent = name.dInv.event;
                    if (name.dInv.stk === "N/A") {
                        $scope.material.dmm = "";
                    } else {
                        $scope.material.dmm = "(" + name.dInv.reord + ',' + name.dInv.max + ")";
                    }
                }
                $scope.material.isBatch = name.be;
                if (name.be) {
                    $scope.material.bquantity = [];
                }
                $scope.material.mm = "(" + name.reord.toFixed(0) + ',' + name.max.toFixed(0) + ")";
                $scope.material.isBinary = name.b === 'bn';
                $scope.material.reason = '';
                if(checkNotNullEmpty($scope.transaction.type)){
                    if(checkNotNullEmpty(name.tgs)) {
                        trnService.getReasons($scope.transaction.type, name.tgs).then(function (data) {
                            $scope.material.rsns = data.data;
                            if (checkNotNullEmpty($scope.material.rsns) && $scope.material.rsns.length > 0) {
                                $scope.material.reason = $scope.material.rsns[0];
                                $scope.$parent.showReason = true;
                            } else if (checkNotNullEmpty($scope.reasons) && $scope.reasons.length > 0) {
                                $scope.material.reason = $scope.reasons[0];
                            }
                        }).catch(function error(msg) {
                            $scope.showErrorMsg(msg);
                        });
                    } else if (checkNotNullEmpty($scope.reasons) &&  $scope.reasons.length > 0) {
                        $scope.material.reason = $scope.reasons[0];
                    }
                }

                if(checkNotNullEmpty($scope.transaction.type)) {
                    if(name.ts) {
                        $scope.material.mst = checkNotNullEmpty($scope.tempmatstatus) ? $scope.tempmatstatus[0] : undefined;
                    } else {
                        $scope.material.mst = checkNotNullEmpty($scope.matstatus) ? $scope.matstatus[0] : undefined;
                    }
                }
                $scope.addRows();
            }
        });
        $scope.filterBatchMat = function(bmat){
            var fBatMat = [];
            if(checkNotNullEmpty(bmat)) {
                bmat.forEach(function (m) {
                    if (m.quantity !== undefined && m.quantity !== '') {
                        fBatMat.push(m);
                    }
                });
            }
            return fBatMat;
        }
    }
]);
trnControllers.controller('BatchTransactionCtrl', ['$scope', 'invService','$timeout',
    function ($scope, invService,$timeout) {
        $scope.invalidPopup = 0;
        var displayStatus;
        $scope.togglePopup = function (batch, index, forceEvent, source) {
            $timeout(function () {
                var skipTrigger = false;
                if (forceEvent) {
                    var eventName = forceEvent
                } else {
                    var avStock = batch.atpstk + (batch.oastk || 0);
                    if(checkNotNullEmpty($scope.obts)) {
                        $scope.obts.some(function (obts) {
                            if (obts.id == batch.bid) {
                                avStock += obts.q * 1;
                                return true;
                            }
                        });
                    }
                    if (checkNotNullEmpty(batch.bid) && (batch.quantity === "0" || batch.quantity > avStock)) {
                        eventName = 'showpopup';
                        if (batch.quantity > avStock) {
                            showPopUP(batch, $scope.resourceBundle['quantity.exceed'], index);
                        } else {
                            showPopUP(batch, "Quantity is invalid for " + batch.bid, index);
                        }
                        skipTrigger = true;
                    } else if (checkNotNullEmpty($scope.huName) && checkNotNullEmpty($scope.huQty) && checkNotNullEmpty(batch.bid) && checkNotNullEmpty(batch.quantity) && batch.quantity % $scope.huQty != 0) {
                        eventName = 'showpopup';
                        showPopUP(batch, batch.quantity + " of " + batch.bid + " does not match the multiples of units expected in " + $scope.huName + ". It should be in multiples of " + $scope.huQty + " " + $scope.mnm + ".", index);
                        skipTrigger = true;
                    } else {
                        if (batch.quantity > 0 && batch.isVisitedStatus && checkNotNullEmpty(displayStatus) && checkNullEmpty(batch.mst) && $scope.msm) {
                            showPopUP(batch, $scope.resourceBundle['status.required'], index, $scope.material.ts ? "btmt" : "btm");
                            skipTrigger = true;
                        } else {
                            batch.ainvalidPopup = false;
                        }
                        eventName = 'hidepopup';
                        if (!skipTrigger || (batch.invalidPopup == undefined || batch.invalidPopup)) {
                            $scope.invalidPopup = $scope.invalidPopup <= 0 ? 0 : $scope.invalidPopup - 1;
                        }
                        batch.invalidPopup = false;
                    }
                }
                if(!skipTrigger) {
                    source = source || "b";
                    $timeout(function () {
                        $("[id='"+ source + $scope.mid + index + "']").trigger(eventName);
                    }, 0);
                }
            }, 0);
        };

        function showPopUP(mat, msg, index, source) {
            $timeout(function () {
                if(checkNullEmpty(source)) {
                    mat.popupMsg = msg;
                    if (!mat.invalidPopup) {
                        mat.invalidPopup = true;
                        $scope.invalidPopup += 1;
                    }
                    source = "b";
                } else {
                    mat.aPopupMsg = msg;
                    if (!mat.ainvalidPopup) {
                        mat.ainvalidPopup = true;
                        $scope.invalidPopup += 1;
                    }
                }
                $timeout(function () {
                    $("[id='"+ source + $scope.mid + index + "']").trigger('showpopup');
                }, 0);
            }, 0);
        }

        $scope.loading = true;
        $scope.updateQuantity = function () {
            var remq = $scope.allocq || 0;
            if (checkNotNullEmpty($scope.exBatches)) {
                var batMap = {};
                $scope.batchDet.forEach(function (batch) {
                    batMap[batch.bid] = batch;
                });
                $scope.exBatches.forEach(function (exBatch) {
                    var batch = batMap[exBatch.id];
                    if (checkNotNull(batch)) {
                        batch.quantity = exBatch.q;
                        remq = remq - batch.quantity;
                    }
                });
            }
            if (checkNotNullEmpty($scope.allocq)) {
                $scope.batchDet.some(function (batch) {
                    if (checkNotNullEmpty(batch.quantity)) {
                        return true;
                    }
                    batch.quantity = Math.min(batch.atpstk, remq);
                    remq = remq - batch.quantity;
                    return remq <= 0;
                });
            }
        };
        $scope.updateMaterialStatus = function() {
            $scope.batchDet.forEach(function(det) {
                var setStatus = $scope.exBatches.some(function (data) {
                    if(data.id == det.bid) {
                        det.mst = data.mst || data.smst || status;
                        return true;
                    }
                });
                if(!setStatus) {
                    det.mst = status;
                }
            });
        };
        var status;
        if($scope.material.ts) {
            status = checkNotNullEmpty($scope.tempmatstatus) ? $scope.tempmatstatus[0] : undefined;
            displayStatus = $scope.tempmatstatus;
        } else {
            status = checkNotNullEmpty($scope.matstatus) ? $scope.matstatus[0] : undefined;
            displayStatus = $scope.matstatus;
        }
        if (checkNullEmpty($scope.bdata)) {
            $scope.batchDet = [];
            if(checkNotNullEmpty($scope.kid)) {
                $scope.showLoading();
                invService.getBatchDetail($scope.mid, $scope.kid, undefined,$scope.shipOrderId).then(function (data) {
                    $scope.batchDet = data.data;
                    if (checkNotNullEmpty($scope.batchDet)) {
                        $scope.updateQuantity();
                        $scope.batchDet.forEach(function (det) {
                            det.bexp = formatDate(new Date(det.bexp));
                            if (checkNotNullEmpty(det.bmfdt)) {
                                det.bmfdt = formatDate(new Date(det.bmfdt));
                            }
                            det.mId = $scope.mid;
                        });
                    }
                    if(checkNotNullEmpty($scope.exBatches)) {
                        $scope.updateMaterialStatus();
                    } else {
                        $scope.batchDet.forEach(function (data) {
                            data.mst = status;
                        });
                    }
                    $scope.loading = false;
                    $scope.hideLoading();
                }).catch(function error(msg) {
                    $scope.loading = false;
                    $scope.hideLoading();
                    $scope.showErrorMsg(msg);
                });
            }
        } else {
            $scope.batchDet = angular.copy($scope.bdata);
            $scope.updateQuantity();
            $scope.loading = false;
            $scope.hideLoading();
        }
        $scope.saveBatchTrans = function () {
            if($scope.type !== 'p') {
                var index = 0;
                var isValidQuantity = false;
                var invalidQuantity = $scope.batchDet.some(function (det) {
                    if(checkNotNullEmpty(det.quantity) && det.quantity > 0) {
                        if (det.q < det.quantity) {
                            showPopUP(det, $scope.resourceBundle['quantity.exceed'], index);
                            return true;
                        }
                        if ($scope.huQty && det.quantity % $scope.huQty != 0) {
                            det.isVisitedQuantity = true;
                            $scope.togglePopup(det, index);
                            return true;
                        }
                        isValidQuantity = true;

                    }
                    index+=1;
                });
                /*if(!isValidQuantity) {
                    $scope.showWarning($scope.resourceBundle['valid.quantity'] +" " + $scope.mnm);
                    return;
                }*/
                var isStatusEmpty = false;
                index = 0;
                $scope.batchDet.forEach(function (data) {
                    if(data.quantity > 0 && checkNotNullEmpty(displayStatus) && checkNullEmpty(data.mst) && $scope.msm) {
                        data.isVisitedStatus = true;
                        $scope.togglePopup(data,index);
                        isStatusEmpty = true;
                    }
                    index+=1;
                });
                if (invalidQuantity || isStatusEmpty) {
                    return;
                }
                var foundLast = false;
                var isInvalid = $scope.batchDet.some(function (det) {
                    if (foundLast && det.quantity > 0) {
                        return true;
                    } else if (det.atpstk > 0 && (checkNullEmpty(det.quantity) || det.quantity < det.atpstk)) {
                        foundLast = true;
                    }
                });
                var proceed = true;
                if (isInvalid && $scope.type != 'w') {
                    if (!confirm($scope.resourceBundle['batches.notallocated'])) {
                        proceed = false;
                    }
                }
                /*if (proceed && checkNotNull($scope.allocq)) {
                    var allocated = 0;
                    $scope.batchDet.forEach(function (bItem) {
                        if (checkNotNullEmpty(bItem.quantity)) {
                            allocated = allocated + parseInt(bItem.quantity);
                        }
                    });
                    if (allocated != $scope.allocq) {
                        if (!confirm($scope.resourceBundle['quantity.allocated'] + " '" + allocated + "'  " + $scope.resourceBundle['quantity.unmatch'] + " '" + $scope.item.q + "' " + "." + $scope.resourceBundle['proceed'])) {
                            proceed = false;
                        }
                    }
                }*/
            }
            if (proceed || $scope.type == 'p') {
                $scope.updateBatch(angular.copy($scope.batchDet));
                $scope.toggle($scope.$index);
            }
        };
    }
]);
trnControllers.controller('AddBatchTransactionCtrl', ['$scope','$timeout',
    function ($scope,$timeout) {

        $scope.invalidPopup = 0;
        var displayStatus;
        $scope.togglePopup = function (batch, index, forceEvent, source) {
            $timeout(function () {
                var skipTrigger = false;
                if (forceEvent) {
                    var eventName = forceEvent
                } else if (checkNotNullEmpty($scope.huName) && checkNotNullEmpty($scope.huQty) && checkNotNullEmpty(batch.bid) && checkNotNullEmpty(batch.quantity) && batch.quantity % $scope.huQty != 0) {
                    eventName = 'showpopup';
                    showPopUP(batch, batch.quantity + " of " + batch.bid + " does not match the multiples of units expected in " + $scope.huName + ". It should be in multiples of " + $scope.huQty + " " + $scope.mnm + ".", index);
                    skipTrigger = true;
                } else {
                    if (batch.quantity > 0 && batch.isVisitedStatus && checkNotNullEmpty(displayStatus) && checkNullEmpty(batch.mst) && $scope.msm) {
                        showPopUP(batch, $scope.resourceBundle['status.required'], index, $scope.material.ts ? "abtmt" : "abtm");
                        skipTrigger = true;
                    } else {
                        batch.ainvalidPopup = false;
                    }
                    eventName = 'hidepopup';
                    batch.invalidPopup = false;
                    if(!skipTrigger) {
                        $scope.invalidPopup = $scope.invalidPopup <= 0 ? 0 : $scope.invalidPopup - 1;
                    }
                }
                if(!skipTrigger) {
                    source = source || "b";
                    $timeout(function () {
                        $("[id='"+ source + $scope.mid + index + "']").trigger('showpopup');
                    }, 0);
                }
            }, 0);
        };

        function showPopUP(mat, msg, index, source) {
            $timeout(function () {
                if(checkNullEmpty(source)) {
                    mat.popupMsg = msg;
                    if (!mat.invalidPopup) {
                        mat.invalidPopup = true;
                        $scope.invalidPopup += 1;
                    }
                    source = "b";
                } else {
                    mat.aPopupMsg = msg;
                    if (!mat.ainvalidPopup) {
                        mat.ainvalidPopup = true;
                        $scope.invalidPopup += 1;
                    }
                }
                $timeout(function () {
                    $("[id='"+ source + $scope.mid + index + "']").trigger('showpopup');
                }, 0);
            }, 0);
        }

        $scope.exists = [];
        $scope.currentBatches = [];
        var status = "";
        if($scope.material.ts) {
            status = checkNotNullEmpty($scope.tempmatstatus) ? $scope.tempmatstatus[0] : undefined;
            displayStatus = $scope.tempmatstatus;
        } else {
            status = checkNotNullEmpty($scope.matstatus) ? $scope.matstatus[0] : undefined;
            displayStatus = $scope.matstatus;
        }
        $scope.addRow = function () {
            $scope.batchDet.push({mst: status});
        };
        if (checkNotNullEmpty($scope.bdata)) {
            $scope.batchDet = angular.copy($scope.bdata);
            var index=0;
            $scope.batchDet.forEach(function (det) {
                if(checkNotNullEmpty(det)){
                    if(!angular.isDate(det.bexp)){
                        det.bexp = string2Date(det.bexp, 'dd/mm/yyyy', '/');
                    }
                    if(!angular.isDate(det.bmfdt)) {
                        det.bmfdt = string2Date(det.bmfdt, 'dd/mm/yyyy', '/');
                    }
                }
                $scope.currentBatches[index++] = det.bid;
            });
        } else {
            $scope.batchDet = [];
            $scope.addRow();
        }


        $scope.checkCurrentBatches = function(data, index) {
            $scope.exists[index] = false;
            if($scope.currentBatches.length > 0) {
                for( var i=0; i<$scope.currentBatches.length; i++) {
                    if(i != index && $scope.currentBatches[i] == data) {
                        $scope.exists[index] = true;
                    }
                }
            }
            $scope.currentBatches[index] = data;
        };
        $scope.checkBatchExists = function(data, index) {
            if(checkNotNullEmpty(data)) {
                $scope.checkCurrentBatches(data, index);
            }
        };


        $scope.saveBatchTrans = function () {
            var isDup = $scope.exists.some(function (val){
               if(val){
                   $scope.showWarning($scope.resourceBundle['bid.dup']);
                   return true;
               }
            });
            if(isDup){
                return false;
            }
            var index = 0;
            var isMissing = $scope.batchDet.some(function (det) {
                if (checkNullEmpty(det.bid) || checkNullEmpty(det.bexp) ||
                    checkNullEmpty(det.bmfnm) || checkNullEmpty(det.quantity)) {
                    $scope.showWarning($scope.resourceBundle['fields.missing']);
                    return true;
                }
                if ($scope.transaction.type != 'p' && checkNotNullEmpty(det.quantity) && det.quantity <= 0) {
                    showPopUP(det,$scope.resourceBundle['invalid.quantity'],index);
                    return true;
                }
                if($scope.huQty && det.quantity % $scope.huQty != 0) {
                    $scope.togglePopup(det,index);
                    return true;
                }
                index+=1;
            });
            var isStatusEmpty = false;
            index = 0;
            $scope.batchDet.forEach(function(data) {
                if(data.quantity > 0 && checkNotNullEmpty(displayStatus) && checkNullEmpty(data.mst) && $scope.msm) {
                    data.isVisitedStatus = true;
                    isStatusEmpty = true;
                    $scope.togglePopup(data,index);
                }
                index+=1;
            });
            if (!isMissing && !isStatusEmpty) {
                $scope.batchDet.forEach(function (det) {
                    det.bexp = formatDate(det.bexp);
                    det.bmfdt = formatDate(det.bmfdt);
                    det.mId = $scope.mid;
                });
                $scope.$parent.material.bquantity = angular.copy($scope.batchDet);
                $scope.toggle($scope.$index);
            }
        };
        $scope.deleteRow = function (index) {
            $scope.exists.splice(index, 1);
            $scope.batchDet.splice(index, 1);
            $scope.currentBatches.splice(index, 1);
            for(var i=0;i<$scope.currentBatches.length;i++){
                $scope.checkCurrentBatches($scope.currentBatches[i],i);
            }
        };
        $scope.today = formatDate2Url(new Date());
    }]);

trnControllers.controller('StockBatchTransactionCtrl',['$scope','invService','$timeout',
    function($scope,invService,$timeout) {

        $scope.invalidPopup = 0;
        var status = "";
        var displayStatus;
        $scope.togglePopup = function (batch, index, forceEvent, source) {
            $timeout(function () {
                var skipTrigger = false;
                if (forceEvent) {
                    var eventName = forceEvent
                } else if (checkNotNullEmpty($scope.huName) && checkNotNullEmpty($scope.huQty) && checkNotNullEmpty(batch.bid) && checkNotNullEmpty(batch.quantity) && batch.quantity % $scope.huQty != 0) {
                    eventName = 'showpopup';
                    showPopUP(batch, batch.quantity + " of " + batch.bid + " does not match the multiples of units expected in " + $scope.huName + ". It should be in multiples of " + $scope.huQty + " " + $scope.mnm + ".", index);
                    skipTrigger = true;
                } else {
                    if (batch.quantity > 0 && batch.isVisitedStatus && checkNotNullEmpty(displayStatus) && checkNullEmpty(batch.mst) && $scope.msm) {
                        showPopUP(batch, $scope.resourceBundle['status.required'], index, $scope.material.ts ? "sbtmt" : "sbtm");
                        skipTrigger = true;
                    } else {
                        batch.ainvalidPopup = false;
                    }
                    eventName = 'hidepopup';
                    batch.invalidPopup = false;
                    if(!skipTrigger) {
                        $scope.invalidPopup = $scope.invalidPopup <= 0 ? 0 : $scope.invalidPopup - 1;
                    }
                }
                if(!skipTrigger) {
                    source = source || "b";
                    $timeout(function () {
                        $("[id='"+ source + $scope.mid + index + "']").trigger(eventName);
                    }, 0);
                }
            }, 0);
        };

        function showPopUP(mat, msg, index, source) {
            $timeout(function () {
                if(checkNullEmpty(source)) {
                    mat.popupMsg = msg;
                    if (!mat.invalidPopup) {
                        mat.invalidPopup = true;
                        $scope.invalidPopup += 1;
                    }
                    source = "b";
                } else {
                    mat.aPopupMsg = msg;
                    if(!mat.ainvalidPopup) {
                        mat.ainvalidPopup = true;
                        $scope.invalidPopup +=1;
                    }
                }
                $timeout(function () {
                    $("[id='"+ source + $scope.mid + index + "']").trigger('showpopup');
                }, 0);
            }, 0);
        }

        $scope.loading = true;
        $scope.exists = [];

        $scope.checkCurrentBatches = function(data, index) {
            $scope.exists[index] = false;
            if($scope.transaction.type == 'p' && $scope.batchDet.length > 0) {
                $scope.batchDet.some(function (det) {
                    if(data == det.bid) {
                        $scope.exists[index] = true;
                        return true;
                    }
                });
            }
            if($scope.batchNewDet.length > 0) {
                for( var i=0; i<$scope.batchNewDet.length; i++) {
                    if(i != index && $scope.batchNewDet[i].bid == data) {
                        $scope.exists[index] = true;
                    }
                }
            }
        };
        $scope.checkBatchExists = function(data, index) {
            if(checkNotNullEmpty(data)) {
                $scope.checkCurrentBatches(data, index);
                if($scope.exists[index]){
                    return false;
                }
            }
        };
        $scope.updateQuantity = function () {
            var remq = $scope.allocq || 0;
            if (checkNotNullEmpty($scope.exBatches)) {
                var batMap = {};
                $scope.batchDet.forEach(function (batch) {
                    batMap[batch.bid] = batch;
                });
                $scope.exBatches.forEach(function (exBatch) {
                    var batch = batMap[exBatch.id];
                    if (checkNotNull(batch)) {
                        batch.quantity = exBatch.atpstk;
                        remq = remq - batch.quantity;
                    }
                });
            }
            if (checkNotNull($scope.allocq)) {
                $scope.batchDet.some(function (batch) {
                    if (checkNotNullEmpty(batch.quantity)) {
                        return true;
                    }
                    batch.quantity = Math.min(batch.atpstk, remq);
                    remq = remq - batch.quantity;
                    return remq <= 0;
                });
            }
        };
        if (checkNullEmpty($scope.bdata)) {
            $scope.batchDet = [];
            if (checkNotNullEmpty($scope.kid)) {
                $scope.showLoading();
                invService.getBatchDetail($scope.mid, $scope.kid).then(function (data) {
                    $scope.batchDet = data.data;
                    if (checkNotNullEmpty($scope.batchDet)) {
                        $scope.updateQuantity();
                        $scope.batchDet.forEach(function (det) {
                            det.bexp = formatDate(new Date(det.bexp));
                            if (checkNotNullEmpty(det.bmfdt)) {
                                det.bmfdt = formatDate(new Date(det.bmfdt));
                            }
                            det.mId = $scope.mid;
                        });
                    }
                    if($scope.material.ts) {
                        status = checkNotNullEmpty($scope.tempmatstatus) ? $scope.tempmatstatus[0] : undefined;
                        displayStatus = $scope.tempmatstatus;
                    } else {
                        status = checkNotNullEmpty($scope.matstatus) ? $scope.matstatus[0] : undefined;
                        displayStatus = $scope.matstatus;
                    }
                    $scope.batchDet.forEach(function(data) {
                        data.mst = status;
                    });
                    $scope.loading = false;
                    $scope.hideLoading();
                }).catch(function error(msg) {
                    $scope.loading = false;
                    $scope.hideLoading();
                    $scope.showErrorMsg(msg);
                });
            }
        } else {
            $scope.batchDet = angular.copy($scope.bdata);
            $scope.updateQuantity();
            $scope.loading = false;
            $scope.hideLoading();
        }
        $scope.clearBatchTrans = function() {
            if (confirm($scope.resourceBundle['clearbatch.confirm'])) {
                $scope.batchDet.forEach(function (det) {
                    det.quantity = '0';
                });
            }
        };

        /*Add batch controller*/
        $scope.addRow = function () {
            $scope.batchNewDet.push({mst: status});
        };
        if (checkNotNullEmpty($scope.bndata)) {
            $scope.batchNewDet = angular.copy($scope.bndata);
            $scope.batchNewDet.forEach(function (det) {
                if (checkNotNullEmpty(det)) {
                    if (!angular.isDate(det.bexp)) {
                        det.bexp = string2Date(det.bexp, 'dd/mm/yyyy', '/');
                    }
                    if (!angular.isDate(det.bmfdt)) {
                        det.bmfdt = string2Date(det.bmfdt, 'dd/mm/yyyy', '/');
                    }
                }
            });
        } else {
            $scope.batchNewDet = [];
        }
        $scope.saveStockBatchTrans = function () {
            var isDup = $scope.exists.some(function (val){
                if(val) {
                    $scope.showWarning($scope.resourceBundle['bid.dup']);
                    return true;
                }
            });
            if(isDup) {
                return false;
            }
            var index = 0;
            var errorList = 0;
            $scope.batchNewDet.forEach(function (det) {
                if (checkNullEmpty(det.bid) || checkNullEmpty(det.bexp) ||
                    checkNullEmpty(det.bmfnm) || checkNullEmpty(det.quantity)) {
                    $scope.showWarning($scope.resourceBundle['fields.missing']);
                    errorList++ ;
                }
                if($scope.huQty && det.quantity % $scope.huQty != 0) {
                    $scope.togglePopup(det,index);
                    errorList++ ;
                }
                if(checkNotNullEmpty(displayStatus) && checkNullEmpty(det.mst) && $scope.msm) {
                    det.isVisitedStatus = true;
                    $scope.togglePopup(det,index);
                    errorList++ ;
                }
                index+=1;
            });
            index = 0;
            $scope.batchDet.forEach(function(data) {
                if(data.quantity > 0 && checkNotNullEmpty(displayStatus) && checkNullEmpty(data.mst) && $scope.msm) {
                    data.isVisitedStatus = true;
                    $scope.togglePopup(data, data.bid);
                    errorList ++;
                }
                index += 1;
            });
            if(errorList > 0) {
                return true;
            }
            if (errorList == 0) {
                $scope.batchNewDet.forEach(function (det) {
                    det.bexp = formatDate(det.bexp);
                    det.bmfdt = formatDate(det.bmfdt);
                    det.mId = $scope.mid;
                });
                $scope.updateNewBatch(angular.copy($scope.batchNewDet));
                $scope.$parent.material.bnquantity = angular.copy($scope.batchNewDet);
                $scope.updateBatch(angular.copy($scope.batchDet));
                $scope.$parent.material.oquantity = angular.copy($scope.batchDet);
                $scope.$parent.material.bquantity = $scope.$parent.material.oquantity.concat($scope.$parent.material.bnquantity);
                $scope.toggle($scope.$index);
            } else {
                return true;
            }

        };
        $scope.deleteRow = function (index) {
            $scope.batchNewDet.splice(index, 1);
            $scope.exists.splice(index, 1);
        };
        $scope.today = formatDate2Url(new Date());
    }
]);

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

var homeControllers = angular.module('homeControllers', []);
homeControllers.controller('homePageReportsCtrl', ['$scope', 'homePageService','domainCfgService','requestContext','$location','matService','$sce','$timeout',
    function($scope,homePageService,domainCfgService,requestContext,$location,matService,$sce,$timeout){
        $scope.wparams = [["vw", "vw"],["day","day","",formatDate2Url]];
        $scope.vw = requestContext.getParam("vw") || "m";
        var dt = new Date();
        $scope.day = parseUrlDate(requestContext.getParam("day")) || new Date(dt.getFullYear(), dt.getMonth(), dt.getDate() );
        $scope.enablePost = false;
        $scope.cType = "mscombi2d";
        $scope.cHeight = "250";
        $scope.cWidth = "600";
        $scope.ag = 't';
        $scope.ig = 's';
        $scope.cur = '';
        $scope.today = parseUrlDate(new Date(dt.getFullYear(), dt.getMonth(), dt.getDate()));
        $scope.filterCaption=undefined;
        $scope.exFilter=undefined;
        function addExtraFilter(value, text, type) {
            if (value != undefined) {
                $scope.exFilter = value;
                $scope.exFilterText = text;
                $scope.exType = type;
            } else if (value == undefined) {
                $scope.exFilter = $scope.exType = undefined;
            }
        }
        $scope.initWatch = function(){
            $scope.$watch('mtag', function (newVal, oldVal) {
                if (oldVal != newVal || checkNullEmpty(newVal) ){
                    if (newVal instanceof Object) {
                        $scope.material = "";
                        addExtraFilter(newVal.id, newVal.text, 'mTag');
                        $scope.getStats();
                    } else if (newVal == undefined) {
                        addExtraFilter(newVal, undefined, 'mTag');
                        $scope.getStats();
                    }
                }
            });

            $scope.$watch('material', function (newVal, oldVal) {
                if (oldVal != newVal) {
                    if (newVal instanceof Object) {
                        $scope.mtag = "";
                        addExtraFilter(newVal.id, newVal.text, 'mId');
                        $scope.getStats();
                    } else if (newVal == undefined) {
                        addExtraFilter(newVal, undefined, 'mId');
                        $scope.getStats();
                    }
                }
            });
        };
        $scope.getDashboardStats = function(){
            $scope.loading = true;
            $scope.showLoading();
            domainCfgService.getDashboardCfg().then(function(data){
                $scope.ds = data.data;
                $scope.getStats();
                $scope.initWatch();
            }).finally (function(){
                $scope.hideLoading();
            });
        };
        $scope.getDashboardStats();

        ListingController.call(this, $scope, requestContext, $location);
        $scope.init = function(){};
        $scope.fetch = function() {
            $scope.getStats();
        };
        $scope.getAccountingConfiguration = function(){
            domainCfgService.getGeneralCfg().then(function (data) {
                $scope.cnf = data.data;
                $scope.cur = " ("+$scope.cnf.cur+")";
            }).catch(function err(msg){
                $scope.showMessage($scope.resourceBundle['configuration.general.unavailable'] + msg);
            })
        };
        $scope.getAccountingConfiguration();
        $scope.getStats = function(){
            $scope.showLoading();
            $scope.hpr = {};
            var mTag = checkNotNullEmpty($scope.mtag)?$scope.mtag.text:"";
            var matId= checkNotNullEmpty($scope.material)?$scope.material.id:"";
            homePageService.getStats(formatDate2Url($scope.day),$scope.vw,mTag,matId).then(function(data) {
                if(checkNotNullEmpty(data.data)) {
                    $scope.hpr = data.data;
                    $scope.tm = $scope.hpr[0].repGenTime;
                }
                $scope.cLabel = getFCCategories($scope.hpr);
                var type = $scope.vw == 'm' ? "column2d" : "line";
                var showValues = $scope.vw == 'm' ? "1" : "0";
                var enable = "1";
                var domainName = $scope.domainName;
                $scope.cOptions = {
                    "exportEnabled": enable,
                    "theme": "fint",
                    "showLegend":0
                };
                $scope.cOptions_t = angular.copy($scope.cOptions);
                $scope.cOptions_t.yAxisName = "#" + $scope.resourceBundle['transactions'];
                $scope.cOptions_t.paletteColors = "2596CF";
                $scope.cData_t = [];
                $scope.cOptions_t.exportFileName = domainName + "_" + "transactions" + "_" + FormatDate_DD_MM_YYYY($scope.day);
                $scope.cData_t[0] = getFCSeries($scope.hpr,0,undefined,type,true,showValues);

                $scope.cOptions_u = angular.copy($scope.cOptions);
                $scope.cOptions_u.yAxisName = "#" + $scope.resourceBundle['home.activeusers'];
                $scope.cOptions_u.paletteColors = "00a65a";
                $scope.cData_u = [];
                $scope.cOptions_u.exportFileName = domainName + "_" + "active_users" + "_" + FormatDate_DD_MM_YYYY($scope.day);
                $scope.cData_u[0] = getFCSeries($scope.hpr,1,undefined,type,true,showValues);

                $scope.cOptions_e = angular.copy($scope.cOptions);
                $scope.cOptions_e.yAxisName = "#" + $scope.resourceBundle['home.activeentities'];
                $scope.cOptions_e.paletteColors = "f56954";
                $scope.cData_e = [];
                $scope.cOptions_e.exportFileName = domainName + "_" + $scope.resourceBundle['home.activeentities'] + "_" + FormatDate_DD_MM_YYYY($scope.day);
                $scope.cData_e[0] = getFCSeries($scope.hpr,2,undefined,type,true,showValues);

                $scope.cOptions_so = angular.copy($scope.cOptions);
                $scope.cOptions_so.yAxisName = "#"  + $scope.resourceBundle['home.stockoutevents'];
                $scope.cOptions_so.paletteColors = "f56954";
                $scope.cData_so = [];
                $scope.cOptions_so.exportFileName = domainName + "_" + "stock_out_events" + "_" + FormatDate_DD_MM_YYYY($scope.day);
                $scope.cData_so[0] = getFCSeries($scope.hpr,8,undefined,type,true,showValues);

                $scope.cOptions_mne = angular.copy($scope.cOptions);
                $scope.cOptions_mne.yAxisName = "# " + $scope.resourceBundle['home.lessthanmin'];
                $scope.cOptions_mne.paletteColors = "f39c12";
                $scope.cData_mne = [];
                $scope.cOptions_mne.exportFileName = domainName + "_" + "less_than_min_events" + "_" + FormatDate_DD_MM_YYYY($scope.day);
                $scope.cData_mne[0] = getFCSeries($scope.hpr,9,undefined,type,true,showValues);

                $scope.cOptions_mxe = angular.copy($scope.cOptions);
                $scope.cOptions_mxe.yAxisName = "# " + $scope.resourceBundle['home.greaterthanmax'];
                $scope.cOptions_mxe.paletteColors = "2596CF";
                $scope.cData_mxe = [];
                $scope.cOptions_mxe.exportFileName = domainName + "_" + "greater_than_max_events" + "_" + FormatDate_DD_MM_YYYY($scope.day);
                $scope.cData_mxe[0] = getFCSeries($scope.hpr,10,undefined,type,true,showValues);

                $scope.cOptions_rpt = angular.copy($scope.cOptions);
                $scope.cOptions_rpt.yAxisName = "#" + $scope.resourceBundle['home.replenishmenttime'];
                $scope.cOptions_rpt.paletteColors = "00a65a";
                $scope.cData_rpt = [];
                $scope.cOptions_rpt.exportFileName = domainName + "_" + "replenishment_time" + "_" + FormatDate_DD_MM_YYYY($scope.day);
                $scope.cData_rpt[0] = getFCSeries($scope.hpr,11,undefined,type,true,showValues);

                $scope.filteredData();
                timeoutFusion();
                $scope.ag = 'e';
                $scope.ig = 's';
                var subCaption='';
                if (checkNotNullEmpty($scope.exFilter)) {
                    if ($scope.exType == "mTag") {
                        subCaption = "<b>Material tag: </b>" + $scope.exFilterText;
                    } else if ($scope.exType == "mId") {
                        subCaption = "<b>Material name: </b>" + $scope.exFilterText;
                    }
                }
                $scope.filterCaption = $sce.trustAsHtml(subCaption);
            }).catch(function error(msg){
                $scope.showErrorMsg(msg);
            }).finally(function (){
                $scope.loading = false;
                $scope.hideLoading();
            });
        };
        function timeoutFusion() {
            $scope.tout = false;
            $scope.showLoading();
            $timeout(function(){
                $scope.tout=true;
                $scope.hideLoading();
            },300);
        }
        $scope.filteredData = function() {
            if (checkNotNullEmpty($scope.hpr[0])) {
                $scope.hpr.tc = $scope.roundValue($scope.hpr[0].value[0]); //transaction count
                $scope.hpr.au = $scope.roundValue($scope.hpr[0].value[1]); //active users count
                $scope.hpr.ae = $scope.roundValue($scope.hpr[0].value[2]); //active entities count
                $scope.hpr.soc = $scope.roundValue($scope.hpr[0].value[8]); //stock out count
                $scope.hpr.lme = $scope.roundValue($scope.hpr[0].value[9]); //less than min event
                $scope.hpr.gme = $scope.roundValue($scope.hpr[0].value[10]); //greater than max event
                $scope.hpr.rpt = $scope.roundValue($scope.hpr[0].value[11]); //replenishment time
                $scope.hpr.tcp = $scope.roundValue($scope.hpr[0].value[12]); //transaction change percentage
                $scope.hpr.tucp = $scope.roundValue($scope.hpr[0].value[13]); //transaction user change percentage
                $scope.hpr.tecp = $scope.roundValue($scope.hpr[0].value[14]); //transaction entity change percentage
                $scope.hpr.socp = $scope.roundValue($scope.hpr[0].value[20]); //stock out change percentage
                $scope.hpr.lmcp = $scope.roundValue($scope.hpr[0].value[21]); //less than min change percentage
                $scope.hpr.gmcp = $scope.roundValue($scope.hpr[0].value[22]); //greater than max change percentage
                $scope.hpr.rptcp = $scope.roundValue($scope.hpr[0].value[23]); //replenishment time change percentage
                $scope.hpr.date = $scope.hpr[0].label;
                $scope.dDate = $scope.hpr.date;
                $scope.getAbsoluteChangePercent();
            }
            if(checkNullEmpty($scope.hpr.date)){
                $scope.hpr.date = $scope.day;
                if($scope.vw == 'm'){
                    $scope.dDate = FormatDate_MMMM_YYYY($scope.hpr.date)
                }else{
                    $scope.dDate = FormatDate_MMMM_DD_YYYY($scope.hpr.date)
                }
            }

        };
        $scope.showActivity = function(char){
            $scope.ag = char;
        };
        $scope.showOrders = function(char) {
            $scope.og = char;
        };
        $scope.showInventory = function(char) {
            $scope.ig = char;
        };
        $scope.roundValue = function(val){
            if(val != null){
                var val1 = val % 1;
                if(val1 != 0){
                    return Math.round( val * 10 ) / 10;
                }else{
                    return parseInt(val);
                }
            }
        };
        $scope.getAbsoluteChangePercent = function(){
            $scope.hpr.atcp = Math.abs($scope.hpr.tcp); //transaction change percentage
            $scope.hpr.atucp = Math.abs($scope.hpr.tucp); //transaction user change percentage
            $scope.hpr.atecp = Math.abs($scope.hpr.tecp); //transaction entity change percentage
            $scope.hpr.asocp = Math.abs($scope.hpr.socp); //stock out change percentage
            $scope.hpr.almcp = Math.abs($scope.hpr.lmcp); //less than min change percentage
            $scope.hpr.agmcp = Math.abs($scope.hpr.gmcp); //greater than max change percentage
            $scope.hpr.arptcp = Math.abs($scope.hpr.rptcp); //replenishment time change percentage
        }
    }
]);
homeControllers.controller('ShortcutController',['$scope','domainCfgService',
    function($scope,domainCfgService) {
        $scope.enableBB = false;
        $scope.getBulletinBoardConfiguration = function(){
            domainCfgService.getBulletinBoardCfg().then(function(data){
                $scope.bb = data.data;
                if($scope.bb.ecl > 0) {
                    $scope.enableBB = true;
                    if($scope.iAdm){
                        $scope.enablePost = true;
                    }
                }
            }).catch(function error(msg) {
                $scope.showErrorMsg(msg);
            });
        };
        $scope.getBulletinBoardConfiguration();
        $scope.showPostBoard = false;
        $scope.toggle = function() {
            $scope.showPostBoard = !$scope.showPostBoard;
        };
    }
]);

homeControllers.controller('DashboardOrderController', ['$scope','homePageService','domainCfgService','requestContext','$location',
function($scope,homePageService,domainCfgService,requestContext,$location){
    $scope.wparams = [["vw", "vw"],["day","day","",formatDate2Url]];
    $scope.vw = requestContext.getParam("vw") || "m";
    var dt = new Date();
    $scope.day = parseUrlDate(requestContext.getParam("day")) || new Date(dt.getFullYear(), dt.getMonth(), dt.getDate() );
    $scope.enablePost = false;
    $scope.cType = "mscombi2d";
    $scope.cHeight = "250";
    $scope.cWidth = "600";
    $scope.og = 'o';
    $scope.cur = '';
    $scope.today = parseUrlDate(new Date(dt.getFullYear(), dt.getMonth(), dt.getDate()));

    $scope.getDashboardStats = function(){
        $scope.loading = true;
        $scope.showLoading();
        domainCfgService.getDashboardCfg().then(function(data){
            $scope.ds = data.data;
            $scope.getStats();
        }).finally (function(){
            $scope.hideLoading();
        });
    };
    ListingController.call(this, $scope, requestContext, $location);
    $scope.init = function(){};
    $scope.fetch = function() {
        $scope.getDashboardStats();
    };
    $scope.getAccountingConfiguration = function(){
        domainCfgService.getGeneralCfg().then(function (data) {
            $scope.cnf = data.data;
            $scope.cur = " ("+$scope.cnf.cur+")";
        }).catch(function err(msg){
            $scope.showMessage($scope.resourceBundle['configuration.general.unavailable'] + msg);
        })
    };
    $scope.getAccountingConfiguration();
    $scope.getStats = function(){
        $scope.showLoading();
        $scope.hpr = {};
        homePageService.getStats(formatDate2Url($scope.day),$scope.vw,"","").then(function(data) {
            if(checkNotNullEmpty(data.data)) {
                $scope.hpr = data.data;
                $scope.tm = $scope.hpr[0].repGenTime;
            }
            $scope.cLabel = getFCCategories($scope.hpr);
            var type = $scope.vw == 'm' ? "column2d" : "line";
            var showValues = $scope.vw == 'm' ? "1" : "0";
            var enable = "1";
            var domainName = $scope.domainName;
            $scope.cOptions = {
                "exportEnabled": enable,
                "theme": "fint",
                "showLegend":0
            };
            $scope.cOptions_r = angular.copy($scope.cOptions);
            $scope.cOptions_r.yAxisName = "#"+$scope.resourceBundle['home.revenue']+$scope.cur;
            $scope.cOptions_r.paletteColors = "00a65a";
            $scope.cData_r = [];
            $scope.cOptions_r.exportFileName = domainName + "_" + "revenue" + "_" + FormatDate_DD_MM_YYYY($scope.day);
            $scope.cData_r[0] = getFCSeries($scope.hpr,3,undefined,"area",true,showValues);

            $scope.cOptions_o = angular.copy($scope.cOptions);
            $scope.cOptions_o.yAxisName = "#" + $scope.resourceBundle['orders'];
            $scope.cOptions_o.paletteColors = "2596CF";
            $scope.cData_o = [];
            $scope.cOptions_o.exportFileName = domainName + "_" + "orders" + "_" + FormatDate_DD_MM_YYYY($scope.day);
            $scope.cData_o[0] = getFCSeries($scope.hpr,4,undefined,type,true,showValues);

            $scope.cOptions_fo = angular.copy($scope.cOptions);
            $scope.cOptions_fo.yAxisName = "#" + $scope.resourceBundle['home.fulfilledorders'];
            $scope.cOptions_fo.paletteColors = "00a65a";
            $scope.cData_fo = [];
            $scope.cOptions_fo.exportFileName = domainName + "_" + "fulfilled_orders" + "_" + FormatDate_DD_MM_YYYY($scope.day);
            $scope.cData_fo[0] = getFCSeries($scope.hpr,5,undefined,type,true,showValues);

            $scope.cOptions_po = angular.copy($scope.cOptions);
            $scope.cOptions_po.yAxisName = "#" + $scope.resourceBundle['home.pendingorders'];
            $scope.cOptions_po.paletteColors = "f56954";
            $scope.cData_po = [];
            $scope.cOptions_po.exportFileName = domainName + "_" + "pending_orders" + "_" + FormatDate_DD_MM_YYYY($scope.day);
            $scope.cData_po[0] = getFCSeries($scope.hpr,6,undefined,type,true,showValues);

            $scope.cOptions_ort = angular.copy($scope.cOptions);
            $scope.cOptions_ort.yAxisName = "#" + $scope.resourceBundle['home.orderresponsetime'];
            $scope.cOptions_ort.paletteColors = "f39c12";
            $scope.cData_ort = [];
            $scope.cOptions_ort.exportFileName = domainName + "_" + "order_response_time" + "_" + FormatDate_DD_MM_YYYY($scope.day);
            $scope.cData_ort[0] = getFCSeries($scope.hpr,7,undefined,type,true,showValues);
            $scope.filteredData();
            $scope.og = 'o';
        }).catch(function error(msg){
            $scope.showErrorMsg(msg);
        }).finally(function (){
            $scope.loading = false;
            $scope.hideLoading();
        });
    };
    $scope.getDashboardStats();
    $scope.filteredData = function() {
        if (checkNotNullEmpty($scope.hpr[0])) {
            $scope.hpr.rvn = $scope.roundValue($scope.hpr[0].value[3]); //revenue
            $scope.hpr.ord = $scope.roundValue($scope.hpr[0].value[4]); //orders
            $scope.hpr.ford = $scope.roundValue($scope.hpr[0].value[5]); //fulfilled orders
            $scope.hpr.pord = $scope.roundValue($scope.hpr[0].value[6]); //pending orders
            $scope.hpr.ort = $scope.roundValue($scope.hpr[0].value[7]); //order response time
            $scope.hpr.rcp = $scope.roundValue($scope.hpr[0].value[15]); //revenue change percentage
            $scope.hpr.ocp = $scope.roundValue($scope.hpr[0].value[16]); //order change percentage
            $scope.hpr.focp = $scope.roundValue($scope.hpr[0].value[17]); //fulfilled order change percentage
            $scope.hpr.pocp = $scope.roundValue($scope.hpr[0].value[18]); //pending order change percentage
            $scope.hpr.ortcp = $scope.roundValue($scope.hpr[0].value[19]); //order response time change percentage
            $scope.hpr.date = $scope.hpr[0].label;
            $scope.dDate = $scope.hpr.date;
            $scope.getAbsoluteChangePercent();
        }
        if(checkNullEmpty($scope.hpr.date)){
            $scope.hpr.date = $scope.day;
            if($scope.vw == 'm'){
                $scope.dDate = FormatDate_MMMM_YYYY($scope.hpr.date)
            }else{
                $scope.dDate = FormatDate_MMMM_DD_YYYY($scope.hpr.date)
            }
        }

    };
    $scope.showOrders = function(char) {
        $scope.og = char;
    };
    $scope.roundValue = function(val){
        if(val != null){
            var val1 = val % 1;
            if(val1 != 0){
                return Math.round( val * 10 ) / 10;
            }else{
                return parseInt(val);
            }
        }
    };
    $scope.getAbsoluteChangePercent = function(){
        $scope.hpr.arcp = Math.abs($scope.hpr.rcp); //revenue change percentage
        $scope.hpr.aocp = Math.abs($scope.hpr.ocp); //order change percentage
        $scope.hpr.afocp = Math.abs($scope.hpr.focp); //fulfilled order change percentage
        $scope.hpr.apocp = Math.abs($scope.hpr.pocp); //pending order change percentage
        $scope.hpr.aortcp = Math.abs($scope.hpr.ortcp); //order response time change percentage
    }
}
]);
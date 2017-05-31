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

/**
 * Created by Mohan Raja on 30/01/15
 */
var reportControllers = angular.module('reportControllers', []);
reportControllers.controller('ReportController', ['$scope',
    function ($scope) {
        $scope.setRType = function (type) {
            $scope.subview = type;
        };
    }
]);
reportControllers.controller('InvTrendController', ['$scope', 'reportService', '$window','exportService','$timeout',
    function ($scope, reportService, $window, exportService, $timeout) {
        $scope.showExport = false;
        var dt = new Date();
        $scope.today = parseUrlDate(new Date(dt.getFullYear(), dt.getMonth(), dt.getDate()));
        $scope.dm = "m";
        var colType = "column2d";
        var lineType = "line";
        var areaType = "area";
        var date = "Date";
        var issues = $scope.resourceBundle['issues'];
        var receipts = "Receipts";
        var discards = "Discards";
        var opnStk = "Opening Stock";
        var dem = "Demand";
        var clsStk = "Closing Stock";
        var stkAdj = "Stock adjustment";
        var min = "Min";
        var max = "Max";
        var transfers = "Transfers";
        $scope.exportAsCSV = function (daily) {
            var data = daily ? $scope.dailyChartData : $scope.chartData;
            var csvData = getDataAsCSV(data, [date, issues, receipts, discards, clsStk, stkAdj, min, max, transfers, opnStk, dem]);
            var fileName = "Inventory_Trends_" + formatDate2Url($scope.from) +"_"+ formatDate2Url($scope.to);
            exportCSV(csvData, fileName, $timeout);
        };
        $scope.cHeight = "300";
        $scope.cWidth = "600";
        $scope.cType = "mscombi2d";
        $scope.cOptions = {
            "yAxisName": $scope.resourceBundle['quantity'],
            "exportEnabled": '1',
            "theme": "fint"
        };
        $scope.dcOptions = angular.copy($scope.cOptions);
        $scope.rep = undefined;
        $scope.filterCaption = '';
        $scope.resetFilters = function () {
            $scope.showDaily = false;
            $scope.showChart = false;
            $scope.rep = undefined;
            $scope.tm = undefined;
            setDefaultDates();
        };
        function setDefaultDates(){
            var dt = new Date();
            $scope.from = new Date(dt.getFullYear() - 5, dt.getMonth(), 1);
            $scope.to = new Date(dt.getFullYear(), dt.getMonth() + 1, 0);
        }
        setDefaultDates();
        $scope.getFChartData = function () {
            if(checkNullEmpty($scope.rep) || checkNullEmpty($scope.rep.mat)){
                $scope.showError($scope.resourceBundle['material.notspecified']);
                return;
            }
            if (checkNotNullEmpty($scope.rep) && checkNotNullEmpty($scope.rep.mat)) {
                var count = $scope.getFieldCount();
                if(count > 1) {
                    $scope.showOptionalWarning();
                    return;
                }
                $scope.selectedMid = $scope.rep.mat.mId;
                $scope.showDaily = false;
                $scope.showChart = true;
                $scope.loading = true;
                $scope.showLoading();
                $scope.fcData = {};
                $scope.fcData.freq = "monthly";
                $scope.fcData.rty = "consumptiontrends";
                $scope.fcData.mid = $scope.selectedMid;
                $scope.fcData.st = $scope.rep.st;
                $scope.fcData.dis = $scope.rep.dis;
                $scope.fcData.egrp = checkNotNullEmpty($scope.rep.eg) ? $scope.rep.eg.id : null;
                $scope.fcData.eid = checkNotNullEmpty($scope.rep.entity) ? $scope.rep.entity.id : null;
                if(checkNotNullEmpty($scope.rep.etag)) {
                    $scope.fcData.etag = $scope.rep.etag;
                }
                $scope.fcData.stDate = formatDate2Url($scope.from);
                var endDate = changeToLastDayOfMonth($scope.to);
                $scope.fcData.enDate = formatDate2Url(endDate);
                $scope.cOptions.caption = $scope.rep.mat.mnm + ": " + $scope.resourceBundle['report.consumptiontrends'] + " - " + $scope.resourceBundle['overview'];
                $scope.cOptions.subcaption = "From: " + formatDate($scope.from) + " To: " + formatDate(endDate) + $scope.filterCaption;
                $scope.cOptions.subCaptionFontSize = 10;
                reportService.getFChartData($scope.fcData).then(function (data) {
                    var mmmYYYYFormat = "mmm yyyy";
                    $scope.chartData = data.data;
                    if(checkNotNullEmpty($scope.chartData)) {
                        $scope.tm = $scope.chartData[0].repGenTime;
                    }
                    $scope.cLabel = getFCCategories(data.data, mmmYYYYFormat);

                    $scope.cData_o = [];
                    $scope.cData_o[0] = getFCSeries(data.data, 0, issues, colType);
                    $scope.cData_o[1] = getFCSeries(data.data, 1, receipts, colType);
                    $scope.cData_o[2] = getFCSeries(data.data, 2, discards, colType);
                    $scope.cData_o[3] = getFCSeries(data.data, 3, clsStk, lineType);
                    $scope.tHead_o = [date, opnStk, issues, receipts, discards, clsStk, stkAdj, min, max, transfers];
                    $scope.tData_o = getTableData(data.data, [8, 0, 1, 2, 3, 4, 5, 6, 7], mmmYYYYFormat);

                    $scope.cOptions_s = angular.copy($scope.cOptions);
                    $scope.cOptions_s.paletteColors = "#0075C2,#FF0000,#00C0EF,#FF00000";
                    var ms = getFCSeries(data.data, 5);
                    if(checkNotNullEmpty(ms)) {
                        var minVal = ms.data[0].value;
                        $scope.cOptions_s.yAxisMinValue = minVal - Math.abs(minVal) * 0.1;
                        if($scope.cOptions_s.yAxisMinValue > 0){
                            $scope.cOptions_s.yAxisMinValue = 0;
                        }
                    }
                    var mxs = getFCSeries(data.data, 6);
                    if(checkNotNullEmpty(mxs)) {
                        var maxVal = mxs.data[0].value;
                        $scope.cOptions_s.yAxisMaxValue = maxVal * 1.1;
                    }
                    $scope.cData_s = [];
                    $scope.cData_s[0] = getFCSeries(data.data, 3, clsStk, areaType);
                    $scope.cData_s[1] = getFCSeries(data.data, 4, stkAdj, colType);
                    $scope.cTrend_s = getFCTrend(minVal,maxVal);
                    $scope.tHead_s = [date, clsStk, stkAdj, min, max];
                    $scope.tData_s = getTableData(data.data, [3, 4, 5, 6], mmmYYYYFormat);

                    $scope.cData_i = [];
                    $scope.cData_i[0] = getFCSeries(data.data, 0, undefined, colType);
                    $scope.tHead_i = [date, issues];
                    $scope.tData_i = getTableData(data.data, [0], mmmYYYYFormat);

                    $scope.cData_r = [];
                    $scope.cData_r[0] = getFCSeries(data.data, 1, undefined, colType);
                    $scope.tHead_r = [date, receipts];
                    $scope.tData_r = getTableData(data.data, [1], mmmYYYYFormat);

                    $scope.cData_d = [];
                    $scope.cData_d[0] = getFCSeries(data.data, 2, undefined, colType);
                    $scope.tHead_d = [date, discards];
                    $scope.tData_d = getTableData(data.data, [2], mmmYYYYFormat);

                    $scope.cData_t = [];
                    $scope.cData_t[0] = getFCSeries(data.data, 7, undefined, colType);
                    $scope.tHead_t = [date, transfers];
                    $scope.tData_t = getTableData(data.data, [7], mmmYYYYFormat);

                    $scope.cSummary = constructSummaryString(data.data, [2], [3,4,5,6,7,8,9], [issues, receipts, discards]);
                    $scope.cOptions.labelStep = getLabelStepValue(data.data.length);
                    $scope.loading = false;
                    $scope.hideLoading();
                }).catch(function error(msg) {
                    $scope.showErrorMsg(msg);
                    $scope.hideLoading();
                });
            }
        };
        $scope.getDFChartData = function (label) {
            $scope.showDaily = true;
            $scope.dLoading = true;
            $scope.fcData = {};
            $scope.fcData.freq = "daily";
            $scope.fcData.rty = "consumptiontrends";
            $scope.fcData.mid = $scope.selectedMid;
            $scope.fcData.st = $scope.rep.st;
            $scope.fcData.dis = $scope.rep.dis;
            $scope.fcData.egrp = checkNotNullEmpty($scope.rep.eg) ? $scope.rep.eg.id : null;
            $scope.fcData.eid = checkNotNullEmpty($scope.rep.entity) ? $scope.rep.entity.id : null;
            if(checkNotNullEmpty($scope.rep.etag)) {
                $scope.fcData.etag = $scope.rep.etag;
            }
            $scope.fcData.stDate = label;
            $scope.fcData.daily = true;
            $scope.dcOptions.caption = $scope.resourceBundle['daily.trends'] + " " + FormatDate_MMM_YYYY(constructDate(label));
            reportService.getFChartData($scope.fcData).then(function (data) {
                $scope.dailyChartData = data.data;
                $scope.dcLabel = getFCCategories(data.data, "mmm dd, yyyy");

                $scope.dcData_o = [];
                $scope.dcData_o[0] = getFCSeries(data.data, 0, issues, colType, true);
                $scope.dcData_o[1] = getFCSeries(data.data, 1, receipts, colType, true);
                $scope.dcData_o[2] = getFCSeries(data.data, 2, discards, colType, true);
                $scope.dcData_o[3] = getFCSeries(data.data, 3, clsStk, lineType, true);
                $scope.dtData_o = getTableData(data.data, [8, 0, 1, 2, 3,4,5,6,7]);

                $scope.dcOptions_s = angular.copy($scope.dcOptions);
                $scope.dcOptions_s.paletteColors = "#0075C2,#FF0000,#00C0EF,#FF00000";
                var ms = getFCSeries(data.data, 5);
                if(checkNotNullEmpty(ms)) {
                    var minVal = ms.data[0].value;
                    $scope.dcOptions_s.yAxisMinValue = minVal - Math.abs(minVal) * 0.1;
                    if($scope.dcOptions_s.yAxisMinValue > 0){
                        $scope.dcOptions_s.yAxisMinValue = 0;
                    }
                }
                var mxs = getFCSeries(data.data, 6);
                if(checkNotNullEmpty(mxs)) {
                    var maxVal = mxs.data[0].value;
                    $scope.dcOptions_s.yAxisMaxValue = maxVal * 1.1;
                }
                $scope.dcData_s = [];
                $scope.dcData_s[0] = getFCSeries(data.data, 3, clsStk, areaType, true);
                $scope.dcData_s[1] = getFCSeries(data.data, 4, stkAdj, colType, true,null,"FF0000");
                $scope.dcTrend_s = getFCTrend(minVal,maxVal);
                $scope.dtData_s = getTableData(data.data, [3, 4, 5, 6]);

                $scope.dcData_i = [];
                $scope.dcData_i[0] = getFCSeries(data.data, 0, undefined, colType, true);
                $scope.dtData_i = getTableData(data.data, [0]);

                $scope.dcData_r = [];
                $scope.dcData_r[0] = getFCSeries(data.data, 1, undefined, colType, true);
                $scope.dtData_r = getTableData(data.data, [1]);

                $scope.dcData_d = [];
                $scope.dcData_d[0] = getFCSeries(data.data, 2, undefined, colType, true);
                $scope.dtData_d = getTableData(data.data, [2]);

                $scope.dcData_t = [];
                $scope.dcData_t[0] = getFCSeries(data.data, 7, undefined, colType, true);
                $scope.dtData_t = getTableData(data.data, [7]);

                $scope.dcSummary = constructSummaryString(data.data, [2], [3,4,5,6,7,8,9], [issues, receipts, discards]);
                $scope.dcOptions.labelStep = getLabelStepValue(data.data.length);
                $scope.dLoading = false;
            }).catch(function error(msg) {
                $scope.showErrorMsg(msg);
            });
        };
        $scope.cView = 'o';
        $scope.vw = 'c';
        $scope.setCView = function (view) {
            $scope.cView = view;
            var caption = $scope.rep.mat.mnm + ": " + $scope.resourceBundle['report.consumptiontrends'] + " - ";
            if($scope.cView == 'o') {
                $scope.cOptions.caption = caption + $scope.resourceBundle['overview'];
            } else if($scope.cView == 's') {
                $scope.cOptions_s.caption = caption + $scope.resourceBundle['stock'];
            } else if($scope.cView == 'i') {
                $scope.cOptions.caption = caption + $scope.resourceBundle['issues'];
            } else if($scope.cView == 'r') {
                $scope.cOptions.caption = caption + $scope.resourceBundle['receipts'];
            } else if($scope.cView == 'd') {
                $scope.cOptions.caption = caption + $scope.resourceBundle['transactions.wastage.upper'];
            } else if($scope.cView == 't') {
                $scope.cOptions.caption = caption + $scope.resourceBundle['transfers'];
            }
        };
        var dt = new Date();
        $scope.startDate = new Date(dt.getFullYear() - 5, dt.getMonth(), 1);
        $scope.endDate = new Date(dt.getFullYear(), dt.getMonth() + 1, 0);

        $scope.openExportOptions = function(){
            $scope.showExport = true;
        };

        $scope.closeExportOptions = function(){
            $scope.showExport = false;
        };

        $scope.getFieldCount = function(){
            var count = 0;
            $scope.filterCaption = '';
            if($scope.rep.st != undefined && checkNotNullEmpty($scope.rep.st)) {
                $scope.filterCaption = " " + $scope.resourceBundle['state'] + ": " + $scope.rep.st;
                count++;
            }
            if($scope.rep.dis != undefined && checkNotNullEmpty($scope.rep.dis)){
                $scope.filterCaption = " " + $scope.resourceBundle['report.district'] + ": " + $scope.rep.dis;
                count++;
            }
            if($scope.rep.eg != undefined && checkNotNullEmpty($scope.rep.eg)){
                $scope.filterCaption = " " + $scope.resourceBundle['report.poolgroup'] + ": " + $scope.rep.eg;
                count++;
            }
            if($scope.rep.entity != undefined && checkNotNullEmpty($scope.rep.entity)){
                $scope.filterCaption = " " + $scope.resourceBundle['kiosk'] + ": " + $scope.rep.entity.nm;
                count++;
            }
            if($scope.rep.etag != undefined && checkNotNullEmpty($scope.rep.etag)){
                $scope.filterCaption = " " + $scope.resourceBundle['tagentity'] + ": " + $scope.rep.etag;
                count++;
            }
            return count;
        };
        $scope.showOptionalWarning = function(){
            $scope.showWarning($scope.resourceBundle['filter.optional']);
        };
        $scope.exportInvTrendsReports = function(type){
            if(checkNotNullEmpty($scope.rep) && checkNotNullEmpty($scope.rep.mat)){
                var count = $scope.getFieldCount();
                if(count > 1){
                    $scope.showOptionalWarning();
                }else{
                    var stDate = angular.copy($scope.startDate);
                    var enDate = angular.copy($scope.endDate);
                    if($scope.dm == 'm') {
                        $scope.frequency = "monthly";
                        if (stDate.getMonth() == 0) {
                            stDate = new Date(stDate.getFullYear() - 1, 11, stDate.getDate());
                        } else {
                            stDate = new Date(stDate.getFullYear(), stDate.getMonth() - 1, stDate.getDate());
                        }
                        enDate = changeToLastDayOfMonth(enDate);
                    } else {
                        $scope.frequency = "daily";
                        stDate = new Date( stDate.getTime() - ( 86400 * 1000 ));
                    }
                    stDate = FormatDate_DD_MM_YYYY(stDate);
                    enDate = FormatDate_DD_MM_YYYY(enDate);
                    $scope.filterMap = {};
                    $scope.mapVal = [];
                    $scope.mapVal.push($scope.rep.mat.mId);
                    $scope.filterMap.mtrl = $scope.mapVal;
                    $scope.mapVal = [];
                    if($scope.rep.st != undefined && checkNotNullEmpty($scope.rep.st)){
                        $scope.mapVal.push($scope.rep.st);
                        $scope.filterMap.stt = $scope.mapVal;
                    }
                    if($scope.rep.dis != undefined && checkNotNullEmpty($scope.rep.dis)){
                        $scope.mapVal.push($scope.rep.dis);
                        $scope.filterMap.dstr = $scope.mapVal;
                    }
                    if($scope.rep.eg != undefined && checkNotNullEmpty($scope.rep.eg)){
                        $scope.mapVal.push($scope.rep.eg.id);
                        $scope.filterMap.plgr = $scope.mapVal;
                    }
                    if($scope.rep.entity != undefined && checkNotNullEmpty($scope.rep.entity)){
                        $scope.mapVal.push($scope.rep.entity.id);
                        $scope.filterMap.ksk = $scope.mapVal;
                    }
                    if($scope.rep.etag != undefined && checkNotNullEmpty($scope.rep.etag)){
                        $scope.mapVal.push($scope.rep.etag);
                        $scope.filterMap.ktag = $scope.mapVal;
                    }
                    exportService.exportReport(type,stDate,enDate,$scope.frequency,$scope.filterMap).then(function(data){
                        $scope.showSuccess($scope.resourceBundle['export.success1'] + ' ' + $scope.mailId + ' ' + $scope.resourceBundle['export.success2']  + ' ' + $scope.resourceBundle['exportstatusinfo2'] + ' ' + data.data + '. ' + $scope.resourceBundle['exportstatusinfo1']);
                    }).catch(function error(msg){
                        $scope.showErrorMsg(msg);
                    });
                }
            }else{
                $scope.showWarning($scope.resourceBundle['data.notavailable']);
            }
        }
    }
]);
reportControllers.controller('OrdResController', ['$scope', 'reportService', '$window','exportService', '$timeout',
    function ($scope, reportService, $window, exportService, $timeout) {
        $scope.showExport = false;
        $scope.dm = 'm';
        $scope.rep = {};
        var colType = "column2d";
        var date = "Date";
        var pt = "Order processing times - day(s)";
        var dlt = "Delivery lead times - day(s)";
        $scope.rep = {};
        $scope.exportAsCSV = function (daily) {
            var data = daily ? $scope.dailyChartData : $scope.chartData;
            var csvData = getDataAsCSV(data, [date, pt, dlt]);
            var fileName = "Order_Response_Times_" + formatDate2Url($scope.from) +"_"+ formatDate2Url($scope.to);
            exportCSV(csvData, fileName, $timeout);
        };
        $scope.cHeight = "300";
        $scope.cWidth = "600";
        $scope.cType = "mscombi2d";
        $scope.cOptions = {
            "yAxisName": $scope.resourceBundle['days'],
            "exportEnabled": '1',
            "theme": "fint"
        };
        $scope.filterCaption = '';
        $scope.resetFilters = function () {
            $scope.showChart = false;
            $scope.rep = {};
            $scope.tm = undefined;
            setDefaultDates();
        };
        function setDefaultDates(){
            var dt = new Date();
            $scope.from = new Date(dt.getFullYear() - 5, dt.getMonth(), 1);
            $scope.to = new Date(dt.getFullYear(), dt.getMonth() + 1, 0);
            $scope.today = parseUrlDate(new Date(dt.getFullYear(), dt.getMonth(), dt.getDate()));
        }
        setDefaultDates();
        $scope.getFChartData = function () {
            var count = $scope.getFieldCount();
            if(count > 1) {
                $scope.showOptionalWarning();
                return;
            }
            $scope.showChart = true;
            $scope.loading = true;
            $scope.showLoading();
            $scope.fcData = {};
            $scope.fcData.freq = "monthly";
            $scope.fcData.rty = "orderresponsetimes";
            $scope.fcData.st = $scope.rep.st;
            $scope.fcData.dis = $scope.rep.dis;
            $scope.fcData.egrp = checkNotNullEmpty($scope.rep.eg) ? $scope.rep.eg.id : null;
            $scope.fcData.eid = checkNotNullEmpty($scope.rep.entity) ? $scope.rep.entity.id : null;
            if(checkNotNullEmpty($scope.rep.etag)) {
                $scope.fcData.etag = $scope.rep.etag;
            }
            $scope.fcData.stDate = formatDate2Url($scope.from);
            var endDate = changeToLastDayOfMonth($scope.to);
            $scope.fcData.enDate = formatDate2Url(endDate);
            $scope.cOptions.caption = $scope.resourceBundle['report.orderresponsetimes'] + " - " + $scope.resourceBundle['order.processingtimes'];
            $scope.cOptions.subcaption = "From: " + formatDate($scope.from) + " To: " + formatDate(endDate) + $scope.filterCaption;
            $scope.cOptions.subCaptionFontSize = 10;
            reportService.getFChartData($scope.fcData).then(function (data) {
                var mmmYYYYFormat = "mmm yyyy";
                $scope.chartData = data.data;
                if(checkNotNullEmpty($scope.chartData)) {
                    $scope.tm = $scope.chartData[0].repGenTime;
                }
                $scope.cLabel = getFCCategories(data.data, mmmYYYYFormat);

                $scope.cData_p = [];
                $scope.cData_p[0] = getFCSeries(data.data, 0, undefined, colType, true);
                $scope.tHead_p = [date, pt];
                $scope.tData_p = getTableData(data.data, [0], mmmYYYYFormat);
                $scope.cSummary_p = getSeriesAverage(data.data, 0);

                $scope.cData_d = [];
                $scope.cData_d[0] = getFCSeries(data.data, 1, undefined, colType, true);
                $scope.tHead_d = [date, dlt];
                $scope.tData_d = getTableData(data.data, [1], mmmYYYYFormat);
                $scope.cSummary_d = getSeriesAverage(data.data, 1);

                $scope.cOptions.labelStep = getLabelStepValue(data.data.length);
                $scope.loading = false;
                $scope.hideLoading();
            }).catch(function error(msg) {
                $scope.showErrorMsg(msg);
                $scope.hideLoading();
            });
        };
        $scope.cView = 'p';
        $scope.vw = 'c';
        $scope.setCView = function (view) {
            $scope.cView = view;
            var caption = $scope.resourceBundle['report.orderresponsetimes'] + " - ";
            if ($scope.cView == 'p') {
                // Set the caption of the fusion chart to Order processing time
                $scope.cOptions.caption = caption + $scope.resourceBundle['order.processingtimes'];
            } else if ($scope.cView == 'd') {
                $scope.cOptions.caption = caption + $scope.resourceBundle['report.deliveryleadtime'];
            }
        };
        var dt = new Date();
        $scope.startDate = new Date(dt.getFullYear() - 5, dt.getMonth(), 1);
        $scope.endDate = new Date(dt.getFullYear(), dt.getMonth() + 1, 0);
        $scope.openExportOptions = function(){
            $scope.showExport = true;
        };

        $scope.closeExportOptions = function(){
            $scope.showExport = false;
        };

        $scope.getFieldCount = function(){
            var count = 0;
            $scope.filterCaption = '';
            if($scope.rep.st != undefined && checkNotNullEmpty($scope.rep.st)){
                $scope.filterCaption = " " + $scope.resourceBundle['state'] + ": " + $scope.rep.st;
                count++;
            }
            if($scope.rep.dis != undefined && checkNotNullEmpty($scope.rep.dis)){
                $scope.filterCaption = " " + $scope.resourceBundle['report.district'] + ": " + $scope.rep.dis;
                count++;
            }
            if($scope.rep.eg != undefined && checkNotNullEmpty($scope.rep.eg)){
                $scope.filterCaption = " " + $scope.resourceBundle['report.poolgroup'] + ": " + $scope.rep.eg;
                count++;
            }
            if($scope.rep.entity != undefined && checkNotNullEmpty($scope.rep.entity)){
                $scope.filterCaption = " " + $scope.resourceBundle['kiosk'] + ": " + $scope.rep.entity.nm;
                count++;
            }
            if($scope.rep.etag != undefined && checkNotNullEmpty($scope.rep.etag)){
                $scope.filterCaption = " " + $scope.resourceBundle['tagentity'] + ": " + $scope.rep.etag;
                count++;
            }
            return count;
        };
        $scope.showOptionalWarning = function(){
            $scope.showWarning($scope.resourceBundle['filter.optional']);
        };

        $scope.exportOrderResponseReports = function(type){
            var count = $scope.getFieldCount();
            if(count > 1) {
                $scope.showOptionalWarning();
            }else{
                if($scope.dm == 'm'){
                    $scope.frequency = "monthly";
                    $scope.endDate = changeToLastDayOfMonth($scope.endDate);
                }else{
                    $scope.frequency = "daily";
                }
                var stDate = FormatDate_DD_MM_YYYY($scope.startDate);
                var enDate = FormatDate_DD_MM_YYYY($scope.endDate);
                $scope.filterMap = {};
                $scope.mapVal = [];
                if($scope.rep.st != undefined && checkNotNullEmpty($scope.rep.st)){
                    $scope.mapVal.push($scope.rep.st);
                    $scope.filterMap.stt = $scope.mapVal;
                }
                if($scope.rep.dis != undefined && checkNotNullEmpty($scope.rep.dis)){
                    $scope.mapVal.push($scope.rep.dis);
                    $scope.filterMap.dstr = $scope.mapVal;
                }
                if($scope.rep.eg != undefined && checkNotNullEmpty($scope.rep.eg)){
                    $scope.mapVal.push($scope.rep.eg.id);
                    $scope.filterMap.plgr = $scope.mapVal;
                }
                if($scope.rep.entity != undefined && checkNotNullEmpty($scope.rep.entity)){
                    $scope.mapVal.push($scope.rep.entity.id);
                    $scope.filterMap.ksk = $scope.mapVal;
                }
                if($scope.rep.etag != undefined && checkNotNullEmpty($scope.rep.etag)){
                    $scope.mapVal.push($scope.rep.etag);
                    $scope.filterMap.ktag = $scope.mapVal;
                }
                exportService.exportReport(type,stDate,enDate,$scope.frequency,$scope.filterMap).then(function(data){
                    $scope.showSuccess($scope.resourceBundle['export.success1'] + ' ' + $scope.mailId + ' ' + $scope.resourceBundle['export.success2'] + ' ' + $scope.resourceBundle['exportstatusinfo2'] + ' ' + data.data + '. ' + $scope.resourceBundle['exportstatusinfo1']);
                }).catch(function error(msg){
                    $scope.showErrorMsg(msg);
                });
            }
        }
    }
]);
reportControllers.controller('RepResController', ['$scope', 'reportService', '$window','exportService', '$timeout',
    function ($scope, reportService, $window, exportService, $timeout) {
        $scope.dm = 'm';
        var colType = "column2d";
        var date = "Date";
        var zs = "Zero Stock - day(s)";
        var min = "< min. - day(s)";
        var max = "> max. - day(s)";
        $scope.rep = {};
        $scope.exportAsCSV = function (daily) {
            var data = daily ? $scope.dailyChartData : $scope.chartData;
            var csvData = getDataAsCSV(data, [date, zs, min, max]);
            var fileName = "Replenishment_Response_Times_" + formatDate2Url($scope.from) +"_"+ formatDate2Url($scope.to);
            exportCSV(csvData, fileName, $timeout);
        };
        $scope.cHeight = "300";
        $scope.cWidth = "600";
        $scope.cType = "mscombi2d";
        $scope.cOptions = {
            "yAxisName": "Quantity",
            "exportEnabled": '1',
            "theme": "fint"
        };
        $scope.filterCaption = '';
        $scope.resetFilters = function () {
            $scope.showChart = false;
            $scope.rep = {};
            $scope.tm = undefined;
            setDefaultDates();
        };
        function setDefaultDates(){
            var dt = new Date();
            $scope.from = new Date(dt.getFullYear() - 5, dt.getMonth(), 1);
            $scope.to = new Date(dt.getFullYear(), dt.getMonth() + 1, 0);
            $scope.today = parseUrlDate(new Date(dt.getFullYear(), dt.getMonth(), dt.getDate()));
        }
        setDefaultDates();
        $scope.getFChartData = function () {
            var count = $scope.getFieldCount();
            if(count > 1) {
                $scope.showOptionalWarning();
                return;
            }
            $scope.showChart = true;
            $scope.loading = true;
            $scope.showLoading();
            $scope.fcData = {};
            $scope.fcData.freq = "monthly";
            $scope.fcData.rty = "replenishmentresponsetimes";
            $scope.fcData.mid = checkNotNullEmpty($scope.rep.mat) ? $scope.rep.mat.mId : null;
            if(checkNotNullEmpty($scope.rep.mtag)) {
                $scope.fcData.mtag = $scope.rep.mtag;
            }
            $scope.fcData.st = $scope.rep.st;
            $scope.fcData.dis = $scope.rep.dis;
            $scope.fcData.egrp = checkNotNullEmpty($scope.rep.eg) ? $scope.rep.eg.id : null;
            $scope.fcData.eid = checkNotNullEmpty($scope.rep.entity) ? $scope.rep.entity.id : null;
            if(checkNotNullEmpty($scope.rep.etag)) {
                $scope.fcData.etag = $scope.rep.etag;
            }
            $scope.fcData.stDate = formatDate2Url($scope.from);
            var endDate = changeToLastDayOfMonth($scope.to);
            $scope.fcData.enDate = formatDate2Url(endDate);
            $scope.cOptions.caption = $scope.resourceBundle['report.replenishmentresponsetimes'] + " - " + $scope.resourceBundle['report.zerostock'];
            $scope.cOptions.subcaption = "From: " + formatDate($scope.from) + " To: " + formatDate(endDate) + $scope.filterCaption;
            $scope.cOptions.subCaptionFontSize = 10;
            reportService.getFChartData($scope.fcData).then(function (data) {
                var mmmYYYYFormat = "mmm yyyy";
                $scope.chartData = data.data;
                if(checkNotNullEmpty($scope.chartData)) {
                    $scope.tm = $scope.chartData[0].repGenTime;
                }
                $scope.cLabel = getFCCategories(data.data, mmmYYYYFormat);

                $scope.cData_z = [];
                $scope.cData_z[0] = getFCSeries(data.data, 0, undefined, colType, true);
                $scope.tHead_z = [date, zs];
                $scope.tData_z = getTableData(data.data, [0], mmmYYYYFormat);
                $scope.cSummary_z = getSeriesAverage(data.data, 0);

                $scope.cData_n = [];
                $scope.cData_n[0] = getFCSeries(data.data, 1, undefined, colType, true);
                $scope.tHead_n = [date, min];
                $scope.tData_n = getTableData(data.data, [1], mmmYYYYFormat);
                $scope.cSummary_n = getSeriesAverage(data.data, 1);

                $scope.cData_x = [];
                $scope.cData_x[0] = getFCSeries(data.data, 2, undefined, colType, true);
                $scope.tHead_x = [date, max];
                $scope.tData_x = getTableData(data.data, [2], mmmYYYYFormat);
                $scope.cSummary_x = getSeriesAverage(data.data, 2);

                $scope.cOptions.labelStep = getLabelStepValue(data.data.length);
                $scope.loading = false;
                $scope.hideLoading();
            }).catch(function error(msg) {
                $scope.showErrorMsg(msg);
                $scope.hideLoading();
            });
        };
        $scope.cView = 'z';
        $scope.vw = 'c';
        $scope.setCView = function (view) {
            $scope.cView = view;
            var caption = $scope.resourceBundle['report.replenishmentresponsetimes'] + " - ";
            if($scope.cView == 'z') {
                $scope.cOptions.caption  = caption + $scope.resourceBundle['report.zerostock'];
            } else if($scope.cView == 'n') {
                $scope.cOptions.caption  = caption + "<" + $scope.resourceBundle['material.reorderlevel'];
            } else if($scope.cView == 'x') {
                $scope.cOptions.caption  = caption + ">" + $scope.resourceBundle['max'];
            }
        };
        var dt = new Date();
        $scope.startDate = new Date(dt.getFullYear() - 5, dt.getMonth(), 1);
        $scope.endDate = new Date(dt.getFullYear(), dt.getMonth() + 1, 0);
        $scope.openExportOptions = function(){
            $scope.showExport = true;
        };

        $scope.closeExportOptions = function(){
            $scope.showExport = false;
        };

        $scope.getFieldCount = function(){
            var count = 0;
            $scope.filterCaption = '';
            if($scope.rep.mat != undefined && checkNotNullEmpty($scope.rep.mat)){
                $scope.filterCaption = " " + $scope.resourceBundle['material'] + ": " + $scope.rep.mat.mnm;
                count++;
            }
            if($scope.rep.mtag != undefined && checkNotNullEmpty($scope.rep.mtag)){
                $scope.filterCaption = " " + $scope.resourceBundle['tagmaterial'] + ": " + $scope.rep.mtag;
                count++;
            }
            if($scope.rep.st != undefined && checkNotNullEmpty($scope.rep.st)){
                $scope.filterCaption = " " + $scope.resourceBundle['state'] + ": " + $scope.rep.st;
                count++;
            }
            if($scope.rep.dis != undefined && checkNotNullEmpty($scope.rep.dis)){
                $scope.filterCaption = " " + $scope.resourceBundle['report.district'] + ": " + $scope.rep.dis;
                count++;
            }
            if($scope.rep.eg != undefined && checkNotNullEmpty($scope.rep.eg)){
                $scope.filterCaption = " " + $scope.resourceBundle['report.poolgroup'] + ": " + $scope.rep.eg;
                count++;
            }
            if($scope.rep.entity != undefined && checkNotNullEmpty($scope.rep.entity)){
                $scope.filterCaption = " " + $scope.resourceBundle['kiosk'] + ": " + $scope.rep.entity.nm;
                count++;
            }
            if($scope.rep.etag != undefined && checkNotNullEmpty($scope.rep.etag)){
                $scope.filterCaption = " " + $scope.resourceBundle['tagentity'] + ": " + $scope.rep.etag;
                count++;
            }
            return count;
        };
        $scope.showOptionalWarning = function(){
            $scope.showWarning($scope.resourceBundle['filter.optional']);
        };

        $scope.exportRepTimeReports = function(type){
            var count = $scope.getFieldCount();
            if(count > 1) {
                $scope.showOptionalWarning();
            } else {
                if($scope.dm == 'm'){
                    $scope.frequency = "monthly";
                    $scope.endDate = changeToLastDayOfMonth($scope.endDate);
                }else{
                    $scope.frequency = "daily";
                }
                var stDate = FormatDate_DD_MM_YYYY($scope.startDate);
                var enDate = FormatDate_DD_MM_YYYY($scope.endDate);
                $scope.filterMap = {};
                $scope.mapVal = [];

                if($scope.rep.mat != undefined && checkNotNullEmpty($scope.rep.mat)){
                    $scope.mapVal.push($scope.rep.mat.mId);
                    $scope.filterMap.mtrl = $scope.mapVal;
                }

                if($scope.rep.mtag != undefined && checkNotNullEmpty($scope.rep.mtag)){
                    $scope.mapVal.push($scope.rep.mtag);
                    $scope.filterMap.mtag = $scope.mapVal;
                }

                if($scope.rep.st != undefined && checkNotNullEmpty($scope.rep.st)){
                    $scope.mapVal.push($scope.rep.st);
                    $scope.filterMap.stt = $scope.mapVal;
                }

                if($scope.rep.dis != undefined && checkNotNullEmpty($scope.rep.dis)){
                    $scope.mapVal.push($scope.rep.dis);
                    $scope.filterMap.dstr = $scope.mapVal;
                }
                if($scope.rep.eg != undefined && checkNotNullEmpty($scope.rep.eg)){
                    $scope.mapVal.push($scope.rep.eg.id);
                    $scope.filterMap.plgr = $scope.mapVal;
                }
                if($scope.rep.entity != undefined && checkNotNullEmpty($scope.rep.entity)){
                    $scope.mapVal.push($scope.rep.entity.id);
                    $scope.filterMap.ksk = $scope.mapVal;
                }
                if($scope.rep.etag != undefined && checkNotNullEmpty($scope.rep.etag)){
                    $scope.mapVal.push($scope.rep.etag);
                    $scope.filterMap.ktag = $scope.mapVal;
                }

                exportService.exportReport(type,stDate,enDate,$scope.frequency,$scope.filterMap).then(function(data){
                    $scope.showSuccess($scope.resourceBundle['export.success1'] + ' ' + $scope.mailId + ' ' + $scope.resourceBundle['export.success2'] + ' ' + $scope.resourceBundle['exportstatusinfo2'] + ' ' + data.data + '. ' + $scope.resourceBundle['exportstatusinfo1']);
                }).catch(function error(msg){
                    $scope.showErrorMsg(msg);
                });
            }
        }
    }
]);
reportControllers.controller('TransCntController', ['$scope', 'reportService', '$window','exportService','$timeout',
    function ($scope, reportService, $window, exportService, $timeout) {
        $scope.dm = 'm';
        $scope.rep = {};
        var colType = "column2d";
        var date = "Date";
        var issues = $scope.resourceBundle['issues'];
        var receipts = "Receipts";
        var stkCnt = "Stock Counts";
        var discards = "Discards";
        var demand = "Demand";
        var transfers = "Transfers";
        $scope.exportAsCSV = function (daily) {
            var data = daily ? $scope.dailyChartData : $scope.chartData;
            var csvData = getDataAsCSV(data, [date, issues, receipts, stkCnt, discards, demand, transfers]);
            var fileName = "Transaction_Counts_" + formatDate2Url($scope.from) +"_"+ formatDate2Url($scope.to);
            exportCSV(csvData, fileName, $timeout);
        };
        $scope.cHeight = "300";
        $scope.cWidth = "600";
        $scope.cType = "stackedcolumn2d";
        $scope.cOptions = {
            "yAxisName": "Quantity",
            "exportEnabled": '1',
            "theme": "fint"
        };
        $scope.dcOptions = angular.copy($scope.cOptions);
        $scope.sCType = "pie2d";
        $scope.sum_co = {
            "exportEnabled": '1',
            "theme": "fint"
        };
        $scope.filterCaption = '';
        $scope.resetFilters = function () {
            $scope.showDaily = false;
            $scope.showChart = false;
            $scope.rep = {};
            $scope.tm = undefined;
            setDefaultDates();
        };
        function setDefaultDates(){
            var dt = new Date();
            $scope.from = new Date(dt.getFullYear() - 5, dt.getMonth(), 1);
            $scope.to = new Date(dt.getFullYear(), dt.getMonth() + 1, 0);
            $scope.today = parseUrlDate(new Date(dt.getFullYear(), dt.getMonth(), dt.getDate()));
        }
        setDefaultDates();
        $scope.getFChartData = function () {
            var count = $scope.getFieldCount();
            if(count > 1) {
                $scope.showOptionalWarning();
                return;
            }
            var endDate = changeToLastDayOfMonth($scope.to);
            $scope.showDaily = false;
            $scope.showChart = true;
            $scope.loading = true;
            $scope.showLoading();
            $scope.fcData = {};
            $scope.fcData.freq = "monthly";
            $scope.fcData.rty = "transactioncounts";
            $scope.fcData.mid = checkNotNullEmpty($scope.rep.mat) ? $scope.rep.mat.mId : null;
            if(checkNotNullEmpty($scope.rep.mtag)) {
                $scope.fcData.mtag = $scope.rep.mtag;
            }
            $scope.fcData.st = $scope.rep.st;
            $scope.fcData.dis = $scope.rep.dis;
            $scope.fcData.egrp = checkNotNullEmpty($scope.rep.eg) ? $scope.rep.eg.id : null;
            $scope.fcData.eid = checkNotNullEmpty($scope.rep.entity) ? $scope.rep.entity.id : null;
            if(checkNotNullEmpty($scope.rep.etag)) {
                $scope.fcData.etag = $scope.rep.etag;
            }
            $scope.fcData.stDate = formatDate2Url($scope.from);
            $scope.fcData.enDate = formatDate2Url(endDate);
            $scope.cOptions.caption = $scope.resourceBundle['report.transactioncounts'] + " - " + $scope.resourceBundle['all'];
            $scope.cOptions.subcaption = "From: " + formatDate($scope.from) + " To: " + formatDate(endDate) + $scope.filterCaption;
            $scope.cOptions.subCaptionFontSize = 10;
            reportService.getFChartData($scope.fcData).then(function (data) {
                var mmmYYYYFormat = "mmm yyyy";
                $scope.chartData = data.data;
                if(checkNotNullEmpty($scope.chartData)) {
                    $scope.tm = $scope.chartData[0].repGenTime;
                }
                $scope.cLabel = getFCCategories(data.data, mmmYYYYFormat);

                $scope.cData_a = [];
                $scope.cData_a[0] = getFCSeries(data.data, 0, issues, colType, false, false, undefined, false, true, true);
                $scope.cData_a[1] = getFCSeries(data.data, 1, receipts, colType, false, false, undefined, false, true, true);
                $scope.cData_a[2] = getFCSeries(data.data, 2, stkCnt, colType, false,false,undefined,false,true, true);
                $scope.cData_a[3] = getFCSeries(data.data, 3, discards, colType, false, false, undefined, false, true, true);
                $scope.cData_a[4] = getFCSeries(data.data, 4, demand, colType, false, false, undefined, false, true, true);
                $scope.cData_a[5] = getFCSeries(data.data, 5, transfers, colType, false, false, undefined, false, true, true);
                $scope.tHead_a = [date, issues, receipts, stkCnt, discards, demand, transfers];
                $scope.tData_a = getTableData(data.data, [0, 1, 2, 3, 4, 5], mmmYYYYFormat);
                $scope.cOptions_a = angular.copy($scope.cOptions);
                //$scope.cOptions_a.showsum = 1;

                $scope.cData_i = [];
                $scope.cData_i[0] = getFCSeries(data.data, 0, undefined, colType, false, false, undefined, false, true);
                $scope.tHead_i = [date, issues];
                $scope.tData_i = getTableData(data.data, [0], mmmYYYYFormat);

                $scope.cData_r = [];
                $scope.cData_r[0] = getFCSeries(data.data, 1, undefined, colType, false, false,undefined,false,true);
                $scope.tHead_r = [date, receipts];
                $scope.tData_r = getTableData(data.data, [1], mmmYYYYFormat);

                $scope.cData_s = [];
                $scope.cData_s[0] = getFCSeries(data.data, 2, undefined, colType, false, false, undefined, false, true);
                $scope.tHead_s = [date, stkCnt];
                $scope.tData_s = getTableData(data.data, [2], mmmYYYYFormat);

                $scope.cData_d = [];
                $scope.cData_d[0] = getFCSeries(data.data, 3, undefined, colType,  false, false, undefined, false, true);
                $scope.tHead_d = [date, discards];
                $scope.tData_d = getTableData(data.data, [3], mmmYYYYFormat);

                $scope.cData_m = [];
                $scope.cData_m[0] = getFCSeries(data.data, 4, undefined, colType,  false, false, undefined, false, true);
                $scope.tHead_m = [date, demand];
                $scope.tData_m = getTableData(data.data, [4], mmmYYYYFormat);

                $scope.cData_t = [];
                $scope.cData_t[0] = getFCSeries(data.data, 5, undefined, colType,  false, false, undefined, false, true);
                $scope.tHead_t = [date, transfers];
                $scope.tData_t = getTableData(data.data, [5], mmmYYYYFormat);

                $scope.sum_t_head = ["Type", "Count"];
                $scope.sum_t = getSummaryTable(data.data, [issues, receipts, stkCnt, discards, demand, transfers]);
                $scope.sum_cd = getSummaryFCData($scope.sum_t);
                $scope.sum_cd_tot = getSummaryFCDataTotal($scope.sum_t);

                $scope.cOptions.labelStep = getLabelStepValue(data.data.length);
                $scope.loading = false;
                $scope.hideLoading();
            }).catch(function error(msg) {
                $scope.showErrorMsg(msg);
                $scope.hideLoading();
            });
        };
        $scope.getDFChartData = function (label) {
            $scope.showDaily = true;
            $scope.dLoading = true;
            $scope.fcData = {};
            $scope.fcData.freq = "daily";
            $scope.fcData.rty = "transactioncounts";
            $scope.fcData.mid = checkNotNullEmpty($scope.rep.mat) ? $scope.rep.mat.mId : null;
            if(checkNotNullEmpty($scope.rep.mtag)) {
                $scope.fcData.mtag = $scope.rep.mtag;
            }
            $scope.fcData.st = $scope.rep.st;
            $scope.fcData.dis = $scope.rep.dis;
            $scope.fcData.egrp = checkNotNullEmpty($scope.rep.eg) ? $scope.rep.eg.id : null;
            $scope.fcData.eid = checkNotNullEmpty($scope.rep.entity) ? $scope.rep.entity.id : null;
            if(checkNotNullEmpty($scope.rep.etag)) {
                $scope.fcData.etag = $scope.rep.etag;
            }
            $scope.fcData.stDate = label;
            $scope.fcData.daily = true;
            $scope.dcOptions.caption = $scope.resourceBundle['daily.trends'] + " " + FormatDate_MMM_YYYY(constructDate(label));
            reportService.getFChartData($scope.fcData).then(function (data) {
                $scope.dailyChartData = data.data;
                $scope.dcLabel = getFCCategories(data.data, "mmm dd, yyyy");

                $scope.dcData_a = [];
                $scope.dcData_a[0] = getFCSeries(data.data, 0, issues, colType, true, false, undefined, false, true);
                $scope.dcData_a[1] = getFCSeries(data.data, 1, receipts, colType, true, false, undefined, false, true);
                $scope.dcData_a[2] = getFCSeries(data.data, 2, stkCnt, colType, true, false, undefined, false, true);
                $scope.dcData_a[3] = getFCSeries(data.data, 3, discards, colType, true, false, undefined, false, true);
                $scope.dcData_a[4] = getFCSeries(data.data, 4, demand, colType, true, false, undefined, false, true);
                $scope.dcData_a[5] = getFCSeries(data.data, 5, transfers, colType, true, false, undefined, false, true);
                $scope.dtData_a = getTableData(data.data, [0, 1, 2, 3, 4, 5]);
                $scope.dcOptions_a = angular.copy($scope.dcOptions);
                //$scope.dcOptions_a.showsum = 1;

                $scope.dcData_i = [];
                $scope.dcData_i[0] = getFCSeries(data.data, 0, undefined, colType, true, false, undefined, false, true);
                $scope.dtData_i = getTableData(data.data, [0]);

                $scope.dcData_r = [];
                $scope.dcData_r[0] = getFCSeries(data.data, 1, undefined, colType, true, false, undefined, false, true);
                $scope.dtData_r = getTableData(data.data, [1]);

                $scope.dcData_s = [];
                $scope.dcData_s[0] = getFCSeries(data.data, 2, undefined, colType, true, false, undefined, false, true);
                $scope.dtData_s = getTableData(data.data, [2]);

                $scope.dcData_d = [];
                $scope.dcData_d[0] = getFCSeries(data.data, 3, undefined, colType, true, false, undefined, false, true);
                $scope.dtData_d = getTableData(data.data, [3]);

                $scope.dcData_m = [];
                $scope.dcData_m[0] = getFCSeries(data.data, 4, undefined, colType, true, false, undefined, false, true);
                $scope.dtData_m = getTableData(data.data, [4]);

                $scope.dcData_t = [];
                $scope.dcData_t[0] = getFCSeries(data.data, 5, undefined, colType, true, false, undefined, false, true);
                $scope.dtData_t = getTableData(data.data, [5]);

                $scope.dcOptions.labelStep = getLabelStepValue(data.data.length);
                $scope.dLoading = false;
            }).catch(function error(msg) {
                $scope.showErrorMsg(msg);
            });
        };
        $scope.cView = 'a';
        $scope.vw = 'c';
        $scope.setCView = function (view) {
            $scope.cView = view;
            var caption = $scope.resourceBundle['report.transactioncounts'] + " - ";
            if($scope.cView == 'a') {
                $scope.cOptions_a.caption = caption + $scope.resourceBundle['all'];
            } else if($scope.cView == 'i') {
                $scope.cOptions.caption = caption + $scope.resourceBundle['issues'];
            } else if($scope.cView == 'r') {
                $scope.cOptions.caption = caption + $scope.resourceBundle['receipts'];
            } else if($scope.cView == 's') {
                $scope.cOptions.caption = caption + $scope.resourceBundle['transactions.stockcounts.upper'];
            } else if($scope.cView == 'd') {
                $scope.cOptions.caption = caption + $scope.resourceBundle['transactions.wastage.upper'];
            } else if($scope.cView == 'm') {
                $scope.cOptions.caption = caption + $scope.resourceBundle['demand'];
            } else if($scope.cView == 't') {
                $scope.cOptions.caption = caption + $scope.resourceBundle['transfers'];
            }
        };
        var dt = new Date();
        $scope.startDate = new Date(dt.getFullYear() - 5, dt.getMonth(), 1);
        $scope.endDate = new Date(dt.getFullYear(), dt.getMonth() + 1, 0);
        $scope.openExportOptions = function(){
            $scope.showExport = true;
        };

        $scope.closeExportOptions = function(){
            $scope.showExport = false;
        };

        $scope.getFieldCount = function(){
            var count = 0;
            $scope.filterCaption = '';
            if($scope.rep.mat != undefined && checkNotNullEmpty($scope.rep.mat)){
                $scope.filterCaption = " " + $scope.resourceBundle['material'] + ": " + $scope.rep.mat.mnm;
                count++;
            }
            if($scope.rep.mtag != undefined && checkNotNullEmpty($scope.rep.mtag)){
                $scope.filterCaption = " " + $scope.resourceBundle['tagmaterial'] + ": " + $scope.rep.mtag;
                count++;
            }
            if($scope.rep.st != undefined && checkNotNullEmpty($scope.rep.st)){
                $scope.filterCaption = " " + $scope.resourceBundle['state'] + ": " + $scope.rep.st;
                count++;
            }
            if($scope.rep.dis != undefined && checkNotNullEmpty($scope.rep.dis)){
                $scope.filterCaption = " " + $scope.resourceBundle['report.district'] + ": " + $scope.rep.dis;
                count++;
            }
            if($scope.rep.eg != undefined && checkNotNullEmpty($scope.rep.eg)){
                $scope.filterCaption = " " + $scope.resourceBundle['report.poolgroup'] + ": " + $scope.rep.eg;
                count++;
            }
            if($scope.rep.entity != undefined && checkNotNullEmpty($scope.rep.entity)){
                $scope.filterCaption = " " + $scope.resourceBundle['kiosk'] + ": " + $scope.rep.entity.nm;
                count++;
            }
            if($scope.rep.etag != undefined && checkNotNullEmpty($scope.rep.etag)){
                $scope.filterCaption = " " + $scope.resourceBundle['tagentity'] + ": " + $scope.rep.etag;
                count++;
            }
            return count;
        };
        $scope.showOptionalWarning = function(){
            $scope.showWarning($scope.resourceBundle['filter.optional']);
        };

        $scope.exportTranCountsReports = function(type){
            var count = $scope.getFieldCount();
            if(count > 1) {
                $scope.showOptionalWarning();
            } else {
                if($scope.dm == 'm'){
                    $scope.frequency = "monthly";
                    $scope.endDate = changeToLastDayOfMonth($scope.endDate);
                }else{
                    $scope.frequency = "daily";
                }
                var stDate = FormatDate_DD_MM_YYYY($scope.startDate);
                var enDate = FormatDate_DD_MM_YYYY($scope.endDate);
                $scope.filterMap = {};
                $scope.mapVal = [];
                if($scope.rep.mat != undefined && checkNotNullEmpty($scope.rep.mat)){
                    $scope.mapVal.push($scope.rep.mat.mId);
                    $scope.filterMap.mtrl = $scope.mapVal;
                }
                if($scope.rep.mtag != undefined && checkNotNullEmpty($scope.rep.mtag)){
                    $scope.mapVal.push($scope.rep.mtag);
                    $scope.filterMap.mtag = $scope.mapVal;
                }
                if($scope.rep.st != undefined && checkNotNullEmpty($scope.rep.st)){
                    $scope.mapVal.push($scope.rep.st);
                    $scope.filterMap.stt = $scope.mapVal;
                }
                if($scope.rep.dis != undefined && checkNotNullEmpty($scope.rep.dis)){
                    $scope.mapVal.push($scope.rep.dis);
                    $scope.filterMap.dstr = $scope.mapVal;
                }
                if($scope.rep.eg != undefined && checkNotNullEmpty($scope.rep.eg)){
                    $scope.mapVal.push($scope.rep.eg.id);
                    $scope.filterMap.plgr = $scope.mapVal;
                }
                if($scope.rep.entity != undefined && checkNotNullEmpty($scope.rep.entity)){
                    $scope.mapVal.push($scope.rep.entity.id);
                    $scope.filterMap.ksk = $scope.mapVal;
                }
                if($scope.rep.etag != undefined && checkNotNullEmpty($scope.rep.etag)){
                    $scope.mapVal.push($scope.rep.etag);
                    $scope.filterMap.ktag = $scope.mapVal;
                }
                exportService.exportReport(type,stDate,enDate,$scope.frequency,$scope.filterMap).then(function(data){
                    $scope.showSuccess($scope.resourceBundle['export.success1'] + ' ' + $scope.mailId + ' ' + $scope.resourceBundle['export.success2'] + ' ' + $scope.resourceBundle['exportstatusinfo2'] + ' ' + data.data + '. ' + $scope.resourceBundle['exportstatusinfo1']);
                }).catch(function error(msg){
                    $scope.showErrorMsg(msg);
                });
            }
        }
    }
]);


reportControllers.controller('DomainHierarchyTableController', ['$scope',
    function($scope) {
        $scope.init = function() {
            $scope.ht = {};
        };
        $scope.init();
    }]);

reportControllers.controller('DomainHierarchyController', ['$scope', 'reportService','$timeout','$compile',
    function($scope, reportService,$timeout,$compile) {
        $scope.ht = {};
        $scope.tags = {};
        $scope.hierarchyData = [];
        $scope.isDisplaying = false;
        $scope.fetchHistoricalData = function(domainId) {
          $scope.showLoading();
          reportService.getHistoricalData(domainId).then(function(data){
              $scope.ht = data.data;
              constructParentLevel(1);
          }).catch(function error(msg){
                $scope.showErrorMsg(msg);
          }).finally(function () {
              $scope.hideLoading();
          });
        };
        if(checkNullEmpty($scope.domainId)){
            $scope.fetchHistoricalData($scope.currentDomain);
        }else{
            $scope.fetchHistoricalData($scope.domainId);
        }

        function constructPercent(data){
            if(checkNotNullEmpty(data)) {
                data.lkcp = data.lkc > 0 && data.kc > 0 ? Math.round(100 * data.lkc/data.kc) : 0;
                data.akcp = data.akc > 0 && data.kc > 0 ? Math.round(100 * data.akc/data.kc) : 0;
                data.mlwap = data.mlwa > 0 && data.mac > 0 ? Math.round(100 * data.mlwa/data.mac) : 0;
                data.mawap = data.mawa > 0 && data.mac > 0 ? Math.round(100 * data.mawa/data.mac) : 0;
            }
        }

        function constructParentLevel(level) {
            if(checkNotNullEmpty($scope.ht)) {
                $scope.parentData = [];
                if(checkNullEmpty($scope.hierarchyData)) {
                    $scope.ht.level = level = 1;
                }
                constructPercent($scope.ht);
                $scope.parentData.push($scope.ht);
                if(checkNotNullEmpty($scope.ht.child)) {
                    constructChildLevel(level, $scope.ht.did);
                } else {
                    constructFinalData();
                }
            }
        }

        function constructChildLevel(level, domainId){
            $scope.ht.level = level;
            $scope.ht.child.forEach(function(data) {
                constructPercent(data);
                data.level = level  + 1;
                data.pid = domainId;
                $scope.parentData.push(data);
            });
            constructFinalData();
        }

        function constructFinalData() {
            if(checkNotNullEmpty($scope.ht)) {
               if($scope.ht.level == 1) {
                   $scope.hierarchyData = $scope.parentData;
               } else {
                   var tempData = [];
                   if(checkNotNullEmpty($scope.parentData)) {
                       var did = $scope.parentData[0].did;
                       for(var i=0 ; i<$scope.hierarchyData.length; i++) {
                           if($scope.hierarchyData[i].did == did) {
                               $scope.parentData.forEach(function(data) {
                                   tempData.push(data);
                               });
                           } else {
                               tempData.push($scope.hierarchyData[i]);
                           }
                       }
                   }
                   $scope.hierarchyData = [];
                   tempData.forEach(function(data) {
                       if(data.did == did) {
                           data.expand = true;
                       }
                   });
                   $scope.hierarchyData = tempData;
               }
            }
        }

        $scope.getStyle = function (level) {
            return {
                'padding-left': level*30 + 'px'
            }
        };


        $scope.removeChildData = function(domainId, level, index) {
            $(".uib-tooltip").removeClass();
            if(checkNotNullEmpty(domainId)) {
                for( var i=index+1; i<$scope.hierarchyData.length; i++) {
                    if($scope.hierarchyData[i].level == level) {
                        break;
                    } else if(i > index && $scope.hierarchyData[i].level > level) {
                        $scope.hierarchyData.splice(i, 1);
                        --i;
                    }
                }
            }
        };

        $scope.fetchChildData = function(domainId, level) {
            $(".uib-tooltip").removeClass();
            if(checkNotNullEmpty(domainId)) {
                $scope.showLoading();
                reportService.getHistoricalData(domainId).then(function(data){
                    $scope.ht = data.data;
                    constructParentLevel(level);
                }).catch(function error(msg){
                    $scope.showErrorMsg(msg);
                }).finally(function () {
                    $scope.hideLoading();
                });
            }
        };

        function constructHeader(type){
            var title = "";
            if(checkNotNullEmpty(type)) {
                switch(type) {
                    case "kc":
                        title = $scope.resourceBundle['kiosks'];
                        break;
                    case "lkc":
                        title = $scope.resourceBundle['kiosks'] + " - " + $scope.resourceBundle['da.live'];
                        break;
                    case "akc":
                        title = $scope.resourceBundle['kiosks'] + " - " + $scope.resourceBundle['da.active'];
                        break;
                    case "mac":
                        title = $scope.resourceBundle['da.monitored.assets'];
                        break;
                    case "mlwa":
                        title = $scope.resourceBundle['da.monitored.assets'] + " - " + $scope.resourceBundle['da.live'];
                        break;
                    case "mawa":
                        title = $scope.resourceBundle['da.monitored.assets'] + " - " + $scope.resourceBundle['da.active'];
                        break;
                    default:
                        title = "";
                }
            }
            return title;
        }

        function constructHTML(data, totalCount, type) {
            $scope.unsafeHtml = undefined;
            if(checkNotNullEmpty(data)) {
                var keys = Object.keys(data);
                var header = constructHeader(type);
                var myTable = "<div class='table-responsive hierarchy-table'><table class='table'>";
                myTable += "<thead><th colspan='3' style='border-bottom:none'>" + header + "</th></thead>";
                var cnt = 0;
                if(keys.length > 0 && totalCount > 0) {
                    for(var key in keys){
                        if(cnt == 0){
                            myTable += "<tr>";
                        }else {
                            myTable += "<tr>";
                        }
                        myTable += "<td class='hierarchy-td' style='text-align: left;'>";
                        myTable += keys[key];
                        myTable += "</td>";
                        myTable += "<td class='hierarchy-td'>";
                        myTable += data[keys[key]];
                        myTable += "</td>";
                        myTable += "<td class='hierarchy-td' style='color:#777;font-size: 10px;'>";
                        myTable += data[keys[key]] > 0 && totalCount > 0 ? Math.round(100 * data[keys[key]]/totalCount) : 0;
                        myTable += "%</td></tr>";
                        cnt = cnt + 1;
                    }
                } else {
                    myTable += "<tr style='border-top:hidden !important;'><td colspan='3'>" + $scope.resourceBundle['data.none'] + "</td></td></tr>"
                }
                myTable += "</table></div>";
                $scope.unsafeHtml = myTable;
            }
        }

        $scope.fetchTagsBasedCount = function(did, totalCount, tag, c, prefix, index) {
            $scope.isDisplaying = true;
            if(!$scope.loadingData && checkNullEmpty($scope.hierarchyData[index].toolText) || checkNullEmpty($scope.hierarchyData[index].toolText[c])) {
                var element = $("#" + prefix + index);
                if (element.parent().find(element.selector + ":hover").length > 0) {
                    if (checkNotNullEmpty(did) && checkNotNullEmpty(tag) && checkStrictNotNullEmpty(c)) {
                        $scope.unsafeHtml = '<span class="glyphicons glyphicons-cogwheel spin"></span>';
                        $scope.loadingData = true;
                        reportService.fetchTagsBasedCount(did, tag, c).then(function (data) {
                            $scope.tags = data.data;
                            constructHTML($scope.tags, totalCount, c);
                            if (checkNullEmpty($scope.hierarchyData[index].toolText)) {
                                $scope.hierarchyData[index].toolText = {};
                            }
                            $scope.hierarchyData[index].toolText[c] = $scope.unsafeHtml;
                            $compile(element)($scope);
                            $timeout(function () {
                                element.trigger('hclick');
                            }, 0);
                            $scope.loadingData = false;
                        }).catch(function error(msg) {
                            $scope.showErrorMsg(msg);
                        });
                    }
                }
            }
        };

        $scope.closeTooltip = function(prefix, index) {
            if(!$scope.loadingData) {
                $scope.isDisplaying = false;
                var element = $("#" + prefix + index);
                $compile(element)($scope);
                $timeout(function () {
                    element.trigger('hleave');
                }, 0);
            }
        }
    }]);

reportControllers.controller('UsrActController', ['$scope', 'reportService', '$window','exportService', '$timeout',
    function ($scope, reportService, $window, exportService, $timeout) {
        $scope.dm = 'm';
        $scope.rep = {};
        var colType = "column2d";
        var stkColType = "stackedcolumn2d";
        var date = "Date";
        var logins = "Logins";
        var issues = $scope.resourceBundle['issues'];
        var receipts = "Receipts";
        var stkCnt = "Stock Counts";
        var discards = "Discards";
        var orders = "Orders";
        var revenues = "Revenues";
        $scope.exportAsCSV = function (daily) {
            var data = daily ? $scope.dailyChartData : $scope.chartData;
            var csvData = getDataAsCSV(data, [date, logins, issues, receipts, stkCnt, discards, orders, revenues]);
            var fileName = "User_Activity_" + formatDate2Url($scope.from) +"_"+ formatDate2Url($scope.to);
            exportCSV(csvData, fileName, $timeout);
        };
        $scope.cHeight = "300";
        $scope.cWidth = "600";
        $scope.cType = "stackedcolumn2d";
        $scope.cOptions = {
            "yAxisName": "Count",
            "exportEnabled": '1',
            "theme": "fint"
        };
        $scope.dcOptions = angular.copy($scope.cOptions);
        $scope.resetFilters = function () {
            $scope.showDaily = false;
            $scope.showChart = false;
            $scope.rep = {};
            $scope.tm = undefined;
            setDefaultDates();
        };
        function setDefaultDates() {
            var dt = new Date();
            $scope.from = new Date(dt.getFullYear() - 5, dt.getMonth(), 1);
            $scope.to = new Date(dt.getFullYear(), dt.getMonth() + 1, 0);
            $scope.today = parseUrlDate(new Date(dt.getFullYear(), dt.getMonth(), dt.getDate()));
        }
        setDefaultDates();
        $scope.getFChartData = function () {
            if(checkNullEmpty($scope.rep) || checkNullEmpty($scope.rep.usr)){
                $scope.showError($scope.resourceBundle['user.notspecified']);
                return;
            }
            if (checkNotNullEmpty($scope.rep) && checkNotNullEmpty($scope.rep.usr)) {
                $scope.selectedUid = $scope.rep.usr.id;
                $scope.showDaily = false;
                $scope.showChart = true;
                $scope.loading = true;
                $scope.showLoading();
                $scope.fcData = {};
                $scope.fcData.freq = "monthly";
                $scope.fcData.rty = "useractivity";
                $scope.fcData.uid = $scope.selectedUid;
                $scope.fcData.stDate = formatDate2Url($scope.from);
                var endDate = changeToLastDayOfMonth($scope.to);
                $scope.fcData.enDate = formatDate2Url(endDate);
                $scope.setCView($scope.cView);
                $scope.cOptions.subcaption = "From: " + formatDate($scope.from) + " To: " + formatDate(endDate) + " User: " + $scope.rep.usr.text;
                $scope.cOptions.subCaptionFontSize = 10;
                reportService.getFChartData($scope.fcData).then(function (data) {
                    var mmmYYYYFormat = "mmm yyyy";
                    $scope.chartData = data.data;
                    if(checkNotNullEmpty($scope.chartData)) {
                        $scope.tm = $scope.chartData[0].repGenTime;
                    }
                    $scope.cLabel = getFCCategories(data.data, mmmYYYYFormat);

                    $scope.cData_l = [];
                    $scope.cData_l[0] = getFCSeries(data.data, 0, undefined, colType, false, false, undefined, false, true);
                    $scope.tHead_l = [date, logins];
                    $scope.tData_l = getTableData(data.data, [0], mmmYYYYFormat);
                    $scope.sData_l = constructSummaryString(data.data, [0], [1,2,3,4,5,6], [logins]);

                    $scope.cData_i = [];
                    $scope.cData_i[0] = getFCSeries(data.data, 1, issues, stkColType, false, false, undefined, false, true);
                    $scope.cData_i[1] = getFCSeries(data.data, 2, receipts, stkColType, false, false, undefined, false, true);
                    $scope.cData_i[2] = getFCSeries(data.data, 3, stkCnt, stkColType,false, false, undefined, false, true);
                    $scope.cData_i[3] = getFCSeries(data.data, 4, discards, stkColType, false, false, undefined, false, true);
                    $scope.tHead_i = [date, issues, receipts, stkCnt, discards];
                    $scope.tData_i = getTableData(data.data, [1, 2, 3, 4], mmmYYYYFormat);
                    $scope.sData_i = constructSummaryString(data.data, [1,2,3,4], [0,5,6], [issues,receipts,stkCnt,discards]);

                    $scope.cData_o = [];
                    $scope.cData_o[0] = getFCSeries(data.data, 5, undefined, colType, false, false, undefined, false, true);
                    $scope.tHead_o = [date, orders];
                    $scope.tData_o = getTableData(data.data, [5], mmmYYYYFormat);
                    $scope.sData_o = constructSummaryString(data.data, [5], [0,1,2,3,4,6], [orders]);

                    $scope.cData_r = [];
                    $scope.cData_r[0] = getFCSeries(data.data, 6, undefined, colType, false, false, undefined, false, true);
                    $scope.tHead_r = [date, revenues];
                    $scope.tData_r = getTableData(data.data, [6], mmmYYYYFormat);
                    $scope.sData_r = constructSummaryString(data.data, [6], [0,1,2,3,4,5], [revenues]);

                    $scope.cOptions.labelStep = getLabelStepValue(data.data.length);
                    $scope.loading = false;
                    $scope.hideLoading();
                }).catch(function error(msg) {
                    $scope.showErrorMsg(msg);
                    $scope.hideLoading();
                });
            }
        };
        $scope.getDFChartData = function (label) {
            $scope.showDaily = true;
            $scope.dLoading = true;
            $scope.fcData = {};
            $scope.fcData.freq = "daily";
            $scope.fcData.rty = "useractivity";
            $scope.fcData.uid = $scope.selectedUid;
            $scope.fcData.stDate = label;
            $scope.fcData.daily = true;
            $scope.dcOptions.caption = "Daily trends in " + FormatDate_MMM_YYYY(constructDate(label));
            reportService.getFChartData($scope.fcData).then(function (data) {
                $scope.dailyChartData = data.data;
                $scope.dcLabel = getFCCategories(data.data, "mmm dd, yyyy");

                $scope.dcData_l = [];
                $scope.dcData_l[0] = getFCSeries(data.data, 0, undefined, colType, true, false, undefined, false, true);
                $scope.dtData_l = getTableData(data.data, [0]);
                $scope.dsData_l = constructSummaryString(data.data, [0], [1,2,3,4,5,6], [logins]);

                $scope.dcData_i = [];
                $scope.dcData_i[0] = getFCSeries(data.data, 1, issues, stkColType, true, false, undefined, false, true);
                $scope.dcData_i[1] = getFCSeries(data.data, 2, receipts, stkColType, true, false, undefined, false, true);
                $scope.dcData_i[2] = getFCSeries(data.data, 3, stkCnt, stkColType, true, false, undefined, false, true);
                $scope.dcData_i[3] = getFCSeries(data.data, 4, discards, stkColType, true, false, undefined, false, true);
                $scope.dtData_i = getTableData(data.data, [1, 2, 3, 4]);
                $scope.dsData_i = constructSummaryString(data.data, [1,2,3,4], [0,5,6], [issues,receipts,stkCnt,discards]);

                $scope.dcData_o = [];
                $scope.dcData_o[0] = getFCSeries(data.data, 5, undefined, colType, true, false, false, undefined, false, true);
                $scope.dtData_o = getTableData(data.data, [5]);
                $scope.dsData_o = constructSummaryString(data.data, [5], [0,1,2,3,4,6], [orders]);

                $scope.dcData_r = [];
                $scope.dcData_r[0] = getFCSeries(data.data, 6, undefined, colType, true, false, false, undefined, false, true);
                $scope.dtData_r = getTableData(data.data, [6]);
                $scope.dsData_r = constructSummaryString(data.data, [6], [0,1,2,3,4,5], [revenues]);

                $scope.dcOptions.labelStep = getLabelStepValue(data.data.length);
                $scope.dLoading = false;
            }).catch(function error(msg) {
                $scope.showErrorMsg(msg);
            });
        };
        $scope.cView = 'l';
        $scope.vw = 'c';
        $scope.setCView = function (view) {
            $scope.cView = view;
            var caption = $scope.resourceBundle['report.useractivity'] + " - ";
            if($scope.cView == 'l') {
                $scope.cOptions.caption = caption + $scope.resourceBundle['logins'];
            } else if($scope.cView == 'i') {
                $scope.cOptions.caption = caption + $scope.resourceBundle['inventory'];
            } else if($scope.cView == 'o') {
                $scope.cOptions.caption = caption + $scope.resourceBundle['orders'];
            } else if($scope.cView == 'r') {
                $scope.cOptions.caption = caption + $scope.resourceBundle['revenue'];
            }
        };
        var dt = new Date();
        $scope.startDate = new Date(dt.getFullYear() - 5, dt.getMonth(), 1);
        $scope.endDate = new Date(dt.getFullYear(), dt.getMonth() + 1, 0);
        $scope.openExportOptions = function(){
            $scope.showExport = true;
        };

        $scope.closeExportOptions = function(){
            $scope.showExport = false;
        };

        $scope.exportUserActivityReports = function(type){
            if(checkNullEmpty($scope.rep) || checkNullEmpty($scope.rep.usr)){
                $scope.showFormError();
                return;
            }
            if($scope.dm == 'm'){
                $scope.frequency = "monthly";
                $scope.endDate = changeToLastDayOfMonth($scope.endDate);
            }else{
                $scope.frequency = "daily";
            }
            var stDate = FormatDate_DD_MM_YYYY($scope.startDate);
            var enDate = FormatDate_DD_MM_YYYY($scope.endDate);
            $scope.filterMap = {};
            $scope.mapVal = [];
            $scope.mapVal.push($scope.rep.usr.id);
            $scope.filterMap.usr = $scope.mapVal;
            exportService.exportReport(type,stDate,enDate,$scope.frequency,$scope.filterMap).then(function(data){
                $scope.showSuccess($scope.resourceBundle['export.success1'] + ' ' + $scope.mailId + ' ' + $scope.resourceBundle['export.success2'] + ' ' + $scope.resourceBundle['exportstatusinfo2'] + ' ' + data.data + '. ' + $scope.resourceBundle['exportstatusinfo1']);
            }).catch(function error(msg){
                $scope.showErrorMsg(msg);
            });
        }
    }
]);
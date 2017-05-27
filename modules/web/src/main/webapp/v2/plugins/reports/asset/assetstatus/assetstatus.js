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
 * Created by Mohan Raja on 23/02/17.
 */

registerWidget('aas', 'rpt-asset-status', 'Assets', 'Asset status','asset/assetstatus');

(function () {
    'use strict';

    var reportType = 'aas';

    reportsPluginCore.directive('rptAssetStatus', function () {
        return {
            restrict: 'E',
            templateUrl: 'plugins/reports/asset/assetstatus/assetstatus.html'
        };
    });

    reportsPluginCore.controller('rptAssetStatusController', ReportAssetStatusController);

    ReportAssetStatusController.$inject = ['$scope', '$timeout','reportsServiceCore','domainCfgService'];

    function ReportAssetStatusController($scope, $timeout, reportsServiceCore,domainCfgService) {

        AssetReportController.call(this, $scope, $timeout, getData);

        $scope.cType = "mscolumn2d";
        $scope.cOptions.exportFileName = "AssetStatus" + "_" + FormatDate_DD_MM_YYYY($scope.today);

        domainCfgService.getAssetWorkingStatusSysCfg().then(function(data){
            var status = data.data;
            for(var s in status) {
                $scope.primaryMetric.push({name: status[s], value: s});
            }
        });

        $scope.secondaryMetric.push({name: 'Number of times assets changed to this state', value: '0'});

        $scope.metricHeadings.push({name: $scope.resourceBundle['overview'], code: "ot"});
        $scope.metricHeadings.push({name: $scope.resourceBundle['by.upper']+" "+$scope.resourceBundle['locations.lower'], code: "rt"});
        $scope.metricHeadings.push({name: $scope.resourceBundle['by.upper']+" "+$scope.resourceBundle['manufacturer.lower'], code: "mnt"});
        $scope.metricHeadings.push({name: $scope.resourceBundle['by.upper']+" "+$scope.resourceBundle['model.lower'], code: "amt"});

        $scope.downloadAsCSV = function (daily) {
            if(daily) {
                var data = $scope.dtableData;
                var heading = $scope.dtableHeading;
            } else {
                data = $scope.tableData;
                heading = $scope.tableHeading;
            }
            var fileName = "Asset_Status" + formatDate2Url($scope.filter.from) +"_"+ formatDate2Url($scope.filter.to);
            $scope.exportAsCSV(data, heading, fileName);
        };

        $scope.downloadTableAsCSV = function () {
            var data = $scope.tableData;
            var heading = $scope.tableCSVHeading;
            var fileName = "Asset_Status" + formatDate2Url($scope.filter.from) +"_"+ formatDate2Url($scope.filter.to);
            $scope.exportAsCSV(data, heading, fileName, $scope.tableSeriesNo);
        };

        $scope.$watch("metrics.primary",function(newValue,oldValue){
            if(newValue != oldValue) {
                if($scope.activeMetric == 'ot'){
                    setChartData();
                } else{
                    setTableMeta();
                }
            }
        });

        $scope.$watch("metrics.secondary",function(newValue,oldValue){
            if(newValue != oldValue) {
                if($scope.activeMetric == 'ot'){
                    setChartData();
                } else {
                    setTableMeta();
                }
            }
        });

        $scope.applyFilter();

        function setTableMeta() {
            $scope.tableSeriesNo = parseInt($scope.metrics.secondary) * $scope.primaryMetric.length
                + parseInt($scope.metrics.primary * 1);
            $scope.tableMetric = $scope.primaryMetric[$scope.metrics.primary - 1].name
                + " - " + $scope.secondaryMetric[$scope.metrics.secondary].name;
        }

        function getData() {
            var selectedFilters = $scope.populateFilters();
            selectedFilters['type'] = reportType;

            if(selectedFilters['level'] == "d") {
                $scope.dLoading = true;
            } else {
                $scope.loading = true;
                $scope.cData = $scope.cLabel = $scope.chartData = undefined;
            }

            if($scope.activeMetric == 'ot') {
                $scope.showLoading();
                reportsServiceCore.getReportData(angular.toJson(selectedFilters)).then(function (data) {
                    $scope.noData = true;
                    if (checkNotNullEmpty(data.data)) {
                        var chartData = angular.fromJson(data.data);
                        chartData = sortByKeyDesc(chartData, 'label');
                        if (selectedFilters['level'] == "d") {
                            $scope.allDChartData = chartData;
                        } else {
                            $scope.allChartData = chartData;
                        }
                        setChartData(true, chartData, selectedFilters.level);
                        $scope.noData = false;
                    }
                }).catch(function error(msg) {
                }).finally(function () {
                    if (selectedFilters['level'] == "d") {
                        $scope.dLoading = false;
                    } else {
                        $scope.loading = false;
                    }
                    $scope.hideLoading();
                });
            } else {
                selectedFilters['viewtype'] = $scope.activeMetric;
                selectedFilters['s'] = $scope.size;
                selectedFilters['o'] = $scope.offset;
                $scope.noData = true;
                $scope.tableHideNext = false;
                $scope.showLoading();
                reportsServiceCore.getReportBreakdownData(angular.toJson(selectedFilters)).then(function (data) {
                    if (checkNotNullEmpty(data.data)) {
                        var breakdownData = angular.fromJson(data.data);

                        setTableMeta();
                        $scope.tableCaption = $scope.getReportCaption(true);

                        $scope.tableHeading = [];
                        $scope.tableHeading = angular.copy(breakdownData.headings);
                        $scope.updateTableHeading($scope.tableHeading,$scope.getReportDateFormat());
                        $scope.tableCSVHeading = breakdownData.headings;
                        $scope.updateTableHeading($scope.tableCSVHeading, "yyyy-MM-dd");
                        $scope.tableData = sortObject(breakdownData.table);
                        formatReportTableData($scope.tableData);
                        $scope.tableDataLength = Object.keys($scope.tableData).length;
                        $scope.noData = false;
                    } else if ($scope.tableDataLength == $scope.size) {
                        $scope.tableHideNext = true;
                        $scope.noData = false;
                        $scope.offset -= $scope.size;
                    }
                }).catch(function error(msg) {
                }).finally(function(){
                    $scope.loading = false;
                    $scope.hideLoading();
                });
            }
        }

        function setChartData(localData, chartData, level) {
            if(!localData) {
                $scope.loading = true;
                chartData = angular.copy($scope.allChartData);
            }
            var linkDisabled = level == "d" || $scope.filter.periodicity == "d";
            var cLabel = getReportFCCategories(chartData,$scope.getReportDateFormat(level));
            var compareFields = [];
            angular.forEach(chartData, function (d) {
                if (compareFields.indexOf(d.value[0].value) == -1) {
                    compareFields.push(d.value[0].value);
                }
            });
            var cData = [];
            for (var i = 0; i < compareFields.length; i++) {
                cData[i] = getReportFCSeries(chartData,
                    $scope.metrics.secondary * $scope.primaryMetric.length + $scope.metrics.primary * 1, compareFields[i],
                    "column2d", linkDisabled, 0);
            }
            $scope.cOptions.caption = $scope.primaryMetric[$scope.metrics.primary - 1].name
                + " - " + $scope.secondaryMetric[$scope.metrics.secondary].name;
            $scope.cOptions.subcaption = $scope.getReportCaption();
            $scope.cOptions.yAxisName = $scope.cOptions.caption;
            if($scope.filter.periodicity != "m" && cLabel.length > 10) {
                $scope.cOptions.rotateLabels = "1";
            } else {
                $scope.cOptions.rotateLabels = undefined;
            }
            if (level == "d") {
                $scope.dcData = cData;
                $scope.dcLabel = cLabel;
                $scope.dchartData = chartData;

                $scope.dtableCaption=undefined;
                $scope.dtableHeading = [];
                $scope.dtableHeading.push("Date");
                if(checkNotNullEmpty(compareFields[0])) {
                    angular.forEach(compareFields, function (d) {
                        $scope.dtableHeading.push(d);
                    });
                } else {
                    $scope.dtableHeading.push($scope.cOptions.caption);
                }
                $scope.dtableData = getReportTableData(cData, cLabel);
                $scope.dtableDataLength = Object.keys($scope.dtableData).length;
                $scope.dcOptions = angular.copy($scope.cOptions);
                if(cLabel.length > 10) {
                    $scope.dcOptions.rotateLabels = "1";
                }
                $scope.dcOptions.rotateValues = "1";
                $scope.dcOptions.caption = $scope.getBreakDownCaption();
                $scope.dcOptions.subcaption = undefined;
                $scope.dtableMetric = $scope.dcOptions.caption;
            } else {
                $scope.cData = cData;
                $scope.cLabel = cLabel;
                $scope.chartData = chartData;
                $scope.dcData = $scope.dcLabel = $scope.dchartData = undefined;

                $scope.tableCaption=$scope.cOptions.subcaption;
                $scope.tableHeading = [];
                $scope.tableHeading.push("Date");
                if(checkNotNullEmpty(compareFields[0])) {
                    $scope.tableMetric = $scope.cOptions.caption;
                    angular.forEach(compareFields, function (d) {
                        $scope.tableHeading.push(d);
                    });
                } else {
                    $scope.tableMetric = undefined;
                    $scope.tableHeading.push($scope.cOptions.caption);
                }
                $scope.tableData = getReportTableData(cData, cLabel,$scope.getReportDateFormat());
                $scope.tableDataLength = Object.keys($scope.tableData).length;
            }
            if(!localData) {
                $timeout(function () {
                    $scope.loading = false;
                }, 200);
            }
        }
    }
})();


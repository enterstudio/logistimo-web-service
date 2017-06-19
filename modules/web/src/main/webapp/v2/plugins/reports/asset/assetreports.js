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

function AssetReportController(s, timeout, getData) {
    s.resourceBundle = s.$parent.$parent.resourceBundle;
    s.ddist = s.$parent.$parent.ddist;
    s.dstate = s.$parent.$parent.dstate;
    s.showLoading = s.$parent.$parent.showLoading;
    s.hideLoading = s.$parent.$parent.hideLoading;
    s.exportAsCSV = s.$parent.$parent.exportAsCSV;

    s.MAX_MONTHS = 11;
    s.MAX_WEEKS = 15;
    s.MAX_DAYS = 31;

    s.PAGE_SIZE = 50;

    s.today = new Date();
    s.today.setHours(0,0,0,0);

    s.cHeight = "400";
    s.cWidth = "800";

    s.mandatoryFilters = {};
    s.metricHeadings = [];
    s.primaryMetric = [];
    s.secondaryMetric = [];
    s.tertiaryMetric = [];
    s.filterLabels = {};

    s.activeMetric = "ot";

    s.vw = "c";

    s.hideFilter = false;
    s.noData = true;

    s.cOptions = {
        "exportEnabled": "1",
        "theme": "fint",
        "subCaptionFontSize":10,
        "yAxisNamePadding":20,
        "rotateValues":"0",
        "placevaluesInside":0,
        "valueFontColor": "#000000"
    };

    if(s.reportStartDate) {
        s.fromTooltip = "This report has been enabled from June 2017";
    }

    s.updateTableHeading = function(tableHeading, dateFormat) {
        dateFormat = dateFormat || "mmm dd, yyyy";
        if (checkNotNullEmpty(tableHeading) && tableHeading.length > 0) {
            if (tableHeading[0] == 'kid') {
                tableHeading[0] = s.resourceBundle['kiosk'];
            } else if (tableHeading[0] == 'mid') {
                tableHeading[0] = s.resourceBundle['material'];
            } else if (tableHeading[0].toLowerCase() == 'ktag') {
                tableHeading[0] = s.resourceBundle['kiosk'] + ' ' + s.resourceBundle['tag.lower'];
            } else if (tableHeading[0].toLowerCase() == 'atype') {
                tableHeading[0] = s.resourceBundle['asset'] + ' ' + s.resourceBundle['type'];
            } else if (tableHeading[0].toLowerCase() == 'vid') {
                tableHeading[0] = s.resourceBundle['manufacturer'];
            } else if (tableHeading[0].toLowerCase() == 'dmodel') {
                tableHeading[0] = s.resourceBundle['asset.model'];
            } else if (tableHeading[0].toLowerCase() == 'dvid') {
                tableHeading[0] = s.resourceBundle['asset.serial.number'];
            } else {
                tableHeading[0] = tableHeading[0].charAt(0).toUpperCase() + tableHeading[0].slice(1);
            }
            for (var i = 1; i < tableHeading.length; i++) {
                tableHeading[i] = formatDateLabel(tableHeading[i], dateFormat);
            }
        }
    };

    s.setMetric = function (metric) {
        s.activeMetric = metric;
        s.qFilter = angular.copy(s.filter);
        s.size = s.PAGE_SIZE;
        s.offset = 0;
        if(metric == 'ot') {
            checkFromDate();
        } else {
            s.filter.fromCopy = undefined;
        }
        s.tableDataLength = 0;
        getData();
    };

    s.openFilters = function () {
        s.hideFilter = !s.hideFilter;
        copyFilters();
    };

    s.cancel = function() {
        restoreFilter();
        s.isCancel = true;
        s.hideFilter = true;
        timeout(function(){
            s.isCancel = false;
        }, 500);
    };

    s.resetFilters = function() {
        s.filter = {};
        s.compare = {type:undefined};
        s.metrics = {};
        resetMetrics();
        s.cards = {};
        s.filter.periodicity = "m";
        s.dateMode = 'month';
        var fromDate = new Date();
        fromDate.setHours(0,0,0,0);
        fromDate.setDate(1);
        s.filter.to = new Date(fromDate);
        fromDate.setMonth(fromDate.getMonth() - s.MAX_MONTHS);
        s.filter.from = getReportFromDate(fromDate);
        s.skipDateWarn = true;
        s.maxDate = new Date();
        s.size = s.PAGE_SIZE;
        s.offset = 0;
        if(s.defaultFilters){
            s.defaultFilters();
        }
    };
    s.resetFilters();

    s.changeSubHeading = function (group, heading) {
        if(s.cards.mc != heading && group == 'm') {
            s.cards.mc = heading;
            s.filter.mt = s.filter.at = undefined;
            if(s.defaultFilters){
                s.defaultFilters();
            }
        } else if(s.cards.mfc != heading && group == 'mf') {
            s.cards.mfc = heading;
            s.filter.mf = s.filter.mm = undefined;
        } else if(s.cards.lc != heading && group == 'l') {
            s.cards.lc = heading;
            s.filter.st = s.filter.dis = s.filter.tlk = s.filter.cty = undefined;
        } else if(s.cards.ec != heading && group == 'e') {
            s.compare.type = undefined;
            s.cards.ec = heading;
            s.filter.entity = s.filter.etag = undefined;
        }
    };

    s.fetchNext = function() {
        s.offset += s.size;
        getData();
    };

    s.fetchPrev = function() {
        s.offset -= s.size;
        getData();
    };

    s.fetchFirst = function() {
        s.offset = 0;
        getData();
    };

    s.populateFilters = function() {
        var selectedFilters = angular.copy(s.qFilter);
        for (var filter in selectedFilters) {
            if (checkNullEmpty(selectedFilters[filter])) {
                delete selectedFilters[filter];
            }
        }
        angular.forEach(selectedFilters, function(value,key){
            if(checkNotNullEmpty(value)) {
                if (key == "from" || key == "to") {
                    selectedFilters[key] = formatDate2Url(value);
                } else if (key == "entity" || key == "etag") {
                    selectedFilters[key] = value.id;
                } else if(key == "st") {
                    selectedFilters["cn"] = value.country;
                    selectedFilters["st"] = value.label;
                } else if(key == "dis") {
                    selectedFilters["cn"] = value.country;
                    selectedFilters["st"] = value.state;
                    selectedFilters["dis"] = value.label;
                } else if(key == "tlk") {
                    selectedFilters["cn"] = value.country;
                    selectedFilters["st"] = value.state;
                    selectedFilters["dis"] = value.district;
                    selectedFilters["tlk"] = value.label;
                } else if(key == "cty") {
                    selectedFilters["cn"] = value.country;
                    selectedFilters["st"] = value.state;
                    selectedFilters["dis"] = value.district;
                    selectedFilters["tlk"] = value.taluk;
                    selectedFilters["cty"] = value.label;
                } else if(key == "at") {
                    selectedFilters[key] = value.id;
                } else if(key == "mf") {
                    selectedFilters[key] = value.id;
                } else if(key == "mt" || key == "mm" || key == "myear") {
                    selectedFilters[key] = value;
                }
            }
        });
        selectedFilters['compare']='none';
        return selectedFilters;
    };

    s.applyFilter = function () {
        if(s.validate) {
            if(!s.validate()) {
                return;
            }
        }
        resetMetrics();
        s.hideMetricHeadings = {};
        filterHeadings();
        s.qFilter = angular.copy(s.filter);
        s.tableData = undefined;
        s.tableDataLength = undefined;
        s.offset = 0;
        getData();
        updateLabels();
        s.openFilters();
        setHeight();
    };

    function filterHeadings() {
        if(checkNotNullEmpty(s.filter['entity'])) {
            s.hideMetricHeadings['kt'] = true;
            if(s.activeMetric == 'kt'){
                s.activeMetric = 'ot';
            }
        }
        if(checkNotNullEmpty(s.filter['at'])) {
            s.hideMetricHeadings['att'] = true;
            if(s.activeMetric == 'att'){
                s.activeMetric = 'ot';
            }
        }
        if(checkNotNullEmpty(s.filter['entity']) ||
            checkNotNullEmpty(s.filter['etag']) || checkNotNullEmpty(s.filter['mf'])
            || checkNotNullEmpty(s.filter['myear']) || checkNotNullEmpty(s.filter['mm'])) {
            s.hideMetricHeadings['ktt'] = true;
            if(s.activeMetric == 'ktt'){
                s.activeMetric = 'ot';
            }
        }
        if(checkNotNullEmpty(s.filter['mm'])) {
            s.hideMetricHeadings['amt'] = true;
            if(s.activeMetric == 'amt'){
                s.activeMetric = 'ot';
            }
        }
        if(checkNotNullEmpty(s.filter['mf'])) {
            s.hideMetricHeadings['mnt'] = true;
            if(s.activeMetric == 'mnt'){
                s.activeMetric = 'ot';
            }
        }
        if(checkNotNullEmpty(s.filter['entity']) || checkNotNullEmpty(s.filter['st'])
            || checkNotNullEmpty(s.filter['dis']) ||
            checkNotNullEmpty(s.filter['tlk']) || checkNotNullEmpty(s.filter['cty'])) {
            s.hideMetricHeadings['rt'] = true;
            if(s.activeMetric == 'rt'){
                s.activeMetric = 'ot';
            }
        }
    }

    s.getFilterLabel = function() {
        var label = '';
        for(var i in s.filterLabels){
            if(checkNotNullEmpty(s.filterLabels[i]) && i != "Periodicity") {
                label += i + ": " + s.filterLabels[i] + "   ";
            }
        }
        return label;
    };

    // Current custom fusion chart directive will call only this method in case of drilling down to daily from monthly.
    s.getDFChartData =  function(label) {
        s.qFilter = angular.copy(s.filter);
        s.qFilter.level = "d";
        s.qFilter.levelPeriodicity = angular.copy(s.qFilter.periodicity);
        s.qFilter.periodicity = "d";
        s.qFilter.from = parseUrlDate(label);
        getData();
    };

    s.isUndef = function(value) {
        return (value == undefined || value == '');
    };

    s.formatReportDate = function(date) {
        if(s.filter.periodicity == "m") {
            return FormatDate_MMM_YYYY(date);
        } else {
            return FormatDate_MMM_DD_YYYY(date);
        }
    };

    s.getReportDateFormat = function(level) {
        return (s.filter.periodicity == "m" && level == undefined) ? "mmm yyyy" : undefined;
    };

    s.getBreakDownCaption = function() {
        if(s.filter.periodicity == "m") {
            return 'Daily trends in ' + FormatDate_MMM_YYYY(s.qFilter.from)
        } else {
            return 'Daily trends in the week of ' + FormatDate_MMM_DD_YYYY(s.qFilter.from)
        }
    };

    s.getReportCaption = function(isTable) {
        if(isTable) {
            var fromDate = new Date(s.filter.to);
            if(s.qFilter.periodicity == "m") {
                fromDate.setMonth(fromDate.getMonth() - 2);
            } else if(s.qFilter.periodicity == "w") {
                fromDate.setDate(fromDate.getDate() - 3 * 7);
            } else {
                fromDate.setDate(fromDate.getDate() - 6);
            }
            return "From: " + s.formatReportDate(fromDate) + "   To: " + s.formatReportDate(s.filter.to) + "   " + s.getFilterLabel()
        } else {
            return "From: " + s.formatReportDate(s.filter.from) + "   To: " + s.formatReportDate(s.filter.to) + "   " + s.getFilterLabel()
        }
    };

    s.$watch("filter.to",function(newValue,oldValue){
        if(newValue.getTime() != oldValue.getTime()) {
            if(s.hideFilter && s.tempFilters['filter']['to'].getTime() != newValue.getTime()) {
                s.hideFilter = false;
                copyFilters();
                s.tempFilters['filter']['to'] = oldValue;
            }
        }
    });

    function getReportFromDate(fromDate) {
        if(s.reportStartDate && fromDate.getTime() < s.reportStartDate.getTime()) {
            fromDate = angular.copy(s.reportStartDate);
            if(s.filter.periodicity == 'w') {
                fromDate.setDate(fromDate.getDate() + (fromDate.getDay() == 0 ? -6 : 1 - fromDate.getDay()));
            }
        }
        return fromDate;
    }

    s.$watch("filter.periodicity",function(newValue,oldValue){
        if(newValue != oldValue) {
            if(s.hideFilter && s.tempFilters['filter']['periodicity'] != newValue) {
                s.hideFilter = false;
                copyFilters();
                s.tempFilters['filter']['periodicity'] = oldValue;
            }
            if(!s.isCancel) {
                var fromDate = new Date();
                fromDate.setHours(0,0,0,0);
                if (newValue == "m") {
                    s.dateMode = 'month';
                    fromDate.setDate(1);
                    fromDate.setMonth(fromDate.getMonth() - s.MAX_MONTHS);
                    s.filter.from = getReportFromDate(fromDate);
                    var toDate = new Date();
                    toDate.setHours(0,0,0,0);
                    toDate.setDate(1);
                    s.filter.to = toDate;
                } else if (newValue == 'w') {
                    fromDate.setDate(fromDate.getDate() - s.MAX_WEEKS * 7);
                    fromDate.setDate(fromDate.getDate() + (fromDate.getDay() == 0 ? -6 : 1 - fromDate.getDay()));
                    s.filter.from = getReportFromDate(fromDate);
                    toDate = new Date();
                    toDate.setHours(0,0,0,0);
                    toDate.setDate(toDate.getDate() + (toDate.getDay() == 0 ? -6 : 1 - toDate.getDay()));
                    s.filter.to = toDate;
                    s.dateMode = 'week';
                } else {
                    fromDate.setDate(fromDate.getDate() - s.MAX_DAYS + 1);
                    s.filter.from = getReportFromDate(fromDate);
                    s.filter.to = new Date();
                    s.filter.to.setHours(0,0,0,0);
                    s.dateMode = 'day';
                }
                s.skipDateWarn = true;
            } else {
                if (newValue == "m") {
                    s.dateMode = 'month';
                } else if (newValue == 'w') {
                    s.dateMode = 'week';
                } else {
                    s.dateMode = 'day';
                }
            }
        }
    });

    s.$watch("filter.from",function(newValue,oldValue){
        if(s.activeMetric == 'ot') {
            s.filter.fromCopy = newValue;
        }
        if(newValue.getTime() != oldValue.getTime()) {
            if(s.hideFilter && s.tempFilters['filter']['from'].getTime() != newValue.getTime()) {
                s.hideFilter = false;
                copyFilters();
                s.tempFilters['filter']['from'] = oldValue;
            }
            var toDate = new Date(newValue);
            //if to date is beyond limit, reset to limit
            if (s.filter.periodicity == 'm') {
                toDate.setMonth(toDate.getMonth() + s.MAX_MONTHS);
                if(toDate.getTime() > s.today.getTime()) {
                    toDate = new Date(s.today);
                    toDate.setDate(1);
                }
            } else if (s.filter.periodicity == 'w') {
                toDate.setDate(toDate.getDate() + s.MAX_WEEKS * 7);
                if(toDate.getTime() > s.today.getTime()) {
                    toDate = new Date(s.today);
                    toDate.setDate(toDate.getDate() + (toDate.getDay() == 0 ? -6 : 1 - toDate.getDay()));
                }
            } else {
                toDate.setDate(toDate.getDate() + s.MAX_DAYS - 1);
                if(toDate.getTime() > s.today.getTime()) {
                    toDate = new Date(s.today);
                }
            }
            s.maxDate = new Date(toDate);
            setToDate(toDate);
        }
        s.skipDateWarn = false;
    });

    function copyFilters() {
        s.tempFilters = {};
        s.tempFilters['filter'] = angular.copy(s.filter);
        s.tempFilters['compare'] = angular.copy(s.compare);
        s.tempFilters['cards'] = angular.copy(s.cards);
    }

    function restoreFilter() {
        if(checkNotNullEmpty(s.tempFilters)) {
            s.filter = angular.copy(s.tempFilters['filter']);
            s.compare = angular.copy(s.tempFilters['compare']);
            s.cards = angular.copy(s.tempFilters['cards']);
        }
    }

    function checkFromDate() {
        if(s.filter.from.getTime() > s.filter.to.getTime()) {
            s.filter.from = angular.copy(s.filter.to);
            if (s.filter.periodicity == 'm') {
                s.filter.from.setMonth(s.filter.from.getMonth() - s.MAX_MONTHS);
            } else if (s.filter.periodicity == 'w') {
                s.filter.from.setDate(s.filter.from.getDate() - s.MAX_WEEKS * 7);
            } else {
                s.filter.from.setDate(s.filter.from.getDate() - s.MAX_DAYS);
            }
            s.filter.from = getReportFromDate(s.filter.from);
        }
    }

    function setHeight() {
        timeout(function() {
            var top = $("#fixedMenu").height() + $("#fixedfilter").height();
            $('.report-data-pane').css("margin-top", top + 40);
        }, 300);
    }
    setHeight();
    
    function resetMetrics() {
        s.metrics.primary = "1";
        s.metrics.secondary = "0";
        s.metrics.tertiary = "0";
        s.tableSeriesNo = 1;
    }
    
    function setToDate(date) {
        if(s.filter.from.getTime() > s.filter.to.getTime()){
            s.filter.to = new Date(s.filter.from);
        } else if(date.getTime() < s.filter.to.getTime()) {
            if(s.filter.to.getTime() != date.getTime()) {
                s.filter.to = new Date(date);
                if (!s.skipDateWarn) {
                    var period = ' days';
                    var maxPeriod = s.MAX_DAYS;
                    if (s.filter.periodicity == 'm') {
                        period = ' months';
                        maxPeriod = s.MAX_MONTHS + 1;
                    } else if (s.filter.periodicity == 'w') {
                        period = ' weeks';
                        maxPeriod = s.MAX_WEEKS;
                    }
                    toastr.warning('<b>From</b> and <b>To</b> periods should be in a range of ' + maxPeriod + period + '. <b>To</b> date has been adjusted to fit within the range.');
                }
            }
        }
    }

    function updateLabels() {
        var labels = {m: "monthly", w: "weekly", d: "daily"};
        s.filterLabels[s.resourceBundle['periodicity']] =
            labels[s.filter.periodicity] ? s.resourceBundle[labels[s.filter.periodicity]] : undefined;
        if(s.filter.mt) {
            s.filterLabels['Monitoring type'] = s.filter.mt == 1 ? 'Monitoring' : 'Monitored';
        } else {
            s.filterLabels['Monitoring type'] = undefined;
        }
        s.filterLabels['Asset type'] = s.filter.at ? s.filter.at.text : undefined;
        s.filterLabels['Manufacturer'] = s.filter.mf ? s.filter.mf.text : undefined;
        s.filterLabels['Model'] = s.filter.mm;
        s.filterLabels['Manufacturing year'] = s.filter.myear;
        s.filterLabels[s.resourceBundle['kiosk']] = s.filter.entity ? s.filter.entity.nm : undefined;
        s.filterLabels[s.resourceBundle['tagentity']] = s.filter.etag?s.filter.etag.text:undefined;
        s.filterLabels[s.resourceBundle['state']] = s.filter.st?s.filter.st.label:undefined;
        s.filterLabels[s.resourceBundle['district']] = s.filter.dis?s.filter.dis.label + ", " + s.filter.dis.subLabel : undefined;
        s.filterLabels[s.resourceBundle['taluk']] = s.filter.tlk?s.filter.tlk.label + ", " + s.filter.tlk.subLabel : undefined;
        s.filterLabels[s.resourceBundle['city']] = s.filter.cty?s.filter.cty.label + ", " + s.filter.cty.subLabel : undefined;
    }
    updateLabels();

    function getDataNotAvailableText() {
        if(checkNullEmpty(s.dstate)) {
            s.noDataText = s.resourceBundle['filter.state.missing'];
        } else if (checkNullEmpty(s.ddist)) {
            s.noDataText = s.resourceBundle['filter.district.missing'];
        } else {
            s.noDataText = s.resourceBundle['filter.taluk.missing'];
        }
    }
    getDataNotAvailableText();
    
}
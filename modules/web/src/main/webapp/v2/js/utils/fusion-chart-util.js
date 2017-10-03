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
 * @author Mohan Raja
 */
function getFCCategories(data, format) {
    if (checkNotNullEmpty(data)) {
        var category = [];
        var ind = 0;
        for (var i = data.length - 1; i >= 0; i--) {
            var t = {};
            t.label = formatLabel(data[i].label, format);
            category[ind++] = t;
        }
        return category;
    }
}
function formatLabel(value, format) {
    switch (format) {
        case "mmm yyyy":
            return FormatDate_MMM_YYYY(constructDate(value));
            break;
        case "mmm dd, yyyy":
            return FormatDate_MMM_DD_YYYY(constructDate(value));
            break;
        case "mmm dd, yy H:MM":
            return FormatDate_MMM_DD_YY_H_MM(constructDate(value));
            break;
        default:
            return value;
    }
}
function constructDate(label,ignoreTime) {
    if (typeof label == "string" && label.indexOf("-") > 0) {
        var dateSplit = label.split("-");
        if(ignoreTime) {
            var index = dateSplit[2].indexOf('T');
            if (index >= 0) {
                dateSplit[2] = dateSplit[2].substring(0, index);
            }
        }
        return new Date(dateSplit[0] + "/" + dateSplit[1] + "/" + dateSplit[2]);
    } else {
        return new Date(label);
    }
}
function getFCCategoriesFromTempJSON(data) {
    if (data.length > 0) {
        var category = [];
        var ind = 0;
        for (var i = data.length - 1; i >= 0; i--) {
            var t = {};
            t.label = data[i].label;
            category[ind++] = t;
        }
        return category;
    }
}
function getFCTrend(min, max, thickness) {
    if (checkNotNullEmpty(parseInt(min, 10)) && checkNotNullEmpty(parseInt(max, 10))) {
        var trendMin = {};
        trendMin.startvalue = min;
        trendMin.color = '#f9cc06';
        trendMin.valueOnRight = 1;
        trendMin.displayvalue = 'Min: ' + parseInt(min, 10);
        trendMin.thickness = thickness ? thickness : 2;

        var trendMax = {};
        trendMax.startvalue = max;
        trendMax.color = '#00c0ef';
        trendMax.valueOnRight = 1;
        trendMax.displayvalue = 'Max: ' + parseInt(max, 10);
        trendMax.thickness = thickness ? thickness : 2;

        return [trendMin, trendMax];
    }
}
function getFCSeriesFromTempJSON(data, type) {
    if (data.length > 0) {
        var series = {};
        series.data = [];
        series.renderAs = type;
        var ind = 0;
        for (var i = data.length - 1; i >= 0; i--) {
            var t = {};
            t.value = data[i].tmp;
            series.data[ind++] = t;
        }
        return series;
    }
}
function getFCSeries(data, seriesno, name, type, isLinkDisabled, showvalue, color, noAnchor, zeroWithEmpty, forceSum, skipSeriesInLabel) {
    if (checkNotNullEmpty(data) && data[0]) {
        if (data[0].value.length > seriesno) {
            var series = {};
            series.seriesName = name;
            series.renderAs = type;
            series.showValues = showvalue ? showvalue : "0";
            series.drawAnchors = noAnchor ? "0" : "1";
            series.data = [];
            var ind = 0;
            for (var i = data.length - 1; i >= 0; i--) {
                var t = {};
                t.value = data[i].value[seriesno];
                if (zeroWithEmpty && (t.value == "0" || t.value == "0.0")) {
                    t.value = "";
                }
                var dec = checkNotNullEmpty(t.value) ? t.value.indexOf(".") : -1;
                if (dec >= 0 && parseInt(t.value.substr(dec + 1)) == 0) {
                    t.value = t.value.substr(0,dec);
                }
                if (!isLinkDisabled) {
                    t.link = "JavaScript: angular.element(document.getElementById('cid')).scope().getDFChartData('" + data[i].label + "');";
                }
                if (color) {
                    t.color = color;
                }
                if(name && !skipSeriesInLabel) {
                    t.toolText = "$seriesName, ";
                }
                if (forceSum || (checkNotNullEmpty(type) && (type.indexOf("Pie") == 0 || type.indexOf("Doughnut") == 0))) {
                    t.toolText = (t.toolText? t.toolText : "" ) + "$label: $value of $unformattedSum";
                } else {
                    t.toolText = (t.toolText? t.toolText : "" ) + "$label: $value";
                }
                series.data[ind++] = t;
            }
            return series;
        }
    }
}
function getDataAsCSV(data, heading) {
    var comma = ",";
    var nl = "\r\n";
    var csvData = heading.join(comma);
    csvData += nl;
    for (var i = data.length - 1; i >= 0; i--) {
        csvData += data[i].label + comma;
        csvData += data[i].value.join(comma);
        csvData += nl;
    }
    return csvData;
}
function getTableData(data, series, format) {
    if (checkNotNullEmpty(data)) {
        var tData = [];
        var ind = 0;
        var fZero = "0.0";
        var zero = "0";
        for (var i = data.length - 1; i >= 0; i--) {
            var tRow = [];
            switch (format) {
                case "mmm yyyy":
                    tRow[0] = FormatDate_MMM_YYYY(constructDate(data[i].label));
                    break;
                default:
                    tRow[0] = FormatDate_DD_MM_YYYY(constructDate(data[i].label));
            }
            var ri = 1;
            for (var j = 0; j < series.length; j++) {
                tRow[ri++] = data[i].value[series[j]] == fZero ? zero : parseFloat(data[i].value[series[j]]);
            }
            tData[ind++] = tRow;
        }
        return tData;
    }
}
function getSummary(data) {
    var sumData = [];
    if (data.length > 0) {
        var nos = data[0].value.length;
        for (var i = 0; i < nos; i++) {
            sumData[i] = 0;
        }
        for (i = 0; i < data.length; i++) {
            for (var j = 0; j < nos; j++) {
                sumData[j] += data[i].value[j] * 1;
            }
        }
    }
    return sumData;
}
function getSeriesAverage(data, series) {
    var sum = 0;
    var noi = 0;
    for (var i = 0; i < data.length; i++) {
        var val = parseFloat(data[i].value[series]);
        if (val > 0) {
            sum += val;
            noi++;
        }
    }
    if (noi > 0) {
        return parseFloat(sum / noi).toFixed(2);
    } else {
        return 0;
    }
}
function getSummaryTable(data, labels) {
    var sumTable = [];
    var sumRow = [];
    for (var i = 0; i < labels.length + 1; i++) {
        sumRow[i] = 0;
    }
    for (i = 0; i < data.length; i++) {
        for (var j = 0; j < labels.length; j++) {
            sumRow[j] += parseInt(data[i].value[j])
        }
    }
    var total = 0;
    for (i = 0; i < labels.length; i++) {
        sumTable[i] = [labels[i], sumRow[i]];
        total += sumRow[i];
    }
    sumTable[labels.length] = ["Total", total];
    return sumTable;
}
function getSummaryFCData(data) {
    var available = false;
    var tData = [];
    for (var i = 0; i < data.length - 1; i++) {
        var row = {};
        row.label = data[i][0];
        row.value = data[i][1];
        tData[i] = row;
        if(row.value > 0) {
            available = true;
        }
    }
    return available ? tData : undefined;
}
function getFCData(data, seriesno, labelFormat, zeroWithEmpty) {
    var tData = [];
    var ind = 0;
    for (var i = data.length - 1; i >= 0; i--) {
        var row = {};
        row.label = formatLabel(data[i].label, labelFormat);
        row.value = data[i].value[seriesno];
        if (zeroWithEmpty && (row.value == "0" || row.value == "0.0")) {
            row.value = "";
        }
        tData[ind++] = row;
    }
    return tData;
}
function getSummaryFCDataTotal(data) {
    var total = 0;
    for (var i = 0; i < data.length - 1; i++) {
        total += data[i][1];
    }
    return total;
}
/**
 *
 * @param data
 * @param zCol - If zero, ignore column
 * @param iCol - Ignore columns
 * @param labels
 * @returns {string}
 */
function constructSummaryString(data, zCol, iCol, labels) {
    var comma = ",";
    var space = " ";
    var sumData = getSummary(data);
    var summary = "";
    var isFirst = true;
    var lblIndex = 0;
    for (var i = 0; i < sumData.length; i++) {
        if (iCol.indexOf(i) == -1) {
            if (sumData[i] > 0 || zCol.indexOf(i) == -1) {
                if (isFirst) {
                    isFirst = false;
                } else {
                    summary += comma + space;
                }
                summary += sumData[i] + space + labels[lblIndex];
            }
            lblIndex++;
        }
    }
    return summary;
}
function getLabelStepValue(size) {
    return Math.ceil(size / 9);
}
function getShortMonth(month) {
    var shortMonth = "Jan_Feb_Mar_Apr_May_Jun_Jul_Aug_Sep_Oct_Nov_Dec".split("_");
    return shortMonth[month];
}

function getMonth(month) {
    var shortMonth = "Jan_Feb_Mar_Apr_May_Jun_Jul_Aug_Sep_Oct_Nov_Dec".split("_");
    return shortMonth.indexOf(month) + 1;
}

function getFullMonth(month) {
    var fullMonth = "January_February_March_April_May_June_July_August_September_October_November_December".split("_");
    return fullMonth[month];
}
function getNoWithZero(no) {
    var nos = "00_01_02_03_04_05_06_07_08_09".split("_");
    return no < 10 ? nos[no] : no;
}
/**
 * @return {string}
 */
function FormatDate_MMM_YYYY(date) {
    return checkNotNullEmpty(date) ? getShortMonth(date.getMonth()) + " " + date.getFullYear() : "";
}
/**
 * @return {string}
 */
function FormatDate_MMMM_YYYY(date) {
    return checkNotNullEmpty(date) ? getFullMonth(date.getMonth()) + " " + date.getFullYear() : "";
}
/**
 * @return {string}
 */
function FormatDate_MMMM_DD_YYYY(date) {
    return checkNotNullEmpty(date) ? getFullMonth(date.getMonth()) + " " + getNoWithZero(date.getDate()) + ", " + date.getFullYear() : "";
}
/**
 * @return {string}
 */
function FormatDate_MMM_DD_YYYY(date) {
    return checkNotNullEmpty(date) ? getShortMonth(date.getMonth()) + " " + getNoWithZero(date.getDate()) + ", " + date.getFullYear() : "";
}
/**
 * @return {string}
 */
function FormatDate_MMM_DD_YY_H_MM(date) {
    return checkNotNullEmpty(date) ? getShortMonth(date.getMonth()) + " " + getNoWithZero(date.getDate()) + ", " + date.getFullYear() + " " + date.getHours() + ":" + getNoWithZero(date.getMinutes()) : "";
}
/**
 * @return {string}
 */
function FormatDate_DD_MM_YYYY(date) {
    return checkNotNullEmpty(date) ? getNoWithZero(date.getDate()) + "/" + getNoWithZero(date.getMonth() + 1) + "/" + date.getFullYear() : "";
}/**
 * @return {string}
 */
function FormatDate_YYYY_MM_DD(date) {
    return checkNotNullEmpty(date) ? date.getFullYear() + "/" + getNoWithZero(date.getMonth() + 1) + "/" + getNoWithZero(date.getDate()) : "";
}
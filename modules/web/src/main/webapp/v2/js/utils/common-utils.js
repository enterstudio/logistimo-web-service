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
 *  Generic watch function that composes url parameters if location is passed else calls fetch
 *  exclusive : Clears other request params and sets the request param. except the ones listed in retainParamsArr
 */
function watchfn(param, defaultValue, $location, $scope, callback, valuefn,exclusive,retainParamsArr) {
    return function (newval, oldval) {
        var nVal = (checkNotNullEmpty(valuefn) && checkNotNullEmpty(newval)) ? valuefn(newval) : newval;
        var oVal = (checkNotNullEmpty(valuefn) && checkNotNullEmpty(oldval)) ? valuefn(oldval) : oldval;
        if (nVal != oVal) {
            if (!checkNotNullEmpty(callback) && param != "o" && param != "s" && param != "vw") {
                if (checkNotNullEmpty($scope.clearOffset)) {
                    $scope.clearOffset();
                    $scope.maxFound = 0;
                }
            }
            if (checkNotNullEmpty($location)) {
                if (checkNotNullEmpty(exclusive) && newval != defaultValue) {
                    if(checkNotNullEmpty(retainParamsArr)){
                        var retainMap = {};
                        retainParamsArr.forEach(function(val){
                            if (checkNotNullEmpty($location.$$search[val])) {
                                retainMap[val] = $location.$$search[val];
                            }
                        });
                    }
                    $location.$$search = {};
                    if(checkNotNullEmpty(retainParamsArr)) {
                        for (var p in retainMap) {
                            $location.$$search[p] = retainMap[p];
                        }
                    }
                }
                if (checkNullEmpty(newval) || newval == defaultValue) {
                    if ($location.$$search[param]) {
                        delete $location.$$search[param];
                    }
                } else {
                    var setVal = newval;
                    if (checkNotNullEmpty(valuefn)) {
                        setVal = valuefn(newval);
                    }
                    $location.$$search[param] = setVal;
                }
                $location.$$compose();
            } else {
                if (checkNotNullEmpty(callback)) {
                    callback();
                } else {
                    $scope.fetch();
                }
            }
        }
    }
}
getVal = function (obj, attribute) {
    if (checkNotNullEmpty(attribute)) {
        var splitString = attribute.split(".");
        var val = obj[splitString[0]];
        for (var i = 1; i < splitString.length; i++) {
            if (checkNotNullEmpty(val)) {
                val = val[splitString[i]];
            }
        }
        return checkNotNullEmpty(val) ? val : 0;
    }
    return 0;
};
getPrintableArgs = function (arguments) {
    var log = "";
    for (var i = 0; i < arguments.length; i++) {
        log += arguments[i] + "\t";
    }
    return log
};
checkNotNullEmpty = function (argument) {
    return typeof argument !== 'undefined' && argument != null && argument != "";
};
checkStrictNotNullEmpty = function(argument) {
    return typeof argument !== 'undefined' && argument != null && argument !== "";
};
checkNullEmpty = function (argument) {
    return !checkNotNullEmpty(argument);
};
checkNotNull = function (argument) {
    return typeof argument !== 'undefined' && argument != null;
};
checkNull = function (argument) {
    return !checkNotNull(argument);
};

stripLastComma = function (str) {
    str = str.trim();
    if (str != "") {
        if (str.slice(-1) == ",") {
            str = str.substring(0, str.length - 1);
        }
    }
    return str;
};
concatIfNotEmpty = function (str, substr) {
    if (checkNotNullEmpty(substr)) {
        str = str.concat(substr)
            .concat(", ")
    }
    return str;
};
constructAddressStr = function (inAddress) {
    var address = "";
    if (inAddress != null) {
        address = concatIfNotEmpty(address, inAddress.str);
        address = concatIfNotEmpty(address, inAddress.ct);
        address = concatIfNotEmpty(address, inAddress.tlk);
        address = concatIfNotEmpty(address, inAddress.ds);
        address = concatIfNotEmpty(address, inAddress.st);
        address = concatIfNotEmpty(address, inAddress.ctr);
        address = concatIfNotEmpty(address, inAddress.zip);
        address = stripLastComma(address);
    }
    return address;
};
function drawStockEvent(divId, url, color, callback) {
    var query = new google.visualization.Query(url);
    query.send(function (response) {
        if (!response.isError()) {
            var data = response.getDataTable();
            var stockEventView = new google.visualization.DataView(data);
            stockEventView.hideColumns([1, 2, 3, 4, 5, 8, 9]); // hide the kiosk-name, stock, min, max, duration and Ids columns (materialId and kioskiId)
            var div = document.getElementById(divId);
            var chart = new google.visualization.Timeline(div);
            chart.draw(stockEventView, {timeline: {showRowLabels: true}, colors: [color], width: 680, height: 100});
        }
        callback();
    });
}
function formatDate(date) {
    return checkNotNullEmpty(date) ? date.getDate() + "/" + (date.getMonth() + 1) + "/" + date.getFullYear() : "";
}
function string2Date(dateString, format, delimiter, removeTime) {
    if (checkNotNullEmpty(dateString) && checkNotNullEmpty(format) && checkNotNullEmpty(delimiter)) {
        if(removeTime) {
            dateString = dateString.substring(0,dateString.indexOf(" "));
        }
        format = format.toLowerCase();
        var fFields = format.split(delimiter);
        var dFields = dateString.split(delimiter);
        var dInd = fFields.indexOf("dd");
        var mInd = fFields.indexOf("mmm");
        var smr = true;
        if(mInd == -1) {
            mInd = fFields.indexOf("mm");
            smr = false;
        }
        var yInd = fFields.indexOf("yyyy");
        var date = new Date(dFields[yInd],(smr?getMonth(dFields[mInd]):dFields[mInd])-1,dFields[dInd]);
        if (date.getFullYear() < 1970) {
            date.setFullYear(date.getFullYear() + 100);
        }
        return date;
    }
}
function formatDate2Url(newVal) {
    return checkNotNullEmpty(newVal) ? newVal.getFullYear() + "-" + (newVal.getMonth() + 1) + "-" + newVal.getDate() : "";
}

/**
 * Sets the date of the given date object to last of the month in the date.
 * @param dt
 * @returns {Date}
 */
function changeToLastDayOfMonth(dt){
    return new Date(dt.getFullYear(), dt.getMonth() + 1, 0);
}

function parseUrlDate(dateString) {
    return checkNotNullEmpty(dateString) ? constructDate(dateString) : "";
}
function getFlotData(data, min, max) {
    var d = [];
    var dmin = [];
    var dmax = [];
    for (var i = 0; i < data.length; i++) {
        var temp = data[i];
        d.push([temp.time * 1000, temp.tmp]); // Changed to milliseconds because json returns seconds
        if (min)
            dmin.push([temp.time * 1000, min]); // Changed to milliseconds because json returns seconds
        if (max)
            dmax.push([temp.time * 1000, max]); // Changed to milliseconds because json returns seconds
    }
    return [d, dmin, dmax];
}

function cleanupString(msg) {
    if(checkNotNullEmpty(msg)) {
        if(typeof msg === 'object'){
            msg = JSON.stringify(msg);
        }
        var r = /\\u([\d\w]{4})/gi;
        msg = msg.replace(r, function (match, grp) {
            return String.fromCharCode(parseInt(grp, 16));
        });
        if (msg.charAt(0) == '"' && msg.charAt(msg.length - 1) == '"') {
            msg = msg.substring(1, msg.length - 1);
        }
        return msg.replace(/\\"/g, '"');
    }
    return msg;
}

/**
 * Compare obj1 and obj2 by field
 * @param obj1
 * @param obj2
 * @param field
 */
function compareObject(obj1,obj2,field){
    if(checkNotNullEmpty(obj1) && checkNotNullEmpty(obj2)){
        if(obj1[field] == obj2[field]){
            return true;
        }else{
            return false;
        }
    }else if(checkNullEmpty(obj1) && checkNullEmpty(obj2)){
        return true;
    }
    return false;
}
function fixTable(){
    if (!$(".table-container").length) {
        setTimeout(function () {
            $("#fixTable").fixMe();
        }, 200);
    }
}

function exportCSV(csvData, fileName, $timeout) {
    var ua = window.navigator.userAgent;
    if(ua.indexOf( "MSIE ") > 0 || ua.indexOf('Trident/') > 0 || ua.indexOf('Edge/') > 0) {
        if(navigator.appVersion.indexOf("MSIE 9.0") !== -1) {
            var oWin = window.open();
            oWin.document.write('sep=,\r\n' + csvData);
            oWin.document.close();
            oWin.document.execCommand('SaveAs', true, fileName + ".csv");
            oWin.close();
        } else {
            var link = document.createElement("a");
            var blob = new Blob([csvData], { type: 'text/csv;charset=utf-8;' });
            if(navigator.msSaveBlob) {
                navigator.msSaveBlob(blob, fileName + ".csv");
            } else {
                if(link.download !== undefined) {
                    var url = URL.createObjectURL(blob);
                    link.setAttribute("href", url);
                    link.setAttribute("download", fileName + ".csv");
                    link.style.visibility = 'hidden';
                    document.body.appendChild(link);
                    $timeout(function(){link.click();}); //to avoid $apply already in progress
                    document.body.removeChild(link);
                }
            }
        }
    } else {
        var uri = "data:text/csv;charset=utf-8," + encodeURIComponent(csvData);
        var link = document.createElement("a");
        link.href = uri;
        link.style = "visibility:hidden";
        link.download = fileName + ".csv";
        document.body.appendChild(link);
        link.click();
        document.body.removeChild(link);
    }
}
checkNullEmptyObject = function(object) {
    return (checkNotNullEmpty(object) ? Object.keys(object).length : 0) == 0;
}

function checkEmail(email){
    var re = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    return re.test(email);
}

showPopup = function($scope, mat, matId, msg, index, $timeout, isAllocate, isStatus){
    if(checkNotNullEmpty(mat) && checkNotNullEmpty(matId)){
        $timeout(function () {
            if (isAllocate) {
                mat.aPopupMsg = msg;
                if (!mat.ainvalidPopup) {
                    mat.ainvalidPopup = true;
                    $scope.invalidPopup += 1;
                }
            } else if(isStatus) {
                mat.sPopupMsg = msg;
                if(!mat.sinvalidPopup) {
                    mat.sinvalidPopup = true;
                    $scope.invalidPopup += 1;
                }
            } else {
                mat.popupMsg = msg;
                if (!mat.invalidPopup) {
                    mat.invalidPopup = true;
                    $scope.invalidPopup += 1;
                }
            }
            $timeout(function () {
                $("[id='"+ matId + index + "']").trigger('showpopup');
            }, 0);
        }, 0);
    }
};

hidePopup = function ($scope, mat, matId, index, $timeout, isAllocate, isStatus){
    if (checkNotNullEmpty(mat) && checkNotNullEmpty(matId)){
        if (mat.invalidPopup) {
            $scope.invalidPopup = $scope.invalidPopup <= 0 ? 0 : $scope.invalidPopup - 1;
        }
        if (isAllocate) {
            mat.ainvalidPopup = false;
        } else if(checkNotNullEmpty(isStatus)) {
            mat.sinvalidPopup = false;
        } else {
            mat.invalidPopup = false;
        }
        $timeout(function () {
            $("[id='"+ matId + index + "']").trigger('hidepopup');

        }, 0);
    }
};


redrawPopup = function($scope,matList,type,$timeout) {
    if (type == 'hide') {
        for (var i = 0; i < matList.length; i++) {
            if(checkNotNullEmpty(matList[i].name)) {
                hidePopup($scope, matList[i], matList[i].name.mId, i, $timeout);
            } else if(checkNotNullEmpty(matList[i].mId)) {
                hidePopup($scope, matList[i], matList[i].mId, i, $timeout);
            } else{
                hidePopup($scope, matList[i], matList[i].id, i, $timeout);
                hidePopup($scope, matList[i], 'a'+matList[i].id, i, $timeout,true);
            }
        }
        $timeout(function () {
            redrawPopup($scope,matList,'show',$timeout);
        }, 0);
    } else {
        for (i = 0; i < matList.length; i++) {
            $scope.validate(matList[i], i);
        }
    }
}
function encodeURIParam(value,noEncode){
    if(checkNotNullEmpty(value)){
        var finalValue = "";
        var valArray = value.split('');
        for(var charVal in valArray){
            finalValue += '#' + valArray[charVal].charCodeAt(0);
        }
        return noEncode?finalValue:encodeURIComponent(finalValue);
    }

    return value;
}
function encodeURI (value) {
    value = value.replace("/", "_lslash_");
    return  encodeURIComponent(value);
}
function validateContact(msg, min, max) {
    var mobilePhoneRegex = new RegExp("^(\\+{1})\\d{1,4}\\s\\d{" + min + "," + max + "}$");
    var invalid = '';
    if (!msg.match(mobilePhoneRegex)) {
        if (msg.search(/\s/) == -1) {
            invalid = 's';
        } else {
            invalid = 'f';
        }
    }
    return invalid;
}
function validateMobile(phm) {
    if (checkNotNullEmpty(phm)) {
         return validateContact(phm.trim(), 1, 20);
    } else {
        return 'r'; // required
    }
}
function validateLL(phl) {
    if (checkNotNullEmpty(phl)) {
        return validateContact(phl.trim(), 0, 20);
    } else {
        return 'r'; // required
    }
}
function validateSupport(phs) {
    if (checkNullEmpty(phs)) {
        return 'r';
    }
    phs = phs.trim();
    var retVal = validateMobile(phs);
    if (retVal == 's' || retVal == 'f' ) {
        retVal = validateLL(phs);
        if (retVal == 's' || retVal == 'f') {
            retVal = validateTollFreeNumber(phs, 1, 20);
        }
    }
    return retVal;
}
function validateTollFreeNumber(msg, min, max){
    var tollFreePhoneRegex = new RegExp("^(18\\d{" + min + "," + max + "}$)"); // starting with 1800 and after it a minimum of 1 digit and max of 20 digits
    var invalid = '';
    if (!msg.match(tollFreePhoneRegex)) {
        invalid = 'f';
    }
    return invalid;
}

windowScrollTop = function(){
    window.scrollTo(0, 0);
};

startsWith = function (state, viewValue) {
    return state.substr(0, viewValue.length).toLowerCase() == viewValue.toLowerCase();
};

function sortObject (object, caseSensitive) {
    function sortFn (a, b) {
        if(caseSensitive) {
            return a.localeCompare(b);
        } else {
            return a.toLowerCase().localeCompare(b.toLowerCase());
        }
    }
    if( Array.isArray(object)) {
        return object.sort(sortFn);
    } else if(checkNotNullEmpty(object)){
        var ordered = {};
        Object.keys(object).sort(sortFn).forEach(function (key) {
            ordered[key] = object[key];
        });
        return ordered;
    }
    return undefined;
}

function constructModel(data, noText) {
    var csv = '';
    var key = (noText) ? "id" : "text";
    if (data) {
        if (data instanceof Array) {
            angular.forEach(data, function (d) {
                if (checkNotNullEmpty(csv)) {
                    csv += ",";
                }
                csv += "'" + d[key] + "'";
            });
        } else {
            csv += "'" + data[key] + "'";
        }
    }
    return csv;
}

function sortByKey(array, key) {
    return array.sort(function(a, b) {
        var x = a[key]; var y = b[key];
        return ((x < y) ? -1 : ((x > y) ? 1 : 0));
    });
}

function sortByKeyDesc(array, key) {
    return array.sort(function(a, b) {
        var x = a[key]; var y = b[key];
        return ((x > y) ? -1 : ((x < y) ? 1 : 0));
    });
}

function momentFormat(date, tz, locale) {
    if (checkNotNullEmpty(date)) {
        tmp = moment(date).tz(tz).locale(locale);
        return tmp.format('L') + " " + tmp.format('LT');
    }
    return "";
}


function momentFromNow(date, tz, locale) {
    if (checkNotNullEmpty(date)) {
        return moment(date).tz(tz).locale(locale).fromNow();
    }
    return "";
}


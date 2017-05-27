/**
 *  Generic watch function that composes url parameters if location is passed else calls fetch
 */
function watchfn(param, defaultValue, $location, $scope,callback,valuefn) {
    return function(newval, oldval) {
        if (newval != oldval) {
            if (!checkNotNullEmpty(callback) && param != "o" && param != "s" && param != "vw") {

                if(checkNotNullEmpty($scope.clearOffset)) {
                    $scope.clearOffset();
                    $scope.maxFound = 0;
                }

            }
            if (checkNotNullEmpty($location)) {
                if (checkNullEmpty(newval) || newval == defaultValue) {
                    if ($location.$$search[param]) {
                        delete $location.$$search[param];
                        $location.$$compose();
                    }
                } else {
                    var setVal = newval;
                    if(checkNotNullEmpty(valuefn)){
                        setVal = valuefn(newval);
                    }
                    $location.search(param, setVal);
                }
            }else{
                if(checkNotNullEmpty(callback)){
                    callback();
                }else{
                    $scope.fetch();
                }
            }
        }
    }
}


getVal = function(obj, attribute) {
    if (checkNotNullEmpty(attribute)) {

        var splitString = attribute.split(".");
        var val = obj[splitString[0]];
        for (i = 1; i < splitString.length; i++) {
            if(checkNotNullEmpty(val)){
                val = val[splitString[i]];
            }
        }
        return checkNotNullEmpty(val)?val:0;
    }
    return 0;
}

getPrintableArgs = function(arguments) {
    var log = "";
    for (var i=0; i < arguments.length; i++) {
        log += arguments[i] +"\t";
    }
    return log
}

checkNotNullEmpty = function(argument) {
    return typeof argument !== 'undefined' && argument != null && argument != "";
};

checkNullEmpty = function(argument) {
    return !checkNotNullEmpty(argument);
};

checkNotNull = function(argument){
    return typeof argument !== 'undefined' && argument != null;
};

checkNull = function(argument){
    return !checkNotNull(argument);
};



displayEntityWAddr = function(name, city, district, state, country) {
    var address = "";
    if (checkNotNullEmpty(name)) {
        address = name + ", ";
        address = concatIfNotEmpty(address, city);
        address = concatIfNotEmpty(address, district);
        address = concatIfNotEmpty(address, state);
        address = concatIfNotEmpty(address, country);
        address = stripLastComma(address);
    }

    return address;
}

stripLastComma = function(str) {
    str = str.trim();
    if (str != "") {
        if(str.slice(-1) == ","){
            str = str.substring(0, str.length - 1);
        }

    }
    return str;
}

concatIfNotEmpty = function(str, substr) {
    if(checkNotNullEmpty(substr)) {
        str = str.concat(substr)
            .concat(", ")
    }
    return str;
}

constructAddressStr = function(inAddress) {
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

}

function drawStockEvent(divId, url, color,callback ) {
    console.log( 'plotStockEvent: url = ' + url );
    var query = new google.visualization.Query( url );
    query.send( function( response ) {
        if ( !response.isError() ) {
            // Get the data table and render the Table report
            var data = response.getDataTable();
            // Hide the irrelevant columns
            var stockEventView = new google.visualization.DataView( data );
            //stockEventView.hideColumns( [ 1,2,3,6,7 ] ); // hide the kiosk-name, stock, duration and Ids columns
            stockEventView.hideColumns( [ 1,2,3,4,5,8,9 ] ); // hide the kiosk-name, stock, min, max, duration and Ids columns (materialId and kioskiId)
            // Draw the chart
            var div = document.getElementById(divId);
            var chart = new google.visualization.Timeline( div );
            chart.draw( stockEventView, { timeline: { showRowLabels: true }, colors: [ color ], width: 680, height:100  } );
        }
        callback();
    } );
}


function formatDate(date) {
    return checkNotNullEmpty(date) ? date.getDate() + "/" + (date.getMonth() + 1) + "/" + date.getFullYear() : "";
}

function string2Date(dateString,format,delimiter) {
    if(checkNotNullEmpty(dateString) && checkNotNullEmpty(format) && checkNotNullEmpty(delimiter)){
        format = format.toLowerCase();
        var fFields = format.split(delimiter);
        var dFields = dateString.split(delimiter);
        var dInd = fFields.indexOf("dd");
        var mInd = fFields.indexOf("mm");
        var yInd = fFields.indexOf("yyyy");
        return new Date(dFields[yInd],dFields[mInd]-1,dFields[dInd]);
    }
    return;
}
function formatDate2Url(newVal) {
    return checkNotNullEmpty(newVal) ? newVal.getFullYear() + "-" + (newVal.getMonth() + 1) + "-" + newVal.getDate() : "";
}

function parseUrlDate(dateString){
    return checkNotNullEmpty(dateString)?new Date(dateString):"";
}

function csvToArray(data){
    var array = data.spl;

}


function getFlotData( data, min, max ) {
    var d = [];
    var dmin = [];
    var dmax = [];
    for ( var i = 0; i < data.length; i++ ) {
        var temp = data[i];
        d.push( [ temp.time * 1000, temp.tmp ] ); // Changed to milliseconds because json returns seconds
        if ( min )
            dmin.push( [ temp.time * 1000, min ] ); // Changed to milliseconds because json returns seconds
        if ( max )
            dmax.push( [ temp.time * 1000, max ] ); // Changed to milliseconds because json returns seconds
    }
    return [ d, dmin, dmax ];
}
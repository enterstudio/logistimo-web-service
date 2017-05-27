var mapServices = angular.module('mapServices', []);
mapServices.factory('mapService', [function () {
    return {
        getLatLong: function (address, callback) {
            try {
                var geocoder = new google.maps.Geocoder();
            } catch(err){ // Due to internet/some issue, google is not able to contact
                return;
            }
            var promise = geocoder.geocode({
                'address': address
            }, function (results, status) {
                if (status == google.maps.GeocoderStatus.OK) {
                    if(checkNotNullEmpty(callback)) {
                        callback(results);
                    }
                } else {
                    console.log('Geocode was not successful for the following reason: '
                    + status);
                }
            });
            return promise;
        },
        /**
         * Adds latitude & longitude parameters as required by google maps api.
         * By default assumes lt ad ln have latitude and longitude respectively.
         * if lt and ln are attributes of sub property in list, lnltobj can be passed.
         * It also sets zoom and map bounds for the given lt and ln
         * @param data
         * @param map
         * @param lnltobj
         */
        convertLnLt: function (data, map, lnltobj) {
            if (data.length != 0) {
                try {
                    var bounds = new google.maps.LatLngBounds();
                    for (var item in data) {
                        var myitem = data[item];
                        if (!myitem.latitude && !myitem.longitude) {
                            ltln = checkNotNullEmpty(lnltobj)?myitem[lnltobj]:myitem;
                            if(checkNotNull(ltln.lt)){
                                myitem.latitude = ltln.lt;
                            }
                            if(checkNotNull(ltln.ln)) {
                                myitem.longitude = ltln.ln;
                            }
                        }
                        bounds.extend(new google.maps.LatLng(myitem.latitude, myitem.longitude));
                    }
                    map.bounds = {
                        northeast: {
                            latitude: bounds.getNorthEast().lat(),
                            longitude: bounds.getNorthEast().lng()
                        },
                        southwest: {
                            latitude: bounds.getSouthWest().lat(),
                            longitude: bounds.getSouthWest().lng()
                        }
                    };
                    map.center = {
                        latitude: bounds.getCenter().lat(),
                        longitude: bounds.getCenter().lng()
                    };
                    map.zoom = this.getBoundsZoomLevel(bounds, {height: 500, width: 900});
                }catch(err){};
            }
        },
        updateEntityMap: function (address, callback, always) {
            var map = {zoom: 8};
            if (checkNotNullEmpty(always) || (typeof address.lt === 'undefined' || address.lt == 0) && (typeof address.lt === 'undefined' || address.ln == 0)) {
                var addressStr = constructAddressStr(address);
                if (addressStr != "") {
                    this.getLatLong(addressStr, callback);
                }
            } else {
               map.center = {latitude: address.lt, longitude: address.ln};
               address.location = {latitude: address.lt, longitude: address.ln};
            }
            return map;
        },
        getBoundsZoomLevel: function (bounds, mapDim) {
            var WORLD_DIM = {height: 256, width: 256};
            var ZOOM_MAX = 14;
            function latRad(lat) {
                var sin = Math.sin(lat * Math.PI / 180);
                var radX2 = Math.log((1 + sin) / (1 - sin)) / 2;
                return Math.max(Math.min(radX2, Math.PI), -Math.PI) / 2;
            }
            function zoom(mapPx, worldPx, fraction) {
                return Math.floor(Math.log(mapPx / worldPx / fraction) / Math.LN2);
            }
            var ne = bounds.getNorthEast();
            var sw = bounds.getSouthWest();
            var latFraction = (latRad(ne.lat()) - latRad(sw.lat())) / Math.PI;
            var lngDiff = ne.lng() - sw.lng();
            var lngFraction = ((lngDiff < 0) ? (lngDiff + 360) : lngDiff) / 360;
            var latZoom = zoom(mapDim.height, WORLD_DIM.height, latFraction);
            var lngZoom = zoom(mapDim.width, WORLD_DIM.width, lngFraction);
            return Math.min(latZoom-1, lngZoom-1, ZOOM_MAX);
        },
        getBubbleIcon: function(number,color){
            color = checkNotNullEmpty(color) ? color : 'FFFFFF';
            var scale = 0.75;
            if(number>99999){
                scale = 1.75;
            }else if(number>9999){
                scale = 1.5;
            }else if(number>999){
                scale = 1.25;
            }else if(number>99){
                scale = 1;
            }
            return "https://chart.googleapis.com/chart?chst=d_map_spin&chld="+scale+"|0|"+color+"|10|_|"+number;
        },
        getWarnBubbleIcon: function(number) {
          return this.getBubbleIcon(number,'FFFF66');
        },
        getInfoBubbleIcon: function(number) {
            return this.getBubbleIcon(number,'33CCFF');
        },
        getDangerBubbleIcon: function(number) {
            return this.getBubbleIcon(number,'FF0000');
        },
        getMarkerIcon: function(number,color) {
            color = checkNotNullEmpty(color) ? color : 'FF0000';
            return "https://chart.googleapis.com/chart?chst=d_map_pin_letter&chld="+number+"|"+color;
        },
        getCloseArrowSymbol : function(){
            return {
                path: google.maps.SymbolPath.FORWARD_CLOSED_ARROW
            };
        }
    }
}]);
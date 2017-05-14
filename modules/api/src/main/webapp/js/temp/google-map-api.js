var mapServices = angular.module('mapServices', []);

mapServices.factory('mapService', [function () {
    return {
        getLatLong: function (address, callback) {
            var geocoder = new google.maps.Geocoder();
            promise = geocoder.geocode({
                'address': address
            }, function (results, status) {
                if (status == google.maps.GeocoderStatus.OK) {
                    if(checkNotNullEmpty(callback)) {
                        callback(results[0].geometry.location);
                    }
                } else {
                    alert('Geocode was not successful for the following reason: '
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
                bounds = new google.maps.LatLngBounds();
                for (item in data) {
                    myitem = data[item];
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
                }
                map.center = {
                    latitude: bounds.getCenter().lat(),
                    longitude: bounds.getCenter().lng()
                }

                map.zoom = this.getBoundsZoomLevel(bounds, {height: 500, width: 900});


            }
        },
        updateEntityMap: function (address, callback, always) {
            var map = {zoom: 12};
            map.center = {latitude: checkNotNullEmpty(address.lat) ? address.lat : 0, longitude: checkNotNullEmpty(address.lng) ? address.lng : 0};
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

            return Math.min(latZoom, lngZoom, ZOOM_MAX);
        },
        getBubbleIcon: function(number){
            scale = 1;
            if(number>9999){
                scale = 2;
            }
            return "https://chart.googleapis.com/chart?chst=d_map_spin&chld="+scale+"|0|F56C64|10|_|"+number;
        },
        getMarkerIcon: function(number) {
            return "https://chart.googleapis.com/chart?chst=d_map_pin_letter&chld="+number+"|FF0000";
        },
        getCloseArrowSymbol : function(){
            return {
                path: google.maps.SymbolPath.FORWARD_CLOSED_ARROW
            };
        }
    }
}]);
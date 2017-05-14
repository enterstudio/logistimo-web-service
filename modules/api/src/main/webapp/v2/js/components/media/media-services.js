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
 * Created by mohansrinivas on 8/13/15.
 */
var mediaServices = angular.module('mediaServices', []);
mediaServices.factory('mediaService',['$http', function ($http) {
    return {
        fetch: function (urlStr) {
            var promise = $http({method: 'GET', url: urlStr});
            return promise;
        },
        fetchP: function (data, urlStr) {
            var promise = $http({method: 'POST', data: data, url: urlStr});
            return promise;
        },
        uploadImage : function(type,domainKey,data){
            var media = {
                "mediaType" : type,
                "domainKey" : domainKey,
                "content" :{
                    "value" :data
                }
            };
            return this.fetchP(media, "/_ah/api/mediaendpoint/v1/media/");
        },
        removeImage : function(domainKey){
            var promise = $http({method: 'DELETE', url: "/_ah/api/mediaendpoint/v1/media/" + domainKey});
            return promise;
        },
        getDomainkeyImages: function(domainKey) {
            return this.fetch('/_ah/api/mediaendpoint/v1/mediaforDomain/' + domainKey);
        },
    }
    }]);
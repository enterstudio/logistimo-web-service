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
 * Created by mohan raja on 08/01/15.
 */

var blkUpServices = angular.module('blkUpServices', []);

blkUpServices.factory('blkUpService', [
    '$http',
    function($http) {
        return {
            fetch: function (urlStr) {
                var promise = $http({method: 'GET', url: urlStr});
                return promise;
            },
            uploadStatus : function(type) {
                return this.fetch('/s2/api/bulk/uploadstatus?type=' + type);
            },
            uploadURL : function(type) {
                return this.fetch('/s2/api/bulk/upload');
            },
            uploadPostUrl : function(url,file, type) {
                var fd = new FormData();
                fd.append('data',file);
                fd.append('type', type);
                var promise = $http.post(url,fd,{
                    transformRequest: angular.identity,
                    headers : { 'Content-Type' : undefined},
                    url : url
                });
                return promise;
            },
            getUploadTransactions : function(eid,from,to,offset,size) {
                offset = typeof offset !== 'undefined' ? offset : 0;
                size = typeof size !== 'undefined' ? size : 50;
                if(eid == undefined || eid == null){
                    eid = "";
                }
                var urlStr = '/s2/api/bulk/view?eid='+ eid + "&offset=" + offset + "&size=" + size;
                if (typeof from !== 'undefined' && from != null && from != "") {
                    urlStr = urlStr + "&from=" + from;
                }
                if (typeof to !== 'undefined' && to != null && to != "") {
                    urlStr = urlStr + "&to=" + to;
                }
                return this.fetch(urlStr);
            },
            getManualUploadStatus : function(){
                return this.fetch('/s2/api/bulk/checkMUStatus');
            }
        }
    }
]);

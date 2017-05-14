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
 * Created by Mohan Raja on 27/01/15.
 */
var exportServices = angular.module('exportServices', []);
exportServices.factory('exportService', ['$http', function($http) {
    return {
        fetch: function (urlStr) {
            var promise = $http({method: 'GET', url: urlStr});
            return promise;
        },
        fetchP: function (data, urlStr) {
            var promise = $http({method: 'POST', data: data, url: urlStr});
            return promise;
        },
        /*downloadFile : function(key) {
            return this.fetch('/s2/api/export/download?key=' + key);
        },*/
        scheduleBatchExport : function(type,extraParams) {
            if(type == "orders" && extraParams.indexOf("&values") >= 0){
                window.location = '/s/export?type=orders&attachtoemail&format=csv' + extraParams;
                return "download complete";
            }
            var exportURL = '/s2/api/export/schedule/batch?type='+type;
            if (extraParams !== 'undefined' && checkNotNullEmpty(extraParams))
                exportURL += extraParams;
            return this.fetch(exportURL);
        },
        /*exportOrders : function(orderIds){
            return this.fetch('/s2/api/export/orders?values=' + orderIds);
        },*/
        exportReport : function(type,startDate,endDate,frequency,filterMap){
            return this.fetchP({type: type,startDate: startDate,endDate: endDate,frequency: frequency,filterMap:filterMap},'/s2/api/export/schedule/report');
        },
        getExportJobList : function(offset,size,type, allExports) {
            offset = typeof offset !== 'undefined' ? offset : 0;
            size = typeof size !== 'undefined' ? size : 50;

            var urlStr = '/s2/api/export/exportjoblist?offset=' + offset + "&size=" + size + "&type=" + type + "&allExports=" + allExports;
            return this.fetch(urlStr);
        },
        exportAssets : function(type) {
            return this.fetch('/s2/api/export/schedule/batch?type='+type);
        }
    }
}]);

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

var assetServices = angular.module('assetServices', []);
assetServices.factory('assetService', ['$http', function ($http) {
    return {
        fetch: function (urlStr) {
            var promise = $http({method: 'GET', url: urlStr});
            return promise;
        },
        fetchP: function (data, urlStr) {
            var promise = $http({method: 'POST', data: data, url: urlStr});
            return promise;
        },
        createAsset: function(data){
            return this.fetchP(data, '/s2/api/assets/');
        },
        getAssetsByKeyword: function(keyword, at, size, offset){
            offset = typeof offset !== 'undefined' ? offset : 0;
            size = typeof size !== 'undefined' ? size : 50;
            var urlStr = "/s2/api/assets/?offset=" + offset + "&size=" + size;
            if(checkNotNullEmpty(keyword)){
                urlStr += '&q=' + keyword;
            }else if(checkNotNullEmpty(at)){
                urlStr += '&at=' + at;
            }
            return this.fetch(urlStr);
        },
        getAssetsInDetail: function(eid, at, ws, alrmType, dur, location, offset, size, awr){
            offset = typeof offset !== 'undefined' ? offset : 0;
            size = typeof size !== 'undefined' ? size : 50;
            var urlStr = "/s2/api/assets/details/?offset=" + offset + "&size=" + size;
            if(checkNotNullEmpty(eid)){
                urlStr += "&eid=" + eid
            }
            if(checkNotNullEmpty(location)){
                urlStr += "&loc=" + location;
            }

            if(checkNotNullEmpty(at)){
                urlStr += "&at=" + at;
            }

            if(checkNotNullEmpty(ws)){
                urlStr += "&ws=" + ws;
            }

            if(checkNotNullEmpty(alrmType)){
                urlStr += "&alrmtype=" + alrmType;

                if(checkNotNullEmpty(dur)){
                    urlStr += "&dur=" + dur;
                }
            }
            if(checkNotNullEmpty(awr)){
                urlStr += "&awr=" + awr;
            }
            return this.fetch(urlStr);
        },
        getAssetDetails: function(manufactureId, assetId){
            return this.fetch("/s2/api/assets/" + manufactureId + "/" + encodeURI(assetId));
        },
        getAssetRelations: function(manufactureId, assetId){
            return this.fetch("/s2/api/assets/relation/" + manufactureId + "/" + encodeURI(assetId));
        },
        getFilteredAssets: function(text, entityId, at, all, ns){
            var urlStr = "/s2/api/assets/filter?q=" + text;
            if(checkNotNullEmpty(at)){
                urlStr += "&at=" + at;
            }

            if(checkNotNullEmpty(entityId)){
                urlStr += "&eid=" + entityId;
            }

            if(all != undefined){
                urlStr += "&all=" + all;
            }

            if(checkNotNullEmpty(ns)){
                urlStr += "&ns=" + ns;
            }
            return this.fetch(urlStr);
        },
        createAssetRelationships: function(data, deleteR){
            var urlStr = "/s2/api/assets/relations";

            if(deleteR != undefined){
                urlStr += "?delete=" + deleteR;
            }

            return this.fetchP(data, urlStr);
        },
        getTemperatures: function (vendorId, deviceId, mpId, at, size, sint, tdate) {
            var url = '/s2/api/assets/temperature/' + vendorId + '/' + encodeURI(deviceId) + '/' + mpId + '?size=' + size + '&sint=' + sint + '&at=' + at;
            if(checkNotNullEmpty(tdate)){
                url += '&edate=' + formatDate2Url(tdate);
            }
            return this.fetch(url);
        },
        getRecentAlerts: function (vendorId, deviceId, page, size) {
            return this.fetch('/s2/api/assets/alerts/recent/' + vendorId + '/' + encodeURI(deviceId) + '?page=' + page + '&size=' + size);
        },
        getAssetConfig: function (vendorId, deviceId) {
            return this.fetch('/s2/api/assets/config/' + vendorId + '/' + encodeURI(deviceId));
        },
        updateDeviceConfig: function (deviceConfig, domainId, pushConfig) {
            return this.fetchP(deviceConfig, '/s2/api/assets/config?pushConfig=' + pushConfig);
        },
        getAssetStats: function (vendorId, deviceId, from, to) {
            return this.fetch('/s2/api/assets/stats/' + vendorId + '/' + encodeURI(deviceId) + '?from=' + from + '&to=' + to);
        },
        getChildTagSummary: function (domainId) {
            return this.fetch('/s2/api/assets/tags/child?tagid=' + domainId);
        },
        getTagAbnormalDevices: function (domainId) {
            return this.fetch('/s2/api/assets/tags/abnormal?tagid=' + domainId);
        },
        getDomainLocation: function () {
            return this.fetch('/s2/api/assets/domain/location');
        },
        getTagSummary: function (domainId, at) {
            return this.fetch('/s2/api/assets/tags?tagid=' + domainId + '&at=' + at);
        },
        updateAsset: function (deviceDetails) {
            return this.fetchP(deviceDetails, '/s2/api/assets/?update=true');
        },
        pushPullConfig: function (requestData) {
            return this.fetchP(requestData, '/s2/api/assets/device/config');
        },
        getAsset: function(assetId){
            return this.fetch('/s2/api/assets/' + assetId);
        },
        deleteAsset: function(requestData){
            return this.fetchP(requestData, '/s2/api/assets/delete');
        },
        getModelSuggestions: function(query) {
            var url = '/s2/api/assets/model';
            if(checkNotNullEmpty(url)) {
                url += '?query=' + query;
            }
            return this.fetch(url);
        }
    }
}]);
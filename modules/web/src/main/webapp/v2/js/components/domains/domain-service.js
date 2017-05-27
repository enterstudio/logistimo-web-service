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

var domainServices = angular.module('domainServices', []);
domainServices.factory('domainService', ['$http', function ($http) {
    return {
        fetch: function (urlStr) {
            var promise = $http({method: 'GET', url: urlStr});
            return promise;
        },
        fetchP: function (data, urlStr) {
            var promise = $http({method: 'POST', data: data, url: urlStr});
            return promise;
        },
        createDomain: function (domainName, desc) {
            var param = '?domainName=' + domainName + '&desc=' + desc;
            return this.fetchP(null, '/s2/api/domain/create' + param);
        },
        deleteDomain: function(dId) {
            var param = '?dId=' + dId;
            return this.fetchP(null, '/s2/api/domain/delete' + param);
        },
        getCurrentDomain: function(){
            return this.fetch('/s2/api/domain/current');
        },
        switchDomain: function(domainId){
            return this.fetchP(domainId,'/s2/api/domain/switch');
        },
        getCurrentUserDomain: function(){
            return this.fetch('/s2/api/domain/currentUser');
        },
        getDomainSuggestions: function (query) {
            var param = '';
            if(checkNotNullEmpty(query)){
                param = '?q='+query;
            }
            return this.fetch('/s2/api/domain/suggestions' + param);
        },
        updatedomaininfo: function (domainId, name, desc) {
            var param = '/' + domainId + '?name=' + name + '&desc=' + desc;
            return this.fetch('/s2/api/domain/updateDomain' + param);
        },
        fetchDomainById: function (data) {
            return this.fetch('/s2/api/domain/domain?domainId=' + data);
        }
    }
}]);
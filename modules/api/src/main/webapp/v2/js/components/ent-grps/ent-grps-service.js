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

var entGrpServices = angular.module('entGrpServices', []);

entGrpServices.factory('entGrpService', ['$http', function($http) {
    return {
        fetch: function (urlStr) {
            var promise = $http({method: 'GET', url: urlStr});
            return promise;
        },
        fetchP: function (data, urlStr) {
            var promise = $http({method: 'POST', data: data, url: urlStr});
            return promise;
        },
        getEntGrps: function (offset,size) {
            offset = typeof offset !== 'undefined' ? offset : 0;
            size = typeof size !== 'undefined' ? size : 50;
            return this.fetch('/s2/api/ent-grps/?offset='+offset+"&size="+size);
        },
        setEntGrps: function(entGrps, action){
            return this.fetchP(entGrps,'/s2/api/ent-grps/action/'+action);
        },
        getPoolGroup: function(groupId){
            return this.fetch('/s2/api/ent-grps/groupId/'+groupId);
        },
        deletePoolGroup: function(groupIds){
            return this.fetchP("'"+groupIds + "'",'/s2/api/ent-grps/delete');
        }
    }
}]);

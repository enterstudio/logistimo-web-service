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

var matServices = angular.module('matServices', []);
matServices.factory('matService', ['$http', function ($http) {
    return {
        fetch: function (urlStr) {
            var promise = $http({method: 'GET', url: urlStr});
            return promise;
        },
        fetchP: function (data, urlStr) {
            var promise = $http({method: 'POST', data: data, url: urlStr});
            return promise;
        },
        get: function (materialId) {
            return this.fetch('/s2/api/materials/material/' + materialId);
        },
        checkMaterialAvailability: function (mnm) {
            return this.fetch('/s2/api/materials/check/?mnm=' + mnm);
        },
        /*getMaterials: function (entityId, q, tag, offset, size) {
            offset = typeof offset !== 'undefined' ? offset : 0;
            size = typeof size !== 'undefined' ? size : 50;
            var urlStr = '/s2/api/materials/entity/' + entityId + "?offset=" + offset + "&size=" + size;
            if (checkNotNullEmpty(tag)) {
                urlStr = urlStr + "&tag=" + tag;
            }
            if (typeof q !== 'undefined') {
                urlStr = urlStr + "&q=" + q;
            }
            return this.fetch(urlStr);
        },*/
        getDomainMaterials: function (q, tag, offset, size, entityId, ihu) {
            offset = typeof offset !== 'undefined' ? offset : 0;
            size = typeof size !== 'undefined' ? size : 50;
            var urlStr = '/s2/api/materials/?offset=' + offset + "&size=" + size;
            if (checkNotNullEmpty(tag)) {
                urlStr = urlStr + "&tag=" + tag;
            }
            if (checkNotNullEmpty(q)) {
                urlStr = urlStr + "&q=" + q;
            }
            if (checkNotNullEmpty(entityId)) {
                urlStr = urlStr + "&entityId=" + entityId;
            }
            if (checkNotNullEmpty(ihu)) {
                urlStr = urlStr + "&ihu=" + ihu;
            }
            return this.fetch(urlStr);
        },
        deleteMaterials: function (materials) {
            return this.fetchP("'" + materials + "'", '/s2/api/materials/delete/');
        },
        createMaterial: function (material) {
            return this.fetchP(material, '/s2/api/materials/create');
        },
        update: function (material) {
            return this.fetchP(material, "/s2/api/materials/update");
        }
    }
}]);

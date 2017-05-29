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

var demandServices = angular.module('demandServices', []);
demandServices.factory('demandService', ['$http', function ($http) {
    return {
        fetch: function (urlStr) {
            var promise = $http({method: 'GET', url: urlStr});
            return promise;
        },
        fetchP: function (data, urlStr) {
            var promise = $http({method: 'POST', data: data, url: urlStr});
            return promise;
        },
        getEntityDemand: function (entityId, from, tag, offset, size) {
            offset = typeof offset !== 'undefined' ? offset : 0;
            size = typeof size !== 'undefined' ? size : 50;
            var urlStr = '/s2/api/demand/entity/' + entityId + "?offset=" + offset + "&size=" + size;
            if (checkNotNullEmpty(from)) {
                urlStr = urlStr + "&from=" + from;
            }
            if (checkNotNullEmpty(tag)) {
                urlStr = urlStr + "&tag=" + tag;
            }
            return this.fetch(urlStr);
        },
        getMaterialDemand: function (materialId, from, etag, offset, size, noDups) {
            offset = typeof offset !== 'undefined' ? offset : 0;
            size = typeof size !== 'undefined' ? size : 50;
            var urlStr = '/s2/api/demand/material/' + materialId + "?offset=" + offset + "&size=" + size;
            if (checkNotNullEmpty(from)) {
                urlStr = urlStr + "&from=" + from;
            }
            if (checkNotNullEmpty(etag)) {
                urlStr = urlStr + "&etag=" + etag;
            }
            if (checkNotNullEmpty(noDups)) {
                urlStr = urlStr + "&noDups=" + noDups;
            }
            return this.fetch(urlStr);
        },
        getDemand: function (etag, tag, kioskId, mId, etrn, sbo, offset, size, otype) {
            offset = typeof offset !== 'undefined' ? offset : 0;
            size = typeof size !== 'undefined' ? size : 50;
            var urlStr = '/s2/api/demand/?offset=' + offset + "&size=" + size;
            if(checkNotNullEmpty(kioskId)) {
                urlStr = urlStr + "&kioskId="+kioskId;
            }
            if(checkNotNullEmpty(mId)) {
                urlStr = urlStr + "&mId="+mId;
            }
            if (checkNotNullEmpty(etag)) {
                urlStr = urlStr + "&etag=" + etag;
            }
            if (checkNotNullEmpty(tag)) {
                urlStr = urlStr + "&tag=" + tag;
            }
            if(checkNotNullEmpty(etrn)) {
                urlStr = urlStr + "&etrn=" + etrn;
            }
            if(checkNotNullEmpty(sbo)) {
                urlStr = urlStr + "&sbo=" + sbo;
            }
            if(checkNotNullEmpty(otype)) {
                urlStr = urlStr + "&otype=" + otype;
            }
            return this.fetch(urlStr);
        },
        clearAllocations: function(kId, mId, oid, etrn, bo) {
            var urlStr = '/s2/api/demand/alloc?mId=' + mId;
            if(checkNotNullEmpty(kId)) {
                urlStr = urlStr + "&kId=" + kId;
            }
            if(checkNotNullEmpty(oid)) {
                urlStr = urlStr + "&oid=" + oid;
            }
            if(checkNotNullEmpty(etrn)) {
                urlStr = urlStr + "&etrn=" + etrn;
            }
            if(checkNotNullEmpty(bo)) {
                urlStr = urlStr + "&bo=" + bo;
            }
            return this.fetchP("", urlStr);
        },
        getDiscrepancies: function(oType, etrn, entityId, materialId, eTag, mTag, from, to, orderId, discType, offset, size) {
            offset = typeof offset !== 'undefined' ? offset : 0;
            size = typeof size !== 'undefined' ? size : 50;
            var urlStr = '/s2/api/demand/discrepancies/' + "?offset=" + offset + "&size=" + size;
            if (checkNotNullEmpty(oType)) {
                urlStr = urlStr + "&oType=" + oType;
            }
            if (checkNotNullEmpty(etrn)) {
                urlStr = urlStr + "&etrn=" + etrn;
            }
            if (checkNotNullEmpty(entityId)) {
                urlStr = urlStr + "&entityId=" + entityId;
            }
            if (checkNotNullEmpty(materialId)) {
                urlStr = urlStr + "&materialId=" + materialId;
            }
            if (checkNotNullEmpty(eTag)) {
                urlStr = urlStr + "&eTag=" + eTag;
            }
            if (checkNotNullEmpty(mTag)) {
                urlStr = urlStr + "&mTag=" + mTag;
            }
            if (checkNotNullEmpty(from)) {
                urlStr = urlStr + "&from=" + from;
            }
            if (checkNotNullEmpty(to)) {
                urlStr = urlStr + "&to=" + to;
            }
            if (checkNotNullEmpty(orderId)) {
                urlStr = urlStr + "&orderId=" + orderId;
            }
            if (checkNotNullEmpty(discType)) {
                urlStr = urlStr + "&discType=" + discType;
            }
            return this.fetch(urlStr);
        },
        getDemandView: function(kId, mId, etrn, bo, otype, incShip) {
            var urlStr = '/s2/api/demand/details?kId=' + kId + "&mId=" + mId + "&otype=" + otype;
            if(checkNotNullEmpty(etrn)) {
                urlStr = urlStr + "&etrn=" +etrn;
            }
            if(checkNotNullEmpty(bo)) {
                urlStr = urlStr + "&bo=" + bo;
            }
            if(checkNotNullEmpty(incShip)) {
                urlStr = urlStr + "&incShip=" + incShip;
            }
            return this.fetch(urlStr);
        },
        allocateQuantity: function(allocation) {
            return this.fetchP(allocation,'/s2/api/demand/allocate/');
        }
    }
}]);

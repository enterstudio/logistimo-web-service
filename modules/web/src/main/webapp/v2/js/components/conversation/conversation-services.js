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

var conversationServices = angular.module('conversationServices', []);
conversationServices.factory('conversationService', ['$http', function ($http) {
    return {
        fetch: function (urlStr) {
            var promise = $http({method: 'GET', url: urlStr});
            return promise;
        },
        fetchP: function (data, urlStr) {
            var promise = $http({method: 'POST', data: data, url: urlStr});
            return promise;
        },
        getMessages: function (conversationId,offset,size) {
            offset = typeof offset !== 'undefined' ? offset : 0;
            size = typeof size !== 'undefined' ? size : 50;
            var urlStr = '/s2/api/conversation/messages/?offset='
                + offset + "&size=" + size;
            if (typeof conversationId !== 'undefined') {
                urlStr = urlStr + "&conversationId=" + conversationId;
            }
            return this.fetch(urlStr);
        },
        getMessagesByObj: function (objType, objId, offset, size, isCnt) {
            offset = typeof offset !== 'undefined' ? offset : 0;
            size = typeof size !== 'undefined' ? size : 50;
            var urlStr = '/s2/api/conversation/messages/?offset='
                + offset + "&size=" + size;
            if (typeof objType !== 'undefined') {
                urlStr = urlStr + "&objType=" + objType;
            }
            if (typeof objType !== 'undefined') {
                urlStr = urlStr + "&objId=" + objId;
            }
            if(isCnt) {
                urlStr += "&cnt=true";
            }
            return this.fetch(urlStr);
        },
        getMessagesByTag: function (tag, offset, size) {
            offset = typeof offset !== 'undefined' ? offset : 0;
            size = typeof size !== 'undefined' ? size : 50;
            var urlStr = '/s2/api/conversation/messages/tag/?offset='
                + offset + "&size=" + size;
            if (typeof tag !== 'undefined') {
                urlStr = urlStr + "&tag=" + tag;
            }
            return this.fetch(urlStr);
        },
        addMessage: function(objType, objId, message) {
            return this.fetchP({"data":message},'/s2/api/conversation/message/' + objType + '/' + objId);
        }
    }
}
]);

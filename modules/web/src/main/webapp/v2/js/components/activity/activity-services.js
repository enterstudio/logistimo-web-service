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

var activityServices = angular.module('activityServices', []);
activityServices.factory('activityService', ['$http', function ($http) {
    return {
        fetch: function (urlStr) {
            var promise = $http({method: 'GET', url: urlStr});
            return promise;
        },
        fetchP: function (data, urlStr) {
            var promise = $http({method: 'POST', data: data, url: urlStr});
            return promise;
        },
        getUsers: function (entityId, srcEntityId) {
            var urlStr = '/s2/api/entities/entity/' + entityId + "/users";
            if (typeof srcEntityId !== 'undefined') {
                urlStr = urlStr + "?srcEntityId=" + srcEntityId;
            }
            return this.fetch(urlStr);
        },
        getUsersByRole: function (role, q, offset, size) {
            offset = typeof offset !== 'undefined' ? offset : 0;
            size = typeof size !== 'undefined' ? size : 50;
            var urlStr = '/s2/api/users/role/' + role + "?offset="
                + offset + "&size=" + size;
            if (typeof q !== 'undefined') {
                urlStr = urlStr + "&q=" + q;
            }
            return this.fetch(urlStr);
        },
        getDomainUsers: function (q, offset, size,utype,includeSuperusers) {
            offset = typeof offset !== 'undefined' ? offset : 0;
            size = typeof size !== 'undefined' ? size : 50;
            var urlStr = '/s2/api/users/?offset=' + offset + "&size="
                + size;
            if (typeof q !== 'undefined') {
                urlStr = urlStr + "&q=" + q;
            }
            if (typeof utype !== 'undefined') {
                urlStr = urlStr + "&utype=" + utype;
            }
            if (typeof includeSuperusers !== 'undefined') {
                urlStr = urlStr + "&includeSuperusers=" + includeSuperusers;
            }
            return this.fetch(urlStr);
        },
        getFilteredDomainUsers : function(filters){
            var urlStr = '/s2/api/users/domain/users';
            return this.fetchP(filters,urlStr);
        },
        getElementsByUserFilter : function(pv,pn){
            var urlStr="/s2/api/users/elements/?paramName=" +pv+ "&paramValue="+pn;
            return this.fetch(urlStr);
        },
        checkUserAvailability: function (userid) {
            return this.fetch('/s2/api/users/check/?userid=' + userid);
        },
        checkCustomIDAvailability: function (customId, userId) {
            var params = customId;
            if(checkNotNullEmpty(userId)){
                params += "&userId="+userId;
            }
            return this.fetch('/s2/api/users/check/custom?customId=' + params);
        },
        createUser: function (user) {
            return this.fetchP(user, "/s2/api/users/");
        },
        deleteUsers: function (user) {
            return this.fetchP("'" + user + "'", "/s2/api/users/delete/");
        },
        getUser: function (userid) {
            return this.fetch('/s2/api/users/user/' + userid);
        },
        getUserMeta: function (userid) {
            return this.fetch('/s2/api/users/user/meta/' + userid);
        },
        getUserDetails: function (userid) {
            return this.fetch('/s2/api/users/user/' + userid + '?isDetail=true');
        },
        getUsersDetailByIds: function (userIds) {
            return this.fetch('/s2/api/users/users/?userIds=' + userIds + '&isMessage=true');
        },
        sendUserMessage: function (message) {
            return this.fetchP(message, '/s2/api/users/sendmessage/');
        },
        getUsersMessageStatus: function (offset, size) {
            return this.fetch('/s2/api/users/msgstatus/?offset=' + offset + '&size=' + size);
        },
        updateUser: function (user, userId) {
            return this.fetchP(user, '/s2/api/users/user/' + userId);
        },
        updateUserPassword: function (user, userId) {
            return this.fetch('/s2/api/users/updatepassword/?userId=' + userId + '&opw=' + user.opw + '&pw=' + user.pw);
        },
        resetUserPassword: function (userId, sendType) {
            return this.fetch('/s2/api/users/resetpassword/?userId=' + userId + '&sendType=' + sendType);
        },
        enableDisableUser: function (userId, action) {
            return this.fetch('/s2/api/users/userstate/?userId=' + userId + '&action=' + action);
        },
        getRoles: function(edit,eUsrid) {
            edit = typeof edit !== 'undefined' ? edit : false;
            eUsrid = typeof eUsrid !== 'undefined' ? eUsrid : "";
            return this.fetch('/s2/api/users/roles?edit='+edit+'&euid='+eUsrid);
        },
        switchConsole: function(){
            return this.fetch('/s2/api/users/switch');
        },
        addAccessibleDomains: function (userId, accDids) {
            return this.fetchP({userId: userId, accDids: accDids}, "/s2/api/users/addaccessibledomains");
        },
        removeAccessibleDomain: function (userId, domainId) {
            return this.fetch("/s2/api/users/removeaccessibledomain?userId="+userId+"&domainId="+domainId);
        },
        forceLogoutOnMobile: function (userId) {
            return this.fetch("/s2/api/users/forcelogoutonmobile?userId="+userId);
        }
    }
}
]);

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

var dashboardServices = angular.module('dashboardServices', []);
dashboardServices.factory('dashboardService', ['$http', function ($http) {
    return {
        fetch: function (urlStr) {
            var promise = $http({method: 'GET', url: urlStr});
            return promise;
        },
        fetchP: function (data, urlStr) {
            var promise = $http({method: 'POST', data: data, url: urlStr});
            return promise;
        },
        getMonthlyStats: function (numMonths) {
            return this.fetch('/s/dashboard?action=getmonthlystats&months=' + numMonths);
        },
        create: function (data) {
            return this.fetchP(data, '/s2/api/dashboard/');
        },
        getById: function (dbId, withConfig) {
            return this.fetch('/s2/api/dashboard/' + dbId + (withConfig ? ('?wc=' + withConfig) : ''));
        },
        getAll: function () {
            return this.fetch('/s2/api/dashboard/all/');
        },
        get: function (filter, level, exFilter, exType, period, tPeriod,aType, eTag, date, excludeETag, skipCache) {
            var url = '';
            if (checkNotNullEmpty(filter)) {
                url = '?filter=' + filter;
                if (checkNotNullEmpty(level)) {
                    url += '&level=' + level;
                }
            }
            if (checkNotNullEmpty(exFilter)) {
                url += (url == '' ? '?' : '&') + 'extraFilter=' + exFilter;
                if (checkNotNullEmpty(exType)) {
                    url += '&exType=' + exType;
                }
            }
            if (checkNotNullEmpty(period)) {
                url += (url == '' ? '?' : '&') + 'period=' + period;
            }
            if (checkNotNullEmpty(tPeriod)) {
                url += (url == '' ? '?' : '&') + 'tPeriod=' + tPeriod;
            }
            if (checkNotNullEmpty(eTag)) {
                url += (url == '' ? '?' : '&') + 'eTag=' + eTag;
            }
            if (checkNotNullEmpty(aType)) {
                url += (url == '' ? '?' : '&') + 'aType=' + aType;
            }
            if (checkNotNullEmpty(date)) {
                url += (url == '' ? '?' : '&') + 'date=' + date;
            }
            if(checkNotNullEmpty(excludeETag)) {
                url += (url == ''? '?' : '&') + 'excludeETag=' + excludeETag;
            }
            if(checkNotNullEmpty(skipCache)) {
                url += (url == ''? '?' : '&') + 'skipCache=' + skipCache;
            }
            return this.fetch('/s2/api/dashboard/' + url);
        },
        getInv: function (state,district,period,eTag,skipCache) {
            var url = '';
            if(checkNotNullEmpty(state)){
                url = '?state=' + state;
            }
            if(checkNotNullEmpty(district)){
                url = url+(checkNullEmpty(url)?'?':'&')+'district=' + district;
            }
            if(checkNotNullEmpty(period)){
                url = url+(checkNullEmpty(url)?'?':'&')+'period=' + period;
            }
            if (checkNotNullEmpty(eTag)) {
                url += (url == '' ? '?' : '&') + 'eTag=' + eTag;
            }
            if(checkNotNullEmpty(skipCache)) {
                url += (url == ''? '?' : '&') + 'skipCache=' + skipCache;
            }
            return this.fetch('/s2/api/dashboard/inv'+url);
        },
        getSessionData: function(filter, level, exFilter, exType, period, eTag, date, type, skipCache) {
            var url = '';
            if (checkNotNullEmpty(filter)) {
                url = '?filter=' + filter;
                if (checkNotNullEmpty(level)) {
                    url += '&level=' + level;
                }
            }
            if (checkNotNullEmpty(exFilter)) {
                url += (url == '' ? '?' : '&') + 'extraFilter=' + exFilter;
                if (checkNotNullEmpty(exType)) {
                    url += '&exType=' + exType;
                }
            }
            if (checkNotNullEmpty(period)) {
                url += (url == '' ? '?' : '&') + 'period=' + period;
            }
            if (checkNotNullEmpty(eTag)) {
                url += (url == '' ? '?' : '&') + 'eTag=' + eTag;
            }
            if (checkNotNullEmpty(date)) {
                url += (url == '' ? '?' : '&') + 'date=' + date;
            }
            if (checkNotNullEmpty(type)) {
                url += (url == '' ? '?' : '&') + 'type=' + type;
            }
            if(checkNotNullEmpty(skipCache)) {
                url += (url == ''? '?' : '&') + 'skipCache=' + skipCache;
            }
            return this.fetch('/s2/api/dashboard/session' + url);
        },
        getPredictive: function (filter, level, exFilter, exType, eTag, excludeETag, skipCache) {
            var url = '';
            if (checkNotNullEmpty(filter)) {
                url = '?filter=' + filter;
                if (checkNotNullEmpty(level)) {
                    url += '&level=' + level;
                }
            }
            if (checkNotNullEmpty(exFilter)) {
                url += (url == '' ? '?' : '&') + 'extraFilter=' + exFilter;
                if (checkNotNullEmpty(exType)) {
                    url += '&exType=' + exType;
                }
            }
            if (checkNotNullEmpty(eTag)) {
                url += (url == '' ? '?' : '&') + 'eTag=' + eTag;
            }
            if(checkNotNullEmpty(excludeETag)) {
                url += (url == ''? '?' : '&') + 'excludeETag=' + excludeETag;
            }
            if(checkNotNullEmpty(skipCache)) {
                url += (url == ''? '?' : '&') + 'skipCache=' + skipCache;
            }
            return this.fetch('/s2/api/dashboard/predictive' + url);
        },
        delete: function (id) {
            return this.fetchP(null, '/s2/api/dashboard/delete?id=' + id);
        },
        setAsDefault: function (curId, id) {
            return this.fetch('/s2/api/dashboard/setdefault?oid=' + curId + '&id=' + id);
        },
        update: function (data) {
            return this.fetchP(data, '/s2/api/dashboard/update');
        },
        saveConfig: function (data) {
            return this.fetchP(data, '/s2/api/dashboard/saveconfig');
        },
        getEntInvData: function(eid, mTag) {
            var url = '/s2/api/dashboard/ent/?eid=' + eid;
            if(checkNotNullEmpty(mTag)) {
                url += '&mTag=' + mTag;
            }
            return this.fetch(url);
        }
    }
}]);


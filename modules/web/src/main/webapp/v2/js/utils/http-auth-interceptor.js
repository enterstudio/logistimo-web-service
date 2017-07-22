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

/*global angular:true, browser:true */

/**
 * @license HTTP Auth Interceptor Module for AngularJS
 * (c) 2012 Witold Szczerba
 * License: MIT
 */
(function () {
    'use strict';

    angular.module('http-auth-interceptor', ['http-auth-interceptor-buffer'])

        .factory('authService', ['$rootScope','httpBuffer', function($rootScope, httpBuffer) {
            return {
                /**
                 * Call this function to indicate that authentication was successfull and trigger a
                 * retry of all deferred requests.
                 * @param data an optional argument to pass on to $broadcast which may be useful for
                 * example if you need to pass through details of the user that was logged in
                 * @param configUpdater an optional transformation function that can modify the
                 * requests that are retried after having logged in.  This can be used for example
                 * to add an authentication token.  It must return the request.
                 */
                loginConfirmed: function(data, configUpdater, userChanged) {
                    var updater = configUpdater || function(config) {return config;};
                    $rootScope.$broadcast('event:auth-loginConfirmed', data);
                    if(userChanged){
                        httpBuffer.rejectAll('Login changed, rejecting deferred calls.');
                    }else{
                        httpBuffer.retryAll(updater);
                    }
                },

                /**
                 * Call this function to indicate that authentication should not proceed.
                 * All deferred requests will be abandoned or rejected (if reason is provided).
                 * @param data an optional argument to pass on to $broadcast.
                 * @param reason if provided, the requests are rejected; abandoned otherwise.
                 */
                loginCancelled: function(data, reason) {
                    httpBuffer.rejectAll(reason);
                    $rootScope.$broadcast('event:auth-loginCancelled', data);
                }
            };
        }])

    /**
     * $http interceptor.
     * On 401 response (without 'ignoreAuthModule' option) stores the request
     * and broadcasts 'event:auth-loginRequired'.
     * On 403 response (without 'ignoreAuthModule' option) discards the request
     * and broadcasts 'event:auth-forbidden'.
     */
        .config(['$httpProvider', function($httpProvider) {
            $httpProvider.interceptors.push(['$rootScope', '$q', 'httpBuffer', function($rootScope, $q, httpBuffer) {
                return {
                    'request': function(config) {
                        // do something on success
                        if(checkNotNullEmpty($rootScope.currentDomain) && checkNotNullEmpty($rootScope.curUser)){
                            config.headers.d = $rootScope.curUser+":"+$rootScope.currentDomain;
                        }
                        return config;
                    },
                    responseError: function(rejection) {
                        if (!rejection.config.ignoreAuthModule) {
                            switch (rejection.status) {
                                case 401:
                                    var deferred = $q.defer();
                                    httpBuffer.append(rejection.config, deferred);
                                    $rootScope.$broadcast('event:auth-loginRequired', rejection);
                                    return deferred.promise;
                                case 404:
                                    if (!navigator.onLine)
                                        $rootScope.$broadcast('event:resource-notFound', rejection);
                                    break;
                                case 403:
                                    $rootScope.$broadcast('event:auth-forbidden', rejection);
                                    break;
                                case 409:
                                    deferred = $q.defer();
                                    httpBuffer.append(rejection.config, deferred);
                                    $rootScope.$broadcast('event:reload-page', rejection);
                                    return deferred.promise;
                            }
                        }
                        // otherwise, default behaviour
                        return $q.reject(rejection);
                    }
                };
            }]);
        }]);

    /**
     * Private module, a utility, required internally by 'http-auth-interceptor'.
     */
    angular.module('http-auth-interceptor-buffer', [])

        .factory('httpBuffer', ['$injector', function($injector) {
            /** Holds all the requests, so they can be re-requested in future. */
            var buffer = [];

            /** Service initialized later because of circular dependency problem. */
            var $http;

            function retryHttpRequest(config, deferred) {
                function successCallback(response) {
                    deferred.resolve(response);
                }
                function errorCallback(response) {
                    deferred.reject(response);
                }
                $http = $http || $injector.get('$http');
                $http(config).then(successCallback, errorCallback);
            }

            return {
                /**
                 * Appends HTTP request configuration object with deferred response attached to buffer.
                 */
                append: function(config, deferred) {
                    buffer.push({
                        config: config,
                        deferred: deferred
                    });
                },

                /**
                 * Abandon or reject (if reason provided) all the buffered requests.
                 */
                rejectAll: function(reason) {
                    if (reason) {
                        for (var i = 0; i < buffer.length; ++i) {
                            buffer[i].deferred.reject(reason);
                        }
                    }
                    buffer = [];
                },

                /**
                 * Retries all the buffered requests clears the buffer.
                 */
                retryAll: function(updater) {
                    for (var i = 0; i < buffer.length; ++i) {
                        retryHttpRequest(updater(buffer[i].config), buffer[i].deferred);
                    }
                    buffer = [];
                }
            };
        }]);
})();

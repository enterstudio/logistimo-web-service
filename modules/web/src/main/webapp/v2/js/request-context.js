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

(['ng', 'logistimoApp'], function (ng, logistimoApp) {
    "use strict";
    logistimoApp.service("requestContext",
        function (RenderContext) {
            function getAction() {
                return action;
            }
            function getNextSection(prefix) {
                if (!startsWith(prefix)) {
                    return null;
                }
                if (prefix === "") {
                    return sections[0];
                }
                var depth = prefix.split(".").length;
                if (depth === sections.length) {
                    return null;
                }
                return sections[depth];
            }
            function getParam(name, defaultValue) {
                if (ng.isUndefined(defaultValue)) {
                    defaultValue = null;
                }
                return params[name] || defaultValue;
            }
            function getParamAsInt(name, defaultValue) {
                var valueAsInt = ( this.getParam(name, defaultValue || 0) * 1 );
                if (isNaN(valueAsInt)) {
                    return defaultValue || 0;
                } else {
                    return valueAsInt;
                }
            }
            function getRenderContext(requestActionLocation, paramNames) {
                requestActionLocation = ( requestActionLocation || "" );
                paramNames = ( paramNames || [] );
                if (!ng.isArray(paramNames)) {
                    paramNames = [paramNames];
                }
                return new RenderContext(this, requestActionLocation, paramNames);
            }
            function hasActionChanged() {
                return action !== previousAction;
            }
            function hasParamChanged(paramName, paramValue) {
                if (!ng.isUndefined(paramValue)) {
                    return !isParam(paramName, paramValue);
                }
                if (!previousParams.hasOwnProperty(paramName) && params.hasOwnProperty(paramName)) {
                    return true;
                } else if (previousParams.hasOwnProperty(paramName) && !params.hasOwnProperty(paramName)) {
                    return true;
                }
                return previousParams[paramName] !== params[paramName];
            }
            function haveParamsChanged(paramNames) {
                for (var i = 0, length = paramNames.length; i < length; i++) {
                    if (hasParamChanged(paramNames[i])) {
                        return true;
                    }
                }
                return false;
            }
            function isParam(paramName, paramValue) {
                return !!(params.hasOwnProperty(paramName) && ( params[paramName] == paramValue ));
            }
            function setContext(newAction, newRouteParams) {
                previousAction = action;
                previousParams = params;
                action = newAction;
                sections = action.split(".");
                params = ng.copy(newRouteParams);
            }
            function startsWith(prefix) {
                return !!(!prefix.length || ( action === prefix ) || ( action.indexOf(prefix + ".") === 0 ));
            }
            var action = "";
            var sections = [];
            var params = {};
            var previousAction = "";
            var previousParams = {};
            return ({
                getNextSection: getNextSection,
                getParam: getParam,
                getParamAsInt: getParamAsInt,
                getRenderContext: getRenderContext,
                hasActionChanged: hasActionChanged,
                hasParamChanged: hasParamChanged,
                haveParamsChanged: haveParamsChanged,
                isParam: isParam,
                setContext: setContext,
                startsWith: startsWith,
                getAction: getAction
            });
        }
    );
})(angular, logistimoApp);
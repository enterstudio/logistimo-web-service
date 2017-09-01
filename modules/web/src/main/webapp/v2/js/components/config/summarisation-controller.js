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

/**
 * Created by mohan on 18/07/17.
 */

domainCfgControllers.controller('SummarisationMenuController', ['$scope', 'domainCfgService',
    function ($scope, domainCfgService) {
        $scope.$on("$routeChangeSuccess", function (event, current, previous) {
            if (current.params.esId != previous.params.esId) {
                $scope.editCounter.value = 0;
                $scope.config[$scope.subview] = angular.copy($scope.masterData[$scope.subview]);
                $scope.tags = angular.copy($scope.masterData.tags);
                $scope.subview = current.params.esId || Object.keys($scope.config)[0];
            }
        });
        $scope.editCounter = {};
        $scope.editCounter.value = 0;
        $scope.showLoading();
        domainCfgService.getEventSummaryConfig().then(function (data) {
            var esConfig = data.data;
            $scope.config = {};
            $scope.tags = {};
            if (checkNotNullEmpty(esConfig)) {
                $scope.config.eventdistribution = [];
                if(!checkNullEmptyObject(esConfig.tag)) {
                    $scope.tags = {
                        ttag: esConfig.tag,
                        tag: esConfig.tag
                    }
                }
                if (checkNotNullEmpty(esConfig.tag_distribution)) {
                    angular.forEach(esConfig.tag_distribution, function (tag) {
                        $scope.config.eventdistribution.push({text: tag, id: tag});
                    });
                }
                angular.forEach(esConfig.events, function (c) {
                    updateConfigCategory(c);
                });
                $scope.config = sortObject($scope.config);
            }
            $scope.subview = $scope.esId || Object.keys($scope.config)[0];
            $scope.masterData = angular.copy($scope.config);
            $scope.masterData.tags = angular.copy($scope.tags);
        }).catch(function error(msg) {
            $scope.showErrorMsg(msg);
        }).finally(function () {
            $scope.hideLoading();
        });

        $scope.buildTagElement = function (tagArray) {
            var tags = [];
            angular.forEach(tagArray, function (tag) {
                tags.push({text: tag, id: tag});
            });
            return tags;
        };

        function buildData(data) {
            if ($scope.isTagField(data.name)) {
                if (checkNotNullEmpty(data.values)) {
                    data.values = $scope.buildTagElement(data.values);
                }
            }
        }

        $scope.isTagField = function (field) {
            return (field == 'include_material_tags' || field == 'include_entity_tags' ||
            field == 'include_user_tags' || field == 'exclude_material_tags' ||
            field == 'exclude_entity_tags' || field == 'exclude_user_tags');
        };

        function updateConfigCategory(c) {
            var category = c.category.toLowerCase();
            if (checkNullEmpty($scope.config[category])) {
                $scope.config[category] = {};
                $scope.config[category]['type'] = [];
            }
            $scope.config[category]['type'].push(c.event_type);
            $scope.config[category][c.event_type] = {};
            $scope.config[category][c.event_type]['rows'] = [];
            $scope.config[category][c.event_type]['heading'] = [];
            $scope.config[category][c.event_type]['units'] = [];
            angular.forEach(c.thresholds, function (th) {
                var row = {};
                var hasData = false;
                angular.forEach(th.conditions, function (t) {
                    if ($scope.config[category][c.event_type]['heading'].indexOf(t.name) == -1) {
                        $scope.config[category][c.event_type]['heading'].push(t.name);
                        var units = '';
                        if (checkNotNullEmpty(t.oper)) {
                            units = t.oper;
                        }
                        if (checkNotNullEmpty(t.units)) {
                            units += (units ? " " : "") + t.units;
                        }
                        $scope.config[category][c.event_type]['units'].push(units);
                    }
                    buildData(t);
                    row[t.name] = t;
                    if (!hasData && (t.value || t.values)) {
                        hasData = true;
                    }
                });
                if (hasData) {
                    $scope.config[category][c.event_type]['rows'].push(row);
                }
                if (!$scope.config[category][c.event_type]['template']) {
                    var template = angular.copy(row);
                    for (var t in template) {
                        template[t].value = undefined;
                        template[t].values = undefined;
                    }
                    $scope.config[category][c.event_type]['template'] = template;
                }
            });
        }
    }


]);
domainCfgControllers.controller('SummarisationConfigurationController', ['$scope', 'domainCfgService',
    function ($scope, domainCfgService) {
        $scope.add = function (type) {
            setEditData($scope.config[$scope.subview][type]['template'], type, $scope.config[$scope.subview][type]['rows'].length);
            $scope.editCounter.value++;
        };

        $scope.remove = function (type, index) {
            $scope.config[$scope.subview][type]['rows'].splice(index, 1);
        };

        $scope.edit = function (type, index) {
            setEditData($scope.config[$scope.subview][type]['rows'][index], type, index);
            $scope.editCounter.value++;
        };


        $scope.displayTable = function (data, key, value) {
            if (key == 'include_material_tags' || key == 'include_entity_tags' || key == 'include_user_tags' || key == 'exclude_material_tags' || key == 'exclude_entity_tags' || key == 'exclude_user_tags') {
                var tags = '';
                if (checkNotNullEmpty(data.values)) {
                    angular.forEach(data.values, function (d) {
                        if (checkNotNullEmpty(tags)) {
                            tags += ",";
                        }
                        tags += d['id'];
                    });
                    return tags;
                }
            }
            return data['value'];
        };

        function updateConditions(th, events) {
            var event = {};
            var type = $scope.config[$scope.subview][th];
            var thresholds = [];
            if (type.rows.length > 0) {
                angular.forEach(type.rows, function (rows) {
                    var conditions = [];
                    angular.forEach(type.heading, function (heading) {
                        var rowCopy = angular.copy(rows[heading]);
                        var copy = true;
                        if (rows[heading].values instanceof Array) {
                            if (checkNotNullEmpty(rowCopy.values)) {
                                var values = [];
                                angular.forEach(rowCopy.values, function (tag) {
                                    values.push(tag['id']);
                                });
                                rowCopy.values = values;
                            } else {
                                copy = false;
                            }
                        } else {
                            if (checkNullEmpty(rowCopy.value)) {
                                copy = false;
                            }
                        }
                        if (copy) {
                            conditions.push(rowCopy);
                        }
                    });
                    thresholds.push({conditions: conditions});
                });
            }
            event.category = $scope.subview;
            event.event_type = th;
            event.thresholds = thresholds;
            events.push(event);
        }

        $scope.updateConfig = function () {
            var events = [];
            if ($scope.subview == 'eventdistribution') {
                var tagDistribution = [];
                angular.forEach($scope.config.eventdistribution, function (d) {
                    tagDistribution.push(d['text']);
                });
            } else {
                var typeArray = $scope.config[$scope.subview]['type'];
                angular.forEach(typeArray, function (th) {
                    updateConditions(th, events);
                });
            }
            $scope.showLoading();
            $scope.masterData[$scope.subview] = angular.copy($scope.config[$scope.subview]);
            $scope.masterData.tags = {tag: $scope.tags.ttag, ttag: $scope.tags.ttag};
            domainCfgService.setEventSummaryConfig({
                events: events,
                tag_distribution: tagDistribution,
                tag: $scope.tags.ttag
            }).then(function (data) {
                $scope.showSuccess(data.data);
            }).catch(function error(msg) {
                $scope.showErrorMsg(msg);
            }).finally(function () {
                $scope.hideLoading();
            });


        };
        function setEditData(data, type, index) {
            $scope.config[$scope.subview][type]['editData'] = {};
            $scope.config[$scope.subview][type]['editData']['data'] = data;
            $scope.config[$scope.subview][type]['editData']['type'] = type;
            $scope.config[$scope.subview][type]['editData']['index'] = index;
        }
    }
]);
domainCfgControllers.controller('AddSummarisationConfigurationController', ['$scope',
    function ($scope) {
        $scope.editData = angular.copy($scope.configData['data']);
        $scope.save = function () {
            var addData = $scope.editData;
            var isValueDefined = false;
            for (var property in addData) {
                if (addData.hasOwnProperty(property)) {
                    if (checkNotNullEmpty(addData[property].value) || checkNotNullEmpty(addData[property].values)) {
                        isValueDefined = true;
                        break;
                    }
                }
            }
            if (!isValueDefined) {
                $scope.showWarning($scope.resourceBundle['error.mandatory']);
                return;
            }
            $scope.config[$scope.subview][$scope.configData['type']]['rows'][$scope.configData['index']] = $scope.editData;
            $scope.config[$scope.subview][$scope.configData['type']]['editData'] = undefined;
            $scope.editCounter.value--;
        };

        $scope.cancel = function () {
            $scope.config[$scope.subview][$scope.configData['type']]['editData'] = undefined;
            $scope.editCounter.value--;

        };


        $scope.getTagType = function (field) {
            if (field == 'include_material_tags' || field == 'exclude_material_tags')
                return "material";
            else if (field == 'include_entity_tags' || field == 'exclude_entity_tags')
                return "entity";
            else if (field == 'include_user_tags' || field == 'exclude_user_tags')
                return "user";
        }
    }
]);

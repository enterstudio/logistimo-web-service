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

logistimoApp.directive('ngConfirmClick', [function () {
    return {
        link: function (scope, element, attr) {
            var msg = attr.ngConfirmClick || "Are you sure?";
            var clickAction = attr.confirmedClick;
            var cancelAction = attr.cancelClick;
            element.bind('click', function (event) {
                if (window.confirm(msg)) {
                    scope.$eval(clickAction)
                } else {
                    scope.$eval(cancelAction)
                }
            });
        }
    };
}]);
logistimoApp.directive('ngLogiTags', function () {
    return {
        restrict: 'A',
        require: '^tgs',
        replace: true,
        template: '<span class="label label-default mt5 dinline"  ng-repeat="tg in tgs">{{tg}}</span>',
        scope: {tgs: '='}
    }
});
logistimoApp.directive('ngMapTabSwitch', function () {
    return {
        restrict: 'A',
        require: '^vw',
        replace: true,
        template: '<div class="btn-group"><label class="btn btn-sm btn-default" ng-model="vw" uib-btn-radio="\'t\'" uib-tooltip="{{$parent.resourceBundle[\'table.view\']}}"><span class="glyphicons glyphicons-table"></span></label><label class="btn btn-sm btn-default" ng-model="vw" uib-btn-radio="\'m\'" uib-tooltip="{{$parent.resourceBundle[\'map.view\']}}"><span class="glyphicons glyphicons-globe"></span></label><label ng-show="showNetwork" class="btn btn-sm btn-default" ng-model="vw" uib-btn-radio="\'h\'" uib-tooltip="{{$parent.resourceBundle[\'network.view\']}}"><span class="glyphicons glyphicons-cluster"></span></label></div>',
        scope: {
            vw: '=',
            showNetwork: '@'
        },
        controller: ['$scope', '$location', function ($scope, $location) {
            $scope.$watch("vw", watchfn("vw", "t", $location, $scope));
        }]
    }
});
logistimoApp.directive('ngChartTabSwitch', function () {
    return {
        restrict: 'A',
        require: '^vw',
        replace: true,
        template: '<div class="btn-group"><label class="btn btn-sm btn-default" ng-model="vw" uib-btn-radio="\'c\'"><span class="glyphicons glyphicons-stats"></span></label> <label class="btn btn-sm btn-default" ng-model="vw" uib-btn-radio="\'t\'"><span class="glyphicons glyphicons-table"></span></label></div>',
        scope: {vw: '='},
        controller: ['$scope', '$location', function ($scope, $location) {
            $scope.$watch("vw", watchfn("vw", "c", $location, $scope));
        }]
    }
});
logistimoApp.directive('ngDayMonthSwitch', function () {
    return {
        restrict: 'A',
        require: '^vw',
        replace: true,
        template: '<div class="btn-group nv-align"><label class="btn btn-sm btn-default" ng-model="vw" uib-btn-radio="\'d\'">Day</label> <label class="btn btn-sm btn-default" ng-model="vw" uib-btn-radio="\'m\'">Month</label></div>',
        scope: {vw: '='},
        controller: ['$scope', '$location', function ($scope, $location) {
            $scope.$watch("vw", watchfn("vw", "m", $location, $scope));
        }]
    }
});
logistimoApp.directive('tagFilter', function () {
    return {
        restrict: 'AE',
        transient: true,
        templateUrl: 'views/tag-select.html',
        scope: {
            tagType: '@',
            tag: '=ngModel',
            isDisabled: '='
        },
        controller: ['$scope', '$location', 'domainCfgService', function ($scope, $location, domainCfgService) {
            $scope.isopen = false;
            $scope.tags = {};
            if ($scope.tagType === "entity") {
                domainCfgService.getEntityTagsCfg().then(function (data) {
                    $scope.tags = data.data.tags;
                });
            } else if ($scope.tagType === "material") {
                domainCfgService.getMaterialTagsCfg().then(function (data) {
                    $scope.tags = data.data.tags;
                });
            } else if ($scope.tagType === "route") {
                domainCfgService.getRouteTagsCfg().then(function (data) {
                    $scope.tags = data.data.tags;
                });
            } else if ($scope.tagType === "order") {
                domainCfgService.getOrderTagsCfg().then(function (data) {
                    $scope.tags = data.data.tags;
                });
            } else if ($scope.tagType === "user") {
                domainCfgService.getUserTagsCfg().then(function (data) {
                    $scope.tags = data.data.tags;
                });
            }

            $scope.setTag = function (tName) {
                $scope.tag = tName;
            };
        }]
    }
});
logistimoApp.directive('editableText', function () {
    return {
        restrict: 'E',
        template: '<div style="position:relative;"><input placeholder="{{placeHolder}}" maxlength="{{maxLength}}" type="text" class="form-control input-sm" ng-model="editModel" sync-focus-with="setFocus"> <span class="fix-bot-right"> <span ng-click="ok()" class="glyphicons glyphicons-ok clickable editBtn"></span> <span ng-click="cancel()" class="glyphicons glyphicons-remove clickable editBtn"></span></span></div>',
        scope: {
            editModel: '=',
            onOkCallback: '&onOk',
            onCancelCallback: '&onCancel',
            placeHolder: '@',
            setFocus: '@',
            maxLength:'@'
        },
        controller: ['$scope', function ($scope) {
            $scope.maxLength = $scope.maxLength || 255;
            $scope.ok = function () {
                $scope.onOkCallback();
            };

            $scope.cancel = function () {
                $scope.onCancelCallback();
            };
        }]
    }
});
logistimoApp.directive('syncFocusWith', function () {
    return {
        restrict: 'A',
        scope: {
            focusValue: "=syncFocusWith"
        },
        link: function (scope, element, attrs) {
            scope.$watch("focusValue", function (currentValue, previousValue) {
                if (currentValue === "true") {
                    element[0].focus();
                }
            })
        }
    }
});
logistimoApp.directive('editableBox', function ($parse) {
    return {
        restrict: 'A',
        link: function (scope, element, attrs) {
            element.addClass('editBoxPadding');
            element.append("<span id='ge' style='display: inline-block;width:13px;float:right'><span class='glyphicons glyphicons-edit'></span></span>");
            var nodes = element[0].childNodes;

            function showBox(value) {
                for (var i = 0; i < nodes.length; i++) {
                    if (nodes[i].id === 'ge') {
                        if (value == true) {
                            nodes[i].childNodes[0].style.display = 'block';
                        } else {
                            nodes[i].childNodes[0].style.display = 'none';
                        }
                        break;
                    }
                }
            }

            showBox(false);
            element.bind("mouseover", function () {
                element.addClass('editBox');
                showBox(true);
            });
            element.bind("mouseout", function () {
                element.removeClass('editBox');
                showBox(false);
            });
        }
    }
});
logistimoApp.directive('locationSelect', function(){
    return {
        restrict: 'E',
        scope: {
            filterModel: '=',
            placeHolder: '@',
            filterType: '@',
            ngDisabled:'='
        },
        controller: ['$scope', 'entityService', function ($scope, entityService) {
            $scope.$watch('model', function (newValue, oldValue) {
                if ((oldValue == undefined && newValue != undefined) ||
                        (oldValue != undefined && newValue == undefined) ||
                        (newValue != undefined && oldValue != undefined && newValue.label != oldValue.label)) {
                        if($scope.filterType == 'state'){
                            $scope.filterModel.state = newValue;
                            if(oldValue != undefined && !oldValue.autofilled){
                                $scope.filterModel.district = undefined;
                                $scope.filterModel.taluk = undefined;
                            }
                        } else if($scope.filterType == 'district'){
                            if(checkNullEmpty(newValue)){
                                $scope.filterModel.district = undefined;
                            } else if(checkNotNullEmpty($scope.model.state)){
                                $scope.filterModel.district = newValue;
                                $scope.filterModel.state = {};
                                $scope.filterModel.state.label = $scope.model.state;
                                $scope.filterModel.state.autofilled = true;
                            }
                            if(oldValue != undefined && !oldValue.autofilled){
                                $scope.filterModel.taluk = undefined;
                            }
                        } else if($scope.filterType == 'taluk'){
                            if(checkNullEmpty(newValue)){
                                $scope.filterModel.taluk = undefined;
                            } else if(checkNotNullEmpty($scope.model.state) && checkNotNullEmpty($scope.model.district)){
                                $scope.filterModel.taluk = newValue;
                                $scope.filterModel.state = {};
                                $scope.filterModel.state.label = $scope.model.state;
                                $scope.filterModel.state.autofilled = true;
                                $scope.filterModel.district = {};
                                $scope.filterModel.district.label = $scope.model.district;
                                $scope.filterModel.district.state = $scope.model.state;
                                $scope.filterModel.district.autofilled = true;
                            }
                        }
                    }
            });
            $scope.$watch('filterModel', function(newValue, oldValue) {
                if(newValue != oldValue) {
                    setModel(newValue);
                }
            });

            function setModel(newValue){
                if(checkNotNullEmpty($scope.filterModel)) {
                    if($scope.filterType == 'taluk') {
                        $scope.model = newValue.taluk;
                    }else if($scope.filterType == 'district') {
                        $scope.model = newValue.district;
                    }if($scope.filterType == 'state') {
                        $scope.model = newValue.state;
                    }
                }
            }
            setModel($scope.filterModel);
            $scope.getData = function (text) {
                var parentLocation={};
                if($scope.filterType == 'district' && $scope.filterModel != undefined && checkNotNullEmpty($scope.filterModel.state)){
                    parentLocation['state'] = $scope.filterModel.state.label;
                }else if($scope.filterType == 'taluk' && $scope.filterModel != undefined){
                    if(checkNotNullEmpty($scope.filterModel.district)){
                        parentLocation['district'] = $scope.filterModel.district.label;
                    }
                    if(checkNotNullEmpty($scope.filterModel.state)){
                        parentLocation['state'] = $scope.filterModel.state.label;
                    }
                }
                return entityService.getLocationSuggestion(text, $scope.filterType, parentLocation).then(function (data) {
                    var d = [];
                    angular.forEach(data.data, function(l){
                        d.push(l);
                    });
                    $scope.filteredData = d;
                    return d;
                });
            };
        }],
        templateUrl: 'views/location-select.html'
    };
});
logistimoApp.directive('tagSelect', function ($compile) {
    var multiple = '<lg-uib-select multiple="multiple" ng-disabled="disabled" query="query(q)" ui-model="tagsModel" place-holder="{{placeHolder}}"> </lg-uib-select>';
    var single = '<lg-uib-select ng-disabled="disabled" allow-clear="allow-clear" query="query(q)" ui-model="tagsModel" place-holder="{{placeHolder}}"> </lg-uib-select>';
    return {
        restrict: 'AE',
        replace: true,
        scope: {
            tagsModel: '=',
            type: '=',
            multiple: '=?',
            placeHolder: '@',
            disabled: '=',
            forceNoUdf: '@'
        },
        controller: ['$scope', '$location', 'domainCfgService', function ($scope, $location, domainCfgService) {
            if (!$scope.multiple) {
                $scope.multiple = "true";
            }
            $scope.tags = {};
            $scope.udf = false;
            if ($scope.type === "entity") {
                domainCfgService.getEntityTagsCfg().then(function (data) {
                    $scope.tags = data.data.tags;
                    $scope.udf = data.data.udf;
                });
            } else if ($scope.type === "material") {
                domainCfgService.getMaterialTagsCfg().then(function (data) {
                    $scope.tags = data.data.tags;
                    $scope.udf = data.data.udf;
                });
            } else if ($scope.type === "order") {
                domainCfgService.getOrderTagsCfg().then(function (data) {
                    $scope.tags = data.data.tags;
                    $scope.udf = data.data.udf;
                });
            } else if ($scope.type === "user") {
                domainCfgService.getUserTagsCfg().then(function (data) {
                    $scope.tags = data.data.tags;
                    $scope.udf = data.data.udf;
                });
            }
            $scope.isSelected = function (i) {
                for (t in $scope.tagsModel) {
                    if (i == $scope.tagsModel[t]) {
                        return true;
                    }
                }
                return false;
            };
            $scope.query = function (query) {
                var data = {results: []};
                var term = query.term.toLowerCase();
                for (var i in $scope.tags) {
                    var tag = $scope.tags[i].toLowerCase();
                    if (!$scope.isSelected($scope.tags[i]) && tag.indexOf(term) >= 0) {
                        data.results.push({'text': $scope.tags[i], 'id': $scope.tags[i]});
                    }
                }
                if (!$scope.forceNoUdf && $scope.udf && !$scope.isSelected(term)) {
                    term = term.replace(/,/g,"");
                    data.results.push({'text': term, 'id': term})
                }
                query.callback(data);
            }
        }],
        link: function (scope, iElement, iAttrs, ctrl) {
            if (scope.multiple != 'false') {
                iElement.html(multiple).show();
            } else {
                iElement.html(single).show();
            }
            $compile(iElement.contents())(scope);
        }
    }
});
logistimoApp.directive('userSelect', function ($compile) {
    var multiple = '<div><a ng-blur="bCallback()" href="">' +
        '<lg-uib-select multiple="multiple" query="query(q)" ui-model="dUserModel" place-holder="{{placeHolder}}"> </lg-uib-select>' +
        '</a></div>';
    var singleNew = '<div class="form-group has-feedback mgh0 mgb0"><input ng-blur="bCallback()" type="text" typeahead-editable="false" ng-model="dUserModel" class="form-control" placeholder="{{placeHolder}}" uib-typeahead="item as item.text for item in query($viewValue) | limitTo:8"' +
        'class="form-control" maxlength="50" typeahead-on-select="setUsersModel($item)"  typeahead-loading="loadingUser"/><span ng-show="loadingUser" class="form-control-feedback typehead-loading" aria-hidden="true"> <span class="glyphicons glyphicons-cogwheel spin"></span> </span></div>';
    return {
        restrict: 'AE',
        scope: {
            usersModel: '=usersModel',
            multiple: '=?',
            callback: '&onSelect',
            placeHolder: "@",
            blurCallback: "&",
            onlyActive: "@",
            role: "@",
            includeSuperusers: "=",
            includeChildDomainUsers: "="
        },
        controller: ['$scope', '$location', 'userService', '$q', function ($scope, $location, userService, $q) {
            $scope.offset = 0;
            $scope.size = 10;
            if(checkNotNullEmpty($scope.usersModel)){
                $scope.dUserModel = angular.copy($scope.usersModel);
            }

            $scope.isSelected = function (i) {
                for (var t in $scope.dUserModel) {
                    if (i == $scope.dUserModel[t]) {
                        return true;
                    }
                }
                return false;
            };
            $scope.bCallback = function () {
                if (checkNotNull($scope.blurCallback)) {
                    $scope.blurCallback();
                }
            };
            $scope.equals = function (m1, m2) {
                if (checkNotNullEmpty(m1) != checkNotNullEmpty(m2)) {
                    return false;
                } else if (angular.isArray(m1) && angular.isArray(m2)) {
                    if (m1.length != m2.length) {
                        return false;
                    } else {
                        for (i in m1) {
                            if (!angular.equals(m1[i], m1[i])) {
                                return false;
                            }
                        }
                        return true;
                    }
                } else if (angular.isArray(m1) != angular.isArray(m2)) {
                    return false;
                } else {
                    return angular.equals(m1, m2);
                }
            };
            $scope.$watch("usersModel", function (newVal, oldVal) {
                if (!$scope.equals(newVal, $scope.dUserModel)) {
                    $scope.dUserModel = angular.copy(newVal);
                    $scope.checkUsers();
                }
            }, true);
            $scope.$watch("dUserModel", function (newVal, oldVal) {
                if (!$scope.equals(newVal, $scope.usersModel)) {
                    $scope.usersModel = angular.copy(newVal);
                }
            }, true);
            $scope.setUsersModel = function (newVal, skipCallback) {
                $scope.dUserModel = angular.copy(newVal);
                if (!skipCallback && checkNotNull($scope.callback)) {
                    //$scope.callback(newVal);
                    $scope.callback({data: newVal.id});
                }
            };
            $scope.checkModel = function () {
                if (checkNullEmpty($scope.dUserModel)) {
                    $scope.usersModel = null;
                }
            };

            $scope.checkUsers = function () {
                if (checkNotNullEmpty($scope.usersModel)) {
                    $scope.loading = true;
                    if ($scope.multiple == 'false') {
                        if (checkNullEmpty($scope.usersModel.text)) {
                            userService.getUserMeta($scope.usersModel.id).then(function (data) {
                                $scope.usersModel.text = data.data.fnm + ' [' + data.data.id + ']';
                            }).catch(function (err) {
                                $scope.$parent.showErrorMsg($scope.$parent.resourceBundle['user.details.fetch.error'] + ' ' + $scope.usersModel.id);
                            }).finally(function () {
                                $scope.loading = false;
                            });
                        }
                    } else {
                        $scope.loading = true;
                        var loaded = 0;
                        for (var i in $scope.usersModel) {
                            var userModel = $scope.usersModel[i];
                            if (checkNullEmpty(userModel.text)) {
                                userService.getUserMeta(userModel.id).then(function (data) {
                                    for (var i in $scope.usersModel) {
                                        if ($scope.usersModel[i].id == data.data.id) {
                                            $scope.usersModel[i].text = data.data.fnm + ' [' + data.data.id + ']';
                                            $scope.setUsersModel($scope.usersModel, true);
                                            break;
                                        }
                                    }
                                }).catch(function () {
                                    $scope.$parent.showErrorMsg($scope.$parent.resourceBundle['user.details.fetch.error'] + ' ' + userModel.id);
                                }).finally(function () {
                                    if ($scope.usersModel.length == ++loaded) {
                                        $scope.loading = false;
                                    }
                                });
                            }
                        }
                    }
                }
            };
            $scope.checkUsers();

            $scope.setResults = function (query, results, deferred) {
                var rdata = {results: []};
                for (var i in results) {
                    var user = results[i];
                    if (!$scope.isSelected(user.id)) {
                        rdata.results.push({'text': user.fnm + ' [' + user.id + ']', 'id': user.id});
                    }
                }
                if ($scope.multiple != 'false') {
                    query.callback(rdata);
                } else {
                    deferred.resolve(rdata.results);
                }
            };

            $scope.query = function (query) {
                var term = checkNotNull(query.term) ? query.term.toLowerCase() : query.toLowerCase();
                var deferred = $q.defer();
                if (checkNullEmpty($scope.uRole)) {
                    var utype = undefined;
                    if ($scope.onlyActive == 'true') {
                        utype = "au";
                    }
                    userService.getDomainUsers(term, $scope.offset, $scope.size, utype, $scope.includeSuperusers, $scope.includeChildDomainUsers).then(function (data) {
                        $scope.setResults(query, data.data.results, deferred);
                    }).catch(function error(msg) {
                        $scope.$parent.showErrorMsg(msg);
                        deferred.reject(msg);
                    });
                } else {
                    userService.getUsersByRole($scope.uRole, term, $scope.offset, $scope.size).then(function (data) {
                        $scope.setResults(query, data.data.results, deferred);
                    }).catch(function error(msg) {
                        $scope.$parent.showErrorMsg(msg);
                        deferred.reject(msg);
                    });
                }

                if ($scope.multiple == 'false') {
                    return deferred.promise;
                }
            }
        }],
        link: function (scope, iElement, iAttrs, ctrl) {
            if (scope.multiple != 'false') {
                iElement.html(multiple).show();
            } else {
                iElement.html(singleNew).show();
            }
            if (scope.role == 'admin') {
                scope.uRole = 'ROLE_do';
            } else if (scope.role == 'mgr') {
                scope.uRole = 'ROLE_sm';
            } else if (scope.role == 'su') {
                scope.uRole = 'ROLE_su';
            } else if (scope.role == 'oper') {
                scope.uRole = 'ROLE_ko';
            }
            $compile(iElement.contents())(scope);
        }
    }
});
logistimoApp.directive('entSelect', function () {
    return {
        restrict: 'AE',
        replace: true,
        template: '<div>' +
        '<lg-uib-select multiple="multiple" query="query(q)" ui-model="entModel" place-holder="Select {{$parent.resourceBundle[\'kiosks.lowercase\']}}"> </lg-uib-select>' +
        '</div>',
        scope: {
            entModel: '=entModel'
        },
        controller: ['$scope', '$location', 'entityService', function ($scope, $location, entityService) {
            $scope.offset = 0;
            $scope.size = 10;
            $scope.isSelected = function (i) {
                for (t in $scope.entModel) {
                    if (i == $scope.entModel[t]) {
                        return true;
                    }
                }
                return false;
            };
            $scope.query = function (query) {
                var term = checkNotNull(query.term) ? query.term.toLowerCase() : query.toLowerCase();
                entityService.getFilteredEntity(term).then(function (data) {
                    var rdata = {results: []};
                    for (var i in data.data.results) {
                        var entity = data.data.results[i];
                        if (!$scope.isSelected(entity.id)) {
                            rdata.results.push({'text': entity.nm, 'id': entity.id});
                        }
                    }
                    query.callback(rdata);
                }).catch(function error(msg) {
                    $scope.$parent.showErrorMsg(msg);
                });
            }
        }]
    }
});

logistimoApp.directive('childDomainSelect', function () {
    return {
        restrict: 'AE',
        replace: true,
        template: '<div>' +
        '<lg-uib-select multiple="multiple" query="query(q)" ui-model="childDomainModel" place-holder="Select Domains"> </lg-uib-select>' +
        '</div>',
        scope: {
            childDomainModel: '=childDomainModel',
            domainId: '='
        },
        controller: ['$scope', '$location', 'linkedDomainService', function ($scope, $location, linkedDomainService) {
            $scope.isSelected = function (i) {
                for (t in $scope.childDomainModel) {
                    if (i == $scope.childDomainModel[t]) {
                        return true;
                    }
                }
                return false;
            };
            $scope.query = function (query) {
                var term = checkNotNull(query.term) ? query.term.toLowerCase() : query.toLowerCase();
                linkedDomainService.getChildSuggestions(term, $scope.domainId).then(function (data) {
                    var rdata = {results: []};
                    for (var i in data.data) {
                        var child = data.data[i];
                        if (!$scope.isSelected(child.id)) {
                            rdata.results.push({'text': child.text, 'id': child.id});
                        }
                    }
                    query.callback(rdata);
                }).catch(function error(msg) {
                    $scope.$parent.showErrorMsg(msg);
                });
            }
        }]
    }
});
logistimoApp.directive('entityDomainSelect', function () {
    return {
        restrict: 'E',
        replace: true,
        template: '<div>' +
        '<lg-uib-select multiple="multiple" query="query(q)" ui-model="model" place-holder="Choose domains"> </lg-uib-select>' +
        '</div>',
        scope: {
            entityId: '=',
            model: '='
        },
        controller: ['$scope', '$location', 'entityService', function ($scope, $location, entityService) {
            $scope.query = function (query) {
                entityService.getFilteredDomains($scope.entityId, query.term).then(function (data) {
                    var rData = {results: []};
                    for (var i in data.data) {
                        rData.results.push({'text': data.data[i].text, 'id': data.data[i].id});
                    }
                    query.callback(rData);
                }).catch(function error(msg) {
                    $scope.$parent.showErrorMsg(msg);
                });
            }
        }]
    }
});
logistimoApp.directive('domainTagSelect', function ($compile) {
    var multiple = '<lg-uib-select multiple="multiple" reset="reset" ng-disabled="disabled" query="query(q)" ui-model="tagsModel" place-holder="{{placeHolder}}"> </lg-uib-select>';
    var single = '<lg-uib-select ng-disabled="disabled" allow-clear="allow-clear" query="query(q)" ui-model="tagsModel" place-holder="{{placeHolder}}"> </lg-uib-select>';
    return {
        restrict: 'AE',
        replace: true,
        scope: {
            tagsModel: '=',
            type: '=',
            multiple: '=?',
            placeHolder: '@',
            disabled: '=',
            forceNoUdf: '@',
            callback: '&onSelect',
            preSelected: '='
        },
        controller: ['$scope', '$location', 'domainCfgService', function ($scope, $location, domainCfgService) {
            if (!$scope.multiple) {
                $scope.multiple = "true";
            }
            $scope.tags = {};
            $scope.udf = false;
            if ($scope.type === "entity") {
                domainCfgService.getEntityTagsCfg().then(function (data) {
                    $scope.tags = data.data.tags;
                    $scope.udf = data.data.udf;
                });
            } else if ($scope.type === "material") {
                domainCfgService.getMaterialTagsCfg().then(function (data) {
                    $scope.tags = data.data.tags;
                    $scope.udf = data.data.udf;
                });
            } else if ($scope.type === "order") {
                domainCfgService.getOrderTagsCfg().then(function (data) {
                    $scope.tags = data.data.tags;
                    $scope.udf = data.data.udf;
                });
            } else if ($scope.type === "user") {
                domainCfgService.getUserTagsCfg().then(function (data) {
                    $scope.tags = data.data.tags;
                    $scope.udf = data.data.udf;
                });
            }
            $scope.isSelected = function (i) {
                for (t in $scope.tagsModel) {
                    if (i == $scope.tagsModel[t]) {
                        return true;
                    }
                }
                for(j in $scope.preSelected) {
                    if(i == $scope.preSelected[j].id) {
                        return true;
                    }
                }
                return false;
            };
            $scope.$watch('tagsModel', function(oldval, newval) {
                if(checkNotNullEmpty($scope.callback)) {
                    $scope.callback();
                }
            });
            $scope.query = function (query) {
                if(checkNotNullEmpty(query) && checkNotNullEmpty(query.term)) {
                    var data = {results: []};
                    var term = query.term.toLowerCase();
                    for (var i in $scope.tags) {
                        var tag = $scope.tags[i].toLowerCase();
                        if (!$scope.isSelected($scope.tags[i]) && tag.indexOf(term) >= 0) {
                            data.results.push({'text': $scope.tags[i], 'id': $scope.tags[i]});
                        }
                    }
                    if (!$scope.forceNoUdf && $scope.udf && !$scope.isSelected(term)) {
                        term = term.replace(/,/g,"");
                        data.results.push({'text': term, 'id': term})
                    }
                    query.callback(data);
                }
            }
        }],
        link: function (scope, iElement, iAttrs, ctrl) {
            if (scope.multiple != 'false') {
                iElement.html(multiple).show();
            } else {
                iElement.html(single).show();
            }
            $compile(iElement.contents())(scope);
        }
    };
});
logistimoApp.directive('domainUserSelect', function ($compile) {
    var multiple = '<div><a ng-blur="bCallback()" href="">' +
        '<lg-uib-select multiple="multiple" reset="reset" query="query(q)" limit="2" ui-model="dUserModel" place-holder="{{placeHolder}}"> </lg-uib-select>' +
        '</a></div>';
    var singleNew = '<div class="form-group has-feedback mgh0 mgb0"><input ng-blur="bCallback()" type="text" typeahead-editable="false" ng-model="dUserModel" class="form-control" placeholder="{{placeHolder}}" uib-typeahead="item as item.text for item in query($viewValue) | limitTo:8"' +
        'class="form-control" maxlength="50" typeahead-on-select="setUsersModel($item)"  typeahead-loading="loadingUser"/><span ng-show="loadingUser" class="form-control-feedback typehead-loading" aria-hidden="true"> <span class="glyphicons glyphicons-cogwheel spin"></span> </span></div>';
    return {
        restrict: 'AE',
        scope: {
            usersModel: '=usersModel',
            multiple: '=?',
            reset: '=?',
            callback: '&onSelect',
            placeHolder: "@",
            blurCallback: "&",
            onlyActive: "@",
            role: "@",
            includeSuperusers: "=",
            includeChildDomainUsers: "=",
            preSelected: "="
        },
        controller: ['$scope', '$location', 'userService', '$q', function ($scope, $location, userService, $q) {
            $scope.offset = 0;
            $scope.size = 10;
            if(checkNotNullEmpty($scope.usersModel)){
                $scope.dUserModel = angular.copy($scope.usersModel);
            }

            $scope.isSelected = function (i) {
                for (var t in $scope.dUserModel) {
                    if (i == $scope.dUserModel[t].id) {
                        return true;
                    }
                }
                if(checkNotNullEmpty($scope.preSelected)) {
                    for(var j in $scope.preSelected) {
                        if(i == $scope.preSelected[j].id) {
                            return true;
                        }
                    }
                }
                return false;
            };
            $scope.bCallback = function () {
                if (checkNotNull($scope.blurCallback)) {
                    $scope.blurCallback();
                }
            };
            $scope.equals = function (m1, m2) {
                if (checkNotNullEmpty(m1) != checkNotNullEmpty(m2)) {
                    return false;
                } else if (angular.isArray(m1) && angular.isArray(m2)) {
                    if (m1.length != m2.length) {
                        return false;
                    } else {
                        for (i in m1) {
                            if (!angular.equals(m1[i], m1[i])) {
                                return false;
                            }
                        }
                        return true;
                    }
                } else if (angular.isArray(m1) != angular.isArray(m2)) {
                    return false;
                } else {
                    return angular.equals(m1, m2);
                }
            };
            $scope.$watch("usersModel", function (newVal, oldVal) {
                if (!$scope.equals(newVal, $scope.dUserModel)) {
                    $scope.dUserModel = angular.copy(newVal);
                    $scope.checkUsers();
                }
            }, true);
            $scope.$watch("dUserModel", function (newVal, oldVal) {
                if (!$scope.equals(newVal, $scope.usersModel)) {
                    $scope.usersModel = angular.copy(newVal);
                }
            }, true);
            $scope.setUsersModel = function (newVal, skipCallback) {
                $scope.dUserModel = angular.copy(newVal);
                if (!skipCallback && checkNotNull($scope.callback)) {
                    //$scope.callback(newVal);
                    $scope.callback({data: newVal.id});
                }
            };
            $scope.checkModel = function () {
                if (checkNullEmpty($scope.dUserModel)) {
                    $scope.usersModel = null;
                }
            };

            $scope.checkUsers = function () {
                if (checkNotNullEmpty($scope.usersModel)) {
                    $scope.loading = true;
                    if ($scope.multiple == 'false') {
                        if (checkNullEmpty($scope.usersModel.text)) {
                            userService.getUserMeta($scope.usersModel.id).then(function (data) {
                                $scope.usersModel.text = data.data.fnm + ' [' + data.data.id + ']';
                            }).catch(function (err) {
                                $scope.$parent.showErrorMsg($scope.$parent.resourceBundle['user.details.fetch.error'] + ' ' + $scope.usersModel.id);
                            }).finally(function () {
                                $scope.loading = false;
                            });
                        }
                    } else {
                        $scope.loading = true;
                        var loaded = 0;
                        for (var i in $scope.usersModel) {
                            var userModel = $scope.usersModel[i];
                            if (checkNullEmpty(userModel.text)) {
                                userService.getUserMeta(userModel.id).then(function (data) {
                                    for (var i in $scope.usersModel) {
                                        if ($scope.usersModel[i].id == data.data.id) {
                                            $scope.usersModel[i].text = data.data.fnm + ' [' + data.data.id + ']';
                                            $scope.setUsersModel($scope.usersModel, true);
                                            break;
                                        }
                                    }
                                }).catch(function () {
                                    $scope.$parent.showErrorMsg($scope.$parent.resourceBundle['user.details.fetch.error'] + ' ' + userModel.id);
                                }).finally(function () {
                                    if ($scope.usersModel.length == ++loaded) {
                                        $scope.loading = false;
                                    }
                                });
                            }
                        }
                    }
                }
            };
            $scope.checkUsers();

            $scope.setResults = function (query, results, deferred) {
                var rdata = {results: []};
                for (var i in results) {
                    var user = results[i];
                    if (!$scope.isSelected(user.id)) {
                        rdata.results.push({'text': user.fnm + ' [' + user.id + ']', 'id': user.id});
                    }
                }
                if ($scope.multiple != 'false') {
                    query.callback(rdata);
                } else {
                    deferred.resolve(rdata.results);
                }
            };

            $scope.query = function (query) {
                if(checkNotNullEmpty(query.term)) {
                    var term = query.term.toLowerCase();
                    var deferred = $q.defer();
                    if (checkNullEmpty($scope.uRole)) {
                        var utype = undefined;
                        if ($scope.onlyActive == 'true') {
                            utype = "au";
                        }
                        userService.getDomainUsers(term, $scope.offset, $scope.size, utype, $scope.includeSuperusers, $scope.includeChildDomainUsers).then(function (data) {
                            $scope.setResults(query, data.data.results, deferred);
                        }).catch(function error(msg) {
                            $scope.$parent.showErrorMsg(msg);
                            deferred.reject(msg);
                        });
                    } else {
                        userService.getUsersByRole($scope.uRole, term, $scope.offset, $scope.size).then(function (data) {
                            $scope.setResults(query, data.data.results, deferred);
                        }).catch(function error(msg) {
                            $scope.$parent.showErrorMsg(msg);
                            deferred.reject(msg);
                        });
                    }
                }
                if ($scope.multiple == 'false') {
                    return deferred.promise;
                }
            }
        }],
        link: function (scope, iElement, iAttrs, ctrl) {
            if (scope.multiple != 'false') {
                iElement.html(multiple).show();
            } else {
                iElement.html(singleNew).show();
            }
            if (scope.role == 'admin') {
                scope.uRole = 'ROLE_do';
            } else if (scope.role == 'mgr') {
                scope.uRole = 'ROLE_sm';
            } else if (scope.role == 'su') {
                scope.uRole = 'ROLE_su';
            } else if (scope.role == 'oper') {
                scope.uRole = 'ROLE_ko';
            }
            $compile(iElement.contents())(scope);
        }
    }
});
logistimoApp.directive('moveDomainSelect', function () {
    return {
        restrict: 'E',
        replace: true,
        template: '<div>' +
        '<lg-uib-select query="query(q)" ui-model="model" place-holder="Select Domain"> </lg-uib-select>' +
        '</div>',
        scope: {
            model: '='
        },
        controller: ['$scope', 'domainService', function ($scope, domainService) {
            $scope.query = function (query) {
                domainService.getDomainSuggestions(query.term).then(function (data) {
                    var rData = {results: []};
                    if (checkNotNullEmpty(data.data)) {
                        data.data.forEach(function (d) {
                            rData.results.push({'text': d.name, 'id': d.dId});
                        });
                    }
                    query.callback(rData);
                }).catch(function error(msg) {
                    $scope.$parent.showErrorMsg(msg);
                });
            };
        }]
    }
});

logistimoApp.directive('domainSelect', function () {
    return {
        restrict: 'E',
        replace: true,
        template: '<div>' +
        '<lg-uib-select query="query(q)" ui-model="model" place-holder="{{placeHolder}}"> </lg-uib-select>' +
        '</div>',
        scope: {
            isSuperUser: '='
        },
        controller: ['$scope', '$location', '$window', 'domainService', 'linkedDomainService',
            function ($scope, $location, $window, domainService, linkedDomainService) {
                $scope.$watch("model", function (currentValue, previousValue) {
                    if ((currentValue != undefined && previousValue != undefined) && (currentValue != previousValue.id && currentValue.id != previousValue  )) {
                        $scope.$parent.showLoading();
                        console.log("switching domain " + currentValue.id + " : "+previousValue.id);
                        domainService.switchDomain(currentValue.id).then(function (data) {
                            $scope.success = data.data;
                            $window.location = "/v2/index.html";
                        }).catch(function error(msg) {
                            $scope.$parent.showErrorMsg(msg);
                        }).finally(function(){
                            $scope.$parent.hideLoading();
                        });
                    }
                });

                domainService.getCurrentDomain().then(function (data) {
                    $scope.model = {'text': data.data.name, 'id': data.data.dId};
                });
                $scope.query = function (query) {
                    if ($scope.isSuperUser) {
                        domainService.getDomainSuggestions(query.term).then(function (data) {
                            setQueryResults(data);
                        }).catch(function error(msg) {
                            $scope.$parent.showErrorMsg(msg);
                        });
                    } else {
                        //Todo: need to do by suggestion
                        /*linkedDomainService.getLinkedDomainSuggestion(query.term).then(function (data) {
                         setQueryResults(data);
                         }).catch(function error(msg) {
                         $scope.$parent.showErrorMsg(msg);
                         });*/
                        if(checkNullEmpty($scope.childDomains)) {
                            linkedDomainService.getLinkedDomainSuggestion().then(function (data) {
                                $scope.childDomains = data.data;
                            }).catch(function error(msg) {
                                $scope.$parent.showErrorMsg(msg);
                            }).finally(function(){
                                query.callback(setNonSUQueryResults($scope.childDomains));
                            });
                        } else {
                            query.callback(setNonSUQueryResults($scope.childDomains));
                        }
                    }
                    function setNonSUQueryResults(childDomains) {
                        var rData = {results: []};
                        if (checkNotNullEmpty(childDomains)) {
                            childDomains.some(function (d) {
                                if (d.name.toLowerCase().indexOf(query.term.toLowerCase()) == 0) {
                                    rData.results.push({'text': d.name, 'id': d.dId});
                                    if(rData.results.length == 10) {
                                        return true;
                                    }
                                }
                            });
                        }
                        return rData;
                    }
                    function setQueryResults(data) {
                        var rData = {results: []};
                        if (checkNotNullEmpty(data.data)) {
                            data.data.forEach(function (d) {
                                rData.results.push({'text': d.name, 'id': d.dId});
                            });
                        }
                        query.callback(rData);
                    }
                };
            }
        ]
    }
});

logistimoApp.directive('materialDropDown', function () {
    return {
        restrict: 'E',
        replace: true,
        template: '<span>' +
        '<lg-uib-select allow-clear="allow-clear" ng-disabled="disabled" query="query(q)" ui-model="ngModel" place-holder="All"> </lg-uib-select>' +
        '</span>',
        scope: {
            ngModel: "=",
            disabled: "=",
            mtag: "="
        },
        controller: ['$scope', 'matService',
            function ($scope, matService) {
                $scope.query = function (query) {
                    matService.getDomainMaterials(query.term, $scope.mtag, 0, 10).then(function (data) {
                        var rData = {results: []};
                        var matRes = data.data.results;
                        if (checkNotNullEmpty(matRes)) {
                            matRes.forEach(function (d) {
                                rData.results.push({'text': d.mnm, 'id': d.mId});
                            });
                        }
                        query.callback(rData);
                    }).catch(function error(msg) {
                        $scope.$parent.showErrorMsg(msg);
                    });
                };
            }
        ]
    }
});

logistimoApp.directive('lgValidateAfter', [function () {
    return {
        restrict: 'A',
        require: 'ngModel',
        link: function (scope, element, attrs, ctrl) {
            ctrl.validate = false;
            element.bind('focus', function (evt) {
                if (ctrl.validate && ctrl.$invalid) // if we focus and the field was invalid, keep the validation
                {
                    scope.$apply(function () {
                        ctrl.validate = true;
                    });
                } else {
                    scope.$apply(function () {
                        ctrl.validate = false;
                    });
                }
            }).bind('blur', function (evt) {
                scope.$apply(function () {
                    ctrl.validate = true;
                });
            });
        }
    }
}]);
/**
 * Checklist-model
 * AngularJS directive for list of checkboxes
 */
logistimoApp.directive('checklistModel', ['$parse', '$compile', function ($parse, $compile) {
    function contains(arr, item) {
        if (angular.isArray(arr)) {
            for (var i = 0; i < arr.length; i++) {
                if (angular.equals(arr[i], item)) {
                    return true;
                }
            }
        }
        return false;
    }

    function add(arr, item) {
        arr = angular.isArray(arr) ? arr : [];
        for (var i = 0; i < arr.length; i++) {
            if (angular.equals(arr[i], item)) {
                return arr;
            }
        }
        arr.push(item);
        return arr;
    }

    function remove(arr, item) {
        if (angular.isArray(arr)) {
            for (var i = 0; i < arr.length; i++) {
                if (angular.equals(arr[i], item)) {
                    arr.splice(i, 1);
                    break;
                }
            }
        }
        return arr;
    }

    function postLinkFn(scope, elem, attrs) {
        $compile(elem)(scope);
        var getter = $parse(attrs.checklistModel);
        var setter = getter.assign;
        var value = $parse(attrs.checklistValue)(scope);
        scope.$watch('checked', function (newValue, oldValue) {
            if (newValue === oldValue) {
                return;
            }
            var current = getter(scope);
            if (newValue === true) {
                setter(scope, add(current, value));
            } else {
                setter(scope, remove(current, value));
            }
        });
        scope.$watch(attrs.checklistModel, function (newArr, oldArr) {
            scope.checked = contains(newArr, value);
        }, true);
    }

    return {
        restrict: 'A',
        priority: 1000,
        terminal: true,
        scope: false,
        compile: function (tElement, tAttrs) {
            if (tElement[0].tagName !== 'INPUT' || !tElement.attr('type', 'checkbox')) {
                throw 'checklist-model should be applied to `input[type="checkbox"]`.';
            }
            if (!tAttrs.checklistValue) {
                throw 'You should provide `checklist-value`.';
            }
            tElement.removeAttr('checklist-model');
            tElement.attr('ng-model', 'checked');
            return postLinkFn;
        }
    };
}]);
logistimoApp.directive('onlyDigits', function () {
    return {
        require: 'ngModel',
        restrict: 'A',
        scope: {
            ngModel: "="
        },
        link: function (scope, element, attr, ctrl) {
            var allowNegative = checkNotNull(attr.allowNegative);
            var temperature = checkNotNull(attr.temperature);
            var allowDecimal = checkNotNull(attr.allowDecimal);
            var minValue = checkNotNull(attr.minValue) ? scope.$eval(attr.minValue) : null;
            var filterFloat = function (value) {
                var testStr = allowDecimal ? /^(\-|\+)?([0-9]*(\.[0-9]*)?)$/ : /^(\-|\+)?([0-9]*)$/;
                if (testStr.test(value))
                    return Number(value);
                return NaN;
            };

            function inputValue(val) {
                if (val) {
                    var change = false;
                    if (!allowNegative && !temperature && val.indexOf('-') == 0) {
                        change = true;
                    }
                    if (!change && isNaN(filterFloat(val))) {
                        change = true;
                    }
                    if (change && allowNegative && val === '-') {
                        change = false;
                    }
                    if (change && temperature && (val === '+' || val == '-')) {
                        change = false;
                    }
                    if (!change && checkNotNullEmpty(minValue) && val < minValue) {//only 1 digit limitation
                        change = true;
                    }
                    if (change) {
                        if (checkNullEmpty(scope.curVal)) {
                            scope.curVal = checkNull(ctrl.$modelValue) ? '' : ctrl.$modelValue;
                        }
                        ctrl.$setViewValue(scope.curVal + '');
                        ctrl.$render();
                    } else {
                        scope.curVal = val;
                    }
                    return scope.curVal;
                } else {
                    scope.curVal = val;
                }
                return '';
            }

            ctrl.$parsers.push(inputValue);
        }
    };
});
logistimoApp.directive('onlyAlphabets', function () {
    return {
        require: 'ngModel',
        restrict: 'A',
        scope: {
            ngModel: "="
        },
        link: function (scope, element, attr, ctrl) {
            var filterAlbhabets = function (value) {
                var testStr = /^([a-z ]*)$/i;
                if (testStr.test(value))
                    return value;
                return '';
            };

            function inputValue(val) {
                if (val) {
                    var change = false;
                    if (filterAlbhabets(val) == '') {
                        change = true;
                    }
                    if (change) {
                        if (checkNullEmpty(scope.curVal)) {
                            scope.curVal = checkNull(ctrl.$modelValue) ? '' : ctrl.$modelValue;
                        }
                        ctrl.$setViewValue(scope.curVal + '');
                        ctrl.$render();
                    } else {
                        scope.curVal = val;
                    }
                    return scope.curVal;
                } else {
                    scope.curVal = val;
                }
                return '';
            }

            ctrl.$parsers.push(inputValue);
        }
    };
});
logistimoApp.directive('contact', function () {
    return {
        require: 'ngModel',
        restrict: 'A',
        link: function (scope, element, attr, ctrl) {
            function inputValue(val) {
                if (val) {
                    var digits = val.replace(/[^+ 0-9]/g, '');
                    if (digits !== val) {
                        ctrl.$setViewValue(digits);
                        ctrl.$render();
                    }
                    return digits;
                }
                return undefined;
            }

            ctrl.$parsers.push(inputValue);
        }
    };
});
logistimoApp.directive('userId', function () {
    return {
        require: 'ngModel',
        restrict: 'A',
        link: function (scope, element, attr, ctrl) {
            function inputValue(val) {
                if (val) {
                    var digits = val.replace(/[^a-z0-9\.\-_@]/g, '');
                    if (digits !== val) {
                        ctrl.$setViewValue(digits);
                        ctrl.$render();
                    }
                    return digits;
                }
                return undefined;
            }

            ctrl.$parsers.push(inputValue);
        }
    };
});
logistimoApp.directive('zipCode', function () {
    return {
        require: 'ngModel',
        restrict: 'A',
        link: function (scope, element, attr, ctrl) {
            function inputValue(val) {
                if (val) {
                    var digits = val.replace(/[^a-z0-9\- ]/g, '');
                    if (digits !== val) {
                        ctrl.$setViewValue(digits);
                        ctrl.$render();
                    }
                    return digits;
                }
                return undefined;
            }

            ctrl.$parsers.push(inputValue);
        }
    };
});
logistimoApp.directive('ngFocusAsync', ['$parse', function ($parse) {
    return {
        compile: function ($element, attr) {
            var fn = $parse(attr.ngFocusAsync);
            return function (scope, element) {
                element.on('focus', function (event) {
                    scope.$evalAsync(function () {
                        fn(scope, {$event: event});
                    });
                });
            };
        }
    };
}]);
logistimoApp.directive('datePicker', function ($compile) {
    var day = '<span {{noclear}} class="datepick"><input type="text" ng-readonly="true" class="form-control datepickerreadonly" ng-class="extraClass" datepicker-options="dateOptions" placeholder="{{placeHolder}}" ng-click="open($event)" ng-focus-async="!IE && open($event)" ng-change="IE && validate()" uib-datepicker-popup="dd MMM yyyy" ng-model="dateModel" is-open="opened" close-text="Close" ng-disabled="disabled" ng-class="{datepickerreadonly: datePickerReadOnlyCss}"/></span>';
    var month = '<span {{noclear}} class="datepick"><input type="text" ng-readonly="true" class="form-control datepickerreadonly" datepicker-mode="mMode" datepicker-options="dateOptions" placeholder="{{placeHolder}}" ng-click="open($event)" ng-focus-async="!IE && open($event)" ng-change="IE && validate()" uib-datepicker-popup="{{format}}" ng-disabled="disabled" ng-model="dateModel" is-open="opened" close-text="Close" ng-class="{datepickerreadonly: datePickerReadOnlyCss}"/></span>';
    return {
        restrict: 'AE',
        replace: true,
        scope: {
            dateModel: '=',
            placeHolder: '@',
            minDate: '=',
            maxDate: '=',
            mode: '=',
            minMode: '@',
            disabled: "=",
            noclear: '@',
            datePickerReadOnly: "=",
            extraClass:'@'
        },
        controller: ['$scope', function ($scope) {
            $scope.datePickerReadOnlyCss = true;
            $scope.$watch('datePickerReadOnly', function(newValue){
                $scope.datePickerReadOnlyCss = newValue;
            });
            $scope.dateOptions = {};
            $scope.dateOptions.minDate = constructDate($scope.minDate);
            $scope.dateOptions.maxDate = constructDate($scope.maxDate);
            $scope.opened = false;
            $scope.IE = false;
            $scope.prevDate = $scope.dateModel;
            var ua = window.navigator.userAgent;
            if(ua.indexOf( "MSIE ") > 0 || ua.indexOf('Trident/') > 0 || ua.indexOf('Edge/') > 0 || navigator.appVersion.indexOf("MSIE 9.0") !== -1) {
                $scope.IE = true;
            }
            $scope.open = function ($event) {
                $event.preventDefault();
                $event.stopPropagation();
                $scope.opened = true;
                if ($scope.mode == 'month') {
                    $scope.mMode = 'month';
                    if ($scope.minMode != 'day') {
                        $scope.dateOptions.minMode= 'month';
                        $scope.format = "MMM yyyy"
                    }
                }
            };
            $scope.validate = function() {
                if(checkNotNullEmpty($scope.dateModel)) {
                    var selDate = $scope.dateModel.getTime();
                    if(checkNotNullEmpty($scope.minDate)) {
                        var mnDate = constructDate($scope.minDate).getTime();
                        if(selDate < mnDate) {
                            $scope.dateModel = $scope.prevDate;
                            alert("Date can not be less than " + $scope.minDate);
                        }
                    }
                    if(checkNotNullEmpty($scope.maxDate)) {
                        var mxDate = constructDate($scope.maxDate).getTime();
                        if(selDate > mxDate) {
                            $scope.dateModel = $scope.prevDate;
                            alert("Date can not be greater than " + $scope.maxDate);
                        }
                    }
                    $scope.prevDate = $scope.dateModel;
                }
            }
        }],
        link: function (scope, iElement, iAttrs, ctrl) {
            scope.format = "dd MMM yyyy";
            if (scope.mode == 'month') {
                scope.mMode = 'month';
                if (scope.minMode != 'day') {
                    scope.dateOptions.minMode='month';
                    scope.format = "MMM yyyy"
                }
                iElement.html(month).show();
            } else {
                iElement.html(day).show();
            }
            $compile(iElement.contents())(scope);
        }
    }
});
logistimoApp.directive('autoFocus', function ($timeout) {
    return {
        restrict: 'AC',
        link: function (_scope, _element) {
            if (_scope.isOpen !== 'false') {
                $timeout(function () {
                    _element[0].focus();
                }, 0);
            }
        }
    };
});
logistimoApp.directive('entitySelect', function () {
    return {
        restrict: 'AE',
        template: '<div><div style="width:100%"><div ng-hide="loading" class="has-feedback mgh0 mgb0">' +
        '<input type="text" ng-model="dEntModel" ng-hide="$parent.isDef(entId) && ((entType == \'vendors\' && !vendors.length) || (entType == \'customers\' && customers.length == 0)) && !isFilter"' +
        'autocomplete="off" ng-disabled = "disable == true" typeahead-template-url="entityTemplate" typeahead-on-select="setEntModel($item)"' +
        'placeholder="{{placeHolder}}" typeahead-wait-ms = "300" uib-typeahead="ent as ent.nm for ent in getFilteredEntity($viewValue)"' +
        ' class="{{classes}}" typeahead-editable="false" ng-blur="blurCallback()" ng-enter="checkModel()" typeahead-loading="loadingEntity"/>' +
        '<span ng-show="loadingEntity" class="form-control-feedback typehead-loading" aria-hidden="true"> <span class="glyphicons glyphicons-cogwheel spin"></span> </span>' +
        '<span ng-show="!loadingEntity && !defVLoading && $parent.isDef(entId) && entType == \'vendors\' && noVendors && vendors.length == 0">{{$parent.resourceBundle["relationship.novendors"]}}. <a ng-href="#/setup/entities/detail/{{entId}}/relationships/add">{{$parent.resourceBundle["relationship.specifyvendors"]}}</a></span>' +
        '<span ng-show="!loadingEntity && $parent.isDef(entId) && entType == \'customers\' && noCustomers">{{$parent.resourceBundle["relationship.nocustomers"]}}. <a ng-href="#setup/entities/detail/{{entId}}/relationships/add">{{$parent.resourceBundle["relationship.specifycustomers"]}}.</a></span>' +
        '</div>' +
        '<div ng-show="loading"><span class="glyphicons glyphicons-repeat spin"></span></div></div>' +
        '</div>',
        scope: {
            entModel: '=entModel',
            disable: '=',
            callback: '&onSelect',
            bCallback: '&onBlur',
            placeHolder: '@',
            entType: '@',
            isFilter: '@',
            entId: "=entId",
            addModelWatch: '@',
            classes: '@'
        },
        controller: ['$scope', 'entityService', 'domainCfgService', '$timeout','$filter', function ($scope, entityService, domainCfgService, $timeout,$filter) {
            $scope.entType = $scope.entType || "all";
            $scope.classes = $scope.classes || "form-control";
            $scope.vendors = [];
            $scope.customers = [];
            $scope.loading = false;
            $scope.$watch("entId", function (nVal, oVal) {
                if (typeof nVal != 'object' || !compareObject(nVal, oVal, 'id')) {
                    $scope.vendors = [];
                    $scope.customers = [];
                    $scope.loadCustVendors();
                }
            });

            $scope.loadCustVendors = function () {
                if (checkNotNullEmpty($scope.entId)) {
                    if ($scope.entType == 'vendors') {
                        $scope.loading = true;
                        $scope.noVendors = false;
                        $scope.defVLoading = false;
                        entityService.getVendors($scope.entId).then(function (data) {
                            $scope.vendors = data.data.results || [];
                            if (!$scope.isFilter) {
                                if ($scope.vendors.length == 0) {
                                    $scope.noVendors = true;
                                    $scope.defVLoading = true;
                                    domainCfgService.getOrdersCfg().then(function (data) {
                                        $scope.vendors = [];
                                        if (checkNotNullEmpty(data.data.vid) && data.data.vid != $scope.entId) {
                                            $scope.defVLoading = true;
                                            entityService.get(data.data.vid).then(function (data) {
                                                if (checkNotNullEmpty(data.data)) {
                                                    $scope.vendors.push(data.data);
                                                    $scope.entModel = data.data;
                                                }
                                            }).catch(function (err) {
                                                $scope.$parent.showErrorMsg(err);
                                            }).finally(function () {
                                                $scope.defVLoading = false;
                                            });
                                        } else {
                                            $scope.defVLoading = false;
                                        }
                                    }).catch(function error(msg) {
                                        $scope.showErrorMsg(msg);
                                        $scope.defVLoading = false;
                                    });
                                } else if ($scope.vendors.length == 1) {
                                    $scope.entModel = $scope.vendors[0];
                                }
                            }
                        }).catch(function error(msg) {
                            $scope.$parent.showErrorMsg(msg);
                        }).finally(function () {
                            $scope.loading = false;
                        });
                    } else if ($scope.entType == 'customers') {
                        $scope.noCustomers=false;
                        return entityService.getCustomers($scope.entId).then(function (data) {
                            $scope.customers = data.data.results || [];
                            if(!$scope.isFilter) {
                                if($scope.customers.length == 0) {
                                    $scope.noCustomers = true;
                                } else if($scope.customers.length == 1){
                                    $scope.entModel = $scope.customers[0];
                                }
                            }
                        }).catch(function error(msg) {
                            $scope.$parent.showErrorMsg(msg);
                        });
                    }

                }
            };
            $scope.loadCustVendors();
            $scope.$watch("entModel", function (newVal, oldVal) {
                if (!compareObject(newVal, $scope.dEntModel, "id")) {
                    $scope.dEntModel = angular.copy(newVal);
                    $scope.checkEntity();
                }
            });
            if ($scope.addModelWatch) {
                $scope.$watch("dEntModel", function (newVal, oldVal) {
                    $scope.setEntModel(newVal);
                });
            }
            $scope.setEntModel = function (newVal) {
                $scope.entModel = angular.copy(newVal);
                $scope.callback({nv:newVal});
            };
            $scope.checkModel = function () {
                $timeout(function () {
                    if (checkNullEmpty($scope.dEntModel)) {
                        $scope.entModel = null;
                    }
                }, 500);
            };
            $scope.blurCallback = function () {
                $scope.checkModel();
                $scope.bCallback();
            };
            $scope.checkEntity = function () {
                if (checkNotNullEmpty($scope.entModel) && checkNullEmpty($scope.entModel.nm)) {
                    entityService.get($scope.entModel.id).then(function (data) {
                        $scope.dEntModel = $scope.entModel = data.data;
                    }).catch(function (err) {
                        $scope.$parent.showErrorMsg(err);
                        $scope.dEntModel = null;
                    });
                }
            };
            $scope.checkEntity();
            $scope.getFilteredEntity = function (text) {
                $scope.loadingEntity = true;
                if ($scope.entType == 'vendors') {
                    return $filter('filter')($scope.vendors,{nm:text},startsWith)
                } else if ($scope.entType == 'customers') {
                    return $filter('filter')($scope.customers,{nm:text},startsWith)
                } else {
                    return entityService.getFilteredEntity(text.toLowerCase()).then(function (data) {
                        $scope.loadingEntity = false;
                        return data.data.results;
                    }).catch(function error(msg) {
                        $scope.$parent.showErrorMsg(msg);
                    });
                }
            };
        }]
    }
});
logistimoApp.directive('materialSelect', function ($q) {
    return {
        restrict: 'AE',
        template: '<div><div class="has-feedback"><input ng-disabled = "disable == true"  type="text" class="{{classes}}" typeahead-editable="false" ng-model="dMatModel" ' +
        'placeholder="{{placeHolder}}" uib-typeahead="mat as mat.mnm for mat in query($viewValue) | limitTo:8"' +
        'class="form-control" maxlength="50" typeahead-wait-ms = "500" typeahead-on-select="setModel($item)" ng-blur="blurCallback()" ng-enter="checkModel()" typeahead-loading="loadingMaterial"/>' +
        '<span ng-show="loadingMaterial" class="form-control-feedback typehead-loading" aria-hidden="true"> <span class="glyphicons glyphicons-cogwheel spin"></span> </span></div>' +
        '</div>',
        scope: {
            matModel: '=',
            disable: '=',
            callback: '&onSelect',
            bCallback: '&onBlur',
            placeHolder: '@',
            matId: "=",
            classes: "@"
        },
        controller: ['$scope', 'matService', '$timeout', function ($scope, matService, $timeout) {
            $scope.classes = $scope.classes || 'form-control';

            $scope.$watch("matModel", function (newVal, oldVal) {
                if (!compareObject(newVal, $scope.dMatModel, "mId")) {
                    $scope.dMatModel = angular.copy(newVal);
                    $scope.checkMaterial();
                }
            });

            $scope.blurCallback = function () {
                $scope.checkModel();
                $scope.bCallback();
            };

            $scope.setModel = function (newVal) {
                $scope.matModel = angular.copy(newVal);
                $scope.callback(newVal);
            };

            $scope.checkModel = function () {
                $timeout(function () {
                    if (checkNullEmpty($scope.dMatModel)) {
                        $scope.matModel = null;
                    }
                }, 500);
            };

            $scope.checkMaterial = function () {
                if (checkNotNullEmpty($scope.matModel) && checkNullEmpty($scope.matModel.mnm)) {
                    matService.get($scope.matModel.mId).then(function (data) {
                        $scope.dMatModel = $scope.matModel = data.data;
                    }).catch(function err(msg) {
                        $scope.$parent.showError(msg);
                        $scope.matModel = null; //reset on error;
                    });
                }
            };

            $scope.checkMaterial();

            $scope.query = function (term) {
                var deferred = $q.defer();
                matService.getDomainMaterials(term, null, 0, 10).then(function (data) {
                    deferred.resolve(data.data.results);
                }).catch(function error(msg) {
                    $scope.$parent.showErrorMsg(msg);
                    deferred.reject(msg);
                });
                return deferred.promise;
            }
        }]
    }
});
logistimoApp.directive('checklistModel', ['$parse', '$compile', function ($parse, $compile) {
    function contains(arr, item) {
        if (angular.isArray(arr)) {
            for (var i = 0; i < arr.length; i++) {
                if (angular.equals(arr[i], item)) {
                    return true;
                }
            }
        }
        return false;
    }

    function add(arr, item) {
        arr = angular.isArray(arr) ? arr : [];
        for (var i = 0; i < arr.length; i++) {
            if (angular.equals(arr[i], item)) {
                return arr;
            }
        }
        arr.push(item);
        return arr;
    }

    function remove(arr, item) {
        if (angular.isArray(arr)) {
            for (var i = 0; i < arr.length; i++) {
                if (angular.equals(arr[i], item)) {
                    arr.splice(i, 1);
                    break;
                }
            }
        }
        return arr;
    }

    function postLinkFn(scope, elem, attrs) {
        $compile(elem)(scope);
        var getter = $parse(attrs.checklistModel);
        var setter = getter.assign;
        var value = $parse(attrs.checklistValue)(scope.$parent);
        scope.$watch('checked', function (newValue, oldValue) {
            if (newValue === oldValue) {
                return;
            }
            var current = getter(scope.$parent);
            if (newValue === true) {
                setter(scope.$parent, add(current, value));
            } else {
                setter(scope.$parent, remove(current, value));
            }
        });
        scope.$parent.$watch(attrs.checklistModel, function (newArr, oldArr) {
            scope.checked = contains(newArr, value);
        }, true);
    }

    return {
        restrict: 'A',
        priority: 1000,
        terminal: true,
        scope: true,
        compile: function (tElement, tAttrs) {
            if (tElement[0].tagName !== 'INPUT' || !tElement.attr('type', 'checkbox')) {
                throw 'checklist-model should be applied to `input[type="checkbox"]`.';
            }
            if (!tAttrs.checklistValue) {
                throw 'You should provide `checklist-value`.';
            }
            tElement.removeAttr('checklist-model');
            tElement.attr('ng-model', 'checked');
            return postLinkFn;
        }
    };
}]);
logistimoApp.directive('tempChart', function () {
    return {
        restrict: 'E',
        link: function (scope, elem, attrs) {
            elem.addClass('tempChart');
            var d = JSON.parse(scope.data.cData);
            if (d.data.length > 0) {
                var flotData = getFlotData(d.data, scope.data.min, scope.data.max);
                $.plot(elem, flotData, {
                    xaxis: {mode: 'time', timezone: 'browser'}
                });
            }
            elem.show();
        }
    };
});
logistimoApp.directive('fileModel', ['$parse', function ($parse) {
    return {
        restrict: 'A',
        link: function (scope, element, attrs) {
            var model = $parse(attrs.fileModel);
            var modelSetter = model.assign;
            element.bind('change', function () {
                scope.$apply(function () {
                    modelSetter(scope, element[0].files[0]);
                });
            });
            scope.$watch(attrs.fileModel, function (newVal) {
                if (checkNullEmpty(newVal)) {
                    element[0].value = '';
                    if(element[0].files.length > 0) {
                        element[0].files = [];
                    }
                }
            });
        }
    };
}]);
logistimoApp.directive('fusionChart', function () {
    return {
        restrict: 'AE',
        template: '<div id="{{chartId}}"></div>',
        scope: {
            type: "=",
            height: "=",
            width: "=",
            chartLabel: '=',
            chartData: '=',
            chartOptions: "=",
            chartId: "@",
            simple: "@",
            trend: "=",
            colorRange: "=",
            markers: "="
        },
        controller: ['$scope','$rootScope', function ($scope,$rootScope) {
            if(typeof(FusionCharts) != "undefined") {
                FusionCharts.ready(function () {
                    function constructDataSource() {
                        var dataSource = {};
                        dataSource.chart = $scope.chartOptions;
                        if ($scope.simple) {
                            dataSource.data = $scope.chartData;
                        } else {
                            dataSource.categories = [{"category": $scope.chartLabel}];
                            dataSource.dataset = $scope.chartData;
                        }
                        if ($scope.trend) {
                            dataSource.trendlines = {};
                            dataSource.trendlines.line = $scope.trend;
                        }
                        if ($scope.colorRange) {
                            dataSource.colorrange = $scope.colorRange;
                        }
                        if ($scope.markers) {
                            dataSource.markers = $scope.markers;
                        }
                        dataSource.entityDef = $rootScope.entDef;
                        return dataSource;
                    }

                    var chartReference = FusionCharts.items["id-" + $scope.chartId];
                    if (checkNotNullEmpty(chartReference)) {
                        chartReference.dispose();
                    }
                    var revenueChart = new FusionCharts({
                        "type": $scope.type,
                        "renderAt": $scope.chartId,
                        "id": "id-" + $scope.chartId,
                        "width": $scope.width,
                        "height": $scope.height,
                        "dataFormat": "json",
                        "dataSource": constructDataSource()
                    });
                    revenueChart.render();
                });
            } else {
                var options = {
                    duration: 300
                };
                $scope.init = function() {
                    nv.render.active = false;
                    nv.addGraph(function () {
                        var chart,
                            isBarChart = $scope.type == 'stackedbar2d' || $scope.type == 'bar2d',
                            isPieChart = $scope.type == 'pie2d' || $scope.type == 'doughnut2d';
                        if (isPieChart) {
                            chart = nv.models.pieChart();
                            chart.donut(true);
                            chart.pie.labelsOutside(true);
                            chart.labelType("percent");
                            if($scope.type != 'doughnut2d') {
                                chart.legendPosition("right");
                            } else {
                                chart.showLegend(false);
                            }
                        } else if(isBarChart) {
                            chart = nv.models.multiBarHorizontalChart();
                            chart.showLegend(false);
                            chart.stacked(true);
                            chart.showControls(false);
                            chart.yDomain([0,100]);
                            chart.tooltip.contentGenerator(function (key, y, e, graph) {
                                return  "<p>" + key.data.toolText + "</p>";
                            });
                        } else {
                            chart = nv.models.lineChart();
                            chart.tooltip.contentGenerator(function (key, y, e, graph) {
                                return  "<p>" + key.point.toolText + "</p>";
                            });
                            if($scope.chartOptions.rotateLabels == "1") {
                                chart.xAxis.rotateLabels(-90);
                                chart.margin({"bottom": 100});
                            }
                        }
                        chart.options(options);
                        if(checkNotNullEmpty($scope.chartLabel)) {
                            var tickValues = [];
                            for (var i = 1; i < $scope.chartLabel.length; i++) {
                                tickValues.push(i);
                            }
                            chart.xAxis.tickValues(tickValues)
                                .tickFormat(function (d) {
                                    if (checkNotNullEmpty($scope.chartLabel[d])) {
                                        return $scope.chartLabel[d].label;
                                    }
                                    return d;
                                });
                            chart.yAxis.axisLabel($scope.chartOptions.yAxisName);
                            chart.margin({"right": 50});
                        }
                        var data;
                        if(isBarChart && $scope.simple) {
                            var barChartData = getBarChartData($scope.chartData);
                            data = barChartData.data;
                            chart.margin({"left": barChartData.labelLength * 7});
                        } else if(isPieChart) {
                            data = getChartData($scope.chartData, $scope.simple);
                            if(addPieTooltip(data)){
                                chart.tooltip.contentGenerator(function (key, y, e, graph) {
                                    return  "<p>" + key.data.toolText + "</p>";
                                });
                            }
                        } else {
                            data = getChartData($scope.chartData, $scope.simple);
                            if(checkNotNullEmpty(data[0]) && checkNullEmpty(data[0].key)){
                                chart.showLegend(false);
                            }
                            addLineTooltip(data, $scope.chartLabel);
                        }
                        var svg = d3.select("[id='" + $scope.chartId + "']").select("svg");
                        if(checkNotNullEmpty(svg)) {
                            svg.selectAll("*").remove();
                        }
                        d3.select("[id='" + $scope.chartId + "']").append('svg').style('height',$scope.height).style('width',$scope.width)
                            .datum(data)
                            .call(chart);
                        d3.selectAll(".nv-x .tick line").remove();
                        nv.utils.windowResize(chart.update);
                        return chart;
                    });
                };
                $scope.init();

                function roundNumber(value, digits, forceRound) {
                    if(checkNotNullEmpty(value)) {
                        digits = digits || 0;
                        if(value  * 1 < 1 && !forceRound && digits == 0) {
                            digits = 2;
                        }
                        value = parseFloat(value).toFixed(digits);
                        var dec = checkNotNullEmpty(value) ? value.indexOf(".") : -1;
                        if (parseFloat(value.substr(dec + 1)) == 0) {
                            value = value.substr(0, dec);
                        }
                    }
                    return value * 1 || 0;
                }

                function getChartData(data, isSimple) {
                    var dataValues = [];
                    if(checkNotNullEmpty(data)) {
                        if(isSimple) {
                            data.forEach(function (v) {
                                var vo = {};
                                vo.x = v.label;
                                if(v.color) {
                                    vo.color  = v.color;
                                }
                                vo.toolText = v.toolText;
                                vo.y = roundNumber(v.value, 2);
                                dataValues.push(vo);
                            });
                        } else {
                            data.forEach(function (d) {
                                var o = {};
                                if (checkNotNullEmpty(d.seriesName)) {
                                    o.key = d.seriesName;
                                }
                                var values = [];
                                var i = 0;
                                d.data.forEach(function (v) {
                                    var vo = {};
                                    vo.x = i++;
                                    vo.y = roundNumber(v.value, 2);
                                    if(v.color) {
                                        vo.color  = v.color;
                                    }
                                    vo.toolText = v.toolText;
                                    values.push(vo);
                                });
                                o.values = values;
                                dataValues.push(o);
                            });
                        }
                    }
                    return dataValues;
                }

                function getBarChartData(data) {
                    var mdata = {};
                    var dataValues = [];
                    var maxLength = 0;
                    if(checkNotNullEmpty(data)) {
                        var o = {};
                        var values = [];
                        data.forEach(function (v) {
                            var vo = {};
                            vo.x = v.label;
                            vo.toolText = v.toolText;
                            if(vo.x.length > maxLength) {
                                maxLength = vo.x.length;
                            }
                            vo.y = roundNumber(v.value, 2);
                            if(v.color) {
                                vo.color  = v.color;
                            }
                            values.push(vo);
                        });
                        o.values = values;
                        dataValues.push(o);
                    }
                    mdata.data = dataValues;
                    mdata.labelLength = maxLength;
                    return mdata;
                }

                function addPieTooltip(data) {
                    var tooltipAdded = false;
                    var total = 0;
                    data.forEach(function(d) {
                        total += d.y * 1;
                    });
                    data.forEach(function(d) {
                        if(checkNotNullEmpty(d.toolText)) {
                            d.toolText = d.toolText.replace("$label", d.x);
                            d.toolText = d.toolText.replace("$value", d.y);
                            d.toolText = d.toolText.replace("$unformattedSum", total);
                            tooltipAdded = true;
                        }
                    });
                    return tooltipAdded;
                }

                function addLineTooltip(data, labels) {
                    if(checkNotNullEmpty(labels)) {
                        data.forEach(function (dd) {
                            dd.values.forEach(function(d) {
                                if(checkNotNullEmpty(d.toolText)) {
                                    d.toolText = d.toolText.replace("$label", labels[d.x].label);
                                    d.toolText = d.toolText.replace("$seriesName", dd.key);
                                } else {
                                    d.toolText = labels[d.x].label + ": " + d.y;
                                }
                            });
                        });
                    }
                }
            }
        }]
    };
});
logistimoApp.directive('chartTable', function () {
    return {
        restrict: 'AE',
        template: '<h4 class="text-center">{{caption}}</h4>' +
        '<table class="table table-striped table-centered table-bordered table-condensed table-hover table-logistimo">' +
        '<tr><th ng-repeat="head in heading">{{head}}</th></tr>' +
        '<tr ng-repeat="data in row">' +
        '<td>{{data[0]}}</td>' +
        '<td ng-repeat="value in data.slice(1) track by $index">{{value | number}}</td></tr>' +
        '</table>',
        scope: {
            caption: "=",
            heading: "=",
            row: "="
        }
    };
});
logistimoApp.directive('reportFilter', function ($compile) {
    var state = '<div class="form-group has-feedback"><input type="text" ng-model="filterModel" ' +
        'autocomplete="off"' +
        'placeholder="{{placeHolder}}" typeahead-wait-ms = "300" uib-typeahead="state for state in states | filter:$viewValue | limitTo:8"' +
        'class="form-control" typeahead-editable="true" typeahead-loading="loadingState"></input>' +
        '<span ng-show="loadingState" class="form-control-feedback typehead-loading" aria-hidden="true"> <span class="glyphicons glyphicons-cogwheel spin"></span> </span> </div>';
    var district = '<div class="form-group has-feedback"><input type="text" ng-model="filterModel" ' +
        'autocomplete="off"' +
        'placeholder="{{placeHolder}}" typeahead-wait-ms = "300" uib-typeahead="district for district in districts | filter:$viewValue | limitTo:8"' +
        'class="form-control" typeahead-editable="true" typeahead-loading="loadingDistrict"></input>' +
        '<span ng-show="loadingDistrict" class="form-control-feedback typehead-loading" aria-hidden="true"> <span class="glyphicons glyphicons-cogwheel spin"></span> </span> </div>';
    ;

    return {
        restrict: 'AE',
        scope: {
            filterModel: '=',
            placeHolder: '@',
            filterType: '@'
        },
        controller: ['$scope', '$location', 'domainCfgService', function ($scope, $location, domainCfgService) {
            domainCfgService.getReportFilters().then(function (data) {
                $scope.states = data.data.stt;
                $scope.districts = data.data.dstr;
            });
        }],
        link: function (scope, iElement, iAttrs, ctrl) {
            if (scope.filterType == 'state') {
                iElement.html(state).show();
            } else if (scope.filterType == 'district') {
                iElement.html(district).show();
            }
            $compile(iElement.contents())(scope);
        }
    }
});
logistimoApp.directive('selectEntGroup', function () {
    return {
        restrict: 'AE',
        template: '<div class="form-group has-feedback"><input type="text" ng-model="entGrpModel" ' +
        'autocomplete="off"' +
        'placeholder="{{placeHolder}}" typeahead-wait-ms = "300" uib-typeahead="eg as eg.nm for eg in entityGroups"' +
        'class="form-control" typeahead-editable="true" typeahead-loading="loadingEntGroup"></input>' +
        '<span ng-show="loadingEntGroup" class="form-control-feedback typehead-loading" aria-hidden="true"> <span class="glyphicons glyphicons-cogwheel spin"></span> </span> </div>',
        scope: {
            entGrpModel: '=',
            placeHolder: '@'
        },
        controller: ['$scope', '$location', 'entGrpService', function ($scope, $location, entGrpService) {
            entGrpService.getEntGrps(0, 9999).then(function (data) {
                $scope.entityGroups = data.data.results;
            });
        }]
    }
});

logistimoApp.directive('popoverTemplate', ['$tooltip', function ($tooltip) {
    return $tooltip('popoverTemplate', 'popover', 'click');
}]);

logistimoApp.directive('noteData', function () {
    var noteTemplate ='<div class="box topbox">' +
        '<div>' +
            '<div class="modal-header">'+
                '<h3 class="modal-title">Note</h3>'+
            '</div>'+
            '<div class="modal-body">'+
                '<p>{{msg}}</p>'+
            '</div>'+
            '<div class="modal-footer">'+
                '<button class="btn btn-primary" ng-click="confirm()">OK</button>'+
                '<button class="btn btn-default" ng-click="close()">Cancel</button>'
            '</div>'+
        '</div>'+
        '</div>';
    return {
        restrict: 'AE',
        controller:['$scope','$uibModal','demandService', function ($scope, $uibModal, demandService) {
            $scope.open = function (data) {
                if(data == "crc") {
                    if($scope.inv.crc == -1 && $scope.inv.mmType == '1') {
                        $scope.msg = $scope.resourceBundle['config.set.consumption.rate'];
                        $scope.modalInstance = $uibModal.open({
                            template: noteTemplate,
                            scope: $scope
                        });
                    } else if ($scope.inv.co != -1) {
                        $scope.msg = $scope.resourceBundle['config.enablecr'];
                        $scope.modalInstance = $uibModal.open({
                            template: noteTemplate,
                            scope: $scope
                        });
                    }
                } else if(data == 'mmw' || data == 'mmm') {
                    $scope.msg = $scope.resourceBundle['config.high.consumption.rate'] + " "+ (data == 'mmw' ? "Weekly" : "Monthly") +". " + $scope.resourceBundle['config.less.consumption.rate'];
                    $scope.modalInstance = $uibModal.open({
                        template: noteTemplate,
                        scope: $scope
                    });
                } else if(data == 'falloc' || data == 'alloc')  {
                    $scope.alloc = true;
                    $scope.type = data;
                    $scope.msg = "Are you sure you want to clear allocations?";
                    $scope.modalInstance = $uibModal.open({
                       template: noteTemplate,
                        scope: $scope
                    });
                }
                else {
                    if($scope.inv.crc != 1) {
                        var m = data == '200'? $scope.forecast[2].key:$scope.forecast[1].key;
                        $scope.msg = m + " " + $scope.resourceBundle['config.enableforecasting'] + " " + m + ".";
                        $scope.modalInstance = $uibModal.open({
                            template: noteTemplate,
                            scope: $scope
                        });
                    }
                }
            };
            $scope.close = function() {
                $scope.modalInstance.dismiss('close');
            };
            $scope.confirm = function() {
                if($scope.alloc) {
                    $scope.clearAllocations($scope.type);
                }
                $scope.modalInstance.dismiss('confirm');
            }
        }]
    }
});

logistimoApp.directive('exportData', function () {
    var batchTemplate = '<div class="modal-header ws">' +
        '<h3 class="modal-title">Export data</h3>' +
        '</div>' +
        '<div class="modal-body ws">' +
        '<p>{{msg}}</p>' +
        '<span ng-if="batchEnabled && ebf == null && abnType == \'\' "><input type="checkbox" ng-model="ibd" ng-change="changeMessage()"> Include batch details</span>' +
        '</div>' +
        '<div class="modal-footer ws">' +
        '<button class="btn btn-primary" ng-click="exportData()">OK</button>' +
        '<button class="btn btn-default" ng-click="cancel()">Cancel</button>' +
        '</div>';
    return {
        restrict: 'AE',
        template: '<button type="button" class="btn btn-sm btn-primary" ng-click="open()">Export</button>',
        scope: {
            mailId: '=',
            entityId: '=',
            entityName: '=',
            matId: '=',
            orderId: '=',
            materialName: '=',
            ebf: '=',
            abnType: '=',
            abnDur: '=',
            location: '=',
            batchNumber: '=',
            exportType: '@',
            batchEnabled: '@',
            reportType: '@',
            text: '=',
            from: '=',
            to: '=',
            frequency: '=',
            exportOrderType: '=',
            exportCallback: '&',
            transactionType: '=',
            linkedId: '=',
            trnsBatch : '=',
            atd : '=',
            nName: '=',
            mobilePhoneNumber: '=',
            role: '=',
            isEnabled: '=',
            v: '=',
            neverLogged: '=',
            utag: '=',
            deviceId : '=',
            assetVendor: '=',
            sensor: '=',
            sensorName: '=',
            sensorId: '=',
            assetType: '=',
            reason: '=',
            discType: '=',
            eTag: '=',
            mTag: '=',
            etrn: '=',
            rows: '=',
            pdos:'='

        },
        controller: ['$scope', '$uibModal', 'exportService', function ($scope, $uibModal, exportService) {
            $scope.modalInstance = "";
            $scope.ibd = false;
            $scope.constructMsg = function () {
                var emailDetailsMsg = '. ' + $scope.$parent.resourceBundle['data.emailed'] + ' ' + $scope.mailId + '. ' + $scope.$parent.resourceBundle['continue'] + '?';
                var frequency = "monthly";
                if ($scope.frequency != undefined && checkNotNullEmpty($scope.frequency)) {
                    if ($scope.frequency == 'd') {
                        frequency = "daily";
                    }
                }
                if($scope.exportType == 'assets') {
                        $scope.msg = $scope.$parent.resourceBundle['assets.export.all'] + emailDetailsMsg;
                } else if ($scope.exportType == 'powerdata') {
                    $scope.msg = $scope.$parent.resourceBundle['export.powerdata'] + " \"" + FormatDate_DD_MM_YYYY($scope.to?$scope.to:new Date()) + "\" of this asset's sensor \"" + $scope.sensorName + "\"" + emailDetailsMsg;
                } else if ($scope.exportType == 'orders') {
                    if (checkNotNullEmpty($scope.orderId)) {
                        $scope.msg = $scope.orderId.length + ' ' + $scope.$parent.resourceBundle['orders.export.spreadsheet'];
                    } else {
                        $scope.msg = $scope.$parent.resourceBundle['export_confirmall1'] + '\n' + $scope.$parent.resourceBundle['export_confirmall2'] + ' ' + $scope.mailId + ' ' + $scope.$parent.resourceBundle['export_confirmall3'];
                    }
                } else if ($scope.exportType == 'users') {
                    var hasFilters = checkNotNullEmpty($scope.role) || checkNotNullEmpty($scope.nName) ||
                        checkNotNullEmpty($scope.mobilePhoneNumber)  || checkNotNullEmpty($scope.from) ||
                        checkNotNullEmpty($scope.to) || checkNotNullEmpty($scope.v) ||
                        checkNotNullEmpty($scope.isEnabled) || checkNotNullEmpty($scope.neverLogged) ||
                        checkNotNullEmpty($scope.utag);
                    if (hasFilters) {
                        $scope.msg =  $scope.$parent.resourceBundle['export.users.criteria'] + emailDetailsMsg;
                    } else {
                        $scope.msg = $scope.$parent.resourceBundle['export.all.users'] + emailDetailsMsg;
                    }
                } else if ($scope.exportType == 'transactions') {
                    var filtersAvailable = checkNotNullEmpty($scope.from) || checkNotNullEmpty($scope.to) ||
                        checkNotNullEmpty($scope.transactionType) || checkNotNullEmpty($scope.reason) ||
                        checkNotNullEmpty($scope.trnsBatch) || checkNotNullEmpty($scope.entityId) ||
                        checkNotNullEmpty($scope.matId) || checkNotNullEmpty($scope.linkedId) ||
                        checkNotNullEmpty($scope.mTag) || checkNotNullEmpty($scope.eTag) ||
                        checkNotNullEmpty($scope.reason);
                    if (filtersAvailable) {
                            $scope.msg = $scope.$parent.resourceBundle['export.transactions.criteria'] + emailDetailsMsg;
                    } else {
                        $scope.msg = $scope.$parent.resourceBundle['export.all.transactions'] + emailDetailsMsg;
                    }
                } else if ($scope.exportType == 'discrepancies') {
                    var filtersAvailable = checkNotNullEmpty($scope.discType) || checkNotNullEmpty($scope.entityId) ||
                        checkNotNullEmpty($scope.matId) || checkNotNullEmpty($scope.eTag) || checkNotNullEmpty($scope.mTag)
                        || checkNotNullEmpty($scope.orderId) || checkNotNullEmpty($scope.from) || checkNotNullEmpty($scope.to)
                        || (checkNullEmpty($scope.etrn) && $scope.etrn==true);
                    if (filtersAvailable) {
                        $scope.msg = $scope.$parent.resourceBundle['export.discrepancies.criteria'] + emailDetailsMsg;
                    } else {
                        $scope.msg = $scope.$parent.resourceBundle['export.all.discrepancies'] + emailDetailsMsg;
                    }
                } else if (checkNullEmpty($scope.entityId) && checkNullEmpty($scope.matId) && checkNullEmpty($scope.ebf) && checkNullEmpty($scope.orderId)) {
                    if (checkNotNullEmpty($scope.from) || checkNotNullEmpty($scope.to)) {
                        if ($scope.exportType == 'notificationStatus' || $scope.exportType == 'manualtransactions') {
                            $scope.msg = $scope.$parent.resourceBundle['export.all.notification.criteria'] + emailDetailsMsg;
                        }
                    } else {
                        if (!$scope.ibd && $scope.exportType == 'inventory') {
                            if(checkNotNullEmpty($scope.abnType) || checkNotNullEmpty($scope.eTag) || checkNotNullEmpty($scope.mTag)){
                                $scope.msg = $scope.$parent.resourceBundle['export.inventory.criteria'] + emailDetailsMsg;
                            }else{
                                $scope.msg = $scope.$parent.resourceBundle['export.all.inventory'] + emailDetailsMsg;
                            }
                        } else if ($scope.exportType == 'users') {
                            $scope.msg = $scope.$parent.resourceBundle['export.all.users'] + emailDetailsMsg;
                        } else if ($scope.exportType == 'entities') {
                            $scope.msg = $scope.$parent.resourceBundle['export.all.entities'] + emailDetailsMsg + '\n' + $scope.$parent.resourceBundle['export.direct.entity'];
                        } else if ($scope.exportType == 'materials') {
                            $scope.msg = $scope.$parent.resourceBundle['export.all.materials'] + emailDetailsMsg;
                        } else if ($scope.exportType == 'abnormalStock') {
                            $scope.msg = $scope.$parent.resourceBundle['export.abnormal.stock'] + emailDetailsMsg;
                        } else if ($scope.exportType == 'reports') {
                            $scope.msg = $scope.$parent.resourceBundle['export.choose'] + ' ' + frequency + ' ' + $scope.reportDisplayMsg($scope.reportType) + emailDetailsMsg;
                        } else if ($scope.exportType == 'events') {
                            $scope.msg = $scope.$parent.resourceBundle['export.all.events'] + emailDetailsMsg;
                        } else if ($scope.exportType == 'manualtransactions') {
                            $scope.msg = $scope.$parent.resourceBundle['export.uploaded.transactions'] + emailDetailsMsg;
                        } else if ($scope.exportType == 'notificationStatus') {
                            $scope.msg = $scope.$parent.resourceBundle['export.all.notification'] + emailDetailsMsg;
                        } else {
                            $scope.msg = $scope.$parent.resourceBundle['export.all.inventory.batch'] + emailDetailsMsg;
                        }
                    }
                } else if (checkNotNullEmpty($scope.entityId)) {
                    if (!$scope.ibd && $scope.exportType == 'inventory') {
                        if(checkNotNullEmpty($scope.abnType) || checkNotNullEmpty($scope.mTag)) {
                            $scope.msg = $scope.$parent.resourceBundle['export.inventory.criteria'] + emailDetailsMsg;
                        }else{
                            $scope.msg = $scope.$parent.resourceBundle['export.inventory.entity'] + ' ' + $scope.entityName + emailDetailsMsg;
                        }
                    } else if ($scope.exportType == 'manualtransactions') {
                        $scope.msg = $scope.$parent.resourceBundle['export.all.transactions.entity'] + emailDetailsMsg;
                    } else {
                        $scope.msg = $scope.$parent.resourceBundle['export.inventory.batch'] + ' ' + $scope.entityName + emailDetailsMsg;
                    }
                } else if (checkNotNullEmpty($scope.matId)) {
                    if (checkNotNullEmpty($scope.batchNumber)) {
                        $scope.msg = $scope.$parent.resourceBundle['export.batch.report.material'] + ' ' + $scope.materialName + ' ' + $scope.$parent.resourceBundle['with.batch'] + ' ' + $scope.batchNumber + '.' + emailDetailsMsg;
                    } else if (checkNotNullEmpty($scope.ebf)) {
                        $scope.msg = $scope.$parent.resourceBundle['export.batch.report.material'] + ' ' + $scope.materialName + ' ' + $scope.$parent.resourceBundle['batch.expire'] + '.' + emailDetailsMsg;
                    } else {
                        if (!$scope.ibd) {
                            if(checkNotNullEmpty($scope.abnType) || checkNotNullEmpty($scope.eTag)){
                                $scope.msg = $scope.$parent.resourceBundle['export.inventory.criteria'] + emailDetailsMsg;
                            }else{
                                $scope.msg = $scope.$parent.resourceBundle['export.inventory.material'] + ' ' + $scope.materialName + emailDetailsMsg;
                            }
                        } else {
                            $scope.msg = $scope.$parent.resourceBundle['export.inventory.material.batch'] + ' ' + $scope.materialName + emailDetailsMsg;
                        }
                    }
                } else if (checkNotNullEmpty($scope.ebf)) {
                    $scope.msg = $scope.$parent.resourceBundle['export.batch.expiry.report'] + ' ' + emailDetailsMsg;
                }
            };

            $scope.reportDisplayMsg = function (msg) {
                if (msg == 'consumptiontrends') {
                    return "Inventory trends";
                } else if (msg == 'orderresponsetimes') {
                    return "Order response times"
                } else if (msg == 'replenishmentresponsetimes') {
                    return 'Replenishment response times'
                } else if (msg == 'transactioncounts') {
                    return 'Transaction counts'
                } else if (msg == 'useractivity') {
                    return 'User activity';
                }
            };

            $scope.open = function () {
                if (!$scope.validate()) {
                    return;
                }
                $scope.constructMsg();
                $scope.modalInstance = $uibModal.open({
                    template: batchTemplate,
                    scope: $scope
                });
            };
            $scope.cancel = function () {
                $scope.modalInstance.dismiss('cancel');
            };
            $scope.changeMessage = function () {
                $scope.ibd = !$scope.ibd;
                $scope.constructMsg();
            };
            $scope.validate = function () {
                if (!$scope.$parent.iAdm && ($scope.exportType == 'orders' || $scope.exportType == 'inventory' || $scope.exportType == 'transactions') && checkNullEmpty($scope.entityId)) {
                    $scope.$parent.showWarning($scope.$parent.resourceBundle['order.export.entityreq']);
                    return false;
                }
                if($scope.rows > 500000){
                    $scope.$parent.showWarning($scope.$parent.resourceBundle['export.error.toomany']);
                    return false;
                }
                return true;
            };
            $scope.exportData = function () {
                $scope.cancel();
                var type = $scope.exportType;
                var extraParams = '';
                // Special cases where type is set to something different from $scope.exporType (because backend expects it that way)
                if ($scope.exportType == 'entities') {
                    type = 'kiosks';
                } else if($scope.exportType == 'abnormalStock') {
                    type = 'stev';
                    extraParams = '&latest=true&reports&abnormalreport';
                    if (checkNotNullEmpty($scope.abnType)) {
                        extraParams += '&eventtype=' + $scope.abnType;
                    }
                } else if ($scope.exportType == 'inventory' && checkNotNullEmpty($scope.abnType)) {
                    type = 'stev';
                    extraParams = '&eventtype=' + $scope.abnType;
                    extraParams += '&reports&abnormalreport&abnstockview';
                    if (checkNotNullEmpty($scope.abnDur)) {
                        extraParams += "&dur=" + $scope.abnDur;
                    } else {
                        extraParams += "&latest=true";
                    }
                }
                if($scope.exportType == "powerdata") {
                    if(checkNotNullEmpty($scope.assetVendor)) {
                        extraParams += '&assetvendor=' + $scope.assetVendor;
                    }
                    if(checkNotNullEmpty($scope.assetType)) {
                        extraParams += '&assetype=' + $scope.assetType;
                    }
                    if(checkNotNullEmpty($scope.sensor)) {
                        extraParams += '&sensor=' + $scope.sensor;
                    }
                    if(checkNotNullEmpty($scope.deviceId)) {
                        extraParams += '&deviceid=' + encodeURI($scope.deviceId);
                    }
                    if(checkNotNullEmpty($scope.to)) {
                        extraParams += '&to=' + $scope.to;
                    }
                    if(checkNotNullEmpty($scope.sensorId)) {
                        extraParams += '&snsname=' + $scope.sensorId;
                    }
                }

                if ($scope.ibd) {
                    type = "inventorybatch";
                }
                if (checkNotNullEmpty($scope.entityId)) {
                    extraParams += '&kioskid=' + $scope.entityId;
                }
                if (checkNotNullEmpty($scope.location)) {
                    var location = {};
                    if($scope.location.state != undefined) {
                        location['state'] = $scope.location.state.label;
                    }
                    if($scope.location.district != undefined) {
                        location['district'] = $scope.location.district.label;
                    }
                    if($scope.location.taluk != undefined) {
                        location['taluk'] = $scope.location.taluk.label;
                    }
                    extraParams += "&loc=" + JSON.stringify(location);
                }
                if (checkNotNullEmpty($scope.matId)) {
                    extraParams += '&materialid=' + $scope.matId;
                    if(checkNotNullEmpty($scope.trnsBatch)){
                        extraParams += '&batchid='+ $scope.trnsBatch;
                    }
                    if (checkNotNullEmpty($scope.batchNumber)) {
                        type = "batchexpiry";
                        extraParams += '&batchid=' + $scope.batchNumber;
                    } else if (checkNotNullEmpty($scope.ebf)) {
                        type = "batchexpiry";
                        extraParams += '&expiresbefore=' + formatDate($scope.ebf) + ' 00:00:00';
                    }
                } else if (checkNotNullEmpty($scope.ebf)) {
                    type = "batchexpiry";
                    extraParams += '&expiresbefore=' + formatDate($scope.ebf) + ' 00:00:00';
                } else if ($scope.exportType == 'events') {
                    extraParams += '&view=configuration';
                }
                if(checkNotNullEmpty($scope.atd)){
                    extraParams += '&atd='+$scope.atd;
                }

                if (checkNotNullEmpty($scope.orderId)) {
                    if (type == 'discrepancies') {
                        extraParams += '&orderid=' + $scope.orderId;
                    } else {
                        extraParams += '&values=' + $scope.orderId;
                    }
                }
                if (checkNotNullEmpty($scope.exportOrderType)) {
                    if (type == 'discrepancies') {
                        extraParams += '&otype=' + $scope.exportOrderType;
                    } else {
                        extraParams += '&exportOrderType=' + $scope.exportOrderType;
                    }
                }
                if (checkNotNullEmpty($scope.transactionType)) {
                    extraParams += '&transactiontype=' + $scope.transactionType;
                }
                if (checkNotNullEmpty($scope.linkedId)) {
                    extraParams += '&lkIdParam=' + $scope.linkedId;
                }
                if (checkNotNullEmpty($scope.nName)) {
                    extraParams += '&nname=' + $scope.nName;
                }
                if (checkNotNullEmpty($scope.role)) {
                    extraParams += '&role=' + $scope.role;
                }
                if (checkNotNullEmpty($scope.mobilePhoneNumber)) {
                    extraParams += '&mobilephonenumber=' + encodeURIComponent($scope.mobilePhoneNumber);
                }
                if (checkNotNullEmpty($scope.from)) {
                    extraParams += '&from=' + formatDate($scope.from) + ' 00:00:00';
                }
                if (checkNotNullEmpty($scope.to)) {
                    extraParams += '&to=' + formatDate($scope.to) + ' 00:00:00';
                }
                if (checkNotNullEmpty($scope.isEnabled)) {
                    extraParams += '&isenabled=' + $scope.isEnabled;
                }
                if (checkNotNullEmpty($scope.v)) {
                    extraParams += '&v=' + $scope.v;
                }
                if (checkNotNullEmpty($scope.neverLogged)) {
                    extraParams += '&neverlogged=' + $scope.neverLogged;
                }
                if (checkNotNullEmpty($scope.utag)) {
                    extraParams += '&utag=' + $scope.utag;
                }
                if (checkNotNullEmpty($scope.reason)) {
                    extraParams += '&rsn=' + $scope.reason;
                }
                if (checkNotNullEmpty($scope.eTag)) {
                    extraParams += '&etag=' + $scope.eTag;
                }
                if (checkNotNullEmpty($scope.mTag)) {
                    extraParams += '&mtag=' + $scope.mTag;
                }
                if (checkNotNullEmpty($scope.discType)) {
                    extraParams += '&disctype=' + $scope.discType;
                }
                if (checkNotNullEmpty($scope.etrn)) {
                    extraParams += '&etrn';
                }
                if (checkNotNullEmpty($scope.pdos)) {
                    extraParams += '&pdos=' + $scope.pdos;
                }
                if (type == 'orders' || type =='reports') {
                    $scope.exportCallback({type: type, extraParams: extraParams});
                } else {
                    exportService.scheduleBatchExport(type, extraParams).then(function (data) {
                        $scope.$parent.showSuccess($scope.$parent.resourceBundle['export.success1'] + ' ' + $scope.mailId + ' ' + $scope.$parent.resourceBundle['export.success2'] + ' ' + $scope.$parent.resourceBundle['exportstatusinfo2'] + ' ' + data.data + '. ' + $scope.$parent.resourceBundle['exportstatusinfo1']);
                    }).catch(function error(msg) {
                        $scope.$parent.showErrorMsg(msg);
                    });
                }
                $scope.ibd = false;
            };
        }]
    }
});

logistimoApp.directive('ngFocusLogi', [function () {
    var FOCUS_CLASS = "ng-focused";
    return {
        restrict: 'A',
        require: 'ngModel',
        link: function (scope, element, attrs, ctrl) {
            ctrl.$focused = false;
            element.bind('focus', function (evt) {
                element.addClass(FOCUS_CLASS);
                scope.$apply(function () {
                    ctrl.$focused = true;
                });
            }).bind('blur', function (evt) {
                element.removeClass(FOCUS_CLASS);
                scope.$apply(function () {
                    ctrl.$focused = false;
                });
            });
        }
    }
}]);
logistimoApp.directive('multiSelect', function () {
    return {
        restrict: 'AE',
        replace: true,
        template: '<div>' +
        '<lg-uib-select multiple="multiple" ng-disabled="disabled" query="query(q)" ui-model="ngModel" place-holder="{{placeHolder?placeHolder:\'Type text and press enter\'}}"> </lg-uib-select>' +
        '</div>',
        scope: {
            ngModel: '=',
            name: '=',
            placeHolder: '=',
            type: '='
        },
        controller: ['$scope', function ($scope) {
            $scope.query = function (query) {
                var rdata = {results: []};
                query.term = query.term.replace(/,/g,"");
                if(checkNotNullEmpty(query.term) && !($scope.type == 'email') ||  checkEmail(query.term)) {
                    rdata.results.push({'text': query.term, 'id': query.term});
                }
                query.callback(rdata);
            }
        }]
    }
});

logistimoApp.filter('timeago', function () {
    return function (input, p_allowFuture, p_agoreq) {
        var substitute = function (stringOrFunction, number, strings) {
                var string = $.isFunction(stringOrFunction) ? stringOrFunction(number, dateDifference) : stringOrFunction;
                var value = (strings.numbers && strings.numbers[number]) || number;
                return string.replace(/%d/i, value);
            },
            nowTime = (new Date()).getTime(),
            date = (new Date(input)).getTime(),
            allowFuture = p_allowFuture || false,
            isSuffixReq = p_agoreq || false,
            strings = {
                prefixAgo: null,
                prefixFromNow: null,
                suffixAgo: "ago",
                suffixFromNow: "ahead",
                seconds: "1 minute",
                minute: "1 minute",
                minutes: "%d minutes",
                hour: "1 hour",
                hours: "%d hours",
                day: "1 day",
                days: "%d days",
                month: "1 month",
                months: "%d months",
                year: "1 year",
                years: "%d years"
            },
            dateDifference = nowTime - date,
            words,
            seconds = Math.abs(dateDifference) / 1000,
            minutes = seconds / 60,
            hours = minutes / 60,
            days = hours / 24,
            years = days / 365,
            separator = strings.wordSeparator === undefined ? " " : strings.wordSeparator;

        // var strings = this.settings.strings;
        if (isSuffixReq) {
            prefix = strings.prefixAgo;
            suffix = strings.suffixAgo;

            if (allowFuture) {
                if (dateDifference < 0) {
                    prefix = strings.prefixFromNow;
                    suffix = strings.suffixFromNow;
                }
            }
        } else {
            prefix = "";
            suffix = "";
        }

        words = seconds < 45 && substitute(strings.seconds, Math.round(seconds), strings) ||
            seconds < 90 && substitute(strings.minute, 1, strings) ||
            minutes < 45 && substitute(strings.minutes, Math.round(minutes), strings) ||
            minutes < 90 && substitute(strings.hour, 1, strings) ||
            hours < 24 && substitute(strings.hours, Math.round(hours), strings) ||
            hours < 42 && substitute(strings.day, 1, strings) ||
            days < 30 && substitute(strings.days, Math.round(days), strings) ||
            days < 45 && substitute(strings.month, 1, strings) ||
            days < 365 && substitute(strings.months, Math.round(days / 30), strings) ||
            years < 1.5 && substitute(strings.year, 1, strings) ||
            substitute(strings.years, Math.round(years), strings);

        return $.trim([prefix, words, suffix].join(separator));
    }
});

logistimoApp.filter('timeStr', function () {
    return function (input, p_allowFuture, p_agoreq) {
        var allowFuture = p_allowFuture || false,
            isSuffixReq = p_agoreq || false,
            strings = {
                prefixAgo: null,
                prefixFromNow: null,
                suffixAgo: "ago",
                suffixFromNow: "ahead",
                seconds: "1 minute",
                minute: "1 minute",
                minutes: "%d minutes",
                hour: "1 hour",
                hours: "%d hours",
                day: "1 day",
                days: "%d days",
                month: "1 month",
                months: "%d months",
                year: "1 year",
                years: "%d years"
            },
            dateDifference = input,
            words,
            seconds = Math.abs(dateDifference) / 1000,
            minutes = seconds / 60,
            hours = minutes / 60,
            days = hours / 24,
            years = days / 365,
            prefix,
            suffix;
        if (isSuffixReq) {
            prefix = strings.prefixAgo;
            suffix = strings.suffixAgo;
            if (allowFuture) {
                if (dateDifference < 0) {
                    prefix = strings.prefixFromNow;
                    suffix = strings.suffixFromNow;
                }
            }
        } else {
            prefix = "";
            suffix = "";
        }

        words = seconds < 45 && strings.seconds.replace(/%d/i, Math.round(seconds))||
            seconds < 90 && strings.minute ||
            minutes < 45 && strings.minutes.replace(/%d/i, Math.round(minutes)) ||
            minutes < 90 && strings.hour ||
            hours < 24 && strings.hours.replace(/%d/i, Math.round(hours)) ||
            hours < 42 && strings.day ||
            days < 30 && strings.days.replace(/%d/i, Math.round(days)) ||
            days < 45 && strings.month ||
            days < 365 && strings.months.replace(/%d/i, Math.round(days / 30)) ||
            years < 1.5 && strings.year ||
            strings.years.replace(/%d/i, Math.round(years));

        return $.trim([prefix, words, suffix].join(" "));
    }
});

logistimoApp.directive('tooltip', function () {
    return {
        restrict: 'A',
        link: function (scope, element, attrs) {
            $(element).hover(function () {
                // on mouseenter
                $(element).tooltip('show');
            }, function () {
                // on mouseleave
                $(element).tooltip('hide');
            });
        }
    };
});
logistimoApp.directive('ngEnter', function () {
    return function (scope, element, attrs) {
        element.bind("keydown keypress", function (event) {
            if (event.which === 13) {
                scope.$apply(function () {
                    scope.$eval(attrs.ngEnter);
                });
                event.preventDefault();
            }
        });
    };
});

logistimoApp.directive('disableAnimation', function ($animate) {
    return {
        restrict: 'A',
        link: function ($scope, $element, $attrs) {
            $attrs.$observe('disableAnimation', function (value) {
                $animate.enabled(!value, $element);
            });
        }
    }
});

logistimoApp.filter('reverse', function () {
    return function (items) {
        if (checkNotNullEmpty(items)) {
            return items.slice().reverse();
        }
    };
});
logistimoApp.directive('inverted', function () {
    return {
        require: 'ngModel',
        link: function (scope, element, attrs, ngModel) {
            ngModel.$parsers.push(function (val) {
                return !val;
            });
            ngModel.$formatters.push(function (val) {
                return !val;
            });
        }
    };
});
logistimoApp.directive('userDomainSelect', function () {
    return {
        restrict: 'E',
        replace: true,
        template: '<div>' +
        '<lg-uib-select multiple="multiple" query="query(q)" ui-model="model" place-holder="Choose Domains"> </lg-uib-select>' +
        '</div>',
        scope: {
            model: '='
        },
        controller: ['$scope', 'domainService', function ($scope, domainService) {
            $scope.query = function (query) {
                domainService.getDomainSuggestions(query.term).then(function (data) {
                    var rData = {results: []};
                    for (var i in data.data) {
                        rData.results.push({'text': data.data[i].name, 'id': data.data[i].dId});
                    }
                    query.callback(rData);
                }).catch(function error(msg) {
                    $scope.$parent.showErrorMsg(msg);
                });
            }
        }]
    }
});

logistimoApp.directive('eventDistribution', function () {
    return {
        restrict: 'A',
        replace: false,
        template: '<span ng-click="open()">{{percentage}}</span>',
        scope: {
            eveTitle: '=',
            material:'=',
            stock:'=',
            pieData:'=',
            pieOpts: '=',
            percentage: '@',
            show: '='
        },
        controller: [ '$scope','$uibModal', function($scope,$uibModal){

            $scope.open = function () {
                $scope.modalInstance = $uibModal.open({
                    templateUrl: 'views/dashboard/event-pie.html',
                    scope: $scope
                });
            };

            $scope.cancel = function () {
                $scope.modalInstance.dismiss('cancel');
            };

        }]
    };
});

logistimoApp.directive('tempDistribution', function () {
    return {
        restrict: 'A',
        replace: true,
        template: '<span ng-click="open()"><fusion-chart type="\'doughnut2d\'" height="\'26\'" width="\'55\'" chart-data="pieIconData" chart-options="pieIconOpts" chart-id="pie_{{eveTitle}}" simple="\'true\'"></fusion-chart></span>',
        scope: {
            eveTitle: '@pieId',
            pieData:'=',
            pieIconData:'=',
            pieOpts: '=',
            pieIconOpts: '=',
            isTemp: '='
        },
        controller: [ '$scope','$uibModal', function($scope,$uibModal){
            $scope.open = function () {
                $scope.modalInstance = $uibModal.open({
                    templateUrl: 'views/dashboard/event-pie.html',
                    scope: $scope
                });
            };

            $scope.cancel = function () {
                $scope.modalInstance.dismiss('cancel');
            };

        }]
    };
});

logistimoApp.directive('upperCase', function() {
    return {
        require: 'ngModel',
        link: function ($scope, element, attrs, modelCtrl) {
            modelCtrl.$parsers.push(function (input) {
                return input ? input.toUpperCase() : "";
            });
            element.css("text-transform", "uppercase");
        }
    };
});

logistimoApp.directive('spinner', function () {
    return {
        restrict: 'E',
        template: '<div class="spinner" ng-class="{\'mini\':__mini}"><div class="rect1"></div><div class="rect2"></div><div class="rect3"></div>' +
        '<div class="rect4"></div><div class="rect5"></div> </div>',
        scope : {},
        link: function ($scope, $element, $attrs) {
            $attrs.$observe('mini', function (value) {
                if(checkNotNull(value)){
                    $scope.__mini=true;
                }else{
                    $scope.__mini = false;
                }
            });
        }
    }
});

logistimoApp.directive('domainTree', function(){
    return {
        restrict: 'E',
        templateUrl: 'views/domains/domain-tree.html'
    }
});

logistimoApp.directive('ngEscape', function() {
    return {
        link: function postLink(scope, element, attrs){
            jQuery(document).on('keyup', function(event){
                if (event.which === 27) {
                    scope.$apply(function () {
                        scope.$eval(attrs.ngEscape);
                    });
                    event.preventDefault();
                }
            });
        }
    };
});

logistimoApp.directive('emptyToNull', function () {
    return {
        require: 'ngModel',
        restrict: 'A',
        link: function (scope, elem, attrs, ctrl) {
            ctrl.$parsers.push(function(viewValue){
                if(viewValue === '') {
                    return null;
                }
                return viewValue;
            });

            ctrl.$formatters.push(function (viewValue) {
                if(viewValue === '') {
                    return null;
                }
                return viewValue;
            });
        }
    };
});

logistimoApp.directive('lgUibSelect', function () {
    return {
        restrict: 'E',
        templateUrl: 'views/lg-uib-select.html',
        scope: {
            queryCallback:'&query',
            uiModel:'=',
            placeHolder:'@',
            multiple: '@?',
            ngDisabled: '=',
            allowClear: '@?',
            limit:'@',
            appendToBody:'@',
            reset: '@?'
        },
        controller: [ '$scope', function($scope) {
            $scope.model = {selectModel:$scope.uiModel};

            $scope.filteredData = [];

            $scope.filter = function (query) {
                $scope.queryCallback({q: {term: query, callback: finalData}});
                if(checkNullEmpty(query) && $scope.reset) {
                    $scope.filteredData = [];
                }
            };
            function finalData(data) {
                $scope.filteredData = data.results;
            }
            var setModel = false;
            $scope.$watch('uiModel',function(newValue,oldValue) {
                if(newValue != oldValue) {
                    if (!setModel && $scope.model.selectModel != newValue) {
                        $scope.model = {selectModel: newValue};
                        setModel = true;
                    } else {
                        setModel = false;
                    }
                }
            });
            $scope.$watch('model.selectModel',function(newValue,oldValue){
                if(newValue != oldValue) {
                    $scope.uiModel = newValue;
                    setModel = false;
                }
            });
        }]
    };
});
logistimoApp.directive('noDoubleQuotes', function() {
    return {
        require: 'ngModel',
        restrict: 'A',
        link: function(scope, element, attrs, modelCtrl) {
            modelCtrl.$parsers.push(function(inputValue) {
                if (inputValue == undefined)
                    return ''
                var cleanInputValue = inputValue.replace(/["]+/g, '');
                if (cleanInputValue != inputValue) {
                    modelCtrl.$setViewValue(cleanInputValue);
                    modelCtrl.$render();
                }
                return cleanInputValue;
            });
        }
    }
});

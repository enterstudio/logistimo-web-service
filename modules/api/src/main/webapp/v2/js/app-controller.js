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

(['ng', 'logistimoApp'], function (ng, logistimoApp) { "use strict";
    logistimoApp.controller("AppController",
        function ($scope, $route, $routeParams, $location, $uibModal, requestContext, ngI18nResourceBundle, $timeout,
                  domainCfgService, exportService, userService, $window,$sce, iAuthService,$q,$rootScope,
                  linkedDomainService, domainService, configService, dashboardService) {
            var renderContext = requestContext.getRenderContext();

            $scope.showpopup = 'showpopup';
            $scope.hidepopup = 'hidepopup';

            var resourceBundleName = 'resourceBundle';
            /*  @if NODE_ENV == 'PRODUCTION' || NODE_ENV == 'DEVELOPMENT' || NODE_ENV == 'TEST' */
            /* @echo resourceInc */
            /* @endif */
            $scope.rFetch = false;
            $scope.showLPage = false;
            $scope.disableScroll = function(){
                document.body.classList.toggle("noscroll",true);
            };
            $scope.enableScroll = function(){
                document.body.classList.toggle("noscroll",false);
            };
            $scope.showDomainTree = function() {
                $scope.showLPage = true;
                $scope.disableScroll();
            };
            $scope.hideDomainTree = function() {
                $scope.showLPage = false;
                $scope.enableScroll();
            };
            $scope.languages = [{"locale": "en",txt:"English"}, {"locale": "fr",txt:"Française (French)"}];
            $scope.$watch('i18n.language', function (language,oldval) {
                if (!checkNullEmpty(language) && (checkNullEmpty(oldval) || (!$scope.rFetch && checkNullEmpty($scope.resourceBundle)) || language.locale != oldval.locale)) {
                    $scope.rFetch = true;
                    $scope.showLoading();
                    ngI18nResourceBundle.get({locale: language.locale,name: resourceBundleName}).success(function (resourceBundle) {
                        $rootScope.resourceBundle = $scope.resourceBundle = resourceBundle;
                    }).finally(function(){
                        $scope.hideLoading();
                        $scope.rFetch = false;
                    });
                }
            });
            $scope.i18n = {language: $scope.languages[0]};
            $scope.$back = function () {
                window.history.back();
            };
            $scope.windowTitle = "Simple";
            $scope.setWindowTitle = function (title) {
                $scope.windowTitle = title;
            };
            $scope.getAddress = function (address) {
                return constructAddressStr(address);
            };
            $scope.getInstanceTime = function () {
                var now = new Date();
                var timeString = now.toTimeString();
                var instanceTime = timeString.match(/\d+:\d+:\d+/i);
                return ( instanceTime[0] );
            };
            $scope.showNotification = function(message, type) {
                message = checkNotNullEmpty(message)?message:$scope.resourceBundle['general.error'];
                switch(type) {
                    case 'success':
                        toastr.success(message);
                        break;
                    case 'info':
                        toastr.info(message);
                        break;
                    case 'warning':
                        toastr.warning(message);
                        break;
                    case 'error':
                        toastr.error(message);
                        break;
                }
            };
            $scope.showPartialSuccessMsg = function (msg) {
                if (checkNotNullEmpty(msg.status)) {
                    if (msg.status == '206') {
                        $scope.showWarning(msg.data);
                    } else {
                        $scope.showSuccess(msg.data);
                    }
                } else {
                    $scope.showSuccess(msg.data);
                }
            };
            $scope.showSuccess = function (message) {
                $scope.showNotification(cleanupString(message), 'success');
            };
            $scope.showFormError = function(){
                $scope.showError($scope.resourceBundle['form.error']);
            };
            $scope.showError = function (message) {
                if( checkNotNullEmpty(message) && message.indexOf("Login changed, rejecting deferred calls.") == -1 ) {
                    $scope.showNotification(cleanupString(message), 'error');
                }
            };
            $scope.showWarning = function (message) {
                $scope.showNotification(cleanupString(message), 'warning');
            };
            $scope.showInfo = function (message) {
                $scope.showNotification(cleanupString(message), 'info');
            };
            $scope.showErrorMsg = function (msg) {
                if(checkNotNullEmpty(msg.data)) {
                    if(checkNotNullEmpty(msg.data.message)) {
                        $scope.showNotification(cleanupString(msg.data.message), 'error');
                    } else {
                        $scope.showNotification(cleanupString($scope.resourceBundle['general.error']), 'error');
                    }
                }else if(checkNotNullEmpty(msg.message)) {
                    $scope.showError(msg.message);
                } else if(msg.data == '') {
                    $scope.showError(msg.data);
                } else {
                    $scope.showError(msg);
                }
            };


            $scope.loadTemplate = '<h1><span class="glyphicons glyphicons-cogwheel spin" style="color:#000000;font-size: 1.6em;"></span></h1>';
            $scope.loaders = 0;
            $scope.showLoading = function(restore) {
                if (restore || $scope.loaders++ == 0) {
                    $scope.showLoadIcon = true;
                }
            };
            $scope.hideLoading = function(forceClose){
                if(forceClose) {
                    $scope.showLoadIcon = false;
                }else if(--$scope.loaders <= 0) {
                    $scope.showLoadIcon = false;
                    if($scope.loaders < 0){
                        $scope.loaders = 0;
                    }
                }
            };
            $scope.showLogin = function(action) {
                if($scope.lgModalInstance == undefined) {
                    if($scope.loaders>0){
                        $scope.hideLoading(true);
                    }
                    $scope.lgModalInstance = $uibModal.open({
                        templateUrl: 'views/login.html',
                        scope: $scope,
                        backdrop: 'static',
                        backdropClass:'login-modal',
                        windowClass:'login-modal-win',
                        keyboard: false,
                        resolve: {
                            userId: function () {
                                return $scope.curUser;
                            }
                        }
                    });
                }
            };
            $scope.hideLogin = function() {
                if (checkNotNullEmpty($scope.lgModalInstance)) {
                    if($scope.loaders>0){
                        $scope.showLoading(true);
                    }
                    $scope.lgModalInstance.close();
                    $scope.lgModalInstance = undefined;
                }
            };
            $scope.logout = function() {
                $scope.showLoading();
                iAuthService.logout().then(function (data){
                    $scope.clearSesNConfig();
                    $scope.userLoggedOut = true;
                    $rootScope.currentDomain = undefined;
                    $scope.showLogin();
                }).catch(function error(msg){
                    $scope.showErrorMsg(msg);
                }).finally(function (){
                    $scope.hideLoading();
                });
            };

            $scope.$on("event:auth-loginRequired", function () {
                //On Session timeout, resetting language.
                $scope.i18n = {language: $scope.languages[0]};
                $scope.showLogin();
            });

            /**
             * Reloading page..
             * @type {boolean}
             * @private
             */
            $scope._rel = false;

            $scope.$on("event:reload-page", function (rejection, data) {
                if(!$scope._rel){
                    $scope._rel = true;
                }else{
                    return;
                }
                var type = data.headers("e");
                showUpgrade(type);
                if(type == '2') {
                    $window.location = "/v2/index.html#/?d=1";
                    $window.location.reload();
                }else{
                    $timeout($scope.reloadPage,5000);
                }
            });

            function showUpgrade(type) {
                $scope.rejectionType = type;
                $uibModal.open({
                    templateUrl: 'views/upgrade.html',
                    scope: $scope,
                    backdrop: 'static',
                    backdropClass:'login-modal',
                    windowClass:'login-modal-win',
                    keyboard: false,
                    resolve: {
                        userId: function () {
                            return $scope.curUser;
                        }
                    }
                })
            }

            $scope.reloadPage = function(){
                $window.location.reload();
            };

            $scope.$on("event:resource-notFound", function () {
                $scope.showError($scope.resourceBundle['error.connectiontimeout']);
            });

            $scope.getGeneralConfig = function() {
                $scope.showLoading();
                configService.getGeneralConfig().then(function (data) {
                    if (data) {
                        $scope.generalconfig = angular.fromJson(data.data);
                    }
                }).catch(function error(msg) {
                    $scope.showErrorMsg(msg);
                }).finally(function () {
                    $scope.loading = false;
                    $scope.hideLoading();
                });
            };

            $scope.getGeneralConfig();

            $scope.getSupportConfig = function() {
                $scope.showLoading();
                domainCfgService.getSupportCfg().then(function(data){
                    $scope.supportConfig = data.data;
                }).catch(function error(msg) {
                    $scope.showErrorMsg(msg);
                }).finally(function () {
                    $scope.loading = false;
                    $scope.hideLoading();
                });
            };

            $scope.generateAssetFilters = function(){
                $scope.assetFilters = [{"value": "0", "dV": "All"}];
                for(var i in $scope.assetConfig.assets){
                    $scope.assetFilters[$scope.assetConfig.assets[i].id] = {"value": $scope.assetConfig.assets[i].id, "dV": $scope.assetConfig.assets[i].an};
                }

                $scope.mAssetFilters = {md: {}, mg: {}};
                angular.forEach($scope.assetConfig.assets, function(asset){
                    if(asset.at == '1'){
                        $scope.mAssetFilters.mg[asset.id] = asset.an;
                    } else if(asset.at == '2') {
                        $scope.mAssetFilters.md[asset.id] = asset.an;
                    }
                });

                $scope.assetWSFilters = [{"status": "0", "dV": "All"}];
                for(var i in $scope.assetConfig.wses){
                    $scope.assetWSFilters[$scope.assetConfig.wses[i].status] = {"status": $scope.assetConfig.wses[i].status, "dV": $scope.assetConfig.wses[i].dV};
                }

                $scope.assetVendorMapping = {};

                angular.forEach($scope.assetConfig.assets, function(asset){
                     angular.forEach(asset.mcs, function(m){
                         $scope.assetVendorMapping[m.id] = m.name;
                     });
                });
            };

            $scope.hasUserChildDomains = false;

            $scope.clearSesNConfig = function(){
                $rootScope.currentDomain = $scope.currentDomain = null;
                $scope.createdOn = null;
                $scope.hasUserChildDomains = false;
                $scope.domainName = null;
                $scope.prevAction = null;
                $scope.curUser = null;
                $scope.dc = null;
                $scope.isTempMonOnly = null;
                $scope.isTempMonWLg = null;
                $scope.accountTabEnabled = null;
                $scope.ordersTabEnabled = null;
                $scope.configTabEnabled = null;
                $scope.repTabEnabled = null;
                $scope.invTabEnabled = null;
                $scope.iAdm = null;
                $scope.iSU = null;
                $scope.iMan = null;
                $scope.isDmdOnly = null;
                $scope.headerImage = null;
                $scope.mailId = null;
                $scope.defaultEntityId = null;
                $scope.defaultCurrency = null;
                $scope.dcntry = null;
                $scope.dstate = null;
                $scope.ddist = null;
                $rootScope.curUser = $scope.curUser = null;
                $scope.i18n = {language: $scope.languages[0]};
                $scope.support = null;
                $scope.mxE = false;
                $scope.assetConfig = null;
                $scope.iAU = null;
                $scope.tempOnlyAU = null;
                $scope.tempEnabled = null;
                $scope.allocate = null;
            };

            $scope.refreshDomainConfig = function () {
                $scope.showLoading();
                var deferred = $q.defer();
                domainCfgService.getDomainConfigMenuStats().then(function (data) {
                    $scope.dc = data.data;
                    $scope.iAU = data.data.iAU;
                    $rootScope.currentDomain = $scope.currentDomain = data.data.dId;
                    $scope.createdOn = data.data.createdOn;
                    $scope.hasUserChildDomains = data.data.hasChild;
                    $scope.isTempMonOnly = data.data.iTempOnly;
                    $scope.isTempMonWLg = data.data.iTempWLg;
                    $scope.tempOnlyAU = ($scope.isTempMonOnly || $scope.iAU);
                    $scope.tempEnabled = ($scope.isTempMonOnly || $scope.isTempMonWLg);
                    $scope.accountTabEnabled = data.data.iAccTbEn && !$scope.tempOnlyAU;
                    $scope.ordersTabEnabled = data.data.iOrdTbEn && !$scope.tempOnlyAU;
                    $scope.configTabEnabled = data.data.iConfTbEn;
                    $scope.repTabEnabled = data.data.iRepTbEn && !$scope.tempOnlyAU;
                    $scope.invTabEnabled = !$scope.tempOnlyAU;
                    $scope.iAdm = data.data.iAdm;
                    $scope.iSU = data.data.iSU;
                    $scope.iMan = data.data.iMan;
                    $scope.isDmdOnly = $scope.dc.iDmdOnly;
                    $scope.headerImage = $sce.trustAsHtml($scope.dc.hImg);
                    $scope.defaultCurrency = data.data.cur;
                    $scope.dcntry = data.data.cnt;
                    $scope.dstate = data.data.st;
                    $scope.ddist = data.data.dst;
                    $scope.iOCEnabled = data.data.iOCEnabled;
                    $scope.onlyNewUI = data.data.onlyNewUI;
                    $scope.support = data.data.support;
                    $scope.mxE = data.data.mxE;
                    $scope.accd = data.data.accd;
                    $scope.assetConfig = data.data.ac;
                    if(checkNotNullEmpty($scope.assetConfig))
                        $scope.generateAssetFilters();
                    $scope.domainName = data.data.dnm;
                    $rootScope.curUser = $scope.curUser = data.data.unm;
                    $scope.i18n.language = {"locale":data.data.lng};
                    $scope.mailId = data.data.em;
                    $scope.defaultEntityId = data.data.eid;
                    $scope.curUserName = data.data.ufn;
                    $scope.iATD = data.data.iATD;
                    $scope.iPredEnabled = data.data.iPredEnabled;
                    $scope.lmmt = data.data.mmt;
                    $scope.lmmd = data.data.mmd;
                    $scope.transRelease = data.data.tr;
                    $scope.allocate = data.data.allocateInventory;
                    $scope.hbUTag = data.data.hbUTag;
                    $scope.vo = data.data.vo;
                    $scope.ns = data.data.ns;
                    $scope.vt = data.data.vt;
                    $scope.ct = data.data.ct;
                    //revenue report tab enable/disable
                    $scope.rpe = data.data.rpe;
                    $scope.hasDashbaccess = data.data.mdp;
                    if($location.path() == '/' && !$scope.hasDashbaccess) {
                        $scope.redirectManagers();
                    }
                    setMinMaxText();
                    $scope.getDashboardConfig();
                    //$scope.getSessionDetails(deferred);
                    $scope.getSupportConfig();
                }).catch(function error(msg) {
                    $scope.showErrorMsg(msg);
                    deferred.reject(msg);
                }).finally(function (){
                    $scope.hideLoading();
                });
                return deferred.promise;
            };

            $scope.setOCEnabled = function(value){
                $scope.iOCEnabled = value;
            };

            $scope.setDefaultCurrency = function(value){
                $scope.defaultCurrency = value;
            };

            $scope.getSessionDetails = function(promiseHandle){
                $scope.showLoading();
                domainCfgService.getCurrentSessionDetails().then(function(data){
                    var details = data.data;
                    $scope.domainName = details.dnm;
                    $scope.curUser = details.unm;
                    $scope.i18n.language = {"locale":details.lng};
                    $scope.mailId = details.em;
                    $scope.defaultEntityId = details.eid;
                    $scope.curUserName = details.ufn;
                    if(checkNotNullEmpty(promiseHandle)){
                        promiseHandle.resolve(true);
                    }
                }).catch(function error(msg) {
                    $scope.showErrorMsg(msg);
                    if(checkNotNullEmpty(promiseHandle)){
                        promiseHandle.reject(msg);
                    }
                }).finally(function (){
                    $scope.hideLoading();
                });
            };

            $scope.setDefaultEntityId = function(value){
                $scope.defaultEntityId = value;
            };

            $scope.loadTempDashboard = function(){
                if($route && $route.current && $route.current.action == 'dashboard.overview'){
                    if(!$scope.iMan){
                        $location.path('dashboard/overview');
                    }else if($scope.iMan){
                        $location.path('setup/users');
                    }
                }
            };

            $scope.refreshDomainConfig();

            function setSubView(){
                if(checkNotNullEmpty($scope.resourceBundle)){
                    if(requestContext.getParam("d")==1){
                        if(checkNullEmpty($scope.domainName)) {
                            $timeout(function () {
                                $scope.showSuccess($scope.resourceBundle['upgrade.domain.msg'] + ' ' + "<b>" + (checkNotNullEmpty($scope.domainName) ? $scope.domainName : 'new') + ' ' + "</b>" + $scope.resourceBundle['domain']);
                            }, 1500);
                        }else{
                            $scope.showSuccess($scope.resourceBundle['upgrade.domain.msg'] + ' ' + "<b>" +  $scope.domainName + ' ' + "</b>" + $scope.resourceBundle['domain']);
                        }
                        delete $location.$$search["d"];
                        $location.$$compose();
                    }
                    $scope.subview = renderContext.getNextSection();
                }
            }

            function setMinMaxText() {
                if(checkNotNull($scope.resourceBundle) && $scope.lmmt == 1) {
                    $scope.mmd = $scope.resourceBundle['daysofstock'];
                    $scope.mmdt = $scope.resourceBundle['days'];
                    if ($scope.lmmd == 'weekly') {
                        $scope.mmd = $scope.resourceBundle['weeksofstock'];
                        $scope.mmdt = $scope.resourceBundle['weeks'];
                    } else if ($scope.lmmd == 'monthly') {
                        $scope.mmd = $scope.resourceBundle['monthsofstock'];
                        $scope.mmdt = $scope.resourceBundle['months'];
                    }
                }
            }

            $scope.$on(
                "requestContextChanged",
                function () {
                    if (!renderContext.isChangeRelevant() || $scope._rel) {
                        return;
                    }
                    if(checkNotNull($scope.resourceBundle)) {
                        setSubView();
                        setMinMaxText();
                    }else{
                        $scope.$watch("resourceBundle",function(){
                            setSubView();
                            setMinMaxText();
                        });
                    }
                }
            );
            $scope.$on("$routeChangeSuccess", function (event) {
                if (isRouteRedirect($route)) {
                    return;
                }
                requestContext.setContext($route.current.action, $routeParams);
                $scope.$broadcast("requestContextChanged", requestContext);
            });

            function isRouteRedirect(route) {
                return (typeof route.current == 'undefined' || typeof route.current.action == 'undefined');
            }
            $scope.getAction = function () {
                return requestContext.getAction();
            };
            $scope.changeContext = function(){
                $window.location = "index.html";
            };
            $scope.resetFilters = function(){
                $location.$$search = {};
                $location.$$compose();
            };
            $scope.isUndef = function(value){
                return (value == undefined || value == '');
            };
            $scope.isDef = function(value){
                return !$scope.isUndef(value);
            };
            $scope.isObjDef = function(value){
                return !checkNullEmptyObject(value);
            };
            $scope.map = {
                center: {
                    latitude: 0,
                    longitude: 0
                },
                zoom: 8,
                options: {scrollwheel: false}
            };

            $scope.switchConsole = function(){
                $scope.switchconsole = false;
                userService.switchConsole().then(function(data){
                    $scope.switchconsole = data.data;
                    if($scope.switchconsole){
                        $window.location = "/s/index.jsp";
                    }
                }).catch(function error(msg){
                    $scope.showErrorMsg(msg);
                });
            };
            $scope.switchDomain = function(id,name){
                if (!confirm($scope.resourceBundle['domain.switch'] + " " + name + '?')) {
                    return;
                }
                if(checkNotNullEmpty(id)){
                    $scope.domainSwitch(id);
                }
            };
            $scope.domainSwitch = function(id){
                $scope.showLoading();
                domainService.switchDomain(id).then(function (data) {
                    $scope.success = data.data;
                    $window.location = "/v2/index.html";
                }).catch(function (msg) {
                    $scope.showErrorMsg(msg);
                }).finally(function () {
                    $scope.hideLoading();
                })
            };

            $scope.hasParents = false;
            $scope.showLoading();
            linkedDomainService.getLinkedDomains(1).then(function(data) {
                $scope.parents = data.data;
                if($scope.parents != null && $scope.parents.length > 1){
                    $scope.hasParents=true;
                }
            }).catch(function (msg) {
                $scope.showErrorMsg(msg);
            }).finally(function() {
                $scope.hideLoading();
            });

            $scope.showLoading();
            $scope.action = true;
            linkedDomainService.getDomainPermission($scope.action).then(function(data){
                if(checkNotNullEmpty(data.data)){
                    $scope.dp = data.data.dp;
                    $scope.getRoleCapabilities();
                    $scope.copyConfig = data.data.cc;
                    $scope.copyMaterials = data.data.cm;
                    $scope.hasChild = data.data.hasChild;
                }
            }).catch(function error(msg){
                $scope.showErrorMsg(msg);
            }).finally(function (){
                $scope.hideLoading();
            });

            $scope.getRoleCapabilities = function () {
                $scope.showLoading();
                domainCfgService.getRoleCapabilitiesCfg().then(function (data) {
                    $scope.cnff = data.data;
                    if(checkNullEmpty($scope.cnff.et)){
                        $scope.cnff.enableVs = $scope.cnff.enableCs = $scope.cnff.enableEn = false;
                    }else{
                        $scope.cnff.enableVs = ($scope.cnff.et.indexOf("vnds")!=-1); //enable Vendor creation
                        $scope.cnff.enableCs = ($scope.cnff.et.indexOf("csts")!=-1); //enable Customer creation
                        $scope.cnff.enableEn = ($scope.cnff.et.indexOf("ents")!=-1); //enable Entity creation
                    }
                }).catch(function error(msg) {
                    $scope.showErrorMsg(msg, true);
                }).finally(function (){
                    $scope.hideLoading();
                });
            };

            $scope.getAllDashboards = function() {
                $scope.showLoading();
                dashboardService.getAll().then(function (data) {
                    $scope.dashboards = data.data;
                }).catch(function error(msg) {
                    $scope.showErrorMsg(msg);
                }).finally(function () {
                    $scope.hideLoading();
                });
            };

            $scope.getAllDashboards();

            $scope.setDashboard = function(newDashboards) {
                $scope.dashboards = newDashboards;
            };

            $scope.getDashboardConfig = function(){
                $scope.loading = true;
                $scope.showLoading();
                domainCfgService.getDashboardCfg().then(function(data){
                    $scope.dashboardConfig = data.data;
                }).finally (function(){
                    $scope.hideLoading();
                });
            };


            domainCfgService.getMapLocationMapping().then(function (data) {
                if (checkNotNullEmpty(data.data)) {
                    var a = angular.fromJson(data.data);
                    $rootScope.entDef = a.entdef;
                }
            });

            $scope.updateDashboardName = function(id,name) {
                $scope.dashboards.some(function(d){
                    if(d.dbId == id){
                        d.nm = name;
                        return true;
                    }
                });
            };

            $scope.getObjectLength = function (object) {
                return checkNotNullEmpty(object) ? Object.keys(object).length : 0;
            };

            $scope.encodeURIParam = function(value,noEncode) {
                return encodeURIParam(value, noEncode);
            };
            $scope.redirectManagers = function() {
                if(checkNullEmpty($scope.defaultEntityId)) {
                    $location.path('/setup/entities/all/');
                } else if($scope.isTempMonOnly){
                    $location.path('/setup/entities/detail/'+$scope.defaultEntityId + "/assets");
                } else {
                    $location.path('/setup/entities/detail/'+$scope.defaultEntityId + "/");
                }
            }
            /*$rootScope.getReportsMenu = function(){
                return reportMenus;
            };*/
            $rootScope.getReportWidgets = function(){
                return reportWidgets;
            };
        }
    );
})(angular, logistimoApp);

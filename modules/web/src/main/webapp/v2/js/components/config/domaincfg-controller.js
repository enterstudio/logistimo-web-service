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

var domainCfgControllers = angular.module('domainCfgControllers', []);
domainCfgControllers.controller('GeneralConfigurationController', ['$scope', 'domainCfgService', 'configService','userService',
    function ($scope, domainCfgService, configService, userService) {
        $scope.cnf = {};
        $scope.cnf.support = [];
        $scope.cnf.adminContact = {};
        $scope.exRow = [];
        $scope.cnf.support.push({"role":"","usrid":"","usrname":"","phm":"","em":""});
        $scope.cnf.lng = "en";
        $scope.loadCounter = 0;
        $scope.complete = false;
        $scope.loading = true;
        $scope.continue = true;
        $scope.pCnt = {};
        $scope.sCnt = {};
        $scope.pUser = undefined;
        $scope.sUser = undefined;
        $scope.defineWatchers = function () {
            $scope.$watch("cnf.cnt", function (newval, oldval) {
                if (newval != oldval) {
                    $scope.cnf.st = "";
                }
            });
            $scope.$watch("cnf.st", function (newval, oldval) {
                if (newval != oldval) {
                    $scope.cnf.ds = "";
                }
            });
        };
        $scope.getGeneralConfiguration = function () {
            $scope.loading = true;
            $scope.showLoading();
            domainCfgService.getGeneralCfg().then(function (data) {
                $scope.cnf = data.data;
                $scope.setCountry($scope.cnf.cnt);
                $scope.setState($scope.cnf.st);
                $scope.defineWatchers();
                $scope.setOCEnabled($scope.cnf.sc);
                $scope.setDefaultCurrency($scope.cnf.cur);
                $scope.getFilteredUserId($scope.cnf.support);
                updatePSUser();
            }).catch(function err(msg){
                $scope.showWarning($scope.resourceBundle['configuration.general.unavailable']);
            }).finally(function (){
                $scope.loading = false;
                $scope.hideLoading();
            });

        };
        LanguageController.call(this, $scope, configService); // load counter value - 2 (1-Lng, 2-mLng)
        TimezonesController.call(this, $scope, configService);
        CurrencyController.call(this, $scope, configService);
        LocationController.call(this, $scope, configService);
        $scope.setGeneralConfig = function () {
            if(checkNullEmpty($scope.pUser) && checkNotNullEmpty($scope.sUser)){
                $scope.showWarning($scope.resourceBundle['primary.admincontact.mandatory']);
                return;
            }
            if(checkNotNullEmpty($scope.pUser) && checkNotNullEmpty($scope.sUser) && $scope.pUser.id == $scope.sUser.id){
                $scope.showWarning($scope.resourceBundle['same.admincontacts.warning']);
                return;
            }
            if ($scope.pUser)
                $scope.continue = true;
            if ($scope.cnf.snh && checkNullEmpty($scope.cnf.nhn)) {
                $scope.showWarning($scope.resourceBundle['enternewhostnamemsg']);
            } else {
                $scope.loading = true;
                $scope.showLoading();
                $scope.getFilteredUserId($scope.cnf.support);
                $scope.getFilteredSupportConfig($scope.cnf.support);
                if ($scope.continue) {
                    domainCfgService.setGeneralCfg($scope.cnf).then(function (data) {
                        $scope.showSuccess(data.data);
                        $scope.setOCEnabled($scope.cnf.sc);
                        $scope.setDefaultCurrency($scope.cnf.cur);
                        $scope.refreshDomainConfig();
                    }).catch(function error(msg) {
                        $scope.showErrorMsg(msg, true);
                    }).finally(function () {
                        $scope.loading = false;
                        $scope.hideLoading();
                        $scope.getGeneralConfiguration();
                    });
                } else {
                    $scope.hideLoading();
                    return false;
                }
            }
        };
        $scope.getUser = function (data, uType) {
            if (checkNotNullEmpty(data)) {
                userService.getUser(data).then(function (data) {
                    getAdminContactUser(data, uType);
                    updateAdminContacts();
                }).catch(function error(msg) {
                    $scope.showErrorMsg(msg);
                })
            }
        }
        function getAdminContactUser(data,t){
            var user = {};
            user.id = data.data.id;
            user.usrname = data.data.fnm + ' ' + (data.data.lnm ? data.data.lnm : '');
            user.phnm = data.data.phm;
            user.em = data.data.em;
            if(t=='p'){
                $scope.pUser = user;
            }else{
                $scope.sUser = user;
            }
        }
        function updateAdminContacts() {
            if ($scope.pUser) {
                $scope.cnf.adminContact.pac = {userId:$scope.pUser.id};
            }
            if ($scope.sUser) {
                $scope.cnf.adminContact.sac = {userId:$scope.sUser.id};
            }
        }
        function setAdminContactUser(data,t) {
            var user = {};
            user.id = data.userId;
            user.usrname = data.userNm;
            user.phnm = data.phn;
            user.em = data.email;
            if (t == 'p') {
                $scope.pUser = user;
            } else {
                $scope.sUser = user;
            }
        }

        function updatePSUser() {
            if(checkNotNullEmpty($scope.cnf.adminContact)) {
                if (!checkNullEmptyObject($scope.cnf.adminContact.pac)) {
                    setAdminContactUser($scope.cnf.adminContact.pac,'p');
                }
                if (!checkNullEmptyObject($scope.cnf.adminContact.sac)) {
                    setAdminContactUser($scope.cnf.adminContact.sac,'s');
                }
            }
        }

        $scope.checkAdminUser = function (usr, r) {
            if (checkNullEmpty(usr)) {
                if (r == 'p') {
                    $scope.cnf.adminContact.pac = {};
                } else {
                    $scope.cnf.adminContact.sac = {};
                }
            }
        };
        $scope.finished = function () {
            $scope.loadCounter++;
            if (!$scope.complete && $scope.loadCounter == 5) {
                $scope.getGeneralConfiguration();
                $scope.complete = true;
            }
        };
        $scope.userPopulate = function(data, index) {
            $scope.userpopulate = [];
            $scope.userpopulate[index] = false;
            if(checkNotNullEmpty(data)){
                userService.getUser(data).then(function(data){
                    $scope.user = data.data;
                    $scope.cnf.support[index].usrname = $scope.user.fnm + ' ' + ($scope.user.lnm ? $scope.user.lnm : '');
                    $scope.cnf.support[index].phnm = $scope.user.phm;
                    $scope.cnf.support[index].em = $scope.user.em;
                    $scope.cnf.support[index].userpopulate = true;
                }).catch(function error(msg){
                    $scope.showErrorMsg(msg);
                })
            }
        };

        $scope.checkUserModel = function(index){
            var sitem = $scope.cnf.support[index];
            if(checkNullEmpty(sitem.modelusr)){
                sitem.phnm = "";
                sitem.em = "";
                sitem.usrname = "";
                sitem.usrid = "";
                sitem.userpopulate = false;
            }
        };

        $scope.getFilteredUserId = function(data){
            if(checkNotNullEmpty(data)){
                var mu = [];
                for(var i=0; i<data.length; i++){
                    if(checkNotNullEmpty(data[i].modelusr)){
                        data[i].usrid = data[i].modelusr.id;
                    }else if(checkNotNullEmpty(data[i].usrid)){
                        mu.push({"id":data[i].usrid});
                        data[i].modelusr = mu[0];
                        mu = [];
                    }
                }
            }
        };
        $scope.getFilteredSupportConfig = function(data){
            if(checkNotNullEmpty(data)){
                var cnt = 0;
                for(var i=0; i<data.length; i++){
                    if(checkNullEmpty(data[i].usrid) && checkNullEmpty(data[i].usrname) && checkNullEmpty(data[i].phnm) && checkNullEmpty(data[i].em)){
                        cnt ++;
                    } else {
                        if (checkNullEmpty(data[i].usrid)) {
                            var role = $scope.getRole(i);
                            var supPhnmValid = $scope.validateSupportPhone(data[i].phnm);
                            var supEmValid = checkNotNullEmpty(data[i].em) ? $scope.validateEmail(data[i].em) : true;

                            if(supPhnmValid != ''){ // empty means it is valid
                                if (supPhnmValid == 'r') { // phone number is not entered
                                    $scope.showWarning($scope.resourceBundle['support.config.useridphonerequired.msg'] + ' ' + role);
                                    $scope.continue = false;
                                } else if (supPhnmValid == 'f' || supPhnmValid == 's') { // phone number is invalid
                                    if (supEmValid) {
                                        $scope.showWarning($scope.resourceBundle['support.config.useridvalidphonerequired.msg'] + ' ' + role);
                                        $scope.continue = false;
                                    } else {
                                        $scope.showWarning($scope.resourceBundle['support.config.useridvalidphonevalidoptemailrequired.msg'] + ' ' + role);
                                        $scope.continue = false;
                                    }
                                }
                            } else {
                                if(!supEmValid) {
                                    $scope.showWarning($scope.resourceBundle['support.config.validemailrequired.msg'] + ' ' + role);
                                    $scope.continue = false;
                                }
                            }
                        }
                    }
                }
                if(cnt == 3){
                    $scope.continue = true;
                }
            }
        };

        $scope.getRole = function(index){
            var role = "";
            if($scope.cnf.support[index].role == "ROLE_ko"){
                role = $scope.resourceBundle['role.kioskowner'];
            }else if($scope.cnf.support[index].role == "ROLE_sm"){
                role = $scope.resourceBundle['role.servicemanager'];
            }else {
                role = $scope.resourceBundle['role.domainowner'];
            }
            return role;
        };

        $scope.validateSupportPhone = function(phnm) {
            return validateSupport(phnm);
        }

        $scope.validateEmail = function(email) {
            return checkEmail(email);
        }
    }
]);
domainCfgControllers.controller('CapabilitiesConfigurationController', ['$scope', 'domainCfgService',
    function ($scope, domainCfgService) {
        $scope.cnf = {};
        $scope.uiCnf = {};
        $scope.cap = ["inventory", "orders"];
        $scope.tm = ["vs", "es", "er", "sc", "wa", "ts", "ns","vo","vp","ep","pi","xi","vh","vt","ct"];
        $scope.et = ["ents", "csts", "vnds"];
        $scope.loading = false;
        $scope.getCapabilitiesConfiguration = function () {
            $scope.loading = true;
            $scope.showLoading();
            domainCfgService.getCapabilitiesCfg().then(function (data) {
                $scope.cnf = data.data;
                $scope.uiCnf = angular.copy($scope.cnf);
                $scope.updateTags();
                if(checkNullEmpty($scope.uiCnf.atexp) || $scope.uiCnf.atexp == 0) {
                    $scope.uiCnf.atexp = 30;
                }
            }).catch(function error(msg) {
                $scope.showErrorMsg(msg, true);
            }).finally(function (){
                $scope.loading = false;
                $scope.hideLoading();
            });
        };
        $scope.updateTags = function () {
            var i;
            if (checkNotNullEmpty($scope.uiCnf.iTags)) {
                $scope.uiCnf.hi = [];
                for (i = 0; i < $scope.uiCnf.iTags.length; i++) {
                    $scope.uiCnf.hi.push($scope.uiCnf.iTags[i].text);
                }
            } else if (checkNotNullEmpty($scope.uiCnf.hi)) {
                $scope.uiCnf.iTags = [];
                for (i = 0; i < $scope.uiCnf.hi.length; i++) {
                    $scope.uiCnf.iTags.push({"id": $scope.uiCnf.hi[i], "text": $scope.uiCnf.hi[i]});
                }
            }
            if (checkNotNullEmpty($scope.uiCnf.oTags)) {
                $scope.uiCnf.ho = [];
                for (i = 0; i < $scope.uiCnf.oTags.length; i++) {
                    $scope.uiCnf.ho.push($scope.uiCnf.oTags[i].text);
                }
            } else if (checkNotNullEmpty($scope.uiCnf.ho)) {
                $scope.uiCnf.oTags = [];
                for (i = 0; i < $scope.uiCnf.ho.length; i++) {
                    $scope.uiCnf.oTags.push({"id": $scope.uiCnf.ho[i], "text": $scope.uiCnf.ho[i]});
                }
            }
            if (checkNotNullEmpty($scope.uiCnf.iiTags)) {
                $scope.uiCnf.hii = [];
                for (i = 0; i < $scope.uiCnf.iiTags.length; i++) {
                    $scope.uiCnf.hii.push($scope.uiCnf.iiTags[i].text);
                }
            } else if (checkNotNullEmpty($scope.uiCnf.hii)) {
                $scope.uiCnf.iiTags = [];
                for (i = 0; i < $scope.uiCnf.hii.length; i++) {
                    $scope.uiCnf.iiTags.push({"id": $scope.uiCnf.hii[i], "text": $scope.uiCnf.hii[i]});
                }
            }
            if (checkNotNullEmpty($scope.uiCnf.irTags)) {
                $scope.uiCnf.hir = [];
                for (i = 0; i < $scope.uiCnf.irTags.length; i++) {
                    $scope.uiCnf.hir.push($scope.uiCnf.irTags[i].text);
                }
            } else if (checkNotNullEmpty($scope.uiCnf.hir)) {
                $scope.uiCnf.irTags = [];
                for (i = 0; i < $scope.uiCnf.hir.length; i++) {
                    $scope.uiCnf.irTags.push({"id": $scope.uiCnf.hir[i], "text": $scope.uiCnf.hir[i]});
                }
            }
            if (checkNotNullEmpty($scope.uiCnf.ipTags)) {
                $scope.uiCnf.hip = [];
                for (i = 0; i < $scope.uiCnf.ipTags.length; i++) {
                    $scope.uiCnf.hip.push($scope.uiCnf.ipTags[i].text);
                }
            } else if (checkNotNullEmpty($scope.uiCnf.hip)) {
                $scope.uiCnf.ipTags = [];
                for (i = 0; i < $scope.uiCnf.hip.length; i++) {
                    $scope.uiCnf.ipTags.push({"id": $scope.uiCnf.hip[i], "text": $scope.uiCnf.hip[i]});
                }
            }
            if (checkNotNullEmpty($scope.uiCnf.iwTags)) {
                $scope.uiCnf.hiw = [];
                for (i = 0; i < $scope.uiCnf.iwTags.length; i++) {
                    $scope.uiCnf.hiw.push($scope.uiCnf.iwTags[i].text);
                }
            } else if (checkNotNullEmpty($scope.uiCnf.hiw)) {
                $scope.uiCnf.iwTags = [];
                for (i = 0; i < $scope.uiCnf.hiw.length; i++) {
                    $scope.uiCnf.iwTags.push({"id": $scope.uiCnf.hiw[i], "text": $scope.uiCnf.hiw[i]});
                }
            }
            if (checkNotNullEmpty($scope.uiCnf.itTags)) {
                $scope.uiCnf.hit = [];
                for (i = 0; i < $scope.uiCnf.itTags.length; i++) {
                    $scope.uiCnf.hit.push($scope.uiCnf.itTags[i].text);
                }
            } else if (checkNotNullEmpty($scope.uiCnf.hit)) {
                $scope.uiCnf.itTags = [];
                for (i = 0; i < $scope.uiCnf.hit.length; i++) {
                    $scope.uiCnf.itTags.push({"id": $scope.uiCnf.hit[i], "text": $scope.uiCnf.hit[i]});
                }
            }
        };
        $scope.getCapabilitiesConfiguration();
        $scope.setCapabilitiesConfiguration = function () {
            $scope.uiCnf.hi = [];
            $scope.uiCnf.ho = [];
            $scope.uiCnf.hii = [];
            $scope.uiCnf.hir = [];
            $scope.uiCnf.hip = [];
            $scope.uiCnf.hiw = [];
            $scope.uiCnf.hit = [];
            if (checkNullEmpty($scope.uiCnf.mdri))
                $scope.uiCnf.mdri = 0;
            if (checkNullEmpty($scope.uiCnf.iri))
                $scope.uiCnf.iri = 0;
            if (checkNullEmpty($scope.uiCnf.aplui))
                $scope.uiCnf.aplui = 0;
            if (checkNullEmpty($scope.uiCnf.stwd))
                $scope.uiCnf.stwd = 0;

            if (checkNullEmpty($scope.uiCnf.atexp) || $scope.uiCnf.atexp == 0)
                $scope.uiCnf.atexp = 30;

            $scope.updateTags();
            $scope.loading = true;
            $scope.showLoading();
            domainCfgService.setCapabilitiesCfg($scope.uiCnf).then(function (data) {
                $scope.showSuccess(data.data[0]);
                $scope.setCapabilitiesByRole($scope.uiCnf.ro,data.data[1]);
                $scope.getCapabilitiesByRole($scope.uiCnf.ro);
                $scope.cnfRole = $scope.uiCnf.ro;
            }).catch(function error(msg) {
                $scope.showErrorMsg(msg, true);
            }).finally(function (){
                $scope.loading = false;
                //$scope.getCapabilitiesConfiguration();
                $scope.hideLoading();
            });
        };
        $scope.setTransactionMenu = function (item) {
            var items = angular.copy($scope.tm);
            if (item === 'inventory') {
                items = items.splice(0, 6);
            } else if (item === 'orders') {
                items = items.splice(6, 2);
            }
            if ($scope.uiCnf.cap != undefined && $scope.uiCnf.cap.indexOf(item) > -1) {
                angular.forEach(items, function (it) {
                    var ind = $scope.uiCnf.tm.indexOf(it);
                    if (ind > -1) {
                        $scope.uiCnf.tm.splice(ind, 1);
                    }
                });
            } else {
                if($scope.uiCnf.tm == undefined){
                    $scope.uiCnf.tm = [];
                }
                angular.forEach(items, function (it) {
                    var ind = $scope.uiCnf.tm.indexOf(it);
                    if (ind === -1) {
                        $scope.uiCnf.tm.push(it);
                    }
                });
            }
        };
        $scope.$watch('cnfRole',function(newVal,oldVal){
            if(!$scope.roleReset) {
                if ($scope.formUpdated && newVal != oldVal) {
                    if (confirm($scope.resourceBundle['configuration.capabilities.change.confirm'])) {
                        $scope.formUpdated = false;
                    } else {
                        $scope.cnfRole = oldVal;
                        $scope.roleReset = true;
                        return;
                    }
                }
                $scope.getCapabilitiesByRole(newVal);
            }
            $scope.roleReset = false;
        });
        $scope.setFormUpdated = function(){
            $scope.formUpdated = true;
        };

        $scope.toggleTransfers = function (type) {
            if (type == 'v') {
                if ($scope.uiCnf.tm.indexOf('vt') != -1 && $scope.uiCnf.tm.indexOf('ct') != -1) {
                    $scope.uiCnf.tm.splice($scope.uiCnf.tm.indexOf('ct'), 1);
                    $scope.uiCnf.tm.splice($scope.uiCnf.tm.indexOf('vt'), 1);
                }
            } else {
                if($scope.uiCnf.tm.indexOf('ct') == -1 && $scope.uiCnf.tm.indexOf('vt') == -1) {
                    $scope.uiCnf.tm.push("vt");
                    $scope.uiCnf.tm.push("ct");
                }
            }
        };


        $scope.getCapabilitiesByRole = function (role) {
            if (role === "ROLE_ko") {
                $scope.uiCnf = angular.copy($scope.cnf.roleConfig.ROLE_ko);
            } else if (role === "ROLE_sm") {
                $scope.uiCnf = angular.copy($scope.cnf.roleConfig.ROLE_sm);
            } else if (role === "ROLE_do") {
                $scope.uiCnf = angular.copy($scope.cnf.roleConfig.ROLE_do);
            } else if (role === "ROLE_su") {
                $scope.uiCnf = angular.copy($scope.cnf.roleConfig.ROLE_su);
            } else {
                $scope.uiCnf = angular.copy($scope.cnf);
            }
            if(!$scope.uiCnf.tm) {
                $scope.uiCnf.tm = [];
            }
            $scope.iTags = [];
            $scope.oTags = [];
            $scope.updateTags();
            $scope.formUpdated=false;
            // Copy the authentication token expiry and synchronization by mobile related configuration from $scope.cnf into $scope.uiCnf only if role is not "" (because for that case, the below steps are unnecessary)
            if (role === "ROLE_ko" || role === "ROLE_sm" || role === "ROLE_do" || role === "ROLE_su") {
                $scope.uiCnf.atexp = $scope.cnf.atexp;
                $scope.uiCnf.mdri = $scope.cnf.mdri;
                $scope.uiCnf.iri = $scope.cnf.iri;
                $scope.uiCnf.aplui = $scope.cnf.aplui;
                $scope.uiCnf.stwd = $scope.cnf.stwd;
                $scope.uiCnf.llr = $scope.cnf.llr;
                $scope.uiCnf.theme = $scope.cnf.theme;
            }
        };
        $scope.setCapabilitiesByRole = function (role, lu) {
            if (role === "ROLE_ko") {
                $scope.cnf.roleConfig.ROLE_ko = $scope.uiCnf;
            } else if (role === "ROLE_sm") {
                $scope.cnf.roleConfig.ROLE_sm = $scope.uiCnf;
            } else if (role === "ROLE_do") {
                $scope.cnf.roleConfig.ROLE_do = $scope.uiCnf;
            } else if (role === "ROLE_su") {
                $scope.cnf.roleConfig.ROLE_su = $scope.uiCnf;
            } else {
                $scope.cnf = $scope.uiCnf;
            }
            $scope.cnf.lastUpdated = lu;
            $scope.cnf.fn = angular.copy($scope.curUserName);
            // Update $scope.cnf variables for the ROLE_ko, ROLE_sm, ROLE_do, ROLE_su. For role == "" it's already done above.
            if (role === "ROLE_ko" || role === "ROLE_sm" || role === "ROLE_do" || role === "ROLE_su") {
                $scope.cnf.atexp = $scope.uiCnf.atexp;
                $scope.cnf.mdri = $scope.uiCnf.mdri;
                $scope.cnf.iri = $scope.uiCnf.iri;
                $scope.cnf.aplui = $scope.uiCnf.aplui;
                $scope.cnf.stwd = $scope.uiCnf.stwd;
                $scope.cnf.llr = $scope.uiCnf.llr;
                $scope.cnf.theme = $scope.uiCnf.theme;
            }
        }
    }
]);

domainCfgControllers.controller('ApprovalConfigurationController',['$scope','domainCfgService',
function($scope,domainCfgService){
    var ty = {ps:true, t:false};
    var hdng = {ps: $scope.resourceBundle['approval.config.purchase.sales'], t: $scope.resourceBundle['approval.config.transfer']};
    var info = {ps: $scope.resourceBundle['approval.config.purchase.sales.info'],
                t: $scope.resourceBundle['approval.config.transfer.info']};
    $scope.init = function(){
        $scope.orderType = ty;
        $scope.spc = false;
        $scope.stc = false;
        $scope.heading = hdng.ps;
        $scope.info = info.ps;
        $scope.aprvls = {order:{}};
        $scope.isAdd = true;
        $scope.orderCfg = {psoa: [], px: 24, sx: 24, tx: 24};
        $scope.edit = false;
        $scope.preSelectedTags = [];
    };
    $scope.init();

    $scope.populatePreSelected = function(newval, oldval) {
        $scope.preSelectedTags = [];
       if(checkNotNullEmpty($scope.orderCfg.psoa) && $scope.orderCfg.psoa.length > 0) {
           $scope.orderCfg.psoa.some(function(data){
                if(checkNotNullEmpty(data.enTgs) && data.enTgs.length > 0) {
                    for(var i in data.enTgs) {
                        $scope.preSelectedTags.push(data.enTgs[i]);
                    }
                }
           })
       }
    };
    $scope.addRow = function(){
        if(checkNullEmpty($scope.orderCfg.psoa)) {
            $scope.orderCfg = {psoa : []};
        }
        $scope.orderCfg.psoa.push({enTgs : [], poa: false, soa: false});
    };
    $scope.deleteRow = function(index) {
        $scope.orderCfg.psoa.splice(index, 1);
    };
    $scope.tabContent = function(type){
        angular.forEach(ty, function(value,key) {
            if(key === type)
                ty[key] = true;
            else
                ty[key] = false;
        });
        if(type == 'ps') {
            $scope.heading = hdng.ps;
            $scope.info = info.ps;
        } else {
            $scope.heading = hdng.t;
            $scope.info = info.t;
        }
        $scope.orderType = ty;
    };
    $scope.getFilteredUsers = function() {

        if(checkNotNullEmpty($scope.orderCfg.pa)) {
            var pap = $scope.orderCfg.pa;
            $scope.orderCfg.pa = [];
            for(var i=0; i< pap.length; i++) {
                $scope.orderCfg.pa.push({"id": pap[i].id, "text": pap[i].fnm+' ['+pap[i].id+']'});
            }
        }

        if(checkNotNullEmpty($scope.orderCfg.sa)) {
            var sas = $scope.orderCfg.sa;
            $scope.orderCfg.sa = [];
            for(var i=0; i< sas.length; i++) {
                $scope.orderCfg.sa.push({"id": sas[i].id, "text": sas[i].fnm+' ['+sas[i].id+']'});
            }
        }
    };
    $scope.updateTags = function() {
        if(checkNotNullEmpty($scope.orderCfg)) {
            if(checkNotNullEmpty($scope.orderCfg.psoa) && $scope.orderCfg.psoa.length > 0) {
                $scope.orderCfg.psoa.some(function(data) {
                    if(checkNotNullEmpty(data.enTgs)) {
                        data.eTgs = [];
                        for(var i=0; i<data.enTgs.length; i++) {
                            data.eTgs.push(data.enTgs[i].text);
                        }
                    } else if(checkNotNullEmpty(data.eTgs)) {
                        data.enTgs = [];
                        for(var i=0 ; i<data.eTgs.length; i++) {
                            data.enTgs.push({"id" : data.eTgs[i], "text" : data.eTgs[i]});
                        }
                    }
                });
            }
        }
    };
    $scope.validateApprovals = function() {
        if(checkNotNullEmpty($scope.orderCfg.sa) && checkNullEmpty($scope.orderCfg.pa)) {
            $scope.showWarning($scope.resourceBundle['approval.config.primary.approver.configure']);
            $scope.continue = false;
            return;
        }
        if(checkNotNullEmpty($scope.orderCfg.psoa))
        {
            $scope.orderCfg.psoa.some(function(data){
                if(checkNotNullEmpty(data)) {
                    if(checkNullEmpty(data.enTgs)){
                        $scope.showWarning($scope.resourceBundle['approval.config.purchase.entity.tags.configure']);
                        $scope.continue = false;
                        return;
                    }
                    if(!data.poa && !data.soa) {
                        $scope.showWarning($scope.resourceBundle['approval.config.time.of.approval']);
                        $scope.continue = false;
                        return;
                    }
                }
            });
        }
    };

    $scope.setApprovalsConfiguration = function() {
        $scope.continue = true;
        if(checkNotNullEmpty($scope.orderCfg)) {
            $scope.validateApprovals();
            if($scope.continue) {
                $scope.showLoading();
                $scope.updateTags();
                domainCfgService.setApprovalsConfig($scope.orderCfg).then(function (data) {
                    $scope.showSuccess(data.data);
                }).catch(function error(msg) {
                    $scope.showErrorMsg(msg, true);
                }).finally(function (){
                    $scope.getApprovalsConfiguration();
                    $scope.hideLoading();
                });
            }
        }else {
            $scope.showWarning($scope.resourceBundle['approval.config.none']);
        }
    };

    $scope.getApprovalsConfiguration = function() {
        $scope.showLoading();
        domainCfgService.getApprovalsConfig().then(function(data) {
            if(checkNotNullEmpty(data.data)) {
                $scope.orderCfg = data.data;
                $scope.updateTags();
            } else {
                $scope.orderCfg = {psoa : []};
            }
        }).catch(function error(msg) {
            $scope.showErrorMsg(msg, true);
        }).finally(function() {
           $scope.hideLoading();
        });
    };

    $scope.getApprovalsConfiguration();

}]);

domainCfgControllers.controller('InventoryConfigurationController', ['$scope', 'domainCfgService','OPTIMIZER','configService','$timeout',
    function ($scope, domainCfgService,OPTIMIZER,configService,$timeout) {
        $scope.inv = {};
        $scope.fr = ["daily", "weekly", "monthly"];
        $scope.forecast = [{key:$scope.resourceBundle['none'], value:-1}, {key:$scope.resourceBundle['demandforecast'], value:100}, {key:$scope.resourceBundle['order.optimalorderquantity'],value:200}];
        $scope.loading = false;
        var ty = {i:true,r:false,s:false,d:false,t:false};
        var ms = {i:true,r:false,s:false,d:false,t:false};
        var atdc = {i:true,r:false,s:false,d:false,t:false};
        $scope.inv.support =[];
        $scope.inv.support.push({"type":"","df":"","etsm":""});
        $scope.transType = ty;
        $scope.matStatus = ms;
        $scope.actualTransDate = atdc;
        $scope.inv.cimt=false;
        $scope.inv.crmt=false;
        $scope.inv.csmt=false;
        $scope.inv.ctmt=false;
        $scope.inv.cdmt=false;
        $scope.inv.irc=false;
        $scope.exRow = [];
        $scope.rxRow = [];
        $scope.sxRow = [];
        $scope.txRow = [];
        $scope.dxRow = [];
        $scope.dmntz = 'UTC';

        TimezonesControllerKVReversed.call(this, $scope, configService);

        $scope.getMaterialTagsCfg = function(inv){
            domainCfgService.getMaterialTagsCfg().then(function (data) {
                $scope.MatTags = data.data.tags;
                $scope.iMatTags=angular.copy($scope.MatTags);
                if(checkNotNullEmpty(inv)){
                    if(checkNotNullEmpty(inv.imt)){
                        for(var i=0;i<inv.imt.length;i++){
                            var idx = $scope.iMatTags.indexOf(inv.imt[i].mtg);
                            if(checkNotNullEmpty(idx) && checkNotNullEmpty($scope.iMatTags[idx])){
                                $scope.iMatTags[idx].hide = false;
                                $scope.iMatTags.splice(idx,1);
                            }
                        }
                    }
                    $scope.rMatTags = angular.copy($scope.MatTags);
                    if(checkNotNullEmpty(inv.rmt)){
                        for(var i=0;i<inv.rmt.length;i++){
                            var idx = $scope.rMatTags.indexOf(inv.rmt[i].mtg);
                            if(checkNotNullEmpty(idx) && checkNotNullEmpty($scope.rMatTags[idx])){
                                $scope.rMatTags[idx].hide = false;
                                $scope.rMatTags.splice(idx,1);
                            }
                        }
                    }

                    $scope.tMatTags = angular.copy($scope.MatTags);
                    if(checkNotNullEmpty(inv.tmt)){
                        for(var i=0;i<inv.tmt.length;i++){
                            var idx = $scope.tMatTags.indexOf(inv.tmt[i].mtg);
                            if(checkNotNullEmpty(idx) && checkNotNullEmpty($scope.tMatTags[idx])){
                                $scope.tMatTags[idx].hide = false;
                                $scope.tMatTags.splice(idx,1);
                            }
                        }
                    }
                    $scope.sMatTags =angular.copy($scope.MatTags);
                    if(checkNotNullEmpty(inv.smt)){
                        for(var i=0;i<inv.smt.length;i++){
                            var idx = $scope.sMatTags.indexOf(inv.smt[i].mtg);
                            if(checkNotNullEmpty(idx)&& checkNotNullEmpty($scope.sMatTags[idx])){
                                $scope.sMatTags[idx].hide = false;
                                $scope.sMatTags.splice(idx,1);
                            }
                        }
                    }
                    $scope.dMatTags = angular.copy($scope.MatTags);
                    if(checkNotNullEmpty(inv.dmt)){
                        for(var i=0;i<inv.dmt.length;i++){
                            var idx = $scope.dMatTags.indexOf(inv.dmt[i].mtg);
                            if(checkNotNullEmpty(idx) && checkNotNullEmpty($scope.dMatTags[idx])){
                                $scope.dMatTags[idx].hide = false;
                                $scope.dMatTags.splice(idx,1);
                            }
                        }
                    }
                }
            }).catch(function error(msg) {
                $scope.showErrorMsg(msg);
            }).finally(function(){
                $scope.addRows('a');
            });

        };

        $scope.getGeneralConfiguration = function() {
            $scope.loading = true;
            $scope.showLoading();

            domainCfgService.getGeneralCfg().then(function (data) {
                if (checkNotNullEmpty(data.data.tz)) {
                    $scope.dmntz = $scope.allTimezonesKVReversed[data.data.tz];
                }
            }).catch(function error(msg) {
                $scope.showErrorMsg(msg, true);
            }).finally(function () {
                $scope.loading = false;
                $scope.hideLoading();
            });
        };
        $scope.getGeneralConfiguration();

        $scope.initWatch = function() {
            $scope.$watch("inv.co", function(newVal, oldVal) {
               if( newVal != $scope.forecast[0].value ) {
                   if($scope.inv.crc != 1) {
                       $timeout(function () {
                           $scope.inv.co = oldVal;
                       }, 100);
                   }
               }
            });
            $scope.$watch("inv.crc", function(newVal, oldVal) {
                if(newVal == -1) {
                    if($scope.inv.co != -1) {
                        $timeout(function () {
                            $scope.inv.crc = oldVal;
                        }, 100);
                    } else {
                        if($scope.inv.mmType == '1'){
                            $scope.inv.mmType = '0';
                        }
                        $scope.inv.dispcr = false;
                        $scope.inv.dcrfreq = "";
                        $scope.inv.mcrfreq = "";
                        $scope.inv.showpr = false;
                        $scope.inv.irc = false;
                        $scope.inv.ersnsObj = [];
                        $scope.inv.edis = true;
                    }
                } else if(newVal == 0 && newVal != oldVal) {
                    if($scope.inv.co != -1) {
                        $timeout(function () {
                            $scope.inv.crc = oldVal;
                        }, 100);
                    } else {
                        $scope.inv.mcrfreq = $scope.fr[0];
                        $scope.inv.irc = false;
                        $scope.inv.ersnsObj = [];
                        $scope.inv.edis = true;
                    }
                } else if(newVal == 1) {
                    $scope.inv.mcrfreq = "";
                    if($scope.inv.mmType == 1 && newVal != oldVal) {
                        $scope.inv.mmFreq = angular.copy($scope.inv.crfreq)
                    }
                }
            });
            $scope.$watch("inv.dispcr", function(newVal) {
                if(newVal && checkNullEmpty($scope.inv.dcrfreq)) {
                    if($scope.inv.crc == 0) {
                        $scope.inv.dcrfreq = $scope.inv.mcrfreq;
                    } else {
                        $scope.inv.dcrfreq = $scope.fr[2];
                    }
                }
                if(!newVal) {
                    $scope.inv.dcrfreq = "";
                }
            });
            $scope.$watch("inv.mmType", function(newVal, oldVal) {
                if(newVal != oldVal) {
                    if (newVal == 1) {
                        $scope.inv.mmDur = 'daily';
                        if($scope.inv.crc == 1) {
                            $scope.inv.mmFreq = angular.copy($scope.inv.crfreq);
                        } else {
                            $scope.inv.mmFreq = undefined;
                        }
                    }
                }
            });
        };

        $scope.getInventoryConfiguration = function () {
            $scope.loading = true;
            $scope.showLoading();

            domainCfgService.getInventoryCfg().then(function (data) {
                $scope.inv = data.data;
                $scope.updateTags();
                $scope.retrieveFiltered();
                if(checkNullEmpty($scope.inv.et)){
                    $scope.inv.et = "00:00";
                }
                $scope.initWatch();
                //$scope.addRows();
            }).catch(function error(msg) {
                $scope.showErrorMsg(msg, true);
            }).finally(function (){
                $scope.getMaterialTagsCfg($scope.inv);
                $scope.loading = false;
                $scope.hideLoading();
            });
        };

        $scope.selectRsn = function () {
            if (!$scope.inv.cimt) {
                $scope.inv.cimt = !confirm("Do you want to clear the reasons by tag?");
            }
        };

        $scope.updateTags = function() {
            var i;
            if(checkNotNullEmpty($scope.inv.eTgs)){
                $scope.inv.enTgs = [];
                for(i=0; i<$scope.inv.eTgs.length; i++) {
                    $scope.inv.enTgs.push($scope.inv.eTgs[i].text);
                }
            } else if(checkNotNullEmpty($scope.inv.enTgs)){
                $scope.inv.eTgs = [];
                for( i=0;i<$scope.inv.enTgs.length;i++) {
                    $scope.inv.eTgs.push({"id":$scope.inv.enTgs[i],"text" : $scope.inv.enTgs[i]});
                }
            }
            if(checkNotNullEmpty($scope.inv.uTgs)) {
                $scope.inv.usrTgs = [];
                for(i=0; i<$scope.inv.uTgs.length; i++) {
                    $scope.inv.usrTgs.push($scope.inv.uTgs[i].text);
                }
            } else if(checkNotNullEmpty($scope.inv.usrTgs)) {
                $scope.inv.uTgs = [];
                for(i=0; i<$scope.inv.usrTgs.length; i++) {
                    $scope.inv.uTgs.push({"id":$scope.inv.usrTgs[i],"text" : $scope.inv.usrTgs[i]});
                }
            }
            if(checkNotNullEmpty($scope.inv.ersns)) {
                $scope.inv.ersnsObj = [];
                for (var i = 0; i < $scope.inv.ersns.length; i++) {
                    $scope.inv.ersnsObj.push({"id":$scope.inv.ersns[i],"text" : $scope.inv.ersns[i]});
                }
                $scope.inv.irc = true;
            }
        };

        $scope.validateStatus = function(defStatus,tempStatus,type) {
            if(checkNullEmpty(defStatus) && checkNullEmpty(tempStatus)){
                switch (type) {
                    case 'i':
                        $scope.inv.ism = false;
                        break;
                    case 'r':
                        $scope.inv.rsm = false;
                        break;
                    case 'p':
                        $scope.inv.psm = false;
                        break;
                    case 'w':
                        $scope.inv.wsm = false;
                        break;
                    case 't':
                        $scope.inv.tsm = false;
                        break;
                }
            }
        }
        $scope.getInventoryConfiguration();
        $scope.setInventoryConfiguration = function () {
            $scope.inv.enTgs = [];
            $scope.inv.usrTgs = [];
            if(!$scope.inv.eidb) {
                $scope.inv.eTgs = [];
            }
            if($scope.inv.irc) {
                if(checkNullEmpty($scope.inv.ersnsObj)) {
                    $scope.showWarning($scope.resourceBundle['config.select.reason.codes']);
                    return;
                }
            }
            if($scope.inv.etdx){
                if(checkNullEmpty($scope.inv.aname) && checkNullEmpty($scope.inv.uTgs)){
                    $scope.showWarning($scope.resourceBundle['config.select.user.tags']);
                    return;
                }
            }
            if($scope.inv.crc == 1 && (checkNullEmpty($scope.inv.minhpccr) || checkNullEmpty($scope.inv.maxhpccr))) {
                $scope.showWarning($scope.resourceBundle['enter.histperiod']);
                return;
            }
            $scope.loading = true;
            $scope.showLoading();
            $scope.getFiltered();
            if(checkNotNullEmpty($scope.inv.imt)){
                var temp = [];
                for(var i=0;i<$scope.inv.imt.length;i++){
                    if(checkNotNullEmpty($scope.inv.imt[i].mtg)){
                        temp.push($scope.inv.imt[i]);
                    }
                }
                $scope.inv.imt=temp;
                if(temp.length == 0)
                    $scope.inv.cimt=false;
            }
            if(checkNotNullEmpty($scope.inv.rmt)){
                var temp = [];
                for(var i=0;i<$scope.inv.rmt.length;i++){
                    if(checkNotNullEmpty($scope.inv.rmt[i].mtg)){
                        temp.push($scope.inv.rmt[i]);
                    }
                }
                $scope.inv.rmt=temp;
                if(temp.length == 0)
                    $scope.inv.crmt=false;
            }
            if(checkNotNullEmpty($scope.inv.smt)){
                var temp = [];
                for(var i=0;i<$scope.inv.smt.length;i++){
                    if(checkNotNullEmpty($scope.inv.smt[i].mtg)){
                        temp.push($scope.inv.smt[i]);
                    }
                }
                $scope.inv.smt=temp;
                if(temp.length == 0)
                    $scope.inv.csmt=false;
            }
            if(checkNotNullEmpty($scope.inv.tmt)){
                var temp = [];
                for(var i=0;i<$scope.inv.tmt.length;i++){
                    if(checkNotNullEmpty($scope.inv.tmt[i].mtg)){
                        temp.push($scope.inv.tmt[i]);
                    }
                }
                $scope.inv.tmt=temp;
                if(temp.length == 0)
                    $scope.inv.ctmt=false;
            }
            if(checkNotNullEmpty($scope.inv.dmt)){
                var temp = [];
                for(var i=0;i<$scope.inv.dmt.length;i++){
                    if(checkNotNullEmpty($scope.inv.dmt[i].mtg)){
                        temp.push($scope.inv.dmt[i]);
                    }
                }
                $scope.inv.dmt=temp;
                if(temp.length == 0)
                    $scope.inv.cdmt=false;
            }
            $scope.inv.ersns = [];
            if($scope.inv.irc && checkNotNullEmpty($scope.inv.ersnsObj)){
                for (var i = 0; i < $scope.inv.ersnsObj.length; i++) {
                    $scope.inv.ersns.push($scope.inv.ersnsObj[i].text);
                }
            }
            $scope.updateTags();
            domainCfgService.setInventoryCfg($scope.inv).then(function (data) {
                $scope.showSuccess(data.data);
            }).catch(function error(msg) {
                $scope.showErrorMsg(msg, true);
            }).finally(function (){
                $scope.loading = false;
                $scope.getInventoryConfiguration();
                $scope.refreshDomainConfig();
                $scope.hideLoading();
            });
        };
        $scope.setMtg = function (newVal) {
            if(checkNotNullEmpty(newVal)){
                $scope.addRows('i');
                var idx = $scope.iMatTags.indexOf(newVal);
                if(checkNotNullEmpty(idx)){
                    $scope.iMatTags[idx].hide = false;
                }
                $scope.iMatTags.splice(idx,1);
            }
        };
        $scope.setRMtg= function (newVal) {
            if(checkNotNullEmpty(newVal)){
                $scope.addRows('r');
                var idx = $scope.rMatTags.indexOf(newVal);
                if(checkNotNullEmpty(idx)){
                    $scope.rMatTags[idx].hide = false;
                }
                $scope.rMatTags.splice(idx,1);
            }
        };
        $scope.setTMtg = function (newVal) {
            if(checkNotNullEmpty(newVal)){
                $scope.addRows('t');
                var idx = $scope.tMatTags.indexOf(newVal);
                if(checkNotNullEmpty(idx)){
                    $scope.tMatTags[idx].hide = false;
                }
                $scope.tMatTags.splice(idx,1);
            }
        };
        $scope.setSMtg = function (newVal) {
            if(checkNotNullEmpty(newVal)){
                $scope.addRows('s');
                var idx = $scope.sMatTags.indexOf(newVal);
                if(checkNotNullEmpty(idx)){
                    $scope.sMatTags[idx].hide = false;
                }
                $scope.sMatTags.splice(idx,1);
            }
        };
        $scope.setDMtg = function (newVal) {
            if(checkNotNullEmpty(newVal)){
                $scope.addRows('d');
                var idx = $scope.dMatTags.indexOf(newVal);
                if(checkNotNullEmpty(idx)){
                    $scope.dMatTags[idx].hide = false;
                }
                $scope.dMatTags.splice(idx,1);
            }
        };
        $scope.getFiltered = function () {
            if ($scope.inv != null) {
                if($scope.inv.etdx){
                    $scope.inv.an = "";
                    if($scope.inv.aname != null && $scope.inv.aname.length > 0){
                        var an = [];
                        for(var i=0; i<$scope.inv.aname.length; i++){
                            an.push($scope.inv.aname[i].id);
                        }
                        if(checkNotNullEmpty(an))
                            $scope.inv.an = an.join();
                    }
                }else{
                    $scope.inv.an = "";
                }
            }
            if ($scope.inv.co == $scope.forecast[1].value) {
                $scope.inv.dooq = false;
            }
        };
        $scope.tabContent = function(type){
            angular.forEach(ty, function(value,key) {
                if(key === type)
                    ty[key] = true;
                else
                    ty[key] = false;
            });
            $scope.transType = ty;
        };
        $scope.StatusContent = function(type){
            angular.forEach(ms, function(value,key) {
                if(key === type)
                    ms[key] = true;
                else
                    ms[key] = false;
            });
            $scope.matStatus = ms;
        };

        $scope.ActualTransContent = function(type){
            angular.forEach(atdc, function(value,key) {
                if(key === type)
                    atdc[key] = true;
                else
                    atdc[key] = false;
            });
            $scope.actualTransDate= atdc;
        };

        $scope.deleteRow = function (id,type) {
            if(type === 'iss'){
                var mIndex = $scope.inv.imt[id];
                if (checkNotNullEmpty(mIndex) && checkNotNullEmpty($scope.inv.imt[mIndex])) {
                    $scope.inv.imt[mIndex].hide = false;
                }
                $scope.inv.imt.splice(id, 1);
                $scope.exRow[id] = '';
                if(checkNullEmpty($scope.inv.imt))
                    $scope.addRows('i');
            }
            if(type === 'rec'){
                var mIndex = $scope.inv.rmt[id];
                if (checkNotNullEmpty(mIndex) && checkNotNullEmpty($scope.inv.rmt[mIndex])) {
                    $scope.inv.rmt[mIndex].hide = false;
                }
                $scope.inv.rmt.splice(id, 1);
                $scope.rxRow[id] = '';
                if(checkNullEmpty($scope.inv.rmt))
                    $scope.addRows('r');
            }
            if(type === 'st'){
                var mIndex = $scope.inv.smt[id];
                if (checkNotNullEmpty(mIndex) && checkNotNullEmpty($scope.inv.smt[mIndex])) {
                    $scope.inv.smt[mIndex].hide = false;
                }
                $scope.inv.smt.splice(id, 1);
                $scope.sxRow[id] = '';
                if(checkNullEmpty($scope.inv.smt))
                    $scope.addRows('s');
            }
            if(type === 'tr'){
                var mIndex = $scope.inv.tmt[id];
                if (checkNotNullEmpty(mIndex) && checkNotNullEmpty($scope.inv.tmt[mIndex])) {
                    $scope.inv.tmt[mIndex].hide = false;
                }
                $scope.inv.tmt.splice(id, 1);
                $scope.txRow[id] = '';
                if(checkNullEmpty($scope.inv.tmt))
                    $scope.addRows('t');
            }
            if(type === 'dc'){
                var mIndex = $scope.inv.dmt[id];
                if (checkNotNullEmpty(mIndex) && checkNotNullEmpty($scope.inv.dmt[mIndex])) {
                    $scope.inv.dmt[mIndex].hide = false;
                }
                $scope.inv.dmt.splice(id, 1);
                $scope.dxRow[id] = '';
                if(checkNullEmpty($scope.inv.dmt))
                    $scope.addRows('d');
            }


        };
        $scope.addRows = function(type){
            if(checkNotNullEmpty($scope.MatTags)){
                if(type == 'i' || type == 'a'){
                    if($scope.MatTags.length>=$scope.inv.imt.length)
                        $scope.inv.imt.push({mtg:"",rsn:""});
                }
                if(type == 'r' || type == 'a'){
                    if($scope.MatTags.length>=$scope.inv.rmt.length)
                        $scope.inv.rmt.push({mtg:"",rsn:""});
                }
                if(type == 's' || type == 'a'){
                    if($scope.MatTags.length>=$scope.inv.smt.length)
                        $scope.inv.smt.push({mtg:"",rsn:""});
                }
                if(type == 't' || type == 'a'){
                    if($scope.MatTags.length>=$scope.inv.tmt.length)
                        $scope.inv.tmt.push({mtg:"",rsn:""});
                }
                if(type == 'd' || type =='a'){
                    if($scope.MatTags.length>=$scope.inv.dmt.length)
                        $scope.inv.dmt.push({mtg:"",rsn:""});
                }
            }
        };

        $scope.retrieveFiltered = function(){
            if($scope.inv != null){
                if(checkNotNullEmpty($scope.inv.an)){
                    var an = [];
                    var names = $scope.inv.an.split(",");
                    for(var i=0 ;i<names.length;i++){
                        an.push({"id":names[i]});
                    }
                    $scope.inv.aname = an;
                }
            }
        };
        $scope.setMMUpdateFreq = function() {
            if($scope.inv.mmType == '1') {
                if ($scope.inv.crfreq == 'weekly' && $scope.inv.mmFreq == 'daily') {
                    $scope.inv.mmFreq = 'weekly';
                    return 'mmw';
                } else if ($scope.inv.crfreq == 'monthly' && ($scope.inv.mmFreq == 'daily' || $scope.inv.mmFreq == 'weekly')) {
                    $scope.inv.mmFreq = 'monthly';
                    return 'mmm';
                }
            }
        };
        $scope.isSelected = function (i) {
            for (t in $scope.inv.ersnsObj) {
                if (i == $scope.inv.ersnsObj[t]) {
                    return true;
                }
            }
            return false;
        };
        $scope.query = function (query) {
            var data = {results: []};
            var term = query.term.toLowerCase();
            var rsns = getExcludeReasons();
            for (var i in rsns) {
                var tag = rsns[i].toLowerCase();
                if (!$scope.isSelected(tag) && tag.indexOf(term) >= 0) {
                    data.results.push({'text': rsns[i], 'id': rsns[i]});
                }
            }
            /*if (!$scope.forceNoUdf && $scope.udf && !$scope.isSelected(term)) {
                term = term.replace(/,/g,"");
                data.results.push({'text': term, 'id': term})
            }*/
            query.callback(data);
        };

        function getExcludeReasons() {
            var rsns = [];
            var ind = 0;
            var r = $scope.inv.ri.split(",");
            for (var i in r) {
                if(checkNotNullEmpty(r[i])) {
                    rsns[ind++] = r[i];
                }
            }
            for (i in $scope.inv.imt) {
                r = $scope.inv.imt[i].rsn.split(",");
                for(var t in r) {
                    if(rsns.hasOwnProperty(r[t]) || checkNullEmpty(r[t])) {
                        continue;
                    }
                    rsns[ind++] = r[t];
                }
            }
            return rsns
        }
    }
]);
domainCfgControllers.controller('AccountingConfigurationController', ['$scope', 'domainCfgService',
    function ($scope, domainCfgService) {
        $scope.ac = {};
        $scope.ac.cl = 0.0;
        $scope.loading = false;
        $scope.getAccountingConfiguration = function () {
            $scope.loading = true;
            $scope.showLoading();
            domainCfgService.getAccountingCfg().then(function (data) {
                $scope.ac = data.data;
                domainCfgService.getGeneralCfg().then(function (data) {
                    $scope.cnf = data.data;
                }).catch(function err(msg){
                    $scope.showWarning($scope.resourceBundle['configuration.general.unavailable']);
                })
            }).catch(function error(msg) {
                $scope.showErrorMsg(msg, true);
            }).finally(function (){
                $scope.loading = false;
                $scope.hideLoading();
            });
        };
        $scope.getAccountingConfiguration();
        $scope.setAccountConfiguration = function () {
            if ($scope.ac != null) {
                if(checkNotNullEmpty($scope.ac.cl)){
                    if(isNaN($scope.ac.cl) ){
                        $scope.showWarning($scope.resourceBundle['credit.validation']);
                        $scope.ac.cl = 0.0;
                        return;
                    }
                }else{
                    $scope.ac.cl = 0.0;
                }
                domainCfgService.setAccountCfg($scope.ac).then(function (data) {
                    $scope.refreshDomainConfig();
                    $scope.showSuccess(data.data);
                }).catch(function error(msg) {
                    $scope.showErrorMsg(msg, true);
                }).finally(function (){
                    $scope.getAccountingConfiguration();
                });
            }
        }
    }
]);
domainCfgControllers.controller('TemperatureConfigurationController', ['$scope', 'domainCfgService', 'configService',
    function ($scope, domainCfgService, configService) {
        $scope.postdata = {};
        $scope.success = false;
        $scope.vendors = false;
        $scope.loading = false;
        $scope.invalidStatusUrl = false;
        $scope.configCommunicationChannel = [{id: 0, value: "SMS"}, {id: 1, value: "Internet"}, {id: 2, value:"Failover"}];
        $scope.config = {};
        $scope.dc = {};
        $scope.uVis = {};
        $scope.loadCounter = 0;
        $scope.asset = {"sns": ['A', 'B', 'C', 'D', 'E']};
        $scope.sensorConfigOverrideKeys = [{grp:'comm', key: 'tmpNotify'}, {grp:'comm', key: 'incExcNotify'}, {grp:'comm', key: 'statsNotify'},
            {grp:'comm', key: 'devAlrmsNotify'}, {grp:'comm', key: 'tmpAlrmsNotify'},
            {grp:'comm', key: 'samplingInt'}, {grp:'comm', key: 'pushInt'},
            {grp:'highAlarm', key: 'temp'}, {grp:'highAlarm', key: 'dur'},
            {grp:'lowAlarm', key: 'temp'}, {grp:'lowAlarm', key: 'dur'},
            {grp:'highWarn', key: 'temp'}, {grp:'highWarn', key: 'dur'},
            {grp:'lowWarn', key: 'temp'}, {grp:'lowWarn', key: 'dur'},
            {grp:'notf', key: 'dur'}, {grp:'notf', key: 'num'}];
        $scope.currentAssetId = 1;

        LanguageController.call(this, $scope, configService);
        TimezonesControllerWithOffset.call(this, $scope, configService);
        LocationController.call(this, $scope, configService);

        $scope.updateSensorConfig = function(){
            //Updating device configuration for multi sensor devices
            $scope.sensorConfig = {};
            if(checkNotNullEmpty(checkNotNullEmpty($scope.config))){
                angular.forEach($scope.asset.sns,function(sensor, index){
                    var isConfigAvailable = false;
                    if(checkNotNullEmpty($scope.config.sensors) && $scope.config.sensors.length > 0){
                        angular.forEach($scope.config.sensors, function(sensorConfig){
                            if(sensorConfig.sId == sensor){
                                isConfigAvailable = true;
                                var tmpSensorConfig = angular.copy(sensorConfig);
                                tmpSensorConfig.useDefault = false;
                                angular.forEach($scope.sensorConfigOverrideKeys, function(value){
                                    if(checkNullEmpty(tmpSensorConfig[value.grp])){
                                        tmpSensorConfig[value.grp] = {};
                                        tmpSensorConfig[value.grp][value.key] = "";
                                    }
                                    if(checkNullEmpty(tmpSensorConfig[value.grp][value.key]) && checkStrictNotNullEmpty($scope.config[value.grp][value.key])){
                                        tmpSensorConfig[value.grp][value.key] = $scope.config[value.grp][value.key];
                                    }
                                });
                                $scope.sensorConfig[sensor] = tmpSensorConfig;
                            }
                        });
                    }

                    if(!isConfigAvailable){
                        var tmpSensorConfig = {sId: sensor, useDefault: true};
                        angular.forEach($scope.sensorConfigOverrideKeys, function(value){
                            if(checkNullEmpty(tmpSensorConfig[value.grp])){
                                tmpSensorConfig[value.grp] = {};
                                tmpSensorConfig[value.grp][value.key] = "";
                            }
                            if(checkNotNullEmpty($scope.config[value.grp]) && checkNotNullEmpty($scope.config[value.grp][value.key])){
                                tmpSensorConfig[value.grp][value.key] = $scope.config[value.grp][value.key];
                            }
                        });
                        $scope.sensorConfig[sensor] = tmpSensorConfig;
                    }
                    if(index == 0){
                        $scope.currentSns = sensor;
                        $scope.currentSnsConfig = $scope.sensorConfig[$scope.currentSns];
                    }
                });
            }
        };

        $scope.resetCurrentSensorConfig = function(){
            if($scope.currentSnsConfig.useDefault){
                var tmpSensorConfig = {sId: $scope.currentSns, useDefault: true};
                angular.forEach($scope.sensorConfigOverrideKeys, function(value){
                    if(checkNullEmpty(tmpSensorConfig[value.grp])){
                        tmpSensorConfig[value.grp] = {};
                        tmpSensorConfig[value.grp][value.key] = "";
                    }
                    if(checkNotNullEmpty($scope.config[value.grp]) && checkNotNullEmpty($scope.config[value.grp][value.key])){
                        tmpSensorConfig[value.grp][value.key] = $scope.config[value.grp][value.key];
                    }
                });
                $scope.sensorConfig[$scope.currentSns] = tmpSensorConfig;
                $scope.currentSnsConfig = tmpSensorConfig;
            }
        };

        $scope.setCurrentSensorConfig = function(value){
            $scope.sensorConfig[$scope.currentSns] = $scope.currentSnsConfig;
            $scope.currentSns = value;
            $scope.currentSnsConfig = $scope.sensorConfig[$scope.currentSns];
        };

        $scope.constructSensorConfig = function(){
            $scope.config.sensors = [];
            $scope.sensorConfig[$scope.currentSns] = $scope.currentSnsConfig;
            if(checkNotNullEmpty($scope.sensorConfig)){
                angular.forEach($scope.sensorConfig, function(sensor){
                    if(!sensor.useDefault) {
                        var tmpSensorConfig = {sId: sensor.sId};
                        angular.forEach($scope.sensorConfigOverrideKeys, function(value){
                            if(checkNotNullEmpty(sensor[value.grp][value.key])
                                && $scope.config[value.grp][value.key] != undefined
                                && sensor[value.grp][value.key] != $scope.config[value.grp][value.key]){
                                if(checkNullEmpty(tmpSensorConfig[value.grp])){
                                    tmpSensorConfig[value.grp] = {};
                                }
                                tmpSensorConfig[value.grp][value.key] = sensor[value.grp][value.key];
                            }
                        });
                        $scope.config.sensors.push(tmpSensorConfig);
                    }
                });
            }
        };

        $scope.getVendorNames = function () {
            $scope.loading = true;
            $scope.showLoading();
            domainCfgService.getAssetCfg().then(function (data) {
                if(checkNotNullEmpty(data.data)){
                    $scope.postdata = data.data;
                    $scope.config = $scope.postdata.config;
                    $scope.config.comm.samplingInt = $scope.config.comm.samplingInt == 0 ? "" : $scope.config.comm.samplingInt;
                    $scope.config.comm.pushInt = $scope.config.comm.pushInt == 0 ? "" : $scope.config.comm.pushInt;
                    $scope.config.highAlarm.dur = $scope.config.highAlarm.dur == 0 ? "" : $scope.config.highAlarm.dur;
                    $scope.config.lowAlarm.dur = $scope.config.lowAlarm.dur == 0 ? "" : $scope.config.lowAlarm.dur;
                    if($scope.postdata != null){
                        $scope.vendors = true;
                    }
                    if(checkNotNullEmpty($scope.config.comm.usrPhones)){
                        $scope.config.comm.usrPhones = $scope.config.comm.usrPhones.toString();
                    }
                    $scope.updateSensorConfig();
                }else{
                    $scope.tscNotConfigured = true;
                }
            }).catch(function error(msg) {
                $scope.showErrorMsg(msg, true);
            }).finally(function (){
                $scope.loading = false;
                $scope.hideLoading();
            });
        };
        $scope.getVendorNames();
        $scope.setTemperatureConfiguration = function () {
            if($scope.postdata.enable != 0){
                var isValid = true;
                if($scope.postdata.assets){
                    for(var assetItem in $scope.postdata.assets){
                        if($scope.postdata.assets[assetItem].mcs){
                            var selectedCount = 0;
                            for(var manuItem in $scope.postdata.assets[assetItem].mcs){
                                var manu = $scope.postdata.assets[assetItem].mcs[manuItem];
                                if(manu.iC){
                                    selectedCount++;
                                    if(checkNotNullEmpty(manu.model) && $scope.postdata.assets[assetItem].at == 1){
                                        var modelSelected = 0;
                                        for(var modelItem in manu.model){
                                            var model = manu.model[modelItem];

                                            if(model.iC){
                                                modelSelected ++;

                                                if(checkNullEmpty(model.dS)){
                                                    $scope.showWarning($scope.resourceBundle['pleaseselect'] +  ' ' + $scope.resourceBundle['default'] + ' ' + $scope.resourceBundle['sensor.for'] + ' "' + $scope.postdata.assets[assetItem].an + '" - ' + model.name + '(' + manu.name + ')');
                                                    return;
                                                }
                                            }
                                        }

                                        if(modelSelected == 0){
                                            $scope.showWarning($scope.resourceBundle['pleaseselect'] +  ' ' + $scope.resourceBundle['asset.model.for'] + ': "' + $scope.postdata.assets[assetItem].an + '" - ' + manu.name);
                                            return;
                                        }
                                    }
                                }
                            }
                            if(selectedCount == 0){
                                $scope.showWarning($scope.resourceBundle['device.select'] + ' "' + $scope.postdata.assets[assetItem].an + '"');
                                return;
                            }
                        }

                        if($scope.postdata.assets[assetItem].at == 2){
                            if(checkNullEmpty($scope.postdata.assets[assetItem].dMp)){
                                $scope.showWarning($scope.resourceBundle['pleaseselect'] +  ' ' + $scope.resourceBundle['default'] + ' ' + $scope.resourceBundle['monitoring.point.for'] + ' "' + $scope.postdata.assets[assetItem].an + '"');
                                return;
                            }
                        }
                    }
                }
            }
            $scope.loading = true;
            $scope.showLoading();
            if($scope.postdata.enable > 0){
                $scope.constructSensorConfig();
            }

            if (checkNotNullEmpty($scope.config)
                && checkNotNullEmpty($scope.config.comm)){
                if(checkNotNullEmpty($scope.config.comm.usrPhones)) {
                    if(!angular.isArray($scope.config.comm.usrPhones)){
                        var usrPhones = $scope.config.comm.usrPhones.split(",");
                        $scope.config.comm.usrPhones = [];
                        for(var i = 0; i < usrPhones.length ; i++){
                            if(checkNotNullEmpty(usrPhones[i].trim())){
                                $scope.config.comm.usrPhones.push(usrPhones[i]);
                            }
                        }
                    }
                }else{
                    $scope.config.comm.usrPhones = [];
                }
                $scope.postdata.config = $scope.config;
            }

            domainCfgService.setAssetCfg($scope.postdata).then(function (data) {
                $scope.refreshDomainConfig();
                if(checkNotNullEmpty($scope.config.comm.usrPhones)){
                    $scope.config.comm.usrPhones = $scope.config.comm.usrPhones.toString();
                }
                $scope.success = true;
                $scope.showSuccess(data.data);
            }).catch(function error(msg) {
                $scope.showErrorMsg(msg, true);
            }).finally(function () {
                $scope.loading = false;
                $scope.getVendorNames();
                $scope.hideLoading();
                $scope.uVis = {};
            });
        };

        $scope.setDomainDefault = function () {
            $scope.loading = true;
            $scope.showLoading();
            domainCfgService.get().then(function (data) {
                $scope.dc = data.data;
            }).catch(function error(msg) {
                $scope.showErrorMsg(msg);
            }).finally(function () {
                $scope.loading = false;
                $scope.hideLoading();
            });
        };

        $scope.finished = function () {
            $scope.loadCounter++;
            if ($scope.loadCounter == 4) {
                $scope.setDomainDefault();
            }
        };

        $scope.selectAll = function (newval, selectedList) {
            for (var i in selectedList) {
                selectedList[i].iC = newval;
            }
        };

        $scope.setUvisited = function(){
            $scope.uVis.commChnl = true;
            $scope.uVis.tmpUrl = true;
            $scope.uVis.alrmUrl = true;
            $scope.uVis.smsGyPh = true;
            $scope.uVis.sInt = true;
            $scope.uVis.pInt = true;
            $scope.uVis.highAlarmTemp = true;
            $scope.uVis.highAlarmDur = true;
            $scope.uVis.lowAlarmTemp = true;
            $scope.uVis.lowAlarmDur = true;
            $scope.uVis.statsUrl = false;

            if (checkNotNullEmpty($scope.config)
                && checkNotNullEmpty($scope.config.comm)){
                if($scope.config.comm.statsNotify && checkNullEmpty($scope.config.comm.statsUrl)){
                    $scope.uVis.statsUrl = true;
                }
            }
        };

        $scope.showValidationError = function(){
            $scope.showWarning($scope.resourceBundle['mandatory.fields']);
            return false;
        };

        $scope.tabContent = function(assetId){
            $scope.currentAssetId = assetId;
        };

        $scope.checkFormValidity = function() {
            $scope.invalidStatusUrl = false;
            $scope.sensorMsg = "";
            $scope.required = false;
            var invalidSensorMsg = [];
            $scope.sensorConfigEnabled = false;
            for(item in $scope.sensorConfig) {
                if($scope.sensorConfig[item].comm.statsNotify) {
                    invalidSensorMsg.push(item);
                }
            }
            if(invalidSensorMsg.length > 0) {
                $scope.sensorConfigEnabled = true;
                $scope.sensorMsg = "Enable stats push is enabled in sensor: " + invalidSensorMsg;

            }
            if($scope.config.comm.statsNotify || $scope.sensorConfigEnabled) {
                if(checkNullEmpty($scope.config.comm.statsUrl)) {
                    $scope.invalidStatusUrl = true;
                    $scope.required = true;
                }
            }
            return $scope.invalidStatusUrl;
        }
    }
]);
domainCfgControllers.controller('TagsConfigurationController', ['$scope', 'domainCfgService',
    function ($scope, domainCfgService) {
        $scope.tags = {};
        $scope.mtags = {mt:[],rt:[],ot:[],et:[],ut:[]};
        $scope.types = ["et","mt","rt","ot","ut"];
        $scope.loading = false;
        $scope.exRow = [];
        $scope.getTags = function () {
            $scope.loading = true;
            $scope.showLoading();
            domainCfgService.getTagsCfg().then(function (data) {
                $scope.tags = data.data;
                $scope.constructModelTags($scope.tags);
            }).catch(function error(msg) {
                $scope.showErrorMsg(msg, true);
            }).finally(function(){
                $scope.getEntityTagsCfg($scope.tags);
                $scope.loading = false;
                $scope.hideLoading();
            });
        };
        $scope.getTags();

        $scope.constructModelTags = function(tags){
            $scope.mtags = {mt:[],rt:[],ot:[],et:[],ut:[]};
            for(var type in $scope.types){
                var lTags = tags[$scope.types[type]];
                for(var tag in lTags){
                    $scope.mtags[$scope.types[type]].push({text: lTags[tag],id:lTags[tag]});
                }
            }
        };

        $scope.updateTagsFromModel = function(){
            for(var type in $scope.types){
                var lTags = $scope.mtags[$scope.types[type]];
                $scope.tags[$scope.types[type]] = [];
                for(var tag in lTags){
                    $scope.tags[$scope.types[type]].push(lTags[tag].text);
                }
            }
        };

        $scope.cleanTagRanks = function() {
            var temp = [];
            if(checkNotNullEmpty($scope.tags.etr)){
                for(var i=0;i<$scope.tags.etr.length;i++){
                    if(checkNotNullEmpty($scope.tags.etr[i].etg)){
                        temp.push($scope.tags.etr[i]);
                    }
                }
            }
            $scope.tags.etr=temp;
        };

        $scope.setTags = function () {
            $scope.updateTagsFromModel();
            $scope.loading = true;
            $scope.showLoading();
            $scope.cleanTagRanks();
            domainCfgService.setTagsCfg($scope.tags).then(function (data) {
                $scope.postdata = data.data;
                $scope.showSuccess(data.data);
            }).catch(function error(msg) {
                $scope.showErrorMsg(msg, true);
            }).finally(function (){
                $scope.loading = false;
                $scope.getTags();
                $scope.hideLoading();
            });
        };
        $scope.selectAll = function (newval) {
            for (var i = 0; i < $scope.postdata.cfg.length; i++) {
                if (newval) {
                    $scope.postdata.cfg_val[i] = $scope.postdata.vn_val[i];
                } else {
                    $scope.postdata.cfg_val[i] = "";
                }
            }
        };

        $scope.setEtg = function (newVal) {
            if(checkNotNullEmpty(newVal)){
                $scope.addTagRankRows();
                var idx = $scope.eTags.indexOf(newVal);
                if(checkNotNullEmpty(idx)){
                    $scope.eTags[idx].hide = false;
                }
                $scope.eTags.splice(idx,1);
            }
        };

        $scope.getEntityTagsCfg = function(tags){
            domainCfgService.getEntityTagsCfg().then(function (data){
                $scope.entTags = data.data.tags;
                $scope.eTags=angular.copy($scope.entTags);
                if(checkNotNullEmpty(tags) && checkNotNullEmpty(tags.etr)){
                    for(var i=0;i<tags.etr.length;i++){
                        var idx = $scope.eTags.indexOf(tags.etr[i].etg);
                        if(checkNotNullEmpty(idx) && checkNotNullEmpty($scope.eTags[idx])){
                            $scope.eTags[idx].hide = false;
                            $scope.eTags.splice(idx,1);
                        }
                    }
                }
            }).catch(function error(msg) {
                $scope.showErrorMsg(msg);
            }).finally(function(){
                $scope.addTagRankRows();
            });
        };

        $scope.deleteRow = function (id) {
                var mIndex = $scope.tags.etr[id];
                if (checkNotNullEmpty(mIndex) && checkNotNullEmpty($scope.tags.etr[mIndex])) {
                    $scope.tags.etr[mIndex].hide = false;
                }
                $scope.tags.etr.splice(id, 1);
                $scope.exRow[id] = '';
                if(checkNullEmpty($scope.tags.etr)) {
                    $scope.addRows();
                }
        };

        $scope.addTagRankRows = function(){
            if(checkNotNullEmpty($scope.entTags)){
                if($scope.entTags.length>=$scope.tags.etr.length)
                    $scope.tags.etr.push({etg:"",rnk:""});
            }
        };
    }
]);
domainCfgControllers.controller('OrdersConfigurationController', ['$scope', 'domainCfgService', 'requestContext', 'entityService', 'configService',
    function ($scope, domainCfgService, requestContext, entityService, configService) {
        $scope.orders = {};
        $scope.change = false;
        $scope.loading = false;
        $scope.dmntz = 'UTC';
        TimezonesControllerKVReversed.call(this, $scope, configService);

        $scope.getGeneralConfiguration = function() {
            $scope.loading = true;
            $scope.showLoading();

            domainCfgService.getGeneralCfg().then(function (data) {
                if (checkNotNullEmpty(data.data.tz)) {
                    $scope.dmntz = $scope.allTimezonesKVReversed[data.data.tz];
                }
            }).catch(function error(msg) {
                $scope.showErrorMsg(msg, true);
            }).finally(function () {
                $scope.loading = false;
                $scope.hideLoading();
            });
        };
        $scope.getGeneralConfiguration();

        function constructReasonsModel() {
            if (checkNotNullEmpty($scope.orders) && checkNotNullEmpty($scope.orders.orr)) {
                var reason = [];
                var a = $scope.orders.orr.split(',');
                for (var i in a) {
                    reason[i] = {"id": a[i], "text": a[i]};
                }
                $scope.orders.orr = reason;
            }
            if (checkNotNullEmpty($scope.orders) && checkNotNullEmpty($scope.orders.eqr)) {
                var reason = [];
                var a = $scope.orders.eqr.split(',');
                for (var i in a) {
                    reason[i] = {"id": a[i], "text": a[i]};
                }
                $scope.orders.eqr = reason;
            }
            if (checkNotNullEmpty($scope.orders) && checkNotNullEmpty($scope.orders.psr)) {
                var reason = [];
                var a = $scope.orders.psr.split(',');
                for (var i in a) {
                    reason[i] = {"id": a[i], "text": a[i]};
                }
                $scope.orders.psr = reason;
            }
            if (checkNotNullEmpty($scope.orders) && checkNotNullEmpty($scope.orders.pfr)) {
                var reason = [];
                var a = $scope.orders.pfr.split(',');
                for (var i in a) {
                    reason[i] = {"id": a[i], "text": a[i]};
                }
                $scope.orders.pfr = reason;
            }
            if (checkNotNullEmpty($scope.orders) && checkNotNullEmpty($scope.orders.cor)) {
                var reason = [];
                var a = $scope.orders.cor.split(',');
                for (var i in a) {
                    reason[i] = {"id": a[i], "text": a[i]};
                }
                $scope.orders.cor = reason;
            }


        }

        $scope.getOrders = function () {
            $scope.loading = true;
            $scope.showLoading();
            $scope.orders.uTgs = [];
            domainCfgService.getOrdersCfg().then(function (data) {
                $scope.orders = data.data;
                constructReasonsModel();
                if (checkNotNullEmpty(data.data.vid)) {
                    entityService.get(data.data.vid).then(function (data) {
                        if(checkNotNullEmpty(data.data.id)){
                            $scope.orders.defVendor = data.data;
                        }

                    }).catch(function error(msg) {
                        $scope.showErrorMsg(msg, true);
                    });
                }
                if(checkNullEmpty($scope.orders.et)){
                    $scope.orders.et = "00:00";
                }
                if(checkNotNullEmpty($scope.orders.an)){
                    var an = [];
                    var names = $scope.orders.an.split(",");
                    for(var i=0 ;i<names.length;i++){
                        an.push({"id":names[i]});
                    }
                    $scope.orders.aname = an;
                }
                $scope.demandBoardSelected();
                $scope.updateTags($scope.orders);
            }).catch(function error(msg) {
                $scope.showErrorMsg(msg, true);
            }).finally(function(){
                $scope.loading = false;
                $scope.hideLoading();
            });
        };

        function updateReasonsFromModel(localOrderCfg) {
            if(localOrderCfg.orr) {
                var csv = '';
                for (var i in localOrderCfg.orr) {
                    if (checkNotNullEmpty(csv)) {
                        csv += ",";
                    }
                    csv += localOrderCfg.orr[i].text;
                }
                localOrderCfg.orr = csv;
            }
            if(localOrderCfg.eqr) {
                var csv = '';
                for (var i in localOrderCfg.eqr) {
                    if (checkNotNullEmpty(csv)) {
                        csv += ",";
                    }
                    csv += localOrderCfg.eqr[i].text;
                }
                localOrderCfg.eqr = csv;
            }
            if(localOrderCfg.psr) {
                var csv = '';
                for (var i in localOrderCfg.psr) {
                    if (checkNotNullEmpty(csv)) {
                        csv += ",";
                    }
                    csv += localOrderCfg.psr[i].text;
                }
                localOrderCfg.psr = csv;
            }
            if(localOrderCfg.pfr) {
                var csv = '';
                for (var i in localOrderCfg.pfr) {
                    if (checkNotNullEmpty(csv)) {
                        csv += ",";
                    }
                    csv += localOrderCfg.pfr[i].text;
                }
                localOrderCfg.pfr = csv;
            }
            if(localOrderCfg.cor) {
                var csv = '';
                for (var i in localOrderCfg.cor) {
                    if (checkNotNullEmpty(csv)) {
                        csv += ",";
                    }
                    csv += localOrderCfg.cor[i].text;
                }
                localOrderCfg.cor = csv;
            }

        }
        $scope.updateTags = function(localOrderCfg) {
            if(checkNotNullEmpty(localOrderCfg.uTgs)) {
                localOrderCfg.usrTgs = [];
                for(var i=0; i<localOrderCfg.uTgs.length; i++) {
                    localOrderCfg.usrTgs.push(localOrderCfg.uTgs[i].text);
                }
            }else if(checkNotNullEmpty(localOrderCfg.usrTgs)) {
                localOrderCfg.uTgs = [];
                for(i=0; i<localOrderCfg.usrTgs.length; i++) {
                    localOrderCfg.uTgs.push({"id":localOrderCfg.usrTgs[i],"text" : localOrderCfg.usrTgs[i]});
                }
            }
        };
        $scope.disableMandatory = function(data,type) {
            $scope.orders.disable.type = false;
            if(checkNullEmpty(data)) {
                $scope.orders.disable.type = true;
            }
        };
        $scope.getOrders();
        $scope.setOrders = function () {
            var localOrderCfg = angular.copy($scope.orders);
            localOrderCfg.usrTgs = [];
            if(!localOrderCfg.eex) {
                localOrderCfg.uTgs = [];
            }
            $scope.updateTags(localOrderCfg);
            if(localOrderCfg.eex){
                if(checkNullEmpty(localOrderCfg.aname) && checkNullEmpty(localOrderCfg.uTgs)){
                    $scope.showWarning($scope.resourceBundle['config.select.user.tags']);
                    return;
                }
            }
            updateReasonsFromModel(localOrderCfg);
            if(checkNotNullEmpty(localOrderCfg.defVendor)) {
                localOrderCfg.vid = localOrderCfg.defVendor.id;
            }else{
                localOrderCfg.vid = "";
            }

            localOrderCfg.an = "";
            if(localOrderCfg.aname != null && localOrderCfg.aname.length > 0){
                var an = [];
                for(var i=0; i<localOrderCfg.aname.length; i++){
                    an.push(localOrderCfg.aname[i].id);
                }
                if(checkNotNullEmpty(an))
                    localOrderCfg.an = an.join();
            }
            $scope.loading = true;
            $scope.showLoading();
            domainCfgService.setOrdersCfg(localOrderCfg).then(function (data) {
                $scope.showSuccess(data.data);
                $scope.refreshDomainConfig();
            }).catch(function error(msg) {
                $scope.showErrorMsg(msg, true);
            }).finally(function (){
                $scope.loading = false;
                $scope.getOrders();
                $scope.hideLoading();
            });
        };
        $scope.demandBoardSelected = function () {
            $scope.change = $scope.orders.ip == "p";
        }
    }
]);

domainCfgControllers.controller('NotificationsExportController', ['$scope','exportService',
    function($scope,exportService){
    }
]);

domainCfgControllers.controller('NotifController',['$scope', function ($scope) {
    $scope.setuinfo = function (data) {
        $scope.uinfo = data;
    };
}]);

domainCfgControllers.controller('NotificationsConfigurationController', ['$scope', 'domainCfgService', 'requestContext', 'NOTIFICATIONS', 'ORDER',
    function ($scope, domainCfgService, requestContext, NOTIFICATIONS, ORDER) {
        var status = "Status changed";
        $scope.getInventoryConfiguration = function () {
            domainCfgService.getInventoryCfg().then(function (data) {
                $scope.inv = data.data;
                $scope.lazyLoad();
            }).catch(function error(msg) {
                $scope.showErrorMsg(msg, true);
            });
        };
        $scope.getInventoryConfiguration();
        $scope.getFiltered = function (reason) {
            $scope.list = [];
            if ($scope.inv != null) {
                if (checkNotNullEmpty(reason)) {
                    $scope.list = reason.split(",");
                }
            }
            return $scope.list;
        };
        function getFilteredCombined(df,estm) {
            var list = [];
            var elist = [];
            if($scope.inv != null){
                if(checkNotNullEmpty(df)){
                    list = df.split(",");
                }
                if(checkNotNullEmpty(estm)){
                    elist = estm.split(",");
                }
                list = getUnique(list.concat(elist));
            }
            return list;
        }
        // to filter the unique values from the list
        function getUnique (origArr) {
            var newArr = [], found;
            if(checkNotNullEmpty(origArr)) {
                origArr.forEach(function(d){
                    if(checkNotNullEmpty(d) ) {
                        found = newArr.some(function (dd) {
                            return d === dd;
                        });
                        if (!found) {
                            newArr.push(d);
                        }
                    }
                });
            }
            return newArr;
        }
        //get list of all reasons for material tags
        function getList(mTagReasons){
            var mRsn = [];
            if(checkNotNullEmpty(mTagReasons)){
                var x;
                var temp = [];
                for(x in mTagReasons){
                    temp = mTagReasons[x].rsn.split(",");
                    mRsn = mRsn.concat(temp);
                }
            }
            return mRsn.toString();
        }
        $scope.assetNotfType = "temperature";
        $scope.nof = {};
        $scope.dialog = "";
        $scope.notf = "";
        $scope.notif = {};
        $scope.notifd = {};
        $scope.shownotf = false;
        $scope.orders = NOTIFICATIONS.orders;
        $scope.shipments = NOTIFICATIONS.shipments;
        $scope.vars = NOTIFICATIONS.vars;
        $scope.inventory = NOTIFICATIONS.inventory;
        $scope.setup = NOTIFICATIONS.setup;
        $scope.temperature = NOTIFICATIONS.temperature;
        $scope.accounts = NOTIFICATIONS.accounts;
        $scope.ordersLabel = NOTIFICATIONS.ordersLabel.events;
        $scope.shipmentsLabel = NOTIFICATIONS.shipmentsLabel.events;
        $scope.inventoryLabel = NOTIFICATIONS.inventoryLabel.events;
        $scope.setupLabel = NOTIFICATIONS.setupLabel.events;
        $scope.temperatureLabel = NOTIFICATIONS.temperatureLabel.events;
        $scope.accountsLabel = NOTIFICATIONS.accountLabel.events;
        $scope.lazyLoad = function () {
            $scope.statusLabel = ORDER.notifStatusLabel;
            $scope.shipmentStatusLabel = ORDER.notifShipmentStatusLabel;
            $scope.stockCounted = getFilteredCombined($scope.inv.rs,getList($scope.inv.smt));
            $scope.stockIssued = getFilteredCombined($scope.inv.ri,getList($scope.inv.imt));
            $scope.stockTransferred = getFilteredCombined($scope.inv.rt,getList($scope.inv.tmt));
            $scope.stockReceived = getFilteredCombined($scope.inv.rr,getList($scope.inv.rmt));
            $scope.stockDiscarded = getFilteredCombined($scope.inv.rd,getList($scope.inv.dmt));

            $scope.stockCountedStatus = getFilteredCombined($scope.inv.pdf,$scope.inv.pestm);
            $scope.stockIssuedStatus = getFilteredCombined($scope.inv.idf,$scope.inv.iestm);
            $scope.stockTransferredStatus = getFilteredCombined($scope.inv.tdf,$scope.inv.testm);
            $scope.stockReceivedStatus = getFilteredCombined($scope.inv.rdf,$scope.inv.restm);
            $scope.stockDiscardedStatus = getFilteredCombined($scope.inv.wdf,$scope.inv.westm);
        };

        $scope.resetParams = function(){
            $scope.notif = {};
            $scope.prShow = false;
            $scope.prSelect = "";
            $scope.prefixName = "";
            $scope.placeHolder = "";
            $scope.title = "";
            $scope.eventType = "";
            $scope.notify = "";
            $scope.id = "";
            $scope.prefix = "";
            $scope.message = "";
            $scope.nid = "";
            $scope.nof.pr = "";
            $scope.nof.os = "";
            $scope.nof.id = "";
            $scope.nof.cot = "";
            $scope.nof.vnt = "";
            $scope.nof.adt = "";
            $scope.nof.ust = "";
            $scope.nof.mt = "";
            $scope.nof.uid = "";
            $scope.nof.nid = "";
            $scope.nof.co = "";
            $scope.nof.vn = "";
            $scope.nof.ad = "";
            $scope.nof.usr = "";
            $scope.nof.bb = "";
            $scope.nof.emt = "";
            $scope.nof.eet = "";
            $scope.nof.eot = "";
            $scope.nof.mst = "";
            $scope.nof.ist = "";
            $scope.prExtraSelect = "";
            $scope.extraParamsName = "";
            $scope.showSelection = false;
            $scope.showStatusSelection = false;
        };

        $scope.fetchEvent = function (notification) {
            $scope.toggle();
            if (notification == 'orders') {
                $scope.resetParams();
                for (var order in $scope.ordersLabel) {
                    if ($scope.ordersLabel[order].name == $scope.nof.or) {
                        $scope.title = $scope.ordersLabel[order].name;
                        $scope.eventType = 'orders';
                        $scope.notify = notification;
                        $scope.id = $scope.ordersLabel[order].id;
                        $scope.prefix = $scope.ordersLabel[order];
                        if ($scope.prefix.params != null) {
                            $scope.prefixName = $scope.prefix.params[0].prefix;
                            $scope.placeHolder = $scope.prefix.params[0].placeholder;
                            $scope.message = $scope.prefix.params[0].name;
                            $scope.alert = $scope.prefix.params[0].alert;
                            $scope.nid = $scope.prefix.params[0].id;
                            if ($scope.prefixName == 'status') {
                                $scope.prSelect = $scope.prefixName;
                            } else {
                                $scope.prShow = true;
                            }
                        }
                    }
                }
            } else if (notification == 'shipments'){
                $scope.resetParams();
                for(var shipment in $scope.shipmentsLabel) {
                    if($scope.shipmentsLabel[shipment].name == $scope.nof.ship) {
                        $scope.title = $scope.shipmentsLabel[shipment].name;
                        $scope.notify = notification;
                        $scope.id = $scope.shipmentsLabel[shipment].id;
                        $scope.prefix = $scope.shipmentsLabel[shipment];
                        if($scope.prefix.params != null){
                            $scope.prefixName = $scope.prefix.params[0].prefix;
                            $scope.placeHolder = $scope.prefix.params[0].placeholder;
                            $scope.message = $scope.prefix.params[0].name;
                            $scope.alert = $scope.prefix.params[0].alert;
                            $scope.nid = $scope.prefix.params[0].id;
                            if($scope.prefixName == 'status'){
                                $scope.prSelect = $scope.prefixName;
                            }else{
                                $scope.prShow = true;
                            }
                        }
                    }
                }
            } else if (notification == 'inventory') {
                $scope.resetParams();
                for (var inventory in $scope.inventoryLabel) {
                    if ($scope.inventoryLabel[inventory].name == $scope.nof.inv) {
                        $scope.title = $scope.inventoryLabel[inventory].name;
                        $scope.notify = notification;
                        $scope.id = $scope.inventoryLabel[inventory].id;
                        $scope.prefix = $scope.inventoryLabel[inventory];
                        if ($scope.prefix.params != null) {
                            $scope.prefixName = $scope.prefix.params[0].prefix;
                            $scope.placeHolder = $scope.prefix.params[0].placeholder;
                            $scope.message = $scope.prefix.params[0].name;
                            $scope.alert = $scope.prefix.params[0].alert;
                            $scope.nid = $scope.prefix.params[0].id;
                        }
                        if($scope.prefix.extraParams != null){
                            $scope.extraParamsName = $scope.prefix.extraParams.prefix;
                            $scope.extraParamsPlaceHolder = $scope.prefix.extraParams.placeholder;
                            $scope.extraParamsMessage = $scope.prefix.extraParams.name;
                            $scope.extraParamsAlert = $scope.prefix.extraParams.alert;
                            $scope.epid = $scope.prefix.extraParams.id;
                        }

                        if ($scope.prefixName != "" || $scope.extraParamsName != "") {
                            $scope.showSelection = false;
                            $scope.showStatusSelection = false;
                            if ($scope.prefixName == 'stockCounted' ||  $scope.extraParamsName == 'stockCounted') {
                                if(checkNotNullEmpty($scope.inv.rs) || checkNotNullEmpty($scope.inv.smt)){
                                    $scope.showSelection = true;
                                }
                                if(checkNotNullEmpty($scope.inv.pdf) || checkNotNullEmpty($scope.inv.pestm)){
                                    $scope.showStatusSelection = true;
                                }
                                $scope.prSelect = $scope.prefixName;
                                $scope.prExtraSelect = $scope.extraParamsName;
                                break;
                            } else if ($scope.prefixName == 'stockIssued' || $scope.extraParamsName == 'stockIssued') {
                                if(checkNotNullEmpty($scope.inv.ri) || checkNotNullEmpty($scope.inv.imt)){
                                    $scope.showSelection = true;
                                }
                                if(checkNotNullEmpty($scope.inv.idf) || checkNotNullEmpty($scope.inv.iestm)){
                                    $scope.showStatusSelection = true;
                                }
                                $scope.prSelect = $scope.prefixName;
                                $scope.prExtraSelect = $scope.extraParamsName;
                                break;
                            } else if ($scope.prefixName == 'stockTransferred' || $scope.extraParamsName == 'stockTransferred') {
                                if(checkNotNullEmpty($scope.inv.rt) ||checkNotNullEmpty($scope.inv.tmt) ){
                                    $scope.showSelection = true;
                                }
                                if(checkNotNullEmpty($scope.inv.tdf) || checkNotNullEmpty($scope.inv.testm)){
                                    $scope.showStatusSelection = true;
                                }
                                $scope.prSelect = $scope.prefixName;
                                $scope.prExtraSelect = $scope.extraParamsName;
                                break;
                            } else if ($scope.prefixName == 'stockReceived' || $scope.extraParamsName == 'stockReceived') {
                                if(checkNotNullEmpty($scope.inv.rr) || checkNotNullEmpty($scope.inv.rmt)){
                                    $scope.showSelection = true;
                                }if(checkNotNullEmpty($scope.inv.rdf) || checkNotNullEmpty($scope.inv.restm)){
                                    $scope.showStatusSelection = true;
                                }
                                $scope.prSelect = $scope.prefixName;
                                $scope.prExtraSelect = $scope.extraParamsName;
                                break;
                            } else if ($scope.prefixName == 'stockDiscarded' || $scope.extraParamsName == 'stockDiscarded') {
                                if(checkNotNullEmpty($scope.inv.rd) || checkNotNullEmpty($scope.inv.dmt)){
                                    $scope.showSelection = true;
                                }
                                if(checkNotNullEmpty($scope.inv.wdf) || checkNotNullEmpty($scope.inv.westm)){
                                    $scope.showStatusSelection = true;
                                }
                                $scope.prSelect = $scope.prefixName;
                                $scope.prExtraSelect = $scope.extraParamsName;
                                break;
                            } else {
                                $scope.prShow = true;
                                break;
                            }
                        }
                    }
                }
            } else if (notification == 'setup') {
                $scope.resetParams();
                for (var setup in $scope.setupLabel) {
                    if ($scope.setupLabel[setup].name == $scope.nof.st) {
                        $scope.title = $scope.setupLabel[setup].name;
                        $scope.notify = notification;
                        $scope.id = $scope.setupLabel[setup].id;
                        $scope.prefix = $scope.setupLabel[setup];
                        if ($scope.prefix.params != null) {
                            $scope.prefixName = $scope.prefix.params[0].prefix;
                            $scope.placeHolder = $scope.prefix.params[0].placeholder;
                            $scope.message = $scope.prefix.params[0].name;
                            $scope.alert = $scope.prefix.params[0].alert;
                            $scope.nid = $scope.prefix.params[0].id;
                            $scope.prShow = true;
                        }
                    }
                }
            } else if(notification == 'accounts'){
                $scope.resetParams();
                $scope.prShow = false;
                for(var account in $scope.accountsLabel){
                    $scope.title = $scope.accountsLabel[account].name;
                    $scope.notify = notification;
                    $scope.id = $scope.accountsLabel[account].id;
                    $scope.prefix = $scope.setupLabel[account];
                }
            } else if (notification == 'temperature') {
                $scope.resetParams();
                $scope.prShow = false;
                for (var temp in $scope.temperatureLabel) {
                    if ($scope.temperatureLabel[temp].name == $scope.nof.temp) {
                        $scope.title = $scope.temperatureLabel[temp].name;
                        $scope.notify = notification;
                        $scope.id = $scope.temperatureLabel[temp].id;
                        $scope.prefix = $scope.temperatureLabel[temp];
                        if ($scope.prefix.params != null) {
                            $scope.prefixName = $scope.prefix.params[0].prefix;
                            $scope.placeHolder = $scope.prefix.params[0].placeholder;
                            $scope.message = $scope.prefix.params[0].name;
                            $scope.alert = $scope.prefix.params[0].alert;
                            $scope.nid = $scope.prefix.params[0].id;
                            $scope.prShow = $scope.title != status;
                            $scope.showStatus = !$scope.prShow;
                        }
                    }
                }
            }
        };
        $scope.getTitle = function (nofObject) {
            var title = "";
            if (checkNotNullEmpty(nofObject.or)) {
                title = nofObject.or;
            } else if (checkNotNullEmpty(nofObject.ship)) {
                title = nofObject.ship;
            } else if (checkNotNullEmpty(nofObject.inv)) {
                title = nofObject.inv;
            } else if (checkNotNullEmpty(nofObject.st)) {
                title = nofObject.st;
            } else if (checkNotNullEmpty(nofObject.temp)) {
                title = nofObject.temp;
            } else if(checkNotNullEmpty(nofObject.acc)) {
                title = nofObject.acc;
            }
            return title;
        };
        $scope.getMessage = function (nofObject) {
            var message = "";
            if (checkNotNullEmpty(nofObject.os)) {
                message = nofObject.os;
            }
            return message;
        };
        $scope.getCount = function (nofObject, isStatus) {
            var count = "";
            if (checkNotNullEmpty(nofObject.pr)) {
                if(isStatus) {
                    count =  $scope.assetWSFilters[parseInt(nofObject.pr) + 1].dV;
                } else {
                    count = nofObject.pr;
                }
            } else if(checkNotNullEmpty(nofObject.os)){
                count = nofObject.os;
            }
            return count;
        };

        $scope.getExtraParamsMessage = function(nofObject){
            return checkNotNullEmpty(nofObject.ist)? nofObject.ist : "";
        };

        $scope.getExtraParamsSelect = function(nofObject){
            return checkNotNullEmpty(nofObject.mst)? nofObject.mst : "";
        };
        $scope.compareNotifications = function(){
            $scope.buildmessage = "";
            $scope.contin = true;
            var title = "";
            var message = "";
            var count = "";
            var extraParamsMessage = "";
            var extraParamsSelect= "";
            if(checkNotNullEmpty($scope.nof.or)){
                title = $scope.nof.or;
            }else if(checkNotNullEmpty($scope.nof.ship)){
                title = $scope.nof.ship;
            }else if(checkNotNullEmpty($scope.nof.inv)){
                title = $scope.nof.inv;
            }else if(checkNotNullEmpty($scope.nof.st)){
                title = $scope.nof.st;
            }else if(checkNotNullEmpty($scope.nof.acc)){
                title = $scope.nof.acc;
            }else if(checkNotNullEmpty($scope.nof.temp)){
                title = $scope.nof.temp;
            }
            if($scope.prefix.params != null){
                for(var j=0; j<$scope.prefix.params.length;j++){
                    message = $scope.prefix.params[j].name;
                }
            }
            if($scope.prefix.extraParams != null){
                extraParamsMessage = $scope.prefix.extraParams.name;
            }
            if(title == status && checkNotNullEmpty($scope.count)) {
                count = $scope.count;
            } else if(checkNotNullEmpty($scope.nof.pr)){
                count = $scope.nof.pr;
            }else if(checkNotNullEmpty($scope.nof.os)){
                count = $scope.nof.os;
            }
            if(checkNotNullEmpty($scope.nof.mst)){
                extraParamsSelect = $scope.nof.mst;
            }

            var buildmessage = $scope.buildNotify(title, message, count,extraParamsMessage,extraParamsSelect);
            $scope.buildmessage = buildmessage;
            if($scope.event != undefined && checkNotNullEmpty($scope.event)){
                for(var i=0; i<$scope.event.length; i++){
                    if(buildmessage.replace(/ /g,'').toLowerCase() == $scope.event[i].name.replace(/ /g,'').toLowerCase()){
                        $scope.contin =  false;
                        break;
                    }
                }
            }
        };
        $scope.getTableData = function(){
            $scope.tableData = "";
            if(checkNotNullEmpty($scope.nof.or) || ($scope.dialog == 'edit' && $scope.subview == 'orders')){
                $scope.tableData = $scope.orders;
            } else if(checkNotNullEmpty($scope.nof.ship) || ($scope.dialog == 'edit' && $scope.subview == 'shipments')){
                $scope.tableData = $scope.shipments;
            } else if(checkNotNullEmpty($scope.nof.inv) || ($scope.dialog == 'edit' && $scope.subview == 'inventory')){
                $scope.tableData = $scope.inventory;
            } else if(checkNotNullEmpty($scope.nof.st) || ($scope.dialog == 'edit' && $scope.subview == 'setup')){
                $scope.tableData = $scope.setup;
            } else if(checkNotNullEmpty($scope.nof.acc) || ($scope.dialog == 'edit' && $scope.subview == 'accounts')){
                $scope.tableData = $scope.accounts;
            }else if(checkNotNullEmpty($scope.nof.temp) || ($scope.dialog == 'edit' && $scope.subview == 'temperature')){
                $scope.tableData = $scope.temperature;
            }
        };

        $scope.setTagFlags = function(event){
            var id = "";
            for(var i=0; i<$scope.label.length; i++){
                if ((checkNotNullEmpty(event.st) && event.st == $scope.label[i].name) ||
                    (checkNotNullEmpty(event.or) && event.or == $scope.label[i].name) ||
                    (checkNotNullEmpty(event.inv) && event.inv == $scope.label[i].name) ||
                    (checkNotNullEmpty(event.acc) && event.acc == $scope.label[i].name) ||
                    (checkNotNullEmpty(event.temp) && event.temp == $scope.label[i].name)) {
                    id = $scope.label[i].id;
                    break;
                }
            }

            if(checkNotNullEmpty(event.or)){
                if(id.indexOf("Order:6") == -1){ // Under Orders, for all events other than NO_ACTIVITY, show entity and order tags
                    $scope.showEntityTags = true;
                    $scope.showOrderTags = true;
                }
            }else if(checkNotNullEmpty(event.ship)){
                $scope.showEntityTags = true;
                $scope.showOrderTags = true;
            }else if(checkNotNullEmpty(event.inv)){
                if(id.indexOf("Transaction:6") == -1 ){ // Under Inventory, for all events other than NO_ACTIVITY, show entity and material tags
                    $scope.showEntityTags = true;
                    $scope.showMaterialTags = true;
                }
            }else if(checkNotNullEmpty(event.st)){ // For all setup events for a User, tags should not be shown.
                if(id.indexOf("UserAccount:6") == -1 && id.indexOf("UserAccount:1") == -1 && id.indexOf("UserAccount:2") == -1 && id.indexOf("UserAccount:3") == -1 && id.indexOf("UserAccount:5") == -1 && id.indexOf("UserAccount:400") == -1) {
                    if (id.indexOf("Kiosk:1") != -1 || id.indexOf("Kiosk:2") != -1 || id.indexOf("Kiosk:3") != -1) { // For entity related events, show entity tags
                        $scope.showEntityTags = true;
                    } else if (id.indexOf("Material:1") != -1 || id.indexOf("Material:2") != -1 || id.indexOf("Material:3") != -1) {
                        $scope.showMaterialTags = true; // For material related events, show material tags
                    } else if (id.indexOf("Invntry:1") != -1 || id.indexOf("Invntry:3") != -1) {
                        $scope.showEntityTags = true; // For Inventory related events, show entity and material tags
                        $scope.showMaterialTags = true;
                    }
                }
            }else if(checkNotNullEmpty(event.acc)){
                $scope.showEntityTags = true;
                $scope.showOrderTags = true;
            } else if(checkNotNullEmpty(event.temp)){
                $scope.showEntityTags = true;
            }
        }
        $scope.showTags = function(event){
            $scope.showOrderTags = false;
            $scope.showEntityTags = false;
            $scope.showMaterialTags = false;
            if(checkNotNullEmpty(event.or)){
                $scope.label = $scope.ordersLabel;
                $scope.setTagFlags(event);
            } else if(checkNotNullEmpty(event.ship)){
                $scope.label = $scope.shipmentsLabel;
                $scope.setTagFlags(event);
            }else if(checkNotNullEmpty(event.inv)){
                $scope.label = $scope.inventoryLabel;
                $scope.setTagFlags(event);
            }else if(checkNotNullEmpty(event.st)){
                $scope.label = $scope.setupLabel;
                $scope.setTagFlags(event);
            }else if(checkNotNullEmpty(event.acc)){
                $scope.label = $scope.accountsLabel;
                $scope.setTagFlags(event);
            }else if(checkNotNullEmpty(event.temp)){
                $scope.label = $scope.temperatureLabel;
                $scope.setTagFlags(event);
            }
        };

        $scope.validateAndOpenAddDialog = function () {
            if (checkNotNullEmpty($scope.nof.or) || checkNotNullEmpty($scope.nof.ship) || checkNotNullEmpty($scope.nof.inv) || checkNotNullEmpty($scope.nof.st) || checkNotNullEmpty($scope.nof.mst) || checkNotNullEmpty($scope.nof.temp || checkNotNullEmpty($scope.nof.acc))) {
                $scope.getTableData();
                if ($scope.nof != null) {
                    var isStatus = $scope.nof.temp == status;
                    $scope.count = $scope.getCount($scope.nof, isStatus);
                    $scope.extraParamsSelect = $scope.getExtraParamsSelect($scope.nof);
                }
                $scope.compareNotifications();
                $scope.showTags($scope.nof);
                if($scope.contin){
                    $scope.showSubview = false;
                    $scope.resetNotif();
                    if ($scope.prefix.params != null) {
                        if(checkNotNullEmpty($scope.nof.temp)){
                            if(checkNotNullEmpty($scope.nof.pr)) {
                                if ($scope.nof.temp == "No data from device" && $scope.nof.pr == 0) {
                                    $scope.showWarning($scope.alert);
                                    return;
                                }
                            }
                        }
                        if (($scope.prShow && checkNotNullEmpty($scope.nof.pr) || checkNotNullEmpty($scope.prSelect) || checkNotNullEmpty($scope.prExtraSelect))
                            || $scope.showStatus) {
                            $scope.showSubview = true;
                        }else{
                            $scope.showWarning($scope.alert);
                        }
                    } else {
                        $scope.showSubview = true;
                    }

                    if ($scope.showSubview) {
                        if ($scope.subview == 'orders') {
                            $scope.dialog = "ordersDialog";
                        } else if ($scope.subview == 'shipments') {
                            $scope.dialog = "shipmentsDialog";
                        } else if ($scope.subview == 'inventory') {
                            $scope.dialog = "inventoryDialog";
                        } else if ($scope.subview == 'setup') {
                            $scope.dialog = "setupDialog";
                        } else if ($scope.subview == 'temperature') {
                            $scope.dialog = "temperatureDialog";
                        } else if($scope.subview == 'accounts') {
                            $scope.dialog = "accountsDialog";
                        }
                    }
                }else{
                    $scope.showWarning($scope.resourceBundle['event.exists'] + '\"' + $scope.buildmessage  + '\"' +  '. ' +  $scope.resourceBundle['event.edit']);
                }
            }else{
                $scope.showWarning($scope.resourceBundle['select.event']);
            }
        };
        $scope.resetNotif = function () {
            $scope.notif.co = false;
            $scope.notif.vn = false;
            $scope.notif.ad = false;
            $scope.notif.usr = false;
            $scope.notif.cr = false;
            $scope.notif.cot = "";
            $scope.notif.vnt = "";
            $scope.notif.adt = "";
            $scope.notif.crt = "";
            $scope.notif.ust = "";
            $scope.notif.uid = "";
            $scope.notif.mt = "";
            $scope.notif.bb = "";
            $scope.notif.nid = "";
            $scope.notif.uids = [];
            $scope.notif.emt = "";
            $scope.notif.eet = "";
            $scope.notif.eot = "";
            $scope.notif.emts = [];
            $scope.notif.eets = [];
            $scope.notif.eots = [];
            $scope.notif.usrTgs = "";
            $scope.notif.uTgs = [];
            $scope.notif.ist="";
            $scope.notif.mst="";
            $scope.notif.misd="";
        };
        $scope.toggle = function () {
            $scope.dialog = "";
            $scope.showSubview = false;
        };
        $scope.resetStatus = function (note) {
            $scope.toggle();
            $scope.dialog = "";
            if (note == 'status') {
                $scope.nof.pr = "";
            } else if (note == 'prefix') {
                $scope.nof.os = "";
                $scope.nof.mst ="";
            }
        };

        $scope.validate = function(notif){
            var noSubscriberTypesChecked = true;
            if(notif.bb){
                noSubscriberTypesChecked = false;
            }
            if(notif.co){
                noSubscriberTypesChecked = false;
                if(checkNullEmpty(notif.cot)){
                    $scope.showWarning($scope.resourceBundle['notifications.freqnotselected']+' '+$scope.resourceBundle['customers']);
                    return false;
                }
            }
            if(notif.vn){
                noSubscriberTypesChecked = false;
                if(checkNullEmpty(notif.vnt)){
                    $scope.showWarning($scope.resourceBundle['notifications.freqnotselected']+' '+$scope.resourceBundle['vendors']);
                    return false;
                }
            }
            if(notif.ad){
                noSubscriberTypesChecked = false;
                if(checkNullEmpty(notif.adt)){
                    $scope.showWarning($scope.resourceBundle['notifications.freqnotselected']+' '+$scope.resourceBundle['administrators']);
                    return false;
                }
            }
            if(notif.au){
                noSubscriberTypesChecked = false;
                if(checkNullEmpty(notif.aut)){
                    $scope.showWarning($scope.resourceBundle['notifications.freqnotselected']+' '+$scope.resourceBundle['asset']
                        + ' ' + $scope.resourceBundle['owners'].toLowerCase() + ' / ' + $scope.resourceBundle['asset.maintainers'].toLowerCase());
                    return false;
                }
            }
            if(notif.cr){
                noSubscriberTypesChecked = false;
                if(checkNullEmpty(notif.crt)){
                    $scope.showWarning($scope.resourceBundle['notifications.freqnotselected']+' '+ $scope.resourceBundle['creator']);
                    return false;
                }
            }
            if(notif.usr){
                noSubscriberTypesChecked = false;
                if(checkNullEmpty(notif.ust)){
                    $scope.showWarning($scope.resourceBundle['notifications.freqnotselected']+' '+$scope.resourceBundle['users']);
                    return false;
                }
                if(checkNullEmpty(notif.uids) && checkNullEmpty(notif.uTgs)){
                    $scope.showWarning($scope.resourceBundle['config.select.user.tags']);
                    return false;
                }
            }
            if(noSubscriberTypesChecked){
                $scope.showWarning($scope.resourceBundle['notifications.nouserselected']);
                return false;
            }
            if(checkNullEmpty(notif.mt)){
                $scope.showWarning($scope.resourceBundle['notifications.nomessagetemplate']);
                return false;
            }

            return true;
        };
        $scope.saveNotifications = function (action) {
            if(!$scope.notif.co && !$scope.notif.vn && !$scope.notif.ad && !$scope.notif.usr && !$scope.notif.bb && !$scope.notif.au && !$scope.notif.cr){
                $scope.showWarning($scope.resourceBundle['select.bulletinoruser']);
            }else{
                $scope.notif.add = true;
                if ($scope.notify == 'orders') {
                    $scope.notif.or = $scope.notify;
                }else if ($scope.notify == 'shipments') {
                    $scope.notif.ship = $scope.notify;
                } else if ($scope.notify == 'inventory') {
                    $scope.notif.inv = $scope.notify;
                } else if ($scope.notify == 'setup') {
                    $scope.notif.st = $scope.notify;
                } else if ($scope.notify == 'temperature') {
                    $scope.notif.temp = $scope.notify;
                } else if($scope.notify == 'accounts') {
                    $scope.notif.acc = $scope.notify;
                }
                if ($scope.notif != null) {
                    if (checkNotNullEmpty($scope.title)) {
                        $scope.notif.or = $scope.title;
                    }
                    var emt = "";
                    if(checkNotNullEmpty($scope.notif.emts)){
                        for(var i in $scope.notif.emts){
                            emt += $scope.notif.emts[i].id+",";
                        }
                        if(i>0){
                            emt = emt.slice(0,-1);
                        }
                    }
                    $scope.notif.emt = emt;
                    var eet = "";
                    if(checkNotNullEmpty($scope.notif.eets)){
                        for(var i in $scope.notif.eets){
                            eet += $scope.notif.eets[i].id+",";
                        }
                        if(i>0){
                            eet = eet.slice(0,-1);
                        }
                    }
                    $scope.notif.eet = eet;
                    var eot = "";
                    if(checkNotNullEmpty($scope.notif.eots)){
                        for(var i in $scope.notif.eots){
                            eot += $scope.notif.eots[i].id+",";
                        }
                        if(i>0){
                            eot = eot.slice(0,-1);
                        }
                    }
                    $scope.notif.eot = eot;

                    if(action == 'edit') {
                        $scope.nof.pr = $scope.notif.pr;
                    }

                    if (checkNotNullEmpty($scope.count)) {
                        $scope.notif.pr = $scope.notif.or == status ? $scope.nof.pr : $scope.count;
                    }
                    if (checkNotNullEmpty($scope.message)) {
                        $scope.notif.os = $scope.message;
                    }
                    if (checkNotNullEmpty($scope.id)) {
                        $scope.notif.id = $scope.id;
                    }
                    if (checkNotNullEmpty($scope.nid)) {
                        $scope.notif.nid = $scope.nid;
                    }
                    if(checkNotNullEmpty($scope.extraParamsMessage)) {
                        $scope.notif.ist = $scope.extraParamsMessage;
                    }
                    if(checkNotNullEmpty($scope.extraParamsSelect)){
                        $scope.notif.mst = $scope.extraParamsSelect;
                    }
                    if(checkNotNullEmpty($scope.notif.uids)){
                        var uid = "";
                        for(var i in $scope.notif.uids){
                            uid += $scope.notif.uids[i].id+",";
                        }
                        if(i>0){
                            uid = uid.slice(0,-1);
                        }

                        $scope.notif.uid = uid;
                    } else {
                        $scope.notif.uid = "";
                    }
                    if(action == 'edit'){
                        $scope.notif.add = false;
                    }
                    if(checkNotNullEmpty($scope.notif.uTgs)) {
                        var utgs = "";
                        for(var i in $scope.notif.uTgs) {
                            utgs += $scope.notif.uTgs[i].id+",";
                        }
                        if(i>0) {
                            utgs = utgs.slice(0,-1);
                        }
                        $scope.notif.usrTgs = utgs;
                    } else {
                        $scope.notif.usrTgs = "";
                        $scope.notif.add = false;
                    }
                }
                if ($scope.validate($scope.notif)) {
                    $scope.showLoading();
                    domainCfgService.setNotificationsCfg($scope.notif).then(function (data) {
                        $scope.getNotifications();
                        $scope.toggle();
                        $scope.showSuccess(data.data);
                        $scope.prShow = false;
                        $scope.prSelect = "";
                        $scope.prExtraSelect = "";
                        $scope.showSelection = false;
                        $scope.showStatusSelection = false;
                        $scope.showStatus = false;
                    }).catch(function error(msg) {
                        $scope.showErrorMsg(msg, true);
                    }).finally(function(){
                        $scope.hideLoading();
                    });
                }
            }
        };
        $scope.getKeys = function () {
            $scope.orderKeys = [];
            $scope.shipmentKeys = [];
            $scope.inventoryKeys = [];
            $scope.setupKeys = [];
            $scope.temperatureKeys = [];
            $scope.accountKeys = [];
            NOTIFICATIONS.ordersLabel.events.forEach(function (event) {
                $scope.orderKeys.push(event.id);
            });
            NOTIFICATIONS.shipmentsLabel.events.forEach(function (event) {
                $scope.shipmentKeys.push(event.id);
            });
            NOTIFICATIONS.inventoryLabel.events.forEach(function (event) {
                $scope.inventoryKeys.push(event.id);
            });
            NOTIFICATIONS.setupLabel.events.forEach(function (event) {
                $scope.setupKeys.push(event.id);
            });
            if($scope.assetNotfType == 'temperature'){
                NOTIFICATIONS.temperatureLabel.events.forEach(function (event) {
                    $scope.temperatureKeys.push(event.id);
                });
            }else if($scope.assetNotfType == 'assetAlarms'){
                NOTIFICATIONS.assetAlarmsLabel.events.forEach(function (event) {
                    $scope.temperatureKeys.push(event.id);
                });
            }
            NOTIFICATIONS.accountLabel.events.forEach(function (event){
                $scope.accountKeys.push(event.id);
            });
        };
        $scope.getNotify = function (nofObject) {
            if (nofObject != null) {
                var notif = "";
                var title = $scope.getTitle(nofObject);
                var message = $scope.getMessage(nofObject);
                var isStatus = nofObject.temp == status;
                var count = $scope.getCount(nofObject, isStatus);
                var extraParamsMessage = $scope.getExtraParamsMessage(nofObject);
                var extraParamsSelect = $scope.getExtraParamsSelect(nofObject);
                notif = title;
                if (checkNotNullEmpty(message)) {
                    notif += ' (' + message + ' = ' + count + ')';
                }
                if(checkNotNullEmpty(extraParamsMessage) && checkNullEmpty(nofObject.or) && checkNullEmpty(nofObject.ship)){
                    notif += ' (' + extraParamsMessage + ' = '+ extraParamsSelect + ')';
                }

                return notif;
            }
            return null;
        };

        $scope.buildNotify = function(title, message, count,extraParamsMessage,extraParamsSelect){
            var notif = "";
            if(checkNotNullEmpty(title)){
                notif = title;
                if(checkNotNullEmpty(message) && checkNotNullEmpty(count)){
                    notif += ' (' + message + ' = ' + count + ')';
                }
                if(checkNotNullEmpty(extraParamsMessage) &&  checkNotNullEmpty(extraParamsSelect)){
                    notif += '(' + extraParamsMessage + ' = ' + extraParamsSelect + ')';
                }
                return notif;
            }
            return null;
        };
        $scope.getNotifyTime = function (nofObject) {
            if (nofObject != null) {
                var notifTime = "";
                var notifPerson = "";
                var notifPeriod = "";
                var not = "";
                if (nofObject.co) {
                    notifPerson = $scope.resourceBundle['customers'];
                    notifTime = $scope.notifyTime(nofObject.cot);
                    notifPeriod = notifPerson + ' (' + notifTime + ')';
                    not = notifPeriod;
                }
                if (nofObject.vn) {
                    notifPerson = $scope.resourceBundle['vendors'];
                    notifTime = $scope.notifyTime(nofObject.vnt);
                    notifPeriod = notifPerson + ' (' + notifTime + ')';
                    if (checkNotNullEmpty(not)) {
                        not = not + ', ' + notifPeriod;
                    } else {
                        not = notifPeriod;
                    }
                }
                if (nofObject.cr) {
                    notifPerson = 'Creator';
                    notifTime = $scope.notifyTime(nofObject.crt);
                    notifPeriod = notifPerson + ' (' + notifTime + ')';
                    if (checkNotNullEmpty(not)) {
                        not = not + ', ' + notifPeriod;
                    } else {
                        not = notifPeriod;
                    }
                }
                if (nofObject.usr) {
                    notifPerson = "Users";
                    notifTime = $scope.notifyTime(nofObject.ust);
                    notifPeriod = notifPerson + ' (' + notifTime + ')';
                    if (checkNotNullEmpty(not)) {
                        not = not + ', ' + notifPeriod;
                    } else {
                        not = notifPeriod;
                    }
                }
                if (nofObject.ad) {
                    notifPerson = "Administrators";
                    notifTime = $scope.notifyTime(nofObject.adt);
                    notifPeriod = notifPerson + ' (' + notifTime + ')';
                    if (checkNotNullEmpty(not)) {
                        not = not + ', ' + notifPeriod;
                    } else {
                        not = notifPeriod;
                    }
                }
                if (nofObject.au) {
                    notifPerson = $scope.resourceBundle['asset'] + ' ' + $scope.resourceBundle['owners'].toLowerCase() + ' / ' + $scope.resourceBundle['asset.maintainers'].toLowerCase();
                    notifTime = $scope.notifyTime(nofObject.aut);
                    notifPeriod = notifPerson + ' (' + notifTime + ')';
                    if (checkNotNullEmpty(not)) {
                        not = not + ', ' + notifPeriod;
                    } else {
                        not = notifPeriod;
                    }
                }
                if(nofObject.bb) {
                    if(checkNotNullEmpty(not)){
                        not = not + ', ' + "Bulletin Board";
                    }else {
                        not = "Bulletin Board";
                    }
                }
                return not;
            }
            return "";
        };
        $scope.notifyTime = function (timeStamp) {
            if (checkNotNullEmpty(timeStamp)) {
                var time = "";
                if (timeStamp == '0') {
                    time = "SMS Immediately";
                } else if (timeStamp == '1') {
                    time = "Email Daily";
                } else if (timeStamp == '2') {
                    time = "Email Weekly";
                } else if (timeStamp == '3') {
                    time = "Email Monthly";
                }
                return time;
            }
            return "";
        };
        $scope.getEventNotifications = function (configKeys, notification) {
            if (notification != null) {
                $scope.shownotf = false;
                $scope.event = [];
                $scope.nfy = [];
                configKeys.forEach(function (key) {
                    $scope.nofObject = notification[key];
                    if (checkNotNullEmpty($scope.nofObject)) {
                        for (var i = 0; i < $scope.nofObject.length; i++) {
                            $scope.nofObjt = $scope.nofObject[i];
                            if ($scope.nofObjt != null) {
                                $scope.event.push({key: key, name: $scope.getNotify($scope.nofObjt)});
                                $scope.nfy.push($scope.getNotifyTime($scope.nofObjt));
                            }
                        }
                        $scope.shownotf = true;
                    }
                });
            }
        };
        $scope.getNotifications = function () {
            $scope.shownotf = false;
            $scope.loading = true;
            $scope.showLoading();
            $scope.getKeys();
            var notificationKey = $scope.assetNotfType == 'assetAlarms' ? $scope.assetNotfType : $scope.subview;
            domainCfgService.getNotificationsCfg(notificationKey).then(function (data) {
                $scope.nof = data.data.config;
                $scope.setuinfo(data.data);
                if ($scope.subview == 'orders') {
                    $scope.getEventNotifications($scope.orderKeys, $scope.nof);
                    $scope.notf = 'order';
                } else if ($scope.subview == 'shipments') {
                    $scope.getEventNotifications($scope.shipmentKeys, $scope.nof);
                    $scope.notf = 'shipment';
                } else if ($scope.subview == 'inventory') {
                    $scope.getEventNotifications($scope.inventoryKeys, $scope.nof);
                    $scope.notf = 'inventory';
                } else if ($scope.subview == 'setup') {
                    $scope.getEventNotifications($scope.setupKeys, $scope.nof);
                    $scope.notf = 'setup';
                } else if ($scope.subview == 'temperature') {
                    $scope.getEventNotifications($scope.temperatureKeys, $scope.nof);
                    $scope.notf = 'temperature';
                } else if ($scope.subview == 'accounts'){
                    $scope.getEventNotifications($scope.accountKeys, $scope.nof);
                    $scope.notf = 'accounts';
                }
            }).catch(function error(msg) {
                $scope.showErrorMsg(msg,true);
            }).finally(function(){
                $scope.loading = false;
                $scope.hideLoading();
            });
        };

        $scope.editNotif = function (notif, event) {
            if (checkNotNullEmpty(notif)) {
                for (var i = 0; i < notif.length; i++) {
                    var message = $scope.getNotify(notif[i]);
                    if (message == event) {
                        return notif[i];
                    }
                }
                return null;
            }
        };
        $scope.getObjectToDelete = function (nofObject, event) {
            if (nofObject != null) {
                for (var i = 0; i < nofObject.length; i++) {
                    var notif = $scope.getNotify(nofObject[i]);
                    if (notif == event) {
                        $scope.notifd = nofObject[i];
                        break;
                    }
                }
                return $scope.notifd;
            }
            return null;
        };
        $scope.editNotifications = function (msgkey, event) {
            $scope.eNotif = "";
            $scope.message = "";
            $scope.count = "";
            $scope.prShow = "";
            $scope.prSelect = "";
            $scope.extraParamsSelect = "";
            $scope.extraParamsMessage = "";
            $scope.id = msgkey;
            if ($scope.nof != null) {
                $scope.dialog = "edit";
                $scope.getKeys();
                $scope.getTableData();
                $scope.eNotif.uTgs = [];
                if ($scope.subview == 'orders') {
                    $scope.nof.or = "";
                    $scope.orderKeys.forEach(function (key) {
                        $scope.notif = $scope.nof[msgkey];
                        if (checkNullEmpty($scope.eNotif)) {
                            $scope.eNotif = $scope.editNotif($scope.notif, event);
                        }

                    });
                } else if ($scope.subview == 'shipments') {
                    $scope.nof.ship = "";
                    $scope.shipmentKeys.forEach(function (key) {
                        $scope.notif = $scope.nof[msgkey];
                        if (checkNullEmpty($scope.eNotif)) {
                            $scope.eNotif = $scope.editNotif($scope.notif, event);
                        }
                    });
                } else if ($scope.subview == 'inventory') {
                    $scope.nof.inv = "";
                    $scope.inventoryKeys.forEach(function (key) {
                        $scope.notif = $scope.nof[msgkey];
                        if (checkNullEmpty($scope.eNotif)) {
                            $scope.eNotif = $scope.editNotif($scope.notif, event);
                        }
                    });
                } else if ($scope.subview == 'setup') {
                    $scope.nof.st = "";
                    $scope.setupKeys.forEach(function (key) {
                        $scope.notif = $scope.nof[msgkey];
                        if (checkNullEmpty($scope.eNotif)) {
                            $scope.eNotif = $scope.editNotif($scope.notif, event);
                        }
                    });
                } else if ($scope.subview == 'temperature') {
                    $scope.nof.temp = "";
                    $scope.temperatureKeys.forEach(function (key) {
                        if(key == msgkey) {
                            $scope.notif = $scope.nof[msgkey];
                            if (checkNullEmpty($scope.eNotif)) {
                                $scope.eNotif = $scope.editNotif($scope.notif, event);
                            }
                        }
                    });
                } else if($scope.subview == 'accounts') {
                    $scope.nof.acc = "";
                    $scope.accountKeys.forEach(function (key){
                        $scope.notif = $scope.nof[msgkey];
                        if(checkNullEmpty($scope.eNotif)){
                            $scope.eNotif = $scope.editNotif($scope.notif, event);
                        }
                    });
                }
            }
            $scope.resetNotif();
            if ($scope.eNotif != null) {
                $scope.title = $scope.getTitle($scope.eNotif);
                $scope.message = $scope.getMessage($scope.eNotif);
                if (checkNotNullEmpty($scope.message)) {
                    var isStatus = $scope.title == status;
                    $scope.count = $scope.getCount($scope.eNotif, isStatus);
                }
                $scope.extraParamsMessage = $scope.getExtraParamsMessage($scope.eNotif);
                if(checkNotNullEmpty($scope.extraParamsMessage)){
                    $scope.extraParamsSelect = $scope.getExtraParamsSelect($scope.eNotif);
                }
                $scope.notif = $scope.eNotif;
                $scope.setUserIds($scope.notif);
                $scope.updateUserTags($scope.notif);
                $scope.setMaterialTagsToExclude($scope.notif);
                $scope.setEntityTagsToExclude($scope.notif);
                $scope.setOrderTagsToExclude($scope.notif);
                $scope.showTags($scope.eNotif);
            }
        };

        $scope.setUserIds = function(eventNotification){
            if(checkNotNullEmpty(eventNotification.uid)){
                eventNotification.uids = [];
                eventNotification.uid.split(",").forEach(function (key) {
                    eventNotification.uids.push({"id":key});
                });
            }
        };

        $scope.setMaterialTagsToExclude = function(eventNotification){
            if(checkNotNullEmpty(eventNotification.emt)){
                eventNotification.emts = [];
                eventNotification.emt.split(",").forEach(function (key) {
                    eventNotification.emts.push({"text":key,"id":key});
                });
            }
        };

        $scope.setEntityTagsToExclude = function(eventNotification){
            if(checkNotNullEmpty(eventNotification.eet)){
                eventNotification.eets = [];
                eventNotification.eet.split(",").forEach(function (key) {
                    eventNotification.eets.push({"text":key,"id":key});
                });
            }
        };

        $scope.setOrderTagsToExclude = function(eventNotification){
            if(checkNotNullEmpty(eventNotification.eot)){
                eventNotification.eots = [];
                eventNotification.eot.split(",").forEach(function (key) {
                    eventNotification.eots.push({"text":key,"id":key});
                });
            }
        };

        $scope.deleteNotification = function (msgkey, event) {
            if ($scope.nof != null) {
                if ($scope.subview == 'orders') {
                    $scope.orderKeys.forEach(function (key) {
                        $scope.notif = $scope.getObjectToDelete($scope.nof[msgkey], event);
                        $scope.notif.id = msgkey;
                    });
                } else if ($scope.subview == 'shipments') {
                    $scope.shipmentKeys.forEach(function (key) {
                        $scope.notif = $scope.getObjectToDelete($scope.nof[msgkey], event);
                        $scope.notif.id = msgkey;
                    });
                } else if ($scope.subview == 'inventory') {
                    $scope.inventoryKeys.forEach(function (key) {
                        $scope.notif = $scope.getObjectToDelete($scope.nof[msgkey], event);
                        $scope.notif.id = msgkey;
                    });
                } else if ($scope.subview == 'setup') {
                    $scope.setupKeys.forEach(function (key) {
                        $scope.notif = $scope.getObjectToDelete($scope.nof[msgkey], event);
                        $scope.notif.id = msgkey;
                    });
                } else if ($scope.subview == 'temperature') {
                    $scope.temperatureKeys.forEach(function (key) {
                        $scope.notif = $scope.getObjectToDelete($scope.nof[msgkey], event);
                        $scope.notif.id = msgkey;
                    });
                } else if($scope.subview == 'accounts'){
                    $scope.accountKeys.forEach(function (key){
                        $scope.notif = $scope.getObjectToDelete($scope.nof[msgkey], event);
                        $scope.notif.id = msgkey;
                    });
                }
                if (checkNotNullEmpty($scope.notif)) {
                    if (!confirm($scope.resourceBundle['removenotification'] + ' ' + '"'+ event + '"')) {
                        return;
                    }
                    domainCfgService.deleteNotificationCfg($scope.notif).then(function (data) {
                        $scope.delete = data.data;
                        $scope.showSuccess($scope.resourceBundle['notification.delete.success']);
                        $scope.getNotifications();
                        $scope.toggle();
                    }).catch(function error(msg) {
                        $scope.showErrorMsg(msg,true);
                    });
                }
            }
        };

        $scope.updateUserTags = function(eventNotification){
            if(checkNotNullEmpty(eventNotification.usrTgs)){
                eventNotification.uTgs = [];
                eventNotification.usrTgs.split(",").forEach(function (key) {
                    eventNotification.uTgs.push({"id":key,"text":key});
                });
            }
        };
        $scope.resetNotfData = function(value){
            $scope.assetNotfType = value;

            if($scope.assetNotfType == 'temperature'){
                $scope.temperature = NOTIFICATIONS.temperature;
                $scope.temperatureLabel = NOTIFICATIONS.temperatureLabel.events;
            }else if($scope.assetNotfType == 'assetAlarms'){
                $scope.temperature = NOTIFICATIONS.assetAlarms;
                $scope.temperatureLabel = NOTIFICATIONS.assetAlarmsLabel.events;
            }
            $scope.getNotifications();
        };

        $scope.getNotifications();
    }
]);
domainCfgControllers.controller('BulletinBoardConfigurationController', ['$scope', 'domainCfgService',
    function ($scope, domainCfgService) {
        $scope.bb = {};
        $scope.omtb = "";
        $scope.domainId = "";
        $scope.loading = false;
        $scope.saveBulletinBoardConfiguration = function () {
            if ($scope.bb != null) {
                $scope.validateConfig();
                domainCfgService.setBulletinBoardCfg($scope.bb).then(function (data) {
                    $scope.showSuccess(data.data);
                }).catch(function error(msg) {
                    $scope.showErrorMsg(msg,true);
                });
            }
        };
        $scope.getBulletinBoardConfiguration = function () {
            $scope.loading = true;
            $scope.showLoading();
            domainCfgService.getBulletinBoardCfg().then(function (data) {
                $scope.bb = data.data;
            }).catch(function error(msg) {
                $scope.showErrorMsg(msg,true);
            }).finally(function(){
                $scope.loading = false;
                $scope.hideLoading();
            });
        };
        $scope.getBulletinBoardConfiguration();
        $scope.openMessageToBoard = function () {
            $scope.omtb = "open";
        };
        $scope.toggle = function () {
            $scope.omtb = "";
        };
        $scope.validateConfig = function(){
            if(checkNullEmpty($scope.bb.dd)){
                $scope.bb.dd = "60";
            }
            if(checkNullEmpty($scope.bb.iob)){
                $scope.bb.iob = "100";
            }
            if(checkNullEmpty($scope.bb.rd)){
                $scope.bb.rd = "3600";
            }
            if(checkNullEmpty($scope.bb.si)){
                $scope.bb.si = "2000";
            }
        }
    }
]);
domainCfgControllers.controller('BBPost', ['$scope', 'domainCfgService',
    function ($scope, domainCfgService) {
        $scope.msg = '';
        $scope.postToBoard = function () {
            if (checkNotNullEmpty($scope.msg)) {
                domainCfgService.postToBoard($scope.msg).then(function (data) {
                    $scope.toggle();
                    $scope.showSuccess(data.data);
                }).catch(function error(msg) {
                    $scope.showErrorMsg(msg,true);
                });
            }
        };
    }
]);
domainCfgControllers.controller('AccessLogController', ['$scope', 'domainCfgService', 'requestContext', '$location',
    function ($scope, domainCfgService, requestContext, $location) {
        $scope.accesslog = {};
        $scope.loading = false;
        $scope.wparams = [["o", "offset"], ["s", "size"]];
        ListingController.call(this, $scope, requestContext, $location);
        $scope.init = function () {}; //Overriding parent init function
        $scope.fetch = function () {
            $scope.loading = true;
            $scope.showLoading();
            domainCfgService.getAccessLog($scope.offset, $scope.size).then(function (data) {
                $scope.accesslog = data.data.results;
                $scope.setResults(data.data);
            }).catch(function err(msg){
                $scope.showWarning($scope.resourceBundle['accesslog.unavailable']);
            }).finally(function(){
                $scope.loading = false;
                $scope.hideLoading();
            });
        };
        $scope.fetch();
    }
]);
domainCfgControllers.controller('NotificationMessageController', ['$scope', 'domainCfgService', 'requestContext', '$location','exportService',
    function ($scope, domainCfgService, requestContext, $location,exportService) {
        $scope.wparams = [["fm","from","", formatDate2Url],["to","to","", formatDate2Url]];
        $scope.filtered = {};
        $scope.today = formatDate2Url(new Date());
        $scope.init = function(){
            $scope.from = parseUrlDate(requestContext.getParam("fm")) || "";
            $scope.to = parseUrlDate(requestContext.getParam("to")) || "";
        };
        $scope.init();
        ListingController.call(this, $scope, requestContext, $location);
        $scope.fetch = function () {
            $scope.loading = true;
            $scope.showLoading();
            if(checkNotNullEmpty($scope.from)){
                var start = formatDate($scope.from);
            }
            if(checkNotNullEmpty($scope.to)){
                var end = formatDate($scope.to);
            }
            domainCfgService.getNotificationsMessage(start,end,$scope.offset, $scope.size).then(function (data) {
                $scope.message1 = data.data;
                $scope.setResults($scope.message1);
            }).catch(function error(msg) {
                $scope.showErrorMsg(msg);
                $scope.setResults(null);
            }).finally(function(){
                $scope.loading = false;
                $scope.hideLoading();
            });
        };
        $scope.fetch();

        $scope.reset = function(){
            $scope.from='';
            $scope.to= '';
        }
    }
]);
domainCfgControllers.controller('CustomReportConfigurationController', ['$scope', 'domainCfgService', 'requestContext', '$location', 'configService',
    function ($scope, domainCfgService, requestContext, $location, configService) {
        TimezonesControllerKVReversed.call(this, $scope, configService);
        $scope.dmntz = 'UTC';
        $scope.init = function(){
            $scope.cr = {};
            $scope.users = {};
            $scope.types = ["mn","an","sn"];
            $scope.fr = ["daily","monthly"];
            $scope.crp = {};
            $scope.open = false;
            $scope.cr.ogf = true;
            $scope.cr.tgf = true;
            $scope.cr.mtgf = true;
            $scope.cr.tcrgf = true;
            $scope.cr.itrgf = true;
            $scope.crt = false;
            $scope.open = false;
            $scope.showEdit = false;
            $scope.edit = false;
            $scope.edituf = true;
            $scope.loading = false;
            $scope.cont = false;
            $scope.emt = false;
            $scope.cr.an = [];
            $scope.cr.mn = [];
            $scope.cr.sn = [];
            $scope.cr.exusrs = [];
            $scope.origname = "";

        };
        $scope.init();
        $scope.getGeneralConfiguration = function() {
            $scope.loading = true;
            $scope.showLoading();

            domainCfgService.getGeneralCfg().then(function (data) {
                if (checkNotNullEmpty(data.data.tz)) {
                    $scope.dmntz = $scope.allTimezonesKVReversed[data.data.tz];
                }
            }).catch(function error(msg) {
                $scope.showErrorMsg(msg, true);
            }).finally(function () {
                $scope.loading = false;
                $scope.hideLoading();
            });
        };
        $scope.getGeneralConfiguration();

        $scope.openReport = function () {
            $scope.open = true;
        };
        $scope.resetSchedule = function () {
            $scope.cr.rgtw = "";
            $scope.cr.rgth = "";
        };
        $scope.resetReportGenerationTime = function () {
            if ($scope.cr.ogf) {
                $scope.cr.od = "";
            }
            if ($scope.cr.tgf) {
                $scope.cr.td = "";
            }
            if($scope.cr.mtgf){
                $scope.cr.mtd = "";
            }
            if ($scope.cr.itrgf) {
                $scope.cr.itd = "";
            }
            if ($scope.cr.tcrgf) {
                $scope.cr.tcd = "";
            }
        };
        $scope.getInventoryConfiguration = function(){
            domainCfgService.getInventoryCfg().then(function (data) {
                $scope.inv = data.data;
                if($scope.inv != null){
                    if(checkNotNullEmpty($scope.inv.emuidt)){
                        $scope.emt = true;
                    }
                }
            }).catch(function error(msg) {
                $scope.showErrorMsg(msg, true);
            })
        };

        $scope.getCustomReports = function () {
            $scope.loading = true;
            $scope.showLoading();
            domainCfgService.getCustomReports().then(function (data) {
                $scope.crp = data.data.config;
                if ($scope.crp != null && $scope.crp.length > 0) {
                    $scope.crt = true;
                    $scope.lastUpdated = data.data.lastUpdated;
                    $scope.createdBy = data.data.createdBy;
                    $scope.fn = data.data.fn;
                }else{
                    $scope.crt = false;
                }
            }).catch(function error(msg) {
                $scope.showErrorMsg(msg,true);
            }).finally(function(){
                $scope.loading = false;
                $scope.hideLoading();
            });
        };
        $scope.addTemplate = function () {
            $scope.init();
            $scope.getInventoryConfiguration();
            $scope.open = true;
            $scope.edituf = false;
        };
        $scope.editUploadedFile = function (name) {
            if (checkNotNullEmpty(name)) {
                $scope.edituf = false;
                $scope.showEdit = true;
                $scope.fileData = undefined;
            }
        };
        $scope.stopEdit = function () {
            $scope.edituf = true;
            $scope.showEdit = false;
        };
        $scope.reloadTemplate = function () {
            $scope.getCustomReports();
            $scope.open = false;
            $scope.edituf = true;
            $scope.edit = false;
        };
        $scope.getCustomReports();
        $scope.uploadURL = function () {
            $scope.uploading = true;
            domainCfgService.uploadURL().then(function (data) {
                $scope.actionURL = cleanupString(data.data.toString().replace(/"/g, ''));
                $scope.uploading = false;
                $scope.urlLoaded = true;
            }).catch(function error(msg) {
                $scope.showErrorMsg(msg,true);
            });
        };
        $scope.uploadURL();

        $scope.constructModelUsers = function(users){
            for(var type in $scope.types){
                var uUsers = users[$scope.types[type]];
                for(var user in uUsers){
                    $scope.users[$scope.types[type]].push({text: uUsers[user],id:uUsers[user]});
                }
            }
        };

        $scope.updateUsersFromModel = function(){
            for(var type in $scope.types){
                var uUsers = $scope.users[$scope.types[type]];
                $scope.cr[$scope.types[type]] = [];
                for(var tag in uUsers){
                    $scope.cr[$scope.types[type]].push(uUsers[tag].text);
                }
            }
        };
        $scope.validateTemplateName = function( templateName ) {
            var regExp = /^[_a-zA-Z0-9-\s]+$/;
            if ( templateName.match( regExp ) )
                return true;
            else
                return false;
        };
        $scope.validateCustomReport = function(){
            $scope.cont = false;
            if($scope.fileData != undefined){
                if(checkNullEmpty($scope.cr.tn)){
                    $scope.showWarning($scope.resourceBundle['custom.templatenamedetail']);
                    return;
                }else{
                    if($scope.validateTemplateName($scope.cr.tn)){
                        if(checkNullEmpty($scope.cr.invsn) && checkNullEmpty($scope.cr.usn) && checkNullEmpty($scope.cr.ksn) && checkNullEmpty($scope.cr.msn) &&
                            checkNullEmpty($scope.cr.osn) && checkNullEmpty($scope.cr.tsn) && checkNullEmpty($scope.cr.tcsn) && checkNullEmpty($scope.cr.itsn) && checkNullEmpty($scope.cr.hsn) && checkNullEmpty($scope.cr.mtsn)){
                            $scope.showWarning($scope.resourceBundle['customreports.entersheetnamemsg']);
                            return;
                        }else{
                            if(checkNotNullEmpty($scope.cr.invsn) && !$scope.validateTemplateName($scope.cr.invsn)){
                                $scope.showWarning($scope.resourceBundle['sheetname.invalid'] + " " + $scope.resourceBundle['inventory.lowercase'] + ". " +  $scope.resourceBundle['sheetname.validate'] + " ");
                                return;
                            }
                            if(checkNotNullEmpty($scope.cr.usn) && !$scope.validateTemplateName($scope.cr.usn)){
                                $scope.showWarning($scope.resourceBundle['sheetname.invalid'] + " " + $scope.resourceBundle['users.lowercase'] + ". " +  $scope.resourceBundle['sheetname.validate'] + " ");
                                return;
                            }
                            if(checkNotNullEmpty($scope.cr.ksn) && !$scope.validateTemplateName($scope.cr.ksn)){
                                $scope.showWarning($scope.resourceBundle['sheetname.invalid'] + " " + $scope.resourceBundle['kiosks.lowercase'] + ". " +  $scope.resourceBundle['sheetname.validate'] + " ");
                                return;
                            }
                            if(checkNotNullEmpty($scope.cr.msn) && !$scope.validateTemplateName($scope.cr.msn)){
                                $scope.showWarning($scope.resourceBundle['sheetname.invalid'] + " " + $scope.resourceBundle['materials.lowercase'] + ". " +  $scope.resourceBundle['sheetname.validate'] + " ");
                                return;
                            }

                            if(checkNotNullEmpty($scope.cr.osn)){
                                if($scope.validateTemplateName($scope.cr.osn)){
                                    if(!$scope.cr.ogf){
                                        if(checkNotNullEmpty($scope.cr.od)){
                                            if($scope.cr.od > 180){
                                                $scope.showWarning($scope.resourceBundle['customreports.maxdatadurationmsg.new'] + " " + $scope.resourceBundle['filename.orders'] + " "+ $scope.resourceBundle['sheet'] + " " + $scope.resourceBundle['of'] + " " + $scope.resourceBundle['customreport.lowercase']);
                                                return;
                                            }
                                        }else{
                                            $scope.showWarning($scope.resourceBundle['export.duration'] + " " + $scope.resourceBundle['filename.orders'] + " " + $scope.resourceBundle['sheet'] + " " + $scope.resourceBundle['of'] + " " + $scope.resourceBundle['customreport.lowercase']);
                                            return;
                                        }
                                    }
                                }else{
                                    $scope.showWarning($scope.resourceBundle['sheetname.invalid'] + " " + $scope.resourceBundle['filename.orders'] + ". " +  $scope.resourceBundle['sheetname.validate'] + " ");
                                    return;
                                }
                            }
                            if(checkNotNullEmpty($scope.cr.tsn)){
                                if($scope.validateTemplateName($scope.cr.tsn)){
                                    if(!$scope.cr.tgf){
                                        if(checkNotNullEmpty($scope.cr.td)){
                                            if($scope.cr.td > 180){
                                                $scope.showWarning($scope.resourceBundle['customreports.maxdatadurationmsg.new'] + " " + $scope.resourceBundle['transactions.lowercase'] + " "+ $scope.resourceBundle['sheet'] + " " + $scope.resourceBundle['of'] + " " + $scope.resourceBundle['customreport.lowercase']);
                                                return;
                                            }
                                        }else{
                                            $scope.showWarning($scope.resourceBundle['export.duration'] + " " + $scope.resourceBundle['transactions.lowercase'] + " " + $scope.resourceBundle['sheet'] + " " + $scope.resourceBundle['of'] + " " + $scope.resourceBundle['customreport.lowercase']);
                                            return;
                                        }
                                    }
                                }else{
                                    $scope.showWarning($scope.resourceBundle['sheetname.invalid'] + " " + $scope.resourceBundle['transactions.lowercase'] + ". " +  $scope.resourceBundle['sheetname.validate'] + " ");
                                    return;
                                }
                            }
                            if(checkNotNullEmpty($scope.cr.mtsn)){
                                if($scope.validateTemplateName($scope.cr.mtsn)){
                                    if(!$scope.cr.mtgf){
                                        if(checkNotNullEmpty($scope.cr.mtd)){
                                            if($scope.cr.mtd > 180){
                                                $scope.showWarning($scope.resourceBundle['customreports.maxdatadurationmsg.new'] + " " + $scope.resourceBundle['manual.transactions.lowercase'] + " " + $scope.resourceBundle['sheet'] + " " + $scope.resourceBundle['of'] + " " + $scope.resourceBundle['customreport.lowercase']);
                                                return;
                                            }
                                        }else{
                                            $scope.showWarning($scope.resourceBundle['export.duration'] + " " + $scope.resourceBundle['manual.transactions.lowercase'] + " " + $scope.resourceBundle['sheet'] + " " + $scope.resourceBundle['of'] + " " + $scope.resourceBundle['customreport.lowercase']);
                                            return;
                                        }
                                    }
                                }else{
                                    $scope.showWarning($scope.resourceBundle['sheetname.invalid'] + " " + $scope.resourceBundle['manual.transactions.lowercase'] + ". " +  $scope.resourceBundle['sheetname.validate'] + " ");
                                    return;
                                }
                            }
                            if(checkNotNullEmpty($scope.cr.tcsn)){
                                if($scope.validateTemplateName($scope.cr.tcsn)){
                                    if(checkNullEmpty($scope.cr.tct)){
                                        $scope.showWarning($scope.resourceBundle['aggregation.period'] + " " + $scope.resourceBundle['for'] + " " + $scope.resourceBundle['report.transactioncounts.lowercase'] + " " + $scope.resourceBundle['sheet']);
                                        return;
                                    }
                                    if(checkNullEmpty($scope.cr.tce)){
                                        $scope.showWarning($scope.resourceBundle['aggregation.event'] + " " + $scope.resourceBundle['for'] + " " + $scope.resourceBundle['report.transactioncounts.lowercase'] + " " + $scope.resourceBundle['sheet']);
                                        return;
                                    }
                                    if(!$scope.cr.tcrgf){
                                        if(checkNotNullEmpty($scope.cr.tcd)){
                                            if($scope.cr.tcd > 180){
                                                $scope.showWarning($scope.resourceBundle['customreports.maxdatadurationmsg.new'] + " " + $scope.resourceBundle['report.transactioncounts.lowercase'] + " " + $scope.resourceBundle['sheet'] + " " + $scope.resourceBundle['of'] + " " + $scope.resourceBundle['customreport.lowercase']);
                                                return;
                                            }
                                        }else{
                                            $scope.showWarning($scope.resourceBundle['export.duration'] + " " + $scope.resourceBundle['report.transactioncounts.lowercase'] + " " + $scope.resourceBundle['sheet'] + " " + $scope.resourceBundle['of'] + " " + $scope.resourceBundle['customreport.lowercase']);
                                            return;
                                        }
                                    }

                                }else{
                                    $scope.showWarning($scope.resourceBundle['sheetname.invalid'] + " " + $scope.resourceBundle['report.transactioncounts.lowercase'] + ". " +  $scope.resourceBundle['sheetname.validate'] + " ");
                                    return;
                                }
                            }
                            if(checkNotNullEmpty($scope.cr.itsn)){
                                if($scope.validateTemplateName($scope.cr.itsn)){
                                    if(checkNullEmpty($scope.cr.itt)){
                                        $scope.showWarning($scope.resourceBundle['aggregation.period'] + " " + $scope.resourceBundle['for'] + " " + $scope.resourceBundle['report.consumptiontrends.lowercase'] + " " + $scope.resourceBundle['sheet']);
                                        return;
                                    }
                                    if(checkNullEmpty($scope.cr.ite)){
                                        $scope.showWarning($scope.resourceBundle['aggregation.event'] + " " + $scope.resourceBundle['for'] + " " + $scope.resourceBundle['report.consumptiontrends.lowercase'] + " " + $scope.resourceBundle['sheet']);
                                        return;
                                    }
                                    if(!$scope.cr.itrgf){
                                        if(checkNotNullEmpty($scope.cr.itd)){
                                            if($scope.cr.itd > 180){
                                                $scope.showWarning($scope.resourceBundle['customreports.maxdatadurationmsg.new'] + " " + $scope.resourceBundle['report.consumptiontrends.lowercase'] + " " + $scope.resourceBundle['sheet'] + " " + $scope.resourceBundle['of'] + " " + $scope.resourceBundle['customreport.lowercase']);
                                                return;
                                            }
                                        }else{
                                            $scope.showWarning($scope.resourceBundle['export.duration'] + " " + $scope.resourceBundle['report.consumptiontrends.lowercase'] + " " + $scope.resourceBundle['sheet'] + " " + $scope.resourceBundle['of'] + " " + $scope.resourceBundle['customreport.lowercase']);
                                            return;
                                        }
                                    }

                                }else{
                                    $scope.showWarning($scope.resourceBundle['sheetname.invalid'] + " " + $scope.resourceBundle['report.consumptiontrends.lowercase'] + ". " +  $scope.resourceBundle['sheetname.validate'] + " ");
                                    return;
                                }
                            }
                            if(checkNullEmpty($scope.cr.rgt)){
                                $scope.showWarning($scope.resourceBundle['customreports.enterreportgenerationschedulemsg']);
                                return;
                            }else {
                                if ($scope.cr.rgt == 0 && checkNullEmpty($scope.cr.rgth)) {
                                    $scope.showWarning($scope.resourceBundle['customreports.entertimefordailyreportgenerationmsg']);
                                    return;
                                } else if ($scope.cr.rgt == 1) {
                                    if (checkNullEmpty($scope.cr.rgtw)) {
                                        $scope.showWarning($scope.resourceBundle['customreports.selectdayforweeklyreportgenerationmsg']);
                                        return;
                                    } else if (checkNullEmpty($scope.cr.rgth)) {
                                        $scope.showWarning($scope.resourceBundle['customreports.entertimeforweeklyreportgenerationmsg']);
                                        return;
                                    }
                                } else if ($scope.cr.rgt == 2) {
                                    if((checkNullEmpty($scope.cr.rgtm)) || ($scope.cr.rgtm < 1 || $scope.cr.rgtm > 28)) {
                                        $scope.showWarning($scope.resourceBundle['customreports.enterdayformonthlyreportgenerationmsg']);
                                        return;
                                    } else if (checkNullEmpty($scope.cr.rgth)) {
                                        $scope.showWarning($scope.resourceBundle['customreports.entertimeformonthlyreportgenerationmsg']);
                                        return;
                                    }
                                }
                            }
                            if (checkNullEmpty($scope.cr.an) && checkNullEmpty($scope.cr.mn) && checkNullEmpty($scope.cr.sn) && checkNullEmpty($scope.cr.exusrsvo) && checkNullEmpty($scope.cr.uTgs)) {
                                $scope.showWarning($scope.resourceBundle['customreports.entermanageroradminorsuperuserlistmsg']);
                                return;
                            }
                        }
                    }else{
                        $scope.showWarning($scope.resourceBundle['sheetname.invalid'] + ". " +  $scope.resourceBundle['sheetname.validate'] + " ");
                        return;
                    }
                }

            }else{
                $scope.showWarning($scope.resourceBundle['customreports.selecttemplatetouploadmsg']);
                return;
            }
            $scope.cont = true;
        };
        $scope.getFilteredUsers = function(){
            if($scope.cr != null) {
                if (checkNotNullEmpty($scope.cr.an)) {
                    $scope.cr.aname = $scope.cr.an;
                    $scope.cr.an = [];
                    for (var i = 0; i < $scope.cr.aname.length; i++) {
                        $scope.cr.an.push({"id": $scope.cr.aname[i].id, "text": $scope.cr.aname[i].fnm+' ['+$scope.cr.aname[i].id+']'});
                    }
                }
                if (checkNotNullEmpty($scope.cr.mn)) {
                    $scope.cr.mname = $scope.cr.mn;
                    $scope.cr.mn = [];
                    for (var i = 0; i < $scope.cr.mname.length; i++) {
                        $scope.cr.mn.push({"id": $scope.cr.mname[i].id, "text": $scope.cr.mname[i].fnm+' ['+$scope.cr.mname[i].id+']'});
                    }
                }
                if (checkNotNullEmpty($scope.cr.sn)) {
                    $scope.cr.sname = $scope.cr.sn;
                    $scope.cr.sn = [];
                    for (var i = 0; i < $scope.cr.sname.length; i++) {
                        $scope.cr.sn.push({"id": $scope.cr.sname[i].id, "text": $scope.cr.sname[i].fnm+' ['+$scope.cr.sname[i].id+']'});
                    }
                }
                if(checkNotNullEmpty($scope.cr.exusrs)){
                    $scope.cr.exusrsvo = [];
                    $scope.cr.exusrs.forEach(function(name){
                       $scope.cr.exusrsvo.push({"id":name,"text":name});
                    });
                }
                if(checkNotNullEmpty($scope.cr.usrTgs)) {
                    $scope.cr.uTgs = [];
                    $scope.cr.usrTgs.forEach(function(item) {
                        $scope.cr.uTgs.push({"id":item,"text":item});
                    });
                }
            }
        };

        function updateExUsrs(){
            $scope.cr.exusrs = [];
            $scope.cr.usrTgs = [];
            if (checkNotNullEmpty($scope.cr.exusrsvo)) {
                $scope.cr.exusrsvo.forEach(function (name) {
                    $scope.cr.exusrs.push(name.id);
                });
            }
            if(checkNotNullEmpty($scope.cr.uTgs)) {
                $scope.cr.uTgs.forEach(function (item) {
                    $scope.cr.usrTgs.push(item.id);
                });
            }
        }

        $scope.saveCustomReports = function () {
            if ($scope.cr != null) {
                $scope.validateCustomReport();
                if ($scope.cont) {
                    updateExUsrs();
                    $scope.showLoading();
                    $scope.cr.tk = "";
                    $scope.edit = false;
                    domainCfgService.uploadPostUrl($scope.actionURL, $scope.fileData, $scope.cr.tn, $scope.cr.tk, $scope.edit).then(function (data) {
                        $scope.config = data.data;
                        domainCfgService.setCustomReports($scope.cr, $scope.config).then(function (data) {
                            $scope.showSuccess(data.data);
                            $scope.reloadTemplate();
                        }).catch(function error(msg) {
                            $scope.showErrorMsg(msg, true);
                        });
                    }).catch(function error(msg) {
                        $scope.showErrorMsg(msg, true);
                    }).finally(function(){
                        $scope.uploadURL();
                        $scope.getCustomReports();
                        $scope.hideLoading();
                    });
                }
            }
        };
        $scope.deleteCustomReport = function (name) {
            if (checkNotNullEmpty(name)) {
                if (!confirm($scope.resourceBundle['customreports.confirmremovetemplatemsg'])) {
                    return;
                }
                domainCfgService.deleteCustomReport(name).then(function (data) {
                    $scope.showSuccess(data.data);
                    $scope.reloadTemplate();
                }).catch(function error(msg) {
                    $scope.showErrorMsg(msg, true);
                });
            }
        };
        $scope.editCustomReport = function (name, key) {
            if (checkNotNullEmpty(name) && checkNotNullEmpty(key)) {
                domainCfgService.getCustomReport(name, key).then(function (data) {
                    $scope.cr = data.data.config;
                    $scope.getFilteredUsers();
                    $scope.getInventoryConfiguration();
                    $scope.open = true; // to open the add custom report page with populated values
                    $scope.crt = false; // to close the table with custom reports value
                    $scope.edit = true;
                    $scope.showEdit = false;
                    $scope.fileData = undefined;
                    if(checkNotNullEmpty($scope.cr.tn)){
                        $scope.cr.origname = $scope.cr.tn;
                    }
                }).catch(function error(msg) {
                    $scope.showErrorMsg(msg, true);
                });
            }
        };
        $scope.exportReport = function (name) {
            if (checkNotNullEmpty(name)) {
                if (!confirm($scope.resourceBundle['export.chosen'] + ' ' + name + '. ' +  $scope.resourceBundle['report.email'] + ' ' + $scope.resourceBundle['report.configured.users'] + '. ' + $scope.resourceBundle['continue'] + '?')) {
                    return;
                }
                domainCfgService.exportReport(name).then(function (data) {
                    $scope.showSuccess($scope.resourceBundle['report.upper'] + " " + name +  " " + $scope.resourceBundle['export.scheduled'] + ' ' + $scope.resourceBundle['exportstatusinfo2'] + ' ' + data.data + '. ' + $scope.resourceBundle['customreportsstatusinfo1'] );
                    $scope.reloadTemplate();
                }).catch(function error(msg) {
                    $scope.showErrorMsg(msg, true);
                });
            }
        };
        $scope.updateCustomReport = function () {
            if($scope.edituf && !$scope.showEdit){
                $scope.fileData = "";
            }
            $scope.validateCustomReport();
            if($scope.cont){
                updateExUsrs();
                if ($scope.showEdit && ($scope.fileData != undefined && checkNotNullEmpty($scope.fileData))) {
                    $scope.uploadCustomReports();
                } else {
                    if ($scope.cr != null) {
                        domainCfgService.updateCustomReport($scope.cr).then(function (data) {
                            $scope.showSuccess(data.data);
                            $scope.reloadTemplate();
                        }).catch(function error(msg) {
                            $scope.showErrorMsg(msg, true);
                        });
                    }
                }
            }
        };
        $scope.uploadCustomReports = function () {
            if ($scope.cr != null) {
                var filename = $scope.fileData.name.split(".");
                var ext = filename[filename.length - 1];
                if(ext != 'xls' && ext != 'xlsx' && ext != 'xlsm') {
                    $scope.showWarning($scope.resourceBundle['upload.excel']);
                    return false;
                }
                domainCfgService.uploadPostUrl($scope.actionURL, $scope.fileData, $scope.cr.tn, $scope.cr.tk, $scope.edit).then(function (data) {
                    $scope.config = data.data;
                    domainCfgService.setCustomReports($scope.cr, $scope.config).then(function (data) {
                        $scope.showSuccess(data.data);
                        $scope.reloadTemplate();
                        $scope.fileData = undefined;
                    }).catch(function error(msg) {
                        $scope.showErrorMsg(msg, true);
                    });
                }).catch(function error(msg) {
                    $scope.showErrorMsg(msg, true);
                }).finally(function(){
                    $scope.uploadURL();
                });
            }
        };
    }
]);
domainCfgControllers.controller('DashboardConfigurationController', ['$scope', 'domainCfgService',
    function ($scope, domainCfgService) {
        $scope.db = {};
        $scope.loading = false;
        $scope.getDashboardCfg = function () {
            $scope.loading = true;
            $scope.showLoading();
            domainCfgService.getDashboardCfg().then(function (data) {
                $scope.db = data.data;
                if(checkNullEmpty($scope.db.edm)){
                    $scope.db.edm = false;
                }
                if(checkNullEmpty($scope.db.aper)) {
                    $scope.db.aper = "7";
                }
                if(checkNotNullEmpty($scope.db.dmtg)) {
                    $scope.db.dmtgo = [];
                    $scope.db.dmtg.forEach(function (mt){
                        $scope.db.dmtgo.push({'text': mt, 'id': mt});
                    });
                }
                if(checkNotNullEmpty($scope.db.dimtg)) {
                    $scope.db.dimtgo = {'text': $scope.db.dimtg, 'id': $scope.db.dimtg};
                }
                if(checkNotNullEmpty($scope.db.detg)) {
                    $scope.db.detgo = {'text': $scope.db.detg, 'id': $scope.db.detg};
                }
                if(checkNotNullEmpty($scope.db.exet)) {
                    $scope.db.exetgo = [];
                    $scope.db.exet.forEach(function (et) {
                        $scope.db.exetgo.push({'text': et, 'id': et});
                    });
                }
                if(checkNotNullEmpty($scope.db.exts)) {
                    $scope.db.exetsta = [];
                    $scope.db.exts.forEach(function (et) {
                        $scope.db.exetsta.push({'text': et, 'id': et});
                    });
                }
                if(checkNotNullEmpty($scope.db.dutg)) {
                    $scope.db.dutgo = [];
                    $scope.db.dutg.forEach(function (ut) {
                        $scope.db.dutgo.push({'text': ut, 'id': ut});
                    });
                }
            }).catch(function error(msg) {
                $scope.showErrorMsg(msg, true);
            }).finally(function(){
                $scope.loading = false;
                $scope.hideLoading();
            });
        };
        $scope.getDashboardCfg();
        $scope.setDashboardCfg = function () {
            if(checkNotNullEmpty($scope.db)){
                if(!$scope.db.ape && !$scope.db.ipe && !$scope.db.ope && !$scope.db.rpe){
                    $scope.showWarning($scope.resourceBundle['notifications.nopanelselected']);
                }else{
                    $scope.loading = true;
                    $scope.showLoading();
                    if(checkNotNullEmpty($scope.db.dmtgo)) {
                        $scope.db.dmtg = [];
                        $scope.db.dmtgo.forEach(function (mt){
                            $scope.db.dmtg.push(mt.id);
                        });
                    } else {
                        $scope.db.dmtg = undefined;
                    }
                    if(checkNotNullEmpty($scope.db.dimtgo)) {
                        $scope.db.dimtg = $scope.db.dimtgo.id;
                    } else {
                        $scope.db.dimtg = undefined;
                    }
                    if(checkNotNullEmpty($scope.db.detgo)) {
                        $scope.db.detg = $scope.db.detgo.id;
                    } else {
                        $scope.db.detg = undefined;
                    }
                    if(checkNotNullEmpty($scope.db.exetgo)) {
                        $scope.db.exet = [];
                        $scope.db.exetgo.forEach(function (et) {
                            $scope.db.exet.push(et.id);
                        });
                    } else {
                        $scope.db.exet = undefined;
                    }
                    if(checkNotNullEmpty($scope.db.dutgo)) {
                        $scope.db.dutg = [];
                        $scope.db.dutgo.forEach(function(ut) {
                           $scope.db.dutg.push(ut.id);
                        });
                    } else {
                        $scope.db.dutg = undefined;
                    }
                    if(checkNotNullEmpty($scope.db.exetsta)) {
                        $scope.db.exts = [];
                        $scope.db.exetsta.forEach(function (et) {
                            $scope.db.exts.push(et.text);
                        });
                    } else {
                        $scope.db.exts = undefined;
                    }
                    domainCfgService.setDashboardCfg($scope.db).then(function (data) {
                        $scope.refreshDomainConfig();
                        $scope.showSuccess(data.data);
                    }).catch(function error(msg) {
                        $scope.showErrorMsg(msg, true);
                    }).finally(function (){
                        $scope.loading = false;
                        $scope.getDashboardCfg();
                        $scope.hideLoading();
                    });
                }
            }
        };

        domainCfgService.getMaterialTagsCfg().then(function (data) {
            $scope.tags = data.data.tags;
            $scope.udf = data.data.udf;
        });

        var exsData = {results: []};

        exsData.results[0] = {id:'tn',text:"Normal"};
        exsData.results[1] = {id:'tl',text:"Low"};
        exsData.results[2] = {id:'th',text:"High"};
        exsData.results[3] = {id:'tu',text:"Unknown"};

        $scope.filterStatus = function(query){
            query.callback(exsData);
        }

        $scope.query = function (query) {
            var data = {results: []};
            var term = query.term.toLowerCase();
            for (var i in $scope.tags) {
                var tag = $scope.tags[i].toLowerCase();
                if (tag.indexOf(term) >= 0) {
                    data.results.push({'text': $scope.tags[i], 'id': $scope.tags[i]});
                }
            }
            if ($scope.udf && !$scope.isSelected(term)) {
                data.results.push({'text': term, 'id': term})
            }
            query.callback(data);
        }
    }
]);

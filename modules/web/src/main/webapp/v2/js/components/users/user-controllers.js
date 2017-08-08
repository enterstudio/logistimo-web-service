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

var userControllers = angular.module('userControllers', []);
userControllers.controller('UsersListController', ['$scope', 'userService', 'requestContext', '$location', '$window', 'exportService',
    function ($scope, userService, requestContext, $location, $window, exportService) {
        $scope.wparams = [["search", "search.nm"],["role","urole"],["phn","mphn"],["vsn","uvsn"],["act","uactive"],
            ["fm","from","", formatDate2Url],["to","to","", formatDate2Url],["nvrlogged","nvrlogged"],["utag","utag"]];
        $scope.filtered = {};
        $scope.userId = '';
        $scope.today = formatDate2Url(new Date());
        $scope.init = function () {
            $scope.search = {};
            $scope.search.nm = requestContext.getParam("search") || "";
            $scope.search.key = $scope.search.nm;
            $scope.urole = requestContext.getParam("role") || "";
            $scope.mphn = requestContext.getParam("phn") || "";
            $scope.uphn = $scope.mphn;
            $scope.uvsn = requestContext.getParam("vsn") || "";
            $scope.uversion=$scope.uvsn;
            $scope.active= requestContext.getParam("act") || "";
            $scope.from = parseUrlDate(requestContext.getParam("fm")) || "";
            $scope.to = parseUrlDate(requestContext.getParam("to")) || "";
            $scope.nvrlogged = requestContext.getParam("nvrlogged") || false;
            $scope.utag = requestContext.getParam("utag") || "";

        };
        $scope.init();
        ListingController.call(this, $scope, requestContext, $location);

        $scope.fetch = function () {
            $scope.loading = true;
            var filters = {};

            $scope.showLoading();
            if(checkNotNullEmpty($scope.search.key)){
                filters.nName = $scope.search.key;
            }
            if(checkNotNullEmpty($scope.uphn)){
                filters.mobilePhoneNumber = $scope.uphn;
            }
            if(checkNotNullEmpty($scope.from)){
                filters.lastLoginFrom = formatDate($scope.from) + ' 00:00:00';
            }
            if(checkNotNullEmpty($scope.to)){
                filters.lastLoginTo = formatDate($scope.to) + ' 00:00:00';
            }
            if(checkNotNullEmpty($scope.uversion)){
                filters.v = $scope.uversion;
            }
            if(checkNotNullEmpty($scope.active)){
                filters.isEnabled = $scope.active;
            }
            if(checkNotNull($scope.nvrlogged) && ($scope.nvrlogged)){
                filters.neverLogged = true;
            }
            if(checkNotNull($scope.utag) && ($scope.utag)){
                filters.tgs = $scope.utag;
            }
            filters.role = $scope.urole;
            filters.offset = $scope.offset;
            filters.size = $scope.size;
            userService.getFilteredDomainUsers(filters).then(function (data) {
                $scope.users = data.data;
                $scope.setResults($scope.users);
                $scope.hideLoading();
                fixTable();
                $scope.noUserFound = checkNullEmpty($scope.users.results);
            }).catch(function error(msg) {
                $scope.setResults(null);
                $scope.showErrorMsg(msg);
            }).finally(function(){
                $scope.loading = false;
                $scope.hideLoading();
            });
        };
        $scope.getFilteredElement = function(elementId,text){
            var loader= 'loading'+elementId;
            $scope[loader]=true;
            return userService.getElementsByUserFilter(elementId,text).then(function (data) {
                $scope[loader]=false;
                return (checkNotNullEmpty(data.data)?data.data:"")
            }).catch(function error(msg) {
                $scope.showErrorMsg(msg);
            });
        };
        $scope.deleteUsers = function () {
            var users = "";
            var isSupport = false;
            for (var item in $scope.filtered) {
                if ($scope.filtered[item].selected) {
                    users = users.concat($scope.filtered[item].id).concat(',');
                }
            }
            users = stripLastComma(users);
            if (users == "") {
                $scope.showWarning($scope.resourceBundle.selectitemtoremovemsg);
                return;
            }
            if (!confirm($scope.resourceBundle['removeuserconfirmmsg']+"?")) {
                return;
            }
            if(checkNotNullEmpty($scope.support)){
                var usersList = users.split(',');
                for(var i=0; i<$scope.support.length; i++){
                    for(var j=0; j<usersList.length; j++){
                        if($scope.support[i].usrid == usersList[j]){
                            $scope.showWarning($scope.resourceBundle["user.delete.warning"] +"<br/> -"+ $scope.support[i].usrid);
                            return;
                        }
                    }
                }
            }
            function validateAdminUser(adminUser, user) {
                if (!checkNullEmptyObject(adminUser)) {
                    if (adminUser.userId == user) {
                        $scope.showWarning($scope.resourceBundle["auser.delete.warning"] + "<br/> -" + adminUser.userId);
                        return false;
                    }
                }
                return true;
            }

            function checkAdminUsers() {
                var usersList = users.split(',');
                if (!checkNullEmptyObject($scope.admin)) {
                    for (var i = 0; i < usersList.length; i++) {
                        var isValid = validateAdminUser($scope.admin.pac, usersList[i]) &&
                            validateAdminUser($scope.admin.sac, usersList[i]);
                        if (!isValid) {
                            return false;
                        }
                    }
                }
                return true;
            }

            if(!checkAdminUsers()){
                return;
            }
            $scope.showLoading();
            userService.deleteUsers(users).then(function (data) {
                $scope.fetch();
                if (data.data.indexOf("One or more errors") > 1) {
                    $scope.showWarning(data.data);
                } else {
                    $scope.showSuccess(data.data);
                }
                $scope.selAll = false;
            }).catch(function error(msg) {
                $scope.showErrorMsg(msg);
            }).finally(function(){
                $scope.hideLoading();
            });

        };
        $scope.selectAll = function (newval) {
            for (var item in $scope.filtered) {
                $scope.filtered[item]['selected'] = newval;
            }
        };
        $scope.fetch();

        $scope.selectedUsers = '';
        $scope.sendMessage = function () {
            var users = '';
            for (var item in $scope.filtered) {
                if ($scope.filtered[item].selected) {
                    users = users.concat($scope.filtered[item].id).concat(',');
                }
            }
            $scope.selectedUsers = stripLastComma(users);
            if ($scope.selectedUsers == "") {
                if (!confirm($scope.resourceBundle['notselectedusers'])) {
                    return;
                }
            }
            $window.location.href = '#/setup/users/all/sendmessage?userIds=' + $scope.selectedUsers;
        };
        $scope.searchUser = function () {
            if($scope.search.nm != $scope.search.key){
                $scope.search.nm = $scope.search.key;
            }
        };
        $scope.searchPhone = function () {
            if($scope.mphn != $scope.uphn){
                $scope.mphn = $scope.uphn;
            }
        };
        $scope.searchVersion=function(){
            if($scope.uvsn != $scope.uversion){
                $scope.uvsn = $scope.uversion;
            }
        }
        $scope.goToUser = function(userId){
            if(checkNotNullEmpty(userId)){
                $window.location = "#/setup/users/all/details?userId=" +userId;
            }
        }
        $scope.reset = function() {
            $scope.search = {};
            $scope.search.nm = "";
            $scope.search.key = $scope.search.nm;
            $scope.urole="";
            $scope.uvsn="";
            $scope.uversion=$scope.uvsn;
            $scope.uactive="";
            $scope.from="";
            $scope.to="";
            $scope.mphn="";
            $scope.uphn=$scope.mphn;
            $scope.userId="";
            $scope.nvrlogged=false;
            $scope.utag = "";

        };
    }
]);

userControllers.controller('AddUserController', ['$scope', 'userService', 'configService', 'entityService','domainCfgService', 'requestContext', '$window',
    function ($scope, userService, configService, entityService, domainCfgService, requestContext, $window) {
        $scope.user = {lgr:-1, theme: -1};
        $scope.user.st = undefined;
        $scope.user.cnt = undefined;
        $scope.uVisited = {};
        $scope.uidStatus = false;
        $scope.onlyNumbers = /^\d+$/;
        $scope.edit = false;
        $scope.loading = false;
        $scope.editUserId = requestContext.getParam("userId");
        $scope.uidLengthVerified = false;
        $scope.theme = 1;
        if (checkNotNullEmpty($scope.editUserId)) {
            $scope.edit = true;
        }
        $scope.init = function() {
            userService.getRoles($scope.edit,$scope.editUserId).then(function(data){
                $scope.roles = data.data;
            });
        };
        $scope.init();
        LocationController.call(this, $scope, configService);
        LanguageController.call(this, $scope, configService);
        TimezonesController.call(this, $scope, configService);
        $scope.setDomainDefault = function () {
            $scope.loading = true;
            $scope.showLoading();
            domainCfgService.get().then(function (data) {
                $scope.dc = data.data;
                $scope.skipWatch = true;
                if (checkNotNullEmpty($scope.dc.country)) {
                    $scope.setCountry($scope.dc.country);
                    $scope.user.cnt = $scope.dc.country;
                }
                if (checkNotNullEmpty($scope.dc.language)) {
                    $scope.user.lng = $scope.dc.language;
                }
                if (checkNotNullEmpty($scope.dc.timezone)) {
                    $scope.user.tz = $scope.dc.timezone;
                }
                $scope.user.gen = "m";
                $scope.user.per = "d";
                if (checkNotNullEmpty($scope.dc.state)) {
                    $scope.setState($scope.dc.state);
                    $scope.user.st = $scope.dc.state;
                }
                if (checkNotNullEmpty($scope.dc.district)) {
                    $scope.setDistrict($scope.dc.district);
                    $scope.user.ds = $scope.dc.district;
                }
            }).catch(function error(msg) {
                $scope.showErrorMsg(msg);
            }).finally(function(){
                $scope.defineWatchers();
                $scope.loading = false;
                $scope.hideLoading();
                $timeout(function(){
                    $scope.skipWatch = false;
                }, 10);
            });
        };
        if (!$scope.edit) {
            $scope.setDomainDefault();
        }
        $scope.defineWatchers = function () {
            $scope.$watch("user.cnt", function (newval, oldval) {
                if (newval != oldval && !$scope.skipWatch) {
                    $scope.user.st = "";
                    $scope.user.ds = "";
                    $scope.user.tlk = "";
                }
            });
            $scope.$watch("user.st", function (newval, oldval) {
                if (newval != oldval &&  !$scope.skipWatch) {
                    $scope.user.ds = "";
                    $scope.user.tlk = "";
                }
            });
            $scope.$watch("user.ds", function (newval, oldval) {
                if (newval != oldval &&  !$scope.skipWatch) {
                    $scope.user.tlk = "";
                }
            });
        };
        if ($scope.edit) {
            $scope.showLoading();
            userService.getUser($scope.editUserId).then(function (data) {
                $scope.user = data.data;
                $scope.user.age = checkNullEmpty($scope.user.age)?'':$scope.user.age;
                $scope.updateTags("fetch");
                $scope.user.pw = " ";
                $scope.user.cpw = $scope.user.pw;
                if($scope.user.theme == -1) {
                    $scope.theme = 1;
                } else {
                    $scope.theme = 2;
                }
                $scope.setCountry($scope.user.cnt);
                $scope.setState($scope.user.st);
                $scope.setDistrict($scope.user.ds);
                $scope.getPrimaryEntityId();
                $scope.defineWatchers();
            }).catch(function error(msg) {
                $scope.showErrorMsg(msg);
            }).finally(function (data){
                $scope.hideLoading();
            });
            $scope.uidStatus = true;
        }
        $scope.checkUserAvailability = function (userid) {
            if ($scope.edit) {
                $scope.uVisited.id = false;
            } else {
                $scope.uidVerified = false;
                userService.checkUserAvailability(userid).then(function (data) {
                    $scope.uidStatus = (data.data == true);
                    $scope.uidVerified = true;
                }).catch(function error(msg) {
                    $scope.showErrorMsg(msg);
                });
            }
        };

        $scope.checkUserSize = function(){
            if ($scope.edit) {
                $scope.uVisited.id = false;
            } else {
                $scope.uidLengthVerified = checkNotNullEmpty($scope.user) && checkNotNullEmpty($scope.user.id) && $scope.user.id.length >= 4 && $scope.user.id.length <= 20;
            }
        };

        $scope.checkCustomIDAvailability = function(customid,userId){
            if(checkNotNullEmpty(customid)) {
                $scope.ucidVerified = false;
                if(checkNullEmpty(userId)){
                    userId = null;
                }
                userService.checkCustomIDAvailability(customid, userId).then(function (data) {
                    $scope.ucidStatus = (data.data == true);
                    $scope.ucidVerified = true;
                }).catch(function error(msg) {
                    $scope.showErrorMsg(msg);
                });
            } else {
                $scope.ucidStatus=undefined;
            }
        };
        $scope.validatePassword = function () {
            if(checkNotNullEmpty($scope.user.pw)) {
                var lengthError = false;
                $scope.uPasswordInvalidType = "";
                if($scope.user.pw.length < 6 || $scope.user.pw.length > 18){
                    lengthError = true;
                    $scope.uPasswordInvalidType = "l";
                }
                var passwordMismatch = false;
                if(!lengthError && $scope.user.pw !== $scope.user.cpw) {
                    passwordMismatch = true;
                    $scope.uPasswordInvalidType = "m";
                }
                $scope.uPasswordInvalid = lengthError || passwordMismatch;
            }
        };

        $scope.validateMobilePhone= function (ll) {
            if(ll){
                $scope.invalidPhl = validateMobile($scope.user.phl);
            }
            $scope.invalidPhm = validateMobile($scope.user.phm);
        };

        $scope.validate = function () {
            var valid = !($scope.uPasswordInvalid || checkNotNullEmpty($scope.invalidPhm) || $scope.uidStatus
            || $scope.ucidStatus || !$scope.uidLengthVerified || ($scope.user.age == 0 && $scope.user.age != '') || checkNullEmpty($scope.user.ro)
            || checkNullEmpty($scope.user.fnm) || checkNullEmpty($scope.user.cnt) || checkNullEmpty($scope.user.st)
            || checkNullEmpty($scope.user.lng) || checkNullEmpty($scope.user.tz) || addUserForm.em.className.indexOf('ng-invalid-email') > -1);
            if (!valid) {
                $scope.showErrorMsg($scope.resourceBundle['form.error']);
            }
            return valid;
        };
        $scope.validateUpdate = function(){
            var valid = !(checkNotNullEmpty($scope.invalidPhm) || $scope.ucidStatus || ($scope.user.age == 0 && $scope.user.age!=''));
            if(!valid) {
                $scope.showErrorMsg($scope.resourceBundle['form.error']);
            }
            return valid;
        };
        $scope.createUser = function () {
            if ($scope.user != null) {
                $scope.updateTags("add");
                $scope.showLoading();
                if(checkNullEmpty($scope.user.age)){
                    $scope.user.age = 0;
                }
                userService.createUser($scope.user).then(function (data) {
                    $scope.resetForm();
                    $scope.showSuccess(data.data);
                }).catch(function error(msg) {
                    $scope.showErrorMsg(msg);
                }).finally(function(){
                    $scope.hideLoading();
                });
            }
        };
        $scope.updatePrimaryEntityId =  function(){
            if($scope.user != null){
                if(checkNotNullEmpty($scope.user.pk)){
                    if(checkNotNullEmpty($scope.user.pk.id)){
                        $scope.user.pk = $scope.user.pk.id;
                        $scope.setDefaultEntityId($scope.user.pk);
                    }
                } else{
                    $scope.setDefaultEntityId("");
                }
            }
        };
        $scope.getPrimaryEntityId = function(){
            if($scope.user != null){
                if(checkNotNullEmpty($scope.user.pk)){
                    entityService.get($scope.user.pk).then(function(data){
                        if(checkNotNullEmpty(data.data.id)){
                            $scope.user.pk = data.data;
                        }else{
                            $scope.user.pk = "";
                        }
                    }).catch(function error(msg){
                        $scope.showErrorMsg(msg);
                    });
                }
            }
        };
        $scope.updateUser = function () {
            if ($scope.user != null) {
                if(checkNullEmpty($scope.user.age)){
                    $scope.user.age = 0;
                }
                $scope.updateTags("update");
                $scope.loading = true;
                $scope.showLoading();
                $scope.updatePrimaryEntityId();
                userService.updateUser($scope.user, $scope.user.id).then(function (data) {
                    $scope.$back();
                    $scope.showSuccess(data.data);
                }).catch(function error(msg) {
                    $scope.showErrorMsg(msg);
                }).finally(function (){
                    $scope.loading = false;
                    $scope.hideLoading();
                });
            }
        };
        $scope.setAllVisited = function () {
            $scope.uVisited.id = true;
            $scope.uVisited.pw = true;
            $scope.uVisited.cpw = true;
            $scope.uVisited.role = true;
            $scope.uVisited.fnm = true;
            $scope.uVisited.phm = true;
            $scope.uVisited.cnt = true;
            $scope.uVisited.st = true;
            $scope.uVisited.lng = true;
            $scope.uVisited.tz = true;
            $scope.uVisited.em = true;
        };
        $scope.resetForm = function () {
            $scope.user = {lgr:-1, theme: -1};
            $scope.uVisited = {};
            $scope.uidStatus = false;
            $scope.theme = 1;
            $scope.setDomainDefault();
        };
        $scope.updateTags = function (action) {
            if ($scope.user != null) {
                if(action == 'add' || action == 'update'){
                    $scope.user.tgs = [];
                    if(checkNotNullEmpty($scope.user.tgObjs)){
                        $scope.user.tgObjs.forEach(function(d){
                            $scope.user.tgs.push(d.text);
                        });
                    }
                } else if(action == 'fetch'){
                    $scope.user.tgObjs = [];
                    if(checkNotNullEmpty($scope.user.tgs)){
                        $scope.user.tgs.forEach(function(d){
                            $scope.user.tgObjs.push({"id": d, "text": d});
                        });
                    }
                }
            }
        };
    }
]);
userControllers.controller('UserDetailsController', ['$scope', 'userService', 'configService', 'entityService','mediaService', 'requestContext',
    function ($scope, userService, configService, entityService,mediaService, requestContext) {
        var renderContext = requestContext.getRenderContext("setup.users.all.details","userId");
        $scope.eRoute = 'false';
        $scope.imageData='';
        $scope.loadimage=true;
        $scope.theme = [{key: 0, value: $scope.resourceBundle['theme.black']}, {key: 1, value:$scope.resourceBundle['theme.red']}];

        var isSupport = false;
        $scope.editRoute = function (value) {
            $scope.eRoute = value;
        };
        $scope.userId = requestContext.getParam("userId");
        $scope.user = undefined;
        $scope.userImages = [];
        $scope.ext='';
        LocationController.call(this, $scope, configService);
        $scope.isNotNullEmpty = function (val) {
            return typeof val !== 'undefined' && val != null && val != "";
        };
        $scope.getUserDetails = function (callback) {
            $scope.showLoading();
            if (checkNotNullEmpty($scope.userId)) {
                userService.getUserDetails($scope.userId).then(function (data) {
                    $scope.user = data.data;
                    $scope.getPrimaryEntityId();
                    $scope.user.cnt = $scope.getCountryNameByCode($scope.user.cnt);
                    if (checkNotNullEmpty(callback)) {
                        callback();
                    }
                    $scope.getDomainKeyImages($scope.userId);
                    $scope.isUserEditable = ($scope.currentDomain == $scope.user.sdid);
                }).catch(function error(msg) {
                    $scope.showErrorMsg(msg);
                }).finally(function (data){
                    $scope.hideLoading();
                });
            }
        };
        $scope.$watch('countries', function(countries) {
           if(!checkNullEmptyObject(countries)) {
               $scope.getUserDetails();
           }
        });
        $scope.getPrimaryEntityId = function(){
            if(checkNotNullEmpty($scope.user) && checkNotNullEmpty($scope.user.pk)){
                entityService.get($scope.user.pk).then(function(data){
                    $scope.user.pk = data.data.nm;
                }).catch(function error(msg){
                    $scope.showErrorMsg(msg);
                });
            }
        };
        $scope.getDomainKeyImages = function(userId){
            $scope.loadimage = true;
            mediaService.getDomainkeyImages(userId).then(function(data){
                $scope.userImages = data.data;
                $scope.doAddImage = false;
            }).catch(function error(msg) {
                $scope.showErrorMsg(msg);
            }).finally(function(){
                $scope.loadimage = false;
            });

        };

        $scope.enableDisableUser = function (action) {
            $scope.user.en = action == 'e';
            var isSupport = false;
            if(checkNotNullEmpty($scope.support) && action == 'd'){
                for(var i=0; i<$scope.support.length; i++){
                    if($scope.support[i].usrid == $scope.userId){
                        $scope.showWarning($scope.resourceBundle["user.disable.warning"] +" <br/> - "+ $scope.support[i].usrid);
                        isSupport = true;
                        break;
                    }
                }
            }
            function validateAdminUser(user) {
                if (!checkNullEmptyObject(user) && action == 'd') {
                    if (user.userId == $scope.userId) {
                        $scope.showWarning($scope.resourceBundle["auser.disable.warning"] + "<br/> -" + user.userId);
                        return false;
                    }
                }
            }

            function checkAdminUsers() {
                if (!checkNullEmptyObject($scope.admin)) {
                        var isValid = validateAdminUser($scope.admin.pac) &&
                            validateAdminUser($scope.admin.sac);
                        if (!isValid) {
                            return false;
                        }
                }
                return true;
            }

            if(!checkAdminUsers()){
                return;
            }
            if (!isSupport) {
                userService.enableDisableUser($scope.userId, action).then(function (data) {
                    $scope.showSuccess(data.data);
                }).catch(function error(msg) {
                    $scope.showErrorMsg(msg);
                });
            }
        };

        $scope.validateImage =function(){
            if($scope.imageData == undefined || $scope.imageData==''){
                $scope.showWarning($scope.resourceBundle['image.upload.empty.warning']);
                return false;
            }
            var filetype = $scope.imageData.filetype.split("/");
            $scope.ext=filetype[filetype.length - 1];
            if($scope.ext !='png' && $scope.ext !='jpg' && $scope.ext != 'gif' && $scope.ext !='jpeg'){
                $scope.showWarning($scope.resourceBundle['image.upload.warning']);
                return false;
            }

            var size = $scope.imageData.filesize;
            if(size > 5 * 1024 * 1024) {
                $scope.showWarning ($scope.resourceBundle['uploadsizemessage']);
                return false;
            }
            return true;
        };
        $scope.uploadImage = function(){
            $scope.showLoading();
            mediaService.uploadImage($scope.ext,$scope.userId,$scope.imageData.base64).then(function(){
                $scope.showSuccess($scope.resourceBundle['image.upload.success']);
                mediaService.getDomainkeyImages($scope.userId).then(function(data){
                    $scope.userImages = data.data;
                    $scope.doAddImage = false;
                    angular.element('#userFileupload').val(null);
                }).catch(function error(msg){
                    $scope.showErrorMsg(msg)
                })
            }).catch(function error(msg){
                $scope.showErrorMsg(msg)
            }).finally(function(){
                $scope.imageData='';
                $scope.hideLoading();
            });
        };
        $scope.removeImage = function(id){
            if (!confirm($scope.resourceBundle['image.removed.warning'] + "?")) {
                return;
            }
            $scope.showLoading();
            mediaService.removeImage(id).then(function(){
                $scope.showSuccess($scope.resourceBundle['image.delete.success']);
                mediaService.getDomainkeyImages($scope.userId).then(function(data){
                    $scope.userImages = data.data;
                    $scope.doAddImage = false;
                }).catch(function error(msg){
                    $scope.showErrorMsg(msg)
                })
            }).catch(function error(msg){
                $scope.showErrorMsg(msg)
            }).finally(function(){
                $scope.hideLoading();
            });

        };

        $scope.cancel = function(){
            $scope.imageData='';
            $scope.doAddImage = false;
        };
        $scope.setUserAccDsm = function (accDsm) {
            if(checkNotNullEmpty(accDsm)) {
                $scope.user.accDsm = accDsm;
            }
        };

        $scope.$on("requestContextChanged", function () {
            if (!renderContext.isChangeRelevant()) {
                return;
            }
            if ($scope.userId != requestContext.getParam("userId")) {
                $scope.userId = undefined;
                $scope.userId = requestContext.getParam("userId");
                $scope.getUserDetails();
            }else{
                $scope.subview = renderContext.getNextSection();
            }

        });

        $scope.forceLogout = function() {
            if(checkNotNullEmpty($scope.userId)) {
                $scope.showLoading();
                userService.forceLogoutOnMobile($scope.userId).then(function (data) {
                    $scope.showSuccess(data.data);
                }).catch(function error(msg){
                    $scope.showErrorMsg(msg.data);
                }).finally(function() {
                    $scope.hideLoading();
                });
            } else {
                $scope.showWarning("Invalid user");
            }
        }
    }
]);
userControllers.controller('UserPasswordController', ['$scope', 'userService', 'requestContext', '$window',
    function ($scope, userService, requestContext, $window) {
        $scope.userId = requestContext.getParam("userId");
        $scope.pType = requestContext.getParam("type");
        $scope.reset = $scope.pType == "reset";
        $scope.upw = {};
        $scope.sendType = "sms";
        $scope.dpwd = false;
        $scope.validatePassword = function () {
            if(checkNotNullEmpty($scope.upw.pw)) {
                var lengthError = false;
                $scope.uPasswordInvalidType = "";
                if($scope.upw.pw.length < 6 || $scope.upw.pw.length > 18) {
                    lengthError = true;
                    $scope.uPasswordInvalidType = "l";
                }
                var samePassError = false;
                if($scope.upw.opw == $scope.upw.pw) {
                    samePassError = true;
                    $scope.uPasswordInvalidType = "s";
                }
                var passMismatch = false;
                if(!lengthError && !samePassError && checkNotNullEmpty($scope.upw.cpw) && $scope.upw.pw !== $scope.upw.cpw) {
                    passMismatch = true;
                    $scope.uPasswordInvalidType = "m";
                }
                $scope.uPasswordInvalid = lengthError || samePassError || passMismatch;
            }
        };
        $scope.user = {};
        $scope.getUserDetails = function(){
            $scope.showLoading();
            userService.getUser($scope.userId).then(function (data) {
                $scope.user = data.data;
            }).catch(function error(msg) {
                $scope.showErrorMsg(msg);
            }).finally(function(){
                $scope.hideLoading();
            });
        };
        $scope.getUserDetails();
        $scope.updatePassword = function () {
            $scope.loading = true;
            $scope.showLoading();
            userService.updateUserPassword($scope.upw, $scope.userId).then(function (data) {
                if (data.data == "\"invalid\"") {
                    $scope.iopw = true;
                } else {
                    $scope.showSuccess(data.data);
                    $window.location.href = '#/setup/users/all/details?userId=' + $scope.userId;
                }
            }).catch(function error(msg) {
                $scope.showErrorMsg(msg);
            }).finally(function (){
                $scope.loading = false;
                $scope.hideLoading();
            });
        };
        $scope.resetPassword = function () {
            $scope.dpwd = true;
            userService.resetUserPassword($scope.userId, $scope.sendType).then(function (data) {
                $scope.showSuccess(data.data);
                $window.location.href = '#/setup/users/all/details?userId=' + $scope.userId;
            }).catch(function error(msg) {
                $scope.showErrorMsg(msg);
                $scope.dpwd = false;
            });
        }
    }
]);
userControllers.controller('UserMessageController', ['$scope', 'userService', 'configService', '$location', 'requestContext','APPTYPE',
    function ($scope, userService, configService, $location, requestContext, APPTYPE) {
        LanguageController.call(this, $scope, configService);
        ListingController.call(this, $scope, requestContext, $location);

        $scope.init = function() {
            $scope.msg = {};
            $scope.msg.type = 'sms';
            $scope.msg.template = 'txt';
            $scope.remLength = 160;
            $scope.msg.text = '';
            $scope.appType = APPTYPE.JAVA_FEATURE_PHONE_APP;
            $scope.lng = 'en';
            $scope.showLng = true;
            $scope.msg.userIds = requestContext.getParam("userIds");
            $scope.mVisitedText = false;
            $scope.showLng = true;
        };
        $scope.setMsgRemLength = function () {
            var baseLength;
            if ($scope.msg.template === "txt") {
                baseLength = 160;
            } else {
                baseLength = 50;
            }
            if (checkNotNullEmpty($scope.msg.text)) {
                $scope.remLength = baseLength - $scope.msg.text.length;
            } else {
                $scope.remLength = baseLength;
            }
            if ($scope.remLength < 0) {
                $scope.msg.text = $scope.msg.text.substr(0, baseLength);
                $scope.remLength = 0;
            }
        };

        $scope.fetch = function () {
            $scope.loading = true;
            $scope.showLoading();
            userService.getUsersDetailByIds($scope.msg.userIds).then(function (data) {
                $scope.users = data.data;
            }).catch(function error(msg) {
                $scope.showErrorMsg(msg);
            }).finally(function(){
                $scope.loading = false;
                $scope.hideLoading();
            });
        };
        $scope.sendUserMessage = function () {
            if ($scope.msg.template == 'wappush' && $scope.msg.pushURL == undefined)  {
                $scope.showErrorMsg( $scope.resourceBundle['general.systemconfigmsg']);
            } else {
                $scope.showLoading();
                if($scope.msg.template == 'wappush' &&  $scope.appType == APPTYPE.ANDROID_SMART_PHONE_APP){
                    $scope.msg.template = 'txt';
                }
                userService.sendUserMessage($scope.msg).then(function (data) {
                    $scope.reset();
                    $scope.showSuccess(data.data);
                }).catch(function error(msg) {
                    $scope.showErrorMsg(msg);
                }).finally(function () {
                    $scope.hideLoading();
                });
            }
        };

        $scope.updateAppURL = function () {
            if (checkNotNullEmpty($scope.appurls)) {
                if ($scope.appType == APPTYPE.ANDROID_SMART_PHONE_APP) {
                    $scope.msg.pushURL = $scope.appurls.android;
                } else {
                    var j2meAppKey = $scope.appType + '_' + $scope.lng;
                    $scope.msg.pushURL = $scope.appurls[j2meAppKey];
                }
                $scope.setDefaultMessage();
                // Error check - appurls present but the push url for the specified app not present. Show an error message.
                if ($scope.msg.template == 'wappush' && checkNullEmpty($scope.msg.pushURL)) {
                    $scope.showErrorMsg($scope.resourceBundle['general.systemconfigmsg']);
                }
            } else {
                if ($scope.msg.template == 'wappush') {
                    // Only if the mobile application push radio button is selected, show the error message.
                    $scope.showErrorMsg($scope.resourceBundle['general.systemconfigmsg']);
                }
            }
        };

        $scope.setDefaultMessage = function () {
            if ($scope.msg.template == "txt") {
                $scope.msg.text = '';
                $scope.remLength = 160;
            } else {
                if ($scope.appType == APPTYPE.ANDROID_SMART_PHONE_APP) {
                    $scope.msg.text = 'Download the mobile application: ' + $scope.msg.pushURL;
                    $scope.remLength = 160 - $scope.msg.text.length;
                } else {
                    $scope.msg.text = 'Download the mobile application';
                    $scope.remLength = 50 - $scope.msg.text.length;
                }
                if (checkNullEmpty($scope.appurls) || checkNullEmpty($scope.msg.pushURL)) {
                    $scope.showErrorMsg($scope.resourceBundle['general.systemconfigmsg']);
                }
            }
        };
        $scope.getGeneralConfig = function(initCall) {
            if(initCall){
                $scope.showLoading();
                configService.getGeneralConfig().then(function (data) {
                    if (data) {
                        $scope.generalconfig = angular.fromJson(data.data);
                        if($scope.generalconfig){
                            $scope.appurls = $scope.generalconfig.appurls;
                            if(checkNotNullEmpty($scope.appurls.android)) {
                                $scope.appType = APPTYPE.ANDROID_SMART_PHONE_APP;
                                $scope.showLng = false;
                            }
                        }

                        $scope.updateAppURL();
                    }
                }).catch(function error(msg) {
                    $scope.showErrorMsg(msg);
                }).finally(function () {
                    $scope.loading = false;
                    $scope.hideLoading();
                });
            } else {
                $scope.updateAppURL();
            }
        };
        $scope.reset = function(initCall) {
            $scope.init();
            $scope.getGeneralConfig(initCall);
            if (!checkNotNullEmpty($scope.msg.userIds)) {
                $scope.allUsers = true;
            }
        };
        $scope.resetAndFetch = function(initCall){
            $scope.reset(initCall);
            if (!$scope.allUsers) {
                $scope.fetch();
            }
        };
        $scope.resetAndFetch(true);
        $scope.toggleLngDivAndUpAppURL = function() {
            if ( $scope.appType == APPTYPE.ANDROID_SMART_PHONE_APP ) {
                $scope.showLng = false;
            } else if ( $scope.appType == APPTYPE.JAVA_FEATURE_PHONE_APP ) {
                $scope.showLng = true;
            }
            $scope.updateAppURL();
        }
    }
]);
userControllers.controller('UsersMessageStatusController', ['$scope', '$location', 'userService', 'requestContext',
    function ($scope, $location, userService, requestContext) {
        $scope.filtered = {};
        ListingController.call(this, $scope, requestContext, $location);
        $scope.init = function(){};
        $scope.fetch = function () {
            $scope.loading = true;
            $scope.showLoading();
            userService.getUsersMessageStatus($scope.offset, $scope.size).then(function (data) {
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
    }
]);
userControllers.controller('UserRouteListController', ['$scope', 'entityService', 'domainCfgService', 'requestContext', '$location',
    function ($scope, entityService, domainCfgService, requestContext, $location) {
        ListingController.call(this, $scope, requestContext, $location);
        $scope.rData = undefined;
        $scope.loading = false;
        $scope.mSize = requestContext.getParam("s") || 50;
        var offset = 0;
        $scope.getUserEntities = function () {
            $scope.loading = true;
            $scope.showLoading();
            if($scope.mSize != $scope.size){
                $scope.mSize = $scope.size;
                $scope.mOffset = 0;
                $scope.rData = undefined;
            }
            var size = $scope.mSize;
            if(checkNotNullEmpty($scope.rData)){
                offset = parseInt(offset) + parseInt(size);
            }
            if(checkNotNullEmpty($scope.allRTags)){
                size = undefined;
                offset = undefined;
            }
            entityService.getUserEntities($scope.userId,size,offset).then(function (data) {
                if(checkNotNullEmpty(data.data.results)) {
                    var append = false;
                    if (checkNotNullEmpty($scope.rData)) {
                        $scope.rData = angular.copy($scope.rData).concat(data.data.results);
                        append = true;
                    } else {
                        $scope.rData = data.data.results;
                    }
                    $scope.setResults(data.data);
                    if ($scope.rData != undefined) {
                        addOrderSno($scope.rData);
                    }
                    if(append){
                        appendOriginalCopy(data.data.results);
                    } else {
                        $scope.setOriginalCopy($scope.rData);
                    }
                }
            }).catch(function error(msg) {
                $scope.showErrorMsg(msg);
            }).finally(function(){
                $scope.loading = false;
                $scope.hideLoading();
            });
        };
        $scope.setOriginalCopy = function(data,resetAll) {
            $scope.orData = angular.copy(data);
            if(resetAll){
                $scope.rData = angular.copy(data);
            }
            $scope.$broadcast("orDataChanged");
        };
        function appendOriginalCopy(data) {
            $scope.orData = $scope.orData.concat(data);
            addOrderSno($scope.orData,true);
            $scope.$broadcast("orDataAppend");
        }
        function addOrderSno(rData,isOrig) {
            setSnoByTags(rData,isOrig);
            rData.forEach(function (data) {
                if(!isOrig && data.osno == 0) {
                    data.osno = data.sno;
                }
            });
            if(!isOrig){
                setCountByTags();
            }
        }
        function checkTagExist(tag){
            return $scope.allRTags.some(function (data) {
                return tag == data;
            });
        }
        function setSnoByTags(rData,isOrig) {
            var tagIndex = {};
            var isEmptyRoot = checkNullEmpty($scope.allRTags);
            rData.forEach(function (data) {
                if(isEmptyRoot || !checkTagExist(data.rt)) {
                    data.rt = '--notag--';
                }
                if (checkNullEmpty(tagIndex[data.rt])) {
                    tagIndex[data.rt] = 1;
                }
                if(!isOrig && data.osno == 0) {
                    data.sno = tagIndex[data.rt];
                }
                tagIndex[data.rt] = tagIndex[data.rt] + 1;
            });
        }
        function setCountByTags() {
            $scope.countTags = {};
            $scope.rData.forEach(function (data) {
                var rt = checkNullEmpty(data.rt) ? "--notag--" : data.rt;
                if (checkNullEmpty($scope.countTags[rt])) {
                    $scope.countTags[rt] = 1;
                } else {
                    $scope.countTags[rt] = $scope.countTags[rt] + 1;
                }
            });
        }
        $scope.init = function (isFirstInit) {
            $scope.vw = requestContext.getParam("vw") || 't';
            $scope.tag = '--notag--';
            $scope.showLoading();
            domainCfgService.getRouteTagsCfg().then(function (data) {
                $scope.allRTags = data.data.tags;
                if(isFirstInit) {
                    if(requestContext.getParam("o") > 0){
                        $scope.offset = 0;
                    } else {
                        $scope.getUserEntities();
                    }
                }
            }).catch(function error(msg) {
                $scope.showErrorMsg(msg);
            }).finally(function(){
                $scope.hideLoading();
            });
        };
        $scope.init(true);
        $scope.resetFetch = function(){
            $scope.rData = undefined;
            $scope.mOffset = 0;
            if ($scope.offset == 0) {
                $scope.fetch();
            }
            $scope.offset = 0;
            offset = 0;
        };
        $scope.fetch = function() {
            $scope.getUserEntities();
        };
        $scope.switchVw = function(sourceVw) {
            if (confirm($scope.resourceBundle['changes.lost'])) {
                $scope.rData = angular.copy($scope.orData);
            } else {
                $scope.vw = sourceVw;
            }
        }
    }
]);
userControllers.controller('ManagedListTableController', ['$scope', 'entityService',
    function ($scope, entityService) {
        $scope.filtered = [];
        $scope.selectAll = function (newVal) {
            $scope.filtered.forEach(function (item) {
                item.selected = newVal;
            });
        };
        function resetOrderSno() {
            var index = 1;
            $scope.filtered.forEach(function (data) {
                data.osno = index++;
                data.ri = data.osno;
            })
        }
        $scope.resetAllEditFlags = function () {
            $scope.filtered.forEach(function (data) {
                data.edit = false;
            });
        };
        $scope.setPosition = function (index) {
            $scope.filtered[index].esno = '';
            $scope.resetAllEditFlags();
            $scope.filtered[index].edit = true;
        };
        $scope.updatePosition = function (index) {
            $scope.filtered[index].edit = false;
            var no = $scope.filtered[index].esno;
            if (checkNotNullEmpty(no)) {
                var nIndex = parseInt(no) - 1;
                if (nIndex >= $scope.filtered.length) {
                    $scope.moveToPos(index, $scope.filtered.length - 1);
                } else if (nIndex <= 0) {
                    $scope.moveToPos(index, 0);
                } else {
                    $scope.moveToPos(index, nIndex);
                }
                $scope.routeUpdated = true;
            }
        };
        $scope.cancelPosition = function (index) {
            $scope.filtered[index].edit = false;
        };
        $scope.reorder = function (index) {
            var newIndex = $scope.filtered[index].sno - 1;
            $scope.moveToPos(index, newIndex);
        };
        $scope.rowHighlight = function (index) {
            $scope.filtered[index].rhc = true;
            setTimeout(function () {
                $scope.$apply(function () {
                    $scope.filtered[index].rhc = false;
                });
            }, 1500);
        };
        $scope.moveUp = function (index) {
            if (index > 0) {
                var t = $scope.filtered[index];
                $scope.filtered[index] = $scope.filtered[index - 1];
                $scope.filtered[index - 1] = t;
                $scope.rowHighlight(index - 1);
                resetOrderSno();
                $scope.routeUpdated = true;
            }
        };
        $scope.moveDown = function (index) {
            if (index < $scope.filtered.length - 1) {
                var t = $scope.filtered[index];
                $scope.filtered[index] = $scope.filtered[index + 1];
                $scope.filtered[index + 1] = t;
                $scope.rowHighlight(index + 1);
                resetOrderSno();
                $scope.routeUpdated = true;
            }
        };
        $scope.moveToPos = function (index, newIndex) {
            if (index > newIndex) { //move up
                var t = $scope.filtered[index];
                for (var i = index; i > newIndex; i--) {
                    $scope.filtered[i] = $scope.filtered[i - 1];
                }
                $scope.filtered[newIndex] = t;
            } else { //move down
                t = $scope.filtered[index];
                for (i = index; i < newIndex; i++) {
                    $scope.filtered[i] = $scope.filtered[i + 1];
                }
                $scope.filtered[newIndex] = t;
            }
            $scope.rowHighlight(newIndex);
            resetOrderSno();
        };
        $scope.updateManagedRouteOrder = function () {
            var isRTAvailable = checkNotNullEmpty($scope.allRTags);
            $scope.showLoading();
            entityService.updateManagedEntityOrder($scope.rData, $scope.userId, isRTAvailable).then(function (data) {
                $scope.routeUpdated = false;
                $scope.resetFetch();
                $scope.showSuccess(data.data);
            }).catch(function error(msg) {
                $scope.showErrorMsg(msg);
            }).finally(function(){
                $scope.hideLoading();
            });
        };
        $scope.$watch("vw", function (newVal) {
            if(newVal == 'm' && $scope.routeUpdated) {
                $scope.switchVw('t');
            }
        });
    }
]);
userControllers.controller('ManagedEntityRouteMapCtrl', ['$scope', 'mapService', 'entityService', 'domainCfgService', 'invService', 'trnService', 'uiGmapGoogleMapApi',
    function ($scope, mapService, entityService, domainCfgService, invService, trnService, uiGmapGoogleMapApi) {
        function trimData(data) {
            var d = {};
            d.lt = data.lt;
            d.ln = data.ln;
            d.nm = data.nm;
            d.loc = data.loc;
            d.sno = data.sno;
            d.osno = data.osno;
            d.id = data.id;
            d.ri = data.ri;
            d.rt = data.rt;
            return d;
        }

        uiGmapGoogleMapApi.then(function () {
            initMap();
        });

        function trimNoGeoEntity() {
            $scope.noGeoData = [];
            var noGeoCnt = [];
            var lmrData = [];
            $scope.mrData.forEach(function(data) {
                data = trimData(data);
                if(data.lt == 0 && data.ln == 0){
                    $scope.noGeoData.push(data);
                    if(checkNullEmpty(noGeoCnt[data.rt])){
                        noGeoCnt[data.rt] = 1;
                    }else{
                        noGeoCnt[data.rt] += 1;
                    }
                } else {
                    if(checkNotNullEmpty(noGeoCnt[data.rt])) {
                        data.osno = data.osno - noGeoCnt[data.rt];
                    }
                    lmrData.push(data);
                }
            });
            $scope.mrData = lmrData;
        }

        function initMap() {
            $scope.mrData = angular.copy($scope.orData);
            $scope.lmap = angular.copy($scope.map);
            $scope.lmap.options = {scrollwheel: false};
            $scope.lmap.control = {};
            var lineCoordinates = [];
            var lineCoordinatesIndex = [];
            var lineSymbol = undefined;
            $scope.today = new Date();
            trimNoGeoEntity();
        }

        $scope.addLineCoord = function (marker,action) {
            if(!$scope.showActual && (checkNullEmpty($scope.allRTags) || $scope.tag != '--notag--')) {
                var markerCoord = marker.model.osno - 1;
                var ind = -1;
                var exists = lineCoordinatesIndex.some(function (coord) {
                    ind++;
                    return markerCoord === coord;
                });
                if (exists) {
                    var con = confirm($scope.resourceBundle['routingremovefromroutemsg']);
                    if (!con) {
                        return;
                    }
                    var sl = 1;
                    if (lineCoordinates[0] === lineCoordinates[1]) {
                        sl = 2;
                    }
                    lineCoordinates.splice(ind, sl);
                    $scope.mFiltered[lineCoordinatesIndex[ind]].icon = null;
                    lineCoordinatesIndex.splice(ind, sl);
                    if (lineCoordinates.length === 1) {
                        lineCoordinates.push(lineCoordinates[0]);
                        lineCoordinatesIndex.push(lineCoordinatesIndex[0]);
                    }
                } else {
                    marker.position = new google.maps.LatLng(marker.model.lt, marker.model.ln);
                    if (lineCoordinates.length === 0) {
                        lineCoordinates.push(marker.getPosition());
                        lineCoordinatesIndex.push(marker.model.osno - 1);
                    }
                    if (lineCoordinates[0] === lineCoordinates[1]) {
                        lineCoordinates.splice(1, 1);
                        lineCoordinatesIndex.splice(1, 1);
                    }
                    lineCoordinates.push(marker.getPosition());
                    lineCoordinatesIndex.push(marker.model.osno - 1);
                }
                $scope.setMarkerIcons();
                if (checkNotNullEmpty($scope.lmap.control.getGMap)) {
                    $scope.drawPolyline();
                }
                if (action == 'click') {
                    $scope.routeUpdated = true;
                }
            }
        };
        $scope.setMarkerIcons = function () {
            var ind = 1;
            if (lineCoordinates.length != 0 && (lineCoordinatesIndex[0] === lineCoordinatesIndex[1])) {
                $scope.mFiltered[lineCoordinatesIndex[0]].icon = mapService.getMarkerIcon(1);
            } else {
                lineCoordinatesIndex.forEach(function (index) {
                    $scope.mFiltered[index].icon = mapService.getMarkerIcon(ind++);
                });
            }
        };
        function clearPolyline(){
            if (checkNotNullEmpty($scope.pline)) {
                $scope.pline.setMap(null);
            }
        }
        $scope.drawPolyline = function () {
            clearPolyline();
            $scope.pline = new google.maps.Polyline({
                path: lineCoordinates,
                strokeColor: '#0000FF',
                icons: [{
                    icon: lineSymbol,
                    offset: '100%'
                }],
                map: $scope.lmap.control.getGMap()
            });
        };
        $scope.$on("orDataChanged", function () {
            $scope.mrData = angular.copy($scope.orData);
            trimNoGeoEntity();
            $scope.filterDataByTag($scope.tag);
        });
        $scope.$on("orDataAppend", function () {
            $scope.mrData = angular.copy($scope.orData);
            trimNoGeoEntity();
            updateOsno();
            $scope.filterDataByTag($scope.tag);
        });
        function updateOsno() {
            $scope.setOrderSno();
            $scope.mFiltered.forEach(function(mf){
                $scope.mrData.some(function(md){
                    if(md.id == mf.id){
                        md.osno = mf.osno;
                        return true;
                    }
                });
            });
        }
        $scope.filterDataByTag = function (newTag) {
            $scope.showMap = false;
            clearPolyline();
            var isActual = $scope.showActual;
            if($scope.showActual) {
                $scope.hideActualRoute();
            }
            $scope.mFiltered = [];
            $scope.mrData.forEach(function (data) {
                if (data.rt == newTag) {
                    $scope.mFiltered[data.osno - 1] = data;
                    $scope.mFiltered[data.osno - 1].options = {title:data.nm + ", " + data.loc};
                }
            });
            lineCoordinates = [];
            lineCoordinatesIndex = [];
            if(checkNullEmpty($scope.allRTags) || newTag != '--notag--') {
                $scope.mFiltered.forEach(function (data) {
                    if(data.ri != 2147483647) {
                        var lt = data.lt || 0;
                        var ln = data.ln || 0;
                        var marker = new google.maps.Marker({
                            position: new google.maps.LatLng(lt, ln),
                            model: {osno: data.osno,lt:lt,ln:ln}
                        });
                        $scope.addLineCoord(marker);
                    }
                });
            }
            if(isActual && $scope.mFiltered.length > 0) {
                addActualRoute();
            } else {
                mapService.convertLnLt($scope.mFiltered, $scope.lmap);
            }
            //For pagination
            $scope.filtered = $scope.mFiltered;
            $scope.numFound = $scope.$parent.numFound;
            setTimeout(function () {
                $scope.$apply(function () {
                    $scope.showMap = true;
                });
            }, 200);
        };
        $scope.setOrderSno = function (newVal, callback) {
            var ind = 1;
            if(lineCoordinates.length != 0 && (lineCoordinatesIndex[0] === lineCoordinatesIndex[1])){
                $scope.mFiltered[lineCoordinatesIndex[0]].osno = ind++;
                $scope.mFiltered[lineCoordinatesIndex[0]].ri = ind - 1;
            } else {
                lineCoordinatesIndex.forEach(function (index) {
                    $scope.mFiltered[index].osno = ind++;
                    $scope.mFiltered[index].ri = ind - 1;
                });
            }
            if (ind <= $scope.mFiltered.length) {
                var li = 0;
                $scope.mFiltered.forEach(function (data) {
                    if (lineCoordinatesIndex.indexOf(li++) == -1) {
                        data.osno = ind++;
                        data.ri = 2147483647;
                    }
                });
            }
            if (checkNotNullEmpty(callback)) {
                callback(newVal);
            }
        };
        $scope.$watch('lmap.control.getGMap', function (newVal) {
            if (checkNotNull(newVal)) {
                if(checkNullEmpty(lineSymbol)) {
                    lineSymbol = mapService.getCloseArrowSymbol();
                }
                $scope.drawPolyline();
                if($scope.showActual) {
                    $scope.drawActualPolyline();
                }
            }
        });
        $scope.$watch('tag', function (newVal) {
            if(checkNullEmpty($scope.mFiltered)){
                $scope.filterDataByTag(newVal);
            }else {
                $scope.setOrderSno(newVal, $scope.filterDataByTag);
            }
        });
        $scope.updateManagedRouteOrderViaMap = function () {
            var isRTAvailable = checkNotNullEmpty($scope.allRTags);
            $scope.update = function () {
                $scope.showLoading();
                entityService.updateManagedEntityOrder($scope.mrData, $scope.userId, isRTAvailable).then(function (data) {
                    $scope.routeUpdated = false;
                    $scope.resetFetch();
                    $scope.showSuccess(data.data);
                }).catch(function error(msg) {
                    $scope.showErrorMsg(msg);
                }).finally(function(){
                    $scope.hideLoading();
                });
            };
            $scope.setOrderSno('', $scope.update);
        };
        $scope.$watch("vw", function (newVal) {
            if(newVal == 't' && $scope.routeUpdated) {
                $scope.switchVw('m');
            }
        });
        domainCfgService.getRouteType().then(function(data){
            $scope.arType = data.data.replace(/"/g,'')
        });
        $scope.almap = angular.copy($scope.map);
        $scope.almap.options = {scrollwheel: false};
        $scope.almap.control = {};

        function addDaysToDate(date,days){
            return new Date(new Date().setDate(date.getDate() + days));
        }
        var actualLineCoordinates = [];
        var dt = new Date();
        $scope.from = addDaysToDate(dt,-7);
        $scope.to = dt;

        $scope.drawActualPolyline = function () {
            $scope.apline = new google.maps.Polyline({
                path: actualLineCoordinates,
                strokeColor: '#FF0000',
                icons: [{
                    icon: lineSymbol,
                    offset: '100%'
                }],
                map: $scope.lmap.control.getGMap()
            });
        };
        $scope.actualRouteStartIndex = undefined;
        var originLoc = undefined;
        var destinationLoc = undefined;
        var wayPoints = [];
        $scope.showActualRoute = function() {
            var service;
            if($scope.arType == 'orders') {
                service = invService
            } else {
                service = trnService;
            }
            service.getActualRoute($scope.userId, formatDate2Url($scope.from), formatDate2Url(addDaysToDate($scope.to, 1))).then(function (data) {
                $scope.actualRoute = data.data;
                addActualRoute(true);
            }).catch(function error(msg) {
                $scope.showErrorMsg(msg);
            });
        };
        function addActualRoute(redrawRoute) {
            $scope.actualRouteStartIndex = $scope.mFiltered.length;
            var ind = 1;
            for (var i = $scope.actualRoute.length - 1; i >= 0; i--) {
                var d = $scope.actualRoute[i];
                var location = new google.maps.LatLng(d.latitude, d.longitude);
                actualLineCoordinates.push(location);
                d.sno = 'a' + ind;
                d.icon = mapService.getMarkerIcon(ind++, "FFFF00");
                d.options = {title: d.title};
                $scope.mFiltered.push(d);
                if(i == $scope.actualRoute.length-1){
                    originLoc = location;
                }else if(i == 0){
                    destinationLoc = location;
                } else {
                    if(wayPoints.length < 8){
                        wayPoints.push( { location: location, stopover: true } );
                    }
                }
            }
            mapService.convertLnLt($scope.mFiltered, $scope.lmap);
            $scope.drawActualPolyline();
            $scope.showRoute = checkNotNullEmpty(originLoc) && checkNotNullEmpty(destinationLoc);
            if(redrawRoute){
                if (checkNotNull($scope.almap.control.getGMap)) {
                    drawRoute();
                }
            }
            $scope.showActual = true;
        }
        $scope.$watch('almap.control.getGMap', function (newVal) {
            if (checkNotNull(newVal)) {
                drawRoute();
            }
        });
        function drawRoute () {
            $scope.distance = undefined;
            $scope.placeVisited = undefined;
            var directionsDisplay = new google.maps.DirectionsRenderer();
            directionsDisplay.setMap($scope.almap.control.getGMap());
            var directionsService = new google.maps.DirectionsService();
            if(checkNotNullEmpty(originLoc) && checkNotNullEmpty(destinationLoc)) {
                var request = {
                    origin: originLoc,
                    destination: destinationLoc,
                    waypoints: wayPoints,
                    travelMode: google.maps.TravelMode.DRIVING
                };
                directionsService.route(request, function (response, status) {
                    if (status == google.maps.DirectionsStatus.OK) {
                        directionsDisplay.setDirections(response);
                        var dist = 0;
                        response.routes[0].legs.forEach(function (data) {
                            dist += parseInt(data.distance.value);
                        });
                        $scope.distance = dist / 1000 + ' km(s)';
                        $scope.placeVisited = response.routes[0].legs.length + 1;
                    } else if (status == google.maps.DirectionsStatus.ZERO_RESULTS) {
                        $scope.showRoute = false;
                    }
                });
            }
        }
        $scope.hideActualRoute = function() {
            $scope.showActual = false;
            if (checkNotNullEmpty($scope.apline)) {
                $scope.apline.setMap(null);
            }
            $scope.mFiltered.splice($scope.actualRouteStartIndex,$scope.mFiltered.length - $scope.actualRouteStartIndex);
            $scope.actualRouteStartIndex = undefined;
            originLoc = undefined;
            destinationLoc = undefined;
            actualLineCoordinates = [];
            wayPoints = [];
        }
    }
]);
userControllers.controller('RelationEditController', ['$scope', 'entityService', 'domainCfgService', 'requestContext', '$location',
    function ($scope, entityService, domainCfgService, requestContext, $location) {
        $scope.leftFiltered = [];
        $scope.rightFiltered = [];
        function checkTagExist(tag){
            return $scope.allRTags.some(function (data) {
                return tag == data;
            });
        }
        function setSnoByTags() {
            var tagIndex = {};
            var isEmptyRoot = checkNullEmpty($scope.allRTags);
            $scope.rData.forEach(function (data) {
                if(isEmptyRoot || !checkTagExist(data.rt)) {
                    data.rt = '--notag--';
                }
                if (checkNullEmpty(tagIndex[data.rt])) {
                    tagIndex[data.rt] = 1;
                }
                data.sno = tagIndex[data.rt];
                tagIndex[data.rt] = tagIndex[data.rt] + 1;
            });
        }
        function addOrderSno() {
            setSnoByTags();
            $scope.rData.forEach(function (data) {
                data.osno = data.sno;
            });
            setCountByTags();
        }
        function setCountByTags() {
            $scope.countTags = {};
            $scope.rData.forEach(function (data) {
                var rt = checkNullEmpty(data.rt) ? "--notag--" : data.rt;
                if (checkNullEmpty($scope.countTags[rt])) {
                    $scope.countTags[rt] = 1;
                } else {
                    $scope.countTags[rt] = $scope.countTags[rt] + 1;
                }
            });
        }
        $scope.getUserEntities = function () {
            $scope.showLoading();
            entityService.getUserEntities($scope.userId).then(function (data) {
                $scope.rData = data.data.results;
                if(checkNotNullEmpty($scope.rData)) {
                    addOrderSno();
                }
            }).catch(function error(msg) {
                $scope.showErrorMsg(msg);
            }).finally(function(){
                $scope.hideLoading();
            });
        };
        $scope.init = function () {
            $scope.entityName = $scope.userId;
            $scope.showLoading();
            domainCfgService.getRouteTagsCfg().then(function (data) {
                $scope.allRTags = data.data.tags;
                if (checkNotNullEmpty($scope.allRTags) && checkNotNullEmpty($scope.allRTags[0])) {
                    $scope.tag = $scope.allRTags[0];
                }
                $scope.getUserEntities();
            }).catch(function error(msg) {
                $scope.showErrorMsg(msg);
            }).finally(function(){
                $scope.hideLoading();
            });
        };
        $scope.init();
        ListingController.call(this, $scope, requestContext, $location);
        $scope.selectAll = function (newVal, side) {
            if (side === 'l') {
                $scope.leftFiltered.forEach(function (item) {
                    item.selected = newVal;
                });
            } else if (side === 'r') {
                $scope.rightFiltered.forEach(function (item) {
                    item.selected = newVal;
                });
            }
        };
        $scope.moveRight = function () {
            var osnoIndex = $scope.rightFiltered.length + 1;
            $scope.leftFiltered.forEach(function (item) {
                if (item.selected) {
                    item.rt = $scope.tag;
                    item.osno = osnoIndex++;
                    item.selected = false;
                    item.hc = true;
                    $scope.routeUpdated = true;
                }
            });
        };
        $scope.moveLeft = function () {
            $scope.rightFiltered.forEach(function (item) {
                if (item.selected) {
                    item.rt = "--notag--";
                    item.selected = false;
                    item.hc = false;
                    $scope.routeUpdated = true;
                }
            });
            resetOrderSno();
        };
        function resetOrderSno() {
            var index = 1;
            $scope.rightFiltered.forEach(function (data) {
                if (data.rt != "--notag--") {
                    data.osno = index++;
                }
            });
        }
        $scope.moveUp = function (index) {
            if (index > 0) {
                var t = $scope.rightFiltered[index];
                $scope.rightFiltered[index] = $scope.rightFiltered[index - 1];
                $scope.rightFiltered[index - 1] = t;
                $scope.rowHighlight(index - 1);
                resetOrderSno();
                $scope.routeUpdated = true;
            }
        };
        $scope.moveDown = function (index) {
            if (index < $scope.rightFiltered.length - 1) {
                var t = $scope.rightFiltered[index];
                $scope.rightFiltered[index] = $scope.rightFiltered[index + 1];
                $scope.rightFiltered[index + 1] = t;
                $scope.rowHighlight(index + 1);
                resetOrderSno();
                $scope.routeUpdated = true;
            }
        };
        $scope.rowHighlight = function (index) {
            $scope.rightFiltered[index].rhc = true;
            setTimeout(function () {
                $scope.$apply(function () {
                    $scope.rightFiltered[index].rhc = false;
                });
            }, 1500);
        };
        $scope.moveToPos = function (index, newIndex) {
            if (index > newIndex) { //move up
                var t = $scope.rightFiltered[index];
                for (var i = index; i > newIndex; i--) {
                    $scope.rightFiltered[i] = $scope.rightFiltered[i - 1];
                }
                $scope.rightFiltered[newIndex] = t;
            } else { //move down
                t = $scope.rightFiltered[index];
                for (i = index; i < newIndex; i++) {
                    $scope.rightFiltered[i] = $scope.rightFiltered[i + 1];
                }
                $scope.rightFiltered[newIndex] = t;
            }
            $scope.rowHighlight(newIndex);
            resetOrderSno();
        };
        $scope.setPosition = function (index) {
            $scope.rightFiltered[index].esno = '';
            $scope.resetAllEditFlags();
            $scope.rightFiltered[index].edit = true;
        };
        $scope.resetAllEditFlags = function () {
            $scope.rightFiltered.forEach(function (data) {
                data.edit = false;
            });
        };
        $scope.cancelPosition = function (index) {
            $scope.rightFiltered[index].edit = false;
        };
        $scope.updatePosition = function (index) {
            $scope.rightFiltered[index].edit = false;
            var no = $scope.rightFiltered[index].esno;
            if (checkNotNullEmpty(no)) {
                var nIndex = parseInt(no) - 1;
                if (nIndex >= $scope.rightFiltered.length) {
                    $scope.moveToPos(index, $scope.rightFiltered.length - 1);
                } else if (nIndex <= 0) {
                    $scope.moveToPos(index, 0);
                } else {
                    $scope.moveToPos(index, nIndex);
                }
                $scope.routeUpdated = true;
            }
        };
        function setRouteIndexToOsno(){
            $scope.rData.forEach(function(data){
                data.ri = data.osno;
            });
        }
        $scope.updateManagedRouteOrder = function () {
            var isRTAvailable = checkNotNullEmpty($scope.allRTags);
            setRouteIndexToOsno();
            $scope.showLoading();
            entityService.updateManagedEntityOrder($scope.rData, $scope.userId, isRTAvailable).then(function (data) {
                setCountByTags();
                $scope.routeUpdated = false;
                $scope.showSuccess(data.data);
            }).catch(function error(msg) {
                $scope.showErrorMsg(msg);
            }).finally(function(){
                $scope.hideLoading();
            });
        };
    }
]);

userControllers.controller('UserDomainListController', ['$scope','$route','requestContext', 'userService',
    function ($scope, $route, requestContext, userService) {
        $scope.domainsToAdd = undefined;
        $scope.domainIdsToAdd = [];
        $scope.addDomains = function(){
            $scope.getDomainIdsToAdd();
            if ($scope.domainIdsToAdd.length == 0){
                $scope.showWarning("Please choose domains with which the user should be associated.");
                return;
            }
            userService.addAccessibleDomains($scope.userId, $scope.domainIdsToAdd).then(function(data){
                var resp = data.data;
                if(resp.respMsg.indexOf("Warning: ") != -1)
                    $scope.showWarning(resp.respMsg);
                else
                    $scope.showSuccess(resp.respMsg);
                $scope.domainsToAdd = undefined;
                $scope.domainIdsToAdd = [];
                $scope.accDsm = resp.accDsm;
                $scope.setUserAccDsm(resp.accDsm);
                $scope.$broadcast("accessibleDomainsChanged");
            }).catch(function error(msg){
                $scope.showErrorMsg(msg);
            }).finally(function(){
                $scope.hideLoading();
            });
        };
        $scope.removeDomain = function (domainId) {
            if (domainId != null) {
                if (!confirm($scope.resourceBundle['domain.child.remove'] + '?')) {
                    return;
                }
                userService.removeAccessibleDomain($scope.userId, domainId).then(function (data) {
                    var resp = data.data;
                    $scope.showSuccess(resp.respMsg);
                    $scope.accDsm=resp.accDsm;
                    $scope.setUserAccDsm(resp.accDsm);
                    $scope.$broadcast("accessibleDomainsChanged");
                }).catch(function error(msg) {
                    $scope.showWarning($scope.resourceBundle['domain.delete.error'] + msg);
                })
            }
        };
        $scope.getDomainIdsToAdd = function(){
            for (var item in $scope.domainsToAdd) {
                $scope.domainIdsToAdd.push($scope.domainsToAdd[item].id);
            }
        };
    }
]);

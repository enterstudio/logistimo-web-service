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
 * Created by Mohan Raja on 03/04/15
 */
var authControllers = angular.module('authControllers', []);
authControllers.controller('LoginController', ['$scope', 'iAuthService', 'authService',
    function ($scope, iAuthService, authService) {
        $scope.lLoading = false;
        $scope.fp = false;

        $scope.init = function(){
            $scope.denied = false;
            if(checkNotNullEmpty($scope.curUser)){
                $scope.userId = $scope.curUser;
            }
        };
        $scope.init();

        $scope.login = function () {
            if (checkNullEmpty($scope.userId) || checkNullEmpty($scope.password)) {
                $scope.invalid = true;
                $scope.errorMsg = (checkNullEmpty($scope.userId) ? $scope.resourceBundle['user.id'] : $scope.resourceBundle['login.password']) + " " + $scope.resourceBundle['isrequired'];
            } else {
                $scope.lLoading = true;
                iAuthService.login($scope.userId, $scope.password, $scope.i18n.language.locale).then(function (data) {
                    $scope.errMsg = data.data;
                    if ($scope.errMsg.isError) {
                        $scope.invalid = true;
                        $scope.showMsg = true;
                        if($scope.errMsg.ec == 4){
                            $scope.denied = true;
                        }
                        $scope.errorMsg = $scope.errMsg.errorMsg;
                    } else {
                        $scope.invalid = false;
                        $scope.errorMsg = undefined;

                        if(checkNotNullEmpty($scope.curUser)){
                            if($scope.curUser != $scope.userId){
                                authService.loginConfirmed(null, null, true);
                                $scope.refreshDomainConfig().then(function(){
                                    $scope.changeContext();
                                }).catch(function error(msg){
                                    console.log(msg);
                                }).finally(function(){
                                    $scope.hideLogin();
                                });
                            }else{
                                authService.loginConfirmed(null, null, false);
                                $scope.hideLogin();
                            }
                        }else{
                            authService.loginConfirmed(null, null, false);
                            $scope.hideLogin();
                            $scope.refreshDomainConfig();

                            if($scope.userLoggedOut){
                                $scope.changeContext();
                                $scope.userLoggedOut = false;
                            }
                        }
                    }
                }).catch(function err(msg){
                    $scope.invalid = true;
                    $scope.errorMsg = $scope.resourceBundle['login.unable'] + " " + msg;
                }).finally(function (){
                    $scope.lLoading = false;
                });
            }
        };

        $scope.forgotPassword = function(){
            $scope.fp = true;
        };

        $scope.reset = function(){
            $scope.fp = false;
        }
    }]);

authControllers.controller('ForgotPasswordController', ['$scope', 'iAuthService',
    function($scope, iAuthService){
        function init(){
            $scope.fpw = {mode:"0"};
            $scope.otp = true;
            $scope.openOtp = false;
            $scope.newotp = false;
            $scope.invalid = false;
            $scope.showMsg = false;
            $scope.errorMsg = "";
            $scope.fLoading = false;
            $scope.nLoading = false;
            $scope.resetPwd = false;
        }

        init();

        $scope.toggleOTP = function(){
            $scope.openOtp = !$scope.openOtp;
        };

        $scope.enterPreviousOTP = function(){
            $scope.resetPwd = true;
            $scope.toggleOTP();
        };

        $scope.cancelFP = function(){
            init();
            $scope.reset();
        };

        $scope.generateNewOtp = function(){
            $scope.fpw.otp = "";
            $scope.newotp = true;
            $scope.generateOtp();
        };

        $scope.generateOtp = function(){
            if(checkNotNullEmpty($scope.fpw)){
                if(checkNullEmpty($scope.fpw.uid)){
                    $scope.showWarning($scope.resourceBundle['pwd.user.id.required']);
                    return;
                }
                if($scope.newotp){
                    $scope.nLoading = true;
                } else {
                    $scope.fLoading = true;
                }
                iAuthService.generateOtp($scope.fpw).then(function(data){
                    $scope.fpResponse = data.data;
                    if ($scope.fpResponse.isError) {
                        $scope.invalid = true;
                        $scope.showMsg = true;
                        $scope.errorMsg = $scope.fpResponse.errorMsg;
                    } else {
                        $scope.invalid = false;
                        $scope.errorMsg = undefined;
                        $scope.showSuccess($scope.fpResponse.errorMsg);
                        $scope.resetPwd = true;
                        if (!$scope.newotp && $scope.fpw.mode == '0') {
                            $scope.otp = false;
                            $scope.toggleOTP();
                        }
                    }
                }).catch(function error(msg){
                    $scope.showErrorMsg(msg);
                }).finally(function (){
                    $scope.fLoading = false;
                    $scope.nLoading = false;
                    $scope.newotp = false;
                    if($scope.fpw.mode == '1' && !$scope.invalid){
                        $scope.cancelFP();
                    }
                });
            }else{
                $scope.showWarning($scope.resourceBundle['pwd.user.id.required']);
            }
        };

        $scope.resetPassword = function(){
            if($scope.fpw.mode != '1' && checkNullEmpty($scope.fpw.otp)){
                $scope.showWarning($scope.resourceBundle['pwd.otp.required']);
                return;
            }
            $scope.fLoading = true;
            iAuthService.generatePassword($scope.fpw, null).then(function(data){
                $scope.fpResponse = data.data;
                if ($scope.fpResponse.isError) {
                    $scope.invalid = true;
                    $scope.showMsg = true;
                    $scope.errorMsg = $scope.fpResponse.errorMsg;
                } else {
                    $scope.invalid = false;
                    $scope.errorMsg = undefined;
                    $scope.showSuccess($scope.fpResponse.errorMsg);
                    $scope.cancelFP();
                }
            }).catch(function error(msg){
                $scope.showErrorMsg(msg);
            }).finally(function (){
                $scope.fLoading = false;
            });
        };

        /*$scope.generatePassword = function(otp){
            if(checkNotNullEmpty($scope.fpw)){
                if(checkNullEmpty($scope.fpw.uid)){
                    $scope.showWarning($scope.resourceBundle['pwd.user.id.required']);
                    return;
                }
                if(otp){
                    if(!$scope.newotp){
                        $scope.fLoading = true;
                    }
                    iAuthService.generateOtp($scope.fpw).then(function(data){
                        $scope.fpResponse = data.data;
                        if ($scope.fpResponse.isError) {
                            $scope.invalid = true;
                            $scope.showMsg = true;
                            $scope.errorMsg = $scope.fpResponse.errorMsg;
                        } else {
                            $scope.invalid = false;
                            $scope.errorMsg = undefined;
                            $scope.showSuccess($scope.fpResponse.errorMsg);
                            if (!$scope.newotp && $scope.fpw.mode == '0') {
                                $scope.otp = false;
                                $scope.toggleOTP();
                            }
                        }
                    }).catch(function error(msg){
                        $scope.showErrorMsg(msg);
                    }).finally(function (){
                        $scope.fLoading = false;
                        $scope.newotp = false;
                        if($scope.fpw.mode == '1' && !$scope.invalid){
                            $scope.cancelFP();
                        }
                    });
                } else {
                    if($scope.fpw.mode != '1' && checkNullEmpty($scope.fpw.otp)){
                        $scope.showWarning($scope.resourceBundle['pwd.otp.required']);
                        return;
                    }
                    $scope.fLoading = true;
                    iAuthService.generatePassword($scope.fpw, null).then(function(data){
                        $scope.fpResponse = data.data;
                        if ($scope.fpResponse.isError) {
                            $scope.invalid = true;
                            $scope.showMsg = true;
                            $scope.errorMsg = $scope.fpResponse.errorMsg;
                        } else {
                            $scope.invalid = false;
                            $scope.errorMsg = undefined;
                            $scope.showSuccess($scope.fpResponse.errorMsg);
                            $scope.cancelFP();
                        }
                    }).catch(function error(msg){
                            $scope.showErrorMsg(msg);
                    }).finally(function (){
                            $scope.fLoading = false;
                    });
                }

            }else{
                $scope.showWarning($scope.resourceBundle['pwd.user.id.required']);
            }
        };*/

        /*
        $scope.$watch("userId", function (newval, oldval) {
            if(newval != oldval || $scope.loadDetail){
                $scope.fetchUserDetails();
                $scope.loadDetail = false;
            }
        });*/
    }]);

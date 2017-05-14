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

var conversationControllers = angular.module('conversationControllers', []);
conversationControllers.controller('ConversationController', ['$scope', 'focus','conversationService',
    function ($scope, focus, conversationService) {

        $scope.getMessages = function(offset,size){
            offset = typeof offset !== 'undefined' ? offset : 0;
            size = typeof size !== 'undefined' ? size : 5;
            $scope.mLoading = true;
            if(offset == 0) {
                $scope.showLoading();
            }
            conversationService.getMessagesByObj($scope.objType,$scope.objId,offset,size).then(function (data){
                if(offset == 0) {
                    $scope.messages = data.data;
                }else {
                    $scope.messages.results = $scope.messages.results.concat(data.data.results);
                }
                $scope.setMessageCount($scope.messages.numFound);
            }).catch(function error(msg){
                $scope.showErrorMsg(msg);
            }).finally(function (){
                $scope.mLoading = false;
                if(offset == 0) {
                    $scope.hideLoading();
                }
            });
        };

        $scope.$on("updateMessage", function (evt, data) {
            $scope.objType = data.objType;
            $scope.objId = data.objId;
            $scope.getMessages(data.offset, data.size);
        });

        $scope.loadMessages = function() {
            $scope.getMessages($scope.messages.results.length);
        };
        $scope.getMessages();
        $scope.addMessage = function () {
            $scope.addMsg = true;
            if(checkNullEmpty($scope.newMsg)){
                $scope.newMsg = "";
            }
            focus('orderMsgArea');
        };
        $scope.saveMessage = function (){
            if(checkNullEmpty($scope.newMsg)){
                $scope.showWarning("Message is required.");
            }else if($scope.newMsg.length>2048) {
                $scope.showWarning("Message is too long.");
            }else {
                $scope.lLoading = true;
                conversationService.addMessage($scope.objType,$scope.objId,$scope.newMsg).then(function(){
                    $scope.addMsg = false;
                    $scope.newMsg = "";
                    $scope.showSuccess("Message added successfully");
                    $scope.getMessages();
                }).catch(function error(msg) {
                    $scope.showErrorMsg(msg);
                }).finally(function () {
                    $scope.lLoading = false;
                });
            }
        };
        $scope.cancel = function () {
            $scope.addMsg = false;
            $scope.newMsg = "";
        };
    }]);
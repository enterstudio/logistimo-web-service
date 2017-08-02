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

var domainCfgServices = angular.module('domainCfgServices', []);
domainCfgServices.factory('domainCfgService', ['$http', function ($http) {
    return {
        fetch: function (urlStr) {
            var promise = $http({method: 'GET', url: urlStr});
            return promise;
        },
        fetchP: function (data, urlStr) {
            var promise = $http({method: 'POST', data: data, url: urlStr});
            return promise;
        },fetchPut: function (data, urlStr) {
            var promise = $http({method: 'PUT', data: data, url: urlStr});
            return promise;
        },
        getMaterialTagsCfg: function () {
            return this.fetch('/s2/api/config/domain/tags/materials');
        },
        getRouteTagsCfg: function () {
            return this.fetch('/s2/api/config/domain/tags/route');
        },
        getOrderTagsCfg: function () {
            return this.fetch('/s2/api/config/domain/tags/order');
        },
        getEntityTagsCfg: function () {
            return this.fetch('/s2/api/config/domain/tags/entities');
        },
        getUserTagsCfg: function () {
            return this.fetch('/s2/api/config/domain/tags/user');
        },
        getOptimizerCfg: function () {
            return this.fetch('/s2/api/config/domain/optimizer');
        },
        get: function () {
            return this.fetch('/s2/api/config/domain/');
        },
        getRouteType: function () {
            return this.fetch('/s2/api/config/domain/artype');
        },
        getGeneralCfg : function(){
            return this.fetch("/s2/api/config/domain/general");
        },
        setGeneralCfg : function(data){
            return this.fetchP(data,"/s2/api/config/domain/general");
        },
        getCapabilitiesCfg : function(){
            return this.fetch("/s2/api/config/domain/capabilities");
        },
        getRoleCapabilitiesCfg : function(){
            return this.fetch("/s2/api/config/domain/rolecapabs");
        },
        setCapabilitiesCfg : function(data){
            return this.fetchP(data,"/s2/api/config/domain/capabilities");
        },
        getAccountingCfg : function(){
            return this.fetch('/s2/api/config/domain/accounts');
        },
        setAccountCfg : function(accountConfig){
            return this.fetchP(accountConfig,'/s2/api/config/domain/add');
        },
        getAssetCfg : function(){
            return this.fetch('/s2/api/config/domain/asset');
        },
        getAssetSysCfg : function(type){
            return this.fetch('/s2/api/config/domain/assetconfig?type='+type);
        },
        getAssetManufacturerSysCfg : function(type){
            return this.fetch('/s2/api/config/domain/assetconfig/manufacturer?type='+type);
        },
        getAssetWorkingStatusSysCfg : function(){
            return this.fetch('/s2/api/config/domain/assetconfig/workingstatus');
        },
        setAssetCfg : function(assetConfig){
            return this.fetchP(assetConfig,'/s2/api/config/domain/asset');
        },
        getTagsCfg : function(){
            return this.fetch('/s2/api/config/domain/tags');
        },
        setTagsCfg : function(tags){
            return this.fetchP(tags,'/s2/api/config/domain/tags');
        },
        getInventoryCfg : function(){
            return this.fetch('/s2/api/config/domain/inventory');
        },
        setInventoryCfg : function(inventory){
            return this.fetchP(inventory,'/s2/api/config/domain/inventory');
        },
        getOrdersCfg : function(){
            return this.fetch('/s2/api/config/domain/orders');
        },
        setOrdersCfg : function(orders){
            return this.fetchP(orders,'/s2/api/config/domain/orders');
        },
        getDashboardCfg : function(){
            return this.fetch('/s2/api/config/domain/dashboard');
        },
        setDashboardCfg : function(dashboard){
            return this.fetchP(dashboard,'/s2/api/config/domain/dashboard');
        },
        setNotificationsCfg : function(notification){
            return this.fetchP(notification,'/s2/api/config/domain/notifications');
        },
        getNotificationsCfg: function (notifType) {
            return this.fetch('/s2/api/config/domain/notifications/fetch?t=' + notifType);
        },
        deleteNotificationCfg : function(notification){
            return this.fetchP(notification,'/s2/api/config/domain/notifications/delete');
        },
        setBulletinBoardCfg : function(bulletinboard){
            return this.fetchP(bulletinboard,'/s2/api/config/domain/bulletinboard');
        },
        getBulletinBoardCfg : function(){
            return this.fetch('/s2/api/config/domain/bulletinboard');
        },
        postToBoard : function(notification){
            return this.fetchP("'"+notification + "'",'/s2/api/config/domain/posttoboard');
        },
        getAccessLog : function(offset, size){
            return this.fetch('/s2/api/config/domain/accesslogs?o='+ offset + '&s=' + size);
        },
        getTempVendorMap: function () {
            return this.fetch('/s2/api/config/domain/temperature/vendors');
        },
        uploadURL: function(){
            return this.fetch('/s2/api/config/domain/customreports');
        },
        uploadPostUrl : function(url,file,templateName,templateKey,edit) {
            var fd = new FormData();
            fd.append('data',file);
            fd.append('templateName',templateName);
            fd.append('templateKey',templateKey);
            fd.append('edit',edit);
            var promise = $http.post(url,fd,{
                transformRequest: angular.identity,
                headers : { 'Content-Type' : undefined},
                url : url
            });
            return promise;
        },
        setCustomReports : function(customReport, config){
            return this.fetchP({customReport: customReport, config: config},'/s2/api/config/domain/customreport/add');
        },
        getCustomReports : function(){
            return this.fetch('/s2/api/config/domain/customreport');
        },
        deleteCustomReport : function(name){
            return this.fetchP("'"+name+"'",'/s2/api/config/domain/customreport/delete');
        },
        exportReport : function(name){
            return this.fetchP("'"+name+"'",'/s2/api/config/domain/customreport/export');
        },
        getCustomReport : function(name, key){
            return this.fetch('/s2/api/config/domain/customreport/fetch?n='+ name + '&k=' + key);
        },
        updateCustomReport : function(customReport){
            return this.fetchP(customReport,'/s2/api/config/domain/customreport/update');
        },
        getReportFilters : function(){
            return this.fetch('/s2/api/config/domain/report/filters');
        },
        getAllDomain: function (size,offset,text) {
            return this.fetch('/s2/api/config/domain/domains/all?s='+size + '&o='+offset+'&q='+text);
        },
        getDomainConfigMenuStats:function(){
            return this.fetch('/s2/api/config/domain/menustats');
        },
        getCurrentSessionDetails: function(){
            return this.fetch('/s2/api/config/domain/domaininfo');
        },
        getMapLocationMapping: function() {
            return this.fetch('/s2/api/config/domain/map/locations');
        },
        getSystemDashboardConfig: function() {
            return this.fetch('/s2/api/config/domain/dashboards');
        },
        getNotificationsMessage: function (start,end,offset, size) {
            offset = typeof offset !== 'undefined' ? offset : 0;
            size = typeof size !== 'undefined' ? size : 50;
            var urlStr = '/s2/api/config/domain/notifcations/messages?offset=' + offset + "&size="
                + size;
            if(typeof start != "undefined"){
                urlStr = urlStr +"&start="+start;
            }
            if(typeof end != "undefined"){
                urlStr = urlStr +"&end="+end;
            }
            return this.fetch(urlStr);
        },
        getActualTransDateCheck : function(){
            return this.fetch("/s2/api/config/domain/getactualtrans")
        },
        getSupportCfg : function() {
            return this.fetch('/s2/api/config/domain/support');
        },
        getUniqueTransReasons : function(){
            return this.fetch('/s2/api/config/domain/inventory/transReasons');
        },
        setApprovalsConfig : function(data) {
            return this.fetchP(data, "/s2/api/config/domain/approvals");
        },
        getApprovalsConfig : function() {
            return this.fetch("/s2/api/config/domain/approvals");
        },
        getEventSummaryConfig : function() {
            return this.fetch("/s2/api/config/domain/event-summary");
        },
        setEventSummaryConfig : function(data) {
            return this.fetchPut(data,"/s2/api/config/domain/event-summary");
        }


    }
}]);

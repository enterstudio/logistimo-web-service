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

var logistimoApp = angular.module('logistimoApp', ['ngSanitize','ngRoute', 'dashboardControllers', 'dashboardServices',
    'widgetControllers', 'widgetServices', 'configServices', 'domainCfgServices', 'domainCfgControllers', 'invServices',
    'invControllers', 'entityServices', 'entityControllers', 'homeControllers', 'homeServices', 'matServices',
    'matControllers', 'entGrpServices', 'entGrpControllers', 'trnServices', 'trnControllers', 'ordServices',
    'ordControllers', 'demandServices', 'demandControllers', 'userServices', 'userControllers', 'ui.bootstrap',
    'ngAnimate', 'cmnControllers', 'tempControllers', 'tempServices', 'ngI18n', 'uiGmapgoogle-maps', 'ui.select',
    'actControllers', 'actServices', 'mapServices', 'blkUpControllers', 'blkUpServices',
    'exportServices', 'reportControllers', 'reportServices','http-auth-interceptor','authControllers','authServices',
    'linkedDomainControllers', 'linkedDomainServices','domainControllers','domainServices', 'mediaServices', 'base64',
    'exportControllers','once','assetControllers','assetServices','handlingUnitControllers','handlingUnitServices',
    'conversationServices', 'activityServices','conversationControllers','hc.downloader','reportsPluginCore','approvalServices',
    'approvalControllers'
    /*<% do-not-remove-this-comment-grunt-will-insert-dep-for-prod %>*/]);

logistimoApp.config(function (uibDatepickerConfig) {
    uibDatepickerConfig.showWeeks = false;
    uibDatepickerConfig.shortcutPropagation = true;
});
logistimoApp.config(function (uiSelectConfig) {
    uiSelectConfig.theme = 'select2';
});

logistimoApp.config(function ($routeProvider) {
    $routeProvider.when("/setup/entities/detail/:entityId/", {
        action: "setup.entities.detail.inventory"
    }).when("/setup/entities/detail/:entityId/summary/", {
        action: "setup.entities.detail.summary"
    }).when("/setup/entities/detail/:entityId/inventory/", {
        action: "setup.entities.detail.inventory"
    }).when("/setup/entities/detail/:entityId/transactions/", {
        action: "setup.entities.detail.transactions.view"
    }).when("/setup/entities/detail/:entityId/transactions/add/", {
        action: "setup.entities.detail.transactions.add"
    }).when("/setup/entities/detail/:entityId/orders/", {
        action: "setup.entities.detail.orders.view"
    }).when("/setup/entities/detail/:entityId/orders/add/", {
        action: "setup.entities.detail.orders.add"
    }).when("/setup/entities/detail/:entityId/orders/demand/", {
        action: "setup.entities.detail.orders.demand"
    }).when("/setup/entities/detail/:entityId/activity/", {
        action: "setup.entities.detail.activity"
    }).when("/setup/entities/detail/:entityId/materials/", {
        action: "setup.entities.detail.materials.list"
    }).when("/setup/entities/detail/:entityId/materials/:materialId/devices/", {
        action: "setup.entities.detail.materials.devices.list"
    }).when("/setup/entities/detail/:entityId/materials/:materialId/devices/list/", {
        action: "setup.entities.detail.materials.devices.list"
    }).when("/setup/entities/detail/:entityId/materials/:materialId/devices/add/", {
        action: "setup.entities.detail.materials.devices.add"
    }).when("/setup/entities/detail/:entityId/materials/edit", {
        action: "setup.entities.detail.materials.edit"
    }).when("/setup/entities/detail/:entityId/relationships/", {
        action: "setup.entities.detail.relationships.list"
    }).when("/setup/entities/detail/:entityId/stockboard/", {
        action: "setup.entities.detail.stockboard.list"
    }).when("/setup/entities/detail/:entityId/domains/", {
        action: "setup.entities.detail.domains"
    }).when("/setup/entities/detail/:entityId/relationships/add", {
        action: "setup.entities.detail.relationships.add"
    }).when("/setup/entities/detail/:entityId/relationships/edit", {
        action: "setup.entities.detail.relationships.edit"
    }).when("/setup/entities/detail/:entityId/relationships/setpermission", {
        action: "setup.entities.detail.relationships.setpermission"
    }).when("/setup/entities/detail/:entityId/assets", {
        action: "setup.entities.detail.assets"
    }).when("/orders/", {
        action: "orders.all.list"
    }).when("/orders/add/", {
        action: "orders.all.add"
    }).when("/orders/detail/:oid/", {
        action: "orders.all.detail"
    }).when("/orders/demand/", {
        action: "orders.demand.list"
    }).when("/orders/approvals/", {
       action: "orders.approvals"
    }).when("/orders/discrepancies/", {
        action: "orders.discrepancies"
    }).when("/orders/addshipment/", {
        action: "orders.all.addshipment"
    }).when("/orders/backorder/", {
        action: "orders.demand.backorder"
    }).when("/inventory/", {
        action: "inventory.stock"
    }).when("/inventory/stock/", {
        action: "inventory.stock"
    }).when("/inventory/abnormalstock/", {
        action: "inventory.abnormalstock"
    }).when("/inventory/transactions/", {
        action: "inventory.transactions.view"
    }).when("/inventory/transactions/add", {
        action: "inventory.transactions.add"
    }).when("/inventory/transactions/upload", {
        action: "inventory.transactions.upload"
    }).when("/inventory/transactions/viewupload", {
        action: "inventory.transactions.viewupload"
    }).when("/reports/", {
        action: "reports"
    }).when("/reports/inv", {
        action: "reports.inv"
    }).when("/reports/ort", {
        action: "reports.ort"
    }).when("/reports/rrt", {
        action: "reports.rrt"
    }).when("/reports/tc", {
        action: "reports.tc"
    }).when("/reports/ua", {
        action: "reports.ua"
    }).when("/reports/cr", {
        action: "reports.cr"
    }).when("/reports/ht", {
        action: "reports.ht"
    }).when("/config/", {
        action: "config.view"
    }).when("/setup/", {
        action: "setup.users.all.list"
    }).when("/setup/entities/", {
        action: "setup.entities.all.list"
    }).when("/setup/entities/all/", {
        action: "setup.entities.all.list"
    }).when("/setup/entities/all/add/", {
        action: "setup.entities.all.add"
    }).when("/setup/entities/all/edit/", {
        action: "setup.entities.all.edit"
    }).when("/setup/entities/all/upload/", {
        action: "setup.entities.all.upload"
    }).when("/setup/entities/all/move/", {
        action: "setup.entities.all.move"
    }).when("/setup/entities/all/materials?:entityIds", {
        action: "setup.entities.all.materials"
    }).when("/setup/materials/", {
        action: "setup.materials.all.list"
    }).when("/setup/materials/all/", {
        action: "setup.materials.all.list"
    }).when("/setup/materials/all/add", {
        action: "setup.materials.all.add"
    }).when("/setup/materials/all/upload", {
        action: "setup.materials.all.upload"
    }).when("/setup/materials/all/detail/:materialId/", {
        action: "setup.materials.all.detail.summary"
    }).when("/setup/materials/all/edit", {
        action: "setup.materials.all.edit"
    }).when("/setup/users/", {
        action: "setup.users.all.list"
    }).when("/setup/users/all/", {
        action: "setup.users.all.list"
    }).when("/setup/users/all/add", {
        action: "setup.users.all.add"
    }).when("/setup/users/all/details", {
        action: "setup.users.all.details.kiosks"
    }).when("/setup/users/all/edit", {
        action: "setup.users.all.edit"
    }).when("/setup/users/all/updatepassword", {
        action: "setup.users.all.updatepassword"
    }).when("/setup/users/all/sendmessage", {
        action: "setup.users.all.sendmessage"
    }).when("/setup/users/all/msgstatus", {
        action: "setup.users.all.msgstatus"
    }).when("/setup/users/all/upload", {
        action: "setup.users.all.upload"
    }).when("/setup/ent-grps/", {
        action: "setup.ent-grps.all.list"
    }).when("/setup/ent-grps/all/", {
        action: "setup.ent-grps.all.list"
    }).when("/setup/ent-grps/all/add", {
        action: "setup.ent-grps.all.add"
    }).when("/setup/inventory", {
        action: "setup.inventory.upload"
    }).when("/setup/inventory/upload", {
        action: "setup.inventory.upload"
    }).when("/configuration/", {
        action: "configuration.general"
    }).when("/configuration/general/", {
        action: "configuration.general"
    }).when("/configuration/capabilities/", {
        action: "configuration.capabilities"
    }).when("/configuration/inventory/", {
        action: "configuration.inventory"
    }).when("/configuration/approvals/", {
        action: "configuration.approvals"
    }).when("/configuration/accounting/", {
        action: "configuration.accounting"
    }).when("/configuration/custom", {
        action: "configuration.custom"
    }).when("/configuration/asset", {
        action: "configuration.asset"
    }).when("/configuration/dashboard", {
        action: "configuration.dashboard"
    }).when("/configuration/tags", {
        action: "configuration.tags"
    }).when("/configuration/orders", {
        action: "configuration.orders"
    }).when("/configuration/notifications", {
        action: "configuration.notifications.event.orders"
    }).when("/configuration/notifications/event", {
        action: "configuration.notifications.event.orders"
    }).when("/configuration/notifications/event/shipments",{
        action: "configuration.notifications.event.shipments"
    }).when("/configuration/notifications/event/accounts",{
        action: "configuration.notifications.event.accounts"
    }).when("/configuration/notifications/bulletin", {
        action: "configuration.notifications.bulletin"
    }).when("/configuration/notifications/event/orders", {
        action: "configuration.notifications.event.orders"
    }).when("/configuration/notifications/event/inventory", {
        action: "configuration.notifications.event.inventory"
    }).when("/configuration/notifications/event/setup", {
        action: "configuration.notifications.event.setup"
    }).when("/configuration/notifications/event/assets", {
        action: "configuration.notifications.event.temperature"
    }).when("/configuration/notifications/accesslogs", {
        action: "configuration.notifications.accesslogs"
    }).when("/configuration/options", {
       action: "configuration.options.all.push"
    }). when("/accounts/", {
        action: "accounts.receivable"
    }).when("/accounts/receivable", {
        action: "accounts.receivable"
    }).when("/accounts/payable", {
        action: "accounts.payable"
    }).when("/assets", {
        action: "assets.dashboard.map"
    }).when("/assets/dashboard", {
        action: "assets.dashboard.map"
    }).when("/assets/dashboard/map", {
        action: "assets.dashboard.map"
    }).when("/assets/dashboard/table", {
        action: "assets.dashboard.table"
    }).when("/assets/all", {
        action: "assets.all"
    }).when("/assets/detail/:vendorId/:deviceId/", {
        action: "assets.detail.summary"
    }).when("/assets/detail/:vendorId/:deviceId/summary", {
        action: "assets.detail.summary"
    }).when("/assets/detail/:vendorId/:deviceId/info", {
        action: "assets.detail.info"
    }).when("/assets/detail/:vendorId/:deviceId/stats", {
        action: "assets.detail.stats"
    }).when("/assets/detail/:vendorId/:deviceId/config", {
        action: "assets.detail.config"
    }).when("/assets/detail/:vendorId/:deviceId/relations", {
        action: "assets.detail.relations"
    }).when("/setup/domains/", {
       action: "setup.domains.all.children"
    }).when("/setup/domains/all/children", {
       action: "setup.domains.all.children"
    }).when("/dashboard/activity/session", {
        action: "dashboard.activity.session"
    }).when("/dashboard/activity", {
        action: "dashboard.activity.session"
    }).when("/dashboard/inventory", {
        action: "dashboard.inventory.overview"
    }).when("/dashboard/inventory/overview", {
        action: "dashboard.inventory.overview"
    }).when("/dashboard/inventory/predictive", {
        action: "dashboard.inventory.predictive"
    }).when("/dashboard/view/:dbid", {
        action: "dashboard.view"
    }).when("/newreports", {
        action: "newreports"
    }).when("/newreports/:rptid", {
        action: "newreports"
    }).when("/dashboard/overview", {
        action: "dashboard.overview"
    }).when("/dashboard/", {
        action: "dashboard.overview"
    }).when("/exportstatus",{
        action: "exportstatus.exports"
    }).when("/exportstatus/exports",{
        action: "exportstatus.exports"
    }).when("/", {
        action: "dashboard.overview"
    }).when("/setup/dashboard", {
        action: "setup.dashboard.summary"
    }).when("/setup/dashboard/summary", {
        action: "setup.dashboard.summary"
    }).when("/setup/dashboard/editdashboard", {
        action: "setup.dashboard.editdashboard"
    }).when("/setup/dashboard/editwidget", {
        action: "setup.dashboard.editwidget"
    }).when("/setup/dashboard/configdashboard", {
        action: "setup.dashboard.configdashboard"
    }).when("/setup/dashboard/configwidget", {
        action: "setup.dashboard.configwidget"
    }).when("/setup/dashboard/createdashboard", {
        action: "setup.dashboard.createdashboard"
    }).when("/setup/dashboard/createwidget", {
        action: "setup.dashboard.createwidget"
    }).when("/manage/", {
        action: "manage.domains.list"
    }).when("/manage/domains/", {
        action: "manage.domains.list"
    }).when("/manage/domains/list", {
        action: "manage.domains.list"
    }).when("/manage/domains/add",{
        action :"manage.domains.add"
    }).when("/manage/domains/:domainId/" ,{
        action :"manage.domains.detail"
    }).when("/setup/users/all/details/kiosks", {
        action: "setup.users.all.details.kiosks"
    }).when("/setup/users/all/details/domains", {
        action: "setup.users.all.details.domains"
    }).when("/configuration/notifications/notifstatus", {
        action: "configuration.notifications.notifstatus"
    }).when("/setup/assets/",{
        action: "setup.assets.all.list"
    }).when("/setup/assets/all/",{
        action: "setup.assets.all.list"
    }).when("/setup/assets/all/list",{
        action: "setup.assets.all.list"
    }).when("/setup/assets/all/add",{
        action: "setup.assets.all.add"
    }).when("/setup/assets/all/edit",{
        action: "setup.assets.all.edit"
    }).when("/setup/assets/all/upload",{
        action: "setup.assets.all.upload"
    }).when("/dashboard/orders", {
        action: "dashboard.orders"
    }).when("/dashboard/orders/detail/:oid/", {
        action: "dashboard.orders.view-order"
    }).when("/dashboard/hierarchy", {
        action: "dashboard.hierarchy"
    }).when("/setup/handlingunits/", {
        action: "setup.handlingunits.all.list"
    }).when("/setup/handlingunits/all/list", {
        action: "setup.handlingunits.all.list"
    }).when("/setup/handlingunits/all/create", {
        action: "setup.handlingunits.all.create"
    }).when("/setup/handlingunits/all/detail/:handlingUnitId/", {
        action: "setup.handlingunits.all.detail"
    }).when("/setup/handlingunits/all/edit/:handlingUnitId", {
        action: "setup.handlingunits.all.edit"
    }).when("/orders/transfers/", {
        action: "orders.transfers.list"
    }).when("/orders/transfers/add", {
        action: "orders.transfers.add"
    }).when("/orders/transfers/detail/:oid", {
        action: "orders.transfers.detail"
    }).when("/setup/entities/detail/:entityId/transfers/", {
        action: "setup.entities.detail.transfers.view"
    }).when("/setup/entities/detail/:entityId/transfers/add/", {
        action: "setup.entities.detail.transfers.add"
    }).when("/setup/entities/detail/:entityId/approvals", {
        action: "setup.entities.detail.approvals"
    }).when("/orders/shipment/", {
        action: "orders.shipment.list"
    }).when("/orders/shipment/detail/:sid/", {
        action: "orders.shipment.shipmentdetail"
    })
});
logistimoApp.config(['$httpProvider', function($httpProvider) {
    var ua = window.navigator.userAgent;
    if(ua.indexOf( "MSIE ") > 0 || ua.indexOf('Trident/') > 0 || ua.indexOf('Edge/') > 0 || navigator.appVersion.indexOf("MSIE 9.0") !== -1) {
        //initialize get if not there
        if (!$httpProvider.defaults.headers.get) {
            $httpProvider.defaults.headers.get = {};
        }
        //disable IE ajax request caching
        $httpProvider.defaults.headers.get['If-Modified-Since'] = 'Mon, 26 Jul 1997 05:00:00 GMT';

        $httpProvider.defaults.headers.get['Cache-Control'] = 'no-cache';
        $httpProvider.defaults.headers.get['Pragma'] = 'no-cache';
    }
}]);
logistimoApp.value('ngI18nConfig', {
    defaultLocale: 'en-us',
    basePath: '/v2/i18n',
    supportedLocales: ['en', 'fr']
});
logistimoApp.filter("asDate", function () {
    return function (input) {
        if (checkNotNullEmpty(input)) {
            return constructDate(input);
        }
        return input;
    }
});
logistimoApp.filter('range', function() {
    return function(input, limit, start) {
        limit = parseInt(limit);
        start = start || 1;
        for (var i=start; i<=limit; i++) {
            input.push(i);
        }
        return input;
    };
});
logistimoApp.filter('roundNoTrailZeros', function() {
    return function(input, fractionSize) {
       return checkNotNullEmpty(input) ? (input * 1).toFixed(fractionSize)*1 : (input  * 1);
    }
});
logistimoApp.filter('orderObjectBy', function () {
    return function (input, attribute, direction) {
        if (!angular.isObject(input))
            return input;
        var array = [];
        for (var objectKey in input) {
            array.push(input[objectKey]);
        }
        array.sort(function (a, b) {
            a = parseInt(getVal(a, attribute));
            b = parseInt(getVal(b, attribute));
            return direction ? b - a : a - b;
        });
        return array;
    }
});
logistimoApp.config(['uiGmapGoogleMapApiProvider', function (GoogleMapApi) {
    GoogleMapApi.configure({
        key: 'google-key',
        v: '3.24',
        libraries: 'weather,geometry,visualization',
        isGoogleMapsForWork: true,
        client : 'google-client-id'
    });
}]);

logistimoApp.config(['$uibTooltipProvider', function($uibTooltipProvider){
    $uibTooltipProvider.setTriggers({
        'showpopup': 'hidepopup',
        'hclick': 'hleave'
    });
}]);

(function($) {
    $.fn.fixMe = function() {
        return this.each(function() {
            var $this = $(this),
                $t_fixed;
            function init() {
                $this.wrap('<div class="table-container" />');
                $t_fixed = $this.clone();
                $t_fixed.find("tbody").remove().end().addClass("fixed").insertBefore($this);
                resizeFixed();
            }
            function resizeFixed() {
                $t_fixed.find("th").each(function(index) {
                    $(this).css("width",$this.find("th").eq(index).outerWidth()+"px");
                });
            }
            function scrollFixed() {
                var offset = $(this).scrollTop(),
                    tableOffsetTop = $this.offset().top,
                    tableOffsetBottom = tableOffsetTop + $this.height() - $this.find("thead").height();
                if(offset < tableOffsetTop || offset > tableOffsetBottom)
                    $t_fixed.hide();
                else if(offset >= tableOffsetTop && offset <= tableOffsetBottom && $t_fixed.is(":hidden"))
                    $t_fixed.show();
            }
            $(window).resize(resizeFixed);
            $(window).scroll(scrollFixed);
            init();
        });
    };
})(jQuery);


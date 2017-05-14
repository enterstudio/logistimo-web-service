var lgTempTab = angular.module('tabular-mod', []);

lgTempTab.controller('tableLoadController', ['$scope', '$location', '$window', 'tableLoadServices',
    function ($scope, $location, $window, tableLoadServices) {
        $scope.invntryItems = "";
        $scope.domainId = "";
        $scope.kioskId = -1;
        $scope.deviceId = -1;
        $scope.vendorId = -1;
        $scope.currentFilter = 0;
        $scope.params = $location.path();
        $scope.filters = [{"value": "0", "displayValue": "All"},
            {"value": "1", "displayValue": "Temperature alarms"},
            {"value": "2", "displayValue": "Device alarms"},
            {"value": "3", "displayValue": "No data"},
            {"value": "4", "displayValue": "Normal items"}];
        $scope.currentFilter = 0;
        $scope.durSortOrder = true;
        $scope.currentFilterDuration = 0;
        $scope.location = "";
        $scope.locType = "";
        $scope.loading = true;

        $scope.init = function (domainId, kioskId, deviceId, vendorId, filter, duration, location, locType) {
            $scope.domainId = domainId;
            if (kioskId != null)
                $scope.kioskId = kioskId;

            if (deviceId != null)
                $scope.deviceId = deviceId;

            if (vendorId != null)
                $scope.vendorId = vendorId;

            if (filter != null)
                $scope.currentFilter = filter;

            if (duration != null)
                $scope.currentFilterDuration = duration;

            if (location != null)
                $scope.location = location;

            if (locType != null)
                $scope.locType = locType;

            tableLoadServices.getInvntryItems($scope.domainId, $scope.kioskId, $scope.deviceId,
                $scope.vendorId, $scope.currentFilter, $scope.currentFilterDuration, $scope.location, $scope.locType).then(function (data) {
                    $scope.invntryItems = data.data;
                    $scope.loading = false;
                }).catch(function error(msg) {
                    console.error(msg);
                });
        };

        $scope.onAlarmFilterChange = function () {
            $scope.loading = true;
            $window.location.href = 'temperature.jsp?subview=items&filter=' + $scope.currentFilter;
        };

        $scope.onDurationFilterChange = function () {
            $scope.loading = true;
                $window.location.href = 'temperature.jsp?subview=items&filter=' + $scope.currentFilter + '&duration=' + $scope.currentFilterDuration;
        };

        $scope.changeDurationOrder = function () {
            $scope.durSortOrder = !$scope.durSortOrder;
        }
    }]);

lgTempTab.factory('tableLoadServices', [
    '$http',
    function ($http) {
        return {
            getInvntryItems: function (domainId, kioskId, deviceId, vendorId, filter, duration, location, locType) {
                return $http({
                    method: 'GET',
                    url: '/tempmonitoring?a=getassets&domainid=' + domainId + '&kioskid=' + kioskId + '&deviceid=' + deviceId + '&vendorid=' + vendorId + '&filter=' + filter + '&duration=' + duration + '&location=' + location + '&loctype=' + locType
                }).success(function (data, status, headers, config) {
                    return data;
                });
            }
        }
    }
]);

lgTempTab.filter('timeago', function() {
    return function(input, p_allowFuture, p_agoreq) {
        var substitute = function (stringOrFunction, number, strings) {
                var string = $.isFunction(stringOrFunction) ? stringOrFunction(number, dateDifference) : stringOrFunction;
                var value = (strings.numbers && strings.numbers[number]) || number;
                return string.replace(/%d/i, value);
            },
            nowTime = (new Date()).getTime(),
            date = (new Date(input)).getTime(),
            allowFuture = p_allowFuture || false,
            isSuffixReq = p_agoreq || false,
            strings= {
                prefixAgo: null,
                prefixFromNow: null,
                suffixAgo: "ago",
                suffixFromNow: "from now",
                seconds: "less than a minute",
                minute: "about a minute",
                minutes: "%d minutes",
                hour: "about an hour",
                hours: "about %d hours",
                day: "a day",
                days: "%d days",
                month: "about a month",
                months: "%d months",
                year: "about a year",
                years: "%d years"
            },
            dateDifference = nowTime - date,
            words,
            seconds = Math.abs(dateDifference) / 1000,
            minutes = seconds / 60,
            hours = minutes / 60,
            days = hours / 24,
            years = days / 365,
            separator = strings.wordSeparator === undefined ?  " " : strings.wordSeparator;

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
        }else{
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
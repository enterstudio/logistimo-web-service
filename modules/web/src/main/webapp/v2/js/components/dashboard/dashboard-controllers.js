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
 * @author Mohan Raja
 */
var domainControllers = angular.module('dashboardControllers', []);
domainControllers.controller('AddDashboardController', ['$scope', 'dashboardService',
    function ($scope, dashboardService) {
        $scope.dbDesc = '';
        $scope.create = function () {
            if (checkNullEmpty($scope.dbName)) {
                $scope.showWarning("Dashboard Name cannot be blank.");
                return;
            }
            $scope.showLoading();
            var data = {nm: $scope.dbName, desc: $scope.dbDesc};
            dashboardService.create(data).then(function (data) {
                $scope.showSuccess(data.data);
                $scope.dbName = '';
                $scope.dbDesc = '';
            }).catch(function error(msg) {
                $scope.showErrorMsg(msg);
            }).finally(function () {
                $scope.hideLoading();
            });
        };
    }
]);

domainControllers.controller('EditDashboardController', ['$scope', 'dashboardService', 'requestContext',
    function ($scope, dashboardService, requestContext) {
        $scope.dbId = requestContext.getParam("dbid");
        $scope.showLoading();
        dashboardService.getById($scope.dbId).then(function (data) {
            $scope.db = data.data;
        }).catch(function error(msg) {
            $scope.showErrorMsg(msg);
        }).finally(function () {
            $scope.hideLoading();
        });

        $scope.setName = function () {
            $scope.descedit = false;
            $scope.enm = $scope.db.nm;
            $scope.nmedit = true;
        };
        $scope.updateName = function () {
            $scope.nmedit = false;
            $scope.updatingName = true;
            var data = {ty: 'nm', id: $scope.db.dbId, val: $scope.enm};
            dashboardService.update(data).then(function () {
                $scope.db.nm = $scope.enm;
                $scope.updateDashboardName($scope.db.dbId, $scope.db.nm);
            }).catch(function error(msg) {
                $scope.showErrorMsg(msg);
            }).finally(function () {
                $scope.updatingName = false;
            });
        };
        $scope.cancelName = function () {
            $scope.nmedit = false;
        };
        $scope.setDesc = function () {
            $scope.nmedit = false;
            $scope.edesc = $scope.db.desc;
            $scope.descedit = true;
        };
        $scope.updateDesc = function () {
            $scope.descedit = false;
            $scope.updatingDesc = true;
            var data = {ty: 'desc', id: $scope.db.dbId, val: $scope.edesc};
            dashboardService.update(data).then(function () {
                $scope.db.desc = $scope.edesc;
            }).catch(function error(msg) {
                $scope.showErrorMsg(msg);
            }).finally(function () {
                $scope.updatingDesc = false;
            });
        };
        $scope.cancelDesc = function () {
            $scope.descedit = false;
        };
    }
]);

domainControllers.controller('DashboardSummaryController', ['$scope', 'dashboardService', 'widgetService',
    function ($scope, dashboardService, widgetService) {

        //$scope.getAllDashboards();

        $scope.showLoading();
        $scope.wLoading = true;
        widgetService.getAll().then(function (data) {
            $scope.widgets = data.data;
        }).catch(function error(msg) {
            $scope.showErrorMsg(msg);
        }).finally(function () {
            $scope.hideLoading();
            $scope.wLoading = false;
        });

        $scope.deleteDashboard = function (id) {
            if (!confirm("Do you want to delete this dashboard completely. Confirm?")) {
                return;
            }
            $scope.showLoading();
            dashboardService.delete(id).then(function (data) {
                $scope.showSuccess(data.data);
                $scope.setDashboard($scope.dashboards.filter(function (l) {
                    return l.dbId != id;
                }));
            }).catch(function error(msg) {
                $scope.showErrorMsg(msg);
            }).finally(function () {
                $scope.hideLoading();
            });
        };

        $scope.deleteWidget = function (id) {
            if (!confirm("Do you want to delete this widget. Confirm?")) {
                return;
            }
            $scope.showLoading();
            widgetService.delete(id).then(function (data) {
                $scope.showSuccess(data.data);
                $scope.widgets = $scope.widgets.filter(function (l) {
                    return l.wId != id;
                });
            }).catch(function error(msg) {
                $scope.showErrorMsg(msg);
            }).finally(function () {
                $scope.hideLoading();
            });
        };

        $scope.setAsDefault = function (id) {
            $scope.showLoading();
            var curDef = getCurrentDefault();
            dashboardService.setAsDefault(curDef, id).then(function () {
                changeCurrentDefault(id);
            }).catch(function error(msg) {
                $scope.showErrorMsg(msg);
            }).finally(function () {
                $scope.hideLoading();
            });
        };

        function getCurrentDefault() {
            var def = 0;
            $scope.dashboards.some(function (d) {
                if (d.def) {
                    def = d.dbId;
                    return true;
                }
            });
            return def;
        }

        function changeCurrentDefault(id) {
            $scope.dashboards.forEach(function (d) {
                d.def = d.dbId == id;
            });
        }
    }
]);

domainControllers.controller('ConfigDashboardController', ['$scope', 'requestContext', 'dashboardService', 'widgetService', '$timeout',
    function ($scope, requestContext, dashboardService, widgetService, $timeout) {
        $scope.dbId = requestContext.getParam("dbid");
        var ul = document.getElementById("allWid");
        var MAX_COLS_WIDTH = parseInt(getComputedStyle(ul).width, 10);
        var sw = MAX_COLS_WIDTH / 12;
        var sh = 110;
        var PAD = 5; // Dont Change: From UI
        var PAD2 = PAD * 2;
        var PAD3 = PAD * 3;

        $scope.dashWidgets = [];
        $scope.allWids = [];
        function setMaxHeight() {
            var maxHeight = 100;
            $scope.maxHeightNo = 0;
            $scope.dashWidgets.forEach(function (dw) {
                if (maxHeight < dw.cr + dw.cy) {
                    maxHeight = dw.cr + dw.cy;
                    $scope.maxHeightNo = dw.r + dw.y;
                }
            });
            ul.style.minHeight = maxHeight - PAD + 'px';
        }

        function addComputeData(wid) {
            if (wid) {
                wid.cx = wid.x * sw;
                wid.cc = wid.c * sw;
                wid.cy = wid.y * sh - PAD2;
                wid.cr = wid.r * sh + PAD;
            } else {
                $scope.dashWidgets.forEach(function (d) {
                    d.cx = d.x * sw;
                    d.cc = d.c * sw;
                    d.cy = d.y * sh - PAD2;
                    d.cr = d.r * sh + PAD;
                });
            }
        }

        function extractWids(w) {
            var ids = [];
            w.forEach(function (w) {
                ids.push(w.wid.toString());
            });
            return ids;
        }

        function addPaneEventListener(wid, time) {
            $timeout(function () {
                var pp = document.getElementById('panel_' + wid);
                pp.addEventListener('mousedown', onMouseDown);
                pp.addEventListener('mousemove', onMove);
                panels[wid] = pp;
                ghostPanels[wid] = document.getElementById('ghost_' + wid);
            }, time ? time : 1000); // Let the page render this elements to add listeners
        }

        function initEventListeners() {
            $scope.showLoading();
            $timeout(function () {
                $scope.dashWidgets.forEach(function (w) {
                    addPaneEventListener(w.wid, 1);
                });
                document.addEventListener('mouseup', onUp);
                animate();
                $scope.hideLoading();
            }, 1000); // Let the page render all elements to add listeners
        }

        $scope.showLoading();
        dashboardService.getById($scope.dbId, 'y').then(function (data) {
            $scope.db = data.data;
            if (checkNotNullEmpty($scope.db.conf)) {
                $scope.dashWidgets = JSON.parse($scope.db.conf);
                addComputeData();
                setMaxHeight();
                $scope.allWids = extractWids($scope.dashWidgets);
            }
            initEventListeners();
        }).catch(function error(msg) {
            $scope.showErrorMsg(msg);
        }).finally(function () {
            $scope.hideLoading();
        });

        $scope.showLoading();
        $scope.wLoading = true;
        widgetService.getAll().then(function (data) {
            $scope.allWidgets = data.data;
            $scope.allWidgetsByIds = {};
            $scope.allWidgets.forEach(function (d) {
                $scope.allWidgetsByIds[d.wId] = d;
            });
        }).catch(function error(msg) {
            $scope.showErrorMsg(msg);
        }).finally(function () {
            $scope.hideLoading();
            $scope.wLoading = false;
        });

        $scope.addWidget = function () {
            if (checkNotNullEmpty($scope.widget)) {
                var newWid = {};
                newWid.wid = $scope.widget.id;
                newWid.c = 0;
                newWid.x = 4;
                newWid.y = 3;
                newWid.r = $scope.maxHeightNo;
                addComputeData(newWid);
                $scope.dashWidgets.push(newWid);
                $scope.allWids.push($scope.widget.id.toString());
                setMaxHeight();
                addPaneEventListener($scope.widget.id);
                movePanesUp();
                $scope.widget = undefined;
            }
        };

        var removeProcessedPanes = [];

        $scope.removeWidget = function (wid) {
            if (!confirm("Do you want to remove this widget from dashboard?")) {
                return;
            }
            var ind = 0;
            $scope.dashWidgets.some(function (d) {
                if (d.wid == wid.wid && d.r == wid.r && d.c == wid.c && d.x == wid.x && d.y == wid.y) {
                    $scope.dashWidgets.splice(ind, 1);
                    return true;
                }
                ind++;
            });
            removeProcessedPanes = [];
            movePanesUp();
            setMaxHeight();
        };

        function movePanesUp() {
            $scope.dashWidgets.forEach(function (d) {
                if (removeProcessedPanes.indexOf(d.wid) == -1) {
                    var cnt = 1;
                    while (d.r - cnt >= 0 && !isBlocked(d, cnt)) {
                        cnt++;
                    }
                    cnt -= 1;
                    if (cnt > 0) {
                        decreaseRow(d, cnt);
                        removeProcessedPanes.push(d.wid);
                        movePanesUp();
                    }
                }
            });
        }

        function constructConfigJSON() {
            var fData = [];
            $scope.dashWidgets.forEach(function (d) {
                var dd = angular.copy(d);
                dd.cc = dd.cr = dd.cx = dd.cy = undefined;
                fData.push(dd);
            });
            return JSON.stringify(fData);
        }

        $scope.saveDashboardConfig = function () {
            $scope.showLoading();
            var row = constructConfigJSON();
            var data = {ty: 'conf', id: $scope.dbId, val: row};
            dashboardService.saveConfig(data).then(function (data) {
                $scope.showSuccess(data.data);
            }).catch(function error(msg) {
                $scope.showErrorMsg(msg);
            }).finally(function () {
                $scope.hideLoading();
            });
        };

        $scope.query = function (query) {
            var rData = {results: []};
            var cnt = 0;
            $scope.allWidgets.some(function (data) {
                if (data.nm.toLowerCase().indexOf(query.term.toLowerCase()) != -1) {
                    var isAlreadyAdded = $scope.dashWidgets.some(function (d) {
                        if (d.wid == data.wId) {
                            return true;
                        }
                    });
                    if (!isAlreadyAdded) {
                        rData.results.push({'text': data.nm, 'id': data.wId});
                        if (cnt++ == 8) {
                            return true;
                        }
                    }
                }
            });
            query.callback(rData);
        };

        var MIN_WIDTH = sw * 2;
        var MIN_HEIGHT = sh * 2;

        var clicked = null;
        var onRightEdge, onBottomEdge, onLeftEdge;
        var b, x, y;
        var panel, ghostPanel;
        var ghostPanels = [], panels = [];
        var redraw = false;
        var e;
        var dProcessedPanels = [];

        function recalculateEdges(wid, l, t, w, h) {
            $scope.dashWidgets.some(function (d) {
                if (d.wid == wid) {
                    d.cx = w;
                    d.cy = h - PAD2;
                    d.cc = l;
                    d.cr = t + PAD;
                    d.x = d.cx / sw;
                    d.y = (d.cy + PAD2) / sh;
                    d.c = d.cc / sw;
                    d.r = (d.cr - PAD) / sh;
                    return true;
                }
            });
        }

        function increaseRow(wid, count) {
            wid.cr = (wid.r + count) * sh + PAD;
            wid.r += count;
            panels[wid.wid].style.top = wid.cr + 'px';
            ghostPanels[wid.wid].style.top = wid.cr + PAD + 'px';
        }

        function decreaseRow(wid, count) {
            increaseRow(wid, -count);
        }

        function isIncrease(wid, t, h, l, w, recall) {
            if ((t + h > wid.cr - PAD && t + h <= wid.cr + wid.cy + PAD) ||
                (t > wid.cr - PAD && t < wid.cr + wid.cy + PAD && recall)) { // h increase move down - recall: because of width increase
                if (l <= wid.cc && l + w >= wid.cc + wid.cx) { // 1 & 3 / 2 & 4,5 // check for eq in 1st cond
                    return true;
                } else if ((l + w - PAD > wid.cc && l + w < wid.cc + wid.cx) ||
                    (l > wid.cc && l < wid.cc + wid.cx - PAD)) { // 1 & 5
                    return true;
                }
            }
            return false;
        }

        function isBlockedPanel(sc, sx, dc, dx, isBig) {
            return (sc <= dc && sc + sx <= dc + dx && sc + sx > dc) ||
                (sc >= dc && sc + sx >= dc + dx && sc < dc + dx) ||
                (sc == dc && sc + sx == dc + dx) ||
                (dc < sc && dc + dx > sc + sx) ||
                (!isBig && (dc > sc && dc + dx < sc + sx));
        }

        function isBlocked(wid, cnt, skipWid) {
            return $scope.dashWidgets.some(function (dd) {
                if (dd.wid != wid.wid && dd.wid != skipWid && isBlockedPanel(dd.cc, dd.cx, wid.cc, wid.cx) &&
                    ((wid.r - cnt) * sh + PAD) < dd.cr + dd.cy && ((wid.r - cnt) * sh + PAD) > dd.cr) {
                    return true;
                }
            });
        }

        function getWidgetId(id, asInt) {
            return asInt ? parseInt(id.replace('panel_', ''), 10) : id.replace('panel_', '');
        }

        function movePanesDown(np, ngp, recall) {
            var nw = parseInt(ngp.style.width, 10) + PAD2;
            var nh = parseInt(ngp.style.height, 10) + PAD2;
            var nt = parseInt(ngp.style.top, 10) - PAD2;
            var nl = parseInt(ngp.style.left, 10) - PAD;
            var curId = getWidgetId(np.id);
            dProcessedPanels.push(curId);
            recalculateEdges(curId, nl, nt, nw, nh);
            $scope.dashWidgets.forEach(function (d) {
                if (dProcessedPanels.indexOf(d.wid.toString()) == -1) {
                    if (isIncrease(d, nt, nh, nl, nw, recall)) {
                        increaseRow(d, 1);
                        movePanesDown(panels[d.wid], ghostPanels[d.wid], true);
                    }
                }
            });
        }

        function getInnerPanel(p) {
            return p.children[0].children[0].children[0];
        }

        function getInnerMostPanel(p) {
            return p.children[0].children[0].children[0].children[1].children[0].children[0];
        }


        var processedPanels = [];

        function reArrangePanes(np, ngp, oWid, oHt, recall, rht) {
            var nw = parseInt(ngp.style.width, 10) + PAD2;
            var nh = parseInt(ngp.style.height, 10) + PAD2;
            var nt = parseInt(ngp.style.top, 10) - PAD2;
            var nl = parseInt(ngp.style.left, 10) - PAD;
            var curId = np.id.replace('panel_', '');
            processedPanels.push(curId);
            recalculateEdges(curId, nl, nt, nw, nh);
            $scope.dashWidgets.forEach(function (d) {
                if (processedPanels.indexOf(d.wid.toString()) == -1) {
                    var inc = false, dec = false;
                    var wd = false;
                    if (nw / sw < oWid / sw || nh / sh < oHt / sh) { // is decrease
                        dec = true;
                    } else {
                        if ((nw / sw > oWid / sw || recall) && nl + nw > d.cc && nl < d.cc + d.cx) { // w increase move down
                            if (nt <= d.cr && nt + nh >= d.cr + d.cy - PAD) { // inside / equal
                                inc = true;
                                wd = true;
                            } else if ((nt + nh > d.cr && nt + nh < d.cr + d.cy - PAD) ||
                                (nt > d.cr + PAD && nt < d.cr + d.cy + PAD)) { // outside
                                inc = true;
                                wd = true;
                            }
                        }
                        if ((nh / sh > oHt / sh || recall) && isIncrease(d, nt, nh, nl, nw, recall)) {
                            inc = true;
                        }
                        if ((nw / sw < oWid / sw || recall) && nt + nh <= d.cr - PAD) { // w decrease move up
                            if (nt + nh == d.cr - PAD && (nl + nw < d.cc + PAD || nl > d.cc + d.cx - PAD)) {
                                dec = true;
                            }
                        }
                        if ((nh / sh < oHt / sh || recall) && nt + nh < d.cr - PAD) { // h decrease move up
                            if (nl <= d.cc && nl + nw >= d.cc + d.cx) { // 1 & 3 / 2 & 4,5
                                dec = true;
                            } else if ((nl + nw - PAD > d.cc && nl + nw < d.cc + d.cx) ||
                                (nl > d.cc && nl < d.cc + d.cx - PAD)) { // 1 & 5
                                dec = true;
                            }
                        }
                    }
                    if (dec) {
                        var cnt = 1;
                        while (d.r - cnt >= 0 && !isBlocked(d, cnt)) {
                            cnt++;
                        }
                        cnt -= 1;
                        if (cnt > 0) {
                            decreaseRow(d, cnt);
                            reArrangePanes(panels[d.wid], ghostPanels[d.wid], parseInt(panels[d.wid].style.width, 10),
                                parseInt(getInnerPanel(panels[d.wid]).style.height, 10) + PAD2, true, cnt);
                        }
                    } else if (inc) {
                        var ic = rht ? rht : (wd ? (nt + nh - d.cr + PAD) / sh : 1);
                        if (ic > 1) {
                            while (ic > 0) {
                                increaseRow(d, 1);
                                dProcessedPanels = [];
                                dProcessedPanels.push(processedPanels[0]);
                                movePanesDown(panels[d.wid], ghostPanels[d.wid]);
                                ic--;
                            }
                        } else {
                            increaseRow(d, ic);
                            reArrangePanes(panels[d.wid], ghostPanels[d.wid], parseInt(panels[d.wid].style.width, 10),
                                parseInt(getInnerPanel(panels[d.wid]).style.height, 10) + PAD2, true, ic);
                        }
                    }
                }
            });
        }

        function setBounds(element, x, y, w, h) {
            element.style.left = x + 'px';
            element.style.top = y + 'px';
            element.style.width = w + 'px';
            element.style.height = h + 'px';
        }

        function calc(e) {
            b = panel.getBoundingClientRect();
            x = e.clientX - b.left;
            y = e.clientY - b.top;
            onLeftEdge = x >= PAD - 3 && x <= PAD + 1;
            onRightEdge = x >= (b.width - PAD - 3) && x <= (b.width - PAD + 1);
            onBottomEdge = y >= (b.height - PAD - 3) && y <= (b.height - PAD + 1);
        }

        function onMouseDown(e) {
            onDown(e);
            e.preventDefault();
        }

        function onDown(e) {
            calc(e);
            var isResizing = onRightEdge || onBottomEdge || onLeftEdge;
            ghostPanel.style.opacity = '0.1';
            clicked = {
                x: x,
                y: y,
                cx: e.clientX,
                cy: e.clientY,
                w: b.width,
                h: b.height,
                l: parseInt(panel.style.left, 10),
                t: parseInt(panel.style.top, 10),
                isResizing: isResizing,
                isMoving: !isResizing && canMove(),
                onRightEdge: onRightEdge,
                onLeftEdge: onLeftEdge,
                onBottomEdge: onBottomEdge
            };
            if (isResizing || clicked.isMoving) {
                document.addEventListener('mousemove', onMove);
                panel.classList.remove('dummy');
                panel.style.zIndex = '3';
                getInnerPanel(panel).classList.remove('dummy');
                getInnerMostPanel(panel).classList.remove('dummy');
            }
        }

        function onUp(e) {
            if(checkNotNullEmpty(panel)) {
                calc(e);
                if (clicked && (clicked.isResizing || clicked.isMoving)) {
                    document.removeEventListener('mousemove', onMove);
                }
                panel.classList.add('dummy');
                panel.style.zIndex = '1';
                getInnerPanel(panel).classList.add('dummy');
                getInnerMostPanel(panel).classList.add('dummy');

                ghostPanel.style.opacity = '0';
                var st = {};
                st.left = ghostPanel.style.left;
                st.top = ghostPanel.style.top;
                st.width = ghostPanel.style.width;
                st.height = ghostPanel.style.height;
                getInnerPanel(panel).style.height = parseInt(st.height, 10) + 'px';
                getInnerMostPanel(panel).style.lineHeight = parseInt(st.height, 10) - 45 + 'px';
                setBounds(panel, parseInt(st.left, 10) - PAD, parseInt(st.top, 10) - PAD, parseInt(st.width, 10) + PAD2, parseInt(st.height, 10) + PAD2);
                setMaxHeight();
                clicked = null;
            }
        }

        function canMove() {
            return x > PAD && x < b.width && y > PAD && y < b.height && y < 50;
        }

        function onMove(ee) {
            if (ee.currentTarget.id && $scope.allWids.indexOf(getWidgetId(ee.currentTarget.id)) >= 0 && !clicked) {
                panel = ee.currentTarget;
                ghostPanel = ghostPanels[getWidgetId(ee.currentTarget.id)];
            }
            calc(ee);
            e = ee;
            redraw = true;
        }

        function setComputeRow(wid, row) {
            $scope.dashWidgets.some(function (dw) {
                if (dw.wid == wid) {
                    dw.r = row;
                    dw.cr = dw.r * sh + PAD;
                    return true;
                }
            });
        }

        function animate() {

            requestAnimationFrame(animate);

            if (!redraw) return;

            redraw = false;
            var rearrange = false;

            if (clicked && clicked.isResizing) {
                if (clicked.onRightEdge) {
                    if (clicked.l + clicked.w + e.clientX - clicked.cx <= MAX_COLS_WIDTH) { // Right most edge
                        panel.style.width = Math.max(x + PAD, MIN_WIDTH) + 'px';
                    }
                }
                if (clicked.onBottomEdge) {
                    getInnerPanel(panel).style.height = Math.max(y - PAD2, MIN_HEIGHT - PAD2) + 'px';
                    getInnerMostPanel(panel).style.lineHeight = Math.max(y - PAD2 - 45, MIN_HEIGHT - PAD2 - 45) + 'px';
                    panel.style.height = Math.max(y + PAD2, MIN_HEIGHT + PAD2) + 'px';
                }
                if (clicked.onLeftEdge) {
                    if (clicked.l + e.clientX - clicked.cx >= 0) { // Left most edge
                        var currentWidth = Math.max(clicked.cx - e.clientX + clicked.w, MIN_WIDTH);
                        if (currentWidth > MIN_WIDTH) {
                            panel.style.width = currentWidth + 'px';
                            panel.style.left = clicked.l + e.clientX - clicked.cx + 'px';
                        }
                    }
                }

                var curWid = parseInt(ghostPanel.style.width, 10) + PAD2;
                var curHt = parseInt(ghostPanel.style.height, 10) + PAD2;
                var gw = parseInt(ghostPanel.style.width, 10);
                var pw = parseInt(panel.style.width, 10);

                if (gw - pw >= sw * 0.65) { // w dec
                    ghostPanel.style.width = gw - sw + 'px';
                    if (clicked.onLeftEdge) {
                        ghostPanel.style.left = parseInt(ghostPanel.style.left, 10) + sw + 'px';
                    }
                    rearrange = true;
                } else if (pw - gw >= sw * 0.35) { // w inc
                    ghostPanel.style.width = gw + sw + 'px';
                    if (clicked.onLeftEdge) {
                        ghostPanel.style.left = parseInt(ghostPanel.style.left, 10) - sw + 'px';
                    }
                    rearrange = true;
                }
                var gh = parseInt(ghostPanel.style.height, 10);
                var ph = parseInt(panel.style.height, 10);
                if (gh - ph >= sh * 0.65) { // h dec
                    ghostPanel.style.height = gh - sh + 'px';
                    rearrange = true;
                } else if (ph - gh >= sh * 0.35) { // h inc
                    ghostPanel.style.height = gh + sh + 'px';
                    rearrange = true;
                }
            }
            if (clicked && clicked.isMoving) {
                if (clicked.t + e.clientY - clicked.cy >= 0) {
                    panel.style.top = (clicked.t + e.clientY - clicked.cy) + 'px';
                }
                if (clicked.l + e.clientX - clicked.cx >= 0 && clicked.l + e.clientX - clicked.cx + clicked.w <= MAX_COLS_WIDTH) {
                    panel.style.left = (clicked.l + e.clientX - clicked.cx) + 'px';
                }

                curWid = parseInt(ghostPanel.style.width, 10) + PAD2;
                curHt = parseInt(ghostPanel.style.height, 10) + PAD2;
                var gl = parseInt(ghostPanel.style.left, 10);
                var pl = parseInt(panel.style.left, 10);

                if (gl - pl >= sw * 0.3) { // moving left
                    ghostPanel.style.left = gl - sw + 'px';
                    panel.style.width = parseInt(clicked.w) + PAD + 'px';
                    rearrange = true;
                } else if (pl - gl >= sw * 0.3) { // moving right
                    ghostPanel.style.left = gl + sw + 'px';
                    panel.style.left = parseInt(panel.style.left, 10) - PAD + 'px';
                    rearrange = true;
                }
                var gt = parseInt(ghostPanel.style.top, 10);
                var pt = parseInt(panel.style.top, 10);
                pw = parseInt(panel.style.width, 10);
                if (pt <= sh * 0.3) {
                    ghostPanel.style.top = PAD2 + 'px';
                    panel.style.height = parseInt(clicked.h, 10) + PAD + 'px';
                    rearrange = true;
                } else if (gt - pt > 0) { //moving up
                    $scope.dashWidgets.some(function (dd) {
                        if (dd.wid.toString() != getWidgetId(panel.id) &&
                            pt < clicked.t - sh && pt - (dd.cr + dd.cy) > 0 && pt - (dd.cr + dd.cy) <= sh * 0.3 &&
                            isBlockedPanel(dd.cc, dd.cx, clicked.l, pw)) {
                            ghostPanel.style.top = dd.cr + dd.cy + PAD3 + 'px';
                            panel.style.height = parseInt(clicked.h, 10) + PAD + 'px';
                            rearrange = true;
                            return true;
                        }
                    });
                } else if (pt - gt > 0) { // moving down
                    var curPid = getWidgetId(panel.id);
                    $scope.dashWidgets.forEach(function (dd) {
                        if (dd.wid.toString() != curPid &&
                            gt + curHt - PAD == dd.cr && pt - dd.cr > sh * 0.3 && pt - dd.cr < sh) {
                            if (dd.cc >= clicked.l && dd.cc + dd.cx <= clicked.l + pw && pt - clicked.t > dd.cy) { //Top: big, Bottom: small
                                decreaseRow(dd, clicked.h / sh);
                                ghostPanel.style.top = dd.cr + dd.cy + PAD3 + 'px';
                                setComputeRow(parseInt(curPid, 10), (dd.cr + dd.cy + PAD) / sh);
                                rearrange = true;
                            } else if (gl >= dd.cc && gl + curWid <= dd.cc + dd.cx) { //Top: small, Bottom: big / both Equal
                                var cnt = 1;
                                while (dd.r - cnt >= 0 && !isBlocked(dd, cnt, parseInt(curPid, 10))) {
                                    cnt++;
                                }
                                cnt -= 1;
                                if (cnt > 0) {
                                    decreaseRow(dd, cnt);
                                    reArrangePanes(panels[dd.wid], ghostPanels[dd.wid], dd.cx, dd.cy, false, cnt);
                                }
                                ghostPanel.style.top = dd.cr + dd.cy + PAD3 + 'px';
                                setComputeRow(parseInt(curPid, 10), (dd.cr + dd.cy + PAD) / sh);
                            } else if (isBlockedPanel(dd.cc, dd.cx, gl, curWid, true)) { // Top: big, Bottom: small
                                cnt = 1;
                                while (dd.r - cnt >= 0 && !isBlocked(dd, cnt, parseInt(curPid, 10))) {
                                    cnt++;
                                }
                                cnt -= 1;
                                if (cnt >= dd.y) {
                                    decreaseRow(dd, cnt);
                                    if (dd.cr + dd.cy > gt) {
                                        ghostPanel.style.top = dd.cr + dd.cy + PAD3 + 'px';
                                        setComputeRow(parseInt(curPid, 10), (dd.cr + dd.cy + PAD) / sh);
                                    }
                                } else if (gt + curHt - PAD > dd.cr) {
                                    var ic = clicked.h / sh;
                                    while (ic > 0) {
                                        increaseRow(dd, 1);
                                        dProcessedPanels = [];
                                        dProcessedPanels.push(parseInt(curPid, 10));
                                        movePanesDown(panels[dd.wid], ghostPanels[dd.wid]);
                                        ic--;
                                    }
                                }
                            }
                        }
                    });
                }
            }

            if (rearrange) {
                processedPanels = [];
                reArrangePanes(panel, ghostPanel, curWid, curHt, clicked.isMoving);
                var d = undefined;
                var curId = getWidgetId(panel.id);
                $scope.dashWidgets.some(function (dw) {
                    if (dw.wid.toString() == curId) {
                        d = dw;
                        return true;
                    }
                });
                if (d != undefined) {
                    var cnt = 1;
                    while (d.r - cnt >= 0 && !isBlocked(d, cnt)) {
                        cnt++;
                    }
                    cnt -= 1;
                    if (cnt > 0) {
                        decreaseRow(d, cnt);
                        reArrangePanes(panel, ghostPanel, parseInt(panel.style.width, 10),
                            parseInt(getInnerPanel(panel).style.height, 10) + PAD2, false, cnt);
                    }
                }
                return;
            }

            if (onRightEdge && onBottomEdge) {
                panel.style.cursor = 'nwse-resize';
            } else if (onBottomEdge && onLeftEdge) {
                panel.style.cursor = 'nesw-resize';
            } else if (onRightEdge || onLeftEdge) {
                panel.style.cursor = 'ew-resize';
            } else if (onBottomEdge) {
                panel.style.cursor = 'ns-resize';
            } else if (canMove()) {
                panel.style.cursor = 'move';
            } else {
                panel.style.cursor = 'default';
            }
        }
    }
])
;

domainControllers.controller('ViewDashboardController', ['$scope', 'dashboardService', 'requestContext','$timeout',
    function ($scope, dashboardService, requestContext, $timeout) {

        var ul = document.getElementById("viewDash");
        var PAD = 5;
        var PAD2 = PAD * 2;
        var MAX_COLS_WIDTH = parseInt(getComputedStyle(ul).width, 10) - PAD2;
        var sw = MAX_COLS_WIDTH / 12;
        var sh = 110;

        function addComputeData() {
            var maxHeight = 100;
            $scope.dashWidgets.forEach(function (d) {
                d.cx = d.x * sw;
                d.cc = d.c * sw;
                d.cy = d.y * sh - PAD2;
                d.cr = d.r * sh + PAD;
                if (maxHeight < d.cr + d.cy) {
                    maxHeight = d.cr + d.cy;
                }
            });
            $scope.maxHeight = maxHeight - PAD + 'px';
        }

        $scope.dbId = requestContext.getParam("dbid");
        $scope.showLoading();
        $scope.loading = true;
        dashboardService.getById($scope.dbId,'y').then(function (data) {
            $scope.db = data.data;
            if (checkNotNullEmpty($scope.db.conf)) {
                $scope.dashWidgets = JSON.parse($scope.db.conf);
                addComputeData();
            }
        }).catch(function error(msg) {
            $scope.showErrorMsg(msg);
        }).finally(function () {
            $scope.loading = false;
            $timeout(function() {
                $scope.hideLoading();
            },100);
        });
    }
]);

domainControllers.controller('MainDashboardController', ['$scope', '$timeout', '$sce', 'dashboardService', 'INVENTORY', 'configService', 'domainCfgService','requestContext','matService','$location','$window',
    function ($scope, $timeout, $sce, dashboardService, INVENTORY, configService, domainCfgService, requestContext, matService,$location,$window) {

        var mapColors, mapRange, invPieColors, invPieOrder, entPieColors, entPieOrder, tempPieColors, tempPieOrder;
        $scope.mc = $scope.mr = undefined;
        $scope.predictiveDiv = false;
        $scope.openPred = function () {
            $window.open($location.$$absUrl.substring(0,$location.$$absUrl.indexOf("#")).concat($scope.predUrl),'_blank');
        };
        $scope.predData = {};
        $scope.predUrl = "";
        $scope.showLoading();
        domainCfgService.getAssetSysCfg('2').then(function(data) {
            $scope.allAssets = data.data;
        }).catch(function error(msg) {
            $scope.showErrorMsg(msg);
        }).finally(function () {
            $scope.hideLoading();
        });

        $scope.filterAssets = function (query) {
            var rData = {results: []};
            for(var key in $scope.allAssets) {
                if($scope.allAssets[key].toLowerCase().indexOf(query.term.toLowerCase()) != -1) {
                    rData.results.push({'text': $scope.allAssets[key], 'id': key});
                }
            }
            query.callback(rData);
        };

        $scope.filterStatus = function(query){

            var sData = {results: []};

            sData.results[0] = {id:'tn',text:"Normal"};
            sData.results[1] = {id:'tl',text:"Low"};
            sData.results[2] = {id:'th',text:"High"};
            sData.results[3] = {id:'tu',text:"Unknown"};

            query.callback(sData);
        }

        function constructPie(data, color, order, pieId, label, ets) {
            if(checkNotNullEmpty(ets)) {
                ets.forEach(function(exStatus){
                    delete data[exStatus.id];
                });
            }
            var d = [];
            var iEntEvent = false;
            var iTempEvent = false;
            var isOneDataAvailable = false;
            for (var or in order) {
                var dd = order[or];

                if(checkNotNullEmpty(ets)){
                    ets.forEach(function(ele){
                        if(ele.id == dd){
                            dd = undefined;
                        }
                    });
                }

                var o = {};
                if (dd == INVENTORY.stock.STOCKOUT) {
                    o.label = "Zero stock";
                    o.color = color[0];
                } else if (dd == INVENTORY.stock.UNDERSTOCK) {
                    o.label = "Min";
                    o.color = color[1];
                } else if (dd == INVENTORY.stock.OVERSTOCK) {
                    o.label = "Max";
                    o.color = color[2];
                } else if (dd == "n") {
                    o.label = "Normal";
                    o.color = color[3];
                } else if (dd == "a") {
                    o.label = "Active";
                    o.color = color[0];
                    iEntEvent = true;
                } else if (dd == "i") {
                    o.label = "Inactive";
                    o.color = color[1];
                    iEntEvent = true;
                } else if (dd == "tn") {
                    o.label = "Normal";
                    o.color = color[0];
                    iTempEvent = true;
                } else if (dd == "tl") {
                    o.label = "Low";
                    o.color = color[1];
                    iTempEvent = true;
                } else if (dd == "th") {
                    o.label = "High";
                    o.color = color[2];
                    iTempEvent = true;
                } else if (dd == "tu") {
                    o.label = "Unknown";
                    o.color = color[3];
                    iTempEvent = true;
                } else {
                    o.label = dd;
                }
                if (dd == $scope.mapEvent) {
                    o.isSliced = 1;
                }
                o.value = data[dd] || 0;
                o.toolText = "$label: $value of $unformattedSum" + " " + label;
                if (data[dd]) {
                    isOneDataAvailable = true;
                }
                o.showValue = o.value > 0 ? 1 : 0;
                o.link = "JavaScript: angular.element(document.getElementById('cid')).scope().constructMapData('" + dd + "');";
                d.push(o);
            }
            var subCaption = undefined;
            var pSubCaption;
            var tSubCaption = undefined;
            if (checkNotNullEmpty($scope.exFilter)) {
                if ($scope.exType == "mTag") {
                    subCaption = "<b>Material tag(s): </b>" + $scope.exFilterText;
                } else if ($scope.exType == "mId") {
                    subCaption = "<b>Material: </b>" + $scope.exFilterText;
                }
            }
            var fPeriod;
            if(!iTempEvent) {
                fPeriod = $scope.period == "0" ? (iEntEvent ? $scope.aper : "0") : $scope.period;
            } else {
                fPeriod = $scope.tperiod;
            }
            if (checkNotNullEmpty($scope.eTag)) {
                subCaption = (checkNullEmpty(subCaption) ? '' : subCaption + ', ') + "<b>" + $scope.resourceBundle.kiosk + " tag(s): </b>" + $scope.eTagText;
            }
            pSubCaption = subCaption;
            if (checkNotNullEmpty(fPeriod)) {
                subCaption = (checkNullEmpty(subCaption) ? '' : subCaption + ', ') + "<b>Period: </b>" + fPeriod + " day(s)";
                if(iTempEvent && fPeriod != 'M_0') {
                    var fp = fPeriod.substr(2);
                    tSubCaption = "<b>Period: </b>" + (fPeriod.indexOf('M') == 0 ? fp + " Minutes(s)" : (fPeriod.indexOf('H') == 0 ? fp + " Hour(s)" : fp + " Day(s)"));
                }
            }

            if (checkNotNullEmpty($scope.asset) && $scope.asset.length > 0) {
                tSubCaption = (checkNullEmpty(tSubCaption) ? '' : tSubCaption + ', ') + "<b>Asset type: </b>";
                var first = true;
                $scope.asset.forEach(function(data) {
                    if(!first) {
                        tSubCaption += ", " + data.text;
                    } else {
                        tSubCaption += data.text;
                        first = false;
                    }
                });
            }

            if (checkNotNullEmpty(ets) && ets.length > 0) {
                tSubCaption = (checkNullEmpty(tSubCaption) ? '' : tSubCaption + ', ') + "<b>Exclude temperature state: </b>";
                var first = true;
                ets.forEach(function(data) {
                    if(!first) {
                        tSubCaption += ", " + data.text;
                    } else {
                        tSubCaption += data.text;
                        first = false;
                    }
                });
            }

            if (pieId == "p1") {
                $scope.p1subCaption = $sce.trustAsHtml(subCaption || '&nbsp;');
                $scope.pSubCaption = $sce.trustAsHtml(pSubCaption || '&nbsp;');
            } else if (pieId == "p2") {
                $scope.p2subCaption = $sce.trustAsHtml(tSubCaption || '&nbsp;');
            }
            return isOneDataAvailable ? d : [];
        }

        function constructStack(data,seriesName,barColor, denominator,code){
            var stackSeries = {};
            if(denominator > 0) {
                stackSeries.seriesname = seriesName;
                stackSeries.data = [{}];
                stackSeries.data[0].value = data;
                stackSeries.data[0].color = barColor;
                stackSeries.data[0].toolText = seriesName + ": " + ((data / denominator) * 100).toFixed(2) + "% (" + data + " of " + denominator + " " + $scope.resourceBundle['kiosks.lower'] + ")";
                stackSeries.data[0].link = "JavaScript: angular.element(document.getElementById('cid')).scope().constructMapData('" + code + "');";
            }
            return stackSeries;
        }

        function constructPieData(data) {
            if(checkNotNullEmpty(data)) {
                var entDomainTotal = (data.entDomain.a || 0) + (data.entDomain.i || 0);
                $scope.pieData = [];
                $scope.pieData[0] = constructPie(data.invDomain, invPieColors, invPieOrder, "p1", $scope.resourceBundle['inventory'] + " " + $scope.resourceBundle['order.items'] + " (" + $scope.resourceBundle['across'] + " " + entDomainTotal + " " + $scope.resourceBundle['kiosks.lowercase'] + ")");
                $scope.pieData[1] = constructPie(data.tempDomain, tempPieColors, tempPieOrder, "p2", "assets", $scope.exTempState);
            }
        }

        function constructPredWarning(data){
            if(checkNotNullEmpty(data) && data.num != "0" ){
                $scope.predData = data;
                $scope.predData.per = $scope.predData.per?$scope.predData.per.toFixed(1):undefined;
                $scope.predictiveDiv = true;
            }
        }

        $scope.loading = true;
        $scope.mloading = true;
        $scope.links = [];
        $scope.period = "0";
        $scope.tperiod = 'M_0';
        $scope.today = new Date();

        function constructStackData(data) {
            var entDomainTotal = (data.a || 0) + (data.i || 0);
            $scope.stackData = [];
            var sData = constructStack(data.a, "Active", entPieColors[0], entDomainTotal, "a");
            if(!checkNullEmptyObject(sData)) {
                $scope.stackData[0] = sData;
            }
            sData = constructStack(data.i, "Inactive", entPieColors[1], entDomainTotal,"i");
            if(!checkNullEmptyObject(sData)) {
                $scope.stackData[1] = sData;
            }
            $scope.stackLabel = [{label: ''}];
        }

        $scope.getDashboardData = function (filter, level, index, skipLink, skipCache) {
            if (checkNotNullEmpty($scope.dcntry)) {
                $scope.loading = true;
                $scope.mloading = true;
                $scope.showLoading();
                var asset = '';
                if(checkNotNullEmpty($scope.asset) && $scope.asset.length > 0) {
                    var first = true;
                    $scope.asset.forEach(function (data) {
                        if(!first) {
                            asset += ","+data.id;
                        } else {
                            asset += data.id;
                            first = false;
                        }

                    });
                }
                $scope.fdate = (checkNotNullEmpty($scope.date)?formatDate($scope.date):undefined);
                if(checkNotNullEmpty($scope.excludeTag)) {
                    $scope.excludeETag = constructModel($scope.excludeTag);
                    $scope.eeTagObj = angular.copy($scope.excludeTag);
                } else {
                    $scope.excludeETag = $scope.eeTagObj = undefined;
                }
                if(checkNotNullEmpty($scope.eTag)) {
                    $scope.includeETag = constructModel($scope.eTag);
                    $scope.eTagObj = angular.copy($scope.eTag);
                    $scope.eTagText = $scope.includeETag.replace(/'/g, '');
                } else {
                    $scope.includeETag = $scope.eTagObj = $scope.eTagText = undefined;
                }
                /*if(checkNotNullEmpty($scope.mtag)) {
                    $scope.exFilter = $scope.constructModel($scope.mtag);
                    $scope.mTagObj = angular.copy($scope.mtag);
                    $scope.exFilterText = $scope.exFilter.replace(/'/g, '');
                } else {
                    $scope.exFilter = $scope.mTagObj = $scope.exFilterText = undefined;
                }*/

                dashboardService.get(filter, level, $scope.exFilter, $scope.exType, $scope.period,$scope.tperiod,asset,$scope.includeETag, $scope.fdate, $scope.excludeETag, skipCache).then(function (data) {
                    if (typeof loadDashboardFusionMaps === "function") {
                        $scope.showMap = true;
                        $scope.showSwitch = true;
                    } else {
                        $scope.showMap = false;
                        $scope.showSwitch = false;
                    }
                    $scope.dashboardView = data.data;
                    var linkText;
                    if ($scope.dashboardView.mLev == "country") {
                        linkText = $scope.locationMapping.data[$scope.dashboardView.mTy].name;
                        $scope.mapType = "maps/" + linkText;
                    } else if ($scope.dashboardView.mLev == "state") {
                        linkText = $scope.dashboardView.mTyNm;
                        $scope.mapType = "maps/" + $scope.dashboardView.mTy;
                    } else {
                        linkText = $scope.dashboardView.mTyNm.substr($scope.dashboardView.mTyNm.lastIndexOf("_") + 1);
                        $scope.showMap = false;
                        $scope.showSwitch = false;
                        $scope.mapData = [];
                    }
                    $scope.links.push({filter: filter, text: linkText, level: level});
                    constructPieData($scope.dashboardView);
                    if(typeof loadDashboardFusionMaps === "function") {
                        constructStackData($scope.dashboardView.entDomain);
                    }
                    constructPredWarning($scope.dashboardView.pred);
                    $scope.constructMapData($scope.mapEvent, true);
                }).catch(function error(msg) {
                    $scope.showErrorMsg(msg);
                }).finally(function () {
                    $scope.hideLoading();
                    $scope.loading = false;
                });
            }
        };

        // main db
        $scope.initDefaults = function(skipCache) {
            var filter = undefined;
            var level = undefined;
            var valid = true;
            var mtReq = false; // Material tag is mandatory when any 1 filter is applied via URL
            var state = requestContext.getParam("st");
            var filters = "";
            $scope.links=[];
            if (requestContext.getParam("dt") && state) {
                if(($scope.dstate && state != $scope.dstate) ||
                    ($scope.ddist && state != $scope.ddist)) {
                    valid = false;
                }
                if(valid) {
                    filter = state + "_" + requestContext.getParam("dt");
                    level = 'district';
                    if(!$scope.dstate) {
                        $scope.links.push({filter: undefined, text: $scope.locationMapping.data[$scope.dcntry].name, level: undefined})
                    }
                    if(!$scope.ddist) {
                        $scope.links.push({filter: $scope.dstate || state, text: $scope.dstate || state, level: 'state'})
                    }
                    mtReq = true;
                }
                filters += "dt=" + requestContext.getParam("dt");
                filters += "&st=" + state;
            } else if (state) {
                if($scope.dstate && filter != $scope.dstate) {
                    valid = false;
                }
                if(valid) {
                    filter = state;
                    level = 'state';
                    $scope.links.push({filter: undefined, text: $scope.locationMapping.data[$scope.dcntry].name, level: undefined})
                    mtReq = true;
                }
                filters += "st=" + state;
            }
            if (requestContext.getParam("p")) {
                var p = requestContext.getParam("p");
                if (p == '0' || p == '1' || p == '2' || p == '3' || p == '7' || p == '30') {
                    $scope.period = p;
                    mtReq = true;
                }
            } else {
                $scope.period = "0";
            }
            $scope.showActivityLabel = $scope.period == "0";

            if (requestContext.getParam("tp")) {
                $scope.tperiod = requestContext.getParam("tp");
                mtReq = true;
            } else {
                $scope.tperiod = 'M_0';
            }

            if (requestContext.getParam("ets")){
                if(checkNotNullEmpty(filters)) {
                    filters += "&";
                }
                filters += "ets=" + requestContext.getParam("ets");
            }

            if (requestContext.getParam("d")) {
                $scope.date = parseUrlDate(requestContext.getParam("d"));
                mtReq = true;
            } else {
                $scope.date = undefined;
            }

            if (requestContext.getParam("et")) {
                var eTag = requestContext.getParam("et").split(",");
                $scope.eTag = [];
                eTag.forEach(function (data) {
                    $scope.eTag.push({'text': data, 'id': data});
                });
                $scope.includeETag = constructModel($scope.eTag);
                mtReq = true;
                if(checkNotNullEmpty(filters)) {
                    filters += "&";
                }
                filters += "et=" + requestContext.getParam("et");
            } else if (requestContext.getParam("eet")) {
                var eeTag = requestContext.getParam("eet").split(",");
                $scope.excludeTag = [];
                eeTag.forEach(function (data) {
                    $scope.excludeTag.push({'text': data, 'id': data});
                });
                $scope.excludeETag = constructModel($scope.excludeTag);
                mtReq = true;
                if(checkNotNullEmpty(filters)) {
                    filters += "&";
                }
                filters += "eet=" + requestContext.getParam("eet");
            } else {
                $scope.eTag = undefined;
            }

            if (requestContext.getParam("a")) {
                var assets = requestContext.getParam("a").split(",");
                $scope.asset = [];
                $scope.assetText = [];
                assets.forEach(function (data) {
                    $scope.asset.push({'text': $scope.allAssets[data], 'id': data});
                    $scope.assetText.push(data);
                });
                mtReq = true;
            } else {
                $scope.asset = undefined;
                $scope.assetText = undefined;
            }
            var loadDash = true;
            // WARNING: make sure this is checked at last
            if (requestContext.getParam("mt")) {

                var mTag = requestContext.getParam("mt").split(",");
                $scope.mtag = [];
                mTag.forEach(function(data){
                    $scope.mtag.push({'text': data, 'id': data});
                });
                $scope.exFilter = constructModel($scope.mtag);
                $scope.exFilterText = requestContext.getParam("mt");
                $scope.exType = 'mTag';
                if(checkNotNullEmpty(filters)) {
                    filters += "&";
                }
                filters += "mt=" + requestContext.getParam("mt");
            } else if (requestContext.getParam("mid")) {
                var mid = requestContext.getParam("mid");
                loadDash = false;
                $scope.showLoading();
                matService.get(mid).then(function (data) {
                    $scope.material = {'text': data.data.mnm, 'id': data.data.mId};
                    $scope.exFilterText = data.data.mnm;
                    $scope.getDashboardData(filter, level, null, null, skipCache);
                }).catch(function error(msg) {
                }).finally(function () {
                    $scope.hideLoading();
                });
                $scope.exFilter = mid;
                $scope.exType = 'mId';
                if(checkNotNullEmpty(filters)) {
                    filters += "&";
                }
                filters += "mid=" + requestContext.getParam("mid");
            } else if(mtReq) {
                $scope.mtag = $scope.exFilter = $scope.exFilterText = $scope.exType = undefined;
                if(checkNotNullEmpty(filters)) {
                    filters += "&";
                }
                filters += "nmt=true";
            } else {
                $scope.mtag = $scope.dfmt;
                $scope.exFilter = constructModel($scope.mtag);
                var text = constructModel($scope.mtag).replace(/'/g, '');
                $scope.exFilterText = text;
                $scope.exType = 'mTag';
                if (checkNotNullEmpty($scope.mtag)) {
                    if (checkNotNullEmpty(filters)) {
                        filters += "&";
                    }
                    filters += "mt=" + text;
                }
            }
            if(loadDash) {
                $scope.getDashboardData(filter, level,null,null,skipCache);
            }
            $scope.predUrl = '#/dashboard/inventory/predictive?' + filters;
        };
        $scope.showLoading();
        domainCfgService.getMapLocationMapping().then(function (data) {
            if (checkNotNullEmpty(data.data)) {
                $scope.locationMapping = angular.fromJson(data.data);
                domainCfgService.getDashboardCfg().then(function (data) {
                    if (checkNotNullEmpty(data.data.dmtg)) {
                        if(checkNotNullEmpty(data.data.dmtg)) {
                            $scope.mtag = [];
                            data.data.dmtg.forEach(function (data) {
                                $scope.mtag.push({'text': data, 'id': data});
                            });
                        }
                        $scope.dfmt = angular.copy($scope.mtag);
                        $scope.exFilter = constructModel($scope.mtag);
                        $scope.exFilterText = data.data.dmtg;
                        $scope.exType = 'mTag';
                    }
                    $scope.aper = data.data.aper || "7";
                    if(checkNotNullEmpty(data.data.exet)) {
                        $scope.excludeTag = [];
                        data.data.exet.forEach(function (data) {
                            $scope.excludeTag.push({'text': data, 'id': data});
                        });
                        $scope.excludeETag = constructModel($scope.excludeTag);
                    }

                    var exStatusList = {"Normal":"tn","Low":"tl","High":"th","Unknown":"tu"};

                    if(checkNotNullEmpty(data.data.exts)) {
                        $scope.exTempState = [];
                        data.data.exts.forEach(function (data) {
                            $scope.exTempState.push({'text': data, 'id': exStatusList[data]});
                        });
                    }
                    domainCfgService.getSystemDashboardConfig().then(function (data) {
                        var dconfig = angular.fromJson(data.data);
                        mapColors = dconfig.mc;
                        $scope.mc = mapColors;
                        mapRange = dconfig.mr;
                        $scope.mr = mapRange;
                        invPieColors = dconfig.pie.ic;
                        invPieOrder = dconfig.pie.io;
                        entPieColors = dconfig.pie.ec;
                        entPieOrder = dconfig.pie.eo;
                        tempPieOrder = dconfig.pie.to;
                        tempPieColors = dconfig.pie.tc;
                        if( $scope.tempOnlyAU ) {
                            $scope.mapEvent = tempPieOrder[0];
                        } else {
                            $scope.mapEvent = invPieOrder[0];
                        }
                        $scope.initDefaults();
                        initWatches();
                    });
                });
            } else {
                $scope.loading = false;
                $scope.mloading = false;
            }
        }).catch(function error(msg) {
        }).finally(function () {
            $scope.hideLoading();
        });


        $scope.hardRefreshDashboard = function(){
            $scope.initDefaults(true);
        }

        $scope.pOpt = {
            "showLabels": 0,
            "showLegend": 1,
            "showPercentValues": 1,
            "showPercentInToolTip": 0,
            "enableSmartLabels": 1,
            "toolTipSepChar": ": ",
            "theme": "fint",
            "enableRotation": 0,
            "enableMultiSlicing": 0,
            //"startingAngle": 90,
            "labelFontSize": 12,
            "slicingDistance": 15,
            "useDataPlotColorForLabels": 1,
            "subCaptionFontSize": 10,
            "interactiveLegend": 0,
            "exportEnabled": 1
        };
        $scope.pieOpt = [];
        $scope.pieOpt[0] = angular.copy($scope.pOpt);
        $scope.p1caption = "Inventory";

        $scope.stackBarOptions = angular.copy($scope.pOpt);
        $scope.stackBarOptions.stack100Percent = 1;
        $scope.stackBarOptions.exportEnabled = 0;
        $scope.stackBarOptions.showLegend = 0;
        $scope.stackBarOptions.showValues = 0;
        $scope.stackBarOptions.showLabels = 0;
        $scope.stackBarOptions.showXAxisLine = 0;
        $scope.stackBarOptions.showYAxisLine = 0;
        $scope.stackBarOptions.maxBarHeight = 5;
        $scope.stackBarOptions.valueFontColor = '#ffffff';


        $scope.p2caption = "Temperature";
        $scope.pieOpt[1] = angular.copy($scope.pOpt);

        $scope.mapOpt = {
            "nullEntityColor": "#cccccc",
            "nullEntityAlpha": "50",
            "hoverOnNull": "0",
            "showLabels": "0",
            "showCanvasBorder": "0",
            "useSNameInLabels": "0",
            "toolTipSepChar": ": ",
            "legendPosition": "BOTTOM",
            "borderColor": "FFFFFF",
            //"entityBorderHoverThickness": "2",
            "interactiveLegend": 1,
            "exportEnabled": 1,
            "baseFontColor": "#000000"
        };

        function setMapRange(event) {
            $scope.mapRange = {color: []};
            for (var i = 1; i < mapRange[event].length; i++) {
                var o = {};
                o.minvalue = mapRange[event][i - 1];
                o.maxvalue = mapRange[event][i];
                o.code = mapColors[event][i - 1];
                if (i == 1) {
                    o.displayValue = "<" + mapRange[event][1] + "%"
                } else if (i == mapRange[event].length - 1) {
                    o.displayValue = ">" + mapRange[event][i - 1] + "%"
                } else {
                    o.displayValue = mapRange[event][i - 1] + "-" + mapRange[event][i] + "%"
                }
                $scope.mapRange.color.push(o);
            }
        }

        $scope.barOpt = {
            "showAxisLines": "0",
            "valueFontColor":"#000000",
            "theme": "fint",
            "yAxisName": "Percentage",
            "yAxisNameFontSize": 12,
            "exportEnabled": 1,
            "caption": ' ',
            "yAxisMaxValue": 100
        };

        function constructBarData(data, allData, event, addLink, level) {
            var bData = [];
            for (var f in allData) {
                if (checkNotNullEmpty(f) && f != "MAT_BD") {
                    bData.push({"label": f});
                }
            }
            for (var n in allData) {
                if (checkNotNullEmpty(n) && n != "MAT_BD") {
                    var per = 0;
                    var value = 0;
                    var den = 0;
                    var kid = undefined;
                    if (data != undefined && data[n] != undefined) {
                        per = data[n].per;
                        value = data[n].value;
                        den = data[n].den;
                        kid = data[n].kid;
                    }
                    for (var i = 0; i < bData.length; i++) {
                        var bd = bData[i];
                        if (n == bd.label) {
                            bd.value = per;
                            bd.displayValue = bd.value + "%";
                            if(event == '200' || event == '201' || event == '202' || event == 'n') {
                                bd.toolText = value + " of " + den + (level == undefined ? " materials" : " inventory items");
                            } else if(event == 'a' || event == 'i') {
                                bd.toolText = value + " of " + den + " " + $scope.resourceBundle['kiosks.lower'];
                            } else if(event == 'tu' || event == 'tl' || event == 'th' || event == 'tn') {
                                bd.toolText = value + " of " + den + " assets";
                            }
                            for (var r = 1; r < mapRange[event].length; r++) {
                                if (per <= mapRange[event][r]) {
                                    bd.color = mapColors[event][r - 1];
                                    break;
                                }
                            }
                            if (addLink) {
                                var filter = bd.label;
                                if ($scope.dashboardView.mLev == "state") {
                                    filter = $scope.dashboardView.mTyNm + "_" + bd.label;
                                }
                                bd.link = "JavaScript: angular.element(document.getElementById('cid')).scope().addFilter('" + filter + "','" + level + "')";
                            } else if((event == '200' || event == '201' || event == '202' || event == 'n')) {
                                var search = "?eid="+kid;
                                if(checkNotNullEmpty($scope.mtag) && $scope.mtag instanceof Array) {
                                    search += "&mtag="+ $scope.mtag.map(function(val){return val.text;}).join(',');
                                }
                                if(event != 'n'){
                                    search += "&abntype="+event;
                                    if(checkNotNullEmpty($scope.period) && $scope.period != '0'){
                                        search += "&dur="+$scope.period;
                                    }
                                }
                                bd.link = "JavaScript: angular.element(document.getElementById('cid')).scope().drillDownInventory('" + search + "')";
                            } else if(event == 'a' || event == 'i') {
                                var fromDate = angular.copy($scope.date || $scope.today);
                                fromDate.setDate(fromDate.getDate() - ($scope.period || $scope.aper) + ($scope.date ? 1 : 0));
                                bd.link = "N-#/inventory/transactions/?eid="+kid+"&from="+formatDate2Url(fromDate)+"&to="+formatDate2Url($scope.date || $scope.today);
                                if(checkNotNullEmpty($scope.mtag) && $scope.mtag.length == 1) {
                                    bd.link += "&tag=" + $scope.mtag[0].text;
                                }
                            } else if(event == 'tu' || event == 'tl' || event == 'th' || event == 'tn') {
                                var at = '&at=md';
                                if(checkNotNullEmpty($scope.assetText)) {
                                    at = '&at=' + $scope.assetText.join(",");
                                }
                                if(!$scope.iMan) {
                                    bd.link = "N-#/assets/all?eid=" + kid + "&alrm=" + (event == 'tn' ? '4' : (event == 'tu' ? '3' : '1')) + at + "&awr=1&ws=1";
                                }
                            }
                            var found = false;
                            //Arrange data in descending order
                            for (var j = 0; j < bData.length; j++) {
                                if (checkNullEmpty(bData[j].value) || bd.value > bData[j].value) {
                                    bData.splice(j, 0, bd);
                                    found = true;
                                    break;
                                }
                            }
                            if (found) {
                                bData.splice(i + 1, 1);
                            }
                            break;
                        }
                    }
                }
            }
            $scope.barHeight = bData.length * 20 + 80;
            $scope.barData = bData;
        }
        function constructMatBarData(data, allData, event) {
            var bData = [];
            for (var f in allData) {
                if (checkNotNullEmpty(f)) {
                    bData.push({"label": f});
                }
            }
            for (var n in allData) {
                if (checkNotNullEmpty(n)) {
                    var value = 0;
                    var den = 0;
                    if (data != undefined && data[n] != undefined) {
                        value = data[n].value;
                        den = data[n].den;
                    }
                    function getLocationParams(links){
                        var params = '';
                        if(checkNotNullEmpty(links) && links.length > 0){
                            for(var l = 0; l<links.length; l++){
                                if(links[l].level == 'state' && checkNotNullEmpty(links[l].text)){
                                    params += '&state='+ links[l].text;
                                } else if(links[l].level == 'district' && checkNotNullEmpty(links[l].text)){
                                    params += '&district='+ links[l].text;
                                }
                            }
                        }
                        return params;
                    }
                    for (var i = 0; i < bData.length; i++) {
                        var bd = bData[i];
                        if (n == bd.label) {
                            bd.value = Math.round(value / den  * 1000) / 10;
                            bd.displayValue = bd.value + "%";
                            bd.toolText = value + " of " + den + " " + $scope.resourceBundle['kiosks.lower'];
                            for (var r = 1; r < mapRange[event].length; r++) {
                                if (bd.value <= mapRange[event][r]) {
                                    bd.color = mapColors[event][r - 1];
                                    break;
                                }
                            }
                            // generate link to stock views page
                            var search;
                            if(checkNotNullEmpty(data[n].mid) && (event == INVENTORY.stock.STOCKOUT ||
                                event == INVENTORY.stock.UNDERSTOCK || event == INVENTORY.stock.OVERSTOCK)
                                && data[n].value != 0) {
                                search = "?abntype="+event + "&mid="+data[n].mid;
                                if(checkNotNullEmpty($scope.period) && $scope.period != '0'){
                                    search += "&dur="+$scope.period;
                                }
                                search += getLocationParams($scope.links);
                            } else if(event == 'n' && checkNotNullEmpty(data[n].mid) && data[n].value != 0){
                                search = "?mid="+data[n].mid;
                                search += getLocationParams($scope.links);
                            }
                            if(checkNotNullEmpty(search) && checkNotNullEmpty($scope.eTag) && $scope.eTag instanceof Array){
                                search += "&etag="+$scope.eTag.map(function(val){
                                        return val.text;}).join(',');
                            }else if(checkNotNullEmpty(search) && checkNotNullEmpty($scope.excludeTag) && $scope.excludeTag instanceof Array){
                                search += "&eetag="+$scope.excludeTag.map(function(val){
                                        return val.text;}).join(',');
                            }
                            if(checkNotNullEmpty(search)){
                                bd.link = "JavaScript: angular.element(document.getElementById('cid')).scope().drillDownInventory('" + search + "')";
                            }
                            var found = false;
                            //Arrange data in descending order
                            for (var j = 0; j < bData.length; j++) {
                                if (checkNullEmpty(bData[j].value) || bd.value > bData[j].value) {
                                    bData.splice(j, 0, bd);
                                    found = true;
                                    break;
                                }
                            }
                            if (found) {
                                bData.splice(i + 1, 1);
                            }
                            break;
                        }
                    }
                }
            }
            $scope.matBarHeight = bData.length * 20 + 80;
            $scope.matBarData = bData;
        }

        $scope.constructMapData = function (event, init) {
            if ($scope.mapEvent == event && !init) {
                return;
            }
            $scope.mloading = true;
            var subData;
            $scope.caption = '';
            $scope.subCaption = '';
            var iEntEvent = false;
            var iTempEvent = false;
            if (event == INVENTORY.stock.STOCKOUT) {
                subData = $scope.dashboardView.inv[INVENTORY.stock.STOCKOUT];
                $scope.caption = "Inventory";
                $scope.subCaption = "<b>Stock:</b> Zero";
            } else if (event == INVENTORY.stock.UNDERSTOCK) {
                subData = $scope.dashboardView.inv[INVENTORY.stock.UNDERSTOCK];
                $scope.caption = "Inventory";
                $scope.subCaption = "<b>Stock:</b> Min";
            } else if (event == INVENTORY.stock.OVERSTOCK) {
                subData = $scope.dashboardView.inv[INVENTORY.stock.OVERSTOCK];
                $scope.caption = "Inventory";
                $scope.subCaption = "<b>Stock:</b> Max";
            } else if (event == 'n') {
                subData = $scope.dashboardView.inv["n"];
                $scope.caption = "Inventory";
                $scope.subCaption = "<b>Stock:</b> Normal";
            } else if (event == 'a') {
                subData = $scope.dashboardView.ent["a"];
                $scope.caption = "Activity";
                $scope.subCaption = "<b>Status: </b> Active";
                iEntEvent = true;
            } else if (event == 'i') {
                subData = $scope.dashboardView.ent["i"];
                $scope.caption = "Activity";
                $scope.subCaption = "<b>Status: </b> Inactive";
                iEntEvent = true;
            } else if (event == 'tn') {
                subData = $scope.dashboardView.temp["tn"];
                $scope.caption = "Temperature";
                $scope.subCaption = "<b>Status: </b> Normal";
                iTempEvent = true;
            } else if (event == 'tl') {
                subData = $scope.dashboardView.temp["tl"];
                $scope.caption = "Temperature";
                $scope.subCaption = "<b>Status: </b> Low";
                iTempEvent = true;
            } else if (event == 'th') {
                subData = $scope.dashboardView.temp["th"];
                $scope.caption = "Temperature";
                $scope.subCaption = "<b>Status: </b> High";
                iTempEvent = true;
            } else if (event == 'tu') {
                subData = $scope.dashboardView.temp["tu"];
                $scope.caption = "Temperature";
                $scope.subCaption = "<b>Status: </b> Unknown";
                iTempEvent = true;
            }
            var allSubData = iEntEvent ? $scope.dashboardView.ent["i"] : (iTempEvent? subData : $scope.dashboardView.inv["n"]);
            var fPeriod;
            if(!iTempEvent) {
                fPeriod = $scope.period == "0" ? (iEntEvent ? $scope.aper : "0") : $scope.period;
            } else {
                fPeriod = $scope.tperiod;
            }
            if (checkNotNullEmpty($scope.exFilter) && !iTempEvent) {
                if ($scope.exType == "mTag") {
                    $scope.subCaption += ($scope.subCaption == '' ? '' : ', ') + "<b>Material tag(s):</b> " + $scope.exFilterText;
                } else if ($scope.exType == "mId") {
                    $scope.subCaption += ($scope.subCaption == '' ? '' : ', ') + "<b>Material:</b> " + $scope.exFilterText;
                }
            }
            if (checkNotNullEmpty(fPeriod)) {
                if(iTempEvent) {
                    if (fPeriod != 'M_0') {
                        var fp = fPeriod.substr(2);
                        $scope.subCaption += ($scope.subCaption == '' ? '' : ', ') + "<b>Period: </b>" + (fPeriod.indexOf('M') == 0 ? fp + " Minutes(s)" : (fPeriod.indexOf('H') == 0 ? fp + " Hour(s)" : fp + " Day(s)"));
                    }
                } else {
                    $scope.subCaption += ($scope.subCaption == '' ? '' : ', ') + "<b>Period:</b> " + fPeriod + " day(s)";
                }
            }
            if (checkNotNullEmpty($scope.eTag)) {
                $scope.subCaption += ($scope.subCaption == '' ? '' : ', ') + "<b>" + $scope.resourceBundle.kiosk + " tag(s): </b>" + $scope.eTagText;
            }
            if (checkNotNullEmpty($scope.asset) && iTempEvent) {
                $scope.subCaption += ($scope.subCaption == '' ? '' : ', ') + "<b>Asset Type:</b> ";
                var first = true;
                $scope.asset.forEach(function(data) {
                    if(!first) {
                        $scope.subCaption += ", " + data.text;
                    } else {
                        $scope.subCaption += data.text;
                        first = false;
                    }
                });
            }
            $scope.subCaption = $sce.trustAsHtml($scope.subCaption);
            var addLink = false;
            if ($scope.showSwitch) {
                var mData = [];
                var level = undefined;
                if ($scope.dashboardView.mLev == "country") {
                    level = "state";
                } else if ($scope.dashboardView.mLev == "state") {
                    level = "district";
                }
                for (var n in allSubData) {
                    if (checkNotNullEmpty(n)) {
                        var per = 0;
                        var value = 0;
                        var den = 0;
                        if (subData != undefined && subData[n] != undefined) {
                            per = subData[n].per;
                            value = subData[n].value;
                            den = subData[n].den;
                        }
                        var o = {};
                        var filter;
                        if ($scope.dashboardView.mLev == "country") {
                            if ($scope.locationMapping.data[$scope.dashboardView.mTy] != undefined &&
                                $scope.locationMapping.data[$scope.dashboardView.mTy].states[n] != undefined) {
                                o.id = $scope.locationMapping.data[$scope.dashboardView.mTy].states[n].id;
                            }
                            filter = n;
                        } else if ($scope.dashboardView.mLev == "state") {
                            if (checkNotNullEmpty($scope.locationMapping.data[$scope.dashboardView.mPTy].states[$scope.dashboardView.mTyNm].districts[n])) {
                                if ($scope.locationMapping.data[$scope.dashboardView.mPTy] != undefined &&
                                    $scope.locationMapping.data[$scope.dashboardView.mPTy].states[$scope.dashboardView.mTyNm] != undefined &&
                                    $scope.locationMapping.data[$scope.dashboardView.mPTy].states[$scope.dashboardView.mTyNm].districts[n] != undefined) {
                                    o.id = $scope.locationMapping.data[$scope.dashboardView.mPTy].states[$scope.dashboardView.mTyNm].districts[n].id;
                                }
                                filter = $scope.dashboardView.mTyNm + "_" + n;
                            }
                        }
                        o.label = n;
                        o.value = per;
                        o.displayValue = o.label + "<br/>" + per + "%";
                        if(iEntEvent) {
                            o.toolText = o.label + ": " + value + " of " + den + " " + $scope.resourceBundle['kiosks.lower'];
                        } else if(iTempEvent) {
                            o.toolText = o.label + ": " + value + " of " + den + " " + " assets";
                        } else {
                            o.toolText = o.label + ": " + value + " of " + den + " " + " inventory items";
                        }
                        o.showLabel = "1";
                        if (checkNotNullEmpty(value)) {
                            o.link = "JavaScript: angular.element(document.getElementById('cid')).scope().addFilter('" + filter + "','" + level + "')";
                        }
                        mData.push(o);
                    }
                }
                $scope.mapData = mData;

/*                $scope.markers = {};
                $scope.markers.items = [];
                $scope.markers.items[0] = {};
                $scope.markers.items[0].id = "tt";
                $scope.markers.items[0].shapeid = "triangle";
                $scope.markers.items[0].x = "170";
                $scope.markers.items[0].y = "350";
                $scope.markers.items[0].label = "Test Label";
                $scope.markers.items[0].tooltext = "Test Tool Text";
                $scope.markers.items[0].labelpos = "right";
                $scope.markers.items[1] = {};
                $scope.markers.items[1].id = "tt1";
                $scope.markers.items[1].shapeid = "circle";
                $scope.markers.items[1].x = "220";
                $scope.markers.items[1].y = "280";
                $scope.markers.items[1].label = "Test Label";
                $scope.markers.items[1].tooltext = "Test Tool Text";
                $scope.markers.items[1].labelpos = "right";*/

                setMapRange(event);
                addLink = true;
            }
            var ei = invPieOrder.indexOf($scope.mapEvent);
            if (ei != -1 && (iEntEvent || iTempEvent)) {
                if (FusionCharts("id-pie1")) {
                    FusionCharts("id-pie1").slicePlotItem(ei, false);
                }
            }
            ei = tempPieOrder.indexOf($scope.mapEvent);
            if (ei != -1 && !iTempEvent) {
                if (FusionCharts("id-pie2")) {
                    FusionCharts("id-pie2").slicePlotItem(ei, false);
                }
            }
            $scope.mapEvent = event;
            constructBarData(subData, allSubData, event, addLink, level);
            if(!iEntEvent && !iTempEvent && subData != undefined) {
                constructMatBarData(subData['MAT_BD'].materials, allSubData['MAT_BD'].materials,event);
            } else {
                $scope.matBarData = undefined;
            }
            if (!init) {
                $timeout(function () {
                    $scope.mloading = false;
                }, 190);
                $scope.$digest();
            } else {
                $timeout(function () {
                    $scope.mloading = false;
                }, 1250);
            }
        };

        function addExtraFilter(value, text, type) {
            if (value != undefined) {
                $scope.exFilter = constructModel(value);
                $scope.exFilterText = constructModel(text).replace(/'/g,'');
                $scope.exType = type;
            } else if (value == undefined) {
                $scope.exFilter = $scope.exType = undefined;
            }
        }

        function initWatches() {
            $scope.$watch('mtag', function (newVal, oldVal) {
                if (oldVal != newVal) {
                    if (newVal instanceof Object) {
                        $scope.material = "";
                        addExtraFilter(newVal, newVal, 'mTag');
                    } else if (newVal == undefined) {
                        addExtraFilter(newVal, undefined, 'mTag');
                    }
                }
            });

            $scope.$watch('material', function (newVal, oldVal) {
                if (oldVal != newVal) {
                    if (newVal instanceof Object) {
                        $scope.mtag = undefined;
                        addExtraFilter(newVal, newVal, 'mId');
                    } else if (newVal == undefined) {
                        addExtraFilter(newVal, undefined, 'mId');
                    }
                }
            });
            $scope.$watch('eTag', function (newVal, oldVal) {
               if(checkNotNullEmpty(newVal)) {
                   $scope.excludeTag = undefined;
               }
            });
            $scope.$watch('excludeTag', function (newVal, oldVal) {
               if(checkNotNullEmpty(newVal)) {
                   $scope.eTag = undefined;
               }
            });
        }

        $scope.applyFilters = function () {
            $scope.toggleFilter('i', false);
            $scope.toggleFilter('a', false);
            $scope.toggleFilter('e', false);
            $scope.setURLParams();
        };

        $scope.addFilter = function(filter, level, index, skipLink) {
            var apply = true;
            if(!skipLink) {
                $scope.links.push({filter: filter, text: undefined, level: level});
            } else if(index != undefined){
                $scope.links.splice(index + 1, $scope.links.length - index);
                apply = false;
            }
            if(apply) {
                $scope.$apply(function(){
                    $scope.setURLParams();
                });
            } else {
                $scope.setURLParams();
            }
        };

        $scope.drillDownInventory = function(search){
            if(checkNullEmpty($scope.date)){
                $window.open("#/inventory/"+search);
            }else{
                $scope.showWarning($scope.resourceBundle['past.date.selected']);
                console.log("message");
            }
        };

        $scope.setURLParams = function() {
            var lastLink = angular.copy($scope.links[$scope.links.length - 1]);
            $location.$$search = {};
            if(lastLink.level == 'state') {
                $location.$$search['st'] = lastLink.filter;
            } else if (lastLink.level == 'district') {
                $location.$$search['st'] = lastLink.filter.substr(0,lastLink.filter.indexOf("_"));
                $location.$$search['dt'] = lastLink.filter.substr(lastLink.filter.indexOf("_") + 1);
            }
            $location.$$search['p'] = $scope.period;
            $location.$$search['tp'] = $scope.tperiod;
            if($scope.exTempState){
                var tempArray = [];
                $scope.exTempState.forEach(function(key){
                    tempArray.push(key.id);
                });
                $location.$$search['ets'] = tempArray.join(',');
            }

            if($scope.date) {
                $location.$$search['d'] = formatDate2Url($scope.date);
            }

            if($scope.mtag && $scope.mtag.length > 0) {
                $location.$$search['mt'] = constructModel($scope.mtag).replace(/'/g,'');
            }

            if($scope.material) {
                $location.$$search['mid'] = $scope.material.id;
            }

            if($scope.eTag && $scope.eTag.length > 0) {
                $location.$$search['et'] = constructModel($scope.eTag).replace(/'/g, '');
            }

            if($scope.excludeTag && $scope.excludeTag.length > 0) {
                $location.$$search['eet'] = constructModel($scope.excludeTag).replace(/'/g, '');
            }

            if($scope.asset && $scope.asset.length > 0) {
                $location.$$search['a'] = constructModel($scope.asset, true).replace(/'/g, '');
            }
            $location.$$compose();
        };

        var renderContext = requestContext.getRenderContext(requestContext.getAction(), ["st", "dt", "p","tp","d","mt","mid","et","eet","a","ets"]);
        $scope.$on("requestContextChanged", function () {
            if (!renderContext.isChangeRelevant()) {
                return;
            }
            $scope.initDefaults();
        });
        $scope.toggleFilter = function(type,force) {
            var show;
            if (force != undefined) {
                if (type == 'i') {
                    $scope.showIFilter = force;
                } else if (type == 'a') {
                    $scope.showAFilter = force;
                } else if( type == 'e') {
                    $scope.showEFilter = force;
                }
                show = force;
            } else {
                if (type == 'i') {
                    $scope.showIFilter = !$scope.showIFilter;
                    show = $scope.showIFilter;
                } else if (type == 'a') {
                    $scope.showAFilter = !$scope.showAFilter;
                    show = $scope.showAFilter;
                } else if (type == 'e') {
                    $scope.showEFilter = !$scope.showEFilter;
                    show = $scope.showEFilter;
                }
            }
            var d = document.getElementById('filter_' + type);
            if (show) {
                d.style.top = '10%';
                d.style.opacity = '100';
                d.style.zIndex = '1';
            } else {
                d.style.top = '0';
                d.style.opacity = '0';
                d.style.zIndex = '-1';
            }
        };
        $scope.setShowMap = function(value) {
            $scope.showMap = value;
        }
    }
]);

domainControllers.controller('InvntryDashboardCtrl', ['$scope', 'invService', 'matService', 'entityService', 'domainCfgService', 'dashboardService', 'requestContext', '$location', 'configService',
    function ($scope, invService, matService, entityService, domainCfgService, dashboardService, requestContext, $location, configService) {
        $scope.dbdata = {};
        $scope.fvmtag = null;
        $scope.filteredMaterials = [];
        $scope.period = "0";
        var loadingCount = 0;
        $scope.updInvType = $scope.invType = {'text': 'Normal', 'id': 'n'};
        $scope.invByMatEmpty = false;
        domainCfgService.getDashboardCfg().then(function (data) {
            if (checkNotNullEmpty(data.data.dmtg)) {
                $scope.fvmtag = [];
                data.data.dmtg.forEach(function (data) {
                    $scope.fvmtag.push({'text': data, 'id': data});
                });
            }
            $scope.hardRefreshDashboard(true, true); 
        });

        var events = ['n', 'oos', 'os', 'us'];

        $scope.init = function () {
            $scope.state = requestContext.getParam("st") || $scope.dstate;
            $scope.isSt = checkNotNullEmpty($scope.state);
            $scope.district = requestContext.getParam("dt") || $scope.ddist;
            $scope.isDt = checkNotNullEmpty($scope.district);
            $scope.iCnt = !$scope.isSt && !$scope.isDt;
            $scope.iState = $scope.isSt && !$scope.isDt;
            $scope.iDis = $scope.isSt && $scope.isDt;
            $scope.period = requestContext.getParam("p") || "0";
            $scope.eTag = requestContext.getParam("eTag") || "";
            if(checkNotNullEmpty($scope.eTag)) {
                $scope.etag = {};
                $scope.etag.id = $scope.etag.text = $scope.eTag;
            }
        };

        LocationController.call(this, $scope, configService);

        $scope.showLoading();
        incrementLoadingCounter();
        matService.getDomainMaterials(null, null, 0, 300).then(function (data) {
            $scope.materials = data.data;
            $scope.getFilteredMaterials();
        }).catch(function error(msg) {
            $scope.showErrorMsg(msg);
            $scope.loading = false;
        }).finally(function () {
            $scope.hideLoading();
            decrementLoadingCounter();
        });
        function decrementLoadingCounter() {
            if (--loadingCount == 0) {
                $scope.iLoading = false;
            }
        }

        function incrementLoadingCounter() {
            loadingCount++;
            $scope.iLoading = true;
        }

        $scope.init();

        var invPieColors, tempPieColors;

        $scope.pieOpts = {
            "showLabels": 0,
            "showPercentValues": 1,
            "showPercentInToolTip": 0,
            "showZeroPies": 1,
            "enableSmartLabels": 1,
            "labelDistance": -10,
            "toolTipSepChar": ": ",
            "theme": "fint",
            "enableRotation": 0,
            "enableMultiSlicing": 0,
            //"startingAngle": 90,
            //"slicingDistance": 10,
            "labelFontSize": 14,
            "useDataPlotColorForLabels": 1,
            "showLegend": 1
        };

        $scope.tPieOpts = {
            "showLabels": 0,
            "showValues":0,
            "showPercentValues": 0,
            "showPercentInToolTip": 0,
            "theme": "fint",
            "enableRotation": 0,
            "enableSlicing": 0,
            "enableSmartLabels": 0,
            "animation": 0,
            "pieRadius": 13,
            "doughnutRadius": 4,
            "showToolTip":0
        };

        function addEventColors(data) {
            if (checkNotNullEmpty(data) && checkNotNullEmpty(data.invByMat)) {
                for (var dimKey in data.invByMat) {
                    var dimData = data.invByMat[dimKey].eve;
                    for (var mKey in dimData) {
                        var mData = dimData[mKey];
                        if (mData.oosp > 0) {
                            mData.cclass = 'alert-danger';
                        } else if (mData.osp > 0) {
                            mData.cclass = 'alert-info';
                        } else if (mData.usp > 0) {
                            mData.cclass = 'alert-warning';
                        }
                    }
                }
            }
            return data;
        }

        function addPieData(data) {
            if (checkNotNullEmpty(data) && checkNotNullEmpty(data.invByMat) && checkNotNullEmpty(invPieColors)) {
                for (var dimKey in data.invByMat) {
                    var dimData = data.invByMat[dimKey].eve;
                    for (var mKey in dimData) {
                        var mData = dimData[mKey];
                        mData.pieData = [{
                            value: mData.n,
                            label: "Normal",
                            color: invPieColors[3],
                            toolText: "$label: $value of $unformattedSum",
                            showValue: mData.n > 0 ? 1 : 0
                        }, {
                            value: mData.oos,
                            label: "Zero stock",
                            color: invPieColors[0],
                            toolText: "$label: $value of $unformattedSum",
                            showValue: mData.oos > 0 ? 1 : 0
                        }, {
                            value: mData.os,
                            label: "Max",
                            color: invPieColors[2],
                            toolText: "$label: $value of $unformattedSum",
                            showValue: mData.os > 0 ? 1 : 0
                        }, {
                            value: mData.us,
                            label: "Min",
                            color: invPieColors[1],
                            toolText: "$label: $value of $unformattedSum",
                            showValue: mData.us > 0 ? 1 : 0
                        }];


                        for (var eve in events) {
                            for (var r = 1; r < $scope.range[events[eve]].length; r++) {
                                if (mData[events[eve] + 'p'] <= $scope.range[events[eve]][r]) {
                                    mData[events[eve] + 'bc'] = $scope.colors[events[eve]][r - 1];
                                    mData[events[eve] + 'c'] = $scope.colors[events[eve] + 'txt'][r - 1];
                                    break;
                                }
                            }
                        }

                    }
                }
            }
            return data;
        }

        function addTempPieData() {
            if (checkNotNullEmpty($scope.dbdata) && checkNotNullEmpty($scope.dbdata.invByMat)) {
                for (var dimKey in $scope.dbdata.invByMat) {
                    var dimData = $scope.dbdata.invByMat[dimKey].assets;
                    $scope.dbdata.invByMat[dimKey].assetPieData = [{
                        value: dimData.tn,
                        toolText: "$label: $value of $unformattedSum",
                        label: "Normal",
                        color: tempPieColors[0],
                        showValue: dimData.tn > 0 ? 1 : 0,
                        link: 'JavaScript:'
                    }, {
                        value: dimData.th,
                        toolText: "$label: $value of $unformattedSum",
                        label: "High",
                        color: tempPieColors[2],
                        showValue: dimData.th > 0 ? 1 : 0,
                        link: 'JavaScript:'
                    }, {
                        value: dimData.tl,
                        toolText: "$label: $value of $unformattedSum",
                        label: "Low",
                        color: tempPieColors[1],
                        showValue: dimData.tl > 0 ? 1 : 0,
                        link: 'JavaScript:'
                    }, {
                        value: dimData.tu,
                        toolText: "$label: $value of $unformattedSum",
                        label: "Unknown",
                        color: tempPieColors[3],
                        showValue: dimData.tu > 0 ? 1 : 0,
                        link: 'JavaScript:'
                    }];
                    $scope.dbdata.invByMat[dimKey].assetPieIconData = [];
                    angular.forEach($scope.dbdata.invByMat[dimKey].assetPieData,function(d) {
                        var dd = angular.copy(d);
                        dd.showValue = 0;
                        $scope.dbdata.invByMat[dimKey].assetPieIconData.push(dd);
                    });
                }
            }
        }

        function fetchDBData(skipCache) {
            dashboardService.getInv($scope.state, $scope.district, $scope.period,$scope.eTag,skipCache).then(function (data) {
                $scope.dbdata = checkNullEmpty($scope.district) ? addPieData(data.data) : addEventColors(data.data);
                $scope.invByMatEmpty = $scope.dbdata ? checkNullEmptyObject($scope.dbdata.invByMat) : true;
                addTempPieData();
            }).catch(function error(msg) {
                $scope.showErrorMsg(msg);
            }).finally(function () {
                $scope.hideLoading();
                decrementLoadingCounter();
            });
        }
        $scope.fetchDashboardData = function (init,skipCache) {
            $scope.showLoading();
            $scope.dbdata = undefined;
            incrementLoadingCounter();
            $('#runGrid').scrollLeft(0);
            if (init) {
                domainCfgService.getSystemDashboardConfig().then(function (data) {
                    var dconfig = angular.fromJson(data.data);
                    $scope.colors = dconfig.tc;
                    $scope.range = dconfig.tr;
                    invPieColors = dconfig.tpc;
                    tempPieColors = dconfig.pie.tc;
                    fetchDBData();
                    initWatches();
                });
            } else {
                fetchDBData(skipCache);
            }
        };
        $scope.hardRefreshDashboard = function (init, useCache) {
            $scope.fetchDashboardData(init, !useCache);
        }



        $scope.getFiltered = function () {
            var list = [];
            if ($scope.inventory != null) {
                for (var item in $scope.inventory) {
                    var inv = $scope.inventory[item];
                    if (inv['f']) {
                        list.push(inv);
                    }
                }
            }
            $scope.filtered = list;
            return list;
        };

        $scope.getFilteredMaterials = function () {
            var list = [];
            if (checkNotNullEmpty($scope.materials) && checkNotNullEmpty($scope.materials.results)) {
                if (checkNullEmpty($scope.fvmtag) || checkNullEmpty(constructModel($scope.fvmtag).replace(/'/g,''))) {
                    list = $scope.materials.results;
                } else {
                    for (var item in $scope.materials.results) {
                        var mat = $scope.materials.results[item];
                        var fnd = false;
                        for (var i in mat.tgs) {
                            for (var j in $scope.fvmtag) {
                                if (mat.tgs[i] == $scope.fvmtag[j].text) {
                                    list.push(mat);
                                    fnd = true;
                                    break;
                                }
                            }
                            if(fnd) {
                                break;
                            }
                        }
                    }
                }
            }
            $scope.filteredMaterials = list;
            $('#runGrid').scrollLeft(0);
            return list;
        };

        function initWatches() {
            $scope.$watch("fvmtag", function (newVal, oldVal) {
                if (newVal != oldVal) {
                    if (newVal instanceof Object || checkNullEmpty(newVal)) {
                        $scope.getFilteredMaterials();
                    }
                }
            });

            $scope.$watch("invType", function (newVal, oldVal) {
                if (newVal != oldVal) {
                    if (newVal instanceof Object || checkNullEmpty(newVal)) {
                        $scope.updInvType = newVal;
                    }
                }
            });

            $scope.$watch('period', function (newVal, oldVal) {
                if (oldVal != newVal && !(checkNullEmpty(oldVal) && newVal == 0 )) {
                    $location.$$search['p'] = newVal;
                    $location.$$compose();
                }
            });
            $scope.$watch('etag', function (newVal, oldVal) {
                if (oldVal != newVal) {
                    if(newVal instanceof Object) {
                        $location.$$search['eTag'] = newVal.id;
                        $location.$$compose();
                    } else if (newVal == undefined) {
                        $location.$$search['eTag'] = "";
                        $location.$$compose();
                    }
                }
            });

            var renderContext = requestContext.getRenderContext(requestContext.getAction(), ["st", "dt", "p","eTag"]);
            $scope.$on("requestContextChanged", function () {
                if (!renderContext.isChangeRelevant()) {
                    return;
                }
                $scope.init();
                $scope.fetchDashboardData();

            });
        }


        $scope.isEmptyMatEntFilters = function () {
            return !(checkNotNullEmpty($scope.entityId) || checkNotNullEmpty($scope.mid));
        };

        $scope.togglePie = function (item) {
            return !item;
        };


        $scope.showDetail = function () {
            if (checkNotNull($scope.modalInstance)) {
                $scope.modalInstance.dismiss();
            }
            $scope.modalInstance = $uibModal.open({
                template: batchTemplate,
                scope: $scope
            });
        };

        var rData = {
            results: [{'text': 'Normal', 'id': 'n'},
                {'text': 'Zero stock', 'id': 'oos'},
                {'text': 'Min', 'id': 'us'},
                {'text': 'Max', 'id': 'os'}]
        };

        $scope.getTypes = function (query) {
            query.callback(rData);
        }

    }
]);

domainControllers.controller('SessionDashboardCtrl', ['$scope', '$timeout', '$sce', 'dashboardService', 'INVENTORY', 'configService', 'domainCfgService',
    function ($scope, $timeout, $sce, dashboardService, INVENTORY, configService, domainCfgService) {

        $scope.links = [];
        $scope.period = "0";

        $scope.loading = true;
        $scope.showLoading();
        domainCfgService.getMapLocationMapping().then(function (data) {
            if (checkNotNullEmpty(data.data)) {
                $scope.locationMapping = angular.fromJson(data.data);
                domainCfgService.getDashboardCfg().then(function (data) {
                    if (checkNotNullEmpty(data.data.dmtg)) {
                        $scope.mtag = [];
                        data.data.dmtg.forEach(function(data){
                            $scope.mtag.push({'text': data, 'id': data});
                        });
                        $scope.exFilter = constructModel($scope.mtag);
                        $scope.exFilterText = constructModel($scope.mtag).replace(/'/g,'');
                        $scope.exType = 'mTag';
                    }
                    if (checkNotNullEmpty(data.data.dtt)) {
                        $scope.type = data.data.dtt;
                    }
                    if (checkNotNullEmpty(data.data.detg)) {
                        $scope.etag = {'text': data.data.detg, 'id': data.data.detg};
                        $scope.eTag = data.data.detg;
                        $scope.eTagText = data.data.detg;
                    }
                    $scope.getSessionData();
                    initWatches();
                });
            }
        }).catch(function error(msg) {
        }).finally(function () {
            $scope.loading = false;
            $scope.hideLoading();
        });

        $scope.today = new Date();
        $scope.getSessionData = function (filter, level, index, skipLink, skipCache) {
            if (checkNotNullEmpty($scope.dcntry)) {
                $scope.loading = true;
                $scope.showLoading();
                if(checkNullEmpty($scope.date)) {
                    $scope.date = new Date();
                }
                var date = formatDate($scope.date);
                dashboardService.getSessionData(filter, level, $scope.exFilter, $scope.exType, $scope.period,$scope.eTag, date, $scope.type, skipCache).then(function (data) {
                    $scope.dashboardView = data.data;
                    $scope.dashboardView.sDomain = {};
                    $scope.dashboardView.sData = {};
                    for(var c in $scope.dashboardView.domain) {
                        $scope.dashboardView.domain[c].date = c;
                        $scope.dashboardView.sDomain[FormatDate_YYYY_MM_DD(string2Date(c,"dd-MMM-yyyy","-"))] = $scope.dashboardView.domain[c];
                    }
                    for(c in $scope.dashboardView.data) {
                        var d={};
                        for(var ic in $scope.dashboardView.data[c]) {
                            d[FormatDate_YYYY_MM_DD(string2Date(ic,"dd-MMM-yyyy","-"))] = $scope.dashboardView.data[c][ic];
                        }
                        $scope.dashboardView.sData[c] = d;
                    }
                    var linkText;
                    $scope.iCnt = $scope.iState = $scope.iDist = undefined;
                    if ($scope.dashboardView.mLev == "country") {
                        linkText = $scope.locationMapping.data[$scope.dashboardView.mTy].name;
                        $scope.iCnt = true;
                        $scope.dataLevel = "State";
                    } else if ($scope.dashboardView.mLev == "state") {
                        linkText = $scope.dashboardView.mTyNm;
                        $scope.iState = true;
                        $scope.dataLevel = "District";
                        filter = linkText;
                        level = $scope.dashboardView.mLev;
                    } else {
                        $scope.iDist = true;
                        $scope.dataLevel = $scope.resourceBundle['kiosk'];
                        linkText = $scope.dashboardView.mTyNm.substr($scope.dashboardView.mTyNm.lastIndexOf("_") + 1);
                    }
                    if (!skipLink) {
                        if (index == undefined) {
                            $scope.links.push({filter: filter, text: linkText, level: level})
                        } else {
                            $scope.links.splice(index + 1, $scope.links.length - index);
                        }
                    }
                }).catch(function error(msg) {
                    $scope.showErrorMsg(msg);
                }).finally(function () {
                    $scope.loading = false;
                    $scope.hideLoading();
                });
            }
        };

        function getDataInCSVFormat(data, total) {
            if (checkNotNullEmpty(data)) {
                var comma = ",";
                var nl = "\r\n";
                var dataLevel = (Object.keys(data));
                var csvData = $scope.dataLevel;
                var timeLines = ["1d", "2d", ">2d"];
                var dates = (Object.keys(data[dataLevel[0]]));
                for (var j = 0; j < dates.length; j++) {
                    for (var l = 0; l < 3; l++) {
                        csvData += comma + dates[j] + " - " + timeLines[l] + "(%)";
                    }
                }
                csvData = csvData.substr(0, csvData.length - 1);
                csvData += nl;
                for (var i = 0; i < dataLevel.length; i++) {
                    var currLevel = dataLevel[i];
                    csvData += currLevel || 'No District';
                    var levelObj = data[currLevel];
                    for (j = 0; j < dates.length; j++) {
                        var dateObj = levelObj[dates[j]];
                        csvData += comma + Math.round(dateObj[0].per);
                        csvData += comma + Math.round(dateObj[1].per);
                        csvData += comma + Math.round(dateObj[2].per);
                    }
                    csvData += nl;
                }

                if(checkNotNullEmpty(total)) {
                   var totalDates = (Object.keys(total));
                    csvData += "Total(%)";
                    for(var i=0;i<totalDates.length;i++) {
                        var currentTotal = total[totalDates[i]];
                        for(var j=0; j<3;j++) {
                            csvData += comma + Math.round(currentTotal[j].per);
                        }
                    }
                }
                return csvData;
            }

        }

        $scope.exportAsCSV = function() {
            if(checkNotNullEmpty($scope.dashboardView.sData)) {
                var data = $scope.dashboardView.sData;
                var total = $scope.dashboardView.sDomain;
                var csvData = getDataInCSVFormat(data, total);
                var fileName = "Actual_Transaction_Date";
                exportCSV(csvData, fileName, $timeout);
            }
        };

        function addExtraFilter(value, text, type) {
            if (value != undefined) {
                if(type == 'mId') {
                    $scope.exFilter = value;
                    $scope.exFilterText = text;
                } else {
                    $scope.exFilter = constructModel(value);
                    $scope.exFilterText = constructModel(text).replace(/'/g, '');
                }
                $scope.exType = type;
                $scope.applyFilters();
            } else if (value == undefined) {
                $scope.exFilter = $scope.exType = undefined;
                $scope.applyFilters();
            }
        }

        function initWatches() {
            $scope.$watch('mtag', function (newVal, oldVal) {
                if (oldVal !== newVal) {
                    if (newVal instanceof Object) {
                        $scope.material = "";
                        addExtraFilter(newVal, newVal, 'mTag');
                    } else if (newVal == undefined) {
                        addExtraFilter(newVal, undefined, 'mTag');
                    }
                }
            });

            $scope.$watch('material', function (newVal, oldVal) {
                if (oldVal != newVal) {
                    if (newVal instanceof Object) {
                        $scope.mtag = undefined;
                        addExtraFilter(newVal.id, newVal.text, 'mId');
                    } else if (newVal == undefined) {
                        addExtraFilter(newVal, undefined, 'mId');
                    }
                }
            });

            $scope.$watch('etag', function (newVal, oldVal) {
                if (oldVal != newVal) {
                    if (newVal instanceof Object) {
                        $scope.eTag = newVal.id;
                        $scope.eTagText = newVal.text;
                        $scope.applyFilters();
                    } else if (newVal == undefined) {
                        $scope.eTag = $scope.eTagText = undefined;
                        $scope.applyFilters();
                    }
                }
            });
            $scope.$watch('date', function (newVal, oldVal) {
                if (oldVal != newVal) {
                    if (newVal instanceof Object || newVal == undefined) {
                        $scope.applyFilters();
                    }
                }
            });
            $scope.$watch('type', function (newVal, oldVal) {
                if (oldVal != newVal) {
                    $scope.applyFilters();
                }
            });
        }

        $scope.hardRefreshDashboard = function () {
            var lastIndex = $scope.links.length - 1;
            $scope.getSessionData($scope.links[lastIndex].filter, $scope.links[lastIndex].level, undefined, true, true);
        };

        $scope.applyFilters = function () {
            var lastIndex = $scope.links.length - 1;
            $scope.getSessionData($scope.links[lastIndex].filter, $scope.links[lastIndex].level, undefined, true);
        };

        $scope.getSessionSubData = function(filter) {
            var level = undefined;
            if ($scope.dashboardView.mLev == "country") {
                level = "state";
            } else if ($scope.dashboardView.mLev == "state") {
                level = "district";
                var lastIndex = $scope.links.length - 1;
                filter = $scope.links[lastIndex].filter +"_"+filter;
            }
            $scope.getSessionData(filter, level);
        }
    }
]);

domainControllers.controller('PredictiveController', ['$scope', '$timeout', '$sce', 'dashboardService', 'INVENTORY', 'configService', 'domainCfgService','requestContext','matService','$location',
    function ($scope, $timeout, $sce, dashboardService, INVENTORY, configService, domainCfgService, requestContext, matService,$location) {

        var mapColors, mapRange, soPieColors, soPieOrder;
        $scope.mc = $scope.mr = undefined;

        function constructPie(data, color, order, label) {
            var d = [];
            var isOneDataAvailable = false;
            for (var or in order) {
                var dd = order[or];
                var o = {};
                 if (dd == "n") {
                    o.label = "Normal";
                    o.color = color[0];
                } else if (dd == "so") {
                    o.label = "Stock out";
                    o.color = color[1];
                } else {
                    o.label = dd;
                }
                if (dd == $scope.mapEvent) {
                    o.isSliced = 1;
                }
                o.value = data[dd] || 0;
                o.toolText = "$label: $value of $unformattedSum" + " " + label;
                if (data[dd]) {
                    isOneDataAvailable = true;
                }
                o.showValue = o.value > 0 ? 1 : 0;
                d.push(o);
            }
            return isOneDataAvailable ? d : [];
        }

        function constructPieData(data) {
            $scope.soPieData = constructPie(data.invDomain, soPieColors, soPieOrder, "inventory items");
        }

        $scope.loading = true;
        $scope.mloading = true;
        $scope.links = [];
        $scope.today = new Date();

        $scope.getDashboardData = function (filter, level, skipCache) {
            if (checkNotNullEmpty($scope.dcntry)) {
                $scope.loading = true;
                $scope.mloading = true;
                $scope.showLoading();
                if(checkNotNullEmpty($scope.excludeTag)) {
                    $scope.excludeETag = constructModel($scope.excludeTag);
                    $scope.eeTagObj = angular.copy($scope.excludeTag);
                } else {
                    $scope.excludeETag = $scope.eeTagObj = undefined;
                }
                if(checkNotNullEmpty($scope.eTag)) {
                    $scope.includeETag = constructModel($scope.eTag);
                    $scope.eTagObj = angular.copy($scope.eTag);
                    $scope.eTagText = $scope.includeETag.replace(/'/g, '');
                } else {
                    $scope.includeETag = $scope.eTagObj = $scope.eTagText = undefined;
                }

                dashboardService.getPredictive(filter, level, $scope.exFilter, $scope.exType, $scope.includeETag, $scope.excludeETag, skipCache).then(function (data) {
                    if (typeof loadDashboardFusionMaps === "function") {
                        $scope.showMap = true;
                        $scope.showSwitch = true;
                    } else {
                        $scope.showMap = false;
                        $scope.showSwitch = false;
                    }
                    $scope.dashboardView = data.data;
                    var linkText;
                    if ($scope.dashboardView.mLev == "country") {
                        linkText = $scope.locationMapping.data[$scope.dashboardView.mTy].name;
                        $scope.mapType = "maps/" + linkText;
                    } else if ($scope.dashboardView.mLev == "state") {
                        linkText = $scope.dashboardView.mTyNm;
                        $scope.mapType = "maps/" + $scope.dashboardView.mTy;
                    } else {
                        linkText = $scope.dashboardView.mTyNm.substr($scope.dashboardView.mTyNm.lastIndexOf("_") + 1);
                        $scope.showMap = false;
                        $scope.showSwitch = false;
                        $scope.mapData = [];
                    }
                    $scope.links.push({filter: filter, text: linkText, level: level});
                    constructPieData($scope.dashboardView);
                    $scope.constructMapData($scope.mapEvent, true);

                }).catch(function error(msg) {
                    $scope.showErrorMsg(msg);
                }).finally(function () {
                    $scope.hideLoading();
                    $scope.loading = false;
                });
            }
        };

        $scope.initDefaults = function(skipCache) {
            var filter = undefined;
            var level = undefined;
            var valid = true;
            var mtReq = false; // Material tag is mandatory when any 1 filter is applied via URL
            var state = requestContext.getParam("st");
            $scope.links=[];
            if (requestContext.getParam("dt") && state) {
                if(($scope.dstate && state != $scope.dstate) ||
                    ($scope.ddist && state != $scope.ddist)) {
                    valid = false;
                }
                if(valid) {
                    filter = state + "_" + requestContext.getParam("dt");
                    level = 'district';
                    if(!$scope.dstate) {
                        $scope.links.push({filter: undefined, text: $scope.locationMapping.data[$scope.dcntry].name, level: undefined})
                    }
                    if(!$scope.ddist) {
                        $scope.links.push({filter: $scope.dstate || state, text: $scope.dstate || state, level: 'state'})
                    }
                    mtReq = true;
                }
            } else if (state) {
                if($scope.dstate && filter != $scope.dstate) {
                    valid = false;
                }
                if(valid) {
                    filter = state;
                    level = 'state';
                    $scope.links.push({filter: undefined, text: $scope.locationMapping.data[$scope.dcntry].name, level: undefined})
                    mtReq = true;
                }
            }

            if (requestContext.getParam("et")) {
                var eTag = requestContext.getParam("et").split(",");
                $scope.eTag = [];
                eTag.forEach(function (data) {
                    $scope.eTag.push({'text': data, 'id': data});
                });
                $scope.includeETag = constructModel($scope.eTag);
                mtReq = true;
            } else if (requestContext.getParam("eet")) {
                var eeTag = requestContext.getParam("eet").split(",");
                $scope.excludeTag = [];
                eeTag.forEach(function (data) {
                    $scope.excludeTag.push({'text': data, 'id': data});
                });
                $scope.excludeETag = constructModel($scope.excludeTag);
                mtReq = true;
            } else {
                $scope.eTag = undefined;
            }

            var loadDash = true;
            // WARNING: make sure this is checked at last
            if (requestContext.getParam("mt")) {
                var mTag = requestContext.getParam("mt").split(",");
                $scope.mtag = [];
                mTag.forEach(function(data){
                    $scope.mtag.push({'text': data, 'id': data});
                });
                $scope.exFilter = constructModel($scope.mtag);
                $scope.exFilterText = requestContext.getParam("mt");
                $scope.exType = 'mTag';
            } else if (requestContext.getParam("mid")) {
                var mid = requestContext.getParam("mid");
                loadDash = false;
                $scope.showLoading();
                matService.get(mid).then(function (data) {
                    $scope.material = {'text': data.data.mnm, 'id': data.data.mId};
                    $scope.exFilterText = data.data.mnm;
                    $scope.getDashboardData(filter, level, skipCache);
                }).catch(function error(msg) {
                }).finally(function () {
                    $scope.hideLoading();
                });
                $scope.exFilter = mid;
                $scope.exType = 'mId';
            } else if(mtReq || $scope.callURL || requestContext.getParam("nmt")) {
                $scope.mtag = $scope.exFilter = $scope.exFilterText = $scope.exType = undefined;
            } else {
                $scope.mtag = $scope.dfmt;
            }

            if(loadDash) {
                $scope.getDashboardData(filter, level, skipCache);
            }
        };

        $scope.hardRefreshDashboard = function(){
            $scope.initDefaults(true);
        }

        $scope.showLoading();
        domainCfgService.getMapLocationMapping().then(function (data) {
            if (checkNotNullEmpty(data.data)) {
                $scope.locationMapping = angular.fromJson(data.data);
                domainCfgService.getDashboardCfg().then(function (data) {
                    if (checkNotNullEmpty(data.data.dmtg)) {
                        if(checkNotNullEmpty(data.data.dmtg)) {
                            $scope.mtag = [];
                            data.data.dmtg.forEach(function (data) {
                                $scope.mtag.push({'text': data, 'id': data});
                            });
                        }
                        $scope.dfmt = angular.copy($scope.mtag);
                        $scope.exFilter = constructModel($scope.mtag);
                        $scope.exFilterText = data.data.dmtg;
                        $scope.exType = 'mTag';
                    }
                    if(checkNotNullEmpty(data.data.exet)) {
                        $scope.excludeTag = [];
                        data.data.exet.forEach(function (data) {
                            $scope.excludeTag.push({'text': data, 'id': data});
                        });
                        $scope.excludeETag = constructModel($scope.excludeTag);
                    }
                    domainCfgService.getSystemDashboardConfig().then(function (data) {
                        var dconfig = angular.fromJson(data.data);
                        mapColors = dconfig.mc;
                        $scope.mc = mapColors;
                        mapRange = dconfig.mr;
                        $scope.mr = mapRange;
                        soPieOrder = dconfig.pie.so;
                        soPieColors = dconfig.pie.sc;
                        $scope.mapEvent = soPieOrder[1];
                        $scope.initDefaults();
                        initWatches();
                    });
                });
            } else {
                $scope.loading = false;
                $scope.mloading = false;
            }
        }).catch(function error(msg) {
        }).finally(function () {
            $scope.hideLoading();
        });

        $scope.predictiveOpt = {
            "showLabels": 0,
            "showLegend": 0,
            "showPercentValues": 1,
            "showPercentInToolTip": 0,
            "enableSmartLabels": 1,
            "toolTipSepChar": ": ",
            "theme": "fint",
            "enableRotation": 0,
            "enableMultiSlicing": 0,
            //"startingAngle": 90,
            "labelFontSize": 12,
            "slicingDistance": 15,
            "useDataPlotColorForLabels": 1,
            "subCaptionFontSize": 10,
            "interactiveLegend": 0,
            "exportEnabled": 1,
            "enableSlicing": 0
        };
        $scope.predictiveCaption = "Stock-out for next week";

        $scope.mapOpt = {
            "nullEntityColor": "#cccccc",
            "nullEntityAlpha": "50",
            "hoverOnNull": "0",
            "showLabels": "0",
            "showCanvasBorder": "0",
            "useSNameInLabels": "0",
            "toolTipSepChar": ": ",
            "legendPosition": "BOTTOM",
            "borderColor": "FFFFFF",
            // "entityBorderHoverThickness": "2",
            "interactiveLegend": 1,
            "exportEnabled": 1
        };

        function setMapRange(event) {
            event = event == 'so' ? '200' : event;
            $scope.mapRange = {color: []};
            for (var i = 1; i < mapRange[event].length; i++) {
                var o = {};
                o.minvalue = mapRange[event][i - 1];
                o.maxvalue = mapRange[event][i];
                o.code = mapColors[event][i - 1];
                if (i == 1) {
                    o.displayValue = "<" + mapRange[event][1] + "%"
                } else if (i == mapRange[event].length - 1) {
                    o.displayValue = ">" + mapRange[event][i - 1] + "%"
                } else {
                    o.displayValue = mapRange[event][i - 1] + "-" + mapRange[event][i] + "%"
                }
                $scope.mapRange.color.push(o);
            }
        }

        $scope.barOpt = {
            "showAxisLines": "0",
            "valueFontColor":"#000000",
            "theme": "fint",
            "yAxisName": "Percentage",
            "yAxisNameFontSize": 12,
            "exportEnabled": 1,
            "caption": ' ',
            "yAxisMaxValue": 100
        };
        function constructBarData(data, allData, event, addLink, level) {
            var mapEvent = event == 'so' ? '200' : event;
            var bData = [];
            for (var f in allData) {
                if (checkNotNullEmpty(f) && f != "MAT_BD") {
                    bData.push({"label": f});
                }
            }
            for (var n in allData) {
                if (checkNotNullEmpty(n) && n != "MAT_BD") {
                    var per = 0;
                    var value = 0;
                    var den = 0;
                    var kid = undefined;
                    if (data != undefined && data[n] != undefined) {
                        per = data[n].per;
                        value = data[n].value;
                        den = data[n].den;
                        kid = data[n].kid;
                    }
                    for (var i = 0; i < bData.length; i++) {
                        var bd = bData[i];
                        if (n == bd.label) {
                            bd.value = per;
                            bd.displayValue = bd.value + "%";
                            if(event == 'so' ||  event == 'n') {
                                bd.toolText = value + " of " + den + (level == undefined ? " materials" : " inventory items");
                            }
                            for (var r = 1; r < mapRange[mapEvent].length; r++) {
                                if (per <= mapRange[mapEvent][r]) {
                                    bd.color = mapColors[mapEvent][r - 1];
                                    break;
                                }
                            }
                            if (addLink) {
                                var filter = bd.label;
                                if ($scope.dashboardView.mLev == "state") {
                                    filter = $scope.dashboardView.mTyNm + "_" + bd.label;
                                }
                                bd.link = "JavaScript: angular.element(document.getElementById('cid')).scope().addFilter('" + filter + "','" + level + "')";
                            } else if(event == 'so' || event == 'n') {
                                var search = {eid: kid};
                                if(checkNotNullEmpty($scope.mtag) && $scope.mtag.length == 1) {
                                    search['mtag'] = $scope.mtag[0].text;
                                }
                            }
                            var found = false;
                            //Arrange data in descending order
                            for (var j = 0; j < bData.length; j++) {
                                if (checkNullEmpty(bData[j].value) || bd.value > bData[j].value) {
                                    bData.splice(j, 0, bd);
                                    found = true;
                                    break;
                                }
                            }
                            if (found) {
                                bData.splice(i + 1, 1);
                            }
                            break;
                        }
                    }
                }
            }
            $scope.barHeight = bData.length * 20 + 80;
            $scope.barData = bData;
        }
        function constructMatBarData(data, allData, event) {
            var mapEvent = event == 'so' ? '200' : event;
            var bData = [];
            for (var f in allData) {
                if (checkNotNullEmpty(f)) {
                    bData.push({"label": f});
                }
            }
            for (var n in allData) {
                if (checkNotNullEmpty(n)) {
                    var value = 0;
                    var den = 0;
                    if (data != undefined && data[n] != undefined) {
                        value = data[n].value;
                        den = data[n].den;
                    }
                    for (var i = 0; i < bData.length; i++) {
                        var bd = bData[i];
                        if (n == bd.label) {
                            bd.value = Math.round(value / den  * 1000) / 10;
                            bd.displayValue = bd.value + "%";
                            bd.toolText = value + " of " + den + " " + $scope.resourceBundle['kiosks.lower'];
                            for (var r = 1; r < mapRange[mapEvent].length; r++) {
                                if (bd.value <= mapRange[mapEvent][r]) {
                                    bd.color = mapColors[mapEvent][r - 1];
                                    break;
                                }
                            }
                            if(event == 'so' && checkNotNullEmpty(data[n].mid) && data[n].value != 0){
                                bd.link = "N-#/inventory/?mid="+data[n].mid+"&pdos=7";
                                bd.link += getLocationParams($scope.links);
                            }
                            var found = false;
                            //Arrange data in descending order
                            for (var j = 0; j < bData.length; j++) {
                                if (checkNullEmpty(bData[j].value) || bd.value > bData[j].value) {
                                    bData.splice(j, 0, bd);
                                    found = true;
                                    break;
                                }
                            }
                            if (found) {
                                bData.splice(i + 1, 1);
                            }
                            break;
                        }
                    }
                }
            }
            $scope.matBarHeight = bData.length * 20 + 80;
            $scope.matBarData = bData;

            function getLocationParams(links) {
                var params = '';
                if (checkNotNullEmpty(links) && links.length > 0) {
                    for (var l = 0; l < links.length; l++) {
                        if (links[l].level == 'state' && checkNotNullEmpty(links[l].text)) {
                            params += '&state=' + links[l].text;
                        } else if (links[l].level == 'district' && checkNotNullEmpty(links[l].text)) {
                            params += '&district=' + links[l].text;
                        }
                    }
                }
                return params;
            }
        }

        $scope.constructMapData = function (event, init) {
            if ($scope.mapEvent == event && !init) {
                return;
            }
            $scope.mloading = true;
            var subData;
            $scope.caption = '';
            $scope.subCaption = '';
            if (event == 'so') {
                subData = $scope.dashboardView.inv["so"];
                $scope.caption = "Likely to stock Out";
            } else if (event == 'n') {
                subData = $scope.dashboardView.inv["n"];
                $scope.caption = "No Stock out";
            }
            var allSubData = $scope.dashboardView.inv["n"];

            if (checkNotNullEmpty($scope.exFilter)) {
                if ($scope.exType == "mTag") {
                    $scope.subCaption += ($scope.subCaption == '' ? '' : ', ') + "<b>Material tag(s):</b> " + $scope.exFilterText;
                } else if ($scope.exType == "mId") {
                    $scope.subCaption += ($scope.subCaption == '' ? '' : ', ') + "<b>Material:</b> " + $scope.exFilterText;
                }
            }

            if (checkNotNullEmpty($scope.eTag)) {
                $scope.subCaption += ($scope.subCaption == '' ? '' : ', ') + "<b>" + $scope.resourceBundle.kiosk + " tag(s): </b>" + $scope.eTagText;
            }

            $scope.subCaption = $sce.trustAsHtml("Within the next 7 days. " + $scope.subCaption);
            var addLink = false;
            if ($scope.showSwitch) {
                var mData = [];
                var level = undefined;
                if ($scope.dashboardView.mLev == "country") {
                    level = "state";
                } else if ($scope.dashboardView.mLev == "state") {
                    level = "district";
                }
                for (var n in allSubData) {
                    if (checkNotNullEmpty(n)) {
                        var per = 0;
                        var value = 0;
                        var den = 0;
                        if (subData != undefined && subData[n] != undefined) {
                            per = subData[n].per;
                            value = subData[n].value;
                            den = subData[n].den;
                        }
                        var o = {};
                        var filter;
                        if ($scope.dashboardView.mLev == "country") {
                            if ($scope.locationMapping.data[$scope.dashboardView.mTy] != undefined &&
                                $scope.locationMapping.data[$scope.dashboardView.mTy].states[n] != undefined) {
                                o.id = $scope.locationMapping.data[$scope.dashboardView.mTy].states[n].id;
                            }
                            filter = n;
                        } else if ($scope.dashboardView.mLev == "state") {
                            if (checkNotNullEmpty($scope.locationMapping.data[$scope.dashboardView.mPTy].states[$scope.dashboardView.mTyNm].districts[n])) {
                                if ($scope.locationMapping.data[$scope.dashboardView.mPTy] != undefined &&
                                    $scope.locationMapping.data[$scope.dashboardView.mPTy].states[$scope.dashboardView.mTyNm] != undefined &&
                                    $scope.locationMapping.data[$scope.dashboardView.mPTy].states[$scope.dashboardView.mTyNm].districts[n] != undefined) {
                                    o.id = $scope.locationMapping.data[$scope.dashboardView.mPTy].states[$scope.dashboardView.mTyNm].districts[n].id;
                                }
                                filter = $scope.dashboardView.mTyNm + "_" + n;
                            }
                        }
                        o.label = n;
                        o.value = per;
                        o.displayValue = o.label + "<br/>" + per + "%";
                        o.toolText = o.label + ": " + value + " of " + den + " " + " inventory items";
                        o.showLabel = "1";
                        if (checkNotNullEmpty(value)) {
                            o.link = "JavaScript: angular.element(document.getElementById('cid')).scope().addFilter('" + filter + "','" + level + "')";
                        }
                        mData.push(o);
                    }
                }
                $scope.mapData = mData;
                setMapRange(event);
                addLink = true;
            }
            $scope.mapEvent = event;
            constructBarData(subData, allSubData, event, addLink, level);
            if(subData != undefined) {
                constructMatBarData(subData['MAT_BD'].materials, allSubData['MAT_BD'].materials,event);
            } else {
                $scope.matBarData = undefined;
            }
            if (!init) {
                $timeout(function () {
                    $scope.mloading = false;
                }, 190);
                $scope.$digest();
            } else {
                $timeout(function () {
                    $scope.mloading = false;
                }, 1250);
            }
        };

        function addExtraFilter(value, text, type) {
            if (value != undefined) {
                $scope.exFilter = constructModel(value);
                $scope.exFilterText = constructModel(text).replace(/'/g,'');
                $scope.exType = type;
            } else if (value == undefined) {
                $scope.exFilter = $scope.exType = undefined;
            }
        }

        function initWatches() {
            $scope.$watch('mtag', function (newVal, oldVal) {
                if (oldVal != newVal) {
                    if (newVal instanceof Object) {
                        $scope.material = "";
                        addExtraFilter(newVal, newVal, 'mTag');
                    } else if (newVal == undefined) {
                        addExtraFilter(newVal, undefined, 'mTag');
                    }
                }
            });

            $scope.$watch('material', function (newVal, oldVal) {
                if (oldVal != newVal) {
                    if (newVal instanceof Object) {
                        $scope.mtag = undefined;
                        addExtraFilter(newVal, newVal, 'mId');
                    } else if (newVal == undefined) {
                        addExtraFilter(newVal, undefined, 'mId');
                    }
                }
            });
            $scope.$watch('eTag', function (newVal, oldVal) {
                if(checkNotNullEmpty(newVal)) {
                    $scope.excludeTag = undefined;
                }
            });
            $scope.$watch('excludeTag', function (newVal, oldVal) {
                if(checkNotNullEmpty(newVal)) {
                    $scope.eTag = undefined;
                }
            });
        }

        $scope.applyFilters = function () {
            $scope.toggleFilter('i', false);
            $scope.setURLParams();
        };

        $scope.addFilter = function(filter, level, index, skipLink) {
            var apply = true;
            if(!skipLink) {
                $scope.links.push({filter: filter, text: undefined, level: level});
            } else if(index != undefined){
                $scope.links.splice(index + 1, $scope.links.length - index);
                apply = false;
            }
            if(apply) {
                $scope.$apply(function(){
                    $scope.setURLParams();
                });
            } else {
                $scope.setURLParams();
            }
        };

        $scope.setURLParams = function() {
            var lastLink = angular.copy($scope.links[$scope.links.length - 1]);
            $location.$$search = {};
            if(lastLink.level == 'state') {
                $location.$$search['st'] = lastLink.filter;
            } else if (lastLink.level == 'district') {
                $location.$$search['st'] = lastLink.filter.substr(0,lastLink.filter.indexOf("_"));
                $location.$$search['dt'] = lastLink.filter.substr(lastLink.filter.indexOf("_") + 1);
            }
            if($scope.mtag && $scope.mtag.length > 0) {
                $location.$$search['mt'] = constructModel($scope.mtag).replace(/'/g, '');
            } else if($scope.material) {
                $location.$$search['mid'] = $scope.material.id;
            }

            if($scope.eTag && $scope.eTag.length > 0) {
                $location.$$search['et'] = constructModel($scope.eTag).replace(/'/g, '');
            }

            if($scope.excludeTag && $scope.excludeTag.length > 0) {
                $location.$$search['eet'] = constructModel($scope.excludeTag).replace(/'/g, '');
            }
            $location.$$compose();
        };

        var renderContext = requestContext.getRenderContext(requestContext.getAction(), ["st", "dt", "mt","mid","et","eet"]);
        $scope.$on("requestContextChanged", function () {
            if (!renderContext.isChangeRelevant()) {
                return;
            }
            $scope.callURL = true;
            $scope.initDefaults();
        });
        $scope.toggleFilter = function(type,force) {
            var show;
            if (force != undefined) {
                if (type == 'i') {
                    $scope.showIFilter = force;
                }
                show = force;
            } else {
                if (type == 'i') {
                    $scope.showIFilter = !$scope.showIFilter;
                    show = $scope.showIFilter;
                }
            }
            var d = document.getElementById('filter_' + type);
            if (show) {
                d.style.top = '10%';
                d.style.opacity = '100';
                d.style.zIndex = '1';
            } else {
                d.style.top = '0';
                d.style.opacity = '0';
                d.style.zIndex = '-1';
            }
        };
        $scope.setShowMap = function(value) {
            $scope.showMap = value;
        }
    }
]);

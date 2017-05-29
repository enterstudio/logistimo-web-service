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

var linkDomainControllers = angular.module('linkedDomainControllers', []);
linkDomainControllers.controller('ChildDomainCtrl', ['$scope', 'linkedDomainService',
    function ($scope, linkedDomainService) {
        $scope.globalHC = false;
        if (checkNotNullEmpty($scope.accChld)) {
            $scope.chld = $scope.accChld;
        } else {
            $scope.chld = [];
        }

        $scope.fetchParents = function(){
            $scope.showLoading();
            linkedDomainService.getLinkedDomains(1).then(function (data) {
                $scope.parents = data.data;
            }).catch(function error(msg){
                $scope.showErrorMsg(msg);
            }).finally(function() {
                $scope.hideLoading();
                $scope.fetchLinkedDomains();
            })
        };
        $scope.fetchLinkedDomains = function () {
            $scope.showLoading();
            $scope.loading = true;
            linkedDomainService.fetchLinkedDomains($scope.did).then(function (data) {
                $scope.chld = data.data;
                if (checkNotNullEmpty($scope.chld)) {
                    $scope.globalHC = $scope.chld.some(function (data) {
                        if (data.hc) {
                            return true;
                        }
                    });
                }
                //$scope.setChild(data.data);
            }).catch(function error(msg) {
                $scope.showErrorMsg(msg);
            }).finally(function () {
                $scope.loading = false;
                $scope.hideLoading();
            });
        };

        if(checkNullEmpty($scope.did) && checkNullEmpty($scope.accChld)) {
            $scope.fetchParents();
        } else if (checkNullEmpty($scope.accChld)) {
            $scope.fetchLinkedDomains();
        } else{
            $scope.accChld = undefined;
        }

        $scope.$on("domainLinksChanged", function () {
            $scope.fetchLinkedDomains();
        });

        $scope.deleteDomainLink = function (domainId) {
            if (domainId != null) {
                if (!confirm($scope.resourceBundle['domain.child.remove'] + '?')) {
                    return;
                }
                linkedDomainService.deleteDomainLink(domainId).then(function (data) {
                    $scope.showSuccess(data.data);
                    $scope.broadcastDomainLinksChange();
                }).catch(function error(msg) {
                    $scope.showWarning($scope.resourceBundle['domain.delete.error'] + msg);
                });
            }
        };
        $scope.$on("accessibleDomainsChanged", function(){
            if($scope.isUserPage) {
                $scope.chld = $scope.accDsm;
            }
        });
    }
]);
linkDomainControllers.controller('DomainFormCtrl', ['$scope',
    function ($scope) {
        $scope.open = false;
        $scope.edit = false;
        $scope.pushConfig = false;
        $scope.chld = [];
        $scope.chldt = [];
        $scope.hasChild = false;
        $scope.hasChildren = false;
        $scope.hasParent = false;
        $scope.broadcastDomainLinksChange = function () {
            $scope.$broadcast("domainLinksChanged");
        };
        $scope.broadcastDomainUpdate=function(){
            $scope.$broadcast("domainUpdated")
        };
        $scope.openForm = function () {
            $scope.open = !$scope.open;
            $scope.edit = false;
            $scope.setNewChild();
            $scope.openDefault=false;
        };
        $scope.openPushConfig = function () {
            $scope.pushConfig = !$scope.pushConfig;
        };
        $scope.setChild = function (data) {
            $scope.chld = data;
        };
        $scope.setNewChild = function () {
            $scope.nchld = {};
            $scope.nchld.cc = true;
            $scope.nchld.cm = true;
            $scope.nchld.uv = true;
            $scope.nchld.ev = true;
            $scope.nchld.egv = true;
            $scope.nchld.erv = true;
            $scope.nchld.iv = true;
            $scope.nchld.ie = true;
            $scope.nchld.cv = true;
            $scope.nchld.ldl = [];
            $scope.nchld.dpl = [];

        };

        $scope.setDomainPerm = function () {
            $scope.dperm = {};
            $scope.dperm.cc = true;
            $scope.dperm.cm = true;
            $scope.dperm.md = true;
            $scope.dperm.mm = false;
            $scope.dperm.mc = true;
            $scope.dperm.mu = true;
            $scope.dperm.me = true;
            $scope.dperm.meg = true;
            $scope.dperm.ldl = [];

        };
        $scope.appendChild = function (data) {
            var sno = 0;
            if ($scope.chld.length == 0) {
                $scope.chld = [];
            } else {
                for (var i = 0; i < $scope.chld.length; i++) {
                    if ($scope.chld[i].type == 0) {
                        sno = ++sno;
                    }
                }
            }

            if (data != null && checkNotNullEmpty(data)) {
                for (i = 0; i < data.length; i++) {
                    if (data[i].type == 0) {
                        data[i].sno = ++sno;
                        $scope.chld.push(data[i]);
                        $scope.hasChild = true;
                    }
                }
            }
        };
        $scope.removeSelectedChild = function () {
            var sno = 1;
            for (var i = 0; i < $scope.chld.length; i++) {
                if ($scope.chld[i].selected && $scope.chld[i].type == 0) {
                    $scope.chld.splice(i--, 1);
                } else if ($scope.chld[i].type != 1) {
                    $scope.chld[i].sno = sno++;
                }
            }

            $scope.hasChild = false;
            $scope.hasParent = false;
            for (i = 0; i < $scope.chld.length; i++) {
                if ($scope.chld[i].type == 0) {
                    $scope.hasChild = true;
                } else if ($scope.chld[i].type == 1) {
                    $scope.hasParent = true;
                }
            }
        };

        $scope.checkChildParentDomains = function () {
            $scope.hasChild = false;
            $scope.hasChildren = false;
            if (checkNotNullEmpty($scope.chld)) {
                var count = 0;
                var sno = 1;
                var pno = 1;
                for (var i = 0; i < $scope.chld.length; i++) {
                    if ($scope.chld[i].type == 0) {
                        $scope.hasChild = true;
                        $scope.chld[i].sno = sno++;
                        count++;
                    } else if ($scope.chld[i].type == 1) {
                        $scope.hasParent = true;
                        $scope.chld[i].pno = pno++;
                    }
                }
                if (count > 1) {
                    $scope.hasChildren = true;
                }
            }
        };

        $scope.setEditDomainLink = function (data,did,dnm) {
            $scope.edit = false;
            if (checkNotNullEmpty(data)) {
                $scope.nchld = data.dp;
            } else {
                $scope.nchld = {};
            }
            $scope.nchld.dId = did;
            $scope.nchld.name = dnm;
            $scope.open = true;
            $scope.edit = true;
        };
    }
]);

linkDomainControllers.controller('DomainListController', ['$scope', 'requestContext', 'linkedDomainService',
    function ($scope, requestContext, linkedDomainService) {
        $scope.metadatac = [{'title': 'Sl.No.', 'field': 'sno'}, {'title': 'Domain Name', 'field': 'text'}];

        $scope.metadatap = [{'title': 'Sl.No.', 'field': 'sno'}, {'title': 'Domain Name', 'field': 'text'},
            {'title': 'Registered On', 'field': 'dct'},
            {'title': 'Switch Domain'}];

        $scope.selectAll = function (newval) {
            for (var item in $scope.chld) {
                $scope.chld[item]['selected'] = newval;
            }
        };
        $scope.fetchLinkedDomains = function () {
            $scope.showLoading();
            $scope.loading = true;
            linkedDomainService.fetchLinkedDomains().then(function (data) {
                $scope.setChild(data.data);
                $scope.checkChildParentDomains();
            }).catch(function error(msg) {
                $scope.showErrorMsg(msg);
            }).finally(function () {
                $scope.loading = false;
                $scope.hideLoading();
            });
        };
        $scope.fetchLinkedDomains();

        $scope.$on("domainLinksChanged", function () {
            $scope.fetchLinkedDomains();
        });
        $scope.editDomainPermission = function (domainId, dName) {
            if (domainId != null && checkNotNullEmpty(domainId)) {
                $scope.showLoading();
                linkedDomainService.getDomainPermission(false,domainId).then(function (data) {
                    $scope.setEditDomainLink(data.data,domainId, dName);
                }).catch(function error(msg) {
                    $scope.showErrorMsg(msg);
                }).finally(function () {
                    $scope.hideLoading();
                })
            }
        };

        $scope.pushConfiguration = function () {
            if (!confirm($scope.resourceBundle['domain.config.push'] + '?')) {
                return;
            }
            $scope.showLoading();
            linkedDomainService.pushConfiguration().then(function (data) {
                $scope.showSuccess(data.data);
                $scope.selectAll(false);
                $scope.selAll = false;
                $scope.openPushConfig();
            }).catch(function error(msg) {
                $scope.showWarning($scope.resourceBundle['domain.delete.error'] + msg);
            }).finally(function () {
                $scope.hideLoading();
            });
        }
    }
]);

linkDomainControllers.controller('AllDomainsListController', ['$scope', 'domainCfgService', 'requestContext', '$location','domainService',
    function ($scope, domainCfgService, requestContext, $location, domainService) {
        $scope.wparams = [["search", "search.nm"], ["o", "offset"], ["s", "size"]];
        $scope.reset = function (isReset) {
            $scope.search = {nm: ""};
            if(!isReset) {
                $scope.search.key = $scope.search.nm = requestContext.getParam("search") || "";
                $scope.offset = requestContext.getParam("o") || 0;
                $scope.size = requestContext.getParam("s") || 50;
            }
        };
        $scope.init = function () {
            $scope.reset();
        };
        $scope.init();
        ListingController.call(this, $scope, requestContext, $location);
        $scope.fetch = function () {
            $scope.loading=true;
            $scope.showLoading();
            domainCfgService.getAllDomain($scope.size, $scope.offset, $scope.search.nm).then(function (data) {
                $scope.domain = data.data.results;
                $scope.setResults(data.data);
            }).catch(function error(msg) {
                $scope.setResults(null);
                $scope.showWarning("Unable to load domains data" + msg);
            }).finally(function () {
                $scope.loading=false;
                $scope.hideLoading();
            });
        };
        $scope.fetch();
        $scope.searchDomain = function () {
            if ($scope.search.nm != $scope.search.key) {
                $scope.search.nm = $scope.search.key;
            }
        };
        $scope.deleteDomain = function (dId) {
            $scope.showLoading();
            domainService.deleteDomain(dId).then(function (data) {
                $scope.showSuccess(data.data);
            }).catch(function error(msg) {
                $scope.showErrorMsg(msg);
            }).finally(function () {
                $scope.hideLoading();
            });
        }


    }
]);

linkDomainControllers.controller('DomainDetailController', ['$scope', 'requestContext', 'linkedDomainService','domainService',
    function ($scope, requestContext, linkedDomainService,domainService) {
        $scope.setDomainPerm();
        $scope.editPerm=false;
        $scope.domainId = requestContext.getParam("domainId");
        $scope.domainbyidDets = {};

        $scope.fetchLinkedDomains = function (){
            $scope.showLoading();
            $scope.loading = true;
            linkedDomainService.fetchLinkedDomains($scope.domainId).then(function (data) {
                $scope.setChild(data.data);
                $scope.checkChildParentDomains();
            }).catch(function error(msg) {
                $scope.showErrorMsg(msg);
            }).finally(function () {
                $scope.loading = false;
                $scope.hideLoading();

            });
        };

        $scope.fetchLinkedDomains();

        $scope.fetchDomainsList = function () {
            $scope.loading = true;
            $scope.showLoading();
            domainService.fetchDomainById($scope.domainId).then(function (data) {
                $scope.domainbyidDets = data.data;
            }).catch(function error(msg) {
                $scope.showErrorMsg(msg);
            }).finally(function () {
                $scope.loading = false;
                $scope.hideLoading();
            });
        };
        $scope.fetchDomainsList();
        $scope.getParentsByDomainId= function(){
            $scope.loading = true;
            $scope.showLoading();
            linkedDomainService.getParentsByDomainId($scope.domainId,1).then(function (data) {
                $scope.domainParents = data.data;
            }).catch(function error(msg){
                $scope.showErrorMsg(msg);
            }).finally(function() {
                $scope.hideLoading();
            });
        };

        $scope.getParentsByDomainId();
        $scope.$on("domainLinksChanged", function () {
            $scope.fetchLinkedDomains();
        });

        $scope.$on("domainUpdated", function () {
            $scope.fetchDomainsList();
            $scope.getParentsByDomainId();
        });

        $scope.setDesc = function () {
            $scope.nedit = false;
            $scope.desc = $scope.domainbyidDets.description;
            $scope.dedit = true;
        };

        $scope.updateDesc = function () {
            if (checkNotNullEmpty($scope.desc)) {
                $scope.domainbyidDets.description = $scope.desc;
            } else {
                $scope.domainbyidDets.description = '';
            }
            $scope.updatingDesc = true;
            domainService.updatedomaininfo($scope.domainId, $scope.domainbyidDets.name,
                $scope.domainbyidDets.description).then(function(){
                    $scope.broadcastDomainUpdate();
                }
            ).catch(function error(msg) {
                    $scope.showErrorMsg(msg);
                }).finally(function () {
                    $scope.updatingDesc = false;
                });
            $scope.broadcastDomainUpdate();
            $scope.dedit = false;
        };
        $scope.cancelDesc = function () {
            $scope.dedit = false;
        };

        $scope.setName = function () {
            $scope.dedit = false;
            $scope.name = $scope.domainbyidDets.name;
            $scope.nedit = true;
        };

        $scope.updateName = function () {
            if (checkNotNullEmpty($scope.name)) {
                $scope.domainbyidDets.name = $scope.name;
            } else {
                $scope.showWarning("Name cannot be blank");
                return;
            }
            if(checkNullEmpty($scope.domainbyidDets.description)){
                $scope.domainbyidDets.description = " ";
            }
            $scope.updatingName = true;
            domainService.updatedomaininfo($scope.domainId, $scope.domainbyidDets.name,
                $scope.domainbyidDets.description).then(function(){
                    $scope.broadcastDomainUpdate();
                }).catch(function error(msg) {
                    $scope.showWarning("Unable to load domains data" + msg);
                }).finally(function () {
                    $scope.updatingName = false;
                });
            $scope.nedit = false;
        };
        $scope.cancelName = function () {
            $scope.nedit = false;
        };

        $scope.editManageDomainPermission = function (domainId) {
            if (domainId != null && checkNotNullEmpty(domainId)) {
                $scope.showLoading();
                linkedDomainService.getDomainPermission(false,domainId).then(function (data) {
                    setManageEditDomainLink(data.data);
                }).catch(function error(msg) {
                    $scope.showErrorMsg(msg);
                }).finally(function () {
                    $scope.hideLoading();
                })

            }
        };

        $scope.editManageDomainPermission($scope.domainId);

        $scope.populateDomainPermission = function () {
            var dp = {};
            dp.uv = $scope.dperm.uv;
            dp.ua = $scope.dperm.ua;
            dp.ue = $scope.dperm.ue;
            dp.ur = $scope.dperm.ur;
            dp.ev = $scope.dperm.ev;
            dp.ea = $scope.dperm.ea;
            dp.ee = $scope.dperm.ee;
            dp.er = $scope.dperm.er;
            dp.iv = $scope.dperm.iv;
            dp.ia = $scope.dperm.ia;
            dp.ie = $scope.dperm.ie;
            dp.ir = $scope.dperm.ir;
            dp.mv = $scope.dperm.mv;
            dp.ma = $scope.dperm.ma;
            dp.me = $scope.dperm.me;
            dp.mr = $scope.dperm.mr;
            dp.egv = $scope.dperm.egv;
            dp.ega = $scope.dperm.ega;
            dp.ege = $scope.dperm.ege;
            dp.egr = $scope.dperm.egr;
            dp.erv = $scope.dperm.erv;
            dp.era = $scope.dperm.era;
            dp.ere = $scope.dperm.ere;
            dp.err = $scope.dperm.err;
            dp.cv = $scope.dperm.cv;
            dp.ce = $scope.dperm.ce;
            dp.ae = $scope.dperm.ae;
            dp.ar = $scope.dperm.ar;
            dp.av = $scope.dperm.av;
            dp.aa = $scope.dperm.aa;
            $scope.dperm.dp = dp;
            $scope.dperm.dId = $scope.domainId;
            $scope.dperm.name = $scope.domainbyidDets.name;
        };

        $scope.checkPermission = function(){
            if(checkNotNullEmpty($scope.dperm)){
                if(!$scope.dperm.uv){
                    $scope.dperm.ua = false;
                    $scope.dperm.ue = false;
                    $scope.dperm.ur = false;
                }

                if(!$scope.dperm.ev){
                    $scope.dperm.ea = false;
                    $scope.dperm.ee = false;
                    $scope.dperm.er = false;
                }

                if(!$scope.dperm.egv){
                    $scope.dperm.ega = false;
                    $scope.dperm.ege = false;
                    $scope.dperm.egr = false;
                }

                if(!$scope.dperm.erv){
                    $scope.dperm.era = false;
                    $scope.dperm.ere = false;
                    $scope.dperm.err = false;
                }

                if(!$scope.dperm.mv){
                    $scope.dperm.ma = false;
                    $scope.dperm.me = false;
                    $scope.dperm.mr = false;
                }

                if(!$scope.dperm.iv){
                    $scope.dperm.ia = false;
                    $scope.dperm.ie = false;
                    $scope.dperm.ir = false;
                }
                if(!$scope.dperm.av){
                    $scope.dperm.aa = false;
                    $scope.dperm.ae = false;
                    $scope.dperm.ar = false;
                }

                if(!$scope.dperm.cv){
                    $scope.dperm.ce = false;
                }
            }
        };

        $scope.updateChild = function () {
            if (checkNullEmpty($scope.dperm)) {
                $scope.showWarning("Please select children to update permission");
                return;
            }
            $scope.showLoading();
            $scope.populateDomainPermission();
            linkedDomainService.updateChildDomainPermissions($scope.dperm).then(function (data) {
                $scope.showSuccess(data.data);
                toggleEdit();
                $scope.masterdPerm = undefined;
            }).catch(function error(msg) {
                $scope.showErrorMsg(msg);
            }).finally(function () {
                $scope.fetchDomainsList();
                $scope.hideLoading();
            })
        };
        //overding the DomainFormCtrl method for manage page
        function setManageEditDomainLink(data) {
            $scope.edit = false;
            if (checkNotNullEmpty(data)) {
                $scope.dperm = data.dp;
            }else{
                $scope.dperm = {};
            }
            $scope.dperm.dId = $scope.domainId;
            $scope.dperm.name = $scope.domainbyidDets.name;
        }
        function toggleEdit(){
            $scope.editPerm=!$scope.editPerm;
        }

        $scope.openEdit= function(){
            if(checkNullEmpty($scope.masterdPerm)){
                $scope.masterdPerm = angular.copy($scope.dperm);
            }
            toggleEdit();
        };
        $scope.cancelEdit = function(){
            $scope.dperm = angular.copy($scope.masterdPerm);
            toggleEdit();
        };

    }
]);

linkDomainControllers.controller('AddChildDomainsController', ['$scope', 'requestContext', 'linkedDomainService',
    function ($scope, requestContext, linkedDomainService) {
        $scope.setNewChild();
        $scope.domId = requestContext.getParam("domainId");
        $scope.populateDomainPermission = function () {
            var dp = {};
            dp.uv = $scope.nchld.uv;
            dp.ua = $scope.nchld.ua;
            dp.ue = $scope.nchld.ue;
            dp.ur = $scope.nchld.ur;
            dp.ev = $scope.nchld.ev;
            dp.ea = $scope.nchld.ea;
            dp.ee = $scope.nchld.ee;
            dp.er = $scope.nchld.er;
            dp.iv = $scope.nchld.iv;
            dp.ia = $scope.nchld.ia;
            dp.ie = $scope.nchld.ie;
            dp.ir = $scope.nchld.ir;
            dp.mv = $scope.nchld.mv;
            dp.ma = $scope.nchld.ma;
            dp.me = $scope.nchld.me;
            dp.mr = $scope.nchld.mr;
            dp.egv = $scope.nchld.egv;
            dp.ega = $scope.nchld.ega;
            dp.ege = $scope.nchld.ege;
            dp.egr = $scope.nchld.egr;
            dp.erv = $scope.nchld.erv;
            dp.era = $scope.nchld.era;
            dp.ere = $scope.nchld.ere;
            dp.err = $scope.nchld.err;
            dp.cv = $scope.nchld.cv;
            dp.ce = $scope.nchld.ce;
            dp.cc = $scope.nchld.cc;
            dp.cm = $scope.nchld.cm;
            dp.av = $scope.nchld.av;
            dp.ae = $scope.nchld.ae;
            dp.aa = $scope.nchld.aa;
            dp.ar = $scope.nchld.ar;
            $scope.nchld.dp = dp;
        };

        $scope.checkPermission = function(){
            if(checkNotNullEmpty($scope.nchld)){
                if(!$scope.nchld.uv){
                    $scope.nchld.ua = false;
                    $scope.nchld.ue = false;
                    $scope.nchld.ur = false;
                }

                if(!$scope.nchld.ev){
                    $scope.nchld.ea = false;
                    $scope.nchld.ee = false;
                    $scope.nchld.er = false;
                }

                if(!$scope.nchld.egv){
                    $scope.nchld.ega = false;
                    $scope.nchld.ege = false;
                    $scope.nchld.egr = false;
                }

                if(!$scope.nchld.erv){
                    $scope.nchld.era = false;
                    $scope.nchld.ere = false;
                    $scope.nchld.err = false;
                }

                if(!$scope.nchld.mv){
                    $scope.nchld.ma = false;
                    $scope.nchld.me = false;
                    $scope.nchld.mr = false;
                }

                if(!$scope.nchld.iv){
                    $scope.nchld.ia = false;
                    $scope.nchld.ie = false;
                    $scope.nchld.ir = false;
                }
                if(!$scope.nchld.av){
                    $scope.nchld.aa = false;
                    $scope.nchld.ae = false;
                    $scope.nchld.ar = false;
                }

                if(!$scope.nchld.cv){
                    $scope.nchld.ce = false;
                }

            }
        };
        $scope.addChildren = function () {
            if (checkNullEmpty($scope.nchld) ||  $scope.nchld.ldl.length == 0) {
                $scope.showWarning("Please select children to add");
                return;
            }
            $scope.populateDomainPermission();
            $scope.showLoading();
            linkedDomainService.addChildren($scope.nchld).then(function (data) {
                $scope.appendChild(data.data);
                $scope.showSuccess(data.data);
                $scope.openForm();
                $scope.broadcastDomainLinksChange();
            }).catch(function error(msg) {
                $scope.showErrorMsg(msg);
            }).finally(function () {
                $scope.hideLoading();
            });
        };

        $scope.addChildrenToDomain = function () {
            if (checkNullEmpty($scope.nchld)  ||  $scope.nchld.ldl.length == 0) {
                $scope.showWarning("Please select children to add");
                return;
            }
            if (checkNullEmpty($scope.domId)) {
                $scope.showWarning("Domain is not proper");
                return;
            }
            $scope.showLoading();
            $scope.populateDomainPermission();
            linkedDomainService.addChildrenTodomain($scope.nchld, $scope.domId).then(function (data) {
                $scope.appendChild(data.data);
                $scope.showSuccess(data.data);
                $scope.openForm();
                $scope.broadcastDomainLinksChange();
            }).catch(function error(msg) {
                $scope.showErrorMsg(msg);
            }).finally(function () {
                $scope.hideLoading();
            });
        };

        $scope.updateChild = function () {
            if (checkNullEmpty($scope.nchld)) {
                $scope.showWarning("Please select children to update permission");
                return;
            }
            $scope.showLoading();
            $scope.populateDomainPermission();
            linkedDomainService.updateChildDomainPermissions($scope.nchld).then(function (data) {
                $scope.showSuccess(data.data);
                $scope.openForm();
            }).catch(function error(msg) {
                $scope.showErrorMsg(msg);
            }).finally(function () {
                $scope.hideLoading();
            })
        };
    }
]);


linkDomainControllers.controller('ManageChildDomainCtrl', ['$scope','linkedDomainService','requestContext',
    function ($scope, linkedDomainService,requestContext) {
        $scope.domainId = requestContext.getParam("domainId");
        $scope.options = false;
        $scope.globalHC = false;
        $scope.chld = [];

        if(angular.isUndefined($scope.did)){
            $scope.did=$scope.domainId;
        }
        $scope.fetchLinkedDomains = function () {
            $scope.showLoading();
            $scope.loading = true;
            linkedDomainService.fetchLinkedDomains($scope.did).then(function (data) {
                $scope.chld = data.data;
                if(checkNotNullEmpty($scope.chld)){
                    $scope.globalHC = $scope.chld.some(function(data){
                        if(data.hc){
                            return true;
                        }
                    });
                }
            }).catch(function error(msg) {
                $scope.showError($scope.resourceBundle['domain.child.fetch.error'] + msg);
            }).finally(function () {
                $scope.loading = false;
                $scope.hideLoading();
            });
        };
        $scope.fetchLinkedDomains();

        $scope.$on("domainLinksChanged", function () {
            $scope.fetchLinkedDomains();
        });

        $scope.deleteDomainLink = function (domainId) {
            if (domainId != null) {
                if (!confirm($scope.resourceBundle['domain.child.remove'] + '?')) {
                    return;
                }
                linkedDomainService.deleteDomainLink(domainId).then(function (data) {
                    $scope.showSuccess(data.data);
                    $scope.broadcastDomainLinksChange();
                }).catch(function error(msg) {
                    $scope.showWarning($scope.resourceBundle['domain.delete.error'] + msg);
                })
            }
        };

        $scope.editDomainPermission = function (domainId,dName) {
            if (domainId != null && checkNotNullEmpty(domainId)) {
                $scope.showLoading();
                linkedDomainService.getDomainPermission(false,domainId).then(function (data) {
                    $scope.setEditDomainLink(data.data,domainId,dName);
                }).catch(function error(msg) {
                    $scope.showErrorMsg(msg);
                }).finally(function () {
                    $scope.hideLoading();
                })

            }
        };

    }
]);

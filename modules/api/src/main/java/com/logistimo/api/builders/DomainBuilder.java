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

package com.logistimo.api.builders;

import com.logistimo.api.models.superdomains.DomainDBModel;
import com.logistimo.api.models.superdomains.DomainModel;
import com.logistimo.api.models.superdomains.DomainPermissionModel;
import com.logistimo.auth.SecurityConstants;
import com.logistimo.config.models.CapabilityConfig;
import com.logistimo.config.models.DomainConfig;
import com.logistimo.dao.JDOUtils;
import com.logistimo.domains.entity.IDomain;
import com.logistimo.domains.entity.IDomainLink;
import com.logistimo.domains.entity.IDomainPermission;
import com.logistimo.domains.service.DomainsService;
import com.logistimo.exception.InvalidServiceException;
import com.logistimo.models.superdomains.DomainSuggestionModel;
import com.logistimo.services.ObjectNotFoundException;
import com.logistimo.services.ServiceException;

import java.util.*;

/**
 * Created by naveensnair on 01/07/15.
 */
public class DomainBuilder {

  public List<DomainSuggestionModel> buildChildSuggestion(List<IDomain> domainList) {
    List<DomainSuggestionModel> suggestionModels = null;
    DomainSuggestionModel domainSuggestionModel;

    if (domainList != null) {
      suggestionModels = new ArrayList<DomainSuggestionModel>();
      for (IDomain i : domainList) {
        domainSuggestionModel = new DomainSuggestionModel();
        domainSuggestionModel.id = i.getId();
        domainSuggestionModel.text = i.getName();
        suggestionModels.add(domainSuggestionModel);
      }
    }
    return suggestionModels;
  }

  public List<IDomainLink> buildDomainLink(List<DomainSuggestionModel> suggestionModels,
                                           Long domainId, DomainsService ds) {
    List<IDomainLink> domainLinkList = null;
    if (suggestionModels != null && suggestionModels.size() > 0) {
      try {
        domainLinkList = new ArrayList<IDomainLink>();
        for (DomainSuggestionModel d : suggestionModels) {
          IDomain domain = ds.getDomain(domainId);
          IDomainLink iDomainLink = JDOUtils.createInstance(IDomainLink.class);
          iDomainLink.setDomainId(domainId, domain.getName());
          iDomainLink.setLinkedDomainId(d.id, d.text);
          iDomainLink.setCreatedOn(new Date());
          iDomainLink.setType(0);// Todo: send type
          //IDomainLink riDomainLink = iDomainLink.loadReverseLink(iDomainLink);
          domainLinkList.add(iDomainLink);
          //domainLinkList.add(riDomainLink);
        }
      } catch (ServiceException e) {
        e.printStackTrace();
      } catch (ObjectNotFoundException e) {
        throw new InvalidServiceException("Unable to fetch domain");
      }

    }
    return domainLinkList;
  }

  public IDomainPermission buildDomainPermission(DomainModel model, Long domainId) {
    IDomainPermission permission = JDOUtils.createInstance(IDomainPermission.class);
    permission.setdId(domainId);
    DomainPermissionModel permissionModel = model.dp;
    if (permissionModel != null) {
      permission.setUsersView(permissionModel.uv == null ? false : permissionModel.uv);
      permission.setUsersAdd(permissionModel.ua == null ? false : permissionModel.ua);
      permission.setUsersEdit(permissionModel.ue == null ? false : permissionModel.ue);
      permission.setUsersRemove(permissionModel.ur == null ? false : permissionModel.ur);
      permission.setEntityView(permissionModel.ev == null ? false : permissionModel.ev);
      permission.setEntityAdd(permissionModel.ea == null ? false : permissionModel.ea);
      permission.setEntityEdit(permissionModel.ee == null ? false : permissionModel.ee);
      permission.setEntityRemove(permissionModel.er == null ? false : permissionModel.er);
      permission.setInventoryView(permissionModel.iv == null ? false : permissionModel.iv);
      permission.setInventoryAdd(permissionModel.ia == null ? false : permissionModel.ia);
      permission.setInventoryEdit(permissionModel.ie == null ? false : permissionModel.ie);
      permission.setInventoryRemove(permissionModel.ir == null ? false : permissionModel.ir);
      permission.setEntityGroupView(permissionModel.egv == null ? false : permissionModel.egv);
      permission.setEntityGroupAdd(permissionModel.ega == null ? false : permissionModel.ega);
      permission.setEntityGroupEdit(permissionModel.ege == null ? false : permissionModel.ege);
      permission.setEntityGroupRemove(permissionModel.egr == null ? false : permissionModel.egr);
      permission
          .setEntityRelationshipView(permissionModel.erv == null ? false : permissionModel.erv);
      permission
          .setEntityRelationshipAdd(permissionModel.era == null ? false : permissionModel.era);
      permission
          .setEntityRelationshipEdit(permissionModel.ere == null ? false : permissionModel.ere);
      permission
          .setEntityRelationshipRemove(permissionModel.err == null ? false : permissionModel.err);
      permission.setMaterialView(permissionModel.mv == null ? false : permissionModel.mv);
      permission.setMaterialAdd(permissionModel.ma == null ? false : permissionModel.ma);
      permission.setMaterialEdit(permissionModel.me == null ? false : permissionModel.me);
      permission.setMaterialRemove(permissionModel.mr == null ? false : permissionModel.mr);
      permission.setConfigurationView(permissionModel.cv == null ? false : permissionModel.cv);
      permission.setConfigurationEdit(permissionModel.ce == null ? false : permissionModel.ce);
      permission.setCopyConfiguration(permissionModel.cc == null ? false : permissionModel.cc);
      permission.setCopyMaterials(permissionModel.cm == null ? false : permissionModel.cm);
      permission.setAssetAdd(permissionModel.aa == null ? false : permissionModel.aa);
      permission.setAssetEdit(permissionModel.ae == null ? false : permissionModel.ae);
      permission.setAssetRemove(permissionModel.ar == null ? false : permissionModel.ar);
      permission.setAssetView(permissionModel.av == null ? false : permissionModel.av);
    }
    return permission;
  }

  public List<DomainSuggestionModel> buildLinkedDomainModelList(List<IDomainLink> linkedDomainList,
                                                                DomainsService ds,
                                                                Long currentDomain) {
    List<DomainSuggestionModel> linkedDomains = new ArrayList<DomainSuggestionModel>();
    int sno = 1;
    if (linkedDomainList != null && linkedDomainList.size() > 0) {
      for (IDomainLink i : linkedDomainList) {
        DomainSuggestionModel model = buildLinkedDomainModel(ds, i, currentDomain);
        if (IDomainLink.TYPE_PARENT != model.type) {
          model.sno = sno++;
        }
        linkedDomains.add(model);
      }
    }
    return linkedDomains;
  }

  public DomainSuggestionModel buildLinkedDomainModel(DomainsService ds, IDomainLink i,
                                                      Long currentDomain) {
    DomainSuggestionModel model = new DomainSuggestionModel();
    try {
      IDomain iDomain = ds.getDomain(i.getLinkedDomainId());
      model.id = iDomain.getId();
      model.text = iDomain.getName();
      model.dsc = iDomain.getDescription();
      model.dct = iDomain.getCreatedOn().toString();
      model.type = i.getType();
      model.key = i.getKey();
      model.hc = iDomain.getHasChild() == null ? false : iDomain.getHasChild();
      model.hp = iDomain.getHasParent() == null ? false : iDomain.getHasParent();
      if (i.getDomainId().equals(currentDomain) && i.getLinkedDomainId().equals(iDomain.getId())) {
        if (i.getType() == 0) {
          model.dc = true;
        } else if (i.getType() == 1) {
          model.dp = true;
        }
      }
      return model;
    } catch (ServiceException e) {
      throw new InvalidServiceException("Unable to fetch domain details for the domain" + i);
    } catch (ObjectNotFoundException e) {
      throw new InvalidServiceException("Unable to fetch domain details for the domain" + i);
    }
  }

  public List<IDomain> buildDomainSwitchSuggestions(List<IDomainLink> linkedDomainList,
                                                    DomainsService ds, IDomain currentDomain) {
    List<IDomain> domains = new ArrayList<IDomain>();
    IDomain domain = null;
    if (currentDomain != null) {
      domains.add(currentDomain);
    }
    if (linkedDomainList != null && linkedDomainList.size() > 0) {
      for (IDomainLink i : linkedDomainList) {
        try {
          if (i.getType() == 1) {
            domain = ds.getDomain(i.getDomainId());
          } else if (i.getType() == 0) {
            domain = ds.getDomain(i.getLinkedDomainId());
          }
          if (domain != null) {
            domains.add(domain);
          }
        } catch (ServiceException e) {
          throw new InvalidServiceException("Unable to fetch domain details for the domain");
        } catch (ObjectNotFoundException e) {
          throw new InvalidServiceException("Unable to fetch domain details for the domain");
        }
      }
    }
    return domains;
  }

  public List<IDomain> buildParentList(List<IDomainLink> linkedDomainList, DomainsService ds,
                                       IDomain currentDomain) {
    List<IDomain> domains = new ArrayList<IDomain>();
    IDomain domain = null;
    if (currentDomain != null) {
      domains.add(currentDomain);
    }
    if (linkedDomainList != null && linkedDomainList.size() > 0) {
      for (IDomainLink i : linkedDomainList) {
        try {
          if (i.getType() == 1) {
            domain = ds.getDomain(i.getLinkedDomainId());
          } else if (i.getType() == 0) {
            domain = ds.getDomain(i.getDomainId());
          }
          if (domain != null) {
            domains.add(domain);
          }
        } catch (ServiceException e) {
          throw new InvalidServiceException("Unable to fetch domain details for the domain");
        } catch (ObjectNotFoundException e) {
          throw new InvalidServiceException("Unable to fetch domain details for the domain");
        }
      }
    }
    return domains;

  }

  public Set<Long> buildChildDomainsList(List<IDomainLink> linkedDomainList) {
    Set<Long> domains = new HashSet<>();
    if (linkedDomainList != null && linkedDomainList.size() > 0) {
      for (IDomainLink i : linkedDomainList) {
        domains.add(i.getLinkedDomainId());
      }
    }
    return domains;
  }

  public DomainModel buildDomain(IDomain domain, IDomainPermission userDomainPermission,
                                 IDomainPermission permission, DomainConfig dc, boolean action,
                                 boolean viewOnly, boolean asset, boolean iMan) {
    if (permission == null) {
      return null;
    }
    DomainModel model = new DomainModel();
    DomainPermissionModel dp = new DomainPermissionModel();
    dp.uv = permission.isUsersView();
    dp.ua = permission.isUsersAdd();
    dp.ue = permission.isUsersEdit();
    dp.ur = permission.isUsersRemove();
    dp.ev = permission.isEntityView();
    dp.ea = permission.isEntityAdd();
    dp.ee = permission.isEntityEdit();
    dp.er = permission.isEntityRemove();
    dp.egv = permission.isEntityGroupView();
    dp.ega = permission.isEntityGroupAdd();
    dp.ege = permission.isEntityGroupEdit();
    dp.egr = permission.isEntityGroupRemove();
    dp.iv = permission.isInventoryView();
    dp.ia = permission.isInventoryAdd();
    dp.ie = permission.isInventoryEdit();
    dp.ir = permission.isInventoryRemove();
    dp.mv = permission.isMaterialView();
    dp.ma = permission.isMaterialAdd();
    dp.me = permission.isMaterialEdit();
    dp.mr = permission.isMaterialRemove();
    dp.cv = permission.isConfigurationView();
    dp.ce = permission.isConfigurationEdit();
    dp.erv = permission.isEntityRelationshipView();
    dp.era = permission.isEntityRelationshipAdd();
    dp.ere = permission.isEntityRelationshipEdit();
    dp.err = permission.isEntityRelationshipRemove();
    dp.cc = permission.isCopyConfiguration();
    dp.cm = permission.isCopyMaterials();
    dp.ae = permission.isAssetEdit();
    dp.ar = permission.isAssetRemove();
    dp.aa = permission.isAssetAdd();
    dp.av = permission.isAssetView();
    model.dId = domain.getId();
    model.name = domain.getName();
    model.hasChild = domain.getHasChild();
    if (userDomainPermission != null && action) {
      if (!dp.uv) {
        dp.uv = userDomainPermission.isUsersView();
      }
      if (!dp.ua) {
        dp.ua = userDomainPermission.isUsersAdd();
      }
      if (!dp.ue) {
        dp.ue = userDomainPermission.isUsersEdit();
      }
      if (!dp.ur) {
        dp.ur = userDomainPermission.isUsersRemove();
      }
      if (!dp.ev) {
        dp.ev = userDomainPermission.isEntityView();
      }
      if (!dp.ea) {
        dp.ea = userDomainPermission.isEntityAdd();
      }
      if (!dp.ee) {
        dp.ee = userDomainPermission.isEntityEdit();
      }
      if (!dp.er) {
        dp.er = userDomainPermission.isEntityRemove();
      }
      if (!dp.egv) {
        dp.egv = userDomainPermission.isEntityGroupView();
      }
      if (!dp.ega) {
        dp.ega = userDomainPermission.isEntityGroupAdd();
      }
      if (!dp.ege) {
        dp.ege = userDomainPermission.isEntityGroupEdit();
      }
      if (!dp.egr) {
        dp.egr = userDomainPermission.isEntityGroupRemove();
      }
      if (!dp.iv) {
        dp.iv = userDomainPermission.isInventoryView();
      }
      if (!dp.ia) {
        dp.ia = userDomainPermission.isInventoryAdd();
      }
      if (!dp.ie) {
        dp.ie = userDomainPermission.isInventoryEdit();
      }
      if (!dp.ir) {
        dp.ir = userDomainPermission.isInventoryRemove();
      }
      if (!dp.mv) {
        dp.mv = userDomainPermission.isMaterialView();
      }
      if (!dp.ma) {
        dp.ma = userDomainPermission.isMaterialAdd();
      }
      if (!dp.me) {
        dp.me = userDomainPermission.isMaterialEdit();
      }
      if (!dp.mr) {
        dp.mr = userDomainPermission.isMaterialRemove();
      }
      if (!dp.erv) {
        dp.erv = userDomainPermission.isEntityRelationshipView();
      }
      if (!dp.era) {
        dp.era = userDomainPermission.isEntityRelationshipAdd();
      }
      if (!dp.ere) {
        dp.ere = userDomainPermission.isEntityRelationshipEdit();
      }
      if (!dp.err) {
        dp.err = userDomainPermission.isEntityRelationshipRemove();
      }
      if (!dp.cv) {
        dp.cv = userDomainPermission.isConfigurationView();
      }
      if (!dp.ce) {
        dp.ce = userDomainPermission.isConfigurationEdit();
      }
      if (!dp.av) {
        dp.av = userDomainPermission.isAssetView();
      }
      if (!dp.aa) {
        dp.aa = userDomainPermission.isAssetAdd();
      }
      if (!dp.ae) {
        dp.ae = userDomainPermission.isAssetEdit();
      }
      if (!dp.ar) {
        dp.ar = userDomainPermission.isAssetRemove();
      }
    }
    if (viewOnly || asset) {
      dp.vp = true;
      dp.ua = false;
      dp.ue = false;
      dp.ur = false;
      dp.ea = false;
      dp.ee = false;
      dp.er = false;
      dp.ega = false;
      dp.ege = false;
      dp.egr = false;
      dp.ia = false;
      dp.ie = false;
      dp.ir = false;
      dp.ma = false;
      dp.me = false;
      dp.mr = false;
      dp.ce = false;
      dp.era = false;
      dp.ere = false;
      dp.err = false;
      dp.aa = false;
      dp.ae = false;
      dp.ar = false;
    }
    if (asset) {
      dp.uv = false;
      dp.mv = false;
      dp.egv = false;
      dp.iv = false;
      dp.erv = false;
      dp.cv = false;
    }
    if (iMan) {
      dp.ma = false;
      dp.me = false;
      dp.mr = false;
    }
    if (iMan && action) {
      CapabilityConfig cConfig = dc.getCapabilityMapByRole().get(SecurityConstants.ROLE_SERVICEMANAGER);
      List<String> creatableEnt;
      creatableEnt =
          (cConfig == null) ? dc.getCreatableEntityTypes() : cConfig.getCreatableEntityTypes();
      if (creatableEnt == null || !creatableEnt.contains(CapabilityConfig.TYPE_MANAGEDENTITY)) {
        dp.ea = dp.ee = dp.er = false;
        dp.ua = dp.ue = dp.ur = false;
      }
    }
    model.dp = dp;
    return model;
  }


  public DomainDBModel buildDomainModel(IDomain domain) {
    DomainDBModel model = new DomainDBModel();
    model.dId = domain.getId();
    model.name = domain.getName();
    model.nNm = domain.getNormalizedName();
    model.description = domain.getDescription();
    model.ownerId = domain.getOwnerId();
    model.createdOn = domain.getCreatedOn();
    model.isActive = domain.isActive();
    model.rptEnabled = domain.isReportEnabled();
    model.hasChild = domain.getHasChild();
    model.hasParent = domain.getHasParent();
    model.ub = domain.getLastUpdatedBy();
    model.uo = domain.getLastUpdatedOn();
    return model;
  }
}

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

import com.logistimo.api.models.EntityApproversModel;
import com.logistimo.api.models.EntityDomainModel;
import com.logistimo.api.models.EntityHierarchyModel;
import com.logistimo.api.models.EntityModel;
import com.logistimo.api.models.EntitySummaryModel;
import com.logistimo.api.models.PermissionModel;
import com.logistimo.api.models.UserModel;
import com.logistimo.api.request.NetworkViewResponseObj;
import com.logistimo.config.models.DomainConfig;
import com.logistimo.constants.Constants;
import com.logistimo.dao.JDOUtils;
import com.logistimo.domains.entity.IDomain;
import com.logistimo.domains.service.DomainsService;
import com.logistimo.domains.service.impl.DomainsServiceImpl;
import com.logistimo.domains.utils.DomainsUtil;
import com.logistimo.entities.auth.EntityAuthoriser;
import com.logistimo.entities.entity.IApprovers;
import com.logistimo.entities.entity.IKiosk;
import com.logistimo.entities.entity.IKioskLink;
import com.logistimo.entities.models.EntityLinkModel;
import com.logistimo.entities.service.EntitiesService;
import com.logistimo.entities.service.EntitiesServiceImpl;
import com.logistimo.inventory.InventoryUtils;
import com.logistimo.logger.XLog;
import com.logistimo.models.superdomains.DomainSuggestionModel;
import com.logistimo.pagination.Results;
import com.logistimo.reports.entity.slices.IReportsSlice;
import com.logistimo.services.ObjectNotFoundException;
import com.logistimo.services.ServiceException;
import com.logistimo.services.Services;
import com.logistimo.users.entity.IUserAccount;
import com.logistimo.users.service.UsersService;
import com.logistimo.users.service.impl.UsersServiceImpl;
import com.logistimo.utils.BigUtil;
import com.logistimo.utils.CommonUtils;
import com.logistimo.utils.LocalDateUtil;

import org.apache.commons.lang.StringUtils;

import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public class EntityBuilder {

  private static final XLog xLogger = XLog.getLog(EntityBuilder.class);

  PoolGroupBuilder poolGroupBuilder = new PoolGroupBuilder();

  private static Integer getRank(Map<String, Integer> entityTagRank, List<String> tags) {
    int rank = 0;
    if (!entityTagRank.isEmpty() && tags != null && tags.size() > 0) {
      for (String tag : tags) {
        Integer tagRank = entityTagRank.get(tag);
        if (tagRank != null && tagRank > rank) {
          rank = tagRank;
        }
      }
    }
    return rank;
  }

  public static EntityHierarchyModel buildFinalEntityHierarchyModel(Map<Long, Entity> entityMap,
                                                                    NetworkViewResponseObj nvo,
                                                                    Integer linksSize) {
    EntityHierarchyModel finalModel = new EntityHierarchyModel();
    List<EntityHierarchyModel> finalModelList = new ArrayList<>();
    Map<Long, Set<Long>> extraLinksMap = new HashMap<>();
    Map<Long, Set<Long>> multipleParentsMap = new HashMap<>();
    if (!entityMap.isEmpty()) {
      for (Entity details : entityMap.values()) {
        int level = 0;
        List<ExtraLinks> extraLinks = new ArrayList<>();
        if (details.depth == 1) {
          EntityHierarchyModel levelOneModel = new EntityHierarchyModel();
          levelOneModel.name = details.kioskName;
          levelOneModel.id = details.kid;
          levelOneModel.maxlevel = levelOneModel.level = ++level;
          if (!details.children.isEmpty()) {
            List<EntityHierarchyModel> levelOneChildren;
            levelOneChildren =
                buildEntityHierarchyChildren(details.kid, details.children, details.depth, entityMap
                    , extraLinks, level, levelOneModel);
            if (levelOneChildren.size() > 0) {
              levelOneModel.children = levelOneChildren;
            }
            if (levelOneModel.maxlevel > finalModel.maxlevel) {
              finalModel.maxlevel = levelOneModel.maxlevel;
            }
            if (extraLinks.size() > 0) {
              for (ExtraLinks links : extraLinks) {
                Set<Long> destinationLinks;
                if (extraLinksMap.get(links.sid) == null) {
                  destinationLinks = new HashSet<>();
                  destinationLinks.add(links.did);
                  extraLinksMap.put(links.sid, destinationLinks);
                } else {
                  destinationLinks = extraLinksMap.get(links.sid);
                  destinationLinks.add(links.did);
                  extraLinksMap.put(links.sid, destinationLinks);
                }
              }
            }
          }
          finalModelList.add(levelOneModel);
        } else if (details.parents != null && details.parents.size() > 0) {
          Map<Integer, Set<Long>> levelMap = new HashMap<>();
          for (final Long parentId : details.parents) {
            Entity ent = entityMap.get(parentId);
            if (levelMap.containsKey(ent.depth)) {
              levelMap.get(ent.depth).add(parentId);
            } else {
              levelMap.put(ent.depth, new HashSet<Long>() {{
                add(parentId);
              }});
            }
          }

          for (Set<Long> parents : levelMap.values()) {
            if (parents.size() > 1) {
              if (multipleParentsMap.containsKey(details.kid)) {
                multipleParentsMap.get(details.kid).addAll(parents);
              } else {
                multipleParentsMap.put(details.kid, parents);
              }
            }
          }
        }
      }
    }
    nvo.extraLinks = extraLinksMap;
    nvo.multipleParentLinks = multipleParentsMap;

    if (finalModelList.size() > 0) {
      finalModel.name = "dummy";
      finalModel.level = 0;
      finalModel.children = finalModelList;
      finalModel.links = linksSize;
      finalModel.upd = new Date();
    }

    return finalModel;
  }

  private static List<EntityHierarchyModel> buildEntityHierarchyChildren(Long kioskId,
                                                                         Set<Long> children,
                                                                         int depth,
                                                                         Map<Long, Entity> entityMap,
                                                                         List<ExtraLinks> extraLinks,
                                                                         int parentLevel,
                                                                         EntityHierarchyModel levelOneModel) {
    List<EntityHierarchyModel> hierarchyModels = new ArrayList<>();
    Entity sourceDetails = entityMap.get(kioskId);
    if (!children.isEmpty()) {
      for (Long child : children) {
        int childLevel = parentLevel + 1;
        Entity kioskDetails = entityMap.get(child);
        EntityHierarchyModel model = null;
        if (kioskDetails.depth - depth == 1) {
          model = new EntityHierarchyModel();
          model.name = kioskDetails.kioskName;
          model.id = kioskDetails.kid;
          model.level = childLevel;
          if (!kioskDetails.children.isEmpty()) {
            List<EntityHierarchyModel> modelList = buildEntityHierarchyChildren(child,
                kioskDetails.children, kioskDetails.depth, entityMap, extraLinks, childLevel,
                levelOneModel);
            if (modelList.size() > 0) {
              model.children = modelList;
            }
          }
          if (levelOneModel.maxlevel < model.level) {
            levelOneModel.maxlevel = model.level;
          }
          hierarchyModels.add(model);
        } else if (kioskDetails.depth - depth > 1) {
          extraLinks.add(new ExtraLinks(sourceDetails.kioskName, kioskDetails.kioskName, depth,
              kioskDetails.depth, sourceDetails.kid, kioskDetails.kid));
        }
      }
    }
    return hierarchyModels;
  }

  private static void updateDepth(Long kiosk, Long child, int depth, List<Long> parentPath,
                                  Map<Long, Entity> entityMap) {
    Entity childDetails = entityMap.get(child);
    if ((childDetails.depth < depth) || (childDetails.depth == depth && childDetails.parents
        .contains(kiosk))) {
      childDetails.depth = ++depth;
      List<Long> newParentPath = new ArrayList<>(parentPath);
      newParentPath.add(child);
      childDetails.pathList = newParentPath;
      for (Long mychild : childDetails.children) {
        if (!newParentPath.contains(mychild)) {
          updateDepth(child, mychild, childDetails.depth, newParentPath, entityMap);
        }
      }
    }
  }

  public String getDomainName(Long domainId) {
    IDomain domain = null;
    String domainName;
    try {
      DomainsService ds = Services.getService(DomainsServiceImpl.class);
      domain = ds.getDomain(domainId);
    } catch (Exception e) {
      xLogger.warn("Error while fetching Domain {0}", domainId);
    }
    if (domain != null) {
      domainName = domain.getName();
    } else {
      domainName = Constants.EMPTY;
    }
    return domainName;
  }

  public Results buildEntityResults(Results kioskResults, Locale locale, String timezone) {
    List entities = kioskResults.getResults();
    List<EntityModel> newEntities = new ArrayList<EntityModel>();
    DomainsService ds = Services.getService(DomainsServiceImpl.class);
    Map<Long, String> domainNames = new HashMap<>(1);
    if (entities != null) {
      UsersService as = null;
      as = Services.getService(UsersServiceImpl.class);
      int sno = kioskResults.getOffset() + 1;
      for (Object obj : entities) {
        IKiosk kiosk = (IKiosk) obj;
        String domainName = domainNames.get(kiosk.getDomainId());
        if (domainName == null) {
          IDomain domain = null;
          try {
            domain = ds.getDomain(kiosk.getDomainId());
          } catch (Exception e) {
            xLogger.warn("Error while fetching Domain {0}", kiosk.getDomainId());
          }
          if (domain != null) {
            domainName = domain.getName();
          } else {
            domainName = Constants.EMPTY;
          }
          domainNames.put(kiosk.getDomainId(), domainName);
        }
        EntityModel model = buildBaseModel((IKiosk) obj, locale, timezone, domainName);
        model.sno = sno++;
        if (as != null) {
          try {
            IUserAccount ua;
            if (StringUtils.isBlank(model.lub)) {
              ua = as.getUserAccount(model.rus);
              model.lub = ua.getUserId();
              model.lubn = ua.getFullName();
            } else {
              ua = as.getUserAccount(model.lub);
              model.lubn = ua.getFullName();
            }
          } catch (Exception e) {
            xLogger.warn("Error in fetching user details", e);
          }
        }
        newEntities.add(model);
      }
    }
    return new Results(newEntities, kioskResults.getCursor(), kioskResults.getNumFound(),
        kioskResults.getOffset());
  }

  public EntityModel buildModel(IKiosk k, IUserAccount u, IUserAccount lu, Locale locale,
                                String timezone) {
    EntityModel model = null;
    if (k != null) {
      model = buildModel(k, locale, timezone);
      if (u != null) {
        model.rusn = u.getFullName();
      }
      if (lu != null) {
        model.lubn = lu.getFullName();
      }
    }
    return model;
  }

  /**
   * Builds complete entity model including user details
   */
  public EntityModel buildModel(IKiosk k, Locale locale, String timezone) {
    EntityModel model = buildBaseModel(k, locale, timezone, getDomainName(k.getDomainId()));
    model.usrs = new UserBuilder().buildUserModels(k.getUsers(), locale, timezone, true);
    model.cid = k.getCustomId();
    model.str = k.getStreet();
    model.ds = k.getDistrict();
    model.cur = k.getCurrency();
    model.inv = k.getInventoryModel();
    model.invDsp = InventoryUtils.getModelDisplay(k.getInventoryModel());
    model.om = k.getOrderingMode();
    model.zip = k.getPinCode();
    model.rt = k.getRouteTag();
    model.pgs = poolGroupBuilder.buildPoolGroupModels(k.getPoolGroups());
    model.sl = k.getServiceLevel();
    model.tx = k.getTax();
    model.txid = k.getTaxId();
    model.typ = k.getType();
    model.oo = k.isOptimizationOn();
    model.rus = k.getRegisteredBy();
    model.lub = k.getUpdatedBy();
    model.add = CommonUtils.getAddress(k.getCity(), k.getTaluk(), k.getDistrict(), k.getState());
    model.be = k.isBatchMgmtEnabled();
    return model;
  }

  public List<EntityModel> buildFilterModelList(List<IKiosk> k) {
    List<EntityModel> models = new ArrayList<EntityModel>(k.size());
    for (IKiosk kiosk : k) {
      models.add(buildFilterModel(kiosk));
    }
    return models;
  }

  public EntityModel buildFilterModel(IKiosk k) {
    EntityModel model = new EntityModel();
    model.id = k.getKioskId();
    model.nm = k.getName();
    model.st = k.getState();
    model.ds = k.getDistrict();
    model.ct = k.getCity();
    return model;
  }

  public List<EntityDomainModel> buildEntityDomainData(IKiosk k) throws ServiceException {
    List<EntityDomainModel> models = new ArrayList<EntityDomainModel>(k.getDomainIds().size());
    DomainsService domainsService = Services.getService(DomainsServiceImpl.class);
    Set<Long> leafDomains = DomainsUtil.extractLeafDomains(k.getDomainIds(), k.getDomainId());
    for (Long did : k.getDomainIds()) {
      try {
        EntityDomainModel model = new EntityDomainModel();
        model.did = did;
        IDomain domain = domainsService.getDomain(did);
        model.name = domain.getName();
        model.desc = domain.getDescription();
        model.isRemovable = leafDomains.contains(did);
        models.add(model);
      } catch (ObjectNotFoundException e) {
        throw new ServiceException(e.getMessage(), e);
      }
    }
    return models;
  }

  public List<DomainSuggestionModel> buildEntityDomainSuggestions(IKiosk k, List<IDomain> domains)
      throws ServiceException {
    if (domains == null) {
      return null;
    }
    List<DomainSuggestionModel> suggestionModels = new ArrayList<DomainSuggestionModel>();
    List<Long> currentDomainIds = k.getDomainIds();
    for (IDomain domain : domains) {
      if (!currentDomainIds.contains(domain.getId())) {
        DomainSuggestionModel domainSuggestionModel = new DomainSuggestionModel();
        domainSuggestionModel.id = domain.getId();
        domainSuggestionModel.text = domain.getName();
        suggestionModels.add(domainSuggestionModel);
      }
    }
    return suggestionModels;
  }

  /**
   * Builds partial model with basic details like name, address etc.
   */
  public EntityModel buildBaseModel(IKiosk k, Locale locale, String timezone, String name) {
    EntityModel model = new EntityModel();
    model.id = k.getKioskId();
    model.nm = k.getName();
    model.ct = k.getCity();
    model.ctr = k.getCountry();
    model.ln = k.getLongitude();
    model.lt = k.getLatitude();
    model.st = k.getState();
    model.ts = LocalDateUtil.format(k.getTimeStamp(), locale, timezone);
    if (k.getLastUpdated() != null) {
      model.lts = LocalDateUtil.format(k.getLastUpdated(), locale, timezone);
    }
    model.tgs = k.getTags();
    model.tlk = k.getTaluk();
    model.ds = k.getDistrict();
    model.rus = k.getRegisteredBy();
    model.lub = k.getUpdatedBy();
    model.loc = k.getLocation();
    model.oo = k.isOptimizationOn();
    model.sdid = k.getDomainId();
    model.sdname = name;
    model.add = CommonUtils.getAddress(k.getCity(), k.getTaluk(), k.getDistrict(), k.getState());
    model.be = k.isBatchMgmtEnabled();
    if (k.getInventoryActiveTime() != null) {
      model.iat = LocalDateUtil.format(k.getInventoryActiveTime(), locale, timezone);
    }
    if (k.getOrderActiveTime() != null) {
      model.oat = LocalDateUtil.format(k.getOrderActiveTime(), locale, timezone);
    }
    return model;
  }

  /**
   * Builds complete kiosk object from the entity model which includes user details
   */
  public IKiosk buildKiosk(EntityModel model, Locale locale, String timezone, String userName,
                           boolean isAdd) {
    return buildKiosk(model, locale, timezone, userName, JDOUtils.createInstance(IKiosk.class),
        isAdd);
  }

  public List<IApprovers> buildApprovers(EntityApproversModel model, String username, Long domainId) {
    List<IApprovers> approvers = null;
    if(model != null) {
      approvers = new ArrayList<>();
      if(model.pap != null && model.pap.size() > 0) {
        for(UserModel um : model.pap) {
          IApprovers primaryApprover = JDOUtils.createInstance(IApprovers.class);
          primaryApprover.setKioskId(model.entityId);
          primaryApprover.setUserId(um.id);
          primaryApprover.setType(IApprovers.PRIMARY_APPROVER);
          primaryApprover.setOrderType(IApprovers.PURCHASE_ORDER);
          primaryApprover.setCreatedBy(username);
          primaryApprover.setCreatedOn(new Date());
          primaryApprover.setSourceDomainId(domainId);
          approvers.add(primaryApprover);
        }
      }
      if(model.sap != null && model.sap.size() > 0) {
        for(UserModel um : model.sap) {
          IApprovers secondaryApprover = JDOUtils.createInstance(IApprovers.class);
          secondaryApprover.setKioskId(model.entityId);
          secondaryApprover.setUserId(um.id);
          secondaryApprover.setType(IApprovers.SECONDARY_APPROVER);
          secondaryApprover.setOrderType(IApprovers.PURCHASE_ORDER);
          secondaryApprover.setCreatedBy(username);
          secondaryApprover.setCreatedOn(new Date());
          secondaryApprover.setSourceDomainId(domainId);
          approvers.add(secondaryApprover);
        }
      }
      if(model.pas != null && model.pas.size() > 0) {
        for(UserModel um : model.pas) {
          IApprovers secondaryApprover = JDOUtils.createInstance(IApprovers.class);
          secondaryApprover.setKioskId(model.entityId);
          secondaryApprover.setUserId(um.id);
          secondaryApprover.setType(IApprovers.PRIMARY_APPROVER);
          secondaryApprover.setOrderType(IApprovers.SALES_ORDER);
          secondaryApprover.setCreatedBy(username);
          secondaryApprover.setCreatedOn(new Date());
          secondaryApprover.setSourceDomainId(domainId);
          approvers.add(secondaryApprover);
        }
      }
      if(model.sas != null && model.sas.size() > 0) {
        for(UserModel um : model.sas) {
          IApprovers secondaryApprover = JDOUtils.createInstance(IApprovers.class);
          secondaryApprover.setKioskId(model.entityId);
          secondaryApprover.setUserId(um.id);
          secondaryApprover.setType(IApprovers.SECONDARY_APPROVER);
          secondaryApprover.setOrderType(IApprovers.SALES_ORDER);
          secondaryApprover.setCreatedBy(username);
          secondaryApprover.setCreatedOn(new Date());
          secondaryApprover.setSourceDomainId(domainId);
          approvers.add(secondaryApprover);
        }
      }

    }
    return approvers;
  }

  public EntityApproversModel buildApprovalsModel(List<IApprovers> approvers, UsersService as, Locale locale,
                                                  String timezone) {
    EntityApproversModel model = new EntityApproversModel();
    if(approvers != null && approvers.size() > 0) {
      List<String> pap = new ArrayList<>();
      List<String> sap = new ArrayList<>();
      List<String> pas = new ArrayList<>();
      List<String> sas = new ArrayList<>();
      UserBuilder userBuilder = new UserBuilder();
      for(IApprovers apr : approvers) {
        if(StringUtils.isNotEmpty(apr.getUserId())) {
          if(apr.getType().equals(IApprovers.PRIMARY_APPROVER)) {
            if(apr.getOrderType().equals(IApprovers.PURCHASE_ORDER)) {
              pap.add(apr.getUserId());
            } else {
              pas.add(apr.getUserId());
            }
          } else {
            if(apr.getOrderType().equals(IApprovers.PURCHASE_ORDER)) {
              sap.add(apr.getUserId());
            } else {
              sas.add(apr.getUserId());
            }
          }
        }
      }
      if(pap.size() > 0) {
        model.pap = userBuilder.buildUserModels(constructUserAccount(as, pap), locale, timezone, true);
      }
      if(sap.size() > 0) {
        model.sap = userBuilder.buildUserModels(constructUserAccount(as, sap), locale, timezone, true);
      }
      if(pas.size() > 0) {
        model.pas = userBuilder.buildUserModels(constructUserAccount(as, pas), locale, timezone, true);
      }
      if(sap.size() > 0) {
        model.sas = userBuilder.buildUserModels(constructUserAccount(as, sas), locale, timezone, true);
      }
      model.createdBy = approvers.get(0).getCreatedBy();
      model.lastUpdated = approvers.get(0).getUpdatedOn();
      model.entityId = approvers.get(0).getKioskId();
    }

    return model;
  }

  public IKiosk buildKiosk(EntityModel model, Locale locale, String timezone, String userName,
                           IKiosk k, boolean isAdd) {
    final String empty = "";
    k.setStreet(model.str != null ? model.str : empty);
    k.setDistrict(model.ds != null ? model.ds : empty);
    k.setCurrency(model.cur != null ? model.cur : empty);
    k.setInventoryModel(model.inv != null ? model.inv : empty);
    k.setOrderingMode(model.om != null ? model.om : empty);
    k.setPinCode(model.zip != null ? model.zip : empty);
    k.setRouteTag(model.rt != null ? model.rt : empty);
    k.setTaxId(model.txid != null ? model.txid : empty);
    k.setType(model.typ != null ? model.typ : empty);
    k.setName(model.nm != null ? model.nm : empty);
    k.setCity(model.ct != null ? model.ct : empty);
    k.setCountry(model.ctr != null ? model.ctr : empty);
    k.setState(model.st != null ? model.st : empty);
    k.setTags(model.tgs != null ? model.tgs : new ArrayList<String>());
    k.setTaluk(model.tlk != null ? model.tlk : empty);
    k.setCustomId(model.cid != null ? model.cid : empty);
    if (isAdd) {
      k.setRegisteredBy(userName);
    }
    k.setUpdatedBy(userName);
    k.setUsers(new UserBuilder().buildUserAccounts(model.usrs, locale, timezone, true));
    k.setLongitude(model.ln);
    k.setLatitude(model.lt);
    k.setKioskId(model.id);
    if (model.inv.equalsIgnoreCase("sq")) {
      k.setServiceLevel(model.sl);
    } else {
      k.setServiceLevel(0);
    }
    k.setTax(model.tx);
    k.setBatchMgmtEnabled(model.be);
    return k;
  }

  public List<EntityModel> buildEntityLinks(EntitiesService as, List<IKioskLink> links,
                                            Locale locale, String timezone, String userId,
                                            Long domainId, String role) throws ServiceException {
    if (links == null) {
      return null;
    }
    List<EntityModel> kioskList = new ArrayList<EntityModel>(links.size());
    Map<Long, String> domainNames = new HashMap<>(1);
    int sno = 1;
    String domainName;
    for (IKioskLink kl : links) {
      IKiosk k;
      try {
        k = as.getKiosk(kl.getLinkedKioskId(), false);
      } catch (Exception e) {
        xLogger.fine("Unable to fetch the entity details for" + kl.getLinkedKioskId());
        continue;
      }
      EntityModel m = new EntityModel();
      m.id = kl.getLinkedKioskId();
      m.lid = kl.getId();
      m.nm = k.getName();
      m.loc = k.getLocation();
      m.lt = k.getLatitude();
      m.ln = k.getLongitude();
      m.st = k.getState();
      m.ds = k.getDistrict();
      m.ct = k.getCity();
      m.sdid = k.getDomainId();
      domainName = domainNames.get(k.getDomainId());
      m.cl = kl.getCreditLimit();
      m.co = LocalDateUtil.format(kl.getCreatedOn(), locale, timezone);
      m.desc = kl.getDescription();
      m.rt = StringUtils.isBlank(kl.getRouteTag()) ? "--notag--" : kl.getRouteTag();
      m.ri = kl.getRouteIndex();
      m.perm = EntityAuthoriser.authoriseEntityPerm(k.getKioskId(), role, locale, userId, domainId);
      if (domainName == null) {
        IDomain domain;
        try {
          DomainsService ds = Services.getService(DomainsServiceImpl.class);
          domain = ds.getDomain(k.getDomainId());
          if (domain != null) {
            domainName = domain.getName();
          } else {
            domainName = Constants.EMPTY;
          }
          domainNames.put(k.getDomainId(), domainName);
        } catch (Exception e) {
          xLogger.fine("Unable to fetch the relationship details" + e);
        }
      }
      m.sdname = domainName;
      m.sno = sno++;
      kioskList.add(m);
    }
    return kioskList;
  }

  public List<EntityModel> buildUserEntities(List<IKiosk> entity) {
    List<EntityModel> entList = new ArrayList<EntityModel>(entity.size());
    Map<Long, String> domainNames = new HashMap<>(1);
    for (IKiosk k : entity) {
      EntityModel m = new EntityModel();
      m.sdid = k.getDomainId();
      m.id = k.getKioskId();
      m.nm = k.getName();
      m.loc = CommonUtils.getAddress(k.getCity(), k.getTaluk(), k.getDistrict(), k.getState());
      m.rt = StringUtils.isBlank(k.getRouteTag()) ? "--notag--" : k.getRouteTag();
      m.ri = k.getRouteIndex();
      m.lt = k.getLatitude();
      m.ln = k.getLongitude();
      m.be = k.isBatchMgmtEnabled();
      String domainName = domainNames.get(k.getDomainId());
      if (domainName == null) {
        IDomain domain;
        try {
          DomainsService ds = Services.getService(DomainsServiceImpl.class);
          domain = ds.getDomain(k.getDomainId());
          if (domain != null) {
            domainName = domain.getName();
          } else {
            domainName = Constants.EMPTY;
          }
          domainNames.put(k.getDomainId(), domainName);
        } catch (Exception e) {
          xLogger.fine("Unable to fetch the domain details " + e);
        }
      }
      m.sdname = domainName;
      entList.add(m);
    }
    return entList;
  }

  public List<EntitySummaryModel> buildStatsMap(List<IReportsSlice> results) {

    List<EntitySummaryModel> summaryModelList = null;
    DateFormat dateFormat = new SimpleDateFormat("MMM yyyy");
    if (results != null) {
      summaryModelList = new ArrayList<>();
      for (int i = 0; i < results.size(); i++) {
        EntitySummaryModel model = new EntitySummaryModel();
        model.month = dateFormat.format(results.get(i).getDate());
        model.soc = Float.toString(results.get(i).getAverageStockoutResponseTime());
        model.oc = Integer.toString(results.get(i).getOrderCount());
        model.tc = Integer.toString(results.get(i).getTotalCount());
        model.br = BigUtil.getFormattedValue(results.get(i).getRevenueBooked());

        Calendar calendar = new GregorianCalendar();
        calendar.setTime(results.get(i).getDate());
        calendar.add(java.util.Calendar.MONTH, -1);

        if (results.size() > i + 1 && results.get(i + 1).getDate().equals(calendar.getTime())) {
          model.socs =
              getStatus(results.get(i).getAverageStockoutResponseTime(),
                  results.get(i + 1).getAverageStockoutResponseTime());
          model.ocs = getStatus(results.get(i).getOrderCount(), results.get(i + 1).getOrderCount());
          model.tcs = getStatus(results.get(i).getTotalCount(), results.get(i + 1).getTotalCount());
          model.brs =
              getStatus(results.get(i).getRevenueBooked(), results.get(i + 1).getRevenueBooked());
        } else {
          model.socs = "";
          model.ocs = "";
          model.tcs = "";
          model.brs = "";
        }

        summaryModelList.add(model);

      }
    }
    return summaryModelList;
  }

  private String getStatus(float cur, float prev) {
    if (cur > prev) {
      return "u";
    } else if (cur < prev) {
      return "d";
    } else {
      return "e";
    }
  }

  private String getStatus(BigDecimal cur, BigDecimal prev) {
    if (BigUtil.greaterThan(cur, prev)) {
      return "u";
    } else if (BigUtil.lesserThan(cur, prev)) {
      return "d";
    } else {
      return "e";
    }
  }

  public PermissionModel buildPermissionModel(IKiosk kiosk, int cPerm, int vPerm) {
    PermissionModel model = new PermissionModel();
    model.eid = kiosk.getKioskId();
    model.enm = kiosk.getName();
    model.cPerm = cPerm;
    model.vPerm = vPerm;
    return model;
  }

  public IKiosk addRelationPermission(IKiosk kiosk, PermissionModel permissionModel) {
    if (kiosk != null && permissionModel != null) {
      kiosk.setVendorPerm(permissionModel.vPerm);
      kiosk.setCustomerPerm(permissionModel.cPerm);
    }
    return kiosk;
  }

  public NetworkViewResponseObj buildNetworkViewRequestObject(List<EntityLinkModel> entityLinks,
                                                              Long domainId) {
    NetworkViewResponseObj nvo = null;
    //Get the configuration tag order rank for the current domain
    Map<String, Integer> entityTagRank;
    DomainConfig dc = DomainConfig.getInstance(domainId);
    entityTagRank = dc.getEntityTagOrder();
    if (entityLinks != null && entityLinks.size() > 0) {
      nvo = new NetworkViewResponseObj();
      List<Link> linksList = new ArrayList<>();
      Link link = null;
      Map<Long, Entity> entityMap = new LinkedHashMap<>();
      try {
        EntitiesService as = Services.getService(EntitiesServiceImpl.class);
        for (EntityLinkModel entityLinkModel : entityLinks) {
          IKiosk kiosk;
          IKiosk kioskLink;
          if (entityLinkModel.kioskId != null) {
            kiosk = as.getKiosk(entityLinkModel.kioskId);
            if (entityLinkModel.linkedKioskId != null) {
              kioskLink = as.getKiosk(entityLinkModel.linkedKioskId);
              if (getRank(entityTagRank, kiosk.getTags()) <= getRank(entityTagRank,
                  kioskLink.getTags())) {
                entityMap.put(kiosk.getKioskId(), new Entity(kiosk.getKioskId(), kiosk.getName()));
                entityMap.put(kioskLink.getKioskId(),
                    new Entity(kioskLink.getKioskId(), kioskLink.getName()));
                link = constructLink(kiosk, kioskLink);
              }
            } else {
              entityMap.put(kiosk.getKioskId(), new Entity(kiosk.getKioskId(), kiosk.getName()));
              link = constructLink(kiosk, null);
            }
            if (link != null) {
              linksList.add(link);
            }
          }
        }
      } catch (ServiceException e) {
        xLogger.warn("Error while getting entity details", e);
      }
      for (Link tempLink : linksList) {
        if (tempLink.did != null && entityMap.get(tempLink.did) != null) {
          entityMap.get(tempLink.sid).children.add(tempLink.did);
          entityMap.get(tempLink.did).parents.add(tempLink.sid);
          entityMap.get(tempLink.did).kid = tempLink.did;
        }
      }
      for (Long kioskId : entityMap.keySet()) {
        Entity details = entityMap.get(kioskId);
        if (details.depth == -1) {
          List<Long> parentPath = Collections.singletonList(kioskId);
          details.depth = 1;
          for (Long childKioskId : details.children) {
            updateDepth(kioskId, childKioskId, details.depth, parentPath, entityMap);
          }
        }
      }
      nvo.network = buildFinalEntityHierarchyModel(entityMap, nvo, entityLinks.size());
    }
    return nvo;
  }

  private Link constructLink(IKiosk kiosk, IKiosk kioskLink) {
    Link link = null;
    if (kiosk != null) {
      if (kioskLink != null) {
        link =
            new Link(kiosk.getName(), kioskLink.getName(), kiosk.getTags(), kioskLink.getTags(),
                kiosk.getKioskId(), kioskLink.getKioskId());
      } else {
        link = new Link(kiosk.getName(), null, kiosk.getTags(), null, kiosk.getKioskId(), null);
      }
    }
    return link;
  }

  private List<IUserAccount> constructUserAccount(UsersService as, List<String> userIds) {
    if (userIds != null && userIds.size() > 0) {
      List<IUserAccount> list = new ArrayList<>(userIds.size());
      for (String userId : userIds) {
        try {
          list.add(as.getUserAccount(userId));
        } catch (Exception ignored) {
          // do nothing
        }
      }
      return list;
    }
    return new ArrayList<>();
  }

  static class Link {
    public String src;
    public String dstn;
    public List<String> srcTg;
    public List<String> dstnTg;
    public Long sid;
    public Long did;


    public Link(String src, String dstn, List<String> srcTg, List<String> dstnTg, Long sid,
                Long did) {
      this.src = src;
      this.dstn = dstn;
      this.srcTg = srcTg;
      this.dstnTg = dstnTg;
      this.sid = sid;
      this.did = did;
    }
  }

  static class Entity {
    public Set<Long> children = new HashSet<>();
    public int depth = -1;
    public Set<Long> parents = new HashSet<>();
    public List<Long> pathList;
    public Long kid;
    public String kioskName;

    public Entity(Long kioskId, String kioskName) {
      this.kid = kioskId;
      this.kioskName = kioskName;
    }

    public Entity() {

    }

    @Override
    public String toString() {
      return "Kiosk{" +
          "children=" + children +
          ", depth=" + depth +
          ", parents=" + parents +
          ", kid=" + kid +
          ", kioskName=" + kioskName +
          '}';
    }
  }

  static class ExtraLinks {
    public String src;
    public String dstn;
    public Integer srcDpth;
    public Integer dstnDpth;
    public Long sid;
    public Long did;

    public ExtraLinks(String src, String dstn, Integer srcDpth, Integer dstnDpth, Long sid,
                      Long did) {
      this.src = src;
      this.dstn = dstn;
      this.srcDpth = srcDpth;
      this.dstnDpth = dstnDpth;
      this.sid = sid;
      this.did = did;
    }
  }
}

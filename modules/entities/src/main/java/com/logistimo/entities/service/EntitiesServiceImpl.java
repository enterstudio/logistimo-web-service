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

package com.logistimo.entities.service;

import com.google.gson.Gson;
import com.logistimo.AppFactory;
import com.logistimo.assets.AssetUtil;
import com.logistimo.assets.entity.IAsset;
import com.logistimo.assets.models.AssetModel;
import com.logistimo.assets.models.AssetModels;
import com.logistimo.auth.SecurityConstants;
import com.logistimo.auth.SecurityUtil;
import com.logistimo.config.models.DomainConfig;
import com.logistimo.config.models.Permissions;
import com.logistimo.config.models.ReportsConfig;
import com.logistimo.constants.CharacterConstants;
import com.logistimo.constants.Constants;
import com.logistimo.constants.QueryConstants;
import com.logistimo.dao.JDOUtils;
import com.logistimo.domains.entity.IDomain;
import com.logistimo.domains.entity.IDomainLink;
import com.logistimo.domains.service.DomainsService;
import com.logistimo.domains.service.impl.DomainsServiceImpl;
import com.logistimo.domains.utils.DomainsUtil;
import com.logistimo.domains.utils.EntityRemover;
import com.logistimo.entities.dao.EntityDao;
import com.logistimo.entities.dao.IEntityDao;
import com.logistimo.entities.entity.*;
import com.logistimo.entities.models.EntityLinkModel;
import com.logistimo.entities.models.LocationSuggestionModel;
import com.logistimo.entities.models.UserEntitiesModel;
import com.logistimo.entities.pagination.processor.UpdateRouteProcessor;
import com.logistimo.entities.utils.EntityUtils;
import com.logistimo.events.entity.IEvent;
import com.logistimo.events.exceptions.EventGenerationException;
import com.logistimo.events.processor.EventPublisher;
import com.logistimo.exception.UnauthorizedException;
import com.logistimo.logger.XLog;
import com.logistimo.pagination.PageParams;
import com.logistimo.pagination.PagedExec;
import com.logistimo.pagination.QueryParams;
import com.logistimo.pagination.Results;
import com.logistimo.proto.JsonTagsZ;
import com.logistimo.services.ObjectNotFoundException;
import com.logistimo.services.Service;
import com.logistimo.services.ServiceException;
import com.logistimo.services.Services;
import com.logistimo.services.impl.PMF;
import com.logistimo.services.impl.ServiceImpl;
import com.logistimo.tags.TagUtil;
import com.logistimo.tags.dao.ITagDao;
import com.logistimo.tags.dao.TagDao;
import com.logistimo.tags.entity.ITag;
import com.logistimo.users.entity.IUserAccount;
import com.logistimo.users.service.UsersService;
import com.logistimo.users.service.impl.UsersServiceImpl;
import com.logistimo.utils.Counter;
import com.logistimo.utils.QueryUtil;
import com.logistimo.utils.StringUtil;
import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.StringUtils;

import javax.jdo.JDOObjectNotFoundException;
import javax.jdo.PersistenceManager;
import javax.jdo.Query;
import java.util.*;

public class EntitiesServiceImpl extends ServiceImpl implements EntitiesService {

  private static final XLog xLogger = XLog.getLog(EntitiesServiceImpl.class);
  private IEntityDao entityDao = new EntityDao();
  private ITagDao tagDao = new TagDao();

  /**
   * Is the user associated with this kiosk
   */
  @SuppressWarnings("unchecked")
  public boolean hasKiosk(String userId, Long kioskId) {
    xLogger.fine("Entered hasKiosk");
    if (userId == null || kioskId == null) {
      throw new IllegalArgumentException("Invalid userId or kioskId");
    }
    PersistenceManager pm = PMF.get().getPersistenceManager();
    String
        queryStr =
        "SELECT kioskId FROM " + JDOUtils.getImplClass(IUserToKiosk.class).getName()
            + " WHERE userId == userIdParam PARAMETERS String userIdParam";
    Query q = pm.newQuery(queryStr);
    try {
      List<Long> results = (List<Long>) q.execute(userId);
      if (results != null) {
        for (Long result : results) {
          if (result.equals(kioskId)) {
            return true;
          }
        }
        UsersService as;
        try {
          as = Services.getService(UsersServiceImpl.class);
          IUserAccount ua = as.getUserAccount(userId);
          Results kiosks = getKiosksForUser(ua, null, null, pm);
          if(kiosks.getResults()!=null) {
            for (Object k : kiosks.getResults()) {
              IKiosk kiosk = (IKiosk) k;
              Permissions perms = kiosk.getPermissions();
              if (perms != null && (
                  checkRelationshipPermission(perms, IKioskLink.TYPE_CUSTOMER, as, kiosk, kioskId)
                      || checkRelationshipPermission(perms, IKioskLink.TYPE_VENDOR, as, kiosk,
                      kioskId))) {
                return true;
              }
            }
          }
        } catch (Exception e) {
          xLogger.warn("Failed to check user permissions on linked kiosks " + e.getMessage(), e);
        }
      }
      return false;
    } finally {
      try {
        q.closeAll();
      } catch (Exception ignored) {
        xLogger.warn("Exception while closing query", ignored);
      }
      pm.close();
      xLogger.fine("Exiting hasKiosk");
    }
  }

  private boolean checkRelationshipPermission(Permissions perms, String relationshipType,
                                              UsersService as, IKiosk k, Long kioskId)
      throws ServiceException {
    if (perms.hasAccess(relationshipType, Permissions.MASTER, Permissions.OP_MANAGE)) {
      List<IKioskLink>
          kls =
          getKioskLinks(k.getKioskId(), relationshipType, null, null, null).getResults();
      for (IKioskLink kl : kls) {
        if (kl.getLinkedKioskId().equals(kioskId)) {
          return true;
        }
      }
    }
    return false;
  }

  /**
   * Get users associated with a given kiosk
   */
  public Results getUsersForKiosk(Long kioskId, PageParams pageParams) throws ServiceException {
    PersistenceManager pm = PMF.get().getPersistenceManager();
    try {
      return getUsersForKiosk(kioskId, pageParams, pm);
    } finally {
      pm.close();
    }
  }

  @SuppressWarnings("unchecked")
  public Results getUsersForKiosk(Long kioskId, PageParams pageParams, PersistenceManager pm)
      throws ServiceException {
    xLogger.fine("Entered getUsersForKiosk");
    List<IUserAccount> users = null;
    String cursor = null;
    Query userToKioskQuery = pm.newQuery(UserToKiosk.class);
    userToKioskQuery.setFilter("kioskId == kioskIdParam");
    userToKioskQuery.declareParameters("Long kioskIdParam");
    if (pageParams != null) {
      QueryUtil.setPageParams(userToKioskQuery, pageParams);
    }
    // Get the list of users associated with this kiosk
    try {
      List<IUserToKiosk> results = (List<IUserToKiosk>) userToKioskQuery.execute(kioskId);
      if (results != null && !results.isEmpty()) {
        cursor = QueryUtil.getCursor(results);
        users = new ArrayList<IUserAccount>();
        Iterator<IUserToKiosk> it = results.iterator();
        while (it.hasNext()) {
          IUserToKiosk u2k = it.next();
          try {
            IUserAccount user = JDOUtils.getObjectById(IUserAccount.class, u2k.getUserId(), pm);
            user = pm.detachCopy(user);
            users.add(user);
          } catch (JDOObjectNotFoundException e) {
            xLogger.warn("User {0} not found when getting users for kiosk {1}", u2k.getUserId(),
                kioskId);
          }
        }
      }
    } finally {
      userToKioskQuery.closeAll();
    }
    xLogger.fine("Exiting getUsersForKiosk");
    return new Results(users, cursor);
  }

  /**
   * Add a new kiosk in a given domain
   */
  @SuppressWarnings("unchecked")
  public Long addKiosk(Long domainId, IKiosk kiosk) throws ServiceException {
    xLogger.fine("Entering addKiosk: domain = {0}, kiosk = {1}", domainId, kiosk.getName());
    if (domainId == null || kiosk == null || kiosk.getName() == null || kiosk.getUser() == null) {
      throw new ServiceException("Invalid parameters for kiosk");
    }
    String errMsg = null;
    //Assuming that all fields except timeStamp are set by the calling function
    //Set the timeStamp to now
    Date now = new Date();
    kiosk.setTimeStamp(now);
    kiosk.setLastUpdated(now);
    kiosk.setDomainId(domainId);
    String sUserId = kiosk.getRegisteredBy();
    // Trim extra spaces if any in kiosk name
    kiosk.setName(StringUtil.getTrimmedName(kiosk.getName()));
    PersistenceManager pm = PMF.get().getPersistenceManager();
    try {
      // Check if a kiosk by this name already exists??
      Query query = pm.newQuery(JDOUtils.getImplClass(IKiosk.class));
      query.setFilter("dId.contains(domainIdParam) && nName == nameParam");
      query.declareParameters("Long domainIdParam, String nameParam");
      List<IKiosk> results = null;
      try {
        results = (List<IKiosk>) query.execute(domainId, kiosk.getName().toLowerCase());
        results
            .size(); // hack to ensure data is retrieved before PM is closed; TODO later figure out the appropriate mechanism to address this
        results = (List<IKiosk>) pm.detachCopyAll(results);
      } finally {
        query.closeAll();
      }
      if (results == null || results.size() == 0) {
        // Kiosk by this name does NOT exist; so save this
        // Check if custom ID is specified for the kiosk. If yes, check if the specified custom ID already exists.
        boolean customIdExists = false;
        if (kiosk.getCustomId() != null && !kiosk.getCustomId().isEmpty()) {
          customIdExists = checkIfCustomIdExists(kiosk);
        }
        if (customIdExists) {
          // Custom ID already exists in the database!
          xLogger.warn("addKiosk: FAILED!! Cannot add kiosk {0}. Custom ID {1} already exists.",
              kiosk.getName(), kiosk.getCustomId());
          throw new ServiceException(
              backendMessages.getString("error.cannotadd") + "'" + kiosk.getName() + "'. "
                  + messages.getString("customid") + " " + kiosk.getCustomId() + " "
                  + backendMessages.getString("error.alreadyexists") + ".");
        }

        if (kiosk.getTags() != null) {
          kiosk.setTgs(tagDao.getTagsByNames(kiosk.getTags(), ITag.KIOSK_TAG));
        }
        // Add the kiosk to this domain and the parents of this domain (superdomains)
        kiosk =
            (IKiosk) DomainsUtil.addToDomain(kiosk, domainId,
                pm); /// earlier: kiosk = pm.makePersistent(kiosk); // TODO: Persist here only
        xLogger.info("addKiosk: adding kiosk {0}. KioskID is {1}", kiosk.getName(),
            kiosk.getKioskId());
        // Increment counter
        List<Long> domainIds = kiosk.getDomainIds();
        incrementKioskCounter(domainIds, 1, pm);

        //Now add all the user to kiosk mappings to the database
        //TODO: Check to see if the user exists?
        List<IUserAccount> users = (List<IUserAccount>) kiosk.getUsers();
        if (users != null) {
          for (IUserAccount user : users) {
            String userId = user.getUserId();
            IUserToKiosk mapping = JDOUtils.createInstance(IUserToKiosk.class);
            mapping.setKioskId(kiosk.getKioskId(), kiosk.getName());
            mapping.setUserId(userId);
            mapping.setDomainId(domainId);
            pm.makePersistent(mapping);
            // Increment the counter for this user's managed kiosks
            Counter.getUserToKioskCounter(domainId, userId).increment(1);
          }
        }
        try {
          EventPublisher.generate(domainId, IEvent.CREATED, null,
              JDOUtils.getImplClass(IKiosk.class).getName(), entityDao.getKeyString(kiosk), null);
        } catch (EventGenerationException e) {
          xLogger.warn(
              "Exception when generating event for kiosk-creation for kiosk {0} in domain {1}: {2}",
              kiosk.getKioskId(), domainId, e.getMessage());
        }
      } else {
        errMsg = kiosk.getName() + " " + backendMessages.getString("error.alreadyexists");
      }
    } catch (Exception e) {
      errMsg = e.getMessage();
    } finally {
      pm.close();
    }
    if (errMsg != null) {
      throw new ServiceException(errMsg);
    }
    // Update the reports configuration for this domain
    if (kiosk.getDomainIds() != null) {
      for (Long aLong : kiosk.getDomainIds()) {
        ReportsConfig rc = ReportsConfig.getInstance(aLong);
        rc.addFilterValues(kiosk.getDimensions());
        rc.store(); // stores if changes exist
      }
    }
    xLogger.fine("Exiting addKiosk");
    return kiosk.getKioskId();
  }

  public void updateKiosk(IKiosk kiosk, Long domainId) throws ServiceException {
    updateKiosk(kiosk, domainId, null);
  }

  /**
   * Update a kiosk (domain Id is expected to be set within the kiosk)
   */
  public void updateKiosk(IKiosk kiosk, Long domainId, String username) throws ServiceException {
    xLogger.fine("Entering updateKiosk");
    if (kiosk == null) {
      throw new IllegalArgumentException("Invalid kiosk object");
    }
    boolean updateDeviceTags = false;
    Exception exception = null;
    List<String> oldTags = null;
    String sUserId = StringUtils.isNotBlank(username) ? username : kiosk.getUpdatedBy();
    PersistenceManager pm = PMF.get().getPersistenceManager();
    try {
      if (!AppFactory.get().getAuthorizationService().authoriseUpdateKiosk(sUserId, domainId)) {
        throw new UnauthorizedException(backendMessages.getString("permission.denied"));
      }
      //First check if the kiosk already exists in the database
      IKiosk k = JDOUtils.getObjectById(IKiosk.class, kiosk.getKioskId(), pm);
      if (!domainId.equals(k.getDomainId())) {
        throw new ServiceException(
            backendMessages.getString("entity.updation.permission.denied") + " : " + k.getName());
      }
      xLogger.info("updateKiosk: Updating kiosk {0}", kiosk.getName());
      //If we get here, it means the kiosk exists
      kiosk.setName(StringUtil.getTrimmedName(kiosk.getName()));
      if (!k.getName().equals(kiosk.getName())) {
        // Kiosk name has changed
        k.setName(kiosk.getName());
      }

      if (!ObjectUtils.equals(k.getState(),kiosk.getState()) || !ObjectUtils.equals(k.getDistrict(),kiosk.getDistrict())
          || !ObjectUtils.equals(k.getTaluk(),kiosk.getTaluk())) {
        updateDeviceTags =
            true; // state, district and taluk are used as tags for temperature devices (which are modeled as inventory items here)
      }


      // Update kiosk
      k.setStreet(kiosk.getStreet());
      k.setCity(kiosk.getCity());
      k.setTaluk(kiosk.getTaluk());
      k.setDistrict(kiosk.getDistrict());
      k.setState(kiosk.getState());
      k.setCountry(kiosk.getCountry());
      k.setPinCode(kiosk.getPinCode());
      k.setVertical(kiosk.getVertical());
      k.setServiceLevel(kiosk.getServiceLevel());
      k.setInventoryModel(kiosk.getInventoryModel());
      k.setOrderingMode(kiosk.getOrderingMode());
      k.setLastUpdated(new Date());
      k.setUpdatedBy(kiosk.getUpdatedBy());
      k.setType(kiosk.getType());
      k.setLatitude(kiosk.getLatitude());
      k.setLongitude(kiosk.getLongitude());
      k.setGeoAccuracy(kiosk.getGeoAccuracy());
      k.setGeoError(kiosk.getGeoError());
      k.setOptimizationOn(kiosk.isOptimizationOn());
      k.setCurrency(kiosk.getCurrency());
      k.setTax(kiosk.getTax());
      k.setTaxId(kiosk.getTaxId());
      k.setPermissions(kiosk.getPermissions());
      k.setBatchMgmtEnabled(kiosk.isBatchMgmtEnabled());
      oldTags = k.getTags();
      k.setTgs(tagDao.getTagsByNames(kiosk.getTags(), ITag.KIOSK_TAG));
      k.setCustomerPerm(kiosk.getCustomerPerm());
      k.setVendorPerm(kiosk.getVendorPerm());

      // Update users for this kiosk, if necessary. First, check if the kiosk to user mappings have changed
      xLogger.fine("updateKiosk: Updating user kiosk mappings for kiosk {0}", kiosk.getName());
      updateUsersForKiosk(kiosk, pm);

      // Check if custom ID is specified for the kiosk. If yes, check if a kiosk with the specified custom ID already exists.
      boolean customIdExists = false;
      if (kiosk.getCustomId() != null && !kiosk.getCustomId().isEmpty() && !kiosk.getCustomId()
          .equals(k.getCustomId())) {
        customIdExists = checkIfCustomIdExists(kiosk);
      }
      if (customIdExists) {
        // Custom ID already exists in the database!
        xLogger.warn("updateKiosk: FAILED!! Cannot update kiosk {0}. Custom ID {1} already exists.",
            kiosk.getName(), kiosk.getCustomId());
        throw new ServiceException(
            backendMessages.getString("error.cannotupdate") + " '" + kiosk.getName() + "'. "
                + messages.getString("customid") + " " + kiosk.getCustomId() + " " + backendMessages
                .getString("error.alreadyexists") + ".");
      }
      k.setCustomId(kiosk.getCustomId());

      // Update device tags in Temp. Service
      if (updateDeviceTags) {
        AssetUtil.updateAssetTags(k.getKioskId(), getAssetTagsToRegister(k.getKioskId()));
      }
      // Update tags, if needed
      PersistenceManager tagsPm = PMF.get().getPersistenceManager();
      try {
        AppFactory.get().getDaoUtil().updateTags(k.getDomainIds(), oldTags, k.getTags(),
            TagUtil.TYPE_ENTITY,
            kiosk.getKioskId(), tagsPm);
      } finally {
        tagsPm.close();
      }
      try {
        EventPublisher.generate(domainId, IEvent.MODIFIED, null,
            JDOUtils.getImplClass(IKiosk.class).getName(), entityDao.getKeyString(kiosk), null);
      } catch (EventGenerationException e) {
        xLogger.warn(
            "Exception when generating event for kiosk-updation for kiosk {0} in domain {1}: {2}",
            kiosk.getKioskId(), kiosk.getDomainId(), e.getMessage());
      }
      pm.makePersistent(k);
    } catch (JDOObjectNotFoundException e) {
      xLogger.warn("updateKiosk: Kiosk {0} does not exist", kiosk.getKioskId());
      exception = e;
    } catch (Exception e) {
      xLogger.severe("{0} when updating kiosk {1}: {2}", e.getClass().getName(), kiosk.getKioskId(),
          e.getMessage(), e);
      exception = e;
    } finally {
      xLogger.fine("Exiting updateKiosk");
      pm.close();
    }

    if (exception != null) {
      throw new ServiceException(exception);
    }
    // Update the reports configuration for this domain
    if (kiosk.getDomainIds() != null) {
      for (Long aLong : kiosk.getDomainIds()) {
        ReportsConfig rc = ReportsConfig.getInstance(aLong);
        rc.addFilterValues(kiosk.getDimensions());
        rc.store(); // stores if changes exist
      }
    }
  }

  /**
   * Get a kiosk, given a unique kiosk Id (this is unique across domains)
   */
  public IKiosk getKiosk(Long kioskId) throws ServiceException {
    return getKiosk(kioskId, true);
  }

  @SuppressWarnings("unchecked")
  public List<IKiosk> getKiosksByIds(List<Long> kioskIds) throws ServiceException {
    List<IKiosk> kioskList = new ArrayList<IKiosk>(kioskIds.size());
    PersistenceManager pm = PMF.get().getPersistenceManager();
    try {
      for (Long kioskId : kioskIds) {
        IKiosk kiosk = JDOUtils.getObjectById(IKiosk.class, kioskId, pm);
        List<IUserAccount> users = getUsersForKiosk(kioskId, null, pm).getResults();
        if (users != null && !users.isEmpty()) {
          kiosk.setUsers(users);
        }
        kioskList.add(kiosk);
      }
    } finally {
      pm.close();
    }
    return kioskList;
  }

  @SuppressWarnings("unchecked")
  public IKiosk getKiosk(Long kioskId, boolean deep) throws ServiceException {
    xLogger.fine("Entering getKiosk");
    IKiosk kiosk = null;
    PersistenceManager pm = PMF.get().getPersistenceManager();
    try {
      //Get the kiosk object from the database
      kiosk = JDOUtils.getObjectById(IKiosk.class, kioskId, pm);
      if (!deep) {
        kiosk = pm.detachCopy(kiosk);
        return kiosk;
      }
      //If we get here, it means the kiosk exists
      //We also need to get the kiosk to user mappings and set them in the kiosk object
      List<IUserAccount> users = getUsersForKiosk(kioskId, null, pm).getResults();
      if (users != null && !users.isEmpty()) {
        kiosk.setUsers(users);
      }
      //We also need to get the kiosk to poolgroup mappings and set them in the kiosk object
      List<IPoolGroup> poolGroups = new ArrayList<IPoolGroup>();
      Query kioskToPoolGroupQuery = pm.newQuery(JDOUtils.getImplClass(IKioskToPoolGroup.class));
      kioskToPoolGroupQuery.setFilter("kioskId == kioskIdParam");
      kioskToPoolGroupQuery.declareParameters("Long kioskIdParam");
      try {
        List<IKioskToPoolGroup>
            results =
            (List<IKioskToPoolGroup>) kioskToPoolGroupQuery.execute(kioskId);
        for (IKioskToPoolGroup mapping : results) {
          try {
            IPoolGroup
                poolGroup =
                JDOUtils.getObjectById(IPoolGroup.class, mapping.getPoolGroupId(), pm);
            xLogger.fine("getKiosk: kiosk {0} is associated with poolgroup {1}", kioskId,
                mapping.getPoolGroupId());
            poolGroup = pm.detachCopy(poolGroup);
            poolGroups.add(poolGroup);
          } catch (JDOObjectNotFoundException e) {
            xLogger.fine("No associated pool group found with id {0}", mapping.getPoolGroupId());
          }
        }
        kiosk.setPoolGroups(poolGroups);
      } finally {
        kioskToPoolGroupQuery.closeAll();
      }
    } catch (JDOObjectNotFoundException e) {
      xLogger.warn("getKiosk: Kiosk {0} does not exist", kioskId);
      throw new ServiceException(messages.getString("kiosk") + " " + kioskId + " " + backendMessages
          .getString("error.notfound"), e);
    } catch (Exception e) {
      throw new ServiceException(e.getMessage(), e);
    } finally {
      pm.close();
    }

    xLogger.fine("Exiting getKiosk");
    return kiosk;
  }

  /**
   * Delete a set of kiosks, given their ids
   */
  public void deleteKiosks(Long domainId, List<Long> kioskIds, String sUserId)
      throws ServiceException {
    xLogger.fine("Entering deleteKiosks");

    PersistenceManager pm = PMF.get().getPersistenceManager();
    PersistenceManager tagsPm = PMF.get().getPersistenceManager();
    DomainConfig dc = DomainConfig.getInstance(domainId);
    Locale locale = dc.getLocale();
    if (locale == null) {
      locale = new Locale(Constants.LANG_DEFAULT, Constants.COUNTRY_DEFAULT);
    }
    String timezone = dc.getTimezone();
    try {
      if (!AppFactory.get().getAuthorizationService().authoriseUpdateKiosk(sUserId, domainId)) {
        throw new UnauthorizedException(backendMessages.getString("permission.denied"));
      }
      List<IKiosk> kiosks = new ArrayList<IKiosk>(kioskIds.size());
      List<String> sdFailedKiosks = new ArrayList<>(1);
      for (Long kioskId : kioskIds) {
        try {
          IKiosk kiosk = JDOUtils.getObjectById(IKiosk.class, kioskId, pm);
          if (domainId.equals(kiosk.getDomainId())) {
            kiosks.add(kiosk);
          } else {
            sdFailedKiosks.add(kioskId.toString());
          }
        } catch (JDOObjectNotFoundException e) {
          xLogger.warn("Kiosk with id {0} not found. Ignoring it.", kioskId);
        }
      }
      if (!sdFailedKiosks.isEmpty()) {
        throw new ServiceException(
            backendMessages.getString("entity.deletion.permission.denied") + " : " + StringUtil
                .getCSV(sdFailedKiosks));
      }
      for (Long kioskId : kioskIds) {
        try {
          IKiosk kiosk = JDOUtils.getObjectById(IKiosk.class, kioskId, pm);
          xLogger.fine("Deleting kiosk...{0} with id {1}", kiosk.getName(), kioskId);
          // Delete assets relation with this entity
          AssetUtil.removeKioskLink(kioskId);
          // Delete tags associated with this entity, if any
//					TagUtil.decrementTagCounts( kiosk.getDomainIds(), kiosk.getTags(), TagUtil.TYPE_ENTITY, kioskId, tagsPm );
          // Delete all the related entities
          EntityRemover
              .removeRelatedEntities(domainId, JDOUtils.getImplClass(IKiosk.class).getName(),
                  kioskId, false);
          // Generate event, if configured
          try {
            EventPublisher
                .generate(domainId, IEvent.DELETED, null,
                    JDOUtils.getImplClass(IKiosk.class).getName(), entityDao.getKeyString(kiosk),
                    null, kiosk);
          } catch (EventGenerationException e) {
            xLogger.warn(
                "Exception when generating event for kiosk-deletion for kiosk {0} in domain {1}: {2}",
                kioskId, domainId, e.getMessage());
          }
          kiosks.add(kiosk);
        } catch (JDOObjectNotFoundException e) {
          xLogger.warn("Kiosk with id {0} not found. Ignoring it.", kioskId);
        }
      }

      pm.deletePersistentAll(kiosks);
    } catch (Exception e) {
      throw new ServiceException(e);
    } finally {
      try {
        pm.close();
      } catch (Exception ignored) {
        xLogger.warn("Exception while closing pm", ignored);
      }
      tagsPm.close();
    }

    xLogger.fine("Exiting deleteKiosks");
  }

  @SuppressWarnings("unchecked")
  public List<IKiosk> findKiosks(Long domainId, String paramName, String paramValue)
      throws ServiceException {
    xLogger.fine("Entering findKiosks");

    if (domainId == null || paramName == null || paramName.isEmpty()) {
      throw new ServiceException("Invalid domain Id or parameter name");
    }

    PersistenceManager pm = PMF.get().getPersistenceManager();
    List<IKiosk> results = null;
    try {
      if (JsonTagsZ.NAME.equalsIgnoreCase(paramName)) {
        Query kioskQuery = pm.newQuery(JDOUtils.getImplClass(IKiosk.class));
        kioskQuery.setFilter("dId.contains(domainIdParam) && nName == nameParam");
        kioskQuery.declareParameters("Long domainIdParam, String nameParam");
        try {
          results = (List<IKiosk>) kioskQuery.execute(domainId, paramValue.toLowerCase());
          results = (List<IKiosk>) pm.detachCopyAll(results);
        } finally {
          kioskQuery.closeAll();
        }
      } else if (JsonTagsZ.COUNTRY.equalsIgnoreCase(paramName)) {
        Query kioskQuery = pm.newQuery(JDOUtils.getImplClass(IKiosk.class));
        kioskQuery.setFilter("dId.contains(domainIdParam), country == countryParam");
        kioskQuery.declareParameters("Long domainIdParam, String countryParam");
        try {
          results = (List<IKiosk>) kioskQuery.execute(domainId, paramValue);
          results = (List<IKiosk>) pm.detachCopyAll(results);
        } finally {
          kioskQuery.closeAll();
        }
      } else if (JsonTagsZ.STATE.equalsIgnoreCase(paramName)) {
        Query kioskQuery = pm.newQuery(JDOUtils.getImplClass(IKiosk.class));
        kioskQuery.setFilter("dId.contains(domainIdParam) && state == stateParam");
        kioskQuery.declareParameters("Long domainIdParam, String stateParam");
        try {
          results = (List<IKiosk>) kioskQuery.execute(domainId, paramValue);
          results = (List<IKiosk>) pm.detachCopyAll(results);
        } finally {
          kioskQuery.closeAll();
        }
      } else if (JsonTagsZ.DISTRICT.equalsIgnoreCase(paramName)) {
        Query kioskQuery = pm.newQuery(JDOUtils.getImplClass(IKiosk.class));
        kioskQuery.setFilter("dId.contains(domainIdParam) && district == districtParam");
        kioskQuery.declareParameters("Long domainIdParam, String districtParam");
        try {
          results = (List<IKiosk>) kioskQuery.execute(domainId, paramValue);
          results = (List<IKiosk>) pm.detachCopyAll(results);
        } finally {
          kioskQuery.closeAll();
        }
      } else if (JsonTagsZ.TALUK.equalsIgnoreCase(paramName)) {
        Query kioskQuery = pm.newQuery(JDOUtils.getImplClass(IKiosk.class));
        kioskQuery.setFilter("dId.contains(domainIdParam) && taluk == talukParam");
        kioskQuery.declareParameters("Long domainIdParam, String talukParam");
        try {
          results = (List<IKiosk>) kioskQuery.execute(domainId, paramValue);
          results = (List<IKiosk>) pm.detachCopyAll(results);
        } finally {
          kioskQuery.closeAll();
        }
      } else if (JsonTagsZ.CITY.equalsIgnoreCase(paramName)) {
        Query kioskQuery = pm.newQuery(JDOUtils.getImplClass(IKiosk.class));
        kioskQuery.setFilter("dId.contains(domainIdParam) && city == cityParam");
        kioskQuery.declareParameters("Long domainIdParam, String cityParam");
        try {
          results = (List<IKiosk>) kioskQuery.execute(domainId, paramValue);
          results = (List<IKiosk>) pm.detachCopyAll(results);
        } finally {
          kioskQuery.closeAll();
        }
      } else if (JsonTagsZ.USER_ID.equalsIgnoreCase(paramName)) {
        Query userToKioskQuery = pm.newQuery(JDOUtils.getImplClass(IUserToKiosk.class));
        userToKioskQuery.setFilter("userId == userIdParam");
        userToKioskQuery.declareParameters("String userIdParam");
        try {
          results = new ArrayList<IKiosk>();
          List<IUserToKiosk> userToKioskQueryResults =
              (List<IUserToKiosk>) userToKioskQuery.execute(paramValue);
          for (IUserToKiosk mapping : userToKioskQueryResults) {
            Long kioskId = mapping.getKioskId();
            try {
              IKiosk kiosk = JDOUtils.getObjectById(IKiosk.class, kioskId, pm);
              kiosk = pm.detachCopy(kiosk);
              results.add(kiosk);
            } catch (JDOObjectNotFoundException e) {
              xLogger.warn("One of the kiosks {0} in the query could not be found in the database",
                  kioskId);
            }
          }
        } finally {
          userToKioskQuery.closeAll();
        }
      }
    } finally {
      pm.close();
    }

    xLogger.fine("Exiting findKiosks");
    return results;
  }

  /**
   * Find all kiosks with pagination
   */
  @SuppressWarnings("unchecked")
  public Results getAllKiosks(Long domainId, String tag, PageParams pageParams) {
    return entityDao.getAllKiosks(domainId, tag, pageParams);
  }

  public Results getAllDomainKiosks(Long domainId, String tag, PageParams pageParams) {
    return entityDao.getAllDomainKiosks(domainId, tag, pageParams);
  }

  public List<Long> getAllDomainKioskIds(Long domainId) {
    Results res = entityDao.getAllDomainKiosks(domainId, null, null);
    List<IKiosk> kiosks = res.getResults();
    if (kiosks == null) {
      return null;
    }
    List<Long> kioskIds = new ArrayList<>(kiosks.size());
    for (IKiosk k : kiosks) {
      kioskIds.add(k.getKioskId());
    }
    return kioskIds;
  }

  @SuppressWarnings("unchecked")
  @Override
  public List<Long> getAllKioskIds(Long domainId) {
    xLogger.fine("Entering getAllKioskIds");
    PersistenceManager pm = PMF.get().getPersistenceManager();
    List<Long> kioskIds = new ArrayList<Long>(0);
    try {
      String filter = "dId.contains(domainIdParam)";
      Map<String, Object> params = new HashMap<String, Object>(1);
      params.put("domainIdParam", domainId);
      Query
          query =
          pm.newQuery("SELECT kioskId FROM " + JDOUtils.getImplClass(IKiosk.class).getName());
      query.setFilter(filter);
      query.declareParameters("Long domainIdParam");
      try {
        kioskIds = (List<Long>) query.executeWithMap(params);
        return new ArrayList<>(kioskIds);
      } finally {
        query.closeAll();
      }
    } catch (Exception e) {
      xLogger.warn("Exception: {0}", e.getMessage(), e);
    } finally {
      pm.close();
    }
    xLogger.fine("Exiting getAllKioskIds");
    return kioskIds;
  }

  /**
   * Get kiosks accessible/visible to a given user
   */
  public Results getKiosks(IUserAccount user, Long domainId, String tag, PageParams pageParams)
      throws ServiceException { // TODO: pagination?
    xLogger.fine("Entered getKiosks");
    Results results = null;
    String role = user.getRole();
    if (SecurityUtil.compareRoles(role, SecurityConstants.ROLE_DOMAINOWNER) >= 0) {
      results = getAllKiosks(domainId, tag, pageParams);
    } else {
      List<IKiosk> kiosks = (List<IKiosk>) getKiosksForUser(user,null,null).getResults();
      // When user object is not fetched as deep, fetch all users kiosk here
      if (kiosks == null || kiosks.size() == 0) {
        Results kiosksForUser = getKiosksForUser(user, null, null);
        if (kiosksForUser == null) {
          return new Results(null, null);
        }
        kiosks = kiosksForUser.getResults();
      }
      if (StringUtils.isNotBlank(tag)) {
        List<String> tags;
        if (tag.contains(CharacterConstants.COMMA)) {
          tags = StringUtil.getList(tag, true);
        } else {
          tags = Collections.singletonList(tag);
        }
        Iterator<IKiosk> iter = kiosks.iterator();
        while (iter.hasNext()) {
          IKiosk k = iter.next();
          boolean found = false;
          for (String t : tags) {
            if (k.getTags().contains(t)) {
              found = true;
              break;
            }
          }
          if (!found) {
            iter.remove();
          }
        }
      }
      results = new Results(kiosks, null);
    }
    xLogger.fine("Existing getKiosks");
    return results;
  }

  /**
   * Get kiosks for a given user, with pagination
   */
  public Results getKiosksForUser(IUserAccount user, String routeTag, PageParams pageParams)
      throws ServiceException {
    PersistenceManager pm = PMF.get().getPersistenceManager();
    try {
      return getKiosksForUser(user, routeTag, pageParams, pm);
    } finally {
      pm.close();
    }
  }

  @SuppressWarnings("unchecked")
  private Results getKiosksForUser(IUserAccount user, String routeTag, PageParams pageParams,
                                   PersistenceManager pm) throws ServiceException {
    xLogger.fine("Entered getKiosks");
    if (user == null) {
      throw new IllegalArgumentException("Invalid user");
    }
    String
        query = "select UK.UserToKioskId as UserToKioskId, UK.* FROM USERTOKIOSK UK, KIOSK K where "
        + "UK.USERID = ? AND K.KIOSKID = UK.KIOSKID";
    List<String> parameters = new ArrayList<>(1);
    parameters.add(user.getUserId());
    if (StringUtils.isNotEmpty(routeTag)) {
      query += " AND UK.TG = ?";
      parameters.add(routeTag);
    }

    if(user.isRteEnabled() || routeTag != null){
      query += " ORDER BY UK.RI ASC";
    } else {
      query += " ORDER BY K.NAME ASC";
    }

    if (pageParams != null) {
      query  +=
          " LIMIT " + pageParams.getOffset() + CharacterConstants.COMMA + pageParams.getSize();
    }
    // Execute
    Query jdoQuery = pm.newQuery("javax.jdo.query.SQL", query);
    jdoQuery.setClass(UserToKiosk.class);
    List<UserToKiosk> results;
    List<IKiosk> kiosks = null;
    try {
      results = (List<UserToKiosk>) jdoQuery.executeWithArray(parameters.toArray());
      if (results != null && !results.isEmpty()) {
        kiosks = new ArrayList<>();
        for (IUserToKiosk uk : results) {
          try {
            IKiosk k = JDOUtils.getObjectById(IKiosk.class, uk.getKioskId(), pm);
            k = pm.detachCopy(k);
            k.setRouteIndex(uk.getRouteIndex());
            k.setRouteTag(uk.getTag());
            kiosks.add(k);
          } catch (JDOObjectNotFoundException e) {
            xLogger.warn("User's {0} kiosk {1} does not exist ", user.getUserId(), uk.getKioskId());
          }
        }
      }
    } finally {
      try {
        jdoQuery.closeAll();

      } catch (Exception ignored) {
        xLogger.warn("Exception while closing query", ignored);
      }
      pm.close();
    }
    return new Results(kiosks, null);
  }

  @Override
  public boolean hasAccessToKiosk(String userId, Long kioskId, Long domainId, String role) {
    Map<String, Object> params = new HashMap<>();
    params.put("kioskIdParam", kioskId);
    params.put("domainIdParam", domainId);
    String queryStr;
    if (SecurityUtil.compareRoles(role, SecurityConstants.ROLE_DOMAINOWNER) >= 0) {
      queryStr =
          "SELECT kioskId FROM " + JDOUtils.getImplClass(IKiosk.class).getName()
              + " WHERE kioskId == kioskIdParam && dId.contains(domainIdParam)" +
              " PARAMETERS Long kioskIdParam, Long domainIdParam";
    } else {
      queryStr =
          "SELECT kioskId FROM " + JDOUtils.getImplClass(IUserToKiosk.class).getName()
              + " WHERE userId == userIdParam && kioskId == kioskIdParam && dId == domainIdParam" +
              " PARAMETERS String userIdParam, Long kioskIdParam, Long domainIdParam";
      params.put("userIdParam", userId);
    }
    return getAccess(params, queryStr);
  }

  public Integer hasAccessToKiosk(String userId, Long kioskId) {
    PersistenceManager pm = PMF.get().getPersistenceManager();
    Query query = null;
    try {
      String
          qry =
          "SELECT IF(R = 'c,v' OR R = 'v,c',GREATEST(C, V),IF(R = 'c', C, V)) Perm FROM(SELECT (SELECT GROUP_CONCAT(KL.LINKTYPE) R FROM KIOSKLINK KL WHERE KL.LINKEDKIOSKID = ?"
              +
              " AND KL.KIOSKID = K.KIOSKID) R,K.CPERM C,K.VPERM V FROM KIOSK K, (SELECT KIOSKID FROM USERTOKIOSK UK WHERE KIOSKID IN (SELECT DISTINCT KL.LINKEDKIOSKID FROM KIOSKLINK KL WHERE KIOSKID = ?)"
              +
              " AND USERID = ?) UK WHERE K.KIOSKID = UK.KIOSKID AND (K.CPERM > 0 OR K.VPERM > 0)) PERM limit 1";
      query = pm.newQuery("javax.jdo.query.SQL", qry);
      query.setUnique(true);
      Integer hasAccess = (Integer) query.executeWithArray(kioskId, kioskId, userId);
      if (hasAccess != null && hasAccess > 0) {
        return hasAccess;
      } else {
        return 0;
      }
    } finally {
      if (query != null) {
        try {
          query.closeAll();
        } catch (Exception ignored) {
          xLogger.warn("Exception while closing query", ignored);
        }
      }
      pm.close();
    }
  }

  @Override
  public boolean hasAccessToUser(String userId, String rUserId, Long domainId, String role) {
    Map<String, Object> params = new HashMap<>(2);
    params.put("domainIdParam", domainId);
    params.put("userIdParam", userId);
    String queryStr;
    if (userId != null && userId.equals(rUserId)) {
      return true;
    } else if (SecurityUtil.compareRoles(role, SecurityConstants.ROLE_DOMAINOWNER) >= 0) {
      //Todo: Need to check user domainId and all its children domains
      queryStr =
          "SELECT userId FROM " + JDOUtils.getImplClass(IUserAccount.class).getName()
              + " WHERE userId == userIdParam && dId.contains(domainIdParam)" +
              " PARAMETERS String userIdParam, Long domainIdParam";
    } else {
      queryStr =
          "SELECT userId FROM " + JDOUtils.getImplClass(IUserAccount.class).getName()
              + " WHERE userId == userIdParam && registeredBy == registeredByParam && sdId == domainIdParam"
              +
              " PARAMETERS String userIdParam, String registeredByParam, Long domainIdParam";
      params.put("registeredByParam", rUserId);
    }
    return getAccess(params, queryStr);
  }
    
	/* (non-Javadoc)
         * @see org.lggi.samaanguru.service.AccountsService#addPoolGroup(org.lggi.samaanguru.entity.PoolGroup)
	 */

  private boolean getAccess(Map<String, Object> params, String queryStr) {
    PersistenceManager pm = PMF.get().getPersistenceManager();
    boolean hasAccess = false;
    List<Long> results;
    Query q = null;
    try {
      q = pm.newQuery(queryStr);
      results = (List<Long>) q.executeWithMap(params);
      if (results != null && results.size() == 1) {
        hasAccess = true;
      }
    } finally {
      if (q != null) {
        try {
          q.closeAll();
        } catch (Exception ignored) {
          xLogger.warn("Exception while closing query", ignored);
        }
      }
      pm.close();
    }
    return hasAccess;
  }

  // Get the kiosk IDs for a given user (and/or route tag)
  @SuppressWarnings("unchecked")
  public Results getKioskIdsForUser(String userId, String routeTag, PageParams pageParams)
      throws ServiceException {
    String
        query;
    List<String> parameters = new ArrayList<>(1);
    if (routeTag != null && !routeTag.isEmpty()) {
      query = "select UK.KIOSKID FROM USERTOKIOSK UK, KIOSK K, KIOSK_TAGS KT where "
          + "UK.KIOSKID = KT.KIOSKID AND UK.USERID = ? AND KT.ID = ? AND K.KIOSKID = UK.KIOSKID";
      parameters.add(userId);
      parameters.add(String.valueOf(tagDao.getTagByName(routeTag, ITag.KIOSK_TAG)));
    }else{
      query = "select UK.KIOSKID FROM USERTOKIOSK UK, KIOSK K where UK.USERID = ?"
          + " AND K.KIOSKID = UK.KIOSKID";
      parameters.add(userId);
    }
    query += " ORDER BY K.NAME ASC";
    if (pageParams != null) {
      query  +=
          " LIMIT " + pageParams.getOffset() + CharacterConstants.COMMA + pageParams.getSize();
    }
    // Execute
    PersistenceManager pm = PMF.get().getPersistenceManager();
    Query jdoQuery = pm.newQuery("javax.jdo.query.SQL", query);
    List<Long> results = null;
    String cursor = null;
    try {
      results = (List<Long>) jdoQuery.executeWithArray(parameters.toArray());
      if (results != null) {
        results.size();
        cursor = QueryUtil.getCursor(results);
        List<Long> respResults = new ArrayList<>(results.size());
        for (Long result : results) {
          respResults.add(result);
        }
        results = respResults;
      }
    } finally {
      try {
        jdoQuery.closeAll();

      } catch (Exception ignored) {
        xLogger.warn("Exception while closing query", ignored);
      }
      pm.close();
    }
    return new Results(results, cursor);
  }

  public Long addPoolGroup(Long domainId, IPoolGroup group) throws ServiceException {
    xLogger.fine("Entering addPoolGroup");

    if (domainId == null || group == null) {
      throw new ServiceException("Invalid domain ID or pool group");
    }

    String errMsg = null;
    Exception exception = null;
    // Assuming that all fields except timeStamp are set by the calling function
    // Set the timestamp to now
    Date now = new Date();
    group.setTimeStamp(now);
    group.setUpdatedOn(now);
    group.setDomainId(domainId);
    PersistenceManager pm = PMF.get().getPersistenceManager();
    try {
      //We don't check if the group already exists, because the key is auto generated
      //TODO: Check a group's existence by its name??
      group = pm.makePersistent(group);
      //Now add all the poolgroup to kiosk mappings to the database
      //TODO: Check to see if the kiosk exists?
      List<IKiosk> kiosks = (List<IKiosk>) group.getKiosks();
      for (IKiosk kiosk : kiosks) {
        IKioskToPoolGroup mapping = JDOUtils.createInstance(IKioskToPoolGroup.class);
        mapping.setKioskId(kiosk.getKioskId());
        mapping.setPoolGroupId(group.getGroupId());
        mapping.setDomainId(domainId);
        pm.makePersistent(mapping);
      }
      try {
        EventPublisher.generate(domainId, IEvent.CREATED, null,
            JDOUtils.getImplClass(IPoolGroup.class).getName(), entityDao.getKeyString(group), null);
      } catch (EventGenerationException e) {
        xLogger.warn(
            "Exception when generating event for poolgroup-creation for poolgroup {0} in domain {1}: {2}",
            group.getGroupId(), domainId, e.getMessage());
      }
    } catch (Exception e) {
      errMsg = e.getMessage();
      exception = e;
    } finally {
      pm.close();
    }
    if (errMsg != null) {
      throw new ServiceException(errMsg, exception);
    }
    xLogger.fine("Exiting addPoolGroup");
    return group.getGroupId();
  }

  /**
   * Update a given pool group (the domain ID is expected to be part of the group object)
   */
  @SuppressWarnings("unchecked")
  public void updatePoolGroup(IPoolGroup group) throws ServiceException {
    xLogger.fine("Entering updatePoolGroup");
    Long domainId = group.getDomainId();
    if (domainId == null) {
      throw new ServiceException("Invalid domain for pool group");
    }

    boolean poolgroupExists = true;
    String errMsg = null;
    Exception exception = null;
    PersistenceManager pm = PMF.get().getPersistenceManager();
    try {
      //First check if the poolgroup already exists in the database
      IPoolGroup pg = JDOUtils.getObjectById(IPoolGroup.class, group.getGroupId(), pm);
      //If we get here, it means the poolgroup exists
      //TODO: Update only those fields which have changed
      pg.setName(group.getName());
      pg.setDescription(group.getDescription());
      pg.setOwnerId(group.getOwnerId());
      pg.setUpdatedBy(group.getUpdatedBy());
      pg.setUpdatedOn(new Date());
      //pg.setCreatedBy(group.getCreatedBy());
      //check if the kiosk to poolgroup mappings have changed
      List<IKiosk> kiosks = (List<IKiosk>) group.getKiosks();
      //Create a list of kioskIds from the list of users
      List<Long> kioskIds = new ArrayList<Long>();
      for (IKiosk kiosk : kiosks) {
        kioskIds.add(kiosk.getKioskId());
      }
      Query query = pm.newQuery(JDOUtils.getImplClass(IKioskToPoolGroup.class));
      query.setFilter("poolGroupId == poolGroupIdParam");
      query.declareParameters("Long poolGroupIdParam");
      List<Long> queryKioskIds = new ArrayList<Long>();
      try {
        List<IKioskToPoolGroup> results = (List<IKioskToPoolGroup>)
            query.execute(group.getGroupId());
        for (IKioskToPoolGroup mapping : results) {
          queryKioskIds.add(mapping.getKioskId());
          if (!kioskIds.contains(mapping.getKioskId())) {
            //remove it
            xLogger.fine("updatePoolGroup: Deleting mapping between poolGroup {0} and kiosk {1}",
                group.getGroupId(), mapping.getKioskId());
            pm.deletePersistent(mapping);
          }
        }
        for (Long kioskId : kioskIds) {
          if (!queryKioskIds.contains(kioskId)) {
            //add it
            IKioskToPoolGroup mapping = JDOUtils.createInstance(IKioskToPoolGroup.class);
            mapping.setKioskId(kioskId);
            mapping.setPoolGroupId(group.getGroupId());
            mapping.setDomainId(domainId);
            xLogger.fine("updatePoolGroup: Adding mapping between poolGroup {0} and kiosk {1}",
                group.getGroupId(), mapping.getKioskId());
            pm.makePersistent(mapping);
          }
        }
      } finally {
        query.closeAll();
      }

      try {
        EventPublisher.generate(domainId, IEvent.MODIFIED, null,
            JDOUtils.getImplClass(IPoolGroup.class).getName(), entityDao.getKeyString(group), null);
      } catch (EventGenerationException e) {
        xLogger.warn(
            "Exception when generating event for poolgroup-updating for poolgroup {0} in domain {1}: {2}",
            group.getGroupId(), group.getDomainId(), e.getMessage());
      }
    } catch (JDOObjectNotFoundException e) {
      poolgroupExists = false;
      exception = e;
      xLogger.warn("updatePoolGroup: FAILED!! PoolGroup {0} does not exist", group.getGroupId());
    } catch (Exception e) {
      errMsg = e.getMessage();
      exception = e;
    } finally {
      pm.close();
    }
    xLogger.fine("Exiting updatePoolGroup");
    if (poolgroupExists == false) {
      errMsg = messages.getString("poolgroup") + " " + backendMessages.getString("error.notfound");
    }
    if (errMsg != null) {
      throw new ServiceException(errMsg, exception);
    }
  }

  /**
   * Get a pool group, given its ID (this ID is unique across domains, so domain ID is not needed)
   */
  @SuppressWarnings("unchecked")
  public IPoolGroup getPoolGroup(Long groupId) throws ServiceException {
    xLogger.fine("Entering getPoolGroup");
    IPoolGroup group = null;
    String errMsg = null;
    Exception exception = null;
    PersistenceManager pm = PMF.get().getPersistenceManager();
    try {
      //Get the poolgroup object from the database
      group = JDOUtils.getObjectById(IPoolGroup.class, groupId, pm);
      xLogger.info("getPoolGroup: Getting info for poolGroup {0}", groupId);
      //If we get here, it means the poolgroup exists
      //We also need to get the poolgroup to kiosk mappings
      //and set them in the poolgroup object
      xLogger.fine("Getting kiosks associated with the poolgroup {0}", groupId);
      List<IKiosk> kiosks = new ArrayList<IKiosk>();
      Query kioskToPoolGroupQuery = pm.newQuery(JDOUtils.getImplClass(IKioskToPoolGroup.class));
      kioskToPoolGroupQuery.setFilter("poolGroupId == poolGroupIdParam");
      kioskToPoolGroupQuery.declareParameters("Long poolGroupIdParam");
      try {
        List<IKioskToPoolGroup>
            results =
            (List<IKioskToPoolGroup>) kioskToPoolGroupQuery.execute(groupId);
        for (IKioskToPoolGroup mapping : results) {
          IKiosk kiosk = JDOUtils.getObjectById(IKiosk.class, mapping.getKioskId(), pm);
          xLogger.fine("getPoolGroup: Adding mapping between poolGroup {0} and kiosk {1}", groupId,
              mapping.getKioskId());
          kiosks.add(kiosk);
        }
        group.setKiosks(kiosks);
      } finally {
        kioskToPoolGroupQuery.closeAll();
      }
      //We also need to get the owner's location info and set it
      //in the poolgroup object before returning it
      IUserAccount owner = JDOUtils.getObjectById(IUserAccount.class, group.getOwnerId(), pm);
      group.setStreet(owner.getStreet());
      group.setCity(owner.getCity());
      group.setTaluk(owner.getTaluk());
      group.setDistrict(owner.getDistrict());
      group.setState(owner.getState());
      group.setCountry(owner.getCountry());
      group.setPinCode(owner.getPinCode());
    } catch (JDOObjectNotFoundException e) {
      xLogger.warn("getPoolGroup: FAILED!! PoolGroup {0} does not exist", groupId);
      errMsg = messages.getString("poolgroup") + " " + backendMessages.getString("error.notfound");
      exception = e;
    } catch (Exception e) {
      errMsg = e.getMessage();
      exception = e;
    } finally {
      pm.close();
    }
    if (errMsg != null) {
      throw new ServiceException(errMsg, exception);
    }

    xLogger.fine("Exiting getPoolGroup");
    return group;
  }

  /**
   * Delete a set of pool groups, given their IDs
   */
  public void deletePoolGroups(Long domainId, List<Long> groupIds) throws ServiceException {
    xLogger.fine("Entering deletePoolGroups");
    IPoolGroup poolGroup;
    PersistenceManager pm = PMF.get().getPersistenceManager();
    DomainConfig dc = DomainConfig.getInstance(domainId);
    Locale locale = dc.getLocale();
    if (locale == null) {
      locale = new Locale(Constants.LANG_DEFAULT, Constants.COUNTRY_DEFAULT);
    }
    String timezone = dc.getTimezone();
    try {
      for (Long poolGroupId : groupIds) {
        xLogger.fine("Deleting pool group {0}...", poolGroupId);
        try {
          poolGroup = JDOUtils.getObjectById(IPoolGroup.class, poolGroupId, pm);
          // Delete all related entities
          EntityRemover
              .removeRelatedEntities(domainId, JDOUtils.getImplClass(IPoolGroup.class).getName(),
                  poolGroupId, false);
          // Generate event, if configured
          try {
            EventPublisher
                .generate(domainId, IEvent.DELETED, null,
                    JDOUtils.getImplClass(IPoolGroup.class).getName(),
                    entityDao.getKeyString(poolGroup), null, poolGroup);
          } catch (EventGenerationException e) {
            xLogger.warn(
                "Exception when generating event for poolgroup-deletion for poolgroup {0} in domain {1}: {2}",
                poolGroupId, domainId, e.getMessage());
          }
          // Delete the pool group
          pm.deletePersistent(poolGroup);
        } catch (JDOObjectNotFoundException e) {
          xLogger.warn("Pool group with id {0} not found. Ignoring it.", poolGroupId);
        }
      }
    } catch (Exception e) {
      throw new ServiceException(e);
    } finally {
      // Close PM
      pm.close();
    }
  }

  /**
   * Find all pool groups with pagination, in a given domain
   * TODO: Pagination
   */
  @SuppressWarnings("unchecked")
  public List<IPoolGroup> findAllPoolGroups(Long domainId, int pageNumber, int numOfEntries) {
    xLogger.fine("Entering findAllPooLGroups");
    PersistenceManager pm = PMF.get().getPersistenceManager();
    List<IPoolGroup> poolGroups = new ArrayList<IPoolGroup>();

    // Formulate query
    Query query = pm.newQuery(JDOUtils.getImplClass(IPoolGroup.class));
    query.setFilter("dId == domainIdParam");
    query.declareParameters("Long domainIdParam");
    query.setOrdering("name asc");
    // Execute query
    try {
      poolGroups = (List<IPoolGroup>) query.execute(domainId);
      poolGroups
          .size(); // TODO - temp. fix for retrieving all obejcts and avoid "object manager closed" exception
      poolGroups = (List<IPoolGroup>) pm.detachCopyAll(poolGroups);
    } catch (Exception e) {
      xLogger.warn("Exception: {0}", e.getMessage());
    } finally {
      try {
        query.closeAll();
      } catch (Exception ignored) {
        xLogger.warn("Exception while closing query", ignored);
      }
      pm.close();
    }

    xLogger.fine("Exiting findAllPooLGroups");
    return poolGroups;
  }

  /**
   * Get poolgroup Ids that a given kiosk is in
   */
  @SuppressWarnings("unchecked")
  public List<Long> getPoolGroupIds(Long kioskId) throws ServiceException {
    xLogger.fine("Entered getPoolGroupIds");
    List<Long> pids = new ArrayList<Long>();
    // Get PM
    PersistenceManager pm = PMF.get().getPersistenceManager();
    // Form query
    Query q = pm.newQuery(JDOUtils.getImplClass(IKioskToPoolGroup.class));
    q.setFilter("kioskId == kioskIdParam");
    q.declareParameters("Long kioskIdParam");
    try {
      List<IKioskToPoolGroup> results = (List<IKioskToPoolGroup>) q.execute(kioskId);
      // Get the pids
      for (IKioskToPoolGroup result : results) {
        pids.add(result.getPoolGroupId());
      }
    } finally {
      try {
        q.closeAll();
      } catch (Exception ignored) {
        xLogger.warn("Exception while closing query", ignored);
      }
      pm.close();
    }

    xLogger.fine("Exiting getPoolGroupIds");
    return pids;
  }

  // Supply-chain relationship (link) creation and management
  private String addKioskLink(Long domainId, IKioskLink link, PersistenceManager pm)
      throws ServiceException {
    xLogger.fine("Entering addKioskLink");
    if (link == null) {
      throw new ServiceException("No link to add");
    }
    // Set the timeStamp to now
    if (link.getCreatedOn() == null) {
      link.setCreatedOn(new Date());
    }
    if (link.getDomainId() == null) {
      link.setDomainId(domainId);
    }
    String
        linkId =
        JDOUtils.createKioskLinkId(link.getKioskId(), link.getLinkType(), link.getLinkedKioskId());
    xLogger.fine("linkId: {0}", linkId);
    // Check if this link already exists, if so, do not add
    try {
      JDOUtils.getObjectById(IKioskLink.class, linkId, pm);
      // Ooops...this link already exists
      throw new ServiceException("Relationship already exists");
    } catch (JDOObjectNotFoundException e) {
      // Good. This link does not exist
    }
    // Set the key, if not set
    if (link.getId() == null) {
      link.setId(linkId);
    }
    // Get the kiosk and linked kiosk names, and add them to the reverse-link and link respectively (to facilitate indexing)
    Long linkedKioskId = link.getLinkedKioskId();
    String kioskName = link.getKioskName();
    Long linkedKioskDomainId = null;
    try {
      // Get linked kiosk name
      IKiosk k = JDOUtils.getObjectById(IKiosk.class, linkedKioskId, pm);
      link.setLinkedKioskName(k.getName());
      linkedKioskDomainId = k.getDomainId();
      // Get the kiosk name, if not supplied via link
      if (kioskName == null) {
        k = JDOUtils.getObjectById(IKiosk.class, link.getKioskId(), pm);
        kioskName = k.getName();
      }
    } catch (Exception e) {
      xLogger
          .warn("{0} when getting the linked kiosk {1} for kiosk {2}: {3}", e.getClass().getName(),
              linkedKioskId, link.getKioskId(), e.getMessage());
    }
    // Create a reverse link
    IKioskLink reverseLink = JDOUtils.createInstance(IKioskLink.class);
    reverseLink.setCreatedBy(link.getCreatedBy());
    reverseLink.setCreatedOn(link.getCreatedOn());
    reverseLink.setDescription(link.getDescription());
    reverseLink.setId(JDOUtils.createKioskLinkId(link.getLinkedKioskId(),
        EntityUtils.getReverseLinkType(link.getLinkType()), link.getKioskId()));
    reverseLink.setKioskId(linkedKioskId);
    reverseLink.setLinkedKioskId(link.getKioskId());
    if (kioskName != null) {
      reverseLink.setLinkedKioskName(kioskName);
    }
    reverseLink.setLinkType(EntityUtils.getReverseLinkType(link.getLinkType()));
    reverseLink.setCreditLimit(link.getCreditLimit());
    reverseLink.setDomainId(linkedKioskDomainId);
    String errMsg = null;
    Exception exception = null;
    // Store the kiosk links
    try {
      // Add the links to the domain (superdomains)
      DomainsUtil.addToDomain(link, domainId, pm); /// earlier: pm.makePersistentAll( links );
      DomainsUtil.addToDomain(reverseLink, domainId, pm);
    } catch (Exception e) {
      exception = e;
      errMsg = e.getMessage();
    } finally {
      if (errMsg != null) {
        throw new ServiceException(errMsg, exception);
      }
    }

    xLogger.fine("Exiting addKioskLink");
    return link.getId();
  }

  public List<String> addKioskLinks(Long domainId, List<IKioskLink> links) throws ServiceException {
    xLogger.fine("Entering addKioskLink");
    if (links == null || links.isEmpty()) {
      throw new ServiceException("No links to add");
    }
    PersistenceManager pm = PMF.get().getPersistenceManager();
    // Set the timeStamp to now
    List<String> linkIds = new ArrayList<>(1);
    try {
      for (IKioskLink link : links) {
        try {
          addKioskLink(domainId, link, pm);
          linkIds.add(link.getId());
        } catch (ServiceException e) {
          xLogger.warn("ServiceException when adding kiosk link {0}: {1}", link.getId(),
              e.getMessage());
        }
      }
    } finally {
      pm.close();
    }
    xLogger.fine("Exiting addKioskLink");
    return linkIds;
  }

  /**
   * Get the kiosk links for a given kiosk and specific type of link (e.g. VENDOR, CUSTOMER)
   *
   * @param kioskId  ID of kiosk of interest
   * @param linkType The type of link of interest (e.g. VENDOR, CUSTOMER)
   * @return A list of KioskLink objects
   */
  @SuppressWarnings("unchecked")
  public Results getKioskLinks(Long kioskId, String linkType, String routeTag,
                               String startsWith, PageParams pageParams) throws ServiceException {
    xLogger.fine("Entering getKioskLinks");
    List<IKioskLink> links = null;
    String cursor = null;
    if (kioskId == null) {
      throw new ServiceException("Invalid kiosk Id");
    }
    boolean hasRouteTag = routeTag != null && !routeTag.isEmpty();
    PersistenceManager pm = PMF.get().getPersistenceManager();
    int count = 0;
    try {
      boolean isRteEnabled;
      // Get the Kiosk object for the specified kiosk id
      IKiosk k = JDOUtils.getObjectById(IKiosk.class, kioskId, pm);
      isRteEnabled = k.isRouteEnabled(linkType);
      String sqlQuery;
      String orderBy;
      String limitStr = null;
      List<String> parameters = new ArrayList<>(2);
      parameters.add(String.valueOf(kioskId));
      parameters.add(linkType);
      if (isRteEnabled || hasRouteTag) {
        sqlQuery = "select KL.*,KL.`KEY` as `KEY` from KIOSKLINK KL where KL.KIOSKID = ? and "
            + "KL.LINKTYPE = ?";
        if (hasRouteTag) {
          sqlQuery += " AND KL.RTG = ?";
          parameters.add(routeTag);
        }
        if (StringUtils.isNotEmpty(startsWith)) {
          sqlQuery += " AND K.NNAME like ?";
          parameters.add(startsWith.toLowerCase() + "%");
        }
        orderBy = " ORDER BY KL.RI";
      } else {
        sqlQuery =
            "select KL.*,KL.`KEY` as `KEY` from KIOSKLINK KL, KIOSK K where KL.KIOSKID = ? AND "
                + "KL.LINKTYPE = ? and KL.LINKEDKIOSKID = K.KIOSKID";
        if (StringUtils.isNotEmpty(startsWith)) {
          sqlQuery += " AND K.NNAME like ?";
          parameters.add(startsWith.toLowerCase() + "%");
        }
        orderBy = " ORDER BY K.NAME";
      }
      sqlQuery += orderBy;
      if (pageParams != null) {
        limitStr =
            " LIMIT " + pageParams.getOffset() + CharacterConstants.COMMA + pageParams.getSize();
        sqlQuery += limitStr;
      }
      Query query = null;
      Query cntQuery = null;
      try {
        query = pm.newQuery("javax.jdo.query.SQL", sqlQuery);
        query.setClass(KioskLink.class);
        links = (List<IKioskLink>) query.executeWithArray(parameters.toArray());
        links.size();
        links = (List<IKioskLink>) pm.detachCopyAll(links);
        String
            cntQueryStr =
            sqlQuery.replace("KL.*,KL.`KEY` as `KEY`", QueryConstants.ROW_COUNT)
                .replace(orderBy, CharacterConstants.EMPTY);
        if (StringUtils.isNotEmpty(limitStr)) {
          cntQueryStr = cntQueryStr.replace(limitStr, CharacterConstants.EMPTY);
        }
        cntQuery = pm.newQuery("javax.jdo.query.SQL", cntQueryStr);
        count =
            ((Long) ((List) cntQuery.executeWithArray(parameters.toArray())).iterator().next())
                .intValue();
      } finally {
        if (query != null) {
          query.closeAll();
        }
        if (cntQuery != null) {
          cntQuery.closeAll();
        }
      }
    } catch (JDOObjectNotFoundException e) {
      // do nothing; null will be returned
    } catch (Exception e) {
      throw new ServiceException(e);
    } finally {
      pm.close();
    }
    xLogger.fine("Exiting getKioskLinks");
    return new Results(links, null, count, pageParams!=null?pageParams.getOffset():0);
  }

  /**
   * Get a given kiosk link
   */
  public IKioskLink getKioskLink(String linkId) throws ObjectNotFoundException, ServiceException {
    xLogger.fine("Entered getKioskLink");
    IKioskLink link = null;
    PersistenceManager pm = PMF.get().getPersistenceManager();
    try {
      link = JDOUtils.getObjectById(IKioskLink.class, linkId, pm);
      link = pm.detachCopy(link);
    } catch (JDOObjectNotFoundException e) {
      throw new ObjectNotFoundException(e.getMessage());
    } finally {
      pm.close();
    }

    xLogger.fine("Exiting getKioskLink");
    return link;
  }

  /**
   * Update a given kiosk link
   */
  public void updateKioskLink(IKioskLink kl) throws ServiceException {
    xLogger.fine("Entered updateKioskLink");
    PersistenceManager pm = PMF.get().getPersistenceManager();
    try {
      // Get the kiosk link
      IKioskLink kioskLink = JDOUtils.getObjectById(IKioskLink.class, kl.getId(), pm);
      // Update attributes
      kioskLink.setCreditLimit(kl.getCreditLimit());
      kioskLink.setDescription(kl.getDescription());
      kioskLink.setLinkedKioskName(kl.getLinkedKioskName());
      // Update attributes of the reverse link
      String
          reverseLinkKey =
          JDOUtils.createKioskLinkId(kl.getLinkedKioskId(),
              EntityUtils.getReverseLinkType(kl.getLinkType()), kl.getKioskId());
      IKioskLink rKioskLink = JDOUtils.getObjectById(IKioskLink.class, reverseLinkKey, pm);
      rKioskLink.setCreditLimit(kl.getCreditLimit());
      rKioskLink.setDescription(kl.getDescription());
      if (kl.getKioskName() != null) {
        rKioskLink.setLinkedKioskName(kl.getKioskName());
      }
    } finally {
      pm.close();
    }
    xLogger.fine("Exiting updateKioskLink");
  }

  /**
   * Get kiosk-link, given the linkid
   */
  @Override
  public IKioskLink getKioskLink(Long kioskId, String linkType, Long linkedKioskId)
      throws ServiceException, ObjectNotFoundException {
    xLogger.fine("Entered getKioskLink");
    PersistenceManager pm = PMF.get().getPersistenceManager();
    IKioskLink kl = null;
    String msg = null;
    try {
      kl =
          JDOUtils.getObjectById(IKioskLink.class,
              JDOUtils.createKioskLinkId(kioskId, linkType, linkedKioskId), pm);
      kl = pm.detachCopy(kl);
    } catch (JDOObjectNotFoundException e) {
      msg = e.getMessage();
    } finally {
      pm.close();
    }
    if (msg != null) {
      throw new ObjectNotFoundException(msg);
    }
    xLogger.fine("Exiting getKioskLink");
    return kl;
  }

  /**
   * Delete kiosk links given the link keys, or Ids
   *
   * @param linkIds The unique key of each link (say, as generated by the datastore)
   */
  public void deleteKioskLinks(Long domainId, List<String> linkIds, String sUser)
      throws ServiceException {
    xLogger.fine("Entered deleteKioskLinks");

    if (linkIds == null || linkIds.size() == 0) {
      throw new ServiceException("Invalid link Ids");
    }
    sUser = (sUser != null ? sUser : " ");
    String errMsg = null;
    Exception exception = null;
    String linkedKioskName;
    String kioskName;
    String linkType;
    PersistenceManager pm = PMF.get().getPersistenceManager();
    try {
      // Get the kiosk link objects
      List<IKioskLink> kioskLinks = new ArrayList<IKioskLink>();
      for (String linkId : linkIds) {
        IKioskLink kl = JDOUtils.getObjectById(IKioskLink.class, linkId, pm);
        kioskLinks.add(kl);
        linkType = kl.getLinkType();//for auditlog
        // Get the reverse link to delete as well
        IKioskLink
            rlink =
            JDOUtils.getObjectById(IKioskLink.class, JDOUtils
                .createKioskLinkId(kl.getLinkedKioskId(),
                    EntityUtils.getReverseLinkType(kl.getLinkType()), kl.getKioskId()), pm);
        // Get the domain ID of the linked entity
        IKiosk linkedKiosk = JDOUtils.getObjectById(IKiosk.class, kl.getLinkedKioskId());
        kioskName = JDOUtils.getObjectById(IKiosk.class, kl.getKioskId()).getName();//for auditlog
        linkedKioskName = linkedKiosk.getName();//for auditlog
        // Delete the links and remove them from the respective domains (superdomains)
        DomainsUtil.removeFromDomain(kl, domainId, pm);
        DomainsUtil.removeFromDomain(rlink, linkedKiosk.getDomainId(), pm);
        xLogger.info("AUDITLOG\t{0}\t{1}\tENTITY\t " +
                "DELETE RELATION\t{2}\t{3}\tLINKTYPE={4}", domainId, sUser, kioskName,
            linkedKioskName,
            linkType);
      }
      // Delete the kiosk links
      pm.deletePersistentAll(kioskLinks);
    } catch (Exception e) {
      exception = e;
      errMsg = e.getMessage();
    } finally {
      pm.close();
    }
    if (errMsg != null) {
      throw new ServiceException(errMsg, exception);
    }
    xLogger.fine("Exiting deleteKioskLinks");
  }

  /**
   * Check if the kiosk-link exists
   */
  public boolean hasKioskLink(Long kioskId, String linkType, Long linkedKioskId)
      throws ServiceException {
    xLogger.fine("Entered hasKioskLink");
    try {
      return getKioskLink(kioskId, linkType, linkedKioskId) != null;
    } catch (ObjectNotFoundException e) {
      return false;
    }
  }

  public void destroy() throws ServiceException {
    xLogger.fine("Entering destroy");
    // TODO Auto-generated method stub
    xLogger.fine("Exiting destroy");
  }

  public Class<? extends Service> getInterface() {
    xLogger.fine("Entering getInterface");
    xLogger.fine("Exiting getInterface");
    return EntitiesServiceImpl.class;
  }

  public void init(Services services) throws ServiceException {
    xLogger.fine("Entering init");
    // TODO Auto-generated method stub
    xLogger.fine("Exiting init");
  }

  public void updateManagedEntitiesOrdering(Long domainId, String userId, String routeQueryString)
      throws ServiceException {
    xLogger.fine("Entering updateManagedEntitiesOrdering");

    if (domainId == null || userId == null || userId.isEmpty() || routeQueryString == null
        || routeQueryString.isEmpty()) {
      xLogger
          .warn("Invalid parameters. domainId: {0}, userId: {1}, routeQueryString: {2}", domainId,
              userId, routeQueryString);
      throw new ServiceException("Invalid parameters");
    }

    // Form the query
    String
        queryStr =
        "SELECT FROM " + JDOUtils.getImplClass(IUserToKiosk.class).getName()
            + " WHERE userId == userIdParam PARAMETERS String userIdParam";
    // Get query params.
    Map<String, Object> params = new HashMap<>();
    params.put("userIdParam", userId);
    // Form the QueryParams
    QueryParams qp = new QueryParams(queryStr, params);
    // Form the PageParams
    PageParams pageParams = new PageParams(null, PageParams.DEFAULT_SIZE);
    boolean hasRoute = true;
    try {
      PagedExec
          .exec(domainId, qp, pageParams, UpdateRouteProcessor.class.getName(), routeQueryString,
              null);
    } catch (Exception e) {
      xLogger.severe(
          "{0} when doing paged-exec to update route for managed entities in domain {1}: {2}",
          e.getClass().getName(), domainId, e.getMessage());
      e.printStackTrace();
      hasRoute = false;
    }
    // Update the UserAccount to set the route enabled flag
    PersistenceManager pm = PMF.get().getPersistenceManager();
    try {
      IUserAccount u = JDOUtils.getObjectById(IUserAccount.class, userId, pm);
      u.setRteEnabled(hasRoute);
    } catch (Exception e) {
      xLogger.severe("{0} when trying to set the routeEnabled flag for user {1}. Message: {2}",
          e.getClass().getName(), userId, e.getMessage());
    } finally {
      pm.close();
    }

    xLogger.fine("Exiting updateManagedEntitiesOrdering");
  }

  public void updateRelatedEntitiesOrdering(Long domainId, Long kioskId, String linkType,
                                            String routeQueryString) throws ServiceException {
    xLogger.fine("Entering updateRelatedEntitiesOrdering");
    if (domainId == null || kioskId == null || linkType == null || linkType.isEmpty()
        || routeQueryString == null || routeQueryString.isEmpty()) {
      xLogger.warn(
          "Invalid parameters. domainId: {0}, kioskId: {1}, linkType: {2}, routeQueryString: {3}",
          domainId, kioskId, linkType, routeQueryString);
      throw new ServiceException("Invalid parameters.");
    }

    // Form the query
    String
        queryStr =
        "SELECT FROM " + JDOUtils.getImplClass(IKioskLink.class).getName()
            + " WHERE kioskId == kioskIdParam && linkType == linkTypeParam PARAMETERS Long kioskIdParam, String linkTypeParam";
    // Get query params.
    Map<String, Object> params = new HashMap<>();
    params.put("kioskIdParam", kioskId);
    params.put("linkTypeParam", linkType);
    // Form the QueryParams
    QueryParams qp = new QueryParams(queryStr, params);
    // Form the PageParams
    PageParams pageParams = new PageParams(null, PageParams.DEFAULT_SIZE);
    boolean hasRoute = true;
    try {
      PagedExec
          .exec(domainId, qp, pageParams, UpdateRouteProcessor.class.getName(), routeQueryString,
              null);
    } catch (Exception e) {
      xLogger.severe("{0} when doing paged-exec to update KioskLink entries in domain {1}: {2}",
          e.getClass().getName(), domainId, e.getMessage());
      e.printStackTrace();
      hasRoute = false;
    }
    // Update the Kiosk to set the route enabled flag
    PersistenceManager pm = PMF.get().getPersistenceManager();
    try {
      IKiosk k = JDOUtils.getObjectById(IKiosk.class, kioskId, pm);
      k.setRouteEnabled(linkType, hasRoute);
    } catch (Exception e) {
      xLogger.severe("{0} when trying to set the routeEnabled flag for kiosk {1}. Message: {2}",
          e.getClass().getName(), kioskId, e.getMessage());
    } finally {
      pm.close();
    }

    xLogger.fine("Exiting updateRelatedEntitiesOrdering");
  }

  public void resetManagedEntitiesOrdering(String userId) throws ServiceException {
    xLogger.fine("Entering resetManagedEntitiesOrdering");
    // Update the UserAccount to set the route enabled flag
    PersistenceManager pm = PMF.get().getPersistenceManager();
    try {
      IUserAccount u = JDOUtils.getObjectById(IUserAccount.class, userId, pm);
      u.setRteEnabled(false);
    } catch (Exception e) {
      xLogger.severe("{0} when trying to set the routeEnabled flag for user {1}. Message: {2}",
          e.getClass().getName(), userId, e.getMessage());
    } finally {
      pm.close();
    }
    xLogger.fine("Exiting resetManagedEntitiesOrdering");
  }

  public void resetRelatedEntitiesOrdering(Long kioskId, String linkType) throws ServiceException {
    xLogger.fine("Entering resetRelatedEntitiesOrdering");
    // Update the Kiosk to set the route enabled flag
    PersistenceManager pm = PMF.get().getPersistenceManager();
    try {
      IKiosk k = JDOUtils.getObjectById(IKiosk.class, kioskId, pm);
      k.setRouteEnabled(linkType, false);
    } catch (Exception e) {
      xLogger.severe("{0} when trying to set the routeEnabled flag for kiosk {1}. Message: {2}",
          e.getClass().getName(), kioskId, e.getMessage());
    } finally {
      pm.close();
    }
    xLogger.fine("Exiting resetRelatedEntitiesOrdering");
  }

  // Update user associations for a given kiosk
  @SuppressWarnings("unchecked")
  private void updateUsersForKiosk(IKiosk kiosk, PersistenceManager pm) {
    List<IUserAccount> users = (List<IUserAccount>) kiosk.getUsers();
    Long domainId = kiosk.getDomainId();
    //Create a list of userIds from the list of users
    List<String> userIds = new ArrayList<>();
    for (IUserAccount user : users) {
      userIds.add(user.getUserId());
    }

    Query query = pm.newQuery(JDOUtils.getImplClass(IUserToKiosk.class));
    query.setFilter("kioskId == kioskIdParam");
    query.declareParameters("Long kioskIdParam");
    List<String> queryUserIds = new ArrayList<>();
    try {
      List<IUserToKiosk> results = (List<IUserToKiosk>) query.execute(kiosk.getKioskId());
      for (IUserToKiosk mapping : results) {
        String userId = mapping.getUserId();
        queryUserIds.add(userId);
        if (!userIds.contains(mapping.getUserId())) {
          //remove it
          xLogger.fine("updateKiosk: Deleting mapping between user {0} and kiosk {1}",
              mapping.getUserId(), mapping.getKioskId());
          pm.deletePersistent(mapping);
          // Decrement counter
          Counter.getUserToKioskCounter(domainId, userId).increment(-1);
        }
      }
      for (String userId : userIds) {
        if (!queryUserIds.contains(userId)) {
          //add it
          IUserToKiosk rel = JDOUtils.createInstance(IUserToKiosk.class);
          rel.setUserId(userId);
          rel.setKioskId(kiosk.getKioskId(), kiosk.getName());
          rel.setDomainId(domainId);
          xLogger.fine("updateKiosk: Adding mapping between user {0} and kiosk {1}", userId,
              kiosk.getKioskId());
          pm.makePersistent(rel);
          // Increment counter
          Counter.getUserToKioskCounter(domainId, userId).increment(1);
        }
      }
    } finally {
      query.closeAll();
    }
  }

  /**
   * Set ManagedEntitiesRouteInfo
   */
  @SuppressWarnings("unchecked")
  public void setManagedEntityRouteInfo(Long domainId, String userId, Long kioskId, String routeTag,
                                        Integer routeIndex) throws ServiceException {
    // Get the UserToKiosk object for the userId and kioskId
    // Set the routeTag to routeTag
    // Do not set the routeIndex because it is already set to Default Route Index
    xLogger.fine("Entering setManagedEntityRouteInfo");

    if (domainId == null || userId == null || userId.isEmpty() || kioskId == null) {
      xLogger.warn("Invalid parameters. domainId: {0}, userId: {1}, kioskId: {2}", domainId, userId,
          kioskId);
      throw new ServiceException("Invalid parameters");
    }

    PersistenceManager pm = PMF.get().getPersistenceManager();
    // Update the UserAccount to set the route enabled flag
    try {
      // Form the query
      Query userToKioskQuery = pm.newQuery(JDOUtils.getImplClass(IUserToKiosk.class));
      userToKioskQuery.setFilter("kioskId == kioskIdParam");
      userToKioskQuery.declareParameters("Long kioskIdParam");
      // Execute the query
      try {
        List<IUserToKiosk> results = (List<IUserToKiosk>) userToKioskQuery.execute(kioskId);
        if (results != null && !results.isEmpty()) {
          // Iterate through the results
          // Check if userId of the UserToKiosk object matches with the userId that is passed
          // If it matches, then set the routeTag and routeIndex for that UserToKiosk object
          for (IUserToKiosk u2k : results) {
            if (u2k.getUserId().equals(userId)) {
              u2k.setTag(routeTag);
              if (routeIndex != null) {
                u2k.setRouteIndex(routeIndex);
              }
              break;
            }
          }
        }
      } finally {
        userToKioskQuery.closeAll();
      }
    } catch (Exception e) {
      xLogger.severe(
          "{0} when trying to set the route info for managed entities for user {1}. Message: {2}",
          e.getClass().getName(), userId, e.getMessage());
    } finally {
      pm.close();
    }

    xLogger.fine("Exiting updateManagedEntityRouteInfo");
  }

  /**
   * Set RelatedEntitiesRouteInfo
   */
  public void setRelatedEntityRouteInfo(Long domainId, Long kioskId, String linkType,
                                        Long linkedKioskId, String routeTag, Integer routeIndex)
      throws ServiceException {
    xLogger.fine("Entering setRelatedEntityRouteInfo");
    if (domainId == null || kioskId == null || linkType == null || linkType.isEmpty()) {
      xLogger.warn(
          "Invalid parameters. domainId: {0}, kioskId: {1}, linkType: {2}, linkedKioskId: {3}",
          domainId, kioskId, linkType, linkedKioskId);
      throw new ServiceException("Invalid parameters");
    }
    // Get the KioskLink object for the kioskId and link type and linkedKioskId
    PersistenceManager pm = PMF.get().getPersistenceManager();
    try {
      IKioskLink
          kl =
          JDOUtils.getObjectById(IKioskLink.class,
              JDOUtils.createKioskLinkId(kioskId, linkType, linkedKioskId), pm);
      kl.setRouteTag(routeTag);
      if (routeIndex != null) {
        kl.setRouteIndex(routeIndex);
      }
    } catch (Exception e) {
      xLogger.severe(
          "{0} while setting related entity route info for kioskId {1} & linked-KioskId {2} & linkType {3}: {4}",
          e.getClass().getName(), kioskId, linkedKioskId, linkType, e.getMessage());
    } finally {
      pm.close();
    }
    xLogger.fine("Exiting setRelatedEntityRouteInfo");
  }

  @SuppressWarnings("unchecked")
  public IKiosk getKioskByName(Long domainId, String kioskName) throws ServiceException {
    xLogger.fine("Entering getKiosk");
    if (domainId == null || kioskName == null || kioskName.isEmpty()) {
      throw new ServiceException("Invalid parameters");
    }
    IKiosk k = null;
    // Form query
    PersistenceManager pm = PMF.get().getPersistenceManager();
    // Update the UserAccount to set the route enabled flag
    try {
      // Form the query
      Query kioskQuery = pm.newQuery(JDOUtils.getImplClass(IKiosk.class));
      kioskQuery.setFilter("dId.contains(dIdParam) && nName == nameParam");
      kioskQuery.declareParameters("Long dIdParam, String nameParam");
      // Execute the query
      try {
        List<IKiosk> results = (List<IKiosk>) kioskQuery.execute(domainId, kioskName.toLowerCase());
        if (results != null && !results.isEmpty()) {
          k = results.get(0);
          k = pm.detachCopy(k);
        }
      } finally {
        kioskQuery.closeAll();
      }
    } catch (Exception e) {
      xLogger.severe("{0} when trying to get Kiosk for kiosk Name {1}. Message: {2}",
          e.getClass().getName(), kioskName, e.getMessage());
    } finally {
      pm.close();
    }
    xLogger.fine("Exiting getKiosk");
    return k;
  }


  private void incrementKioskCounter(List<Long> domainIds, int amount, PersistenceManager pm) {
    if (domainIds == null || domainIds.isEmpty()) {
      return;
    }
    Iterator<Long> it = domainIds.iterator();
    while (it.hasNext()) {
      Counter.getKioskCounter(it.next()).increment(amount, pm);
    }
  }

  // Private method that checks if a custom ID already exists in the domain. This helps to avoid user accounts or kiosks with duplicate custom IDs in a domain
  private boolean checkIfCustomIdExists(Object object) {
    xLogger.fine("Entering checkIfCustomIdExists");
    boolean customIdExists = false;
    if (object instanceof IUserAccount) {
      xLogger.fine("object is an instance of UserAccount");
      IUserAccount userAccount = (IUserAccount) object;
      Long domainId = userAccount.getDomainId();
      PersistenceManager pm = PMF.get().getPersistenceManager();
      // Check if another user by the same custom ID exists in the database
      Query query = pm.newQuery(JDOUtils.getImplClass(IUserAccount.class));
      query.setFilter("dId.contains(domainIdParam) && cId == cidParam");
      query.declareParameters("Long domainIdParam, String cidParam");
      try {
        @SuppressWarnings("unchecked")
        List<IUserAccount>
            results =
            (List<IUserAccount>) query.execute(domainId, userAccount.getCustomId());
        if (results != null && results.size() == 1) {
          // UserAccount with this custom id already exists in the database!
          xLogger.warn(
              "Error while adding or updating user Account {0}: Custom ID {1} already exists.",
              userAccount.getUserId(), userAccount.getCustomId());
          customIdExists = true;
        }
      } finally {
        try {
          query.closeAll();
        } catch (Exception ignored) {
          xLogger.warn("Exception while closing query", ignored);
        }
        pm.close();
      }
    } else if (object instanceof IKiosk) {
      xLogger.fine("Object is an instance of Kiosk");
      IKiosk kiosk = (IKiosk) object;
      Long domainId = kiosk.getDomainId();
      PersistenceManager pm = PMF.get().getPersistenceManager();
      // Check if another kiosk by the same custom ID exists in the database
      Query query = pm.newQuery(JDOUtils.getImplClass(IKiosk.class));
      query.setFilter("dId.contains(domainIdParam) && cId == cidParam");
      query.declareParameters("Long domainIdParam, String cidParam");
      try {
        @SuppressWarnings("unchecked")
        List<IKiosk> results = (List<IKiosk>) query.execute(domainId, kiosk.getCustomId());
        if (results != null && results.size() == 1) {
          // Kiosk with this name already exists in the database!
          xLogger.warn("Error while adding or updating kiosk {0}: Custom ID {1} already exists.",
              kiosk.getKioskId(), kiosk.getCustomId());
          customIdExists = true;
        }
      } finally {
        try {
          query.closeAll();
        } catch (Exception ignored) {
          xLogger.warn("Exception while closing query", ignored);
        }
        pm.close();
      }
    } else {
      xLogger.severe("Invalid object type: {0}", object.getClass().getName());
    }
    xLogger.fine("Exiting checkIfCustomIdExists. Returning customIdExists: {0}", customIdExists);
    return customIdExists;
  }

  /**
   * Get a domain, given its ID
   *
   * @deprecated Moved to DomainsService, but presevered here to enable backward compatibility in JSPs
   */
  public IDomain getDomain(Long domainId) throws ServiceException, ObjectNotFoundException {
    xLogger.fine("Entered getDomain");
    DomainsService ds = Services.getService(DomainsServiceImpl.class);
    xLogger.fine("Exiting getDomain");
    return ds.getDomain(domainId);
  }

  /**
   * Get all domains
   *
   * @deprecated Moved to DomainsService, but preserved here to enable backward compatibility in JSPs
   */
  public Results getAllDomains(PageParams pageParams) throws ServiceException {
    xLogger.fine("Entered getAllDomains");
    DomainsService ds = Services.getService(DomainsServiceImpl.class);
    xLogger.fine("Exiting getAllDomains");
    return ds.getAllDomains(pageParams);
  }


  public void updateEntityActivityTimestamps(Long entityId, Date timestamp, int actType) {
    // Modify the kiosk's inventory/order active time stamp
    PersistenceManager pm = PMF.get().getPersistenceManager();
    try {
      // Set the entity's inventory active time
      IKiosk k = JDOUtils.getObjectById(IKiosk.class, entityId, pm);
      if (actType == IKiosk.TYPE_INVENTORYACTIVITY && k.getInventoryActiveTime() == null) {
        k.setInventoryActiveTime(timestamp);
      }
      if (actType == IKiosk.TYPE_ORDERACTIVITY && k.getOrderActiveTime() == null) {
        k.setOrderActiveTime(timestamp);
      }
    } catch (Exception e) {
      xLogger
          .warn("Exception when trying to update activity timestamps for entity {0}", entityId, e);
    } finally {
      pm.close();
    }
  }


  public List<EntityLinkModel> getKioskLinksInDomain(Long domainId, String user, String role) {
    PersistenceManager pm = null;
    Query query = null;
    try {
      pm = PMF.get().getPersistenceManager();
      StringBuilder
          sqlQuery =
          new StringBuilder(
              "SELECT K.NAME,K.KIOSKID,KL.LINKTYPE,KL.LINKEDKIOSKID,KL.LKNM FROM KIOSK K LEFT JOIN KIOSKLINK KL ON K.KIOSKID = KL.KIOSKID WHERE K.KIOSKID IN(");
      if (SecurityConstants.ROLE_SERVICEMANAGER.equalsIgnoreCase(role)) {
        sqlQuery.append("SELECT KIOSKID FROM USERTOKIOSK WHERE USERID=");
        sqlQuery.append("'");
        sqlQuery.append(user);
        sqlQuery.append("' AND DID= ");
      } else {
        sqlQuery.append("SELECT KIOSKID_OID FROM KIOSK_DOMAINS WHERE DOMAIN_ID=");
      }
      sqlQuery.append(domainId);
      sqlQuery.append(") AND IFNULL(KL.LINKTYPE,'c') = 'c'");

      query = pm.newQuery("javax.jdo.query.SQL", sqlQuery.toString());
      List data = (List) query.execute();
      if (data != null && !data.isEmpty()) {
        List<EntityLinkModel> models = new ArrayList<>();
        for (Object aData : data) {
          EntityLinkModel nvm = new EntityLinkModel();
          Object[] kLinks = (Object[]) aData;
          nvm.kioskName = (String) kLinks[0];
          nvm.kioskId = (Long) kLinks[1];
          nvm.linkType = (String) kLinks[2];
          nvm.linkedKioskId = (Long) kLinks[3];
          nvm.linkedKioskName = (String) kLinks[4];
          models.add(nvm);
        }
        return models;
      }

    } catch (Exception e) {
      xLogger.warn("Exception while fetching kiosk links", e);
    } finally {
      if (query != null) {
        try {
          query.closeAll();
        } catch (Exception ignored) {
          xLogger.warn("Exception while closing query", ignored);
        }
      }
      if (pm != null) {
        pm.close();
      }
    }
    xLogger.fine("Exiting getKioskLinks");
    return null;
  }

  @SuppressWarnings("unchecked")
  @Override
  public List<String> getAllDistricts(Long domainId) {
    PersistenceManager pm = PMF.get().getPersistenceManager();
    Query query = null;
    List<String> districts = new ArrayList<>(1);
    try {
      query =
          pm.newQuery("javax.jdo.query.SQL",
              "SELECT DISTINCT DISTRICT  FROM KIOSK WHERE KIOSKID IN " +
                  "( SELECT KIOSKID_OID FROM KIOSK_DOMAINS WHERE DOMAIN_ID=" + domainId + ")");
      List dsts = (List) query.execute();
      for (Object dst : dsts) {
        String district = (String) dst;
        if (district != null) {
          districts.add(district);
        }
      }
      return districts;
    } catch (Exception e) {
      xLogger.severe("Error while fetching material ids from domain {0} and query {1}", domainId,
          query, e);
    } finally {
      if (query != null) {
        try {
          query.closeAll();
        } catch (Exception ignored) {
          xLogger.warn("Exception while closing query", ignored);
        }
      }
      pm.close();
    }
    return null;
  }

  @Override
  public List<String> getAllStates(Long domainId) {
    PersistenceManager pm = PMF.get().getPersistenceManager();
    Query query = null;
    List<String> districts = new ArrayList<>(1);
    try {
      query =
          pm.newQuery("javax.jdo.query.SQL", "SELECT DISTINCT STATE  FROM KIOSK WHERE KIOSKID IN " +
              "( SELECT KIOSKID_OID FROM KIOSK_DOMAINS WHERE DOMAIN_ID=" + domainId + ")");
      List dsts = (List) query.execute();
      for (Object dst : dsts) {
        String district = (String) dst;
        if (district != null) {
          districts.add(district);
        }
      }
      return districts;
    } catch (Exception e) {
      xLogger.severe("Error while fetching material ids from domain {0} and query {1}", domainId,
          query, e);
    } finally {
      if (query != null) {
        try {
          query.closeAll();
        } catch (Exception ignored) {
          xLogger.warn("Exception while closing query", ignored);
        }
      }
      pm.close();
    }
    return null;
  }

  @Override
  public List<String> getAllCountries(Long domainId) {
    PersistenceManager pm = PMF.get().getPersistenceManager();
    Query query = null;
    List<String> districts = new ArrayList<>(1);
    try {
      query =
          pm.newQuery("javax.jdo.query.SQL",
              "SELECT DISTINCT COUNTRY  FROM KIOSK WHERE KIOSKID IN " +
                  "( SELECT KIOSKID_OID IN  KIOSK_DOMAINS WHERE DOMAIN_ID=" + domainId + ")");
      List dsts = (List) query.execute();
      for (Object dst : dsts) {
        String district = (String) dst;
        if (district != null) {
          districts.add(district);
        }
      }
      return districts;
    } catch (Exception e) {
      xLogger.severe("Error while fetching material ids from domain {0} and query {1}", domainId,
          query, e);
    } finally {
      if (query != null) {
        try {
          query.closeAll();
        } catch (Exception ignored) {
          xLogger.warn("Exception while closing query", ignored);
        }
      }
      pm.close();
    }
    return null;
  }

  @Override
  public List<String> getAssetTagsToRegister(Long kId) throws ServiceException {
    IKiosk kiosk = getKiosk(kId, false);
    List<String> tagsForDevices = new ArrayList<String>();
    String state = kiosk.getState();
    List<Long> dIds = kiosk.getDomainIds();
    for (Long dId : dIds) {
      if (dId != null) {
        tagsForDevices.add(dId.toString());
        if (state != null && !state.isEmpty()) {
          tagsForDevices.add(dId + "." + state);
          // Get the district of the kiosk, if specified
          String district = kiosk.getDistrict();
          if (district != null && !district.isEmpty()) {
            tagsForDevices.add(dId + "." + state + "." + district);
            // Get the taluk of the kiosk, if present
            String taluk = kiosk.getTaluk();
            if (taluk != null && !taluk.isEmpty()) {
              tagsForDevices.add(dId + "." + state + "." + district + "." + taluk);
            }
          }
        }
      }
    }
    tagsForDevices.add("kiosk" + "." + kId);
    return tagsForDevices;
  }

  public void registerOrUpdateDevices(List<IAsset> assetList) throws ServiceException {
    if (assetList != null && !assetList.isEmpty()) {
      List<AssetModel> assetModels = new ArrayList<>(assetList.size());

      for (IAsset asset : assetList) {
        AssetModel assetModel = AssetUtil.buildFilterModel(asset);
        if (assetModel.kId != null) {
          assetModel.tags = getAssetTagsToRegister(assetModel.kId);
        } else {
          //Getting listing linked domains, if asset not mapped to any entity.
          Set<Long>
              domainIds =
              DomainsUtil.getDomainLinks(asset.getDomainId(), IDomainLink.TYPE_PARENT, true);
          assetModel.tags = new ArrayList<>(domainIds.size());
          for (Long currentDomainId : domainIds) {
            assetModel.tags.add(String.valueOf(currentDomainId));
          }
        }
        assetModels.add(assetModel);
      }
      AssetUtil.registerDevices(new Gson().toJson(
          new AssetModels.AssetRegistrationModel(assetModels)));
    }
  }

  @Override
  public UserEntitiesModel getUserWithKiosks(String userId)
      throws ObjectNotFoundException, ServiceException {
    UsersService usersService = Services.getService(UsersServiceImpl.class, this.getLocale());
    IUserAccount userAccount = usersService.getUserAccount(userId);
    Results results = getKiosksForUser(userAccount, null, null);
    return new UserEntitiesModel(userAccount, results.getResults());
  }

  @Override
  public UserEntitiesModel getUserWithKiosks(IUserAccount userAccount)
      throws ObjectNotFoundException, ServiceException {
    Results results = getKiosksForUser(userAccount, null, null);
    return new UserEntitiesModel(userAccount, results.getResults());
  }

  // Get the linked kiosks that for a given kiosk
  @SuppressWarnings("unchecked")
  @Override
  public Results getLinkedKiosks(Long kioskId, String linkType, String routeTag,
                                 PageParams pageParams) throws ServiceException {
    xLogger.fine("Entering getVendors");
    List<IKiosk> linkedKiosks = new ArrayList<>();
    // Get the services
    // Get the vendor kiosk links
    Results results = getKioskLinks(kioskId, linkType, routeTag, null, pageParams);
    List<IKioskLink> links = results.getResults();
    if (links != null && !links.isEmpty()) {
      for (IKioskLink kl : links) {
        try {
          IKiosk k = getKiosk(kl.getLinkedKioskId(), false);
          k.setRouteIndex(kl.getRouteIndex());
          k.setRouteTag(kl.getRouteTag());
          linkedKiosks.add(k);
        } catch (Exception e) {
          xLogger.warn("{0} when getting getting kiosk {1} linked to kiosk {2} with type {3}: {4}",
              e.getClass().getName(), kl.getLinkedKioskId(), kioskId, linkType, e.getMessage());
        }
      }
    }
    xLogger.fine("Exiting getVendors");
    return new Results(linkedKiosks, results.getCursor());
  }

  private String getLocFilterSubQuery(LocationSuggestionModel parentLocation) {
    StringBuilder filterQuery = new StringBuilder();
    if (parentLocation != null) {
        if (StringUtils.isNotEmpty(parentLocation.state)) {
            filterQuery.append(" AND STATE ")
                .append(CharacterConstants.EQUALS).append(CharacterConstants.S_QUOTE)
                .append(parentLocation.state).append(CharacterConstants.S_QUOTE).append(CharacterConstants.SPACE);
        }
        if (StringUtils.isNotEmpty(parentLocation.district)) {
            filterQuery.append(" AND DISTRICT ")
                .append(CharacterConstants.EQUALS).append(CharacterConstants.S_QUOTE)
                .append(parentLocation.district).append(CharacterConstants.S_QUOTE).append(CharacterConstants.SPACE);
        }
    }
    return filterQuery.toString();
  }

  @Override
  public List<LocationSuggestionModel> getCitySuggestions(Long domainId, String text) {
    PersistenceManager pm = PMF.get().getPersistenceManager();
    Query query = null;
    List<LocationSuggestionModel> states = new ArrayList<>(8);
    try {
      query =
          pm.newQuery("javax.jdo.query.SQL",
              "SELECT DISTINCT COUNTRY, STATE, DISTRICT, TALUK, CITY FROM KIOSK WHERE CITY COLLATE UTF8_GENERAL_CI LIKE '%" + text + "%' AND KIOSKID IN " +
                  "( SELECT KIOSKID_OID FROM KIOSK_DOMAINS WHERE DOMAIN_ID=" + domainId
                  + ") LIMIT 8");
      List stateList = (List) query.execute();
      for (Object st : stateList) {
        String country = (String)((Object[])st)[0];
        String state = (String)((Object[])st)[1];
        String district = (String)((Object[])st)[2];
        String taluk = (String)((Object[])st)[3];
        String city = (String)((Object[])st)[4];
        LocationSuggestionModel model = new LocationSuggestionModel();
        model.label = city;
        StringBuilder sl = new StringBuilder();
        if(StringUtils.isNotBlank(taluk)) {
          sl.append(taluk).append(CharacterConstants.COMMA).append(CharacterConstants.SPACE);
        }
        if(StringUtils.isNotBlank(district)) {
          sl.append(district).append(CharacterConstants.COMMA).append(CharacterConstants.SPACE);
        }
        model.subLabel = sl.append(state).toString();
        model.state = state;
        model.country = country;
        model.district = district;
        model.taluk = taluk;
        states.add(model);
      }
      return states;
    } catch (Exception e) {
      xLogger.severe("Error while fetching taluk suggestions from domain {0}", domainId, e);
    } finally {
      if (query != null) {
        try {
          query.closeAll();
        } catch (Exception ignored) {
          xLogger.warn("Exception while closing query", ignored);
        }
      }
      pm.close();
    }
    return null;
  }

  @Override
  public List<LocationSuggestionModel> getTalukSuggestions(Long domainId, String text, LocationSuggestionModel parentLocation) {
    PersistenceManager pm = PMF.get().getPersistenceManager();
    Query query = null;
    List<LocationSuggestionModel> states = new ArrayList<>(8);
    try {
      query =
          pm.newQuery(
              "javax.jdo.query.SQL",
              "SELECT DISTINCT COUNTRY, STATE, DISTRICT, TALUK FROM KIOSK WHERE TALUK COLLATE UTF8_GENERAL_CI LIKE '%"
                  + text
                  + "%' AND TALUK != '' AND TALUK IS NOT NULL AND KIOSKID IN "
                  + "( SELECT KIOSKID_OID FROM KIOSK_DOMAINS WHERE DOMAIN_ID="
                  + domainId
                  + ") "
                  + getLocFilterSubQuery(parentLocation)
                  +"LIMIT 8");
      List stateList = (List) query.execute();
      for (Object st : stateList) {
        String country = (String)((Object[])st)[0];
        String state = (String)((Object[])st)[1];
        String district = (String)((Object[])st)[2];
        String taluk = (String)((Object[])st)[3];
        LocationSuggestionModel model = new LocationSuggestionModel();
        model.label = taluk;
        StringBuilder sl = new StringBuilder();
        if(StringUtils.isNotBlank(district)) {
          sl.append(district).append(CharacterConstants.COMMA).append(CharacterConstants.SPACE);
        }
        model.subLabel = sl.append(state).toString();
        model.state = state;
        model.country = country;
        model.district = district;
        states.add(model);
      }
      return states;
    } catch (Exception e) {
      xLogger.severe("Error while fetching taluk suggestions from domain {0}", domainId, e);
    } finally {
      if (query != null) {
        try {
          query.closeAll();
        } catch (Exception ignored) {
          xLogger.warn("Exception while closing query", ignored);
        }
      }
      pm.close();
    }
    return null;
  }

  @Override
  public List<LocationSuggestionModel> getDistrictSuggestions(Long domainId, String text, LocationSuggestionModel parentLocation) {
    PersistenceManager pm = PMF.get().getPersistenceManager();
    Query query = null;
    List<LocationSuggestionModel> states = new ArrayList<>(8);
    try {
      query =
          pm.newQuery(
              "javax.jdo.query.SQL",
              "SELECT DISTINCT COUNTRY, STATE, DISTRICT FROM KIOSK WHERE DISTRICT COLLATE UTF8_GENERAL_CI LIKE '%"
                  + text
                  + "%' AND DISTRICT != '' AND DISTRICT IS NOT NULL AND KIOSKID IN "
                  + "( SELECT KIOSKID_OID FROM KIOSK_DOMAINS WHERE DOMAIN_ID="
                  + domainId
                  + ")"
                  + getLocFilterSubQuery(parentLocation)
                  +" LIMIT 8");
      List stateList = (List) query.execute();
      for (Object st : stateList) {
        String country = (String)((Object[])st)[0];
        String state = (String)((Object[])st)[1];
        String district = (String)((Object[])st)[2];
        LocationSuggestionModel model = new LocationSuggestionModel();
        model.label = district;
        model.subLabel = state;
        model.state = state;
        model.country = country;
        states.add(model);
      }
      return states;
    } catch (Exception e) {
      xLogger.severe("Error while fetching district suggestions from domain {0}", domainId, e);
    } finally {
      if (query != null) {
        try {
          query.closeAll();
        } catch (Exception ignored) {
          xLogger.warn("Exception while closing query", ignored);
        }
      }
      pm.close();
    }
    return null;
  }

  @Override
  public List<LocationSuggestionModel> getStateSuggestions(Long domainId, String text) {
    PersistenceManager pm = PMF.get().getPersistenceManager();
    Query query = null;
    List<LocationSuggestionModel> states = new ArrayList<>(8);
    try {
      query =
          pm.newQuery("javax.jdo.query.SQL",
              "SELECT DISTINCT COUNTRY,STATE  FROM KIOSK WHERE STATE COLLATE UTF8_GENERAL_CI LIKE '%" + text + "%' AND KIOSKID IN " +
                  "( SELECT KIOSKID_OID FROM KIOSK_DOMAINS WHERE DOMAIN_ID=" + domainId
                  + ") LIMIT 8");
      List stateList = (List) query.execute();
      for (Object st : stateList) {
        String country = (String)((Object[])st)[0];
        String state = (String)((Object[])st)[1];
        LocationSuggestionModel model = new LocationSuggestionModel();
        model.label = state;
        model.country = country;
        states.add(model);
      }
      return states;
    } catch (Exception e) {
      xLogger.severe("Error while fetching state suggestions from domain {0}", domainId, e);
    } finally {
      if (query != null) {
        try {
          query.closeAll();
        } catch (Exception ignored) {
          xLogger.warn("Exception while closing query", ignored);
        }
      }
      pm.close();
    }
    return null;
  }

}

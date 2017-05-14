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

package com.logistimo.dao.impl;

import com.logistimo.dao.IDaoUtil;
import com.logistimo.dao.JDOUtils;

import com.logistimo.entity.BBoard;

import com.logistimo.domains.IMultiDomain;
import com.logistimo.entities.service.EntitiesService;
import com.logistimo.entities.service.EntitiesServiceImpl;
import com.logistimo.entity.IUploaded;
import com.logistimo.entity.IUploadedMsgLog;
import com.logistimo.pagination.QueryParams;
import com.logistimo.orders.entity.IDemandItem;
import com.logistimo.orders.entity.IOrder;
import com.logistimo.services.Services;
import com.logistimo.services.utils.ConfigUtil;
import com.logistimo.tags.TagUtil;
import com.logistimo.tags.dao.ITagDao;
import com.logistimo.tags.dao.TagDao;
import com.logistimo.tags.entity.ITag;

import com.logistimo.pagination.PageParams;
import com.logistimo.pagination.PagedExec;
import com.logistimo.pagination.processor.PropagationProcessor;
import com.logistimo.services.ServiceException;
import com.logistimo.services.impl.PMF;
import com.logistimo.utils.StringUtil;
import com.logistimo.logger.XLog;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.jdo.PersistenceManager;
import javax.jdo.Query;

/**
 * @author Mohan Raja
 */
public class DaoUtil implements IDaoUtil {

  private static final XLog xLogger = XLog.getLog(DaoUtil.class);
  private static ITagDao tagDao = new TagDao();

  @Override
  @SuppressWarnings({"unchecked", "rawtypes"})
  public void updateTags(List<Long> domainIds, List<String> oldTags, List<String> newTags,
                         String type, Long entityId, PersistenceManager pm)
      throws ServiceException {
    xLogger.fine("Entered updateStoredTags");
    if (domainIds == null || domainIds.isEmpty()) {
      return;
    }
    List[] tagDiffs = StringUtil.getDiffs(oldTags, newTags);
    xLogger.fine("tagDiffs: {0}", (Object[]) tagDiffs);
    boolean hasNewTags = (tagDiffs[0] != null && !tagDiffs[0].isEmpty());
    boolean hasTagsRemoved = (tagDiffs[1] != null && !tagDiffs[1].isEmpty());
    if (hasNewTags || hasTagsRemoved) { // there are tag changes
      propagateTags(domainIds.get(0), entityId, type,
          newTags); // propagate the tags (including new ones) across
    }
    
    xLogger.fine("Exiting updateStoredTags");
  }

  // Propagate tags to required tables
  @SuppressWarnings("rawtypes")
  public static void propagateTags(Long domainId, Long entityId, String tagType,
                                   List<String> tags) {
    xLogger.fine("Entered propagateTags");
    String query = null;
    Map<String, Object> params = new HashMap<>();
    // Get the field data parameters
    String
        methodName =
        "setTgs"; // NOTE: The same method name exists on all objects: invntry, transaction, order, demanditem, invntryevntlog
    // Param types
    Class[] paramTypes = new Class[2];
    paramTypes[0] = List.class;
    paramTypes[1] = String.class;
    Object[] paramValues = new Object[2];
    paramValues[0] = tagDao.getTagsByNames(tags, getITagType(tagType));
    paramValues[1] = tagType;
    PropagationProcessor.FieldData
        fd =
        new PropagationProcessor.FieldData(methodName, paramTypes, paramValues);
    // Propagate to Invntry
    try {
      if (TagUtil.TYPE_MATERIAL.equals(tagType)) {
        query =
            "SELECT FROM com.logistimo.inventory.entity.Invntry"
                + " WHERE mId == mIdParam PARAMETERS Long mIdParam";
        params.put("mIdParam", entityId);
      } else if (TagUtil.TYPE_ENTITY.equals(tagType)) {
        query =
            "SELECT FROM com.logistimo.inventory.entity.Invntry"
                + " WHERE kId == kIdParam PARAMETERS Long kIdParam";
        params.put("kIdParam", entityId);
      } else {
        xLogger.severe("Invalid tag type {0} in domain {1}", tagType, domainId);
        return;
      }
      xLogger.info("Calling paged exec on propagation-processor: field data str = {0}",
          fd.toJSONString());
      PagedExec.exec(domainId, new QueryParams(query, params),
          new PageParams(null, PageParams.DEFAULT_SIZE), new PropagationProcessor(),
          fd.toJSONString(), null, 0, true);
    } catch (Exception e) {
      xLogger.severe(
          "{0} when propagating tags {1} of type {2} for Invntry object {3} in domain {4}: {5}",
          e.getClass().getName(), tags, tagType, entityId, domainId, e.getMessage());
    }

    // Propagate to InvntryBatch
    try {
      if (TagUtil.TYPE_MATERIAL.equals(tagType)) {
        query =
            "SELECT FROM com.logistimo.inventory.entity.InvntryBatch"
                + " WHERE mId == mIdParam PARAMETERS Long mIdParam";
        params.put("mIdParam", entityId);
      } else if (TagUtil.TYPE_ENTITY.equals(tagType)) {
        query =
            "SELECT FROM com.logistimo.inventory.entity.InvntryBatch"
                + " WHERE kId == kIdParam PARAMETERS Long kIdParam";
        params.put("kIdParam", entityId);
      } else {
        xLogger.severe("Invalid tag type {0} in domain {1}", tagType, domainId);
        return;
      }
      xLogger.info("Calling paged exec on propagation-processor: field data str = {0}",
          fd.toJSONString());
      PagedExec.exec(domainId, new QueryParams(query, params),
          new PageParams(null, PageParams.DEFAULT_SIZE), new PropagationProcessor(),
          fd.toJSONString(), null, 0, true);
    } catch (Exception e) {
      xLogger.severe(
          "{0} when propagating tags {1} of type {2} for Invntry object {3} in domain {4}: {5}",
          e.getClass().getName(), tags, tagType, entityId, domainId, e.getMessage());
    }

    // Propagate to Transaction
    if (ConfigUtil.isGAE()) {
      try {
        if (TagUtil.TYPE_MATERIAL.equals(tagType)) {
          query =
              "SELECT FROM com.logistimo.inventory.entity.ITransaction"
                  + " WHERE mId == mIdParam PARAMETERS Long mIdParam";
          params.put("mIdParam", entityId);
        } else if (TagUtil.TYPE_ENTITY.equals(tagType)) {
          query =
              "SELECT FROM com.logistimo.inventory.entity.ITransaction"
                  + " WHERE kId == kIdParam PARAMETERS Long kIdParam";
          params.put("kIdParam", entityId);
        }
        PagedExec.exec(domainId, new QueryParams(query, params),
            new PageParams(null, PageParams.DEFAULT_SIZE), new PropagationProcessor(),
            fd.toJSONString(), null, 0, true);
      } catch (Exception e) {
        xLogger.severe(
            "{0} when propagating tags {1} of type {2} for Transaction object {3} in domain {4}: {5}",
            e.getClass().getName(), tags, tagType, entityId, domainId, e.getMessage());
      }
    }

    // Propagate to InvntryEvntLog (abnormal stock events)
    try {
      if (TagUtil.TYPE_MATERIAL.equals(tagType)) {
        query =
            "SELECT FROM com.logistimo.inventory.entity.InvntryEvntLog"
                + " WHERE mId == mIdParam PARAMETERS Long mIdParam";
        params.put("mIdParam", entityId);
      } else if (TagUtil.TYPE_ENTITY.equals(tagType)) {
        query =
            "SELECT FROM com.logistimo.inventory.entity.InvntryEvntLog"
                + " WHERE kId == kIdParam PARAMETERS Long kIdParam";
        params.put("kIdParam", entityId);
      }
      PagedExec.exec(domainId, new QueryParams(query, params),
          new PageParams(null, PageParams.DEFAULT_SIZE), new PropagationProcessor(),
          fd.toJSONString(), null, 0, true);
    } catch (Exception e) {
      xLogger.severe(
          "{0} when propagating tags {1} of type {2} for InvntryEvntLog object {3} in domain {4}: {5}",
          e.getClass().getName(), tags, tagType, entityId, domainId, e.getMessage());
    }

    // Propagate to DemandItem
    try {
      if (TagUtil.TYPE_MATERIAL.equals(tagType)) {
        query =
            "SELECT FROM com.logistimo.orders.entity.DemandItem"
                + " WHERE mId == mIdParam PARAMETERS Long mIdParam";
        params.put("mIdParam", entityId);
      } else if (TagUtil.TYPE_ENTITY.equals(tagType)) {
        query =
            "SELECT FROM " + JDOUtils.getImplClass(IDemandItem.class).getName()
                + " WHERE kId == kIdParam PARAMETERS Long kIdParam";
        params.put("kIdParam", entityId);
      }
      PagedExec.exec(domainId, new QueryParams(query, params),
          new PageParams(null, PageParams.DEFAULT_SIZE), new PropagationProcessor(),
          fd.toJSONString(), null, 0, true);
    } catch (Exception e) {
      xLogger.severe(
          "{0} when propagating tags {1} of type {2} for DemandItem object {3} in domain {4}: {5}",
          e.getClass().getName(), tags, tagType, entityId, domainId, e.getMessage());
    }

    // Propagate to Order
    try {
      if (TagUtil.TYPE_ENTITY.equals(tagType)) {
        query =
            "SELECT FROM " + JDOUtils.getImplClass(IOrder.class).getName()
                + " WHERE kId == kIdParam PARAMETERS Long kIdParam";
        params.put("kIdParam", entityId);
      }
      PagedExec.exec(domainId, new QueryParams(query, params),
          new PageParams(null, PageParams.DEFAULT_SIZE), new PropagationProcessor(),
          fd.toJSONString(), null, 0, true);
    } catch (Exception e) {
      xLogger.severe(
          "{0} when propagating tags {1} of type {2} for Order object {3} in domain {4}: {5}",
          e.getClass().getName(), tags, tagType, entityId, domainId, e.getMessage());
    }

  }

  private static int getITagType(String tagType) {
    if (TagUtil.TYPE_ENTITY.equals(tagType)) {
      return ITag.KIOSK_TAG;
    } else if (TagUtil.TYPE_MATERIAL.equals(tagType)) {
      return ITag.MATERIAL_TAG;
    } else if (TagUtil.TYPE_ORDER.equals(tagType)) {
      return ITag.ORDER_TAG;
    }
    return 0;
  }

  public Object createKeyFromString(String encodedString) {
    try {
      return Long.parseLong(encodedString);
    } catch (NumberFormatException e) {
      return encodedString;
    }
  }

  @Override
  public List<String> getUploadedMessages(String uploadedKey) {
    xLogger.fine("Entered getUploadedMessages");
    List<String> msgs = null;
    PersistenceManager pm = PMF.get().getPersistenceManager();
    try {
      List<IUploadedMsgLog> msgLogs = getUploadMsgLog(uploadedKey, pm);
      if (msgLogs != null && !msgLogs.isEmpty()) {
        msgs = new ArrayList<>(msgLogs.size());
        for (IUploadedMsgLog uploadedMsgLog : msgLogs) {
          msgs.add(uploadedMsgLog.getMessage());
        }
      }

    } catch (Exception e) {
      xLogger.warn("Error getting msg logs for upload key :{0}", uploadedKey, e);
    } finally {
      pm.close();
    }

    xLogger.fine("Exiting getUploadedMessages");
    return msgs;
  }

  @Override
  public void deleteUploadedMessage(String uploadedKey) {
    xLogger.fine("Entered deleteUploadedMessage");
    PersistenceManager pm = null;
    try {
      pm = PMF.get().getPersistenceManager();
      IUploaded uploaded = JDOUtils.getObjectById(IUploaded.class, uploadedKey, pm);
      if (uploaded != null) {
        List<IUploadedMsgLog> msgLogs = getUploadMsgLog(uploadedKey, pm);
        if (msgLogs != null && !msgLogs.isEmpty()) {
          pm.deletePersistentAll(msgLogs);
        }
      }
    } catch (Exception e) {
      xLogger.warn("Error removing msg logs for upload key :{0}", uploadedKey, e);
    } finally {
      if (pm != null) {
        pm.close();
      }
    }
    xLogger.fine("Exiting deleteUploadedMessage");
  }

  @Override
  public void setCursorExtensions(String cursorStr, Query query) {
    //No action required for SQL
  }

  @Override
  public String getCursor(List results) {
    return null;
  }

  @Override
  public IMultiDomain getKioskDomains(Long kioskId) throws ServiceException {
    EntitiesService entitiesService = Services.getService(EntitiesServiceImpl.class);
    return entitiesService.getKiosk(kioskId);
  }

  @SuppressWarnings("unchecked")
  private List<IUploadedMsgLog> getUploadMsgLog(String uploadedKey, PersistenceManager pm) {
    Query query = pm.newQuery(JDOUtils.getImplClass(IUploadedMsgLog.class));
    try {
      query.setFilter("uploadedId == uploadedIdParam");
      query.declareParameters("String uploadedIdParam");
      List<IUploadedMsgLog> results = (List<IUploadedMsgLog>) query.execute(uploadedKey);
      return (List<IUploadedMsgLog>) pm.detachCopyAll(results);
    } finally {
      if (query != null) {
        query.closeAll();
      }
    }

  }

  public void removeBBItems(List<Long> bbItemIds) {
    xLogger.fine("Entered remove");
    if (bbItemIds == null || bbItemIds.isEmpty()) {
      xLogger.warn("Invalid inputs to remove items from Bulletin Board");
      return;
    }
    // Get the key list

    Iterator<Long> it = bbItemIds.iterator();

    PersistenceManager pm = null;
    // Get datastore service
    try {
      pm = PMF.get().getPersistenceManager();
      while (it.hasNext()) {
        Long id = it.next();
        try {
          Object object = pm.getObjectById(BBoard.class, id);
          pm.deletePersistent(object);
        } catch (Exception e) {
          xLogger.warn("Bulletin board item not found while deleting: {0} with error : {1}", id,
              e.getMessage(), e);
        }
      }

    } catch (Exception e) {
      xLogger.severe("{0} when deleting BB items with keys {1}: {2}", e.getClass().getName(),
          bbItemIds, e);
    } finally {
      if (pm != null) {
        pm.close();
      }
    }
    xLogger.fine("Exiting remove");
  }
  
  
}

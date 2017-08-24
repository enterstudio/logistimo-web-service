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

package com.logistimo.inventory.dao.impl;

import com.logistimo.constants.CharacterConstants;
import com.logistimo.constants.QueryConstants;
import com.logistimo.dao.JDOUtils;
import com.logistimo.entities.models.LocationSuggestionModel;
import com.logistimo.exception.InvalidDataException;
import com.logistimo.inventory.dao.IInvntryDao;
import com.logistimo.inventory.entity.IInvntry;
import com.logistimo.inventory.entity.IInvntryBatch;
import com.logistimo.inventory.entity.IInvntryEvntLog;
import com.logistimo.inventory.entity.IInvntryLog;
import com.logistimo.inventory.entity.Invntry;
import com.logistimo.inventory.entity.InvntryEvntLog;
import com.logistimo.inventory.models.InventoryFilters;
import com.logistimo.logger.XLog;
import com.logistimo.pagination.PageParams;
import com.logistimo.pagination.QueryParams;
import com.logistimo.pagination.Results;
import com.logistimo.services.ServiceException;
import com.logistimo.services.impl.PMF;
import com.logistimo.tags.dao.ITagDao;
import com.logistimo.tags.dao.TagDao;
import com.logistimo.tags.entity.ITag;
import com.logistimo.utils.LocalDateUtil;

import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.jdo.JDOObjectNotFoundException;
import javax.jdo.PersistenceManager;
import javax.jdo.Query;

/**
 * Created by charan on 03/03/15.
 */
@Component
public class InvntryDao implements IInvntryDao {

  private static XLog xLogger = XLog.getLog(InvntryDao.class);
  private ITagDao tagDao = new TagDao();

  public IInvntry getById(String id) {
    PersistenceManager pm = PMF.get().getPersistenceManager();
    try {
      return getById(id, pm);
    } finally {
      pm.close();
    }
  }

  public IInvntry getById(String id, PersistenceManager pm) {
    return pm.getObjectById(Invntry.class, Long.parseLong(id));
  }

  public String getInvKeyAsString(IInvntry invntry) {
    return String.valueOf(((Invntry) invntry).getKey());
  }

  public String getKeyString(Long kioskId, Long materialId) {
    IInvntry invntry = findId(kioskId, materialId);
    return invntry != null ? String.valueOf(((Invntry) invntry).getKey()) : null;
  }

  public IInvntry findId(Long kioskId, Long materialId) {
    PersistenceManager pm = PMF.get().getPersistenceManager();
    try {
      return findId(kioskId, materialId, pm);
    } finally {
      pm.close();
    }
  }

  public IInvntry findId(Long kioskId, Long materialId, PersistenceManager pm) {
    Query q = pm.newQuery(Invntry.class);
    try {
      q.setFilter("kId == " + kioskId.toString() + " && mId == " + materialId.toString());
      Object results = q.execute();
      if (results instanceof IInvntry) {
        results = pm.detachCopy(results);
        return (Invntry) results;
      } else if (results instanceof List) {
        if (!((List) results).isEmpty()) {
          Invntry result = ((List<Invntry>) results).get(0);
          result = pm.detachCopy(result);
          return result;
        }
      }
      return null;
    } finally {
      if (q != null) {
        q.closeAll();
      }
    }
  }

  public IInvntry findShortId(Long kioskId, Long shortId, PersistenceManager pm) {
    Query q = pm.newQuery(Invntry.class);
    try {
      q.setFilter("kId == " + kioskId.toString() + " && sId == " + shortId.toString());
      Object results = q.execute();
      if (results instanceof IInvntry) {
        results = pm.detachCopy(results);
        return (Invntry) results;
      } else if (results instanceof List) {
        if (!((List) results).isEmpty()) {
          Invntry result = ((List<Invntry>) results).get(0);
          result = pm.detachCopy(result);
          return result;
        }
      }
      return null;
    } finally {
      if (q != null) {
        q.closeAll();
      }
    }
  }


  public IInvntry getInvntry(IInvntryEvntLog invEventLog) {
    PersistenceManager pm = PMF.get().getPersistenceManager();
    try {
      IInvntry invntry = pm.getObjectById(Invntry.class, ((InvntryEvntLog) invEventLog).getInvId());
      invntry = pm.detachCopy(invntry);
      return invntry;
    } finally {
      pm.close();
    }
  }

  public Invntry getDBInvntry(IInvntry invntry, PersistenceManager pm) {
    return pm.getObjectById(Invntry.class, ((Invntry) invntry).getKey());
  }

  public IInvntry getDBInvntry(IInvntry invntry) {
    PersistenceManager pm = PMF.get().getPersistenceManager();
    IInvntry in = null;
    try {
      in = getDBInvntry(invntry, pm);
    } catch (JDOObjectNotFoundException e) {
      // ignore
    } finally {
      pm.close();
    }
    return in;
  }


  public IInvntryEvntLog getInvntryEvntLog(IInvntry iInvntry) {
    Invntry invntry = (Invntry) iInvntry;
    if (invntry.getLastStockEvent() != null) {
      PersistenceManager pm = PMF.get().getPersistenceManager();
      try {
        IInvntryEvntLog
            invLog =
            pm.getObjectById(InvntryEvntLog.class, invntry.getLastStockEvent());
        invLog = pm.detachCopy(invLog);
        return invLog;
      } catch (Exception e) {
        return null;
      } finally {
        pm.close();
      }
    }
    return null;
  }

  public void createInvntryEvntLog(int type, IInvntry iInv) {
    Invntry inv = (Invntry) iInv;
    PersistenceManager pm = PMF.get().getPersistenceManager();
    try {
      InvntryEvntLog
          invEventLog =
          new InvntryEvntLog(type,
              inv); /// new InvntryEvntLog( type, inv.getDomainId(), inv.getKioskId(), inv.getMaterialId(), inv.getTimestamp(), null );
      pm.makePersistent(invEventLog);
      inv.setLastStockEvent(invEventLog.getKey());
    } finally {
      pm.close();
    }
  }

  public String getStockEventWarning(IInvntry inv, Locale locale, String timezone) {
    String txt = "";
    int eventType = inv.getStockEvent();
    if (eventType != -1) {
      IInvntryEvntLog invEventLog = getInvntryEvntLog(inv);
      if (invEventLog != null && eventType == invEventLog.getType()) {
        txt =
            "<b>" + LocalDateUtil.getFormattedMillisInHoursDays(
                (new Date().getTime() - invEventLog.getStartDate().getTime()), locale) + "</b>";
      }
    }
    return txt;
  }

  public void setInvntryLogKey(IInvntryLog invntryLog) {

  }

  public void setInvBatchKey(IInvntryBatch invBatch) {

  }

  public IInvntryBatch findInvBatch(Long kioskId, Long materialId, String batchId) {
    PersistenceManager pm = PMF.get().getPersistenceManager();
    try {
      return findInvBatch(kioskId, materialId, batchId, pm);
    } finally {
      pm.close();
    }
  }

  public IInvntryBatch findInvBatch(Long kioskId, Long materialId, String batchId,
                                    PersistenceManager pm) {
    if (batchId == null) {
      throw new IllegalArgumentException("Batch Id cannot be null");
    }
    Query
        q =
        pm.newQuery("SELECT FROM " + JDOUtils.getImplClassName(IInvntryBatch.class)
            + " WHERE kId == kIdParam && " +
            "mId == mIdParam && bid == bIdParam PARAMETERS Long kIdParam, Long mIdParam, String bIdParam");
    Map<String, Object> params = new HashMap<>();
    params.put("kIdParam", kioskId);
    params.put("mIdParam", materialId);
    params.put("bIdParam", batchId.toUpperCase());
    try {
      q.setUnique(true);
      IInvntryBatch result = (IInvntryBatch) q.executeWithMap(params);
      return pm.detachCopy(result);
    } finally {
      if (q != null) {
        q.closeAll();
      }
    }
  }

  public IInvntryEvntLog getLastStockEvent(IInvntry inv, PersistenceManager pm) {
    Long key = ((Invntry) inv).getLastStockEvent();
    if (key == null) {
      return null;
    }
    try {
      // Get the stock event object of this type
      return pm.getObjectById(InvntryEvntLog.class, key);
    } catch (Exception e) {
      xLogger.warn(
          "{0} when getting inv. event log for stock-replensishment event mid-kid {1}-{2} in domain {3} for key {4}: {5}",
          e.getClass().getName(), inv.getMaterialId(), inv.getKioskId(), inv.getDomainId(),
          key.toString(), e);
    }
    return null;
  }

  @Override
  public List<IInvntryEvntLog> getInvntryEvntLog(Long invId, int size, int offset) {
    PersistenceManager pm = PMF.get().getPersistenceManager();
    List<IInvntryEvntLog> results;
    try {
      Query q = pm.newQuery(InvntryEvntLog.class);
      q.setFilter("invId == invIdParam");
      Map<String, Object> params = new HashMap<>();
      params.put("invIdParam", invId);
      q.declareParameters("Long invIdParam");
      q.setOrdering("sd desc");
      q.setRange(offset, offset + size);
      results = (List<IInvntryEvntLog>) q.executeWithMap(params);
      results = (List<IInvntryEvntLog>) pm.detachCopyAll(results);
    } finally {
      pm.close();
    }
    return results;
  }

  @Override
  public List<IInvntryEvntLog> getInvEventLogs(Long kioskId, Long materialId, Date start, Date end,
                                               PersistenceManager pm) {
    boolean useLocalPm = false;
    if (pm == null) {
      pm = PMF.get().getPersistenceManager();
      useLocalPm = true;
    }
    Query query = null;
    try {
      String queryStr = "SELECT FROM " + JDOUtils.getImplClass(IInvntryEvntLog.class).getName()
          + " WHERE mId == mIdParam && kId == kIdParam && ( ed > sdParam || ed == nullParam ) && sd < edParam"
          + " PARAMETERS String nullParam, Long mIdParam, Long kIdParam, Date sdParam ,Date edParam import java.util.Date;";
      Map<String, Object> params = new HashMap<>(5);
      params.put("mIdParam", materialId);
      params.put("kIdParam", kioskId);
      params.put("sdParam", start);
      params.put("edParam", end != null ? end : new Date());
      params.put("nullParam", null);

      query = pm.newQuery(queryStr);
      query.setResultClass(InvntryEvntLog.class);
      List<IInvntryEvntLog> results = (List<IInvntryEvntLog>) query.executeWithMap(params);
      return (List<IInvntryEvntLog>) pm.detachCopyAll(results);
    } finally {
      if (query != null) {
        query.closeAll();
      }
      if (useLocalPm) {
        pm.close();
      }
    }
  }


  @Override
  public List<IInvntryEvntLog> removeInvEventLogs(Long kioskId, Long materialId, Date start,
                                                  Date end,
                                                  PersistenceManager pm) {
    boolean useLocalPm = false;
    if (pm == null) {
      pm = PMF.get().getPersistenceManager();
      useLocalPm = true;
    }
    try {
      List<IInvntryEvntLog> results = getInvEventLogs(kioskId, materialId, start, end, pm);
      if (results != null && !results.isEmpty()) {
        pm.deletePersistentAll(results);
      }
      return results;
    } finally {
      if (useLocalPm) {
        pm.close();
      }
    }
  }

  @Override
  public QueryParams buildInventoryQuery(InventoryFilters filters, boolean countQuery)
      throws InvalidDataException {
    StringBuilder
        queryBuilder =
        new StringBuilder("SELECT I.`KEY` AS `KEY`, I.* FROM INVNTRY I, KIOSK K, MATERIAL M");
    if (!filters.getEventTypes().isEmpty()) {
      queryBuilder.append(", INVNTRYEVNTLOG IE");
    }
    queryBuilder.append(" WHERE I.KID = K.KIOSKID AND I.MID = M.MATERIALID");
    if (!filters.getEventTypes().isEmpty()) {
      queryBuilder.append(" AND I.LSEV = IE.`KEY`");
    }
    List<String> params = new ArrayList<>();

    if (filters.getKioskId() != null) {
      queryBuilder.append(" AND I.KID = ?");
      params.add(String.valueOf(filters.getKioskId()));
    } else if (filters.getKioskTags() != null && !filters.getKioskTags().isEmpty()) {
      queryBuilder.append(" AND I.KID in (SELECT KIOSKID from KIOSK_TAGS where ID in (");
      for (String tag : filters.getKioskTags()) {
        queryBuilder.append(CharacterConstants.QUESTION).append(CharacterConstants.COMMA);
        params.add(String.valueOf(tagDao.getTagFilter(tag, ITag.KIOSK_TAG)));
      }
      queryBuilder.setLength(queryBuilder.length() - 1);
      queryBuilder.append(CharacterConstants.C_BRACKET).append(CharacterConstants.C_BRACKET);
    } else if (filters.getExcludedKioskTags() != null && !filters.getExcludedKioskTags()
        .isEmpty()) {
      queryBuilder.append(" AND I.KID NOT in (SELECT KIOSKID from KIOSK_TAGS where ID in (");
      for (String tag : filters.getExcludedKioskTags()) {
        queryBuilder.append(CharacterConstants.QUESTION).append(CharacterConstants.COMMA);
        params.add(String.valueOf(tagDao.getTagFilter(tag, ITag.KIOSK_TAG)));
      }
      queryBuilder.setLength(queryBuilder.length() - 1);
      queryBuilder.append(CharacterConstants.C_BRACKET).append(CharacterConstants.C_BRACKET);
    } else if (filters.getKioskIds() != null && !filters.getKioskIds().isEmpty()) {
      queryBuilder.append(" AND I.KID in (");
      for (Long id : filters.getKioskIds()) {
        queryBuilder.append(CharacterConstants.QUESTION).append(CharacterConstants.COMMA);
        params.add(String.valueOf(id));
      }
      queryBuilder.setLength(queryBuilder.length() - 1);
      queryBuilder.append(CharacterConstants.C_BRACKET);
    }
    if (filters.getMatType() != IInvntry.ALL && (filters.getMatType() == IInvntry.BATCH_ENABLED
        || filters.getMatType() == IInvntry.BATCH_DISABLED)) {
      queryBuilder.append(" AND MID IN (SELECT MATERIALID FROM MATERIAL WHERE BM = ").append(CharacterConstants.QUESTION);
      if (filters.getMatType() == IInvntry.BATCH_ENABLED) {
        params.add(String.valueOf(filters.getMatType()));
      } else {
        params.add(String.valueOf(IInvntry.ALL));
      }
      queryBuilder.append(CharacterConstants.C_BRACKET);
    }
    // Add the materialId param, if present
    if (filters.getMaterialId() != null) {
      queryBuilder.append(" AND I.MID = ?");
      params.add(String.valueOf(filters.getMaterialId()));
    } else {
      if (filters.getMaterialTags() != null && !filters.getMaterialTags().isEmpty()) {
        queryBuilder.append(" AND I.MID in (SELECT MATERIALID from MATERIAL_TAGS where (");
        for (int i = 0; i < filters.getMaterialTags().size(); i++) {
          String tag = filters.getMaterialTags().get(i);
          if(i>0){
            queryBuilder.append(" OR ");
          }
          queryBuilder.append("ID = ?");
          params.add(String.valueOf(tagDao.getTagFilter(tag, ITag.MATERIAL_TAG)));
        }
        queryBuilder.append(CharacterConstants.C_BRACKET).append(CharacterConstants.C_BRACKET);
      }
      if (!StringUtils.isEmpty(filters.getMaterialNameStartsWith())) {
        queryBuilder.append(" AND M.UNAME LIKE ?");
        params.add(filters.getMaterialNameStartsWith() + CharacterConstants.PERCENT);
      }
    }

    if (filters.getDomainId() != null) {
      queryBuilder.append(" AND KID IN (SELECT KIOSKID_OID FROM KIOSK_DOMAINS WHERE DOMAIN_ID= ? )");
      params.add(String.valueOf(filters.getDomainId()));
    } else if (filters.getSourceDomainId() != null) {
      queryBuilder.append(" AND K.SDID = ?");
      params.add(String.valueOf(filters.getSourceDomainId()));
    }
    LocationSuggestionModel location = filters.getLocation();
    if(location != null && location.isNotEmpty()){
      if(StringUtils.isNotEmpty(location.state)){
        queryBuilder.append(" AND K.STATE = ? ");
        params.add(location.state);
      }
      if(StringUtils.isNotEmpty(location.district)){
        queryBuilder.append(" AND K.DISTRICT = ? ");
        params.add(location.district);
      }
      if(StringUtils.isNotEmpty(location.taluk)){
        queryBuilder.append(" AND K.TALUK = ? ");
        params.add(location.taluk);
      }
    }
    if (filters.isOnlyNonZeroStk()) {
      queryBuilder.append(" AND I.STK > 0");
    }
    if (StringUtils.isNotEmpty(filters.getPdos())) {
      queryBuilder.append(" AND I.PDOS <=").append(CharacterConstants.QUESTION);
      params.add(filters.getPdos());
    }
    if (!filters.getEventTypes().isEmpty()) {
      queryBuilder.append(" AND IE.ED IS NULL AND IE.TY in (");
      for (Integer id : filters.getEventTypes()) {
        queryBuilder.append(CharacterConstants.QUESTION).append(CharacterConstants.COMMA);
        params.add(String.valueOf(id));
      }
      queryBuilder.setLength(queryBuilder.length() - 1);
      queryBuilder.append(CharacterConstants.C_BRACKET);
    }
    if (filters.isNoInTransitStock()) {
      queryBuilder.append(" AND (I.TSTK <= 0 OR I.TSTK IS NULL)");
    }

    String orderByStr = " ORDER BY K.NAME ASC, M.NAME ASC";
    queryBuilder.append(orderByStr);

    if (countQuery) {
      String
          cntQueryStr =
          queryBuilder.toString().replace("I.`KEY` AS `KEY`, I.*", QueryConstants.ROW_COUNT)
              .replace(orderByStr, CharacterConstants.EMPTY);
      return new QueryParams(cntQueryStr, params, QueryParams.QTYPE.SQL,
          IInvntry.class);
    }
    return new QueryParams(queryBuilder.toString(), params, QueryParams.QTYPE.SQL,
        IInvntry.class);
  }


  @Override
  public Results getInventory(InventoryFilters inventoryFilters, PageParams pageParams,
                              PersistenceManager pm)
      throws ServiceException {
    Query query = null;
    Query cntQuery = null;
    List<Invntry> inventoryList = null;
    int count = 0;
    try {
      QueryParams
          sqlQueryModel = buildInventoryQuery(inventoryFilters, false);
      String executeQuery = sqlQueryModel.query;
      if (pageParams != null) {
        executeQuery += " LIMIT " + pageParams.getOffset() + CharacterConstants.COMMA
            + pageParams.getSize();
      }
      query = pm.newQuery("javax.jdo.query.SQL", executeQuery);
      query.setClass(Invntry.class);
      inventoryList = (List<Invntry>) query.executeWithArray(
          sqlQueryModel.listParams.toArray());
      inventoryList = (List<Invntry>) pm.detachCopyAll(inventoryList);
      QueryParams cntSqlQueryModel = buildInventoryQuery(inventoryFilters, true);
      cntQuery = pm.newQuery("javax.jdo.query.SQL", cntSqlQueryModel.query);
      count =
          ((Long) ((List) cntQuery.executeWithArray(cntSqlQueryModel.listParams.toArray())).iterator().next())
              .intValue();

    } catch (Exception e){
      xLogger.severe("Error while reading inventory data", e);
    } finally {
      if (query != null) {
        query.closeAll();
      }
      if (cntQuery != null) {
        cntQuery.closeAll();
      }
    }
    return new Results(inventoryList, null, count,
        pageParams == null ? 0 : pageParams.getOffset());
  }

  @Override
  public boolean validateEntityBatchManagementUpdate(Long kioskId, PersistenceManager pm) throws ServiceException {
    if (kioskId == null) {
      throw new ServiceException("Invalid or null kioskId {0} while changing batch management on entity", kioskId);
    }
    boolean useLocalPm = false;
    if (pm == null) {
      pm = PMF.get().getPersistenceManager();
      useLocalPm = true;
    }

    Query query = null;
    List<String> parameters = new ArrayList<>(1);
    try {
      StringBuilder sqlQuery = new StringBuilder("SELECT 1 FROM INVNTRY I, MATERIAL M WHERE I.MID = M.MATERIALID AND M.BM = 1 AND I.STK > 0 AND I.KID = ").append(CharacterConstants.QUESTION);
      parameters.add(String.valueOf(kioskId));
      sqlQuery.append( " LIMIT 1");
      query = pm.newQuery("javax.jdo.query.SQL", sqlQuery.toString());
      query.setUnique(true);
      Long hasInv = (Long) query.executeWithArray(parameters.toArray());
      return (hasInv == null);
    } finally {
      if (query != null) {
        try {
          query.closeAll();
        } catch (Exception ignored) {
          xLogger.warn("Exception while closing query", ignored);
        }
      }
      if (useLocalPm) {
        pm.close();
      }
    }
  }

  @Override
  public boolean validateMaterialBatchManagementUpdate(Long materialId, PersistenceManager pm) throws ServiceException {
    if (materialId == null) {
      throw new ServiceException("Invalid or null materialId {0} while changing batch management on material", materialId);
    }
    boolean useLocalPm = false;
    if (pm == null) {
      pm = PMF.get().getPersistenceManager();
      useLocalPm = true;
    }

    Query query = null;
    List<String> parameters = new ArrayList<>(1);
    try {
      StringBuilder sqlQuery = new StringBuilder("SELECT 1 FROM INVNTRY I, MATERIAL M WHERE I.MID = M.MATERIALID AND M.MATERIALID = ").append(CharacterConstants.QUESTION);
      parameters.add(String.valueOf(materialId));
      sqlQuery.append(" AND I.STK > 0 LIMIT 1");
      query = pm.newQuery("javax.jdo.query.SQL", sqlQuery.toString());
      query.setUnique(true);
      Long hasInv = (Long) query.executeWithArray(parameters.toArray());
      return (hasInv == null);
    } finally {
      if (query != null) {
        try {
          query.closeAll();
        } catch (Exception ignored) {
          xLogger.warn("Exception while closing query", ignored);
        }
      }
      if (useLocalPm) {
        pm.close();
      }
    }
  }
}

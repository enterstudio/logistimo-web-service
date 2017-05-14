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

import com.logistimo.inventory.dao.ITransDao;
import com.logistimo.inventory.entity.ITransaction;
import com.logistimo.inventory.entity.Transaction;
import com.logistimo.pagination.QueryParams;
import com.logistimo.tags.dao.ITagDao;
import com.logistimo.tags.dao.TagDao;
import com.logistimo.tags.entity.ITag;

import com.logistimo.pagination.PageParams;
import com.logistimo.pagination.Results;
import com.logistimo.services.impl.PMF;
import com.logistimo.constants.CharacterConstants;
import com.logistimo.utils.LocalDateUtil;
import com.logistimo.constants.QueryConstants;
import com.logistimo.logger.XLog;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.jdo.PersistenceManager;
import javax.jdo.Query;

/**
 * Created by charan on 03/03/15.
 */
public class TransDao implements ITransDao {

  private static final XLog xLogger = XLog.getLog(ITransDao.class);
  private ITagDao tagDao = new TagDao();

  @Override
  public String getKeyAsString(ITransaction transaction) {
    return String.valueOf(((Transaction) transaction).getKey());
  }

  /**
   * Not required for SQL mode..
   */
  @Override
  public void setKey(ITransaction transaction) {

  }

  @Override
  public ITransaction getById(String id) {
    PersistenceManager persistenceManager = PMF.get().getPersistenceManager();
    try {
      return getById(id, persistenceManager);
    } finally {
      persistenceManager.close();
    }
  }

  @Override
  public ITransaction getById(String id, PersistenceManager persistenceManager) {
    return persistenceManager.getObjectById(Transaction.class, Long.valueOf(id));
  }

  @Override
  public void linkTransactions(ITransaction destination, ITransaction source) {
    ((Transaction) destination).setLinkedTransactionId(((Transaction) source).getKey());
    destination.setTrackingId(String.valueOf(source.getTransactionId()));
    destination.setTrackingObjectType(ITransaction.TYPE_TRANSFER);
  }

  @Override
  public Results getInventoryTransactions(Date sinceDate, Date untilDate, Long domainId,
                                          Long kioskId,
                                          Long materialId, List<String> transTypes,
                                          Long linkedKioskId, String kioskTag,
                                          String materialTag, List<Long> kioskIds,
                                          PageParams pageParams, String bid,
                                          boolean atd, String reason, List<String> excludeReasons) {
    PersistenceManager pm = PMF.get().getPersistenceManager();
    Query query = null;
    Query cntQuery = null;
    Results res;
    try{
      QueryParams queryParams = buildTransactionsQuery(sinceDate, untilDate, domainId,
          kioskId, materialId, transTypes, linkedKioskId, kioskTag, materialTag, kioskIds,
          bid, atd, reason, excludeReasons);
      StringBuilder sqlQuery = new StringBuilder(queryParams.query);
      final String orderBy = " ORDER BY T DESC";
      String limitStr = null;
      if (pageParams != null) {
        limitStr =
            " LIMIT " + pageParams.getOffset() + CharacterConstants.COMMA + pageParams.getSize();
        sqlQuery.append(limitStr);
      }
      query = pm.newQuery("javax.jdo.query.SQL", sqlQuery.toString());
      query.setClass(Transaction.class);
      List<Transaction> trans = (List<Transaction>) query.executeWithArray(queryParams.listParams.toArray());
      trans = (List<Transaction>) pm.detachCopyAll(trans);
      String
          cntQueryStr =
          sqlQuery.toString().replace("*", QueryConstants.ROW_COUNT)
              .replace(orderBy, CharacterConstants.EMPTY);
      if (limitStr != null) {
        cntQueryStr = cntQueryStr.replace(limitStr, CharacterConstants.EMPTY);
      }
      cntQuery = pm.newQuery("javax.jdo.query.SQL", cntQueryStr);
      int
          count =
          ((Long) ((List) cntQuery.executeWithArray(queryParams.listParams.toArray())).iterator().next())
              .intValue();
      res = new Results(trans, null, count, pageParams == null ? 0 : pageParams.getOffset());
    } finally {
      if (query != null) {
        try {
          query.closeAll();
        } catch (Exception ignored) {
          xLogger.warn("Exception while closing query", ignored);
        }
      }
      if (cntQuery != null) {
        try {
          cntQuery.closeAll();
        } catch (Exception ignored) {
          xLogger.warn("Exception while closing query", ignored);
        }
      }
      pm.close();

    }
    return res;

  }

  @Override
  public QueryParams buildTransactionsQuery(Date sinceDate, Date untilDate, Long domainId,
                                                      Long kioskId,
                                                      Long materialId, List<String> transTypes,
                                                      Long linkedKioskId, String kioskTag,
                                                      String materialTag, List<Long> kioskIds,
                                                      String bid,
                                                      boolean atd, String reason,
                                                      List<String> excludeReasons) {

    List<String> parameters = new ArrayList<>(1);
    StringBuilder sqlQuery = new StringBuilder("SELECT * FROM TRANSACTION");
    if (kioskId != null) {
      sqlQuery.append(" WHERE KID IN (").append(CharacterConstants.QUESTION);
      parameters.add(String.valueOf(kioskId));
    } else if (kioskIds != null && !kioskIds.isEmpty()) {
      sqlQuery.append(" WHERE KID IN (");
      for (Long id : kioskIds) {
        sqlQuery.append(CharacterConstants.QUESTION).append(CharacterConstants.COMMA);
        parameters.add(String.valueOf(id));
      }
      sqlQuery.setLength(sqlQuery.length() - 1);
    } else if (domainId != null) {
      sqlQuery.append(" WHERE KID IN (SELECT KIOSKID_OID FROM KIOSK_DOMAINS WHERE DOMAIN_ID=")
          .append(CharacterConstants.QUESTION);
      parameters.add(String.valueOf(domainId));
    }
    if (kioskId == null && kioskTag != null && !kioskTag.isEmpty()) {
      sqlQuery.append(" AND KIOSKID_OID IN (SELECT KIOSKID from KIOSK_TAGS where ID = ")
          .append(CharacterConstants.QUESTION)
          .append(")");
      parameters.add(String.valueOf(tagDao.getTagFilter(kioskTag, ITag.KIOSK_TAG)));
    }
    sqlQuery.append(") ");
    if (materialId != null) {
      sqlQuery.append(" AND MID = ").append(CharacterConstants.QUESTION);
      parameters.add(String.valueOf(materialId));
    } else if (materialTag != null && !materialTag.isEmpty()) {
      sqlQuery.append(" AND MID IN (SELECT MATERIALID from MATERIAL_TAGS where ID = ")
          .append(CharacterConstants.QUESTION)
          .append(")");
      parameters.add(String.valueOf(tagDao.getTagFilter(materialTag, ITag.MATERIAL_TAG)));
    }
    if (transTypes != null && transTypes.size() > 0) {
      if (transTypes.size() == 1) {
        sqlQuery.append(" AND TYPE = ").append(CharacterConstants.QUESTION);
        parameters.add(transTypes.get(0));
      } else {
        sqlQuery.append(" AND TYPE IN (");
        for (String transType : transTypes) {
          sqlQuery.append(CharacterConstants.QUESTION)
              .append(CharacterConstants.COMMA);
          parameters.add(transType);
        }
        sqlQuery.setLength(sqlQuery.length() - 1);
        sqlQuery.append(CharacterConstants.C_BRACKET);
      }
    }
    if (linkedKioskId != null) {
      sqlQuery.append(" AND LKID = ").append(CharacterConstants.QUESTION);
      parameters.add(String.valueOf(linkedKioskId));
    }
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    if (sinceDate != null) {
      if (atd) {
        sqlQuery.append(" AND ATD >= TIMESTAMP(").append(CharacterConstants.QUESTION).append(")");
      } else {
        sqlQuery.append(" AND T >= TIMESTAMP(").append(CharacterConstants.QUESTION).append(")");
      }
      parameters.add(sdf.format(sinceDate));
    }
    if (untilDate != null) {
      untilDate = LocalDateUtil.getOffsetDate(untilDate, 1, Calendar.DAY_OF_MONTH);
      if (atd) {
        sqlQuery.append(" AND ATD < TIMESTAMP(").append(CharacterConstants.QUESTION).append(")");
      } else {
        sqlQuery.append(" AND T < TIMESTAMP(").append(CharacterConstants.QUESTION).append(")");
      }
      parameters.add(sdf.format(untilDate));
    }
    if (bid != null) {
      sqlQuery.append(" AND BID = ").append(CharacterConstants.QUESTION);
      parameters.add(bid);
    }
    if (reason != null) {
      sqlQuery.append(" AND RS = ").append(CharacterConstants.QUESTION);
      parameters.add(reason);
    }
    if (excludeReasons != null && excludeReasons.size() > 0) {
      if (excludeReasons.size() == 1) {
        sqlQuery
            .append(" AND (RS IS NULL OR RS <> ")
            .append(CharacterConstants.QUESTION)
            .append(CharacterConstants.C_BRACKET);;
        parameters.add(excludeReasons.get(0));
      } else {
        sqlQuery.append(" AND ( RS IS NULL OR RS NOT IN (");
        for (String excludeReason : excludeReasons) {
          sqlQuery.append(CharacterConstants.QUESTION)
              .append(CharacterConstants.COMMA);
          parameters.add(excludeReason);
        }
        sqlQuery.append(sqlQuery.length() - 1);
        sqlQuery.append(CharacterConstants.C_BRACKET).append(CharacterConstants.C_BRACKET);
      }
    }
    final String orderBy = " ORDER BY T DESC";
    sqlQuery.append(orderBy);
    return new QueryParams(sqlQuery.toString(), parameters,
        QueryParams.QTYPE.SQL, ITransaction.class);
  }
}

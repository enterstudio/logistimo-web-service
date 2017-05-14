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
 *
 */
package com.logistimo.reports.generators;

import com.logistimo.auth.SecurityConstants;
import com.logistimo.config.models.DomainConfig;
import com.logistimo.constants.CharacterConstants;
import com.logistimo.constants.Constants;
import com.logistimo.dao.JDOUtils;
import com.logistimo.entities.models.LocationSuggestionModel;
import com.logistimo.entities.service.EntitiesService;
import com.logistimo.entities.service.EntitiesServiceImpl;
import com.logistimo.inventory.entity.IInvntry;
import com.logistimo.inventory.entity.IInvntryEvntLog;
import com.logistimo.logger.XLog;
import com.logistimo.pagination.PageParams;
import com.logistimo.pagination.QueryParams;
import com.logistimo.reports.ReportsConstants;
import com.logistimo.services.Services;
import com.logistimo.services.impl.PMF;
import com.logistimo.tags.dao.ITagDao;
import com.logistimo.tags.dao.TagDao;
import com.logistimo.tags.entity.ITag;
import com.logistimo.users.entity.IUserAccount;
import com.logistimo.users.service.UsersService;
import com.logistimo.users.service.impl.UsersServiceImpl;
import com.logistimo.utils.LocalDateUtil;
import com.logistimo.utils.QueryUtil;
import org.apache.commons.lang.StringUtils;
import org.json.JSONObject;

import javax.jdo.PersistenceManager;
import javax.jdo.Query;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @author Arun
 */
public class StockEventDataGenerator implements ReportDataGenerator {

  private static final XLog xLogger = XLog.getLog(StockEventDataGenerator.class);
  private ITagDao tagDao = new TagDao();

  @SuppressWarnings("unchecked")
  @Override
  public ReportData getReportData(Date from, Date until, String frequency,
                                  Map<String, Object> filters, Locale locale, String timezone,
                                  PageParams pageParams, DomainConfig dc, String sourceUserId)
      throws ReportingDataException {
    xLogger.fine("Entered StockEventDataGenerator.getReportData");
    // Execute query
    PersistenceManager pm = PMF.get().getPersistenceManager();
    // Query q = pm.newQuery( queryStr );
    boolean isAbnormalStockReport =
        ((filters.get(ReportsConstants.FILTER_ABNORMALSTOCKVIEW) != null)
            && (boolean) filters.get(ReportsConstants.FILTER_ABNORMALSTOCKVIEW));
    List results = null;
    String cursor = null;

    QueryParams queryParams =
        getReportQuery(
            from, until, frequency, filters, locale, timezone, pageParams, dc, sourceUserId);
    Query q = null;
    if (isAbnormalStockReport) {
      q = pm.newQuery("javax.jdo.query.SQL", queryParams.query);
      q.setClass(JDOUtils.getImplClass(IInvntry.class));
    } else {
      q = pm.newQuery(queryParams.query);
    }
    if (pageParams != null) {
      QueryUtil.setPageParams(q, pageParams);
    }
    xLogger.fine("Query: {0}, QueryParams: {1}", q, queryParams.params);
    try {
      // results = (List<InvntryEvntLog>) q.executeWithMap( params );
      results = (List<IInvntryEvntLog>) q.executeWithMap(queryParams.params);
      if (results != null) {
        results.size();
        cursor = QueryUtil.getCursor(results);
        results = (List<IInvntryEvntLog>) pm.detachCopyAll(results);
      }
    } finally {
      try {
        q.closeAll();
      } catch (Exception ignored) {
        xLogger.warn("Exception while closing query", ignored);
      }
      pm.close();
    }

    xLogger.fine("Exiting StockEventDataGenerator.getReportData");
    return new StockEventData(from, until, filters, locale, timezone, results, cursor);
  }

  @SuppressWarnings("unchecked")
  @Override
  public QueryParams getReportQuery(Date from, Date until, String frequency,
                                    Map<String, Object> filters, Locale locale, String timezone,
                                    PageParams pageParams, DomainConfig dc, String sourceUserId) {
      if (filters == null || filters.isEmpty()) {
          throw new IllegalArgumentException("Filters not specified");
      }
      boolean isAbnormalStockReport =
              ((filters.get(ReportsConstants.FILTER_ABNORMALSTOCKVIEW) != null)
                      && (boolean) filters.get(ReportsConstants.FILTER_ABNORMALSTOCKVIEW));
      if(isAbnormalStockReport){
          return getReportSqlQuery(from,until,filters,locale,dc,sourceUserId,pageParams);
      }
      Integer eventType = (Integer) filters.get(ReportsConstants.FILTER_EVENT);
      Long domainId = (Long) filters.get(ReportsConstants.FILTER_DOMAIN);
      Long kioskId = (Long) filters.get(ReportsConstants.FILTER_KIOSK);
      Long materialId = (Long) filters.get(ReportsConstants.FILTER_MATERIAL);
      String kioskTag = (String) filters.get(ReportsConstants.FILTER_KIOSKTAG);
      String materialTag = (String) filters.get(ReportsConstants.FILTER_MATERIALTAG);
      boolean outstandingEvents = filters.get(ReportsConstants.FILTER_LATEST) != null;
      boolean
              ascendingOrder =
              filters.get(ReportsConstants.SORT_ASC) == null || (boolean) filters.get(ReportsConstants.SORT_ASC);
      // Event type is optional.
                /*if ( eventType == null )
                        throw new IllegalArgumentException( "Event type not specified" );
			*/
      if (kioskId == null && materialId == null && domainId == null) {
          throw new IllegalArgumentException("Neither kioskId, materialId nor domainId specified");
      }
      // Get kiosks IDs that should be filtered (for managers)
      List<Long> kioskIds = null;
      boolean hasKioskIds = false;
      if (sourceUserId != null) {
          try {
              // Get user
            UsersService as = Services.getService(UsersServiceImpl.class, locale);
            EntitiesService es = Services.getService(EntitiesServiceImpl.class, locale);
              IUserAccount u = as.getUserAccount(sourceUserId);
              if (SecurityConstants.ROLE_SERVICEMANAGER.equals(u.getRole())) {
                  kioskIds =
                          es.getKioskIdsForUser(sourceUserId, null, null).getResults(); // TODO: pagination?
                  hasKioskIds = true;
              }
          } catch (Exception e) {
              throw new IllegalArgumentException(e.getMessage());
          }
      }
      // Form query
      String
              queryStr =
              "SELECT FROM " + JDOUtils.getImplClass(IInvntryEvntLog.class).getName() + " WHERE ";
      String declaration = " PARAMETERS ";
      String filterStr = "";
      String importStr = "";
      String orderBy = " ORDER BY sd " + (ascendingOrder ? "ASC" : "DESC");
      Map<String, Object> params = new HashMap<String, Object>();
      if (eventType != null) {
          filterStr += "ty == tyParam";
          declaration += " Integer tyParam";
          params.put("tyParam", eventType);
      }
      if (kioskId != null) {
          if (!filterStr.isEmpty()) {
              filterStr += " && ";
              declaration += ",";
          }
          filterStr += " kId == kIdParam";
          declaration += "Long kIdParam";
          params.put("kIdParam", kioskId);
      } else if (hasKioskIds) {
          if (!filterStr.isEmpty()) {
              filterStr += " && ";
              declaration += ",";
          }
          filterStr += " kioskIds.contains( kId )";
          declaration += "java.util.Collection kioskIds";
          params.put("kioskIds", kioskIds);
      }
      if (materialId != null) {
          if (!filterStr.isEmpty()) {
              filterStr += " && ";
              declaration += ",";
          }
          filterStr += "mId == mIdParam";
          declaration += "Long mIdParam";
          params.put("mIdParam", materialId);
      }
      if (kioskId == null && !hasKioskIds && materialId == null && domainId != null) {
          if (!filterStr.isEmpty()) {
              filterStr += " && ";
              declaration += ",";
          }
          filterStr += "dId.contains(dIdParam)";
          declaration += "Long dIdParam";
          params.put("dIdParam", domainId);
          ///orderBy = " ORDER BY dr DESC";
      }
      // Tags, if any
      if (materialId == null && materialTag != null && !materialTag.isEmpty()) {
          filterStr += " && mtgs.contains(mtgsParam)";
          declaration += ",Long mtgsParam";
          params.put("mtgsParam", tagDao.getTagFilter(materialTag, ITag.MATERIAL_TAG));
      } else if (kioskId == null && kioskTag != null && !kioskTag.isEmpty()) {
          filterStr += " && ktgs.contains(ktgsParam)";
          declaration += ",Long ktgsParam";
          params.put("ktgsParam", tagDao.getTagFilter(kioskTag, ITag.KIOSK_TAG));
      }
      // Add from date
      if (from != null) {
          filterStr += " && sd > fromParam";
          declaration += ",Date fromParam";
          importStr = " import java.util.Date; ";
          params.put("fromParam", LocalDateUtil.getOffsetDate(from, -1, Calendar.MILLISECOND));
      }
      // Add until date, if needed
      if (outstandingEvents) {
          filterStr += " && ed == untilParam";
          declaration += ",Date untilParam";
          importStr = " import java.util.Date; ";
          params.put("untilParam", null);
      } else if (until != null) {
          filterStr += " && sd < untilParam";
          declaration += ",Date untilParam";
          importStr = " import java.util.Date; ";
          params.put("untilParam", until);
      }
      queryStr += filterStr + declaration + importStr + orderBy;
      return new QueryParams(queryStr, params);
  }

  /**
   * @return returns SQL QueryParams for Full abnormal inventory
   */
  public QueryParams getReportSqlQuery(Date from, Date until, Map<String, Object> filters,
      Locale locale, DomainConfig dc, String sourceUserId, PageParams pageParams) {
    Integer abnormalBefore = (Integer) filters.get(ReportsConstants.FILTER_ABNORMALDURATION);
    Date abnormalBeforeDate =
        (abnormalBefore != null) ? LocalDateUtil.getOffsetDate(new Date(), -1*abnormalBefore) : null;
    Integer eventType = (Integer) filters.get(ReportsConstants.FILTER_EVENT);
    Long domainId = (Long) filters.get(ReportsConstants.FILTER_DOMAIN);
    Long kioskId = (Long) filters.get(ReportsConstants.FILTER_KIOSK);
    Long materialId = (Long) filters.get(ReportsConstants.FILTER_MATERIAL);
    String kioskTag = (String) filters.get(ReportsConstants.FILTER_KIOSKTAG);
    String materialTag = (String) filters.get(ReportsConstants.FILTER_MATERIALTAG);
    LocationSuggestionModel location = (LocationSuggestionModel) filters.get(ReportsConstants.FILTER_LOCATION);
    boolean outstandingEvents = filters.get(ReportsConstants.FILTER_LATEST) != null;
    boolean ascendingOrder =
        filters.get(ReportsConstants.SORT_ASC) == null || (boolean) filters.get(ReportsConstants.SORT_ASC);
    if (kioskId == null && materialId == null && domainId == null) {
      throw new IllegalArgumentException("Neither kioskId, materialId nor domainId specified");
    }
    boolean hasKioskIds = false;
    if (sourceUserId != null) {
      try {
        // Get user
        UsersService as = Services.getService(UsersServiceImpl.class, locale);
        IUserAccount u = as.getUserAccount(sourceUserId);
        if (SecurityConstants.ROLE_SERVICEMANAGER.equals(u.getRole())) {
          hasKioskIds = true;
        }
      } catch (Exception e) {
        throw new IllegalArgumentException(e.getMessage());
      }
    }

    StringBuilder queryStr = new StringBuilder();
    Map<String, Object> params = new HashMap<String, Object>(1);
    queryStr.append("SELECT * FROM INVNTRY INV, INVNTRYEVNTLOG INVLOG WHERE INVLOG.KEY = INV.LSEV ");
    StringBuilder filterStr = new StringBuilder();
    String orderBy = " ORDER BY INVLOG.sd " + (ascendingOrder ? "ASC" : "DESC");
    if (eventType != null) {
      filterStr.append(" AND INVLOG.TY = " + eventType);
    }
    if (kioskId != null) {
      filterStr.append(" AND INVLOG.kId = " + kioskId);
    } else if (hasKioskIds) {
      filterStr
          .append(" AND INV.KID IN (SELECT UK.KIOSKID FROM USERTOKIOSK UK WHERE USERID = '")
          .append(sourceUserId)
          .append("')");
    }
    if (materialId != null) {
      filterStr.append(" AND INV.MID = ").append(materialId);
    }
    if (kioskId == null && !hasKioskIds && domainId != null) {
      filterStr
          .append(" AND ")
          .append(domainId)
          .append(" IN (SELECT DOMAIN_ID FROM INVNTRY_DOMAINS ID WHERE ID.KEY_OID = INV.KEY)");
    }
    // Tags, if any
    if (materialId == null && materialTag != null && !materialTag.isEmpty()) {
      filterStr
          .append(" AND '")
          .append(materialTag)
          .append(
                  "' IN (SELECT T.NAME FROM MATERIAL_TAGS MT INNER JOIN TAG T WHERE MT.MATERIALID = INV.MID AND T.ID = MT.ID)");
    } else if (kioskId == null && kioskTag != null && !kioskTag.isEmpty()) {
      filterStr
          .append(" AND '")
          .append(kioskTag)
          .append(
                  "' IN (SELECT T.NAME FROM KIOSK_TAGS KT INNER JOIN TAG T WHERE KT.KIOSKID = INV.KID AND T.ID = KT.ID)");
    }
    // Add from date
    if (from != null) {
      filterStr
          .append(" AND DATE(INVLOG.SD) > ")
          .append(CharacterConstants.SINGLE_QUOTES)
          .append(new SimpleDateFormat(Constants.DATE_FORMAT_CSV).format(from))
          .append(CharacterConstants.SINGLE_QUOTES);
    }
    // Add until date, if needed
    if (abnormalBeforeDate != null) {
      filterStr.append(" AND INVLOG.ED IS NULL");
      filterStr
          .append(" AND DATE(INVLOG.SD) < ")
          .append(CharacterConstants.SINGLE_QUOTES)
          .append(new SimpleDateFormat(Constants.DATE_FORMAT_CSV).format(abnormalBeforeDate))
          .append(CharacterConstants.SINGLE_QUOTES);
    } else if (outstandingEvents) {
      filterStr.append(" AND INVLOG.ED IS NULL");
    } else if (until != null) {
      filterStr
          .append(" AND DATE(INVLOG.SD) < ")
          .append(CharacterConstants.SINGLE_QUOTES)
          .append(new SimpleDateFormat(Constants.DATE_FORMAT_CSV).format(until))
          .append(CharacterConstants.SINGLE_QUOTES);
    }
    if((location != null && location.isNotEmpty())){
      boolean isAnd = false;
      filterStr
          .append(" AND INV.KID IN (SELECT KIOSKID FROM KIOSK WHERE");
      if (StringUtils.isNotEmpty(location.state)) {
        filterStr.append(" STATE = " + CharacterConstants.S_QUOTE)
            .append(location.state)
            .append(CharacterConstants.S_QUOTE);
        isAnd = true;
      }
      if (StringUtils.isNotEmpty(location.district)) {
        filterStr.append(isAnd ? " AND " : CharacterConstants.EMPTY);
        filterStr.append(" DISTRICT = ")
            .append(CharacterConstants.S_QUOTE)
            .append(location.district)
            .append(CharacterConstants.S_QUOTE);
        isAnd = true;
      }
      if (StringUtils.isNotEmpty(location.taluk)) {
        filterStr.append(isAnd ? " AND " : CharacterConstants.EMPTY);
        filterStr.append(" TALUK = ")
            .append(CharacterConstants.S_QUOTE)
            .append(location.taluk)
            .append(CharacterConstants.S_QUOTE);
      }
      filterStr.append(")");
    }
    String limitStr =
        (pageParams != null)
            ? " LIMIT " + pageParams.getOffset() + CharacterConstants.COMMA + pageParams.getSize()
            : "";
    queryStr.append(filterStr.toString()).append(orderBy).append(limitStr);
    return new QueryParams(queryStr.toString(), params, QueryParams.QTYPE.SQL, IInvntry.class);
  }
}

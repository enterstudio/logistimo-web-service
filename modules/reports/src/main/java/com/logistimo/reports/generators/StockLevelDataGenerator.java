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

import com.ibm.icu.util.Calendar;
import com.logistimo.config.models.DomainConfig;
import com.logistimo.dao.JDOUtils;
import com.logistimo.inventory.entity.IInvntry;
import com.logistimo.inventory.entity.IInvntryLog;
import com.logistimo.inventory.service.InventoryManagementService;
import com.logistimo.inventory.service.impl.InventoryManagementServiceImpl;

import com.logistimo.pagination.PageParams;
import com.logistimo.pagination.QueryParams;
import com.logistimo.reports.ReportsConstants;
import com.logistimo.services.ServiceException;
import com.logistimo.services.Services;
import com.logistimo.services.impl.PMF;
import com.logistimo.utils.LocalDateUtil;
import com.logistimo.utils.QueryUtil;
import com.logistimo.logger.XLog;

import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.jdo.PersistenceManager;
import javax.jdo.Query;

/**
 * @author Arun
 */
public class StockLevelDataGenerator implements ReportDataGenerator {

  private static final XLog xLogger = XLog.getLog(StockLevelDataGenerator.class);

  /**
   * Get stock levels over time
   */
  @SuppressWarnings("unchecked")
  @Override
  public ReportData getReportData(Date from, Date until, String frequency,
                                  Map<String, Object> filters, Locale locale, String timezone,
                                  PageParams pageParams, DomainConfig dc, String sourceUserId)
      throws ReportingDataException {
    xLogger.fine("Entered getReportData");
    // Validate the required filters
    if (from == null || until == null) {
      throw new ReportingDataException("Both from and until dates are required");
    }
    if (filters == null || filters.size() == 0) {
      throw new ReportingDataException("No material and kiosk filter were specified");
    }
    // Get the Ids
    Long materialId = (Long) filters.get(ReportsConstants.FILTER_MATERIAL);
    Long kioskId = (Long) filters.get(ReportsConstants.FILTER_KIOSK);
    if (materialId == null || kioskId == null) {
      throw new ReportingDataException(
          "Either the material and/or kiosk filter were not specified");
    }
    // Offset the from date
    Date modFrom = LocalDateUtil.getOffsetDate(from, -1, Calendar.MILLISECOND);
    // Get PM
    PersistenceManager pm = PMF.get().getPersistenceManager();
    // Form the query
    Query q = pm.newQuery(JDOUtils.getImplClass(IInvntryLog.class));
    q.setFilter("mId == mIdParam && kId == kIdParam && t > fromParam && t < untilParam");
    q.declareParameters("Long mIdParam, Long kIdParam, Date fromParam, Date untilParam");
    q.declareImports("import java.util.Date;");
    q.setOrdering("t asc"); // NOTE: indexes are defined on asc
    if (pageParams != null) {
      QueryUtil.setPageParams(q, pageParams);
    }
    // Get the param map
    Map<String, Object> paramMap = new HashMap<String, Object>();
    paramMap.put("mIdParam", materialId);
    paramMap.put("kIdParam", kioskId);
    paramMap.put("fromParam", modFrom);
    paramMap.put("untilParam", until);
    List<IInvntryLog> results = null;
    String cursor = null;
    try {
      // Execute query
      results = (List<IInvntryLog>) q.executeWithMap(paramMap);
      if (results != null) {
        results.size();
        cursor = QueryUtil.getCursor(results);
        results = (List<IInvntryLog>) pm.detachCopyAll(results);
      }
    } finally {
      try {
        q.closeAll();
      } catch (Exception ignored) {
        xLogger.warn("Exception while closing query", ignored);
      }
      pm.close();
    }
    // Get the re-order level
    BigDecimal reorderLevel = BigDecimal.ZERO;
    try {
      InventoryManagementService
          ims =
          Services.getService(InventoryManagementServiceImpl.class);
      IInvntry inv = ims.getInventory(kioskId, materialId);
      if (inv != null) {
        reorderLevel = inv.getReorderLevel();
      }
    } catch (ServiceException e) {
      xLogger.warn("ServiceException when getting inventory re-order level: {0}", e.getMessage());
    }
    xLogger.fine("Exiting getReportData");
    return new StockLevelData(from, until, filters, locale, timezone, results, cursor,
        reorderLevel);
  }

  @Override
  public QueryParams getReportQuery(Date from, Date until, String frequency,
                                    Map<String, Object> filters, Locale locale, String timezone,
                                    PageParams pageParams, DomainConfig dc, String sourceUserId) {
    // TODO Auto-generated method stub
    return null;
  }
}

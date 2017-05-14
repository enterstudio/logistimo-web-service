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

import com.logistimo.config.models.DomainConfig;
import com.logistimo.dao.JDOUtils;
import com.logistimo.entities.entity.IKiosk;
import com.logistimo.inventory.entity.IInvntry;

import com.logistimo.pagination.PageParams;
import com.logistimo.pagination.QueryParams;
import com.logistimo.reports.ReportsConstants;
import com.logistimo.services.impl.PMF;
import com.logistimo.utils.QueryUtil;
import com.logistimo.logger.XLog;

import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.jdo.JDOObjectNotFoundException;
import javax.jdo.PersistenceManager;
import javax.jdo.Query;

/**
 * @author Arun
 */
public class StockReportDataGenerator implements ReportDataGenerator {

  private static final XLog xLogger = XLog.getLog(StockReportDataGenerator.class);

  /**
   * Generate a data for reporting stock levels and safety-stocks
   * The only input required is a kioskId filter - all other inputs are ignored
   */
  @SuppressWarnings("unchecked")
  @Override
  public ReportData getReportData(Date from, Date until, String frequency,
                                  Map<String, Object> filters, Locale locale, String timezone,
                                  PageParams pageParams, DomainConfig dc, String sourceUserId)
      throws ReportingDataException {
    xLogger.fine("Entered StockReportDataGenerator.getReportData");

    if (filters == null || filters.isEmpty()) {
      throw new ReportingDataException("No filters specified.");
    }

    // Get the possible filters
    Long domainId = (Long) filters.get(ReportsConstants.FILTER_DOMAIN);
    Long kioskId = (Long) filters.get(ReportsConstants.FILTER_KIOSK);
    Long materialId = (Long) filters.get(ReportsConstants.FILTER_MATERIAL);

    // Form the query
    String queryStr = "SELECT FROM " + JDOUtils.getImplClass(IInvntry.class).getName() + " WHERE ";
    Long value = null;
    if (domainId != null) {
      queryStr += " dId.contains(dIdParam) PARAMETERS Long dIdParam order by knm asc";
      value = domainId;
    } else if (kioskId != null) {
      queryStr += " kId == kIdParam PARAMETERS Long kIdParam order by mnm asc";
      value = kioskId;
    } else if (materialId != null) {
      queryStr += " mId == mIdParam PARAMETERS Long mIdParam order by knm asc";
      value = materialId;
    } else {
      throw new ReportingDataException("Neither domainId, kioskId or materialId passed");
    }
    PersistenceManager pm = PMF.get().getPersistenceManager();
    List<IInvntry> results;
    String cursor;
    boolean optimizationOn;
    try {

      Query q = pm.newQuery(queryStr);
      // Set page params.
      if (pageParams != null) {
        QueryUtil.setPageParams(q, pageParams);
      }
      // Execute query
      cursor = null;
      results = null;
      try {
        results = (List<IInvntry>) q.execute(value);
        if (results != null) {
          results
              .size(); // loads the objects; important to do given, we are closing the PM (avoids ObjectManagerClosed exception)
          cursor = QueryUtil.getCursor(results);
          results = (List<IInvntry>) pm.detachCopyAll(results);
        }
      } finally {
        q.closeAll();
      }
      // Check if optimization is on
      optimizationOn = false;
      if (kioskId != null) {
        try {
          IKiosk k = JDOUtils.getObjectById(IKiosk.class, kioskId, pm);
          optimizationOn = k.isOptimizationOn();
        } catch (JDOObjectNotFoundException e) {
          xLogger.warn("Unable to find kiosk {0} when getting stock report data: {1}", kioskId,
              e.getMessage());
        }
      }
    } finally {
      pm.close();
    }
    StockReportData
        sr =
        new StockReportData(from, until, filters, locale, timezone, results, cursor,
            optimizationOn);
    xLogger.fine("Exiting StockReportDataGenerator.getReportData");
    return sr;
  }

  @Override
  public QueryParams getReportQuery(Date from, Date until, String frequency,
                                    Map<String, Object> filters, Locale locale, String timezone,
                                    PageParams pageParams, DomainConfig dc, String sourceUserId) {
    // TODO Auto-generated method stub
    return null;
  }
}

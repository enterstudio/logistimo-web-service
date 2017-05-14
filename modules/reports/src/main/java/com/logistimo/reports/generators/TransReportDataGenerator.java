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
import com.logistimo.inventory.entity.ITransaction;

import com.logistimo.pagination.PageParams;
import com.logistimo.pagination.QueryParams;
import com.logistimo.reports.ReportsConstants;
import com.logistimo.services.impl.PMF;
import com.logistimo.utils.LocalDateUtil;
import com.logistimo.utils.QueryUtil;
import com.logistimo.logger.XLog;

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
public class TransReportDataGenerator implements ReportDataGenerator {

  private static final XLog xLogger = XLog.getLog(TransReportDataGenerator.class);

  public TransReportDataGenerator() {
  }

  /**
   * Get data on transaction counts. From date is mandatory, Until date is optional. Filters are optional.
   * Only certain filters are recognized, others are ignored.
   * Accepts the following filters: FILTER_DOMAIN, FILTER_KIOSK and FILTER_MATERIAL.
   * NOTE 1: The Kiosk and Material filter cannot occur together - either Kiosk or Material can be specified.
   * NOTE 2: Location filters not supported for this type at this time.
   * NOTE 3: Only the following combinations of filters are allowed:
   * - from [, until], domainId
   * - from [,until], kioskId
   * - from [,until], materialId
   */
  @SuppressWarnings("unchecked")
  public ReportData getReportData(Date from, Date until, String frequency,
                                  Map<String, Object> filters, Locale locale, String timezone,
                                  PageParams pageParams, DomainConfig dc, String sourceUserId)
      throws ReportingDataException {
    xLogger.fine("Entered getTransactionReportData");

    // Get the query filters
    Map<String, Object> paramMap = new HashMap<String, Object>();
    // Add the 'from date' filter
    String transQueryFilter = "";
    String paramDeclaration = "";
    if (from != null) {
      transQueryFilter =
          "t > fromParam"; // we do not use >= given GAE translates that into two queries; instead we use > and offset the from date by 1 previous day
      paramDeclaration = "Date fromParam";
      paramMap.put("fromParam", LocalDateUtil.getOffsetDate(from, -1,
          Calendar.MILLISECOND)); // from date typically starts from 12am; so move back 1ms so that > query can be used (instead of >=)
    }
    // Add the 'until' date filter, if needed
    if (until != null) {
      if (!transQueryFilter.isEmpty()) {
        transQueryFilter += " && ";
        paramDeclaration += ", ";
      }
      transQueryFilter +=
          "t < untilParam"; // we do not use <= given GAE translates that into two queries; instead we use < and offset the date by 1 additional day
      paramDeclaration += "Date untilParam";
      Date
          nextDay =
          LocalDateUtil.getOffsetDate(until, 1,
              Calendar.DATE); // move this by one day, given until starts at 12am, and we want to include all transactions on this day
      paramMap.put("untilParam", LocalDateUtil.getOffsetDate(nextDay, 1,
          Calendar.MILLISECOND)); // move if further by 1ms, so that < query can be used (insted of <=)
    }
    xLogger.fine("getReportData: from = {0}, until = {1}", paramMap.get("fromParam"),
        paramMap.get("untilParam"));
    // Add the other filters, as needed
    if (filters != null) {
      // Add the 'kiosk' filter, if needed
      Long kioskId = (Long) filters.get(ReportsConstants.FILTER_KIOSK);
      if (kioskId != null) {
        if (!transQueryFilter.isEmpty()) {
          transQueryFilter += " && ";
          paramDeclaration += ", ";
        }
        transQueryFilter += "kId == kioskIdParam";
        paramDeclaration += "Long kioskIdParam";
        paramMap.put("kioskIdParam", kioskId);
      }
      // Add the 'material' filter, if needed
      Long materialId = (Long) filters.get(ReportsConstants.FILTER_MATERIAL);
      if (materialId != null) {
        // Material Id cannot co-occur with kioskId, throw an exception if it does
        if (kioskId != null) {
          throw new ReportingDataException(
              "Material filter cannot co-occur with Kiosk filter. Only one of them can be present.");
        }
        if (!transQueryFilter.isEmpty()) {
          transQueryFilter += " && ";
          paramDeclaration += ", ";
        }
        transQueryFilter += "mId == materialIdParam";
        paramDeclaration += "Long materialIdParam";
        paramMap.put("materialIdParam", materialId);
      }
      // Add the 'domain' filter, if needed, and only if kiosk and material filters were not present (given that kiosk and material IDs are unique throughout the system)
      Long domainId = (Long) filters.get(ReportsConstants.FILTER_DOMAIN);
      if (domainId != null && kioskId == null && materialId == null) {
        if (!transQueryFilter.isEmpty()) {
          transQueryFilter += " && ";
          paramDeclaration += ", ";
        }
        transQueryFilter += "dId.contains(domainIdParam)";
        paramDeclaration += "Long domainIdParam";
        paramMap.put("domainIdParam", domainId);
      }
    }

    // Form the transaction query
    PersistenceManager pm = PMF.get().getPersistenceManager();
    Query transQuery = pm.newQuery(JDOUtils.getImplClass(ITransaction.class));
    transQuery.setFilter(transQueryFilter);
    transQuery.declareParameters(paramDeclaration);
    transQuery.declareImports("import java.util.Date;");
    transQuery.setOrdering("t desc");
    if (pageParams != null) {
      QueryUtil.setPageParams(transQuery, pageParams);
    }
    // Execute the query and get the results
    List<ITransaction> trans = null;
    TransReportData tr = null;
    try {
      // Execute query
      trans = (List<ITransaction>) transQuery.executeWithMap(paramMap);
      trans.size(); // TODO Hack to ensure all objects are retrieved
      String cursor = QueryUtil.getCursor(trans);
      trans = (List<ITransaction>) pm.detachCopyAll(trans);
      tr = new TransReportData(from, until, filters, locale, timezone, trans, cursor);
    } catch (Exception e) {
      xLogger.warn("getTransactionReportData Exception: {0}", e.getMessage());
      throw new ReportingDataException(e.getMessage());
    } finally {
      try {
        transQuery.closeAll();
      } catch (Exception ignored) {
        xLogger.warn("Exception while closing transQuery", ignored);
      }
      pm.close();
    }

    return tr;
  }

  @Override
  public QueryParams getReportQuery(Date from, Date until, String frequency,
                                    Map<String, Object> filters, Locale locale, String timezone,
                                    PageParams pageParams, DomainConfig dc, String sourceUserId) {
    return null;
  }
}

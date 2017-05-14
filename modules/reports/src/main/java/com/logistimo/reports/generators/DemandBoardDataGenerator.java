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

import com.logistimo.orders.entity.IDemandItem;
import com.logistimo.orders.service.OrderManagementService;
import com.logistimo.orders.service.impl.OrderManagementServiceImpl;
import com.logistimo.pagination.PageParams;
import com.logistimo.pagination.QueryParams;
import com.logistimo.pagination.Results;
import com.logistimo.reports.ReportsConstants;
import com.logistimo.services.ServiceException;
import com.logistimo.services.Services;
import com.logistimo.logger.XLog;

import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Generate demand item data for reporting purposes, as per specified criteria
 *
 * @author Arun
 */
public class DemandBoardDataGenerator implements ReportDataGenerator {

  // Logger
  private static final XLog xLogger = XLog.getLog(DemandBoardDataGenerator.class);
  private PageParams pageParams = null;

  /**
   * Construct the DemandBoardDataGenerator, with an optional pagination parameters (such as cursor and number of results)
   */
  public DemandBoardDataGenerator() {
  }

  public PageParams getPageParams() {
    return pageParams;
  }

  public void setPageParams(PageParams pageParams) {
    this.pageParams = pageParams;
  }

  /**
   * Get Demand items according to specified criteria.
   * From date and at least one of [domainId, kioskId, materialId] are mandatory; until is ignored.
   */
  @SuppressWarnings("unchecked")
  @Override
  public ReportData getReportData(Date from, Date until, String frequency,
                                  Map<String, Object> filters, Locale locale, String timezone,
                                  PageParams pageParams, DomainConfig dc, String sourceUserId)
      throws ReportingDataException {
    xLogger.fine("Entered getReportData");
    DemandBoardData dbd = null;
    ///if ( from == null )
    ///	throw new ReportingDataException( "From date is not specified. It is mandatory" );
    Long domainId = (Long) filters.get(ReportsConstants.FILTER_DOMAIN);
    Long kioskId = (Long) filters.get(ReportsConstants.FILTER_KIOSK);
    Long materialId = (Long) filters.get(ReportsConstants.FILTER_MATERIAL);
    String kioskTag = (String) filters.get(ReportsConstants.FILTER_KIOSKTAG);
    String materialTag = (String) filters.get(ReportsConstants.FILTER_MATERIALTAG);
    if (domainId == null && kioskId == null && materialId == null) {
      throw new ReportingDataException(
          "Neither domain ID, kiosk ID or material ID are specified. At least one of them must be specified");
    }
    try {
      OrderManagementService oms = Services.getService(OrderManagementServiceImpl.class);
      Results
          results =
          oms.getDemandItems(domainId, kioskId, materialId, kioskTag, materialTag, from,
              pageParams);
      List<IDemandItem> items = null;
      if (results != null) {
        items = results.getResults();
      }
      dbd = new DemandBoardData(from, until, filters, locale, timezone, items, results.getCursor());
    } catch (ServiceException e) {
      throw new ReportingDataException(e.getMessage());
    }
    xLogger.fine("Exiting getReportData");
    return dbd;
  }

  @Override
  public QueryParams getReportQuery(Date from, Date until, String frequency,
                                    Map<String, Object> filters, Locale locale, String timezone,
                                    PageParams pageParams, DomainConfig dc, String sourceUserId) {
    // TODO Auto-generated method stub
    return null;
  }
}

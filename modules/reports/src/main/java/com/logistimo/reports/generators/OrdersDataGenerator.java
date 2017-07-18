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
import com.logistimo.entities.service.EntitiesService;
import com.logistimo.entities.service.EntitiesServiceImpl;
import com.logistimo.orders.service.OrderManagementService;
import com.logistimo.orders.service.impl.OrderManagementServiceImpl;
import com.logistimo.pagination.PageParams;
import com.logistimo.pagination.QueryParams;
import com.logistimo.pagination.Results;
import com.logistimo.reports.ReportsConstants;
import com.logistimo.services.Services;
import com.logistimo.tags.TagUtil;
import com.logistimo.users.entity.IUserAccount;
import com.logistimo.users.service.UsersService;
import com.logistimo.users.service.impl.UsersServiceImpl;

import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * @author Arun
 */
public class OrdersDataGenerator implements ReportDataGenerator {

  /* (non-Javadoc)
   * @see org.lggi.samaanguru.reports.ReportDataGenerator#getReportData(java.util.Date, java.util.Date, java.util.Map, java.util.Locale, java.lang.String, org.lggi.samaanguru.pagination.PageParams)
   */
  @SuppressWarnings("unchecked")
  @Override
  public ReportData getReportData(Date from, Date until, String frequency,
                                  Map<String, Object> filters, Locale locale, String timezone,
                                  PageParams pageParams, DomainConfig dc, String sourceUserId)
      throws ReportingDataException {
    OrdersData od = null;
    try {
      OrderManagementService oms = Services.getService(OrderManagementServiceImpl.class);
      // Get the parameters
      Long domainId = (Long) filters.get(ReportsConstants.FILTER_DOMAIN);
      Long kioskId = (Long) filters.get(ReportsConstants.FILTER_KIOSK);
      String status = (String) filters.get(ReportsConstants.FILTER_STATUS);
      String otype = (String) filters.get(ReportsConstants.FILTER_OTYPE);
      String kioskTag = (String) filters.get(ReportsConstants.FILTER_KIOSKTAG);
      String orderTag = (String) filters.get(ReportsConstants.FILTER_ORDERTAG);
      String tagType = null;
      String tag = null;
      if (kioskTag != null && !kioskTag.isEmpty()) {
        tagType = TagUtil.TYPE_ENTITY;
        tag = kioskTag;
      } else if (orderTag != null && !orderTag.isEmpty()) {
        tagType = TagUtil.TYPE_ORDER;
        tag = orderTag;
      }

      // Get kiosks IDs that should be filtered (for managers)
      List<Long> kioskIds = null;
      if (sourceUserId != null) {
        // Get user
        UsersService as = Services.getService(UsersServiceImpl.class, locale);
        EntitiesService es = Services.getService(EntitiesServiceImpl.class, locale);
        IUserAccount u = as.getUserAccount(sourceUserId);
        if (SecurityConstants.ROLE_SERVICEMANAGER.equals(u.getRole())) {
          kioskIds =
              es.getKioskIdsForUser(sourceUserId, null, null).getResults(); // TODO: pagination?
        }
      }
      // Results or = oms.getOrders( domainId, kioskId, status, from, until, otype, kioskTag, kioskIds, pageParams );
      Results
          or =
          oms.getOrders(domainId, kioskId, status, from, until, otype, tagType, tag, kioskIds,
              pageParams, null, null, null);
      od = new OrdersData(from, null, filters, locale, timezone, or.getResults(), or.getCursor());
    } catch (Exception e) {
      throw new ReportingDataException(e.getMessage());
    }

    return od;
  }

  @Override
  public QueryParams getReportQuery(Date from, Date until, String frequency,
                                    Map<String, Object> filters, Locale locale, String timezone,
                                    PageParams pageParams, DomainConfig dc, String sourceUserId) {
    // TODO Auto-generated method stub
    return null;
  }

}

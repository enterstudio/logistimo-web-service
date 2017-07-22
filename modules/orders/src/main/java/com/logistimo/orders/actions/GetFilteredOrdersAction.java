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

package com.logistimo.orders.actions;

import com.logistimo.constants.CharacterConstants;
import com.logistimo.logger.XLog;
import com.logistimo.orders.entity.IOrder;
import com.logistimo.orders.entity.Order;
import com.logistimo.orders.models.OrderFilters;
import com.logistimo.orders.service.IDemandService;
import com.logistimo.pagination.PageParams;
import com.logistimo.pagination.QueryParams;
import com.logistimo.pagination.Results;
import com.logistimo.services.impl.PMF;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

import javax.jdo.PersistenceManager;
import javax.jdo.Query;

/**
 * Created by charan on 19/07/17.
 */
@Component
public class GetFilteredOrdersAction {

  private static final XLog xLogger = XLog.getLog(GetFilteredOrdersAction.class);

  @Autowired
  private IDemandService demandService;

  @Autowired
  private GetFilteredOrdersQueryAction getFilteredOrdersQueryAction;

  public Results invoke(OrderFilters filters, PageParams pageParams) {
    if (filters.getKioskId() == null && filters.getDomainId() == null) {
      throw new IllegalArgumentException(
          "No kiosk or domain specified. At least one of them must be specified");
    }
    Query query = null;
    Query cntQuery = null;
    PersistenceManager pm = null;
    try {
      QueryParams
          queryParams =
          getFilteredOrdersQueryAction.invoke(filters);
      StringBuilder sqlQuery = new StringBuilder(queryParams.query);
      StringBuilder filterQuery = new StringBuilder(queryParams.filterQuery);

      applyPagination(sqlQuery, pageParams);
      pm = PMF.get().getPersistenceManager();
      query = pm.newQuery("javax.jdo.query.SQL", sqlQuery.toString());
      query.setClass(Order.class);
      List<Order> orders = (List<Order>) query.executeWithArray(queryParams.listParams.toArray());
      orders = (List<Order>) pm.detachCopyAll(orders);
      int count = 0;
      if (!orders.isEmpty()) {
        if (filters.isWithDemand()) {
          includeDemandItems(orders, pm);
        }
        String
            countQuery =
            "SELECT COUNT(1) FROM `ORDER` WHERE " + filterQuery;
        cntQuery = pm.newQuery("javax.jdo.query.SQL", countQuery);
        count =
            ((Long) ((List) cntQuery.executeWithArray(queryParams.listParams.toArray())).iterator()
                .next())
                .intValue();
      }
      return new Results(orders, null, count,
          pageParams == null ? 0 : pageParams.getOffset());
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
      if (pm != null) {
        pm.close();
      }
    }

  }

  private void includeDemandItems(List<Order> orders, PersistenceManager pm) {
    for (IOrder order : orders) {
      order.setItems(demandService.getDemandItems(order.getOrderId(), pm));
    }
  }

  private void applyPagination(StringBuilder sqlQuery, PageParams pageParams) {
    if (pageParams != null) {
      sqlQuery.append(" LIMIT ").append(pageParams.getOffset()).append(CharacterConstants.COMMA)
          .append(pageParams.getSize());
    }
  }

}

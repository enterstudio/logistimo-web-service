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

package com.logistimo.orders.dao.impl;

import com.logistimo.constants.CharacterConstants;
import com.logistimo.exception.LogiException;
import com.logistimo.logger.XLog;
import com.logistimo.orders.dao.IOrderDao;
import com.logistimo.orders.dao.OrderUpdateStatus;
import com.logistimo.orders.entity.IOrder;
import com.logistimo.orders.entity.Order;
import com.logistimo.services.impl.PMF;
import com.logistimo.tags.TagUtil;
import com.logistimo.utils.BigUtil;

import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.jdo.PersistenceManager;
import javax.jdo.Query;

/**
 * Created by charan on 03/03/15.
 */
@Repository
public class OrderDao implements IOrderDao {

  private static final XLog xlogger = XLog.getLog(OrderDao.class);

  @Override
  public String getKeyAsString(IOrder order) {
    return String.valueOf(((Order) order).getId());
  }

  @Override
  public Object createKey(Long orderId) {
    return orderId;
  }

  @Override
  public IOrder getOrder(Long orderId) {
    PersistenceManager pm = PMF.get().getPersistenceManager();
    try {
      return getOrder(orderId, pm);
    } finally {
      pm.close();
    }

  }

  @Override
  public IOrder getOrder(Long orderId, PersistenceManager persistenceManager) {
    return persistenceManager.getObjectById(Order.class, orderId);
  }

  @Override
  public OrderUpdateStatus update(IOrder order, PersistenceManager pm) throws LogiException {
    // Get the order with specified ID
    IOrder o = getOrder(order.getOrderId(), pm);
    // Check if status has changed
    String oldStatus = o.getStatus();
    String newStatus = order.getStatus();
    boolean statusChanged = !newStatus.equals(oldStatus);
    // Set new status
    if (statusChanged) {
      order
          .commitStatus(); // NOTE: This method takes care of propagating status, setting order processing times, and/or updating accounts if accounting is enabled
    }
    order.setUpdatedOn(new Date());
    order.setTotalPrice(order.computeTotalPrice()); // recompute price
    // Update paid field
    BigDecimal paidDiff = order.getPaid().subtract(o.getPaid());
    boolean paymentChanged = BigUtil.notEqualsZero(paidDiff);
    if (paymentChanged) {
      order.commitPayment(
          paidDiff); // NOTE: This method will update accounts, if accounting is enabled
    }
    o.setExpectedArrivalDate(order.getExpectedArrivalDate());
    o.setDueDate(order.getDueDate());
    o.setTags(order.getTags(TagUtil.TYPE_ORDER), TagUtil.TYPE_ORDER);
    o.setNumberOfItems(order.getItems().size());
    pm.makePersistent(order);
    return new OrderUpdateStatus(order, oldStatus, paymentChanged, statusChanged);
  }

  @Override
  public List<IOrder> getMigratoryOrders(Integer offset, Integer size, String cutoffDate) {

    PersistenceManager pm = null;
    Query query = null;
    List<IOrder> retlist = null;
    List<String> params = new ArrayList<>();
    try {
      String
          squery =
          " SELECT * FROM  logistimo.ORDER ord LEFT JOIN logistimo.ORDER_JOB_STATUS ojs ON (ord.ID = ojs.ID) "
              +
              " WHERE ord.CON <= ? AND ojs.STATUS NOT IN ('COMPLETED','FAILED') OR ojs.STATUS is null "
              +
              " ORDER BY ord.ID ASC limit " + offset + CharacterConstants.COMMA + size;
      params.add(cutoffDate);
      pm = PMF.get().getPersistenceManager();
      query = pm.newQuery("javax.jdo.query.SQL", squery);
      query.setClass(Order.class);
      retlist = (List<IOrder>) query.executeWithArray(params.toArray());
      retlist = (List<IOrder>) pm.detachCopyAll(retlist);

    } catch (Exception e) {
      xlogger.warn("Error encountered during order migrations", e);
    } finally {
      if (query != null) {
        query.closeAll();
      }
      if (pm != null) {
        pm.close();
      }
    }
    return retlist;
  }
}

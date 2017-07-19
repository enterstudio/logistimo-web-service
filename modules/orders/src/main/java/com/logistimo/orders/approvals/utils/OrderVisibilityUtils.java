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

package com.logistimo.orders.approvals.utils;

import com.logistimo.domains.entity.IDomainLink;
import com.logistimo.domains.utils.DomainsUtil;
import com.logistimo.orders.entity.IOrder;
import com.logistimo.services.ServiceException;

/**
 * Created by charan on 18/07/17.
 */
public class OrderVisibilityUtils {

  protected static void updateTransferOrderVisibility(IOrder order, Long domainId,
                                                      boolean isApproved)
      throws ServiceException {
    order.setVisibleToCustomer(isApproved);
    order.setVisibleToVendor(isApproved);
    order.setDomainId(domainId);
    if (!isApproved) {
      order.setDomainIds(DomainsUtil.getVisibleDomains(domainId, IDomainLink.TYPE_PARENT));
    } else {
      DomainsUtil.addToDomain(order, domainId, null);
    }
  }

  protected static void updatePurchaseOrderVisibility(IOrder order, Long domainId,
                                                      boolean isApproved)
      throws ServiceException {
    order.setVisibleToCustomer(true);
    order.setVisibleToVendor(isApproved);
    order.setDomainId(domainId);
    if (!isApproved) {
      order.setDomainIds(
          DomainsUtil.getVisibleDomains(order.getKioskDomainId(), IDomainLink.TYPE_PARENT));
    } else {
      DomainsUtil.addToDomain(order, domainId, null);
    }
  }

  protected static void updateSalesOrderVisibility(IOrder order, Long domainId)
      throws ServiceException {
    order.setVisibleToCustomer(true);
    order.setVisibleToVendor(true);
    order.setDomainId(domainId);
    DomainsUtil.addToDomain(order, domainId, null);
  }

  public static void setOrderVisibility(IOrder order, Long domainId, boolean isApproved)
      throws ServiceException {

    switch (order.getOrderType()) {
      case IOrder.TRANSFER_ORDER:
        updateTransferOrderVisibility(order, domainId, isApproved);
        break;
      case IOrder.PURCHASE_ORDER:
        updatePurchaseOrderVisibility(order, domainId, isApproved);
        break;
      case IOrder.SALES_ORDER:
        updateSalesOrderVisibility(order, domainId);
        break;
      default:
        throw new IllegalArgumentException("Invalid order type " + order.getOrderType());
    }
  }
}

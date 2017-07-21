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

package com.logistimo.orders.approvals.actions;

import com.logistimo.domains.utils.DomainsUtil;
import com.logistimo.orders.approvals.service.IOrderApprovalsService;
import com.logistimo.orders.approvals.utils.OrderVisibilityUtils;
import com.logistimo.orders.entity.IOrder;
import com.logistimo.services.ObjectNotFoundException;
import com.logistimo.services.ServiceException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Created by charan on 18/07/17.
 */
@Component
public class OrderVisibilityAction {

  @Autowired
  private IOrderApprovalsService orderApprovalsService;

  public void invoke(IOrder o, Long domainId) throws ServiceException, ObjectNotFoundException {
    boolean isApprovalRequired = orderApprovalsService.isApprovalRequired(o);
    if (isApprovalRequired) {
      OrderVisibilityUtils.setOrderVisibility(o, domainId, false);
    } else {
      o.setVisibleToCustomer(true);
      o.setVisibleToVendor(true);
      DomainsUtil.addToDomain(o, domainId, null);
    }
  }

}

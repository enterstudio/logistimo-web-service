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

import com.logistimo.approvals.client.IApprovalsClient;
import com.logistimo.approvals.client.models.Approval;
import com.logistimo.approvals.client.models.AttributeFilter;
import com.logistimo.approvals.client.models.RestResponsePage;
import com.logistimo.auth.utils.SecurityUtils;
import com.logistimo.entities.service.EntitiesService;
import com.logistimo.orders.approvals.constants.ApprovalConstants;
import com.logistimo.orders.approvals.models.OrderApprovalFilters;
import com.logistimo.orders.entity.IOrder;
import com.logistimo.services.ServiceException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

/**
 * Created by charan on 22/06/17.
 */
@Component
public class GetOrderApprovalsAction {

  @Autowired
  IApprovalsClient approvalsClient;

  @Autowired
  private EntitiesService entitiesService;

  public RestResponsePage<Approval> invoke(OrderApprovalFilters orderApprovalFilters)
      throws ServiceException {
    checkManager(orderApprovalFilters);
    return approvalsClient.fetchApprovals(orderApprovalFilters);
  }

  private void checkManager(OrderApprovalFilters orderApprovalFilters) throws ServiceException {
    if (!SecurityUtils.isAdmin()) {
      if (orderApprovalFilters.getEntityId() == null) {
        List<Long> kioskIds =
            entitiesService.getKioskIdsForUser(SecurityUtils.getUsername(), null, null)
                .getResults();
        orderApprovalFilters.setEntityList(kioskIds);
      }
      if (orderApprovalFilters.getRequestType() == null) {
        orderApprovalFilters.addAttribute(
            new AttributeFilter()
                .setKey(ApprovalConstants.ATTRIBUTE_APPROVAL_TYPE)
                .setValues(
                    Arrays.asList(String.valueOf(IOrder.PURCHASE_ORDER),
                        String.valueOf(IOrder.SALES_ORDER))));
      }

    }
  }
}

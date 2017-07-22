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

package com.logistimo.api.servlets.mobile.builders;

import com.logistimo.approvals.client.models.CreateApprovalResponse;
import com.logistimo.context.StaticApplicationContext;
import com.logistimo.orders.approvals.actions.GetOrderApprovalAction;
import com.logistimo.orders.approvals.service.IOrderApprovalsService;
import com.logistimo.orders.entity.IOrder;
import com.logistimo.orders.entity.approvals.IOrderApprovalMapping;
import com.logistimo.proto.MobileApprovalResponse;
import com.logistimo.services.ObjectNotFoundException;
import com.logistimo.services.Services;
import com.logistimo.users.entity.IUserAccount;
import com.logistimo.users.service.UsersService;
import com.logistimo.users.service.impl.UsersServiceImpl;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MobileApprovalResponseBuilder {

  private static final String GET_ORDER_APPROVAL_ACTION = "getOrderApprovalAction";

  /**
   * Build a approval response
   *
   * @param o Order
   * @return Approval response
   */
  public MobileApprovalResponse buildApprovalResponse(IOrder o) throws ObjectNotFoundException {
    List<IOrderApprovalMapping> orderApprovalMappings = getOrderApprovalMappings(o);
    if (orderApprovalMappings == null || orderApprovalMappings.isEmpty()) {
      return null;
    }
    IOrderApprovalMapping orderApprovalMapping = orderApprovalMappings.get(0);
    CreateApprovalResponse
        createApprovalResponse =
        getCreateApprovalResponse(orderApprovalMapping.getApprovalId());
    if (createApprovalResponse == null) {
      return null;
    }
    MobileApprovalResponse response = new MobileApprovalResponse();
    setUserDetails(orderApprovalMapping.getUpdatedBy(), createApprovalResponse.getRequesterId(),
        response);
    response.setApprid(createApprovalResponse.getApprovalId());
    if (createApprovalResponse.getExpireAt() != null) {
      response.setExpt(createApprovalResponse.getExpireAt().getTime());
    }
    response.setSt(createApprovalResponse.getStatus());
    if (createApprovalResponse.getUpdatedAt() != null) {
      response.setT(createApprovalResponse.getUpdatedAt().getTime());
    }
    return response;
  }

  /**
   * Get the approval details for the given order
   *
   * @param o order
   * @return List of approval mappings
   */
  private List<IOrderApprovalMapping> getOrderApprovalMappings(IOrder o) {
    Set<Long> orderIdSet = new HashSet<>(1);
    orderIdSet.add(o.getOrderId());
    return StaticApplicationContext.getBean(IOrderApprovalsService.class)
        .getOrdersApprovalMapping(orderIdSet, o.getOrderType());
  }

  /**
   * Method to fetch the get Order Approval Action
   *
   * @param approvalId Approval Id
   * @return response
   */
  private CreateApprovalResponse getCreateApprovalResponse(String approvalId)
      throws ObjectNotFoundException {
    return ((GetOrderApprovalAction) StaticApplicationContext.getApplicationContext()
        .getBean(GET_ORDER_APPROVAL_ACTION))
        .invoke(approvalId);
  }

  /**
   * Method to fetch the approver and requester details and set the same in response
   *
   * @param approverId  Approver Id same as updated by
   * @param requesterId Requester Id
   * @param response    Populated response
   */
  private void setUserDetails(String approverId, String requesterId,
                              MobileApprovalResponse response) {
    UsersService usersService = Services.getService(UsersServiceImpl.class);
    Set<String> userIds = new HashSet<>(2);
    userIds.add(approverId);
    userIds.add(requesterId);
    List<IUserAccount> userAccountList = usersService.getUsersByIds(new ArrayList<>(userIds));
    for (IUserAccount iUserAccount : userAccountList) {
      if (iUserAccount.getUserId().equals(approverId)) {
        response.setArrpvrn(iUserAccount.getFullName());
        response.setArrpvr(approverId);
      }
      if (iUserAccount.getUserId().equals(requesterId)) {
        response.setReqrn(iUserAccount.getFullName());
        response.setReqr(requesterId);
      }
    }
  }
}

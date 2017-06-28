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

package com.logistimo.orders.approvals.dao;

import com.logistimo.approvals.client.models.CreateApprovalResponse;
import com.logistimo.models.StatusModel;
import com.logistimo.orders.entity.approvals.IOrderApprovalMapping;
import com.logistimo.services.ObjectNotFoundException;
import com.logistimo.services.ServiceException;
import com.logistimo.users.service.UsersService;

import java.util.Set;

/**
 * Created by naveensnair on 21/06/17.
 */
public interface IApprovalsDao {

  IOrderApprovalMapping updateOrderApprovalMapping(CreateApprovalResponse approvalResponse,
      Integer approvalType) throws ServiceException, ObjectNotFoundException;

  void updateOrderApprovalStatus(Long orderId, String approvalId, String status);

  Set<String> getFilteredRequesters(String requester, Long domainId, UsersService usersService)
      throws ServiceException;

  Set<String> getFilteredApprovers(String approver, Long domainId) throws ServiceException;

  Integer getApprovalType(Long orderId, String approvalId);

  IOrderApprovalMapping getOrderApprovalMapping(String approvalId, String status);

  void updateOrderApprovalStatus(String approvalId, StatusModel model, String userId);

}

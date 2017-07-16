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

package com.logistimo.orders.approvals.service;

import com.logistimo.orders.entity.IOrder;
import com.logistimo.orders.entity.approvals.IOrderApprovalMapping;
import com.logistimo.services.ObjectNotFoundException;
import com.logistimo.services.ServiceException;

import java.util.Collection;
import java.util.List;
import java.util.Set;

/**
 * Created by naveensnair on 13/06/17.
 */
public interface IOrderApprovalsService {


  Collection<String> findRequesters(String query, Long domainId);

  Collection<String> findApprovers(String query, Long domainId);

  String getOrderType(Long orderId, String approvalId);

  List<IOrderApprovalMapping> getOrdersApprovalMapping(Set<Long> orderIds, int orderAppprovalType);

  boolean isApprovalRequired(IOrder order) throws ServiceException;

  boolean isApprovalRequired(IOrder order, Integer approvalType) throws ServiceException;

  boolean isShippingApprovalRequired(IOrder order) throws ServiceException, ObjectNotFoundException;

  boolean isShippingApprovalComplete(IOrder order) throws ServiceException, ObjectNotFoundException;

  boolean isTransferApprovalComplete(IOrder order);

  boolean isTransferApprovalRequired(IOrder order);
}

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

package com.logistimo.orders.approvals.service.impl;

import com.logistimo.orders.approvals.dao.IApprovalsDao;
import com.logistimo.orders.approvals.dao.impl.ApprovalsDao;
import com.logistimo.orders.approvals.service.IApprovalService;
import com.logistimo.services.Service;
import com.logistimo.services.ServiceException;
import com.logistimo.services.impl.ServiceImpl;
import com.logistimo.users.service.UsersService;

import java.util.Collection;
import java.util.Set;

/**
 * Created by naveensnair on 13/06/17.
 */
public class ApprovalServiceImpl extends ServiceImpl implements IApprovalService {
  private IApprovalsDao approvalDao = new ApprovalsDao();
  private static final Integer REQUESTER = 0;

  @Override
  public Class<? extends Service> getInterface() {
    return IApprovalService.class;
  }


  public Collection<String> getFilteredRequesters(String user, Long domainId, UsersService usersService,
                                           Integer type) throws ServiceException {
    Set<String> users = null;
    if (user != null && domainId != null && usersService != null) {
      if (REQUESTER.equals(type)) {
        users = approvalDao.getFilteredRequesters(user, domainId, usersService);
      } else {
        users = approvalDao.getFilteredApprovers(user, domainId);
      }
    }

    return users;
  }

  public String getOrderType(Long orderId, String approvalId) {
    String orderType = null;
    if (orderId != null && approvalId != null) {
      Integer oType = approvalDao.getApprovalType(orderId, approvalId);
      if (0 == oType) {
        orderType = "transfer";
      } else if (1 == oType) {
        orderType = "purchase";
      } else {
        orderType = "sales";
      }
    }
    return orderType;
  }

}

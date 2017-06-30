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

package com.logistimo.orders.approvals.dao.impl;

import com.logistimo.approvals.client.models.CreateApprovalResponse;
import com.logistimo.dao.JDOUtils;
import com.logistimo.logger.XLog;
import com.logistimo.models.StatusModel;
import com.logistimo.orders.approvals.builders.ApprovalsBuilder;
import com.logistimo.orders.approvals.constants.ApprovalConstants;
import com.logistimo.orders.approvals.dao.IApprovalsDao;
import com.logistimo.orders.entity.IOrder;
import com.logistimo.orders.entity.approvals.IOrderApprovalMapping;
import com.logistimo.orders.service.OrderManagementService;
import com.logistimo.orders.service.impl.OrderManagementServiceImpl;
import com.logistimo.services.ObjectNotFoundException;
import com.logistimo.services.ServiceException;
import com.logistimo.services.Services;
import com.logistimo.services.impl.PMF;
import com.logistimo.users.entity.IUserAccount;
import com.logistimo.users.service.UsersService;

import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.jdo.PersistenceManager;
import javax.jdo.Query;

/**
 * Created by naveensnair on 21/06/17.
 */
@Repository
public class ApprovalsDao implements IApprovalsDao {

  public static final String APPROVAL_ID_PARAM = "approvalIdParam";
  private static final XLog xLogger = XLog.getLog(ApprovalsDao.class);
  private ApprovalsBuilder builder = new ApprovalsBuilder();

  public IOrderApprovalMapping updateOrderApprovalMapping(CreateApprovalResponse approvalResponse,
      Integer approvalType) throws ServiceException, ObjectNotFoundException {
    IOrderApprovalMapping orderApprovalMapping = null;
    if (approvalResponse != null) {
      Long kioskId = null;
      OrderManagementService oms = Services.getService(OrderManagementServiceImpl.class);
      IOrder order = oms.getOrder(Long.parseLong(approvalResponse.getTypeId()));
      if(approvalType.equals(IOrder.PURCHASE_ORDER)) {
        kioskId = order.getKioskId();
      } else if(approvalType.equals(IOrder.SALES_ORDER)) {
        kioskId = order.getServicingKiosk();
      }
      orderApprovalMapping = builder.buildOrderApprovalMapping(approvalResponse, approvalType, kioskId);
    }
    if (orderApprovalMapping != null) {
      PersistenceManager pm = null;
      try {
        pm = PMF.get().getPersistenceManager();
        pm.makePersistent(orderApprovalMapping);
        orderApprovalMapping = pm.detachCopy(orderApprovalMapping);
      } catch (Exception e) {
        xLogger.severe("Error while persisting order approval mapping {0} for order type {1}",
            orderApprovalMapping, approvalType, e);
        throw new ServiceException(e);
      } finally {
        if (pm != null) {
          pm.close();
        }
      }
    }
    return orderApprovalMapping;
  }

  public void updateOrderApprovalStatus(String approvalId, StatusModel model, String userId) {
    IOrderApprovalMapping approvalMapping = getOrderApprovalMapping(approvalId,
        ApprovalConstants.PENDING);
    PersistenceManager pm = null;
    try {
      pm = PMF.get().getPersistenceManager();
      approvalMapping.setStatus(model.getStatus());
      approvalMapping.setUpdatedAt(new Date());
      approvalMapping.setUpdatedBy(userId);
      pm.makePersistent(approvalMapping);
    } catch (Exception e) {
      xLogger.fine("Failed to get order approval mapping for approval: {0} with status: {1}",
          approvalId, model.getStatus(), e);
    } finally {
      if (pm != null) {
        pm.close();
      }
    }
  }

  public IOrderApprovalMapping getOrderApprovalMapping(String approvalId, String status) {

    IOrderApprovalMapping orderApprovalMapping = null;
    if(StringUtils.isNotEmpty(approvalId)) {
      PersistenceManager pm = null;
      Query query = null;
      try {
        pm = PMF.get().getPersistenceManager();
        Map<String, Object> params = new HashMap<>();
        query = pm.newQuery(JDOUtils.getImplClass(IOrderApprovalMapping.class));
        query.setFilter("status == statusParam && approvalId == approvalIdParam");
        query.declareParameters("String statusParam, String approvalIdParam");
        params.put("statusParam", status);
        params.put(APPROVAL_ID_PARAM, approvalId);
        query.setUnique(true);
        orderApprovalMapping = (IOrderApprovalMapping) query.executeWithMap(params);
        orderApprovalMapping = pm.detachCopy(orderApprovalMapping);
      } catch (Exception e) {
        xLogger.fine("Failed to get order approval mapping for approval: {0} with status: {1}",
            approvalId, status, e);
      } finally {
        if (query != null) {
          try {
            query.closeAll();
          } catch (Exception ignored) {
            xLogger.warn(ignored.getMessage(), ignored);
          }
        }
        if (pm != null) {
          pm.close();
        }
      }
    }
    return orderApprovalMapping;
  }

  public void updateOrderApprovalStatus(Long orderId, String approvalId, String status) {
    if (orderId != null && StringUtils.isNotEmpty(approvalId)) {
      PersistenceManager pm = null;
      Query query = null;
      try {
        pm = PMF.get().getPersistenceManager();
        Map<String, Object> params = new HashMap<>();
        query = pm.newQuery(JDOUtils.getImplClass(IOrderApprovalMapping.class));
        query.setFilter("orderId == orderIdParam && approvalId == approvalIdParam");
        query.declareParameters("Long orderIdParam, String approvalIdParam");
        params.put("orderIdParam", orderId);
        params.put(APPROVAL_ID_PARAM, approvalId);
        query.setUnique(true);
        IOrderApprovalMapping orderApprovalMapping = (IOrderApprovalMapping) query
            .executeWithMap(params);
        if (orderApprovalMapping != null) {
          orderApprovalMapping.setStatus(status);
          orderApprovalMapping.setUpdatedAt(new Date());
          pm.makePersistent(orderApprovalMapping);
        }
      } catch (Exception e) {
        xLogger.fine("Failed to get order approval mapping for order: {0} with approval: {1}",
            orderId, approvalId, e);
      } finally {
        if (query != null) {
          try {
            query.closeAll();
          } catch (Exception ignored) {
            xLogger.warn(ignored.getMessage(), ignored);
          }
        }
        if (pm != null) {
          pm.close();
        }
      }
    }
  }

  public Integer getApprovalType(Long orderId, String approvalId) {
    Integer type = null;
    if (orderId != null && StringUtils.isNotEmpty(approvalId)) {
      PersistenceManager pm = null;
      Query query = null;
      try {
        pm = PMF.get().getPersistenceManager();
        Map<String, Object> params = new HashMap<>();
        query = pm.newQuery(JDOUtils.getImplClass(IOrderApprovalMapping.class));
        query.setFilter("orderId == orderIdParam && approvalId == approvalIdParam");
        query.declareParameters("Long orderIdParam, String approvalIdParam");
        params.put("orderIdParam", orderId);
        params.put(APPROVAL_ID_PARAM, approvalId);
        query.setUnique(true);
        IOrderApprovalMapping orderApprovalMapping = (IOrderApprovalMapping) query
            .executeWithMap(params);
        if (orderApprovalMapping != null) {
          type = orderApprovalMapping.getApprovalType();
        }
      } catch (Exception e) {
        xLogger
            .fine("Failed to get order approval mapping for order: {0} with approval: {1}",
                orderId,
                approvalId, e);
      } finally {
        if (query != null) {
          try {
            query.closeAll();
          } catch (Exception ignored) {
            xLogger.warn(ignored.getMessage(), ignored);
          }
        }
        if (pm != null) {
          pm.close();
        }
      }
    }
    return type;
  }

  public Set<String> getFilteredRequesters(String requester, Long domainId,
      UsersService usersService) throws ServiceException {
    Set<String> requesters = null;
    PersistenceManager pm = PMF.get().getPersistenceManager();
    Query query;
    if (StringUtils.isNotEmpty(requester)) {
      requesters = new HashSet<>();
      try {
        query = pm.newQuery("javax.jdo.query.SQL",
            "SELECT DISTINCT RID FROM ORDER_APPROVAL_MAPPING WHERE RID LIKE '"
                + requester + "%'");
        List requesterList = (List) query.execute();
        if (requesterList != null && !requesterList.isEmpty()) {
          for (Object object : requesterList) {
            IUserAccount userAccount = usersService.getUserAccount(object.toString());
            if (domainId.equals(userAccount.getDomainId())) {
              requesters.add(object.toString());
            }
          }
        }
      } catch (ObjectNotFoundException e) {
        xLogger
            .severe("Error while fetching requesters for domain {0} with name starting with {1}",
                domainId, requester, e);
        throw new ServiceException("Error while fetching requesters for domain " + domainId);
      }
    }
    return requesters;
  }

  public Set<String> getFilteredApprovers(String approver, Long domainId) throws ServiceException {
    Set<String> approvers = null;
    PersistenceManager pm = PMF.get().getPersistenceManager();
    Query query;
    if (StringUtils.isNotEmpty(approver)) {
      approvers = new HashSet<>();
      try {
        query = pm.newQuery("javax.jdo.query.SQL",
            "SELECT DISTINCT UID FROM APPROVERS WHERE UID LIKE '" + approver + "%' AND SDID="
                + domainId);
        List approversList = (List) query.execute();
        if (approversList != null && !approversList.isEmpty()) {
          for (Object object : approversList) {
            approvers.add(object.toString());
          }
        }
      } catch (Exception e) {
        xLogger.fine("Error while fetching approvers for domain {0} with name starting with {1}",
            domainId, approver, e);
        throw new ServiceException(
            "Unable to fetch approvers for domain " + domainId + " with name starting with "
                + approver);
      }
    }
    return approvers;
  }
}

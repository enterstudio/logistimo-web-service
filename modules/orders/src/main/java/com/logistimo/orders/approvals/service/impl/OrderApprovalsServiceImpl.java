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

import com.logistimo.config.models.ApprovalsConfig;
import com.logistimo.config.models.DomainConfig;
import com.logistimo.constants.CharacterConstants;
import com.logistimo.constants.Constants;
import com.logistimo.dao.JDOUtils;
import com.logistimo.entities.entity.IKiosk;
import com.logistimo.entities.service.EntitiesService;
import com.logistimo.logger.XLog;
import com.logistimo.orders.approvals.constants.ApprovalConstants;
import com.logistimo.orders.approvals.dao.IApprovalsDao;
import com.logistimo.orders.approvals.service.IOrderApprovalsService;
import com.logistimo.orders.entity.IOrder;
import com.logistimo.orders.entity.approvals.IOrderApprovalMapping;
import com.logistimo.orders.entity.approvals.OrderApprovalMapping;
import com.logistimo.services.ObjectNotFoundException;
import com.logistimo.services.ServiceException;
import com.logistimo.services.impl.PMF;
import com.logistimo.users.entity.IUserAccount;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.SQLException;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import javax.jdo.PersistenceManager;
import javax.jdo.Query;

/**
 * Created by naveensnair on 13/06/17.
 */
@Service
public class OrderApprovalsServiceImpl implements IOrderApprovalsService {

  private static final XLog xLogger = XLog.getLog(OrderApprovalsServiceImpl.class);


  @Autowired
  private IApprovalsDao approvalDao;

  @Autowired
  private EntitiesService entitiesService;


  @Override
  public Collection<String> findRequesters(String query, Long domainId) {
    return approvalDao.getFilteredRequesters(query, domainId);
  }

  @Override
  public Collection<String> findApprovers(String query, Long domainId) {
    return approvalDao.getFilteredApprovers(query, domainId);

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

  public List<IOrderApprovalMapping> getOrdersApprovalMapping(Set<Long> orderIds,
                                                              int orderAppprovalType) {

    if (orderIds == null || orderIds.isEmpty()) {
      return null;
    }
    PersistenceManager pm = PMF.get().getPersistenceManager();
    Query query = null;
    List<IOrderApprovalMapping> results = null;
    try {

      StringBuilder queryBuilder = new StringBuilder("SELECT * FROM `ORDER_APPROVAL_MAPPING` ");
      queryBuilder.append("WHERE ORDER_ID IN (");
      for (Long orderId : orderIds) {
        queryBuilder.append(orderId).append(CharacterConstants.COMMA);
      }
      queryBuilder.setLength(queryBuilder.length() - 1);
      queryBuilder.append(" )");
      queryBuilder.append(" AND APPROVAL_TYPE=").append(orderAppprovalType);
      queryBuilder.append(" ORDER BY ORDER_ID ASC");
      query = pm.newQuery("javax.jdo.query.SQL", queryBuilder.toString());
      query.setClass(OrderApprovalMapping.class);
      results = (List<IOrderApprovalMapping>) query.execute();
      results = (List<IOrderApprovalMapping>) pm.detachCopyAll(results);
    } catch (Exception e) {
      xLogger.warn("Exception while fetching approval status", e);
    } finally {
      if (query != null) {
        try {
          query.closeAll();
        } catch (Exception ignored) {
          xLogger.warn("Exception while closing query", ignored);
        }
      }
      pm.close();
    }
    return results;
  }

  public boolean isApprovalRequired(IOrder order) throws ServiceException {
    return isApprovalRequired(order, getApprovalType(order));
  }

  /**
   * Checks if any of the entity tags of the entity associated with this order is configured for approval in domain approval config
   */
  public boolean isApprovalRequired(IOrder order, Integer approvalType) throws ServiceException {
    boolean required = false;
    if (order != null) {
      IKiosk kiosk = null;
      List<String> entityTags = null;
      List<String> configTags;
      if (IOrder.PURCHASE_ORDER == approvalType) {
        kiosk = entitiesService.getKiosk(order.getKioskId());
      } else if (IOrder.SALES_ORDER == approvalType) {
        kiosk = entitiesService.getKiosk(order.getServicingKiosk());
      }
      if (kiosk != null) {
        entityTags = kiosk.getTags();
      }
      if (entityTags != null && !entityTags.isEmpty()) {
        DomainConfig dc = DomainConfig.getInstance(kiosk.getDomainId());
        ApprovalsConfig ac = dc.getApprovalsConfig();
        ApprovalsConfig.OrderConfig orderConfig = ac.getOrderConfig();
        if (orderConfig != null) {
          if (order.getOrderType().equals(IOrder.TRANSFER)
              && orderConfig.getPrimaryApprovers() != null && !orderConfig.getPrimaryApprovers()
              .isEmpty()) {
            required = true;
          } else {
            List<ApprovalsConfig.PurchaseSalesOrderConfig>
                psoa =
                orderConfig.getPurchaseSalesOrderApproval();
            for (ApprovalsConfig.PurchaseSalesOrderConfig ps : psoa) {
              configTags = ps.getEntityTags();
              int atype = approvalType != null ? approvalType : order.getOrderType();
              if (configTags != null && !configTags.isEmpty()) {
                required = isTagConfigured(entityTags, configTags, atype, ps);
              }
              if (required) {
                break;
              }
            }
          }
        }
      }
    }
    return required;
  }

  @Override
  public boolean isShippingApprovalRequired(IOrder order)
      throws ServiceException, ObjectNotFoundException {
    return DomainConfig.getInstance(order.getLinkedDomainId()).getApprovalsConfig().getOrderConfig()
        .isSaleApprovalEnabled(
            entitiesService.getKioskIfPresent(order.getServicingKiosk()).getTags());
  }

  @Override
  public boolean isShippingApprovalComplete(IOrder order)
      throws ServiceException, ObjectNotFoundException {
    if (isShippingApprovalRequired(order)) {
      IOrderApprovalMapping
          approval =
          approvalDao.getOrderApprovalMapping(order.getOrderId(), IOrder.SALES_ORDER);
      return approval != null && ApprovalConstants.APPROVED.equals(approval.getStatus());
    }
    return true;
  }

  @Override
  public boolean isTransferApprovalComplete(IOrder order) {
    if (isTransferApprovalRequired(order)) {
      IOrderApprovalMapping
          approval =
          approvalDao.getOrderApprovalMapping(order.getOrderId(), IOrder.TRANSFER_ORDER);
      return approval != null && ApprovalConstants.APPROVED.equals(approval.getStatus());
    }
    return true;
  }

  @Override
  public boolean isTransferApprovalRequired(IOrder order) {
    return DomainConfig.getInstance(order.getLinkedDomainId()).getApprovalsConfig().getOrderConfig()
        .isTransferApprovalEnabled();
  }

  /**
   * Checks whether purchase or sales order approval is configured
   */
  public boolean isTagConfigured(List<String> entityTags, List<String> configTags,
                                 Integer orderType, ApprovalsConfig.PurchaseSalesOrderConfig ps) {
    boolean found = false;
    for (String entityTag : entityTags) {
      for (String configTag : configTags) {
        if (entityTag.equals(configTag)) {
          if (IOrder.PURCHASE_ORDER == orderType) {
            found = ps.isPurchaseOrderApproval();
          } else if (IOrder.SALES_ORDER == orderType) {
            found = ps.isSalesOrderApproval();
          }
          if (found) {
            break;
          }
        }
      }
    }
    return found;
  }

  /**
   * Get the approval type for a given order
   */
  @Override
  public Integer getApprovalType(IOrder order) {
    Integer approvalType = null;
    if (IOrder.PURCHASE_ORDER == order.getOrderType()) {
      if (!order.isVisibleToVendor()) {
        approvalType = IOrder.PURCHASE_ORDER;
      } else {
        approvalType = IOrder.SALES_ORDER;
      }
    } else if (IOrder.SALES_ORDER == order.getOrderType()) {
      approvalType = IOrder.SALES_ORDER;
    } else if (IOrder.TRANSFER_ORDER == order.getOrderType()) {
      approvalType = IOrder.TRANSFER_ORDER;
    }
    return approvalType;
  }
  @Override
  public Boolean isApprover(String userId) throws SQLException {
    PersistenceManager pm = PMF.get().getPersistenceManager();
    Query query = null;
    IUserAccount userAccount;
    try {
      String
          sqlQuery =
          "SELECT 1 FROM APPROVERS WHERE UID = ? LIMIT 1";
      query = pm.newQuery(Constants.JAVAX_JDO_QUERY_SQL, sqlQuery);
      query.setUnique(true);
      if ((query.execute(userId)) != null) {
        return true;
      }
      userAccount = JDOUtils.getObjectById(IUserAccount.class, userId, pm);
      DomainConfig dc = DomainConfig.getInstance(userAccount.getDomainId());
      ApprovalsConfig.OrderConfig ac = dc.getApprovalsConfig().getOrderConfig();
      return ac != null && ac.isApprover(userId);
    } finally {
      if(query != null) {
        query.closeAll();
      }
      pm.close();
    }
  }

}

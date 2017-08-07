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
import com.logistimo.orders.entity.approvals.OrderApprovalMapping;
import com.logistimo.orders.service.OrderManagementService;
import com.logistimo.orders.service.impl.OrderManagementServiceImpl;
import com.logistimo.services.ObjectNotFoundException;
import com.logistimo.services.ServiceException;
import com.logistimo.services.Services;
import com.logistimo.services.impl.PMF;

import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import javax.jdo.PersistenceManager;
import javax.jdo.Query;

/**
 * Created by naveensnair on 21/06/17.
 */
@Repository
public class ApprovalsDao implements IApprovalsDao {

  public static final String APPROVAL_ID_PARAM = "approvalIdParam";
  public static final String ORDER_ID_PARAM = "orderIdParam";
  public static final String STATUS_PARAM = "statusParam";
  public static final String APPROVAL_TYPE_PARAM = "approvalTypeParam";

  private static final XLog xLogger = XLog.getLog(ApprovalsDao.class);
  private static final String APRROVAL_TYPE_PARAM = "approvalTypeParam";
  private ApprovalsBuilder builder = new ApprovalsBuilder();

  public IOrderApprovalMapping updateOrderApprovalMapping(CreateApprovalResponse approvalResponse,
                                                          Integer approvalType)
      throws ServiceException, ObjectNotFoundException {
    IOrderApprovalMapping orderApprovalMapping = null;
    if (approvalResponse != null) {
      Long kioskId = null;
      OrderManagementService oms = Services.getService(OrderManagementServiceImpl.class);
      IOrder order = oms.getOrder(Long.parseLong(approvalResponse.getTypeId()));
      if (approvalType.equals(IOrder.PURCHASE_ORDER)) {
        kioskId = order.getKioskId();
      } else {
        kioskId = order.getServicingKiosk();
      }
      orderApprovalMapping =
          builder.buildOrderApprovalMapping(approvalResponse, approvalType, kioskId);
      orderApprovalMapping.setLatest(true);
    }
    if (orderApprovalMapping != null) {
      PersistenceManager pm = null;
      try {
        pm = PMF.get().getPersistenceManager();
        pm.makePersistent(orderApprovalMapping);
        updateOldApprovalRequests(orderApprovalMapping, pm);
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

  private void updateOldApprovalRequests(final IOrderApprovalMapping orderApprovalMapping,
                                         PersistenceManager pm) {
    Query query = pm.newQuery(OrderApprovalMapping.class);
    query.setFilter("orderId == orderIdParam && approvalType == approvalTypeParam && latest ");
    query.declareParameters("Long orderIdParam, Integer approvalTypeParam");
    Map<String, Object> params = new HashMap<>();
    params.put(APRROVAL_TYPE_PARAM, orderApprovalMapping.getApprovalType());
    params.put(ORDER_ID_PARAM, orderApprovalMapping.getOrderId());
    List<OrderApprovalMapping> results = (List<OrderApprovalMapping>) query.executeWithMap(params);
    if (results != null && !results.isEmpty()) {
      results.stream().filter(approvalMapping -> !Objects
          .equals(approvalMapping.getApprovalId(), orderApprovalMapping.getApprovalId()))
          .forEach(approvalMapping -> approvalMapping.setLatest(false));
    }
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
      xLogger.warn("Failed to get order approval mapping for approval: {0} with status: {1}",
          approvalId, model.getStatus(), e);
    } finally {
      if (pm != null) {
        pm.close();
      }
    }
  }

  public IOrderApprovalMapping getOrderApprovalMapping(Long orderId, String status) {
    IOrderApprovalMapping orderApprovalMapping = null;
    if (orderId != null) {
      PersistenceManager pm = null;
      Query query = null;
      try {
        pm = PMF.get().getPersistenceManager();
        Map<String, Object> params = new HashMap<>();
        query = pm.newQuery(JDOUtils.getImplClass(IOrderApprovalMapping.class));
        query.setFilter("status == statusParam && orderId == orderIdParam");
        query.declareParameters("String statusParam, Long orderIdParam");
        params.put(STATUS_PARAM, status);
        params.put(ORDER_ID_PARAM, orderId);
        query.setUnique(true);
        orderApprovalMapping = (IOrderApprovalMapping) query.executeWithMap(params);
        orderApprovalMapping = pm.detachCopy(orderApprovalMapping);
      } catch (Exception e) {
        xLogger.warn("Failed to get order approval mapping for order: {0} with status: {1}",
            orderApprovalMapping, status, e);
      } finally {
        if (query != null) {
          try {
            query.closeAll();
          } catch (Exception ignored) {
            //ignored
          }
        }
        if (pm != null) {
          pm.close();
        }
      }
    }
    return orderApprovalMapping;
  }

  public IOrderApprovalMapping getOrderApprovalMapping(Long orderId, Integer approvalType) {
    IOrderApprovalMapping orderApprovalMapping = null;
    List<IOrderApprovalMapping> results = null;
    if (orderId != null) {
      PersistenceManager pm = null;
      Query query = null;
      try {
        pm = PMF.get().getPersistenceManager();
        Map<String, Object> params = new HashMap<>();
        query = pm.newQuery(JDOUtils.getImplClass(IOrderApprovalMapping.class));
        query.setFilter("orderId == orderIdParam && approvalType == approvalTypeParam");
        query.declareParameters("Long orderIdParam, Integer approvalTypeParam");
        query.setOrdering("createdAt desc");
        query.setRange(0, 1);
        params.put(ORDER_ID_PARAM, orderId);
        params.put(APPROVAL_TYPE_PARAM, approvalType);
        results = (List<IOrderApprovalMapping>) query.executeWithMap(params);
        if (results != null && !results.isEmpty()) {
          orderApprovalMapping = results.get(0);
        }
      } catch (Exception e) {
        xLogger.warn("Failed to get order approval mapping for order: {0}",
            orderId, e);
      } finally {
        if (query != null) {
          try {
            query.closeAll();
          } catch (Exception ignored) {
            xLogger.warn("Exception while closing query", ignored);
          }
        }
        if (pm != null) {
          pm.close();
        }
      }
    }
    return orderApprovalMapping;
  }

  /**
   * returns the total approvals against that order
   */
  public List<IOrderApprovalMapping> getTotalOrderApprovalMapping(Long orderId) {
    List<IOrderApprovalMapping> results = null;
    if (orderId != null) {
      PersistenceManager pm = null;
      Query query = null;
      try {
        pm = PMF.get().getPersistenceManager();
        Map<String, Object> params = new HashMap<>();
        query = pm.newQuery(JDOUtils.getImplClass(IOrderApprovalMapping.class));
        query.setFilter("orderId == orderIdParam");
        query.declareParameters("Long orderIdParam");
        query.setOrdering("createdAt desc");
        params.put(ORDER_ID_PARAM, orderId);
        results = (List<IOrderApprovalMapping>) query.executeWithMap(params);
        results = (List<IOrderApprovalMapping>) pm.detachCopyAll(results);
      } catch (Exception e) {
        xLogger.warn("Failed to get order approval mapping for order: {0}",
            orderId, e);
      } finally {
        if (query != null) {
          try {
            query.closeAll();
          } catch (Exception ignored) {
            xLogger.warn("Exception while closing query", ignored);
          }
        }
        if (pm != null) {
          pm.close();
        }
      }
    }
    return results;

  }

  public IOrderApprovalMapping getOrderApprovalMapping(Long orderId) {
    IOrderApprovalMapping orderApprovalMapping = null;
    List<IOrderApprovalMapping> results = null;
    if (orderId != null) {
      PersistenceManager pm = null;
      Query query = null;
      try {
        pm = PMF.get().getPersistenceManager();
        Map<String, Object> params = new HashMap<>();
        query = pm.newQuery(JDOUtils.getImplClass(IOrderApprovalMapping.class));
        query.setFilter("orderId == orderIdParam");
        query.declareParameters("Long orderIdParam");
        query.setOrdering("createdAt desc");
        params.put(ORDER_ID_PARAM, orderId);
        results = (List<IOrderApprovalMapping>) query.executeWithMap(params);
        if (results != null && !results.isEmpty()) {
          orderApprovalMapping = results.get(0);
        }
      } catch (Exception e) {
        xLogger.warn("Failed to get order approval mapping for order: {0}",
            orderId, e);
      } finally {
        if (query != null) {
          try {
            query.closeAll();
          } catch (Exception ignored) {
            xLogger.warn("Exception while closing query", ignored);
          }
        }
        if (pm != null) {
          pm.close();
        }
      }
    }
    return orderApprovalMapping;
  }


  public IOrderApprovalMapping getOrderApprovalMapping(String approvalId, String status) {
    IOrderApprovalMapping orderApprovalMapping = null;
    if (StringUtils.isNotEmpty(approvalId)) {
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
        xLogger.warn("Failed to get order approval mapping for approval: {0} with status: {1}",
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

  public IOrderApprovalMapping getOrderApprovalMapping(String approvalId) {
    IOrderApprovalMapping orderApprovalMapping = null;
    if (StringUtils.isNotEmpty(approvalId)) {
      PersistenceManager pm = null;
      Query query = null;
      try {
        pm = PMF.get().getPersistenceManager();
        Map<String, Object> params = new HashMap<>();
        query = pm.newQuery(JDOUtils.getImplClass(IOrderApprovalMapping.class));
        query.setFilter("approvalId == approvalIdParam");
        query.declareParameters("String approvalIdParam");
        params.put(APPROVAL_ID_PARAM, approvalId);
        query.setUnique(true);
        orderApprovalMapping = (IOrderApprovalMapping) query.executeWithMap(params);
        orderApprovalMapping = pm.detachCopy(orderApprovalMapping);
      } catch (Exception e) {
        xLogger.fine("Failed to get order approval mapping for approval: {0} ",
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
        query.setFilter("orderId == orderIdParam && approvalId == approvalIdParam ");
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
        xLogger.warn("Failed to get order approval mapping for order: {0} with approval: {1}",
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
        IOrderApprovalMapping
            orderApprovalMapping =
            (IOrderApprovalMapping) query.executeWithMap(params);
        orderApprovalMapping = pm.detachCopy(orderApprovalMapping);
        if (orderApprovalMapping != null) {
          type = orderApprovalMapping.getApprovalType();
        }
      } catch (Exception e) {
        xLogger
            .warn("Failed to get order approval mapping for order: {0} with approval: {1}",
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

  public Collection<String> getFilteredRequesters(String requester, Long domainId) {
    PersistenceManager pm = PMF.get().getPersistenceManager();
    Query query = null;
    List<String> queryList = new ArrayList<>(0);
    try {
      if (StringUtils.isNotEmpty(requester)) {
        query = pm.newQuery("javax.jdo.query.SQL",
            "SELECT DISTINCT CREATED_BY FROM ORDER_APPROVAL_MAPPING WHERE "
                + "EXISTS (SELECT 1 from USERACCOUNT_DOMAINS WHERE CREATED_BY = USERID_OID "
                + "AND DOMAIN_ID = ?) "
                + "AND CREATED_BY in (SELECT USERID from USERACCOUNT WHERE NNAME like ?) limit 10");
        List results = (List) query.executeWithArray(domainId, requester.toLowerCase() + "%");
        if(results != null && !results.isEmpty()) {
          queryList = new ArrayList<>(results.size());
          for(Object o : results) {
            queryList.add((String)o);
          }
        }
      }
    } finally {
      if (query != null) {
        query.closeAll();
      }
      pm.close();
    }
    return queryList;
  }

  public Collection<String> getFilteredApprovers(String requester, Long domainId) {
    PersistenceManager pm = PMF.get().getPersistenceManager();
    Query query = null;
    List<String> queryList = new ArrayList<>(0);
    try {
      if (StringUtils.isNotEmpty(requester)) {
        query = pm.newQuery("javax.jdo.query.SQL",
            "SELECT DISTINCT UID FROM APPROVERS WHERE "
                + "EXISTS (SELECT 1 from USERACCOUNT_DOMAINS WHERE UID = USERID_OID "
                + "AND DOMAIN_ID = ?) "
                + "AND UID in (SELECT USERID from USERACCOUNT WHERE NNAME like ?) limit 10");
        List results = (List) query.executeWithArray(domainId, requester.toLowerCase()+"%");
        if(results != null && !results.isEmpty()) {
          queryList = new ArrayList<>(results.size());
          for(Object o : results) {
            queryList.add((String)o);
          }
        }
      }
    } finally {
      if (query != null) {
        query.closeAll();
      }
      pm.close();
    }
    return queryList;
  }

}

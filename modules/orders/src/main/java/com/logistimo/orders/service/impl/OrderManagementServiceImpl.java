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

package com.logistimo.orders.service.impl;

import com.ibm.icu.util.Calendar;
import com.logistimo.AppFactory;
import com.logistimo.activity.entity.IActivity;
import com.logistimo.activity.service.ActivityService;
import com.logistimo.activity.service.impl.ActivityServiceImpl;
import com.logistimo.auth.utils.SecurityUtils;
import com.logistimo.config.models.ApprovalsConfig;
import com.logistimo.config.models.DomainConfig;
import com.logistimo.config.models.EventSpec;
import com.logistimo.config.models.LeadTimeAvgConfig;
import com.logistimo.constants.CharacterConstants;
import com.logistimo.constants.Constants;
import com.logistimo.context.StaticApplicationContext;
import com.logistimo.conversations.entity.IMessage;
import com.logistimo.conversations.service.ConversationService;
import com.logistimo.conversations.service.impl.ConversationServiceImpl;
import com.logistimo.dao.JDOUtils;
import com.logistimo.domains.entity.IDomainLink;
import com.logistimo.domains.utils.DomainsUtil;
import com.logistimo.entities.entity.IKiosk;
import com.logistimo.entities.entity.IKioskLink;
import com.logistimo.entities.service.EntitiesService;
import com.logistimo.entities.service.EntitiesServiceImpl;
import com.logistimo.events.EventConstants;
import com.logistimo.events.entity.IEvent;
import com.logistimo.events.models.CustomOptions;
import com.logistimo.events.processor.EventPublisher;
import com.logistimo.exception.LogiException;
import com.logistimo.exception.TaskSchedulingException;
import com.logistimo.exception.ValidationException;
import com.logistimo.inventory.TransactionUtil;
import com.logistimo.inventory.dao.ITransDao;
import com.logistimo.inventory.dao.impl.TransDao;
import com.logistimo.inventory.entity.IInvAllocation;
import com.logistimo.inventory.entity.IInvntry;
import com.logistimo.inventory.entity.ITransaction;
import com.logistimo.inventory.exceptions.InventoryAllocationException;
import com.logistimo.inventory.service.InventoryManagementService;
import com.logistimo.inventory.service.impl.InventoryManagementServiceImpl;
import com.logistimo.logger.XLog;
import com.logistimo.materials.entity.IHandlingUnit;
import com.logistimo.materials.entity.IMaterial;
import com.logistimo.materials.service.IHandlingUnitService;
import com.logistimo.materials.service.MaterialCatalogService;
import com.logistimo.materials.service.impl.HandlingUnitServiceImpl;
import com.logistimo.materials.service.impl.MaterialCatalogServiceImpl;
import com.logistimo.models.shipments.ShipmentItemModel;
import com.logistimo.models.shipments.ShipmentModel;
import com.logistimo.orders.OrderResults;
import com.logistimo.orders.OrderUtils;
import com.logistimo.orders.approvals.service.IOrderApprovalsService;
import com.logistimo.orders.dao.IOrderDao;
import com.logistimo.orders.dao.OrderUpdateStatus;
import com.logistimo.orders.dao.impl.OrderDao;
import com.logistimo.orders.entity.IDemandItem;
import com.logistimo.orders.entity.IDemandItemBatch;
import com.logistimo.orders.entity.IOrder;
import com.logistimo.orders.entity.Order;
import com.logistimo.orders.models.UpdatedOrder;
import com.logistimo.orders.service.IDemandService;
import com.logistimo.orders.service.OrderManagementService;
import com.logistimo.orders.validators.UpdateOrderStatusValidator;
import com.logistimo.pagination.PageParams;
import com.logistimo.pagination.Results;
import com.logistimo.services.ObjectNotFoundException;
import com.logistimo.services.ServiceException;
import com.logistimo.services.Services;
import com.logistimo.services.impl.PMF;
import com.logistimo.services.impl.ServiceImpl;
import com.logistimo.services.taskqueue.ITaskService;
import com.logistimo.services.utils.ConfigUtil;
import com.logistimo.shipments.ShipmentStatus;
import com.logistimo.shipments.entity.IShipment;
import com.logistimo.shipments.service.IShipmentService;
import com.logistimo.shipments.service.impl.ShipmentService;
import com.logistimo.tags.TagUtil;
import com.logistimo.tags.dao.ITagDao;
import com.logistimo.tags.dao.TagDao;
import com.logistimo.tags.entity.ITag;
import com.logistimo.utils.BigUtil;
import com.logistimo.utils.LocalDateUtil;
import com.logistimo.utils.LockUtil;
import com.logistimo.utils.QueryUtil;

import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.jdo.JDOObjectNotFoundException;
import javax.jdo.PersistenceManager;
import javax.jdo.Query;
import javax.jdo.Transaction;

/**
 * @author arun
 */
@Service
public class OrderManagementServiceImpl extends ServiceImpl implements OrderManagementService {

  private static final XLog xLogger = XLog.getLog(OrderManagementServiceImpl.class);
  private static final String
      UPDATE_ENTITYACTIVITYTIMESTAMPS_TASK =
      "/s2/api/entities/task/updateentityactivitytimestamps";
  private static ITaskService taskService = AppFactory.get().getTaskService();
  private ITagDao tagDao = new TagDao();
  private IOrderDao orderDao = new OrderDao();
  private ITransDao transDao = new TransDao();


  // Get a demand item with same material ID
  private static IDemandItem getDemandItemByMaterial(List<IDemandItem> demandList,
                                                     Long materialId) {
    if (demandList == null || demandList.isEmpty()) {
      return null;
    }
    for (IDemandItem di : demandList) {
      if (di.getMaterialId().equals(materialId)) {
        return di;
      }
    }
    return null;
  }

  public IOrder getOrder(Long orderId) throws ObjectNotFoundException, ServiceException {
    return getOrder(orderId, false);
  }

  /**
   * Get an order, given an order Id
   */
  @Override
  public IOrder getOrder(Long orderId, boolean includeItems)
      throws ObjectNotFoundException, ServiceException {
    xLogger.fine("Entered getOrder");
    if (orderId == null) {
      throw new ServiceException("No order ID specified");
    }
    PersistenceManager pm = PMF.get().getPersistenceManager();
    IOrder o = null;
    try {
      o = JDOUtils.getObjectById(IOrder.class, orderDao.createKey(orderId), pm);
      o = pm.detachCopy(o);
      if (includeItems) {
        IDemandService ds = Services.getService(DemandService.class, this.getLocale());
        o.setItems(ds.getDemandItems(orderId));
      }
    } catch (JDOObjectNotFoundException e) {
      throw new ObjectNotFoundException(e.getMessage());
    } catch (Exception e) {
      xLogger.severe("Exception in getOrder: {0}", e.getMessage(), e);
      throw new ServiceException(e.getMessage());
    } finally {
      // Close PM
      pm.close();
    }
    xLogger.fine("Exiting getOrder");
    return o;
  }

  @Override
  public void updateOrderVisibility(Long orderId, Integer orderType) throws ObjectNotFoundException {
    PersistenceManager pm = PMF.get().getPersistenceManager();
    IOrder o;
    try {
      o = JDOUtils.getObjectById(IOrder.class, orderDao.createKey(orderId), pm);
      if(IOrder.PURCHASE_ORDER.equals(orderType)) {
        o.setVisibleToVendor(true);
      } else if(IOrder.TRANSFER_ORDER.equals(orderType)) {
        o.setVisibleToCustomer(true);
        o.setVisibleToVendor(true);
      }
      pm.makePersistent(o);
    } catch (JDOObjectNotFoundException e) {
      throw new ObjectNotFoundException(e.getMessage());
    } finally {
      pm.close();
    }
  }

  /**
   * Add a new order
   */
  public Long addOrder(IOrder order) throws ServiceException {
    xLogger.fine("Entered addOrder");
    if (order == null) {
      throw new ServiceException("Invalid order");
    }
    PersistenceManager pm = PMF.get().getPersistenceManager();
    Long oid = null;
    try {
      pm.makePersistent(order);
      oid = order.getOrderId();
    } catch (Exception e) {
      xLogger.severe("Exception in addOrder: {0}", e.getMessage());
      throw new ServiceException(e.getMessage());
    } finally {
      // Close PM
      pm.close();
    }
    xLogger.fine("Exiting addOrder");
    return oid;
  }

  public UpdatedOrder updateOrder(IOrder order, int source) throws LogiException {
    return updateOrder(order, source, false, false);
  }

  public UpdatedOrder updateOrder(IOrder order, int source, boolean isLocked, boolean validateHU)
      throws LogiException {
    return updateOrder(order, source, isLocked, validateHU, null);
  }

  /**
   * Update an order, and post inventory issues/receipts, if needed.
   */
  public UpdatedOrder updateOrder(IOrder order, int source, boolean isLocked, boolean validateHU,
                                  String userId) throws LogiException {
    return updateOrder(order, source, isLocked, validateHU, userId, null);
  }

  public UpdatedOrder updateOrder(IOrder order, int source, boolean isLocked, boolean validateHU,
                                  String userId, PersistenceManager pm) throws LogiException {
    xLogger.fine("Entered updateOrder");
    if (order == null) {
      throw new ServiceException("Invalid order");
    }
    UpdatedOrder uo = new UpdatedOrder();
    boolean useLocalPM = pm == null;
    // Init. services and data
    if (useLocalPM) {
      pm = PMF.get().getPersistenceManager();
    }

    LockUtil.LockStatus lockStatus = LockUtil.lock(Constants.TX_O + order.getOrderId());
    if (!LockUtil.isLocked(lockStatus)) {
      throw new ServiceException("O002", order.getOrderId());
    }
    try {
      if (validateHU) {
        validateHU((List<IDemandItem>) order.getItems());
      }

      if (IOrder.BACKORDERED.equals(order.getStatus())) {
        boolean isOrderCompleted = true;
        for (IDemandItem demandItem : order.getItems()) {
          if (BigUtil.notEquals(demandItem.getQuantity(), demandItem.getShippedQuantity())) {
            isOrderCompleted = false;
            break;
          }
        }
        if (isOrderCompleted) {
          IShipmentService ss = Services.getService(ShipmentService.class);
          List<IShipment> shipments = ss.getShipmentsByOrderId(order.getOrderId());
          String newOrderStatus = ss.getOverallStatus(shipments, true, order.getOrderId());
          pm.makePersistentAll(order.getItems());
          updateOrderStatus(order.getOrderId(), newOrderStatus, userId, null, null, source, pm,
              null);
          order.setStatus(newOrderStatus);
        }
      }
      OrderUpdateStatus orderUpdateStatus = orderDao.update(order, pm);
      if (order.getItems() != null) {
        pm.makePersistentAll(order.getItems());
      }
      order = uo.order = orderUpdateStatus.order;
      // Generate event
      if (orderUpdateStatus.paymentChanged || orderUpdateStatus.statusChanged) {
        if (orderUpdateStatus.paymentChanged) {
          generateEvent(order.getDomainId(), IEvent.PAID, order, null, null);
        }
        if (orderUpdateStatus.statusChanged) {
          generateEvent(order.getDomainId(), IEvent.STATUS_CHANGE, order, null, null);
        }
      } else {
        generateEvent(order.getDomainId(), IEvent.MODIFIED, order, null, null);
      }
    } catch (LogiException e) {
      xLogger.severe("Exception in getOrder: {0}", e);
      throw e;
    } catch (Exception e) {
      xLogger.severe("Exception in getOrder: {0}", e);
      throw new ServiceException(e.getMessage());
    } finally {
      if (LockUtil.shouldReleaseLock(lockStatus) && !LockUtil
          .release(Constants.TX_O + order.getOrderId())) {
        xLogger.warn("Unable to release lock for key {0}", Constants.TX_O + order.getOrderId());
      }
      if (useLocalPM) {
        // Close PM
        pm.close();
      }
    }
    xLogger.fine("Exiting updateOrder");
    return uo;
  }

  private void validateHU(List<IDemandItem> items) throws LogiException {
    if (items != null) {
      for (IDemandItem dm : items) {
        validateHU(dm.getQuantity(), dm.getMaterialId(), null);
      }
    }
  }

  private IOrder getDetached(IOrder o, PersistenceManager pm) {
    //Required for SQL only..
    if (!ConfigUtil.getBoolean(Constants.GAE_DEPLOYMENT, false)) {
      List<IDemandItem> demandItems = new ArrayList<>();
      if (o.getItems() != null) {
        for (IDemandItem item : o.getItems()) {
          demandItems.add(pm.detachCopy(item));
        }
      }
      o = pm.detachCopy(o);
      o.setItems(demandItems);
    }
    return o;
  }

  public UpdatedOrder updateOrderStatus(Long orderId, String newStatus, String updatingUserId,
                                        String message, List<String> userIdsToBeNotified,
                                        int source) throws ServiceException {
    return updateOrderStatus(orderId, newStatus, updatingUserId, message, userIdsToBeNotified,
        source, null, null);
  }

  /**
   * Update an order's status, and post inventory issues/receipts, if needed.
   */
  public UpdatedOrder updateOrderStatus(Long orderId, String newStatus, String updatingUserId,
                                        String message, List<String> userIdsToBeNotified,
                                        int source, PersistenceManager pm, String crsn)
      throws ServiceException {

    boolean isLocalPM = pm == null;
    UpdatedOrder uo = null;
    Long domainId = null;
    LockUtil.LockStatus lockStatus = LockUtil.lock(Constants.TX_O + orderId);
    if (!LockUtil.isLocked(lockStatus)) {
      throw new ServiceException("O002", orderId);
    }
    Transaction tx = null;
    try {
      if (isLocalPM) {
        pm = PMF.get().getPersistenceManager();
        tx = pm.currentTransaction();
        tx.begin();
      }
      IOrder o = JDOUtils.getObjectById(IOrder.class, orderId, pm);
      if (newStatus == null || o.getStatus().equals(newStatus)) {
        return new UpdatedOrder(o);
      }

      StaticApplicationContext.getBean(UpdateOrderStatusValidator.class)
          .validateOrderStatusChange(o,
              newStatus);

      IDemandService ds = Services.getService(DemandService.class);
      List<IDemandItem> demandList = ds.getDemandItems(orderId, pm);
      o.setItems(demandList);
      domainId = o.getDomainId();
      DomainConfig dc = DomainConfig.getInstance(domainId);
      String tag = IInvAllocation.Type.ORDER + CharacterConstants.COLON + orderId;
      InventoryManagementService
          ims =
          Services.getService(InventoryManagementServiceImpl.class,
              this.getLocale());

      switch (newStatus) {
        case IOrder.CANCELLED:
          o.setCancelledDiscrepancyReason(crsn);
          IShipmentService ss = Services.getService(ShipmentService.class, this.getLocale());
          List<IShipment> shipments = ss.getShipmentsByOrderId(orderId, pm);
          for (IShipment shipment : shipments) {
            xLogger
                .info("Cancelling shipment {0} for order {1}", orderId, shipment.getShipmentId());
            ss.updateShipmentStatus(shipment.getShipmentId(), ShipmentStatus.CANCELLED, message,
                updatingUserId, crsn, false, pm, source);
          }
          if (dc.autoGI()) {
            ims.clearAllocationByTag(null, null, tag, pm);
          }
          break;
        case IOrder.CONFIRMED:
          if (dc.autoGI()) {
            if (dc.getOrdersConfig().allocateStockOnConfirmation()) {
              for (IDemandItem d : demandList) {
                try {
                  ims.allocateAutomatically(o.getServicingKiosk(), d.getMaterialId(),
                      IInvAllocation.Type.ORDER,
                      String.valueOf(d.getOrderId()), tag, d.getQuantity(), d.getUserId(), pm);
                } catch (InventoryAllocationException ie) {
                  xLogger.warn("Unable to auto allocate for order {0}, k: {1}, m: {2}, q: {3}"
                      , o.getOrderId(), d.getMaterialId(), d.getQuantity(), ie);
                }
              }
            }
          }
      }

      uo = updateOrderStatus(o, newStatus, updatingUserId, message, pm);
      if (isLocalPM) {
        tx.commit();
      }
      uo.order = getDetached(o, pm);
    } catch (ServiceException e) {
      throw e;
    } catch (Exception e) {
      xLogger.severe("Exception update order status {0}", e.getMessage(), e);
      throw new ServiceException(e.getClass().getName() + ": " + e.getMessage());
    } finally {
      if (LockUtil.shouldReleaseLock(lockStatus) && !LockUtil.release(Constants.TX_O + orderId)) {
        xLogger.warn("Unable to release lock for key {0}", Constants.TX_O + orderId);
      }
      if (isLocalPM) {
        if (tx != null && tx.isActive()) {
          tx.rollback();
        }
        pm.close();
      }
    }

    // Schedule a status change notification
    // NOTE: Do this after pm is closed so that the order status is persisted
    generateEvent(domainId, IEvent.STATUS_CHANGE, uo.order, null, userIdsToBeNotified);
    return uo;
  }

  private UpdatedOrder updateOrderStatus(IOrder o, String newStatus, String updatingUserId,
                                         String message, PersistenceManager pm)
      throws ServiceException {
    xLogger.fine("Entered updateOrderStatus");
    if (o == null || newStatus == null || newStatus.isEmpty() || o.isStatus(newStatus)) {
      throw new IllegalArgumentException(
          "Invalid order or order status: " + newStatus + " old status: " + (o != null ? o
              .getStatus() : "'Order is null'"));
    }
    UpdatedOrder uo = new UpdatedOrder();
    try {
      String oldStatus = o.getStatus();

      // Change status
      o.setStatus(newStatus);
      o.commitStatus(); // NOTE: This method takes care of propagating status, setting order processing times, and/or updating accounts if accounting is enabled
      pm.makePersistentAll(o.getItems());
      o.setUpdatedBy(updatingUserId);
      o.setUpdatedOn(new Date(o.getStatusUpdatedOn().getTime()));
      IMessage iMessage = null;
      if (message != null && !message.isEmpty()) {
        iMessage = addMessageToOrder(o.getOrderId(), o.getDomainId(), message, updatingUserId, pm);
      }
      addStatusHistory(o.getOrderId(), oldStatus, newStatus, o.getDomainId(), iMessage,
          updatingUserId, pm);

    } catch (Exception e) {
      xLogger.severe("Exception in updateOrderStatus: {0} : {1}", e.getClass().getName(),
          e.getMessage(), e);
      throw new ServiceException(e);
    }
    xLogger.fine("Exiting updateOrderStatus");
    return uo;
  }

  public IMessage addMessageToOrder(Long orderId, String message, String userId)
      throws ServiceException, ObjectNotFoundException {
    IOrder order = getOrder(orderId);
    return addMessageToOrder(orderId, order.getDomainId(), message, userId, null);
  }

  @Override
  public String shipNow(IOrder order, String transporter, String trackingId, String reason,
                        Date expectedFulfilmentDate, String userId, String ps, int source)
      throws ServiceException, ObjectNotFoundException, ValidationException {
    IShipmentService
        shipmentService =
        Services.getService(ShipmentService.class, this.getLocale());
    MaterialCatalogService
        mcs =
        Services.getService(MaterialCatalogServiceImpl.class, this.getLocale());
    EntitiesService as = Services.getService(EntitiesServiceImpl.class, this.getLocale());
    ShipmentModel model = new ShipmentModel();
    if (expectedFulfilmentDate != null) {
      model.ead = new SimpleDateFormat(Constants.DATE_FORMAT).format(expectedFulfilmentDate);
    }
    model.trackingId = trackingId;
    model.transporter = transporter;
    model.ps = ps;
    model.orderId = order.getOrderId();
    model.customerId = order.getKioskId();
    model.vendorId = order.getServicingKiosk();
    IKiosk vendor = as.getKiosk(model.vendorId, false);
    model.reason = reason;
    model.status = ShipmentStatus.SHIPPED;
    model.tags = order.getTags(TagUtil.TYPE_ORDER);
    model.userID = userId;
    model.items = new ArrayList<>();
    model.sdid = order.getDomainId();
    DomainConfig dc = DomainConfig.getInstance(order.getDomainId());
    for (IDemandItem demandItem : order.getItems()) {
      if (BigUtil.greaterThanZero(demandItem.getQuantity())) {
        ShipmentItemModel shipmentItemModel = new ShipmentItemModel();
        shipmentItemModel.mId = demandItem.getMaterialId();
        shipmentItemModel.q = demandItem.getQuantity();
        shipmentItemModel.afo = dc.autoGI();
        IMaterial material = mcs.getMaterial(shipmentItemModel.mId);
        shipmentItemModel.isBa = material.isBatchEnabled() && vendor.isBatchMgmtEnabled();
        shipmentItemModel.mnm = material.getName();
        model.items.add(shipmentItemModel);
      }
    }
    return shipmentService.createShipment(model, source);
  }

  private IMessage addMessageToOrder(Long orderId, Long domainId, String message,
                                     String updatingUserId,
                                     PersistenceManager pm) throws ServiceException {
    ConversationService cs =
        Services.getService(ConversationServiceImpl.class, this.getLocale());
    IMessage
        iMessage =
        cs.addMsgToConversation("ORDER", orderId.toString(), message, updatingUserId,
            Collections.singleton("ORDER:" + orderId), domainId, pm);
    generateOrderCommentEvent(domainId, IEvent.COMMENTED, JDOUtils.getImplClassName(IOrder.class),
        orderId.toString(), null, null);
    return iMessage;

  }

  // Generate shipment events, if configured
  @Override
  public void generateOrderCommentEvent(Long domainId, int eventId, String objectType,
                                        String objectId, String message,
                                        List<String> userIds) {
    try {
      // Custom options
      CustomOptions customOptions = new CustomOptions();
      if (message != null && !message.isEmpty() || (userIds != null && !userIds.isEmpty())) {
        customOptions.message = message;
        if (userIds != null && !userIds.isEmpty()) {
          Map<Integer, List<String>> userIdsMap = new HashMap<>();
          userIdsMap.put(Integer.valueOf(EventSpec.NotifyOptions.IMMEDIATE), userIds);
          customOptions.userIds = userIdsMap;
        }
      }
      // Generate event, if needed
      EventPublisher.generate(domainId, eventId, null, objectType, objectId,
          customOptions);
    } catch (Exception e) {
      xLogger.severe("{0} when generating Comment event {1} for object {2} in domain {3}: {4}",
          e.getClass().getName(), eventId, objectId, domainId, e);
    }
  }

  private void addStatusHistory(Long orderId, String oldStatus, String newStatus, Long domainId,
                                IMessage iMessage,
                                String userId, PersistenceManager pm) throws ServiceException {
    ActivityService
        activityService =
        Services.getService(ActivityServiceImpl.class, this.getLocale());
    activityService
        .createActivity(IActivity.TYPE.ORDER.name(), String.valueOf(orderId), "STATUS", oldStatus,
            newStatus,
            userId, domainId, iMessage != null ? iMessage.getMessageId() : null,
            "ORDER:" + orderId, pm);
  }

  /**
   * Get orders for a given kiosk, status (optional) and a time limit (optional)
   */
  @SuppressWarnings("unchecked")
  public Results getOrders(Long domainId, Long kioskId, String status, Date since, Date until,
                           String otype, String tagType, String tag, List<Long> kioskIds,
                           PageParams pageParams, Integer orderType, String referenceId, String approvalStatus)
      throws ServiceException {
    return getOrders(domainId, kioskId, status, since, until, otype, tagType, tag, kioskIds,
        pageParams, orderType, referenceId, approvalStatus, false);
  }

  public Results getOrders(Long domainId, Long kioskId, String status, Date since, Date until,
                           String otype, String tagType, String tag, List<Long> kioskIds,
                           PageParams pageParams, Integer orderType, String referenceId,
                           String approvalStatus, boolean withDemand) throws ServiceException {
    xLogger.fine("Entered getOrders");
    if (kioskId == null && domainId == null) {
      throw new ServiceException(
          "No kiosk or domain specified. At least one of them must be specified");
    }
    OrderResults results = null;
    PersistenceManager pm = PMF.get().getPersistenceManager();
    try {
      // Get query filters
      String filters = "";
      String declarations = "";
      String imports = null;
      Map<String, Object> paramMap = new HashMap<String, Object>();
      if (kioskId != null) {
        if (IOrder.TYPE_SALE.equals(otype)) { // sales orders
          filters = "skId == skIdParam";
          declarations = "Long skIdParam";
          paramMap.put("skIdParam", kioskId);
        } else if (IOrder.TYPE_PURCHASE.equals(otype)) { // purchase orders
          filters = "kId == kIdParam";
          declarations = "Long kIdParam";
          paramMap.put("kIdParam", kioskId);
        }
      } else if (kioskIds != null && !kioskIds.isEmpty()) {
        String kioskField = (IOrder.TYPE_SALE.equals(otype) ? "skId" : "kId");
        filters = "kioskIds.contains( " + kioskField + " )";
        declarations = "java.util.Collection kioskIds";
        paramMap.put("kioskIds", kioskIds);
      } else if (domainId != null) {
        filters = "dId.contains(dIdParam)";
        declarations = "Long dIdParam";
        paramMap.put("dIdParam", domainId);
      }

      if (status != null && !status.isEmpty()) {
        filters += " && st == stParam";
        declarations += ", String stParam";
        paramMap.put("stParam", status);
      }
      if (kioskId == null && (domainId != null || status != null) && tagType != null && !tagType
          .isEmpty() && tag != null && !tag
          .isEmpty()) { // tag inclusion in query possible on with domainId and/or status
        if (TagUtil.TYPE_ENTITY.equals(tagType)) {
          filters += " && ktgs.contains(ktgsParam)";
          declarations += ", Long ktgsParam";
          paramMap.put("ktgsParam", tagDao.getTagFilter(tag, ITag.KIOSK_TAG));
        } else if (TagUtil.TYPE_ORDER.equals(tagType)) {
          filters += " && otgs.contains(otgsParam)";
          declarations += ", Long otgsParam";
          paramMap.put("otgsParam", tagDao.getTagFilter(tag, ITag.ORDER_TAG));
        }
      } else if (kioskId != null && tagType != null && !tagType.isEmpty() && tag != null && !tag
          .isEmpty() && TagUtil.TYPE_ORDER.equals(tagType)) {
        filters += " && otgs.contains(otgsParam)";
        declarations += ", Long otgsParam";
        paramMap.put("otgsParam", tagDao.getTagFilter(tag, ITag.ORDER_TAG));
      }
      // Add from date
      if (since != null) {
        filters += " && cOn > cOnParam";
        declarations += ", Date cOnParam";
        imports = "import java.util.Date;";
        paramMap.put("cOnParam", LocalDateUtil.getOffsetDate(since, -1, Calendar.MILLISECOND));
      }
      // Add until date
      if (until != null) {
        filters += " && cOn < untilParam";
        declarations += ", Date untilParam";
        imports = "import java.util.Date;";
        paramMap.put("untilParam", until);
      }
      // Filter transfer order
      if (orderType != null) {
          if(orderType == IOrder.TRANSFER) {
              filters += " && oty == otyParam";
          } else {
              orderType = IOrder.TRANSFER;
              filters += " && oty != otyParam";
          }

        declarations += ", Integer otyParam";
        paramMap.put("otyParam", orderType);
      }
      if (referenceId != null) {
        filters += " && rid == ridParam";
        declarations += ", String ridParam";
        paramMap.put("ridParam", referenceId);
      }
      // Form the query
      Query q = pm.newQuery(JDOUtils.getImplClass(IOrder.class));
      q.setFilter(filters);
      q.declareParameters(declarations);
      q.setOrdering("cOn desc");
      if (imports != null) {
        q.declareImports(imports);
      }
      // Add pagination parameters, if needed
      if (pageParams != null) {
        QueryUtil.setPageParams(q, pageParams);
      }
      // Execute query
      try {
        // Execute query
        List<IOrder> orders = (List<IOrder>) q.executeWithMap(paramMap);
        orders.size(); // to ensure orders are retrieved before PM is closed
        // Get the cursor of the next element in the result set (for future iteration, efficiently)
        String cursorStr = QueryUtil.getCursor(orders);
        orders = (List<IOrder>) pm.detachCopyAll(orders);

        if (withDemand) {
          IDemandService ds = Services.getService(DemandService.class);
          for (IOrder order : orders) {
            order.setItems(ds.getDemandItems(order.getOrderId()));
          }
        }
        // Create the result set
        results = new OrderResults(orders, cursorStr);
      } finally {
        q.closeAll();
      }
    } catch (Exception e) {
      xLogger.severe("Exception in getOrders(): {0}", e.getMessage());
      throw new ServiceException(e.getMessage(), e);
    } finally {
      // Close PM
      pm.close();
    }

    xLogger.fine("Exiting getOrders");
    return results;
  }

  /**
   * Get orders based on kiosk,status,ordertype
   *
   * @param kioskId    - Kiosk ID
   * @param status     -Order status
   * @param pageParams -Page params with max results and offset
   * @param orderType  -Order type sle for sales and prc for purchase
   * @param isTransfer - True for transfers, false if it is sales/purchase
   * @return List of IOrder
   * @throws ServiceException from service layer
   */
  public List<IOrder> getOrders(Long kioskId, String status, PageParams pageParams,
                                String orderType, boolean isTransfer) throws ServiceException {
    PersistenceManager pm = PMF.get().getPersistenceManager();
    List<String> parameters = new ArrayList<>(1);
    StringBuilder queryBuilder = new StringBuilder("SELECT * FROM `ORDER` ");
    Query query = null;
    List<IOrder> results;
    try {

      //Set the oty based on transfer or not
      queryBuilder.append(" WHERE OTY").append(isTransfer ? "=" : "!=")
          .append(CharacterConstants.QUESTION);
      parameters.add(String.valueOf(IOrder.TRANSFER));

      //If the order type is purchase append kid, if it is sales append the lkid
      if (OrderUtils.isValidOrderType(orderType)) {
        if (orderType.equalsIgnoreCase(IOrder.TYPE_PURCHASE)) {
          queryBuilder.append(" AND KID =").append(CharacterConstants.QUESTION);
        } else {
          queryBuilder.append(" AND SKID =").append(CharacterConstants.QUESTION);
        }
        parameters.add(String.valueOf(kioskId));
      }
      //Append status information
      if (StringUtils.isNotBlank(status) && OrderUtils.isValidOrderStatus(status)) {
        queryBuilder.append("AND ST=").append(CharacterConstants.QUESTION);
        parameters.add(status);
      }

      queryBuilder.append(" ORDER BY UON DESC");
      queryBuilder.append(" LIMIT ").append(pageParams.getOffset()).append(CharacterConstants.COMMA)
          .append(pageParams.getSize());
      query = pm.newQuery("javax.jdo.query.SQL", queryBuilder.toString());
      query.setClass(Order.class);
      results = (List<IOrder>) query.executeWithArray(parameters.toArray());
      results = (List<IOrder>) pm.detachCopyAll(results);
    } catch (Exception e) {
      xLogger.warn("Exception while fetching orders minimum response", e);
      throw new ServiceException("Service exception fetching order details");
    } finally {
      if (query != null) {
        try {
          query.closeAll();
        } catch (Exception e) {
          xLogger.warn("Exception while closing query", e);
        }
      }
      pm.close();
    }
    return results;
  }

  /**
   * Get orders placed by a certain user
   */
  @SuppressWarnings("unchecked")
  public Results getOrders(String userId, Date fromDate, Date toDate, PageParams pageParams)
      throws ServiceException {
    xLogger.fine("Entered getOrders (by user)");
    if (userId == null || userId.isEmpty() || fromDate == null) {
      throw new IllegalArgumentException("User ID or 'from' date not specified");
    }
    PersistenceManager pm = PMF.get().getPersistenceManager();
    String
        query =
        "SELECT FROM " + JDOUtils.getImplClass(IOrder.class).getName()
            + " WHERE uId == uIdParam && cOn > fromParam";
    Map<String, Object> params = new HashMap<String, Object>();
    params.put("uIdParam", userId);
    params.put("fromParam", LocalDateUtil.getOffsetDate(fromDate, -1, Calendar.MILLISECOND));
    if (toDate != null) {
      query += " && cOn < toParam";
      params.put("toParam", toDate);
    }
    query += " PARAMETERS String uIdParam, Date fromParam";
    if (toDate != null) {
      query += ", Date toParam";
    }
    query += " import java.util.Date; ORDER by cOn desc";
    // Form query
    Query q = pm.newQuery(query);
    if (pageParams != null) {
      QueryUtil.setPageParams(q, pageParams);
    }
    // Execute query
    List<IOrder> orders = null;
    String cursor = null;
    try {
      orders = (List<IOrder>) q.executeWithMap(params);
      if (orders != null) {
        orders.size();
        cursor = QueryUtil.getCursor(orders);
        orders = (List<IOrder>) pm.detachCopyAll(orders);
      }
    } finally {
      try {
        q.closeAll();
      } catch (Exception ignored) {
        xLogger.warn("Exception while closing query", ignored);
      }
      pm.close();
    }
    xLogger.fine("Exiting getOrders (by user)");
    return new Results(orders, cursor);
  }

  /**
   * Get demand items according to specified criteria (returns only unfulfilled demand)
   * NOTE: domainId is the mandatory attribute, all others are optional; either kiosk or material id can be specified, but NOT both
   */
  @SuppressWarnings("unchecked")
  public Results getDemandItems(Long domainId, Long kioskId, Long materialId, String kioskTag,
                                String materialTag, Date since, PageParams pageParams)
      throws ServiceException {
    xLogger.fine("Entered getDemandItems");
    if (domainId == null && kioskId == null && materialId == null) {
      throw new ServiceException(
          "Neither domain Id, kiosk Id or material Id is specified. At least one of them must be specified.");
    }
    if (kioskId != null && materialId != null) {
      throw new ServiceException(
          "Both kiosk and material are specified. Only one of them is allowed");
    }
    OrderResults results = null;
    PersistenceManager pm = PMF.get().getPersistenceManager();
    try {
      // Get query criteria
      String filter = "";
      String declaration = "";
      String imports = null;
      Map<String, Object> paramMap = new HashMap<String, Object>();
      if (domainId != null) {
        filter += "dId.contains(dIdParam)";
        declaration += "Long dIdParam";
        paramMap.put("dIdParam", domainId);
      }
      if (kioskId != null) {
        if (!filter.isEmpty()) {
          filter += " && ";
        }
        filter += "kId == kIdParam";
        if (!declaration.isEmpty()) {
          declaration += ", ";
        }
        declaration += "Long kIdParam";
        paramMap.put("kIdParam", kioskId);
      } else if (materialId != null) {
        if (!filter.isEmpty()) {
          filter += " && ";
        }
        filter += "mId == mIdParam";
        if (!declaration.isEmpty()) {
          declaration += ", ";
        }
        declaration += "Long mIdParam";
        paramMap.put("mIdParam", materialId);
      }
      // Add tags, if present
      if (kioskId == null && kioskTag != null && !kioskTag.isEmpty()) {
        if (!filter.isEmpty()) {
          filter += " && ";
        }
        filter += "ktgs.contains(ktgsParam)";
        if (!declaration.isEmpty()) {
          declaration += ", ";
        }
        declaration += "Long ktgsParam";
        paramMap.put("ktgsParam", tagDao.getTagFilter(kioskTag, ITag.KIOSK_TAG));
      } else if (materialId == null && materialTag != null && !materialTag.isEmpty()) {
        if (!filter.isEmpty()) {
          filter += " && ";
        }
        filter += "mtgs.contains(mtgsParam)";
        if (!declaration.isEmpty()) {
          declaration += ", ";
        }
        declaration += "Long mtgsParam";
        paramMap.put("mtgsParam", tagDao.getTagFilter(materialTag, ITag.MATERIAL_TAG));
      }
      // Add the time filter
      if (since != null) {
        filter += " && t > tParam";
        declaration += ", Date tParam";
        imports = "import java.util.Date;";
        paramMap.put("tParam", LocalDateUtil.getOffsetDate(since, -1, Calendar.MILLISECOND));
      }

      // Filter Order status other than Fulfilled or Cancelled
      List<String> orderStatus = Arrays.asList("cm", "cf", "pn");
      if (!filter.isEmpty()) {
        filter += " && ";
        declaration += ",";
      }
      filter += " ost.contains( st )";
      declaration += "java.util.Collection ost";
      paramMap.put("ost", orderStatus);

      // Form query
      Query q = pm.newQuery(JDOUtils.getImplClass(IDemandItem.class));
      q.setFilter(filter);
      q.declareParameters(declaration);
      q.setOrdering("t desc");
      if (imports != null) {
        q.declareImports(imports);
      }
      // Add pagination parameters, if needed
      if (pageParams != null) {
        QueryUtil.setPageParams(q, pageParams);
      }
      // Execute query
      try {
        List<IDemandItem> items = (List<IDemandItem>) q.executeWithMap(paramMap);
        items.size(); // to ensure objects are retrieved before PM is closed.
        // Filter the results to ONLY include un-fuliflled or un-cancelled demand
                                /*List<String> statusList = new ArrayList<String>();
                                statusList.add( IOrder.FULFILLED );
				statusList.add( IOrder.CANCELLED );
				List<IDemandItem> filteredResults = filterDemandItems( items, statusList );*/
        // Get the cursor of the next element in the result set (for future iteration, efficiently)
        String cursorStr = QueryUtil.getCursor(items);
        items = (List<IDemandItem>) pm.detachCopyAll(items);
        // Create the result set
        results = new OrderResults(items, cursorStr);
      } finally {
        q.closeAll();
      }
    } catch (Exception e) {
      xLogger.severe("Exception in getDemandItems: {0}", e.getMessage());
      throw new ServiceException(e.getMessage());
    } finally {
      // Close PM
      pm.close();
    }
    xLogger.fine("Exiting getDemandItems");
    return results;
  }

  public OrderResults updateOrderTransactions(
      Long domainId, String userId, String transType, List<ITransaction> inventoryTransactions,
      Long kioskId, Long trackingId,
      String message, boolean createOrder, Long servicingKioskId, Double latitude, Double longitude,
      Double geoAccuracy, String geoErrorCode,
      String utcExpectedFulfillmentTimeRangesCSV, String utcConfirmedFulfillmentTimeRange,
      BigDecimal payment, String paymentOption, String packageSize,
      boolean allowEmptyOrders,int src) throws ServiceException {

    return updateOrderTransactions(domainId, userId, transType, inventoryTransactions, kioskId,
        trackingId, message, createOrder, servicingKioskId, latitude, longitude,
        geoAccuracy, geoErrorCode, utcExpectedFulfillmentTimeRangesCSV,
        utcConfirmedFulfillmentTimeRange, payment, paymentOption,
        packageSize, allowEmptyOrders, null, null, null, null, null, null,src);
  }

  @Override
  public OrderResults updateOrderTransactions(
      Long domainId, String userId, String transType, List<ITransaction> inventoryTransactions,
      Long kioskId,
      Long trackingId, String message, boolean createOrder, Long servicingKioskId, Double latitude,
      Double longitude, Double geoAccuracy, String geoErrorCode,
      String utcExpectedFulfillmentTimeRangesCSV,
      String utcConfirmedFulfillmentTimeRange, BigDecimal payment, String paymentOption,
      String packageSize,
      boolean allowEmptyOrders, List<String> orderTags, Integer orderType, Boolean isSalesOrder,
      String referenceId, Date reqByDate, Date eta,int src) throws ServiceException {
    return updateOrderTransactions(domainId, userId, transType, inventoryTransactions, kioskId,
        trackingId, message,
        createOrder, servicingKioskId, latitude, longitude, geoAccuracy, geoErrorCode,
        utcExpectedFulfillmentTimeRangesCSV, utcConfirmedFulfillmentTimeRange, payment,
        paymentOption,
        packageSize, allowEmptyOrders, orderTags, orderType, isSalesOrder, referenceId, reqByDate,
        eta, src, null);
  }

  @Override
  public OrderResults updateOrderTransactions(
      Long domainId, String userId, String transType, List<ITransaction> inventoryTransactions,
      Long kioskId,
      Long trackingId, String message, boolean createOrder, Long servicingKioskId, Double latitude,
      Double longitude, Double geoAccuracy, String geoErrorCode,
      String utcExpectedFulfillmentTimeRangesCSV,
      String utcConfirmedFulfillmentTimeRange, BigDecimal payment, String paymentOption,
      String packageSize,
      boolean allowEmptyOrders, List<String> orderTags, Integer orderType, Boolean isSalesOrder,
      String referenceId, Date reqByDate, Date eta, int source, PersistenceManager pm)
      throws ServiceException {
    xLogger.fine("Entering updateOrderTransactions");
    if (domainId == null) {
      throw new ServiceException("Unknown domain");
    }
    boolean useLocalPM = pm == null;
    // Init. services and data
    if (useLocalPM) {
      pm = PMF.get().getPersistenceManager();
    }
    Date now = new Date(); // timestamp for transactions
    IOrder o = null;
    List<IDemandItem> items = null;
    // Flag for re-ordering
    boolean reorder = ITransaction.TYPE_REORDER.equals(transType);
    javax.jdo.Transaction tx = null;
    // Check transaction availability
    if ((inventoryTransactions == null || inventoryTransactions.size() == 0) && !(reorder
        || allowEmptyOrders)) {
      throw new ServiceException("Transaction list cannot be empty");
    }
    // Update or create order
    if (reorder) {
      if (trackingId == null) {
        xLogger.severe("No tracking id sent on re-order for kiosk {0}", kioskId);
        throw new ServiceException("Order id was not specified");
      }
      LockUtil.LockStatus lockStatus = LockUtil.lock(Constants.TX_O + trackingId);
      if (!LockUtil.isLocked(lockStatus)) {
        throw new ServiceException("O002", trackingId);
      }
      try {
        // Get the order
        o = JDOUtils.getObjectById(IOrder.class, trackingId, pm);
        IDemandService ds = Services.getService(DemandService.class);
        o.setItems(ds.getDemandItems(o.getOrderId(), pm));
        xLogger.fine("inventoryTransactions: {0}, order size: {1}",
            (inventoryTransactions == null ? "NULL" : inventoryTransactions.size()), o.size());
        o.setDueDate(reqByDate);
        o.setExpectedArrivalDate(eta);
        modifyOrder(o, userId, inventoryTransactions, now, domainId, transType, message,
            utcExpectedFulfillmentTimeRangesCSV, utcConfirmedFulfillmentTimeRange, payment,
            paymentOption, packageSize, allowEmptyOrders, orderTags, orderType, referenceId, pm);
        // Prevent an order from being edited out of all items, unless empty orders are allowed
        if (!allowEmptyOrders && o.size() == 0) {
          throw new ServiceException("Order has no items with a quantity greater than zero");
        }
        // Persist the order and item updates
        if (useLocalPM) {
          tx = pm.currentTransaction();
          tx.begin();
        }
        UpdatedOrder uo = updateOrder(o, source, true, true, userId, pm);
        o = uo.order;

        List<IDemandItem> localItems = (List<IDemandItem>) o.getItems();
        o.setNumberOfItems(localItems.size());
        o = pm.detachCopy(o);
        o.setItems((List<IDemandItem>) pm.detachCopyAll(localItems));
        if (useLocalPM) {
          tx.commit();
        }
      } catch (JDOObjectNotFoundException e) {
        xLogger
            .severe("Order with i {0} not found while re-ordering for kiosk {1}: {2}", trackingId,
                kioskId, e.getMessage(), e);
        throw new ServiceException(
            messages.getString("order") + " " + trackingId + " " + backendMessages
                .getString("error.notfound"));
      } catch (Exception e) {
        xLogger.severe("Exception while re-ordering: {0}", e.getMessage(), e);
        throw new ServiceException(e);
      } finally {
        if (LockUtil.shouldReleaseLock(lockStatus) && !LockUtil
            .release(Constants.TX_O + trackingId)) {
          xLogger.warn("Unable to release lock for key {0}", Constants.TX_O + trackingId);
        }
        if (useLocalPM) {
          if (tx != null && tx.isActive()) {
            tx.rollback();
          }
          pm.close();
        }
      }
    } else {
      // First time order
      MaterialCatalogService mcs = Services.getService(MaterialCatalogServiceImpl.class);
      InventoryManagementService
          ims =
          Services.getService(InventoryManagementServiceImpl.class);
      List<IDemandItem> demandList = new ArrayList<>(); // demand list
      if (inventoryTransactions != null && !inventoryTransactions.isEmpty()) {
        Iterator<ITransaction> it = inventoryTransactions.iterator();
        // Get the transactions and demand items
        while (it.hasNext()) {
          ITransaction trans = it.next();
          // Update timestamp, if needed
          if (trans.getTimestamp() == null) {
            trans.setTimestamp(now);
          }

          // Update the transaction with the domainId, and the parent domains (superdomains)
          DomainsUtil
              .addToDomain(trans, domainId, null); /// earlier: trans.setDomainId( domainId );

          // Update trans. type
          trans.setType(transType);
          // Get material
          Long materialId = trans.getMaterialId();
          IMaterial m = mcs.getMaterial(materialId);
          // Get inventory
          IInvntry inv = ims.getInventory(trans.getKioskId(), materialId);
          if (inv == null) {
            xLogger.warn(
                "Inv. for kiosk-material {0}-{1} in domain {2} is not available. Cannot process order.",
                trans.getKioskId(), materialId, domainId);
            throw new ServiceException("Material " + m.getName()
                + " is not available at this entity. Please contact administrator and ensure it is configured.");
          }
          // Check if this demand item is already in the list (possible when there are multiple transactions for same material of different batches)
          if (trans.hasBatch()) {
            IDemandItem item = getDemandItemByMaterial(demandList, materialId);
            if (item != null) {
              item.addBatch(JDOUtils.createInstance(IDemandItemBatch.class).init(trans));
            }
          }
          try {
            validateHU(trans.getQuantity(), m.getMaterialId(), m.getName());
          } catch (LogiException e) {
            throw new ServiceException(e.getCode(), e.getMessage());
          }
          // Add to demand list
          demandList.add(getDemandItem(trans, m, inv,
              ims)); // if transaction has batch, then a DemandItemBatch is also created within the demand item
        } // end while
      }
      // Make the updated objects persistent
      try {
        // Update demand items and orders, if necessary
        if (createOrder) {
          // Get tax info. if present (from kiosk)
          BigDecimal taxPercent = BigDecimal.ZERO;
          String currency = null;
          List<String> kioskTags = null;
          // Get kiosk
          if (kioskId != null) {
            EntitiesService as = Services.getService(EntitiesServiceImpl.class);
            IKiosk k = as.getKiosk(kioskId, false);
            taxPercent = k.getTax();
            currency = k.getCurrency();
            kioskTags = k.getTags();
          }
          // Create dummy order, so we can get order Id (created by system)
          o = JDOUtils.createInstance(IOrder.class);
//					o.setItems( new HashSet<IDemandItem>() );
          o = pm.makePersistent(o);
          o = getDetached(o, pm);
          // Persist the order and its items (via transaction, given demand items will also be updated)
          if (useLocalPM) {
            tx = pm.currentTransaction();
            tx.begin();
          }
          // Update order data
          createOrder(o, domainId, kioskId, userId, demandList, message, taxPercent, currency,
              servicingKioskId, latitude, longitude, geoAccuracy, geoErrorCode,
              utcExpectedFulfillmentTimeRangesCSV, utcConfirmedFulfillmentTimeRange, payment,
              paymentOption, packageSize, kioskTags, orderTags, orderType, referenceId);
          o.setNumberOfItems(demandList.size());
          o.setExpectedArrivalDate(eta);
          o.setDueDate(reqByDate);
          o.setSrc(source);
          DomainConfig dc = DomainConfig.getInstance(domainId);
          ApprovalsConfig approvalsConfig = dc.getApprovalsConfig();
          boolean isApprovalRequired = StaticApplicationContext.getBean(
              IOrderApprovalsService.class).isApprovalRequired(o);
          if(isApprovalRequired){
            o = setOrderVisibility(o, approvalsConfig);
            if(IOrder.PURCHASE_ORDER.equals(o.getOrderType())
                || IOrder.TRANSFER_ORDER.equals(o.getOrderType())) {
              o = setDomainVisibility(o);
              o.setDomainId(domainId);
            } else {
              DomainsUtil.addToDomain(o, domainId, null);
            }
          } else {
            o.setVisibleToCustomer(true);
            o.setVisibleToVendor(true);
            DomainsUtil.addToDomain(o, domainId, null);
          }
          o = pm.makePersistent(o);
          demandList = (List<IDemandItem>) pm.makePersistentAll(demandList);
          demandList = (List<IDemandItem>) pm.detachCopyAll(demandList);
          if (isSalesOrder != null && isSalesOrder && dc.getOrdersConfig()
              .allowSalesOrderAsConfirmed()) {
            o.setStatus(IOrder.CONFIRMED);
            if (dc.autoGI() && dc.getOrdersConfig().allocateStockOnConfirmation()) {
              for (IDemandItem d : demandList) {
                String tag = IInvAllocation.Type.ORDER.toString().concat(":")
                    .concat(String.valueOf(d.getOrderId()));
                try {
                  ims.allocateAutomatically(o.getServicingKiosk(), d.getMaterialId(),
                      IInvAllocation.Type.ORDER, String.valueOf(d.getOrderId()), tag,
                      d.getOriginalQuantity(), d.getUserId(), pm);
                  d.setStatus(IOrder.CONFIRMED);
                } catch (InventoryAllocationException invException) {
                  xLogger.warn("Could not allocate fully to Order for k: {0}, m: {1}, q: {2}",
                      o.getServicingKiosk(), d.getMaterialId(), d.getOriginalQuantity(),
                      invException);
                }
              }
            }
          }
          IMessage iMessage = null;
          if (message != null && !message.isEmpty()) {
            iMessage = addMessageToOrder(o.getOrderId(), o.getDomainId(), message, userId, pm);
          }
          addStatusHistory(o.getOrderId(), null, o.getStatus(), o.getDomainId(), iMessage, userId,
              pm);
          o = pm.detachCopy(o);
          o.setItems(demandList);
          if (useLocalPM) {
            tx.commit();
          }
          // Increment order counter
//					incrementOrderCounter( o, 1, pm );
          // Generate event
          generateEvent(domainId, IEvent.CREATED, o, null, null);
          if (BigUtil.notEqualsZero(payment)) {
            generateEvent(domainId, IEvent.PAID, o, null, null);
          }
        } else {
          // Simply persist demand items (without order)
          pm.makePersistentAll(demandList);
          // Get demand list
          items = new ArrayList<>();
          for (IDemandItem aDemandList : demandList) {
            items.add(aDemandList);
          }
        }
      } catch (Exception e) {
        xLogger.severe("Exception: {0}", e.getMessage(), e);
        throw new ServiceException(e);
      } finally {
        if (useLocalPM) {
          if (tx != null && tx.isActive()) {
            tx.rollback();
          }
          // Close the persistence manager
          pm.close();
        }
      }
    }
    // Update kiosk activity timestamp
    updateEntityActivityTimestamps(o);
    return new OrderResults(items, null, o);
  }

  private IOrder setOrderVisibility(IOrder o, ApprovalsConfig approvalsConfig)
      throws ServiceException {
    if(approvalsConfig != null) {
      if(IOrder.PURCHASE_ORDER.equals(o.getOrderType())) {
          o.setVisibleToCustomer(true);
          o.setVisibleToVendor(false);
      } else if(IOrder.SALES_ORDER.equals(o.getOrderType())) {
          o.setVisibleToCustomer(true);
          o.setVisibleToVendor(true);
      } else if(IOrder.TRANSFER_ORDER.equals(o.getOrderType())) {
          o.setVisibleToCustomer(false);
          o.setVisibleToVendor(false);
        }
    }
    return o;
  }

  private IOrder setDomainVisibility(IOrder o) throws ServiceException {
    EntitiesService entitiesService = Services.getService(EntitiesServiceImpl.class, getLocale());
    if(IOrder.PURCHASE_ORDER.equals(o.getOrderType())) {
      IKiosk kiosk = entitiesService.getKiosk(o.getKioskId());
      o.setDomainIds(DomainsUtil.getVisibleDomains(kiosk.getDomainId(), IDomainLink.TYPE_PARENT));
    } else if(IOrder.TRANSFER_ORDER.equals(o.getOrderType())) {
      o.setDomainIds(DomainsUtil.getVisibleDomains(SecurityUtils.getCurrentDomainId(), IDomainLink.TYPE_PARENT));
    }
    return o;
  }

  private void validateHU(BigDecimal quantity, Long mId, String mName) throws LogiException {
    IHandlingUnitService hus = Services.getService(HandlingUnitServiceImpl.class);
    Map<String, String> huData = hus.getHandlingUnitDataByMaterialId(mId);
    if (huData != null) {
      if (BigUtil.notEqualsZero(quantity.remainder(new BigDecimal(huData.get("quantity"))))) {
        if (mName == null) {
          MaterialCatalogService mcs = Services.getService(MaterialCatalogServiceImpl.class);
          IMaterial m = mcs.getMaterial(mId);
          mName = m.getName();
        }
        throw new LogiException("T001", quantity.stripTrailingZeros().toPlainString(), mName,
            huData.get(IHandlingUnit.NAME), huData.get(IHandlingUnit.QUANTITY), mName);
      }
    }
  }

	/*@Override
        public void modifyOrder ( IOrder o, String userId, List<ITransaction> transactions, Date timestamp, Long domainId, String transType, String message, String utcEstimatedFulfillmentTimeRanges, String utcConfirmedFulfillmentTimeRange, BigDecimal payment, String paymentOption, String packageSize, boolean allowEmptyOrders ) throws ServiceException {
		modifyOrder(o, userId, transactions, timestamp, domainId, transType, message, utcEstimatedFulfillmentTimeRanges, utcConfirmedFulfillmentTimeRange, payment, paymentOption, packageSize, allowEmptyOrders, null, null, null);
	}*/

  // Get an order given a demand list
  private void createOrder(
      IOrder o, Long domainId, Long kioskId, String userId, List<IDemandItem> items, String message,
      BigDecimal taxPercent, String currency, Long servicingKioskId, Double latitude,
      Double longitude,
      Double geoAccuracy, String geoErrorCode, String utcEstimatedFulfillmentTimeRangesCSV,
      String utcConfirmedFulfillmentTimeRange, BigDecimal payment, String paymentOption,
      String packageSize,
      List<String> kioskTags, List<String> orderTags, Integer orderType, String referenceId
  ) throws ServiceException {
    xLogger.fine("Entered createOrder");
    Date t;
    if (items != null && !items.isEmpty()) {
      t = items.iterator().next().getTimestamp();
    } else {
      t = new Date();
    }

    o.setKioskId(kioskId);
    try {
      servicingKioskId = TransactionUtil.getDefaultVendor(domainId, kioskId, servicingKioskId);
    } catch (ServiceException se) {
      xLogger.warn("{0} while getting default vendor for an order. Message: {1}",
          se.getClass().getName(), se.getMessage());
    }
    o.setServicingKiosk(servicingKioskId);
    o.setCreatedOn(t);
    o.setUpdatedOn(t);
    o.setStatus(IOrder.PENDING);
//		o.setThreadedMessage( message, userId );
    if (currency != null) {
      o.setCurrency(currency);
    }
    o.setUserId(userId);
    o.setUpdatedBy(userId);
    o.setTax(taxPercent);
    if (latitude != null) {
      o.setLatitude(latitude);
    }
    if (longitude != null) {
      o.setLongitude(longitude);
    }
    if (geoAccuracy != null) {
      o.setGeoAccuracy(geoAccuracy);
    }
    if (geoErrorCode != null) {
      o.setGeoErrorCode(geoErrorCode);
    }
    // Set tags
    o.setTgs(tagDao.getTagsByNames(kioskTags, ITag.KIOSK_TAG), TagUtil.TYPE_ENTITY);
    o.setTgs(tagDao.getTagsByNames(orderTags, ITag.ORDER_TAG), TagUtil.TYPE_ORDER);
    /*if (orderType == 2) {
      orderType = 1;
    }*/
    o.setOrderType(orderType);
    /*// Add the order to this domain and parent domains (superdomains)
    // NOTE: kioskId has to be set in the order first, before this call, given addToDomain relies on the kiosk to get the domain Ids
    DomainsUtil.addToDomain(o, domainId, null);*/
    // Update the demand items with order id and compute price
    if (items != null && !items.isEmpty()) {
      Iterator<IDemandItem> it = items.iterator();
      while (it.hasNext()) {
        IDemandItem item = it.next();
        // Add order id to item
        item.updateOId(o);
      }
      // Add the items to the order
      o.setItems(items);
      // Lastly, calculate total order price and set it (ensure tax, if any, is set before a price computation)
      o.setTotalPrice(
          o.computeTotalPrice()); // NOTE: price computation is always taken from this function to ensure correct computations (even at cost of minor inefficiency)
      o.setReferenceID(referenceId);
    }
    // Update other order metadata
    updateOrderMetadata(o, utcEstimatedFulfillmentTimeRangesCSV, utcConfirmedFulfillmentTimeRange,
        payment, paymentOption, packageSize);
  }

  @Override
  public void modifyOrder(IOrder o, String userId, List<ITransaction> transactions, Date timestamp,
                          Long domainId, String transType, String message,
                          String utcEstimatedFulfillmentTimeRanges,
                          String utcConfirmedFulfillmentTimeRange, BigDecimal payment,
                          String paymentOption, String packageSize, boolean allowEmptyOrders,
                          List<String> orderTags, Integer orderType, String referenceId)
      throws ServiceException {
    modifyOrder(o, userId, transactions, timestamp, domainId, transType, message,
        utcEstimatedFulfillmentTimeRanges, utcConfirmedFulfillmentTimeRange, payment, paymentOption,
        packageSize, allowEmptyOrders, orderTags, orderType, referenceId, null);
  }

  @Override
  // Modify order status and its items
  public void modifyOrder(IOrder o, String userId, List<ITransaction> transactions, Date timestamp,
                          Long domainId,
                          String transType, String message,
                          String utcEstimatedFulfillmentTimeRanges,
                          String utcConfirmedFulfillmentTimeRange,
                          BigDecimal payment, String paymentOption, String packageSize,
                          boolean allowEmptyOrders,
                          List<String> orderTags, Integer orderType, String referenceId,
                          PersistenceManager pm) throws ServiceException {
    Date t = null;
    MaterialCatalogService mcs = Services.getService(MaterialCatalogServiceImpl.class);
    InventoryManagementService
        ims =
        Services.getService(InventoryManagementServiceImpl.class);
    if (transactions != null && !transactions.isEmpty()) {
      Iterator<ITransaction> it = transactions.iterator();
      while (it.hasNext()) {
        ITransaction trans = it.next();
        // Update transaction data
        if (trans.getTimestamp() == null) {
          trans.setTimestamp(timestamp);
          t = timestamp;
        } else {
          t = trans.getTimestamp();
        }
        if (userId == null) {
          userId = trans.getSourceUserId();
        }

        // Add the transaction to this domain and parent domains
        DomainsUtil.addToDomain(trans, domainId, null); /// earlier: trans.setDomainId( domainId );
        trans.setType(transType);

        try {
          validateHU(trans.getQuantity(), trans.getMaterialId(), null);
        } catch (LogiException e) {
          throw new ServiceException(e.getCode(), e.getMessage());
        }
        // Get the demand item
        IDemandItem item = o.getItem(trans.getMaterialId());
        if (item == null) {
          Long materialId = trans.getMaterialId();
          // A new item has to be added to the order
          try {
            item =
                getDemandItem(trans, mcs.getMaterial(trans.getMaterialId()),
                    ims.getInventory(trans.getKioskId(), materialId), ims);
            if (item.getOrderId() == null) {
              item.updateOId(o);
            }
            // Add item to order
            ((List<IDemandItem>) o.getItems()).add(item);
          } catch (Exception e) {
            xLogger.warn(
                "{0} when getting material/inventory for a newly added item to order {1}: materialId = {2}, kioskId = {3}:",
                e.getClass().getName(), o.getOrderId(), materialId, trans.getKioskId(), e);
          }
        } else {
          // Update item data
          BigDecimal q = trans.getQuantity();
          if (BigUtil.lesserThanZero(q)) {
            xLogger.warn("Invalid quantity for re-order for material {0} in order {1}",
                trans.getMaterialId(), o.getIdString());
            continue; // go to next item
          } else {
            item.setQuantity(q);
          }
          if (trans.getReason() != null && !trans.getReason().isEmpty()) {
            item.setReason(trans.getReason());
          }
          item.setTimestamp(t); /// timestamp );
          item.setUserId(trans.getSourceUserId());
          if (trans.getMessage() != null) {
            item.setMessage(trans.getMessage());
          }
          if (BigUtil.equalsZero(q)) {
            // Cancel this item
            item.setStatus(IOrder.CANCELLED);
          } else {
            // Set changed status
            item.setStatus(IOrder.CHANGED); // we will keep the CHANGED status at the item level
          }
          if (trans.getEditOrderQtyReason() != null && !trans.getEditOrderQtyReason().isEmpty()) {
            item.setShippedDiscrepancyReason(trans.getEditOrderQtyReason());
          }
        }
      }
    } // end if ( transactions != null && ... )
    // Iterate through demand items for the order. If all items have quantity 0 and if allowEmptyOrders is false then throw exception
    if (allItemsZeroQty(o) && !allowEmptyOrders) {
      throw new ServiceException("An order should have atleast one item");
    }

    // Recompute the order's price based on the above
    o.setTotalPrice(o.computeTotalPrice());
    o.setTgs(tagDao.getTagsByNames(orderTags, ITag.ORDER_TAG), TagUtil.TYPE_ORDER);
    o.setOrderType(orderType);
    DomainsUtil.addToDomain(o, o.getDomainId(), null);
    // Update other order metadata
    updateOrderMetadata(o, utcEstimatedFulfillmentTimeRanges, utcConfirmedFulfillmentTimeRange,
        payment, paymentOption, packageSize);
    // Set timestamp and user
    if (t == null) {
      t = new Date();
    }
    o.setUpdatedOn(t);
    o.setUpdatedBy(userId);
    if (message != null) {
      ConversationService
          cs =
          Services.getService(ConversationServiceImpl.class, this.getLocale());
      cs.addMsgToConversation("ORDER", String.valueOf(o.getOrderId()), message, userId,
          Collections.singleton("ORDER:" + o.getOrderId())
          , o.getDomainId(), pm);
      generateOrderCommentEvent(domainId, IEvent.COMMENTED, JDOUtils.getImplClassName(IOrder.class),
          o.getOrderId().toString(), null, null);
    }
    o.setReferenceID(referenceId);
  }

  // Update other order metadata (fulfillment times, payment options, package size, etc.)
  private void updateOrderMetadata(IOrder o, String utcEstimatedFulfillmentTimeRangesCSV,
                                   String utcConfirmedFulfillmentTimeRange, BigDecimal payment,
                                   String paymentOption, String packageSize) {
    // Update estimated fulfillment time ranges
    if (utcEstimatedFulfillmentTimeRangesCSV != null) {
      o.setExpectedFulfillmentTimeRangesCSV(utcEstimatedFulfillmentTimeRangesCSV);
    }
    // Update the order with fulfillment time ranges, payment options, package size, etc.
    if (utcConfirmedFulfillmentTimeRange != null) {
      o.setConfirmedFulfillmentTimeRange(utcConfirmedFulfillmentTimeRange);
    }
    // Add payment to order
    if (payment != null && BigUtil.notEqualsZero(payment)) {
      o.addPayment(payment);
      o.commitPayment(payment);
    }
    // Payment option
    if (paymentOption != null) {
      o.setPaymentOption(paymentOption);
    }

  }

  private BigDecimal computeRecommendedOrderQuantity(IInvntry invntry) {
    BigDecimal roq = new BigDecimal(-1);
    BigDecimal huQty;
    if (IInvntry.MODEL_SQ.equals(invntry.getInventoryModel())) {
      roq =
          BigUtil.lesserThanZero(invntry.getEconomicOrderQuantity()) ? BigDecimal.ZERO
              : invntry.getEconomicOrderQuantity();
    } else if (BigUtil.greaterThanZero(invntry.getMaxStock())) {
      if (BigUtil
          .lesserThan(invntry.getStock().add(invntry.getInTransitStock()), invntry.getMaxStock())) {
        roq =
            invntry.getMaxStock().subtract(invntry.getStock())
                .subtract(invntry.getInTransitStock());
      } else {
        roq = BigDecimal.ZERO;
      }
    }
    if(BigUtil.notEqualsZero(roq)) {
      try {
        IHandlingUnitService hus = Services.getService(HandlingUnitServiceImpl.class);
        Map<String, String> hu = hus.getHandlingUnitDataByMaterialId(invntry.getMaterialId());
        if (hu != null) {
          huQty = new BigDecimal(hu.get(IHandlingUnit.QUANTITY));
          roq = roq.divide(huQty, 0, RoundingMode.CEILING).multiply(huQty);
        } else {
          roq = roq.setScale(0, BigDecimal.ROUND_UP);
        }
      } catch (Exception e) {
        xLogger.warn("Error while fetching Handling Unit {0}", invntry.getMaterialId(), e);
      }
    }
    return roq;
  }

  // Get a demand item, given a transaction
  private IDemandItem getDemandItem(ITransaction trans, IMaterial m, IInvntry inv,
                                    InventoryManagementService ims) {
    IDemandItem di = JDOUtils.createInstance(IDemandItem.class);
    di.setDomainId(trans.getDomainId());
    di.setKioskId(trans.getKioskId());
    di.setMaterialId(trans.getMaterialId());
    // Add this object to this domain and parent domains
    ///DomainsUtil.addToDomain( di, trans.getDomainId(), null ); /// earlier: di.setDomainId( trans.getDomainId() );
    di.setDomainId(trans.getDomainId());
    di.addDomainIds(trans.getDomainIds());

    BigDecimal q = trans.getQuantity();
    di.setQuantity(q);
    if (BigUtil.equalsZero(
        di.getOriginalQuantity())) { // must be first-time order, set the original quantity
      di.setOriginalQuantity(q);
      //Update recommended order quantity
      di.setRecommendedOrderQuantity(computeRecommendedOrderQuantity(inv));
    }
    di.setReason(trans.getReason());

    di.setStatus(IOrder.PENDING);
    di.setTimestamp(trans.getTimestamp());
    di.setMessage(trans.getMessage());
    di.setUserId(trans.getSourceUserId());
    // Set tags
    if (inv != null) {
      di.setTgs(tagDao.getTagsByNames(inv.getTags(TagUtil.TYPE_ENTITY), ITag.KIOSK_TAG),
          TagUtil.TYPE_ENTITY);
      di.setTgs(tagDao.getTagsByNames(inv.getTags(TagUtil.TYPE_MATERIAL), ITag.MATERIAL_TAG),
          TagUtil.TYPE_MATERIAL);
    }
    if (trans.hasBatch()) {
      di.addBatch(JDOUtils.createInstance(IDemandItemBatch.class).init(trans));
    }
    // Add price metadata
    BigDecimal p = m.getRetailerPrice();
    if (inv != null && BigUtil.notEqualsZero(inv.getRetailerPrice())) {
      p = inv.getRetailerPrice();
    }
    if (BigUtil.greaterThanZero(p)) {
      di.setUnitPrice(p);
      di.setCurrency(m.getCurrency());
    }
    // If inventory available, check/set tax rate
    if (inv != null) {
      di.setTax(inv.getTax());
      di.setTimeToOrder(ims.getDurationFromRP(inv.getKey()));
    }
    di.setShippedDiscrepancyReason(trans.getEditOrderQtyReason());

    return di;
  }

  // Generate order events, if configured
  private void generateEvent(Long domainId, int eventId, IOrder o, String message,
                             List<String> userIds) {
    try {

      Map<String, Object> params = null;
      if (eventId == IEvent.STATUS_CHANGE) {
        params = new HashMap<>();
        params.put(EventConstants.PARAM_STATUS, o.getStatus());
      }
      // Custom options
      CustomOptions customOptions = new CustomOptions();
      if (message != null && !message.isEmpty() || (userIds != null && !userIds.isEmpty())) {
        customOptions.message = message;
        if (userIds != null && !userIds.isEmpty()) {
          Map<Integer, List<String>> userIdsMap = new HashMap<Integer, List<String>>();
          userIdsMap.put(EventSpec.NotifyOptions.IMMEDIATE, userIds);
          customOptions.userIds = userIdsMap;
        }
      }
      // Generate event, if needed
      EventPublisher.generate(domainId, eventId, params,
          JDOUtils.getImplClass(IOrder.class).getName(), orderDao.getKeyAsString(o), customOptions);
    } catch (Exception e) {
      xLogger.severe("{0} when generating Order event {1} for order {2} in domain {3}: {4}",
          e.getClass().getName(), eventId, o.getOrderId(), domainId, e);
    }
  }

  @Override
  public List<IDemandItem> getDemandItemByStatus(Long kioskId, Long materialId,
                                                 Collection<String> status)
      throws ServiceException {
    if (kioskId == null || materialId == null || status == null) {
      throw new ServiceException("KioskId, MaterialId and status are mandatory");
    }
    PersistenceManager pm = PMF.get().getPersistenceManager();
    Query q = pm.newQuery(JDOUtils.getImplClass(IDemandItem.class));
    String filter = "kId == kIdParam && mId == mIdParam && ost.contains(st)";
    String declaration = "Long kIdParam, Long mIdParam, java.util.Collection ost";
    Map<String, Object> paramMap = new HashMap<>(3);
    paramMap.put("kIdParam", kioskId);
    paramMap.put("mIdParam", materialId);
    paramMap.put("ost", status);
    try {
      q.setFilter(filter);
      q.declareParameters(declaration);
      List<IDemandItem> di = (List<IDemandItem>) q.executeWithMap(paramMap);
      return (List<IDemandItem>) pm.detachCopyAll(di);
    } catch (Exception e) {
      xLogger.warn("Error while getting demand item by status for kioskID {0}, materialId {1}",
          kioskId, materialId, e);
    } finally {
      try {
        q.closeAll();
      } catch (Exception ignored) {

      }
      pm.close();
    }
    return null;
  }


  public List<String> getIdSuggestions(Long domainId, String id, String type, Integer oty,
                                       List<Long> kioskIds) throws ServiceException {
    List<String> filterIds = new ArrayList<>();
    String filterQuery = "SELECT ID_OID FROM ORDER_DOMAINS WHERE DOMAIN_ID = " + domainId;
    StringBuilder sqlQuery = new StringBuilder();
      if(StringUtils.isNotEmpty(type)) {
          if ("rid".equals(type)) {
              sqlQuery.append("SELECT DISTINCT RID FROM `ORDER` WHERE ID IN (").append(filterQuery)
                      .append(") AND RID LIKE '").append(id).append("%' ");
          } else if ("oid".equals(type)) {
              sqlQuery.append("SELECT ID FROM `ORDER` WHERE ID IN(");
              sqlQuery.append(filterQuery).append(" AND ID_OID LIKE '").append(id).append("%')");
          }
      }
    if (oty != null) {
      sqlQuery.append(" AND OTY = ").append(oty);
    }
    if (kioskIds != null && !kioskIds.isEmpty()) {
      sqlQuery.append(" AND (KID IN(");
      for (Long kid : kioskIds) {
        sqlQuery.append(kid).append(CharacterConstants.COMMA);
      }
      sqlQuery.setLength(sqlQuery.length() - 1);
      sqlQuery.append(") OR SKID IN(");
      for (Long kid : kioskIds) {
        sqlQuery.append(kid).append(CharacterConstants.COMMA);
      }
      sqlQuery.setLength(sqlQuery.length() - 1);
      sqlQuery.append("))");
    }
    sqlQuery.append(CharacterConstants.SPACE);

    sqlQuery.append("LIMIT 0,8");
    PersistenceManager pm = PMF.get().getPersistenceManager();
    Query query = pm.newQuery("javax.jdo.query.SQL", sqlQuery.toString());
    try {
      List rs = (List) query.execute();
      for (Object r : rs) {
        String a = String.valueOf(r);
        if (a != null) {
          filterIds.add(a);
        }
      }
    } catch (Exception e) {
      xLogger.warn("Error in fetching id suggestions for domain:{0}", domainId, e);
    } finally {
      query.closeAll();
      pm.close();
    }
    return filterIds;
  }

  public BigDecimal getLeadTime(Long kid, Long mid, float orderPeriodicityInConfig,
                                LeadTimeAvgConfig leadTimeAvgConfig, float leadTimeDefaultInConfig)
      throws ServiceException {
    BigDecimal avgLeadTime = BigDecimal.ZERO;
    if (kid == null || mid == null) {
      xLogger.warn("Either Kiosk ID or material ID is null, kid: {0}, mid: {1}", kid, mid);
      return avgLeadTime;
    }
    float maxOrderPeriods = LeadTimeAvgConfig.MAX_ORDER_PERIODS_DEFAULT;
    int minNumberOfOrders = LeadTimeAvgConfig.MINIMUM_NUMBER_OF_ORDERS_DEFAULT;
    int maxNumberOfOrders = LeadTimeAvgConfig.MAXIMUM_NUMBER_OF_ORDERS_DEFAULT;
    boolean excludeProcessingTime = false;
    if (leadTimeAvgConfig != null) {
      maxOrderPeriods = leadTimeAvgConfig.getMaxOrderPeriods();
      maxNumberOfOrders = leadTimeAvgConfig.getMaxNumOfOrders();
      minNumberOfOrders = leadTimeAvgConfig.getMinNumOfOrders();
      excludeProcessingTime = leadTimeAvgConfig.getExcludeOrderProcTime();
    }

    InventoryManagementService ims = Services.getService(InventoryManagementServiceImpl.class);
    EntitiesService es = Services.getService(EntitiesServiceImpl.class);
    Results results = es.getLinkedKiosks(kid, IKioskLink.TYPE_VENDOR, null, null);
    boolean kskHasMoreThanOneVnd = false;
    if (results.getResults().size() > 1) {
      kskHasMoreThanOneVnd = true;
    }
    IInvntry inv = ims.getInventory(kid, mid);
    BigDecimal orderPeriodicity = inv.getOrderPeriodicity();
    if (BigUtil.equalsZero(orderPeriodicity)) {
      orderPeriodicity = new BigDecimal(orderPeriodicityInConfig);
    }
    int maxHistoricalPeriod = orderPeriodicity.multiply(new BigDecimal(maxOrderPeriods)).intValue();
    List<String> parameters = new ArrayList<>(1);
    StringBuilder sqlQuery = new StringBuilder("SELECT AVG(DLT_ALIAS)");
    if (!excludeProcessingTime) {
      sqlQuery.append(" + AVG(PT_ALIAS)");
    }
    sqlQuery.append(", COUNT(1) FROM (");
    sqlQuery.append("SELECT DLT DLT_ALIAS");
    if (!excludeProcessingTime) {
      sqlQuery.append(", PT PT_ALIAS");
    }
    sqlQuery.append(" FROM `ORDER` WHERE ");
    if (kskHasMoreThanOneVnd) {
      sqlQuery.append("ID IN (SELECT DISTINCT OID FROM DEMANDITEM WHERE KID = ")
          .append(CharacterConstants.QUESTION);
      parameters.add(String.valueOf(kid));
      sqlQuery.append(" AND MID = ").append(CharacterConstants.QUESTION)
          .append(CharacterConstants.C_BRACKET);
      parameters.add(String.valueOf(mid));
    } else {
      sqlQuery.append("KID = ").append(CharacterConstants.QUESTION);
      parameters.add(String.valueOf(kid));
    }
    sqlQuery.append(" AND ST = '").append(IOrder.FULFILLED).append("'");

    sqlQuery.append(" AND UON >= (DATE_SUB(NOW(),INTERVAL ").append(maxHistoricalPeriod)
        .append(" DAY))").append(" ORDER BY UON DESC LIMIT 0,").append(maxNumberOfOrders);
    sqlQuery.append(") ALIAS");
    PersistenceManager pm = PMF.get().getPersistenceManager();
    Query query = pm.newQuery("javax.jdo.query.SQL", sqlQuery.toString());
    try {
      List queryResults = (List) query.executeWithArray(parameters.toArray());
      if (queryResults != null && !queryResults.isEmpty()) {
        Iterator iterator = queryResults.iterator();
        while (iterator.hasNext()) {
          Object[] resultsArray = (Object[]) iterator.next();
          avgLeadTime = (BigDecimal) resultsArray[0];
          long numberOfOrders = (long) resultsArray[1];
          if (numberOfOrders < minNumberOfOrders) {
            avgLeadTime = new BigDecimal(leadTimeDefaultInConfig);
          }
        }
      }
    } catch (Exception e) {
      xLogger.warn("Error while calculating average lead time for kid: {0}, mid: {1}", kid, mid, e);
    } finally {
      query.closeAll();
      pm.close();
    }
    return avgLeadTime;
  }


  @Override
  public void updateOrderMetadata(Long orderId, String updatedBy,
                                  PersistenceManager persistenceManager) {
    Boolean isLocalPersistentManager = Boolean.FALSE;
    if (persistenceManager == null) {
      persistenceManager = PMF.get().getPersistenceManager();
      isLocalPersistentManager = Boolean.TRUE;
    }
    IOrder order = JDOUtils.getObjectById(IOrder.class, orderId, persistenceManager);
    order.setUpdatedBy(updatedBy);
    order.setUpdatedOn(new Date());

    if (isLocalPersistentManager) {
      persistenceManager.close();
    }
  }

  private void updateEntityActivityTimestamps(IOrder o) {
    // Modify the active time stamps of the entities in the order.
    Set<Long> kids = new HashSet<>(1);
    kids.add(o.getKioskId());
    if (o.getServicingKiosk() != null) {
      kids.add(o.getServicingKiosk());
    }
    for (Long kid : kids) {
      Map<String, String> params = new HashMap<>(3);
      try {
        params.put("entityId", String.valueOf(kid));
        params.put("timestamp", String.valueOf(o.getCreatedOn().getTime()));
        params.put("actType", String.valueOf(IKiosk.TYPE_ORDERACTIVITY));
        taskService
            .schedule(ITaskService.QUEUE_DEFAULT, UPDATE_ENTITYACTIVITYTIMESTAMPS_TASK, params,
                ITaskService.METHOD_POST);
      } catch (TaskSchedulingException e) {
        xLogger.warn(
            "Error while scheduling update entity activity timestamp for entityId {0} in order {1}",
            o.getKioskId(), o.getOrderId(), e);
      }
    }
  }

  private boolean allItemsZeroQty(IOrder o) {
    List<IDemandItem> its = (List<IDemandItem>) o.getItems();
    boolean allQtyZero = true;
    if (its != null) {
      for (IDemandItem it : its) {
        if (BigUtil.equalsZero(it.getQuantity())) {
          continue;
        } else {
          allQtyZero = false;
          break;
        }
      }
    }
    return allQtyZero;
  }


}

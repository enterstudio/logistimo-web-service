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

package com.logistimo.shipments.service.impl;

import com.logistimo.AppFactory;
import com.logistimo.activity.entity.IActivity;
import com.logistimo.activity.models.ActivityModel;
import com.logistimo.activity.service.ActivityService;
import com.logistimo.activity.service.impl.ActivityServiceImpl;
import com.logistimo.auth.SecurityConstants;
import com.logistimo.auth.SecurityUtil;
import com.logistimo.config.models.DomainConfig;
import com.logistimo.config.models.EventSpec;
import com.logistimo.constants.CharacterConstants;
import com.logistimo.constants.Constants;
import com.logistimo.constants.QueryConstants;
import com.logistimo.constants.SourceConstants;
import com.logistimo.conversations.entity.IMessage;
import com.logistimo.conversations.service.ConversationService;
import com.logistimo.conversations.service.impl.ConversationServiceImpl;
import com.logistimo.dao.JDOUtils;
import com.logistimo.domains.utils.DomainsUtil;
import com.logistimo.entities.entity.IKiosk;
import com.logistimo.entities.service.EntitiesService;
import com.logistimo.entities.service.EntitiesServiceImpl;
import com.logistimo.events.EventConstants;
import com.logistimo.events.entity.IEvent;
import com.logistimo.events.models.CustomOptions;
import com.logistimo.events.processor.EventPublisher;
import com.logistimo.exception.InvalidDataException;
import com.logistimo.exception.InvalidServiceException;
import com.logistimo.exception.LogiException;
import com.logistimo.inventory.entity.IInvAllocation;
import com.logistimo.inventory.entity.IInvntry;
import com.logistimo.inventory.entity.IInvntryBatch;
import com.logistimo.inventory.entity.ITransaction;
import com.logistimo.inventory.service.InventoryManagementService;
import com.logistimo.inventory.service.impl.InventoryManagementServiceImpl;
import com.logistimo.logger.XLog;
import com.logistimo.materials.entity.IMaterial;
import com.logistimo.materials.service.MaterialCatalogService;
import com.logistimo.materials.service.MaterialUtils;
import com.logistimo.materials.service.impl.MaterialCatalogServiceImpl;
import com.logistimo.models.ResponseModel;
import com.logistimo.models.shipments.ShipmentItemBatchModel;
import com.logistimo.models.shipments.ShipmentItemModel;
import com.logistimo.models.shipments.ShipmentMaterialsModel;
import com.logistimo.models.shipments.ShipmentModel;
import com.logistimo.orders.entity.IDemandItem;
import com.logistimo.orders.entity.IOrder;
import com.logistimo.orders.service.IDemandService;
import com.logistimo.orders.service.OrderManagementService;
import com.logistimo.orders.service.impl.DemandService;
import com.logistimo.orders.service.impl.OrderManagementServiceImpl;
import com.logistimo.pagination.Results;
import com.logistimo.proto.JsonTagsZ;
import com.logistimo.services.ObjectNotFoundException;
import com.logistimo.services.Service;
import com.logistimo.services.ServiceException;
import com.logistimo.services.Services;
import com.logistimo.services.impl.PMF;
import com.logistimo.services.impl.ServiceImpl;
import com.logistimo.services.taskqueue.ITaskService;
import com.logistimo.shipments.ShipmentStatus;
import com.logistimo.shipments.entity.IShipment;
import com.logistimo.shipments.entity.IShipmentItem;
import com.logistimo.shipments.entity.IShipmentItemBatch;
import com.logistimo.shipments.entity.ShipmentItem;
import com.logistimo.shipments.service.IShipmentService;
import com.logistimo.users.entity.IUserAccount;
import com.logistimo.users.service.UsersService;
import com.logistimo.users.service.impl.UsersServiceImpl;
import com.logistimo.utils.BigUtil;
import com.logistimo.utils.LocalDateUtil;
import com.logistimo.utils.LockUtil;
import com.logistimo.utils.MsgUtil;
import com.logistimo.utils.StringUtil;

import org.apache.commons.lang.StringUtils;

import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.jdo.PersistenceManager;
import javax.jdo.Query;
import javax.jdo.Transaction;

/**
 * Created by Mohan Raja on 29/09/16
 */
public class ShipmentService extends ServiceImpl implements IShipmentService {

  private static final XLog xLogger = XLog.getLog(ShipmentService.class);
  private SimpleDateFormat sdf = new SimpleDateFormat(Constants.DATE_FORMAT);
  private ITaskService taskService = AppFactory.get().getTaskService();

  private Map<Long, IDemandItem> getDemandMetadata(Long orderId, PersistenceManager pm)
      throws ServiceException {
    Map<Long, IDemandItem> demand = new HashMap<>();
    IDemandService ds = Services.getService(DemandService.class);
    List<IDemandItem> demandItems = ds.getDemandItems(orderId, pm);
    for (IDemandItem di : demandItems) {
      demand.put(di.getMaterialId(), di);
    }
    return demand;
  }

  /**
   * Create shipment
   *
   * @param model {@code ShipmentModel}
   * @return -
   */
  @Override
  @SuppressWarnings("unchecked")
  public String createShipment(ShipmentModel model) throws ServiceException {
    LockUtil.LockStatus lockStatus = LockUtil.lock(Constants.TX_O + model.orderId);
    if (!LockUtil.isLocked(lockStatus)) {
      throw new InvalidServiceException(new ServiceException("O002", model.orderId));
    }
    PersistenceManager pm = PMF.get().getPersistenceManager();
    Transaction tx = pm.currentTransaction();
    try {
            /*if (!validate(model)) {
                throw new InvalidDataException("Invalid data. Shipment cannot be created.");
            }*/
      tx.begin();
      Date now = new Date();
      IShipment shipment = JDOUtils.createInstance(IShipment.class);
      shipment.setShipmentId(constructShipmentId(model.orderId));
      shipment.setOrderId(model.orderId);
      shipment.setDomainId(model.sdid);
      shipment.setStatus(model.status != null ? model.status : ShipmentStatus.OPEN);
      if (StringUtils.isNotEmpty(model.ead)) {
        shipment.setExpectedArrivalDate(sdf.parse(model.ead));
      }
      shipment.setNumberOfItems(model.items != null ? model.items.size() : 0);
      shipment.setKioskId(model.customerId);
      shipment.setServicingKiosk(model.vendorId);
      shipment.setTransporter(model.transporter);
      shipment.setTrackingId(model.trackingId);
      shipment.setPackageSize(model.ps);
      shipment.setReason(model.reason);
      shipment.setCancelledDiscrepancyReasons(model.cdrsn);
      shipment.setCreatedBy(model.userID);
      shipment.setCreatedOn(now);
      shipment.setLatitude(model.latitude);
      shipment.setLongitude(model.longitude);
      shipment.setGeoAccuracy(model.geoAccuracy);
      shipment.setGeoErrorCode(model.geoError);
      try {
        validateBeforeCreateShipment(model, pm);
      } catch (ServiceException e) {
        throw e;
      }
      DomainsUtil.addToDomain(shipment, model.sdid, null);
      DomainConfig dc = DomainConfig.getInstance(model.sdid);
      InventoryManagementService
          ims =
          Services.getService(InventoryManagementServiceImpl.class);
      for (ShipmentItemModel item : model.items) {
        if (dc.autoGI() && item.afo) {
          BigDecimal transferQuantity = item.q;
          if (item.isBa) {
            List<IInvAllocation> allocations = ims.getAllocationsByTypeId(model.vendorId, item.mId,
                IInvAllocation.Type.ORDER, String.valueOf(model.orderId));
            if (allocations != null) {
              item.bq = new ArrayList<>(allocations.size());
              Results results = ims.getBatches(item.mId, model.vendorId, null);
              if (results != null) {
                List<IInvntryBatch> allBatches = (List<IInvntryBatch>) results.getResults();
                boolean complete = false;
                for (IInvntryBatch allBatch : allBatches) {
                  for (IInvAllocation allocation : allocations) {
                    if (allBatch.getBatchId().equals(allocation.getBatchId())) {
                      ShipmentItemBatchModel m = new ShipmentItemBatchModel();
                      m.id = allocation.getBatchId();
                      if (BigUtil.lesserThanEquals(allocation.getQuantity(), transferQuantity)) {
                        m.q = allocation.getQuantity();
                      } else {
                        m.q = transferQuantity;
                      }
                      m.smst = allocation.getMaterialStatus();
                      item.bq.add(m);
                      transferQuantity = transferQuantity.subtract(m.q);
                      if (BigUtil.equalsZero(transferQuantity)) {
                        complete = true;
                        break;
                      }
                    }
                    if (complete) {
                      break;
                    }
                  }
                }
              }
            }
            transferQuantity = null;
          } else {
            List<IInvAllocation>
                allocations =
                ims.getAllocationsByTypeId(model.vendorId, item.mId, IInvAllocation.Type.ORDER,
                    String.valueOf(model.orderId));
            if (!allocations.isEmpty()) {
              if (BigUtil.greaterThan(item.q, allocations.get(0).getQuantity())) {
                transferQuantity = allocations.get(0).getQuantity();
              }
              item.smst = allocations.get(0).getMaterialStatus();
            }
          }
          ims.transferAllocation(model.vendorId, item.mId, IInvAllocation.Type.ORDER,
              String.valueOf(model.orderId),
              IInvAllocation.Type.SHIPMENT, shipment.getShipmentId(), transferQuantity, item.bq,
              model.userID, null, pm, item.smst, false);
        }
      }
      pm.makePersistent(shipment);
      List<IShipmentItem> items = new ArrayList<>(model.items.size());
      for (ShipmentItemModel item : model.items) {
        item.kid = model.customerId;
        item.uid = model.userID;
        item.sid = shipment.getShipmentId();
        item.sdid = model.sdid;
        IShipmentItem sItem = createShipmentItem(item);
        items.add(sItem);
      }
      pm.makePersistentAll(items);
      items = (List<IShipmentItem>) pm.detachCopyAll(items);
      List<IShipmentItemBatch> bItems = new ArrayList<>(1);
      for (int i = 0; i < model.items.size(); i++) {
        ShipmentItemModel item = model.items.get(i);
        if (item.bq != null) {
          List<IShipmentItemBatch> sbatch = new ArrayList<>(item.bq.size());
          for (ShipmentItemBatchModel quantityByBatch : item.bq) {
            quantityByBatch.uid = model.userID;
            quantityByBatch.mid = item.mId;
            quantityByBatch.kid = model.customerId;
            quantityByBatch.siId = items.get(i).getShipmentItemId();
            quantityByBatch.sdid = model.sdid;
            IShipmentItemBatch sbItem = createShipmentItemBatch(quantityByBatch);
            bItems.add(sbItem);
            sbatch.add(sbItem);
          }
          items.get(i).setShipmentItemBatch(sbatch);
        }
      }
      shipment.setShipmentItems(items);
      if (!bItems.isEmpty()) {
        pm.makePersistentAll(bItems);
      }
      Map<Long, IDemandItem> dItems = getDemandMetadata(model.orderId, pm);
      for (ShipmentItemModel shipmentItemModel : model.items) {
        IDemandItem demandItem = dItems.get(shipmentItemModel.mId);
        demandItem
            .setInShipmentQuantity(demandItem.getInShipmentQuantity().add(shipmentItemModel.q));
      }
      pm.makePersistentAll(dItems.values());
      // ShipmentStatus is sent as OPEN , since status changes are managed subsequently below.
      updateMessageAndHistory(shipment.getShipmentId(), null, model.userID, model.orderId,
          model.sdid, null, ShipmentStatus.OPEN, pm);
      // if both conversation and activity returns success, proceed to commit changes
      if (model.status != null && !model.status.equals(ShipmentStatus.OPEN)) {
        updateShipmentStatus(shipment.getShipmentId(), model.status, null, model.userID, pm, null,
            shipment);
      }
      OrderManagementService oms = Services.getService(OrderManagementServiceImpl.class);
      oms.updateOrderMetadata(model.orderId, model.userID, pm);
      generateEvent(model.sdid, IEvent.CREATED, shipment, null, null);

      tx.commit();
      return shipment.getShipmentId();
    } catch (ServiceException ie) {
      throw ie;
    } catch (Exception e) {
      xLogger.severe("Error while creating shipment", e);
      throw new ServiceException(backendMessages.getString("shipment.create.error"), e);
    } finally {
      if (tx.isActive()) {
        tx.rollback();
      }
      PMF.close(pm);
      if (LockUtil.shouldReleaseLock(lockStatus) && !LockUtil
          .release(Constants.TX_O + model.orderId)) {
        xLogger.warn("Unable to release lock for key {0}", Constants.TX_O + model.orderId);
      }
    }
  }

  private void updateMessageAndHistory(String shipmentId, String message, String userId,
      Long orderId, Long domainId
      , ShipmentStatus prevStatus, ShipmentStatus newStatus, PersistenceManager pm)
      throws ServiceException {
    updateMessageAndHistory(shipmentId, message, userId, orderId, domainId, prevStatus, newStatus,
        null, pm);
  }

  private void updateMessageAndHistory(String shipmentId, String message, String userId,
      Long orderId, Long domainId
      , ShipmentStatus prevStatus, ShipmentStatus newStatus, Date createDate, PersistenceManager pm)
      throws ServiceException {
    IMessage iMessage = null;
    if (message != null) {
      ConversationService
          cs =
          Services.getService(ConversationServiceImpl.class, this.getLocale());
      iMessage = cs
          .addMsgToConversation(ConversationServiceImpl.ObjectTypeShipment, shipmentId, message,
              userId,
              Collections.singleton("ORDER:" + orderId), domainId, createDate, pm);
      OrderManagementService
          oms =
          Services.getService(OrderManagementServiceImpl.class, getLocale());
      oms.generateOrderCommentEvent(domainId, IEvent.COMMENTED, JDOUtils.getImplClassName(IShipment.class), shipmentId, null,
          null);
    }
    updateHistory(shipmentId, userId, orderId, domainId, prevStatus, newStatus, createDate, pm,
        iMessage);
  }

  private void updateHistory(String shipmentId, String userId, Long orderId, Long domainId,
      ShipmentStatus prevStatus, ShipmentStatus newStatus, Date createDate,
      PersistenceManager pm, IMessage iMessage) throws ServiceException {
    ActivityService
        activityService =
        Services.getService(ActivityServiceImpl.class, this.getLocale());
    activityService.createActivity(IActivity.TYPE.SHIPMENT.name(), shipmentId, "STATUS",
        prevStatus != null ? prevStatus.toString() : null,
        newStatus.toString(), userId, domainId, iMessage != null ? iMessage.getMessageId() : null,
        "ORDER:" + orderId, createDate, pm);
  }

  private IShipmentItemBatch createShipmentItemBatch(ShipmentItemBatchModel batches)
      throws ServiceException {
    Date now = new Date();
    IShipmentItemBatch sbItem = JDOUtils.createInstance(IShipmentItemBatch.class);
    sbItem.setCreatedBy(batches.uid);
    sbItem.setCreatedOn(now);
    sbItem.setUpdatedBy(batches.uid);
    sbItem.setUpdatedOn(now);
    sbItem.setMaterialId(batches.mid);
    sbItem.setKioskId(batches.kid);
    sbItem.setBatchManufacturer(batches.bmfnm);
    sbItem.setShipmentItemId(batches.siId);

    sbItem.setQuantity(batches.q);
    sbItem.setBatchId(batches.id);
    sbItem.setShippedMaterialStatus(batches.smst);
    DomainsUtil.addToDomain(sbItem, batches.sdid, null);
    return sbItem;
  }

  private IShipmentItem createShipmentItem(ShipmentItemModel item) throws ServiceException {
    IShipmentItem sItem = JDOUtils.createInstance(IShipmentItem.class);
    sItem.setCreatedBy(item.uid);
    Date now = new Date();
    sItem.setCreatedOn(now);
    sItem.setUpdatedBy(item.uid);
    sItem.setUpdatedOn(now);
    sItem.setQuantity(item.q);
    sItem.setMaterialId(item.mId);
    sItem.setKioskId(item.kid);
    sItem.setShipmentId(item.sid);
    sItem.setShippedMaterialStatus(item.smst);
    DomainsUtil.addToDomain(sItem, item.sdid, null);
    return sItem;
  }

  private boolean validate(ShipmentModel model) throws ParseException {
    Date now = new Date();

    if ((StringUtils.isNotEmpty(model.ead) && sdf.parse(model.ead).before(now))
        || model.items == null) {
      return false;
    }
//        for (ShipmentItemModel item : model.items) {
//            if (item.mId == null || (item.q != null && item.bq != null) ||
//                    (item.q == null && item.bq == null)) {
//                return false;
//            }
//        }
    return true;
  }

  private String constructShipmentId(Long orderId) throws Exception {
    LockUtil.LockStatus lockStatus = LockUtil.lock(Constants.TX_O + orderId);
    if (!LockUtil.isLocked(lockStatus)) {
      throw new InvalidServiceException(new ServiceException("O002", orderId));
    }
    PersistenceManager pm = PMF.get().getPersistenceManager();
    Query q = null;
    try {
      q = pm.newQuery("javax.jdo.query.SQL", "SELECT count(1) FROM SHIPMENT WHERE ORDERID=?");
      int count = ((Long) ((List) q.executeWithArray(orderId)).iterator().next()).intValue();
      return orderId + CharacterConstants.HYPHEN + (count + 1);
    } finally {
      if (q != null) {
        q.closeAll();
      }
      pm.close();
      if (LockUtil.shouldReleaseLock(lockStatus) && !LockUtil.release(Constants.TX_O + orderId)) {
        xLogger.warn("Unable to release lock for key {0}", Constants.TX_O + orderId);
      }
    }
  }

  // Generate shipment events, if configured
  private void generateEvent(Long domainId, int eventId, IShipment s, String message,
      List<String> userIds) {
    try {
      Map<String, Object> params = null;

      if (eventId == IEvent.STATUS_CHANGE) {
        params = new HashMap<>(1);
        params.put(EventConstants.PARAM_STATUS, s.getStatus().toString());
      }
      // Custom options
      CustomOptions customOptions = new CustomOptions();
      if (StringUtils.isNotEmpty(message) || (userIds != null && !userIds.isEmpty())) {
        customOptions.message = message;
        if (userIds != null && !userIds.isEmpty()) {
          Map<Integer, List<String>> userIdsMap = new HashMap<Integer, List<String>>();
          userIdsMap.put(Integer.valueOf(EventSpec.NotifyOptions.IMMEDIATE), userIds);
          customOptions.userIds = userIdsMap;
        }
      }
      // Generate event, if needed
      EventPublisher.generate(domainId, eventId, params,
          JDOUtils.getImplClass(IShipment.class).getName(), s.getShipmentId(), customOptions);
    } catch (Exception e) {
      xLogger.severe("{0} when generating Shipment event {1} for shipment {2} in domain {3}: {4}",
              e.getClass().getName(), eventId, s.getShipmentId(), domainId, e);
    }
  }

  @Override
  public ResponseModel updateShipmentStatus(String shipmentId, ShipmentStatus status,
      String message,
      String userId,
      String reason, boolean updateOrderStatus,
      PersistenceManager pm) throws LogiException {
    return updateShipmentStatus(shipmentId, status, message, userId, pm, reason, null,
        updateOrderStatus, false);
  }

  @Override
  public ResponseModel updateShipmentStatus(String shipmentId, ShipmentStatus status,
      String message,
      String userId,
      String reason) throws LogiException {
    return updateShipmentStatus(shipmentId, status, message, userId, null, reason, null);
  }

  /**
   * Updtes the shipment's status.
   *
   * @return ResponseModel containing the status (true if success and false if failure) and a
   * message(usually a warning in case of partial success, empty otherwise)
   */
  private ResponseModel updateShipmentStatus(String shipmentId, ShipmentStatus status,
      String message,
      String userId,
      PersistenceManager pm, String reason, IShipment shipment)
      throws LogiException {
    return updateShipmentStatus(shipmentId, status, message, userId, pm, reason, shipment, true,
        false);
  }

  private ResponseModel updateShipmentStatus(String shipmentId, ShipmentStatus status,
      String message,
      String userId,
      PersistenceManager pm, String reason, IShipment shipment,
      boolean isOrderFulfil) throws LogiException {
    return updateShipmentStatus(shipmentId, status, message, userId, pm, reason, shipment, true,
        isOrderFulfil);
  }


  private ResponseModel updateShipmentStatus(String shipmentId, ShipmentStatus status,
      String message,
      String userId,
      PersistenceManager pm, String reason, IShipment shipment,
      boolean updateOrderStatus, boolean isOrderFulfil)
      throws LogiException {
    Long orderId = extractOrderId(shipmentId);
    LockUtil.LockStatus lockStatus = LockUtil.lock(Constants.TX_O + orderId);
    if (!LockUtil.isLocked(lockStatus)) {
      throw new InvalidServiceException(new ServiceException("O002", orderId));
    }
    boolean closePM = false;
    boolean localShipObject = shipment == null;
    ResponseModel responseModel = new ResponseModel();
    Transaction tx = null;
    if (pm == null) {
      pm = PMF.get().getPersistenceManager();
      tx = pm.currentTransaction();
      closePM = true;
    }
    try {
      if (closePM) {
        tx.begin();
      }
      if (shipment == null) {
        shipment = JDOUtils.getObjectById(IShipment.class, shipmentId, pm);
        if (shipment == null) {
          throw new IllegalArgumentException(
              backendMessages.getString("shipment.unavailable.db") + " " + backendMessages
                  .getString("shipment.id") + " : "
                  + shipmentId);
        }
        includeShipmentItems(shipment, pm);
      }
      ShipmentStatus prevStatus = shipment.getStatus();
      validateStatusTransition(prevStatus, status);
      responseModel = validateStatusChange(shipment, status.toString(),pm);
      if (ShipmentStatus.CANCELLED == status) {
        cancelShipment(shipmentId, message, userId, pm, reason);
        if (ShipmentStatus.SHIPPED.equals(prevStatus) ||
            ShipmentStatus.FULFILLED.equals(prevStatus)) {
          postInventoryTransaction(shipment, userId, pm, prevStatus);
        } else if (ShipmentStatus.OPEN.equals(prevStatus)) {
          DomainConfig dc = DomainConfig.getInstance(shipment.getDomainId());
          if (dc.autoGI()) {
            InventoryManagementService
                ims =
                Services.getService(InventoryManagementServiceImpl.class);
            ims.clearAllocation(null, null, IInvAllocation.Type.SHIPMENT, shipmentId, pm);
          }
        }
        if (updateOrderStatus) {
          updateOrderStatus(shipment.getOrderId(), status, userId, pm);
        }
        if (closePM) {
          tx.commit();
        }
        generateEvent(shipment.getDomainId(), IEvent.STATUS_CHANGE, shipment, null, null);
        responseModel.status = true;
        return responseModel;
      }

      shipment.setStatus(status);
      shipment.setUpdatedBy(userId);
      shipment.setUpdatedOn(new Date());
      if (status == ShipmentStatus.SHIPPED || (prevStatus != ShipmentStatus.SHIPPED &&
          status == ShipmentStatus.FULFILLED)) {

        //To check for batch enabled materials and vendor

        if(status==ShipmentStatus.SHIPPED ){

          checkShipmentRequest(shipment.getKioskId(),shipment.getServicingKiosk(),shipment.getShipmentItems());
        }

        DomainConfig dc = DomainConfig.getInstance(shipment.getDomainId());
        InventoryManagementService
            ims =
            Services.getService(InventoryManagementServiceImpl.class);
        if (dc.autoGI()) {
          ims.clearAllocation(null, null, IInvAllocation.Type.SHIPMENT, shipmentId, pm);
        }
        Map<Long, IDemandItem> demandItems = getDemandMetadata(orderId, pm);
        for (IShipmentItem shipmentItem : shipment.getShipmentItems()) {
          IDemandItem demandItem = demandItems.get(shipmentItem.getMaterialId());
          List<IShipmentItemBatch>
              batch =
              (List<IShipmentItemBatch>) shipmentItem.getShipmentItemBatch();
          if (batch != null && batch.size() > 0) {
            Results rs = ims.getBatches(shipmentItem.getMaterialId(),
                shipment.getServicingKiosk(), null);
            List<IInvntryBatch> results = (List<IInvntryBatch>) rs.getResults();
            for (IShipmentItemBatch ib : batch) {
              for (IInvntryBatch invntryBatch : results) {
                if (ib.getBatchId().equals(invntryBatch.getBatchId())) {
                  ib.setBatchExpiry(invntryBatch.getBatchExpiry());
                  ib.setBatchManufacturedDate(invntryBatch.getBatchManufacturedDate());
                  ib.setBatchManufacturer(invntryBatch.getBatchManufacturer());
                  if (localShipObject) {
                    pm.makePersistent(ib);
                  }
                  break;
                }
              }
            }
          }
          demandItem
              .setShippedQuantity(demandItem.getShippedQuantity().add(shipmentItem.getQuantity()));
        }
        pm.makePersistentAll(demandItems.values());
        postInventoryTransaction(shipment, userId, pm, prevStatus);
      }

      if (status == ShipmentStatus.FULFILLED && prevStatus != ShipmentStatus.FULFILLED) {
        //Only post fulfilled transaction here.
        postInventoryTransaction(shipment, userId, pm, ShipmentStatus.SHIPPED);
      }

      generateEvent(shipment.getDomainId(), IEvent.STATUS_CHANGE, shipment, null, null);
      OrderManagementService oms = Services.getService(OrderManagementServiceImpl.class);
      if (isOrderFulfil) {
        IMessage msg = null;
        if (message != null) {
          msg = oms.addMessageToOrder(shipment.getOrderId(), message, userId);
        }
        updateHistory(shipmentId, userId, shipment.getOrderId(),
            shipment.getDomainId(), prevStatus, shipment.getStatus(), null, pm, msg);
      } else {
        updateMessageAndHistory(shipmentId, message, userId, shipment.getOrderId(),
            shipment.getDomainId(), prevStatus, shipment.getStatus(), pm);
      }

      if (updateOrderStatus) {
        updateOrderStatus(shipment.getOrderId(), shipment.getStatus(), userId, pm);
      }
      oms.updateOrderMetadata(orderId, userId, pm);
      if (closePM) {
        tx.commit();
      }

    } catch (LogiException e) {
      throw e;
    } catch (Exception e) {
      xLogger.severe("Error while getting shipment details.", e);
      throw new ServiceException(e);
    } finally {
      if (closePM && tx.isActive()) {
        tx.rollback();
      }
      if (closePM) {
        PMF.close(pm);
      }
      if (LockUtil.shouldReleaseLock(lockStatus) && !LockUtil.release(Constants.TX_O + orderId)) {
        xLogger.warn("Unable to release lock for key {0}", Constants.TX_O + orderId);
      }
    }
    responseModel.status = true;
    return responseModel;
  }

  private Boolean validateStatusTransition(ShipmentStatus prevStatus, ShipmentStatus status) {
    if (ShipmentStatus.OPEN.equals(prevStatus) && (ShipmentStatus.SHIPPED.equals(status)
        || ShipmentStatus.CANCELLED.equals(status))) {
      return Boolean.TRUE;
    }

    if (ShipmentStatus.SHIPPED.equals(prevStatus) && (ShipmentStatus.FULFILLED.equals(status)
        || ShipmentStatus.CANCELLED.equals(status))) {
      return Boolean.TRUE;
    }

    if (ShipmentStatus.FULFILLED.equals(prevStatus) && ShipmentStatus.CANCELLED.equals(status)) {
      return Boolean.TRUE;
    }

    return Boolean.FALSE;
  }

  private void postInventoryTransaction(IShipment shipment, String userId, PersistenceManager pm,
                                        ShipmentStatus prevStatus) throws ServiceException {
    DomainConfig dc = DomainConfig.getInstance(shipment.getDomainId());
    if (!dc.autoGI()) {
      return;
    }
    List<ITransaction> transactionList = new ArrayList<>();
    List<IInvntry> inTransitList = new ArrayList<>(1);
    List<IInvntry> unFulfilledTransitList = null;
    InventoryManagementService
        ims =
        Services.getService(InventoryManagementServiceImpl.class);
    List<ITransaction> errors;
    EntitiesService as = Services.getService(EntitiesServiceImpl.class);
    boolean checkBatch = true;
    for (IShipmentItem shipmentItem : shipment.getShipmentItems()) {
      ITransaction t = JDOUtils.createInstance(ITransaction.class);
      IInvntry inv = ims.getInventory(shipment.getKioskId(), shipmentItem.getMaterialId(), pm);
      IInvntry
          vndInv =
          ims.getInventory(shipment.getServicingKiosk(), shipmentItem.getMaterialId(), pm);
      // If inventory is removed in the customer, do not try to post receipts for that material.
      if (inv == null && ShipmentStatus.FULFILLED.equals(shipment.getStatus())) {
        xLogger.warn(
            "Material with ID {0} does not exist in customer with ID {1} while posting transactions for shipment ID {2}",
            shipmentItem.getMaterialId(), shipment.getKioskId(), shipment.getShipmentId());
        continue;
      }
      if (vndInv == null && (ShipmentStatus.CANCELLED.equals(shipment.getStatus())
          || ShipmentStatus.SHIPPED.equals(shipment.getStatus()))) {
        xLogger.warn(
            "Material with ID {0} does not exist in vendor with ID {1} while posting transactions for shipment ID {2}",
            shipmentItem.getMaterialId(), shipment.getKioskId(), shipment.getShipmentId());
        continue;
      }
      if (ShipmentStatus.SHIPPED.equals(shipment.getStatus()) || (
          prevStatus != ShipmentStatus.SHIPPED &&
              ShipmentStatus.FULFILLED.equals(shipment.getStatus()))) {
        t.setType(ITransaction.TYPE_ISSUE);
        t.setQuantity(shipmentItem.getQuantity());
        t.setReason(shipment.getReason());
        t.setKioskId(shipment.getServicingKiosk());
        t.setLinkedKioskId(shipment.getKioskId());
        DomainsUtil.addToDomain(t, shipment.getLinkedDomainId(), null);
        if (inv != null) {
          inv.setInTransitStock(inv.getInTransitStock().add(shipmentItem.getQuantity()));
          inTransitList.add(inv);
        }
      } else if (ShipmentStatus.CANCELLED.equals(shipment.getStatus())) {
        t.setType(ITransaction.TYPE_RECEIPT);
        t.setQuantity(shipmentItem.getQuantity());
        t.setReason(shipment.getCancelledDiscrepancyReasons());
        t.setKioskId(shipment.getServicingKiosk());
        DomainsUtil.addToDomain(t, shipment.getLinkedDomainId(), null);
        if (ShipmentStatus.SHIPPED.equals(prevStatus) && inv != null) {
          BigDecimal inTransStock = inv.getInTransitStock().subtract(shipmentItem.getQuantity());
          inv.setInTransitStock(
              BigUtil.greaterThanEqualsZero(inTransStock) ? inTransStock : BigDecimal.ZERO);
          inTransitList.add(inv);
        }
      } else if (ShipmentStatus.FULFILLED.equals(shipment.getStatus())) {
        t.setType(ITransaction.TYPE_RECEIPT);
        t.setQuantity(shipmentItem.getFulfilledQuantity());
        t.setKioskId(shipmentItem.getKioskId());
        t.setLinkedKioskId(shipment.getServicingKiosk());
        DomainsUtil.addToDomain(t, shipment.getKioskDomainId(), null);
        t.setReason(shipmentItem.getFulfilledDiscrepancyReason());
        //Reduce quantity (instead of fulfilled quantity) , since in-transit is updated based on issue.
        BigDecimal inTransStock = inv.getInTransitStock().subtract(shipmentItem.getQuantity());
        inv.setInTransitStock(
            BigUtil.greaterThanEqualsZero(inTransStock) ? inTransStock : BigDecimal.ZERO);
        if(BigUtil.equalsZero(shipmentItem.getFulfilledQuantity())){
          if(unFulfilledTransitList == null){
            unFulfilledTransitList = new ArrayList<>(1);
          }
          unFulfilledTransitList.add(inv);
        }else {
          inTransitList.add(inv);
        }
        checkBatch = as.getKiosk(shipment.getKioskId(), false).isBatchMgmtEnabled();
      } else {
        throw new ServiceException(backendMessages.getString("inventory.post"));
      }
      t.setMaterialId(shipmentItem.getMaterialId());
      t.setSrc(SourceConstants.WEB);
      t.setTrackingId(shipment.getShipmentId());
      t.setTrackingObjectType(ITransaction.TYPE_SHIPMENT);
      t.setSourceUserId(userId);
      List<IShipmentItemBatch>
          batch =
          checkBatch ? (List<IShipmentItemBatch>) shipmentItem.getShipmentItemBatch() : null;
      if (batch != null && !batch.isEmpty()) {
        for (IShipmentItemBatch ib : batch) {
          if ((ShipmentStatus.FULFILLED.equals(shipment.getStatus()) && BigUtil
              .equalsZero(ib.getFulfilledQuantity())) || (
              !(ShipmentStatus.FULFILLED.equals(shipment.getStatus())) && BigUtil
                  .equalsZero(ib.getQuantity()))) {
            continue;
          }
          ITransaction batchTrans = t.clone();
          batchTrans.setBatchId(ib.getBatchId());
          batchTrans.setBatchExpiry(ib.getBatchExpiry());
          if (ib.getBatchManufacturedDate() != null) {
            batchTrans.setBatchManufacturedDate(ib.getBatchManufacturedDate());
          }
          batchTrans.setBatchManufacturer(ib.getBatchManufacturer());
          batchTrans.setSrc(SourceConstants.WEB);
          if (ShipmentStatus.FULFILLED.equals(shipment.getStatus())) {
            batchTrans.setQuantity(ib.getFulfilledQuantity());
            batchTrans.setReason(ib.getFulfilledDiscrepancyReason());
            batchTrans.setMaterialStatus(ib.getFulfilledMaterialStatus());
          } else {
            batchTrans.setQuantity(ib.getQuantity());
            batchTrans.setMaterialStatus(ib.getShippedMaterialStatus());
          }
          if (BigUtil.greaterThanZero(batchTrans.getQuantity())) {
            transactionList.add(batchTrans);
          }
        }
      } else {
        t.setMaterialStatus(ShipmentStatus.FULFILLED.equals(shipment.getStatus()) ?
            shipmentItem.getFulfilledMaterialStatus() : shipmentItem.getShippedMaterialStatus());
        if (BigUtil.greaterThanZero(t.getQuantity())) {
          transactionList.add(t);
        }
      }
    }
    if(unFulfilledTransitList != null && !unFulfilledTransitList.isEmpty()){
      pm.makePersistentAll(unFulfilledTransitList);
    }
    if (!transactionList.isEmpty()) {
      try {
        if (!ShipmentStatus.FULFILLED.equals(shipment.getStatus())) {
          inTransitList = (List<IInvntry>) pm.makePersistentAll(inTransitList);
          //Need to send inTransitlist only if order is fulfilled.
          inTransitList = null;
        }
        errors =
            ims.updateInventoryTransactions(shipment.getDomainId(), transactionList, inTransitList,
                true,
                false, pm);

        if (dc.getInventoryConfig().isCREnabled() &&
            !(ShipmentStatus.FULFILLED.equals(shipment.getStatus()))) {
          Map<String, String> params = new HashMap<>(1);
          params.put("orderId", String.valueOf(shipment.getOrderId()));
          //Added 40 sec delay to let update inventory during post transaction
          taskService.schedule(ITaskService.QUEUE_OPTIMZER,
              Constants.UPDATE_PREDICTION_TASK, params,
              null, ITaskService.METHOD_POST, System.currentTimeMillis() + 40000);
        }
      } catch (ServiceException e) {
        xLogger.warn("ServiceException when doing auto {0} for order {1}: {2}",
            shipment.getShipmentId(),
            shipment.getOrderId(), e);
        throw e;
      } catch (Exception e) {
        xLogger.warn("Error in posting transactions", e);
        throw new ServiceException(backendMessages.getString("order.post.exception"), e);
      }
      if (errors != null && errors.size() > 0) {
        StringBuilder errorMsg = new StringBuilder();
        for (ITransaction error : errors) {
          errorMsg.append("-").append(error.getMessage()).append(MsgUtil.newLine());
        }
        xLogger.warn("Inventory posting failed {0}", errorMsg.toString());
        throw new ServiceException("T002", errorMsg.toString());
      }
    }
  }

  /**
   * Determines the new Order status based on the current status of Shipments, It invoked Order
   * management service to update the same - If all Shipments are SHIPPED and the Demand Items have
   * no remaining quantity to be SHIPPED, then mark order as SHIPPED - If all Shipments are either
   * SHIPPED or FULFILLED and there is at least one SHIPPED item and Demand Items have no remaining
   * quantity to be SHIPPED, then marke Order as SHIPPED. - If all Shipments are FULFILLED and
   * Demand Items have no remaining quantity, then mark Order as FULFILLED. - If there is remaining
   * quantity on Demand Items and at least on Shipment is SHIPPED , then mark Order as BACKORDERED.
   * - If there is remaining quantity on Demand Items and all Shipments or OPEN or CANCELLED, then
   * Order status should have be in CONFIRMED/PENDING. If the Order status is not CONFIMED/PENDING
   * then change it to one of the previous available status between CONFIRMED and PENDING from
   * activity history of this order.
   *
   * @param orderId - Order Id of the order for which Status should be updated
   * @param status - Shipment status
   * @param userId - User Id who triggered this status change.
   * @param pm - Persistence Manager.
   */
  private void updateOrderStatus(Long orderId, ShipmentStatus status, String userId,
      PersistenceManager pm)
      throws Exception {
    boolean fulfilled = status == ShipmentStatus.FULFILLED;
    boolean shipped = status == ShipmentStatus.SHIPPED;
    boolean cancelled = status == ShipmentStatus.CANCELLED;

    if (fulfilled || shipped || cancelled) {
      List<IShipment> shipments = getShipmentsByOrderId(orderId, pm);
      Map<Long, IDemandItem> demandMap = getDemandMetadata(orderId, pm);
      boolean allItemsInShipments = true;
      for (IDemandItem demand : demandMap.values()) {
        if (BigUtil.notEquals(demand.getQuantity(), demand.getInShipmentQuantity())) {
          allItemsInShipments = false;
          break;
        }
      }
      String newOrderStatus = getOverallStatus(shipments, allItemsInShipments, orderId);
      if (newOrderStatus != null) {
        try {
          OrderManagementService oms = Services.getService(OrderManagementServiceImpl.class);
          oms.updateOrderStatus(orderId, newOrderStatus, userId, null, null, SourceConstants.WEB,
              pm,
              null);
        } catch (Exception e) {
          xLogger
              .warn("Error while updating order status from shipments for order {0}", orderId, e);
          throw e;
        }
      }
    }
  }

  public String getOverallStatus(List<IShipment> shipments, boolean allItemsInShipments,
      Long orderId) throws ServiceException {
    boolean hasShipped = false;
    boolean hasFulfilled = false;
    boolean allShipped = !shipments.isEmpty();
    boolean allFulfilled = !shipments.isEmpty();
    for (IShipment shipment : shipments) {
      switch (shipment.getStatus()) {
        case SHIPPED:
          hasShipped = true;
          allFulfilled = false;
          break;
        case FULFILLED:
          hasFulfilled = true;
          break;
        case CANCELLED:
          break;
        default:
          allShipped = false;
          allFulfilled = false;
      }
    }
    return getNewOrderStatus(allItemsInShipments, allFulfilled, allShipped, hasShipped,
        hasFulfilled, orderId);
  }

  /**
   * Calculates new status refer #updateOrderStatus
   *
   * @param allItemsInShipments - Indicates that demand items quantity is fully processed and there
   * is no Yet to create shipment quantity
   * @param fulfilled - All shipments in the order are marked as fulfilled.
   * @param shipped - All Shipments in the Order are shipped.
   * @param hasShipped - There is at least one shipment in Order which is Shipped.
   * @param hasFulfilled - There is at least one shipment in Order which is fulfilled.
   * @param orderId - Order Id.
   * @return new order status for the order.
   */
  public String getNewOrderStatus(boolean allItemsInShipments, boolean fulfilled,
      boolean shipped,
      boolean hasShipped, boolean hasFulfilled, Long orderId)
      throws ServiceException {
    String newOrderStatus;
    if (allItemsInShipments) {
      if (fulfilled) {
        newOrderStatus = IOrder.FULFILLED;
      } else if (shipped) {
        newOrderStatus = IOrder.COMPLETED;
      } else if (hasShipped || hasFulfilled) {
        newOrderStatus = IOrder.BACKORDERED;
      } else {
        // If current status not pending or confirmed then
        // check Activity and get previous status, else mark as PENDING.
        newOrderStatus = getPreviousStatus(orderId);
      }
    } else {
      if (hasShipped || hasFulfilled) {
        newOrderStatus = IOrder.BACKORDERED;
      } else {
        // If current status not pending or confirmed then
        // check Activity and get previous status, else mark as PENDING.
        newOrderStatus = getPreviousStatus(orderId);
      }
    }
    return newOrderStatus;
  }

  private String getPreviousStatus(Long orderId) throws ServiceException {
    ActivityService ats = Services.getService(ActivityServiceImpl.class);
    Results
        res =
        ats.getActivity(String.valueOf(orderId), IActivity.TYPE.ORDER.name(), null, null, null,
            null, null);
    List<ActivityModel> activityList = res.getResults();
    if (activityList != null && activityList.size() > 0) {
      for (ActivityModel activity : activityList) {
        if (IOrder.CONFIRMED.equals(activity.newValue) || IOrder.PENDING
            .equals(activity.newValue)) {
          return activity.newValue;
        }
      }
    }
    return IOrder.PENDING;
  }

  /**
   * Update the shipment quantity and allocations with message
   *
   * @param model {@code ShipmentMaterialsModel}
   * @return -
   */
  @Override
  public boolean updateShipment(ShipmentMaterialsModel model) throws LogiException {
    Long orderId = extractOrderId(model.sId);
    LockUtil.LockStatus lockStatus = LockUtil.lock(Constants.TX_O + orderId);
    if (!LockUtil.isLocked(lockStatus)) {
      throw new InvalidServiceException(new ServiceException("O002", orderId));
    }
    IShipment shipment = getShipmentById(model.sId);
    includeShipmentItems(shipment);
    if (shipment != null) {
      PersistenceManager pm = PMF.get().getPersistenceManager();
      Transaction tx = pm.currentTransaction();
      try {
        OrderManagementService oms = Services.getService(OrderManagementServiceImpl.class);
        IOrder order = oms.getOrder(orderId);
        if (StringUtils.isNotBlank(model.orderUpdatedAt)) {
          UsersService as = Services.getService(UsersServiceImpl.class, getLocale());
          IUserAccount userAccount = as.getUserAccount(order.getUpdatedBy());
          if (!model.orderUpdatedAt.equals(
              LocalDateUtil.formatCustom(order.getUpdatedOn(), Constants.DATETIME_FORMAT, null))) {
            throw new LogiException("O004", userAccount.getFullName(),
                LocalDateUtil.format(order.getUpdatedOn(), getLocale(), userAccount.getTimezone()));
          }
        }
        List<IShipmentItemBatch> newShipmentItemBatches = new ArrayList<>();
        List<IShipmentItemBatch> delShipmentItemBatches = new ArrayList<>();
        for (ShipmentItemModel item : model.items) {
          for (IShipmentItem shipmentItem : shipment.getShipmentItems()) {
            if (item.mId.equals(shipmentItem.getMaterialId())) {
              if (item.isBa) {
                item.aq = null;
                for (ShipmentItemBatchModel shipmentItemBatchModel : item.bq) {
                  boolean newBatch = true;
                  int ind = 0;
                  for (IShipmentItemBatch shipmentItemBatch : shipmentItem.getShipmentItemBatch()) {
                    if (shipmentItemBatch.getBatchId().equals(shipmentItemBatchModel.id)) {
                      if (shipmentItemBatchModel.q == null || BigUtil
                          .equalsZero(shipmentItemBatchModel.q)) {
                        delShipmentItemBatches.add(shipmentItem.getShipmentItemBatch().get(ind));
                        item.smst = null;
                      } else {
                        shipmentItemBatch.setQuantity(shipmentItemBatchModel.q);
                        shipmentItemBatch.setShippedMaterialStatus(shipmentItemBatchModel.smst);
                      }
                      newBatch = false;
                      break;
                    }
                    ind++;
                  }
                  if (newBatch && BigUtil.greaterThanZero(shipmentItemBatchModel.q)) {
                    shipmentItemBatchModel.uid = model.userId;
                    shipmentItemBatchModel.kid = shipmentItem.getKioskId();
                    shipmentItemBatchModel.mid = shipmentItem.getMaterialId();
                    shipmentItemBatchModel.siId = shipmentItem.getShipmentItemId();
                    shipmentItemBatchModel.sdid = shipmentItem.getDomainId();
                    IShipmentItemBatch b = createShipmentItemBatch(shipmentItemBatchModel);
                    newShipmentItemBatches.add(b);
                  }
                }
              } else {
                shipmentItem.setQuantity(item.q);
                if (item.aq == null) {
                  item.smst = null;
                }
                shipmentItem.setShippedMaterialStatus(item.smst);
              }
              break;
            }
          }
        }
        tx.begin();
        pm.makePersistentAll(newShipmentItemBatches);
        for (IShipmentItem shipmentItem : shipment.getShipmentItems()) {
          if (shipmentItem.getShipmentItemBatch() != null) {
            pm.makePersistentAll(shipmentItem.getShipmentItemBatch());
          }
        }
        if (delShipmentItemBatches.size() > 0) {
          pm.deletePersistentAll(delShipmentItemBatches);
        }
        pm.makePersistentAll(shipment.getShipmentItems());
        pm.makePersistentAll(newShipmentItemBatches);
        InventoryManagementService
            ims =
            Services.getService(InventoryManagementServiceImpl.class);

        for (ShipmentItemModel item : model.items) {
          //Remove all emptied batches or make allocation zero.
          if (item.isBa) {
            for (ShipmentItemBatchModel itemBatch : item.bq) {
              if (itemBatch.q == null || BigUtil.equalsZero(itemBatch.q)) {
                ims.clearBatchAllocation(model.kid, item.mId, IInvAllocation.Type.SHIPMENT,
                    model.sId,
                    itemBatch.id, pm);
              }

            }
          } else if (item.aq == null || BigUtil.equalsZero(item.aq)) {
            ims.clearAllocation(model.kid, item.mId, IInvAllocation.Type.SHIPMENT, model.sId, pm);
          }
          //allocate remaining from Order.
          String tag = IInvAllocation.Type.ORDER + CharacterConstants.COLON + orderId;
          if (item.aq != null && BigUtil.greaterThan(item.aq, item.q)) {
            throw new ServiceException(backendMessages.getString("allocated.qty.greater"));
          }
          ims.transferAllocation(model.kid, item.mId, IInvAllocation.Type.ORDER, orderId.toString(),
              IInvAllocation.Type.SHIPMENT, model.sId, item.aq, item.bq, model.userId, tag, pm,
              item.smst, true);
        }
        shipment.setUpdatedOn(new Date());
        shipment.setUpdatedBy(model.userId);
        pm.makePersistent(shipment);
        oms.updateOrderMetadata(orderId, model.userId, pm);
        generateEvent(shipment.getDomainId(), IEvent.MODIFIED, shipment, null, null);
        tx.commit();
      } catch (ObjectNotFoundException e) {
        xLogger.warn("Order not found - ", e);
      } catch (LogiException le) {
        xLogger.warn("Error while updating shipment", le);
        throw le;
      } finally {
        if (tx.isActive()) {
          tx.rollback();
        }
        pm.close();
        if (LockUtil.shouldReleaseLock(lockStatus) && !LockUtil.release(Constants.TX_O + orderId)) {
          xLogger.warn("Unable to release lock for key {0}", Constants.TX_O + orderId);
        }
      }
    }
    return true;
  }

  public IShipment updateShipmentData(String updType, String updValue, String orderUpdatedAt,
                                    String sId, String userId)
      throws ServiceException {
    Long orderId = extractOrderId(sId);
    LockUtil.LockStatus lockStatus = LockUtil.lock(Constants.TX_O + orderId);
    if (!LockUtil.isLocked(lockStatus)) {
      throw new InvalidServiceException(new ServiceException("O002", orderId));
    }
    IShipment shipment = getShipmentById(sId);
    if (shipment != null) {
      PersistenceManager pm = PMF.get().getPersistenceManager();
      Transaction tx = pm.currentTransaction();
      try {
        OrderManagementService
            oms =
            Services.getService(OrderManagementServiceImpl.class, getLocale());
        IOrder order = oms.getOrder(orderId);
        if (StringUtils.isNotBlank(orderUpdatedAt)) {
          UsersService as = Services.getService(UsersServiceImpl.class, getLocale());
          IUserAccount userAccount = as.getUserAccount(order.getUpdatedBy());
          if (!orderUpdatedAt.equals(
              LocalDateUtil.formatCustom(order.getUpdatedOn(), Constants.DATETIME_FORMAT, null))) {
            throw new LogiException("O004", userAccount.getFullName(),
                LocalDateUtil.format(order.getUpdatedOn(), getLocale(), userAccount.getTimezone()));
          }
        }
        if ("tpName".equals(updType)) {
          shipment.setTransporter(updValue);
        } else if ("tId".equals(updType)) {
          shipment.setTrackingId(updValue);
        } else if ("rsn".equals(updType)) {
          shipment.setReason(updValue);//todo: update reason for shipment pending.
        } else if ("date".equals(updType)) {
          if (StringUtils.isNotBlank(updValue)) {
            SimpleDateFormat sdf = new SimpleDateFormat(Constants.DATE_FORMAT);
            shipment.setExpectedArrivalDate(sdf.parse(updValue));
          } else {
            shipment.setExpectedArrivalDate(null);
          }
        } else if ("ps".equals(updType)) {
          shipment.setPackageSize(updValue);
        }
        shipment.setUpdatedBy(userId);
        shipment.setUpdatedOn(new Date());
        generateEvent(shipment.getDomainId(), IEvent.MODIFIED, shipment, null, null);

        tx.begin();
        pm.makePersistent(shipment);
        oms.updateOrderMetadata(orderId, userId, pm);
        tx.commit();
        return getShipmentById(sId);
      } catch (InvalidServiceException e) {
        xLogger.warn("Error while updating shipment", e);
        throw e;
      } catch (LogiException le) {
        throw new ServiceException(le.getMessage());
      } catch (Exception e) {
        xLogger.warn("Error while updating shipment", e);
        throw new ServiceException(e);
      } finally {
        if (tx.isActive()) {
          tx.rollback();
        }
        pm.close();
        if (LockUtil.shouldReleaseLock(lockStatus) && !LockUtil.release(Constants.TX_O + orderId)) {
          xLogger.warn("Unable to release lock for key {0}", Constants.TX_O + orderId);
        }
      }
    }

    throw new ServiceException("Shipment not found");
  }

  @Override
  public ResponseModel fulfillShipment(String shipmentId, String userId) throws ServiceException {
    IShipment shipment = getShipmentById(shipmentId);
    includeShipmentItems(shipment);
    ShipmentMaterialsModel sModel = new ShipmentMaterialsModel();
    sModel.sId = shipmentId;
    sModel.userId = userId;
    SimpleDateFormat sdf = new SimpleDateFormat(Constants.DATE_FORMAT);
    sModel.afd = sdf.format(new Date());
    sModel.items = new ArrayList<>();
    MaterialCatalogService
        mcs =
        Services.getService(MaterialCatalogServiceImpl.class, this.getLocale());
    EntitiesService as = Services.getService(EntitiesServiceImpl.class, this.getLocale());
    for (IShipmentItem sItem : shipment.getShipmentItems()) {
      ShipmentItemModel siModel = new ShipmentItemModel();
      siModel.mId = sItem.getMaterialId();
      siModel.fq = siModel.q = sItem.getQuantity();
      IMaterial material = mcs.getMaterial(sItem.getMaterialId());
      IKiosk kiosk = as.getKiosk(shipment.getServicingKiosk(), false);
      siModel.isBa = material.isBatchEnabled() && kiosk.isBatchMgmtEnabled();
      if (siModel.isBa) {
        siModel.bq = new ArrayList<>();

        if (ShipmentStatus.OPEN.equals(shipment.getStatus())) {
          //Pick shipment batches from allocations if any
          InventoryManagementService
              ims =
              Services.getService(InventoryManagementServiceImpl.class,
                  getLocale());
          List<IInvAllocation>
              allocations =
              ims.getAllocationsByTypeId(shipment.getServicingKiosk(),
                  siModel.mId, IInvAllocation.Type.SHIPMENT, shipmentId);
          if (allocations != null && !allocations.isEmpty()) {
            for (IInvAllocation alloc : allocations) {
              if (BigUtil.greaterThanZero(alloc.getQuantity())) {
                ShipmentItemBatchModel sbModel = new ShipmentItemBatchModel();
                sbModel.fq = sbModel.q = alloc.getQuantity();
                sbModel.id = alloc.getBatchId();
                siModel.bq.add(sbModel);
              }
            }
          }
        } else {
          for (IShipmentItemBatch shipmentItemBatch : sItem.getShipmentItemBatch()) {
            ShipmentItemBatchModel sbModel = new ShipmentItemBatchModel();
            sbModel.fq = sbModel.q = shipmentItemBatch.getQuantity();
            sbModel.id = shipmentItemBatch.getBatchId();
            siModel.bq.add(sbModel);
          }
        }
      }
      sModel.items.add(siModel);
    }

    return fulfillShipment(sModel, userId);
  }


  @Override
  public ResponseModel fulfillShipment(ShipmentMaterialsModel model, String userId) {
    Long orderId = extractOrderId(model.sId);
    LockUtil.LockStatus lockStatus = LockUtil.lock(Constants.TX_O + orderId);
    if (!LockUtil.isLocked(lockStatus)) {
      throw new InvalidServiceException(new ServiceException("O002", orderId));
    }
    PersistenceManager pm = null;
    Transaction tx = null;
    ResponseModel responseModel = new ResponseModel();
    try {
      OrderManagementService oms = Services.getService(OrderManagementServiceImpl.class);
      IOrder order = oms.getOrder(orderId);
      if (StringUtils.isNotBlank(model.orderUpdatedAt)) {
        if (!model.orderUpdatedAt.equals(
            LocalDateUtil.formatCustom(order.getUpdatedOn(), Constants.DATETIME_FORMAT, null))) {
          UsersService as = Services.getService(UsersServiceImpl.class, getLocale());
          IUserAccount userAccount = as.getUserAccount(order.getUpdatedBy());
          throw new LogiException("O004", userAccount.getFullName(),
              LocalDateUtil.format(order.getUpdatedOn(), getLocale(), userAccount.getTimezone()));
        }
      }
      pm = PMF.get().getPersistenceManager();
      tx = pm.currentTransaction();
      tx.begin();
      IShipment shipment = getShipmentById(model.sId, true, pm);
      Map<Long, List<IShipmentItemBatch>> newShipmentItemBatchesMap = new HashMap<>();
      if (shipment != null) {
        for (ShipmentItemModel item : model.items) {
          for (IShipmentItem shipmentItem : shipment.getShipmentItems()) {
            if (item.mId.equals(shipmentItem.getMaterialId())) {
              if (item.isBa) {
                if (item.bq == null || item.bq.isEmpty()) {
                  throw new IllegalArgumentException(
                      backendMessages.getString("shipment.fulfill.error") + " " + model.sId +
                          ", " + backendMessages.getString("materials.batch.empty") + " "
                          + item.mId);
                }
                BigDecimal totalFQ = BigDecimal.ZERO;
                BigDecimal totalDQ = BigDecimal.ZERO;
                for (ShipmentItemBatchModel shipmentItemBatchModel : item.bq) {
                  boolean newBatch = true;
                  for (IShipmentItemBatch shipmentItemBatch : shipmentItem.getShipmentItemBatch()) {
                    if (shipmentItemBatch.getBatchId().equals(shipmentItemBatchModel.id)) {
                      if (shipmentItemBatchModel.fq == null || BigUtil.lesserThanZero(
                          shipmentItemBatchModel.fq)) {
                        throw new ServiceException(
                            backendMessages.getString("shipment.batch.item") + " " +
                                shipmentItemBatchModel.id + " " + backendMessages
                                .getString("qty.lower") + " " +
                                shipmentItemBatchModel.fq + backendMessages.getString("valid.qty"));
                      }
                      updateShipmentItemBatch(shipmentItemBatch, shipmentItemBatchModel,
                          Boolean.FALSE);
                      //shipmentItemBatch.setDiscrepancyQuantity(shipmentItemBatch.getQuantity().subtract(shipmentItemBatchModel.q));
                      totalFQ = totalFQ.add(shipmentItemBatch.getFulfilledQuantity());
                      totalDQ = totalDQ.add(shipmentItemBatch.getDiscrepancyQuantity());
                      newBatch = false;
                      break;
                    }
                  }
                  if (newBatch && BigUtil.greaterThanZero(shipmentItemBatchModel.fq)) {
                    IShipmentItemBatch shipmentItemBatch =
                        getShipmentItemBatchFromShipmentItem(model.userId, shipmentItem,
                            shipmentItemBatchModel);
                    shipmentItemBatch.setBatchExpiry(LocalDateUtil
                        .parseCustom(shipmentItemBatchModel.e, Constants.DATE_FORMAT, null));
                    if(StringUtils.isNotEmpty(shipmentItemBatchModel.bmfdt)) {
                      shipmentItemBatch.setBatchManufacturedDate(LocalDateUtil
                          .parseCustom(shipmentItemBatchModel.bmfdt, Constants.DATE_FORMAT, null));
                    }
                    updateShipmentItemBatch(shipmentItemBatch, shipmentItemBatchModel,
                        Boolean.FALSE);
                    totalFQ = totalFQ.add(shipmentItemBatch.getFulfilledQuantity());
                    totalDQ = totalDQ.add(shipmentItemBatch.getDiscrepancyQuantity());

                    if (newShipmentItemBatchesMap.containsKey(shipmentItem.getMaterialId())) {
                      newShipmentItemBatchesMap.get(shipmentItem.getMaterialId())
                          .add(shipmentItemBatch);
                    } else {
                      List<IShipmentItemBatch> newShipmentItemBatches = new ArrayList<>(1);
                      newShipmentItemBatches.add(shipmentItemBatch);
                      newShipmentItemBatchesMap
                          .put(shipmentItem.getMaterialId(), newShipmentItemBatches);
                    }
                  }
                }
                shipmentItem.setFulfilledQuantity(totalFQ);
                shipmentItem.setDiscrepancyQuantity(totalDQ);
              } else {
                shipmentItem.setFulfilledQuantity(item.fq);
                shipmentItem.setDiscrepancyQuantity(shipmentItem.getQuantity().subtract(item.fq));
                shipmentItem.setFulfilledMaterialStatus(item.fmst);
                shipmentItem.setFulfilledDiscrepancyReason(item.frsn);
              }
              break;
            }
          }
        }

        for (IShipmentItem shipmentItem : shipment.getShipmentItems()) {
          List<IShipmentItemBatch> shipmentItemBatches = (List<IShipmentItemBatch>) shipmentItem
              .getShipmentItemBatch();
          if (newShipmentItemBatchesMap.containsKey(shipmentItem.getMaterialId())) {
            shipmentItemBatches.addAll(newShipmentItemBatchesMap.get(shipmentItem.getMaterialId()));
          }
        }

        for (IShipmentItem shipmentItem : shipment.getShipmentItems()) {
          List<IShipmentItemBatch> batches = (List<IShipmentItemBatch>) shipmentItem
              .getShipmentItemBatch();
          if (batches != null && batches.size() > 0) {
            pm.makePersistentAll(batches);
          }
        }
        pm.makePersistentAll(shipment.getShipmentItems());
        Map<Long, IDemandItem> items = getDemandMetadata(extractOrderId(model.sId), pm);
        for (IShipmentItem sItem : shipment.getShipmentItems()) {
          IDemandItem item = items.get(sItem.getMaterialId());
          item.setFulfilledQuantity(item.getFulfilledQuantity().add(sItem.getFulfilledQuantity()));
          item.setDiscrepancyQuantity(
              item.getDiscrepancyQuantity().add(sItem.getDiscrepancyQuantity()));
        }
        pm.makePersistentAll(items.values());
        SimpleDateFormat sdf = new SimpleDateFormat(Constants.DATE_FORMAT);
        shipment.setActualFulfilmentDate(sdf.parse(model.afd));
        pm.makePersistent(shipment);
        responseModel = updateShipmentStatus(model.sId, ShipmentStatus.FULFILLED, model.msg, model.userId,
            pm, null, shipment, model.isOrderFulfil);
        oms.updateOrderMetadata(orderId, userId, pm);
        tx.commit();
      }
    } catch (InvalidServiceException | LogiException e) {
      xLogger.warn("Error while updating shipment", e);
      throw new InvalidServiceException(e.getMessage());
    } catch (Exception e1) {
      xLogger.warn("Error while updating shipment", e1);
      throw new InvalidServiceException(e1);
    } finally {
      if (tx != null && tx.isActive()) {
        tx.rollback();
      }
      if (pm != null) {
        pm.close();
      }
      if (LockUtil.shouldReleaseLock(lockStatus) && !LockUtil.release(Constants.TX_O + orderId)) {
        xLogger.warn("Unable to release lock for key {0}", Constants.TX_O + orderId);
      }
    }

    return responseModel;
  }


  private IShipmentItemBatch getShipmentItemBatchFromShipmentItem(String userId,
      IShipmentItem shipmentItem, ShipmentItemBatchModel shipmentItemBatchModel)
      throws ServiceException {
    shipmentItemBatchModel.uid = userId;
    shipmentItemBatchModel.kid = shipmentItem.getKioskId();
    shipmentItemBatchModel.mid = shipmentItem.getMaterialId();
    shipmentItemBatchModel.siId = shipmentItem.getShipmentItemId();
    shipmentItemBatchModel.sdid = shipmentItem.getDomainId();
    return createShipmentItemBatch(shipmentItemBatchModel);
  }

  private void updateShipmentItemBatch(IShipmentItemBatch shipmentItemBatch,
      ShipmentItemBatchModel shipmentItemBatchModel, Boolean setBatch) throws ParseException {

    shipmentItemBatch.setFulfilledMaterialStatus(shipmentItemBatchModel.fmst);
    shipmentItemBatch.setFulfilledQuantity(shipmentItemBatchModel.fq);
    shipmentItemBatch.setDiscrepancyQuantity(
        shipmentItemBatch.getQuantity().subtract(shipmentItemBatchModel.fq));
    shipmentItemBatch.setFulfilledDiscrepancyReason(shipmentItemBatchModel.frsn);
    if (setBatch) {
      shipmentItemBatch.setBatchExpiry(
          LocalDateUtil.parseCustom(shipmentItemBatchModel.e, Constants.DATE_FORMAT, null));
      shipmentItemBatch.setBatchId(shipmentItemBatchModel.id);
      shipmentItemBatch.setBatchManufacturer(shipmentItemBatchModel.bmfnm);
      shipmentItemBatch.setBatchManufacturedDate(
          LocalDateUtil.parseCustom(shipmentItemBatchModel.bmfdt, Constants.DATE_FORMAT, null));
    }
  }

  @Override
  @SuppressWarnings("unchecked")
  public Results getShipments(String userId, Long domainId, Long custId, Long vendId, Date from,
      Date to,
      Date etaFrom, Date etaTo, String transporter, String trackingId,
      ShipmentStatus status, int size, int offset) {
    if (domainId == null) {
      xLogger.warn("Domain id is required while getting the shipments");
      return null;
    }

    PersistenceManager pm = PMF.get().getPersistenceManager();
    Query query = null;
    Query cntQuery = null;
    try {
      List<String> params = new ArrayList<>(1);
      StringBuilder queryStr = new StringBuilder();
      queryStr.append(
          "SELECT * FROM SHIPMENT S, SHIPMENT_DOMAINS SD WHERE SD.ID_OID = S.ID AND SD.DOMAIN_ID = ?");
      params.add(String.valueOf(domainId));
      if (custId != null) {
        queryStr.append(" AND S.KID=?");
        params.add(String.valueOf(custId));

        UsersService as = Services.getService(UsersServiceImpl.class);
        IUserAccount acc = as.getUserAccount(userId);
        if (SecurityUtil.compareRoles(acc.getRole(), SecurityConstants.ROLE_DOMAINOWNER) < 0) {
          queryStr.append(
              " AND ( S.STATUS IN ('sp','fl') OR EXISTS(SELECT 1 FROM USERTOKIOSK WHERE USERID = ? AND KIOSKID = S.SKID)"
                  +
                  " OR EXISTS (SELECT 1 FROM KIOSKLINK KL, KIOSK K WHERE KL.KIOSKID IN (SELECT UK.KIOSKID FROM USERTOKIOSK UK "
                  +
                  "WHERE USERID = ?) AND ((KL.LINKTYPE = 'c' AND K.CPERM > 0) " +
                  " OR (KL.LINKTYPE = 'v' AND K.VPERM > 0))" +
                  " AND K.KIOSKID = KL.KIOSKID AND S.SKID = KL.LINKEDKIOSKID LIMIT 1))");
          params.add(userId);
          params.add(userId);
        }
      }
      if (vendId != null) {
        queryStr.append(" AND S.SKID=?");
        params.add(String.valueOf(vendId));
      }
      if (from != null) {
        queryStr.append(" AND S.CON >=?");
        SimpleDateFormat sdf = new SimpleDateFormat(Constants.DATE_FORMAT_CSV);
        params.add(sdf.format(from));
      }
      if (to != null) {
        queryStr.append(" AND S.CON <?");
        SimpleDateFormat sdf = new SimpleDateFormat(Constants.DATE_FORMAT_CSV);
        params.add(sdf.format(to));
      }
      if (etaFrom != null) {
        queryStr.append(" AND S.AFD >=?");
        SimpleDateFormat sdf = new SimpleDateFormat(Constants.DATE_FORMAT_CSV);
        params.add(sdf.format(etaFrom));
      }
      if (etaTo != null) {
        queryStr.append(" AND S.AFD <?");
        SimpleDateFormat sdf = new SimpleDateFormat(Constants.DATE_FORMAT_CSV);
        params.add(sdf.format(etaTo));
      }
      if (StringUtils.isNotEmpty(transporter)) {
        queryStr.append(" AND S.TRANSPORTER=?");
        params.add(transporter);
      }
      if (StringUtils.isNotEmpty(trackingId)) {
        queryStr.append(" AND S.TRACKINGID=?");
        params.add(trackingId);
      }
      if (status != null) {
        queryStr.append(" AND S.STATUS=?");
        params.add(status.toString());
      }
      queryStr.append(" ORDER BY S.CON DESC");
      String limitStr = " LIMIT " + offset + CharacterConstants.COMMA + size;
      queryStr.append(limitStr);
      query = pm.newQuery("javax.jdo.query.SQL", queryStr.toString());
      query.setClass(JDOUtils.getImplClass(IShipment.class));
      List list = (List) query.executeWithArray(params.toArray());
      List<IShipment> shipments = new ArrayList<>(list.size());
      for (Object o : list) {
        shipments.add((IShipment) o);
      }
      String cntQueryStr = queryStr.toString().replace("*", QueryConstants.ROW_COUNT);
      cntQueryStr = cntQueryStr.replace(limitStr, CharacterConstants.EMPTY);
      cntQuery = pm.newQuery("javax.jdo.query.SQL", cntQueryStr);
      int
          count =
          ((Long) ((List) cntQuery.executeWithArray(params.toArray())).iterator().next())
              .intValue();
      return new Results(shipments, null, count, offset);
    } catch (Exception e) {
      xLogger.severe("Error while getting the shipments", e);
    } finally {
      if (query != null) {
        query.closeAll();
      }
      if (cntQuery != null) {
        cntQuery.closeAll();
      }
      pm.close();
    }
    return null;
  }

//    /**
//     * Cancel the shipment given shipment id with message
//     *
//     * @param shipmentId Shipment Id
//     * @param message    Message to post against this cancellation
//     * @param userId     Id of user who made this change
//     * @return -
//     */
//    @Override
//    public boolean cancelShipment(String shipmentId, String message, String userId) {
//        return cancelShipment(shipmentId, message, userId, null);
//    }

  @Override
  public IShipment getShipment(String shipId) {
    PersistenceManager pm = PMF.get().getPersistenceManager();
    try {
      return getShipmentById(shipId, true, pm);
    } finally {
      pm.close();
    }
  }

  private boolean cancelShipment(String shipmentId, String message, String userId,
                                 PersistenceManager pm, String reason) throws ServiceException {
    Long orderId = extractOrderId(shipmentId);
    LockUtil.LockStatus lockStatus = LockUtil.lock(Constants.TX_O + orderId);
    if (!LockUtil.isLocked(lockStatus)) {
      throw new InvalidServiceException(new ServiceException("O002", orderId));
    }
    boolean closePM = false;
    Transaction tx = null;
    if (pm == null) {
      pm = PMF.get().getPersistenceManager();
      tx = pm.currentTransaction();
      closePM = true;
    }
    try {
      IShipment shipment = getShipmentById(shipmentId);
      ShipmentStatus prevStatus = shipment.getStatus();
      shipment.setStatus(ShipmentStatus.CANCELLED);
      shipment.setCancelledDiscrepancyReasons(reason);
      shipment.setUpdatedBy(userId);
      shipment.setUpdatedOn(new Date());
      includeShipmentItems(shipment, pm);
      boolean
          decrementShipped =
          prevStatus == ShipmentStatus.SHIPPED || prevStatus == ShipmentStatus.FULFILLED;
      Map<Long, IDemandItem> demandItems = getDemandMetadata(orderId, pm);
      for (IShipmentItem shipmentItem : shipment.getShipmentItems()) {
        if (prevStatus == ShipmentStatus.OPEN) {
          InventoryManagementService
              ims =
              Services.getService(InventoryManagementServiceImpl.class);
          List<IInvAllocation>
              allocations =
              ims.getAllocationsByTypeId(shipment.getServicingKiosk(), shipmentItem.getMaterialId(),
                  IInvAllocation.Type.SHIPMENT, shipmentId);
          List<ShipmentItemBatchModel> bq = new ArrayList<>(1);
          BigDecimal q = BigDecimal.ZERO;
          for (IInvAllocation allocation : allocations) {
            if (allocation.getBatchId() != null) {
              ShipmentItemBatchModel m = new ShipmentItemBatchModel();
              m.id = allocation.getBatchId();
              m.q = allocation.getQuantity();
              bq.add(m);
            } else {
              q = allocation.getQuantity();
            }
          }
          if (BigUtil.greaterThanZero(q)) {
            ims.transferAllocation(shipment.getServicingKiosk(), shipmentItem.getMaterialId(),
                IInvAllocation.Type.SHIPMENT, shipmentId,
                IInvAllocation.Type.ORDER, String.valueOf(orderId), q, null, userId, null, pm,
                null, false);
          }
          if (bq.size() > 0) {
            ims.transferAllocation(shipment.getServicingKiosk(), shipmentItem.getMaterialId(),
                IInvAllocation.Type.SHIPMENT, shipmentId,
                IInvAllocation.Type.ORDER, String.valueOf(orderId), null, bq, userId, null, pm,
                null, false);
          }
        }
        IDemandItem demandItem = demandItems.get(shipmentItem.getMaterialId());
        if (decrementShipped) {
          demandItem.setShippedQuantity(
              demandItem.getShippedQuantity().subtract(shipmentItem.getQuantity()));
        }
        demandItem.setInShipmentQuantity(
            demandItem.getInShipmentQuantity().subtract(shipmentItem.getQuantity()));
        //TODO reset fulfilled quantities.
      }
      if (closePM) {
        tx.begin();
      }
      pm.makePersistentAll(demandItems.values());
      updateMessageAndHistory(shipmentId, message, userId, orderId, shipment.getDomainId(),
          prevStatus,
          shipment.getStatus(), pm);
      pm.makePersistent(shipment);
      OrderManagementService oms = Services.getService(OrderManagementServiceImpl.class);
      oms.updateOrderMetadata(orderId, userId, pm);
      if (closePM) {
        tx.commit();
      }
    } catch (Exception e) {
      xLogger.severe("Error while cancelling the shipment {0}", shipmentId, e);
      throw new ServiceException(
          backendMessages.getString("shipment.cancel.error") + " " + shipmentId, e);
    } finally {
      if (closePM && tx.isActive()) {
        tx.rollback();
      }
      if (closePM) {
        pm.close();
      }
      if (LockUtil.shouldReleaseLock(lockStatus) && !LockUtil.release(Constants.TX_O + orderId)) {
        xLogger.warn("Unable to release lock for key {0}", Constants.TX_O + orderId);
      }
    }
    return true;
  }

  private long extractOrderId(String shipmentId) {
    return Long.parseLong(shipmentId.substring(0, shipmentId.indexOf(CharacterConstants.HYPHEN)));
  }

  @Override
  public List<IShipment> getShipmentsByOrderId(Long orderId) {
    PersistenceManager pm = null;
    try {
      pm = PMF.get().getPersistenceManager();
      return getShipmentsByOrderId(orderId, pm);
    } finally {
      if (pm != null) {
        pm.close();
      }
    }
  }

  /**
   * Get all shipment for a specific order
   *
   * @param orderId Order Id
   * @return -
   */
  @Override
  @SuppressWarnings("unchecked")
  public List<IShipment> getShipmentsByOrderId(Long orderId, PersistenceManager pm) {
    Query query = null;
    try {
      query = pm.newQuery("javax.jdo.query.SQL", "SELECT * FROM SHIPMENT WHERE ORDERID = ?");
      query.setClass(JDOUtils.getImplClass(IShipment.class));
      List list = (List) query.executeWithArray(orderId);
      List<IShipment> shipments = new ArrayList<>(list.size());
      for (Object shipment : list) {
        shipments.add((IShipment) shipment);
      }
      return shipments;
    } catch (Exception e) {
      xLogger.severe("Error while fetching shipments by order id: {0}", orderId, e);
    } finally {
      if (query != null) {
        query.closeAll();
      }
    }
    return null;
  }

  private IShipment getShipmentById(String shipmentId) {
    PersistenceManager pm = PMF.get().getPersistenceManager();
    try {
      IShipment shipment = getShipmentById(shipmentId, false, pm);
      return pm.detachCopy(shipment);
    } finally {
      pm.close();
    }
  }

  private IShipment getShipmentById(String shipmentId, boolean includeShipmentItems,
      PersistenceManager pm) {
    IShipment shipment = JDOUtils.getObjectById(IShipment.class, shipmentId, pm);
    if (includeShipmentItems) {
      includeShipmentItems(shipment, pm);
    }
    return shipment;
  }

  @Override
  public void includeShipmentItems(IShipment shipment) {
    PersistenceManager pm = null;
    try {
      pm = PMF.get().getPersistenceManager();
      includeShipmentItems(shipment, pm);
    } finally {
      if (pm != null) {
        pm.close();
      }
    }
  }

  @SuppressWarnings("unchecked")
  private void includeShipmentItems(IShipment shipment, PersistenceManager pm) {
    if (shipment == null) {
      return;
    }
    Query src = null;
    try {
      src = pm.newQuery("javax.jdo.query.SQL", "SELECT * FROM SHIPMENTITEM WHERE sid = ?");
      src.setClass(JDOUtils.getImplClass(IShipmentItem.class));
      shipment
          .setShipmentItems((List<IShipmentItem>) src.executeWithArray(shipment.getShipmentId()));
      shipment
          .setShipmentItems((List<IShipmentItem>) pm.detachCopyAll(shipment.getShipmentItems()));
      src = pm.newQuery("javax.jdo.query.SQL", "SELECT * FROM SHIPMENTITEMBATCH WHERE siId = ?");
      src.setClass(JDOUtils.getImplClass(IShipmentItemBatch.class));
      for (IShipmentItem iShipmentItem : shipment.getShipmentItems()) {
        List<IShipmentItemBatch>
            sb =
            (List<IShipmentItemBatch>) src.executeWithArray(iShipmentItem.getShipmentItemId());
        iShipmentItem.setShipmentItemBatch((List<IShipmentItemBatch>) pm.detachCopyAll(sb));
      }
    } catch (Exception e) {
      xLogger
          .severe("Error while fetching shipment items for shipment: {0}", shipment.getShipmentId(),
              e);
    } finally {
      if (src != null) {
        src.closeAll();
      }
    }
  }

  @Override
  public List<String> getTransporterSuggestions(Long domainId, String text) {
    List<String> filterIds = new ArrayList<>();
    String domainFilterQuery = "SELECT ID_OID FROM SHIPMENT_DOMAINS WHERE DOMAIN_ID = " + domainId;
    PersistenceManager pm = PMF.get().getPersistenceManager();
    Query
        query =
        pm.newQuery("javax.jdo.query.SQL",
            "SELECT DISTINCT TRANSPORTER FROM `SHIPMENT` WHERE ID IN (" +
                domainFilterQuery + ") AND TRANSPORTER LIKE '" + text + "%' " + "LIMIT 0,8");
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

  @Override
  public void init(Services services) throws ServiceException {
  }

  @Override
  public void destroy() throws ServiceException {
  }

  @Override
  public Class<? extends Service> getInterface() {
    return this.getClass();
  }

  @Override
  public IMessage addMessage(String shipmentId, String message, String userId)
      throws ServiceException {
    ConversationService
        cs =
        Services.getService(ConversationServiceImpl.class, this.getLocale());
    IShipment shipment = getShipmentById(shipmentId);
    IMessage iMessage = cs.addMsgToConversation("SHIPMENT", shipmentId, message, userId,
        Collections.singleton("SHIPMENT:" + shipmentId),
        shipment.getDomainId(), null);
    OrderManagementService
        oms =
        Services.getService(OrderManagementServiceImpl.class, getLocale());
    oms.generateOrderCommentEvent(shipment.getDomainId(), IEvent.COMMENTED, JDOUtils.getImplClassName(IShipment.class),
        shipmentId, null,
        null);
    return iMessage;
  }

  /**
   * This method is used to create shipment for migratory orders.
   * This should only be called from migration context.
   *
   * @return shipmentId
   */
  @Override
  public String createShipmentForMigratoryOrder(ShipmentModel model) throws ServiceException {

    PersistenceManager pm = PMF.get().getPersistenceManager();
    Transaction tx = pm.currentTransaction();
    try {
      if (!validate(model)) {
        throw new InvalidDataException("Invalid data. Shipment cannot be created.");
      }
      tx.begin();
      Date now = new Date();
      IShipment shipment = JDOUtils.createInstance(IShipment.class);
      shipment.setShipmentId(constructShipmentId(model.orderId));
      shipment.setOrderId(model.orderId);
      shipment.setDomainId(model.sdid);
      shipment.setStatus(ShipmentStatus.OPEN);
      if (StringUtils.isNotEmpty(model.ead)) {
        shipment.setExpectedArrivalDate(sdf.parse(model.ead));
      }
      shipment.setNumberOfItems(model.items != null ? model.items.size() : 0);
      shipment.setKioskId(model.customerId);
      shipment.setServicingKiosk(model.vendorId);
      shipment.setTransporter(model.transporter);
      shipment.setTrackingId(model.trackingId);
      shipment.setReason(model.reason);
      shipment.setCancelledDiscrepancyReasons(model.cdrsn);
      shipment.setCreatedBy(model.userID);
      SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
      shipment.setCreatedOn(model.cOn != null ? sdf.parse(model.cOn) : now);
      shipment.setLatitude(model.latitude);
      shipment.setLongitude(model.longitude);
      shipment.setGeoAccuracy(model.geoAccuracy);
      shipment.setGeoErrorCode(model.geoError);
      DomainsUtil.addToDomain(shipment, model.sdid, null);
      pm.makePersistent(shipment);
      List<IShipmentItem> items = new ArrayList<>(model.items.size());
      for (ShipmentItemModel item : model.items) {
        if (BigUtil.greaterThanZero(item.q)) {
          item.kid = model.customerId;
          item.uid = model.userID;
          item.sid = shipment.getShipmentId();
          item.sdid = model.sdid;
          IShipmentItem sItem = createShipmentItem(item);
          sItem.setFulfilledQuantity(item.fq);
          items.add(sItem);
        }
      }
      pm.makePersistentAll(items);
      items = (List<IShipmentItem>) pm.detachCopyAll(items);
      List<IShipmentItemBatch> bItems = new ArrayList<>(1);
      for (int i = 0; i < model.items.size(); i++) {
        ShipmentItemModel item = model.items.get(i);
        if (item.bq != null) {
          for (ShipmentItemBatchModel quantityByBatch : item.bq) {
            quantityByBatch.uid = model.userID;
            quantityByBatch.mid = item.mId;
            quantityByBatch.kid = model.customerId;
            quantityByBatch.siId = items.get(i).getShipmentItemId();
            quantityByBatch.sdid = model.sdid;
            IShipmentItemBatch sbItem = createShipmentItemBatch(quantityByBatch);
            sbItem.setFulfilledQuantity(quantityByBatch.fq);
            bItems.add(sbItem);
          }
        }
      }
      if (!bItems.isEmpty()) {
        pm.makePersistentAll(bItems);
      }
      IDemandService ds = Services.getService(DemandService.class);
      List<IDemandItem> dItems = ds.getDemandItems(model.orderId, pm);
      for (IDemandItem item : dItems) {
        for (ShipmentItemModel shipmentItemModel : model.items) {
          if (item.getMaterialId().equals(shipmentItemModel.mId)) {
            item.setInShipmentQuantity(item.getInShipmentQuantity().add(shipmentItemModel.q));
            break;
          }
        }
      }
      pm.makePersistentAll(dItems);
      updateMessageAndHistory(shipment.getShipmentId(), null, model.userID, model.orderId,
          model.sdid, null, shipment.getStatus(), model.cOn != null ? sdf.parse(model.cOn) : null,
          pm);
      // if both conversation and activity returns success, proceed to commit changes
      if (model.status != null && !model.status.equals(ShipmentStatus.OPEN)) {
        Date date;
        if (model.cOn != null) {
          date = sdf.parse(model.cOn);
          Calendar cal = Calendar.getInstance();
          cal.setTime(date);
          cal.add(Calendar.SECOND, 1);
          date = cal.getTime();
        } else {
          date = null;
        }
        updateShipmentStatusForMigratoryOrder(shipment.getShipmentId(), model.status, null,
            model.userID, pm, null, date);
      }
      tx.commit();
      return shipment.getShipmentId();
    } catch (Exception e) {
      xLogger.severe("Error while creating shipment", e);
      throw new ServiceException("Error occurred while creating shipment", e);
    } finally {
      if (tx.isActive()) {
        tx.rollback();
      }
      pm.close();
    }
  }

  public BigDecimal getAllocatedQuantityForShipmentItem(String sId, Long kId, Long mId) {
    try {
      InventoryManagementService
          ims =
          Services.getService(InventoryManagementServiceImpl.class);
      List<IInvAllocation>
          iAllocs =
          ims.getAllocationsByTypeId(kId, mId, IInvAllocation.Type.SHIPMENT, sId);
      if (iAllocs != null && !iAllocs.isEmpty()) {
        BigDecimal alq = BigDecimal.ZERO;
        for (IInvAllocation iAlloc : iAllocs) {
          alq = alq.add(iAlloc.getQuantity());
        }
        return alq;
      }
    } catch (Exception e) {
      xLogger.warn("Exception while getting inventory allocation for the shipment {0}", sId, e);
    }
    return null;
  }

  private boolean updateShipmentStatusForMigratoryOrder(String shipmentId, ShipmentStatus status,
      String message, String userId,
      PersistenceManager pm, String reason,
      Date shpDate) {
    boolean closePM = false;
    Transaction tx = null;
    if (pm == null) {
      pm = PMF.get().getPersistenceManager();
      tx = pm.currentTransaction();
      closePM = true;
    }
    try {
      if (closePM) {
        tx.begin();
      }
      IShipment shipment = JDOUtils.getObjectById(IShipment.class, shipmentId, pm);
      if (shipment == null) {
        throw new Exception(
            "Shipment is not available in db to update status. Shipment ID:" + shipmentId);
      }
      Date uon = null != shpDate ? shpDate : new Date();
      includeShipmentItems(shipment, pm);
      ShipmentStatus prevStatus = shipment.getStatus();
      shipment.setStatus(status);
      shipment.setUpdatedBy(userId);
      shipment.setUpdatedOn(shpDate);
      if (status == ShipmentStatus.SHIPPED || (prevStatus != ShipmentStatus.SHIPPED
          && status == ShipmentStatus.FULFILLED)) {

        IDemandService ds = Services.getService(DemandService.class);
        InventoryManagementService
            ims =
            Services.getService(InventoryManagementServiceImpl.class, this.getLocale());
        Long orderId = extractOrderId(shipmentId);
        List<IDemandItem> demandItems = ds.getDemandItems(orderId, pm);
        for (IShipmentItem shipmentItem : shipment.getShipmentItems()) {
          for (IDemandItem demandItem : demandItems) {
            if (demandItem.getMaterialId().equals(shipmentItem.getMaterialId())) {
              demandItem.setShippedQuantity(
                  demandItem.getShippedQuantity().add(shipmentItem.getQuantity()));
              if (status == ShipmentStatus.FULFILLED) {
                demandItem.setFulfilledQuantity(
                    demandItem.getFulfilledQuantity().add(shipmentItem.getQuantity()));
              } else {
                IInvntry
                    custInvntry =
                    ims.getInventory(shipment.getKioskId(), demandItem.getMaterialId(), pm);
                custInvntry.setInTransitStock(
                    custInvntry.getInTransitStock().add(shipmentItem.getQuantity()));
                pm.makePersistent(custInvntry);
              }
            }
          }
        }
        pm.makePersistentAll(demandItems);
      }
      updateMessageAndHistory(shipmentId, message, userId, shipment.getOrderId(),
          shipment.getDomainId(), prevStatus, shipment.getStatus(), shpDate, pm);
      if (closePM) {
        tx.commit();
      }
    } catch (Exception e) {
      xLogger.severe("Error while getting shipment details.", e);
      return false;
    } finally {
      if (closePM && tx.isActive()) {
        tx.rollback();
      }
      if (closePM) {
        pm.close();
      }
    }
    return true;
  }

  @Override
  public Map<String, Object> getShipmentItemAsMap(IShipmentItem shipment, String currency,
                                                  Locale locale, String timezone) {
    Map<String, Object> siMap = new HashMap();
    if (shipment.getMaterialId() != null) {
      siMap.put(JsonTagsZ.MATERIAL_ID, String.valueOf(shipment.getMaterialId()));
    }
    List<Map<String, String>> batches = new ArrayList<>(1);
    try {
      MaterialCatalogService mcs = Services.getService(MaterialCatalogServiceImpl.class);
      IMaterial m = mcs.getMaterial(shipment.getMaterialId());
      if (m.isBatchEnabled()) {
        List<IShipmentItemBatch> sibList = (List<IShipmentItemBatch>) shipment
            .getShipmentItemBatch();
        if (sibList != null && !sibList.isEmpty()) {
          for (IShipmentItemBatch sib : sibList) {
            Map<String, String> sibMap = getShipmentItemBatchAsMap(sib, timezone);
            if (sibMap != null && !sibMap.isEmpty()) {
              batches.add(sibMap);
            }
          }
        }
      }
      InventoryManagementService
          ims =
          Services.getService(InventoryManagementServiceImpl.class);
      BigDecimal alq = getAllocatedQuantityForShipmentItem(ims, shipment);
      if (alq != null) {
        siMap.put(JsonTagsZ.ALLOCATED_QUANTITY, BigUtil.getFormattedValue(alq));
      }
    } catch (Exception e) {
      xLogger.warn("Exception while getting inventory allocation for the shipment item {0}",
          shipment.getShipmentItemId(), e);
    }

    if (shipment.getQuantity() != null) {
      siMap.put(JsonTagsZ.QUANTITY, BigUtil.getFormattedValue(shipment.getQuantity()));
    }
    if (shipment.getFulfilledQuantity() != null) {
      siMap.put(JsonTagsZ.FULFILLED_QUANTITY,
          BigUtil.getFormattedValue(shipment.getFulfilledQuantity()));
    }
    if (shipment.getFulfilledDiscrepancyReason() != null && !shipment
        .getFulfilledDiscrepancyReason().isEmpty()) {
      siMap.put(JsonTagsZ.REASON_FOR_PARTIAL_FULFILLMENT, shipment.getFulfilledDiscrepancyReason());
    }
    if (shipment.getUpdatedOn() != null) {
      siMap.put(JsonTagsZ.TIMESTAMP,
          LocalDateUtil.format(shipment.getUpdatedOn(), locale, timezone));
    } else if (shipment.getCreatedOn() != null) {
      siMap.put(JsonTagsZ.TIMESTAMP,
          LocalDateUtil.format(shipment.getCreatedOn(), locale, timezone));
    }
    // Message - ?
    if (shipment.getFulfilledMaterialStatus() != null && !shipment.getFulfilledMaterialStatus()
        .isEmpty()) {
      siMap.put(JsonTagsZ.MATERIAL_STATUS, shipment.getFulfilledMaterialStatus());
    }
    // Add custom material ID
    try {
      MaterialCatalogService mcs = Services.getService(MaterialCatalogServiceImpl.class);
      IMaterial m = mcs.getMaterial(shipment.getMaterialId());
      String customMaterialId = m.getCustomId();
      if (customMaterialId != null && !customMaterialId.isEmpty()) {
        siMap.put(JsonTagsZ.CUSTOM_MATERIALID, customMaterialId);
      }
    } catch (Exception e) {
      xLogger.warn("Exception when getting custom material ID for material {1}",
          shipment.getMaterialId(), e);
    }
    // Add batches if has inventory allocation by batches is present
    if (batches != null && !batches.isEmpty()) {
      siMap.put(JsonTagsZ.BATCHES, batches);
    }
    return siMap;
  }

  private BigDecimal getAllocatedQuantityForShipmentItem(InventoryManagementService ims,
      IShipmentItem shipmentItem) {

    try {
      List<IInvAllocation>
          iAllocs =
          ims.getAllocationsByTypeId(shipmentItem.getKioskId(), shipmentItem.getMaterialId(),
              IInvAllocation.Type.SHIPMENT, String.valueOf(shipmentItem.getShipmentItemId()));
      if (iAllocs != null && !iAllocs.isEmpty()) {
        BigDecimal alq = new BigDecimal(0);
        for (IInvAllocation iAlloc : iAllocs) {
          alq = alq.add(iAlloc.getQuantity());
        }
        return alq;
      }
    } catch (Exception e) {
      xLogger.warn("Exception while getting inventory allocation for the demand item {0}",
          shipmentItem.getShipmentId(), e);
    }
    return null;
  }

  public Map<String, String> getShipmentItemBatchAsMap(IShipmentItemBatch sib, String timezone) {
    Map<String, String> map = new HashMap<>(1);
    if (StringUtils.isNotEmpty(sib.getBatchId())) {
      map.put(JsonTagsZ.BATCH_ID, sib.getBatchId());
    }
    if (StringUtils.isNotEmpty(sib.getBatchManufacturer())) {
      map.put(JsonTagsZ.BATCH_MANUFACTUER_NAME, sib.getBatchManufacturer());
    }
    if (sib.getBatchManufacturedDate() != null) {
      map.put(JsonTagsZ.BATCH_MANUFACTURED_DATE,
          LocalDateUtil.formatCustom(sib.getBatchManufacturedDate(), Constants.DATE_FORMAT,
              timezone));
    }
    if (sib.getBatchExpiry() != null) {
      map.put(JsonTagsZ.BATCH_EXPIRY,
          LocalDateUtil.formatCustom(sib.getBatchExpiry(), Constants.DATE_FORMAT, timezone));
    }

    map.put(JsonTagsZ.QUANTITY, BigUtil.getFormattedValue(sib.getQuantity()));
    map.put(JsonTagsZ.FULFILLED_QUANTITY, BigUtil.getFormattedValue(sib.getFulfilledQuantity()));
    if (sib.getUpdatedOn() != null) {
      map.put(JsonTagsZ.TIMESTAMP,
          LocalDateUtil.formatCustom(sib.getUpdatedOn(), Constants.DATE_FORMAT, timezone));
    } else if (sib.getCreatedOn() != null) {
      map.put(JsonTagsZ.TIMESTAMP,
          LocalDateUtil.formatCustom(sib.getCreatedOn(), Constants.DATE_FORMAT, timezone));
    }
    // Get inventoryAllocation for batch
    try {
      InventoryManagementService
          ims =
          Services.getService(InventoryManagementServiceImpl.class);
      List<IInvAllocation>
          iAllocs =
          ims.getAllocationsByTypeId(sib.getKioskId(), sib.getMaterialId(),
              IInvAllocation.Type.SHIPMENT, sib.getShipmentItemId().toString());

      if (iAllocs != null && !iAllocs.isEmpty()) {
        for (IInvAllocation iAlloc : iAllocs) {
          if (iAlloc.getBatchId() != null && !iAlloc.getBatchId().isEmpty() && iAlloc.getBatchId()
              .equals(sib.getBatchId())) {
            map.put(JsonTagsZ.ALLOCATED_QUANTITY, BigUtil.getFormattedValue(iAlloc.getQuantity()));
          }
        }
      }
    } catch (Exception e) {
      xLogger.warn(
          "Exception while getting inventory allocation for the shipment item batch with id {0}",
          sib.getShipmentItemId(), e);
    }
    return map;
  }

  private List<IMaterial> getMaterialsNotExistingInKiosk(Long kioskId, IShipment shipment, PersistenceManager pm) {
    List<IMaterial> materialsNotExisting = new ArrayList<>(1);
    try {
      InventoryManagementService
          ims =
          Services.getService(InventoryManagementServiceImpl.class);
      MaterialCatalogService mcs = Services.getService(MaterialCatalogServiceImpl.class);
      for (IShipmentItem shipmentItem : shipment.getShipmentItems()) {
        IInvntry inv = ims.getInventory(kioskId, shipmentItem.getMaterialId(), pm);
        if (inv == null && BigUtil.greaterThanZero(shipmentItem.getQuantity())) {
          IMaterial material = mcs.getMaterial(shipmentItem.getMaterialId());
          materialsNotExisting.add(material);
        }
      }
    } catch (ServiceException e) {
      xLogger.warn("Exception while getting materials not existing in kioskId {0}", kioskId, e);
    }
    return materialsNotExisting;
  }

  private List<IMaterial> getMaterialsNotExistingInKiosk(Long kioskId,
                                                         List<ShipmentItemModel> models, PersistenceManager pm) {
    List<IMaterial> materialsNotExisting = new ArrayList<>(1);
    try {
      InventoryManagementService
          ims =
          Services.getService(InventoryManagementServiceImpl.class);
      MaterialCatalogService mcs = Services.getService(MaterialCatalogServiceImpl.class);
      for (ShipmentItemModel shipmentItem : models) {
        IInvntry inv = ims.getInventory(kioskId, shipmentItem.mId, pm);
        if (inv == null) {
          IMaterial material = mcs.getMaterial(shipmentItem.mId);
          materialsNotExisting.add(material);
        }
      }
    } catch (ServiceException e) {
      xLogger.warn("Exception while getting materials not existing in kioskId {0}", kioskId, e);
    }
    return materialsNotExisting;
  }

  private void validateBeforeCreateShipment(ShipmentModel model, PersistenceManager pm)
      throws ServiceException {
    // Validate only vendor inventory while creating shipment
    InventoryManagementService
        ims =
        Services.getService(InventoryManagementServiceImpl.class);
    MaterialCatalogService mcs = Services.getService(MaterialCatalogServiceImpl.class);
    List<IMaterial>
        materialsNotExistingInVendor =
        getMaterialsNotExistingInKiosk(model.vendorId, model.items, pm);
    if (materialsNotExistingInVendor != null && !materialsNotExistingInVendor.isEmpty()) {
      EntitiesService as = Services.getService(EntitiesServiceImpl.class);
      IKiosk vnd = as.getKiosk(model.vendorId, false);
      throw new ServiceException("I005", MsgUtil.bold(vnd.getName()),
          MaterialUtils.getMaterialNamesString(
              materialsNotExistingInVendor));
    }
  }

  private ResponseModel validateStatusChange(IShipment shipment, String newStatus, PersistenceManager pm)
      throws ServiceException {
    // Validate vendor inventory if newStatus is shipped (throw exception) or cancelled (show warning if previous status is not pending)
    // Validate customer inventory if newstatus is fulfilled. (show warning)
    ResponseModel responseModel = new ResponseModel();
    EntitiesService as = Services.getService(EntitiesServiceImpl.class);
    List<IMaterial> materialsNotExistingInCustomer = getMaterialsNotExistingInKiosk(
        shipment.getKioskId(), shipment, pm);
    List<IMaterial>
        materialsNotExistingInVendor =
        getMaterialsNotExistingInKiosk(shipment.getServicingKiosk(), shipment, pm);
    // If auto posting of transactions is configured
    DomainConfig dc = DomainConfig.getInstance(shipment.getDomainId());
    if (ShipmentStatus.FULFILLED.toString().equals(newStatus)
        && materialsNotExistingInCustomer != null
        && !materialsNotExistingInCustomer.isEmpty()) {
      IKiosk cst = as.getKiosk(shipment.getKioskId(), false);
      responseModel.status = true;
      if (dc.autoGI()) {
        responseModel.message =
            backendMessages.getString("the.following.items")
                + CharacterConstants.SPACE + MsgUtil.bold(cst.getName())
                + CharacterConstants.DOT + CharacterConstants.SPACE + backendMessages
                .getString("receipts.not.posted") + CharacterConstants.DOT
                + MaterialUtils.getMaterialNamesString(
                materialsNotExistingInCustomer);
      }
      return responseModel;
    }
    if ((ShipmentStatus.SHIPPED.toString().equals(newStatus) || ShipmentStatus.CANCELLED.toString()
        .equals(newStatus)) && materialsNotExistingInVendor != null && !materialsNotExistingInVendor
        .isEmpty()) {
      IKiosk vnd = as.getKiosk(shipment.getServicingKiosk(), false);
      if (ShipmentStatus.SHIPPED.toString().equals(newStatus)) {
        throw new ServiceException("I006", MsgUtil.bold(vnd.getName()),
            MaterialUtils.getMaterialNamesString(
                materialsNotExistingInVendor));

      }
      if (ShipmentStatus.CANCELLED.toString().equals(newStatus) && !ShipmentStatus.PENDING
          .toString().equals(shipment.getStatus().toString()) && !ShipmentStatus.OPEN
          .toString().equals(shipment.getStatus().toString())) {
        responseModel.status = true;
        if (dc.autoGI()) {
          responseModel.message = backendMessages.getString("the.following.items")
              + CharacterConstants.SPACE + MsgUtil.bold(vnd.getName())
              + CharacterConstants.DOT + CharacterConstants.SPACE + backendMessages
              .getString("receipts.not.posted") + CharacterConstants.DOT + MaterialUtils.getMaterialNamesString(
              materialsNotExistingInVendor);
        }
        return responseModel;
      }
    }
    responseModel.status = true;
    return responseModel;
  }

  public void checkShipmentRequest(Long customerKioskId,Long vendorKioskId,List itemList) throws ServiceException{

    EntitiesService as = Services.getService(EntitiesServiceImpl.class);
    IKiosk customerKiosk=as.getKiosk(customerKioskId);
    IKiosk vendorKiosk=as.getKiosk(vendorKioskId);

    boolean checkBEMaterials=customerKiosk.isBatchMgmtEnabled() && !vendorKiosk.isBatchMgmtEnabled();
    if(checkBEMaterials){
      MaterialCatalogService
              materialCatalogService =
              Services.getService(MaterialCatalogServiceImpl.class);
      List<String> berrorMaterials = new ArrayList<>(1);


      Long materialId=null;
      BigDecimal quantity=null;
      for (Object item : itemList) {

        if(item instanceof ShipmentItem){
          materialId=((ShipmentItem) item).getMaterialId();
          quantity=((ShipmentItem) item).getQuantity();
        }else if(item instanceof IDemandItem){
          materialId=((IDemandItem) item).getMaterialId();
          quantity=((IDemandItem) item).getQuantity();
        }
        if(materialId!=null && quantity!=null) {
          IMaterial material = materialCatalogService.getMaterial(materialId);
          if (material.isBatchEnabled() && BigUtil.greaterThanZero(quantity)) {
            berrorMaterials.add(material.getName());
          }
        }
      }

      if (!berrorMaterials.isEmpty()) {
        throw new ServiceException("O005",berrorMaterials.size(),customerKiosk.getName(),StringUtil.getCSV(berrorMaterials));
      }

    }
  }

}

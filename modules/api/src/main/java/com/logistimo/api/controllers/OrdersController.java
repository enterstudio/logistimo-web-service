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

package com.logistimo.api.controllers;

import com.google.gson.internal.LinkedTreeMap;

import com.logistimo.AppFactory;
import com.logistimo.api.builders.DemandBuilder;
import com.logistimo.api.builders.OrdersAPIBuilder;
import com.logistimo.api.models.DemandItemBatchModel;
import com.logistimo.api.models.DemandModel;
import com.logistimo.api.models.OrderMaterialsModel;
import com.logistimo.api.models.OrderModel;
import com.logistimo.api.models.OrderResponseModel;
import com.logistimo.api.models.OrderStatusModel;
import com.logistimo.api.models.OrderUpdateModel;
import com.logistimo.api.models.PaymentModel;
import com.logistimo.api.models.Permissions;
import com.logistimo.api.models.UserContactModel;
import com.logistimo.api.util.DedupUtil;
import com.logistimo.auth.SecurityConstants;
import com.logistimo.auth.SecurityMgr;
import com.logistimo.auth.utils.SecurityUtils;
import com.logistimo.auth.utils.SessionMgr;
import com.logistimo.config.models.DomainConfig;
import com.logistimo.config.models.EventsConfig;
import com.logistimo.config.models.OrdersConfig;
import com.logistimo.constants.CharacterConstants;
import com.logistimo.constants.Constants;
import com.logistimo.constants.SourceConstants;
import com.logistimo.dao.JDOUtils;
import com.logistimo.entities.service.EntitiesService;
import com.logistimo.entities.service.EntitiesServiceImpl;
import com.logistimo.events.generators.EventGeneratorFactory;
import com.logistimo.events.generators.OrdersEventGenerator;
import com.logistimo.exception.BadRequestException;
import com.logistimo.exception.InvalidDataException;
import com.logistimo.exception.InvalidServiceException;
import com.logistimo.exception.LogiException;
import com.logistimo.exception.UnauthorizedException;
import com.logistimo.inventory.entity.IInvAllocation;
import com.logistimo.inventory.entity.ITransaction;
import com.logistimo.inventory.exceptions.InventoryAllocationException;
import com.logistimo.inventory.service.InventoryManagementService;
import com.logistimo.inventory.service.impl.InventoryManagementServiceImpl;
import com.logistimo.logger.XLog;
import com.logistimo.models.ICounter;
import com.logistimo.models.shipments.ShipmentItemBatchModel;
import com.logistimo.orders.OrderResults;
import com.logistimo.orders.OrderUtils;
import com.logistimo.orders.approvals.service.IOrderApprovalsService;
import com.logistimo.orders.entity.IDemandItem;
import com.logistimo.orders.entity.IOrder;
import com.logistimo.orders.models.UpdatedOrder;
import com.logistimo.orders.service.IDemandService;
import com.logistimo.orders.service.OrderManagementService;
import com.logistimo.orders.service.impl.DemandService;
import com.logistimo.orders.service.impl.OrderManagementServiceImpl;
import com.logistimo.pagination.Navigator;
import com.logistimo.pagination.PageParams;
import com.logistimo.pagination.Results;
import com.logistimo.security.SecureUserDetails;
import com.logistimo.services.ObjectNotFoundException;
import com.logistimo.services.Resources;
import com.logistimo.services.ServiceException;
import com.logistimo.services.Services;
import com.logistimo.services.cache.MemcacheService;
import com.logistimo.services.impl.PMF;
import com.logistimo.shipments.entity.IShipment;
import com.logistimo.shipments.service.IShipmentService;
import com.logistimo.shipments.service.impl.ShipmentService;
import com.logistimo.tags.TagUtil;
import com.logistimo.tags.dao.ITagDao;
import com.logistimo.tags.dao.TagDao;
import com.logistimo.tags.entity.ITag;
import com.logistimo.utils.BigUtil;
import com.logistimo.utils.Counter;
import com.logistimo.utils.LocalDateUtil;
import com.logistimo.utils.LockUtil;
import com.logistimo.utils.MsgUtil;
import com.logistimo.utils.StringUtil;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

import javax.jdo.PersistenceManager;
import javax.jdo.Transaction;
import javax.servlet.http.HttpServletRequest;

@Controller
@RequestMapping("/orders")
public class OrdersController {


  public static final String WARN_PREFIX = "w:";
  private static final XLog xLogger = XLog.getLog(OrdersController.class);
  private static final String CACHE_KEY = "order";

    private static final String PERMISSIONS = "permissions";

  @Autowired
  OrdersAPIBuilder builder;

  @Autowired
  IOrderApprovalsService orderApprovalsService;

  @RequestMapping("/order/{orderId}")
  public
  @ResponseBody
  OrderModel getOrder(@PathVariable Long orderId,
                      @RequestParam(required = false, value = "embed") String[] embed,
                      HttpServletRequest request) throws Exception {
    SecureUserDetails user = SecurityUtils.getUserDetails(request);
    Locale locale = user.getLocale();
    OrderModel model;

    Long domainId = SessionMgr.getCurrentDomain(request.getSession(), user.getUsername());
    OrderManagementService oms =
        Services.getService(OrderManagementServiceImpl.class, locale);
    IOrder order = oms.getOrder(orderId);
    if (order == null || domainId == null) {
      throw new InvalidServiceException(CharacterConstants.EMPTY);
    }
    List<Long> domainIds = order.getDomainIds();
    if (domainIds != null && !domainIds.contains(domainId)) {
      throw new UnauthorizedException(CharacterConstants.EMPTY, HttpStatus.FORBIDDEN);
    }
    model = builder.buildFullOrderModel(order, user, domainId);
    model.setApprovalTypesModels(builder.buildOrderApprovalTypesModel(model, oms, locale));
    Integer approvalType = builder.getApprovalType(order);
    boolean isApprovalRequired = false;
    if (approvalType != null) {
      model.setApprover(
          builder.buildOrderApproverModel(user.getUsername(), approvalType, domainId, order));
      isApprovalRequired = orderApprovalsService.isApprovalRequired(order, approvalType);
    }
    if (embed != null) {
      for (String s : embed) {
        if (s.equals(PERMISSIONS)) {
          Permissions
              permissions =
              builder.buildPermissionModel(order, model, approvalType, isApprovalRequired);
          model.setPermissions(permissions);
        }
      }
    }
    return model;

  }

  @RequestMapping(value = "/order/{orderId}/approvers", method = RequestMethod.GET)
  public
  @ResponseBody
  List<UserContactModel> getPrimaryApprovers(@PathVariable Long orderId)
      throws ServiceException, ObjectNotFoundException {
    OrderManagementService oms = Services.getService(OrderManagementServiceImpl.class);
    IOrder order = oms.getOrder(orderId);
    Integer approvalType = builder.getApprovalType(order);
    return builder.buildPrimaryApprovers(order, SecurityUtils.getLocale(), approvalType);
  }

  @RequestMapping("/")
  public
  @ResponseBody
  Results getDomainOrders(@RequestParam(defaultValue = PageParams.DEFAULT_OFFSET_STR) int offset,
                          @RequestParam(defaultValue = PageParams.DEFAULT_SIZE_STR) int size,
                          @RequestParam(required = false) String status,
                          @RequestParam(required = false) String from,
                          @RequestParam(required = false) String until,
                          @RequestParam(required = false) String otype,
                          @RequestParam(required = false) String tgType,
                          @RequestParam(required = false) String tag,
                          @RequestParam(required = false) Integer oty,
                          @RequestParam(required = false) String rid,
                          @RequestParam(required = false) String approval_status,
                          HttpServletRequest request) {
    return getOrders(null, offset, size, status, from, until, otype, tgType, tag, oty, rid,
        approval_status, request);
  }

  @RequestMapping("/entity/{entityId}")
  public
  @ResponseBody
  Results getEntityOrders(@PathVariable Long entityId,
                          @RequestParam(defaultValue = PageParams.DEFAULT_OFFSET_STR) int offset,
                          @RequestParam(defaultValue = PageParams.DEFAULT_SIZE_STR) int size,
                          @RequestParam(required = false) String status,
                          @RequestParam(required = false) String from,
                          @RequestParam(required = false) String until,
                          @RequestParam(defaultValue = IOrder.TYPE_SALE) String otype,
                          @RequestParam(required = false) String tgType,
                          @RequestParam(required = false) String tag,
                          @RequestParam(required = false) Integer oty,
                          @RequestParam(required = false) String rid,
                          @RequestParam(required = false) String approval_status,
                          HttpServletRequest request) {
    return getOrders(entityId, offset, size, status, from, until, otype, tgType, tag, oty, rid,
        approval_status, request);
  }


  @RequestMapping(value = "/order/{orderId}/vendor", method = RequestMethod.POST)
  public
  @ResponseBody
  OrderResponseModel updateVendor(@PathVariable Long orderId, @RequestBody OrderUpdateModel model,
                                  HttpServletRequest request) {
    return updateOrder("vend", orderId, model.orderUpdatedAt, null, Long.valueOf(model.updateValue),
        null, null, null, request);
  }

  @RequestMapping(value = "/order/{orderId}/transporter", method = RequestMethod.POST)
  public
  @ResponseBody
  OrderResponseModel updateTransporter(@PathVariable Long orderId, @RequestBody OrderUpdateModel model,
                                       HttpServletRequest request) {
    return updateOrder("trans", orderId, model.orderUpdatedAt, null, null, null, model.updateValue, null, request);
  }

  @RequestMapping(value = "/order/{orderId}/status", method = RequestMethod.POST)
  public
  @ResponseBody
  OrderResponseModel updateStatus(@PathVariable Long orderId, @RequestBody OrderStatusModel status,
                                  HttpServletRequest request) {
    SecureUserDetails user = SecurityUtils.getUserDetails(request);
    Locale locale = user.getLocale();
    ResourceBundle backendMessages = Resources.get().getBundle("BackendMessages", locale);
    Long domainId = SecurityUtils.getCurrentDomainId();
    try {
      UpdatedOrder updOrder;
      OrderManagementService oms = Services.getService(OrderManagementServiceImpl.class, locale);
      IOrder o = oms.getOrder(orderId,true);
      if(!status.orderUpdatedAt.equals(LocalDateUtil.formatCustom(o.getUpdatedOn(), Constants.DATETIME_FORMAT, null))) {
        throw new LogiException("O004", user.getUsername(), LocalDateUtil.format(o.getUpdatedOn(), user.getLocale(), user.getTimezone()));
      }
      if (IOrder.COMPLETED.equals(status.st) || IOrder.FULFILLED.equals(status.st)) {
        SimpleDateFormat sdf = new SimpleDateFormat(Constants.DATE_FORMAT_CSV);
        Date efd = null;
        if (status.efd != null) {
          efd = sdf.parse(status.efd);
        }

        IShipmentService shipmentService = Services.getService(ShipmentService.class,
            user.getLocale());
        List<IShipment> shipments = shipmentService.getShipmentsByOrderId(orderId);
        String shipmentId;
        if (status.st.equals(IOrder.COMPLETED)) {
          if (shipments != null && !shipments.isEmpty()) {
            xLogger.warn("Order already has shipments, hence cannot be shipped", orderId);
            throw new BadRequestException(
                backendMessages.getString("error.unabletoshiporder.shipments"));
          }
          if (o.isStatus(IOrder.COMPLETED) || o.isStatus(IOrder.CANCELLED) ||
              o.isStatus(IOrder.FULFILLED)) {
            xLogger.warn("Invalid order {0} status {1} cannot ship ", orderId, o.getStatus());
            throw new BadRequestException(backendMessages.getString("error.unabletoshiporder"));
          }
        }
        if (!o.isStatus(IOrder.COMPLETED) && !o.isStatus(IOrder.CANCELLED) && !o
            .isStatus(IOrder.FULFILLED)) {
          shipmentId =
              oms.shipNow(o, status.t, status.tid, status.cdrsn, efd, user.getUsername(),
                  status.ps,SourceConstants.WEB);
        } else if (o.isStatus(IOrder.COMPLETED)) {
          if (shipments == null || shipments.size() > 1) {
            xLogger.warn("Invalid order {0} ({1}) cannot fulfill, already has more shipments or " +
                "no shipments ", orderId, o.getStatus());
            throw new BadRequestException(backendMessages.getString("error.unabletofulfilorder"));
          }
          shipmentId = shipments.get(0).getShipmentId();
        } else {
          xLogger.warn("Invalid order {0} status ({1}) cannot fulfill ", orderId, o.getStatus());
          throw new BadRequestException(backendMessages.getString("error.unabletofulfilorder"));
        }

        if (IOrder.FULFILLED.equals(status.st)) {
          if (shipmentId != null) {
            shipmentService.fulfillShipment(shipmentId, user.getUsername(),SourceConstants.WEB);
          } else {
            xLogger.warn("Invalid order {0} status ({1}) cannot fulfill ", orderId, o.getStatus());
            throw new BadRequestException(backendMessages.getString("error.unabletofulfilorder"));
          }
        }

        //Messages added to order, anyone using ship now will not be using shipments.
        if (status.msg != null) {
          oms.addMessageToOrder(orderId, status.msg, user.getUsername());
        }
        o = oms.getOrder(orderId);
        updOrder = new UpdatedOrder(o);
      } else {
        updOrder = oms.updateOrderStatus(orderId, status.st,
            user.getUsername(), status.msg, status.users,
            SourceConstants.WEB, null, status.cdrsn);
      }
      return builder.buildOrderResponseModel(updOrder, true, user, domainId, true);
    } catch (ServiceException ie) {
      xLogger.severe("Error in updating order status", ie);
      if (ie.getCode() != null) {
        throw new InvalidDataException(ie.getMessage());
      } else {
        throw new InvalidServiceException(backendMessages.getString("order.status.update.error"));
      }
    } catch (BadRequestException e) {
      throw e;
    } catch (LogiException le) {
      xLogger.severe("Failed to update order status", le);
      throw new InvalidServiceException(le.getMessage());
    } catch (Exception e) {
      xLogger.severe("Failed to update order status", e);
      throw new InvalidServiceException(backendMessages.getString("order.status.update.error"));
    }
  }


  @RequestMapping(value = "/order/{orderId}/payment", method = RequestMethod.POST)
  public
  @ResponseBody
  OrderResponseModel updatePayment(@PathVariable Long orderId,
                                   @RequestBody PaymentModel paymentDetails,
                                   HttpServletRequest request) {
    return updateOrder("pmt", orderId, paymentDetails.orderUpdatedAt, paymentDetails, null, null,
        null, null, request);
  }

  @RequestMapping(value = "/order/{orderId}/fulfillmenttime", method = RequestMethod.POST)
  public
  @ResponseBody
  OrderResponseModel updateFulfillmentTime(@PathVariable Long orderId,
                                           @RequestBody OrderUpdateModel model,
                                           HttpServletRequest request) {
    return updateOrder("cft", orderId, model.orderUpdatedAt, null, null, model.updateValue, null,
        null, request);
  }

  @RequestMapping(value = "/order/{orderId}/efd", method = RequestMethod.POST)
  public
  @ResponseBody
  OrderResponseModel updateExpectedFulfillmentDate(@PathVariable Long orderId,
                                                   @RequestBody OrderUpdateModel model,
                                                   HttpServletRequest request) {
    return updateOrder("efd", orderId, model.orderUpdatedAt, null, null, model.updateValue, null,
        null, request);
  }

  @RequestMapping(value = "/order/{orderId}/edd", method = RequestMethod.POST)
  public
  @ResponseBody
  OrderResponseModel updateDueDate(@PathVariable Long orderId, @RequestBody OrderUpdateModel model,
                                   HttpServletRequest request) {
    return updateOrder("edd", orderId, model.orderUpdatedAt, null, null, model.updateValue, null,
        null, request);
  }

  @RequestMapping(value = "/order/{orderId}/statusJSON", method = RequestMethod.GET)
  public
  @ResponseBody
  String getOrderStatusJSON(@PathVariable Long orderId, HttpServletRequest request) {
    SecureUserDetails user = SecurityUtils.getUserDetails(request);
    Locale locale = user.getLocale();
    ResourceBundle backendMessages = Resources.get().getBundle("BackendMessages", locale);
    Long domainId = SessionMgr.getCurrentDomain(request.getSession(), user.getUsername());
    EventsConfig ec = DomainConfig.getInstance(domainId).getEventsConfig();
    OrderManagementService oms;
    try {
      oms = Services.getService(OrderManagementServiceImpl.class, user.getLocale());
      IOrder order = oms.getOrder(orderId);
      List<String> excludeVars = new ArrayList<>(1);
      excludeVars.add(EventsConfig.VAR_ORDERSTATUS);
      OrdersEventGenerator eg = (OrdersEventGenerator) EventGeneratorFactory
          .getEventGenerator(domainId, JDOUtils.getImplClassName(IOrder.class));
      return eg.getOrderStatusJSON(order, user.getLocale(), user.getTimezone(), excludeVars);
    } catch (ServiceException | ObjectNotFoundException e) {
      xLogger.severe("Error in fetching order status for order {0}", orderId, e);
      throw new InvalidServiceException(backendMessages.getString("order.status.fetch.error"));
    }
  }

  @RequestMapping(value = "/order/{orderId}/items", method = RequestMethod.POST)
  public
  @ResponseBody
  OrderResponseModel updateDemandItems(@PathVariable Long orderId,
                                       @RequestBody OrderMaterialsModel model,
                                       HttpServletRequest request) {
    SecureUserDetails user = SecurityUtils.getUserDetails(request);
    Locale locale = user.getLocale();
    ResourceBundle backendMessages = Resources.get().getBundle("BackendMessages", locale);
    Long domainId = SessionMgr.getCurrentDomain(request.getSession(), user.getUsername());
    LockUtil.LockStatus lockStatus = LockUtil.lock(Constants.TX_O + orderId);
    if (!LockUtil.isLocked(lockStatus)) {
      throw new InvalidServiceException(new ServiceException("O002", orderId));
    }
    PersistenceManager pm = null;
    Transaction tx = null;
    try {
      OrderManagementService
          oms =
          Services.getService(OrderManagementServiceImpl.class, user.getLocale());
      IOrder order = oms.getOrder(orderId);
      if (order == null) {
        throw new BadRequestException(backendMessages.getString("order.none") + " " + orderId);
      }
      if (!model.orderUpdatedAt.equals(
          LocalDateUtil.formatCustom(order.getUpdatedOn(), Constants.DATETIME_FORMAT, null))) {
        throw new LogiException("O004", user.getUsername(),
            LocalDateUtil.format(order.getUpdatedOn(), user.getLocale(), user.getTimezone()));
      }
      if (order.isStatus(IOrder.CANCELLED) || order.isStatus(IOrder.FULFILLED)) {
        xLogger.warn("User {0} tried to edit materials of {1} order {2}", user, order.getStatus(),
            orderId);
        throw new BadRequestException(new ServiceException("O003", orderId,
            OrderUtils.getStatusDisplay(order.getStatus(), locale)));
      }
      DomainConfig dc = DomainConfig.getInstance(domainId);
      IDemandService dms = Services.getService(DemandService.class);
      order.setItems(dms.getDemandItems(orderId));
      List<ITransaction> transactions = builder.buildTransactionsForNewItems(order, model.items);
      if (transactions != null && !transactions.isEmpty()) {
        oms.modifyOrder(order, user.getUsername(), transactions, new Date(), domainId,
            ITransaction.TYPE_REORDER, model.msg, null, null, BigDecimal.ZERO, null, null,
            dc.allowEmptyOrders(), order.getTags(TagUtil.TYPE_ORDER), order.getOrderType(),
            order.getReferenceID());
      }
      order = builder.buildOrderMaterials(order, model.items);
      //TODO use OrderManagementServiceImpl updateOrderWithAllocations
      String oIdStr = String.valueOf(order.getOrderId());
      pm = PMF.get().getPersistenceManager();
      tx = pm.currentTransaction();
      tx.begin();
      if (dc.autoGI() && order.getServicingKiosk() != null) {
        InventoryManagementService
            ims =
            Services.getService(InventoryManagementServiceImpl.class);
        String tag = IInvAllocation.Type.ORDER.toString() + CharacterConstants.COLON + oIdStr;
        for (DemandModel item : model.items) {
          List<ShipmentItemBatchModel> batchDetails = null;
          List<IInvAllocation> invAllocations = ims.getAllocationsByTagMaterial(item.id,tag);
          BigDecimal totalShipmentAllocation = BigDecimal.ZERO;
          for(IInvAllocation invAllocation : invAllocations) {
            if(IInvAllocation.Type.SHIPMENT.toString().equals(invAllocation.getType())) {
              totalShipmentAllocation = totalShipmentAllocation
                  .add(invAllocation.getQuantity());
            }
          }
          IDemandItem demandItem = order.getItem(item.id);
          if(item.astk != null && BigUtil.greaterThan(item.astk, item.q.subtract(
              demandItem.getShippedQuantity().add(totalShipmentAllocation)))) {
            throw new ServiceException(backendMessages.getString("allocated.qty.greater"));
          }
          if (item.isBa) {
            item.astk = null;
            if (item.bts != null) {
              batchDetails = new ArrayList<>(item.bts.size());
              for (DemandItemBatchModel bt : item.bts) {
                ShipmentItemBatchModel details = new ShipmentItemBatchModel();
                details.id = bt.id;
                details.q = bt.q;
                details.smst = bt.mst;
                batchDetails.add(details);
              }
            }
          }
          if (item.astk != null || batchDetails != null) {
            ims.allocate(order.getServicingKiosk(), item.id, IInvAllocation.Type.ORDER, oIdStr, tag,
                item.astk, batchDetails, user.getUsername(), pm, item.isBa ? null : item.mst);
          } else {
            ims.clearAllocation(order.getServicingKiosk(), item.id, IInvAllocation.Type.ORDER,
                String.valueOf(orderId), pm);
          }
        }
      }
      UpdatedOrder
          updorder =
          oms.updateOrder(order, SourceConstants.WEB, true, true, user.getUsername());
      tx.commit();
      return builder.buildOrderResponseModel(updorder, true, user, domainId, true);
    } catch (InventoryAllocationException ie) {
      xLogger.severe("Error in updating demand items for order {0}", orderId, ie);
      if (ie.getCode() != null) {
        throw new InvalidDataException(ie.getMessage());
      } else {
        throw new InvalidServiceException(backendMessages.getString("demand.items.update.error"));
      }
    } catch (LogiException e) {
      xLogger.severe("Error in updating demand items for order {0}", orderId, e);
      if ("T001".equals(e.getCode()) || "O004".equals(e.getCode())) {
        throw new InvalidServiceException(e.getMessage());
      } else {
        throw new InvalidServiceException(backendMessages.getString("demand.items.update.error"));
      }
    } catch (BadRequestException | InvalidServiceException e) {
      throw e;
    } catch (Exception e) {
      xLogger.severe("Error in updating demand items for order {0}", orderId, e);
      throw new InvalidServiceException(backendMessages.getString("demand.items.update.error"));
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
  }

  private OrderResponseModel updateOrder(String updType, Long orderId, String orderUpdatedAt,
                                         PaymentModel paymentDetails,
                                         Long vendorId, String data, String transporter,
                                         List<String> tags, HttpServletRequest request) {
    SecureUserDetails user = SecurityUtils.getUserDetails(request);
    Locale locale = user.getLocale();
    ResourceBundle backendMessages = Resources.get().getBundle("BackendMessages", locale);
    try {
      Long domainId = SessionMgr.getCurrentDomain(request.getSession(), user.getUsername());
      OrderManagementService
          oms =
          Services.getService(OrderManagementServiceImpl.class, user.getLocale());
      IOrder order = oms.getOrder(orderId);
      boolean fullOrder = false;
      if (order == null) {
        throw new Exception(backendMessages.getString("order.none") + " " + orderId);
      }
      if (!OrderUtils.validateOrderUpdatedTime(orderUpdatedAt, order.getUpdatedOn())) {
        throw new LogiException("O004", user.getUsername(),
            LocalDateUtil.format(order.getUpdatedOn(), user.getLocale(), user.getTimezone()));
      }
      String labelText = null;
      IDemandService ds = Services.getService(DemandService.class);
      order.setItems(ds.getDemandItems(orderId));
      if (updType.equals("pmt") && paymentDetails != null) {
        order.addPayment(paymentDetails.pay);
        order.setPaymentOption(paymentDetails.po);
      } else if (updType.equals("vend")) {
        fullOrder = true;
        order.setServicingKiosk(vendorId);
      } else if (updType.equals("trans")) {
                /*if (transporter == null || transporter.trim().isEmpty()) {
                    order.clearTransporters();
                } else if (order.getTransporter(ITransporter.TYPE_FREEFORM, transporter) == null) {
                    order.clearTransporters();
                    ITransporter t = JDOUtils.createInstance(ITransporter.class);
                    orderDao.setOrderForTransporter(t, order);
                    t.setTransporterId(transporter);
                    t.setType(ITransporter.TYPE_FREEFORM);
                    t.setDomainId(domainId);
                    order.addTransporter(t);
                }*/
      } else if (updType.equals("cft")) {
        order.setConfirmedFulfillmentTimeRange(data);
      } else if (updType.equals("tgs")) {
        ITagDao tagDao = new TagDao();
        order.setTgs(tagDao.getTagsByNames(tags, ITag.ORDER_TAG), TagUtil.TYPE_ORDER);
      } else if (updType.equals("rid")) {
        order.setReferenceID(data);
      } else if (updType.equals("efd")) {
        if (StringUtils.isNotEmpty(data)) {
          SimpleDateFormat sdf = new SimpleDateFormat(Constants.DATE_FORMAT);
          order.setExpectedArrivalDate(sdf.parse(data));
          labelText =
              LocalDateUtil
                  .format(order.getExpectedArrivalDate(), user.getLocale(), user.getTimezone(),
                      true);
        } else {
          order.setExpectedArrivalDate(null);
        }
      } else if (updType.equals("edd")) {
        if (StringUtils.isNotEmpty(data)) {
          SimpleDateFormat sdf = new SimpleDateFormat(Constants.DATE_FORMAT);
          order.setDueDate(sdf.parse(data));
          labelText =
              LocalDateUtil.format(order.getDueDate(), user.getLocale(), user.getTimezone(), true);
        } else {
          order.setDueDate(null);
        }
      }
      order.setUpdatedBy(user.getUsername());
      order.setUpdatedOn(new Date());
      UpdatedOrder updOrder = oms.updateOrder(order, SourceConstants.WEB);
      OrderResponseModel
          orderResponseModel =
          builder.buildOrderResponseModel(updOrder, true, user, domainId, fullOrder);
      orderResponseModel.respData = labelText;
      return orderResponseModel;
    } catch (LogiException le) {
      xLogger.severe("Error in updating order", le);
      throw new InvalidServiceException(le.getMessage());
    } catch (Exception e) {
      xLogger.severe("Error in updating order {0}", orderId, e);
      throw new InvalidServiceException(backendMessages.getString("order.update.error"));
    }
  }

  public Results getOrders(Long entityId, int offset, int size,
                           String status, String from, String until, String otype, String tgType,
                           String tag, Integer oty, String rid, String approvalStatus, HttpServletRequest request) {
    SecureUserDetails user = SecurityUtils.getUserDetails(request);
    Locale locale = user.getLocale();
    ResourceBundle backendMessages = Resources.get().getBundle("BackendMessages", locale);
    try {
      Long domainId = SessionMgr.getCurrentDomain(request.getSession(),
          user.getUsername());
      DomainConfig dc = DomainConfig.getInstance(domainId);
      Date startDate = null;
      Date endDate = null;
      if (from != null && !from.isEmpty()) {
        startDate = LocalDateUtil.parseCustom(from, Constants.DATE_FORMAT, dc.getTimezone());
      }
      if (until != null && !until.isEmpty()) {
        endDate = LocalDateUtil.getOffsetDate(
            LocalDateUtil.parseCustom(until, Constants.DATE_FORMAT, dc.getTimezone()), 1);
      }
      oty = oty == null ? IOrder.NONTRANSFER : oty;
      Navigator
          navigator =
          new Navigator(request.getSession(), "OrdersController.getOrders", offset, size, "dummy",
              0);
      PageParams pageParams = new PageParams(navigator.getCursor(offset), offset, size);
      OrderManagementService oms = Services.getService(OrderManagementServiceImpl.class);
      List<Long> kioskIds = null;
      if (user.getUsername() != null) {
        // Get user
        EntitiesService as = Services.getService(EntitiesServiceImpl.class, user.getLocale());
        if (SecurityConstants.ROLE_SERVICEMANAGER.equals(user.getRole())) {
          kioskIds = as.getKioskIdsForUser(user.getUsername(), null, null)
              .getResults(); // TODO: pagination?
          if (kioskIds == null || kioskIds.isEmpty()) {
            return new Results(null, null, 0, offset);
          }
        }
      }
      Results
          or =
          oms.getOrders(domainId, entityId, status, startDate, endDate, otype, tgType, tag,
              kioskIds, pageParams, oty, rid, approvalStatus);
      int total = -1;
      if (!(SecurityConstants.ROLE_SERVICEMANAGER.equals(user.getRole()) && entityId == null) && (
          status == null || status.isEmpty()) && startDate == null
          && endDate == null && rid == null) {
        ICounter counter;
        if (tag != null && !tag.isEmpty()) {
          counter = Counter.getOrderCounter(domainId, tag, oty, tgType);
        } else if (oty == IOrder.TRANSFER) {
          counter = Counter.getTransferOrderCounter(domainId, entityId, otype);
        } else {
          counter = Counter.getOrderCounter(domainId, entityId, otype, oty);
        }
        total = counter.getCount();
      }
      or.setNumFound(total);
      or.setOffset(offset);
      return builder.buildOrders(or, user, SecurityUtils.getDomainId(request));
    } catch (Exception e) {
      xLogger.severe("Error in fetching orders for entity {0} of type {1}", entityId, otype, e);
      throw new InvalidServiceException(backendMessages.getString("orders.fetch.error"));
    }
  }

  @RequestMapping(value = "/add/", method = RequestMethod.POST)
  public
  @ResponseBody
  OrderMaterialsModel createOrder(@RequestBody Map<String, Object> orders,
                                  HttpServletRequest request) {
    xLogger.fine("Entered create Order");
    SecureUserDetails sUser = SecurityMgr.getUserDetails(request.getSession());
    Locale locale;
    if (sUser.getLocale() != null) {
      locale = sUser.getLocale();
    } else {
      locale = new Locale(Constants.LANG_DEFAULT, Constants.COUNTRY_DEFAULT);
    }
    ResourceBundle backendMessages = Resources.get().getBundle("BackendMessages", locale);
    ResourceBundle messages = Resources.get().getBundle("Messages", locale);
    String userId = sUser.getUsername();
    Long domainId = SessionMgr.getCurrentDomain(request.getSession(), userId);
    OrderMaterialsModel model = new OrderMaterialsModel();
    MemcacheService cache = null;
    String signature = orders.get("signature") != null ?
        CACHE_KEY + String.valueOf(orders.get("signature")) : null;
    if (signature != null) {
      cache = AppFactory.get().getMemcacheService();
      if (cache != null) {
        OrderMaterialsModel oModel = validateSignature(model, cache, signature);
        if (oModel != null) {
          return oModel;
        }
      }
    }
    DomainConfig dc = DomainConfig.getInstance(domainId);
    Long kioskId = Long.parseLong(String.valueOf(orders.get("kioskid")));
    Long vendorKioskId = orders.get("vkioskid") != null ?
        Long.parseLong(String.valueOf(orders.get("vkioskid"))) : null;
    String ordMsg = null;
    if (orders.get("ordMsg") != null) {
      ordMsg = String.valueOf(orders.get("ordMsg"));
    }
    ArrayList<String> oTag = (ArrayList<String>) orders.get("oTag");
    Integer oType = Integer.parseInt(String.valueOf(orders.get("orderType")));
    boolean skipCheck = Boolean.parseBoolean(String.valueOf(orders.get("skipCheck")));
    String referenceId = null;
    if (orders.get("rid") != null) {
      referenceId = String.valueOf(orders.get("rid"));
    }
    try {
      SimpleDateFormat sdf = new SimpleDateFormat(Constants.DATE_FORMAT);
      Date edd = null;
      if (orders.get("edd") != null) {
        edd = sdf.parse(orders.get("edd").toString());
      }
      Date efd = null;
      if (orders.get("efd") != null) {
        efd = sdf.parse(orders.get("efd").toString());
      }
      List<ITransaction> transList = new ArrayList<ITransaction>();
      OrderManagementService oms =
          Services.getService(OrderManagementServiceImpl.class, locale);
      Date now = new Date();
      LinkedTreeMap materials = (LinkedTreeMap) orders.get("materials");
      for (Object m : materials.keySet()) {
        Long materialId = Long.parseLong(String.valueOf(m));
        LinkedTreeMap map = (LinkedTreeMap) materials.get(m);
        BigDecimal quantity = new BigDecimal(String.valueOf(map.get("q")));
        String reason = null;
        if (map.get("r") != null) {
          reason = String.valueOf(map.get("r"));
        }
        ITransaction
            trans =
            getInventoryTransaction(domainId, kioskId, materialId, quantity,
                ITransaction.TYPE_ORDER, userId, now, reason);
        transList.add(trans);
      }
      boolean removeSignature = false;
      if (!skipCheck && vendorKioskId != null) {
        DemandBuilder b = new DemandBuilder();
        for (ITransaction iTransaction : transList) {
          IDemandService ds = Services.getService(DemandService.class);
          Results dItems = ds.getDemandDetails(kioskId, iTransaction.getMaterialId(), true, false,
              IOrder.TYPE_PURCHASE, false);
          for (Object item : dItems.getResults()) {
            Object[] f = (Object[]) item;
            String kidStr = String.valueOf(f[0]);
            if (StringUtils.isNotEmpty(kidStr)) {
              Long kId = Long.parseLong(kidStr);
              if (vendorKioskId.equals(kId)) {
                if (model.items == null) {
                  model.items = new ArrayList<>();
                }
                model.items.add(b.buildDemandModel(sUser, kioskId, iTransaction.getMaterialId(), f,
                    false));
              }
            }
          }
        }
        if (model.items != null) {
          removeSignature = true;
        }
      }
      if (model.items == null) {
        OrderResults orderResults =
            oms.updateOrderTransactions(domainId, userId, ITransaction.TYPE_ORDER,
                transList, kioskId, null, ordMsg, dc.autoOrderGeneration(), vendorKioskId, null,
                null, null,
                null, null, null, BigDecimal.ZERO, null, null, dc.allowEmptyOrders(), oTag, oType,
                oType == 2,
                referenceId, edd, efd,SourceConstants.WEB);
        IOrder order = orderResults.getOrder();
//            String strPrice = null; // get price statement
//            if ( order != null && BigUtil.greaterThanZero(order.getTotalPrice()))
//               strPrice = backendMessages.getString("order.price") + " <b>" + order.getPriceStatement() + "</b>.";
        String prefix = CharacterConstants.EMPTY;
        if (oType == 0) {
          if (dc.getOrdersConfig() != null && dc.getOrdersConfig().isTransferRelease()) {
            prefix = "Release ";
          } else {
            prefix = messages.getString("transactions.transfer.upper") + CharacterConstants.SPACE;
          }
        }
        model.orderId=order.getOrderId();
        model.msg =
            prefix + backendMessages.getString("order.lowercase") + " <b>" + order.getOrderId()
                + "</b> " + backendMessages.getString("created.successwith") + " <b>" + order.size()
                + "</b> " + backendMessages.getString("items.lowercase") + ". ";
      }
      if (removeSignature) {
        DedupUtil.removeSignature(cache, signature);
      } else {
        DedupUtil.setSignatureAndStatus(cache, signature, DedupUtil.SUCCESS);
      }
    } catch (Exception e) {
      xLogger.severe("Error in creating Order on domain {0}", domainId, e);
      throw new InvalidServiceException(backendMessages.getString("order.create.error"));
    }
    return model;
  }

  /**
   * Check if the signature exists in cache
   */
  private OrderMaterialsModel validateSignature(OrderMaterialsModel model, MemcacheService cache,
                                                String signature) {
    Integer lastStatus = (Integer) cache.get(signature);
    if (lastStatus != null) {
      switch (lastStatus) {
        case DedupUtil.SUCCESS:
          model.msg = "This order request was previously successful. "
              + "Please check " + MsgUtil.bold("Orders") + " listing page.";
          return model;
        case DedupUtil.PENDING:
          model.msg = "The previous order request may or may not have been successful. Please "
              + "click the 'Orders' page to see if they are already submitted. If not, "
              + "please create the order again. We are sorry for the inconvenience caused.";
          return model;
        case DedupUtil.FAILED:
          DedupUtil.setSignatureAndStatus(cache, signature, DedupUtil.PENDING);
          break;
        default:
          break;
      }
    } else {
      DedupUtil.setSignatureAndStatus(cache, signature, DedupUtil.PENDING);
    }
    return null;
  }

  private ITransaction getInventoryTransaction(Long domainId, Long kioskId, Long materialId,
                                               BigDecimal quantity, String transType, String userId,
                                               Date now, String reason) {
    ITransaction t = JDOUtils.createInstance(ITransaction.class);
    t.setKioskId(kioskId);
    t.setMaterialId(materialId);
    t.setQuantity(quantity);
    t.setType(transType);
    t.setDomainId(domainId);
    t.setSourceUserId(userId);
    t.setTimestamp(now);
    t.setBatchId(null);
    t.setBatchExpiry(null);
    t.setReason(reason);
    return t;
  }

  @RequestMapping(value = "/order/reasons/{type}", method = RequestMethod.GET)
  public
  @ResponseBody
  List<String> getOrderReasons(@PathVariable String type, HttpServletRequest request) {
    SecureUserDetails user = SecurityMgr.getUserDetails(request.getSession());
    Long domainId = SessionMgr.getCurrentDomain(request.getSession(), user.getUsername());
    DomainConfig dc = DomainConfig.getInstance(domainId);
    OrdersConfig oc = dc.getOrdersConfig();
    String reasons = null;
    switch (type) {
      case "eqr":
        reasons = oc.getEditingQuantityReasons();
        break;
      case "orr":
        reasons = oc.getOrderRecommendationReasons();
        break;
      case "psr":
        reasons = oc.getPartialShipmentReasons();
        break;
      case "cor":
        reasons = oc.getCancellingOrderReasons();
        break;
      case "pfr":
        reasons = oc.getPartialFulfillmentReasons();
        break;
    }
    if (reasons != null && reasons.length() > 0) {
      return new ArrayList<>(Arrays.asList(reasons.split(CharacterConstants.COMMA)));
    }
    return null;
  }

  @RequestMapping(value = "/order/{orderId}/tags", method = RequestMethod.POST)
  public
  @ResponseBody
  OrderResponseModel updateOrderTags(@PathVariable Long orderId,
                                     @RequestBody OrderUpdateModel model,
                                     HttpServletRequest request) {
    List<String> tags = StringUtil.getList(model.updateValue, true);
    return updateOrder("tgs", orderId, model.orderUpdatedAt, null, null, null, null, tags, request);
  }

  @RequestMapping(value = "/order/{orderId}/referenceid", method = RequestMethod.POST)
  public
  @ResponseBody
  OrderResponseModel updateReferenceID(@PathVariable Long orderId,
                                       @RequestBody OrderUpdateModel model,
                                       HttpServletRequest request) {
    return updateOrder("rid", orderId, model.orderUpdatedAt, null, null, model.updateValue, null,
        null, request);
  }

  @RequestMapping(value = "/filter", method = RequestMethod.GET)
  public
  @ResponseBody
  List<String> getIdSuggestions(@RequestParam String id, @RequestParam(required = false) String type,
                                @RequestParam(required = false) Integer oty,
                                HttpServletRequest request) {
    List<String> rid;
    List<Long> kioskIds = null;
    try {
      SecureUserDetails user = SecurityUtils.getUserDetails(request);
      Long domainId = SessionMgr.getCurrentDomain(request.getSession(), user.getUsername());
      if (user.getUsername() != null) {
        // Get user
        if (SecurityConstants.ROLE_SERVICEMANAGER.equals(user.getRole())) {
          EntitiesService
              as =
              Services.getService(EntitiesServiceImpl.class, user.getLocale());
          kioskIds = as.getKioskIdsForUser(user.getUsername(), null, null).getResults();
          if (kioskIds == null || kioskIds.isEmpty()) {
            return new ArrayList<>(1);
          }
        }
      }
      OrderManagementService
          oms =
          Services.getService(OrderManagementServiceImpl.class, request.getLocale());
      rid = oms.getIdSuggestions(domainId, id, type, oty, kioskIds);
      return rid;
    } catch (Exception e) {
      xLogger
          .warn("Error in getting order id for suggestions starting with {0} of type {1}", id, type,
              e);
    }
    return null;
  }

}

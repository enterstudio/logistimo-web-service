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

package com.logistimo.api.servlets.mobile;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;

import com.logistimo.AppFactory;
import com.logistimo.api.servlets.JsonRestServlet;
import com.logistimo.api.servlets.mobile.builders.MobileOrderBuilder;
import com.logistimo.api.util.GsonUtil;
import com.logistimo.api.util.RESTUtil;
import com.logistimo.auth.SecurityConstants;
import com.logistimo.auth.SecurityUtil;
import com.logistimo.config.models.ApprovalsConfig;
import com.logistimo.config.models.DomainConfig;
import com.logistimo.constants.CharacterConstants;
import com.logistimo.constants.Constants;
import com.logistimo.constants.SourceConstants;
import com.logistimo.context.StaticApplicationContext;
import com.logistimo.entities.entity.IKiosk;
import com.logistimo.entities.service.EntitiesService;
import com.logistimo.entities.service.EntitiesServiceImpl;
import com.logistimo.entity.IJobStatus;
import com.logistimo.exception.InvalidDataException;
import com.logistimo.exception.InvalidServiceException;
import com.logistimo.exception.LogiException;
import com.logistimo.exception.UnauthorizedException;
import com.logistimo.exception.ValidationException;
import com.logistimo.inventory.entity.IInvAllocation;
import com.logistimo.inventory.entity.ITransaction;
import com.logistimo.inventory.exceptions.InventoryAllocationException;
import com.logistimo.inventory.service.InventoryManagementService;
import com.logistimo.inventory.service.impl.InventoryManagementServiceImpl;
import com.logistimo.logger.XLog;
import com.logistimo.materials.entity.IMaterial;
import com.logistimo.materials.service.MaterialCatalogService;
import com.logistimo.materials.service.impl.MaterialCatalogServiceImpl;
import com.logistimo.models.shipments.ShipmentItemBatchModel;
import com.logistimo.orders.OrderResults;
import com.logistimo.orders.OrderUtils;
import com.logistimo.orders.actions.GetFilteredOrdersAction;
import com.logistimo.orders.entity.IDemandItem;
import com.logistimo.orders.entity.IOrder;
import com.logistimo.orders.models.OrderFilters;
import com.logistimo.orders.models.UpdatedOrder;
import com.logistimo.orders.service.OrderManagementService;
import com.logistimo.orders.service.impl.OrderManagementServiceImpl;
import com.logistimo.pagination.PageParams;
import com.logistimo.pagination.Results;
import com.logistimo.proto.BatchRequest;
import com.logistimo.proto.JsonTagsZ;
import com.logistimo.proto.MaterialRequest;
import com.logistimo.proto.MobileOrderModel;
import com.logistimo.proto.MobileOrdersModel;
import com.logistimo.proto.RestConstantsZ;
import com.logistimo.proto.UpdateOrderRequest;
import com.logistimo.proto.UpdateOrderStatusRequest;
import com.logistimo.services.ObjectNotFoundException;
import com.logistimo.services.ServiceException;
import com.logistimo.services.Services;
import com.logistimo.services.cache.MemcacheService;
import com.logistimo.services.impl.PMF;
import com.logistimo.services.utils.ConfigUtil;
import com.logistimo.users.entity.IUserAccount;
import com.logistimo.utils.BigUtil;
import com.logistimo.utils.CommonUtils;
import com.logistimo.utils.LocalDateUtil;
import com.logistimo.utils.StringUtil;

import org.apache.commons.lang.StringUtils;

import java.io.IOException;
import java.math.BigDecimal;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

import javax.jdo.PersistenceManager;
import javax.jdo.Transaction;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@SuppressWarnings("serial")
public class OrderServlet extends JsonRestServlet {

  private static final XLog xLogger = XLog.getLog(OrderServlet.class);
  private static final String STARTDATE = "sd";
  private static final int CACHE_EXPIRY = ConfigUtil
      .getInt("cache.expiry.for.transaction.signature", 900); // 15 minutes

  // Check if kioskId is needed for updating order status via REST
  public static boolean isKioskIdOptionalForRESTStatusUpdate(String appVersion) {
    return RESTUtil
        .forceIntegerForStock(appVersion); // same conditions as forceInteger, so using that here
  }

  public void processGet(HttpServletRequest req, HttpServletResponse resp,
                         ResourceBundle backendMessages, ResourceBundle messages)
      throws IOException, ServiceException, ValidationException {
    xLogger.fine("OrderServlet: Entering doGet");
    String action = req.getParameter(RestConstantsZ.ACTION);
    if (RestConstantsZ.ACTION_GETORDERS.equals(action)) {
      if (RestConstantsZ.MINIMUM_RESP_VALUE.equalsIgnoreCase(req
          .getParameter(RestConstantsZ.MINIMUM_RESPONSE))) {
        getOrdersMinimumResponse(req, resp, backendMessages);
      } else {
        getOrders(req, resp, backendMessages, messages);
      }
    } else if (RestConstantsZ.ACTION_GETORDER.equals(action)) {
      getOrCancelOrder(req, resp, false, backendMessages, messages);
    } else if (RestConstantsZ.ACTION_CANCELORDER.equals(action)) {
      getOrCancelOrder(req, resp, true, backendMessages,
          messages); // TODO: NOTE: CancelOrder is retrained here for backward compatibility
    } else if (RestConstantsZ.ACTION_UPDATEORDER_OLD.equals(action)) {
      createOrUpdateOrder(req, resp, backendMessages, messages, true);
    } else if (RestConstantsZ.ACTION_UPDATEORDERSTATUS_OLD.equals(action)) {
      updateOrderStatusOld(req, resp, backendMessages, messages);
    } else if (RestConstantsZ.ACTION_EXPORT.equals(action)) {
      scheduleExport(req, resp, backendMessages, messages);
    } else if (RestConstantsZ.ACTION_UPDATEORDER.equals(action)) {
      createOrUpdateOrder(req, resp, backendMessages, messages, false);
    } else {
      throw new ServiceException("Invalid action: " + action);
    }
  }

  public void processPost(HttpServletRequest req, HttpServletResponse resp,
                          ResourceBundle backendMessages, ResourceBundle messages)
      throws IOException, ServiceException, ValidationException {
    String action = req.getParameter(RestConstantsZ.ACTION);
    if (RestConstantsZ.ACTION_UPDATEORDERSTATUS_OLD.equals(action)) {
      updateOrderStatus(req, resp, backendMessages, messages, true);
    } else if (RestConstantsZ.ACTION_UPDATEORDERSTATUS.equals(action)) {
      updateOrderStatus(req, resp, backendMessages, messages, false);
    } else {
      processGet(req, resp, backendMessages, messages);
    }
  }

  /**
   * Method to get the minimum details of orders for the order listing
   *
   * @param request         - HttpServletRequest
   * @param response        - HttpServletResponse
   * @param backendMessages - Error messages resource bundle
   */
  private void getOrdersMinimumResponse(HttpServletRequest request, HttpServletResponse response,
                                        ResourceBundle backendMessages) {
    // Get request parameters
    String userId = request.getParameter(RestConstantsZ.USER_ID);
    String kioskIdStr = request.getParameter(RestConstantsZ.KIOSK_ID);
    String status = request.getParameter(RestConstantsZ.STATUS);
    String size = request.getParameter(RestConstantsZ.SIZE);
    String otype = request.getParameter(RestConstantsZ.ORDER_TYPE);
    String transfers = request.getParameter(RestConstantsZ.TRANSFERS);
    String reqOffset = request.getParameter(RestConstantsZ.OFFSET);
    String message = null;
    Long kioskId = null;
    int statusCode = HttpServletResponse.SC_OK;
    JsonObject jsonObject = null;
    try {
      //Get kiosk from request
      if (StringUtils.isBlank(kioskIdStr)) {
        throw new ServiceException(backendMessages.getString("error.nokiosk"));
      }
      kioskId = Long.parseLong(kioskIdStr);
      //validate token and kiosk
      IUserAccount u = RESTUtil.authenticate(userId, null, kioskId, request, response);
      //Get the max results from request
      int maxResults = PageParams.DEFAULT_SIZE;
      if (StringUtils.isNotBlank(size)) {
        try {
          maxResults = Integer.parseInt(size);
        } catch (NumberFormatException e) {
          xLogger.warn("Invalid max orders received. Reset to default size", size, e.getMessage());
        }
      }
      //Get the offset from request
      int offset = 0;
      if (StringUtils.isNotBlank(reqOffset)) {
        try {
          offset = Integer.parseInt(reqOffset);
        } catch (NumberFormatException e) {
          xLogger.warn("Invalid offset received. Reset to 0", reqOffset, e.getMessage());
        }
      }
      PageParams pageParams = new PageParams(offset, maxResults);

      // If order type is invalid or not sales, it is set to purchase
      if (!OrderUtils.isValidOrderType(otype)) {
        otype = IOrder.TYPE_PURCHASE;
      }
      //Based on whether transfer is requested or not, set the flag
      boolean isTransfer = OrderUtils.isTransfer(transfers);

      //Get orders for specified entity, status, order type and transfers
      List<IOrder> ordersList = StaticApplicationContext.getBean(GetFilteredOrdersAction.class)
          .invoke(new OrderFilters()
                  .setKioskId(kioskId)
                  .setStatus(status)
                  .setOtype(otype)
                  .setOrderType(isTransfer ? IOrder.TRANSFER : IOrder.NONTRANSFER)
              , pageParams
          ).getResults();
      if (ordersList == null || ordersList.isEmpty()) {
        message = backendMessages.getString("error.noorders");
      } else {

        //Get the order approval type
        int orderApprovalType = OrderUtils.getOrderApprovalType(otype, isTransfer);

        //Build json response
        jsonObject =
            new MobileOrderBuilder()
                .buildOrdersResponse(ordersList, u.getLocale(), u.getTimezone(), orderApprovalType);
      }
    } catch (NumberFormatException e) {
      xLogger.severe(" Exception when getting kiosk", e);
      message = backendMessages.getString("error.nokiosk");
    } catch (UnauthorizedException e) {
      xLogger.warn(" User unauthorized", e);
      statusCode = HttpServletResponse.SC_UNAUTHORIZED;
      message = e.getMessage();
    } catch (ServiceException e) {
      xLogger.severe(" Service Exception", e);
      message = e.getMessage();
    } catch (Exception e) {
      xLogger.severe(" Exception when fetching orders for kiosk {0}: {1}", kioskId, e);
      message = backendMessages.getString("error.systemerror");
    }
    try {
      String resp = GsonUtil.buildResponse(jsonObject, message, RESTUtil.VERSION_01);
      sendJsonResponse(response, statusCode, resp);
    } catch (Exception e1) {
      xLogger.severe("Protocol exception when sending orders for kiosk {0}: {1}", kioskId,
          e1);
      response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
    }

  }

  // Get orders for a given kiosk
  @SuppressWarnings({"rawtypes", "unchecked"})
  private void getOrders(HttpServletRequest req, HttpServletResponse resp,
                         ResourceBundle backendMessages, ResourceBundle messages)
      throws IOException {
    xLogger.fine("Entered getOrders");
    // Get request parameters
    String userId = req.getParameter(RestConstantsZ.USER_ID);
    String kioskIdStr = req.getParameter(RestConstantsZ.KIOSK_ID);
    String statusStr = req.getParameter(RestConstantsZ.STATUS);
    String maxOrders = req.getParameter(RestConstantsZ.NUM_RESULTS);
    String
        loadAllStr =
        req.getParameter(RestConstantsZ.LOADALL); // load order items along with order metadata
    String password = req.getParameter(RestConstantsZ.PASSWORD); // sent when SMS message is sent
    String
        exShpItemsStr =
        req.getParameter(
            RestConstantsZ.EXCLUDE_SHIPMENT_ITEMS); // exclude shipment items from shipments along with order metadata
    boolean
        incShpItems =
        (exShpItemsStr == null || exShpItemsStr.isEmpty()) ? true
            : exShpItemsStr.equals("1") ? false : true;

    // Get the order type, if sent
    String otype = req.getParameter(RestConstantsZ.ORDER_TYPE);
    if (otype == null || otype.isEmpty()) {
      otype = IOrder.TYPE_PURCHASE; // defaulted to purchase orders
    }
    String transfers = req.getParameter("transfers");
    int
        orderType =
        IOrder.NONTRANSFER; // defaulted to non transfer. Can be non transfer or transfer.
    if (transfers != null && !transfers.isEmpty() && Integer.valueOf(transfers) == 1) {
      orderType = IOrder.TRANSFER; // Only if transfers = 1, then orderType is set to transfer
    }
    // Start date and end date parameters
    String startDateStr = req.getParameter(STARTDATE);
    String endDateStr = req.getParameter(RestConstantsZ.ENDDATE);

    // Init.
    Long domainId = null;
    Long kioskId = null;
    boolean status = true;
    boolean hasOrders = true;
    boolean loadAll = false;
    String message = null;
    String currency = null;
    Locale locale = null;
    String timezone = null;
    String appVersion = null;
    boolean isUserAdminOrGreater = false;
    int statusCode = HttpServletResponse.SC_OK;
    // Get domain Id
    // Authenticate or check if authenticated
    try {
      if (kioskIdStr != null && !kioskIdStr.isEmpty()) {
        try {
          kioskId = Long.valueOf(kioskIdStr);
        } catch (NumberFormatException e) {
          xLogger.warn("Invalid kiosk Id {0}: {1}", kioskIdStr, e.getMessage());
        }
      }
      // Get the user details
      IUserAccount u = RESTUtil.authenticate(userId, password, kioskId, req, resp);
      domainId = u.getDomainId();
      locale = u.getLocale();
      timezone = u.getTimezone();
      appVersion = u.getAppVersion();
      isUserAdminOrGreater =
          (SecurityUtil.compareRoles(u.getRole(), SecurityConstants.ROLE_DOMAINOWNER) >= 0);
      if (kioskId == null
          && !isUserAdminOrGreater) { // kiosk ID is mandatory, and user should have authorization on it (either domain owner, or a operator/manager of it)
        status = false;
        message = backendMessages.getString("error.nokiosk");
      }
    } catch (ServiceException e) {
      status = false;
      message = e.getMessage();
    } catch (NumberFormatException e) {
      status = false;
      message = "Invalid kiosk identifier";
    } catch (UnauthorizedException e) {
      message = e.getMessage();
      status = false;
      statusCode = HttpServletResponse.SC_UNAUTHORIZED;
    }
    List<IOrder> orders = null;
    boolean includeBatchDetails = true;
    if (status) {
      try {
        // Init. service
        OrderManagementService
            oms =
            Services.getService(OrderManagementServiceImpl.class, locale);
        // Get max. orders, if any
        int maxResults = PageParams.DEFAULT_SIZE;
        if (maxOrders != null && !maxOrders.isEmpty()) {
          maxResults = Integer.parseInt(maxOrders);
        }
        loadAll = JsonTagsZ.STATUS_TRUE.equals(loadAllStr);
        // Get kiosk Id
        if (kioskIdStr != null && !kioskIdStr.isEmpty()) {
          kioskId = Long.valueOf(kioskIdStr);
        }
        if (statusStr != null && !statusStr.isEmpty()) {
          statusStr = null;
        }
        // Get the start date
        Date startDate = null;
        if (startDateStr != null && !startDateStr.isEmpty()) {
          if (!isOfDateTimeFormat(startDateStr)) {
            startDateStr += " 00:00:00";
          }
          try {
            startDate = LocalDateUtil.parseCustom(startDateStr, Constants.DATETIME_FORMAT, null);
          } catch (ParseException e) {
            xLogger.warn("{0} while parsing start date. Invalid format. {1}, {2}. ",
                e.getClass().getName(), startDateStr, e.getMessage());
          }
        }
        // Get the end date
        Date endDate = null;
        if (endDateStr != null && !endDateStr.isEmpty()) {
          if (!isOfDateTimeFormat(endDateStr)) {
            endDateStr += " 00:00:00";
          }
          try {
            endDate = LocalDateUtil.parseCustom(endDateStr, Constants.DATETIME_FORMAT, null);
          } catch (ParseException e) {
            xLogger.warn("{0} while parsing end date. Invalid format. {1}, {2}. ",
                e.getClass().getName(), endDateStr, e.getMessage());
          }
        }
        String offsetStr = req.getParameter(Constants.OFFSET);
        int offset = 0;
        if (StringUtils.isNotBlank(offsetStr)) {
          try {
            offset = Integer.parseInt(offsetStr);
          } catch (Exception e) {
            xLogger.warn("Invalid offset {0}: {1}", offsetStr, e.getMessage());
          }
        }
        PageParams pageParams = new PageParams(offset, maxResults);
        // Get the currency of the kiosk
        if (kioskId != null) {
          EntitiesService as = Services.getService(EntitiesServiceImpl.class, locale);
          IKiosk k = as.getKiosk(kioskId, false);
          currency = k.getCurrency();
          includeBatchDetails = k.isBatchMgmtEnabled();
        }
        // Get the configuration for orders, for this domain
        DomainConfig dc = DomainConfig.getInstance(domainId);
        hasOrders = dc.autoOrderGeneration();
        if (currency == null || currency.isEmpty()) {
          currency = dc.getCurrency();
        }
        if (hasOrders) {
          Results
              res =
              oms.getOrders(domainId, kioskId, statusStr, startDate, endDate, otype, null, null,
                  null, pageParams, orderType, null, null, loadAll);
          if (res != null) {
            orders = res.getResults();
          }
        }
      } catch (NumberFormatException e) {
        xLogger.severe("Number format exception when getting orders for kiosk {0}: {1}", kioskId,
            e.getMessage());
        status = false;
        message = backendMessages.getString("error.systemerror");
      } catch (ServiceException e) {
        xLogger.severe("Service exception when getting orders for kiosk {0}: {1}", kioskId,
            e.getMessage());
        status = false;
        message =
            backendMessages.getString("error.noorders")
                + " [2]"; // [2] is marker for error position in code
      }
    }

    MobileOrdersModel mom = null;
    if (status) {
      if (orders == null || orders.isEmpty()) {
        status = false;
        message = backendMessages.getString("error.noorders");
      } else {
        MobileOrderBuilder mob = new MobileOrderBuilder();
        mom = mob.buildOrders(orders, locale, timezone, loadAll, incShpItems, includeBatchDetails);
      }
    }

    // For the JSON output and send
    try {
      String
          jsonOutput =
          GsonUtil.buildGetOrdersResponseModel(status, mom, message, RESTUtil.VERSION_01);
      sendJsonResponse(resp, statusCode, jsonOutput);
    } catch (Exception e) {
      xLogger.severe("Protocol exception when sending orders for kiosk {0}: {1}", kioskId,
          e.getMessage());
      status = false;
      message = backendMessages.getString("error.systemerror") + " [2]";
      try {
        String
            jsonOutput =
            GsonUtil.buildGetOrdersResponseModel(status, mom, message, RESTUtil.VERSION_01);
        sendJsonResponse(resp, statusCode, jsonOutput);
      } catch (Exception e1) {
        xLogger.severe("Protocol exception when sending orders for kiosk {0}: {1}", kioskId,
            e1.getMessage());
        resp.setStatus(500);
      }
    }
    xLogger.fine("Exiting getOrders");
  }


  // Get order given an order Id for a given kiosk
  @SuppressWarnings("rawtypes")
  private void getOrCancelOrder(HttpServletRequest req, HttpServletResponse resp,
                                boolean cancelOrder, ResourceBundle backendMessages,
                                ResourceBundle messages) throws IOException {
    xLogger.fine("Entered getOrCancelOrder");
    // Get request parameters
    String userId = req.getParameter(RestConstantsZ.USER_ID);
    String oidStr = req.getParameter(RestConstantsZ.ORDER_ID);
    String password = req.getParameter(RestConstantsZ.PASSWORD);
    String kioskIdStr = req.getParameter(RestConstantsZ.KIOSK_ID);
    String
        exShpItemsStr =
        req.getParameter(
            RestConstantsZ.EXCLUDE_SHIPMENT_ITEMS); // exclude shipment items from shipments along with order metadata
    boolean
        incShpItems =
        (exShpItemsStr == null || exShpItemsStr.isEmpty()) ? true
            : exShpItemsStr.equals("1") ? false : true;
    // Init.
    Long orderId = null;
    boolean status = true;
    String message = null;
    Locale locale = null;
    String timezone = null;
    String appVersion = null;
    Long kioskId = null;
    int statusCode = HttpServletResponse.SC_OK;
    // Get domain Id
    // Authenticate or check authentication
    try {
      if (kioskIdStr != null && !kioskIdStr.isEmpty()) {
        try {
          kioskId = Long.valueOf(kioskIdStr);
        } catch (NumberFormatException e) {
          xLogger.warn("Invalid kiosk Id {0}: {1}", kioskIdStr, e.getMessage());
        }
      }
      // TODO: Checking appVersion for backward compatibility before app. version 2.0.0
      if (!isKioskIdOptionalForRESTStatusUpdate(appVersion) && kioskId
          == null) { // kiosk ID is mandatory, and user should have authorization on it (either domain owner, or a operator/manager of it)
        status = false;
        message = backendMessages.getString("error.nokiosk");
      } else {
        IUserAccount u = RESTUtil.authenticate(userId, password, kioskId, req, resp);
        timezone = u.getTimezone();
        locale = u.getLocale();
        appVersion = u.getAppVersion();
      }
    } catch (UnauthorizedException e) {
      message = e.getMessage();
      status = false;
      statusCode = HttpServletResponse.SC_UNAUTHORIZED;
    } catch (ServiceException e) {
      status = false;
      message = e.getMessage();
    }

    // Order
    IOrder order = null;
    if (status) {
      String signature = null;
      MemcacheService cache = null;
      try {
        if (oidStr != null && !oidStr.isEmpty()) {
          orderId = Long.valueOf(oidStr);
        }
        if (orderId == null) {
          status = false;
          message = "Order ID not specified";
        } else {
          // Init. service
          OrderManagementService
              oms =
              Services.getService(OrderManagementServiceImpl.class, locale);
          // Get the orders
          order = oms.getOrder(orderId, true);
          // Check if order has to be cancelled
          if (cancelOrder) {
            signature = CommonUtils
                .getMD5(
                    "CancelMobile" + userId + kioskIdStr + oidStr + exShpItemsStr + incShpItems);
            cache = AppFactory.get().getMemcacheService();
            Integer lastStatus = (Integer) cache.get(signature);
            boolean isAlreadyProcessed = false;
            if (lastStatus != null) {
              switch (lastStatus) {
                case IJobStatus.COMPLETED:
                  isAlreadyProcessed = true;
                  break;
                case IJobStatus.INPROGRESS:
                  sendPendingResponse(resp);
                  return;
                case IJobStatus.FAILED:
                  setSignatureAndStatus(cache, signature, IJobStatus.INPROGRESS);
              }
            }
            if (!isAlreadyProcessed) {
              if (IOrder.CANCELLED.equals(order.getStatus())) {
                status = false;
                message =
                    "Order " + orderId + " is already cancelled.";
              } else {
                order.setStatus(IOrder.CANCELLED);
                oms.updateOrder(order, SourceConstants.MOBILE);
              }
              setSignatureAndStatus(cache, signature,
                  IJobStatus.COMPLETED);
            } else {
              order = getOrder(orderId);
            }


          }
        }
      } catch (NumberFormatException e) {
        xLogger.severe("Number format exception when getting order Id: {0}", orderId, e);
        status = false;
        message = backendMessages.getString("error.systemerror") + " [1]";
        setSignatureAndStatus(cache, signature, IJobStatus.FAILED);
      } catch (ObjectNotFoundException e) {
        xLogger.severe("Object not found with order ID: {0}", orderId, e);
        status = false;
        message =
            messages.getString("order") + " " + orderId + " " + backendMessages
                .getString("error.notfound");
        setSignatureAndStatus(cache, signature, IJobStatus.FAILED);
      } catch (LogiException e) {
        xLogger.severe("Service exception when getting order with ID: {0}", orderId, e);
        status = false;
        message = backendMessages.getString("error.systemerror");
        setSignatureAndStatus(cache, signature, IJobStatus.FAILED);
      }
    }
    // For the JSON output and send
    try {
      MobileOrderModel mom = null;
      if (order != null) {
        DomainConfig dc = DomainConfig.getInstance(order.getDomainId());
        boolean isAccounting = dc.isAccountingEnabled();
        MobileOrderBuilder mob = new MobileOrderBuilder();

        boolean isBatchEnabled;
        EntitiesService entitiesService = Services.getService(EntitiesServiceImpl.class);
        if(kioskId != null){
          IKiosk k = entitiesService.getKiosk(kioskId, false);
          isBatchEnabled=k.isBatchMgmtEnabled();
        } else {
          IKiosk k = entitiesService.getKiosk(order.getKioskId(), false);
          isBatchEnabled=k.isBatchMgmtEnabled();
        }

        mom =
            mob.build(order, locale, timezone, true, isAccounting, incShpItems,isBatchEnabled);
      }
      String
          jsonOutput =
          GsonUtil.buildGetOrderResponseModel(status, mom, message, RESTUtil.VERSION_01);
      sendJsonResponse(resp, statusCode, jsonOutput);
    } catch (Exception e) {
      xLogger.severe("Protocol exception when sending order with ID {0}: {1}", orderId,
          e.getMessage());
      status = false;
      message = backendMessages.getString("error.systemerror");
      try {
        String
            jsonOutput =
            GsonUtil.buildGetOrderResponseModel(status, null, message, RESTUtil.VERSION_01);
        sendJsonResponse(resp, statusCode, jsonOutput);
      } catch (Exception e1) {
        xLogger.severe("Protocol exception when sending order {0}: {1}", orderId, e1.getMessage());
        resp.setStatus(500);
      }
    }

    xLogger.fine("Exiting getOrCancelOrder");
  }

  // Create or update a given order
  @SuppressWarnings({"rawtypes", "unchecked"})
  private void createOrUpdateOrder(HttpServletRequest req, HttpServletResponse resp,
                                   ResourceBundle backendMessages, ResourceBundle messages,
                                   boolean isOldRequest)
      throws IOException {
    xLogger.fine("Entered createOrUpdateOrder");
    // Get the type
    String type = req.getParameter(RestConstantsZ.TRANS_TYPE);
    // Get JSON input
    String jsonInput = req.getParameter(RestConstantsZ.JSON_STRING);
    // Get password (in case of SMS)
    String password = req.getParameter(RestConstantsZ.PASSWORD);
    String errorCode = null;
    OrderResults or = null;
    MobileOrderModel mom = null;
    IOrder o = null;
    String message = null;
    boolean status = true;
    Locale locale = null;
    String timezone = null;
    String appVersion = null;
    Date now = new Date();
    int statusCode = HttpServletResponse.SC_OK;
    boolean includeBatchDetails = true;
    // Input error checks
    if (type == null || type.isEmpty() || jsonInput == null || jsonInput.isEmpty()) {
      message = "Invalid input parameters";
      status = false;
    } else {
      UpdateOrderRequest uoReq = GsonUtil.updateOrderInputFromJson(jsonInput);

      try {
        List<String> orderTags = StringUtil.getList(uoReq.tg);
        // Get the domain Id
        Long domainId = null;
        try {
          // kiosk ID is mandatory, and user should have authorization on it
          // (either domain owner, or a operator/manager of it)
          if (uoReq.kid == null) {
            status = false;
            message = backendMessages.getString("error.nokiosk");
          } else {
            IUserAccount u = null;
            if (uoReq.oty.equals(IOrder.TYPE_SALE)) {
              u =
                  RESTUtil
                      .authenticate(uoReq.uid, password, uoReq.lkid, req, resp);
            } else {
              u =
                  RESTUtil
                      .authenticate(uoReq.uid, password, uoReq.kid, req, resp);
            }
            domainId = u.getDomainId();
            locale = u.getLocale();
            timezone = u.getTimezone();
            appVersion = u.getAppVersion();
          }
        } catch (ServiceException e) {
          status = false;
          message = e.getMessage();
        } catch (NumberFormatException e) {
          status = false;
          message = "Invalid kiosk identifier";
        } catch (UnauthorizedException e) {
          message = e.getMessage();
          status = false;
          statusCode = HttpServletResponse.SC_UNAUTHORIZED;
        }
        DomainConfig dc = null;
        if (status) {
          // Get config.
          dc = DomainConfig.getInstance(domainId);
          if (isOldRequest && isApprovalConfigEnabled(dc.getApprovalsConfig())) {
            status = false;
            message =
                backendMessages.getString("upgrade.app.message");
          }
        }
        if (status) {
          Boolean isSalesOrder = false;
          int tOrNt = IOrder.PURCHASE_ORDER;
          if (uoReq.oty != null && uoReq.oty.equals(IOrder.TYPE_SALE)) {
            isSalesOrder = true;
            tOrNt = IOrder.SALES_ORDER;
          }
          if (!(isOldRequest && RestConstantsZ.TYPE_ORDER.equals(type)) && uoReq.trf != null
              && uoReq.trf.equals(IOrder.TRANSFER)) {
            tOrNt = uoReq.trf;
          }
          // Determine if orders are to be created automatically, or whether to allow empty orders, etc.
          boolean createOrder = dc.autoOrderGeneration();
          boolean allowEmptyOrders = dc.allowEmptyOrders();
          // Get the transaction list from the input
          List<ITransaction> list = null;
          try {
            list = RESTUtil.getInventoryTransactions(uoReq, type, now, locale);
            if (!allowEmptyOrders && list == null && ITransaction.TYPE_ORDER
                .equals(type)) {
              xLogger.severe("New order attempted without items in domain {0}",
                  domainId);
              status = false;
              message = backendMessages.getString("error.unabletoupdateorder");
            }
          } catch (ServiceException e) {
            xLogger.severe("OrderServlet Exception: {0}", e.getMessage());
            message = backendMessages.getString("error.unabletoupdateorder");
            status = false;
          }

          String signature = null;
          MemcacheService
              cache = null;
          boolean isAlreadyProcessed = false;
          OrderManagementService
              oms =
              Services.getService(OrderManagementServiceImpl.class,
                  locale);
          if (RestConstantsZ.TYPE_REORDER.equalsIgnoreCase(type)) {
            o = oms.getOrder(uoReq.tid);
            if (!OrderUtils.validateOrderUpdatedTime(uoReq.tm, o.getUpdatedOn())) {
              errorCode = "O004";
              status = false;
              mom = buildOrderModel(timezone, o, locale, includeBatchDetails);
            }

          }
          if (status) {
            if (RestConstantsZ.TYPE_REORDER.equals(type)) {
              tOrNt = o.getOrderType();
              signature = CommonUtils.getMD5(jsonInput);
              cache = AppFactory.get().getMemcacheService();
              Integer lastStatus = (Integer) cache.get(signature);
              if (lastStatus != null) {
                switch (lastStatus) {
                  case IJobStatus.COMPLETED:
                    isAlreadyProcessed = true;
                    break;
                  case IJobStatus.INPROGRESS:
                    sendPendingResponse(resp);
                    return;
                  case IJobStatus.FAILED:
                    setSignatureAndStatus(cache, signature,
                        IJobStatus.INPROGRESS);
                }
              }
            }
            if (!isAlreadyProcessed) {
              // Get the IMS service
              InventoryManagementService
                  ims =
                  Services.getService(InventoryManagementServiceImpl.class,
                      locale);
              MaterialCatalogService
                  mcs =
                  Services
                      .getService(MaterialCatalogServiceImpl.class, locale);
              EntitiesService
                  as =
                  Services.getService(EntitiesServiceImpl.class, locale);

              PersistenceManager pm = null;

              Transaction tx = null;
              try {
                pm = PMF.get().getPersistenceManager();
                tx = pm.currentTransaction();
                tx.begin();
                // Update order transactions
                or =
                    oms.updateOrderTransactions(domainId, uoReq.uid, type, list,
                        uoReq.kid, uoReq.tid,
                        uoReq.ms, createOrder, uoReq.lkid, uoReq.lat, uoReq.lng,
                        uoReq.gacc, uoReq.gerr,
                        uoReq.efts, uoReq.cft, uoReq.pymt, uoReq.popt, uoReq.pksz,
                        allowEmptyOrders, orderTags,
                        tOrNt, isSalesOrder, uoReq.rid, uoReq.rbd, uoReq.eta,
                        SourceConstants.MOBILE, pm);
                // Check if order status has to be updated to a newer one
                if (or != null) {
                  o = or.getOrder();
                }
                if (uoReq.oty.equals(IOrder.TYPE_PURCHASE)) {
                  IKiosk k = as.getKiosk(uoReq.kid, false);
                  includeBatchDetails = k.isBatchMgmtEnabled();
                } else if (uoReq.oty.equals(IOrder.TYPE_SALE)) {
                  IKiosk k = as.getKiosk(uoReq.lkid, false);
                  includeBatchDetails = k.isBatchMgmtEnabled();
                }

                if (type.equals(ITransaction.TYPE_REORDER)) {
                  //Allocate quantities, this should be managed by OMS ideally.
                  String
                      tag =
                      IInvAllocation.Type.ORDER.toString()
                          + CharacterConstants.COLON + o.getOrderId();
                  if (dc.autoGI() && o.getServicingKiosk() != null) {
                    if (uoReq.mt != null) {
                      for (MaterialRequest item : uoReq.mt) {
                        List<IInvAllocation>
                            invAllocations =
                            ims.getAllocationsByTag(tag);

                        BigDecimal totalInvAllocation = BigDecimal.ZERO;
                        for (IInvAllocation invAllocation : invAllocations) {
                          if (IInvAllocation.Type.SHIPMENT.toString()
                              .equals(invAllocation.getType())) {
                            totalInvAllocation =
                                totalInvAllocation
                                    .add(invAllocation.getQuantity());
                          }
                        }
                        IDemandItem demandItem = o.getItem(item.mid);
                        if (item.alq != null && BigUtil.greaterThan(item.alq,
                            demandItem.getQuantity().subtract(
                                demandItem.getShippedQuantity()
                                    .add(totalInvAllocation)))) {
                          throw new ServiceException(
                              "Allocated Quantity cannot be greater than "
                                  + "Total Quantity");
                        }
                        List<ShipmentItemBatchModel> batchDetails = null;
                        IMaterial material = mcs.getMaterial(item.mid);
                        IKiosk kiosk = as.getKiosk(o.getServicingKiosk(), false);
                        item.isBa =
                            material.isBatchEnabled() && kiosk
                                .isBatchMgmtEnabled();
                        if (item.isBa) {
                          item.alq = null;
                          if (item.bt != null) {
                            batchDetails = new ArrayList<>(item.bt.size());
                            for (BatchRequest bt : item.bt) {
                              ShipmentItemBatchModel
                                  details =
                                  new ShipmentItemBatchModel();
                              details.id = bt.bid;
                              details.q = bt.alq;
                              details.smst = bt.mst;
                              batchDetails.add(details);
                            }
                          }
                        }

                        if (item.alq != null || batchDetails != null) {
                          ims.allocate(o.getServicingKiosk(), item.mid,
                              IInvAllocation.Type.ORDER,
                              String.valueOf(o.getOrderId()), tag, item.alq,
                              batchDetails, uoReq.uid, pm,
                              item.mst);
                        } else {
                          ims.clearAllocation(o.getServicingKiosk(), item.mid,
                              IInvAllocation.Type.ORDER,
                              String.valueOf(o.getOrderId()), pm);
                        }
                      }
                    }
                  }
                }
                tx.commit();
              } finally {
                if (tx != null && tx.isActive()) {
                  tx.rollback();
                  setSignatureAndStatus(cache, signature, IJobStatus.FAILED);
                } else {
                  setSignatureAndStatus(cache, signature, IJobStatus.COMPLETED);
                }
                if (pm != null) {
                  pm.close();
                }
              }
            } else {
              o = getOrder(uoReq.tid);
            }
          }
        }
      } catch (InventoryAllocationException e) {
        xLogger
            .severe("InventoryAllocationException while updating order id :{0}",
                uoReq.tid, e);
        status = false;
        message = e.getMessage();
      } catch (ServiceException e2) {
        xLogger.severe("OrderServlet Exception: {0}", uoReq.tid, e2);
        message = backendMessages.getString("error.unabletoupdateorder");
        status = false;
      } catch (NumberFormatException e) {
        xLogger.warn("Invalid number passed during inventory update: {0}",
            uoReq.tid, e);
        message = backendMessages.getString("error.systemerror");
        status = false;
      } catch (InvalidDataException ide) {
        xLogger.warn(backendMessages.getString("error.transferfailed") + " " +
            backendMessages.getString("affectedmaterials") + ": " + ide);
        status = false;
        message = backendMessages.getString("error.systemerror");
      } catch (Exception e2) {
        xLogger.severe("Exception: {0} : {1}", e2.getClass().getName(), e2);
        message = backendMessages.getString("error.systemerror") + " [2]";
        status = false;
      }
    }

    // Send the order output as response
    String jsonString = null;

    if (status) {
      mom = buildOrderModel(timezone, o, locale, includeBatchDetails);

      // Get JSON output
      jsonString = buildJsonResponse(resp, status, mom, message, errorCode);
    }
    // If errors, try and get the error message for sending back
    if (!status) {
      jsonString = buildJsonResponse(resp, status, mom, message, errorCode);
    }

    // Send back response
    if (jsonString != null) {
      sendJsonResponse(resp, statusCode, jsonString);
    } else {
      resp.setStatus(500);
    }

    xLogger.fine("Exiting createOrUpdateOrder");
  }

  /**
   * Builds a json from the order model
   *
   * @param response         http response
   * @param status           error status
   * @param mobileOrderModel order model
   * @param message          error message
   * @param errorCode        error code
   * @return Json string
   */
  private String buildJsonResponse(HttpServletResponse response, boolean status,
                                   MobileOrderModel mobileOrderModel, String message,
                                   String errorCode) {
    try {
      return
          GsonUtil
              .buildOrderJson(status, mobileOrderModel, message, RESTUtil.VERSION_01, errorCode);
    } catch (Exception e1) {
      xLogger.severe("Protocol exception when getting error response", e1);
      if (mobileOrderModel != null) {
        buildJsonResponse(response, status, null, message, errorCode);
      }
      response.setStatus(500);
    }
    return null;
  }

  /**
   * Builds a order model from the order object
   *
   * @param timezone user's timezone
   * @param order    Order
   * @param locale   user's locale
   * @return MobileOrderModel order model
   */
  private MobileOrderModel buildOrderModel(String timezone, IOrder order, Locale locale,
                                           boolean includeBatch) {
    if (order == null) {
      return null;
    }
    DomainConfig dc = DomainConfig.getInstance(order.getDomainId());
    boolean isAccounting = dc.isAccountingEnabled();
    return new MobileOrderBuilder()
        .build(order, locale, timezone, true, isAccounting, true, includeBatch);
  }


  // Update status of an order - DEPRECATED
  @SuppressWarnings("rawtypes")
  private void updateOrderStatusOld(HttpServletRequest req, HttpServletResponse resp,
                                    ResourceBundle backendMessages, ResourceBundle messages)
      throws IOException, ValidationException {
    xLogger.fine("Entered updateOrderStatusOld");
    // Get request parameters
    String userId = req.getParameter(RestConstantsZ.USER_ID);
    String oidStr = req.getParameter(RestConstantsZ.ORDER_ID);
    String password = req.getParameter(RestConstantsZ.PASSWORD);
    String newStatus = req.getParameter(RestConstantsZ.STATUS);
    String
        kioskIdStr =
        req.getParameter(
            RestConstantsZ.KIOSK_ID); // Reading since mob. ver. 2.0.0, but seems like this was being passed in earlier versions as well, but not used here
    // Init.
    Long orderId = null;
    boolean status = true;
    String message = null;
    Locale locale = null;
    String timezone = null;
    String appVersion = null;
    DomainConfig dc = null;
    Long kioskId = null;
    int statusCode = HttpServletResponse.SC_OK;
    // Authenticate or check authentication
    try {
      if (kioskIdStr != null && !kioskIdStr.isEmpty()) {
        try {
          kioskId = Long.valueOf(kioskIdStr);
        } catch (NumberFormatException e) {
          xLogger.warn("Invalid kiosk Id {0}: {1}", kioskIdStr, e.getMessage());
        }
      }
      if (kioskId
          == null) { // kiosk ID is mandatory, and user should have authorization on it (either domain owner, or a operator/manager of it)
        status = false;
        message = backendMessages.getString("error.nokiosk");
      } else {
        IUserAccount u = RESTUtil.authenticate(userId, password, kioskId, req, resp);
        if (userId == null) // possible if BasicAuth was used
        {
          userId = u.getUserId();
        }
        timezone = u.getTimezone();
        locale = u.getLocale();
        appVersion = u.getAppVersion();
        // Get domain config
        dc =
            DomainConfig.getInstance(
                u.getDomainId()); // SessionMgr.getDomainConfig( req.getSession(), u.getDomainId() ); -- session may not have it, given offline usage on mobile
      }
    } catch (ServiceException e) {
      status = false;
      message = e.getMessage();
    } catch (UnauthorizedException e) {
      message = e.getMessage();
      status = false;
      statusCode = HttpServletResponse.SC_UNAUTHORIZED;
    }
    // Update order status
    IOrder order = null;
    if (status) {
      String signature = null;
      MemcacheService cache = null;
      try {
        if (oidStr != null && !oidStr.isEmpty()) {
          orderId = Long.valueOf(oidStr);
        }
        if (orderId == null) {
          status = false;
          message = "Order ID not specified";
        } else {
          signature = CommonUtils.getMD5("CancelMobile" + userId + kioskIdStr + oidStr + newStatus);
          cache = AppFactory.get().getMemcacheService();
          Integer lastStatus = (Integer) cache.get(signature);
          boolean isAlreadyProcessed = false;
          if (lastStatus != null) {
            switch (lastStatus) {
              case IJobStatus.COMPLETED:
                isAlreadyProcessed = true;
                break;
              case IJobStatus.INPROGRESS:
                sendPendingResponse(resp);
                return;
              case IJobStatus.FAILED:
                setSignatureAndStatus(cache, signature, IJobStatus.INPROGRESS);
            }
          }
          if (!isAlreadyProcessed) {
            // Update the status of the order
            if (newStatus == null || newStatus.isEmpty()) {
              status = false;
              message = "Invalid status specified";
              setSignatureAndStatus(cache, signature, IJobStatus.FAILED);
            } else {
              // Update order status
              UpdatedOrder uo = OrderUtils.updateOrderStatus(
                  orderId, newStatus, userId, null, dc,
                  SourceConstants.MOBILE, backendMessages);
              order = uo.order;
              // Send inventory error message, if any
              if (uo.inventoryError) {
                xLogger.warn(
                    "Error when auto-posting inventory transactiosn when updating order {0}: {1}",
                    orderId, uo.message);
                status = false;
                message = uo.message;
              }
              setSignatureAndStatus(cache, signature, IJobStatus.COMPLETED);
            }
          } else {
            order = getOrder(orderId);
          }
        }
      } catch (NumberFormatException e) {
        xLogger.severe("Number format exception when getting order Id: {1}", e.getMessage());
        status = false;
        message = "Invalid order identifier. It has to be a number";
        setSignatureAndStatus(cache, signature, IJobStatus.FAILED);
      } catch (ObjectNotFoundException e) {
        xLogger.severe("Object not found with order ID {0}: {1}", orderId, e.getMessage());
        status = false;
        message = messages.getString("order") + " " + orderId + " " + backendMessages.getString(
            "error.notfound");
        setSignatureAndStatus(cache, signature, IJobStatus.FAILED);
      } catch (ServiceException e) {
        xLogger.severe("Service exception when getting order with ID {0}: {1}", orderId,
            e.getMessage());
        status = false;
        message = backendMessages.getString("error.systemerror");
        setSignatureAndStatus(cache, signature, IJobStatus.FAILED);
      }
    }
    // Get the locale string
    String localeStr = Constants.LANG_DEFAULT;
    if (locale != null) {
      localeStr = locale.toString();
    }
    // For the JSON output and send
    try {
      MobileOrderModel mom = null;
      if (order != null) {
        dc = DomainConfig.getInstance(order.getDomainId());
        boolean isAccounting = dc.isAccountingEnabled();
        MobileOrderBuilder mob = new MobileOrderBuilder();
        EntitiesService as = Services.getService(EntitiesServiceImpl.class);
        IKiosk k = as.getKiosk(order.getKioskId(), false);
        mom = mob.build(order, locale, timezone, true, isAccounting, true, k.isBatchMgmtEnabled());
      }

      String json = GsonUtil.buildGetOrderResponseModel(status, mom, message, RESTUtil.VERSION_01);
      sendJsonResponse(resp, statusCode, json);

    } catch (Exception e) {
      xLogger.severe("Protocol exception when sending order with ID {0}: {1}", orderId,
          e.getMessage());
      status = false;
      message = backendMessages.getString("error.systemerror");
      try {
        String
            json =
            GsonUtil.orderOutputToJson(false, null, message, localeStr, RESTUtil.VERSION_01);
        sendJsonResponse(resp, statusCode, json);

      } catch (Exception e1) {
        xLogger.severe("Protocol exception when sending order {0}: {1}", orderId, e1.getMessage());
        resp.setStatus(500);
      }
    }
    xLogger.fine("Exiting updateOrderStatusOld");
  }

  @SuppressWarnings("rawtypes")
  private void updateOrderStatus(HttpServletRequest req, HttpServletResponse resp,
                                 ResourceBundle backendMessages, ResourceBundle messages,
                                 boolean doApprovalConfigCheck)
      throws IOException, ServiceException {
    xLogger.fine("Entered updateOrderStatus");
    String password = req.getParameter(RestConstantsZ.PASSWORD); // sent when SMS message is sent
    // Get JSON input
    String jsonInput = req.getParameter(RestConstantsZ.JSON_STRING);
    UpdateOrderStatusRequest uosReq = GsonUtil.buildUpdateOrderStatusRequestFromJson(jsonInput);

    boolean status = true;
    String message = null;
    Locale locale = null;
    String timezone = null;

    DomainConfig dc = null;
    Long kioskId = null;
    Long vid = null;
    int statusCode = HttpServletResponse.SC_OK;
    // Authenticate or check authentication
    try {
      if (uosReq.kid
          == null) { // kiosk ID is mandatory, and user should have authorization on it (either domain owner, or a operator/manager of it)
        status = false;
        message = backendMessages.getString("error.nokiosk");
      } else {
        IUserAccount u = null;
        if (uosReq.oty.equals(IOrder.TYPE_SALE)) {
          if (uosReq.vid != null) {
            u = RESTUtil.authenticate(uosReq.uid, password, vid, req, resp);
          }
        } else {
          u = RESTUtil.authenticate(uosReq.uid, password, kioskId, req, resp);
        }

        if (uosReq.uid == null) // possible if BasicAuth was used
        {
          uosReq.uid = u.getUserId();
        }
        timezone = u.getTimezone();
        locale = u.getLocale();
        // Get domain config
        dc = DomainConfig.getInstance(u.getDomainId());
        if (doApprovalConfigCheck && isApprovalConfigEnabled(dc.getApprovalsConfig())) {
          status = false;
          message =
              backendMessages.getString("upgrade.app.message");
        }
      }
    } catch (ServiceException e) {
      status = false;
      message = e.getMessage();
    } catch (UnauthorizedException e) {
      message = e.getMessage();
      status = false;
      statusCode = HttpServletResponse.SC_UNAUTHORIZED;
    }
    // Update order status
    IOrder order = null;
    String errorCode = null;
    if (status) {
      String signature = CommonUtils.getMD5(jsonInput);
      MemcacheService cache = AppFactory.get().getMemcacheService();
      Integer lastStatus = (Integer) cache.get(signature);
      boolean isAlreadyProcessed = false;
      if (lastStatus != null) {
        switch (lastStatus) {
          case IJobStatus.COMPLETED:
            isAlreadyProcessed = true;
            break;
          case IJobStatus.INPROGRESS:
            sendPendingResponse(resp);
            return;
          case IJobStatus.FAILED:
            setSignatureAndStatus(cache, signature, IJobStatus.INPROGRESS);
        }
      }
      if (!isAlreadyProcessed) {
        try {
          if (uosReq.tid == null && (uosReq.sid == null || uosReq.sid.isEmpty())) {
            status = false;
            message = "Invalid Order ID or Shipment ID";
          } else if (uosReq.ost == null || uosReq.ost.isEmpty()) {
            status = false;
            message = backendMessages.getString("error.invalidstatus");
          } else if (uosReq.ost.equals(IOrder.FULFILLED)) {
            if (uosReq.sid == null || uosReq.sid.isEmpty()) {
              message = "Invalid shipment ID";
              status = false;
            } else if (uosReq.mt == null) {
              message = "Invalid materials list";
              status = false;
            } else if (uosReq.dar == null) {
              message = "Invalid actual date of receipt";
              status = false;
            }
          }
          if (status) {
            // Update order status
            UpdatedOrder uo = new UpdatedOrder();
            if (uosReq.sid != null && !uosReq.sid.isEmpty()) {
              if (uosReq.tid == null) {
                uo = OrderUtils.updateShpStatus(uosReq,
                    dc, SourceConstants.MOBILE,
                    backendMessages, uosReq.tm);
              } else {
                uo = OrderUtils.updateOrdStatus(uosReq,
                    dc, SourceConstants.MOBILE,
                    backendMessages);
              }
            } else if (uosReq.tid != null) {
              uo = OrderUtils.updateOrdStatus(uosReq, dc,
                  SourceConstants.MOBILE, backendMessages);
            } else {
              // Error cannot change status. One of them should be present.
              uo.message = "Either tid or mid should be present";
              uo.inventoryError = true;
            }
            order = uo.order;
            // Send inventory error message, if any
            if (uo.inventoryError) {
              xLogger.warn(
                  "Error when auto-posting inventory transactions when updating order {0}: {1}",
                  uosReq.tid, uo.message);
              status = false;
              message = uo.message;
            }
            setSignatureAndStatus(cache, signature, IJobStatus.COMPLETED);
          }
        } catch (ObjectNotFoundException e) {
          xLogger.severe("Object not found with order ID {0}: {1}", uosReq.tid,
              e.getMessage());
          status = false;
          message =
              messages.getString("order") + " " + uosReq.tid + " " + backendMessages
                  .getString(
                      "error.notfound");
          setSignatureAndStatus(cache, signature, IJobStatus.FAILED);

        } catch (InventoryAllocationException e) {
          xLogger.severe(
              "InventoryAllocationException while updating order status for order ID: {0}, shipment ID: {1}",
              uosReq.tid, uosReq.sid);
          status = false;
          message = e.getMessage();
          setSignatureAndStatus(cache, signature, IJobStatus.FAILED);
        } catch (InvalidServiceException e) {
          xLogger.severe("Error while updating shipment with ID {0}: {1}", uosReq.sid, e);
          status = false;
          message = e.getMessage();
          setSignatureAndStatus(cache, signature, IJobStatus.FAILED);
        } catch (ServiceException e) {
          xLogger.severe("Service exception when getting order with ID {0}: {1}",
              uosReq.tid, e.getMessage());
          status = false;
          message = backendMessages.getString("error.systemerror");
          setSignatureAndStatus(cache, signature, IJobStatus.FAILED);
        } catch (LogiException e) {
          xLogger.severe("Logi exception when updating shipment with ID {0}: {1}",
              uosReq.sid, e.getMessage());
          status = false;
          if (StringUtils.isNotEmpty(e.getCode())) {
            errorCode = e.getCode();
            message = e.getMessage();
          } else{
            message = backendMessages.getString("error.systemerror");
          }
          setSignatureAndStatus(cache, signature, IJobStatus.FAILED);
        }
      } else {
        order = getOrder(uosReq.tid);
      }
    }

    // For the JSON output and send
    try {
      boolean includeBatchDetails = true;
      MobileOrderModel mom = null;
      if (order != null) {
        dc = DomainConfig.getInstance(order.getDomainId());
        boolean isAccounting = dc.isAccountingEnabled();
        EntitiesService as = Services.getService(EntitiesServiceImpl.class);
        if (uosReq.oty.equals(IOrder.TYPE_PURCHASE)) {
          IKiosk k = as.getKiosk(uosReq.kid, false);
          includeBatchDetails = k.isBatchMgmtEnabled();
        } else if (uosReq.oty.equals(IOrder.TYPE_SALE)) {
          IKiosk k = as.getKiosk(uosReq.vid, false);
          includeBatchDetails = k.isBatchMgmtEnabled();
        }

        MobileOrderBuilder mob = new MobileOrderBuilder();
        mom = mob.build(order, locale, timezone, true, isAccounting, true, includeBatchDetails);
      }
      String jsonString = buildJsonResponse(resp, status, mom, message, errorCode);
      sendJsonResponse(resp, statusCode, jsonString);
    } catch (Exception e) {
      xLogger.severe("Protocol exception when sending order with ID {0}: {1}", uosReq.tid,
          e.getMessage());
      status = false;
      message = backendMessages.getString("error.systemerror");
      try {
        // Send error output as JSON
        String
            json =
            GsonUtil.buildGetOrderResponseModel(status, null, message, RESTUtil.VERSION_01);
        sendJsonResponse(resp, statusCode, json);

      } catch (Exception e1) {
        xLogger.severe("Protocol exception when sending order {0}: {1}",
            uosReq.tid, e1.getMessage());
        resp.setStatus(500);
      }
    }
    xLogger.fine("Exiting updateOrderStatus");
  }

  private IOrder getOrder(Long tid) throws ServiceException {
    try {
      OrderManagementService oms = Services.getService(OrderManagementServiceImpl.class);
      return oms.getOrder(tid, true);
    } catch (ServiceException | ObjectNotFoundException e) {
      xLogger.warn("Could not find the order {0}", tid);
    }
    return null;
  }

  // Schedule export of inventory data
  private void scheduleExport(HttpServletRequest req, HttpServletResponse resp,
                              ResourceBundle backendMessages, ResourceBundle messages) {
    xLogger.fine("Entered scheduleExport");
    int statusCode = HttpServletResponse.SC_OK;
    // Send response back to client
    try {
      String respStr = RESTUtil.scheduleKioskDataExport(req, messages, resp);
      if (respStr.contains("Invalid token")) {
        statusCode = HttpServletResponse.SC_UNAUTHORIZED;
      }
      // Send response
      sendJsonResponse(resp, statusCode, respStr);
    } catch (Exception e2) {
      xLogger.severe("OrderServlet Protocol Exception: {0}", e2.getMessage());
      resp.setStatus(500);
    }
    xLogger.fine("Exiting scheduleExport");
  }

  private boolean isOfDateTimeFormat(String dateStr) {
    boolean returnVal = false;
    if (dateStr != null && !dateStr.isEmpty()) {
      // Check if the date string has a white space character. If yes, return true. Else return false.
      if (dateStr.contains(" ")) {
        returnVal = true;
      }
    }
    return returnVal;
  }

  private void sendPendingResponse(HttpServletResponse resp) throws IOException {
    JsonObject jsonObject = new JsonObject();
    jsonObject.addProperty(JsonTagsZ.VERSION, RESTUtil.VERSION_01);
    jsonObject.addProperty(JsonTagsZ.MESSAGE, "Request is still being processed.");
    jsonObject.addProperty(JsonTagsZ.STATUS, 1);
    Gson gson = new GsonBuilder().setDateFormat(Constants.DATE_FORMAT)
        .excludeFieldsWithoutExposeAnnotation().create();
    sendJsonResponse(resp, HttpServletResponse.SC_OK, gson.toJson(jsonObject));
  }

  // Set the signature and status in cache
  private void setSignatureAndStatus(MemcacheService cache, String signature, int status) {
    if (signature != null && cache != null) {
      cache.put(signature, status, CACHE_EXPIRY);
    }
  }

  // Check if approval configuration is enabled
  private boolean isApprovalConfigEnabled(ApprovalsConfig ac) {
    return (ac != null && ac.getOrderConfig() != null && (
        !ac.getOrderConfig().getPurchaseSalesOrderApproval().isEmpty() || ac.getOrderConfig()
            .isTransferApprovalEnabled()));
  }

}

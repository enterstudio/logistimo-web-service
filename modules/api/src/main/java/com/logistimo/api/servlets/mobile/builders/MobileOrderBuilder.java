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

package com.logistimo.api.servlets.mobile.builders;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import com.logistimo.accounting.models.CreditData;
import com.logistimo.accounting.service.impl.AccountingServiceImpl;
import com.logistimo.activity.entity.IActivity;
import com.logistimo.activity.models.ActivityModel;
import com.logistimo.activity.service.ActivityService;
import com.logistimo.activity.service.impl.ActivityServiceImpl;
import com.logistimo.api.models.OrderMinimumResponseModel;
import com.logistimo.config.models.DomainConfig;
import com.logistimo.constants.Constants;
import com.logistimo.context.StaticApplicationContext;
import com.logistimo.entities.entity.IKiosk;
import com.logistimo.entities.service.EntitiesService;
import com.logistimo.entities.service.EntitiesServiceImpl;
import com.logistimo.logger.XLog;
import com.logistimo.orders.OrderUtils;
import com.logistimo.orders.approvals.service.IOrderApprovalsService;
import com.logistimo.orders.entity.IDemandItem;
import com.logistimo.orders.entity.IOrder;
import com.logistimo.orders.entity.approvals.IOrderApprovalMapping;
import com.logistimo.pagination.Results;
import com.logistimo.proto.ApprovalResponse;
import com.logistimo.proto.MobileApprovalResponse;
import com.logistimo.proto.MobileConversationModel;
import com.logistimo.proto.MobileDemandItemModel;
import com.logistimo.proto.MobileOrderModel;
import com.logistimo.proto.MobileOrdersModel;
import com.logistimo.proto.MobileShipmentModel;
import com.logistimo.proto.RestConstantsZ;
import com.logistimo.services.ServiceException;
import com.logistimo.services.Services;
import com.logistimo.tags.TagUtil;
import com.logistimo.users.entity.IUserAccount;
import com.logistimo.users.service.UsersService;
import com.logistimo.users.service.impl.UsersServiceImpl;
import com.logistimo.utils.BigUtil;
import com.logistimo.utils.LocalDateUtil;
import com.logistimo.utils.StringUtil;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * Created by vani on 03/11/16.
 */
public class MobileOrderBuilder {

  private static final XLog xLogger = XLog.getLog(MobileOrderBuilder.class);

  public MobileOrderModel build(IOrder o, Locale locale, String timezone, boolean includeItems,
                                boolean includeAccountingData, boolean includeShipmentItems,
                                boolean includeBatchDetails) {
    if (o == null) {
      return null;
    }
    UsersService as;
    EntitiesService entitiesService;
    try {
      as = Services.getService(UsersServiceImpl.class);
      entitiesService = Services.getService(EntitiesServiceImpl.class);
    } catch (Exception e) {
      xLogger.warn("Error while fetching account service", e);
      return null;
    }
    MobileOrderModel mom = new MobileOrderModel();
    IUserAccount user;
    mom.tid = o.getOrderId();
    mom.rid = o.getReferenceID();
    mom.ost = o.getStatus();
    mom.q = o.getNumberOfItems();
    mom.cbid = o.getUserId();
    mom.tm = LocalDateUtil.formatCustom(o.getUpdatedOn(), Constants.DATETIME_FORMAT, null);
    try {
      user = as.getUserAccount(o.getUserId());
      mom.cbn = user.getFullName();
      String customUserId = user.getCustomId();
      if (customUserId != null && !customUserId.isEmpty()) {
        mom.cuid = customUserId;
      }
    } catch (Exception e) {
      xLogger.warn("Exception while getting user account for user {0} for order with ID {1}",
          o.getUserId(), o.getOrderId(), e);
    }
    mom.ubid = o.getUpdatedBy();
    if (mom.ubid != null) {
      try {
        user = as.getUserAccount(mom.ubid);
        mom.ubn = user.getFullName();
      } catch (Exception e) {
        xLogger.warn("Exception while getting user account for user {0} for order with ID {1}",
            o.getUpdatedBy(), o.getOrderId(), e);
      }
    }
    mom.t = LocalDateUtil.format(o.getCreatedOn(), locale, timezone);
    if (o.getUpdatedOn() != null) {
      mom.ut = LocalDateUtil.format(o.getUpdatedOn(), locale, timezone);
    }
    try {
      ActivityService acs = Services.getService(ActivityServiceImpl.class);
      Results
          res =
          acs.getActivity(o.getOrderId().toString(), IActivity.TYPE.ORDER.toString(), null, null,
              null, null, null);
      if (res != null) {
        List<ActivityModel> amList = res.getResults();
        if (amList != null && !amList.isEmpty()) {
          SimpleDateFormat sdf = new SimpleDateFormat(Constants.DATETIME_FORMAT);
          for (ActivityModel am : amList) {
            if (IOrder.COMPLETED.equals(am.newValue)) {
              Date cd = sdf.parse(am.createDate);
              mom.osht = LocalDateUtil.format(cd, locale, timezone);
              break;
            }
          }
        }
      }
    } catch (Exception e) {
      xLogger.warn("Exception while getting shipped time for order with ID {0}", o.getOrderId(), e);
    }
    mom.kid = o.getKioskId();

    try {
      IKiosk k = entitiesService.getKiosk(o.getKioskId(), false);
      if (o.getOrderType() == IOrder.TRANSFER) {
        mom.knm = k.getName();
        mom.kcty = k.getCity();
      }
      String customKioskId = k.getCustomId();
      if (customKioskId != null && !customKioskId.isEmpty()) {
        mom.ckid = customKioskId;
      }
    } catch (Exception e) {
      xLogger.warn("Exception while getting kiosk for kiosk id {0} for order {1}", o.getKioskId(),
          o.getOrderId(), e);
      return null;
    }
    mom.vid = o.getServicingKiosk();
    try {
      if (o.getServicingKiosk() != null) {
        IKiosk vendor = entitiesService.getKiosk(o.getServicingKiosk(), false);
        String customVendorId = vendor.getCustomId();
        if (customVendorId != null && !customVendorId.isEmpty()) {
          mom.cvid = customVendorId;
        }
        if (o.getOrderType() == IOrder.TRANSFER) {
          mom.vnm = vendor.getName();
          mom.vcty = vendor.getCity();
        }
      }
    } catch (Exception e) {
      xLogger
          .warn("Exception when getting vendor with id {0} for in order {1}", o.getServicingKiosk(),
              o.getOrderId(), e);
      if (o.getOrderType() == IOrder.TRANSFER) {
        return null; // For transfer, vendor should be present
      }
    }

    List<String> otgs = o.getTags(TagUtil.TYPE_ORDER);
    if (otgs != null && otgs.size() > 0) {
      mom.tg = StringUtil.getCSV(otgs);
    }
    mom.tp = o.getTotalPrice();
    mom.cu = o.getCurrency();
    if (o.getExpectedArrivalDate() != null) {
      mom.eta = LocalDateUtil.format(o.getExpectedArrivalDate(), locale, timezone);
    }
    if (o.getDueDate() != null) {
      mom.rbd = LocalDateUtil.format(o.getDueDate(), locale, timezone);
    }
    mom.rsnco = o.getCancelledDiscrepancyReason();
    mom.pymt = o.getPaid();
    mom.popt = o.getPaymentOption();

    if (includeItems) {
      MobileDemandBuilder mdb = new MobileDemandBuilder();
      List<IDemandItem> diList = (List<IDemandItem>) o.getItems();
      List<MobileDemandItemModel>
          mdimList =
          mdb.buildMobileDemandItemModels(diList, locale, timezone, includeBatchDetails);
      if (mdimList != null && !mdimList.isEmpty()) {
        mom.mt = mdimList;
      }
    }
    // Credit related data
    if (includeAccountingData) {
      try {
        CreditData
            cd =
            Services.getService(AccountingServiceImpl.class).getCreditData(o.getKioskId(),
                o.getServicingKiosk(),
                DomainConfig.getInstance(o.getDomainId()));
        if (BigUtil.notEqualsZero(cd.creditLimit)) {
          mom.crl = cd.creditLimit;
          mom.pybl = cd.creditLimit.subtract(cd.availabeCredit);
        }
      } catch (Exception e) {
        xLogger.warn("{0} when trying to get credit data for order {0}", o.getOrderId(), e);
      }
    }

    // Shipment data
    MobileShipmentBuilder msb = new MobileShipmentBuilder();
    List<MobileShipmentModel>
        msList =
        msb.buildMobileShipmentModels(o.getOrderId(), locale, timezone, includeShipmentItems,
            includeBatchDetails);
    if (msList != null && !msList.isEmpty()) {
      mom.shps = msList;
    }
    // Conversations
    MobileConversationBuilder mcb = new MobileConversationBuilder();
    MobileConversationModel
        mcm =
        mcb.build(MobileConversationBuilder.CONVERSATION_OBJECT_TYPE_ORDER,
            o.getOrderId().toString(), locale, timezone);
    if (mcm != null && mcm.cnt > 0) {
      mom.cmnts = mcm;
    }

    //Set the status update time
    if (o.getStatusUpdatedOn() != null) {
      mom.ostt = o.getStatusUpdatedOn().getTime();
    }

    //Get the order type
    mom.oty = OrderUtils.getOrderType(o.getOrderType());

    /* Get approval details for the order */
    try {
      mom.apprvl = new MobileApprovalResponse();
      ApprovalResponse
          purchaseApproval =
          new MobileApprovalResponseBuilder().buildApprovalResponse(o, IOrder.PURCHASE_ORDER);
      if (purchaseApproval != null) {
        mom.apprvl.prc = purchaseApproval;
      }
      ApprovalResponse
          salesApproval =
          new MobileApprovalResponseBuilder().buildApprovalResponse(o, IOrder.SALES_ORDER);
      if (salesApproval != null) {
        mom.apprvl.sle = salesApproval;
      }
    } catch (Exception e) {
      xLogger.warn("Exception fetching approval details", e);
    }
    return mom;
  }

  public MobileOrdersModel buildOrders(List<IOrder> orders, Locale locale, String timezone,
                                       boolean includeItems, boolean includeShipmentItems,
                                       boolean includeBatchDetails) {
    if (orders == null || orders.isEmpty()) {
      return null;
    }
    List<MobileOrderModel> momList = new ArrayList<>(1);
    for (IOrder o : orders) {
      DomainConfig dc = DomainConfig.getInstance(o.getDomainId());
      boolean isAccEnabled = dc.isAccountingEnabled();
      MobileOrderModel
          mom =
          build(o, locale, timezone, includeItems, isAccEnabled, includeShipmentItems,
              includeBatchDetails);
      if (mom != null) {
        momList.add(mom);
      }
    }
    MobileOrdersModel mom = null;
    if (momList != null && !momList.isEmpty()) {
      mom = new MobileOrdersModel();
      mom.os = momList;
    }
    return mom;
  }

  /**
   * Method to build the order response json
   *
   * @param orders    List of Orders
   * @param locale    user's locale
   * @param timezone  user's timezone
   * @param orderType Order type required for adding approval status
   * @return JsonObject - response
   */
  public JsonObject buildOrdersResponse(List<IOrder> orders, Locale locale, String timezone,
                                        int orderType) {

    if (orders != null && !orders.isEmpty()) {
      //Get the kiosk as a map
      Map<Long, Kiosk> kioskMap = getKioskMap(orders);
      //Get user details userId and full name as a map
      Map<String, String> userDetailsMap = getUserDetailsMap(orders);
      List<OrderMinimumResponseModel> orderResponseList = new ArrayList<>(orders.size());
      Set<Long> orderIds = getOrderIds(orders);
      Map<Long, IOrderApprovalMapping>
          orderApprovalMap =
          getOrderApprovalMap(orderIds, orderType);
      for (IOrder order : orders) {
        OrderMinimumResponseModel
            model =
            buildMinimumOrder(order, userDetailsMap, kioskMap, locale, timezone,
                orderApprovalMap.get(order.getOrderId()));
        orderResponseList.add(model);
      }
      return buildOrderJson(orderResponseList);
    }
    return null;
  }

  /**
   * Method to build json response
   *
   * @param orderMinRespModelList Orders model
   * @return json response
   */
  private JsonObject buildOrderJson(List<OrderMinimumResponseModel> orderMinRespModelList) {
    JsonObject jsonObject = new JsonObject();
    Gson gson = new Gson();
    String orderString = gson.toJson(orderMinRespModelList);
    JsonElement mElement = gson.fromJson(orderString, JsonElement.class);
    jsonObject.add(RestConstantsZ.MINI_RESP_ORDER_KEY, gson.toJsonTree(mElement));
    return jsonObject;
  }

  /**
   * Method to get a set of order ids
   *
   * @param orderList List of orders
   * @return Set of order ids
   */
  private Set<Long> getOrderIds(List<IOrder> orderList) {
    Set<Long> orderIdSet = new HashSet<>();
    if (orderList != null && !orderList.isEmpty()) {
      for (IOrder order : orderList) {
        orderIdSet.add(order.getOrderId());
      }
    }
    return orderIdSet;
  }

  /**
   * Method to populate a map with order Id as key and associated orderApprovalMapping as value
   *
   * @param orderIds  List of order Ids
   * @param orderType Approval type ( 0-TRANSFER, 1- PURCHASE, 2-SALES)
   * @return Map
   */
  private Map<Long, IOrderApprovalMapping> getOrderApprovalMap(Set<Long> orderIds,
                                                               int orderType) {

    List<IOrderApprovalMapping>
        orderApprovalMappingList =
        StaticApplicationContext
            .getBean(IOrderApprovalsService.class).getOrdersApprovalMapping(orderIds, orderType);
    Map<Long, IOrderApprovalMapping> orderMap = new HashMap<>(orderApprovalMappingList.size());
    for (IOrderApprovalMapping orderApprovalMapping : orderApprovalMappingList) {
      orderMap.put(orderApprovalMapping.getOrderId(), orderApprovalMapping);
    }
    return orderMap;
  }

  /**
   * Populate field of minimum response model
   *
   * @param order    - Order object
   * @param userMap  - Map which has user name as keys and user full name as value
   * @param kioskMap -Map which has the Kiosk ID as key and associated kiosk details as value
   * @param locale   -User's locale
   * @param timezone -User's timezone
   */
  private OrderMinimumResponseModel buildMinimumOrder(IOrder order, Map<String, String> userMap,
                                                      Map<Long, Kiosk> kioskMap, Locale locale,
                                                      String timezone,
                                                      IOrderApprovalMapping approvalMapping) {

    OrderMinimumResponseModel model = new OrderMinimumResponseModel();
    model.setTid(order.getOrderId());
    model.setOst(order.getStatus());
    model.setQ(order.getNumberOfItems());
    model.setCbid(order.getUserId());
    List<String> otgs = order.getTags(TagUtil.TYPE_ORDER);
    if ((otgs != null) && !otgs.isEmpty()) {
      model.setTg(StringUtil.getCSV(otgs));
    }
    model.setCbn(userMap.get(order.getUserId()));
    model.setUbid(order.getUpdatedBy());
    model.setUbn(userMap.get(order.getUpdatedBy()));
    String createdTime = LocalDateUtil.format(order.getCreatedOn(), locale, timezone);
    model.setT(createdTime);
    if (order.getUpdatedOn() != null) {
      String updatedTime = LocalDateUtil.format(order.getUpdatedOn(), locale, timezone);
      model.setUt(updatedTime);
    }
    //Set the source kiosk details
    model.setKid(order.getKioskId());
    if (kioskMap.containsKey(order.getKioskId())) {
      Kiosk kiosk = kioskMap.get(order.getKioskId());
      model.setKnm(kiosk.getName());
      model.setKcty(kiosk.getCity());
    }
    //Set the servicing kiosk details
    if (order.getServicingKiosk() != null) {
      model.setVid(order.getServicingKiosk());
      if (kioskMap.containsKey(order.getServicingKiosk())) {
        Kiosk vendor = kioskMap.get(order.getServicingKiosk());
        model.setVnm(vendor.getName());
        model.setVcty(vendor.getCity());
      }
    }
    //Add approver details to the response
    JsonObject jsonObject = new JsonObject();
    if (approvalMapping != null) {
      jsonObject.addProperty(RestConstantsZ.STATUS, approvalMapping.getStatus());
      jsonObject.addProperty(RestConstantsZ.TIME, approvalMapping.getUpdatedAt().getTime());
    }
    model.setApprvl(jsonObject);
    return model;
  }


  /**
   * Method to fetch the list of user details and populate it in a map
   *
   * @param orderList List of orders
   * @return Map with user id as key and user full name as value
   */
  private Map<String, String> getUserDetailsMap(List<IOrder> orderList) {
    Set<String> userIdList = new HashSet<>();
    for (IOrder order : orderList) {
      userIdList.add(order.getUserId());
      userIdList.add(order.getUpdatedBy());
    }
    UsersService usersService = Services.getService(UsersServiceImpl.class);
    List<IUserAccount> userAccountList = usersService.getUsersByIds(new ArrayList<>(userIdList));
    Map<String, String> userDetailsMap = new HashMap<>(userAccountList.size());
    for (IUserAccount userAccount : userAccountList) {
      userDetailsMap.put(userAccount.getUserId(), userAccount.getFullName());
    }
    return userDetailsMap;
  }

  /**
   * Method takes the unique kiosk ids from the given set of orders and returns a map with kiosk id as key and kiosk object as value
   *
   * @param orderList List of orders
   * @return returns a map with kiosk id as key and kiosk object as value
   */
  private Map<Long, Kiosk> getKioskMap(List<IOrder> orderList) {
    Set<Long> kioskIdList = new HashSet<>();
    EntitiesService entitiesService = Services.getService(EntitiesServiceImpl.class);
    Map<Long, Kiosk> kioskMap = new HashMap<>();
    for (IOrder order : orderList) {
      getKioskDetails(kioskIdList, entitiesService, kioskMap, order.getKioskId());
      getKioskDetails(kioskIdList, entitiesService, kioskMap, order.getServicingKiosk());
    }
    return kioskMap;
  }

  /**
   * Get the details of the kiosk
   *
   * @param kioskIdList     List of unique kiosk ids
   * @param entitiesService entity service
   * @param kioskMap        map with kiosk id and Kiosk details
   * @param kioskId         kioskId
   */
  private void getKioskDetails(Set<Long> kioskIdList, EntitiesService entitiesService,
                               Map<Long, Kiosk> kioskMap, Long kioskId) {
    if (!kioskIdList.contains(kioskId)) {
      try {
        IKiosk kiosk = entitiesService.getKiosk(kioskId);
        kioskMap.put(kiosk.getKioskId(), new Kiosk(kiosk.getName(), kiosk.getCity()));
        kioskIdList.add(kiosk.getKioskId());
      } catch (ServiceException e) {
        xLogger.warn("Exception fetching kiosk details", e);
      }
    }
  }


  class Kiosk {
    private String name;
    private String city;

    Kiosk(String name, String city) {
      this.city = city;
      this.name = name;
    }

    public String getName() {
      return name;
    }

    public String getCity() {
      return city;
    }
  }
}

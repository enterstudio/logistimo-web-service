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

package com.logistimo.api.builders;

import com.logistimo.accounting.service.impl.AccountingServiceImpl;
import com.logistimo.api.models.DemandItemBatchModel;
import com.logistimo.api.models.DemandModel;
import com.logistimo.api.models.OrderApprovalTypesModel;
import com.logistimo.api.models.OrderApproverModel;
import com.logistimo.api.models.OrderModel;
import com.logistimo.api.models.OrderResponseModel;
import com.logistimo.api.models.Permissions;
import com.logistimo.api.models.UserContactModel;
import com.logistimo.auth.SecurityConstants;
import com.logistimo.auth.utils.SecurityUtils;
import com.logistimo.config.models.ApprovalsConfig;
import com.logistimo.config.models.DomainConfig;
import com.logistimo.constants.CharacterConstants;
import com.logistimo.constants.Constants;
import com.logistimo.constants.PermissionConstants;
import com.logistimo.dao.JDOUtils;
import com.logistimo.domains.entity.IDomain;
import com.logistimo.domains.service.DomainsService;
import com.logistimo.domains.service.impl.DomainsServiceImpl;
import com.logistimo.domains.utils.DomainsUtil;
import com.logistimo.entities.auth.EntityAuthoriser;
import com.logistimo.entities.entity.IApprovers;
import com.logistimo.entities.entity.IKiosk;
import com.logistimo.entities.service.EntitiesService;
import com.logistimo.entities.service.EntitiesServiceImpl;
import com.logistimo.inventory.dao.impl.InvntryDao;
import com.logistimo.inventory.entity.IInvAllocation;
import com.logistimo.inventory.entity.IInvntry;
import com.logistimo.inventory.entity.IInvntryBatch;
import com.logistimo.inventory.entity.IInvntryEvntLog;
import com.logistimo.inventory.entity.ITransaction;
import com.logistimo.inventory.service.InventoryManagementService;
import com.logistimo.inventory.service.impl.InventoryManagementServiceImpl;
import com.logistimo.logger.XLog;
import com.logistimo.materials.entity.IHandlingUnit;
import com.logistimo.materials.entity.IMaterial;
import com.logistimo.materials.service.IHandlingUnitService;
import com.logistimo.materials.service.MaterialCatalogService;
import com.logistimo.materials.service.impl.HandlingUnitServiceImpl;
import com.logistimo.materials.service.impl.MaterialCatalogServiceImpl;
import com.logistimo.models.shipments.ShipmentItemBatchModel;
import com.logistimo.models.shipments.ShipmentItemModel;
import com.logistimo.orders.OrderUtils;
import com.logistimo.orders.approvals.constants.ApprovalConstants;
import com.logistimo.orders.approvals.dao.impl.ApprovalsDao;
import com.logistimo.orders.approvals.service.IOrderApprovalsService;
import com.logistimo.orders.entity.IDemandItem;
import com.logistimo.orders.entity.IOrder;
import com.logistimo.orders.entity.approvals.IOrderApprovalMapping;
import com.logistimo.orders.models.UpdatedOrder;
import com.logistimo.orders.service.IDemandService;
import com.logistimo.orders.service.OrderManagementService;
import com.logistimo.orders.service.impl.DemandService;
import com.logistimo.orders.service.impl.OrderManagementServiceImpl;
import com.logistimo.pagination.Results;
import com.logistimo.security.SecureUserDetails;
import com.logistimo.services.ObjectNotFoundException;
import com.logistimo.services.ServiceException;
import com.logistimo.services.Services;
import com.logistimo.shipments.ShipmentStatus;
import com.logistimo.shipments.entity.IShipment;
import com.logistimo.shipments.entity.IShipmentItem;
import com.logistimo.shipments.entity.IShipmentItemBatch;
import com.logistimo.shipments.service.IShipmentService;
import com.logistimo.shipments.service.impl.ShipmentService;
import com.logistimo.tags.TagUtil;
import com.logistimo.users.entity.IUserAccount;
import com.logistimo.users.service.UsersService;
import com.logistimo.users.service.impl.UsersServiceImpl;
import com.logistimo.utils.BigUtil;
import com.logistimo.utils.CommonUtils;
import com.logistimo.utils.LocalDateUtil;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

@Component
public class OrdersAPIBuilder {

  private static final XLog xLogger = XLog.getLog(OrdersAPIBuilder.class);

  public static final String PERMISSIONS = "permissions";

  public static final String[] DEFAULT_EMBED = new String[]{OrdersAPIBuilder.PERMISSIONS};


  @Autowired
  private IOrderApprovalsService orderApprovalsService;

  public Results buildOrders(Results results, SecureUserDetails user, Long domainId) {
    List orders = results.getResults();
    List<OrderModel> newOrders = new ArrayList<>();
    int sno = results.getOffset() + 1;
    Map<Long, String> domainNames = new HashMap<>(1);
    for (Object obj : orders) {
      IOrder o = (IOrder) obj;
      // Add row
      OrderModel model = build(o, user, domainId, domainNames);
      if (model != null) {
        model.sno = sno++;
        newOrders.add(model);
      }
    }
    return new Results(newOrders, results.getCursor(),
        results.getNumFound(), results.getOffset());
  }

  public OrderModel build(IOrder o, SecureUserDetails user, Long domainId,
                          Map<Long, String> domainNames) {
    OrderModel model = new OrderModel();
    Long kioskId = o.getKioskId();
    Locale locale = user.getLocale();
    String timezone = user.getTimezone();
    IKiosk k;
    IKiosk vendor = null;
    try {
      EntitiesService as = Services.getService(EntitiesServiceImpl.class, locale);
      k = as.getKiosk(kioskId, false);
    } catch (Exception e) {
      xLogger.warn("{0} when getting kiosk data for order {1}: {2}",
          e.getClass().getName(), o.getOrderId(), e.getMessage());
      return null;
    }
    try {
      DomainsService ds = Services.getService(DomainsServiceImpl.class);
      UsersService as = Services.getService(UsersServiceImpl.class, locale);
      EntitiesService es = Services.getService(EntitiesServiceImpl.class, locale);
      String domainName = domainNames.get(o.getDomainId());

      model.id = o.getOrderId();
      model.size = o.getNumberOfItems();
      model.cur = o.getCurrency();
      model.price = o.getFormattedPrice();
      model.status = OrderUtils.getStatusDisplay(o.getStatus(), locale);
      model.st = o.getStatus();
      model.enm = k.getName();
      model.eadd = CommonUtils.getAddress(k.getCity(), k.getTaluk(), k.getDistrict(), k.getState());
      model.cdt = LocalDateUtil.format(o.getCreatedOn(), locale, timezone);
      model.ubid = o.getUpdatedBy();
      model.src = o.getSrc();
      model.rid = o.getReferenceID();
      if (o.getUpdatedBy() != null) {
        try {
          model.uby = as.getUserAccount(o.getUpdatedBy()).getFullName();
          model.udt = LocalDateUtil.format(o.getUpdatedOn(), locale, timezone);
          model.orderUpdatedAt =
              LocalDateUtil.formatCustom(o.getUpdatedOn(), Constants.DATETIME_FORMAT, null);
        } catch (Exception e) {
          // ignore
        }
      }
      model.tax = o.getTax();
      model.uid = o.getUserId();

      if (model.uid != null) {
        try {
          IUserAccount orderedBy = as.getUserAccount(model.uid);
          model.unm = orderedBy.getFullName();
        } catch (Exception e) {
          xLogger.warn("{0} when getting details for user who created the order {1}: ",
              e.getClass().getName(), o.getOrderId(), e);
        }
      }
      model.eid = o.getKioskId();
      model.tgs = o.getTags(TagUtil.TYPE_ORDER);
      model.sdid = o.getDomainId();
      model.lt = o.getLatitude();
      model.ln = o.getLongitude();
      Long vendorId = o.getServicingKiosk();

      if (vendorId != null) {
        try {
          vendor = es.getKiosk(vendorId, false);
          model.vid = vendorId.toString();
          model.vnm = vendor.getName();
          model.vadd =
              CommonUtils.getAddress(vendor.getCity(), vendor.getTaluk(), vendor.getDistrict(),
                  vendor.getState());
          model.hva =
              EntityAuthoriser
                  .authoriseEntity(vendorId, user.getRole(), locale, user.getUsername(), domainId);
        } catch (Exception e) {
          xLogger.warn("{0} when getting vendor data for order {1}: {2}",
              e.getClass().getName(), o.getOrderId(), e.getMessage());
          if (o.getStatus().equals(IOrder.FULFILLED)) {
            model.vid = "-1";
          }
        }
      }
      Integer vPermission = k.getVendorPerm();
      if (vPermission < 2 && model.vid != null) {
        vPermission =
            EntityAuthoriser
                .authoriseEntityPerm(Long.valueOf(model.vid), user.getRole(), user.getLocale(),
                    user.getUsername(), user.getDomainId());
      }
      model.atv = vPermission > 1;
      model.atvv = vPermission > 0;

      if (vendor != null) {
        Integer cPermission = vendor.getCustomerPerm();
        if (cPermission < 2 && model.eid != null) {
          cPermission =
              EntityAuthoriser.authoriseEntityPerm(model.eid, user.getRole(), user.getLocale(),
                  user.getUsername(), user.getDomainId());
        }
        model.atc = cPermission > 1;
        model.atvc = cPermission > 0;
      }
      if (domainName == null) {
        IDomain domain = null;
        try {
          domain = ds.getDomain(o.getDomainId());
        } catch (Exception e) {
          xLogger.warn("Error while fetching Domain {0}", o.getDomainId());
        }
        if (domain != null) {
          domainName = domain.getName();
        } else {
          domainName = Constants.EMPTY;
        }
        domainNames.put(o.getDomainId(), domainName);
      }
      model.sdname = domainName;
      SimpleDateFormat sdf = new SimpleDateFormat(Constants.DATE_FORMAT_CSV);
      if (o.getExpectedArrivalDate() != null) {
        model.efd = sdf.format(o.getExpectedArrivalDate());
        model.efdLabel =
            LocalDateUtil
                .format(o.getExpectedArrivalDate(), user.getLocale(), user.getTimezone(), true);
      }
      if (o.getDueDate() != null) {
        model.edd = sdf.format(o.getDueDate());
        model.eddLabel =
            LocalDateUtil.format(o.getDueDate(), user.getLocale(), user.getTimezone(), true);
      }
    } catch (Exception e) {
      xLogger.warn("{0} when trying to get kiosk {1} and create a new row for order {2}: {3}",
          e.getClass().getName(), kioskId, o.getOrderId(), e);
    }
    model.oty = o.getOrderType();
    model.crsn = o.getCancelledDiscrepancyReason();
    return model;
  }

  /*public OrderModel buildApprovalModel(OrderModel model, CreateApprovalResponse response, int approvalsSize) {
    OrderApprovalModel approvalModel = new OrderApprovalModel();
    if(response.getActiveApproverType().equals(Approver.PRIMARY)) {
      approvalModel.setAprvs(model.getPrimaryApprovers());
    } else {
      approvalModel.setAprvs(model.getSecondaryApprovers());
    }
    approvalModel.setArs(approvalsSize);
    approvalModel.setCt(response.getCreatedAt());
    approvalModel.setUt(response.getUpdatedAt());
    Long hours = (new Date().getTime() - response.getExpireAt().getTime()) / (60 * 60 * 1000);
    approvalModel.setExpiryTime(hours);
    approvalModel.setStatus(response.getStatus());


    return model;
  }*/

  /*public OrderModel buildApproverDetails(OrderModel model) {
    if(model.getPrimaryApprovers() != null) {
      StringBuilder sb = new StringBuilder();
      int j=0;
      for(int i=0; i<model.getPrimaryApprovers().size(); i++) {
        UserModel userModel = model.getPrimaryApprovers().get(i);
        if(userModel.fnm != null) {
          sb.append(userModel.fnm);
          sb.append(SPACE);
        }
        if(userModel.lnm != null) {
          sb.append(userModel.lnm);
          sb.append(SPACE);
        }
        if(StringUtils.isNotEmpty(userModel.phm) || StringUtils.isNotEmpty(userModel.em)) {
          sb.append("(");
          boolean found = false;
          if(userModel.phm != null) {
            sb.append(userModel.phm);
            found = true;
          }
          if(userModel.em != null) {
            if(found){
              sb.append(COMMA_SPACE);
            }
            sb.append(userModel.em);
          }
        }
        if(i < model.getPrimaryApprovers().size()) {
          sb.append(")");
          j = j+1;
        }
        if(j < model.getPrimaryApprovers().size()) {
          sb.append(COMMA_SPACE);
        }
      }
      if(sb.length() > 0) {
        model.setApproversDetail(sb.toString());
      }
    }
    return model;
  }*/

  /**
   * Returns the primary approvers for a particular order and approval type
   */
  public List<UserContactModel> buildPrimaryApprovers(IOrder order, Locale locale,
                                                      Integer approvalType)
      throws ServiceException, ObjectNotFoundException {
    List<String> prApprovers = new ArrayList<>(1);
    EntitiesService entitiesService = Services.getService(EntitiesServiceImpl.class, locale);
    if (IOrder.TRANSFER_ORDER == approvalType) {
      prApprovers = DomainConfig.getInstance(order.getDomainId()).getApprovalsConfig()
          .getOrderConfig().getPrimaryApprovers();
    } else {
      List<IApprovers> primaryApprovers;
      if (IOrder.PURCHASE_ORDER == approvalType) {
        primaryApprovers =
            entitiesService.getApprovers(order.getKioskId(), IApprovers.PRIMARY_APPROVER,
                IApprovers.PURCHASE_ORDER);
      } else {
        primaryApprovers =
            entitiesService.getApprovers(order.getServicingKiosk(), IApprovers.PRIMARY_APPROVER,
                IApprovers.SALES_ORDER);
      }
      if (primaryApprovers != null) {
        for (IApprovers apr : primaryApprovers) {
          prApprovers.add(apr.getUserId());
        }
      }
    }
    return buildUserContactModels(prApprovers);
  }

  public List<UserContactModel> buildUserContactModels(List<String> approvers)
      throws ObjectNotFoundException {
    List<UserContactModel> models = new ArrayList<>(1);
    if (approvers != null && !approvers.isEmpty()) {
      UsersService
          usersService =
          Services.getService(UsersServiceImpl.class, SecurityUtils.getLocale());
      for (String s : approvers) {
        IUserAccount userAccount = usersService.getUserAccount(s);
        UserContactModel model = new UserContactModel();
        model.setEmail(userAccount.getEmail());
        model.setName(userAccount.getFullName());
        model.setPhone(userAccount.getMobilePhoneNumber());
        model.setUserId(userAccount.getUserId());
        models.add(model);
      }
    }
    return models;
  }

  /*public OrderModel buildApprovalType(OrderModel model, IOrder order, Locale locale)
      throws ServiceException {
    OrderManagementService orderManagementService =
        Services.getService(OrderManagementServiceImpl.class, locale);
    if(IOrder.PURCHASE_ORDER.equals(order.getOrderType())) {
      if(order.isVisibleToCustomer() && !order.isVisibleToVendor()) {
        if(orderManagementService.isApprovalRequired(order, locale, IOrder.PURCHASE_ORDER)) {
          model.setApprovalType(IOrder.PURCHASE_ORDER);
        }
      } else if(orderManagementService.isApprovalRequired(order, locale, IOrder.SALES_ORDER)) {
        model.setApprovalType(IOrder.SALES_ORDER);
      }
    } else if(IOrder.SALES_ORDER.equals(order.getOrderType())) {
      if(orderManagementService.isApprovalRequired(order, locale, IOrder.SALES_ORDER)) {
        model.setApprovalType(IOrder.SALES_ORDER);
      }
    }else if(IOrder.TRANSFER_ORDER.equals(order.getOrderType())
        && !order.isVisibleToCustomer() && !order.isVisibleToVendor()) {
      if(orderManagementService.isApprovalRequired(order, locale)) {
        model.setApprovalType(IOrder.TRANSFER);
      }
    }
    return model;
  }*/

  /**
   * Returns the permission to be restricted
   */
  public Permissions buildPermissionModel(IOrder order, OrderModel orderModel, Integer approvalType,
                                          boolean isApprovalRequired) {
    Permissions model = new Permissions();
    List<String> permissions = new ArrayList<>(1);
    ApprovalsDao approvalsDao = new ApprovalsDao();
    if (isApprovalRequired) {
      IOrderApprovalMapping
          approvalMapping =
          approvalsDao.getOrderApprovalMapping(order.getOrderId(), approvalType);

      if (IOrder.PURCHASE_ORDER == approvalType) {
        if (approvalMapping != null) {
          if (ApprovalConstants.PENDING.equals(approvalMapping.getStatus())) {
            if (orderModel.atc) {
              //permissions.add(PermissionConstants.CANCEL);
            }
          } else if (ApprovalConstants.CANCELLED.equals(approvalMapping.getStatus()) ||
              ApprovalConstants.REJECTED.equals(approvalMapping.getStatus()) ||
              ApprovalConstants.EXPIRED.equals(approvalMapping.getStatus())) {
            if (orderModel.atc) {
              permissions.add(PermissionConstants.CANCEL);
              permissions.add(PermissionConstants.EDIT);
            }
          } else if (ApprovalConstants.APPROVED.equals(approvalMapping.getStatus())) {
            if (orderModel.atv) {
              permissions.add(PermissionConstants.ALLOCATE);
              permissions.add(PermissionConstants.EDIT);
              permissions.add(PermissionConstants.CONFIRM);
              permissions.add(PermissionConstants.CANCEL);
            }
          }
        } else {
          if (orderModel.atc) {
            permissions.add(PermissionConstants.CANCEL);
            permissions.add(PermissionConstants.EDIT);
          }
        }
      } else if (IOrder.SALES_ORDER == approvalType) {
        if (approvalMapping != null) {
          if (ApprovalConstants.PENDING.equals(approvalMapping.getStatus())) {
            if (orderModel.atv) {
              //permissions.add(PermissionConstants.CANCEL);
            }
          } else if (ApprovalConstants.CANCELLED.equals(approvalMapping.getStatus()) ||
              ApprovalConstants.REJECTED.equals(approvalMapping.getStatus()) ||
              ApprovalConstants.EXPIRED.equals(approvalMapping.getStatus())) {
            if (orderModel.atv) {
              permissions.add(PermissionConstants.ALLOCATE);
              permissions.add(PermissionConstants.CANCEL);
              permissions.add(PermissionConstants.CONFIRM);
              permissions.add(PermissionConstants.EDIT);
            }
          } else if (ApprovalConstants.APPROVED.equals(approvalMapping.getStatus())) {
            if (orderModel.atv) {
              permissions.add(PermissionConstants.ALLOCATE);
              permissions.add(PermissionConstants.SHIP);
              permissions.add(PermissionConstants.CREATE_SHIPMENT);
              permissions.add(PermissionConstants.CONFIRM);
            }
          }
        } else {
          if (orderModel.atv) {
            permissions.add(PermissionConstants.CANCEL);
            permissions.add(PermissionConstants.CONFIRM);
            permissions.add(PermissionConstants.ALLOCATE);
            permissions.add(PermissionConstants.EDIT);
          }
        }
      } else if (IOrder.TRANSFER_ORDER == approvalType) {
        if (approvalMapping != null) {
          if (ApprovalConstants.PENDING.equals(approvalMapping.getStatus())) {
            //permissions.add(PermissionConstants.CANCEL);

          } else if (ApprovalConstants.CANCELLED.equals(approvalMapping.getStatus())
              || ApprovalConstants.REJECTED.equals(approvalMapping.getStatus())
              || ApprovalConstants.EXPIRED.equals(approvalMapping.getStatus())) {
            permissions.add(PermissionConstants.EDIT);
            permissions.add(PermissionConstants.CANCEL);

          } else if (ApprovalConstants.APPROVED.equals(approvalMapping.getStatus())) {
            if (orderModel.atv) {
              permissions.add(PermissionConstants.CONFIRM);
              permissions.add(PermissionConstants.ALLOCATE);
              permissions.add(PermissionConstants.SHIP);
              permissions.add(PermissionConstants.CREATE_SHIPMENT);
            }
          }
        } else {
          permissions.add(PermissionConstants.CANCEL);
          permissions.add(PermissionConstants.EDIT);
        }
      }
    } else {
      permissions.add(PermissionConstants.CANCEL);
      permissions.add(PermissionConstants.CONFIRM);
      permissions.add(PermissionConstants.ALLOCATE);
      permissions.add(PermissionConstants.EDIT);
      permissions.add(PermissionConstants.SHIP);
      permissions.add(PermissionConstants.CREATE_SHIPMENT);
    }
    model.setPermissions(permissions);
    return model;
  }


  /**
   * Gives the types of approval to be shown to the user
   */
  public List<OrderApprovalTypesModel> buildOrderApprovalTypesModel(OrderModel orderModel,
                                                                    OrderManagementService oms)
      throws ServiceException, ObjectNotFoundException {
    List<OrderApprovalTypesModel> models = new ArrayList<>(1);
    boolean isPurchaseApprovalRequired = false;
    boolean isSalesApprovalRequired = false;
    boolean isTransferApprovalRequired = false;
    ApprovalsDao approvalsDao = new ApprovalsDao();
    IOrder order = oms.getOrder(orderModel.id);
    if (IOrder.TRANSFER_ORDER == order.getOrderType() && !orderModel.isVisibleToCustomer()
        && !orderModel.isVisibleToVendor()) {
      isTransferApprovalRequired = orderApprovalsService.isApprovalRequired(order,
          IOrder.TRANSFER_ORDER);
    } else {
      if (IOrder.PURCHASE_ORDER == order.getOrderType() && orderModel.isVisibleToCustomer()
          && orderModel.atc) {
        isPurchaseApprovalRequired =
            orderApprovalsService.isApprovalRequired(order, IOrder.PURCHASE_ORDER);
      }
      if (IOrder.TRANSFER_ORDER != order.getOrderType() && orderModel.isVisibleToVendor() && orderModel.atv) {
        isSalesApprovalRequired =
            orderApprovalsService.isApprovalRequired(order, IOrder.SALES_ORDER);
      }
    }
    if (isTransferApprovalRequired) {
      OrderApprovalTypesModel model = new OrderApprovalTypesModel();
      model.setType(ApprovalConstants.TRANSFER);
      IOrderApprovalMapping
          orderApprovalMapping =
          approvalsDao.getOrderApprovalMapping(order.getOrderId(), IOrder.TRANSFER_ORDER);
      if (orderApprovalMapping != null) {
        model.setId(orderApprovalMapping.getApprovalId());
        List<IOrderApprovalMapping>
            approvalMappings =
            approvalsDao.getTotalOrderApprovalMapping(order.getOrderId());
        if (approvalMappings != null && !approvalMappings.isEmpty()) {
          model.setCount(approvalMappings.size());
        }
      }
      models.add(model);
    } else {
      if (isPurchaseApprovalRequired) {
        OrderApprovalTypesModel model = new OrderApprovalTypesModel();
        model.setType(ApprovalConstants.PURCHASE);
        IOrderApprovalMapping
            orderApprovalMapping =
            approvalsDao.getOrderApprovalMapping(order.getOrderId(), IOrder.PURCHASE_ORDER);
        if (orderApprovalMapping != null) {
          model.setId(orderApprovalMapping.getApprovalId());
          List<IOrderApprovalMapping>
              approvalMappings =
              approvalsDao.getTotalOrderApprovalMapping(order.getOrderId());
          if (approvalMappings != null && !approvalMappings.isEmpty()) {
            model.setCount(approvalMappings.size());
          }
        }
        models.add(model);
      }
      if (isSalesApprovalRequired) {
        OrderApprovalTypesModel model = new OrderApprovalTypesModel();
        model.setType(ApprovalConstants.SALES);
        IOrderApprovalMapping
            orderApprovalMapping =
            approvalsDao.getOrderApprovalMapping(order.getOrderId(), IOrder.SALES_ORDER);
        if (orderApprovalMapping != null) {
          model.setId(orderApprovalMapping.getApprovalId());
          List<IOrderApprovalMapping>
              approvalMappings =
              approvalsDao.getTotalOrderApprovalMapping(order.getOrderId());
          if (approvalMappings != null && !approvalMappings.isEmpty()) {
            model.setCount(approvalMappings.size());
          }
        }
        models.add(model);
      }
    }
    return models;
  }

  public OrderModel buildFullOrderModel(IOrder order, SecureUserDetails user,
                                        Long domainId, String[] embed) throws Exception {
    OrderModel model = buildOrderModel(order, user, domainId);
    includeApprovals(model, order, user, domainId,
        embed != null && Arrays.asList(embed).contains(PERMISSIONS));
    return model;
  }

  private void includeApprovals(OrderModel model, IOrder order, SecureUserDetails user,
                                Long domainId, boolean includePermissions)
      throws ServiceException, ObjectNotFoundException {
    model.setApprovalTypesModels(buildOrderApprovalTypesModel(model,
        Services.getService(OrderManagementServiceImpl.class, SecurityUtils.getLocale())));
    Integer approvalType = orderApprovalsService.getApprovalType(order);
    boolean isApprovalRequired = false;
    if (approvalType != null) {
      model.setApprover(
          buildOrderApproverModel(user.getUsername(), approvalType, domainId, order));
      isApprovalRequired = orderApprovalsService.isApprovalRequired(order, approvalType);
    }
    if (includePermissions) {
      Permissions
          permissions =
          buildPermissionModel(order, model, approvalType, isApprovalRequired);
      model.setPermissions(permissions);
    }
  }

  public OrderModel buildOrderModel(IOrder order, SecureUserDetails user,
                                    Long domainId) throws Exception {
    Map<Long, String> domainNames = new HashMap<>(1);
    OrderModel model = build(order, user, domainId, domainNames);
    DomainConfig dc = DomainConfig.getInstance(domainId);
    EntitiesService as = Services.getService(EntitiesServiceImpl.class, user.getLocale());
    InventoryManagementService
        ims =
        Services.getService(InventoryManagementServiceImpl.class, user.getLocale());
    IKiosk k = null;
    IKiosk vendorKiosk = null;
    Locale locale = user.getLocale();
    MaterialCatalogService
        mcs =
        Services.getService(MaterialCatalogServiceImpl.class, user.getLocale());

    boolean
        showStocks =
        IOrder.PENDING.equals(order.getStatus()) || IOrder.CONFIRMED.equals(order.getStatus())
            || IOrder.BACKORDERED.equals(order.getStatus());
    // to the logged in user
    boolean showVendorStock = dc.autoGI() && order.getServicingKiosk() != null;

    if (order.getServicingKiosk() != null) {
      try {
        vendorKiosk = as.getKiosk(order.getServicingKiosk(), false);
      } catch (Exception e) {
        xLogger.warn("Error when trying to get kiosk {0} and create a new row for order {1}",
            order.getKioskId(), order.getOrderId(), e);
      }
    }

    // Accounting
    // Check if accounting is enabled
    boolean accountingEnabled = dc.isAccountingEnabled();

    // Get the credit limit and amount paid for the customer
    String creditLimitErr = null;
    BigDecimal availableCredit = BigDecimal.ZERO;
    // Get the credit limit and check against receivables
    if (accountingEnabled) {
      try {
        Long customerId = order.getKioskId();
        // Get the credit limit
        if (customerId != null && model.vid != null) {
          availableCredit = Services.getService(AccountingServiceImpl.class, locale)
              .getCreditData(customerId,
                  order.getServicingKiosk(), dc).availabeCredit;
        }
      } catch (Exception e) {
        creditLimitErr = e.getMessage();
      }
    }
    model.setVisibleToCustomer(order.isVisibleToCustomer());
    model.setVisibleToVendor(order.isVisibleToVendor());
    model.avc = availableCredit;
    model.avcerr = creditLimitErr;

    model.lt = order.getLatitude();
    model.ln = order.getLongitude();
    model.ac = order.getGeoAccuracy();

    if (order.getKioskId() != null) {
      try {
        k = as.getKiosk(order.getKioskId(), false);
      } catch (Exception e) {
        xLogger.warn(
            "{0} when trying to get kiosk {1} while fetching order details for order {2}: {3}",
            e.getClass().getName(), order.getKioskId(), order.getOrderId(), e);
      }
    }

    if (k != null) {
      model.elt = k.getLatitude();
      model.eln = k.getLongitude();
    }

    model.tp = order.getTotalPrice();
    model.pst = order.getPriceStatement();

    if (accountingEnabled) {
      model.pd = order.getPaid();
      model.po = order.getPaymentOption();

    }

    UsersService accountsService = Services.getService(UsersServiceImpl.class);
    if (model.uid != null) {
      try {
        IUserAccount orderedBy = accountsService.getUserAccount(model.uid);
        model.unm = orderedBy.getFullName();
      } catch (Exception e) {
        // ignore
      }
    }
    if (order.getOrderType() != null && order.getOrderType() == 0) {
      model.alc = false;
      if (SecurityConstants.ROLE_SUPERUSER.equals(user.getRole()) || user.getUsername()
          .equals(order.getUserId())) {
        model.alc = true;
      } else if ((SecurityConstants.ROLE_DOMAINOWNER.equals(user.getRole()))) {
        if (user.getDomainId().equals(order.getDomainId())) {
          model.alc = true;
        } else {
          Set<Long> rs = DomainsUtil.getDomainParents(order.getDomainId(), true);
          if (rs != null) {
            model.alc = rs.contains(user.getDomainId());
          }
        }
      }
    } else {
      model.alc = true;
    }
    model.tgs = order.getTags(TagUtil.TYPE_ORDER);
    model.rid = order.getReferenceID();
    model.pt = LocalDateUtil.getFormattedMillisInHoursDays(order.getProcessingTime(), locale, true);
    model.dlt =
        LocalDateUtil.getFormattedMillisInHoursDays(order.getDeliveryLeadTime(), locale, true);

    IShipmentService ss = Services.getService(ShipmentService.class);
    List<IShipment> shipments = ss.getShipmentsByOrderId(order.getOrderId());
    Map<Long, Map<String, BigDecimal>> quantityByBatches = null;
    Map<Long, Map<String, DemandBatchMeta>> fQuantityByBatches = null;
    Map<Long, List<ShipmentItemModel>> fReasons = null;
    if (!showStocks) {
      quantityByBatches = new HashMap<>();
      for (IShipment shipment : shipments) {
        boolean isFulfilled = ShipmentStatus.FULFILLED.equals(shipment.getStatus());
        if (isFulfilled && fQuantityByBatches == null) {
          fQuantityByBatches = new HashMap<>();
        }
        if (isFulfilled && fReasons == null) {
          fReasons = new HashMap<>();
        }
        if (ShipmentStatus.SHIPPED.equals(shipment.getStatus()) || isFulfilled) {
          ss.includeShipmentItems(shipment);
          for (IShipmentItem iShipmentItem : shipment.getShipmentItems()) {
            if (iShipmentItem.getShipmentItemBatch() != null
                && iShipmentItem.getShipmentItemBatch().size() > 0) {
              for (IShipmentItemBatch iShipmentItemBatch : iShipmentItem.getShipmentItemBatch()) {
                if (!quantityByBatches.containsKey(iShipmentItem.getMaterialId())) {
                  quantityByBatches
                      .put(iShipmentItem.getMaterialId(), new HashMap<>());
                }
                Map<String, BigDecimal>
                    batches =
                    quantityByBatches.get(iShipmentItem.getMaterialId());
                if (batches.containsKey(iShipmentItemBatch.getBatchId())) {
                  batches.put(iShipmentItemBatch.getBatchId(),
                      batches.get(iShipmentItemBatch.getBatchId())
                          .add(iShipmentItemBatch.getQuantity()));
                } else {
                  batches.put(iShipmentItemBatch.getBatchId(), iShipmentItemBatch.getQuantity());
                }
                if (isFulfilled) {
                  if (!fQuantityByBatches.containsKey(iShipmentItem.getMaterialId())) {
                    fQuantityByBatches
                        .put(iShipmentItem.getMaterialId(), new HashMap<>());
                  }
                  Map<String, DemandBatchMeta>
                      fBatches =
                      fQuantityByBatches.get(iShipmentItem.getMaterialId());
                  if (fBatches.containsKey(iShipmentItemBatch.getBatchId())) {
                    fBatches.get(iShipmentItemBatch.getBatchId()).quantity =
                        fBatches.get(iShipmentItemBatch.getBatchId()).quantity
                            .add(iShipmentItemBatch.getFulfilledQuantity());
                    fBatches.get(iShipmentItemBatch.getBatchId()).bd
                        .add(getShipmentItemBatchBD(shipment.getShipmentId(), iShipmentItemBatch));
                  } else {
                    DemandBatchMeta
                        dMeta =
                        new DemandBatchMeta(iShipmentItemBatch.getFulfilledQuantity());
                    dMeta.bd
                        .add(getShipmentItemBatchBD(shipment.getShipmentId(), iShipmentItemBatch));
                    fBatches.put(iShipmentItemBatch.getBatchId(), dMeta);
                  }
                }
              }
            } else if (isFulfilled) {
              if (!fReasons.containsKey(iShipmentItem.getMaterialId())) {
                fReasons.put(iShipmentItem.getMaterialId(), new ArrayList<>());
              }
              ShipmentItemModel m = new ShipmentItemModel();
              m.sid = shipment.getShipmentId();
              m.q = iShipmentItem.getQuantity();
              m.fq = iShipmentItem.getFulfilledQuantity();
              m.frsn = iShipmentItem.getFulfilledDiscrepancyReason();
              fReasons.get(iShipmentItem.getMaterialId()).add(m);
            }
          }
        }
      }
    }
    IDemandService dms = Services.getService(DemandService.class);
    List<IDemandItem> items = dms.getDemandItems(order.getOrderId());
    if (items != null) {
      Set<DemandModel> modelItems = new TreeSet<>();
      for (IDemandItem item : items) {
        Long mid = item.getMaterialId();
        IMaterial m;
        try {
          m = mcs.getMaterial(item.getMaterialId());
        } catch (Exception e) {
          xLogger.warn("WARNING: " + e.getClass().getName() + " when getting material "
              + item.getMaterialId() + ": " + e.getMessage());
          continue;
        }
        DemandModel itemModel = new DemandModel();
        itemModel.nm = m.getName();
        itemModel.id = mid;
        itemModel.q = item.getQuantity();
        itemModel.p = item.getFormattedPrice();
        itemModel.t = item.getTax();
        itemModel.d = item.getDiscount();
        itemModel.a = CommonUtils.getFormattedPrice(item.computeTotalPrice(false));
        itemModel.isBn = m.isBinaryValued();
        itemModel.isBa =
            (vendorKiosk == null || vendorKiosk.isBatchMgmtEnabled()) && m.isBatchEnabled();
        itemModel.oq = item.getOriginalQuantity();
        itemModel.tx = item.getTax();
        itemModel.rsn = item.getReason();
        itemModel.sdrsn = item.getShippedDiscrepancyReason();
        itemModel.sq = item.getShippedQuantity();
        itemModel.yts = itemModel.q.subtract(itemModel.sq);
        itemModel.isq = item.getInShipmentQuantity();
        itemModel.ytcs = itemModel.q.subtract(itemModel.isq);
        //itemModel.mst = item.getMaterialStatus();
        itemModel.rq = item.getRecommendedOrderQuantity();
        itemModel.fq = item.getFulfilledQuantity();
        itemModel.oastk = BigDecimal.ZERO;
        itemModel.astk = BigDecimal.ZERO;
        itemModel.tm = m.isTemperatureSensitive();
        if (showStocks) {
          List<IInvAllocation> allocationList = ims.getAllocationsByTagMaterial(mid,
              IInvAllocation.Type.ORDER + CharacterConstants.COLON + order.getOrderId());
          for (IInvAllocation ia : allocationList) {
            if (IInvAllocation.Type.ORDER.toString().equals(ia.getType())) {
              if (itemModel.isBa) {
                if (BigUtil.equalsZero(ia.getQuantity())) {
                  continue;
                }
                DemandItemBatchModel batchModel = new DemandItemBatchModel();
                batchModel.q = ia.getQuantity();
                batchModel.id = ia.getBatchId();
                IInvntryBatch
                    b =
                    ims.getInventoryBatch(order.getServicingKiosk(), item.getMaterialId(),
                        batchModel.id, null);
                if (b == null) {
                  b = ims.getInventoryBatch(order.getKioskId(), item.getMaterialId(),
                      batchModel.id, null);
                }
                if (b == null) {
                  xLogger.warn("Error while getting inventory batch for kiosk {0}, material {1}, "
                          + "batch id {2}, order id: {3}", order.getServicingKiosk(),
                      item.getMaterialId(), batchModel.id, order.getOrderId());
                  continue;
                }
                batchModel.e = b.getBatchExpiry() != null ? LocalDateUtil
                    .formatCustom(b.getBatchExpiry(), "dd/MM/yyyy", null) : "";
                batchModel.m = b.getBatchManufacturer();
                batchModel.mdt = b.getBatchManufacturedDate() != null ? LocalDateUtil
                    .formatCustom(b.getBatchManufacturedDate(), "dd/MM/yyyy", null) : "";
                itemModel.astk = itemModel.astk.add(batchModel.q);
                if (itemModel.bts == null) {
                  itemModel.bts = new HashSet<>();
                }
                batchModel.mst = ia.getMaterialStatus();
                itemModel.bts.add(batchModel);
              } else {
                itemModel.astk = ia.getQuantity();
                itemModel.mst = ia.getMaterialStatus();
              }
            }
            itemModel.oastk = itemModel.oastk.add(ia.getQuantity());
          }
        } else {
          if (itemModel.isBa) {
            Map<String, BigDecimal> batchMap = quantityByBatches.get(item.getMaterialId());
            Map<String, DemandBatchMeta> fBatchMap = null;
            if (fQuantityByBatches != null) {
              fBatchMap = fQuantityByBatches.get(item.getMaterialId());
            }
            if (batchMap != null) {
              for (String batchId : batchMap.keySet()) {
                DemandItemBatchModel batchModel = new DemandItemBatchModel();
                batchModel.q = batchMap.get(batchId);
                if (fBatchMap != null && fBatchMap.get(batchId) != null) {
                  batchModel.fq = fBatchMap.get(batchId).quantity;
                  batchModel.bd = fBatchMap.get(batchId).bd;
                }
                batchModel.id = batchId;
                IInvntryBatch
                    b =
                    ims.getInventoryBatch(order.getServicingKiosk(), item.getMaterialId(),
                        batchModel.id, null);
                if (b == null) {
                  b = ims.getInventoryBatch(order.getKioskId(), item.getMaterialId(), batchModel.id,
                      null);
                }
                if (b == null) {
                  xLogger.warn("Error while getting inventory batch for kiosk {0}, material {1}, "
                          + "batch id {2}, order id: {3}", order.getServicingKiosk(),
                      item.getMaterialId(), batchModel.id, order.getOrderId());
                  continue;
                }
                batchModel.e = b.getBatchExpiry() != null ? LocalDateUtil
                    .formatCustom(b.getBatchExpiry(), "dd/MM/yyyy", null) : "";
                batchModel.m = b.getBatchManufacturer();
                batchModel.mdt = b.getBatchManufacturedDate() != null ? LocalDateUtil
                    .formatCustom(b.getBatchManufacturedDate(), "dd/MM/yyyy", null) : "";
                itemModel.astk = itemModel.astk.add(batchModel.q);
                if (itemModel.bts == null) {
                  itemModel.bts = new HashSet<>();
                }
                itemModel.bts.add(batchModel);
              }
            }
          } else {
            if (fReasons != null) {
              itemModel.bd = fReasons.get(item.getMaterialId());
            }
          }
        }
        if (showVendorStock && showStocks) {
          try {
            IInvntry inv = ims.getInventory(order.getServicingKiosk(), mid);
            if (inv != null) {
              itemModel.vs = inv.getStock();
              itemModel.vsavibper = ims.getStockAvailabilityPeriod(inv, dc);
              itemModel.atpstk =
                  inv.getAvailableStock(); //todo: Check Available to promise stock is right??????
              itemModel.itstk = inv.getInTransitStock();
              itemModel.vmax = inv.getMaxStock();
              itemModel.vmin = inv.getReorderLevel();
              IInvntryEvntLog lastEventLog = new InvntryDao().getInvntryEvntLog(inv);
              if (lastEventLog != null) {
                itemModel.vevent = inv.getStockEvent();
              }
            }
          } catch (Exception e) {
            // Its ok for vendor to not have inventory;
          }
        }
        if (showStocks) {
          try {
            IInvntry inv = ims.getInventory(order.getKioskId(), mid);
            if (inv != null) {
              itemModel.stk = inv.getStock();
              itemModel.max = inv.getMaxStock();
              itemModel.min = inv.getReorderLevel();
              IInvntryEvntLog lastEventLog = new InvntryDao().getInvntryEvntLog(inv);
              if (lastEventLog != null) {
                itemModel.event = inv.getStockEvent();
              }
              itemModel.im = inv.getInventoryModel();
              itemModel.eoq = inv.getEconomicOrderQuantity();
              itemModel.csavibper = ims.getStockAvailabilityPeriod(inv, dc);
            }
          } catch (Exception ignored) {
            // ignore
          }
        }
        try {
          IHandlingUnitService hus = Services.getService(HandlingUnitServiceImpl.class);
          Map<String, String> hu = hus.getHandlingUnitDataByMaterialId(mid);
          if (hu != null) {
            itemModel.huQty = new BigDecimal(hu.get(IHandlingUnit.QUANTITY));
            itemModel.huName = hu.get(IHandlingUnit.NAME);
          }
        } catch (Exception e) {
          xLogger.warn("Error while fetching Handling Unit {0}", mid, e);
        }
        modelItems.add(itemModel);

      }

      model.its = modelItems;
    }
    return model;
  }

  private ShipmentItemBatchModel getShipmentItemBatchBD(String shipmentID,
                                                        IShipmentItemBatch iShipmentItemBatch) {
    ShipmentItemBatchModel bd = new ShipmentItemBatchModel();
    bd.fq = iShipmentItemBatch.getFulfilledQuantity();
    bd.frsn = iShipmentItemBatch.getFulfilledDiscrepancyReason();
    bd.sid = shipmentID;
    bd.q = iShipmentItemBatch.getQuantity();
    return bd;
  }

  public OrderResponseModel buildOrderResponseModel(UpdatedOrder updOrder,
                                                    boolean includeOrder, SecureUserDetails sUser,
                                                    Long domainId, boolean isFullOrder,
                                                    String[] embed)
      throws Exception {
    OrderModel order = null;
    if (includeOrder) {
      if (isFullOrder) {
        order = buildFullOrderModel(updOrder.order, sUser, domainId, embed);
      } else {
        order = build(updOrder.order, sUser, domainId, new HashMap<>());
      }
    }
    return new OrderResponseModel(order, updOrder.message, updOrder.inventoryError, null);
  }

  public List<ITransaction> buildTransactionsForNewItems(IOrder order,
                                                         List<DemandModel> demandItemModels) {
    Long domainId = order.getDomainId();
    Long kioskId = order.getKioskId();
    String userId = order.getUserId();
    Date now = new Date();
    List<ITransaction> transactions = new ArrayList<>();
    for (DemandModel demandModel : demandItemModels) {
      IDemandItem item = order.getItem(demandModel.id);
      if (item == null) {
        ITransaction trans = JDOUtils.createInstance(ITransaction.class);
        trans.setDomainId(domainId);
        trans.setKioskId(kioskId);
        trans.setMaterialId(demandModel.id);
        trans.setQuantity(demandModel.q);
        trans.setType(ITransaction.TYPE_REORDER);
        trans.setTrackingId(String.valueOf(order.getOrderId()));
        trans.setSourceUserId(userId);
        trans.setTimestamp(now);
        transactions.add(trans);
      }

    }
    return transactions;
  }


  public IOrder buildOrderMaterials(IOrder order, List<DemandModel> items) {
    for (DemandModel model : items) {
      IDemandItem item = order.getItem(model.id);
      if (item != null) {
        if (BigUtil.notEquals(item.getQuantity(), model.q)) {
          item.setQuantity(model.q);
        }
        if (BigUtil.equals(model.q, model.oq)) {
          item.setOriginalQuantity(model.q);
          item.setShippedDiscrepancyReason(null);
        }
        if (BigUtil.notEquals(item.getDiscount(), model.d)) {
          item.setDiscount(model.d);
        }
        item.setShippedDiscrepancyReason(model.sdrsn);
        item.setReason(model.rsn);
      }
    }
    return order;
  }

  private class DemandBatchMeta {
    public BigDecimal quantity;
    List<ShipmentItemBatchModel> bd = new ArrayList<>();

    DemandBatchMeta(BigDecimal quantity) {
      this.quantity = quantity;
    }
  }

  public OrderApproverModel buildOrderApproverModel(String userId, Integer approvalType,
                                                    Long domainId, IOrder order) {
    OrderApproverModel orderApproverModel = null;

    EntitiesService
        entitiesService =
        Services.getService(EntitiesServiceImpl.class, SecurityUtils.getLocale());
    if (IOrder.TRANSFER_ORDER == approvalType) {
      DomainConfig dc = DomainConfig.getInstance(domainId);
      ApprovalsConfig ac = dc.getApprovalsConfig();
      ApprovalsConfig.OrderConfig orderConfig = ac.getOrderConfig();
      if (orderConfig != null) {
        if (orderConfig.getPrimaryApprovers() != null && !orderConfig.getPrimaryApprovers()
            .isEmpty()) {
          for (String s : orderConfig.getPrimaryApprovers()) {
            if (s.equals(userId)) {
              orderApproverModel = new OrderApproverModel();
              orderApproverModel.setApproverType(IApprovers.PRIMARY_APPROVER);
              orderApproverModel.setOrderType("t");

            }
          }
        }
        if (orderConfig.getSecondaryApprovers() != null && !orderConfig.getSecondaryApprovers()
            .isEmpty()) {
          for (String s : orderConfig.getSecondaryApprovers()) {
            if (s.equals(userId)) {
              orderApproverModel = new OrderApproverModel();
              orderApproverModel.setApproverType(IApprovers.SECONDARY_APPROVER);
              orderApproverModel.setOrderType("t");
            }
          }
        }
      }
    } else {
      Long kioskId = null;
      String oty = "";
      if (IOrder.PURCHASE_ORDER == approvalType) {
        kioskId = order.getKioskId();
        oty = "p";
      } else if (IOrder.SALES_ORDER == approvalType) {
        kioskId = order.getServicingKiosk();
        oty = "s";
      }
      if (kioskId != null) {
        List<IApprovers> approvers = entitiesService.getApprovers(kioskId);
        if (approvers != null && !approvers.isEmpty()) {
          for (IApprovers apr : approvers) {
            if (userId.equals(apr.getUserId()) && apr.getOrderType().equals(oty)) {
              orderApproverModel = new OrderApproverModel();
              orderApproverModel.setApproverType(apr.getType());
              if (IApprovers.PURCHASE_ORDER.equals(apr.getOrderType())) {
                orderApproverModel.setOrderType(apr.getOrderType());
              } else if (IApprovers.SALES_ORDER.equals(apr.getOrderType())) {
                orderApproverModel.setOrderType(apr.getOrderType());
              }
            }
          }
        }
      }
    }
    return orderApproverModel;
  }
}

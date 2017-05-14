package com.logistimo.api.servlets.mobile.builders;

import com.logistimo.config.models.DomainConfig;
import com.logistimo.pagination.Results;
import com.logistimo.services.Services;

import com.logistimo.accounting.models.CreditData;
import com.logistimo.accounting.service.impl.AccountingServiceImpl;
import com.logistimo.activity.entity.IActivity;
import com.logistimo.activity.models.ActivityModel;
import com.logistimo.activity.service.ActivityService;
import com.logistimo.activity.service.impl.ActivityServiceImpl;
import com.logistimo.entities.entity.IKiosk;
import com.logistimo.entities.service.EntitiesService;
import com.logistimo.entities.service.EntitiesServiceImpl;
import com.logistimo.orders.entity.IDemandItem;
import com.logistimo.orders.entity.IOrder;
import com.logistimo.proto.MobileConversationModel;
import com.logistimo.proto.MobileDemandItemModel;
import com.logistimo.proto.MobileOrderModel;
import com.logistimo.proto.MobileOrdersModel;
import com.logistimo.proto.MobileShipmentModel;
import com.logistimo.tags.TagUtil;
import com.logistimo.users.entity.IUserAccount;
import com.logistimo.users.service.UsersService;
import com.logistimo.users.service.impl.UsersServiceImpl;

import com.logistimo.utils.BigUtil;
import com.logistimo.constants.Constants;
import com.logistimo.utils.LocalDateUtil;
import com.logistimo.utils.StringUtil;
import com.logistimo.logger.XLog;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

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
}

package com.logistimo.api.builders;

import com.logistimo.dao.JDOUtils;

import com.logistimo.services.impl.PMF;
import com.logistimo.utils.LocalDateUtil;
import com.logistimo.utils.NumberUtil;
import com.logistimo.logger.XLog;
import com.logistimo.api.models.MarkerModel;
import com.logistimo.entities.entity.IKiosk;
import com.logistimo.inventory.TransactionUtil;
import com.logistimo.inventory.entity.ITransaction;
import com.logistimo.orders.entity.IOrder;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import javax.jdo.PersistenceManager;

/**
 * Created by Mohan Raja on 05/05/15
 */
public class MarkerBuilder {
  private static final XLog xLogger = XLog.getLog(MarkerBuilder.class);
  private String transUid;
  private Date transTimestamp;

  public MarkerModel buildMarkerFromOrder(IOrder order, Locale locale, String timezone) {
    double lat = NumberUtil.getDoubleValue(order.getLatitude());
    double lng = NumberUtil.getDoubleValue(order.getLongitude());
    if (lat == 0 && lng == 0) {
      return null;
    }
    MarkerModel model = new MarkerModel();
    model.latitude = lat;
    model.longitude = lng;
    model.accuracy = NumberUtil.getDoubleValue(order.getGeoAccuracy());
//            tid = order.getOrderId().toString();
    PersistenceManager pm = null;
    try {
      pm = PMF.get().getPersistenceManager();
      IKiosk customer = JDOUtils.getObjectById(IKiosk.class, order.getKioskId(), pm);
      IKiosk vendor = null;
      if (order.getServicingKiosk() != null) {
        vendor = JDOUtils.getObjectById(IKiosk.class, order.getServicingKiosk(), pm);
      }
      StringBuilder title = new StringBuilder();
      title.append(order.size()).append(" item(s) ordered by ").append(customer.getName())
          .append(", ").append(customer.getCity());
      if (vendor != null) {
        title.append(" from ").append(vendor.getName()).append(", ").append(vendor.getCity());
      }
      title.append(" on ").append(LocalDateUtil.format(order.getCreatedOn(), locale, timezone));
      title.append(" [Order: ").append(order.getOrderId()).append("]");
      model.title = title.toString();
    } catch (Exception e) {
      xLogger
          .warn("{0} when getting customer/vendor for order {1} during geo-data acquisition: {2}",
              e.getClass().getName(), order.getOrderId(), e.getMessage());
    } finally {
      if (pm != null) {
        pm.close();
      }
    }
    return model;
  }

  public List<MarkerModel> buildMarkerListFromOrders(List results, Locale locale, String timezone) {
    List<MarkerModel> models = new ArrayList<MarkerModel>();
    for (Object o : results) {
      MarkerModel model = buildMarkerFromOrder((IOrder) o, locale, timezone);
      if (model != null) {
        models.add(model);
      }
    }
    return models;
  }

  public List<MarkerModel> buildMarkerListFromTransactions(List results, Locale locale,
                                                           String timezone) {
    List<MarkerModel> models = new ArrayList<MarkerModel>();
    for (Object o : results) {
      MarkerModel model = buildMarkerFromTransaction((ITransaction) o, locale, timezone);
      if (model != null) {
        models.add(model);
      }
    }
    return models;
  }

  private MarkerModel buildMarkerFromTransaction(ITransaction trans, Locale locale,
                                                 String timezone) {
    double lat = NumberUtil.getDoubleValue(trans.getLatitude());
    double lng = NumberUtil.getDoubleValue(trans.getLongitude());
    if (lat == 0 && lng == 0) {
      return null;
    }
    String thisTransUid = trans.getSourceUserId();
    Date thisTransTimestamp = trans.getTimestamp();
    if (thisTransUid != null && !thisTransUid.isEmpty() && thisTransUid.equals(transUid)
        && thisTransTimestamp != null && thisTransTimestamp.equals(transTimestamp)) {
      return null;
    }
    transUid = thisTransUid;
    transTimestamp = thisTransTimestamp;
    MarkerModel model = new MarkerModel();
    model.latitude = lat;
    model.longitude = lng;
    model.accuracy = NumberUtil.getDoubleValue(trans.getGeoAccuracy());
//        tid = trans.getKeyString();
    PersistenceManager pm = null;
    try {
      pm = PMF.get().getPersistenceManager();
      IKiosk k = JDOUtils.getObjectById(IKiosk.class, trans.getKioskId(), pm);
      IKiosk lk = null;
      if (trans.getLinkedKioskId() != null) {
        lk = JDOUtils.getObjectById(IKiosk.class, trans.getLinkedKioskId(), pm);
      }
      String transType = trans.getType();
      StringBuilder title = new StringBuilder();

      title.append(TransactionUtil.getDisplayName(transType, locale));
      if (ITransaction.TYPE_ISSUE.equals(transType) || ITransaction.TYPE_TRANSFER
          .equals(transType)) {
        if (lk != null) {
          title.append(" to ").append(lk.getName()).append(", ").append(lk.getCity());
        }
        title.append(" from");
      } else if (ITransaction.TYPE_RECEIPT.equals(transType)) {
        if (lk != null) {
          title.append(" from ").append(lk.getName()).append(", ").append(lk.getCity());
        }
        title.append(" to");
      } else {
        title.append(" at");
      }
      title.append(" ").append(k.getName()).append(", ").append(k.getCity()).append(" on ")
          .append(LocalDateUtil.format(trans.getTimestamp(), locale, timezone));
      model.title = title.toString();
    } catch (Exception e) {
      xLogger.warn("{0} when getting kiosk for ID {1} during trans. geo-data acquisition: {2}",
          e.getClass().getName(), trans.getKioskId(), e.getMessage());
    } finally {
      if (pm != null) {
        pm.close();
      }
    }
    return model;
  }
}

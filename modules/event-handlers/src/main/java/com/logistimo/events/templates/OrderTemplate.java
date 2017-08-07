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

package com.logistimo.events.templates;

import com.logistimo.config.models.EventsConfig;
import com.logistimo.conversations.entity.IMessage;
import com.logistimo.conversations.service.ConversationService;
import com.logistimo.conversations.service.impl.ConversationServiceImpl;
import com.logistimo.entities.entity.IKiosk;
import com.logistimo.entities.entity.Kiosk;
import com.logistimo.logger.XLog;
import com.logistimo.materials.service.MaterialCatalogService;
import com.logistimo.materials.service.impl.MaterialCatalogServiceImpl;
import com.logistimo.orders.OrderUtils;
import com.logistimo.orders.entity.IDemandItem;
import com.logistimo.orders.entity.IOrder;
import com.logistimo.orders.service.IDemandService;
import com.logistimo.orders.service.impl.DemandService;
import com.logistimo.services.Services;
import com.logistimo.services.impl.PMF;
import com.logistimo.users.entity.UserAccount;
import com.logistimo.utils.BigUtil;
import com.logistimo.utils.LocalDateUtil;

import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.jdo.PersistenceManager;

/**
 * Created by charan on 10/03/17.
 */
public class OrderTemplate implements ITemplate {
  private static final XLog xLogger = XLog.getLog(OrderTemplate.class);
  private final IOrder order;

  public OrderTemplate(IOrder order) {
    this.order = order;
  }

  // Get template values for an order
  public Map<String, String> getTemplateValues(Locale locale, String timezone,
                                               List<String> excludeVars, Date updationTime) {
    // Get the variable map for SMS message formation
    // NOTE: Order status is deliberately OMITTED so that it can be replaced by the Javascript function
    HashMap<String, String> varMap = new HashMap<String, String>();
    PersistenceManager pm = PMF.get().getPersistenceManager();
    try {
      if (excludeVars == null || !excludeVars.contains(EventsConfig.VAR_CREATIONTIME)) {
        varMap.put(EventsConfig.VAR_CREATIONTIME,
            LocalDateUtil.format(order.getCreatedOn(), locale, timezone));
      }
      IKiosk k = null;
      if (excludeVars == null || !excludeVars.contains(EventsConfig.VAR_CUSTOMER)) {
        k = pm.getObjectById(Kiosk.class, order.getKioskId());
        varMap.put(EventsConfig.VAR_CUSTOMER, k.getName());
      }
      if (excludeVars == null || !excludeVars.contains(EventsConfig.VAR_CUSTOMERCITY)) {
        if (k == null) {
          k = pm.getObjectById(Kiosk.class, order.getKioskId());
        }
        varMap.put(EventsConfig.VAR_CUSTOMERCITY, k.getCity());
      }
      ConversationService cs = Services.getService(ConversationServiceImpl.class, locale);
      IMessage msg = cs.getLastMessage(null, "ORDER", order.getOrderId().toString()); //conversationId is optional
      if (msg != null && msg.getMessage() != null && (excludeVars == null || !excludeVars.contains(EventsConfig.VAR_COMMENT))) {
        if (msg.getMessage().length() > 100) {
          varMap.put(EventsConfig.VAR_COMMENT, "(" + msg.getMessage().substring(0, 98) + "..)");
        }else{
          varMap.put(EventsConfig.VAR_COMMENT, "("+msg.getMessage()+")");
        }
      }
      if (msg != null && msg.getUserId() != null && (excludeVars == null || !excludeVars.contains(EventsConfig.VAR_COMMENTED_BY))) {
        varMap.put(EventsConfig.VAR_COMMENTED_BY, msg.getUserId());
      }
      if (msg != null && msg.getCreateDate() != null && (excludeVars == null || !excludeVars.contains(EventsConfig.VAR_COMMETED_ON))) {
        varMap.put(EventsConfig.VAR_COMMETED_ON, LocalDateUtil.format(msg.getCreateDate(), locale, timezone));
      }
      try {
        if ((excludeVars == null || !excludeVars
            .contains(EventsConfig.VAR_ESTIMATEDDATEOFARRIVAL))) {
          varMap.put(EventsConfig.VAR_ESTIMATEDDATEOFARRIVAL,
              order.getExpectedArrivalDate() != null ? LocalDateUtil.format(order.getExpectedArrivalDate(), null, null, true) : "N/A");
        }
      } catch (Exception e) {
        xLogger.warn("Exception when parsing Estimated date of arrival {0} in order {1}: {2}", order.getExpectedArrivalDate(),
            order.getOrderId(), e.getMessage());
      }
      try {
        if ((excludeVars == null || !excludeVars.contains(EventsConfig.VAR_REQUIREDBYDATE))) {
          varMap.put(EventsConfig.VAR_REQUIREDBYDATE,
              order.getDueDate() != null ? LocalDateUtil.format(order.getDueDate(), null, null, true) : "N/A");
        }
      } catch (Exception e) {
        xLogger.warn("Exception when parsing required by date {0} in order {1}: {2}", order.getDueDate(),
            order.getOrderId(), e.getMessage());
      }
      // Materials
      int size = order.getNumberOfItems();
      if (size > 0 && (excludeVars == null || !excludeVars.contains(EventsConfig.VAR_MATERIALS)
          || !excludeVars.contains(EventsConfig.VAR_MATERIALSWITHMETADATA))) {
        try {
          MaterialCatalogService mcs = Services.getService(MaterialCatalogServiceImpl.class);
          IDemandService ds = Services.getService(DemandService.class);
          List<IDemandItem> items = ds.getDemandItems(order.getOrderId());
          String materialsStr = "", materialsWithMetadataStr = "";
          if (items != null && !items.isEmpty()) {
            boolean skipItems = false; // to avoid large materials list message
            boolean skipItemsQuantities = false; // to avoid large materials with quantities message
            for (IDemandItem item : items) {
              String name = mcs.getMaterial(item.getMaterialId()).getName();
              if (materialsStr.length() < EventsConfig.VAR_MESSAGE_LIMIT && !skipItems) {
                if (!materialsStr.isEmpty()) {
                  materialsStr += ", ";
                }
                materialsStr += name;
              } else if (!skipItems) {
                materialsStr += "... ";
                skipItems = true;
              }
              if (materialsWithMetadataStr.length() < EventsConfig.VAR_MESSAGE_LIMIT && !skipItemsQuantities) {
                if (!materialsWithMetadataStr.isEmpty()) {
                  materialsWithMetadataStr += ", ";
                }
                materialsWithMetadataStr += BigUtil.getFormattedValue(item.getQuantity()) + " " + name;
                String currency = order.getCurrency();
                if (currency == null) {
                  currency = item.getCurrency();
                }
                currency = (currency == null || currency.isEmpty() ? "" : currency + " ");
                if (BigUtil.notEqualsZero(item.getUnitPrice())) {
                  materialsWithMetadataStr +=
                      " (" + currency + BigUtil.getFormattedValue(item.getUnitPrice()) + " ea.)";
                }
              } else if (!skipItemsQuantities) {
                materialsWithMetadataStr += "... ";
                skipItemsQuantities = true;
              }
              if (skipItems && skipItemsQuantities) {
                break;
              }
            } // end while
            varMap.put(EventsConfig.VAR_MATERIALS, materialsStr);
            varMap.put(EventsConfig.VAR_MATERIALSWITHMETADATA, materialsWithMetadataStr);
          }
        } catch (Exception e) {
          xLogger.severe(
              "{0} when trying to get material variables for notificaiton message for order {1}: {2}",
              e.getClass().getName(), order.getOrderId(), e.getMessage());
        }
      }
      if (excludeVars == null || !excludeVars.contains(EventsConfig.VAR_NUMBEROFITEMS)) {
        varMap.put(EventsConfig.VAR_NUMBEROFITEMS, String.valueOf(size));
      }
      if (excludeVars == null || !excludeVars.contains(EventsConfig.VAR_ORDERID)) {
        varMap.put(EventsConfig.VAR_ORDERID, order.getOrderId().toString());
      }
      if (excludeVars == null || !excludeVars.contains(EventsConfig.VAR_ORDERSTATUS)) {
        varMap.put(EventsConfig.VAR_ORDERSTATUS, OrderUtils.getStatusDisplay(order.getStatus(), locale));
      }
      if (excludeVars == null || !excludeVars.contains(EventsConfig.VAR_STATUSCHANGETIME)) {
        Date d = order.getStatusUpdatedOn();
        if (d == null) {
          d = new Date();
        }
        varMap.put(EventsConfig.VAR_STATUSCHANGETIME, LocalDateUtil.format(d, locale, timezone));
      }
//            if (getTransporter() != null && (excludeVars == null || !excludeVars.contains(EventsConfig.VAR_TRANSPORTER))) {
//                    varMap.put(EventsConfig.VAR_TRANSPORTER, getTransporterName());
//            }
      if (order.getServicingKiosk() != null) {
        if (excludeVars == null || !excludeVars.contains(EventsConfig.VAR_VENDOR)) {
          try {
            k = pm.getObjectById(Kiosk.class, order.getServicingKiosk());
            varMap.put(EventsConfig.VAR_VENDOR, k.getName());
          } catch (Exception e) {
            xLogger.warn("Vendor could not be fetched {0} for order:{1} vendor:{2}", e.getMessage(),
                order.getOrderId(), order.getServicingKiosk());
          }
        }
        if (excludeVars == null || !excludeVars.contains(EventsConfig.VAR_VENDORCITY)) {
          if (k == null) {
            k = pm.getObjectById(Kiosk.class, order.getServicingKiosk());
          }
          varMap.put(EventsConfig.VAR_VENDORCITY, k.getCity());
        }
      }
      if (excludeVars == null || !excludeVars.contains(EventsConfig.VAR_UPDATIONTIME)) {
        Date d = order.getUpdatedOn();
        if (d == null) {
          d = updationTime;
        }
        if (d != null) {
          varMap.put(EventsConfig.VAR_UPDATIONTIME, LocalDateUtil.format(d, locale, timezone));
        }
      }
      if (excludeVars == null || !excludeVars.contains(EventsConfig.VAR_PAYMENT)) {
        varMap.put(EventsConfig.VAR_PAYMENT, String.valueOf(order.getPaid()));
      }
      if (order.getUserId() != null && (excludeVars == null || !excludeVars.contains(EventsConfig.VAR_USER))) {
        try {
          varMap.put(EventsConfig.VAR_USER, pm.getObjectById(UserAccount.class, order.getUserId()).getFullName());
        } catch (Exception e) {
          //Ignore if user doesn't exist
        }
      }
      if (order.getUserId() != null && (excludeVars == null || !excludeVars.contains(EventsConfig.VAR_USERID))) {
        varMap.put(EventsConfig.VAR_USERID, order.getUserId());
      }
      // Check if there are any additional order fields
      Map<String, String> fields = order.getFields();
      if (fields != null && !fields.isEmpty()) {
        Iterator<String> it = fields.keySet().iterator();
        while (it.hasNext()) {
          String key = it.next();
          String value = fields.get(key);
          if (value != null) {
            varMap.put("%" + key + "%", value);
          }
        }
      }
    } finally {
      pm.close();
    }
    return varMap;
  }

}

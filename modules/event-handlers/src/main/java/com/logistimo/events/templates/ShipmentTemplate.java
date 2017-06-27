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
import com.logistimo.services.Services;
import com.logistimo.services.impl.PMF;
import com.logistimo.shipments.entity.IShipment;
import com.logistimo.shipments.entity.IShipmentItem;
import com.logistimo.shipments.service.impl.ShipmentService;
import com.logistimo.users.entity.UserAccount;
import com.logistimo.utils.BigUtil;
import com.logistimo.utils.LocalDateUtil;

import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.jdo.PersistenceManager;

/**
 * Created by charan on 10/03/17.
 */
public class ShipmentTemplate implements ITemplate {

  private static final XLog xLogger = XLog.getLog(ShipmentTemplate.class);

  private final IShipment shipment;

  public ShipmentTemplate(IShipment shipment) {
    this.shipment = shipment;
  }

  @Override
  public Map<String, String> getTemplateValues(Locale locale, String timezone,
                                               List<String> excludeVars, Date updateTime) {
    // Get the variable map for SMS message formation
    // NOTE: Order status is deliberately OMITTED so that it can be replaced by the Javascript function
    HashMap<String, String> varMap = new HashMap<String, String>();
    PersistenceManager pm = PMF.get().getPersistenceManager();
    try {
      if (excludeVars == null || !excludeVars.contains(EventsConfig.VAR_CREATIONTIME)) {
        varMap.put(EventsConfig.VAR_CREATIONTIME,
            LocalDateUtil.format(shipment.getCreatedOn(), locale, timezone));
      }
      IKiosk k = null;
      if (excludeVars == null || !excludeVars.contains(EventsConfig.VAR_CUSTOMER)) {
        k = pm.getObjectById(Kiosk.class, shipment.getKioskId());
        varMap.put(EventsConfig.VAR_CUSTOMER, k.getName());
      }
      if (excludeVars == null || !excludeVars.contains(EventsConfig.VAR_CUSTOMERCITY)) {
        if (k == null) {
          k = pm.getObjectById(Kiosk.class, shipment.getKioskId());
        }
        varMap.put(EventsConfig.VAR_CUSTOMERCITY, k.getCity());
      }
      try {
        if ((excludeVars == null || !excludeVars
            .contains(EventsConfig.VAR_ESTIMATEDDATEOFARRIVAL))) {
          varMap.put(EventsConfig.VAR_ESTIMATEDDATEOFARRIVAL,
              shipment.getExpectedArrivalDate() != null ? LocalDateUtil.format(shipment.getExpectedArrivalDate(), null, null, true) : "N/A");
        }
      } catch (Exception e) {
        xLogger.warn("Exception when parsing Estimated date of arrival {0} in order {1}: {2}", shipment.getExpectedArrivalDate(),
            shipment.getOrderId(), e);
      }

      // Materials
      int size = shipment.getNumberOfItems();
      if (size > 0 && (excludeVars == null || !excludeVars.contains(EventsConfig.VAR_MATERIALS)
          || !excludeVars.contains(EventsConfig.VAR_MATERIALSWITHQUANTITIES) || !excludeVars.contains(EventsConfig.VAR_MATERIALSWITHALLOCATED)
          || !excludeVars.contains(EventsConfig.VAR_MATERIALSWITHFULFILED))) {
        try {
          MaterialCatalogService mcs = Services.getService(MaterialCatalogServiceImpl.class);
          //IDemandService ds = Services.getService(DemandService.class);
          //List<IDemandItem> items = ds.getDemandItems(id);
          ShipmentService sS = Services.getService(ShipmentService.class);
          String materialsStr = "", materialsWithQuantityStr = "", materialsWithAllocatedQuantityStr = "", materialsWithFulfiledQuantityStr="";
          sS.includeShipmentItems(shipment);
          BigDecimal allocated;
          //List<IShipmentItem> items = (List<IShipmentItem>) getShipmentItems();
          if (shipment.getShipmentItems() != null && !shipment.getShipmentItems().isEmpty()) {
            IShipmentItem item;
            boolean skipItems = false; // to avoid large materials list message
            boolean skipItemsQuantities = false; // to avoid large materials with quantities message
            for (int i = 0; i < shipment.getShipmentItems().size(); i++) {
              item = shipment.getShipmentItems().get(i);
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
              if (materialsWithQuantityStr.length() < EventsConfig.VAR_MESSAGE_LIMIT && !skipItemsQuantities) {
                if (!materialsWithQuantityStr.isEmpty()) {
                  materialsWithQuantityStr += ", ";
                  materialsWithAllocatedQuantityStr += ", ";
                  materialsWithFulfiledQuantityStr += ", ";
                }
                materialsWithQuantityStr += BigUtil.getFormattedValue(item.getQuantity()) + "-" + name;
                materialsWithFulfiledQuantityStr += BigUtil.getFormattedValue(item.getFulfilledQuantity()) + "-" + name;

                allocated = sS.getAllocatedQuantityForShipmentItem(item.getShipmentId(), shipment.getServicingKiosk(), item.getMaterialId());
                String allc = (allocated == null) ? "0" : BigUtil.getFormattedValue(allocated);
                materialsWithAllocatedQuantityStr += allc + "-" + name;
              } else if (!skipItemsQuantities) {
                materialsWithQuantityStr += "... ";
                materialsWithAllocatedQuantityStr += "... ";
                materialsWithFulfiledQuantityStr += "... ";
                skipItemsQuantities = true;
              }
              if (skipItems && skipItemsQuantities) {
                break;
              }
            }
            varMap.put(EventsConfig.VAR_MATERIALS, materialsStr);
            varMap.put(EventsConfig.VAR_MATERIALSWITHQUANTITIES, materialsWithQuantityStr);
            varMap.put(EventsConfig.VAR_MATERIALSWITHALLOCATED, materialsWithAllocatedQuantityStr);
            varMap.put(EventsConfig.VAR_MATERIALSWITHFULFILED, materialsWithFulfiledQuantityStr);
          }
        } catch (Exception e) {
          xLogger.severe(
              "{0} when trying to get material variables for notificaiton message for shipment {1}: {2}",
              e.getClass().getName(), shipment.getShipmentId(), e.getMessage(),e);
        }
      }
      if (excludeVars == null || !excludeVars.contains(EventsConfig.VAR_NUMBEROFITEMS)) {
        varMap.put(EventsConfig.VAR_NUMBEROFITEMS, String.valueOf(size));
      }
      if (excludeVars == null || !excludeVars.contains(EventsConfig.VAR_ORDERID)) {
        varMap.put(EventsConfig.VAR_ORDERID, shipment.getOrderId().toString());
      }
      if (excludeVars == null || !excludeVars.contains(EventsConfig.VAR_SHIPMENTID)) {
        varMap.put(EventsConfig.VAR_SHIPMENTID, shipment.getShipmentId().toString());
      }
      if (shipment.getTransporter() != null && (excludeVars == null || !excludeVars.contains(EventsConfig.VAR_TRANSPORTER))) {
        varMap.put(EventsConfig.VAR_TRANSPORTER, shipment.getTransporter());
      }
      if (shipment.getServicingKiosk() != null) {
        if (excludeVars == null || !excludeVars.contains(EventsConfig.VAR_VENDOR)) {
          try {
            k = pm.getObjectById(Kiosk.class, shipment.getServicingKiosk());
            varMap.put(EventsConfig.VAR_VENDOR, k.getName());
          } catch (Exception e) {
            xLogger.warn("Vendor could not be fetched {0} for shipment:{1} vendor:{2}", e.getMessage(),
                shipment.getShipmentId(), shipment.getServicingKiosk());
          }
        }
        if (excludeVars == null || !excludeVars.contains(EventsConfig.VAR_VENDORCITY)) {
          if (k == null) {
            k = pm.getObjectById(Kiosk.class, shipment.getServicingKiosk());
          }
          varMap.put(EventsConfig.VAR_VENDORCITY, k.getCity());
        }
      }
      if (excludeVars == null || !excludeVars.contains(EventsConfig.VAR_UPDATIONTIME)) {
        Date d = shipment.getUpdatedOn();
        if (d == null) {
          d = updateTime;
        }
        if (d != null) {
          varMap.put(EventsConfig.VAR_UPDATIONTIME, LocalDateUtil.format(d, locale, timezone));
        }
      }
      ConversationService cs = Services.getService(ConversationServiceImpl.class, locale);
      IMessage msg = cs.getLastMessage(null, "SHIPMENT", shipment.getShipmentId()); //conversationId is optional
      if (msg != null && msg.getMessage() != null && (excludeVars == null || !excludeVars.contains(EventsConfig.VAR_COMMENT))) {
        if(msg.getMessage().length()>20){
          varMap.put(EventsConfig.VAR_COMMENT, "("+msg.getMessage().substring(0,30)+"..)");
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
      if (shipment.getCreatedBy() != null && (excludeVars == null || !excludeVars.contains(EventsConfig.VAR_USER))) {
        try {
          varMap.put(EventsConfig.VAR_USER, pm.getObjectById(UserAccount.class, shipment.getCreatedBy()).getFullName());
        } catch (Exception e) {
          //Ignore if user doesn't exist
        }
      }
      if (shipment.getCreatedBy() != null && (excludeVars == null || !excludeVars.contains(EventsConfig.VAR_USERID))) {
        varMap.put(EventsConfig.VAR_USERID, shipment.getCreatedBy());
      }
      if (shipment.getStatus() != null && (excludeVars == null || !excludeVars.contains(EventsConfig.VAR_SHIPMENTSTATUS))) {
        varMap.put(EventsConfig.VAR_SHIPMENTSTATUS, OrderUtils
            .getShipmentStatusDisplay(shipment.getStatus(), locale));
      }
      if (shipment.getUpdatedBy() != null && (excludeVars == null || !excludeVars.contains(EventsConfig.VAR_UPDATEDBY))) {
        varMap.put(EventsConfig.VAR_UPDATEDBY, shipment.getUpdatedBy());
      }
      if (shipment.getTrackingId() != null && (excludeVars == null || !excludeVars.contains(EventsConfig.VAR_TRACKINGID))) {
        varMap.put(EventsConfig.VAR_TRACKINGID, shipment.getTrackingId());
      }
      if (shipment.getPackageSize() != null && (excludeVars == null || !excludeVars.contains(EventsConfig.VAR_PACKAGESIZE))) {
        varMap.put(EventsConfig.VAR_PACKAGESIZE, shipment.getPackageSize());
      }
      if (shipment.getReason() != null && (excludeVars == null || !excludeVars.contains(EventsConfig.VAR_REASONSHIPMENT))) {
        varMap.put(EventsConfig.VAR_REASONSHIPMENT, shipment.getReason());
      }
      if (shipment.getCancelledDiscrepancyReasons() != null && (excludeVars == null || !excludeVars.contains(EventsConfig.VAR_REASONCANCEL))) {
        varMap.put(EventsConfig.VAR_REASONCANCEL, shipment.getCancelledDiscrepancyReasons());
      }
    } finally {
      pm.close();
    }
    return varMap;
  }

}

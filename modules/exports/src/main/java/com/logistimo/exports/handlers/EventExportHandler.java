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

package com.logistimo.exports.handlers;

import com.logistimo.accounting.entity.IAccount;
import com.logistimo.assets.entity.IAsset;
import com.logistimo.assets.entity.IAssetRelation;
import com.logistimo.assets.entity.IAssetStatus;
import com.logistimo.config.models.DomainConfig;
import com.logistimo.constants.Constants;
import com.logistimo.dao.JDOUtils;
import com.logistimo.entities.entity.IKiosk;
import com.logistimo.entities.entity.Kiosk;
import com.logistimo.events.entity.Event;
import com.logistimo.events.entity.IEvent;
import com.logistimo.events.generators.EventGeneratorFactory;
import com.logistimo.events.models.ObjectData;
import com.logistimo.events.processor.EventPublisher;
import com.logistimo.inventory.entity.IInvntry;
import com.logistimo.inventory.entity.IInvntryBatch;
import com.logistimo.inventory.entity.ITransaction;
import com.logistimo.logger.XLog;
import com.logistimo.materials.entity.IMaterial;
import com.logistimo.materials.entity.Material;
import com.logistimo.orders.entity.IOrder;
import com.logistimo.services.Resources;
import com.logistimo.services.impl.PMF;
import com.logistimo.users.entity.IUserAccount;
import com.logistimo.users.entity.UserAccount;
import com.logistimo.utils.LocalDateUtil;

import org.apache.commons.lang.StringEscapeUtils;

import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

import javax.jdo.PersistenceManager;

/**
 * Created by charan on 08/03/17.
 */
public class EventExportHandler implements IExportHandler {

  private static final XLog xLogger = XLog.getLog(InvntryExportHandler.class);

  IEvent event;

  public EventExportHandler(IEvent event){
    this.event = event;
  }

  @Override
  public String getCSVHeader(Locale locale, DomainConfig dc, String type) {
    return "event,type,parameters,time,object-id,entity,material,user";
  }

  @Override
  public String toCSV(Locale locale, String timezone, DomainConfig dc, String type) {
    String str = "";
    PersistenceManager pm = PMF.get().getPersistenceManager();
    try {
      ObjectData odata = null;
      // Get the object associated with the event
      if (event.getObjectId() != null) {

        try {
          xLogger.fine("Event = {0}:{2}. Old Key id = {1}", event.getObjectType(),
              event.getObjectId(), event.getId());
          Object o = event.getObject(pm);
          odata = EventGeneratorFactory.getEventGenerator(event.getDomainId(),
              event.getObjectType()).getObjectData(o, pm);
        } catch (Exception e) {
          xLogger.warn(
              "{0} when getting object using oldKey for event {1}:{2} on object {3} in domain {4}: {5}",
              e.getClass().getName(), event.getObjectType(), event.getId(),
              event.getObjectId(), event.getDomainId(), e.getMessage());
        }
      }
      // Form the CSV string
      str +=
          EventPublisher.getEventName(event.getId(), locale) + "," +
              getEventObjectType(event.getObjectType(), locale)
              + "," +
              (event.getParams() != null ? StringEscapeUtils.escapeCsv(
                  Event.getParamsString((Map<String, Object>) event.getParams())) : "") + "," +
              LocalDateUtil.formatCustom(event.getTimestamp(),
                  Constants.DATETIME_CSV_FORMAT, timezone) + "," +
              (odata != null && odata.oid != null ? odata.oid : "") + ",";
      if (odata != null && odata.kioskId != null) {
        str += "\"" + pm.getObjectById(Kiosk.class, odata.kioskId).getName() + "\"";
      }
      str += ",";
      if (odata != null && odata.materialId != null) {
        str += pm.getObjectById(Material.class, odata.materialId).getName();
      }
      str += ",";
      if (odata != null && odata.userId != null) {
        str += pm.getObjectById(UserAccount.class, odata.userId).getFullName();
      }
    } catch (Exception e) {
      xLogger.warn("{0} when getting CSV for event {1}:{2} on object {3} in domain {4}: {5}",
          e.getClass().getName(), event.getObjectType(), event.getId(), event.getObjectId(),
          event.getDomainId(), e.getMessage());
    } finally {
      pm.close();
    }
    return str;
  }

  public static String getEventObjectType(String oty, Locale locale) {
    ResourceBundle messages = Resources.get().getBundle("Messages", locale);
    if (JDOUtils.getImplClassName(IOrder.class).equals(oty)) {
      return messages.getString("order");
    } else if (JDOUtils.getImplClassName(IInvntry.class).equals(oty)) {
      return messages.getString("inventory");
    } else if (JDOUtils.getImplClassName(ITransaction.class).equals(oty)) {
      return messages.getString("transaction");
    } else if (JDOUtils.getImplClassName(IUserAccount.class).equals(oty)) {
      return messages.getString("user");
    } else if (JDOUtils.getImplClassName(IMaterial.class).equals(oty)) {
      return messages.getString("material");
    } else if (JDOUtils.getImplClassName(IKiosk.class).equals(oty)) {
      return messages.getString("kiosk");
    } else if (JDOUtils.getImplClassName(IAccount.class).equals(oty)) {
      return messages.getString("accounting");
    } else if (JDOUtils.getImplClassName(IAssetStatus.class).equals(oty)) {
      return messages.getString("asset") + " " + messages.getString("status");
    } else if (JDOUtils.getImplClassName(IInvntryBatch.class).equals(oty)) {
      return messages.getString("inventory") + " " + messages.getString("batch");
    } else if (JDOUtils.getImplClassName(IAssetRelation.class).equals(oty)) {
      return messages.getString("asset") + " " + messages.getString("relationship");
    } else if (JDOUtils.getImplClassName(IAsset.class).equals(oty)) {
      return messages.getString("assets");
    }
    else {
      return "Unknown";
    }
  }
}

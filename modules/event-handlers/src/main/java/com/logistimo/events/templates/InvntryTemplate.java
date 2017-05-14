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
import com.logistimo.entities.entity.IKiosk;
import com.logistimo.entities.entity.Kiosk;
import com.logistimo.events.processor.EventPublisher;
import com.logistimo.inventory.entity.IInvntry;
import com.logistimo.inventory.entity.IInvntryEvntLog;
import com.logistimo.inventory.entity.InvntryEvntLog;
import com.logistimo.logger.XLog;
import com.logistimo.materials.entity.Material;
import com.logistimo.services.impl.PMF;
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
public class InvntryTemplate implements ITemplate {
  private static final XLog xLogger = XLog.getLog(InvntryTemplate.class);

  private final IInvntry invntry;

  public InvntryTemplate(IInvntry invntry) {
    this.invntry = invntry;
  }

  // Get template values for inventory transactions
  public Map<String, String> getTemplateValues(Locale locale, String timezone,
                                               List<String> excludeVars, Date updationTime) {
    // Get the variable map for SMS message formation
    // NOTE: Order status is deliberately OMITTED so that it can be replaced by the Javascript function
    HashMap<String, String> varMap = new HashMap<String, String>();
    PersistenceManager pm = PMF.get().getPersistenceManager();
    try {
      // Entity
      IKiosk k = null;
      if (excludeVars == null || !excludeVars.contains(EventsConfig.VAR_CUSTOMER)) {
        k = pm.getObjectById(Kiosk.class, invntry.getKioskId());
        varMap.put(EventsConfig.VAR_CUSTOMER, k.getName());
      }
      if (excludeVars == null || !excludeVars.contains(EventsConfig.VAR_CUSTOMERCITY)) {
        if (k == null) {
          k = pm.getObjectById(Kiosk.class, invntry.getKioskId());
        }
        varMap.put(EventsConfig.VAR_CUSTOMERCITY, k.getCity());
      }
      if (excludeVars == null || !excludeVars.contains(EventsConfig.VAR_ENTITY)) {
        if (k == null) {
          k = pm.getObjectById(Kiosk.class, invntry.getKioskId());
        }
        varMap.put(EventsConfig.VAR_ENTITY, k.getName());
      }
      if (excludeVars == null || !excludeVars.contains(EventsConfig.VAR_ENTITYCITY)) {
        if (k == null) {
          k = pm.getObjectById(Kiosk.class, invntry.getKioskId());
        }
        varMap.put(EventsConfig.VAR_ENTITYCITY, k.getCity());
      }
      // Material
      if (excludeVars == null || !excludeVars.contains(EventsConfig.VAR_MATERIAL)) {
        String name = pm.getObjectById(Material.class, invntry.getMaterialId()).getName();
        varMap.put(EventsConfig.VAR_MATERIAL, name);
      }
      // Quantity
      if (excludeVars == null || !excludeVars.contains(EventsConfig.VAR_QUANTITY)) {
        varMap.put(EventsConfig.VAR_QUANTITY, BigUtil.getFormattedValue(invntry.getStock()));
      }
      BigDecimal safeStock = invntry.getNormalizedSafetyStock();
      if (excludeVars == null || !excludeVars.contains(EventsConfig.VAR_SAFETYSTOCK)) {
        varMap.put(EventsConfig.VAR_SAFETYSTOCK, BigUtil.getFormattedValue(safeStock));
      }
      if (excludeVars == null || !excludeVars.contains(EventsConfig.VAR_CREATIONTIME)) {
        varMap.put(EventsConfig.VAR_CREATIONTIME, LocalDateUtil.format(invntry.getTimestamp(), locale, timezone));
      }
      if (updationTime != null && (excludeVars == null || !excludeVars
          .contains(EventsConfig.VAR_UPDATIONTIME))) {
        varMap.put(EventsConfig.VAR_UPDATIONTIME,
            LocalDateUtil.format(updationTime, locale, timezone));
      }
      // Min./Max
      if (BigUtil.notEqualsZero(invntry.getReorderLevel()) && (excludeVars == null || !excludeVars
          .contains(EventsConfig.VAR_MINSTOCK))) {
        varMap.put(EventsConfig.VAR_MINSTOCK, BigUtil.getFormattedValue(invntry.getReorderLevel()));
      }
      if (BigUtil.notEqualsZero(invntry.getMaxStock()) && (excludeVars == null || !excludeVars
          .contains(EventsConfig.VAR_MAXSTOCK))) {
        varMap.put(EventsConfig.VAR_MAXSTOCK, BigUtil.getFormattedValue(invntry.getMaxStock()));
      }
      // Check if the abnormal stock event duration exists, if so fill this up
      if (invntry.getLastStockEvent() != null && (excludeVars == null || !(
          excludeVars.contains(EventsConfig.VAR_ABNORMALSTOCKDURATION) && excludeVars
              .contains(EventsConfig.VAR_ABNORMALSTOCKEVENT)))) {
        // Get last stock event
        try {
          IInvntryEvntLog invEventLog = pm.getObjectById(InvntryEvntLog.class, invntry.getLastStockEvent());
          Date start = invEventLog.getStartDate();
          Date end = invEventLog.getEndDate();
          int eventType = invEventLog.getType();
          if (end == null) {
            end = new Date();
          }
          if (start != null && (excludeVars == null || !excludeVars
              .contains(EventsConfig.VAR_ABNORMALSTOCKDURATION))) {
            varMap.put(EventsConfig.VAR_ABNORMALSTOCKDURATION, LocalDateUtil
                .getFormattedMillisInHoursDays((end.getTime() - start.getTime()), locale));
          }
          if (excludeVars == null || excludeVars.contains(EventsConfig.VAR_ABNORMALSTOCKEVENT)) {
            varMap.put(EventsConfig.VAR_ABNORMALSTOCKEVENT,
                EventPublisher.getEventName(eventType, locale));
          }
        } catch (Exception e) {

          xLogger.warn("{0} when getting last stock event for inv. {1} in domain {2}: {3}",
              e.getClass().getName(), invntry.getKey(), invntry.getDomainId(), e.getMessage());
        }
      }
      if(invntry.getUpdatedBy() != null) {
        varMap.put(EventsConfig.VAR_REGISTEREDBY, invntry.getUpdatedBy());
        varMap.put(EventsConfig.VAR_UPDATEDBY, invntry.getUpdatedBy());
      }

    } finally {
      pm.close();
    }
    return varMap;
  }
}

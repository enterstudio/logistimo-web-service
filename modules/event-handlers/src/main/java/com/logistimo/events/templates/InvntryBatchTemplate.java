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
import com.logistimo.inventory.entity.IInvntryBatch;
import com.logistimo.materials.entity.Material;
import com.logistimo.services.impl.PMF;
import com.logistimo.utils.BigUtil;
import com.logistimo.utils.LocalDateUtil;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.jdo.PersistenceManager;


/**
 * Created by charan on 10/03/17.
 */
public class InvntryBatchTemplate implements ITemplate {

  private final IInvntryBatch invntryBatch;

  public InvntryBatchTemplate(IInvntryBatch invntryBatch) {
    this.invntryBatch = invntryBatch;
  }

  @Override
  public Map<String, String> getTemplateValues(Locale locale, String timezone,
                                               List<String> excludeVars, Date updationTime) {
    HashMap<String, String> varMap = new HashMap<String, String>();
    PersistenceManager pm = PMF.get().getPersistenceManager();
    try {
      // Entity
      IKiosk k = null;
      if (excludeVars == null || !excludeVars.contains(EventsConfig.VAR_CUSTOMER)) {
        k = pm.getObjectById(Kiosk.class, invntryBatch.getKioskId());
        varMap.put(EventsConfig.VAR_CUSTOMER, k.getName());
      }
      if (excludeVars == null || !excludeVars.contains(EventsConfig.VAR_CUSTOMERCITY)) {
        if (k == null) {
          k = pm.getObjectById(Kiosk.class, invntryBatch.getKioskId());
        }
        varMap.put(EventsConfig.VAR_CUSTOMERCITY, k.getCity());
      }
      if (excludeVars == null || !excludeVars.contains(EventsConfig.VAR_ENTITY)) {
        if (k == null) {
          k = pm.getObjectById(Kiosk.class, invntryBatch.getKioskId());
        }
        varMap.put(EventsConfig.VAR_ENTITY, k.getName());
      }
      if (excludeVars == null || !excludeVars.contains(EventsConfig.VAR_ENTITYCITY)) {
        if (k == null) {
          k = pm.getObjectById(Kiosk.class, invntryBatch.getKioskId());
        }
        varMap.put(EventsConfig.VAR_ENTITYCITY, k.getCity());
      }
      // Material
      if (excludeVars == null || !excludeVars.contains(EventsConfig.VAR_MATERIAL)) {
        String name = pm.getObjectById(Material.class, invntryBatch.getMaterialId()).getName();
        varMap.put(EventsConfig.VAR_MATERIAL, name);
      }
      // Batch
      if (excludeVars == null || !excludeVars.contains(EventsConfig.VAR_BATCHNUMBER)) {
        varMap.put(EventsConfig.VAR_BATCHNUMBER, invntryBatch.getBatchId());
      }
      // Quantity
      if (excludeVars == null || !excludeVars.contains(EventsConfig.VAR_QUANTITY)) {
        varMap.put(EventsConfig.VAR_QUANTITY, BigUtil.getFormattedValue(invntryBatch.getQuantity()));
      }
      // Expiry
      if (excludeVars == null || !excludeVars.contains(EventsConfig.VAR_BATCHEXPIRY)) {
        varMap.put(EventsConfig.VAR_BATCHEXPIRY, LocalDateUtil.format(invntryBatch.getBatchExpiry(), locale, null, true));
      }
      // Created / last updated
      if (excludeVars == null || !excludeVars.contains(EventsConfig.VAR_CREATIONTIME)) {
        varMap.put(EventsConfig.VAR_CREATIONTIME, LocalDateUtil.format(invntryBatch.getTimestamp(), locale, timezone));
      }
      Date updTime = (updationTime == null ? invntryBatch.getTimestamp() : updationTime);
      if (excludeVars == null || !excludeVars.contains(EventsConfig.VAR_UPDATIONTIME)) {
        varMap.put(EventsConfig.VAR_UPDATIONTIME, LocalDateUtil.format(updTime, locale, timezone));
      }
    } finally {
      pm.close();
    }
    return varMap;
  }
}

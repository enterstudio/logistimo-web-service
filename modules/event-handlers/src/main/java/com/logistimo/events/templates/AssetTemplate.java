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

import com.logistimo.assets.AssetUtil;
import com.logistimo.assets.entity.IAsset;
import com.logistimo.config.models.AssetConfig;
import com.logistimo.config.models.AssetSystemConfig;
import com.logistimo.config.models.DomainConfig;
import com.logistimo.config.models.EventsConfig;
import com.logistimo.entities.entity.IKiosk;
import com.logistimo.entities.entity.Kiosk;
import com.logistimo.entities.service.EntitiesService;
import com.logistimo.entities.service.EntitiesServiceImpl;
import com.logistimo.logger.XLog;
import com.logistimo.services.ServiceException;
import com.logistimo.services.Services;
import com.logistimo.services.impl.PMF;
import com.logistimo.users.entity.IUserAccount;
import com.logistimo.utils.LocalDateUtil;
import com.logistimo.utils.StringUtil;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.jdo.PersistenceManager;

/**
 * Created by charan on 10/03/17.
 */
public class AssetTemplate implements ITemplate {

  private static final XLog xLogger = XLog.getLog(AssetTemplate.class);


  private final IAsset asset;

  public AssetTemplate(IAsset asset) {
    this.asset = asset;
  }

  @Override
  public Map<String, String> getTemplateValues(Locale locale, String timezone,
                                               List<String> excludeVars, Date updationTime) {
    HashMap<String, String> varMap = new HashMap<String, String>();
    PersistenceManager pm = PMF.get().getPersistenceManager();
    try {
      // Entity
      IKiosk k = null;
      EntitiesService as = null;
      try {
        as = Services.getService(EntitiesServiceImpl.class, locale);
      } catch (ServiceException e) {
        //ignore
      }

      if (as != null) {
        try {
          List<IUserAccount> users = as.getUsersForKiosk(asset.getKioskId(), null, pm).getResults();
          if (users != null) {
            List<String> userIds = new ArrayList<String>();
            List<String> userNames = new ArrayList<String>();
            List<String> userPhones = new ArrayList<String>();
            for (IUserAccount user : users) {
              userNames.add(user.getFullName());
              userIds.add(user.getUserId());
              userPhones.add(user.getMobilePhoneNumber());
            }

            if (excludeVars == null || !excludeVars.contains(EventsConfig.VAR_USER)) {
              varMap.put(EventsConfig.VAR_USER, StringUtil.getCSV(userNames));
            }

            if (excludeVars == null || !excludeVars.contains(EventsConfig.VAR_USERID)) {
              varMap.put(EventsConfig.VAR_USERID, StringUtil.getCSV(userIds));
            }

            if (excludeVars == null || !excludeVars.contains(EventsConfig.VAR_MOBILEPHONE)) {
              varMap.put(EventsConfig.VAR_MOBILEPHONE, StringUtil.getCSV(userPhones));
            }
          }
        } catch (ServiceException e) {
          //ignore
        }
      }

      if (asset.getKioskId() != null) {
        if (excludeVars == null || !excludeVars.contains(EventsConfig.VAR_CUSTOMER)) {
          k = pm.getObjectById(Kiosk.class, asset.getKioskId());
          varMap.put(EventsConfig.VAR_CUSTOMER, k.getName());
        }
        if (excludeVars == null || !excludeVars.contains(EventsConfig.VAR_CUSTOMERCITY)) {
          if (k == null) {
            k = pm.getObjectById(Kiosk.class, asset.getKioskId());
          }
          varMap.put(EventsConfig.VAR_CUSTOMERCITY, k.getCity());
        }
        if (excludeVars == null || !excludeVars.contains(EventsConfig.VAR_ENTITY)) {
          if (k == null) {
            k = pm.getObjectById(Kiosk.class, asset.getKioskId());
          }
          varMap.put(EventsConfig.VAR_ENTITY, k.getName());
        }
        if (excludeVars == null || !excludeVars.contains(EventsConfig.VAR_ENTITYCITY)) {
          if (k == null) {
            k = pm.getObjectById(Kiosk.class, asset.getKioskId());
          }
          varMap.put(EventsConfig.VAR_ENTITYCITY, k.getCity());
        }
      }

      // Serial number
      if (excludeVars == null || !excludeVars.contains(EventsConfig.VAR_SERIALNUMBER)) {
        varMap.put(EventsConfig.VAR_SERIALNUMBER, asset.getSerialId());
      }

      if (excludeVars == null || !excludeVars.contains(EventsConfig.VAR_DEVICEID)) {
        varMap.put(EventsConfig.VAR_DEVICEID, asset.getSerialId());
      }

      if (excludeVars == null || !excludeVars.contains(EventsConfig.VAR_MODEL)) {
        varMap.put(EventsConfig.VAR_MODEL, asset.getModel());
      }
      if(excludeVars == null || !excludeVars.contains(EventsConfig.VAR_MANUFACTURE_YEAR)) {
        varMap.put(EventsConfig.VAR_MANUFACTURE_YEAR, (asset.getYom() == null) ? NOT_AVAILABLE : String.valueOf(asset.getYom()));
      }
      if (excludeVars == null || !excludeVars.contains(EventsConfig.VAR_UPDATIONTIME)) {
        varMap.put(EventsConfig.VAR_UPDATIONTIME,
            LocalDateUtil.format(updationTime, locale, timezone));
      }
      if (excludeVars == null || !excludeVars.contains(EventsConfig.VAR_CREATIONTIME)) {
        varMap.put(EventsConfig.VAR_CREATIONTIME,
            LocalDateUtil.format(asset.getCreatedOn(), locale, timezone));
      }
      if (excludeVars == null || !excludeVars.contains(EventsConfig.VAR_REGISTEREDBY)) {
        varMap.put(EventsConfig.VAR_REGISTEREDBY, asset.getCreatedBy());
      }
      if (excludeVars == null || !excludeVars.contains(EventsConfig.VAR_UPDATEDBY)) {
        varMap.put(EventsConfig.VAR_UPDATEDBY, asset.getUpdatedBy());
      }
      DomainConfig dc = DomainConfig.getInstance(asset.getDomainId());
      AssetConfig tc = dc.getAssetConfig();
      if (excludeVars == null || !excludeVars.contains(EventsConfig.VAR_ASSETTYPE)) {
        if (AssetUtil.getAssetName(asset.getType()) != null) {
          varMap.put(EventsConfig.VAR_ASSETTYPE, AssetUtil.getAssetName(asset.getType()));
        }
      }

      if (excludeVars == null || !excludeVars.contains(EventsConfig.VAR_DEVICEVENDOR)) {
        try {
          AssetSystemConfig tsc = AssetSystemConfig.getInstance();

          String vendorName = asset.getVendorId();
          if (tc != null && tsc != null
              && tsc.getManufacturerName(AssetSystemConfig.TYPE_TEMPERATURE_DEVICE,
              asset.getVendorId()) != null
              && !tsc
              .getManufacturerName(AssetSystemConfig.TYPE_TEMPERATURE_DEVICE, asset.getVendorId())
              .isEmpty()) {
            vendorName =
                tsc.getManufacturerName(AssetSystemConfig.TYPE_TEMPERATURE_DEVICE,
                    asset.getVendorId());
          }
          varMap.put(EventsConfig.VAR_DEVICEVENDOR, vendorName);
          if (excludeVars == null || !excludeVars.contains(EventsConfig.VAR_MANUFACTURER)) {
            varMap.put(EventsConfig.VAR_MANUFACTURER, vendorName);
          }
        } catch (Exception e) {
          xLogger.warn(
              "{0} in fetching configured temperature vendors while generating notification template for domain {1}, and vendor {2}",
              e.getMessage(), asset.getDomainId(), asset.getVendorId());
        }
      }

      return varMap;
    } finally {
      pm.close();
    }
  }

}

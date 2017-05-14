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
import com.logistimo.assets.entity.IAssetAttribute;
import com.logistimo.assets.entity.IAssetStatus;
import com.logistimo.config.models.AssetConfig;
import com.logistimo.config.models.AssetSystemConfig;
import com.logistimo.config.models.DomainConfig;
import com.logistimo.config.models.EventsConfig;
import com.logistimo.dao.JDOUtils;
import com.logistimo.entities.entity.IKiosk;
import com.logistimo.entities.entity.Kiosk;
import com.logistimo.logger.XLog;
import com.logistimo.services.Resources;
import com.logistimo.services.impl.PMF;
import com.logistimo.utils.LocalDateUtil;
import com.logistimo.utils.NumberUtil;

import org.apache.commons.lang.StringUtils;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.ResourceBundle;

import javax.jdo.PersistenceManager;

/**
 * Created by charan on 10/03/17.
 */
public class AssetStatusTemplate implements ITemplate {

  private static final XLog xLogger = XLog.getLog(AssetStatusTemplate.class);

  private final IAssetStatus status;

  public AssetStatusTemplate(IAssetStatus status) {
    this.status = status;
  }

  @Override
  public Map<String, String> getTemplateValues(Locale locale, String timezone,
                                               List<String> excludeVars, Date updationTime) {
    HashMap<String, String> varMap = new HashMap<>();
    PersistenceManager pm = PMF.get().getPersistenceManager();
    try {
      // Entity
      IKiosk k = null;
      IAsset asset;
      try {
        asset = JDOUtils.getObjectById(IAsset.class, status.getAssetId());
      } catch (Exception e) {
        xLogger
            .warn("Associated asset with id : {0} for status id : {1} not found ",
                status.getAssetId(), status.getId(), e);
        return varMap;
      }

      if (asset != null) {
        Long kId = asset.getKioskId();
        if(kId != null) {
          if (excludeVars == null || !excludeVars.contains(EventsConfig.VAR_CUSTOMER)) {
            k = pm.getObjectById(Kiosk.class, kId);
          }
          varMap.put(EventsConfig.VAR_CUSTOMERCITY, k.getCity());
          if (excludeVars == null || !excludeVars.contains(EventsConfig.VAR_ENTITY)) {
            if (k == null) {
              k = pm.getObjectById(Kiosk.class, kId);
            }
            varMap.put(EventsConfig.VAR_ENTITY, k.getName());
          }
          if (excludeVars == null || !excludeVars.contains(EventsConfig.VAR_ENTITYCITY)) {
            if (k == null) {
              k = pm.getObjectById(Kiosk.class, kId);
            }
            varMap.put(EventsConfig.VAR_ENTITYCITY, k.getCity());
          }
        }
        // Serial number
        if (excludeVars == null || !excludeVars.contains(EventsConfig.VAR_SERIALNUMBER)) {
          varMap.put(EventsConfig.VAR_SERIALNUMBER, asset.getSerialId());
        }

        if (excludeVars == null || !excludeVars.contains(EventsConfig.VAR_MODEL)) {
          varMap.put(EventsConfig.VAR_MODEL, asset.getModel());
        }

        if(excludeVars == null || !excludeVars.contains(EventsConfig.VAR_MANUFACTURE_YEAR)) {
          varMap.put(EventsConfig.VAR_MANUFACTURE_YEAR, (asset.getYom() == null) ? NOT_AVAILABLE : String.valueOf(asset.getYom()));
        }

        if (excludeVars == null || !excludeVars.contains(EventsConfig.VAR_MANUFACTURER)) {
          try {
            DomainConfig dc = DomainConfig.getInstance(asset.getDomainId());
            AssetConfig tc = dc.getAssetConfig();
            AssetSystemConfig tsc = AssetSystemConfig.getInstance();

            String vendorName = asset.getVendorId();
            if (tc != null && tsc != null) {
              vendorName = tsc.getManufacturerName(asset.getType(), asset.getVendorId());
              if (StringUtils.isEmpty(vendorName)) {
                vendorName = asset.getVendorId();
              }
            }
            varMap.put(EventsConfig.VAR_MANUFACTURER, vendorName);
          } catch (Exception e) {
            xLogger.warn(
                "{0} in fetching configured temperature vendors while generating notification template for domain {1}, and vendor {2}",
                e.getMessage(), asset.getDomainId(), asset.getVendorId());
          }
        }

        // Temperature
        if (excludeVars == null || !excludeVars.contains(EventsConfig.VAR_TEMPERATURE)) {
          varMap.put(EventsConfig.VAR_TEMPERATURE, NumberUtil.getFormattedValue(status.getTmp()));
        }
        // Status
        String abnstatus = getTemperatureStatusDisplay(locale);
        if (excludeVars == null || !excludeVars.contains(EventsConfig.VAR_TEMPERATURESTATUS)) {
          varMap.put(EventsConfig.VAR_TEMPERATURESTATUS, abnstatus);
        }

        // Status update time
        if (excludeVars == null || !excludeVars.contains(EventsConfig.VAR_STATUSUPDATEDTIME)) {
          varMap.put(EventsConfig.VAR_STATUSUPDATEDTIME,
              this.status.getTs() != null ?
                  LocalDateUtil.format(status.getTs(), locale, timezone) : "None");
        }

        // Asset Type
        if (excludeVars == null || !excludeVars.contains(EventsConfig.VAR_ASSETTYPE)) {
          varMap.put(EventsConfig.VAR_ASSETTYPE, AssetUtil.getAssetName(asset.getType()));
        }

        // Temperature range
        String min = getAttribute(IAssetAttribute.MIN);
        String max = getAttribute(IAssetAttribute.MAX);
        String range = NumberUtil.getFormattedValue(min) + "-" + NumberUtil.getFormattedValue(max);
        if (excludeVars == null || !excludeVars.contains(EventsConfig.VAR_TEMPERATURERANGE)) {
          varMap.put(EventsConfig.VAR_TEMPERATURERANGE, range);
        }
        if (excludeVars == null || !excludeVars.contains(EventsConfig.VAR_TEMPERATUREMIN)) {
          varMap.put(EventsConfig.VAR_TEMPERATUREMIN, min);
        }

        if (excludeVars == null || !excludeVars.contains(EventsConfig.VAR_TEMPERATUREMAX)) {
          varMap.put(EventsConfig.VAR_TEMPERATUREMAX, max);
        }

        if (excludeVars == null || !excludeVars.contains(EventsConfig.VAR_ASSET_SENSOR_ID)) {
          varMap.put(EventsConfig.VAR_ASSET_SENSOR_ID, status.getsId());
        }
        AssetSystemConfig tsc;
        AssetSystemConfig.Asset assetConfig;
        if (excludeVars == null || !excludeVars.contains(EventsConfig.VAR_ASSET_MP)) {
          try {
            DomainConfig dc = DomainConfig.getInstance(asset.getDomainId());
            AssetConfig tc = dc.getAssetConfig();
            tsc = AssetSystemConfig.getInstance();
            if (tc != null && tsc != null && tsc.assets != null) {
              assetConfig = tsc.assets.get(asset.getType());
              if (assetConfig.monitoringPositions != null) {
                for (AssetSystemConfig.MonitoringPosition mpositions : assetConfig.monitoringPositions) {
                  if (Objects.equals(mpositions.mpId, status.getMpId())) {
                    varMap.put(EventsConfig.VAR_ASSET_MP, mpositions.name);
                  }
                }
              }
            }
          } catch (Exception e) {
            xLogger.warn(
                "{0} in fetching configured temperature vendors while generating notification template for domain {1}, and vendor {2}",
                e.getMessage(), asset.getDomainId(), asset.getVendorId());
          }
        }
        if(status.getStatus() != null && (excludeVars == null || !excludeVars.contains(EventsConfig.VAR_ASSET_STATUS))) {
          try {
            tsc = AssetSystemConfig.getInstance();
            varMap.put(EventsConfig.VAR_ASSET_STATUS, tsc.workingStatuses.get(status.getStatus()).displayValue);
          } catch (Exception e) {
            xLogger.warn("{0} while fetching asset system config", e.getMessage());
          }
        }
        return varMap;
      }
    } finally {
      pm.close();
    }
    return null;
  }

  private String getAttribute(String key) {
    if (status.getAttributes() != null) {
      for (IAssetAttribute attribute : status.getAttributes()) {
        if (Objects.equals(attribute.getAtt(), key)) {
          return attribute.getVal();
        }
      }
    }
    return null;
  }

  public String getTemperatureStatusDisplay(Locale locale) {
    ResourceBundle messages = Resources.get().getBundle("Messages", locale);
    switch (status.getAbnStatus()) {
      case IAsset.ABNORMAL_TYPE_HIGH:
        return messages.getString("high");
      case IAsset.ABNORMAL_TYPE_LOW:
        return messages.getString("low");
      default:
        return messages.getString("normal");
    }
  }
}

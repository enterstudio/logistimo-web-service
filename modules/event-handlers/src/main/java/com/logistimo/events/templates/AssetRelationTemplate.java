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
import com.logistimo.assets.entity.IAssetRelation;
import com.logistimo.config.models.EventsConfig;
import com.logistimo.dao.JDOUtils;
import com.logistimo.entities.entity.IKiosk;
import com.logistimo.logger.XLog;
import com.logistimo.utils.LocalDateUtil;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Created by charan on 14/03/17.
 */
public class AssetRelationTemplate implements ITemplate {

  private final IAssetRelation assetRelation;
  private static final XLog xLogger = XLog.getLog(AssetRelationTemplate.class);

  public AssetRelationTemplate(IAssetRelation assetRelation){
    this.assetRelation = assetRelation;
  }

  @Override
  public Map<String, String> getTemplateValues(Locale locale, String timezone,
                                               List<String> excludeVars, Date updationTime) {
    HashMap<String, String> varMap = new HashMap<>();
    try {
      IAsset asset = JDOUtils.getObjectById(IAsset.class, assetRelation.getRelatedAssetId());
      varMap.put(EventsConfig.VAR_RELATED_ASSET_SERIAL_NUM, String.valueOf(asset.getSerialId()));
      varMap.put(EventsConfig.VAR_RELATED_ASSET_TYPE, AssetUtil.getAssetName(asset.getType()));
      varMap.put(EventsConfig.VAR_RELATED_ASSET_MODEL, asset.getModel());
      varMap.put(EventsConfig.VAR_RELATED_ASSET_MANUFACTURER, asset.getVendorId());
      asset = JDOUtils.getObjectById(IAsset.class, assetRelation.getAssetId());
      varMap.put(EventsConfig.VAR_SERIALNUMBER, String.valueOf(asset.getSerialId()));
      varMap.put(EventsConfig.VAR_ASSETTYPE, AssetUtil.getAssetName(asset.getType()));
      varMap.put(EventsConfig.VAR_MODEL, asset.getModel());
      varMap.put(EventsConfig.VAR_MANUFACTURER, asset.getVendorId());
      if(asset.getKioskId() != null) {
        IKiosk kiosk = JDOUtils.getObjectById(IKiosk.class, asset.getKioskId());
        varMap.put(EventsConfig.VAR_ENTITY, kiosk.getName());
        varMap.put(EventsConfig.VAR_ENTITYCITY, kiosk.getCity());
      }
      varMap.put(EventsConfig.VAR_CREATIONTIME, LocalDateUtil.format(updationTime, locale, timezone));
      varMap.put(EventsConfig.VAR_UPDATIONTIME, LocalDateUtil.format(updationTime, locale, timezone));
    } catch (Exception e) {
      xLogger.warn("Error while getting the asset system config", e);
    }
    return varMap;
  }

}

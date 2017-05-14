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

package com.logistimo.service;

import com.logistimo.api.builders.AssetBuilder;
import com.logistimo.assets.AssetUtil;
import com.logistimo.assets.entity.IAsset;
import com.logistimo.assets.service.AssetManagementService;
import com.logistimo.assets.service.impl.AssetManagementServiceImpl;
import com.logistimo.config.entity.IConfig;
import com.logistimo.config.models.AssetSystemConfig;
import com.logistimo.config.models.ConfigurationException;
import com.logistimo.config.service.ConfigurationMgmtService;
import com.logistimo.config.service.impl.ConfigurationMgmtServiceImpl;
import com.logistimo.dao.JDOUtils;

import org.codehaus.jettison.json.JSONException;
import org.json.JSONArray;
import org.json.JSONObject;
import com.logistimo.LgTestCase;
import com.logistimo.services.ObjectNotFoundException;
import com.logistimo.services.ServiceException;
import com.logistimo.services.Services;

import com.logistimo.logger.XLog;
import org.testng.annotations.Test;

import java.util.Date;

/**
 * Created by smriti on 2/8/17.
 */
public class AssetMgmtServiceTest extends LgTestCase {
  private static final XLog XLOGGER = XLog.getLog(AssetMgmtServiceTest.class);
  private static final String assetName = "ILR";

  @Test
  public void createAssetTest()
      throws Exception {
    boolean assetCreated = createAssetMatchingPattern();
    if (!assetCreated) {
      throw new Exception("Error while creating asset");
    }
  }

  private boolean createAssetMatchingPattern()
      throws ConfigurationException, ObjectNotFoundException, JSONException {
    try {
      //asset related variables
      String serialId = "HBC2345";
      String modelId = "HBD11asa";
      String manufacturer = "Haier";
      String serialRegex = "^HBC[\\d]";
      String modelRegex = "^HBD[a-z]";

      Long domainId = getDomainId("Default").getId();
      String userId = getUser("smriti").getUserId();
      IAsset asset = JDOUtils.createInstance(IAsset.class);
      updateSystemConfig(manufacturer, serialRegex, modelRegex);
      AssetManagementService ams = Services.getService(AssetManagementServiceImpl.class);
      asset = createAssetInstance(asset, serialId, manufacturer, modelId, assetName, userId);

      //create asset
      ams.createAsset(domainId, asset, new AssetBuilder().buildFilterModel(asset));
      return true;
    } catch (ServiceException e) {
      XLOGGER.warn("Error while getting domain id for domain Default: ", e);
    }
    return false;
  }

  private IAsset createAssetInstance(IAsset asset, String serialId, String vendorId, String modelId,
                                     String assetName, String userId) {
    asset.setSerialId(serialId);
    asset.setModel(modelId);
    asset.setVendorId(vendorId);
    asset.setType(AssetUtil.getAssetType(assetName));
    asset.setCreatedBy(userId);
    asset.setUpdatedBy(userId);
    return asset;
  }

  private void updateSystemConfig(String vendor, String serialRegex, String modelRegex)
      throws ConfigurationException, ServiceException, ObjectNotFoundException, JSONException {
    boolean configUpdated = false;
    ConfigurationMgmtService cms = Services.getService(ConfigurationMgmtServiceImpl.class);
    IConfig assetSystemConfig = cms.getConfiguration(IConfig.ASSETSYSTEMCONFIG);
    JSONObject assetConfig = new JSONObject(assetSystemConfig.getConfig());
    JSONArray assets = (JSONArray) assetConfig.get("assets");
    for (int i = 0; i < assets.length(); i++) {
      JSONObject obj = assets.getJSONObject(i);
      if (!obj.get("name").equals(assetName)) {
        XLOGGER.info("Asset name not matched");
        continue;
      }
      JSONArray manufacturerJson = obj.getJSONArray("manufacturers");
      for (int j = 0; j < manufacturerJson.length(); j++) {
        JSONObject object = (JSONObject) manufacturerJson.get(j);
        if (object.get("name").equals(vendor)) {
          object.put(AssetSystemConfig.Manufacturer.MANC_SERIAL_FORMAT, serialRegex);
          object.put(AssetSystemConfig.Manufacturer.MANC_MODEL_FORMAT, modelRegex);
          manufacturerJson.put(j, object);
          configUpdated = true;
          System.out.println("Found");
          break;
        }
      }
      if (configUpdated) {
        obj.put("manufacturers", manufacturerJson);
        assets.put(i, obj);
        break;
      }
    }
    assetConfig.put("assets", assets);
    assetSystemConfig.setConfig(assetConfig.toString());
    assetSystemConfig.setLastUpdated(new Date());
    assetSystemConfig.setUserId("smriti");
    cms.updateConfiguration(assetSystemConfig);
    XLOGGER.info("System config updated with new serial and model regex");
  }
}

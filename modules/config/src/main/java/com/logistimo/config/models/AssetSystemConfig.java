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

package com.logistimo.config.models;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.logistimo.config.entity.IConfig;
import com.logistimo.config.service.ConfigurationMgmtService;
import com.logistimo.config.service.impl.ConfigurationMgmtServiceImpl;
import com.logistimo.logger.XLog;
import com.logistimo.services.ObjectNotFoundException;
import com.logistimo.services.ServiceException;
import com.logistimo.services.Services;
import org.apache.commons.lang.StringUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AssetSystemConfig {
  public static final Integer TYPE_TEMPERATURE_DEVICE = 1;
  public static final String URL = "url";
  public static final String USERNAME = "username";
  public static final String PASSWORD = "password";
  public static final String SECRETKEY = "secretkey";
  public static final String ASSETS = "assets";
  public static final String WORKING_STATUSES = "workingStatuses";
  public static final int MONITORED_ASSET = 2;
  // Logger
  private static final XLog xLogger = XLog.getLog(AssetSystemConfig.class);
  public String url;
  public String username;
  public String password;
  public String secretKey;
  public Map<Integer, Asset> assets;
  public List<WorkingStatus> workingStatuses;

  public AssetSystemConfig() {

  }

  public AssetSystemConfig(String jsonString) throws ConfigurationException {
    xLogger.fine("Entering TemperatureSystemConfig constructor. jsonString: {0}", jsonString);
    try {
      if (jsonString != null && !jsonString.isEmpty()) {
        JSONObject jsonObject = new JSONObject(jsonString);

        this.url = jsonObject.getString(URL);
        this.username = jsonObject.getString(USERNAME);
        this.password = jsonObject.getString(PASSWORD);
        this.secretKey = jsonObject.getString(SECRETKEY);
        JSONArray assetsJSONArray = jsonObject.getJSONArray(ASSETS);
        if (!jsonObject.isNull(WORKING_STATUSES)) {
          this.workingStatuses =
              new Gson().fromJson(jsonObject.get(WORKING_STATUSES).toString(),
                  new TypeToken<List<WorkingStatus>>() {
                  }.getType());
        }

        assets = new HashMap<>(assetsJSONArray.length());
        for (int i = 0; i < assetsJSONArray.length(); i++) {
          Asset asset = new Asset();
          JSONObject assetJsonObject = assetsJSONArray.getJSONObject(i);
          asset.name = assetJsonObject.getString(Asset.ASSET_NAME);
          asset.type = assetJsonObject.getInt(Asset.ASSET_TYPE);
          asset.isGSMEnabled =
              !assetJsonObject.isNull(Asset.ASSET_IS_GSM_ENABLED) && assetJsonObject
                  .getBoolean(Asset.ASSET_IS_GSM_ENABLED);
          asset.isTemperatureEnabled =
              !assetJsonObject.isNull(Asset.ASSET_IS_TEMP_SENSITIVE) && assetJsonObject
                  .getBoolean(Asset.ASSET_IS_TEMP_SENSITIVE);
          if (!assetJsonObject.isNull(Asset.ASSET_MONITORING_POSITIONS)) {
            asset.monitoringPositions =
                new Gson()
                    .fromJson(assetJsonObject.get(Asset.ASSET_MONITORING_POSITIONS).toString(),
                        new TypeToken<List<MonitoringPosition>>() {
                        }.getType());
          }

          for (int j = 0; j < assetJsonObject.getJSONArray(Asset.ASSET_MANUFACTURERS).length();
               j++) {
            JSONObject
                mancJSONOBject =
                assetsJSONArray.getJSONObject(i).getJSONArray(Asset.ASSET_MANUFACTURERS)
                    .getJSONObject(j);
            Manufacturer manufacturer = new Manufacturer();
            manufacturer.name = mancJSONOBject.getString(Manufacturer.MANC_NAME);
            if (!mancJSONOBject.isNull(Manufacturer.MANC_MODEL)) {
              manufacturer.model =
                  new Gson().fromJson(mancJSONOBject.get(Manufacturer.MANC_MODEL).toString(),
                      new TypeToken<List<Model>>() {
                      }.getType());
            }
            if(asset.type == MONITORED_ASSET) {
              if(mancJSONOBject.has(Manufacturer.MANC_SERIAL_FORMAT)) {
                manufacturer.serialFormat = mancJSONOBject.getString(Manufacturer.MANC_SERIAL_FORMAT);
              }
              if(mancJSONOBject.has(Manufacturer.MANC_MODEL_FORMAT)) {
                manufacturer.modelFormat = mancJSONOBject.getString(Manufacturer.MANC_MODEL_FORMAT);
              }
              if(mancJSONOBject.has(Manufacturer.MANC_SERIAL_FORMAT_DESCRIPTION)) {
                manufacturer.serialFormatDescription = mancJSONOBject.getString(Manufacturer.MANC_SERIAL_FORMAT_DESCRIPTION);
              }
              if(mancJSONOBject.has(Manufacturer.MANC_MODEL_FORMAT_DESCRIPTION)) {
                manufacturer.modelFormatDescription = mancJSONOBject.getString(Manufacturer.MANC_MODEL_FORMAT_DESCRIPTION);
              }
            }
            asset.getManufacturers()
                .put(mancJSONOBject.getString(Manufacturer.MANC_ID), manufacturer);
          }
          assets.put(assetsJSONArray.getJSONObject(i).getInt(Asset.ASSET_ID), asset);
        }
      }
    } catch (Exception e) {
      throw new ConfigurationException(
          "Invalid Json for temperature system configuration. " + e.getMessage());
    }
    xLogger.fine("Exiting TemperatureSystemConfig constructor");
  }

  // Get an instance of the Temp Device Vendors config
  public static AssetSystemConfig getInstance() throws ConfigurationException {
    xLogger.fine("Entering TemperatureSystemConfig.getInstance");
    try {
      ConfigurationMgmtService cms = Services.getService(ConfigurationMgmtServiceImpl.class);
      IConfig c = cms.getConfiguration(IConfig.ASSETSYSTEMCONFIG);
      return new AssetSystemConfig(c.getConfig());
    } catch (ObjectNotFoundException | ServiceException e) {
      throw new ConfigurationException(e.getMessage());
    }
  }

  public String getManufacturerName(Integer type, String manuId) {
    if (assets != null && assets.get(type) != null && assets.get(type).getManufacturers() != null
        && assets.get(type).getManufacturers().get(manuId) != null) {
      return assets.get(type).getManufacturers().get(manuId).name;
    }

    return "";
  }

  public String getManufacturerId(Integer type, String manuName) {
    if (StringUtils.isNotEmpty(manuName) && assets != null && assets.get(type) != null
        && assets.get(type).getManufacturers() != null) {
      Map<String, Manufacturer> manufacturers = assets.get(type).getManufacturers();
      for (String manufacturerId : manufacturers.keySet()) {
        if (manuName.equalsIgnoreCase(manufacturers.get(manufacturerId).name)) {
          return manufacturerId;
        }
      }
    }

    return null;
  }

  public Asset getAsset(Integer type) {
    if (assets != null) {
      return assets.get(type);
    }

    return null;
  }

  // Get the temperature monitoring service url
  public String getUrl() {
    return url;
  }

  // Get the userName for accessing temperature monitoring service
  public String getUserName() {
    return username;
  }

  // Get the password for accessing temperature monitoring service
  public String getPassword() {
    return password;
  }

  // Get the secret key for authenticating the Temperature Monitoring Service
  public String getSecretKey() {
    return secretKey;
  }

  public Map<Integer, Asset> getAssetsByType(Integer type) {
    Map<Integer, Asset> ats = new HashMap<>();
    for (Integer integer : assets.keySet()) {
      Asset asset = assets.get(integer);
      if (type.equals(asset.type)) {
        ats.put(integer, asset);
      }
    }
    return ats;
  }

  public Map<Integer, String> getAssetsNameByType(Integer type) {
    Map<Integer, String> ats = new HashMap<>();
    for (Integer integer : assets.keySet()) {
      Asset asset = assets.get(integer);
      if (type.equals(asset.type)) {
        ats.put(integer, asset.name);
      }
    }
    return ats;
  }

  public Map<String, String> getManufacturersByType(Integer type) {
    Map<String, String> manufacturers = new HashMap<>();
    for (Integer integer : assets.keySet()) {
      Asset asset = assets.get(integer);
      if (type.equals(asset.type)) {
        Map<String, Manufacturer> assetManufacturers = asset.getManufacturers();
        for (String id : assetManufacturers.keySet()) {
          manufacturers.put(id, assetManufacturers.get(id).name);
        }
      }
    }
    return manufacturers;
  }

  public Map<Integer, String> getAllWorkingStatus() {
    Map<Integer, String> allWorkingStatus = new HashMap<>();
    for (WorkingStatus workingStatus : workingStatuses) {
      allWorkingStatus.put(workingStatus.status, workingStatus.displayValue);
    }
    return allWorkingStatus;
  }

  /**
   * Returns the config pull for the requested device model.
   *
   * @return true - if config enabled.
   */
  public boolean isConfigPullEnabled(Integer type, String vendorId, String model) {
    return getAsset(type).getManufacturers().get(vendorId).model.stream()
        .anyMatch(configModel -> configModel.name.equals(model) && configModel.feature.pullConfig);
  }

  public static class Asset {
    public static final String ASSET_NAME = "name";
    public static final String ASSET_ID = "id";
    public static final String ASSET_TYPE = "type";
    public static final String ASSET_MANUFACTURERS = "manufacturers";
    public static final String ASSET_IS_TEMP_SENSITIVE = "isTempSensitive";
    public static final String ASSET_IS_GSM_ENABLED = "isGSMEnabled";
    public static final String ASSET_MONITORING_POSITIONS = "monitoringPositions";
    public Integer type;
    public List<MonitoringPosition> monitoringPositions;
    private String name;
    private boolean isTemperatureEnabled = false;
    private boolean isGSMEnabled = false;
    private Map<String, Manufacturer> manufacturers;

    public Asset() {
      manufacturers = new HashMap<>(1);
    }

    public String getName() {
      return name;
    }

    public void setName(String name) {
      this.name = name;
    }

    public Map<String, Manufacturer> getManufacturers() {
      return manufacturers;
    }

    public void setManufacturers(Map<String, Manufacturer> manufacturers) {
      this.manufacturers = manufacturers;
    }

    public boolean isTemperatureEnabled() {
      return isTemperatureEnabled;
    }

    public void isTemperatureEnabled(boolean isTemperatureEnabled) {
      this.isTemperatureEnabled = isTemperatureEnabled;
    }

    public boolean isGSMEnabled() {
      return isGSMEnabled;
    }

    public void setIsGSMEnabled(boolean isGSMEnabled) {
      this.isGSMEnabled = isGSMEnabled;
    }
  }

  public static class Manufacturer {
    public static final String MANC_NAME = "name";
    public static final String MANC_ID = "id";
    public static final String MANC_MODEL = "model";
    public static final String MANC_SERIAL_FORMAT = "serialFormat";
    public static final String MANC_MODEL_FORMAT = "modelFormat";
    public static final String MANC_SERIAL_FORMAT_DESCRIPTION = "serialFormatDescription";
    public static final String MANC_MODEL_FORMAT_DESCRIPTION = "modelFormatDescription";

    public String name;
    public List<Model> model;
    public String serialFormat;
    public String modelFormat;
    public String serialFormatDescription;
    public String modelFormatDescription;
  }

  public static class Model {
    public static final String NAME = "name";
    public static final String TYPE = "type";
    public static final String SENSORS = "sns";

    public String name;
    public String type;
    public Feature feature;
    public List<Sensor> sns;
  }

  public static class Sensor {
    public static final String NAME = "name";
    public static final String LOCATION_ID = "mpId";

    public String name;
    public Integer mpId;
    public String cd;
  }

  public static class MonitoringPosition {
    public Integer mpId;
    public String name;
    public String sId;
  }

  public static class WorkingStatus {
    public Integer status;
    public String displayValue;
  }

  public static class Feature {
    public Boolean pullConfig;
    public Boolean dailyStats;
    public Boolean powerStats;
    public Boolean displayLabel;
    public Boolean currentTemperature;
  }
}

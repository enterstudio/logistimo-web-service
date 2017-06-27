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

package com.logistimo.assets;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.internal.LinkedTreeMap;

import com.logistimo.AppFactory;
import com.logistimo.assets.entity.IAsset;
import com.logistimo.assets.entity.IAssetRelation;
import com.logistimo.assets.entity.IAssetStatus;
import com.logistimo.assets.models.AssetModel;
import com.logistimo.assets.models.AssetModels;
import com.logistimo.assets.models.AssetRelationModel;
import com.logistimo.assets.models.DeviceConfigPushPullModel;
import com.logistimo.assets.models.TemperatureResponse;
import com.logistimo.assets.service.AssetManagementService;
import com.logistimo.assets.service.impl.AssetManagementServiceImpl;
import com.logistimo.config.models.AssetConfig;
import com.logistimo.config.models.AssetSystemConfig;
import com.logistimo.config.models.ConfigurationException;
import com.logistimo.config.models.DomainConfig;
import com.logistimo.config.models.EventSpec;
import com.logistimo.constants.Constants;
import com.logistimo.dao.JDOUtils;
import com.logistimo.domains.entity.IDomainLink;
import com.logistimo.domains.utils.DomainsUtil;
import com.logistimo.events.EventConstants;
import com.logistimo.events.entity.IEvent;
import com.logistimo.events.exceptions.EventGenerationException;
import com.logistimo.events.processor.EventPublisher;
import com.logistimo.logger.XLog;
import com.logistimo.services.ServiceException;
import com.logistimo.services.Services;
import com.logistimo.services.http.HttpResponse;
import com.logistimo.services.http.URLFetchService;
import com.logistimo.services.impl.PMF;
import com.logistimo.utils.MsgUtil;
import com.logistimo.utils.StringUtil;

import org.apache.commons.lang.StringUtils;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import javax.jdo.PersistenceManager;

/**
 * Created by kaniyarasu on 12/11/15.
 */
public class AssetUtil {
  public final static int HTTP_STATUS_UNAUTHORIZED = 401;
  public final static int HTTP_STATUS_CREATED = 201;
  public final static int HTTP_STATUS_NOTFOUND = 404;
  public final static int HTTP_STATUS_PARTIALCONTENT = 206;
  public final static int HTTP_STATUS_INTERNALSERVERERROR = 500;
  public final static int HTTP_STATUS_BADREQUEST = 400;
  public final static int HTTP_STATUS_OK = 200;
  public final static String SERIAL_NUMBER = "serial_number";
  public final static String MANUFACTURER_NAME = "manu_name";
  public final static String ASSET_MODEL = "model";
  public final static String ASSET_TYPE = "type";
  public final static String ASSET_NAME = "name";
  public final static String SENSOR_DEVICE_VENDOR = "nexleaf";
  public final static String SENSOR_DEVICE_MODEL = "CT5";
  //GSM meta data
  public final static String GSM_SIM_PHN_NUMBER = "gsm.sim.phn";
  public final static String GSM_SIM_SIMID = "gsm.sim.sid";
  public final static String GSM_SIM_NETWORK_PROVIDER = "gsm.sim.np";
  public final static String GSM_ALTSIM_PHN_NUMBER = "gsm.altSim.phn";
  public final static String GSM_ALTSIM_SIMID = "gsm.altSim.sid";
  public final static String GSM_ALTSIM_NETWORK_PROVIDER = "gsm.altSim.np";
  public final static List<String> GSM_GROUP = new ArrayList<String>() {{
    add(GSM_SIM_PHN_NUMBER);
    add(GSM_SIM_SIMID);
    add(GSM_SIM_NETWORK_PROVIDER);
    add(GSM_ALTSIM_PHN_NUMBER);
    add(GSM_ALTSIM_SIMID);
    add(GSM_ALTSIM_NETWORK_PROVIDER);
  }};
  public final static String DEV_MODEL = "dev.mdl";
  public final static String DEV_IMEI = "dev.imei";
  private static final XLog xLogger = XLog.getLog(AssetUtil.class);
  public static final String TAGS = "tags";
  public static final String ASSET_YOM = "yom";
  private static URLFetchService urlFetchService = AppFactory.get().getURLFetchService();
  private static Gson gson = new Gson();

  private static String post(String path, String data) throws ServiceException {
    try {
      AssetSystemConfig tempConfig = AssetSystemConfig.getInstance();
      String url = tempConfig.getUrl();
      if (url != null && !url.isEmpty()) {
        url += (url.endsWith("/") ? "" : "/") + path;
        xLogger.info("URL: {0}", url);
        HttpResponse
            response =
            urlFetchService.post(new URL(url), data.getBytes(), tempConfig.getUserName(),
                tempConfig.getPassword(), 60);
        int status = response.getResponseCode();
        if (status == HTTP_STATUS_CREATED || status == HTTP_STATUS_OK) {
          return new String(response.getContent(), "UTF-8");
        } else {
          xLogger.warn("Asset. server returned a status of {0} for req. {1}", status, url);
          throw new ServiceException(new String(response.getContent(), "UTF-8"));
        }
      } else {
        xLogger.severe("Invalid asset. service URL: null or empty");
      }
    } catch (Exception e) {
      xLogger.severe("{0} when posting to URL: {1}", e.getMessage(), path, e);
      throw new ServiceException(e.getMessage());
    }

    return null;
  }

  private static String get(String path) {
    try {
      AssetSystemConfig tempConfig = AssetSystemConfig.getInstance();
      String url = tempConfig.getUrl();
      if (url != null && !url.isEmpty()) {
        url += (url.endsWith("/") ? "" : "/") + path;
        xLogger.info("URL: {0}", url);
        HttpResponse
            response =
            urlFetchService
                .get(new URL(url), tempConfig.getUserName(), tempConfig.getPassword(), 60);
        int status = response.getResponseCode();
        if (status == HTTP_STATUS_OK) {
          return new String(response.getContent(), "UTF-8");
        } else {
          xLogger.warn("Asset. server returned a status of {0} for req. {1}", status, url);
        }
      } else {
        xLogger.severe("Invalid asset. service URL: null or empty");
      }
    } catch (Exception e) {
      xLogger.severe("{0} when fetching URL: {1}", e.getMessage(), path, e);
    }

    return null;
  }

  public static IAsset verifyAndRegisterAsset(Long domainId, String userId, Long kioskId,
                                              Map<String, Object> variableMap,
                                              Map<String, Object> metaDataMap)
      throws ServiceException {
    String assetName = ((String) variableMap.get(ASSET_NAME));
    if (!((String) variableMap.get(SERIAL_NUMBER)).isEmpty()
        && !((String) variableMap.get(MANUFACTURER_NAME)).isEmpty()
        && !((String) variableMap.get(ASSET_MODEL)).isEmpty()
        && ((StringUtils.isNotBlank(assetName)) || variableMap.containsKey(ASSET_TYPE))) {

      String manufacturerId;
      Integer assetType;
      try {
        if (!variableMap.containsKey(ASSET_TYPE)) {
          if (StringUtils.isNotBlank(assetName)) {
            assetType = getAssetType(assetName);
            if (assetType == null) {
              throw new ServiceException("Asset type is invalid.");
            }
          } else {
            throw new ServiceException("Asset type is empty.");
          }
        } else {
          assetType = (Integer) variableMap.get(ASSET_TYPE);
        }
      } catch (Exception e) {
        throw new ServiceException(e.getMessage());
      }

      try {
        AssetSystemConfig asc = AssetSystemConfig.getInstance();

        if (asc == null) {
          throw new ConfigurationException();
        }
        Map<Integer, AssetSystemConfig.Asset> assets = asc.assets;
        if (assets == null) {
          throw new ConfigurationException();
        }
        manufacturerId = asc.getManufacturerId(assetType,
                ((String) variableMap.get(MANUFACTURER_NAME)).toLowerCase());
      } catch (ConfigurationException e) {
        throw new ServiceException("Manufacturer is invalid.");
      }

      if (StringUtils.isEmpty(manufacturerId) || !isManufacturerConfigured(assetType,
          manufacturerId, domainId)) {
        throw new ServiceException("Manufacturer is invalid or not configured.");
      }

      if (StringUtils.isEmpty((String) variableMap.get(ASSET_MODEL)) || (
          Objects.equals(assetType, IAsset.TEMP_DEVICE)
              && getAssetModel(assetType, manufacturerId, (String) variableMap.get(ASSET_MODEL),
              domainId) == null)) {
        throw new ServiceException("Asset model is invalid or not configured.");
      }

      AssetManagementService ams = Services.getService(AssetManagementServiceImpl.class);
      IAsset asset = ams.getAsset(manufacturerId, (String) variableMap.get(SERIAL_NUMBER));

      if (asset == null) {
        //Registering new fridge asset
        asset = JDOUtils.createInstance(IAsset.class);
        asset.setSerialId((String) variableMap.get(SERIAL_NUMBER));
        asset.setVendorId(manufacturerId);
        asset.setModel((String) variableMap.get(ASSET_MODEL));
        if(variableMap.get(ASSET_YOM) != null) {
          asset.setYom(Integer.valueOf((String)variableMap.get(ASSET_YOM)));
        }
        asset.setType(assetType);
        asset.setCreatedBy(userId);
        asset.setUpdatedBy(userId);
        asset.setNsId(asset.getSerialId().toLowerCase());
        if (kioskId != null) {
          asset.setKioskId(kioskId);
        }

        AssetModel assetModel = AssetUtil.buildAssetModel(domainId,asset,metaDataMap);
        if(variableMap.get(AssetUtil.TAGS) != null){
          assetModel.tags = (List<String>) variableMap.get(AssetUtil.TAGS);
        }
        //Registering in LS
        ams.createAsset(domainId, asset, assetModel);

      } else {
        //Validating domain association
        if (!Objects.equals(asset.getDomainId(), domainId)) {
          throw new ServiceException(
              "Given asset " + asset.getSerialId() + " is mapped to another domain.");
        }

        //Validating kiosk association
        if (kioskId != null && asset.getKioskId() != null && !Objects
            .equals(asset.getKioskId(), kioskId)) {
          throw new ServiceException(
              "Given asset " + asset.getSerialId() + " is mapped to another store.");
        }

        if (kioskId != null && asset.getKioskId() == null) {
          asset.setKioskId(kioskId);
        }

        if (variableMap.get(ASSET_MODEL) != null && !((String) variableMap.get(ASSET_MODEL))
            .isEmpty()) {
          asset.setModel((String) variableMap.get(ASSET_MODEL));
        }

        if (!Objects.equals(asset.getType(), assetType)) {
          try {
            AssetSystemConfig asc = AssetSystemConfig.getInstance();
            if (asc == null) {
              throw new ConfigurationException();
            }
            AssetSystemConfig.Asset cAsset = asc.getAsset(asset.getType());
            AssetSystemConfig.Asset nAsset = asc.getAsset(assetType);
            if (nAsset.type == IAsset.MONITORED_ASSET) {
              if (Objects.equals(cAsset.type, nAsset.type)) {
                IAssetRelation assetRelation = ams.getAssetRelationByAsset(asset.getId());
                if (assetRelation == null) {
                  asset.setType(assetType);
                } else {
                  throw new ServiceException("Asset type can be changed only for unrelated asset.");
                }
              } else {
                throw new ServiceException(
                    "Asset type can be changed only to another monitored asset.");
              }
            } else {
              throw new ServiceException("Asset type of monitored asset only be changed.");
            }
          } catch (ConfigurationException e) {
            throw new ServiceException("Asset type is invalid");
          }
        }
        AssetModel assetModel = AssetUtil.buildAssetModel(domainId, asset, metaDataMap);
        if(variableMap.get(AssetUtil.TAGS) != null){
          assetModel.tags = (List<String>) variableMap.get(AssetUtil.TAGS);
        }
        //Updating asset in LS
        ams.updateAsset(domainId, asset, assetModel);

      }

      return asset;
    } else {
      boolean isError = false;
      if (!((String) variableMap.get(SERIAL_NUMBER)).isEmpty()) {
        isError = true;
      }

      if (!((String) variableMap.get(MANUFACTURER_NAME)).isEmpty()) {
        isError = true;
      }

      if (!((String) variableMap.get(ASSET_MODEL)).isEmpty()) {
        isError = true;
      }

      if (StringUtils.isNotBlank(assetName)) {
        isError = true;
      }

      if (isError) {
        throw new ServiceException(
            "Given asset data is invalid, please provide all mandatory fields.");
      }
    }

    return null;
  }

  public static Boolean isManufacturerConfigured(Integer assetType, String manufacturerId,
                                                 Long domainId) throws ServiceException {
    try {
      DomainConfig dc = DomainConfig.getInstance(domainId);
      if (dc != null) {
        AssetConfig ac = dc.getAssetConfig();
        if (ac != null && ac.getVendorIds() != null && (
            (Objects.equals(assetType, IAsset.TEMP_DEVICE) && ac.getVendorIds()
                .contains(manufacturerId.toLowerCase()))
                || ac.getVendorIds()
                .contains(assetType + Constants.KEY_SEPARATOR + manufacturerId.toLowerCase()))) {
          return true;
        }
      } else {
        throw new ConfigurationException();
      }
    } catch (Exception e) {
      throw new ServiceException("Asset configuration not found for the domain " + domainId);
    }
    return false;
  }

  public static Map<Integer, AssetSystemConfig.Asset> getAssets() throws ConfigurationException {
    AssetSystemConfig asc = AssetSystemConfig.getInstance();
    if (asc == null) {
      throw new ConfigurationException();
    }
    Map<Integer, AssetSystemConfig.Asset> assets = asc.assets;
    if (assets == null) {
      throw new ConfigurationException();
    }

    return assets;
  }

  public static AssetSystemConfig.Asset getAssetConfig(Integer assetType) {
    try {
      return getAssets().get(assetType);
    } catch (Exception e) {
      xLogger.warn("{0} while getting asset {1} information from config", e.getMessage(), assetType,
          e);
      return null;
    }
  }

  public static List<Integer> getAssetsByType(Integer assetType) {
    List<Integer> filteredAsset = new ArrayList<>(1);
    try {
      Map<Integer, AssetSystemConfig.Asset> assets = getAssets();
      for (Integer assetId : assets.keySet()) {
        if (Objects.equals(assetType, assets.get(assetId).type)) {
          filteredAsset.add(assetId);
        }
      }
    } catch (Exception e) {
      xLogger.warn("{0} while getting asset {1} information from config", e.getMessage(), assetType,
          e);
    }

    return filteredAsset;
  }

  public static Integer getAssetType(String assetName) {
    if (assetName != null && !assetName.isEmpty()) {
      try {
        Map<Integer, AssetSystemConfig.Asset> assets = getAssets();
        for (Integer assetId : assets.keySet()) {
          if (assets.get(assetId).getName().equalsIgnoreCase(assetName)) {
            return assetId;
          }
        }
      } catch (Exception e) {
        xLogger
            .warn("{0} while getting asset {1} information from config", e.getMessage(), assetName,
                e);
        return null;
      }
    }

    return null;
  }

  public static String getAssetName(Integer assetType) {
    if (assetType != null) {
      try {
        Map<Integer, AssetSystemConfig.Asset> assets = getAssets();
        if (assets.containsKey(assetType)) {
          return assets.get(assetType).getName();
        }
      } catch (Exception e) {
        xLogger
            .warn("{0} while getting asset {1} information from config", e.getMessage(), assetType,
                e);
      }
    }
    return null;
  }


  public static AssetSystemConfig.Model getAssetModel(Integer assetType, String manufacturerId,
                                                      String model, Long domainId)
      throws ServiceException {
    try {
      DomainConfig dc = DomainConfig.getInstance(domainId);
      if (dc != null) {
        AssetConfig ac = dc.getAssetConfig();
        if (ac != null && ac.getVendorIds() != null && (
            (Objects.equals(assetType, IAsset.TEMP_DEVICE) && ac.getVendorIds()
                .contains(manufacturerId.toLowerCase()))
                || ac.getVendorIds()
                .contains(assetType + Constants.KEY_SEPARATOR + manufacturerId.toLowerCase()))) {
          Map<Integer, AssetSystemConfig.Asset> assets = getAssets();
          AssetSystemConfig.Asset asset = assets.get(assetType);
          if (asset != null && asset.getManufacturers() != null) {
            AssetSystemConfig.Manufacturer
                manufacturer =
                asset.getManufacturers().get(manufacturerId.toLowerCase());
            if (manufacturer != null && manufacturer.model != null) {
              for (AssetSystemConfig.Model assetModel : manufacturer.model) {
                if (assetModel.name.equalsIgnoreCase(model)) {
                  return assetModel;
                }
              }
            }
          }
        }
      } else {
        throw new ConfigurationException();
      }
    } catch (Exception e) {
      throw new ServiceException("Asset configuration not found for the domain " + domainId);
    }
    return null;
  }

  public static AssetModel buildAssetModel(Long domainId, IAsset asset,
                                           Map<String, Object> metaDataMap)
      throws ServiceException {
    AssetModel assetModel = new AssetModel();
    assetModel.dId = asset.getSerialId();
    assetModel.vId = asset.getVendorId();
    assetModel.typ = asset.getType();
    assetModel.cb = asset.getCreatedBy();
    assetModel.ub = asset.getUpdatedBy();
    assetModel.kId = asset.getKioskId();
    if (metaDataMap != null) {
      assetModel.meta = constructDeviceMetaJsonFromMap(metaDataMap);
    }
    if (Objects.equals(asset.getType(), IAsset.TEMP_DEVICE)) {
      assetModel.pc = false;
      DomainConfig dc = DomainConfig.getInstance(domainId);
      if (dc != null) {
        assetModel.config = dc.getAssetConfig().getConfiguration();
      }

      AssetSystemConfig.Model
          model =
          getAssetModel(asset.getType(), asset.getVendorId(), asset.getModel(), domainId);
      if (model != null && model.sns != null && !model.sns.isEmpty()) {
        assetModel.sns = new ArrayList<>(model.sns.size());
        for (AssetSystemConfig.Sensor sensor : model.sns) {
          AssetModels.TemperatureSensorRequest
              sensorRequest =
              new AssetModels.TemperatureSensorRequest();
          sensorRequest.sId = sensor.name;
          sensorRequest.cd = sensor.cd;
          assetModel.sns.add(sensorRequest);
        }
      }
    }
    return assetModel;
  }

  public static AssetModel buildFilterModel(IAsset asset) {
    AssetModel assetModel = new AssetModel();
    assetModel.dId = asset.getSerialId();
    assetModel.vId = asset.getVendorId();
    assetModel.typ = asset.getType();
    assetModel.mdl = asset.getModel();
    assetModel.kId = asset.getKioskId();
    return assetModel;
  }

  public static void registerOrUpdateDevice(AssetModel assetModel, Long domainId)
      throws ServiceException {
    if (assetModel != null) {
      if (assetModel.kId == null) {
        //Getting listing linked domains, if asset not mapped to any entity.
        Set<Long> domainIds = DomainsUtil.getDomainLinks(domainId, IDomainLink.TYPE_PARENT, true);
        assetModel.tags = new ArrayList<>(domainIds.size());
        for (Long currentDomainId : domainIds) {
          assetModel.tags.add(String.valueOf(currentDomainId));
        }
      }

      AssetSystemConfig.Asset asset = getAssetConfig(assetModel.typ);
      if (asset != null && Objects.equals(asset.type, IAsset.MONITORED_ASSET)) {
        assetModel.mps = new ArrayList<>(asset.monitoringPositions.size());
        for (AssetSystemConfig.MonitoringPosition monitoringPosition : asset.monitoringPositions) {
          assetModel.mps.add(monitoringPosition.mpId);
        }
      }

      registerDevices(gson.toJson(new AssetModels.AssetRegistrationModel(assetModel)));

      if (assetModel.config != null) {
        registerConfig(assetModel);

        if (assetModel.pc && assetModel.config.getComm() != null) {
          DeviceConfigPushPullModel deviceConfigPushPullModel = new DeviceConfigPushPullModel(
              assetModel.vId,
              assetModel.dId
          );

          if (assetModel.config.getComm().getChnl() == 1) {
            deviceConfigPushPullModel.typ = 0;
          }
          AssetUtil.pushDeviceConfig(new Gson().toJson(deviceConfigPushPullModel));
        }
      }
    } else {
      xLogger.warn("There are no devices to register");
      throw new ServiceException("There are no devices to register");
    }
  }

  public static void registerDevices(String devicesToAddJsonStr) throws ServiceException {
    xLogger.info("Entering registerDevices. devicesToAddJsonStr: {0}", devicesToAddJsonStr);
    if (devicesToAddJsonStr == null || devicesToAddJsonStr.isEmpty()) {
      xLogger.warn("There are no devices to register");
      throw new ServiceException("There are no devices to register");
    }
    String userName;
    String password;
    String url;

    try {
      AssetSystemConfig tsc = AssetSystemConfig.getInstance();
      userName = tsc.getUserName();
      password = tsc.getPassword();
      url = tsc.getUrl();
    } catch (ConfigurationException e) {
      xLogger.severe(
          "Exception {0} when trying to get Asset System Configuration while registering devices with Asset Monitoring Service. Message: {1}",
          e.getClass().getName(), e.getMessage());
      throw new ServiceException(e.getMessage());
    }

    // URLFetch Implementation
    try {
      if (url != null && !url.isEmpty()) {
        if (!url.endsWith("/")) {
          url += "/v2/devices";
        } else {
          url += "v2/devices";
        }
      }

      URL urlObj = new URL(url);
      HttpResponse
          response =
          urlFetchService.post(urlObj, devicesToAddJsonStr.getBytes(), userName, password, 15);

      if (response.getResponseCode() != HTTP_STATUS_CREATED) {
        xLogger.severe(
            "Failed to register devices with Asset Monitoring Service. Response Code: {0}. Response Content: {1}",
            response.getResponseCode(),
            (response.getContent() != null ? new String(response.getContent()) : null));
        throw new ServiceException(
            response.getContent() != null ? new String(response.getContent()) : null);
      } else {
        xLogger.info(
            "Successfully registered devices with Asset Monitoring Service, Response Code: {0}. Response Content: {1}",
            response.getResponseCode(),
            (response.getContent() != null ? new String(response.getContent()) : null));
      }
    } catch (Exception e) {
      xLogger
          .severe("{0} when trying to register devices with Asset Monitoring Service. Message: {1}",
              e.getClass().getName(), e.getMessage());
      throw new ServiceException(e.getMessage());
    }

    xLogger.fine("Exiting registerDevices");
  }

  public static String getAssetsByTag(String tag, String q, String assetType, Integer workingStatus,
                                      Integer alarmType, Integer alarmDuration, Integer awr,
                                      Integer page, Integer size) {
    try {
      String urlStr = "v2/tags/" + encodeURLParameters(tag) + "/devices?";

      if (q != null && !q.isEmpty()) {
        urlStr += "&q=" + q;
      }

      if (assetType != null) {
        urlStr += "&typ=" + assetType;
      }

      if (workingStatus != null) {
        urlStr += "&ws=" + (workingStatus - 1);
      }

      if (alarmType != null) {
        urlStr += "&aType=" + alarmType;

        if (alarmDuration != null) {
          urlStr += "&dur=" + alarmDuration;
        }
      }
      if (awr != null) {
        urlStr += "&awr=" + awr;
      }

      urlStr += "&page=" + page + "&size=" + size;
      return get(urlStr);
    } catch (Exception e) {
      xLogger.severe("{0} when getting tagged device for tag {1}: {2}", e.getClass().getName(), tag,
          e.getMessage(), e);
    }
    return null;
  }

  /**
   * Get the asset details as string from json
   *
   * @return String
   */
  public static String getAssetDetails(String manufacturerId, String serialNumber) {
    try {
      return get("v2/devices/" + manufacturerId + "/" + encodeURLParameters(serialNumber));
    } catch (Exception e) {
      xLogger.severe("{0} when getting asset details: {1}, {2}", e.getMessage(), manufacturerId,
          serialNumber, e);
    }
    return null;
  }

  /**
   * Get the asset model from the json
   *
   * @return AssetModel
   */
  public static AssetModel getAssetModel(String manufacturerId, String serialNumber) {
    return new Gson().fromJson(getAssetDetails(manufacturerId, serialNumber), AssetModel.class);
  }

  public static String getAssetRelations(String manufacturerId, String assetId) {
    try {
      return get("v2/devices/relation/" + manufacturerId + "/" + encodeURLParameters(assetId));
    } catch (Exception e) {
      xLogger.severe("{0} when getting asset relation: {1}, {2}", e.getMessage(), manufacturerId,
          assetId, e);
    }
    return null;
  }

  public static void createOrUpdateAssetRelations(String data) throws ServiceException {
    post("v2/devices/relation", data);
  }

  public static TemperatureResponse getTemperatureResponse(String vendorId,
                                                                           String deviceId,
                                                                           String mpOrSensorId,
                                                                           Integer assetType,
                                                                           long startTime,
                                                                           long endTime,
                                                                           int pageNum, int size) {
    GsonBuilder gsonBuilder = new GsonBuilder();
    Gson gson = gsonBuilder.create();

    return gson.fromJson(
        getTemperatures(vendorId, deviceId, mpOrSensorId, assetType, startTime, endTime, pageNum,
            size), TemperatureResponse.class);
  }

  // Get temperatures
  public static String getTemperatures(String vendorId, String deviceId, String mpOrSensorId,
                                       Integer assetType, long startTime, long endTime, int pageNum,
                                       int size) {
    try {
      String
          path =
          "v3/temp/" + vendorId + "/" + encodeURLParameters(deviceId) + "/" + mpOrSensorId;
      if (Objects.equals(IAsset.MONITORING_ASSET, assetType)) {
        path = "v2/temp/" + vendorId + "/" + encodeURLParameters(deviceId) + "/" + mpOrSensorId;
      }

      if (startTime > 0 && endTime > 0) {
        path += "/" + startTime + "/" + endTime;
      }
      path += "?page=" + pageNum;
      path += "&size=" + size;
      return get(path);
    } catch (Exception e) {
      xLogger.severe("{0} when getting temperature for asset: {1}, {2}", e.getMessage(), vendorId,
          deviceId, e);
    }
    return null;
  }

  public static void registerConfig(AssetModel assetModel) throws ServiceException {
    AssetModels.AssetConfigModel assetConfigModel = new AssetModels.AssetConfigModel();
    assetConfigModel.vId = assetModel.vId;
    assetConfigModel.dId = assetModel.dId;
    assetConfigModel.configuration = assetModel.config;
    registerConfig(gson.toJson(assetConfigModel));
  }

  public static void registerConfig(String config) throws ServiceException {
    post("v2/config", config);
  }

  public static String getConfig(String vendorId, String deviceId) {
    return get("v2/apps/config/" + vendorId + "/" + encodeURLParameters(deviceId));
  }

  public static String getAssetStats(String vendorId, String deviceId, String from, String to) {
    return get(
        "v2/stats/" + vendorId + "/" + encodeURLParameters(deviceId) + "/" + from + "/" + to);
  }

  public static String getRecentAlerts(String vendorId, String deviceId, String page, String size) {
    return get(
        "v2/alarms/recent/" + vendorId + "/" + encodeURLParameters(deviceId) + "?page=" + page
            + "&size=" + size);
  }

  public static String getChildTagSummary(String tag) {
    return get("v2/tags/" + encodeURLParameters(tag) + "/sub-summary");
  }

  public static String getTagAbnormalDevices(String tag) {
    return get("v2/tags/" + encodeURLParameters(tag) + "/abnormal-devices?page=1&size=20");
  }

  public static String getTagSummary(String domainId) {
    return get("v2/tags/" + encodeURLParameters(domainId) + "/device-counts");
  }

  // Private method to generate Asset events, for high excursion, low excursion and incursion
  public static void generateAssetEvents(Long domainId, EventSpec eventSpec,
                                         IAssetStatus assetStatus, IAsset asset, int eventType) {
    xLogger.fine("Entered generateTemperatureEvents");
    Map<String, EventSpec.ParamSpec> paramSpecs = eventSpec.getParamSpecs();
    // Multiple low excursion and high excursion events can be present based on different "remindminsafter" parameter
    if (paramSpecs == null || paramSpecs.isEmpty()) {
      return;
    }
    // Iterate through paramSpecs and generate events for each of the remind mins after param
    for (EventSpec.ParamSpec paramSpec : paramSpecs.values()) {
      // Get remindMinsAfter
      String remindMinsAfterStr = null;
      Map<String, Object> eventParams = paramSpec.getParams();
      Map<String, Object> params = new HashMap<String, Object>(1);
      if (eventParams != null && !eventParams.isEmpty()) {
        if (eventType == IEvent.HIGH_EXCURSION || eventType == IEvent.LOW_EXCURSION
            || eventType == IEvent.HIGH_WARNING || eventType == IEvent.LOW_WARNING
            || eventType == IEvent.HIGH_ALARM || eventType == IEvent.LOW_ALARM
            || IEvent.ASSET_ALARM_GROUP.contains(eventType)) {
          remindMinsAfterStr = (String) eventParams.get(EventConstants.PARAM_REMINDMINSAFTER);
        }
        if (remindMinsAfterStr != null && !remindMinsAfterStr.isEmpty()) {
          // Form event params for every invItem
          params.put(EventConstants.PARAM_REMINDMINSAFTER, remindMinsAfterStr);
        }
      }
      params.put(EventConstants.EVENT_TIME, assetStatus.getTs());

      if (assetStatus.getTags() != null && !assetStatus.getTags().isEmpty()) {
        params.put(EventConstants.PARAM_ENTITYTAGSTOEXCLUDE, assetStatus.getTags());
      }

      try {
        EventPublisher.generate(domainId, eventType, params,
            JDOUtils.getImplClass(IAssetStatus.class).getName(), String.valueOf(assetStatus.getId()),
            null);
      } catch (EventGenerationException e) {
        xLogger.warn("Exception when generating event for asset {0} in domain {1}: {2}",
            asset.getSerialId(), domainId, e.getMessage(), e);
      }
    }
  }

  public static void pushDeviceConfig(String requestData) throws ServiceException {
    post("v2/devices/config", requestData);
  }

    /*public static void deleteAsset(String requestData){
        post("v2/devices/delete", requestData);
    }*/

  public static void removeKioskLink(Long kioskId) {
    AssetManagementService ams = null;
    PersistenceManager pm = PMF.get().getPersistenceManager();
    try {
      ams = Services.getService(AssetManagementServiceImpl.class);
      List<IAsset> assets = ams.getAssetsByKiosk(kioskId);

      if (assets != null && !assets.isEmpty()) {
        List<AssetModel> assetModels = new ArrayList<>(assets.size());
        for (IAsset asset : assets) {
          asset.setKioskId(null);
          asset.setUpdatedOn(new Date());

          AssetModel assetModel = buildFilterModel(asset);
          //Getting linked domains, because asset is disassociated from kiosk
          Set<Long>
              domainIds =
              DomainsUtil.getDomainLinks(asset.getDomainId(), IDomainLink.TYPE_PARENT, true);
          assetModel.tags = new ArrayList<>(domainIds.size());
          for (Long currentDomainId : domainIds) {
            assetModel.tags.add(String.valueOf(currentDomainId));
          }
          assetModels.add(assetModel);
        }
        pm.makePersistentAll(assets);

        //Updating tags for asset in AMS.
        registerDevices(gson.toJson(new AssetModels.AssetRegistrationModel(assetModels)));
      }
    } catch (Exception e) {
      xLogger.severe("Error while removing asset link for the kiosk {0}", kioskId, e);
    } finally {
      pm.close();
    }
  }

  /**
   * Update asset tags in AMS for the given assets
   * @param assets List of assets to be moved
   * @param assetTagsToRegister
   */
  public static void updateAssetTags(List<IAsset> assets, List<String> assetTagsToRegister) {
    try {
      List<AssetModel> assetModels = new ArrayList<>(assets.size());
      for (IAsset asset : assets) {
        AssetModel assetModel = buildFilterModel(asset);
        assetModel.tags = assetTagsToRegister;
        assetModels.add(assetModel);
      }
      //Updating tags for asset in AMS.
      registerDevices(gson.toJson(new AssetModels.AssetRegistrationModel(assetModels)));
    } catch (Exception e) {
      xLogger.severe("Error while moving assets {0}", assets.toString(), e);
    }
  }

  public static void updateAssetTags(Long kioskId, List<String> assetTagsToRegister) {
    AssetManagementService ams;
    try {
      ams = Services.getService(AssetManagementServiceImpl.class);
      List<IAsset> assets = ams.getAssetsByKiosk(kioskId);
      if (assets != null && !assets.isEmpty()) {
       updateAssetTags(assets, assetTagsToRegister);
      }
    } catch (Exception e) {
      xLogger.severe("Error while updating asset tags for the kiosk {0}", kioskId, e);
    }
  }

  public static void deleteRelationShip(IAsset asset, Long domainId) {
    AssetManagementService ams;
    try {
      boolean isRelated = true;
      //Deleting asset relationship in LS
      ams = Services.getService(AssetManagementServiceImpl.class);
      if (Objects.equals(asset.getType(), IAsset.TEMP_DEVICE)) {
        IAssetRelation assetRelation = ams.getAssetRelationByRelatedAsset(asset.getId());
        if (assetRelation != null) {
          asset = ams.getAsset(assetRelation.getAssetId());
        } else {
          //If there is no relationship for temperature logger, we don't need to do any deletion of relation
          isRelated = false;
        }
      }
      if (isRelated) {
        ams.deleteAssetRelation(asset.getId(), domainId, asset);

        AssetRelationModel assetRelationModel = new AssetRelationModel();
        AssetRelationModel.AssetRelations assetRelations = new AssetRelationModel.AssetRelations(
            asset.getVendorId(), asset.getSerialId(),
            new ArrayList<AssetRelationModel.Relation>(1)
        );
        assetRelationModel.data.add(assetRelations);

        //Deleting asset relationship in AMS
        AssetUtil.createOrUpdateAssetRelations(new Gson().toJson(assetRelationModel));
      }
    } catch (Exception e) {
      xLogger.severe("Error while deleting relationship for the asset {0}", asset.getId(), e);
    }
  }

  public static JsonElement constructDeviceMetaJsonFromMap(Map<String, Object> metaDataMap) {
    JSONObject jsonObject = new JSONObject();
    try {
      for (String key : metaDataMap.keySet()) {
        if (metaDataMap.get(key) != null && !((String) metaDataMap.get(key)).isEmpty()) {
          String keys[] = key.split("\\.");
          JSONObject jsonObjectTmp = jsonObject;
          for (int i = 0; i < keys.length; i++) {
            if (!jsonObjectTmp.has(keys[i])) {
              jsonObjectTmp.put(keys[i], new JSONObject());
            }
            if (i == keys.length - 1) {
              jsonObjectTmp.put(keys[i], metaDataMap.get(key));
            } else {
              jsonObjectTmp = jsonObjectTmp.getJSONObject(keys[i]);
            }
          }
        }
      }
    } catch (Exception e) {
      xLogger.warn("{} while constructing device meta data json from map {}", e.getMessage(),
          metaDataMap.toString(), e);
    }
    return gson.fromJson(jsonObject.toString(), JsonElement.class);
  }

  @SuppressWarnings("unchecked")
  public static Map<String, String> constructDeviceMetaDataFromJSON(String parentKey,
                                                                    Map<String, Object> result) {
    Map<String, String> metaDataMap = new HashMap<>(1);
    String currentKey;
    for (String key : result.keySet()) {
      Object object = result.get(key);
      currentKey = key;
      if (parentKey != null) {
        currentKey = parentKey + "." + key;
      }
      if (object != null) {
        if (object instanceof LinkedTreeMap) {
          metaDataMap.putAll(constructDeviceMetaDataFromJSON(currentKey, (LinkedTreeMap) object));
        } else {
          metaDataMap.put(currentKey, object.toString());
        }
      }
    }
    return metaDataMap;
  }

  public static void createAssetRelationship(Long domainId, IAsset fridgeAsset, IAsset sensorAsset,
                                             List<String> assetTags)
      throws ServiceException {
    //Create relationship
    AssetSystemConfig.Model
        assetModel =
        AssetUtil
            .getAssetModel(sensorAsset.getType(), sensorAsset.getVendorId(), sensorAsset.getModel(),
                domainId);
    AssetRelationModel.AssetRelations
        assetRelations =
        new AssetRelationModel.AssetRelations(fridgeAsset.getVendorId(), fridgeAsset.getSerialId());
    AssetSystemConfig.Asset fridgeAssetConfig = AssetUtil.getAssetConfig(fridgeAsset.getType());

    if (fridgeAssetConfig != null && assetModel != null) {
      for (AssetSystemConfig.MonitoringPosition monitoringPosition : fridgeAssetConfig.monitoringPositions) {
        for (AssetSystemConfig.Sensor sensor : assetModel.sns) {
          if (sensor.mpId != null && Objects.equals(monitoringPosition.mpId, sensor.mpId)) {
            AssetRelationModel.Relation relation = new AssetRelationModel.Relation();
            relation.dId = sensorAsset.getSerialId();
            relation.vId = sensorAsset.getVendorId();
            relation.sId = sensor.name;
            relation.mpId = sensor.mpId;
            relation.typ = IAssetRelation.MONITORED_BY;
            assetRelations.ras.add(relation);
          }
        }
      }

      AssetRelationModel assetRelationModel = new AssetRelationModel();
      assetRelationModel.data.add(assetRelations);

      //Creating asset relationship in AMS
      AssetUtil.createOrUpdateAssetRelations(new Gson().toJson(assetRelationModel));

      //Creating asset relationship in LS
      List<IAssetRelation> assetRelationList;
      try {
        assetRelationList = buildAssetRelations(assetRelationModel, assetTags);
        for (IAssetRelation assetRelation : assetRelationList) {
          AssetManagementService ams = Services.getService(AssetManagementServiceImpl.class);
          ams.createOrUpdateAssetRelation(domainId, assetRelation);
        }
      } catch (ServiceException e) {
        xLogger
            .warn("{0} while creating asset relating asset {}, {} to asset {}, {}", e.getMessage(),
                fridgeAsset.getSerialId(), fridgeAsset.getVendorId(), sensorAsset.getSerialId(),
                sensorAsset.getVendorId(), e);
        throw new ServiceException(
            "Unable to create asset relation, received error: " + e.getMessage());
      }
    }
  }

  private static List<IAssetRelation> buildAssetRelations(AssetRelationModel assetRelationModel,
                                                         List<String> tags)
      throws ServiceException {
    List<IAssetRelation> assetRelationList = new ArrayList<>(assetRelationModel.data.size());
    for (AssetRelationModel.AssetRelations assetRelations : assetRelationModel.data) {
      assetRelationList.add(buildAssetRelation(assetRelations, null, tags));
    }

    return assetRelationList;
  }

  private static IAssetRelation buildAssetRelation(AssetRelationModel.AssetRelations assetRelations,
                                                  String userId, List<String> tags) throws ServiceException {
    IAssetRelation assetRelation = JDOUtils.createInstance(IAssetRelation.class);
    // We support only on relation at this time.
    if (assetRelations.ras != null && assetRelations.ras.size() > 0) {
      AssetRelationModel.Relation relation = assetRelations.ras.get(0);

      try {
        AssetManagementService
            assetManagementService =
            Services.getService(AssetManagementServiceImpl.class);
        IAsset asset, relationAsset = null;
        try {
          asset = assetManagementService.getAsset(assetRelations.vId, assetRelations.dId);
        } catch (ServiceException e) {
          xLogger.warn("{0} while getting asset {1}, {2}", e.getMessage(), assetRelations.vId,
              assetRelations.dId, e);
          throw new ServiceException(
              "Unable to fetch asset " + MsgUtil.bold(assetRelations.dId) + "(" + MsgUtil
                  .bold(assetRelations.vId) + ")");
        }

        try {
          relationAsset = assetManagementService.getAsset(relation.vId, relation.dId);
        } catch (ServiceException e) {
          //do nothing
        }
        if (relationAsset == null) {
          try {
            Map<String, Object> variableMap = new HashMap<>(5);
            Map<String, Object> metaDataMap = new HashMap<>(5);
            variableMap.put(AssetUtil.SERIAL_NUMBER, relation.dId);
            variableMap.put(AssetUtil.MANUFACTURER_NAME, relation.vId);
            variableMap.put(AssetUtil.ASSET_MODEL, AssetUtil.SENSOR_DEVICE_MODEL);
            variableMap.put(AssetUtil.ASSET_TYPE, IAsset.TEMP_DEVICE);
            variableMap.put(AssetUtil.TAGS, tags);
            metaDataMap.put(AssetUtil.DEV_MODEL, AssetUtil.SENSOR_DEVICE_MODEL);
            relationAsset =
                AssetUtil.verifyAndRegisterAsset(asset.getDomainId(), userId, asset.getKioskId(),
                    variableMap, metaDataMap);
          } catch (ServiceException se) {
            xLogger.warn("{0} while register asset {1}, {2}", se.getMessage(), relation.vId,
                relation.dId, se);
            throw new ServiceException(
                "Unable to register asset " + MsgUtil.bold(relation.dId) + "(" + MsgUtil
                    .bold(relation.vId) + "): " + se.getMessage());
          }
        }

        assetRelation.setAssetId(asset.getId());
        assetRelation.setRelatedAssetId(relationAsset.getId());
        assetRelation.setType(relation.typ);
      } catch (ServiceException e) {
        xLogger.warn("{0} while building asset relation for the asset {1}, {2}", e.getMessage(),
            assetRelations.vId, assetRelations.dId, e);
        throw new ServiceException(e);
      } catch (Exception e) {
        xLogger.warn("{0} while building asset relation for the asset {1}, {2}", e.getMessage(),
            assetRelations.vId, assetRelations.dId, e);
        throw new ServiceException("Unable to create asset relation");
      }
    }

    return assetRelation;
  }

  public static AssetModels.AssetPowerTransitions getAssetPowerTransition(String vendorId,
                                                                          String deviceId,
                                                                          Integer from,
                                                                          Integer to) {
    String
        json =
        get("v2/devices/power/" + vendorId + "/" + encodeURLParameters(deviceId) + "/" + from + "/"
            + to);

    if (json != null) {
      return gson.fromJson(json, AssetModels.AssetPowerTransitions.class);
    }
    return null;
  }

  private static String encodeURLParameters(String value) {
    if (StringUtils.isEmpty(value)) {
      return value;
    }

    try {
      value =
          URLEncoder.encode(URLEncoder.encode(value, "UTF-8").replaceAll("\\+", "%20"), "UTF-8");
    } catch (UnsupportedEncodingException e) {
      //do nothing
    }
    return value;
  }

  public static String decodeURLParameters(String value) {
    if (StringUtils.isEmpty(value)) {
      return value;
    }

    StringBuilder finalValue = new StringBuilder();
    if (value.contains("#")) {
      for (String character : value.split("#")) {
        if (StringUtil.isStringInteger(character)) {
          int charValue = Integer.parseInt(character);
          finalValue.append(Character.toString((char) charValue));
        } else {
          finalValue.append(character);
        }
      }
      return finalValue.toString();
    }

    return value;
  }
}

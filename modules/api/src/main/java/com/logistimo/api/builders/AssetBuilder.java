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

package com.logistimo.api.builders;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import com.logistimo.api.models.AssetBaseModel;
import com.logistimo.api.models.AssetDetailsModel;
import com.logistimo.api.models.EntityModel;
import com.logistimo.api.models.TemperatureDomainModel;
import com.logistimo.assets.AssetUtil;
import com.logistimo.assets.entity.IAsset;
import com.logistimo.assets.entity.IAssetRelation;
import com.logistimo.assets.models.AssetDataModel;
import com.logistimo.assets.models.AssetDeviceModel;
import com.logistimo.assets.models.AssetModel;
import com.logistimo.assets.models.AssetModels;
import com.logistimo.assets.models.AssetRelationModel;
import com.logistimo.assets.models.DeviceTempModel;
import com.logistimo.assets.models.DeviceTempsModel;
import com.logistimo.assets.models.Temperature;
import com.logistimo.assets.models.TemperatureResponse;
import com.logistimo.assets.service.AssetManagementService;
import com.logistimo.assets.service.impl.AssetManagementServiceImpl;
import com.logistimo.constants.Constants;
import com.logistimo.dao.JDOUtils;
import com.logistimo.domains.entity.IDomain;
import com.logistimo.domains.service.DomainsService;
import com.logistimo.domains.service.impl.DomainsServiceImpl;
import com.logistimo.entities.entity.IKiosk;
import com.logistimo.entities.service.EntitiesService;
import com.logistimo.entities.service.EntitiesServiceImpl;
import com.logistimo.logger.XLog;
import com.logistimo.pagination.Results;
import com.logistimo.services.ServiceException;
import com.logistimo.services.Services;
import com.logistimo.users.entity.IUserAccount;
import com.logistimo.users.service.UsersService;
import com.logistimo.users.service.impl.UsersServiceImpl;
import com.logistimo.utils.LocalDateUtil;
import com.logistimo.utils.MsgUtil;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

/**
 * Created by kaniyarasu on 03/11/15.
 */
public class AssetBuilder {

  private static final XLog xLogger = XLog.getLog(AssetBuilder.class);
  private static final String DEV = "dev";
  private static final String YOM = "yom";
  private static final String MDL = "mdl";

  GsonBuilder gsonBuilder = new GsonBuilder();
  Gson gson = gsonBuilder.create();
  UserBuilder userBuilder = new UserBuilder();

  public IAsset buildAsset(AssetModel assetModel, String userId, Boolean isCreate) {
    return buildAsset(JDOUtils.createInstance(IAsset.class), assetModel, userId, isCreate);
  }

  public IAsset buildAsset(IAsset asset, AssetModel assetModel, String userId, Boolean isCreate) {
    asset.setSerialId(assetModel.dId);
    asset.setVendorId(assetModel.vId);
    asset.setKioskId(assetModel.kId);
    asset.setType(assetModel.typ);
    asset.setLocationId(assetModel.lId);
    if(assetModel.meta != null && assetModel.meta.getAsJsonObject() != null && assetModel.meta.getAsJsonObject().getAsJsonObject(DEV) != null) {
      if(assetModel.meta.getAsJsonObject().getAsJsonObject(DEV).get(YOM) != null) {
        asset.setYom(assetModel.meta.getAsJsonObject().getAsJsonObject(DEV).get(YOM).getAsInt());
      }
      if(assetModel.meta.getAsJsonObject().getAsJsonObject(DEV).get(MDL) != null) {
        asset.setModel(assetModel.meta.getAsJsonObject().getAsJsonObject(DEV).get(MDL).getAsString());
      }
    }
    asset.setNsId(assetModel.dId.toLowerCase().trim());
    if (assetModel.ons != null && assetModel.ons.size() > 0) {
      asset.setOwners(assetModel.ons);
    } else {
      asset.setOwners(null);
    }
    if (assetModel.mts != null && assetModel.mts.size() > 0) {
      asset.setMaintainers(assetModel.mts);
    } else {
      asset.setMaintainers(null);
    }
    if (userId != null) {
      if (isCreate) {
        asset.setCreatedBy(userId);
      }
      asset.setUpdatedBy(userId);
    } else {
      asset.setCreatedBy(assetModel.cb);
      asset.setUpdatedBy(assetModel.ub);
    }
    asset.setUpdatedOn(new Date());
    return asset;
  }

  public Results buildAssetResults(Results results, Locale locale, String timezone) {
    List<AssetDetailsModel> assetDetailsModelList = new ArrayList<>(results.getSize());
    Map<Long, String> domainNames = new HashMap<>(1);
    Map<Long, IKiosk> kioskMap = new HashMap<>(1);

    int sno = results.getOffset() + 1;
    UsersService as = null;
    as = Services.getService(UsersServiceImpl.class);
    for (Object asset : results.getResults()) {
      AssetDetailsModel
          assetDetailsModel =
          buildAssetDetailsModel((IAsset) asset, domainNames, kioskMap, locale, timezone);
      assetDetailsModel.sno = sno++;
      if (as != null) {
        try {
          IUserAccount ua = as.getUserAccount(assetDetailsModel.lub);
          assetDetailsModel.lubn = ua.getFullName();
        } catch (Exception e) {
          xLogger.warn("Error in fetching user details", e);
        }
      }
      assetDetailsModelList.add(assetDetailsModel);
    }
    return new Results(assetDetailsModelList, results.getCursor(), results.getNumFound(),
        results.getOffset());
  }

  public AssetDetailsModel buildAssetDetailsModel(IAsset asset, Map<Long, String> domainNames,
                                                  Map<Long, IKiosk> kioskMap, Locale locale,
                                                  String timezone) {
    AssetDetailsModel assetDetailsModel = new AssetDetailsModel();
    String domainName = domainNames != null ? domainNames.get(asset.getDomainId()) : null;
    assetDetailsModel.setId(asset.getId());
    assetDetailsModel.setdId(asset.getSerialId());
    assetDetailsModel.setvId(asset.getVendorId());
    assetDetailsModel.con = asset.getCreatedOn();
    assetDetailsModel.typ = asset.getType();
    assetDetailsModel.setSdid(asset.getDomainId());
    assetDetailsModel.mdl = asset.getModel();
    assetDetailsModel.ts = LocalDateUtil.format(asset.getCreatedOn(), locale, timezone);
    if (asset.getUpdatedOn() != null) {
      assetDetailsModel.lts = LocalDateUtil.format(asset.getUpdatedOn(), locale, timezone);
    }
    assetDetailsModel.rus = asset.getCreatedBy();
    assetDetailsModel.lub = asset.getUpdatedBy();

    try {
      EntitiesService accountsService = Services.getService(EntitiesServiceImpl.class,locale);
      UsersService usersService = Services.getService(UsersServiceImpl.class,locale);
      EntityModel entityModel = new EntityModel();
      if (asset.getKioskId() != null) {
        IKiosk ki = kioskMap != null ? kioskMap.get(asset.getKioskId()) : null;
        if (ki == null) {
          ki = accountsService.getKiosk(asset.getKioskId(), false);
          if (kioskMap != null) {
            kioskMap.put(asset.getKioskId(), ki);
          }
        }
        if (ki != null) {
          entityModel.id = ki.getKioskId();
          entityModel.nm = ki.getName();
          entityModel.ct = ki.getCity();
          entityModel.tlk = ki.getTaluk();
          entityModel.ds = ki.getDistrict();
          entityModel.st = ki.getState();
          entityModel.ctr = ki.getCountry();
          entityModel.ln = ki.getLongitude();
          entityModel.lt = ki.getLatitude();

          assetDetailsModel.setEntity(entityModel);
        }
      }

      if (asset.getOwners() != null) {
        List<IUserAccount> userAccounts = new ArrayList<>(asset.getOwners().size());
        for (String userId : asset.getOwners()) {
          userAccounts.add(usersService.getUserAccount(userId));
        }
        assetDetailsModel.ons =
            userBuilder.buildUserModels(userAccounts, locale, timezone, true);
      }

      if (asset.getMaintainers() != null) {
        List<IUserAccount> userAccounts = new ArrayList<>(asset.getMaintainers().size());
        for (String userId : asset.getMaintainers()) {
          userAccounts.add(usersService.getUserAccount(userId));
        }
        assetDetailsModel.mts =
            userBuilder.buildUserModels(userAccounts, locale, timezone, true);
      }
    } catch (Exception e) {
      //do nothing
    }

    DomainsService ds = Services.getService(DomainsServiceImpl.class);
    if (domainName == null) {
      IDomain domain = null;
      try {
        domain = ds.getDomain(asset.getDomainId());
      } catch (Exception e) {
        //ignore
      }
      if (domain != null) {
        domainName = domain.getName();
      } else {
        domainName = Constants.EMPTY;
      }
      if (domainNames != null) {
        domainNames.put(asset.getDomainId(), domainName);
      }
    }
    assetDetailsModel.sdname = domainName;

    return assetDetailsModel;
  }

  public List<AssetModel> buildAssetFilterModel(String jsonResult) {
    if (jsonResult != null) {
      List<AssetModel> assetModelList = new ArrayList<>(10);
      for (AssetModel assetModel : gson
          .fromJson(jsonResult, AssetModels.AssetResponseModel.class).data) {
        try {
          assetModel.mdl =
              assetModel.meta.getAsJsonObject().getAsJsonObject("dev").get("mdl").getAsString();
        } catch (Exception e) {
          //do nothing
        }
        assetModelList.add(assetModel);
      }
      return assetModelList;
    }

    return null;
  }

  public List<AssetBaseModel> buildAssets(AssetDataModel results)
      throws ServiceException {
    List<AssetBaseModel> detailsModel = new ArrayList<>(1);
    if (results != null) {
      for (AssetDeviceModel dModel : results.data) {
        AssetBaseModel model = buildAssetModel(dModel.vId, dModel.dId);
        if (model != null) {
          detailsModel.add(model);
        }
      }
    }
    return detailsModel;
  }

  public AssetBaseModel buildAssetModel(String vendorId, String deviceId)
      throws ServiceException {
    AssetBaseModel model = new AssetBaseModel();
    AssetManagementService ams = Services.getService(AssetManagementServiceImpl.class);
    IAsset asset = ams.getAsset(vendorId, deviceId);
    if (asset != null) {
      model.setId(asset.getId());
      model.setSdid(asset.getDomainId());
      model.setdId(asset.getSerialId());
      model.setvId(vendorId);
      if (asset.getKioskId() != null) {
        EntitiesService es = Services.getService(EntitiesServiceImpl.class);
        IKiosk k = es.getKiosk(asset.getKioskId());
        if (k != null) {
          EntityModel entityModel = new EntityModel();
          entityModel.id = k.getKioskId();
          entityModel.nm = k.getName();
          entityModel.ct = k.getCity();
          entityModel.ds = k.getDistrict();
          entityModel.st = k.getState();
          entityModel.ctr = k.getCountry();
          entityModel.tlk = k.getTaluk();
          model.setEntity(entityModel);
        }
      }
      return model;
    }
    return null;
  }
  public Results buildAssetsFromJson(String results, int size, Locale locale, String timezone,
                                     int offset) {
    List<AssetDetailsModel> assetDetailsModelList = new ArrayList<>(1);
    Map<Long, String> domainNames = new HashMap<>(1);
    Map<Long, IKiosk> kioskMap = new HashMap<>(1);

    if (results != null) {
      AssetModels.AssetResponseModel
          assetResponseModel =
          new Gson().fromJson(results, AssetModels.AssetResponseModel.class);
      for (AssetModel assetModel : assetResponseModel.data) {
        AssetDetailsModel
            assetDetailsModel =
            buildAssetDetailsModel(assetModel, domainNames, kioskMap, locale, timezone);
        if (assetDetailsModel != null) {
          assetDetailsModel.sno = ++offset;
          assetDetailsModelList.add(assetDetailsModel);
        }
      }
    }

    return new Results(assetDetailsModelList, null, assetDetailsModelList.size(), size);
  }

  public AssetDetailsModel buildAssetModelFromJson(String results, Locale locale, String timezone) {
    if (results != null) {
      return buildAssetDetailsModel(new Gson().fromJson(results, AssetModel.class), null, null,
          locale, timezone);
    }

    return null;
  }

  public AssetDetailsModel buildAssetDetailsModel(AssetModel assetModel,
                                                  Map<Long, String> domainNames,
                                                  Map<Long, IKiosk> kioskMap, Locale locale,
                                                  String timezone) {
    AssetDetailsModel assetDetailsModel = new AssetDetailsModel();
    IAsset asset;
    String domainName = null;
    assetDetailsModel.setdId(assetModel.dId);
    assetDetailsModel.setvId(assetModel.vId);
    assetDetailsModel.meta = assetModel.meta;
    assetDetailsModel.tags = assetModel.tags;
    assetDetailsModel.lId = assetModel.lId;
    assetDetailsModel.cb = assetModel.cb;
    assetDetailsModel.ub = assetModel.ub;
    assetDetailsModel.sns = assetModel.sns;
    assetDetailsModel.rel = assetModel.rel;
    assetDetailsModel.tmp = assetModel.tmp;
    assetDetailsModel.iCon = assetModel.co;

    Map<Integer, Boolean> isMonitoredAssetActive = new HashMap<>(1);
    Map<Integer, String> inactiveSince = new HashMap<>(1);
    assetDetailsModel.alrm = assetModel.alrm;
    //Formatting data for display
    if (assetDetailsModel.alrm != null) {
      for (AssetModels.TempDeviceAlarmModel tempDeviceAlarmModel : assetDetailsModel.alrm) {
        tempDeviceAlarmModel.ftime = formatDate(tempDeviceAlarmModel.time, locale, timezone);

        if (tempDeviceAlarmModel.mpId != null && tempDeviceAlarmModel.typ == 4) {
          if (tempDeviceAlarmModel.stat == 0) {
            isMonitoredAssetActive.put(tempDeviceAlarmModel.mpId, true);
          } else {
            inactiveSince.put(tempDeviceAlarmModel.mpId, tempDeviceAlarmModel.ftime);
            isMonitoredAssetActive.put(tempDeviceAlarmModel.mpId, false);
          }
        }

        if (tempDeviceAlarmModel.stat > 0) {
          assetDetailsModel.iDa = true;
        }
      }
    }
    //Formatting date for display
    if (assetDetailsModel.tmp != null) {
      for (AssetModels.AssetStatus assetStatus : assetDetailsModel.tmp) {
        if (assetStatus.time != null) {
          assetStatus.ftime = formatDate(assetStatus.time, locale, timezone);
        }

        if (assetStatus.stut != null) {
          assetStatus.fstut = formatDate(assetStatus.stut, locale, timezone);
        }

        assetStatus.isActive =
            isMonitoredAssetActive.containsKey(assetStatus.mpId) ? isMonitoredAssetActive
                .get(assetStatus.mpId) : true;

        if (!assetStatus.isActive) {
          assetStatus.fstut =
              inactiveSince.containsKey(assetStatus.mpId) ? inactiveSince.get(assetStatus.mpId)
                  : assetStatus.fstut;
        }
      }
    }
    assetDetailsModel.cfg = assetModel.cfg;
    if (assetDetailsModel.cfg != null && assetDetailsModel.cfg.stut != null) {
      assetDetailsModel.cfg.fstut = formatDate(assetDetailsModel.cfg.stut, locale, timezone);
    }
    if (assetDetailsModel.cfg != null && assetDetailsModel.cfg.stub != null) {
      try {
        UsersService service = Services.getService(UsersServiceImpl.class);
        IUserAccount ua = service.getUserAccount(assetDetailsModel.cfg.stub);
        assetDetailsModel.cfg.stubn = ua.getFullName();
      } catch (Exception e) {
        xLogger.warn("Error while fetching the user details for {0} and with asset: {1}",
            assetDetailsModel.cfg.stub, assetModel.dId, e);
      }
    }
    assetDetailsModel.ws = assetModel.ws;
    if (assetDetailsModel.ws != null && assetDetailsModel.ws.stut != null) {
      assetDetailsModel.ws.fstut = formatDate(assetDetailsModel.ws.stut, locale, timezone);
    }
    assetDetailsModel.drdy = assetModel.drdy;
    if (assetDetailsModel.drdy != null && assetDetailsModel.drdy.time != null) {
      assetDetailsModel.drdy.fTime = formatDate(assetDetailsModel.drdy.time, locale, timezone);
    }
    assetDetailsModel.typ = assetModel.typ;
    if (assetModel.meta != null && assetModel.meta.getAsJsonObject() != null
        && assetModel.meta.getAsJsonObject().getAsJsonObject("dev") != null
        && assetModel.meta.getAsJsonObject().getAsJsonObject("dev").get("mdl") != null) {
      assetDetailsModel.mdl =
          assetModel.meta.getAsJsonObject().getAsJsonObject("dev").get("mdl").getAsString();
    } else {
      assetDetailsModel.mdl = "";
    }
    try {
      AssetManagementService
          assetManagementService =
          Services.getService(AssetManagementServiceImpl.class);
      asset = assetManagementService.getAsset(assetModel.vId, assetModel.dId);
      if (asset != null) {
        assetDetailsModel.setSdid(asset.getDomainId());
        assetDetailsModel.setId(asset.getId());
        assetDetailsModel.ts = LocalDateUtil.format(asset.getCreatedOn(), locale, timezone);
        if (asset.getUpdatedOn() != null) {
          assetDetailsModel.lts = LocalDateUtil.format(asset.getUpdatedOn(), locale, timezone);
        }
        assetDetailsModel.rus = asset.getCreatedBy();
        assetDetailsModel.lub = asset.getUpdatedBy();
        try {
          UsersService as = Services.getService(UsersServiceImpl.class);
          IUserAccount ua = as.getUserAccount(assetDetailsModel.lub);
          assetDetailsModel.lubn = ua.getFullName();
          ua = as.getUserAccount(assetDetailsModel.rus);
          assetDetailsModel.rusn = ua.getFullName();
        } catch (Exception e) {
          xLogger.warn("Error in fetching user details for assets", e);
        }

        if (domainNames != null) {
          domainName = domainNames.get(asset.getDomainId());
        }
      } else {
        throw new ServiceException("");
      }
    } catch (Exception e) {
      return null;
    }

    try {
      EntitiesService accountsService = Services.getService(EntitiesServiceImpl.class);
      UsersService usersService = Services.getService(UsersServiceImpl.class);
      EntityModel entityModel = new EntityModel();
      if (asset.getKioskId() != null) {
        IKiosk ki = kioskMap != null ? kioskMap.get(asset.getKioskId()) : null;
        if (ki == null) {
          ki = accountsService.getKiosk(asset.getKioskId(), false);
          if (kioskMap != null) {
            kioskMap.put(asset.getKioskId(), ki);
          }
        }
        if (ki != null) {
          entityModel.id = ki.getKioskId();
          entityModel.nm = ki.getName();
          entityModel.ct = ki.getCity();
          entityModel.ds = ki.getDistrict();
          entityModel.st = ki.getState();
          entityModel.ctr = ki.getCountry();
          entityModel.ln = ki.getLongitude();
          entityModel.lt = ki.getLatitude();
          entityModel.ds = ki.getDistrict();
          entityModel.tlk = ki.getTaluk();

          assetDetailsModel.setEntity(entityModel);
        }
      }

      if (asset.getOwners() != null) {
        List<IUserAccount> userAccounts = new ArrayList<>(asset.getOwners().size());
        for (String userId : asset.getOwners()) {
          userAccounts.add(usersService.getUserAccount(userId));
        }
        assetDetailsModel.ons =
            userBuilder.buildUserModels(userAccounts, locale, timezone, true);
      }

      if (asset.getMaintainers() != null) {
        List<IUserAccount> userAccounts = new ArrayList<>(asset.getMaintainers().size());
        for (String userId : asset.getMaintainers()) {
          userAccounts.add(usersService.getUserAccount(userId));
        }
        assetDetailsModel.mts =
            userBuilder.buildUserModels(userAccounts, locale, timezone, true);
      }
    } catch (Exception e) {
      //do nothing
    }

    DomainsService ds = Services.getService(DomainsServiceImpl.class);
    if (domainName == null) {
      IDomain domain = null;
      try {
        domain = ds.getDomain(asset.getDomainId());
      } catch (Exception e) {
        //xLogger.warn("Error while fetching Domain {0}", item.getDomainId());
      }
      if (domain != null) {
        domainName = domain.getName();
      } else {
        domainName = Constants.EMPTY;
      }
      if (domainNames != null) {
        domainNames.put(asset.getDomainId(), domainName);
      }
    }
    assetDetailsModel.sdname = domainName;

    return assetDetailsModel;
  }

  public List<AssetModel> buildFilterModels(List<IAsset> assetList) {
    List<AssetModel> assetModelList = new ArrayList<>(assetList.size());

    for (IAsset asset : assetList) {
      AssetModel assetModel = buildFilterModel(asset);
      if (assetModel != null) {
        assetModelList.add(assetModel);
      }
    }

    return assetModelList;
  }

  public AssetModel buildFilterModel(IAsset asset) {
    AssetModel assetModel = new AssetModel();
    assetModel.dId = asset.getSerialId();
    assetModel.vId = asset.getVendorId();
    assetModel.typ = asset.getType();
    assetModel.mdl = asset.getModel();
    assetModel.kId = asset.getKioskId();
    return assetModel;
  }

  public AssetModels.DeviceStatsModel buildDeviceStatsModel(String stats, Locale locale,
                                                            String timezone) {
    AssetModels.DeviceStatsModel deviceStatsModel = new AssetModels.DeviceStatsModel();
    if (stats != null && !stats.isEmpty()) {
      deviceStatsModel = gson.fromJson(stats, AssetModels.DeviceStatsModel.class);

      if (deviceStatsModel != null && deviceStatsModel.data != null
          && deviceStatsModel.data.size() > 0) {
        for (AssetModels.DeviceStatsModel.DeviceStatsResponse deviceStatsResponse : deviceStatsModel.data) {
          if (deviceStatsResponse != null && deviceStatsResponse.stats != null) {
            if (deviceStatsResponse.stats.low != null && deviceStatsResponse.stats.low.time != null
                && deviceStatsResponse.stats.low.time != 0) {
              deviceStatsResponse.stats.low.fTime =
                  formatDate(deviceStatsResponse.stats.low.time, locale, timezone);
            }

            if (deviceStatsResponse.stats.high != null
                && deviceStatsResponse.stats.high.time != null
                && deviceStatsResponse.stats.high.time != 0) {
              deviceStatsResponse.stats.high.fTime =
                  formatDate(deviceStatsResponse.stats.high.time, locale, timezone);
            }

            if (deviceStatsResponse.stats.batt != null
                && deviceStatsResponse.stats.batt.time != null
                && deviceStatsResponse.stats.batt.time != 0) {
              deviceStatsResponse.stats.batt.fTime =
                  formatDate(deviceStatsResponse.stats.batt.time, locale, timezone);
            }

            if (deviceStatsResponse.stats.dCon != null
                && deviceStatsResponse.stats.dCon.time != null
                && deviceStatsResponse.stats.dCon.time != 0) {
              deviceStatsResponse.stats.dCon.fTime =
                  formatDate(deviceStatsResponse.stats.dCon.time, locale, timezone);
            }

            if (deviceStatsResponse.stats.xSns != null
                && deviceStatsResponse.stats.xSns.time != null
                && deviceStatsResponse.stats.xSns.time != 0) {
              deviceStatsResponse.stats.xSns.fTime =
                  formatDate(deviceStatsResponse.stats.xSns.time, locale, timezone);
            }

            if (deviceStatsResponse.stats.errs != null) {
              for (AssetModels.DeviceStatsModel.DailyStatsDeviceError dailyStatsDeviceError : deviceStatsResponse.stats.errs) {
                if (dailyStatsDeviceError != null && dailyStatsDeviceError.time != null
                    && dailyStatsDeviceError.time != 0) {
                  dailyStatsDeviceError.fTime =
                      formatDate(dailyStatsDeviceError.time, locale, timezone);
                }
              }
            }
          }
        }
      }
    }

    return deviceStatsModel;
  }

  public AssetModels.TempDeviceRecentAlertsModel buildTempDeviceRecentAlertsModel(String json,
                                                                                  Locale locale,
                                                                                  String timezone) {
    AssetModels.TempDeviceRecentAlertsModel
        tempDeviceRecentAlertsModel =
        new AssetModels.TempDeviceRecentAlertsModel();

    if (json != null && !json.isEmpty()) {
      tempDeviceRecentAlertsModel =
          gson.fromJson(json, AssetModels.TempDeviceRecentAlertsModel.class);

      if (tempDeviceRecentAlertsModel.data != null && tempDeviceRecentAlertsModel.data.size() > 0) {
        for (AssetModels.TempDeviceRecentAlertsModel.TempDeviceAlertsModel tempDeviceAlertsModel : tempDeviceRecentAlertsModel.data) {
          if (tempDeviceAlertsModel.time != null) {
            tempDeviceAlertsModel.ft =
                LocalDateUtil.getTimeago(tempDeviceAlertsModel.time, timezone, locale, true);
          }
        }
      }
    }

    return tempDeviceRecentAlertsModel;
  }

  public DeviceTempsModel buildAssetTemperatures(
      TemperatureResponse temperatureResponse, long endTime, int samplingInt,
      String timezone) {
    DeviceTempsModel deviceTempsModel = new DeviceTempsModel();

    if (temperatureResponse != null && temperatureResponse.data != null) {
      deviceTempsModel.nPages = temperatureResponse.nPages;
      int recordCounter = 0;
      for (Temperature temperature : temperatureResponse.data) {
        if (endTime > 0 && samplingInt > 0 && (endTime - temperature.time) > (2 * samplingInt)) {
          long missingDataPoints = (endTime - temperature.time) / (samplingInt);
          for (int i = 1; i < missingDataPoints; i++) {
            DeviceTempModel deviceTempModel = new DeviceTempModel();
            deviceTempModel.time = (int) (endTime - i * samplingInt);
            deviceTempModel.label = getTemperatureLabel(endTime - i * samplingInt, timezone);
            deviceTempModel.value = "";
            deviceTempModel.pwa = "";
            deviceTempsModel.data.add(deviceTempModel);

            //Limiting number of temperature data points to 500.
            recordCounter++;
            if (recordCounter >= 300) {
              break;
            }
          }
        }
        //Limiting number of temperature data points to 500.
        recordCounter++;
        if (recordCounter >= 300) {
          break;
        } else {
          DeviceTempModel deviceTempModel = new DeviceTempModel();
          deviceTempModel.time = temperature.time;
          deviceTempModel.typ = temperature.typ;
          deviceTempModel.tmp = temperature.tmp;
          deviceTempModel.label = getTemperatureLabel(temperature.time, timezone);
          deviceTempModel.value = String.valueOf(temperature.tmp);
          deviceTempModel.pwa = temperature.pwa != null ? String.valueOf(temperature.pwa) : "";

          deviceTempsModel.data.add(deviceTempModel);
          endTime = temperature.time;
        }
      }

      Collections.reverse(deviceTempsModel.data);
    }

    return deviceTempsModel;
  }

  private String getTemperatureLabel(long time, String timezone) {
    Calendar calendar = Calendar.getInstance();
    calendar.setTimeZone(TimeZone.getTimeZone(timezone));
    calendar.setTimeInMillis(time * 1000);
    return calendar.getDisplayName(Calendar.MONTH, Calendar.SHORT, Locale.getDefault())
        + " " + String.format("%02d", calendar.get(Calendar.DAY_OF_MONTH))
        + " " + String.format("%02d", calendar.get(Calendar.HOUR_OF_DAY))
        + ":" + String.format("%02d", calendar.get(Calendar.MINUTE));
  }

  private String formatDate(long timeInSeconds, Locale locale, String timezone) {
    Calendar calendar = Calendar.getInstance();
    calendar.setTimeZone(TimeZone.getTimeZone(timezone));
    calendar.setTimeInMillis(timeInSeconds * 1000);

    return LocalDateUtil.format(calendar.getTime(), locale, timezone);
  }

  public TemperatureDomainModel buildTemperatureDomainModel(String location, String countryCode,
                                                            String stateCode) {
    TemperatureDomainModel temperatureDomainModel = new TemperatureDomainModel();

    temperatureDomainModel.tl = location;
    temperatureDomainModel.cc = countryCode;
    temperatureDomainModel.st = stateCode;

    return temperatureDomainModel;
  }

  public List<IAssetRelation> buildAssetRelations(AssetRelationModel assetRelationModel)
      throws ServiceException {
    List<IAssetRelation> assetRelationList = new ArrayList<>(assetRelationModel.data.size());
    for (AssetRelationModel.AssetRelations assetRelations : assetRelationModel.data) {
      assetRelationList.add(buildAssetRelation(assetRelations, null));
    }

    return assetRelationList;
  }

  public IAssetRelation buildAssetRelation(AssetRelationModel.AssetRelations assetRelations,
                                           String userId) throws ServiceException {
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
            metaDataMap.put(AssetUtil.DEV_MODEL, AssetUtil.SENSOR_DEVICE_MODEL);
            if(asset.getKioskId() != null) {
              EntitiesService
                  es =
                  Services.getService(EntitiesServiceImpl.class);
              variableMap.put(AssetUtil.TAGS, es.getAssetTagsToRegister(asset.getKioskId()));
            }
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

  public void buildAssetTags(AssetModel assetModel) {
    if(assetModel.kId != null) {
      try {
        // Knowing the kioskId, get the state
        EntitiesService as = Services.getService(EntitiesServiceImpl.class);
        assetModel.tags = as.getAssetTagsToRegister(assetModel.kId);
      } catch (Exception e) {
        xLogger
            .severe("{0} when trying to get tags for devices. Message: {1}", e.getClass().getName(),
                e.getMessage());
      }
    }
  }
}

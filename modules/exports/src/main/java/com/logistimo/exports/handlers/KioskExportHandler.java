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

import com.google.gson.Gson;

import com.logistimo.assets.AssetUtil;
import com.logistimo.assets.entity.IAsset;
import com.logistimo.assets.entity.IAssetRelation;
import com.logistimo.assets.models.AssetModel;
import com.logistimo.assets.service.AssetManagementService;
import com.logistimo.assets.service.impl.AssetManagementServiceImpl;
import com.logistimo.config.models.AssetSystemConfig;
import com.logistimo.config.models.ConfigurationException;
import com.logistimo.config.models.DomainConfig;
import com.logistimo.entities.entity.IKiosk;
import com.logistimo.entities.entity.UserToKiosk;
import com.logistimo.tags.TagUtil;
import com.logistimo.users.entity.IUserAccount;
import com.logistimo.users.service.UsersService;
import com.logistimo.users.service.impl.UsersServiceImpl;

import org.apache.commons.lang.StringEscapeUtils;
import com.logistimo.services.Resources;
import com.logistimo.services.ServiceException;
import com.logistimo.services.Services;
import com.logistimo.services.impl.PMF;
import com.logistimo.utils.BigUtil;
import com.logistimo.constants.CharacterConstants;
import com.logistimo.constants.Constants;
import com.logistimo.utils.LocalDateUtil;
import com.logistimo.utils.StringUtil;
import com.logistimo.logger.XLog;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.ResourceBundle;

import javax.jdo.PersistenceManager;
import javax.jdo.Query;

/**
 * Created by charan on 06/03/17.
 */
public class KioskExportHandler implements IExportHandler {

  private static final XLog xLogger = XLog.getLog(KioskExportHandler.class);

  private IKiosk kiosk;

  public KioskExportHandler(IKiosk kiosk){
    this.kiosk = kiosk;
  }

  // Get the CSV header for kiosks
  @Override
  public String getCSVHeader(Locale locale, DomainConfig dc, String type) {
    StringBuilder header = new StringBuilder();
    ResourceBundle bundle = Resources.get().getBundle("BackendMessages", locale);
    ResourceBundle messageBundle = Resources.get().getBundle("Messages", locale);
    ResourceBundle jsBundle = Resources.get().getBundle("JSMessages", locale);
    if ("assets".equalsIgnoreCase(type)) {
      header.append(bundle.getString("kiosk")).append(CharacterConstants.SPACE)
          .append(jsBundle.getString("id")).append(CharacterConstants.COMMA)
          .append(messageBundle.getString("customid.entity")).append(CharacterConstants.COMMA)
          .append(messageBundle.getString("kiosk.name")).append(CharacterConstants.COMMA)
          .append(messageBundle.getString("country")).append(CharacterConstants.COMMA)
          .append(bundle.getString("state")).append(CharacterConstants.COMMA)
          .append(bundle.getString("district")).append(CharacterConstants.COMMA)
          .append(messageBundle.getString("taluk")).append(CharacterConstants.COMMA)
          .append(messageBundle.getString("village")).append(CharacterConstants.COMMA)
          .append(messageBundle.getString("streetaddress")).append(CharacterConstants.COMMA)
          .append(messageBundle.getString("zipcode")).append(CharacterConstants.COMMA)
          .append(messageBundle.getString("latitude")).append(CharacterConstants.COMMA)
          .append(messageBundle.getString("longitude")).append(CharacterConstants.COMMA)
          .append(messageBundle.getString("accuracy")).append(CharacterConstants.SPACE)
          .append(CharacterConstants.O_BRACKET).append(messageBundle.getString("meters"))
          .append(CharacterConstants.C_BRACKET).append(CharacterConstants.COMMA)
          .append(jsBundle.getString("gps")).append(CharacterConstants.SPACE)
          .append(messageBundle.getString("errors.small")).append(CharacterConstants.COMMA)
          .append(bundle.getString("asset.type")).append(CharacterConstants.COMMA)
          .append(bundle.getString("bulk.asset.id")).append(CharacterConstants.COMMA)
          .append(bundle.getString("manufacturer")).append(CharacterConstants.COMMA)
          .append(bundle.getString("model")).append(CharacterConstants.COMMA)
          .append(bundle.getString("monitored.asset.manufacture.year")).append(CharacterConstants.COMMA)
          .append(bundle.getString("sensor.device.id")).append(CharacterConstants.COMMA)
          .append(bundle.getString("sim1")).append(CharacterConstants.COMMA)
          .append(bundle.getString("sim1.id")).append(CharacterConstants.COMMA)
          .append(bundle.getString("sim1.ntw.provider")).append(CharacterConstants.COMMA)
          .append(bundle.getString("sim2")).append(CharacterConstants.COMMA)
          .append(bundle.getString("sim2.id")).append(CharacterConstants.COMMA)
          .append(bundle.getString("sim2.ntw.provider")).append(CharacterConstants.COMMA)
          .append(bundle.getString("imei")).append(CharacterConstants.COMMA)
          .append(bundle.getString("monitoring.asset.manufacture.year"));
    } else {
      header.append(bundle.getString("kiosk")).append(CharacterConstants.SPACE)
          .append(jsBundle.getString("id")).append(CharacterConstants.COMMA)
          .append(messageBundle.getString("customid.entity")).append(CharacterConstants.COMMA)
          .append(messageBundle.getString("kiosk.name")).append(CharacterConstants.COMMA)
          .append(messageBundle.getString("users")).append(CharacterConstants.COMMA)
          .append(messageBundle.getString("country")).append(CharacterConstants.COMMA)
          .append(messageBundle.getString("state")).append(CharacterConstants.COMMA)
          .append(messageBundle.getString("district")).append(CharacterConstants.COMMA)
          .append(messageBundle.getString("taluk")).append(CharacterConstants.COMMA)
          .append(messageBundle.getString("village")).append(CharacterConstants.COMMA)
          .append(messageBundle.getString("streetaddress")).append(CharacterConstants.COMMA)
          .append(messageBundle.getString("zipcode")).append(CharacterConstants.COMMA)
          .append(messageBundle.getString("latitude")).append(CharacterConstants.COMMA)
          .append(messageBundle.getString("longitude")).append(CharacterConstants.COMMA)
          .append(messageBundle.getString("accuracy")).append(CharacterConstants.SPACE)
          .append(CharacterConstants.O_BRACKET).append(messageBundle.getString("meters"))
          .append(CharacterConstants.C_BRACKET).append(CharacterConstants.COMMA)
          .append(jsBundle.getString("gps")).append(CharacterConstants.SPACE)
          .append(messageBundle.getString("errors.small")).append(CharacterConstants.COMMA)
          .append(messageBundle.getString("tags")).append(CharacterConstants.COMMA)
          .append(messageBundle.getString("currency")).append(CharacterConstants.COMMA)
          .append(messageBundle.getString("tax")).append(CharacterConstants.SPACE)
          .append(CharacterConstants.O_BRACKET)
          .append(CharacterConstants.PERCENT).append(CharacterConstants.C_BRACKET)
          .append(CharacterConstants.COMMA)
          .append(messageBundle.getString("tax.id")).append(CharacterConstants.COMMA)
          .append(jsBundle.getString("optimization")).append(CharacterConstants.SPACE)
          .append(messageBundle.getString("on")).append(CharacterConstants.COMMA)
          .append(messageBundle.getString("inventory.policy")).append(CharacterConstants.COMMA)
          .append(messageBundle.getString("inventory.servicelevel"))
          .append(CharacterConstants.SPACE).append(CharacterConstants.O_BRACKET)
          .append(CharacterConstants.PERCENT).append(CharacterConstants.C_BRACKET)
          .append(CharacterConstants.COMMA)
          .append(messageBundle.getString("disable.batch")).append(CharacterConstants.COMMA)
          .append(jsBundle.getString("createdby")).append(CharacterConstants.SPACE)
          .append(jsBundle.getString("id")).append(CharacterConstants.COMMA)
          .append(jsBundle.getString("createdby")).append(CharacterConstants.SPACE)
          .append(jsBundle.getString("customid.lower")).append(CharacterConstants.COMMA)
          .append(jsBundle.getString("createdby")).append(CharacterConstants.SPACE)
          .append(jsBundle.getString("fullname.lower")).append(CharacterConstants.COMMA)
          .append(messageBundle.getString("createdon")).append(CharacterConstants.COMMA)
          .append(messageBundle.getString("updatedby")).append(CharacterConstants.SPACE)
          .append(jsBundle.getString("id")).append(CharacterConstants.COMMA)
          .append(messageBundle.getString("updatedby")).append(CharacterConstants.SPACE)
          .append(jsBundle.getString("customid.lower")).append(CharacterConstants.COMMA)
          .append(messageBundle.getString("updatedby")).append(CharacterConstants.SPACE)
          .append(jsBundle.getString("fullname.lower")).append(CharacterConstants.COMMA)
          .append(messageBundle.getString("updatedon"));
    }
    return header.toString();
  }

  // Get the CSV of this kiosk
  @Override
  public String toCSV(Locale locale, String timezone, DomainConfig dc, String type) {
    if ("assets".equalsIgnoreCase(type)) {
      StringBuilder sb = new StringBuilder();
      try {
        AssetSystemConfig asc = AssetSystemConfig.getInstance();
        if (asc == null) {
          throw new ConfigurationException();
        }

        AssetManagementService
            ams =
            Services.getService(AssetManagementServiceImpl.class, locale);
        StringBuilder ksb = constructKioskDetails(kiosk);
        List<Integer> monitoredAssets = AssetUtil.getAssetsByType(IAsset.MONITORED_ASSET);

        List<IAsset> assets;
        for (Integer monitoredAssetId : monitoredAssets) {
          assets = ams.getAssetsByKiosk(kiosk.getKioskId(), monitoredAssetId);
          if (assets != null && assets.size() > 0) {
            for (IAsset asset : assets) {
              sb.append(ksb);
              constructAssetDetails(sb, asc, ams, asset);
              sb.append("\n");
            }
          }
        }

        assets = ams.getAssetsByKiosk(kiosk.getKioskId(), IAsset.TEMP_DEVICE);
        if (assets != null && assets.size() > 0) {
          for (IAsset asset : assets) {
            IAssetRelation relation =
                (asset != null ? ams.getAssetRelationByRelatedAsset(asset.getId()) : null);
            if (relation == null) {
              sb.append(ksb);
              constructAssetDetails(sb, asc, ams, asset);
              sb.append("\n");
            }
          }
        } else if (sb.length() == 0) {
          sb.append(ksb);
          sb.append(",,,,,,,,,,,,\n");
        }
        sb.setLength(sb.length() - 1);
      } catch (Exception e) {
        xLogger.severe("Exception while generating asset CSV line", e);
        return null;
      }
      return sb.toString();
    } else {
      String cbFullName = null, ubFullName = null, cbCustomID = null, ubCustomID = null;
      try {
        // Get services
        UsersService as = Services.getService(UsersServiceImpl.class);
        if (kiosk.getRegisteredBy() != null) {
          try {
            IUserAccount cbUser = as.getUserAccount(kiosk.getRegisteredBy());
            cbFullName = cbUser.getFullName();
            cbCustomID = cbUser.getCustomId();
          } catch (Exception e) {
            cbFullName = Constants.UNKNOWN;
          }
        }
        if (kiosk.getUpdatedBy() != null) {
          try {
            IUserAccount ubUser = as.getUserAccount(kiosk.getUpdatedBy());
            ubFullName = ubUser.getFullName();
            ubCustomID = ubUser.getCustomId();
          } catch (Exception e) {
            ubFullName = Constants.UNKNOWN;
          }
        }
      } catch (Exception e) {
        xLogger.warn("{0} when getting CSV for kiosk {1}: {2}", e.getClass().getName(), kiosk.getKioskId(),
            e.getMessage());
      }

      StringBuilder csvSb = new StringBuilder();
      List<String> userIds = getUserIds(kiosk.getKioskId());
      List<String> tgs = kiosk.getTags();
      csvSb.append(kiosk.getKioskId()).append(CharacterConstants.COMMA)
          .append(kiosk.getCustomId() != null ? StringEscapeUtils.escapeCsv(kiosk.getCustomId()) : CharacterConstants.EMPTY)
          .append(CharacterConstants.COMMA)
          .append(StringEscapeUtils.escapeCsv(kiosk.getName())).append(CharacterConstants.COMMA)
          .append((userIds != null && !userIds.isEmpty() ? StringUtil
              .getCSV(userIds, CharacterConstants.SEMICOLON) : CharacterConstants.EMPTY))
          .append(CharacterConstants.COMMA)
          .append(kiosk.getCountry() != null ? StringEscapeUtils.escapeCsv(kiosk.getCountry()) : CharacterConstants.EMPTY)
          .append(CharacterConstants.COMMA)
          .append(kiosk.getState() != null ? StringEscapeUtils.escapeCsv(kiosk.getState()) : CharacterConstants.EMPTY)
          .append(CharacterConstants.COMMA)
          .append(
              kiosk.getDistrict() != null ? StringEscapeUtils.escapeCsv(kiosk.getDistrict()) : CharacterConstants.EMPTY)
          .append(CharacterConstants.COMMA)
          .append(kiosk.getTaluk() != null ? StringEscapeUtils.escapeCsv(kiosk.getTaluk()) : CharacterConstants.EMPTY)
          .append(CharacterConstants.COMMA)
          .append(kiosk.getCity() != null ? StringEscapeUtils.escapeCsv(kiosk.getCity()) : CharacterConstants.EMPTY)
          .append(CharacterConstants.COMMA)
          .append(kiosk.getStreet() != null ? StringEscapeUtils.escapeCsv(kiosk.getStreet()) : CharacterConstants.EMPTY)
          .append(CharacterConstants.COMMA)
          .append(kiosk.getPinCode() != null ? StringEscapeUtils.escapeCsv(kiosk.getPinCode()) : CharacterConstants.EMPTY)
          .append(CharacterConstants.COMMA)
          .append(kiosk.getLatitude()).append(CharacterConstants.COMMA)
          .append(kiosk.getLongitude()).append(CharacterConstants.COMMA)
          .append(kiosk.getGeoAccuracy()).append(CharacterConstants.COMMA)
          .append(kiosk.getGeoError() != null ? StringEscapeUtils.escapeCsv(kiosk.getGeoError()) : CharacterConstants.EMPTY)
          .append(CharacterConstants.COMMA)
          .append(tgs != null && !tgs.isEmpty() ? StringUtil
              .getCSV(tgs, CharacterConstants.SEMICOLON) : CharacterConstants.EMPTY)
          .append(CharacterConstants.COMMA)
          .append(
              kiosk.getCurrency() != null ? StringEscapeUtils.escapeCsv(kiosk.getCurrency()) : CharacterConstants.EMPTY)
          .append(CharacterConstants.COMMA)
          .append(kiosk.getTax() != null ? BigUtil.getFormattedValue(kiosk.getTax()) : CharacterConstants.EMPTY)
          .append(CharacterConstants.COMMA)
          .append(kiosk.getTaxId() != null ? StringEscapeUtils.escapeCsv(kiosk.getTaxId()) : CharacterConstants.EMPTY)
          .append(CharacterConstants.COMMA)
          .append(kiosk.isOptimizationOn() ? Constants.YES : Constants.NO).append(CharacterConstants.COMMA)
          .append(kiosk.getInventoryModel() != null ? kiosk.getInventoryModel() : CharacterConstants.EMPTY)
          .append(CharacterConstants.COMMA)
          .append(kiosk.getServiceLevel()).append(CharacterConstants.COMMA)
          .append(kiosk.isBatchMgmtEnabled() ? Constants.NO : Constants.YES).append(
          CharacterConstants.COMMA)
          .append(kiosk.getRegisteredBy()).append(CharacterConstants.COMMA)
          .append(cbCustomID != null ? StringEscapeUtils.escapeCsv(cbCustomID)
              : CharacterConstants.EMPTY).append(CharacterConstants.COMMA)
          .append(cbFullName != null ? StringEscapeUtils.escapeCsv(cbFullName)
              : CharacterConstants.EMPTY).append(CharacterConstants.COMMA)
          .append(LocalDateUtil.formatCustom(kiosk.getTimeStamp(), Constants.DATETIME_CSV_FORMAT, timezone))
          .append(CharacterConstants.COMMA)
          .append(kiosk.getUpdatedBy() != null ? StringEscapeUtils.escapeCsv(kiosk.getUpdatedBy()) : CharacterConstants.EMPTY)
          .append(CharacterConstants.COMMA)
          .append(ubCustomID != null ? StringEscapeUtils.escapeCsv(ubCustomID)
              : CharacterConstants.EMPTY).append(CharacterConstants.COMMA)
          .append(ubFullName != null ? StringEscapeUtils.escapeCsv(ubFullName)
              : CharacterConstants.EMPTY).append(CharacterConstants.COMMA)
          .append(kiosk.getLastUpdated() != null ? LocalDateUtil
              .formatCustom(kiosk.getLastUpdated(), Constants.DATETIME_CSV_FORMAT, timezone)
              : CharacterConstants.EMPTY);

      return csvSb.toString();
    }
  }

  private StringBuilder constructKioskDetails(IKiosk kiosk) {
    StringBuilder ksb = new StringBuilder();
    if (kiosk != null) {
      ksb.append(kiosk.getKioskId()).append(CharacterConstants.COMMA)
          .append(kiosk.getCustomId() != null ? StringEscapeUtils.escapeCsv(kiosk.getCustomId())
              : CharacterConstants.EMPTY).append(CharacterConstants.COMMA)
          .append(StringEscapeUtils.escapeCsv(kiosk.getName())).append(CharacterConstants.COMMA)
          .append(kiosk.getCountry() != null ? StringEscapeUtils.escapeCsv(kiosk.getCountry())
              : CharacterConstants.EMPTY).append(CharacterConstants.COMMA)
          .append(kiosk.getState() != null ? StringEscapeUtils.escapeCsv(kiosk.getState())
              : CharacterConstants.EMPTY).append(CharacterConstants.COMMA)
          .append(kiosk.getDistrict() != null ? StringEscapeUtils.escapeCsv(kiosk.getDistrict())
              : CharacterConstants.EMPTY).append(CharacterConstants.COMMA)
          .append(kiosk.getTaluk() != null ? StringEscapeUtils.escapeCsv(kiosk.getTaluk())
              : CharacterConstants.EMPTY).append(CharacterConstants.COMMA)
          .append(kiosk.getCity() != null ? StringEscapeUtils.escapeCsv(kiosk.getCity())
              : CharacterConstants.EMPTY).append(CharacterConstants.COMMA)
          .append(kiosk.getStreet() != null ? StringEscapeUtils.escapeCsv(kiosk.getStreet())
              : CharacterConstants.EMPTY).append(CharacterConstants.COMMA)
          .append(kiosk.getPinCode() != null ? StringEscapeUtils.escapeCsv(kiosk.getPinCode())
              : CharacterConstants.EMPTY).append(CharacterConstants.COMMA)
          .append(kiosk.getLatitude()).append(CharacterConstants.COMMA)
          .append(kiosk.getLongitude()).append(CharacterConstants.COMMA)
          .append(kiosk.getGeoAccuracy()).append(CharacterConstants.COMMA)
          .append(kiosk.getGeoError() != null ? StringEscapeUtils.escapeCsv(kiosk.getGeoError())
              : CharacterConstants.EMPTY).append(CharacterConstants.COMMA);
    } else {
      ksb.append(",,,,,,,,,,,,,,");
    }
    return ksb;
  }

  @SuppressWarnings("unchecked")
  private StringBuilder constructAssetDetails(StringBuilder sb, AssetSystemConfig assetSystemConfig,
                                              AssetManagementService ams, IAsset asset)
      throws ServiceException {
    if (asset != null) {
      if (Objects.equals(asset.getType(), IAsset.TEMP_DEVICE)) {
        sb.append(",,,,,");
        sb.append(constructRelatedAssetDetails(asset));
      } else {
        sb.append(AssetUtil.getAssetName(asset.getType()))
            .append(CharacterConstants.COMMA)
            .append(asset.getSerialId())
            .append(CharacterConstants.COMMA)
            .append(assetSystemConfig.getManufacturerName(asset.getType(), asset.getVendorId()))
            .append(CharacterConstants.COMMA)
            .append(asset.getModel())
            .append(CharacterConstants.COMMA)
            .append((asset.getYom()==null)?"":asset.getYom())
            .append(CharacterConstants.COMMA);

        IAssetRelation relation = ams.getAssetRelationByAsset(asset.getId());
        if (relation != null) {
          IAsset relatedAsset = ams.getAsset(relation.getRelatedAssetId());
          if (relatedAsset != null) {
            sb.append(constructRelatedAssetDetails(relatedAsset));
          } else {
            sb.append(",");
          }
        } else {
          sb.append(",,,,,,,,,");
        }
      }
    } else {
      sb.append(",,,,,,,,,,,,,");
    }

    return sb;
  }

  @SuppressWarnings("unchecked")
  private StringBuilder constructRelatedAssetDetails(IAsset relatedAsset) {
    StringBuilder sb = new StringBuilder();
    sb.append(relatedAsset.getSerialId())
        .append(CharacterConstants.COMMA);

    AssetModel assetModel = AssetUtil.getAssetModel(relatedAsset.getVendorId(), relatedAsset.getSerialId());
    if (assetModel != null && assetModel.meta != null) {
      Map<String, String> metadata = AssetUtil.constructDeviceMetaDataFromJSON(null,
              new Gson().fromJson(assetModel.meta, LinkedHashMap.class));

      for (String gsmKey : AssetUtil.GSM_GROUP) {
        if (metadata.containsKey(gsmKey)) {
          sb.append(metadata.get(gsmKey));
        }
        sb.append(CharacterConstants.COMMA);
      }

      if (metadata.containsKey(AssetUtil.DEV_IMEI)) {
        sb.append(metadata.get(AssetUtil.DEV_IMEI));
      }
      sb.append(CharacterConstants.COMMA);
      sb.append((relatedAsset.getYom()==null)?"":relatedAsset.getYom());
      sb.append(CharacterConstants.COMMA);
    } else {
      sb.append(",,,,,,,,");
    }

    return sb;
  }

  // Get the user Ids associated with a given kiosk
  @SuppressWarnings("unchecked")
  private static List<String> getUserIds(Long kioskId) {
    List<String> userIds = null;
    PersistenceManager pm = PMF.get().getPersistenceManager();
    try {
      Query
          q =
          pm.newQuery("SELECT userId FROM " + UserToKiosk.class.getName()
              + " WHERE kioskId == kioskIdParam PARAMETERS Long kioskIdParam");
      try {
        userIds = (List<String>) q.execute(kioskId);
        if (userIds != null) {
          userIds.size();
          List<String> respUserIds = new ArrayList<>(userIds.size());
          respUserIds.addAll(userIds);
          return respUserIds;
        }
      } finally {
        q.closeAll();
      }
    } catch (Exception e) {
      xLogger.warn("{0} when getting user IDs for kiosk {1}: {2}", e.getClass().getName(), kioskId,
          e.getMessage());
    } finally {
      pm.close();
    }
    return userIds;
  }
}

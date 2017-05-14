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

import com.logistimo.config.models.DomainConfig;
import com.logistimo.constants.Constants;
import com.logistimo.entities.entity.IKiosk;
import com.logistimo.entities.service.EntitiesService;
import com.logistimo.entities.service.EntitiesServiceImpl;
import com.logistimo.inventory.entity.IInvntry;
import com.logistimo.inventory.entity.IInvntryBatch;
import com.logistimo.inventory.service.InventoryManagementService;
import com.logistimo.inventory.service.impl.InventoryManagementServiceImpl;
import com.logistimo.logger.XLog;
import com.logistimo.materials.entity.IMaterial;
import com.logistimo.materials.service.MaterialCatalogService;
import com.logistimo.materials.service.impl.MaterialCatalogServiceImpl;
import com.logistimo.services.Resources;
import com.logistimo.services.Services;
import com.logistimo.tags.TagUtil;
import com.logistimo.utils.BigUtil;
import com.logistimo.utils.LocalDateUtil;
import com.logistimo.utils.StringUtil;

import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

/**
 * Created by charan on 10/03/17.
 */
public class InvntryBatchExportHandler implements IExportHandler {

  private static final XLog xLogger = XLog.getLog(InvntryExportHandler.class);
  private IInvntryBatch invntryBatch;

  public InvntryBatchExportHandler(IInvntryBatch invntryBatch){
    this.invntryBatch = invntryBatch;
  }

  @Override
  public String getCSVHeader(Locale locale, DomainConfig dc, String type) {
    xLogger.fine("Entering getCSVHeader. locale: {0}", locale);
    ResourceBundle messages = Resources.get().getBundle("Messages", locale);
    String header = messages.getString("kiosk") + ",";
    header += messages.getString("material") + ",";
    header += messages.getString("batch") + ",";
    header += messages.getString("stockinbatch") + ",";
    header += messages.getString("expiry") + ",";
    header += messages.getString("manufacturer") + ",";
    header += messages.getString("manufactured") + ",";
    header += messages.getString("lastupdated") + ",";
    header += messages.getString("kiosk") + " " + messages.getString("tags") + ",";
    header += messages.getString("material") + " " + messages.getString("tags") + ",";
    header += messages.getString("customid.entity") + ",";
    header += messages.getString("customid.material") + "," +
        messages.getString("country") + "," + messages.getString("state") + "," +
        messages.getString("district") + "," + messages.getString("taluk") + "," +
        messages.getString("zipcode") + "," + messages.getString("village");
    xLogger.fine("Exiting getCSVHeader. locale: {0}", locale);
    return header;
  }

  @Override
  public String toCSV(Locale locale, String timezone, DomainConfig dc, String type) {
    xLogger.fine("Entering toCSV. locale: {0}, timezone: {1}", locale, timezone);
    try {
      // Get Services
      EntitiesService as = Services.getService(EntitiesServiceImpl.class);
      MaterialCatalogService mcs = Services.getService(MaterialCatalogServiceImpl.class);
      InventoryManagementService
          ims =
          Services.getService(InventoryManagementServiceImpl.class);
      String csv = null;
      List<String> ktgs = invntryBatch.getTags(TagUtil.TYPE_ENTITY);
      List<String> mtgs = invntryBatch.getTags(TagUtil.TYPE_MATERIAL);
      IKiosk k = as.getKiosk(invntryBatch.getKioskId(), false);
      IMaterial m = mcs.getMaterial(invntryBatch.getMaterialId());
      if (k != null && m != null && k.isBatchMgmtEnabled() && m.isBatchEnabled()) {
        IInvntry i = ims.getInventory(invntryBatch.getKioskId(), invntryBatch.getMaterialId());
        if (i != null) {
          csv = "\"" + k.getName() + "\",\"" + m.getName() + "\"," +
              "\"" + invntryBatch.getBatchId() + "\"," +
              BigUtil.getFormattedValue(invntryBatch.getQuantity()) + "," +
              LocalDateUtil.formatCustom(invntryBatch.getBatchExpiry(), Constants.DATETIME_CSV_FORMAT, null) + "," +
              "\"" + invntryBatch.getBatchManufacturer() + "\"" + "," +
              (invntryBatch.getBatchManufacturedDate() != null ? LocalDateUtil
                  .formatCustom(invntryBatch.getBatchManufacturedDate(), Constants.DATETIME_CSV_FORMAT, null) : "") + "," +
              LocalDateUtil.formatCustom(invntryBatch.getTimestamp(), Constants.DATETIME_CSV_FORMAT, timezone) + "," +
              (ktgs != null && !ktgs.isEmpty() ? "\"" + StringUtil.getCSV(ktgs) + "\"" : "")
              + "," +
              (mtgs != null && !mtgs.isEmpty() ? "\"" + StringUtil.getCSV(mtgs) + "\"" : "")
              + "," +
              (k.getCustomId() != null ? k.getCustomId() : "") + "," +
              (m.getCustomId() != null ? m.getCustomId() : "");
          csv += ",\"" + k.getCountry() + "\",\"" + k.getState() + "\"," +
              (k.getDistrict() != null ? "\"" + k.getDistrict() + "\"" : "") + "," +
              (k.getTaluk() != null ? "\"" + k.getTaluk() + "\"" : "") + "," +
              (k.getPinCode() != null ? "\"" + k.getPinCode() + "\"" : "") + "," +
              "\"" + k.getCity() + "\"";
        }
      }
      return csv;
    } catch (Exception e) {
      xLogger.warn("{0} when getting CSV for inventory batch {1}: {2}", e.getClass().getName(), invntryBatch.getKeyString(),
          e.getMessage());
      return null;
    }

  }
}

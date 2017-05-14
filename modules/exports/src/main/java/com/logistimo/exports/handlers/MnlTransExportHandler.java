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
import com.logistimo.logger.XLog;
import com.logistimo.materials.entity.IMaterial;
import com.logistimo.materials.service.MaterialCatalogService;
import com.logistimo.materials.service.impl.MaterialCatalogServiceImpl;
import com.logistimo.mnltransactions.entity.IMnlTransaction;
import com.logistimo.services.Resources;
import com.logistimo.services.Services;
import com.logistimo.utils.LocalDateUtil;
import com.logistimo.utils.StringUtil;

import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

/**
 * Created by charan on 12/03/17.
 */
public class MnlTransExportHandler implements IExportHandler {

  private final IMnlTransaction mnlTransaction;

  private static final XLog xLogger = XLog.getLog(MnlTransExportHandler.class);


  public MnlTransExportHandler(IMnlTransaction mnlTransaction){
    this.mnlTransaction = mnlTransaction;
  }
  @Override
  public String getCSVHeader(Locale locale, DomainConfig dc, String type) {
    ResourceBundle messages = Resources.get().getBundle("Messages", locale);
    // Note: The following code block is the same as the code in getUploadableCSVHeader except that "closingstock" is also included here.
    String
        header =
        messages.getString("kiosk") + "* (" + messages.getString("kiosk") + " name)" + "," +
            messages.getString("material") + "* (Material name)" + "," +
            messages.getString("openingstock") + "* (Opening stock)" + "," +
            messages.getString("reportingperiod") + " (yyyy-MM-dd)," +
            messages.getString("receipts") + "," +
            messages.getString("issues") + "," +
            messages.getString("transactions.wastage.upper") + "," +
            messages.getString("closingstock") + "," +
            messages.getString("stockout") + " " + messages.getString("duration") + "," +
            //messages.getString( "numberofstockouts" ) +"," +
            messages.getString("manual") + " " + messages.getString("consumption") + "," +
            messages.getString("computed") + " " + messages.getString("consumption") + "," +
            messages.getString("ordered") + " " + messages.getString("quantity") + "," +
            messages.getString("fulfilled") + " " + messages.getString("quantity") + "," +
            messages.getString("order") + " " + messages.getString("tags") + " "
            + "(semi-colon separated list of tags)" + "," +
            messages.getString("vendor") + " (" + messages.getString("vendor") + " name)";

    // Add entity and material tags
    header += "," + messages.getString("tagentity") + "," + messages.getString("tagmaterial");
    // Add location metadata
    header += "," + messages.getString("country") + "," + messages.getString("state") + "," +
        messages.getString("district") + "," + messages.getString("taluk") + "," +
        messages.getString("zipcode") + "," + messages.getString("village");
    // Add upload time
    header += "," + messages.getString("manualtransactions.uploadtime");
    return header;
  }

  @Override
  public String toCSV(Locale locale, String timezone, DomainConfig dc, String type) {
    try {
      EntitiesService as = Services.getService(EntitiesServiceImpl.class);
      MaterialCatalogService mcs = Services.getService(MaterialCatalogServiceImpl.class);
      IKiosk k = as.getKiosk(mnlTransaction.getKioskId(), false);
      IMaterial m = mcs.getMaterial(mnlTransaction.getMaterialId());
      String csv = "\"" + k.getName() + "\",\"" + m.getName() + "\"," +
          mnlTransaction.getOpeningStock() + "," +
          (mnlTransaction.getReportingPeriod() != null ?
              LocalDateUtil.formatCustom(mnlTransaction.getReportingPeriod(),
                  Constants.DATE_FORMAT_CSV, null) : "") + ","
          +
          mnlTransaction.getReceiptQuantity() + "," +
          mnlTransaction.getIssueQuantity() + "," +
          mnlTransaction.getDiscardQuantity() + "," +
          mnlTransaction.getClosingStock() + "," +
          mnlTransaction.getStockoutDuration() + "," +
          mnlTransaction.getManualConsumptionRate() + "," +
          mnlTransaction.getComputedConsumptionRate() + "," +
          mnlTransaction.getOrderedQuantity() + "," +
          mnlTransaction.getFulfilledQuantity() + "," +
          (mnlTransaction.getTags() != null && !mnlTransaction.getTags().isEmpty() ? "\"" +
              StringUtil.getCSV(mnlTransaction.getTags()) + "\"" : "") + "," +
          (mnlTransaction.getVendorId() != null ? "\"" + as.getKiosk(mnlTransaction.getVendorId(),
              false).getName() + "\"" : "");
      // Add entity/material tags
      List<String> ktag = k.getTags();
      List<String> mtag = m.getTags();
      csv += "," + (ktag != null && !ktag.isEmpty() ? "\"" + StringUtil.getCSV(ktag) + "\"" : "") +
          "," + (mtag != null && !mtag.isEmpty() ? "\"" + StringUtil.getCSV(mtag) + "\"" : "");
      // Add location metadata
      csv += ",\"" + k.getCountry() + "\",\"" + k.getState() + "\"," +
          (k.getDistrict() != null ? "\"" + k.getDistrict() + "\"" : "") + "," +
          (k.getTaluk() != null ? "\"" + k.getTaluk() + "\"" : "") + "," +
          (k.getPinCode() != null ? "\"" + k.getPinCode() + "\"" : "") + "," +
          "\"" + k.getCity() + "\"";
      // Add upload time
      csv +=
          "," + (mnlTransaction.getTimestamp() != null ?
              LocalDateUtil.formatCustom(mnlTransaction.getTimestamp(), Constants.DATETIME_CSV_FORMAT, timezone)
              : "");
      return csv;
    } catch (Exception e) {
      xLogger.warn("{0} in toCSV on MnlTransaction {1}: {2}", e.getClass().getName(),
          mnlTransaction.getKeyString(),
          e.getMessage());
      return null;
    }
  }
}

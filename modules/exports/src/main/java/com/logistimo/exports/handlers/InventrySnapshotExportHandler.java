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
import com.logistimo.entities.entity.IKiosk;
import com.logistimo.entities.service.EntitiesService;
import com.logistimo.entities.service.EntitiesServiceImpl;
import com.logistimo.inventory.models.InvntrySnapshot;
import com.logistimo.materials.entity.IMaterial;
import com.logistimo.materials.service.MaterialCatalogService;
import com.logistimo.materials.service.impl.MaterialCatalogServiceImpl;

import org.apache.commons.lang.StringEscapeUtils;
import com.logistimo.services.Resources;
import com.logistimo.services.ServiceException;
import com.logistimo.services.Services;
import com.logistimo.utils.BigUtil;
import com.logistimo.constants.CharacterConstants;
import com.logistimo.constants.Constants;
import com.logistimo.utils.LocalDateUtil;
import com.logistimo.utils.NumberUtil;
import com.logistimo.utils.StringUtil;
import com.logistimo.logger.XLog;

import java.util.Locale;
import java.util.ResourceBundle;

/**
 * Created by yuvaraj on 09/03/17.
 */
public class InventrySnapshotExportHandler implements IExportHandler {

  private static final XLog xLogger = XLog.getLog(InvntryExportHandler.class);
  private final InvntrySnapshot invSnapshot;

  public InventrySnapshotExportHandler(InvntrySnapshot invSnapshot){
    this.invSnapshot = invSnapshot;
  }
  
  @Override
  public String getCSVHeader(Locale locale, DomainConfig dc, String type) {
    xLogger.fine("Entering getCSVHeader. locale: {0}", locale);
    ResourceBundle messages = Resources.get().getBundle("Messages", locale);
    ResourceBundle jsMessages = Resources.get().getBundle("JSMessages", locale);
    String
        header =
        messages.getString("kiosk") + CharacterConstants.SPACE + jsMessages.getString("id")
            + CharacterConstants.COMMA +
            messages.getString("customid.entity") + CharacterConstants.COMMA +
            messages.getString("kiosk.name") + CharacterConstants.COMMA +
            messages.getString("material") + CharacterConstants.SPACE + jsMessages.getString("id")
            + CharacterConstants.COMMA +
            messages.getString("customid.material") + CharacterConstants.COMMA +
            messages.getString("material.name") + CharacterConstants.COMMA +
            messages.getString("inventory.currentstock") + "," + messages.getString("lastupdated")
            + "," +
            messages.getString("inventory.reorderlevel") + "," +
            messages.getString("max") + "," +
            messages.getString("config.consumptionrates") + "/" + messages.getString("day") + "," +
            messages.getString("config.consumptionrates") + "/" + messages.getString("week") + "," +
            messages.getString("config.consumptionrates") + "/" + messages.getString("month") + ","
            +
            messages.getString("demandforecast") + "," +
            messages.getString("inventory.safetystock") + " (computed)" + "," +
            messages.getString("order.optimalorderquantity") + "," +
            messages.getString("inventory.policy") + "," + messages
            .getString("inventory.servicelevel") + "," +
            messages.getString("kiosk") + CharacterConstants.SPACE +
            jsMessages.getString("tags.lower") + CharacterConstants.COMMA +
            messages.getString("material") + CharacterConstants.SPACE +
            jsMessages.getString("tags.lower") + CharacterConstants.COMMA +
            messages.getString("country") + "," + messages.getString("state") + "," +
            messages.getString("district") + "," + messages.getString("taluk") + "," +
            messages.getString("zipcode") + "," + messages.getString("village");
    xLogger.fine("Exiting getCSVHeader");
    return header;
  }

  @Override
  public String toCSV(Locale locale, String timezone, DomainConfig dc, String type) {
    xLogger.fine("Entering toCSV. locale: {0}, timezone: {1}", locale, timezone);
    try {
      // Get services

      EntitiesService es = null;
      IKiosk k = null;
      IMaterial m = null;
      try {
        es = Services.getService(EntitiesServiceImpl.class);
        k = es.getKiosk(invSnapshot.getkId());
        MaterialCatalogService mcs = Services.getService(MaterialCatalogServiceImpl.class);
        m = mcs.getMaterial(invSnapshot.getmId());
        invSnapshot.setKnm(k.getName());
      } catch (ServiceException e) {
        e.printStackTrace();
      }

      String csv = invSnapshot.getkId() + CharacterConstants.COMMA +
          (k.getCustomId() != null ? StringEscapeUtils.escapeCsv(k.getCustomId())
              : CharacterConstants.EMPTY) + CharacterConstants.COMMA +
          StringEscapeUtils.escapeCsv(k.getName()) + CharacterConstants.COMMA +
          invSnapshot.getmId() + CharacterConstants.COMMA +
          (m.getCustomId() != null ? StringEscapeUtils.escapeCsv(m.getCustomId())
              : CharacterConstants.EMPTY) + CharacterConstants.COMMA +
          StringEscapeUtils.escapeCsv(m.getName()) + CharacterConstants.COMMA +
          BigUtil.getFormattedValue(invSnapshot.getStock()) + "," + LocalDateUtil
          .formatCustom(invSnapshot.getT(), Constants.DATETIME_CSV_FORMAT, timezone) + "," +
          BigUtil.getFormattedValue(invSnapshot.getReorderLevel()) + "," +
          BigUtil.getFormattedValue(invSnapshot.getMaxStock()) + "," +
          BigUtil.getFormattedValue(invSnapshot.getConsumptionRateDaily()) + "," + BigUtil
          .getFormattedValue(invSnapshot.getConsumptionRateWeekly()) + "," + BigUtil
          .getFormattedValue(invSnapshot.getConsumptionRateMonthly()) + "," +
          BigUtil.getFormattedValue(invSnapshot.getRevPeriodDemand()) + "," + BigUtil
          .getFormattedValue(invSnapshot.getSafetyStock()) + "," +
          BigUtil.getFormattedValue(invSnapshot.getEconomicOrderQuantity()) + "," +
          (invSnapshot.getImdl() != null ? invSnapshot.getImdl() : "") + "," + NumberUtil.getFormattedValue(invSnapshot.getServiceLevel()) + "," +
          (invSnapshot.getKtgs() != null && !invSnapshot.getKtgs().isEmpty() ? StringEscapeUtils
              .escapeCsv(StringUtil.getCSV(invSnapshot.getKtgs(), CharacterConstants.SEMICOLON))
              : CharacterConstants.EMPTY) + CharacterConstants.COMMA +
          (invSnapshot.getTgs() != null && !invSnapshot.getTgs().isEmpty() ? StringEscapeUtils
              .escapeCsv(StringUtil.getCSV(invSnapshot.getTgs(), CharacterConstants.SEMICOLON))
              : CharacterConstants.EMPTY);

      csv += ",\"" + k.getCountry() + "\",\"" + k.getState() + "\"," +
          (k.getDistrict() != null ? "\"" + k.getDistrict() + "\"" : "") + "," +
          (k.getTaluk() != null ? "\"" + k.getTaluk() + "\"" : "") + "," +
          (k.getPinCode() != null ? "\"" + k.getPinCode() + "\"" : "") + "," +
          "\"" + k.getCity() + "\"";
      return csv;
    } catch (Exception e) {
      xLogger.warn(
          "{0} when getting CSV for inventory snap shot with kiosk id {1} and material id {2}: {3}",
          e.getClass().getName(), invSnapshot.getkId(), invSnapshot.getmId(), e.getMessage());
      return null;
    }
  }
}

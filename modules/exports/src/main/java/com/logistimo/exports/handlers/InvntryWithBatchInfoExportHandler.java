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
import com.logistimo.config.models.InventoryConfig;
import com.logistimo.config.utils.DomainConfigUtil;
import com.logistimo.constants.CharacterConstants;
import com.logistimo.constants.Constants;
import com.logistimo.entities.entity.IKiosk;
import com.logistimo.entities.service.EntitiesService;
import com.logistimo.entities.service.EntitiesServiceImpl;
import com.logistimo.events.entity.Event;
import com.logistimo.events.entity.IEvent;
import com.logistimo.inventory.models.InvntryWithBatchInfo;
import com.logistimo.logger.XLog;
import com.logistimo.materials.entity.IMaterial;
import com.logistimo.materials.service.MaterialCatalogService;
import com.logistimo.materials.service.impl.MaterialCatalogServiceImpl;
import com.logistimo.services.Resources;
import com.logistimo.services.Services;
import com.logistimo.tags.TagUtil;
import com.logistimo.users.entity.IUserAccount;
import com.logistimo.users.service.UsersService;
import com.logistimo.users.service.impl.UsersServiceImpl;
import com.logistimo.utils.BigUtil;
import com.logistimo.utils.LocalDateUtil;
import com.logistimo.utils.NumberUtil;
import com.logistimo.utils.StringUtil;
import org.apache.commons.lang.StringEscapeUtils;

import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

/**
 * Created by charan on 06/03/17.
 */
public class InvntryWithBatchInfoExportHandler implements IExportHandler {

  static XLog xLogger = XLog.getLog(InvntryWithBatchInfoExportHandler.class);


  public InvntryWithBatchInfo invntryWithBatchInfo;

  public InvntryWithBatchInfoExportHandler(InvntryWithBatchInfo invntryWithBatchInfo){
    this.invntryWithBatchInfo = invntryWithBatchInfo;
  }

  @Override
  public String getCSVHeader(Locale locale, DomainConfig dc, String type) {
    ResourceBundle messages = Resources.get().getBundle("Messages", locale);
    ResourceBundle jsMessages = Resources.get().getBundle("JSMessages", locale);
    StringBuilder header = new StringBuilder();
    String mcrUnits = getMCRUnits(locale);
    String mmd = DomainConfigUtil.getMinMaxDuration(dc);
    header.append(messages.getString("kiosk")).append(CharacterConstants.SPACE)
        .append(jsMessages.getString("id")).append(CharacterConstants.COMMA)
        .append(messages.getString("customid.entity")).append(CharacterConstants.COMMA)
        .append(messages.getString("kiosk.name")).append(CharacterConstants.COMMA)
        .append(messages.getString("material")).append(CharacterConstants.SPACE)
        .append(jsMessages.getString("id")).append(CharacterConstants.COMMA)
        .append(messages.getString("customid.material")).append(CharacterConstants.COMMA)
        .append(messages.getString("material.name")).append(CharacterConstants.COMMA)
        .append(messages.getString("country")).append(CharacterConstants.COMMA)
        .append(messages.getString("state")).append(CharacterConstants.COMMA)
        .append(messages.getString("district")).append(CharacterConstants.COMMA)
        .append(messages.getString("taluk")).append(CharacterConstants.COMMA)
        .append(messages.getString("village")).append(CharacterConstants.COMMA)
        .append(messages.getString("streetaddress")).append(CharacterConstants.COMMA)
        .append(messages.getString("zipcode")).append(CharacterConstants.COMMA)
        .append(messages.getString("latitude")).append(CharacterConstants.COMMA)
        .append(messages.getString("longitude")).append(CharacterConstants.COMMA)
        .append(messages.getString("accuracy")).append(CharacterConstants.SPACE)
        .append(CharacterConstants.O_BRACKET).append(messages.getString("meters"))
        .append(CharacterConstants.C_BRACKET).append(CharacterConstants.COMMA)
        .append(jsMessages.getString("gps")).append(CharacterConstants.SPACE)
        .append(messages.getString("errors.small")).append(CharacterConstants.COMMA)
        .append(messages.getString("kiosk")).append(CharacterConstants.SPACE)
        .append(jsMessages.getString("tags.lower")).append(CharacterConstants.COMMA)
        .append(messages.getString("material")).append(CharacterConstants.SPACE)
        .append(jsMessages.getString("tags.lower")).append(CharacterConstants.COMMA)
        .append(messages.getString("material.stockonhand")).append(CharacterConstants.COMMA)
        .append(messages.getString("inventory.reorderlevel")).append(CharacterConstants.COMMA)
        .append(messages.getString("max")).append(CharacterConstants.COMMA);
    if (!mmd.isEmpty()) {
      header.append(messages.getString("inventory.reorderlevel")).append(mmd)
          .append(CharacterConstants.COMMA)
          .append(messages.getString("max")).append(mmd).append(CharacterConstants.COMMA);
    }
    header.append(jsMessages.getString("abnormality.type")).append(CharacterConstants.COMMA)
        .append(jsMessages.getString("abnormality.duration") + "-" + messages.getString("days")).append(CharacterConstants.COMMA)
        .append(messages.getString("material.retailerprice")).append(CharacterConstants.COMMA)
        .append(messages.getString("inventory.policy")).append(CharacterConstants.COMMA)
        .append(messages.getString("inventory.servicelevel")).append(CharacterConstants.COMMA)
        .append(messages.getString("batchid")).append(CharacterConstants.COMMA)
        .append(messages.getString("stockinbatch")).append(CharacterConstants.COMMA)
        .append(messages.getString("batch")).append(CharacterConstants.SPACE)
        .append(jsMessages.getString("expiry.lower")).append(CharacterConstants.COMMA)
        .append(messages.getString("batch")).append(CharacterConstants.SPACE)
        .append(jsMessages.getString("manufacturer.lower")).append(CharacterConstants.COMMA)
        .append(messages.getString("batch")).append(CharacterConstants.SPACE)
        .append(jsMessages.getString("manufactured.lower")).append(CharacterConstants.COMMA)
        .append(messages.getString("batch")).append(CharacterConstants.SPACE)
        .append(jsMessages.getString("updatedon.lower")).append(CharacterConstants.COMMA)
            .append(mcrUnits).append(CharacterConstants.COMMA)
        .append(messages.getString("config.consumptionrates")).append(CharacterConstants.F_SLASH)
        .append(messages.getString("day")).append(CharacterConstants.COMMA)
        .append(messages.getString("config.consumptionrates")).append(CharacterConstants.F_SLASH)
        .append(messages.getString("week")).append(CharacterConstants.COMMA)
        .append(messages.getString("config.consumptionrates")).append(CharacterConstants.F_SLASH)
        .append(messages.getString("month")).append(CharacterConstants.COMMA)
        .append(messages.getString("demandforecast")).append(CharacterConstants.COMMA)
        .append(messages.getString("inventory.safetystock")).append(CharacterConstants.SPACE)
        .append(CharacterConstants.O_BRACKET).append(jsMessages.getString("computed.lower"))
        .append(CharacterConstants.C_BRACKET).append(CharacterConstants.COMMA)
        .append(messages.getString("order.optimalorderquantity")).append(CharacterConstants.COMMA)
        .append(messages.getString("order.leadtimedemand")).append(CharacterConstants.COMMA)
        .append(messages.getString("order.leadtime")).append(CharacterConstants.SPACE)
        .append(messages.getString("days")).append(CharacterConstants.COMMA)
        .append(messages.getString("order.periodicity")).append(CharacterConstants.SPACE)
        .append(messages.getString("days")).append(CharacterConstants.COMMA)
        .append(messages.getString("order.stddevleadtime")).append(CharacterConstants.COMMA)
        .append(messages.getString("createdon")).append(CharacterConstants.COMMA)
        .append(messages.getString("updatedby")).append(CharacterConstants.SPACE)
        .append(jsMessages.getString("id")).append(CharacterConstants.COMMA)
        .append(messages.getString("updatedby")).append(CharacterConstants.SPACE)
        .append(jsMessages.getString("customid.lower")).append(CharacterConstants.COMMA)
        .append(messages.getString("updatedby")).append(CharacterConstants.SPACE)
        .append(jsMessages.getString("fullname.lower")).append(CharacterConstants.COMMA)
        .append(messages.getString("updatedon")).append(CharacterConstants.COMMA)
        .append(messages.getString("min")).append(CharacterConstants.SPACE)
        .append(jsMessages.getString("updatedon.lower")).append(CharacterConstants.COMMA)
        .append(messages.getString("max")).append(CharacterConstants.SPACE)
        .append(jsMessages.getString("updatedon.lower")).append(CharacterConstants.COMMA)
        .append(messages.getString("material.retailerprice")).append(CharacterConstants.SPACE)
        .append(jsMessages.getString("updatedon.lower")).append(CharacterConstants.COMMA)
        .append(jsMessages.getString("pands")).append(CharacterConstants.SPACE)
        .append(jsMessages.getString("updatedon.lower")).append(CharacterConstants.COMMA)
        .append(jsMessages.getString("dandq")).append(CharacterConstants.SPACE)
        .append(jsMessages.getString("updatedon.lower"));

    return header.toString();
  }

  @Override
  public String toCSV(Locale locale, String timezone, DomainConfig dc, String type) {
    try {
      // Get services
      EntitiesService as = Services.getService(EntitiesServiceImpl.class);
      MaterialCatalogService mcs = Services.getService(MaterialCatalogServiceImpl.class);
      UsersService us = Services.getService(UsersServiceImpl.class);
      IKiosk k = as.getKiosk(invntryWithBatchInfo.getKioskId(), false);
      IMaterial m = mcs.getMaterial(invntryWithBatchInfo.getMaterialId());

      String ubFullName = null, ubCustomId = null;
      if (invntryWithBatchInfo.getUpdatedBy() != null) {
        try {

          IUserAccount ubUser = us.getUserAccount(invntryWithBatchInfo.getUpdatedBy());
          ubFullName = ubUser.getFullName();
          ubCustomId = ubUser.getCustomId();
        } catch (Exception e) {
          ubFullName = Constants.UNKNOWN;
        }
      }
      List<String> ktgs = invntryWithBatchInfo.getTags(TagUtil.TYPE_ENTITY);
      List<String> tgs = invntryWithBatchInfo.getTags(TagUtil.TYPE_MATERIAL);
      StringBuilder csv = new StringBuilder();
      csv.append(invntryWithBatchInfo.getKioskId()).append(CharacterConstants.COMMA)
          .append(k.getCustomId() != null ? StringEscapeUtils.escapeCsv(k.getCustomId())
                  : CharacterConstants.EMPTY).append(CharacterConstants.COMMA)
          .append(StringEscapeUtils.escapeCsv(k.getName())).append(CharacterConstants.COMMA)
          .append(invntryWithBatchInfo.getMaterialId()).append(CharacterConstants.COMMA)
          .append(m.getCustomId() != null ? StringEscapeUtils.escapeCsv(m.getCustomId())
                  : CharacterConstants.EMPTY).append(CharacterConstants.COMMA)
          .append(StringEscapeUtils.escapeCsv(m.getName())).append(CharacterConstants.COMMA)
          .append(k.getCountry() != null ? StringEscapeUtils.escapeCsv(k.getCountry())
                  : CharacterConstants.EMPTY).append(CharacterConstants.COMMA)
          .append(k.getState() != null ? StringEscapeUtils.escapeCsv(k.getState())
                  : CharacterConstants.EMPTY).append(CharacterConstants.COMMA)
          .append(k.getDistrict() != null ? StringEscapeUtils.escapeCsv(k.getDistrict())
                  : CharacterConstants.EMPTY).append(CharacterConstants.COMMA)
          .append(k.getTaluk() != null ? StringEscapeUtils.escapeCsv(k.getTaluk())
                  : CharacterConstants.EMPTY).append(CharacterConstants.COMMA)
          .append(k.getCity() != null ? StringEscapeUtils.escapeCsv(k.getCity())
                  : CharacterConstants.EMPTY).append(CharacterConstants.COMMA)
          .append(k.getStreet() != null ? StringEscapeUtils.escapeCsv(k.getStreet())
                  : CharacterConstants.EMPTY).append(CharacterConstants.COMMA)
          .append(k.getPinCode() != null ? StringEscapeUtils.escapeCsv(k.getPinCode())
                  : CharacterConstants.EMPTY).append(CharacterConstants.COMMA)
          .append(k.getLatitude()).append(CharacterConstants.COMMA)
          .append(k.getLongitude()).append(CharacterConstants.COMMA)
          .append(k.getGeoAccuracy()).append(CharacterConstants.COMMA)
          .append(k.getGeoError() != null ? StringEscapeUtils.escapeCsv(k.getGeoError())
                  : CharacterConstants.EMPTY).append(CharacterConstants.COMMA)
          .append(ktgs != null && !ktgs.isEmpty() ? StringEscapeUtils
                  .escapeCsv(StringUtil.getCSV(ktgs, CharacterConstants.SEMICOLON))
                  : CharacterConstants.EMPTY).append(CharacterConstants.COMMA)
          .append(tgs != null && !tgs.isEmpty() ? StringEscapeUtils
                  .escapeCsv(StringUtil.getCSV(tgs, CharacterConstants.SEMICOLON))
                  : CharacterConstants.EMPTY).append(CharacterConstants.COMMA)
          .append(BigUtil.getFormattedValue(invntryWithBatchInfo.getStock())).append(CharacterConstants.COMMA)
          .append(BigUtil.getFormattedValue(invntryWithBatchInfo.getReorderLevel())).append(CharacterConstants.COMMA)
          .append(BigUtil.getFormattedValue(invntryWithBatchInfo.getMaxStock())).append(CharacterConstants.COMMA);
      if (!dc.getInventoryConfig().isMinMaxAbsolute()) {
        csv.append(BigUtil.getFormattedValue(invntryWithBatchInfo.getMinDuration())).append(CharacterConstants.COMMA)
            .append(BigUtil.getFormattedValue(invntryWithBatchInfo.getMaxDuration())).append(CharacterConstants.COMMA);
      }
      csv.append(invntryWithBatchInfo.getStockEvent()!= IEvent.NORMAL ?
                Event.getEventName(invntryWithBatchInfo.getStockEvent(),locale) : CharacterConstants.EMPTY).append(CharacterConstants.COMMA)
                .append(invntryWithBatchInfo.getStockEventDuration() != null ? invntryWithBatchInfo.getStockEventDuration()
                        : CharacterConstants.EMPTY).append(CharacterConstants.COMMA)
          .append(BigUtil.getFormattedValue(invntryWithBatchInfo.getRetailerPrice())).append(CharacterConstants.COMMA)
          .append(invntryWithBatchInfo.getInventoryModel() != null ? StringEscapeUtils.escapeCsv(invntryWithBatchInfo.getInventoryModel()) : CharacterConstants.EMPTY)
          .append(CharacterConstants.COMMA)
          .append(NumberUtil.getFormattedValue(invntryWithBatchInfo.getServiceLevel())).append(CharacterConstants.COMMA)
          .append(invntryWithBatchInfo.getBatchId() != null ? StringEscapeUtils.escapeCsv(invntryWithBatchInfo.getBatchId()) : CharacterConstants.EMPTY)
          .append(CharacterConstants.COMMA)
          .append(
              invntryWithBatchInfo.getBatchId() != null ? BigUtil.getFormattedValue(invntryWithBatchInfo.getStockInBatch()) : CharacterConstants.EMPTY)
          .append(CharacterConstants.COMMA)
          .append(
              invntryWithBatchInfo.getBatchExpiry() != null ? LocalDateUtil.formatCustom(invntryWithBatchInfo.getBatchExpiry(), Constants.DATETIME_CSV_FORMAT, null)
                  : CharacterConstants.EMPTY).append(CharacterConstants.COMMA)
          .append(invntryWithBatchInfo.getBatchManufacturer() != null ? StringEscapeUtils.escapeCsv(invntryWithBatchInfo.getBatchManufacturer()) : CharacterConstants.EMPTY)
          .append(CharacterConstants.COMMA)
          .append(
              invntryWithBatchInfo.getBatchManufacturedDate() != null ? LocalDateUtil.formatCustom(invntryWithBatchInfo.getBatchManufacturedDate(), Constants.DATETIME_CSV_FORMAT, null)
                  : CharacterConstants.EMPTY).append(CharacterConstants.COMMA)
          .append(
              invntryWithBatchInfo.getBatchTimestamp() != null ? LocalDateUtil.formatCustom(invntryWithBatchInfo.getBatchTimestamp(), Constants.DATETIME_CSV_FORMAT, timezone)
                  : CharacterConstants.EMPTY).append(CharacterConstants.COMMA)
          .append(BigUtil.getFormattedValue(invntryWithBatchInfo.getStockAvailabilityPeriod()))
          .append(CharacterConstants.COMMA)
          .append(BigUtil.getFormattedValue(invntryWithBatchInfo.getConsumptionRateDaily()))
          .append(CharacterConstants.COMMA)
          .append(BigUtil.getFormattedValue(invntryWithBatchInfo.getConsumptionRateWeekly()))
          .append(CharacterConstants.COMMA)
          .append(BigUtil.getFormattedValue(invntryWithBatchInfo.getConsumptionRateMonthly()))
          .append(CharacterConstants.COMMA)
          .append(BigUtil.getFormattedValue(invntryWithBatchInfo.getRevPeriodDemand())).append(CharacterConstants.COMMA)
          .append(BigUtil.getFormattedValue(invntryWithBatchInfo.getSafetyStock())).append(CharacterConstants.COMMA)
          .append(BigUtil.getFormattedValue(invntryWithBatchInfo.getEconomicOrderQuantity()))
          .append(CharacterConstants.COMMA)
          .append(BigUtil.getFormattedValue(invntryWithBatchInfo.getLeadTimeDemand())).append(CharacterConstants.COMMA)
          .append(BigUtil.getFormattedValue(invntryWithBatchInfo.getLeadTime())).append(CharacterConstants.COMMA)
          .append(BigUtil.getFormattedValue(invntryWithBatchInfo.getOrderPeriodicity())).append(CharacterConstants.COMMA)
          .append(BigUtil.getFormattedValue(invntryWithBatchInfo.getStdevRevPeriodDemand()))
          .append(CharacterConstants.COMMA)
          .append(
              invntryWithBatchInfo.getCreatedOn() != null ? LocalDateUtil.formatCustom(invntryWithBatchInfo.getCreatedOn(), Constants.DATETIME_CSV_FORMAT, timezone)
                  : CharacterConstants.EMPTY).append(CharacterConstants.COMMA)
          .append(invntryWithBatchInfo.getUpdatedBy() != null ? invntryWithBatchInfo.getUpdatedBy() : CharacterConstants.EMPTY).append(CharacterConstants.COMMA)
          .append(ubCustomId != null ? StringEscapeUtils.escapeCsv(ubCustomId)
              : CharacterConstants.EMPTY).append(CharacterConstants.COMMA)
          .append(ubFullName != null ? StringEscapeUtils.escapeCsv(ubFullName)
              : CharacterConstants.EMPTY).append(CharacterConstants.COMMA)
          .append(invntryWithBatchInfo.getTimestamp() != null ? LocalDateUtil.formatCustom(invntryWithBatchInfo.getTimestamp(), Constants.DATETIME_CSV_FORMAT, timezone)
              : CharacterConstants.EMPTY).append(CharacterConstants.COMMA)
          .append(invntryWithBatchInfo.getReorderLevelUpdatedTime() != null ? LocalDateUtil
              .formatCustom(invntryWithBatchInfo.getReorderLevelUpdatedTime(), Constants.DATETIME_CSV_FORMAT, timezone)
              : CharacterConstants.EMPTY).append(CharacterConstants.COMMA)
          .append(invntryWithBatchInfo.getMaxUpdatedTime() != null ? LocalDateUtil
              .formatCustom(invntryWithBatchInfo.getMaxUpdatedTime(), Constants.DATETIME_CSV_FORMAT, timezone)
              : CharacterConstants.EMPTY).append(CharacterConstants.COMMA)
          .append(invntryWithBatchInfo.getRetailerPriceUpdatedTime() != null ? LocalDateUtil
              .formatCustom(invntryWithBatchInfo.getRetailerPriceUpdatedTime(), Constants.DATETIME_CSV_FORMAT, timezone)
              : CharacterConstants.EMPTY).append(CharacterConstants.COMMA)
          .append(
              invntryWithBatchInfo.getPSTimestamp() != null ? LocalDateUtil.formatCustom(invntryWithBatchInfo.getPSTimestamp(), Constants.DATETIME_CSV_FORMAT, timezone)
                  : CharacterConstants.EMPTY).append(CharacterConstants.COMMA)
          .append(
              invntryWithBatchInfo.getDQTimestamp() != null ? LocalDateUtil.formatCustom(invntryWithBatchInfo.getDQTimestamp(), Constants.DATETIME_CSV_FORMAT, timezone)
                  : CharacterConstants.EMPTY);

      return csv.toString();
    } catch (Exception e) {
      xLogger.warn("{0} when getting CSV for inventory with batch: {1}", e.getClass().getName(),
          e.getMessage());
      return null;
    }
  }

  private String getMCRUnits(Locale locale) {
    ResourceBundle messages = Resources.get().getBundle("Messages", locale);
    ResourceBundle jsMessages = Resources.get().getBundle("JSMessages", locale);
    Long domainId = invntryWithBatchInfo.getDomainId();
    DomainConfig dc = DomainConfig.getInstance(domainId);
    InventoryConfig ic = dc.getInventoryConfig();

    boolean allowManualConsumptionRates = (ic != null && ic.getManualCRFreq() != null);
    String
        manualCrUnits =
        (allowManualConsumptionRates ? InventoryConfig
            .getFrequencyDisplay(ic.getManualCRFreq(), false, locale) : null);
    if (manualCrUnits == null || manualCrUnits.isEmpty() || manualCrUnits
        .equalsIgnoreCase(messages.getString("days"))) {
      manualCrUnits =
          jsMessages.getString("daysofstock"); // Default the manual consumption rate units to Days.
    } else if (manualCrUnits.equalsIgnoreCase(messages.getString("weeks"))) {
      manualCrUnits = jsMessages.getString("weeksofstock");
    } else if (manualCrUnits.equalsIgnoreCase(messages.getString("months"))) {
      manualCrUnits = jsMessages.getString("monthsofstock");
    }
    return manualCrUnits;
  }
}

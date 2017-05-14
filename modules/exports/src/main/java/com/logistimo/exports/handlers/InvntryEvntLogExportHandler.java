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
import com.logistimo.events.processor.EventPublisher;
import com.logistimo.inventory.entity.IInvntry;
import com.logistimo.inventory.entity.IInvntryEvntLog;
import com.logistimo.inventory.service.InventoryManagementService;
import com.logistimo.inventory.service.impl.InventoryManagementServiceImpl;
import com.logistimo.materials.entity.IMaterial;
import com.logistimo.materials.service.MaterialCatalogService;
import com.logistimo.materials.service.impl.MaterialCatalogServiceImpl;

import org.apache.commons.lang.StringEscapeUtils;
import com.logistimo.services.Resources;
import com.logistimo.services.Services;
import com.logistimo.tags.TagUtil;
import com.logistimo.utils.BigUtil;
import com.logistimo.constants.CharacterConstants;
import com.logistimo.constants.Constants;
import com.logistimo.utils.LocalDateUtil;
import com.logistimo.utils.NumberUtil;
import com.logistimo.utils.StringUtil;
import com.logistimo.logger.XLog;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.TimeZone;

/**
 * Created by charan on 08/03/17.
 */
public class InvntryEvntLogExportHandler implements  IExportHandler {

  private static final XLog xLogger = XLog.getLog(InvntryEvntLogExportHandler.class);


  IInvntryEvntLog invntryEvntLog;

  public InvntryEvntLogExportHandler(IInvntryEvntLog invntryEvntLog){
    this.invntryEvntLog = invntryEvntLog;
  }

  public String getCSVHeader(Locale locale, DomainConfig dc, String type) {
    xLogger.fine("Entering getCSVHeader. locale: {0}", locale);
    ResourceBundle messages = Resources.get().getBundle("Messages", locale);
    ResourceBundle jsMessages = Resources.get().getBundle("JSMessages", locale);
    StringBuilder header = new StringBuilder();
    header.append(messages.getString("material")).append(CharacterConstants.SPACE)
        .append(jsMessages.getString("id")).append(CharacterConstants.COMMA)
        .append(messages.getString("customid.material")).append(CharacterConstants.COMMA)
        .append(messages.getString("material.name")).append(CharacterConstants.COMMA)
        .append(messages.getString("kiosk")).append(CharacterConstants.SPACE)
        .append(jsMessages.getString("id")).append(CharacterConstants.COMMA)
        .append(messages.getString("customid.entity")).append(CharacterConstants.COMMA)
        .append(messages.getString("kiosk.name")).append(CharacterConstants.COMMA)
        .append(messages.getString("country")).append(CharacterConstants.COMMA)
        .append(messages.getString("state")).append(CharacterConstants.COMMA)
        .append(messages.getString("district")).append(CharacterConstants.COMMA)
        .append(messages.getString("taluk")).append(CharacterConstants.COMMA)
        .append(messages.getString("village")).append(CharacterConstants.COMMA)
        .append(messages.getString("type")).append(CharacterConstants.COMMA)
        .append(messages.getString("stock")).append(CharacterConstants.COMMA)
        .append(messages.getString("min")).append(CharacterConstants.COMMA)
        .append(messages.getString("max")).append(CharacterConstants.COMMA)
        .append(messages.getString("duration")).append(CharacterConstants.SPACE)
        .append(CharacterConstants.O_BRACKET)
        .append(messages.getString("days")).append(CharacterConstants.C_BRACKET)
        .append(CharacterConstants.COMMA)
        .append(messages.getString("from")).append(CharacterConstants.COMMA)
        .append(messages.getString("until")).append(CharacterConstants.COMMA)
        .append(messages.getString("kiosk")).append(CharacterConstants.SPACE)
        .append(jsMessages.getString("tags.lower")).append(CharacterConstants.COMMA)
        .append(messages.getString("material")).append(CharacterConstants.SPACE)
        .append(jsMessages.getString("tags.lower"));

    xLogger.info("Exiting getCSVHeader. locale: {0}", locale);
    return header.toString();
  }

  @Override
  public String toCSV(Locale locale, String timezone, DomainConfig dc, String type) {
    xLogger.fine(
        "Entering toCSV. locale: {0}, timezone: {1}, kId: {2}, mId: {3}, startDate: {4}, endDate: {5}",
        locale, timezone, invntryEvntLog.getKioskId(), invntryEvntLog.getMaterialId(), invntryEvntLog.getStartDate(), invntryEvntLog.getEndDate());
    try {
      // Get services
      EntitiesService as = Services.getService(EntitiesServiceImpl.class);
      MaterialCatalogService mcs = Services.getService(MaterialCatalogServiceImpl.class);
      InventoryManagementService
          ims =
          Services.getService(InventoryManagementServiceImpl.class);

      IKiosk k = as.getKiosk(invntryEvntLog.getKioskId(), false);
      IMaterial m = mcs.getMaterial(invntryEvntLog.getMaterialId());
      IInvntry i = ims.getInventory(invntryEvntLog.getKioskId(), invntryEvntLog.getMaterialId());
      xLogger.fine("kiosk: {0}, material: {1}, inventory: {2}", k, m, i);
      // Compute the duration
      Date startDate = invntryEvntLog.getStartDate();
      // Add the latest entry for the day
      Calendar cal = GregorianCalendar.getInstance(locale);
      cal.setTimeZone(TimeZone.getTimeZone(timezone));
      cal.setTime(startDate);

      Date endDate = invntryEvntLog.getEndDate();
      if (endDate == null) {
        endDate = new Date();
      }
      cal.setTime(endDate);
      float
          duration =
          (endDate.getTime() - startDate.getTime())
              / 86400000F; // duration must be computed here, given end date can be null and duration in table will be zero for open events (i.e. not yet resolved)
      List<String> ktgs = invntryEvntLog.getTags(TagUtil.TYPE_ENTITY);
      List<String> mtgs = invntryEvntLog.getTags(TagUtil.TYPE_MATERIAL);
      StringBuilder csv = new StringBuilder();
      if (k != null && m != null && i != null) {
        csv.append(invntryEvntLog.getMaterialId()).append(CharacterConstants.COMMA)
            .append(m.getCustomId() != null ? StringEscapeUtils.escapeCsv(m.getCustomId())
                : CharacterConstants.EMPTY).append(CharacterConstants.COMMA)
            .append(StringEscapeUtils.escapeCsv(m.getName())).append(CharacterConstants.COMMA)
            .append(invntryEvntLog.getKioskId()).append(CharacterConstants.COMMA)
            .append(k.getCustomId() != null ? StringEscapeUtils.escapeCsv(k.getCustomId())
                : CharacterConstants.EMPTY).append(CharacterConstants.COMMA)
            .append(StringEscapeUtils.escapeCsv(k.getName())).append(CharacterConstants.COMMA)
            .append(StringEscapeUtils.escapeCsv(k.getCountry())).append(CharacterConstants.COMMA)
            .append(StringEscapeUtils.escapeCsv(k.getState())).append(CharacterConstants.COMMA)
            .append(StringEscapeUtils.escapeCsv(k.getDistrict())).append(CharacterConstants.COMMA)
            .append(StringEscapeUtils.escapeCsv(k.getTaluk())).append(CharacterConstants.COMMA)
            .append(StringEscapeUtils.escapeCsv(k.getCity())).append(CharacterConstants.COMMA)

            .append(invntryEvntLog.getType() != 0 ? StringEscapeUtils.escapeCsv(EventPublisher.getEventName(invntryEvntLog.getType(), locale))
                : CharacterConstants.EMPTY).append(CharacterConstants.COMMA)
            .append(BigUtil.getFormattedValue(i.getStock())).append(CharacterConstants.COMMA)
            .append(BigUtil.getFormattedValue(i.getReorderLevel())).append(CharacterConstants.COMMA)
            .append(BigUtil.getFormattedValue(i.getMaxStock())).append(CharacterConstants.COMMA)
            .append(String.valueOf(NumberUtil.round2(duration))).append(CharacterConstants.COMMA)
            .append(LocalDateUtil.formatCustom(startDate, Constants.DATETIME_CSV_FORMAT, timezone))
            .append(CharacterConstants.COMMA)
            .append(LocalDateUtil.formatCustom(endDate, Constants.DATETIME_CSV_FORMAT, timezone))
            .append(CharacterConstants.COMMA)
            .append(ktgs != null && !ktgs.isEmpty() ? StringUtil
                .getCSV(ktgs, CharacterConstants.SEMICOLON) : CharacterConstants.EMPTY)
            .append(CharacterConstants.COMMA)
            .append(mtgs != null && !mtgs.isEmpty() ? StringUtil
                .getCSV(mtgs, CharacterConstants.SEMICOLON) : CharacterConstants.EMPTY);

      }
      return csv.toString();
    } catch (Exception e) {
      xLogger.warn("{0} when getting CSV for inventory event log {1}: {2}", e.getClass().getName(),
          invntryEvntLog.getKey(), e.getMessage());
      return null;
    }

  }

}

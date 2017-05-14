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
import com.logistimo.reports.models.DomainUsageStats;

import com.logistimo.services.Resources;
import com.logistimo.utils.BigUtil;
import com.logistimo.constants.Constants;
import com.logistimo.utils.LocalDateUtil;
import com.logistimo.utils.NumberUtil;
import com.logistimo.logger.XLog;

import java.util.Date;
import java.util.Locale;
import java.util.ResourceBundle;

/**
 * Created by yuvaraj on 09/03/17.
 */
public class DomainUsageStatsExportHandler implements IExportHandler {

  private static final XLog xLogger = XLog.getLog(DomainUsageStats.class);
  private final DomainUsageStats domainUsageStats;

  public DomainUsageStatsExportHandler(DomainUsageStats domainUsageStats){
    this.domainUsageStats = domainUsageStats;
  }

  @Override
  public String getCSVHeader(Locale locale, DomainConfig dc, String type) {
    xLogger.fine("Entering getCSVHeader. locale: {0}", locale);
    ResourceBundle messages = Resources.get().getBundle("Messages", locale);
    String header = messages.getString("date") + "," +
        messages.getString("domain") + "," +
        messages.getString("transactions") + "," +
        messages.getString("orders") + "," +
        messages.getString("logins") + "," +
        messages.getString("currency") + "," +
        messages.getString("revenue") + "," +
        // messages.getString( "transactiveusers" ) + "," +
        // messages.getString( "activekiosks" ) + "," +
        messages.getString("cumulative") + " " + messages.getString("transactions") + "," +
        messages.getString("cumulative") + " " + messages.getString("orders") + "," +
        messages.getString("cumulative") + " " + messages.getString("logins") + "," +
        messages.getString("cumulative") + " " + messages.getString("revenue") + "," +
        messages.getString("users") + "," +
        messages.getString("kiosks") + "," +
        messages.getString("materials");
    xLogger.fine("Exiting getCSVHeader");
    return header;
  }

  @Override
  public String toCSV(Locale locale, String timezone, DomainConfig dc, String type) {
    xLogger.fine("Entering toCSV. locale: {0}, timezone: {1}", locale, timezone);
    try {

      String
          csv =
          LocalDateUtil.formatCustom(new Date(domainUsageStats.getT()), Constants.DATE_FORMAT_CSV, timezone) + "," +
              "\"" + domainUsageStats.getdName() + "\"," +
              NumberUtil.getFormattedValue(domainUsageStats.getTc()) + "," +
              NumberUtil.getFormattedValue(domainUsageStats.getOc()) + "," +
              NumberUtil.getFormattedValue(domainUsageStats.getLnc()) + "," +
              (domainUsageStats.getCur() != null ? domainUsageStats.getCur() : "") + "," +
              BigUtil.getFormattedValue(domainUsageStats.getBrv()) + "," +
              NumberUtil.getFormattedValue(domainUsageStats.getCtc()) + "," +
              NumberUtil.getFormattedValue(domainUsageStats.getCoc()) + "," +
              NumberUtil.getFormattedValue(domainUsageStats.getClnc()) + "," +
              BigUtil.getFormattedValue(domainUsageStats.getCbrv()) + "," +
              // NumberUtil.getFormattedValue( this.truc ) + "," +
              NumberUtil.getFormattedValue(domainUsageStats.getUc()) + "," +
              NumberUtil.getFormattedValue(domainUsageStats.getEc()) + "," +
              NumberUtil.getFormattedValue(domainUsageStats.getMc());
      // NumberUtil.getFormattedValue( this.aec ) + "," +

      return csv;
    } catch (Exception e) {
      xLogger.warn("{0} when getting CSV for usage statistics for domain {1}: {2}",
          e.getClass().getName(), domainUsageStats.getdName(), e.getMessage());
      return null;
    }
  }
}

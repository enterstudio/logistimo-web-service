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

import com.logistimo.assets.models.Temperature;
import com.logistimo.config.models.DomainConfig;

import com.logistimo.constants.CharacterConstants;
import com.logistimo.constants.Constants;
import com.logistimo.utils.LocalDateUtil;
import com.logistimo.logger.XLog;

import java.util.Date;
import java.util.Locale;

/**
 * Created by yuvaraj on 09/03/17.
 */
public class TemperatureExportHandler implements IExportHandler {


  private final Temperature temperature;

  public TemperatureExportHandler(Temperature temperature) {
    this.temperature = temperature;
  }

  private static final XLog xLogger = XLog.getLog(InvntryExportHandler.class);

  public static final int RAW_DATA = 0;
  public static final int INCURSION = 1;
  public static final int EXCURSION = 2;

  @Override
  public String getCSVHeader(Locale locale, DomainConfig dc, String type) {
    return "Asset ID" + CharacterConstants.COMMA + "Asset type" + CharacterConstants.COMMA
        + "Sensor ID" + CharacterConstants.COMMA +
        "Temperature" + CharacterConstants.COMMA + "Temperature type (raw/incursion/excursion)"
        + CharacterConstants.COMMA + "Power available (0/1)" +
        CharacterConstants.COMMA + "Time" + CharacterConstants.COMMA
        + "Channel type (sms/internet)";
  }

  @Override
  public String toCSV(Locale locale, String timezone, DomainConfig dc, String type) {
    Date d = new Date((long) temperature.time * 1000); // convert to millisecond
    String source = null;
    if (temperature.src != null) {
      try {
        source = temperature.GPRS_CODE == Integer.parseInt(temperature.src) ? temperature.GPRS : temperature.SMS;
      } catch (Exception e) {
        xLogger.warn("Error in setting channel source", e);
      }
    }
    if (source == null) {
      source = CharacterConstants.EMPTY;
    }
    String tempType = null;
    switch (temperature.typ) {
      case RAW_DATA:
        tempType = "Raw";
        break;
      case INCURSION:
        tempType = "Incursion";
        break;
      case EXCURSION:
        tempType = "Excursion";
        break;
    }
    return String.valueOf(temperature.tmp) + CharacterConstants.COMMA + tempType + CharacterConstants.COMMA
        + temperature.pwa + CharacterConstants.COMMA +
        LocalDateUtil.formatCustom(d, Constants.DATETIME_FORMAT, timezone)
        + CharacterConstants.COMMA + source;
  }
}

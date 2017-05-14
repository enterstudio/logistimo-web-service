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
import com.logistimo.materials.entity.IMaterial;
import com.logistimo.users.entity.IUserAccount;
import com.logistimo.users.service.UsersService;
import com.logistimo.users.service.impl.UsersServiceImpl;

import org.apache.commons.lang.StringEscapeUtils;
import com.logistimo.services.Resources;
import com.logistimo.services.Services;
import com.logistimo.utils.BigUtil;
import com.logistimo.constants.CharacterConstants;
import com.logistimo.constants.Constants;
import com.logistimo.utils.LocalDateUtil;
import com.logistimo.utils.NumberUtil;
import com.logistimo.utils.StringUtil;
import com.logistimo.logger.XLog;

import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

/**
 * Created by charan on 06/03/17.
 */
public class MaterialExportHandler implements IExportHandler {

  static XLog xLogger = XLog.getLog(MaterialExportHandler.class);

  IMaterial material;

  public MaterialExportHandler(IMaterial material){
    this.material = material;
  }

  // Get the CSV header for materials
  @Override
  public String getCSVHeader(Locale locale, DomainConfig dc, String type) {
    ResourceBundle bundle = Resources.get().getBundle("Messages", locale);
    ResourceBundle jsBundle = Resources.get().getBundle("JSMessages", locale);
    StringBuilder header = new StringBuilder();
    header.append(bundle.getString("material")).append(CharacterConstants.SPACE)
        .append(jsBundle.getString("id")).append(CharacterConstants.COMMA)
        .append(bundle.getString("customid.material")).append(CharacterConstants.COMMA)
        .append(bundle.getString("material.name")).append(CharacterConstants.COMMA)
        .append(bundle.getString("shortname")).append(CharacterConstants.COMMA)
        .append(bundle.getString("description")).append(CharacterConstants.COMMA)
        .append(bundle.getString("material.addinfo")).append(CharacterConstants.COMMA)
        .append(bundle.getString("material.addinfocheck")).append(CharacterConstants.COMMA)
        .append(bundle.getString("tags")).append(CharacterConstants.COMMA)
        .append(bundle.getString("material.msrp")).append(CharacterConstants.COMMA)
        .append(bundle.getString("material.retailerprice")).append(CharacterConstants.COMMA)
        .append(bundle.getString("currency")).append(CharacterConstants.COMMA)
        .append(jsBundle.getString("binaryvalued")).append(CharacterConstants.COMMA)
        .append(bundle.getString("material.seasonal")).append(CharacterConstants.COMMA)
        .append(bundle.getString("batch.enable")).append(CharacterConstants.COMMA)
        .append(bundle.getString("temperature.sensitive")).append(CharacterConstants.COMMA)
        .append(bundle.getString("temperature")).append(CharacterConstants.SPACE)
        .append(jsBundle.getString("min.lower")).append(CharacterConstants.O_BRACKET).append("C")
        .append(CharacterConstants.C_BRACKET).append(CharacterConstants.COMMA)
        .append(bundle.getString("temperature")).append(CharacterConstants.SPACE)
        .append(jsBundle.getString("max.lower")).append(CharacterConstants.O_BRACKET).append("C")
        .append(CharacterConstants.C_BRACKET).append(CharacterConstants.COMMA)
        .append(jsBundle.getString("createdby")).append(CharacterConstants.SPACE)
        .append(jsBundle.getString("id")).append(CharacterConstants.COMMA)
        .append(jsBundle.getString("createdby")).append(CharacterConstants.SPACE)
        .append(jsBundle.getString("customid.lower")).append(CharacterConstants.COMMA)
        .append(jsBundle.getString("createdby")).append(CharacterConstants.SPACE)
        .append(jsBundle.getString("fullname.lower")).append(CharacterConstants.COMMA)
        .append(bundle.getString("createdon")).append(CharacterConstants.COMMA)
        .append(bundle.getString("updatedby")).append(CharacterConstants.SPACE)
        .append(jsBundle.getString("id")).append(CharacterConstants.COMMA)
        .append(bundle.getString("updatedby")).append(CharacterConstants.SPACE)
        .append(jsBundle.getString("customid.lower")).append(CharacterConstants.COMMA)
        .append(bundle.getString("updatedby")).append(CharacterConstants.SPACE)
        .append(jsBundle.getString("fullname.lower")).append(CharacterConstants.COMMA)
        .append(bundle.getString("updatedon"));

    return header.toString();
  }



  // Get the CSV for this material
  @Override
  public String toCSV(Locale locale, String timezone, DomainConfig dc, String type) {
    IUserAccount cbUser = null, ubUser = null;
    String cbFullName = null, ubFullName = null, cbCustomID = null, ubCustomID = null;
    try {
      // Get services
      UsersService as = Services.getService(UsersServiceImpl.class);
      if (material.getCreatedBy() != null) {
        try {
          cbUser = as.getUserAccount(material.getCreatedBy());
          cbFullName = cbUser.getFullName();
          cbCustomID = cbUser.getCustomId();
        } catch (Exception e) {
          cbFullName = Constants.UNKNOWN;
        }
      }
      if (material.getLastUpdatedBy() != null) {
        try {
          ubUser = as.getUserAccount(material.getLastUpdatedBy());
          ubFullName = ubUser.getFullName();
          ubCustomID = ubUser.getCustomId();
        } catch (Exception e) {
          ubFullName = Constants.UNKNOWN;
        }
      }
    } catch (Exception e) {
      xLogger.warn("{0} when getting CSV for material {1}: {2}", e.getClass().getName(), material.getMaterialId(),
          e.getMessage());
    }
    List<String> tags = material.getTags();
    StringBuilder csv = new StringBuilder();
    csv.append(
        (material.getMaterialId() != null ? material.getMaterialId() : CharacterConstants.EMPTY))
        .append(CharacterConstants.COMMA)
        .append((material.getCustomId() != null ? StringEscapeUtils.escapeCsv(
            material.getCustomId()) : CharacterConstants.EMPTY))
        .append(CharacterConstants.COMMA)
        .append(StringEscapeUtils.escapeCsv(material.getName())).append(CharacterConstants.COMMA)
        .append((material.getShortName() != null ? StringEscapeUtils.escapeCsv(material.getShortName()) : CharacterConstants.EMPTY))
        .append(CharacterConstants.COMMA)
        .append(material.getDescription() != null ? StringEscapeUtils.escapeCsv(material.getDescription())
            : CharacterConstants.EMPTY).append(CharacterConstants.COMMA)
        .append(material.getInfo() != null ? StringEscapeUtils.escapeCsv(material.getInfo()) : CharacterConstants.EMPTY)
        .append(CharacterConstants.COMMA)
        .append(material.displayInfo() ? Constants.YES : Constants.NO)
        .append(CharacterConstants.COMMA)
        .append(
            tags != null && !tags.isEmpty() ? StringUtil.getCSV(tags, CharacterConstants.SEMICOLON)
                : CharacterConstants.EMPTY).append(CharacterConstants.COMMA)
        .append(
            material.getMSRP() != null ? BigUtil.getFormattedValue(material.getMSRP())
                : CharacterConstants.EMPTY)
        .append(CharacterConstants.COMMA)
        .append((material.getRetailerPrice() != null ? BigUtil.getFormattedValue(
            material.getRetailerPrice())
            : CharacterConstants.EMPTY)).append(CharacterConstants.COMMA)
        .append(material.getCurrency() != null ? material.getCurrency() : CharacterConstants.EMPTY)
        .append(CharacterConstants.COMMA)
        .append(IMaterial.TYPE_BINARY.equals(material.getType()) ? Constants.YES : Constants.NO)
        .append(CharacterConstants.COMMA)
        .append(material.isSeasonal() ? Constants.YES : Constants.NO)
        .append(CharacterConstants.COMMA)
        .append(material.isBatchEnabled() ? Constants.YES : Constants.NO)
        .append(CharacterConstants.COMMA)
        .append(material.isTemperatureSensitive() ? Constants.YES : Constants.NO)
        .append(CharacterConstants.COMMA)
        .append(NumberUtil.getFormattedValue(material.getTemperatureMin()))
        .append(CharacterConstants.COMMA)
        .append((NumberUtil.getFormattedValue(material.getTemperatureMax())))
        .append(CharacterConstants.COMMA)
        .append(material.getCreatedBy() != null ? material.getCreatedBy() : CharacterConstants.EMPTY).append(CharacterConstants.COMMA)
        .append(
            cbCustomID != null ? StringEscapeUtils.escapeCsv(cbCustomID) : CharacterConstants.EMPTY)
        .append(CharacterConstants.COMMA)
        .append(
            cbFullName != null ? StringEscapeUtils.escapeCsv(cbFullName) : CharacterConstants.EMPTY)
        .append(CharacterConstants.COMMA)
        .append(material.getTimeStamp() != null ? LocalDateUtil
            .formatCustom(material.getTimeStamp(), Constants.DATETIME_CSV_FORMAT, timezone)
            : CharacterConstants.EMPTY).append(CharacterConstants.COMMA)
        .append(material.getLastUpdatedBy() != null ? material.getLastUpdatedBy() : CharacterConstants.EMPTY).append(CharacterConstants.COMMA)
        .append(
            ubCustomID != null ? StringEscapeUtils.escapeCsv(ubCustomID) : CharacterConstants.EMPTY)
        .append(CharacterConstants.COMMA)
        .append(
            ubFullName != null ? StringEscapeUtils.escapeCsv(ubFullName) : CharacterConstants.EMPTY)
        .append(CharacterConstants.COMMA)
        .append(material.getLastUpdated() != null ? LocalDateUtil
            .formatCustom(material.getLastUpdated(), Constants.DATETIME_CSV_FORMAT, timezone)
            : CharacterConstants.EMPTY);

    return csv.toString();
  }
}

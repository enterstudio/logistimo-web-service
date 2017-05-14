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
import com.logistimo.orders.OrderUtils;
import com.logistimo.orders.models.DiscrepancyExportableModel;

import org.apache.commons.lang.StringEscapeUtils;
import com.logistimo.services.Resources;
import com.logistimo.utils.BigUtil;
import com.logistimo.constants.CharacterConstants;
import com.logistimo.constants.Constants;
import com.logistimo.utils.LocalDateUtil;
import com.logistimo.utils.StringUtil;
import com.logistimo.logger.XLog;

import java.util.Locale;
import java.util.ResourceBundle;

/**
 * Created by charan on 06/03/17.
 */
public class DiscrepancyExportHandler implements IExportHandler {

  private final DiscrepancyExportableModel discrepancyExportableModel;

  public DiscrepancyExportHandler(DiscrepancyExportableModel discrepancyExportableModel){
    this.discrepancyExportableModel = discrepancyExportableModel;
  }

  @Override
  public String getCSVHeader(Locale locale, DomainConfig dc, String type) {

    ResourceBundle messages = Resources.get().getBundle("Messages", locale);
    ResourceBundle jsMessages = Resources.get().getBundle("JSMessages", locale);
    StringBuilder headerSb = new StringBuilder();
    headerSb.append(jsMessages.getString("discrepancy")).append(CharacterConstants.COMMA)
        .append(messages.getString("order")).append(CharacterConstants.COMMA)
        .append(messages.getString("referenceid")).append(CharacterConstants.COMMA)
        .append(messages.getString("order")).append(CharacterConstants.SPACE)
        .append(jsMessages.getString("createdon.lower")).append(CharacterConstants.COMMA)
        .append(messages.getString("material")).append(CharacterConstants.SPACE)
        .append(jsMessages.getString("id")).append(CharacterConstants.COMMA)
        .append(messages.getString("customid.material")).append(CharacterConstants.COMMA)
        .append(messages.getString("material.name")).append(CharacterConstants.COMMA)
        .append(jsMessages.getString("order.recommended")).append(CharacterConstants.COMMA)
        .append(jsMessages.getString("ordered")).append(CharacterConstants.COMMA)
        .append(jsMessages.getString("discrepancy.ordering.reasons"))
        .append(CharacterConstants.COMMA)
        .append(jsMessages.getString("shipped")).append(CharacterConstants.COMMA)
        .append(jsMessages.getString("discrepancy.shipping.reasons"))
        .append(CharacterConstants.COMMA)
        .append(jsMessages.getString("received")).append(CharacterConstants.COMMA)
        .append(jsMessages.getString("discrepancy.fulfillment.reasons"))
        .append(CharacterConstants.SPACE).append(CharacterConstants.O_BRACKET)
        .append(jsMessages.getString("discrepancy.fulfillment.reasons.addinfo"))
        .append(CharacterConstants.C_BRACKET).append(CharacterConstants.COMMA)
        .append(messages.getString("status")).append(CharacterConstants.COMMA)
        .append(messages.getString("status")).append(CharacterConstants.SPACE)
        .append(jsMessages.getString("updatedon.lower")).append(CharacterConstants.COMMA)
        .append(messages.getString("customer")).append(CharacterConstants.SPACE)
        .append(jsMessages.getString("id")).append(CharacterConstants.COMMA)
        .append(messages.getString("customid.customer")).append(CharacterConstants.COMMA)
        .append(messages.getString("customer")).append(CharacterConstants.SPACE)
        .append(jsMessages.getString("name.lower")).append(CharacterConstants.COMMA)
        .append(messages.getString("vendor")).append(CharacterConstants.SPACE)
        .append(jsMessages.getString("id")).append(CharacterConstants.COMMA)
        .append(messages.getString("customid.vendor")).append(CharacterConstants.COMMA)
        .append(messages.getString("vendor")).append(CharacterConstants.SPACE)
        .append(jsMessages.getString("name.lower"));
    return headerSb.toString();

  }

  private static final XLog xLogger = XLog.getLog(InvntryExportHandler.class);


  @Override
  public String toCSV(Locale locale, String timezone, DomainConfig dc, String type) {
    xLogger.fine("Entering toCSV. locale: {0}, timezone: {1}", locale, timezone);
    StringBuilder csv = new StringBuilder();
    csv.append((discrepancyExportableModel.discTypes != null && !discrepancyExportableModel.discTypes.isEmpty() ? StringUtil
        .getCSV(discrepancyExportableModel.discTypes, CharacterConstants.SEMICOLON) : CharacterConstants.EMPTY))
        .append(CharacterConstants.COMMA)
        .append(discrepancyExportableModel.oId != null ? discrepancyExportableModel.oId : CharacterConstants.EMPTY).append(CharacterConstants.COMMA)
        .append(discrepancyExportableModel.rId != null ? StringEscapeUtils.escapeCsv(discrepancyExportableModel.rId) : CharacterConstants.EMPTY)
        .append(CharacterConstants.COMMA)
        .append(
            discrepancyExportableModel.oct != null ? LocalDateUtil.formatCustom(discrepancyExportableModel.oct, Constants.DATETIME_CSV_FORMAT, timezone)
                : CharacterConstants.EMPTY).append(CharacterConstants.COMMA)
        .append(discrepancyExportableModel.mId != null ? discrepancyExportableModel.mId : CharacterConstants.EMPTY).append(CharacterConstants.COMMA)
        .append((discrepancyExportableModel.cmId != null ? StringEscapeUtils.escapeCsv(discrepancyExportableModel.cmId) : CharacterConstants.EMPTY))
        .append(CharacterConstants.COMMA)
        .append(discrepancyExportableModel.mnm != null ? StringEscapeUtils.escapeCsv(discrepancyExportableModel.mnm) : CharacterConstants.EMPTY)
        .append(CharacterConstants.COMMA)
        .append(BigUtil.getFormattedValue(discrepancyExportableModel.roq)).append(CharacterConstants.COMMA)
        .append(BigUtil.getFormattedValue(discrepancyExportableModel.oq)).append(CharacterConstants.COMMA)
        .append(
            discrepancyExportableModel.hasOd && discrepancyExportableModel.odrsn != null ? StringEscapeUtils.escapeCsv(discrepancyExportableModel.odrsn) : CharacterConstants.EMPTY)
        .append(CharacterConstants.COMMA)
        .append(BigUtil.getFormattedValue(discrepancyExportableModel.sq)).append(CharacterConstants.COMMA)
        .append(
            discrepancyExportableModel.hasSd && discrepancyExportableModel.sdrsn != null ? StringEscapeUtils.escapeCsv(discrepancyExportableModel.sdrsn) : CharacterConstants.EMPTY)
        .append(CharacterConstants.COMMA)
        .append(BigUtil.getFormattedValue(discrepancyExportableModel.fq)).append(CharacterConstants.COMMA)
        .append((discrepancyExportableModel.hasFd && discrepancyExportableModel.fdrsns != null && !discrepancyExportableModel.fdrsns.isEmpty() ? StringEscapeUtils
            .escapeCsv(StringUtil.getCSV(discrepancyExportableModel.fdrsns, CharacterConstants.SEMICOLON))
            : CharacterConstants.EMPTY)).append(CharacterConstants.COMMA)
        .append(OrderUtils.getStatusDisplay(discrepancyExportableModel.st, locale)).append(CharacterConstants.COMMA)
        .append(
            discrepancyExportableModel.stt != null ? LocalDateUtil.formatCustom(discrepancyExportableModel.stt, Constants.DATETIME_CSV_FORMAT, timezone)
                : CharacterConstants.EMPTY).append(CharacterConstants.COMMA)
        .append(discrepancyExportableModel.cId != null ? discrepancyExportableModel.cId : CharacterConstants.EMPTY).append(CharacterConstants.COMMA)
        .append(discrepancyExportableModel.ccId != null ? StringEscapeUtils.escapeCsv(discrepancyExportableModel.ccId) : CharacterConstants.EMPTY)
        .append(CharacterConstants.COMMA)
        .append(discrepancyExportableModel.cnm != null ? StringEscapeUtils.escapeCsv(discrepancyExportableModel.cnm) : CharacterConstants.EMPTY)
        .append(CharacterConstants.COMMA)
        .append(discrepancyExportableModel.vId != null ? discrepancyExportableModel.vId : CharacterConstants.EMPTY).append(CharacterConstants.COMMA)
        .append(discrepancyExportableModel.cvId != null ? StringEscapeUtils.escapeCsv(discrepancyExportableModel.cvId) : CharacterConstants.EMPTY)
        .append(CharacterConstants.COMMA)
        .append(discrepancyExportableModel.vnm != null ? StringEscapeUtils.escapeCsv(discrepancyExportableModel.vnm) : CharacterConstants.EMPTY);

    return csv.toString();
  }
}

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
import com.logistimo.reports.entity.slices.IMonthSlice;
import com.logistimo.services.Resources;
import com.logistimo.utils.NumberUtil;

import java.util.Locale;
import java.util.ResourceBundle;

/**
 * Created by charan on 12/03/17.
 */
public class MonthSliceExportHandler implements IExportHandler {

  private final IMonthSlice slice;
  private final SliceExportHandler daySliceHandler;

  public MonthSliceExportHandler(IMonthSlice slice) {
    this.slice = slice;
    this.daySliceHandler = new SliceExportHandler(slice);
  }

  public String getCSVHeader(Locale locale, DomainConfig dc, String type) {
    String header = daySliceHandler.getCSVHeader(locale, dc, type);
    ResourceBundle messages = Resources.get().getBundle("Messages", locale);
    header += "," + messages.getString("stockout") + " " + messages.getString("duration") + "," +
        messages.getString("stockout") + " " + messages.getString("count") + "," +
        messages.getString("stockout") + " " + messages.getString("duration") + " " + messages
        .getString("inventory.lessthanmin") + "," +
        messages.getString("stockout") + " " + messages.getString("count") + " " + messages
        .getString("inventory.lessthanmin") + "," +
        messages.getString("stockout") + " " + messages.getString("duration") + " " + messages
        .getString("inventory.morethanmax") + "," +
        messages.getString("stockout") + " " + messages.getString("count") + " " + messages
        .getString("inventory.morethanmax") + "," +
        messages.getString("order") + " " + messages.getString("order.processingtime") + "," +
        messages.getString("order") + " " + messages.getString("processingcount") + "," +
        messages.getString("order.deliveryleadtime") + "," +
        messages.getString("deliveryleadcount")
    ;
    return header;
  }

  public String toCSV(Locale locale, String timezone, DomainConfig dc, String type) {
    String csv = daySliceHandler.toCSV(locale, timezone, dc, type);
    csv += "," + NumberUtil.getFloatValue(slice.getSod()) + "," +
        NumberUtil.getIntegerValue(slice.getSoc()) + "," +
        NumberUtil.getFloatValue(slice.getLmd()) + "," +
        NumberUtil.getIntegerValue(slice.getLmc()) + "," +
        NumberUtil.getFloatValue(slice.getGmd()) + "," +
        NumberUtil.getIntegerValue(slice.getGmc()) + "," +
        NumberUtil.getFloatValue(slice.getOpt()) + "," +
        NumberUtil.getIntegerValue(slice.getOpc()) + "," +
        NumberUtil.getFloatValue(slice.getDlt()) + "," +
        NumberUtil.getIntegerValue(slice.getDlc());
    return csv;
  }
}

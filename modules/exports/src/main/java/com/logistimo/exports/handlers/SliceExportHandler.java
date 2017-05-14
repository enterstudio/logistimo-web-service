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
import com.logistimo.config.models.ReportObjDimType;
import com.logistimo.config.models.ReportObjDimValue;
import com.logistimo.reports.entity.slices.ISlice;
import com.logistimo.reports.entity.slices.ReportsSlice;
import com.logistimo.reports.utils.ReportsUtil;

import com.logistimo.services.Resources;
import com.logistimo.constants.Constants;
import com.logistimo.utils.LocalDateUtil;
import com.logistimo.logger.XLog;

import java.util.Locale;
import java.util.ResourceBundle;

/**
 * Created by charan on 08/03/17.
 */
public class SliceExportHandler implements IExportHandler {

  private static final XLog xLogger = XLog.getLog(SliceExportHandler.class);

  private final ISlice slice;

  public SliceExportHandler(ISlice slice) {
    this.slice = slice;
  }

  @Override
  public String getCSVHeader(Locale locale, DomainConfig dc, String type) {
    xLogger.fine("Entering getCSVHeader. locale: {0}", locale);
    ResourceBundle messages = Resources.get().getBundle("Messages", locale);
    ReportObjDimType reportObjDimType = ReportsUtil.getObjectDimType(((ReportsSlice)slice).cassandraRow);
    String header = messages.getString("date") + ",";
    header += ReportsUtil.getDisplayName(reportObjDimType.objType, messages) + ",";
    header += ReportsUtil.getDisplayName(reportObjDimType.dimType, messages) + "," +
        messages.getString("issues") + "," +
        messages.getString("receipts") + "," +
        messages.getString("transactions.stockcounts.upper") + "," +
        messages.getString("transactions.wastage.upper") + "," +
        messages.getString("transfers") + "," +
        messages.getString("report.transactioncounts") + "," +
        messages.getString("demand") + "," +
        messages.getString("orders") + "," +
        messages.getString("returns") + "," +
        messages.getString("transactions.issue.upper") + " " + messages.getString("quantity") + ","
        +
        messages.getString("transactions.receipt.upper") + " " + messages.getString("quantity")
        + "," +
        messages.getString("stock") + " " + messages.getString("quantity") + "," +
        messages.getString("stock") + " " + messages.getString("quantity") + " " + messages
        .getString("difference") + "," +
        messages.getString("transactions.wastage.upper") + " " + messages.getString("quantity")
        + "," +
        messages.getString("transactions.transfer.upper") + " " + messages.getString("quantity")
        + "," +
        messages.getString("transactions.return.upper") + " " + messages.getString("quantity") + ","
        +
        messages.getString("demand") + " " + messages.getString("quantity") + "," +
        messages.getString("order") + " " + messages.getString("size") + "," +
        messages.getString("revenue") + " " + messages.getString("booked") + "," +
        messages.getString("revenue") + " " + messages.getString("realizable") + "," +
        messages.getString("logins") + "," +
        messages.getString("stockout") + " " + messages.getString("events") + "," +
        messages.getString("inventory.lessthanmin") + " " + messages.getString("events") + "," +
        messages.getString("inventory.morethanmax") + " " + messages.getString("events") + "," +
        messages.getString("total") + " " + messages.getString("abnormalstock") + " " + messages
        .getString("events") + "," +
        messages.getString("orders") + " " + messages.getString("pending") + "," +
        messages.getString("orders") + " " + messages.getString("order.confirmed") + "," +
        messages.getString("orders") + " " + messages.getString("order.shipped") + "," +
        messages.getString("orders") + " " + messages.getString("order.fulfilled") + "," +
        messages.getString("orders") + " " + messages.getString("order.cancelled") + "," +
        messages.getString("orders") + " " + messages.getString("paid") + "," +
        messages.getString("demand") + " " + messages.getString("order.shipped") + "," +
        messages.getString("demand") + " " + messages.getString("order.shipped") + " " + messages
        .getString("quantity") + "," +
        messages.getString("message.sms") + " " + messages.getString("count") + "," +
        messages.getString("user.email") + " " + messages.getString("count");
    xLogger.fine("Exiting getCSVHeader");
    return header;
  }

  @Override
  public String toCSV(Locale locale, String timezone, DomainConfig dc, String type) {
    xLogger.fine("Entering toCSV. locale: {0}, timezone: {1}", locale, timezone);
    String csv = "";
    ReportObjDimValue reportObjDimValue = ReportsUtil.getObjectDimValue(((ReportsSlice)slice).cassandraRow);
    if (reportObjDimValue != null) {
      csv = LocalDateUtil.formatCustom(slice.getDate(), Constants.DATETIME_CSV_FORMAT, null) + ",";
      csv += "\"" + reportObjDimValue.objValue + "\",";
      csv += "\"" + reportObjDimValue.dimValue + "\"" + "," +
          slice.getIssueCount() + "," +
          slice.getReceiptCount() + "," +
          slice.getStockcountCount() + "," +
          slice.getWastageCount() + "," +
          slice.getTransferCount() + "," +
          slice.getTotalCount() + "," +
          slice.getDemandCount() + "," +
          slice.getOrderCount() + "," +
          slice.getReturnCount() + "," +
          slice.getIssueQuantity() + "," +
          slice.getReceiptQuantity() + "," +
          slice.getStockQuantity() + "," +
          slice.getStockDifference() + "," +
          slice.getWastageQuantity() + "," +
          slice.getTransferQuantity() + "," +
          slice.getReturnQuantity() + "," +
          slice.getDemandQuantity() + "," +
          slice.getOrderSize() + "," +
          slice.getRevenueBooked() + "," +
          slice.getRevenueRealizable() + "," +
          slice.getLoginCounts() + "," +
          slice.getStockoutEventCounts() + "," +
          slice.getLessThanMinEventCounts() + "," +
          slice.getGreaterThanMaxEventCounts() + "," +
          slice.getTotalAbnormalStockEventCounts() + "," +
          slice.getPendingOrderCount() + "," +
          slice.getConfirmedOrderCount() + "," +
          slice.getShippedOrderCount() + "," +
          slice.getFulfilledOrderCount() + "," +
          slice.getCancelledOrderCount() + "," +
          slice.getPaidOrderCount() + "," +
          slice.getDemandShippedCount() + "," +
          slice.getDemandShippedQuantity() + "," +
          slice.getSMSCount() + "," +
          slice.getEmailCount();
    }
    return csv;
  }

}

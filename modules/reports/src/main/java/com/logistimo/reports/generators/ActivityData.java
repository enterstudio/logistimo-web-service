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

/**
 *
 */
package com.logistimo.reports.generators;

import com.google.visualization.datasource.datatable.ColumnDescription;
import com.google.visualization.datasource.datatable.DataTable;
import com.google.visualization.datasource.datatable.TableRow;
import com.google.visualization.datasource.datatable.value.DateValue;
import com.google.visualization.datasource.datatable.value.ValueType;

import com.logistimo.reports.entity.slices.ISlice;

import com.logistimo.utils.BigUtil;
import com.logistimo.logger.XLog;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * @author Arun
 */
public class ActivityData extends ReportData {

  // Types

  // Column IDs
  public static final String DATE = "0";
  public static final String ISSUES = "1";
  public static final String RECEIPTS = "2";
  public static final String STOCKCOUNTS = "3";
  public static final String WASTAGE = "4";
  public static final String TOTAL = "5";
  public static final String ORDERS = "6";
  public static final String REVENUE = "7";
  public static final String LOGINS = "8";

  private static final XLog xLogger = XLog.getLog(ActivityData.class);

  public ActivityData(Date from, Date until, Map<String, Object> filters,
                      Locale locale, String timezone, List<? extends ISlice> slices,
                      String cursor) {
    super(from, until, filters, locale, timezone, slices, cursor, null);
  }

  @SuppressWarnings("unchecked")
  @Override
  public DataTable generateDataTable(String reportSubType) throws ReportingDataException {
    if (results == null || results.isEmpty()) {
      throw new ReportingDataException(messages.getString("nodataavailable"));
    }
    DataTable data = new DataTable();
    try {
      // Columns
      data.addColumn(new ColumnDescription(DATE, ValueType.DATE, messages.getString("time")));
      data.addColumn(new ColumnDescription(ISSUES, ValueType.NUMBER, messages.getString("issues")));
      data.addColumn(
          new ColumnDescription(RECEIPTS, ValueType.NUMBER, messages.getString("receipts")));
      data.addColumn(new ColumnDescription(STOCKCOUNTS, ValueType.NUMBER,
          messages.getString("stock") + " " + jsMessages.getString("count") + "s"));
      data.addColumn(new ColumnDescription(WASTAGE, ValueType.NUMBER,
          messages.getString("transactions.wastage.upper")));
      data.addColumn(new ColumnDescription(TOTAL, ValueType.NUMBER, messages.getString("total")));
      data.addColumn(new ColumnDescription(ORDERS, ValueType.NUMBER, messages.getString("orders")));
      data.addColumn(
          new ColumnDescription(REVENUE, ValueType.NUMBER, messages.getString("revenue")));
      data.addColumn(new ColumnDescription(LOGINS, ValueType.NUMBER, messages.getString("logins")));
      // Add rows
      Iterator<? extends ISlice> it = results.iterator();
      Calendar cal = GregorianCalendar.getInstance();
      while (it.hasNext()) {
        ISlice slice = it.next();
        cal.setTime(slice.getDate());
        TableRow tr = new TableRow();
        DateValue
            dv =
            new DateValue(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH),
                cal.get(Calendar.DAY_OF_MONTH));
        tr.addCell(dv);
        tr.addCell(slice.getIssueCount());
        tr.addCell(slice.getReceiptCount());
        tr.addCell(slice.getStockcountCount());
        tr.addCell(slice.getWastageCount());
        tr.addCell(slice.getTotalCount());
        tr.addCell(slice.getOrderCount());
        tr.addCell(BigUtil.round2(slice.getRevenueBooked()).doubleValue());
        tr.addCell(slice.getLoginCounts());
        // Update data
        data.addRow(tr);
      }
    } catch (Exception e) {
      xLogger.severe("{0} when generating datatable: {1}", e.getClass().getName(), e.getMessage());
    }
    return data;
  }
}
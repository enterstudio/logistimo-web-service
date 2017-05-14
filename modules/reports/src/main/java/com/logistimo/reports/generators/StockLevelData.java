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

import com.google.visualization.datasource.base.TypeMismatchException;
import com.google.visualization.datasource.datatable.ColumnDescription;
import com.google.visualization.datasource.datatable.DataTable;
import com.google.visualization.datasource.datatable.TableRow;
import com.google.visualization.datasource.datatable.value.DateValue;
import com.google.visualization.datasource.datatable.value.ValueType;

import com.logistimo.inventory.entity.IInvntryLog;

import com.logistimo.logger.XLog;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Represents stock level over time
 *
 * @author Arun
 */
public class StockLevelData extends ReportData {

  private static final XLog xLogger = XLog.getLog(StockLevelData.class);

  private BigDecimal reorderLevel = BigDecimal.ZERO;

  public StockLevelData(Date from, Date until, Map<String, Object> filters,
                        Locale locale, String timezone, List<IInvntryLog> results, String cursor,
                        BigDecimal reorderLevel) {
    super(from, until, filters, locale, timezone, results, cursor);
    this.reorderLevel = reorderLevel;
  }

  public BigDecimal getReorderLevel() {
    return reorderLevel;
  }

  /**
   * Generate stock level data over time for a given material and kiosk
   */
  @SuppressWarnings("unchecked")
  @Override
  public DataTable generateDataTable(String reportSubType) throws ReportingDataException {
    xLogger.fine("Entered generateDataTable");
    if (results == null || results.size() == 0) {
      throw new ReportingDataException(messages.getString("nodataavailable"));
    }
    // Instantiate the DataTable for Google Visualization data formatting
    DataTable data = new DataTable();
    // Add the column descriptions
    ArrayList<ColumnDescription> cd = new ArrayList<ColumnDescription>();
    cd.add(new ColumnDescription("d", ValueType.DATE, jsMessages.getString("date")));
    cd.add(new ColumnDescription("s", ValueType.NUMBER, messages.getString("stock")));
    cd.add(
        new ColumnDescription("r", ValueType.NUMBER, messages.getString("inventory.reorderlevel")));
    cd.add(new ColumnDescription("a", ValueType.TEXT, jsMessages.getString("annotation")));
    // Add columns to data
    data.addColumns(cd);
    // Add rows
    Iterator<IInvntryLog> it = results.iterator();
    IInvntryLog prevLog = null;
    if (it.hasNext()) {
      prevLog = it.next();
    }
    Calendar prevCal = GregorianCalendar.getInstance();
    prevCal.setTime(prevLog.getCreatedOn());
    while (it.hasNext()) {
      IInvntryLog curLog = it.next();
      // Take only the latest entry in a given day (since multiple stock updates can happen over the day)
      Date curDate = curLog.getCreatedOn();
      Calendar cal = GregorianCalendar.getInstance();
      cal.setTime(curDate);
      // Check if the current day is the same as previous day, if so go to next entry
      if (cal.get(Calendar.MONTH) == prevCal.get(Calendar.MONTH) && (
          cal.get(Calendar.DATE) == prevCal.get(Calendar.DATE) || !it.hasNext())) {
        prevLog = curLog;
        prevCal.setTime(curDate);
        continue;
      }
      // Add row
      addRow(data, prevCal, prevLog);
      prevLog = curLog;
      prevCal.setTime(curDate);
    }
    // Add the final row
    addRow(data, prevCal, prevLog);
    xLogger.fine("Exiting generateDataTable");
    return data;
  }

  // Add a row to data
  private void addRow(DataTable data, Calendar cal, IInvntryLog log) {
    // Add the latest entry for the day
    DateValue
        dv =
        new DateValue(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH),
            cal.get(Calendar.DAY_OF_MONTH));
    TableRow tr = new TableRow();
    try {
      tr.addCell(dv);
      tr.addCell(log.getStock().longValue());
      tr.addCell(this.reorderLevel.longValue());
      tr.addCell("");
      // Add row to data
      data.addRow(tr);
    } catch (TypeMismatchException e) {
      xLogger.warn("TypeMismatchException: {0}", e.getLocalizedMessage());
    }
  }
}

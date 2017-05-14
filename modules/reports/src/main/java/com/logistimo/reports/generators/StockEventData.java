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
import com.google.visualization.datasource.datatable.TableCell;
import com.google.visualization.datasource.datatable.TableRow;
import com.google.visualization.datasource.datatable.value.DateTimeValue;
import com.google.visualization.datasource.datatable.value.ValueType;

import com.logistimo.dao.JDOUtils;
import com.logistimo.entities.entity.IKiosk;
import com.logistimo.inventory.dao.IInvntryDao;
import com.logistimo.inventory.dao.impl.InvntryDao;
import com.logistimo.inventory.entity.IInvntry;
import com.logistimo.inventory.entity.IInvntryEvntLog;
import com.logistimo.materials.entity.IMaterial;

import com.logistimo.services.impl.PMF;
import com.logistimo.utils.LocalDateUtil;
import com.logistimo.utils.NumberUtil;
import com.logistimo.logger.XLog;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

import javax.jdo.PersistenceManager;

/**
 * @author Arun
 */
public class StockEventData extends ReportData {

  private static final XLog xLogger = XLog.getLog(StockEventData.class);
  private IInvntryDao invntryDao = new InvntryDao();

  /**
   * @param from
   * @param until
   * @param filters
   * @param locale
   * @param timezone
   * @param results
   * @param cursor
   */
  @SuppressWarnings("rawtypes")
  public StockEventData(Date from, Date until, Map<String, Object> filters,
                        Locale locale, String timezone, List results, String cursor) {
    super(from, until, filters, locale, timezone, results, cursor);
  }

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
    cd.add(new ColumnDescription("m", ValueType.TEXT, messages.getString("material")));
    cd.add(new ColumnDescription("k", ValueType.TEXT, messages.getString("kiosk")));
    cd.add(new ColumnDescription("stk", ValueType.NUMBER, messages.getString("stock")));
    cd.add(new ColumnDescription("min", ValueType.NUMBER, messages.getString("min")));
    cd.add(new ColumnDescription("max", ValueType.NUMBER, messages.getString("max")));
    cd.add(new ColumnDescription("dr", ValueType.NUMBER,
        messages.getString("duration") + " - " + messages.getString("days")));
    cd.add(new ColumnDescription("sd", ValueType.DATETIME, messages.getString("from")));
    cd.add(new ColumnDescription("ed", ValueType.DATETIME, messages.getString("until")));
    // TODO: Remove the commented code below.
    // cd.add( new ColumnDescription( "kid", ValueType.TEXT, "materialId" ) );
    // cd.add( new ColumnDescription( "mid", ValueType.TEXT, "kioskId" ) );
    cd.add(new ColumnDescription("mid", ValueType.TEXT, "materialId"));
    cd.add(new ColumnDescription("kid", ValueType.TEXT, "kioskId"));

    // Add columns to data
    data.addColumns(cd);
    // Add the rows
    PersistenceManager pm = PMF.get().getPersistenceManager();
    try {
      for (IInvntryEvntLog result : (Iterable<IInvntryEvntLog>) results) {
        addRow(data, result, pm);
      }
    } finally {
      pm.close();
    }
    return data;
  }

  // Add a row to data
  private void addRow(DataTable data, IInvntryEvntLog invEventLog, PersistenceManager pm) {
    Date startDate = invEventLog.getStartDate();
    // Add the latest entry for the day
    Calendar cal = GregorianCalendar.getInstance(locale);
    cal.setTimeZone(TimeZone.getTimeZone(timezone));
    cal.setTime(startDate);
    DateTimeValue
        startDv =
        new DateTimeValue(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH),
            cal.get(Calendar.DAY_OF_MONTH), cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE),
            cal.get(Calendar.SECOND), cal.get(Calendar.MILLISECOND));
    Date endDate = invEventLog.getEndDate();
    if (endDate == null) {
      endDate = new Date();
    }
    cal.setTime(endDate);
    float
        duration =
        (endDate.getTime() - startDate.getTime())
            / 86400000F; // duration must be computed here, given end date can be null and duration in table will be zero for open events (i.e. not yet resolved)
    DateTimeValue
        endDv =
        new DateTimeValue(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH),
            cal.get(Calendar.DAY_OF_MONTH), cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE),
            cal.get(Calendar.SECOND), cal.get(Calendar.MILLISECOND));
    TableRow tr = new TableRow();
    try {
      Long kioskId = invEventLog.getKioskId();
      Long materialId = invEventLog.getMaterialId();
      tr.addCell(JDOUtils.getObjectById(IMaterial.class, materialId, pm).getName());
      tr.addCell(JDOUtils.getObjectById(IKiosk.class, kioskId, pm).getName());
      IInvntry invntry = invntryDao.getInvntry(invEventLog);
      tr.addCell(invntry.getStock().longValue());
      tr.addCell(invntry.getReorderLevel().longValue());
      tr.addCell(invntry.getMaxStock().longValue());
      TableCell cell = new TableCell(duration);
      cell.setFormattedValue(String.valueOf(NumberUtil.round2(duration)));
      tr.addCell(cell);
      cell = new TableCell(startDv);
      cell.setFormattedValue(LocalDateUtil.format(startDate, locale, timezone));
      tr.addCell(cell);
      cell = new TableCell(endDv);
      cell.setFormattedValue(LocalDateUtil.format(endDate, locale, timezone));
      tr.addCell(cell);
      tr.addCell(String.valueOf(materialId));
      tr.addCell(String.valueOf(kioskId));
      // Add row to data
      data.addRow(tr);
    } catch (Exception e) {
      xLogger.warn(
          "{0} when getting stock-event data for mid-kid {1}-{2} in domain {3} starting {4}: {5}",
          e.getClass().getName(), invEventLog.getMaterialId(), invEventLog.getKioskId(),
          invEventLog.getDomainId(), invEventLog.getStartDate(), e.getMessage());
    }
  }
}

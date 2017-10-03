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
import com.google.visualization.datasource.datatable.TableCell;
import com.google.visualization.datasource.datatable.TableRow;
import com.google.visualization.datasource.datatable.value.DateValue;
import com.google.visualization.datasource.datatable.value.ValueType;

import com.logistimo.config.models.DomainConfig;
import com.logistimo.inventory.TransactionUtil;
import com.logistimo.inventory.entity.ITransaction;

import com.logistimo.reports.ReportsConstants;
import com.logistimo.utils.LocalDateUtil;
import com.logistimo.logger.XLog;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

// Google visualization library

/**
 * @author arun
 *
 *         Represents Transaction Report data, which reports on specific and total transaction counts,
 *         in a given time, and based on certain filters.
 */
public class TransReportData extends ReportData {

  // Transaction report subtypes
  public static final String SUBTYPE_SUMMARY = "smm";
  public static final String SUBTYPE_TIMELINE = "tml";

  // Transaction display names
  public static final String VAL_TOTAL = "Total";

  // Column names
  public static final String COL_TRANSTYPE = "Type";
  public static final String COL_TRANSCOUNT = "Number of Transactions";
  public static final String COL_TIME = "Time";
  public static final String COL_ANNOTATION = "Annotation";

  // Max. records sent for timeline views
  private static final int MAX_RECORDS = 10000;

  // Logger
  private static final XLog xLogger = XLog.getLog(TransReportData.class);

  public TransReportData(Date from, Date until, Map<String, Object> filters, Locale locale,
                         String timezone, List<ITransaction> trans, String cursor) {
    super(from, until, filters, locale, timezone, trans, cursor, null);
  }

  /**
   * Generate a data table for Google visualization. If a subtype is given, generate data accordingly.
   */
  public DataTable generateDataTable(String reportSubtype) throws ReportingDataException {
    if (SUBTYPE_TIMELINE.equals(reportSubtype)) {
      return generateTimelineDataTable();
    } else {
      return generateSummaryDataTable();
    }
  }

  /**
   * Return a object representation of the basic data table of transaction counts summary
   */
  @SuppressWarnings("unchecked")
  private DataTable generateSummaryDataTable() throws ReportingDataException {
    if (this.results == null || this.results.size() == 0) {
      throw new ReportingDataException("No transactions specified");
    }

    // Get the issue count
    int numIssues = 0;
    int numReceipts = 0;
    int numStockCounts = 0;
    int numOrders = 0;
    int numReorders = 0;
    // Get the respective transaction counts
    Iterator<ITransaction> it = this.results.iterator();
    while (it.hasNext()) {
      ITransaction t = it.next();
      if (ITransaction.TYPE_ISSUE.equals(t.getType())) {
        ++numIssues;
      } else if (ITransaction.TYPE_RECEIPT.equals(t.getType())) {
        ++numReceipts;
      } else if (ITransaction.TYPE_PHYSICALCOUNT.equals(t.getType())) {
        ++numStockCounts;
      } else if (ITransaction.TYPE_ORDER.equals(t.getType())) {
        ++numOrders;
      } else if (ITransaction.TYPE_REORDER.equals(t.getType())) {
        ++numReorders;
      }
    }
    // Get total counts
    int numTrans = numIssues + numReceipts + numStockCounts + numOrders + numReorders;
    // Get the transaction naming convention, if domain Id is available
    Long domainId = null;
    if (filters != null) {
      domainId = (Long) filters.get(ReportsConstants.FILTER_DOMAIN);
    }
    String transNaming = DomainConfig.TRANSNAMING_DEFAULT;
    if (domainId != null) {
      DomainConfig dc = DomainConfig.getInstance(domainId);
      if (dc != null) {
        transNaming = dc.getTransactionNaming();
      }
    }
    // Instantiate the DataTable for Google Visualization data formatting
    DataTable data = new DataTable();
    // Add the column descriptions
    ArrayList<ColumnDescription> cd = new ArrayList<ColumnDescription>();
    cd.add(new ColumnDescription("transtype", ValueType.TEXT, COL_TRANSTYPE)); // Transaction type
    cd.add(
        new ColumnDescription("transcount", ValueType.NUMBER, COL_TRANSCOUNT)); // Transaction count
    data.addColumns(cd);
    // Add the rows and fill the data table
    try {
      if (numIssues > 0) {
        data.addRowFromValues(
            TransactionUtil.getDisplayName(ITransaction.TYPE_ISSUE, transNaming, locale) + "s",
            numIssues);
      }
      if (numReceipts > 0) {
        data.addRowFromValues(
            TransactionUtil.getDisplayName(ITransaction.TYPE_RECEIPT, transNaming, locale) + "s",
            numReceipts);
      }
      if (numStockCounts > 0) {
        data.addRowFromValues(
            TransactionUtil.getDisplayName(ITransaction.TYPE_PHYSICALCOUNT, transNaming, locale)
                + "s", numStockCounts);
      }
      if (numOrders > 0) {
        data.addRowFromValues(
            TransactionUtil.getDisplayName(ITransaction.TYPE_ORDER, transNaming, locale) + "s",
            numOrders);
      }
      if (numReorders > 0) {
        data.addRowFromValues(
            TransactionUtil.getDisplayName(ITransaction.TYPE_REORDER, transNaming, locale) + "s",
            numReorders);
      }
      // Style the last row and add
      TableRow tr = new TableRow();
      TableCell totalNameCell = new TableCell(VAL_TOTAL);
      totalNameCell.setCustomProperty("style", "font-weight: bold;");
      tr.addCell(totalNameCell);
      TableCell totalCell = new TableCell(numTrans);
      totalCell.setCustomProperty("style", "font-weight: bold;");
      tr.addCell(totalCell);
      // Add last row
      data.addRow(tr);
    } catch (TypeMismatchException e) {
      xLogger.warn("Exception (type mismatch) while generating Transaction Count Report: {0}",
          e.getLocalizedMessage());
      throw new ReportingDataException(e.getLocalizedMessage());
    }

    return data;
  }

  /**
   * Given a transaction type, generate a (Google) JSON data table of time vs transaction-counts
   */
  @SuppressWarnings("unchecked")
  public DataTable generateTimelineDataTable() throws ReportingDataException {
    xLogger.fine("Entering generateTimelineDataTable");

    if (this.results == null || this.results.size() == 0) {
      throw new ReportingDataException("No transactions specified");
    }

    // Instantiate the DataTable for Google Visualization data formatting
    DataTable data = new DataTable();
    // Add the column descriptions
    ArrayList<ColumnDescription> cd = new ArrayList<ColumnDescription>();
    cd.add(new ColumnDescription("date", ValueType.DATE,
        COL_TIME)); // first-column has to be a Date value
    cd.add(
        new ColumnDescription("transcount", ValueType.NUMBER, COL_TRANSCOUNT)); // Transaction count
    cd.add(new ColumnDescription("annotation", ValueType.TEXT, COL_ANNOTATION));
    data.addColumns(cd);

    // Get the non-order transactions
    if (this.results == null || this.results.size() == 0) {
      return null;
    }
    boolean isTruncated = this.results.size() > MAX_RECORDS;
    Iterator<ITransaction> it = this.results.iterator();
    // Get the first transaction in the list
    ITransaction t = it.next();
    int
        numRecords =
        1; // tracks the total number of records processed, and ensure we don't go over MAX_RECORDS
    // Get the date
    Calendar nextCal, curCal = null;
    if (this.locale != null) {
      nextCal = GregorianCalendar.getInstance(this.locale);
      curCal = GregorianCalendar.getInstance(this.locale);
    } else {
      nextCal = GregorianCalendar.getInstance();
      curCal = GregorianCalendar.getInstance();
    }
    //Date curDate = t.getTimeStamp();
    curCal.setTime(t.getTimestamp());
    // Get the day (of month) of this transaction
    int curDay = curCal.get(Calendar.DATE);
    while (it.hasNext() && numRecords < MAX_RECORDS) {
      boolean isSameDay = true; // tracks whether the current transaction is in the same day
      int numTransPerDay = 1; // tracks the total number of transactions in a given day
      while (it.hasNext() && numRecords < MAX_RECORDS && isSameDay) {
        ITransaction dayTrans = it.next();
        ++numRecords;
        nextCal.setTime(dayTrans.getTimestamp());
        if (curDay == nextCal.get(Calendar.DATE)) {
          ++numTransPerDay;
        } else { // next day with transactions; form the record and start the next day iterations
          // Add the row of day counts to the data table
          try {
            TableRow tr = getTimelineTableRow(curCal, numTransPerDay, "");
            // Add the row to the data table
            data.addRow(tr);
            // Check if there are zero count days in-between current day and the next
            addZeroCountDays(data, nextCal,
                curCal); // NOTE: the records are ordered in descending order of time - hence nextCal will be lower than curCal
          } catch (TypeMismatchException e) {
            xLogger
                .warn("Exception while generating data for Transaction count timeline report: {0}",
                    e.getLocalizedMessage());
            throw new ReportingDataException(e.getLocalizedMessage());
          }

          // Reset day-specific pointers for processing next day transactions
          isSameDay = false;
          curCal.setTime(nextCal.getTime());
          curDay = curCal.get(Calendar.DATE);
          // Break out of the loop (to start the next day)
          break;
        }
      }
    }
    // Generate a warning message if data got truncated
    if (isTruncated) {
      this.message = "Data was truncated to " + String.valueOf(MAX_RECORDS) + " records!";
      xLogger.warn("Transaction timeline report: {0}", this.message);
    }

    xLogger.fine("Exiting generateTimelineDataTable");

    // Get the JSON string
    return data;
  }

  // Get a row of the timeine data table
  private TableRow getTimelineTableRow(Calendar cal, int numTrans, String annotation) {
    // Get the date value
    DateValue
        dv =
        new DateValue(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH),
            cal.get(Calendar.DAY_OF_MONTH));
    // Form the table row
    TableRow tr = new TableRow();
    tr.addCell(dv);
    tr.addCell(numTrans);
    tr.addCell(annotation);

    xLogger.fine("getTimelineTableRow: date-value = {0}", dv);

    return tr;
  }

  // Find the number of zero count days and add them
  private void addZeroCountDays(DataTable data, Calendar start, Calendar end)
      throws TypeMismatchException {
    // Ensure that end is greater than start
    if (end.compareTo(start) <= 0) {
      xLogger
          .fine("Invalid start/end dates: End date {0} is lower than start date {1}", end.getTime(),
              start.getTime());
      return;
    }
    // Get the difference between the days
    int days = LocalDateUtil.daysBetweenDates(start, end);
    // If the difference is more than one day, then add zero count rows in descending order of date
    if (days > 1) {
      xLogger.fine("Difference between days {0}-{1} = {2}", start.getTime(), end.getTime(), days);
      // Clone the end date
      Calendar endC = (Calendar) end.clone();
      // Go back 1 day
      endC.add(Calendar.DATE, -1);
      // If this is not the same as start date, then add a table row with zero count
      while (LocalDateUtil.compareDates(start, endC) != 0) {
        data.addRow(getTimelineTableRow(endC, 0, ""));
        endC.add(Calendar.DATE, -1);
      }
    }
  }

  public void finalize() {
    this.results = null;
  }
}
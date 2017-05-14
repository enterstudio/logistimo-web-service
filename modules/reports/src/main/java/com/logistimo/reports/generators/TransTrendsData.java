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
import com.logistimo.inventory.dao.IInvntryDao;
import com.logistimo.inventory.dao.impl.InvntryDao;
import com.logistimo.inventory.entity.IInvntry;
import com.logistimo.inventory.entity.ITransaction;
import com.logistimo.reports.ReportsConstants;
import com.logistimo.reports.entity.slices.Counts;
import com.logistimo.reports.entity.slices.ISlice;

import com.logistimo.reports.entity.slices.IReportsSlice;
import com.logistimo.services.impl.PMF;
import com.logistimo.utils.LocalDateUtil;
import com.logistimo.utils.NumberUtil;
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

import javax.jdo.PersistenceManager;

/**
 * @author Arun
 */
public class TransTrendsData extends ReportData {

  // Column indices
  public static final String DATE = "0";
  // Consumption table
  public static final String C_OPENINGSTOCK = "1";
  public static final String C_ISSUES = "2";
  public static final String C_RECEIPTS = "3";
  public static final String C_WASTAGE = "4";
  public static final String C_STOCK = "5";
  public static final String C_STOCKADJUSTMENT = "6";
  public static final String C_MIN = "7";
  public static final String C_MAX = "8";
  public static final String C_DEMAND = "9";
  public static final String C_TRANSFER = "10";
  // Transaction count table
  public static final String T_TOTAL = "1";
  public static final String T_ISSUES = "2";
  public static final String T_RECEIPTS = "3";
  public static final String T_STOCKCOUNT = "4";
  public static final String T_WASTAGE = "5";
  public static final String T_DEMAND = "6";
  public static final String T_TRANSFER = "7";
  // Stock event response times (average)
  public static final String R_STOCKOUT_MONTHLY_AVERAGE = "1";
  public static final String R_LESSTHANMIN_MONTHLY_AVERAGE = "2";
  public static final String R_MORETHANMAX_MONTHLY_AVERAGE = "3";
  // Order response times
  public static final String O_PROCESSINGTIME_MONTHLY_AVERAGE = "1";
  public static final String O_DELIVERYTIME_MONTHLY_AVERAGE = "2";

  private static final XLog xLogger = XLog.getLog(TransTrendsData.class);

  private String reportType = null;
  private DomainConfig dc = null;

  private IInvntryDao invntryDao = new InvntryDao();

  public TransTrendsData(Date from, Date until, Map<String, Object> filters, Locale locale,
                         String timezone,
                         List<? extends ISlice> results, String cursor, String reportType,
                         DomainConfig dc) {
    super(from, until, filters, locale, timezone, results, cursor);
    this.reportType = reportType;
    this.dc = dc;
  }

  /* (non-Javadoc)
   * @see org.lggi.samaanguru.reports.ReportData#generateDataTable(java.lang.String)
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
    cd.add(new ColumnDescription(DATE, ValueType.DATE, jsMessages.getString("date")));
    String ISSUES = messages.getString("issues");
    String RECEIPTS = messages.getString("receipts");
    // Get configurable column names
    if (dc != null && DomainConfig.TRANSNAMING_SALESPURCHASES.equals(dc.getTransactionNaming())) {
      ISSUES =
          TransactionUtil
              .getDisplayName(ITransaction.TYPE_ISSUE, dc.getTransactionNaming(), locale);
      RECEIPTS =
          TransactionUtil
              .getDisplayName(ITransaction.TYPE_RECEIPT, dc.getTransactionNaming(), locale);
    }
    if (ReportsConstants.TYPE_CONSUMPTION.equals(reportType)) {
      // Quantities
      cd.add(new ColumnDescription(C_OPENINGSTOCK, ValueType.NUMBER,
          messages.getString("openingstock")));
      cd.add(new ColumnDescription(C_ISSUES, ValueType.NUMBER, ISSUES));
      cd.add(new ColumnDescription(C_RECEIPTS, ValueType.NUMBER, RECEIPTS));
      cd.add(new ColumnDescription(C_WASTAGE, ValueType.NUMBER,
          messages.getString("transactions.wastage.upper")));
      cd.add(new ColumnDescription(C_STOCK, ValueType.NUMBER, messages.getString("closingstock")));
      cd.add(new ColumnDescription(C_STOCKADJUSTMENT, ValueType.NUMBER,
          messages.getString("stockadjustment")));
      cd.add(new ColumnDescription(C_MIN, ValueType.NUMBER,
          messages.getString("inventory.reorderlevel")));
      cd.add(new ColumnDescription(C_MAX, ValueType.NUMBER, messages.getString("max")));
      cd.add(new ColumnDescription(C_DEMAND, ValueType.NUMBER, messages.getString("demand")));
      cd.add(new ColumnDescription(C_TRANSFER, ValueType.NUMBER, messages.getString("transfers")));
    } else if (ReportsConstants.TYPE_TRANSACTION.equals(reportType)) {
      // Transaction counts
      cd.add(new ColumnDescription(T_TOTAL, ValueType.NUMBER, jsMessages.getString("total")));
      cd.add(new ColumnDescription(T_ISSUES, ValueType.NUMBER, ISSUES));
      cd.add(new ColumnDescription(T_RECEIPTS, ValueType.NUMBER, RECEIPTS));
      cd.add(new ColumnDescription(T_STOCKCOUNT, ValueType.NUMBER,
          messages.getString("stock") + " " + jsMessages.getString("count") + "s"));
      cd.add(new ColumnDescription(T_WASTAGE, ValueType.NUMBER,
          messages.getString("transactions.wastage.upper")));
      cd.add(new ColumnDescription(T_DEMAND, ValueType.NUMBER, messages.getString("demand")));
      cd.add(new ColumnDescription(T_TRANSFER, ValueType.NUMBER, messages.getString("transfers")));
    } else if (ReportsConstants.TYPE_STOCKEVENTRESPONSETIME.equals(reportType)) {
      // Stock even response times
      String days = messages.getString("days");
      cd.add(new ColumnDescription(R_STOCKOUT_MONTHLY_AVERAGE, ValueType.NUMBER,
          messages.getString("inventory.zerostock") + " - " + days));
      cd.add(new ColumnDescription(R_LESSTHANMIN_MONTHLY_AVERAGE, ValueType.NUMBER,
          messages.getString("inventory.lessthanmin") + " - " + days));
      cd.add(new ColumnDescription(R_MORETHANMAX_MONTHLY_AVERAGE, ValueType.NUMBER,
          messages.getString("inventory.morethanmax") + " - " + days));
    } else if (ReportsConstants.TYPE_ORDERRESPONSETIMES.equals(reportType)) {
      // Stock even response times
      String days = messages.getString("days");
      cd.add(new ColumnDescription(O_PROCESSINGTIME_MONTHLY_AVERAGE, ValueType.NUMBER,
          messages.getString("order.processingtime") + " - " + days));
      cd.add(new ColumnDescription(O_DELIVERYTIME_MONTHLY_AVERAGE, ValueType.NUMBER,
          messages.getString("order.deliveryleadtime") + " - " + days));
    } else {
      xLogger.severe("Unknown report type for trans. trend data: {0}", reportType);
      return null;
    }
    // Add columns to data
    data.addColumns(cd);
    // Add the rows
    Iterator<? extends ISlice> it = results.iterator();
    Calendar prevCal = null;
    Counts counts = new Counts();
    ISlice nextSlice = null;
    if (it.hasNext()) {
      nextSlice = it.next();
    }
    if ((ReportsConstants.TYPE_STOCKEVENTRESPONSETIME.equals(reportType)
        || ReportsConstants.TYPE_ORDERRESPONSETIMES.equals(reportType)) && nextSlice != null
        && ReportsConstants.FREQ_DAILY.equals(nextSlice.getSliceType())) {
      xLogger.severe("Stock events response time report cannot be generated on day slices");
      throw new ReportingDataException(
          "Stock events response time report cannot be generated on day slices");
    }
    PersistenceManager pm = PMF.get().getPersistenceManager();
    try {
      while (nextSlice != null) {
        ISlice slice = nextSlice;
        if (it.hasNext()) {
          nextSlice = it.next();
        } else {
          nextSlice = null;
        }
        Calendar cal = GregorianCalendar.getInstance();
        cal.setTime(slice.getDate());
        TableRow tr = new TableRow();
        try {
          if (ReportsConstants.TYPE_CONSUMPTION.equals(reportType)) {
            if (slice.getDate().getTime() == this.getFromDate().getTime()) {
              continue;
            }
            DateValue
                dv =
                new DateValue(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH),
                    cal.get(Calendar.DAY_OF_MONTH));
            tr.addCell(dv);
            // Get the opening stock
            BigDecimal
                openingStock =
                (nextSlice != null ? nextSlice.getStockQuantity()
                    : BigDecimal.ZERO); // NOTE: data is assumed to be in desc order
            tr.addCell(openingStock.longValue());
            tr.addCell(slice.getIssueQuantity().longValue());
            tr.addCell(slice.getReceiptQuantity().longValue());
            tr.addCell(slice.getWastageQuantity().longValue());
            tr.addCell(slice.getStockQuantity().longValue());
            tr.addCell(slice.getStockDifference().longValue());
            // Add min/max, as needed
            BigDecimal min = BigDecimal.ZERO, max = BigDecimal.ZERO;
            if (ReportsConstants.FILTER_KIOSK.equals(slice.getDimensionType())) {
              try {
                Long kioskId = Long.valueOf(slice.getDimensionValue());
                IInvntry inv = invntryDao.findId(kioskId, Long.valueOf(slice.getObjectId()));
                min = inv.getReorderLevel();
                max = inv.getMaxStock();
              } catch (Exception e) {
                xLogger.warn("{0} when getting kioskId {1} from slices to get inventory: {2}",
                    e.getClass().getName(), slice.getDimensionValue(), e.getMessage());
              }
            }
            tr.addCell(min.longValue());
            tr.addCell(max.longValue());
            tr.addCell(slice.getDemandQuantity().longValue());
            tr.addCell(slice.getTransferQuantity().longValue());
          } else if (ReportsConstants.TYPE_TRANSACTION.equals(reportType)) {
            // Aggregate all material counts for a given day (there will multiple records in a given day, one per material)
            if (prevCal == null || LocalDateUtil.compareDates(cal, prevCal) == 0) {
              aggregateCounts(counts, slice);
              // Advance the previous date pointer
              prevCal = cal;
              // Continue to next record
              continue;
            } else { // create a row for this day
              tr = getCountsRow(prevCal, counts);
              // Reset counts
              counts = new Counts();
              aggregateCounts(counts, slice); // aggregate for next iteration
              // Advance the previous date pointer
              prevCal = cal;
            }
          } else if (ReportsConstants.TYPE_STOCKEVENTRESPONSETIME.equals(reportType)) {
            IReportsSlice monthSlice = (IReportsSlice) slice;
            DateValue
                dv =
                new DateValue(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH),
                    cal.get(Calendar.DAY_OF_MONTH));
            tr.addCell(dv);
            // Stockout response time
            float val = monthSlice.getAverageStockoutResponseTime();
            TableCell cell = new TableCell(val);
            cell.setFormattedValue(NumberUtil.getFormattedValue(val));
            tr.addCell(cell);
            // < min. response time
            val = monthSlice.getAverageLessThanMinResponseTimeAverage();
            cell = new TableCell(val);
            cell.setFormattedValue(NumberUtil.getFormattedValue(val));
            tr.addCell(cell);
            // > max. response time
            val = monthSlice.getAverageMoreThanMaxResponse();
            cell = new TableCell(val);
            cell.setFormattedValue(NumberUtil.getFormattedValue(val));
            tr.addCell(cell);
          } else if (ReportsConstants.TYPE_ORDERRESPONSETIMES.equals(reportType)) {
            IReportsSlice monthSlice = (IReportsSlice) slice;
            DateValue
                dv =
                new DateValue(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH),
                    cal.get(Calendar.DAY_OF_MONTH));
            tr.addCell(dv);
            // Order processing time
            float val = monthSlice.getAverageOrderProcessingTime();
            TableCell cell = new TableCell(val);
            cell.setFormattedValue(NumberUtil.getFormattedValue(val));
            tr.addCell(cell);
            // Order delivery time
            val = monthSlice.getAverageOrderDeliveryTime();
            cell = new TableCell(val);
            cell.setFormattedValue(NumberUtil.getFormattedValue(val));
            tr.addCell(cell);
          }
          // Add row to data
          data.addRow(tr);
        } catch (TypeMismatchException e) {
          xLogger.warn("TypeMismatchException: {0}", e.getLocalizedMessage());
        }
      }
    } finally {
      pm.close();
    }
    // Add last row (only for transaction counts)
    if (ReportsConstants.TYPE_TRANSACTION.equals(reportType)) {
      try {
        TableRow tr = getCountsRow(prevCal, counts);
        data.addRow(tr);
      } catch (TypeMismatchException e) {
        xLogger.warn("TypeMismatchException: {0}", e.getLocalizedMessage());
      }
    }
    xLogger.fine("Exiting generateDataTable");
    return data;
  }

  // Add a row to data
  private TableRow getCountsRow(Calendar cal, Counts counts) {
    DateValue
        dv =
        new DateValue(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH),
            cal.get(Calendar.DAY_OF_MONTH));
    TableRow tr = new TableRow();
    tr.addCell(dv);
    tr.addCell(counts.getTotal());
    tr.addCell(counts.i);
    tr.addCell(counts.r);
    tr.addCell(counts.s);
    tr.addCell(counts.w);
    tr.addCell(counts.o);
    tr.addCell(counts.tr);
    return tr;
  }

  // Aggregate counts from a given slices
  private void aggregateCounts(Counts counts, ISlice tds) {
    counts.i += tds.getIssueCount();
    counts.r += tds.getReceiptCount();
    counts.s += tds.getStockcountCount();
    counts.o += tds.getDemandCount();
    counts.w += tds.getWastageCount();
    counts.tr += tds.getTransferCount();
  }
}

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

/** */
package com.logistimo.reports.generators;

import com.google.visualization.datasource.datatable.DataTable;

import com.logistimo.config.models.DomainConfig;
import com.logistimo.reports.ReportsConstants;
import com.logistimo.reports.entity.slices.ISlice;

import com.logistimo.services.Resources;
import com.logistimo.logger.XLog;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

/**
 * @author arun
 *     <p>Represents the output results of a report query
 */
public abstract class ReportData {


  // Logger
  private static final XLog xLogger = XLog.getLog(ReportsConstants.class);
  // Properties
  protected Date from = null;
  protected Date until = null;
  protected Map<String, Object> filters = null;
  protected Locale locale = null;
  protected String timezone = null;
  protected String message = null;
  // any warning message about data (e.g. "data is truncated to 500 records")
  protected String cursor = null; // pagination cursor pointing to next set of results
  protected ResourceBundle messages = null;
  protected ResourceBundle jsMessages = null;

  @SuppressWarnings("rawtypes")
  protected List results = null;

  protected ISlice slice = null; // required only when trying to a CSV of this view out of a slices

  @SuppressWarnings("rawtypes")
  public ReportData(
          Date from,
          Date until,
          Map<String, Object> filters,
          Locale locale,
          String timezone,
          List results,
          String cursor) {
    this.from = from;
    this.until = until;
    this.filters = filters;
    this.locale = locale;
    this.timezone = timezone;
    this.results = results;
    this.cursor = cursor;
    try {
      this.messages =
          Resources.get()
              .getBundle("Messages", locale); /// Resources.get().getBundle( "Messages", locale );
      this.jsMessages =
          Resources.get().getBundle("JSMessages",
              locale); ///Resources.get().getBundle( "JSMessages", locale );
    } catch (Exception e) {
      xLogger.severe(
              "Exception when loading resource bundle - Messages and JSMessages: {0} : {1}",
          e.getClass().getName(), e.getMessage());
    }
  }

  @SuppressWarnings({"rawtypes", "unchecked"})
  public static ReportData getInstance(
          Long domainId,
          String reportType,
          List results,
          Locale locale,
          String timezone,
          Date from,
          Date to) {
    xLogger.fine("Entering ReportsConstants.getInstance: {0}", reportType);
    if (ReportsConstants.TYPE_TRANSACTION0.equals(reportType)
            || ReportsConstants.TYPE_TRANSACTION_RAW.equals(reportType)) {
      return new TransReportData(null, null, null, locale, timezone, results, null);
    } else if (ReportsConstants.TYPE_STOCK.equals(reportType)) {
      return new StockReportData(null, null, null, locale, timezone, results, null, false);
    } else if (ReportsConstants.TYPE_DEMANDBOARD.equals(reportType)) {
      return new DemandBoardData(null, null, null, locale, timezone, results, null);
    } else if (ReportsConstants.TYPE_ORDERS.equals(reportType)) {
      return new OrdersData(null, null, null, locale, timezone, results, null);
    } else if (ReportsConstants.TYPE_CONSUMPTION.equals(reportType) || ReportsConstants.TYPE_TRANSACTION
        .equals(reportType) ||
        ReportsConstants.TYPE_STOCKEVENTRESPONSETIME.equals(reportType)
        || ReportsConstants.TYPE_ORDERRESPONSETIMES.equals(reportType)) {
      return new TransTrendsData(
              from,
              to,
              null,
              locale,
              timezone,
              results,
              null,
              reportType,
          DomainConfig.getInstance(domainId));
    } else if (ReportsConstants.TYPE_STOCKLEVEL.equals(reportType)) {
      return new StockLevelData(null, null, null, locale, timezone, results, null, BigDecimal.ZERO);
    } else if (ReportsConstants.TYPE_USERACTIVITY.equals(reportType)
            || ReportsConstants.TYPE_DOMAINACTIVITY.equals(reportType)) {
      return new ActivityData(null, null, null, locale, timezone, results, null);
    } else if (ReportsConstants.TYPE_STOCKEVENT.equals(reportType)) {
      return new StockEventData(null, null, null, locale, timezone, results, null);
    } else {
      return null;
    }
  }

  // Get the aggregate report types
  public static List<String> getReportTypes() {
    List<String> types = new ArrayList<String>();
    types.add(ReportsConstants.TYPE_CONSUMPTION);
    types.add(ReportsConstants.TYPE_TRANSACTION);
    return types;
  }

  public Date getFromDate() {
    return from;
  }

  public Date getUntilDate() {
    return until;
  }

  public Map<String, Object> getFilters() {
    return filters;
  }

  public Locale getLocale() {
    return locale;
  }

  public String getTimezone() {
    return timezone;
  }

  public String getMessage() {
    return message;
  }

  public String getCursor() {
    return cursor;
  }

  @SuppressWarnings("rawtypes")
  public List getResults() {
    return results;
  }

  /**
   * Generate the JSON String format of the data table required for Google Visualization.
   * (NOTE: Google visualization uses a non-standard JSON which does not double-quote keys/values, and single-quotes literals)
   *
   * @param A sub-type of the report (e.g. summary, timeline), or pass null to get the default
   * @return A Google visualization DataTable object, representing data to be visualized
   */
  public abstract DataTable generateDataTable(String reportSubType) throws ReportingDataException;

}

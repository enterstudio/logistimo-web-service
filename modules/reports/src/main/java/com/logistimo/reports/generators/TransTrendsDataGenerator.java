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

import com.logistimo.config.models.DomainConfig;
import com.logistimo.dao.JDOUtils;
import com.logistimo.reports.ReportsConstants;
import com.logistimo.reports.entity.slices.IDaySlice;
import com.logistimo.reports.entity.slices.IMonthSlice;
import com.logistimo.reports.entity.slices.ISlice;

import com.logistimo.pagination.PageParams;
import com.logistimo.pagination.QueryParams;
import com.logistimo.services.impl.PMF;
import com.logistimo.utils.LocalDateUtil;
import com.logistimo.utils.QueryUtil;
import com.logistimo.logger.XLog;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.jdo.PersistenceManager;
import javax.jdo.Query;

/**
 * @author Arun
 */
public class TransTrendsDataGenerator implements ReportDataGenerator {

  private static final XLog xLogger = XLog.getLog(TransTrendsDataGenerator.class);
  private String reportType = null;

  public TransTrendsDataGenerator(String reportType) {
    this.reportType = reportType;
  }

  public String getReportType() {
    return reportType;
  }

  /**
   * Get the consumption trends report data
   * NOTE: Only one of the dimension filters will be used, if multiple are passed
   */
  @SuppressWarnings("unchecked")
  @Override
  public ReportData getReportData(Date from, Date until, String frequency,
                                  Map<String, Object> filters, Locale locale, String timezone,
                                  PageParams pageParams, DomainConfig dc, String sourceUserId)
      throws ReportingDataException {

    return new TransTrendsData(from, until, filters, locale, timezone, new ArrayList<ISlice>(1),
        null, reportType, dc);
  }

  @Override
  public QueryParams getReportQuery(Date from, Date until, String frequency,
                                    Map<String, Object> filters, Locale locale, String timezone,
                                    PageParams pageParams, DomainConfig dc, String sourceUserId) {
    if (from == null) {
      throw new IllegalArgumentException("From date has to be specified");
    }
    if (filters == null || filters.isEmpty()) {
      throw new IllegalArgumentException("At least one filter has to be specified");
    }
    return null;
  }
}

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
import com.logistimo.logger.XLog;
import com.logistimo.pagination.PageParams;
import com.logistimo.pagination.QueryParams;
import com.logistimo.reports.entity.slices.ISlice;

import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.Map;

/**
 * Generates user activity data
 *
 * @author Arun
 */
public class ActivityDataGenerator implements ReportDataGenerator {

  private static final XLog xLogger = XLog.getLog(ActivityDataGenerator.class);

  // Get the user activity data; if user ID is given, then get the daily transaction for the user; else, get the monthly data across users in the given period, or the latest month, if no period is given
  @SuppressWarnings({"unchecked", "rawtypes"})
  @Override
  public ReportData getReportData(Date from, Date until, String frequency,
                                  Map<String, Object> filters, Locale locale, String timezone,
                                  PageParams pageParams, DomainConfig dc, String sourceUserId)
      throws ReportingDataException {
    return new ActivityData(from, until, filters, locale, timezone, new ArrayList<ISlice>(1), null);
  }

  @Override
  public QueryParams getReportQuery(Date from, Date until, String frequency,
                                    Map<String, Object> filters, Locale locale, String timezone,
                                    PageParams pageParams, DomainConfig dc, String sourceUserId) {
    xLogger.fine("Entering getReportQuery. filters: {0}", filters);
    if (from == null) {
      throw new IllegalArgumentException("From date has to be specified");
    }
    if (filters == null || filters.isEmpty()) {
      throw new IllegalArgumentException("At least one filter has to be specified");
    }
    return null; //TODO
  }
}

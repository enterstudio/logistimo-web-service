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

import com.logistimo.pagination.PageParams;
import com.logistimo.pagination.QueryParams;

import java.util.Date;
import java.util.Locale;
import java.util.Map;

/**
 * @author Arun
 *         Interface for report generation
 */
public interface ReportDataGenerator {

  /**
   * Get filtered report data for various types of reports
   *
   * @param from    Date from which report is desired
   * @param until   Date until which report is desired
   * @param filters Filters (such as kiosk, materials, or location) for reporting; filter types are defined in Report interface
   * @param userId  The user ID of the user requesting the report
   * @return An instance of the relevant report data, which can be processed further
   * @throws ReportingDataException Thrown if there was error in retrieving data
   */
  ReportData getReportData(Date from, Date until, String frequency, Map<String, Object> filters,
                           Locale locale, String timezone, PageParams pageParams, DomainConfig dc,
                           String sourceUserId) throws ReportingDataException;

  /**
   * Get the report generate query
   */
  QueryParams getReportQuery(Date from, Date until, String frequency, Map<String, Object> filters,
                             Locale locale, String timezone, PageParams pageParams, DomainConfig dc,
                             String sourceUserId);
}

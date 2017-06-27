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

package com.logistimo.reports.service;


import com.logistimo.inventory.models.InvntrySnapshot;
import com.logistimo.reports.generators.IReportDataGeneratorFactory;
import com.logistimo.reports.models.DomainCounts;

import com.logistimo.config.models.DomainConfig;
import com.logistimo.reports.entity.slices.IDomainStats;
import com.logistimo.reports.entity.IReportSchedule;
import com.logistimo.pagination.PageParams;
import com.logistimo.pagination.Results;
import com.logistimo.reports.generators.ReportData;
import com.logistimo.services.Service;
import com.logistimo.services.ServiceException;

import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public interface ReportsService extends Service {

  @SuppressWarnings("unchecked")
  InvntrySnapshot getInventorySnapshot(Long kioskId, Long materialId, Date date,
                                       Long domainId);

  /**
   * Get filtered report data for various types of reports
   *
   * @param reportType The type of report required (e.g. transaction, demand); Types are defined in Report interface
   * @param from       Date from which report is desired
   * @param until      Date until which report is desired
   * @param frequency  whether 'daily' or 'monthly'
   * @param filters    Filters (such as kiosk, materials, or location) for reporting; filter types are defined in Report interface
   * @return An instance of the relevant report data, which can be processed further
   * @throws ServiceException Thrown if there was error in retrieving data
   */
  ReportData getReportData(String reportType, Date from, Date until, String frequency,
                           Map<String, Object> filters, Locale locale, String timezone,
                           PageParams pageParams, DomainConfig dc, String sourceUserId)
      throws ServiceException;

  /**
   * Get JSON string of monthly domain data
   */
  String getMonthlyStatsJSON(Long domainId, int numMonths) throws ServiceException;

  /**
   * Get Usage Stats across domains
   */
  Results getUsageStatsAcrossDomains(Date start, PageParams pageParams) throws ServiceException;

  /**
   * Get Usage Stats for a domain
   */
  Results getMonthlyUsageStatsForDomain(Long domainId, Date start, PageParams pageParams)
      throws ServiceException;

  /**
   * Get Usage Stats for a Kiosk
   */
  Results getMonthlyUsageStatsForKiosk(Long domainId, Long entityId, Date start)
      throws ServiceException;

  /**
   * Get DomainCounts
   */
  DomainCounts getDomainCounts(Long domainId, Date endDate, int period, String periodType,
                               String mTag, String matId) throws ServiceException;

  /**
   * Get Slices
   */
  Results getSlices(Date startDate, Date endDate, String periodType, String oty, String oId,
                    String dt, String dv, boolean fillMissingSlices, PageParams pageParams)
      throws ServiceException;

  Results getSlices(Date endDate, String periodType, String oty, String oId, String dt, String dv,
                    boolean fillMissingSlices, PageParams pageParams) throws ServiceException;

  IReportSchedule getReportSchedule(Long domainId) throws Exception;

  void updateReportHours(Long domainId, Date date) throws Exception;

  String getRepGenTime(Long domainId, Locale locale, String timezone) throws ServiceException;

  List<? extends IDomainStats> getDomainStatistics(Long domainId);

  Map<String, String> getDomainStatisticsByTag(Long domainId, String tag, String c);

  /**
   * Gets the last run time of the report
   *
   * @param appName - Application name registered in Analytics
   */
  String getReportLastRunTime(String appName);

  /**
   * Gets the respective ReportDataGeneratorFactory implementation
   * @return
   */
  IReportDataGeneratorFactory getReportDataGeneratorFactory();
}

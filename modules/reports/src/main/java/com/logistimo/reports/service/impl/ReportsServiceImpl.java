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
package com.logistimo.reports.service.impl;

import com.logistimo.AppFactory;
import com.logistimo.config.models.DomainConfig;
import com.logistimo.constants.CharacterConstants;
import com.logistimo.inventory.models.InvntrySnapshot;
import com.logistimo.logger.XLog;
import com.logistimo.pagination.PageParams;
import com.logistimo.pagination.Results;
import com.logistimo.reports.entity.IReportSchedule;
import com.logistimo.reports.entity.ReportSchedule;
import com.logistimo.reports.entity.slices.IDomainStats;
import com.logistimo.reports.entity.slices.ISlice;
import com.logistimo.reports.generators.IReportDataGeneratorFactory;
import com.logistimo.reports.generators.ReportData;
import com.logistimo.reports.generators.ReportDataGenerator;
import com.logistimo.reports.generators.ReportDataGeneratorFactory;
import com.logistimo.reports.models.DomainCounts;
import com.logistimo.reports.service.ReportsService;
import com.logistimo.services.Service;
import com.logistimo.services.ServiceException;
import com.logistimo.services.Services;
import com.logistimo.services.cache.MemcacheService;
import com.logistimo.services.impl.ServiceImpl;
import com.logistimo.utils.LocalDateUtil;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * @author arun
 */
public class ReportsServiceImpl extends ServiceImpl implements ReportsService {

  private static final XLog xLogger = XLog.getLog(ReportsServiceImpl.class);

  private IReportDataGeneratorFactory reportDataGeneratorFactory = new ReportDataGeneratorFactory();

  @Override
  public InvntrySnapshot getInventorySnapshot(Long kioskId, Long materialId, Date date,
                                              Long domainId) {
    throw new UnsupportedOperationException("Reports modules does not support inventory snapshot");
  }

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
  public ReportData getReportData(String reportType, Date from, Date until, String frequency,
                                  Map<String, Object> filters, Locale locale, String timezone,
                                  PageParams pageParams, DomainConfig dc, String sourceUserId)
      throws ServiceException {
    try {
      ReportDataGenerator rdg = reportDataGeneratorFactory.getInstance(reportType);
      return rdg.getReportData(from, until, frequency, filters, locale, timezone, pageParams, dc,
          sourceUserId);
    } catch (Exception e) {
      // Added this block because getReportData does not throw ReportingDataException in StockEventDataGenerator.
      throw new ServiceException(e.getMessage());
    }
  }

  public void destroy() throws ServiceException {
    xLogger.fine("Entering destroy");
    // TODO Auto-generated method stub
    xLogger.fine("Exiting destroy");
  }

  public Class<? extends Service> getInterface() {
    xLogger.fine("Entering getInterface");
    xLogger.fine("Exiting getInterface");
    return ReportsServiceImpl.class;
  }

  public void init(Services services) throws ServiceException {
    xLogger.fine("Entering init");
    // TODO Auto-generated method stub
    xLogger.fine("Exiting init");
  }

  /**
   * Get JSON string of monthly domain data (specify up to a max of 100 months)
   */
  @SuppressWarnings("unchecked")
  @Override
  public String getMonthlyStatsJSON(Long domainId, int numMonths) throws ServiceException {
    return "";
  }

  /**
   * Get Usage Stats across domains
   */
  @SuppressWarnings("unchecked")
  public Results getUsageStatsAcrossDomains(Date start, PageParams pageParams)
      throws ServiceException {
    return new Results(new ArrayList<ISlice>(1),null);
  }

  // Returns monthly statistics for a domain given start date and the number of months
  // Query the MonthSlice table for oty == Slice.OTYPE_DOMAIn, oid == domainId, dt == ReportsConstants.FILTER_DOMAIN, dv == domainId, d >= startDate
  @SuppressWarnings("unchecked")
  public Results getMonthlyUsageStatsForDomain(Long domainId, Date start, PageParams pageParams)
      throws ServiceException {
    return new Results(new ArrayList<ISlice>(1),null);
  }

  // Returns monthly statistics for a domain given start date and the number of months
  // Query the MonthSlice table for oty == Slice.OTYPE_DOMAIn, oid == kioskId, dt == ReportsConstants.FILTER_KIOSK, dv == kioskId, d >= startDate
  @SuppressWarnings("unchecked")
  public Results getMonthlyUsageStatsForKiosk(Long domainId, Long kioskId, Date start)
      throws ServiceException {
    return new Results(new ArrayList<ISlice>(1),null);
  }

  /**
   * Method that returns Slices for a domain, between startDate and endDate where endDate = startDate - historicalPeriod. The type of slices returned are DaySlice or MonthSlice
   * depending on the periodType. The results include missing slices also( which are initialized from the previous slices )
   */
  @SuppressWarnings({"unchecked"})
  public DomainCounts getDomainCounts(Long domainId, Date endDate, int period, String periodType,
                                      String mTag, String matId) throws ServiceException {
    return new DomainCounts(domainId);
  }

  @SuppressWarnings("unchecked")
  public Results getSlices(Date stDate, Date endDate, String periodType, String oty, String oId,
                           String dt, String dv, boolean fillMissingSlices, PageParams pageParams)
      throws ServiceException {
    xLogger.info("Entering getSlices");
    // If any of the parameters are null or empty, throw an IllegalArgumentException
    if (endDate == null || periodType == null || periodType.isEmpty() || oty == null || oty
        .isEmpty() || oId == null || oId.isEmpty() || dt == null || dt.isEmpty() || dv == null || dv
        .isEmpty() || pageParams == null) {
      xLogger.severe(
          "One or more mandatory parameters are null or empty endDate: {0}, periodType: {1}, oty: {2}, oId: {3}, dt: {4}, dv: {5}, pageParams: {6}",
          endDate, periodType, oty, oId, dt, dv, pageParams);
      throw new IllegalArgumentException();
    }

    return new Results(new ArrayList<ISlice>(1), null);
  }

  public Results getSlices(Date endDate, String periodType, String oty, String oId, String dt,
                           String dv, boolean fillMissingSlices, PageParams pageParams)
      throws ServiceException {
    return getSlices(null, endDate, periodType, oty, oId, dt, dv, fillMissingSlices, pageParams);
  }

  public String getRepGenTime(Long domainId, Locale locale, String timezone) {
    try {
      MemcacheService cache = AppFactory.get().getMemcacheService();
      Date curDT = (Date) cache.get("RLU_" + domainId);
      if (curDT == null) {
        curDT = getReportSchedule(domainId).getLastUpdate();

        cache.put("RLU_" + domainId, curDT);
      }
      return LocalDateUtil.format(curDT, locale, timezone);
    } catch (Exception e) {
      xLogger.warn("Error in getting Report last update time for domain {0}:{1}", domainId,
          e.getMessage());
    }
    return CharacterConstants.EMPTY;
  }

  @Override
  public IReportSchedule getReportSchedule(Long domainId) throws Exception {
    return new ReportSchedule();
  }

  @Override
  public void updateReportHours(Long domainId, Date date) throws Exception {
    //do nothing
  }

  /**
   * @param domainId get domain statistics data
   */

  @Override
  public List<? extends IDomainStats> getDomainStatistics(Long domainId) {
    return null;
  }

  /**
   * @param domainId get domain statistics data by tags
   */

  @Override
  public Map<String, String> getDomainStatisticsByTag(Long domainId, String tag, String c) {
    return null;
  }

  @Override
  public String getReportLastRunTime(String appName) {
    return null;
  }

  @Override
  public IReportDataGeneratorFactory getReportDataGeneratorFactory() {
    return reportDataGeneratorFactory;
  }
}

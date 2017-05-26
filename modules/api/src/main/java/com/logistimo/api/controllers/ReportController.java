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

package com.logistimo.api.controllers;

import com.logistimo.api.auth.Authoriser;
import com.logistimo.domains.entity.IDomainLink;
import com.logistimo.domains.service.DomainsService;
import com.logistimo.domains.service.impl.DomainsServiceImpl;
import com.logistimo.reports.ReportsConstants;
import com.logistimo.reports.service.ReportsService;
import com.logistimo.services.utils.ConfigUtil;

import org.apache.commons.lang.StringUtils;
import com.logistimo.config.models.DomainConfig;
import com.logistimo.reports.entity.slices.IDomainStats;
import com.logistimo.reports.generators.ReportData;
import com.logistimo.security.SecureUserDetails;
import com.logistimo.services.Resources;
import com.logistimo.services.ServiceException;
import com.logistimo.services.Services;
import com.logistimo.constants.Constants;
import com.logistimo.utils.LocalDateUtil;
import com.logistimo.api.util.SessionMgr;
import com.logistimo.logger.XLog;
import com.logistimo.api.builders.DomainBuilder;
import com.logistimo.api.builders.DomainStatisticsBuilder;
import com.logistimo.api.builders.FChartBuilder;
import com.logistimo.exception.BadRequestException;
import com.logistimo.exception.InvalidServiceException;
import com.logistimo.exception.UnauthorizedException;
import com.logistimo.api.models.DomainStatisticsModel;
import com.logistimo.api.models.FChartModel;
import com.logistimo.api.request.FusionChartRequest;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import static com.logistimo.api.security.SecurityMgr.getUserDetails;

/**
 * Created by Mohan Raja on 30/01/15
 */

@Controller
@RequestMapping("/report")
public class ReportController {

  private static final XLog xLogger = XLog.getLog(ReportController.class);

  @RequestMapping(value = "/fchartdata", method = RequestMethod.POST)
  public
  @ResponseBody
  List<FChartModel> getFChartData(@RequestBody FusionChartRequest fcRequest,
                                  HttpServletRequest request) {
    SecureUserDetails sUser = getUserDetails(request.getSession());
    String userId = sUser.getUsername();
    Locale locale = sUser.getLocale();
    ResourceBundle backendMessages = Resources.get().getBundle("BackendMessages", locale);
    String timezone = sUser.getTimezone();
    Long domainId = SessionMgr.getCurrentDomain(request.getSession(), userId);
    Map<String, Object> filters = getFilters(fcRequest, domainId);
    DomainConfig dc = DomainConfig.getInstance(domainId);
    ReportsService rs;
    try {
      rs = Services.getService("reports",locale);
      SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
      if (fcRequest.daily) {
        // The fcRequest.stDate is set to the the first day of the month for which the report is being drilled down.
        // Set the fcRequest.enDate to the first day of the next month before sending request to the backend.
        Calendar d = new GregorianCalendar();
        d.setTime(sdf.parse(fcRequest.stDate));
        if (ReportsConstants.TYPE_CONSUMPTION.equals(fcRequest.rty)) {
          d.add(Calendar.DAY_OF_MONTH, -1);
          fcRequest.stDate = sdf.format(d.getTime());
          d.add(Calendar.DAY_OF_MONTH, 1);
        }
        d.add(Calendar.MONTH, 1);
        d.set(Calendar.DAY_OF_MONTH, 1);
        fcRequest.enDate = sdf.format(d.getTime());
      } else {
        // The fcRequest.enDate comes in as the last day of the selected month in the UI.
        // It should be set to the should be set to first day of next month before sending request to the backend.
        Calendar d = new GregorianCalendar();
        if (ReportsConstants.TYPE_CONSUMPTION.equals(fcRequest.rty)) {
          d.setTime(sdf.parse(fcRequest.stDate));
          d.add(Calendar.MONTH, -1);
          fcRequest.stDate = sdf.format(d.getTime());
        }
        d.setTime(sdf.parse(fcRequest.enDate));
        d.add(Calendar.DATE, 1);
        fcRequest.enDate = sdf.format(d.getTime());
      }

      ReportData
          r =
          rs.getReportData(fcRequest.rty, sdf.parse(fcRequest.stDate), sdf.parse(fcRequest.enDate),
              fcRequest.freq, filters, locale, timezone, null, dc, userId);
      String repGenTime = rs.getRepGenTime(domainId, locale, timezone);
      if (r.getResults() == null) {
        return new ArrayList<>(0);
      }
      switch (fcRequest.rty) {
        case ReportsConstants.TYPE_CONSUMPTION:
          return new FChartBuilder().buildConsumptionChartModel(r, repGenTime);
        case ReportsConstants.TYPE_ORDERRESPONSETIMES:
          return new FChartBuilder().buildORTChartModel(r, repGenTime);
        case ReportsConstants.TYPE_STOCKEVENTRESPONSETIME:
          return new FChartBuilder().buildRRTChartModel(r, repGenTime);
        case ReportsConstants.TYPE_TRANSACTION:
          return new FChartBuilder().buildTCChartModel(r, repGenTime);
        case ReportsConstants.TYPE_USERACTIVITY:
          return new FChartBuilder().buildUAChartModel(r, repGenTime);
        default:
          xLogger.warn("invalid report type found while fetching report data: " + fcRequest.rty);
          throw new BadRequestException(backendMessages.getString("chart.data.fetch.error"));
      }
    } catch (ServiceException | ParseException e) {
      xLogger.severe(backendMessages.getString("chart.data.fetch.error"), e);
      throw new InvalidServiceException(backendMessages.getString("chart.data.fetch.error"));
    }
  }


  private Map<String, Object> getFilters(FusionChartRequest fcRequest, Long domainId) {
    Map<String, Object> filters = new HashMap<String, Object>();
    filters.put(ReportsConstants.FILTER_DOMAIN, domainId);
    if (fcRequest.eid != null) {
      filters.put(ReportsConstants.FILTER_KIOSK, fcRequest.eid);
    }
    if (fcRequest.mid != null) {
      filters.put(ReportsConstants.FILTER_MATERIAL, fcRequest.mid);
    }
    if (StringUtils.isNotBlank(fcRequest.st)) {
      filters.put(ReportsConstants.FILTER_STATE, fcRequest.st);
    }
    if (StringUtils.isNotBlank(fcRequest.dis)) {
      filters.put(ReportsConstants.FILTER_DISTRICT, fcRequest.dis);
    }
    if (fcRequest.egrp != null) {
      filters.put(ReportsConstants.FILTER_POOLGROUP, fcRequest.egrp);
    }
    if (StringUtils.isNotBlank(fcRequest.uid)) {
      filters.put(ReportsConstants.FILTER_USER, fcRequest.uid);
    }
    if (StringUtils.isNotBlank(fcRequest.mtag)) {
      filters.put(ReportsConstants.FILTER_MATERIALTAG, fcRequest.mtag);
    }
    if (StringUtils.isNotBlank(fcRequest.etag)) {
      filters.put(ReportsConstants.FILTER_KIOSKTAG, fcRequest.etag);
    }
    if (StringUtils.isNotBlank(fcRequest.mtag)) {
      filters.put(ReportsConstants.FILTER_MATERIALTAG, fcRequest.mtag);
    }
        /*if (filters.containsKey(ReportsConstants.FILTER_MATERIAL) && filters.size() >= 3) {
            filters.remove(ReportsConstants.FILTER_DOMAIN);
        }*/
    return filters;
  }

  @RequestMapping(value = "/domainstats", method = RequestMethod.GET)
  public
  @ResponseBody
  DomainStatisticsModel getDomainStatistics(HttpServletRequest request, Long domainId) {
    SecureUserDetails sUser = getUserDetails(request.getSession());
    String userId = sUser.getUsername();
    DomainStatisticsModel domainStatisticsModel = null;
    try {
      if (!Authoriser.authoriseUser(request, userId)) {
        throw new UnauthorizedException("Permission denied of the user to access this domain");
      }
      domainId =
          (domainId != null ? domainId : SessionMgr.getCurrentDomain(request.getSession(), userId));

      ReportsService rs = Services.getService("reports");
      List<IDomainLink> linkedDomains;
      Set<Long> domains;
      List<? extends IDomainStats> parentData;
      List<IDomainStats> childrenData = new ArrayList<>();
      DomainBuilder domainBuilder = new DomainBuilder();
      DomainStatisticsBuilder domainStatisticsBuilder = new DomainStatisticsBuilder();
      String domainStatsApp = ConfigUtil.get(ReportsConstants.MASTER_DATA_APP_NAME);
      parentData = rs.getDomainStatistics(domainId);
      if (parentData != null && parentData.size() > 0) {
        domainStatisticsModel = domainStatisticsBuilder.buildParentModel(parentData.get(0));
        Date date = LocalDateUtil.parseCustom(
            rs.getReportLastRunTime(domainStatsApp), Constants.ANALYTICS_DATE_FORMAT, null);
        domainStatisticsModel.lrt = LocalDateUtil.format(date, sUser.getLocale(),
            sUser.getTimezone());
      }
      DomainsService ds = Services.getService(DomainsServiceImpl.class);
      linkedDomains = ds.getDomainLinks(domainId, 0, 0);
      domains = domainBuilder.buildChildDomainsList(linkedDomains);
      if (domains != null) {
        for (Long d : domains) {
          List<IDomainStats> domainStats = (List<IDomainStats>) rs.getDomainStatistics(d);
          if (domainStats != null && domainStats.size() > 0) {
            childrenData.add(rs.getDomainStatistics(d).get(0));
          }
        }
        if (childrenData.size() > 0) {
          domainStatisticsModel =
              domainStatisticsBuilder
                  .buildChildModel(childrenData, domainStatisticsModel, sUser.getLocale(),
                      sUser.getTimezone());
        }
      }
    } catch (ServiceException | ParseException e) {
      xLogger
          .severe("Exception while fetching domain statistics details for the domain {0}",
              domainId,
              e);
      xLogger.severe(
          "Invalid service exception while fetching domain level statistics for the domain {0} for user {1} ",
          domainId, userId);
    }
    return domainStatisticsModel;
  }

  @RequestMapping(value = "/domainstats/tag", method = RequestMethod.GET)
  public
  @ResponseBody
  Map<String, String> getDomainStatisticsByTag(HttpServletRequest request, Long domainId,
                                               String tag, String c) {
    SecureUserDetails sUser = getUserDetails(request.getSession());
    String userId = sUser.getUsername();
    Map<String, String> results;
    try {
      if (!Authoriser.authoriseUser(request, userId)) {
        throw new UnauthorizedException("Permission denied of the user to access this domain");
      }
      domainId =
          (domainId != null ? domainId : SessionMgr.getCurrentDomain(request.getSession(), userId));
      if (StringUtils.isBlank(tag)) {
        return null;
      }

      ReportsService rs = Services.getService("reports");
      results = rs.getDomainStatisticsByTag(domainId, tag, c);
    } catch (ServiceException e) {
      xLogger
          .severe(
              "Exception while fetching domain statistics details by tags for the domain {0}",
              domainId, e.getMessage());
      throw new InvalidServiceException("Unable to fetch domain statistics data for the domain");
    }
    return results;
  }


}

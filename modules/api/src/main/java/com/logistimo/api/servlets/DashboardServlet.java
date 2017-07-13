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

package com.logistimo.api.servlets;

import com.logistimo.api.servlets.mobile.json.JsonOutput;
import com.logistimo.api.servlets.mobile.json.Kiosks;
import com.logistimo.api.util.RESTUtil;
import com.logistimo.auth.SecurityConstants;
import com.logistimo.auth.SecurityMgr;
import com.logistimo.auth.SecurityUtil;
import com.logistimo.auth.utils.SessionMgr;
import com.logistimo.constants.Constants;
import com.logistimo.entities.service.EntitiesService;
import com.logistimo.entities.service.EntitiesServiceImpl;
import com.logistimo.logger.XLog;
import com.logistimo.pagination.PageParams;
import com.logistimo.pagination.Results;
import com.logistimo.proto.RestConstantsZ;
import com.logistimo.reports.entity.slices.IMonthSlice;
import com.logistimo.reports.entity.slices.ISlice;
import com.logistimo.reports.models.DomainCounts;
import com.logistimo.reports.models.UsageStats;
import com.logistimo.reports.service.ReportsService;
import com.logistimo.security.SecureUserDetails;
import com.logistimo.services.ServiceException;
import com.logistimo.services.Services;
import com.logistimo.users.entity.IUserAccount;
import com.logistimo.utils.Counter;
import com.logistimo.utils.LocalDateUtil;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.ResourceBundle;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

public class DashboardServlet extends JsonRestServlet {

  private static final long serialVersionUID = 1L;

  private static final XLog xLogger = XLog.getLog(DashboardServlet.class);
  // Actions
  private static final String ACTION_GETKIOSKS = "getkiosks";
  private static final String GETMONTHLYSTATS = "getmonthlystats";
  private static final String
      ACTION_GETMONTHLYUSAGESTATSACROSSDOMAINS =
      "getmonthlyusagestatsacrossdomains";
  private static final String
      ACTION_GETMONTHLYUSAGESTATSFORDOMAIN =
      "getmonthlyusagestatsfordomain";
  private static final String ACTION_GETDOMAINCOUNTS = "getdomaincounts";

  // Get monthly stats. for dashboard display
  private static void getMonthlyStats(HttpServletRequest request, HttpServletResponse response,
                                      ResourceBundle backendMessages, ResourceBundle messages)
      throws IOException {
    xLogger.fine("Entered getMonthlyStats");
    // Get the domain ID
    HttpSession session = request.getSession();
    SecureUserDetails sUser = SecurityMgr.getUserDetails(session);
    String userId = sUser.getUsername();
    Long domainId = SessionMgr.getCurrentDomain(session, userId);
    try {
      // Get number of months
      String monthsStr = request.getParameter("months");
      int months = 2; // default
      if (monthsStr != null && !monthsStr.isEmpty()) {
        try {
          months = Integer.parseInt(monthsStr);
        } catch (Exception e) {
          // ignore; return default months data
        }
      }
      ReportsService svc = Services.getService("reports");
      String json = svc.getMonthlyStatsJSON(domainId, months);
      writeText(response, json);
    } catch (Exception e) {
      xLogger
          .severe("{0} when trying to get monthly stats in domain {1}: {2}", e.getClass().getName(),
              domainId, e.getMessage());
      response.setStatus(500);
    }
    xLogger.fine("Exiting getMonthlyStats");
  }

  // Get monthly stats. for dashboard display
  @SuppressWarnings("unchecked")
  private static void getMonthlyUsageStatsForDomain(HttpServletRequest request,
                                                    HttpServletResponse response,
                                                    ResourceBundle backendMessages,
                                                    ResourceBundle messages) throws IOException {
    xLogger.fine("Entered getMonthlyUsageStatsForDomain");
    // Get the domain ID
    String domainIdStr = request.getParameter("domainid");
    String offsetStr = request.getParameter("offset");
    String sizeStr = request.getParameter("size");
    String startDateStr = request.getParameter("startdate");
    if (sizeStr == null || sizeStr.isEmpty() || startDateStr == null || startDateStr.isEmpty()
        || domainIdStr == null || domainIdStr.isEmpty()) {
      xLogger.severe(
          "One or more manadatory parameters or null or empty. offsetStr: {0}, sizeStr: {1}, startDateStr: {2}, domainIdStr: {3}",
          offsetStr, sizeStr, startDateStr, domainIdStr);
      response.setStatus(500);
      return;
    }
    Long domainId = null;
    Date startDate = null;
    int offset = 0;
    try {
      domainId = Long.parseLong(domainIdStr);
      SimpleDateFormat df = new SimpleDateFormat(Constants.DATE_FORMAT);
      startDate = df.parse(startDateStr);
      int size = Integer.parseInt(sizeStr);
      if (offsetStr != null) {
        offset = Integer.parseInt(offsetStr);
      }

      // Proceed only if the mandatory attributes are present.
      ReportsService rs = Services.getService("reports");
      PageParams pageParams = new PageParams(null, offset, size);
      Results results = rs.getMonthlyUsageStatsForDomain(domainId, startDate, pageParams);
      List<IMonthSlice> resultsList = results.getResults();
      if (resultsList != null && !resultsList.isEmpty()) {
        // From the List<MonthSlice> get UsageStats object
        UsageStats usageStats = new UsageStats(results);
        xLogger.info("usageStats: " + usageStats.toJSONString());
        // Convert the usageStats object to JSON and return it.
        writeText(response, usageStats.toJSONString());
      } else {
        xLogger.info("No results: {0}", resultsList);
        writeText(response, "{\"msg\": \"No results\" }");
      }
    } catch (Exception e) {
      xLogger.severe("{0} when trying to get monthly usage stats for domain {1}. Message: {2}",
          e.getClass().getName(), domainId, e.getMessage());
      response.setStatus(500);
    }
    xLogger.fine("Exiting getMonthlyUsageStatsForDomain");
  }

  // Get counts for a domain
  private static void getDomainCounts(HttpServletRequest request, HttpServletResponse response,
                                      ResourceBundle backendMessages, ResourceBundle messages)
      throws IOException {
    xLogger.fine("Entered getDomainCounts");
    // Read the request parameters. All parameters are mandatory
    String domainIdStr = request.getParameter("domainid");
    String endDateStr = request.getParameter("enddate");
    String periodStr = request.getParameter("period");
    String periodTypeStr = request.getParameter("periodtype");

    if (domainIdStr == null || domainIdStr.isEmpty() || endDateStr == null || endDateStr.isEmpty()
        || periodStr == null || periodStr.isEmpty() || periodTypeStr == null || periodTypeStr
        .isEmpty()) {
      xLogger.severe(
          "One or more mandatory parameters are null or empty. domainIdStr: {0}, endDateStr: {1}, periodStr: {2}",
          domainIdStr, endDateStr, periodStr);
      response.setStatus(500);
      return;
    }

    int period;
    Long domainId = null;
    Date endDate = null;
    Date startDate = null;
    try {
      period = Integer.parseInt(periodStr);
      domainId = Long.parseLong(domainIdStr);
      SimpleDateFormat df = new SimpleDateFormat(Constants.DATE_FORMAT);
      endDate = df.parse(endDateStr);

      // startDate is endDate - historicalPeriod (months or days depending on the periodType)
      Calendar cal = GregorianCalendar.getInstance();
      cal.setTime(endDate);
      LocalDateUtil.resetTimeFields(cal);

      if (ISlice.MONTHLY.equals(periodTypeStr)) {
        cal.add(Calendar.MONTH, -1 * period);
      } else if (ISlice.DAILY.equals(periodTypeStr)) {
        cal.add(Calendar.DATE, -1 * period);
      }
      startDate = cal.getTime();
      xLogger.info("startDate: {0}, endDate: {1}", startDate.toString(), endDate.toString());
      // Call ReportsService API to get domain counts
      ReportsService rs = Services.getService("reports");
      DomainCounts
          domainCounts =
          rs.getDomainCounts(domainId, endDate, period, periodTypeStr, null, null);
      xLogger.fine("domainCounts.toJSONString(): {0}", domainCounts.toJSONString());
      // Convert the domainCounts object to JSON and return it to the caller.
      writeText(response, domainCounts
          .toJSONString()); // If no data was available, the domainCounts.toJSONString() will be an empty JSON - {}
    } catch (Exception e) {
      // Return a status 500 response in case there is any error while getting domain counts
      xLogger.severe("{0} when trying to get domain counts for domain {1}. Message: {2}",
          e.getClass().getName(), domainId, e.getMessage());
      response.setStatus(500);
    }
    xLogger.fine("Exiting getDomainCounts");
  }

  @Override
  protected void processGet(HttpServletRequest request,
                            HttpServletResponse response, ResourceBundle backendMessages,
                            ResourceBundle messages) throws ServletException, IOException,
      ServiceException {
    xLogger.fine("Entered processGet");
    String action = request.getParameter("action");
    if (ACTION_GETKIOSKS.equals(action)) {
      getKiosks(request, response, backendMessages, messages);
    } else if (GETMONTHLYSTATS.equals(action)) {
      getMonthlyStats(request, response, backendMessages, messages);
    } else if (ACTION_GETMONTHLYUSAGESTATSACROSSDOMAINS.equals(action)) {
      getMonthlyUsageStatsAcrossDomains(request, response, backendMessages, messages);
    } else if (ACTION_GETMONTHLYUSAGESTATSFORDOMAIN.equals(action)) {
      getMonthlyUsageStatsForDomain(request, response, backendMessages, messages);
    } else if (ACTION_GETDOMAINCOUNTS.equals(action)) {
      getDomainCounts(request, response, backendMessages, messages);
    } else {
      try {
        String message = "Invalid action: " + action;
        JsonOutput json = new JsonOutput(JsonOutput.VERSION_DEFAULT, false, message);
        sendJsonResponse(response, 200, json.toJSONString());
        xLogger.severe(message);
      } catch (IOException e) {
        xLogger.severe("IOException when sending JSON: {0}", e.getMessage());
      }
    }

    xLogger.fine("Exiting processGet");
  }

  @Override
  protected void processPost(HttpServletRequest request,
                             HttpServletResponse response, ResourceBundle backendMessages,
                             ResourceBundle messages) throws ServletException, IOException,
      ServiceException {
    processGet(request, response, backendMessages, messages);
  }

  // Get the stock view dashboard
  private void getKiosks(HttpServletRequest request, HttpServletResponse response,
                         ResourceBundle backendMessages, ResourceBundle messages)
      throws ServiceException {
    xLogger.fine("Entered getKiosks");
    // Get the parameters
    String userId = request.getParameter(RestConstantsZ.USER_ID);
    String password = request.getParameter(RestConstantsZ.PASSWORD);
    String sizeStr = request.getParameter(RestConstantsZ.SIZE);
    String cursor = request.getParameter(RestConstantsZ.CURSOR);
    // Get pagination params
    int size = PageParams.DEFAULT_SIZE;
    if (sizeStr != null && !sizeStr.isEmpty()) {
      size = Integer.parseInt(sizeStr);
    }
    PageParams pageParams = new PageParams(cursor, size);
    // Authenticate user
    String jsonOutput = null;
    try {
      IUserAccount u = RESTUtil.authenticate(userId, password, null, request, response);
      Long domainId = null;
      // Get domain ID of logged-in user
      SecureUserDetails sUser = SecurityMgr.getUserDetails(request.getSession());
      if (sUser != null) {
        domainId = SessionMgr.getCurrentDomain(request.getSession(), sUser.getUsername());
      }
      Results results = null;
      EntitiesService as = Services.getService(EntitiesServiceImpl.class);
      if (SecurityUtil.compareRoles(u.getRole(), SecurityConstants.ROLE_DOMAINOWNER) >= 0) {
        results = as.getAllKiosks(domainId, null, null, pageParams);
      } else {
        results = as.getKiosksForUser(u, null, pageParams);
      }
      int totalKiosks = Counter.getKioskCounter(domainId).getCount();
      Kiosks kiosksJson = new Kiosks(JsonOutput.VERSION_DEFAULT, true, null, results, totalKiosks);
      jsonOutput = kiosksJson.toJSONString();
    } catch (Exception e) {
      xLogger.severe("{0} when getting kiosks JSON: {1}", e.getClass().getName(), e.getMessage());
      jsonOutput = new JsonOutput(JsonOutput.VERSION_DEFAULT, false, e.getMessage()).toJSONString();
    }
    // Send output
    try {
      sendJsonResponse(response, 200, jsonOutput);
    } catch (IOException e) {
      xLogger.severe("IOException when sending JSON: {0}", e.getMessage());
    }
    xLogger.fine("Exiting getKiosks");
  }

  // Get the monthly statistics across all domains
  @SuppressWarnings("unchecked")
  private void getMonthlyUsageStatsAcrossDomains(HttpServletRequest request,
                                                 HttpServletResponse response,
                                                 ResourceBundle backendMessages,
                                                 ResourceBundle messages) throws ServiceException {
    xLogger.fine("Entering getMonthlyUsageStatsAcrossDomains");
    // Read the cursor, size, start date (1st of months)
    // Query the MonthSlice table - get all month slices with oty = 'domain' and dt = 'domain' and d > (1st of month - 1 millisec) and order by tc desc (cursor and size is set)
    // Iterate through the MonthSlice objects and get the DomainDashboardStats for every domain
    // Create a UsageStats object and populate it with the DomainUsageStats objects
    // Return a Json representation of UsageStats object.
    String offsetStr = request.getParameter("offset");
    String sizeStr = request.getParameter("size");
    String startDateStr = request.getParameter("startdate");
    if (sizeStr == null || sizeStr.isEmpty() || startDateStr == null || startDateStr.isEmpty()) {
      xLogger.severe(
          "One or more manadatory parameters or null or empty. offsetStr: {0}, sizeStr: {1}, dateStr: {2}",
          offsetStr, sizeStr, startDateStr);
      response.setStatus(500);
    }
    int offset = 0;
    try {
      int size = Integer.parseInt(sizeStr);
      if (offsetStr != null) {
        offset = Integer.parseInt(offsetStr);
      }
      // Get the start and end dates
      SimpleDateFormat df = new SimpleDateFormat(Constants.DATE_FORMAT);
      Date startDate = null;
      startDate = df.parse(startDateStr);
      // Proceed only if the mandatory attributes are present.
      ReportsService rs = Services.getService("reports");
      PageParams pageParams = new PageParams(null, offset, size);
      Results results = rs.getUsageStatsAcrossDomains(startDate, pageParams);
      List<IMonthSlice> resultsList = results.getResults();
      if (resultsList != null && !resultsList.isEmpty()) {
        // From the List<MonthSlice> get UsageStats object
        UsageStats usageStats = new UsageStats(results);
        xLogger.fine("usageStats.domainUsageStatsList: {0}",
            (usageStats == null || usageStats.getDomainUsageStatsList() == null ? "NULL"
                : usageStats.getDomainUsageStatsList().toString()));
        xLogger.fine("usageStats.toJSONString(): {0}",
            (usageStats == null ? "NULL" : usageStats.toJSONString()));
        // Convert the usageStats object to JSON and return it.
        writeText(response, usageStats.toJSONString());
      } else {
        xLogger.info("No results: {0}", resultsList);
        writeText(response, "{\"msg\": \"No results\" }");
      }

    } catch (Exception e) {
      xLogger.severe("{0} when trying to get monthly usage stats across domains. Message: {1}",
          e.getClass().getName(), e.getMessage());
      response.setStatus(500);
    }
    xLogger.fine("Exiting getMonthlyUsageStatsAcrossDomains");
  }
}

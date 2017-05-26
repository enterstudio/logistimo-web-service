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

import com.google.visualization.datasource.DataSourceHelper;
import com.google.visualization.datasource.DataSourceRequest;
import com.google.visualization.datasource.base.DataSourceException;
import com.google.visualization.datasource.base.ReasonType;
import com.google.visualization.datasource.base.ResponseStatus;
import com.google.visualization.datasource.base.StatusType;
import com.google.visualization.datasource.datatable.DataTable;
import com.google.visualization.datasource.datatable.TableRow;

import com.logistimo.config.models.DomainConfig;
import com.logistimo.pagination.PageParams;
import com.logistimo.reports.ReportsConstants;
import com.logistimo.reports.generators.ReportData;
import com.logistimo.reports.generators.ReportingDataException;
import com.logistimo.reports.service.ReportsService;
import com.logistimo.security.SecureUserDetails;
import com.logistimo.api.security.SecurityMgr;
import com.logistimo.services.ServiceException;
import com.logistimo.services.Services;
import com.logistimo.constants.Constants;
import com.logistimo.reports.utils.ReportsUtil;
import com.logistimo.api.util.SessionMgr;
import com.logistimo.logger.XLog;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

//import java.io.PrintWriter;
//import java.util.List;

/**
 * Generate report data for Google Visualization
 *
 * @author Arun
 */
@SuppressWarnings("serial")
public class ReportsServlet extends SgServlet {

  // Logger
  private static final XLog xLogger = XLog.getLog(ReportsServlet.class);

  // Process the GET request
  public void processGet(HttpServletRequest request, HttpServletResponse response,
                         ResourceBundle backendMessages, ResourceBundle messages)
      throws IOException {
    processRequest(request, response, backendMessages, messages);
  }

  public void processPost(HttpServletRequest request, HttpServletResponse response,
                          ResourceBundle backendMessages, ResourceBundle messages)
      throws IOException {
    // do nothing
  }

  // This is public, given PublicDemandDetailsServlet also uses this for public demand board details (only)
  protected void processRequest(HttpServletRequest request, HttpServletResponse response,
                                ResourceBundle backendMessages, ResourceBundle messages)
      throws IOException {
    xLogger.fine("Entered processRequest");

    // Get the various reporting filters
    String reportType = request.getParameter("type");
    String reportSubtype = request.getParameter("subtype");
    String startDateStr = request.getParameter("startdate");
    String endDateStr = request.getParameter("enddate");
    String domainIdStr = request.getParameter("domainid");
    // Get pagination parameters, if present
    String cursor = request.getParameter("cursor");
    String sizeStr = request.getParameter("size");
    // Get cursor type (for getting cursor from session) - typically sent instead of an actual cursor
    String cursorType = request.getParameter("cursortype");
    String offsetStr = request.getParameter("offset");
    String
        pdbDomainIdStr =
        request.getParameter(
            "pdbdomainid"); // domain ID from the public demand board (esp. given session info. can be missing there)
    // Check whether the data to be obtained is monthly
    String frequency = request.getParameter("frequency");

    if (frequency == null || frequency.isEmpty()) {
      frequency = ReportsConstants.FREQ_DAILY;
    }
    // Flag to determine if cursor is to be stored
    boolean storeCursorInSession = false;

    // If cursor is not sent, check if has to obtained from the session
    HttpSession session = request.getSession();
    int offset = 0;
    if (offsetStr != null && !offsetStr.isEmpty()) {
      offset = Integer.parseInt(offsetStr);
    }
    int size = 0;
    if (sizeStr != null && !sizeStr.isEmpty()) {
      size = Integer.parseInt(sizeStr);
    }
    if (cursor == null || cursor.isEmpty()) {
      if (cursorType != null && !cursorType.isEmpty() && offset > 0) {
        cursor = SessionMgr.getCursor(session, cursorType, offset);
        storeCursorInSession = true;
      }
    }
    if (startDateStr != null) {
      startDateStr = startDateStr.trim();
    }
    if (endDateStr != null) {
      endDateStr = endDateStr.trim();
    }

    // Get the report data and send a Google visualization response
    DataSourceRequest dsRequest = null;
    String errMsg = null;
    try {
      // Get the pdbDomainId, if present
      Long pdbDomainId = null;
      if (pdbDomainIdStr != null && !pdbDomainIdStr.isEmpty()) {
        try {
          pdbDomainId = Long.valueOf(pdbDomainIdStr);
        } catch (NumberFormatException e) {
          xLogger
              .warn("Numberformatexception in public demand board domainId: {0}", e.getMessage());
        }
      }
      // Get the user's locale
      SecureUserDetails sUser = null;
      String userId = null;
      Locale locale = null;
      String timezone = null;
      if (pdbDomainId == null) { // this means this is not from public demand board
        sUser = SecurityMgr.getUserDetails(request.getSession());
        userId = sUser.getUsername();
        locale = sUser.getLocale();
        timezone = sUser.getTimezone();
      }
      // Get domain Id
      Long domainId = null;
      if (domainIdStr != null && !domainIdStr.isEmpty()) {
        try {
          domainId = Long.valueOf(domainIdStr);
        } catch (NumberFormatException e) {
          xLogger.warn("Invalid domain ID format: {0}", domainIdStr);
        }
      } else // Get from the user's session
      {
        domainId = SessionMgr.getCurrentDomain(request.getSession(), userId);
      }
      // Get the domain config
      DomainConfig dc = null;
      if (pdbDomainId != null) { // always get domain config from db. for public demand board
        dc = DomainConfig.getInstance(pdbDomainId);
      } else if (domainId != null) { // get from session/db. for a logged in session
        dc = DomainConfig.getInstance(domainId);
      }
      // If locale/timezone is null, check the domain defaults (typically, the case for public demand board)
      if (locale == null || timezone == null) {
        if (dc != null) {
          if (locale == null) {
            locale = dc.getLocale();
          }
          if (timezone == null) {
            timezone = dc.getTimezone();
          }
        }
      }
      // Get the start and end dates
      SimpleDateFormat df = new SimpleDateFormat(Constants.DATE_FORMAT);
      Date startDate = null;
      Date endDate = null;
      if (startDateStr != null && !startDateStr.isEmpty()) {
        startDate = df.parse(startDateStr);
      }
      if (endDateStr != null && !endDateStr.isEmpty()) {
        endDate = df.parse(endDateStr);
      }
      xLogger.fine("startDate: {0}, endDate: {1}", startDate, endDate);
      // Create the filter map
      Map<String, Object> filters = ReportsUtil.getReportFilters(request);
      xLogger.fine("filters: {0}", filters);
      // Get the pagination parameters, if present
      PageParams pageParams = getPageParams(cursor, size);
      // Get the report data
      ReportsService rs = Services.getService("reports", locale);
      ReportData
          r =
          rs.getReportData(reportType, startDate, endDate, frequency, filters, locale, timezone,
              pageParams, dc, userId);
      // Update session with cursor, if necessary
      if (storeCursorInSession && r.getCursor() != null) {
        int nextOffset = offset + size;
        SessionMgr.setCursor(session, cursorType, nextOffset, r.getCursor());
        xLogger.fine(
            "ReportsServlet: after API call, set cursor - cursor = {0}, cursorType = {1}, (nxt)offset = {2}",
            r.getCursor(), cursorType, nextOffset);
      }
      // Get the data table
      DataTable data = new DataTable();
      if (r != null) {
        data = r.generateDataTable(reportSubtype);
      } else {
        xLogger.warn("Report data returned NULL");
      }
      // If cursor is to be sent back, set it as the first cell in the last row
      if (cursorType != null && !cursorType.isEmpty()) {
        if (data != null) {
          TableRow tr = new TableRow();
          tr.addCell(r.getCursor() != null ? r.getCursor() : "");
          data.addRow(tr);
        }
      }
      // Process the Google visualization reponse
      // Extract the datasource request parameters, if any (e.g. tq)
      dsRequest = new DataSourceRequest(request);
      // NOTE: If you want to work in restricted mode, which means that only
      // requests from the same domain can access the data source, do the verification below.
      // DataSourceHelper.verifyAccessApproved(dsRequest);

      // Apply the query to the data table.
      DataTable
          newData =
          DataSourceHelper.applyQuery(dsRequest.getQuery(), data, dsRequest.getUserLocale());
      // Set the response.
      DataSourceHelper.setServletResponse(newData, dsRequest, response);
    } catch (ServiceException e) {
      errMsg = e.getMessage();
      xLogger.warn("Exception while getting report data: {0}", errMsg);
    } catch (ParseException e) {
      errMsg = e.getMessage();
      xLogger.warn("Exception while parsing start/end dates: {0}", errMsg);
    } catch (ReportingDataException e) {
      errMsg = e.getMessage();
      xLogger.warn("Reporting data exception: {0}", errMsg);
//		} catch ( DeadlineExceededException e ) {
//    		errMsg = backendMessages.getString("error.deadlineexceeded");
//    		xLogger.severe( "DeadlineExceeded: {0}", e.getMessage() );
//    	} catch ( CapabilityDisabledException e ) {
//    		errMsg = backendMessages.getString("error.capabilitydisabled");
//    		xLogger.severe( "CapabilityDisabled: {0}", e.getMessage() );
    } catch (DataSourceException e) {
      if (dsRequest != null) {
        DataSourceHelper.setServletErrorResponse(e, dsRequest, response);
      } else {
        DataSourceHelper.setServletErrorResponse(e, request, response);
      }
    } catch (RuntimeException e) {
      errMsg = e.getMessage();
      xLogger.warn("A runtime exception has occured", errMsg);
    }
    // Send the internal error response, if any
    if (errMsg != null) {
      ResponseStatus
          status =
          new ResponseStatus(StatusType.ERROR, ReasonType.INTERNAL_ERROR, errMsg);
      if (dsRequest == null) {
        dsRequest = DataSourceRequest.getDefaultDataSourceRequest(request);
      }
      DataSourceHelper.setServletErrorResponse(status, dsRequest, response);
    }

    xLogger.fine("Exiting processRequest");
  }

  // Get pagination parameters, if any
  private PageParams getPageParams(String cursor, int size) {
    if ((cursor == null || cursor.isEmpty()) && size == 0) {
      return null;
    } else {
      return new PageParams(cursor, size);
    }
  }
}

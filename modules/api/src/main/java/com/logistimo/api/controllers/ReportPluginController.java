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

import com.logistimo.api.security.SecurityMgr;
import com.logistimo.api.util.SessionMgr;
import com.logistimo.exception.BadRequestException;
import com.logistimo.logger.XLog;
import com.logistimo.reports.plugins.models.ReportChartModel;
import com.logistimo.reports.plugins.models.TableResponseModel;
import com.logistimo.reports.plugins.service.ReportPluginService;
import com.logistimo.security.SecureUserDetails;

import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

/** Created by mohan on 02/03/17. */
@Controller
@RequestMapping("/plugins/report")
public class ReportPluginController {

  private static final XLog xLogger = XLog.getLog(ReportPluginController.class);

  private static final String JSON_REPORT_TYPE = "type";
  private static final String JSON_REPORT_VIEW_TYPE = "viewtype";

  @Autowired ReportPluginService service;

  @RequestMapping(value = "/", method = RequestMethod.GET)
  public @ResponseBody
  List<ReportChartModel> getReportData(@RequestParam String json, HttpServletRequest request) {
    xLogger.fine("Entering getReportData");
    try {
      JSONObject jsonObject = new JSONObject(json);
      if (!jsonObject.has(JSON_REPORT_TYPE)) {
        xLogger.warn("Report type is mandatory.");
        throw new BadRequestException("Invalid request");
      }
      SecureUserDetails sUser = SecurityMgr.getUserDetails(request.getSession());
      String userId = sUser.getUsername();
      Long domainId = SessionMgr.getCurrentDomain(request.getSession(), userId);
      return service.getReportData(domainId, json);
    } catch (Exception e) {
      xLogger.severe("Error while getting the report data", e);
      return null;
    }
  }

  @RequestMapping(value = "/breakdown", method = RequestMethod.GET)
  public @ResponseBody
  TableResponseModel getReportTableData(
      @RequestParam String json, HttpServletRequest request) {
    xLogger.fine("Entering getReportData");
    try {
      JSONObject jsonObject = new JSONObject(json);
      if (!jsonObject.has(JSON_REPORT_TYPE) || !jsonObject.has(JSON_REPORT_VIEW_TYPE)) {
        xLogger.warn("Both report type and view type is mandatory.");
        throw new BadRequestException("Invalid request");
      }
      SecureUserDetails sUser = SecurityMgr.getUserDetails(request.getSession());
      String userId = sUser.getUsername();
      Long domainId = SessionMgr.getCurrentDomain(request.getSession(), userId);
      return service.getReportTableData(domainId, json);
    } catch (Exception e) {
      xLogger.severe("Error while getting the data", e);
      return null;
    }
  }
}

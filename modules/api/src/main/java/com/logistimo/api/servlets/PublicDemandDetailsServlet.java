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
package com.logistimo.api.servlets;

import com.logistimo.config.models.DomainConfig;
import com.logistimo.reports.ReportsConstants;
import com.logistimo.reports.generators.ReportData;
import com.logistimo.logger.XLog;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ResourceBundle;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Forwards a public demand board details request to the reports servlet. Any other type of reports request is disallowed.
 * This servlet allows public access, whereas /reports only allows private/secured access.
 *
 * @author Arun
 */
@SuppressWarnings("serial")
public class PublicDemandDetailsServlet extends ReportsServlet {

  private static final XLog xLogger = XLog.getLog(PublicDemandDetailsServlet.class);

  @Override
  protected void processRequest(HttpServletRequest req, HttpServletResponse resp,
                                ResourceBundle backendMessages, ResourceBundle messages)
      throws IOException {
    // Read the report type
    String type = req.getParameter("type");
    xLogger.fine("Type is: " + type);
    String domainIdStr = req.getParameter("pdbdomainid");
    boolean accessDenied = true;
    Long domainId = null;
    if (domainIdStr != null && !domainIdStr.isEmpty()) {
      domainId = Long.valueOf(domainIdStr);
    }
    if (domainId != null) {
      DomainConfig dc = DomainConfig.getInstance(domainId);
      if (dc.isDemandBoardPublic()) {
        accessDenied = false;
      }
    }

    if (!accessDenied && ReportsConstants.TYPE_DEMANDBOARD.equals(type)) {
      super.processRequest(req, resp, backendMessages, messages);
    } else if (!accessDenied && ReportsConstants.TYPE_STOCK.equals(type)) {
      super.processRequest(req, resp, backendMessages, messages);
    } else {
      PrintWriter pw = resp.getWriter();
      pw.write(
          "Access to this data is denied. Please check with your administrator for appropriate permissions.");
      pw.close();
    }
  }
}

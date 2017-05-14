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

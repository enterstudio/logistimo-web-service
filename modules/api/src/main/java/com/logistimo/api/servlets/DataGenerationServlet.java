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

import com.logistimo.constants.Constants;
import com.logistimo.api.util.KioskDataSimulator;
import com.logistimo.logger.XLog;

import java.io.IOException;
import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ResourceBundle;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author arun
 */
@SuppressWarnings("serial")
public class DataGenerationServlet extends JsonRestServlet {

  // Action values
  private static final String
      ACTION_SIMULATETRANSDATA =
      "simulatetransdata";
  // simulates transaction data (as defined by data.jsp)

  // Parameters
  private static final String KIOSKID_PARAM = "kioskid";
  private static final String STARTDATE_PARAM = "startdate";
  private static final String DURATION_PARAM = "duration";
  private static final String STOCKONHAND_PARAM = "stockonhand";
  private static final String ISSUEPERIODICITY_PARAM = "issueperiodicity";
  private static final String ISSUEMEAN_PARAM = "issuemean";
  private static final String ISSUESTDDEV_PARAM = "issuestddev";
  private static final String RECEIPTMEAN_PARAM = "receiptmean";
  private static final String RECEIPTSTDDEV_PARAM = "receiptstddev";
  private static final String ZEROSTOCKDAYSLOW_PARAM = "zerostockdayslow";
  private static final String ZEROSTOCKDAYSHIGH_PARAM = "zerostockdayshigh";
  private static final String MATERIALID_PARAM = "materialid";
  private static final String DOMAINID_PARAM = "domainid";
  private static final String USERID_PARAM = "userid";

  private static final XLog xLogger = XLog.getLog(DataGenerationServlet.class);

  public void processGet(HttpServletRequest req, HttpServletResponse resp,
                         ResourceBundle backendMessages, ResourceBundle messages)
      throws IOException {
    xLogger.fine("Entered doGet");
    xLogger.warn("GET not supported by DataGenerationServlet");
    xLogger.fine("Entered doGet");
  }

  public void processPost(HttpServletRequest req, HttpServletResponse resp,
                          ResourceBundle backendMessages, ResourceBundle messages)
      throws IOException {
    xLogger.fine("Entered doPost");
    // Get the action command
    String action = req.getParameter("action");

    if (ACTION_SIMULATETRANSDATA.equalsIgnoreCase(action)) {
      executeTransDataSimulation(req, resp);
    } else {
      xLogger.warn("No relevant action specified: {0}", action);
      sendErrorResponse(resp, 200, "0", "Invalid action");
    }

    xLogger.fine("Exiting doPost");
  }

  /**
   * Generate transaction-data generation based on an earlier request
   * (typically called from a task in the task queue, which was earlier scheduled by
   * scheduleTransDataSimulationRequest)
   */
  private void executeTransDataSimulation(HttpServletRequest req, HttpServletResponse resp)
      throws IOException {
    xLogger.fine("Entering executeTransDataSimulationRequest");

    // Get the post parameters
    String kioskIdStr = req.getParameter(KIOSKID_PARAM);
    String startDateStr = req.getParameter(STARTDATE_PARAM);
    String durationStr = req.getParameter(DURATION_PARAM);
    String stockOnHandStr = req.getParameter(STOCKONHAND_PARAM);
    String issuePeriodicityStr = req.getParameter(ISSUEPERIODICITY_PARAM);
    String issueMeanStr = req.getParameter(ISSUEMEAN_PARAM);
    String issueStdDevStr = req.getParameter(ISSUESTDDEV_PARAM);
    String receiptMeanStr = req.getParameter(RECEIPTMEAN_PARAM);
    String receiptStdDevStr = req.getParameter(RECEIPTSTDDEV_PARAM);
    String zeroStockDaysLowStr = req.getParameter(ZEROSTOCKDAYSLOW_PARAM);
    String zeroStockDaysHighStr = req.getParameter(ZEROSTOCKDAYSHIGH_PARAM);
    // Get the material id
    String materialIdStr = req.getParameter(MATERIALID_PARAM);
    // Get the domain Id
    String domainIdStr = req.getParameter(DOMAINID_PARAM);
    // Get the user Id
    String userId = req.getParameter(USERID_PARAM);

    // Initialize simulator and simulate the data
    String message = null;
    Long kioskId = null;
    int numTrans = 0; // number of transactions generated
    // Get the kiosk ID
    if (kioskIdStr != null && !kioskIdStr.isEmpty()) {
      try {
        kioskId = Long.parseLong(kioskIdStr);
      } catch (NumberFormatException e) {
        message =
            "Number format error while parsing kiosk-id: " + kioskIdStr + " : " + e.getMessage();
      }
    } else {
      message = "Invalid kiosk ID has been passed.";
    }

    if (kioskId == null) {
      xLogger.warn("Error: Cannot schedule data generation task: Invalid kiosk Id");
      xLogger.fine("Exiting executeTransDataSimulationRequest");
      return;
    }

    // Execute the request for a given kiosk-material pair (this request typically comes from the scheduled task (above)
    // Simulate data
    try {
      Long materialId = null;
      Long domainId = null;
      if (materialIdStr != null && !materialIdStr.isEmpty()) {
        materialId = Long.valueOf(materialIdStr);
      }
      if (domainIdStr != null && !domainIdStr.isEmpty()) {
        domainId = Long.valueOf(domainIdStr);
      }

      xLogger.fine("Now executing for kiosk-material {0}-{1}...and domain {2}", kioskId, materialId,
          domainId);

      // Initialize the kiosk data simulator
      KioskDataSimulator simulator = new KioskDataSimulator();
      simulator.setStartDate((new SimpleDateFormat(Constants.DATE_FORMAT)).parse(startDateStr));
      simulator.setNumberOfDays(Integer.parseInt(durationStr));
      simulator.setIssuePeriodicity(issuePeriodicityStr);
      simulator.setStockOnHand(new BigDecimal(Integer.parseInt(stockOnHandStr)));
      simulator.setIssueMean(Float.parseFloat(issueMeanStr));
      simulator.setIssueStdDev(Float.parseFloat(issueStdDevStr));
      simulator.setReceiptMean(Float.parseFloat(receiptMeanStr));
      simulator.setReceiptStdDev(Float.parseFloat(receiptStdDevStr));
      simulator.setZeroStockDaysLow(Integer.parseInt(zeroStockDaysLowStr));
      simulator.setZeroStockDaysHigh(Integer.parseInt(zeroStockDaysHighStr));
      // Simulate data
      numTrans = simulator.generateData(domainId, kioskId, materialId, userId);

      // Log the information
      xLogger.fine("Simulated " + String.valueOf(numTrans) + " transaction(s) for kiosk-material "
          + kioskIdStr + "-" + materialIdStr);
    } catch (NumberFormatException e) {
      message =
          "Number formatting error for kiosk-material: " + kioskIdStr + "-" + materialIdStr + " ("
              + e.getMessage() + ")";
    } catch (ParseException e) {
      message = "Invalid date format for date : " + startDateStr + " (" + e.getMessage() + ")";
    } catch (Exception e) {
      message =
          "Exception when simulating data for kiosk-material " + kioskIdStr + "-" + materialIdStr
              + " (" + e.getMessage() + ")";
    }

    if (message != null) {
      xLogger.warn(message);
    }

    xLogger.fine("Exiting executeTransDataSimulationRequest");
  }
}

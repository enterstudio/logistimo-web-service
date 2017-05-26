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

import com.logistimo.AppFactory;
import com.logistimo.entities.entity.IKiosk;
import com.logistimo.entities.service.EntitiesService;
import com.logistimo.entities.service.EntitiesServiceImpl;
import com.logistimo.inventory.entity.IInvntry;
import com.logistimo.inventory.service.InventoryManagementService;
import com.logistimo.inventory.service.impl.InventoryManagementServiceImpl;
import com.logistimo.materials.entity.IMaterial;
import com.logistimo.materials.service.MaterialCatalogService;
import com.logistimo.materials.service.impl.MaterialCatalogServiceImpl;
import com.logistimo.services.taskqueue.ITaskService;

import com.logistimo.security.SecureUserDetails;
import com.logistimo.api.security.SecurityMgr;
import com.logistimo.services.ServiceException;
import com.logistimo.services.Services;
import com.logistimo.proto.JsonTagsZ;
import com.logistimo.users.entity.IUserAccount;
import com.logistimo.users.service.UsersService;
import com.logistimo.users.service.impl.UsersServiceImpl;

import com.logistimo.constants.Constants;
import com.logistimo.api.util.KioskDataSimulator;
import com.logistimo.api.util.SessionMgr;
import com.logistimo.utils.StringUtil;
import com.logistimo.exception.TaskSchedulingException;
import com.logistimo.logger.XLog;

import java.io.IOException;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author arun
 */
@SuppressWarnings("serial")
public class DataServlet extends JsonRestServlet {

  // Action values
  ///private static final String ACTION_CREATE = "create";
  private static final String ACTION_GETUSER = "getuser";
  private static final String ACTION_GETKIOSK = "getkiosk";
  private static final String ACTION_GETKIOSKBYID = "getkioskbyid";
  private static final String ACTION_GETMATERIALBYID = "getmaterialbyid";
  private static final String
      ACTION_SIMULATETRANSDATA =
      "simulatetransdata";
  // simulates transaction data (as defined by data.jsp)
  private static final String
      ACTION_SIMULATEORDERS =
      "simulateorders";
  // simulates optimized orders (as defined by data.jsp)
  ///private static final String ACTION_MIGRATE = "migratedata"; // migrates data, as coded

  // Parameters
  private static final String REQUESTTYPE_PARAM = "requesttype";
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
  ///private static final String TYPE_PARAM = "type";
  ///private static final String NUMRECORDS_PARAM = "numrecords";
  private static final String USERID_PARAM = "userid";

  private static final String REQUESTTYPE_SCHEDULE = "schedule";
  private static final String REQUESTTYPE_EXECUTE = "execute";

  private static final String TASK_URL = "/task/datagenerator";
  private static final XLog xLogger = XLog.getLog(DataServlet.class);
  private static ITaskService taskService = AppFactory.get().getTaskService();

  public void processGet(HttpServletRequest req, HttpServletResponse resp,
                         ResourceBundle backendMessages, ResourceBundle messages)
      throws IOException {
    String action = req.getParameter("action");
    xLogger.fine("Executing action {0}", action);
    try {
      UsersService as = Services.getService(UsersServiceImpl.class);
      EntitiesService es = Services.getService(EntitiesServiceImpl.class);
      if (ACTION_GETUSER.equalsIgnoreCase(action)) {
        String userId = req.getParameter("userId");
        IUserAccount user = as.getUserAccount(userId);
        if (user != null) {
          xLogger.fine("Got UserAccount for id: {0}", userId);
          // Check if kiosks are retrieved
          List<IKiosk> kiosks = (List<IKiosk>) es.getKiosksForUser(user,null,null).getResults();
          if (kiosks == null) {
            xLogger.fine("NO Kiosks for user: {0}" + userId);
          } else {
            xLogger.fine("Kiosks found for this user: {0} kiosks" + kiosks.size());
          }

          @SuppressWarnings("rawtypes")
          Hashtable userMap = user.getMapZ();
          if (userMap != null) {
            writeText(resp, userMap.toString());
          }
        } else {
          xLogger.fine("UserAccount is NULL for id: {0}", userId);
        }
      } else if (ACTION_GETKIOSK.equalsIgnoreCase(action)) {
        String kioskName = req.getParameter("name");
        xLogger.fine("Getting kiosk: {0}", kioskName);
        List<IKiosk> kiosks = es.findKiosks(Constants.DOMAINID_DEFAULT, JsonTagsZ.NAME, kioskName);
        if (kiosks != null) {
          Iterator<IKiosk> it = kiosks.iterator();
          while (it.hasNext()) {
            IKiosk kiosk = it.next();
            writeText(resp, (kiosk.getMapZ(false, null).toString() + "\n"));
          }
        }
      } else if (ACTION_GETMATERIALBYID.equalsIgnoreCase(action)) {
        String materialId = req.getParameter("materialId");

        if (materialId != null && StringUtil.isStringLong(materialId)) {
          MaterialCatalogService mcs = Services.getService(MaterialCatalogServiceImpl.class);
          IMaterial material = mcs.getMaterial(Long.parseLong(materialId));

          if (material != null) {
            writeText(resp, material.toJsonStr());
          } else {
            xLogger.fine("No material for materialId: " + materialId);
          }
        } else {
          xLogger.fine("Invalid materialId: " + materialId);
        }
      } else if (ACTION_GETKIOSKBYID.equalsIgnoreCase(action)) {
        String kioskId = req.getParameter("kioskId");
        String rType = req.getParameter("type");
        xLogger.fine("Getting kiosk: {0}", kioskId);
        IKiosk kiosk = es.getKiosk(Long.valueOf(kioskId));
        if (kiosk != null) {
          List<IUserAccount> users = (List<IUserAccount>) kiosk.getUsers();
          if (users == null) {
            xLogger.fine("NO users for this kiosk");
          } else {
            xLogger.fine("USERS found for this kiosk: {0}", users.size());
          }
          if ("json".equals(rType)) {
            writeText(resp, kiosk.getEntityJSON());
          } else {
            writeText(resp, kiosk.getMapZ(false, null).toString());
          }
        } else {
          xLogger.fine("NULL kiosk for kioskId: " + kioskId);
        }
      } else {
        xLogger.fine("No action specified");
        sendErrorResponse(resp, 200, "0", "Invalid action");
      }
    } catch (Exception e) {
      e.printStackTrace();
      resp.setStatus(500);
    }
  }

  public void processPost(HttpServletRequest req, HttpServletResponse resp,
                          ResourceBundle backendMessages, ResourceBundle messages)
      throws IOException {
    xLogger.fine("Entered doPost");
    // Get the action command
    String action = req.getParameter("action");
    // Get the request type
    String reqType = req.getParameter(REQUESTTYPE_PARAM);

    try {
      if (ACTION_SIMULATETRANSDATA.equalsIgnoreCase(action)) {
        if (REQUESTTYPE_SCHEDULE.equals(reqType)) {
          scheduleTransDataSimulationRequest(req, resp);
        }
      } else if (ACTION_SIMULATEORDERS.equalsIgnoreCase(action)) {
        simulateOrders(req, resp);
      } else {
        xLogger.fine("No action specified");
        sendErrorResponse(resp, 200, "0", "Invalid action");
      }
    } catch (ServiceException e) {
      e.printStackTrace();
      resp.setStatus(500);
    }

    xLogger.fine("Exiting doPost");
  }

  /**
   * Schedule one task-queue transaction-data generation request per kiosk-material combination.
   */
  @SuppressWarnings("unchecked")
  private void scheduleTransDataSimulationRequest(HttpServletRequest req, HttpServletResponse resp)
      throws IOException {
    xLogger.fine("Entering scheduleTransDataSimulationRequest");

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
    String domainIdStr = req.getParameter(DOMAINID_PARAM);

    // Initialize simulator and simulate the data
    String message = null;
    Long kioskId = null;
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

    // Get the logged in user
    SecureUserDetails sUser = SecurityMgr.getUserDetails(req.getSession());
    String userId = sUser.getUsername();
    // Get the domain Id from the session for the logged in user
    Long domainId = null; // Can also get this from the session, but now getting it from the post
    if (domainIdStr != null && !domainIdStr.isEmpty()) {
      try {
        domainId = Long.valueOf(domainIdStr);
      } catch (NumberFormatException e) {
        message = "Domain id format invalid: " + e.getMessage();
      }
    }

    // Schedule one data-simulator task per kiosk-material combination, if request is for scheduling
    xLogger.fine("Now scheduling for kiosk {0} and domain {1}...", kioskId, domainId);

    // If kioskId is invalid, then write back a message and return
    if (kioskId == null || domainId == null) {
      writeToScreen(req, resp, message, Constants.MODE_MANAGE, Constants.VIEW_DATA);
      xLogger.fine("Exiting scheduleTransDataSimulationRequest");
      return;
    }

    try {
      InventoryManagementService
          ims =
          Services.getService(InventoryManagementServiceImpl.class);
      // Get the materials associated with the kiosk
      List<IInvntry>
          inventories =
          ims.getInventoryByKiosk(kioskId, null).getResults(); // TODO: pagination

      if (inventories == null || inventories.size() == 0) {
        message = "No materials are associated with the kiosk. No transaction data was generated.";
      } else {
        Iterator<IInvntry> it = inventories.iterator();
        // Schedule data generation tasks per material
        int totalMaterials = inventories.size();
        int numMaterials = 0;
        while (it.hasNext() && numMaterials < KioskDataSimulator.MATERIAL_LIMIT) {
          IInvntry inv = it.next();
          // Get the parameter map
          Map<String, String> params = new HashMap<String, String>();
          // Add action and type parameters
          params.put("action", ACTION_SIMULATETRANSDATA);
          params.put(REQUESTTYPE_PARAM, REQUESTTYPE_EXECUTE);
          // Add the data simulation parameters
          params.put(KIOSKID_PARAM, kioskIdStr);
          params.put(STARTDATE_PARAM, startDateStr);
          params.put(DURATION_PARAM, durationStr);
          params.put(STOCKONHAND_PARAM, stockOnHandStr);
          params.put(ISSUEPERIODICITY_PARAM, issuePeriodicityStr);
          params.put(ISSUEMEAN_PARAM, issueMeanStr);
          params.put(ISSUESTDDEV_PARAM, issueStdDevStr);
          params.put(RECEIPTMEAN_PARAM, receiptMeanStr);
          params.put(RECEIPTSTDDEV_PARAM, receiptStdDevStr);
          params.put(ZEROSTOCKDAYSLOW_PARAM, zeroStockDaysLowStr);
          params.put(ZEROSTOCKDAYSHIGH_PARAM, zeroStockDaysHighStr);
          // Add the material Id as a new parameter to the scheduled task URL
          params.put(MATERIALID_PARAM, inv.getMaterialId().toString());
          // Add the domain Id as a new parameter to the scheduled task URL
          params.put(DOMAINID_PARAM, (domainId != null ? domainId.toString() : ""));
          // Add the user Id
          params.put(USERID_PARAM, (userId != null ? userId : ""));
          // Schedule the task of simulating trans. data for a given kiosk-material
          try {
            taskService.schedule(taskService.QUEUE_DATASIMULATOR, TASK_URL, params, null,
                taskService.METHOD_POST, domainId, userId, "SIMULATE_TRANS");
            numMaterials++; // count materials for successful scheduling
          } catch (TaskSchedulingException e) {
            xLogger.warn("Exception while scheduling task for kiosk-material: {0}-{1} : {2}",
                kioskIdStr, inv.getMaterialId(), e.getMessage());
          }
        }

        // Form the success message
        message =
            "Scheduled transaction-data simulation request for <b>" + String.valueOf(numMaterials)
                + "</b> material(s).";
        if ((totalMaterials - numMaterials) > 0) {
          message +=
              " Could not schedule task(s) for <b>" + String.valueOf(totalMaterials - numMaterials)
                  + "</b> material(s).";
        }
      }
    } catch (ServiceException e) {
      message = "System error when getting Material Catalog Service: " + e.getMessage();
      xLogger.severe("System error when getting Material Catalog Service: ", e);
    }

    // Send confirmation of success/errors to user
    writeToScreen(req, resp, message, Constants.MODE_MANAGE, Constants.VIEW_DATA);

    xLogger.fine("Exiting scheduleTransDataSimulationRequest");
  }

  /**
   * Simulate orders for a given kiosk, based on historical transaction data that is available
   */
  private void simulateOrders(HttpServletRequest req, HttpServletResponse resp)
      throws ServiceException, IOException {
    xLogger.fine("Entered simulateOrders");

    // Get the form parameters
    String kioskIdStr = req.getParameter("kioskid");
    // Get the locale
    SecureUserDetails sUser = SecurityMgr.getUserDetails(req.getSession());
    Long domainId = SessionMgr.getCurrentDomain(req.getSession(), sUser.getUsername());

    // Make the HTTP call to optimize servlet
    Map<String, String> params = new HashMap<String, String>();
    params.put("domainid", domainId.toString());
    params.put("kioskid", kioskIdStr);
    params.put("force", "true");
    String message = null;
    try {
      taskService.schedule(taskService.QUEUE_OPTIMZER, "/task/optimize", params, null,
          taskService.METHOD_POST, domainId, sUser.getUsername(), "SIMULATE_ORDERS");
      message = "Successfully scheduled task to perform computations.";
    } catch (Exception e) {
      xLogger.severe("Exception when calling /task/optimize: {0} : {1}", e.getClass().getName(),
          e.getMessage());
      message = "Error: " + e.getMessage();
    }

    writeToScreen(req, resp, message, Constants.MODE_MANAGE, Constants.VIEW_DATA);

    xLogger.fine("Exiting simulateOrders");
  }

  /**
   * Migrate data as required
   */
        /*
        private void migrateData( HttpServletRequest req, HttpServletResponse resp ) throws IOException {
		xLogger.fine( "Entering migrateData" );

		// Get the type of migration required
		String type = req.getParameter( TYPE_PARAM );
		String domainIdStr = req.getParameter( DOMAINID_PARAM );
		String numRecordsStr = req.getParameter( NUMRECORDS_PARAM );
		int numRecords = 0;
		if ( numRecordsStr != null && !numRecordsStr.isEmpty() )
			numRecords = Integer.parseInt( numRecordsStr );
		Long domainId = null;
		if ( domainIdStr != null && !domainIdStr.isEmpty() )
			domainId = Long.valueOf( domainIdStr );
		
		try {
			MigrationUtil.migrate( type, numRecords, domainId );
		} catch ( Exception e ) {
			e.printStackTrace();
		}
		
		xLogger.fine( "Exiting migrateData" );
	}
	*/
  private void writeToScreen(HttpServletRequest req, HttpServletResponse resp, String message,
                             String mode, String view)
      throws IOException {
    writeToScreen(req, resp, message, mode, view, "/s/message.jsp");
  }
}

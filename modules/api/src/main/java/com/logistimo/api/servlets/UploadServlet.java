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
import com.logistimo.auth.SecurityMgr;
import com.logistimo.auth.utils.SessionMgr;
import com.logistimo.bulkuploads.BulkImportMapperContants;
import com.logistimo.bulkuploads.BulkUploadMgr;
import com.logistimo.constants.Constants;
import com.logistimo.dao.JDOUtils;
import com.logistimo.entity.IUploaded;
import com.logistimo.exception.TaskSchedulingException;
import com.logistimo.logger.XLog;
import com.logistimo.security.SecureUserDetails;
import com.logistimo.services.BulkImportHandler;
import com.logistimo.services.ObjectNotFoundException;
import com.logistimo.services.ServiceException;
import com.logistimo.services.Services;
import com.logistimo.services.UploadService;
import com.logistimo.services.blobstore.BlobInfo;
import com.logistimo.services.blobstore.BlobstoreService;
import com.logistimo.services.impl.UploadServiceImpl;
import com.logistimo.services.mapred.IMapredService;
import com.logistimo.services.taskqueue.ITaskService;
import com.logistimo.utils.HttpUtil;
import com.logistimo.utils.NumberUtil;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


/**
 * @author Arun
 */
public class UploadServlet extends SgServlet {

  private static final long serialVersionUID = 1L;
  // Logger
  private static final XLog xLogger = XLog.getLog(UploadServlet.class);
  // Actions
  private static final String ACTION_SCHEDULEBULKUPLOAD = "schdbu";
  // Values
  private static final String CONFIGNAME_BULKIMPORTER = "BulkImporter";
  // Done callback URL
  private static final String DONE_CALLBACK_URL = "/task/upload";

  private static ITaskService taskService = AppFactory.get().getTaskService();


  @Override
  protected void processGet(HttpServletRequest request, HttpServletResponse response,
                            ResourceBundle backendMessages,
                            ResourceBundle messages)
      throws ServletException, IOException, ServiceException {
    processPost(request, response, backendMessages, messages);
  }

  @Override
  protected void processPost(HttpServletRequest request, HttpServletResponse response,
                             ResourceBundle backendMessages,
                             ResourceBundle messages)
      throws ServletException, IOException, ServiceException {
    xLogger.fine("Entered processPost");
    String action = request.getParameter("a");
    String
        mrJobId =
        request.getParameter(
            IMapredService.JOBID_PARAM); // MapReduce job ID, if this is was a MR callback invocation
    if (mrJobId != null && !mrJobId.isEmpty()) {
      xLogger.info("mrJobId: {1}", action, mrJobId);
      BulkImportHandler.handleBulkImportMRCallback(mrJobId);
      return;
    }
    // Process our actions (NOTE: NONE of our actions should have a parameter called 'job_id' - this is reserved for MR callback on bulk import only
    if (ACTION_SCHEDULEBULKUPLOAD.equals(action)) {
      scheduleBulkUpload(request, response, backendMessages, messages);
    } else {
      xLogger.severe("Invalid action: {0}", action);
    }
    xLogger.fine("Existing processPost");
  }

  // Handle a bulk upload of a given type
  private void scheduleBulkUpload(HttpServletRequest request, HttpServletResponse response,
                                  ResourceBundle backendMessages, ResourceBundle messages)
      throws IOException, ServiceException {
    xLogger.fine("Entered scheduleBulkUpload");
    // Get the type
    String type = request.getParameter("type");
    // Get the kioskId if specified
    String kioskIdStr = request.getParameter("kioskid");
    Long kioskId = null;
    if (kioskIdStr != null && !kioskIdStr.isEmpty()) {
      kioskId = Long.valueOf(kioskIdStr);
    }

    // Get the blobstore service
    BlobstoreService blobstoreService = AppFactory.get().getBlobstoreService();
    Map<String, List<String>> blobMap = blobstoreService.getUploads(request);
    // Get the blob-key
    String blobKey = null;
    if (blobMap != null) {
      List<String> keys = blobMap.get("data");
      if (keys != null && keys.size() > 0) {
        blobKey = keys.get(0);
      }
    }
    if (blobKey == null) {
      writeToSetupScreen(request, response, "Could not access uploaded file. Please try again.",
          type);
      return;
    }
    String blobKeyStr = blobKey;
    // Get user data
    SecureUserDetails sUser = SecurityMgr.getUserDetails(request.getSession());
    Long domainId = SessionMgr.getCurrentDomain(request.getSession(), sUser.getUsername());
    String userId = sUser.getUsername();
    Locale locale = sUser.getLocale();
    String localeStr = sUser.getLocale().toString();
    String version = "0";
    // Get the uploaded file name
    BlobInfo binfo = blobstoreService.getBlobInfo(blobKeyStr);
    String filename = userId + "." + type;
    if (binfo != null) {
      filename = binfo.getFilename();
    }
    // Store an uploaded object; first check if an older one exists from previous upload of same file
    IUploaded uploaded = null;
    UploadService svc = null;
    String uploadedKey = BulkUploadMgr.getUploadedKey(domainId, type, userId);
    boolean isUploadedNew = false;
    try {
      svc = Services.getService(UploadServiceImpl.class);
      uploaded = svc.getUploaded(uploadedKey);
      // Found an older upload, remove the older blob
      try {
        blobstoreService.remove(uploaded.getBlobKey());
      } catch (Exception e) {
        xLogger.warn("Exception {0} when trying to remove older blob for key {1}: {2}",
            e.getClass().getName(), uploaded.getId(), e.getMessage());
      }
    } catch (ObjectNotFoundException e) {
      uploaded = JDOUtils.createInstance(IUploaded.class);
      uploaded.setId(uploadedKey);
      isUploadedNew = true;
    } catch (Exception e) {
      xLogger.severe("{0} while trying to get UploadService or getting Uploaded for key {1}: {2}",
          e.getClass().getName(), uploadedKey, e.getMessage());
      writeToSetupScreen(request, response,
          "System error occurred during upload. Please try again or contact your administrator.",
          type);
      return;
    }
    Date now = new Date();
    // Update uploaded object
    uploaded.setBlobKey(blobKeyStr);
    uploaded.setDomainId(domainId);
    uploaded.setFileName(filename);
    uploaded.setLocale(localeStr);
    uploaded.setTimeStamp(now);
    uploaded.setType(IUploaded.BULKUPLOAD);
    uploaded.setUserId(userId);
    uploaded.setVersion(version);
    // Important to delete any error messages accumulated due to previous upload
    BulkUploadMgr.deleteUploadedMessage(uploadedKey);
    // Start bulk import task
    String message = "";
    try {
      // Start importing
      String
          jobId =
          startBulkImport(type, blobKeyStr, userId, domainId, kioskId,
              HttpUtil.getUrlBase(request));
      // Update Uploaded with Job Id
      if (jobId != null) {
        uploaded.setJobId(jobId);
        uploaded.setJobStatus(IUploaded.STATUS_PENDING);
      }
      // Persist upload metadata
      if (isUploadedNew) {
        svc.addNewUpload(uploaded);
      } else {
        svc.updateUploaded(uploaded);
      }
      String redirectUrlBase = null;
      if (BulkUploadMgr.TYPE_TRANSACTIONS.equals(type)
          || BulkUploadMgr.TYPE_TRANSACTIONS_CUM_INVENTORY_METADATA.equals(type)) {
        redirectUrlBase = "/s/inventory/inventory.jsp?subview=" + "Inventory Transactions";
      } else {
        redirectUrlBase = "/s/setup/setup.jsp?subview=" + type;
      }
      //String url = "https://" + request.getServerName() + ( SecurityManager.isDevServer() ? ":" + request.getServerPort() : "" ) + redirectUrlBase + "&form=bulkupload&type=" + type;
      String url = redirectUrlBase + "&form=bulkupload&type=" + type;
      message =
          backendMessages.getString("bulkupload.successfullyscheduled") + " <b>" + BulkUploadMgr
              .getDisplayName(type, locale) + "</b>. " + backendMessages
              .getString("pleasecheckyourstatus") + " <a href=\"" + url + "\">" + backendMessages
              .getString("bulkupload.onuploadpage") + "</a>.";
    } catch (Exception e) {
      xLogger.severe("{0} when scheduling upload task for type {1} by user {2}: {3}",
          e.getClass().getName(), type, sUser.getUsername(), e.getMessage());
      message =
          "Unable to schedule task to import bulk data on " + BulkUploadMgr
              .getDisplayName(type, locale) + ". Please try again later";
    }
    // Get back to user
    if (BulkUploadMgr.TYPE_TRANSACTIONS.equals(type)
        || BulkUploadMgr.TYPE_TRANSACTIONS_CUM_INVENTORY_METADATA.equals(type)) {
      writeToScreen(request, response, message, Constants.VIEW_INVENTORY);
    } else {
      writeToSetupScreen(request, response, message, type);
    }
    xLogger.fine("Exiting scheduleBulkUpload");
  }

  // Start a bulk import map-reduce process for a given type
  private String startBulkImport(String type, String blobKeyStr, String userId, Long domainId,
                                 Long kioskId, String urlBase) throws IOException {
    xLogger
        .fine("Entered startBulkImport: type = {0}, blobKeyStr = {1}, userId = {2}, domainId = {3}",
            type, blobKeyStr, userId, domainId);
    // If type is BulkUploadMgr.TYPE_TRANSACTIONS, or BulkUploadMgr.TRANSACTIONS_CUM_INVENTORY_METADATA do not start MapReduce job.
    // Instead, schedule a task for importing transactions and return a five digit random number as jobId
    if (BulkUploadMgr.TYPE_TRANSACTIONS.equals(type)
        || BulkUploadMgr.TYPE_TRANSACTIONS_CUM_INVENTORY_METADATA.equals(type)) {
      Map<String, String> params = new HashMap<String, String>();
      try {
        params.put("action", TransUploadServlet.ACTION_TRANSACTIONIMPORT);
        params.put("userid", userId);
        params.put("domainid", domainId.toString());
        params.put("blobkey", blobKeyStr);
        params.put("type", type);
        if (kioskId != null) {
          params.put("kioskid", kioskId.toString());
        }
        taskService.schedule(ITaskService.QUEUE_DEFAULT, "/task/transupload", params, null,
            ITaskService.METHOD_POST, -1, domainId, userId, "BULKIMPORT_TRANS");
      } catch (TaskSchedulingException e) {
        xLogger
            .severe("{0} when trying to schedule task to bulk import transactions into Logistimo");
      }
      String jobId = String.valueOf(NumberUtil.generateFiveDigitRandomNumber());
      return jobId;
    }
    // Form the URL parameters
    Map<String, String> params = new HashMap<String, String>();
    params.put(IMapredService.PARAM_BLOBKEY, blobKeyStr);
    params.put(BulkImportMapperContants.TYPE, type);
    params.put(BulkImportMapperContants.USERID, userId);
    params.put(BulkImportMapperContants.DOMAINID, domainId.toString());
    params.put(IMapredService.PARAM_DONECALLBACK,
        DONE_CALLBACK_URL); // callback to notify on completion
    // Params. to control input processing rate
    params.put(IMapredService.PARAM_SHARDCOUNT, "8");
    params.put(IMapredService.PARAM_INPUTPROCESSINGRATE, "3");
    // Start the MR job
    String jobId = AppFactory.get().getMapredService().startJob(CONFIGNAME_BULKIMPORTER, params);
    xLogger.fine("Exiting startBulkImport");
    return jobId;
  }

  // Handle the bulk import MR callback


  // Write to setup screen
  private void writeToSetupScreen(HttpServletRequest req, HttpServletResponse resp, String message,
                                  String subview)
      throws IOException {
    String
        url =
        "/s/setup/setup.jsp?form=messages&subview=" + subview + "&message=" + URLEncoder
            .encode(message, "UTF-8");
    writeToScreen(req, resp, message, null, null, url);
  }

  private void writeToScreen(HttpServletRequest req, HttpServletResponse resp, String message,
                             String view) throws IOException {
    writeToScreenWithMode(req, resp, message, null, view);
  }

  private void writeToScreenWithMode(HttpServletRequest req, HttpServletResponse resp,
                                     String message, String mode, String view)
      throws IOException {
    writeToScreen(req, resp, message, mode, view, "/s/message.jsp");
  }
}

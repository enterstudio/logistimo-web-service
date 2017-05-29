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

import com.logistimo.AppFactory;
import com.logistimo.bulkuploads.BulkUploadMgr;
import com.logistimo.bulkuploads.MnlTransactionUtil;
import com.logistimo.dao.JDOUtils;
import com.logistimo.services.blobstore.BlobInfo;
import com.logistimo.services.blobstore.BlobstoreService;
import com.logistimo.services.mapred.IMapredService;
import com.logistimo.services.taskqueue.ITaskService;

import org.apache.commons.lang.StringUtils;
import com.logistimo.config.models.DomainConfig;
import com.logistimo.config.models.InventoryConfig;
import com.logistimo.entity.IUploaded;
import com.logistimo.bulkuploads.BulkImportMapperContants;
import com.logistimo.pagination.Navigator;
import com.logistimo.pagination.PageParams;
import com.logistimo.pagination.Results;
import com.logistimo.security.SecureUserDetails;
import com.logistimo.api.security.SecurityMgr;
import com.logistimo.services.ObjectNotFoundException;
import com.logistimo.services.Resources;
import com.logistimo.services.ServiceException;
import com.logistimo.services.Services;
import com.logistimo.services.UploadService;
import com.logistimo.services.impl.UploadServiceImpl;
import com.logistimo.api.servlets.TransUploadServlet;

import com.logistimo.constants.Constants;
import com.logistimo.utils.LocalDateUtil;
import com.logistimo.utils.NumberUtil;
import com.logistimo.api.util.SessionMgr;
import com.logistimo.exception.TaskSchedulingException;
import com.logistimo.logger.XLog;
import com.logistimo.api.builders.UploadDataViewBuilder;
import com.logistimo.exception.BadRequestException;
import com.logistimo.exception.InvalidServiceException;
import com.logistimo.api.models.UploadModel;
import com.logistimo.users.service.UsersService;
import com.logistimo.users.service.impl.UsersServiceImpl;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

import javax.servlet.http.HttpServletRequest;

/**
 * Created by mohan raja on 08/01/15
 */

@Controller
@RequestMapping("/bulk")
public class BulkUploadController {

  private static final XLog xLogger = XLog.getLog(BulkUploadController.class);
  private static final String DONE_CALLBACK_URL = "/task/upload";
  private static final String CONFIGNAME_BULKIMPORTER = "BulkImporter";

  UploadDataViewBuilder builder = new UploadDataViewBuilder();
  private IMapredService mapredService = AppFactory.get().getMapredService();
  private ITaskService taskService = AppFactory.get().getTaskService();
  private BlobstoreService blobstoreService = AppFactory.get().getBlobstoreService();

  @RequestMapping(value = "/checkMUStatus", method = RequestMethod.GET)
  public
  @ResponseBody
  boolean checkManualUploadStatus(HttpServletRequest request) {
    SecureUserDetails sUser = SecurityMgr.getUserDetails(request.getSession());
    String userId = sUser.getUsername();
    Long domainId = SessionMgr.getCurrentDomain(request.getSession(), userId);
    DomainConfig dc = DomainConfig.getInstance(domainId);
    InventoryConfig ic = dc.getInventoryConfig();
    return ic.getManualTransConfig() != null && ic
        .getManualTransConfig().enableManualUploadInvDataAndTrans;
  }

  @RequestMapping(value = "/uploadstatus", method = RequestMethod.GET)
  public
  @ResponseBody
  UploadModel getUploadStatus(@RequestParam String type, HttpServletRequest request) {
    UploadModel model = new UploadModel();

    final int UPLOAD_ELAPSE_TIME_MIN = 15;
    SecureUserDetails sUser = SecurityMgr.getUserDetails(request.getSession());
    String userId = sUser.getUsername();
    Long domainId = SessionMgr.getCurrentDomain(request.getSession(), userId);
    Locale locale = sUser.getLocale();
    String timezone = sUser.getTimezone();
    ResourceBundle backendMessages = Resources.get().getBundle("BackendMessages", locale);

    if (type.equals(BulkUploadMgr.TYPE_TRANSACTIONS)) {
      DomainConfig dc = DomainConfig.getInstance(domainId);
      InventoryConfig ic = dc.getInventoryConfig();
      if (ic.getManualTransConfig() != null && ic
          .getManualTransConfig().enableManualUploadInvDataAndTrans) {
        type = BulkUploadMgr.TYPE_TRANSACTIONS_CUM_INVENTORY_METADATA;
      }
    }
    model.ty = type;
    model.tnm = BulkUploadMgr.getDisplayName(type, locale);

    IUploaded uploaded = BulkUploadMgr.getUploaded(domainId, type, userId);
    model.iu = uploaded != null;

    List<BulkUploadMgr.ErrMessage> errors = null;
    boolean prevJobComplete = true;
    int jobStatus = IUploaded.STATUS_DONE;
    if (model.iu) {
      model.id = uploaded.getId();
      jobStatus = uploaded.getJobStatus();
      prevJobComplete = jobStatus == IUploaded.STATUS_DONE;
      errors = BulkUploadMgr.getErrorMessageObjects(uploaded.getId());
      if (!prevJobComplete) {
        Date now = new Date();
        if (((now.getTime() - uploaded.getTimeStamp().getTime()) / (1000 * 60))
            >= UPLOAD_ELAPSE_TIME_MIN) {
          prevJobComplete = true;
          jobStatus = IUploaded.STATUS_DONE;

          UploadService svc;
          try {
            svc = Services.getService(UploadServiceImpl.class);
            IUploaded u = svc.getUploadedByJobId(model.id);
            if (u != null) {
              u.setJobStatus(IUploaded.STATUS_DONE);
              svc.updateUploaded(u);
            }
          } catch (Exception e) {
            xLogger
                .warn("{0} when getting uploaded object by job ID {1}: {2}", e.getClass().getName(),
                    model.id, e.getMessage(), e);
          }

        }
      }
      if (errors != null) {
        for (BulkUploadMgr.ErrMessage error : errors) {
          model.addError(error.offset, BulkUploadMgr.getOpDisplayName(error.operation, locale),
              error.messages, error.csvLine);
        }
      }
      model.fnm = uploaded.getFileName();
      model.utm = LocalDateUtil.format(uploaded.getTimeStamp(), locale, timezone);
      // Add user details
      model.uid = uploaded.getUserId();
      try {
        UsersService as = Services.getService(UsersServiceImpl.class);
        model.unm = as.getUserAccount(model.uid).getFullName();
      } catch (Exception e) {
        xLogger.warn("{0} when getting uploaded user details {1}: {2}", e.getClass().getName(),
            model.uid, e.getMessage(), e);
      }
    }
    model.ipc = prevJobComplete;
    model.js = jobStatus;
    model.jsnm = BulkUploadMgr.getJobStatusDisplay(jobStatus, locale);
    model.ierr = errors != null && !errors.isEmpty();
    model.errcnt = errors == null ? 0 : errors.size();

    if (BulkUploadMgr.TYPE_USERS.equals(type) || BulkUploadMgr.TYPE_MATERIALS.equals(type)
        || BulkUploadMgr.TYPE_INVENTORY.equals(type) || BulkUploadMgr.TYPE_ASSETS.equals(type)) {
      model.iut = BulkUploadMgr.TYPE_USERS.equals(type);
      model.mp = "#/setup/" + type;
    } else if (BulkUploadMgr.TYPE_KIOSKS.equals(type)) {
      model.mp = "#/setup/entities";
    } else if (BulkUploadMgr.TYPE_TRANSACTIONS.equals(type)
        || BulkUploadMgr.TYPE_TRANSACTIONS_CUM_INVENTORY_METADATA.equals(type)) {
      model.mp = "#/inventory/transactions";
    }
    return model;
  }

  @RequestMapping(value = "/upload", method = RequestMethod.GET)
  public
  @ResponseBody
  String upload(HttpServletRequest request) {
    return blobstoreService.createUploadUrl(request.getServletPath() + request.getPathInfo());
  }

  @RequestMapping(value = "/upload", method = RequestMethod.POST)
  public
  @ResponseBody
  String uploadCallback(@RequestParam String type, HttpServletRequest request)
      throws ServiceException {
    Map<String, List<String>> blobMap = blobstoreService.getUploads(request);
    SecureUserDetails sUser = SecurityMgr.getUserDetails(request.getSession());
    Locale locale = sUser.getLocale();
    ResourceBundle backendMessages = Resources.get().getBundle("BackendMessages", locale);
    String blobKeyStr = null;
    if (blobMap != null) {
      List<String> keys = blobMap.get("data");
      if (keys != null && keys.size() > 0) {
        blobKeyStr = keys.get(0);
      }
    }
    if (blobKeyStr == null) {
      xLogger.severe("Error in uploading file");
      throw new BadRequestException(backendMessages.getString("fileupload.error"));
    }
    Long domainId = SessionMgr.getCurrentDomain(request.getSession(), sUser.getUsername());
    String userId = sUser.getUsername();
    String localeStr = sUser.getLocale().toString();
    String version = "0";
    BlobInfo binfo = blobstoreService.getBlobInfo(blobKeyStr);
    String filename = userId + "." + type;
    if (binfo != null) {
      filename = binfo.getFilename();
    }
    IUploaded uploaded;
    UploadService svc = null;
    String uploadedKey = BulkUploadMgr.getUploadedKey(domainId, type, userId);
    boolean isUploadedNew = false;
    try {
      svc = Services.getService(UploadServiceImpl.class);
      uploaded = svc.getUploaded(uploadedKey);
      blobstoreService.remove(uploaded.getBlobKey());
      BulkUploadMgr.deleteUploadedMessage(uploadedKey);
    } catch (ObjectNotFoundException e) {
      uploaded = JDOUtils.createInstance(IUploaded.class);
      uploaded.setId(uploadedKey);
      isUploadedNew = true;
    } catch (Exception e) {
      xLogger.severe("{0} while trying to get UploadService or getting Uploaded for key {1}: {2}",
          e.getClass().getName(), uploadedKey, e.getMessage());
      throw new InvalidServiceException(
          backendMessages.getString("error.in") + " " + e.getClass().getName() + " "
              + backendMessages.getString("uploaded.error") + uploadedKey);
    }
    Date now = new Date();
    uploaded.setBlobKey(blobKeyStr);
    uploaded.setDomainId(domainId);
    uploaded.setFileName(filename);
    uploaded.setLocale(localeStr);
    uploaded.setTimeStamp(now);
    uploaded.setType(IUploaded.BULKUPLOAD);
    uploaded.setUserId(userId);
    uploaded.setVersion(version);
    try {
      String jobId = startBulkImport(type, blobKeyStr, userId, domainId);
      if (jobId != null) {
        uploaded.setJobId(jobId);
        uploaded.setJobStatus(IUploaded.STATUS_PENDING);
      }
      if (isUploadedNew) {
        svc.addNewUpload(uploaded);
      } else {
        svc.updateUploaded(uploaded);
      }
    } catch (Exception e) {
      xLogger.severe("{0} when scheduling upload task for type {1} by user {2}: {3}",
          e.getClass().getName(), type, sUser.getUsername(), e.getMessage());
      throw new InvalidServiceException(
          backendMessages.getString("error.in") + " " + e.getClass().getName() + " "
              + backendMessages.getString("scheduling.upload") + " " + type + " " + backendMessages
              .getString("by.user") + " " + sUser.getUsername());
    }
    xLogger.fine("Exiting scheduleBulkUpload");
    return backendMessages.getString("fileupload.success");
  }

  @RequestMapping(value = "/")
  private String startBulkImport(String type, String blobKeyStr, String userId, Long domainId)
      throws IOException, TaskSchedulingException {
    xLogger
        .fine("Entered startBulkImport: type = {0}, blobKeyStr = {1}, userId = {2}, domainId = {3}",
            type, blobKeyStr, userId, domainId);
    if (BulkUploadMgr.TYPE_TRANSACTIONS.equals(type)
        || BulkUploadMgr.TYPE_TRANSACTIONS_CUM_INVENTORY_METADATA.equals(type)) {
      Map<String, String> params = new HashMap<String, String>();
      params.put("action", TransUploadServlet.ACTION_TRANSACTIONIMPORT);
      params.put("userid", userId);
      params.put("domainid", domainId.toString());
      params.put("blobkey", blobKeyStr);
      params.put("type", type);
      taskService.schedule(ITaskService.QUEUE_DEFAULT, "/task/transupload", params, null,
          ITaskService.METHOD_POST, -1);
      return String.valueOf(NumberUtil.generateFiveDigitRandomNumber());
    }
    Map<String, String> params = new HashMap<String, String>();
    params.put(IMapredService.PARAM_BLOBKEY, blobKeyStr);
    params.put(BulkImportMapperContants.TYPE, type);
    params.put(BulkImportMapperContants.USERID, userId);
    params.put(BulkImportMapperContants.DOMAINID, domainId.toString());
    params.put(IMapredService.PARAM_DONECALLBACK,
        DONE_CALLBACK_URL); // callback to notify on completion
    params.put(IMapredService.PARAM_SHARDCOUNT, "8");
    params.put(IMapredService.PARAM_INPUTPROCESSINGRATE, "100");
    String jobId = mapredService.startJob(CONFIGNAME_BULKIMPORTER, params);
    xLogger.fine("Exiting startBulkImport");
    return jobId;
  }

  @RequestMapping(value = "/view", method = RequestMethod.GET)
  public
  @ResponseBody
  Results getUploadedData(@RequestParam(required = false) Long eid,
                          @RequestParam(defaultValue = PageParams.DEFAULT_OFFSET_STR) int offset,
                          @RequestParam(defaultValue = PageParams.DEFAULT_SIZE_STR) int size,
                          @RequestParam(required = false) String from,
                          @RequestParam(required = false) String to,
                          HttpServletRequest request) {
    SecureUserDetails sUser = SecurityMgr.getUserDetails(request.getSession());
    String userId = sUser.getUsername();
    Locale locale = sUser.getLocale();
    ResourceBundle backendMessages = Resources.get().getBundle("BackendMessages", locale);
    String timezone = sUser.getTimezone();
    Long domainId = SessionMgr.getCurrentDomain(request.getSession(), userId);
    DomainConfig dc = DomainConfig.getInstance(domainId);
    try {
      Navigator
          navigator =
          new Navigator(request.getSession(), "BulkUploadController.getUploadedData", offset, size,
              "", 0);
      PageParams pageParams = new PageParams(navigator.getCursor(offset), offset, size);
      Date
          fromDate =
          StringUtils.isBlank(from) ? null
              : LocalDateUtil.parseCustom(from, Constants.DATE_FORMAT, dc.getTimezone());
      Date
          toDate =
          StringUtils.isBlank(to) ? null
              : LocalDateUtil.parseCustom(to, Constants.DATE_FORMAT, dc.getTimezone());
      Results
          results =
          MnlTransactionUtil
              .getManuallyUploadedTransactions(domainId, eid, fromDate, toDate, pageParams);
      if (results == null) {
        return null;
      }
      navigator.setResultParams(results);
      return new Results(builder.build(results, locale, eid, timezone), "viewbulk", -1, offset);
    } catch (Exception e) {
      xLogger.warn("Exception: " + e.getClass().getName() + ": " + e.getMessage(), e);
      throw new InvalidServiceException(backendMessages.getString("fileupload.fetch.error"));
    }
  }
}

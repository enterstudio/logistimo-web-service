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
import com.logistimo.api.security.SecurityMgr;
import com.logistimo.api.util.SessionMgr;
import com.logistimo.assets.AssetUtil;
import com.logistimo.assets.entity.IAsset;
import com.logistimo.assets.models.Temperature;
import com.logistimo.assets.models.TemperatureResponse;
import com.logistimo.bulkuploads.BulkUploadMgr;
import com.logistimo.communications.MessageHandlingException;
import com.logistimo.communications.service.EmailService;
import com.logistimo.communications.service.MessageService;
import com.logistimo.config.entity.IConfig;
import com.logistimo.config.models.AssetSystemConfig;
import com.logistimo.config.models.ConfigurationException;
import com.logistimo.config.models.DomainConfig;
import com.logistimo.config.service.ConfigurationMgmtService;
import com.logistimo.config.service.impl.ConfigurationMgmtServiceImpl;
import com.logistimo.constants.CharacterConstants;
import com.logistimo.constants.Constants;
import com.logistimo.dao.JDOUtils;
import com.logistimo.domains.service.DomainsService;
import com.logistimo.domains.service.impl.DomainsServiceImpl;
import com.logistimo.entities.service.EntitiesService;
import com.logistimo.entities.service.EntitiesServiceImpl;
import com.logistimo.entity.IJobStatus;
import com.logistimo.entity.IUploaded;
import com.logistimo.exports.BulkExportMgr;
import com.logistimo.exports.BulkExportMgr.ExportParams;
import com.logistimo.exports.handlers.OrderExportHandler;
import com.logistimo.exports.pagination.processor.ExportProcessor;
import com.logistimo.logger.XLog;
import com.logistimo.materials.service.MaterialCatalogService;
import com.logistimo.materials.service.impl.MaterialCatalogServiceImpl;
import com.logistimo.orders.entity.IOrder;
import com.logistimo.orders.service.OrderManagementService;
import com.logistimo.orders.service.impl.OrderManagementServiceImpl;
import com.logistimo.pagination.Executor;
import com.logistimo.pagination.PageParams;
import com.logistimo.pagination.PagedExec;
import com.logistimo.pagination.PagedExec.Finalizer;
import com.logistimo.pagination.QueryParams;
import com.logistimo.pagination.Results;
import com.logistimo.pagination.processor.Processor;
import com.logistimo.reports.ReportsConstants;
import com.logistimo.reports.generators.ReportData;
import com.logistimo.reports.generators.ReportDataGenerator;
import com.logistimo.reports.generators.ReportDataGeneratorFactory;
import com.logistimo.reports.utils.ReportsUtil;
import com.logistimo.security.SecureUserDetails;
import com.logistimo.services.ObjectNotFoundException;
import com.logistimo.services.ServiceException;
import com.logistimo.services.Services;
import com.logistimo.services.UploadService;
import com.logistimo.services.blobstore.BlobstoreService;
import com.logistimo.services.impl.PMF;
import com.logistimo.services.impl.UploadServiceImpl;
import com.logistimo.services.storage.StorageUtil;
import com.logistimo.services.taskqueue.ITaskService;
import com.logistimo.services.utils.ConfigUtil;
import com.logistimo.users.UserUtils;
import com.logistimo.users.entity.IUserAccount;
import com.logistimo.users.service.UsersService;
import com.logistimo.users.service.impl.UsersServiceImpl;
import com.logistimo.utils.JobUtil;
import com.logistimo.utils.LocalDateUtil;
import com.logistimo.utils.StringUtil;

import org.apache.commons.lang.StringUtils;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.URLDecoder;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.TimeZone;

import javax.jdo.PersistenceManager;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Export data of various types in different formats
 *
 * @author Arun
 */
@SuppressWarnings("serial")
public class ExportServlet extends SgServlet {

  public static final String ACTION_BATCHEXPORT = "be";
  public static final String ACTION_SCHEDULEBATCHEXPORT = "sbe";
  public static final String ACTION_SCHEDULEREPORTEXPORT = "sre";
  private static final XLog xLogger = XLog.getLog(ExportServlet.class);
  // Actions
  private static final String ACTION_DOWNLOAD = "dl";
  private static final String ACTION_BULKUPLOADFORMATEXPORT = "bufe";
  private static final String ACTION_FINALIZEEXPORT = "fnlzexp";

  // URL
  private static final String EXPORT_TASK_URL = "/task/export";

  private static ITaskService taskService = AppFactory.get().getTaskService();

  private BlobstoreService blobstoreService = AppFactory.get().getBlobstoreService();

  // Get a CSV of email IDs
  private static String getEmailCSV(String userIdsCSV) {
    if (userIdsCSV == null || userIdsCSV.isEmpty()) {
      return userIdsCSV;
    }
    PersistenceManager pm = PMF.get().getPersistenceManager();
    List<String> userIds = StringUtil.getList(userIdsCSV);
    Iterator<String> it = userIds.iterator();
    String emailCSV = "";
    try {
      while (it.hasNext()) {
        String userId = it.next();
        try {
          IUserAccount u = JDOUtils.getObjectById(IUserAccount.class, userId, pm);
          if (u.isEnabled()) {
            String email = u.getEmail();
            if (email == null || email.isEmpty()) {
              continue;
            }
            if (!emailCSV.isEmpty()) {
              emailCSV += ",";
            }
            emailCSV += email;
          }
        } catch (Exception e) {
          xLogger.warn("{0} when getting user {1} for his/her email for export: {1}",
              e.getClass().getName(), userId, e.getMessage());
        }
      }
    } finally {
      pm.close();
    }
    return emailCSV;
  }

  @Override
  protected void processGet(HttpServletRequest request, HttpServletResponse response,
                            ResourceBundle backendMessages, ResourceBundle messages)
      throws ServletException, IOException, ServiceException {
    xLogger.fine("Entered doGet");
    String action = request.getParameter("action");
    if (action == null || action.isEmpty()) {
      String type = request.getParameter("type");
      if (BulkExportMgr.TYPE_ORDERS.equals(type)) {
        exportOrders(request, response);
      } else {
        xLogger.warn("Unknown type: {0}", type);
      }
    } else if (ACTION_DOWNLOAD.equals(action)) {
      serveFile(request, response);
    } else if (ACTION_SCHEDULEBATCHEXPORT.equals(action)) {
      scheduleBatchExport(request, response, backendMessages, messages);
    } else if (ACTION_BATCHEXPORT.equals(action)) {
      batchExport(request, response, backendMessages, messages);
    } else if (ACTION_BULKUPLOADFORMATEXPORT.equals(action)) {
      exportBulkUploadFormat(request, response, messages);
    } else if (ACTION_SCHEDULEREPORTEXPORT.equals(action)) {
      scheduleReportExport(request, response, backendMessages, messages);
    } else if (ACTION_FINALIZEEXPORT.equals(action)) {
      finalizeExport(request, response, backendMessages, messages);
    } else {
      xLogger.severe("Unknown action: " + action);
    }
    xLogger.fine("Exiting doGet");
  }

  @Override
  protected void processPost(HttpServletRequest request, HttpServletResponse response,
                             ResourceBundle backendMessages, ResourceBundle messages)
      throws ServletException, IOException,
      ServiceException {
    processGet(request, response, backendMessages, messages);
  }

  // Export orders in the specified format
  private void exportOrders(HttpServletRequest request, HttpServletResponse response)
      throws IOException, ServiceException {
    xLogger.fine("Entered exportOrders");
    // Get the export format
    String format = request.getParameter("format");
    if ("csv".equals(format)) {
      exportOrdersCSV(request, response);
    }
    xLogger.fine("Existing exportOrders");
  }

  // Export orders in CSV format
  private void exportOrdersCSV(HttpServletRequest request, HttpServletResponse response)
      throws IOException, ServiceException {
    xLogger.fine("Entered exportOrdersCSV");
    // Get the values
    String valuesCSV = request.getParameter("values");
    // Get locale/timezone of user
    SecureUserDetails sUser = SecurityMgr.getUserDetails(request.getSession());
    Locale locale = sUser.getLocale();
    String timezone = sUser.getTimezone();
    Long domainId = SessionMgr.getCurrentDomain(request.getSession(), sUser.getUsername());
    // Get the fields config., if any
    DomainConfig dc = DomainConfig.getInstance(domainId);
    // Init. services
    OrderManagementService oms = Services.getService(OrderManagementServiceImpl.class);
    // Get the order IDs
    String[] oids = valuesCSV.split(",");
    String csv = "";
    for (int i = 0; i < oids.length; i++) {
      try {
        IOrder o = oms.getOrder(Long.valueOf(oids[i]), true);
        OrderExportHandler orderExportHandler = new OrderExportHandler(o);
        if (i == 0) {
          csv = orderExportHandler.getCSVHeader(locale, dc, null);
        }
        csv += "\n" + orderExportHandler.toCSV(locale, timezone, dc, null);
      } catch (NumberFormatException | ObjectNotFoundException e) {
        // Skip this item
      }
    }
    String
        fileName =
        LocalDateUtil.getNameWithDate("Orders", new Date(), locale, timezone) + ".csv";
    // Send CSV
    response.setContentType("text/csv");
    response.addHeader("Content-Disposition", "inline; filename=" + fileName);
    response.setCharacterEncoding("UTF-8");
    PrintWriter pw = response.getWriter();
    pw.write(csv);
    pw.close();
    xLogger.fine("Exiting exportOrdersCSV");
  }

  // Schedule a batch export as a task (typically done from a UI)
  private void scheduleBatchExport(HttpServletRequest req, HttpServletResponse resp,
                                   ResourceBundle backendMessages, ResourceBundle messages) {
    xLogger.fine("Entered scheduleBatchExport");
    // View params.
    String view = req.getParameter("view");
    String subview = req.getParameter("subview");
    String sourceUserId = req.getParameter("sourceuserid"); // user who requested export
    // Get the task URL params. for export
    Map<String, String> params = taskService.getParamsFromQueryString(req.getQueryString());
    // Update the action to batch export
    params.put("action", ACTION_BATCHEXPORT);
    // Add the header to target export backend
    Map<String, String> headers = BulkExportMgr.getExportBackendHeader();
    xLogger.fine("task url params: {0}", params);
    String message = null;
    try {
      SecureUserDetails sUser = SecurityMgr.getUserDetails(req.getSession());
      // Schedule task for export
      taskService.schedule(ITaskService.QUEUE_EXPORTER, EXPORT_TASK_URL, params, headers,
          ITaskService.METHOD_POST, sUser.getDomainId(), sourceUserId, "BATCH_EXPORT");
      // Get the user for his email
      UsersService as = Services.getService(UsersServiceImpl.class);
      IUserAccount sourceUser = as.getUserAccount(sourceUserId);
      // Write message to screen
      message =
          backendMessages.getString("export.success1") + " <b>" + sourceUser.getEmail() + "</b> "
              + backendMessages.getString("export.success2");
      message += " [<a href=\"javascript:history.go(-1)\">" + messages.getString("back") + "</a>]";
    } catch (Exception e) {
      xLogger.severe("{0} when scheduling export task with params {1}: {2}", e.getClass().getName(),
          params, e.getMessage());
      message = "System error occurred. Please contact your administrator.";
    }
    // Write to screen
    // Get back to user, if this came from the browser (i.e. not the task)
    if (!req.getRequestURI().contains("/task/")) {
      try {
        writeToScreen(req, resp, message, null, view, subview, "/s/message.jsp");
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
    xLogger.fine("Entered scheduleBatchExport");
  }

  // Do a batch export of items using the paginator
  private void batchExport(HttpServletRequest req, HttpServletResponse resp,
                           ResourceBundle backendMessages, ResourceBundle messages) {
    xLogger.fine("Entered batchExport");
    // Get request parameters
    String
        exportType =
        req.getParameter(
            "type"); // export type of data exported (e.g. orders/transactions/inventory/asset) - BulkExportMgr.TYPE_XXX
    String sourceUserId = req.getParameter("sourceuserid"); // user who requested export
    String userIds = req.getParameter("userids"); // user Ids of recepients of the export
    String userTags = req.getParameter("usertags"); // user Tags
    String domainIdStr = req.getParameter("domainid"); // mandatory
    // Date params., for logging purposes
    String
        fromDateStr =
        req.getParameter("from"); // from date (optional) - see Constants.DATETIME_FORMAT
    String toDateStr = req.getParameter("to"); // to date (optional) - see Constants.DATETIME_FORMAT
    boolean isReportType = (req.getParameter("reports") != null);
    boolean isAbnormalStockReport = (req.getParameter("abnormalreport") != null);
    boolean _fromCassandra = ConfigUtil.getBoolean("reports.cassandra", false);
    if (StringUtils.isEmpty(exportType) || StringUtils.isEmpty(domainIdStr) ||
        StringUtils.isEmpty(sourceUserId) || StringUtils.isEmpty(userIds) && StringUtils
        .isEmpty(userTags)) {
      xLogger.severe(
          "One or more mandatory parameters for batch export are missing: type = {0}, domainId = {1}, sourceUserId = {2}, userIds = {3}, userTags = {4}",
          exportType, domainIdStr, sourceUserId, userIds, userTags);
      return;
    }
    // Domain Id
    Long domainId = Long.valueOf(domainIdStr);
    BulkExportMgr.ExportParams exportParams = getExportParams(req);
    if (exportParams == null) {
      xLogger.severe("Failed to created ExportParams from request.");
      return;
    }
    if ("powerdata".equals(exportType)) {
      int size = 500;
      try {
        ConfigurationMgmtService
            cms =
            Services.getService(ConfigurationMgmtServiceImpl.class);
        IConfig c = cms.getConfiguration(IConfig.GENERALCONFIG);
        String sz = c.getString("pdexportsize");
        if (StringUtils.isNotBlank(sz)) {
          size = Integer.parseInt(sz);
        }
      } catch (ServiceException | ObjectNotFoundException e) {
        xLogger.warn("Error in getting system configuration while reading powerdata", e);
      } catch (NumberFormatException e) {
        xLogger.warn("Error in parsing pdexportsize", e);
      }
      String deviceId = req.getParameter("deviceid");
      try {
        if (deviceId != null) {
          deviceId =
              AssetUtil
                  .decodeURLParameters(URLDecoder.decode(req.getParameter("deviceid"), "UTF-8"));
        }
      } catch (Exception e) {
        xLogger.warn("Error in getting device ID: {0}", deviceId, e);
        JobUtil.setJobFailed(Long.valueOf(req.getParameter("jobid")),
            "Error in exporting raw temperature and power data for: " + deviceId);
        return;
      }
      //String abc = URLDecoder(req.getParameter("deviceid"));
      String vId = req.getParameter("assetvendor");
      String
          sensor =
          req.getParameter(
              "sensor"); // if assetype = monitored asset: sensor id, if monitoring asset: monitoring position id
      String
          asType =
          req.getParameter(
              "assetype"); // Asset Id: id of Temperature logger/ ILR/ Walk in cooler/freezer
      String to = req.getParameter("to");
      String tz = req.getParameter("tz");
      String retry = req.getParameter("retry");
      String
          asset =
          req.getParameter(
              "asset"); // Asset name i.e., Temperature logger/ ILR/ Walk in cooler/freezer
      String sensorName = req.getParameter("snsname"); // Sensor name i.e., A,B,C,D
      if(sensorName == null) {
        xLogger.warn("Sensor name is mandatory to export power data");
        JobUtil.setJobFailed(Long.valueOf(req.getParameter("jobid")),
            "Error in exporting raw temperature and power data for: " + deviceId);
        return;
      }

      int retryCount = retry != null ? Integer.valueOf(retry) : 0;
      long end = LocalDateUtil.getCurrentTimeInSeconds(tz);
      long start;
      int
          assetModelId =
          req.getParameter("assetmodelid") != null ? Integer
              .parseInt(req.getParameter("assetmodelid")) : 0;
      SimpleDateFormat sdf = new SimpleDateFormat(Constants.DATE_FORMAT);
      String exportParamsJson;
      try {
        if (req.getParameter("from") == null) {
          Calendar cal = GregorianCalendar.getInstance(TimeZone.getTimeZone(tz));
          if (to != null && !to.isEmpty()) {
            cal.setTime(sdf.parse(to));
            cal.add(Calendar.DATE, 1);
            cal = LocalDateUtil.resetTimeFields(cal);
            end = cal.getTimeInMillis() / 1000;
          }
          cal.add(Calendar.DATE, -30);
          cal = LocalDateUtil.resetTimeFields(cal);
          start = cal.getTimeInMillis() / 1000;
        } else {
          start = Long.valueOf(req.getParameter("from"));
          end = Long.valueOf(req.getParameter("to"));
        }

        if (req.getParameter("gcsfilename") != null) {
          exportParams.gcsFilename = req.getParameter("gcsfilename");
          exportParams.filename = req.getParameter("filename");
        }
        if (asset == null) {
          AssetSystemConfig asc = AssetSystemConfig.getInstance();
          int aType = Integer.parseInt(asType);
          asset = asc.getAsset(aType).getName();
          assetModelId =
              asc.getAsset(aType).monitoringPositions == null ? IAsset.MONITORING_ASSET
                  : IAsset.MONITORED_ASSET;
        }
      } catch (ConfigurationException | ParseException e) {
        xLogger.severe("Error in getting parameters from request header:", e);
        JobUtil.setJobFailed(Long.valueOf(req.getParameter("jobid")),
            "Error in exporting raw temperature and power data for: " + deviceId);
        return;
      }
      exportParams.assetId = deviceId;
      exportParams.asseTyNm = asset;
      exportParams.sensorName = sensorName;
      exportParamsJson = exportParams.toJSONString();

      PersistenceManager pm = PMF.get().getPersistenceManager();
      try {
        Processor processor = new ExportProcessor();
        Boolean hasMoreData = true;
        while (hasMoreData) {
          TemperatureResponse
              respData =
              AssetUtil
                  .getTemperatureResponse(vId, deviceId, sensor, assetModelId, start, end, 1, size);
          if (respData != null) {
            List<Temperature> tempData = respData.data;
            if (tempData.size() > 0) {
              end = (long) tempData.get(tempData.size() - 1).time;
              exportParamsJson =
                  processor.process(domainId, new Results(tempData, null), exportParamsJson, pm);
            }
          }
          if (respData == null || respData.data.size() < size) {
            hasMoreData = false;
          }
        }
      } catch (Exception e) {
        if (retryCount <= 2) {
          retryCount++;
          JSONObject obj = new JSONObject(exportParamsJson);
          String gcsfilename = null;
          String filename = null;
          try {
            gcsfilename = obj.getString("gcsfn");
            filename = obj.getString("fn");
          } catch (Exception ignored) {
          }
          Map<String, String> retryParams = new HashMap<>(19);
          retryParams.put("sensor", req.getParameter("sensor"));
          retryParams.put("assetvendor", req.getParameter("assetvendor"));
          retryParams.put("sourceuserid", req.getParameter("sourceuserid"));
          retryParams.put("assetype", req.getParameter("assetype"));
          retryParams.put("action", req.getParameter("action"));
          retryParams.put("domainid", req.getParameter("domainid"));
          retryParams.put("userids", req.getParameter("userids"));
          retryParams.put("type",
              req.getParameter("type")); // Type of the exported sheet i.e., powerdata, assets etc.
          retryParams.put("deviceid", req.getParameter("deviceid"));
          retryParams.put("jobid", req.getParameter("jobid"));
          retryParams.put("from", String.valueOf(start));
          retryParams.put("to", String.valueOf(end));
          retryParams.put("asset", asset);
          retryParams.put("snsname", sensorName);
          retryParams.put("tz", tz);
          retryParams.put("retry", String.valueOf(retryCount));
          if (StringUtils.isNotEmpty(gcsfilename)) {
            retryParams.put("gcsfilename", gcsfilename);
          }
          if (StringUtils.isNotEmpty(gcsfilename)) {
            retryParams.put("filename", filename);
          }
          retryParams.put("assetmodelid", String.valueOf(assetModelId));
          ITaskService taskService = AppFactory.get().getTaskService();
          try {
            xLogger
                .warn("Error in exporting raw temperature and power data for: {0}. Retry count:{1}",
                    deviceId, retryCount, e);
            taskService.schedule(ITaskService.QUEUE_EXPORTER, EXPORT_TASK_URL, retryParams, null,
                ITaskService.METHOD_POST, System.currentTimeMillis() + 60_000);
          } catch (Exception e1) {
            xLogger.warn("Error in scheduling export task", e1);
          }
        } else {
          xLogger.severe(
              "Retry limit reached: {0}. Error in exporting raw temperature and power data for: {1}",
              retryCount, deviceId, e);
          JobUtil.setJobFailed(Long.valueOf(req.getParameter("jobid")),
              "Error in exporting raw temperature and power data for: " + deviceId);
        }
        return;
      } finally {
        pm.close();
      }
      try {
        Finalizer finalizer = new Finalizer();
        finalizer.url = EXPORT_TASK_URL + "?action=" + ACTION_FINALIZEEXPORT;
        finalizer.queue = ITaskService.QUEUE_EXPORTER;
        PagedExec.finalize(finalizer, exportParamsJson);
      } catch (Exception e) {
        xLogger.severe("Error in exporting raw temperature and power data for: {0}", deviceId, e);
      }
    } else if (_fromCassandra && isReportType && !isAbnormalStockReport) {
      xLogger.fine("Entering Cassandra Export ");
      SimpleDateFormat df = new SimpleDateFormat(Constants.DATE_FORMAT);
      Date from = null, to = null;
      if (fromDateStr != null && !fromDateStr.isEmpty()) {
        try {
          from = df.parse(fromDateStr);
        } catch (Exception e) {
          xLogger
              .warn("{0} when parsing from-date {1}...ignoring this: {2}", e.getClass().getName(),
                  fromDateStr, e.getMessage());
        }
      }
      if (toDateStr != null && !toDateStr.isEmpty()) {
        try {
          to = df.parse(toDateStr);
        } catch (Exception e) {
          xLogger.warn("{0} when parsing to-date {1}...ignoring this: {2}", e.getClass().getName(),
              fromDateStr, e.getMessage());
        }
      }
      xLogger.fine("from: {0}, to: {1}", from, to);
      String frequencyStr = req.getParameter("frequency");
      Map<String, Object> filters = ReportsUtil.getReportFilters(req);
      xLogger.fine("filters: {0}", filters);
      // Get domain config.
      DomainConfig dc = DomainConfig.getInstance(domainId);
      // Get the locale
      String sourceUserIdStr = req.getParameter("sourceuserid");
      Locale locale = dc.getLocale();
      String timezone = dc.getTimezone();
      if (sourceUserIdStr != null && !sourceUserIdStr.isEmpty()) {
        IUserAccount sourceUser = null;
        try {
          UsersService as = Services.getService(UsersServiceImpl.class);
          sourceUser = as.getUserAccount(sourceUserIdStr);
          locale = sourceUser.getLocale();
        } catch (Exception e) {
          xLogger
              .severe("{0} when getting source user {1} in domain {2}: {3}", e.getClass().getName(),
                  sourceUserIdStr, domainId, e.getMessage());
        }
      }
      String exportParamsJson = exportParams.toJSONString();
      PageParams pageParams = new PageParams(null, PageParams.DEFAULT_SIZE);
      xLogger.info(
          "Exporting {0} in domain {1} to users {2} initated by {3}; data duration from {4} to {5}",
          exportType, domainIdStr, userIds, sourceUserId, fromDateStr, toDateStr);
      try {
        ReportDataGenerator rdg = ReportDataGeneratorFactory.getInstanceV2(exportType);
        ReportData
            rd =
            rdg.getReportData(from, to, frequencyStr, filters, locale, timezone, pageParams, dc,
                sourceUserId);
        Results rs = new Results(rd.getResults(), null);
        ExportProcessor processor = new ExportProcessor();
        exportParamsJson = processor.process(domainId, rs, exportParamsJson, null);
      } catch (Exception e) {
        xLogger.severe(
            "{0} while getting report query params for type {1} in domain {2}. Message: {3}",
            e.getClass().getName(), exportType, domainId, e.getMessage());
      }

      // Finalizer
      try {
        Finalizer finalizer = new Finalizer();
        finalizer.url = EXPORT_TASK_URL + "?action=" + ACTION_FINALIZEEXPORT;
        finalizer.queue = ITaskService.QUEUE_EXPORTER;
        PagedExec.finalize(finalizer, exportParamsJson);
      } catch (Exception e) {
        xLogger.severe(
            "{0} while getting report query params for type {1} in domain {2}. Message: {3}",
            e.getClass().getName(), exportType, domainId, e.getMessage());
      }
    } else {
      // Get the query params. object
      QueryParams qp = null;
      try {
        qp = BulkExportMgr.getQueryParams(exportType, domainId, req);
      } catch (Exception e) {
        xLogger
            .severe("Invalid query params for export of {0} in domain {1}. Aborting...", exportType,
                domainId);
      }
      if (qp == null) {
        JobUtil.setJobFailed(Long.valueOf(req.getParameter("jobid")),
            "System error: Unable to generate export query");
        return;
      }
      if (qp.params != null && qp.params.get("fromParam") != null) {
        if (exportParams.from != null) {
          qp.params.put("fromParam", exportParams.from);
        }
      }
      if (qp.params != null && qp.params.get("toParam") != null) {
        if (exportParams.to != null) {
          qp.params.put("toParam", exportParams.to);
        }
      }
      xLogger.info("Printing query params... query: {0}, params: {1}", qp.query, qp.params);
      // Page params
      PageParams pageParams = new PageParams(null, PageParams.DEFAULT_SIZE);
      // Finalizer
      Finalizer finalizer = new Finalizer();
      finalizer.url = EXPORT_TASK_URL + "?action=" + ACTION_FINALIZEEXPORT;
      finalizer.queue = ITaskService.QUEUE_EXPORTER;
      xLogger.info(
          "Exporting {0} in domain {1} to users {2} initated by {3}; data duration from {4} to {5}",
          exportType, domainIdStr, userIds, sourceUserId, fromDateStr, toDateStr);
      try {
        String exportParamsJson = exportParams.toJSONString();
        xLogger.fine("exportParamsJson: {0}", exportParamsJson);
        Executor.exec(domainId, qp, pageParams, ExportProcessor.class.getName(), exportParamsJson,
            finalizer);
      } catch (Exception e) {
        xLogger.severe(
            "{0} when paged-execing data export for {1} in domain {2} to users {3}. Aborting...: {4}",
            e.getClass().getName(), exportType, domainId, userIds, e.getMessage());
      }
    }
    xLogger.fine("Exiting batchExport");
  }

  private void scheduleReportExport(HttpServletRequest req, HttpServletResponse resp,
                                    ResourceBundle backendMessages, ResourceBundle messages) {
    xLogger.fine("Entering scheduleReportExport");
    // Get request parameters
    String
        type =
        req.getParameter(
            "type"); // type of data exported (e.g. orders/transactions/inventory - BulkExportMgr.TYPE_XXX or report types such as transactioncounts, consumptiontrends, etc. - ReportsConstants.TYPE_XXX)
    String sourceUserId = req.getParameter("sourceuserid"); // user who requested export
    String userIds = req.getParameter("userids"); // user Ids of recepients of the export
    String domainIdStr = req.getParameter("domainid"); // mandatory
    if (type == null || type.isEmpty() || domainIdStr == null || domainIdStr.isEmpty() ||
        sourceUserId == null || sourceUserId.isEmpty() || userIds == null || userIds.isEmpty()) {
      xLogger.severe(
          "One or more mandatory parameters for schedule report export are missing: type = {0}, domainId = {1}, sourceUserId = {2}, userIds = {3}",
          type, domainIdStr, sourceUserId, userIds);
      return;
    }
    // Domain Id
    Long domainId = Long.valueOf(domainIdStr);
    IUserAccount sourceUser = null;
    try {
      UsersService as = Services.getService(UsersServiceImpl.class);
      sourceUser = as.getUserAccount(sourceUserId);
    } catch (Exception e) {
      xLogger.severe("{0} while getting account details for user {1} in domain {2}. Message: {3}",
          e.getClass().getName(), sourceUserId, domainId, e.getMessage());
    }
    // View params.
    String view = req.getParameter("view");
    String subview = req.getParameter("subview");
    // Date params., for logging purposes
    String
        fromDateStr =
        req.getParameter("from"); // from date (optional) - see Constants.DATETIME_FORMAT
    String toDateStr = req.getParameter("to"); // to date (optional) - see Constants.DATETIME_FORMAT
    // Frequency
    String frequency = req.getParameter("frequency");

    // Get filters
    String filters = req.getParameter("filters");
    Map<String, String[]> filterMap = ReportsUtil.getFilterMap(filters);
    if (filterMap == null || filterMap.isEmpty()) {
      if (filterMap == null) {
        filterMap = new HashMap<String, String[]>();
      }
      // Add a domain filter
      String[] domains = new String[1];
      domains[0] = domainId.toString();
      filterMap.put(ReportsConstants.FILTER_DOMAIN, domains);
    }
    String filtersQueryString = ReportsUtil.getQueryString(filterMap, null);
    String
        queryString =
        "action=" + ACTION_BATCHEXPORT + "&type=" + type + "&sourceuserid=" + sourceUserId
            + "&userids=" + userIds + "&domainid=" + domainIdStr;
    if (fromDateStr != null) {
      queryString += "&from=" + fromDateStr;
    }
    if (toDateStr != null) {
      queryString += "&to=" + toDateStr;
    }
    if (frequency == null || frequency
        .isEmpty()) // If the frequency is not specified, default it to 'monthly"
    {
      frequency = ReportsConstants.FREQ_MONTHLY;
    }
    queryString += "&frequency=" + frequency;
    // Append filtersQueryString to the queryString
    queryString += "&" + filtersQueryString + "&reports";
    xLogger.fine("queryString: {0}", queryString);
    Map<String, String> params = taskService.getParamsFromQueryString(queryString);
    if (params == null) {
      xLogger.severe("Invalid params {0} while scheduling report export task in domain {1}", params,
          domainIdStr);
      return;
    }
    xLogger.fine("params: {0}", params);
    String url = ExportServlet.EXPORT_TASK_URL;
    // Get export backend headers to target backend instance
    Map<String, String> headers = BulkExportMgr.getExportBackendHeader();
    try {
      taskService
          .schedule(taskService.QUEUE_EXPORTER, url, params, headers, taskService.METHOD_POST,
              domainId, sourceUserId, "EXPORT_REPORT");
      // Get back to user, if this came from the browser (i.e. not the task)
      String
          message =
          backendMessages.getString("export.success1") + " <b>" + sourceUser.getEmail() + "</b> "
              + backendMessages.getString("export.success2");
      // message += " [<a href=\"javascript:history.go(-1)\">" + messages.getString( "back" ) + "</a>]";
      // Write to screen
      try {
        xLogger.fine("Before calling writeToScreen. view: {0}, subview: {1}", view, subview);
        writeText(resp, message); //, null, view, subview, "/s/message.jsp" );
        xLogger.fine("After calling writeToScreen");
      } catch (IOException e) {
        xLogger.severe("IOExeption when writing to screen: {0}", e.getMessage());
      }
    } catch (Exception e) {
      xLogger.severe("{0} while sceduling report export task in domain {1}. Message: {2}",
          e.getClass().getName(), domainId, e.getMessage());
    }
    xLogger.fine("Exiting scheduleReportExport");
  }

  // Finalize an export and send the email notification
  private void finalizeExport(HttpServletRequest req, HttpServletResponse resp,
                              ResourceBundle backendMessages, ResourceBundle messages) {
    xLogger.fine("Entered finalizeExport");
    // Get the export parms. posted by the page-exed'ed export task
    String exportParamsJson = req.getParameter("output");
    ExportParams exportParams = null;
    try {
      exportParams = new BulkExportMgr.ExportParams(exportParamsJson);
    } catch (Exception e) {
      xLogger.severe("{0} when getting export params JSON {1} during finalization: {2}",
          e.getClass().getName(), exportParamsJson, e.getMessage());
      return;
    }
    // Get the source user
    IUserAccount u = null;
    UsersService as = null;
    EntitiesService es = null;
    try {
      as = Services.getService(UsersServiceImpl.class);
      es = Services.getService(EntitiesServiceImpl.class);
      u = as.getUserAccount(exportParams.sourceUserId);
    } catch (Exception e) {
      xLogger.warn("{0} when getting user {1} in domain {2}. Aborting export...: {3}",
          e.getClass().getName(), exportParams.sourceUserId, exportParams.domainId, e.getMessage());
      return;
    }
    // Create an record of the export
    Date now = new Date();
    // If notification is required, then notify
    String formattedTime = LocalDateUtil.format(now, exportParams.locale, exportParams.timezone);
    Long domainId = exportParams.domainId;
    List<String>
        userIds =
        UserUtils.getEnabledUniqueUserIds(domainId, exportParams.userIds, exportParams.userTags);
    String emailCSV;
    if (exportParams.emailId != null && !exportParams.emailId.isEmpty()) {
      emailCSV = exportParams.emailId;
    } else {
      emailCSV = getEmailCSV(StringUtil.getCSV(userIds));
    }
    if ((emailCSV == null || emailCSV.isEmpty())) {
      xLogger.warn(
          "Email CSV is empty. {0} users are either disabled or unavailable in the datastore in domain {1}. Not sending exported data to anyone...",
          exportParams.userIds, exportParams.domainId);
      return;
    }
    String type = exportParams.type;
    // If exportParams.type is ReportsConstants.TYPE_STOCKEVENT, then set type to BulkExportMgr.TYPE_ABNORMALSTOCK so that the email title mentions Abnormal Stock and not Stock event
    if (ReportsConstants.TYPE_STOCKEVENT.equals(type)) {
      type = BulkExportMgr.TYPE_ABNORMALSTOCK;
    }
    if (exportParams.size == 0) {
      notifyNoData(emailCSV, type, formattedTime, u, exportParams.domainId, exportParams.kioskId,
          exportParams.materialId, es, messages, exportParams.assetId, exportParams.sensorName);
      JobUtil.setJobCompleted(exportParams.jobId, IJobStatus.TYPE_EXPORT, exportParams.size,
          exportParams.gcsFilename, backendMessages);
      return;
    }
    // Finalize the exported file
    // String version = exportParams.type;
    String version = type;
    // Get the filename from filepath
    String filename = exportParams.filename;
    if (filename == null) // default file name, if absent
    {
      filename = type + ".csv"; // exportParams.type + ".csv";
    }
    // Form Uploaded object
    IUploaded uploaded = JDOUtils.createInstance(IUploaded.class);

    if (exportParams.gcsFilename != null) {
      uploaded.setBlobKey(
          exportParams.gcsFilename); // This is not BlobKey because exported file is not stored in Blob store anymore.
    }
    uploaded
        .setDescription(String.valueOf(exportParams.size) + " items to " + exportParams.userIds);
    uploaded.setDomainId(exportParams.domainId);
    uploaded.setFileName(filename);
    String
        uploadedKey =
        JDOUtils
            .createUploadedKey(filename + IUploaded.SEPARATOR + System.currentTimeMillis(), version,
                exportParams.locale.toString());
    uploaded.setId(uploadedKey);
    uploaded.setLocale(exportParams.locale.toString());
    uploaded.setTimeStamp(now);
    uploaded.setType(IUploaded.EXPORT);
    uploaded.setUserId(exportParams.sourceUserId);
    uploaded.setVersion(version);

    xLogger.info(
        "Created export file {0} with {1} items of type {2} by user {3} to user(s): {4} ({5})",
        filename, exportParams.size, exportParams.type, exportParams.sourceUserId,
        exportParams.userIds, emailCSV);
    // Send exported data via email
    // notifyExport( req, emailCSV, exportParams.type, formattedTime, uploaded, u, true, exportParams.domainId, exportParams.kioskId, exportParams.materialId, as, backendMessages, messages );
    notifyExport(req, emailCSV, type, formattedTime, uploaded, u, true, exportParams.domainId,
        exportParams.kioskId, exportParams.materialId, es, backendMessages, messages,
        exportParams.assetId, exportParams.sensorName);
    // Store uploaded
    try {
//            StorageUtil storageUtil = AppFactory.get().getStorageUtil();
      // Delete the blob after sending (only in production, retain it in on local dev server for debugging purposes)
//			if ( !SecurityManager.isDevServer() )
//                storageUtil.removeFile(ExportProcessor.DATAEXPORT_BUCKETNAME, exportParams.gcsFilename);
      // TODO: Reset blob-key in Uploaded (so a dangling blob reference does not exist)
      // Reset blobKey in Uploaded so that a dangling reference to Google Cloud Storage does not exisit.
      uploaded.setBlobKey(null);
      // Reset the blob-key
      UploadService us = Services.getService(UploadServiceImpl.class);
      List<IUploaded> uploads = new ArrayList<IUploaded>();
      uploads.add(uploaded);
      us.addNewUpload(uploads);
      JobUtil.setJobCompleted(exportParams.jobId, IJobStatus.TYPE_EXPORT, exportParams.size,
          exportParams.gcsFilename, backendMessages);
    } catch (Exception e) {
      xLogger.severe("{0} when storing Uploaded with key {1} in domain {2}: {3}",
          e.getClass().getName(), uploadedKey, exportParams.domainId, e.getMessage(), e);
      JobUtil.setJobFailed(exportParams.jobId, "Reason: System error.");
    }
    xLogger.fine("Exiting finalizeExport");
  }

  // Notify the exporter via email
  private void notifyExport(HttpServletRequest req, String addressCSV, String type,
                            String formattedTime, IUploaded uploaded, IUserAccount u,
                            boolean attachToEmail, Long domainId, Long kioskId, Long materialId,
                            EntitiesService as, ResourceBundle backendMessages,
                            ResourceBundle messages, String deviceId, String sensorName) {
    xLogger.fine("Entered notifyExport, type: {0}", type);
    // Form the subject
    String
        subject =
        getMailSubject(type, formattedTime, domainId, kioskId, materialId, as, messages, deviceId,
            sensorName);
    // Send the email
    try {
      MessageService
          ms =
          MessageService.getInstance(MessageService.EMAIL, u.getCountry(), true, u.getDomainId(),
              u.getUserId(), null);
      List<String> userIds = StringUtil.getList(addressCSV);
      if (attachToEmail) {
        sendEmailWithAttachment(((EmailService) ms), userIds, type, subject, uploaded, messages);
      } else {
        // Content
        String content = backendMessages.getString("export.notifymessage");
        try {
          content += " " + messages.getString(type);
        } catch (Exception e) {
          xLogger.warn("{0} when getting resource with key {1}: {2}", e.getClass().getName(), type,
              e.getMessage());
        }
        String uploadedKey = uploaded.getId();
        // Form the url
        String
            url =
            "https://" + req.getServerName() + (SecurityMgr.isDevServer() ? ":" + req
                .getServerPort() : "") + "/export?action=" + ACTION_DOWNLOAD + "&key="
                + uploadedKey;
        content += ":\n\n" + url;
        String[] addresses = addressCSV.split(",");
        for (int i = 0; i < addresses.length; i++) {
          ms.send(addresses[i], content, MessageService.NORMAL, subject, null);
        }
      }
    } catch (Exception e) {
      xLogger.severe("{0} during email notification for address {1} for export of {2}: {3}",
          e.getClass().getName(), addressCSV, type, e.getMessage());
    }
    xLogger.fine("Exiting notifyExport");
  }

  // Notify that no data is available for export
  private void notifyNoData(String addressCSV, String type, String formattedTime, IUserAccount u,
                            Long domainId, Long kioskId, Long materialId, EntitiesService as,
                            ResourceBundle messages, String deviceId, String sensorName) {
    xLogger.fine("Entering notifyNoData");
    String
        subject =
        getMailSubject(type, formattedTime, domainId, kioskId, materialId, as, messages, deviceId,
            sensorName);
    try {
      MessageService
          ms =
          MessageService.getInstance(MessageService.EMAIL, u.getCountry(), true, u.getDomainId(),
              u.getUserId(), null);
      // Content
      String content = "No data available while exporting";
      try {
        content += " " + messages.getString(type);
      } catch (Exception e) {
        xLogger.warn("{0} when getting resource with key {1}: {2}", e.getClass().getName(), type,
            e.getMessage());
      }

      List<String> userIds = StringUtil.getList(addressCSV);
      for (int i = 0; i < userIds.size(); i++) {
        ms.send(userIds.get(i), content, MessageService.NORMAL, subject, null);
      }
    } catch (Exception e) {
      xLogger.severe("{0} during email notification for address {1} for export of {2}: {3}",
          e.getClass().getName(), addressCSV, type, e.getMessage());
    }
    xLogger.fine("Exiting notifyNoData");
  }

  // Get the mail subject
  private String getMailSubject(String type, String formattedTime, Long domainId, Long kioskId,
                                Long materialId, EntitiesService as, ResourceBundle messages,
                                String deviceid, String sensorName) {
    String subject;
    if ("powerdata".equals(type)) {
      subject =
          "[" + messages.getString("export") + "] " + messages.getString("asset.tempdata") + " "
              + deviceid + CharacterConstants.SPACE + messages.getString("asset.sensor") + " "
              + sensorName;
    } else {
      subject = "[" + messages.getString("export") + "]";
      // Form the subject
      try {
        subject += " " + messages.getString(type);
      } catch (Exception e) {
        xLogger.warn("{0} when getting resource for key {1}: {2}", e.getClass().getName(), type,
            e.getMessage());
      }
      if (kioskId != null && as != null) {
        try {
          subject += " " + messages.getString("for") + " " + as.getKiosk(kioskId, false).getName();
        } catch (Exception e) {
          // ignore
        }
      }
      if (materialId != null) {
        try {
          MaterialCatalogService mcs = Services.getService(MaterialCatalogServiceImpl.class);
          subject +=
              " " + (kioskId != null ? "&" : messages.getString("for")) + " " + mcs
                  .getMaterial(materialId).getName();
        } catch (Exception e) {
          // ignore
        }
      }
      // Do not add the domain name in the mail subject if type == BulkExportMgr.TYPE_USAGESTATISTICS
      if (kioskId == null && materialId == null && domainId != null && as != null
          && !BulkExportMgr.TYPE_USAGESTATISTICS.equals(type)) {
        try {
          DomainsService ds = Services.getService(DomainsServiceImpl.class);
          subject += " " + messages.getString("for") + " " + ds.getDomain(domainId).getName();
        } catch (Exception e) {
          // ignore
        }
      }
    }
    subject += " " + messages.getString("on") + " " + formattedTime;
    return subject;
  }

  // Serve an uploaded file
  private void serveFile(HttpServletRequest req, HttpServletResponse resp)
      throws IOException, ServiceException {
    xLogger.fine("Entered serveFile");
    // Get the uploaded key
    String key = req.getParameter("key");
    if (key == null || key.isEmpty()) {
      writeText(resp, "No key specified");
      return;
    }
    // Get the uploaded object
    boolean notFound = false;
    try {
      UploadService us = Services.getService(UploadServiceImpl.class);
      IUploaded uploaded = us.getUploaded(key);
      resp.setCharacterEncoding("UTF-8");
      resp.addHeader("Content-Disposition", "inline; filename=" + uploaded.getFileName());
      // Get the blob key
      String blobKeyStr = uploaded.getBlobKey();
      if (blobKeyStr == null) {
        notFound = true;
      } else {
        // Serve the blob
        blobstoreService.serve(blobKeyStr, resp);
      }
    } catch (ObjectNotFoundException e) {
      notFound = true;
    }
    // If not found, respond with error message
    if (notFound) {
      writeText(resp, "No such file found");
      return;
    }
    xLogger.fine("Exiting serveFile");
  }


  // Export bulk upload formats, as per the type
  private void exportBulkUploadFormat(HttpServletRequest req, HttpServletResponse resp,
                                      ResourceBundle messages) throws IOException {
    xLogger.fine("Entered exportBulkUploadFormat");
    // Get type
    String type = req.getParameter("type");
    String typeName = null;
    try {
      typeName = messages.getString(type);
    } catch (Exception e) {
      // ignore
    }
    String filename = (typeName != null ? typeName : type) + ".csv";
    // Get locale
    SecureUserDetails sUser = SecurityMgr.getUserDetails(req.getSession());
    Locale locale = sUser.getLocale();
    Long domainId = SessionMgr.getCurrentDomain(req.getSession(), sUser.getUsername());
    // Send the CSV format
    sendCSVFile(BulkUploadMgr.getCSVFormat(type, locale, DomainConfig.getInstance(domainId)),
        filename, resp);
    xLogger.fine("Exiting exportBulkUploadFormat");
  }


  // Send CSV file
  private void sendCSVFile(String csv, String filename, HttpServletResponse resp)
      throws IOException {
    xLogger.fine("Entered sendCSVFile");
    if (csv == null) {
      return;
    }
    resp.setCharacterEncoding("UTF-8");
    resp.setContentType("text/csv");
    resp.addHeader("Content-Disposition", "inline; filename=" + filename);
    PrintWriter pw = resp.getWriter();
    pw.write(csv);
    pw.close();
    xLogger.fine("Exiting sendCSVFile");
  }

  // Send email with the data file as attachment
  private void sendEmailWithAttachment(EmailService svc, List<String> addressCSV, String type,
                                       String subject, IUploaded uploaded, ResourceBundle messages)
      throws IOException, MessageHandlingException {
    xLogger.fine("Entered sendEmailWithAttachment");
    InputStream is = null;
    try {
      String blobKey = uploaded.getBlobKey();
      // Get the filename
      String filename = uploaded.getFileName(); /// type;
      xLogger.info(
          "BLOB KEY FROM UPLOADED: blobKey = {0}, addressCSV = {1}, subject = {2}, filename = {3}",
          blobKey, addressCSV, subject, filename);
      // Get the attachment data
      StorageUtil storageUtil = AppFactory.get().getStorageUtil();
      // xLogger.info( "Got attachment data: {0}", ( attachmentData == null ? "NULL" : attachmentData.length ) );
      // Address
      List<String> addresses = new ArrayList<String>();
      // Check if the address is a CSV of addresses
      String[] addressArray = addressCSV.toArray(new String[addressCSV.size()]);
      for (int i = 0; i < addressArray.length; i++) {
        addresses.add(addressArray[i]); // addresses.add( address );
      }
      // Content messages
      String message;
      if (ConfigUtil.isLogi()) {
        long
            fileSize =
            storageUtil.getFileSizeInBytes(ExportProcessor.DATAEXPORT_BUCKETNAME, blobKey);
        if (fileSize >= 5_000_000) { // 5 MB
          String host = ConfigUtil.get("logi.host.server");
          String path = host == null ? "http://localhost:50070/webhdfs/v1" : "/media";
          String localStr = host == null ? "?op=OPEN" : "";
          message =
              "<br/><br/>Please <a href=\"" + (host == null ? "" : host) + path
                  + "/user/logistimoapp/dataexport/" + blobKey + localStr
                  + "\">click here</a> to download the file.";
          svc.sendHTML(null, addresses, subject, message, null);
          return;
        }
      }
      try {
        is = storageUtil.getInputStream(ExportProcessor.DATAEXPORT_BUCKETNAME, blobKey);
      } catch (Exception e) {
        xLogger.severe("{0} while getting input stream for gcsFilename {1} Message: {2}",
            e.getClass().getName(), blobKey, e.getMessage(), e);
      }
      message =
          "<p>" + messages.getString(type) + ("powerdata".equals(type) ? "" : " data")
              + " is attached" + "</p>";
      svc.sendWithAttachmentStream(addresses, message, MessageService.NORMAL, subject, is,
          EmailService.MIME_CSV, filename);
    } catch (Exception e) {
      xLogger.warn("{0} when trying to get send email with attachement to {1}: {2}",
          e.getClass().getName(), addressCSV, e.getMessage(), e);
    } finally {
      if (is != null) {
        is.close();
      }
    }
    xLogger.fine("Exiting sendEmailWithAttachment");
  }

  private ExportParams getExportParams(HttpServletRequest req) {
    String
        type =
        req.getParameter(
            "type"); // type of data exported (e.g. orders/transactions/inventory) - BulkExportMgr.TYPE_XXX
    String subType = req.getParameter("subType");
    String sourceUserId = req.getParameter("sourceuserid"); // user who requested export
    String userIds = req.getParameter("userids"); // user Ids of recepients of the export
    String userTags = req.getParameter("userTags"); // user Tags of recepients of the export
    String domainIdStr = req.getParameter("domainid"); // mandatory
    String jobIdStr = req.getParameter("jobid");

    // Date params., for logging purposes
    String
        fromDateStr =
        req.getParameter("from"); // from date (optional) - see Constants.DATETIME_FORMAT
    String toDateStr = req.getParameter("to"); // to date (optional) - see Constants.DATETIME_FORMAT
    // Optional params. for export params.
    String kioskIdStr = req.getParameter("kioskid");
    String materialIdStr = req.getParameter("materialid");
    String batchIdStr = req.getParameter("batchid");
    // Domain Id
    Long domainId = Long.valueOf(domainIdStr);
    // Get transaction type
    String trnType = req.getParameter("transactiontype");
    // Get Customer/Vendor id
    String lkIdStr = req.getParameter("lkIdParam");
    String emailId = req.getParameter("emailid"); // optional
    if (StringUtils.isBlank(jobIdStr)) {
      Map<String, String[]> reqParams = req.getParameterMap();
      Map<String, String> params = new HashMap<>(reqParams.size());
      for (String st : reqParams.keySet()) {
        params.put(st, req.getParameter(st));
      }
      jobIdStr =
          String.valueOf(JobUtil
              .createJob(Long.parseLong(domainIdStr), null, null, IJobStatus.TYPE_EXPORT, type,
                  params));
    }

    // Get the export params.
    ExportParams exportParams = new ExportParams();
    exportParams.type = type;
    exportParams.subType = subType;
    exportParams.sourceUserId = sourceUserId;
    exportParams.userIds = userIds;
    exportParams.userTags = userTags;
    exportParams.domainId = domainId;
    if (trnType != null && !trnType.isEmpty()) {
      exportParams.transactionType = trnType;
    }
    if (StringUtils.isNotEmpty(kioskIdStr)) {
      try {
        exportParams.kioskId = Long.valueOf(kioskIdStr);
      } catch (Exception e) {
        xLogger.warn("{0} when getting kiosk ID {1}: {2}", e.getClass().getName(), kioskIdStr,
            e.getMessage());
      }
    }
    if (StringUtils.isNotEmpty(materialIdStr)) {
      try {
        exportParams.materialId = Long.valueOf(materialIdStr);
      } catch (Exception e) {
        xLogger.warn("{0} when getting material ID {1}: {2}", e.getClass().getName(), materialIdStr,
            e.getMessage());
      }
    }
    if (StringUtils.isNotEmpty(batchIdStr)) {
      try {
        exportParams.batchId = batchIdStr;
      } catch (Exception e) {
        xLogger.warn("{0} when getting batch ID {1}: {2}", e.getClass().getName(), batchIdStr,
            e.getMessage());
      }
    }
    if (StringUtils.isNotEmpty(lkIdStr)) {
      try {
        exportParams.lkioskId = Long.valueOf(lkIdStr);
      } catch (Exception e) {
        xLogger
            .warn("{0} when getting customer/vendor ID {1}: {2}", e.getClass().getName(), lkIdStr,
                e.getMessage());
      }
    }
    exportParams.emailId = emailId;
    exportParams.jobId = Long.valueOf(jobIdStr);
    // Get domain config.
    DomainConfig dc = DomainConfig.getInstance(domainId);
    exportParams.currency = dc.getCurrency();

    // Get the parameter objects
    if (StringUtils.isNotEmpty(fromDateStr)) {
      try {
        fromDateStr = URLDecoder.decode(fromDateStr, "UTF-8");
        exportParams.from =
            LocalDateUtil.parseCustom(fromDateStr, Constants.DATE_FORMAT, dc.getTimezone());
      } catch (Exception e) {
        xLogger.warn("{0} when parsing from-date {1}...ignoring this: {2}", e.getClass().getName(),
            fromDateStr, e.getMessage());
      }
    }
    if (StringUtils.isNotEmpty(toDateStr)) {
      try {
        toDateStr = URLDecoder.decode(toDateStr, "UTF-8");
        exportParams.to =
            LocalDateUtil.getOffsetDate(
                (LocalDateUtil.parseCustom(toDateStr, Constants.DATE_FORMAT, dc.getTimezone())), 1);
      } catch (Exception e) {
        xLogger.warn("{0} when parsing to-date {1}...ignoring this: {2}", e.getClass().getName(),
            fromDateStr, e.getMessage());
      }
    }

    try {
      UsersService as = Services.getService(UsersServiceImpl.class);
      IUserAccount sourceUser = as.getUserAccount(sourceUserId);
      exportParams.locale = sourceUser.getLocale();
      exportParams.timezone = sourceUser.getTimezone();
    } catch (Exception e) {
      xLogger.severe("{0} when getting source user {1} in domain {2}: {3}", e.getClass().getName(),
          sourceUserId, domainId, e.getMessage());
      return null;
    }
    return exportParams;
  }
}

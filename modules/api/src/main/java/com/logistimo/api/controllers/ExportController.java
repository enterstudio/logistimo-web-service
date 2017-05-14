package com.logistimo.api.controllers;

import com.logistimo.AppFactory;
import com.logistimo.bulkuploads.BulkUploadMgr;
import com.logistimo.exports.BulkExportMgr;
import com.logistimo.reports.ReportsConstants;
import com.logistimo.services.taskqueue.ITaskService;

import org.apache.commons.lang.StringUtils;
import com.logistimo.config.models.DomainConfig;
import com.logistimo.entity.IJobStatus;
import com.logistimo.entity.IUploaded;
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
import com.logistimo.utils.JobUtil;
import com.logistimo.reports.utils.ReportsUtil;
import com.logistimo.api.util.SessionMgr;
import com.logistimo.exception.TaskSchedulingException;
import com.logistimo.logger.XLog;
import com.logistimo.api.builders.JobStatusBuilder;
import com.logistimo.exception.BadRequestException;
import com.logistimo.exception.InvalidServiceException;
import com.logistimo.api.request.ExportReportRequestObj;
import com.logistimo.api.util.SecurityUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Created by Mohan Raja on 23/01/15
 */
@Controller
@RequestMapping("/export")
public class ExportController {

  private static final XLog xLogger = XLog.getLog(ExportController.class);

  private static final String EXPORT_TASK_URL = "/task/export";
  JobStatusBuilder builder = new JobStatusBuilder();
  private ITaskService taskService = AppFactory.get().getTaskService();

  @RequestMapping(value = "/download", method = RequestMethod.GET)
  public
  @ResponseBody
  void downloadFile(@RequestParam String key, HttpServletRequest request,
                    HttpServletResponse response) {
    SecureUserDetails sUser = SecurityUtils.getUserDetails(request);
    Locale locale = sUser.getLocale();
    ResourceBundle backendMessages = Resources.get().getBundle("BackendMessages", locale);
    if (key == null || key.isEmpty()) {
      throw new BadRequestException(backendMessages.getString("file.download.error"));
    }
    try {
      UploadService us = Services.getService(UploadServiceImpl.class);
      IUploaded uploaded = us.getUploaded(key);
      response.setCharacterEncoding("UTF-8");
      response.addHeader("Content-Disposition", "inline; filename=" + uploaded.getFileName());
      String blobKeyStr = uploaded.getBlobKey();
      if (blobKeyStr != null) {
        AppFactory.get().getBlobstoreService().serve(blobKeyStr, response);
      }
    } catch (ServiceException | ObjectNotFoundException | IOException e) {
      xLogger.warn("Error in downloading file", e);
      throw new InvalidServiceException(backendMessages.getString("file.download.error"));
    }
  }

  @RequestMapping(value = "/uploadformat", method = RequestMethod.GET)
  public
  @ResponseBody
  void bulkUploadFormat(@RequestParam String type, HttpServletRequest request,
                        HttpServletResponse response) {
    SecureUserDetails sUser = SecurityMgr.getUserDetails(request.getSession());
    Locale locale = sUser.getLocale();
    ResourceBundle backendMessages = Resources.get().getBundle("BackendMessages", locale);
    Long domainId = SessionMgr.getCurrentDomain(request.getSession(), sUser.getUsername());
    String csv = BulkUploadMgr.getCSVFormat(type, locale, DomainConfig.getInstance(domainId));
    if (csv == null) {
      throw new BadRequestException(backendMessages.getString("file.uploadformat.fetch.error"));
    }
    String typeName = null;
    try {
      ResourceBundle messages = Resources.get().getBundle("Messages", locale);
      ResourceBundle bckMessages = Resources.get().getBundle("BackendMessages", locale);
      if ("kiosks".equalsIgnoreCase(type)) {
        typeName = bckMessages.getString(type);
      } else {
        typeName = messages.getString(type);
      }
    } catch (Exception ignored) {
      xLogger.warn("Exception while getting resource bundle", ignored);
    }
    String filename = (typeName != null ? typeName : type) + ".csv";
    response.setCharacterEncoding("UTF-8");
    response.setContentType("text/csv");
    response.addHeader("Content-Disposition", "inline; filename=" + filename);
    PrintWriter pw;
    try {
      pw = response.getWriter();
      pw.write(csv);
      pw.close();
    } catch (IOException e) {
      xLogger.severe("Error in fetching upload format response", e);
      throw new InvalidServiceException(
          backendMessages.getString("file.uploadformat.fetch.response"));
    }
  }

  @RequestMapping(value = "/schedule/batch", method = RequestMethod.GET)
  public
  @ResponseBody
  String scheduleBatch(HttpServletRequest request) throws IOException {
    SecureUserDetails user = SecurityUtils.getUserDetails(request);
    Long jobId;
    Locale locale = user.getLocale();
    ResourceBundle backendMessages = Resources.get().getBundle("BackendMessages", locale);
    Map<String, String> params = taskService.getParamsFromQueryString(request.getQueryString());
    params.put("action", "be");
    params.put("sourceuserid", user.getUsername());
    params.put("userids", user.getUsername());
    params.put("domainid",
        String.valueOf(SessionMgr.getCurrentDomain(request.getSession(), user.getUsername())));
    params.put("tz", user.getTimezone());
    Map<String, String> headers = BulkExportMgr.getExportBackendHeader();
    if ("transactions".equalsIgnoreCase(params.get("type")) || "orders"
        .equalsIgnoreCase(params.get("type"))) {
      if (StringUtils.isEmpty(params.get("to"))) {
        params.remove("to");
      }
      if (StringUtils.isEmpty(params.get("from"))) {
        params.remove("from");
      }
      if (StringUtils.isBlank(params.get("kioskid")) || "null"
          .equalsIgnoreCase(params.get("kioskid"))) {
        params.remove("kioskid");
      }
      if (StringUtils.isBlank(params.get("materialid")) || "null"
          .equalsIgnoreCase(params.get("materialid"))) {
        params.remove("materialid");
      }
      if (StringUtils.isBlank(params.get("batchid")) || "null"
          .equalsIgnoreCase(params.get("batchid"))) {
        params.remove("batchid");
      }
      if (StringUtils.isBlank(params.get("rsn"))) {
        params.remove("rsn");
      } else {
        params.put("rsn", URLDecoder.decode(params.get("rsn"), "UTF-8"));
      }
    }
    if ("orders".equalsIgnoreCase(params.get("type"))) {
      if (StringUtils.isNotEmpty(params.get("to")) && StringUtils.isNotEmpty(params.get("from"))) {
        String from = params.get("from");
        String to = params.get("to");
        params.put("startdate", from.substring(0, from.indexOf('%')));
        params.put("enddate", to.substring(0, to.indexOf('%')));
      }
      if (StringUtils.isNotEmpty(params.get("values"))) {
        params.put("attachtoemail", "");
        params.put("format", "csv");
        params.remove("sourceuserid");
        params.remove("domainid");
        params.remove("action");
        params.remove("userids");
      }
    }
    if ("assets".equalsIgnoreCase(params.get("type"))) {
      params.put("subType", "assets");
    }
    if ("discrepancies".equalsIgnoreCase(params.get("type"))) {
      if (StringUtils.isEmpty(params.get("to"))) {
        params.remove("to");
      }
      if (StringUtils.isEmpty(params.get("from"))) {
        params.remove("from");
      }
      if (StringUtils.isBlank(params.get("kioskid")) || "null"
          .equalsIgnoreCase(params.get("kioskid"))) {
        params.remove("kioskid");
      }
      if (StringUtils.isBlank(params.get("materialid")) || "null"
          .equalsIgnoreCase(params.get("materialid"))) {
        params.remove("materialid");
      }
      if (StringUtils.isBlank(params.get("etag")) || "null".equalsIgnoreCase(params.get("etag"))) {
        params.remove("etag");
      }
      if (StringUtils.isBlank(params.get("mtag")) || "null".equalsIgnoreCase(params.get("mtag"))) {
        params.remove("mtag");
      }
      if (StringUtils.isBlank(params.get("disctype")) || "null"
          .equalsIgnoreCase(params.get("disctype"))) {
        params.remove("disctype");
      }
      if (StringUtils.isBlank(params.get("orderid")) || "null"
          .equalsIgnoreCase(params.get("orderid"))) {
        params.remove("orderid");
      }
      if (StringUtils.isBlank(params.get("otype")) || "null"
          .equalsIgnoreCase(params.get("otype"))) {
        params.remove("otype");
      }

    }
    // Create a entry in the JobStatus table using JobUtil.createJob method.
    jobId =
        JobUtil.createJob(SessionMgr.getCurrentDomain(request.getSession(), user.getUsername()),
            user.getUsername(), null, IJobStatus.TYPE_EXPORT, params.get("type"), params);
    if (jobId != null) {
      params.put("jobid", jobId.toString());
    }
    try {
      taskService.schedule(ITaskService.QUEUE_EXPORTER, EXPORT_TASK_URL, params, headers,
          ITaskService.METHOD_POST);
    } catch (TaskSchedulingException e) {
      xLogger.severe("{0} when scheduling export task with params {1}: {2}", e.getClass().getName(),
          params, e.getMessage());
      throw new InvalidServiceException(
          backendMessages.getString("error.in") + " " + e.getClass().getName() + " "
              + backendMessages.getString("schedule.export.task"));
    }
    if (jobId != null) {
      return String.valueOf(jobId);
    }
    return backendMessages.getString("export.submit.success");
  }

  @RequestMapping(value = "/schedule/report", method = RequestMethod.POST)
  public
  @ResponseBody
  String scheduleReport(@RequestBody ExportReportRequestObj model,
                        HttpServletRequest request) throws IOException {
    SecureUserDetails sUser = SecurityUtils.getUserDetails(request);
    Locale locale = sUser.getLocale();
    Long jobId = null;
    ResourceBundle backendMessages = Resources.get().getBundle("BackendMessages", locale);
    String userIds = sUser.getUsername();
    String sourceUserId = sUser.getUsername();
    Long domainId = SessionMgr.getCurrentDomain(request.getSession(), userIds);
    if (model.filterMap == null || model.filterMap.isEmpty()) {
      if (model.filterMap == null) {
        model.filterMap = new HashMap<String, String[]>();
      }
      String[] domains = new String[1];
      domains[0] = domainId.toString();
      model.filterMap.put(ReportsConstants.FILTER_DOMAIN, domains);
    }
    String filtersQueryString = ReportsUtil.getQueryString(model.filterMap, null);
    String
        queryString =
        "action=be&type=" + model.type + "&sourceuserid=" + sourceUserId + "&userids=" + userIds
            + "&domainid=" + domainId;
    if (model.startDate != null) {
      queryString += "&from=" + model.startDate + " 00:00:00";
    }
    if (model.endDate != null) {
      queryString += "&to=" + model.endDate + " 00:00:00";
    }
    if (model.frequency == null || model.frequency.isEmpty()) {
      model.frequency = ReportsConstants.FREQ_MONTHLY;
    }
    queryString += "&frequency=" + model.frequency;
    queryString += "&" + filtersQueryString + "&reports";
    xLogger.fine("queryString: {0}", queryString);
    Map<String, String> params = taskService.getParamsFromQueryString(queryString);
    if (params == null) {
      xLogger.severe("Invalid params while scheduling report export task in domain {0}", domainId);
      return null;
    }
    // Create a entry in the JobStatus table using JobUtil.createJob method.
    jobId =
        JobUtil.createJob(SessionMgr.getCurrentDomain(request.getSession(), sourceUserId),
            sourceUserId, null, IJobStatus.TYPE_EXPORT, params.get("type"), params);
    if (jobId != null) {
      params.put("jobid", jobId.toString());
    }

    xLogger.fine("params: {0}", params);
    String url = EXPORT_TASK_URL;
    Map<String, String> headers = BulkExportMgr.getExportBackendHeader();
    try {
      taskService
          .schedule(ITaskService.QUEUE_EXPORTER, url, params, headers, ITaskService.METHOD_POST);
    } catch (Exception e) {
      xLogger.severe("{0} while scheduling report export task in domain {1}. Message: {2}",
          e.getClass().getName(), domainId, e.getMessage(), e);
    }
    xLogger.fine("Exiting scheduleReport");
    if (jobId != null) {
      return String.valueOf(jobId);
    }
    return backendMessages.getString("export.submit.success");
  }


  @RequestMapping(value = "/exportjoblist", method = RequestMethod.GET)
  public
  @ResponseBody
  Results getExportJobList(
      @RequestParam(defaultValue = PageParams.DEFAULT_OFFSET_STR) int offset,
      @RequestParam(defaultValue = PageParams.DEFAULT_SIZE_STR) int size,
      @RequestParam String type,
      @RequestParam boolean allExports,
      HttpServletRequest request) {

    SecureUserDetails user = SecurityUtils.getUserDetails(request);

    Long domainId = SessionMgr.getCurrentDomain(request.getSession(), user.getUsername());
    Results results;
    Navigator
        navigator =
        new Navigator(request.getSession(), "ExportController.getExportList", offset, size, "dummy",
            0);
    PageParams pageParams = new PageParams(navigator.getCursor(offset), offset, size);
    try {
      if (allExports) {
        results = JobUtil.getRecentJobs(type, null, domainId, pageParams);
      } else {
        results = JobUtil.getRecentJobs(type, user.getUsername(), domainId, pageParams);
      }

      navigator.setResultParams(results);
      if (results != null) {
        results.setOffset(offset);
      }
      return builder.buildJobs(results, user);
    } catch (Exception e) {
      xLogger.warn("{0} when trying to get recent jobs for user {1} in domain {2}. Message: {3}",
          e.getClass().getName(), user.getUsername(), domainId, e.getMessage(), e);
      throw new InvalidServiceException("When trying to get recent jobs", e);
    }
  }

}

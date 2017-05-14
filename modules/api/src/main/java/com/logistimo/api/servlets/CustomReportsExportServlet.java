package com.logistimo.api.servlets;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import com.logistimo.AppFactory;
import com.logistimo.api.security.SecurityMgr;
import com.logistimo.customreports.CustomReportConstants;
import com.logistimo.dao.JDOUtils;
import com.logistimo.services.storage.StorageUtil;
import com.logistimo.services.taskqueue.ITaskService;
import com.logistimo.services.utils.ConfigUtil;

import org.apache.commons.lang.StringUtils;
import com.logistimo.communications.service.EmailService;
import com.logistimo.communications.service.MessageService;
import com.logistimo.entity.IJobStatus;
import com.logistimo.pagination.Executor;
import com.logistimo.pagination.PageParams;
import com.logistimo.pagination.PagedExec.Finalizer;
import com.logistimo.pagination.QueryParams;
import com.logistimo.customreports.processor.CustomReportsExportProcessor;
import com.logistimo.services.ServiceException;
import com.logistimo.services.impl.PMF;
import com.logistimo.api.servlets.mobile.json.JsonOutput;
import com.logistimo.users.entity.IUserAccount;
import com.logistimo.utils.MsgUtil;

import com.logistimo.constants.Constants;
import com.logistimo.customreports.CustomReportsExportMgr;
import com.logistimo.customreports.CustomReportsExportMgr.CustomReportsExportParams;
import com.logistimo.utils.JobUtil;
import com.logistimo.utils.LocalDateUtil;
import com.logistimo.customreports.utils.SpreadsheetUtil;
import com.logistimo.utils.StringUtil;
import com.logistimo.logger.XLog;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.URLDecoder;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

import javax.jdo.PersistenceManager;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class CustomReportsExportServlet extends SgServlet {
  // Url

  private static final long serialVersionUID = 1L;
  // Logger
  private static final XLog xLogger = XLog.getLog(CustomReportsExportServlet.class);
  private static final String UTF8_CHARSET = "; charset=\"UTF-8\"";
  private static final String JSON_UTF8 = "application/json" + UTF8_CHARSET;
  private static ITaskService taskService = AppFactory.get().getTaskService();

  private static void export(HttpServletRequest req, HttpServletResponse resp) {
    xLogger.fine("Entering export");
    // Read the parameters
    String
        type =
        req.getParameter(
            "type"); // type of data exported (e.g. orders/transactions/inventory/users/entities/materials/transactioncounts)
    String domainIdStr = req.getParameter("domainid"); // mandatory
    String countryStr = req.getParameter("country");
    String languageStr = req.getParameter("language");
    String timezoneStr = req.getParameter("timezone");
    String userIds = req.getParameter("userids"); // mandatory, user ids of recepients of the export
    String emailIds = req.getParameter("emids");
    String templateNameStr = req.getParameter("templatename"); // mandatory
    String sheetNameStr = req.getParameter("sheetname"); // mandatory
    String fileNameStr = req.getParameter("filename"); // mandatory
    String subjectStr = req.getParameter("subject"); // mandatory
    String bodyStr = req.getParameter("body");
    String jobIdStr = req.getParameter("jobid");
    String furl = req.getParameter("furl");
    String fqueue = req.getParameter("fqueue");
    String aggrFreq = CustomReportConstants.FREQUENCY_DAILY;
    String filterBy = null;

    if (countryStr == null || countryStr.isEmpty()) {
      countryStr = Constants.COUNTRY_DEFAULT;
    }
    if (languageStr == null || languageStr.isEmpty()) {
      languageStr = Constants.LANG_DEFAULT;
    }
    if (timezoneStr == null || timezoneStr.isEmpty()) {
      timezoneStr = Constants.TIMEZONE_DEFAULT;
    }
    String fromDateStr = req.getParameter("from"); // mandatory - see Constants.DATETIME_FORMAT
    String toDateStr = req.getParameter("to"); // mandatory - see Constants.DATETIME_FORMAT
    if (type == null || type.isEmpty() || domainIdStr == null || domainIdStr.isEmpty() || (
        (userIds == null ||
            userIds.isEmpty()) && (emailIds == null || emailIds.isEmpty()))
        || templateNameStr == null || templateNameStr.isEmpty() ||
        sheetNameStr == null || sheetNameStr.isEmpty() || fromDateStr == null ||
        fromDateStr.isEmpty() || toDateStr == null || toDateStr.isEmpty() || fileNameStr == null ||
        fileNameStr.isEmpty() || subjectStr == null || subjectStr.isEmpty() || furl == null ||
        furl.isEmpty() || fqueue == null || fqueue.isEmpty()) {
      xLogger.severe("One or more mandatory parameters for export are missing: type = {0}, " +
              "domainId = {1}, userIds = {2}, templateNameStr: {3}, sheetNameStr: {4}, fromDateStr: {5}, "
              +
              "toDateStr: {6}, fileNameStr: {7}, furl: {8}, fqueue: {9}",
          type, domainIdStr, userIds, templateNameStr, sheetNameStr, fromDateStr, toDateStr,
          fileNameStr, furl, fqueue);
      return;
    }
    if (StringUtils.isBlank(jobIdStr) || Constants.NULL.equalsIgnoreCase(jobIdStr)) {
      Map<String, String[]> reqParams = req.getParameterMap();
      Map<String, String> params = new HashMap<>(reqParams.size());
      for (String st : reqParams.keySet()) {
        if (!"furl".equals(st)) {
          params.put(st, req.getParameter(st));
        }
      }
      jobIdStr =
          String.valueOf(JobUtil
              .createJob(Long.parseLong(domainIdStr), null, null, IJobStatus.TYPE_CUSTOMREPORT,
                  templateNameStr, params));
    }
    if (CustomReportConstants.TYPE_TRANSACTIONCOUNTS.equals(type)
        || CustomReportConstants.TYPE_INVENTORYTRENDS.equals(type)) {
      filterBy = req.getParameter("filterby");
      aggrFreq = req.getParameter("aggregatefreq");
    }
    // Domain Id
    Long domainId = Long.valueOf(domainIdStr);

    // Get the Custom report export params.
    CustomReportsExportParams cstRepExportParams = new CustomReportsExportParams();
    cstRepExportParams.type = type;
    cstRepExportParams.domainId = domainId;
    cstRepExportParams.locale = new Locale(languageStr, countryStr);
    cstRepExportParams.timezone = timezoneStr;
    cstRepExportParams.fileName = fileNameStr;
    cstRepExportParams.templateName = templateNameStr;
    cstRepExportParams.sheetName = sheetNameStr;
    cstRepExportParams.userIds = userIds;
    cstRepExportParams.emIds = emailIds;
    cstRepExportParams.subject = subjectStr;
    cstRepExportParams.body = bodyStr;

    // Parse the fromDateStr
    try {
      Date
          from =
          LocalDateUtil.getOffsetDate(
              LocalDateUtil.parseCustom(fromDateStr, Constants.DATETIME_FORMAT, null), -1,
              Calendar.MILLISECOND);
      if (CustomReportConstants.TYPE_HISTORICAL_INVENTORYSNAPSHOT.equals(type)) {
        cstRepExportParams.snapshotDate =
            from; // The from date should be sent as a parameter in CustomReportsExportParams to the CustomReportsExportProcessor. Otherwise, it is lost.
      }
    } catch (ParseException e) {
      xLogger.severe(
          "{0} when getting parsing from date for template {1} in domain {2}. Aboriting... {4}",
          e.getClass().getName(), templateNameStr, domainId, e.getMessage());
      return;
    }
    cstRepExportParams.jobId = Long.valueOf(jobIdStr);
    QueryParams qp = null;
    try {
      // Get the query params. object
      qp =
          CustomReportsExportMgr
              .getQueryParams(type, domainId, fromDateStr, toDateStr, filterBy, aggrFreq);
    } catch (ParseException e) {
      xLogger
          .severe("{0} when getting QueryParams for template {1} in domain {2}. Aboriting... {4}",
              e.getClass().getName(), templateNameStr, domainId, e.getMessage());
      return;
    }
    if (qp == null) {
      xLogger.severe(
          "Invalid query params for custom reports export for template {0} in domain {1}. Aborting...",
          templateNameStr, domainId);
      return;
    }
    // Page params
    PageParams pageParams = new PageParams(null, PageParams.DEFAULT_SIZE);
    try {
      // Finalizer
      Finalizer finalizer = new Finalizer();
      finalizer.url = URLDecoder.decode(furl, "UTF-8");
      finalizer.queue = fqueue;
      xLogger.info(
          "Exporting {0} for template {1} in domain {2} to users {3}, data duration from {4} to {5}",
          type, templateNameStr, domainIdStr, userIds, fromDateStr, toDateStr);
      String cstReportsExportParamsJson = cstRepExportParams.toJSONString();
      xLogger.info("exportParamsJson: {0}", cstReportsExportParamsJson);
      Executor.exec(domainId, qp, pageParams, CustomReportsExportProcessor.class.getName(),
          cstReportsExportParamsJson, finalizer);
    } catch (Exception e) {
      xLogger.severe(
          "{0} when paged-execing data export for {1} in domain {2} to users {3}. Aborting...: {4}",
          e.getClass().getName(), type, domainId, userIds, e.getMessage());
    }
    xLogger.fine("Exiting export");
  }

  @Override
  protected void processGet(HttpServletRequest request, HttpServletResponse response,
                            ResourceBundle backendMessages,
                            ResourceBundle messages)
      throws ServletException, IOException, ServiceException {
    processPost(request, response, backendMessages, messages);
  }

  @Override
  protected void processPost(HttpServletRequest req, HttpServletResponse resp,
                             ResourceBundle backendMessages, ResourceBundle messages)
      throws ServletException, IOException, ServiceException {
    xLogger.fine("Entered processPost");
    // Get request parameters
    String action = req.getParameter("action");
    xLogger.fine("action: {0}", action);
    if (action == null || action.isEmpty()) {
      xLogger.warn("Invalid action and type parameters. action: {0}, type: {1}", action);
      return;
    }
    if (action.equals(CustomReportConstants.ACTION_EXPORT)) {
      export(req, resp);
    } else if (action.equals(CustomReportConstants.ACTION_SCHEDULEEXPORT)) {
      scheduleExport(req, resp);
    } else if (CustomReportConstants.ACTION_FINALIZECUSTOMREPORTSEXPORT.equals(action)) {
      finalizeExport(req, resp, backendMessages);
    }
    xLogger.fine("Exiting processPost");
  }

  // Schedule an export
  private void scheduleExport(HttpServletRequest req, HttpServletResponse resp) {
    xLogger.fine("Entered scheduleExport");
    // Schedule a task immediately for custom report export
    // Read the parameters
    String domainIdStr = req.getParameter("domainid"); // mandatory
    String reportNameStr = req.getParameter("reportname"); // report name
    boolean schedule = req.getParameter("schedule") != null;
    String jobIdStr = req.getParameter("jobid");
    Long jobId = null;
    if (jobIdStr != null && !jobIdStr.isEmpty()) {
      jobId = Long.valueOf(jobIdStr);
    }
    if (schedule) {
      JsonOutput jsonOutput = null;
      if (domainIdStr == null || domainIdStr.isEmpty() || reportNameStr == null || reportNameStr
          .isEmpty()) {
        xLogger.severe("Invalid or null parameters: domainId: {0}, reportName: {1}", domainIdStr,
            reportNameStr);
        // Return an error json to the user and exit.
        // Get back to user, if this came from the browser (i.e. not the task)
        String
            message =
            "Error while generating custom report. Invalid or null domainId or reportName";
        jsonOutput = new JsonOutput("", false, message);
      } else {
        // Params
        Map<String, String> params = new HashMap<String, String>();
        params.put("action", CustomReportConstants.ACTION_SCHEDULEEXPORT);
        params.put("domainid", domainIdStr);
        params.put("reportname", reportNameStr);
        // Headers (to target backend)
        Map<String, String> headers = new HashMap<String, String>();
        //headers.put( "Host", BackendServiceFactory.getBackendService().getBackendAddress( Constants.BACKEND1 ) );
        try {
          taskService
              .schedule(taskService.QUEUE_EXPORTER, CustomReportConstants.CUSTOMREPORTS_EXPORT_TASK_URL, params, headers,
                  taskService.METHOD_POST, domainIdStr != null ? Long.parseLong(domainIdStr) : -1,
                  SecurityMgr.getUserDetails(req.getSession())
                      .getUsername(), "CUSTOM_REPORTS_EXPORT");
          jsonOutput =
              new JsonOutput("", true,
                  "Report '" + reportNameStr + "' is now scheduled for export.");
        } catch (Exception e) {
          xLogger.severe("{0} when scheduling task for custom report export of {1}: {2}",
              e.getClass().getName(), reportNameStr, e.getMessage());
          jsonOutput = new JsonOutput("", false, e.getMessage());
        }
      }
      // Send JSON output
      try {
        xLogger.fine("jsonOutput: {0}, jsonOutput.message: {1}, jsonOutput.status: {2}", jsonOutput,
            jsonOutput.getMessage(), jsonOutput.getStatus());
        Gson
            gson =
            new GsonBuilder().setDateFormat(Constants.DATE_FORMAT)
                .excludeFieldsWithoutExposeAnnotation().create();
        sendJsonResponse(resp, 200, gson.toJson(jsonOutput));

      } catch (IOException e) {
        xLogger.severe("{0} when sending json output while scheduling export. Message: {1}",
            e.getClass().getName(), e.getMessage());
        resp.setStatus(500);
      }
    } else { // start the export
      Long domainId = Long.valueOf(domainIdStr);
      CustomReportsExportMgr.handleCustomReportsExport(domainId, reportNameStr, jobId);
    }

    xLogger.fine("Exiting scheduleExport");
  }

  // Finalize an export and send the email notification
  private void finalizeExport(HttpServletRequest req, HttpServletResponse resp,
                              ResourceBundle backendMessages) {
    xLogger.fine("Entered finalizeExport");
    // Get the file name and user ids from the params string
    String cstReportsExportParamsJson = req.getParameter("output");
    CustomReportsExportParams cstReportsExportParams = null;
    try {
      cstReportsExportParams = new CustomReportsExportParams(cstReportsExportParamsJson);
    } catch (Exception e) {
      xLogger.severe("{0} when getting export params JSON {1} during finalization: {2}",
          e.getClass().getName(), cstReportsExportParams, e.getMessage());
      return;
    }
    // Finalize the exported file
    xLogger.info("Finalizing exported-file {0} in domain {1}...", cstReportsExportParams.fileName,
        cstReportsExportParams.domainId);
    if (cstReportsExportParams.fileName == null) {
      xLogger.warn(
          "No file available to send for template {0} in domain {1}. Possibly nothing to export? Aborting...",
          cstReportsExportParams.templateName, cstReportsExportParams.domainId);
      return;
    }
    // Evaluate the formulas in the excel file (report) before sending the report by email.
    try {
      if (ConfigUtil.isGAE()) {
        SpreadsheetUtil.evaluateFormulas(cstReportsExportParams.fileName);
      }
    } catch (IOException e) {
      xLogger.severe(
          "{0} when evaluating formulas for exported file {1} with template name {2} in domain {3}. Message: {4}",
          e.getClass().getName(), cstReportsExportParams.fileName,
          cstReportsExportParams.templateName, cstReportsExportParams.domainId, e.getMessage());
    }
    String emailCSV = getEmailCSV(cstReportsExportParams);
    if (emailCSV == null || emailCSV.isEmpty()) {
      xLogger.warn(
          "Email CSV is empty. {0} users are either disabled or unavailable in the datastore in domain {1}. Not sending exported data to anyone...",
          cstReportsExportParams.userIds, cstReportsExportParams.domainId);
      return;
    }
    // Send the email and remove the file.
    notifyExport(req, emailCSV, cstReportsExportParams);
    JobUtil.setJobCompleted(cstReportsExportParams.jobId, IJobStatus.TYPE_CUSTOMREPORT,
        cstReportsExportParams.size, cstReportsExportParams.fileName, backendMessages);
  }

  // Get a CSV of email IDs
  private String getEmailCSV(CustomReportsExportParams crParams) {
    String emailCSV = "";
    if (crParams.emIds != null) {
      emailCSV = crParams.emIds;
    }
    String userIdsCSV = crParams.userIds;
    if (userIdsCSV != null && !userIdsCSV.isEmpty()) {

      PersistenceManager pm = PMF.get().getPersistenceManager();
      try {
        List<String> userIds = StringUtil.getList(userIdsCSV);
        for (String userId : userIds) {
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
    }
    return emailCSV;
  }

  private void notifyExport(HttpServletRequest req, String addressCSV,
                            CustomReportsExportParams cstReportsExportParams) {
    xLogger.fine("Entering notifyExport");
    String subject = cstReportsExportParams.subject;
    String fileName = cstReportsExportParams.fileName;
    String body = cstReportsExportParams.body;
    String message;
    // Address
    List<String> addresses = new ArrayList<String>();
    // Check if the address is a CSV of addresses
    String[] addressArray = addressCSV.split(",");
    for (int i = 0; i < addressArray.length; i++) {
      addresses.add(addressArray[i]); // addresses.add( address );
    }
    message = MsgUtil.bold("Custom report data: ") + cstReportsExportParams.templateName;
    if (StringUtils.isNotEmpty(body)) {
      message =
          message.concat(MsgUtil.newLine()).concat(MsgUtil.bold("Description: ")).concat(body);
    }
    InputStream is = null;
    try {
      StorageUtil storageUtil = AppFactory.get().getStorageUtil();
      xLogger.fine("Getting message service instance");
      MessageService
          ms =
          MessageService.getInstance(MessageService.EMAIL, Constants.COUNTRY_DEFAULT);
      if (ConfigUtil.isLogi()) {
        long
            fileSize =
            storageUtil
                .getFileSizeInBytes(CustomReportsExportMgr.CUSTOMREPORTS_BUCKETNAME, fileName);
        if (fileSize >= 5_000_000) { // 5 MB
          String host = ConfigUtil.get("logi.host.server");
          String path = host == null ? "http://localhost:50070/webhdfs/v1" : "/media";
          String localStr = host == null ? "?op=OPEN" : "";
          message +=
              "<br/><br/>Please <a href=\"" + (host == null ? "" : host) + path
                  + "/user/logistimoapp/customreports/" + fileName + localStr
                  + "\">click here</a> to download the file.";
          ((EmailService) ms).sendHTML(null, addresses, subject, message, null);
          return;
        }
      }
      is = storageUtil.getInputStream(CustomReportsExportMgr.CUSTOMREPORTS_BUCKETNAME, fileName);
      String
          attachmentMimeType =
          "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";

      if (fileName.contains(CustomReportConstants.EXTENSION_XLSX)) {
        attachmentMimeType = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
      } else if (fileName.contains(CustomReportConstants.EXTENSION_XLS)) {
        attachmentMimeType = "application/vnd-msexcel";
      } else {
        xLogger.severe("Invalid report. Aborting...");
        return;
      }
      ((EmailService) ms)
          .sendWithAttachmentStream(addresses, message, MessageService.NORMAL, subject, is,
              attachmentMimeType, fileName);
      // storageUtil.removeFile(CustomReportsExportMgr.CUSTOMREPORTS_BUCKETNAME, fileName); // LS-1623 Do not remove the file, since user might want to download it from Logistimo web ui.
    } catch (Exception e) {
      xLogger.severe(
          "{0} during email notification of template {1} to addresses {2} during export of {2}: {3}",
          e.getClass().getName(), cstReportsExportParams.templateName, addressCSV, fileName,
          e.getMessage());
    } finally {
      if (is != null) {
        try {
          is.close();
        } catch (Exception e) {
          xLogger.severe("{0} while closing InputStream is. Message: {1}", e.getClass().getName(),
              e.getMessage());
        }
      }
    }
    xLogger.fine("Exiting notifyExport");
  }

  private void sendJsonResponse(HttpServletResponse response, int statusCode, String jsonString)
      throws IOException {
    response.setStatus(statusCode);
    response.setContentType(JSON_UTF8);
    PrintWriter pw = response.getWriter();
    pw.write(jsonString);
    pw.close();
  }

}


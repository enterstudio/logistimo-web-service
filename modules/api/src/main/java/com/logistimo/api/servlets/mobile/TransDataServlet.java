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

package com.logistimo.api.servlets.mobile;

import com.logistimo.api.servlets.JsonRestServlet;
import com.logistimo.api.servlets.mobile.builders.MobileTransactionsBuilder;
import com.logistimo.api.servlets.mobile.models.ParsedRequest;
import com.logistimo.api.util.GsonUtil;
import com.logistimo.api.util.RESTUtil;
import com.logistimo.config.models.DomainConfig;
import com.logistimo.constants.Constants;
import com.logistimo.dao.JDOUtils;
import com.logistimo.exception.UnauthorizedException;
import com.logistimo.inventory.entity.ITransaction;
import com.logistimo.inventory.service.InventoryManagementService;
import com.logistimo.inventory.service.impl.InventoryManagementServiceImpl;
import com.logistimo.logger.XLog;
import com.logistimo.pagination.PageParams;
import com.logistimo.pagination.Results;
import com.logistimo.proto.JsonTagsZ;
import com.logistimo.proto.MobileTransactionsModel;
import com.logistimo.proto.RestConstantsZ;
import com.logistimo.reports.entity.slices.IDaySlice;
import com.logistimo.reports.entity.slices.ISlice;
import com.logistimo.services.ServiceException;
import com.logistimo.services.Services;
import com.logistimo.services.impl.PMF;
import com.logistimo.users.entity.IUserAccount;
import com.logistimo.utils.LocalDateUtil;
import com.logistimo.utils.QueryUtil;

import org.apache.commons.lang.StringUtils;

import java.io.IOException;
import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Vector;

import javax.jdo.PersistenceManager;
import javax.jdo.Query;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@SuppressWarnings("serial")
public class TransDataServlet extends JsonRestServlet {
  public static final int PERIOD = 1;
  public static final int DEFAULT_MAX_RESULTS = 100;
  public static final int ALLOWED_MAX_RESULTS = 500;
  private static final XLog xLogger = XLog.getLog(TransDataServlet.class);

  public void processGet(HttpServletRequest req, HttpServletResponse res,
                         ResourceBundle backendMessages, ResourceBundle messages)
      throws ServletException, IOException, ServiceException {
    xLogger.fine("Entering processGet");
    // Get the request parameters
    String action = req.getParameter(RestConstantsZ.ACTION);
    if (RestConstantsZ.ACTION_GETTRANSACTIONAGGREGATE.equalsIgnoreCase(action)) {
      getTransAggregate(req, res, backendMessages, messages, action);
    } else if (RestConstantsZ.ACTION_GETTRANSACTIONS_OLD.equalsIgnoreCase(action)) {
      getTransactionsOld(req, res, backendMessages, messages, action);
    } else if (RestConstantsZ.ACTION_GETTRANSACTIONS.equalsIgnoreCase(action)) {
      getTransactions(req, res, backendMessages);
    } else {
      throw new ServiceException("Invalid action: " + action);
    }

    xLogger.fine("Exiting processGet");
  }

  public void processPost(HttpServletRequest req, HttpServletResponse res,
                          ResourceBundle backendMessages, ResourceBundle messages)
      throws ServletException, IOException, ServiceException {
    xLogger.fine("Entering processPost");
    processGet(req, res, backendMessages, messages);
    xLogger.fine("Exiting processPost");
  }

  @SuppressWarnings("rawtypes")
  private void getTransAggregate(HttpServletRequest req, HttpServletResponse res,
                                 ResourceBundle backendMessages, ResourceBundle messages,
                                 String action) throws IOException, ServiceException {
    xLogger.fine("Entering getTransAggregate");
    boolean status = true;
    String errMsg = null;
    String localeStr = Constants.LANG_DEFAULT;
    int statusCode = HttpServletResponse.SC_OK;

    // Read the request parameters
    String userId = req.getParameter(RestConstantsZ.USER_ID);
    String password = req.getParameter(RestConstantsZ.PASSWORD);
    if (userId == null || userId.isEmpty() || password == null || password.isEmpty()) {
      status = false;
      errMsg = "Invalid user name or password";
      sendError(res, localeStr, errMsg);
      return;
    }
    // Proceed only if status is true
    int period = PERIOD;
    String endDateStr = null;
    int numResults = DEFAULT_MAX_RESULTS;

    // Read the optional request parameters
    // Period
    String periodStr = req.getParameter(RestConstantsZ.PERIOD);
    if (periodStr != null && !periodStr.isEmpty()) {
      period = Integer.parseInt(periodStr);
    }
    // End date
    endDateStr = req.getParameter(RestConstantsZ.ENDDATE);
    // Number of results to be returned
    String numResultsStr = req.getParameter(RestConstantsZ.NUM_RESULTS);
    if (numResultsStr != null && !numResultsStr.isEmpty()) {
      numResults = Integer.parseInt(numResultsStr);
    }
    if (numResults > ALLOWED_MAX_RESULTS) {
      numResults = ALLOWED_MAX_RESULTS;
    }

    String offsetStr = req.getParameter(Constants.OFFSET);
    int offset = 0;
    if (StringUtils.isNotBlank(offsetStr)) {
      try {
        offset = Integer.parseInt(offsetStr);
      } catch (Exception e) {
        xLogger.warn("Invalid offset {0}: {1}", offsetStr, e.getMessage());
      }
    }

    Long domainId = null;
    Locale locale = null;
    String timezone = null;
    // Authenticate the user id and password
    try {
      IUserAccount u = RESTUtil.authenticate(userId, password, null, req, res);
      domainId = u.getDomainId();
      locale = u.getLocale();
      timezone = u.getTimezone();
    } catch (ServiceException e) {
      xLogger.severe("Authentication of caller failed: Exception: {0}, Msg: {1} ",
          e.getClass().getName(), e.getMessage());
      errMsg = e.getMessage();
      status = false;
    } catch (UnauthorizedException e) {
      errMsg = e.getMessage();
      status = false;
      statusCode = HttpServletResponse.SC_UNAUTHORIZED;
    }
    // If the caller is not authenticated send error and return
    if (!status) {
      sendError(res, localeStr, errMsg);
      return;
    }

    // Only if the caller is authenticated, proceed
    Results
        reportData =
        getReportData(period, endDateStr, domainId, numResults, offset, locale, timezone);
    if (reportData == null) {
      status = false;
      errMsg = "Invalid report data";
      sendError(res, locale.toString(), errMsg);
      xLogger.severe("{0}", errMsg);
      return;
    }
    // Obtain the list of TrsDaySlice objects
    @SuppressWarnings("unchecked")
    List<? extends ISlice> results = reportData.getResults();
    if (results == null || results.isEmpty()) {
      status = false;
      errMsg = "No Results obtained";
      sendError(res, locale.toString(), errMsg);
      xLogger.fine("{0}", errMsg);
      return;
    }

    // Create a vector of hashtables to hold the results.
    Vector<Hashtable> transDataVector = new Vector<Hashtable>();
    int resultsSize = results.size();
    Iterator<? extends ISlice>
        resultsIter =
        results.iterator(); // Iterate through the list of TransDaySlice objects
    while (resultsIter.hasNext()) {
      ISlice transDaySlice = resultsIter.next();
      Hashtable
          transDaySliceMap =
          transDaySlice.toMap(locale, timezone); // Convert TransDaySlice object to Hashtable
      transDataVector.add(transDaySliceMap); // Store it in the transDataVector
    }
    try {
      // Create an AggTransDataOutput object
      //AggTransDataOutput aggTransDataOutput = new AggTransDataOutput( status, resultsSize, null, transDataVector, errMsg, locale.toString(), RESTUtil.VERSION_01 );
      //sendJsonResponse( res, statusCode, aggTransDataOutput.toJSONString() );
      String
          aggTransDataOutputString =
          GsonUtil.aggTransDataOutputToJson(status, resultsSize, transDataVector, errMsg,
              locale.toString(), RESTUtil.VERSION_01);
      sendJsonResponse(res, statusCode, aggTransDataOutputString);
    } catch (Exception e) {
      xLogger.severe("{0} while creating AggTransDataOutput. Msg: {1}", e.getClass().getName(),
          e.getMessage());
      res.setStatus(500);
    }
    xLogger.fine("Exiting getTransAggregate");
  }

  @SuppressWarnings("unchecked")
  private Results getReportData(int period, String endDateStr, Long domainId, int numResults,
                                int offset, Locale locale, String timezone) {
    xLogger.fine("Entering getReportData");
    // Get the end-date parameter, if given
    Date endDate = null;
    if (endDateStr != null && !endDateStr.isEmpty()) {
      try {
        endDate = LocalDateUtil.parseCustom(endDateStr, Constants.DATE_FORMAT, null);
      } catch (ParseException e) {
        xLogger.warn("Invalid end date...unable to parse {0} into {1}", endDateStr,
            Constants.DATE_FORMAT);
      }
    }
    if (endDate == null) {
      endDate = new Date();
    }
    Calendar cal = GregorianCalendar.getInstance();
    cal.setTime(endDate);
    resetTimeFields(cal); // reset hours/mins./secs/ms to 0
    endDate = cal.getTime();
    xLogger.fine("endDate: {0}", endDate.toString());
    // Go back by given period and set fromDate
    cal.add(Calendar.DATE, -1 * period);
    Date fromDate = cal.getTime();
    xLogger.fine("fromDate: {0}, endDate: {1}", fromDate.toString(), endDate.toString());
    // Form query
    String
        queryStr =
        "SELECT FROM " + JDOUtils.getImplClass(IDaySlice.class).getName()
            + " WHERE dId == dIdParam && oty == otyParam && d > fromParam && d < untilParam PARAMETERS Long dIdParam, String otyParam, Date fromParam, Date untilParam import java.util.Date; ORDER BY d DESC";
    Map<String, Object> params = new HashMap<String, Object>();
    params.put("dIdParam", domainId);
    params.put("otyParam", ISlice.OTYPE_MATERIAL);
    params.put("fromParam", LocalDateUtil.getOffsetDate(fromDate, -1, Calendar.MILLISECOND));
    params.put("untilParam", endDate);
    // Execute query
    PersistenceManager pm = PMF.getReportsPM().getPersistenceManager();
    Query q = pm.newQuery(queryStr);
    PageParams pageParams = new PageParams(offset, numResults);
    QueryUtil.setPageParams(q, pageParams);
    List<IDaySlice> results = null;
    try {
      results = (List<IDaySlice>) q.executeWithMap(params);
      if (results != null) {
        results.size();
        results = (List<IDaySlice>) pm.detachCopyAll(results);
      }
    } finally {
      try {
        q.closeAll();
      } catch (Exception ignored) {

      }
      pm.close();
    }
    xLogger.fine("Exiting getReportData");
    return new Results(results, null);
  }

  private void sendError(HttpServletResponse res, String locale, String errMsg) throws IOException {
    xLogger.fine("Entering sendError");
    try {
      //AggTransDataOutput aggTransDataOutput = new AggTransDataOutput( false, 0, null, null, errMsg, locale.toString(), RESTUtil.VERSION_01 );
      //sendJsonResponse( res, HttpServletResponse.SC_OK,  aggTransDataOutput.toJSONString() );
      String
          aggTransDataOutput =
          GsonUtil.aggTransDataOutputToJson(false, 0, null, errMsg, locale.toString(),
              RESTUtil.VERSION_01);
      sendJsonResponse(res, HttpServletResponse.SC_OK, aggTransDataOutput);
    } catch (Exception e) {
      xLogger.severe("{0} while creating AggTransDataOutput. Msg: {1}", e.getClass().getName(),
          e.getMessage());
      res.setStatus(500);
    }
    xLogger.fine("Exiting sendError");
  }

  private void resetTimeFields(Calendar cal) {
    cal.set(Calendar.HOUR_OF_DAY, 0);
    cal.set(Calendar.MINUTE, 0);
    cal.set(Calendar.SECOND, 0);
    cal.set(Calendar.MILLISECOND, 0);
  }

  private void getTransactionsOld(HttpServletRequest req, HttpServletResponse resp,
                               ResourceBundle backendMessages, ResourceBundle messages,
                               String action) throws IOException, ServiceException {
    String errMessage = null;
    Long kioskId = null;
    boolean status = true;
    Vector<Hashtable<String, String>> transactionList = null;
    Locale locale = new Locale(Constants.LANG_DEFAULT, "");
    String timezone = null;
    Long domainId = null;
    int size = PageParams.DEFAULT_SIZE;
    Date endDate = new Date();
    int statusCode = HttpServletResponse.SC_OK;

    // Get request parameters
    String strKioskId = req.getParameter(RestConstantsZ.KIOSK_ID);
    String strUserId = req.getParameter(RestConstantsZ.USER_ID);
    String password = req.getParameter(RestConstantsZ.PASSWORD);
    String sizeStr = req.getParameter(RestConstantsZ.SIZE);
    String endDateStr = req.getParameter(RestConstantsZ.ENDDATE);
    String tag = req.getParameter(JsonTagsZ.TAGS);

    String offsetStr = req.getParameter(Constants.OFFSET);
    int offset = 0;
    if (StringUtils.isNotBlank(offsetStr)) {
      try {
        offset = Integer.parseInt(offsetStr);
      } catch (Exception e) {
        xLogger.warn("Invalid offset {0}: {1}", offsetStr, e.getMessage());
      }
    }
    PageParams pageParams = null;
    if (sizeStr != null && !sizeStr.isEmpty()) {
      try {
        size = Integer.parseInt(sizeStr);
        pageParams = new PageParams(offset, size);
      } catch (Exception e) {
        xLogger.warn("Invalid number for size: {0}", sizeStr); // size is set to DEFAULT_SIZE
      }
    }
    // Authenticate the user - either with password or via the kioskId/session combination
    try {
      if (strKioskId != null && !strKioskId.isEmpty()) {
        try {
          kioskId = Long.valueOf(strKioskId);
        } catch (NumberFormatException e) {
          xLogger.warn("Invalid kiosk Id {0}: {1}", strKioskId, e.getMessage());
        }
      }
      if (kioskId
          == null) { // kiosk ID is mandatory, and user should have authorization on it (either domain owner, or a operator/manager of it)
        status = false;
        errMessage = backendMessages.getString("error.nokiosk");
        xLogger.severe("kioskId is null");
      } else {
        // Authenticate user
        IUserAccount
            u =
            RESTUtil.authenticate(strUserId, password, kioskId, req,
                resp); // NOTE: throws ServiceException in case of invalid credentials or no authentication
        // Get user metadata
        locale = u.getLocale();
        timezone = u.getTimezone();
        domainId = u.getDomainId();
      }
    } catch (ServiceException e) {
      errMessage = e.getMessage();
      status = false;
    } catch (UnauthorizedException e) {
      errMessage = e.getMessage();
      status = false;
      statusCode = HttpServletResponse.SC_UNAUTHORIZED;
    }
    if (status) {
      try {
        // Get domain config
        DomainConfig dc = DomainConfig.getInstance(domainId);
        if (endDateStr != null && !endDateStr.isEmpty()) {
          // Convert the end date string to a Date format.
          try {
            endDate = LocalDateUtil.parseCustom(endDateStr, Constants.DATE_FORMAT, timezone);
          } catch (ParseException pe) {
            status = false;
            errMessage = backendMessages.getString("error.invalidenddate");
            xLogger.severe("Exception while parsing end date. Exception: {0}, Message: {1}",
                pe.getClass().getName(), pe.getMessage());
          }
        }
        if (status) {
          // Get the transaction list
          Results
              results =
              RESTUtil
                  .getTransactions(domainId, kioskId, locale, timezone, dc, endDate, pageParams, tag);
          transactionList = (Vector<Hashtable<String, String>>) results.getResults();
        }
      } catch (Exception e) {
        xLogger.severe("TransDataServlet Exception: {0}", e);
        status = false;
        errMessage = backendMessages.getString("error.notransactions");
      }
    }
    // Send the response
    try {
      // Get the json return object
      //GetTransactionsOutput jsonOutput = new GetTransactionsOutput( status, ( kioskId != null ? kioskId.toString() : null ), transactionList, errMessage, locale.toString(), RESTUtil.VERSION_01 );
      //sendJsonResponse( resp, statusCode, jsonOutput.toJSONString() );
      String
          jsonOutputString =
          GsonUtil
              .getTransactionsOutputToJson(status, (kioskId != null ? kioskId.toString() : null),
                  transactionList, errMessage, locale.toString(), RESTUtil.VERSION_01);
      sendJsonResponse(resp, statusCode, jsonOutputString);
    } catch (Exception e1) {
      xLogger.severe("TransDataServlet Exception: {0}", e1);
      e1.printStackTrace();
      resp.setStatus(500);
    }
  }

  private void getTransactions(HttpServletRequest req, HttpServletResponse resp,
                               ResourceBundle backendMessages
  ) throws IOException, ServiceException {
    String errMessage = null;
    boolean isValid = true;
    int statusCode = HttpServletResponse.SC_OK;
    MobileTransactionsModel mobileTransactionsModel = null;
    Locale locale = new Locale(Constants.LANG_DEFAULT, "");
    try {
      String kidStr = req.getParameter(RestConstantsZ.KIOSK_ID);
      String userIdStr = req.getParameter(RestConstantsZ.USER_ID);
      String passwordStr = req.getParameter(RestConstantsZ.PASSWORD);
      Long kioskId = null;
      String timezone = null;
      // Authenticate the user - either with password or via the kioskId/session combination
      try {
        if (kidStr != null && !kidStr.isEmpty()) {
          try {
            kioskId = Long.valueOf(kidStr);
          } catch (NumberFormatException e) {
            xLogger.warn("Invalid kiosk Id {0}", kidStr, e);
          }
        }
        // kiosk ID is mandatory, and user should have authorization on it (either domain owner, or a operator/manager of it)
        if (kioskId == null) {
          isValid = false;
          errMessage = backendMessages.getString("error.nokiosk");
          xLogger.severe("kioskId is null");
        } else {
          // Authenticate user
          IUserAccount
              u =
              RESTUtil.authenticate(userIdStr, passwordStr, kioskId, req,
                  resp); // NOTE: throws ServiceException in case of invalid credentials or no authentication
          // Get user metadata
          locale = u.getLocale();
          timezone = u.getTimezone();
        }
      } catch (ServiceException e) {
        errMessage = e.getMessage();
        isValid = false;
      } catch (UnauthorizedException e) {
        errMessage = e.getMessage();
        isValid = false;
        statusCode = HttpServletResponse.SC_UNAUTHORIZED;
      }
      if (!isValid) {
        return;
      }
      Map<String, String> reqParamsMap = new HashMap<>(1);
      reqParamsMap.put(Constants.OFFSET, req.getParameter(Constants.OFFSET));
      reqParamsMap.put(RestConstantsZ.SIZE, req.getParameter(RestConstantsZ.SIZE));
      reqParamsMap.put(RestConstantsZ.ENDDATE, req.getParameter(RestConstantsZ.ENDDATE));
      reqParamsMap
          .put(RestConstantsZ.TRANSACTION_TYPE, req.getParameter(RestConstantsZ.TRANSACTION_TYPE));
      reqParamsMap.put(RestConstantsZ.STARTDATE, req.getParameter(RestConstantsZ.STARTDATE));
      reqParamsMap.put(RestConstantsZ.MATERIAL_ID, req.getParameter(RestConstantsZ.MATERIAL_ID));
      reqParamsMap.put(RestConstantsZ.TAGS, req.getParameter(RestConstantsZ.TAGS));

      ParsedRequest parsedRequest = parseGetTransactionsRequestParams(reqParamsMap, backendMessages,
          timezone);
      if (StringUtils.isNotEmpty(parsedRequest.errMessage)) {
        errMessage = parsedRequest.errMessage;
        isValid = false;
        return;
      }
      PageParams pageParams =
          new PageParams((Integer) parsedRequest.parsedReqMap.get(Constants.OFFSET),
              (Integer) parsedRequest.parsedReqMap.get(RestConstantsZ.SIZE));

      InventoryManagementService
          ims =
          Services.getService(InventoryManagementServiceImpl.class);
      // Get the transactions
      Results
          results =
          ims.getInventoryTransactionsByKiosk(kioskId,
              (Long) parsedRequest.parsedReqMap.get(RestConstantsZ.MATERIAL_ID), (String) parsedRequest.parsedReqMap.get(RestConstantsZ.TAGS),
              (Date) parsedRequest.parsedReqMap.get(RestConstantsZ.STARTDATE),
              (Date) parsedRequest.parsedReqMap.get(RestConstantsZ.ENDDATE),
              (String) parsedRequest.parsedReqMap.get(RestConstantsZ.TRANSACTION_TYPE), pageParams,
              null,
              false, null);
      List<ITransaction> transactions = (List<ITransaction>) results.getResults();
      MobileTransactionsBuilder mobileTransactionsBuilder = new MobileTransactionsBuilder();
      mobileTransactionsModel =
          mobileTransactionsBuilder.build(transactions, kioskId, locale, timezone);
    } finally {
      try {
        String
            jsonOutputString =
            GsonUtil
                .buildGetTransactionsResponseModel(isValid,
                    mobileTransactionsModel, errMessage, RESTUtil.VERSION_01);
        sendJsonResponse(resp, statusCode, jsonOutputString);
      } catch (Exception e1) {
        xLogger.severe("TransDataServlet Exception: {0}", e1);
        resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
      }
    }
  }

  private ParsedRequest parseGetTransactionsRequestParams(Map<String, String> reqParamsMap,
                                                          ResourceBundle backendMessages,
                                                          String timezone) {
    ParsedRequest parsedRequest = new ParsedRequest();
    if (reqParamsMap == null || reqParamsMap.isEmpty()) {
      return parsedRequest;
    }
    String offsetStr = reqParamsMap.get(Constants.OFFSET);
    Integer offset = 0;
    if (StringUtils.isNotEmpty(offsetStr)) {
      try {
        offset = Integer.parseInt(offsetStr);
      } catch (NumberFormatException e) {
        parsedRequest.errMessage = backendMessages.getString("error.invalidoffset");
        xLogger.severe("Exception while parsing offset {0}: ",
            offsetStr, e);
        return parsedRequest;
      }
    }
    parsedRequest.parsedReqMap.put(Constants.OFFSET, offset);
    String sizeStr = reqParamsMap.get(RestConstantsZ.SIZE);
    Integer size = PageParams.DEFAULT_SIZE;
    if (StringUtils.isNotEmpty(sizeStr)) {
      try {
        size = Integer.parseInt(sizeStr);
      } catch (NumberFormatException e) {
        parsedRequest.errMessage = backendMessages.getString("error.invalidsize");
        xLogger.severe("Exception while parsing size {0}: ",
            sizeStr, e);
        return parsedRequest;
      }
    }
    parsedRequest.parsedReqMap.put(RestConstantsZ.SIZE, size);
    String endDateStr = reqParamsMap.get(RestConstantsZ.ENDDATE);
    Date endDate = new Date();
    if (StringUtils.isNotEmpty(endDateStr)) {
      try {
        endDate = LocalDateUtil.parseCustom(endDateStr, Constants.DATE_FORMAT, timezone);
        parsedRequest.parsedReqMap.put(RestConstantsZ.ENDDATE, endDate);
      } catch (ParseException pe) {
        parsedRequest.errMessage = backendMessages.getString("error.invalidenddate");
        xLogger.severe("Exception while parsing end date {0}: ",
            endDateStr, pe);
        return parsedRequest;
      }
    } else {
      parsedRequest.parsedReqMap.put(RestConstantsZ.ENDDATE, endDate);
    }
    String startDateStr = reqParamsMap.get(RestConstantsZ.STARTDATE);
    Date startDate = null;
    if (StringUtils.isNotEmpty(startDateStr)) {
      try {
        startDate = LocalDateUtil.parseCustom(startDateStr, Constants.DATE_FORMAT, timezone);
        parsedRequest.parsedReqMap.put(RestConstantsZ.STARTDATE, startDate);
      } catch (ParseException pe) {
        parsedRequest.errMessage = backendMessages.getString("error.notvalidstartdate");
        xLogger.severe("Exception while parsing start date {0}: ", startDateStr, pe);
        return parsedRequest;
      }
    }
    // If startDate is greater than endDate set the error message
    if (startDate != null && startDate.after(endDate)) {
      parsedRequest.errMessage = backendMessages.getString("error.startdateisgreaterthanenddate");
      return parsedRequest;
    }

    String transTypeStr = reqParamsMap.get(RestConstantsZ.TRANSACTION_TYPE);
    if (StringUtils.isNotEmpty(transTypeStr)) {
      parsedRequest.parsedReqMap.put(RestConstantsZ.TRANSACTION_TYPE, transTypeStr);
    }
    String materialIdStr = reqParamsMap.get(RestConstantsZ.MATERIAL_ID);
    if (StringUtils.isNotEmpty(materialIdStr)) {
      try {
        Long materialId = Long.parseLong(materialIdStr);
        parsedRequest.parsedReqMap.put(RestConstantsZ.MATERIAL_ID, materialId);
      } catch (NumberFormatException e) {
        parsedRequest.errMessage = backendMessages.getString("error.invalidmaterialid");
        xLogger.severe("Exception while parsing material id. {0}",
            materialIdStr, e);
        return parsedRequest;
      }
    }
      String materialTag = reqParamsMap.get(RestConstantsZ.TAGS);
      if(StringUtils.isNotEmpty(materialTag)) {
          parsedRequest.parsedReqMap.put(RestConstantsZ.TAGS, materialTag);
      }
    return parsedRequest;
  }

}

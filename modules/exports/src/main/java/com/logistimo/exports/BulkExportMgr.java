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

/** */
package com.logistimo.exports;

import com.logistimo.AppFactory;
import com.logistimo.auth.SecurityConstants;
import com.logistimo.config.models.DomainConfig;
import com.logistimo.constants.CharacterConstants;
import com.logistimo.constants.Constants;
import com.logistimo.dao.JDOUtils;
import com.logistimo.entities.entity.IKiosk;
import com.logistimo.entities.models.LocationSuggestionModel;
import com.logistimo.entities.service.EntitiesService;
import com.logistimo.entities.service.EntitiesServiceImpl;
import com.logistimo.entity.IJobStatus;
import com.logistimo.entity.IMessageLog;
import com.logistimo.events.entity.IEvent;
import com.logistimo.inventory.dao.IInvntryDao;
import com.logistimo.inventory.dao.ITransDao;
import com.logistimo.inventory.dao.impl.InvntryDao;
import com.logistimo.inventory.dao.impl.TransDao;
import com.logistimo.inventory.entity.IInvntry;
import com.logistimo.inventory.entity.IInvntryBatch;
import com.logistimo.inventory.entity.IInvntryEvntLog;
import com.logistimo.inventory.models.InvntryWithBatchInfo;
import com.logistimo.inventory.service.InventoryManagementService;
import com.logistimo.inventory.service.impl.InventoryManagementServiceImpl;
import com.logistimo.logger.XLog;
import com.logistimo.materials.entity.IMaterial;
import com.logistimo.materials.service.MaterialCatalogService;
import com.logistimo.materials.service.impl.MaterialCatalogServiceImpl;
import com.logistimo.mnltransactions.entity.IMnlTransaction;
import com.logistimo.models.orders.DiscrepancyModel;
import com.logistimo.orders.entity.IOrder;
import com.logistimo.orders.models.DiscrepancyExportableModel;
import com.logistimo.orders.service.IDemandService;
import com.logistimo.orders.service.OrderManagementService;
import com.logistimo.orders.service.impl.DemandService;
import com.logistimo.orders.service.impl.OrderManagementServiceImpl;
import com.logistimo.pagination.PageParams;
import com.logistimo.pagination.QueryParams;
import com.logistimo.pagination.Results;
import com.logistimo.reports.ReportsConstants;
import com.logistimo.reports.entity.slices.IMonthSlice;
import com.logistimo.reports.entity.slices.ISlice;
import com.logistimo.reports.generators.IReportDataGeneratorFactory;
import com.logistimo.reports.generators.ReportDataGenerator;
import com.logistimo.reports.utils.ReportsUtil;
import com.logistimo.services.Resources;
import com.logistimo.services.ServiceException;
import com.logistimo.services.Services;
import com.logistimo.tags.dao.ITagDao;
import com.logistimo.tags.dao.TagDao;
import com.logistimo.tags.entity.ITag;
import com.logistimo.users.dao.IUserDao;
import com.logistimo.users.dao.UserDao;
import com.logistimo.users.entity.IUserAccount;
import com.logistimo.users.service.UsersService;
import com.logistimo.users.service.impl.UsersServiceImpl;
import com.logistimo.utils.LocalDateUtil;

import org.apache.commons.lang.StringUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URLDecoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

import javax.servlet.http.HttpServletRequest;

/**
 * Export data in bulk
 *
 * @author Arun
 */
public class BulkExportMgr {

  public static final String TYPE_ASSETS = "assets";
  // Export types
  // NOTE: values should be same as the resource keys in resource property files
  public static final String TYPE_ORDERS = "orders";
  public static final String TYPE_TRANSACTIONS = "transactions";
  public static final String TYPE_INVENTORY = "inventory";
  public static final String TYPE_USERS = "users";
  public static final String TYPE_MATERIALS = "materials";
  public static final String TYPE_KIOSKS = "kiosks";
  public static final String TYPE_EVENTS = "events";
  public static final String TYPE_ABNORMALSTOCK = "abnormalstock";
  public static final String TYPE_BATCHEXPIRY = "batchexpiry";
  public static final String TYPE_INVENTORYBATCH = "inventorybatch";
  public static final String TYPE_USAGESTATISTICS = "usagestatistics";
  public static final String TYPE_MANUALTRANSACTIONS = "manualtransactions";
  public static final String TYPE_NOTIFICATIONS_STATUS = "notificationStatus";
  public static final String TYPE_DISCREPANCIES = "discrepancies";
  private static final XLog xLogger = XLog.getLog(BulkExportMgr.class);
  // Backend for export
  private static String EXPORT_BACKEND = "backend1";
  private static IUserDao accountDao = new UserDao();
  private static ITagDao tagDao = new TagDao();

  // Get the query parameters for a given type of export
  // NOTE: The keys in params are the same as the request parameter keys in ExportServlet.batchExport()
  public static QueryParams getQueryParams(String type, Long domainId, IReportDataGeneratorFactory iReportDataGeneratorFactory, HttpServletRequest req)
      throws Exception {
    if (type == null || type.isEmpty() || domainId == null) {
      throw new IllegalArgumentException(
          "type or domainId are invalid. Both have to be specified.");
    }
    if (TYPE_USERS.equals(type)) {
      Map<String, Object> filters = getFilterParams(domainId, req);
      return accountDao.getQueryParams(domainId, filters, true);
    }

    // Get the req. parameters, if any
    String fromDateStr =
        req.getParameter("from"); // from date (optional) - see Constants.DATE_FORMAT
    String toDateStr = req.getParameter("to"); // to date (optional) - see Constants.DATE_FORMAT
    String kioskIdStr =
        req.getParameter(
            "kioskid"); // optional, depending on filter criteria (if present, domain Id is not sent as a filter criteria)
    String materialIdStr =
        req.getParameter(
            "materialid"); // optional, depending on filter criteria (if present, domain Id is not sent as a filter criteria)
    String oType =
        req.getParameter("otype"); //depending on entity filter in orders.[sales or purchase]
    //as part of order export configuration "orderType" parameter is not added to export url hence assigning defaul value---LS-4168
    String spTransfer =
        StringUtils.isEmpty(req.getParameter("orderType"))
            ? "1"
            : req.getParameter("orderType"); //is Sales/purchase or transfer
    // Order specific parameters, if any
    String orderStatus = req.getParameter("status");
    // Batch specific parameters
    String expiresBeforeStr = req.getParameter("expiresbefore");
    String batchIdStr = req.getParameter("batchid");
    boolean hasBatchId = batchIdStr != null && !batchIdStr.isEmpty();
    boolean hasExpiresBefore = expiresBeforeStr != null && !expiresBeforeStr.isEmpty();
    // Check if reports type or not
    boolean isReportType = (req.getParameter("reports") != null);
    String trnType = req.getParameter("transactiontype");
    String lkIdParamStr = req.getParameter("lkIdParam");
    String reason = req.getParameter("rsn");
    String pdos = req.getParameter("pdos");
    boolean hasAtd = false;
    if (req.getParameter("atd") != null) {
      hasAtd = req.getParameter("atd").equals("true") ? true : false;
    }
    String discType = req.getParameter("disctype"); // Type of discrepancy
    String eTags =
        req.getParameter("etag"); // Entity tags, multiple possible in case of Inventory export
    String mTag = req.getParameter("mtag"); // Material tag
    String orderIdStr = req.getParameter("orderid"); // Order ID
    boolean etrn = req.getParameter("etrn") != null ? true : false; // Exclude transfer orders

    // Get the parameter objects
    SimpleDateFormat df =
        new SimpleDateFormat(
            Constants.DATETIME_FORMAT); // use hours and minute level granularity, if present
    Date from = null, to = null;
    if (fromDateStr != null && !fromDateStr.isEmpty()) {
      try {
        fromDateStr = URLDecoder.decode(fromDateStr, "UTF-8");
        from = df.parse(fromDateStr);
        from =
            LocalDateUtil.getOffsetDate(
                from, -1, Calendar.MILLISECOND); // offset to enable > query (instead of >=)
      } catch (Exception e) {
        xLogger.warn(
            "{0} when parsing from-date {1}...ignoring this: {2}",
            e.getClass().getName(), fromDateStr, e.getMessage());
      }
    }
    if (toDateStr != null && !toDateStr.isEmpty()) {
      try {
        toDateStr = URLDecoder.decode(toDateStr, "UTF-8");
        to = df.parse(toDateStr);
      } catch (Exception e) {
        xLogger.warn(
            "{0} when parsing to-date {1}...ignoring this: {2}",
            e.getClass().getName(), toDateStr, e.getMessage());
      }
    }
    Long kioskId = null, materialId = null, lkIdParam = null, orderId = null;
    if (kioskIdStr != null && !kioskIdStr.isEmpty()) {
      try {
        kioskId = Long.valueOf(kioskIdStr);
      } catch (Exception e) {
        xLogger.warn(
            "{0} when parsing kioskIdStr {1}...ignoring this: {2}",
            e.getClass().getName(), kioskIdStr, e.getMessage());
      }
    }
    if (materialIdStr != null && !materialIdStr.isEmpty()) {
      try {
        materialId = Long.valueOf(materialIdStr);
      } catch (Exception e) {
        xLogger.warn(
            "{0} when parsing materialIdStr {1}...ignoring this: {2}",
            e.getClass().getName(), materialIdStr, e.getMessage());
      }
    }
    if (lkIdParamStr != null && !lkIdParamStr.isEmpty()) {
      try {
        lkIdParam = Long.valueOf(lkIdParamStr);
      } catch (Exception e) {
        xLogger.warn(
            "{0} when parsing lkIdParamStr {1}...ignoring this: {2}",
            e.getClass().getName(), lkIdParamStr, e.getMessage());
      }
    }
    if (orderIdStr != null && !orderIdStr.isEmpty()) {
      try {
        orderId = Long.valueOf(orderIdStr);
      } catch (Exception e) {
        xLogger.warn("Exception when parsing orderIdStr {0}...ignoring this", orderIdStr, e);
      }
    }
    Date expiresBefore = null;
    LocationSuggestionModel location =
        ReportsUtil.parseLocation(
            req.getParameter("loc") != null
                ? URLDecoder.decode(req.getParameter("loc"), "UTF-8")
                : null);
    if (expiresBeforeStr != null && !expiresBeforeStr.isEmpty()) {
      try {
        expiresBeforeStr = URLDecoder.decode(expiresBeforeStr, "UTF-8");
        expiresBefore = df.parse(expiresBeforeStr);
      } catch (Exception e) {
        xLogger.warn(
            "{0} when parsing expires-before-date {1}...ignoring this: {2}",
            e.getClass().getName(), expiresBeforeStr, e.getMessage());
      }
    }
    if (StringUtils.isNotEmpty(eTags)) {
      eTags = URLDecoder.decode(eTags, "UTF-8");
    }

    if (StringUtils.isNotEmpty(mTag)) {
      mTag = URLDecoder.decode(mTag, "UTF-8");
    }

    if (StringUtils.isNotEmpty(reason)) {
      reason = URLDecoder.decode(reason, "UTF-8");
    }

    if (isReportType) {
      return getReportQueryParams(type, domainId, iReportDataGeneratorFactory, req);
      // TODO: return getReportQueryParams(...) - use ReportDataGeneratorFactory, get generator instance based on type, ReportDataGenerator.getQueryParams(...filters...) - DO NULL CHECK AND LOG Severe error; for filters use ReportUtils.getFilters
    }
    UsersService as = Services.getService(UsersServiceImpl.class);
    EntitiesService es = Services.getService(EntitiesServiceImpl.class);
    // Get the kiosk ids for the user who initiated the export
    String sourceUserId = req.getParameter("sourceuserid");
    IUserAccount user = as.getUserAccount(sourceUserId);
    List<Long> kioskIds = null;
    if (SecurityConstants.ROLE_SERVICEMANAGER.equals(user.getRole())) {
      kioskIds = es.getKioskIdsForUser(user.getUserId(), null, null).getResults();
    }
    if (TYPE_DISCREPANCIES.equals(type)) {
      IDemandService ds = Services.getService(DemandService.class);
      return ds.getQueryParams(
          domainId,
          oType,
          etrn,
          kioskId,
          kioskIds,
          materialId,
          eTags,
          mTag,
          from,
          to,
          orderId,
          discType,
          null);
    } else if (TYPE_INVENTORY.equals(type) || (TYPE_INVENTORYBATCH.equals(type))) {
      IInvntryDao invntryDao = new InvntryDao();
      return invntryDao.buildInventoryQuery(
          kioskId,
          materialId,
          eTags != null ? Arrays.asList(eTags.split(CharacterConstants.COMMA)) : null,
          null,
          mTag,
          null,
          null,
          domainId,
          null, IInvntry.ALL, false, location, false, pdos);
    } else if (TYPE_TRANSACTIONS.equals(type)) {
      ITransDao transDao = new TransDao();
      return transDao.buildTransactionsQuery(from, to, domainId, kioskId, materialId,
          trnType != null ? Collections.singletonList(trnType) : null, lkIdParam, eTags, mTag,
          kioskIds, batchIdStr, hasAtd, reason, null);
    }
    // Form query params.
    String queryStr = "";
    String paramsStr = " PARAMETERS ";
    String variablesStr = "";
    String orderingStr = null;
    String dateField = null;
    Map<String, Object> params = new HashMap<String, Object>();
    // Generic query parameters
    // kioskId or domainId
    if (kioskId != null) {
      if (StringUtils.isNotEmpty(oType) && oType.equalsIgnoreCase("sle")) {
        queryStr += "skId == kIdParam";
      } else {
        queryStr += "kId == kIdParam";
      }
      paramsStr += "Long kIdParam";
      params.put("kIdParam", kioskId);
    } else if (materialId != null) {
      queryStr += "mId == mIdParam && dId.contains(dIdParam)";
      paramsStr += "Long mIdParam, Long dIdParam";
      params.put("mIdParam", materialId);
      params.put("dIdParam", domainId);
    } else {
      if (TYPE_USAGESTATISTICS.equals(type)) {
        queryStr += "dId == dIdParam";
      } else {
        queryStr += "dId.contains(dIdParam)";
      }
      paramsStr += "Long dIdParam";
      params.put("dIdParam", domainId);
    }
    // Object-specific query
    if (TYPE_ORDERS.equals(type)) {
      queryStr =
          "SELECT FROM " + JDOUtils.getImplClass(IOrder.class).getName() + " WHERE " + queryStr;
      // Order status, if any
      if (orderStatus != null && !orderStatus.isEmpty()) {
        queryStr += " && st == stParam";
        paramsStr += ",String stParam";
        params.put("stParam", orderStatus);
      }

      queryStr += " && oty == otParam";
      paramsStr += ",Long otParam";
      params.put("otParam", Integer.parseInt(spTransfer));

      dateField = "cOn";
      orderingStr = "ORDER BY cOn DESC";
    } else if (TYPE_MATERIALS.equals(type)) {
      queryStr =
          "SELECT FROM " + JDOUtils.getImplClass(IMaterial.class).getName() + " WHERE " + queryStr;
      orderingStr = "ORDER BY uName ASC";
    } else if (TYPE_KIOSKS.equals(type)) {
      queryStr =
          "SELECT FROM " + JDOUtils.getImplClass(IKiosk.class).getName() + " WHERE " + queryStr;
      orderingStr = "ORDER BY nName ASC";
    } else if (TYPE_EVENTS.equals(type)) {
      queryStr =
          "SELECT FROM "
              + JDOUtils.getImplClass(IEvent.class).getName()
              + " WHERE "
              + queryStr; // Typically, domainId is the param. present
      dateField = "t";
      orderingStr = "ORDER BY t DESC";
    } else if (TYPE_ASSETS.equals(type)) {
      queryStr =
          "SELECT FROM " + JDOUtils.getImplClass(IKiosk.class).getName() + " WHERE " + queryStr;
    } else if (TYPE_BATCHEXPIRY.equals(type)) {
      if (hasBatchId || hasExpiresBefore) {
        if (hasBatchId) {
          queryStr += " && bid == bidParam";
          paramsStr += ",String bidParam";
          params.put("bidParam", batchIdStr);
        }
        if (materialId == null && mTag != null && !mTag.isEmpty()) {
          queryStr += " && mtgs.contains(mtgsParam)";
          paramsStr += ",Long mtgsParam";
          params.put("mtgsParam", tagDao.getTagFilter(mTag, ITag.MATERIAL_TAG));
        }
        if (kioskId == null && eTags != null && !eTags.isEmpty()) {
          queryStr += " && ktgs.contains(etgsParam)";
          paramsStr += ",Long etgsParam";
          params.put("etgsParam", tagDao.getTagFilter(eTags, ITag.KIOSK_TAG));
        }
        if (location != null && location.isNotEmpty()) {
          variablesStr += " VARIABLES " + JDOUtils.getImplClass(IKiosk.class).getName() + " kiosk";
          queryStr += " && kId == kiosk.kioskId";
          if (StringUtils.isNotEmpty(location.state)) {
            queryStr += " && kiosk.state == stateParam";
            params.put("stateParam", location.state);
            paramsStr += ", String stateParam";
          }
          if (StringUtils.isNotEmpty(location.district)) {
            queryStr += " && kiosk.district == districtParam";
            params.put("districtParam", location.district);
            paramsStr += ", String districtParam";
          }
          if (StringUtils.isNotEmpty(location.taluk)) {
            queryStr += " && kiosk.taluk == talukParam";
            params.put("talukParam", location.taluk);
            paramsStr += ", String talukParam";
          }
        }
        // Hard code the vld field to true
        queryStr += " && vld == vldParam";
        paramsStr += ",Boolean vldParam";
        params.put("vldParam", Boolean.TRUE);
        // Add the expiryDate param to the orderingStr.
        queryStr =
            "SELECT FROM "
                + JDOUtils.getImplClass(IInvntryBatch.class).getName()
                + " WHERE "
                + queryStr;
        dateField = "bexp";
        orderingStr = "ORDER BY bexp ASC";
      } else {
        xLogger.severe(
            "Invalid or null batch id: {0} and batch expiry date: {1} for type: {2}",
            batchIdStr, expiresBeforeStr, type);
        return null;
      }
    } else if (TYPE_USAGESTATISTICS.equals(type)) {
      queryStr =
          "SELECT FROM "
              + JDOUtils.getImplClass(IMonthSlice.class).getName()
              + " WHERE oty == otyParam && dt == dtParam";
      paramsStr = " PARAMETERS Long otyParam, String dtParam";
      params.put("otyParam", ISlice.OTYPE_DOMAIN);
      params.put("dtParam", ReportsConstants.FILTER_DOMAIN);
      dateField = "d";
      orderingStr = "ORDER BY d DESC, tc DESC";
    } else if (TYPE_MANUALTRANSACTIONS.equals(
        type)) { // manual transactions uploaded into MnlTransactions
      queryStr =
          "SELECT FROM "
              + JDOUtils.getImplClass(IMnlTransaction.class).getName()
              + " WHERE "
              + queryStr;
      dateField = "rp";
      orderingStr = "ORDER BY rp DESC";
    } else if (TYPE_NOTIFICATIONS_STATUS.equals(type)) {
      queryStr =
          "SELECT FROM "
              + JDOUtils.getImplClass(IMessageLog.class).getName()
              + " WHERE "
              + "dId == dIdParam && notif == notifParam ";
      paramsStr += ",Integer notifParam ";
      params.put("notifParam", 1);
      dateField = "t";
      orderingStr = "ORDER BY t DESC";
    } else {
      xLogger.severe("Invalid type: {0}", type);
      return null;
    }

    if (from != null && dateField != null) {
      //if ( type.equals( TYPE_USAGESTATISTICS ) ) {
      //queryStr += " && " + dateField + " == fromParam"; // d == fromParam for usagestatistics
      //} else {
      queryStr += " && " + dateField + " > fromParam";
      //}
      paramsStr += ",Date fromParam";
      params.put("fromParam", from);
    }
    // To date, if any
    if (to != null && dateField != null) {
      queryStr += " && " + dateField + " < toParam";
      paramsStr += ",Date toParam";
      params.put("toParam", to);
    }
    // Expiry date, if any
    if (expiresBefore != null && dateField != null) {
      queryStr += " && " + dateField + " < bexpParam";
      paramsStr += ",Date bexpParam";
      params.put("bexpParam", expiresBefore);
    }
    // Add parameters
    queryStr += variablesStr + paramsStr;
    // Add date import, if needed
    if (from != null || to != null || expiresBefore != null) {
      queryStr += " import java.util.Date;";
    }
    // Add ordering, if needed
    if (orderingStr != null) {
      queryStr += " " + orderingStr;
    }
    xLogger.info("Export query: {0}, params: {1}", queryStr, params);

    return new QueryParams(queryStr, params);
  }

  // Private method that returns the report query params
  private static QueryParams getReportQueryParams(
      String type, Long domainId, IReportDataGeneratorFactory iReportDataGeneratorFactory, HttpServletRequest req) {
    xLogger.fine("Entering getReportQueryParams");
    // use ReportDataGeneratorFactory, get generator instance based on type, ReportDataGenerator.getQueryParams(...filters...)
    //Read the required parameters for generating report query
    // Get the req. parameters, if any
    String fromDateStr =
        req.getParameter("from"); // from date (optional) - see Constants.DATE_FORMAT
    String toDateStr = req.getParameter("to"); // to date (optional) - see Constants.DATE_FORMAT
    // Get the parameter objects
    SimpleDateFormat df = new SimpleDateFormat(Constants.DATE_FORMAT);
    Date from = null, to = null;
    if (fromDateStr != null && !fromDateStr.isEmpty()) {
      try {
        from = df.parse(fromDateStr);
      } catch (Exception e) {
        xLogger.warn(
            "{0} when parsing from-date {1}...ignoring this: {2}",
            e.getClass().getName(), fromDateStr, e.getMessage());
      }
    }
    if (toDateStr != null && !toDateStr.isEmpty()) {
      try {
        to = df.parse(toDateStr);
      } catch (Exception e) {
        xLogger.warn(
            "{0} when parsing to-date {1}...ignoring this: {2}",
            e.getClass().getName(), fromDateStr, e.getMessage());
      }
    }
    xLogger.fine("from: {0}, to: {1}", from, to);

    String frequencyStr = req.getParameter("frequency");
    // Create the filter map
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
        xLogger.severe(
            "{0} when getting source user {1} in domain {2}: {3}",
            e.getClass().getName(), sourceUserIdStr, domainId, e.getMessage());
        return null;
      }
    }
    PageParams pageParams = null;
    QueryParams qp = null;
    try {
      ReportDataGenerator rdg = iReportDataGeneratorFactory.getInstance(type);
      qp =
          rdg.getReportQuery(
              from, to, frequencyStr, filters, locale, timezone, pageParams, dc, sourceUserIdStr);
    } catch (Exception e) {
      xLogger.severe(
          "{0} while getting report query params for type {1} in domain {2}. Message: {3}",
          e.getClass().getName(), type, domainId, e.getMessage());
    }
    xLogger.fine("Exiting getReportQueryParams");
    return qp;
  }

  public static Map<String, String> getExportBackendHeader() {
    Map<String, String> headers = new HashMap<String, String>();
    headers.put("Host", AppFactory.get().getBackendService().getBackendAddress(EXPORT_BACKEND));
    return headers;
  }

  @SuppressWarnings("unchecked")
  public static List<InvntryWithBatchInfo> getInvntryWithBatchInfoList(Results res) {
    // Check if invList is null or empty, if yes, return an empty List of InvntryWithBatchInfo objects, so as to avoid NullPointerException in the ExportProcessor.
    // Create an new List<InvntryWithBatchInfo>
    // If invList is not null, iterate through invList
    // For each Invntry, check if the material is batch enabled.
    // If yes, then call a method to get a List of InvntryWithBatchInfo objects from the Invntry object, and set it's batch parameters from the InvntryBatch objects
    // If no, then create an InvntryWithBatchInfo object from Invntry object
    // Add to the List<InvntryWithBatchInfo>
    // return
    List<InvntryWithBatchInfo> invWithBatchInfoList = new ArrayList<>();
    if (res == null) {
      return invWithBatchInfoList;
    }

    Iterator<IInvntry> invListIter = res.getResults().iterator();
    MaterialCatalogService mcs;
    InventoryManagementService ims;
    EntitiesService as;
    IInvntryDao invDao;
    try {
      mcs = Services.getService(MaterialCatalogServiceImpl.class);
      ims = Services.getService(InventoryManagementServiceImpl.class);
      as = Services.getService(EntitiesServiceImpl.class);
      invDao = new InvntryDao();
    } catch (ServiceException e) {
      xLogger.severe("Exception while creating services during batch export", e);
      return invWithBatchInfoList;
    }
    while (invListIter.hasNext()) {
      IInvntry inv = invListIter.next();
      Long mId = inv.getMaterialId();
      Long kId = inv.getKioskId();
      IInvntryEvntLog lastEventLog = invDao.getInvntryEvntLog(inv);
      try {

        IMaterial m = mcs.getMaterial(inv.getMaterialId());
        IKiosk k = as.getKiosk(inv.getKioskId());
        List<InvntryWithBatchInfo> tempList = new ArrayList<>();
        if (m.isBatchEnabled() && k.isBatchMgmtEnabled()) {
          List<IInvntryBatch> invBatchList;
          Results results = ims.getValidBatches(mId, kId, null);
          // results is never null because getValidBatches
          List<IInvntryBatch> resultsList = results.getResults();

          if (resultsList != null && !resultsList.isEmpty()) {
            invBatchList = results.getResults();
            // Iterate through the invBatchList
            for (IInvntryBatch invBatch : invBatchList) {
              InvntryWithBatchInfo invWithBatchInfo = new InvntryWithBatchInfo();
              invWithBatchInfo.setInvntryParameters(inv);
              invWithBatchInfo.setBatchInfoParameters(invBatch);
              if (lastEventLog != null && inv.getStockEvent() != IEvent.NORMAL) {
                invWithBatchInfo.setInvntryEventParameters(lastEventLog);
              }
              tempList.add(invWithBatchInfo);
            }
          } else {
            xLogger.fine(
                "There are no valid batches for material {0} in kiosk {1}",
                m.getName(), k.getName());
            // Create the InvntryWithBatchInfo object but set only the Invntry parameters
            InvntryWithBatchInfo invWithBatchInfo = new InvntryWithBatchInfo();
            invWithBatchInfo.setInvntryParameters(inv);
            if (lastEventLog != null && inv.getStockEvent() != IEvent.NORMAL) {
              invWithBatchInfo.setInvntryEventParameters(lastEventLog);
            }
            tempList.add(invWithBatchInfo);
          }
        } else {
          // Create the InvntryWithBatchInfo object but set only the Invntry parameters
          InvntryWithBatchInfo invWithBatchInfo = new InvntryWithBatchInfo();
          invWithBatchInfo.setInvntryParameters(inv);
          if (lastEventLog != null && inv.getStockEvent() != IEvent.NORMAL) {
            invWithBatchInfo.setInvntryEventParameters(lastEventLog);
          }
          tempList.add(invWithBatchInfo);
        }
        invWithBatchInfoList.addAll(tempList);
      } catch (ServiceException e) {
        xLogger.severe(
            "{0} when trying to get material with mId {1}. Message: {2}",
            e.getClass().getName(), mId, e.getMessage());
        continue;
      }
    } // end while
    return invWithBatchInfoList;
  }

  private static Map<String, Object> getFilterParams(Long domainId, HttpServletRequest req) {
    DomainConfig dc = DomainConfig.getInstance(domainId);
    Map<String, Object> filterParams = new HashMap<>();
    String nNameStr = req.getParameter("nname");
    try {
      nNameStr = URLDecoder.decode(nNameStr, "UTF-8");
    } catch (Exception e) {
      xLogger.warn("Exception when parsing from nname {0}", nNameStr, e);
    }
    if (StringUtils.isNotEmpty(nNameStr)) {
      filterParams.put("nName", nNameStr);
    }
    String mobilePhoneNumberStr = req.getParameter("mobilephonenumber");
    if (StringUtils.isNotEmpty(mobilePhoneNumberStr)) {
      try {
        mobilePhoneNumberStr = URLDecoder.decode(mobilePhoneNumberStr, "UTF-8");
      } catch (Exception e) {
        xLogger.warn(
            "Exception when parsing from mobile phone number {0}", mobilePhoneNumberStr, e);
      }
      if (!mobilePhoneNumberStr.startsWith("+")) {
        mobilePhoneNumberStr = "+" + mobilePhoneNumberStr;
      }
      filterParams.put("mobilePhoneNumber", mobilePhoneNumberStr);
    }
    String roleStr = req.getParameter("role");
    if (StringUtils.isNotEmpty(roleStr)) {
      try {
        roleStr = URLDecoder.decode(roleStr, "UTF-8");
      } catch (Exception e) {
        xLogger.warn("Exception when parsing from role {0}", roleStr, e);
      }
      filterParams.put("role", roleStr);
    }
    String isEnabledStr = req.getParameter("isenabled");
    if (StringUtils.isNotEmpty(isEnabledStr)) {
      filterParams.put("isEnabled", Boolean.parseBoolean(isEnabledStr));
    }
    String versionStr = req.getParameter("v");
    if (StringUtils.isNotEmpty(versionStr)) {
      try {
        versionStr = URLDecoder.decode(roleStr, "UTF-8");
      } catch (Exception e) {
        xLogger.warn("Exception when parsing from version {0}", versionStr, e);
      }
      filterParams.put("v", versionStr);
    }
    String fromDateStr = req.getParameter("from");
    if (StringUtils.isNotEmpty(fromDateStr)) {
      try {
        fromDateStr = URLDecoder.decode(fromDateStr, "UTF-8");
        Date fromDate =
            LocalDateUtil.parseCustom(fromDateStr, Constants.DATETIME_FORMAT, dc.getTimezone());
        filterParams.put("from", fromDate);
      } catch (Exception e) {
        xLogger.warn("Exception when parsing from date {0}", fromDateStr, e);
      }
    }
    String toDateStr = req.getParameter("to");
    if (StringUtils.isNotEmpty(toDateStr)) {
      try {
        toDateStr = URLDecoder.decode(toDateStr, "UTF-8");
        Date toDate =
            LocalDateUtil.parseCustom(toDateStr, Constants.DATETIME_FORMAT, dc.getTimezone());
        filterParams.put("to", toDate);
      } catch (Exception e) {
        xLogger.warn("Exception when parsing from date {0}", toDateStr, e);
      }
    }
    String neverLoggedStr = req.getParameter("neverlogged");
    if (StringUtils.isNotEmpty(neverLoggedStr)) {
      filterParams.put("neverLogged", true);
    }
    String utagStr = req.getParameter("utag");
    try {
      if (StringUtils.isNotEmpty(utagStr)) {
        utagStr = URLDecoder.decode(utagStr, "UTF-8");
        filterParams.put("utag", utagStr);
      }
    } catch (Exception e) {
      xLogger.warn("Error while decoding the user tag : {0}", utagStr, e);
    }
    return filterParams;
  }

  public static List<DiscrepancyExportableModel> getDiscrepancyExportableModels(Results res) {
    List<DiscrepancyExportableModel> discExportList = new ArrayList<>(1);
    try {
      List<DiscrepancyModel> discModels;
      if (res == null) {
        return discExportList;
      }
      IDemandService ds = Services.getService(DemandService.class);
      discModels = ds.getDiscrepancyModels(res.getResults());
      for (DiscrepancyModel dm : discModels) {
        DiscrepancyExportableModel dem = new DiscrepancyExportableModel(dm);
        discExportList.add(dem);
      }
    } catch (Exception e) {
      xLogger.severe("Exception when trying to get DemandService", e);
    }
    return discExportList;
  }

  public static List<IOrder> getOrdersWithItems(Results res) {
    List<IOrder> ordWithItemsList = new ArrayList<>(1);
    try {
      if (res == null) {
        return ordWithItemsList;
      }
      OrderManagementService oms = Services.getService(OrderManagementServiceImpl.class);
      List<IOrder> ords = res.getResults();
      for (IOrder or : ords) {
        IOrder orwithIt = oms.getOrder(or.getOrderId(), true);
        ordWithItemsList.add(orwithIt);
      }
    } catch (Exception e) {
      xLogger.severe("Exception when trying to get orders with items", e);
    }
    return ordWithItemsList;
  }

  public static String getExportJobStatusDisplay(int status, Locale locale) {
    ResourceBundle messages = Resources.get().getBundle("Messages", locale);
    if (messages == null) {
      return "unknown";
    }
    String name = "";
    if (IJobStatus.INPROGRESS == status) {
      name = messages.getString("inprogress");
    } else if (IJobStatus.COMPLETED == status) {
      name = messages.getString("completed");
    } else if (IJobStatus.FAILED == status) {
      name = messages.getString("failed");
    } else {
      name = "unknown";
    }

    return name;
  }

  public static class ExportParams {

    private static final String TYPE = "ty";
    private static final String COUNTRY = "cn";
    private static final String CURRENCY = "cu";
    private static final String DOMAINID = "dm";
    private static final String FILENAME = "fn";
    private static final String FROM = "from";
    private static final String TO = "to";
    private static final String KIOSKID = "kid";
    private static final String LANGUAGE = "ln";
    private static final String MATERIALID = "mid";
    private static final String TIMEZONE = "tz";
    private static final String GCSFILENAME = "gcsfn";
    private static final String SIZE = "sz";
    private static final String USERIDS = "uids";
    private static final String SOURCEUSERID = "suid";
    private static final String TRANSACTIONTYPE = "type";
    private static final String LKIOSKID = "lkId";
    private static final String JOBID = "jid";
    private static final String SUBTYPE = "sty";
    private static final String USERTAGS = "utgs";
    private static final String ASSETID = "aid";
    private static final String ASSETYPENAME = "atynm";
    private static final String SENSORNAME = "snnm";
    private static final String EMAILID = "emailid";

    public String type = null;
    public String subType = null;
    public Long domainId = null;
    public Long kioskId = null;
    public Long materialId = null;
    public Locale locale = new Locale(Constants.LANG_DEFAULT, Constants.COUNTRY_DEFAULT);
    public String timezone = Constants.TIMEZONE_DEFAULT;
    public String userIds = null; // recepient user Ids CSV
    public String userTags = null; // recepient user tags CSV
    public String sourceUserId = null;
    public String currency = null;
    public String gcsFilename = null; // file name of the exported file in Google Cloud Storage
    public String filename = null; // filename to be used for email attachment
    public int size = 0; // number of records so far contained within the blobHandle
    public Date from;
    public Date to;
    public String transactionType = null;
    public Long lkioskId = null;
    public Long jobId = null;
    public String batchId = null;
    public String assetId;
    public String asseTyNm;
    public String sensorName;
    public String emailId;

    public ExportParams() {
    }

    public ExportParams(String exportParamsJSON) throws JSONException {
      JSONObject json = new JSONObject(exportParamsJSON);
      type = json.getString(TYPE);
      try {
        subType = json.getString(SUBTYPE);
      } catch (Exception e) {
        //ignore
      }
      domainId = new Long(json.getLong(DOMAINID));
      try {
        kioskId = json.getLong(KIOSKID);
      } catch (Exception e) {
        // ignore
      }
      try {
        materialId = json.getLong(MATERIALID);
      } catch (Exception e) {
        // ignore
      }
      locale = new Locale(json.getString(LANGUAGE), json.getString(COUNTRY));
      timezone = json.getString(TIMEZONE);
      userIds = json.getString(USERIDS);
      try {
        userTags = json.getString(USERTAGS);
      } catch (Exception e) {
        //ignore
      }

      sourceUserId = json.getString(SOURCEUSERID);
      try {
        currency = json.getString(CURRENCY);
      } catch (Exception e) {
        // ignore
      }
      try {
        gcsFilename = json.getString(GCSFILENAME);
      } catch (Exception e) {
        // ignore
      }
      try {
        filename = json.getString(FILENAME);
      } catch (Exception e) {
        // ignore
      }
      try {
        from = new Date(json.getLong(FROM));
      } catch (Exception e) {
        // ignore
      }
      try {
        to = new Date(json.getLong(TO));
      } catch (Exception e) {
        // ignore
      }
      jobId = json.getLong(JOBID);
      size = json.getInt(SIZE);
      try {
        assetId = json.getString(ASSETID);
      } catch (Exception e) {

      }
      try {
        asseTyNm = json.getString(ASSETYPENAME);
      } catch (Exception e) {

      }
      try {
        sensorName = json.getString(SENSORNAME);
      } catch (Exception e) {

      }
      try {
        emailId = json.getString(EMAILID);
      } catch (Exception e) {
        // ignore
      }
    }

    public String toJSONString() throws JSONException {
      JSONObject json = new JSONObject();
      json.put(TYPE, type);
      if (subType != null) {
        json.put(SUBTYPE, subType);
      }
      if (domainId != null) {
        json.put(DOMAINID, domainId);
      }
      if (kioskId != null) {
        json.put(KIOSKID, kioskId);
      }
      if (materialId != null) {
        json.put(MATERIALID, materialId);
      }
      if (locale != null) {
        json.put(LANGUAGE, locale.getLanguage());
        json.put(COUNTRY, locale.getCountry());
      }
      if (transactionType != null) {
        json.put(TRANSACTIONTYPE, transactionType);
      }
      if (lkioskId != null) {
        json.put(LKIOSKID, lkioskId);
      }
      if (timezone != null) {
        json.put(TIMEZONE, timezone);
      }
      if (userIds != null) {
        json.put(USERIDS, userIds);
      }
      if (userTags != null) {
        json.put(USERTAGS, userTags);
      }
      if (sourceUserId != null) {
        json.put(SOURCEUSERID, sourceUserId);
      }
      if (currency != null) {
        json.put(CURRENCY, currency);
      }
      if (gcsFilename != null) {
        json.put(GCSFILENAME, gcsFilename);
      }
      if (filename != null) {
        json.put(FILENAME, filename);
      }
      if (from != null) {
        json.put(FROM, from.getTime());
      }
      if (to != null) {
        json.put(TO, to.getTime());
      }
      json.put(SIZE, size);
      json.put(JOBID, jobId);
      if (assetId != null) {
        json.put(ASSETID, assetId);
      }
      if (asseTyNm != null) {
        json.put(ASSETYPENAME, asseTyNm);
      }
      if (sensorName != null) {
        json.put(SENSORNAME, sensorName);
      }
      if (emailId != null) {
        json.put(EMAILID, emailId);
      }
      return json.toString();
    }
  }
}

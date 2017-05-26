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
import com.logistimo.auth.SecurityConstants;
import com.logistimo.config.service.ConfigurationMgmtService;
import com.logistimo.config.service.impl.ConfigurationMgmtServiceImpl;
import com.logistimo.dao.JDOUtils;
import com.logistimo.domains.entity.IDomain;
import com.logistimo.domains.service.DomainsService;
import com.logistimo.domains.service.impl.DomainsServiceImpl;
import com.logistimo.entities.entity.IKiosk;
import com.logistimo.entities.service.EntitiesService;
import com.logistimo.entities.service.EntitiesServiceImpl;
import com.logistimo.events.entity.IEvent;
import com.logistimo.inventory.entity.IInvntry;
import com.logistimo.inventory.entity.IInvntryBatch;
import com.logistimo.inventory.entity.IInvntryEvntLog;
import com.logistimo.inventory.entity.IInvntryLog;
import com.logistimo.inventory.entity.ITransaction;
import com.logistimo.inventory.optimization.entity.IOptimizerLog;
import com.logistimo.inventory.pagination.processor.InventoryResetProcessor;
import com.logistimo.materials.entity.IMaterial;
import com.logistimo.mnltransactions.entity.IMnlTransaction;
import com.logistimo.orders.entity.IDemandItem;
import com.logistimo.orders.entity.IDemandItemBatch;
import com.logistimo.orders.entity.IOrder;
import com.logistimo.reports.entity.slices.IDaySlice;
import com.logistimo.reports.entity.slices.IMonthSlice;
import com.logistimo.services.cache.MemcacheService;
import com.logistimo.services.taskqueue.ITaskService;
import com.logistimo.users.entity.IUserAccount;
import com.logistimo.users.service.UsersService;
import com.logistimo.users.service.impl.UsersServiceImpl;

import com.logistimo.domains.IMultiDomain;
import com.logistimo.entity.IBBoard;
import com.logistimo.config.entity.IConfig;
import com.logistimo.entity.IDownloaded;
import com.logistimo.entity.IUploaded;
import com.logistimo.entity.IUploadedMsgLog;
import com.logistimo.pagination.PageParams;
import com.logistimo.pagination.PagedExec;
import com.logistimo.pagination.QueryParams;
import com.logistimo.domains.processor.DeleteProcessor;
import com.logistimo.api.security.SecurityMgr;
import com.logistimo.services.Services;
import com.logistimo.services.impl.PMF;
import com.logistimo.constants.Constants;
import com.logistimo.utils.Counter;
import com.logistimo.utils.HttpUtil;
import com.logistimo.models.ICounter;
import com.logistimo.utils.LocalDateUtil;
import com.logistimo.api.util.SessionMgr;
import com.logistimo.logger.XLog;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.jdo.JDOObjectNotFoundException;
import javax.jdo.PersistenceManager;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


/**
 * Administrative tasks
 *
 * @author Arun
 */
public class AdminServlet extends HttpServlet {

  private static final XLog xLogger = XLog.getLog(AdminServlet.class);

  // Actions
  private static final String ACTION_RESETCACHECOUNT = "resetcachecount";
  private static final String ACTION_RESETCOUNT = "resetcount";
  private static final String ACTION_DELETETRANSSLICES = "deletetransslices";
  private static final String ACTION_ADDCOUNT = "addcount";
  private static final String ACTION_RESETTRANSACTIONS = "resettransactions";
  private static final String ACTION_DELETEENTITIES = "deleteentities";
  private static final String ACTION_DELETEENTITIESBYQUERY = "deleteentitiesbyquery";
  private static final String ACTION_DELETESESSIONS = "deletesessions";
  private static final String ACTION_GETSYSCONFIG = "getsysconfig";
  private static final String ACTION_UPDATESYSCONFIG = "updatesysconfig";
  private static final String ACTION_BACKUPDATA = "backupdata";

  private static final String ACTION_CLOSINGSTOCKINTRANS = "closingstockintrans";

  // URLs
  private static final String URL_PROD = "https://logistimo-web.appspot.com";
  private static final String URL_BACKUPDATA = "/_ah/datastore_admin/backup.create";

  private static final String PROD_APP_NAME = "logistimo-web";

  // Classes not be backed up
  private static final String[]
      DO_NOT_BACKUP_LIST =
      {"ShardState", "MapReduceState", JDOUtils.getImplClass(IDaySlice.class).getSimpleName(),
          JDOUtils.getImplClass(IMonthSlice.class).getSimpleName(),
          JDOUtils.getImplClass(IDownloaded.class).getSimpleName(),
          JDOUtils.getImplClass(IUploaded.class).getSimpleName(),
          JDOUtils.getImplClass(IUploadedMsgLog.class).getSimpleName()};

  private static final long serialVersionUID = 1L;

  private static ITaskService taskService = AppFactory.get().getTaskService();

  // Reset a given count in cache
  private static void resetCacheCount(HttpServletRequest req) throws IOException {
    xLogger.fine("Entered resetCacheCount");
    String key = req.getParameter("key");
    String value = req.getParameter("value");
    if (key != null && !key.isEmpty()) {
      MemcacheService memcache = AppFactory.get().getMemcacheService();
      if (memcache != null) {
        if (value != null && !value.isEmpty()) {
          try {
            memcache.put(key, new Integer(value));
          } catch (NumberFormatException e) {
            xLogger.severe("Invalid number " + value);
          }
        } else {
          if (!memcache.delete(key)) {
            xLogger.warn("Unable to delete key {0} from memcache", key);
          }
        }
      }
    } else {
      xLogger.warn("No key or value supplied");
    }
    xLogger.fine("Exiting resetCacheCount");
  }

  // Reset a counter, both in cache and persistent database
  private static void resetCount(HttpServletRequest req) {
    xLogger.fine("Entered resetCount");
    String domainIdStr = req.getParameter("domainid");
    String key = req.getParameter("key");
    String kind = req.getParameter("kind");
    Long domainId = null;
    if (domainIdStr != null && !domainIdStr.isEmpty()) {
      domainId = Long.valueOf(domainIdStr);
    } else {
      xLogger.severe("Domain ID is missing");
      return;
    }
    if ((key == null || key.isEmpty()) && (kind == null || kind.isEmpty())) {
      xLogger.severe("Either key or kind must be present");
      return;
    }
    if (key != null && !key.isEmpty()) {
      // Get the counter
      ICounter c = Counter.getInstance(domainId, key);
      c.delete();
      return;
    }
    // Reset all counters of the given kind
    resetKindCounters(domainId, kind);
    xLogger.fine("Exiting resetCount");
  }

  // Reset the kind counter
  @SuppressWarnings("unchecked")
  private static void resetKindCounters(Long domainId, String kind) {
    xLogger.fine("Entered resetKindCounter");
    // Get all the kiosks
    EntitiesService as = null;
    try {
      as = Services.getService(EntitiesServiceImpl.class);
    } catch (Exception e) {
      xLogger.severe(
          "{0} when getting AccountsService in resetKindCounters() for domainId {1}, kind {2}: {3}",
          e.getClass().getName(), domainId, kind, e.getMessage());
      return;
    }
    List<IKiosk> kiosks = as.getAllKiosks(domainId, null, null).getResults();
    // Material counters
    if (JDOUtils.getImplClass(IMaterial.class).getSimpleName().equals(kind)) {
      // Reset the material counter
      try {
        Counter.getMaterialCounter(domainId, null).delete();
      } catch (Exception e) {
        xLogger
            .warn("{0} when removing domain-wide material counter with domainId {1}, kind {2}: {3}",
                e.getClass().getName(), domainId, kind, e.getMessage());
      }
    } else if (JDOUtils.getImplClass(IOrder.class).getSimpleName().equals(kind)) { // Order counters
      // Remove domain-wide order counter
      try {
        Counter.getOrderCounter(domainId, null, null).delete();
      } catch (Exception e) {
        xLogger.warn("{0} when removing domain-wide order for domainId {1}: {2}",
            e.getClass().getName(), domainId, e.getMessage());
      }
      // Remove kiosk-specific order counters (by otype)
      if (kiosks != null) {
        Iterator<IKiosk> it = kiosks.iterator();
        while (it.hasNext()) {
          IKiosk k = it.next();
          // Remove purchase order counter
          try {
            Counter.getOrderCounter(domainId, k.getKioskId(), IOrder.TYPE_PURCHASE, null).delete();
          } catch (Exception e) {
            xLogger.warn(
                "{0} when removing kiosk-specific purchase orders for domainId {1}, kiosk {2}: {3}",
                e.getClass().getName(), domainId, k.getKioskId(), e.getMessage());
          }
          // Remove sales order counter
          try {
            Counter.getOrderCounter(domainId, k.getKioskId(), IOrder.TYPE_SALE, null).delete();
          } catch (Exception e) {
            xLogger.warn(
                "{0} when removing kiosk-specific sales orders for domainId {1}, kiosk {2}: {3}",
                e.getClass().getName(), domainId, k.getKioskId(), e.getMessage());
          }
        }
      }
    } else if (JDOUtils.getImplClass(IUserAccount.class).getSimpleName().equals(kind)) {
      try {
        Counter.getUserCounter(domainId).delete();
      } catch (Exception e) {
        xLogger.warn("{0} when removing user counter for domainId {1}: {2}", e.getClass().getName(),
            domainId, e.getMessage());
      }
    } else if (JDOUtils.getImplClass(IKiosk.class).getSimpleName().equals(kind)) {
      try {
        Counter.getKioskCounter(domainId).delete();
      } catch (Exception e) {
        xLogger
            .warn("{0} when removing kiosk counter for domainId {1}: {2}", e.getClass().getName(),
                domainId, e.getMessage());
      }
    } else {
      try {
        Counter.getInstance(domainId, kind).delete();
      } catch (Exception e) {
        xLogger.warn("{0} when removing counter for domainId {1}, kind {2}: {3}",
            e.getClass().getName(), domainId, kind, e.getMessage());
      }
    }

    xLogger.fine("Exiting resetKindCounter");
  }

  // Add a given count to the counter
  private static void addCount(HttpServletRequest req) {
    xLogger.fine("Entered addCount");
    String domainIdStr = req.getParameter("domainid");
    String key = req.getParameter("key");
    String valueStr = req.getParameter("value");
    Long domainId = null;
    if (domainIdStr != null && !domainIdStr.isEmpty()) {
      domainId = Long.valueOf(domainIdStr);
    } else {
      xLogger.severe("Domain ID is missing");
      return;
    }
    if (key == null || key.isEmpty() || valueStr == null || valueStr.isEmpty()) {
      xLogger.severe("Key and/or value must be present");
      return;
    }
    int value = Integer.parseInt(valueStr);
    // Get the counter and increment
    Counter.getInstance(domainId, key).increment(value);
    xLogger.fine("Exiting addCount");
  }

  // Add a given count to the counter
  private static void resetDomainTransactions(HttpServletRequest req) {
    xLogger.fine("Entered resetDomainTransactions");
    String domainIdStr = req.getParameter("domainid");
    boolean execute = req.getParameter("execute") != null;
    if (!execute) { // schedule...
      // Schedule task to delete
      Map<String, String> params = new HashMap<String, String>();
      params.put("action", ACTION_RESETTRANSACTIONS);
      params.put("domainid", domainIdStr);
      params.put("execute", "true");
      // Schedule delete transactions job for this domain
      try {
        taskService
            .schedule(taskService.QUEUE_DEFAULT, "/task/admin", params, taskService.METHOD_POST);
      } catch (Exception e) {
        xLogger.severe("{0} when scheduling task: {1}", e.getClass().getName(), e.getMessage());
      }
      return;
    }
    // Get domain Id
    Long domainId = Long.valueOf(domainIdStr);
    // Reset all transactions and inventory for the domain
    resetDomainTransactions(domainId);
    // Reset the bulletin board, if any
    deleteEntitiesByDate(domainIdStr, JDOUtils.getImplClass(IBBoard.class).getName(), null, null,
        null, null);
    xLogger.fine("Exiting resetDomainTransactions");
  }

  // Delete all transactions (including orders in a given domain), and inventory for a given domain
  public static void resetDomainTransactions(Long domainId) {
    xLogger.fine("Entered resetDomainTransactions");
    if (domainId == null) {
      return;
    }
    // Get the parameters
    PageParams pageParams = new PageParams(null, PageParams.DEFAULT_SIZE);
    Map<String, Object> params = new HashMap<>();
    params.put("dIdParam", domainId);
    // Get the classes to be deleted
    List<String>
        kinds =
        Arrays.asList(JDOUtils.getImplClass(ITransaction.class).getName(),
            JDOUtils.getImplClass(IInvntryLog.class).getName(),
            JDOUtils.getImplClass(IInvntryEvntLog.class).getName(),
            JDOUtils.getImplClass(IInvntryBatch.class).getName(),
            JDOUtils.getImplClass(IOrder.class).getName(),
            JDOUtils.getImplClass(IDemandItem.class).getName(),
            JDOUtils.getImplClass(IDemandItemBatch.class).getName(),
            JDOUtils.getImplClass(IOptimizerLog.class).getName(),
            JDOUtils.getImplClass(IEvent.class).getName(),
            JDOUtils.getImplClass(IMnlTransaction.class).getName());
    // Delete raw data models
    for (String kind : kinds) {
      String query = "SELECT FROM " + kind + " WHERE sdId == dIdParam PARAMETERS Long dIdParam";
      if (kind.equals(JDOUtils.getImplClass(IOptimizerLog.class).getName())) {
        query = "SELECT FROM " + kind + " WHERE dId == dIdParam PARAMETERS Long dIdParam";
      }
      try {
        xLogger.info("Deleting {0}...", kind);
        PagedExec.exec(domainId, new QueryParams(query, params), pageParams,
            DeleteProcessor.class.getName(), null, null, 0, false, false);
      } catch (Exception e) {
        xLogger.severe("{0} when deleting {1} for domain {2}: {3}", e.getClass().getName(), kind,
            domainId, e.getMessage());
      }
    }

    // Reset inventory
    String
        query =
        "SELECT FROM " + JDOUtils.getImplClass(IInvntry.class).getName()
            + " WHERE sdId == dIdParam PARAMETERS Long dIdParam";
    try {
      xLogger.info("Resetting inventory...");
      PagedExec.exec(domainId, new QueryParams(query, params), pageParams,
          InventoryResetProcessor.class.getName(), null, null);
    } catch (Exception e) {
      xLogger.severe("{0} when resetting inventory for domain {1}: {2}", e.getClass().getName(),
          domainId, e.getMessage());
    }
    xLogger.fine("Exiting resetDomainTransactions");
  }

  // Reset bulletin board
  private static void deleteEntitiesByDate(HttpServletRequest req) {
    xLogger.fine("Entered deleteEntitiesByDate");
    String domainIdStr = req.getParameter("domainid");
    String entityClass = req.getParameter("entity");
    String startDateField = req.getParameter("startfield");
    String startDateStr = req.getParameter("start"); // format dd/MM/yyyy
    String endDateStr = req.getParameter("end"); // format dd/MM/yyyy - optional
    String orderBy = req.getParameter("orderby");
    if (orderBy == null || orderBy.isEmpty()) {
      orderBy = "desc";
    }
    deleteEntitiesByDate(domainIdStr, entityClass, startDateField, startDateStr, endDateStr,
        orderBy);
  }

  // Delete entities based on query and its params.
  private static void deleteEntitiesByQuery(HttpServletRequest req) {
    xLogger.fine("Entered deleteEntitiesByQuery");
    String domainIdStr = req.getParameter("domainid");
    String queryStr = req.getParameter("q");
    String paramsCSV = req.getParameter("params"); // name|type|value,name|type|value,...
    if (domainIdStr == null || domainIdStr.isEmpty() || queryStr == null || queryStr.isEmpty()) {
      xLogger.severe("domainId and query are mandatory. One or both of them not specified");
      return;
    }
    Long domainId = Long.valueOf(domainIdStr);
    try {
      queryStr = URLDecoder.decode(queryStr, "UTF-8");
    } catch (UnsupportedEncodingException e) {
      e.printStackTrace();
      return;
    }
    xLogger.info("queryStr: {0}, paramsCSV: {1}", queryStr, paramsCSV);
    // Get the params., if specified
    HashMap<String, Object> params = null;
    if (paramsCSV != null && !paramsCSV.isEmpty()) {
      params = new HashMap<String, Object>();
      String[] paramsArray = paramsCSV.split(",");
      for (int i = 0; i < paramsArray.length; i++) {
        String[] paramValue = paramsArray[i].split(":");
        if (paramValue.length < 3) {
          xLogger.severe("Parameter value is of invalid format (should be name|type|value): {0}",
              paramsArray[i]);
          return;
        }
        String name = paramValue[0];
        String type = paramValue[1];
        String value = paramValue[2];
        xLogger.info("params {0}: name = {1}, type = {2}, value = {3}", paramsArray[i], name, type,
            value);
        Object o = null;
        if ("String".equals(type)) {
          o = value;
        } else if ("Long".equals(type)) {
          o = Long.valueOf(value);
        } else if ("Integer".equals(type)) {
          o = Integer.valueOf(value);
        } else if ("Boolean".equals(type)) {
          o = Boolean.valueOf(value);
        } else if ("Float".equals(type)) {
          o = Float.valueOf(value);
        } else if ("Double".equals(type)) {
          o = Double.valueOf(value);
        } else {
          xLogger.severe("Unknown type {0} in param-value {1}", type, paramsArray[i]);
          return;
        }
        params.put(name, o);
      }
      // Execute deletion
      try {
        PagedExec.exec(domainId, new QueryParams(queryStr, params),
            new PageParams(null, PageParams.DEFAULT_SIZE), DeleteProcessor.class.getName(), "true",
            null);
      } catch (Exception e) {
        xLogger
            .severe("{0} when doing paged exec.: {1}", e.getClass().getName(), e.getMessage(), e);
      }
    }
    xLogger.fine("Exiting deleteEntitiesByQuery");
  }

  private static void deleteEntitiesByDate(String domainIdStr, String entityClass,
                                           String startDateField, String startDateStr,
                                           String endDateStr, String orderBy) {
    xLogger.info(
        "Deleting entities by date: domainId = {0}, entityClass = {1}, startDateField = {2}, startDateStr = {3}",
        domainIdStr, entityClass, startDateField, startDateStr);
    Long domainId = null;
    if (domainIdStr != null && !domainIdStr.isEmpty()) {
      domainId = Long.valueOf(domainIdStr);
    }
    if (domainId == null) {
      xLogger.severe("Invalid domain ID");
      return;
    }
    if (entityClass == null || entityClass.isEmpty()) {
      xLogger.severe("Invalid entity class");
      return;
    }
    String queryStr = "SELECT FROM " + entityClass;
    try {
      Class clazz = Class.forName(entityClass);
      if (IMultiDomain.class.isAssignableFrom(clazz)) {
        queryStr += " WHERE dId.contains(dIdParam)";
      } else {
        queryStr += " WHERE dId == dIdParam";
      }
    } catch (ClassNotFoundException e) {
      xLogger.severe("Invalid entity class: {0}", entityClass, e);
      return;
    }

    String paramsStr = " PARAMETERS Long dIdParam";
    Map<String, Object> params = new HashMap<String, Object>();
    params.put("dIdParam", domainId);
    if (startDateStr != null && !startDateStr.isEmpty()) {
      if (startDateField == null || startDateField.isEmpty()) {
        xLogger.severe("No startfield specified");
        return;
      }
      SimpleDateFormat sdf = new SimpleDateFormat(Constants.DATE_FORMAT);
      try {
        Date start = sdf.parse(startDateStr);
        Calendar cal = GregorianCalendar.getInstance();
        cal.setTime(start);
        LocalDateUtil.resetTimeFields(cal);
        queryStr += " && " + startDateField + " > startParam";
        // paramsStr += ", Date startParam import java.util.Date";
        paramsStr += ", Date startParam";
        params.put("startParam",
            LocalDateUtil.getOffsetDate(cal.getTime(), -1, Calendar.MILLISECOND));
        // If endDateStr is present, add it to the queryStr and paramsStr and the params map.
        if (endDateStr != null && !endDateStr.isEmpty()) {
          Date end = sdf.parse(endDateStr);
          cal.setTime(end);
          LocalDateUtil.resetTimeFields(cal);
          queryStr += " && " + startDateField + " < endParam";
          paramsStr += ", Date endParam";
          params.put("endParam", cal.getTime());
        }
        paramsStr += "  import java.util.Date;";
        xLogger.info("paramsStr: {0}", paramsStr);
      } catch (ParseException e) {
        e.printStackTrace();
        return;
      }
    }
    queryStr += paramsStr;
    if (startDateField != null && !startDateField.isEmpty() && orderBy != null && !orderBy
        .isEmpty()) {
      queryStr += " ORDER BY " + startDateField + " " + orderBy;
    }
    xLogger.info("queryStr: {0}", queryStr);
    QueryParams qp = new QueryParams(queryStr, params);
    try {
      PagedExec.exec(domainId, qp, new PageParams(null, PageParams.DEFAULT_SIZE),
          DeleteProcessor.class.getName(), null, null);
    } catch (Exception e) {
      xLogger.severe("{0} when doing paged-exec to delete BBoard entries in domain {1}: {2}",
          e.getClass().getName(), domainId, e.getMessage());
      e.printStackTrace();
    }
    xLogger.fine("Exiting deleteEntitiesByDate");
  }

  // Delete expired sessions from data store
  public static void deleteSessions(HttpServletRequest req, HttpServletResponse resp) {
    xLogger.fine("Entered deleteSessions");
    SessionMgr.deleteSessions();
    xLogger.fine("Exiting deleteSessions");
  }

  // Backup data store
  private static void backupData(HttpServletRequest req) {
    xLogger.fine("Entered backupData");
    String name = req.getParameter("name");
    String bucketName = req.getParameter("bucket");
    boolean verifyOnly = req.getParameter("verifyonly") != null;
    boolean force = req.getParameter("force") != null;
    String appName = SecurityMgr.getApplicationName();
    xLogger.info("App: {0}", appName);
    // Do not backup, if its the dev. server
    if (!force && (appName == null || !appName.equals(PROD_APP_NAME))) {
      xLogger.warn("Not prod. server. Not backing up anything. Goodbye!");
      return;
    }
    if (name == null || name.isEmpty()) {
      name = "BackupToCloud";
    }
    if (bucketName == null || bucketName.isEmpty()) {
      bucketName = appName + ".appspot.com";
    }
    // Form the URL by getting all the classes
    //String backupUrl = "http://" + req.getServerName() + ( SecurityManager.isDevServer() ? ":" + req.getServerPort() : "" ) + URL_BACKUPDATA;
    String backupUrl = URL_BACKUPDATA;
    backupUrl += "?name=" + name + "&filesystem=gs&gs_bucket_name=" + bucketName;
    List<String> doNotBackup = Arrays.asList(DO_NOT_BACKUP_LIST);
    //TODO Charan Backup.. Not required in SQL I suppose..
    backupUrl = AppFactory.get().getBackendService().getBackupURL(backupUrl, doNotBackup);

    xLogger.info("BACKUP URL: {0}", backupUrl);
    if (verifyOnly) {
      return; // DO not perform the actual backup
    }
    try {
      taskService.schedule(taskService.QUEUE_DEFAULT, backupUrl, null, taskService.METHOD_GET);
                        /*
                        URL url = new URL( backupUrl );
            BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream()));
            String line;
            while ((line = reader.readLine()) != null)
                xLogger.info( line );
            reader.close();
            */
    } catch (Exception e) {
      xLogger.severe("{0} when invoking backup URL {1}: {2}", e.getClass().getName(), backupUrl,
          e.getMessage(), e);
    }
    xLogger.fine("Exiting backupData");
                /*
                 * old cron url:     <url>/_ah/datastore_admin/backup.create?name=BackupToCloud&amp;kind=ALog&amp;kind=Account&amp;kind=BBoard&amp;kind=Conig&amp;kind=DemandItem&amp;kind=DemandItemBatch&amp;kind=Domain&amp;kind=Event&amp;kind=Invntry&amp;kind=InvntryBatch&amp;kind=InvntryEvntLog&amp;kind=InvntryLog&amp;kind=Kiosk&amp;kind=KioskLink&amp;kind=KioskToPoolGroup&amp;kind=Material&amp;kind=Media&amp;kind=MessageLog&amp;kind=OptimizerLog&amp;kind=Order&amp;kind=OrderQuantityLog&amp;kind=PoolGroup&amp;kind=ShardedCounter&amp;kind=Tag&amp;kind=Transaction&amp;kind=Transporter&amp;kind=UserAccount&amp;kind=UserToKiosk&amp;filesystem=gs&amp;gs_bucket_name=logistimo-web.appspot.com</url>
		 */
  }

  public void doGet(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {
    xLogger.fine("Entered doGet");
    String action = req.getParameter("action");
    if (ACTION_RESETCACHECOUNT.equals(action)) {
      resetCacheCount(req);
    } else if (ACTION_RESETCOUNT.equals(action)) {
      resetCount(req);
    } else if (ACTION_ADDCOUNT.equals(action)) {
      addCount(req);
    } else if (ACTION_RESETTRANSACTIONS.equals(action)) {
      resetDomainTransactions(req);
    } else if (ACTION_DELETEENTITIES.equals(action)) {
      deleteEntitiesByDate(req);
    } else if (ACTION_DELETESESSIONS.equals(action)) {
      deleteSessions(req, resp);
    } else if (ACTION_UPDATESYSCONFIG.equals(action)) {
      updateSysConfig(req, resp);
    } else if (ACTION_DELETEENTITIESBYQUERY.equals(action)) {
      deleteEntitiesByQuery(req);
    } else if (ACTION_BACKUPDATA.equals(action)) {
      backupData(req);
    } else {
      xLogger.severe("Invalid action: {0}", action);
    }
    xLogger.fine("Existing doGet");
  }

  public void doPost(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {
    doGet(req, resp);
  }

  // Update the system configuration, given a key, or with our standard set of config keys
  private void updateSysConfig(HttpServletRequest req, HttpServletResponse resp) {
    xLogger.fine("Entered updateSysConfig");
    List<String> keys = new ArrayList<String>();
    String key = req.getParameter("key");
    boolean hasKey = key != null && !key.isEmpty();
    String host = req.getParameter("host");
    String userId = req.getParameter("userid");
    String password = req.getParameter("password");
    String email = req.getParameter("email");
    if (userId == null || userId.isEmpty() || password == null || password.isEmpty()) {
      xLogger.severe("Invalid user name or password");
      PrintWriter pw;
      try {
        pw = resp.getWriter();
        pw.write("Invalid name or password");
        pw.close();
      } catch (IOException e) {
        e.printStackTrace();
      }
      return;
    }
    if (email == null || email.isEmpty()) {
      xLogger.severe("Invalid email. Please provide an email.");
      PrintWriter pw;
      try {
        pw = resp.getWriter();
        pw.write("Invalid email. Please provide an email");
        pw.close();
      } catch (IOException e) {
        e.printStackTrace();
      }
      return;
    }
    // Execute and update config.
    if (host == null || host.isEmpty()) {
      host = URL_PROD;
    }
    if (hasKey) {
      keys.add(key);
    } else { // Add the basic keys required
      keys.add(IConfig.COUNTRIES);
      keys.add(IConfig.CURRENCIES);
      keys.add(IConfig.LANGUAGES);
      keys.add(IConfig.LANGUAGES_MOBILE);
      keys.add(IConfig.LOCATIONS);
      keys.add(IConfig.OPTIMIZATION);
      keys.add(IConfig.REPORTS);
      keys.add(IConfig.SMSCONFIG);
      keys.add(IConfig.GENERALCONFIG);
    }
    // Get the configuration
    PersistenceManager pm = PMF.get().getPersistenceManager();
    try {
      Date now = new Date();
      String
          url =
          host + "/api/cfg?a=" + ACTION_GETSYSCONFIG + "&userid=" + userId + "&password=" + password
              + "&key=";
      ConfigurationMgmtService cms = Services.getService(ConfigurationMgmtServiceImpl.class);
      PrintWriter pw = resp.getWriter();
      for (String k : keys) {
        try {
          // Fetch config. string from the server
          String configStr = HttpUtil.get(url + k, null);
          xLogger.info("Got config. string for key {0}: {1}", k, configStr);
          pw.append("Got config string for key " + k + ": " + configStr + "...\n\n\n");
          // Update config. locally
          if (configStr != null && !configStr.isEmpty()) {
            IConfig c = JDOUtils.createInstance(IConfig.class);
            c.setConfig(configStr);
            c.setKey(k);
            c.setLastUpdated(now);
            cms.addConfiguration(k, c);
          } else {
            xLogger.warn("Empty config. string returned for key {0}", k);
            pw.append("Empty config string returned for key: " + k);
          }
        } catch (Exception e) {
          xLogger.warn("{0} when getting config. for key {1}: {2}", e.getClass().getName(), key,
              e.getMessage(), e);
        }
      }
      // Check if this user exists; if not, create this user
      UsersService as = null;
      DomainsService ds = null;
      try {
        as = Services.getService(UsersServiceImpl.class);
        ds = Services.getService(DomainsServiceImpl.class);
        JDOUtils.getObjectById(IUserAccount.class, userId);
      } catch (JDOObjectNotFoundException e) {
        // Check if the default domain exists
        Long dId = -1l;
        try {
          pm.getObjectById(JDOUtils.getImplClass(IDomain.class), new Long(-1));
        } catch (JDOObjectNotFoundException e1) {
          IDomain d = JDOUtils.createInstance(IDomain.class);
          d.setCreatedOn(new Date());
          d.setId(new Long(-1));
          d.setIsActive(true);
          d.setName("Default");
          d.setOwnerId(userId);
          d.setReportEnabled(true);
          dId = ds.addDomain(d);
          ds.createDefaultDomainPermissions(dId);
          pw.append("Create a Default domain with ID " + dId + "\n\n\n");
        }
        // Create user
        IUserAccount u = JDOUtils.createInstance(IUserAccount.class);
        u.setUserId(userId);
        u.setEncodedPassword(password);
        u.setRole(SecurityConstants.ROLE_SUPERUSER);
        u.setFirstName(userId);
        u.setMobilePhoneNumber("+91 999999999");
        u.setCountry("IN");
        u.setState("Karnataka");
        u.setLanguage("en");
        u.setTimezone("Asia/Kolkata");
        u.setEmail(email);
        as.addAccount(dId, u);
        pw.append("Created user account " + userId
            + ".\n\nYOU MAY NOW LOGIN LOCALLY USING THIS ACCOUNT.");
      } finally {
        pw.close();
      }
    } catch (Exception e) {
      xLogger.severe("{0} when getting config. for key {1}: {2}", e.getClass().getName(), key,
          e.getMessage(), e);
    } finally {
      pm.close();
    }
    xLogger.fine("Exiting updateSysConfig");
  }
}

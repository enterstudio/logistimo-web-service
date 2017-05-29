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
package com.logistimo.utils;

import com.logistimo.dao.JDOUtils;

import com.logistimo.communications.MessageHandlingException;
import com.logistimo.entity.IMessageLog;
import com.logistimo.entity.IMultipartMsg;
import com.logistimo.pagination.PageParams;
import com.logistimo.pagination.Results;
import com.logistimo.services.ObjectNotFoundException;
import com.logistimo.services.impl.PMF;

import com.logistimo.logger.XLog;

import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.jdo.JDOObjectNotFoundException;
import javax.jdo.PersistenceManager;
import javax.jdo.Query;

/**
 * @author Arun
 */
public class MessageUtil {

  // Logger
  private static final XLog xLogger = XLog.getLog(MessageUtil.class);

  // Log a message
  public static void log(IMessageLog mlog) throws MessageHandlingException {
    xLogger.fine("Entered log");
    if (mlog == null) {
      throw new MessageHandlingException("Invalid message log");
    }
    if (mlog.getKey() == null) {
      throw new MessageHandlingException("Invalid key");
    }
    PersistenceManager pm = PMF.get().getPersistenceManager();
    String msg = null;
    try {
      pm.makePersistent(mlog);
    } catch (Exception e) {
      msg = e.getMessage();
    } finally {
      pm.close();
    }
    if (msg != null) {
      throw new MessageHandlingException(msg);
    }
    xLogger.fine("Exiting log");
  }

  // Get a pre-existing message log
  public static IMessageLog getLog(String jobId, String address) throws MessageHandlingException {
    if (jobId == null && address == null) {
      throw new MessageHandlingException("Invalid input parameters when getting message log");
    }
    IMessageLog mlog = null;
    PersistenceManager pm = PMF.get().getPersistenceManager();
    String msg = null;
    try {
      String key = JDOUtils.createMessageLogKey(jobId, address);
      mlog = JDOUtils.getObjectById(IMessageLog.class, key, pm);
      mlog = pm.detachCopy(mlog);
    } catch (JDOObjectNotFoundException e) {
      msg =
          "Message log with key '" + JDOUtils.createMessageLogKey(jobId, address) + "' not found ["
              + e.getMessage() + "]";
    } finally {
      pm.close();
    }
    if (msg != null) {
      throw new MessageHandlingException(msg);
    }
    return mlog;
  }

  // Get logs for a given sender
  @SuppressWarnings("unchecked")
  public static Results getLogs(String senderId, PageParams pageParams)
      throws MessageHandlingException {
    if (senderId == null) {
      throw new MessageHandlingException("No userId specified");
    }
    List<IMessageLog> mlogs = null;
    String cursor = null;
    PersistenceManager pm = PMF.get().getPersistenceManager();
    Query q = pm.newQuery(JDOUtils.getImplClass(IMessageLog.class));
    q.setFilter("suId == suIdParam");
    q.declareParameters("String suIdParam");
    q.setOrdering("t desc");
    if (pageParams != null) {
      QueryUtil.setPageParams(q, pageParams);
    }
    try {
      mlogs = (List<IMessageLog>) q.execute(senderId);
      if (mlogs != null) {
        mlogs.size(); // TODO: to ensure all objects are retrieved before closing object manager
        cursor = QueryUtil.getCursor(mlogs);
        mlogs = (List<IMessageLog>) pm.detachCopyAll(mlogs);
      }
    } finally {
      try {
        q.closeAll();
      } catch (Exception ignored) {

      }
      pm.close();
    }
    return new Results(mlogs, cursor);
  }

  @SuppressWarnings("unchecked")
  public static Results getLogs(Long domainId, PageParams pageParams)
      throws MessageHandlingException {
    xLogger.fine("Entered getLogs");
    if (domainId == null) {
      throw new MessageHandlingException("No domainId specified");
    }
    List<IMessageLog> mlogs = null;
    String cursor = null;
    Map<String, Object> params = new HashMap<>();
    PersistenceManager pm = PMF.get().getPersistenceManager();
    Query q = pm.newQuery(JDOUtils.getImplClass(IMessageLog.class));
    q.setFilter("dId == dIdParam && notif == notifParam");
    q.declareParameters("Long dIdParam , Integer notifParam");
    params.put("dIdParam", domainId);
    params.put("notifParam", 0);
    q.setOrdering("t desc");
    if (pageParams != null) {
      QueryUtil.setPageParams(q, pageParams);
    }
    try {
      mlogs = (List<IMessageLog>) q.executeWithMap(params);
      if (mlogs != null) {
        mlogs.size(); // TODO: to ensure all objects are retrieved before closing object manager
        cursor = QueryUtil.getCursor(mlogs);
        mlogs = (List<IMessageLog>) pm.detachCopyAll(mlogs);
        mlogs = sortByTime(mlogs);
      }
    } finally {
      try {
        q.closeAll();
      } catch (Exception ignored) {

      }
      pm.close();
    }
    return new Results(mlogs, cursor);
  }

  //@SuppressWarnings("unchecked") // get all notifications for all domains
  public static Results getNotifactionLogs(Long domainId, Date startDate, Date endDate,
                                           PageParams pageParams) throws MessageHandlingException {
    xLogger.fine("Entered getLogs");
    if (domainId == null) {
      throw new MessageHandlingException("No domainId specified");
    }
    Map<String, Object> params = new HashMap<>();
    List<IMessageLog> mlogs = null;
    String cursor = null;
    PersistenceManager pm = PMF.get().getPersistenceManager();
    Query q = pm.newQuery(JDOUtils.getImplClass(IMessageLog.class));
    boolean imports = false;
    params.put("dIdParam", domainId);
    params.put("notifParam", 1);
    String filter = "dId == dIdParam && notif == notifParam ";
    String declaration = "Long dIdParam , Integer notifParam ";
    if (startDate != null) {
      //offset date with less 1 milli second
      params
          .put("startDateParam", LocalDateUtil.getOffsetDate(startDate, -1, Calendar.MILLISECOND));
      filter += "&& t >= startDateParam ";
      declaration += ",Date startDateParam ";
      imports = true;
    }
    if (endDate != null) {
      //offset date with one day increase to get inclusive end date
      params.put("endDateParam", LocalDateUtil.getOffsetDate(endDate, +1, Calendar.DATE));
      filter += " && t < endDateParam ";
      declaration += ",Date endDateParam ";
      imports = true;
    }
    q.setFilter(filter);
    q.declareParameters(declaration);
    if (imports) {
      q.declareImports("import java.util.Date");
    }
    q.setOrdering("t desc");
    if (pageParams != null) {
      QueryUtil.setPageParams(q, pageParams);
    }
    try {
      mlogs = (List<IMessageLog>) q.executeWithMap(params);
      if (mlogs != null) {
        mlogs.size(); // TODO: to ensure all objects are retrieved before closing object manager
        cursor = QueryUtil.getCursor(mlogs);
        mlogs = (List<IMessageLog>) pm.detachCopyAll(mlogs);
        mlogs = sortByTime(mlogs);
      }
    } finally {
      try {
        q.closeAll();
      } catch (Exception ignored) {

      }
      pm.close();
    }
    return new Results(mlogs, cursor);
  }

  // Store a multi-part message

  public static void storeMultipartMsg(IMultipartMsg mmsg) throws MessageHandlingException {
    if (mmsg == null || mmsg.getMessages() == null || mmsg.getMessages().isEmpty()) {
      throw new MessageHandlingException("No multi-part message specified");
    }
    if (mmsg.getId() == null) {
      throw new MessageHandlingException("No key specified for multi-part message");
    }
    if (mmsg.getTimestamp() == null) {
      mmsg.setTimestamp(new Date());
    }
    PersistenceManager pm = PMF.get().getPersistenceManager();
    try {
      pm.makePersistent(mmsg);
    } finally {
      pm.close();
    }
  }

  // Get a multi-part message
  public static IMultipartMsg getMultipartMsg(String id)
      throws ObjectNotFoundException, MessageHandlingException {
    if (id == null || id.isEmpty()) {
      throw new MessageHandlingException("Invalid ID");
    }
    PersistenceManager pm = PMF.get().getPersistenceManager();
    IMultipartMsg mmsg = null;
    try {
      mmsg = JDOUtils.getObjectById(IMultipartMsg.class, id, pm);
      mmsg = pm.detachCopy(mmsg);
    } catch (JDOObjectNotFoundException e) {
      throw new ObjectNotFoundException(e.getMessage());
    } catch (Exception e) {
      throw new MessageHandlingException(e.getMessage());
    } finally {
      pm.close();
    }

    return mmsg;
  }

  // Remove a multi-part message
  public static void removeMultipartMsg(String id) throws MessageHandlingException {
    if (id == null || id.isEmpty()) {
      throw new MessageHandlingException("Invalid ID");
    }
    PersistenceManager pm = PMF.get().getPersistenceManager();
    String err = null;
    try {
      IMultipartMsg mmsg = JDOUtils.getObjectById(IMultipartMsg.class, id, pm);
      removeMultipartMsg(mmsg);
    } catch (JDOObjectNotFoundException e) {
      err = e.getMessage();
    } finally {
      pm.close();
    }
    if (err != null) {
      throw new MessageHandlingException(err);
    }

  }

  public static void removeMultipartMsg(IMultipartMsg mmsg) throws MessageHandlingException {
    if (mmsg == null) {
      throw new MessageHandlingException("Invalid message object");
    }
    PersistenceManager pm = PMF.get().getPersistenceManager();
    try {
      pm.deletePersistent(mmsg);
    } finally {
      pm.close();
    }
  }

  // Sort logs by time
  public static List<IMessageLog> sortByTime(List<IMessageLog> mlogs) {
    if (mlogs == null || mlogs.isEmpty() || mlogs.size() == 1) {
      return mlogs;
    }
    Collections.sort(mlogs, new Comparator<IMessageLog>() {
      @Override
      public int compare(IMessageLog o1, IMessageLog o2) {
        return o2.getTimestamp().compareTo(o1.getTimestamp());
      }
    });
    return mlogs;
  }

  // Get a CSV version of a list
  public static String getCSV(List<String> list) {
    String csv = "";
    if (list == null) {
      return null;
    }
    Iterator<String> it = list.iterator();
    while (it.hasNext()) {
      if (csv.length() > 0) {
        csv += ",";
      }
      csv += it.next();
    }
    return csv;
  }

  // Get a CSV version of a list
  public static String getCSVWithEnclose(List<String> list) {
    if (list == null) {
      return null;
    }
    StringBuilder csv = new StringBuilder();
    for (String s : list) {
      csv.append("'").append(s).append("'").append(",");
    }
    csv.setLength(csv.length() - 1);
    return csv.toString();
  }

}

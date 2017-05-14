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
package com.logistimo.mappers;


import com.logistimo.AppFactory;
import com.logistimo.config.models.OrdersConfig;
import com.logistimo.exports.BulkExportMgr;
import com.logistimo.services.mapper.Entity;
import com.logistimo.services.mapper.GenericMapper;
import com.logistimo.services.mapper.Key;
import com.logistimo.services.taskqueue.TaskService;

import com.logistimo.config.models.DomainConfig;
import com.logistimo.config.models.InventoryConfig;
import com.logistimo.entity.IJobStatus;
import com.logistimo.services.impl.PMF;
import com.logistimo.users.UserUtils;

import com.logistimo.constants.CharacterConstants;
import com.logistimo.utils.JobUtil;
import com.logistimo.utils.LocalDateUtil;
import com.logistimo.utils.StringUtil;
import com.logistimo.logger.XLog;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.jdo.PersistenceManager;

/**
 * Schedules data export transactions for a given domain (esp. transaction data export)
 *
 * @author Arun
 */
public class DataExportScheduler extends GenericMapper<Key, Entity, NullWritable, NullWritable> {

  // Logger
  private static final XLog xLogger = XLog.getLog(DataExportScheduler.class);

  PersistenceManager pm = null;

  // Schedule a transaction data export task (hour times are in hh:mm format)
  private static void scheduleExportTask(String type, Long domainId, String userId,
                                         String userIdsCSV, Calendar fromTime,
                                         Calendar scheduleTime) {
    xLogger.fine("Entered scheduleExportTask");
    String url = "/task/export";
    Map<String, String> params = new HashMap<String, String>();
    params.put("action", "be");
    params.put("type", type);
    params.put("sourceuserid", userId);
    params.put("domainid", domainId.toString());
    params.put("userids", userIdsCSV);
    // Get the scheduling ETA
    long etaMillis = -1;
    if (scheduleTime != null) {
      etaMillis = scheduleTime.getTimeInMillis();
      if (etaMillis < (new Date())
          .getTime()) // if schedule time is lesser than current time, run task now
      {
        etaMillis = -1;
      }
    }
    if (fromTime != null) {
      String
          from =
          String.valueOf(fromTime.get(Calendar.DATE)) + CharacterConstants.F_SLASH + String
              .valueOf(fromTime.get(Calendar.MONTH) + 1) + CharacterConstants.F_SLASH + String
              .valueOf(fromTime.get(Calendar.YEAR)) + CharacterConstants.SPACE
              + getDoubleDigitString(String.valueOf(fromTime.get(Calendar.HOUR_OF_DAY)))
              + CharacterConstants.COLON + getDoubleDigitString(
              String.valueOf(fromTime.get(Calendar.MINUTE)))
              + ":00"; // NOTE: add the seconds, given the datetime format requires it
      params.put("from", from);
    } else {
      xLogger.severe("'From' time is invalid for inventory transaction export in domain {1}",
          domainId);
      return;
    }
    xLogger.info("Scheduling {0} export task in domain {1} with a from date of {2} and eta of {3}",
        type, domainId, params.get("from"), scheduleTime.getTime());
    // Get headers to target backend
    Map<String, String> headers = BulkExportMgr.getExportBackendHeader();
    // Schedule task with eta
    Long jobId = null;
    if (etaMillis <= 0) {
      jobId =
          JobUtil
              .createJob(domainId, null, null, IJobStatus.TYPE_EXPORT, params.get("type"), params);
      params.put("jobid", jobId.toString());
    }
    try {
      AppFactory.get().getTaskService()
          .schedule(TaskService.QUEUE_EXPORTER, url, params, null, headers, TaskService.METHOD_POST,
              etaMillis, domainId, userId, "EXPORT_TRANS");
    } catch (Exception e) {
      xLogger.warn(
          "{0} when scheduling transaction data export task with job ID {1} for domain {2}: {3}. Job ID: {4}",
          e.getClass().getName(), jobId, domainId, e.getMessage(), e);
    }
    xLogger.fine("Exiting scheduleExportTask");
  }

  // Get a date given a hour of day in hh:mm format
  private static Calendar getCalendar(String hourOffset, Date curTime, boolean previousDay) {
    Calendar cal = GregorianCalendar.getInstance();
    if (curTime == null) {
      cal.setTime(new Date());
      LocalDateUtil.resetTimeFields(cal);
    } else {
      cal.setTime(curTime);
    }
    if (previousDay) {
      cal.add(Calendar.DATE, -1);
    }
    if (hourOffset == null) {
      return cal;
    }
    // Get hour/min.
    String[] timeOfDay = hourOffset.split(":");
    try {
      cal.set(Calendar.SECOND, 0);
      cal.set(Calendar.MILLISECOND, 0);
      if (timeOfDay.length >= 1) {
        cal.set(Calendar.HOUR_OF_DAY, Integer.parseInt(timeOfDay[0]));
      }
      if (timeOfDay.length == 2) {
        cal.set(Calendar.MINUTE, Integer.parseInt(timeOfDay[1]));
      }
      return cal;
    } catch (NumberFormatException e) {
      xLogger.warn("Invalid number for hour or minute in {0}", hourOffset);
      return null;
    }
  }

  private static String getDoubleDigitString(String digit) {
    if (digit.length() == 1) {
      return "0" + digit;
    }
    return digit;
  }

  @Override
  public void taskSetup(Context context) throws IOException, InterruptedException {
    super.taskSetup(context);
    pm = PMF.get().getPersistenceManager();
  }

  @Override
  public void taskCleanup(Context context) throws IOException, InterruptedException {
    super.taskCleanup(context);
    pm.close();
  }

  @Override
  public void map(Key key, Entity entity, Context context) {
    xLogger.fine("Entered DataExportScheduler.map");
    // Get domain Id
    Long domainId = Long.valueOf(key.getId());
    String type = null;
    try {
      // Get the domain config.
      DomainConfig dc = DomainConfig.getInstance(domainId);
      // Inventory export
      type = BulkExportMgr.TYPE_TRANSACTIONS;
      InventoryConfig ic = dc.getInventoryConfig();
      if (ic != null && ic.isEnabled()) // schedule tasks for export of inventory
      {
        processScheduledExport(type, domainId, ic.getTimes(), ic.getExportUsers(), ic.getUserTags(),
            ic.getSourceUserId());
      }
      // Order export
      type = BulkExportMgr.TYPE_ORDERS;
      OrdersConfig oc = dc.getOrdersConfig();
      if (oc != null && oc.isExportEnabled()) {
        processScheduledExport("orders", domainId, StringUtil.getList(oc.getExportTimes()),
            StringUtil.getList(oc.getExportUserIds()), oc.getUserTags(), oc.getSourceUserId());
      }
    } catch (Exception e) {
      xLogger.warn("{0} when trying to schedule {1} data export task in domain {2}: {3}",
          e.getClass().getName(), type, domainId, e.getMessage());
    }
    xLogger.fine("Exiting DataExportScheduler.map");
  }

  private void processScheduledExport(String type, Long domainId, List<String> times,
                                      List<String> exportUserIds, List<String> usrTags,
                                      String userId) {
    xLogger.fine("Entered processScheduleExport");
    try {
      // Get the schedule
      if ((exportUserIds == null || exportUserIds.isEmpty()) && (usrTags == null
          || usrTags.size() == 0)) {
        xLogger.warn("Export users not specified for {0} export in domain {1}. Aborting...", type,
            domainId);
        return;
      }
      // Get the export userIds CSV
      String userIdsCSV = StringUtil.getCSV(exportUserIds);
      // Get the final list of enabled user ids from userIds list and user tags list
      List<String>
          enabledUserIds =
          UserUtils.getEnabledUniqueUserIds(domainId, userIdsCSV, StringUtil.getCSV(usrTags));
      // Get the final userIds CSV
      String enabledUserIdsCSV = StringUtil.getCSV(enabledUserIds);
      // Source user Id
      if (userId == null || userId.isEmpty()) {
        xLogger.warn("User ID not specified for transaction data export in domain {0}. Aborting...",
            domainId);
        return;
      }
      // Export times
      if (times == null || times.isEmpty()) {
        times = new ArrayList<String>();
        times.add("00:00");
      }
      String fromHourOffset = null;
      int numTimes = times.size();
      if (numTimes > 1) {
        fromHourOffset = times.get(numTimes - 1);
      }
      for (int i = 0; i < numTimes; i++) {
        String scheduleHour = times.get(i);
        Calendar scheduleTime = getCalendar(scheduleHour, null, false);
        Calendar
            fromTime =
            getCalendar(fromHourOffset, scheduleTime.getTime(), i
                == 0); // for the first time, get a time from previous day; for the rest of the 'times', get it on current day
        // Schedule transaction export at specified times
        scheduleExportTask(type, domainId, userId, enabledUserIdsCSV, fromTime, scheduleTime);
        fromHourOffset = scheduleHour;
      }
    } catch (Exception e) {
      xLogger.warn("{0} when trying to schedule transaction data export task for domain {1}: {2}",
          e.getClass().getName(), domainId, e.getMessage());
    }
    xLogger.fine("Exiting DataExportScheduler.map");
  }
}

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

package com.logistimo.logger;

import com.ibm.icu.util.Calendar;
import com.logistimo.dao.JDOUtils;
import com.logistimo.entity.IALog;
import com.logistimo.pagination.PageParams;
import com.logistimo.pagination.Results;
import com.logistimo.services.impl.PMF;
import com.logistimo.utils.LocalDateUtil;
import com.logistimo.utils.ParamChecker;
import com.logistimo.utils.QueryUtil;

import java.text.MessageFormat;
import java.util.Date;
import java.util.List;

import javax.jdo.PersistenceManager;
import javax.jdo.Query;

public class XLog {

  private ILogger xLogger;

  private XLog(ILogger logger) {
    xLogger = logger;
  }

  public static XLog getLog(String name) {
    return new XLog(new LogiLogger(name));
  }

  @SuppressWarnings("rawtypes")
  public static XLog getLog(Class klazz) {
    return getLog(klazz.getName());
  }

  public static String format(String msgTemplate, Object... params) {
    ParamChecker.notEmpty(msgTemplate, "msgTemplate");
    msgTemplate = msgTemplate.replace("{E}", System.getProperty("line.separator"));
    if (params != null && params.length > 0) {
      msgTemplate = MessageFormat.format(msgTemplate, params);
    }
    return msgTemplate;
  }

  public static Throwable getCause(Object... params) {
    Throwable throwable = null;
    if (params != null && params.length > 0 && params[params.length - 1] instanceof Throwable) {
      throwable = (Throwable) params[params.length - 1];
    }
    return throwable;
  }

  // Create a bulletin board log
  public static void logRequest(IALog log) {
    PersistenceManager pm = PMF.get().getPersistenceManager();
    try {
      pm.makePersistent(log);
    } catch (Exception e) {
      e.printStackTrace();
    } finally {
      pm.close();
    }
  }

  // Get BBoard access logs
  @SuppressWarnings("unchecked")
  public static Results getRequestLogs(Long domainId, String type, Date startDate,
                                       PageParams pageParams) {
    String
        queryStr =
        "SELECT FROM " + JDOUtils.getImplClass(IALog.class).getName()
            + " WHERE dId == dIdParam && ty == tyParam";
    String paramsStr = " PARAMETERS Long dIdParam, String tyParam";
    if (startDate != null) {
      queryStr += " && t > startParam";
      paramsStr += ", Date startParam import java.util.Date;";
    }
    queryStr += paramsStr + " ORDER BY t desc";
    PersistenceManager pm = PMF.get().getPersistenceManager();
    Query q = pm.newQuery(queryStr);
    if (pageParams != null) {
      QueryUtil.setPageParams(q, pageParams);
    }
    List<IALog> results = null;
    String cursor = null;
    try {
      if (startDate != null) {
        results =
            (List<IALog>) q.execute(domainId, type,
                LocalDateUtil.getOffsetDate(startDate, -1, Calendar.MILLISECOND));
      } else {
        results = (List<IALog>) q.execute(domainId, type);
      }
      if (results != null) {
        results.size();
        cursor = QueryUtil.getCursor(results);
        results = (List<IALog>) pm.detachCopyAll(results);
      }
    } finally {
      try {
        q.closeAll();
      } catch (Exception ignored) {

      }
      pm.close();
    }
    return new Results(results, cursor);
  }

  public void fine(String msgTemplate, Object... params) {
    xLogger.fine(msgTemplate, params);
  }

  public void finer(String msgTemplate, Object... params) {
    xLogger.finer(msgTemplate, params);
  }

  public void finest(String msgTemplate, Object... params) {
    xLogger.finest(msgTemplate, params);
  }

  public void info(String msgTemplate, Object... params) {
    xLogger.info(msgTemplate, params);
  }

  public void warn(String msgTemplate, Object... params) {
    xLogger.warn(msgTemplate, params);
  }

  public void severe(String msgTemplate, Object... params) {
    xLogger.severe(msgTemplate, params);
  }
}

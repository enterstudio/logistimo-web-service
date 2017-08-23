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
package com.logistimo.pagination;

import com.logistimo.AppFactory;
import com.logistimo.dao.JDOUtils;
import com.logistimo.services.taskqueue.ITaskService;

import com.logistimo.pagination.processor.ProcessingException;
import com.logistimo.pagination.processor.Processor;
import com.logistimo.services.impl.PMF;
import com.logistimo.constants.CharacterConstants;
import com.logistimo.utils.QueryUtil;
import com.logistimo.exception.TaskSchedulingException;
import com.logistimo.logger.XLog;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.URLEncoder;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import javax.jdo.PersistenceManager;
import javax.jdo.Query;

/**
 * Paginated execution of a some task, wherein the executor runs a given query, and passes the results to a processor
 *
 * @author Arun
 */
public class PagedExec {

  // Logger
  private static final XLog xLogger = XLog.getLog(PagedExec.class);
  // Task URL
  private static final String TASK_URL = "/task/pagedexec";

  // Execute a procedure on entities of class 'clazz' with filtering on key-values params.
  // NOTE: The key names in 'params' should be same as method names in the entity class 'clazz'; 'prevOutput' is the previous output of processor.process(), if any
  // NOTE: Only Date, Long and String are allowed as query parameters (at this time)
  // secondsBetweenTasks: the number of seconds to be given before two pages can be executed - this can be useful when you want the next page to run only after giving time for previous persistent results to become eventually consistent (in case of HRD)
  public static void exec(Long domainId, QueryParams qp, PageParams pageParams,
                          String processorClassName, String prevOutput, Finalizer finalizer)
      throws TaskSchedulingException, ProcessingException {
    exec(domainId, qp, pageParams, processorClassName, prevOutput, finalizer, 0);
  }

  public static void exec(Long domainId, QueryParams qp, PageParams pageParams,
                          String processorClassName, String prevOutput, Finalizer finalizer,
                          int secondsBetweenTasks)
      throws TaskSchedulingException, ProcessingException {
    // Load the processor class
    Processor proc = loadProcessor(processorClassName);
    if (proc == null) {
      throw new ProcessingException("Could not load processor with name " + processorClassName);
    }
    exec(domainId, qp, pageParams, proc, prevOutput, finalizer, secondsBetweenTasks, false, true);
  }

  public static void exec(Long domainId, QueryParams qp, PageParams pageParams,
                          String processorClassName, String prevOutput, Finalizer finalizer,
                          int secondsBetweenTasks, boolean async, boolean incrementOffset)
      throws TaskSchedulingException, ProcessingException {
    // Load the processor class
    Processor proc = loadProcessor(processorClassName);
    if (proc == null) {
      throw new ProcessingException("Could not load processor with name " + processorClassName);
    }
    exec(domainId, qp, pageParams, proc, prevOutput, finalizer, secondsBetweenTasks, async,
        incrementOffset);
  }

  public static void exec(Long domainId, QueryParams qp, PageParams pageParams, Processor proc,
                          String prevOutput, Finalizer finalizer, int secondsBetweenTasks,
                          boolean async) throws TaskSchedulingException, ProcessingException {
    exec(domainId, qp, pageParams, proc, prevOutput, finalizer, secondsBetweenTasks, async, true);
  }

  @SuppressWarnings("rawtypes")
  // Execute the query and process results;
  // NOTE: prevOutput is the results returned from a previous execution; results from current execution will be passed to next task in chain
  //       finalizationUrl in finalizer is scheduled in the given queue, when this task chain completes
  public static void exec(Long domainId, QueryParams qp, PageParams pageParams, Processor proc,
                          String prevOutput, Finalizer finalizer, int secondsBetweenTasks,
                          boolean async, boolean incrementOffset)
      throws TaskSchedulingException, ProcessingException {

    xLogger.fine("Entered exec");
    if (qp == null || qp.query == null) {
      throw new IllegalArgumentException("Query class and/or processor not specified");
    }
    int maxResults = 0;
    if (pageParams != null) {
      maxResults = pageParams.getSize();
    }
    boolean moreData = false;
    String cursor = null;
    String output = prevOutput;
    String taskId = null;
    int offset = pageParams.getOffset();

    // Process results, if synchronous execution is sought
    if (!async) {
      long startTime = System.currentTimeMillis();
      // Get PM
      PersistenceManager pm;
      if (qp.query != null && (qp.query.toLowerCase().contains("dayslice") || qp.query.toLowerCase()
          .contains("monthslice") || qp.query.toLowerCase().contains("activecountsstatsstore"))) {
        pm = PMF.getReportsPM().getPersistenceManager();
      } else {
        pm = PMF.get().getPersistenceManager();
      }
      try {
        // Get results
        Query q;
        if (QueryParams.QTYPE.SQL.equals(qp.qType)) {
          String query = qp.query;
          if (query != null && pageParams != null && !query.contains(" LIMIT ")) {
            query +=
                " LIMIT " + pageParams.getOffset() + CharacterConstants.COMMA + pageParams
                    .getSize();
          }
          q = pm.newQuery("javax.jdo.query.SQL", query);
          q.setClass(JDOUtils.getImplClass(qp.qClazz));
        } else {
          q = pm.newQuery(qp.query);
          if (pageParams != null) {
            QueryUtil.setPageParams(q, pageParams);
          }
        }
        List results = null;
        int size = 0;
        try {
          results = execute(qp, q);
          if (results != null) {
            size = results.size();
            if (incrementOffset) {
              offset += size;
            }
            // Get the cursor
            cursor = QueryUtil.getCursor(results);
          }

          // Start processing...
          if (size == 0) { // IMPORTANT: process ONLY if there are results, otherwise return
            xLogger.info("No results to process for domain {0} using processor {1}", domainId,
                proc.getClass().getSimpleName());
            finalize(finalizer, prevOutput);
            // NOTE: 'finally' section closed PM; so no need to do it here
            return;
          }
          xLogger.info("EXEC: Processing {0} results...using {1}", size,
              proc.getClass().getSimpleName());
          // **** Process ****
          output = proc.process(domainId, new Results(results, cursor), prevOutput, pm);
          xLogger.info("EXEC: Completed process... took:{0} ms",
              (System.currentTimeMillis() - startTime));
          // Check if more data exists, and is to be processed
          moreData = maxResults != 0 && size == maxResults;
        } finally {
          q.closeAll();
        }
      } catch (Exception e) {
        xLogger.warn("Processing exception {0} for domain {1} using processor {2}", e.getMessage(),
            domainId, proc.getClass().getSimpleName(), e);
        throw new ProcessingException(e.getClass().getName() + ": " + e.getMessage());
      } finally {
        try {
          pm.close();
        } catch (Exception e) {
          xLogger.warn("Failed to close pm in PagedExec", e);
        }
      }
      // **** Completed ****
    }
    // If first call, then schedule next task
    if (async || moreData) {
      try {
        String processorClassName = proc.getClass().getName();
        Map<String, String> taskParams = new HashMap<>();
        taskParams.put("q", URLEncoder.encode(qp.query, "UTF-8"));
        if (qp.qType != null) {
          taskParams.put("qt", URLEncoder.encode(qp.qType.name(), "UTF-8"));
        }
        if (qp.qClazz != null) {
          taskParams.put("qc", URLEncoder.encode(qp.qClazz.getName(), "UTF-8"));
        }
        taskParams.put("proc", processorClassName);
        taskParams.put("o", String.valueOf(offset));
        taskParams.put("io", String.valueOf(incrementOffset));
        if (domainId != null) {
          taskParams.put("domainid", domainId.toString());
        }
        if (maxResults > 0) {
          taskParams.put("s", String.valueOf(maxResults));
        }
        if (cursor != null) {
          taskParams.put("c", cursor);
        }
        if (output != null && !output.isEmpty()) {
          taskParams.put("output", URLEncoder.encode(output, "UTF-8"));
        }
        String paramsStr = qp.toParamsString();
        if (paramsStr != null) {
          taskParams.put("params", URLEncoder.encode(paramsStr, "UTF-8"));
        }
        if (finalizer != null) {
          taskParams.put("furl", URLEncoder.encode(finalizer.url, "UTF-8"));
          taskParams.put("fqueue", finalizer.queue);
        }
        // Check if there has to be a gap between tasks
        long taskEta = 0;
        if (secondsBetweenTasks > 0) {
          taskEta = getEta(secondsBetweenTasks);
          // Add to task params for next run
          taskParams.put("taskinterval", String.valueOf(secondsBetweenTasks));
        }
        // Schedule next task
        getTaskService()
            .schedule(proc.getQueueName(), TASK_URL, taskParams, null, ITaskService.METHOD_POST,
                taskEta, domainId, null, "PAGED_EXEC");
      } catch (Exception e) {
        xLogger.warn("Scheduling exception {0} for domain {1} using processor {2}", e.getMessage(),
            domainId, proc.getClass().getSimpleName(), e);
        throw new ProcessingException(e.getClass().getName() + ": " + e.getMessage());
      }
    } else {
      // No more data, finalize if needed
      finalize(finalizer, output);
    }

    xLogger.fine("Exiting exec");
  }

  private static ITaskService getTaskService() {
    return AppFactory.get().getTaskService();
  }

  protected static List execute(QueryParams qp, Query q) {
    List results;
    if (qp.params != null && !qp.params.isEmpty()) {
      results = (List) q.executeWithMap(qp.params);
    } else if (qp.listParams != null && !qp.listParams.isEmpty()) {
      results = (List) q.executeWithArray(qp.listParams.toArray());
    } else {
      results = (List) q.execute();
    }
    return results;
  }

  // Finalize the task chain
  public static void finalize(Finalizer finalizer, String output) throws TaskSchedulingException {
    if (finalizer != null && finalizer.url != null) {
      String url = finalizer.url;
      Map<String, String> params = null;
      int qindex = finalizer.url.indexOf("?");
      if (qindex != -1) {
        url = url.substring(0, qindex);
        params = fromQueryString(finalizer.url);
        if (output != null && !output.isEmpty()) {
          if (params == null) {
            params = new HashMap<>();
          }
          params.put("output", output);
        }
      }
      xLogger.info("Finalizing with URL {0} in queue {1} with params {2}", url, finalizer.queue,
          params);
      getTaskService().schedule(finalizer.queue, url, params, ITaskService.METHOD_POST);
    }
  }

  // Load a processor
  public static Processor loadProcessor(String processorClassName) {
    try {
      return (Processor) Class.forName(processorClassName).newInstance();
    } catch (Exception e) {
      xLogger.severe("Exception {0} when loading class {1}: {2}", e.getClass().getName(),
          processorClassName, e.getMessage());
    }
    return null;
  }

  // Get the expected time of task execution
  public static long getEta(int secondsBetweenTasks) {
    Calendar cal = GregorianCalendar.getInstance();
    cal.add(Calendar.SECOND, secondsBetweenTasks);
    return cal.getTimeInMillis();
  }

  // Serialize a java object to a base 64 encoded string
  public static String serialize(Object obj) {
    try {
      ByteArrayOutputStream bytesOut = new ByteArrayOutputStream();
      ObjectOutputStream objectOut = new ObjectOutputStream(new BufferedOutputStream(bytesOut));
      objectOut.writeObject(obj);
      objectOut.close();
      return org.apache.commons.codec.binary.Base64.encodeBase64String(bytesOut.toByteArray());
    } catch (IOException e) {
      xLogger.severe("IOException when serializing: {0}", e.getMessage());
      e.printStackTrace();
      throw new RuntimeException(e);
    }
  }

  // Deserialize a java object from a base 64 encoded string
  public static Object deserialize(String base64String) {
    ObjectInputStream objectIn = null;
    try {
      byte[] bytesIn = org.apache.commons.codec.binary.Base64.decodeBase64(base64String);
      objectIn = new ObjectInputStream(new BufferedInputStream(new ByteArrayInputStream(bytesIn)));
      return objectIn.readObject();
    } catch (Exception e) {
      xLogger.severe("Error deserializing task: {0} : {1}", e.getClass().getName(), e.getMessage());
      return null; // don't retry task
    } finally {
      try {
        if (objectIn != null) {
          objectIn.close();
        }
      } catch (IOException ignore) {
        xLogger.warn("Exception while trying to close objectIn", ignore);
      }
    }
  }

  // Get the query string as a Map from a given URL
  private static Map<String, String> fromQueryString(String url) {
    if (url == null || url.isEmpty()) {
      return null;
    }
    int start = url.indexOf("?");
    if (start == -1) {
      return null;
    }
    Map<String, String> params = new HashMap<String, String>();
    StringTokenizer paramVals = new StringTokenizer(url.substring(start + 1), "&");
    while (paramVals.hasMoreTokens()) {
      String paramVal = paramVals.nextToken();
      int eq = paramVal.indexOf("=");
      if (eq == -1) {
        params.put(paramVal, "");
      } else {
        params.put(paramVal.substring(0, eq), paramVal.substring(eq + 1));
      }
    }
    xLogger.fine("url: {0}, params: {1}", url, params);
    return params;
  }

  public static class Finalizer {
    public String url = null;
    public String queue = ITaskService.QUEUE_DEFAULT;
  }
}

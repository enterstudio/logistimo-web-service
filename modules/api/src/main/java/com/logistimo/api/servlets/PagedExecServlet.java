/**
 *
 */
package com.logistimo.api.servlets;


import org.apache.commons.lang.StringUtils;
import com.logistimo.pagination.PageParams;
import com.logistimo.pagination.PagedExec;
import com.logistimo.pagination.PagedExec.Finalizer;
import com.logistimo.pagination.QueryParams;
import com.logistimo.services.ServiceException;
import com.logistimo.logger.XLog;

import java.io.IOException;
import java.net.URLDecoder;
import java.util.ResourceBundle;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Servlet to do a paged execution of a given process
 *
 * @author Arun
 */
public class PagedExecServlet extends SgServlet {

  private static final long serialVersionUID = 1L;
  private static final XLog xLogger = XLog.getLog(PagedExecServlet.class);

  @Override
  protected void processGet(HttpServletRequest request, HttpServletResponse response,
                            ResourceBundle backendMessages,
                            ResourceBundle messages) throws ServletException, IOException,
      ServiceException {
    processPost(request, response, backendMessages, messages);
  }

  @Override
  protected void processPost(HttpServletRequest request, HttpServletResponse response,
                             ResourceBundle backendMessages,
                             ResourceBundle messages) throws ServletException, IOException,
      ServiceException {
    xLogger.fine("Entered processPost");
    // Get parameters
    String query = request.getParameter("q");
    String qTypeStr = request.getParameter("qt");
    String qClazzStr = request.getParameter("qc");
    String processorClass = request.getParameter("proc");
    String domainIdStr = request.getParameter("domainid");
    String sizeStr = request.getParameter("s");
    String cursor = request.getParameter("c");
    String offsetStr = request.getParameter("o");
    String incrementOffsetStr = request.getParameter("io");
    String prevOutput = request.getParameter("output");
    String paramsStr = request.getParameter("params");
    String finalizerUrl = request.getParameter("furl");
    String finalizerQueue = request.getParameter("fqueue");
    String taskIntervalSecondsStr = request.getParameter("taskinterval");
    int secondsBetweenTasks = 0;
    if (taskIntervalSecondsStr != null && !taskIntervalSecondsStr.isEmpty()) {
      try {
        secondsBetweenTasks = Integer.parseInt(taskIntervalSecondsStr);
      } catch (Exception e) {
        xLogger.warn("{0} when trying to parse taskinterval {1} in domain {2}: {3}",
            e.getClass().getName(), taskIntervalSecondsStr, domainIdStr, e.getMessage());
      }
    }
    xLogger.info(
        "q: {0}, proc: {1}, s: {2}, c: {3}, output: {4}, params: {5}, task-interval-secs.: {6}",
        query, processorClass, sizeStr, cursor, prevOutput, paramsStr, taskIntervalSecondsStr);
    Long domainId = null;
    if (query != null) {
      query = URLDecoder.decode(query, "UTF-8");
    }
    if (prevOutput != null) {
      prevOutput = URLDecoder.decode(prevOutput, "UTF-8");
    }
    if (paramsStr != null) {
      paramsStr = URLDecoder.decode(paramsStr, "UTF-8");
    }
    if (domainIdStr != null && !domainIdStr.isEmpty()) {
      domainId = Long.valueOf(domainIdStr);
    }

    int offset = 1;
    if (offsetStr != null && !offsetStr.isEmpty()) {
      offset = Integer.valueOf(offsetStr);
    }
    boolean incrementOffset = true;
    if (StringUtils.isNotEmpty(incrementOffsetStr)) {
      incrementOffset = Boolean.parseBoolean(incrementOffsetStr);
    }
    int size = 0;
    if (sizeStr != null && !sizeStr.isEmpty()) {
      size = Integer.parseInt(sizeStr);
    }
    Finalizer finalizer = null;
    if (finalizerUrl != null && !finalizerUrl.isEmpty()) {
      finalizer = new Finalizer();
      finalizer.url = URLDecoder.decode(finalizerUrl, "UTF-8");
      if (finalizerQueue != null && !finalizerQueue.isEmpty()) {
        finalizer.queue = finalizerQueue;
      }
    }

    QueryParams.QTYPE qType = null;
    Class qClazz = null;
    if (qTypeStr != null) {
      qType = QueryParams.QTYPE.valueOf(qTypeStr);
      if (qType == QueryParams.QTYPE.SQL) {
        try {
          qClazz = Class.forName(qClazzStr);
        } catch (ClassNotFoundException e) {
          xLogger.severe("Class sent for qClazz is invalid in PagedExec {0}", qClazzStr, e);
          return;
        }
      }
    }
    // Form the query params
    QueryParams qp = new QueryParams(query, paramsStr, qType, qClazz);
    xLogger.info("Params: {0}", qp.params);
    PageParams pageParams = new PageParams(cursor, offset, size);
    try {
      PagedExec.exec(domainId, qp, pageParams, processorClass, prevOutput, finalizer,
          secondsBetweenTasks, false, incrementOffset);
    } catch (Exception e) {
      xLogger.severe("Exception {0} : {1}", e.getClass().getName(), e.getMessage());
    }
    xLogger.fine("Exiting processPost");
  }
}

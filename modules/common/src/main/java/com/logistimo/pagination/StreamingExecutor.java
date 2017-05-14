package com.logistimo.pagination;

import com.logistimo.AppFactory;
import com.logistimo.dao.JDOUtils;
import com.logistimo.services.utils.ConfigUtil;

import com.logistimo.pagination.processor.ProcessingException;
import com.logistimo.pagination.processor.Processor;
import com.logistimo.services.impl.PMF;
import com.logistimo.exception.TaskSchedulingException;
import com.logistimo.logger.XLog;

import java.util.List;
import java.util.concurrent.ScheduledThreadPoolExecutor;

import javax.jdo.PersistenceManager;
import javax.jdo.Query;

/**
 * @author Mohan Raja
 */
public class StreamingExecutor {

  private static final XLog xLogger = XLog.getLog(StreamingExecutor.class);
  public static ScheduledThreadPoolExecutor
      poolExecutor =
      new ScheduledThreadPoolExecutor(ConfigUtil.getInt("export.queue.size", 2));

  public static void exec(Long domainId, QueryParams qp, PageParams pageParams,
                          String processorClassName, String prevOutput,
                          PagedExec.Finalizer finalizer)
      throws TaskSchedulingException, ProcessingException {
    exec(domainId, qp, pageParams, processorClassName, prevOutput, finalizer, 0);
  }

  public static void exec(Long domainId, QueryParams qp, PageParams pageParams,
                          String processorClassName, String prevOutput,
                          PagedExec.Finalizer finalizer, int secondsBetweenTasks)
      throws TaskSchedulingException, ProcessingException {
    Processor proc = PagedExec.loadProcessor(processorClassName);
    if (proc == null) {
      throw new ProcessingException("Could not load processor with name " + processorClassName);
    }
    exec(domainId, qp, pageParams, proc, prevOutput, finalizer, secondsBetweenTasks, false);
  }

  public static void exec(Long domainId, QueryParams qparam, PageParams pageParams,
                          Processor proc, String prevOutput, PagedExec.Finalizer finalizer,
                          int secondsBetweenTasks, boolean async)
      throws TaskSchedulingException, ProcessingException {
    xLogger.fine("Entered exec");
    if (qparam == null || qparam.query == null) {
      throw new IllegalArgumentException("Query class and/or processor not specified");
    }
    final Processor p = proc;
    final Long did = domainId;
    final PagedExec.Finalizer f = finalizer;
    final QueryParams qp = qparam;
    final String po = prevOutput;
    Thread thread = new Thread() {
      public void run() {
        try {
          long startTime = System.currentTimeMillis();
          xLogger.info("EXEC: Processing results... using {0}", p.getClass().getSimpleName());
          PersistenceManager pm;
          if (qp.query != null && (qp.query.toLowerCase().contains("dayslice") || qp.query
              .toLowerCase().contains("monthslice") || qp.query.toLowerCase()
              .contains("activecountsstatsstore"))) {
            pm = PMF.getReportsPM().getPersistenceManager();
          } else {
            pm = PMF.get().getPersistenceManager();
          }
          Query q;
          if (QueryParams.QTYPE.SQL.equals(qp.qType)) {
            q = pm.newQuery("javax.jdo.query.SQL", qp.query);
            if (!qp.query.toLowerCase().contains("fdreasons")) {
              q.setClass(JDOUtils.getImplClass(qp.qClazz));
            }
            q.getFetchPlan().setFetchSize(ConfigUtil.getInt("stream.fetch.size", 10));
          } else if (QueryParams.QTYPE.CQL.equals(qp.qType)) {
            q = null;
          } else {
            q = pm.newQuery(qp.query);
          }
          try {
            List results;
            if (qp.params != null && !qp.params.isEmpty() && QueryParams.QTYPE.JQL
                .equals(qp.qType)) {
              results = (List) q.executeWithMap(qp.params);
            } else if (QueryParams.QTYPE.CQL.equals(qp.qType)) {
              results = AppFactory.get().getReportsDao().getResults(qp.query);
            } else if (qp.listParams != null && !qp.listParams.isEmpty()
                && QueryParams.QTYPE.SQL.equals(qp.qType)) {
              results = (List) q.executeWithArray(qp.listParams.toArray());
            } else {
              results = (List) q.execute();
            }
            String out = p.process(did, new Results(results, null), po, pm);
            xLogger.info("EXEC: Completed process... took:{0} ms",
                (System.currentTimeMillis() - startTime));
            PagedExec.finalize(f, out);
          } catch (Exception e) {
            xLogger.severe("Processing exception {0} for domain {1} using processor {2}",
                e.getMessage(), did, p.getClass().getSimpleName(), e);
          } finally {
            if (q != null) {
              q.closeAll();
            }
            try {
              pm.close();
            } catch (Exception e) {
              xLogger.warn("Failed to close pm in PagedExec", e);
            }
          }
        } catch (Throwable e) {
          xLogger
              .severe("Processing exception {0} for domain {1} using processor {2}", e.getMessage(),
                  did, p.getClass().getSimpleName(), e);
        }
      }
    };
    poolExecutor.execute(thread);
    xLogger.fine("Exiting exec");
  }
}

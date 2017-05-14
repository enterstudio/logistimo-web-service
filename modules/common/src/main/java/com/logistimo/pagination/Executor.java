package com.logistimo.pagination;

import com.logistimo.services.utils.ConfigUtil;

import com.logistimo.pagination.processor.ProcessingException;
import com.logistimo.pagination.processor.Processor;
import com.logistimo.exception.TaskSchedulingException;

/**
 * @author Mohan Raja
 */
public class Executor {
  private static boolean isGAE = ConfigUtil.isGAE();

  public static void exec(Long domainId, QueryParams qp, PageParams pageParams,
                          String processorClassName, String prevOutput,
                          PagedExec.Finalizer finalizer)
      throws TaskSchedulingException, ProcessingException {
    if (isGAE) {
      PagedExec.exec(domainId, qp, pageParams, processorClassName, prevOutput, finalizer);
    } else {
      StreamingExecutor.exec(domainId, qp, null, processorClassName, prevOutput, finalizer);
    }
  }

  public static void exec(Long domainId, QueryParams qp, PageParams pageParams,
                          String processorClassName, String prevOutput,
                          PagedExec.Finalizer finalizer, int secondsBetweenTasks)
      throws TaskSchedulingException, ProcessingException {
    if (isGAE) {
      PagedExec.exec(domainId, qp, pageParams, processorClassName, prevOutput, finalizer,
          secondsBetweenTasks);
    } else {
      StreamingExecutor
          .exec(domainId, qp, null, processorClassName, prevOutput, finalizer, secondsBetweenTasks);
    }
  }

  public static void exec(Long domainId, QueryParams qp, PageParams pageParams,
                          Processor proc, String prevOutput, PagedExec.Finalizer finalizer,
                          int secondsBetweenTasks, boolean async)
      throws TaskSchedulingException, ProcessingException {
    if (isGAE) {
      PagedExec
          .exec(domainId, qp, pageParams, proc, prevOutput, finalizer, secondsBetweenTasks, async);
    } else {
      StreamingExecutor
          .exec(domainId, qp, null, proc, prevOutput, finalizer, secondsBetweenTasks, async);
    }
  }
}

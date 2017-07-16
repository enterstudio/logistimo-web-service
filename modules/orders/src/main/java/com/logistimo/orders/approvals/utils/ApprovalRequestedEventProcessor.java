package com.logistimo.orders.approvals.utils;

import com.codahale.metrics.Meter;
import com.logistimo.approvals.client.models.CreateApprovalResponse;
import com.logistimo.logger.XLog;
import com.logistimo.services.ServiceException;
import com.logistimo.utils.MetricsUtil;

import org.apache.camel.Handler;

/**
 * Created by charan on 14/07/17.
 */
public class ApprovalRequestedEventProcessor {

  private static Meter jmsMeter = MetricsUtil
      .getMeter(ApprovalStatusUpdateEventProcessor.class, "approvalRequested");

  private static final XLog xLogger = XLog.getLog(ApprovalRequestedEventProcessor.class);

  @Handler
  public void execute(CreateApprovalResponse approvalResponse) throws ServiceException {
    //TODO check if approval mapping exists for the order
    jmsMeter.mark();
    xLogger.info("Approval requested {0}", approvalResponse);
  }
}

package com.logistimo.orders.approvals.utils;

import com.codahale.metrics.Meter;
import com.logistimo.logger.XLog;
import com.logistimo.orders.approvals.dao.IApprovalsDao;
import com.logistimo.orders.approvals.dao.impl.ApprovalsDao;
import com.logistimo.services.ServiceException;
import com.logistimo.utils.MetricsUtil;

import org.apache.camel.Handler;

/**
 * Created by nitisha.khandelwal on 02/06/17.
 */

public class ApprovalStatusUpdateEventProcessor {

  private static final String ORDER = "order";

  private static Meter jmsMeter = MetricsUtil
      .getMeter(ApprovalStatusUpdateEventProcessor.class, "approvalStatusUpdateEventMeter");

  private static final XLog xLogger = XLog.getLog(ApprovalStatusUpdateEventProcessor.class);

  private IApprovalsDao approvalDao = new ApprovalsDao();

  @Handler
  public void execute(ApprovalStatusUpdateEvent event) throws ServiceException {
    jmsMeter.mark();
    xLogger.info("Received event - {0}", event);
    if (ORDER.equalsIgnoreCase(event.getType())) {
      approvalDao.updateOrderApprovalStatus(Long.valueOf(event.getTypeId()), event.getApprovalId(),
          event.getStatus());
    }
  }
}

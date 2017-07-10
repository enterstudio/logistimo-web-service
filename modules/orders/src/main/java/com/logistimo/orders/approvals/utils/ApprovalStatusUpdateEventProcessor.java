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
  private static final XLog xLogger = XLog.getLog(ApprovalStatusUpdateEventProcessor.class);
  private static Meter jmsMeter = MetricsUtil
      .getMeter(ApprovalStatusUpdateEventProcessor.class, "approvalStatusUpdateEventMeter");
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

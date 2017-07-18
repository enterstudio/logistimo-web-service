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
import com.logistimo.approvals.client.models.CreateApprovalResponse;
import com.logistimo.logger.XLog;
import com.logistimo.orders.approvals.dao.IApprovalsDao;
import com.logistimo.orders.approvals.service.IOrderApprovalsService;
import com.logistimo.orders.entity.approvals.IOrderApprovalMapping;
import com.logistimo.orders.service.OrderManagementService;
import com.logistimo.services.ObjectNotFoundException;
import com.logistimo.services.ServiceException;
import com.logistimo.utils.MetricsUtil;

import org.apache.camel.Handler;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Created by charan on 14/07/17.
 */

public class ApprovalRequestedEventProcessor {

  private static Meter jmsMeter = MetricsUtil
      .getMeter(ApprovalStatusUpdateEventProcessor.class, "approvalRequested");

  private static final XLog xLogger = XLog.getLog(ApprovalRequestedEventProcessor.class);

  @Autowired
  private IApprovalsDao approvalDao;

  @Autowired
  private OrderManagementService orderManagementService;

  @Autowired
  private IOrderApprovalsService orderApprovalsService;

  @Handler
  public void execute(ApprovalCreatedEvent event) throws ServiceException {

    jmsMeter.mark();
    xLogger.info("Approval created event received -  {0}", event);

    IOrderApprovalMapping orderApprovalMapping = approvalDao
        .getOrderApprovalMapping(event.getApprovalId());

    if (orderApprovalMapping == null) {
      xLogger.info("Order approval was created, but no mapping found for order {0}",
          event.getTypeId());
      try {
        approvalDao.updateOrderApprovalMapping(createApprovalResponseFromEvent(event),
            orderApprovalsService
                .getApprovalType(orderManagementService.getOrder(Long.valueOf(event.getTypeId()))));
      } catch (ObjectNotFoundException e) {
        xLogger.warn("Order not available for order id - ", event.getTypeId(), e);
      }

    }
  }

  private CreateApprovalResponse createApprovalResponseFromEvent(ApprovalCreatedEvent event) {

    CreateApprovalResponse createApprovalResponse = new CreateApprovalResponse();
    createApprovalResponse.setApprovalId(event.getApprovalId());
    createApprovalResponse.setType(event.getType());
    createApprovalResponse.setTypeId(event.getTypeId());
    createApprovalResponse.setRequesterId(event.getRequesterId());
    createApprovalResponse.setStatus(event.getStatus());
    createApprovalResponse.setConversationId(event.getConversationId());
    createApprovalResponse.setSourceDomainId(event.getSourceDomainId());
    createApprovalResponse.setDomains(event.getDomains());
    createApprovalResponse.setExpireAt(event.getExpireAt());
    createApprovalResponse.setAttributes(event.getAttributes());
    createApprovalResponse.setActiveApproverType(event.getActiveApproverType());
    createApprovalResponse.setCreatedAt(event.getCreatedAt());
    createApprovalResponse.setUpdatedAt(event.getUpdatedAt());
    createApprovalResponse.setUpdatedBy(event.getUpdatedBy());
    return createApprovalResponse;
  }

}

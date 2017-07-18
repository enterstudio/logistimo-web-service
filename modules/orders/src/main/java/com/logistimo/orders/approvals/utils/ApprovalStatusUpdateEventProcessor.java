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
import com.logistimo.communications.MessageHandlingException;
import com.logistimo.communications.service.MessageService;
import com.logistimo.entities.entity.IKiosk;
import com.logistimo.entities.service.EntitiesService;
import com.logistimo.entities.service.EntitiesServiceImpl;
import com.logistimo.logger.XLog;
import com.logistimo.orders.approvals.dao.IApprovalsDao;
import com.logistimo.orders.approvals.dao.impl.ApprovalsDao;
import com.logistimo.orders.entity.approvals.IOrderApprovalMapping;
import com.logistimo.services.ObjectNotFoundException;
import com.logistimo.services.Resources;
import com.logistimo.services.ServiceException;
import com.logistimo.services.Services;
import com.logistimo.users.entity.IUserAccount;
import com.logistimo.users.service.UsersService;
import com.logistimo.users.service.impl.UsersServiceImpl;
import com.logistimo.utils.LocalDateUtil;
import com.logistimo.utils.MetricsUtil;

import org.apache.camel.Handler;
import org.apache.commons.lang.text.StrSubstitutor;

import java.io.IOException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

/**
 * Created by nitisha.khandelwal on 02/06/17.
 */

public class ApprovalStatusUpdateEventProcessor {

  private static final String ORDER = "order";
  private static final String APPROVED_STATUS = "ap";
  private static final String REJECTED_STATUS = "rj";
  private static final String CANCELLED_STATUS = "cn";
  private static final String EXPIRED_STATUS = "ex";

  private IApprovalsDao approvalDao = new ApprovalsDao();

  private static Meter jmsMeter = MetricsUtil
      .getMeter(ApprovalStatusUpdateEventProcessor.class, "approvalStatusUpdateEventMeter");

  private static final XLog xLogger = XLog.getLog(ApprovalStatusUpdateEventProcessor.class);

  @Handler
  public void execute(ApprovalStatusUpdateEvent event) throws ServiceException {

    jmsMeter.mark();
    xLogger.info("Approval status update event received - {0}", event);

    if (ORDER.equalsIgnoreCase(event.getType())) {

      UsersService usersService = Services.getService(UsersServiceImpl.class);
      EntitiesService entitiesService = Services.getService(EntitiesServiceImpl.class);

      try {

        IOrderApprovalMapping orderApprovalMapping = approvalDao
            .getOrderApprovalMapping(event.getApprovalId());

        approvalDao.updateOrderApprovalStatus(Long.valueOf(event.getTypeId()),
            event.getApprovalId(), event.getStatus());

        IUserAccount requester = usersService.getUserAccount(event.getRequesterId());

        MessageService messageService = MessageService
            .getInstance(MessageService.SMS, requester.getCountry());

        IKiosk kiosk = entitiesService.getKiosk(orderApprovalMapping.getKioskId());

        String resolvedMessage = getMessage(event, orderApprovalMapping, requester, kiosk);

        if (!CANCELLED_STATUS.equalsIgnoreCase(event.getStatus())) {
          messageService.send(requester, resolvedMessage, MessageService.NORMAL, null, null, null);
        }

        for (String approverId : event.getApproverIds()) {
          IUserAccount approver = usersService.getUserAccount(approverId);
          messageService.send(approver, resolvedMessage, MessageService.NORMAL, null, null, null);
        }

      } catch (ObjectNotFoundException e) {
        xLogger.warn("Object not found : {0}", e);
      } catch (IOException e) {
        xLogger.warn("Error in sending message - ", e);
      } catch (MessageHandlingException e) {
        xLogger.warn("Error in building message status - ", e);
      } catch (Exception e) {
        xLogger.warn("Error in handling approval message - ", e);
      }
    }
  }

  private String getMessage(ApprovalStatusUpdateEvent event, IOrderApprovalMapping orderApproval,
      IUserAccount requester, IKiosk kiosk) {

    String message = getMessage(event.getStatus(), requester.getLocale());
    Map<String, String> values = new HashMap<>();
    values.put("approvalType", ApprovalUtils.getApprovalType(orderApproval.getApprovalType()));
    values.put("requestorName", requester.getFullName());
    values.put("requestorPhone", requester.getMobilePhoneNumber());
    values.put("entityName", kiosk.getName());
    values.put("entityCity", requester.getCity());
    values.put("orderId", event.getTypeId());
    values.put("statusChangedTime", LocalDateUtil.format(event.getUpdatedAt(),
        requester.getLocale(), requester.getTimezone()));

    StrSubstitutor sub = new StrSubstitutor(values);

    return sub.replace(message);
  }

  private String getMessage(String status, Locale locale) {
    String message;
    ResourceBundle messages = Resources.get().getBundle("Messages", locale);
    switch (status) {
      case APPROVED_STATUS:
        message = messages.getString("approval.approved.message");
        break;
      case REJECTED_STATUS:
        message = messages.getString("approval.rejected.message");
        break;
      case CANCELLED_STATUS:
        message = messages.getString("approval.cancelled.message");
        break;
      case EXPIRED_STATUS:
        message = messages.getString("approval.expired.message");
        break;
      default:
        message = messages.getString("approval.status.general.message");
    }
    return message;
  }
}

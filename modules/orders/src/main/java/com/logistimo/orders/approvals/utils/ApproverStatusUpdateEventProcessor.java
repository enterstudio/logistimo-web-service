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
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.text.StrSubstitutor;
import org.springframework.util.CollectionUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

/**
 * Created by nitisha.khandelwal on 04/07/17.
 */

public class ApproverStatusUpdateEventProcessor {

  private static final String ORDER = "order";
  private static final String ACTIVE_STATUS = "ac";
  private static final String EXPIRED_STATUS = "ex";

  private IApprovalsDao approvalDao = new ApprovalsDao();

  private static Meter jmsMeter = MetricsUtil
      .getMeter(ApproverStatusUpdateEventProcessor.class, "approverStatusUpdateEventMeter");

  private static final XLog xLogger = XLog.getLog(ApproverStatusUpdateEventProcessor.class);

  @Handler
  public void execute(ApproverStatusUpdateEvent event)
      throws ServiceException, MessageHandlingException, IOException {

    jmsMeter.mark();
    xLogger.info("Approver status update event received - {0}", event);

    if (ORDER.equalsIgnoreCase(event.getType())) {

      UsersService usersService = Services.getService(UsersServiceImpl.class);
      EntitiesService entitiesService = Services.getService(EntitiesServiceImpl.class);

      try {

        IOrderApprovalMapping orderApprovalMapping = approvalDao
            .getOrderApprovalMapping(event.getApprovalId());

        IUserAccount requester = usersService.getUserAccount(event.getRequesterId());
        IUserAccount userAccount = usersService.getUserAccount(event.getUserId());

        IKiosk kiosk = entitiesService.getKiosk(orderApprovalMapping.getKioskId());

        MessageService messageService = MessageService.getInstance(
            MessageService.SMS, userAccount.getCountry(), true, kiosk.getDomainId(), null, null);

        List<String> nextApproverNames = new ArrayList<>();

        if (!CollectionUtils.isEmpty(event.getNextApproverIds())) {
          for (String nextApproverId : event.getNextApproverIds()) {
            nextApproverNames.add(usersService.getUserAccount(nextApproverId).getFullName());
          }
        }

        String message = getMessage(event, orderApprovalMapping, requester, kiosk,
            nextApproverNames);

        messageService.send(userAccount, message, MessageService.NORMAL, null, null, null);

      } catch (ObjectNotFoundException e) {
        xLogger.warn("Error in building message status", e);
      }
    }
  }

  private String getMessage(ApproverStatusUpdateEvent event, IOrderApprovalMapping orderApproval,
      IUserAccount requester, IKiosk kiosk, List<String> nextApproverNames) {

    String message = null;
    ResourceBundle messages = Resources.get().getBundle("Messages", requester.getLocale());

    Map<String, String> values = new HashMap<>();
    values.put("approvalType", ApprovalUtils.getApprovalType(orderApproval.getApprovalType()));
    values.put("orderId", event.getTypeId());
    values.put("requestorName", requester.getFullName());
    values.put("requestorPhone", requester.getMobilePhoneNumber());
    values.put("eName", kiosk.getName());
    values.put("eCity", kiosk.getCity());
    values.put("requestedAt", LocalDateUtil.format(event.getRequestedAt(),
        requester.getLocale(), requester.getTimezone()));

    values.put("expiryInHours", String.valueOf(event.getExpiryInHours()));

    if (event.getExpiryTime() != null) {
      values.put("expiryTime", LocalDateUtil.format(event.getExpiryTime(),
          requester.getLocale(), requester.getTimezone()));
    }

    StrSubstitutor sub = new StrSubstitutor(values);

    if (ACTIVE_STATUS.equalsIgnoreCase(event.getStatus())) {
      message = messages.getString("approver.activation.message");
    }

    if (EXPIRED_STATUS.equalsIgnoreCase(event.getStatus())) {
      message = messages.getString("approver.expiry.message");
    }

    if (EXPIRED_STATUS.equalsIgnoreCase(event.getStatus()) &&
        !CollectionUtils.isEmpty(nextApproverNames)) {
      values.put("secondaryApproversCSV", StringUtils.join(nextApproverNames, ','));
      message = messages.getString("approver.expiry.next.message");
    }

    return sub.replace(message);
  }
}

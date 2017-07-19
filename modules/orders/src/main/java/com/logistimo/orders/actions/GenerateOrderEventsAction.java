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

package com.logistimo.orders.actions;

import com.logistimo.config.models.EventSpec;
import com.logistimo.dao.JDOUtils;
import com.logistimo.events.EventConstants;
import com.logistimo.events.entity.IEvent;
import com.logistimo.events.models.CustomOptions;
import com.logistimo.events.processor.EventPublisher;
import com.logistimo.logger.XLog;
import com.logistimo.orders.dao.IOrderDao;
import com.logistimo.orders.entity.IOrder;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by charan on 19/07/17.
 */
@Component
public class GenerateOrderEventsAction {

  private static final XLog xLogger = XLog.getLog(GenerateOrderEventsAction.class);

  @Autowired
  private IOrderDao orderDao;

  // Generate order events, if configured
  public void invoke(Long domainId, int eventId, Long orderId, String orderStatus, String message,
                     List<String> userIds) {
    try {

      Map<String, Object> params = null;
      if (eventId == IEvent.STATUS_CHANGE) {
        params = new HashMap<>();
        params.put(EventConstants.PARAM_STATUS, orderStatus);
      }
      // Custom options
      CustomOptions customOptions = new CustomOptions();
      if (message != null && !message.isEmpty() || (userIds != null && !userIds.isEmpty())) {
        customOptions.message = message;
        if (userIds != null && !userIds.isEmpty()) {
          Map<Integer, List<String>> userIdsMap = new HashMap<>(1);
          userIdsMap.put(EventSpec.NotifyOptions.IMMEDIATE, userIds);
          customOptions.userIds = userIdsMap;
        }
      }

      // Generate event, if needed
      EventPublisher
          .generate(domainId != null ? domainId : orderDao.getOrder(orderId).getDomainId(), eventId,
              params,
              JDOUtils.getImplClass(IOrder.class).getName(), String.valueOf(orderId),
              customOptions);
    } catch (Exception e) {
      xLogger.severe("{0} when generating Order event {1} for order {2} in domain {3}: {4}",
          e.getClass().getName(), eventId, orderId, domainId, e);
    }
  }
}

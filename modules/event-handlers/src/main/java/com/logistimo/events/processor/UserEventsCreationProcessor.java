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

/**
 *
 */
package com.logistimo.events.processor;

import com.logistimo.config.models.DomainConfig;
import com.logistimo.config.models.EventSpec;
import com.logistimo.config.models.EventsConfig;
import com.logistimo.dao.JDOUtils;
import com.logistimo.events.EventConstants;
import com.logistimo.events.handlers.EventHandler;
import com.logistimo.services.taskqueue.ITaskService;
import com.logistimo.users.entity.IUserAccount;

import com.logistimo.events.entity.IEvent;
import com.logistimo.pagination.Results;
import com.logistimo.pagination.processor.ProcessingException;
import com.logistimo.pagination.processor.Processor;
import com.logistimo.utils.LocalDateUtil;
import com.logistimo.logger.XLog;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.jdo.PersistenceManager;


/**
 * Generate user events (esp. user dormant/inactive)
 *
 * @author Arun
 */
public class UserEventsCreationProcessor implements Processor {

  // Logger
  private static final XLog xLogger = XLog.getLog(UserEventsCreationProcessor.class);

  // Get order expiry/inactive event
  private IEvent getInactiveEvent(Long domainId, IUserAccount u, EventSpec.ParamSpec paramSpec,
                                         Date inactiveDate) {
    xLogger.fine("Entered getInactiveEvent");
    IEvent e = null;
    if (isUserInactive(u, inactiveDate)) {
      e =
          JDOUtils.createInstance(IEvent.class)
              .init(domainId, IEvent.EXPIRED, paramSpec.getParams(),
                  JDOUtils.getImplClass(IUserAccount.class).getName(), u.getKeyString(),
                  paramSpec.isRealTime(), null, null, -1,
                  LocalDateUtil
                      .getZeroTime(DomainConfig.getInstance(domainId).getTimezone()).getTime());
    }
    return e;
  }

  public boolean isUserInactive(IUserAccount u, Date inactiveDate) {
    Date loginDate = u.getLastLogin();
    Date lre = u.getLastMobileAccessed();
    return u.isEnabled() && ((loginDate == null || loginDate.compareTo(inactiveDate) == -1) && (lre == null || lre.compareTo(inactiveDate) == -1));
  }

  @SuppressWarnings("unchecked")
  @Override
  public String process(Long domainId, Results results, String prevOutput, PersistenceManager pm)
      throws ProcessingException {
    xLogger.fine("Entered UserEventsCreationProcessor.process");
    if (results == null) {
      return null;
    }
    List<IUserAccount> users = results.getResults();
    if (users == null) {
      return null;
    }

    //Check if list is empty without using size. To avoid eager fetching.
    try {
      users.get(0);
    } catch (IndexOutOfBoundsException e) {
      return null;
    }
    // Get the events config.
    EventsConfig ec = DomainConfig.getInstance(domainId).getEventsConfig();
    // Get the user inactive event details
    float inactiveDuration = 0;
    EventSpec.ParamSpec paramSpecExpiry = null;
    EventSpec
        espec =
        ec.getEventSpec(IEvent.EXPIRED,
            JDOUtils.getImplClass(IUserAccount.class).getName()); // no user login
    if (espec != null) {
      paramSpecExpiry = espec.getFirstParamSpec();
      if (paramSpecExpiry != null) {
        Map<String, Object> params = paramSpecExpiry.getParams();
        String inactiveDurationStr = (String) params.get(EventConstants.PARAM_INACTIVEDURATION);
        if (inactiveDurationStr != null && !inactiveDurationStr.isEmpty()) {
          try {
            inactiveDuration = Float.parseFloat(inactiveDurationStr);
          } catch (NumberFormatException e) {
            xLogger
                .warn("Number format exception in inactiveDuration {0}: {1}", inactiveDurationStr,
                    e.getMessage());
          }
        }
      }
    }
    if (inactiveDuration == 0) {
      return null;
    }
    // Get inactivity date limit
    Calendar cal = LocalDateUtil.getZeroTime(DomainConfig.getInstance(domainId).getTimezone());
    cal.add(Calendar.DATE, -1 * Math.round(inactiveDuration));
    Date inactiveDate = cal.getTime();
    // Loop over orders and generate relevant events
    List<IEvent> events = new ArrayList<IEvent>();
    Iterator<IUserAccount> it = users.iterator();
    while (it.hasNext()) {
      IUserAccount u = it.next();
      // Generate fulfillment due event if needed
      IEvent e = getInactiveEvent(domainId, u, paramSpecExpiry, inactiveDate);
      if (e != null) {
        events.add(e);
      }
    }
    if (!events.isEmpty()) {
      EventHandler.log(events);
    }
    xLogger.fine("Exiting UserEventsGenerationProcessor.process");
    return null;
  }

  @Override
  public String getQueueName() {
    return ITaskService.QUEUE_MESSAGE;
  }
}

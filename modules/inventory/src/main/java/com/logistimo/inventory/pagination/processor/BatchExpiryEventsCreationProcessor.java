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
package com.logistimo.inventory.pagination.processor;

import com.logistimo.config.models.DomainConfig;
import com.logistimo.dao.JDOUtils;
import com.logistimo.events.EventConstants;
import com.logistimo.events.exceptions.EventGenerationException;
import com.logistimo.events.processor.EventPublisher;
import com.logistimo.inventory.entity.IInvntryBatch;
import com.logistimo.services.taskqueue.ITaskService;

import com.logistimo.events.entity.IEvent;
import com.logistimo.pagination.Results;
import com.logistimo.pagination.processor.ProcessingException;
import com.logistimo.pagination.processor.Processor;
import com.logistimo.utils.LocalDateUtil;
import com.logistimo.logger.XLog;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.jdo.PersistenceManager;


/**
 * Generate batch expiry events
 *
 * @author Arun
 */
public class BatchExpiryEventsCreationProcessor implements Processor {

  // Logger
  private static final XLog xLogger = XLog.getLog(BatchExpiryEventsCreationProcessor.class);

  @SuppressWarnings("unchecked")
  @Override
  public String process(Long domainId, Results results, String expiresInDaysStr,
                        PersistenceManager pm) throws ProcessingException {
    xLogger.fine("Entered BatchExpiryEventsCreationProcessor.process");
    if (results == null) {
      return expiresInDaysStr;
    }
    List<IInvntryBatch> invBatches = results.getResults();
    if (invBatches == null) {
      return expiresInDaysStr;
    }

    //Check if list is empty without using size. To avoid eager fetching.
    try {
      invBatches.get(0);
    } catch (IndexOutOfBoundsException e) {
      return expiresInDaysStr;
    }
    xLogger.info(
        "Found {0} inv. batches matching expiry event for expiresInDaysStr={1} in domain {2}...",
        invBatches.size(), expiresInDaysStr, domainId);

    // Loop over orders and generate relevant events
    Iterator<IInvntryBatch> it = invBatches.iterator();
    while (it.hasNext()) {
      // Form event params for every invBatch.
      Map<String, Object> params = new HashMap<>();
      params.put(EventConstants.PARAM_EXPIRESINDAYS, expiresInDaysStr);
      params.put(EventConstants.EVENT_TIME, LocalDateUtil.getZeroTime(
          DomainConfig.getInstance(domainId).getTimezone()).getTime());
      IInvntryBatch invBatch = it.next();
      // Check if this inv. batch is in the source domain; if not, do not generate event again (superdomains)
      if (!invBatch.getDomainId().equals(domainId)) {
        continue;
      }

      // Generate event, if configured
      try {
        EventPublisher.generate(domainId, IEvent.EXPIRED, params,
            JDOUtils.getImplClass(IInvntryBatch.class).getName(), invBatch.getKeyString(), null);
      } catch (EventGenerationException e) {
        xLogger.warn("Exception when generating event for inv batch {0} expired in domain {1}: {2}",
            invBatch.getBatchId(), domainId, e.getMessage());
      }

    }
    xLogger.fine("Exiting BatchExpiryEventsCreationProcessor.process");
    return expiresInDaysStr;
  }

  @Override
  public String getQueueName() {
    return ITaskService.QUEUE_MESSAGE;
  }
}

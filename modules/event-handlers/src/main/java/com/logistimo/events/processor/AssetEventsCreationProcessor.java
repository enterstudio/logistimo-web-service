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
import com.logistimo.dao.JDOUtils;
import com.logistimo.events.EventConstants;
import com.logistimo.events.generators.EventGenerator;
import com.logistimo.events.generators.EventGeneratorFactory;
import com.logistimo.events.handlers.EventHandler;
import com.logistimo.services.taskqueue.ITaskService;

import com.logistimo.assets.entity.IAssetStatus;
import com.logistimo.events.entity.IEvent;
import com.logistimo.pagination.Results;
import com.logistimo.pagination.processor.ProcessingException;
import com.logistimo.pagination.processor.Processor;
import com.logistimo.utils.LocalDateUtil;
import com.logistimo.logger.XLog;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.jdo.PersistenceManager;

/**
 * Generate Asset events
 *
 * @author Charan
 */
public class AssetEventsCreationProcessor implements Processor {

  // Logger
  private static final XLog xLogger = XLog.getLog(AssetEventsCreationProcessor.class);

  // Generate events for temperature no data.
  @SuppressWarnings("unchecked")
  @Override
  public String process(Long domainId, Results results, String prevOutput, PersistenceManager pm)
      throws ProcessingException {
    xLogger.fine("Entered TeEventsCreationProcessor.process");
    if (results == null) {
      return prevOutput;
    }
    List<IAssetStatus> assetStatuses = results.getResults();
    if (assetStatuses == null) {
      return prevOutput;
    }

    //Check if list is empty without using size. To avoid eager fetching.
    try {
      assetStatuses.get(0);
    } catch (IndexOutOfBoundsException e) {
      return prevOutput;
    }

    Iterator<IAssetStatus> it = assetStatuses.iterator();
    List<IEvent> events = new ArrayList<>(1);
    while (it.hasNext()) {
      Map<String, Object> params = null;
      if (prevOutput != null) {
        params = new HashMap<>(1);
        params.put(EventConstants.PARAM_INACTIVEDURATION, prevOutput);
      }

      IAssetStatus assetStatus = it.next();
      Map<String, Object> tagParams = EventHandler.getTagParams(assetStatus);
      if (params != null && !params.isEmpty() && tagParams != null && !tagParams.isEmpty()) {
        params.putAll(EventHandler.getTagParams(assetStatus));
      }

      // Generate event, if configured.
      EventGenerator eg = EventGeneratorFactory.getEventGenerator(domainId,
          JDOUtils.getImplClass(IAssetStatus.class).getName());

      EventSpec.ParamSpec paramSpec = eg.match(IEvent.NO_ACTIVITY, params);
      IEvent e = null;
      if (paramSpec != null) {
        e = JDOUtils.createInstance(IEvent.class).init(domainId, IEvent.NO_ACTIVITY, params,
            JDOUtils.getImplClass(IAssetStatus.class).getName(),
            String.valueOf(assetStatus.getId()),
            paramSpec.isRealTime(), null, null, -1, LocalDateUtil.getZeroTime(
                DomainConfig.getInstance(domainId).getTimezone()).getTime());
      }
      if (e != null) // Accumulate the events for this prevOutput
      {
        events.add(e);
      }
    }
    if (!events.isEmpty()) // Persist the events
    {
      EventHandler.log(events);
    }

    xLogger.fine("Exiting TemperatureEventsCreationProcessor.process");
    return prevOutput;
  }

  @Override
  public String getQueueName() {
    return ITaskService.QUEUE_MESSAGE;
  }
}

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

package com.logistimo.events.processor;

import com.logistimo.config.models.DomainConfig;
import com.logistimo.events.entity.IEvent;
import com.logistimo.events.generators.EventGenerator;
import com.logistimo.events.generators.EventGeneratorFactory;
import com.logistimo.events.models.EventData;
import com.logistimo.logger.XLog;

import org.apache.camel.Handler;

/**
 * Created by charan on 07/03/17.
 */
public class EventProcessor {

  XLog log = XLog.getLog(EventProcessor.class);

  @Handler
  public void execute(EventData eventData) {
    try {
      EventGenerator eg = EventGeneratorFactory.getEventGenerator(eventData.domainId,
          eventData.objectType);
      if(eventData.eventId == IEvent.DELETED) {
        DomainConfig dc = DomainConfig.getInstance(eventData.domainId);
        eg.generateDeleteEvent(eg, eventData.eventObject, eventData.objectId, dc.getLocale(), dc.getTimezone(), eventData.params);
      } else {
        eg.generate(eventData.eventId, eventData.params, eventData.objectId,
            eventData.customOptions);
      }
    } catch (Exception e) {
      log.severe("Issue with submitted task {0} with error {1}", eventData, e.getMessage(), e);
      throw new RuntimeException(e);
    }
  }
}

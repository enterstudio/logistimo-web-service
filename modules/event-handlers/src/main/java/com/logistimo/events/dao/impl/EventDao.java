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

package com.logistimo.events.dao.impl;


import com.logistimo.events.dao.IEventDao;

import com.logistimo.events.entity.IEvent;
import com.logistimo.services.impl.PMF;
import com.logistimo.logger.XLog;

import java.util.Date;
import java.util.Iterator;
import java.util.List;

import javax.jdo.PersistenceManager;

/**
 * Created by charan on 16/03/15.
 */
public class EventDao implements IEventDao {

  private static final XLog xLogger = XLog.getLog(EventDao.class);

  public Long store(IEvent event) {
    xLogger.fine("Entering EventHandler.log");
    if (event == null) {
      throw new IllegalArgumentException("Invalid event input");
    }
    // Update event object
    if (event.getTimestamp() == null) {
      event.setTimestamp(new Date());
    }
    // Use the data store service directly, given its callbacks are being used
    PersistenceManager pm = null;
    IEvent storedEvent = null;
    try {
      pm = PMF.get().getPersistenceManager();
      storedEvent = pm.makePersistent(event);
    } catch (Exception e) {
      xLogger.warn("Failed to persist event {}", event);
      throw new RuntimeException(e);
    } finally {
      if (pm != null) {
        pm.close();
      }
    }
    xLogger.fine("Exiting EventHandler.log:");
    return storedEvent.getKey();
  }

  public void store(List<IEvent> events) {
    xLogger.fine("Entered log(events)");
    if (events == null || events.isEmpty()) {
      throw new IllegalArgumentException("Invalid events input");
    }

    Iterator<IEvent> it = events.iterator();
    PersistenceManager pm = null;
    try {
      pm = PMF.get().getPersistenceManager();
      while (it.hasNext()) {
        pm.makePersistent(it.next());
      }
    } catch (Exception e) {
      xLogger.warn("Failed to persist events {}", events);
      throw new RuntimeException(e);
    } finally {
      if (pm != null) {
        pm.close();
      }
    }

    xLogger.fine("Exiting log(events)");
  }
}

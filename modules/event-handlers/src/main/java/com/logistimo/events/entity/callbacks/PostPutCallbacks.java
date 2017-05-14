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
package com.logistimo.events.entity.callbacks;


import com.logistimo.AppFactory;
import com.logistimo.communications.service.MessageService;
import com.logistimo.config.models.EventSpec;
import com.logistimo.events.entity.IEvent;
import com.logistimo.services.taskqueue.ITaskService;

import org.datanucleus.enhancer.Persistable;
import com.logistimo.logger.XLog;

import java.util.HashMap;
import java.util.Map;

import javax.jdo.listener.InstanceLifecycleEvent;
import javax.jdo.listener.StoreLifecycleListener;


/**
 * Callbacks after an entity is put into datastore
 *
 * @author Arun
 */
public class PostPutCallbacks implements StoreLifecycleListener {

  private static final XLog xLogger = XLog.getLog(PostPutCallbacks.class);

  private static final String NOTIFIER_URL = "/task/notifier";

  private static ITaskService taskService = AppFactory.get().getTaskService();

	/*static{
                PMF.get().addInstanceLifecycleListener(new PostPutCallbacks(), new Class[]{org.lggi.samaanguru.entity.Event.class});
	}*/

  public PostPutCallbacks() {

  }

  @Override
  public void postStore(InstanceLifecycleEvent event) {
    xLogger.fine("Lifecycle : create for " +
        ((Persistable) event.getSource()).dnGetObjectId());
    IEvent eventEntity = (IEvent) event.getPersistentInstance();
    xLogger.fine("Entered PostPutCallbacks.notify");
    Boolean isRealTime = eventEntity.isRealTime();
    //Getting estimated time of action for scheduled tasks.
    long etaMillis = -1l;
    Object etaObject = eventEntity.getEtaMillis();
    if (etaObject != null) {
      etaMillis = (Long) etaObject;
    }
    try {
      if (isRealTime) {
        Map<String, String> params = new HashMap<String, String>();
        params.put("action", "notify");
        params.put("eventkey", String.valueOf(eventEntity.getKey()));
        params.put("frequency", String.valueOf(EventSpec.NotifyOptions.IMMEDIATE));
        params.put("method", MessageService.SMS);
        if (etaMillis == -1) {
          etaMillis = System.currentTimeMillis() + 10_000;
        }
        taskService.schedule(ITaskService.QUEUE_MESSAGE, NOTIFIER_URL, params, null,
            ITaskService.METHOD_POST, etaMillis, eventEntity.getDomainId(), null,
            "EVENT_NOTIFICATION");
      }
    } catch (Exception e) {
      xLogger.warn("{0} during PostPut notify for event key {1}: {2}", e.getClass().getName(),
          eventEntity.getKey(), e.getMessage());
    }
    xLogger.fine("Exiting PostPutCallbacks.notify");
  }

  @Override
  public void preStore(InstanceLifecycleEvent event) {
    // TODO Auto-generated method stub

  }

}
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
package com.logistimo.services.taskqueue;

import com.logistimo.services.taskqueue.util.TaskUtil;

import com.logistimo.exception.TaskSchedulingException;
import com.logistimo.logger.XLog;
import com.logistimo.services.utils.ConfigUtil;

import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextStoppedEvent;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.io.UnsupportedEncodingException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;


/**
 * Schedule tasks using that GAE Task Queue APIs. Each task is defined by a HTTP URL, the method and its paramters.
 *
 * @author arun
 */
public class TaskService extends AbstractTaskService implements
    ApplicationListener<ContextStoppedEvent> {

  // Logger
  private final XLog xLogger = XLog.getLog(TaskService.class);

  private ApplicationContext context;

  @Override
  public void initContext() {
    context = new ClassPathXmlApplicationContext(ConfigUtil.get("camel.file.name", "camel-tasks.xml"));
    xLogger.info("camel routes initialised");
  }

  @Override
  public ApplicationContext getContext() {
    return context;
  }

  @Override
  public void onApplicationEvent(ContextStoppedEvent event) {
    ((AbstractApplicationContext) context).close();
  }

  // Schedule to start at etaMillis (absolute time);

  /**
   *
   * @param queueName
   * @param url
   * @param params
   * @param multiValueParams
   * @param headers
   * @param methodType
   * @param etaMillis
   * @param domainId
   * @param userName
   * @param taskName
   * @return
   * @throws TaskSchedulingException
   */
  @Override
  public long schedule(String queueName, String url, Map<String, String> params,
                       List<String> multiValueParams, Map<String, String> headers, int methodType,
                       long etaMillis, long domainId, String userName, String taskName,
                       String jsonData)
      throws TaskSchedulingException {
    xLogger.fine("Entered schedule");

    if (url == null) {
      throw new TaskSchedulingException("Missing URL. URL is mandatory for scheduling tasks");
    }

    // Get the queue
    Queue q = null;
    if (queueName != null && !queueName.isEmpty()) {
      q = QueueFactory.getQueue(queueName);
    } else {
      q = QueueFactory.getDefaultQueue();
    }

    // Create the task option
    TaskOptions task = TaskOptions.Builder.withUrl(url).method(methodType);

    // Add headers, if present
    if (headers != null && !headers.isEmpty()) {
      Iterator<String> it = headers.keySet().iterator();
      while (it.hasNext()) {
        String key = it.next();
        task = task.header(key, headers.get(key));
      }
    }

    // Add URL parameters to the task
    if (params != null && !params.isEmpty()) {
      Set<String> keys = params.keySet();
      Iterator<String> keysIter = keys.iterator();
      while (keysIter.hasNext()) {
        String key = keysIter.next();
        if (multiValueParams != null && multiValueParams.contains(key)) {
          String valuesStr = params.get(key);
          String[] values = valuesStr.split(",");
          for (int i = 0; i < values.length; i++) {
            if (values[i] != null) {
              task = task.param(key, values[i]);
            }
          }
        } else {
          String value = params.get(key);
          if (value != null) {
            task = task.param(key, value);
          }
        }
      }
    }

    if (jsonData != null) {
      try {
        task.payload(jsonData.getBytes("UTF-8"), "application/json");
      } catch (UnsupportedEncodingException e) {
        throw new TaskSchedulingException(e);
      }
    }
    // Check if there is a specific time the task has to be scheduled
    if (etaMillis > 0) {
      task = task.etaMillis(etaMillis);
    }

    try {
      task.setTaskId(TaskUtil.getTaskLogger()
          .createTask(queueName, url, etaMillis, domainId, userName, taskName).getTaskId());
      return q.add(task).getEtaMillis();
    } catch (Exception e) {
      xLogger.warn("Failed to execute tasks {0}", e.getMessage(), e);
      throw new TaskSchedulingException(e.getMessage());
    }

  }

  public void scheduleNow(TaskOptions taskOptions) {
    Queue q = QueueFactory.getDefaultQueue();
    taskOptions.setEtaMillis(-1);
    q.add(taskOptions);
    xLogger.info("Triggering scheduled task for url: {0}", taskOptions.getUrl());
  }


}

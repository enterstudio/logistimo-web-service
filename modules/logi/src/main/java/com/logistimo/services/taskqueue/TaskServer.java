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

package com.logistimo.services.taskqueue;

import com.logistimo.AppFactory;
import com.logistimo.services.utils.ConfigUtil;

import com.logistimo.logger.XLog;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Created by charan on 17/11/14.
 */
public class TaskServer {

  private static final XLog _log = XLog.getLog(TaskServer.class);
  public static Map<String, TaskServer> _instances = new HashMap<>(2);
  TaskService taskService = (TaskService) AppFactory.get().getTaskService();
  int pickDuration = ConfigUtil.getInt("redis.task.pick.duration", 5000);
  private boolean shutdown;

  private TaskServer(final String domain) {
    Thread thread = new Thread() {
      public void run() {
        while (!shutdown) {
          try {
            Set<TaskOptions> tasks = DelayScheduler.getInstance().getTasksToRun(domain);
            for (TaskOptions task : tasks) {
              taskService.scheduleNow(task);
            }
          } catch (Exception e) {
            _log.severe("Exception occurred while fetching scheduled tasks to run", e);
          } finally {
            try {
              Thread.sleep(pickDuration);
            } catch (InterruptedException ignored) {
            }
          }
        }
      }
    };
    thread.start();
    _log.info("Started Task Consumer from JMS Queue");
  }

  public static TaskServer getInstance(String domain) {
    TaskServer instance = _instances.get(domain);
    if (instance == null) {
      synchronized (TaskServer.class) {
        instance = _instances.get(domain);
        if (instance == null) {
          instance = new TaskServer(domain);
          _instances.put(domain, instance);
        }
      }
    }
    return instance;
  }

  public static synchronized void close() {
    for (String xml : _instances.keySet()) {
      close(xml, false);
    }
  }

  public static void close(String domain) {
    close(domain, true);
  }

  public static synchronized void close(String domain, boolean removeFromList) {
    TaskServer instance = _instances.get(domain);
    if (instance != null) {
      instance.shutdown = true;
      if (removeFromList) {
        _instances.remove(domain);
      }
    }
  }
}

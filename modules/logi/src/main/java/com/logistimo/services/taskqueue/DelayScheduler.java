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

import com.google.gson.Gson;

import com.logistimo.AppFactory;
import com.logistimo.services.cache.RedisMemcacheService;
import com.logistimo.services.utils.ConfigUtil;

import com.logistimo.constants.Constants;
import com.logistimo.logger.XLog;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Created by charan on 24/09/15.
 */
public class DelayScheduler {

  private static final XLog xLog = XLog.getLog(DelayScheduler.class);
  private static DelayScheduler _instance;
  RedisMemcacheService redisMemcacheService;
  private String queueName;


  private DelayScheduler() {
    redisMemcacheService = (RedisMemcacheService) AppFactory.get().getMemcacheService();
    queueName = ConfigUtil.get("redis.tasks.queue.name", "tasks");
  }

  public static DelayScheduler getInstance() {
    if (_instance == null) {
      _instance = new DelayScheduler();
    }
    return _instance;
  }

  private String getQueuename(String domain) {
    return queueName + (Constants.DEFAULT.equals(domain) ? "" : "_" + domain);
  }

  public void schedule(TaskOptions taskOptions, String domain) {
    redisMemcacheService
        .zadd(getQueuename(domain), taskOptions.getEtaMillis(), new Gson().toJson(taskOptions));
  }

  public Set<TaskOptions> getTasksToRun(String domain) {
    Set<String>
        tasksJson =
        redisMemcacheService.getAndRemZRange(getQueuename(domain), System.currentTimeMillis());
    Set<TaskOptions> tasks = new LinkedHashSet<>(tasksJson.size());
    for (String task : tasksJson) {
      try {
        tasks.add(new Gson().fromJson(task, TaskOptions.class));
      } catch (Exception e) {
        xLog.severe("{0} while trying to convert json taskoptions to object: {1}", e.getMessage(),
            task);
      }
    }
    return tasks;
  }

  public void moveScheduledTasksToDefault(String domain) {
    redisMemcacheService.moveZRange(getQueuename(domain), getQueuename(Constants.DEFAULT));
  }

    /*public static void main(String[] args) {
        DelayScheduler scheduler = new DelayScheduler();
        int i = 0;
        do {
            System.out.println("***************Iteration "+ i);
            TaskOptions taskOptions = new TaskOptions("/testing"+i);
            taskOptions.setEtaMillis(5000);
            scheduler.schedule(taskOptions);
            Set<String> data = scheduler.redisMemcacheService.getAndRemZRange(scheduler.queueName, System.currentTimeMillis());
            for (String s : data) {
                System.out.println("Task: " + s);
            }
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        } while (i++ < 10);

    }*/

}

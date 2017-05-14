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

import com.logistimo.services.utils.ConfigUtil;

import com.logistimo.logger.XLog;

import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Created by charan on 24/09/14.
 */
public class SimpleQueue implements Queue {

  XLog log = XLog.getLog(SimpleQueue.class);

  //LinkedBlockingDeque<Runnable> runnableQueue = new LinkedBlockingDeque<Runnable>();

  ScheduledThreadPoolExecutor executorService = null;
  ScheduledThreadPoolExecutor scheduledExecutorService = null;

  private int maxActivePoolSize = ConfigUtil.getInt("task.queue.size", 10);
  private int waitTime = ConfigUtil.getInt("task.queue.waittime", 1000);
  private int maxRetryCount = ConfigUtil.getInt("task.queue.max.retry", 60);


  public SimpleQueue() {
    executorService = new ScheduledThreadPoolExecutor(ConfigUtil.getInt("task.pool.size", 10));
    scheduledExecutorService =
        new ScheduledThreadPoolExecutor(ConfigUtil.getInt("task.pool.sched.size", 1));
    Runtime.getRuntime().addShutdownHook(new Thread() {
      @Override
      public void run() {
        close();
      }
    });
  }

  @Override
  public Task add(TaskOptions taskOptions) {
    Task simpleTask = new SimpleTask(taskOptions);
    long etaMillis = taskOptions.getEtaMillis() - System.currentTimeMillis();
    int retryCount = maxRetryCount;

    if (etaMillis < 1000) {
      executorService.submit((Runnable) simpleTask);
    } else {
      scheduledExecutorService
          .schedule((Runnable) new ScheduledSimpleTask(taskOptions, this), etaMillis,
              TimeUnit.MILLISECONDS);
    }

    while (executorService.getActiveCount() >= maxActivePoolSize && retryCount-- > 0) {
      try {
        Thread.sleep(waitTime);
      } catch (InterruptedException e) {
      }
    }

    log.info("Added tasks {0} to Queue with etaMillis {1}. Active size is {2} and queue size {3}",
        simpleTask, etaMillis, executorService.getActiveCount(), executorService.getQueue().size());
    return simpleTask;
  }

  @Override
  public void startDefault() {
    log.warn("Ignore start default queue.. operating in local mode");
  }

  @Override
  public void stopDefault() {
    log.warn("Ignore stop default queue.. operating in local mode");
  }

  public void close() {
    log.info("Shutting down Local Job queue");
    executorService.shutdown();
    try {
      executorService.awaitTermination(20, TimeUnit.SECONDS);
    } catch (InterruptedException e) {
      log.warn("Termination not complete", e);
    }
    executorService.shutdownNow();
    scheduledExecutorService.shutdown();
    try {
      scheduledExecutorService.awaitTermination(20, TimeUnit.SECONDS);
    } catch (InterruptedException e) {
      log.warn("Termination not complete", e);
    }
    scheduledExecutorService.shutdownNow();
  }
}

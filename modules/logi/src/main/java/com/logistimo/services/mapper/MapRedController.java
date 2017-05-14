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

package com.logistimo.services.mapper;

import com.logistimo.logger.XLog;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * Created by charan on 29/09/14.
 */
public class MapRedController {
  private static XLog xLogger = XLog.getLog(MapRedController.class);

  private static LinkedBlockingQueue<Job> jobQueue = new LinkedBlockingQueue<Job>();

  /**
   * Do not remove this line. It is required to start job.
   */
  private static JobExecutorThread executor = new JobExecutorThread();

  public static String handleStart(String configName, Configuration configuration, String taskUrl) {
    Job job = new Job(configName, configuration, taskUrl);
    jobQueue.add(job);
    xLogger.info("Job queued for {0} with id {1}", configName, job.getId());
    return job.getId();
  }

  public static class JobExecutorThread implements Runnable {

    private boolean shutdown;

    public JobExecutorThread() {
      Thread start = new Thread(this);
      start.start();
    }

    @Override
    public void run() {
      Runtime.getRuntime().addShutdownHook(new Thread() {
        public void run() {
          shutdown = true;
        }
      });
      while (!shutdown) {
        Job job = null;
        try {
          job = jobQueue.poll(5, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
          e.printStackTrace();
        }
        if (job != null) {
          try {
            xLogger.info("Job started for {0} with id {1}", job.getConfigName(), job.getId());
            JobExecutor jobExecutor = new JobExecutor(job);
            jobExecutor.execute();
            xLogger.info("Job done for {0} with id {1}", job.getConfigName(), job.getId());
          } catch (Exception e) {
            xLogger
                .severe("Failed to process job {0} with id {1}", job.getConfigName(), job.getId(),
                    e);
          }
        }
      }
    }
  }
}

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

import com.logistimo.services.taskqueue.util.TaskUtil;

import com.logistimo.utils.HttpUtil;
import com.logistimo.logger.XLog;


/**
 * Created by charan on 24/09/14.
 */
public class SimpleTask implements Task, Runnable {

  private static final XLog logger = XLog.getLog(SimpleTask.class);
  private final TaskOptions taskOptions;
  private long etaMillis;

  public SimpleTask(TaskOptions taskOptions) {
    this.taskOptions = taskOptions;
    this.etaMillis = taskOptions.getEtaMillis();
  }

  @Override
  public long getEtaMillis() {
    return etaMillis;
  }

  @Override
  public void setEtaMillis(long etaMillis) {
    this.etaMillis = etaMillis;
  }

  @Override
  public void run() {
    logger.info("Starting task {0}", taskOptions);
    long startTime = System.currentTimeMillis();
    try {
      TaskUtil.getTaskLogger().moveToInProgress(taskOptions.getTaskId());
      if (taskOptions.getMethod() == ITaskService.METHOD_GET) {
        HttpUtil.getMulti(taskOptions.getUrl(), taskOptions.getParams(), taskOptions.getHeaders(),
            taskOptions.getPayload(), taskOptions.getContentType());
      } else {
        HttpUtil.postMulti(taskOptions.getUrl(), taskOptions.getParams(), taskOptions.getHeaders(),
            taskOptions.getPayload(), taskOptions.getContentType());
      }
      logger.info("Completed task {0}", taskOptions);
      TaskUtil.getTaskLogger()
          .complete(taskOptions.getTaskId(), System.currentTimeMillis() - startTime);
    } catch (Exception e) {
      logger.severe("Failed to execute task {0}", e.getMessage(), e);
    }
  }
}

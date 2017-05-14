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

package com.logistimo.services.taskqueue.util;

import com.logistimo.entity.Task;
import com.logistimo.services.taskqueue.Queue;
import com.logistimo.services.taskqueue.QueueFactory;
import com.logistimo.services.taskqueue.TaskOptions;

import com.logistimo.entity.ITask;
import com.logistimo.services.impl.PMF;
import com.logistimo.logger.XLog;

import java.util.Date;

import javax.jdo.PersistenceManager;

/**
 * Created by charan on 15/09/16.
 */
public class SQLTaskLogger implements ITaskLogger {

  private static final XLog _logger = XLog.getLog(SQLTaskLogger.class);

  @Override
  public ITask createTask(String queue, String url, long scheduledTime, long domainId,
                          String userName, String taskName) {
    PersistenceManager pmf = PMF.get().getPersistenceManager();
    ITask taskEntity;
    try {
      taskEntity = new Task();
      taskEntity.setCreatedBy(userName);
      taskEntity.setQueue(queue);
      if (taskName != null) {
        taskEntity.setName(taskName);
      } else {
        taskEntity.setName(url);
      }
      taskEntity.setdId(domainId);
      if (scheduledTime > 0) {
        taskEntity.setScheduleTime(new Date(scheduledTime));
      }
      taskEntity = pmf.makePersistent(taskEntity);
    } finally {
      pmf.close();
    }
    return taskEntity;
  }

  @Override
  public void moveToInProgress(Long taskId) {
    PersistenceManager pm = PMF.get().getPersistenceManager();
    try {
      ITask task = pm.getObjectById(Task.class, taskId);
      task.setStatus(Task.INPROGRESS);
      task.setUpdatedOn(new Date());
      pm.makePersistent(task);
    } finally {
      pm.close();
    }
  }

  @Override
  public void complete(Long taskId, long duration) {
    PersistenceManager pm = PMF.get().getPersistenceManager();
    try {
      ITask task = pm.getObjectById(Task.class, taskId);
      task.setStatus(Task.COMPLETED);
      task.setUpdatedOn(new Date());
      task.setDuration((int) (duration));
      pm.makePersistent(task);
    } finally {
      pm.close();
    }
  }

  @Override
  public void fail(TaskOptions taskOptions, long duration, String reason) {
    PersistenceManager pm = PMF.get().getPersistenceManager();
    try {
      ITask task = pm.getObjectById(Task.class, taskOptions.getTaskId());
      task.setStatus(Task.FAILED);
      task.setUpdatedOn(new Date());
      task.setDuration((int) duration);
      task.setReason(reason);
      task.setRetries(task.getRetries() + 1);
      task = pm.makePersistent(task);

      if (task.getRetries() < 3) {
        Queue q = null;
        String queueName = task.getQueue();
        if (queueName != null && !queueName.isEmpty()) {
          q = QueueFactory.getQueue(queueName);
        } else {
          q = QueueFactory.getDefaultQueue();
        }

        taskOptions.etaMillis(System.currentTimeMillis() + task.getRetries() * 30000);
        q.add(taskOptions);
        _logger.info("Rescheduling failed task {0} with interval {1}", taskOptions,
            taskOptions.getEtaMillis());
      }
    } finally {
      pm.close();
    }
  }
}

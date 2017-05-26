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

package com.logistimo.entity;

import java.util.Date;

/**
 * Created by charan on 20/05/15.
 */
public interface ITask {

  int QUEUED = 0;
  int INPROGRESS = 1;
  int COMPLETED = 2;
  int FAILED = 3;

  Long getTaskId();

  void setTaskId(Long taskId);

  String getName();

  void setName(String name);

  Date getCreatedOn();

  void setCreatedOn(Date createdOn);

  Date getUpdatedOn();

  void setUpdatedOn(Date updatedOn);

  String getCreatedBy();

  void setCreatedBy(String createdBy);

  int getStatus();

  void setStatus(int status);

  int getDuration();

  void setDuration(int duration);

  String getReason();

  void setReason(String reason);

  Long getdId();

  void setdId(Long dId);

  Date getScheduleTime();

  void setScheduleTime(Date scheduleTime);

  int getRetries();

  void setRetries(int retries);

  String getQueue();

  void setQueue(String queue);
}

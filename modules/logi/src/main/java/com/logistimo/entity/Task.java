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
package com.logistimo.entity;

import com.logistimo.entity.ITask;

import java.util.Date;

import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

/**
 * @author arun
 *
 *         Represents a domain
 */
@PersistenceCapable(identityType = IdentityType.APPLICATION, detachable = "true")
public class Task implements ITask {

  @PrimaryKey
  @Persistent(valueStrategy = IdGeneratorStrategy.IDENTITY)
  private Long taskId;
  @Persistent
  private Long dId;
  @Persistent
  private String name;
  @Persistent
  private Date createdOn;
  @Persistent
  private Date updatedOn;
  @Persistent
  private Date scheduleTime;
  @Persistent
  private String createdBy;
  @Persistent
  private int status;
  @Persistent
  private int retries;
  @Persistent
  private int duration;
  @Persistent
  private String reason;
  @Persistent
  private String queue;

  public Task() {
    initDates();
  }

  public Task(String name, String createdBy, Long domainId) {
    initDates();
    this.name = name;
    this.createdBy = createdBy;
    this.dId = domainId;

  }

  public Task(String name) {
    initDates();
    this.name = name;
  }

  private void initDates() {
    this.createdOn = new Date();
    this.scheduleTime = this.createdOn;
    this.updatedOn = this.createdOn;
  }

  @Override
  public Long getTaskId() {
    return taskId;
  }

  @Override
  public void setTaskId(Long taskId) {
    this.taskId = taskId;
  }

  @Override
  public String getName() {
    return name;
  }

  @Override
  public void setName(String name) {
    this.name = name;
  }

  @Override
  public Date getCreatedOn() {
    return createdOn;
  }

  @Override
  public void setCreatedOn(Date createdOn) {
    this.createdOn = createdOn;
  }

  @Override
  public Date getUpdatedOn() {
    return updatedOn;
  }

  @Override
  public void setUpdatedOn(Date updatedOn) {
    this.updatedOn = updatedOn;
  }

  @Override
  public String getCreatedBy() {
    return createdBy;
  }

  @Override
  public void setCreatedBy(String createdBy) {
    this.createdBy = createdBy;
  }

  @Override
  public int getStatus() {
    return status;
  }

  @Override
  public void setStatus(int status) {
    this.status = status;
  }

  @Override
  public int getDuration() {
    return duration;
  }

  @Override
  public void setDuration(int duration) {
    this.duration = duration;
  }

  @Override
  public String getReason() {
    return reason;
  }

  @Override
  public void setReason(String reason) {
    this.reason = reason;
  }

  @Override
  public Long getdId() {
    return dId;
  }

  @Override
  public void setdId(Long dId) {
    this.dId = dId;
  }

  @Override
  public Date getScheduleTime() {
    return scheduleTime;
  }

  @Override
  public void setScheduleTime(Date scheduleTime) {
    this.scheduleTime = scheduleTime;
  }

  @Override
  public int getRetries() {
    return retries;
  }

  @Override
  public void setRetries(int retries) {
    this.retries = retries;
  }

  @Override
  public String getQueue() {
    return queue;
  }

  @Override
  public void setQueue(String queue) {
    this.queue = queue;
  }
}

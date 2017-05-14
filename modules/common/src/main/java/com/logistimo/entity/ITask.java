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

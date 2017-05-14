package com.logistimo.entity;

import java.util.Date;

/**
 * Created by charan on 20/05/15.
 */
public interface ITaskLog {
  ITaskLog init(Long domainId, String key, Date time);

  String getKey();

  Long getDomainId();

  void setDomainId(Long domainId);

  Date getTimestamp();

  void setTimestamp(Date timestamp);
}

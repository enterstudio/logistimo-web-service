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

import com.logistimo.entity.ITaskLog;

import java.util.Date;

import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;
import javax.jdo.annotations.Version;
import javax.jdo.annotations.VersionStrategy;

/**
 * Tracks a running task; if an entry is present it means that the task, identify by ky, is running. This ensures idenmpotency of tasks, if used within tasks.
 * NOTE: This object is versioned by JDO to enable optimistic locking via JDO transactions as described in http://gae-java-persistence.blogspot.in/2009/10/optimistic-locking-with-version.html.
 *
 * @author Arun
 */
@PersistenceCapable(identityType = IdentityType.APPLICATION)
@Version(strategy = VersionStrategy.VERSION_NUMBER)
public class TaskLog implements ITaskLog {


  @PrimaryKey
  @Persistent
  private String
      ky;
  // Task Id (e.g. query task can have an ID digest(domainId+Query+QueryParams+cursor)) - this is a unique key that identifies the task
  @Persistent
  private Long dId;
  @Persistent
  private Date d; // timestamp

  public ITaskLog init(Long domainId, String key, Date time) {
    dId = domainId;
    ky = key;
    d = time;
    return this;
  }

  @Override
  public String getKey() {
    return ky;
  }

  @Override
  public Long getDomainId() {
    return dId;
  }

  @Override
  public void setDomainId(Long domainId) {
    dId = domainId;
  }

  @Override
  public Date getTimestamp() {
    return d;
  }

  @Override
  public void setTimestamp(Date timestamp) {
    d = timestamp;
  }
}

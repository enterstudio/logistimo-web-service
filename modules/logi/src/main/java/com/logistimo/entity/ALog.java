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

import com.logistimo.entity.IALog;

import java.util.Date;

import javax.jdo.annotations.Extension;
import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

/**
 * Represents a bulletin board log
 *
 * @author Arun
 */
@PersistenceCapable(identityType = IdentityType.APPLICATION, detachable = "true")
public class ALog implements IALog {

  @PrimaryKey
  @Persistent(valueStrategy = IdGeneratorStrategy.IDENTITY)
  private Long ky;
  @Persistent
  private Long dId;
  @Persistent
  private Date t; // timestamp
  @Persistent
  private String ty; // type of entry
  @Persistent
  @Extension(vendorName = "datanucleus", key = "gae.unindexed", value = "true")
  private String ip; // IP address
  @Persistent
  @Extension(vendorName = "datanucleus", key = "gae.unindexed", value = "true")
  private String ua; // user agent string

  public ALog() {
  }

  public ALog(Long domainId, String type, String ipAddress, String userAgent) {
    dId = domainId;
    ty = type;
    ip = ipAddress;
    ua = userAgent;
    t = new Date();
  }

  @Override
  public Long getKey() {
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
  public String getType() {
    return ty;
  }

  @Override
  public void setType(String type) {
    ty = type;
  }

  @Override
  public Date getTimestamp() {
    return t;
  }

  @Override
  public void setTimestamp(Date timestamp) {
    t = timestamp;
  }

  @Override
  public String getIPAddress() {
    return ip;
  }

  @Override
  public void setIPAddress(String ipAddress) {
    ip = ipAddress;
  }

  @Override
  public String getUserAgent() {
    return ua;
  }

  @Override
  public void setUserAgent(String userAgent) {
    ua = userAgent;
  }

  @Override
  public IALog init(Long domainId, String type, String ipAddress, String userAgent) {
    dId = domainId;
    ty = type;
    ip = ipAddress;
    ua = userAgent;
    t = new Date();
    return this;
  }
}

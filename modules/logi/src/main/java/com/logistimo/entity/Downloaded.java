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

import com.logistimo.entity.IDownloaded;

import java.util.Date;

import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

@PersistenceCapable(identityType = IdentityType.APPLICATION, detachable = "true")
public class Downloaded implements IDownloaded {
  @PrimaryKey
  @Persistent(valueStrategy = IdGeneratorStrategy.IDENTITY)
  private Long key;

  @Persistent
  private String uid; // id of the user downloading the file

  @Persistent
  private Date t; // time at which the file is downloaded

  @Persistent
  private String v; // version of the file that is downloaded

  @Persistent
  private String l; // locale

  @Persistent
  private String dd; // device details

  @Override
  public Long getKey() {
    return key;
  }

  @Override
  public void setKey(Long key) {
    this.key = key;
  }

  @Override
  public String getUserId() {
    return uid;
  }

  @Override
  public void setUserId(String userId) {
    uid = userId;
  }

  @Override
  public Date getTime() {
    return t;
  }

  @Override
  public void setTime(Date time) {
    t = time;
  }

  @Override
  public String getVersion() {
    return v;
  }

  @Override
  public void setVersion(String version) {
    v = version;
  }

  @Override
  public String getLocale() {
    return l;
  }

  @Override
  public void setLocale(String locale) {
    l = locale;
  }

  @Override
  public String getDeviceDetails() {
    return dd;
  }

  @Override
  public void setDeviceDetails(String deviceDetails) {
    dd = deviceDetails;
  }
}

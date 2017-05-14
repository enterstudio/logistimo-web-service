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

import com.logistimo.entity.IUploadedMsgLog;

import java.util.Date;

import javax.jdo.annotations.Column;
import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;


@PersistenceCapable(identityType = IdentityType.APPLICATION, detachable = "true")
public class UploadedMsgLog implements IUploadedMsgLog {
  @PrimaryKey
  @Persistent(valueStrategy = IdGeneratorStrategy.IDENTITY)
  private Long key;

  @Persistent
  private String uploadedId;
  @Persistent
  @Column(length = 2048)
  private String msg; // file name (has to be unique)
  @Persistent
  private Date t; // time at which the file is uploaded
  @Persistent
  private Long dId; // domain Id

  public Long getKey() {
    return key;
  }

  public void setKey(Long id) {
    this.key = id;
  }


  @Override
  public Date getTimeStamp() {
    return t;
  }

  @Override
  public void setTimeStamp(Date timeStamp) {
    t = timeStamp;
  }

  @Override
  public Long getDomainId() {
    return dId;
  }

  @Override
  public void setDomainId(Long dId) {
    this.dId = dId;
  }

  @Override
  public String getMessage() {

    return msg;
  }

  @Override
  public void setMessage(String msg) {
    this.msg = msg;
  }

  @Override
  public String getUploadedId() {
    return uploadedId;
  }

  @Override
  public void setUploadedId(String uploadedId) {
    this.uploadedId = uploadedId;
  }
}
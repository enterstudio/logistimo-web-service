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

import com.logistimo.entity.IMessageLog;

import java.util.Date;

import javax.jdo.annotations.Column;
import javax.jdo.annotations.Extension;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;


/**
 * A log of messages sent
 *
 * @author Arun
 */
@PersistenceCapable(identityType = IdentityType.APPLICATION, detachable = "true")
public class MessageLog implements IMessageLog {

  @PrimaryKey
  @Persistent
  private String key;
  @Persistent
  private String ty; // type (sms or email)
  @Persistent
  private String pid; // provider ID (e.g. SMSCountry)
  @Persistent
  private String
      adr;
  // address: mobile nos. or email addresses (typically, comma separated in case of multiple)
  @Persistent
  private String uId; // userId (to whom message was sent)
  @Persistent
  private String suId; // userId of user who sent this message
  @Persistent
  @Column(length = 2048)
  private String msg; // message
  @Persistent
  private String st; // status (if separate from response above)
  @Persistent
  private Date t; // timestamp (timestamp of sending)
  @Persistent
  private Date dt; // done time (timestamp of status update, if different)
  @Persistent
  private Long dId; // domainId
  @Extension(vendorName = "datanucleus", key = "gae.unindexed", value = "true")
  private Integer ast; // aggregation status

  @Persistent
  private Integer notif;
  @Persistent
  private String eventType;


  @Override
  public String getKey() {
    return key;
  }

  @Override
  public void setKey(String key) {
    this.key = key;
  }

  @Override
  public String getType() {
    return ty;
  }

  @Override
  public void setType(String ty) {
    this.ty = ty;
  }

  @Override
  public String getProviderId() {
    return pid;
  }

  @Override
  public void setProviderId(String pid) {
    this.pid = pid;
  }

  @Override
  public String getAddress() {
    return adr;
  }

  @Override
  public void setAddress(String adr) {
    this.adr = adr;
  }

  @Override
  public String getUserId() {
    return uId;
  }

  @Override
  public void setUserId(String uId) {
    this.uId = uId;
  }

  @Override
  public String getSenderId() {
    return suId;
  }

  @Override
  public void setSenderId(String suId) {
    this.suId = suId;
  }

  @Override
  public String getMessage() {
    return msg;
  }

  @Override
  public void setMessage(String message) {
    this.msg = message;
  }

  @Override
  public String getStatus() {
    return st;
  }

  @Override
  public void setStatus(String st) {
    this.st = st;
  }

  @Override
  public Date getTimestamp() {
    return t;
  }

  @Override
  public void setTimestamp(Date t) {
    this.t = t;
  }

  @Override
  public Date getDoneTime() {
    return dt;
  }

  @Override
  public void setDoneTime(Date dt) {
    this.dt = dt;
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
  public Integer getNotif() {
    return notif;
  }

  @Override
  public void setNotif(Integer notif) {
    this.notif = notif;
  }

  @Override
  public String getEventType() {
    return eventType;
  }

  @Override
  public void setEventType(String eventType) {
    this.eventType = eventType;
  }


}
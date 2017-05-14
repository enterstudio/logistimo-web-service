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

package com.logistimo.activity.entity;

import java.util.Date;

import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

/**
 * Created by kumargaurav on 07/10/16.
 */

@PersistenceCapable(identityType = IdentityType.APPLICATION, detachable = "true")
public class Activity implements IActivity {

  @PrimaryKey
  @Persistent(customValueStrategy = "uuid")
  private String id;

  @Persistent
  private String objectId;

  @Persistent
  private String objectType;

  @Persistent
  private Long domainId;

  @Persistent
  private String userId;

  @Persistent
  private Date createDate;

  @Persistent
  private String field;

  @Persistent
  private String action;

  @Persistent
  private String prevValue;

  @Persistent
  private String newValue;

  @Persistent
  private String messageId;

  @Persistent
  private String tag;

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getObjectId() {
    return objectId;
  }

  public void setObjectId(String objectId) {
    this.objectId = objectId;
  }

  public String getObjectType() {
    return objectType;
  }

  public void setObjectType(String objectType) {
    this.objectType = objectType;
  }

  public Long getDomainId() {
    return domainId;
  }

  public void setDomainId(Long domainId) {
    this.domainId = domainId;
  }

  public String getUserId() {
    return userId;
  }

  public void setUserId(String userId) {
    this.userId = userId;
  }

  public Date getCreateDate() {
    return createDate;
  }

  public void setCreateDate(Date createDate) {
    this.createDate = createDate;
  }

  public String getField() {
    return field;
  }

  public void setField(String field) {
    this.field = field;
  }

  public String getAction() {
    return action;
  }

  public void setAction(String action) {
    this.action = action;
  }

  public String getPrevValue() {
    return prevValue;
  }

  public void setPrevValue(String prevValue) {
    this.prevValue = prevValue;
  }

  public String getNewValue() {
    return newValue;
  }

  public void setNewValue(String newValue) {
    this.newValue = newValue;
  }

  public String getMessageId() {
    return messageId;
  }

  public void setMessageId(String messageId) {
    this.messageId = messageId;
  }

  public String getTag() {
    return tag;
  }

  public void setTag(String tag) {
    this.tag = tag;
  }

  @Override
  public String toString() {
    return "Activity{" +
        "objectId=" + objectId +
        ", objectType='" + objectType + '\'' +
        ", domainId=" + domainId +
        ", userId='" + userId + '\'' +
        ", createDate=" + createDate +
        ", field='" + field + '\'' +
        ", action='" + action + '\'' +
        ", prevValue='" + prevValue + '\'' +
        ", newValue='" + newValue + '\'' +
        ", messageId='" + messageId + '\'' +
        ", tag='" + tag + '\'' +
        '}';
  }
}


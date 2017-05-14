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

package com.logistimo.conversations.entity;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.NotPersistent;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

/**
 * Created by kumargaurav on 04/10/16.
 */

@PersistenceCapable(identityType = IdentityType.APPLICATION, detachable = "true")
public class Conversation implements IConversation {


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
  private Date updateDate;

  @Persistent(defaultFetchGroup = "true", mappedBy = "conversation", dependentElement = "true")
  private Set<ConversationTag> conversationTags;

  @NotPersistent
  private Set<String> tags;

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

  public Date getUpdateDate() {
    return updateDate;
  }

  public void setUpdateDate(Date updateDate) {
    this.updateDate = updateDate;
  }

  public Set<? extends IConversationTag> getConversationTags() {
    return conversationTags;
  }

  public void setConversationTags(Set<? extends IConversationTag> conversationTags) {

    if (this.conversationTags == null) {
      this.conversationTags = new HashSet<>(1);
    } else {
      this.conversationTags.clear();
    }
    for (IConversationTag iConversationTag : conversationTags) {
      this.conversationTags.add((ConversationTag) iConversationTag);
    }
  }

  @Override
  public Set<String> getTags() {
    return tags;
  }

  @Override
  public void setTags(Set<String> tags) {
    if (this.tags == null) {
      this.tags = new HashSet<>(1);
    } else {
      this.tags.clear();
    }
    this.tags.addAll(tags);
  }

  @Override
  public String toString() {
    return "Conversation{" +
        "objectId=" + objectId +
        ", objectType='" + objectType + '\'' +
        ", domainId=" + domainId +
        ", userId='" + userId + '\'' +
        ", tags=" + tags +
        '}';
  }
}

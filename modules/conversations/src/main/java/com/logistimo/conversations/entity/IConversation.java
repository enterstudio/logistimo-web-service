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
import java.util.Set;

/**
 * Created by kumargaurav on 04/10/16.
 */
public interface IConversation {

  String getId();

  void setId(String id);

  String getObjectId();

  void setObjectId(String objectId);

  String getObjectType();

  void setObjectType(String objectType);

  Long getDomainId();

  void setDomainId(Long domainId);

  String getUserId();

  void setUserId(String userId);

  Date getCreateDate();

  void setCreateDate(Date createDate);

  Date getUpdateDate();

  void setUpdateDate(Date updateDate);

  Set<String> getTags();

  void setTags(Set<String> tags);

  Set<? extends IConversationTag> getConversationTags();

  void setConversationTags(Set<? extends IConversationTag> conversationTags);
}

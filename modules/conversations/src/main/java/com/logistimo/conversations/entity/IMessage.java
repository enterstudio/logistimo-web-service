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

/**
 * Created by kumargaurav on 04/10/16.
 */
public interface IMessage {

  public String getMessageId();

  public void setMessageId(String messageId);

  public String getConversationId();

  public void setConversationId(String conversationId);

  public String getMessage();

  public void setMessage(String message);

  public Date getCreateDate();

  public void setCreateDate(Date createDate);

  public Date getUpdateDate();

  public void setUpdateDate(Date updateDate);

  public String getUserId();

  public void setUserId(String userId);
}

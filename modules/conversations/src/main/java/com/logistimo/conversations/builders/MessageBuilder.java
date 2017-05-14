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

package com.logistimo.conversations.builders;

import com.logistimo.conversations.models.MessageModel;
import com.logistimo.conversations.service.ConversationService;
import com.logistimo.conversations.service.impl.ConversationServiceImpl;
import com.logistimo.dao.JDOUtils;

import com.logistimo.conversations.entity.IMessage;
import com.logistimo.services.ServiceException;
import com.logistimo.services.Services;
import com.logistimo.constants.Constants;
import com.logistimo.utils.LocalDateUtil;
import com.logistimo.users.service.UsersService;
import com.logistimo.users.service.impl.UsersServiceImpl;

import java.util.Date;

/**
 * Created by kumargaurav on 05/10/16.
 */
public class MessageBuilder {

  public IMessage buildMessage(MessageModel model, String userId, boolean isCreate)
      throws ServiceException {
    IMessage message = null;
    if (isCreate) {
      message = JDOUtils.createInstance(IMessage.class);
    } else {
      ConversationService service = Services.getService(ConversationServiceImpl.class);
      message = service.getMessageById(model.messageId);
    }
    return buildMessage(message, model, userId, isCreate);
  }

  public IMessage buildMessage(IMessage message, MessageModel model, String userId,
                               boolean isCreate)
      throws ServiceException {

    message.setConversationId(model.conversationId);
    message.setMessage(model.message);
    message.setUserId(userId);
    Date now = new Date();
    if (isCreate) {
      message.setCreateDate(now);
    } else {
      message.setUpdateDate(now);
    }
    return message;
  }

  public MessageModel buildModel(IMessage message) {
    if (message == null) {
      return null;
    }
    MessageModel model = new MessageModel();
    model.messageId = message.getMessageId();
    model.message = message.getMessage();
    model.conversationId = message.getConversationId();
    model.userId = message.getUserId();
    try {
      UsersService as = Services.getService(UsersServiceImpl.class);
      model.userName = as.getUserAccount(model.userId).getFullName();
    } catch (Exception ignored) {
      //Ignore exception
    }
    if (null != message.getCreateDate()) {
      model.createDate =
          LocalDateUtil.formatCustom(message.getCreateDate(), Constants.DATETIME_FORMAT, null);
      model.cts = message.getCreateDate().getTime();
    }
    if (null != message.getUpdateDate()) {
      model.updateDate =
          LocalDateUtil.formatCustom(message.getUpdateDate(), Constants.DATETIME_FORMAT, null);
    }
    return model;
  }
}

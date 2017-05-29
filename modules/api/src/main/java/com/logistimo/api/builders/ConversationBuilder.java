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

package com.logistimo.api.builders;

import com.logistimo.conversations.entity.IConversation;
import com.logistimo.conversations.service.ConversationService;
import com.logistimo.conversations.service.impl.ConversationServiceImpl;
import com.logistimo.dao.JDOUtils;

import com.logistimo.security.SecureUserDetails;
import com.logistimo.services.ServiceException;
import com.logistimo.services.Services;
import com.logistimo.utils.LocalDateUtil;
import com.logistimo.api.models.ConversationModel;

import java.util.Date;

/**
 * Created by kumargaurav on 04/10/16.
 */
public class ConversationBuilder {


  public IConversation buildConversation(ConversationModel model, String userId, boolean isCreate)
      throws ServiceException {

    IConversation conversation = null;
    if (isCreate) {
      conversation = JDOUtils.createInstance(IConversation.class);
    } else {
      ConversationService service = Services.getService(ConversationServiceImpl.class);
      conversation = service.getConversationById(model.id);
    }
    return buildConversation(conversation, model, userId, isCreate);
  }


  public IConversation buildConversation(IConversation conversation, ConversationModel model,
                                         String userId, boolean isCreate) {

    conversation.setObjectId(model.objectId);
    conversation.setObjectType(model.objectType);
    conversation.setDomainId(model.domainId);
    conversation.setUserId(model.userId);
    Date now = new Date();
    if (isCreate) {
      conversation.setCreateDate(now);
    } else {
      conversation.setUpdateDate(now);
    }
    //adding conversation tags
    if (null != model.tags && !model.tags.isEmpty()) {
      conversation.setTags(model.tags);
    }
    return conversation;
  }

  public ConversationModel buildModel(IConversation conversation, SecureUserDetails user) {

    ConversationModel model = new ConversationModel();
    model.id = conversation.getId();
    model.domainId = conversation.getDomainId();
    model.objectId = conversation.getObjectId();
    model.objectType = conversation.getObjectType();
    model.userId = conversation.getUserId();
    if (null != conversation.getCreateDate()) {
      model.createDate =
          LocalDateUtil.format(conversation.getCreateDate(), user.getLocale(), user.getTimezone());
    }
    if (null != conversation.getUpdateDate()) {
      model.updateDate =
          LocalDateUtil.format(conversation.getUpdateDate(), user.getLocale(), user.getTimezone());
    }
    return model;
  }

}


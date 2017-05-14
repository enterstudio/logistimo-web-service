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


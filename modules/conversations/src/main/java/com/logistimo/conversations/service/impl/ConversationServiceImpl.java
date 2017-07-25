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

package com.logistimo.conversations.service.impl;

import com.logistimo.constants.CharacterConstants;
import com.logistimo.conversations.dao.IMessageDao;
import com.logistimo.conversations.dao.impl.MessageDao;
import com.logistimo.conversations.entity.IConversation;
import com.logistimo.conversations.entity.IConversationTag;
import com.logistimo.conversations.entity.IMessage;
import com.logistimo.conversations.service.ConversationService;
import com.logistimo.dao.JDOUtils;
import com.logistimo.logger.XLog;
import com.logistimo.pagination.PageParams;
import com.logistimo.pagination.Results;
import com.logistimo.services.Service;
import com.logistimo.services.ServiceException;
import com.logistimo.services.Services;
import com.logistimo.services.impl.PMF;
import com.logistimo.services.impl.ServiceImpl;

import org.apache.commons.lang.StringUtils;

import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.jdo.PersistenceManager;
import javax.jdo.Query;
import javax.jdo.Transaction;

/**
 * Created by kumargaurav on 04/10/16.
 */
public class ConversationServiceImpl extends ServiceImpl implements ConversationService {

  private static final XLog xLogger = XLog.getLog(ConversationServiceImpl.class);
  public static String ObjectTypeShipment = "SHIPMENT";
  private IMessageDao messageDao = new MessageDao();

  @Override
  public void init(Services services) throws ServiceException {

  }

  @Override
  public void destroy() throws ServiceException {

  }

  @Override
  public Class<? extends Service> getInterface() {
    return ConversationService.class;
  }

  @Override
  public IConversation addEditConversation(IConversation conversation, boolean isCreate)
      throws ServiceException {
    PersistenceManager pm = null;
    try {
      pm = PMF.get().getPersistenceManager();
      return addEditConversation(conversation, isCreate, pm);
    } finally {
      if (pm != null) {
        pm.close();
      }
    }
  }


  public IConversation addEditConversation(IConversation conversation, boolean isCreate,
                                           PersistenceManager pm) throws ServiceException {

    Set<IConversationTag> ctags = null;
    try {

      if (null != conversation.getTags() && !conversation.getTags().isEmpty()) {
        ctags = constructConversationTags(conversation.getTags(), conversation);
        conversation.setConversationTags(ctags);
      }
      pm.makePersistent(conversation);
      conversation = pm.detachCopy(conversation);
    } catch (Exception e) {
      xLogger.severe("{0} while creating conversation {1}", e.getMessage(), conversation, e);
      throw new ServiceException(e);
    }
    return conversation;
  }

  private Set<IConversationTag> constructConversationTags(Set<String> tags,
                                                          IConversation conversation) {
    Set<IConversationTag> tagSet = new HashSet<>();
    IConversationTag conversationTag = null;
    for (String tag : tags) {
      conversationTag = JDOUtils.createInstance(IConversationTag.class);
      conversationTag.setConversation(conversation);
      conversationTag.setTag(tag);
      tagSet.add(conversationTag);
    }
    return tagSet;
  }

  public IConversation getConversationById(String convId) throws ServiceException {
    IConversation conversation = null;
    PersistenceManager pm = null;
    try {
      pm = PMF.get().getPersistenceManager();
      Query query = pm.newQuery(JDOUtils.getImplClass(IConversation.class));
      query.setFilter("id == :convIdParam");
      Map<String, String> paramValues = new HashMap<>(1);
      paramValues.put("convIdParam", convId);
      query.setUnique(true);
      conversation = (IConversation) query.executeWithMap(paramValues);
      conversation = pm.detachCopy(conversation);
    } catch (Exception e) {
      xLogger.severe("{0} while creating getting conversation {1}", e.getMessage(), convId, e);
      throw new ServiceException(e);
    } finally {
      if (pm != null) {
        pm.close();
      }
    }
    return conversation;
  }

  public IMessage getMessageById(String messageId) throws ServiceException {
    return messageDao.getMessageById(messageId);
  }

  public IMessage getLastMessage(String convId, String objectType, String objectId){
    return messageDao.getLastMessage(convId,objectType,objectId);
  }

  public IMessage addEditMessage(IMessage message, boolean isCreate) throws ServiceException {
    PersistenceManager pm = null;
    try {
      pm = PMF.get().getPersistenceManager();
      pm.makePersistent(message);
      message = pm.detachCopy(message);
    } catch (Exception e) {
      xLogger.severe("{0} while creating message {1}", e.getMessage(), message, e);
      throw new ServiceException(e);
    } finally {
      if (pm != null) {
        pm.close();
      }
    }
    return message;
  }

  public Results getMessages(String convId, String objectType, String objectId,
                             PageParams pageParams) {
    Results results = messageDao.getMessages(convId, objectType, objectId, pageParams);
    xLogger.fine("Returning messages for conversation id {}", convId);
    return results;
  }

  public Results getMessagesCount(String convId, String objectType, String objectId,
                                  PageParams pageParams) {
    Results results = messageDao.getMessagesCount(convId, objectType, objectId, pageParams);
    xLogger.fine("Returning messages for conversation id {}", convId);
    return results;
  }

  public Results getMessagesByTags(String tags, PageParams pageParams) {
    String[] tagarr = tags.split(CharacterConstants.COMMA);
    Results results = messageDao.getMessagesByTags(Arrays.asList(tagarr), pageParams);
    xLogger.fine("Returning messages for tags {}", tags);
    return results;
  }

  @Override
  public IMessage addMsgToConversation(String objectType, String objectId, String message,
                                       String updatingUserId,
                                       Set<String> tags, Long domainId, PersistenceManager pm)
      throws ServiceException {
    return addMsgToConversation(objectType, objectId, message, updatingUserId, tags, domainId, null,
        pm);
  }

  @Override
  public IMessage addMsgToConversation(String objectType, String objectId, String message,
                                       String updatingUserId,
                                       Set<String> tags, Long domainId, Date date,
                                       PersistenceManager pm) throws ServiceException {
    PersistenceManager localPm = pm;
    boolean useLocalPM = pm == null;

    String conversationId = null;
    Transaction tx = null;
    IMessage iMessage = null;
    Date cdate = date;
    if (null == cdate) {
      cdate = new Date();
    }
    try {
      if (useLocalPM) {
        localPm = PMF.get().getPersistenceManager();
        tx = localPm.currentTransaction();
        tx.begin();
      }
      conversationId = messageDao.getConversationId(objectType, objectId, localPm);
      if (conversationId == null) {
        conversationId =
            addEditConversation(objectType, objectId, tags, domainId, cdate, localPm,
                updatingUserId).getId();
      }
      if (conversationId != null) {
        iMessage = JDOUtils.createInstance(IMessage.class);
        if (!StringUtils.isBlank(message)) {
          iMessage.setConversationId(conversationId);
          iMessage.setCreateDate(cdate);
          iMessage.setMessage(message);
          iMessage.setUserId(updatingUserId);
          localPm.makePersistent(iMessage);
          iMessage = localPm.detachCopy(iMessage);
        }
        iMessage.setConversationId(conversationId);
      }
      if (tx != null) {
        tx.commit();
      }
    } catch (ServiceException e) {
      xLogger.severe("Error while creating conversation ", e);
      throw e;
    } finally {
      if (useLocalPM) {
        if (tx != null && tx.isActive()) {
          tx.rollback();
        }
        localPm.close();
      }
    }

    if (conversationId == null) {
      xLogger.severe("Failed to create conversation for the message {0}:{1}:{2}:{3}", objectType,
          objectId, message, updatingUserId);
      throw new ServiceException("Failed to create conversation for the message");

    }
    return iMessage;
  }



  private IConversation addEditConversation(String objectType, String objectId, Set<String> tags,
                                            Long domainId,
                                            PersistenceManager pm) throws ServiceException {
    return addEditConversation(objectType, objectId, tags, domainId, null, pm);
  }

  private IConversation addEditConversation(String objectType, String objectId, Set<String> tags,
                                            Long domainId, Date date,
                                            PersistenceManager pm) throws ServiceException {
    return addEditConversation(objectType, objectId, tags, domainId, date, pm, null);
  }

  private IConversation addEditConversation(String objectType, String objectId, Set<String> tags,
                                            Long domainId, Date date,
                                            PersistenceManager pm, String userId)
      throws ServiceException {
    Date cdate = date;
    if (date == null) {
      date = new Date();
    }
    IConversation conversation = JDOUtils.createInstance(IConversation.class);
    conversation.setObjectId(objectId);
    conversation.setObjectType(objectType);
    conversation.setDomainId(domainId);
    conversation.setTags(tags);
    conversation.setCreateDate(date);
    conversation.setUserId(userId);
    return addEditConversation(conversation, true, pm);
  }
}

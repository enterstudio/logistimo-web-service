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

package com.logistimo.conversations.service;

import com.logistimo.conversations.entity.IConversation;
import com.logistimo.conversations.entity.IMessage;
import com.logistimo.pagination.PageParams;
import com.logistimo.pagination.Results;
import com.logistimo.services.Service;
import com.logistimo.services.ServiceException;

import java.util.Date;
import java.util.Set;

import javax.jdo.PersistenceManager;

/**
 * Created by kumargaurav on 04/10/16.
 */
public interface ConversationService extends Service {

  /**
   * creates a converstation with given object
   */
  IConversation addEditConversation(IConversation conversation, boolean isCreate)
      throws ServiceException;

  IConversation getConversationById(String convId) throws ServiceException;

  IMessage getMessageById(String messageId) throws ServiceException;

  /**
   * @param convId conversationId, this alone is enough to get the Last message, if not Null
   * @param objectType object type, e.g. "ORDER" or "SHIPMENT"
   * @param objectId orderId or shipmentId
   * @return returns the last message instance of the conversation
   */
  IMessage getLastMessage(String convId, String objectType, String objectId);

  IMessage addEditMessage(IMessage message, boolean isCreate) throws ServiceException;

  Results getMessages(String convId, String objType, String objId, PageParams pageParams);

  Results getMessagesCount(String convId, String objType, String objId, PageParams pageParams);

  Results getMessagesByTags(String tags, PageParams pageParams);

  IMessage addMsgToConversation(String objectType, String objectId, String message,
                                String updatingUserId,
                                Set<String> tags, Long domainId, PersistenceManager pm)
      throws ServiceException;

  IMessage addMsgToConversation(String objectType, String objectId, String message,
                                String updatingUserId,
                                Set<String> tags, Long domainId, Date date, PersistenceManager pm)
      throws ServiceException;
}

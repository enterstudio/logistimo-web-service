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

package com.logistimo.conversations.dao.impl;

import com.logistimo.conversations.builders.MessageBuilder;
import com.logistimo.conversations.entity.Conversation;
import com.logistimo.conversations.entity.Message;
import com.logistimo.conversations.models.MessageModel;
import com.logistimo.conversations.dao.IMessageDao;
import com.logistimo.dao.JDOUtils;

import org.apache.commons.lang.StringUtils;
import com.logistimo.conversations.entity.IMessage;
import com.logistimo.pagination.PageParams;
import com.logistimo.pagination.Results;
import com.logistimo.services.ServiceException;
import com.logistimo.services.impl.PMF;
import com.logistimo.constants.CharacterConstants;
import com.logistimo.constants.QueryConstants;
import com.logistimo.logger.XLog;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.jdo.PersistenceManager;
import javax.jdo.Query;

/**
 * Created by kumargaurav on 05/10/16.
 */
public class MessageDao implements IMessageDao {

  private static final XLog xLogger = XLog.getLog(MessageDao.class);

  private MessageBuilder msgBuilder = new MessageBuilder();

  public IMessage getMessageById(String messageId) throws ServiceException {

    IMessage message = null;
    PersistenceManager pm = null;
    Query query = null;
    try {
      pm = PMF.get().getPersistenceManager();
      query = pm.newQuery(JDOUtils.getImplClass(IMessage.class));
      query.setFilter("messageId == :messageIdParam");
      Map<String, String> paramValues = new HashMap<>();
      paramValues.put("messageIdParam", messageId);
      query.setUnique(true);
      message = (IMessage) query.executeWithMap(paramValues);
      message = pm.detachCopy(message);
    } catch (Exception e) {
      xLogger.severe("{0} while getting message {1}", e.getMessage(), messageId, e);
      throw new ServiceException(e);
    } finally {
      if (query != null) {
        try {
          query.closeAll();
        } catch (Exception ignored) {
          xLogger.warn("Exception while closing query", ignored);
        }
      }
      if (pm != null) {
        pm.close();
      }
    }
    return message;
  }

  public IMessage getLastMessage(String convId, String objectType, String objectId){
    PersistenceManager pm = null;
    Query query = null;
    IMessage message = null;

    List<String> queryParamArray = new ArrayList<>();
    StringBuilder queryString = new StringBuilder("SELECT * FROM MESSAGE WHERE ");
    if (StringUtils.isNotBlank(convId)) {
      queryString.append("CONVERSATIONID = ").append(CharacterConstants.QUESTION);
      queryParamArray.add(convId);
    } else if (StringUtils.isNotBlank(objectType) && StringUtils.isNotBlank(objectId)) {
      queryString.append("CONVERSATIONID IN ( SELECT ID FROM CONVERSATION WHERE ")
              .append("OBJECTID = ").append(CharacterConstants.QUESTION)
              .append(" AND OBJECTTYPE = ").append(CharacterConstants.QUESTION)
              .append(" ) ");
      queryParamArray.add(objectId);
      queryParamArray.add(objectType);
    } else {
      throw new IllegalArgumentException(
              "One of conversation Id, and Object type/id combination is mandatory");
    }
    final String orderBy = " ORDER BY CREATEDATE DESC";
    queryString.append(orderBy);
    String limitStr = " LIMIT 1";
    queryString.append(limitStr);
    try {
      pm = PMF.get().getPersistenceManager();
      query = pm.newQuery("javax.jdo.query.SQL", queryString.toString());
      query.setClass(Message.class);

      List<Message> msgList = null;
      msgList = (List<Message>) query.executeWithArray(queryParamArray.toArray());
      msgList = (List<Message>) pm.detachCopyAll(msgList);
      if (msgList.size() > 0)
        message = msgList.get(0);
    } catch (Exception e){
      xLogger.severe("Exception while querying last message of conversation", e);
    } finally {
      if (query != null) {
        try {
          query.closeAll();
        } catch (Exception ignored) {
          xLogger.warn("Exception while closing query", ignored);
        }
      }
      if (pm != null) {
        pm.close();
      }
    }
    return message;
  }

  public Results getMessages(String convId, String objectType, String objectId,
                             PageParams pageParams) {
    return getMessages(convId, objectType, objectId, pageParams, false);
  }

  public Results getMessagesCount(String convId, String objectType, String objectId,
                                  PageParams pageParams) {
    return getMessages(convId, objectType, objectId, pageParams, true);
  }

  private Results getMessages(String convId, String objectType, String objectId,
                              PageParams pageParams, boolean isOnlyCount) {
    Results res = null;
    PersistenceManager pm = null;
    Query query = null;
    Query cntQuery = null;
    List<String> queryParamArray = new ArrayList<>();

    StringBuilder queryString = new StringBuilder("SELECT * FROM MESSAGE WHERE ");
    if (StringUtils.isNotBlank(convId)) {
      queryString.append("CONVERSATIONID = ").append(CharacterConstants.QUESTION);
      queryParamArray.add(convId);
    } else if (StringUtils.isNotBlank(objectType) && StringUtils.isNotBlank(objectId)) {
      queryString.append("CONVERSATIONID IN ( SELECT ID FROM CONVERSATION WHERE ")
          .append("OBJECTID = ").append(CharacterConstants.QUESTION)
          .append(" AND OBJECTTYPE = ").append(CharacterConstants.QUESTION)
          .append(" ) ");
      queryParamArray.add(objectId);
      queryParamArray.add(objectType);
    } else {
      throw new IllegalArgumentException(
          "One of conversation Id, and Object type/id combination is mandatory");
    }

    final String orderBy = " ORDER BY CREATEDATE DESC";
    queryString.append(orderBy);
    String
        limitStr =
        " LIMIT " + pageParams.getOffset() + CharacterConstants.COMMA + pageParams.getSize();
    queryString.append(limitStr);

    try {
      pm = PMF.get().getPersistenceManager();
      query = pm.newQuery("javax.jdo.query.SQL", queryString.toString());
      query.setClass(Message.class);

      List<Message> msgList = null;
      if (!isOnlyCount) {
        msgList = (List<Message>) query.executeWithArray(queryParamArray.toArray());
        msgList = (List<Message>) pm.detachCopyAll(msgList);
      }
      String
          cntQueryStr =
          queryString.toString().replace("*", QueryConstants.ROW_COUNT)
              .replace(orderBy, CharacterConstants.EMPTY);
      cntQueryStr = cntQueryStr.replace(limitStr, CharacterConstants.EMPTY);

      cntQuery = pm.newQuery("javax.jdo.query.SQL", cntQueryStr);
      int
          count =
          ((Long) ((List) cntQuery.executeWithArray(queryParamArray.toArray())).iterator().next())
              .intValue();

      List<MessageModel> messageModelList = null;
      if (null != msgList) {
        messageModelList = new ArrayList<>();
        for (IMessage message : msgList) {
          messageModelList.add(msgBuilder.buildModel(message));
        }
      }
      res = new Results(messageModelList, null, count, pageParams.getOffset());
    } finally {
      if (query != null) {
        try {
          query.closeAll();
        } catch (Exception ignored) {
          xLogger.warn("Exception while closing query", ignored);
        }
      }
      if (cntQuery != null) {
        try {
          cntQuery.closeAll();
        } catch (Exception ignored) {
          xLogger.warn("Exception while closing query", ignored);
        }
      }
      if (pm != null) {
        pm.close();
      }
    }

    return res;
  }

  public Results getMessagesByTags(List<String> tags, PageParams pageParams) {

    PersistenceManager pm = null;
    Query query = null;
    Query msgQuery = null;
    Query cntQuery = null;
    List<String> convids = null;
    List<String> paramArray = new ArrayList<>();
    List<String> paramArray1 = new ArrayList<>();
    Results res = null;

    StringBuilder
        conquery =
        new StringBuilder("SELECT CONVERSATION_ID_OID FROM CONVERSATIONTAG where TAG IN "
            + CharacterConstants.O_BRACKET);
    for (String tag : tags) {
      conquery.append(CharacterConstants.QUESTION).append(CharacterConstants.COMMA);
      paramArray.add(tag);
    }
    conquery.setLength(conquery.length() - 1);
    conquery.append(CharacterConstants.C_BRACKET);
    try {
      pm = PMF.get().getPersistenceManager();
      query = pm.newQuery("javax.jdo.query.SQL", conquery.toString());
      List l = (List) query.executeWithArray(paramArray.toArray());
      //handling the case when there is no valid tags
      if (null == l || l.isEmpty()) {
        return new Results(new ArrayList(), null, 0, pageParams.getOffset());
      }
      convids = new ArrayList<>(l.size());
      for (Object o : l) {
        convids.add((String) o);
      }

      StringBuilder queryString = new StringBuilder("SELECT * FROM MESSAGE");
      queryString.append(" WHERE CONVERSATIONID IN ");
      queryString.append(CharacterConstants.O_BRACKET);
      for (String id : convids) {
        queryString.append(CharacterConstants.QUESTION).append(CharacterConstants.COMMA);
      }
      queryString.setLength(queryString.length() - 1);
      queryString.append(CharacterConstants.C_BRACKET);
      paramArray1.addAll(convids);
      final String orderBy = " ORDER BY CREATEDATE DESC";
      queryString.append(orderBy);
      String
          limitStr =
          " LIMIT " + pageParams.getOffset() + CharacterConstants.COMMA + pageParams.getSize();
      queryString.append(limitStr);

      msgQuery = pm.newQuery("javax.jdo.query.SQL", queryString.toString());
      msgQuery.setClass(Message.class);

      List<Message> msgList = (List<Message>) msgQuery.executeWithArray(paramArray1.toArray());
      msgList = (List<Message>) pm.detachCopyAll(msgList);

      String
          cntQueryStr =
          queryString.toString().replace("*", QueryConstants.ROW_COUNT)
              .replace(orderBy, CharacterConstants.EMPTY);
      cntQueryStr = cntQueryStr.replace(limitStr, CharacterConstants.EMPTY);

      cntQuery = pm.newQuery("javax.jdo.query.SQL", cntQueryStr);
      int
          count =
          ((Long) ((List) cntQuery.executeWithArray(convids.toArray())).iterator().next())
              .intValue();

      List<MessageModel> messageModelList = null;
      if (null != msgList) {
        messageModelList = new ArrayList<>();
        for (IMessage message : msgList) {
          messageModelList.add(msgBuilder.buildModel(message));
        }
      }
      res = new Results(messageModelList, null, count, pageParams.getOffset());
    } finally {
      if (query != null) {
        try {
          query.closeAll();
        } catch (Exception ignored) {
          xLogger.warn("Exception while closing query", ignored);
        }
      }
      if (msgQuery != null) {
        try {
          msgQuery.closeAll();
        } catch (Exception ignored) {
          xLogger.warn("Exception while closing query", ignored);
        }
      }
      if (cntQuery != null) {
        try {
          cntQuery.closeAll();
        } catch (Exception ignored) {
          xLogger.warn("Exception while closing query", ignored);
        }
      }
      if (pm != null) {
        pm.close();
      }
    }
    return res;
  }

  @Override
  public String getConversationId(String objectType, String objectId, PersistenceManager pm) {
    Query query = pm.newQuery(Conversation.class);
    query.setFilter("objectId == objIdParam && objectType == objTypeParam");
    query.declareParameters("String objIdParam, String objTypeParam");
    Map<String, Object> params = new HashMap<>(2);
    params.put("objIdParam", objectId);
    params.put("objTypeParam", objectType);
    query.setRange(0, 1);
    List<Conversation> results = (List<Conversation>) query.executeWithMap(params);
    if (results != null && !results.isEmpty()) {
      return results.get(0).getId();
    }
    return null;
  }

}

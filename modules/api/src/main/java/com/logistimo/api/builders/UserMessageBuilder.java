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

import com.logistimo.communications.MessageHandlingException;
import com.logistimo.communications.service.MessageService;
import com.logistimo.entity.IMessageLog;
import com.logistimo.services.ObjectNotFoundException;
import com.logistimo.services.ServiceException;
import com.logistimo.utils.LocalDateUtil;
import com.logistimo.api.models.UserMessageModel;
import com.logistimo.users.entity.IUserAccount;
import com.logistimo.users.service.UsersService;

import java.util.Locale;

/**
 * Created by mohan raja.
 */
public class UserMessageBuilder {

  final static int MAX_MESSAGE_SIZE = 160;

  public UserMessageModel buildUserMessageModel(IMessageLog messageLog, UsersService as,
                                                Locale locale, String userId, int offset,
                                                String timezone)
      throws ServiceException, ObjectNotFoundException, MessageHandlingException {
    IUserAccount u = as.getUserAccount(userId);
    MessageService smsService = MessageService.getInstance(MessageService.SMS, u.getCountry());
    MessageService emailService = MessageService.getInstance(MessageService.EMAIL, u.getCountry());

    IUserAccount sender = null;
    IUserAccount user = null;

    try {
      sender = as.getUserAccount(messageLog.getSenderId());
    } catch (Exception ignored) {
      // ignore
    }

    try {
      user = as.getUserAccount(messageLog.getUserId());
    } catch (Exception ignored) {
      // ignore
    }

    MessageService ms;
    if (MessageService.SMS.equals(messageLog.getType())) {
      ms = smsService;
    } else {
      ms = emailService;
    }

    String msg = messageLog.getMessage();
    if (msg != null && msg.length() > MAX_MESSAGE_SIZE) {
      msg = msg.substring(0, MAX_MESSAGE_SIZE) + "...";
    }

    UserMessageModel model = new UserMessageModel();
    model.sno = offset;
    if (null != user) {
      model.sto = user.getFullName() + " (" + user.getMobilePhoneNumber() + ")";
    }
    model.tm = LocalDateUtil.format(messageLog.getTimestamp(), locale, timezone);
    model.sta = ms.getStatusMessage(messageLog.getStatus(), locale);
    if (sender != null) {
      model.sby = sender.getFullName();
    }
    model.text = msg;
    if (messageLog.getEventType() != null && !messageLog.getEventType().isEmpty()) {
      model.evtp = messageLog.getEventType();
    }
    return model;
  }

/*    public List<UserMessageModel> buildUserMessageModels(List<MessageLog> messageLogs, AccountsService as, Locale locale, String userId, int offset, String timezone)
            throws ServiceException, ObjectNotFoundException, MessageHandlingException {
        List<UserMessageModel> models = new ArrayList<UserMessageModel>(messageLogs.size());
        for (MessageLog messageLog : messageLogs) {
            models.add(buildUserMessageModel(messageLog,as,locale,userId,offset,timezone));
        }
        return models;
    }*/
}

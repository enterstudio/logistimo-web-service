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

package com.logistimo.exports.handlers;

import com.logistimo.communications.MessageHandlingException;
import com.logistimo.communications.service.MessageService;
import com.logistimo.config.models.DomainConfig;
import com.logistimo.entity.IMessageLog;
import com.logistimo.services.ObjectNotFoundException;
import com.logistimo.services.Services;
import com.logistimo.users.entity.IUserAccount;
import com.logistimo.users.service.UsersService;
import com.logistimo.users.service.impl.UsersServiceImpl;
import com.logistimo.utils.LocalDateUtil;

import org.apache.commons.lang.StringEscapeUtils;

import java.util.Locale;

/**
 * Created by charan on 08/03/17.
 */
public class MessageLogExportHandler implements IExportHandler {

  IMessageLog messageLog;

  public MessageLogExportHandler(IMessageLog messageLog){
    this.messageLog = messageLog;
  }

  @Override
  public String getCSVHeader(Locale locale, DomainConfig dc, String type) {
    return "User,Mobile phone number,Notification type,Message,Status,Time ";
  }

  @Override
  public String toCSV(Locale locale, String timezone, DomainConfig dc, String type) {
    String str = "";
    String status = "";
    String name;
    String ph;
    try {
      UsersService as = Services.getService(UsersServiceImpl.class, locale);
      try {
        IUserAccount u = as.getUserAccount(messageLog.getUserId());
        MessageService smsService = MessageService.getInstance(MessageService.SMS, u.getCountry());
        name = u.getFullName();
        ph = u.getMobilePhoneNumber();
        status = smsService.getStatusMessage(messageLog.getStatus(), locale);
      } catch (ObjectNotFoundException e) {
        name = messageLog.getUserId() + "(" + "User deleted" + ")";
        ph = "";
      }
      str += name + ",";
      str += ph + ",";
      str +=  messageLog.getEventType() + "," + StringEscapeUtils.escapeCsv( messageLog.getMessage()) + ",";
      str += status + ",";
      str += LocalDateUtil.format(messageLog.getTimestamp(), locale, timezone);
    } catch (MessageHandlingException ignored) {
      // ignore
    }
    return str;
  }

}

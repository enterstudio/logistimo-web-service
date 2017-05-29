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

package com.logistimo.api.util;

import com.logistimo.AppFactory;
import com.logistimo.services.taskqueue.ITaskService;

import com.logistimo.communications.MessageHandlingException;
import com.logistimo.communications.service.MessageService;
import com.logistimo.communications.ServiceResponse;
import com.logistimo.pagination.PageParams;
import com.logistimo.pagination.Results;
import com.logistimo.services.ServiceException;
import com.logistimo.services.Services;
import com.logistimo.logger.XLog;

import com.logistimo.api.models.UserMessageModel;
import com.logistimo.users.entity.IUserAccount;
import com.logistimo.users.service.UsersService;
import com.logistimo.users.service.impl.UsersServiceImpl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Created by mohan raja.
 */
public class UserMessageUtil {

  private static final int MAX_USERS_PER_TASK = 100;
  private static final String TASK_URL = "/task/communicator";
  private static final String GETJAR_URL = "/task/j";

  private static final XLog xLogger = XLog.getLog(UserMessageUtil.class);

  private static ITaskService taskService = AppFactory.get().getTaskService();

  public static void scheduleSendingToAllUsers(UserMessageModel model, Long domainId, Locale locale,
                                               String cursor, String size, String senderUserId) {
    // Parameters used when sending to all users
    if (size == null && cursor == null) {
      ScheduleMessageWithoutCursor(model, domainId, senderUserId);
      return;
    }
    // Process the request
    int maxUserSize = MAX_USERS_PER_TASK;
    if (size != null && !size.isEmpty()) {
      maxUserSize = Integer.parseInt(size);
    }

    // Get the page parameters
    PageParams pageParams = new PageParams(cursor, maxUserSize);
    Results results;
    IUserAccount sendingUser;
    try {
      // Get service
      UsersService as = Services.getService(UsersServiceImpl.class, locale);
      sendingUser = as.getUserAccount(senderUserId);
      results = as.getUsers(domainId, sendingUser, true, null, pageParams);
    } catch (Exception e) {
      xLogger.severe("Error when getting users in domain {0} for sending user {1}: {2}", domainId,
          senderUserId, e.getMessage());
      return;
    }
    List<IUserAccount> users = (List<IUserAccount>) results.getResults();
    try {
      sendSMSMessage(model.type, model.text, users, model.pushURL, sendingUser, false, domainId);
    } catch (Exception e) {
      xLogger.severe("Exception when sending message {0} / url {1} to {2} users in domain {3}: {4}",
          model.text, model.pushURL, users.size(), domainId, e.getMessage());
    }

    if (users.size() < maxUserSize) {
      return; // we are done
    }

    ScheduleMessageWithCursor(model, domainId, size, results, sendingUser);
    xLogger.fine("Exiting scheduleSendingToAllUsers");
  }

  private static void ScheduleMessageWithoutCursor(UserMessageModel model, Long domainId,
                                                   String sendingUserId) {
    Map<String, String> params = new HashMap<String, String>();
    params.put("msgtype", model.type);
    params.put("msgtemplate", model.template);
    params.put("message", model.text);
    params.put("pushurl", model.pushURL);
    params.put("allusers", "yes");
    params.put("sendinguserid", sendingUserId);
    params.put("domainid", domainId.toString());
    params.put("s", String.valueOf(MAX_USERS_PER_TASK));
    params.put("batch", "true");
    try {
      taskService
          .schedule(ITaskService.QUEUE_MESSAGE, TASK_URL, params, null, ITaskService.METHOD_POST);
    } catch (Exception e) {
      xLogger.severe(
          "Exception when scheduling task to send message: sending user = {0}, message = {1}, push-url = {2}, domainId = {3}: {4}",
          sendingUserId, model.text, model.pushURL, domainId, e.getMessage());
    }
  }

  private static void ScheduleMessageWithCursor(UserMessageModel model, Long domainId, String size,
                                                Results results, IUserAccount sendingUser) {
    Map<String, String> params = new HashMap<String, String>();
    params.put("msgtype", model.type);
    params.put("msgtemplate", model.template);
    params.put("message", model.text);
    params.put("pushurl", model.pushURL);
    params.put("allusers", "yes");
    params.put("sendinguserid", sendingUser.getUserId());
    params.put("domainid", domainId.toString());
    params.put("s", String.valueOf(size));
    params.put("o", String.valueOf(size));
    params.put("batch", "true");

    // Schedule the first task in the chain
    try {
      taskService
          .schedule(ITaskService.QUEUE_MESSAGE, TASK_URL, params, null, ITaskService.METHOD_POST);
    } catch (Exception e) {
      xLogger.severe(
          "Exception when scheduling task to send message: sending user = {0}, message = {1}, push-url = {2}, domainId = {3}: {4}",
          sendingUser.getUserId(), model.text, model.pushURL, domainId, e.getMessage());
    }
  }

  public static void sendToSelectedUsers(UserMessageModel model, String senderUserId, Long domainId)
      throws ServiceException {
    UsersService as = Services.getService(UsersServiceImpl.class);
    try {
      IUserAccount sendingUser = as.getUserAccount(senderUserId);
      // Get the user Ids
      String[] userIds = model.userIds.split(",");
      if (userIds.length == 0) {
        userIds = new String[1];
        userIds[0] = model.userIds;
      }
      // Get the user's phone numbers or email addresses, as required
      List<IUserAccount> users = new ArrayList<>();
      for (String userId : userIds) {
        users.add(as.getUserAccount(userId));
      }
      // Send message
      sendSMSMessage(model.type, model.text, users, model.pushURL, sendingUser, false,
          domainId); // response is ignored, due to logging
    } catch (IOException e) {
      xLogger.severe("IOException: {0}", e.getMessage());
    } catch (Exception e) {
      throw new ServiceException(e.getMessage());
    }

  }

  private static ServiceResponse sendSMSMessage(String svcType, String message,
                                                List<IUserAccount> users, String pushUrl,
                                                IUserAccount sendingUser, boolean customApp,
                                                Long domainId)
      throws IOException, ServiceException, MessageHandlingException {
    ServiceResponse resp = null;
    xLogger.fine("Entered sendSMSMessage");
    Long lDomainId = domainId;
    if (lDomainId == null) {
      lDomainId = sendingUser.getDomainId();
    }
    // Get the SMS service with logging enabled
    MessageService
        msgService =
        MessageService.getInstance(svcType, sendingUser.getCountry(), true, lDomainId,
            sendingUser.getUserId(), null);
    // If custom application, spawn a task per user to customize application and send message
    if (customApp) {
      sendCustomAppMessage(users);
      return null;
    }
    // Send the message
    if (pushUrl != null && !pushUrl.isEmpty()) {
      resp = msgService.send(users, message, MessageService.WAPPUSH, pushUrl, null, null, 0, null);
    } else if (message != null && !message.isEmpty()) {
      resp =
          msgService
              .send(users, message, MessageService.getMessageType(message), null, null, null, 0,
                  null);
    } else {
      xLogger.warn("Nothing to send - message or URL");
    }
    xLogger.fine("Exiting sendSMSMessage");
    return resp;
  }

  private static void sendCustomAppMessage(List<IUserAccount> users) {
    xLogger.fine("Entered sendCustomAppMessage");
    if (users == null) {
      return;
    }
    Iterator<IUserAccount> it = users.iterator();
    Map<String, String> params = new HashMap<String, String>();
    while (it.hasNext()) {
      IUserAccount u = it.next();
      params.put("u", u.getUserId());
      params.put("l",
          u.getLanguage()); // NOTE: Language is sent, instead of actual locale, given mobile app. is current language specific
      params.put("f", "true"); // force creation of a JAR
      params.put("n", "s"); // send SMS
      // Schedule task to create custom JAR and notify user
      try {
        taskService.schedule(ITaskService.QUEUE_MESSAGE, GETJAR_URL, params, null,
            ITaskService.METHOD_POST);
      } catch (Exception e) {
        xLogger.warn(
            "Unable to schedule task to send custom JAR to user {0} with locale {1}: {2} : {3}",
            u.getUserId(), u.getLanguage(), e.getClass().getName(), e.getMessage());
      }
    }
    xLogger.fine("Exiting sendCustomAppMessage");
  }
}

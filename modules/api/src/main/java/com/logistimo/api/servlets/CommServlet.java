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

/**
 *
 */
package com.logistimo.api.servlets;

import com.logistimo.AppFactory;
import com.logistimo.auth.SecurityMgr;
import com.logistimo.auth.utils.SessionMgr;
import com.logistimo.communications.MessageHandlingException;
import com.logistimo.communications.ServiceResponse;
import com.logistimo.communications.service.MessageService;
import com.logistimo.constants.Constants;
import com.logistimo.logger.XLog;
import com.logistimo.pagination.PageParams;
import com.logistimo.pagination.Results;
import com.logistimo.security.SecureUserDetails;
import com.logistimo.services.ServiceException;
import com.logistimo.services.Services;
import com.logistimo.services.taskqueue.ITaskService;
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
import java.util.ResourceBundle;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Sends SMS or email messages to users
 *
 * @author Arun
 */
@SuppressWarnings("serial")
public class CommServlet extends SgServlet {

  // Logger
  private static final XLog xLogger = XLog.getLog(CommServlet.class);
  // Constants
  private static final String SMS = "sms";
  private static final String EMAIL = "email";
  private static final int SMS_LIMIT = 160;
  private static final int EMAIL_LIMIT = 2000;
  private static final int MAX_USERS_PER_TASK = 100;
  // Task URL
  private static final String TASK_URL = "/task/communicator";
  private static final String GETJAR_URL = "/task/j";

  private static ITaskService taskService = AppFactory.get().getTaskService();

  // GET method
  public void processGet(HttpServletRequest request, HttpServletResponse response,
                         ResourceBundle backendMessages, ResourceBundle messages)
      throws ServletException, IOException, ServiceException {
    // Do nothing
  }

  // Get the details from the form, and then send messages
  public void processPost(HttpServletRequest request, HttpServletResponse response,
                          ResourceBundle backendMessages, ResourceBundle messages)
      throws ServletException, IOException, ServiceException {
    xLogger.fine("Entered processPost");
    // Get the request parameters
    String msgType = request.getParameter("msgtype");
    String msgTemplate = request.getParameter("msgtemplate");
    String sendMessage = request.getParameter("message");
    String pushUrl = request.getParameter("pushurl");
    String userIdsStr = request.getParameter("userids");
    String
        allUsersStr =
        request.getParameter("allusers"); // should be "yes" if we need to send to all users
    String appType = request.getParameter("apptype"); // standard or custom - only for WAP pushes
    boolean isBatch = request.getParameter("batch") != null;

    if (msgType == null || msgType.isEmpty()) {
      msgType = SMS;
    }
    boolean customApp = false;
    if (!"wappush".equals(msgTemplate)) {
      pushUrl = null;
    } else {
      // Check whether custom application
      customApp = "custom".equals(appType);
      xLogger.fine("customApp: {0}", customApp);
    }
    boolean allUsers = allUsersStr != null && "yes".equals(allUsersStr);
    String msg = "";
    // NOTE: Given client-side JS validation, these messages are unlikely to be used
    if (!customApp && (sendMessage == null || sendMessage.isEmpty())) {
      msg += " - No message specified<br/>";
    } else if (!customApp && SMS.equals(msgType) && sendMessage.length() > SMS_LIMIT) {
      msg +=
          " - Message size is greater than " + SMS_LIMIT
              + " characters. Please re-type a shorter message.";
    } else if (!customApp && EMAIL.equals(msgType) && sendMessage.length() > EMAIL_LIMIT) {
      msg +=
          " - Message size is greater than " + EMAIL_LIMIT
              + " characters. Please re-type a shorter message.";
    }
    if ((userIdsStr == null || userIdsStr.isEmpty()) && !allUsers) {
      msg += " - No users specified<br/>";
    }
    if (msg.length() > 0) {
      msg = "One or more errors were encountered:<br/><br/>" + msg +
          "<br><br/>Please click your browser's 'Back' button, fix the error(s) and retry.";
      writeToScreen(request, response, msg, Constants.VIEW_USERS);
      return;
    }
    // Get the sending user's domain/ID information
    SecureUserDetails sUser = SecurityMgr.getUserDetails(request.getSession());
    String sendingUserId = null;
    Long domainId = null;
    Locale locale = null;
    if (sUser != null) {
      sendingUserId = sUser.getUsername();
      domainId = SessionMgr.getCurrentDomain(request.getSession(), sendingUserId);
      locale = sUser.getLocale();
    }
    // Send the message to selected users or schedule sending to all users
    if (allUsers) {
      scheduleSendingToAllUsers(request, domainId, locale, sendingUserId, msgType, msgTemplate,
          sendMessage, pushUrl, customApp);
      if (!isBatch) {
        msg =
            "Scheduled task for sending message to all users"
                + " &nbsp;[<a href=\"/s/setup/setup.jsp?subview=users\">" + backendMessages
                .getString("users.view") + "</a>]";
        writeToScreen(request, response, msg, Constants.VIEW_USERS);
      }
    } else {
      sendToSelectedUsers(request, response, backendMessages, customApp);
    }
    xLogger.fine("Exiting processPost");
  }

  // Send messages to selected users
  private void sendToSelectedUsers(HttpServletRequest request, HttpServletResponse response,
                                   ResourceBundle backendMessages, boolean customApp)
      throws ServletException, IOException, ServiceException {
    xLogger.fine("Entered sendToSelectedUsers");
    // Get the request parameters
    String msgType = request.getParameter("msgtype");
    String msgTemplate = request.getParameter("msgtemplate");
    String sendMessage = request.getParameter("message");
    String userIdsStr = request.getParameter("userids");
    String pushUrl = request.getParameter("pushurl");
    // Get parameters, in case this request comes as a task
    String sendingUserId = request.getParameter("sendinguserid");
    String domainIdStr = request.getParameter("domainid");

    if (msgType == null || msgType.isEmpty()) {
      msgType = SMS;
    }
    if (!"wappush".equals(msgTemplate)) {
      pushUrl = null;
    }

    String msg = "";
    // NOTE: Given client-side JS validation, these messages are unlikely to be used
    if (!customApp && (sendMessage == null || sendMessage.isEmpty())) {
      msg += " - No message specified<br/>";
    } else if (!customApp && SMS.equals(msgType) && sendMessage.length() > SMS_LIMIT) {
      msg +=
          " - Message size is greater than " + SMS_LIMIT
              + " characters. Please re-type a shorter message.";
    } else if (!customApp && EMAIL.equals(msgType) && sendMessage.length() > EMAIL_LIMIT) {
      msg +=
          " - Message size is greater than " + EMAIL_LIMIT
              + " characters. Please re-type a shorter message.";
    }
    if (userIdsStr == null || userIdsStr.isEmpty()) {
      msg += " - No users specified<br/>";
    }
    if (msg.length() > 0) {
      msg = "One or more errors were encountered:<br/><br/>" + msg +
          "<br><br/>Please click your browser's 'Back' button, fix the error(s) and retry.";
      writeToScreen(request, response, msg, Constants.VIEW_USERS);
      return;
    }
    // Get the sending user's domain/ID information
    SecureUserDetails sUser = SecurityMgr.getUserDetails(request.getSession());
    Long domainId = null;
    if (sUser != null) {
      sendingUserId = sUser.getUsername();
      domainId = SessionMgr.getCurrentDomain(request.getSession(), sendingUserId);
    } else {
      if (domainIdStr != null && !domainIdStr.isEmpty()) {
        domainId = Long.valueOf(domainIdStr);
      } else {
        throw new ServiceException("Invalid domain ID");
      }
    }
    // Get the accounts service
    UsersService as = Services.getService(UsersServiceImpl.class);
    try {
      IUserAccount sendingUser = as.getUserAccount(sendingUserId);
      // Get the user Ids
      String[] userIds = userIdsStr.split(",");
      if (userIds == null || userIds.length == 0) {
        userIds = new String[1];
        userIds[0] = userIdsStr;
      }
      // Get the user's phone numbers or email addresses, as required
      List<IUserAccount> users = new ArrayList<IUserAccount>();
      for (int i = 0; i < userIds.length; i++) {
        users.add(as.getUserAccount(userIds[i]));
      }
      // Send message
      sendSMSMessage(msgType, sendMessage, users, pushUrl, sendingUser, customApp,
          domainId); // response is ignored, due to logging
      msg =
          "<b>" + users.size() + "</b> " + backendMessages.getString("users.sentmessage")
              + " &nbsp;[<a href=\"/s/setup/setup.jsp?subview=users\">" + backendMessages
              .getString("users.view") + "</a>]";
    } catch (IOException e) {
      msg =
          backendMessages.getString("error") + ": " + backendMessages
              .getString("error.connectiontimeout") + ".\n\n" + backendMessages
              .getString("browser.simplygoback");
      xLogger.severe("IOException: {0}", e.getMessage());
    } catch (Exception e) {
      throw new ServiceException(e.getMessage());
    }
    // Write message
    if (sUser
        != null) // implies the request has come from a browser; otherwise, via task (/task/communicator)
    {
      writeToScreen(request, response, msg, Constants.VIEW_USERS);
    }
    xLogger.fine("Exiting sendToSelectedUsers");
  }

  // Schedule a task to send message to all users
  @SuppressWarnings("unchecked")
  private void scheduleSendingToAllUsers(HttpServletRequest request, Long domainId, Locale locale,
                                         String sendingUserId, String msgType, String msgTemplate,
                                         String sendMessage, String pushUrl, boolean customApp) {
    xLogger.fine("Entered scheduleSendingToAllUsers");
    // Parameters used when sending to all users
    String offsetStr = request.getParameter("o"); // query cursor, used with allusers
    String sizeStr = request.getParameter("s"); // number of users to query, user with allusers
    if (sendingUserId == null) {
      sendingUserId = request.getParameter("sendinguserid"); // used with allusers
    }
    String domainIdStr = request.getParameter("domainid"); // used with allusers
    if (domainId == null && domainIdStr != null && !domainIdStr.isEmpty()) {
      domainId = Long.parseLong(domainIdStr);
    }
    // Process the very first request for users in batch (size nor cursor are not passed first time)
    if (sizeStr == null && offsetStr == null) {
      Map<String, String> params = new HashMap<String, String>();
      params.put("msgtype", msgType);
      params.put("msgtemplate", msgTemplate);
      params.put("message", sendMessage);
      params.put("pushurl", pushUrl);
      params.put("allusers", "yes");
      params.put("sendinguserid", sendingUserId);
      params.put("domainid", domainId.toString());
      params.put("s", String.valueOf(MAX_USERS_PER_TASK));
      params.put("o", "0");
      params.put("batch", "true");
      if (customApp) {
        params.put("apptype", "custom");
      }
      // Schedule the first task in the chain
      try {
        taskService
            .schedule(taskService.QUEUE_MESSAGE, TASK_URL, params, null, taskService.METHOD_POST,
                domainId, sendingUserId, "SMS_ALL_USERS");
      } catch (Exception e) {
        xLogger.severe(
            "Exception when scheduling task to send message: sending user = {0}, message = {1}, push-url = {2}, domainId = {3}: {4}",
            sendingUserId, sendMessage, pushUrl, domainId, e.getMessage());
        // TODO: Send an email on exception to the sendingUser?
      }
      return;
    }
    // Process the request
    int size = MAX_USERS_PER_TASK;
    if (sizeStr != null && !sizeStr.isEmpty()) {
      size = Integer.parseInt(sizeStr);
    }

    int offset = offsetStr != null ? Integer.parseInt(offsetStr) : 0;
    // Get the page parameters
    PageParams pageParams = new PageParams(null, offset, size);
    Results results = null;
    IUserAccount sendingUser = null;
    try {
      // Get service
      UsersService as = Services.getService(UsersServiceImpl.class, locale);
      sendingUser = as.getUserAccount(sendingUserId);
      results = as.getUsers(domainId, sendingUser, true, null, pageParams);
    } catch (Exception e) {
      xLogger.severe("Error when getting users in domain {0} for sending user {1}: {2}", domainId,
          sendingUserId, e.getMessage());
      return;
      // TODO: Send an email on exception to the sendingUser?
    }
    // Get the list of users (in CSV form)
    List<IUserAccount> users = (List<IUserAccount>) results.getResults();
    // Send message
    try {
      sendSMSMessage(msgType, sendMessage, users, pushUrl, sendingUser, customApp, domainId);
    } catch (Exception e) {
      xLogger.severe("Exception when sending message {0} / url {1} to {2} users in domain {3}: {4}",
          sendMessage, pushUrl, users.size(), domainId, e.getMessage());
      // TODO: Send an email on exception to the sendingUser?
    }

    // Check if more users exist; if not, stop chaining and return
    if (users.size() < size) {
      return; // we are done
    }

    // Chain task to send to next set of users
    Map<String, String> params = new HashMap<>();
    params.put("msgtype", msgType);
    params.put("msgtemplate", msgTemplate);
    params.put("message", sendMessage);
    params.put("pushurl", pushUrl);
    params.put("allusers", "yes");
    params.put("sendinguserid", sendingUser.getUserId());
    params.put("domainid", domainId.toString());
    params.put("s", String.valueOf(size));
    params.put("o", String.valueOf(offset + size));
    params.put("batch", "true");

    // Schedule the first task in the chain
    try {
      taskService
          .schedule(taskService.QUEUE_MESSAGE, TASK_URL, params, null, taskService.METHOD_POST,
              domainId, sendingUserId, "SMS_ALL_USERS");
    } catch (Exception e) {
      xLogger.severe(
          "Exception when scheduling task to send message: sending user = {0}, message = {1}, push-url = {2}, domainId = {3}: {4}",
          sendingUser.getUserId(), sendMessage, pushUrl, domainId, e.getMessage());
      // TODO: Send an email on exception to the sendingUser?
    }
    // send an email with errors, if any
    xLogger.fine("Exiting scheduleSendingToAllUsers");
  }

  // Send sms/email messages
  private ServiceResponse sendSMSMessage(String svcType, String message, List<IUserAccount> users,
                                         String pushUrl, IUserAccount sendingUser,
                                         boolean customApp, Long domainId)
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

  // Send a custom application message to given users
  private void sendCustomAppMessage(List<IUserAccount> users) {
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
        taskService
            .schedule(taskService.QUEUE_MESSAGE, GETJAR_URL, params, null, taskService.METHOD_POST,
                -1, null, "SMS_APP_PUSH");
      } catch (Exception e) {
        xLogger.warn(
            "Unable to schedule task to send custom JAR to user {0} with locale {1}: {2} : {3}",
            u.getUserId(), u.getLanguage(), e.getClass().getName(), e.getMessage());
      }
    }
    xLogger.fine("Exiting sendCustomAppMessage");
  }

  // Write to screen
  private void writeToScreen(HttpServletRequest req, HttpServletResponse resp, String message,
                             String view) throws IOException {
    writeToScreen(req, resp, message, null, view);
  }

  private void writeToScreen(HttpServletRequest req, HttpServletResponse resp, String message,
                             String mode, String view)
      throws IOException {
    writeToScreen(req, resp, message, mode, view, "/s/message.jsp");
  }
}

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
package com.logistimo.communications.service;

import com.codahale.metrics.Timer;
import com.logistimo.dao.JDOUtils;
import com.logistimo.users.entity.IUserAccount;
import com.logistimo.utils.MetricsUtil;

import org.apache.commons.lang.StringUtils;
import com.logistimo.communications.MessageHandlingException;
import com.logistimo.communications.ServiceResponse;
import com.logistimo.entity.IMessageLog;
import com.logistimo.constants.Constants;
import com.logistimo.utils.MessageUtil;
import com.logistimo.logger.XLog;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

/**
 * Represents a messaging service - SMS or Email
 *
 * @author Arun
 */
public abstract class MessageService {

  // Message send types
  public static final String SMS = "sms";
  public static final String EMAIL = "email";
  // Message types
  public static final int NORMAL = 0;
  public static final int UNICODE = 1;
  public static final int BINARY = 2;
  public static final int WAPPUSH = 3;
  // Logger
  protected static final XLog xLogger = XLog.getLog(MessageService.class);
  // Character Encoder
  protected static CharsetEncoder
      asciiEncoder =
      Charset.forName("ISO-8859-1").newEncoder();
  // or "ISO-8859-1" for ISO Latin 1
  private final Timer smsTimer = MetricsUtil.getTimer(MessageService.class, "sms");
  private final Timer emailTimer = MetricsUtil.getTimer(MessageService.class, "emails");
  protected String type = null;
  protected boolean logging = false;
  protected Long domainId = null;
  protected String sendingUserId = null;

  public static MessageService getInstance(String type, String countryCode)
      throws MessageHandlingException {
    return getInstance(type, countryCode, false, null, null, null);
  }

  public static MessageService getInstance(String type, String countryCode, boolean logging,
                                           Long domainId) throws MessageHandlingException {
    return getInstance(type, countryCode, logging, domainId, null, null);
  }

  public static MessageService getInstance(String type, String countryCode, boolean logging,
                                           Long domainId, String sendingUserId, Integer notif)
      throws MessageHandlingException {
    MessageService s = null;
    if (SMS.equals(type)) {
      try {
        s = SMSService.getInstance(countryCode, SMSService.OUTGOING);
      } catch (Exception e) {
        throw new MessageHandlingException(e.getMessage());
      }
    } else if (EMAIL.equals(type)) {
      s = EmailService.getInstance();
    } else {
      xLogger.warn("Unsupported message service type: {0}", type);
    }
    // Update service properties
    if (s != null) {
      s.type = type;
      s.logging = logging;
      //if ( logging ) {
      s.domainId = domainId;
      s.sendingUserId = sendingUserId;
      //}
    }
    return s;
  }

  // Get the type of message encoding - whether normal, binray, unicode, etc.
  public static int getMessageType(String message) {
    int mtype = MessageService.NORMAL;
    try {
      if (message != null && !asciiEncoder.canEncode(message)) {
        mtype = MessageService.UNICODE;
      }
    } catch (Exception e) {
      xLogger
          .warn("{0} when detecting is message can be ASCII encoded: {1}", e.getClass().getName(),
              e.getMessage());
    }
    return mtype;
  }

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  public boolean isLoggingEnabled() {
    return logging;
  }

  public void setLogging(boolean logging) {
    this.logging = logging;
  }

  public Long getDomainId() {
    return domainId;
  }

  public void setDomainId(Long domainId) {
    this.domainId = domainId;
  }

  public String getSendingUserId() {
    return sendingUserId;
  }

  public void setSendingUserId(String sendingUserId) {
    this.sendingUserId = sendingUserId;
  }

  // Send a message to a single address
  public ServiceResponse send(String address, String message, int messageType, String subject,
                              String port) throws MessageHandlingException, IOException {
    List<String> addresses = new ArrayList<String>();
    addresses.add(address);
    final Timer.Context context = SMS.equals(type) ? smsTimer.time() : emailTimer.time();
    try {
      return doSend(addresses, message, messageType, subject, port, domainId);
    } finally {
      context.stop();
    }
  }

  // Send a single message to a single user (subject, isBinary and port are optional)
  public ServiceResponse send(IUserAccount user, String message, int messageType, String subject,
                              String port, String logMessage)
      throws MessageHandlingException, IOException {
    List<IUserAccount> users = new ArrayList<>(1);
    users.add(user);
    return send(users, message, messageType, subject, port, logMessage, 0, null);
  }

  // Send a single message to multiple users
  public ServiceResponse send(List<IUserAccount> users, String message, int messageType,
                              String subject, String port, String logMessage, Integer notif,
                              String eventType) throws MessageHandlingException, IOException {
    xLogger.fine("Entered send");

    // Get the address list
    List<String> addresses = getAddresses(users);
    final Timer.Context context = SMS.equals(type) ? smsTimer.time() : emailTimer.time();
    ServiceResponse resp = null;
    try {
      // Send the message and get response
      resp = doSend(addresses, message, messageType, subject, port, domainId);
    } finally {
      context.stop();
    }
    // Store the response
    if (logging) {
      logMessage(domainId, sendingUserId, logMessage != null ? logMessage : message, users, resp,
          notif, eventType);
    }
    xLogger.fine("Exiting send");
    return resp;
  }

  // Log the message sent (can be used by the child classes)
  protected void logMessage(Long domainId, String sendingUserId, String message,
                            List<IUserAccount> users, ServiceResponse svcResponse, Integer notif,
                            String eventType) throws MessageHandlingException {
    xLogger.fine("Entered logMessage");
    int method = svcResponse.getMethod();
    List<String> responses = svcResponse.getResponses(); // e.g., list of jobIds
    if (responses == null) {
      return;
    }
    int idx = 0;
    int numUsers = users.size();
    Date now = new Date();
    for (String respStr : svcResponse.getRespMap().keySet()) {
      // Get the list of addresses in this resp.
      List<String> addresses = svcResponse.getAddresses(respStr);
      xLogger.fine("respStr: {0}, addresses: {1}, numUsers: {2}", respStr, addresses, numUsers);
      if (addresses == null) {
        continue;
      }
      // Create a message log, per jobId-address pair
      for (String address1 : addresses) {
        String address = getFormattedAddress(address1);
        IMessageLog mlog = JDOUtils.createInstance(IMessageLog.class);
        if (method == ServiceResponse.METHOD_STATUS || method == ServiceResponse.METHOD_MESSAGE) {
          mlog.setKey(JDOUtils.createMessageLogKey(String.valueOf(now.getTime()), address));
          mlog.setStatus(respStr);
        } else if (svcResponse.getMethod() == ServiceResponse.METHOD_ID) {
          String key;
          String jobId = getJobId(respStr);
          if (jobId == null) {
            jobId = String.valueOf(now.getTime());
          }
          if (svcResponse.isJobIdUnique()) {
            key =
                JDOUtils.createMessageLogKey(jobId,
                    null); // job ID itself is unique (no need to use address for uniqueness, since it may not be available in status report)
          } else {
            key =
                JDOUtils
                    .createMessageLogKey(jobId, address); // use a combination of job ID and address
          }
          // Set key
          mlog.setKey(key);
        }
        mlog.setType(type);
        mlog.setDomainId(domainId);
        mlog.setSenderId(sendingUserId);
        if (idx < numUsers) {
          mlog.setUserId(users.get(idx).getUserId());
          idx++;
        }
        xLogger.fine("mlog.setUserId = {0}, idx = {1}", mlog.getUserId(), idx);
        //mlog.setAddress( MessageUtil.getCSV( svcResponse.getAddresses( respStr ) ) );
        mlog.setAddress(address);
        mlog.setMessage(message);
        mlog.setProviderId(svcResponse.getProviderId());
        mlog.setTimestamp(now);
        mlog.setNotif(notif);
        mlog.setEventType(eventType);
        if (StringUtils.isNotBlank(respStr) && respStr.trim().startsWith(Constants.ERROR)) {
          mlog.setStatus(Constants.MINUSONE);
        } else if (SMSService.SMS_MAX_USER_KEY.equals(respStr) || SMSService.SMS_MAX_DOMAIN_KEY
            .equals(respStr) || SMSService.SMS_DUPLICATE_KEY.equals(respStr)) {
          mlog.setStatus(respStr);
        }
        try {
          // Log message
          MessageUtil.log(mlog);
        } catch (Exception e) {
          xLogger.warn("Error while sending messages to address: {0} and message: {1}", address,
              message, e);
        }
      }
    }
    xLogger.fine("Exiting logMessage");
  }

  // Get the list of addresses from users
  private List<String> getAddresses(List<IUserAccount> users) {
    List<String> addresses = new ArrayList<String>();
    Iterator<IUserAccount> it = users.iterator();
    while (it.hasNext()) {
      IUserAccount u = it.next();
      String address = null;
      if (EMAIL.equals(type)) {
        address = u.getEmail();
      } else {
        address = u.getMobilePhoneNumber();
      }
      addresses.add(address);
    }
    return addresses;
  }

  // Method that children of this class should implement for sending message (this is not public)
  protected abstract ServiceResponse doSend(List<String> addresses, String message, int messageType,
                                            String subject, String port, Long domainId)
      throws MessageHandlingException, IOException;

  // Get the job Id from a given response string (of a gateway sms push call)
  protected abstract String getJobId(String responseStr);

  // Get the formatted address, if any
  public abstract String getFormattedAddress(String address);

  // Get the status description, given a code
  public abstract String getStatusMessage(String statusCode, Locale locale);

  // Get the provider Id
  public abstract String getProviderId();

}

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
package com.logistimo.api.communications;

import com.logistimo.communications.MessageHandlingException;
import com.logistimo.communications.service.MessageService;
import com.logistimo.constants.Constants;
import com.logistimo.logger.XLog;
import com.logistimo.materials.service.MaterialCatalogService;
import com.logistimo.materials.service.impl.MaterialCatalogServiceImpl;
import com.logistimo.services.ObjectNotFoundException;
import com.logistimo.services.Resources;
import com.logistimo.services.ServiceException;
import com.logistimo.services.Services;
import com.logistimo.users.entity.IUserAccount;
import com.logistimo.users.service.UsersService;
import com.logistimo.users.service.impl.UsersServiceImpl;

import java.io.IOException;
import java.util.Date;
import java.util.Enumeration;
import java.util.ResourceBundle;
import java.util.Vector;

/**
 * Handles incoming messages, esp. protocol related
 *
 * @author Arun
 */
public abstract class MessageHandler {

  // Parameters
  public static final String ADDRESS = "address";
  public static final String MESSAGE = "message";
  public static final String RECEIVEDON = "recdon";
  public static final String WIRETYPE = "wiretype";
  // Protocol types
  public static final String MACHINE = "m"; // machine-understandable protocol
  public static final String HUMAN = "h"; // human-understandable protocol
  // Incoming SMS params.
  public static final String KEYWORD = "LOGI";
  // Retrials
  public static final int NUM_RETRIALS = 3;
  // Delay between REST API calls
  public static final int REST_CALL_DELAY = 1000; // milliseconds
  // Logger
  protected static final XLog xLogger = XLog.getLog(MessageHandler.class);
  // Properties
  protected String wireType = null;
  protected String address = null;
  protected String message = null;
  protected Date recdOn = null;
  protected String urlBase = null;
  // Services
  protected UsersService as = null;
  protected MaterialCatalogService mcs = null;
  // User details
  protected IUserAccount user = null;
  protected String countryCode = null;
  // Message service
  protected MessageService msgservice = null;
  // Resource bundle
  ResourceBundle backendMessages = null;
  ///protected long delayBetweenCalls = 0; // milliseconds

  protected MessageHandler(String wireType, String message, String address, Date recdOn)
      throws MessageHandlingException {
    this.wireType = wireType;
    this.message = message;
    this.address = address;
    this.recdOn = recdOn;
    as = Services.getService(UsersServiceImpl.class);
    mcs = Services.getService(MaterialCatalogServiceImpl.class);
    ///delayBetweenCalls = msgservice.getMillisBetweenCalls();
  }


  // Get the appropriate instance of the message handler
  public static MessageHandler getInstance(String wireType, String message, String address,
                                           Date recdOn)
      throws MessageHandlingException {
    if (message == null || message.isEmpty()) {
      throw new MessageHandlingException("Message not specified");
    }
    // Check the human/machine protocol type, and return instance
    String protocolType = getProtocolType(message);
    xLogger.fine("Protocol type: {0}, Message: {1}", protocolType, message);
    // Return appropriate handler
    if (HUMAN.equals(protocolType)) {
      return new HumanProtocolMessageHandler(wireType, message, address, recdOn);
    } else if (MACHINE.equals(protocolType)) {
      return new MachineProtocolMessageHandler(wireType, message, address, recdOn);
    } else {
      xLogger.warn("Unsupported protocol type: {0}", protocolType);
    }
    return null;
  }

  // Get the protocol type from the message - if the message starts with a number, then it is machine-type
  private static String getProtocolType(String message) {
    String type = MACHINE;
    try {
      Integer.parseInt(message.substring(0, 1));
    } catch (NumberFormatException e) {
      type = HUMAN;
    }
    return type;
  }

  public String getWireType() {
    return wireType;
  }

  public String getAddress() {
    return address;
  }

  public String getMessage() {
    return message;
  }

  public Date getReceivedOn() {
    return recdOn;
  }

  public String getUrlBase() {
    return urlBase;
  }

  public void setUrlBase(String urlBase) {
    this.urlBase = urlBase;
  }

  // Send multiple messages (subject is optional)
  protected void sendMessages(Vector<String> messages, String subject, int messageType, String port)
      throws IOException, MessageHandlingException {
    if (messages == null) {
      return;
    }
    xLogger.info("Sending: " + messages.toString());
    if (msgservice == null) {
      msgservice = getMessageService();
    }
    Enumeration<String> en = messages.elements();
    int numSent = 0;
    while (en.hasMoreElements()) {
      String msg = en.nextElement();
      for (int i = 0; i < NUM_RETRIALS; i++) { // re-trial loop
        try {
          if (user != null) {
            msgservice.send(user, msg, messageType, subject, port, null);
          } else {
            msgservice.send(address, msg, messageType, subject, port);
          }
          numSent++;
          break; // break out of re-trial loop
        } catch (IOException e) {
          xLogger.warn("IOException when sending message - retrying: {0}", e.getMessage());
          // Give a delay here before retrying
          delay(REST_CALL_DELAY);
        } catch (MessageHandlingException e) {
          xLogger.warn("MessageHandlingException when sending message: {0}", e.getMessage());
        }
      }
      // Give a delay before sending the next message
      ///delay( delayBetweenCalls );
    }
    xLogger.info("{0} messages, {1} sent", messages.size(), numSent);
  }

  protected void sendMessages(String address, Vector<String> messages, int messageType, String port)
      throws IOException, MessageHandlingException {
    xLogger.fine("Entered sendMessages - with address (not user)");
    if (messages == null) {
      return;
    }
    xLogger.info("Sending: " + messages.toString());
    if (countryCode == null) {
      countryCode = Constants.COUNTRY_DEFAULT;
    }
    MessageService msgsvc = MessageService.getInstance(MessageService.SMS, countryCode);
    Enumeration<String> en = messages.elements();
    int numSent = 0;
    while (en.hasMoreElements()) {
      String msg = en.nextElement();
      for (int i = 0; i < NUM_RETRIALS; i++) { // re-trial loop
        try {
          msgsvc.send(address, msg, messageType, null, port);
          numSent++;
          break; // break out of re-trial loop
        } catch (IOException e) {
          xLogger.warn("IOException when sending message - retrying: {0}", e.getMessage());
          // Give a delay here before retrying
          delay(REST_CALL_DELAY);
        } catch (MessageHandlingException e) {
          xLogger.warn("MessageHandlingException when sending message: {0}", e.getMessage());
        }
      }
      // Give a delay before sending the next message
      ///delay( delayBetweenCalls );
    }
    xLogger.info("{0} messages, {1} sent", messages.size(), numSent);
  }

  // Set the user details, given user Id (countryCode required to determine sms g/w; domainId, sendingUserId required for logging)
  protected void setUserDetails(String userId) throws ServiceException,
      ObjectNotFoundException {
    user = as.getUserAccount(userId);
    xLogger.fine("setUserDetails: userId = {0}, user = {1}", userId, user);
    countryCode =
        user.getCountry(); // this is set explicitly, so that MachineProtocolMessageHandler.getResponseMessages() can set this value for later use (i.e. determining which g/w)
    backendMessages = Resources.get().getBundle("BackendMessages", user.getLocale());
  }

  // Get the message service instance
  protected MessageService getMessageService() throws MessageHandlingException {
    Long domainId = null;
    if (user != null) {
      domainId = user.getDomainId();
    }
    if (countryCode == null) {
      countryCode = Constants.COUNTRY_DEFAULT;
    }
    // Get the message service
    MessageService msgservice = null;
    if (domainId != null) {
      msgservice =
          MessageService
              .getInstance(MessageService.SMS, countryCode, true, domainId, Constants.SYSTEM_ID,
                  null);
    } else {
      msgservice = MessageService.getInstance(MessageService.SMS, countryCode);
    }
    return msgservice;
  }

  // Process the message and send response
  public abstract void process() throws MessageHandlingException, IOException;

  private void delay(long millis) {
    if (millis <= 0) {
      return;
    }
    try {
      Thread.sleep(millis);
    } catch (InterruptedException e) {
      // do nothing
      xLogger.warn("InterruptedException: {0}", e.getMessage());
    }
  }
}

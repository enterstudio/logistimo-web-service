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

import com.logistimo.communications.service.MessageService;
import com.logistimo.entities.entity.IKiosk;
import com.logistimo.entities.service.EntitiesService;
import com.logistimo.entities.service.EntitiesServiceImpl;
import com.logistimo.inventory.entity.IInvntry;
import com.logistimo.inventory.service.InventoryManagementService;
import com.logistimo.inventory.service.impl.InventoryManagementServiceImpl;
import com.logistimo.users.entity.IUserAccount;

import com.logistimo.communications.MessageHandlingException;
import com.logistimo.services.ObjectNotFoundException;
import com.logistimo.services.ServiceException;
import com.logistimo.services.Services;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

/**
 * Represents the handling of messages for human-protocol
 *
 * @author Arun
 */
public class HumanProtocolMessageHandler extends MessageHandler {

  // Commands
  private static String GET_INVENTORY = "geti";
  private static String HELP = "help";
  // Limist
  private static int
      MAX_SMS_SIZE =
      152;
  // since we add a 8-letter prefix for message sequence (e.g. [msg-1])
  private static int MAX_MATERIAL_NAME_SIZE = 20;
  // Properties
  private String[] tokens = null;
  private InventoryManagementService ims = null;

  public HumanProtocolMessageHandler(String wireType, String message, String address, Date recdOn)
      throws MessageHandlingException {
    super(wireType, message, address, recdOn);
    tokens = message.split(" ");
    if (tokens == null || tokens.length == 0) {
      tokens = new String[1];
      tokens[0] = message;
    }
    // Init. services
    try {
      ims = Services.getService(InventoryManagementServiceImpl.class);
    } catch (ServiceException e) {
      throw new MessageHandlingException(e.getMessage());
    }
  }

  @Override
  public void process() throws MessageHandlingException {
    String cmd = tokens[0];
    // Process depending on the command
    try {
      if (HELP.equalsIgnoreCase(cmd)) {
        sendHelpMessage();
      } else if (GET_INVENTORY.equalsIgnoreCase(cmd)) {
        sendInventory();
      } else {
        sendMessage("Invalid command '" + cmd + "'. Please send message '" + MessageHandler.KEYWORD
            + " help' to get help.", "Invalid command");
      }
    } catch (Exception e) {
      throw new MessageHandlingException(e.getMessage());
    }
  }

  // Send Help message
  private void sendHelpMessage() throws IOException, MessageHandlingException {
    String message = "To get inventory, send: LOGI geti {username} {password}";
    sendMessage(message, "Help");
  }

  // Send inventory details
  @SuppressWarnings("unchecked")
  private void sendInventory() throws IOException, MessageHandlingException {
    // Get the services
    try {
      if (tokens.length < 3) {
        sendMessage(
            "Invalid command format. To get inventory, send 'LOGI geti <username> <password>'",
            "Invalid command");
        return;
      }
      // Get tokens
      String userId = tokens[1];
      String password = tokens[2];
      // Authenticate the user
      try {
        if (as.authenticateUser(userId, password, null) != null) {
          // Get the user object
          IUserAccount
              u =
              as.getUserAccount(userId); // NOTE: need to call this API to also get user's kiosks
          setUserDetails(userId);
          // Get the kiosk Id
          EntitiesService es = Services.getService(EntitiesServiceImpl.class);
          List<IKiosk> kiosks = (List<IKiosk>) es.getKiosksForUser(u,null,null);
          if (kiosks == null || kiosks.size() == 0) {
            sendMessage(
                "No entities associated with user '" + userId + "'. No inventory is available.",
                "No data available");
            return;
          }
          IKiosk k = kiosks.get(0);
          Long kioskId = k.getKioskId();

          // Get the inventory
          List<IInvntry>
              invlist =
              ims.getInventoryByKiosk(kioskId, null).getResults(); // TODO: pagination?
          // Get formatted message and send
          List<String> msgs = getInventoryDetails(invlist);
          int i = 0;
          for (String msg : msgs) {
            sendMessage(msg, "Inventory data " + i++);
          }
        } else {
          sendMessage("Invalid name or password. Please verify details and re-send query",
              "Could not authenticate");
        }
      } catch (ObjectNotFoundException e) {
        sendMessage(
            "Invalid username or password. Please check your information and re-send query.",
            "Could not authenticate");
      }
    } catch (ServiceException e) {
      throw new MessageHandlingException(e.getMessage());
    }
  }

  // Send error response
  @SuppressWarnings({"rawtypes", "unchecked"})
  private void sendMessage(String msg, String subject)
      throws IOException, MessageHandlingException {
    Vector messages = new Vector();
    messages.add(msg);
    sendMessages(messages, subject, MessageService.NORMAL, null);
  }

  // Get the inventory details, in the form of one or more messages
  private List<String> getInventoryDetails(List<IInvntry> list) throws ServiceException {
    List<String> msgs = new ArrayList<String>();
    if (list == null || list.size() == 0) {
      msgs.add("No inventory is available");
      return msgs;
    }
    Iterator<IInvntry> it = list.iterator();
    int i = 1;
    String msg = list.size() + " items - ";
    while (it.hasNext()) {
      IInvntry inv = it.next();
      // Get the material name
      String mname = mcs.getMaterial(inv.getMaterialId()).getName();
      if (mname.length() > MAX_MATERIAL_NAME_SIZE) {
        mname = mname.substring(0, 15);
      }
      BigDecimal stock = inv.getStock();
      String itemDetails = mname + ":" + stock;
      // Check if this message has reached the max. size for SMS
      if (MessageService.SMS.equals(wireType)) {
        if ((msg + itemDetails).length() > MAX_SMS_SIZE) {
          msg = "[msg-" + i + "] " + msg;
          i++;
          msgs.add(msg);
          msg = itemDetails;
        } else {
          msg += itemDetails;
        }
      }
      if (it.hasNext()) {
        msg += ", ";
      }
    }
    // Add the last message
    msg = "[msg-" + i + "] " + msg;
    msgs.add(msg);
    return msgs;
  }
}

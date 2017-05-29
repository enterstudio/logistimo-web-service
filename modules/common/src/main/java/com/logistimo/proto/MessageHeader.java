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

package com.logistimo.proto;

import java.util.StringTokenizer;

/**
 * Represents a machine protocol message header
 * Format: <msg-no> <no-of-msgs> <msg-id> <version> <cmd/status>
 *
 * @author Arun
 */
public class MessageHeader {

  // Version
  public static final String VERSION = "01";
  public static final String VERSION02 = "02";
  public static final String VERSION03 = "03";
  public static final String VERSION04 = "04";
  public static final String VERSION05 = "05";
  public static final String VERSION06 = "06";
  public static final String VERSION07 = "07";

  private int numMessages = 0;
  private int msgNo = 0;
  private String msgId = null;
  private String version = null;
  private String cmd = null;

  public MessageHeader(String msgId, int msgNo, int numMessages) {
    this.msgId = msgId;
    this.msgNo = msgNo;
    this.numMessages = numMessages;
  }

  public MessageHeader(String message) throws ProtocolException {
    if (message == null || message.equals("")) {
      throw new ProtocolException("Message not specified");
    }
    StringTokenizer st = new StringTokenizer(message, " ");
    if (st.countTokens() < 3) {
      throw new ProtocolException("Message has too few tokens");
    }
    // Parse the tokens
    try {
      msgNo = Integer.parseInt(st.nextToken());
      numMessages = Integer.parseInt(st.nextToken());
      msgId = st.nextToken();
      if (st.hasMoreTokens()) {
        version = st.nextToken();
      }
      if (st.hasMoreTokens()) {
        cmd = st.nextToken(); // cmd on forward msg. from mobile; status on return msg.
      }
    } catch (NumberFormatException e) {
      throw new ProtocolException(e.getMessage());
    }
  }

  public int getMessageNo() {
    return msgNo;
  }

  public int getNumberOfMessages() {
    return numMessages;
  }

  public String getMessageId() {
    return msgId;
  }

  public String getVersion() {
    return version;
  }

  public String getCommand() {
    return cmd;
  }
}


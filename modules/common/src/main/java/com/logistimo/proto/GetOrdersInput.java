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

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.logistimo.proto;

import com.logistimo.proto.utils.StringTokenizer;

import java.util.Vector;

/**
 * @author Arun
 */
public class GetOrdersInput extends InputMessageBean {

  private String kioskId = null;
  private String status = null;
  private int maxResults = 5;
  private String
      load =
      "1";
  // i.e. do not send individual order details; 0 = send individual order details
  private String otype = null; // order type

  public GetOrdersInput() {
  }

  public GetOrdersInput(String version, String userId, String password, String kioskId,
                        int maxResults, String load, String status, String otype) {
    super(userId, password, version);
    this.userId = userId;
    this.password = password;
    this.kioskId = kioskId;
    this.status = status;
    this.maxResults = maxResults;
    this.load = load;
    this.otype = otype;
  }

  public String getKioskId() {
    return kioskId;
  }

  public String getStatus() {
    return status;
  }

  public int getMaxResults() {
    return maxResults;
  }

  public String getLoad() {
    return load;
  }

  public String getOrderType() {
    return otype;
  }

  public void fromMessageString(Vector messages) throws ProtocolException {
    if (messages == null || messages.isEmpty()) {
      throw new ProtocolException("No message specified");
    }
    // Assemble
    String msg = MessageUtil.assemble(messages);
    if (msg == null || msg.length() == 0) {
      throw new ProtocolException("Message not assembled");
    }
    // Tokenize
    StringTokenizer st = new StringTokenizer(msg, " ");
    if (st.countTokens() < 7) {
      throw new ProtocolException("At least 7 tokens expected in message");
    }
    version = st.nextToken();
    String cmd = null;
    if (!RestConstantsZ.ACTION_GETORDERS.equals(cmd = st.nextToken())) {
      throw new ProtocolException(
          "Invalid command: " + cmd + ". Expected " + RestConstantsZ.ACTION_GETORDERS);
    }
    userId = st.nextToken();
    password = st.nextToken();
    kioskId = st.nextToken();
    try {
      maxResults = Integer.parseInt(st.nextToken());
    } catch (NumberFormatException e) {
      throw new ProtocolException(e.getMessage());
    }
    load = st.nextToken();
    String value = null;
    if (st.hasMoreTokens() && (value = st.nextToken()) != null && !MessageUtil.DUMMY
        .equals(value)) {
      status = value;
    }
    if (Integer.parseInt(version) >= Integer.parseInt(MessageHeader.VERSION06) && st.hasMoreTokens()
        && (value = st.nextToken()) != null && !MessageUtil.DUMMY
        .equals(value)) { // version 06 onwards
      otype = value;
    }
    if (st.hasMoreTokens() && (value = st.nextToken()) != null && !MessageUtil.DUMMY
        .equals(value)) {
      setResponseMessageNumbers(value);
    }
  }

  public Vector toMessageString() throws ProtocolException {
    String msg = version + " " + RestConstantsZ.ACTION_GETORDERS + " " +
        userId + " " + password + " " + kioskId + " " +
        maxResults + " " + load;
    if (status != null && status.length() > 0) {
      msg += " " + status;
    } else {
      msg += " " + MessageUtil.DUMMY;
    }
    if (Integer.parseInt(version) >= Integer.parseInt(MessageHeader.VERSION06)) {
      if (otype != null && !otype.equals("")) {
        msg += " " + otype;
      } else {
        msg += " " + MessageUtil.DUMMY;
      }
    }
    if (hasResponseMessageNumbers()) {
      msg += " " + getResponseMessageNumbers();
    } else {
      msg += " " + MessageUtil.DUMMY;
    }
    if (isDev) {
      msg += " d";
    }
    return MessageUtil.split(msg, msgId);
  }
}

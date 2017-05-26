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
public class GetOrderInput extends InputMessageBean {

  protected String cmd = null;
  private String orderId = null;

  public GetOrderInput() {
    cmd = RestConstantsZ.ACTION_GETORDER;
  }

  public GetOrderInput(String version, String userId, String password, String orderId) {
    super(userId, password, version);
    this.userId = userId;
    this.password = password;
    this.orderId = orderId;
    this.cmd = RestConstantsZ.ACTION_GETORDER;
  }

  public String getOrderId() {
    return orderId;
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
    if (st.countTokens() < 5) {
      throw new ProtocolException("At least 5 tokens should be present in the message");
    }
    version = st.nextToken();
    String cmdM = st.nextToken();
    if (!cmd.equals(cmdM)) {
      throw new ProtocolException("Invalid command: " + cmdM + ". Expected " + cmd);
    }
    userId = st.nextToken();
    password = st.nextToken();
    orderId = st.nextToken();
  }

  public Vector toMessageString() throws ProtocolException {
    String msg = version + " " + cmd + " " +
        userId + " " + password + " " + orderId;
    if (isDev) {
      msg += " d";
    }
    return MessageUtil.split(msg, msgId);
  }

}

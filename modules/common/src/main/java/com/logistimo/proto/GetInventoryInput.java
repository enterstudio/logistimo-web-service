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
public class GetInventoryInput extends InputMessageBean {

  private boolean onlyStock = false;
  private String kid = null; // kiosk Id of kiosk for which inventory is required

  public GetInventoryInput() {
  }

  public GetInventoryInput(String userId, String password, String kid, boolean onlyStock,
                           String version) {
    super(userId, password, version);
    this.onlyStock = onlyStock;
    this.kid = kid;
  }

  public boolean isOnlyStock() {
    return onlyStock;
  }

  public String getKioskId() {
    return kid;
  }

  public Vector toMessageString() throws ProtocolException {
    if (userId == null || userId.equals("") || password == null || password.equals("")) {
      throw new ProtocolException("UserId or password not specified");
    }
    String
        msg =
        MessageHeader.VERSION03 + " " + RestConstantsZ.ACTION_GETINVENTORY + " " + userId + " "
            + password;
    if (onlyStock) {
      msg += " 1";
    } else {
      msg += " 0";
    }
    if (kid != null) // VERSION03 feature
    {
      msg += " " + kid;
    } else {
      msg += " " + MessageUtil.DUMMY;
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

  public void fromMessageString(Vector messages) throws ProtocolException {
    if (messages == null || messages.isEmpty()) {
      throw new ProtocolException("Message not specified");
    }
    String message = MessageUtil.assemble(messages);
    if (message == null || message.length() == 0) {
      throw new ProtocolException("Message not assembled");
    }
    StringTokenizer st = new StringTokenizer(message, " ");
    if (st.countTokens() < 4) {
      throw new ProtocolException("At least 4 tokens have to be present in message");
    }
    version = st.nextToken();
    String cmdM = st.nextToken();
    if (!RestConstantsZ.ACTION_GETINVENTORY.equals(cmdM)) {
      throw new ProtocolException(
          "Invalid command: " + cmdM + ". Excepted " + RestConstantsZ.ACTION_GETINVENTORY);
    }
    userId = st.nextToken();
    password = st.nextToken();
    onlyStock = "1".equals(st.nextToken());
    if (MessageHeader.VERSION03.equals(version)) {
      kid = st.nextToken();
      if (MessageUtil.DUMMY.equals(kid)) {
        kid = null;
      }
    }
    String value = null;
    if (st.hasMoreTokens()) {
      value = st.nextToken(); // check for missing response messages that did not arrive at client
    }
    if (value != null && !MessageUtil.DUMMY.equals(value)) {
      setResponseMessageNumbers(value);
    }
  }
}

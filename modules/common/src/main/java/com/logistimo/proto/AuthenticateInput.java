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
 * Represents the input of authentication
 *
 * @author Arun
 */
public class AuthenticateInput extends InputMessageBean {
  protected String cmd = RestConstantsZ.ACTION_LOGIN;
  private boolean onlyAuthenticate = false;
  private boolean
      minResponse =
      false;
  // minimize the response data, to optimize data transfers (e.g. only send kiosks info., if more than one kiosk)
  private String locale = null;

  public AuthenticateInput() {
  }

  ;

  public AuthenticateInput(String userId, String password, boolean onlyAuthenticate,
                           boolean minResponse, String locale, String version) {
    super(userId, password, version);
    this.onlyAuthenticate = onlyAuthenticate;
    this.minResponse = minResponse;
    this.locale = locale;
  }

  public boolean isMinResponse() {
    return minResponse;
  }

  public boolean isOnlyAuthenticate() {
    return onlyAuthenticate;
  }

  public String getLocale() {
    return locale;
  }

  public Vector toMessageString() throws ProtocolException {
    if (userId == null || userId.equals("") || password == null || password.equals("")) {
      throw new ProtocolException("UserId or password not specified");
    }
    String msg = version + " " + cmd + " " + userId + " " + password;
    if (minResponse) // VERSION03 onwards feature, to send only kiosks info. if more than one kiosk
    {
      msg += " 1";
    } else {
      msg += " " + MessageUtil.DUMMY;
    }
    if (Integer.parseInt(version) >= Integer.parseInt(MessageHeader.VERSION05)) {
      // Add the locale
      if (locale != null && !locale.equals("")) {
        msg += " " + locale;
      } else {
        msg += " " + MessageUtil.DUMMY;
      }
    }
    if (Integer.parseInt(version) >= Integer.parseInt(MessageHeader.VERSION07)) {
      // Add only authenticate request, if present
      if (onlyAuthenticate) {
        msg += " 1";
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

  public void fromMessageString(Vector messages) throws ProtocolException {
    fromMessageString(messages, RestConstantsZ.ACTION_LOGIN);
  }

  protected void fromMessageString(Vector messages, String cmd) throws ProtocolException {
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
    if (!cmd.equals(cmdM)) {
      throw new ProtocolException("Invalid command: " + cmdM + ". Excepted " + cmd);
    }
    userId = st.nextToken();
    password = st.nextToken();
    String value = null;
    if (Integer.parseInt(version) >= Integer.parseInt(
        MessageHeader.VERSION03)) { // MessageHeader.VERSION03.equals( version ) || MessageHeader.VERSION04.equals( version ) ) {
      if (!MessageUtil.DUMMY.equals((value = st.nextToken()))) {
        minResponse = true;
      }
    }
    if (Integer.parseInt(version) >= Integer
        .parseInt(MessageHeader.VERSION05)) { // version 05 onwards
      // Locale
      if (st.hasMoreTokens() && !MessageUtil.DUMMY.equals((value = st.nextToken()))) {
        locale = value;
      }
    }
    if (Integer.parseInt(version) >= Integer.parseInt(MessageHeader.VERSION07)) {
      if (st.hasMoreTokens() && !MessageUtil.DUMMY.equals((value = st.nextToken()))) {
        onlyAuthenticate = true;
      }
    }
    if (st.hasMoreTokens()) {
      value = st.nextToken(); // check for missing response messages that did not arrive at client
    }
    if (value != null && !MessageUtil.DUMMY.equals(value)) {
      setResponseMessageNumbers(value);
    }
  }
}

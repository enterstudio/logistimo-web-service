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

import com.logistimo.proto.utils.ResourceBundle;
import com.logistimo.proto.utils.Resources;

import java.io.IOException;
import java.util.Vector;

/**
 * @author Arun
 */
public abstract class OutputMessageBean implements MessageBean {

  protected String msgId = null;
  protected String version = MessageHeader.VERSION02;
  protected boolean status = false;
  protected String statusCode = null;
  protected String errMsg = null;
  protected ResourceBundle protoMessages = null;
  protected String cursor = null;

  public OutputMessageBean(String locale) throws ProtocolException {
    getResources(locale);
  }

  public OutputMessageBean(boolean status, String errMsg, String locale, String version)
      throws ProtocolException {
    this.status = status;
    this.errMsg = errMsg;
    this.statusCode = (status ? JsonTagsZ.STATUS_TRUE : JsonTagsZ.STATUS_FALSE);
    this.version = version;
    getResources(locale);
  }

  public String getMessageId() {
    return msgId;
  }

  public void setMessageId(String msgId) {
    this.msgId = msgId;
  }

  public String getVersion() {
    return this.version;
  }

  public void setVersion(String version) {
    this.version = version;
  }

  public String getCursor() {
    return cursor;
  }

  public void setCursor(String cursor) {
    this.cursor = cursor;
  }

  public boolean getStatus() {
    return this.status;
  }

  public String getStatusCode() {
    return this.statusCode;
  }

  public String getMessage() {
    return this.errMsg;
  }

  private void getResources(String locale) throws ProtocolException {
    try {
      protoMessages = Resources.get().getBundle("ProtoMessages", locale);
    } catch (IOException e) {
      throw new ProtocolException(e.getMessage());
    }
  }

  // arun (3)
  // NOTE: These methods have to be defined abstract, else we get a NoSuchMethod error in some S40 phones (e.g. S40 5th edition, feature pack 1)
  // Ref: http://discussion.forum.nokia.com/forum/showthread.php?154818-java-lang-NoSuchMethodError-on-Series-40
  public abstract Vector toMessageString() throws ProtocolException;

  public abstract void fromMessageString(Vector messages) throws ProtocolException;
}

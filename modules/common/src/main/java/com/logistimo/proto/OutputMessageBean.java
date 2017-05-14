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

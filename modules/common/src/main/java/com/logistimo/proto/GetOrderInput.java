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

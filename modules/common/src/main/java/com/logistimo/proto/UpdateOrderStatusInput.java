/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.logistimo.proto;

import com.logistimo.proto.utils.StringTokenizer;

import java.util.Vector;

/**
 * Use for updating order status including canceling, marking order as fulfilled, and so on
 *
 * @author Arun
 */
public class UpdateOrderStatusInput extends InputMessageBean {

  protected String orderId = null;
  protected String status = null;

  public UpdateOrderStatusInput() {
  }

  public UpdateOrderStatusInput(String userId, String password, String orderId, String status,
                                String version) {
    super(userId, password, version);
    this.status = status;
    this.orderId = orderId;
  }

  public String getOrderId() {
    return orderId;
  }

  public String getStatus() {
    return status;
  }

  public Vector toMessageString() throws ProtocolException {
    if (userId == null || userId.equals("") || password == null || password.equals("")) {
      throw new ProtocolException("UserId or password not specified");
    }
    String
        msg =
        version + " " + RestConstantsZ.ACTION_UPDATEORDERSTATUS + " " + userId + " " + password +
            " " + orderId + " " + status;
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
      throw new ProtocolException("No messages specified");
    }
    String message = MessageUtil.assemble(messages);
    if (message == null || message.length() == 0) {
      throw new ProtocolException("Message not assembled");
    }
    StringTokenizer st = new StringTokenizer(message, " ");
    if (st.countTokens() < 6) {
      throw new ProtocolException("At least 6 tokens have to be present in message");
    }
    version = st.nextToken();
    String cmdM = st.nextToken();
    if (!RestConstantsZ.ACTION_UPDATEORDERSTATUS.equals(cmdM)) {
      throw new ProtocolException(
          "Invalid command: " + cmdM + ". Excepted " + RestConstantsZ.ACTION_UPDATEORDERSTATUS);
    }
    userId = st.nextToken();
    password = st.nextToken();
    orderId = st.nextToken();
    status = st.nextToken();
    String value = null;
    if (st.hasMoreTokens()) {
      value = st.nextToken(); // check for missing response messages that did not arrive at client
    }
    if (value != null && !MessageUtil.DUMMY.equals(value)) {
      setResponseMessageNumbers(value);
    }
  }
}

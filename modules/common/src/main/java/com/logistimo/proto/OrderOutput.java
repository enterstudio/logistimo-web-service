/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.logistimo.proto;

import org.json.JSONException;
import org.json.JSONObject;
import com.logistimo.proto.utils.StringTokenizer;

import java.util.Hashtable;
import java.util.Vector;

/**
 * Represents the output of a Get Order command
 *
 * @author Arun
 */
public class OrderOutput extends OutputMessageBean implements JsonBean {

  private Hashtable orderData = null;

  public OrderOutput(String locale) throws ProtocolException {
    super(locale);
  }

  public OrderOutput(boolean status, Hashtable orderData, String errMsg, String locale,
                     String version) throws ProtocolException {
    super(status, errMsg, locale, version);
    this.orderData = orderData;
  }

  /**
   * Load the object data from a JSON string
   */
  public void fromJSONString(String jsonString) throws ProtocolException {
    try {
      JSONObject json = new JSONObject(jsonString);
      // Get version
      this.version = (String) json.get(JsonTagsZ.VERSION);
      loadFromJSON01(json);
    } catch (JSONException e) {
      throw new ProtocolException(e.getMessage());
    }
  }

  /**
   * Convert to JSON String
   */
  public String toJSONString() throws ProtocolException {
    String jsonString = null;
    try {
      jsonString = toJSONObject01().toString();
    } catch (JSONException e) {
      throw new ProtocolException(e.getMessage());
    }

    return jsonString;
  }

  public Hashtable getOrderData() {
    return orderData;
  }

  public Vector getMaterials() {
    if (orderData != null) {
      return (Vector) orderData.get(JsonTagsZ.MATERIALS);
    }
    return null;
  }

  public boolean hasOrders() {
    if (orderData != null && orderData.get(JsonTagsZ.TRACKING_ID) != null) {
      return true;
    } else {
      return false;
    }
  }

  // Get the JSON Object
  private JSONObject toJSONObject01() throws JSONException {
    JSONObject json = null;
    if (!status) { // Failed
      json = new JSONObject();
      // Add version
      json.put(JsonTagsZ.VERSION, version);
      // Add the status code
      json.put(JsonTagsZ.STATUS, this.statusCode);
      json.put(JsonTagsZ.MESSAGE, this.errMsg);
    } else { // Success or partial success
      if (orderData == null || orderData.isEmpty()) {
        throw new JSONException(protoMessages.getString("error.noorder"));
      }
      json = JsonUtil.getOrderJSON(orderData);
      // Add version
      json.put(JsonTagsZ.VERSION, version);
      // Add the status code
      json.put(JsonTagsZ.STATUS, this.statusCode);
    }
    return json;
  }

  // Load from JSON string (ver 01)
  private void loadFromJSON01(JSONObject json) throws JSONException {
    // Get status
    statusCode = (String) json.get(JsonTagsZ.STATUS);
    status = JsonTagsZ.STATUS_TRUE.equals(statusCode);
    if (!status) {
      errMsg = (String) json.get(JsonTagsZ.MESSAGE);
    } else {
      // Get order metadata
      orderData = JsonUtil.getOrder(json);
    }
  }

  // NOTE: est. fulfillment times, confirmed fulfillment time, payment option, package size not available in SMS
  public void fromMessageString(Vector messages) throws ProtocolException {
    if (messages == null || messages.isEmpty()) {
      throw new ProtocolException("No message specified");
    }
    // Assemble message
    String msg = MessageUtil.assemble(messages);
    if (msg == null || msg.length() == 0) {
      throw new ProtocolException("Message not assembled");
    }
    // Tokenize
    StringTokenizer st = new StringTokenizer(msg, " ");
    if (st.countTokens() < 3) {
      throw new ProtocolException("At least 3 tokens are expected");
    }
    version = st.nextToken();
    statusCode = st.nextToken();
    status = JsonTagsZ.STATUS_TRUE.equals(statusCode);
    if (!status) {
      errMsg = MessageUtil.decode(st.nextToken());
    } else {
      orderData = MessageUtil.getOrderObject(st, version);
    }
  }

  // NOTE: est. fulfillment times, confirmed fulfillment time, payment option, package size not available in SMS
  public Vector toMessageString() throws ProtocolException {
    // Add version
    String msg = version + " " + statusCode;
    if (!status) { // Failed
      if (errMsg == null && (orderData == null || orderData.isEmpty())) {
        errMsg = protoMessages.getString("error.noorder");
      }
      msg += " " + MessageUtil.encode(errMsg);
    } else { // Success or partial success
      msg += " " + MessageUtil.getOrderString(orderData, version);
    }
    return MessageUtil.split(msg, getMessageId());
  }
}

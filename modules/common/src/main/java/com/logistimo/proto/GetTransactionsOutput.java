/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.logistimo.proto;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Vector;

/**
 * @author Chitra Char
 */
public class GetTransactionsOutput extends OutputMessageBean implements JsonBean {
  String kioskId = null;
  Vector transactionList = null; // List of hashtable

  public GetTransactionsOutput(String locale) throws ProtocolException {
    super(locale);
  }

  public GetTransactionsOutput(boolean status, String kioskId, Vector transactionList,
                               String errMsg, String locale, String version)
      throws ProtocolException {
    super(status, errMsg, locale, version);
    this.kioskId = kioskId;
    this.transactionList = transactionList;
  }

  // Accessor methods
  public String getKioskId() {
    return kioskId;
  }

  public void setKioskId(String kioskId) {
    this.kioskId = kioskId;
  }

  public Vector getTransactionList() {
    return this.transactionList;
  }

  /**
   * Load data from a JSON string
   */
  public void fromJSONString(String jsonString) throws ProtocolException {

    try {
      JSONObject json = new JSONObject(jsonString);
      try {
        this.version = (String) json.get(JsonTagsZ.VERSION);
      } catch (JSONException e) {
        // version not present; do nothing
      }
      // Load data from JSON, as per version
      loadFromJSON(json);
    } catch (JSONException e) {
      throw new ProtocolException(e.getMessage());
    }
  }

  // Convert to JSONString
  public String toJSONString() throws ProtocolException {
    String jsonString = null;
    try {
      jsonString = toJSONObject().toString();
    } catch (JSONException e) {
      throw new ProtocolException(e.getMessage());
    }
    return jsonString;
  }

  // Load from a plain text message string
  public void fromMessageString(Vector messages) throws ProtocolException {
    throw new ProtocolException("Not supported yet.");
  }

  public Vector toMessageString() throws ProtocolException {
    throw new ProtocolException("Not supported yet.");
  }

  // Get JSON Object in new version (01)
  private JSONObject toJSONObject() throws JSONException {
    JSONObject json = new JSONObject();
    // Add version
    if (version != null && !version.equals("")) {
      json.put(JsonTagsZ.VERSION, version);
    } else {
      json.put(JsonTagsZ.VERSION, "01");
    }
    // Add status
    json.put(JsonTagsZ.STATUS, statusCode);
    // Add cursor, if any
    if (cursor != null && !cursor.equals("")) {
      json.put(JsonTagsZ.CURSOR, cursor);
    }
    if (!status) {
      // Failure
      json.put(JsonTagsZ.MESSAGE, errMsg);
    } else {
      // Success
      // Add currency
      if (kioskId != null) {
        json.put(JsonTagsZ.KIOSK_ID, kioskId);
      }
      // Add transactions
      JsonUtil.addTransactionData(json, transactionList);
    }

    return json;
  }

  // Load from newer version (01) of JSON
  private void loadFromJSON(JSONObject json) throws JSONException {
    // Get status
    statusCode = (String) json.get(JsonTagsZ.STATUS);
    status = JsonTagsZ.STATUS_TRUE.equals(statusCode);
    try {
      cursor = json.getString(JsonTagsZ.CURSOR);
    } catch (Exception e) {
      // ignore
    }
    if (!status) {
      errMsg = (String) json.get(JsonTagsZ.MESSAGE);
    } else {
      try {
        kioskId = (String) json.get(JsonTagsZ.KIOSK_ID);
      } catch (JSONException e) {
        // do nothing
      }
      transactionList = JsonUtil.getTransactionData(json);
    }
  }
}

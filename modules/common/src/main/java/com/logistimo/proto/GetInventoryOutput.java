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

/**
 *
 */
package com.logistimo.proto;

import org.json.JSONException;
import org.json.JSONObject;
import com.logistimo.proto.utils.StringTokenizer;

import java.util.Vector;

/**
 * @author arun
 *
 *         Represents the output from Get Inventory REST API / Plain Message
 */
public class GetInventoryOutput extends OutputMessageBean implements JsonBean {

  private Vector inventoryList = null; // list of Hashtable
  private String currency = null;
  private Tags tags = null;
  private boolean onlyStock = false;

  public GetInventoryOutput(String locale) throws ProtocolException {
    super(locale);
    this.tags = new Tags(protoMessages);
  }

  public GetInventoryOutput(boolean status, Vector inventoryList, String currency, String errMsg,
                            boolean onlyStock, String locale, String version)
      throws ProtocolException {
    super(status, errMsg, locale, version);
    this.inventoryList = inventoryList;
    this.currency = currency;
    this.onlyStock = onlyStock;
    this.tags = new Tags(protoMessages);
  }

  // Accessor methods
  public Vector getMaterials() {
    return this.inventoryList;
  }

  public String getCurrency() {
    return currency;
  }

  public Tags getTags() {
    return tags;
  }

  public boolean hasTags() {
    return tags != null && tags.hasTags();
  }

  public boolean isOnlyStock() {
    return onlyStock;
  }

  public void setOnlyStock(boolean onlyStock) {
    this.onlyStock = onlyStock;
  }

  /**
   * Load data from a JSON string
   */
  public void fromJSONString(String jsonString) throws ProtocolException {
    this.tags = new Tags(protoMessages);
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
    if (messages == null || messages.isEmpty()) {
      throw new ProtocolException("Message not specified");
    }
    String message = MessageUtil.assemble(messages);
    if (message == null || message.length() == 0) {
      throw new ProtocolException("No message specified");
    }
    // Tokenize
    StringTokenizer st = new StringTokenizer(message, " ");
    if (st.hasMoreTokens()) {
      version = st.nextToken();
    }
    if (st.hasMoreTokens()) {
      statusCode = st.nextToken();
    }
    status = JsonTagsZ.STATUS_TRUE.equals(statusCode);
    if (!status) {
      errMsg = MessageUtil.decode(st.nextToken());
    } else {
      // Get currency
      if (st.hasMoreTokens()) {
        currency = st.nextToken();
      }
      if (MessageUtil.DUMMY.equals(currency)) {
        currency = null;
      }
      // Get materials
      inventoryList = MessageUtil.getMaterialsVector(st, tags);
    }
  }

  public Vector toMessageString() throws ProtocolException {
    String msg = "";
    msg += version; // version
    msg += " " + statusCode; // status
    if (!status) {
      msg += " " + MessageUtil.encode(errMsg);
    } else {
      // Currency
      String cur = MessageUtil.DUMMY;
      if (currency != null && !currency.equals("")) {
        cur = currency;
      }
      msg += " " + cur;
      // Materials
      int fields = MessageUtil.ALL_FIELDS;
      if (onlyStock) {
        fields = MessageUtil.QUANTITY_ONLY;
      }
      msg += " " + MessageUtil.getMaterialsString(inventoryList, fields, false); // material info.
    }
    return MessageUtil.split(msg, getMessageId());
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
      if (currency != null) {
        json.put(JsonTagsZ.CURRENCY, currency);
      }
      // Add materials
      // if ( inventoryList == null || inventoryList.isEmpty() )
      //    throw new JSONException( protoMessages.getString( "error.nomaterials" ) );
      JsonUtil.addMaterialData(json, inventoryList);
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
      inventoryList = JsonUtil.getMaterialData(json, tags);
      try {
        currency = (String) json.get(JsonTagsZ.CURRENCY);
      } catch (JSONException e) {
        // do nothing
      }
    }
  }
}

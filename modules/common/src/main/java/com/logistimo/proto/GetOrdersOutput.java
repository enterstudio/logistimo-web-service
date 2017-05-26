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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.logistimo.proto.utils.StringTokenizer;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

/**
 * Represents the output of a Get Orders command
 *
 * @author Arun
 */
public class GetOrdersOutput extends OutputMessageBean implements JsonBean {

  // Modes
  private static final String MODE_ORDER = "o";
  private static final String MODE_DEMAND = "d";

  private Vector orders = null;
  private Vector materials = null;
  private boolean hasOrders = false; // arun (3) - earlier was set to true
  private String currency = null;

  public GetOrdersOutput(String locale) throws ProtocolException {
    super(locale);
  }

  public GetOrdersOutput(
      boolean status, Vector ordersOrMaterials, String errMsg,
      boolean hasOrders, String currency, String locale, String version) throws ProtocolException {
    super(status, errMsg, locale, version);
    this.hasOrders = hasOrders;
    this.currency = currency;
    if (hasOrders) {
      this.orders = ordersOrMaterials;
    } else {
      this.materials = ordersOrMaterials;
    }
    if (!status && errMsg == null) {
      if (hasOrders) {
        if (orders == null || orders.isEmpty()) {
          this.errMsg = protoMessages.getString("error.noorders");
        }
      } else {
        if (materials == null || materials.isEmpty()) {
          this.errMsg = protoMessages.getString("error.nomaterials");
        }
      }
    }
  }

  public Vector getOrders() {
    return this.orders;
  }

  public Vector getMaterials() {
    return this.materials;
  }

  public boolean hasOrders() {
    return this.hasOrders;
  }

  /**
   * Load the object from the JSON string
   */
  public void fromJSONString(String jsonString) throws ProtocolException {
    try {
      JSONObject json = new JSONObject(jsonString);
      // Get the version
      this.version = (String) json.get(JsonTagsZ.VERSION);
      if ("01".equals(this.version)) {
        loadFromJSON01(json);
      } else {
        throw new ProtocolException("Unsupported protocol version: " + this.version);
      }
    } catch (JSONException e) {
      throw new ProtocolException(e.getMessage());
    }
  }

  /**
   * Get the JSON String representation of this object
   */
  public String toJSONString() throws ProtocolException {
    String jsonString = null;
    try {
      if ("01".equals(this.version)) {
        jsonString = toJSONObject01().toString();
      } else {
        throw new ProtocolException("Unsupported version: " + this.version);
      }
    } catch (JSONException e) {
      throw new ProtocolException(e.getMessage());
    }
    return jsonString;
  }

  // Get the JSON object represenation for version 01
  private JSONObject toJSONObject01() throws JSONException {
    JSONObject json = new JSONObject();
    // Add the version
    json.put(JsonTagsZ.VERSION, version);
    // Add the status
    json.put(JsonTagsZ.STATUS, this.statusCode);
    // Add cursor, if any
    if (cursor != null && !cursor.equals("")) {
      json.put(JsonTagsZ.CURSOR, cursor);
    }
    if (status) {
      // Add the orders
      if (hasOrders) {
        addOrders(json);
      } else {
        addMaterials(json);
        // Add currency
        if (this.currency != null) {
          json.put(JsonTagsZ.CURRENCY, this.currency);
        }
      }
    } else {
      json.put(JsonTagsZ.MESSAGE, this.errMsg);
    }

    return json;
  }

  // Add orders to JSON in version 01 format
  private void addOrders(JSONObject json) throws JSONException {
    JSONArray array = new JSONArray();
    if (this.orders != null && !this.orders.isEmpty()) {
      Enumeration en = this.orders.elements();
      while (en.hasMoreElements()) {
        // Form a new order JSON container
        JSONObject o = JsonUtil.getOrderJSON((Hashtable) en.nextElement());
        // Add to array
        array.put(o);
      }
    }
    // Add the array to JSON container
    json.put(JsonTagsZ.ORDERS, array);
  }

  // Load orders from the JSON string in ver. 01 format
  private void loadOrders(JSONObject json) throws JSONException {
    // Get the JSON array
    JSONArray array = (JSONArray) json.get(JsonTagsZ.ORDERS);
    this.orders = new Vector();
    // Get the orders
    for (int i = 0; i < array.length(); i++) {
      // Form the order
      Hashtable order = JsonUtil.getOrder((JSONObject) array.get(i));
      // Add to vector
      this.orders.addElement(order);
    }
  }

  // Add materials, if no orders present
  private void addMaterials(JSONObject json) throws JSONException {
    // if ( this.materials == null || this.materials.size() == 0 )
    //  throw new JSONException( protoMessages.getString( "error.nomaterials" ) );
    JsonUtil.addMaterialData(json, materials);
  }

  // Load materials
  private void loadMaterials(JSONObject json) throws JSONException {
    this.materials = JsonUtil.getMaterialData(json);
  }

  // Load data from the given JSON in 01 format
  private void loadFromJSON01(JSONObject json) throws JSONException {
    // Get the status code
    this.statusCode = (String) json.get(JsonTagsZ.STATUS);
    this.status = JsonTagsZ.STATUS_TRUE.equals(this.statusCode);
    try {
      cursor = json.getString(JsonTagsZ.CURSOR);
    } catch (Exception e) {
      // ignore
    }
    if (status) {
      // Check if orders are present
      try {
        json.get(JsonTagsZ.ORDERS);
        // Has orders
        this.hasOrders = true;
        loadOrders(json);
      } catch (JSONException e) {
        // Does not have orders
        this.hasOrders = false;
        loadMaterials(json);
        // Get currency, if available
        try {
          this.currency = (String) json.get(JsonTagsZ.CURRENCY);
        } catch (JSONException e1) {
          // do nothing
        }
      }
    } else {
      this.errMsg = (String) json.get(JsonTagsZ.MESSAGE);
    }
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
    if (st.countTokens() < 3) {
      throw new ProtocolException("At least 3 tokens expected in message");
    }
    version = st.nextToken();
    statusCode = st.nextToken();
    status = JsonTagsZ.STATUS_TRUE.equals(statusCode);
    if (!status) {
      errMsg = MessageUtil.decode(st.nextToken());
    } else {
      hasOrders = MODE_ORDER.equals(st.nextToken()); // mode
      if (hasOrders) {
        int numOrders = Integer.parseInt(st.nextToken()); // number of orders
        orders = new Vector();
        for (int i = 0; i < numOrders; i++) {
          orders.addElement(MessageUtil.getOrderObject(st, version));
        }
      } else {
        currency = st.nextToken();
        if (MessageUtil.DUMMY.equals(currency)) {
          currency = null;
        }
        materials = MessageUtil.getMaterialsVector(st, null);
      }
    }
  }

  public Vector toMessageString() throws ProtocolException {
    String msg = version + " " + statusCode;
    if (!status) {
      if (errMsg == null) {
        if (hasOrders) {
          if (orders == null || orders.isEmpty()) {
            errMsg = protoMessages.getString("error.noorders");
          }
        } else {
          if (materials == null || materials.isEmpty()) {
            errMsg = protoMessages.getString("error.nomaterials");
          }
        }
      }
      msg += " " + MessageUtil.encode(errMsg);
    } else {
      if (hasOrders) {
        msg += " " + MODE_ORDER;
        int numOrders = orders.size();
        msg += " " + numOrders;
        Enumeration en = orders.elements();
        while (en.hasMoreElements()) {
          Hashtable ord = (Hashtable) en.nextElement();
          if (ord == null || ord.isEmpty()) {
            throw new ProtocolException(protoMessages.getString("error.noorder"));
          }
          msg += " " + MessageUtil.getOrderString(ord, version);
        }
      } else {
        msg += " " + MODE_DEMAND;
        String cur = MessageUtil.DUMMY;
        if (currency != null && currency.length() > 0) {
          cur = currency;
        }
        msg += " " + cur; // currency
        ////int numMaterials = materials.size();
        ////msg += " " + numMaterials; // num. materials
        msg +=
            " " + MessageUtil
                .getMaterialsString(materials, MessageUtil.QUANTITY_AND_PRICE, true); // materials
      }
    }
    return MessageUtil.split(msg, getMessageId());
  }
}
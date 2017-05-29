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

import java.util.Enumeration;
import java.util.Vector;

/**
 * @author Vani
 *
 *         Represents the output JSON of User-Entity create/update operation
 */
public class SetupDataOutput extends OutputMessageBean implements JsonBean {

  private String userId = null;
  private String entityId = null;
  private Vector errors = null;

  public SetupDataOutput(String locale) throws ProtocolException {
    super(locale);
  }

  public SetupDataOutput(boolean status, String userId, String entityId, String errMsg,
                         Vector errors, String locale, String version) throws ProtocolException {
    super(status, errMsg, locale, version);
    this.userId = userId;
    this.entityId = entityId;
    this.errors = errors;
  }

  // Accessor methods
  public String getUserId() {
    return userId;
  }

  public String getEntityId() {
    return entityId;
  }

  public Vector getErrors() {
    return errors;
  }

  public void fromMessageString(Vector messages) throws ProtocolException {
  }

  public Vector toMessageString() throws ProtocolException {
    return null;
  }

  public void fromJSONString(String jsonString) throws ProtocolException {
    try {
      JSONObject json = new JSONObject(jsonString);
      try {
        this.version = (String) json.get(JsonTagsZ.VERSION);
      } catch (JSONException e) {
        // do nothing, if no version is present
      }
      loadFromJSON(json);
    } catch (JSONException e) {
      throw new ProtocolException(e.getMessage());
    }
  }

  public String toJSONString() throws ProtocolException {
    String jsonString = null;
    try {
      jsonString = toJSONObject().toString();
    } catch (JSONException e) {
      throw new ProtocolException(e.getMessage());
    }
    return jsonString;
  }

  // Get the JSON object
  private JSONObject toJSONObject() throws JSONException {
    JSONObject json = new JSONObject();
    // Add version
    json.put(JsonTagsZ.VERSION, version);
    // Add the status code
    json.put(JsonTagsZ.STATUS, this.statusCode);
    if (JsonTagsZ.STATUS_FALSE.equals(this.statusCode)) {
      // Error message
      json.put(JsonTagsZ.MESSAGE, this.errMsg);
      // Errors
      if (errors != null && !errors.isEmpty()) {
        addErrorData(json, this.errors);
      }
    } else { // success
      // Add userId
      json.put(JsonTagsZ.USER_ID, this.userId);
      // Add kiosk/entity id
      json.put(JsonTagsZ.KIOSK_ID, this.entityId);
    }
    return json;
  }

  private void addErrorData(JSONObject json, Vector errors) throws JSONException {
    JSONArray array = new JSONArray();
    Enumeration en = errors.elements();
    while (en.hasMoreElements()) {
      array.put(en.nextElement());
    }
    // Add to json object
    json.put(JsonTagsZ.ERRORS, array);
  }

  private void loadFromJSON(JSONObject json) throws JSONException {
    // Get the status code
    this.statusCode = (String) json.get(JsonTagsZ.STATUS);
    if (JsonTagsZ.STATUS_TRUE.equals(this.statusCode)) {
      this.status = true;
    } else {
      this.status = false;
    }

    if (!status) {
      this.errMsg = (String) json.opt(JsonTagsZ.MESSAGE);
      this.errors = getErrorData(json);
    } else {
      // Load the userId
      this.userId = (String) json.get(JsonTagsZ.USER_ID);
      // Load the kiosk Id
      this.entityId = (String) json.get(JsonTagsZ.KIOSK_ID);
    }
  }

  private Vector getErrorData(JSONObject json) throws JSONException {
    Vector errors = new Vector();
    JSONArray array = (JSONArray) json.opt(JsonTagsZ.ERRORS);
    for (int i = 0; i < array.length(); i++) {
      String e = (String) array.get(i);
      // Add to vector
      errors.addElement(e.toString());
    }
    return errors;
  }
}

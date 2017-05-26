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
 * @author vani
 */
public class BasicOutput extends OutputMessageBean implements JsonBean {
  private Vector errors;

  public BasicOutput(String locale) throws ProtocolException {
    super(locale);
  }

  public BasicOutput(boolean status, String errMsg, Vector errors, String locale,
                     String version) throws ProtocolException {
    super(status, errMsg, locale, version);
    this.errors = errors;
  }

  public void fromJSONString(String jsonString) throws ProtocolException {
    try {
      JSONObject json = new JSONObject(jsonString);
      try {
        this.version = (String) json.get(JsonTagsZ.VERSION);
      } catch (JSONException e) {
        // do nothing, if no version is present
      }
      // Get the status code
      this.statusCode = (String) json.get(JsonTagsZ.STATUS);
      if (JsonTagsZ.STATUS_TRUE.equals(this.statusCode)) {
        this.status = true;
        // In this case there is no message
      } else {
        this.status = false;
      }
      // Get message, if any. Message is present only if status is false
      try {
        this.errMsg = (String) json.get(JsonTagsZ.MESSAGE);
      } catch (JSONException e) {
        // do nothing
      }

      try {
        // Get Vector of error messages if any. Error messages are present if status is false
        this.errors = getErrorData(json);
      } catch (JSONException e) {
        // do nothing
      }
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

  // Get the JSON object in the newer (01) format
  private JSONObject toJSONObject() throws JSONException {
    JSONObject json = new JSONObject();
    // Add version
    json.put(JsonTagsZ.VERSION, version);
    // Add the status code
    json.put(JsonTagsZ.STATUS, this.statusCode);
    // If status is failed, then add message
    if (!status) { // Failed
      json.put(JsonTagsZ.MESSAGE, this.errMsg);
      // Errors
      if (this.errors != null && !this.errors.isEmpty()) {
        addErrorData(json);
      }
    }
    return json;
  }

  private void addErrorData(JSONObject json) throws JSONException {
    JSONArray array = new JSONArray();
    Enumeration en = errors.elements();
    while (en.hasMoreElements()) {
      array.put(en.nextElement());
    }
    // Add to json object
    json.put(JsonTagsZ.ERRORS, array);
  }

  private Vector getErrorData(JSONObject json) throws JSONException {
    Vector errMsgs = new Vector();
    JSONArray array = (JSONArray) json.opt(JsonTagsZ.ERRORS);
    if (array != null) {
      for (int i = 0; i < array.length(); i++) {
        String e = (String) array.get(i);
        // Add to vector
        errMsgs.addElement(e.toString());
      }
    }
    return errMsgs;
  }

  public void fromMessageString(Vector messages) throws ProtocolException {
  }

  public Vector toMessageString() throws ProtocolException {
    return null;
  }
}

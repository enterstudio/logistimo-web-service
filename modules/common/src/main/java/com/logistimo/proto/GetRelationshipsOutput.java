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

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Vector;

/**
 * Embeds customers/vendors
 *
 * @author Arun
 */
public class GetRelationshipsOutput extends OutputMessageBean implements JsonBean {

  Vector relationships = null;
  String relationshipType = null;

  public GetRelationshipsOutput(boolean status, String relationshipType, Vector relationships,
                                String cursor, String errMsg, String locale, String version)
      throws ProtocolException {
    super(status, errMsg, locale, version);
    this.relationships = relationships;
    this.relationshipType = relationshipType;
    super.cursor = cursor;
  }

  public void fromJSONString(String jsonString) throws ProtocolException {
    try {
      JSONObject json = new JSONObject(jsonString);
      // Get version
      try {
        this.version = (String) json.get(JsonTagsZ.VERSION);
      } catch (JSONException e) {
        // version not present; do nothing
      }
      // Get status
      this.statusCode = (String) json.get(JsonTagsZ.STATUS);
      this.status = JsonTagsZ.STATUS_TRUE.equals(this.statusCode);
      // Get cursor, if any
      try {
        cursor = json.getString(JsonTagsZ.CURSOR);
      } catch (Exception e) {
        // ignore
      }
      if (this.status) {
        this.relationshipType = json.getString(JsonTagsZ.TYPE);
        this.relationships = JsonUtil.getRelatedKioskData(json, JsonTagsZ.KIOSKS);
      } else {
        // Get the error message
        this.errMsg = (String) json.get(JsonTagsZ.MESSAGE);
      }
    } catch (Exception e) {
      throw new ProtocolException(e.getMessage());
    }
  }

  public String toJSONString() throws ProtocolException {
    try {
      JSONObject json = new JSONObject();
      // Add version
      if (version != null && !version.equals("")) {
        json.put(JsonTagsZ.VERSION, version);
      } else {
        json.put(JsonTagsZ.VERSION, "01");
      }
      // Add status code
      json.put(JsonTagsZ.STATUS, statusCode);
      if (cursor != null && !cursor.equals("")) {
        json.put(JsonTagsZ.CURSOR, cursor);
      }
      if (status) {
        // Add relationship type and list
        json.put(JsonTagsZ.TYPE, relationshipType);
        JsonUtil.addRelatedKioskData(json, relationships, JsonTagsZ.KIOSKS);
      } else {
        // Send error message
        json.put(JsonTagsZ.MESSAGE, this.errMsg);
      }
      return json.toString();
    } catch (Exception e) {
      throw new ProtocolException(e.getMessage());
    }
  }

  public Vector toMessageString() throws ProtocolException {
    return null;
  }

  public void fromMessageString(Vector messages) throws ProtocolException {
  }

}

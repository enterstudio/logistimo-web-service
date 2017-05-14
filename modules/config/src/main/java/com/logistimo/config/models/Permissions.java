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
package com.logistimo.config.models;

import org.json.JSONException;
import org.json.JSONObject;
import com.logistimo.logger.XLog;

/**
 * @author Arun
 */
//Class the represents relationship permissions
public class Permissions {

  // Relationships key
  public static final String RELATIONSHIPS = "lnks"; // link permissions
  // Entity keys
  public static final String INVENTORY = "inv";
  public static final String ORDERS = "ord";
  public static final String MASTER = "mst";
  // Operations
  public static final String OP_VIEW = "vw";
  public static final String OP_TRANSACT = "tr";
  public static final String OP_MANAGE = "mg";
  private static final XLog xLogger = XLog.getLog(Permissions.class);
  private JSONObject prsJson = null;

  public Permissions() {
    prsJson = new JSONObject();
  }

  public Permissions(String jsonString) throws ConfigurationException {
    try {
      prsJson = new JSONObject(jsonString);
    } catch (JSONException e) {
      throw new ConfigurationException(e.getMessage());
    }
  }

  public boolean hasAccess(String linkType, String entityKey, String operation) {
    try {
      JSONObject lnkPerms = prsJson.getJSONObject(RELATIONSHIPS);
      JSONObject typePerms = lnkPerms.getJSONObject(linkType);
      JSONObject perms = typePerms.getJSONObject(entityKey);
      return "1".equals(perms.getString(operation));
    } catch (JSONException e) {
      xLogger
          .warn("JSON Exception while getting permissions: entityKey = {0}, operation = {1}: {2}",
              entityKey, operation, e.getMessage());
      return false;
    }
  }

  public void putAccess(String linkType, String entityKey, String operation, boolean hasAccess) {
    try {
      JSONObject lnkPerms = null, lnkTypePerms = null, perms = null;
      try {
        lnkPerms = prsJson.getJSONObject(RELATIONSHIPS);
      } catch (JSONException e) {
        lnkPerms = new JSONObject();
        prsJson.put(RELATIONSHIPS, lnkPerms);
      }
      try {
        lnkTypePerms = lnkPerms.getJSONObject(linkType);
      } catch (JSONException e) {
        lnkTypePerms = new JSONObject();
        lnkPerms.put(linkType, lnkTypePerms);
      }
      try {
        perms = lnkTypePerms.getJSONObject(entityKey);
      } catch (JSONException e) {
        perms = new JSONObject();
        lnkTypePerms.put(entityKey, perms);
      }
      String p = (hasAccess ? "1" : "0");
      perms.put(operation, p);
    } catch (JSONException e) {
      xLogger.severe(
          "JSON Exception while updating permissions: entityKey = {0}, operation = {1}: {2}",
          entityKey, operation, e.getMessage());
    }
  }

  public String toJSONString() {
    return prsJson.toString();
  }
}

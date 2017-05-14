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

package com.logistimo.config.models;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Created by vani on 03/08/15.
 */
public class SupportConfig implements Serializable {
  // Tags
  public static final String SUPPORT_USER_ROLE = "supportuserrole";
  public static final String SUPPORT_USER = "supportuser";
  public static final String SUPPORT_USER_NAME = "supportusername";
  public static final String SUPPORT_EMAIL = "supportemail";
  public static final String SUPPORT_PHONE = "supportphone";
  private static final long serialVersionUID = 1L;
  // Support userId
  private String supportUser = null;
  // Support email
  private String supportEmail = null;
  // Support phone number
  private String supportPhone = null;
  // Support user role
  private String supportUserRole = null;
  // Support user name
  private String supportUserName = null;

  public SupportConfig() {

  }

  public SupportConfig(JSONObject json) {
    // support user
    try {
      supportUser = json.getString(SUPPORT_USER);
    } catch (JSONException e) {
      // do nothing
    }
    // support email
    try {
      supportEmail = json.getString(SUPPORT_EMAIL);
    } catch (JSONException e) {
      // do nothing
    }
    // support phone
    try {
      supportPhone = json.getString(SUPPORT_PHONE);
    } catch (JSONException e) {
      // do nothing
    }
    // support user name
    try {
      supportUserName = json.getString(SUPPORT_USER_NAME);
    } catch (JSONException e) {
      // do nothing
    }
    // support user role
    try {
      supportUserRole = json.getString(SUPPORT_USER_ROLE);
    } catch (JSONException e) {
      // do nothing
    }
  }

  @SuppressWarnings("rawtypes")
  public static Map<String, SupportConfig> getSupportMap(JSONObject json) {
    Map<String, SupportConfig> map = new HashMap<String, SupportConfig>();
    // Get the roles
    Iterator keys = json.keys();
    while (keys.hasNext()) {
      String key = (String) keys.next();
      try {
        map.put(key, new SupportConfig(json.getJSONObject(key)));
      } catch (Exception e) {
        // do nothing
      }
    }
    return map;
  }

  public static JSONObject getSupportJSON(Map<String, SupportConfig> map) {
    if (map == null || map.isEmpty()) {
      return null;
    }
    JSONObject json = new JSONObject();
    Iterator<String> it = map.keySet().iterator();
    while (it.hasNext()) {
      String key = it.next();
      try {
        json.put(key, map.get(key).toJSONObject());
      } catch (Exception e) {
        // do nothing
      }
    }
    return json;
  }

  public String getSupportUser() {
    return supportUser;
  }

  public void setSupportUser(String supportUser) {
    this.supportUser = supportUser;
  }

  public String getSupportEmail() {
    return supportEmail;
  }

  public void setsupportEmail(String supportEmail) {
    this.supportEmail = supportEmail;
  }

  public String getSupportPhone() {
    return supportPhone;
  }

  public void setSupportPhone(String supportPhone) {
    this.supportPhone = supportPhone;
  }

  public String getSupportUserName() {
    return supportUserName;
  }

  public void setSupportUserName(String supportUserName) {
    this.supportUserName = supportUserName;
  }

  public String getSupportUserRole() {
    return supportUserRole;
  }

  public void setSupportUserRole(String supportUserRole) {
    this.supportUserRole = supportUserRole;
  }

  public Object toJSONObject() throws ConfigurationException {
    JSONObject json = null;
    try {
      json = new JSONObject();
      if (supportUser != null) {
        json.put(SUPPORT_USER, supportUser);
      }
      if (supportEmail != null) {
        json.put(SUPPORT_EMAIL, supportEmail);
      }
      if (supportPhone != null) {
        json.put(SUPPORT_PHONE, supportPhone);
      }
      if (supportUserName != null) {
        json.put(SUPPORT_USER_NAME, supportUserName);
      }
      if (supportUserRole != null) {
        json.put(SUPPORT_USER_ROLE, supportUserRole);
      }
    } catch (JSONException e) {
      throw new ConfigurationException(e.getMessage());
    }
    return json;
  }
}

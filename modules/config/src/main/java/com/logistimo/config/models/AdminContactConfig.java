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


public class AdminContactConfig implements Serializable {

  public static final String ADMIN_USER_ROLE = "role";
  public static final String ADMIN_USER = "usrid";
  public static final String ADMIN_USER_NAME = "usrname";
  public static final String ADMIN_EMAIL = "em";
  public static final String ADMIN_PHONE = "phnm";
  protected static final long serialVersionUID = 1l;
  private String usrid;

  private String usrname;

  private String role;

  private String em;

  private String phnm;


  public AdminContactConfig() {
  }

  public AdminContactConfig(JSONObject json) {
    try {
      usrid = json.getString(ADMIN_USER);
    } catch (JSONException e) {
      // do nothing
    }
    try {
      usrname = json.getString(ADMIN_USER_NAME);
    } catch (JSONException e) {
      // do nothing
    }
    try {
      role = json.getString(ADMIN_USER_ROLE);
    } catch (JSONException e) {
      // do nothing
    }
    try {
      em = json.getString(ADMIN_EMAIL);
    } catch (JSONException e) {
      // do nothing
    }
    try {
      phnm = json.getString(ADMIN_PHONE);
    } catch (JSONException e) {
      // do nothing
    }
  }

  @SuppressWarnings("rawtypes")
  public static Map<String, AdminContactConfig> getAdminContactMap(JSONObject json) {
    Map<String, AdminContactConfig> map = new HashMap<>();
    // Get the roles
    Iterator keys = json.keys();
    while (keys.hasNext()) {
      String key = (String) keys.next();
      try {
        map.put(key, new AdminContactConfig(json.getJSONObject(key)));
      } catch (Exception e) {
        // do nothing
      }
    }
    return map;
  }

  public static JSONObject getAdminContactJSON(Map<String, AdminContactConfig> map) {
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

  public String getUsrid() {
    return usrid;
  }

  public void setUsrid(String usrid) {
    this.usrid = usrid;
  }

  public String getUsrname() {
    return usrname;
  }

  public void setUsrname(String usrname) {
    this.usrname = usrname;
  }

  public String getRole() {
    return role;
  }

  public void setRole(String role) {
    this.role = role;
  }

  public String getEm() {
    return em;
  }

  public void setEm(String em) {
    this.em = em;
  }

  public String getPhnm() {
    return phnm;
  }

  public void setPhnm(String phnm) {
    this.phnm = phnm;
  }

  public Object toJSONObject() throws ConfigurationException {
    JSONObject json = null;
    try {
      json = new JSONObject();
      if (usrid != null) {
        json.put(ADMIN_USER, usrid);
      }
      if (usrname != null) {
        json.put(ADMIN_USER_NAME, usrid);
      }
      if (role != null) {
        json.put(ADMIN_USER_ROLE, role);
      }
      if (em != null) {
        json.put(ADMIN_EMAIL, em);
      }
      if (phnm != null) {
        json.put(ADMIN_PHONE, phnm);
      }
    } catch (JSONException e) {
      throw new ConfigurationException(e.getMessage());
    }
    return json;
  }

}

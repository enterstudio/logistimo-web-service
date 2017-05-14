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
 * Created by mohansrinivas on 11/12/15.
 */
public class MatStatusConfig implements Serializable {
  // Tags
  public static final String TRANSACTION_TYPE = "type";
  public static final String DEFAULT_STATUS = "df";
  public static final String TEMPERATURE_SENSITIVE_MATERIAL_STATUS = "etsm";
  public static final String STATUS = "mtst";
  private static final long serialVersionUID = 1L;
  private static final String STATUS_MANDATORY = "sm";
  private String df = null;
  private String etsm = null;
  private boolean sm;

  public MatStatusConfig() {

  }

  public MatStatusConfig(JSONObject json) {
    // default status
    try {
      df = json.getString(DEFAULT_STATUS);
    } catch (JSONException e) {
      // do nothing
    }
    // temperature sensitive material status
    try {
      etsm = json.getString(TEMPERATURE_SENSITIVE_MATERIAL_STATUS);
    } catch (JSONException e) {
      // do nothing
    }
    try {
      sm = json.getBoolean(STATUS_MANDATORY);
    } catch (JSONException e) {
      // do nothing
    }
  }

  @SuppressWarnings("rawtypes")
  public static Map<String, MatStatusConfig> getMatStatustMap(JSONObject json) {
    Map<String, MatStatusConfig> map = new HashMap<String, MatStatusConfig>();
    // Get the types
    Iterator keys = json.keys();
    while (keys.hasNext()) {
      String key = (String) keys.next();
      try {
        map.put(key, new MatStatusConfig(json.getJSONObject(key)));
      } catch (Exception e) {
        // do nothing
      }
    }
    return map;
  }

  public static JSONObject getMatStatusJSON(Map<String, MatStatusConfig> map) {
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

  public String getDf() {
    return df;
  }

  public void setDf(String df) {
    this.df = df;
  }

  public String getEtsm() {
    return etsm;
  }

  public void setEtsm(String etsm) {
    this.etsm = etsm;
  }

  public boolean isStatusMandatory() { return sm; }

  public void setStatusMandatory(boolean sm) { this.sm = sm; }

  public Object toJSONObject() throws ConfigurationException {
    JSONObject json = null;
    try {
      json = new JSONObject();
      if (df != null) {
        json.put(DEFAULT_STATUS, df);
      }
      if (etsm != null) {
        json.put(TEMPERATURE_SENSITIVE_MATERIAL_STATUS, etsm);
      }
      json.put(STATUS_MANDATORY, sm);
    } catch (JSONException e) {
      throw new ConfigurationException(e.getMessage());
    }
    return json;
  }

}

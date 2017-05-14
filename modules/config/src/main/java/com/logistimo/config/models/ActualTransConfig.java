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
 * Created by mohansrinivas on 12/21/15.
 */
public class ActualTransConfig implements Serializable {

  //constants
  public static final String TYPE = "ty";
  public static final String
      ACTUAL_NONE =
      "0";
  private static final long serialVersionUID = 1L;
  //"0" -> actual transaction date is not mandatory for a transaction
  private String ty = ACTUAL_NONE;


  public ActualTransConfig() {
  }

  public ActualTransConfig(JSONObject actTranConf) {
    try {
      ty = actTranConf.getString(TYPE) != null ? actTranConf.getString(TYPE) : ACTUAL_NONE;
    } catch (JSONException e) {
      //do Nothing
    }
  }

  @SuppressWarnings("rawtypes")
  public static Map<String, ActualTransConfig> getActualTranstMap(JSONObject json) {
    Map<String, ActualTransConfig> map = new HashMap<String, ActualTransConfig>();
    // Get the types
    Iterator keys = json.keys();
    while (keys.hasNext()) {
      String key = (String) keys.next();
      try {
        map.put(key, new ActualTransConfig(json.getJSONObject(key)));
      } catch (Exception e) {
        // do nothing
      }
    }
    return map;
  }

  public static JSONObject getActualTransJSON(Map<String, ActualTransConfig> map) {
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

  public String getTy() {
    return ty;
  }

  public void setTy(String ty) {
    this.ty = ty;
  }

  public Object toJSONObject() throws ConfigurationException {
    JSONObject json = null;
    try {
      json = new JSONObject();
      if (ty != null) {
        json.put(TYPE, ty);
      } else {
        json.put(TYPE, ACTUAL_NONE);
      }
    } catch (JSONException e) {
      throw new ConfigurationException(e.getMessage());
    }
    return json;
  }


}

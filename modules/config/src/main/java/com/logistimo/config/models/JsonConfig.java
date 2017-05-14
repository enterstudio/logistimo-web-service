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
 * @author arun
 *         Represents a JSON configuration that maps a code to a name (e.g. country codes to names, language codes to names, currency codes to names, and so on)
 *         Serialization format is JSON, as shown in the example below:
 *         { "en":"English", ...}
 */
public class JsonConfig {

  private static final XLog xLogger = XLog.getLog(JsonConfig.class);
  protected JSONObject json = null;

  /**
   * Construct a configuration from a JSON config. string
   */
  public JsonConfig(String jsonStr) throws ConfigurationException {
    try {
      this.json = new JSONObject(jsonStr);
    } catch (JSONException e) {
      throw new ConfigurationException(e.getMessage());
    }
  }

  /**
   * Returns the JSON string that was passed during object creation.
   */
  public String toJSONString() {
    if (this.json != null) {
      return this.json.toString();
    }

    return null;
  }

  /**
   * Get value, given key
   */
  public String getStringValue(String key) {
    String value = null;
    try {
      if (this.json != null) {
        value = this.json.getString(key);
      }
    } catch (JSONException e) {
      xLogger
          .warn("Exception while retrieving value from JSON for key {0}: {1}", key, e.getMessage());
    }

    return value;
  }
}

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

import com.logistimo.config.entity.IConfig;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * @author arun
 *
 *         Validates a configuration to ensure its validity
 */
public class ConfigValidator {

  public static void validate(String configType, String configStr) throws ConfigurationException {
    if (IConfig.OPTIMIZATION.equalsIgnoreCase(configType)) {
      try {
        JSONObject json = new JSONObject(configStr);
        new OptimizerConfig(json);
      } catch (JSONException e) {
        throw new ConfigurationException(e.getMessage());
      }
    } else {
      try {
        new JSONObject(configStr);
      } catch (JSONException e) {
        throw new ConfigurationException(e.getMessage());
      }
    }
  }
}

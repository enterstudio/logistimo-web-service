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

import com.logistimo.config.entity.IConfig;
import com.logistimo.config.service.ConfigurationMgmtService;
import com.logistimo.config.service.impl.ConfigurationMgmtServiceImpl;
import com.logistimo.services.ObjectNotFoundException;
import com.logistimo.services.ServiceException;
import com.logistimo.services.Services;
import com.logistimo.logger.XLog;

import java.io.Serializable;

public class KioskConfig implements Serializable {

  private static final long serialVersionUID = 1L;

  private static final String STOCKBOARD_CONFIG = "sbConfig"; // JSON Tag
  // Logger
  private static final XLog xLogger = XLog.getLog(KioskConfig.class);
  private StockboardConfig sbConfig;

  private KioskConfig(String jsonString) throws ConfigurationException {
    try {
      JSONObject json = new JSONObject(jsonString);
      JSONObject sbConfigJson = json.getJSONObject(STOCKBOARD_CONFIG);
      this.sbConfig = new StockboardConfig(sbConfigJson.toString());
    } catch (JSONException je) {
      throw new ConfigurationException(je.getMessage());
    }
  }

  private KioskConfig() {
    sbConfig = new StockboardConfig();
  }

  public static KioskConfig getInstance(Long kioskId) {
    xLogger.fine("Entering KioskConfig.getInstance");
    KioskConfig kc = null;
    if (kioskId == null) {
      return null;
    }
    String key = IConfig.CONFIG_KIOSK_PREFIX + kioskId.toString();
    // Get from kiosk configuration from the datastore using the key
    try {
      ConfigurationMgmtService cms = Services.getService(ConfigurationMgmtServiceImpl.class);
      String configStr = cms.getConfiguration(key).getConfig();
      xLogger.fine("configStr: " + configStr);
      kc = new KioskConfig(configStr);
      xLogger.fine("kc: {0}", kc);
    } catch (ObjectNotFoundException e) {
      xLogger.fine("Kiosk config not found for kiosk id {0}", kioskId);
      kc = new KioskConfig();
      xLogger.fine("kc: {0}", kc);
    } catch (ServiceException e) {
      xLogger.severe("Service exception: {0}", e.getMessage());
    } catch (ConfigurationException e) {
      xLogger.severe("Configuration Exception: {0}", e.getMessage());
    }
    return kc;
  }

  public String toJSONString() throws JSONException {
    String jsonString = toJSONObject().toString();
    return jsonString;
  }

  public JSONObject toJSONObject() throws JSONException {
    JSONObject json = new JSONObject();
    if (sbConfig != null) {
      json.put(STOCKBOARD_CONFIG, sbConfig.toJSONObject());
    }
    return json;
  }

  public void setSbConfig(StockboardConfig sbConfig) {
    this.sbConfig = sbConfig;
  }

  public StockboardConfig getStockboardConfig() {
    return sbConfig;
  }
}

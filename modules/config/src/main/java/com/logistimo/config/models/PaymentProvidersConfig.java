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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.logistimo.config.entity.IConfig;
import com.logistimo.config.service.ConfigurationMgmtService;
import com.logistimo.config.service.impl.ConfigurationMgmtServiceImpl;
import com.logistimo.services.ObjectNotFoundException;
import com.logistimo.services.ServiceException;
import com.logistimo.services.Services;
import com.logistimo.logger.XLog;

import java.util.ArrayList;
import java.util.List;

public class PaymentProvidersConfig {
  // JSON Keys
  public static final String PROVIDERS = "providers";
  // Logger
  private static final XLog xLogger = XLog.getLog(PaymentProvidersConfig.class);
  public JSONObject providers = null;

  public PaymentProvidersConfig(String jsonString) throws ConfigurationException {
    try {
      JSONObject jsonPaymentProvidersConfig = new JSONObject(jsonString);
      providers = (JSONObject) jsonPaymentProvidersConfig.get("providers");
    } catch (JSONException e) {
      throw new ConfigurationException(e.getMessage());
    }
  }

  // Get an instance of the Payments config
  public static PaymentProvidersConfig getInstance() throws ConfigurationException {
    try {
      ConfigurationMgmtService cms = Services.getService(ConfigurationMgmtServiceImpl.class);
      IConfig c = cms.getConfiguration(IConfig.PAYMENTPROVIDERSCONFIG);
      return new PaymentProvidersConfig(c.getConfig());
    } catch (ObjectNotFoundException e) {
      throw new ConfigurationException(e.getMessage());
    } catch (ServiceException e) {
      throw new ConfigurationException(e.getMessage());
    }
  }

  // Get a given providers config
  public ProviderConfig getProviderConfig(String providerName) {
    if (providers == null) {
      return null;
    }
    try {
      return new ProviderConfig(providers.getJSONObject(providerName));
    } catch (JSONException e) {
      return null;
    }
  }

  // Get the list of provider names
  public List<ProviderConfig> getProviders() {
    xLogger.fine("Entering getProviders");
    List<ProviderConfig> providerConfigList = null;
    if (providers != null) {
      JSONArray keys = providers.names();
      if (keys != null && keys.length() > 0) {
        providerConfigList = new ArrayList<ProviderConfig>();
        for (int i = 0; i < keys.length(); i++) {
          try {
            String pn = keys.getString(i);
            ProviderConfig pc = new ProviderConfig(providers.getJSONObject(pn));
            providerConfigList.add(pc);
          } catch (JSONException je) {
            xLogger.info("json exception: Message: ", je.getMessage());
          }
        }
      } else {
        // No keys in the providers json
        xLogger.severe(
            "No keys in the providers json. Please check paymentprovidersconfig under system confuguration");
      }
    } else {
      xLogger.info("providers is null");
    }
    return providerConfigList;
  }


  public class ProviderConfig {
    public static final String PROVIDER_ID = "pid";
    public static final String PROVIDER_NAME = "pname";
    public static final String USER_NAME = "uname";
    public static final String PASSWORD = "pwd";
    public static final String URL = "url";
    public static final String SERVER_URI = "serveruri";

    private JSONObject json = null;

    public ProviderConfig(JSONObject json) {
      this.json = json;
    }

    public String getString(String key) {
      try {
        if (json != null) {
          return json.getString(key);
        }
      } catch (JSONException e) {
        // do nothing
      }
      return null;
    }
  }
}

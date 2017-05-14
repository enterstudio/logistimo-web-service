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
import com.logistimo.config.service.ConfigurationMgmtService;
import com.logistimo.config.service.impl.ConfigurationMgmtServiceImpl;
import com.logistimo.dao.JDOUtils;

import org.json.JSONException;
import org.json.JSONObject;
import com.logistimo.services.ObjectNotFoundException;
import com.logistimo.services.ServiceException;
import com.logistimo.services.Services;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Represents currency configuration
 *
 * @author Arun
 */
public class CurrencyConfig {

  private static final String DEFAULT = "{\"INR\":\"Indian Rupees\", \"USD\":\"US Dollars\"}";

  private static ConfigurationMgmtService cms = null;
  private Map<String, String> cmap = new HashMap<String, String>();
  private Map<String, String> reversemap = new HashMap<String, String>();

  public CurrencyConfig(String currencySpec) throws ConfigurationException {
    parse(currencySpec);
  }

  public static CurrencyConfig getInstance() throws ConfigurationException {
    String curSpec = null;
    try {
      cms = Services.getService(ConfigurationMgmtServiceImpl.class);
      IConfig c = cms.getConfiguration(IConfig.CURRENCIES);
      curSpec = c.getConfig();
    } catch (ObjectNotFoundException e) {
      curSpec = DEFAULT;
    } catch (ServiceException e) {
      throw new ConfigurationException(e.getMessage());
    }
    return new CurrencyConfig(curSpec);
  }

  public String getName(String code) {
    return cmap.get(code);
  }

  public String getCode(String name) {
    return reversemap.get(name);
  }

  public void put(String code, String name) {
    cmap.put(code, name);
  }

  public void save() throws ConfigurationException {
    try {
      IConfig c = cms.getConfiguration(IConfig.CURRENCIES);
      // Update this config.
      c.setConfig(toJsonString());
      cms.updateConfiguration(c);
    } catch (ObjectNotFoundException e) {
      // Add a new config.
      IConfig c = JDOUtils.createInstance(IConfig.class);
      c.setKey(IConfig.CURRENCIES);
      c.setConfig(toJsonString());
      try {
        cms.addConfiguration(IConfig.CURRENCIES, c);
      } catch (ServiceException e1) {
        throw new ConfigurationException(e.getMessage());
      }
    } catch (ServiceException e) {
      throw new ConfigurationException(e.getMessage());
    }
  }

  @SuppressWarnings("rawtypes")
  public List getNames() {
    Object[] coll = cmap.values().toArray();
    Arrays.sort(coll);
    return Arrays.asList(coll);
  }

  public String toJsonString() throws ConfigurationException {
    String str = null;
    if (cmap == null || cmap.isEmpty()) {
      return null;
    }
    try {
      JSONObject json = new JSONObject();
      Iterator<String> it = cmap.keySet().iterator();
      while (it.hasNext()) {
        String code = it.next();
        json.put(code, cmap.get(code));
      }
      str = json.toString();
    } catch (JSONException e) {
      throw new ConfigurationException(e.getMessage());
    }
    return str;
  }

  @SuppressWarnings("unchecked")
  private void parse(String currencySpec) throws ConfigurationException {
    if (currencySpec == null || currencySpec.isEmpty()) {
      throw new ConfigurationException("Invalid currency specification");
    }
    try {
      JSONObject json = new JSONObject(currencySpec);
      Iterator<String> keys = json.keys();
      while (keys.hasNext()) {
        String code = keys.next();
        String name = json.getString(code);
        if (name != null && !name.isEmpty()) {
          cmap.put(code, name);
          reversemap.put(name, code);
        }
      }
    } catch (JSONException e) {
      throw new ConfigurationException(e.getMessage());
    }
  }
}

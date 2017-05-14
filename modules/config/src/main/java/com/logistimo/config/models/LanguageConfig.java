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

import com.logistimo.config.entity.IConfig;
import com.logistimo.config.service.ConfigurationMgmtService;
import com.logistimo.config.service.impl.ConfigurationMgmtServiceImpl;
import com.logistimo.services.Services;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Represents a language configuration.
 *
 * @author Arun
 */
public class LanguageConfig extends JsonConfig {

  public static final String LANGUAGES_MOBILE = IConfig.LANGUAGES_MOBILE;

  private Map<String, String> name2code = new HashMap<String, String>();

  @SuppressWarnings("unchecked")
  public LanguageConfig(String jsonStr) throws ConfigurationException {
    super(jsonStr);
    Iterator<String> en = json.keys();
    while (en.hasNext()) {
      String code = en.next();
      try {
        name2code.put(json.getString(code), code);
      } catch (JSONException e) {
        // Ignore
      }
    }
  }

  public static LanguageConfig getInstance() throws ConfigurationException {
    return getInstance(IConfig.LANGUAGES);
  }

  public static LanguageConfig getInstance(String key) throws ConfigurationException {
    String jsonStr = null;
    try {
      ConfigurationMgmtService cms = Services.getService(ConfigurationMgmtServiceImpl.class);
      IConfig config = cms.getConfiguration(key);
      jsonStr = config.getConfig();
    } catch (Exception e) {
      jsonStr = "{ \"en\":\"English\" }";
    }
    return new LanguageConfig(jsonStr);
  }

  // Get a reverse language map - language name vs ISO code
  public Map<String, String> getName2CodeMap() {
    return name2code;
  }

  // Get sorted names
  @SuppressWarnings("rawtypes")
  public List getNames() {
    Object[] array = name2code.keySet().toArray();
    Arrays.sort(array);
    return Arrays.asList(array);
  }

  // Get the language code, given a name
  public String getCode(String name) {
    return name2code.get(name);
  }

}

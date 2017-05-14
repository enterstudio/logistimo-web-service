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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.logistimo.services.ObjectNotFoundException;
import com.logistimo.services.ServiceException;
import com.logistimo.services.Services;
import com.logistimo.constants.Constants;
import com.logistimo.logger.XLog;

/**
 * Represents SMS Gateway and related configuration
 *
 * @author Arun
 */
public class SMSConfig {

  // JSON Keys
  public static final String COUNTRY_CONFIG = "country-config";
  public static final String PROVIDERS = "providers";
  // Logger
  private static final XLog xLogger = XLog.getLog(SMSConfig.class);
  public JSONObject countryConfig = null;
  public JSONObject providers = null;

  public SMSConfig(String jsonString) throws ConfigurationException {
    try {
      JSONObject jsonSMSConfig = new JSONObject(jsonString);
      countryConfig = jsonSMSConfig.getJSONObject(COUNTRY_CONFIG);
      providers = jsonSMSConfig.getJSONObject(PROVIDERS);
    } catch (JSONException e) {
      throw new ConfigurationException(e.getMessage());
    }
  }

  // Get an instance of the SMS config
  public static SMSConfig getInstance() throws ConfigurationException {
    try {
      ConfigurationMgmtService cms = Services.getService(ConfigurationMgmtServiceImpl.class);
      IConfig c = cms.getConfiguration(IConfig.SMSCONFIG);
      return new SMSConfig(c.getConfig());
    } catch (ObjectNotFoundException e) {
      throw new ConfigurationException(e.getMessage());
    } catch (ServiceException e) {
      throw new ConfigurationException(e.getMessage());
    }
  }

  // Get a given providers config
  public ProviderConfig getProviderConfig(String providerId) {
    if (providers == null) {
      return null;
    }
    try {
      return new ProviderConfig(providers.getJSONObject(providerId));
    } catch (JSONException e) {
      return null;
    }
  }

  // Get the providers for a given country and direction (incoming/outgoing)
  // NOTE: "incoming" is from user to our service; "outgoing" is from our service to the user
  public String getProviderId(String countryCode, String direction) {
    JSONObject countryJson = null;
    try {
      try {
        countryJson = countryConfig.getJSONObject(countryCode);
      } catch (JSONException e) {
        countryJson = countryConfig.getJSONObject(Constants.COUNTRY_DEFAULT);
      }
      return countryJson.getString(direction);
    } catch (JSONException e) {
      return null;
    }
  }

  public class ProviderConfig {

    public static final String PROVIDER_ID = "pid";
    public static final String USER_ID = "uid";
    public static final String PASSWORD = "pwd";
    public static final String SENDER_ID = "sid";
    public static final String KEYWORD = "keyword";
    public static final String LONGCODE = "longcode";
    public static final String APPENDIX = "appendix"; // if present, append to each message

    public static final String NORMAL_URL = "normal-url";
    public static final String UNICODE_URL = "unicode-url";
    public static final String WAPPUSH_URL = "wappush-url";
    public static final String BINARYTOPORT_URL = "binarytoport-url";

    public static final String
        FORMAT_DATETIME =
        "format-datetime";
    // SimpleDateFormat style date/time format specification

    public static final String
        REGEXES_JOBID =
        "regexes-jid";
    // regex to pull Job Id from return value
    public static final String
        REPLACE_PHONENUMBER_PLUS =
        "replace-phonenum-plus";
    // string to replace + in our standard phone format
    public static final String
        REPLACE_PHONENUMBER_SPACE =
        "replace-phonenum-space";
    // string to replace " " from our standard phone format

    public static final String STATUS_TYPE = "status-type"; // push/pull
    public static final String STATUS_FORMAT = "status-format"; // qs = query string; xml = XML
    public static final String STATUS_METHOD = "status-method"; // one "perjob", "peraddress"

    // Status code mapping
    public static final String
        STATUS_CODES =
        "status-codes";
    // JSON mapping of standard status codes to provider-specific codes
    public static final String
        STATUS_CODE_DEFAULT =
        "status-code-default";
    // default status code immediately after sending

    // Max. addresses
    public static final String MAX_ADDRESSES = "max-addresses"; // max. addresses per call

    // Parameter mappings
    public static final String PARAM_NAMES = "param-names";

    // Job ID unique per number or not (i.e. unique across multiple nos. and needs to be combined with mobile-number (for uniqueness)
    public static final String PARAM_JIDUNIQUE = "jid-unique";

    // Values
    public static final String PERJOB = "perjob";
    public static final String PERADDRESS = "peraddress";
    public static final String QUERY_STRING = "qs";
    public static final String XML = "xml";

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

    public String[] getStrings(String key) {
      String[] strings = null;
      try {
        if (json != null) {
          JSONArray array = json.getJSONArray(key);
          strings = new String[array.length()];
          for (int i = 0; i < array.length(); i++) {
            strings[i] = array.getString(i);
          }
        }
      } catch (JSONException e) {
        xLogger.warn("Invalid regex for key {0}; expected a JSONArray", key);
      }
      return strings;
    }

    public Integer getInteger(String key) {
      String str = getString(key);
      Integer i = null;
      if (str != null && !str.isEmpty()) {
        try {
          i = Integer.valueOf(str);
        } catch (NumberFormatException e) {
          // do nothing
        }
      }
      return i;
    }

    public String getStatusResourceKey(String code) {
      if (json == null) {
        return null;
      }
      try {
        JSONObject codes = json.getJSONObject(STATUS_CODES);
        return codes.getString(code);
      } catch (JSONException e) {
        return null;
      }
    }

    public String getParameterName(String paramName) {
      try {
        JSONObject paramNames = json.getJSONObject(PARAM_NAMES);
        return paramNames.getString(paramName);
      } catch (JSONException e) {
        return null;
      }
    }
  }
}

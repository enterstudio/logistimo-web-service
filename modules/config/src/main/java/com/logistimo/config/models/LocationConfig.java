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

import com.google.gson.Gson;

import org.apache.commons.lang.StringUtils;
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
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author Arun
 */
public class LocationConfig {

  // Constants
  public static final String STATES = "states";
  public static final String DISTRICTS = "districts";
  private static final XLog xLogger = XLog.getLog(LocationConfig.class);
  private static Map<String, StateModel> districts = null;
  private static Map<String, String> states = null;
  // Map<country-code, Map<state,List<district>>>
  private Map<String, Map<String, List<String>>>
      lmap =
      new HashMap<String, Map<String, List<String>>>();
  // Map<country-code,country-name>
  private Map<String, String> cmap = new HashMap<String, String>();

  @SuppressWarnings("unchecked")
  public LocationConfig(String jsonCountries) throws ConfigurationException {
    if (jsonCountries == null || jsonCountries.isEmpty()) {
      throw new ConfigurationException("Null or empty JSON string");
    }
    try {
      JSONObject json = new JSONObject(jsonCountries);
      Iterator<String> codes = json.keys();
      while (codes.hasNext()) {
        String key = codes.next();
        cmap.put(key, json.getString(key));
      }
    } catch (JSONException e) {
      throw new ConfigurationException(e.getMessage());
    }
  }

  // Init. location config from locations json
  @SuppressWarnings("unchecked")
  public LocationConfig(String jsonCountries, String jsonStates) throws ConfigurationException {
    this(jsonCountries);
    if (jsonStates == null || jsonStates.isEmpty()) {
      throw new ConfigurationException("Null or empty JSON string");
    }
    try {
      JSONObject json = new JSONObject(jsonStates);
      JSONArray jstates = json.getJSONArray(STATES);
      JSONArray jdistricts = json.getJSONArray(DISTRICTS);
      for (int i = 0; i < jstates.length(); i++) {
        JSONObject jcs = jstates.getJSONObject(i);
        Iterator<String> countryCodes = jcs.keys(); // only one country code per object
        if (countryCodes.hasNext()) {
          String countryCode = countryCodes.next();
          Map<String, List<String>> stateMap = new HashMap<String, List<String>>();
          // Update location map
          lmap.put(countryCode, stateMap);
          // Get the states of this country
          JSONArray statesOfCountry = jcs.getJSONArray(countryCode);
          for (int j = 0; j < statesOfCountry.length(); j++) {
            String state = statesOfCountry.getString(j);
            // Init. districts list
            List<String> districts = new ArrayList<String>();
            stateMap.put(state, districts);
            // Get the districts
            for (int d = 0; d < jdistricts.length(); d++) {
              JSONObject jd = jdistricts.getJSONObject(d);
              Iterator<String> jdkeys = jd.keys();
              if (jdkeys.hasNext()) {
                String jdkey = jdkeys.next();
                if (jdkey.equals(countryCode + "." + state)) {
                  JSONArray districtsOfState = jd.getJSONArray(jdkey);
                  // Get the districts list
                  for (int l = 0; l < districtsOfState.length(); l++) {
                    districts.add(districtsOfState.getString(l));
                  }
                }
              }
            }
          }
        }
      }
    } catch (JSONException e) {
      throw new ConfigurationException(e.getMessage());
    }
  }

  public static LocationConfig getLocationConfig(boolean countriesOnly) {

    LocationConfig lc = null;
    try {
      ConfigurationMgmtService cms = Services.getService(ConfigurationMgmtServiceImpl.class);
      String cconfigStr = cms.getConfiguration(IConfig.COUNTRIES).getConfig();
      if (countriesOnly) {
        lc = new LocationConfig(cconfigStr);
      } else {
        String lconfigStr = cms.getConfiguration(IConfig.LOCATIONS).getConfig();
        lc = new LocationConfig(cconfigStr, lconfigStr);
      }
    } catch (ObjectNotFoundException e) {
      xLogger.severe("Object Not found exception: {0}", e.getMessage());
    } catch (ServiceException e) {
      xLogger.severe("Service exception: {0}", e.getMessage());
      return null;
    } catch (ConfigurationException e) {
      xLogger.severe("Configuration exception: {0}", e.getMessage());
      return null;
    }

    return lc;
  }

  public static LocationConfig getLocationConfig() {
    return getLocationConfig(false);
  }

  /**
   * constructing a map of districts map with  key and state as a value for reports
   * and states map with key as a state and country as a value
   */

  public static void constructStateDistrictMaps(LocationModel lc) {
    states = new HashMap<>(1);
    districts = new HashMap<>(1);
    try {
      for (String cc : lc.data.keySet()) {
        LocationModel.Country country = lc.data.get(cc);
        if (country.states != null) {
          for (String st : country.states.keySet()) {
            if (StringUtils.isNotBlank(st)) {
              states.put(st, cc);
              LocationModel.State state = country.states.get(st);
              if (state.districts != null) {
                for (String dst : state.districts.keySet()) {
                  if (StringUtils.isNotBlank(dst)) {
                    StateModel dm = new StateModel(cc, st);
                    districts.put(dst, dm);
                  }
                }
              }
            }
          }
        }
      }
    } catch (Exception e) {
      xLogger.severe("Exception: {0}", e.getMessage(), e);
    }
  }

  /**
   * get the state of the given district from the districts map
   */
  public static StateModel getState(String district) {
    if (districts == null) {
      initialize();
    }
    return districts.get(district) != null ? districts.get(district) : null;
  }

  /**
   * get the country for the given state from the states map
   */

  public static String getCountry(String state) {
    if (states == null) {
      initialize();
    }
    return states.get(state) != null ? states.get(state) : null;
  }

  /**
   * initialize the location config service and construct the district and states map
   * district and states maps are used for reports
   */
  public static void initialize() {
    try {
      ConfigurationMgmtService cms = Services.getService(ConfigurationMgmtServiceImpl.class);
      String locationConfig = cms.getConfiguration(IConfig.LOCATIONS).getConfig();
      LocationModel lc = new Gson().fromJson(locationConfig, LocationModel.class);
      constructStateDistrictMaps(lc);
    } catch (ObjectNotFoundException e) {
      xLogger.severe("Object Not found exception: {0}", e.getMessage(), e);
    } catch (ServiceException e) {
      xLogger.severe("Service exception: {0}", e.getMessage(), e);
    }
  }

  public List<String> getCountryCodes() {
    Set<String> ccodesset = lmap.keySet();
    if (!ccodesset.isEmpty()) {
      // Sort the list
      String[] array = ccodesset.toArray(new String[ccodesset.size()]);
      Arrays.sort(array);
      return Arrays.asList(array);
    }

    return null;
  }

  public String getCountryName(String countryCode) {
    return cmap.get(countryCode);
  }

  public List<String> getStates(String countryCode) {
    Map<String, List<String>> stateMap = getStateMap(countryCode);
    if (stateMap == null) {
      return null;
    }
    Set<String> states = stateMap.keySet();
    String[] array = states.toArray(new String[states.size()]);
    Arrays.sort(array);
    return Arrays.asList(array);
  }

  public List<String> getDistricts(String countryCode, String state) {
    Map<String, List<String>> stateMap = getStateMap(countryCode);
    if (stateMap == null) {
      return null;
    }
    return stateMap.get(state);
  }

  public Map<String, List<String>> getStateMap(String countryCode) {
    return lmap.get(countryCode);
  }
}

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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.logistimo.services.ObjectNotFoundException;
import com.logistimo.services.Services;
import com.logistimo.logger.XLog;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author Arun
 */
public class ReportsConfig {

  private static final XLog xLogger = XLog.getLog(ReportsConfig.class);
  // Default
  private static final String
      DEFAULT =
      "{ \"types\" : [ \"consumptiontrends\", \"transactioncounts\"],  \"filters\": { \"consumptiontrends\" : { \"mandatory\" : [ \"mtrl\" ], \"optional\" : [\"stt\", \"dstr\", \"plgr\", \"ksk\"] }, \"transactioncounts\" : { \"optional\" : [ \"mtrl\", \"stt\", \"dstr\", \"plgr\", \"ksk\" ] } }, \"filterkeys\" : { \"mtrl\" : \"material\", \"stt\" : \"state\", \"dstr\" : \"district\", \"plgr\" : \"poolgroup\", \"ksk\" : \"kiosk\" } }";

  // Properties
  private Long domainId = null;
  private Map<String, List<String>> filters = new HashMap<String, List<String>>();
  private boolean dirty = false;

  private ReportsConfig(Long domainId) {
    this.domainId = domainId;
  }

  @SuppressWarnings("unchecked")
  private ReportsConfig(Long domainId, String jsonString) throws JSONException {
    this.domainId = domainId;
    JSONObject json = new JSONObject(jsonString);
    Iterator<String> en = json.keys();
    while (en.hasNext()) {
      String key = en.next();
      JSONArray array = json.getJSONArray(key);
      List<String> values = new ArrayList<String>();
      for (int i = 0; i < array.length(); i++) {
        values.add(array.getString(i));
      }
      if (!values.isEmpty()) {
        filters.put(key, values);
      }
    }
  }

  // Domain specific reports configuration
  public static ReportsConfig getInstance(Long domainId) {
    try {
      String jsonStr = getJSONString(domainId);
      if (jsonStr != null) {
        return new ReportsConfig(domainId, jsonStr);
      }
    } catch (Exception e) {
      xLogger.warn("Excetion when getting reports config: {0} : {1}", e.getClass().getName(),
          e.getMessage());
    }
    return new ReportsConfig(domainId);
  }

  // Domain level reports config (as seen under System Configuration)
  public static String getJSONString() {
    // Get from datastore
    try {
      ConfigurationMgmtService cms = Services.getService(ConfigurationMgmtServiceImpl.class);
      return cms.getConfiguration(IConfig.REPORTS).getConfig();
    } catch (Exception e) {
      xLogger.severe("Service exception: {0}", e.getMessage());
      return DEFAULT;
    }
  }

  // Get a domain specific reports configuration - esp. dimensions and values
  public static String getJSONString(Long domainId) {
    // Get from datastore
    try {
      ConfigurationMgmtService cms = Services.getService(ConfigurationMgmtServiceImpl.class);
      return cms.getConfiguration(IConfig.REPORTS + "." + domainId).getConfig();
    } catch (Exception e) {
      xLogger.warn("Service exception: {0}", e.getMessage());
      return null;
    }
  }

  // Returns report types (list of report types)
  public static List<String> getReportTypes() {
    List<String> list = null;
    try {
      JSONObject json = new JSONObject(getJSONString());
      JSONArray types = json.getJSONArray("types"); // types should be the same as resource keys
      list = new ArrayList<String>();
      for (int i = 0; i < types.length(); i++) {
        list.add(types.getString(i));
      }
    } catch (JSONException e) {
      // ignore
    }
    return list;
  }

  private static JSONArray toJSONArray(List<String> values) {
    if (values == null || values.isEmpty()) {
      return null;
    }
    JSONArray array = new JSONArray();
    Iterator<String> it = values.iterator();
    while (it.hasNext()) {
      array.put(it.next());
    }
    return array;
  }

  public Long getDomainId() {
    return domainId;
  }

  public Map<String, List<String>> getFilterMap() {
    return filters;
  }

  public List<String> getFilterValues(String filterType) {
    List<String> values = filters.get(filterType);
    if (values == null || values.isEmpty()) {
      return null;
    }
    Collections.sort(values);
    return values;
  }

  @SuppressWarnings({"unchecked", "rawtypes"})
  public void addFilterValue(String filterType, Object newValue) {
    if (newValue == null) {
      return;
    }
    List<String> values = filters.get(filterType);
    if (values == null) {
      values = new ArrayList<>(1);
      filters.put(filterType, values);
    }
    if (newValue instanceof String && !((String) newValue).isEmpty()) {
      if (!values.contains(newValue)) {
        values.add((String) newValue);
        dirty = true;
      }
    } else if (newValue instanceof List && !((List) newValue).isEmpty()) {
      Set<String> tmpSet = new HashSet<>(values);
      tmpSet.addAll((Collection<? extends String>) newValue);
      if (values.size() != tmpSet.size()) {
        dirty = true;
      }
      values.clear();
      values.addAll(tmpSet);
    }
  }

  // Update this config with values from the given kiosk
  public void addFilterValues(Map<String, Object> filterValues) {
    if (filterValues == null || filterValues.isEmpty()) {
      return;
    }
    for (String filterType : filterValues.keySet()) {
      Object value = filterValues.get(filterType);
      if (value != null) {
        addFilterValue(filterType, value);
      }
    }
  }

  // Store a domain specific instance of report config (esp. filters/dimension values)
  public void store() {
    if (!dirty) {
      return; // don't store, since nothing changed
    }
    // Get from datastore
    try {
      ConfigurationMgmtService cms = Services.getService(ConfigurationMgmtServiceImpl.class);
      String configStr = toJSONString();
      String key = IConfig.REPORTS + "." + domainId;
      try {
        IConfig config = cms.getConfiguration(key);
        config.setPrevConfig(config.getConfig());
        config.setConfig(configStr);
        config.setLastUpdated(new Date());
        // Update
        cms.updateConfiguration(config);
      } catch (ObjectNotFoundException e) {
        IConfig config = JDOUtils.createInstance(IConfig.class);
        config.setConfig(configStr);
        config.setDomainId(domainId);
        config.setKey(key);
        config.setLastUpdated(new Date());
        // Add
        cms.addConfiguration(key, domainId, config);
      }
    } catch (Exception e) {
      xLogger.severe("Service exception: {0}", e.getMessage());
    }
  }

  private String toJSONString() throws JSONException {
    if (filters.isEmpty()) {
      return null;
    }
    JSONObject json = new JSONObject();
    Iterator<String> keys = filters.keySet().iterator();
    while (keys.hasNext()) {
      String key = keys.next();
      List<String> values = filters.get(key);
      JSONArray array = toJSONArray(values);
      if (array != null) {
        json.put(key, array);
      }
    }
    return json.toString();
  }
}

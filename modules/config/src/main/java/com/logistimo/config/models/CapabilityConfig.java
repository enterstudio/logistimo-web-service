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
import com.logistimo.utils.StringUtil;

import java.io.Serializable;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Stores capabilities that are allowed on the client - like a mobile app. or web client
 *
 * @author Arun
 */
public class CapabilityConfig implements Serializable {

  // Constants
  public static final String GEOCODING_STRATEGY_OPTIMISTIC = "o";
  public static final String GEOCODING_STRATEGY_STRICT = "s";
  // Keys
  public static final String DISABLED = "dsbld";
  public static final String GEOCODING_STRATEGY = "gstr";
  public static final String TAGS_INVENTORY = "tgi";
  public static final String TAGS_ORDERS = "tgo";
  public static final String SEND_CUSTOMERS = "sndcsts";
  public static final String SEND_VENDORS = "sndvnds";
  public static final String WASTAGE_REASONS = "wrsns";
  public static final String CREATABLE_ENTITY_TYPES = "crenty";
  public static final String ALLOW_ROUTETAG_EDITING = "arte";
  public static final String DISABLE_SHIPPING_ON_MOBILE = "disponmob";
  public static final String DISABLE_TAGS_INVENTRY_OPERATION = "tgiov";
  // Creatable entity types
  public static final String TYPE_VENDOR = "vnds";
  public static final String TYPE_CUSTOMER = "csts";
  public static final String TYPE_MANAGEDENTITY = "ents";
  public static final String LOGIN_AS_RECONNECT = "lasr";
  // Capability constants (only those used internally for permission checks are listed here)
  public static final String CAPABILITY_EDITPROFILE = "ep";
  private static final long serialVersionUID = 1L;
  private List<String> disabled = null;
  private String geoCodingStrategy = GEOCODING_STRATEGY_OPTIMISTIC;
  private String tagsInventory = null; // tags to be hidden from Inventory in client app.
  private String tagsOrders = null; // tags to be hidden from Orders in client app.
  private boolean
      sendVendors =
      true;
  // send vendor info. to mobile (sent only if Orders are enabled)
  private boolean sendCustomers = true; // send customer info. to mobile
  private String wastageReasons = null; // CSV of reasons for issue (e.g. expired, damaged)
  private List<String> creatableEntityTypes; // the types of entities that can be created
  private boolean allowRouteTagEditing = false;
  private boolean loginAsReconnect = false;
  private boolean disableShippingOnMobile = false; // By default disable shipping on mobile.
  private Map<String, String> tagsInvByOperation = new HashMap<>();

  public CapabilityConfig() {
  }

  public CapabilityConfig(JSONObject json) throws ConfigurationException {
    // Get capabilities for disablement
    try {
      this.disabled = StringUtil.getList(json.getString(DISABLED));
    } catch (JSONException e) {
      // do nothing
    }
    // Get the tags to be hidden from Inventory
    try {
      this.tagsInventory = (String) json.get(TAGS_INVENTORY);
    } catch (JSONException e) {
      // do nothing
    }
    // Get the tags to be hidden from Orders
    try {
      this.tagsOrders = (String) json.get(TAGS_ORDERS);
    } catch (JSONException e) {
      // do nothing
    }
    // Flags to send customers / vendors
    try {
      this.sendVendors = json.getBoolean(SEND_VENDORS);
    } catch (JSONException e) {
      // do nothing
    }
    try {
      this.sendCustomers = json.getBoolean(SEND_CUSTOMERS);
    } catch (JSONException e) {
      // do nothing
    }
    try {
      this.wastageReasons = json.getString(WASTAGE_REASONS);
    } catch (JSONException e) {
      // do nothing
    }
    try {
      this.geoCodingStrategy = json.getString(GEOCODING_STRATEGY);
    } catch (JSONException e) {
      // do nothing
    }
    try {
      this.creatableEntityTypes = StringUtil.getList(json.getString(CREATABLE_ENTITY_TYPES));
    } catch (JSONException e) {
      // do nothing (already initialized in the constructor)
    }
    try {
      this.allowRouteTagEditing = json.getBoolean(ALLOW_ROUTETAG_EDITING);
    } catch (JSONException e) {
      // do nothing (already initialized in the constructor)
    }
    // Flags to disable shipping on mobile.
    try {
      this.disableShippingOnMobile = json.getBoolean(DISABLE_SHIPPING_ON_MOBILE);
    } catch (JSONException e) {
      // do nothing
    }
    try {
      JSONObject invTags = json.getJSONObject(DISABLE_TAGS_INVENTRY_OPERATION);
      Iterator<String> en = invTags.keys();
      while (en.hasNext()) {
        String transType = en.next();
        tagsInvByOperation.put(transType, invTags.getString(transType));
      }
    } catch (JSONException e) {
      //do nothhing
    }
    loginAsReconnect = json.optBoolean(LOGIN_AS_RECONNECT, false);
  }

  @SuppressWarnings("rawtypes")
  public static Map<String, CapabilityConfig> getCapabilitiesMap(JSONObject json) {
    Map<String, CapabilityConfig> map = new HashMap<String, CapabilityConfig>();
    // Get the roles
    Iterator keys = json.keys();
    while (keys.hasNext()) {
      String key = (String) keys.next();
      try {
        map.put(key, new CapabilityConfig(json.getJSONObject(key)));
      } catch (Exception e) {
        // do nothing
      }
    }
    return map;
  }

  public static JSONObject getCapabilitiesJSON(Map<String, CapabilityConfig> map) {
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

  public JSONObject toJSONObject() throws ConfigurationException {
    JSONObject json = new JSONObject();
    try {
      // Get the capabilities to be disabled
      String capabilitiesStr = StringUtil.getCSV(this.disabled);
      if (capabilitiesStr != null && !capabilitiesStr.isEmpty()) {
        json.put(DISABLED, capabilitiesStr);
      }
      if (tagsInventory != null && !tagsInventory.isEmpty()) {
        json.put(TAGS_INVENTORY, tagsInventory);
      }
      if (tagsOrders != null && !tagsOrders.isEmpty()) {
        json.put(TAGS_ORDERS, tagsOrders);
      }
      // Add parameters to determine whether to send vendors/customers
      json.put(SEND_VENDORS, sendVendors);
      json.put(SEND_CUSTOMERS, sendCustomers);
      if (wastageReasons != null && !wastageReasons.isEmpty()) {
        json.put(WASTAGE_REASONS, wastageReasons);
      }
      if (geoCodingStrategy != null) {
        json.put(GEOCODING_STRATEGY, geoCodingStrategy);
      }
      if (creatableEntityTypes != null && !creatableEntityTypes.isEmpty()) {
        json.put(CREATABLE_ENTITY_TYPES, StringUtil.getCSV(creatableEntityTypes));
      }
      if (loginAsReconnect) {
        json.put(LOGIN_AS_RECONNECT, true);
      }
      // Allow route tag editing
      json.put(ALLOW_ROUTETAG_EDITING, allowRouteTagEditing);
      json.put(DISABLE_SHIPPING_ON_MOBILE, disableShippingOnMobile);
      if (!tagsInvByOperation.isEmpty()) {
        JSONObject invTags = new JSONObject();
        Iterator<String> it = tagsInvByOperation.keySet().iterator();
        while (it.hasNext()) {
          String transType = it.next();
          String tagsCsv = (String) tagsInvByOperation.get(transType);
          if (tagsCsv != null && !tagsCsv.isEmpty()) {
            invTags.put(transType, tagsCsv);
          }
        }
        json.put(DISABLE_TAGS_INVENTRY_OPERATION, invTags);
      }
    } catch (JSONException e) {
      throw new ConfigurationException(e.getMessage());
    }

    return json;
  }

  public List<String> getCapabilities() {
    return disabled;
  }

  public void setCapabilities(String[] capabilitiesArray) {
    if (capabilitiesArray == null || capabilitiesArray.length == 0) {
      this.disabled = null;
    } else {
      this.disabled = Arrays.asList(capabilitiesArray);
    }
  }

  public void setCapabilities(List<String> capabilities) {
    this.disabled = capabilities;
  }

  public boolean isCapabilityDisabled(String capability) {
    return (disabled != null && disabled.contains(capability));
  }

  public String getTagsInventory() {
    return tagsInventory;
  }

  public void setTagsInventory(String tagsInventory) {
    this.tagsInventory = tagsInventory;
  }

  public String getTagsOrders() {
    return tagsOrders;
  }

  public void setTagsOrders(String tagsOrders) {
    this.tagsOrders = tagsOrders;
  }

  public boolean sendVendors() {
    return sendVendors;
  }

  public void setSendVendors(boolean sendVendors) {
    this.sendVendors = sendVendors;
  }

  public boolean sendCustomers() {
    return sendCustomers;
  }

  public void setSendCustomers(boolean sendCustomers) {
    this.sendCustomers = sendCustomers;
  }

  public String getWastageReasons() {
    return wastageReasons;
  }

  public void setWastageReasons(String issueReasons) {
    this.wastageReasons = issueReasons;
  }

  public boolean isLoginAsReconnect() {
    return loginAsReconnect;
  }

  public void setLoginAsReconnect(boolean loginAsReconnect) {
    this.loginAsReconnect = loginAsReconnect;
  }

  public String getGeoCodingStrategy() {
    if (geoCodingStrategy == null) {
      geoCodingStrategy = GEOCODING_STRATEGY_OPTIMISTIC;
    }
    return geoCodingStrategy;
  }

  public void setGeoCodingStrategy(String geoCodingStrategy) {
    this.geoCodingStrategy = geoCodingStrategy;
  }

  public List<String> getCreatableEntityTypes() {
    return creatableEntityTypes;
  }

  public void setCreatableEntityTypes(List<String> creatableEntityTypes) {
    this.creatableEntityTypes = creatableEntityTypes;
  }

  public boolean allowRouteTagEditing() {
    return allowRouteTagEditing;
  }

  public void setAllowRouteTagEditing(boolean allow) {
    this.allowRouteTagEditing = allow;
  }

  public boolean isDisableShippingOnMobile() {
    return disableShippingOnMobile;
  }

  public void setDisableShippingOnMobile(boolean disableShippingOnMobile) {
    this.disableShippingOnMobile = disableShippingOnMobile;
  }

  public Map<String, String> gettagsInvByOperation() {
    return tagsInvByOperation;
  }

  public String gettagByOperation(String transType) {
    return (String) tagsInvByOperation.get(transType);
  }

  public void settagInvByOperation(Map<String, String> tagsInvByOperation) {
    this.tagsInvByOperation = tagsInvByOperation;
  }

  public void puttagsInvByOperation(String transType, String tagsCSV) {
    tagsInvByOperation.put(transType, tagsCSV);
  }
}

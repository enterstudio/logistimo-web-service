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

import java.io.Serializable;
import java.util.Locale;

public class DashboardConfig implements Serializable {
  /**
   *
   */
  private static final long serialVersionUID = -6997420541742196139L;
  private static final String ACTIVITY_PANEL_CONFIG = "actpanelcfg"; // Activity panel configuration
  private static final String REVENUE_PANEL_CONFIG = "rvnpanelcfg"; // Revenue panel configuration
  private static final String ORDER_PANEL_CONFIG = "ordpanelcfg"; // Order panel configuration
  private static final String
      INVENTORY_PANEL_CONFIG =
      "invpanelcfg";
  // Inventory panel configuration
  private static final String DASHBOARD_CONFIG = "dbcfg";
  private ActivityPanelConfig actPanelConfig = null;
  private RevenuePanelConfig rvnPanelConfig = null;
  private OrderPanelConfig ordPanelConfig = null;
  private InventoryPanelConfig invPanelConfig = null;
  private DBOverviewConfig dbOverConfig = null;

  public DashboardConfig() {
    actPanelConfig = new ActivityPanelConfig();
    rvnPanelConfig = new RevenuePanelConfig();
    ordPanelConfig = new OrderPanelConfig();
    invPanelConfig = new InventoryPanelConfig();
  }

  public DashboardConfig(JSONObject json, Locale locale, String timezone) {
    try {
      actPanelConfig = new ActivityPanelConfig(json.getJSONObject(ACTIVITY_PANEL_CONFIG));
    } catch (JSONException e) {
      actPanelConfig = new ActivityPanelConfig();
    }
    try {
      rvnPanelConfig = new RevenuePanelConfig(json.getJSONObject(REVENUE_PANEL_CONFIG));
    } catch (JSONException e) {
      rvnPanelConfig = new RevenuePanelConfig();
    }
    try {
      ordPanelConfig = new OrderPanelConfig(json.getJSONObject(ORDER_PANEL_CONFIG));
    } catch (JSONException e) {
      ordPanelConfig = new OrderPanelConfig();
    }
    try {
      invPanelConfig = new InventoryPanelConfig(json.getJSONObject(INVENTORY_PANEL_CONFIG));
    } catch (JSONException e) {
      invPanelConfig = new InventoryPanelConfig();
    }
    try {
      dbOverConfig = new DBOverviewConfig(json.getJSONObject(DASHBOARD_CONFIG));
    } catch (JSONException e) {
      dbOverConfig = new DBOverviewConfig();
    }
  }

  public ActivityPanelConfig getActivityPanelConfig() {
    return actPanelConfig;
  }

  public void setActivityPanelConfig(ActivityPanelConfig actPanelConfig) {
    this.actPanelConfig = actPanelConfig;
  }

  public RevenuePanelConfig getRevenuePanelConfig() {
    return rvnPanelConfig;
  }

  public void setRevenuePanelConfig(RevenuePanelConfig rvnPanelConfig) {
    this.rvnPanelConfig = rvnPanelConfig;
  }

  public OrderPanelConfig getOrderPanelConfig() {
    return ordPanelConfig;
  }

  public void setOrderPanelConfig(OrderPanelConfig ordPanelConfig) {
    this.ordPanelConfig = ordPanelConfig;
  }

  public InventoryPanelConfig getInventoryPanelConfig() {
    return invPanelConfig;
  }

  public void setInventoryPanelConfig(InventoryPanelConfig invPanelConfig) {
    this.invPanelConfig = invPanelConfig;
  }

  public DBOverviewConfig getDbOverConfig() {
    return dbOverConfig != null ? dbOverConfig : new DBOverviewConfig();
  }

  public void setDbOverConfig(DBOverviewConfig dbOverConfig) {
    this.dbOverConfig = dbOverConfig;
  }

  public JSONObject toJSONObject() throws ConfigurationException {
    try {
      JSONObject json = new JSONObject();
      if (actPanelConfig != null) {
        json.put(ACTIVITY_PANEL_CONFIG, actPanelConfig.toJSONObject());
      }
      if (rvnPanelConfig != null) {
        json.put(REVENUE_PANEL_CONFIG, rvnPanelConfig.toJSONObject());
      }
      if (ordPanelConfig != null) {
        json.put(ORDER_PANEL_CONFIG, ordPanelConfig.toJSONObject());
      }
      if (invPanelConfig != null) {
        json.put(INVENTORY_PANEL_CONFIG, invPanelConfig.toJSONObject());
      }
      if (dbOverConfig != null) {
        json.put(DASHBOARD_CONFIG, dbOverConfig.toJSONObject());
      }
      return json;
    } catch (Exception e) {
      throw new ConfigurationException(e.getMessage());
    }
  }

  public static class ActivityPanelConfig implements Serializable {
    /**
     *
     */
    private static final long serialVersionUID = 1L;

    private static final String SHOW_ACTIVITY_PANEL = "showactpanel";

    public boolean showActivityPanel = true;

    public ActivityPanelConfig() {
    }

    public ActivityPanelConfig(JSONObject json) throws JSONException {
      try {
        showActivityPanel = json.getBoolean(SHOW_ACTIVITY_PANEL);
      } catch (Exception e) {
        // ignore
      }
    }

    public JSONObject toJSONObject() throws JSONException {
      JSONObject json = new JSONObject();
      json.put(SHOW_ACTIVITY_PANEL, showActivityPanel);
      return json;
    }
  }

  public static class RevenuePanelConfig implements Serializable {
    /**
     *
     */
    private static final long serialVersionUID = 1L;

    private static final String SHOW_REVENUE_PANEL = "showrvnpanel";

    public boolean showRevenuePanel = false;

    public RevenuePanelConfig() {
    }

    public RevenuePanelConfig(JSONObject json) throws JSONException {
      try {
        showRevenuePanel = json.getBoolean(SHOW_REVENUE_PANEL);
      } catch (Exception e) {
        // ignore
      }
    }

    public JSONObject toJSONObject() throws JSONException {
      JSONObject json = new JSONObject();
      json.put(SHOW_REVENUE_PANEL, showRevenuePanel);
      return json;
    }
  }

  public static class OrderPanelConfig implements Serializable {
    /**
     *
     */
    private static final long serialVersionUID = 1L;

    private static final String SHOW_ORDER_PANEL = "showordpanel";

    public boolean showOrderPanel = true;

    public OrderPanelConfig() {
    }

    public OrderPanelConfig(JSONObject json) throws JSONException {
      try {
        showOrderPanel = json.getBoolean(SHOW_ORDER_PANEL);
      } catch (Exception e) {
        // ignore
      }
    }

    public JSONObject toJSONObject() throws JSONException {
      JSONObject json = new JSONObject();
      json.put(SHOW_ORDER_PANEL, showOrderPanel);
      return json;
    }
  }

  public static class InventoryPanelConfig implements Serializable {
    /**
     *
     */
    private static final long serialVersionUID = 1L;

    private static final String SHOW_INVENTORY_PANEL = "showinvpanel";

    public boolean showInvPanel = true;

    public InventoryPanelConfig() {
    }

    public InventoryPanelConfig(JSONObject json) throws JSONException {
      try {
        showInvPanel = json.getBoolean(SHOW_INVENTORY_PANEL);
      } catch (Exception e) {
        // ignore
      }
    }

    public JSONObject toJSONObject() throws JSONException {
      JSONObject json = new JSONObject();
      json.put(SHOW_INVENTORY_PANEL, showInvPanel);
      return json;
    }
  }

  public static class DBOverviewConfig implements Serializable {
    private static final String DEFAULT_MATERIAL_TAG = "dmtg";
    private static final String DEFAULT_INV_MATERIAL_TAG = "dimtg";
    private static final String ENABLE_DASHBOARD_MANAGERS = "edm";
    private static final String DEFAULT_ENTITY_TAG = "detg";
    private static final String DEFAULT_TRANSACTION_TYPE = "dtt";
    private static final String ATD_DISABLED = "atdd";
    private static final String EXCLUDE_ENTITY_TAG = "exet";
    private static final String EXCLUDE_TEMP_STATUS_TAG = "exts";
    private static final String ACTIVITY_PERIOD = "aper";
    private static final String DISABLE_BY_USER_TAG = "dutg";

    public String dmtg;
    public String dimtg;
    public String detg;
    public String aper;
    public String dtt;
    public boolean edm;
    public boolean atdd;
    public String exet;
    public String exts;
    public String dutg;

    public DBOverviewConfig() {
    }

    public DBOverviewConfig(JSONObject json) throws JSONException {
      try {
        dmtg = json.optString(DEFAULT_MATERIAL_TAG);
        dimtg = json.optString(DEFAULT_INV_MATERIAL_TAG);
        detg = json.optString(DEFAULT_ENTITY_TAG);
        dtt = json.optString(DEFAULT_TRANSACTION_TYPE);
        atdd = json.optBoolean(ATD_DISABLED);
        edm = json.optBoolean(ENABLE_DASHBOARD_MANAGERS);
        exet = json.optString(EXCLUDE_ENTITY_TAG);
        exts = json.optString(EXCLUDE_TEMP_STATUS_TAG);
        aper = json.optString(ACTIVITY_PERIOD);
        dutg = json.optString(DISABLE_BY_USER_TAG);
      } catch (Exception ignored) {
      }
    }

    public JSONObject toJSONObject() throws JSONException {
      JSONObject json = new JSONObject();
      json.put(DEFAULT_MATERIAL_TAG, dmtg);
      json.put(DEFAULT_INV_MATERIAL_TAG, dimtg);
      json.put(DEFAULT_ENTITY_TAG, detg);
      json.put(DEFAULT_TRANSACTION_TYPE, dtt);
      json.put(ATD_DISABLED, atdd);
      json.put(ENABLE_DASHBOARD_MANAGERS, edm);
      json.put(EXCLUDE_ENTITY_TAG, exet);
      json.put(EXCLUDE_TEMP_STATUS_TAG, exts);
      json.put(ACTIVITY_PERIOD, aper);
      json.put(DISABLE_BY_USER_TAG, dutg);
      return json;
    }
  }
}

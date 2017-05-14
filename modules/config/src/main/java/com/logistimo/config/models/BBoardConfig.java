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

import java.io.Serializable;

/**
 * @author Arun
 */
public class BBoardConfig implements Serializable {

  public static final int DISABLED = 0;
  public static final int PRIVATE = 1;
  public static final int PUBLIC = 2;
  // Scroll interval default
  public static final int SCROLL_INTERVAL_DEFAULT = 2000; // milliseconds
  public static final int DATA_DURATION_DEFAULT = 30; // days
  public static final int REFRESH_DURATION_DEFAULT = 3600; // seconds
  public static final int MAX_ITEMS = 100;

  private static final long serialVersionUID = 1L;
  // JSON tags
  private static final String ENABLE = "enble";
  private static final String DATA_DURATION = "ddur";
  private static final String REFRESH_DURATION = "rdur";
  private static final String SCROLL_INTERVAL = "sint";
  private static final String PAUSE_ON_HOVER = "phvr";
  private static final String SHOW_NAV = "shnv";
  private static final String MAXITEMS = "mxitms";

  private int enabled = DISABLED;
  private int dataDuration = DATA_DURATION_DEFAULT; // days
  private int refreshDuration = REFRESH_DURATION_DEFAULT; // seconds
  private int scrollInterval = SCROLL_INTERVAL_DEFAULT; // milliseconds
  private boolean pauseOnHover = false;
  private boolean showNav = false; // show slide navigator
  private int maxItems = MAX_ITEMS;

  public BBoardConfig() {
  }

  public BBoardConfig(int enabled, int dataDuration, int refreshDuration, int scrollInterval,
                      boolean pauseOnHover, boolean showNav, int maxItems) {
    this.enabled = enabled;
    this.dataDuration = dataDuration;
    this.refreshDuration = refreshDuration;
    this.scrollInterval = scrollInterval;
    this.pauseOnHover = pauseOnHover;
    this.showNav = showNav;
    this.maxItems = maxItems;
  }

  public BBoardConfig(JSONObject json) throws JSONException {
    enabled = json.getInt(ENABLE);
    dataDuration = json.getInt(DATA_DURATION);
    refreshDuration = json.getInt(REFRESH_DURATION);
    scrollInterval = json.getInt(SCROLL_INTERVAL);
    pauseOnHover = json.getBoolean(PAUSE_ON_HOVER);
    showNav = json.getBoolean(SHOW_NAV);
    try {
      maxItems = json.getInt(MAXITEMS);
    } catch (Exception e) {
      // ignore
    }
  }

  public JSONObject toJSONObject() throws JSONException {
    JSONObject json = new JSONObject();
    json.put(ENABLE, enabled);
    json.put(DATA_DURATION, dataDuration);
    json.put(REFRESH_DURATION, refreshDuration);
    json.put(SCROLL_INTERVAL, scrollInterval);
    json.put(PAUSE_ON_HOVER, pauseOnHover);
    json.put(SHOW_NAV, showNav);
    json.put(MAXITEMS, maxItems);
    return json;
  }

  public int getEnabled() {
    return enabled;
  }

  public boolean isEnabled() {
    return (enabled != DISABLED);
  }

  public int getDataDuration() {
    return dataDuration;
  }

  public int getRefreshDuration() {
    return refreshDuration;
  }

  public int getScrollInterval() {
    return scrollInterval;
  }

  public boolean pauseOnHover() {
    return pauseOnHover;
  }

  public boolean showNav() {
    return showNav;
  }

  public int getMaxItems() {
    return maxItems;
  }
}

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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


public class StockboardConfig implements Serializable {
  public static final String KIOSKCONFIG = "kioskconfig";

  public static final int DISABLED = 0;
  public static final int PRIVATE = 1;
  public static final int PUBLIC = 2;
  public static final int
      SCROLL_INTERVAL_DEFAULT =
      5000;
  // Scroll interval for the vertical ticker in milliseconds
  public static final int REFRESH_DURATION_DEFAULT = 3600; // seconds
  public static final int MAX_ITEMS = 10; // Number of items in view on the vertical ticker
  public static final int
      HOR_SCROLL_INTERVAL_DEFAULT =
      10000;
  // Horizontal Scroll interval default in milliseconds
  private static final long serialVersionUID = 1L;

  // JSON tags
  private static final String ENABLE = "enble";
  private static final String MAXITEMS = "mxitms";
  private static final String REFRESH_DURATION = "rdur";
  private static final String SCROLL_INTERVAL = "sint";
  private static final String HOR_SCRL_MESSAGES = "horscrmsgs";
  private static final String HOR_SCROLL_INTERVAL = "hsint";

  private int enabled = DISABLED;
  private int maxItems = MAX_ITEMS;
  private int refreshDuration = REFRESH_DURATION_DEFAULT; // seconds
  private int scollInterval = SCROLL_INTERVAL_DEFAULT; // milliseconds
  private List<String> horScrlMsgs = null; // List of horizontal scroll messages
  private int hsint = HOR_SCROLL_INTERVAL_DEFAULT; // milliseconds

  public StockboardConfig() {

  }

  public StockboardConfig(int enabled, int maxItems, int refreshDuration, int scrollInterval,
                          List<String> horizontalScrollMsgs, int horScrollInterval) {
    this.enabled = enabled;
    this.maxItems = maxItems;
    this.refreshDuration = refreshDuration;
    this.scollInterval = scrollInterval;
    this.horScrlMsgs = horizontalScrollMsgs;
    this.hsint = horScrollInterval;
  }

  public StockboardConfig(String jsonString) throws ConfigurationException {
    if (jsonString != null && !jsonString.isEmpty()) {
      try {
        JSONObject json = new JSONObject(jsonString);
        this.enabled = json.getInt(ENABLE);
        this.maxItems = json.getInt(MAXITEMS);
        this.scollInterval = json.getInt(SCROLL_INTERVAL);
        this.refreshDuration = json.getInt(REFRESH_DURATION);
        // Optional
        try {
          JSONArray horScrMsgsArray = (JSONArray) json.get(HOR_SCRL_MESSAGES);
          if (horScrMsgsArray.length() > 0) {
            horScrlMsgs = new ArrayList<String>();
            for (int i = 0; i < horScrMsgsArray.length(); i++) {
              horScrlMsgs.add((String) horScrMsgsArray.get(i));
            }
          }
        } catch (JSONException je) {
          // ignore
        }
        // Optional
        try {
          this.hsint = json.getInt(HOR_SCROLL_INTERVAL);
        } catch (JSONException je) {
          // ignore
        }
      } catch (JSONException je) {
        throw new ConfigurationException(je.getMessage());
      }
    }
  }

  public String toJSONString() throws JSONException {
    String jsonString = toJSONObject().toString();
    return jsonString;
  }

  public JSONObject toJSONObject() throws JSONException {
    JSONObject json = new JSONObject();
    json.put(ENABLE, enabled);
    json.put(MAXITEMS, maxItems);
    json.put(REFRESH_DURATION, refreshDuration);
    json.put(SCROLL_INTERVAL, scollInterval);
    if (this.horScrlMsgs != null && !this.horScrlMsgs.isEmpty()) {
      JSONArray horScrMsgsJson = new JSONArray();
      // Iterate through the horizontal scroll messages.
      Iterator<String> horscrmsgsIter = horScrlMsgs.iterator();
      while (horscrmsgsIter.hasNext()) {
        horScrMsgsJson.put(horscrmsgsIter.next());
      }
      json.put(HOR_SCRL_MESSAGES, horScrMsgsJson);
    }
    json.put(HOR_SCROLL_INTERVAL, hsint);
    return json;
  }

  public int getEnabled() {
    return enabled;
  }

  public boolean isEnabled() {
    return (enabled != DISABLED);
  }

  public void setEnabled(int enabled) {
    this.enabled = enabled;
  }

  public int getRefreshDuration() {
    return refreshDuration;
  }

  public void setRefreshDuration(int refreshDuration) {
    this.refreshDuration = refreshDuration;
  }

  public int getScrollInterval() {
    return scollInterval;
  }

  public void setScrollInterval(int scrollInterval) {
    this.scollInterval = scrollInterval;
  }

  public int getMaxItems() {
    return maxItems;
  }

  public void setMaxItems(int maxItems) {
    this.maxItems = maxItems;
  }

  public List<String> getHorScrlMsgsList() {
    return horScrlMsgs;
  }

  public void setHorScrlMsgsList(List<String> horScrlMsgsList) {
    this.horScrlMsgs = horScrlMsgsList;
  }

  public int getHorScrollInterval() {
    return this.hsint;
  }

  public void setHorScrollInterval(int horScrollInterval) {
    this.hsint = horScrollInterval;
  }
}



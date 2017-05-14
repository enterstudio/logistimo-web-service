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

/**
 * Created by vani on 31/03/17.
 */

public class LeadTimeAvgConfig implements Serializable {

  // Constants
  private static final long serialVersionUID = 1L;
  public static final int MINIMUM_NUMBER_OF_ORDERS_DEFAULT = 3;
  public static final int MAXIMUM_NUMBER_OF_ORDERS_DEFAULT = 30;
  public static final int MAX_ORDER_PERIODS_DEFAULT = 6;

  // Tags
  private static final String MINIMUM_NUMBER_OF_ORDERS = "minno";
  private static final String MAXIMUM_NUMBER_OF_ORDERS = "maxno";
  private static final String MAXIMUM_ORDER_PERIODS = "maxnop";
  private static final String EXCLUDE_ORDER_PROCESSING_TIME_FROM_LEADTIME_COMPUTATION = "exopt";


  public int minNumOfOrders = MINIMUM_NUMBER_OF_ORDERS_DEFAULT;
  public int maxNumOfOrders = MAXIMUM_NUMBER_OF_ORDERS_DEFAULT;
  public float maxOrderPeriods = MAX_ORDER_PERIODS_DEFAULT;
  public boolean excludeOrdProcTime = false;


  public LeadTimeAvgConfig() {
  }

  public LeadTimeAvgConfig(JSONObject json) {
    if (json != null) {
      minNumOfOrders = json.optInt(MINIMUM_NUMBER_OF_ORDERS, this.minNumOfOrders);
      maxNumOfOrders = json.optInt(MAXIMUM_NUMBER_OF_ORDERS, this.maxNumOfOrders);
      maxOrderPeriods = new Float(json.optDouble(MAXIMUM_ORDER_PERIODS, this.maxOrderPeriods));
      excludeOrdProcTime =
          json.optBoolean(EXCLUDE_ORDER_PROCESSING_TIME_FROM_LEADTIME_COMPUTATION,
              this.excludeOrdProcTime);
    }
  }

  public int getMinNumOfOrders() {
    return this.minNumOfOrders;
  }
  public void setMinNumOfOrders(int minNumOfOrders) {
    this.minNumOfOrders = minNumOfOrders;
  }
  public int getMaxNumOfOrders() {
    return this.maxNumOfOrders;
  }
  public void setMaxNumOfOrders(int maxNumOfOrders) {
    this.maxNumOfOrders = maxNumOfOrders;
  }
  public float getMaxOrderPeriods() {
    return this.maxOrderPeriods;
  }
  public void setMaxOrderPeriods(float maxOrderPeriods) {
    this.maxOrderPeriods = maxOrderPeriods;
  }
  public boolean getExcludeOrderProcTime() {
    return this.excludeOrdProcTime;
  }
  public void setExcludeOrderProcTime(boolean excludeOrderProcTime) {
    this.excludeOrdProcTime = excludeOrderProcTime;
  }

  public JSONObject toJSONObject() throws JSONException {
    JSONObject json = new JSONObject();
    json.put(MINIMUM_NUMBER_OF_ORDERS, minNumOfOrders);
    json.put(MAXIMUM_NUMBER_OF_ORDERS, maxNumOfOrders);
    json.put(MAXIMUM_ORDER_PERIODS, maxOrderPeriods);
    json.put(EXCLUDE_ORDER_PROCESSING_TIME_FROM_LEADTIME_COMPUTATION, excludeOrdProcTime);
    return json;
  }
}


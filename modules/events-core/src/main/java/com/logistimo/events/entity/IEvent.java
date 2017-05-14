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

package com.logistimo.events.entity;

import com.logistimo.domains.ISuperDomain;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.jdo.PersistenceManager;

/**
 * Created by charan on 20/05/15.
 */
public interface IEvent extends ISuperDomain {
  // Event IDs
  // General
  int CREATED = 1;
  int MODIFIED = 2;
  int DELETED = 3;
  int NOT_CREATED = 4;
  int EXPIRED = 5;
  int NO_ACTIVITY = 6;
  int DUPLICATED = 7;
  // Orders
  int FULFILLMENT_DUE = 100;
  int STATUS_CHANGE = 101;
  int STATUS_CHANGE_TEMP = 102;
  int COMMENTED = 150;
  // Inventory
  int NORMAL = -1;
  int STOCKOUT = 200;
  int UNDERSTOCK = 201;
  int OVERSTOCK = 202;
  int STOCKCOUNT_EXCEEDS_THRESHOLD = 203;
  int STOCK_COUNTED = 204;
  int STOCK_ISSUED = 205;
  int STOCK_RECEIVED = 206;
  int STOCK_WASTED = 207;
  int
      STOCK_REPLENISHED =
      208;
  // to normal from abnormal level (say, from stockout, min/max stock levels)
  int STOCK_TRANSFERRED = 209;
  // Payment
  int CREDIT_LIMIT_EXCEEDED = 300;
  int PAYMENT_DUE = 301;
  int PAID = 302;
  // User related
  int IP_ADDRESS_MATCHED = 400; // IP address of request matched
  //Asset
  int HIGH_EXCURSION = 500;
  int LOW_EXCURSION = 501;
  int INCURSION = 502;
  int NO_DATA_FROM_DEVICE = 503;
  int HIGH_WARNING = 504;
  int LOW_WARNING = 505;
  int HIGH_ALARM = 506;
  int LOW_ALARM = 507;
  int SENSOR_DISCONNECTED = 508;
  int SENSOR_CONNECTION_NORMAL = 509;
  int BATTERY_LOW = 510;
  int BATTERY_ALARM = 511;
  int BATTERY_NORMAL = 512;
  int ASSET_INACTIVE = 513;
  int ASSET_ACTIVE = 514;
  int POWER_OUTAGE = 515;
  int POWER_NORMAL = 516;
  int DEVICE_DISCONNECTED = 517;
  int DEVICE_CONNECTION_NORMAL = 518;
  List<Integer> ASSET_ALARM_GROUP = new ArrayList<Integer>() {{
    add(SENSOR_DISCONNECTED);
    add(BATTERY_ALARM);
    add(BATTERY_LOW);
    add(ASSET_INACTIVE);
    add(POWER_OUTAGE);
    add(DEVICE_DISCONNECTED);
  }};

  List<Integer> ASSET_ALARM_NORMAL_GROUP = new ArrayList<Integer>() {{
    add(SENSOR_CONNECTION_NORMAL);
    add(BATTERY_NORMAL);
    add(ASSET_ACTIVE);
    add(POWER_NORMAL);
    add(DEVICE_CONNECTION_NORMAL);
  }};

  // Status-es
  int STATUS_CREATED = 0;

  IEvent init(Long dId, int id, Map<String, Object> params, String objectType, String objectId,
              boolean isRealTime, String message, Map<Integer, List<String>> userIds,
              long etaMillis);

  IEvent init(Long dId, int id, Map<String, Object> params, String objectType, String objectId,
              boolean isRealTime, String message, Map<Integer, List<String>> userIds,
              long etaMillis, Date eventTime);

  Long getKey();

  int getId();

  void setId(int id);

  Map<String, Object> getParams();

  void setParams(Map<String, Object> params);

  Long getDomainId();

  void setDomainId(Long domainId);

  String getObjectType();

  void setObjectType(String objectType);

  String getObjectId();

  void setObjectId(String oId);

  String getMessage();

  void setMessage(String message);

  // Get user Ids by frequency of notification
  @SuppressWarnings("unchecked")
  Map<Integer, List<String>> getUserIds();

  void setUserIds(Map<Integer, List<String>> userIds);

  Date getTimestamp();

  void setTimestamp(Date t);

  int getStatus();

  void setStatus(int status);

  boolean isRealTime();

  void setRealTime(boolean isRealTime);

  Long getEtaMillis();

  void setEtaMillis(Long etaMillis);

  boolean hasCustomOptions();

  // Get the object on which this event occurred - e.g. inventory, order, material, entity, useraccount, and so on
  Object getObject(PersistenceManager pm);
}

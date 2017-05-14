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

package com.logistimo.config.entity;

import java.util.Date;

/**
 * Created by charan on 20/05/15.
 */
public interface IConfig {
  // Configuration keys
  String COUNTRIES = "countries"; // mapping of ISO country codes and names
  String LOCATIONS = "locations"; // hierarchical locations, exception countries
  String LANGUAGES = "languages"; // mapping of ISO language codes and namesCon


  String LANGUAGES_MOBILE = "languages_mobile"; // list of languages supported on mobile app.
  String CURRENCIES = "currencies"; // mapping of currency
  String OPTIMIZATION = "optimization"; // optimization parameter defaults
  String ORDERS = "orders"; // order configuration
  String REPORTS = "reports";
  String SMSCONFIG = "smsconfig";
  String PAYMENTPROVIDERSCONFIG = "paymentprovidersconfig";
  // public static final String TEMPERATUREMONITORINGCONFIG = "tempmonitoringconfig";
  String ASSETSYSTEMCONFIG = "temperaturesysconfig";
  String MAPLOCATIONCONFIG = "maplocationconfig";
  String DASHBOARDCONFIG = "dashboardconfig";
  String GENERALCONFIG = "generalconfig";
  String CONFIG_PREFIX = "config.";
  String CONFIG_KIOSK_PREFIX = "config.kiosk.";
  // Configuration attributes (within a given config.)
  String
      ATTR_AUTO =
      "auto";
  // true/false; whether automatic or not (e.g. auto-ordering or not, after order transaction
  // Configuration values (standard ones)
  String TRUE = "true";
  String FALSE = "false";

  IConfig init(IConfig iConfig);

  IConfig copyFrom(IConfig config);

  String getKey();

  void setKey(String key);

  String getConfig();

  void setConfig(String configStr);

  Date getLastUpdated();

  void setLastUpdated(Date lastUpdated);

  String getUserId();

  void setUserId(String userId);

  Long getDomainId();

  void setDomainId(Long domainId);

  String getPrevConfig();

  void setPrevConfig(String prevConfigStr);

  String getString(String attribute);
}

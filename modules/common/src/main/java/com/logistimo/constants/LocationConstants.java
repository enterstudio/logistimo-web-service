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

package com.logistimo.constants;

import com.logistimo.services.utils.ConfigUtil;

/**
 * Created by yuvaraj on 25/05/17.
 */
public class LocationConstants {


  public static final String
      LS_URL =
      ConfigUtil.get("location.service.url", "http://localhost:9090") + "/locations/city";
  public static final Integer TIMED_OUT = ConfigUtil.getInt("locations.service.timeout", 5000);
  public static final String
      FALLBACK_ENDPOINT =
      ConfigUtil.get("location.service.fallback", "seda:location");

  public static final String CONTENT_TYPE = "application/json";
  public static final String COUNTRY_LITERAL = "countryCode";
  public static final String STATE_LITERAL = "state";
  public static final String DIST_LITERAL = "district";
  public static final String SUBDIST_LITERAL = "taluk";
  public static final String CITY_LITERAL = "city";
  public static final String APP_LITERAL = "appName";
  public static final String APP_NAME = "logistimo";
  public static final String USER_LITERAL = "userName";
  public static final String COUNTRYID_LITERAL = "countryId";
  public static final String STATEID_LITERAL = "stateId";
  public static final String DISTID_LITERAL = "districtId";
  public static final String SUBDISTID_LITERAL = "talukId";
  public static final String CITYID_LITERAL = "placeId";
  public static final String LAT_LITERAL = "latitude";
  public static final String LONG_LITERAL = "longitude";
  public static final String ZIP_LITERAL = "pincode";
  public static final String KIOSKID_LITERAL = "kioskId";
  public static final String USERID_LITERAL = "userId";
  public static final String USER_TYPE_LITERAL = "user";
  public static final String KIOSK_TYPE_LITERAL = "kiosk";
  public static final String STATUS_TYPE_LITERAL = "status";
  public static final String SUCCESS_LITERAL = "success";
  public static final String FAILURE_LITERAL = "failed";
}

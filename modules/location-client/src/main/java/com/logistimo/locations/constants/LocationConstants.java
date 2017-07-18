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

package com.logistimo.locations.constants;

import com.logistimo.services.utils.ConfigUtil;

public class LocationConstants {

  private static final String
      LS_URL =
      ConfigUtil.get("location.service.url", "http://localhost:9090");
  private static final String LS_PATH = ConfigUtil.get("location.service.path", "/locations/ids");
  public static final Integer TIMED_OUT = ConfigUtil.getInt("location.service.timeout", 5000);
  public static final String APP_NAME = "logistimo";
  public static final String CLIENT_NAME = "LocationServiceClient";
  public static final String URL = LS_URL.concat(LS_PATH);
  public static final String STATE_LITERAL = "state";
  public static final String DIST_LITERAL = "district";
  public static final String SUBDIST_LITERAL = "taluk";
  public static final String ZIP_LITERAL = "pincode";
  public static final String STREET_LITERAL = "street";
}

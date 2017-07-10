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

package com.logistimo.utils;

/**
 * Created by vani on 07/07/17.
 */
public class FieldLimits {
  private FieldLimits() {
  }
  // Limits for fields
  public static final int USERID_MIN_LENGTH = 4;
  public static final int USERID_MAX_LENGTH = 20;
  public static final int FIRSTNAME_MIN_LENGTH = 1;
  public static final int MOBILE_PHONE_MAX_LENGTH = 20;
  public static final int AGE_MIN = 1;
  public static final int AGE_MAX = 99;
  public static final int STREET_ADDRESS_MAX_LENGTH = 200;
  public static final int TEXT_FIELD_MAX_LENGTH = 50;
  public static final int PASSWORD_MIN_LENGTH = 6;
  public static final int PASSWORD_MAX_LENGTH = 18;
  public static final int EMAIL_MAX_LENGTH = 100;
  public static final double LATITUDE_MIN = -90;
  public static final double LATITUDE_MAX = 90;
  public static final int LAT_LONG_MAX_DIGITS_AFTER_DECIMAL = 8;
  public static final double LONGITUDE_MIN = -180;
  public static final double LONGITUDE_MAX = 180;
  public static final double TEMP_MAX_VALUE = 99.99;
  public static final double TEMP_MIN_VALUE = -99.99;
  public static final double TAX_MIN_VALUE = 0.00;
  public static final double TAX_MAX_VALUE = 100.00;
  public static final String SYSTEM_DETERMINED_REPLENISHMENT = "sq";
  public static final int MATERIAL_NAME_MIN_LENGTH = 1;
  public static final int MATERIAL_SHORTNAME_MAX_LENGTH = 6;
  public static final int MATERIAL_DESCRIPTION_MAX_LENGTH = 200;
  public static final int MATERIAL_ADDITIONAL_INFO_MAX_LENGTH = 400;
  public static final int MIN_SERVICE_LEVEL = 65;
  public static final int MAX_SERVICE_LEVEL = 99;
}

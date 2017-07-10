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
 * Created by vani on 23/06/17.
 */
public class PatternConstants {
  private PatternConstants() {
  }
  public static final String TAX = "^(\\d{0,2}(\\.\\d{1,2})?|100(\\.00?)?)$";
  public static final String PRICE = "^(\\d{0,9}(\\.\\d{1,2})?|1000000000(\\.00?)?)$";
  public static final String LATITUDE = "^-?(([0-8])?((([0-9])(\\.\\d{1,8})?)|(90(\\.[0]{0,8})?)))$";
  public static final String LONGITUDE = "^-?(((1[0-7][0-9])|(\\d{1,2})|180)(\\.\\d{1,8})?)$";
  public static final String TEMPERATURE = "^-?(\\d{0,2}(\\.\\d{1,2})?)$";
  // Email validation regex is as specified in the UI by Angular JS
  public static final String EMAIL = "^(?=.{1,254}$)(?=.{1,64}@)[-!#$%&'*+/0-9=?A-Z^_`a-z{|}~]+(\\.[-!#$%&'*+/0-9=?A-Z^_`a-z{|}~]+)*@[A-Za-z0-9]([A-Za-z0-9-]{0,61}[A-Za-z0-9])?(\\.[A-Za-z0-9]([A-Za-z0-9-]{0,61}[A-Za-z0-9])?)*$";
  public static final String USERID = "[a-z0-9\\.\\-_@\\s]+";
  public static final String FIRSTNAME = "[A-Za-z ]+";
  public static final String LASTNAME = "[A-Za-z ]*";
  public static final String ZIPCODE = "[A-Za-z0-9- ]{1," + FieldLimits.TEXT_FIELD_MAX_LENGTH + "}";
}

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

import java.io.UnsupportedEncodingException;
import java.util.Enumeration;
import java.util.Locale;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;

/**
 * Created by charan on 29/08/17.
 */
public class UTF8PropertyResourceBundle extends ResourceBundle {

  private final PropertyResourceBundle propertyResourceBundle;
  private Locale locale;

  public UTF8PropertyResourceBundle(PropertyResourceBundle propertyResourceBundle, Locale locale) {
    this.propertyResourceBundle = propertyResourceBundle;
    this.locale = locale;
  }

  @Override
  public String handleGetObject(String key) {
    String value = propertyResourceBundle.getString(key);
    try {
      return new String(value.getBytes("ISO-8859-1"), "UTF-8");
    } catch (UnsupportedEncodingException e) {
      return value;
    }
  }

  @Override
  public Enumeration<String> getKeys() {
    return propertyResourceBundle.getKeys();
  }

  @Override
  public Locale getLocale() {
    return locale;
  }
}

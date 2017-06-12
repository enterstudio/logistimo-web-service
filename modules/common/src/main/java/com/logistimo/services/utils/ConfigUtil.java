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

package com.logistimo.services.utils;

import org.apache.commons.lang.StringUtils;
import com.logistimo.constants.Constants;

import java.io.IOException;
import java.util.Properties;

/**
 * Created by charan on 14/11/14.
 */
public class ConfigUtil {


  private static final Properties properties;

  static {
    properties = new Properties();
    try {
      properties.load(Thread.currentThread().getContextClassLoader()
          .getResourceAsStream("samaanguru.properties"));

    }catch(Exception e) {
      throw new RuntimeException("Unable to load samaanguru.properties", e);
    }

    try{
      properties.load(Thread.currentThread().getContextClassLoader()
          .getResourceAsStream("beans.properties"));
    } catch (IOException e) {
      e.printStackTrace();
    }

    properties.putAll(System.getProperties());
  }

  public static String get(String name) {
    return (String) getObject(name);
  }

  public static Object getObject(String name) {
    return properties.get(name);
  }

  public static String get(String name, String defaultValue) {
    String retVal = get(name);
    return retVal == null ? defaultValue : retVal;
  }

  public static boolean getBoolean(String key, boolean defaultValue) {
    return Boolean.parseBoolean(get(key, Boolean.toString(defaultValue)));
  }

  public static int getInt(String key, int defaultValue) {
    String value = get(key);
    return value == null ? defaultValue : Integer.parseInt(value);
  }

  public static double getDouble(String key, double defaultValue) {
    String value = get(key);
    return value == null ? defaultValue : Double.parseDouble(value);
  }

  public static boolean isGAE() {
    return getBoolean(Constants.GAE_DEPLOYMENT, true);
  }

  public static boolean isLogi() {
    return !isGAE();
  }

  public static String[] getCSVArray(String key, String[] defaultValue) {
    String value = get(key);
    if (value != null) {
      return StringUtils.split(value, ",");
    }
    return defaultValue;
  }

  public static String[] getCSVArray(String key) {
    return getCSVArray(key, null);
  }

  public static boolean isLocal() {
    return getBoolean("local.environment", true);
  }

  public static String getDomain() {
    return ConfigUtil.get("logi.domain", Constants.DEFAULT);
  }

  public static Properties getProperties() {
    return (Properties) properties.clone();
  }
}

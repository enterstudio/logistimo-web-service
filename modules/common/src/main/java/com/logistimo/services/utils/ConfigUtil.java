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

      properties.load(Thread.currentThread().getContextClassLoader()
          .getResourceAsStream("beans.properties"));

    } catch (IOException e) {
      e.printStackTrace();
    }
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
}

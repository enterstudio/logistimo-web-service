package com.logistimo.utils;

import com.google.gson.GsonBuilder;

/**
 * Created by charan on 24/06/17.
 */
public class GsonUtils {

  private static GsonBuilder
      builder =
      new GsonBuilder().setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");

  private GsonUtils() {

  }

  /**
   * Uses Json conventions of Logistimo to covert object
   * specifically the date format at this time.
   *
   * @param o - any object
   * @return - json string
   */
  public static String toJson(Object o) {
    return builder.create().toJson(o);
  }
}

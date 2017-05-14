/**
 *
 */
package com.logistimo.utils;

import com.logistimo.services.Resources;

import java.util.Locale;
import java.util.ResourceBundle;

/**
 * @author Arun
 */
public class GeoUtil {
  // Get the geo-error message string
  public static String getGeoErrorMessage(String geoErrorCode, Locale locale) {
    if (geoErrorCode == null || geoErrorCode.isEmpty()) {
      return null;
    }
    ResourceBundle messages = Resources.get().getBundle("Messages", locale);
    String str;
    switch (geoErrorCode) {
      case "1":
        str = messages.getString("geocodes.error.1");
        break;
      case "2":
        str = messages.getString("geocodes.error.2");
        break;
      case "3":
        str = messages.getString("geocodes.error.3");
        break;
      default:
        str = messages.getString("geocodes.error.someerror");
        break;
    }
    return str;
  }
}

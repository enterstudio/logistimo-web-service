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

/**
 *
 */
package com.logistimo.services;

import com.logistimo.logger.XLog;
import com.logistimo.utils.UTF8PropertyResourceBundle;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;

/**
 * Loads resources once and stored as a singleton
 *
 * @author Arun
 */
public class Resources {
  // Logger
  private static final XLog xLogger = XLog.getLog(Resources.class);
  // Singleton
  private static final Resources SINGLETON = new Resources();
  // Resource bundles
  private final Map<String, ResourceBundle> rmap = new HashMap<String, ResourceBundle>();

  public static Resources get() {
    return SINGLETON;
  }

  // Get a resource bundle read in UTF-8 format (by default, ResourceBundle would read it in ISO-8859-1 format)
  private static ResourceBundle getUTF8Bundle(String baseName, Locale locale) {
    ResourceBundle bundle = ResourceBundle.getBundle(baseName, locale);
    xLogger.fine("Resources.getUTF8Bundle: bundle = {0}, locale = {1}", bundle, bundle.getLocale());
    if (!(bundle instanceof PropertyResourceBundle)) {
      return bundle;
    }
    return new UTF8PropertyResourceBundle((PropertyResourceBundle) bundle, locale);

  }

  public ResourceBundle getBundle(String baseName, Locale locale) throws MissingResourceException {
    if (baseName == null || locale == null) {
      return null;
    }
    // Get the resource bundle, if not already present
    String key = baseName + "_" + locale.toString();
    xLogger.fine("Resources.getBundle(): trying first key = {0}", key);
    ResourceBundle bundle = rmap.get(key);
    if (bundle == null) {
      key = baseName + "_" + locale.getLanguage();
      bundle = rmap.get(key);
      xLogger.fine("Resources.getBundle(): tried second key = {0}, bundle = {1}", key, bundle);
      if (bundle == null) {
        bundle = getUTF8Bundle(baseName, locale);
        key =
            baseName + "_" + bundle.getLocale()
                .toString(); // actual (fallback) locale used to get the file
        xLogger.fine(
            "Resource.getBundle(): getting it first time using locale = {0}, actual key = {0}",
            locale.toString(), key);
        rmap.put(key, bundle);
      }
    }
    return bundle;
  }

  public void destroy() {
    rmap.clear();
  }
}

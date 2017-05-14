/**
 *
 */
package com.logistimo.services;

import com.logistimo.logger.XLog;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;
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

  // Get a resource bundle read in UTF-8 format (by default, ResourceBunndle would read it in ISO-8859-1 format)
  private static ResourceBundle getUTF8Bundle(String baseName, Locale locale) {
    ResourceBundle bundle = ResourceBundle.getBundle(baseName, locale);
    xLogger.fine("Resources.getUTF8Bundle: bundle = {0}, locale = {1}", bundle, bundle.getLocale());
    return bundle;
                /*
                if ( !( bundle instanceof PropertyResourceBundle ) )
			return bundle;
		return new UTF8PropertyResourceBundle( (PropertyResourceBundle) bundle );
		*/
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

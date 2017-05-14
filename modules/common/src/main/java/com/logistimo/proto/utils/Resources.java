package com.logistimo.proto.utils;

import java.io.IOException;
import java.util.Hashtable;


public class Resources {
  // Languages (default)
  public static final String ENGLISH = "en";
  // Properties
  private static Resources SINGLETON = new Resources();
  private Hashtable propsMap = new Hashtable(); // map of resource bundle key and Properties object

  public static Resources get() {
    return SINGLETON;
  }

  // Get the generic locale (typically, language), or return null if locale is already generic (i.e. only one token in locale)
  private static String getGenericLocale(String locale) {
    StringTokenizer st = new StringTokenizer(locale, "_");
    String genericLocale = null;
    if (st.countTokens()
        > 1) { // implies this has language and country (and possibly an additional custom token)
      genericLocale = st.nextToken(); // get the language (skip country/others)
    }
    return genericLocale;
  }

  // Get the resource bundle; do a search from specific to generic locale
  public ResourceBundle getBundle(String baseName, String locale) throws IOException {
    String
        key =
        baseName + "_" + locale; // Messages_en.properties is the file that holsd english messages.
    ResourceBundle r = (ResourceBundle) propsMap.get(key);
    if (r == null) {
      String genericKey = null;
      String genericLocale = getGenericLocale(locale);
      if (genericLocale != null) {
        genericKey = baseName + "_" + genericLocale;
        r = (ResourceBundle) propsMap.get(genericKey);
      }
      if (r == null) {
        try {
          r = new ResourceBundle(Properties.loadProperties("/" + key + ".properties"));
        } catch (Exception e) {
          if (genericLocale == null) {
            throw new IOException(e.getMessage());
          }
          key = genericKey;
          r = new ResourceBundle(Properties.loadProperties("/" + key + ".properties"));
        }
      }
      propsMap.put(key, r);
    }
    return r;
  }
}

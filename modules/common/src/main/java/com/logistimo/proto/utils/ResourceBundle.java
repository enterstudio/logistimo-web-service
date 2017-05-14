package com.logistimo.proto.utils;

public class ResourceBundle {
  // Languages (default)
  public static final String ENGLISH = "en";
  // Properties
  private Properties props = null;

  public ResourceBundle(Properties props) {
    this.props = props;
  }

  public String getString(String key) {
    return props.getProperty(key);
  }
}

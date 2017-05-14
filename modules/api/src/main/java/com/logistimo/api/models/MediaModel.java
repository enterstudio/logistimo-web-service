package com.logistimo.api.models;

/**
 * Warning: Variable name should not be changed. This is synced with google appengine Media object.
 *
 * @author Mohan Raja
 */
public class MediaModel {
  public Long id;
  public String mediaType;
  public String domainKey;
  public MediaText content;
  public String servingUrl;
  public String fn;

  public static class MediaText {
    public String value;
  }
}

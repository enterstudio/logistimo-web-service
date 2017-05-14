package com.logistimo.api.models;

import java.util.List;

/**
 * This is the wrapper class to send all images as items.
 * Warning: Variable name should not be changed. This is synced with google appengine Media object.
 *
 * @author Mohan Raja
 */
public class MediaModels {
  public List<MediaModel> items;

  public MediaModels(List<MediaModel> items) {
    this.items = items;
  }
}

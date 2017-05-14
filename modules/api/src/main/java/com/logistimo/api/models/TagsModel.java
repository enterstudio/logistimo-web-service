package com.logistimo.api.models;

import java.util.List;

/**
 * Created by charan on 15/04/15.
 */
public class TagsModel {

  /**
   * Tags list
   */
  public List<String> tags;

  /**
   * Allow User Defined tags
   */
  public boolean udf;

  public TagsModel(List<String> tags, boolean allowUserDef) {
    this.tags = tags;
    this.udf = allowUserDef;
  }
}

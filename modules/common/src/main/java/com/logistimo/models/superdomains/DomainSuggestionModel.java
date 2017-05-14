package com.logistimo.models.superdomains;

/**
 * Created by naveensnair on 01/07/15.
 */
public class DomainSuggestionModel {
  public Long id;
  public String text;
  public String dsc; // linked domain description
  public String dct; // linked domain created time
  public int type; // linked domain type
  public String key; // domain link key
  public int sno; // domain link key
  public boolean dc; // direct child
  public boolean dp; // direct parent
  public boolean hc; //has children
  public boolean hp; //has parent
  public boolean mc; // manage configuration
}

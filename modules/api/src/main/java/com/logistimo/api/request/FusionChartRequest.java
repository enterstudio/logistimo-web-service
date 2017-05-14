package com.logistimo.api.request;

/**
 * Created by Mohan Raja on 19/02/15
 */
public class FusionChartRequest {

  public String freq; // Frequency
  public String rty; // Report Type
  public String stDate; // Start Date
  public String enDate; // End Date
  public boolean daily; // Daily Month

  // Filters
  public Long mid; // Material Id
  public String uid; // User Id
  public String st; // State
  public String dis; // District
  public Long egrp; // Entity Group
  public Long eid; // Entity
  public String etag; // Entity Tag
  public String mtag; // Material Tag

}

package com.logistimo.api.models.configuration;

import java.util.List;

/**
 * Created by mohan raja.
 */
public class GeneralConfigModel {
  public Long domainId;
  public String cnt;
  public String st;
  public String ds;
  public String lng;
  public String tz;
  public String cur;
  public String pgh;
  public boolean sc;
  public String createdBy;
  public String lastUpdated;
  public String fn;
  public List<SupportConfigModel> support;
  public boolean snh; // Enable switch to new host
  public String nhn; // New host name
}

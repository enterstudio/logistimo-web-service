package com.logistimo.api.models;

import java.util.List;

/**
 * Created by yuvaraj on 16/07/16.
 */
public class HUModel {
  public Long id;
  public Long sdId; // source domain ID
  public String sdname; // source domain name
  public String name;
  public List<HUContentModel> contents;
  public String description;
  public String timeStamp; // creation date
  public String lastUpdated;
  public String cb;
  public String cbName;
  public String ub;
  public String ubName;

}

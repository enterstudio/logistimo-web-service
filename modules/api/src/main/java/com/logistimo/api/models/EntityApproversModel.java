package com.logistimo.api.models;

import java.util.Date;
import java.util.List;

/**
 * Created by naveensnair on 22/05/17.
 */
public class EntityApproversModel {
  public Long entityId;
  public List<UserModel> pap;
  public List<UserModel> sap;
  public List<UserModel> pas;
  public List<UserModel> sas;
  public String createdBy; //user who last updated config
  public Date lastUpdated; //last updated time
  public String fn; //first name
}

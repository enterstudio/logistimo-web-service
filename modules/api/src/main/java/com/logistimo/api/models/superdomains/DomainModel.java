package com.logistimo.api.models.superdomains;

import com.logistimo.models.superdomains.DomainSuggestionModel;

import java.util.List;

/**
 * Created by naveensnair on 30/06/15.
 */
public class DomainModel {
  public String dn; //Current domain name
  public Long dId; //Current domain id
  public List<DomainSuggestionModel> ldl; //linked domain list
  public DomainPermissionModel dp; //domain permission list
  public String name; // domain name
  public String createdOn;
  public String description;
  public String lastUpdatedBy;
  public String lastUpdatedByn;
  public String lastUpdatedOn;
  public String ownerId;
  public String ownerName;
  public boolean isActive;
  public boolean hasChild;

}

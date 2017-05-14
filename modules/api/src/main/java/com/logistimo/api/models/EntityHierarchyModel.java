package com.logistimo.api.models;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * Created by naveensnair on 15/10/16.
 */
public class EntityHierarchyModel implements Serializable {
  public Long id;
  public String name;
  public List<EntityHierarchyModel> children;
  public Integer level;
  public Integer maxlevel = 0;
  public Integer links = 0;
  public Date upd;
  public String updatedOn;
}

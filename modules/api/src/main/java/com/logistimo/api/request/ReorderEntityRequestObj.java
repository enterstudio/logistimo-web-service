package com.logistimo.api.request;

import com.logistimo.api.models.EntityModel;

import java.util.List;

/**
 * Created by mohan raja on 23/12/14
 */
public class ReorderEntityRequestObj {
  public List<EntityModel> ordEntities;
  public String lt; // Link Type
  public Long eid; //Entity Id
  public String uid; //User Id - Managed Entities
  public boolean rta;
}

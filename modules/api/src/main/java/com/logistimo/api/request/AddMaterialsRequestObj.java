package com.logistimo.api.request;

import com.logistimo.api.models.InventoryModel;

import java.util.List;

/**
 * Created by naveensnair on 28/10/14.
 */
public class AddMaterialsRequestObj {
  public List<InventoryModel> materials;
  public List<Long> entityIds;
  public String userId;

  public Long domainId;
  public boolean execute;
  public boolean overwrite;
}

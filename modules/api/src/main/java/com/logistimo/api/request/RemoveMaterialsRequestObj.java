package com.logistimo.api.request;

import com.logistimo.api.models.InventoryModel;

import java.util.List;

/**
 * Created by naveensnair on 03/11/14.
 */
public class RemoveMaterialsRequestObj {
  public List<InventoryModel> materials;
  public List<Long> entityIds;
}

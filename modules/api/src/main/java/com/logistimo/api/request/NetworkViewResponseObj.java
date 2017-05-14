package com.logistimo.api.request;


import com.logistimo.api.models.EntityHierarchyModel;

import java.io.Serializable;
import java.util.Map;
import java.util.Set;

/**
 * Created by naveensnair on 12/10/16.
 */
public class NetworkViewResponseObj implements Serializable {
  public EntityHierarchyModel network;
  public Map<Long, Set<Long>> extraLinks;
  public Map<Long, Set<Long>> multipleParentLinks;

}

package com.logistimo.api.models;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by charan on 03/11/15.
 */
public class InvDashboardModel implements Serializable {
  /**
   * State or District or Kiosk name
   */
  public String dim;
  /**
   * Material wise type distribution
   */
  public Map<Long, EventDistribModel> eve = new HashMap<>(3);
  /**
   * Dimension id.. applicable only for Kiosk at this time.
   */
  public long id;

  public Map<String, Integer> assets = new HashMap<>(4);
}

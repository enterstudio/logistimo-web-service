package com.logistimo.api.models;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by smriti on 10/18/16.
 */
public class DemandBreakdownModel {
  /**
   * Order id
   */
  public Long orderId;
  /**
   * Quantity for type Order
   */
  public BigDecimal oQty;
  /**
   * Batch id
   */
  public String bid;
  /**
   * Batch expiry
   */
  public String bexp;
  /**
   * Available stock for each batch
   */
  public BigDecimal batpstk;
  /**
   * Map of type id and quantity for batch material
   */
  public Map<String, BigDecimal> allocations = new HashMap<>();
  /**
   * Material id
   */
  public Long matId;
  /**
   * Kiosk id
   */
  public Long kId;
  /**
   * Batch quantity
   */
  public BigDecimal bQty;
  /**
   * Material status
   */
  public String mst;
  /**
   * Allocated quantity
   */
  public BigDecimal aQty;

}

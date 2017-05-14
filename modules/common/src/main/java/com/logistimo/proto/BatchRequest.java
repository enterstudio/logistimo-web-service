package com.logistimo.proto;

import java.math.BigDecimal;

/**
 * Created by charan on 01/11/16.
 */
public class BatchRequest {
  /**
   * Batch Id
   */
  public String bid;

  /**
   * Allocated quantity
   */
  public BigDecimal alq;
  /**
   * Material status
   */
  public String mst;
}

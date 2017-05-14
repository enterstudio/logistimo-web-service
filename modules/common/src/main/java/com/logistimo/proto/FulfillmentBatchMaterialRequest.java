package com.logistimo.proto;

import java.math.BigDecimal;

/**
 * Created by vani on 05/11/16.
 */
public class FulfillmentBatchMaterialRequest {
  /**
   * Batch ID
   */
  public String bid;
  /**
   * Received quantity
   */
  public BigDecimal q;
  /**
   * Fulfilled batch material status
   */
  public String fmst;
  /**
   * Reason for partial fulfillment
   */
  public String rsnpf;
}

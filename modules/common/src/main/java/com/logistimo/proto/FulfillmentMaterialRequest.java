package com.logistimo.proto;

import java.math.BigDecimal;
import java.util.List;

/**
 * Created by vani on 05/11/16.
 */
public class FulfillmentMaterialRequest {
  /**
   * Material ID
   */
  public Long mid;
  /**
   * Fulfilled quantity
   */
  public BigDecimal q;
  /**
   * Reason for partial fulfillment
   */
  public String rsnpf;
  /**
   * Fulfilled material status
   */
  public String fmst;
  /**
   * Batch material list
   */
  public List<FulfillmentBatchMaterialRequest> bt;

}

package com.logistimo.proto;

import java.math.BigDecimal;

/**
 * Created by vani on 03/11/16.
 */
public class MobileShipmentItemBatchModel {
  /**
   * Batch ID
   */
  public String bid;
  /**
   * Allocated quantity
   */
  public BigDecimal alq;
  /**
   * Shipped quantity
   */
  public BigDecimal q;
  /**
   * Fulfilled quantity
   */
  public BigDecimal flq;
  /**
   * Batch expiry
   */
  public String bexp;
  /**
   * Batch manufactured name
   */
  public String bmfnm;
  /**
   * Batch manufactured date
   */
  public String bmfdt;
  /**
   * Batch updated timestamp -- DD/MM/YYYY
   */
  public String t;
  /**
   * Shipped material status
   */
  public String mst;
  /**
   * Fulfilled material status
   */
  public String fmst;
  /**
   * Reason for partial fulfillment
   */
  public String rsnpf;
}

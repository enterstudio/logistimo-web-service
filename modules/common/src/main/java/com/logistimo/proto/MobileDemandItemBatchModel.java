package com.logistimo.proto;

import java.math.BigDecimal;

/**
 * Created by vani on 03/11/16.
 */
public class MobileDemandItemBatchModel {
  /**
   * Batch ID
   */
  public String bid;
  /**
   * Allocated quantity
   */
  public BigDecimal alq;
  /**
   * Batch expiry - DD/MM/YYYY
   */
  public String bexp;
  /**
   * Batch manufactured name
   */
  public String bmfnm;
  /**
   * Batch manufactured date - DD/MM/YYYY
   */
  public String bmfdt;
  /**
   * Batch updated timestamp
   */
  public String t;
  /**
   * Batch material status
   */
  public String mst;

}

package com.logistimo.proto;

import java.math.BigDecimal;
import java.util.List;

/**
 * Created by charan on 01/11/16.
 */
public class MaterialRequest {

  /**
   * Material Id
   */
  public Long mid;

  /**
   * Quantity ordered
   */
  public BigDecimal q;

  /**
   * Allocated quantity
   */
  public BigDecimal alq;

  /**
   * Micro message by user
   */
  public String ms;

  /**
   * Ignore recommended order quantity reason code
   */
  public String rsn;

  /**
   * Reason for editing order quantity
   */
  public String rsneoq;

  /**
   * Batch allocations
   */
  public List<BatchRequest> bt;

  /**
   * is Batch - false
   */
  public boolean isBa = false;
  /**
   * Material status
   */
  public String mst;
}

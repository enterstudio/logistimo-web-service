package com.logistimo.api.models;

import java.math.BigDecimal;
import java.util.List;

/**
 * Created by yuvaraj on 24/04/17.
 */
public class MobileInvModel {

  /**
   * Material Name
   */
  public String mnm;
  /**
   * Current Stock (Stock in Hand)
   */
  public BigDecimal stk = BigDecimal.ZERO; // current stock
  /**
   * Min
   */
  public BigDecimal reord = BigDecimal.ZERO; // re-order levels or MIN
  /**
   * Max
   */
  public BigDecimal max = BigDecimal.ZERO;
  /**
   * Allocated stock
   */
  public BigDecimal astk = BigDecimal.ZERO;
  /**
   * In transit stock
   */
  public BigDecimal tstk = BigDecimal.ZERO;
  /**
   * get tags by type
   */
  public List<String> tgs;
  /**
   * Predicted days of stock
   */
  public BigDecimal pdos;
  /**
   * Stock Availability Period
   */
  public BigDecimal sap;
  /**
   * Material Description
   */
  public String mDes;
  /**
   * Last updated time of reorder-level or min
   */
  public String reordT;
}

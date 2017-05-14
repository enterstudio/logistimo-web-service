package com.logistimo.proto;

import java.math.BigDecimal;

/**
 * Created by vani on 18/01/17.
 */
public class MobileTransactionModel {
  /**
   * Material id
   */
  public Long mid;
  /**
   * Material name - optional
   */
  public String mnm;
  /**
   * Type of transaction
   */
  public String ty;
  /**
   * Quantity
   */
  public BigDecimal q;
  /**
   * Timestamp
   */
  public String t;
  /**
   * Reason
   */
  public String rsn;
  /**
   * Material status
   */
  public String mst;
  /**
   * Opening stock
   */
  public BigDecimal ostk;
  /**
   * Closing stock
   */
  public BigDecimal cstk;
  /**
   * User ID
   */
  public String uid;
  /**
   * User name
   */
  public String u;
  /**
   * Linked kiosk id
   */
  public Long lkid;
  /**
   * Linked kiosk name
   */
  public String lknm;
  /**
   * Batch ID
   */
  public String bid;
  /**
   * Opening stock in batch
   */
  public BigDecimal ostkb;
  /**
   * Batch expiry - DD/MM/YYYY
   */
  public String bexp;
  /**
   * Batch manufacturer name
   */
  public String bmfnm;
  /**
   * Batch manufactured date - DD/MM/YYYY
   */
  public String bmfdt;
  /**
   * Closing stock in batch
   */
  public BigDecimal cstkb;
  /**
   * Actual date of transaction
   */
  public String atd;
  /**
   * Tracking object type
   */
  public String troty;
  /**
   * Tracking object id
   */
  public String trid;
}
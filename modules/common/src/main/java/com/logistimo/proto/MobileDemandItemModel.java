package com.logistimo.proto;

import java.math.BigDecimal;
import java.util.List;

/**
 * Created by vani on 03/11/16.
 */
public class MobileDemandItemModel {
  /**
   * Material Id
   */
  public Long mid;
  /**
   * Material name
   */
  public String mnm;
  /**
   * Material status
   */
  public String mst;
  /**
   * Quantity ordered
   */
  public BigDecimal q;
  /**
   * Original ordered quantity
   */
  public BigDecimal oq;
  /**
   * Recommended ordered quantity
   */
  public BigDecimal roq;

  /**
   * Allocated quantity
   */
  public BigDecimal alq;
  /**
   * Fulfilled quantity
   */
  public BigDecimal flq;
  /**
   * Reason for editing order quantity
   */
  public String rsneoq;
  /**
   * Retail price
   */
  public BigDecimal rp;
  /**
   * Currency
   */
  public String cu;
  /**
   * Status of the demand item
   */
  public String ost;
  /**
   * Last updated time
   */
  public String t;
  /**
   * Message
   */
  public String ms;
  /**
   * Custom material id
   */
  public String cmid;
  /**
   * Reason for ignoring recommended order quantity -- DEPRECATED
   */
  public String rsn;
  /**
   * Reason for ignoring recommended order quantity
   */
  public String rsnirq;
  /**
   * Demand item batches
   */
  public List<MobileDemandItemBatchModel> bt;

}

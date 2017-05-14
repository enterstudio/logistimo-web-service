package com.logistimo.models.shipments;

import java.math.BigDecimal;

/**
 * Created by Mohan Raja on 05/10/16
 */
public class ShipmentItemBatchModel {
  public BigDecimal q;
  /**
   * Old allocated quantity
   */
  public BigDecimal oq;
  public BigDecimal fq;
  /**
   * Order allocated Quantity
   */
  public BigDecimal oastk;
  public String mr;
  public String bmfdt;
  public String e;
  public String id;
  public String bmfnm;

  public Long kid;
  public Long sdid;
  public Long siId;
  public String uid;
  public Long mid;
  public String mnm;
  public BigDecimal huQty;
  public String huName;
  /**
   * Shipped material status
   */
  public String smst;
  /**
   * Fulfilled material status
   */
  public String fmst;
  /**
   * Fulfilled discrepancy reason
   */
  public String frsn;
  /**
   * Fulfilled discrepancy reason
   */
  public String sid;
}

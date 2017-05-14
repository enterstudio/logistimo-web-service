package com.logistimo.models.shipments;

import java.math.BigDecimal;
import java.util.List;

/**
 * Created by Mohan Raja on 29/09/16
 */
public class ShipmentItemModel {
  public Long mId;
  public String mnm;
  public BigDecimal q;
  public BigDecimal aq;
  public BigDecimal fq;
  /**
   * Allocate from order
   */
  public boolean afo;
  public boolean isBa;
  public List<ShipmentItemBatchModel> bq;

  public BigDecimal astk;
  public Long kid;
  public Long sdid;
  public String sid;
  public String uid;
  public BigDecimal vs;
  public BigDecimal vmin;
  public BigDecimal vmax;
  public BigDecimal vsavibper;
  public BigDecimal atpstk;
  public BigDecimal stk;
  public BigDecimal max;
  public BigDecimal min;
  public BigDecimal csavibper;
  public BigDecimal huQty;
  public String huName;
  /**
   * Fulfilled material status
   */
  public String fmst;
  /**
   * Fulfil discrepancy reason
   */
  public String frsn;
  /**
   * Shipped material status
   */
  public String smst;
  /**
   * Order Allocated Stock
   */
  public BigDecimal oastk;
  /**
   * Available to allocate quantity
   */
  public BigDecimal aaq;
  /**
   * Temperature sensitive material
   */
  public Boolean tm;

}

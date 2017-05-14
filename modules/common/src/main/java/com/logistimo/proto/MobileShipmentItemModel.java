package com.logistimo.proto;

import java.math.BigDecimal;
import java.util.List;

/**
 * Created by vani on 03/11/16.
 */
public class MobileShipmentItemModel {
  /**
   * Material id
   */
  public Long mid;
  /**
   * Material name
   */
  public String mnm;
  /**
   * Allocated quantity
   */
  public BigDecimal alq;
  /**
   * Quantity shipped
   */
  public BigDecimal q;
  /**
   * Fulfilled quantity
   */
  public BigDecimal flq;
  /**
   * Reason for partial fulfillment
   */
  public String rsnpf;
  /**
   * Last updated time
   */
  public String t;
  /**
   * Message
   */
  public String ms;
  /**
   * Shipped material status
   */
  public String mst;
  /**
   * Fulfilled material status
   */
  public String fmst;
  /**
   * Custom material id
   */
  public String cmid;
  /**
   * Shipment Item batches
   */
  public List<MobileShipmentItemBatchModel> bt;

}

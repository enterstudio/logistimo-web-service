package com.logistimo.proto;


import java.util.Date;
import java.util.List;

/**
 * Created by vani on 05/11/16.
 */
public class UpdateOrderStatusRequest {
  /**
   * String version
   */
  public String v;
  /**
   * String userId
   */
  public String uid;
  /**
   * Kiosk/Entity Id
   */
  public Long kid;
  /**
   * Order type
   */
  public String oty = "prc";
  /**
   * Vendor id
   */
  public Long vid;
  /**
   * Tracking ID-Order ID
   */
  public Long tid;
  /**
   * Shipment ID
   */
  public String sid;
  /**
   * Order status
   */
  public String ost;
  /**
   * Message
   */
  public String ms;
  /**
   * Transporter
   */
  public String trsp;
  /**
   * Transporter tracking ID
   */
  public String trid;
  /**
   * Expected Date of arrival
   */
  public Date ead;
  /**
   * Reason for cancellation
   */
  public String rsnco;
  /**
   * Materials for partial fulfillment
   */
  public List<FulfillmentMaterialRequest> mt;
  /**
   * Date of actual receipt
   */
  public Date dar;
  /**
   * Package size
   */
  public String pksz;

}

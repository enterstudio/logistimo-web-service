package com.logistimo.proto;

import java.util.List;

/**
 * Created by vani on 03/11/16.
 */
public class MobileShipmentModel {
  /**
   * Shipment ID
   */
  public String sid;
  /**
   * Status
   */
  public String st;
  /**
   * Shipment status updated on
   */
  public String t;
  /**
   * Status changed to shipped on
   */
  public String ssht;
  /**
   * Shipment updated by user id
   */
  public String uid;
  /**
   * Shipment updated by user name
   */
  public String n;
  /**
   * Transporter name
   */
  public String trsp;
  /**
   * Transporter ID
   */
  public String trid;
  /**
   * Reason for partial shipment
   */
  public String rsnps;
  /**
   * Reason for cancelling shipment
   */
  public String rsnco;
  /**
   * Estimated date of arrival
   */
  public String ead;
  /**
   * Date of actual receipt
   */
  public String dar;
  /**
   * Shipment items
   */
  public List<MobileShipmentItemModel> mt;
  /**
   * Package size
   */
  public String pksz;
  /**
   * Comments
   */
  public MobileConversationModel cmnts;

}

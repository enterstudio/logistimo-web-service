package com.logistimo.models.shipments;



import com.logistimo.shipments.ShipmentStatus;

import java.util.List;

/**
 * Created by Mohan Raja on 29/09/16
 */
public class ShipmentModel {
  public Long orderId;
  public Long customerId;
  public Long vendorId;
  public List<ShipmentItemModel> items;
  public ShipmentStatus status;
  public String transporter;
  public String trackingId;
  public String reason;
  public String cdrsn;
  /**
   * Expected arrival date
   */
  public String ead;
  public List<String> tags;
  public Double latitude;
  public Double longitude;
  public Double geoAccuracy;
  public String geoError;
  /**
   * Shipment Id
   */
  public String sId;
  /**
   * Number or items
   */
  public int noi;
  /**
   * Created by user full name
   */
  public String createdBy;
  /**
   * Created by user Id
   */
  public String userID;
  /**
   * Created on
   */
  public String cOn;
  /**
   * Source domain Id
   */
  public Long sdid;
  /**
   * Updated by user full name
   */
  public String upBy;
  /**
   * Updated by user id
   */
  public String upId;
  /**
   * Updated on
   */
  public String upOn;

  // other fields
  /**
   * Actual fulfilment date
   */
  public String afd;
  /**
   * Shipment Status to be changed
   */
  public String changeStatus;
  public String sdname;
  public String customerAdd;
  public String vendorAdd;
  public String customerName;
  public String vendorName;
  public String statusCode;
  /**
   * Access to vendor
   */
  public Boolean atv = true;
  /**
   * Access to view vendor
   */
  public Boolean atvv = true;
  /**
   * Access to customer
   */
  public Boolean atc = true;
  /**
   * Access to view customer
   */
  public Boolean atvc = true;
  /**
   * Package size
   */
  public String ps;
  /**
   * Order type
   */
  public Integer oty;
  /**
   * Display text for expected arrival date
   */
  public String eadLabel;
  /**
   * Order update time
   */
  public String orderUpdatedAt;

}

package com.logistimo.models.orders;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * Created by vani on 14/10/16.
 */
public class DiscrepancyModel {
  public List<String> discTypes; // Discrepancies
  public Long id; // Demand item ID
  public Long oId; // Order ID
  public Date oct; // Order creation timestamp
  public String rId; // Order reference ID
  public Integer oty; // Order type
  public Long mId; // Material ID
  public String cmId; // Custom material ID
  public String mnm; // Material name
  public BigDecimal oq; // Ordered quantity
  public String odrsn; // Order discrepancy reason
  public BigDecimal roq = new BigDecimal(0); // Recommended ordered quantity
  public BigDecimal sq = new BigDecimal(0); // Shipped quantity
  public String sdrsn; // Shipping discrepancy reason
  public BigDecimal fq = new BigDecimal(0); // Fulfilled quantity
  public List<String> fdrsns; // List of Shipment ID:Fulfillment discrepancy reasons
  public String st; // Status
  public Date stt; // Status updated timestamp
  public Long cId; // Customer kiosk ID
  public String ccId; // Custom customer kiosk ID
  public String cnm; // Customer kiosk name
  public Long vId; // Vendor Kiosk ID
  public String cvId; // Custom vendor ID
  public String vnm; // Vendor kiosk name
  public Long sdid; // Source domain ID
}

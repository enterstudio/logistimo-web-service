package com.logistimo.api.models;

import java.math.BigDecimal;
import java.util.List;

/**
 * Created by vani on 04/10/16.
 */
public class DemandItemDiscModel {
  public int sno; // Serial number
  public Long diId; // Demand item ID
  public List<String> disc; // Discrepancies
  public Long oid; // Order ID
  public Integer oty; // Order type
  public String otyStr; // Order type string
  public Long mid; // Material ID
  public String mnm; // Material name
  public BigDecimal oq; // Ordered quantity
  public String odRsn; // Ordering discrepancy reason
  public String fdRsn; // Fulfillment discrepancy reason
  public BigDecimal roq; // Recommended ordered quantity
  public BigDecimal sq; // Shipped quantity
  public BigDecimal fq; // Fulfilled quantity
  public String st; // Status code
  public String status; // Status string used in display
  public String stt; // Status updated timestamp
  public Long cid; // Customer Kiosk ID
  public String cnm; // Customer Kiosk name
  public String cadd; // Customer Kiosk address
  public Long vid; // Vendor Kiosk ID
  public String vnm; // Vendor Kiosk name
  public String vadd; // Vendor Kiosk address
  public String cby; // Created by
  public String cbyun; // Created by full name
  public String uby; // Updated by
  public String con; // Created on
  public String uon; // Last updated on
  public Long sdid; // Source domain ID
  public String sdnm; // Source domain name
}

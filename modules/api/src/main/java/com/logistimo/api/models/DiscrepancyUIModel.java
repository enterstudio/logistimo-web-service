package com.logistimo.api.models;

import java.math.BigDecimal;
import java.util.List;

/**
 * Created by vani on 15/10/16.
 */
public class DiscrepancyUIModel {
  public int sno; // Serial number
  public Long id; // Demand item ID
  public List<String> disc; // Discrepancies
  public boolean hasOd; // Has Ordering discrepancy
  public boolean hasSd; // Has Shipping discrepancy
  public boolean hasFd; // Has Fulfillment discrepancy
  public Long oid; // Order ID
  public String oct; // Order creation timestamp
  public String rid; // Order reference ID
  public Integer oty; // Order type
  public String otyStr; // Order type string
  public Long mid; // Material ID
  public String mnm; // Material name
  public BigDecimal roq; // Recommended ordered quantity
  public BigDecimal oq; // Ordered quantity
  public String odRsn; // Ordering discrepancy reason
  public String odRsnStr; // Ordering discrepancy reason display string (HTML)
  public BigDecimal od; // Ordering discrepancy (oq-roq)
  public BigDecimal sq; // Shipped quantity
  public String sdRsn; // Shipping discrepancy reason
  public String sdRsnStr; // Shipping discrepancy reason display string (HTML)
  public BigDecimal sd; // Shipping discrepancy (sq-oq)
  public BigDecimal fq; // Fulfilled quantity
  public List<String> fdRsns; // Fulfillment discrepancy reason
  public String fdRsnsStr; // Fulfillment discrepancy reasons used in display (HTML)
  public BigDecimal fd; // Fulfillment discrepancy (fq-sq)
  public String st; // Status code
  public String status; // Status string used in display
  public String stt; // Status updated timestamp
  public Long cid; // Customer Kiosk ID
  public String cnm; // Customer Kiosk name
  public String cadd; // Customer Kiosk address
  public Long vid; // Vendor Kiosk ID
  public String vnm; // Vendor Kiosk name
  public String vadd; // Vendor Kiosk address
  public Long sdid; // Source domain ID
  public String sdnm; // Source domain name
}

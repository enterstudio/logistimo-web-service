package com.logistimo.api.models;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;

public class OrderModel {

  public Long id;
  public Integer size;
  /**
   * Currency
   */
  public String cur;
  public String price;
  /**
   * Status text
   */
  public String status;
  public String cdt;
  public String udt;
  public String msg;
  public BigDecimal tax;
  public String uid;
  public Long eid;
  public String vid;
  public String vnm = "";
  public String enm = "";
  /**
   * Available Credit
   */
  public BigDecimal avc;
  /**
   * Available Credit Error
   */
  public String avcerr;

  /**
   * Latitude
   */
  public Double lt;
  /**
   * Longitude
   */
  public Double ln;
  public Double ac;
  /**
   * Entity Latitude
   */
  public double elt;
  /**
   * Entity Longitude
   */
  public double eln;
  /**
   * Price Statement
   */
  public String pst;
  /**
   * Order Package Size
   */
  public String pkgs;
  /**
   * Paid amount
   */
  public BigDecimal pd;
  /**
   * Payment Option
   */
  public String po;
  /**
   * Processing time
   */
  public String pt;
  /**
   * Delivery Lead time
   */
  public String dlt;

  /**
   * Order Total Price
   */

  public BigDecimal tp;

  /**
   * Items
   */
  public Set<DemandModel> its;

  /**
   * Status Code
   */
  public String st;

  /**
   * Transporter
   */
  public String trns;

  /**
   * Order Tags
   */
  public List<String> tgs;

  /**
   * Serial Number for display
   */
  public int sno;
  /**
   * Confirmed Fulfillment Time range
   */
  public String cft;
  /**
   * Expected Fulfillment Time range
   */
  public LinkedHashMap<String, String> eft;
  /**
   * User name
   */
  public String unm;
  /**
   * Updated by
   */
  public String uby;
  /**
   * Has Vendor access
   */
  public boolean hva;
  /**
   * Source domain id
   */
  public Long sdid;
  /**
   * Source domain name
   */
  public String sdname;
  /**
   * Customer address
   */
  public String eadd;
  /**
   * Vendor address
   */
  public String vadd;
  /**
   * Updated by
   */
  public String ubid;
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
   * Order type
   */
  public Integer ty;
  /**
   * Order reason
   */
  public String crsn;
  /**
   * Order type
   */
  public Integer oty;
  /**
   * Reference Id
   */
  public String rid;
  /**
   * Expected fulfillment date
   */
  public String efd;
  /**
   * Expected due date
   */
  public String edd;
  /**
   * allowCancel
   */
  public boolean alc;
  /**
   * Display text for expected due date
   */
  public String eddLabel;
  /**
   * Display text for expected fulfillment time
   */
  public String efdLabel;

  /**
   * Order update time
   */
  public String orderUpdatedAt;
}

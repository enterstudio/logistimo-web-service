package com.logistimo.api.models;

import java.math.BigDecimal;
import java.util.List;

public class TransactionModel {

  public String id;
  public int sno;
  public Long mid;
  public String mnm;
  public String type;
  public String ty;
  public Long eid;
  public String enm;
  public BigDecimal q;
  public String ts;
  public String uid;
  public BigDecimal cs;
  public BigDecimal os;
  public Double lt;
  public Double ln;
  public Double ac; //accuracy
  public Long lkId; // Customer/Vendor Id
  public String lknm = ""; //Customer/Vendor Name
  public double lklt; //Customer/Vendor Latitude
  public double lkln; //Customer/Vendor Longitude
  public String bid; // batch id/number
  public String bexp; // batch expiry date
  public String bmfnm; // batch manufacturer name
  public String bmfdt; // batch manufactured date
  public BigDecimal csb;
  public BigDecimal osb;
  public String resn;
  public String unm;
  public List<String> mtgs;
  public Long sdid; // source domain
  public String sdname; // source domain name
  public String st; // state
  public String ds; // district
  public String ct; // city name
  public String eadd; // entity address
  public String lkadd; // Customer/Vendor Address
  public String trkid; // Tracking ID if transaction is linked to an order.
  public String trkObTy; // Tracking object type
  public Long trnId; // Transaction ID
  public Integer trnSrc; // Transaction source
  public boolean enMap; // Entity lat long is shown
  public String mst;
  public String atd;
}

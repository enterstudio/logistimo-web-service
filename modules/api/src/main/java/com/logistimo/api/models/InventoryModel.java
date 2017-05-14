package com.logistimo.api.models;

import com.logistimo.constants.Constants;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public class InventoryModel {

  public int sno;
  public Long invId;
  public Long mId;
  public String mnm;
  public Long kId;
  public Long sdid;
  public BigDecimal reord = BigDecimal.ZERO; // re-order levels or MIN
  public BigDecimal max = BigDecimal.ZERO;
  public BigDecimal minDur = BigDecimal.ZERO;
  public BigDecimal maxDur = BigDecimal.ZERO;
  public List<String> tgs;
  public BigDecimal crMnl;
  public BigDecimal stk = BigDecimal.ZERO; // current stock
  /**
   * Available stock
   */
  public BigDecimal atpstk = BigDecimal.ZERO;
  /**
   * In transit stock
   */
  public BigDecimal tstk = BigDecimal.ZERO;
  /**
   * Allocated stock
   */
  public BigDecimal astk = BigDecimal.ZERO;
  public BigDecimal ldtdmd = BigDecimal.ZERO; // lead time demand
  public BigDecimal ldt = new BigDecimal(Constants.LEADTIME_DEFAULT); // lead time
  public BigDecimal rvpdmd = BigDecimal.ZERO; // forecasted demand (review period demand)
  public BigDecimal stdv = BigDecimal.ZERO; // std. deviation of rev. period demand
  public BigDecimal ordp = BigDecimal.ZERO; // days; order periodicity
  public BigDecimal sfstk = BigDecimal.ZERO; // safety stock
  public String t; // creation or last stock update timestamp
  public int event = -1; // event Type
  public long period; //Stock Event warning duration
  public BigDecimal rp; //Retailers Price
  public BigDecimal tx = BigDecimal.ZERO; //Tax
  public boolean enOL; //enable ReOrder Level
  public float tmin = 0F; //temperature Min
  public float tmax = 0F; //Temperature Max
  public String im; //Inventory Model
  public float sl; //Service Level
  public String enm; //Entity Name
  public double lt; //Latitude
  public double ln; //Longitude
  public String b; //Binary Valued Material

  public boolean be; // Batch Enabled
  public boolean ts; // Temperature Sensitive
  public BigDecimal sap; // Stock Availability Period
  public BigDecimal crd; // Consumption Rate Daily
  public BigDecimal crw; // Consumption Rate Weekly
  public BigDecimal crm; // Consumption Rate Monthly
  public BigDecimal eoq; // Economic Order Quantity
  public int eventType = -1; // event Type

  public String omsg; //optional message
  public String pst; //last updated time for P and/or S
  public String dqt; //last updated time for D and/or Q

  public int anmCnt; //abnormal item count
  public float anmTemp; //abnormal item temperature
  public String sdname;
  public String add; //address

  // Updating timestamps
  public String reordT; // last updated time of reorder-level or min
  public String maxT; // last updated time of max level
  public String crMnlT; // last updated time of manual consumption rate
  public String rpT; // last update time of retailer price
  public String updtBy; // last updated user of inventory
  public String fn; // full name of user who last updated inventory

  public Map<String, Integer> assets;

  public List<String> eTags; //Default entity tags to filter configured in inventory configuration
  public BigDecimal huQty; // Handling unit quantity
  public String huName; // Handling unit Name

  public BigDecimal pdos; // Predicted days of stock
  public int matType = 0; // All
  public boolean onlyNZStk = false;
}

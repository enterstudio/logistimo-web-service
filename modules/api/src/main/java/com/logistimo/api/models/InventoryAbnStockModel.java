package com.logistimo.api.models;

import java.math.BigDecimal;
import java.util.Date;

/**
 * Created by mohan raja on 18/11/14
 */
public class InventoryAbnStockModel {
  public String mnm; // Material Name
  public String enm; // Entity Name
  public BigDecimal st; // Stock
  public BigDecimal min; // Stock
  public BigDecimal max; // Stock
  public long du; // Duration
  public Date stDt; // Start Date
  public Date edDt; // End Date
  public Long kid; // Kiosk ID
  public Long mid; // Material ID
  public int type; // Event Type
  public String stDtstr; // locale date string
  public String edDtstr; //locale date string
  public Long sdid; // Source domain id
  public String sdname; // Source domain name
  public String add; // address
  public BigDecimal minDur = BigDecimal.ZERO;
  public BigDecimal maxDur = BigDecimal.ZERO;
}

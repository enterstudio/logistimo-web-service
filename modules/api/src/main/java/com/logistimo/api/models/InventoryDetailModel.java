package com.logistimo.api.models;

import com.logistimo.api.models.mobile.CurrentStock;

import java.math.BigDecimal;
import java.util.List;

/**
 * Created by yuvaraj on 04/05/17.
 */
public class InventoryDetailModel {

  /**
   * Material id
   */
  public Long mId;
  /**
   * Material name
   */
  public String mnm;
  /**
   * Entity id
   */
  public Long eId;
  /**
   * Entity Name
   */
  public String enm; //Entity Name
  /**
   * Material Tags
   */
  public List<String> mat_tgs;
  /**
   * Current Stock
   */
  public CurrentStock currentStock;
  /**
   * Min stock
   */
  public BigDecimal min = BigDecimal.ZERO; // re-order levels or MIN
  /**
   * Max Stock
   */
  public BigDecimal max = BigDecimal.ZERO;
  /**
   * Material description
   */
  public String description;
  /**
   * Material Image path
   */
  public List<String> imgPath;
  /**
   * Stock event
   */
  public int se;
  /**
   * Event duration
   */
  public long ed;
  /**
   * Allocated Stock
   */
  public BigDecimal as;
  /**
   * Intransit stock
   */
  public BigDecimal it;
  /**
   * Likely to stock out (or) Predicted Days Of Stock
   */
  public BigDecimal ls;
  /**
   * Stock Availability Days
   */
  public BigDecimal sad;
}

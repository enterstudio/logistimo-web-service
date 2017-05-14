package com.logistimo.api.models;

import java.math.BigDecimal;
import java.util.List;

public class MaterialModel {

  public int sno;
  /*
  * Material ID
  * */
  public Long mId;
  /*
  * Domain ID
  * */
  public Long dId;
  /*
  * Material Name
  * */
  public String mnm;
  /*
  * Short Name
  * */
  public String snm;
  /*
  * Custom ID
  * */
  public String cId;
  /*
 * Description
 * */
  public String dsc;
  /*
  * Additional Information
  * */
  public String info;
  /*
  * Display info on mobile or not
  * */
  public String dispInfo;
  /*
* Tags
* */
  public List<String> tgs;
  /*
  * Retailer's Price
  * */
  public String rp;
  /*
  * Manufacturer Specified Retail Price
  * */
  public String msrp;
  /*
  * Last Updated
  * */
  public String t;
  /*
  * Batch
  * */
  public String b;

  /*
  * Does material exist or not
  * */
  public String dty;
  /*
   * Is Binary
   */
  public String ib;
  /*
  * Seasonal
  * */
  public String snl;
  /*
  * Currency
  * */
  public String cur;
  /*
  * Enable temperature monitoring
  * */
  public String tm;
  /*
  * Minimum temperature
  * */
  public String tmin;
  /*
  * Maximum temperature
  * */
  public String tmax;

  public String creBy; //Created by
  public String creByn;

  public String ludBy; //last updated by;
  public String ludByn;

  public String creOn; //created On

  public boolean isAdded; // Is Entity already having this material
  public Long sdid; // source domain
  public String sdname; // source domain name

  //Inventory items added while adding materials to an entity
  public BigDecimal reord = BigDecimal.ZERO; // re-order levels or MIN
  public BigDecimal max = BigDecimal.ZERO;
  public BigDecimal minDur = BigDecimal.ZERO; // re-order levels or MIN
  public BigDecimal maxDur = BigDecimal.ZERO;
  public BigDecimal crMnl;
  public BigDecimal tx = BigDecimal.ZERO; //Tax
  public String im; //Inventory Model
  public float sl; //Service Level

  public Long huId; // Handling unit id
  public BigDecimal huQty; // Handling unit quantity
  public String huName; // Handling unit Name
}

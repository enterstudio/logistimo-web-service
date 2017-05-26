/*
 * Copyright © 2017 Logistimo.
 *
 * This file is part of Logistimo.
 *
 * Logistimo software is a mobile & web platform for supply chain management and remote temperature monitoring in
 * low-resource settings, made available under the terms of the GNU Affero General Public License (AGPL).
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General
 * Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any
 * later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Affero General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Affero General Public License along with this program.  If not, see
 * <http://www.gnu.org/licenses/>.
 *
 * You can be released from the requirements of the license by purchasing a commercial license. To know more about
 * the commercial license, please contact us at opensource@logistimo.com
 */

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

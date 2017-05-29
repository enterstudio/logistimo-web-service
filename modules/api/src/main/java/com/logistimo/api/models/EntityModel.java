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

public class EntityModel {

  public int sno;
  public Long id;
  public Long sdid; //sourceDomain
  public String sdname; //sourceDomain name
  /**
   * Custom Id
   */
  public String cid;
  public String lid;

  /**
   * Entity Name
   */
  public String nm;
  /**
   * City
   */
  public String ct;
  /**
   * State
   */
  public String st;
  /**
   * Country
   */
  public String ctr;
  /**
   * latitude
   */
  public double lt;
  /**
   * Longitude
   */
  public double ln;
  /**
   * Timestamp
   */
  public String ts;
  public List<String> tgs;
  /**
   * Taluk
   */
  public String tlk;
  public String rus;
  public String rusn;
  public List<UserModel> usrs;
  /**
   * Street Address
   */
  public String str;
  /**
   * District
   */
  public String ds;
  /**
   * Currency
   */
  public String cur;
  /**
   * Inventory Policy
   */
  public String inv;
  public String om;
  /**
   * Zip Code
   */
  public String zip;
  /**
   * Route tag
   */
  public String rt;
  /**
   * Route index
   */
  public int ri;
  public List<EntityGroupModel> pgs;
  /**
   * Service Level
   */
  public int sl;
  /**
   * Tax
   */
  public BigDecimal tx;
  public String txid;
  public String typ;

  public String loc; // Location
  public BigDecimal cl; // Credit Limit
  public String co; // Created On
  public String desc; // Description
  /**
   * isOptimizationOn
   */
  public boolean oo;

  public int osno; //ordered serial no
  //Display value for inv Model.
  public String invDsp;

  /**
   * Lat updated on
   */
  public String lts;

  public String lub; //Last updated by
  public String lubn;//last updated by full name

  public boolean be;
  public String add; //address
  public int perm; // 1 for view and 2 for manage

  public String iat; // Inventory activity time
  public String oat; // Order activity time
}

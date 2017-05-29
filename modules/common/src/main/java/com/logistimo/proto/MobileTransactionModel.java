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

package com.logistimo.proto;

import java.math.BigDecimal;

/**
 * Created by vani on 18/01/17.
 */
public class MobileTransactionModel {
  /**
   * Material id
   */
  public Long mid;
  /**
   * Material name - optional
   */
  public String mnm;
  /**
   * Type of transaction
   */
  public String ty;
  /**
   * Quantity
   */
  public BigDecimal q;
  /**
   * Timestamp
   */
  public String t;
  /**
   * Reason
   */
  public String rsn;
  /**
   * Material status
   */
  public String mst;
  /**
   * Opening stock
   */
  public BigDecimal ostk;
  /**
   * Closing stock
   */
  public BigDecimal cstk;
  /**
   * User ID
   */
  public String uid;
  /**
   * User name
   */
  public String u;
  /**
   * Linked kiosk id
   */
  public Long lkid;
  /**
   * Linked kiosk name
   */
  public String lknm;
  /**
   * Batch ID
   */
  public String bid;
  /**
   * Opening stock in batch
   */
  public BigDecimal ostkb;
  /**
   * Batch expiry - DD/MM/YYYY
   */
  public String bexp;
  /**
   * Batch manufacturer name
   */
  public String bmfnm;
  /**
   * Batch manufactured date - DD/MM/YYYY
   */
  public String bmfdt;
  /**
   * Closing stock in batch
   */
  public BigDecimal cstkb;
  /**
   * Actual date of transaction
   */
  public String atd;
  /**
   * Tracking object type
   */
  public String troty;
  /**
   * Tracking object id
   */
  public String trid;
}
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
 * Created by vani on 21/03/17.
 */
public class MobileTransModel {
  /**
   * Transaction entry time in milliseconds (time at which the transaction is entered on the mobile)
   */
  public Long entm;
  /**
   * Transaction type
   */
  public String ty;
  /**
   * Quantity
   */
  public BigDecimal q;
  /**
   * Opening stock
   */
  public BigDecimal ostk;
  /**
   * Batch ID
   */
  public String bid;
  /**
   * Batch expiry date in dd/mm/yyyy format
   */
  public String bexp;
  /**
   * Batch manufacturer name
   */
  public String bmfnm;
  /**
   * Batch manufactured date in dd/mm/yyyy format
   */
  public String bmfdt;
  /**
   * Reason
   */
  public String rsn;
  /**
   * Material status
   */
  public String mst;
  /**
   * Actual date of transaction in dd/mm/yyyy format
   */
  public String atd;
  /**
   * Linked Kiosk ID
   */
  public Long lkid;
  /**
   * Geo model
   */
  public MobileGeoModel geo;

  /**
   * For sorting the entry time
   */
  public Long sortEtm;
}

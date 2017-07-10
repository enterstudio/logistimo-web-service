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

/**
 * Created by yuvaraj on 24/04/17.
 */
public class MobileInvModel {

  /**
   * Material Name
   */
  public String mnm;
  /**
   * Current Stock (Stock in Hand)
   */
  public BigDecimal stk = BigDecimal.ZERO; // current stock
  /**
   * Min
   */
  public BigDecimal reord = BigDecimal.ZERO; // re-order levels or MIN
  /**
   * Max
   */
  public BigDecimal max = BigDecimal.ZERO;
  /**
   * Allocated stock
   */
  public BigDecimal astk = BigDecimal.ZERO;
  /**
   * In transit stock
   */
  public BigDecimal tstk = BigDecimal.ZERO;
  /**
   * get tags by type
   */
  public List<String> tgs;
  /**
   * Predicted days of stock
   */
  public BigDecimal pdos;
  /**
   * Stock Availability Period
   */
  public BigDecimal sap;
  /**
   * Material Description
   */
  public String mDes;
  /**
   * Last updated time of reorder-level or min
   */
  public String reordT;
}

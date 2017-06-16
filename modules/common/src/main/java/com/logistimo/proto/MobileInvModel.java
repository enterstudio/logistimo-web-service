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
import java.util.List;

/**
 * Created by vani on 24/03/17.
 */
public class MobileInvModel {
  /**
   * Material ID
   */
  public Long mid;
  /**
   * Short material ID
   */
  public Long smid;
  /**
   * Quantity
   */
  public BigDecimal q;
  /**
   * Allocated quantity
   */
  public BigDecimal alq;
  /**
   * In transit quantity
   */
  public BigDecimal itq;
  /**
   * Available quantity
   */
  public BigDecimal avq;
  /**
   * Min.
   */
  public BigDecimal min;
  /**
   * Max.
   */
  public BigDecimal max;
  /**
   * Duration of stock (stock availability period)
   */
  public BigDecimal dsq;
  /**
   * Duration of min
   */
  public BigDecimal dmin;
  /**
   * Duration of max
   */
  public BigDecimal dmax;
  /**
   * Consumption rate model
   */
  public MobileConsRateModel cr;
  /**
   * Non expired batches
   */
  public List<MobileInvBatchModel> bt;
  /**
   * Expired batches
   */
  public List<MobileInvBatchModel> xbt;
  /**
   * Last updated time of inventory
   */
  public String t;
}

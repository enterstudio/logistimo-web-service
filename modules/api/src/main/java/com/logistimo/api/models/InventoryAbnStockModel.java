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

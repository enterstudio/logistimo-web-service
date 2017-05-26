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

package com.logistimo.models.shipments;

import java.math.BigDecimal;
import java.util.List;

/**
 * Created by Mohan Raja on 29/09/16
 */
public class ShipmentItemModel {
  public Long mId;
  public String mnm;
  public BigDecimal q;
  public BigDecimal aq;
  public BigDecimal fq;
  /**
   * Allocate from order
   */
  public boolean afo;
  public boolean isBa;
  public List<ShipmentItemBatchModel> bq;

  public BigDecimal astk;
  public Long kid;
  public Long sdid;
  public String sid;
  public String uid;
  public BigDecimal vs;
  public BigDecimal vmin;
  public BigDecimal vmax;
  public BigDecimal vsavibper;
  public BigDecimal atpstk;
  public BigDecimal stk;
  public BigDecimal max;
  public BigDecimal min;
  public BigDecimal csavibper;
  public BigDecimal huQty;
  public String huName;
  /**
   * Fulfilled material status
   */
  public String fmst;
  /**
   * Fulfil discrepancy reason
   */
  public String frsn;
  /**
   * Shipped material status
   */
  public String smst;
  /**
   * Order Allocated Stock
   */
  public BigDecimal oastk;
  /**
   * Available to allocate quantity
   */
  public BigDecimal aaq;
  /**
   * Temperature sensitive material
   */
  public Boolean tm;

}

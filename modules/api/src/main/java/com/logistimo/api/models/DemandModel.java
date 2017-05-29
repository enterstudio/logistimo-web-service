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

/**
 *
 */
package com.logistimo.api.models;

import com.logistimo.models.shipments.ShipmentItemModel;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * @author charan
 */
public class DemandModel implements Comparable<DemandModel> {

  /**
   * Material Name
   */
  public String nm;
  /**
   * Material Id
   */
  public Long id;
  /**
   * Quantity
   */
  public BigDecimal q;
  /**
   * Price
   */
  public String p;
  /**
   * Tax
   */
  public BigDecimal t;
  /**
   * Discount
   */
  public BigDecimal d;
  /**
   * Formatted Amount
   */
  public String a;
  /**
   * isBinary values
   */
  public boolean isBn;
  /**
   * isBatch
   */
  public boolean isBa;
  /**
   * Original Quantity
   */
  public BigDecimal oq;

  /**
   * Batches
   */
  public Set<DemandItemBatchModel> bts;

  /**
   * vendor stock
   */
  public BigDecimal vs = BigDecimal.ZERO;

  /**
   * Current stock
   */
  public BigDecimal stk;

  /**
   * Max Stock
   */
  public BigDecimal max;

  /**
   * Min Stock
   */
  public BigDecimal min;

  /**
   * Current Stock event type - Customer
   */
  public int event = -1;

  /**
   * Current Stock event type - Vendor
   */
  public int vevent = -1;

  /**
   * Economic Order Quantity
   */
  public BigDecimal eoq;
  /**
   * Recommended Order Quantity
   */
  public BigDecimal rq;
  /**
   * Inventory Model
   */
  public String im;
  /**
   * Order Id
   */
  public Long oid;

  /**
   * Currency
   */
  public String c;
  /**
   * Item tax
   */
  public BigDecimal tx;

  /**
   * Time Stamp
   */
  public String ts;


  /**
   * Entity
   */
  public EntityModel e;
  /**
   * User Id
   */
  public String uid;

  /**
   * Status
   */
  public String stt;

  /**
   * Message
   */
  public String msg;

  /**
   * Serial number
   */
  public int sno;
  public Long sdid; // source domain id
  public String sdname; // source domain name
  public String rsn;
  public BigDecimal huQty;
  public String huName;
  /**
   * Allocated stock
   */
  public BigDecimal astk;
  /**
   * Allocated stock for Order
   */
  public BigDecimal oastk;
  /**
   * Available to promise stock
   */
  public BigDecimal atpstk;
  /**
   * In transit stock
   */
  public BigDecimal itstk;
  /**
   * Shipped quantity
   */
  public BigDecimal sq;
  /**
   * Available Vendor Stock Availability Period
   */
  public BigDecimal vsavibper;
  /**
   * Available Customer Stock Availability Period
   */
  public BigDecimal csavibper;
  /**
   * Order type
   */
  public Integer oty;
  /**
   * Order status
   */
  public String st;
  /**
   * Yet to allocate quantity
   */
  public BigDecimal yta;
  /**
   * Yet to ship quantity
   */
  public BigDecimal yts;
  /**
   * In shipment quantity
   */
  public BigDecimal isq;
  /**
   * Yet to Create shipment
   */
  public BigDecimal ytcs;
  /**
   * Fulfilled quantity
   */
  public BigDecimal fq;
  /**
   * Shipment id and quantity
   */
  public List<DemandBreakdownModel> allocations = new ArrayList<>();
  /**
   * Shipped discrepancy reason
   */
  public String sdrsn;
  /**
   * Material status
   */
  public String mst;
  /**
   * Vendor Max stock
   */
  public BigDecimal vmax;
  /**
   * Vendor Min stock
   */
  public BigDecimal vmin;
  /**
   * Temperature sensitive
   */
  public Boolean tm;
  /**
   * Used for displaying tooltip breakdown on Order detail page
   */
  public List<ShipmentItemModel> bd = new ArrayList<>();

  /* (non-Javadoc)
   * @see java.lang.Comparable#compareTo(java.lang.Object)
   */
  @Override
  public int compareTo(DemandModel o) {
    if (o == null) {
      return 1;
    }
    if (o.nm != null && this.nm == null) {
      return -1;
    }
    return nm.compareTo(o.nm);
  }
}

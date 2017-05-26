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
 * Created by vani on 15/10/16.
 */
public class DiscrepancyUIModel {
  public int sno; // Serial number
  public Long id; // Demand item ID
  public List<String> disc; // Discrepancies
  public boolean hasOd; // Has Ordering discrepancy
  public boolean hasSd; // Has Shipping discrepancy
  public boolean hasFd; // Has Fulfillment discrepancy
  public Long oid; // Order ID
  public String oct; // Order creation timestamp
  public String rid; // Order reference ID
  public Integer oty; // Order type
  public String otyStr; // Order type string
  public Long mid; // Material ID
  public String mnm; // Material name
  public BigDecimal roq; // Recommended ordered quantity
  public BigDecimal oq; // Ordered quantity
  public String odRsn; // Ordering discrepancy reason
  public String odRsnStr; // Ordering discrepancy reason display string (HTML)
  public BigDecimal od; // Ordering discrepancy (oq-roq)
  public BigDecimal sq; // Shipped quantity
  public String sdRsn; // Shipping discrepancy reason
  public String sdRsnStr; // Shipping discrepancy reason display string (HTML)
  public BigDecimal sd; // Shipping discrepancy (sq-oq)
  public BigDecimal fq; // Fulfilled quantity
  public List<String> fdRsns; // Fulfillment discrepancy reason
  public String fdRsnsStr; // Fulfillment discrepancy reasons used in display (HTML)
  public BigDecimal fd; // Fulfillment discrepancy (fq-sq)
  public String st; // Status code
  public String status; // Status string used in display
  public String stt; // Status updated timestamp
  public Long cid; // Customer Kiosk ID
  public String cnm; // Customer Kiosk name
  public String cadd; // Customer Kiosk address
  public Long vid; // Vendor Kiosk ID
  public String vnm; // Vendor Kiosk name
  public String vadd; // Vendor Kiosk address
  public Long sdid; // Source domain ID
  public String sdnm; // Source domain name
}

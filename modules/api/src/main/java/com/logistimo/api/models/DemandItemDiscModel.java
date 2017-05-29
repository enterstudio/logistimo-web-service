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
 * Created by vani on 04/10/16.
 */
public class DemandItemDiscModel {
  public int sno; // Serial number
  public Long diId; // Demand item ID
  public List<String> disc; // Discrepancies
  public Long oid; // Order ID
  public Integer oty; // Order type
  public String otyStr; // Order type string
  public Long mid; // Material ID
  public String mnm; // Material name
  public BigDecimal oq; // Ordered quantity
  public String odRsn; // Ordering discrepancy reason
  public String fdRsn; // Fulfillment discrepancy reason
  public BigDecimal roq; // Recommended ordered quantity
  public BigDecimal sq; // Shipped quantity
  public BigDecimal fq; // Fulfilled quantity
  public String st; // Status code
  public String status; // Status string used in display
  public String stt; // Status updated timestamp
  public Long cid; // Customer Kiosk ID
  public String cnm; // Customer Kiosk name
  public String cadd; // Customer Kiosk address
  public Long vid; // Vendor Kiosk ID
  public String vnm; // Vendor Kiosk name
  public String vadd; // Vendor Kiosk address
  public String cby; // Created by
  public String cbyun; // Created by full name
  public String uby; // Updated by
  public String con; // Created on
  public String uon; // Last updated on
  public Long sdid; // Source domain ID
  public String sdnm; // Source domain name
}

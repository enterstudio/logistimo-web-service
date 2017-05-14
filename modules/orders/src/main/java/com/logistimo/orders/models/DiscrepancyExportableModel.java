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

package com.logistimo.orders.models;

import com.logistimo.orders.entity.IDemandItem;

import com.logistimo.models.orders.DiscrepancyModel;
import com.logistimo.logger.XLog;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by vani on 07/10/16.
 */
public class DiscrepancyExportableModel {
  private static final XLog xLogger = XLog.getLog(DiscrepancyExportableModel.class);

  public List<String> discTypes; // Discrepancies
  public Long oId; // Order ID
  public String rId; // Order reference ID
  public Date oct; // Order creation timestamp
  public Integer oty; // Order type
  public Long mId; // Material ID
  public String cmId; // Custom material ID
  public String mnm; // Material name
  public BigDecimal roq = new BigDecimal(0); // Recommended ordered quantity
  public BigDecimal oq; // Ordered quantity
  public String odrsn; // Order discrepancy reason
  public BigDecimal sq = new BigDecimal(0); // Shipped quantity
  public String sdrsn; // Shipping discrepancy reason
  public BigDecimal fq = new BigDecimal(0); // Fulfilled quantity
  public List<String> fdrsns; // List of Shipment ID:Fulfillment discrepancy reasons
  public String st; // Status
  public Date stt; // Status updated timestamp
  public Long cId; // Customer kiosk ID
  public String ccId; // Custom customer kiosk ID
  public String cnm; // Customer kiosk name
  public Long vId; // Vendor Kiosk ID
  public String cvId; // Custom vendor ID
  public String vnm; // Vendor kiosk name
  public boolean hasOd; // Has ordering discrepancy
  public boolean hasSd; // Has shipping discrepancy
  public boolean hasFd; // Has fulfillment discrepancy

  public DiscrepancyExportableModel(DiscrepancyModel dm) {
    discTypes = dm.discTypes;
    oId = dm.oId;
    rId = dm.rId;
    oct = dm.oct;
    oty = dm.oty;
    mId = dm.mId;
    cmId = dm.cmId;
    mnm = dm.mnm;
    roq = dm.roq;
    oq = dm.oq;
    odrsn = dm.odrsn;
    sq = dm.sq;
    sdrsn = dm.sdrsn;
    fq = dm.fq;
    if (dm.fdrsns != null && !dm.fdrsns.isEmpty()) {
      fdrsns = new ArrayList<>(dm.fdrsns.size());
      for (String fdrsn : dm.fdrsns) {
        fdrsns.add(fdrsn.replace("||", ":"));
      }
    }
    st = dm.st;
    stt = dm.stt;
    cId = dm.cId;
    ccId = dm.ccId;
    cnm = dm.cnm;
    vId = dm.vId;
    cvId = dm.cvId;
    vnm = dm.vnm;
    if (discTypes != null && !discTypes.isEmpty()) {
      if (discTypes.contains(IDemandItem.ORDERING_DISCREPANCY)) {
        hasOd = true;
      }
      if (discTypes.contains(IDemandItem.SHIPPING_DISCREPANCY)) {
        hasSd = true;
      }
      if (discTypes.contains(IDemandItem.FULFILLMENT_DISCREPANCY)) {
        hasFd = true;
      }
    }
  }


}

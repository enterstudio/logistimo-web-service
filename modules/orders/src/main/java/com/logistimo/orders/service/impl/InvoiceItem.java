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

package com.logistimo.orders.service.impl;

import com.logistimo.constants.Constants;
import org.apache.commons.lang.StringUtils;

/**
 * Created by nitisha.khandelwal on 01/08/17.
 */

public class InvoiceItem {

  String sno;

  String item;

  String remarks = Constants.EMPTY;

  String recommended = Constants.EMPTY;

  String quantity;

  String batchId;
  String manufacturer;
  String expiry;
  String batchQuantity;


  Boolean batchEnabled;

  String materialStatus;

  public String getSno() {
    return sno;
  }

  public void setSno(String sno) {
    this.sno = sno;
  }

  public String getItem() {
    if (!batchEnabled && StringUtils.isNotEmpty(materialStatus)) {
      return item + "(" + materialStatus + ")";
    } else {
      return item;
    }
  }

  public void setItem(String item) {
    this.item = item;
  }

  public String getRemarks() {
    return remarks;
  }

  public void setRemarks(String remarks) {
    this.remarks = remarks;
  }

  public String getRecommended() {
    return recommended;
  }

  public void setRecommended(String recommended) {
    this.recommended = recommended;
  }

  public String getQuantity() {
    return quantity;
  }

  public void setQuantity(String quantity) {
    this.quantity = quantity;
  }

  public String getBatchId() {
    return batchId;
  }

  public InvoiceItem setBatchId(String batchId) {
    this.batchId = batchId;
    return this;
  }

  public String getManufacturer() {
    return manufacturer;
  }

  public InvoiceItem setManufacturer(String manufacturer) {
    this.manufacturer = manufacturer;
    return this;
  }

  public String getExpiry() {
    return expiry;
  }

  public InvoiceItem setExpiry(String expiry) {
    this.expiry = expiry;
    return this;
  }

  public String getBatchQuantity() {
    return batchQuantity;
  }

  public InvoiceItem setBatchQuantity(String batchQuantity) {
    this.batchQuantity = batchQuantity;
    return this;
  }

  public String getMaterialStatus() {
    return materialStatus;
  }

  public void setMaterialStatus(String materialStatus) {
    this.materialStatus = materialStatus;
  }

  public Boolean getBatchEnabled() {
    return batchEnabled;
  }

  public void setBatchEnabled(Boolean batchEnabled) {
    this.batchEnabled = batchEnabled;
  }

  public String getBatch() {
    if (batchId != null) {
      if (StringUtils.isNotEmpty(materialStatus)) {
        return batchId + ", " + manufacturer + ", " + expiry + "(" + materialStatus + ")";
      } else {
        return batchId + ", " + manufacturer + ", " + expiry;
      }
    }
    return null;
  }
}

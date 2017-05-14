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
package com.logistimo.accounting.entity;

import java.math.BigDecimal;
import java.util.Date;

import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

/**
 * Financial account of payables and paid amounts (typically in the context of orders)
 *
 * @author Arun
 */
@PersistenceCapable(identityType = IdentityType.APPLICATION, detachable = "true")
public class Account implements IAccount {

  @PrimaryKey
  @Persistent
  private String key;  // vendorId.customerId.year
  @Persistent
  private Long dId; // domain Id
  @Persistent
  private Long vId; // vendor kiosk Id
  @Persistent
  private String vNm; // vendor name for2 ordering
  @Persistent
  private Long cId; // customer kiosk Id
  @Persistent
  private String cNm; // customer name for ordering
  @Persistent
  private Integer y; // year (could also be a reference to a financial year)
  @Persistent
  private BigDecimal
      py =
      BigDecimal.ZERO;
  // payable (amount to be paid, by customer to vendor); its inverse becomes the receivable
  @Persistent
  private Date t; // timestamp of creation

  public static String createKey(Long vId, Long cId, int year) {
    return vId + "." + cId + "." + year;
  }


  @Override
  public String getKey() {
    return key;
  }

  @Override
  public void setKey(String key) {
    this.key = key;
  }

  @Override
  public Long getDomainId() {
    return dId;
  }

  @Override
  public void setDomainId(Long dId) {
    this.dId = dId;
  }

  @Override
  public Long getVendorId() {
    return vId;
  }

  @Override
  public void setVendorId(Long vId) {
    this.vId = vId;
  }

  @Override
  public String getVendorName() {
    return vNm;
  }

  @Override
  public void setVendorName(String vName) {
    vNm = vName;
  }

  @Override
  public Long getCustomerId() {
    return cId;
  }

  @Override
  public void setCustomerId(Long cId) {
    this.cId = cId;
  }

  @Override
  public String getCustomerName() {
    return cNm;
  }

  @Override
  public void setCustomerName(String cName) {
    cNm = cName;
  }

  @Override
  public int getYear() {
    if (y != null) {
      return y.intValue();
    }
    return 0;
  }

  @Override
  public void setYear(int year) {
    this.y = new Integer(year);
  }

  @Override
  public BigDecimal getPayable() {
    if (py != null) {
      return py;
    }
    return BigDecimal.valueOf(0);
  }

  @Override
  public void setPayable(BigDecimal payable) {
    this.py = payable;
  }

  @Override
  public Date getTimestamp() {
    return t;
  }

  @Override
  public void setTimestamp(Date t) {
    this.t = t;
  }

}

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

package com.logistimo.orders.entity;

import com.logistimo.inventory.entity.ITransaction;
import com.logistimo.utils.BigUtil;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.jdo.annotations.Element;
import javax.jdo.annotations.Extension;
import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.Join;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;


@PersistenceCapable(identityType = IdentityType.APPLICATION, detachable = "true")
public class DemandItemBatch implements IDemandItemBatch {

  @PrimaryKey
  @Persistent(valueStrategy = IdGeneratorStrategy.IDENTITY)
  private Long id; // id
  @Element(column = "dItmId")
  private DemandItem dmdItm;
  @Persistent(table = "DEMANDITEMBATCH_DOMAINS", defaultFetchGroup = "true")
  @Join
  @Element(column = "DOMAIN_ID")
  private List<Long> dId;
  @Persistent
  private Long sdId; // source domain Id
  @Persistent
  private Long kId; // kiosk Id
  @Persistent
  private Long mId; // material Id
  @Persistent
  @Extension(vendorName = "datanucleus", key = "gae.unindexed", value = "true")
  private BigDecimal q; // quantity
  @Persistent
  private String bid; // batch id
  @Persistent
  @Extension(vendorName = "datanucleus", key = "gae.unindexed", value = "true")
  private Date bexp; // batch expiry
  @Persistent
  @Extension(vendorName = "datanucleus", key = "gae.unindexed", value = "true")
  private String bmfnm; // manufacturer name
  @Persistent
  @Extension(vendorName = "datanucleus", key = "gae.unindexed", value = "true")
  private Date bmfdt; // manufactured-on date

  public DemandItemBatch() {
  }

  @Override
  public IDemandItemBatch init(ITransaction trans) {
    dId = trans.getDomainIds();
    sdId = trans.getDomainId();
    kId = trans.getKioskId();
    mId = trans.getMaterialId();
    q = trans.getQuantity();
    bid = trans.getBatchId();
    bexp = trans.getBatchExpiry();
    bmfnm = trans.getBatchManufacturer();
    bmfdt = trans.getBatchManufacturedDate();
    return this;
  }

  public Long getDomainId() {
    return sdId;
  }

  public void setDomainId(Long domainId) {
    sdId = domainId;
  }

  public List<Long> getDomainIds() {
    return dId;
  }

  public void setDomainIds(List<Long> domainIds) {
    this.dId.clear();
    this.dId.addAll(domainIds);
  }

  public void addDomainIds(List<Long> domainIds) {
    if (domainIds == null || domainIds.isEmpty()) {
      return;
    }
    if (this.dId == null) {
      this.dId = new ArrayList<>();
    }
    for (Long dId : domainIds) {
      if (!this.dId.contains(dId)) {
        this.dId.add(dId);
      }
    }
  }

  public void removeDomainId(Long domainId) {
    if (this.dId != null && !this.dId.isEmpty()) {
      this.dId.remove(domainId);
    }
  }

  public void removeDomainIds(List<Long> domainIds) {
    if (this.dId != null && !this.dId.isEmpty()) {
      this.dId.removeAll(domainIds);
    }
  }

  @Override
  public Long getKioskId() {
    return kId;
  }

  @Override
  public void setKioskId(Long kId) {
    this.kId = kId;
  }

  @Override
  public Long getMaterialId() {
    return mId;
  }

  @Override
  public void setMaterialId(Long mId) {
    this.mId = mId;
  }

  @Override
  public BigDecimal getQuantity() {
    return BigUtil.getZeroIfNull(q);
  }

  @Override
  public void setQuantity(BigDecimal quantity) {
    q = quantity;
  }

  @Override
  public String getBatchId() {
    return bid;
  }

  @Override
  public void setBatchId(String bid) {
    this.bid = bid;
  }

  @Override
  public Date getBatchExpiry() {
    return bexp;
  }

  @Override
  public void setBatchExpiry(Date batchExpiry) {
    bexp = batchExpiry;
  }

  @Override
  public String getBatchManufacturer() {
    return bmfnm;
  }

  @Override
  public void setBatchManufacturer(String manufacturerName) {
    bmfnm = manufacturerName;
  }

  @Override
  public Date getBatchManufacturedDate() {
    return bmfdt;
  }

  @Override
  public void setBatchManufacturedDate(Date manufacturedDate) {
    bmfdt = manufacturedDate;
  }

  @Override
  public Map<String, Object> toMap() {
    Map<String, Object> map = new HashMap<String, Object>();
    map.put(BATCH_ID, bid);
    map.put(QUANTITY, BigUtil.getFormattedValue(q));
    map.put(EXPIRY, bexp);
    if (bmfnm != null) {
      map.put(MANUFACTURER, bmfnm);
    }
    if (bmfdt != null) {
      map.put(MANUFACTURED_ON, bmfdt);
    }
    return map;
  }
}

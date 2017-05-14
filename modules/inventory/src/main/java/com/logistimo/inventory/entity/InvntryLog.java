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
package com.logistimo.inventory.entity;

import com.logistimo.utils.BigUtil;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.jdo.annotations.Element;
import javax.jdo.annotations.Extension;
import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.Join;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

/**
 * @author arun
 *         NOTE: Attribute names are kept small to optimize on storage in data store
 */
@PersistenceCapable(identityType = IdentityType.APPLICATION, detachable = "true", cacheable = "false")
public class InvntryLog implements IInvntryLog {
  @Persistent
  String bid; // batch Id, if any
  @Persistent
  @Extension(vendorName = "datanucleus", key = "gae.unindexed", value = "true")
  BigDecimal bs; // stock in batch, if any
  @PrimaryKey
  @Persistent(valueStrategy = IdGeneratorStrategy.IDENTITY)
  private Long key;
  @Persistent(table = "INVNTRYLOG_DOMAINS", defaultFetchGroup = "true")
  @Join
  @Element(column = "DOMAIN_ID")
  private List<Long> dId;
  @Persistent
  private Long sdId; // source domain ID
  @Persistent
  private Long kId;
  @Persistent
  private Long mId;
  @Persistent
  @Extension(vendorName = "datanucleus", key = "gae.unindexed", value = "true")
  private BigDecimal s = BigDecimal.ZERO; // stock
  @Persistent
  private Date t; // creation timestamp
  @Persistent
  private Date arcAt;
  @Persistent
  private String arcBy;


  public Long getKey() {
    return key;
  }

  public void setKey(Long key) {
    this.key = key;
  }


  @Override
  public Long getKioskId() {
    return kId;
  }

  @Override
  public void setKioskId(Long kioskId) {
    this.kId = kioskId;
  }

  @Override
  public Long getMaterialId() {
    return mId;
  }

  @Override
  public void setMaterialId(Long materialId) {
    this.mId = materialId;
  }

  @Override
  public BigDecimal getStock() {
    return BigUtil.getZeroIfNull(s);
  }

  @Override
  public void setStock(BigDecimal stockLevel) {
    this.s = stockLevel;
  }

  @Override
  public Date getCreatedOn() {
    return t;
  }

  @Override
  public void setCreatedOn(Date createdOn) {
    this.t = createdOn;
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
  public String getBatchId() {
    return bid;
  }

  @Override
  public BigDecimal getBatchStock() {
    return BigUtil.getZeroIfNull(bs);
  }

  @Override
  public void setBatchData(String batchId, BigDecimal stockInBatch) {
    bid = batchId;
    bs = stockInBatch;
  }

  public Date getArchivedAt() {
    return arcAt;
  }

  public void setArchivedAt(Date archivedAt) {
    arcAt = archivedAt;
  }

  public String getArchivedBy() {
    return arcBy;
  }

  public void setArchivedBy(String archivedBy) {
    arcBy = archivedBy;
  }
}

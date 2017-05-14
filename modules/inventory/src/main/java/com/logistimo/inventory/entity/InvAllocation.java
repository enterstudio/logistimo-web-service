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

package com.logistimo.inventory.entity;

import com.logistimo.logger.XLog;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import javax.jdo.annotations.Element;
import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.Join;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

/**
 * Created by Mohan Raja on 29/09/16
 */
@PersistenceCapable(identityType = IdentityType.APPLICATION, detachable = "true")
public class InvAllocation implements IInvAllocation {
  static XLog xLogger = XLog.getLog(InvAllocation.class);

  @PrimaryKey
  @Persistent(valueStrategy = IdGeneratorStrategy.IDENTITY)
  private Long id;
  @Persistent
  private Long mId;
  @Persistent
  private Long kId;
  @Persistent
  private String bId;
  @Persistent
  private BigDecimal q = BigDecimal.ZERO;
  @Persistent
  private String type;
  @Persistent
  private String typeId;
  @Persistent
  private Date cOn;
  @Persistent
  private String cBy;
  @Persistent
  private Date uOn;
  @Persistent
  private String uBy;
  @Persistent
  private String mst;

  @Persistent(table = "INV_ALLOC_TAG", defaultFetchGroup = "true")
  @Join
  @Element(column = "TAG")
  private List<String> tags;

  @Override
  public Long getAllocationId() {
    return id;
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
  public Long getKioskId() {
    return kId;
  }

  @Override
  public void setKioskId(Long kioskId) {
    this.kId = kioskId;
  }

  @Override
  public String getBatchId() {
    return bId;
  }

  @Override
  public void setBatchId(String bId) {
    this.bId = bId;
  }

  @Override
  public BigDecimal getQuantity() {
    if (q != null) {
      return q;
    }
    return BigDecimal.ZERO;
  }

  @Override
  public void setQuantity(BigDecimal q) {
    this.q = q;
  }

  @Override
  public String getType() {
    return type;
  }

  @Override
  public void setType(String type) {
    this.type = type;
  }

  @Override
  public String getTypeId() {
    return typeId;
  }

  @Override
  public void setTypeId(String typeId) {
    this.typeId = typeId;
  }

  @Override
  public List<String> getTags() {
    return this.tags;
  }

  @Override
  public void setTags(List<String> tags) {
    if (this.tags != null) {
      this.tags.clear();
      this.tags.addAll(tags);
    } else {
      this.tags = tags;
    }
  }

  @Override
  public String getUpdatedBy() {
    return uBy;
  }

  @Override
  public void setUpdatedBy(String uBy) {
    this.uBy = uBy;
  }

  @Override
  public Date getUpdatedOn() {
    return uOn;
  }

  @Override
  public void setUpdatedOn(Date uOn) {
    this.uOn = uOn;
  }

  @Override
  public String getCreatedBy() {
    return cBy;
  }

  @Override
  public void setCreatedBy(String cBy) {
    this.cBy = cBy;
  }

  @Override
  public Date getCreatedOn() {
    return cOn;
  }

  @Override
  public void setCreatedOn(Date cOn) {
    this.cOn = cOn;
  }

  @Override
  public String getMaterialStatus() {
    return mst;
  }

  @Override
  public void setMaterialStatus(String mst) {
    this.mst = mst;
  }
}

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

package com.logistimo.materials.entity;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import javax.jdo.annotations.Element;
import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.Join;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

/**
 * @author Mohan Raja
 */
@PersistenceCapable(identityType = IdentityType.APPLICATION, detachable = "true")
public class HandlingUnitContent implements IHandlingUnitContent {
  @PrimaryKey
  @Persistent(valueStrategy = IdGeneratorStrategy.IDENTITY)
  private Long id; // id

  @Element(column = "huId")
  private HandlingUnit hu;

  @Persistent(table = "HUC_DOMAINS", defaultFetchGroup = "true")
  @Join
  @Element(column = "DOMAIN_ID")
  private List<Long> dId;

  @Persistent
  private Long sdId; // source domain Id

  @Persistent
  private Long cntId; // id of material / handling unit

  @Persistent
  private Integer ty; // 0-Material / 1-Handling unit

  @Persistent
  private BigDecimal quantity;

  @Override
  public Long getId() {
    return id;
  }

  @Override
  public void setId(Long id) {
    this.id = id;
  }

  @Override
  public Long getCntId() {
    return cntId;
  }

  @Override
  public void setCntId(Long cntId) {
    this.cntId = cntId;
  }

  @Override
  public Integer getTy() {
    return ty;
  }

  @Override
  public void setTy(Integer ty) {
    this.ty = ty;
  }

  @Override
  public BigDecimal getQuantity() {
    return quantity;
  }

  @Override
  public void setQuantity(BigDecimal quantity) {
    this.quantity = quantity;
  }

  @Override
  public Long getDomainId() {
    return sdId;
  }

  @Override
  public void setDomainId(Long sdId) {
    this.sdId = sdId;
  }

  @Override
  public List<Long> getDomainIds() {
    return this.dId;
  }

  @Override
  public void setDomainIds(List<Long> domainIds) {
    if (this.dId == null) {
      this.dId = new ArrayList<>();
    } else {
      this.dId.clear();
    }
    this.dId.addAll(domainIds);
  }

  @Override
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

  @Override
  public void removeDomainId(Long domainId) {
    if (this.dId != null && !this.dId.isEmpty()) {
      this.dId.remove(domainId);
    }
  }

  @Override
  public void removeDomainIds(List<Long> domainIds) {
    if (this.dId != null && !this.dId.isEmpty()) {
      this.dId.removeAll(domainIds);
    }
  }
}

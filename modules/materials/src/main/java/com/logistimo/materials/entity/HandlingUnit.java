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
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import javax.jdo.annotations.Column;
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
public class HandlingUnit implements IHandlingUnit {

  @PrimaryKey
  @Persistent(valueStrategy = IdGeneratorStrategy.IDENTITY)
  private Long id;

  @Persistent(table = "HU_DOMAINS", defaultFetchGroup = "true")
  @Join
  @Element(column = "DOMAIN_ID")
  private List<Long> dId;
  @Persistent
  private Long sdId; // source domain ID

  @Persistent
  private String name;

  @Persistent
  private String nName;

  @Persistent(defaultFetchGroup = "true", mappedBy = "hu", dependentElement = "true")
  private Set<HandlingUnitContent> contents;

  @Persistent
  @Column(length = 400)
  private String description;

  @Persistent
  private BigDecimal quantity;

  @Persistent
  private BigDecimal volume;

  @Persistent
  private BigDecimal weight;
  @Persistent
  private Date timeStamp; // creation date
  @Persistent
  private Date lastUpdated;
  @Persistent
  private String cb;
  @Persistent
  private String ub;

  @Override
  public Long getId() {
    return id;
  }

  @Override
  public void setId(Long id) {
    this.id = id;
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

  @Override
  public String getName() {
    return name;
  }

  @Override
  public void setName(String name) {
    this.name = name;
    if (name != null) {
      nName = name.toLowerCase();
    }
  }

  @Override
  public String getNormalisedName() {
    return nName;
  }

  @Override
  public Set<? extends IHandlingUnitContent> getContents() {
    return contents;
  }

  @Override
  public void setContents(Set<? extends IHandlingUnitContent> contents) {
    if (this.contents != null) {
      this.contents.clear();
      if (contents != null && !contents.isEmpty()) {
        this.contents.addAll((Collection<HandlingUnitContent>) contents);
      }
    } else {
      this.contents = (Set<HandlingUnitContent>) contents;
    }
  }

  @Override
  public String getDescription() {
    return description;
  }

  @Override
  public void setDescription(String description) {
    this.description = description;
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
  public BigDecimal getVolume() {
    return volume;
  }

  @Override
  public void setVolume(BigDecimal volume) {
    this.volume = volume;
  }

  @Override
  public BigDecimal getWeight() {
    return weight;
  }

  @Override
  public void setWeight(BigDecimal weight) {
    this.weight = weight;
  }

  @Override
  public Date getTimeStamp() {
    return timeStamp;
  }

  @Override
  public void setTimeStamp(Date timeStamp) {
    this.timeStamp = timeStamp;
  }

  @Override
  public Date getLastUpdated() {
    return lastUpdated;
  }

  @Override
  public void setLastUpdated(Date lastUpdated) {
    this.lastUpdated = lastUpdated;
  }

  @Override
  public String getCreatedBy() {
    return cb;
  }

  @Override
  public void setCreatedBy(String cb) {
    this.cb = cb;
  }

  @Override
  public String getUpdatedBy() {
    return ub;
  }

  @Override
  public void setUpdatedBy(String ub) {
    this.ub = ub;
  }

}

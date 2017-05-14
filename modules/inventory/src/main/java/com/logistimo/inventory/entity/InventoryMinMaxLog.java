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

import java.math.BigDecimal;
import java.util.Date;

import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

/**
 * @author Mohan Raja
 */

@PersistenceCapable(identityType = IdentityType.APPLICATION, detachable = "true")
public class InventoryMinMaxLog implements IInventoryMinMaxLog {

  @PrimaryKey
  @Persistent(valueStrategy = IdGeneratorStrategy.IDENTITY)
  private Long id;

  @Persistent
  private Long invId; // Invntry Id

  @Persistent
  private Long mId; // material Id

  @Persistent
  private Long kId; // kiosk Id

  @Persistent
  private BigDecimal min;

  @Persistent
  private BigDecimal max;

  @Persistent
  private BigDecimal minDur = BigDecimal.ZERO; // Minimum duration of stock

  @Persistent
  private BigDecimal maxDur = BigDecimal.ZERO; // Maximum duration of stock

  @Persistent
  private Integer type;

  @Persistent
  private BigDecimal cr;

  @Persistent
  private Integer src;

  @Persistent
  private String user;

  @Persistent
  private Date t;

  @Persistent
  private Integer freq; // Min max log frequency

  @Override
  public Long getId() {
    return id;
  }

  @Override
  public void setId(Long id) {
    this.id = id;
  }

  @Override
  public Long getInventoryId() {
    return invId;
  }

  @Override
  public void setInventoryId(Long invId) {
    this.invId = invId;
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
  public Long getKioskId() {
    return kId;
  }

  @Override
  public void setKioskId(Long kId) {
    this.kId = kId;
  }

  @Override
  public BigDecimal getMin() {
    return min;
  }

  @Override
  public void setMin(BigDecimal min) {
    this.min = min;
  }

  @Override
  public BigDecimal getMinDuration() {
    return minDur;
  }

  @Override
  public void setMinDuration(BigDecimal minDur) {
    this.minDur = minDur != null ? minDur : BigDecimal.ZERO;
  }

  @Override
  public BigDecimal getMaxDuration() {
    return maxDur;
  }

  @Override
  public void setMaxDuration(BigDecimal maxDur) {
    this.maxDur = maxDur != null ? maxDur : BigDecimal.ZERO;
  }

  @Override
  public BigDecimal getMax() {
    return max;
  }

  @Override
  public void setMax(BigDecimal max) {
    this.max = max;
  }

  @Override
  public Integer getType() {
    return type;
  }

  @Override
  public void setType(Integer type) {
    this.type = type;
  }

  @Override
  public BigDecimal getConsumptionRate() {
    return cr;
  }

  @Override
  public void setConsumptionRate(BigDecimal cr) {
    this.cr = cr;
  }

  @Override
  public Integer getSource() {
    return src;
  }

  @Override
  public void setSource(Integer src) {
    this.src = src;
  }

  @Override
  public String getUser() {
    return user;
  }

  @Override
  public void setUser(String user) {
    this.user = user;
  }

  @Override
  public Date getCreatedTime() {
    return t;
  }

  @Override
  public void setCreatedTime(Date t) {
    this.t = t;
  }

  @Override
  public Integer getMinMaxFrequency() {
    return freq;
  }

  @Override
  public void setMinMaxFrequency(Frequency freq) {
    this.freq = freq.ordinal();
  }
}

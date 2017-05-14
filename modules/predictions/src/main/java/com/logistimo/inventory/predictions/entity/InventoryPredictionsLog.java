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

package com.logistimo.inventory.predictions.entity;

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
public class InventoryPredictionsLog implements IInventoryPredictionsLog {

  @PrimaryKey
  @Persistent(valueStrategy = IdGeneratorStrategy.IDENTITY)
  private Long key;

  @Persistent
  private Long iKey; // Invntry Id

  @Persistent
  private Long mId; // material Id

  @Persistent
  private Long kId; // kiosk Id

  @Persistent
  private Date t; // created timestamp

  @Persistent
  private BigDecimal pdos; // Predicted days of stock including order status

  @Persistent
  private BigDecimal cr = BigDecimal.ZERO; // daily consumption rate over max. historical period

  @Persistent
  private BigDecimal op = BigDecimal.ZERO; // days; order periodicity

  @Override
  public Long getKey() {
    return key;
  }

  @Override
  public void setKey(Long key) {
    this.key = key;
  }

  @Override
  public Long getInventoryKey() {
    return iKey;
  }

  @Override
  public void setInventoryKey(Long iKey) {
    this.iKey = iKey;
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
  public Date getCreatedTime() {
    return t;
  }

  @Override
  public void setCreatedTime(Date t) {
    this.t = t;
  }

  @Override
  public BigDecimal getPredictedDaysOfStock() {
    return pdos;
  }

  @Override
  public void setPredictedDaysOfStock(BigDecimal pdos) {
    this.pdos = pdos;
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
  public BigDecimal getOrderPeriodicity() {
    return op;
  }

  @Override
  public void setOrderPeriodicity(BigDecimal op) {
    this.op = op;
  }
}

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
package com.logistimo.inventory.optimization.entity;

import java.util.Date;

import javax.jdo.annotations.Column;
import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;


/**
 * @author arun
 *
 *         Represents an item in the order
 */
@PersistenceCapable(identityType = IdentityType.APPLICATION, detachable = "true")
public class OptimizerLog implements IOptimizerLog {

  @PrimaryKey
  @Persistent(valueStrategy = IdGeneratorStrategy.IDENTITY)
  private Long key;
  @Persistent
  private Long dId; // Domain ID
  @Persistent
  private Date s = null; // start time
  @Persistent
  private Date e = null; // end time
  @Persistent
  private String ty = TYPE_UNKNOWN;
  @Persistent
  private int n = 0; // number of inventory items processed
  @Persistent
  @Column(length = 2048)
  private String msg = null;
  @Persistent
  private String inIds; // inventories IDs processed (kid:mid,...)

  @Override
  public Long getKey() {
    return key;
  }

  @Override
  public void setKey(Long key) {
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
  public Date getRunStart() {
    return s;
  }

  @Override
  public void setRunStart(Date runStart) {
    this.s = runStart;
  }

  @Override
  public Date getRunEnd() {
    return e;
  }

  @Override
  public void setRunEnd(Date runEnd) {
    this.e = runEnd;
  }

  @Override
  public String getComputationType() {
    return ty;
  }

  @Override
  public void setComputationType(String type) {
    this.ty = type;
  }

  @Override
  public int numItemsProcessed() {
    return n;
  }

  @Override
  public void setNumItemsProcessed(int n) {
    this.n = n;
  }

  @Override
  public String getInventoryIds() {
    return inIds;
  }

  @Override
  public void setInventoryIds(String inIds) {
    this.inIds = inIds;
  }

  @Override
  public String getMessage() {

    return msg;
  }

  @Override
  public void setMessage(String message) {
    this.msg = message;
  }
}
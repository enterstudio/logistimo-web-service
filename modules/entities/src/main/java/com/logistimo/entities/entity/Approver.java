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

package com.logistimo.entities.entity;

import java.util.Date;
import java.util.List;

import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.NotPersistent;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

/**
 * Created by naveensnair on 19/05/17.
 */
@PersistenceCapable(identityType = IdentityType.APPLICATION, detachable = "true", table = "APPROVERS")
public class Approver implements IApprover {

  @NotPersistent
  List<String> pa;
  @NotPersistent
  List<String> sa;
  @PrimaryKey
  @Persistent(valueStrategy = IdGeneratorStrategy.IDENTITY)
  private Long id;
  @Persistent
  private Long kid;
  @Persistent
  private String uid;
  @Persistent
  private Integer type;
  @Persistent
  private String otype;
  @Persistent
  private Date con;
  @Persistent
  private String cby;
  @Persistent
  private String uby;
  @Persistent
  private Long sdid;

  public Long getId() {
    return id;
  }

  public Long getKioskId() {
    return kid;
  }

  public void setKioskId(Long kid) {
    this.kid = kid;
  }

  public String getUserId() {
    return uid;
  }

  public void setUserId(String uid) {
    this.uid = uid;
  }

  public Integer getType() {
    return type;
  }

  public void setType(Integer type) {
    this.type = type;
  }

  public String getOrderType() {
    return otype;
  }

  public void setOrderType(String orderType) {
    this.otype = orderType;
  }

  public Date getCreatedOn() {
    return con;
  }

  public void setCreatedOn(Date con) {
    this.con = con;
  }

  public String getCreatedBy() {
    return cby;
  }

  public void setCreatedBy(String cby) {
    this.cby = cby;
  }

  public String getUpdatedBy() {
    return uby;
  }

  public void setUpdatedBy(String uby) {
    this.uby = uby;
  }
  public List<String> getPrimaryApprovers() {
    return pa;
  }

  public void setPrimaryApprovers(List<String> pa) {
    this.pa = pa;
  }

  public List<String> getSecondaryApprovers() {
    return sa;
  }

  public void setSecondaryApprovers(List<String> sa) {
    this.sa = sa;
  }

  public Long getSourceDomainId() {
    return sdid;
  }

  public void setSourceDomainId(Long sdid) {
    this.sdid = sdid;
  }

}

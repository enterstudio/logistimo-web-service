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
package com.logistimo.domains.entity;

import com.logistimo.domains.entity.IDomain;

import java.util.Date;

import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

/**
 * @author arun
 *
 *         Represents a domain
 */
@PersistenceCapable(identityType = IdentityType.APPLICATION, detachable = "true")
public class Domain implements IDomain { // implements Serializable {

  /**
   * Generated serial version ID
   */
  //private static final long serialVersionUID = 3948331422626828507L;

  @PrimaryKey
  @Persistent(valueStrategy = IdGeneratorStrategy.IDENTITY)
  private Long dId; // Domain ID
  @Persistent
  private String name;
  @Persistent
  private String nNm; // normalized name for sorting
  @Persistent
  private String description;
  @Persistent
  private String ownerId; // userId of owner of domain
  @Persistent
  private Date createdOn;
  @Persistent
  private boolean isActive = true;
  @Persistent
  private Boolean rptEnabled = true;
  @Persistent
  private Boolean hasChild = false; // Identifier to check whether domain has child
  @Persistent
  private Boolean hasParent = false; // Identifier to check whether domain has parent
  @Persistent
  private String ub;
  @Persistent
  private Date uo;

  public Domain() {

  }

  public Domain(Long domainId, String name, String description) {
    this.dId = domainId;
    this.name = name;
    this.description = description;
  }

  @Override
  public Long getId() {
    return dId;
  }

  @Override
  public void setId(Long dId) {
    this.dId = dId;
  }

  @Override
  public String getName() {
    return name;
  }

  @Override
  public void setName(String name) {
    this.name = name;
    if (name != null) {
      nNm = name.toLowerCase();
    } else {
      nNm = null;
    }
  }

  @Override
  public String getNormalizedName() {
    return nNm;
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
  public String getOwnerId() {
    return ownerId;
  }

  @Override
  public void setOwnerId(String ownerId) {
    this.ownerId = ownerId;
  }

  @Override
  public Date getCreatedOn() {
    return createdOn;
  }

  @Override
  public void setCreatedOn(Date createdOn) {
    this.createdOn = createdOn;
  }

  @Override
  public boolean isActive() {
    return isActive;
  }

  @Override
  public void setIsActive(boolean isActive) {
    this.isActive = isActive;
  }

  @Override
  public boolean isReportEnabled() {
    return rptEnabled != null && rptEnabled.booleanValue();
  }

  @Override
  public void setReportEnabled(boolean enabled) {
    rptEnabled = enabled;
  }

  public Boolean getHasChild() {
    return hasChild == null ? true : hasChild;
  }

  public void setHasChild(Boolean hasChild) {
    this.hasChild = hasChild;
  }

  public Boolean getHasParent() {
    return hasParent == null ? true : hasParent;
  }

  public void setHasParent(Boolean hasParent) {
    this.hasParent = hasParent;
  }


  public Date getLastUpdatedOn() {
    return uo;
  }

  public void setLastUpdatedOn(Date luo) {
    this.uo = luo;
  }

  public String getLastUpdatedBy() {
    return ub;
  }

  public void setLastUpdatedBy(String lub) {
    this.ub = lub;
  }

}

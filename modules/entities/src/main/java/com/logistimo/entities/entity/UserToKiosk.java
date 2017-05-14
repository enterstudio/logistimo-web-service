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
package com.logistimo.entities.entity;

import com.logistimo.utils.NumberUtil;

import java.util.Date;

import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.NotPersistent;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

/**
 * @author arun
 */
@PersistenceCapable(identityType = IdentityType.APPLICATION, detachable = "true", table = "USERTOKIOSK")
public class UserToKiosk implements IUserToKiosk {

  @PrimaryKey
  @Persistent(valueStrategy = IdGeneratorStrategy.IDENTITY)
  private Long UserToKioskId;
  @Persistent
  private String userId;
  @Persistent
  private Long kioskId;
  @NotPersistent
  private String
      knm;
  // normalized kiosk name that can be used in a query for sorting on kiosk names
  @Persistent
  private Integer ri = DEFAULT_ROUTE_INDEX; // Route Index
  @Persistent
  private String tg; // tag (say, a route tag)
  @Persistent
  private Long dId;

  @Persistent
  private Date arcAt;
  @Persistent
  private String arcBy;


  /**
   * @return the userToKioskId
   */
  @Override
  public Long getUserToKioskId() {
    return UserToKioskId;
  }

  /**
   * @return the userId
   */
  @Override
  public String getUserId() {
    return userId;
  }

  /**
   * @param userId the userId to set
   */
  @Override
  public void setUserId(String userId) {
    this.userId = userId;
  }

  /**
   * @param kioskId the kioskId to set
   */
  @Override
  public void setKioskId(Long kioskId, String kioskName) {
    this.kioskId = kioskId;
    if (kioskName != null) {
      knm = kioskName.toLowerCase();
    }
  }

  @Override
  public void setNormalizedKioskName(String kioskName) {
    if (kioskName != null) {
      knm = kioskName.toLowerCase();
    } else {
      knm = null;
    }
  }

  /**
   * @return the kioskId
   */
  @Override
  public Long getKioskId() {
    return kioskId;
  }

  @Override
  public int getRouteIndex() {
    return NumberUtil.getIntegerValue(ri);
  }

  @Override
  public void setRouteIndex(int ri) {
    this.ri = new Integer(ri);
  }

  @Override
  public String getTag() {
    return tg;
  }

  @Override
  public void setTag(String tag) {
    tg = tag;
  }

  @Override
  public Long getDomainId() {
    return dId;
  }

  @Override
  public void setDomainId(Long domainId) {
    dId = domainId;
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

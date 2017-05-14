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
package com.logistimo.entity;

import com.logistimo.domains.ISuperDomain;
import com.logistimo.entity.IBBoard;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.jdo.annotations.Column;
import javax.jdo.annotations.Element;
import javax.jdo.annotations.Extension;
import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.Join;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;


/**
 * Represents a bulletin board
 *
 * @author Arun
 */
@PersistenceCapable(identityType = IdentityType.APPLICATION, detachable = "true")
public class BBoard implements IBBoard, ISuperDomain {

  @PrimaryKey
  @Persistent(valueStrategy = IdGeneratorStrategy.IDENTITY)
  private Long ky;

  @Persistent(table = "BBOARD_DOMAINS", defaultFetchGroup = "true")
  @Join
  @Element(column = "DOMAIN_ID")
  private List<Long> dId;
  @Persistent
  private Long sdId;
  @Persistent
  private Integer eId; // event Id, if any
  @Persistent
  private String ty; // type of object (e.g. order, inventory, transaction, user, adhoc, etc.)
  @Persistent
  private Long eky; // key of correspoding event object, if any
  @Persistent
  @Column(length = 2048)
  private String ms; // message
  @Persistent
  @Extension(vendorName = "datanucleus", key = "gae.unindexed", value = "true")
  private String murl; // medial URL (if any)
  @Persistent
  private Long kId; // kiosk Id, if any
  @Persistent
  private String cty; // city, if any
  @Persistent
  private String dst; // district, if any
  @Persistent
  private String st; // state, if any
  @Join
  @Element(column = "tag")
  @Persistent
  private List<String> tgs; // free-form tag, if any
  @Persistent
  private String uId; // user who posted, if any
  @Persistent
  private Date t; // timestamp

  @Override
  public Long getKey() {
    return ky;
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
  public String getType() {
    return ty;
  }

  @Override
  public void setType(String type) {
    ty = type;
  }

  @Override
  public Long getEventKey() {
    return eky;
  }

  @Override
  public void setEventKey(Long eky) {
    this.eky = eky;
  }

  @Override
  public Integer getEventId() {
    return eId;
  }

  @Override
  public void setEventId(Integer eventId) {
    eId = eventId;
  }

  @Override
  public String getMessage() {
    return ms;
  }

  @Override
  public void setMessage(String message) {
    ms = message;
  }

  @Override
  public String getMediaUrl() {
    return murl;
  }

  @Override
  public void setMediaUrl(String mediaUrl) {
    murl = mediaUrl;
  }

  @Override
  public Long getKioskId() {
    return kId;
  }

  @Override
  public void setKioskId(Long kioskId) {
    kId = kioskId;
  }

  @Override
  public String getCity() {
    return cty;
  }

  @Override
  public void setCity(String city) {
    cty = city;
  }

  @Override
  public String getDistrict() {
    return dst;
  }

  @Override
  public void setDistrict(String district) {
    dst = district;
  }

  @Override
  public String getState() {
    return st;
  }

  @Override
  public void setState(String state) {
    st = state;
  }

  @Override
  public List<String> getTags() {
    return tgs;
  }

  @Override
  public void setTags(List<String> tags) {
    tgs = tags;
  }

  @Override
  public String getUserId() {
    return uId;
  }

  @Override
  public void setUserId(String userId) {
    uId = userId;
  }

  @Override
  public Date getTimestamp() {
    return t;
  }

  @Override
  public void setTimestamp(Date timestamp) {
    t = timestamp;
  }
}

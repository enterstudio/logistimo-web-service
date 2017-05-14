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

package com.logistimo.assets.entity;

import com.logistimo.config.models.EventsConfig;
import com.logistimo.users.entity.IUserAccount;

import com.logistimo.config.models.DomainConfig;
import com.logistimo.services.Resources;
import com.logistimo.services.ServiceException;
import com.logistimo.services.Services;
import com.logistimo.services.impl.PMF;
import com.logistimo.utils.StringUtil;
import com.logistimo.logger.XLog;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

import javax.jdo.PersistenceManager;
import javax.jdo.annotations.Element;
import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.Join;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

/**
 * Created by kaniyarasu on 02/11/15.
 */
@PersistenceCapable(identityType = IdentityType.APPLICATION, detachable = "true")
public class Asset implements IAsset {
  private static final XLog xLogger = XLog.getLog(Asset.class);

  @PrimaryKey
  @Persistent(valueStrategy = IdGeneratorStrategy.IDENTITY)
  private Long id;

  @Persistent(table = "ASSET_DOMAINS", defaultFetchGroup = "true")
  @Join
  @Element(column = "DOMAIN_ID")
  private List<Long> dId;

  @Persistent
  private Long sdId; // source domain Id

  @Persistent
  private String sId; //Serial number

  @Persistent
  private String vId; //Vendor/Mancfacturer id

  @Persistent
  private Long kId; // kiosk Id

  @Persistent
  private String lId; //asset location Id

  @Persistent
  private Integer type; //asset type

  @Persistent
  private String model; //asset model

  @Persistent
  private String nsId; //Normalized serial id for searching

  @Persistent(table = "ASSET_OWNERS", defaultFetchGroup = "true")
  @Join(column = "id")
  @Element(column = "userId")
  private List<String> ons; //Owner user Ids

  @Persistent(table = "ASSET_MAINTAINERS", defaultFetchGroup = "true")
  @Join(column = "id")
  @Element(column = "userId")
  private List<String> mns; //Maintainer user Ids

  //Create meta
  @Persistent
  private Date cOn;

  @Persistent
  private String cb;

  //Update meta
  @Persistent
  private Date uOn;

  @Persistent
  private String ub; // last updated user

  @Persistent
  private Date arcAt;

  @Persistent
  private String arcBy;

  @Persistent
  private Integer yom;

  @Override
  public String getModel() {
    return model;
  }

  @Override
  public void setModel(String model) {
    this.model = model;
  }

  @Override
  public String getNsId() {
    return nsId;
  }

  @Override
  public void setNsId(String nsId) {
    this.nsId = nsId;
  }

  @Override
  public String getLocationId() {
    return lId;
  }

  @Override
  public void setLocationId(String lId) {
    this.lId = lId;
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
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

  public Long getDomainId() {
    return sdId;
  }

  public void setDomainId(Long sdId) {
    this.sdId = sdId;
  }

  public String getSerialId() {
    return sId;
  }

  public void setSerialId(String sId) {
    this.sId = sId;
  }

  public String getVendorId() {
    return vId;
  }

  public void setVendorId(String vId) {
    this.vId = vId;
  }

  public Long getKioskId() {
    return kId;
  }

  public void setKioskId(Long kId) {
    this.kId = kId;
  }

  public List<String> getOwners() {
    return ons;
  }

  public void setOwners(List<String> ons) {
    this.ons = ons;
  }

  public List<String> getMaintainers() {
    return mns;
  }

  public void setMaintainers(List<String> mns) {
    this.mns = mns;
  }

  public Date getCreatedOn() {
    return cOn;
  }

  public void setCreatedOn(Date cOn) {
    this.cOn = cOn;
  }

  public String getUpdatedBy() {
    return ub;
  }

  public void setUpdatedBy(String ub) {
    this.ub = ub;
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

  public String getCreatedBy() {
    return cb;
  }

  public void setCreatedBy(String cb) {
    this.cb = cb;
  }

  public Date getUpdatedOn() {
    return uOn;
  }

  public void setUpdatedOn(Date uOn) {
    this.uOn = uOn;
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
  public Integer getYom() {
    return yom;
  }

  @Override
  public void setYom(Integer yom) {
    this.yom = yom;
  }

}

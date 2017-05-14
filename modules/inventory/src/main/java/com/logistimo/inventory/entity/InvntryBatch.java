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

import com.google.gson.annotations.Expose;

import com.logistimo.config.models.EventsConfig;
import com.logistimo.entities.entity.IKiosk;
import com.logistimo.entities.entity.Kiosk;
import com.logistimo.entities.service.EntitiesService;
import com.logistimo.entities.service.EntitiesServiceImpl;
import com.logistimo.inventory.service.InventoryManagementService;
import com.logistimo.inventory.service.impl.InventoryManagementServiceImpl;
import com.logistimo.materials.entity.IMaterial;
import com.logistimo.materials.entity.Material;
import com.logistimo.materials.service.MaterialCatalogService;
import com.logistimo.materials.service.impl.MaterialCatalogServiceImpl;
import com.logistimo.tags.TagUtil;
import com.logistimo.tags.entity.ITag;
import com.logistimo.tags.entity.Tag;

import com.logistimo.config.models.DomainConfig;
import com.logistimo.services.Resources;
import com.logistimo.services.Services;
import com.logistimo.services.impl.PMF;
import com.logistimo.proto.JsonTagsZ;
import com.logistimo.utils.BigUtil;
import com.logistimo.constants.Constants;
import com.logistimo.utils.LocalDateUtil;
import com.logistimo.logger.XLog;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

import javax.jdo.PersistenceManager;
import javax.jdo.annotations.Element;
import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.Join;
import javax.jdo.annotations.NotPersistent;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

@PersistenceCapable(identityType = IdentityType.APPLICATION, detachable = "true")
public class InvntryBatch implements IInvntryBatch {
  private static final XLog xLogger = XLog.getLog(InvntryBatch.class);

  @PrimaryKey
  @Persistent(valueStrategy = IdGeneratorStrategy.IDENTITY)
  private Long key; // child of inventory
  @Persistent(table = "INVNTRYBATCH_DOMAINS", defaultFetchGroup = "true")
  @Join
  @Element(column = "DOMAIN_ID")
  private List<Long> dId;
  @Persistent
  private Long sdId; // source domain Id
  @Persistent
  private Long kId; // kiosk id
  @Persistent
  private Long mId; // material id
  @Expose
  @Persistent
  private BigDecimal q = BigDecimal.ZERO; // quantity
  @Persistent
  private BigDecimal aStk = BigDecimal.ZERO;
  @Persistent
  private BigDecimal atpStk = BigDecimal.ZERO;

  @Expose
  @Persistent
  private String bid; // batch id/number
  @Expose
  @Persistent
  private Date bexp; // batch expiry date
  @Expose
  @Persistent
  private String bmfnm; // batch manufacturer name
  @Expose
  @Persistent
  private Date bmfdt; // batch manufactured date
  @Expose
  @Persistent
  private Date t; // created/last updated

  @Persistent(table = "INVNTRYBATCH_MTAGS", defaultFetchGroup = "true")
  @Join(column = "key")
  @Element(column = "id")
  private List<Tag> mtgs;

  @NotPersistent
  private List<String>
      oldmtgs;
  // list of material mtgs, if present (used mainly for queries involving kioskId and mtgs)

  @Persistent(table = "INVNTRYBATCH_KTAGS", defaultFetchGroup = "true")
  @Join(column = "key")
  @Element(column = "id")
  private List<Tag> ktgs;

  @NotPersistent
  private List<String> oldktgs; // list of kiosk mtgs (for queries)
  @Persistent
  private Boolean vld = false; // true, if q > 0 and not expired

  @Persistent
  private Date arcAt;
  @Persistent
  private String arcBy;


  public Long getKey() {
    return key;
  }

  public void setKey(Long key) {
    this.key = key;
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
  public Long getKioskId() {
    return kId;
  }

  @Override
  public void setKioskId(Long kioskId, String kioskName) {
    kId = kioskId;
  }

  @Override
  public Long getMaterialId() {
    return mId;
  }

  @Override
  public void setMaterialId(Long materialId) {
    mId = materialId;
  }

  @Override
  public BigDecimal getQuantity() {
    return BigUtil.getZeroIfNull(q);
  }

  @Override
  public void setQuantity(BigDecimal q) {
    this.q = q;
    setValidity();
  }

  @Override
  public BigDecimal getAllocatedStock() {
    return BigUtil.getZeroIfNull(aStk);
  }

  @Override
  public void setAllocatedStock(BigDecimal stock) {
    this.aStk = stock;
  }

  @Override
  public BigDecimal getAvailableStock() {
    return BigUtil.getZeroIfNull(atpStk);
  }

  @Override
  public void setAvailableStock(BigDecimal stock) {
    this.atpStk = stock;
  }

  @Override
  public String getBatchId() {
    return bid;
  }

  @Override
  public void setBatchId(String bid) {
    this.bid = bid != null ? bid.toUpperCase() : null;
  }

  @Override
  public Date getBatchExpiry() {
    return bexp;
  }

  @Override
  public void setBatchExpiry(Date batchExpiry) {
    bexp = batchExpiry;
    setValidity();
  }

  @Override
  public boolean isExpired() {
    return (bexp != null && bexp.compareTo(LocalDateUtil
        .getZeroTime(DomainConfig.getInstance(sdId).getTimezone()).getTime()) < 0);
  }

  @Override
  public String getBatchManufacturer() {
    return bmfnm;
  }

  @Override
  public void setBatchManufacturer(String manufacturerName) {
    bmfnm = manufacturerName;
  }

  @Override
  public Date getBatchManufacturedDate() {
    return bmfdt;
  }

  @Override
  public void setBatchManufacturedDate(Date manufacturedDate) {
    bmfdt = manufacturedDate;
  }

  @Override
  public Date getTimestamp() {
    return t;
  }

  @Override
  public void setTimestamp(Date t) {
    this.t = t;
  }

  @Override
  public List<String> getTags(String tagType) {
    if (TagUtil.TYPE_MATERIAL.equals(tagType)) {
      if (oldmtgs == null) {
        oldmtgs = TagUtil.getList(mtgs);
      }
      return oldmtgs;
    } else if (TagUtil.TYPE_ENTITY.equals(tagType)) {
      if (oldktgs == null) {
        oldktgs = TagUtil.getList(ktgs);
      }
      return oldktgs;
    } else {
      return null;
    }
  }

  @Override
  public void setTags(List<String> tags, String tagType) {
    if (TagUtil.TYPE_MATERIAL.equals(tagType)) {
      oldmtgs = tags;
    } else if (TagUtil.TYPE_ENTITY.equals(tagType)) {
      oldktgs = tags;
    }
  }

  @Override
  public void setTgs(List<? extends ITag> tags, String tagType) {
    if (TagUtil.TYPE_MATERIAL.equals(tagType)) {
      if (this.mtgs != null) {
        this.mtgs.clear();
        if (tags != null) {
          this.mtgs.addAll((Collection<Tag>) tags);
        }
      } else {
        this.mtgs = (List<Tag>) tags;
      }
      this.oldmtgs = null;
    } else if (TagUtil.TYPE_ENTITY.equals(tagType)) {
      if (this.ktgs != null) {
        this.ktgs.clear();
        if (tags != null) {
          this.ktgs.addAll((Collection<Tag>) tags);
        }
      } else {
        this.ktgs = (List<Tag>) tags;
      }
      this.oldktgs = null;
    }
  }

  private void setValidity() {
    vld = !(BigUtil.equalsZero(getQuantity()) || isExpired());
  }

  @Override
  public Boolean getVld() {
    return vld;
  }

  @Override
  public void setVld(Boolean vld) {
    this.vld = vld;
  }




  @Override
  public String getKeyString() {
    return String.valueOf(key);
  }

  // Get the metadata in a form required for J2ME protocol
  @Override
  public Hashtable<String, String> toMapZ(Locale locale, String timezone,
                                          boolean isAutoPostingIssuesEnabled) {
    Hashtable<String, String> batch = new Hashtable<String, String>();
    batch.put(JsonTagsZ.BATCH_ID, bid);
    if (bexp != null) {
      batch.put(JsonTagsZ.BATCH_EXPIRY,
          LocalDateUtil.formatCustom(bexp, Constants.DATE_FORMAT, timezone));
    }
    if (bmfnm != null && !bmfnm.isEmpty()) {
      batch.put(JsonTagsZ.BATCH_MANUFACTUER_NAME, bmfnm);
    }
    if (bmfdt != null) {
      batch.put(JsonTagsZ.BATCH_MANUFACTURED_DATE,
          LocalDateUtil.formatCustom(bmfdt, Constants.DATE_FORMAT, timezone));
    }
    batch.put(JsonTagsZ.QUANTITY, BigUtil.getFormattedValue(q));
    if (t != null) {
      batch.put(JsonTagsZ.TIMESTAMP, LocalDateUtil.format(t, locale, timezone));
    }

    if (isAutoPostingIssuesEnabled) {
      batch.put(JsonTagsZ.ALLOCATED_QUANTITY, BigUtil.getFormattedValue(getAllocatedStock()));
      batch.put(JsonTagsZ.AVAILABLE_QUANTITY, BigUtil.getFormattedValue(getAvailableStock()));
    }
    return batch;
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

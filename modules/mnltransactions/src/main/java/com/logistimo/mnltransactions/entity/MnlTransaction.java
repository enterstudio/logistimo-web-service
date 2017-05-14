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

package com.logistimo.mnltransactions.entity;

import com.logistimo.services.Resources;
import com.logistimo.services.Services;
import com.logistimo.utils.BigUtil;
import com.logistimo.constants.Constants;
import com.logistimo.utils.LocalDateUtil;
import com.logistimo.utils.NumberUtil;
import com.logistimo.utils.StringUtil;

import com.logistimo.config.models.DomainConfig;
import com.logistimo.entities.entity.IKiosk;
import com.logistimo.entities.service.EntitiesService;
import com.logistimo.entities.service.EntitiesServiceImpl;
import com.logistimo.materials.entity.IMaterial;
import com.logistimo.materials.service.MaterialCatalogService;
import com.logistimo.materials.service.impl.MaterialCatalogServiceImpl;
import com.logistimo.tags.TagUtil;
import com.logistimo.tags.dao.TagDao;
import com.logistimo.tags.entity.ITag;
import com.logistimo.tags.entity.Tag;

import com.logistimo.logger.XLog;

import java.math.BigDecimal;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

import javax.jdo.annotations.Element;
import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.Join;
import javax.jdo.annotations.NotPersistent;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;


@PersistenceCapable(identityType = IdentityType.APPLICATION, detachable = "true")
public class MnlTransaction implements IMnlTransaction {
  private static final XLog xLogger = XLog.getLog(XLog.class);


  @PrimaryKey
  @Persistent(valueStrategy = IdGeneratorStrategy.IDENTITY)
  private Long key; // kId.mId.timestamp similar to Invntry.createKey
  @Persistent(table = "MNLTRANSACTION_DOMAINS", defaultFetchGroup = "true")
  @Join
  @Element(column = "DOMAIN_ID")
  private List<Long> dId;
  @Persistent
  private Long sdId; // source domain ID
  @Persistent
  private String uId; // User ID
  @Persistent
  private Long kId; // Kiosk/Entity ID
  @Persistent
  private Long mId; // Material ID
  @Persistent
  private BigDecimal os; // Opening Stock
  @Persistent
  private Date rp; // Reporting Period ( parse the String in MM/yyyy format into a Date object)
  @Persistent
  private BigDecimal rq; // Receipt quantity
  @Persistent
  private BigDecimal iq; // Issue quantity
  @Persistent
  private BigDecimal dq; // Discard/Wastage quanity
  @Persistent
  private Integer sod; // Stockout duration (days)
  //	@Persistent
//	private Integer nso; // Number of stock out instances
  @Persistent
  private BigDecimal mcr; // Manual consumption rate
  @Persistent
  private BigDecimal ccr; // Computed consumption rate
  @Persistent
  private BigDecimal moq; // Manual order quantity
  @Persistent
  private BigDecimal coq; // Computed order quantity

  @NotPersistent
  private List<String> oldtgs; // Tags

  @Persistent(table = "MNLTRANSACTION_OTAGS", defaultFetchGroup = "true")
  @Join(column = "key")
  @Element(column = "id")
  private List<Tag> tgs;

  @Persistent
  private Long vId; // Vendor Id corresponding to the vendor name
  @Persistent
  private Date t; // Time stamp

  public Long getKey() {
    return key;
  }

  public void setKey(Long key) {
    this.key = key;
  }

  @Override
  public String getKeyString() {
    return String.valueOf(key);
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
  public String getUserId() {
    return uId;
  }

  @Override
  public void setUserId(String userId) {
    this.uId = userId;
  }

  @Override
  public Long getKioskId() {
    return kId;
  }

  @Override
  public void setKioskId(Long kioskId) {
    this.kId = kioskId;
  }

  @Override
  public Long getMaterialId() {
    return mId;
  }

  @Override
  public void setMaterialId(Long materialId) {
    this.mId = materialId;
  }

  @Override
  public BigDecimal getOpeningStock() {
    return BigUtil.getZeroIfNull(os);
  }

  @Override
  public void setOpeningStock(BigDecimal openingStock) {
    this.os = openingStock;
  }

  @Override
  public Date getReportingPeriod() {
    return rp;
  }

  @Override
  public void setReportingPeriod(Date reportingPeriod) {
    this.rp = reportingPeriod;
  }

  @Override
  public BigDecimal getReceiptQuantity() {
    return BigUtil.getZeroIfNull(rq);
  }

  @Override
  public void setReceiptQuantity(BigDecimal receiptQuantity) {
    this.rq = receiptQuantity;
  }

  @Override
  public BigDecimal getIssueQuantity() {
    return BigUtil.getZeroIfNull(iq);
  }

  @Override
  public void setIssueQuantity(BigDecimal issueQuantity) {
    this.iq = issueQuantity;
  }

  @Override
  public BigDecimal getDiscardQuantity() {
    return BigUtil.getZeroIfNull(dq);
  }

  @Override
  public void setDiscardQuantity(BigDecimal discardQuantity) {
    this.dq = discardQuantity;
  }

  @Override
  public double getStockoutDuration() {
    return NumberUtil.getIntegerValue(sod);
  }

  @Override
  public void setStockoutDuration(int stockoutDuration) {
    this.sod = stockoutDuration;
  }

//	@Override
//	public void setNumberOfStockouts(int noOfStockoutInstances) { this.nso = noOfStockoutInstances; }
//	@Override
//	public double getNumberOfStockoutInstances() { return NumberUtil.getIntegerValue( nso ); }

  @Override
  public BigDecimal getManualConsumptionRate() {
    return BigUtil.getZeroIfNull(mcr);
  }

  @Override
  public void setManualConsumptionRate(BigDecimal manualConsumptionRate) {
    this.mcr = manualConsumptionRate;
  }

  @Override
  public BigDecimal getComputedConsumptionRate() {
    return BigUtil.getZeroIfNull(ccr);
  }

  @Override
  public void setComputedConsumptionRate(BigDecimal computedConsumptionRate) {
    this.ccr = computedConsumptionRate;
  }

  @Override
  public BigDecimal getOrderedQuantity() {
    return BigUtil.getZeroIfNull(moq);
  }

  @Override
  public void setOrderedQuantity(BigDecimal orderedQuantity) {
    this.moq = orderedQuantity;
  }

  @Override
  public BigDecimal getFulfilledQuantity() {
    return BigUtil.getZeroIfNull(coq);
  }

  @Override
  public void setFulfilledQuantity(BigDecimal fulfilledQuantity) {
    this.coq = fulfilledQuantity;
  }

  @Override
  public BigDecimal getClosingStock() {
    return getOpeningStock().add(getReceiptQuantity()).subtract(getIssueQuantity())
        .subtract(getDiscardQuantity());
  }

  @Override
  public void setTgs(List<? extends ITag> tags) {
    if (this.tgs != null) {
      this.tgs.clear();
      if (tags != null) {
        this.tgs.addAll((List<Tag>) tags);
      }
    } else {
      this.tgs = (List<Tag>) tags;
    }
    this.oldtgs = null;
  }

  @Override
  public List<String> getTags() {
    if (oldtgs == null) {
      oldtgs = TagUtil.getList(tgs);
    }
    return oldtgs;
  }

  @Override
  public void setTags(List<String> tags) {
    this.oldtgs = tags;
  }

  @Override
  public boolean hasTag(String tag) {
    getTags();
    return oldtgs != null && oldtgs.contains(tag);
  }

  @Override
  public Long getVendorId() {
    return vId;
  }

  @Override
  public void setVendorId(Long vendorId) {
    this.vId = vendorId;
  }

  @Override
  public Date getTimestamp() {
    return t;
  }

  @Override
  public void setTimestamp(Date timeStamp) {
    this.t = timeStamp;
  }

}

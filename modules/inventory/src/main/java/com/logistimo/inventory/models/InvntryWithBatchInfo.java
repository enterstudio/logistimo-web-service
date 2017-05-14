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

package com.logistimo.inventory.models;

import com.logistimo.constants.Constants;
import com.logistimo.events.entity.IEvent;
import com.logistimo.inventory.entity.IInvntry;
import com.logistimo.inventory.entity.IInvntryBatch;
import com.logistimo.inventory.entity.IInvntryEvntLog;
import com.logistimo.logger.XLog;
import com.logistimo.tags.TagUtil;
import com.logistimo.utils.BigUtil;
import com.logistimo.utils.LocalDateUtil;
import com.logistimo.utils.NumberUtil;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Date;
import java.util.List;

public class InvntryWithBatchInfo {
  private static final XLog xLogger = XLog.getLog(InvntryWithBatchInfo.class);

  private Long dId; // domain Id
  private Long mId; // material Id
  private Long kId; // kiosk Id
  private Float svclvl = 85F; // service level
  private String imdl = IInvntry.MODEL_SQ; // inventory model
  private BigDecimal ldtm = new BigDecimal(Constants.LEADTIME_DEFAULT); // lead time in days
  private BigDecimal stk = BigDecimal.ZERO; // current stock
  private Integer stockevent = IEvent.NORMAL; // stock event (understock, overstock, zero stock)
  private Long stkeventdur; // stock event duration (days)
  private BigDecimal ldtdmd = BigDecimal.ZERO; // lead time demand
  private BigDecimal rvpdmd = BigDecimal.ZERO; // forecasted demand (review period demand)
  private BigDecimal stdv = BigDecimal.ZERO; // std. deviation of rev. period demand
  private BigDecimal ordp = BigDecimal.ZERO; // days; order periodicity
  private BigDecimal sfstk = BigDecimal.ZERO; // safety stock
  private Date t; // creation or last stock update timestamp
  private String knm; // kiosk name - redundantly stored here to enable sorting Inventory by kiosk
  private String mnm;
  // material name - redundantly stored here to enable sorting Inventory by material
  private BigDecimal reord = BigDecimal.ZERO; // re-order levels or MIN
  private BigDecimal max = BigDecimal.ZERO; // MAX level
  private BigDecimal crD = BigDecimal.ZERO; // daily consumption rate over max. historical period
  private BigDecimal crW = BigDecimal.ZERO; // weekly consumption rate over max. historical period
  private BigDecimal crM = BigDecimal.ZERO; // monthly consumption rate over max. historical period
  private BigDecimal Q = BigDecimal.ZERO; // economic order quantity
  private Date tPS; // last updated time for P and/or S
  private Date tDQ; // last updated time for D and/or Q
  private String omsg; // optimization err/other msg, if any
  private List<String> tgs;
  // list of material tags, if present (used mainly for queries involving kioskId and tags)
  private List<String> ktgs; // list of kiosk tags (for queries)
  private BigDecimal prc; // retailer price
  private BigDecimal tx; // tax rate in %
  private BigDecimal crMnl; // manual consumption rate, if any
  private String bid; // batch id/number
  private BigDecimal bq = BigDecimal.ZERO; // quantity
  private Date bexp; // batch expiry date
  private String bmfnm; // batch manufacturer name
  private Date bmfdt; // batch manufactured date
  private Date bt; // batch created/last updated
  private String ub; // last inventory updated user
  private Date cOn; // inventory object created-on date (can be null for older objects in db.)
  private Date reordT; // time when min/reorder-level was last updated
  private Date maxT; // time when max was last updated
  private Date prcT; // time when retailer price was last updated
  private BigDecimal minDur = BigDecimal.ZERO; // Minimum duration of stock
  private BigDecimal maxDur = BigDecimal.ZERO; // Maximum duration of stock

  public void setInvntryParameters(IInvntry inv) {
    // Set the Invntry parameters first
    this.setDomainId(inv.getDomainId());
    this.setKioskId(inv.getKioskId());
    this.setMaterialId(inv.getMaterialId());
    this.setServiceLevel(inv.getServiceLevel());
    this.setInventoryModel(inv.getInventoryModel());
    this.setLeadTime(inv.getLeadTime());
    this.setStock(inv.getStock());
    this.setLeadTimeDemand(inv.getLeadTimeDemand());
    this.setRevPeriodDemand(inv.getRevPeriodDemand());
    this.setStdevRevPeriodDemand(inv.getStdevRevPeriodDemand());
    this.setOrderPeriodicity(inv.getOrderPeriodicity());
    this.setSafetyStock(inv.getSafetyStock());
    this.setTimestamp(inv.getTimestamp());
    this.setKioskName(inv.getKioskName());
    this.setMaterialName(inv.getMaterialName());
    this.setReorderLevel(inv.getReorderLevel());
    this.setMaxStock(inv.getMaxStock());
    this.setConsumptionRateDaily(inv.getConsumptionRateDaily());
    this.setConsumptionRateWeekly(inv.getConsumptionRateWeekly());
    this.setConsumptionRateMonthly(inv.getConsumptionRateMonthly());
    this.setEconomicOrderQuantity(inv.getEconomicOrderQuantity());
    this.setPSTimestamp(inv.getPSTimestamp());
    this.setDQTimestamp(inv.getDQTimestamp());
    this.setOptMessage(inv.getOptMessage());
    this.setTags(inv.getTags(TagUtil.TYPE_ENTITY), TagUtil.TYPE_ENTITY);
    this.setTags(inv.getTags(TagUtil.TYPE_MATERIAL), TagUtil.TYPE_MATERIAL);
    this.setRetailerPrice(inv.getRetailerPrice());
    this.setTax(inv.getTax());
    this.setConsumptionRateManual(inv.getConsumptionRateManual());
    this.setUpdatedBy(inv.getUpdatedBy());
    this.setCreatedOn(inv.getCreatedOn());
    this.setMaxUpdatedTime(inv.getMaxUpdatedTime());
    this.setReorderLevelUpdatedTime(inv.getReorderLevelUpdatedTime());
    this.setRetailerPriceUpdatedTime(inv.getRetailerPriceUpdatedTime());
    this.setMinDuration(inv.getMinDuration());
    this.setMaxDuration(inv.getMaxDuration());
  }

  public void setBatchInfoParameters(IInvntryBatch invBatch) {
    // Also set the batch info.
    this.setBatchId(invBatch.getBatchId());
    this.setStockInBatch(invBatch.getQuantity());
    this.setBatchExpiry(invBatch.getBatchExpiry());
    this.setBatchManufacturer(invBatch.getBatchManufacturer());
    this.setBatchManufacturedDate(invBatch.getBatchManufacturedDate());
    this.setBatchTimestamp(invBatch.getTimestamp());
  }

  public void setInvntryEventParameters(IInvntryEvntLog invLog) {
    this.setStockEvent(invLog.getType());
    this.setStockEventDuration(
        (System.currentTimeMillis() - invLog.getStartDate().getTime())
            / LocalDateUtil.MILLISECS_PER_DAY);
  }

  public Long getDomainId() {
    return dId;
  }

  public void setDomainId(Long domainId) {
    this.dId = domainId;
  }

  public Long getMaterialId() {
    return mId;
  }

  public void setMaterialId(Long materialId) {
    this.mId = materialId;
  }

  public Long getKioskId() {
    return kId;
  }

  public void setKioskId(Long kioskId) {
    this.kId = kioskId;
  }

  public float getServiceLevel() {
    return NumberUtil.getFloatValue(svclvl);
  }

  public void setServiceLevel(float serviceLevel) {
    this.svclvl = new Float(serviceLevel);
  }

  public String getInventoryModel() {
    return imdl;
  }

  public void setInventoryModel(String inventoryModel) {
    this.imdl = inventoryModel;
  }

  public BigDecimal getLeadTime() {
    return BigUtil.getZeroIfNull(ldtm);
  }

  public void setLeadTime(BigDecimal leadTime) {
    this.ldtm = leadTime;
  }

  public BigDecimal getStock() {
    return BigUtil.getZeroIfNull(stk);
  }

  public void setStock(BigDecimal stock) {
    this.stk = stock;
  }

  public void setStockEvent(Integer stkEvent) {
    this.stockevent = stkEvent;
  }

  public Integer getStockEvent() {
    return this.stockevent;
  }

  public void setStockEventDuration(Long stkEventDur) {
    this.stkeventdur = stkEventDur;
  }

  public Long getStockEventDuration() {
    return this.stkeventdur;
  }

  public BigDecimal getLeadTimeDemand() {
    return BigUtil.getZeroIfNull(ldtdmd);
  }

  public void setLeadTimeDemand(BigDecimal leadTimeDemand) {
    this.ldtdmd = leadTimeDemand;
  }

  public BigDecimal getRevPeriodDemand() {
    return BigUtil.getZeroIfNull(rvpdmd);
  }

  public void setRevPeriodDemand(BigDecimal revPeriodDemand) {
    this.rvpdmd = revPeriodDemand;
  }

  public BigDecimal getStdevRevPeriodDemand() {
    return BigUtil.getZeroIfNull(stdv);
  }

  public void setStdevRevPeriodDemand(BigDecimal stdevRevPeriodDemand) {
    this.stdv = stdevRevPeriodDemand;
  }

  public BigDecimal getSafetyStock() {
    return BigUtil.getZeroIfNull(sfstk);
  }

  public void setSafetyStock(BigDecimal safetyStock) {
    this.sfstk = safetyStock;
  }

  public Date getTimestamp() {
    return t;
  }

  public void setTimestamp(Date timeStamp) {
    this.t = timeStamp;
  }

  public BigDecimal getOrderPeriodicity() {
    return BigUtil.getZeroIfNull(ordp);
  }

  public void setOrderPeriodicity(BigDecimal orderPeriodicity) {
    this.ordp = orderPeriodicity;
  }

  public String getKioskName() {
    return knm;
  }

  public void setKioskName(String kioskName) {
    if (kioskName != null) {
      this.knm = kioskName.toLowerCase();
    } else {
      this.knm = null;
    }
  }

  public String getMaterialName() {
    return mnm;
  }

  public void setMaterialName(String materialName) {
    if (materialName != null) {
      this.mnm = materialName.toLowerCase();
    } else {
      this.mnm = null;
    }
  }

  public BigDecimal getReorderLevel() {
    return BigUtil.getZeroIfNull(reord);
  }

  public void setReorderLevel(BigDecimal reordLevel) {
    this.reord = reordLevel;
  }

  public BigDecimal getMaxStock() {
    return BigUtil.getZeroIfNull(max);
  }

  public void setMaxStock(BigDecimal maxStock) {
    this.max = maxStock;
  }

  public BigDecimal getNormalizedSafetyStock() {
    BigDecimal safeStock = getReorderLevel();
    if (BigUtil.equalsZero(safeStock)) {
      safeStock = getSafetyStock();
    }
    return safeStock;
  }

  public boolean isStockUnsafe() {
    BigDecimal stock = getStock();
    BigDecimal safeStock = getNormalizedSafetyStock();
    return (BigUtil.greaterThanZero(safeStock)
        && BigUtil.greaterThanZero(stock)
        && BigUtil.lesserThanEquals(stock, safeStock));
  }

  public boolean isStockExcess() {
    BigDecimal maxStock = getMaxStock();
    return (BigUtil.greaterThanZero(maxStock) && BigUtil.greaterThan(getStock(), maxStock));
  }

  // Get the remaining period for which stock exists
  public BigDecimal getStockAvailabilityPeriod() {
    BigDecimal consumptionRate = BigUtil.getZeroIfNull(crMnl);
    if (BigUtil.greaterThanZero(consumptionRate)) {
      return getStock().divide(consumptionRate, RoundingMode.HALF_UP);
    }
    return BigDecimal.ZERO;
  }

  public BigDecimal getConsumptionRateManual() {
    return BigUtil.getZeroIfNull(crMnl);
  } // units are in OptimizerConfig

  public void setConsumptionRateManual(BigDecimal consumptionRate) {
    crMnl = consumptionRate;
  }

  public BigDecimal getConsumptionRateDaily() {
    return BigUtil.getZeroIfNull(crD);
  }

  public void setConsumptionRateDaily(BigDecimal crt) {
    this.crD = crt;
  }

  public BigDecimal getConsumptionRateWeekly() {
    return BigUtil.getZeroIfNull(crW);
  }

  public void setConsumptionRateWeekly(BigDecimal crt) {
    this.crW = crt;
  }

  public BigDecimal getConsumptionRateMonthly() {
    return BigUtil.getZeroIfNull(crM);
  }

  public void setConsumptionRateMonthly(BigDecimal crt) {
    this.crM = crt;
  }

  public BigDecimal getEconomicOrderQuantity() {
    return BigUtil.getZeroIfNull(Q);
  }

  public void setEconomicOrderQuantity(BigDecimal eoq) {
    this.Q = eoq;
  }

  public Date getPSTimestamp() {
    return tPS;
  }

  public void setPSTimestamp(Date tPS) {
    this.tPS = tPS;
  }

  public Date getDQTimestamp() {
    return tDQ;
  }

  public void setDQTimestamp(Date tDQ) {
    this.tDQ = tDQ;
  }

  public String getOptMessage() {
    return omsg;
  }

  public void setOptMessage(String msg) {
    this.omsg = msg;
  }

  public List<String> getTags(String tagType) {
    if (TagUtil.TYPE_MATERIAL.equals(tagType)) {
      return tgs;
    } else if (TagUtil.TYPE_ENTITY.equals(tagType)) {
      return ktgs;
    } else {
      return null;
    }
  }

  public void setTags(List<String> tags, String tagType) {
    if (TagUtil.TYPE_MATERIAL.equals(tagType)) {
      tgs = tags;
    } else if (TagUtil.TYPE_ENTITY.equals(tagType)) {
      ktgs = tags;
    }
  }

  public BigDecimal getRetailerPrice() {
    return BigUtil.getZeroIfNull(prc);
  }

  public void setRetailerPrice(BigDecimal price) {
    prc = price;
  }

  public BigDecimal getTax() {
    return BigUtil.getZeroIfNull(tx);
  }

  public void setTax(BigDecimal tax) {
    tx = tax;
  }

  public String getIdString() {
    return (kId + ":" + mId);
  }

  public String getBatchId() {
    return bid;
  }

  public void setBatchId(String bid) {
    this.bid = bid;
  }

  public BigDecimal getStockInBatch() {
    return BigUtil.getZeroIfNull(bq);
  }

  public void setStockInBatch(BigDecimal bq) {
    this.bq = bq;
  }

  public Date getBatchExpiry() {
    return bexp;
  }

  public void setBatchExpiry(Date batchExpiry) {
    bexp = batchExpiry;
  }

  public String getBatchManufacturer() {
    return bmfnm;
  }

  public void setBatchManufacturer(String manufacturerName) {
    bmfnm = manufacturerName;
  }

  public Date getBatchManufacturedDate() {
    return bmfdt;
  }

  public void setBatchManufacturedDate(Date manufacturedDate) {
    bmfdt = manufacturedDate;
  }

  public Date getBatchTimestamp() {
    return bt;
  }

  public void setBatchTimestamp(Date bt) {
    this.bt = bt;
  }

  public String getUpdatedBy() {
    return ub;
  }

  public void setUpdatedBy(String user) {
    this.ub = user;
  }

  public Date getCreatedOn() {
    return cOn;
  }

  public void setCreatedOn(Date createdOn) {
    cOn = createdOn;
    t = createdOn; // last stock updated time is set to creation time
  }

  public Date getReorderLevelUpdatedTime() {
    return reordT;
  } // min. update time

  public void setReorderLevelUpdatedTime(Date t) {
    this.reordT = t;
  }

  public Date getMaxUpdatedTime() {
    return maxT;
  }

  public void setMaxUpdatedTime(Date t) {
    this.maxT = t;
  }

  public Date getRetailerPriceUpdatedTime() {
    return prcT;
  }

  public void setRetailerPriceUpdatedTime(Date t) {
    this.prcT = t;
  }

  public BigDecimal getMinDuration() {
    return minDur;
  }

  public void setMinDuration(BigDecimal minDur) {
    this.minDur = minDur != null ? minDur : BigDecimal.ZERO;
  }

  public BigDecimal getMaxDuration() {
    return maxDur;
  }

  public void setMaxDuration(BigDecimal maxDur) {
    this.maxDur = maxDur != null ? maxDur : BigDecimal.ZERO;
  }
}

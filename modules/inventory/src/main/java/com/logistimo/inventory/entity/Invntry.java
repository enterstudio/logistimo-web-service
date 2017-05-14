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
package com.logistimo.inventory.entity;

import com.logistimo.config.models.DomainConfig;
import com.logistimo.config.models.InventoryConfig;
import com.logistimo.tags.TagUtil;
import com.logistimo.tags.entity.ITag;
import com.logistimo.tags.entity.Tag;

import com.logistimo.events.entity.IEvent;
import com.logistimo.services.Resources;
import com.logistimo.utils.BigUtil;
import com.logistimo.constants.Constants;
import com.logistimo.utils.NumberUtil;
import com.logistimo.logger.XLog;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

import javax.jdo.annotations.Element;
import javax.jdo.annotations.Extension;
import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.Join;
import javax.jdo.annotations.NotPersistent;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;


/**
 * @author arun
 */
@PersistenceCapable(identityType = IdentityType.APPLICATION, detachable = "true")
public class Invntry implements IInvntry {

  private static final XLog xLogger = XLog.getLog(Invntry.class);

  @PrimaryKey
  @Persistent(valueStrategy = IdGeneratorStrategy.IDENTITY)
  private Long key;
  @Persistent(table = "INVNTRY_DOMAINS", defaultFetchGroup = "true")
  @Join
  @Element(column = "DOMAIN_ID")
  private List<Long> dId;
  @Persistent
  private Long sdId; // source domain Id
  @Persistent
  private Long mId; // material Id
  @Persistent
  private Long kId; // kiosk Id
  @Persistent
  private Long sId; // short Id for SMS
  @Persistent
  private Float svclvl = 85F; // service level
  @Persistent
  private String imdl = MODEL_NONE; // inventory model (earlier: MODEL_SQ)
  @Persistent
  private BigDecimal ldtm = new BigDecimal(Constants.LEADTIME_DEFAULT); // lead time in days
  @Persistent
  private BigDecimal stk = BigDecimal.ZERO; // current stock
  @Persistent
  private BigDecimal aStk = BigDecimal.ZERO;
  @Persistent
  private BigDecimal tStk = BigDecimal.ZERO;
  @Persistent
  private BigDecimal atpStk = BigDecimal.ZERO;
  @Persistent
  private BigDecimal ldtdmd = BigDecimal.ZERO; // lead time demand
  @Persistent
  private BigDecimal rvpdmd = BigDecimal.ZERO; // forecasted demand (review period demand)
  @Persistent
  private BigDecimal stdv = BigDecimal.ZERO; // std. deviation of rev. period demand
  @Persistent
  private BigDecimal ordp = BigDecimal.ZERO; // days; order periodicity
  @Persistent
  private BigDecimal sfstk = BigDecimal.ZERO; // safety stock
  @Persistent
  private Date t; // last stock update timestamp (should be same as creation timestamp at start)

  @NotPersistent
  private String knm; // kiosk name - redundantly stored here to enable sorting Inventory by kiosk
  @NotPersistent
  private String mnm;
  // material name - redundantly stored here to enable sorting Inventory by material
  @Persistent
  private String b; //material binary valued
  @Persistent
  @Extension(vendorName = "datanucleus", key = "gae.unindexed", value = "true")
  private BigDecimal reord = BigDecimal.ZERO; // re-order levels or MIN
  @Persistent
  @Extension(vendorName = "datanucleus", key = "gae.unindexed", value = "true")
  private BigDecimal max = BigDecimal.ZERO; // MAX level
  @Persistent
  private BigDecimal minDur = BigDecimal.ZERO; // Minimum duration of stock
  @Persistent
  private BigDecimal maxDur = BigDecimal.ZERO; // Maximum duration of stock
  @Persistent
  private BigDecimal crD = BigDecimal.ZERO; // daily consumption rate over max. historical period
  @Persistent
  private BigDecimal Q = BigDecimal.ZERO; // economic order quantity
  @Persistent
  private Date tPS; // last updated time for P and/or S
  @Persistent
  private Date tDQ; // last updated time for D and/or Q
  @Persistent
  @Extension(vendorName = "datanucleus", key = "gae.unindexed", value = "true")
  private String omsg; // optimization err/other msg, if any

  @Persistent(table = "INVNTRY_MTAGS", defaultFetchGroup = "true")
  @Join(column = "key")
  @Element(column = "id")
  private List<Tag> tgs;

  @NotPersistent
  private List<String>
      oldtgs;
  // list of material tgs, if present (used mainly for queries involving kioskId and tgs)

  @Persistent(table = "INVNTRY_KTAGS", defaultFetchGroup = "true")
  @Join(column = "key")
  @Element(column = "id")
  private List<Tag> ktgs;

  @NotPersistent
  private List<String> oldktgs; // list of kiosk tgs (for queries)
  @Persistent
  private BigDecimal prc; // retailer price
  @Persistent
  @Extension(vendorName = "datanucleus", key = "gae.unindexed", value = "true")
  private BigDecimal tx; // tax rate in %
  @Persistent
  private Float tmin;
  @Persistent
  private Float tmax;
  @Persistent
  private Long lsev; // key to last stock event (such as stockout, understock or overstock)
  @Persistent
  private BigDecimal crMnl; // manual consumption rate, if any

  @Persistent
  private Date cOn; // inventory object created-on date (can be null for older objects in db.)
  @Persistent
  private Date reordT; // time when min/reorder-level was last updated
  @Persistent
  private Date maxT; // time when max was last updated
  @Persistent
  private Date crMnlT; // time when manual consumption rate was last updated
  @Persistent
  private Date prcT; // time when retailer price was last updated


  @Persistent
  private String ub; // last inventory updated user

  @Persistent
  private Date arcAt;
  @Persistent
  private String arcBy;
  @Persistent
  private BigDecimal pdos; // Predicted days of stock including order status

  public static String getModelDisplay(String modelType) {
    String display = "User specified replenishment - s,X";
    if (MODEL_SQ.equals(modelType)) {
      display = "System determined replenishment - s,Q";
    } else if (MODEL_MINMAX.equals(modelType)) {
      display = "System determined replenishment - s,S";
    } else if (MODEL_KANBAN.equals(modelType)) {
      display = "User specified replenishment - Kanban";
    }

    return display;
  }

  // Check if setting re-order level is allowed
  public static boolean isReorderAllowed(String invModel) {
    return (invModel == null || invModel.isEmpty() ||
        MODEL_KANBAN.equals(invModel));
  }

  @Override
  public BigDecimal getPredictedDaysOfStock() {
    return pdos;
  }

  @Override
  public void setPredictedDaysOfStock(BigDecimal pdos) {
    this.pdos = pdos;
  }

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
  public Long getMaterialId() {
    return mId;
  }

  @Override
  public void setMaterialId(Long materialId) {
    this.mId = materialId;
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
  public Long getShortId() {
    return sId;
  }

  @Override
  public void setShortId(Long sId) {
    this.sId = sId;
  }

  @Override
  public float getServiceLevel() {
    return NumberUtil.getFloatValue(svclvl);
  }

  @Override
  public void setServiceLevel(float serviceLevel) {
    this.svclvl = new Float(serviceLevel);
  }

  @Override
  public String getInventoryModel() {
    return imdl;
  }

  @Override
  public void setInventoryModel(String inventoryModel) {
    this.imdl = inventoryModel;
  }

  @Override
  public BigDecimal getLeadTime() {
    return BigUtil.getZeroIfNull(ldtm);
  }

  @Override
  public void setLeadTime(BigDecimal leadTime) {
    this.ldtm = leadTime;
  }

  @Override
  public BigDecimal getStock() {
    return BigUtil.getZeroIfNull(stk);
  }

  @Override
  public void setStock(BigDecimal stock) {
    this.stk = stock;
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
  public BigDecimal getInTransitStock() {
    return BigUtil.getZeroIfNull(tStk);
  }

  @Override
  public void setInTransitStock(BigDecimal stock) {
    this.tStk = stock;
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
  public BigDecimal getLeadTimeDemand() {
    return BigUtil.getZeroIfNull(ldtdmd);
  }

  @Override
  public void setLeadTimeDemand(BigDecimal leadTimeDemand) {
    this.ldtdmd = leadTimeDemand;
  }

  @Override
  public BigDecimal getRevPeriodDemand() {
    return BigUtil.getZeroIfNull(rvpdmd);
  }

  @Override
  public void setRevPeriodDemand(BigDecimal revPeriodDemand) {
    this.rvpdmd = revPeriodDemand;
  }

  @Override
  public BigDecimal getStdevRevPeriodDemand() {
    return BigUtil.getZeroIfNull(stdv);
  }

  @Override
  public void setStdevRevPeriodDemand(BigDecimal stdevRevPeriodDemand) {
    this.stdv = stdevRevPeriodDemand;
  }

  @Override
  public BigDecimal getSafetyStock() {
    return BigUtil.getZeroIfNull(sfstk);
  }

  @Override
  public void setSafetyStock(BigDecimal safetyStock) {
    this.sfstk = safetyStock;
  }

  @Override
  public boolean isSafetyStockDefined() {
    return sfstk == null;
  }

  @Override
  public Date getTimestamp() {
    return t;
  }

  @Override
  public void setTimestamp(Date timeStamp) {
    this.t = timeStamp;
  }

  @Override
  public Date getCreatedOn() {
    return cOn;
  }

  @Override
  public void setCreatedOn(Date createdOn) {
    cOn = createdOn;
    t = createdOn; // last stock updated time is set to creation time
  }

  @Override
  public Date getReorderLevelUpdatedTime() {
    return reordT;
  } // min. update time

  @Override
  public Date getMaxUpdatedTime() {
    return maxT;
  }

  @Override
  public Date getMnlConsumptionRateUpdatedTime() {
    return crMnlT;
  }

  @Override
  public Date getRetailerPriceUpdatedTime() {
    return prcT;
  }

  @Override
  public BigDecimal getOrderPeriodicity() {
    return BigUtil.getZeroIfNull(ordp);
  }

  @Override
  public void setOrderPeriodicity(BigDecimal orderPeriodicity) {
    this.ordp = orderPeriodicity;
  }

  @Override
  public String getKioskName() {
    return knm;
  }

  @Override
  public void setKioskName(String kioskName) {
    if (kioskName != null) {
      this.knm = kioskName.toLowerCase();
    } else {
      this.knm = null;
    }
  }

  @Override
  public String getMaterialName() {
    return mnm;
  }

  @Override
  public void setMaterialName(String materialName) {
    if (materialName != null) {
      this.mnm = materialName.toLowerCase();
    } else {
      this.mnm = null;
    }
  }

  @Override
  public String getBinaryValued() {
    return b;
  }

  @Override
  public void setBinaryValued(String binaryValued) {
    if (binaryValued != null) {
      this.b = binaryValued.toLowerCase();
    } else {
      this.b = "no";
    }
  }

  @Override
  public BigDecimal getReorderLevel() {
    return BigUtil.getZeroIfNull(reord);
  }

  @Override
  public void setReorderLevel(BigDecimal reordLevel) {
    if ((this.reord == null && BigUtil.notEqualsZero(reordLevel)) || (this.reord != null && BigUtil
        .notEquals(reordLevel, this.reord))) {
      this.reordT = new Date();
    }
    if (reordLevel == null) {
      this.reord = null;
    } else {
      this.reord = reordLevel.setScale(0, BigDecimal.ROUND_HALF_UP);
    }
  }

  @Override
  public BigDecimal getMaxStock() {
    return BigUtil.getZeroIfNull(max);
  }

  @Override
  public void setMaxStock(BigDecimal maxStock) {
    if ((this.max == null && BigUtil.notEqualsZero(maxStock)) || (this.max != null && BigUtil
        .notEquals(maxStock, this.max))) {
      this.maxT = new Date();
    }
    if (maxStock == null) {
      this.max = null;
    } else {
      this.max = maxStock.setScale(0, BigDecimal.ROUND_HALF_UP);
    }
  }

  @Override
  public BigDecimal getMinDuration() {
    return minDur;
  }

  @Override
  public void setMinDuration(BigDecimal minDur) {
    this.minDur = minDur != null ? minDur : BigDecimal.ZERO;
  }

  @Override
  public BigDecimal getMaxDuration() {
    return maxDur;
  }

  @Override
  public void setMaxDuration(BigDecimal maxDur) {
    this.maxDur = maxDur != null ? maxDur : BigDecimal.ZERO;
  }

  @Override
  public BigDecimal getNormalizedSafetyStock() {
    BigDecimal safeStock = getReorderLevel();
    if (BigUtil.equalsZero(safeStock)) {
      safeStock = getSafetyStock();
    }
    return safeStock;
  }

  @Override
  public boolean isStockUnsafe() {
    BigDecimal stock = getStock();
    BigDecimal safeStock = getNormalizedSafetyStock();
    return (BigUtil.greaterThanZero(safeStock) && BigUtil.greaterThanZero(stock) && BigUtil
        .lesserThanEquals(stock, safeStock));
  }

  @Override
  public boolean isStockExcess() {
    BigDecimal maxStock = getMaxStock();
    return (BigUtil.greaterThanZero(maxStock) && BigUtil.greaterThan(getStock(), maxStock));
  }

  @Override
  public BigDecimal getConsumptionRateManual() {
    return BigUtil.getZeroIfNull(crMnl);
  } // units are in OptimizerConfig

  @Override
  public void setConsumptionRateManual(BigDecimal consumptionRate) {
    if ((crMnl == null && BigUtil.notEqualsZero(consumptionRate)) || (crMnl != null && BigUtil
        .notEquals(consumptionRate, crMnl))) {
      crMnlT = new Date();
    }
    crMnl = consumptionRate;
  }

  @Override
  public BigDecimal getConsumptionRateDaily() {
    return BigUtil.getZeroIfNull(crD);
  }

  @Override
  public void setConsumptionRateDaily(BigDecimal crt) {
    this.crD = crt;
  }

  @Override
  public BigDecimal getConsumptionRateWeekly() {
    return BigUtil.getZeroIfNull(crD.multiply(Constants.WEEKLY_COMPUTATION));
  }

  @Override
  public BigDecimal getConsumptionRateMonthly() {
    return BigUtil.getZeroIfNull(crD.multiply(Constants.MONTHLY_COMPUTATION));
  }

  @Override
  public BigDecimal getEconomicOrderQuantity() {
    return BigUtil.getZeroIfNull(Q);
  }

  @Override
  public void setEconomicOrderQuantity(BigDecimal eoq) {
    this.Q = eoq;
  }

  @Override
  public Date getPSTimestamp() {
    return tPS;
  }

  @Override
  public void setPSTimestamp(Date tPS) {
    this.tPS = tPS;
  }

  @Override
  public Date getDQTimestamp() {
    return tDQ;
  }

  @Override
  public void setDQTimestamp(Date tDQ) {
    this.tDQ = tDQ;
  }

  @Override
  public String getOptMessage() {
    return omsg;
  }

  @Override
  public void setOptMessage(String msg) {
    this.omsg = msg;
  }

  @Override
  public List<String> getTags(String tagType) {
    if (TagUtil.TYPE_MATERIAL.equals(tagType)) {
      if (oldtgs == null) {
        oldtgs = TagUtil.getList(tgs);
      }
      return oldtgs;
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
      oldtgs = tags;
    } else if (TagUtil.TYPE_ENTITY.equals(tagType)) {
      oldktgs = tags;
    }
  }

  @Override
  public void setTgs(List<? extends ITag> tags, String tagType) {
    if (TagUtil.TYPE_MATERIAL.equals(tagType)) {
      if (this.tgs != null) {
        this.tgs.clear();
        if (tags != null) {
          this.tgs.addAll((Collection<Tag>) tags);
        }
      } else {
        this.tgs = (List<Tag>) tags;
      }
      this.oldtgs = null;
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

  @Override
  public BigDecimal getRetailerPrice() {
    return BigUtil.getZeroIfNull(prc);
  }

  @Override
  public void setRetailerPrice(BigDecimal price) {
    if ((prc == null && BigUtil.notEqualsZero(price)) || (prc != null && BigUtil
        .notEquals(price, prc))) {
      prcT = new Date();
    }
    prc = price;
  }

  @Override
  public BigDecimal getTax() {
    return BigUtil.getZeroIfNull(tx);
  }

  @Override
  public void setTax(BigDecimal tax) {
    tx = tax;
  }

  @Override
  public Long getLastStockEvent() {
    return lsev;
  }

  @Override
  public void setLastStockEvent(Long key) {
    lsev = key;
  }

  @Override
  public String getIdString() {
    return (kId + ":" + mId);
  }


  @Override
  public Float getTmin() {
    return tmin;
  }

  @Override
  public void setTmin(Float tmin) {
    this.tmin = tmin;
  }

  @Override
  public Float getTmax() {
    return tmax;
  }

  @Override
  public void setTmax(Float tmax) {
    this.tmax = tmax;
  }

  // Reset stock and related parameters
  @Override
  public void reset() {
    setStock(BigDecimal.ZERO);
    setLeadTimeDemand(BigDecimal.ZERO);
    setRevPeriodDemand(BigDecimal.ZERO);
    setStdevRevPeriodDemand(BigDecimal.ZERO);
    setSafetyStock(BigDecimal.ZERO);
    setOrderPeriodicity(BigDecimal.ZERO);
    setConsumptionRateDaily(BigDecimal.ZERO);
    setEconomicOrderQuantity(BigDecimal.ZERO);
    setAllocatedStock(BigDecimal.ZERO);
    setAvailableStock(BigDecimal.ZERO);
    setTimestamp(new Date());
    setPSTimestamp(null);
    setDQTimestamp(null);
  }


  // Get the type of inventory stock event such as stock-out, under/over stock; otherwise, return -1
  @Override
  public int getStockEvent() {
    int type = IEvent.NORMAL;
    BigDecimal stock = getStock();
    BigDecimal minStock = getNormalizedSafetyStock();
    BigDecimal maxStock = getMaxStock();
    if (BigUtil.equalsZero(stock)) {
      type = IEvent.STOCKOUT;
    } else if (BigUtil.greaterThanZero(minStock) && BigUtil.lesserThanEquals(stock, minStock)) {
      type = IEvent.UNDERSTOCK;
    } else if (BigUtil.greaterThanZero(maxStock) && BigUtil.greaterThan(stock, maxStock)) {
      type = IEvent.OVERSTOCK;
    }
    return type;
  }

  @Override
  public String getKeyString() {
    return String.valueOf(key);
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

  public String getUpdatedBy() {
    return ub;
  }

  public void setUpdatedBy(String user) {
    this.ub = user;
  }

  @Override
  public String getMCRUnits(Locale locale) {
    ResourceBundle messages = Resources.get().getBundle("Messages", locale);
    ResourceBundle jsMessages = Resources.get().getBundle("JSMessages", locale);
    Long domainId = this.getDomainId();
    DomainConfig dc = DomainConfig.getInstance(domainId);
    InventoryConfig ic = dc.getInventoryConfig();

    boolean allowManualConsumptionRates = (ic != null && ic.getManualCRFreq() != null);
    String
        manualCrUnits =
        (allowManualConsumptionRates ? InventoryConfig
            .getFrequencyDisplay(ic.getManualCRFreq(), false, locale) : null);
    if (manualCrUnits == null || manualCrUnits.isEmpty() || manualCrUnits
        .equalsIgnoreCase(messages.getString("days"))) {
      manualCrUnits =
          jsMessages.getString("daysofstock"); // Default the manual consumption rate units to Days.
    } else if (manualCrUnits.equalsIgnoreCase(messages.getString("weeks"))) {
      manualCrUnits = jsMessages.getString("weeksofstock");
    } else if (manualCrUnits.equalsIgnoreCase(messages.getString("months"))) {
      manualCrUnits = jsMessages.getString("monthsofstock");
    }
    return manualCrUnits;
  }
}

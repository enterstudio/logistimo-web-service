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

import com.logistimo.entities.service.EntitiesService;
import com.logistimo.entities.service.EntitiesServiceImpl;
import com.logistimo.inventory.entity.IInvntry;
import com.logistimo.materials.service.MaterialCatalogService;
import com.logistimo.materials.service.impl.MaterialCatalogServiceImpl;
import com.logistimo.tags.TagUtil;

import com.logistimo.services.ServiceException;
import com.logistimo.services.Services;
import com.logistimo.utils.BigUtil;
import com.logistimo.constants.Constants;
import com.logistimo.utils.NumberUtil;
import com.logistimo.logger.XLog;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;


public class InvntrySnapshot {
  // Inventory model types
  public static final String MODEL_NONE = "";
  public static final String MODEL_SQ = "sq";
  public static final String MODEL_MINMAX = "mm";
  public static final String MODEL_KANBAN = "kn";
  private static final XLog xLogger = XLog.getLog(InvntrySnapshot.class);
  private Long dId; // domain Id
  private Long mId; // material Id
  private Long kId; // kiosk Id

  public Long getdId() {
    return dId;
  }

  public void setdId(Long dId) {
    this.dId = dId;
  }

  public Long getmId() {
    return mId;
  }

  public void setmId(Long mId) {
    this.mId = mId;
  }

  public Long getkId() {
    return kId;
  }

  public void setkId(Long kId) {
    this.kId = kId;
  }

  public Float getSvclvl() {
    return svclvl;
  }

  public void setSvclvl(Float svclvl) {
    this.svclvl = svclvl;
  }

  public String getImdl() {
    return imdl;
  }

  public void setImdl(String imdl) {
    this.imdl = imdl;
  }

  public BigDecimal getLdtm() {
    return ldtm;
  }

  public void setLdtm(BigDecimal ldtm) {
    this.ldtm = ldtm;
  }

  public BigDecimal getStk() {
    return stk;
  }

  public void setStk(BigDecimal stk) {
    this.stk = stk;
  }

  public BigDecimal getLdtdmd() {
    return ldtdmd;
  }

  public void setLdtdmd(BigDecimal ldtdmd) {
    this.ldtdmd = ldtdmd;
  }

  public BigDecimal getRvpdmd() {
    return rvpdmd;
  }

  public void setRvpdmd(BigDecimal rvpdmd) {
    this.rvpdmd = rvpdmd;
  }

  public BigDecimal getStdv() {
    return stdv;
  }

  public void setStdv(BigDecimal stdv) {
    this.stdv = stdv;
  }

  public BigDecimal getOrdp() {
    return ordp;
  }

  public void setOrdp(BigDecimal ordp) {
    this.ordp = ordp;
  }

  public BigDecimal getSfstk() {
    return sfstk;
  }

  public void setSfstk(BigDecimal sfstk) {
    this.sfstk = sfstk;
  }

  public Date getT() {
    return t;
  }

  public void setT(Date t) {
    this.t = t;
  }

  public String getKnm() {
    return knm;
  }

  public void setKnm(String knm) {
    this.knm = knm;
  }

  public String getMnm() {
    return mnm;
  }

  public void setMnm(String mnm) {
    this.mnm = mnm;
  }

  public BigDecimal getReord() {
    return reord;
  }

  public void setReord(BigDecimal reord) {
    this.reord = reord;
  }

  public BigDecimal getMax() {
    return max;
  }

  public void setMax(BigDecimal max) {
    this.max = max;
  }

  public BigDecimal getCrD() {
    return crD;
  }

  public void setCrD(BigDecimal crD) {
    this.crD = crD;
  }

  public BigDecimal getCrW() {
    return crW;
  }

  public void setCrW(BigDecimal crW) {
    this.crW = crW;
  }

  public BigDecimal getCrM() {
    return crM;
  }

  public void setCrM(BigDecimal crM) {
    this.crM = crM;
  }

  public BigDecimal getQ() {
    return Q;
  }

  public void setQ(BigDecimal q) {
    Q = q;
  }

  public Date gettPS() {
    return tPS;
  }

  public void settPS(Date tPS) {
    this.tPS = tPS;
  }

  public Date gettDQ() {
    return tDQ;
  }

  public void settDQ(Date tDQ) {
    this.tDQ = tDQ;
  }

  public String getOmsg() {
    return omsg;
  }

  public void setOmsg(String omsg) {
    this.omsg = omsg;
  }

  public List<String> getTgs() {
    return tgs;
  }

  public void setTgs(List<String> tgs) {
    this.tgs = tgs;
  }

  public List<String> getKtgs() {
    return ktgs;
  }

  public void setKtgs(List<String> ktgs) {
    this.ktgs = ktgs;
  }

  public BigDecimal getPrc() {
    return prc;
  }

  public void setPrc(BigDecimal prc) {
    this.prc = prc;
  }

  public BigDecimal getTx() {
    return tx;
  }

  public void setTx(BigDecimal tx) {
    this.tx = tx;
  }

  public BigDecimal getCrMnl() {
    return crMnl;
  }

  public void setCrMnl(BigDecimal crMnl) {
    this.crMnl = crMnl;
  }

  private Float svclvl = 85F; // service level
  private String imdl = MODEL_SQ; // inventory model
  private BigDecimal ldtm = new BigDecimal(Constants.LEADTIME_DEFAULT); // lead time in days
  private BigDecimal stk = BigDecimal.ZERO; // current stock
  private BigDecimal ldtdmd = BigDecimal.ZERO; // lead time demand
  private BigDecimal rvpdmd = BigDecimal.ZERO; // forecasted demand (review period demand)
  private BigDecimal stdv = BigDecimal.ZERO; // std. deviation of rev. period demand
  private BigDecimal ordp = BigDecimal.ZERO; // days; order periodicity
  private BigDecimal sfstk = BigDecimal.ZERO; // safety stock
  private Date t; // creation or last stock update timestamp
  private String knm; // kiosk name - redundantly stored here to enable sorting Inventory by kiosk
  private String
      mnm;
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
  private List<String>
      tgs;
  // list of material tags, if present (used mainly for queries involving kioskId and tags)
  private List<String> ktgs; // list of kiosk tags (for queries)
  private BigDecimal prc; // retailer price
  private BigDecimal tx; // tax rate in %
  //private Long lsev; // key to last stock event (such as stockout, understock or overstock)
  private BigDecimal crMnl; // manual consumption rate, if any
  // a set of items with serial nos. (size should ideally match the stock quantity, if all serial nos. are entered)

  public InvntrySnapshot(IInvntry invntry) {
    this.dId = invntry.getDomainId();
    this.mId = invntry.getMaterialId();
    this.kId = invntry.getKioskId();
    this.svclvl = invntry.getServiceLevel();
    this.imdl = invntry.getInventoryModel();
    this.ldtm = invntry.getLeadTime();
    this.stk = invntry.getStock();
    this.ldtdmd = invntry.getLeadTimeDemand();
    this.rvpdmd = invntry.getRevPeriodDemand();
    this.stdv = invntry.getStdevRevPeriodDemand();
    this.ordp = invntry.getOrderPeriodicity();
    this.sfstk = invntry.getSafetyStock();
    this.t = invntry.getTimestamp();
    EntitiesService es = null;
    try {
      es = Services.getService(EntitiesServiceImpl.class);
      this.knm = es.getKiosk(invntry.getKioskId()).getName();
    } catch (ServiceException e) {
      e.printStackTrace();
    }
    MaterialCatalogService mcs = null;
    try {
      mcs = Services.getService(MaterialCatalogServiceImpl.class);
      this.mnm = mcs.getMaterial(invntry.getMaterialId()).getName();
    } catch (ServiceException e) {
      e.printStackTrace();
    }
    this.reord = invntry.getReorderLevel();
    this.max = invntry.getMaxStock();
    this.crD = invntry.getConsumptionRateDaily();
    this.crW = invntry.getConsumptionRateWeekly();
    this.crM = invntry.getConsumptionRateMonthly();
    this.Q = invntry.getEconomicOrderQuantity();
    this.tPS = invntry.getPSTimestamp();
    this.tDQ = invntry.getDQTimestamp();

    this.omsg = invntry.getOptMessage();
    this.tgs = invntry.getTags(TagUtil.TYPE_MATERIAL);
    this.ktgs = invntry.getTags(TagUtil.TYPE_ENTITY);
    this.prc = invntry.getRetailerPrice();
    this.tx = invntry.getTax();
    //this.lsev = invntry.getLastStockEvent();
    this.crMnl = invntry.getConsumptionRateManual();

  }

  public void setTimestamp(Date timeStamp) {
    this.t = timeStamp;
  }

  public BigDecimal getStock() {
    return BigUtil.getZeroIfNull(stk);
  }

  public void setStock(BigDecimal stock) {
    this.stk = stock;
  }

  public BigDecimal getReorderLevel() {
    return BigUtil.getZeroIfNull(reord);
  }

  public BigDecimal getMaxStock() {
    return BigUtil.getZeroIfNull(max);
  }

  public BigDecimal getConsumptionRateDaily() {
    return BigUtil.getZeroIfNull(crD);
  }

  public BigDecimal getConsumptionRateWeekly() {
    return BigUtil.getZeroIfNull(crW);
  }

  public BigDecimal getConsumptionRateMonthly() {
    return BigUtil.getZeroIfNull(crM);
  }

  public BigDecimal getRevPeriodDemand() {
    return BigUtil.getZeroIfNull(rvpdmd);
  }

  public BigDecimal getSafetyStock() {
    return BigUtil.getZeroIfNull(sfstk);
  }

  public BigDecimal getEconomicOrderQuantity() {
    return BigUtil.getZeroIfNull(Q);
  }

  public float getServiceLevel() {
    return NumberUtil.getFloatValue(svclvl);
  }


}

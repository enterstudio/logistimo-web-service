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

package com.logistimo.reports.models;

import com.google.gson.Gson;
import com.google.gson.annotations.Expose;

import com.logistimo.config.models.DomainConfig;
import com.logistimo.domains.entity.IDomain;
import com.logistimo.domains.service.DomainsService;
import com.logistimo.domains.service.impl.DomainsServiceImpl;
import com.logistimo.reports.entity.slices.IMonthSlice;

import com.logistimo.services.Services;

import com.logistimo.utils.Counter;
import com.logistimo.models.ICounter;
import com.logistimo.logger.XLog;

import java.math.BigDecimal;

public class DomainUsageStats {
  // Logger
  private static final XLog xLogger = XLog.getLog(DomainUsageStats.class);

  @Expose
  private Long dId = null; // domain id
  @Expose
  private String dName = null; // domain name
  @Expose
  private int uc = 0; // User count
  @Expose
  private int lnc = 0; // login count/active users count
  // @Expose
  // private int truc = 0; // transactive user count
  @Expose
  private int ec = 0; // Entity count
  // @Expose
  // private int aec = 0; // active entities count i.e. entities that have entered at least one transaction
  @Expose
  private int tc = 0; // transaction count
  @Expose
  private int oc = 0; // Order count
  // @Expose
  // private int evc = 0; // Event count
  @Expose
  private BigDecimal brv = BigDecimal.ZERO; // Booked revenue
  @Expose
  private int mc = 0; // Material count
  @Expose
  private int ctc = 0; // Cumulative transaction count
  @Expose
  private int coc = 0; // Cumulative order count
  // @Expose
  // private int cevc = 0; // Cumulative event count
  @Expose
  private BigDecimal cbrv = BigDecimal.ZERO; // Cumulative booked revenue
  @Expose
  private int clnc = 0; // Cumulative login count
  @Expose
  private long t = 0; // timestamp in milliseconds
  @Expose
  private String cur = null;

  public static XLog getxLogger() {
    return xLogger;
  }

  public Long getdId() {
    return dId;
  }

  public void setdId(Long dId) {
    this.dId = dId;
  }

  public String getdName() {
    return dName;
  }

  public void setdName(String dName) {
    this.dName = dName;
  }

  public int getUc() {
    return uc;
  }

  public void setUc(int uc) {
    this.uc = uc;
  }

  public int getLnc() {
    return lnc;
  }

  public void setLnc(int lnc) {
    this.lnc = lnc;
  }

  public int getEc() {
    return ec;
  }

  public void setEc(int ec) {
    this.ec = ec;
  }

  public int getTc() {
    return tc;
  }

  public void setTc(int tc) {
    this.tc = tc;
  }

  public int getOc() {
    return oc;
  }

  public void setOc(int oc) {
    this.oc = oc;
  }

  public BigDecimal getBrv() {
    return brv;
  }

  public void setBrv(BigDecimal brv) {
    this.brv = brv;
  }

  public int getMc() {
    return mc;
  }

  public void setMc(int mc) {
    this.mc = mc;
  }

  public int getCtc() {
    return ctc;
  }

  public void setCtc(int ctc) {
    this.ctc = ctc;
  }

  public int getCoc() {
    return coc;
  }

  public void setCoc(int coc) {
    this.coc = coc;
  }

  public BigDecimal getCbrv() {
    return cbrv;
  }

  public void setCbrv(BigDecimal cbrv) {
    this.cbrv = cbrv;
  }

  public int getClnc() {
    return clnc;
  }

  public void setClnc(int clnc) {
    this.clnc = clnc;
  }

  public long getT() {
    return t;
  }

  public void setT(long t) {
    this.t = t;
  }

  public String getCur() {
    return cur;
  }

  public void setCur(String cur) {
    this.cur = cur;
  }

  public DomainUsageStats(IMonthSlice monthSlice) {
    if (monthSlice != null) {
      this.dId = monthSlice.getDomainId();
      this.lnc = monthSlice.getLoginCounts();
      this.clnc = monthSlice.getCumulativeLoginCounts();
      this.tc = monthSlice.getTotalCount();
      this.ctc = monthSlice.getCumulativeTotalCount();
      this.oc = monthSlice.getOrderCount();
      this.coc = monthSlice.getCumulativeOrderCount();
      // Add cevc
      this.brv = monthSlice.getRevenueBooked();
      this.cbrv = monthSlice.getCumulativeRevenueBooked();
      // Fill the user, entity and material counts.
      ICounter counter = Counter.getUserCounter(dId);
      this.uc = counter.getCount();
      counter = Counter.getKioskCounter(dId);
      this.ec = counter.getCount();
      counter =
          Counter.getMaterialCounter(dId,
              null); // Since material tag is optional, null tag will return all materials
      this.mc = counter.getCount();
      this.t = monthSlice.getDate().getTime();
      try {
        // Set the domain name
        DomainsService ds = Services.getService(DomainsServiceImpl.class);
        IDomain d = ds.getDomain(this.dId);
        if (d != null) {
          this.dName = d.getName();
        }

      } catch (Exception e) {
        xLogger.severe("{0} while getting domain name for domain id {1}. Message: {2}",
            e.getClass().getName(), dId, e.getMessage());
      }

      DomainConfig dc = DomainConfig.getInstance(this.dId);
      this.cur = dc.getCurrency();
    }
  }

  public String toJSONString() {
    Gson gson = new Gson();
    return gson.toJson(this);

  }

  @Override
  public String toString() {
    String strOutput = "dId: " + dId + ", dName: " + dName + ", tc: " + tc + ", oc: " + oc +
        ", lnc: " + lnc +
        ", brv: " + brv + ", ctc: " + ctc +
        ", coc: " + coc + ", clnc: " + clnc +
        ", cbrv: " + cbrv + ", uc: " + uc +
        ", ec: " + ec + ", mc: " + mc +
        ", t: " + t + ", cur: " + cur;
    return strOutput;
  }
}

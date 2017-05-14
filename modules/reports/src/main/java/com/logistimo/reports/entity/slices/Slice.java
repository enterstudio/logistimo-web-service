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
package com.logistimo.reports.entity.slices;

import com.logistimo.dao.JDOUtils;

import org.json.JSONException;
import org.json.JSONObject;

import com.logistimo.proto.JsonTagsZ;

import com.logistimo.utils.BigUtil;
import com.logistimo.utils.LocalDateUtil;
import com.logistimo.utils.NumberUtil;
import com.logistimo.logger.XLog;

import java.math.BigDecimal;
import java.util.Date;
import java.util.Hashtable;
import java.util.Locale;

import javax.jdo.annotations.Cacheable;
import javax.jdo.annotations.Extension;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.Inheritance;
import javax.jdo.annotations.InheritanceStrategy;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;


/**
 * Represents a time slice of aggregated data on a specific dimension
 *
 * @author Arun
 */

@PersistenceCapable(identityType = IdentityType.APPLICATION, detachable = "true")
@Inheritance(strategy = InheritanceStrategy.SUBCLASS_TABLE)
@Cacheable("false")
public class Slice implements ISlice {
  private static final XLog xLogger = XLog.getLog(Slice.class);

  @PrimaryKey
  @Persistent
  protected String k; // key
  @Persistent
  protected Date d; // date
  @Persistent
  protected String dt; // dimension type such as domain, district, poolgroup, and so on
  @Persistent
  protected String dv; // dimension value
  @Persistent
  protected Long dId; // domain Id
  @Persistent
  protected String oty; // object type
  @Persistent
  protected String oId; // object Id
  // Cardinalities
  @Persistent
  @Extension(vendorName = "datanucleus", key = "gae.unindexed", value = "true")
  protected Integer ic = new Integer(0); // issue counts
  @Persistent
  @Extension(vendorName = "datanucleus", key = "gae.unindexed", value = "true")
  protected Integer rc = new Integer(0); // receipt counts
  @Persistent
  @Extension(vendorName = "datanucleus", key = "gae.unindexed", value = "true")
  protected Integer sc = new Integer(0); // stock-count counts
  @Persistent
  @Extension(vendorName = "datanucleus", key = "gae.unindexed", value = "true")
  protected Integer wc = new Integer(0); // wastage counts
  @Persistent
  @Extension(vendorName = "datanucleus", key = "gae.unindexed", value = "true")
  protected Integer trc = new Integer(0); // transfer counts
  @Persistent
  protected Integer
      tc =
      new Integer(0);
  // Total number of all transactions (present to facilitate sorting on total)
  @Persistent
  @Extension(vendorName = "datanucleus", key = "gae.unindexed", value = "true")
  protected Integer dc = new Integer(0); // demand counts for a material/inventory
  @Persistent
  @Extension(vendorName = "datanucleus", key = "gae.unindexed", value = "true")
  protected Integer oc = new Integer(0); // order count for a domain/entity/user
  @Persistent
  @Extension(vendorName = "datanucleus", key = "gae.unindexed", value = "true")
  protected Integer rtc = new Integer(0); // return counts
  // Quantities
  @Persistent
  @Extension(vendorName = "datanucleus", key = "gae.unindexed", value = "true")
  protected BigDecimal iq = BigDecimal.ZERO; // issue quantity
  @Persistent
  @Extension(vendorName = "datanucleus", key = "gae.unindexed", value = "true")
  protected BigDecimal rq = BigDecimal.ZERO; // receipt quantity
  @Persistent
  //@Extension(vendorName="datanucleus", key="gae.unindexed", value="true")
  protected BigDecimal sq = BigDecimal.ZERO; // stock quantity/level
  @Persistent
  @Extension(vendorName = "datanucleus", key = "gae.unindexed", value = "true")
  protected BigDecimal sdf = BigDecimal.ZERO; // stock quantity difference due to stock-count
  @Persistent
  @Extension(vendorName = "datanucleus", key = "gae.unindexed", value = "true")
  protected BigDecimal wq = BigDecimal.ZERO; // wastage quantity
  @Persistent
  @Extension(vendorName = "datanucleus", key = "gae.unindexed", value = "true")
  protected BigDecimal trq = BigDecimal.ZERO; // transfer quantity (for a given material)
  @Persistent
  @Extension(vendorName = "datanucleus", key = "gae.unindexed", value = "true")
  protected BigDecimal rtq = BigDecimal.ZERO; // return quantity
  @Persistent
  @Extension(vendorName = "datanucleus", key = "gae.unindexed", value = "true")
  protected BigDecimal dq = BigDecimal.ZERO; // demand quantity for material/inventory
  @Persistent
  @Extension(vendorName = "datanucleus", key = "gae.unindexed", value = "true")
  protected Integer os = new Integer(0); // order size for domain/entity/user
  // Revenue
  @Persistent
  @Extension(vendorName = "datanucleus", key = "gae.unindexed", value = "true")
  protected BigDecimal brv = BigDecimal.ZERO; // booked revenue (from non-cancelled orders)
  @Persistent
  @Extension(vendorName = "datanucleus", key = "gae.unindexed", value = "true")
  protected BigDecimal rrv = BigDecimal.ZERO; // realizable revenue (from shipped/fulfilled orders)
  // User parameters
  @Persistent
  @Extension(vendorName = "datanucleus", key = "gae.unindexed", value = "true")
  protected Integer lnc = new Integer(0); // login count
  // Cumulative counts
  @Persistent
  protected Integer
      ctc =
      new Integer(0);
  // Cumulative total number of all transactions (global total until this time)
  @Persistent
  @Extension(vendorName = "datanucleus", key = "gae.unindexed", value = "true")
  protected Integer cic = new Integer(0); // cumulative issues
  @Persistent
  @Extension(vendorName = "datanucleus", key = "gae.unindexed", value = "true")
  protected Integer crc = new Integer(0); // cumulative receipts
  @Persistent
  @Extension(vendorName = "datanucleus", key = "gae.unindexed", value = "true")
  protected Integer csc = new Integer(0); // cumulative stock counts
  @Persistent
  @Extension(vendorName = "datanucleus", key = "gae.unindexed", value = "true")
  protected Integer cwc = new Integer(0); // cumulative wastage
  @Persistent
  @Extension(vendorName = "datanucleus", key = "gae.unindexed", value = "true")
  protected Integer ctrc = new Integer(0); // cumulative transfers
  @Persistent
  @Extension(vendorName = "datanucleus", key = "gae.unindexed", value = "true")
  protected Integer crtc = new Integer(0); // cumulative returns
  @Persistent
  @Extension(vendorName = "datanucleus", key = "gae.unindexed", value = "true")
  protected Integer cdc = new Integer(0); // cumulative demand
  @Persistent
  @Extension(vendorName = "datanucleus", key = "gae.unindexed", value = "true")
  protected Integer coc = new Integer(0); // cumulative orders
  @Persistent
  @Extension(vendorName = "datanucleus", key = "gae.unindexed", value = "true")
  protected Integer cos = new Integer(0); // cumulative order size
  @Persistent
  @Extension(vendorName = "datanucleus", key = "gae.unindexed", value = "true")
  protected Integer clnc = new Integer(0); // cumulative login counts
  @Persistent
  @Extension(vendorName = "datanucleus", key = "gae.unindexed", value = "true")
  protected BigDecimal
      cbrv =
      BigDecimal.ZERO;
  // cumulative booked revenue (from non-cancelled orders)
  @Persistent
  @Extension(vendorName = "datanucleus", key = "gae.unindexed", value = "true")
  protected BigDecimal
      crrv =
      BigDecimal.ZERO;
  // cumulative realized revenue (from non-cancelled orders)
  // Event Metrics
  @Persistent
  protected Integer soec = new Integer(0); // Stock Out event count
  @Persistent
  protected Integer lmec = new Integer(0); // Less than min event count
  @Persistent
  protected Integer gmec = new Integer(0); // Greater than max event count
  @Persistent
  protected Integer tasec = new Integer(0); // Total abnormal stock event count
  @Persistent
  protected Integer nlec = new Integer(0); // Not logged in event count
  // Cumulative Event metrics
  @Persistent
  protected Integer csoec = new Integer(0); // Cumulative Stock Out event count
  @Persistent
  protected Integer clmec = new Integer(0); // Cumulative Less than min event count
  @Persistent
  protected Integer cgmec = new Integer(0); // Cumulative Greater than max event count
  @Persistent
  protected Integer cnlec = new Integer(0); // Cumulative Not logged in event count
  @Persistent
  protected Integer ctasec = new Integer(0); // Cumulative Total abnormal stock event count
  // Transactive users
  @Persistent
  protected Integer
      tuc =
      new Integer(0);
  // Transactive user count, (Users who have had atleast one transaction or order)
  // Active entities
  @Persistent
  protected Integer
      tec =
      new Integer(0);
  // Transactive entity count, (Entities with at least one transaction)
  // Order metrics
  @Persistent
  protected Integer
      pnoc =
      new Integer(0);
  // Pending Order count - pn for pending oc for order count. Two letters of the order status is used because there is another metric called paid order count.
  @Persistent
  protected Integer cfoc = new Integer(0); // Confirmed Order count
  @Persistent
  protected Integer shoc = new Integer(0); // Shipped Order count
  @Persistent
  protected Integer floc = new Integer(0); // Fulfilled Order count
  @Persistent
  protected Integer cnoc = new Integer(0); // Cancelled Order count
  @Persistent
  protected Integer poc = new Integer(0); // Paid Order count - Orders with payment
  // Cumulative Order metrics
  @Persistent
  protected Integer ccfoc = new Integer(0); // Cumulative Confirmed Order count
  @Persistent
  protected Integer cshoc = new Integer(0); // Cumulative Shipped Order count
  @Persistent
  protected Integer cfloc = new Integer(0); // Cumulative Fulfilled Order count
  @Persistent
  protected Integer ccnoc = new Integer(0); // Cumulative Cancelled Order count
  @Persistent
  protected Integer cpoc = new Integer(0); // Cumulative Paid Order count
  @Persistent
  protected Integer dsc = new Integer(0); // Demand Shipped count
  @Persistent
  protected BigDecimal dsq = BigDecimal.ZERO; // Demand Shipped quantity
  // Message Log metrics
  @Persistent
  protected Integer smsc = new Integer(0); // SMS count
  @Persistent
  protected Integer emlc = new Integer(0); // Email count
  // Cumulative Message Log metrics
  @Persistent
  protected Integer csmsc = new Integer(0); // SMS count
  @Persistent
  protected Integer cemlc = new Integer(0); // Email count

  @Override
  public String getKey() {
    return k;
  }

  @Override
  public Date getDate() {
    return d;
  }

  @Override
  public void setDate(Date d, String tz, String freq) {
    this.d = d;
  }

  @Override
  public String getDimensionType() {
    return dt;
  }

  @Override
  public String getDimensionValue() {
    return dv;
  }

  // Set dimension, given dimension type (e.g. domain, district) and dimension value (e.g. domainId, district name)
  @Override
  public void setDimension(String dmType, String dmValue) {
    this.dt = dmType;
    this.dv = dmValue;
  }

  @Override
  public String getObjectType() {
    return oty;
  }

  @Override
  public String getObjectId() {
    return oId;
  }

  @Override
  public void setObject(String objectType, String objectId) {
    oty = objectType;
    oId = objectId;
  }

  @Override
  public Long getDomainId() {
    return dId;
  }

  @Override
  public void setDomainId(Long dId) {
    this.dId = dId;
  }

  @Override
  public int getIssueCount() {
    return NumberUtil.getIntegerValue(ic);
  }

  @Override
  public void setIssueCount(int i) {
    ic = new Integer(i);
  }

  @Override
  public int getReceiptCount() {
    return NumberUtil.getIntegerValue(rc);
  }

  @Override
  public void setReceiptCount(int r) {
    rc = new Integer(r);
  }

  @Override
  public int getStockcountCount() {
    return NumberUtil.getIntegerValue(sc);
  }

  @Override
  public void setStockcountCount(int s) {
    sc = new Integer(s);
  }

  @Override
  public int getWastageCount() {
    return NumberUtil.getIntegerValue(wc);
  }

  @Override
  public void setWastageCount(int wc) {
    this.wc = new Integer(wc);
  }

  @Override
  public int getTransferCount() {
    return NumberUtil.getIntegerValue(trc);
  }

  @Override
  public void setTransferCount(int trc) {
    this.trc = new Integer(trc);
  }

  @Override
  public int getTotalCount() {
    return NumberUtil.getIntegerValue(tc);
  }

  @Override
  public int getDemandCount() {
    return NumberUtil.getIntegerValue(dc);
  }

  @Override
  public void setDemandCount(int dc) {
    this.dc = new Integer(dc);
  }

  @Override
  public int getReturnCount() {
    return NumberUtil.getIntegerValue(rtc);
  }

  @Override
  public void setReturnCount(int rtc) {
    this.rtc = new Integer(rtc);
  }

  @Override
  public int getOrderCount() {
    return NumberUtil.getIntegerValue(oc);
  }

  @Override
  public void setOrderCount(int oc) {
    this.oc = new Integer(oc);
  }

  @Override
  public BigDecimal getIssueQuantity() {
    return BigUtil.getZeroIfNull(iq);
  }

  @Override
  public void setIssueQuantity(BigDecimal i) {
    iq = i;
  }

  @Override
  public BigDecimal getReceiptQuantity() {
    return BigUtil.getZeroIfNull(rq);
  }

  @Override
  public void setReceiptQuantity(BigDecimal r) {
    rq = r;
  }

  @Override
  public BigDecimal getDemandQuantity() {
    return BigUtil.getZeroIfNull(dq);
  }

  @Override
  public void setDemandQuantity(BigDecimal dq) {
    this.dq = dq;
  }

  @Override
  public BigDecimal getStockQuantity() {
    return BigUtil.getZeroIfNull(sq);
  }

  @Override
  public void setStockQuantity(BigDecimal s) {
    sq = s;
  }

  @Override
  public BigDecimal getStockDifference() {
    return BigUtil.getZeroIfNull(sdf);
  }

  @Override
  public void setStockDifference(BigDecimal sdf) {
    this.sdf = sdf;
  }

  @Override
  public BigDecimal getWastageQuantity() {
    return BigUtil.getZeroIfNull(wq);
  }

  @Override
  public void setWastageQuantity(BigDecimal wq) {
    this.wq = wq;
  }

  @Override
  public BigDecimal getTransferQuantity() {
    return BigUtil.getZeroIfNull(trq);
  }

  @Override
  public void setTransferQuantity(BigDecimal trq) {
    this.trq = trq;
  }

  @Override
  public BigDecimal getReturnQuantity() {
    return BigUtil.getZeroIfNull(rtq);
  }

  @Override
  public void setReturnQuantity(BigDecimal rtq) {
    this.rtq = rtq;
  }

  @Override
  public int getOrderSize() {
    return NumberUtil.getIntegerValue(os);
  }

  @Override
  public void setOrderSize(int os) {
    this.os = new Integer(os);
  }

  @Override
  public BigDecimal getRevenueBooked() {
    return BigUtil.getZeroIfNull(brv);
  }

  @Override
  public void setRevenueBooked(BigDecimal rvn) {
    brv = rvn;
  }

  @Override
  public BigDecimal getRevenueRealizable() {
    return BigUtil.getZeroIfNull(rrv);
  }

  @Override
  public void setRevenueRealizable(BigDecimal rvn) {
    rrv = rvn;
  }

  @Override
  public int getLoginCounts() {
    return NumberUtil.getIntegerValue(lnc);
  }

  @Override
  public void setLoginCounts(int loginCounts) {
    this.lnc = new Integer(loginCounts);
  }

  // Cumulative counts
  @Override
  public int getCumulativeIssueCount() {
    return NumberUtil.getIntegerValue(cic);
  }

  @Override
  public int getCumulativeReceiptCount() {
    return NumberUtil.getIntegerValue(crc);
  }

  @Override
  public int getCumulativeStockcountCount() {
    return NumberUtil.getIntegerValue(csc);
  }

  @Override
  public int getCumulativeWastageCount() {
    return NumberUtil.getIntegerValue(cwc);
  }

  @Override
  public int getCumulativeTransferCount() {
    return NumberUtil.getIntegerValue(ctrc);
  }

  @Override
  public int getCumulativeTotalCount() {
    return NumberUtil.getIntegerValue(ctc);
  }

  @Override
  public int getCumulativeDemandCount() {
    return NumberUtil.getIntegerValue(cdc);
  }

  @Override
  public int getCumulativeReturnCount() {
    return NumberUtil.getIntegerValue(crtc);
  }

  @Override
  public int getCumulativeOrderCount() {
    return NumberUtil.getIntegerValue(coc);
  }

  @Override
  public int getCumulativeOrderSize() {
    return NumberUtil.getIntegerValue(cos);
  }

  @Override
  public int getCumulativeLoginCounts() {
    return NumberUtil.getIntegerValue(clnc);
  }

  @Override
  public void setCumulativeLoginCounts(int cumulativeNumLogins) {
    clnc = new Integer(cumulativeNumLogins);
  }

  @Override
  public BigDecimal getCumulativeRevenueBooked() {
    return BigUtil.getZeroIfNull(cbrv);
  }

  @Override
  public BigDecimal getCumulativeRevenueRealized() {
    return BigUtil.getZeroIfNull(crrv);
  }

  // Event metrics
  @Override
  public int getStockoutEventCounts() {
    return NumberUtil.getIntegerValue(soec);
  }

  @Override
  public void setStockoutEventCounts(int stockoutEventCounts) {
    this.soec = new Integer(stockoutEventCounts);
  }

  @Override
  public int getLessThanMinEventCounts() {
    return NumberUtil.getIntegerValue(lmec);
  }

  @Override
  public void setLessThanMinEventCounts(int lessThanMinEventCounts) {
    this.lmec = new Integer(lessThanMinEventCounts);
  }

  @Override
  public int getGreaterThanMaxEventCounts() {
    return NumberUtil.getIntegerValue(gmec);
  }

  @Override
  public void setGreaterThanMaxEventCounts(int greaterThanMaxEventCounts) {
    this.gmec = new Integer(greaterThanMaxEventCounts);
  }

  @Override
  public int getNotLoggedInEventCounts() {
    return NumberUtil.getIntegerValue(nlec);
  }

  @Override
  public void setNotLoggedInEventCounts(int notLoggedInEventCounts) {
    this.nlec = new Integer(notLoggedInEventCounts);
  }

  @Override
  public int getTotalAbnormalStockEventCounts() {
    return NumberUtil.getIntegerValue(tasec);
  }

  @Override
  public void setTotalAbnormalStockEventCounts(int totalAbnormalStockEventCounts) {
    this.tasec = new Integer(totalAbnormalStockEventCounts);
  }

  // Cumulative Event Metrics
  @Override
  public int getCumulativeStockoutEventCounts() {
    return NumberUtil.getIntegerValue(csoec);
  }

  @Override
  public int getCumulativeLessThanMinEventCounts() {
    return NumberUtil.getIntegerValue(clmec);
  }

  @Override
  public int getCumulativeGreaterThanMaxEventCounts() {
    return NumberUtil.getIntegerValue(cgmec);
  }

  @Override
  public int getCumulativeNotLoggedInEventCounts() {
    return NumberUtil.getIntegerValue(cnlec);
  }

  @Override
  public int getCumulativeTotalAbnormalStockEventCounts() {
    return NumberUtil.getIntegerValue(ctasec);
  }

  // Transactive users
  @Override
  public int getTransactiveUserCounts() {
    return NumberUtil.getIntegerValue(tuc);
  }

  @Override
  public void setTransactiveUserCounts(int transactiveUserCounts) {
    this.tuc = new Integer(transactiveUserCounts);
  }

  // Active entities
  @Override
  public int getTransactiveEntityCounts() {
    return NumberUtil.getIntegerValue(tec);
  }

  @Override
  public void setTransactiveEntitiyCounts(int transactiveEntityCounts) {
    this.tec = new Integer(transactiveEntityCounts);
  }

  // Order metrics
  @Override
  public int getPendingOrderCount() {
    return NumberUtil.getIntegerValue(pnoc);
  }

  @Override
  public void setPendingOrderCount(int pendingOrderCount) {
    this.pnoc = new Integer(pendingOrderCount);
  }

  @Override
  public int getConfirmedOrderCount() {
    return NumberUtil.getIntegerValue(cfoc);
  }

  @Override
  public void setConfirmedOrderCount(int confirmedOrderCount) {
    this.cfoc = new Integer(confirmedOrderCount);
  }

  @Override
  public int getShippedOrderCount() {
    return NumberUtil.getIntegerValue(shoc);
  }

  @Override
  public void setShippedOrderCount(int shippedOrderCount) {
    this.shoc = new Integer(shippedOrderCount);
  }

  @Override
  public int getFulfilledOrderCount() {
    return NumberUtil.getIntegerValue(floc);
  }

  @Override
  public void setFulfilledOrderCount(int fulfilledOrderCount) {
    this.floc = new Integer(fulfilledOrderCount);
  }

  @Override
  public int getCancelledOrderCount() {
    return NumberUtil.getIntegerValue(cnoc);
  }

  @Override
  public void setCancelledOrderCount(int cancelledOrderCount) {
    this.cnoc = new Integer(cancelledOrderCount);
  }

  @Override
  public int getPaidOrderCount() {
    return NumberUtil.getIntegerValue(poc);
  }

  @Override
  public void setPaidOrderCount(int paidOrderCount) {
    this.poc = new Integer(paidOrderCount);
  }

  // Cumulative Order metrics
  @Override
  public int getCumulativeConfirmedOrderCount() {
    return NumberUtil.getIntegerValue(ccfoc);
  }

  @Override
  public int getCumulativeShippedOrderCount() {
    return NumberUtil.getIntegerValue(cshoc);
  }

  @Override
  public int getCumulativeFulfilledOrderCount() {
    return NumberUtil.getIntegerValue(cfloc);
  }

  @Override
  public int getCumulativeCancelledOrderCount() {
    return NumberUtil.getIntegerValue(ccnoc);
  }

  @Override
  public int getCumulativePaidOrderCount() {
    return NumberUtil.getIntegerValue(cpoc);
  }

  // In transit inventory metrics
  @Override
  public int getDemandShippedCount() {
    return NumberUtil.getIntegerValue(dsc);
  }

  ;

  @Override
  public void setDemandShippedCount(int demandShippedCount) {
    this.dsc = new Integer(demandShippedCount);
  }

  ;

  @Override
  public BigDecimal getDemandShippedQuantity() {
    return BigUtil.getZeroIfNull(dsq);
  }

  ;

  @Override
  public void setDemandShippedQuantity(BigDecimal demandShippedQuantity) {
    this.dsq = demandShippedQuantity;
  }

  ;

  // Message Log metrics
  @Override
  public int getSMSCount() {
    return NumberUtil.getIntegerValue(smsc);
  }

  @Override
  public void setSMSCount(int smsCount) {
    this.smsc = new Integer(smsCount);
  }

  @Override
  public int getEmailCount() {
    return NumberUtil.getIntegerValue(emlc);
  }

  @Override
  public void setEmailCount(int emailCount) {
    this.emlc = new Integer(emailCount);
  }

  // Cumulative MessageLog metrics
  @Override
  public int getCumulativeSMSCount() {
    return NumberUtil.getIntegerValue(csmsc);
  }

  @Override
  public int getCumulativeEmailCount() {
    return NumberUtil.getIntegerValue(cemlc);
  }

  @Override
  public void initFromLastSlice(ISlice iLastSlice) {
    Slice lastSlice = (Slice) iLastSlice;
    sq = lastSlice.getStockQuantity();
    // Init. cumulative counts
    cic = lastSlice.cic;
    crc = lastSlice.crc;
    csc = lastSlice.csc;
    cwc = lastSlice.cwc;
    ctc = lastSlice.ctc;
    crtc = lastSlice.crtc; // return counts
    ctrc = lastSlice.ctrc; // transfer counts
    cdc = lastSlice.cdc;
    coc = lastSlice.coc;
    cos = lastSlice.cos;
    cbrv = lastSlice.cbrv;
    crrv = lastSlice.crrv;
    clnc = lastSlice.clnc;
    // Abnormal Stock Event specific counts
    csoec = lastSlice.csoec; // Cumulative stock out event counts
    clmec = lastSlice.clmec; // Cumulative less than min event counts
    cgmec = lastSlice.cgmec; // Cumulative greater than max event counts
    ctasec = lastSlice.ctasec; // Cumulative total abnormal stock event counts
    cnlec = lastSlice.cnlec; // Cumulative not logged in event count
    // Order metrics
    ccfoc = lastSlice.ccfoc; // Cumulative Confirmed Order counts
    cshoc = lastSlice.cshoc; // Cumulative Shipped Order counts
    cfloc = lastSlice.cfloc; // Cumulative Fulfilled Order counts
    ccnoc = lastSlice.ccnoc; // Cumulative Cancelled Order counts
    cpoc = lastSlice.cpoc; // Cumulative Paid Order counts
    // MessageLog metrics
    csmsc = lastSlice.csmsc; // Cumulative SMS count
    cemlc = lastSlice.cemlc; // Cumulative Email count
  }

  // Private method to initialize the cumulative counts from last slice.
  @Override
  public void updateCumulativeCounts(ISlice iLastSlice) {
    Slice lastSlice = (Slice) iLastSlice;
    xLogger.fine("Entering updateCumulativeCounts");
    cic =
        (lastSlice == null || lastSlice.cic == null ? (ic == null ? new Integer(0) : ic)
            : (ic == null ? lastSlice.cic : lastSlice.cic + ic)); // Cumulative issue count
    crc =
        (lastSlice == null || lastSlice.crc == null ? (rc == null ? new Integer(0) : rc)
            : (rc == null ? lastSlice.crc : lastSlice.crc + rc)); // Cumulative receipt count
    csc =
        (lastSlice == null || lastSlice.csc == null ? (sc == null ? new Integer(0) : sc)
            : (sc == null ? lastSlice.csc : lastSlice.csc + sc)); // Cumulative stock count
    cwc =
        (lastSlice == null || lastSlice.cwc == null ? (wc == null ? new Integer(0) : wc)
            : (wc == null ? lastSlice.cwc : lastSlice.cwc + wc)); // Cumulative wastage count
    ctc =
        (lastSlice == null || lastSlice.ctc == null ? (tc == null ? new Integer(0) : tc)
            : (tc == null ? lastSlice.ctc : lastSlice.ctc + tc)); // Cumulative transfer count
    ctrc =
        (lastSlice == null || lastSlice.ctrc == null ? (trc == null ? new Integer(0) : trc)
            : (trc == null ? lastSlice.ctrc : lastSlice.ctrc + trc)); // Cumulative transfer counts
    crtc =
        (lastSlice == null || lastSlice.crtc == null ? (rtc == null ? new Integer(0) : rtc)
            : (rtc == null ? lastSlice.crtc : lastSlice.crtc + rtc)); // Cumulative return count
    cdc =
        (lastSlice == null || lastSlice.cdc == null ? (dc == null ? new Integer(0) : dc)
            : (dc == null ? lastSlice.cdc : lastSlice.cdc + dc)); // Cumulative demand count
    coc =
        (lastSlice == null || lastSlice.coc == null ? (oc == null ? new Integer(0) : oc)
            : (oc == null ? lastSlice.coc : lastSlice.coc + oc)); // Cumulative order count
    cos =
        (lastSlice == null || lastSlice.cos == null ? (os == null ? new Integer(0) : os)
            : (os == null ? lastSlice.cos : lastSlice.cos + os)); // Cumulative order size

    cbrv =
        (lastSlice == null || lastSlice.cbrv == null ? (brv == null ? BigDecimal.ZERO : brv)
            : (brv == null ? lastSlice.cbrv
                : lastSlice.cbrv.add(brv)));  // Cumulative Booked Revenue
    crrv =
        (lastSlice == null || lastSlice.crrv == null ? (rrv == null ? BigDecimal.ZERO : rrv)
            : (rrv == null ? lastSlice.crrv
                : lastSlice.crrv.add(rrv))); // Cumulative realized revenue

    clnc =
        (lastSlice == null || lastSlice.clnc == null ? (lnc == null ? new Integer(0) : lnc)
            : (lnc == null ? lastSlice.clnc : lastSlice.clnc + lnc));// Cumulative Login count

    // Abnormal Stock Event specific counts
    csoec =
        (lastSlice == null || lastSlice.csoec == null ? (soec == null ? new Integer(0) : soec)
            : (soec == null ? lastSlice.csoec
                : lastSlice.csoec + soec));// Cumulative stock out event counts
    clmec =
        (lastSlice == null || lastSlice.clmec == null ? (lmec == null ? new Integer(0) : lmec)
            : (lmec == null ? lastSlice.clmec
                : lastSlice.clmec + lmec)); // Cumulative less than min event counts
    cgmec =
        (lastSlice == null || lastSlice.cgmec == null ? (gmec == null ? new Integer(0) : gmec)
            : (gmec == null ? lastSlice.cgmec
                : lastSlice.cgmec + gmec)); // Cumulative greater than max event counts
    ctasec =
        (lastSlice == null || lastSlice.ctasec == null ? (tasec == null ? new Integer(0) : tasec)
            : (tasec == null ? lastSlice.ctasec
                : lastSlice.ctasec + tasec)); // Cumulative total abnormal stock event counts

    // Not logged in event metric
    cnlec =
        (lastSlice == null || lastSlice.cnlec == null ? (nlec == null ? new Integer(0) : nlec)
            : (nlec == null ? lastSlice.cnlec
                : lastSlice.cnlec + nlec)); // Cumulative Not logged in event count

    // Order metrics
    ccfoc =
        (lastSlice == null || lastSlice.ccfoc == null ? (cfoc == null ? new Integer(0) : cfoc)
            : (cfoc == null ? lastSlice.ccfoc
                : lastSlice.ccfoc + cfoc));// Cumulative Confirmed Order counts
    cshoc =
        (lastSlice == null || lastSlice.cshoc == null ? (shoc == null ? new Integer(0) : shoc)
            : (shoc == null ? lastSlice.cshoc
                : lastSlice.cshoc + shoc)); // Cumulative Shipped Order counts
    cfloc =
        (lastSlice == null || lastSlice.cfloc == null ? (floc == null ? new Integer(0) : floc)
            : (floc == null ? lastSlice.cfloc
                : lastSlice.cfloc + floc));// Cumulative Fulfilled Order counts
    ccnoc =
        (lastSlice == null || lastSlice.ccnoc == null ? (cnoc == null ? new Integer(0) : cnoc)
            : (cnoc == null ? lastSlice.ccnoc
                : lastSlice.ccnoc + cnoc)); // Cumulative Cancelled Order counts
    cpoc =
        (lastSlice == null || lastSlice.cpoc == null ? (poc == null ? new Integer(0) : poc)
            : (poc == null ? lastSlice.cpoc
                : lastSlice.cpoc + poc)); // Cumulative Paid Order counts

    // MessageLog metrics
    csmsc =
        (lastSlice == null || lastSlice.csmsc == null ? (smsc == null ? new Integer(0) : smsc)
            : (smsc == null ? lastSlice.csmsc : lastSlice.csmsc + smsc));// Cumulative SMS count
    cemlc =
        (lastSlice == null || lastSlice.cemlc == null ? (emlc == null ? new Integer(0) : emlc)
            : (emlc == null ? lastSlice.cemlc : lastSlice.cemlc + emlc)); // Cumulative Email count

    xLogger.fine("Exiting updateCumulativeCounts");
  }

  @Override
  public Hashtable<String, String> toMap(Locale locale, String timezone) {
    Hashtable<String, String> ht = new Hashtable<String, String>();
    ht.put(JsonTagsZ.TIMESTAMP, LocalDateUtil.format(d, locale, timezone));
    ht.put(JsonTagsZ.DIMENSION_TYPE, dt);
    ht.put(JsonTagsZ.DIMENSION_VALUE, dv);
    // For backward compatibility
    if (OTYPE_MATERIAL.equals(oty)) {
      ht.put(JsonTagsZ.MATERIAL_ID, oId);
    }
    // End backward compatibility
    // Cardinalities
    if (ic != null) {
      ht.put(JsonTagsZ.ISSUE_COUNT, ic.toString());
    }
    if (rc != null) {
      ht.put(JsonTagsZ.RECEIPT_COUNT, rc.toString());
    }
    if (sc != null) {
      ht.put(JsonTagsZ.STOCK_COUNT, sc.toString());
    }
    if (dc != null) {
      ht.put(JsonTagsZ.DEMAND_COUNT, dc.toString()); // actually order count
    }
    if (wc != null) {
      ht.put(JsonTagsZ.WASTAGE_COUNT, wc.toString());
    }
    if (rtc != null) {
      ht.put(JsonTagsZ.RETURN_COUNT, rtc.toString());
    }
    if (trc != null) {
      ht.put(JsonTagsZ.TRANSFER_COUNT, trc.toString());
    }
    // Quantities
    if (iq != null) {
      ht.put(JsonTagsZ.ISSUE_QUANTITY, iq.toString());
    }
    if (rq != null) {
      ht.put(JsonTagsZ.RECEIPT_QUANTITY, rq.toString());
    }
    if (dq != null) {
      ht.put(JsonTagsZ.DEMAND_QUANTITY, dq.toString());
    }
    if (sq != null) {
      ht.put(JsonTagsZ.STOCK_QUANTITY, sq.toString());
    }
    if (sdf != null) {
      ht.put(JsonTagsZ.STOCKCOUNT_DIFFERENCE, sdf.toString());
    }
    if (wq != null) {
      ht.put(JsonTagsZ.WASTAGE_QUANTITY, wq.toString());
    }
    if (rtq != null) {
      ht.put(JsonTagsZ.RETURN_QUANTITY, rtq.toString());
    }
    if (trq != null) {
      ht.put(JsonTagsZ.TRANSFER_QUANTITY, trq.toString());
    }
    return ht;
  }


  @Override
  public JSONObject toJSONObject() throws JSONException {
    JSONObject json = new JSONObject();
    json.put("t", d.getTime());
    json.put("lns", (lnc == null ? 0 : lnc)); // logins
    json.put("ic", ic); // issue counts
    json.put("rc", rc); // receipt counts
    json.put("sc", sc); // stock counts
    json.put("wc", wc); // wastage counts
    if (trc != null) {
      json.put("trc", trc); // transfer counts
    }
    json.put("oc", (oc == null ? 0 : oc)); // orders
    json.put("tc", tc); // total trans. counts
    json.put("rvn", (brv == null ? 0F : brv)); // booked revenue
    return json;
  }

  public String toString() {
    return this.getClass().getSimpleName() + ": d: " + d + ", oty: " + oty + ", oId: " + oId
        + ", dt: " + dt + ", dv: " + dv + ", iq: " + getIssueQuantity() + ", rq: "
        + getReceiptQuantity() + ", sq: " + getStockQuantity() + ", sdf: " + getStockDifference()
        + ", ic: " + getIssueCount() + ", rc: " + getReceiptCount() + ", sc: "
        + getStockcountCount() + ", wc: " + getWastageCount() + ", dc: " + getDemandCount()
        + ", oc: " + getOrderCount() + " , lnc: " + getLoginCounts() + ", clnc: "
        + getCumulativeLoginCounts() + ", soec: " + getStockoutEventCounts() + ", csoec: "
        + getCumulativeStockoutEventCounts() + ", lmec: " + getLessThanMinEventCounts()
        + ", clmec: " + getCumulativeLessThanMinEventCounts() + ", gmec: "
        + getGreaterThanMaxEventCounts() + ", cgmec: " + getCumulativeGreaterThanMaxEventCounts()
        + ", tasec: " + getTotalAbnormalStockEventCounts() + ", ctasec: "
        + getCumulativeTotalAbnormalStockEventCounts() + ", tuc: " + getTransactiveUserCounts()
        + ", tec: " + getTransactiveEntityCounts() + ", pnoc: " + getPendingOrderCount()
        + ", cfoc: " + getConfirmedOrderCount() + ", ccfoc: " + getCumulativeConfirmedOrderCount()
        + ", shoc: " + getShippedOrderCount() + ", cshoc: " + getCumulativeShippedOrderCount()
        + ", floc: " + getFulfilledOrderCount() + ", cfloc: " + getCumulativeFulfilledOrderCount()
        + ", cnoc: " + getCancelledOrderCount() + ", ccnoc: " + getCumulativeCancelledOrderCount()
        + ", poc: " + getPaidOrderCount() + ", cpoc: " + getCumulativePaidOrderCount() + ", dsc: "
        + getDemandShippedCount();
  }



  // Method used purely to fix (migrate) cumulative counts of orders, revenue and logins (NOT NEEDED POST MIGRATION)
  @Override
  public void setCumulativeCounts(ISlice iPrevSlice) {
    Slice prevSlice = (Slice) iPrevSlice;
    // Init. cumulative counts
    if (prevSlice == null) {
      cdc = dc;
      coc = oc;
      cos = os;
      cbrv = brv;
      crrv = rrv;
      clnc = lnc;
    } else {
      cdc = (prevSlice.cdc != null ? prevSlice.cdc : 0) + (dc != null ? dc : 0);
      coc = (prevSlice.coc != null ? prevSlice.coc : 0) + (oc != null ? oc : 0);
      cos = (prevSlice.cos != null ? prevSlice.cos : 0) + (os != null ? os : 0);
      cbrv =
          (prevSlice.cbrv != null ? prevSlice.cbrv : BigDecimal.ZERO)
              .add(brv != null ? brv : BigDecimal.ZERO);
      crrv =
          (prevSlice.crrv != null ? prevSlice.crrv : BigDecimal.ZERO)
              .add(rrv != null ? rrv : BigDecimal.ZERO);
      clnc = (prevSlice.clnc != null ? prevSlice.clnc : 0) + (lnc != null ? lnc : 0);
    }
  }


  @Override
  public String getSliceType() {
    return null;
  }

  @Override
  public void createKey(String tz) {
    k = JDOUtils.createSliceKey(dId, d, oty, oId, dt, dv, tz);
  }
}



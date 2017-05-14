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

package com.logistimo.reports.entity.slices;

import com.logistimo.reports.ReportsConstants;

import org.json.JSONException;
import org.json.JSONObject;

import com.logistimo.utils.BigUtil;
import com.logistimo.utils.LocalDateUtil;
import com.logistimo.utils.NumberUtil;

import java.math.BigDecimal;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Locale;
import java.util.Map;

/**
 * Created by mohansrinivas on 10/3/16.
 */
public class ReportsSlice implements IReportsSlice {

  private static final float AVG_DAYS_PER_MONTH = 30.44F;
  public Map cassandraRow = new HashMap<>();

  private static float getDurationMonthlyAverageHours(long durationMillis) {
    float durationDays = durationMillis / 86400000F;
    if (durationDays <= AVG_DAYS_PER_MONTH) {
      return durationMillis / 3600000F;
    }
    float numMonths = Math.round(durationDays / AVG_DAYS_PER_MONTH);
    return (durationDays / numMonths) * 24F; // hours
  }

  @Override
  public String getKey() {
    return null;
  }

  @Override
  public Date getDate() {
    Calendar d = GregorianCalendar.getInstance();
    // T field of cassandra which is analogous to Date field in the slice
    // T will be in the format of year-month-day.
    // day will be present only for day table data.
    if (cassandraRow.containsKey("t")) {
      String t = cassandraRow.get("t").toString();
      String[] date = t.split("-");
      int year = Integer.parseInt(date[0].toString());
      int month = Integer.parseInt(date[1].toString());
      int day = date.length > 2 ? Integer.parseInt(date[2].toString()) : 1;
      LocalDateUtil.resetTimeFields(d);
      d.set(year, month - 1, day);
    }
    return d.getTime();
  }

  @Override
  public void setDate(Date d, String tz, String freq) {
    cassandraRow.put("t", LocalDateUtil.formatDateForReports(d,freq));
  }

  @Override
  public String getDimensionType() {
    return cassandraRow.get("kid") != null ? ReportsConstants.FILTER_KIOSK : null;
  }

  @Override
  public String getDimensionValue() {
    return cassandraRow.get("kid") != null ? cassandraRow.get("kid").toString() : null;
  }

  @Override
  public void setDimension(String dmType, String dmValue) {

  }

  @Override
  public String getObjectType() {
    return cassandraRow.get("mid") != null ? OTYPE_MATERIAL : OTYPE_DOMAIN;
  }

  @Override
  public String getObjectId() {
    return cassandraRow.get("mid") != null ? cassandraRow.get("mid").toString()
        : cassandraRow.get("did").toString();
  }

  @Override
  public void setObject(String objectType, String objectId) {

  }

  @Override
  public Long getDomainId() {
    return Long.parseLong(cassandraRow.get("did").toString());
  }

  @Override
  public void setDomainId(Long dId) {
    cassandraRow.put("did",dId);
  }

  @Override
  public int getIssueCount() {
    return cassandraRow.get("ic") != null ? Integer.parseInt(cassandraRow.get("ic").toString()) : 0;
  }

  @Override
  public void setIssueCount(int i) {

  }

  @Override
  public int getReceiptCount() {
    return cassandraRow.get("rc") != null ? Integer.parseInt(cassandraRow.get("rc").toString()) : 0;
  }

  @Override
  public void setReceiptCount(int r) {

  }

  @Override
  public int getStockcountCount() {
    return cassandraRow.get("sc") != null ? Integer.parseInt(cassandraRow.get("sc").toString()) : 0;
  }

  @Override
  public void setStockcountCount(int s) {

  }

  @Override
  public int getWastageCount() {
    return cassandraRow.get("wc") != null ? Integer.parseInt(cassandraRow.get("wc").toString()) : 0;
  }

  @Override
  public void setWastageCount(int wc) {

  }

  @Override
  public int getTransferCount() {
    return cassandraRow.get("trc") != null ? Integer.parseInt(cassandraRow.get("trc").toString())
        : 0;
  }

  @Override
  public void setTransferCount(int trc) {

  }

  @Override
  public int getTotalCount() {
    int
        tc =
        cassandraRow.get("tc") != null ? Integer.parseInt(cassandraRow.get("tc").toString()) : 0;
    return tc;
  }

  @Override
  public int getDemandCount() {
    int
        dc =
        cassandraRow.get("dc") != null ? Integer.parseInt(cassandraRow.get("dc").toString()) : 0;
    return dc;
  }

  @Override
  public void setDemandCount(int dc) {

  }

  @Override
  public int getReturnCount() {
    return cassandraRow.get("rtc") != null ? Integer.parseInt(cassandraRow.get("rtc").toString())
        : 0;
  }

  @Override
  public void setReturnCount(int rtc) {

  }

  @Override
  public int getOrderCount() {
    return cassandraRow.get("oc") != null ? Integer.parseInt(cassandraRow.get("oc").toString()) : 0;
  }

  @Override
  public void setOrderCount(int oc) {

  }

  @Override
  public BigDecimal getIssueQuantity() {
    return cassandraRow.get("iq") != null ? BigUtil
        .getZeroIfNull(new BigDecimal(cassandraRow.get("iq").toString())) : BigDecimal.ZERO;
  }

  @Override
  public void setIssueQuantity(BigDecimal i) {

  }

  @Override
  public BigDecimal getReceiptQuantity() {
    return cassandraRow.get("rq") != null ? BigUtil
        .getZeroIfNull(new BigDecimal(cassandraRow.get("rq").toString())) : BigDecimal.ZERO;
  }

  @Override
  public void setReceiptQuantity(BigDecimal r) {

  }

  @Override
  public BigDecimal getDemandQuantity() {
    return cassandraRow.get("dq") != null ? BigUtil
        .getZeroIfNull(new BigDecimal(cassandraRow.get("dq").toString())) : BigDecimal.ZERO;
  }

  @Override
  public void setDemandQuantity(BigDecimal dq) {

  }

  @Override
  public BigDecimal getStockQuantity() {
    return cassandraRow.get("sq") != null ? BigUtil
        .getZeroIfNull(new BigDecimal(cassandraRow.get("sq").toString())) : BigDecimal.ZERO;
  }

  @Override
  public void setStockQuantity(BigDecimal s) {

  }

  @Override
  public BigDecimal getStockDifference() {
    return cassandraRow.get("sdf") != null ? BigUtil
        .getZeroIfNull(new BigDecimal(cassandraRow.get("sdf").toString())) : BigDecimal.ZERO;
  }

  @Override
  public void setStockDifference(BigDecimal sdf) {

  }

  @Override
  public BigDecimal getWastageQuantity() {
    return cassandraRow.get("wq") != null ? BigUtil
        .getZeroIfNull(new BigDecimal(cassandraRow.get("wq").toString())) : BigDecimal.ZERO;
  }

  @Override
  public void setWastageQuantity(BigDecimal wq) {

  }

  @Override
  public BigDecimal getTransferQuantity() {
    return cassandraRow.get("trq") != null ? BigUtil
        .getZeroIfNull(new BigDecimal(cassandraRow.get("trq").toString())) : BigDecimal.ZERO;
  }

  @Override
  public void setTransferQuantity(BigDecimal trq) {

  }

  @Override
  public BigDecimal getReturnQuantity() {
    return cassandraRow.get("rtq") != null ? BigUtil
        .getZeroIfNull(new BigDecimal(cassandraRow.get("rtq").toString())) : BigDecimal.ZERO;
  }

  @Override
  public void setReturnQuantity(BigDecimal rtq) {

  }

  @Override
  public int getOrderSize() {
    return cassandraRow.get("os") != null ? Integer.parseInt(cassandraRow.get("os").toString()) : 0;
  }

  @Override
  public void setOrderSize(int os) {

  }

  @Override
  public BigDecimal getRevenueBooked() {
    return cassandraRow.get("brv") != null ? BigUtil
        .getZeroIfNull(new BigDecimal(cassandraRow.get("brv").toString())) : BigDecimal.ZERO;
  }

  @Override
  public void setRevenueBooked(BigDecimal rvn) {

  }

  @Override
  public BigDecimal getRevenueRealizable() {

    return cassandraRow.get("rrv") != null ? BigUtil
        .getZeroIfNull(new BigDecimal(cassandraRow.get("rrv").toString())) : BigDecimal.ZERO;
  }

  @Override
  public void setRevenueRealizable(BigDecimal rvn) {

  }

  @Override
  public int getLoginCounts() {
    return cassandraRow.get("lnc") != null ? Integer.parseInt(cassandraRow.get("lnc").toString())
        : 0;
  }

  @Override
  public void setLoginCounts(int loginCounts) {

  }

  @Override
  public int getCumulativeIssueCount() {
    return cassandraRow.get("cic") != null ? Integer.parseInt(cassandraRow.get("cic").toString())
        : 0;
  }

  @Override
  public int getCumulativeReceiptCount() {
    return cassandraRow.get("crc") != null ? Integer.parseInt(cassandraRow.get("crc").toString())
        : 0;
  }

  @Override
  public int getCumulativeStockcountCount() {
    return cassandraRow.get("csc") != null ? Integer.parseInt(cassandraRow.get("csc").toString())
        : 0;
  }

  @Override
  public int getCumulativeWastageCount() {
    return cassandraRow.get("cwc") != null ? Integer.parseInt(cassandraRow.get("cwc").toString())
        : 0;
  }

  @Override
  public int getCumulativeTransferCount() {
    return cassandraRow.get("ctrc") != null ? Integer.parseInt(cassandraRow.get("ctrc").toString())
        : 0;
  }

  @Override
  public int getCumulativeTotalCount() {
    return cassandraRow.get("ctc") != null ? Integer.parseInt(cassandraRow.get("ctc").toString())
        : 0;
  }

  @Override
  public int getCumulativeDemandCount() {
    return cassandraRow.get("cdc") != null ? Integer.parseInt(cassandraRow.get("cdc").toString())
        : 0;
  }

  @Override
  public int getCumulativeReturnCount() {
    return cassandraRow.get("crtc") != null ? Integer.parseInt(cassandraRow.get("crtc").toString())
        : 0;
  }

  @Override
  public int getCumulativeOrderCount() {
    return cassandraRow.get("coc") != null ? Integer.parseInt(cassandraRow.get("coc").toString())
        : 0;
  }

  @Override
  public int getCumulativeOrderSize() {
    return cassandraRow.get("cos") != null ? Integer.parseInt(cassandraRow.get("cos").toString())
        : 0;
  }

  @Override
  public int getCumulativeLoginCounts() {
    return cassandraRow.get("clnc") != null ? Integer.parseInt(cassandraRow.get("clnc").toString())
        : 0;
  }

  @Override
  public void setCumulativeLoginCounts(int cumulativeNumLogins) {

  }

  @Override
  public BigDecimal getCumulativeRevenueBooked() {
    return cassandraRow.get("cbrv") != null ? BigUtil
        .getZeroIfNull(new BigDecimal(cassandraRow.get("cbrv").toString())) : BigDecimal.ZERO;
  }

  @Override
  public BigDecimal getCumulativeRevenueRealized() {
    return cassandraRow.get("crrv") != null ? BigUtil
        .getZeroIfNull(new BigDecimal(cassandraRow.get("crrv").toString())) : BigDecimal.ZERO;
  }

  @Override
  public int getStockoutEventCounts() {
    return cassandraRow.get("soec") != null ? Integer.parseInt(cassandraRow.get("soec").toString())
        : 0;
  }

  @Override
  public void setStockoutEventCounts(int stockoutEventCounts) {

  }

  @Override
  public int getLessThanMinEventCounts() {
    return cassandraRow.get("lmec") != null ? Integer.parseInt(cassandraRow.get("lmec").toString())
        : 0;
  }

  @Override
  public void setLessThanMinEventCounts(int lessThanMinEventCounts) {

  }

  @Override
  public int getGreaterThanMaxEventCounts() {
    return cassandraRow.get("gmec") != null ? Integer.parseInt(cassandraRow.get("gmec").toString())
        : 0;
  }

  @Override
  public void setGreaterThanMaxEventCounts(int greaterThanMaxEventCounts) {

  }

  @Override
  public int getNotLoggedInEventCounts() {
    return cassandraRow.get("nlec") != null ? Integer.parseInt(cassandraRow.get("nlec").toString())
        : 0;
  }

  @Override
  public void setNotLoggedInEventCounts(int notLoggedInEventCounts) {

  }

  @Override
  public int getTotalAbnormalStockEventCounts() {
    return cassandraRow.get("tasec") != null ? Integer
        .parseInt(cassandraRow.get("tasec").toString()) : 0;
  }

  @Override
  public void setTotalAbnormalStockEventCounts(int totalAbnormalStockEventCounts) {

  }

  @Override
  public int getCumulativeStockoutEventCounts() {
    return cassandraRow.get("csoec") != null ? Integer
        .parseInt(cassandraRow.get("csoec").toString()) : 0;
  }

  @Override
  public int getCumulativeLessThanMinEventCounts() {
    return cassandraRow.get("clmec") != null ? Integer
        .parseInt(cassandraRow.get("clmec").toString()) : 0;
  }

  @Override
  public int getCumulativeGreaterThanMaxEventCounts() {
    return cassandraRow.get("cgmec") != null ? Integer
        .parseInt(cassandraRow.get("cgmec").toString()) : 0;
  }

  @Override
  public int getCumulativeNotLoggedInEventCounts() {
    return cassandraRow.get("cnlec") != null ? Integer
        .parseInt(cassandraRow.get("cnlec").toString()) : 0;
  }

  @Override
  public int getCumulativeTotalAbnormalStockEventCounts() {
    return cassandraRow.get("ctasec") != null ? Integer
        .parseInt(cassandraRow.get("ctasec").toString()) : 0;
  }

  @Override
  public int getTransactiveUserCounts() {
    return cassandraRow.get("tuc") != null ? Integer.parseInt(cassandraRow.get("tuc").toString())
        : 0;
  }

  @Override
  public void setTransactiveUserCounts(int transactiveUserCounts) {

  }

  @Override
  public int getTransactiveEntityCounts() {
    return cassandraRow.get("tec") != null ? Integer.parseInt(cassandraRow.get("tec").toString())
        : 0;
  }

  @Override
  public void setTransactiveEntitiyCounts(int transactiveEntityCounts) {

  }

  @Override
  public int getPendingOrderCount() {
    return cassandraRow.get("pnoc") != null ? Integer.parseInt(cassandraRow.get("pnoc").toString())
        : 0;
  }

  @Override
  public void setPendingOrderCount(int pendingOrderCount) {

  }

  @Override
  public int getConfirmedOrderCount() {
    return cassandraRow.get("cfoc") != null ? Integer.parseInt(cassandraRow.get("cfoc").toString())
        : 0;
  }

  @Override
  public void setConfirmedOrderCount(int confirmedOrderCount) {

  }

  @Override
  public int getShippedOrderCount() {
    return cassandraRow.get("shoc") != null ? Integer.parseInt(cassandraRow.get("shoc").toString())
        : 0;
  }

  @Override
  public void setShippedOrderCount(int shippedOrderCount) {

  }

  @Override
  public int getFulfilledOrderCount() {
    return cassandraRow.get("floc") != null ? Integer.parseInt(cassandraRow.get("floc").toString())
        : 0;
  }

  @Override
  public void setFulfilledOrderCount(int fulfilledOrderCount) {

  }

  @Override
  public int getCancelledOrderCount() {
    return cassandraRow.get("cnoc") != null ? Integer.parseInt(cassandraRow.get("cnoc").toString())
        : 0;
  }

  @Override
  public void setCancelledOrderCount(int cancelledOrderCount) {

  }

  @Override
  public int getPaidOrderCount() {
    return cassandraRow.get("poc") != null ? Integer.parseInt(cassandraRow.get("poc").toString())
        : 0;
  }

  @Override
  public void setPaidOrderCount(int paidOrderCount) {

  }

  @Override
  public int getCumulativeConfirmedOrderCount() {
    return cassandraRow.get("ccfoc") != null ? Integer
        .parseInt(cassandraRow.get("ccfoc").toString()) : 0;
  }

  @Override
  public int getCumulativeShippedOrderCount() {
    return cassandraRow.get("cshoc") != null ? Integer
        .parseInt(cassandraRow.get("cshoc").toString()) : 0;
  }

  @Override
  public int getCumulativeFulfilledOrderCount() {
    return cassandraRow.get("cfloc") != null ? Integer
        .parseInt(cassandraRow.get("cfloc").toString()) : 0;
  }

  @Override
  public int getCumulativeCancelledOrderCount() {
    return cassandraRow.get("ccnoc") != null ? Integer
        .parseInt(cassandraRow.get("ccnoc").toString()) : 0;
  }

  @Override
  public int getCumulativePaidOrderCount() {
    return cassandraRow.get("cpoc") != null ? Integer.parseInt(cassandraRow.get("cpoc").toString())
        : 0;
  }

  @Override
  public int getDemandShippedCount() {
    return cassandraRow.get("dsc") != null ? Integer.parseInt(cassandraRow.get("dsc").toString())
        : 0;
  }

  @Override
  public void setDemandShippedCount(int demandShippedCount) {

  }

  @Override
  public BigDecimal getDemandShippedQuantity() {
    return cassandraRow.get("dsq") != null ? BigUtil
        .getZeroIfNull(new BigDecimal(cassandraRow.get("dsq").toString())) : BigDecimal.ZERO;
  }

  @Override
  public void setDemandShippedQuantity(BigDecimal demandShippedQuantity) {

  }

  @Override
  public int getSMSCount() {
    return cassandraRow.get("smsc") != null ? Integer.parseInt(cassandraRow.get("smsc").toString())
        : 0;
  }

  @Override
  public void setSMSCount(int smsCount) {

  }

  @Override
  public int getEmailCount() {
    return cassandraRow.get("emlc") != null ? Integer.parseInt(cassandraRow.get("emlc").toString())
        : 0;
  }

  @Override
  public void setEmailCount(int emailCount) {

  }

  @Override
  public int getCumulativeSMSCount() {
    return cassandraRow.get("csmsc") != null ? Integer
        .parseInt(cassandraRow.get("csmsc").toString()) : 0;
  }

  @Override
  public int getCumulativeEmailCount() {
    return cassandraRow.get("cemlc") != null ? Integer
        .parseInt(cassandraRow.get("cemlc").toString()) : 0;
  }

  @Override
  public void initFromLastSlice(ISlice iLastSlice) {
    ReportsSlice lastSlice = (ReportsSlice) iLastSlice;
    cassandraRow.put("sq", lastSlice.getStockQuantity());
    cassandraRow.put("cic", lastSlice.getCumulativeIssueCount());
    cassandraRow.put("crc", lastSlice.getCumulativeReceiptCount());
    cassandraRow.put("csc", lastSlice.getCumulativeStockcountCount());
    cassandraRow.put("cwc", lastSlice.getCumulativeWastageCount());
    cassandraRow.put("ctc", lastSlice.getCumulativeTotalCount());
    cassandraRow.put("ctrc", lastSlice.getCumulativeTransferCount());
    cassandraRow.put("crtc", lastSlice.getCumulativeReturnCount());
    cassandraRow.put("cdc", lastSlice.getCumulativeDemandCount());
    cassandraRow.put("coc", lastSlice.getCumulativeOrderCount());
    cassandraRow.put("cos", lastSlice.getCumulativeOrderSize());
    cassandraRow.put("cbrv", lastSlice.getCumulativeRevenueBooked());
    cassandraRow.put("crrv", lastSlice.getCumulativeRevenueRealized());
    cassandraRow.put("clnc", lastSlice.getCumulativeLoginCounts());
    // Abnormal Stock Event specific counts
    cassandraRow.put("csoec", lastSlice.getCumulativeStockoutEventCounts());
    cassandraRow.put("clmec", lastSlice.getCumulativeLessThanMinEventCounts());
    cassandraRow.put("cgmec", lastSlice.getCumulativeGreaterThanMaxEventCounts());
    cassandraRow.put("ctasec", lastSlice.getCumulativeTotalAbnormalStockEventCounts());
    cassandraRow.put("cnlec", lastSlice.getCumulativeNotLoggedInEventCounts());
    cassandraRow.put("cnlec", lastSlice.getCumulativeNotLoggedInEventCounts());
    // Order metrics
    cassandraRow.put("ccfoc", lastSlice.getCumulativeCancelledOrderCount());
    cassandraRow.put("cshoc", lastSlice.getCumulativeShippedOrderCount());
    cassandraRow.put("cfloc", lastSlice.getCumulativeFulfilledOrderCount());
    cassandraRow.put("ccnoc", lastSlice.getCumulativeNotLoggedInEventCounts());
    cassandraRow.put("cpoc", lastSlice.getCumulativeNotLoggedInEventCounts());
  }

  @Override
  public void updateCumulativeCounts(ISlice lastSlice) {

  }

  @Override
  public Hashtable<String, String> toMap(Locale locale, String timezone) {
    return null;
  }

  @Override
  public JSONObject toJSONObject() throws JSONException {
    return null;
  }

  @Override
  public void setCumulativeCounts(ISlice prevSlice) {

  }

  @Override
  public void createKey(String tz) {

  }

  @Override
  public String getSliceType() {
    String key = ReportsConstants.FREQ_MONTHLY;
    if (cassandraRow.containsKey("t")) {
      String t = cassandraRow.get("t").toString();
      String[] date = t.split("-");
      if (date.length > 2) {
        key = ReportsConstants.FREQ_DAILY;
      }
    }
    return key;
  }



  @Override
  public float getAverageStockoutResponseTime() {
    Integer
        soc =
        cassandraRow.get("soc") != null ? Integer.parseInt(cassandraRow.get("soc").toString()) : 0;
    Float
        sod =
        cassandraRow.get("sod") != null ? Float.parseFloat(cassandraRow.get("sod").toString()) : 0F;
    return (soc == 0) ? 0F : NumberUtil.round2((sod / soc) / 24F);
  }

  @Override
  public float getAverageLessThanMinResponseTimeAverage() {
    Integer
        lmc =
        cassandraRow.get("lmc") != null ? Integer.parseInt(cassandraRow.get("lmc").toString()) : 0;
    Float
        lmd =
        cassandraRow.get("lmd") != null ? Float.parseFloat(cassandraRow.get("lmd").toString()) : 0F;
    return (lmc == 0) ? 0F : NumberUtil.round2((lmd / lmc) / 24F);

  }

  @Override
  public float getAverageMoreThanMaxResponse() {
    Integer
        gmc =
        cassandraRow.get("gmc") != null ? Integer.parseInt(cassandraRow.get("gmc").toString()) : 0;
    Float
        gmd =
        cassandraRow.get("gmd") != null ? Float.parseFloat(cassandraRow.get("gmd").toString()) : 0;
    return (gmc == 0) ? 0F : NumberUtil.round2((gmd / gmc) / 24F);
  }

  @Override
  public float getAverageOrderProcessingTime() {
    Integer
        ptc =
        cassandraRow.get("ptc") != null ? Integer.parseInt(cassandraRow.get("ptc").toString()) : 0;
    Long
        pt =
        cassandraRow.get("pt") != null ? Long.parseLong(cassandraRow.get("pt").toString()) : 0L;
    return (ptc == 0) ? 0F : NumberUtil.round2((getDurationMonthlyAverageHours(pt) / ptc) / 24F);
  }

  @Override
  public float getAverageOrderDeliveryTime() {
    Integer
        dltc =
        cassandraRow.get("dltc") != null ? Integer.parseInt(cassandraRow.get("dltc").toString())
            : 0;
    Long
        dlt =
        cassandraRow.get("dlt") != null ? Long.parseLong(cassandraRow.get("dlt").toString()) : 0L;
    return (dltc == 0) ? 0F : NumberUtil.round2((getDurationMonthlyAverageHours(dlt) / dltc) / 24F);
  }

  public Float getSod() {
    return cassandraRow.get("sod") != null ? Float.parseFloat(cassandraRow.get("sod").toString())
        : 0F;
  }

  public Integer getSoc() {
    return cassandraRow.get("soc") != null ? Integer.parseInt(cassandraRow.get("soc").toString())
        : 0;
  }

  public Float getLmd() {
    return cassandraRow.get("lmd") != null ? Float.parseFloat(cassandraRow.get("lmd").toString())
        : 0F;
  }

  public Integer getLmc() {
    return cassandraRow.get("lmc") != null ? Integer.parseInt(cassandraRow.get("lmc").toString())
        : 0;
  }

  public Float getGmd() {
    return cassandraRow.get("gmd") != null ? Float.parseFloat(cassandraRow.get("gmd").toString())
        : 0F;
  }

  public Integer getGmc() {
    return cassandraRow.get("gmc") != null ? Integer.parseInt(cassandraRow.get("gmc").toString())
        : 0;
  }

  public Float getOpt() {
    return cassandraRow.get("opc") != null ? Float.parseFloat(cassandraRow.get("opc").toString())
        : 0F;
  }

  public Integer getOpc() {
    return cassandraRow.get("opc") != null ? Integer.parseInt(cassandraRow.get("opc").toString())
        : 0;
  }

  public Float getDlt() {
    return cassandraRow.get("dlt") != null ? Float.parseFloat(cassandraRow.get("dlt").toString())
        : 0F;
  }

  public Integer getDlc() {
    return cassandraRow.get("dlc") != null ? Integer.parseInt(cassandraRow.get("dlc").toString())
        : 0;
  }


}

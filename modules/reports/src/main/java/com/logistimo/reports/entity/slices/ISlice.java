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

import org.json.JSONException;
import org.json.JSONObject;

import java.math.BigDecimal;
import java.util.Date;
import java.util.Hashtable;
import java.util.Locale;

/**
 * Created by charan on 21/05/15.
 */
public interface ISlice {
  // Dimensions
  String DOMAIN = "dm";
  String DISTRICT = "ds";
  String STATE = "st";
  String POOLGROUP = "pl";
  String KIOSK = "ksk";
  // Frequency
  String DAILY = "daily";
  String HOURLY = "hourly";
  String MONTHLY = "monthly";
  // Slicer types
  String TRANSACTIONS = "trns";
  // Type of counts
  int TYPE_ALL = 0;
  int TYPE_TRANS = 1;
  int TYPE_ORDERS = 2;
  int TYPE_LOGINS = 3;
  int TYPE_STOCKEVENTLOGS = 4;
  int TYPE_ORDERRESPONSETIMES = 5;
  int TYPE_EVENTS = 6;
  int TYPE_MESSAGELOGS = 7;
  // Object Types
  String OTYPE_DOMAIN = "d";
  String OTYPE_ENTITY = "e";
  String OTYPE_INVENTORY = "i";
  String OTYPE_MATERIAL = "m";
  String OTYPE_USER = "u";

  String getKey();

  Date getDate();

  void setDate(Date d, String tz, String freq);

  String getDimensionType();

  String getDimensionValue();

  // Set dimension, given dimension type (e.g. domain, district) and dimension value (e.g. domainId, district name)
  void setDimension(String dmType, String dmValue);

  String getObjectType();

  String getObjectId();

  void setObject(String objectType, String objectId);

  Long getDomainId();

  void setDomainId(Long dId);

  int getIssueCount();

  void setIssueCount(int i);

  int getReceiptCount();

  void setReceiptCount(int r);

  int getStockcountCount();

  void setStockcountCount(int s);

  int getWastageCount();

  void setWastageCount(int wc);

  int getTransferCount();

  void setTransferCount(int trc);

  int getTotalCount();

  int getDemandCount();

  void setDemandCount(int dc);

  int getReturnCount();

  void setReturnCount(int rtc);

  int getOrderCount();

  void setOrderCount(int oc);

  BigDecimal getIssueQuantity();

  void setIssueQuantity(BigDecimal i);

  BigDecimal getReceiptQuantity();

  void setReceiptQuantity(BigDecimal r);

  BigDecimal getDemandQuantity();

  void setDemandQuantity(BigDecimal dq);

  BigDecimal getStockQuantity();

  void setStockQuantity(BigDecimal s);

  BigDecimal getStockDifference();

  void setStockDifference(BigDecimal sdf);

  BigDecimal getWastageQuantity();

  void setWastageQuantity(BigDecimal wq);

  BigDecimal getTransferQuantity();

  void setTransferQuantity(BigDecimal trq);

  BigDecimal getReturnQuantity();

  void setReturnQuantity(BigDecimal rtq);

  int getOrderSize();

  void setOrderSize(int os);

  BigDecimal getRevenueBooked();

  void setRevenueBooked(BigDecimal rvn);

  BigDecimal getRevenueRealizable();

  void setRevenueRealizable(BigDecimal rvn);

  int getLoginCounts();

  void setLoginCounts(int loginCounts);

  // Cumulative counts
  int getCumulativeIssueCount();

  int getCumulativeReceiptCount();

  int getCumulativeStockcountCount();

  int getCumulativeWastageCount();

  int getCumulativeTransferCount();

  int getCumulativeTotalCount();

  int getCumulativeDemandCount();

  int getCumulativeReturnCount();

  int getCumulativeOrderCount();

  int getCumulativeOrderSize();

  int getCumulativeLoginCounts();

  void setCumulativeLoginCounts(int cumulativeNumLogins);

  BigDecimal getCumulativeRevenueBooked();

  BigDecimal getCumulativeRevenueRealized();

  // Event metrics
  int getStockoutEventCounts();

  void setStockoutEventCounts(int stockoutEventCounts);

  int getLessThanMinEventCounts();

  void setLessThanMinEventCounts(int lessThanMinEventCounts);

  int getGreaterThanMaxEventCounts();

  void setGreaterThanMaxEventCounts(int greaterThanMaxEventCounts);

  int getNotLoggedInEventCounts();

  void setNotLoggedInEventCounts(int notLoggedInEventCounts);

  int getTotalAbnormalStockEventCounts();

  void setTotalAbnormalStockEventCounts(int totalAbnormalStockEventCounts);

  // Cumulative Event Metrics
  int getCumulativeStockoutEventCounts();

  int getCumulativeLessThanMinEventCounts();

  int getCumulativeGreaterThanMaxEventCounts();

  int getCumulativeNotLoggedInEventCounts();

  int getCumulativeTotalAbnormalStockEventCounts();

  // Transactive users
  int getTransactiveUserCounts();

  void setTransactiveUserCounts(int transactiveUserCounts);

  // Active entities
  int getTransactiveEntityCounts();

  void setTransactiveEntitiyCounts(int transactiveEntityCounts);

  // Order metrics
  int getPendingOrderCount();

  void setPendingOrderCount(int pendingOrderCount);

  int getConfirmedOrderCount();

  void setConfirmedOrderCount(int confirmedOrderCount);

  int getShippedOrderCount();

  void setShippedOrderCount(int shippedOrderCount);

  int getFulfilledOrderCount();

  void setFulfilledOrderCount(int fulfilledOrderCount);

  int getCancelledOrderCount();

  void setCancelledOrderCount(int cancelledOrderCount);

  int getPaidOrderCount();

  void setPaidOrderCount(int paidOrderCount);

  // Cumulative Order metrics
  int getCumulativeConfirmedOrderCount();

  int getCumulativeShippedOrderCount();

  int getCumulativeFulfilledOrderCount();

  int getCumulativeCancelledOrderCount();

  int getCumulativePaidOrderCount();

  // In transit inventory metrics
  int getDemandShippedCount();

  void setDemandShippedCount(int demandShippedCount);

  BigDecimal getDemandShippedQuantity();

  void setDemandShippedQuantity(BigDecimal demandShippedQuantity);

  // Message Log metrics
  int getSMSCount();

  void setSMSCount(int smsCount);

  int getEmailCount();

  void setEmailCount(int emailCount);

  // Cumulative MessageLog metrics
  int getCumulativeSMSCount();

  int getCumulativeEmailCount();

  void initFromLastSlice(ISlice lastSlice);

  // Private method to initialize the cumulative counts from last slice.
  void updateCumulativeCounts(ISlice lastSlice);

  Hashtable<String, String> toMap(Locale locale, String timezone);

  JSONObject toJSONObject() throws JSONException;

  // Method used purely to fix (migrate) cumulative counts of orders, revenue and logins (NOT NEEDED POST MIGRATION)
  void setCumulativeCounts(ISlice prevSlice);

  void createKey(String tz);

  String getSliceType();
}

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

package com.logistimo.orders.entity;

import com.logistimo.domains.ICrossDomain;
import com.logistimo.tags.entity.ITag;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Created by charan on 20/05/15.
 */
public interface IOrder extends ICrossDomain {

  // Order states
  String CANCELLED = "cn";
  String CHANGED = "ch"; // not used
  String COMPLETED = "cm"; // shipped
  String CONFIRMED = "cf"; // confirmed receipt of order, and in process
  String FULFILLED = "fl"; // goods arrived at destnation
  String PENDING = "pn"; // order just placed, but not acted on
  String BACKORDERED = "bo";
  // Order status together
  String[] STATUSES = {CANCELLED, CONFIRMED, FULFILLED, PENDING, COMPLETED, BACKORDERED};
  // Types
  String TYPE_SALE = "sle";
  String TYPE_PURCHASE = "prc";
  String TYPE_TRANSFER = "tr";
  // Paid states
  String PAID_FULL = "f";
  String PAID_PARTIAL = "p";
  // Payment history tags
  String AMOUNT = "a";
  String TIME = "t";
  String TIMELINE = "tl";
  // Discount types
  int DISCOUNT_AMOUNT = 1;
  int DISCOUNT_PERCENTAGE = 2;
  int TRANSFER = 0;
  int NONTRANSFER = 1;
  int PURCHASE_ORDER = 1;
  int SALES_ORDER = 2;
  int TRANSFER_ORDER = 0;

  Long getOrderId();

  Long getDomainId();

  void setDomainId(Long dId);

  Long getKioskId();

  void setKioskId(Long kId);

  boolean isStatus(String status);

  String getStatus();

  void setStatus(String status);

  void commitStatus();

  String getPreviousStatus();

  Date getCreatedOn();

  void setCreatedOn(Date cOn);

  Date getUpdatedOn();

  void setUpdatedOn(Date uOn);

  Date getStatusUpdatedOn();

  BigDecimal getTotalPrice();

  void setTotalPrice(BigDecimal tp);

  BigDecimal getTax();

  void setTax(BigDecimal tx);

  String getCurrency();

  void setCurrency(String cr);

  BigDecimal getDiscount();

  void setDiscount(BigDecimal discount);

  int getDiscountType();

  void setDiscountType(int discountType);

  BigDecimal getDiscountedPrice(BigDecimal price);

  Long getServicingKiosk();

  void setServicingKiosk(Long skId);

  String getUserId();

  void setUserId(String uId);

  List<? extends IDemandItem> getItems();

  void setItems(List<? extends IDemandItem> items);

  Integer getNumberOfItems();

  void setNumberOfItems(int noi);

  Date getExpectedArrivalDate();

  void setExpectedArrivalDate(Date date);

  Date getDueDate();

  void setDueDate(Date date);

  String getUpdatedBy();

  void setUpdatedBy(String userId);

  Map<String, String> getFields();

  void putField(String key, String value);

  void putFields(Map<String, String> fields);

  String getFieldsRaw();

  void setFieldsRaw(String fields);

  long getDeliveryLeadTime();

  void setDeliveryLeadTime(long dlt);

  float getDeliveryLeadTimeInHours();

  long getProcessingTime();

  void setProcessingTime(long pt);

  float getProcessingTimeInHours();

  BigDecimal getPaid();

  void addPayment(BigDecimal paid);

  void commitPayment(BigDecimal paid);

  // Get payment history, each time is timeInMillis and paid amount
  Map<Date, BigDecimal> getPaymentTimeline();

  boolean isFullyPaid();

  String getPaidStatus();

  Double getLatitude();

  void setLatitude(Double latitude);

  Double getLongitude();

  void setLongitude(Double longitude);

  Double getGeoAccuracy();

  void setGeoAccuracy(Double geoAccuracyMeters);

  String getGeoErrorCode();

  void setGeoErrorCode(String errorCode);

  IDemandItem getItem(Long materialId);

  int size();

  boolean isValid();

  BigDecimal computeTotalPrice();

  BigDecimal computeTotalPrice(boolean includeTax);

  // Spews out something like INR 5.00 (incl. 10% tax)
  String getPriceStatement();

  // Get formatted price, formatted to two decimal places
  String getFormattedPrice();

  // Check if issues have to be done automatically
  boolean isAutoIssue();

  // Check if an order has been reversed; i.e. it was fulfilled, but then later changed to pending
  boolean isReversed();

  // Get/set the desired fulfillment times, in UTC
  String getExpectedFulfillmentTimeRangesCSV();

  void setExpectedFulfillmentTimeRangesCSV(String expectedFulfillmentTimesCSV);

  // Get/set the confirmed fulfillment time ranges, in UTC
  Date getConfirmedFulfillmentTimeStart();

  Date getConfirmedFulfillmentTimeEnd();

  String getConfirmedFulfillmentTimeRange();

  void setConfirmedFulfillmentTimeRange(String confirmedFulfillmentTimeRange);

  // Get/set the payment option
  String getPaymentOption();

  void setPaymentOption(String popt);

  List<String> getTags(String tagType);

  void setTags(List<String> tags, String tagType);

  void setTgs(List<? extends ITag> tags, String tagType);

  List<Long> getDomainIds();

  @SuppressWarnings("rawtypes")
  Map<String, Object> toMap(boolean includeItems, Locale locale, String timezone,
                            boolean forceIntegerQuantity, boolean includeAccountingData,
                            boolean includeShipmentItems);

  // Name of discount type
  String getDiscountTypeName();

  String getIdString();

  Date getArchivedAt();

  void setArchivedAt(Date archivedAt);

  String getArchivedBy();

  void setArchivedBy(String archivedBy);

  Integer getOrderType();

  void setOrderType(Integer orderType);

  String getReferenceID();

  void setReferenceID(String referenceID);

  String getCancelledDiscrepancyReason();

  void setCancelledDiscrepancyReason(String cdrsn);

  String getPaymentHistory();

  boolean isVisibleToCustomer();

  void setVisibleToCustomer(boolean vtc);

  boolean isVisibleToVendor();

  void setVisibleToVendor(boolean vtv);

  boolean isTransfer();

  boolean isSales();

  boolean isPurchase();

  Integer getSrc();

  void setSrc(Integer source);
}

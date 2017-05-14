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

import com.logistimo.accounting.models.CreditData;
import com.logistimo.accounting.service.IAccountingService;
import com.logistimo.accounting.service.impl.AccountingServiceImpl;
import com.logistimo.config.models.EventsConfig;
import com.logistimo.conversations.service.ConversationService;
import com.logistimo.conversations.service.impl.ConversationServiceImpl;
import com.logistimo.entities.entity.IKiosk;
import com.logistimo.entities.entity.Kiosk;
import com.logistimo.entities.service.EntitiesService;
import com.logistimo.entities.service.EntitiesServiceImpl;
import com.logistimo.materials.service.MaterialCatalogService;
import com.logistimo.materials.service.impl.MaterialCatalogServiceImpl;
import com.logistimo.orders.OrderUtils;
import com.logistimo.orders.service.IDemandService;
import com.logistimo.orders.service.impl.DemandService;
import com.logistimo.services.ServiceException;
import com.logistimo.services.Services;
import com.logistimo.services.impl.PMF;
import com.logistimo.tags.TagUtil;
import com.logistimo.tags.entity.ITag;
import com.logistimo.tags.entity.Tag;
import com.logistimo.users.entity.IUserAccount;
import com.logistimo.users.entity.UserAccount;
import com.logistimo.users.service.impl.UsersServiceImpl;

import org.json.JSONArray;
import org.json.JSONObject;
import com.logistimo.config.models.DomainConfig;
import com.logistimo.conversations.entity.IMessage;

import com.logistimo.proto.JsonTagsZ;
import com.logistimo.utils.BigUtil;
import com.logistimo.utils.LocalDateUtil;
import com.logistimo.utils.NumberUtil;

import com.logistimo.inventory.TransactionUtil;
import com.logistimo.logger.XLog;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;
import java.util.Vector;

import javax.jdo.PersistenceManager;
import javax.jdo.annotations.Column;
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
 * @author Arun
 */

@PersistenceCapable(identityType = IdentityType.APPLICATION, detachable = "true")
public class Order implements IOrder {

  // Logger
  static XLog xLogger = XLog.getLog(Order.class);

  @PrimaryKey
  @Persistent(valueStrategy = IdGeneratorStrategy.IDENTITY)
  private Long id; // order id
  @Persistent(table = "ORDER_DOMAINS", defaultFetchGroup = "true")
  @Join
  @Element(column = "DOMAIN_ID")
  private List<Long> dId;
  @Persistent
  private Long sdId; // source domain Id
  @Persistent
  private Long kId; // kiosk Id
  @Persistent
  private String st = PENDING; // order status
  @Persistent
  private Date cOn; // created on
  @Persistent
  private Date uOn; // (last) updated on
  @Persistent
  private Date stOn; // status last updated on
  @Persistent
  private BigDecimal tp; // total price of order
  @Persistent
  private BigDecimal tx; // tax
  @Persistent
  private String cr; // currency
  @Persistent
  private String uId; // user Id who created order
  @Persistent
  private Long skId; // servicing kiosk Id (i.e. the vendor to fulfill this order)

  @Persistent
  private Integer noi;

  @NotPersistent
  private List<DemandItem> items;

  @Persistent
  private String uuId; // user Id of user who last updated the order status
  @Persistent
  private Long dlt; // delivery lead time in millis - time between "shipped" and "fulfilled"
  @Persistent
  private Long pt; // processing time in millis - time between "confirmed" and "shipped"
  @Persistent
  private String flds; // custom fields
  @Persistent
  private BigDecimal pd; // amount paid on this order so far
  @Persistent
  private String pds; // paid status
  @Persistent
  @Column(length = 2048)
  private String ph; // payment history
  @Persistent
  private Double lt; // latitude
  @Persistent
  private Double ln; // longitude
  @Persistent
  @Extension(vendorName = "datanucleus", key = "gae.unindexed", value = "true")
  private Double gacc; // geo-accuracy in meters, if lat/lng were given
  @Persistent
  @Extension(vendorName = "datanucleus", key = "gae.unindexed", value = "true")
  private String gerr; // geo-error code, in case of an error
  @Persistent
  @Extension(vendorName = "datanucleus", key = "gae.unindexed", value = "true")
  private String
      efts;
  // expected fulfilment times - primary time, alternate time, etc. (CSV of date-time ranges, as <startTime>-<endTime>, each date being of format Constants.DATE_FORMAT)
  @Persistent
  private Date cfts; // confirmed fulfillment time start, if specified
  @Persistent
  private Date cfte; // confirmed fulfillment time end, if specified
  @Persistent
  private String popt; // payment option
  @Persistent
  @Extension(vendorName = "datanucleus", key = "gae.unindexed", value = "true")
  private Integer ast; // aggregation status

  @Persistent(table = "ORDER_TAGS", defaultFetchGroup = "true")
  @Join(column = "key")
  @Element(column = "id")
  private List<Tag> otgs;

  @NotPersistent
  private List<String>
      oldotgs;
  // list of order tags, if present (used mainly for queries involving kioskId and tags)

  @Persistent(table = "ORDER_KTAGS", defaultFetchGroup = "true")
  @Join(column = "key")
  @Element(column = "id")
  private List<Tag> ktgs;

  @NotPersistent
  private List<String> oldktgs; // list of kiosk tags (for queries)

  @Extension(vendorName = "datanucleus", key = "gae.unindexed", value = "true")
  @Persistent
  private BigDecimal dsc; // discount on order
  @Extension(vendorName = "datanucleus", key = "gae.unindexed", value = "true")
  @Persistent
  private Integer dtyp; // discount type


  @NotPersistent
  private String prevStatus = null;

  @Persistent
  private Date arcAt;
  @Persistent
  private String arcBy;
  @Persistent
  private Integer oty = 1; //Order type
  @Persistent
  private String rid; // Reference ID
  /**
   * Estimated date of arrival
   */
  @Persistent
  private Date ead;
  /**
   * Required by date
   */
  @Persistent
  private Date edd;
  /**
   * Cancelled discrepancy reason
   */
  @Persistent
  private String cdrsn;

  public static String getFormattedPrice(float price) {
    return String.format("%.2f", price);
  }

  public static String getFormattedPrice(BigDecimal price) {
    return String.format("%.2f", price);
  }

  // Get estimated fulfillment time-ranges as a string, from a vector of hashtable (as coming in from the REST API)
  public static String getEstimatedFulfillmentTimeRangesAsUTCString(
      Vector<Hashtable<String, String>> timeRanges, Locale locale, String timezone) {
    if (timeRanges == null || timeRanges.isEmpty()) {
      return null;
    }
    Iterator<Hashtable<String, String>> it = timeRanges.iterator();
    List<Map<String, Date>> utcTimeRanges = new ArrayList<Map<String, Date>>();
    while (it.hasNext()) {
      Hashtable<String, String> timeRange = it.next();
      String timeRangeStr = timeRange.get(JsonTagsZ.TIME_START);
      if (timeRangeStr == null || timeRangeStr.isEmpty()) {
        continue;
      }
      try {
        Map<String, Date>
            utcTimeRange =
            LocalDateUtil.parseTimeRange(timeRangeStr, locale, timezone);
        if (utcTimeRange != null && !utcTimeRange.isEmpty()) {
          utcTimeRanges.add(utcTimeRange);
        }
      } catch (ParseException e) {
        xLogger.warn("Error parsing time range {0}", timeRangeStr);
      }
    }
    if (!utcTimeRanges.isEmpty()) {
      return LocalDateUtil.formatTimeRanges(utcTimeRanges, null, null);
    } else {
      return null;
    }
  }

  public Long getId() {
    return id;
  }

  @Override
  public Long getOrderId() {
    return id;
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
  public void setKioskId(Long kId) {
    this.kId = kId;
  }

  @Override
  public boolean isStatus(String status) {
    return status != null && st.equals(status);
  }

  @Override
  public String getStatus() {
    return st;
  }

  @Override
  public void setStatus(
      String status) {  // this just sets the status value; does not affect other attributes
    if (status != null && !status.equals(st)) {
      prevStatus = st;
      st = status;
    }
  }

  @Override
  public void commitStatus() { // NOTE: This commits the new status value, and updates all other attributes - only to be invoked by service classes
    // Update order metrics and accounting associated with order, as needed
    if (prevStatus != null && !st.equals(prevStatus)) { // status changed
      Date newDate = new Date(); // status updated on
      // Update delivery lead time, if necessary
      Date oldStatusDate = getStatusUpdatedOn();
      if (COMPLETED.equals(prevStatus) && FULFILLED.equals(st)) {
        if (oldStatusDate != null) {
          setDeliveryLeadTime(newDate.getTime() - oldStatusDate.getTime());
        }
      } else if ((PENDING.equals(prevStatus) || CONFIRMED.equals(prevStatus)) && (
          COMPLETED.equals(st) || FULFILLED.equals(st))) {
        if (oldStatusDate == null && PENDING.equals(
            prevStatus)) // in case of a pending order, take the order creation timestamp, since status change date may not be available
        {
          oldStatusDate = getCreatedOn();
        }
        if (oldStatusDate != null) {
          setProcessingTime(newDate.getTime() - oldStatusDate.getTime());
        }
        // If product is shipped, update accounting as needed
        doAccounting(getTotalPrice(), BigDecimal.ZERO);
      }
      // Update status update timestamp
      stOn = newDate;
      // Propagate status to demand items (required to efficiently query demand items on status), if status has changed
      propagateStatus();
    }
  }

  @Override
  public String getPreviousStatus() {
    return this.prevStatus;
  }

  @Override
  public Date getCreatedOn() {
    return cOn;
  }

  @Override
  public void setCreatedOn(Date cOn) {
    this.cOn = cOn;
    this.stOn = cOn;
  }

  @Override
  public Date getUpdatedOn() {
    return uOn;
  }

  @Override
  public void setUpdatedOn(Date uOn) {
    Calendar instance = Calendar.getInstance();
    instance.setTime(uOn);
    instance.clear(Calendar.MILLISECOND);
    this.uOn = instance.getTime();
  }

  @Override
  public Date getStatusUpdatedOn() {
    return stOn;
  }

  @Override
  public BigDecimal getTotalPrice() {
    if (tp != null) {
      return tp;
    }
    return BigDecimal.ZERO;
  }

  @Override
  public void setTotalPrice(BigDecimal tp) {
    this.tp = tp;
  }

  @Override
  public BigDecimal getTax() {
    if (tx != null) {
      return tx;
    }
    return BigDecimal.ZERO;
  }

  @Override
  public void setTax(BigDecimal tx) {
    this.tx = tx;
  }

  @Override
  public String getCurrency() {
    return cr;
  }

  @Override
  public void setCurrency(String cr) {
    this.cr = cr;
  }

  @Override
  public BigDecimal getDiscount() {
    if (dsc != null) {
      return dsc;
    }
    return BigDecimal.ZERO;
  }

  @Override
  public void setDiscount(BigDecimal discount) {
    dsc = discount;
  }

  @Override
  public int getDiscountType() {
    return NumberUtil.getIntegerValue(dtyp);
  }

  @Override
  public void setDiscountType(int discountType) {
    dtyp = new Integer(discountType);
  }

  @Override
  public BigDecimal getDiscountedPrice(BigDecimal price) {
    BigDecimal discount = getDiscount();
    if (BigUtil.equalsZero(discount)) {
      return price;
    }
    if (dtyp == DISCOUNT_AMOUNT) {
      return price.subtract(discount);
    } else // percentage
    {
      return price.multiply(BigDecimal.ONE
          .subtract((discount.divide(BigDecimal.valueOf(100), RoundingMode.HALF_UP))));
    }
  }

  @Override
  public Long getServicingKiosk() {
    return this.skId;
  }

  @Override
  public void setServicingKiosk(Long skId) {
    this.skId = skId;
  }

  @Override
  public String getUserId() {
    return uId;
  }

  @Override
  public void setUserId(String uId) {
    this.uId = uId;
  }

  @Override
  public List<? extends IDemandItem> getItems() {
    return items;
  }

  @Override
  public void setItems(List<? extends IDemandItem> items) {
    if (this.items != null) {
      this.items.clear();
      if (items != null && !items.isEmpty()) {
        this.items.addAll((Collection<DemandItem>) items);
      }
    } else {
      this.items = (List<DemandItem>) items;
    }
  }

  @Override
  public Integer getNumberOfItems() {
    return noi;
  }

  @Override
  public void setNumberOfItems(int noi) {
    this.noi = noi;
  }

  @Override
  public Date getExpectedArrivalDate() {
    return ead;
  }

  @Override
  public void setExpectedArrivalDate(Date date) {
    this.ead = date;
  }

  @Override
  public Date getDueDate() {
    return edd;
  }

  @Override
  public void setDueDate(Date date) {
    this.edd = date;
  }

  @Override
  public String getUpdatedBy() {
    return uuId;
  }

  @Override
  public void setUpdatedBy(String userId) {
    this.uuId = userId;
  }

  @Override
  public Map<String, String> getFields() {
    if (flds == null) {
      return null;
    }
    return TransactionUtil.getMap(flds);
  }

  @Override
  public void putField(String key, String value) {
    Map<String, String> map = getFields();
    if (map == null) {
      map = new HashMap<String, String>();
    }
    if (value != null) {
      map.put(key, value);
    } else {
      map.remove(key);
    }
    String str = TransactionUtil.getString(map);
    if (str == null) {
      flds = null;
    } else {
      flds = str;
    }
  }

  @Override
  public void putFields(Map<String, String> fields) {
    if (fields == null || fields.isEmpty()) {
      return;
    }
    Map<String, String> map = null;
    if (flds == null) {
      map = new HashMap<String, String>();
    } else {
      map = TransactionUtil.getMap(flds);
    }
    Iterator<String> keys = fields.keySet().iterator();
    while (keys.hasNext()) {
      String key = keys.next();
      String val = fields.get(key);
      if (val != null) {
        map.put(key, val);
      } else {
        map.remove(key);
      }
    }
    String str = TransactionUtil.getString(map);
    if (str == null) {
      flds = null;
    } else {
      flds = str;
    }
  }

  @Override
  public String getFieldsRaw() {
    if (flds != null) {
      return flds;
    }
    return null;
  }

  @Override
  public void setFieldsRaw(String fields) {
    if (fields != null) {
      this.flds = fields;
    } else {
      this.flds = null;
    }
  }

  @Override
  public long getDeliveryLeadTime() {
    if (dlt == null) {
      return 0;
    }
    return dlt.longValue();
  }

  @Override
  public void setDeliveryLeadTime(long dlt) {
    this.dlt = new Long(dlt);
  }

  @Override
  public float getDeliveryLeadTimeInHours() {
    return LocalDateUtil.getMillisInHours(getDeliveryLeadTime());
  }

  @Override
  public long getProcessingTime() {
    if (pt == null) {
      return 0;
    }
    return pt.longValue();
  }

  @Override
  public void setProcessingTime(long pt) {
    this.pt = new Long(pt);
  }

  @Override
  public float getProcessingTimeInHours() {
    return LocalDateUtil.getMillisInHours(getProcessingTime());
  }

  @Override
  public BigDecimal getPaid() {
    if (pd != null) {
      return pd;
    }
    return BigDecimal.ZERO;
  }

  @Override
  public void addPayment(BigDecimal paid) {
    if (BigUtil.equalsZero(paid)) {
      return;
    }
    this.pd = getPaid().add(paid);
    // Update payment status
    if (BigUtil.greaterThanEquals(getPaid(), getTotalPrice())) {
      pds = PAID_FULL;
    } else {
      pds = null;
    }
    // Update payment history
    updatePaymentHistory(paid);
  }

  @Override
  public void commitPayment(BigDecimal paid) { // update accounts related to payments, if required
    // Do accounting updates, as needed
    doAccounting(BigDecimal.ZERO, paid);
  }

  // Get payment history, each time is timeInMillis and paid amount
  @Override
  public Map<Date, BigDecimal> getPaymentTimeline() {
    if (ph == null) {
      return null;
    }
    Map<Date, BigDecimal> map = new TreeMap<>();
    try {
      JSONObject json = new JSONObject(ph);
      JSONArray array = json.getJSONArray(TIMELINE);
      for (int i = 0; i < array.length(); i++) {
        JSONObject h = array.getJSONObject(i);
        String amt = h.getString(AMOUNT);
        if (amt != null && !amt.isEmpty()) {
          map.put(new Date(h.getLong(TIME)), new BigDecimal(amt));
        }
      }
    } catch (Exception e) {
      xLogger.severe("Exception {0} when getting payment history for order {1}: {2}",
          e.getClass().getName(), id, e.getMessage());
    }
    return map;
  }

  @Override
  public boolean isFullyPaid() {
    return BigUtil.equalsZero(getTotalPrice()) || PAID_FULL.equals(pds);
  }

  @Override
  public String getPaidStatus() {
    return pds;
  }

  @Override
  public Double getLatitude() {
    return lt;
  }

  @Override
  public void setLatitude(Double latitude) {
    this.lt = latitude;
  }

  @Override
  public Double getLongitude() {
    return ln;
  }

  @Override
  public void setLongitude(Double longitude) {
    this.ln = longitude;
  }

  @Override
  public Double getGeoAccuracy() {
    return gacc;
  }

  @Override
  public void setGeoAccuracy(Double geoAccuracyMeters) {
    gacc = geoAccuracyMeters;
  }

  @Override
  public String getGeoErrorCode() {
    return gerr;
  }

  @Override
  public void setGeoErrorCode(String errorCode) {
    gerr = errorCode;
  }

  @Override
  public IDemandItem getItem(Long materialId) {
    if (items == null || items.size() == 0 || materialId == null) {
      return null;
    }
    Iterator<DemandItem> it = items.iterator();
    while (it.hasNext()) {
      IDemandItem di = it.next();
      if (materialId.equals(di.getMaterialId())) {
        return di;
      }
    }
    return null;
  }

  @Override
  public int size() {
    if (items != null) {
      return items.size();
    }
    return 0;
  }

  @Override
  public boolean isValid() {
    return PENDING.equals(st) || CHANGED.equals(st);
  }

  @Override
  public BigDecimal computeTotalPrice() {
    return computeTotalPrice(true);
  }

  @Override
  public BigDecimal computeTotalPrice(boolean includeTax) {
    if (items == null || items.size() == 0) {
      return BigDecimal.ZERO;
    }
    BigDecimal price = BigDecimal.ZERO, priceWithTax = BigDecimal.ZERO;
    Iterator<DemandItem> it = items.iterator();
    while (it.hasNext()) {
      IDemandItem item = it.next();
      if (BigUtil.notEqualsZero(item.getTax()) && includeTax) {
        priceWithTax =
            priceWithTax.add(item.computeTotalPrice(
                true)); // material-level tax present, so use that in tax computation
      } else {
        price = price.add(item.computeTotalPrice(false)); // order-level price, use that
      }
    }
    // Add discount, if specified
    if (getDiscount() != null && BigUtil.notEqualsZero(getDiscount())) {
      price = getDiscountedPrice(price);
    }
    // Add tax, if present
    if (BigUtil.notEqualsZero(price) && tx != null && includeTax) {
      price = price.add(price.multiply(tx.divide(BigDecimal.valueOf(100), RoundingMode.HALF_UP)));
    }
    // Update price with tax, if any
    price = price.add(priceWithTax);
    return price;
  }

  // Spews out something like INR 5.00 (incl. 10% tax)
  @Override
  public String getPriceStatement() {
    String strPrice = "";
    if (cr != null) {
      strPrice = cr + " ";
    }
    strPrice += getFormattedPrice();
    if (tx != null && BigUtil.greaterThanZero(tx)) {
      strPrice += " (incl. " + tx + "% tax)";
    }
    return strPrice;
  }

  // Get formatted price, formatted to two decimal places
  @Override
  public String getFormattedPrice() {
    if (tp == null) {
      return "0.0";
    }
    return getFormattedPrice(tp); // String.format( "%.2f", tp );
  }

  // Check if issues have to be done automatically
  @Override
  public boolean isAutoIssue() {
    return COMPLETED.equals(st);
  }

  // Check if an order has been reversed; i.e. it was fulfilled, but then later changed to pending
  @Override
  public boolean isReversed() {
    // Old status was 'shipped', and new status is something (e.g. pending) other than 'fulfilled' OR
    // old status was 'fulfilled' and new status is 'shipped' or 'cancelled'
    return ((COMPLETED.equals(prevStatus) && !COMPLETED.equals(st) && !FULFILLED.equals(st)) ||
        (FULFILLED.equals(prevStatus) && (COMPLETED.equals(st) || CANCELLED.equals(st))));
  }

  // Get/set the desired fulfillment times, in UTC
  @Override
  public String getExpectedFulfillmentTimeRangesCSV() {
    return efts;
  }

  @Override
  public void setExpectedFulfillmentTimeRangesCSV(String expectedFulfillmentTimesCSV) {
    efts = expectedFulfillmentTimesCSV;
  }

  // Get/set the confirmed fulfillment time ranges, in UTC
  @Override
  public Date getConfirmedFulfillmentTimeStart() {
    return cfts;
  }

  @Override
  public Date getConfirmedFulfillmentTimeEnd() {
    return cfte;
  }

  @Override
  public String getConfirmedFulfillmentTimeRange() {
    if (cfts == null) {
      return null;
    }
    String str = LocalDateUtil.format(cfts, null, null); // UTC time
    if (cfte != null) {
      str += '-' + LocalDateUtil.format(cfte, null, null);
    }
    return str;
  }

  @Override
  public void setConfirmedFulfillmentTimeRange(String confirmedFulfillmentTimeRange) {
    if (confirmedFulfillmentTimeRange != null && !confirmedFulfillmentTimeRange.isEmpty()) {
      try {
        Map<String, Date>
            timeRange =
            LocalDateUtil.parseTimeRange(confirmedFulfillmentTimeRange, null,
                null); // no locale/timezone given UTC time
        if (timeRange != null) {
          Date start = timeRange.get(JsonTagsZ.TIME_START);
          if (start != null) {
            cfts = start;
          } else {
            cfts = null;
          }
          Date end = timeRange.get(JsonTagsZ.TIME_END);
          if (end != null) {
            cfte = end;
          } else {
            cfte = null;
          }
        }
      } catch (ParseException e) {
        xLogger.warn("Parsing exception for confirmed fulfillment time range {0} in order {1}: {2}",
            confirmedFulfillmentTimeRange, getOrderId(), e.getMessage());
      }
    } else {
      cfts = cfte = null;
    }
  }

  // Get/set the payment option
  @Override
  public String getPaymentOption() {
    return popt;
  }

  @Override
  public void setPaymentOption(String popt) {
    this.popt = popt;
  }

  @Override
  public List<String> getTags(String tagType) {
    if (TagUtil.TYPE_ORDER.equals(tagType)) {
      if (oldotgs == null) {
        oldotgs = TagUtil.getList(otgs);
      }
      return oldotgs;
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
    if (TagUtil.TYPE_ORDER.equals(tagType)) {
      oldotgs = tags;
    } else if (TagUtil.TYPE_ENTITY.equals(tagType)) {
      oldktgs = tags;
    }
  }

  @Override
  public void setTgs(List<? extends ITag> tags, String tagType) {
    if (TagUtil.TYPE_ORDER.equals(tagType)) {
      if (this.otgs != null) {
        this.otgs.clear();
        if (tags != null) {
          this.otgs.addAll((Collection<Tag>) tags);
        }
      } else {
        this.otgs = (List<Tag>) tags;
      }
      this.oldotgs = null;
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
  @SuppressWarnings("rawtypes")
  public Map<String, Object> toMap(boolean includeItems, Locale locale, String timezone,
                                   boolean forceIntegerQuantity, boolean includeAccountingData,
                                   boolean includeShipmentItems) {
    Map<String, Object> map = new HashMap<>(1);
    map.put(JsonTagsZ.TRACKING_ID, String.valueOf(this.id));
    map.put(JsonTagsZ.KIOSK_ID, kId.toString());
    map.put(JsonTagsZ.ORDER_STATUS, st);
    int q = 0;
    if (items != null) {
      q = items.size();
    }
    map.put(JsonTagsZ.QUANTITY, String.valueOf(q));

    map.put(JsonTagsZ.TIMESTAMP, LocalDateUtil.format(cOn, locale, timezone));
    if (uOn != null) {
      map.put(JsonTagsZ.UPDATED_TIME, LocalDateUtil.format(uOn, locale, timezone));
    }
    if (tp != null) {
      map.put(JsonTagsZ.TOTAL_PRICE, getFormattedPrice());
    }
    if (cr != null) {
      map.put(JsonTagsZ.CURRENCY, cr);
    }
    if (tx != null && BigUtil.greaterThanZero(tx)) {
      map.put(JsonTagsZ.TAX, tx.toString());
    }
    if (skId != null) {
      map.put(JsonTagsZ.VENDORID, skId.toString());
    }
    if (edd != null) {
      map.put(JsonTagsZ.REQUIRED_BY_DATE, LocalDateUtil.format(edd, locale, timezone));
    }
    if (ead != null) {
      map.put(JsonTagsZ.EXPECTED_TIME_OF_ARRIVAL, LocalDateUtil.format(ead, locale, timezone));
    }
    if (pd != null && BigUtil.notEqualsZero(pd)) {
      map.put(JsonTagsZ.PAYMENT, String.valueOf(pd));
    }
    if (popt != null && !popt.isEmpty()) {
      map.put(JsonTagsZ.PAYMENT_OPTION, popt);
    }
    if (cdrsn != null && !cdrsn.isEmpty()) {
      map.put(JsonTagsZ.REASONS_FOR_CANCELLING_ORDER, cdrsn);
    }
//		if ( ms != null ) // Send message
//			ht.put( JsonTagsZ.MESSAGE, ms );
    if (includeItems && items != null && !items.isEmpty()) {
      IDemandService ds = null;
      try {
        ds = Services.getService(DemandService.class,locale);
        // Add items
        List<Map>
            materials =
            ds.getDemandItems(items, cr, locale, timezone, forceIntegerQuantity);
        if (materials != null) {
          map.put(JsonTagsZ.MATERIALS, materials);
        }
      } catch (ServiceException e) {
        xLogger.warn("Unable to add materials, since demand service initialisation failed",e);
      }
    }
    // Include accounting data (credit limit, available credit)
    if (includeAccountingData && skId != null) {
      try {
        IAccountingService accountingService = Services.getService(AccountingServiceImpl.class);
        CreditData
            cd =
            accountingService.getCreditData(kId, skId, DomainConfig.getInstance(getDomainId()));
        if (BigUtil.notEqualsZero(cd.creditLimit)) {
          map.put(JsonTagsZ.CREDIT_LIMIT, String.valueOf(cd.creditLimit));
          map.put(JsonTagsZ.PAYABLE, String.valueOf(cd.creditLimit.subtract(cd.availabeCredit)));
        }
      } catch (Exception e) {
        xLogger
            .warn("{0} when trying to get credit data ({1}-{2}) for order {3} in domain {4}: {5}",
                e.getClass().getName(), kId, skId, id, getDomainId(), e.getMessage());
      }
    }
    if (otgs != null && otgs.size() > 0) {
      map.put(JsonTagsZ.TAGS, TagUtil.getTagCSV(otgs));
    }
    // Check custom IDs and include
    try {
      EntitiesService as = Services.getService(EntitiesServiceImpl.class);
      // Kiosk
      IKiosk k = as.getKiosk(kId, false);
      String customKioskId = k.getCustomId();
      if (customKioskId != null && !customKioskId.isEmpty()) {
        map.put(JsonTagsZ.CUSTOM_KIOSKID, customKioskId);
      }
      // Vendor
      if (skId != null) {
        IKiosk vendor = as.getKiosk(skId, false);
        String customVendorId = vendor.getCustomId();
        if (customVendorId != null && !customVendorId.isEmpty()) {
          map.put(JsonTagsZ.CUSTOM_VENDORID, customVendorId);
        }
      }
      // User
      if (uId != null) {
        IUserAccount user = Services.getService(UsersServiceImpl.class).getUserAccount(uId);
        String customUserId = user.getCustomId();
        if (customUserId != null && !customUserId.isEmpty()) {
          map.put(JsonTagsZ.CUSTOM_USERID, customUserId);
        }
      }
    } catch (Exception e) {
      xLogger.severe("{0} when getting custom ID for order {1}: {2}", e.getClass().getName(),
          getOrderId(), e.getMessage());
    }
    // If a transporter has been set for an order, put it into the ht.
//		String transporterName =  getTransporterName();
//		if ( transporterName != null && !transporterName.isEmpty() ) {
//			ht.put( JsonTagsZ.TRANSPORTER, transporterName );
//		}
    return map;
  }

  // Name of discount type
  @Override
  public String getDiscountTypeName() {
    if (getDiscountType() == DISCOUNT_PERCENTAGE) {
      return "%";
    }
    return "";
  }

  // Update payment history for this order
  private void updatePaymentHistory(BigDecimal paid) {
    try {
      JSONObject json = null;
      JSONArray array = null;
      if (ph != null) {
        json = new JSONObject(ph);
        array = json.getJSONArray(TIMELINE);
      } else {
        json = new JSONObject();
        array = new JSONArray();
        json.put(TIMELINE, array);
      }
      JSONObject h = new JSONObject();
      h.put(AMOUNT, String.valueOf(paid));
      h.put(TIME, (new Date()).getTime());
      array.put(h);
      ph = json.toString();
    } catch (Exception e) {
      xLogger.severe("Exception {0} when updating payment history for order {1}: {2}",
          e.getClass().getName(), id, e.getMessage());
    }
  }

  // Update accounting associated with this order
  private void doAccounting(BigDecimal payable, BigDecimal paid) {
    if (BigUtil.equalsZero(payable) && BigUtil.equalsZero(paid)) {
      return;
    }
    DomainConfig dc = DomainConfig.getInstance(getDomainId());
    if (!dc.isAccountingEnabled()) // proceed only if accounting is enabled
    {
      return;
    }
    // Update accounts
    if (getServicingKiosk() == null) { // no vendor
      xLogger.warn("Cannot update accounting info. given vendor is not available for order {0}",
          getOrderId());
    } else {
      // Update payable in accounts
      try {
        IAccountingService os = Services.getService(AccountingServiceImpl.class);
        os.updateAccount(getDomainId(), getServicingKiosk(), getKioskId(),
            LocalDateUtil.getCurrentYear(), payable, paid);
      } catch (Exception e) {
        xLogger.severe(
            "Unable to update accounting when updating order {0}: vendor {1}, customer {2}, payable {3}, paid {4}",
            getOrderId(), getServicingKiosk(), getKioskId(), payable, paid);
      }
    }
  }

  // Propagate status to demand items
  private void propagateStatus() {
    xLogger.fine("Entered propagateStatus");
    if (items == null || items.isEmpty()) {
      return;
    }
    String status = getStatus();
    Iterator<DemandItem> it = items.iterator();
    while (it.hasNext()) {
      IDemandItem di = it.next();
      di.setStatus(status);
    }
    xLogger.fine("Exiting propagateStatus");
  }

  @Override
  public String getIdString() {
    return String.valueOf(id);
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

  public Integer getOrderType() {
    return oty;
  }

  public void setOrderType(Integer orderType) {
    oty = orderType;
  }

  public String getReferenceID() {
    return rid;
  }

  public void setReferenceID(String rid) {
    this.rid = rid;
  }

  @Override
  public Long getLinkedDomainId() {
    try {
      if (skId != null) {
        EntitiesService as = Services.getService(EntitiesServiceImpl.class);
        return as.getKiosk(getServicingKiosk(), false).getDomainId();
      }
    } catch (Exception e) {
      xLogger
          .warn("Error when trying to get linked kiosk {0} for kiosk link {1}", getServicingKiosk(),
              getKioskId(), e);
    }
    return null;
  }

  @Override
  public Long getKioskDomainId() {
    try {
      if (kId != null) {
        EntitiesService as = Services.getService(EntitiesServiceImpl.class);
        return as.getKiosk(getKioskId(), false).getDomainId();
      }
    } catch (Exception e) {
      xLogger.warn("Error when trying to get kiosk {0} for kiosk link {1}", getKioskId(),
          getServicingKiosk(), e);
    }
    return null;
  }

  @Override
  public String getCancelledDiscrepancyReason() {
    return cdrsn;
  }

  @Override
  public void setCancelledDiscrepancyReason(String cdrsn) {
    this.cdrsn = cdrsn;
  }

  @Override
  public String getPaymentHistory(){
    return this.ph;
  }
}

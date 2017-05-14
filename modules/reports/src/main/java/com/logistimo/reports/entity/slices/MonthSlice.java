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

import com.logistimo.utils.LocalDateUtil;
import com.logistimo.utils.NumberUtil;
import com.logistimo.logger.XLog;

import java.math.BigDecimal;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import javax.jdo.annotations.Cacheable;
import javax.jdo.annotations.Extension;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;

/**
 * Represents a slice of monthly aggregated transaction data (quantities and trans. counts) per object over a given dimension
 *
 * @author Arun
 */
@PersistenceCapable(identityType = IdentityType.APPLICATION, detachable = "true")
@Cacheable("false")
public class MonthSlice extends Slice implements IMonthSlice {
  private static final float AVG_DAYS_PER_MONTH = 30.44F;

  // Stock event response times
  // Stock outs
  @Persistent
  @Extension(vendorName = "datanucleus", key = "gae.unindexed", value = "true")
  protected Float sod; // stock out duration (in hours)
  @Persistent
  @Extension(vendorName = "datanucleus", key = "gae.unindexed", value = "true")
  protected Integer soc; // stock-out counts - num. stock-outs in the above duration
  // < min.
  @Persistent
  @Extension(vendorName = "datanucleus", key = "gae.unindexed", value = "true")
  protected Float lmd; // stock out duration
  @Persistent
  @Extension(vendorName = "datanucleus", key = "gae.unindexed", value = "true")
  protected Integer lmc; // stock-out counts - num. stock-outs in the above duration
  // > max.
  @Persistent
  @Extension(vendorName = "datanucleus", key = "gae.unindexed", value = "true")
  protected Float gmd; // stock out duration
  @Persistent
  @Extension(vendorName = "datanucleus", key = "gae.unindexed", value = "true")
  protected Integer gmc; // stock-out counts - num. stock-outs in the above duration
  @Persistent
  @Extension(vendorName = "datanucleus", key = "gae.unindexed", value = "true")
  protected Float opt; // order processing time (in hours)
  @Persistent
  @Extension(vendorName = "datanucleus", key = "gae.unindexed", value = "true")
  protected Integer opc; // order processing count (= num. shipped orders)
  @Persistent
  @Extension(vendorName = "datanucleus", key = "gae.unindexed", value = "true")
  protected Float dlt; // delivery lead time (in hours)
  @Persistent
  @Extension(vendorName = "datanucleus", key = "gae.unindexed", value = "true")
  protected Integer dlc; // delivery lead count (= num. fulfilled orders)

  private static float getDurationMonthlyAverageHours(long durationMillis) {
    float durationDays = durationMillis / 86400000F;
    if (durationDays <= AVG_DAYS_PER_MONTH) {
      return durationMillis / 3600000F;
    }
    float numMonths = Math.round(durationDays / AVG_DAYS_PER_MONTH);
    return (durationDays / numMonths) * 24F; // hours
  }

  public static Date createTimestamp(Date t) {
    Calendar cal = GregorianCalendar.getInstance();
    cal.setTime(t);
    cal.set(Calendar.DATE, 1); // reset date to the first of the month
    LocalDateUtil.resetTimeFields(cal);
    return cal.getTime();
  }

  @Override
  public float getAverageStockoutResponseTime() { // days
    if (soc == null || soc == 0 || sod == null) {
      return 0F;
    }
    return NumberUtil.round2((sod / soc) / 24F);
  }

  @Override
  public float getAverageLessThanMinResponseTimeAverage() { // days
    if (lmc == null || lmc == 0 || lmd == null) {
      return 0F;
    }
    return NumberUtil.round2((lmd / lmc) / 24F);
  }

  @Override
  public float getAverageMoreThanMaxResponse() { // days
    if (gmc == null || gmc == 0 || gmd == null) {
      return 0;
    }
    return NumberUtil.round2((gmd / gmc) / 24F);
  }

  @Override
  public float getAverageOrderProcessingTime() { // days
    if (opt == null || opc == null || opc == 0) {
      return 0F;
    }
    return NumberUtil.round2((opt / opc) / 24F);
  }

  @Override
  public float getAverageOrderDeliveryTime() { // days
    if (dlt == null || dlc == null || dlc == 0) {
      return 0F;
    }
    return NumberUtil.round2((dlt / dlc) / 24F);
  }

  public Float getSod() {
    return sod;
  }

  public Integer getSoc() {
    return soc;
  }

  public Float getLmd() {
    return lmd;
  }

  public Integer getLmc() {
    return lmc;
  }

  public Float getGmd() {
    return gmd;
  }

  public Integer getGmc() {
    return gmc;
  }

  public Float getOpt() {
    return opt;
  }

  public Integer getOpc() {
    return opc;
  }

  public Float getDlt() {
    return dlt;
  }

  public Integer getDlc() {
    return dlc;
  }
}

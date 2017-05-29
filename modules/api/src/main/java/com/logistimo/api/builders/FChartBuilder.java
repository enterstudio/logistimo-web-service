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

package com.logistimo.api.builders;

import com.logistimo.dashboards.entity.IWidget;
import com.logistimo.inventory.dao.IInvntryDao;
import com.logistimo.inventory.dao.impl.InvntryDao;
import com.logistimo.inventory.entity.IInvntry;
import com.logistimo.reports.ReportsConstants;
import com.logistimo.reports.entity.slices.Counts;
import com.logistimo.reports.entity.slices.ISlice;
import com.logistimo.reports.entity.slices.IReportsSlice;
import com.logistimo.reports.service.ReportsService;
import com.logistimo.reports.service.impl.ReportsServiceImpl;
import com.logistimo.services.utils.ConfigUtil;

import com.logistimo.reports.models.DomainCounts;
import com.logistimo.pagination.PageParams;
import com.logistimo.pagination.Results;
import com.logistimo.reports.generators.ReportData;
import com.logistimo.services.ServiceException;
import com.logistimo.services.Services;
import com.logistimo.api.constants.AggregationConstants;
import com.logistimo.utils.BigUtil;
import com.logistimo.constants.CharacterConstants;
import com.logistimo.utils.LocalDateUtil;
import com.logistimo.utils.NumberUtil;
import com.logistimo.api.models.FChartModel;

import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Created by Mohan Raja on 19/02/15
 */
public class FChartBuilder {

  private static final int PREDICTIVE_FUTURE_DAYS = ConfigUtil.getInt("predictive.future.days", 30);
  private IInvntryDao invDao = new InvntryDao();

  public List<FChartModel> buildConsumptionChartModel(ReportData r, String repGenTime) {
    Iterator<? extends ISlice> it = r.getResults().iterator();
    ISlice nextSlice = null;
    if (it.hasNext()) {
      nextSlice = it.next();
    }
    List<FChartModel> models = new ArrayList<>(r.getResults().size());
    while (nextSlice != null) {
      IReportsSlice slice = (IReportsSlice) nextSlice;
      if (it.hasNext()) {
        nextSlice = it.next();
      } else {
        nextSlice = null;
      }
      if (slice.getDate().getTime() == r.getFromDate().getTime()) {
        continue;
      }
      FChartModel model = new FChartModel();
      Calendar cal = GregorianCalendar.getInstance();
      cal.setTime(slice.getDate());
      model.repGenTime = repGenTime;
      model.label =
          cal.get(Calendar.YEAR) + "-" + (cal.get(Calendar.MONTH) + 1) + "-" + cal
              .get(Calendar.DAY_OF_MONTH);
      List<String> value = new ArrayList<>();
      value.add(String.valueOf(BigUtil.round2(slice.getIssueQuantity())));
      value.add(String.valueOf(BigUtil.round2(slice.getReceiptQuantity())));
      value.add(String.valueOf(BigUtil.round2(slice.getWastageQuantity())));
      value.add(String.valueOf(BigUtil.round2(slice.getStockQuantity())));
      value.add(String.valueOf(BigUtil.round2(slice.getStockDifference())));
      BigDecimal min = BigDecimal.ZERO, max = BigDecimal.ZERO;
      if (ReportsConstants.FILTER_KIOSK.equals(slice.getDimensionType())) {
        try {
          Long kioskId = Long.valueOf(slice.getDimensionValue());
          IInvntry inv = invDao.findId(kioskId, Long.valueOf(slice.getObjectId()));
          min = inv.getNormalizedSafetyStock();
          max = inv.getMaxStock();
        } catch (Exception ignored) {
          // ignore
        }
      }
      value.add(String.valueOf(BigUtil.round2(min)));
      value.add(String.valueOf(BigUtil.round2(max)));
      value.add(String.valueOf(BigUtil.round2(slice.getTransferQuantity())));
      value.add(
          String.valueOf(nextSlice != null ? BigUtil.round2(nextSlice.getStockQuantity()) : 0F));
      value.add(String.valueOf(BigUtil.round2(slice.getDemandQuantity())));
      model.value = value;
      models.add(model);
    }
    return models;
  }

  public List<FChartModel> buildORTChartModel(ReportData r, String repGenTime) {
    Iterator<? extends ISlice> it = r.getResults().iterator();
    ISlice nextSlice = null;
    if (it.hasNext()) {
      nextSlice = it.next();
    }
    List<FChartModel> models = new ArrayList<FChartModel>(r.getResults().size());
    while (nextSlice != null) {
      IReportsSlice monthSlice = (IReportsSlice) nextSlice;
      FChartModel model = new FChartModel();
      Calendar cal = GregorianCalendar.getInstance();
      cal.setTime(nextSlice.getDate());
      model.repGenTime = repGenTime;
      model.label =
          cal.get(Calendar.YEAR) + "-" + (cal.get(Calendar.MONTH) + 1) + "-" + cal
              .get(Calendar.DAY_OF_MONTH);
      List<String> value = new ArrayList<String>();
      value.add(NumberUtil.getFormattedValue(monthSlice.getAverageOrderProcessingTime()));
      value.add(NumberUtil.getFormattedValue(monthSlice.getAverageOrderDeliveryTime()));
      model.value = value;
      models.add(model);
      if (it.hasNext()) {
        nextSlice = it.next();
      } else {
        nextSlice = null;
      }
    }
    return models;
  }

  public List<FChartModel> buildRRTChartModel(ReportData r, String repGenTime) {
    Iterator<? extends ISlice> it = r.getResults().iterator();
    ISlice nextSlice = null;
    if (it.hasNext()) {
      nextSlice = it.next();
    }
    List<FChartModel> models = new ArrayList<FChartModel>(r.getResults().size());
    while (nextSlice != null) {
      IReportsSlice monthSlice = (IReportsSlice) nextSlice;
      FChartModel model = new FChartModel();
      Calendar cal = GregorianCalendar.getInstance();
      cal.setTime(nextSlice.getDate());
      model.repGenTime = repGenTime;
      model.label =
          cal.get(Calendar.YEAR) + "-" + (cal.get(Calendar.MONTH) + 1) + "-" + cal
              .get(Calendar.DAY_OF_MONTH);
      List<String> value = new ArrayList<String>();
      value.add(NumberUtil.getFormattedValue(monthSlice.getAverageStockoutResponseTime()));
      value
          .add(NumberUtil.getFormattedValue(monthSlice.getAverageLessThanMinResponseTimeAverage()));
      value.add(NumberUtil.getFormattedValue(monthSlice.getAverageMoreThanMaxResponse()));
      model.value = value;
      models.add(model);
      if (it.hasNext()) {
        nextSlice = it.next();
      } else {
        nextSlice = null;
      }
    }
    return models;
  }

  public List<FChartModel> buildTCChartModel(ReportData r, String repGenTime) {
    Iterator<? extends ISlice> it = r.getResults().iterator();
    ISlice nextSlice = null;
    if (it.hasNext()) {
      nextSlice = it.next();
    }
    List<FChartModel> models = new ArrayList<FChartModel>(r.getResults().size());
    Calendar prevCal = null;
    Counts counts = new Counts();

    while (nextSlice != null) {
      Calendar cal = GregorianCalendar.getInstance();
      cal.setTime(nextSlice.getDate());
      if (prevCal == null || LocalDateUtil.compareDates(cal, prevCal) == 0) {
        aggregateCounts(counts, nextSlice);
        prevCal = cal;
      } else {
        models.add(getFChartModel(counts, prevCal, repGenTime));
        counts = new Counts();
        aggregateCounts(counts, nextSlice);
        prevCal = cal;
      }
      if (it.hasNext()) {
        nextSlice = it.next();
      } else {
        models.add(getFChartModel(counts, cal, repGenTime));
        nextSlice = null;
      }
    }
    return models;
  }

  public List<FChartModel> buildUAChartModel(ReportData r, String repGenTime) {
    Iterator<? extends ISlice> it = r.getResults().iterator();
    List<FChartModel> models = new ArrayList<FChartModel>(r.getResults().size());
    while (it.hasNext()) {
      ISlice slice = it.next();
      Calendar cal = GregorianCalendar.getInstance();
      cal.setTime(slice.getDate());
      FChartModel model = new FChartModel();
      model.repGenTime = repGenTime;
      model.label =
          cal.get(Calendar.YEAR) + "-" + (cal.get(Calendar.MONTH) + 1) + "-" + cal
              .get(Calendar.DAY_OF_MONTH);
      List<String> value = new ArrayList<String>();
      value.add(NumberUtil.getFormattedValue(slice.getLoginCounts()));
      value.add(NumberUtil.getFormattedValue(slice.getIssueCount()));
      value.add(NumberUtil.getFormattedValue(slice.getReceiptCount()));
      value.add(NumberUtil.getFormattedValue(slice.getStockcountCount()));
      value.add(NumberUtil.getFormattedValue(slice.getWastageCount()));
      value.add(NumberUtil.getFormattedValue(slice.getOrderCount()));
      value.add(String.valueOf(BigUtil.round2(slice.getRevenueBooked())));
      model.value = value;
      models.add(model);
    }
    return models;
  }

  private FChartModel getFChartModel(Counts counts, Calendar cal, String repGenTime) {
    FChartModel model = new FChartModel();
    model.repGenTime = repGenTime;
    model.label =
        cal.get(Calendar.YEAR) + "-" + (cal.get(Calendar.MONTH) + 1) + "-" + cal
            .get(Calendar.DAY_OF_MONTH);
    List<String> value = new ArrayList<String>();
    value.add(String.valueOf(counts.i));
    value.add(String.valueOf(counts.r));
    value.add(String.valueOf(counts.s));
    value.add(String.valueOf(counts.w));
    value.add(String.valueOf(counts.o));
    value.add(String.valueOf(counts.tr));
    model.value = value;
    return model;
  }

  private void aggregateCounts(Counts counts, ISlice tds) {
    counts.i += tds.getIssueCount();
    counts.r += tds.getReceiptCount();
    counts.s += tds.getStockcountCount();
    counts.o += tds.getDemandCount();
    counts.w += tds.getWastageCount();
    counts.tr += tds.getTransferCount();
  }

  public List<FChartModel> buildHDashboardChartModel(DomainCounts counts, String periodType,
                                                     Boolean isCurrentMonth, String repGenTime) {
    if (counts != null && counts.getCounts() != null) {
      List<FChartModel> modelList = new ArrayList<FChartModel>();
      List<DomainCounts.Counts> countsList = counts.getCounts();
      SimpleDateFormat simpleDateFormat;
      if (periodType.equals("monthly")) {
        simpleDateFormat = new SimpleDateFormat("MMM yyyy");
      } else {
        simpleDateFormat = new SimpleDateFormat("MMM dd, yyyy");
      }
      for (DomainCounts.Counts c : countsList) {
        FChartModel model = new FChartModel();
        model.repGenTime = repGenTime;
        model.label = simpleDateFormat.format(new Date(c.getT()));
        model.value.add(Integer.toString(c.getTrans()));
        model.value.add(Integer.toString(c.getTransUsrs()));
        model.value.add(Integer.toString(c.getTransEnts()));
        model.value.add(c.getRvn().toString());
        model.value.add(Integer.toString(c.getOrdrs()));
        model.value.add(Integer.toString(c.getFulOrdrs()));
        model.value.add(Integer.toString(c.getPndgOrdrs()));
        model.value.add(Double.toString(c.getOrdrRspTime()));
        model.value.add(Double.toString(c.getStckOuts()));
        model.value.add(Double.toString(c.getLessThanMin()));
        model.value.add(Double.toString(c.getGrtThanMax()));
        model.value.add(Double.toString(c.getRplRspTime()));
        if (!isCurrentMonth) {
          model.value.add(Double.toString(c.getTransChngPer()));
          model.value.add(Double.toString(c.getTransUsrsChngPer()));
          model.value.add(Double.toString(c.getTransEntsChngPer()));
          model.value.add(Double.toString(c.getRvnChngPer()));
          model.value.add(Double.toString(c.getOrdrsChngPer()));
          model.value.add(Double.toString(c.getFulOrdrsChngPer()));
          model.value.add(Double.toString(c.getPndgOrdrsChngPer()));
          model.value.add(Double.toString(c.getOrdrRspTimeChngPer()));
          model.value.add(Double.toString(c.getStckOutsChngPer()));
          model.value.add(Double.toString(c.getLessThanMinChngPer()));
          model.value.add(Double.toString(c.getGrtThanMaxChngPer()));
          model.value.add(Double.toString(c.getRplRspTimeChngPer()));
        }
        modelList.add(model);
      }
      return modelList;
    }
    return null;
  }

  @SuppressWarnings("unchecked")
  public List<FChartModel> getAggrData(Long did, String fq, int period, String aggregation)
      throws ServiceException {
    List<FChartModel> models = new ArrayList<>(period);
    ReportsService rs = Services.getService(ReportsServiceImpl.class);
    SimpleDateFormat simpleDateFormat;
    String freq = IWidget.MONTHLY.equals(fq) ? ISlice.MONTHLY : ISlice.DAILY;
    if (ISlice.MONTHLY.equals(freq)) {
      simpleDateFormat = new SimpleDateFormat("MMM yyyy");
    } else {
      simpleDateFormat = new SimpleDateFormat("MMM dd, yyyy");
    }
    Results
        r =
        rs.getSlices(new Date(), freq, "d", String.valueOf(did), "dmn", String.valueOf(did), true,
            new PageParams(period));
    Iterable<ISlice> slices = (List<ISlice>) r.getResults();
    for (ISlice slice : slices) {
      FChartModel model = new FChartModel();
      model.label = simpleDateFormat.format(slice.getDate());
      if (AggregationConstants.TRANSACTION.equals(aggregation)) {
        model.value.add(Integer.toString(slice.getTotalCount()));
      } else if (AggregationConstants.ACTIVE_USERS.equals(aggregation)) {
        model.value.add(Integer.toString(slice.getTransactiveUserCounts()));
      } else if (AggregationConstants.ACTIVE_ENTITY.equals(aggregation)) {
        model.value.add(Integer.toString(slice.getTransactiveEntityCounts()));
      } else if (AggregationConstants.REVENUE.equals(aggregation)) {
        model.value.add(slice.getRevenueBooked().toString());
      } else if (AggregationConstants.ORDERS.equals(aggregation)) {
        model.value.add(Integer.toString(slice.getOrderCount()));
      } else if (AggregationConstants.FULFILLED_ORDERS.equals(aggregation)) {
        model.value.add(Integer.toString(slice.getFulfilledOrderCount()));
      } else if (AggregationConstants.PENDING_ORDERS.equals(aggregation)) {
        model.value.add(Integer.toString(slice.getPendingOrderCount()));
      } else if (AggregationConstants.ORDER_RESPONSE_TIME.equals(aggregation)) {
        model.value.add(Integer.toString(0));
      } else if (AggregationConstants.STOCK_OUT_EVENT.equals(aggregation)) {
        model.value.add(Integer.toString(slice.getStockoutEventCounts()));
      } else if (AggregationConstants.MIN_EVENT.equals(aggregation)) {
        model.value.add(Integer.toString(slice.getLessThanMinEventCounts()));
      } else if (AggregationConstants.MAX_EVENT.equals(aggregation)) {
        model.value.add(Integer.toString(slice.getGreaterThanMaxEventCounts()));
      } else if (AggregationConstants.REPLENISHMENT_TIME.equals(aggregation)) {
        model.value.add(Integer.toString(0));
      }
      models.add(model);
      if (period == models.size()) {
        break;
      }
    }
    return models;
  }

  public List<FChartModel> buildPredChartModel(List<ISlice> slices,
                                               Map<String, BigDecimal> ordersMap,
                                               BigDecimal consumptionRate, BigDecimal stock)
      throws ParseException {
    List<FChartModel> models = new ArrayList<>();
    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MMM dd, yyyy");
    if (slices != null) {
      for (int i = slices.size() - 1; i >= 0; i--) {
        ISlice slice = slices.get(i);
        FChartModel m = new FChartModel();
        m.label = simpleDateFormat.format(slice.getDate());
        m.value.add(String.valueOf(BigUtil.round2(slice.getStockQuantity())));
        m.value.add(CharacterConstants.EMPTY); // For Predictive
        models.add(m);
      }
    }
    FChartModel currentDay = new FChartModel();
    currentDay.label = simpleDateFormat.format(new Date());
    currentDay.value.add(String.valueOf(BigUtil.round2(stock)));
    currentDay.value.add(String.valueOf(BigUtil.round2(stock)));
    models.add(currentDay);

    Calendar predCal = new GregorianCalendar();
    SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
    SimpleDateFormat sdfDay = new SimpleDateFormat("yyyyMMdd");
    Map<String, List<String>> orders = new HashMap<>(ordersMap.size());
    for (String s : ordersMap.keySet()) {
      String d = sdfDay.format(sdf.parse(s));
      if (orders.containsKey(d)) {
        orders.get(d).add(s);
      } else {
        List<String> dList = new ArrayList<>(1);
        dList.add(s);
        orders.put(d, dList);
      }
    }
    int zeroStart = -1;
    int cInd = models.size();
    for (int i = 0; i < PREDICTIVE_FUTURE_DAYS; i++) {
      predCal.add(Calendar.DAY_OF_MONTH, 1);
      String pDate = sdfDay.format(predCal.getTime());
      FChartModel predDay = new FChartModel();
      predDay.label = simpleDateFormat.format(predCal.getTime());
      if (orders.containsKey(pDate)) {
        for (String dates : orders.get(pDate)) {
          stock = stock.add(ordersMap.get(dates));
        }
      }
      stock = stock.subtract(consumptionRate);
      if (BigUtil.lesserThanEqualsZero(stock)) {
        stock = BigDecimal.ZERO;
        if (zeroStart == -1) {
          zeroStart = i;
        }
      } else {
        zeroStart = -1;
      }
      predDay.value.add(CharacterConstants.EMPTY);
      predDay.value.add(String.valueOf(BigUtil.round2(stock)));
      models.add(predDay);
    }
    if (zeroStart != -1) {
      models = models.subList(0, zeroStart + cInd + 1);
    }
    Collections.reverse(models);
    return models;
  }
}

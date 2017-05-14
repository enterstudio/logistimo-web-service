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

import com.logistimo.config.models.DashboardConfig;
import com.logistimo.reports.entity.slices.ISlice;

import com.logistimo.reports.entity.slices.IReportsSlice;
import com.logistimo.utils.BigUtil;
import com.logistimo.utils.Counter;
import com.logistimo.models.ICounter;
import com.logistimo.logger.XLog;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class DomainCounts {
  // Logger
  private static final XLog xLogger = XLog.getLog(DomainCounts.class);

  @Expose
  private Long dId = null; // domain id
  @Expose
  private List<Counts> counts; // List of Counts objects


  public DomainCounts(Long domainId) {
    this.dId = domainId;
  }

  public DomainCounts(long domainId, List<ISlice> slices) {
    this(domainId);
    this.counts = new ArrayList<Counts>();
    if (slices != null && !slices.isEmpty()) {
      xLogger.fine("Inside DomainCounts constructor, slices: {0}", slices.toString());
      initCountsFromSlices(slices);
    }
  }

  public String toJSONString() {
    Gson gson = new Gson();
    return gson.toJson(this);

  }

  public List<Counts> getCounts() {
    return this.counts;
  }

  public void removeLastElement() {
    if (counts != null && !counts.isEmpty()) {
      counts.remove(counts.size() - 1);
    }
  }

  private void initCountsFromSlices(List<ISlice> slices) {
    if (slices == null || slices.isEmpty()) {
      return;
    }
    // Iterate through the slices and construct a Counts object from it and add it to the counts array.
    int n = slices.size();
    int i = n - 2;
    ISlice prevSlice = slices.get(n - 1);

    Counts count = new Counts(prevSlice);
    counts.add(count);
    while (i >= 0) {
      ISlice curSlice = slices.get(i);
      Counts cnt = new Counts(curSlice, prevSlice);
      counts.add(cnt);
      prevSlice = curSlice;
      i--;
    }
    // Sort the counts in descending order
    Collections.sort(counts, Counts.CountsDateComparator);
  }

  public static class Counts {
    public static Comparator<Counts> CountsDateComparator = new Comparator<Counts>() {

      public int compare(Counts c1, Counts c2) {
        // Descending order
        long d1 = c1.t;
        long d2 = c2.t;
        if (d2 < d1) {
          return -1;
        } else if (d1 == d2) {
          return 0;
        }
        return 1;

      }
    };
    @Expose
    private long t = 0; // timestamp in milliseconds
    @Expose
    private int trans = 0; // Total number of transactions
    @Expose
    private double transChngPer = 0; // Percentage change since the last period
    @Expose
    private int totalUsrs = 0; // Total number of Users (enabled users) in the domain
    @Expose
    private int totalLns = 0; // Number of logins in the domain
    @Expose
    private int
        transUsrs =
        0;
    // Number of transactive users i.e. users who have made atleast one transaction or order.
    @Expose
    private double
        transUsrsChngPer =
        0;
    // Percentage Change in the number of active users from the last period
    @Expose
    private int
        totalEnts =
        0;
    // Total number of entities in the domain (TODO: this should include only active entities)
    @Expose
    private int
        transEnts =
        0;
    // Number of transactive entities in the domain, i.e. entities with at least one transaction or order
    @Expose
    private double
        transEntsChngPer =
        0;
    // Percentage Change in the number of transactive users from the last period.
    @Expose
    private int
        actInvItems =
        0;
    // Number of active inventory items in the domain (this metric shows the inventory movement)
    @Expose
    private int totalInvItems = 0; // Total Number of inventory items in the domain
    @Expose
    private double
        actInvItemsChngPer =
        0;
    // Percentage Change in the number of inventory items from the last period
    @Expose
    private BigDecimal rvn = BigDecimal.ZERO; // Revenue for the domain in this period
    @Expose
    private double rvnChngPer = 0; // Percentage Change in the revenue from the pervious period.
    @Expose
    private int numEntsExcCrLmt = 0; // Number of entities exceeding credit limit
    @Expose
    private double
        numEntsExcCrLmtChngPer =
        0;
    // Percentage Change in the number of entities exceeding credit limit since the last period.
    @Expose
    private int ordrs = 0; // NUmber of orders
    @Expose
    private double
        ordrsChngPer =
        0;
    // Percentage Change in the number of orders since the last period
    @Expose
    private int fulOrdrs = 0; // Number of fulfilled orders
    @Expose
    private double
        fulOrdrsChngPer =
        0;
    // Percentage Change in the Number of fulfilled orders since the last period
    @Expose
    private int pndgOrdrs = 0; // Number of pending orders
    @Expose
    private double
        pndgOrdrsChngPer =
        0;
    // Percentage Change in the Number of pending orders since the last period
    @Expose
    private double ordrRspTime = 0; // Order Response Time
    @Expose
    private double
        ordrRspTimeChngPer =
        0;
    // Percentage Change in the Order Response Time since the last period
    @Expose
    private int stckOuts = 0; // Number of Stock Out events
    @Expose
    private double
        stckOutsChngPer =
        0;
    // Percentage Change in the number of stockout events since the last period.
    @Expose
    private int lessThanMin = 0; // Number of Less than min
    @Expose
    private double
        lessThanMinChngPer =
        0;
    // Percentage Change in the number of less than min events since the last period.
    @Expose
    private int grtThanMax = 0; // Number of greater than max events
    @Expose
    private double
        grtThanMaxChngPer =
        0;
    // Percentage Change in the Number of greater than max events since the last period.
    @Expose
    private double rplRspTime = 0; // Replenishment Response time in days
    @Expose
    private double rplRspTimeChngPer = 0; // Replenishment Response time Change percentage
    // The following are not yet shown in the UI
    @Expose
    private int issues = 0;
    @Expose
    private int rcpts = 0;
    @Expose
    private int dscrds = 0;
    @Expose
    private int stkcnts = 0;
    @Expose
    private int cumTrans = 0;
    @Expose
    private int cumIssues = 0;
    @Expose
    private int cumRcpts = 0;
    @Expose
    private int cumStkCnts = 0;
    @Expose
    private int cumDscrds = 0;
    @Expose
    private int cumOrders = 0;

    private Counts(ISlice slice) {
      init(slice);
    }

    // Calculate the change % ages for the metrics using previous slice
    private Counts(ISlice curSlice, ISlice prevSlice) {
      initCurSliceFromPrevSlice(curSlice, prevSlice);
    }

    public long getT() {
      return t;
    }

    public void setT(long t) {
      this.t = t;
    }

    public int getTrans() {
      return trans;
    }

    public void setTrans(int trans) {
      this.trans = trans;
    }

    public double getTransChngPer() {
      return transChngPer;
    }

    public void setTransChngPer(double transChngPer) {
      this.transChngPer = transChngPer;
    }

    public int getTotalUsrs() {
      return totalUsrs;
    }

    public void setTotalUsrs(int totalUsrs) {
      this.totalUsrs = totalUsrs;
    }

    public int getTotalLns() {
      return totalLns;
    }

    public void setTotalLns(int totalLns) {
      this.totalLns = totalLns;
    }

    public int getTransUsrs() {
      return transUsrs;
    }

    public void setTransUsrs(int transUsrs) {
      this.transUsrs = transUsrs;
    }

    public double getTransUsrsChngPer() {
      return transUsrsChngPer;
    }

    public void setTransUsrsChngPer(double transUsrsChngPer) {
      this.transUsrsChngPer = transUsrsChngPer;
    }

    public int getTotalEnts() {
      return totalEnts;
    }

    public void setTotalEnts(int totalEnts) {
      this.totalEnts = totalEnts;
    }

    public int getTransEnts() {
      return transEnts;
    }

    public void setTransEnts(int transEnts) {
      this.transEnts = transEnts;
    }

    public double getTransEntsChngPer() {
      return transEntsChngPer;
    }

    public void setTransEntsChngPer(double transEntsChngPer) {
      this.transEntsChngPer = transEntsChngPer;
    }

    public int getActInvItems() {
      return actInvItems;
    }

    public void setActInvItems(int actInvItems) {
      this.actInvItems = actInvItems;
    }

    public int getTotalInvItems() {
      return totalInvItems;
    }

    public void setTotalInvItems(int totalInvItems) {
      this.totalInvItems = totalInvItems;
    }

    public double getActInvItemsChngPer() {
      return actInvItemsChngPer;
    }

    public void setActInvItemsChngPer(double actInvItemsChngPer) {
      this.actInvItemsChngPer = actInvItemsChngPer;
    }

    public BigDecimal getRvn() {
      return rvn;
    }

    public void setRvn(BigDecimal rvn) {
      this.rvn = rvn;
    }

    public double getRvnChngPer() {
      return rvnChngPer;
    }

    public void setRvnChngPer(double rvnChngPer) {
      this.rvnChngPer = rvnChngPer;
    }

    public int getNumEntsExcCrLmt() {
      return numEntsExcCrLmt;
    }

    public void setNumEntsExcCrLmt(int numEntsExcCrLmt) {
      this.numEntsExcCrLmt = numEntsExcCrLmt;
    }

    public double getNumEntsExcCrLmtChngPer() {
      return numEntsExcCrLmtChngPer;
    }

    public void setNumEntsExcCrLmtChngPer(double numEntsExcCrLmtChngPer) {
      this.numEntsExcCrLmtChngPer = numEntsExcCrLmtChngPer;
    }

    public int getOrdrs() {
      return ordrs;
    }

    public void setOrdrs(int ordrs) {
      this.ordrs = ordrs;
    }

    public double getOrdrsChngPer() {
      return ordrsChngPer;
    }

    public void setOrdrsChngPer(double ordrsChngPer) {
      this.ordrsChngPer = ordrsChngPer;
    }

    public int getFulOrdrs() {
      return fulOrdrs;
    }

    public void setFulOrdrs(int fulOrdrs) {
      this.fulOrdrs = fulOrdrs;
    }

    public double getFulOrdrsChngPer() {
      return fulOrdrsChngPer;
    }

    public void setFulOrdrsChngPer(double fulOrdrsChngPer) {
      this.fulOrdrsChngPer = fulOrdrsChngPer;
    }

    public int getPndgOrdrs() {
      return pndgOrdrs;
    }

    public void setPndgOrdrs(int pndgOrdrs) {
      this.pndgOrdrs = pndgOrdrs;
    }

    public double getPndgOrdrsChngPer() {
      return pndgOrdrsChngPer;
    }

    public void setPndgOrdrsChngPer(double pndgOrdrsChngPer) {
      this.pndgOrdrsChngPer = pndgOrdrsChngPer;
    }

    public double getOrdrRspTime() {
      return ordrRspTime;
    }

    public void setOrdrRspTime(double ordrRspTime) {
      this.ordrRspTime = ordrRspTime;
    }

    public double getOrdrRspTimeChngPer() {
      return ordrRspTimeChngPer;
    }

    public void setOrdrRspTimeChngPer(double ordrRspTimeChngPer) {
      this.ordrRspTimeChngPer = ordrRspTimeChngPer;
    }

    public int getStckOuts() {
      return stckOuts;
    }

    public void setStckOuts(int stckOuts) {
      this.stckOuts = stckOuts;
    }

    public double getStckOutsChngPer() {
      return stckOutsChngPer;
    }

    public void setStckOutsChngPer(double stckOutsChngPer) {
      this.stckOutsChngPer = stckOutsChngPer;
    }

    public int getLessThanMin() {
      return lessThanMin;
    }

    public void setLessThanMin(int lessThanMin) {
      this.lessThanMin = lessThanMin;
    }

    public double getLessThanMinChngPer() {
      return lessThanMinChngPer;
    }

    public void setLessThanMinChngPer(double lessThanMinChngPer) {
      this.lessThanMinChngPer = lessThanMinChngPer;
    }

    public int getGrtThanMax() {
      return grtThanMax;
    }

    public void setGrtThanMax(int grtThanMax) {
      this.grtThanMax = grtThanMax;
    }

    public double getGrtThanMaxChngPer() {
      return grtThanMaxChngPer;
    }

    public void setGrtThanMaxChngPer(double grtThanMaxChngPer) {
      this.grtThanMaxChngPer = grtThanMaxChngPer;
    }

    public double getRplRspTime() {
      return rplRspTime;
    }

    public void setRplRspTime(double rplRspTime) {
      this.rplRspTime = rplRspTime;
    }

    public double getRplRspTimeChngPer() {
      return rplRspTimeChngPer;
    }

    public void setRplRspTimeChngPer(double rplRspTimeChngPer) {
      this.rplRspTimeChngPer = rplRspTimeChngPer;
    }

    public int getIssues() {
      return issues;
    }

    public void setIssues(int issues) {
      this.issues = issues;
    }

    public int getRcpts() {
      return rcpts;
    }

    public void setRcpts(int rcpts) {
      this.rcpts = rcpts;
    }

    public int getDscrds() {
      return dscrds;
    }

    public void setDscrds(int dscrds) {
      this.dscrds = dscrds;
    }

    public int getStkcnts() {
      return stkcnts;
    }

    public void setStkcnts(int stkcnts) {
      this.stkcnts = stkcnts;
    }

    public int getCumTrans() {
      return cumTrans;
    }

    public void setCumTrans(int cumTrans) {
      this.cumTrans = cumTrans;
    }

    public int getCumIssues() {
      return cumIssues;
    }

    public void setCumIssues(int cumIssues) {
      this.cumIssues = cumIssues;
    }

    public int getCumRcpts() {
      return cumRcpts;
    }

    public void setCumRcpts(int cumRcpts) {
      this.cumRcpts = cumRcpts;
    }

    public int getCumStkCnts() {
      return cumStkCnts;
    }

    public void setCumStkCnts(int cumStkCnts) {
      this.cumStkCnts = cumStkCnts;
    }

    public int getCumDscrds() {
      return cumDscrds;
    }

    public void setCumDscrds(int cumDscrds) {
      this.cumDscrds = cumDscrds;
    }

    public int getCumOrders() {
      return cumOrders;
    }

    public void setCumOrders(int cumOrders) {
      this.cumOrders = cumOrders;
    }

    private void init(ISlice slice) {
      this.t = slice.getDate().getTime();
      Long domainId = slice.getDomainId();
      this.trans = slice.getTotalCount();
      this.transChngPer = 0;
      ICounter counter = Counter.getUserCounter(domainId);
      this.totalUsrs = counter.getCount();
      this.totalLns = slice.getLoginCounts();
      this.transUsrs = slice.getTransactiveUserCounts();
      this.transUsrsChngPer = 0;
      counter = Counter.getKioskCounter(domainId);
      this.totalEnts = counter.getCount();
      this.transEnts = slice.getTransactiveEntityCounts();
      this.transEntsChngPer = 0;
      // counter = Counter.getMaterialCounter( domainId, null ); // tag is optional while getting material counter
      this.totalInvItems = 0; // TODO:
      this.actInvItems = 0; // TODO:
      this.actInvItemsChngPer = 0; // TODO:
      this.rvn = slice.getRevenueBooked();
      this.rvnChngPer = 0;
      this.ordrs = slice.getOrderCount();
      this.ordrsChngPer = 0;
      this.fulOrdrs = slice.getFulfilledOrderCount();
      this.fulOrdrsChngPer = 0;
      this.pndgOrdrs = slice.getPendingOrderCount();
      this.pndgOrdrsChngPer = 0;
      this.ordrRspTime = 0;
      this.ordrRspTimeChngPer = 0;
      this.stckOuts = slice.getStockoutEventCounts();
      this.stckOutsChngPer = 0;
      this.lessThanMin = slice.getLessThanMinEventCounts();
      this.lessThanMinChngPer = 0;
      this.grtThanMax = slice.getGreaterThanMaxEventCounts();
      this.grtThanMaxChngPer = 0;
      this.rplRspTime = 0;
      this.rplRspTimeChngPer = 0;
      this.issues = slice.getIssueCount();
      this.rcpts = slice.getReceiptCount();
      this.stkcnts = slice.getStockcountCount();
      this.dscrds = slice.getWastageCount();
      this.cumIssues = slice.getCumulativeIssueCount();
      this.cumRcpts = slice.getCumulativeReceiptCount();
      this.cumStkCnts = slice.getCumulativeStockcountCount();
      this.cumDscrds = slice.getCumulativeWastageCount();
      this.cumTrans = slice.getCumulativeTotalCount();
      this.cumOrders = slice.getCumulativeOrderCount();
    }

    // Private method that calculates % change for the metrics using previous slice.
    private void initCurSliceFromPrevSlice(ISlice curSlice, ISlice prevSlice) {
      this.t = curSlice.getDate().getTime();
      this.trans = curSlice.getTotalCount();
      int prevTrans = prevSlice.getTotalCount();
      if (prevTrans > 0) {
        double tChngPer = (((double) (this.trans - prevTrans)) / prevTrans) * 100;
        this.transChngPer = Math.round(tChngPer * 100.0) / 100.0;
      }
      Long domainId = curSlice.getDomainId();
      ICounter counter = Counter.getUserCounter(domainId);
      this.totalUsrs = counter.getCount();
      this.totalLns = curSlice.getLoginCounts();
      this.transUsrs = curSlice.getTransactiveUserCounts();
      int prevTransUsrs = prevSlice.getTransactiveUserCounts();
      if (prevTransUsrs > 0) {
        double tuChngPer = (((double) (this.transUsrs - prevTransUsrs)) / prevTransUsrs) * 100;
        this.transUsrsChngPer = Math.round(tuChngPer * 100.0) / 100.0;
      }

      counter = Counter.getKioskCounter(domainId);
      this.totalEnts = counter.getCount();
      this.transEnts = curSlice.getTransactiveEntityCounts();
      int prevTransEnts = prevSlice.getTransactiveEntityCounts();
      if (prevTransEnts > 0) {
        double teChngPer = (((double) (this.transEnts - prevTransEnts)) / prevTransEnts) * 100;
        this.transEntsChngPer = Math.round(teChngPer * 100.0) / 100.0;
      }

      this.totalInvItems = 0; // TODO:
      this.actInvItems = 0; // TODO:
      this.actInvItemsChngPer = 0; // TODO:

      this.rvn = curSlice.getRevenueBooked();
      BigDecimal prevRvn = prevSlice.getRevenueBooked();
      if (BigUtil.greaterThanZero(prevRvn)) {
        BigDecimal
            rChngPer =
            this.rvn.subtract(prevRvn).divide(prevRvn, BigDecimal.ROUND_HALF_UP)
                .multiply(BigUtil.HUNDRED);
        this.rvnChngPer = rChngPer.setScale(2, RoundingMode.HALF_UP).doubleValue();
      }
      this.rvn =
          this.rvn.setScale(2,
              BigDecimal.ROUND_HALF_UP); // After computing percentage change in revenue, round off the revenue
      // TODO: Number of entities exceeding credit limit

      this.ordrs = curSlice.getOrderCount();
      int prevOrdrs = prevSlice.getOrderCount();
      if (prevOrdrs > 0) {
        double oChngPer = (((double) (this.ordrs - prevOrdrs)) / prevOrdrs) * 100;
        this.ordrsChngPer = Math.round(oChngPer * 100.0) / 100.0;
      }

      this.fulOrdrs = curSlice.getFulfilledOrderCount();
      int prevFulOrdrs = prevSlice.getFulfilledOrderCount();
      if (prevFulOrdrs > 0) {
        double foChngPer = (((double) (this.fulOrdrs - prevFulOrdrs)) / prevFulOrdrs) * 100;
        this.fulOrdrsChngPer = Math.round(foChngPer * 100.0) / 100.0;
      }

      this.pndgOrdrs = curSlice.getPendingOrderCount();
      int prevPndgOrdrs = prevSlice.getPendingOrderCount();
      if (prevPndgOrdrs > 0) {
        double poChngPer = ((double) (this.pndgOrdrs - prevPndgOrdrs) / prevPndgOrdrs) * 100;
        this.pndgOrdrsChngPer = Math.round(poChngPer * 100.0) / 100.0;
      }

      // Order response time - Is present only for MonthSlice
      if (curSlice instanceof IReportsSlice) {
        this.ordrRspTime =
            Math.round(((IReportsSlice) curSlice).getAverageOrderProcessingTime() * 100.0) / 100.0;
        double prevOrdrRspTime = ((IReportsSlice) prevSlice).getAverageOrderProcessingTime();
        if (prevOrdrRspTime > 0) {
          double
              ordrRspTimeChngPer =
              ((double) (this.ordrRspTime - prevOrdrRspTime) / prevOrdrRspTime) * 100;
          this.ordrRspTimeChngPer = Math.round(ordrRspTimeChngPer * 100.0) / 100.0;
        }
      }
      this.stckOuts = curSlice.getStockoutEventCounts();
      int prevStckOuts = prevSlice.getStockoutEventCounts();
      if (prevStckOuts > 0) {
        double soChngPer = (((double) this.stckOuts - prevStckOuts) / prevStckOuts) * 100;
        this.stckOutsChngPer = Math.round(soChngPer) * 100.0 / 100.0;
      }

      this.lessThanMin = curSlice.getLessThanMinEventCounts();
      int prevLessThanMin = prevSlice.getLessThanMinEventCounts();
      if (prevLessThanMin > 0) {
        double
            lmChngPer =
            (((double) (this.lessThanMin - prevLessThanMin)) / prevLessThanMin) * 100;
        this.lessThanMinChngPer = Math.round(lmChngPer * 100.0) / 100.0;
      }

      this.grtThanMax = curSlice.getGreaterThanMaxEventCounts();
      int prevGrtThanMax = prevSlice.getGreaterThanMaxEventCounts();
      if (prevGrtThanMax > 0) {
        double gmChngPer = (((double) (this.grtThanMax - prevGrtThanMax)) / prevGrtThanMax) * 100;
        this.grtThanMaxChngPer = Math.round(gmChngPer * 100.0) / 100.0;
      }

      // Replenishment Response time
      if (curSlice instanceof IReportsSlice) {
        this.rplRspTime =
            Math.round(((IReportsSlice) curSlice).getAverageStockoutResponseTime() * 100.0) / 100.0;
        double prevRplRspTime = ((IReportsSlice) prevSlice).getAverageStockoutResponseTime();
        if (prevRplRspTime > 0) {
          double
              rplRspTimeChngPer =
              ((double) (this.rplRspTime - prevRplRspTime) / prevRplRspTime) * 100;
          this.rplRspTimeChngPer = Math.round(rplRspTimeChngPer * 100.0) / 100.0;
        }
      }

      this.issues = curSlice.getIssueCount();
      this.rcpts = curSlice.getReceiptCount();
      this.stkcnts = curSlice.getStockcountCount();
      this.dscrds = curSlice.getWastageCount();
      this.cumTrans = curSlice.getCumulativeTotalCount();
      this.cumIssues = curSlice.getCumulativeIssueCount();
      this.cumRcpts = curSlice.getCumulativeReceiptCount();
      this.cumStkCnts = curSlice.getCumulativeStockcountCount();
      this.cumDscrds = curSlice.getCumulativeWastageCount();
      this.cumOrders = curSlice.getCumulativeOrderCount();
    }
  }

  private static class DshbrdCfg {
    @Expose
    private ActPnlCfg actPnlCfg = null; // Activity panel configuration
    @Expose
    private RvnPnlCfg rvnPnlCfg = null; // Revenue panel configuration
    @Expose
    private OrdPnlCfg ordPnlCfg = null; // Order panel configuration
    @Expose
    private InvPnlCfg invPnlCfg = null; // Inventory panel configuration

    public DshbrdCfg() {

    }

    public DshbrdCfg(DashboardConfig dashboardConfig) {
      if (dashboardConfig != null) {
        DashboardConfig.ActivityPanelConfig
            actPanelConfig = dashboardConfig.getActivityPanelConfig();
        if (actPanelConfig != null) {
          this.actPnlCfg = new ActPnlCfg(actPanelConfig);
        }
        DashboardConfig.RevenuePanelConfig rvnPanelConfig = dashboardConfig.getRevenuePanelConfig();
        if (rvnPanelConfig != null) {
          this.rvnPnlCfg = new RvnPnlCfg(rvnPanelConfig);
        }
        DashboardConfig.OrderPanelConfig ordPanelConfig = dashboardConfig.getOrderPanelConfig();
        if (ordPanelConfig != null) {
          this.ordPnlCfg = new OrdPnlCfg(ordPanelConfig);
        }
        DashboardConfig.InventoryPanelConfig
            invPanelConfig = dashboardConfig.getInventoryPanelConfig();
        if (invPanelConfig != null) {
          this.invPnlCfg = new InvPnlCfg(invPanelConfig);
        }
      }
    }
  }

  private static class ActPnlCfg {
    @Expose
    private boolean show = false;

    public ActPnlCfg(DashboardConfig.ActivityPanelConfig actPnlCfg) {
      if (actPnlCfg != null) {
        this.show = actPnlCfg.showActivityPanel;
      }
    }
  }

  private static class RvnPnlCfg {
    @Expose
    private boolean show = false;

    public RvnPnlCfg(DashboardConfig.RevenuePanelConfig rvnPnlCfg) {
      if (rvnPnlCfg != null) {
        this.show = rvnPnlCfg.showRevenuePanel;
      }
    }
  }

  private static class OrdPnlCfg {
    @Expose
    private boolean show = false;

    public OrdPnlCfg(DashboardConfig.OrderPanelConfig ordPnlCfg) {
      if (ordPnlCfg != null) {
        this.show = ordPnlCfg.showOrderPanel;
      }
    }
  }

  private static class InvPnlCfg {
    @Expose
    private boolean show = false;

    public InvPnlCfg(DashboardConfig.InventoryPanelConfig invPnlCfg) {
      if (invPnlCfg != null) {
        this.show = invPnlCfg.showInvPanel;
      }
    }
  }
}

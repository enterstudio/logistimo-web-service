package com.logistimo.api.util;

import com.logistimo.dao.JDOUtils;

import com.logistimo.inventory.dao.ITransDao;
import com.logistimo.inventory.dao.impl.TransDao;
import com.logistimo.inventory.entity.IInvntry;
import com.logistimo.inventory.entity.ITransaction;
import com.logistimo.inventory.service.InventoryManagementService;
import com.logistimo.inventory.service.impl.InventoryManagementServiceImpl;

import com.logistimo.services.DuplicationException;
import com.logistimo.services.ServiceException;
import com.logistimo.services.Services;
import com.logistimo.utils.BigUtil;
import com.logistimo.utils.LocalDateUtil;
import com.logistimo.logger.XLog;

import java.math.BigDecimal;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

/**
 * Simulates transaction data (issues, receipts) for a given kiosk over a given period
 *
 * @author arun
 */

public class KioskDataSimulator {

  // Constants
  // Periodicity of data
  public static final String PERIODICITY_DAILY = "d";
  public static final String PERIODICITY_WEEKLY = "w";
  // Duration and start date
  public static final int DAYS = 90;
  // Mean and standard-deviation defaults
  public static final int ISSUE_MEAN = 50;
  public static final int ISSUE_STDDEV = 25;
  public static final int RECEIPT_MEAN = 150;
  public static final int RECEIPT_STDDEV = 40;
  // Initial stock
  public static final BigDecimal INITIAL_STOCK = new BigDecimal(RECEIPT_MEAN);
  // Order periodicity
  public static final int ORDER_PERIODICITY = 6; // day(s)
  // Run frequency
  public static final int OPTIMIZATION_FREQUENCY = 10; // day(s)
  // Zero stock days range
  public static final int ZEROSTOCK_LOW = 1; // day(s)
  public static final int ZEROSTOCK_HIGH = 2; // day(s)

  // Limits on data generation
  public static final int
      MATERIAL_LIMIT =
      10;
  // max materials per kiosk for which data is generated
  public static final int
      DAYS_LIMIT =
      365;
  // max days for which data can be generated per kiosk-material
  private static final XLog xLogger = XLog.getLog(KioskDataSimulator.class);
  // Properties
  // Duration of data
  private Date startDate = null;
  private int days = DAYS; // days
  // Transaction periodicity
  private String issuePeriodicity = PERIODICITY_DAILY;
  // Stock on hand - a default starting point for each material
  private BigDecimal stockOnHand = INITIAL_STOCK;
  // Issue mean and std. deviation
  private float issueMean = ISSUE_MEAN;
  private float issueStdDev = ISSUE_STDDEV;
  // Receipt mean and std. deviation
  private float receiptMean = RECEIPT_MEAN;
  private float receiptStdDev = RECEIPT_STDDEV;
  // Order periodicity
  private int receiptPeriodicity = ORDER_PERIODICITY; // days
  // Zero stock days range
  private int zeroStockDaysLow = ZEROSTOCK_LOW;
  private int zeroStockDaysHigh = ZEROSTOCK_HIGH;
  // Optimization frequency
  private int optimizationFrequency = OPTIMIZATION_FREQUENCY; // days - say, every 1 day
  private InventoryManagementService ims = null;

  private Random rand = null;
  private ITransDao transDao = new TransDao();

  public KioskDataSimulator() throws ServiceException {
    // Init.
    ims = Services.getService(InventoryManagementServiceImpl.class);
    rand = new Random();

    // Set the default dates to be 'days' days from now
    Calendar cal = GregorianCalendar.getInstance();
    cal.setTime(new Date());
    cal.add(Calendar.DATE, -1 * days); // go back 'days' days in time
    this.startDate = cal.getTime();
  }

  public int getNumberOfDays() {
    return days;
  }

  public void setNumberOfDays(int days) {
    this.days = days;
  }

  public String getIssuePeriodicity() {
    return issuePeriodicity;
  }

  public void setIssuePeriodicity(String issuePeriodicity) {
    this.issuePeriodicity = issuePeriodicity;
  }

  public BigDecimal getStockOnHand() {
    return stockOnHand;
  }

  public void setStockOnHand(BigDecimal stockOnHand) {
    this.stockOnHand = stockOnHand;
  }

  public float getIssueMean() {
    return issueMean;
  }

  public void setIssueMean(float issueMean) {
    this.issueMean = issueMean;
  }

  public float getIssueStdDev() {
    return issueStdDev;
  }

  public void setIssueStdDev(float issueStdDev) {
    this.issueStdDev = issueStdDev;
  }

  public float getReceiptMean() {
    return receiptMean;
  }

  public void setReceiptMean(float receiptMean) {
    this.receiptMean = receiptMean;
  }

  public float getReceiptStdDev() {
    return receiptStdDev;
  }

  public void setReceiptStdDev(float receiptStdDev) {
    this.receiptStdDev = receiptStdDev;
  }

  public int getOrderPeriodicity() {
    return receiptPeriodicity;
  }

  public void setOrderPeriodicity(int receiptPeriodicity) {
    this.receiptPeriodicity = receiptPeriodicity;
  }

  public Date getStartDate() {
    return startDate;
  }

  public void setStartDate(Date startDate) {
    this.startDate = startDate;
  }

  public int getZeroStockDaysLow() {
    return zeroStockDaysLow;
  }

  public void setZeroStockDaysLow(int zeroStockDaysLow) {
    this.zeroStockDaysLow = zeroStockDaysLow;
  }

  public int getZeroStockDaysHigh() {
    return zeroStockDaysHigh;
  }

  public void setZeroStockDaysHigh(int zeroStockDaysHigh) {
    this.zeroStockDaysHigh = zeroStockDaysHigh;
  }

  public int getOptimizationFrequency() {
    return optimizationFrequency;
  }

  public void setOptimizationFrequency(int optimizationFrequency) {
    this.optimizationFrequency = optimizationFrequency;
  }

  /**
   * Simulate transaction data of issues and receipts as per the settings above
   *
   * @param kioskId The ID of kiosk for which data is to be genereted; generates for all materials in the kiosk
   * @return The number of transaction records generated
   */
  @SuppressWarnings("unchecked")
  public int generateData(Long domainId, Long kioskId, String userId)
      throws ServiceException, DuplicationException {
    xLogger.fine("Entered generateData");

    // Get all materials of this kiosk
    List<IInvntry>
        inventories =
        ims.getInventoryByKiosk(kioskId, null).getResults(); // TODO: pagination
    Iterator<IInvntry> it = inventories.iterator();
    int numTrans = 0;
    int numMaterials = 0;
    while (it.hasNext() && numMaterials < MATERIAL_LIMIT) {
      numTrans += generateData(domainId, kioskId, it.next().getMaterialId(), userId);
      numMaterials++;
    }

    xLogger.fine("Existing generateData");

    return numTrans;
  }

  /**
   * Simulate transaction data (issues, receipts) for a given kisok-material combination
   */
  public int generateData(Long domainId, Long kioskId, Long materialId, String userId)
      throws ServiceException, DuplicationException {
    xLogger.fine("Entering generateData");

    // Benchmarking time
    long startL = System.currentTimeMillis();

    // Get the start calendar
    Calendar cal = GregorianCalendar.getInstance();
    cal.setTime(this.startDate);

    // Get time increments (in days) for the transaction
    int incr = 1;
    if (this.issuePeriodicity == PERIODICITY_WEEKLY) {
      incr = 7;
    }

    // Update inventory with current stock-on-hand
    BigDecimal stockOnHand = this.stockOnHand;
    // Do an initial physical stock count
    // Post a physical stock count
    ITransaction trans1 = JDOUtils.createInstance(ITransaction.class);
    trans1.setType(ITransaction.TYPE_PHYSICALCOUNT);
    trans1.setDomainId(domainId);
    trans1.setKioskId(kioskId);
    trans1.setMaterialId(materialId);
    trans1.setQuantity(stockOnHand);
    trans1.setTimestamp(cal.getTime());
    trans1.setUseCustomTimestamp(
        true); // this ensures that the above timestamp is used by the system, instead of system assigned timestamp
    trans1.setSourceUserId(userId);
    transDao.setKey(trans1);
    // Store the transaction
    ims.updateInventoryTransaction(domainId, trans1, true);
    xLogger.fine("***** Updated initial stocked-on-hand {0} for domain-kiosk-material {1}-{2}-{3}",
        stockOnHand, domainId, kioskId, materialId);

    // Create transaction objects and update data store
    int i = 0;
    int numTrans = 0;
    // Advance clock by a few mins. (say, 5), given a physical count trans. just occurred
    advanceClock(cal, 1, Calendar.SECOND);
    while (i < this.days) {
      // Get issue quantity
      BigDecimal issueQuantity = getIssueQuantity();
      if (BigUtil.greaterThan(issueQuantity, stockOnHand)) {
        issueQuantity = stockOnHand;
      }
      // Trans. on this day
      int numTransToday = 0;
      // Generate an issue
      if (BigUtil.greaterThanEqualsZero(stockOnHand.subtract(issueQuantity))) {
        // Create transaction object
        ITransaction trans = JDOUtils.createInstance(ITransaction.class);
        trans.setType(ITransaction.TYPE_ISSUE);
        trans.setDomainId(domainId);
        trans.setKioskId(kioskId);
        trans.setMaterialId(materialId);
        trans.setQuantity(issueQuantity);
        trans.setTimestamp(cal.getTime());
        trans.setUseCustomTimestamp(
            true); // ensures that the above timestamp is used, instead of system assigned timestamp
        trans.setSourceUserId(userId);
        transDao.setKey(trans);
        // Store the transaction
        ims.updateInventoryTransaction(domainId, trans, true);

        // Increment transaction count
        numTrans++;
        numTransToday++;
      }

      // Decrement stock-on-hand
      stockOnHand = stockOnHand.subtract(issueQuantity);
      if (BigUtil.equalsZero(stockOnHand)) {
        // Get the zero stock days
        int zeroStockDays = getZeroStockDays();
        // Increment calendar days by this much
        cal.add(Calendar.DATE, zeroStockDays);
        i += zeroStockDays;

        // Generate receipt for this material
        BigDecimal receivedQuantity = getReceiptQuantity();
        ITransaction trans = JDOUtils.createInstance(ITransaction.class);
        trans.setType(ITransaction.TYPE_RECEIPT);
        trans.setDomainId(domainId);
        trans.setKioskId(kioskId);
        trans.setMaterialId(materialId);
        trans.setQuantity(receivedQuantity);
        trans.setTimestamp(cal.getTime());
        trans.setUseCustomTimestamp(
            true); // ensures that the above timestamp is used, instead of system assigned timestamp
        trans.setSourceUserId(userId);
        transDao.setKey(trans);
        // Store the transaction
        ims.updateInventoryTransaction(domainId, trans, true);
        //xLogger.fine( "***** Received {0} on day {1} of {2} after {3} zero-stock days [{4}]", receivedQuantity, i, this.days, zeroStockDays, cal.getTime().toString() );

        // Reset stock-on-hand
        stockOnHand = receivedQuantity;
        // Increment transaction count
        numTrans++;
      } else {
        // Do a physical count adjustment every 7 days
        if ((i % 7) == 0) {
          // Advance clock by a few seconds (say, 1)
          if (numTransToday > 0) {
            advanceClock(cal, 1, Calendar.SECOND);
          }
          BigDecimal stockAdj = getStockAdjustment(stockOnHand);
          BigDecimal adjustedStock = stockOnHand.add(stockAdj);
          xLogger.fine(
              "***** Adjusting stock via physical count for {0}-{1}: stock = {2}, adjustment = {3}, adjusted stock = {4}, day = {5}, date = {6}",
              kioskId, materialId, stockOnHand, stockAdj, adjustedStock, i, cal.getTime());
          // Post a physical stock count
          ITransaction trans = JDOUtils.createInstance(ITransaction.class);
          trans.setType(ITransaction.TYPE_PHYSICALCOUNT);
          trans.setDomainId(domainId);
          trans.setKioskId(kioskId);
          trans.setMaterialId(materialId);
          trans.setQuantity(adjustedStock);
          trans.setTimestamp(cal.getTime());
          trans.setUseCustomTimestamp(
              true); // ensures that the above timestamp is used, instead of system assigned timestamp
          trans.setSourceUserId(userId);
          transDao.setKey(trans);
          // Store the transaction
          ims.updateInventoryTransaction(domainId, trans, true);
        }
      }

      // Update time
      cal.add(Calendar.DATE, incr);

      // Increment days index
      i += incr;
    }

    // Benchmarking time
    long endL = System.currentTimeMillis();

    xLogger.fine("Exiting generateData: duration = {0} millisecs", (endL - startL));
    return numTrans;
  }

  // Generate issue quantities according to a Gaussian (normal) distribution
  private BigDecimal getIssueQuantity() {
    int q = -1;
    while (q < 0) {
      q = ((int) (this.rand.nextGaussian() * this.issueStdDev + this.issueMean)) / 10 * 10;
    }
    return new BigDecimal(q);
  }

  // Generate receipt quantities according to a Gaussian (normal) distribution
  private BigDecimal getReceiptQuantity() {
    int q = -1;
    while (q < 0) {
      q = ((int) (this.rand.nextGaussian() * this.receiptStdDev + this.receiptMean)) / 10 * 10;
    }
    return new BigDecimal(q);
  }

  // Generate a random stock-adjustment value (+ve or -ve)
  private BigDecimal getStockAdjustment(BigDecimal val) {
    int range = (int) (val.floatValue() / 10); // + or -10% of the given value
    if (range == 0) {
      range = 1;
    }
    int rangeLow = -1 * range;
    int r = (int) (Math.random() * (range - rangeLow + 1)) + rangeLow;
    if (r == 0) {
      r = 1;
    }
    r = r > 0 ? r + 10 : r - 10;
    return new BigDecimal(r / 10 * 10);
  }

  // Zero-stock days is "uniformly randomly" distributed in the given range
  private int getZeroStockDays() {
    return (int) (Math.random() * (this.zeroStockDaysHigh - this.zeroStockDaysLow + 1))
        + this.zeroStockDaysLow;
  }

  // Advance clock
  private void advanceClock(Calendar cal, int x, int calType) {
    cal.setTime(LocalDateUtil.getOffsetDate(cal.getTime(), x, calType));
  }
}

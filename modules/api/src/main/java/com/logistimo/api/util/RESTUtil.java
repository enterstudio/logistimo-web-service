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
package com.logistimo.api.util;


import com.logistimo.AppFactory;
import com.logistimo.accounting.entity.IAccount;
import com.logistimo.accounting.service.IAccountingService;
import com.logistimo.accounting.service.impl.AccountingServiceImpl;
import com.logistimo.api.servlets.mobile.builders.MobileConfigBuilder;
import com.logistimo.api.servlets.mobile.builders.MobileEntityBuilder;
import com.logistimo.api.servlets.mobile.models.ParsedRequest;
import com.logistimo.auth.SecurityConstants;
import com.logistimo.auth.SecurityMgr;
import com.logistimo.auth.SecurityUtil;
import com.logistimo.auth.service.AuthenticationService;
import com.logistimo.auth.service.impl.AuthenticationServiceImpl;
import com.logistimo.auth.utils.SessionMgr;
import com.logistimo.communications.service.SMSService;
import com.logistimo.config.models.ActualTransConfig;
import com.logistimo.config.models.ApprovalsConfig;
import com.logistimo.config.models.CapabilityConfig;
import com.logistimo.config.models.ConfigurationException;
import com.logistimo.config.models.DomainConfig;
import com.logistimo.config.models.GeneralConfig;
import com.logistimo.config.models.InventoryConfig;
import com.logistimo.config.models.MatStatusConfig;
import com.logistimo.config.models.OptimizerConfig;
import com.logistimo.config.models.OrdersConfig;
import com.logistimo.config.models.SMSConfig;
import com.logistimo.config.models.SupportConfig;
import com.logistimo.config.models.SyncConfig;
import com.logistimo.constants.CharacterConstants;
import com.logistimo.constants.Constants;
import com.logistimo.constants.SourceConstants;
import com.logistimo.dao.JDOUtils;
import com.logistimo.entities.auth.EntityAuthoriser;
import com.logistimo.entities.entity.IApprovers;
import com.logistimo.entities.entity.IKiosk;
import com.logistimo.entities.entity.IKioskLink;
import com.logistimo.entities.models.UserEntitiesModel;
import com.logistimo.entities.service.EntitiesService;
import com.logistimo.entities.service.EntitiesServiceImpl;
import com.logistimo.entity.IJobStatus;
import com.logistimo.exception.InvalidDataException;
import com.logistimo.exception.SystemException;
import com.logistimo.exception.UnauthorizedException;
import com.logistimo.exports.BulkExportMgr;
import com.logistimo.inventory.TransactionUtil;
import com.logistimo.inventory.dao.ITransDao;
import com.logistimo.inventory.dao.impl.TransDao;
import com.logistimo.inventory.entity.IInvntry;
import com.logistimo.inventory.entity.IInvntryBatch;
import com.logistimo.inventory.entity.ITransaction;
import com.logistimo.inventory.service.InventoryManagementService;
import com.logistimo.inventory.service.impl.InventoryManagementServiceImpl;
import com.logistimo.logger.XLog;
import com.logistimo.materials.entity.IHandlingUnit;
import com.logistimo.materials.entity.IMaterial;
import com.logistimo.materials.service.IHandlingUnitService;
import com.logistimo.materials.service.MaterialCatalogService;
import com.logistimo.materials.service.impl.HandlingUnitServiceImpl;
import com.logistimo.materials.service.impl.MaterialCatalogServiceImpl;
import com.logistimo.orders.entity.IOrder;
import com.logistimo.pagination.PageParams;
import com.logistimo.pagination.Results;
import com.logistimo.proto.BasicOutput;
import com.logistimo.proto.JsonTagsZ;
import com.logistimo.proto.MaterialRequest;
import com.logistimo.proto.MobileApprovalsConfigModel;
import com.logistimo.proto.ProtocolException;
import com.logistimo.proto.RestConstantsZ;
import com.logistimo.proto.UpdateInventoryInput;
import com.logistimo.proto.UpdateOrderRequest;
import com.logistimo.security.SecureUserDetails;
import com.logistimo.services.ObjectNotFoundException;
import com.logistimo.services.Resources;
import com.logistimo.services.ServiceException;
import com.logistimo.services.Services;
import com.logistimo.services.impl.PMF;
import com.logistimo.services.taskqueue.ITaskService;
import com.logistimo.tags.TagUtil;
import com.logistimo.users.entity.IUserAccount;
import com.logistimo.users.service.UsersService;
import com.logistimo.users.service.impl.UsersServiceImpl;
import com.logistimo.utils.BigUtil;
import com.logistimo.utils.JobUtil;
import com.logistimo.utils.LocalDateUtil;
import com.logistimo.utils.StringUtil;

import org.apache.commons.lang.StringUtils;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.Serializable;
import java.math.BigDecimal;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.Enumeration;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.Vector;

import javax.jdo.JDOObjectNotFoundException;
import javax.jdo.PersistenceManager;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;


/**
 * @author Arun
 */
public class RESTUtil {

  public static final String VERSION_01 = "01";

  private static final XLog xLogger = XLog.getLog(RESTUtil.class);

  // URLs
  private static final String EXPORT_URL = "/task/export";
  private static final String TRANSACTIONS = "transactions";
  private static final String INVENTORY = "inventory";
  private static final String ORDERS = "orders";

  private static final String MINIMUM_RESPONSE_CODE_TWO = "2";

  private static ITaskService taskService = AppFactory.get().getTaskService();
  private static ITransDao transDao = new TransDao();

  @SuppressWarnings("unchecked")
  public static Results getInventoryData(Long domainId, Long kioskId, Locale locale,
                                         String timezone, String currency, boolean onlyStock,
                                         DomainConfig dc, boolean forceIntegerForStock, Date start,
                                         PageParams pageParams) throws ServiceException {
    xLogger.fine("Entered getInventoryData");
    // Get the services
    InventoryManagementService
        ims =
        Services.getService(InventoryManagementServiceImpl.class);
    MaterialCatalogService mcs = Services.getService(MaterialCatalogServiceImpl.class);
    // Get optimization config
    OptimizerConfig oc = dc.getOptimizerConfig();
    InventoryConfig ic = dc.getInventoryConfig();
    boolean hasDemandForecast = (oc.getCompute() == OptimizerConfig.COMPUTE_FORECASTEDDEMAND);
    boolean hasEOQ = (oc.getCompute() == OptimizerConfig.COMPUTE_EOQ);
    boolean hasStartDate = (start != null);
    // Get the inventories
    Results results = ims.getInventoryByKiosk(kioskId, pageParams);
    IKiosk kiosk = Services.getService(EntitiesServiceImpl.class).getKiosk(kioskId);
    List<IInvntry> inventories = (List<IInvntry>) results.getResults();
    String cursor = results.getCursor();
    boolean isBatchMgmtEnabled = kiosk.isBatchMgmtEnabled();
    Vector<Hashtable<String, Object>> invData = new Vector<Hashtable<String, Object>>();
    Iterator<IInvntry> it = inventories.iterator();
    while (it.hasNext()) {
      IInvntry inv = it.next();
      // Get the material data
      IMaterial m = null;
      try {
        m = mcs.getMaterial(inv.getMaterialId());
      } catch (Exception e) {
        xLogger
            .warn("{0} when getting material {1}: {2}", e.getClass().getName(), inv.getMaterialId(),
                e.getMessage());
        continue;
      }
      // Create a material data hashtable
      Hashtable<String, Object> material = new Hashtable<String, Object>();
      material.put(JsonTagsZ.MATERIAL_ID, m.getMaterialId().toString());
      String stockStr = null;
      if (forceIntegerForStock) {
        stockStr = BigUtil.getFormattedValue(inv.getStock());
      } else {
        stockStr = BigUtil.getFormattedValue(inv.getStock());
      }
      material.put(JsonTagsZ.QUANTITY, stockStr);
      // Send the allocated quantity, in transit quantity and available quantity only if automactic posting of issues on shipping an order is configured.
      if (dc.autoGI()) {
        material
            .put(JsonTagsZ.ALLOCATED_QUANTITY, BigUtil.getFormattedValue(inv.getAllocatedStock()));
        material
            .put(JsonTagsZ.INTRANSIT_QUANTITY, BigUtil.getFormattedValue(inv.getInTransitStock()));
        material
            .put(JsonTagsZ.AVAILABLE_QUANTITY, BigUtil.getFormattedValue(inv.getAvailableStock()));
      }

      Date time = inv.getTimestamp();
      material.put(JsonTagsZ.TIMESTAMP, LocalDateUtil.format(time, locale, timezone));
      // Custom Id, if any
      // Check custom material ID
      String customMaterialId = m.getCustomId();
      if (customMaterialId != null && !customMaterialId.isEmpty()) {
        material.put(JsonTagsZ.CUSTOM_MATERIALID, customMaterialId);
      }
      // Add batches metadata, if any
      Vector<Hashtable<String, String>>
          batches =
          getBatchData(inv, locale, timezone, ims, isBatchMgmtEnabled, dc.autoGI());
      if (batches != null && !batches.isEmpty()) {
        material.put(JsonTagsZ.BATCHES, batches);
      }
      Vector<Hashtable<String, String>>
          expiredBatches =
          getExpiredBatchData(inv, locale, timezone, ims, isBatchMgmtEnabled, dc.autoGI());
      if (expiredBatches != null && !expiredBatches.isEmpty()) {
        material.put(JsonTagsZ.EXPIRED_NONZERO_BATCHES, expiredBatches);
      }
      // If metadata in addition to stock is required, add those here
      if (!onlyStock) {
        material.put(JsonTagsZ.NAME, m.getName());
        // Add 'additional info.' if required
        String addInfo = m.getInfo();
        if (addInfo != null && !addInfo.isEmpty() && m.displayInfo()) {
          material.put(JsonTagsZ.MESSAGE, addInfo);
        }
        BigDecimal price;
        if (BigUtil.greaterThanZero(price = m.getMSRP())) // manufacturer price
        {
          material.put(JsonTagsZ.MANUFACTURER_PRICE, BigUtil.getFormattedValue(price));
        }
        // Retailer price
        BigDecimal rprice = m.getRetailerPrice();
        if (BigUtil.notEqualsZero(
            inv.getRetailerPrice())) // check if price is specified at inventory level; use it, if it exists
        {
          rprice = inv.getRetailerPrice();
        }
        if (BigUtil.notEqualsZero(rprice)) // price to retailer
        {
          material.put(JsonTagsZ.RETAILER_PRICE, BigUtil.getFormattedValue(rprice));
        }
        BigDecimal tax;
        if (BigUtil.notEqualsZero(tax = inv.getTax())) /// tax
        {
          material.put(JsonTagsZ.TAX, BigUtil.getFormattedValue(tax));
        }
        if (m.getCurrency() != null) {
          material.put(JsonTagsZ.CURRENCY, m.getCurrency());
        }
        // If tags are specified, send that back
        List<String> tags = m.getTags();
        if (tags != null && !tags.isEmpty()) {
          material.put(JsonTagsZ.TAGS, StringUtil.getCSV(tags)); // earlier using TagUtil.getCSVTags
        }
        // Material type, if present
        String type = m.getType();
        if (type != null && !type.isEmpty()) {
          material.put(JsonTagsZ.DATA_TYPE, type);
        }
        /// Min/Max, if any
        BigDecimal min = inv.getReorderLevel();
        BigDecimal max = inv.getMaxStock();
        if (BigUtil.notEqualsZero(min)) {
          material.put(JsonTagsZ.MIN, BigUtil.getFormattedValue(min));
        }
        if (BigUtil.notEqualsZero(max)) {
          material.put(JsonTagsZ.MAX, BigUtil.getFormattedValue(max));
        }

        BigDecimal minDur = inv.getMinDuration();
        BigDecimal maxDur = inv.getMaxDuration();
        if (minDur != null && BigUtil.notEqualsZero(minDur)) {
          material.put(JsonTagsZ.MINDUR, BigUtil.getFormattedValue(minDur));
        }
        if (maxDur != null && BigUtil.notEqualsZero(maxDur)) {
          material.put(JsonTagsZ.MAXDUR, BigUtil.getFormattedValue(maxDur));
        }
        //if (ic.getMinMaxType() == InventoryConfig.MIN_MAX_DOS) {
        //	material.put(JsonTagsZ.MMDUR, ic.getMinMaxDur());
        //}
        BigDecimal stockAvailPeriod = ims.getStockAvailabilityPeriod(inv, dc);
        if (stockAvailPeriod != null && BigUtil.notEqualsZero(stockAvailPeriod)) {
          material.put(JsonTagsZ.STOCK_DURATION, BigUtil.getFormattedValue(stockAvailPeriod));
        }
        // Batch details
        if (m.isBatchEnabled() && kiosk.isBatchMgmtEnabled()) {
          material.put(JsonTagsZ.BATCH_ENABLED, "true");
        }
        // Add optimization parameters, if needed and present
        // Consumption rates
        material = getConsumptionRate(ic, inv, material);
        BigDecimal val;
        // Demand forecast
        if ((hasDemandForecast || hasEOQ) && (BigUtil
            .greaterThanZero(val = inv.getRevPeriodDemand()))) {
          material.put(JsonTagsZ.DEMAND_FORECAST, String.valueOf(BigUtil.getFormattedValue(val)));
          if (BigUtil.greaterThanZero(val = inv.getOrderPeriodicity())) {
            material
                .put(JsonTagsZ.ORDER_PERIODICITY, String.valueOf(BigUtil.getFormattedValue(val)));
          }
        }
        // EOQ
        if (hasEOQ && (BigUtil.greaterThanZero(val = inv.getEconomicOrderQuantity()))) {
          material.put(JsonTagsZ.EOQ, BigUtil.getFormattedValue(val));
        }

        // Is temperature sensitive
        if (m.isTemperatureSensitive()) {
          material.put(JsonTagsZ.IS_TEMPERATURE_SENSITIVE, "true");
        }
      }
      if (inv.getShortId() != null) {
        material.put(JsonTagsZ.SHORT_MATERIAL_ID, String.valueOf(inv.getShortId()));
      }
      Vector<Hashtable<String, String>> handlingUnit = getHandlingUnits(inv.getMaterialId());
      if (handlingUnit != null && !handlingUnit.isEmpty()) {
        material.put(JsonTagsZ.ENFORCE_HANDLING_UNIT_CONSTRAINT, "yes");
        material.put(JsonTagsZ.HANDLING_UNIT, handlingUnit);
      }
      // If start date is specified, then check and add the material to invData only if the it was created or updated on or after the start date.
      Date materialCreatedOn = m.getTimeStamp();
      Date materialLastUpdatedOn = m.getLastUpdated();

      xLogger.fine(
          "material name: {0}, start: {1}, hasStartDate: {2}, materialCreatedOn: {3}, materialLastUpdatedOn: {4}",
          m.getName(), start, hasStartDate, materialCreatedOn, materialLastUpdatedOn);
      if (hasStartDate && ((materialCreatedOn != null && materialCreatedOn.compareTo(start) >= 0)
          || (materialLastUpdatedOn != null && materialLastUpdatedOn.compareTo(start) >= 0))) {
        // Add to vector
        invData.add(material);
      } else if (!hasStartDate) {
        // If start date is not specified, add the material to the vector
        invData.add(material);
      }
    }
    xLogger.fine("Exiting getInventoryData: {0} inventory items, invData: {1}", inventories.size(),
        invData);
    return new Results(invData, cursor);
  }

  public static Hashtable<String, Object> getConsumptionRate(InventoryConfig ic, IInvntry inv,
                                                             Hashtable<String, Object> material)
      throws ServiceException {
    boolean displayConsumptionRateDaily = Constants.FREQ_DAILY.equals(ic.getDisplayCRFreq());
    boolean displayConsumptionRateWeekly = Constants.FREQ_WEEKLY.equals(ic.getDisplayCRFreq());
    boolean displayConsumptionRateMonthly = Constants.FREQ_MONTHLY.equals(ic.getDisplayCRFreq());
    if (InventoryConfig.CR_MANUAL == ic.getConsumptionRate()) { // If consumption rate is manual
      InventoryManagementService
          ims =
          Services.getService(InventoryManagementServiceImpl.class);
      BigDecimal cr = ims.getDailyConsumptionRate(inv);
      if (displayConsumptionRateDaily && BigUtil.greaterThanZero(cr)) {
        material.put(JsonTagsZ.CR_DAILY, BigUtil.getFormattedValue(cr));
      } else if (displayConsumptionRateWeekly && BigUtil
          .greaterThanZero(cr = cr.multiply(Constants.WEEKLY_COMPUTATION))) {
        material.put(JsonTagsZ.CR_WEEKLY, BigUtil.getFormattedValue(cr));
      } else if (displayConsumptionRateMonthly && BigUtil
          .greaterThanZero(cr = cr.multiply(Constants.MONTHLY_COMPUTATION))) {
        material.put(JsonTagsZ.CR_MONTHLY, BigUtil.getFormattedValue(cr));
      }
    } else if (InventoryConfig.CR_AUTOMATIC == ic
        .getConsumptionRate()) { // If consumption rate is automatic
      BigDecimal val;
      if (displayConsumptionRateDaily && (BigUtil
          .greaterThanZero(val = inv.getConsumptionRateDaily()))) {
        material.put(JsonTagsZ.CR_DAILY, BigUtil.getFormattedValue(val));
      } else if (displayConsumptionRateWeekly && (BigUtil
          .greaterThanZero(val = inv.getConsumptionRateWeekly()))) {
        material.put(JsonTagsZ.CR_WEEKLY, BigUtil.getFormattedValue(val));
      } else if (displayConsumptionRateMonthly && (BigUtil
          .greaterThanZero(val = inv.getConsumptionRateMonthly()))) {
        material.put(JsonTagsZ.CR_MONTHLY, BigUtil.getFormattedValue(val));
      }
    }
    return material;
  }

  private static Vector<Hashtable<String, String>> getHandlingUnits(Long materialId) {
    Vector<Hashtable<String, String>> hu = new Vector<>(1);
    IHandlingUnitService hus = Services.getService(HandlingUnitServiceImpl.class);
    Map<String, String> huMap = hus.getHandlingUnitDataByMaterialId(materialId);
    if (huMap != null) {
      Hashtable<String, String> h = new Hashtable<>();
      h.put(JsonTagsZ.HANDLING_UNIT_ID, huMap.get(IHandlingUnit.HUID));
      h.put(JsonTagsZ.HANDLING_UNIT_NAME, huMap.get(IHandlingUnit.NAME));
      h.put(JsonTagsZ.QUANTITY, huMap.get(IHandlingUnit.QUANTITY));
      hu.add(h);
      return hu;
    }
    return null;
  }


  /**
   * Create transactions from order request
   */
  public static List<ITransaction> getInventoryTransactions(UpdateOrderRequest uoReq,
                                                            String transType, Date time,
                                                            Locale locale) throws ServiceException {
    List<ITransaction> list = new ArrayList<ITransaction>();

    // Parameter checks
    if (uoReq == null) {
      throw new ServiceException("Invalid JSON input object during inventory update");
    }
    if (transType == null || transType.isEmpty()) {
      throw new ServiceException("Invalid transaction type during inventory update");
    }
    // Get the transactions in the input JSON
    List<MaterialRequest> transObjs = uoReq.mt;
    if (transObjs == null || transObjs.size() == 0) {
      return null;
    }

    boolean checkBatchMgmt = ITransaction.TYPE_TRANSFER.equals(transType);
    List<String> bErrorMaterials = new ArrayList<>(1);
    MaterialCatalogService mcs = null;
    EntitiesService as;
    IKiosk kiosk = null;
    IKiosk linkedKiosk = null;
    try {
      if (checkBatchMgmt) {
        as = Services.getService(EntitiesServiceImpl.class);
        kiosk = as.getKiosk(uoReq.kid);
        linkedKiosk = as.getKiosk(uoReq.lkid);
        checkBatchMgmt =
            !kiosk.isBatchMgmtEnabled() && linkedKiosk != null && linkedKiosk.isBatchMgmtEnabled();
        mcs = Services.getService(MaterialCatalogServiceImpl.class, locale);
      }

    } catch (ServiceException se) {
      xLogger.warn("ServiceException while getting kiosk details. Exception: {0}", se);
    }

    // Form the inventory transaction objects
    for (MaterialRequest mReq : transObjs) {
      if (checkBatchMgmt) {
        IMaterial material = mcs.getMaterial(mReq.mid);
        if (material.isBatchEnabled()) {
          mReq.isBa = true;
          bErrorMaterials.add(material.getName());
        }
      }
      ITransaction trans = JDOUtils.createInstance(ITransaction.class);
      trans.setKioskId(uoReq.kid);
      trans.setMaterialId(mReq.mid);
      trans.setType(transType);
      trans.setQuantity(mReq.q);
      trans.setSourceUserId(uoReq.uid);
      trans.setDestinationUserId(uoReq.duid);
      trans.setMessage(mReq.ms);
      trans.setLinkedKioskId(uoReq.lkid);
      if (uoReq.lat != null) {
        trans.setLatitude(uoReq.lat);
      }
      if (uoReq.lng != null) {
        trans.setLongitude(uoReq.lng);
      }
      if (uoReq.galt != null) {
        trans.setAltitude(uoReq.galt);
      }
      if (uoReq.gacc != null) {
        trans.setGeoAccuracy(uoReq.gacc);
      }
      trans.setGeoErrorCode(uoReq.gerr);
      trans.setReason(mReq.rsn);
      trans.setTrackingId(String.valueOf(uoReq.tid));
      trans.setTrackingObjectType(transType);

      Date t = time;
      if (t == null) {
        t = new Date();
      }
      trans.setTimestamp(t);
      trans.setEditOrderQtyRsn(mReq.rsneoq);

      // Set the material status
      trans.setMaterialStatus(mReq.mst);
      // Add to transaction list
      list.add(trans);

    }
    if (!bErrorMaterials.isEmpty()) {
      xLogger.severe(
          "Transfer failed because source entity {0} is batch disabled and destination entity {1} "
              +
              "is batch enabled. Affected materials: {2}", kiosk != null ? kiosk.getName() : null,
          linkedKiosk != null ? linkedKiosk.getName() : null, bErrorMaterials.toString());
      throw new InvalidDataException(bErrorMaterials.toString());
    }
    return list;
  }

  @SuppressWarnings({"rawtypes", "unchecked"})
  public static List<ITransaction> getInventoryTransactions(UpdateInventoryInput updInventoryJson,
                                                            String transType, Date time,
                                                            Locale locale) throws ServiceException {
    List<ITransaction> list = new ArrayList<ITransaction>();

    // Parameter checks
    if (updInventoryJson == null) {
      throw new ServiceException("Invalid JSON input object during inventory update");
    }
    if (transType == null || transType.isEmpty()) {
      throw new ServiceException("Invalid transaction type during inventory update");
    }
    // Get the transactions in the input JSON
    Vector transObjs = updInventoryJson.getMaterials();
    if (transObjs == null || transObjs.size() == 0) {
      return null; /// NOTE: no longer throwing exception; to be handled by caller; /// earlier: throw new ServiceException( "No transactions specified" );
    }
    // Get the user and kiosk IDs
    String userId = updInventoryJson.getUserId();
    String kioskIdStr = updInventoryJson.getKioskId();
    String destUserId = updInventoryJson.getDestUserId();
    String linkedKioskIdStr = updInventoryJson.getLinkedKioskId();
    Long linkedKioskId = null;
    if (linkedKioskIdStr != null && !linkedKioskIdStr.isEmpty()) {
      linkedKioskId = Long.valueOf(linkedKioskIdStr);
    }

    boolean checkBatchMgmt = ITransaction.TYPE_TRANSFER.equals(transType);
    List<String> bErrorMaterials = new ArrayList<String>(1);
    MaterialCatalogService mcs = null;
    EntitiesService as = null;
    IKiosk kiosk = null;
    IKiosk linkedKiosk = null;
    try {
      if (checkBatchMgmt) {
        as = Services.getService(EntitiesServiceImpl.class);
        kiosk = as.getKiosk(Long.valueOf(kioskIdStr));
        linkedKiosk = as.getKiosk(linkedKioskId);
        checkBatchMgmt =
            !kiosk.isBatchMgmtEnabled() && linkedKiosk != null && linkedKiosk.isBatchMgmtEnabled();
        mcs = Services.getService(MaterialCatalogServiceImpl.class, locale);
      }

    } catch (ServiceException se) {
      xLogger.warn("ServiceException while getting kiosk details. Exception: {0}", se);
    }
    // Get the latitude and longitude, if present
    String latitudeStr = updInventoryJson.getLatitude();
    String longitudeStr = updInventoryJson.getLongitude();
    String geoAccuracyStr = updInventoryJson.getGeoAccuracy();
    String geoErrorCode = updInventoryJson.getGeoErrorCode();
    String altitudeStr = updInventoryJson.getAltitude();

    Double latitude = null, longitude = null, geoAccuracy = null, altitude = null;
    if (latitudeStr != null && !latitudeStr.isEmpty()) {
      try {
        latitude = Double.valueOf(latitudeStr);
      } catch (NumberFormatException e) {
        xLogger.warn("NumberFormatException when getting latitude {0}: {1}", latitudeStr,
            e.getMessage());
      }
    }
    if (longitudeStr != null && !longitudeStr.isEmpty()) {
      try {
        longitude = Double.valueOf(longitudeStr);
      } catch (NumberFormatException e) {
        xLogger.warn("NumberFormatException when getting longitude {0}: {1}", longitudeStr,
            e.getMessage());
      }
    }
    if (altitudeStr != null && !altitudeStr.isEmpty()) {
      try {
        altitude = Double.valueOf(altitudeStr);
      } catch (NumberFormatException e) {
        xLogger.warn("NumberFormatException when getting altitude {0}: {1}", altitudeStr,
            e.getMessage());
      }
    }
    if (geoAccuracyStr != null && !geoAccuracyStr.isEmpty()) {
      try {
        geoAccuracy = Double.valueOf(geoAccuracyStr);
      } catch (NumberFormatException e) {
        xLogger.warn("NumberFormatException when getting geo-accuracy {0}: {1}", geoAccuracyStr,
            e.getMessage());
      }
    }
    // Tracking Id
    String trackingIdStr = updInventoryJson.getTrackingId();
    Long trackingId = null;
    if (trackingIdStr != null && !trackingIdStr.isEmpty()) {
      try {
        trackingId = Long.valueOf(trackingIdStr);
      } catch (Exception e) {
        xLogger.warn("{0} when getting trackingId {1}: {2}", e.getClass().getName(), trackingIdStr,
            e.getMessage());
      }
    }

    // Transaction saved timestamp (in milliseconds), if any
    String transSavedTimestampStr = updInventoryJson.getTimestampSaveMillis();
    Long svdTimeInMillis = null;
    if (transSavedTimestampStr != null && !transSavedTimestampStr.isEmpty()) {
      try {
        svdTimeInMillis = Long.valueOf(transSavedTimestampStr);
      } catch (NumberFormatException e) {
        xLogger.warn("NumberFormatException when getting transaction saved time {0}: {1}",
            transSavedTimestampStr, e.getMessage());
      }
    }
    Date transSavedTimestamp = null;
    if (svdTimeInMillis != null) {
      transSavedTimestamp = new Date(svdTimeInMillis);
    }

    Enumeration<Hashtable> en = transObjs.elements();
    // Form the inventory transaction objects
    while (en.hasMoreElements()) {
      Hashtable<String, String> map = (Hashtable<String, String>) en.nextElement();

      // Get the material details
      String materialIdStr = map.get(JsonTagsZ.MATERIAL_ID);
      if (checkBatchMgmt) {
        IMaterial material = mcs.getMaterial(Long.valueOf(materialIdStr));
        if (material.isBatchEnabled()) {
          bErrorMaterials.add(material.getName());
        }
      }
      String quantityStr = map.get(JsonTagsZ.QUANTITY);
      String
          openingStockStr =
          map.get(JsonTagsZ.OPENING_STOCK); // Opening stock if present. Just reading it.
      String reason = map.get(JsonTagsZ.REASON);
      // FOR BACKWARD COMPATIBILITY (28/4/2012)
      if (reason == null || reason.isEmpty()) {
        reason = map.get(JsonTagsZ.REASONS_WASTAGE); // reason for wastage
      }
      // END BACKWARD COMPATIBITY
      String microMessage = map.get(JsonTagsZ.MESSAGE);
      // Batch parameters, if any
      String batchIdStr = map.get(JsonTagsZ.BATCH_ID);
      String batchExpiryStr = map.get(JsonTagsZ.BATCH_EXPIRY);
      String batchManufacturer = map.get(JsonTagsZ.BATCH_MANUFACTUER_NAME);
      String batchManufacturedDateStr = map.get(JsonTagsZ.BATCH_MANUFACTURED_DATE);

      // Material status, if any
      String matStatusStr = map.get(JsonTagsZ.MATERIAL_STATUS);
      // Actual date of transaction
      String actualDateOfTransStr = map.get(JsonTagsZ.ACTUAL_TRANSACTION_DATE);
      Date actualDateOfTrans = null;
      if (actualDateOfTransStr != null) {
        try {
          actualDateOfTrans =
              LocalDateUtil.parseCustom(actualDateOfTransStr, Constants.DATE_FORMAT, null);
        } catch (Exception e) {
          xLogger.warn("Error while setting actual date of transaction", e);
        }
      }
      // Edit order quantity reason if any.
      String eoqrsn = map.get(JsonTagsZ.REASON_FOR_EDITING_ORDER_QUANTITY);
      // Allocated quantity if any.
      String alqStr = map.get(JsonTagsZ.ALLOCATED_QUANTITY);
      // Form the inventory transaction object
      try {
        ITransaction trans = JDOUtils.createInstance(ITransaction.class);
        Long kioskId = Long.valueOf(kioskIdStr);
        Long materialId = Long.valueOf(materialIdStr);
        trans.setKioskId(kioskId);
        trans.setMaterialId(materialId);
        trans.setType(transType);
        trans.setQuantity(new BigDecimal(quantityStr));
        if (matStatusStr != null && !matStatusStr
            .isEmpty()) // Material status is optional. Set it only if it is present.
        {
          trans.setMaterialStatus(matStatusStr);
        }
        trans.setSourceUserId(userId);
        if (destUserId != null) {
          trans.setDestinationUserId(destUserId);
        }
        if (microMessage != null) {
          trans.setMessage(microMessage);
        }
        if (time != null) {
          trans.setTimestamp(time);
        }
        if (linkedKioskId != null) {
          trans.setLinkedKioskId(linkedKioskId);
        }
        if (latitude != null) {
          trans.setLatitude(latitude.doubleValue());
        }
        if (longitude != null) {
          trans.setLongitude(longitude.doubleValue());
        }
        if (altitude != null) // Altitude is optional. Set it only if it is present
        {
          trans.setAltitude(altitude.doubleValue());
        }
        if (geoAccuracy != null) {
          trans.setGeoAccuracy(geoAccuracy.doubleValue());
        }
        if (geoErrorCode != null) {
          trans.setGeoErrorCode(geoErrorCode);
        }
        if (reason != null && !reason.isEmpty()) {
          trans.setReason(reason);
        }
        if (trackingId != null) {
          trans.setTrackingId(String.valueOf(trackingId));
          trans.setTrackingObjectType(transType);
        }
        if (actualDateOfTrans != null) {
          trans.setAtd(actualDateOfTrans);
        }

        if (batchIdStr != null) // add batch parameters, if needed
        {
          TransactionUtil.setBatchData(trans, batchIdStr, batchExpiryStr, batchManufacturer,
              batchManufacturedDateStr);
        }

        Date t = time;
        if (t == null) {
          t = new Date();
        }
        trans.setTimestamp(t);
        if (eoqrsn != null && !eoqrsn.isEmpty()) {
          trans.setEditOrderQtyRsn(eoqrsn);
        }

        //trans.setKey( Transaction.createKey( kioskId, materialId, transType, t, batchIdStr ) );
        transDao.setKey(trans);
        // Add to transaction list
        list.add(trans);
      } catch (NumberFormatException e) {
        xLogger.warn("Invalid number sent during inventory update: kiosk-material = {0}-{1}: {2}",
            kioskIdStr, materialIdStr, e.getMessage());
      }
    }
    if (!bErrorMaterials.isEmpty()) {
      xLogger.severe(
          "Transfer failed because source entity {0} is batch disabled and destination entity {1} is batch enabled. Affected materials: {2}",
          kiosk.getName(), linkedKiosk.getName(), bErrorMaterials.toString());
      throw new InvalidDataException(bErrorMaterials.toString());
    }
    return list;
  }

  // Authenticate a user and get the user account data
  // If Basic Auth. header is present, authenticate using that, else authenticate using userId and password in query string (to ensure backward compatibility since 1.3.0 of HTML5 app. - Aug 2015)
  // NOTE: The returned user will NOT have associated kiosks; you will need to get them separately (say, via a as.getUserAccount( userId ) call)
  // NOTE: kiosk-based authorization check is made ONLY if kioskId is supplied; kioskId null check should be done by caller, if it is needed
  public static IUserAccount authenticate(String userId, String password, Long kioskId,
                                          HttpServletRequest req, HttpServletResponse resp)
      throws ServiceException, UnauthorizedException {
    ///HttpSession session = req.getSession();
    // Get service
    UsersService as = Services.getService(UsersServiceImpl.class);
    EntitiesService entitiesService = Services.getService(EntitiesServiceImpl.class);
    AuthenticationService aus = Services.getService(AuthenticationServiceImpl.class);
    String errMsg = null;
    IUserAccount u = null;
    ResourceBundle backendMessages;
    String uId = userId;
    String pwd = password;
    String token = req.getHeader(Constants.TOKEN);
    String sourceInitiatorStr = req.getHeader(Constants.ACCESS_INITIATOR);
    int actionInitiator = -1;
    if (sourceInitiatorStr != null) {
      try {
        actionInitiator = Integer.parseInt(sourceInitiatorStr);
      } catch (NumberFormatException e) {

      }
    }
    // Get default backendMessages
    backendMessages =
        Resources.get().getBundle("BackendMessages", new Locale(Constants.LANG_DEFAULT));
    try {
      boolean authenticated = false;
      // Check if Basic auth. header exists
      SecurityMgr.Credentials creds = SecurityMgr.getUserCredentials(req);
      if (StringUtils.isNotEmpty(token)) {
        uId = aus.authenticateToken(token, actionInitiator);
        u = as.getUserAccount(uId);
        authenticated = true;
      } else {
        if (creds != null) {
          uId = creds.userId;
          pwd = creds.password;
        }
        // Authenticate using password (either from auth. header or parameters (for backward compatibility)
        if (uId != null && pwd != null) {
          // Authenticate user
          u = as.authenticateUser(uId, pwd, SourceConstants.MOBILE);
          authenticated = (u != null);
          if (!authenticated) {
            backendMessages =
                Resources.get().getBundle("BackendMessages", new Locale(Constants.LANG_DEFAULT));
            errMsg = backendMessages.getString("error.invalidusername");
          }
        } else if (uId == null) { // no proper credentials
          backendMessages =
              Resources.get().getBundle("BackendMessages", new Locale(Constants.LANG_DEFAULT));
          errMsg = backendMessages.getString("error.invalidusername");
        } else {
          // Brought this back, without this few pages in old UI break. ( Stock view dashboard)
          // DEPRECATED
          // No password - check whether an authenticated session exists
          SecureUserDetails sUser = SecurityMgr.getUserDetails(req.getSession());
          if (sUser != null && sUser.getUsername()
              .equals(uId)) { // authenticated via web login session
            u = as.getUserAccount(uId);
            authenticated = true;
          } else { // no authenticated session either
            backendMessages =
                Resources.get().getBundle("BackendMessages", new Locale(Constants.LANG_DEFAULT));
            errMsg = backendMessages.getString("error.invalidusername");
          }
        }
      }

      // If authenticated, check permissions for this kiosk, if available
      if (authenticated) { // authenticated, so proceed...
        // Check if switch to new host is required. If yes, then return an error message. Otherwise, proceed.
        if (switchToNewHostIfRequired(u, resp)) {
          xLogger.warn("Switched user {0} to new host", uId);
        } else if (kioskId != null) {
          // If not, proceed.
          String role = u.getRole();
          if (SecurityUtil.compareRoles(role, SecurityConstants.ROLE_DOMAINOWNER)
              < 0) { // user has a role less than domain owner (say, entity operator or manager)
            if (!EntityAuthoriser.authoriseEntity(kioskId, u.getRole(), u.getLocale(), u.getUserId(),
                u.getDomainId())) {
              try {
                errMsg =
                    "You do not have authorization for " + entitiesService.getKiosk(kioskId, false).getName();
              } catch (Exception e) {
                xLogger.warn(
                    "Exception {0} when getting find kiosk {1} when authenticating user {2}: {3}",
                    e.getClass().getName(), kioskId, uId, e.getMessage());
                errMsg = "This entity could not be found.";
              }
            }
          } else if (SecurityConstants.ROLE_DOMAINOWNER.equals(role)) { // user is domain owner
            // Ensure that the kiosk belongs to this domain
            try {
              IKiosk k = entitiesService.getKiosk(kioskId, false);
              if (!k.getDomainIds().contains(u.getDomainId())) {
                errMsg = "You do not have authorization for " + k.getName();
              }
            } catch (Exception e) {
              xLogger.warn(
                  "Exception {0} when getting find kiosk {1} when authenticating user {2}: {3}",
                  e.getClass().getName(), kioskId, uId, e.getMessage());
              errMsg = "This entity could not be found.";
            }
          } else {
            try {
              IKiosk k = entitiesService.getKiosk(kioskId, false);
            } catch (Exception e) {
              xLogger.warn(
                  "Exception {0} when getting find kiosk {1} when authenticating user {2}: {3}",
                  e.getClass().getName(), kioskId, uId, e.getMessage());
              errMsg = "This entity could not be found";
            }
          }
        }
      }
    } catch (InvalidDataException | JDOObjectNotFoundException e) {
      throw new UnauthorizedException("Invalid token");
    } catch (ObjectNotFoundException e) {
      errMsg = backendMessages != null ? backendMessages.getString("error.invalidusername") : null;
    } catch (UnauthorizedException e) {
      throw new UnauthorizedException("Invalid token");
    }
    // Check for error
    if (errMsg != null) {
      throw new ServiceException(errMsg);
    }

    return u;
  }

  public static Long getDomainId(HttpSession session, String userId) {
    return SessionMgr.getCurrentDomain(session, userId);
  }

  public static String getJsonOutputAuthenticate(boolean status, IUserAccount user, String message,
                                                 DomainConfig dc, String localeStr,
                                                 String minResponseCode,
                                                 ResourceBundle backendMessages,
                                                 boolean onlyAuthenticate,
                                                 boolean forceIntegerForStock, Date start,
                                                 PageParams kioskPageParams)
      throws ProtocolException, ServiceException {
    xLogger.fine("Entered RESTUtil.getJsonOutputAuthenticate");
    xLogger.fine("start: {0}", start != null ? start.toString() : null);
    Vector<Hashtable> kiosksData = null;
    Hashtable<String, Object> config = null;
    String expiryTime = null;
    boolean hasStartDate = (start != null);
    List<IKiosk> kiosks = null;
    List kioskList = new ArrayList();

    UserEntitiesModel fullUser = new UserEntitiesModel(user, null);
    if (status && !onlyAuthenticate) {
      // Get the kiosks
      EntitiesService as = Services.getService(EntitiesServiceImpl.class);
      // Get paginated kiosks for a given user
      Results
          results =
          as.getKiosksForUser(user, null, kioskPageParams);
      kiosks = results.getResults();
      boolean isSingleKiosk = kiosks.size() == 1;
      // Get the kiosk maps
      kiosksData = new Vector();
      if (kiosks != null && !kiosks.isEmpty()) {
        boolean getUsersForKiosk = (MINIMUM_RESPONSE_CODE_TWO.equals(minResponseCode));
        boolean
            getMaterials =
            (isSingleKiosk || StringUtils.isEmpty(minResponseCode));
        boolean
            getLinkedKiosks =
            (isSingleKiosk || !getUsersForKiosk);
        Iterator<IKiosk> it = kiosks.iterator();
        while (it.hasNext()) {
          IKiosk k = it.next(); // NOTE: kiosk will have NOT its users set
          // set users if minResponseCode is 2 (local editing of kiosks/users is now possible)
          if (getUsersForKiosk) {
            k.setUsers(as.getUsersForKiosk(k.getKioskId(), null).getResults());
          }
          // Get user locale
          Locale locale = user.getLocale();
          // Get kiosk data
          Hashtable<String, Object>
              kioskData =
              getKioskData(k, locale, user.getTimezone(), dc, getUsersForKiosk, getMaterials,
                  getLinkedKiosks, forceIntegerForStock, user.getUserId(), user.getRole());
          Date kioskCreatedOn = k.getTimeStamp();
          Date kioskLastUpdatedOn = k.getLastUpdated();
          xLogger.fine(
              "kiosk name: {0}, start: {1}, hasStartDate: {2}, kioskCreatedOn: {3}, kioskLastUpdatedOn: {4}",
              k.getName(), start, hasStartDate, k.getTimeStamp(), k.getLastUpdated());
          if (hasStartDate && ((kioskCreatedOn != null && kioskCreatedOn.compareTo(start) >= 0) || (
              kioskLastUpdatedOn != null && kioskLastUpdatedOn.compareTo(start) >= 0))) {
            // Add kiosk data
            kiosksData.add(kioskData);
          } else if (!hasStartDate) {
            // Add kiosk data
            kiosksData.add(kioskData);
            kioskList.add(kioskData);
          }
        }
        // Get the expiry time
        expiryTime = String.valueOf(getLoginExpiry(user));
      }
      // Get config. for transaction inclusion and naming
      config = getConfig(dc, user.getRole());
      if (config != null) {
        if (IUserAccount.LR_LOGIN_RECONNECT.equals(user.getLoginReconnect())) {
          config.put(JsonTagsZ.LOGIN_AS_RECONNECT, Constants.TRUE);
        } else if (IUserAccount.LR_LOGIN_DONT_RECONNECT.equals(user.getLoginReconnect())) {
          config.remove(JsonTagsZ.LOGIN_AS_RECONNECT);
        }
      }
      fullUser = new UserEntitiesModel(user, kioskList);
    }
    xLogger.fine("Exiting RESTUtil.getJsonOutputAuthenticate");
    String
        jsonString =
        GsonUtil.authenticateOutputToJson(status, message, expiryTime, fullUser, config,
            RESTUtil.VERSION_01);
    return jsonString;
  }

  // Get data for kiosks for JSON output
  @SuppressWarnings({"unchecked", "rawtypes"})
  private static Hashtable<String, Object> getKioskData(IKiosk k, Locale locale, String timezone,
                                                        DomainConfig dc, boolean getUsersForKiosk,
                                                        boolean getMaterials,
                                                        boolean getLinkedKiosks,
                                                        boolean forceIntegerForStock, String userId,
                                                        String role) {
    xLogger.fine("Entered getKioskData");
    Hashtable
        kioskData =
        k.getMapZ(true,
            null); // NOTE: here all operators can be managed by the given user; so not restricting them to those registered by this user
    // Add the materials and other data
    try {
      // Add currency, if present
      String currency = k.getCurrency();
      if (currency == null) {
        currency = Constants.CURRENCY_DEFAULT;
      }
      kioskData.put(JsonTagsZ.CURRENCY, currency);
      if (k.getCustomerPerm() > 0 || k.getVendorPerm() > 0) {
        Hashtable<String, Hashtable<String, String>> perm = new Hashtable<>();
        if (k.getCustomerPerm() > 0) {
          Hashtable<String, String> customerPermission = new Hashtable<>();
          if (k.getCustomerPerm() > 1) {
            customerPermission.put(JsonTagsZ.MANAGE_MASTER_DATA, "yes");
          } else {
            customerPermission.put(JsonTagsZ.VIEW_INVENTORY_ONLY, "yes");
          }
          perm.put(JsonTagsZ.CUSTOMERS, customerPermission);
        }
        if (k.getVendorPerm() > 0) {
          Hashtable<String, String> vendorPermission = new Hashtable<>();
          if (k.getVendorPerm() > 1) {
            vendorPermission.put(JsonTagsZ.MANAGE_MASTER_DATA, "yes");
          } else {
            vendorPermission.put(JsonTagsZ.VIEW_INVENTORY_ONLY, "yes");
          }
          perm.put(JsonTagsZ.VENDORS, vendorPermission);
        }
        kioskData.put(JsonTagsZ.PERMISSIONS, perm);
      }
      // Add materials
      if (getMaterials) {
        kioskData.put(JsonTagsZ.MATERIALS, RESTUtil
            .getInventoryData(k.getDomainId(), k.getKioskId(), locale, timezone, currency, false,
                dc, forceIntegerForStock, null, null).getResults());
      }
      if (getLinkedKiosks) {
        CapabilityConfig cconf = dc.getCapabilityByRole(role);
        boolean sendVendors = dc.sendVendors();
        boolean sendCustomers = dc.sendCustomers();
        if (cconf != null) {
          sendVendors = cconf.sendVendors();
          sendCustomers = cconf.sendCustomers();
        }
        if (sendVendors) {
          // Add vendor info., if present
          Vector<Hashtable<String, String>>
              vendors =
              (Vector<Hashtable<String, String>>) getLinkedKiosks(k.getKioskId(),
                  IKioskLink.TYPE_VENDOR, userId, getUsersForKiosk, dc, null).getResults();
          if (vendors != null && !vendors.isEmpty()) {
            kioskData
                .put(JsonTagsZ.VENDORS, vendors); // vector of Hashtable of vendor kiosk metadata
          }
        }

        if (sendCustomers) {
          // Add customer info., if present
          Vector<Hashtable<String, String>>
              customers =
              (Vector<Hashtable<String, String>>) getLinkedKiosks(k.getKioskId(),
                  IKioskLink.TYPE_CUSTOMER, userId, getUsersForKiosk, dc, null).getResults();
          if (dc.sendCustomers() && customers != null && !customers.isEmpty()) {
            kioskData.put(JsonTagsZ.CUSTOMERS, customers); // vector of Hashtable(kiosk-metadata)
          }
        }
      }
      // Add approvers if configured for the kiosk.
      EntitiesService as = Services.getService(EntitiesServiceImpl.class, locale);
      MobileEntityBuilder mobileEntityBuilder = new MobileEntityBuilder();
      List<IApprovers> approversList = as.getApprovers(k.getKioskId());
      if (approversList != null && !approversList.isEmpty()) {
        kioskData.put(JsonTagsZ.APPROVERS,
            mobileEntityBuilder.buildApproversModel(approversList));
      }
      // Add kiosk tags if any configured for the kiosk
      // If tags are specified, send that back
      List<String> tags = k.getTags();
      if (tags != null && !tags.isEmpty()) {
        kioskData.put(JsonTagsZ.TAGS, StringUtil.getCSV(tags));
      }
    } catch (ServiceException e) {
      xLogger
          .warn("Unable to get material data for JSON output of REST authentication for kiosk: {0}",
              k.getKioskId());
    }
    xLogger.fine("Exiting getKioskData");
    return kioskData;
  }

  // Get the vendor hashtable of a given kiosk
  @SuppressWarnings("unchecked")
  public static Results getLinkedKiosks(Long kioskId, String linkType, String userId,
                                        boolean getUsersForKiosk, DomainConfig dc,
                                        PageParams pageParams) {
    Vector<Hashtable<String, String>> linkedKiosks = new Vector<Hashtable<String, String>>();
    String cursor = null;
    try {
      EntitiesService as = Services.getService(EntitiesServiceImpl.class);
      Results results = as.getKioskLinks(kioskId, linkType, null, null, pageParams);
      List<IKioskLink> vlinks = results.getResults();
      cursor = results.getCursor();
      if (vlinks == null || vlinks.isEmpty()) {
        return new Results(linkedKiosks, cursor);
      }
      PersistenceManager pm = PMF.get().getPersistenceManager();
      // Check if accounting is enabled - to send credit limit and payable information
      boolean isAccounting = dc.isAccountingEnabled();
      BigDecimal
          defaultCreditLimit =
          (isAccounting && dc.getAccountingConfig() != null ? dc.getAccountingConfig()
              .getCreditLimit() : BigDecimal.ZERO);
      try {
        Iterator<IKioskLink> it = vlinks.iterator();
        while (it.hasNext()) {
          IKioskLink link = it.next();
          try {
            ///Kiosk k = pm.getObjectById( Kiosk.class, link.getLinkedKioskId() );
            Long linkedKioskId = link.getLinkedKioskId();
            IKiosk k = JDOUtils.getObjectById(IKiosk.class, linkedKioskId);
            if (getUsersForKiosk) {
              k.setUsers(as.getUsersForKiosk(linkedKioskId, null, pm)
                  .getResults()); // get the users associated with kiosk
            }
            // Set the route index / tag, if present
            k.setRouteIndex(link.getRouteIndex());
            k.setRouteTag(link.getRouteTag());
            Hashtable<String, String>
                linkedKiosk =
                k.getMapZ(true,
                    userId); // allow only users created by userId to be editable; others are not editable by this user
            // If accounting is enabled, add credit limit and payable (NOTE: payable is always to the vendor from the customer)
            if (isAccounting) {
              BigDecimal
                  creditLimit =
                  (BigUtil.notEqualsZero(link.getCreditLimit()) ? link.getCreditLimit()
                      : defaultCreditLimit);
              if (BigUtil.notEqualsZero(creditLimit)) {
                linkedKiosk.put(JsonTagsZ.CREDIT_LIMIT, BigUtil.getFormattedValue(creditLimit));
                Long vId = null, cId = null;
                if (IKioskLink.TYPE_VENDOR.equals(linkType)) {
                  cId = link.getKioskId();
                  vId = link.getLinkedKioskId();
                } else {
                  vId = link.getKioskId();
                  cId = link.getLinkedKioskId();
                }
                // Get the account payable/receiveable as the case may be
                int year = GregorianCalendar.getInstance().get(Calendar.YEAR);
                try {
                  IAccountingService
                      oms =
                      Services.getService(AccountingServiceImpl.class);
                  IAccount account = oms.getAccount(vId, cId, year);
                  linkedKiosk
                      .put(JsonTagsZ.PAYABLE, BigUtil.getFormattedValue(account.getPayable()));
                } catch (ObjectNotFoundException e) {
                  // ignore
                } catch (Exception e) {
                  xLogger.warn(
                      "{0} when getting account for kiosk link vendor {1}, customer {2}, year {3}: {4}",
                      e.getClass().getName(), vId, cId, year, e.getMessage());
                }
              } // end if ( creditLimit != 0F )
            }
            // Add the disabled batch management flag, if applicable
            if (!k.isBatchMgmtEnabled()) {
              linkedKiosk.put(JsonTagsZ.DISABLE_BATCH_MGMT, "true");
            }
            // Add to vector
            linkedKiosks.add(linkedKiosk);
          } catch (Exception e) {
            xLogger.warn("{0} when getting linked kiosk {1} of kiosk {2} and link type {3}: {4}",
                e.getClass().getName(), link.getLinkedKioskId(), kioskId, linkType, e.getMessage());
          }
        } // end while
      } finally {
        // Close pm
        pm.close();
      }
    } catch (ServiceException e) {
      xLogger
          .warn("ServiceException when getting KioskLinks for {0}: {1}", kioskId, e.getMessage());
    }
    xLogger.fine("getLinkedKiosks: linkedKiosks: {0}", linkedKiosks);
    return new Results(linkedKiosks, cursor);
  }

  // Schedule export of inventory, transactions or orders via REST for a given kiosk
  // JSON response string is returned
  public static String scheduleKioskDataExport(HttpServletRequest req,
                                               ResourceBundle backendMessages,
                                               HttpServletResponse resp) throws ProtocolException {
    xLogger.fine("Entered scheduleKioskDataExport");
    Locale locale = null;
    String timezone;
    String message;
    boolean isValid = true;
    Long domainId;
    Map<String, String> reqParamsMap = new HashMap<>(1);
    String userId = req.getParameter(RestConstantsZ.USER_ID);
    String password = req.getParameter(RestConstantsZ.PASSWORD);
    reqParamsMap.put(RestConstantsZ.TYPE, req.getParameter(RestConstantsZ.TYPE));
    reqParamsMap.put(RestConstantsZ.KIOSK_ID, req.getParameter(RestConstantsZ.KIOSK_ID));
    reqParamsMap.put(RestConstantsZ.USER_ID, userId);
    reqParamsMap.put(RestConstantsZ.PASSWORD, password);
    reqParamsMap.put(RestConstantsZ.EMAIL, req.getParameter(RestConstantsZ.EMAIL));
    reqParamsMap.put(RestConstantsZ.ORDER_TYPE, req.getParameter(RestConstantsZ.ORDER_TYPE));
    ParsedRequest parsedRequest = parseGeneralExportFilters(reqParamsMap, backendMessages);
    if (StringUtils.isNotEmpty(parsedRequest.errMessage)) {
      isValid = false;
      message = parsedRequest.errMessage;
      return getKioskDataExportJsonOutput(locale, isValid, message);
    }
    try {
      // Authenticate user
      IUserAccount
          u =
          RESTUtil.authenticate(userId, password, (Long) parsedRequest.parsedReqMap.get(
                  RestConstantsZ.KIOSK_ID), req,
              resp); // NOTE: throws ServiceException in case of invalid credentials or no authentication
      if (userId == null) { // can be the case if BasicAuth was used
        userId = u.getUserId();
      }
      domainId = u.getDomainId();
      locale = u.getLocale();
      timezone = u.getTimezone();
    } catch (UnauthorizedException e) {
      isValid = false;
      message = e.getMessage();
      resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
      return getKioskDataExportJsonOutput(locale, isValid, message);
    } catch (Exception e) {
      isValid = false;
      message = e.getMessage();
      return getKioskDataExportJsonOutput(locale, isValid, message);
    }
    // Order filters
    ParsedRequest parsedOrderExportFilters = parseOrdersExportFilters(reqParamsMap);
    if (StringUtils.isNotEmpty(parsedOrderExportFilters.errMessage)) {
      isValid = false;
      message = parsedOrderExportFilters.errMessage;
      return getKioskDataExportJsonOutput(locale, isValid, message);
    }
    parsedRequest.parsedReqMap.putAll(parsedOrderExportFilters.parsedReqMap);
    // Transaction filters
    reqParamsMap
        .put(RestConstantsZ.TRANSACTION_TYPE, req.getParameter(RestConstantsZ.TRANSACTION_TYPE));
    reqParamsMap.put(RestConstantsZ.MATERIAL_ID, req.getParameter(RestConstantsZ.MATERIAL_ID));
    reqParamsMap.put(RestConstantsZ.STARTDATE, req.getParameter(RestConstantsZ.STARTDATE));
    reqParamsMap.put(RestConstantsZ.ENDDATE, req.getParameter(RestConstantsZ.ENDDATE));
    ParsedRequest parsedTransExportFilters =
        parseTransactionsExportFilters(reqParamsMap, backendMessages, timezone);
    if (StringUtils.isNotEmpty(parsedTransExportFilters.errMessage)) {
      isValid = false;
      message = parsedTransExportFilters.errMessage;
      return getKioskDataExportJsonOutput(locale, isValid, message);
    }
    parsedRequest.parsedReqMap.putAll(parsedTransExportFilters.parsedReqMap);
    // Export, if still valid
    Map<String, String> params = new HashMap<>();
    params.put("action", "be"); // batch export
    params.put("type", (String) parsedRequest.parsedReqMap.get(RestConstantsZ.TYPE));
    params.put("domainid", domainId.toString());
    params.put("sourceuserid", userId);
    params.put("userids", userId);
    params.put("kioskid", parsedRequest.parsedReqMap.get(RestConstantsZ.KIOSK_ID).toString());
    if (BulkExportMgr.TYPE_TRANSACTIONS
        .equals(parsedRequest.parsedReqMap.get(RestConstantsZ.TYPE))) {
      if (parsedRequest.parsedReqMap.get(RestConstantsZ.ENDDATE) != null) {
        params.put("to", LocalDateUtil
            .formatCustom((Date) parsedRequest.parsedReqMap.get(RestConstantsZ.ENDDATE),
                Constants.DATETIME_FORMAT, null));
      }
      if (parsedRequest.parsedReqMap.get(RestConstantsZ.STARTDATE) != null) {
        params.put("from", LocalDateUtil
            .formatCustom((Date) parsedRequest.parsedReqMap.get(RestConstantsZ.STARTDATE),
                Constants.DATETIME_FORMAT, null));
      } else {
        // Export a year's worth of transactions/orders
        Calendar cal = GregorianCalendar.getInstance();
        cal.add(Calendar.YEAR, -1);
        params.put("from",
            LocalDateUtil.formatCustom(cal.getTime(), Constants.DATETIME_FORMAT, null));
      }
      if (parsedRequest.parsedReqMap.get(RestConstantsZ.MATERIAL_ID) != null) {
        params.put("materialid",
            parsedRequest.parsedReqMap.get(RestConstantsZ.MATERIAL_ID).toString());
      }
      if (parsedRequest.parsedReqMap.get(RestConstantsZ.TRANSACTION_TYPE) != null) {
        params.put("transactiontype",
            parsedRequest.parsedReqMap.get(RestConstantsZ.TRANSACTION_TYPE).toString());
      }
    }
    params.put("emailid", (String) parsedRequest.parsedReqMap.get(RestConstantsZ.EMAIL));
    if (BulkExportMgr.TYPE_ORDERS.equals(parsedRequest.parsedReqMap.get(RestConstantsZ.TYPE))) {
      // Export a year's worth of transactions/orders
      Calendar cal = GregorianCalendar.getInstance();
      cal.add(Calendar.YEAR, -1);
      params.put("from",
          LocalDateUtil.formatCustom(cal.getTime(), Constants.DATETIME_FORMAT, null));

      params.put("otype", (String) parsedRequest.parsedReqMap.get(RestConstantsZ.ORDER_TYPE));
      params.put("orderType", (String) parsedRequest.parsedReqMap.get("orderType"));
    }
    // Get headers to target backend
    Map<String, String> headers = BulkExportMgr.getExportBackendHeader();
    // Create a entry in the JobStatus table using JobUtil.createJob method.
    Long
        jobId =
        JobUtil.createJob(domainId, userId, null, IJobStatus.TYPE_EXPORT, params.get("type"),
            params);
    if (jobId != null) {
      params.put("jobid", jobId.toString());
    }
    // Schedule task
    try {
      taskService.schedule(taskService.QUEUE_EXPORTER, EXPORT_URL, params, headers,
          taskService.METHOD_POST, domainId, userId, "EXPORT_KIOSK");
      message =
          "Successfully scheduled export, data will be sent to email address \""
              + parsedRequest.parsedReqMap.get(RestConstantsZ.EMAIL)
              + "\".";
    } catch (Exception e) {
      xLogger.severe(
          "{0} when scheduling task for kiosk data export of type {1} for kiosk {2} in domain {3} by user {4}: ",
          e.getClass().getName(), parsedRequest.parsedReqMap.get(RestConstantsZ.TYPE),
          parsedRequest.parsedReqMap.get(RestConstantsZ.KIOSK_ID), domainId, userId, e
      );
      isValid = false;
      message =
          "A system error occurred. Please retry your export once again, or contact your Administrator.";
      return getKioskDataExportJsonOutput(locale, isValid, message);
    }
    return getKioskDataExportJsonOutput(locale, isValid, message);
  }

  private static String getKioskDataExportJsonOutput(Locale locale, boolean status, String message)
      throws ProtocolException {
    String localeStr = Constants.LANG_DEFAULT;
    if (locale != null) {
      localeStr = locale.toString();
    }
    // Form the basic out and sent back JSON return string
    BasicOutput jsonOutput =
        new BasicOutput(status, message, null, localeStr, RESTUtil.VERSION_01);
    return jsonOutput.toJSONString();
  }

  // Get the login expiry time (in milliseconds)
  private static long getLoginExpiry(IUserAccount user) {
    Calendar cal = GregorianCalendar.getInstance();
    cal.setTime(new Date());
    // Add expiry time to this date
    cal.add(Calendar.DATE, Constants.LOGIN_DURATION);

    return cal.getTimeInMillis();
  }

  // Get domain-specific configuration to be sent to mobile
  private static Hashtable<String, Object> getConfig(DomainConfig dc, String role) {
    xLogger.fine("Entered getConfig");
    OrdersConfig oc = dc.getOrdersConfig();
    // Get domain config
    if (dc != null) {
      Hashtable<String, Object>
          config =
          new Hashtable<String, Object>(); // Changed to Hashtable<String,Object> to accomodate mst, rsnsbytg and tgiov which are JSON objects
      String transNaming = dc.getTransactionNaming();
      if (transNaming != null) {
        config.put(JsonTagsZ.TRANSACTION_NAMING, transNaming);
      }
      String orderGen = dc.getOrderGeneration();
      if (orderGen == null) {
        orderGen = DomainConfig.ORDERGEN_DEFAULT;
      }
      config.put(JsonTagsZ.ORDER_GENERATION, orderGen);
      // Get config. on auto-posting of inventory
      config.put(JsonTagsZ.UPDATESTOCK_ON_SHIPPED, String.valueOf(dc.autoGI()));
      config.put(JsonTagsZ.UPDATESTOCK_ON_FULFILLED, String.valueOf(dc.autoGR()));
      // Role-specific configs.
      CapabilityConfig cconf = dc.getCapabilityByRole(role);
      String transMenu = null, tagsInventory = null, tagsOrders = null, geoCodingStrategy = null,
          creatableEntityTypes =
              null;
      boolean allowRouteTagEditing = false, loginAsReconnect = false;
      boolean sendVendors = false, sendCustomers = false, enableShippingOnMobile = false;
      // Inventory tags to hide by operation, if any
      Hashtable<String, String> invTgsToHide = null;
      if (cconf != null) { // send role-specific configuration
        transMenu = StringUtil.getCSV(cconf.getCapabilities());
        tagsInventory = cconf.getTagsInventory();
        tagsOrders = cconf.getTagsOrders();
        sendVendors = cconf.sendVendors();
        sendCustomers = cconf.sendCustomers();
        geoCodingStrategy = cconf.getGeoCodingStrategy();
        creatableEntityTypes = StringUtil.getCSV(cconf.getCreatableEntityTypes());
        allowRouteTagEditing = cconf.allowRouteTagEditing();
        loginAsReconnect = cconf.isLoginAsReconnect();
        enableShippingOnMobile = cconf.isEnableShippingOnMobile();
        invTgsToHide = getInventoryTagsToHide(cconf);
      } else { // send generic configuration
        transMenu = dc.getTransactionMenusString();
        tagsInventory = dc.getTagsInventory();
        tagsOrders = dc.getTagsOrders();
        sendVendors = dc.sendVendors();
        sendCustomers = dc.sendCustomers();
        geoCodingStrategy = dc.getGeoCodingStrategy();
        creatableEntityTypes = StringUtil.getCSV(dc.getCreatableEntityTypes());
        allowRouteTagEditing = dc.allowRouteTagEditing();
        loginAsReconnect = dc.isLoginAsReconnect();
        enableShippingOnMobile = dc.isEnableShippingOnMobile();
        invTgsToHide = getInventoryTagsToHide(dc);
      }
      if (invTgsToHide != null && !invTgsToHide.isEmpty()) {
        config.put(JsonTagsZ.TAGS_INVENTORY_OPERATION, invTgsToHide);
      }

      if (transMenu != null) {
        config.put(JsonTagsZ.TRANSACTIONS, transMenu);
      }
      if (tagsInventory != null && !tagsInventory.isEmpty()) {
        config.put(JsonTagsZ.TAGS_INVENTORY, tagsInventory);
      }
      if (tagsOrders != null && !tagsOrders.isEmpty()) {
        config.put(JsonTagsZ.TAGS_ORDERS, tagsOrders);
      }
      if (sendVendors) {
        config.put(JsonTagsZ.VENDORS_MANDATORY, JsonTagsZ.STATUS_TRUE);
      }
      if (sendCustomers) {
        config.put(JsonTagsZ.CUSTOMERS_MANDATORY, JsonTagsZ.STATUS_TRUE);
      }
      if (dc.allowEmptyOrders()) {
        config.put(JsonTagsZ.ALLOW_EMPTY_ORDERS, JsonTagsZ.STATUS_TRUE);
      }
      if (dc.allowMarkOrderAsFulfilled()) {
        config.put(JsonTagsZ.ORDER_MARKASFULFILLED, JsonTagsZ.STATUS_TRUE);
      }
      if (dc.getPaymentOptions() != null && !dc.getPaymentOptions().isEmpty()) {
        config.put(JsonTagsZ.PAYMENT_OPTION, dc.getPaymentOptions());
      }
      if (dc.getPackageSizes() != null && !dc.getPackageSizes().isEmpty()) {
        config.put(JsonTagsZ.PACKAGE_SIZE, dc.getPackageSizes());
      }
      if (dc.getOrderTags() != null && !dc.getOrderTags().isEmpty()) {
        config.put(JsonTagsZ.ORDER_TAGS, dc.getOrderTags());
      }
      if (geoCodingStrategy != null) {
        config.put(JsonTagsZ.GEOCODING_STRATEGY, geoCodingStrategy);
      }
      if (creatableEntityTypes != null && !creatableEntityTypes.isEmpty()) {
        config.put(JsonTagsZ.CREATABLE_ENTITY_TYPES, creatableEntityTypes);
      }
      if (allowRouteTagEditing) {
        config.put(JsonTagsZ.ALLOW_ROUTETAG_EDITING, String.valueOf(allowRouteTagEditing));
      }
      if (loginAsReconnect) {
        config.put(JsonTagsZ.LOGIN_AS_RECONNECT, String.valueOf(loginAsReconnect));
      }
      if (enableShippingOnMobile) // Set the eshp flag only if it has to be true.
      {
        config.put(JsonTagsZ.ENABLE_SHIPPING_MOBILE, String.valueOf(enableShippingOnMobile));
      }
      // Send transaction reasons
      // Get wastage reason from the higest level, always
      String wastageReasons = dc.getWastageReasons();
      if (wastageReasons != null && !wastageReasons.isEmpty()) {
        config.put(JsonTagsZ.REASONS_WASTAGE, StringUtil.getCSV(new ArrayList<>(
            new LinkedHashSet<>(Arrays.asList(wastageReasons.split(CharacterConstants.COMMA))))));
      }
      InventoryConfig ic = dc.getInventoryConfig();
      // Issue reasons, if any
      String issueReasons = ic.getTransReason(ITransaction.TYPE_ISSUE);
      if (issueReasons != null && !issueReasons.isEmpty()) {
        config.put(JsonTagsZ.REASONS_ISSUE, StringUtil.getCSV(new ArrayList<>(
            new LinkedHashSet<>(Arrays.asList(issueReasons.split(CharacterConstants.COMMA))))));
      }
      // Receipt reasons, if any
      String receiptReasons = ic.getTransReason(ITransaction.TYPE_RECEIPT);
      if (receiptReasons != null && !receiptReasons.isEmpty()) {
        config.put(JsonTagsZ.REASONS_RECEIPT, StringUtil.getCSV(new ArrayList<>(
            new LinkedHashSet<>(Arrays.asList(receiptReasons.split(CharacterConstants.COMMA))))));
      }
      // Stockcount reasons, if any
      String stockcountReasons = ic.getTransReason(ITransaction.TYPE_PHYSICALCOUNT);
      if (stockcountReasons != null && !stockcountReasons.isEmpty()) {
        config.put(JsonTagsZ.REASONS_STOCKCOUNT, StringUtil.getCSV(new ArrayList<>(
            new LinkedHashSet<>(
                Arrays.asList(stockcountReasons.split(CharacterConstants.COMMA))))));
      }
      // Transfer reasons, if any
      String transferReasons = ic.getTransReason(ITransaction.TYPE_TRANSFER);
      if (transferReasons != null && !transferReasons.isEmpty()) {
        config.put(JsonTagsZ.REASONS_TRANSFER, StringUtil.getCSV(new ArrayList<>(
            new LinkedHashSet<>(Arrays.asList(transferReasons.split(CharacterConstants.COMMA))))));
      }

      // Reasons by material tag, if any
      Hashtable<String, Hashtable<String, String>> rsnsByMtag = getReasonsByTag(ic);
      if (rsnsByMtag != null && !rsnsByMtag.isEmpty()) {
        config.put(JsonTagsZ.REASONS_BY_TAG, rsnsByMtag);
      }

      // Material Status, if any
      Hashtable<String, Hashtable<String, String>> mStHt = getMaterialStatus(ic);
      if (mStHt != null && !mStHt.isEmpty()) {
        config.put(JsonTagsZ.MATERIAL_STATUS_OPERATION, mStHt);
      }

      //Min Max Frequency
      String minMaxFreq = ic.getMinMaxDur();
      if (minMaxFreq != null && !minMaxFreq.isEmpty()) {
        if (ic.FREQ_DAILY.equals(minMaxFreq)) {
          config.put(JsonTagsZ.MINMAX_FREQUENCY, 'd');
        } else if (ic.FREQ_WEEKLY.equals(minMaxFreq)) {
          config.put(JsonTagsZ.MINMAX_FREQUENCY, 'w');
        } else if (ic.FREQ_MONTHLY.equals(minMaxFreq)) {
          config.put(JsonTagsZ.MINMAX_FREQUENCY, 'm');
        }
      }
      if (ic.getPermissions() != null && ic.getPermissions().invCustomersVisible) {
        config.put(JsonTagsZ.INVENTORY_VISIBLE, true);
      }
      // Currency
      String currency = dc.getCurrency();
      if (currency != null && !currency.isEmpty()) {
        config.put(JsonTagsZ.CURRENCY, currency);
      }
      // Route tags, if any
      if (dc.getRouteTags() != null) {
        config.put(JsonTagsZ.ROUTE_TAG, dc.getRouteTags());
      }
      // The following code was added to fix LS-1227.
      // Default vendor ID and Default vendor Name if any
      Long defaultVendorId = dc.getVendorId();
      if (defaultVendorId != null) {
        config.put(JsonTagsZ.VENDORID, defaultVendorId.toString());
        // Get the default vendor name
        try {
          EntitiesService as = Services.getService(EntitiesServiceImpl.class);
          IKiosk k = as.getKiosk(defaultVendorId, false);
          if (k != null) {
            config.put(JsonTagsZ.VENDOR, k.getName());
            config.put(JsonTagsZ.VENDOR_CITY, k.getCity());
          }
        } catch (ServiceException se) {
          xLogger.warn("ServiceException when getting default vendor name for vendor id {0}: {1}",
              defaultVendorId, se.getMessage());
        }
      }
      // The support related information should be added here.
      // Get the general config from system configuration
      GeneralConfig gc = null;
      try {
        gc = GeneralConfig.getInstance();
      } catch (ConfigurationException ce) {
        xLogger.warn("Exception while getting GeneralConfiguration. Message: {0}", ce.getMessage());
      }
      if (gc != null && gc.getAupg() != null) {
        Hashtable<String, String> upgradeConfig = getAppUpgradeVersion(gc);
        if (upgradeConfig != null) {
          config.put(JsonTagsZ.APP_UPGRADE, upgradeConfig);
        }
      }
      SupportConfig
          supportConfig =
          dc.getSupportConfigByRole(
              role); // Get the support configuration for the role of the logged in user
      String supportEmail = null, supportPhone = null, supportContactName = null;
      // If Support is configured in Domain configuration, get support information.
      if (supportConfig != null) {
        // Get the supportUserId or support user name from Support configuration.
        String supportUserId = supportConfig.getSupportUser();

        if (supportUserId != null && !supportUserId.isEmpty()) {
          // If userId is configured in SupportConfig, get the phone number, email and contact name from the UserAccount object
          try {
            UsersService as = Services.getService(UsersServiceImpl.class);
            IUserAccount ua = as.getUserAccount(supportUserId);
            supportEmail = ua.getEmail();
            supportPhone = ua.getMobilePhoneNumber();
            supportContactName = ua.getFullName();
          } catch (SystemException se) {
            xLogger
                .warn("ServiceException when getting support user with id {0}: {1}", supportUserId,
                    se.getMessage());
          } catch (ObjectNotFoundException onfe) {
            xLogger.warn("ObjectNotFoundException when getting support user with id {0}: {1}",
                supportUserId, onfe.getMessage());
          }
        } else {
          supportPhone = supportConfig.getSupportPhone();
          supportEmail = supportConfig.getSupportEmail();
          supportContactName = supportConfig.getSupportUserName();
          // The extra check below is added because if a support info is removed from the
          // General Configuration, supportConfig exists but values for supportPhone, supportEmail and supportContactName are ""
          // In that case, it has to send the support information present in System Configuration -> generalconfig
          if (supportPhone == null || supportPhone.isEmpty() && gc != null) {
            supportPhone = gc.getSupportPhone();
          }
          if (supportEmail == null || supportEmail.isEmpty() && gc != null) {
            supportEmail = gc.getSupportEmail();
          }
        }
      } else {
        // If Support is not configured under Domain configuration, get the support phone number and email from the System configuration
        if (gc != null) {
          supportEmail = gc.getSupportEmail();
          supportPhone = gc.getSupportPhone();
        }
      }
      // Add support configuration only if it is present in Domain configuration or in System configuration.
      if (supportContactName != null && !supportContactName.isEmpty()) {
        config.put(JsonTagsZ.SUPPORT_CONTACT_NAME, supportContactName);
      }
      if (supportEmail != null && !supportEmail.isEmpty()) {
        config.put(JsonTagsZ.SUPPORT_EMAIL, supportEmail);
      }
      if (supportPhone != null && !supportPhone.isEmpty()) {
        config.put(JsonTagsZ.SUPPORT_PHONE, supportPhone);
      }
      // Synchronization by mobile configuration to be read from DomainConfig. It should be sent to the mobile app, only if the values of intervals are > 0
      SyncConfig syncConfig = dc.getSyncConfig();
      if (syncConfig != null) {
        Hashtable<String, String> intrvlsHt = getIntervalsHashtable(syncConfig);
        if (intrvlsHt != null && !intrvlsHt.isEmpty()) {
          config.put(JsonTagsZ.INTERVALS, intrvlsHt);
        }
      }
      // Local login required
      config.put(JsonTagsZ.NO_LOCAL_LOGIN_WITH_VALID_TOKEN,
          String.valueOf(!dc.isLocalLoginRequired()));
      // Add the config. to capture actual date of trans, if enabled
      Hashtable<String, Hashtable<String, String>> aTdHt = getActualTransDate(ic);
      if (aTdHt != null && !aTdHt.isEmpty()) {
        config.put(JsonTagsZ.CAPTURE_ACTUAL_TRANSACTION_DATE, aTdHt);
      }
      if (dc.isDisableOrdersPricing()) {
        config.put(JsonTagsZ.DISABLE_ORDER_PRICING, String.valueOf(dc.isDisableOrdersPricing()));
      }
      // Add order reasons to config
      addReasonsConfiguration(config, oc);
      Hashtable<String, Object> oCfg = getOrdersConfiguration(dc);
      if (!oCfg.isEmpty()) {
        config.put(JsonTagsZ.ORDER_CONFIG, oCfg);
      }

      try {
        SMSConfig smsConfig = SMSConfig.getInstance();
        if (smsConfig != null) {
          String country = dc.getCountry() != null ? dc.getCountry() : Constants.COUNTRY_DEFAULT;
          // For incoming
          SMSConfig.ProviderConfig
              iProviderConfig =
              smsConfig.getProviderConfig(smsConfig.getProviderId(country, SMSService.INCOMING));
          Hashtable<String, String> sms = new Hashtable<>(3);
          if (iProviderConfig != null) {
            sms.put(JsonTagsZ.GATEWAY_PHONE_NUMBER,
                iProviderConfig.getString(SMSConfig.ProviderConfig.LONGCODE));
            sms.put(JsonTagsZ.GATEWAY_ROUTING_KEYWORD,
                iProviderConfig.getString(SMSConfig.ProviderConfig.KEYWORD));
          }
          //For outgoing
          SMSConfig.ProviderConfig
              oProviderConfig =
              smsConfig.getProviderConfig(smsConfig.getProviderId(country, SMSService.OUTGOING));
          if (oProviderConfig != null) {
            sms.put(JsonTagsZ.SENDER_ID,
                oProviderConfig.getString(SMSConfig.ProviderConfig.SENDER_ID));
          }
          config.put(JsonTagsZ.SMS, sms);
        }
        // Approval configuration
        ApprovalsConfig approvalsConfig = dc.getApprovalsConfig();
        if (approvalsConfig != null) {
          MobileConfigBuilder mobileConfigBuilder = new MobileConfigBuilder();
          MobileApprovalsConfigModel mobileApprovalsConfigModel = mobileConfigBuilder.buildApprovalConfiguration(approvalsConfig);
          if (mobileApprovalsConfigModel != null) {
            config.put(JsonTagsZ.APPROVALS, mobileApprovalsConfigModel);
          }
        }
      } catch (Exception e) {
        xLogger.warn("Error in getting system configuration: {0}", e);
      }
      // Return config
      if (config.isEmpty()) {
        return null;
      } else {
        xLogger.fine("Exiting getConfig, config: {0}", config.toString());
        return config;
      }
    }
    return null;
  }

  // Check if the stock value should be sent back as an integer or float - depending on app. version (beyond 1.2.0 it is float)
  public static boolean forceIntegerForStock(String appVersion) {
    if (appVersion == null) {
      return true;
    }
    String v = appVersion.replaceAll("\\.", "");
    try {
      int n = Integer.parseInt(v);
      if (n >= 200) {
        return false;
      }
    } catch (NumberFormatException e) {
      xLogger.warn("Invalid app. version number when checking force-integer-for-stock, {0}: {1}",
          appVersion, e.getMessage());
    }
    return true;
  }

  private static void addReasonsConfiguration(Hashtable config, OrdersConfig oc) {
    Hashtable<String, Object> hs = new Hashtable<>();
    if (oc.getOrderRecommendationReasons() != null && !oc.getOrderRecommendationReasons()
        .isEmpty()) {
      hs.put(JsonTagsZ.REASONS, oc.getOrderRecommendationReasons());
    }
    hs.put(JsonTagsZ.MANDATORY, oc.getOrderRecommendationReasonsMandatory());
    config.put(JsonTagsZ.IGNORE_ORDER_RECOMMENDATION_REASONS, hs);

    // Partial Fulfillment reasons
    Hashtable<String, Object> pfRsnsHt = new Hashtable<>();
    if (oc.getPartialFulfillmentReasons() != null && !oc.getPartialFulfillmentReasons().isEmpty()) {
      pfRsnsHt.put(JsonTagsZ.REASONS, oc.getPartialFulfillmentReasons());
    }
    pfRsnsHt.put(JsonTagsZ.MANDATORY, oc.getPartialFulfillmentReasonsMandatory());
    config.put(JsonTagsZ.REASONS_FOR_PARTIAL_FULFILLMENT, pfRsnsHt);

    // Partial shipment reasons
    Hashtable<String, Object> psRsnsHt = new Hashtable<>();
    if (oc.getPartialShipmentReasons() != null && !oc.getPartialShipmentReasons().isEmpty()) {
      psRsnsHt.put(JsonTagsZ.REASONS, oc.getPartialShipmentReasons());
    }
    psRsnsHt.put(JsonTagsZ.MANDATORY, oc.getPartialShipmentReasonsMandatory());
    config.put(JsonTagsZ.REASONS_FOR_PARTIAL_SHIPMENT, psRsnsHt);

    // Cancellation order reasons
    Hashtable<String, Object> coRsnsHt = new Hashtable<>();
    if (oc.getCancellingOrderReasons() != null && !oc.getCancellingOrderReasons().isEmpty()) {
      coRsnsHt.put(JsonTagsZ.REASONS, oc.getCancellingOrderReasons());
    }
    coRsnsHt.put(JsonTagsZ.MANDATORY, oc.getCancellingOrderReasonsMandatory());
    config.put(JsonTagsZ.REASONS_FOR_CANCELLING_ORDER, coRsnsHt);

    // Reasons for editing order quantity
    Hashtable<String, Object> eoRsnsHt = new Hashtable<>();
    if (oc.getEditingQuantityReasons() != null && !oc.getEditingQuantityReasons().isEmpty()) {
      eoRsnsHt.put(JsonTagsZ.REASONS, oc.getEditingQuantityReasons());
    }
    eoRsnsHt.put(JsonTagsZ.MANDATORY, oc.getEditingQuantityReasonsMandatory());
    config.put(JsonTagsZ.REASONS_FOR_EDITING_ORDER_QUANTITY, eoRsnsHt);
  }

  // Method to switch to new host if configured in the Domain configuration for a user's domain
  public static boolean switchToNewHostIfRequired(IUserAccount u, HttpServletResponse resp) {
    // Get the configuration. Check if switch to new host is enabled. If yes, get the new host name and return 409 response.
    Long domainId = u.getDomainId();
    DomainConfig dc = DomainConfig.getInstance(domainId);
    boolean switched = false;
    if (dc.isEnableSwitchToNewHost() && dc.getNewHostName() != null && !dc.getNewHostName()
        .isEmpty()) {
      xLogger.fine("Switch to new host is enabled for domainId: {0}, dc.getNewHostName(): {1}",
          domainId, dc.getNewHostName());
      try {
        resp.setStatus(409);
        resp.setContentType("text/plain; charset=UTF-8");
        PrintWriter pw = resp.getWriter();
        pw.write(dc.getNewHostName());
        pw.close();
        switched = true;
      } catch (IOException e) {
        xLogger
            .severe("Exception {0} sending error message to user {1} in domain {2}. Message: {3}",
                e.getClass().getName(), u.getUserId(), domainId, e.getMessage());
      }
    }
    return switched;
  }

  // Method to get transaction history
  @SuppressWarnings("unchecked")
  public static Results getTransactions(Long domainId, Long kioskId, Locale locale, String timezone,
                                        DomainConfig dc, Date untilDate, PageParams pageParams, String materialTag)
      throws ServiceException {
    // Get the services
    InventoryManagementService
        ims =
        Services.getService(InventoryManagementServiceImpl.class);
    UsersService as = Services.getService(UsersServiceImpl.class);
    EntitiesService entitiesService = Services.getService(EntitiesServiceImpl.class);
    MaterialCatalogService mcs = Services.getService(MaterialCatalogServiceImpl.class);
    // Get the transactions
    Results
        results =
        ims.getInventoryTransactionsByKiosk(kioskId, materialTag, null, untilDate, null, pageParams, null,
            false, null);
    List<ITransaction> transactions = (List<ITransaction>) results.getResults();
    String cursor = results.getCursor();
    Vector<Hashtable<String, String>> transData = new Vector<Hashtable<String, String>>();
    Iterator<ITransaction> it = transactions.iterator();
    while (it.hasNext()) {
      ITransaction trans = it.next();
      // Create a transaction data Hashtable
      Hashtable<String, String> transaction = new Hashtable<String, String>();
      transaction.put(JsonTagsZ.MATERIAL_ID, trans.getMaterialId().toString());

      IMaterial m;
      try {
        m = mcs.getMaterial(trans.getMaterialId());
      } catch (ServiceException e) {
        xLogger.warn("Exception while getting material for material ID {0}",
            trans.getMaterialId()); // Material may have been deleted, skip this transaction
        continue;
      }
      if (!materialExistsInKiosk(trans.getKioskId(), trans.getMaterialId())) {
          transaction.put(JsonTagsZ.MATERIAL_NAME, m.getName());
      }
      transaction.put(JsonTagsZ.TRANSACTION_TYPE, trans.getType());
      transaction.put(JsonTagsZ.QUANTITY, String.valueOf(trans.getQuantity()));

      Date time = trans.getTimestamp();
      transaction.put(JsonTagsZ.TIMESTAMP, LocalDateUtil.format(time, locale, timezone));

      if (trans.getReason() != null && !trans.getReason().isEmpty()) {
        transaction.put(JsonTagsZ.REASON, trans.getReason());
      }
      transaction.put(JsonTagsZ.OPENING_STOCK, String.valueOf(trans.getOpeningStock()));
      transaction.put(JsonTagsZ.CLOSING_STOCK, String.valueOf(trans.getClosingStock()));
      String userId = trans.getSourceUserId();
      if (userId
          != null) { // typically, this is not null, but could be for very old trans. generated using very old code where this functionality did not exist
        transaction.put(JsonTagsZ.USER_ID, userId);
        try {
          IUserAccount u = as.getUserAccount(userId);
          String userName = u.getFullName();
          transaction.put(JsonTagsZ.USER, userName);
        } catch (Exception e) {
          xLogger.warn("{0} while getting user name for userId: {1}. Message:{2}",
              e.getClass().getName(), userId, e.getMessage());
        }
      }

      Long linkedKioskId = trans.getLinkedKioskId();
      if (linkedKioskId != null) {
        transaction.put(JsonTagsZ.LINKED_KIOSK_ID, String.valueOf(linkedKioskId));
        try {
          IKiosk linkedKiosk = entitiesService.getKiosk(linkedKioskId, false);
          String linkedKioskName = linkedKiosk.getName();
          transaction.put(JsonTagsZ.LINKED_KIOSK_NAME, linkedKioskName);
        } catch (Exception e) {
          xLogger.warn("{0} while getting kiosk name for linkedKioskId: {1}. Message:{2}",
              e.getClass().getName(), linkedKioskId, e.getMessage());
        }
      }
      if (trans.getBatchId() != null && !trans.getBatchId().isEmpty()) {
        transaction.put(JsonTagsZ.BATCH_ID, trans.getBatchId());
      }
      if (m.isBatchEnabled()) {
        transaction
            .put(JsonTagsZ.OPENING_STOCK_IN_BATCH, String.valueOf(trans.getOpeningStockByBatch()));
      }

      Date batchExpiry = trans.getBatchExpiry();
      if (batchExpiry != null) {
        transaction.put(JsonTagsZ.BATCH_EXPIRY,
            LocalDateUtil.formatCustom(batchExpiry, Constants.DATE_FORMAT, null));
      }

      if (trans.getBatchManufacturer() != null && !trans.getBatchManufacturer().isEmpty()) {
        transaction.put(JsonTagsZ.BATCH_MANUFACTUER_NAME, trans.getBatchManufacturer());
      }

      Date batchMfdDate = trans.getBatchManufacturedDate();
      if (batchMfdDate != null) {
        transaction.put(JsonTagsZ.BATCH_MANUFACTURED_DATE,
            LocalDateUtil.formatCustom(batchMfdDate, Constants.DATE_FORMAT, null));
      }

      if (m.isBatchEnabled()) {
        transaction
            .put(JsonTagsZ.CLOSING_STOCK_IN_BATCH, String.valueOf(trans.getClosingStockByBatch()));
      }

      if (StringUtils.isNotBlank(trans.getMaterialStatus())) {
        transaction.put(JsonTagsZ.MATERIAL_STATUS, trans.getMaterialStatus());
      }

      Date atd = trans.getAtd();
      if (atd != null) {
        transaction.put(JsonTagsZ.ACTUAL_TRANSACTION_DATE,
            LocalDateUtil.formatCustom(atd, Constants.DATE_FORMAT, null));
      }
      transaction.put(JsonTagsZ.TAGS, StringUtil.getCSV(trans.getTags(TagUtil.TYPE_MATERIAL)));

      transData.add(transaction);
    }
    return new Results(transData, cursor);
  }

  // Get relevant (valid (non zero stock) and non expired) batches for a given inventory
  public static Vector<Hashtable<String, String>> getBatchData(IInvntry inv, Locale locale,
                                                               String timezone,
                                                               InventoryManagementService ims,
                                                               boolean isBatchMgmtEnabled,
                                                               boolean isAutoPostingIssuesEnabled) {
    xLogger.fine("Entered getBatchData");
    Vector<Hashtable<String, String>> batches = null;
    // Only if kiosk is batch management enabled, return batches. Otherwise, return null.
    if (isBatchMgmtEnabled) {
      try {
        Results
            results =
            ims.getValidBatches(inv.getMaterialId(), inv.getKioskId(), new PageParams(null,
                PageParams.DEFAULT_SIZE)); // NOTE: Get only up to the 50 last batches
        if (results != null && results.getResults() != null) {
          batches = new Vector<Hashtable<String, String>>();
          Iterator<IInvntryBatch> it = results.getResults().iterator();
          while (it.hasNext()) {
            IInvntryBatch batch = it.next();
            if (!batch.isExpired()) {
              batches.add(batch.toMapZ(locale, timezone, isAutoPostingIssuesEnabled));
            }
          }
        }
      } catch (Exception e) {
        xLogger.warn("{0} when trying to get batch info. for inv. {1}-{2} in domain {3}: {4}",
            e.getClass().getName(), inv.getKioskId(), inv.getMaterialId(), inv.getDomainId(),
            e.getMessage());
        e.printStackTrace();
      }
    }
    return batches;
  }

  public static Vector<Hashtable<String, String>> getExpiredBatchData(IInvntry inv, Locale locale,
                                                                      String timezone,
                                                                      InventoryManagementService ims,
                                                                      boolean isBatchMgmtEnabled,
                                                                      boolean isAutoPostingIssuesEnabled) {
    xLogger.fine("Entered getExpiredBatchData");
    Vector<Hashtable<String, String>> expiredBatches = null;
    // Only if kiosk is batch management enabled, return batches. Otherwise, return null.
    if (isBatchMgmtEnabled) {
      try {
        Results
            results =
            ims.getBatches(inv.getMaterialId(), inv.getKioskId(), new PageParams(null,
                PageParams.DEFAULT_SIZE)); // NOTE: Get only up to the 50 last batches
        if (results != null && results.getResults() != null) {
          expiredBatches = new Vector<>();
          Iterator<IInvntryBatch> it = results.getResults().iterator();
          while (it.hasNext()) {
            IInvntryBatch batch = it.next();
            if (batch.isExpired() && BigUtil.greaterThanZero(batch.getQuantity())) {
              expiredBatches.add(batch.toMapZ(locale, timezone, isAutoPostingIssuesEnabled));
            }
          }
        }
      } catch (Exception e) {
        xLogger.warn("{0} when trying to get batch info. for inv. {1}-{2} in domain {3}: {4}",
            e.getClass().getName(), inv.getKioskId(), inv.getMaterialId(), inv.getDomainId(),
            e.getMessage());
        e.printStackTrace();
      }
    }
    return expiredBatches;
  }

  private static Hashtable<String, Hashtable<String, String>> getReasonsByTag(InventoryConfig ic) {
    Hashtable<String, Hashtable<String, String>> rsnsByMtag = new Hashtable<>(1);
    // For Issue
    Map<String, String> iMtagReasons = ic.getImTransReasons();
    if (iMtagReasons != null && !iMtagReasons.isEmpty()) {
      Hashtable<String, String> iMtagReasonsHt = getHashtableFromMap(iMtagReasons);
      if (iMtagReasonsHt != null && !iMtagReasonsHt.isEmpty()) {
        rsnsByMtag.put(JsonTagsZ.ISSUES, iMtagReasonsHt);
      }
    }
    // For Receipt
    Map<String, String> rMtagReasons = ic.getRmTransReasons();
    if (rMtagReasons != null && !rMtagReasons.isEmpty()) {
      Hashtable<String, String> rMTagReasonsHt = getHashtableFromMap(rMtagReasons);
      if (rMTagReasonsHt != null && !rMTagReasonsHt.isEmpty()) {
        rsnsByMtag.put(JsonTagsZ.RECEIPTS, rMTagReasonsHt);
      }
    }
    // For Physical stock count
    Map<String, String> scMtagReasons = ic.getSmTransReasons();
    if (scMtagReasons != null && !scMtagReasons.isEmpty()) {
      Hashtable<String, String> scMTagReasonsHt = getHashtableFromMap(scMtagReasons);
      if (scMTagReasonsHt != null && !scMTagReasonsHt.isEmpty()) {
        rsnsByMtag.put(JsonTagsZ.PHYSICAL_STOCK, scMTagReasonsHt);
      }
    }
    // For Wastage/Discards
    Map<String, String> wMtagReasons = ic.getDmTransReasons();
    if (wMtagReasons != null && !wMtagReasons.isEmpty()) {
      Hashtable<String, String> wMTagReasonsHt = getHashtableFromMap(wMtagReasons);
      if (wMTagReasonsHt != null && !wMTagReasonsHt.isEmpty()) {
        rsnsByMtag.put(JsonTagsZ.DISCARDS, wMTagReasonsHt);
      }
    }
    // For Transfer
    Map<String, String> tMtagReasons = ic.getTmTransReasons();
    if (tMtagReasons != null && !tMtagReasons.isEmpty()) {
      Hashtable<String, String> tMTagReasonsHt = getHashtableFromMap(tMtagReasons);
      if (tMTagReasonsHt != null && !tMTagReasonsHt.isEmpty()) {
        rsnsByMtag.put(JsonTagsZ.TRANSFER, tMTagReasonsHt);
      }
    }

    return rsnsByMtag;
  }

  private static Hashtable<String, String> getHashtableFromMap(Map<String, String> map) {
    Hashtable<String, String> ht = null;
    if (map != null && !map.isEmpty()) {
      ht = new Hashtable<>();
      Set<String> keys = map.keySet();
      if (!keys.isEmpty()) {
        for (String key : keys) {
          String
              value =
              StringUtil.getCSV(new ArrayList<>(new LinkedHashSet<>(
                  Arrays.asList(map.get(key).split(CharacterConstants.COMMA)))));
          ht.put(key, value);
        }
      }
    }
    return ht;
  }

  protected static Hashtable<String, Hashtable<String, String>> getMaterialStatus(
      InventoryConfig ic) {
    Hashtable<String, Hashtable<String, String>> mStHt = new Hashtable<>(1);
    Map<String, MatStatusConfig> mStMap = ic.getMatStatusConfigMapByType();
    if (mStMap != null && !mStMap.isEmpty()) {
      Set<String> transTypes = mStMap.keySet();
      if (!transTypes.isEmpty()) {
        for (String transType : transTypes) {
          MatStatusConfig mStConfig = mStMap.get(transType);
          Hashtable<String, String> mTyStHt = new Hashtable<>();
          if (mStConfig != null) {
            if(mStConfig.getDf() !=null) {
              String df = StringUtil.getUniqueValueCSV(mStConfig.getDf());
              if (StringUtils.isNotEmpty(df)) {
                mTyStHt.put(JsonTagsZ.ALL, df);
              }
            }
            if(mStConfig.getEtsm() != null) {
              String etsm = StringUtil.getUniqueValueCSV(mStConfig.getEtsm());
              if (StringUtils.isNotEmpty(etsm)) {
                mTyStHt.put(JsonTagsZ.TEMP_SENSITVE_MATERIALS, etsm);
              }
            }
            if (!mTyStHt.isEmpty()) {
              mTyStHt.put(JsonTagsZ.MANDATORY, String.valueOf(mStConfig.isStatusMandatory()));
              mStHt.put(transType, mTyStHt);
            }
          }
        }
      }
    }
    return mStHt;
  }

  private static Hashtable<String, Hashtable<String, String>> getActualTransDate(
      InventoryConfig ic) {
    Hashtable<String, Hashtable<String, String>> aTdHt = new Hashtable<>();
    Map<String, ActualTransConfig> aTdMap = ic.getActualTransConfigMapByType();
    if (aTdMap != null && !aTdMap.isEmpty()) {
      Set<String> transTypes = aTdMap.keySet();
      if (!transTypes.isEmpty()) {
        for (String transType : transTypes) {
          ActualTransConfig aTdConf = aTdMap.get(transType);
          Hashtable<String, String> aTyTdHt = new Hashtable<>();
          if (aTdConf != null) {
            String ty = aTdConf.getTy();
            if (ty != null && !ty.isEmpty()) {
              aTyTdHt.put(JsonTagsZ.TYPE, ty);
            }
          }
          if (!aTyTdHt.isEmpty()) {
            aTdHt.put(transType, aTyTdHt);
          }
        }
      }
    }
    return aTdHt;
  }

  private static Hashtable<String, String> getInventoryTagsToHide(Serializable config) {
    Hashtable<String, String> invTgsToHide = new Hashtable<String, String>();
    Map<String, String> tagsByInvOp = null;
    if (config instanceof CapabilityConfig) {
      tagsByInvOp = ((CapabilityConfig) config).gettagsInvByOperation();
    } else if (config instanceof DomainConfig) {
      tagsByInvOp = ((DomainConfig) config).gettagsInvByOperation();
    }

    if (tagsByInvOp != null && !tagsByInvOp.isEmpty()) {
      Set<String> transTypes = tagsByInvOp.keySet();
      if (transTypes != null && !transTypes.isEmpty()) {
        Iterator<String> transTypesIter = transTypes.iterator();
        while (transTypesIter.hasNext()) {
          String transType = transTypesIter.next();
          String tagsToHide = tagsByInvOp.get(transType);
          if (tagsToHide != null && !tagsToHide.isEmpty()) {
            invTgsToHide.put(transType, tagsToHide);
          }
        }
      }
    }
    return invTgsToHide;
  }

  private static Hashtable<String, String> getIntervalsHashtable(SyncConfig syncConfig) {
    Hashtable intrvlsHt = null;
    if (syncConfig != null) {
      intrvlsHt = new Hashtable<String, String>();
      if (syncConfig.getMasterDataRefreshInterval() > 0) {
        intrvlsHt.put(JsonTagsZ.INTERVAL_REFRESHING_MASTER_DATA_HOURS,
            Integer.valueOf(syncConfig.getMasterDataRefreshInterval()).toString());
      }
      if (syncConfig.getAppLogUploadInterval() > 0) {
        intrvlsHt.put(JsonTagsZ.INTERVAL_SENDING_APP_LOG_HOURS,
            Integer.valueOf(syncConfig.getAppLogUploadInterval()).toString());
      }
      if (syncConfig.getSmsTransmissionWaitDuration() > 0) {
        intrvlsHt.put(JsonTagsZ.INTERVAL_WAIT_BEFORE_SENDING_SMS_HOURS,
            Integer.valueOf(syncConfig.getSmsTransmissionWaitDuration()).toString());
      }
    }
    return intrvlsHt;
  }

  private static Hashtable<String, String> getAppUpgradeVersion(GeneralConfig config) {
    Hashtable intrvlsHt = null;
    if (config != null) {
      intrvlsHt = new Hashtable<String, Long>();
      if (config.getAupg() != null) {
        if (config.getAupg().get(JsonTagsZ.VERSION) != null) {
          intrvlsHt.put(JsonTagsZ.VERSION, config.getAupg().get(JsonTagsZ.VERSION));
        }
        if (config.getAupg().get(JsonTagsZ.TIMESTAMP) != null) {
          intrvlsHt
              .put(JsonTagsZ.TIMESTAMP, String.valueOf(config.getAupg().get(JsonTagsZ.TIMESTAMP)));
        }
      }
    }

    return intrvlsHt;
  }


  private static Hashtable<String, Object> getOrdersConfiguration(DomainConfig dc) {
    Hashtable<String, Object> ordCfg = new Hashtable();
    if (dc.autoGI()) {
      ordCfg.put(JsonTagsZ.AUTOMATICALLY_POST_ISSUES_ON_SHIPPING_ORDER, dc.autoGI());
    }
    if (dc.autoGR()) {
      ordCfg.put(JsonTagsZ.AUTOMATICALLY_POST_RECEIPTS_ON_FULFILLING_ORDER, dc.autoGR());
    }
    OrdersConfig oc = dc.getOrdersConfig();
    if (oc != null) {
      ordCfg.put(JsonTagsZ.TRANSFER_RELEASE, oc.isTransferRelease());
    }

    if (dc.isTransporterMandatory()) {
      ordCfg.put(JsonTagsZ.TRANSPORTER_MANDATORY, dc.isTransporterMandatory());
    }
    return ordCfg;
  }

  public static final boolean materialExistsInKiosk(Long kioskId, Long materialId) {
    try {
      InventoryManagementService
          ims =
          Services.getService(InventoryManagementServiceImpl.class);
      IInvntry inv = ims.getInventory(kioskId, materialId);
      if (inv == null) {
        return false;
      }
    } catch (Exception e) {
      xLogger.warn("Exception while getting inventory for kiosk: {0}, material: {0}", kioskId,
          materialId, e);
      return false;
    }
    return true;
  }

  private static ParsedRequest parseGeneralExportFilters(
      Map<String, String> reqParamsMap,
      ResourceBundle backendMessages) {
    ParsedRequest parsedRequest = new ParsedRequest();
    if (reqParamsMap == null || reqParamsMap.isEmpty()) {
      return parsedRequest;
    }
    String kioskIdStr = reqParamsMap.get(RestConstantsZ.KIOSK_ID);
    if (StringUtils.isEmpty(kioskIdStr)) {
      parsedRequest.errMessage = backendMessages.getString("error.invalidkioskid");
      return parsedRequest;
    }
    try {
      Long kioskId = Long.parseLong(kioskIdStr);
      parsedRequest.parsedReqMap.put(RestConstantsZ.KIOSK_ID, kioskId);
    } catch (NumberFormatException e) {
      parsedRequest.errMessage = backendMessages.getString("error.invalidkioskid");
      xLogger.severe("Exception while parsing kiosk id. {0}",
          kioskIdStr, e);
      return parsedRequest;
    }
    String type = reqParamsMap.get(RestConstantsZ.TYPE);
    if (StringUtils.isEmpty(type) || !(INVENTORY.equals(type) || TRANSACTIONS.equals(type)
        || ORDERS.equals(type))) {
      parsedRequest.errMessage = backendMessages.getString("error.invalidexporttype");
      return parsedRequest;
    }
    parsedRequest.parsedReqMap.put(RestConstantsZ.TYPE, type);

    String email = reqParamsMap.get(RestConstantsZ.EMAIL);
    if (StringUtils.isEmpty(email)) {
      parsedRequest.errMessage = backendMessages.getString("error.invalidemail");
      return parsedRequest;
    }
    if (!StringUtil.isEmailValid(email)) {
      parsedRequest.errMessage =
          "Email address " + email
              + " is not valid. Please provide a correct one, or contact your Administrator to configure your email.";
      return parsedRequest;
    }
    parsedRequest.parsedReqMap.put(RestConstantsZ.EMAIL, reqParamsMap.get(RestConstantsZ.EMAIL));
    return parsedRequest;
  }

  private static ParsedRequest parseOrdersExportFilters(Map<String, String> reqParamsMap) {
    ParsedRequest parsedRequest = new ParsedRequest();
    if (reqParamsMap == null || reqParamsMap.isEmpty()) {
      return parsedRequest;
    }
    // Get the order type, if sent
    String otype = reqParamsMap.get(RestConstantsZ.ORDER_TYPE);
    if (StringUtils.isEmpty(otype)) {
      otype = IOrder.TYPE_PURCHASE; // defaulted to purchase orders - Can be sales or purchase
    }
    parsedRequest.parsedReqMap.put(RestConstantsZ.ORDER_TYPE, otype);
    int
        orderType =
        IOrder.NONTRANSFER; // defaulted to non transfer. Can be non transfer or transfer
    String
        spTransfer =
        String.valueOf(
            orderType); // Should be sent from the mobile when transfers are enabled on mobile.
    parsedRequest.parsedReqMap.put("orderType", spTransfer);
    return parsedRequest;
  }

  private static ParsedRequest parseTransactionsExportFilters(
      Map<String, String> reqParamsMap,
      ResourceBundle backendMessages,
      String timezone) {
    ParsedRequest parsedRequest = new ParsedRequest();
    if (reqParamsMap == null || reqParamsMap.isEmpty()) {
      return parsedRequest;
    }
    String endDateStr = reqParamsMap.get(RestConstantsZ.ENDDATE);
    Date endDate = new Date();
    if (StringUtils.isNotEmpty(endDateStr)) {
      try {
        endDate = LocalDateUtil.parseCustom(endDateStr,Constants.DATE_FORMAT,timezone);
        parsedRequest.parsedReqMap.put(RestConstantsZ.ENDDATE, endDate);
      } catch (ParseException pe) {
        parsedRequest.errMessage = backendMessages.getString("error.invalidenddate");
        xLogger.severe("Exception while parsing end date {0}: ",
            endDateStr, pe);
        return parsedRequest;
      }
    } else {
      parsedRequest.parsedReqMap.put(RestConstantsZ.ENDDATE, endDate);
    }
    String startDateStr = reqParamsMap.get(RestConstantsZ.STARTDATE);
    Date startDate = null;
    if (StringUtils.isNotEmpty(startDateStr)) {
      try {
        startDate = LocalDateUtil.parseCustom(startDateStr, Constants.DATE_FORMAT, timezone);
        parsedRequest.parsedReqMap.put(RestConstantsZ.STARTDATE, startDate);
      } catch (ParseException pe) {
        parsedRequest.errMessage = backendMessages.getString("error.notvalidstartdate");
        xLogger.severe("Exception while parsing start date {0}: ", startDateStr, pe);
        return parsedRequest;
      }
    }
    // If startDate is greater than endDate set the error message
    if (startDate != null && startDate.after(endDate)) {
      parsedRequest.errMessage = backendMessages.getString("error.startdateisgreaterthanenddate");
      return parsedRequest;
    }

    String transTypeStr = reqParamsMap.get(RestConstantsZ.TRANSACTION_TYPE);
    if (StringUtils.isNotEmpty(transTypeStr)) {
      if (ITransaction.TYPE_PHYSICALCOUNT.equals(transTypeStr) || ITransaction.TYPE_ISSUE
          .equals(transTypeStr) || ITransaction.TYPE_RECEIPT.equals(transTypeStr)
          || ITransaction.TYPE_TRANSFER.equals(transTypeStr) || ITransaction.TYPE_WASTAGE
          .equals(transTypeStr)) {
        parsedRequest.parsedReqMap.put(RestConstantsZ.TRANSACTION_TYPE, transTypeStr);
      } else {
        parsedRequest.errMessage = backendMessages.getString("error.invalidtransactiontype");
        return parsedRequest;
      }
    }
    String materialIdStr = null;
    if (reqParamsMap.containsKey(RestConstantsZ.MATERIAL_ID)) {
      materialIdStr = reqParamsMap.get(RestConstantsZ.MATERIAL_ID);
    }
    if (StringUtils.isNotEmpty(materialIdStr)) {
      try {
        Long materialId = Long.parseLong(materialIdStr);
        parsedRequest.parsedReqMap.put(RestConstantsZ.MATERIAL_ID, materialId);
      } catch (NumberFormatException e) {
        parsedRequest.errMessage = backendMessages.getString("error.invalidmaterialid");
        xLogger.severe("Exception while parsing material id. {0}",
            materialIdStr, e);
        return parsedRequest;
      }
    }
    return parsedRequest;
  }
}

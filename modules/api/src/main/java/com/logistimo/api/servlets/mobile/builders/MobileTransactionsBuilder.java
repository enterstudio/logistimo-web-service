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

package com.logistimo.api.servlets.mobile.builders;

import com.google.gson.Gson;

import com.logistimo.api.util.RESTUtil;
import com.logistimo.config.models.DomainConfig;
import com.logistimo.config.models.InventoryConfig;
import com.logistimo.constants.Constants;
import com.logistimo.dao.JDOUtils;
import com.logistimo.entities.entity.IKiosk;
import com.logistimo.entities.service.EntitiesService;
import com.logistimo.entities.service.EntitiesServiceImpl;
import com.logistimo.inventory.TransactionUtil;
import com.logistimo.inventory.entity.IInvntry;
import com.logistimo.inventory.entity.IInvntryBatch;
import com.logistimo.inventory.entity.ITransaction;
import com.logistimo.inventory.entity.InvntryBatch;
import com.logistimo.inventory.models.ErrorDetailModel;
import com.logistimo.inventory.service.InventoryManagementService;
import com.logistimo.inventory.service.impl.InventoryManagementServiceImpl;
import com.logistimo.logger.XLog;
import com.logistimo.materials.entity.IMaterial;
import com.logistimo.materials.service.MaterialCatalogService;
import com.logistimo.materials.service.impl.MaterialCatalogServiceImpl;
import com.logistimo.pagination.PageParams;
import com.logistimo.pagination.Results;
import com.logistimo.proto.MobileConsRateModel;
import com.logistimo.proto.MobileGeoModel;
import com.logistimo.proto.MobileInvBatchModel;
import com.logistimo.proto.MobileInvModel;
import com.logistimo.proto.MobileMaterialTransModel;
import com.logistimo.proto.MobileTransErrModel;
import com.logistimo.proto.MobileTransErrorDetailModel;
import com.logistimo.proto.MobileTransModel;
import com.logistimo.proto.MobileTransactionModel;
import com.logistimo.proto.MobileTransactionsModel;
import com.logistimo.proto.MobileUpdateInvTransRequest;
import com.logistimo.proto.MobileUpdateInvTransResponse;
import com.logistimo.services.ServiceException;
import com.logistimo.services.Services;
import com.logistimo.tags.TagUtil;
import com.logistimo.users.entity.IUserAccount;
import com.logistimo.users.service.UsersService;
import com.logistimo.users.service.impl.UsersServiceImpl;
import com.logistimo.utils.BigUtil;
import com.logistimo.utils.LocalDateUtil;
import com.logistimo.utils.StringUtil;

import org.apache.commons.lang.StringUtils;

import java.math.BigDecimal;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * Created by vani on 18/01/17.
 */
public class MobileTransactionsBuilder {
  private static final XLog xLogger = XLog.getLog(MobileTransactionsBuilder.class);

  private static final int SUCCESS = 0;
  private static final int ERROR = 1;
  private static final int PARTIAL_ERROR = 2;

  private static final String VERSION_01 = "01";

  /**
   * Builds a transactions request object as required by the mobile from a json string
   *
   * @param updateInvTransReqJson - JSON string containing the transactions request as sent by the mobile
   * @return - MobileUpdateInvTransRequest object
   */
  public MobileUpdateInvTransRequest buildMobileUpdateInvTransRequest(
      String updateInvTransReqJson) {
    return new Gson().fromJson(updateInvTransReqJson,
        MobileUpdateInvTransRequest.class);
  }


  public MobileTransactionsModel build(List<ITransaction> transactions, Long kioskId, Locale locale,
                                       String timezone) {
    MobileTransactionsModel mtsm = new MobileTransactionsModel();
    mtsm.kid = kioskId;
    if (transactions == null || transactions.isEmpty()) {
      return mtsm;
    }
    List<MobileTransactionModel> mtmList = new ArrayList<>(transactions.size());
    EntitiesService as;
    UsersService us;
    MaterialCatalogService mcs;
    as = Services.getService(EntitiesServiceImpl.class);
    us = Services.getService(UsersServiceImpl.class);
    mcs = Services.getService(MaterialCatalogServiceImpl.class);
    for (ITransaction transaction : transactions) {

      IMaterial m;
      try {
        m = mcs.getMaterial(transaction.getMaterialId());
      } catch (ServiceException e) {
        xLogger.warn("Exception while getting material for material ID {0}",
            transaction.getMaterialId()); // Material may have been deleted, skip this transaction
        continue;
      }
      MobileTransactionModel mtm = new MobileTransactionModel();
      if (!RESTUtil.materialExistsInKiosk(transaction.getKioskId(), transaction.getMaterialId())) {
        mtm.mnm = m.getName();
      }

      mtm.mid = transaction.getMaterialId();
      mtm.ty = transaction.getType();
      mtm.q = transaction.getQuantity();
      mtm.t = LocalDateUtil.format(transaction.getTimestamp(), locale, timezone);
      if (StringUtils.isNotEmpty(transaction.getReason())) {
        mtm.rsn = transaction.getReason();
      }
      mtm.ostk = transaction.getOpeningStock();
      mtm.cstk = transaction.getClosingStock();
      mtm.uid = transaction.getSourceUserId();
      try {
        IUserAccount u = us.getUserAccount(mtm.uid);
        mtm.u = u.getFullName();
      } catch (Exception e) {
        xLogger.warn("Exception while getting user name for userId: {0}: ",
            mtm.uid, e);
      }
      mtm.lkid = transaction.getLinkedKioskId();
      if (mtm.lkid != null) {
        try {
          IKiosk lk = as.getKiosk(mtm.lkid, false);
          mtm.lknm = lk.getName();
        } catch (Exception e) {
          xLogger.warn("Exception while getting kiosk name for linked kiosk Id: {0}: ",
              mtm.lkid, e);
        }
      }
      if (StringUtils.isNotEmpty(transaction.getBatchId())) {
        mtm.bid = transaction.getBatchId();
        mtm.ostkb = transaction.getOpeningStockByBatch();
        if (transaction.getBatchExpiry() != null) {
          mtm.bexp =
              LocalDateUtil.formatCustom(transaction.getBatchExpiry(), Constants.DATE_FORMAT, null);
        }
        mtm.bmfnm = transaction.getBatchManufacturer();
        if (transaction.getBatchManufacturedDate() != null) {
          mtm.bmfdt =
              LocalDateUtil
                  .formatCustom(transaction.getBatchManufacturedDate(), Constants.DATE_FORMAT,
                      null);
        }
        mtm.cstkb = transaction.getClosingStockByBatch();
      }
      if (StringUtils.isNotEmpty(transaction.getMaterialStatus())) {
        mtm.mst = transaction.getMaterialStatus();
      }
      if (transaction.getAtd() != null) {
        mtm.atd = LocalDateUtil.formatCustom(transaction.getAtd(), Constants.DATE_FORMAT, null);
      }
      if (transaction.getTrackingObjectType() != null) {
        mtm.troty = transaction.getTrackingObjectType();
      }
      if (transaction.getTrackingId() != null) {
        mtm.trid = transaction.getTrackingId();
      }
      List<String> mTags = transaction.getTags(TagUtil.TYPE_MATERIAL);
      mtm.tg = StringUtil.getCSV(mTags);

      if (transaction.getEntryTime() != null) {
        mtm.svtm = transaction.getEntryTime().getTime();
      }
      // Add mtm to mtmList
      mtmList.add(mtm);
    }
    mtsm.trn = mtmList;
    mtsm.kid = kioskId;
    return mtsm;
  }

  /**
   * Builds the mobile update inventory transactions response object
   * @param domainId
   * @param userId
   * @param kioskId
   * @param errorMessage
   * @param midErrorDetailModels
   * @param mobMatTransList
   * @return
   */
  public MobileUpdateInvTransResponse buildMobileUpdateInvTransResponse(Long domainId,
                                                                        String userId, Long kioskId, String partialId,
                                                                        String errorMessage,
                                                                        Map<Long,List<ErrorDetailModel>> midErrorDetailModels,
                                                                        List<MobileMaterialTransModel> mobMatTransList) {
    if (domainId == null) {
      return null;
    }
    MobileUpdateInvTransResponse mobUpdateInvTransResp = null;
    try {
      mobUpdateInvTransResp = new MobileUpdateInvTransResponse();
      mobUpdateInvTransResp.v = VERSION_01;
      if (StringUtils.isNotEmpty(errorMessage)) {
        mobUpdateInvTransResp.st = ERROR;
        mobUpdateInvTransResp.ms = errorMessage;
      } else if (midErrorDetailModels == null
          || midErrorDetailModels.isEmpty()) {
        mobUpdateInvTransResp.st = SUCCESS;
      } else {
        mobUpdateInvTransResp.st = PARTIAL_ERROR;
      }
      mobUpdateInvTransResp.kid = kioskId;
      if (mobUpdateInvTransResp.st == ERROR || mobUpdateInvTransResp.st == PARTIAL_ERROR) {
        mobUpdateInvTransResp.errs = buildErrorModelList(midErrorDetailModels);
      }
      mobUpdateInvTransResp.inv =
          buildMobileInvModelList(domainId, userId, kioskId, mobMatTransList);
      mobUpdateInvTransResp.pid = partialId;
    } catch (Exception e) {
      xLogger
          .warn("Exception while building mobile update inventory transactions response model", e);
    }
    return mobUpdateInvTransResp;
  }

 /**
   * Builds the mobile inventory model
   * @param domainId
   * @param userId
   * @param kioskId
   * @param mobMatTransList
   * @return
   */
 public List<MobileInvModel> buildMobileInvModelList(Long domainId, String userId, Long kioskId,
                                                      List<MobileMaterialTransModel> mobMatTransList) {
   if (mobMatTransList == null || mobMatTransList.isEmpty()) {
     return null;
   }
   Set<Long> mids = new HashSet<>();
   for (MobileMaterialTransModel mobileMaterialTransModel : mobMatTransList) {
     mids.add(mobileMaterialTransModel.mid);
   }
   List<MobileInvModel> mobInvList = new ArrayList<>();
   EntitiesService es = Services.getService(EntitiesServiceImpl.class);
   InventoryManagementService ims = Services.getService(InventoryManagementServiceImpl.class);
   for (Long mid : mids) {
     IInvntry inventory;
     try {
       inventory = ims.getInventory(kioskId, mid);
       if (inventory == null) {
          xLogger.warn("Inventory does not exist for kid {0} and mid {1}", kioskId, mid);
          continue;
       }
     } catch (ServiceException e) {
        xLogger.warn("Exception while getting inventory for kid: {0}, mid: {1}", kioskId, mid, e);
        continue;
     }
     MobileInvModel inv = buildMobileInvModel(inventory, domainId, userId, ims, es);
     if (inv != null) {
       mobInvList.add(inv);
     }
   }
   return mobInvList;
 }

 private MobileInvModel buildMobileInvModel(IInvntry inventory,
                                             Long domainId, String userId,
                                             InventoryManagementService ims, EntitiesService es) {
   MobileInvModel inv = new MobileInvModel();
   try {
      DomainConfig dc = DomainConfig.getInstance(domainId);
      UsersService us = Services.getService(UsersServiceImpl.class);
      MaterialCatalogService mcs = Services.getService(MaterialCatalogServiceImpl.class);
      IUserAccount user = us.getUserAccount(userId);
      Locale locale = user.getLocale();
      String timezone = user.getTimezone();
      inv.mid = inventory.getMaterialId();
      inv.smid = inventory.getShortId();
      inv.q = inventory.getStock();
      if (dc.autoGI()) {
        inv.alq = inventory.getAllocatedStock();
        inv.itq = inventory.getInTransitStock();
        inv.avq = inventory.getAvailableStock();
      }
      BigDecimal stockAvailPeriod = ims.getStockAvailabilityPeriod(inventory, dc);
      if (stockAvailPeriod != null && BigUtil.notEqualsZero(stockAvailPeriod)) {
        inv.dsq = stockAvailPeriod;
      }
      if (inventory.getMinDuration() != null && BigUtil
          .notEqualsZero(inventory.getMinDuration())) {
        inv.dmin = inventory.getMinDuration();
      }
      if (inventory.getMaxDuration() != null && BigUtil
          .notEqualsZero(inventory.getMaxDuration())) {
        inv.dmax = inventory.getMaxDuration();
      }
      MobileConsRateModel crModel = buildMobileConsRateModel(domainId, inventory);
      if (crModel != null) {
        inv.cr = crModel;
      }
      inv.t = LocalDateUtil.format(inventory.getTimestamp(), locale,
          timezone);
      Long kid = inventory.getKioskId();
      Long mid = inventory.getMaterialId();
      IKiosk k = es.getKiosk(kid, false);
      IMaterial m = mcs.getMaterial(mid);
      if (k.isBatchMgmtEnabled() && m.isBatchEnabled()) {
        inv.bt = buildMobileInvBatchModelList(kid, mid, locale, timezone,
            dc.autoGI(), true);
        inv.xbt = buildMobileInvBatchModelList(kid, mid, locale, timezone, dc.autoGI(),
            false);
      }
      return inv;
   } catch (Exception e) {
     xLogger.warn(
          "Exception while building inventory model in update inventory transactions response", e);
      return null;
   }
 }

  private List<MobileInvBatchModel> buildMobileInvBatchModelList(Long kid, Long mid, Locale locale,
                                                                 String timezone,
                                                                 boolean isAutoPostingIssuesEnabled,
                                                                 boolean buildValidBatchModel) {
    List<MobileInvBatchModel> mobileInvBatchModelList = null;
    try {
      InventoryManagementService ims = Services.getService(InventoryManagementServiceImpl.class);
      // NOTE: Get only up to the 50 last batches
      Results
          results =
          ims.getBatches(mid, kid, new PageParams(null,
              PageParams.DEFAULT_SIZE));
      if (results != null) {
        List<InvntryBatch> batches = results.getResults();
        if (batches != null && !batches.isEmpty()) {
          mobileInvBatchModelList = new ArrayList<>(batches.size());
          for (InvntryBatch invBatch : batches) {
            MobileInvBatchModel
                mibm =
                buildMobileInvBatchModel(invBatch, isAutoPostingIssuesEnabled, locale, timezone,
                    buildValidBatchModel);
            if (mibm != null) {
              mobileInvBatchModelList.add(mibm);
            }
          }
        }
      }
    } catch (Exception e) {
      xLogger
          .warn("Exception when trying to get batch information for kid {0} mid {1}", kid, mid, e);
      return null;
    }
    return mobileInvBatchModelList;
  }

  private MobileInvBatchModel buildMobileInvBatchModel(IInvntryBatch invBatch,
                                                       boolean isAutoPostingIssuesEnabled,
                                                       Locale locale, String timezone,
                                                       boolean validBatchesOnly) {
    if (validBatchesOnly && invBatch.isExpired()) {
      return null;
    }
    if (!validBatchesOnly && !invBatch.isExpired()) {
      return null;
    }
    // For any batch whether valid or expired, if q is not > 0, return null
    if (!BigUtil.greaterThanZero(invBatch.getQuantity())) {
      return null;
    }
    MobileInvBatchModel mobileInvBatchModel = new MobileInvBatchModel();
    mobileInvBatchModel.bid = invBatch.getBatchId();
    if (invBatch.getBatchManufacturedDate() != null) {
      mobileInvBatchModel.bmfdt =
          LocalDateUtil
              .formatCustom(invBatch.getBatchManufacturedDate(), Constants.DATE_FORMAT, null);
    }
    mobileInvBatchModel.bmfnm = invBatch.getBatchManufacturer();
    if (invBatch.getBatchExpiry() != null) {
      mobileInvBatchModel.bexp =
          LocalDateUtil.formatCustom(invBatch.getBatchExpiry(), Constants.DATE_FORMAT, null);
    } else {
      xLogger.warn("Null Batch expiry date when building mobile inventory batch model for kid: {0}, mid: {1}, bid: {2}, bexp: {3}", invBatch.getKioskId(), invBatch.getMaterialId(), invBatch.getBatchId(),
          invBatch.getBatchExpiry());
    }

    mobileInvBatchModel.q = invBatch.getQuantity();
    if (isAutoPostingIssuesEnabled && validBatchesOnly) {
      mobileInvBatchModel.alq = invBatch.getAllocatedStock();
      mobileInvBatchModel.avq = invBatch.getAvailableStock();
    }
    mobileInvBatchModel.t = LocalDateUtil.format(invBatch.getTimestamp(), locale,
        timezone);

    return mobileInvBatchModel;
  }

  private MobileConsRateModel buildMobileConsRateModel(Long domainId, IInvntry inv) {
    DomainConfig dc = DomainConfig.getInstance(domainId);
    InventoryConfig ic = dc.getInventoryConfig();
    String displayFreq = ic.getDisplayCRFreq();
    try {
      InventoryManagementService ims = Services.getService(InventoryManagementServiceImpl.class);
      BigDecimal cr = ims.getDailyConsumptionRate(inv);
      if (BigUtil.greaterThanZero(cr)) {
        MobileConsRateModel mobConRateModel = new MobileConsRateModel();
        mobConRateModel.val = BigUtil.getFormattedValue(cr);
        switch (displayFreq) {
          case Constants.FREQ_DAILY:
            mobConRateModel.ty = Constants.FREQ_TYPE_DAILY;
            break;
          case Constants.FREQ_WEEKLY:
            mobConRateModel.ty = Constants.FREQ_TYPE_WEEKLY;
            break;
          case Constants.FREQ_MONTHLY:
            mobConRateModel.ty = Constants.FREQ_TYPE_MONTHLY;
            break;
          default:
            xLogger.warn("Invalid displayFrequency: {0} while building mobile consumption rate model for inventory with kid: {1} and mid: {1}", displayFreq, inv.getKioskId(), inv.getMaterialId());
            break;
        }
        return mobConRateModel;
      }
    } catch (Exception e) {
      return null;
    }
    return null;
  }

  /**
   * Builds a map of material to list of server transaction objects from list of mobile transaction objects
   *
   * @param userId                    User ID of the user sending the mobile transactions
   * @param kid                       Kiosk ID of the kiosk for which the transactions are sent
   * @param mobileMaterialTransModels List of mobile transactions grouped by material
   * @return - Map of material id to list of transactions as required by the server
   */
  public Map<Long, List<ITransaction>> buildMaterialTransactionsMap(String userId, Long kid,
                                                                    List<MobileMaterialTransModel> mobileMaterialTransModels) {
    if (mobileMaterialTransModels == null || mobileMaterialTransModels.isEmpty()) {
      return null;
    }
    Map<Long, List<ITransaction>>
        midTransModelMap =
        new HashMap<>(mobileMaterialTransModels.size());
    for (MobileMaterialTransModel mobileMaterialTransModel : mobileMaterialTransModels) {
      Long mid = mobileMaterialTransModel.mid;
      List<ITransaction> transactionList = buildTransactions(userId, kid, mobileMaterialTransModel);
      midTransModelMap.put(mid, transactionList);
    }
    return midTransModelMap;
  }

  /**
   * Sets the partial id in the mobile update inventory transaction response json string (if not set already)
   */
  public String buildUpdateInvTransResponseWithPartialID(String mobUpdateInvTransRespJsonStr, String pid) {
    MobileUpdateInvTransResponse mobileUpdateInvTransResponse =  new Gson().fromJson(mobUpdateInvTransRespJsonStr,
        MobileUpdateInvTransResponse.class);
    if (StringUtils.isEmpty(mobileUpdateInvTransResponse.pid)) {
      mobileUpdateInvTransResponse.pid = pid;
      return new Gson().toJson(mobileUpdateInvTransResponse);
    } else {
      return mobUpdateInvTransRespJsonStr;
    }
  }

  private List<ITransaction> buildTransactions(String userId, Long kid, MobileMaterialTransModel mobileMaterialTransModel) {
    if (mobileMaterialTransModel == null || mobileMaterialTransModel.trns == null) {
      return null;
    }
    List<ITransaction> transactions = new ArrayList<>(mobileMaterialTransModel.trns.size());
    for (MobileTransModel mobileTransModel : mobileMaterialTransModel.trns) {
      ITransaction transaction = buildTransaction(userId, kid, mobileTransModel);
      if (transaction != null) {
        transaction.setMaterialId(mobileMaterialTransModel.mid);
        transactions.add(transaction);
      }
    }
    return transactions;
  }

  /**
   *
   * Builds an ITransaction object from a MobileTransModel object
   * @param userId
   * @param kid
   * @param mobTrans
   * @return
   */
  private ITransaction buildTransaction(String userId, Long kid, MobileTransModel mobTrans) {
    ITransaction trans = JDOUtils.createInstance(ITransaction.class);
    trans.setSourceUserId(userId);
    trans.setKioskId(kid);
    if (mobTrans.entm != null) {
      trans.setEntryTime(new Date(mobTrans.entm));
    }
    trans.setType(mobTrans.ty);
    trans.setQuantity(mobTrans.q);
    if (StringUtils.isNotEmpty(mobTrans.bid)) {
      trans.setOpeningStockByBatch(mobTrans.ostk);
    } else {
      trans.setOpeningStock(mobTrans.ostk);
    }
    TransactionUtil
        .setBatchData(trans, mobTrans.bid, mobTrans.bexp, mobTrans.bmfnm, mobTrans.bmfdt);
    trans.setReason(mobTrans.rsn);
    trans.setMaterialStatus(mobTrans.mst);
    String atd = mobTrans.atd;
    if (StringUtils.isNotEmpty(atd)) {
      try {
        Date actTransDt = LocalDateUtil.parseCustom(atd, Constants.DATE_FORMAT, null);
        trans.setAtd(actTransDt);
      } catch (ParseException e) {
        xLogger.severe("Error while parsing actual transaction date string for kid {0}", kid, e);
      }
    }
    trans.setLinkedKioskId(mobTrans.lkid);
    MobileGeoModel mobileGeo = mobTrans.geo;
    if (mobileGeo != null) {
      if (mobileGeo.lat != null) {
        trans.setLatitude(mobileGeo.lat);
      }
      if (mobileGeo.lng != null) {
        trans.setLongitude(mobileGeo.lng);
      }
      if (mobileGeo.galt != null) {
      trans.setAltitude(mobileGeo.galt);
      }
      if (mobileGeo.gacc != null) {
      trans.setGeoAccuracy(mobileGeo.gacc);
      }
      trans.setGeoErrorCode(mobileGeo.gerr);
    }
    return trans;
  }

  private List<MobileTransErrModel> buildErrorModelList(Map<Long,List<ErrorDetailModel>> midErrorDetailModelsMap) {
    if (midErrorDetailModelsMap == null || midErrorDetailModelsMap.isEmpty()) {
      return null;
    }
    List<MobileTransErrModel> mobileTransErrModels = new ArrayList<>(midErrorDetailModelsMap.size());
    Set<Long> mids = midErrorDetailModelsMap.keySet();
    for (Long mid : mids) {
      MobileTransErrModel mobileTransErrModel = buildMobileTransErrorModel(mid, midErrorDetailModelsMap.get(mid));
      mobileTransErrModels.add(mobileTransErrModel);
    }
    return mobileTransErrModels;
  }

  private MobileTransErrModel buildMobileTransErrorModel(Long mid,
                                                       List<ErrorDetailModel> errorDetailModels) {
    if (mid == null || errorDetailModels == null || errorDetailModels.isEmpty()) {
      return null;
    }
    MobileTransErrModel mobileTransErrModel = new MobileTransErrModel();
    mobileTransErrModel.mid = mid;
    mobileTransErrModel.errdtl = buildMobileTransErrorDetailModel(errorDetailModels);
    return mobileTransErrModel;
  }

  private List<MobileTransErrorDetailModel> buildMobileTransErrorDetailModel(List<ErrorDetailModel> errorDetailModels) {
    if(errorDetailModels == null || errorDetailModels.isEmpty()) {
      return null;
    }
    List<MobileTransErrorDetailModel> mobileTransErrorDetailModels = new ArrayList<>(errorDetailModels.size());
    for (ErrorDetailModel errorDetailModel : errorDetailModels) {
      MobileTransErrorDetailModel mobileTransErrorDetailModel = new MobileTransErrorDetailModel();
      mobileTransErrorDetailModel.ec = errorDetailModel.errorCode;
      mobileTransErrorDetailModel.idx = errorDetailModel.index;
      mobileTransErrorDetailModels.add(mobileTransErrorDetailModel);
    }
    return mobileTransErrorDetailModels;
  }

}

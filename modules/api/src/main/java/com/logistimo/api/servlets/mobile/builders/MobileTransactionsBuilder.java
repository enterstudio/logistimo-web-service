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

import com.logistimo.api.util.RESTUtil;
import com.logistimo.constants.Constants;
import com.logistimo.entities.entity.IKiosk;
import com.logistimo.entities.service.EntitiesService;
import com.logistimo.entities.service.EntitiesServiceImpl;
import com.logistimo.inventory.entity.ITransaction;
import com.logistimo.logger.XLog;
import com.logistimo.materials.entity.IMaterial;
import com.logistimo.materials.service.MaterialCatalogService;
import com.logistimo.materials.service.impl.MaterialCatalogServiceImpl;
import com.logistimo.proto.MobileTransactionModel;
import com.logistimo.proto.MobileTransactionsModel;
import com.logistimo.services.ServiceException;
import com.logistimo.services.Services;
import com.logistimo.users.entity.IUserAccount;
import com.logistimo.users.service.UsersService;
import com.logistimo.users.service.impl.UsersServiceImpl;
import com.logistimo.utils.LocalDateUtil;

import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Created by vani on 18/01/17.
 */
public class MobileTransactionsBuilder {
  private static final XLog xLogger = XLog.getLog(MobileTransactionsBuilder.class);

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
    try {
      as = Services.getService(EntitiesServiceImpl.class);
      us = Services.getService(UsersServiceImpl.class);
      mcs = Services.getService(MaterialCatalogServiceImpl.class);
    } catch (ServiceException e) {
      xLogger.warn("Exception while getting services", e);
      return mtsm;
    }
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
      // Add mtm to mtmList
      mtmList.add(mtm);
    }
    mtsm.trn = mtmList;
    mtsm.kid = kioskId;
    return mtsm;
  }
}
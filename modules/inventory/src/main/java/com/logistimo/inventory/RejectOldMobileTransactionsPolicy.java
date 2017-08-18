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

package com.logistimo.inventory;

import com.logistimo.constants.Constants;
import com.logistimo.constants.SourceConstants;
import com.logistimo.exception.LogiException;
import com.logistimo.inventory.entity.ITransaction;
import com.logistimo.logger.XLog;
import com.logistimo.services.Resources;
import com.logistimo.services.Services;
import com.logistimo.users.entity.IUserAccount;
import com.logistimo.users.service.UsersService;
import com.logistimo.users.service.impl.UsersServiceImpl;

import java.math.BigDecimal;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.ListIterator;
import java.util.ResourceBundle;

/**
 * Created by vani on 19/04/17.
 */
public class RejectOldMobileTransactionsPolicy implements MobileTransactionsHandler {
  private static final XLog xLogger = XLog.getLog(RejectOldMobileTransactionsPolicy.class);

  public int applyPolicy(List<ITransaction> transactions, ITransaction lastWebTrans){
    if (transactions == null || transactions.isEmpty() || lastWebTrans == null) {
      return -1;
    }
    int index = -1;
    ListIterator<ITransaction> transactionListIterator = transactions.listIterator();
    while(transactionListIterator.hasNext()){
      if (lastWebTrans.getTimestamp().getTime() > transactionListIterator.next()
          .getEntryTime().getTime()) {
        transactionListIterator.remove();
        index++;
      }
    }
    return index;
  }

  public void addStockCountIfNeeded(ITransaction lastWebTrans, List<ITransaction> transactions) throws LogiException {
    if (transactions == null || transactions.isEmpty()) {
      return;
    }
    BigDecimal currentStock;
    if (lastWebTrans == null) {
      currentStock = BigDecimal.ZERO;
    } else {
      currentStock = lastWebTrans.hasBatch() ? lastWebTrans.getClosingStockByBatch(): lastWebTrans.getClosingStock();
    }
    ITransaction firstValidMobTrans = transactions.get(0);
    if (currentStock.compareTo(firstValidMobTrans.hasBatch() ? firstValidMobTrans.getOpeningStockByBatch() : firstValidMobTrans.getOpeningStock()) != 0) {
      ITransaction scTrans = buildStockCountTrans(firstValidMobTrans);
      if (scTrans == null) {
        throw new LogiException("M012", (Object[])null);
      }
      transactions.add(0,scTrans);
    }
  }

  protected ITransaction buildStockCountTrans(ITransaction trans) {
    try {
      UsersService us = Services.getService(UsersServiceImpl.class);
      IUserAccount ua = us.getUserAccount(trans.getSourceUserId());
      ResourceBundle
          backendMessages =
          Resources.get().getBundle("BackendMessages", ua.getLocale());
      ITransaction scTrans = trans.clone();
      // Set the entry time of this stock count transaction to 1 ms less than the entry time of trans
      Date et = trans.getEntryTime();
      Calendar etCal = Calendar.getInstance();
      etCal.setTimeInMillis(et.getTime() - 1);
      scTrans.setEntryTime(etCal.getTime());
      scTrans.setType(ITransaction.TYPE_PHYSICALCOUNT);
      scTrans.setReason(backendMessages.getString("openingstock.mismatch"));
      scTrans.setSystemCreated(true);
      scTrans.setSourceUserId(Constants.SYSTEM_USER_ID);
      scTrans.setSrc(SourceConstants.WEB);
      if (trans.hasBatch()) {
        scTrans.setQuantity(trans.getOpeningStockByBatch());
      } else {
        scTrans.setQuantity(trans.getOpeningStock());
      }
      if (trans.hasBatch()) {
        scTrans.setOpeningStockByBatch(trans.getOpeningStockByBatch());
      } else {
        scTrans.setOpeningStock(trans.getOpeningStock());
      }
      return scTrans;
    } catch (Exception e) {
      xLogger.severe("Exception when creating a stock count transaction", e);
      return null;
    }
  }
}

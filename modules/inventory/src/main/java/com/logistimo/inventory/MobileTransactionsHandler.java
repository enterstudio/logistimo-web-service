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

import com.logistimo.exception.LogiException;
import com.logistimo.inventory.entity.ITransaction;

import java.util.List;


/**
 * Created by vani on 19/04/17.
 */
public interface MobileTransactionsHandler {
  /**
   * Given list of ITransaction objects and the last web transaction, applies a policy to sequence them.
   * @param transactions - List of transactions
   * @param lastWebTrans - Last web transaction for a kid, mid and/or bid
   * @return index until which the transactions should be rejected (inclusive), -1 if no transactions are rejected.
   */
  int applyPolicy(List<ITransaction> transactions, ITransaction lastWebTrans);

  /**
   * Adds a stock count transaction to the list of transactions if there is a mismatch in the opening stock of the first transaction and the last web transaction's closing stock
   * @param lastWebTrans - Last web transaction for a kid, mid and/bid
   * @param transactions - List of transactions
   * @throws LogiException if there is an error while creating the stock count transaction
   */
  void addStockCountIfNeeded(ITransaction lastWebTrans, List<ITransaction> transactions) throws LogiException;
}

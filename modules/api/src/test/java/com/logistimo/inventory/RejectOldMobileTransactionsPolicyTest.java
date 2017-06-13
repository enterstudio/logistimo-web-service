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

import com.logistimo.LgTestCase;
import com.logistimo.inventory.entity.ITransaction;
import com.logistimo.inventory.entity.Transaction;

import org.junit.Test;

import java.math.BigDecimal;
import java.util.Date;

import static org.mockito.Mockito.spy;

/**
 * Created by vani on 16/05/17.
 */
public class RejectOldMobileTransactionsPolicyTest extends LgTestCase {
  @Test
  public void testBuildStockCountTrans() {
    RejectOldMobileTransactionsPolicy policy = spy(RejectOldMobileTransactionsPolicy.class);
    ITransaction trans = new Transaction();
    trans.setKioskId(1l);
    trans.setOpeningStock(new BigDecimal(700));
    trans.setMaterialId(2l);
    trans.setType(ITransaction.TYPE_RECEIPT);
    trans.setQuantity(new BigDecimal(100));
    trans.setEntryTime(new Date());
    ITransaction scTrans = policy.buildStockCountTrans(trans);
    assert(trans.getEntryTime().getTime() - scTrans.getEntryTime().getTime() == 1);
  }
}
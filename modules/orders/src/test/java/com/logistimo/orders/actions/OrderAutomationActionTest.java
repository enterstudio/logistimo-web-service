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

package com.logistimo.orders.actions;

import com.logistimo.inventory.entity.ITransaction;
import com.logistimo.inventory.entity.Transaction;
import com.logistimo.orders.entity.DemandItem;
import com.logistimo.orders.entity.IDemandItem;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * Created by charan on 06/08/17.
 */
public class OrderAutomationActionTest {

  private OrderAutomationAction orderAutomationAction;

  @Before
  public void setup() {
    orderAutomationAction =
        new OrderAutomationAction(null, null, null, null, null);
  }


  @Test
  public void testFilter() {
    List<ITransaction> kioskTransactions = new ArrayList<>();
    kioskTransactions.add(getTransaction(1l, 2l));
    kioskTransactions.add(getTransaction(2l, 2l));
    List<IDemandItem> demandItems = new ArrayList<>();
    demandItems.add(getDemandItem(1l, 2l));

    List<ITransaction>
        filteredTransactions =
        orderAutomationAction.filter(kioskTransactions, demandItems);
    assertEquals(filteredTransactions.size(), 1);
  }

  private IDemandItem getDemandItem(long materialId, long kioskId) {
    IDemandItem item = new DemandItem();
    item.setMaterialId(materialId);
    item.setKioskId(kioskId);
    return item;
  }

  private ITransaction getTransaction(long materialId, long kioskId) {
    ITransaction transaction = new Transaction();
    transaction.setMaterialId(materialId);
    transaction.setKioskId(kioskId);
    return transaction;
  }

}
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

import com.logistimo.entities.entity.IKioskLink;
import com.logistimo.entities.entity.KioskLink;
import com.logistimo.entities.service.EntitiesServiceImpl;
import com.logistimo.inventory.entity.IInvntry;
import com.logistimo.inventory.entity.ITransaction;
import com.logistimo.inventory.entity.Invntry;
import com.logistimo.inventory.entity.Transaction;
import com.logistimo.inventory.service.impl.InventoryManagementServiceImpl;
import com.logistimo.orders.entity.DemandItem;
import com.logistimo.orders.entity.IDemandItem;
import com.logistimo.orders.service.impl.OrderManagementServiceImpl;
import com.logistimo.pagination.Results;
import com.logistimo.services.ServiceException;
import com.logistimo.utils.BigUtil;

import org.junit.Before;
import org.junit.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

/**
 * Created by charan on 06/08/17.
 */
public class OrderAutomationActionTest {

  private OrderAutomationAction orderAutomationAction;

  @Before
  public void setup() throws ServiceException {
    OrderManagementServiceImpl orderManagementService = spy(OrderManagementServiceImpl.class);
    EntitiesServiceImpl ems = mock(EntitiesServiceImpl.class);
    when(ems.getKioskLinks(1l, IKioskLink.TYPE_VENDOR, null, null, null))
        .thenReturn(new Results<>(null, null));
    when(ems.getKioskLinks(2l, IKioskLink.TYPE_VENDOR, null, null, null))
        .thenReturn(getKioskLink(2l));
    InventoryManagementServiceImpl ims = mock(InventoryManagementServiceImpl.class);
    when(ims.getInventory(2l, 2l)).thenReturn(getInvntry(1l, 2l, null, null));
    orderAutomationAction =
        new OrderAutomationAction(ims, orderManagementService, ems, null, null);
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

  @Test
  public void testGetTransactionWithROQGreaterThanZero() {
    IInvntry invntry = getInvntry(1l, 2l, BigDecimal.TEN, new BigDecimal(100));
    assertNotEquals(orderAutomationAction.getTransaction(invntry), Optional.empty());
  }

  @Test
  public void testRoundingROQ() {
    IInvntry invntry = getInvntry(1l, 2l, BigDecimal.TEN, new BigDecimal(100));
    invntry.setInventoryModel(IInvntry.MODEL_SQ);
    invntry.setEconomicOrderQuantity(new BigDecimal(2.45));
    Optional<ITransaction> transaction = orderAutomationAction.getTransaction(invntry);
    assertNotEquals(transaction, Optional.empty());
    assertTrue("Rounding failed expected 3 but was " + transaction.get().getQuantity(),
        BigUtil.equals(transaction.get().getQuantity(), new BigDecimal(3)));
  }

  @Test
  public void testGetTransactionWithROQLessOrEqualThanZero() {
    IInvntry invntry = getInvntry(1l, 2l, BigDecimal.ZERO, BigDecimal.ZERO);
    assertEquals(orderAutomationAction.getTransaction(invntry), Optional.empty());
  }

  @Test
  public void testChooseVendorWithNoVendor() throws ServiceException {
    assertEquals(orderAutomationAction.chooseVendor(1l, null), Optional.empty());
  }

  @Test
  public void testChooseVendorWithVendorExists() throws ServiceException {
    assertNotEquals(orderAutomationAction.chooseVendor(2l, 2l), Optional.empty());
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

  private IInvntry getInvntry(Long kioskId, Long materialId, BigDecimal stock,
                              BigDecimal maxStock) {
    Invntry invntry = new Invntry();
    invntry.setMaterialId(materialId);
    invntry.setKioskId(kioskId);
    invntry.setStock(stock);
    invntry.setMaxStock(maxStock);
    return invntry;
  }

  private Results getKioskLink(Long kioskId) {
    IKioskLink kioskLink = new KioskLink();
    kioskLink.setLinkedKioskId(kioskId);
    List<IKioskLink> results = new ArrayList<>();
    results.add(kioskLink);

    return new Results<>(results, null);
  }

}
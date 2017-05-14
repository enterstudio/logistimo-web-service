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

package com.logistimo.service.impl;

import com.logistimo.inventory.entity.IInvntry;
import com.logistimo.inventory.entity.Invntry;
import com.logistimo.inventory.exceptions.InventoryAllocationException;
import com.logistimo.inventory.service.impl.InventoryManagementServiceImpl;
import com.logistimo.utils.BigUtil;
import org.testng.annotations.Test;

import java.math.BigDecimal;

import javax.jdo.PersistenceManager;

import static org.mockito.Mockito.mock;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;

/**
 * Created by charan on 04/11/16.
 */
public class InvManagementUnitTest {

  @Test
  public void testIncrementInventoryAvailableQuantity() throws Exception {
    InventoryManagementServiceImpl ims = new InventoryManagementServiceImpl();
    PersistenceManager pm = mock(PersistenceManager.class);
    IInvntry inv = new Invntry();
    inv.setStock(BigDecimal.TEN);
    inv.setAllocatedStock(BigDecimal.ZERO);
    inv.setAvailableStock(BigDecimal.TEN);
    inv.setKioskName("Test kiosk");
    inv.setMaterialName("Test material");
    ims.incrementInventoryAvailableQuantity(1l, 1l, null, BigDecimal.ZERO.subtract(BigDecimal.TEN),
        pm, inv, null, false);
    assertTrue(BigUtil.equals(inv.getAllocatedStock(), BigDecimal.TEN), "Allocated stock mismatch"
        + inv.getAllocatedStock() + ": e:" + BigDecimal.ZERO);
    assertTrue(BigUtil.equals(inv.getAvailableStock(), BigDecimal.ZERO),
        "Available stock mismatch a:"
            + inv.getAvailableStock() + ": e:" + BigDecimal.ZERO);
    assertTrue(BigUtil.equals(inv.getStock(), BigDecimal.TEN), "Stock mismatch a:"
        + inv.getStock() + ": e:" + BigDecimal.TEN);
  }


  @Test
  public void testAllocationNotEnoughAvailable() throws Exception {
    InventoryManagementServiceImpl ims = new InventoryManagementServiceImpl();
    PersistenceManager pm = mock(PersistenceManager.class);
    IInvntry inv = new Invntry();
    inv.setStock(BigDecimal.TEN);
    inv.setAllocatedStock(BigDecimal.TEN);
    inv.setAvailableStock(BigDecimal.ZERO);
    inv.setKioskName("Test kiosk");
    inv.setMaterialName("Test material");
    try {
      ims.incrementInventoryAvailableQuantity(1l, 1l, null,
          BigDecimal.ZERO.subtract(BigDecimal.TEN),
          pm, inv, null, false);
      fail("Expected exception that inventory is not available to allocate");
    } catch (InventoryAllocationException e) {
      assertTrue(e.getMessage().startsWith("Unable to allocate stock for material"),
          "Message does not match");
    }


  }
}

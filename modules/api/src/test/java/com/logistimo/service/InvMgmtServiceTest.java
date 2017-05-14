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

package com.logistimo.service;

import com.logistimo.dao.JDOUtils;
import com.logistimo.inventory.entity.IInvAllocation;
import com.logistimo.inventory.entity.IInvntryBatch;
import com.logistimo.inventory.entity.ITransaction;
import com.logistimo.inventory.service.InventoryManagementService;
import com.logistimo.inventory.service.impl.InventoryManagementServiceImpl;
import com.logistimo.materials.entity.IMaterial;
import com.logistimo.materials.service.impl.MaterialCatalogServiceImpl;
import com.logistimo.models.shipments.ShipmentItemBatchModel;
import com.logistimo.services.DuplicationException;
import com.logistimo.services.ObjectNotFoundException;
import com.logistimo.services.ServiceException;
import com.logistimo.services.Services;

import com.logistimo.LgTestCase;
import com.logistimo.inventory.entity.IInvntry;
import com.logistimo.pagination.PageParams;
import com.logistimo.pagination.Results;
import com.logistimo.utils.BigUtil;
import com.logistimo.utils.LocalDateUtil;
import com.logistimo.constants.SourceConstants;
import org.testng.annotations.Test;

import java.math.BigDecimal;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

/**
 * Created by charan on 27/10/16.
 */
public class InvMgmtServiceTest extends LgTestCase {

  @Test(sequential = true)
  public void testStockCount()
      throws ServiceException, ObjectNotFoundException, DuplicationException {
    Long domainId = getDomainId("Default").getId();
    Long kioskId = getKiosk(domainId, "entitytest").getKioskId();
    Long materialId = getMaterialId(domainId, "BCG (dose)").getMaterialId();
    Long vendorId = getKiosk(domainId, "storetest").getKioskId();
    String userId = getUser("dilbert").getUserId();
    InventoryManagementService
        ims =
        Services.getService(InventoryManagementServiceImpl.class);
    ITransaction transaction = JDOUtils.createInstance(ITransaction.class);
    transaction.setDomainId(domainId);
    transaction.setKioskId(kioskId);
    transaction.setMaterialId(materialId);
    transaction.setType(ITransaction.TYPE_PHYSICALCOUNT);
    BigDecimal quantity = BigDecimal.valueOf(500);
    transaction.setQuantity(quantity);
    transaction.setSourceUserId(userId);
    transaction.setSrc(SourceConstants.WEB);
    IMaterial
        material =
        Services.getService(MaterialCatalogServiceImpl.class).getMaterial(materialId);
    String batchId = "UNITTB_123";
    if (material.isBatchEnabled()) {
      transaction.setBatchId(batchId);
      Calendar cal = Calendar.getInstance();
      cal.set(cal.get(Calendar.YEAR) + 2, Calendar.DECEMBER, 31);
      transaction.setBatchExpiry(cal.getTime());
      cal.add(Calendar.YEAR, -3);
      cal = LocalDateUtil.resetTimeFields(cal);
      transaction.setBatchManufacturedDate(cal.getTime());
      transaction.setBatchManufacturer("UNITB_MFR");
      transaction.setReason("UNIT_TESTS_SC");
      cal.add(Calendar.YEAR, 10);
      cal = LocalDateUtil.resetTimeFields(cal);
      transaction.setBatchExpiry(cal.getTime());
    }
    Date now = new Date();
    ims.updateInventoryTransactions(domainId, Collections.singletonList(transaction), true);
    Results results = ims.getInventoryTransactions(now, null, domainId, kioskId, materialId,
        Collections.singletonList(ITransaction.TYPE_PHYSICALCOUNT), null, null, null, null,
        new PageParams(10),
        batchId, false, "UNIT_TESTS_SC", null);
    assertNotNull(results);
    assertEquals(results.getResults().size(), 1, "There should be one Stockout transaction");
    transaction = (ITransaction) results.getResults().get(0);
    assertEquals(transaction.getBatchId(), batchId, "Batch Id missing in transaction");
    assertTrue(BigUtil.equals(transaction.getQuantity(), quantity),
        "Batch quantity is not matching");
    IInvntry inv = ims.getInventory(kioskId, materialId);
    assertTrue(BigUtil.equals(inv.getStock(), quantity), "Stock quantity does not match");
    assertTrue(BigUtil.equals(inv.getAllocatedStock(), 0),
        "Allocations should be zero a: " + inv.getAllocatedStock() + " e:" + 0);
    assertTrue(BigUtil.equals(inv.getAvailableStock(), quantity),
        "Available should match a: " + inv.getAvailableStock() + " e:" + 0);
    IInvntryBatch iInvntryBatch = ims.getInventoryBatch(kioskId, materialId, batchId, null);
    assertTrue(BigUtil.equals(iInvntryBatch.getQuantity(), quantity),
        "Inventory batch quantity does not match");
  }

  @Test(sequential = true)
  public void testStockCountNonBatch()
      throws ServiceException, ObjectNotFoundException, DuplicationException {
    Long domainId = getDomainId("Default").getId();
    Long kioskId = getKiosk(domainId, "entitytest").getKioskId();
    Long materialId = getMaterialId(domainId, "5 ml syringe").getMaterialId();
    Long vendorId = getKiosk(domainId, "storetest").getKioskId();
    String userId = getUser("dilbert").getUserId();
    InventoryManagementService
        ims =
        Services.getService(InventoryManagementServiceImpl.class);
    ITransaction transaction = JDOUtils.createInstance(ITransaction.class);
    transaction.setDomainId(domainId);
    transaction.setKioskId(kioskId);
    transaction.setMaterialId(materialId);
    transaction.setType(ITransaction.TYPE_PHYSICALCOUNT);
    BigDecimal quantity = BigDecimal.valueOf(500);
    transaction.setQuantity(quantity);
    transaction.setSourceUserId(userId);
    transaction.setSrc(SourceConstants.WEB);
    IMaterial
        material =
        Services.getService(MaterialCatalogServiceImpl.class).getMaterial(materialId);
    String batchId = null;
    if (material.isBatchEnabled()) {
      batchId = "UNITTB_123";
      transaction.setBatchId(batchId);
      Calendar cal = Calendar.getInstance();
      cal.set(cal.get(Calendar.YEAR) + 2, Calendar.DECEMBER, 31);
      transaction.setBatchExpiry(cal.getTime());
      cal.add(Calendar.YEAR, -3);
      transaction.setBatchManufacturedDate(cal.getTime());
      transaction.setBatchManufacturer("UNITB_MFR");
    }
    transaction.setReason("UNIT_TESTS_SC");
    Date now = new Date();
    ims.updateInventoryTransactions(domainId, Collections.singletonList(transaction), true);
    Results results = ims.getInventoryTransactions(now, null, domainId, kioskId, materialId,
        Collections.singletonList(ITransaction.TYPE_PHYSICALCOUNT), null, null, null, null,
        new PageParams(10),
        batchId, false, "UNIT_TESTS_SC", null);
    assertNotNull(results);
    assertEquals(results.getResults().size(), 1, "There should be one Stockout transaction");
    transaction = (ITransaction) results.getResults().get(0);
    if (material.isBatchEnabled()) {
      assertEquals(transaction.getBatchId(), batchId, "Batch Id missing in transaction");
    }
    assertTrue(BigUtil.equals(transaction.getQuantity(), quantity),
        "Batch quantity is not matching");
    IInvntry inv = ims.getInventory(kioskId, materialId);
    assertTrue(BigUtil.equals(inv.getStock(), quantity), "Stock quantity does not match");
    assertTrue(BigUtil.equals(inv.getAllocatedStock(), BigDecimal.ZERO),
        "Allocations should be zero");
    assertTrue(BigUtil.equals(inv.getAvailableStock(), quantity), "Available should match");
    if (material.isBatchEnabled()) {
      IInvntryBatch iInvntryBatch = ims.getInventoryBatch(kioskId, materialId, batchId, null);
      assertTrue(BigUtil.equals(iInvntryBatch.getQuantity(), quantity),
          "Inventory batch quantity does not match");
    }
  }

  @Test(sequential = true)
  public void testIssue() throws ServiceException, ObjectNotFoundException, DuplicationException {
    Date now = new Date();
    Long domainId = getDomainId("Default").getId();
    Long kioskId = getKiosk(domainId, "entitytest").getKioskId();
    Long materialId = getMaterialId(domainId, "BCG (dose)").getMaterialId();
    Long vendorId = getKiosk(domainId, "storetest").getKioskId();
    String userId = getUser("dilbert").getUserId();
    String batchId = "UNITTB_123";
    BigDecimal quantity = BigDecimal.valueOf(100);
    resetStock(kioskId, materialId, domainId, quantity, userId);
    InventoryManagementService
        ims =
        Services.getService(InventoryManagementServiceImpl.class);
    ITransaction transaction = JDOUtils.createInstance(ITransaction.class);
    transaction.setDomainId(domainId);
    transaction.setKioskId(kioskId);
    transaction.setMaterialId(materialId);
    transaction.setType(ITransaction.TYPE_ISSUE);
    transaction.setQuantity(quantity);
    transaction.setSourceUserId(userId);
    transaction.setSrc(SourceConstants.WEB);
    IMaterial
        material =
        Services.getService(MaterialCatalogServiceImpl.class).getMaterial(materialId);
    if (material.isBatchEnabled()) {
      transaction.setBatchId(batchId);
      Calendar cal = Calendar.getInstance();
      cal.set(cal.get(Calendar.YEAR) + 2, Calendar.DECEMBER, 31);
      transaction.setBatchExpiry(cal.getTime());
      cal.add(Calendar.YEAR, -3);
      transaction.setBatchManufacturedDate(cal.getTime());
      transaction.setBatchManufacturer("UNITB_MFR");
      transaction.setReason("UNIT_TESTS");
    }
    ims.updateInventoryTransactions(domainId, Collections.singletonList(transaction), true);
    Results results = ims.getInventoryTransactions(now, null, domainId, kioskId, materialId,
        Collections.singletonList(ITransaction.TYPE_ISSUE), null, null, null, null,
        new PageParams(10), null,
        false, null, null);
    assertNotNull(results);
    assertEquals(results.getResults().size(), 1, "There should be one Stockout transaction");
    transaction = (ITransaction) results.getResults().get(0);
    assertEquals(transaction.getBatchId(), batchId, "Batch Id missing in transaction");
    assertEquals(transaction.getQuantity(), quantity, "Batch quantity is not matching");
    assertTrue(BigUtil.equalsZero(transaction.getClosingStock()),
        "Batch quantity is not matching " + transaction.getClosingStock());
    assertTrue(BigUtil.equalsZero(transaction.getClosingStockByBatch()),
        "Batch quantity is not matching" + transaction.getClosingStockByBatch());
    IInvntry inv = ims.getInventory(kioskId, materialId);
    assertTrue(BigUtil.equalsZero(inv.getStock()), "Stock should be zero");
    assertTrue(BigUtil.equals(inv.getAllocatedStock(), 0),
        "Allocations should be zero a: " + inv.getAllocatedStock() + " e:" + 0);
    assertTrue(BigUtil.equals(inv.getAvailableStock(), 0),
        "Available should match a: " + inv.getAvailableStock() + " e:" + 0);
    IInvntryBatch iInvntryBatch = ims.getInventoryBatch(kioskId, materialId, batchId, null);
    if (iInvntryBatch != null) {
      assertTrue(BigUtil.equals(iInvntryBatch.getQuantity(), BigDecimal.ZERO),
          "Inventory batch quantity does not match" + iInvntryBatch.getQuantity() + ":"
              + BigDecimal.ZERO);
    }
  }


  @Test(sequential = true)
  public void testReceipt() throws ServiceException, ObjectNotFoundException, DuplicationException {
    Date now = new Date();
    Long domainId = getDomainId("Default").getId();
    Long kioskId = getKiosk(domainId, "entitytest").getKioskId();
    Long materialId = getMaterialId(domainId, "BCG (dose)").getMaterialId();
    Long vendorId = getKiosk(domainId, "storetest").getKioskId();
    String userId = getUser("dilbert").getUserId();
    String batchId = "UNITTB_123";
    BigDecimal quantity = BigDecimal.valueOf(100);
    resetStock(kioskId, materialId, domainId, quantity, userId);
    InventoryManagementService
        ims =
        Services.getService(InventoryManagementServiceImpl.class);
    ITransaction transaction = JDOUtils.createInstance(ITransaction.class);
    transaction.setDomainId(domainId);
    transaction.setKioskId(kioskId);
    transaction.setMaterialId(materialId);
    transaction.setType(ITransaction.TYPE_RECEIPT);
    transaction.setQuantity(quantity);
    transaction.setSourceUserId(userId);
    transaction.setSrc(SourceConstants.WEB);
    IMaterial
        material =
        Services.getService(MaterialCatalogServiceImpl.class).getMaterial(materialId);
    if (material.isBatchEnabled()) {
      transaction.setBatchId(batchId);
      Calendar cal = Calendar.getInstance();
      cal.set(cal.get(Calendar.YEAR) + 2, Calendar.DECEMBER, 31);
      transaction.setBatchExpiry(cal.getTime());
      cal.add(Calendar.YEAR, -3);
      transaction.setBatchManufacturedDate(cal.getTime());
      transaction.setBatchManufacturer("UNITB_MFR");
      transaction.setReason("UNIT_TESTS");
    }
    ims.updateInventoryTransactions(domainId, Collections.singletonList(transaction), true);
    Results results = ims.getInventoryTransactions(now, null, domainId, kioskId, materialId,
        Collections.singletonList(ITransaction.TYPE_RECEIPT), null, null, null, null,
        new PageParams(10), null,
        false, null, null);
    assertNotNull(results);
    assertEquals(results.getResults().size(), 1, "There should be one Stockout transaction");
    transaction = (ITransaction) results.getResults().get(0);
    assertEquals(transaction.getBatchId(), batchId, "Batch Id missing in transaction");
    assertEquals(transaction.getQuantity(), quantity, "Batch quantity is not matching");
    assertTrue(BigUtil.equals(transaction.getClosingStock(), 200),
        "Batch quantity is not matching");
    assertTrue(BigUtil.equals(transaction.getClosingStockByBatch(), 200),
        "Batch quantity is not matching");
    IInvntry inv = ims.getInventory(kioskId, materialId);
    assertTrue(BigUtil.equals(inv.getStock(), 200), "Stock should be zero");
    assertTrue(BigUtil.equals(inv.getAllocatedStock(), 0),
        "Allocations should be zero a: " + inv.getAllocatedStock() + " e:" + 0);
    assertTrue(BigUtil.equals(inv.getAvailableStock(), 200),
        "Available should match a: " + inv.getAvailableStock() + " e:" + 200);
    IInvntryBatch iInvntryBatch = ims.getInventoryBatch(kioskId, materialId, batchId, null);
    if (iInvntryBatch != null) {
      assertTrue(BigUtil.equals(iInvntryBatch.getQuantity(), 200),
          "Inventory batch quantity does not match" + iInvntryBatch.getQuantity() + ":"
              + BigDecimal.ZERO);
    }
  }

  @Test(sequential = true)
  public void testDiscard() throws ServiceException, ObjectNotFoundException, DuplicationException {
    Date now = new Date();
    Long domainId = getDomainId("Default").getId();
    Long kioskId = getKiosk(domainId, "entitytest").getKioskId();
    Long materialId = getMaterialId(domainId, "BCG (dose)").getMaterialId();
    Long vendorId = getKiosk(domainId, "storetest").getKioskId();
    String userId = getUser("dilbert").getUserId();
    String batchId = "UNITTB_123";
    BigDecimal quantity = BigDecimal.valueOf(100);
    resetStock(kioskId, materialId, domainId, quantity, userId);
    InventoryManagementService
        ims =
        Services.getService(InventoryManagementServiceImpl.class);
    ITransaction transaction = JDOUtils.createInstance(ITransaction.class);
    transaction.setDomainId(domainId);
    transaction.setKioskId(kioskId);
    transaction.setMaterialId(materialId);
    transaction.setType(ITransaction.TYPE_WASTAGE);
    transaction.setQuantity(quantity);
    transaction.setSourceUserId(userId);
    transaction.setSrc(SourceConstants.WEB);
    IMaterial
        material =
        Services.getService(MaterialCatalogServiceImpl.class).getMaterial(materialId);
    if (material.isBatchEnabled()) {
      transaction.setBatchId(batchId);
      Calendar cal = Calendar.getInstance();
      cal.set(cal.get(Calendar.YEAR) + 2, Calendar.DECEMBER, 31);
      transaction.setBatchExpiry(cal.getTime());
      cal.add(Calendar.YEAR, -3);
      transaction.setBatchManufacturedDate(cal.getTime());
      transaction.setBatchManufacturer("UNITB_MFR");
    }
    transaction.setReason("UNIT_TESTS");

    ims.updateInventoryTransactions(domainId, Collections.singletonList(transaction), true);
    Results results = ims.getInventoryTransactions(now, null, domainId, kioskId, materialId,
        Collections.singletonList(ITransaction.TYPE_WASTAGE), null, null, null, null,
        new PageParams(10), null,
        false, null, null);
    assertNotNull(results);
    assertEquals(results.getResults().size(), 1, "There should be one Stockout transaction");
    transaction = (ITransaction) results.getResults().get(0);
    assertEquals(transaction.getBatchId(), batchId, "Batch Id missing in transaction");
    assertEquals(transaction.getQuantity(), quantity, "Batch quantity is not matching");
    assertTrue(BigUtil.equals(transaction.getClosingStock(), 0), "Batch quantity is not matching");
    assertTrue(BigUtil.equals(transaction.getClosingStockByBatch(), 0),
        "Batch quantity is not matching");
    IInvntry inv = ims.getInventory(kioskId, materialId);
    assertTrue(BigUtil.equals(inv.getStock(), 0), "Stock should be zero");
    assertTrue(BigUtil.equals(inv.getAllocatedStock(), 0),
        "Allocations should be zero a: " + inv.getAllocatedStock() + " e:" + 0);
    assertTrue(BigUtil.equals(inv.getAvailableStock(), 0),
        "Available should match a: " + inv.getAvailableStock() + " e:" + 0);
    IInvntryBatch iInvntryBatch = ims.getInventoryBatch(kioskId, materialId, batchId, null);
    if (iInvntryBatch != null) {
      assertTrue(BigUtil.equals(iInvntryBatch.getQuantity(), 0),
          "Inventory batch quantity does not match" + iInvntryBatch.getQuantity() + ":"
              + BigDecimal.ZERO);
    }
  }


  private void createAllocation(Long kioskId, Long materialId, Long domainId, BigDecimal quantity,
                                String userId, String batchId, String orderId) throws Exception {

    resetStock(kioskId, materialId, domainId, quantity, userId);

    InventoryManagementService
        ims =
        Services.getService(InventoryManagementServiceImpl.class);
    ShipmentItemBatchModel batchModel = new ShipmentItemBatchModel();
    batchModel.id = batchId;
    batchModel.q = quantity;
    List<ShipmentItemBatchModel> bdetails = Collections.singletonList(batchModel);

    ims.allocate(kioskId, materialId, IInvAllocation.Type.ORDER, orderId, "ORDER:TESTORDER", null,
        bdetails,
        userId, null);
    List<IInvAllocation>
        allocations =
        ims.getAllocationsByTypeId(kioskId, materialId, IInvAllocation.Type.ORDER, orderId);
    assertEquals(allocations.size(), 1, "Allocation is missing");
    IInvAllocation a = allocations.get(0);
    IInvntry inv = ims.getInventory(kioskId, materialId);

    validateInventory(inv, a.getQuantity(), BigDecimal.ZERO, quantity);

    validateAllocation(a, batchId, quantity);
  }

  @Test(sequential = true)
  public void testAllocations() throws Exception {
    Long domainId = getDomainId("Default").getId();
    Long kioskId = getKiosk(domainId, "entitytest").getKioskId();
    Long materialId = getMaterialId(domainId, "BCG (dose)").getMaterialId();
    String userId = getUser("dilbert").getUserId();
    String batchId = "UNITTB_123";
    BigDecimal quantity = BigDecimal.valueOf(100);
    String testOrderId = "TESTORDER";

    createAllocation(kioskId, materialId, domainId, quantity, userId, batchId, testOrderId);

    resetStock(kioskId, materialId, domainId, quantity, userId);
    InventoryManagementService
        ims =
        Services.getService(InventoryManagementServiceImpl.class);
    List<IInvAllocation>
        allocations =
        ims.getAllocationsByTypeId(kioskId, materialId, IInvAllocation.Type.ORDER, testOrderId);
    assertEquals(allocations.size(), 0, "Allocations should have been cleared after stock count");
    IInvntry inv = ims.getInventory(kioskId, materialId);
    validateInventory(inv, BigDecimal.ZERO, quantity, quantity);

  }


  @Test(sequential = true)
  public void testClearAllocationsByKIDMID() throws Exception {
    Long domainId = getDomainId("Default").getId();
    Long kioskId = getKiosk(domainId, "entitytest").getKioskId();
    Long materialId = getMaterialId(domainId, "BCG (dose)").getMaterialId();
    String userId = getUser("dilbert").getUserId();
    String batchId = "UNITTB_123";
    BigDecimal quantity = BigDecimal.valueOf(100);
    String testOrderId = "TESTORDER";

    createAllocation(kioskId, materialId, domainId, quantity, userId, batchId, testOrderId);

    InventoryManagementService
        ims =
        Services.getService(InventoryManagementServiceImpl.class);
    ims.clearAllocation(kioskId, materialId, null, null);

    List<IInvAllocation>
        allocations =
        ims.getAllocationsByTypeId(kioskId, materialId, IInvAllocation.Type.ORDER, testOrderId);
    assertEquals(allocations.size(), 0,
        "Allocations should have been cleared after calling clear allocations");
    IInvntry inv = ims.getInventory(kioskId, materialId);
    validateInventory(inv, BigDecimal.ZERO, quantity, quantity);
  }

  @Test(sequential = true)
  public void testClearAllocationsByTag() throws Exception {
    Long domainId = getDomainId("Default").getId();
    Long kioskId = getKiosk(domainId, "entitytest").getKioskId();
    Long materialId = getMaterialId(domainId, "BCG (dose)").getMaterialId();
    String userId = getUser("dilbert").getUserId();
    String batchId = "UNITTB_123";
    BigDecimal quantity = BigDecimal.valueOf(100);
    String testOrderId = "TESTORDER";

    createAllocation(kioskId, materialId, domainId, quantity, userId, batchId, testOrderId);

    InventoryManagementService
        ims =
        Services.getService(InventoryManagementServiceImpl.class);
    ims.clearAllocationByTag(kioskId, materialId, "ORDER:TESTORDER", null);

    List<IInvAllocation>
        allocations =
        ims.getAllocationsByTypeId(kioskId, materialId, IInvAllocation.Type.ORDER, testOrderId);
    assertEquals(allocations.size(), 0,
        "Allocations should have been cleared after calling clear allocations");
    IInvntry inv = ims.getInventory(kioskId, materialId);
    validateInventory(inv, BigDecimal.ZERO, quantity, quantity);
  }


  private void validateInventory(IInvntry inv, BigDecimal aq, BigDecimal atp, BigDecimal stk) {
    assertTrue(BigUtil.equals(inv.getAllocatedStock(), aq),
        "Allocated stock does not match with inventory a: " + inv.getAllocatedStock() + " e:" + aq);
    assertTrue(BigUtil.equals(inv.getAvailableStock(), atp),
        "Available stock does not match with inventory a: " + inv.getAvailableStock() + " e:"
            + atp);
    assertTrue(BigUtil.equals(inv.getStock(), stk),
        "Current stock does not match with inventory a: " + inv.getStock() + " e:" + stk);
  }

  private void validateAllocation(IInvAllocation a, String batchId, BigDecimal quantity) {
    assertEquals(a.getBatchId(), batchId, "Batch Id allocated is not matching");
    assertTrue(BigUtil.equals(a.getQuantity(), quantity),
        "Allocated quantity for order is not matching a: "
            + quantity + " e: " + a.getQuantity());
  }


}

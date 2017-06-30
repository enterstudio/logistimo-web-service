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

import com.logistimo.LgTestCase;
import com.logistimo.api.builders.ShipmentBuilder;
import com.logistimo.auth.SecurityConstants;
import com.logistimo.config.models.OptimizerConfig;
import com.logistimo.constants.SourceConstants;
import com.logistimo.config.models.DomainConfig;
import com.logistimo.constants.Constants;
import com.logistimo.dao.JDOUtils;
import com.logistimo.inventory.entity.IInvAllocation;
import com.logistimo.inventory.entity.ITransaction;
import com.logistimo.inventory.service.InventoryManagementService;
import com.logistimo.inventory.service.impl.InventoryManagementServiceImpl;
import com.logistimo.models.shipments.ShipmentItemBatchModel;
import com.logistimo.models.shipments.ShipmentItemModel;
import com.logistimo.models.shipments.ShipmentMaterialsModel;
import com.logistimo.models.shipments.ShipmentModel;
import com.logistimo.orders.OrderResults;
import com.logistimo.orders.entity.IOrder;
import com.logistimo.orders.service.OrderManagementService;
import com.logistimo.orders.service.impl.OrderManagementServiceImpl;
import com.logistimo.services.DuplicationException;
import com.logistimo.services.ObjectNotFoundException;
import com.logistimo.services.ServiceException;
import com.logistimo.services.Services;

import com.logistimo.config.models.DomainConfig;
import com.logistimo.pagination.PageParams;
import com.logistimo.pagination.Results;
import com.logistimo.security.SecureUserDetails;
import com.logistimo.shipments.ShipmentStatus;
import com.logistimo.shipments.entity.IShipment;
import com.logistimo.shipments.entity.IShipmentItem;
import com.logistimo.shipments.entity.IShipmentItemBatch;
import com.logistimo.shipments.service.IShipmentService;
import com.logistimo.shipments.service.impl.ShipmentService;
import com.logistimo.users.entity.IUserAccount;
import com.logistimo.utils.BigUtil;
import com.logistimo.constants.Constants;
import org.testng.annotations.Test;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import static com.logistimo.users.entity.IUserAccount.*;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;


/**
 * Created by charan on 26/10/16.
 */
public class OrderMgmtServiceTest extends LgTestCase {

  Long orderId = null;

  private Long createOrderLocal()
      throws ServiceException, DuplicationException, ObjectNotFoundException {
    OrderManagementService oms = Services.getService(OrderManagementServiceImpl.class);
    Long domainId = getDomainId("Default").getId();
    Long kioskId = getKiosk(domainId, "entitytest").getKioskId();
    Long materialId = getMaterialId(domainId, "BCG (dose)").getMaterialId();
    Long vendorId = getKiosk(domainId, "storetest").getKioskId();
    String userId = getUser("dilbert").getUserId();
    resetStock(kioskId, materialId, domainId, BigDecimal.valueOf(2000), userId);
    ITransaction t = JDOUtils.createInstance(ITransaction.class);
    t.setDomainId(domainId);
    t.setKioskId(kioskId);
    t.setLinkedKioskId(vendorId);
    t.setQuantity(BigDecimal.valueOf(100));
    t.setMaterialId(materialId);
    t.setSourceUserId(userId);
    List<ITransaction> transList = Collections.singletonList(t);
    OrderResults
        or =
        oms.updateOrderTransactions(domainId, userId, ITransaction.TYPE_ORDER, transList, kioskId,
            1234l,
            "Hi creating new order", true, vendorId, 1d, 1d, 1d, null, null, null, BigDecimal.TEN,
            "Cash", "100",
            false, Collections.singletonList("TEST"), IOrder.NONTRANSFER, true, "Funny", null,
            null);
    IOrder order = or.getOrder();
    this.orderId = order.getOrderId();
    assertNotNull(order, "Order created successfully");
    return this.orderId;
  }

  @Test
  public void createOrder() throws DuplicationException, ServiceException, ObjectNotFoundException {
    createOrderLocal();
  }

  @Test
  public void checkTransactions()
      throws ServiceException, ObjectNotFoundException, DuplicationException {

    OrderManagementService oms = Services.getService(OrderManagementServiceImpl.class);
    Date now = new Date();
    Long domainId = getDomainId("Default").getId();
    try {
      DomainConfig dc = DomainConfig.getInstance(domainId);
      dc.getOrdersConfig().setAllocateStockOnConfirmation(true);
      dc.getOrdersConfig().setAllowMarkOrderAsConfirmed(true);
      dc.setAutoGI(true);
      dc.setAutoGR(true);
      setDomainConfig(domainId, dc);

      Long kioskId = getKiosk(domainId, "entitytest").getKioskId();
      Long materialId = getMaterialId(domainId, "BCG (dose)").getMaterialId();
      Long vendorId = getKiosk(domainId, "storetest").getKioskId();
      String userId = getUser("dilbert").getUserId();
      resetStock(vendorId, materialId, domainId, BigDecimal.valueOf(2000), userId);
      ITransaction t = JDOUtils.createInstance(ITransaction.class);
      BigDecimal q = BigDecimal.valueOf(100);
      t.setDomainId(domainId);
      t.setKioskId(kioskId);
      t.setLinkedKioskId(vendorId);
      t.setQuantity(q);
      t.setMaterialId(materialId);
      t.setSourceUserId(userId);
      List<ITransaction> transList = Collections.singletonList(t);
      OrderResults
          or =
          oms.updateOrderTransactions(domainId, userId, ITransaction.TYPE_ORDER, transList, kioskId,
              1234l, "Hi createing new order", true, vendorId, 1d, 1d, 1d, null, null, null,
              BigDecimal.TEN, "Cash", "100",
              false, Collections.singletonList("TEST"), IOrder.NONTRANSFER, true, "Funny", null,
              null);
      IOrder order = or.getOrder();
      this.orderId = order.getOrderId();
      assertNotNull(order, "Order created successfully");
      InventoryManagementService
          ims =
          Services.getService(InventoryManagementServiceImpl.class);
      List<IInvAllocation>
          alloc =
          ims.getAllocationsByTypeId(vendorId, materialId, IInvAllocation.Type.ORDER,
              String.valueOf(order.getOrderId()));
      assertNotNull(alloc, "Auto allocation failed for order");
      assertTrue(BigUtil.equals(alloc.get(0).getQuantity(), q),
          "Allocation mismatch a:" + alloc.get(0).getQuantity() + " e:" + q);
      IShipmentService ss = Services.getService(ShipmentService.class);
      order = oms.getOrder(orderId, true);
      String
          shipmentId =
          oms.shipNow(order, "Tusker India", "TS1234", "Reaping profits", null, userId, null,SourceConstants.WEB);
      IShipment shipment = ss.getShipment(shipmentId);
      assertEquals(shipment.getShipmentItems().size(), 1, "Shipment count doesnt match");
      Results results = ims.getInventoryTransactions(now, null, domainId, vendorId, materialId,
          Collections.singletonList(ITransaction.TYPE_ISSUE), null, null, null, null,
          new PageParams(1), null,
          false, null, null);
      assertNotNull(results);
      assertEquals(results.getResults().size(), 1, "There should be one transaction issue");
      ITransaction transaction = (ITransaction) results.getResults().get(0);
      assertEquals(transaction.getBatchId(), alloc.get(0).getBatchId(),
          "Batch Id missing in transaction");
      SecureUserDetails sUser = new SecureUserDetails();
      sUser.setDomainId(domainId);
      sUser.setUsername(userId);
      sUser.setLocale(Locale.ENGLISH);
      sUser.setRole(SecurityConstants.ROLE_SUPERUSER);
      ShipmentMaterialsModel smm = new ShipmentMaterialsModel();
      ShipmentModel sModel = new ShipmentBuilder().buildShipmentModel(shipment, sUser, true);
      smm.afd = new SimpleDateFormat(Constants.DATE_FORMAT).format(new Date());
      smm.kid = vendorId;
      smm.msg = "Hey !! I just received these items";
      smm.userId = userId;
      smm.sId = shipmentId;
      smm.items = sModel.items;
      for (ShipmentItemModel sim : sModel.items) {
        sim.fmst = "VVM Usable";
        sim.frsn = "DONE";
        if (sim.bq != null) {
          for (ShipmentItemBatchModel sib : sim.bq) {
            sib.fq = sib.q;
          }
        }
      }
      ss.fulfillShipment(smm, userId,SourceConstants.WEB);
      shipment = ss.getShipment(shipmentId);
      assertEquals(shipment.getStatus(), ShipmentStatus.FULFILLED, "Shipment status doesn't match");
      List<IShipmentItem> items = (List<IShipmentItem>) shipment.getShipmentItems();
      assertEquals(items.size(), 1, "Shipment count doesnt match");
      IShipmentItem item = items.get(0);
      assertTrue(BigUtil.equals(item.getFulfilledQuantity(), q),
          "Fulfilled quantity should be 100 but was a:" + item.getFulfilledQuantity());
      List<IShipmentItemBatch> batches = (List<IShipmentItemBatch>) item.getShipmentItemBatch();
      assertEquals(batches.size(), 1, "Shipment batch sizes does not match");
      assertTrue(BigUtil.equals(batches.get(0).getFulfilledQuantity(), q),
          "Shipment batch quantity does not match a:" + batches.get(0).getFulfilledQuantity()
              + " e:" + q);
    } finally {
      resetDomainConfig(domainId);
    }

  }

  @Test
  public void shipOrderTest()
      throws ServiceException, ObjectNotFoundException, DuplicationException {

    createShipmentLocal();

  }

  public String createShipmentLocal()
      throws ServiceException, ObjectNotFoundException, DuplicationException {
    Long orderId = createOrderLocal();
    IShipmentService ss = Services.getService(ShipmentService.class);
    Date now = new Date();
    Long domainId = getDomainId("Default").getId();
    DomainConfig dc = DomainConfig.getInstance(domainId);
    dc.getOrdersConfig().setAllocateStockOnConfirmation(true);
    dc.getOrdersConfig().setAllowMarkOrderAsConfirmed(true);
    dc.setAutoGI(true);
    dc.setAutoGR(true);
    setDomainConfig(domainId, dc);

    Long kioskId = getKiosk(domainId, "entitytest").getKioskId();
    Long materialId = getMaterialId(domainId, "BCG (dose)").getMaterialId();
    Long vendorId = getKiosk(domainId, "storetest").getKioskId();
    String userId = getUser("dilbert").getUserId();

    ShipmentModel m = new ShipmentModel();
    m.orderId = orderId;
    m.customerId = kioskId;
    m.vendorId = vendorId;
    m.transporter = "Tusker India";
    m.trackingId = "TS1234";
    m.reason = "test";
    m.sdid = domainId;
    ShipmentItemModel im = new ShipmentItemModel();
    im.mId = materialId;
    im.q = BigDecimal.valueOf(100l);
    im.aq = BigDecimal.valueOf(100l);
    im.afo = true;
    im.isBa = true;
    List<ShipmentItemModel> itemModels = new ArrayList<>();
    itemModels.add(im);
    m.items = itemModels;

    String shipmentId = ss.createShipment(m, SourceConstants.WEB);
    assertEquals(shipmentId, orderId + "-1", "Shipment Id does not match");
    return shipmentId;

  }


  @Test
  public void fulfilOrderTest()
      throws ServiceException, ObjectNotFoundException, DuplicationException {

    String shipmentId = createShipmentLocal();
    OrderManagementService oms = Services.getService(OrderManagementServiceImpl.class);
    IShipmentService ss = Services.getService(ShipmentService.class);
    Date now = new Date();
    Long domainId = getDomainId("Default").getId();
    try {
      DomainConfig dc = DomainConfig.getInstance(domainId);
      dc.getOrdersConfig().setAllocateStockOnConfirmation(true);
      dc.getOrdersConfig().setAllowMarkOrderAsConfirmed(true);
      dc.setAutoGI(true);
      dc.setAutoGR(true);
      setDomainConfig(domainId, dc);

      Long kioskId = getKiosk(domainId, "entitytest").getKioskId();
      Long materialId = getMaterialId(domainId, "BCG (dose)").getMaterialId();
      Long vendorId = getKiosk(domainId, "storetest").getKioskId();
      String userId = getUser("dilbert").getUserId();

      ss.fulfillShipment(shipmentId, userId,SourceConstants.WEB);

      Long orderId = Long.valueOf(shipmentId.substring(0, shipmentId.indexOf("-")));
//
      IOrder order = oms.getOrder(orderId);
      assertEquals(order.getStatus(), "fl");

      InventoryManagementService
          ims =
          Services.getService(InventoryManagementServiceImpl.class);
//            Results resultsv = ims.getInventoryTransactions(now, null, domainId, vendorId, materialId,
//                    Collections.singletonList(ITransaction.TYPE_ISSUE), null, null, null, null, new PageParams(10), null,
//                    false, null, null);

      Results resultsc = ims.getInventoryTransactions(now, null, domainId, kioskId, materialId,
          Collections.singletonList(ITransaction.TYPE_RECEIPT), null, null, null, null,
          new PageParams(10), null,
          false, null, null);
      assertNotNull(resultsc);
      assertEquals(resultsc.getResults().size(), 1, "There should be one transaction issue");

    } finally {
      resetDomainConfig(domainId);
    }
  }
}

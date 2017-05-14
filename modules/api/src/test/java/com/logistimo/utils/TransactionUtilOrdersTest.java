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

package com.logistimo.utils;

import com.logistimo.bulkuploads.BulkUploadMgr;
import com.logistimo.bulkuploads.MnlTransactionUtil;
import com.logistimo.dao.JDOUtils;

import com.logistimo.LgTestCase;
import com.logistimo.config.models.DomainConfig;
import com.logistimo.inventory.TransactionUtil;
import com.logistimo.materials.entity.IMaterial;
import com.logistimo.mnltransactions.entity.IMnlTransaction;
import com.logistimo.mnltransactions.entity.MnlTransaction;
import com.logistimo.orders.entity.IDemandItem;
import com.logistimo.orders.entity.IOrder;
import com.logistimo.orders.service.OrderManagementService;
import com.logistimo.orders.service.impl.OrderManagementServiceImpl;
import com.logistimo.services.DuplicationException;
import com.logistimo.services.ObjectNotFoundException;
import com.logistimo.services.ServiceException;
import com.logistimo.services.Services;
import com.logistimo.shipments.ShipmentStatus;
import com.logistimo.shipments.entity.IShipment;
import com.logistimo.shipments.entity.IShipmentItem;
import com.logistimo.shipments.service.IShipmentService;
import com.logistimo.shipments.service.impl.ShipmentService;

import org.testng.annotations.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;

/**
 * Created by charan on 25/10/16.
 */
public class TransactionUtilOrdersTest extends LgTestCase {

  @Test
  public void testOrderRowList()
      throws ServiceException, ObjectNotFoundException, DuplicationException {
    List<MnlTransactionUtil.OrderRow> orderRowList = new ArrayList<>(1);

    List<String> orderTags = Collections.singletonList("EMERGENCY");
    Long domainId = getDomainId("Default").getId();
    try {
      DomainConfig dc = DomainConfig.getInstance(domainId);
      dc.setAutoGI(false);
      dc.setAutoGR(false);
      dc.getInventoryConfig().getManualTransConfig().enableManualUploadInvDataAndTrans = true;
      setDomainConfig(domainId, dc);

      Long kioskId = getKiosk(domainId, "entitytest").getKioskId();
      IMaterial material = getMaterialId(domainId, "5 ml syringe");
      assertFalse(material.isBatchEnabled(),
          "5 ml syringe should be configured to be batch disabled.");
      Long materialId = material.getMaterialId();
      Long vendorId = getKiosk(domainId, "storetest").getKioskId();
      String userId = getUser("dilbert").getUserId();
      IMnlTransaction mnlTrans = JDOUtils.createInstance(IMnlTransaction.class);
      mnlTrans.setFulfilledQuantity(BigDecimal.valueOf(100));
      mnlTrans.setOrderedQuantity(BigDecimal.valueOf(100));
      mnlTrans.setKioskId(kioskId);
      mnlTrans.setMaterialId(materialId);
      mnlTrans.setDomainId(domainId);
      mnlTrans.setUserId(userId);
      mnlTrans.setTimestamp(new Date());
      mnlTrans.setTags(orderTags);
      mnlTrans.setVendorId(vendorId);
      orderRowList.add(MnlTransactionUtil.getOrderRow(mnlTrans, "", 0, 1));
      Long orderId = MnlTransactionUtil.saveOrderRowList(orderRowList,
          BulkUploadMgr.TYPE_TRANSACTIONS_CUM_INVENTORY_METADATA);
      IShipmentService shipmentService = Services.getService(ShipmentService.class);
      OrderManagementService oms = Services.getService(OrderManagementServiceImpl.class);
      IOrder order = oms.getOrder(orderId, true);
      assertEquals(order.getKioskId(), mnlTrans.getKioskId(), "Kiosk does not match");
      assertEquals(order.getServicingKiosk(), mnlTrans.getVendorId(), "Kiosk does not match");
      assertEquals(order.getStatus(), IOrder.FULFILLED, "Order status should be fulfilled");
      assertEquals(order.getNumberOfItems().intValue(), 1, "Order demand Items should be one");
      assertEquals(order.getItems().size(), 1, "Order number of items should be one");
      IDemandItem di = order.getItem(materialId);
      assertEquals(di.getShippedQuantity(), BigDecimal.valueOf(100),
          "Demand item shipped quantity does not match");
      assertEquals(di.getFulfilledQuantity(), BigDecimal.valueOf(100),
          "Demand item fulfilled quantity does not match");
      List<IShipment> shipments = shipmentService.getShipmentsByOrderId(orderId);
      assertEquals(shipments.size(), 1, "Shipment should be one");
      assertEquals(shipments.get(0).getStatus(), ShipmentStatus.FULFILLED,
          "Shipment should be fulfilled");
      IShipment shipment = shipmentService.getShipment(shipments.get(0).getShipmentId());
      assertEquals(shipment.getShipmentItems().size(), 1, "ShipmentItem should be one");
      IShipmentItem shipmentItem = shipment.getShipmentItems().get(0);
      assertEquals(shipmentItem.getQuantity(), BigDecimal.valueOf(100),
          "ShipmentItem shipped quantity does not match");
      assertEquals(shipmentItem.getFulfilledQuantity(), BigDecimal.valueOf(100),
          "ShipmentItem fulfilled quantity does not match");
    } finally {
      resetDomainConfig(domainId);
    }

  }

  @Test
  public void testOrderDefaultVendor()
      throws ServiceException, ObjectNotFoundException, DuplicationException {
    List<MnlTransactionUtil.OrderRow> orderRowList = new ArrayList<>(1);

    List<String> orderTags = Collections.singletonList("EMERGENCY");
    Long domainId = getDomainId("Default").getId();
    try {
      DomainConfig dc = DomainConfig.getInstance(domainId);
      dc.setAutoGI(false);
      dc.setAutoGR(false);
      dc.getInventoryConfig().getManualTransConfig().enableManualUploadInvDataAndTrans = true;
      setDomainConfig(domainId, dc);

      Long kioskId = getKiosk(domainId, "entitytest").getKioskId();
      IMaterial material = getMaterialId(domainId, "5 ml syringe");
      assertFalse(material.isBatchEnabled(),
          "5 ml syringe should be configured to be batch disabled.");
      Long materialId = material.getMaterialId();
      Long vendorId = getKiosk(domainId, "storetest").getKioskId();
      String userId = getUser("dilbert").getUserId();
      IMnlTransaction mnlTrans = JDOUtils.createInstance(IMnlTransaction.class);
      mnlTrans.setFulfilledQuantity(BigDecimal.valueOf(100));
      mnlTrans.setOrderedQuantity(BigDecimal.valueOf(100));
      mnlTrans.setKioskId(kioskId);
      mnlTrans.setMaterialId(materialId);
      mnlTrans.setDomainId(domainId);
      mnlTrans.setUserId(userId);
      mnlTrans.setTimestamp(new Date());
      mnlTrans.setTags(orderTags);
      mnlTrans.setVendorId(null);
      orderRowList.add(MnlTransactionUtil.getOrderRow(mnlTrans, "", 0, 1));
      Long orderId = MnlTransactionUtil.saveOrderRowList(orderRowList,
          BulkUploadMgr.TYPE_TRANSACTIONS_CUM_INVENTORY_METADATA);
      IShipmentService shipmentService = Services.getService(ShipmentService.class);
      OrderManagementService oms = Services.getService(OrderManagementServiceImpl.class);
      IOrder order = oms.getOrder(orderId, true);
      assertEquals(order.getKioskId(), mnlTrans.getKioskId(), "Kiosk does not match");
      assertNotNull(order.getServicingKiosk(), "Default vendor is not set");
      assertEquals(order.getStatus(), IOrder.FULFILLED, "Order status should be fulfilled");
      assertEquals(order.getNumberOfItems().intValue(), 1, "Order demand Items should be one");
      assertEquals(order.getItems().size(), 1, "Order number of items should be one");
      IDemandItem di = order.getItem(materialId);
      assertEquals(di.getShippedQuantity(), BigDecimal.valueOf(100),
          "Demand item shipped quantity does not match");
      assertEquals(di.getFulfilledQuantity(), BigDecimal.valueOf(100),
          "Demand item fulfilled quantity does not match");
      List<IShipment> shipments = shipmentService.getShipmentsByOrderId(orderId);
      assertEquals(shipments.size(), 1, "Shipment should be one");
      assertEquals(shipments.get(0).getStatus(), ShipmentStatus.FULFILLED,
          "Shipment should be fulfilled");
      IShipment shipment = shipmentService.getShipment(shipments.get(0).getShipmentId());
      assertEquals(shipment.getShipmentItems().size(), 1, "ShipmentItem should be one");
      IShipmentItem shipmentItem = shipment.getShipmentItems().get(0);
      assertEquals(shipmentItem.getQuantity(), BigDecimal.valueOf(100),
          "ShipmentItem shipped quantity does not match");
      assertEquals(shipmentItem.getFulfilledQuantity(), BigDecimal.valueOf(100),
          "ShipmentItem fulfilled quantity does not match");
    } finally {
      resetDomainConfig(domainId);
    }

  }
}

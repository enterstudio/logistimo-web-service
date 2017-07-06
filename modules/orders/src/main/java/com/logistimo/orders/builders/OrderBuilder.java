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

package com.logistimo.orders.builders;

import com.logistimo.config.models.DomainConfig;
import com.logistimo.constants.Constants;
import com.logistimo.entities.builders.EntityBuilder;
import com.logistimo.entities.models.*;
import com.logistimo.inventory.entity.IInvntry;
import com.logistimo.inventory.service.InventoryManagementService;
import com.logistimo.logger.XLog;
import com.logistimo.materials.entity.IMaterial;
import com.logistimo.materials.service.MaterialCatalogService;
import com.logistimo.models.StatusModel;
import com.logistimo.orders.entity.IDemandItem;
import com.logistimo.orders.entity.IOrder;
import com.logistimo.orders.models.ExtendedOrderModel;
import com.logistimo.orders.models.OrderModel;
import com.logistimo.orders.service.OrderManagementService;
import com.logistimo.services.ObjectNotFoundException;
import com.logistimo.services.ServiceException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.stream.Collectors;

/**
 * Created by charan on 26/06/17.
 */
@Component
public class OrderBuilder {

    private static final XLog xlogger = XLog.getLog(OrderBuilder.class);

    @Autowired
    OrderManagementService orderManagementService;

    @Autowired
    EntityBuilder entityBuilder;

    @Autowired
    MaterialCatalogService materialCatalogService;

    @Autowired
    InventoryManagementService inventoryManagementService;

    public OrderModel buildMeta(Long orderId) throws ServiceException, ObjectNotFoundException {
        IOrder order = orderManagementService.getOrder(orderId);
        return buildOrderModel(order, false);
    }

    public OrderModel build(Long orderId) throws ServiceException, ObjectNotFoundException {
        IOrder order = orderManagementService.getOrder(orderId, true);
        return buildOrderModel(order, true);
    }

    private OrderModel buildOrderModel(IOrder order, boolean isExtended) throws ServiceException {

        OrderModel model;
        if (isExtended) {
            ExtendedOrderModel emodel = new ExtendedOrderModel();
            StatusModel status = new StatusModel();
            status.setStatus(order.getStatus());
            status.setUpdatedBy(order.getUpdatedBy());
            status.setUpdatedAt(order.getUpdatedOn());
            emodel.setStatus(status);
            buildOrderItems(order, emodel);
            model = emodel;
        } else {
            model = new OrderModel();
            model.setNumItems(order.getNumberOfItems());
        }
        if (isExtended) {
            model.setCustomer(entityBuilder.build(order.getKioskId(), true));
            if (order.getServicingKiosk() != null) {
                model.setVendor(entityBuilder.build(order.getServicingKiosk(), true));
            }
        } else {
            model.setCustomer(entityBuilder.buildMeta(order.getKioskId()));
            if (order.getServicingKiosk() != null) {
                model.setVendor(entityBuilder.buildMeta(order.getServicingKiosk()));
            }
        }
        model.setOrderId(order.getOrderId());
        model.setCreatedAt(order.getCreatedOn());
        return model;
    }

    private void buildOrderItems(IOrder order, ExtendedOrderModel model) {

        if (order.getItems() != null && !order.getItems().isEmpty()) {
            DomainConfig dc = DomainConfig.getInstance(order.getKioskDomainId());
            String crFreq = dc.getInventoryConfig().getDisplayCRFreq();
            model.setItems(
                    order.getItems().stream()
                            .map(demandItem -> buildDemandItemModel(order, demandItem, crFreq))
                            .collect(Collectors.toList()));
        }
    }

    private DemandItemModel buildDemandItemModel(IOrder order, IDemandItem demandItem,
                                                 String crFreq) {
        DemandItemModel i = new DemandItemModel();
        Long mid = demandItem.getMaterialId();
        IMaterial m = null;
        try {
            m = materialCatalogService.getMaterial(mid);
        } catch (Exception e) {
            xlogger.warn("WARNING: " + e.getClass().getName() + " when getting material "
                    + demandItem.getMaterialId() + ": " + e.getMessage());
            return null;
        }
        i.setMaterialId(String.valueOf(mid));
        i.setName(m.getName());
        i.setOriginallyOrdered(demandItem.getOriginalQuantity());
        i.setRecommended(demandItem.getRecommendedOrderQuantity());
        i.setOrdered(demandItem.getQuantity());
        Long ckioskid = order.getKioskId();
        CustomerVendor inventory = new CustomerVendor();
        if (ckioskid != null) {
            inventory.setCustomer(buildInventory(ckioskid, mid, crFreq));
        }
        Long vkioskid = order.getKioskId();
        if (vkioskid != null) {
            inventory.setVendor(buildInventory(vkioskid, mid));
        }
        i.setCustomerVendor(inventory);
        return i;
    }

    private Inventory buildInventory(Long kioskId, Long materialId) {
        return buildInventory(kioskId, materialId, null);
    }

    private Inventory buildInventory(Long kioskId, Long materialId, String crFreq) {
        Inventory inventory = null;
        IInvntry
                iInvntry =
                null;
        try {
            iInvntry = inventoryManagementService.getInventory(kioskId, materialId);
        } catch (ServiceException e) {
            xlogger
                    .warn("Issue with getting inventory for kiosk {0},material {1}", kioskId, materialId, e);
        }
        if (iInvntry != null) {
            inventory = new Inventory();
            inventory.setMax(iInvntry.getMaxStock());
            inventory.setMin(iInvntry.getReorderLevel());
            inventory.setStockOnHand(iInvntry.getStock());
            inventory.setAvailableStock(iInvntry.getAvailableStock());
            inventory.setAllocatedStock(iInvntry.getAllocatedStock());
        }
        if (crFreq != null) {
            BigDecimal consumptionRate = getStockConsumptionRate(iInvntry, crFreq);
            BigDecimal sap = inventoryManagementService
                    .getStockAvailabilityPeriod(consumptionRate, iInvntry.getStock());
            if (null != sap) {
                DurationOfStock ds = new DurationOfStock();
                ds.setDurationUnit(crFreq);
                ds.setDuration(sap);
                inventory.setDurationOfStock(ds);
            }
            Predictions predictions = new Predictions();
            predictions.setDurationUnit(crFreq);
            predictions.setConsumptionRate(consumptionRate);
            if (iInvntry.getPredictedDaysOfStock() != null) {
                predictions.setStockOutDuration(iInvntry.getPredictedDaysOfStock().intValue());
            }
            inventory.setPredictions(predictions);
        }
        return inventory;
    }

    private BigDecimal getStockConsumptionRate(IInvntry invntry, String freq) {
        BigDecimal crate = null;
        if (Constants.FREQ_DAILY.equals(freq)) {
            crate = invntry.getConsumptionRateDaily();
        } else if (Constants.FREQ_WEEKLY.equals(freq)) {
            crate = invntry.getConsumptionRateWeekly();
        } else if (Constants.FREQ_MONTHLY.equals(freq)) {
            crate = invntry.getConsumptionRateMonthly();
        }
        return crate;
    }

}

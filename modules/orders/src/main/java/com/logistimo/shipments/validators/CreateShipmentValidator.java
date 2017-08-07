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

package com.logistimo.shipments.validators;

import com.logistimo.entities.entity.IKiosk;
import com.logistimo.entities.service.EntitiesService;
import com.logistimo.entities.service.EntitiesServiceImpl;
import com.logistimo.exception.ValidationException;
import com.logistimo.inventory.entity.IInvntry;
import com.logistimo.inventory.service.InventoryManagementService;
import com.logistimo.inventory.service.impl.InventoryManagementServiceImpl;
import com.logistimo.logger.XLog;
import com.logistimo.materials.entity.IMaterial;
import com.logistimo.materials.service.MaterialCatalogService;
import com.logistimo.materials.service.MaterialUtils;
import com.logistimo.materials.service.impl.MaterialCatalogServiceImpl;
import com.logistimo.models.shipments.ShipmentItemModel;
import com.logistimo.models.shipments.ShipmentModel;
import com.logistimo.orders.approvals.service.IOrderApprovalsService;
import com.logistimo.orders.entity.IOrder;
import com.logistimo.orders.service.OrderManagementService;
import com.logistimo.orders.validators.UpdateOrderStatusValidator;
import com.logistimo.services.ObjectNotFoundException;
import com.logistimo.services.ServiceException;
import com.logistimo.services.Services;
import com.logistimo.utils.MsgUtil;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by charan on 16/07/17.
 */
@Component
public class CreateShipmentValidator {

  private static final XLog xLogger = XLog.getLog(CreateShipmentValidator.class);


  @Autowired
  EntitiesService entitiesService;

  @Autowired
  MaterialCatalogService materialCatalogService;

  @Autowired
  InventoryManagementService inventoryManagementService;

  @Autowired
  IOrderApprovalsService orderApprovalsService;

  @Autowired
  OrderManagementService orderManagementService;

  @Autowired
  UpdateOrderStatusValidator orderStatusValidator;

  public void validate(ShipmentModel model)
      throws ServiceException, ValidationException, ObjectNotFoundException {
    checkIfMaterialsExist(model);
    IOrder order = orderManagementService.getOrder(model.orderId, false);
    orderStatusValidator.checkIfVisibleToVendor(order);
    orderStatusValidator.checkTransferStatus(order);
    orderStatusValidator.checkShippingApproval(order);
  }

  private void checkIfMaterialsExist(ShipmentModel model)
      throws ServiceException, ValidationException {
    // Validate only vendor inventory while creating shipment
    List<IMaterial>
        materialsNotExistingInVendor =
        getMaterialsNotExistingInKiosk(model.vendorId, model.items);
    if (materialsNotExistingInVendor != null && !materialsNotExistingInVendor.isEmpty()) {
      EntitiesService as = Services.getService(EntitiesServiceImpl.class);
      IKiosk vnd = as.getKiosk(model.vendorId, false);
      throw new ValidationException("I003", MsgUtil.bold(vnd.getName()),
          MaterialUtils.getMaterialNamesString(
              materialsNotExistingInVendor));
    }
  }

  private List<IMaterial> getMaterialsNotExistingInKiosk(Long kioskId,
                                                         List<ShipmentItemModel> models) {
    List<IMaterial> materialsNotExisting = new ArrayList<>(1);
    try {
      InventoryManagementService
          ims =
          Services.getService(InventoryManagementServiceImpl.class);
      MaterialCatalogService mcs = Services.getService(MaterialCatalogServiceImpl.class);
      for (ShipmentItemModel shipmentItem : models) {
        IInvntry inv = ims.getInventory(kioskId, shipmentItem.mId);
        if (inv == null) {
          IMaterial material = mcs.getMaterial(shipmentItem.mId);
          materialsNotExisting.add(material);
        }
      }
    } catch (ServiceException e) {
      xLogger.warn("Exception while getting materials not existing in kioskId {0}", kioskId, e);
    }
    return materialsNotExisting;
  }
}

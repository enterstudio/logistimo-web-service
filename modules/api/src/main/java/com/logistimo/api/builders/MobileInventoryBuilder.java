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

package com.logistimo.api.builders;

import com.logistimo.api.models.MobileInvModel;
import com.logistimo.config.models.DomainConfig;
import com.logistimo.inventory.entity.IInvntry;
import com.logistimo.inventory.service.InventoryManagementService;
import com.logistimo.inventory.service.impl.InventoryManagementServiceImpl;
import com.logistimo.logger.XLog;
import com.logistimo.materials.entity.IMaterial;
import com.logistimo.materials.service.impl.MaterialCatalogServiceImpl;
import com.logistimo.services.ServiceException;
import com.logistimo.services.Services;
import com.logistimo.tags.TagUtil;

import java.util.Locale;


public class MobileInventoryBuilder {

  private static final XLog xLogger = XLog.getLog(MobileInventoryBuilder.class);

  public MobileInvModel buildInventoryDetails(Long kioskId, Long materialId, Locale locale,
                                              Long domainId) {

    try {

      InventoryManagementService
          ims =
          Services.getService(InventoryManagementServiceImpl.class, locale);
      IInvntry inventory = ims.getInventory(kioskId, materialId);

      MaterialCatalogServiceImpl
          mcs =
          Services.getService(MaterialCatalogServiceImpl.class, locale);
      IMaterial material = mcs.getMaterial(materialId);

      DomainConfig dc = DomainConfig.getInstance(domainId);

      MobileInvModel model = new MobileInvModel();

      model.mnm = inventory.getMaterialName();
      model.stk = inventory.getAvailableStock();
      model.reord = inventory.getReorderLevel();
      model.max = inventory.getMaxStock();
      model.astk = inventory.getAllocatedStock();
      model.tstk = inventory.getInTransitStock();
      model.tgs = inventory.getTags(TagUtil.TYPE_MATERIAL);
      model.pdos = inventory.getPredictedDaysOfStock();
      model.sap = ims.getStockAvailabilityPeriod(inventory, dc);
      model.mDes = material.getDescription();
      model.reordT = inventory.getReorderLevelUpdatedTime().toString();
      return model;

    } catch (ServiceException e) {
      xLogger.severe("Error in reading destination inventories", e);
      return null;
    }

  }

}

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

/**
 *
 */
package com.logistimo.events.generators;

import com.logistimo.assets.entity.IAsset;
import com.logistimo.assets.entity.IAssetRelation;
import com.logistimo.dao.JDOUtils;

import com.logistimo.assets.entity.IAssetStatus;
import com.logistimo.entities.entity.IKiosk;
import com.logistimo.entities.entity.IPoolGroup;
import com.logistimo.inventory.entity.IInvntry;
import com.logistimo.inventory.entity.IInvntryBatch;
import com.logistimo.inventory.entity.ITransaction;
import com.logistimo.materials.entity.IMaterial;
import com.logistimo.orders.entity.IOrder;
import com.logistimo.shipments.entity.IShipment;
import com.logistimo.users.entity.IUserAccount;

/**
 * Factory to generate event processors
 *
 * @author Arun
 */
public class EventGeneratorFactory {

  public static EventGenerator getEventGenerator(Long domainId, String objectType) {
    if (JDOUtils.getImplClass(IOrder.class).getName().equals(objectType) ||  JDOUtils.getImplClass(IShipment.class).getName().equals(objectType)) {
      return new OrdersEventGenerator(domainId,objectType);
    } else if (JDOUtils.getImplClass(ITransaction.class).getName().equals(objectType) || JDOUtils
        .getImplClass(IInvntry.class).getName().equals(objectType) || JDOUtils
        .getImplClass(IInvntryBatch.class).getName().equals(objectType)) {
      return new TransactionsEventGenerator(domainId, objectType);
    } else if (JDOUtils.getImplClass(IUserAccount.class).getName().equals(objectType) || JDOUtils
        .getImplClass(IKiosk.class).getName().equals(objectType) ||
        JDOUtils.getImplClass(IMaterial.class).getName().equals(objectType) || JDOUtils
        .getImplClass(IPoolGroup.class).getName().equals(objectType) || JDOUtils
        .getImplClass(IAsset.class).getName().equals(objectType) || JDOUtils.getImplClass(
        IAssetRelation.class).getName().equals(objectType)) {
      return new SetupEventGenerator(domainId, objectType);
    } else if (JDOUtils
        .getImplClass(IAssetStatus.class).getName().equals(objectType)) {
      return new AssetEventGenerator(domainId, objectType);
    } else {
      return new EventGenerator(domainId, objectType);
    }
  }
}

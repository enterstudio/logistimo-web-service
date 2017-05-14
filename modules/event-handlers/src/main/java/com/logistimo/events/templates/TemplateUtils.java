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

package com.logistimo.events.templates;

import com.logistimo.accounting.entity.IAccount;
import com.logistimo.assets.entity.AssetStatus;
import com.logistimo.assets.entity.IAsset;
import com.logistimo.assets.entity.IAssetRelation;
import com.logistimo.assets.entity.IAssetStatus;
import com.logistimo.assets.models.Temperature;
import com.logistimo.entities.entity.IKiosk;
import com.logistimo.entities.entity.IPoolGroup;
import com.logistimo.events.entity.IEvent;
import com.logistimo.inventory.entity.IInvntry;
import com.logistimo.inventory.entity.IInvntryBatch;
import com.logistimo.inventory.entity.ITransaction;
import com.logistimo.materials.entity.IHandlingUnit;
import com.logistimo.materials.entity.IMaterial;
import com.logistimo.orders.entity.IOrder;
import com.logistimo.shipments.entity.IShipment;
import com.logistimo.users.entity.IUserAccount;


import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Created by charan on 09/03/17.
 */
public class TemplateUtils {

  public static ITemplate getTemplateType(Object o){

    if(o instanceof IUserAccount){
      return new UserTemplate((IUserAccount) o);
    }else if(o instanceof IKiosk){
      return new KioskTemplate((IKiosk) o);
    }else if(o instanceof IMaterial){
      return new MaterialTemplate((IMaterial) o);
    }else if(o instanceof IInvntry){
      return new InvntryTemplate((IInvntry) o);
    }else if(o instanceof IOrder){
      return new OrderTemplate((IOrder) o);
    }else if(o instanceof IAsset){
      return new AssetTemplate((IAsset) o);
    }else if(o instanceof IInvntryBatch){
      return new InvntryBatchTemplate((IInvntryBatch) o);
    }else if(o instanceof IShipment){
      return new ShipmentTemplate((IShipment) o);
    }else if(o instanceof ITransaction){
      return new TransactionsTemplate((ITransaction) o);
    }else if(o instanceof IAssetStatus){
      return new AssetStatusTemplate((IAssetStatus) o);
    }else if(o instanceof IAccount){
      return new AccountTempalte((IAccount) o);
    }else if(o instanceof IHandlingUnit){
      return new HandlingUnitTemplate((IHandlingUnit) o);
    }else if(o instanceof IPoolGroup){
      return new PoolGroupTemplate((IPoolGroup) o);
    }else if(o instanceof IAssetRelation){
      return new AssetRelationTemplate((IAssetRelation) o);
    }
    throw new RuntimeException("Unkown object type "+ o.getClass().getSimpleName());
  }
}

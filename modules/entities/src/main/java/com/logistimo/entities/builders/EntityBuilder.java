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

package com.logistimo.entities.builders;

import com.logistimo.entities.entity.IKiosk;
import com.logistimo.entities.models.EntityMinModel;
import com.logistimo.entities.models.Location;
import com.logistimo.entities.service.EntitiesService;
import com.logistimo.logger.XLog;
import com.logistimo.services.ObjectNotFoundException;
import com.logistimo.services.ServiceException;
import com.logistimo.utils.CommonUtils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Created by charan on 26/06/17.
 */
@Component
public class EntityBuilder {

  private static final XLog LOGGER = XLog.getLog(EntityBuilder.class);
  @Autowired
  EntitiesService entitiesService;

  public EntityMinModel buildMeta(Long kioskId) throws ServiceException {
    return build(kioskId, false);
  }

  public EntityMinModel build(Long kioskId, boolean incAddress) throws ServiceException {
    EntityMinModel minModel = new EntityMinModel();
    minModel.setId(kioskId);
    try {
      IKiosk kiosk = entitiesService.getKioskIfPresent(kioskId, false);
      minModel.setName(kiosk.getName());
      minModel.setAddress(CommonUtils.getAddress(kiosk.getCity(), kiosk.getTaluk(),
          kiosk.getDistrict(), kiosk.getState()));
      if (incAddress) {
        Location location = new Location();
        location.setCity(kiosk.getCity());
        location.setDistrict(kiosk.getDistrict());
        location.setState(kiosk.getState());
        minModel.setLocation(location);
      }
    } catch (ObjectNotFoundException e) {
      LOGGER.info("Entity not found {0} while building meta model", kioskId);
    }
    return minModel;
  }
}

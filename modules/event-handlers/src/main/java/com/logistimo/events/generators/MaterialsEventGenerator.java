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

package com.logistimo.events.generators;

import com.logistimo.config.models.EventSpec;
import com.logistimo.events.handlers.EventHandler;
import com.logistimo.events.models.ObjectData;
import com.logistimo.materials.entity.IMaterial;

import java.util.List;

import javax.jdo.PersistenceManager;

/**
 * Created by charan on 07/03/17.
 */
public class MaterialsEventGenerator extends EventGenerator {

  public MaterialsEventGenerator(Long domainId, String objectType) {
    super(domainId, objectType);
  }

  @Override
  public List<String> getSubscriberIds(EventSpec.Subscriber subscriber, Object o, Long domainId) {
    xLogger.fine("Entered getSubscriberIds");
    if (subscriber == null) {
      return null;
    }
    if (EventSpec.Subscriber.ADMINISTRATORS.equals(subscriber.type) && o != null
        && !(o instanceof Long)) {
      if (domainId == null) {
        domainId = ((IMaterial) o).getDomainId();
      }
      return EventHandler.getSubsribers(subscriber.type, domainId);
    } else {
      return super.getSubscriberIds(subscriber, o, domainId);
    }
  }

  // Get common metadata associated with a given object
  @Override
  public ObjectData getObjectData(Object o, PersistenceManager pm) {
    ObjectData od = new ObjectData();
    if (o instanceof IMaterial) {
      IMaterial m = (IMaterial) o;
      od.oid = m.getMaterialId();
      od.materialId = (Long) od.oid;
      return od;
    } else {
      return super.getObjectData(o, pm);
    }
  }
}

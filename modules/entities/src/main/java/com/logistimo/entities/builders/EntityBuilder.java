package com.logistimo.entities.builders;

import com.logistimo.entities.entity.IKiosk;
import com.logistimo.entities.models.EntityMinModel;
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

  @Autowired
  EntitiesService entitiesService;

  private static final XLog LOGGER = XLog.getLog(EntityBuilder.class);


  public EntityMinModel buildMeta(Long kioskId) throws ServiceException {
    EntityMinModel minModel = new EntityMinModel();
    minModel.setId(kioskId);
    try {
      IKiosk kiosk = entitiesService.getKioskIfPresent(kioskId);
      minModel.setName(kiosk.getName());
      minModel.setAddress(CommonUtils.getAddress(kiosk.getCity(), kiosk.getTaluk(),
          kiosk.getDistrict(), kiosk.getState()));
    } catch (ObjectNotFoundException e) {
      LOGGER.info("Entity not found {0} while building meta model", kioskId);
    }
    return minModel;
  }
}

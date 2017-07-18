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
package com.logistimo.api.builders;

import com.logistimo.api.models.DemandModel;
import com.logistimo.config.models.DomainConfig;
import com.logistimo.config.models.InventoryConfig;
import com.logistimo.constants.Constants;
import com.logistimo.domains.entity.IDomain;
import com.logistimo.domains.service.DomainsService;
import com.logistimo.domains.service.impl.DomainsServiceImpl;
import com.logistimo.entities.entity.IKiosk;
import com.logistimo.entities.service.EntitiesService;
import com.logistimo.entities.service.EntitiesServiceImpl;
import com.logistimo.inventory.dao.impl.InvntryDao;
import com.logistimo.inventory.entity.IInvntry;
import com.logistimo.inventory.entity.IInvntryEvntLog;
import com.logistimo.inventory.service.InventoryManagementService;
import com.logistimo.inventory.service.impl.InventoryManagementServiceImpl;
import com.logistimo.logger.XLog;
import com.logistimo.materials.entity.IMaterial;
import com.logistimo.materials.service.MaterialCatalogService;
import com.logistimo.materials.service.impl.MaterialCatalogServiceImpl;
import com.logistimo.orders.entity.IDemandItem;
import com.logistimo.pagination.Results;
import com.logistimo.security.SecureUserDetails;
import com.logistimo.services.ObjectNotFoundException;
import com.logistimo.services.ServiceException;
import com.logistimo.services.Services;
import com.logistimo.utils.CommonUtils;
import com.logistimo.utils.LocalDateUtil;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author charan
 */
public class DemandItemBuilder {

  private static final XLog xLogger = XLog.getLog(DemandItemBuilder.class);

  EntityBuilder entityBuilder = new EntityBuilder();

  public Results buildDemandItems(Results results, SecureUserDetails user)
      throws ServiceException, ObjectNotFoundException {
    return buildDemandItems(results, user, false);
  }

  public Results buildDemandItems(Results results, SecureUserDetails user, boolean skipDuplicates)
      throws ServiceException, ObjectNotFoundException {
    List demandItems = null;
    List<DemandModel> modelItems = null;
    if (results != null) {
      demandItems = results.getResults();
      modelItems = new ArrayList<DemandModel>(demandItems.size());
      MaterialCatalogService mcs = Services.getService(
          MaterialCatalogServiceImpl.class, user.getLocale());
      EntitiesService as = Services.getService(
          EntitiesServiceImpl.class);
      int count = results.getOffset() + 1;
      Set<String> kioskMaterials = new HashSet<String>(demandItems.size());
      Map<Long, String> domainNames = new HashMap<>(1);
      for (Object obj : demandItems) {
        IDemandItem item = (IDemandItem) obj;
        String km = item.getKioskId() + "m:" + item.getMaterialId();
        if (skipDuplicates && kioskMaterials.contains(km)) {
          continue; // This is already processed
        }
        IMaterial m = null;
        try {
          m = mcs.getMaterial(item.getMaterialId());
        } catch (Exception e) {
          xLogger.warn("WARNING: " + e.getClass().getName()
              + " when getting material " + item.getMaterialId()
              + ": " + e.getMessage());
          continue;
        }
        // Add row
        DemandModel model = build(item, m, user, as, domainNames);
        if (model != null) {
          model.sno = count++;
          model.ts =
              LocalDateUtil.format(item.getTimestamp(), user.getLocale(), user.getTimezone());
          modelItems.add(model);
          if (skipDuplicates) {
            kioskMaterials.add(km);
          }
        }
      }
    }
    Results finalResults = new Results(modelItems, results.getCursor(), -1,
        results.getOffset());
    return finalResults;
  }

  /**
   * @param
   * @param user
   * @param as
   * @return
   * @throws Exception
   */
  private DemandModel build(IDemandItem item, IMaterial m,
                            SecureUserDetails user, EntitiesService as,
                            Map<Long, String> domainNames)
      throws ServiceException, ObjectNotFoundException {
    DemandModel itemModel = new DemandModel();
    Long kioskId = item.getKioskId();
    IKiosk k = null;
    try {
      k = as.getKiosk(kioskId);
    } catch (Exception e) {
      xLogger.warn("Error while fetching Kiosk {0}", item.getKioskId());
      return null;
    }
    itemModel.e = entityBuilder.buildBaseModel(k, user.getLocale(),
        user.getTimezone(), "");
    itemModel.nm = m.getName();
    itemModel.oid = item.getOrderId();
    itemModel.c = item.getCurrency();
    itemModel.id = item.getMaterialId();
    itemModel.q = item.getQuantity();
    itemModel.p = item.getFormattedPrice();
    itemModel.tx = item.getTax();
    itemModel.d = item.getDiscount();
    itemModel.a = CommonUtils.getFormattedPrice(item.computeTotalPrice(false));
    itemModel.isBn = m.isBinaryValued();
    itemModel.isBa = m.isBatchEnabled();
    itemModel.oq = item.getOriginalQuantity();
    itemModel.uid = item.getUserId();
    itemModel.stt = item.getStatus();
    itemModel.msg = item.getMessage();
    itemModel.sdid = item.getDomainId();
    String domainName = domainNames.get(item.getDomainId());
    if (domainName == null) {
      IDomain domain = null;
      try {
        DomainsService ds = Services.getService(DomainsServiceImpl.class);
        domain = ds.getDomain(item.getDomainId());
      } catch (Exception e) {
        xLogger.warn("Error while fetching Domain {0}", item.getDomainId());
      }
      if (domain != null) {
        domainName = domain.getName();
      } else {
        domainName = Constants.EMPTY;
      }
      domainNames.put(item.getDomainId(), domainName);
    }
    itemModel.sdname = domainName;

    return itemModel;
  }

  public List<DemandModel> getDemandItems(List<IDemandItem> demandItems, SecureUserDetails sUser)
      throws ServiceException {
    List<DemandModel> demandModelList = new ArrayList<>();
    try {
      MaterialCatalogService mcs = Services.getService(MaterialCatalogServiceImpl.class);
      EntitiesService es = Services.getService(EntitiesServiceImpl.class);
      InventoryManagementService ims = Services.getService(InventoryManagementServiceImpl.class);
      DomainConfig dc = DomainConfig.getInstance(sUser.getDomainId());
      for (IDemandItem item : demandItems) {
        DemandModel model = new DemandModel();
        IMaterial material = mcs.getMaterial(item.getMaterialId());
        model.id = item.getMaterialId();
        model.nm = material.getName();
        IKiosk kiosk = es.getKiosk(item.getKioskId());
        model.e = entityBuilder.buildBaseModel(kiosk, sUser.getLocale(),
            sUser.getTimezone(), "");
        model.oid = item.getOrderId();
        model.rq = item.getRecommendedOrderQuantity();
        model.p = item.getFormattedPrice();
        model.a = CommonUtils.getFormattedPrice(item.computeTotalPrice(false));
        model.q = item.getQuantity();
        model.isBa = material.isBatchEnabled();
        IInvntry inv = ims.getInventory(item.getKioskId(), item.getMaterialId());
        if (inv != null) {
          IInvntryEvntLog lastEventLog = new InvntryDao().getInvntryEvntLog(inv);
          if (lastEventLog != null) {
            model.event = inv.getStockEvent();
          }
          model.csavibper =
              ims.getStockAvailabilityPeriod(inv, dc).setScale(1, BigDecimal.ROUND_HALF_UP);
          model.crFreq =
              InventoryConfig.getFrequencyDisplay(dc.getInventoryConfig().getDisplayCRFreq(), false,
                  sUser.getLocale());
        }
        demandModelList.add(model);
      }
    } catch (Exception e) {
      xLogger.warn("Error while fetching demand items in domain: {0}", sUser.getDomainId(), e);
      throw new ServiceException(e);
    }

    return demandModelList;
  }
}

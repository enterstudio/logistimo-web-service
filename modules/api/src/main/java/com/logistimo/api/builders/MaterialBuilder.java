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

import com.logistimo.api.models.MaterialModel;
import com.logistimo.config.models.DomainConfig;
import com.logistimo.config.models.InventoryConfig;
import com.logistimo.constants.Constants;
import com.logistimo.dao.JDOUtils;
import com.logistimo.domains.entity.IDomain;
import com.logistimo.domains.service.DomainsService;
import com.logistimo.domains.service.impl.DomainsServiceImpl;
import com.logistimo.entities.entity.IKiosk;
import com.logistimo.inventory.entity.IInvntry;
import com.logistimo.logger.XLog;
import com.logistimo.materials.entity.IHandlingUnit;
import com.logistimo.materials.entity.IMaterial;
import com.logistimo.materials.service.IHandlingUnitService;
import com.logistimo.materials.service.impl.HandlingUnitServiceImpl;
import com.logistimo.pagination.Results;
import com.logistimo.security.SecureUserDetails;
import com.logistimo.services.ObjectNotFoundException;
import com.logistimo.services.ServiceException;
import com.logistimo.services.Services;
import com.logistimo.users.entity.IUserAccount;
import com.logistimo.users.service.UsersService;
import com.logistimo.users.service.impl.UsersServiceImpl;
import com.logistimo.utils.BigUtil;
import com.logistimo.utils.LocalDateUtil;

import org.apache.commons.lang.StringUtils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MaterialBuilder {
  private static final XLog xLogger = XLog.getLog(MaterialBuilder.class);

  public Results buildMaterialModelListWithEntity(Results results, SecureUserDetails sUser,
                                                  Results eResults, Long domainId, IKiosk k) {
    List<IMaterial> materials = results.getResults();
    List<MaterialModel> newInventory = new ArrayList<>(materials.size());
    Map<Long, String> domainNames = new HashMap<>(1);
    int itemCount = results.getOffset() + 1;
    List<IInvntry> invntries = eResults.getResults();
    DomainConfig domainConfig = DomainConfig.getInstance(domainId);
    InventoryConfig ic = domainConfig.getInventoryConfig();
    boolean allowManualConsumptionRates = (ic != null && ic.getManualCRFreq() != null);
    for (IMaterial material : materials) {
      MaterialModel item = buildMaterialModel(material, sUser, itemCount, domainNames);
      if (item != null) {
        IInvntry inv = getInventory(item.mId, invntries);
        item.isAdded = inv != null;
        if (item.isAdded) {
          item.reord = inv.getReorderLevel();
          item.max = inv.getMaxStock();
          item.minDur = inv.getMinDuration();
          item.maxDur = inv.getMaxDuration();
          if (allowManualConsumptionRates) {
            item.crMnl = inv.getConsumptionRateManual();
          }
          item.rp =
              String.valueOf(
                  BigUtil.equalsZero(inv.getRetailerPrice()) ? material.getRetailerPrice()
                      : inv.getRetailerPrice());
          item.tx =
              BigUtil.notEqualsZero(inv.getTax()) ? inv.getTax()
                  : k != null ? k.getTax() : BigDecimal.ZERO;
          if (k != null && k.isOptimizationOn()) {
            item.im = inv.getInventoryModel();
            item.sl = inv.getServiceLevel();
          }
        }
        newInventory.add(item);
        itemCount++;
      }
    }
    return new Results(newInventory, results.getCursor(), results.getNumFound(),
        results.getOffset());
  }

  private IInvntry getInventory(Long mId, List<IInvntry> invntries) {
    for (IInvntry invntry : invntries) {
      if (invntry.getMaterialId().equals(mId)) {
        return invntry;
      }
    }
    return null;
  }

  public Results buildMaterialModelList(Results results,
                                        SecureUserDetails sUser, Long domainId)
      throws ServiceException {
    List materials = results.getResults();
    List<MaterialModel> newInventory = new ArrayList<MaterialModel>(
        materials.size());
    int itemCount = results.getOffset() + 1;
    Map<Long, String> domainNames = new HashMap<>(1);
    UsersService as = null;
    as = Services.getService(UsersServiceImpl.class);
    for (Object material : materials) {
      MaterialModel item = buildMaterialModel((IMaterial) material,
          sUser, itemCount, domainNames);
      if (item != null) {
        if (as != null) {
          try {
            IUserAccount ua;
            if (StringUtils.isNotBlank(item.ludBy)) {
              ua = as.getUserAccount(item.ludBy);
              item.ludByn = ua.getFullName();
            } else if (StringUtils.isNotBlank(item.creBy)) {
              ua = as.getUserAccount(item.creBy);
              item.ludBy = ua.getUserId();
              item.ludByn = ua.getFullName();
            }
          } catch (ObjectNotFoundException e) {
            xLogger.warn("User {0} not found", item.ludBy);
          }
        }
        newInventory.add(item);
        itemCount++;
      }
    }
    return new Results(newInventory, results.getCursor(), results.getNumFound(),
        results.getOffset());
  }

  public MaterialModel buildMaterialModel(IMaterial material, IUserAccount cb, IUserAccount ub,
                                          SecureUserDetails sUser, int itemCount,
                                          Map<Long, String> domainNames) {
    MaterialModel model = buildMaterialModel(material, sUser, itemCount, domainNames);

    if (cb != null) {
      model.creByn = cb.getFullName();
    }
    if (ub != null) {
      model.ludByn = ub.getFullName();
    }
    return model;

  }

  public MaterialModel buildMaterialModel(IMaterial material,
                                          SecureUserDetails sUser, int itemCount,
                                          Map<Long, String> domainNames) {
    DomainsService ds = Services.getService(DomainsServiceImpl.class);
    MaterialModel model = new MaterialModel();
    model.sno = itemCount;
    model.mId = material.getMaterialId();
    model.dId = material.getDomainId();
    model.mnm = material.getName();
    model.snm = material.getShortName();
    model.cId = material.getCustomId();
    model.dsc = material.getDescription();
    model.info = material.getInfo();
    model.dispInfo = material.displayInfo() ? "yes" : "no";
    model.tgs = material.getTags();
    model.rp = BigUtil.getFormattedValue(material.getRetailerPrice());
    model.msrp = BigUtil.getFormattedValue(material.getMSRP());
    if (material.getLastUpdated() == null) {
      model.t =
          LocalDateUtil.format(material.getTimeStamp(), sUser.getLocale(), sUser.getTimezone());
    } else {
      model.t =
          LocalDateUtil.format(material.getLastUpdated(), sUser.getLocale(), sUser.getTimezone());
    }
    model.b = material.isBatchEnabled() ? "yes" : "no";
    model.ib = material.isBinaryValued() ? "yes" : "no";
    model.dty = material.getType();
    model.snl = material.isSeasonal() ? "yes" : "no";
    model.cur = material.getCurrency();
    model.tm = material.isTemperatureSensitive() ? "yes" : "no";
    model.tmin = Float.toString(material.getTemperatureMin());
    model.tmax = Float.toString(material.getTemperatureMax());
    model.sdid = material.getDomainId();
    model.creBy = material.getCreatedBy();
    model.ludBy = material.getLastUpdatedBy();
    model.creOn =
        LocalDateUtil.format(material.getTimeStamp(), sUser.getLocale(), sUser.getTimezone());
    String domainName = domainNames.get(material.getDomainId());
    if (domainName == null) {
      IDomain domain = null;
      try {
        domain = ds.getDomain(material.getDomainId());
      } catch (Exception e) {
        xLogger.warn("Error while fetching Domain {0}", material.getDomainId());
      }
      if (domain != null) {
        domainName = domain.getName();
      } else {
        domainName = Constants.EMPTY;
      }
      domainNames.put(material.getDomainId(), domainName);
    }
    try {
      IHandlingUnitService hus = Services.getService(HandlingUnitServiceImpl.class);
      Map<String, String> hu = hus.getHandlingUnitDataByMaterialId(material.getMaterialId());
      if (hu != null) {
        model.huId = Long.valueOf(hu.get(IHandlingUnit.HUID));
        model.huQty = new BigDecimal(hu.get(IHandlingUnit.QUANTITY));
        model.huName = hu.get(IHandlingUnit.NAME);
      }
    } catch (Exception e) {
      xLogger.warn("Error while fetching handling unit {0}", material.getMaterialId(), e);
    }
    model.sdname = domainName;
    return model;
  }

  /*
  * Builds complete material object
  * */
  public IMaterial buildMaterial(MaterialModel model) {
    IMaterial m = JDOUtils.createInstance(IMaterial.class);
    m.setName(model.mnm);
    m.setMaterialId(model.mId);
    m.setDomainId(model.dId);
    m.setShortName(model.snm);
    m.setCustomId(model.cId);
    m.setDescription(model.dsc);
    m.setInfo(model.info);
    m.setInfoDisplay(Boolean.parseBoolean(toBoolean(model.dispInfo)));
    m.setTags(model.tgs);
    m.setBatchEnabled(Boolean.parseBoolean(toBoolean(model.b)));
    m.setBatchEnabledOnMobile(m.isBatchEnabled());
    if ("yes".equalsIgnoreCase(model.dty)) {
      m.setType(IMaterial.TYPE_BINARY);
    }
    m.setSeasonal(Boolean.parseBoolean(toBoolean(model.snl)));
    if (StringUtils.isNotBlank(model.msrp)) {
      m.setMSRP(new BigDecimal(model.msrp));
    }
    if (StringUtils.isNotBlank(model.rp)) {
      m.setRetailerPrice(new BigDecimal(model.rp));
    }
    m.setCurrency(model.cur);
    boolean isTempEnable = Boolean.parseBoolean(toBoolean(model.tm));
    m.setTemperatureSensitive(isTempEnable);
    if (isTempEnable && model.tmax != null) {
      m.setTemperatureMax(Float.parseFloat(model.tmax));
    }
    if (isTempEnable && model.tmin != null) {
      m.setTemperatureMin(Float.parseFloat(model.tmin));
    }

    return m;
  }

  public String toBoolean(String s) {
    if (StringUtils.isNotEmpty(s)) {
      if (s.equalsIgnoreCase("yes")) {
        s = "true";
      } else {
        s = "false";
      }
    }
    return s;
  }
}

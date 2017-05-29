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

import com.logistimo.dao.JDOUtils;

import com.logistimo.pagination.Results;
import com.logistimo.security.SecureUserDetails;
import com.logistimo.services.ServiceException;
import com.logistimo.services.Services;
import com.logistimo.utils.LocalDateUtil;
import com.logistimo.api.models.HUModel;
import com.logistimo.domains.service.DomainsService;
import com.logistimo.domains.service.impl.DomainsServiceImpl;
import com.logistimo.materials.entity.IHandlingUnit;
import com.logistimo.materials.entity.IHandlingUnitContent;
import com.logistimo.users.entity.IUserAccount;
import com.logistimo.users.service.UsersService;
import com.logistimo.users.service.impl.UsersServiceImpl;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class HUBuilder {

  private HUContentBuilder cBuilder = new HUContentBuilder();

  public IHandlingUnit buildHandlingUnit(HUModel model) {
    IHandlingUnit hu = JDOUtils.createInstance(IHandlingUnit.class);
    hu.setName(model.name);
    hu.setDescription(model.description);
    hu.setId(model.id);
    if (model.contents != null) {
      hu.setContents(cBuilder.buildHUContentSet(model.contents));
    }
    return hu;
  }

  public HUModel buildHUModel(IHandlingUnit hu, IUserAccount cb, IUserAccount ub,
                              SecureUserDetails sUser) {
    HUModel model = buildHUModel(hu, sUser);
    if (cb != null) {
      model.cbName = cb.getFullName();
    }
    if (ub != null) {
      model.ubName = ub.getFullName();
    }
    return model;
  }

  public HUModel buildHUModel(IHandlingUnit hu, SecureUserDetails sUser) {
    HUModel model = new HUModel();
    model.id = hu.getId();
    model.name = hu.getName();
    model.description = hu.getDescription();
    model.timeStamp =
        LocalDateUtil.format(hu.getTimeStamp(), sUser.getLocale(), sUser.getTimezone());
    model.ub = hu.getUpdatedBy();
    model.cb = hu.getCreatedBy();
    model.lastUpdated =
        LocalDateUtil.format(hu.getLastUpdated(), sUser.getLocale(), sUser.getTimezone());
    model.sdId = hu.getDomainId();
    try {
      DomainsService ds = Services.getService(DomainsServiceImpl.class);
      model.sdname = ds.getDomain(model.sdId).getName();
      UsersService as = Services.getService(UsersServiceImpl.class);
      IUserAccount ua = as.getUserAccount(model.ub);
      model.ubName = ua.getFullName();
      ua = as.getUserAccount(model.cb);
      model.cbName = ua.getFullName();
    } catch (Exception ignored) {
      // ignore
    }
    model.contents = cBuilder.buildHUContentModelList((Set<IHandlingUnitContent>) hu.getContents());
    return model;
  }

  public Results buildHandlingUnitModelList(Results results, SecureUserDetails sUser)
      throws ServiceException {
    List huList = results.getResults();
    List<HUModel> newInventory = new ArrayList<>(huList.size());
    for (Object material : huList) {
      HUModel item = buildHUModel((IHandlingUnit) material, sUser);
      if (item != null) {
        newInventory.add(item);
      }
    }
    return new Results(newInventory, results.getCursor(), results.getNumFound(),
        results.getOffset());
  }
}

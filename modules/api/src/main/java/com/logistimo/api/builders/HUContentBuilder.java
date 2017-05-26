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

import com.logistimo.services.Services;
import com.logistimo.logger.XLog;
import com.logistimo.api.models.HUContentModel;
import com.logistimo.materials.entity.IHandlingUnitContent;
import com.logistimo.materials.entity.IMaterial;
import com.logistimo.materials.service.MaterialCatalogService;
import com.logistimo.materials.service.impl.MaterialCatalogServiceImpl;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by yuvaraj on 16/07/16.
 */
public class HUContentBuilder {
  private static final XLog xLogger = XLog.getLog(EntityBuilder.class);

  public Set<IHandlingUnitContent> buildHUContentSet(List<HUContentModel> lModel) {
    Set<IHandlingUnitContent> items = new HashSet<>(lModel.size());
    for (HUContentModel cModel : lModel) {
      items.add(buildHUContent(cModel));
    }
    return items;
  }

  public IHandlingUnitContent buildHUContent(HUContentModel model) {
    IHandlingUnitContent huc = JDOUtils.createInstance(IHandlingUnitContent.class);
    huc.setCntId(model.cntId);
    huc.setDomainId(model.dId);
    huc.setId(model.id);
    huc.setQuantity(model.quantity);
    huc.setTy(model.ty);
    return huc;
  }

  public List<HUContentModel> buildHUContentModelList(Set<IHandlingUnitContent> huc) {
    List<HUContentModel> huModel = new ArrayList<>(huc.size());
    for (IHandlingUnitContent ihu : huc) {
      huModel.add(buildHUContentModel(ihu));
    }
    return huModel;
  }

  public HUContentModel buildHUContentModel(IHandlingUnitContent huc) {
    HUContentModel cm = new HUContentModel();
    cm.dId = huc.getDomainId();
    cm.id = huc.getId();
    cm.quantity = huc.getQuantity();
    cm.ty = huc.getTy();
    cm.cntId = huc.getCntId();
    try {
      MaterialCatalogService
          materialCatalogService =
          Services.getService(MaterialCatalogServiceImpl.class);
      IMaterial m = materialCatalogService.getMaterial(huc.getCntId());
      cm.cntName = m.getName();
    } catch (Exception e) {
      xLogger.warn("Error when getting material name of {0} ", huc.getCntId(), e);
    }
    return cm;
  }
}

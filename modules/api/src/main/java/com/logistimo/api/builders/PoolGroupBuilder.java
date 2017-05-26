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

import org.apache.commons.lang.StringUtils;
import com.logistimo.services.Services;
import com.logistimo.utils.LocalDateUtil;
import com.logistimo.api.models.EntityGroupModel;
import com.logistimo.api.models.EntityModel;
import com.logistimo.entities.entity.IKiosk;
import com.logistimo.entities.entity.IPoolGroup;
import com.logistimo.users.entity.IUserAccount;
import com.logistimo.users.service.UsersService;
import com.logistimo.users.service.impl.UsersServiceImpl;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Created by naveensnair on 30/01/15.
 */
public class PoolGroupBuilder {

  public EntityGroupModel buildPoolGroupModel(IPoolGroup pg, Locale locale, String timeZone) {
    EntityGroupModel model = new EntityGroupModel();
    EntityBuilder entityBuilder = new EntityBuilder();
    List<EntityModel> entityModelList = new ArrayList<EntityModel>();
    if (pg != null) {
      if (StringUtils.isNotEmpty(pg.getName())) {
        model.nm = pg.getName();
      }
      if (StringUtils.isNotEmpty(pg.getDescription())) {
        model.dsc = pg.getDescription();
      }
      if (pg.getGroupId() != null) {
        model.id = pg.getGroupId();
      }

      if (StringUtils.isNotEmpty(pg.getCountry())) {
        model.cnt = pg.getCountry();
      }
      if (StringUtils.isNotEmpty(pg.getState())) {
        model.st = pg.getState();
      }
      if (StringUtils.isNotEmpty(pg.getCity())) {
        model.ct = pg.getCity();
      }
      if (StringUtils.isNotEmpty(pg.getDistrict())) {
        model.dt = pg.getDistrict();
      }
      if (StringUtils.isNotEmpty(pg.getTaluk())) {
        model.tlk = pg.getTaluk();
      }
      if (StringUtils.isNotEmpty(pg.getStreet())) {
        model.str = pg.getStreet();
      }
      if (pg.getKiosks() != null) {
        for (IKiosk k : pg.getKiosks()) {
          entityModelList.add(entityBuilder.buildModel(k, locale, timeZone));
        }
        if (entityModelList.size() > 0) {
          model.ent = entityModelList;
        }
      }
      if (StringUtils.isNotEmpty(pg.getOwnerId())) {
        model.uid = pg.getOwnerId();
        try {
          UsersService accountsService = Services.getService(UsersServiceImpl.class);
          IUserAccount account = accountsService.getUserAccount(model.uid);
          model.unm = account.getFullName();
        } catch (Exception e) {
          // ignore
        }
      }

      if (pg.getCreatedBy() != null) {
        try {
          UsersService as = Services.getService(UsersServiceImpl.class);
          IUserAccount cb = as.getUserAccount(pg.getCreatedBy());
          model.creByn = cb.getFullName();
          model.creBy = pg.getCreatedBy();
        } catch (Exception e) {
          // ignore
        }
      }
      if (pg.getUpdatedBy() != null) {
        try {
          UsersService as = Services.getService(UsersServiceImpl.class);
          IUserAccount ub = as.getUserAccount(pg.getUpdatedBy());
          model.updByn = ub.getFullName();
          model.updBy = pg.getUpdatedBy();
        } catch (Exception e) {
          // ignore
        }
      }

      if (pg.getUpdatedOn() != null) {
        model.updOn = LocalDateUtil.format(pg.getUpdatedOn(), locale, timeZone);
      }
      if (pg.getTimeStamp() != null) {
        model.t = pg.getTimeStamp();
        model.creOn = LocalDateUtil.format(pg.getTimeStamp(), locale, timeZone);
      }
    }
    return model;
  }

  /**
   * Builds simple Entity group model, to be used for display only.
   */
  public List<EntityGroupModel> buildPoolGroupModels(List<? extends IPoolGroup> poolGroups) {
    if (poolGroups != null && !poolGroups.isEmpty()) {
      List<EntityGroupModel> models = new ArrayList<EntityGroupModel>(poolGroups.size());
      for (IPoolGroup poolGroup : poolGroups) {
        EntityGroupModel model = new EntityGroupModel();
        model.nm = poolGroup.getName();
        model.gid = poolGroup.getGroupId();
        models.add(model);
      }
      return models;
    }
    return null;
  }
}

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

import com.logistimo.pagination.Results;
import com.logistimo.services.ServiceException;
import com.logistimo.services.Services;
import com.logistimo.utils.BigUtil;
import com.logistimo.utils.LocalDateUtil;
import com.logistimo.utils.NumberUtil;
import com.logistimo.api.models.UploadDataViewModel;
import com.logistimo.entities.entity.IKiosk;
import com.logistimo.entities.service.EntitiesService;
import com.logistimo.entities.service.EntitiesServiceImpl;
import com.logistimo.materials.entity.IMaterial;
import com.logistimo.materials.service.MaterialCatalogService;
import com.logistimo.materials.service.impl.MaterialCatalogServiceImpl;
import com.logistimo.mnltransactions.entity.IMnlTransaction;
import com.logistimo.users.entity.IUserAccount;
import com.logistimo.users.service.UsersService;
import com.logistimo.users.service.impl.UsersServiceImpl;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Created by mohan raja on 22/01/15
 */
public class UploadDataViewBuilder {

  public List<UploadDataViewModel> build(Results results, Locale locale, Long eid, String timezone)
      throws ServiceException {
    int size = results.getSize();
    if (size > 0) {
      List<IMnlTransaction> manUpTransactions = results.getResults();
      List<UploadDataViewModel> models = new ArrayList<UploadDataViewModel>(size);
      MaterialCatalogService
          mc =
          Services.getService(MaterialCatalogServiceImpl.class, locale);
      EntitiesService as = Services.getService(EntitiesServiceImpl.class, locale);
      UsersService usersService = Services.getService(UsersServiceImpl.class, locale);
      IKiosk k = null;
      if (eid != null) {
        k = as.getKiosk(eid);
      }
      for (IMnlTransaction manUpTrans : manUpTransactions) {
        UploadDataViewModel model = new UploadDataViewModel();
        try {
          IMaterial m = mc.getMaterial(manUpTrans.getMaterialId());
          model.mnm = m.getName();
          model.mid = m.getMaterialId();
        } catch (ServiceException ignored) {
          // ignore
        }
        IKiosk kiosk = k;
        try {
          if (k == null) {
            kiosk = as.getKiosk(manUpTrans.getKioskId(), false);
          }
        } catch (Exception e) {
          continue;
        }
        IKiosk vendor = null;
        try {
          if (manUpTrans.getVendorId() != null) {
            vendor = as.getKiosk(manUpTrans.getVendorId(), false);
          }
        } catch (Exception e) {
          continue;
        }
        IUserAccount u = null;
        try {
          if (manUpTrans.getUserId() != null) {
            u = usersService.getUserAccount(manUpTrans.getUserId());
          }
        } catch (Exception e) {
          continue;
        }
        if (kiosk != null) {
          model.enm = kiosk.getName();
          model.eid = kiosk.getKioskId();
        }
        model.cst = BigUtil.getFormattedValue(manUpTrans.getClosingStock());
        model.ost = BigUtil.getFormattedValue(manUpTrans.getOpeningStock());
        model.rQty = BigUtil.getFormattedValue(manUpTrans.getReceiptQuantity());
        model.iQty = BigUtil.getFormattedValue(manUpTrans.getIssueQuantity());
        model.dQty = BigUtil.getFormattedValue(manUpTrans.getDiscardQuantity());
        model.stodur = NumberUtil.getFormattedValue((float) manUpTrans.getStockoutDuration());
//                model.noSto = NumberUtil.getFormattedValue((float) manUpTrans.getNumberOfStockoutInstances());
        if (BigUtil.notEquals(manUpTrans.getManualConsumptionRate(),
            manUpTrans.getComputedConsumptionRate())) {
          model.mcrc = "red";
        }
        model.mcr = BigUtil.getFormattedValue(manUpTrans.getManualConsumptionRate());
        model.ccr = BigUtil.getFormattedValue(manUpTrans.getComputedConsumptionRate());

        if (BigUtil.notEquals(manUpTrans.getOrderedQuantity(), manUpTrans.getFulfilledQuantity())) {
          model.moqc = "red";
        }
        model.moq = BigUtil.getFormattedValue(manUpTrans.getOrderedQuantity());
        model.coq = BigUtil.getFormattedValue(manUpTrans.getFulfilledQuantity());
        if (manUpTrans.getTags() != null && !manUpTrans.getTags().isEmpty()) {
          model.tag = manUpTrans.getTags();
        }
        if (vendor != null) {
          model.ven = vendor.getName();
        }
        if (manUpTrans.getReportingPeriod() != null) {
          model.repPer =
              LocalDateUtil.format(manUpTrans.getReportingPeriod(), locale, timezone, true);
        }
        if (u != null) {
          model.upBy = u.getFullName();
        }
        model.upTm = LocalDateUtil.format(manUpTrans.getTimestamp(), locale, timezone);
        models.add(model);
      }
      return models;
    }
    return null;
  }
}

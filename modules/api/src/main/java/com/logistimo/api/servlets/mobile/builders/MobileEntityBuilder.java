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

package com.logistimo.api.servlets.mobile.builders;

import com.logistimo.entities.entity.IApprovers;
import com.logistimo.proto.MobileApproversModel;
import com.logistimo.proto.MobileEntityApproversModel;
import com.logistimo.services.Services;
import com.logistimo.users.service.UsersService;
import com.logistimo.users.service.impl.UsersServiceImpl;

import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by vani on 28/06/17.
 */
public class MobileEntityBuilder {

  /**
   * Builds a list of approver models for an entity as required by the mobile from a list of IApporver objects
   */
  public MobileEntityApproversModel buildApproversModel(List<IApprovers> approversList) {
    if (approversList == null || approversList.isEmpty()) {
      return null;
    }
    List<String> pap = new ArrayList<>();
    List<String> sap = new ArrayList<>();
    List<String> pas = new ArrayList<>();
    List<String> sas = new ArrayList<>();
    for (IApprovers apr : approversList) {
      if (StringUtils.isNotEmpty(apr.getUserId())) {
        if (apr.getType().equals(IApprovers.PRIMARY_APPROVER)) {
          if (apr.getOrderType().equals(IApprovers.PURCHASE_ORDER)) {
            pap.add(apr.getUserId());
          } else {
            pas.add(apr.getUserId());
          }
        } else {
          if (apr.getOrderType().equals(IApprovers.PURCHASE_ORDER)) {
            sap.add(apr.getUserId());
          } else {
            sas.add(apr.getUserId());
          }
        }
      }
    }
    if (pap.isEmpty() && pas.isEmpty() && sap.isEmpty() && sas.isEmpty()) {
      return null;
    }
    MobileEntityApproversModel mobileEntityApproversModel = new MobileEntityApproversModel();
    MobileUserBuilder mobileUserBuilder = new MobileUserBuilder();
    UsersService us = Services.getService(UsersServiceImpl.class);
    if (!pap.isEmpty() || !sap.isEmpty()) {
      MobileApproversModel mobileApproversModel = new MobileApproversModel();
      mobileApproversModel.prm =
          mobileUserBuilder.buildMobileUserModels(mobileUserBuilder.constructUserAccount(
              us, pap));
      mobileApproversModel.scn =
          mobileUserBuilder.buildMobileUserModels(mobileUserBuilder.constructUserAccount(
              us, sap));
      mobileEntityApproversModel.prc = mobileApproversModel;
    }
    if (!pas.isEmpty() || !sas.isEmpty()) {
      MobileApproversModel mobileApproversModel = new MobileApproversModel();
      mobileApproversModel.prm =
          mobileUserBuilder.buildMobileUserModels(mobileUserBuilder.constructUserAccount(
              us, pas));
      mobileApproversModel.scn =
          mobileUserBuilder.buildMobileUserModels(mobileUserBuilder.constructUserAccount(us, sas));
      mobileEntityApproversModel.sle = mobileApproversModel;
    }
    return mobileEntityApproversModel;
  }
}

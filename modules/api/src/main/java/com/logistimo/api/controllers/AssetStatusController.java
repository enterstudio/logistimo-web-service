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

package com.logistimo.api.controllers;

import com.google.gson.Gson;

import com.logistimo.api.models.AssetStatusRequest;
import com.logistimo.assets.entity.IAsset;
import com.logistimo.assets.entity.IAssetAttribute;
import com.logistimo.assets.entity.IAssetStatus;
import com.logistimo.assets.models.AssetStatusModel;
import com.logistimo.assets.service.AssetManagementService;
import com.logistimo.assets.service.impl.AssetManagementServiceImpl;
import com.logistimo.auth.utils.SecurityUtils;
import com.logistimo.constants.Constants;
import com.logistimo.dao.JDOUtils;
import com.logistimo.entities.service.EntitiesService;
import com.logistimo.entities.service.EntitiesServiceImpl;
import com.logistimo.exception.UnauthorizedException;
import com.logistimo.logger.XLog;
import com.logistimo.services.ServiceException;
import com.logistimo.services.Services;

import org.apache.commons.io.IOUtils;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

/**
 * Created by charan on 27/11/15.
 */
@Controller
@RequestMapping("/assetstatus")
public class AssetStatusController {

  private static final XLog xLogger = XLog.getLog(AssetStatusController.class);

  @RequestMapping(value = "/", method = RequestMethod.POST)
  public
  @ResponseBody
  String postAssetStatus(HttpServletRequest request) throws IOException, ServiceException {
    String jsonBody = IOUtils.toString(request.getInputStream());
    String signature = request.getHeader(Constants.TEMPSERVICE_SIGNATURE_HEADER);
    try {
      if (SecurityUtils.verifyAssetServiceRequest(signature, jsonBody)) {
        AssetStatusRequest assetStatusRequest = new Gson().fromJson(jsonBody, AssetStatusRequest.class);
        List<AssetStatusModel> assetStatusModelList = assetStatusRequest.data;
        if (assetStatusModelList != null && !assetStatusModelList.isEmpty()) {
          AssetManagementService ams = Services.getService(AssetManagementServiceImpl.class);
          ams.updateAssetStatus(build(assetStatusModelList, ams));
        }
      } else {
        throw new UnauthorizedException("Unauthorized request", HttpStatus.UNAUTHORIZED);
      }
    } catch (ServiceException e) {
      xLogger.severe("Failed to authenticate asset status request", e);
      throw e;
    }
    return "success";
  }


  private List<IAssetStatus> build(List<AssetStatusModel> assetStatusModelList,
                                   AssetManagementService ams) throws ServiceException {
    List<IAssetStatus> assetStatusList = new ArrayList<>(assetStatusModelList.size());
    for (AssetStatusModel assetStatusModel : assetStatusModelList) {
      IAssetStatus assetStatus = JDOUtils.createInstance(IAssetStatus.class);
      IAsset asset = ams.getAsset(assetStatusModel.vId, assetStatusModel.dId);
      if (asset == null) {
        xLogger.warn("Asset with vendor: {0} and device Id: {1}", assetStatusModel.vId,
            assetStatusModel.dId);
        continue;
      }
      EntitiesService entitiesService = Services.getService(EntitiesServiceImpl.class);
      List<String> tags = null;
      if(asset.getKioskId() != null) {
        tags = entitiesService.getKiosk(asset.getKioskId()).getTags();
      }
      assetStatus
          .init(asset.getId(), assetStatusModel.mpId, assetStatusModel.sId, assetStatusModel.type,
              assetStatusModel.st, assetStatusModel.aSt, assetStatusModel.tmp,
              assetStatusModel.time, buildAttributes(assetStatusModel.attrs), tags);
      assetStatusList.add(assetStatus);
    }
    return assetStatusList;
  }

  private List<IAssetAttribute> buildAttributes(Map<String, String> attrs) {
    List<IAssetAttribute> assetAttributes = new ArrayList<>(1);
    if (attrs != null) {
      for (String key : attrs.keySet()) {
        assetAttributes
            .add(JDOUtils.createInstance(IAssetAttribute.class).init(key, attrs.get(key)));
      }
    }
    return assetAttributes;
  }


}

package com.logistimo.api.controllers;

import com.google.gson.Gson;

import com.logistimo.api.models.AssetStatusRequest;
import com.logistimo.assets.entity.IAsset;
import com.logistimo.assets.entity.IAssetAttribute;
import com.logistimo.assets.entity.IAssetStatus;
import com.logistimo.assets.service.AssetManagementService;
import com.logistimo.assets.service.impl.AssetManagementServiceImpl;
import com.logistimo.dao.JDOUtils;

import org.apache.commons.io.IOUtils;
import com.logistimo.services.ServiceException;
import com.logistimo.services.Services;
import com.logistimo.constants.Constants;
import com.logistimo.logger.XLog;

import com.logistimo.entities.service.EntitiesService;
import com.logistimo.entities.service.EntitiesServiceImpl;
import com.logistimo.exception.UnauthorizedException;
import com.logistimo.assets.models.AssetStatusModel;
import com.logistimo.api.util.SecurityUtils;
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

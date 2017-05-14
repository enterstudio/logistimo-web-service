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

package com.logistimo.assets.utils;

import com.logistimo.assets.models.AssetStatusModel;
import com.logistimo.assets.models.AssetStatusRequest;
import com.logistimo.assets.service.AssetManagementService;
import com.logistimo.assets.service.impl.AssetManagementServiceImpl;
import com.logistimo.logger.XLog;
import com.logistimo.services.ServiceException;
import com.logistimo.services.Services;
import com.codahale.metrics.Meter;
import com.logistimo.dao.JDOUtils;
import com.logistimo.utils.MetricsUtil;
import org.apache.camel.Handler;
import com.logistimo.assets.entity.IAssetStatus;
import com.logistimo.assets.entity.IAsset;
import com.logistimo.assets.entity.IAssetAttribute;


import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by nitisha.khandelwal on 17/03/17.
 */

public class AssetAlarmsMessagingProcessor {

  private static Meter jmsMeter = MetricsUtil
      .getMeter(AssetAlarmsMessagingProcessor.class, "assetAlarmsMeter");

  private static final XLog xLogger = XLog.getLog(AssetAlarmsMessagingProcessor.class);

  @Handler
  public void execute(AssetStatusRequest assetStatusRequest) throws ServiceException {

    jmsMeter.mark();

    List<AssetStatusModel> assetStatusModelList = assetStatusRequest.data;
    if (assetStatusModelList != null && !assetStatusModelList.isEmpty()) {
      AssetManagementService assetManagementService = Services
          .getService(AssetManagementServiceImpl.class);
      assetManagementService
          .updateAssetStatus(build(assetStatusModelList, assetManagementService));

    }

  }

  private List<IAssetStatus> build(List<AssetStatusModel> assetStatusModelList,
      AssetManagementService assetManagementService) throws ServiceException {
    List<IAssetStatus> assetStatusList = new ArrayList<>(assetStatusModelList.size());
    for (AssetStatusModel assetStatusModel : assetStatusModelList) {
      IAsset asset = assetManagementService.getAsset(assetStatusModel.vId, assetStatusModel.dId);
      if (asset == null) {
        xLogger.warn("Asset with vendor: {0} and device Id: {1} not found.", assetStatusModel.vId,
            assetStatusModel.dId);
        continue;
      }
      IAssetStatus assetStatus = JDOUtils.createInstance(IAssetStatus.class);
      assetStatus
          .init(asset.getId(), assetStatusModel.mpId, assetStatusModel.sId, assetStatusModel.type,
              assetStatusModel.st, assetStatusModel.aSt, assetStatusModel.tmp,
              assetStatusModel.time, buildAttributes(assetStatusModel.attrs), null);
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

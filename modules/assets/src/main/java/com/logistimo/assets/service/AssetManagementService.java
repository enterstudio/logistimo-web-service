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

package com.logistimo.assets.service;

import com.logistimo.assets.entity.IAsset;
import com.logistimo.assets.entity.IAssetRelation;
import com.logistimo.assets.entity.IAssetStatus;

import com.logistimo.assets.models.AssetModel;
import com.logistimo.pagination.PageParams;
import com.logistimo.pagination.Results;
import com.logistimo.services.Service;
import com.logistimo.services.ServiceException;

import java.util.List;
import java.util.Map;

/**
 * Created by kaniyarasu on 02/11/15.
 */
public interface AssetManagementService extends Service {

  void createAsset(Long domainId, IAsset asset, AssetModel assetModel) throws ServiceException;

  void updateAsset(Long domainId, IAsset asset, AssetModel assetModel) throws ServiceException;

  IAsset getAsset(Long assetId) throws ServiceException;

  Results getAssetsByDomain(Long domainId, Integer assetType, PageParams pageParams)
      throws ServiceException;

  List<IAsset> getAssetsByKiosk(Long kioskId) throws ServiceException;

  /**
   * Get Assets by kiosk and type.
   *
   * @param kioskId   Entity id. Required
   * @param assetType Type of the asset. Required.
   */
  List<IAsset> getAssetsByKiosk(Long kioskId, Integer assetType) throws ServiceException;

  IAsset getAsset(String manufacturerId, String assetId) throws ServiceException;

  /**
   * Update asset status and generate events to trigger notifications.
   */
  void updateAssetStatus(List<IAssetStatus> assetStatusModelList) throws ServiceException;

  void deleteAsset(String manufacturerId, List<String> serialIds, Long domainId) throws ServiceException;

  /**
   * Create asset relation
   */
  IAssetRelation createOrUpdateAssetRelation(Long domainId, IAssetRelation assetRelation)
      throws ServiceException;

  void deleteAssetRelation(Long assetId, Long domainId, IAsset asset) throws ServiceException;

  void deleteAssetRelationByRelatedAsset(Long relatedAssetId) throws ServiceException;

  IAssetRelation getAssetRelationByRelatedAsset(Long relatedAssetId) throws ServiceException;

  /**
   * Get the asset relation for given asset
   */
  IAssetRelation getAssetRelationByAsset(Long assetId) throws ServiceException;

  List<IAsset> getAssets(Long domainId, Long kId, String q, String assetType, Boolean all)
      throws ServiceException;

  Map<String, Integer> getTemperatureStatus(Long entityId);

  List<String> getModelSuggestion(Long domainId, String term);

  String getMonitoredAssetIdsForReport(Map<String,String> filters);

  String getVendorIdsForReports(String did);

  String getAssetTypesForReports(String did,String exclude);

}

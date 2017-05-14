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

/**
 *
 */
package com.logistimo.materials.service;

import com.logistimo.materials.entity.IMaterial;

import com.logistimo.pagination.PageParams;
import com.logistimo.pagination.Results;
import com.logistimo.services.Service;
import com.logistimo.services.ServiceException;

import java.util.List;

/**
 * @author juhee
 */
public interface MaterialCatalogService extends Service {

  /**
   * Add a material to a given domain
   */
  Long addMaterial(Long domainId, IMaterial material) throws ServiceException;

  /**
   * Update a given material (domain ID is assumed to be correct in the object)
   */
  void updateMaterial(IMaterial material, Long domainId) throws ServiceException;

  /**
   * Get a material, given a material Id
   */
  IMaterial getMaterial(Long materialId) throws ServiceException;

  /**
   * Get a materialId, given a domain and material short-code
   */
  Long getMaterialId(Long domainId, String shortCode) throws ServiceException;

  /**
   * Get a materialId, given a domain and material name
   */
  IMaterial getMaterialByName(Long domainId, String materialName) throws ServiceException;

  /**
   * Delete materials, given the material Ids
   */
  void deleteMaterials(Long domainId, List<Long> materialIds) throws ServiceException;

  /**
   * Find all materials in a given domain, with pagination
   */
  Results getAllMaterials(Long domainId, String tag, PageParams pageParams) throws ServiceException;

  Results searchMaterialsNoHU(Long domainId, String q);

  List<Long> getAllMaterialIds(Long domainId);
}

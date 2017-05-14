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

package com.logistimo.orders.service;

import com.logistimo.pagination.QueryParams;
import com.logistimo.orders.entity.IDemandItem;

import com.logistimo.pagination.PageParams;
import com.logistimo.pagination.Results;
import com.logistimo.services.Service;
import com.logistimo.services.ServiceException;
import com.logistimo.models.orders.DiscrepancyModel;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.jdo.PersistenceManager;

/**
 * Created by smriti on 9/30/16.
 */
public interface IDemandService extends Service {
  @SuppressWarnings("rawtypes")
  List<Map> getDemandItems(Collection<? extends IDemandItem> items, String currency,
                           Locale locale, String timezone,
                           boolean forceIntegerQuantity);

  Results getDemandItems(Long domainId, Long kioskId, Long mId, String eTag, String mTag,
                         Boolean excludeTransfer, Boolean showBackOrder,
                         String orderType, Integer offset, Integer size) throws ServiceException;

  List<IDemandItem> getDemandItems(Long orderId) throws ServiceException;

  void clearAllocations(Long kioskId, Long materialId, Long orderId, Boolean includeTransfer,
                        Boolean BackOrder) throws ServiceException;

  Results getDemandItemsWithDiscrepancies(Long domainId, String oTypBoolean,
                                          Boolean excludeTransfer, Long kioskId,
                                          List<Long> kioskIds, Long materialId, String kioskTag,
                                          String materialTag, Date from, Date to, Long orderId,
                                          String discType, PageParams pageParams)
      throws ServiceException;

  /**
   * @param kioskId         - Entity Id
   * @param materialId      - Material Id
   * @param excludeTransfer - Includes transfer orders
   * @param showbackOrder   - Includes only orders in back order tests
   * @param orderType       - Sales or purchase orders
   * @param includeShipped  - Include shipped orders, will not be honoured with showBackOrder is sent.
   */
  Results getDemandDetails(Long kioskId, Long materialId, Boolean excludeTransfer,
                           Boolean showbackOrder,
                           String orderType, boolean includeShipped)
      throws ServiceException;

  List<IDemandItem> getDemandItems(Long orderId, PersistenceManager pm);

  QueryParams getQueryParams(Long domainId, String oType, Boolean excludeTransfer,
                                       Long kioskId, List<Long> kioskIds, Long materialId,
                                       String kioskTag, String materialTag, Date from, Date to,
                                       Long orderId, String discType, PageParams pageParams)
      throws ServiceException;

  List<DiscrepancyModel> getDiscrepancyModels(List objects) throws ServiceException;

  Map<String, Object> getDemandItemAsMap(Long id, String currency, Locale locale, String timezone,
                                         boolean forceIntegerQuantity);

  BigDecimal getAllocatedQuantityForDemandItem(String id, Long oId, Long mId);

  String getMaterialStatusForDemandItem(String id, Long oId, Long mId);
  //IDemandItem getDemandItemByMaterial(Long orderId, Long materialId, PersistenceManager pm);

}

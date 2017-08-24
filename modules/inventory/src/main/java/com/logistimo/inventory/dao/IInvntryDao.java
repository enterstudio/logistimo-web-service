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

package com.logistimo.inventory.dao;


import com.logistimo.exception.InvalidDataException;
import com.logistimo.inventory.entity.IInvntry;
import com.logistimo.inventory.entity.IInvntryBatch;
import com.logistimo.inventory.entity.IInvntryEvntLog;
import com.logistimo.inventory.entity.IInvntryLog;
import com.logistimo.inventory.models.InventoryFilters;
import com.logistimo.pagination.PageParams;
import com.logistimo.pagination.QueryParams;
import com.logistimo.pagination.Results;
import com.logistimo.services.ServiceException;

import java.util.Date;
import java.util.List;
import java.util.Locale;

import javax.jdo.PersistenceManager;

/**
 * Created by charan on 03/03/15.
 */
public interface IInvntryDao {
  /**
   * Lookup by Id.
   */
  IInvntry getById(String id);

  IInvntry getById(String id, PersistenceManager pm);

  /**
   * Get Key as String.
   * In GAE, using KeyFactory
   * In SQL, using String.valueOf
   */
  String getInvKeyAsString(IInvntry invntry);

  /**
   * Returns key as string for Inventory matching with kioskId and materialId.
   * In GAE, Just creates via KeyFactory
   * In SQL, Lookup matching inventory and return Long id as String.
   */
  String getKeyString(Long kioskId, Long materialId);


  /**
   * Find associated Inventory matching with kioskId and materialId.
   * In GAE, Just creates via KeyFactory
   * In SQL, Lookup matching inventory and return Long id as String.
   */
  IInvntry findId(Long kioskId, Long materialId);

  IInvntry findId(Long kioskId, Long materialId, PersistenceManager persistenceManager);

  IInvntry findShortId(Long kioskId, Long shortId, PersistenceManager persistenceManager);


  /**
   *
   * @param invEventLog
   * @return
   */
  IInvntry getInvntry(IInvntryEvntLog invEventLog);

  IInvntry getDBInvntry(IInvntry invntry, PersistenceManager pm);

  /**
   * Get Matching Inventory from DB. Uses passed invntry's key to get.
   */
  IInvntry getDBInvntry(IInvntry invntry);

  /**
   * Get associated Invntry Event Log object , if defined else returns null.
   */
  IInvntryEvntLog getInvntryEvntLog(IInvntry invntry);

  /**
   * Creates Invntry Event Log
   */
  void createInvntryEvntLog(int type, IInvntry inv);

  /**
   * Get the warning text for stock events
   */
  String getStockEventWarning(IInvntry inv, Locale locale, String timezone);

  void setInvntryLogKey(IInvntryLog invntryLog);

  void setInvBatchKey(IInvntryBatch invBatch);

  IInvntryBatch findInvBatch(Long kioskId, Long materialId, String batchId);

  IInvntryBatch findInvBatch(Long kioskId, Long materialId, String batchId, PersistenceManager pm);

  /**
   * Get the last stock event
   */
  IInvntryEvntLog getLastStockEvent(IInvntry inv, PersistenceManager pm);

  /**
   * Get recent inventory event logs for the given inventory Id
   */
  List<IInvntryEvntLog> getInvntryEvntLog(Long invId, int size, int offset);

  /**
   * Finds and returns all inventory events active during the period between start date and end date.
   *
   * @param kioskId    - Entity Id
   * @param materialId - Material Id
   * @param start      - Start date
   * @param end        - end date
   * @param pm         - Persistence Manager (optional)
   * @return List of InvntryEventLog
   */
  List<IInvntryEvntLog> getInvEventLogs(Long kioskId, Long materialId, Date start, Date end,
                                        PersistenceManager pm);

  /**
   * Removes all inventory events active during the period between start and end date.
   *
   * @param kioskId    - Entity Id
   * @param materialId - Material Id
   * @param start      - Start date
   * @param end        - end date
   * @param pm         - Persistence Manager (optional)
   * @return Removed list of InvntryEventLog
   */
  List<IInvntryEvntLog> removeInvEventLogs(Long kioskId, Long materialId, Date start, Date end,
                                           PersistenceManager pm);

  QueryParams buildInventoryQuery(InventoryFilters inventoryFilters, boolean countQuery)
      throws InvalidDataException;


  /**
   * Get Inventory using the filters
   * @param inventoryFilters
   * @param pageParams
   * @return Inventory objects
   * @throws ServiceException
   */
  Results getInventory(InventoryFilters inventoryFilters, PageParams pageParams,
                       PersistenceManager pm)
      throws ServiceException;

  /**
   * Validates if batch management update is allowed on an entity, by checking if there are any non zero batch enabled inventory items for that entity
   * @param kioskId - The kiosk for which batch management update check is to be made
   * @param pm - Persistence Manager instance
   * @return true or false
   * @throws ServiceException
   */
  boolean validateEntityBatchManagementUpdate(Long kioskId, PersistenceManager pm) throws ServiceException;

  /**
   * Validates if batch management update is allowed on an material, by checking if there are any non zero batch enabled inventory items for that material
   * @param materialId - The material for which batch management update check is to be made
   * @param pm - Persistence Manager instance
   * @return true or false
   * @throws ServiceException
   */
  boolean validateMaterialBatchManagementUpdate(Long materialId, PersistenceManager pm) throws ServiceException;
}

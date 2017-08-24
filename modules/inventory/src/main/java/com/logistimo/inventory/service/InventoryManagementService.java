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

package com.logistimo.inventory.service;

import com.logistimo.config.models.DomainConfig;
import com.logistimo.entities.models.LocationSuggestionModel;
import com.logistimo.inventory.entity.IInvAllocation;
import com.logistimo.inventory.entity.IInventoryMinMaxLog;
import com.logistimo.inventory.entity.IInvntry;
import com.logistimo.inventory.entity.IInvntryBatch;
import com.logistimo.inventory.entity.IInvntryEvntLog;
import com.logistimo.inventory.entity.ITransaction;
import com.logistimo.inventory.models.ErrorDetailModel;
import com.logistimo.inventory.models.InventoryFilters;
import com.logistimo.models.shipments.ShipmentItemBatchModel;
import com.logistimo.pagination.PageParams;
import com.logistimo.pagination.Results;
import com.logistimo.services.DuplicationException;
import com.logistimo.services.Service;
import com.logistimo.services.ServiceException;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.jdo.PersistenceManager;

public interface InventoryManagementService extends Service {

  /**
   * Get the inventory, given a kiosk ID and material ID
   *
   * @param kioskId    the kiosk ID
   * @param materialId the material ID
   * @return Inventory object contain inventory metadata
   */
  IInvntry getInventory(Long kioskId, Long materialId) throws ServiceException;

  /**
   * Get inventory from kiosk id and short id
   */
  IInvntry getInvntryByShortID(Long kioskId, Long shortId) throws ServiceException;

  /** Get inventory from location */
  Results getInvntryByLocation(
      Long domainId,
      LocationSuggestionModel location,
      String kioskTags,
      String excludedKioskTags,
      String materialTags,
      String pdos,
      PageParams pageParams)
      throws ServiceException;

  /**
   * Get all inventory items associated with a given kiosk.
   *
   * @param kioskId The kiosk identified by its ID
   * @return A list of Inventory objects
   */
  Results getInventoryByKiosk(Long kioskId, PageParams params) throws ServiceException;

  /**
   * Get inventory by kiosk and a material tag (optional)
   */
  Results getInventoryByKiosk(Long kioskId, String materialTag, PageParams params)
      throws ServiceException;

  /**
   * Search Inventory starts with by kiosk and material tag (optional)
   */
  Results searchKioskInventory(Long kioskId, String materialTag, String nameStartsWith,
                               PageParams params) throws ServiceException;

  /**
   * Get all inventory items associated with a given material, across kiosks
   *
   * @param materialId The material identified by its ID
   * @param kioskTag   Tag of the kiosk
   * @param kioskIds   A list of kiosk IDs to filter the result
   * @return A list of Inventory objects
   * NOTE: kiosk tag is optional
   */
  Results getInventoryByMaterial(Long materialId, String kioskTag, List<Long> kioskIds,
                                 PageParams pageParams) throws ServiceException;

  /**
   * Get Inventory by material of a domain
   */
  Results getInventoryByMaterialDomain(Long materialId, String kioskTag, List<Long> kioskIds,
                                       PageParams pageParams, Long domainId)
      throws ServiceException;

  /**
   * Get inventory by batch number for a given material
   */
  Results getInventoryByBatchId(Long materialId, String batchId, PageParams pageParams,
                                Long domainId, String kioskTags, String excludedKioskTags, LocationSuggestionModel location) throws ServiceException;

  /**
   * Get the valid and as well expired or active batches for a given batchid based on excludeExpired param
   */
  Results getValidBatchesByBatchId(String batchId, Long materialId, Long kioskId, Long domainId,
                                   boolean excludeExpired, PageParams pageParams);

  /**
   * Get inventory by batch expiry
   */
  Results getInventoryByBatchExpiry(Long domainId, Long materialId, Date start, Date end,
                                    String kioskTag, String excludedKioskTag, String materialTag, LocationSuggestionModel location, PageParams pageParams)
      throws ServiceException;

  /**
   * Get valid batches for a given inventory item - this includes active batches with non-zero stock
   */
  Results getValidBatches(Long materialId, Long kioskId, PageParams pageParams)
      throws ServiceException;

  Results getBatches(Long materialId, Long kioskId, PageParams pageParams) throws ServiceException;

  /**
   * Add a new inventory items to the data store.
   *
   * @param items     The set of inventory items materials to be associated with this kiosk
   * @param overwrite if true will overwrite inventory meta
   */
  void addInventory(Long domainId, List<IInvntry> items, boolean overwrite, String user)
      throws ServiceException;

  Long getShortId(Long kioskId);

  /**
   * Update the list of inventory items.
   */
  void updateInventory(List<IInvntry> items, String user) throws ServiceException;

  void updateInventory(List<IInvntry> items, String user, PersistenceManager pm, boolean closePM)
      throws ServiceException;

  /**
   * Remove a given inventory item identified by its kiosk ID and material ID.
   */
  void removeInventory(Long domainId, Long kioskId, List<Long> materialIds) throws ServiceException;

  /**
   * Get inventory transactions for a given kiosk.
   * kioskId is mandatory.
   */
  Results getInventoryTransactionsByKiosk(Long kioskId, Long materialId, String materialTag,
                                          Date sinceDate,
                                          Date untilDate, String transType, PageParams pageParams,
                                          String bid, boolean atd, String reason)
      throws ServiceException;

  /**
   * Get inventory transactions for a given kiosk.
   * kioskId and sinceDate are mandatory. transType is optional
   */
  Results getInventoryTransactionsByKiosk(Long kioskId, String materialTag, Date sinceDate,
                                          Date untilDate, String transType, PageParams pageParams,
                                          String bid, boolean atd, String reason)
      throws ServiceException;

  /**
   * Get inventory transactions for a given material.
   * materialId and sinceDate are mandatory. transType is optional
   */
  Results getInventoryTransactionsByMaterial(Long materialId, String kioskTag, Date sinceDate,
                                             Date untilDate, String transType,
                                             PageParams pageParams, Long domainId, String bid,
                                             boolean atd, String reason) throws ServiceException;

  /**
   * Get inventory transactions for a given domain.
   * domainId and sinceDate are mandatory. transType is optional
   * NOTE: Only one of kioskTag or materialTag can be passed
   */
  Results getInventoryTransactionsByDomain(Long domainId, String kioskTag, String materialTag,
                                           Date sinceDate, Date untilDate, String transType,
                                           List<Long> kioskIds, PageParams pageParams, String bid,
                                           boolean atd, String reason) throws ServiceException;

  /**
   *
   * @throws ServiceException
   */
  Results getInventoryTransactionsByKioskLink(Long kioskId, Long linkedKioskId, String materialTag,
                                              Date sinceDate, Date untilDate, String transType,
                                              PageParams pageParams, String bid, boolean atd,
                                              String reason) throws ServiceException;

  /**
   * Get inventory transactions by a given user
   */
  Results getInventoryTransactionsByUser(String userId, Date fromDate, Date toDate,
                                         PageParams pageParams) throws ServiceException;

  /**
   * Get inventory transactions, common method that honors all parameters
   *
   * @param sinceDate      - Start date of the transaction, inclusive
   * @param untilDate      - End date of the transactions, inclusive
   * @param domainId       - Domain of which transactions should be fetched
   * @param kioskId        - Entity id
   * @param materialId     - Material id
   * @param transTypes     - List of transaction types to filter see ITransaction constants
   * @param linkedKioskId  - Customer or Vendor entity id
   * @param kioskTag       - Entity tag
   * @param materialTag    - Material tag
   * @param kioskIds       - List of entity ids with in which transactions should be fetched, used for manager roles
   * @param pageParams     - Page object with size, offset params
   * @param bid            - Batch id
   * @param atd            - Use actual transaction date to filter sinceDate and untilDate params instead of transaction time
   * @param reason         - Reason code provided in the transaction
   * @param excludeReasons - excludes transactions containing the list of excluded reasons.
   * @return Transactions which meet the filter criteria specified as parameters in Results object.
   */
  Results getInventoryTransactions(Date sinceDate, Date untilDate, Long domainId, Long kioskId,
                                   Long materialId, List<String> transTypes, Long linkedKioskId,
                                   String kioskTag, String materialTag, List<Long> kioskIds,
                                   PageParams pageParams, String bid, boolean atd, String reason,
                                   List<String> excludeReasons) throws ServiceException;

  Results getInventoryTransactions(Date sinceDate, Date untilDate, Long domainId, Long kioskId,
                                   Long materialId, String transType, Long linkedKioskId,
                                   String kioskTag, String materialTag, List<Long> kioskIds,
                                   PageParams pageParams, String bid, boolean atd, String reason)
      throws ServiceException;

  /**
   * Get inventory transactions by tracking id
   */
  Results getInventoryTransactionsByTrackingId(Long trackingId) throws ServiceException;

  /**
   * Updates multiple inventory transactions and return a list of errors
   *
   * @return Erroneous transactions with error message in the object
   */
  List<ITransaction> updateInventoryTransactions(Long domainId,
                                                 List<ITransaction> inventoryTransactions)
      throws ServiceException, DuplicationException;

  List<ITransaction> updateInventoryTransactions(Long domainId,
                                                 List<ITransaction> inventoryTransactions,
                                                 PersistenceManager pm)
      throws ServiceException, DuplicationException;

  List<ITransaction> updateInventoryTransactions(Long domainId,
                                                 List<ITransaction> inventoryTransactions,
                                                 boolean skipVal)
      throws ServiceException, DuplicationException;

  List<ITransaction> updateInventoryTransactions(Long domainId,
                                                 List<ITransaction> inventoryTransactions,
                                                 boolean skipVal, PersistenceManager pm)
      throws ServiceException, DuplicationException;

  List<ITransaction> updateInventoryTransactions(Long domainId,
                                                 List<ITransaction> inventoryTransactions,
                                                 boolean skipVal, boolean skipPred)
      throws ServiceException, DuplicationException;

  List<ITransaction> updateInventoryTransactions(Long domainId,
                                                 List<ITransaction> inventoryTransactions,
                                                 boolean skipVal, boolean skipPred,
                                                 PersistenceManager pm)
      throws ServiceException, DuplicationException;


  /**
   * Updates Inventory, persists transactions, Creates inventory logs, events and posts prediction tasks
   *
   * @param domainId              - Domain Id
   * @param inventoryTransactions - List of transactions to persist.
   * @param invntryList           - Inventory list available, will reuse if passed.
   * @param skipVal               - Skip validations
   * @param skipPred              - Skip predictions
   * @param pm                    - Persistence manager
   */
  @SuppressWarnings({"unchecked", "rawtypes"})
  List<ITransaction> updateInventoryTransactions(Long domainId,
                                                 List<ITransaction> inventoryTransactions,
                                                 List<IInvntry> invntryList,
                                                 boolean skipVal, boolean skipPred,
                                                 PersistenceManager pm)
      throws ServiceException, DuplicationException;

  /**
   * Update a single inventory transaction
   */
  ITransaction updateInventoryTransaction(Long domainId, ITransaction inventoryTransaction)
      throws ServiceException, DuplicationException;

  ITransaction updateInventoryTransaction(Long domainId, ITransaction inventoryTransaction,
                                          PersistenceManager pm)
      throws ServiceException, DuplicationException;

  ITransaction updateInventoryTransaction(Long domainId, ITransaction inventoryTransaction,
                                          boolean skipPred)
      throws ServiceException, DuplicationException;

  ITransaction updateInventoryTransaction(Long domainId, ITransaction inventoryTransaction,
                                          boolean skipPred, boolean skipVal, PersistenceManager pm)
      throws ServiceException, DuplicationException;

  ITransaction updateInventoryTransaction(Long domainId, ITransaction inventoryTransaction,
                                          boolean skipPred,
                                          PersistenceManager pm)
      throws ServiceException, DuplicationException;

  /**
   * Undo inventory transactions (depending on the transaction type). Returns a list of transactions that could NOT be un-done.
   */
  List<ITransaction> undoTransactions(List<String> transactionIds) throws ServiceException;

  /**
   * Get the k value, given the Service Level
   */
  float getKValue(float serviceLevel) throws ServiceException;

  int getOutOfStockCounts(Long entityId);

  // Get the inventory given either of kioskId or materialId
  IInvntry getInventory(Long kioskId, Long materialId, PersistenceManager pm)
      throws ServiceException;

  IInvntryBatch getInventoryBatch(Long kioskId, Long materialId, String batchId,
                                  PersistenceManager pm);

  BigDecimal getStockAvailabilityPeriod(IInvntry invntry, DomainConfig currentDC);

  BigDecimal getStockAvailabilityPeriod(BigDecimal cr, BigDecimal stk);

  BigDecimal getDailyConsumptionRate(IInvntry inv);

  BigDecimal getDailyConsumptionRate(IInvntry inv, int crType, String frequency);

  IInventoryMinMaxLog createMinMaxLog(IInvntry inv, int type, int crType);

  IInventoryMinMaxLog createMinMaxLog(IInvntry inv, int type, int crType, int source);

  /**
   * Get the list of IInventoryMinMaxLog for a given inventory id
   */
  List<IInventoryMinMaxLog> fetchMinMaxLog(String invId);

  List<IInvAllocation> getAllocationsByTag(String tag);

  List<IInvAllocation> getAllocationsByTagMaterial(Long mId, String tag);

  List<IInvAllocation> getAllocations(Long kid, Long mid);

  List<IInvAllocation> getAllocationsByTypeId(Long kid, Long mid, IInvAllocation.Type type,
                                              String typeId);

  void allocate(Long kid, Long mid, IInvAllocation.Type type, String typeId, String tag,
                BigDecimal quantity,
                List<ShipmentItemBatchModel> batchDetails, String userId, PersistenceManager pm)
      throws ServiceException;

  void allocate(Long kid, Long mid, IInvAllocation.Type type, String typeId, String tag,
                BigDecimal quantity, List<ShipmentItemBatchModel> bdetails, String userId,
                PersistenceManager pm, String materialStatus) throws ServiceException;

  void allocateAutomatically(Long kid, Long mid, IInvAllocation.Type type, String typeId,
                             String tag,
                             BigDecimal quantity, String userId, boolean autoAssignStatus,
                             PersistenceManager pm)
      throws ServiceException;

  void clearAllocation(Long kid, Long mid, IInvAllocation.Type type, String typeId)
      throws ServiceException;

  void clearAllocation(Long kid, Long mid, IInvAllocation.Type type, String typeId,
                       PersistenceManager pm)
      throws ServiceException;

  void clearBatchAllocation(Long kid, Long mid, IInvAllocation.Type type, String typeId,
                            String batchId,
                            PersistenceManager persistenceManager) throws ServiceException;

  void clearAllocationByTag(Long kid, Long mid, String tag) throws ServiceException;

  void clearAllocationByTag(Long kid, Long mid, String tag, PersistenceManager pm)
      throws ServiceException;

  /**
   * Transfers the allocation between inventory by type and type id
   *
   * @param kid          Kiosk Id
   * @param mid          Material Id
   * @param srcType      Source inventory type
   * @param srcTypeId    ID of source inventory type object
   * @param destType     Type of destination inventory
   * @param destTypeId   ID of destination inventory type object
   * @param quantity     Quantity to transfer, if non batch material
   * @param batchDetails Batch details with Batch ID, Quantity and other meta to transfer, if batch material
   * @param tag          Default tag to be used when source doesn't have one, in-case source not exist
   * @param userId       ID of user who is transferring
   * @param isSet        If true sets the destination allocation to the requested value, else increments the destination allocation
   *
   */
  void transferAllocation(Long kid, Long mid, IInvAllocation.Type srcType, String srcTypeId,
                          IInvAllocation.Type destType, String destTypeId, BigDecimal quantity,
                          List<ShipmentItemBatchModel> batchDetails, String userId, String tag,
                          PersistenceManager pm, String materialStatus, boolean isSet) throws ServiceException;

  /**
   * Returns the duration from expected reorder point, min event start time if applicable
   */
  Long getDurationFromRP(Long invKey);

  /**
   * Adjusts events of the entity and material to match with this event. This should be used only for
   * Manual bulk upload configuration.
   *
   * @param invntryEvntLog - Inventory event log
   */
  IInvntryEvntLog adjustInventoryEvents(IInvntryEvntLog invntryEvntLog) throws ServiceException;

  Results getInventory(Long domainId, Long kioskId, List<Long> kioskIds, String kioskTags, String excludedKioskTags,
                              Long materialId, String materialTag, int batchEnabled,
                              boolean onlyNZInv, String pdos, LocationSuggestionModel location, PageParams pageParams) throws ServiceException;

  Results getInventory(InventoryFilters filters, PageParams pageParams) throws ServiceException;

  boolean validateEntityBatchManagementUpdate(Long kioskId) throws ServiceException;
  boolean validateMaterialBatchManagementUpdate(Long materialId) throws ServiceException;

  /**
   * Updates multiple inventories by persisting multiple transactions (of one or more types and one or more material)
   * @param materialTransactionsMap - map of material id to list of transactions to be persisted
   * @param domainId - domain ID
   * @param userId - user ID
   * @return - Map<Long,List<ErrorDetailModel>> - A map of material id and the details of the errors that were encountered while persisting the transactions
   * @throws ServiceException
   */
  Map<Long,List<ErrorDetailModel>> updateMultipleInventoryTransactions(Map<Long,List<ITransaction>> materialTransactionsMap, Long domainId, String userId) throws ServiceException;

  /**
   * Get the unusable stock (stock total stock expiring before consumption) for a specified kiosk and material
   * @param kId - kiosk ID
   * @param mId - material ID
   * @return the total unusable stock across batches
   * @throws ServiceException
   */
  BigDecimal getUnusableStock(Long kId, Long mId) throws ServiceException;

  Long getInvMaterialCount(Long domainId, Long tagId) throws ServiceException;
}

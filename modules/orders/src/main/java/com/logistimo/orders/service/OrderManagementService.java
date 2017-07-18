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

import com.logistimo.config.models.LeadTimeAvgConfig;
import com.logistimo.conversations.entity.IMessage;
import com.logistimo.exception.LogiException;
import com.logistimo.exception.ValidationException;
import com.logistimo.inventory.entity.ITransaction;
import com.logistimo.orders.OrderResults;
import com.logistimo.orders.entity.IDemandItem;
import com.logistimo.orders.entity.IOrder;
import com.logistimo.orders.models.UpdatedOrder;
import com.logistimo.pagination.PageParams;
import com.logistimo.pagination.Results;
import com.logistimo.services.ObjectNotFoundException;
import com.logistimo.services.Service;
import com.logistimo.services.ServiceException;

import javax.jdo.PersistenceManager;
import java.math.BigDecimal;
import java.util.Collection;
import java.util.Date;
import java.util.List;

public interface OrderManagementService extends Service {

  /**
   * Adds given message to the order
   */
  IMessage addMessageToOrder(Long orderId, String message, String userId)
      throws ServiceException, ObjectNotFoundException;

  /**
   * Creates a shipment with all demand items in this Order and marks the shipment as shipped.
   *
   * @param order                  - Order object
   * @param transporter            - transporter name
   * @param trackingId             - tracking Id
   * @param reason                 - reason code string
   * @param expectedFulfilmentDate - Expected fulfilment date (optional)
   */
  String shipNow(IOrder order, String transporter, String trackingId, String reason,
                 Date expectedFulfilmentDate,
                 String userId, String ps, int source)
      throws ServiceException, ObjectNotFoundException, ValidationException;

  /**
   * Gets the order details for given order. This method does not fetch demand items.
   *
   * @param orderId - order id
   * @return - returns order details if found without demand items.
   */
  IOrder getOrder(Long orderId) throws ObjectNotFoundException, ServiceException;

  /**
   * Gets the order details for given order.
   *
   * @param orderId      - order id
   * @param includeItems - determines whether demand items should be included in response or not
   * @return - returns order details
   */
  IOrder getOrder(Long orderId, boolean includeItems)
      throws ObjectNotFoundException, ServiceException;

  /**
   * Add a new order
   */
  Long addOrder(IOrder order) throws ServiceException;

  /**
   * Update an order and automatically post goods issued/received (GI/GR)
   */
  UpdatedOrder updateOrder(IOrder order, int source) throws LogiException;

  /**
   * Update an order and automatically post goods issued/received (GI/GR)
   *
   * @param isLocked Is order already locked
   */
  UpdatedOrder updateOrder(IOrder order, int source, boolean isLocked, boolean validateHU)
      throws LogiException;

    /*UpdatedOrder updateOrderWithAllocations(IOrder order, int source, boolean isLocked,
                                            boolean validateHU, String userId) throws LogiException;*/

  UpdatedOrder updateOrder(IOrder order, int source, boolean isLocked, boolean validateHU,
                           String user) throws LogiException;

  UpdatedOrder updateOrder(IOrder order, int source, boolean isLocked, boolean validateHU,
                           String user, PersistenceManager pm) throws LogiException;

  // Generate shipment events, if configured
  void generateOrderCommentEvent(Long domainId, int eventId, String objectType, String objectId,
                                 String message,
                                 List<String> userIds);

  /**
   * Get orders for a given kiosk, status (optional) and a time limit (optional)
   * NOTE: otype = order type, which is optional and defaulted to purchase orders, but can be set as 'sale' or 'purchase'; kioskIds is the list of kiosk IDs by which orders have to be filtered
   */
  Results getOrders(Long domainId, Long kioskId, String status, Date since, Date untilDate,
                    String otype, String tagType, String tag, List<Long> kioskIds,
                    PageParams pageParams, Integer orderType, String referenceId, String approvalStatus)
      throws ServiceException;

  Results getOrders(Long domainId, Long kioskId, String status, Date since, Date untilDate,
                    String otype, String tagType, String tag, List<Long> kioskIds,
                    PageParams pageParams, Integer orderType, String referenceId, String approvalStatus,
                    boolean withDemand) throws ServiceException;

  /**
   * Get orders placed by a certain user
   */
  Results getOrders(String userId, Date fromDate, Date toDate, PageParams pageParams)
      throws ServiceException;

  /**
   * Get demand items according to specified criteria
   * NOTE: domainId is the mandatory attribute, all others are optional; either kiosk or material id can be specified, but NOT both
   * NOTE: kioskTag and materialTag are both optional, and if specified, only one should be specified
   */
  Results getDemandItems(Long domainId, Long kioskId, Long materialId, String kioskTag,
                         String materialTag, Date since, PageParams pageParams)
      throws ServiceException;

  /**
   * Create/update an order from a transaction list
   *
   * @param transType                           Whether order (new order) or re-order (edit order)
   * @param trackingId                          Same as Order Id
   * @param createOrder                         Create an order and not just a demand list
   * @param servicingKiosk                      Vendor entity ID
   * @param utcExpectedFulfillmentTimeRangesCSV CSV of UTC date-ranges with each entry formatted as <fromDate>-<toDate>. Each date is a UTC date of the format dd/MM/yy hh:mm:ss; <toDate> is optional in any given entry.
   * @param utcConfirmedFulfillmentTimeRange    <fromDate>-<toDate>, where <toDate> is optional, and dates are UTC dates in the format dd/MM/yy hh:mm:ss
   * @param paymentOption                       A freeform payment option string
   * @param packageSize                         A freeform package size string
   */
  OrderResults updateOrderTransactions(
      Long domainId, String userId, String transType, List<ITransaction> inventoryTransactions,
      Long kioskId, Long trackingId,
      String message, boolean createOrder, Long servicingKiosk, Double latitude, Double longitude,
      Double geoAccuracy, String geoErrorCode,
      String utcExpectedFulfillmentTimeRangesCSV, String utcConfirmedFulfillmentTimeRange,
      BigDecimal payment, String paymentOption, String packageSize,
      boolean allowEmptyOrders,int source
  ) throws ServiceException;

  /**
   * Update inventory transactions, orders included - return an order Id, if successful
   * transType can be order or re-order
   */
  OrderResults updateOrderTransactions(
      Long domainId, String userId, String transType, List<ITransaction> inventoryTransactions,
      Long kioskId, Long trackingId,
      String message, boolean createOrder, Long servicingKiosk, Double latitude, Double longitude,
      Double geoAccuracy, String geoErrorCode,
      String utcExpectedFulfillmentTimeRangesCSV, String utcConfirmedFulfillmentTimeRange,
      BigDecimal payment, String paymentOption, String packageSize,
      boolean allowEmptyOrders, List<String> orderTags, Integer orderType, Boolean isSalesOrder,
      String referenceId, Date reqByDate, Date eta,int src
  ) throws ServiceException;

  OrderResults updateOrderTransactions(
      Long domainId, String userId, String transType, List<ITransaction> inventoryTransactions,
      Long kioskId, Long trackingId,
      String message, boolean createOrder, Long servicingKiosk, Double latitude, Double longitude,
      Double geoAccuracy, String geoErrorCode,
      String utcExpectedFulfillmentTimeRangesCSV, String utcConfirmedFulfillmentTimeRange,
      BigDecimal payment, String paymentOption, String packageSize,
      boolean allowEmptyOrders, List<String> orderTags, Integer orderType, Boolean isSalesOrder,
      String referenceId, Date reqByDate, Date eta, int source, PersistenceManager pm
  ) throws ServiceException;

  /**
   * Update an order's status, and post inventory issues/receipts, if needed.
   * NOTE: If message and userIdsToBeNotified are not present, they are taken from the Notifications configuration
   */
  UpdatedOrder updateOrderStatus(Long orderId, String newStatus, String updatingUserId,
                                 String message, List<String> userIdsToBeNotified, int source)
      throws ServiceException;

  UpdatedOrder updateOrderStatus(Long orderId, String newStatus, String updatingUserId,
                                 String message, List<String> userIdsToBeNotified, int source,
                                 PersistenceManager pm, String reason) throws ServiceException;

  // Modify order status and its items
//	void modifyOrder(IOrder o, String userId, List<ITransaction> transactions, Date timestamp, Long domainId, String transType, String message, String utcEstimatedFulfillmentTimeRanges, String utcConfirmedFulfillmentTimeRange, BigDecimal payment, String paymentOption, String packageSize, boolean allowEmptyOrders) throws ServiceException;
  void modifyOrder(IOrder o, String userId, List<ITransaction> transactions, Date timestamp,
                   Long domainId, String transType, String message,
                   String utcEstimatedFulfillmentTimeRanges,
                   String utcConfirmedFulfillmentTimeRange, BigDecimal payment,
                   String paymentOption, String packageSize, boolean allowEmptyOrders,
                   List<String> orderTags, Integer orderType, String referenceId)
      throws ServiceException;

  void modifyOrder(IOrder o, String userId, List<ITransaction> transactions, Date timestamp,
                   Long domainId, String transType, String message,
                   String utcEstimatedFulfillmentTimeRanges,
                   String utcConfirmedFulfillmentTimeRange, BigDecimal payment,
                   String paymentOption, String packageSize, boolean allowEmptyOrders,
                   List<String> orderTags, Integer orderType, String referenceId,
                   PersistenceManager pm) throws ServiceException;


  List<IDemandItem> getDemandItemByStatus(Long kioskId, Long materialId, Collection<String> status)
      throws ServiceException;

  List<String> getIdSuggestions(Long domainId, String text, String type, Integer oty,
                                List<Long> kioskIds) throws ServiceException;

  /**
   * Get the lead time based on the configuration.
   * @param kid - kiosk Id
   * @param mid - material Id
   * @param orderPeriodicityInConfig - order periodicity as configured (in days)
   * @param leadTimeAvgConfig - lead time configuration
   * @param leadTimeDefaultInConfig - default lead time as configured (in days)
   * @return - lead time for the inventory item specified by the kId and mId
   * @throws ServiceException
   */
  BigDecimal getLeadTime(Long kid, Long mid, float orderPeriodicityInConfig, LeadTimeAvgConfig leadTimeAvgConfig, float leadTimeDefaultInConfig) throws ServiceException;

  void updateOrderMetadata(Long orderId, String updatedBy, PersistenceManager pm);

  List<IOrder> getOrders(Long kioskId, String status, PageParams pageParams, String orderType,
                         boolean isTransfer)
      throws ServiceException;

  void updateOrderVisibility(Long orderId, Integer orderType) throws ObjectNotFoundException;


}

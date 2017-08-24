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

package com.logistimo.orders.actions;

import com.logistimo.config.models.DomainConfig;
import com.logistimo.config.models.OrdersConfig;
import com.logistimo.constants.Constants;
import com.logistimo.constants.SourceConstants;
import com.logistimo.dao.JDOUtils;
import com.logistimo.entities.entity.IKioskLink;
import com.logistimo.entities.service.EntitiesService;
import com.logistimo.events.entity.IEvent;
import com.logistimo.exception.LogiException;
import com.logistimo.inventory.entity.IInvntry;
import com.logistimo.inventory.entity.ITransaction;
import com.logistimo.inventory.models.InventoryFilters;
import com.logistimo.inventory.service.InventoryManagementService;
import com.logistimo.logger.XLog;
import com.logistimo.materials.service.MaterialCatalogService;
import com.logistimo.orders.OrderResults;
import com.logistimo.orders.approvals.constants.ApprovalConstants;
import com.logistimo.orders.approvals.dao.impl.ApprovalsDao;
import com.logistimo.orders.entity.IDemandItem;
import com.logistimo.orders.entity.IOrder;
import com.logistimo.orders.models.OrderFilters;
import com.logistimo.orders.service.OrderManagementService;
import com.logistimo.pagination.PageParams;
import com.logistimo.pagination.Results;
import com.logistimo.services.Resources;
import com.logistimo.services.ServiceException;
import com.logistimo.services.impl.PMF;
import com.logistimo.utils.StringUtil;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

import javax.jdo.PersistenceManager;
import javax.jdo.Transaction;

/**
 * Created by charan on 30/07/17.
 */
@Component
public class OrderAutomationAction {

  private static final XLog LOGGER = XLog.getLog(OrderAutomationAction.class);
  private static final String BACKEND_MESSAGES = "BackendMessages";

  private final OrderManagementService orderManagementService;
  private final InventoryManagementService inventoryManagementService;
  private final EntitiesService entitiesService;
  private final MaterialCatalogService materialsService;
  private final ApprovalsDao approvalsDao;

  @Autowired
  public OrderAutomationAction(InventoryManagementService inventoryManagementService,
                               OrderManagementService orderManagementService,
                               EntitiesService entitiesService,
                               MaterialCatalogService materialsService,
                               ApprovalsDao approvalsDao) {
    this.inventoryManagementService = inventoryManagementService;
    this.orderManagementService = orderManagementService;
    this.entitiesService = entitiesService;
    this.materialsService = materialsService;
    this.approvalsDao = approvalsDao;
  }

  public void invoke(Long domainId) throws ServiceException {
    LOGGER.info("Started processing order automation for domain {0}", domainId);
    DomainConfig domainConfig = DomainConfig.getInstance(domainId);
    OrdersConfig ordersConfig = domainConfig.getOrdersConfig();
    if (domainConfig.isCapabilityDisabled(DomainConfig.CAPABILITY_ORDERS) || !ordersConfig
        .isCreationAutomated()) {
      return;
    }
    Locale locale = domainConfig.getLocale();
    InventoryFilters filters = new InventoryFilters()
        .withSourceDomainId(domainId)
        .withMaterialTags(ordersConfig.getAutoCreateMaterialTags())
        .withKioskTags(ordersConfig.getAutoCreateEntityTags())
        .withNoIntransitStock();
    if (ordersConfig.isAutoCreateOnMin()) {
      filters.withEventType(IEvent.UNDERSTOCK)
          .withEventType(IEvent.STOCKOUT);
    } else {
      filters.withPdos(String.valueOf(ordersConfig.getAutoCreatePdos()));
    }
    List<IInvntry>
        invntryList =
        inventoryManagementService.getInventory(filters, null).getResults();
    if (invntryList == null || invntryList.isEmpty()) {
      return;
    }

    invntryList.stream()
        .map(this::getTransaction)
        .filter(Optional::isPresent)
        .map(Optional::get)
        .map(transaction -> {
          transaction.setReason(getReason(ordersConfig, locale));
          return transaction;
        })
        .collect(Collectors.groupingBy(
            iTransaction -> iTransaction.getKioskId() + "_" + iTransaction.getLinkedKioskId()))
        .forEach((key, transactions) -> process(transactions, locale));

    LOGGER.info("Completed processing order automation for domain {0}", domainId);
  }

  private String getReason(OrdersConfig ordersConfig, Locale locale) {
    ResourceBundle backendMessages = Resources.get().getBundle(BACKEND_MESSAGES, locale);
    return ordersConfig.isAutoCreateOnMin() ? backendMessages.getString("orders.item.reached.min")
        : MessageFormat.format(backendMessages.getString("orders.item.likely.stockout"),
            ordersConfig.getAutoCreatePdos());
  }


  private void process(List<ITransaction> kioskTransactions, Locale locale) {
    Long kioskId = kioskTransactions.get(0).getKioskId();
    try {
      List<IDemandItem>
          demandItems =
          orderManagementService
              .getDemandItemByStatus(kioskId, null,
                  Arrays.asList(IOrder.PENDING, IOrder.CONFIRMED));
      if (demandItems == null || demandItems.isEmpty()) {
        createNewOrder(kioskId, kioskTransactions, locale);
      } else {
        List<ITransaction> filteredTransactions = filter(kioskTransactions, demandItems);
        if (!filteredTransactions.isEmpty()) {
          List<IOrder>
              orderResults =
              orderManagementService.getOrders(new OrderFilters()
                  .setKioskId(kioskId)
                  .setLinkedKioskId(kioskTransactions.get(0).getLinkedKioskId())
                  .setStatus(IOrder.PENDING), new PageParams(1)).getResults();
          if(!orderResults.isEmpty() && isOrderEditable(orderResults.get(0).getOrderId())) {
            Long orderId = orderResults.get(0).getOrderId();
              addToExistingOrder(filteredTransactions,
                  orderId, locale);
          } else {
            createNewOrder(kioskId, filteredTransactions, locale);
          }
        }
      }
    } catch (Exception e) {
      LOGGER.warn("Unable to process orders for kiosk {0}", kioskId, e);
    }
  }

  /**
   * Check order edit conditions
   * @param orderId - order Id
   * @return - true/false
   */
  private boolean isOrderEditable(Long orderId) {
    String
        status =
        approvalsDao.getOrderApprovalMapping(orderId) != null ? approvalsDao
            .getOrderApprovalMapping(orderId).getStatus() : null;
    return
        !(status != null && (ApprovalConstants.APPROVED.equals(status)
            || ApprovalConstants.PENDING
            .equals(status)));
  }

  /**
   * Remove items that are already in an order
   */
  protected List<ITransaction> filter(List<ITransaction> kioskTransactions,
                                    List<IDemandItem> demandItems) {
    return kioskTransactions.stream()
        .filter(inventory -> !demandItems.stream()
            .anyMatch(di -> di.getMaterialId().equals(inventory.getMaterialId())))
        .collect(Collectors.toList());
  }

  private void addToExistingOrder(List<ITransaction> kioskTransactions,
                                  Long orderId, Locale locale)
      throws LogiException {
    IOrder order = orderManagementService.getOrder(orderId, true);
    PersistenceManager pm = PMF.get().getPersistenceManager();
    Transaction tx = null;
    try {
      String message = getAddMaterialsMessage(kioskTransactions, locale);
      tx = pm.currentTransaction();
      tx.begin();
      orderManagementService.modifyOrder(order, Constants.SYSTEM_USER_ID,
          kioskTransactions, new Date(), order.getDomainId(),
          ITransaction.TYPE_REORDER, message, null, null,
          null,
          null, null,
          true, null, order.getOrderType(), order.getReferenceID());
      orderManagementService
          .updateOrder(order, SourceConstants.SYSTEM, true, true, Constants.SYSTEM_USER_ID);
      LOGGER.info("Added new materials to order {0} for kiosk {1} with message {2}", orderId,
          order.getKioskId(), message);
      tx.commit();
    } finally {
      if (tx != null && tx.isActive()) {
        tx.rollback();
      }
      pm.close();
    }
  }

  private String getAddMaterialsMessage(List<ITransaction> kioskTransactions, Locale locale) {
    ResourceBundle backendMessages = Resources.get().getBundle(BACKEND_MESSAGES, locale);
    List<String> matList = kioskTransactions.stream()
        .map(ITransaction::getMaterialId)
        .map(materialId -> {
          try {
            return materialsService.getMaterial(materialId).getName();
          } catch (ServiceException e) {
            LOGGER.warn("Error finding material {0}", materialId);
          }
          return null;
        })
        .filter(Objects::nonNull)
        .collect(Collectors.toList());
    return MessageFormat
        .format(backendMessages.getString("orders.items.added"), StringUtil.getCSV(matList));
  }

  private void createNewOrder(Long kioskId, List<ITransaction> kioskTransactions, Locale locale)
      throws ServiceException {
    ResourceBundle backendMessages = Resources.get().getBundle(BACKEND_MESSAGES, locale);
    OrderResults results = orderManagementService
        .updateOrderTransactions(kioskTransactions.get(0).getDomainId(), Constants.SYSTEM_USER_ID,
            ITransaction.TYPE_ORDER, kioskTransactions, kioskId, null,
            backendMessages.getString("orders.created.by.system"), true,
            kioskTransactions.get(0).getLinkedKioskId(), null, null
            , null, null, null, null, BigDecimal.ZERO, null, null, false,
            null, IOrder.PURCHASE_ORDER, false, null, null, null,
            SourceConstants.SYSTEM);
    LOGGER.info("Created new order {0} for kiosk {1}", results.getOrder().getOrderId(), kioskId);
  }

  private Optional<ITransaction> getTransaction(IInvntry invntry) {
    try {
      ITransaction t = JDOUtils.createInstance(ITransaction.class);
      t.setKioskId(invntry.getKioskId());
      t.setMaterialId(invntry.getMaterialId());
      t.setQuantity(orderManagementService.computeRecommendedOrderQuantity(invntry));
      t.setType(ITransaction.TYPE_ORDER);
      t.setDomainId(invntry.getDomainId());
      t.setSourceUserId(invntry.getUpdatedBy());
      t.setTimestamp(new Date());
      t.setBatchId(null);
      t.setBatchExpiry(null);
      t.setReason("AUTOMATED");
      Optional<IKioskLink> vendor = chooseVendor(invntry.getKioskId(), invntry.getMaterialId());
      t.setLinkedKioskId(vendor.isPresent() ? vendor.get().getLinkedKioskId() : null);
      return Optional.of(t);
    } catch (ServiceException e) {
      LOGGER.warn("Error choosing vendor for inventory {0}:{1}", invntry.getKioskId(),
          invntry.getMaterialId(), e);
    }
    return Optional.empty();
  }

  private Optional<IKioskLink> chooseVendor(Long kioskId, Long materialId) throws ServiceException {
    Results
        links =
        entitiesService.getKioskLinks(kioskId, IKioskLink.TYPE_VENDOR, null, null, null);
    List<IKioskLink> kioskLinks = links.getResults();
    if (kioskLinks == null || kioskLinks.isEmpty()) {
      return Optional.empty();
    }
    return kioskLinks.stream()
        .filter(link -> checkInventoryExists(link.getLinkedKioskId(), materialId))
        .findFirst();
  }

  private boolean checkInventoryExists(Long kioskId, Long materialId) {
    try {
      return inventoryManagementService.getInventory(kioskId, materialId) != null;
    } catch (ServiceException e) {
      LOGGER.warn("Error while fetching inventory for {0}:{1}", kioskId, materialId, e);
    }
    return false;
  }


}

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

package com.logistimo.events.templates;

import com.logistimo.config.models.DomainConfig;
import com.logistimo.config.models.EventsConfig;
import com.logistimo.constants.Constants;
import com.logistimo.entities.entity.IKiosk;
import com.logistimo.entities.entity.Kiosk;
import com.logistimo.inventory.TransactionUtil;
import com.logistimo.inventory.entity.ITransaction;
import com.logistimo.logger.XLog;
import com.logistimo.materials.entity.Material;
import com.logistimo.services.Resources;
import com.logistimo.services.impl.PMF;
import com.logistimo.users.entity.UserAccount;
import com.logistimo.utils.BigUtil;
import com.logistimo.utils.LocalDateUtil;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

import javax.jdo.PersistenceManager;

/**
 * Created by charan on 10/03/17.
 */
public class TransactionsTemplate implements ITemplate {

  private static final XLog xLogger = XLog.getLog(TransactionsTemplate.class);
  private final ITransaction transaction;

  public TransactionsTemplate(ITransaction transaction) {
    this.transaction = transaction;
  }


  // Get template values for inventory transactions
  public Map<String, String> getTemplateValues(Locale locale, String timezone,
                                               List<String> excludeVars, Date updationTime) {
    // Get the variable map for SMS message formation
    // NOTE: Order status is deliberately OMITTED so that it can be replaced by the Javascript function
    HashMap<String, String> varMap = new HashMap<String, String>();
    PersistenceManager pm = PMF.get().getPersistenceManager();
    try {
      IKiosk customer = null, vendor = null;
      if (ITransaction.TYPE_ISSUE.equals(transaction.getType()) || ITransaction.TYPE_TRANSFER.equals(transaction.getType())) {
        if (transaction.getLinkedKioskId() != null) {
          try {
            customer = pm.getObjectById(Kiosk.class, transaction.getLinkedKioskId());
          } catch (Exception e) {
            xLogger.warn(
                "{0} when getting template values and fetching customer kiosk {1} in transaction {2} in domain {3}: {4}",
                e.getClass().getName(), transaction.getLinkedKioskId(), transaction.getKeyString(), transaction.getDomainId(), e.getMessage());
          }
        }
        try {
          vendor = pm.getObjectById(Kiosk.class, transaction.getKioskId());
        } catch (Exception e) {
          xLogger.warn(
              "{0} when getting template values and fetching vendor kiosk {1} in transaction {2} in domain {3}: {4}",
              e.getClass().getName(), transaction.getKioskId(), transaction.getKeyString(), transaction.getDomainId(), e.getMessage());
        }
      } else if (ITransaction.TYPE_RECEIPT.equals(transaction.getType())) {
        try {
          customer = pm.getObjectById(Kiosk.class, transaction.getKioskId());
        } catch (Exception e) {
          xLogger.warn(
              "{0} when getting template values and fetching customer kiosk {1} in transaction {2} in domain {3}: {4}",
              e.getClass().getName(), transaction.getLinkedKioskId(), transaction.getKeyString(), transaction.getDomainId(), e.getMessage());
        }
        if (transaction.getLinkedKioskId() != null) {
          try {
            vendor = pm.getObjectById(Kiosk.class, transaction.getLinkedKioskId());
          } catch (Exception e) {
            xLogger.warn(
                "{0} when getting template values and fetching vendor kiosk {1} in transaction {2} in domain {3}: {4}",
                e.getClass().getName(), transaction.getLinkedKioskId(), transaction.getKeyString(), transaction.getDomainId(), e.getMessage());
          }
        }
      } else { // stock count / wastage
        try {
          customer = pm.getObjectById(Kiosk.class, transaction.getKioskId());
        } catch (Exception e) {
          xLogger.warn(
              "{0} when getting template values and fetching customer kiosk {1} in transaction {2} in domain {3}: {4}",
              e.getClass().getName(), transaction.getKioskId(), transaction.getKeyString(), transaction.getDomainId(), e
              .getMessage());
        }
      }
      if (customer != null) {
        if (excludeVars == null || !excludeVars.contains(EventsConfig.VAR_CUSTOMER)) {
          varMap.put(EventsConfig.VAR_CUSTOMER, customer.getName());
        }
        if (excludeVars == null || !excludeVars.contains(EventsConfig.VAR_CUSTOMERCITY)) {
          varMap.put(EventsConfig.VAR_CUSTOMERCITY, customer.getCity());
        }
      }
      if (vendor != null) {
        if (excludeVars == null || !excludeVars.contains(EventsConfig.VAR_VENDOR)) {
          varMap.put(EventsConfig.VAR_VENDOR, vendor.getName());
        }
        if (excludeVars == null || !excludeVars.contains(EventsConfig.VAR_VENDORCITY)) {
          varMap.put(EventsConfig.VAR_VENDORCITY, vendor.getCity());
        }
      }
      // Material
      if (excludeVars == null || !excludeVars.contains(EventsConfig.VAR_MATERIAL)) {
        String name = pm.getObjectById(Material.class, transaction.getMaterialId()).getName();
        varMap.put(EventsConfig.VAR_MATERIAL, name);
      }
      // Quantity
      if (excludeVars == null || !excludeVars.contains(EventsConfig.VAR_QUANTITY)) {
        varMap.put(EventsConfig.VAR_QUANTITY, BigUtil.getFormattedValue(transaction.getQuantity()));
      }
      if (transaction.getReason() != null && (excludeVars == null || !excludeVars.contains(EventsConfig.VAR_REASON))) {
        varMap.put(EventsConfig.VAR_REASON, transaction.getReason());
      }
      if (transaction.getTrackingId() != null && (excludeVars == null || !excludeVars.contains(EventsConfig.VAR_ORDERID))) {
        varMap.put(EventsConfig.VAR_ORDERID, transaction.getTrackingId().toString());
      }
      if (excludeVars == null || !excludeVars.contains(EventsConfig.VAR_CREATIONTIME)) {
        varMap.put(EventsConfig.VAR_CREATIONTIME, LocalDateUtil.format(transaction.getTimestamp(), locale, timezone));
      }
      if (updationTime != null && (excludeVars == null || !excludeVars
          .contains(EventsConfig.VAR_UPDATIONTIME))) {
        varMap.put(EventsConfig.VAR_UPDATIONTIME,
            LocalDateUtil.format(updationTime, locale, timezone));
      }
      if (transaction.getType() != null && (excludeVars == null || !excludeVars
          .contains(EventsConfig.VAR_TRANSACTIONTYPE))) {
        varMap.put(EventsConfig.VAR_TRANSACTIONTYPE, TransactionUtil.getDisplayName(
            transaction.getType(), locale));
      }
      if (transaction.getSourceUserId() != null && (excludeVars == null || !excludeVars.contains(EventsConfig.VAR_USER))) {
        try {
          varMap.put(EventsConfig.VAR_USER, pm.getObjectById(UserAccount.class, transaction.getSourceUserId()).getFullName());
        } catch (Exception e) {
          xLogger.warn(
              "{0} when getting template values and fetching user {1} in transaction {2} in domain {3}: {4}",
              e.getClass().getName(), transaction.getSourceUserId(), transaction.getKeyString(), transaction.getDomainId(), e.getMessage());
        }
      }
      if (transaction.getSourceUserId() != null && (excludeVars == null || !excludeVars.contains(EventsConfig.VAR_USERID))) {
        varMap.put(EventsConfig.VAR_USERID, transaction.getSourceUserId());
      }

      if (transaction.getMaterialStatus() != null && (excludeVars == null || !excludeVars
          .contains(EventsConfig.VAR_MATERIAL_STATUS))) {
        varMap.put(EventsConfig.VAR_MATERIAL_STATUS, transaction.getMaterialStatus());
      }
      if (excludeVars == null || !excludeVars.contains(EventsConfig.VAR_BATCHNUMBER)) {
        varMap.put(EventsConfig.VAR_BATCHNUMBER, transaction.getBatchId() != null ? transaction.getBatchId() : Constants.EMPTYQUOTES);
      }
      if (excludeVars == null || !excludeVars.contains(EventsConfig.VAR_BATCHEXPIRY)) {
        varMap.put(EventsConfig.VAR_BATCHEXPIRY,
            transaction.getBatchExpiry() != null ? LocalDateUtil.format(transaction.getBatchExpiry(), null, null, true) : Constants.EMPTYQUOTES);
      }
    } finally {
      pm.close();
    }
    return varMap;
  }

}

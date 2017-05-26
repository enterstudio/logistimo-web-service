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

package com.logistimo.customreports;

/**
 * Created by charan on 09/03/17.
 */
public interface CustomReportConstants {

  String CUSTOMREPORTS_EXPORT_TASK_URL = "/task/customreportsexport";
  String ACTION_FINALIZECUSTOMREPORTSEXPORT = "fnlzcstrepexp";
  String ACTION_EXPORT = "export";
  String ACTION_SCHEDULEEXPORT = "scheduleexport";
  // Types
  String TYPE_TRANSACTIONS = "transactions";
  String TYPE_MANUALTRANSACTIONS = "manualtransactions"; // Manual transactions
  String TYPE_ORDERS = "orders";
  String TYPE_INVENTORY = "inventory";
  String TYPE_USERS = "users";
  String TYPE_ENTITIES = "entities";
  String TYPE_MATERIALS = "materials";
  String TYPE_TRANSACTIONCOUNTS = "transactioncounts";
  String
      TYPE_INVENTORYTRENDS =
      "inventorytrends";
  // same as ReportsConstants.TYPE_CONSUMPTION, but unrelated
  String TYPE_HISTORICAL_INVENTORYSNAPSHOT = "historicalinventorysnapshot";
  String
      TYPE_INVENTORYBATCH =
      "inventorybatch";
  // Frequency of export
  String FREQUENCY_DAILY = "daily";
  // Same as Inventory export except that this contains batch details also.
  String FREQUENCY_WEEKLY = "weekly";
  String FREQUENCY_MONTHLY = "monthly";
  // File Extension types
  String EXTENSION_XLSX = ".xlsx";
  String EXTENSION_XLS = ".xls";
  String EXTENSION_XLSM = ".xlsm";
}

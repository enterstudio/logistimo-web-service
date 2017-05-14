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

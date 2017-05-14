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

package com.logistimo.customreports.processor;

import com.logistimo.config.models.DomainConfig;
import com.logistimo.customreports.CustomReportConstants;
import com.logistimo.customreports.CustomReportsExportMgr;
import com.logistimo.customreports.CustomReportsExportMgr.CustomReportsExportParams;
import com.logistimo.customreports.utils.SpreadsheetUtil;
import com.logistimo.exports.BulkExportMgr;
import com.logistimo.exports.handlers.ExportHandlerUtil;
import com.logistimo.inventory.entity.IInvntry;
import com.logistimo.inventory.models.InvntrySnapshot;
import com.logistimo.logger.XLog;
import com.logistimo.orders.entity.IOrder;
import com.logistimo.pagination.Results;
import com.logistimo.pagination.processor.ProcessingException;
import com.logistimo.pagination.processor.Processor;
import com.logistimo.reports.service.ReportsService;
import com.logistimo.services.ServiceException;
import com.logistimo.services.Services;
import com.logistimo.services.taskqueue.ITaskService;
import com.logistimo.services.utils.ConfigUtil;
import com.logistimo.utils.JobUtil;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;

import javax.jdo.PersistenceManager;

public class CustomReportsExportProcessor implements Processor {
  private static final XLog xLogger = XLog.getLog(CustomReportsExportProcessor.class);
  private static final String DEMANDITEM_DELIMITER = "\n";
  private static final boolean isGAE = ConfigUtil.isGAE();

  private static void storeExportedData(List<String> data,
                                        CustomReportsExportMgr.CustomReportsExportParams cstReportsExportParams) {
    xLogger.fine("Entered storeExportedData");
    if (data == null || data.isEmpty()) {
      return;
    }
    xLogger.fine(
        "cstReportsexportParams.templateName = {0}, cstReportsExportParams.sheetName: {1}, cstReportsExportParams.fileName: {2}",
        cstReportsExportParams.templateName, cstReportsExportParams.sheetName,
        cstReportsExportParams.fileName);
    storeFile(data, cstReportsExportParams);
    xLogger.fine("Exiting storeExportedData");
  }

  // Store data in cloud storage, so it can be used by next task
  // private static  void storeFile( List<String> data, String fileName, String sheetName, boolean finalize, Long jobId, int size  ) {
  private static void storeFile(List<String> data,
                                CustomReportsExportMgr.CustomReportsExportParams cstReportsExportParams) {
    if (data == null || data.isEmpty() || data.size() == 0) {
      return;
    }
    // Update the JobStatus table
    JobUtil.setNumberOfRecordsCompleted(cstReportsExportParams.jobId, cstReportsExportParams.size);
    try {
      // Using SpreadsheetUtil, add or append the data in the workbook specified by fileName to the sheet specified by sheetName. This method also writes to the google cloud storage file.
      SpreadsheetUtil
          .addDataToSheet(cstReportsExportParams.fileName, cstReportsExportParams.sheetName, data);
    } catch (Exception e) {
      xLogger.severe("Exception when writing to file {0}: {1}", cstReportsExportParams.fileName,
          e.getMessage());
      JobUtil.setJobFailed(cstReportsExportParams.jobId, e.getMessage());
    }
  }

  // If the type is historicalinventorysnapshot, then given the Invntry object and snapshot date, return the InvntrySnapShot object.
  private static InvntrySnapshot getInventorySnapshot(IInvntry invntry,
                                                 CustomReportsExportMgr.CustomReportsExportParams cstReportsExportParams,
                                                 Long domainId) throws ServiceException {
    if (invntry != null) {
      ReportsService reportsService = Services.getService("reports");
      InvntrySnapshot
          invSnapshot =
          reportsService.getInventorySnapshot(invntry.getKioskId(), invntry.getMaterialId(),
              cstReportsExportParams.snapshotDate, domainId);
      return invSnapshot;
    }
    return null;
  }

  @SuppressWarnings("unchecked")
  public String process(Long domainId, Results results, String cstReportsExportParamsJson,
                        PersistenceManager pm) throws ProcessingException {
    xLogger.fine(
        "Entered CustomReportsExportProcessor.process. domainId: {0}, cstReportsExportParamsJson: {1}",
        domainId, cstReportsExportParamsJson);
    if (results == null) {
      xLogger.info("Results is null or has zero size");
      return cstReportsExportParamsJson;
    }
    CustomReportsExportParams cstReportsExportParams = null;
    try {
      if (cstReportsExportParamsJson != null && !cstReportsExportParamsJson.isEmpty()) {
        try {
          cstReportsExportParams = new CustomReportsExportParams(cstReportsExportParamsJson);
        } catch (Exception e) {
          xLogger.severe("{0} in domain {1} when parsing exportParams JSON {2}: {3}",
              e.getClass().getName(), domainId, cstReportsExportParamsJson, e.getMessage(), e);
          return cstReportsExportParamsJson;
        }
      }
      if (isGAE) {
        xLogger.info("Exporting {0} more {1} in domain {2}...{3} done so far", results.getSize(),
            cstReportsExportParams.type, domainId, cstReportsExportParams.size);
      } else {
        xLogger.info("Exporting data for domain {0}...{1} done so far", domainId,
            cstReportsExportParams.size);
      }
      // Get domain config.
      DomainConfig dc = DomainConfig.getInstance(domainId);
      List exportables = null;

      // Get the CSV for this result set
      List<String> csvList = new ArrayList<>();
      // List<Exportable> exportables = results.getResults();
      if (CustomReportConstants.TYPE_INVENTORYBATCH.equals(cstReportsExportParams.type)) {
        exportables = BulkExportMgr.getInvntryWithBatchInfoList(results);
      } else {
        exportables = results.getResults();
      }
      final boolean
          isInvSnapshot =
          CustomReportConstants.TYPE_HISTORICAL_INVENTORYSNAPSHOT
              .equals(cstReportsExportParams.type);
      // Add the csvheader
      if (cstReportsExportParams.size == 0 && exportables != null) {
        // Since exportables can be null or empty if the type == inventorybatch, it's important to a null/empty check before getting the CSV Header.
        try {
          exportables.get(0);
        } catch (IndexOutOfBoundsException e) {
          return cstReportsExportParamsJson;
        }
        if (isInvSnapshot) {
          csvList.add(ExportHandlerUtil.getInstance(getInventorySnapshot(
              (IInvntry) exportables.get(0),
              cstReportsExportParams, domainId))
              .getCSVHeader(cstReportsExportParams.locale, dc, null));
        } else {
          csvList.add(ExportHandlerUtil.getInstance(exportables.get(0))
              .getCSVHeader(cstReportsExportParams.locale, dc, null));
        }

      }
      long startTime = System.currentTimeMillis();
      // Append the csv lines
      Iterator it = exportables.iterator();
      while (it.hasNext()) {
        Object exportable = it.next();
        // If customReportsExportParams.type == HISTORICAL_INVENTORY_SNAPSHOT, then exportable is of type Invntry
        // But it has the current snapshot
        // Get the specific day's snapshot using DaySlice, i.e using Invntry and snap shot date, get the InvntrySnapshot object.
        // Then, convert the InvntrySnapshot object to csv.
        if (isInvSnapshot) {
          exportable = getInventorySnapshot((IInvntry) exportable, cstReportsExportParams, domainId);
        }
        if (exportable != null) {
          // Add csv line
          String
              csv = ExportHandlerUtil.getInstance(exportable)
              .toCSV(cstReportsExportParams.locale, cstReportsExportParams.timezone, dc, null);
          if (csv != null && !csv
              .isEmpty()) { // Important bug fix: Empty rows were being added to the report because null csv was being added to csvList.
            // If the exportable is an instance of Order
            if (exportable instanceof IOrder) {
              // Tokenize the csv by DEMANDITEM_DELIMITER and add the tokens to the csvList. Csv contains "\n" in case the exportable
              // is of type Order. In case exportable is an instance of Transaction or Inventory, there is no "\n" in the csv.
              // Tokenize the string based on DEMANDITEM_DELIMITER.
              StringTokenizer st = new StringTokenizer(csv, DEMANDITEM_DELIMITER);
              if (st.countTokens() == 1) {
                // If the number of tokens is 1, which means there is no DEMANDITEM_DELIMITER in the csv,
                // add it to csvList and increment the cstReportsExportParams.size
                csvList.add(csv);
                cstReportsExportParams.size++;
              } else {
                // If there are more than one tokens, then add the tokens to the csvList and increment the cstReportsExportParams.size
                while (st.hasMoreTokens()) {
                  csvList.add(st.nextToken());
                  cstReportsExportParams.size++;
                }
              }
            } else { // If exportable is of type Inventory, Transaction, User, Entity, Material just add the csv to the csvList.
              csvList.add(csv);
              cstReportsExportParams.size++;
            }
            if (!isGAE && cstReportsExportParams.size % 1000 == 0) {
              xLogger.info(
                  "Custom report exporting data in domain {0}...{1} done so far. TimeTaken: {2}",
                  domainId, cstReportsExportParams.size, System.currentTimeMillis() - startTime);
              // Store data, and if necessary
              //   storeExportedData( domainId, csvList, cstReportsExportParams );
              // xLogger.info("Custom report data saved to HDFS for domain {0}...{1} done so far. TimeTaken:{2}", domainId, cstReportsExportParams.size, System.currentTimeMillis() - startTime);
              //csvList.clear();
            }

            if (cstReportsExportParams.size >= 60000) {
              break;
            }
          } // end if csv != null
        } // end if exportable != null
      }
      // Store data, and if necessary
      storeExportedData(csvList, cstReportsExportParams);
      // Return updated export params.

      return cstReportsExportParams.toJSONString();

    } catch (Exception e) {
      JobUtil.setJobFailed(cstReportsExportParams.jobId, e.getMessage());
      throw new ProcessingException(e);
    }
  }

  @Override
  public String getQueueName() {
    return ITaskService.QUEUE_EXPORTER;
  }
}

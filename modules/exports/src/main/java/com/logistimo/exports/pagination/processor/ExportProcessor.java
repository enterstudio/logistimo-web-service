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

package com.logistimo.exports.pagination.processor;

import com.logistimo.AppFactory;
import com.logistimo.domains.entity.IDomain;
import com.logistimo.domains.service.DomainsService;
import com.logistimo.domains.service.impl.DomainsServiceImpl;
import com.logistimo.exports.handlers.ExportHandlerUtil;
import com.logistimo.reports.ReportsConstants;
import com.logistimo.reports.entity.slices.ISlice;
import com.logistimo.reports.generators.ReportData;
import com.logistimo.reports.models.DomainUsageStats;
import com.logistimo.reports.models.UsageStats;
import com.logistimo.services.storage.StorageUtil;
import com.logistimo.services.taskqueue.ITaskService;
import com.logistimo.services.utils.ConfigUtil;

import com.logistimo.config.models.DomainConfig;
import com.logistimo.pagination.Results;
import com.logistimo.pagination.processor.ProcessingException;
import com.logistimo.pagination.processor.Processor;
import com.logistimo.services.Resources;
import com.logistimo.services.Services;
import com.logistimo.exports.BulkExportMgr;
import com.logistimo.exports.BulkExportMgr.ExportParams;
import com.logistimo.constants.CharacterConstants;
import com.logistimo.utils.JobUtil;
import com.logistimo.utils.LocalDateUtil;
import com.logistimo.logger.XLog;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

import javax.jdo.PersistenceManager;

/**
 * Processor to export objects in CSV format
 *
 * @author Arun
 */
public class ExportProcessor implements Processor {

  public static final String DATAEXPORT_BUCKETNAME = "dataexport";
  public static final String SEPARATOR = "_";
  private static final XLog xLogger = XLog.getLog(ExportProcessor.class);
  private static final boolean isGAE = ConfigUtil.isGAE();

  @SuppressWarnings("unchecked")
  private static List customizeExportables(ExportParams exportParams, Results results) {
    List exportables = null;
    try {
      if (BulkExportMgr.TYPE_USAGESTATISTICS.equals(exportParams.type)) {
        // In this case, results.getResults().get( 0 ) is an instance of MonthSlice
        exportables = new ArrayList<>();
        UsageStats usageStats = new UsageStats(results);
        List<DomainUsageStats> domainUsageStatsList = usageStats.getDomainUsageStatsList();
        // Iterate through the list of DomainUsageStats objects and put each of them into the list of exportables.
        Iterator<DomainUsageStats> domainUsageStatsListIter = domainUsageStatsList.iterator();
        while (domainUsageStatsListIter.hasNext()) {
          Object exp = domainUsageStatsListIter.next();
          exportables.add(exp);
        }
      } else if (results.getResults().get(0) instanceof ISlice) {
        exportables = new ArrayList<>();
        xLogger.fine("exportParams.locale: {0}", exportParams.locale);
        ReportData
            reportData =
            ReportData.getInstance(exportParams.domainId, exportParams.type, results.getResults(),
                exportParams.locale, exportParams.timezone, exportParams.from,
                exportParams.to); // Earlier domainId
        exportables.add(reportData);
      } else if (BulkExportMgr.TYPE_INVENTORYBATCH.equals(exportParams.type)) {
        exportables = BulkExportMgr.getInvntryWithBatchInfoList(results);
      } else if (BulkExportMgr.TYPE_DISCREPANCIES.equals(exportParams.type)) {
        exportables = BulkExportMgr.getDiscrepancyExportableModels(results);
      } else if (BulkExportMgr.TYPE_ORDERS.equals(exportParams.type)) {
        exportables = BulkExportMgr.getOrdersWithItems(results);
      } else {
        exportables = results.getResults();
      }
    } catch (IndexOutOfBoundsException ignored) {
      xLogger.warn("Exception while trying to customize exportables", ignored);
    }
    return exportables;
  }

  // Store data in Google Cloud Storage, and return the name of the file in GCS, which contains the exported data.
  // NOTE: We don't finalize the blob file here (defer it to the finalize operation)
  private static void storeExportedData(Long domainId, String data, ExportParams exportParams) {
    xLogger.fine("Entered storeExportedData");
    ResourceBundle messages = null;
    String domainName = "";
    DomainsService ds;
    try {
      ds = Services.getService(DomainsServiceImpl.class, null);
      IDomain d = ds.getDomain(domainId);
      domainName = d.getName();
    } catch (Exception e) {
      xLogger.warn("Domain Name is null " + domainId + e);

    }

    if (data == null || data.isEmpty()) {
      return;
    }
    xLogger.fine("exportParams.gcsFilename = {0}", exportParams.gcsFilename);

    if (exportParams.gcsFilename == null) {
      messages = Resources.get().getBundle("Messages", Locale.ENGLISH);
      // Change the type only if exportParams.type is ReportsConstants.TYPE_STOCKEVENT. This is done so that the user gets an email with
      // the title Abnormal Stock instead of Stock Event. Also the csv file is called abnormalstock_<date>.csv instead of stev_<date>.csv
      String type = exportParams.type;
      if (ReportsConstants.TYPE_STOCKEVENT.equals(exportParams.type)) {
        type = BulkExportMgr.TYPE_ABNORMALSTOCK;
      }
      // Get the filename, which is the name of the file that has the exported data, that will be sent as attachment.
      // String filename = LocalDateUtil.getNameWithDate( exportParams.type, new Date(), exportParams.locale, exportParams.timezone ) + ".csv";
      String filename;
      if (exportParams.type.equals("powerdata")) {
        type =
            "Temperature_powerdata_device_" + exportParams.assetId + "_sensor_"
                + exportParams.sensorName;
      }
      filename =
          LocalDateUtil.getNameWithDate(
              "kiosks".equals(type) ? messages.getString("kiosks.lowercase") : type, new Date(),
              exportParams.locale, exportParams.timezone) + ".csv";
      // Prepend the filename with domainId string. This is done to maintain unique export file names across domains in Google Cloud Storage.
      String
          gcsFilename =
          storeFileInGCS(data, (domainName + SEPARATOR + filename), false, exportParams);
      // Update export params - filename and gcsFilename
      exportParams.filename = filename;

      if (gcsFilename != null) {
        exportParams.gcsFilename = gcsFilename;
      } else {
        xLogger.warn(
            "Invalid GCSFilename (NULL) returned when trying to store exported data file {0} in domain {1}",
            gcsFilename, domainId);
      }
    } else { // Just append to the existing file in Google Cloud Storage(referenced by gcsFilename)
      storeFileInGCS(data, exportParams.gcsFilename, true, exportParams);
    }
    xLogger.fine("Exiting storeExportedData");
  }

  // Store the data in Google Cloud Storage. filename is the name of the GCS file.
  // appendMode true indicates that an existing GCS file is updated.
  // appendMode false indicates that the GCS file is newly created.
  private static String storeFileInGCS(String data, String filename, boolean appendMode,
                                       ExportParams exportParams) {
    xLogger.fine("Entering storeFileInGCS");
    if (data == null || data.isEmpty() || filename == null || filename.isEmpty()) {
      return null;
    }
    // Update the JobStatus table
    JobUtil.setNumberOfRecordsCompleted(exportParams.jobId, exportParams.size);

    StorageUtil storageUtil = AppFactory.get().getStorageUtil();
    // If appendMode is true, append the data to the file in GCS
    try {
      if (appendMode) {
        storageUtil.append(DATAEXPORT_BUCKETNAME, filename, data);
      } else {
        // Create the file in GCS and write data to it.
        storageUtil.write(DATAEXPORT_BUCKETNAME, filename, data);
      }
    } catch (Exception e) {
      xLogger.severe(
          "{0} when " + (appendMode ? "appending" : "writing") + " to GCS file {1}. Message: {2}",
          e.getClass().getName(), filename, e.getMessage(), e);
      JobUtil.setJobFailed(exportParams.jobId, e.getMessage());
    }
    xLogger.fine("Exiting storeFileInGCS");
    return filename;
  }

  @Override
  public String process(Long domainId, Results results, String exportParamsJson,
                        PersistenceManager pm) throws ProcessingException {
    xLogger.fine("Entered ExportProcessor.process");
    if (results == null) {
      return exportParamsJson;
    }
    ExportParams exportParams = null;
    try {
      if (exportParamsJson != null && !exportParamsJson.isEmpty()) {
        try {
          exportParams = new ExportParams(exportParamsJson);
        } catch (Exception e) {
          xLogger.severe("{0} in domain {1} when parsing exportParams JSON {2}: {3}",
              e.getClass().getName(), domainId, exportParamsJson, e.getMessage(), e);
          return exportParamsJson;
        }
      }

      // Get exportables; if Slices, get the exportables (i.e. ReportData) from the slices
      List exportables = null;
      exportables = customizeExportables(exportParams, results);
      if (exportables == null) {
        return exportParamsJson;
      }
      xLogger
            .info("Exporting data in domain {0}...{1} done so far.", domainId, exportParams.size);

      long startTime = System.currentTimeMillis();
      // Get domain config.
      DomainConfig dc = DomainConfig.getInstance(domainId);
      // Get the CSV for this result set
      StringBuilder csv = new StringBuilder();
      if (exportParams.gcsFilename == null) {
        // Since exportables can be null or empty if type == inventorybatch, do null check before getting the CSV header
        csv.append(ExportHandlerUtil.getInstance(exportables.get(0))
            .getCSVHeader(exportParams.locale, dc, exportParams.subType));
      }
      Iterator it = exportables.iterator();
      while (it.hasNext()) {
        Object exportable = it.next();
        String
            line =
            ExportHandlerUtil.getInstance(exportable).toCSV(exportParams.locale,
                exportParams.timezone, dc, exportParams.subType);
        // Add csv line
        if (line != null && !line.isEmpty()) {
          csv.append("\n");
          if ("powerdata".equals(exportParams.type)) {
            csv.append(exportParams.assetId).append(CharacterConstants.COMMA)
                .append(exportParams.asseTyNm).append(CharacterConstants.COMMA)
                .append(exportParams.sensorName).append(CharacterConstants.COMMA);
          }
          csv.append(line);
          exportParams.size++;
        }
        if (!isGAE && exportParams.size % 1000 == 0) {
          xLogger.info("Exporting data in domain {0}...{1} done so far. TimeTaken: {2}", domainId,
              exportParams.size, System.currentTimeMillis() - startTime);
          // Store data, and update gcsFileName, if necessary
          storeExportedData(domainId, csv.toString(), exportParams);
          xLogger
              .info("Data saved to HDFS for domain {0}...{1} done so far. TimeTaken:{2}", domainId,
                  exportParams.size, System.currentTimeMillis() - startTime);
          csv.setLength(0);
        }/*
                if (exportParams.size == 5) {
                    throw new ProcessingException("Simulating an Export failure case" );
                }*/
      }
      if (!isGAE) {
        xLogger.info("Exporting data in domain {0} completed. Total rows {1}. TotalTimeTaken:{2}",
            domainId, exportParams.size, System.currentTimeMillis() - startTime);
      }
      // Store data, and update gcsFileName, if necessary
      storeExportedData(domainId, csv.toString(), exportParams);
      xLogger.fine("Exiting ExportProcessor.process");
      // Return updated export params.
      try {
        return exportParams.toJSONString();
      } catch (Exception e) {
        throw new ProcessingException(e);
      }
    } catch (ProcessingException e) {
      JobUtil.setJobFailed(exportParams.jobId, e.getMessage());
      throw e;
    }
  }

  @Override
  public String getQueueName() {
    return ITaskService.QUEUE_EXPORTER;
  }
}

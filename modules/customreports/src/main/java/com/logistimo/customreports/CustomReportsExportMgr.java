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

import com.logistimo.AppFactory;
import com.logistimo.config.models.CustomReportsConfig;
import com.logistimo.config.models.CustomReportsConfig.Config;
import com.logistimo.config.models.DomainConfig;
import com.logistimo.constants.CharacterConstants;
import com.logistimo.constants.Constants;
import com.logistimo.constants.QueryConstants;
import com.logistimo.customreports.utils.SpreadsheetUtil;
import com.logistimo.dao.JDOUtils;
import com.logistimo.domains.entity.IDomain;
import com.logistimo.domains.service.DomainsService;
import com.logistimo.domains.service.impl.DomainsServiceImpl;
import com.logistimo.entities.entity.IKiosk;
import com.logistimo.entities.service.EntitiesService;
import com.logistimo.entities.service.EntitiesServiceImpl;
import com.logistimo.entity.IJobStatus;
import com.logistimo.entity.IUploaded;
import com.logistimo.exception.InvalidDataException;
import com.logistimo.inventory.entity.IInvntry;
import com.logistimo.inventory.entity.ITransaction;
import com.logistimo.logger.XLog;
import com.logistimo.materials.entity.IMaterial;
import com.logistimo.materials.service.MaterialCatalogService;
import com.logistimo.materials.service.impl.MaterialCatalogServiceImpl;
import com.logistimo.mnltransactions.entity.IMnlTransaction;
import com.logistimo.orders.entity.IOrder;
import com.logistimo.pagination.QueryParams;
import com.logistimo.reports.ReportsConstants;
import com.logistimo.reports.dao.IReportsDao;
import com.logistimo.services.ServiceException;
import com.logistimo.services.Services;
import com.logistimo.services.UploadService;
import com.logistimo.services.blobstore.BlobInfo;
import com.logistimo.services.blobstore.BlobKey;
import com.logistimo.services.blobstore.BlobstoreService;
import com.logistimo.services.impl.UploadServiceImpl;
import com.logistimo.services.taskqueue.ITaskService;
import com.logistimo.tags.TagUtil;
import com.logistimo.tags.entity.ITag;
import com.logistimo.users.UserUtils;
import com.logistimo.users.entity.IUserAccount;
import com.logistimo.utils.JobUtil;
import com.logistimo.utils.LocalDateUtil;
import com.logistimo.utils.StringUtil;

import org.apache.commons.lang.StringUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public class CustomReportsExportMgr {
  public static final String CUSTOMREPORTS_BUCKETNAME = "customreports";
  private static final XLog xLogger = XLog.getLog(CustomReportsExportMgr.class);
  private static ITaskService taskService = AppFactory.get().getTaskService();
  private static BlobstoreService blobstoreService = AppFactory.get().getBlobstoreService();

  public static void handleCustomReportsExport(Long domainId, String reportName, Long jobId) {
    xLogger.fine("Entering handleCustomReportsExport, domainId: {0}, reportName: {1}", domainId,
        reportName);
    // Get the DomainConfig instance
    DomainConfig dc = DomainConfig.getInstance(domainId);
    boolean scheduleExportNow = false;
    // Get the Custom Reports Configuration from domain config
    CustomReportsConfig crc = dc.getCustomReportsConfig();
    String fileName = null;
    String exportedFileName = null;
    List<CustomReportsConfig.Config> customReportsConfig = null;
    boolean isSuccess = false;
    if (crc != null) {
      if (reportName != null && !reportName.isEmpty()) {
        Config c = crc.getConfig(reportName);
        if (c != null) {
          customReportsConfig = new ArrayList<Config>();
          customReportsConfig.add(c);
          scheduleExportNow = true;
        }
      } else {
        // Get the list of Custom report Config objects
        customReportsConfig = crc.getCustomReportsConfig();
      }
      if (customReportsConfig == null || customReportsConfig.isEmpty()
          || customReportsConfig.size() == 0) {
        xLogger.info("No custom report templates configured for domain {0}", domainId);
        return;
      }

      // If custom reports are configured, iterate through them.
      Iterator<Config> customReportsConfigIter = customReportsConfig.iterator();
      while (customReportsConfigIter.hasNext()) {
        Config config = customReportsConfigIter.next();
        if (config != null) {
          // Get the reporting template.
          String templateKey = config.templateKey;
          String templateName = config.templateName;
          if (templateKey == null || templateKey.isEmpty() || templateName == null || templateName
              .isEmpty()) {
            xLogger.warn(
                "Invalid or null templateName and templateKey in domain {0}. templateName: {1}, templateKey: {2}",
                domainId, templateName, templateKey);
            continue;
          }

          // Get the uploaded object using templateKey
          try {
            UploadService us = Services.getService(UploadServiceImpl.class);
            IUploaded uploaded = us.getUploaded(templateKey);
            if (uploaded == null) {
              xLogger.severe(
                  "Failed to get uploaded object for templateName {0} with templateKey {1} in domain {2}",
                  templateName, templateKey, domainId);
              continue;
            }

            // Get the blob key string
            BlobKey blobKeyStr = new BlobKey(uploaded.getBlobKey());
            if (blobKeyStr == null || blobKeyStr.isEmpty()) {
              xLogger.severe(
                  "Failed to get blobKeyStr for templateName {0} with templateKey {1} in domain {2}",
                  templateName, templateKey, domainId);
              continue;
            }

            // Get the blobKey from the blobKeyStr

            BlobInfo blobInfo = blobstoreService.getBlobInfo(blobKeyStr.getKeyString());
            if (blobInfo == null) {
              xLogger.severe(
                  "Failed to create BlobInfo object from blobKey {0} for templateName {1} with templateKey {2} in domain {3}",
                  blobKeyStr, templateName, templateKey, domainId);
              continue;
            }

            String blobFileName = blobInfo.getFilename();
            String fileExtension = getBlobFileExtension(blobFileName);
            if (fileExtension == null || fileExtension.isEmpty()) {
              xLogger
                  .severe("Invalid blobFileName {0} for template {1} in domain {2}", blobFileName,
                      templateName, domainId);
              continue; // Continue the while loop to iterate through the list of customReportsConfig objects
            }
            // If the template has any other extension other than xls or xlsx, then log an error message and continue the loop.
            if (!(CustomReportConstants.EXTENSION_XLSX.equalsIgnoreCase(fileExtension)
                || CustomReportConstants.EXTENSION_XLS.equalsIgnoreCase(fileExtension)
                || CustomReportConstants.EXTENSION_XLSM.equalsIgnoreCase(fileExtension))) {
              xLogger
                  .severe("Invalid blobFileName {0} for template {1} in domain {2}", blobFileName,
                      templateName, domainId);
              continue; // Continue the while loop to iterate through the list of customReportsConfig objects
            }
            // Get a BlobstoreInputStream using blobKey
            InputStream
                bis =
                AppFactory.get().getStorageUtil().readAsBlobstoreInputStream(blobKeyStr);
            if (bis == null) {
              xLogger.severe("Failed to get BlobstoreInputStream for template {0} in domain {1}",
                  templateName, domainId); // Log a message for that template and continue the loop
              continue; // Continue the loop
            }

            boolean scheduleExport = false;
            DateRange dateRange = null;
            // For every template, get a map of the type to the sheetdata
            Map<String, Map<String, String>> typeSheetdataMap = getTypeSheetdataMap(config);
            xLogger.info("typeSheetdataMap for domain {0} for template {1} is {2}", domainId,
                templateName, typeSheetdataMap);
            if (typeSheetdataMap == null || typeSheetdataMap.isEmpty()) {
              // This usually does not occur because during upload of a template, at least one type and the corresponding sheet data
              // has to be specified.
              xLogger.severe(
                  "Invalid or null typeSheetdataMap for template {0} in domain {1}. Reason: No types specified in the configuration",
                  templateName, domainId);
              continue;
            }
            // Get the typeSheetnameMap from the typeSheetdataMap. This is used by SpreadsheetUtil.clearSheets method
            Map<String, String> typeSheetnameMap = getTypeSheetnameMap(typeSheetdataMap);
            xLogger.fine("typeSheetnameMap for domain {0} for template {1} is {2}", domainId,
                templateName, typeSheetnameMap);
            if (typeSheetnameMap == null || typeSheetnameMap.isEmpty()) {
              // This usually does not occur because during upload of a template, at least one type and the corresponding sheet name
              // has to be specified.
              xLogger.severe(
                  "Invalid or null typeSheetnameMap for template {0} in domain {1}. Reason: No types specified in the configuration",
                  templateName, domainId);
              continue;
            }

            String
                fileNameWithDomainName =
                getFileNameWithDomainName(templateName, domainId, dc.getLocale());
            String notificationSubject = null;
            String body = "";
            xLogger.fine("fileNameWithDomainName: {0}", fileNameWithDomainName);
            // ExportType
            String exportType = null;
            String exportTime = null;

            // Daily Export
            String dailyTime = config.dailyTime;
            if (dailyTime != null && !dailyTime.isEmpty()) {
              // Daily reporting is set.
              exportType = CustomReportConstants.FREQUENCY_DAILY;
              exportTime = config.dailyTime;
            }
            // Weekly Export
            int dayOfTheWeek = config.dayOfWeek;
            if (dayOfTheWeek != 0) {
              // Weekly reporting is set.
              exportType = CustomReportConstants.FREQUENCY_WEEKLY;
              exportTime = config.weeklyRepGenTime;
            }
            xLogger.fine("dayOfTheWeek: {0}", dayOfTheWeek);
            // Monthly Export
            int dayOfTheMonth = config.dayOfMonth;
            xLogger.fine("dayOfTheMonth: {0}", dayOfTheMonth);
            if (dayOfTheMonth != 0) {
              // Monthly export is set.
              exportType = CustomReportConstants.FREQUENCY_MONTHLY;
              exportTime = config.monthlyRepGenTime;
            }

            // Get the typeDateRangeMap from typeSheetdataMap.
            Map<String, DateRange>
                typeDateRangeMap =
                getTypeDateRangeMap(config, exportType, exportTime, typeSheetdataMap, dc);
            xLogger.fine("typeDateRangeMap: {0}", typeDateRangeMap);
            if (typeDateRangeMap == null || typeDateRangeMap.isEmpty()) {
              // This usually does not occur.
              xLogger.severe("Invalid or null typeDateRangeMap for template {0} in domain {1}",
                  templateName, domainId);
              continue;
            }

            // Get the DateRange for the first element in the map.
            Collection<DateRange> dateRanges = typeDateRangeMap.values();
            Iterator<DateRange> dateRangesIter = dateRanges.iterator();
            dateRange = dateRangesIter.next(); // Get the first element in the collection

            if (dateRange != null) {
              Calendar toDate = dateRange.to;
              if (toDate != null) {
                if (CustomReportConstants.FREQUENCY_DAILY.equals(exportType)) {
                  scheduleExport = true;
                } else if (CustomReportConstants.FREQUENCY_WEEKLY.equals(exportType)) {
                  // dayOfWeek in config matches with toDate's day of the week then schedule export
                  if (toDate.get(Calendar.DAY_OF_WEEK) == dayOfTheWeek) {
                    scheduleExport = true;
                  }
                } else if (CustomReportConstants.FREQUENCY_MONTHLY.equals(exportType)) {
                  if (dayOfTheMonth == toDate.get(Calendar.DAY_OF_MONTH)) {
                    scheduleExport = true;
                  }
                } // end if export type is monthly

                if (scheduleExport || scheduleExportNow) {
//									SimpleDateFormat sdf = new SimpleDateFormat(Constants.DATE_FORMAT_CUSTOMREPORT);
                  String
                      formattedToDate =
                      LocalDateUtil
                          .formatCustom(toDate.getTime(), Constants.DATE_FORMAT_CUSTOMREPORT,
                              dc.getTimezone());
                  fileName = fileNameWithDomainName + "_" + formattedToDate;
                  notificationSubject =
                      getNotificationSubject(exportType, templateName, dateRange, dc.getLocale(),
                          dc.getTimezone(), domainId);
                  if (config.description != null) {
                    body = config.description;
                  }
                }
              }
            }

            if (scheduleExport || scheduleExportNow) {
              // Before scheduling an export, clear the sheets.
              // Empty the relevant sheets from the template. Return the file name of the gcs file which is config.fileName + time stamp at which the sheets were cleared.
              exportedFileName =
                  SpreadsheetUtil
                      .clearSheets(templateName, fileName, fileExtension, typeSheetnameMap, bis,
                          dc.getLocale(), dc.getTimezone());
              xLogger.fine("exportedFileName: {0}", exportedFileName);
              if (exportedFileName != null && !exportedFileName.isEmpty()
                  && notificationSubject != null && !notificationSubject.isEmpty()) {
                xLogger.fine("dateRange: from: {0}, to: {1}", dateRange.from, dateRange.to);
                processScheduledExport(domainId, config, typeDateRangeMap, typeSheetdataMap,
                    exportedFileName, notificationSubject, scheduleExportNow, exportTime, body,
                    jobId);
                isSuccess = true;
              } else {
                xLogger.severe("Did not schedule a custom report export");
              }
            } // End if scheduleExport is true
          } catch (Exception e) {
            xLogger.severe(
                "{0} while getting uploaded object for template {1} with templateKey {2} in domain {3}, Message: {4}",
                e.getClass().getName(), templateName, templateKey, domainId, e.getMessage(), e);
          }

        } // End if config != null
      } // End while iterating through the list of CustomReportConfig objects
    } else {
      xLogger.info("CustomReportsConfiguration for domain {0} is null", domainId);
    }

    // If not found, respond with error message
    if (!isSuccess && jobId != null) {
      // The template was not found. Continue with the next template.
      JobUtil.setJobFailed(jobId, "Failed to schedule custom reports");
    }

    xLogger.fine("Exiting handleCustomReportsExport");
  }

  // Private function to get the extension of the blob file.
  private static String getBlobFileExtension(String blobFileName) {
    xLogger.fine("Entering getBlobFileExtension");
    String fileExtension = null;
    if (blobFileName != null && !blobFileName.isEmpty()) {
      int index = blobFileName.lastIndexOf(".");
      if (index != -1) {
        fileExtension = blobFileName.substring(index);
      }
    }
    xLogger.fine("Exiting getBlobFileExtension. {0}", fileExtension);
    return fileExtension;
  }


  // Private method to get a copy of the config.typeSheetdataMap
  private static Map<String, Map<String, String>> getTypeSheetdataMap(Config config) {
    xLogger.fine("Entering getTypeSheetdataMap");
    Map<String, Map<String, String>> configTypeSheetDataMap = config.typeSheetDataMap;
    Map<String, Map<String, String>> typeSheetdataMap = null;
    if (configTypeSheetDataMap != null && !configTypeSheetDataMap.isEmpty()
        && configTypeSheetDataMap.size() != 0) {
      typeSheetdataMap = new LinkedHashMap<>();
      typeSheetdataMap.putAll(configTypeSheetDataMap);
    }
    xLogger.fine("Exiting getTypeSheetdataMap");
    return typeSheetdataMap;
  }

  // Private function to get a map of the type to the sheet name, given a map of type to the sheet data
  private static Map<String, String> getTypeSheetnameMap(
      Map<String, Map<String, String>> typeSheetdataMap) {
    xLogger.fine("Entering typeSheetnameMap");
    if (typeSheetdataMap == null || typeSheetdataMap.isEmpty() || typeSheetdataMap.size() == 0) {
      xLogger.severe("Invalid or null typeSheetdataMap");
      return null;
    }
    Map<String, String> typeSheetnameMap = new LinkedHashMap<>();
    // Get the keys in the typeSheetdataMap and iterate over the keys
    Set<String> typeSheetdataMapKeys = typeSheetdataMap.keySet();
    Iterator<String> typeSheetdataMapKeysIter = typeSheetdataMapKeys.iterator();
    while (typeSheetdataMapKeysIter.hasNext()) {
      String type = typeSheetdataMapKeysIter.next();
      Map<String, String> sheetData = typeSheetdataMap.get(type);
      String sheetName = null;
      if (sheetData != null) {
        if (sheetData.containsKey(CustomReportsConfig.SHEETNAME)) {
          sheetName = sheetData.get(CustomReportsConfig.SHEETNAME);
        }
      }
      if (sheetName != null && !sheetName.isEmpty()) {
        typeSheetnameMap.put(type, sheetName);
      }
    }
    xLogger.fine("Exiting typeSheetnameMap");
    return typeSheetnameMap;
  }

  // Private function to get a map of type to DateRange given a map of type to the sheet data.
  private static Map<String, DateRange> getTypeDateRangeMap(Config config, String exportType,
                                                            String exportTime,
                                                            Map<String, Map<String, String>> typeSheetdataMap,
                                                            DomainConfig dc) {
    xLogger.fine("Entering getTypeDateRangeMap");
    if (typeSheetdataMap == null || typeSheetdataMap.isEmpty() || typeSheetdataMap.size() == 0) {
      xLogger.severe("Invalid or null typeSheetdataMap");
      return null;
    }

    Map<String, DateRange> typeDateRangeMap = new LinkedHashMap<>();
    // Get the keys in the typeSheetdataMap and iterate over the keys
    Set<String> typeSheetdataMapKeys = typeSheetdataMap.keySet();
    Iterator<String> typeSheetdataMapKeysIter = typeSheetdataMapKeys.iterator();

    while (typeSheetdataMapKeysIter.hasNext()) {
      String type = typeSheetdataMapKeysIter.next();
      Map<String, String> sheetData = typeSheetdataMap.get(type);
      DateRange dateRange = null;
      // If type is inventory/users/entities/materials, get Date Range with default values. Because from date does not matter here.
      // If type is orders/transactions/mnltransactions/transactioncounts/inventorytrends/historicalinventorysnapshot get Date Range based on the sheet data keys
      if (sheetData != null) {
        int dataDuration = 0;
        if (CustomReportConstants.TYPE_INVENTORY.equals(type) || CustomReportConstants.TYPE_INVENTORYBATCH.equals(type) || CustomReportConstants.TYPE_USERS
            .equals(type) || CustomReportConstants.TYPE_ENTITIES.equals(type) || CustomReportConstants.TYPE_MATERIALS.equals(type)) {
          dateRange = getDateRange(exportTime, exportType, true, dataDuration, CustomReportConstants.FREQUENCY_DAILY, dc);
        } else if (
            CustomReportConstants.TYPE_ORDERS.equalsIgnoreCase(type) || CustomReportConstants.TYPE_TRANSACTIONS.equals(type)
            || CustomReportConstants.TYPE_MANUALTRANSACTIONS.equals(type) || CustomReportConstants.TYPE_TRANSACTIONCOUNTS.equals(type)
            || CustomReportConstants.TYPE_INVENTORYTRENDS.equals(type)) {
          String
              dataDurationSameAsRepGen =
              sheetData.get(CustomReportsConfig.DATA_DURATION_SAMEAS_REPGENFREQ);
          if (CustomReportsConfig.FALSE.equals(dataDurationSameAsRepGen)) {
            dataDuration = Integer.parseInt(sheetData.get(CustomReportsConfig.DATA_DURATION));
          }
          if (CustomReportConstants.TYPE_ORDERS.equalsIgnoreCase(type) || CustomReportConstants.TYPE_TRANSACTIONS.equals(type)
              || CustomReportConstants.TYPE_MANUALTRANSACTIONS.equals(type)) {
            dateRange =
                getDateRange(exportTime, exportType, Boolean.parseBoolean(dataDurationSameAsRepGen),
                    dataDuration, CustomReportConstants.FREQUENCY_DAILY, dc);
          } else if (
              CustomReportConstants.TYPE_TRANSACTIONCOUNTS.equals(type) || CustomReportConstants.TYPE_INVENTORYTRENDS.equals(type)) {
            String aggregateFreq = sheetData.get(CustomReportsConfig.AGGREGATEFREQ);
            dateRange =
                getDateRange(exportTime, exportType, Boolean.parseBoolean(dataDurationSameAsRepGen),
                    dataDuration, aggregateFreq, dc);
          }
        } else if (CustomReportConstants.TYPE_HISTORICAL_INVENTORYSNAPSHOT.equals(type)) {
          dataDuration = Integer.parseInt(sheetData.get(CustomReportsConfig.DATA_DURATION));
          dateRange =
              getDateRange(exportTime, exportType, false, dataDuration, CustomReportConstants.FREQUENCY_DAILY, dc);
        }
      }
      if (dateRange != null) {
        typeDateRangeMap.put(type, dateRange);
      }
    }

    return typeDateRangeMap;
  }

  // Private method to get the notification subject, ie. subject of the email that shoudl be sent after the export
  private static String getNotificationSubject(String exportType, String templateName,
                                               DateRange dateRange, Locale locale, String timezone,
                                               Long domainId) {
    xLogger.fine("Entering getNotificationSubject");
    String subject = "";
    String domainName = null;
    DomainsService ds = null;
    if (domainId != null) {
      try {
        ds = Services.getService(DomainsServiceImpl.class, locale);
        IDomain d = ds.getDomain(domainId);
        if (d != null) {
          domainName = d.getName();
        }
      } catch (Exception e) {
        xLogger.severe("{0} while domainName for template {1} in domain {2}. Message: {3}",
            e.getClass().getName(), templateName, domainId, e.getMessage());
      }
    }
    if (domainName != null && !domainName.isEmpty()) {
      if (exportType != null && !exportType.isEmpty() && templateName != null && !templateName
          .isEmpty() && dateRange.to != null) {
        Calendar toCal = dateRange.to;
        // Get the Date from toCal and fromCal
        Date toDate = toCal.getTime();
        String
            formattedToDate =
            LocalDateUtil.formatCustom(toDate, Constants.DATE_FORMAT_CUSTOMREPORT, timezone);
        subject += templateName + " for " + domainName + " as on " + formattedToDate;
      }
    }
    xLogger.fine("Exiting getNotificationSubject");
    return subject;
  }

  // Private function to schedule export task.
  private static void processScheduledExport(Long domainId, Config config,
                                             Map<String, DateRange> typeDateRangeMap,
                                             Map<String, Map<String, String>> typeSheetdataMap,
                                             String fileName, String notificationSubject,
                                             boolean scheduleExportNow, String exportTime,
                                             String body, Long jobId) {
    xLogger.fine("Entering processScheduledExport");
    // Schedule the export task
    scheduleExportTask(domainId, config, typeDateRangeMap, typeSheetdataMap, fileName,
        notificationSubject, scheduleExportNow, exportTime, body, jobId);
    xLogger.fine("Exiting processScheduledExport");
  }

  // Private method to schedule export task.
  private static void scheduleExportTask(Long domainId, Config config,
                                         Map<String, DateRange> typeDateRangeMap,
                                         Map<String, Map<String, String>> typeSheetdataMap,
                                         String fileName, String notificationSubject,
                                         boolean scheduleExportNow, String exportTime, String body,
                                         Long jobId) {
    xLogger.fine(
        "Entering scheduleExportTask. domainId: {0}, dateRange: {1}, typeSheetdataMap: {2}, fileName: {3}",
        domainId, typeDateRangeMap, typeSheetdataMap, fileName);
    String url = CustomReportConstants.CUSTOMREPORTS_EXPORT_TASK_URL;
    Map<String, String> params = new HashMap<String, String>();
    params.put("action", CustomReportConstants.ACTION_EXPORT);
    String typeForFirstRun = getTypeForFirstRun(typeSheetdataMap);
    xLogger.fine("typeForFirstRun: {0}", typeForFirstRun);
    Map<String, String> sheetData = typeSheetdataMap.get(typeForFirstRun);
    DateRange dateRange = typeDateRangeMap.get(typeForFirstRun);
    String sheetName = null;
    String aggregateFreq = CustomReportConstants.FREQUENCY_DAILY;
    String filterBy = null;
    if (sheetData != null && !sheetData.isEmpty()) {
      sheetName = sheetData.get(CustomReportsConfig.SHEETNAME);
      if (CustomReportConstants.TYPE_TRANSACTIONCOUNTS.equals(typeForFirstRun)
          || CustomReportConstants.TYPE_INVENTORYTRENDS.equals(typeForFirstRun)) {
        filterBy = sheetData.get(CustomReportsConfig.FILTERBY);
        aggregateFreq = sheetData.get(CustomReportsConfig.AGGREGATEFREQ);
      }
    }
    if (sheetName == null) {
      // Usually doesn't occur, because the sheetName is always present in the sheetData.
      xLogger.severe(
          "Not scheduling export task for template {0} in domain {1}. Reason: Invalid or null sheetName",
          config.templateName, domainId);
      return;
    }
    params.put("type", typeForFirstRun);
    params.put("domainid", domainId.toString());
    boolean recepients = false;
    // Get the list of users from config
    String userIdsCSV = getUserIdsCSV(domainId, config);
    if (StringUtils.isNotEmpty(userIdsCSV)) {
      params.put("userids", userIdsCSV);
      recepients = true;
    }
    if (config.extUsers != null && !config.extUsers.isEmpty()) {
      params.put("emids", StringUtil.getCSV(config.extUsers));
      recepients = true;
    }
    if (!recepients) {
      xLogger.severe(
          "Not scheduling export task for template {0} in domain {1}. Reason: invalid or no users defined",
          config.templateName, domainId);
      return;
    }
    params.put("filename", fileName);
    params.put("templatename", config.templateName);
    params.put("sheetname", sheetName);
    // Add the type specific parameter here. Only when type is transactioncounts, parameters "aggregatefreq" and "filterby" should be added
    if (CustomReportConstants.TYPE_TRANSACTIONCOUNTS.equals(typeForFirstRun)
        || CustomReportConstants.TYPE_INVENTORYTRENDS.equals(typeForFirstRun)) {
      params.put("filterby", filterBy);
      params.put("aggregatefreq", aggregateFreq);
    }
    params.put("subject", notificationSubject);
    params.put("body", body);

    // Remove the first type from the typeSheetdataMap
    typeSheetdataMap.remove(typeForFirstRun);
    // Remove the first type from the typeDateRangeMap
    typeDateRangeMap.remove(typeForFirstRun);

    // Get the scheduling ETA
    long etaMillis = -1;
    String from = null, to = null;
    if (dateRange != null && dateRange.to != null) {
      to = LocalDateUtil.formatCustom(dateRange.to.getTime(), Constants.DATETIME_FORMAT, null);
      params.put("to", to);
      etaMillis = getScheduleETA(exportTime);
      if (etaMillis < (new Date()).getTime()
          || scheduleExportNow) // if schedule time is lesser than current time, run task now
      {
        etaMillis = -1;
      }
    }
    if (dateRange != null && dateRange.from != null) {
      from = LocalDateUtil.formatCustom(dateRange.from.getTime(), Constants.DATETIME_FORMAT, null);
      params.put("from", from);
    }
    if (jobId == null) {
      jobId =
          JobUtil.createJob(domainId, null, null, IJobStatus.TYPE_CUSTOMREPORT, config.templateName,
              params, etaMillis <= 0 ? IJobStatus.INPROGRESS : IJobStatus.INQUEUE);
    } else {
      JobUtil.setJobMetaInfo(jobId,
          params); // Need to do this because at the time of creation of the job, metadatainfo was not available.
    }
    params.put("jobid", jobId.toString());

    // Schedule task with eta
    String action = CustomReportConstants.ACTION_EXPORT;
    try {
      //Put transaction at first so that it will be executed at last to avoid reading big stream
      Map<String, Map<String, String>> reverseList = new LinkedHashMap<>();
      List<String> reverseKey = new ArrayList<>(typeSheetdataMap.size());
      reverseKey.addAll(typeSheetdataMap.keySet());
      Collections.reverse(reverseKey);
      for (String s : reverseKey) {
        reverseList.put(s, typeSheetdataMap.get(s));
      }
      String
          furl =
          URLEncoder.encode(
              getFinalizerUrl(domainId, action, config, reverseList, typeDateRangeMap, fileName,
                  notificationSubject, body, jobId), "UTF-8");
      xLogger.fine("furl: {0}", furl);
      if (furl != null && !furl.isEmpty()) {
        params.put("furl", furl);
      }
      params.put("fqueue", ITaskService.QUEUE_EXPORTER);
      xLogger.fine("Before calling TaskScheduler.schedule: params:{0}", params.toString());
      // DEBUG
      // TaskScheduler.schedule( TaskScheduler.QUEUE_EXPORTER, url, params, TaskScheduler.METHOD_POST );
      taskService
          .schedule(ITaskService.QUEUE_EXPORTER, url, params, null, null, ITaskService.METHOD_POST,
              etaMillis, domainId, null, "CUSTOMREPORT_EXPORT");
    } catch (Exception e) {
      xLogger.warn(
          "{0} when scheduling transaction data export task for template {1} in domain {2}: {3}",
          e.getClass().getName(), config.templateName, domainId, e.getMessage());
    }
    xLogger.fine("Exiting scheduleExportTask");
  }

  private static long getScheduleETA(String exportTime) {
    Calendar toCal = GregorianCalendar.getInstance();
    toCal.setTime(new Date());
    LocalDateUtil.resetTimeFields(toCal);
    // Get hour/min.
    String[] timeOfDay = exportTime.split(":");
    try {
      toCal.set(Calendar.SECOND, 0);
      toCal.set(Calendar.MILLISECOND, 0);
      if (timeOfDay.length >= 1) {
        toCal.set(Calendar.HOUR_OF_DAY, Integer.parseInt(timeOfDay[0]));
      }
      if (timeOfDay.length == 2) {
        toCal.set(Calendar.MINUTE, Integer.parseInt(timeOfDay[1]));
      }
      xLogger.fine("toCal: {0}", toCal.toString());
    } catch (NumberFormatException e) {
      xLogger.warn("Invalid number for hour or minute in {0}", exportTime);
    }
    return toCal.getTimeInMillis();
  }

  // Private method to get the date range i.e from and to dates, given the hourOffset in UTC and export type
  private static DateRange getDateRange(String hourOffset, String exportType,
                                        boolean dataDurationSameAsRepGenFreq, int dataDuration,
                                        String aggregateFreq, DomainConfig dc) {
    DateRange dr = null;
    // Get the date given the hour of the day in hh:mm format
    Calendar toCal = LocalDateUtil.getZeroTime(dc.getTimezone());
    if (exportType != null && !exportType.isEmpty()) {
      Calendar fromCal = (Calendar) toCal.clone();
      if (dataDurationSameAsRepGenFreq) {
        if (CustomReportConstants.FREQUENCY_DAILY.equals(exportType)) {
          fromCal.add(Calendar.DATE, -1);
        }
                                /*
                                if ( CustomReportsExportMgr.FREQUENCY_WEEKLY.equals( exportType ) ) {
					fromCal.add( Calendar.DATE, -1 * 7 );
				}
				*/
        if (CustomReportConstants.FREQUENCY_WEEKLY.equals(exportType)) {
          if (aggregateFreq != null && !aggregateFreq.isEmpty()) {
            if (CustomReportConstants.FREQUENCY_DAILY.equals(aggregateFreq)) {
              // Use the dataDuration variable to calculate fromCal. Go backwards by 7 days.
              fromCal.add(Calendar.DATE, -7);
            } else if (CustomReportConstants.FREQUENCY_MONTHLY.equals(aggregateFreq)) {
              // Use the dataDuration variable to calculate fromCal. Go backwards by 1 month.
              fromCal.add(Calendar.MONTH, -1);
            }
          }
        }
        if (CustomReportConstants.FREQUENCY_MONTHLY.equals(exportType)) {
          fromCal.add(Calendar.MONTH, -1);
        }
      } else {
        if (aggregateFreq != null && !aggregateFreq.isEmpty()) {
          if (CustomReportConstants.FREQUENCY_DAILY.equals(aggregateFreq)) {
            // Use the dataDuration variable to calculate fromCal. Go backwards by dataDuration number of days.
            fromCal.add(Calendar.DATE, -1 * dataDuration);
          } else if (CustomReportConstants.FREQUENCY_MONTHLY.equals(aggregateFreq)) {
            // Use the dataDuration variable to calculate fromCal. Go backwards by dataDuration number of months.
            fromCal.add(Calendar.MONTH, -1 * dataDuration);
          }
        }
      }
      dr = new DateRange();
      dr.from = fromCal;
      dr.to = toCal;
    }
    return dr;
  }

  private static String getTypeForFirstRun(Map<String, Map<String, String>> typeSheetdataMap) {
    xLogger.fine("Entering getTypeForFirstRun");
    if (typeSheetdataMap != null && !typeSheetdataMap.isEmpty()) {
      Set<String> keys = typeSheetdataMap.keySet();
      Iterator<String> keysIter = keys.iterator();
      if (keysIter.hasNext()) {
        return keysIter.next();
      }
    }
    xLogger.fine("Exiting getTypeForFirstRun");
    return null;
  }

  private static String getDoubleDigitString(String digit) {
    if (digit.length() == 1) {
      return "0" + digit;
    }
    return digit;
  }

  // Get the finalization URL
  private static String getFinalizerUrl(Long domainId, String action, Config config,
                                        Map<String, Map<String, String>> typeSheetdataMap,
                                        Map<String, DateRange> typeDateRangeMap, String fileName,
                                        String subject, String body, Long jobId) {
    xLogger.fine("Entering getFinalizerUrl");
    String
        urlBase =
        CustomReportConstants.CUSTOMREPORTS_EXPORT_TASK_URL + "?domainid=" + domainId
            + "&jobid=" + jobId;
    String userIdsStr = getUserIdsCSV(domainId, config);
    if (userIdsStr != null && !userIdsStr.isEmpty()) {
      urlBase += "&userids=" + userIdsStr;
    }
    if (config.extUsers != null && !config.extUsers.isEmpty()) {
      urlBase += "&emids=" + StringUtil.getCSV(config.extUsers);
    }
    urlBase += "&templatename=" + config.templateName;
    urlBase += "&filename=" + fileName;
    urlBase += "&subject=" + subject;
    urlBase += "&body=" + body;
    String
        finalizerUrl =
        CustomReportConstants.CUSTOMREPORTS_EXPORT_TASK_URL + "?action="
            + CustomReportConstants.ACTION_FINALIZECUSTOMREPORTSEXPORT;
    if (!typeSheetdataMap.isEmpty()) {
      Iterator<String> types = typeSheetdataMap.keySet().iterator();
      // Get all the URLs to be part of finalizer
      while (types.hasNext()) {
        String type = types.next();
        Map<String, String> sheetData = typeSheetdataMap.get(type);
        DateRange dateRange = typeDateRangeMap.get(type);
        String from = null, to = null;
        if (dateRange != null && dateRange.to != null) {
          to =
              String.valueOf(dateRange.to.get(Calendar.DATE)) + "/" + String
                  .valueOf(dateRange.to.get(Calendar.MONTH) + 1) + "/" + String
                  .valueOf(dateRange.to.get(Calendar.YEAR)) + " " + getDoubleDigitString(
                  String.valueOf(dateRange.to.get(Calendar.HOUR_OF_DAY))) + ":"
                  + getDoubleDigitString(String.valueOf(dateRange.to.get(Calendar.MINUTE)))
                  + ":00"; // NOTE: add the seconds, given the datetime format requires it
        }
        if (dateRange != null && dateRange.from != null) {
          from =
              String.valueOf(dateRange.from.get(Calendar.DATE)) + "/" + String
                  .valueOf(dateRange.from.get(Calendar.MONTH) + 1) + "/" + String
                  .valueOf(dateRange.from.get(Calendar.YEAR)) + " " + getDoubleDigitString(
                  String.valueOf(dateRange.from.get(Calendar.HOUR_OF_DAY))) + ":"
                  + getDoubleDigitString(String.valueOf(dateRange.from.get(Calendar.MINUTE)))
                  + ":00"; // NOTE: add the seconds, given the datetime format requires it
        }
        if (from != null && !from.isEmpty()) {
          urlBase += "&from=" + from;
        }
        if (to != null && !to.isEmpty()) {
          urlBase += "&to=" + to;
        }
        if (sheetData != null && !sheetData.isEmpty()) {
          String sheetName = sheetData.get(CustomReportsConfig.SHEETNAME);
          String aggregateFreq = CustomReportConstants.FREQUENCY_DAILY;
          String filterBy = null;
          if (CustomReportConstants.TYPE_TRANSACTIONCOUNTS.equals(type)
              || CustomReportConstants.TYPE_INVENTORYTRENDS.equals(type)) {
            aggregateFreq = sheetData.get(CustomReportsConfig.AGGREGATEFREQ);
            filterBy = sheetData.get(CustomReportsConfig.FILTERBY);
          }
          if (sheetName != null && !sheetName.isEmpty()) {
            String
                url =
                urlBase + "&action=" + CustomReportConstants.ACTION_EXPORT + "&type=" + type
                    + "&sheetname=" + sheetName;
            if (CustomReportConstants.TYPE_TRANSACTIONCOUNTS.equals(type)
                || CustomReportConstants.TYPE_INVENTORYTRENDS.equals(type)) {
              url += "&aggregatefreq=" + aggregateFreq + "&filterby=" + filterBy;
            }
            try {
              finalizerUrl =
                  url + "&furl=" + URLEncoder.encode(finalizerUrl, "UTF-8") + "&fqueue="
                      + taskService.QUEUE_EXPORTER;
            } catch (UnsupportedEncodingException e) {
              e.printStackTrace();
            }
          }
        }
      }
    }
    xLogger.fine("Exiting getFinalizerUrl");
    return finalizerUrl;
  }

  private static String getUserIdsCSV(Long domainId, Config config) {
    // Get the list of users from config
    List<String> userIds = new ArrayList<String>();
    if (config.managers != null && !config.managers.isEmpty()) {
      userIds.addAll(config.managers);
    }
    // Added because this list can be null too.
    if (config.users != null && !config.users.isEmpty()) {
      // Iterate through config.users
      // Check if a user is already there in the userIds list
      // If not then add the user to userIds. Otherwise do not add.
      Iterator<String> adminUsersIter = config.users.iterator();
      while (adminUsersIter.hasNext()) {
        String adminUser = adminUsersIter.next();
        if (adminUser != null && !userIds.contains(adminUser)) {
          userIds.add(adminUser);
        }
      }
    }

    if (config.superUsers != null && !config.superUsers.isEmpty()) {
      userIds.addAll(config.superUsers);
    }
    // Get userIds CSV
    String userIdsCSV = StringUtil.getCSV(userIds);
    List<String>
        enabledUserIds =
        UserUtils.getEnabledUniqueUserIds(domainId, userIdsCSV, StringUtil.getCSV(config.usrTgs));
    return StringUtil.getCSV(enabledUserIds);
  }

  public static QueryParams getQueryParams(String type, Long domainId, String fromDateStr,
                                           String toDateStr, String filterBy, String aggrFrequency)
      throws ParseException {
    xLogger.info(
        "Entering getQueryParams. type: {0}, domainId: {1}, fromDateStr: {2}, toDateStr: {3}, filterBy: {4}, aggrFrequency: {5}",
        type, domainId, fromDateStr, toDateStr, filterBy, aggrFrequency);
    if (type == null || type.isEmpty() || domainId == null) {
      throw new IllegalArgumentException(
          "type or domainId are invalid. Both have to be specified.");
    }
    // Null checks for fromDateStr and toDateStr are made in CustomReportsExportServlet already. So, no need to do that check here.
    // Get the parameter objects
    Date
        from =
        LocalDateUtil
            .getOffsetDate(LocalDateUtil.parseCustom(fromDateStr, Constants.DATETIME_FORMAT, null),
                -1, Calendar.MILLISECOND);
    Date to = LocalDateUtil.parseCustom(toDateStr, Constants.DATETIME_FORMAT, null);
    // Form query params.
    if (CustomReportConstants.TYPE_INVENTORYTRENDS.equals(type)
        || CustomReportConstants.TYPE_TRANSACTIONCOUNTS.equals(type)) {
      if (filterBy == null || filterBy.isEmpty()) {
        throw new InvalidDataException("FilterBy parameter not given for " + type + "export");
      }
      return getInventoryAndTransCountResults(type, domainId, from, to, filterBy, aggrFrequency);
    } else {
      String queryStr = "";
      String paramsStr = " PARAMETERS ";
      String orderingStr = null;
      String dateField = null;
      Map<String, Object> params = new HashMap<String, Object>();
      // Add the dIdParam to queryStr
      queryStr += "dId.contains(dIdParam)";
      paramsStr += "Long dIdParam";
      params.put("dIdParam", domainId);

      // Object-specific query
      if (CustomReportConstants.TYPE_ORDERS.equals(type)) {
        queryStr =
            "SELECT FROM " + JDOUtils.getImplClass(IOrder.class).getName() + " WHERE " + queryStr;
        dateField = "cOn";
        orderingStr = "ORDER BY cOn DESC";
      } else if (CustomReportConstants.TYPE_TRANSACTIONS.equals(type)) {
        queryStr =
            "SELECT FROM " + JDOUtils.getImplClass(ITransaction.class).getName() + " WHERE "
                + queryStr;
        dateField = "t";
        orderingStr = "ORDER BY t DESC";
      } else if (CustomReportConstants.TYPE_MANUALTRANSACTIONS.equals(type)) {
        queryStr =
            "SELECT FROM " + JDOUtils.getImplClass(IMnlTransaction.class).getName() + " WHERE "
                + queryStr;
        dateField = "t";
        orderingStr = "ORDER BY t DESC";
      } else if (CustomReportConstants.TYPE_INVENTORY.equals(type)
          || CustomReportConstants.TYPE_INVENTORYBATCH.equals(type)
          || CustomReportConstants.TYPE_HISTORICAL_INVENTORYSNAPSHOT.equals(type)) {
        queryStr = "SELECT I.`KEY` AS `KEY`, I.* FROM INVNTRY I, KIOSK K WHERE I.KID = K.KIOSKID AND K.KIOSKID IN "
            + "(SELECT KIOSKID_OID from KIOSK_DOMAINS where DOMAIN_ID = " + domainId + " ) ORDER BY "
            + "K.NAME ASC;";
        return new QueryParams(queryStr, "", QueryParams.QTYPE.SQL, IInvntry.class);
      } else if (CustomReportConstants.TYPE_USERS.equals(type)) {
        queryStr =
            "SELECT FROM " + JDOUtils.getImplClass(IUserAccount.class).getName() + " WHERE "
                + queryStr;
        orderingStr = "ORDER BY nName ASC";
      } else if (CustomReportConstants.TYPE_ENTITIES.equals(type)) {
        queryStr =
            "SELECT FROM " + JDOUtils.getImplClass(IKiosk.class).getName() + " WHERE " + queryStr;
        orderingStr = "ORDER BY nName ASC";
      } else if (CustomReportConstants.TYPE_MATERIALS.equals(type)) {
        queryStr =
            "SELECT FROM " + JDOUtils.getImplClass(IMaterial.class).getName() + " WHERE "
                + queryStr;
        orderingStr = "ORDER BY uName ASC";
      } else {
        xLogger.severe("Invalid type: {0}", type);
        return null;
      }
      // Date fields
      // From date, if any
      if (from != null && dateField != null) {
        queryStr += " && " + dateField + " > fromParam";
        paramsStr += ",Date fromParam";
        params.put("fromParam", from);
      }
      // To date, if any
      if (to != null && dateField != null) {
        queryStr += " && " + dateField + " < toParam";
        paramsStr += ",Date toParam";
        params.put("toParam", to);
      }
      // Add parameters
      queryStr += paramsStr;
      // Add date import, if needed
      if (dateField != null && (from != null || to != null)) {
        queryStr += " import java.util.Date;";
      }
      // Add ordering, if needed
      if (orderingStr != null) {
        queryStr += " " + orderingStr;
      }
      xLogger.info("Custom Report Export query: {0}, params: {1}", queryStr, params);
      return new QueryParams(queryStr, params);
    }
  }

  private static String getMaterialTagsFilterQuery(Long domainId) {
    String queryFilters;
    String tags = DomainConfig.getInstance(domainId).getMaterialTags();
    if (StringUtils.isBlank(tags)) {
      return null;
    }
    String tagIds = TagUtil.getTagsByNames(tags, ITag.MATERIAL_TAG);
    if (StringUtils.isBlank(tagIds)) {
      throw new InvalidDataException("Tag's specified are not valid");
    }
    queryFilters =
        CharacterConstants.SPACE + ReportsConstants.QUERY_LITERAL_AND + CharacterConstants.SPACE
            + ReportsConstants.MATERIAL_TAG + QueryConstants.IN + CharacterConstants.O_BRACKET
            + tagIds + CharacterConstants.C_BRACKET;
    return queryFilters;
  }


  private static String getKioskTagsFilterQuery(Long domainId) {
    String queryFilter;
    String tags = DomainConfig.getInstance(domainId).getKioskTags();
    if (StringUtils.isBlank(tags)) {
      return null;
    }
    String tagIds = TagUtil.getTagsByNames(tags, ITag.KIOSK_TAG);
    if (StringUtils.isBlank(tagIds)) {
      throw new InvalidDataException("Tag's specified are not valid");
    }
    queryFilter = CharacterConstants.SPACE + ReportsConstants.QUERY_LITERAL_AND + CharacterConstants.SPACE
        + ReportsConstants.KIOSK_TAG + QueryConstants.IN + CharacterConstants.O_BRACKET + tagIds
        + CharacterConstants.C_BRACKET;
    return queryFilter;
  }


  private static String getStateFilterQuery(Long domainId) {
    String queryFilter = "";
    EntitiesService as = Services.getService(EntitiesServiceImpl.class);
    String states = StringUtil.getCSV(as.getAllStates(domainId));
    String countries = StringUtil.getCSV(as.getAllCountries(domainId));
    queryFilter =
        CharacterConstants.SPACE + ReportsConstants.QUERY_LITERAL_AND + CharacterConstants.SPACE
            + ReportsConstants.COUNTRY + QueryConstants.IN + CharacterConstants.O_BRACKET + countries
            + CharacterConstants.C_BRACKET
            + CharacterConstants.SPACE + ReportsConstants.QUERY_LITERAL_AND + CharacterConstants.SPACE
            + ReportsConstants.STATE + QueryConstants.IN + CharacterConstants.O_BRACKET + states
            + CharacterConstants.C_BRACKET;

    return queryFilter;
  }

  private static String getDistrictFilterQuery(Long domainId) {
    String queryFilters = "";
    EntitiesService as = Services.getService(EntitiesServiceImpl.class);
    String districts = StringUtil.getCSV(as.getAllDistricts(domainId));
    String states = StringUtil.getCSV(as.getAllStates(domainId));
    String countries = StringUtil.getCSV(as.getAllCountries(domainId));

    queryFilters +=
        CharacterConstants.SPACE + ReportsConstants.QUERY_LITERAL_AND + CharacterConstants.SPACE
            + ReportsConstants.COUNTRY + CharacterConstants.SPACE + QueryConstants.IN
            + CharacterConstants.O_BRACKET + countries + CharacterConstants.C_BRACKET
            + CharacterConstants.SPACE + ReportsConstants.QUERY_LITERAL_AND + CharacterConstants.SPACE
            + ReportsConstants.STATE + CharacterConstants.SPACE + QueryConstants.IN
            + CharacterConstants.O_BRACKET + states + CharacterConstants.C_BRACKET
            + CharacterConstants.SPACE + ReportsConstants.QUERY_LITERAL_AND + CharacterConstants.SPACE
            + ReportsConstants.DISTRICT + CharacterConstants.SPACE + QueryConstants.IN
            + CharacterConstants.O_BRACKET + districts + CharacterConstants.C_BRACKET;
    return queryFilters;
  }

  private static String getMaterialFilterQuery(Long domainId) {
    String queryFilters = "";
    String matIds = getAllMaterialIdsCSV(domainId);
    queryFilters =
        CharacterConstants.SPACE + ReportsConstants.QUERY_LITERAL_AND + CharacterConstants.SPACE +
            ReportsConstants.MATERIAL + CharacterConstants.SPACE + QueryConstants.IN
            + CharacterConstants.SPACE +
            CharacterConstants.O_BRACKET + matIds + CharacterConstants.C_BRACKET;
    return queryFilters;
  }

  private static String getAllMaterialIdsCSV(Long domainId) {
    String materialIdCSV = "";
    MaterialCatalogService mcs = Services.getService(MaterialCatalogServiceImpl.class);
    List<Long> matIds = mcs.getAllMaterialIds(domainId);
    Iterator it = matIds.iterator();
    if (it.hasNext()) {
      materialIdCSV = it.next().toString();
    }
    while (it.hasNext()) {
      materialIdCSV += CharacterConstants.COMMA + it.next().toString();
    }
    return materialIdCSV;
  }

  private static String getKioskFilterQuery(Long domainId) {
    String queryFilters = "";
    String kioskIds = getAllKioskIdsCSV(domainId);
    queryFilters +=
        CharacterConstants.SPACE + ReportsConstants.QUERY_LITERAL_AND + CharacterConstants.SPACE +
            ReportsConstants.KIOSK + CharacterConstants.SPACE + QueryConstants.IN
            + CharacterConstants.SPACE +
            CharacterConstants.O_BRACKET + kioskIds + CharacterConstants.C_BRACKET;
    return queryFilters;
  }

  private static String getAllKioskIdsCSV(Long domainId) {
    String kioskIdsCSV = "";
    EntitiesService as = Services.getService(EntitiesServiceImpl.class);
    List<Long> kiosks = as.getAllKioskIds(domainId);
    Iterator it = kiosks.iterator();
    if (it.hasNext()) {
      kioskIdsCSV = it.next().toString();
    }
    while (it.hasNext()) {
      kioskIdsCSV += CharacterConstants.COMMA + it.next().toString();
    }
    return kioskIdsCSV;
  }

  private static QueryParams getInventoryAndTransCountResults(String type, Long domainId, Date from,
                                                              Date to, String filterBy,
                                                              String aggrFrequency) {
    String query;
    String tableName = CharacterConstants.SPACE + ReportsConstants.DOMAIN;
    String
        queryFilters =
        CharacterConstants.SPACE + ReportsConstants.DOMAIN + CharacterConstants.EQUALS + domainId;

    if (CustomReportConstants.TYPE_INVENTORYTRENDS.equals(type)) {
      tableName += CharacterConstants.UNDERSCORE + ReportsConstants.MATERIAL;
      queryFilters += getMaterialFilterQuery(domainId);
    }
    switch (filterBy) {
      case ReportsConstants.FILTER_KIOSK:
        tableName += CharacterConstants.UNDERSCORE + ReportsConstants.KIOSK;
        queryFilters += getKioskFilterQuery(domainId);
        break;
      case ReportsConstants.MATERIAL_TAG:
        tableName += CharacterConstants.UNDERSCORE + ReportsConstants.MATERIAL_TAG;
        queryFilters += getMaterialTagsFilterQuery(domainId);
        break;
      case ReportsConstants.KIOSK_TAG:
        tableName += CharacterConstants.UNDERSCORE + ReportsConstants.KIOSK_TAG;
        queryFilters += getKioskTagsFilterQuery(domainId);
        break;
      case ReportsConstants.FILTER_STATE:
        tableName +=
            CharacterConstants.UNDERSCORE + ReportsConstants.COUNTRY + CharacterConstants.UNDERSCORE
                + ReportsConstants.STATE;
        queryFilters += getStateFilterQuery(domainId);
        break;
      case ReportsConstants.FILTER_DISTRICT:
        tableName +=
            CharacterConstants.UNDERSCORE + ReportsConstants.COUNTRY + CharacterConstants.UNDERSCORE
                + ReportsConstants.STATE + CharacterConstants.UNDERSCORE + ReportsConstants.DISTRICT;
        queryFilters += getDistrictFilterQuery(domainId);
        break;

    }
    String sParam = LocalDateUtil.formatDateForReports(from, aggrFrequency);
    String eParam = LocalDateUtil.formatDateForReports(to, aggrFrequency);
    tableName +=
        ReportsConstants.FREQ_DAILY.equals(aggrFrequency) ? (CharacterConstants.UNDERSCORE
            + ReportsConstants.DAY) : CharacterConstants.UNDERSCORE + ReportsConstants.MONTH;

    queryFilters +=
        CharacterConstants.SPACE + ReportsConstants.QUERY_LITERAL_AND + CharacterConstants.SPACE
            + ReportsConstants.TIME_FIELD
            + QueryConstants.GREATER_THAN + CharacterConstants.S_QUOTE +
            sParam + CharacterConstants.S_QUOTE + CharacterConstants.SPACE
            + ReportsConstants.QUERY_LITERAL_AND
            + CharacterConstants.SPACE +
            ReportsConstants.TIME_FIELD + QueryConstants.LESS_THAN + CharacterConstants.S_QUOTE + eParam
            + CharacterConstants.S_QUOTE;

    query = ReportsConstants.SELECT_ALL_FROM + CharacterConstants.SPACE + tableName
        + CharacterConstants.SPACE +
        QueryConstants.WHERE + CharacterConstants.SPACE + queryFilters;
    return new QueryParams(query, "", QueryParams.QTYPE.CQL, IReportsDao.class);
  }

  private static class DateRange {
    Calendar from;
    Calendar to;
  }

  public static class CustomReportsExportParams {
    private static final String TYPE = "ty";
    private static final String DOMAINID = "dm";
    private static final String COUNTRY = "cn";
    private static final String FILENAME = "fn";
    private static final String LANGUAGE = "ln";
    private static final String TIMEZONE = "tz";
    private static final String USERIDS = "uids";
    private static final String TEMPLATENAME = "tn";
    private static final String SHEETNAME = "sn";
    private static final String SIZE = "sz";
    private static final String SUBJECT = "sbj";
    private static final String SNAPSHOTDATE = "ssdt";
    private static final String BODY = "bdy";

    private static final String JOBID = "jid";
    private static final String EMAILIDS = "emids";


    // All the parameters are mandatory
    public String type = null;
    public Long domainId = null;
    public Locale locale = new Locale(Constants.LANG_DEFAULT, Constants.COUNTRY_DEFAULT);
    public String timezone = Constants.TIMEZONE_DEFAULT;
    public String userIds = null; // recepient user Ids CSV
    public String
        templateName =
        null;
    // NOTE: This is mainly required for logging purposes, to enable production debugging; otherwise, all params. are already part of this class
    public String sheetName = null;
    public String fileName = null; // filename to be used for email attachment
    public String subject = null; // Subject of the notification
    public int size = 0; // number of records so far contained within the blobHandle
    public Date snapshotDate = null;
    public String body = null; // Body of the message
    public Long jobId = null;
    public String emIds;

    public CustomReportsExportParams() {
    }

    public CustomReportsExportParams(String customReportsExportParamsJSON) throws JSONException {
      JSONObject json = new JSONObject(customReportsExportParamsJSON);
      type = json.getString(TYPE);
      domainId = new Long(json.getLong(DOMAINID));
      locale = new Locale(json.getString(LANGUAGE), json.getString(COUNTRY));
      timezone = json.getString(TIMEZONE);
      try {
        userIds = json.getString(USERIDS);
      } catch (JSONException e) {
        // ignore
      }
      templateName = json.getString(TEMPLATENAME);
      sheetName = json.getString(SHEETNAME);
      fileName = json.getString(FILENAME);
      subject = json.getString(SUBJECT);
      size = json.getInt(SIZE);
      try {
        snapshotDate = new Date(json.getLong(SNAPSHOTDATE));
      } catch (JSONException e) {
        // ignore
      }
      try {
        body = json.getString(BODY);
      } catch (JSONException e) {
        // ignore
      }
      try {
        emIds = json.getString(EMAILIDS);
      } catch (JSONException e) {
        // ignore
      }
      jobId = json.getLong(JOBID);
    }

    public String toJSONString() throws JSONException {
      xLogger.fine("Entering CustomReportsExportJson.toJSONString");
      // All attributes are mandatory
      JSONObject json = new JSONObject();
      json.put(TYPE, type);
      json.put(DOMAINID, domainId);
      json.put(LANGUAGE, locale.getLanguage());
      json.put(COUNTRY, locale.getCountry());
      json.put(TIMEZONE, timezone);
      json.put(USERIDS, userIds);
      if (emIds != null) {
        json.put(EMAILIDS, emIds);
      }
      json.put(TEMPLATENAME, templateName);
      json.put(SHEETNAME, sheetName);
      json.put(FILENAME, fileName);
      json.put(SUBJECT, subject);
      json.put(SIZE, size);
      if (body != null) {
        json.put(BODY, body);
      }
      if (snapshotDate != null) {
        json.put(SNAPSHOTDATE, snapshotDate.getTime());
      }
      json.put(JOBID, jobId);
      return json.toString();
    }
  }

  public static String getFileNameWithDomainName(String fileName, Long domainId, Locale locale) {
    xLogger.fine("Entering getFileNameWithDomainName");
    String newFileName = "";
    DomainsService ds = null;
    if (fileName != null && !fileName.isEmpty() && domainId != null) {
      try {
        ds = Services.getService(DomainsServiceImpl.class, locale);
        IDomain d = ds.getDomain(domainId);
        String domainName = d.getName();
        if (domainName != null && !domainName.isEmpty()) {
          domainName = domainName.replace("\"", "_");
          domainName = domainName.replace("\'", "_");
          newFileName += domainName + CustomReportsConfig.SEPARATOR + fileName;
        }
      } catch (Exception e) {
        xLogger.severe(
            "{0} while gettingFileNameWithDomainName for fileName {1} in domain {2}. Message: {3}",
            e.getClass().getName(), fileName, domainId, e.getMessage());
      }
    }
    xLogger.fine("Exiting getFileNameWithDomainName, newFileName: {0}", newFileName);
    return newFileName;
  }

}

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

package com.logistimo.api.builders;

import com.logistimo.AppFactory;
import com.logistimo.customreports.CustomReportConstants;
import com.logistimo.dao.JDOUtils;
import com.logistimo.services.blobstore.BlobInfo;
import com.logistimo.services.blobstore.BlobstoreService;

import org.apache.commons.lang.StringUtils;
import com.logistimo.config.models.CustomReportsConfig;
import com.logistimo.config.models.DomainConfig;
import com.logistimo.entity.IUploaded;
import com.logistimo.security.SecureUserDetails;
import com.logistimo.services.ObjectNotFoundException;
import com.logistimo.services.ServiceException;
import com.logistimo.services.Services;
import com.logistimo.services.UploadService;
import com.logistimo.services.impl.UploadServiceImpl;
import com.logistimo.constants.Constants;
import com.logistimo.utils.LocalDateUtil;
import com.logistimo.utils.NumberUtil;
import com.logistimo.logger.XLog;
import com.logistimo.api.controllers.DomainConfigController;
import com.logistimo.api.constants.ConfigConstants;
import com.logistimo.api.models.UserModel;
import com.logistimo.api.models.configuration.CustomReportsConfigModel;
import com.logistimo.api.models.configuration.NotificationsModel;
import com.logistimo.users.service.UsersService;
import com.logistimo.users.service.impl.UsersServiceImpl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

/**
 * Created by naveensnair on 26/12/14.
 */
public class CustomReportsBuilder {

  public static final XLog xLogger = XLog.getLog(DomainConfigController.class);

  BlobstoreService blobstoreService = AppFactory.get().getBlobstoreService();

  public CustomReportsConfig.Config updateCustomReportsConfiguration(CustomReportsConfigModel model,
                                                                     BlobstoreService blobstoreService) {

    CustomReportsConfig.Config config = new CustomReportsConfig.Config();

    updateTemplateInfo(model, config);
    updateInventorySheet(model, config);
    updateUsersSheet(model, config);
    updateEntitiesSheet(model, config);
    updateMaterialsSheet(model, config);
    updateOrdersSheet(model, config);
    updateTransactionSheet(model, config);
    updateTransactionCountSheet(model, config);
    updateInventoryTrendsSheet(model, config);
    updateHistoricalInventorySnapShotSheetName(model, config);
    updateFrequency(model, config);
    updateMailingAddress(model, config);

    return config;
  }

  public void updateTemplateInfo(CustomReportsConfigModel model,
                                 CustomReportsConfig.Config config) {
    String templateName = "";
    String description = "";
    if (model.tn != null) {
      templateName = model.tn;
    }
    if (model.dsc != null) {
      description = model.dsc;
    }

    config.fileName = templateName;

  }

  public void updateInventorySheet(CustomReportsConfigModel model,
                                   CustomReportsConfig.Config config) {
    if (model.invsn != null && !model.invsn.isEmpty()) {
      if (model.ib) {
        Map<String, String> invBatchSheetDataMap = new HashMap<String, String>();
        invBatchSheetDataMap.put(CustomReportsConfig.SHEETNAME, model.invsn);
        config.typeSheetDataMap
            .put(CustomReportConstants.TYPE_INVENTORYBATCH, invBatchSheetDataMap);
      } else {
        Map<String, String> invSheetDataMap = new HashMap<String, String>();
        invSheetDataMap.put(CustomReportsConfig.SHEETNAME, model.invsn);
        config.typeSheetDataMap.put(CustomReportConstants.TYPE_INVENTORY, invSheetDataMap);
      }
    }
  }

  public void updateUsersSheet(CustomReportsConfigModel model, CustomReportsConfig.Config config) {
    if (model.usn != null && !model.usn.isEmpty()) {
      Map<String, String> usersSheetDataMap = new HashMap<String, String>();
      usersSheetDataMap.put(CustomReportsConfig.SHEETNAME, model.usn);
      config.typeSheetDataMap.put(CustomReportConstants.TYPE_USERS, usersSheetDataMap);
    }
  }

  public void updateEntitiesSheet(CustomReportsConfigModel model,
                                  CustomReportsConfig.Config config) {
    if (model.ksn != null && !model.ksn.isEmpty()) {
      Map<String, String> entitiesSheetDataMap = new HashMap<String, String>();
      entitiesSheetDataMap.put(CustomReportsConfig.SHEETNAME, model.ksn);
      config.typeSheetDataMap.put(CustomReportConstants.TYPE_ENTITIES, entitiesSheetDataMap);
    }
  }

  public void updateMaterialsSheet(CustomReportsConfigModel model,
                                   CustomReportsConfig.Config config) {
    if (model.msn != null && !model.msn.isEmpty()) {
      Map<String, String> materialsSheetDataMap = new HashMap<String, String>();
      materialsSheetDataMap.put(CustomReportsConfig.SHEETNAME, model.msn);
      config.typeSheetDataMap.put(CustomReportConstants.TYPE_MATERIALS, materialsSheetDataMap);
    }
  }

  public void updateOrdersSheet(CustomReportsConfigModel model, CustomReportsConfig.Config config) {
    if (model.osn != null && !model.osn.isEmpty()) {
      Map<String, String> ordersSheetDataMap = new HashMap<String, String>();
      ordersSheetDataMap.put(CustomReportsConfig.SHEETNAME, model.osn);
      if (model.ogf) {
        ordersSheetDataMap
            .put(CustomReportsConfig.DATA_DURATION_SAMEAS_REPGENFREQ, CustomReportsConfig.TRUE);
        if (model.od != null && !model.od.isEmpty()) {
          ordersSheetDataMap.put(CustomReportsConfig.DATA_DURATION, model.od);
        }
      } else {
        ordersSheetDataMap
            .put(CustomReportsConfig.DATA_DURATION_SAMEAS_REPGENFREQ, CustomReportsConfig.FALSE);
      }

      config.typeSheetDataMap.put(CustomReportConstants.TYPE_ORDERS, ordersSheetDataMap);
    }
  }

  public void updateTransactionSheet(CustomReportsConfigModel model,
                                     CustomReportsConfig.Config config) {
    if (model.tsn != null && !model.tsn.isEmpty()) {
      Map<String, String> transactionsSheetDataMap = new HashMap<String, String>();
      transactionsSheetDataMap.put(CustomReportsConfig.SHEETNAME, model.tsn);
      if (model.tgf) {
        transactionsSheetDataMap
            .put(CustomReportsConfig.DATA_DURATION_SAMEAS_REPGENFREQ, CustomReportsConfig.TRUE);
      } else {
        transactionsSheetDataMap
            .put(CustomReportsConfig.DATA_DURATION_SAMEAS_REPGENFREQ, CustomReportsConfig.FALSE);
        if (model.td != null && !model.td.isEmpty()) {
          transactionsSheetDataMap.put(CustomReportsConfig.DATA_DURATION, model.td);
        }
      }
      config.typeSheetDataMap
          .put(CustomReportConstants.TYPE_TRANSACTIONS, transactionsSheetDataMap);
    }
  }

  public void updateTransactionCountSheet(CustomReportsConfigModel model,
                                          CustomReportsConfig.Config config) {
    if (model.tcsn != null && model.tcsn.isEmpty()) {
      Map<String, String> transactionCountsSheetDataMap = new HashMap<String, String>();
      transactionCountsSheetDataMap.put(CustomReportsConfig.SHEETNAME, model.tcsn);
      if (model.tcrgf) {
        transactionCountsSheetDataMap
            .put(CustomReportsConfig.DATA_DURATION_SAMEAS_REPGENFREQ, CustomReportsConfig.TRUE);
      } else {
        transactionCountsSheetDataMap
            .put(CustomReportsConfig.DATA_DURATION_SAMEAS_REPGENFREQ, CustomReportsConfig.FALSE);
        if (model.tcd != null && model.tcd.isEmpty()) {
          transactionCountsSheetDataMap.put(CustomReportsConfig.DATA_DURATION, model.tcd);
        }
      }
      if (model.tct != null && model.tct.isEmpty()) {
        transactionCountsSheetDataMap.put(CustomReportsConfig.AGGREGATEFREQ, model.tct);
      }
      if (model.tce != null && model.tce.isEmpty()) {
        transactionCountsSheetDataMap.put(CustomReportsConfig.FILTERBY, model.tce);
      }

      config.typeSheetDataMap
          .put(CustomReportConstants.TYPE_TRANSACTIONCOUNTS, transactionCountsSheetDataMap);
    }
  }

  public void updateInventoryTrendsSheet(CustomReportsConfigModel model,
                                         CustomReportsConfig.Config config) {
    if (model.itsn != null && model.itsn.isEmpty()) {
      Map<String, String> inventoryTrendsSheetDataMap = new HashMap<String, String>();
      inventoryTrendsSheetDataMap.put(CustomReportsConfig.SHEETNAME, model.itsn);
      if (model.itrgf) {
        inventoryTrendsSheetDataMap
            .put(CustomReportsConfig.DATA_DURATION_SAMEAS_REPGENFREQ, CustomReportsConfig.TRUE);
      } else {
        inventoryTrendsSheetDataMap
            .put(CustomReportsConfig.DATA_DURATION_SAMEAS_REPGENFREQ, CustomReportsConfig.FALSE);
        if (model.itd != null && model.itd.isEmpty()) {
          inventoryTrendsSheetDataMap.put(CustomReportsConfig.DATA_DURATION, model.itd);
        }
      }
      if (model.itt != null && model.itt.isEmpty()) {
        inventoryTrendsSheetDataMap.put(CustomReportsConfig.AGGREGATEFREQ, model.itt);
      }
      if (model.ite != null && model.ite.isEmpty()) {
        inventoryTrendsSheetDataMap.put(CustomReportsConfig.FILTERBY, model.ite);
      }

      config.typeSheetDataMap
          .put(CustomReportConstants.TYPE_INVENTORYTRENDS, inventoryTrendsSheetDataMap);
    }
  }

  public void updateHistoricalInventorySnapShotSheetName(CustomReportsConfigModel model,
                                                         CustomReportsConfig.Config config) {
    if (model.hsn != null && !model.hsn.isEmpty()) {
      Map<String, String> historicalInvSnapshotSheetDataMap = new HashMap<String, String>();
      historicalInvSnapshotSheetDataMap.put(CustomReportsConfig.SHEETNAME, model.hsn);
      if (model.hd != null && model.hd.isEmpty()) {
        historicalInvSnapshotSheetDataMap.put(CustomReportsConfig.DATA_DURATION, model.hd);
      }
      config.typeSheetDataMap.put(CustomReportConstants.TYPE_HISTORICAL_INVENTORYSNAPSHOT,
          historicalInvSnapshotSheetDataMap);
    }
  }

  public void updateFrequency(CustomReportsConfigModel model, CustomReportsConfig.Config config) {
    if (model.rgt != null && !model.rgt.isEmpty()) {

      if (CustomReportConstants.FREQUENCY_DAILY.equalsIgnoreCase(model.rgt)) {
        if (model.rgth != null && !model.rgth.isEmpty()) {
          config.dailyTime = model.rgth;
        }
      } else if (CustomReportConstants.FREQUENCY_WEEKLY.equalsIgnoreCase(model.rgt)) {
        if (model.rgtw != null && !model.rgtw.isEmpty()) {
          config.dayOfWeek = Integer.parseInt(model.rgtw);
        }
      } else if (CustomReportConstants.FREQUENCY_MONTHLY.equalsIgnoreCase(model.rgt)) {
        if (model.rgtm != null && model.rgtm.isEmpty()) {
          config.dayOfMonth = Integer.parseInt(model.rgtm);
        }
      }
    }
  }

  public void updateMailingAddress(CustomReportsConfigModel model,
                                   CustomReportsConfig.Config config) {
    if (model.mn != null && model.mn.isEmpty()) {
      config.managers = buildMailers(model.mn);
    }
    if (model.an != null && model.an.isEmpty()) {
      config.users = buildMailers(model.an);
    }
    if (model.sn != null && model.sn.isEmpty()) {
      config.superUsers = buildMailers(model.sn);
    }
    config.extUsers = model.exusrs;
    config.usrTgs = model.usrTgs;

  }

  public List<String> buildMailers(List<UserModel> users) {
    List<String> mailers = new ArrayList<String>();
    if (users.size() > 0) {
      for (UserModel user : users) {
        mailers.add(user.id);
      }
    }

    return mailers;
  }

  // Create/update an existing an Uploaded object.
  public IUploaded updateUploadedObject(HttpServletRequest req, SecureUserDetails sUser,
                                        Long domainId, BlobstoreService blobstoreService,
                                        String templateName) throws ServiceException {
    Map<String, List<String>>
        blobs =
        blobstoreService.getUploads(
            req); // Note getUploadedBlobs is deprecated because it does not handle file uploads with multiple attribute set to true.

    if (blobs != null && !blobs.isEmpty()) {
      List<String> blobKeyList = blobs.get("data");
      String blobKey = blobKeyList.get(0);
      if (blobKey != null) {
        BlobInfo blobInfo = blobstoreService.getBlobInfo(blobKey);
        String fileName = blobInfo.getFilename();
        // Create an uploaded object
        IUploaded uploaded = null;
        UploadService svc = null;
        String
            uploadedFileName =
            CustomReportsConfig.Config.getFileNameWithDomainId(templateName, domainId);
        // Generate a 5 digit random number and append it to the uploadedFileName, just to make sure that the uploaded key is unique
        uploadedFileName += NumberUtil.generateFiveDigitRandomNumber();

        String
            uploadedKey =
            JDOUtils.createUploadedKey(uploadedFileName, CustomReportsConfig.VERSION,
                Constants.LANG_DEFAULT);
        boolean isUploadedNew = false;
        try {
          svc = Services.getService(UploadServiceImpl.class);
          uploaded = svc.getUploaded(uploadedKey);
          // Found an older upload, remove the older blob
          try {
            blobstoreService.remove(uploaded.getBlobKey());
          } catch (Exception e) {
            xLogger.warn("Exception while removing uploaded object", e);
          }
        } catch (ObjectNotFoundException e) {
          uploaded = JDOUtils.createInstance(IUploaded.class);
          uploaded.setId(uploadedKey);
          isUploadedNew = true;
        } catch (Exception e) {
          throw new ServiceException(
              "System error occurred during upload. Please try again or contact your administrator.");
          // writeToScreen( req, resp, "System error occurred during upload. Please try again or contact your administrator.", Constants.VIEW_CONFIGURATION );
        }
        Date now = new Date();
        // Update uploaded object
        uploaded.setBlobKey(blobKey);
        uploaded.setDomainId(domainId);
        uploaded.setFileName(fileName);
        uploaded.setLocale(Constants.LANG_DEFAULT);
        uploaded.setTimeStamp(now);
        uploaded.setType(IUploaded.CUSTOMREPORT_TEMPLATE);
        uploaded.setUserId(sUser.getUsername());
        uploaded.setVersion(CustomReportsConfig.VERSION);

        try {
          // Persist upload metadata
          if (isUploadedNew) {
            svc.addNewUpload(uploaded);
          } else {
            svc.updateUploaded(uploaded);
          }
          return uploaded;
        } catch (Exception e) {
          throw new ServiceException(
              "System error occurred during upload. Please try again or contact your administrator.");
        }
      } // End if blobKey != null
    } // End if blobs != null
    return null;
  }

  public CustomReportsConfig.Config populateConfig(CustomReportsConfigModel model,
                                                   CustomReportsConfig.Config config, boolean add,
                                                   String timezone) {
    if (!add) {
      config.description = null;
      config.typeSheetDataMap.clear(); //= new HashMap<String,Map<String,String>>();
      // Reset the time fields also
      config.dailyTime = null;
      config.dayOfWeek = 0;
      config.weeklyRepGenTime = null;
      config.dayOfMonth = 0;
      config.monthlyRepGenTime = null;
      // Reset the data duration to true and data duration to 0
      config.dataDurationSameAsRepGenFreq = true;
      config.dataDuration = 0;
      // Reset the manager users, admin users and super users to null if logged in user is a Domain Owner or a Service Manager
      config.managers = null;
      config.users = null;
      config.superUsers = null;
      config.extUsers = null;
      config.usrTgs = null;
      config.fileName = model.fn;
      config.templateKey = model.tk;
    }

    if (StringUtils.isNotEmpty(model.tn)) {
      config.templateName = model.tn;
    }
    if (StringUtils.isNotEmpty(model.dsc)) {
      config.description = model.dsc;
    }
    if (model.invsn != null && !model.invsn.isEmpty()) {
      if (model.ib) {
        Map<String, String> invBatchSheetDataMap = new HashMap<String, String>();
        invBatchSheetDataMap.put(CustomReportsConfig.SHEETNAME, model.invsn);
        config.typeSheetDataMap
            .put(CustomReportConstants.TYPE_INVENTORYBATCH, invBatchSheetDataMap);
      } else {
        Map<String, String> invSheetDataMap = new HashMap<String, String>();
        invSheetDataMap.put(CustomReportsConfig.SHEETNAME, model.invsn);
        config.typeSheetDataMap.put(CustomReportConstants.TYPE_INVENTORY, invSheetDataMap);
      }
    }
    if (model.usn != null && !model.usn.isEmpty()) {
      Map<String, String> usersSheetDataMap = new HashMap<String, String>();
      usersSheetDataMap.put(CustomReportsConfig.SHEETNAME, model.usn);
      config.typeSheetDataMap.put(CustomReportConstants.TYPE_USERS, usersSheetDataMap);
    }
    if (model.ksn != null && !model.ksn.isEmpty()) {
      Map<String, String> entitiesSheetDataMap = new HashMap<String, String>();
      entitiesSheetDataMap.put(CustomReportsConfig.SHEETNAME, model.ksn);
      config.typeSheetDataMap.put(CustomReportConstants.TYPE_ENTITIES, entitiesSheetDataMap);
    }
    if (model.msn != null && !model.msn.isEmpty()) {
      Map<String, String> materialsSheetDataMap = new HashMap<String, String>();
      materialsSheetDataMap.put(CustomReportsConfig.SHEETNAME, model.msn);
      config.typeSheetDataMap.put(CustomReportConstants.TYPE_MATERIALS, materialsSheetDataMap);
    }
    if (model.osn != null && !model.osn.isEmpty()) {
      Map<String, String> ordersSheetDataMap = new HashMap<String, String>();
      ordersSheetDataMap.put(CustomReportsConfig.SHEETNAME, model.osn);
      // Add data duration data to the ordersSheetDataMap
      if (!model.ogf) {
        ordersSheetDataMap
            .put(CustomReportsConfig.DATA_DURATION_SAMEAS_REPGENFREQ, CustomReportsConfig.FALSE);
        if (model.od != null && !model.od.isEmpty()) {
          ordersSheetDataMap.put(CustomReportsConfig.DATA_DURATION, model.od);
        }
      } else {
        ordersSheetDataMap
            .put(CustomReportsConfig.DATA_DURATION_SAMEAS_REPGENFREQ, CustomReportsConfig.TRUE);
        // No need to put dataduration key because it is the same as report generation frequency.
      }
      config.typeSheetDataMap.put(CustomReportConstants.TYPE_ORDERS, ordersSheetDataMap);
    }
    if (model.tsn != null && !model.tsn.isEmpty()) {
      Map<String, String> transactionsSheetDataMap = new HashMap<String, String>();
      transactionsSheetDataMap.put(CustomReportsConfig.SHEETNAME, model.tsn);
      // Add data duration data to the transactionsSheetDataMap
      if (!model.tgf) {
        transactionsSheetDataMap
            .put(CustomReportsConfig.DATA_DURATION_SAMEAS_REPGENFREQ, CustomReportsConfig.FALSE);
        if (model.td != null && !model.td.isEmpty()) {
          transactionsSheetDataMap.put(CustomReportsConfig.DATA_DURATION, model.td);
        }
      } else {
        transactionsSheetDataMap
            .put(CustomReportsConfig.DATA_DURATION_SAMEAS_REPGENFREQ, CustomReportsConfig.TRUE);
        // No need to put dataduration key because it is the same as report generation frequency.
      }
      config.typeSheetDataMap
          .put(CustomReportConstants.TYPE_TRANSACTIONS, transactionsSheetDataMap);
    }
    if (model.mtsn != null && !model.mtsn.isEmpty()) {
      Map<String, String> manualTransactionsSheetDataMap = new HashMap<String, String>();
      manualTransactionsSheetDataMap.put(CustomReportsConfig.SHEETNAME, model.mtsn);
      // Add data duration data to the transactionsSheetDataMap
      if (!model.mtgf) {
        manualTransactionsSheetDataMap
            .put(CustomReportsConfig.DATA_DURATION_SAMEAS_REPGENFREQ, CustomReportsConfig.FALSE);
        if (model.mtd != null && !model.mtd.isEmpty()) {
          manualTransactionsSheetDataMap.put(CustomReportsConfig.DATA_DURATION, model.mtd);
        }
      } else {
        manualTransactionsSheetDataMap
            .put(CustomReportsConfig.DATA_DURATION_SAMEAS_REPGENFREQ, CustomReportsConfig.TRUE);
        // No need to put dataduration key because it is the same as report generation frequency.
      }
      config.typeSheetDataMap
          .put(CustomReportConstants.TYPE_MANUALTRANSACTIONS, manualTransactionsSheetDataMap);
    }
    if (model.tcsn != null && !model.tcsn.isEmpty()) {
      Map<String, String> transactioncountsSheetDataMap = new HashMap<String, String>();
      transactioncountsSheetDataMap.put(CustomReportsConfig.SHEETNAME, model.tcsn);
      // Add data duration data to the transactioncountsSheetDataMap
      if (!model.tcrgf) {
        transactioncountsSheetDataMap
            .put(CustomReportsConfig.DATA_DURATION_SAMEAS_REPGENFREQ, CustomReportsConfig.FALSE);
        if (model.tcd != null && !model.tcd.isEmpty()) {
          transactioncountsSheetDataMap.put(CustomReportsConfig.DATA_DURATION, model.tcd);
        }
      } else {
        transactioncountsSheetDataMap
            .put(CustomReportsConfig.DATA_DURATION_SAMEAS_REPGENFREQ, CustomReportsConfig.TRUE);
        // No need to put dataduration key because it is the same as report generation frequency.
      }
      // Add aggregate frequency and filterby to transactioncountsSheetDataMap
      if (model.tct != null && !model.tct.isEmpty()) {
        transactioncountsSheetDataMap.put(CustomReportsConfig.AGGREGATEFREQ, model.tct);
      }
      if (model.tce != null && !model.tce.isEmpty()) {
        transactioncountsSheetDataMap.put(CustomReportsConfig.FILTERBY, model.tce);
      }
      config.typeSheetDataMap
          .put(CustomReportConstants.TYPE_TRANSACTIONCOUNTS, transactioncountsSheetDataMap);
      xLogger.fine("typeSheetDataMap: {0}", config.typeSheetDataMap);
    }
    if (model.itsn != null && !model.itsn.isEmpty()) {
      Map<String, String> inventorytrendsSheetDataMap = new HashMap<String, String>();
      inventorytrendsSheetDataMap.put(CustomReportsConfig.SHEETNAME, model.itsn);
      // Add data duration data to the inventorytrendsSheetDataMap
      if (!model.itrgf) {
        inventorytrendsSheetDataMap
            .put(CustomReportsConfig.DATA_DURATION_SAMEAS_REPGENFREQ, CustomReportsConfig.FALSE);
        if (model.itd != null && !model.itd.isEmpty()) {
          inventorytrendsSheetDataMap.put(CustomReportsConfig.DATA_DURATION, model.itd);
        }
      } else {
        inventorytrendsSheetDataMap
            .put(CustomReportsConfig.DATA_DURATION_SAMEAS_REPGENFREQ, CustomReportsConfig.TRUE);
        // No need to put dataduration key because it is the same as report generation frequency.
      }
      // Add aggregate frequency and filterby to inventorytrendsSheetDataMap
      if (model.itt != null && !model.itt.isEmpty()) {
        inventorytrendsSheetDataMap.put(CustomReportsConfig.AGGREGATEFREQ, model.itt);
      }
      if (model.ite != null && !model.ite.isEmpty()) {
        inventorytrendsSheetDataMap.put(CustomReportsConfig.FILTERBY, model.ite);
      }
      config.typeSheetDataMap
          .put(CustomReportConstants.TYPE_INVENTORYTRENDS, inventorytrendsSheetDataMap);
      xLogger.fine("typeSheetDataMap: {0}", config.typeSheetDataMap);
    }
    if (model.hsn != null && !model.hsn.isEmpty()) {
      Map<String, String> historicalInvSnapshotSheetDataMap = new HashMap<String, String>();
      historicalInvSnapshotSheetDataMap.put(CustomReportsConfig.SHEETNAME, model.hsn);
      if (model.hd != null && !model.hd.isEmpty()) {
        historicalInvSnapshotSheetDataMap.put(CustomReportsConfig.DATA_DURATION, model.hd);
      }
      config.typeSheetDataMap.put(CustomReportConstants.TYPE_HISTORICAL_INVENTORYSNAPSHOT,
          historicalInvSnapshotSheetDataMap);
      xLogger.fine("typeSheetDataMap: {0}", config.typeSheetDataMap);
    }
    // Set the frequency
    if (model.rgt != null && !model.rgt.isEmpty()) {
      if (model.rgt.equalsIgnoreCase("0")) {
        if (model.rgth != null && !model.rgth.isEmpty()) {
          config.dailyTime =
              LocalDateUtil.convertTimeString(model.rgth, timezone,
                  true); // Convert from domain specific timezone to utc
        }

      } else if (model.rgt.equalsIgnoreCase("1")) {
        if (model.rgtw != null && !model.rgtw.isEmpty()) {
          config.dayOfWeek = Integer.parseInt(model.rgtw);
        }
        if (model.rgth != null && !model.rgth.isEmpty()) {
          config.weeklyRepGenTime =
              LocalDateUtil.convertTimeString(model.rgth, timezone,
                  true); // Convert from domain specific timezone to utc
        }

      } else if (model.rgt.equalsIgnoreCase("2")) {
        if (model.rgtm != null && !model.rgtm.isEmpty()) {
          config.dayOfMonth = Integer.parseInt(model.rgtm);
        }
        if (model.rgth != null && !model.rgth.isEmpty()) {
          config.monthlyRepGenTime =
              LocalDateUtil.convertTimeString(model.rgth, timezone,
                  true); // Convert from domain specific timezone to utc
        }
      }
    }

    xLogger.fine("managerUsersStr: {0}", model.mn);
    if (model.mn != null && !model.mn.isEmpty()) {
      List<String> managers = new ArrayList<String>();
      for (UserModel m : model.mn) {
        managers.add(m.id);
      }
      if (managers.size() > 0) {
        config.managers = managers;
      }

    }
    xLogger.fine("adminUsersStr: {0}", model.an);
    if (model.an != null && !model.an.isEmpty()) {
      List<String> administrators = new ArrayList<String>();
      for (UserModel a : model.an) {
        administrators.add(a.id);
      }
      if (administrators.size() > 0) {
        config.users = administrators;
      }
    }
    if (model.sn != null && !model.sn.isEmpty()) {
      List<String> superusers = new ArrayList<String>();
      for (UserModel s : model.sn) {
        superusers.add(s.id);
      }
      if (superusers.size() > 0) {
        config.superUsers = superusers;
      }
    }
    config.extUsers = model.exusrs;
    config.usrTgs = model.usrTgs;

    if (!add) {
      config.lastUpdatedTime = new Date();
    } else {
      config.creationTime = new Date();
      config.lastUpdatedTime = new Date();
    }
    return config;
  }

  public NotificationsModel populateCustomReportModelsList(
      List<CustomReportsConfig.Config> configList, Locale locale, Long domainId, String timezone,
      boolean isList) throws ServiceException, ObjectNotFoundException {
    NotificationsModel model = new NotificationsModel();
    DomainConfig dc = DomainConfig.getInstance(domainId);
    if (isList) {
      List<CustomReportsConfigModel> modelList = new ArrayList<>();
      if (configList != null) {
        Collections.sort(configList);
        for (CustomReportsConfig.Config c : configList) {
          modelList.add(buildCustomReportModel(c, locale, timezone, domainId));
        }
      }
      model.config = modelList;
    } else {
      model.config = buildCustomReportModel(configList.get(0), locale, timezone, domainId);
    }

    List<String> val = dc.getDomainData(ConfigConstants.CUSTOM_REPORTS);
    if (val != null) {
      model.createdBy = val.get(0);
      model.lastUpdated =
          LocalDateUtil.format(new Date(Long.parseLong(val.get(1))), locale, timezone);
      try {
        UsersService as = Services.getService(UsersServiceImpl.class, locale);
        model.fn = as.getUserAccount(model.createdBy).getFullName();
      } catch (Exception e) {
        //ignore.. get for display only.
      }
    }
    return model;
  }

  // Remove the Uploaded object with the specified uploadedKey from the datastore.
  public boolean removeUploadedObject(String uploadedKey) throws ServiceException {
    boolean success = false;
    xLogger.fine("Entering removeUploadedObject");
    if (uploadedKey != null && !uploadedKey.isEmpty()) {
      UploadService svc;

      try {
        svc = Services.getService(UploadServiceImpl.class);
        IUploaded uploaded = svc.getUploaded(uploadedKey);
        // Remove the blob from the blobstore.
        String blobKeyStr = uploaded.getBlobKey();
        blobstoreService.remove(blobKeyStr);
        // Now remove the uploaded object
        svc.removeUploaded(uploadedKey);
        success = true;
      } catch (ObjectNotFoundException onfe) {
        xLogger.warn("Uploaded object not found, ignoring.. " + onfe.getMessage());
      } catch (Exception e) {
        xLogger.severe("{0} while removing uploaded object. Message: {1}", e.getClass().getName());
        throw new ServiceException(e.getMessage());
      }
    } else {
      xLogger.warn("Cannot remove uploaded object with null or empty key");
    }
    xLogger.fine("Exiting removeUploadedObject");

    return success;
  }

  public CustomReportsConfigModel buildCustomReportModel(CustomReportsConfig.Config c,
                                                         Locale locale, String timezone,
                                                         Long domainId) {
    CustomReportsConfigModel model = new CustomReportsConfigModel();
    if (c != null) {
      model.tn = c.templateName;
      model.dsc = c.description;
      model.fn = c.fileName;
      model.tk = c.templateKey;
      if (c.lastUpdatedTime != null) {
        model.lut = LocalDateUtil.format(c.lastUpdatedTime, locale, timezone);
      } else {
        model.lut = LocalDateUtil.format(c.creationTime, locale, timezone);
      }

      if (c.typeSheetDataMap != null) {
        if (c.typeSheetDataMap.containsKey(CustomReportConstants.TYPE_INVENTORYBATCH) ||
            c.typeSheetDataMap.containsKey(CustomReportConstants.TYPE_INVENTORY)) {
          if (c.typeSheetDataMap.containsKey(CustomReportConstants.TYPE_INVENTORYBATCH)) {
            Map<String, String> invBatchSheetDataMap = new HashMap<String, String>();
            invBatchSheetDataMap =
                c.typeSheetDataMap.get(CustomReportConstants.TYPE_INVENTORYBATCH);
            model.invsn = invBatchSheetDataMap.get(CustomReportsConfig.SHEETNAME);
            model.ib = true;
          } else {
            if (c.typeSheetDataMap.containsKey(CustomReportConstants.TYPE_INVENTORY)) {
              Map<String, String> invSheetDataMap = new HashMap<String, String>();
              invSheetDataMap = c.typeSheetDataMap.get(CustomReportConstants.TYPE_INVENTORY);
              model.invsn = invSheetDataMap.get(CustomReportsConfig.SHEETNAME);
              model.ib = false;
            }
          }
        }

        if (c.typeSheetDataMap.containsKey(CustomReportConstants.TYPE_USERS)) {
          Map<String, String> usersSheetDataMap = new HashMap<String, String>();
          usersSheetDataMap = c.typeSheetDataMap.get(CustomReportConstants.TYPE_USERS);
          model.usn = usersSheetDataMap.get(CustomReportsConfig.SHEETNAME);
        }

        if (c.typeSheetDataMap.containsKey(CustomReportConstants.TYPE_ENTITIES)) {
          Map<String, String> entitiesSheetDataMap = new HashMap<String, String>();
          entitiesSheetDataMap = c.typeSheetDataMap.get(CustomReportConstants.TYPE_ENTITIES);
          model.ksn = entitiesSheetDataMap.get(CustomReportsConfig.SHEETNAME);
        }

        if (c.typeSheetDataMap.containsKey(CustomReportConstants.TYPE_MATERIALS)) {
          Map<String, String> materialsSheetDataMap = new HashMap<String, String>();
          materialsSheetDataMap = c.typeSheetDataMap.get(CustomReportConstants.TYPE_MATERIALS);
          model.msn = materialsSheetDataMap.get(CustomReportsConfig.SHEETNAME);
        }

        if (c.typeSheetDataMap.containsKey(CustomReportConstants.TYPE_ORDERS)) {
          Map<String, String> ordersSheetDataMap = new HashMap<String, String>();
          ordersSheetDataMap = c.typeSheetDataMap.get(CustomReportConstants.TYPE_ORDERS);
          if (ordersSheetDataMap.containsKey(CustomReportsConfig.DATA_DURATION_SAMEAS_REPGENFREQ)) {
            if (ordersSheetDataMap.get(CustomReportsConfig.DATA_DURATION_SAMEAS_REPGENFREQ)
                .equalsIgnoreCase(CustomReportsConfig.TRUE)) {
              model.ogf = true;
            } else {
              model.od = ordersSheetDataMap.get(CustomReportsConfig.DATA_DURATION);
              model.ogf = false;
            }

            model.osn = ordersSheetDataMap.get(CustomReportsConfig.SHEETNAME);
          }
        }

        if (c.typeSheetDataMap.containsKey(CustomReportConstants.TYPE_TRANSACTIONS)) {
          Map<String, String> transactionsSheetDataMap = new HashMap<String, String>();
          transactionsSheetDataMap =
              c.typeSheetDataMap.get(CustomReportConstants.TYPE_TRANSACTIONS);
          if (transactionsSheetDataMap
              .containsKey(CustomReportsConfig.DATA_DURATION_SAMEAS_REPGENFREQ)) {
            if (transactionsSheetDataMap.get(CustomReportsConfig.DATA_DURATION_SAMEAS_REPGENFREQ)
                .equalsIgnoreCase(CustomReportsConfig.TRUE)) {
              model.tgf = true;
            } else {
              model.td = transactionsSheetDataMap.get(CustomReportsConfig.DATA_DURATION);
              model.tgf = false;
            }

            model.tsn = transactionsSheetDataMap.get(CustomReportsConfig.SHEETNAME);
          }
        }

        if (c.typeSheetDataMap.containsKey(CustomReportConstants.TYPE_MANUALTRANSACTIONS)) {
          Map<String, String> manualTransactionsSheetDataMap = new HashMap<String, String>();
          manualTransactionsSheetDataMap =
              c.typeSheetDataMap.get(CustomReportConstants.TYPE_MANUALTRANSACTIONS);
          if (manualTransactionsSheetDataMap
              .containsKey(CustomReportsConfig.DATA_DURATION_SAMEAS_REPGENFREQ)) {
            if (manualTransactionsSheetDataMap
                .get(CustomReportsConfig.DATA_DURATION_SAMEAS_REPGENFREQ)
                .equalsIgnoreCase(CustomReportsConfig.TRUE)) {
              model.mtgf = true;
            } else {
              model.mtd = manualTransactionsSheetDataMap.get(CustomReportsConfig.DATA_DURATION);
              model.mtgf = false;
            }

            model.mtsn = manualTransactionsSheetDataMap.get(CustomReportsConfig.SHEETNAME);
          }
        }

        if (c.typeSheetDataMap.containsKey(CustomReportConstants.TYPE_TRANSACTIONCOUNTS)) {
          Map<String, String> transactionCountsSheetDataMap = new HashMap<String, String>();
          if (c.typeSheetDataMap.containsKey(CustomReportConstants.TYPE_TRANSACTIONCOUNTS)) {
            transactionCountsSheetDataMap =
                c.typeSheetDataMap.get(CustomReportConstants.TYPE_TRANSACTIONCOUNTS);
            if (transactionCountsSheetDataMap
                .get(CustomReportsConfig.DATA_DURATION_SAMEAS_REPGENFREQ)
                .equalsIgnoreCase(CustomReportsConfig.TRUE)) {
              model.tcrgf = true;
            } else {
              model.tcd = transactionCountsSheetDataMap.get(CustomReportsConfig.DATA_DURATION);
              model.tcrgf = false;
            }

            model.tcsn = transactionCountsSheetDataMap.get(CustomReportsConfig.SHEETNAME);
            if (transactionCountsSheetDataMap.containsKey(CustomReportsConfig.AGGREGATEFREQ)) {
              model.tct = transactionCountsSheetDataMap.get(CustomReportsConfig.AGGREGATEFREQ);
            }

            if (transactionCountsSheetDataMap.containsKey(CustomReportsConfig.FILTERBY)) {
              model.tce = transactionCountsSheetDataMap.get(CustomReportsConfig.FILTERBY);
            }

          }
        }

        if (c.typeSheetDataMap.containsKey(CustomReportConstants.TYPE_INVENTORYTRENDS)) {
          Map<String, String> inventoryTrendsSheetDataMap = new HashMap<String, String>();
          if (c.typeSheetDataMap.containsKey(CustomReportConstants.TYPE_INVENTORYTRENDS)) {
            inventoryTrendsSheetDataMap =
                c.typeSheetDataMap.get(CustomReportConstants.TYPE_INVENTORYTRENDS);
            if (inventoryTrendsSheetDataMap.get(CustomReportsConfig.DATA_DURATION_SAMEAS_REPGENFREQ)
                .equalsIgnoreCase(CustomReportsConfig.TRUE)) {
              model.itrgf = true;
            } else {
              model.itd = inventoryTrendsSheetDataMap.get(CustomReportsConfig.DATA_DURATION);
              model.itrgf = false;
            }

            model.itsn = inventoryTrendsSheetDataMap.get(CustomReportsConfig.SHEETNAME);

            if (inventoryTrendsSheetDataMap.containsKey(CustomReportsConfig.AGGREGATEFREQ)) {
              model.itt = inventoryTrendsSheetDataMap.get(CustomReportsConfig.AGGREGATEFREQ);
            }

            if (inventoryTrendsSheetDataMap.containsKey(CustomReportsConfig.FILTERBY)) {
              model.ite = inventoryTrendsSheetDataMap.get(CustomReportsConfig.FILTERBY);
            }
          }

        }

        if (c.typeSheetDataMap
            .containsKey(CustomReportConstants.TYPE_HISTORICAL_INVENTORYSNAPSHOT)) {
          Map<String, String> historicalInvSnapshotSheetDataMap = new HashMap<String, String>();
          historicalInvSnapshotSheetDataMap =
              c.typeSheetDataMap.get(CustomReportConstants.TYPE_HISTORICAL_INVENTORYSNAPSHOT);
          model.hsn = historicalInvSnapshotSheetDataMap.get(CustomReportsConfig.SHEETNAME);
          model.hd = historicalInvSnapshotSheetDataMap.get(CustomReportsConfig.DATA_DURATION);
        }

        // Get the domain's timezone
        DomainConfig dc = DomainConfig.getInstance(domainId);
        if (StringUtils.isNotEmpty(c.dailyTime)) {
          model.rgth =
              LocalDateUtil.convertTimeString(c.dailyTime, dc.getTimezone(),
                  false); // Convert from UTC to domain specific timezone
          model.rgt = "0";
        } else if (StringUtils.isNotEmpty(c.weeklyRepGenTime)) {
          model.rgth =
              LocalDateUtil.convertTimeString(c.weeklyRepGenTime, dc.getTimezone(),
                  false); // Convert from UTC to domain specific timezone
          if (c.dayOfWeek != 0) {
            model.rgtw = Integer.toString(c.dayOfWeek);
          }
          model.rgt = "1";
        } else if (StringUtils.isNotEmpty(c.monthlyRepGenTime)) {
          model.rgth =
              LocalDateUtil.convertTimeString(c.monthlyRepGenTime, dc.getTimezone(),
                  false); // Convert from UTC to domain specific timezone
          if (c.dayOfMonth != 0) {
            model.rgtm = Integer.toString(c.dayOfMonth);
          }
          model.rgt = "2";
        }
      }
    }

    return model;

  }
}


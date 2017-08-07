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

import com.logistimo.api.constants.SMSConstants;
import com.logistimo.api.models.InventoryTransactions;
import com.logistimo.api.models.SMSModel;
import com.logistimo.api.models.SMSTransactionModel;
import com.logistimo.api.util.SMSUtil;
import com.logistimo.constants.CharacterConstants;
import com.logistimo.constants.Constants;
import com.logistimo.constants.SourceConstants;
import com.logistimo.dao.JDOUtils;
import com.logistimo.entities.entity.IKiosk;
import com.logistimo.entities.service.EntitiesService;
import com.logistimo.entities.service.EntitiesServiceImpl;
import com.logistimo.exception.BadRequestException;
import com.logistimo.exception.InvalidDataException;
import com.logistimo.inventory.dao.ITransDao;
import com.logistimo.inventory.dao.impl.TransDao;
import com.logistimo.inventory.entity.IInvntry;
import com.logistimo.inventory.entity.ITransaction;
import com.logistimo.inventory.service.InventoryManagementService;
import com.logistimo.inventory.service.impl.InventoryManagementServiceImpl;
import com.logistimo.logger.XLog;
import com.logistimo.materials.entity.IMaterial;
import com.logistimo.materials.service.MaterialCatalogService;
import com.logistimo.materials.service.impl.MaterialCatalogServiceImpl;
import com.logistimo.proto.MobileInvBatchModel;
import com.logistimo.proto.MobileInvModel;
import com.logistimo.proto.MobileTransErrModel;
import com.logistimo.proto.MobileTransErrorDetailModel;
import com.logistimo.proto.MobileTransModel;
import com.logistimo.proto.MobileUpdateInvTransResponse;
import com.logistimo.services.ObjectNotFoundException;
import com.logistimo.services.ServiceException;
import com.logistimo.services.Services;
import com.logistimo.users.entity.IUserAccount;
import com.logistimo.users.service.UsersService;
import com.logistimo.users.service.impl.UsersServiceImpl;
import com.logistimo.utils.BigUtil;
import com.logistimo.utils.LocalDateUtil;

import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

/**
 * @author Mohan Raja
 */
public class SMSBuilder {

  private static final XLog xLogger = XLog.getLog(SMSBuilder.class);

  public SMSModel constructSMSModel(String message) throws ServiceException {
    SMSModel model = new SMSModel();
    List<String> fields = Arrays.asList(message.split(SMSConstants.FIELD_SEPARATOR));
    for (String field : fields) {
      String[] keyValue = field.split(SMSConstants.KEY_SEPARATOR);
      switch (keyValue[0]) {
        case SMSConstants.TOKEN:
          model.token = keyValue[1];
          break;
        case SMSConstants.TRANSACTION_TYPE:
          model.type = keyValue[1];
          break;
        case SMSConstants.INVENTORY:
          model.materials = new ArrayList<>();
          List<String>
              materials =
              Arrays.asList(keyValue[1].split(SMSConstants.MATERIAL_SEPARATOR));
          for (String material : materials) {
            String[] mat = material.split(CharacterConstants.COMMA);
            model.addMaterial(Long.parseLong(mat[0]), new BigDecimal(mat[1]),
                new BigDecimal(mat[2]));
          }
          break;
        case SMSConstants.PARTIAL_ID:
          model.partialId = keyValue[1];
          break;
        case SMSConstants.SAVE_TIMESTAMP:
          model.saveTS = new Date(Long.parseLong(keyValue[1]));
          break;
        case SMSConstants.ACTUAL_TIMESTAMP:
          model.actualTD = keyValue[1];
          break;
        case SMSConstants.USER_ID:
          model.userId = keyValue[1];
          break;
        case SMSConstants.KIOSK_ID:
          model.kioskId = Long.parseLong(keyValue[1]);
          break;
        case SMSConstants.DEST_KIOSK_ID:
          model.destKioskId = Long.parseLong(keyValue[1]);
          break;
        default:
          xLogger.warn("Unknown key,value found in SMS: " + Arrays.toString(keyValue));
      }
    }
    if (model.materials != null && model.kioskId != null) {
      updateMaterialDetails(model);
    }
    return model.isValid() ? model : null;
  }


  /**
   * Method to parse message and create a SMS transaction model
   *
   * @param message request message
   * @return Transaction Model
   * @throws ServiceException        from service layer
   * @throws ObjectNotFoundException when user, kiosk not found
   */
  public SMSTransactionModel buildSMSModel(String message)
      throws ServiceException, ObjectNotFoundException {

    SMSTransactionModel model = new SMSTransactionModel();
    try {
      //Split based on :
      List<String> fields = Arrays.asList(message.split(SMSConstants.FIELD_SEPARATOR));
      String inventoryDetails = null;
      //Get fields
      for (String field : fields) {
        String[] keyValue = field.split(SMSConstants.KEY_SEPARATOR);
        switch (keyValue[0]) {
          case SMSConstants.TOKEN:
            model.setToken(keyValue[1]);
            break;
          case SMSConstants.INVENTORY:
            //Split based on | for materials
            inventoryDetails = keyValue[1];
            break;
          case SMSConstants.PARTIAL_ID:
            model.setPartialId(keyValue[1]);
            break;
          case SMSConstants.VERSION:
            model.setVersion(keyValue[1]);
            break;
          case SMSConstants.SAVE_TIMESTAMP:
            setSendTime(model, keyValue[1]);
            break;
          case SMSConstants.ACTUAL_TIMESTAMP:
            if (keyValue[1] != null) {
              setActualTransactionDate(model, keyValue[1]);
            }
            break;
          case SMSConstants.USER_ID:
            model.setUserId(keyValue[1]);
            break;
          case SMSConstants.KIOSK_ID:
            model.setKioskId(Long.parseLong(keyValue[1]));
            break;
          default:
            xLogger.warn("Unknown key,value found in SMS: " + Arrays.toString(keyValue));
        }

      }
      processInventoryDetails(inventoryDetails, model);
      return model;
    } catch (Exception e) {
      throw new InvalidDataException("M013");
    }

  }

  /**
   * Set the actual transaction date
   *
   * @param model - Transaction model
   * @param value - parsed value
   */
  private void setActualTransactionDate(SMSTransactionModel model, String value) {
    if (model.getSendTime() != null) {
      model.setActualTransactionDate(model.getSendTime()
          - Integer.parseInt(value) * SMSConstants.DAYS_IN_MILLI_SEC);
    } else {
      model.setActualTransactionDate(
          (long) (Integer.parseInt(value) * SMSConstants.DAYS_IN_MILLI_SEC));
    }
  }

  /**
   * Set the send time
   *
   * @param model- Transaction model
   * @param value- parsed value
   */
  private void setSendTime(SMSTransactionModel model, String value) {
    Calendar calendar = GregorianCalendar.getInstance();
    if (Long.parseLong(value) * SMSConstants.MILLISECONDS > calendar
        .getTimeInMillis()) {
      //send time cannot be a future time
      throw new BadRequestException("M016");
    }
    model.setSendTime(Long.parseLong(value) * SMSConstants.MILLISECONDS);
    if (model.getActualTransactionDate() != null) {
      model.setActualTransactionDate(model.getSendTime()
          - model.getActualTransactionDate());
    }
  }

  /**
   * Method to parse and process inventory details
   *
   * @param inventoryDetails Request which has inventory details
   * @param model            Transaction Model
   */
  private void processInventoryDetails(String inventoryDetails, SMSTransactionModel model) {
    //Split based on | for materials
    List<String>
        materialsList =
        Arrays.asList(inventoryDetails.split(SMSConstants.MATERIAL_DETAIL_SEPARATOR));
    List<InventoryTransactions> inventoryTransactionsList = new ArrayList<>(materialsList.size());
    for (String material : materialsList) {
      InventoryTransactions
          inventoryTransactions =
          getInventoryTransactions(material, model.getSendTime(),
              model.getActualTransactionDate());
      inventoryTransactionsList.add(inventoryTransactions);
    }
    model.setInventoryTransactionsList(inventoryTransactionsList);
  }

  /**
   * Method to populate inventory for each material
   *
   * @param material              Material Details string
   * @param sendTime              Send Time
   * @param actualTransactionDate Actual Date of transaction
   * @return Inventory Transactions
   */
  private InventoryTransactions getInventoryTransactions(String material, Long sendTime,
                                                         Long actualTransactionDate) {

    String[] mat = material.split(SMSConstants.ENTRY_TIME_SEPARATOR);
    //Material short Id
    Long materialId = Long.parseLong(mat[0]);
    InventoryTransactions inventoryTransactions = new InventoryTransactions();
    inventoryTransactions.setMaterialShortId(materialId);
    List<MobileTransModel> mobileTransModels = new ArrayList<>();
    for (int i = 1; i < mat.length; i++) {
      String[] transactions = mat[i].split(SMSConstants.TRANSACTION_SEPARATOR);
      //set entry time
      Long entryTimeInMinutes = null;
      if (transactions[0] != null && !transactions[0].equalsIgnoreCase(CharacterConstants.EMPTY)) {
        entryTimeInMinutes =
            sendTime - (Long.parseLong(transactions[0])
                * SMSConstants.MIN_IN_MILLI_SEC);
      }
      for (int j = 1; j < transactions.length; j++) {
        MobileTransModel
            mobileTransModel =
            populateTransModel(transactions[j], sendTime, entryTimeInMinutes,
                actualTransactionDate);
        //Increase entry time by one millisecond for every transaction since the service sorts it based on entry time
        entryTimeInMinutes += 1;
        mobileTransModels.add(mobileTransModel);
      }
    }
    inventoryTransactions.setMobileTransModelList(mobileTransModels);
    return inventoryTransactions;
  }

  /**
   * Populate the mobile transaction model quantity, opening stock based on the values in the SMS request
   *
   * @param transaction        Transaction String
   * @param sendTime           Send Time
   * @param entryTimeInMinutes Entry Time in minutes
   * @param actualTransDate    Actual Transaction Date
   * @return Mobile Trans Model
   */
  private MobileTransModel populateTransModel(String transaction, Long sendTime,
                                              Long entryTimeInMinutes, Long actualTransDate) {
    SimpleDateFormat sdf = new SimpleDateFormat(Constants.DATE_FORMAT);
    MobileTransModel mobileTransModel = new MobileTransModel();
    mobileTransModel.entm = entryTimeInMinutes;
    String[] transactionDet = transaction.split(SMSConstants.COMMA_SEPARATOR);
    mobileTransModel.ty = transactionDet[0];
    mobileTransModel.ostk = transactionDet[1] != null ? new BigDecimal(transactionDet[1]) : null;
    mobileTransModel.q = transactionDet[2] != null ? new BigDecimal(transactionDet[2]) : null;
    //set actual transaction date
    Long actualTransactionDate = actualTransDate;
    if (transactionDet.length > 3 && transactionDet[3] != null && !transactionDet[3]
        .equalsIgnoreCase(CharacterConstants.EMPTY)) {
      Long days = Long.parseLong(transactionDet[3]);
      actualTransactionDate = sendTime - days * SMSConstants.DAYS_IN_MILLI_SEC;
    }
    mobileTransModel.bid =
        ((transactionDet.length > 4) && (transactionDet[4] != null) && !CharacterConstants.EMPTY
            .equalsIgnoreCase(transactionDet[4])) ? transactionDet[4] : null;
    mobileTransModel.lkid =
        (transactionDet.length > 5 && transactionDet[5] != null && !transactionDet[5]
            .equalsIgnoreCase(CharacterConstants.EMPTY)) ? Long.parseLong(transactionDet[5]) : null;
    mobileTransModel.atd = sdf.format(new Date(actualTransactionDate));
    return mobileTransModel;
  }


  public void updateMaterialDetails(SMSModel model) throws ServiceException {
    for (SMSModel.SMSInv material : model.materials) {
      InventoryManagementService ims = new InventoryManagementServiceImpl();
      IInvntry invntry = ims.getInvntryByShortID(model.kioskId, material.id);
      if (invntry != null) {
        material.matId = invntry.getMaterialId();
        material.curStk = invntry.getStock();
      }
    }
  }


  public String constructSMS(SMSModel model, String failMessage) throws ServiceException {
    return constructSMS(model, null, failMessage);
  }

  public String constructSMS(SMSModel model, Map<Long, String> errorCodes) throws ServiceException {
    return constructSMS(model, errorCodes, null);
  }

  public String constructSMS(SMSModel model, Map<Long, String> errorCodes, String failMessage)
      throws ServiceException {
    updateMaterialDetails(model);
    StringBuilder sms = new StringBuilder();
    sms.append(SMSConstants.TRANSACTION_TYPE).append(SMSConstants.KEY_SEPARATOR).append(model.type);
    if (model.partialId != null) {
      sms.append(SMSConstants.FIELD_SEPARATOR).append(SMSConstants.PARTIAL_ID)
          .append(SMSConstants.KEY_SEPARATOR).append(model.partialId);
    }
    sms.append(SMSConstants.FIELD_SEPARATOR).append(SMSConstants.SAVE_TIMESTAMP)
        .append(SMSConstants.KEY_SEPARATOR).append(model.saveTS.getTime())
        .append(SMSConstants.FIELD_SEPARATOR).append(SMSConstants.KIOSK_ID)
        .append(SMSConstants.KEY_SEPARATOR).append(model.kioskId);
    if (failMessage == null) {
      StringBuilder successInventory = new StringBuilder();
      StringBuilder failInventory = new StringBuilder();
      for (SMSModel.SMSInv material : model.materials) {
        if (!errorCodes.containsKey(material.matId)) {
          if (successInventory.length() > 0) {
            successInventory.append(SMSConstants.MATERIAL_SEPARATOR);
          }
          successInventory.append(material.id).append(CharacterConstants.COMMA)
              .append(BigUtil.getFormattedValue(material.curStk));
        } else {
          if (failInventory.length() > 0) {
            failInventory.append(SMSConstants.MATERIAL_SEPARATOR);
          }
          failInventory.append(material.id).append(CharacterConstants.COMMA)
              .append(BigUtil
                  .getFormattedValue(material.curStk == null ? BigDecimal.ZERO : material.curStk))
              .append(CharacterConstants.COMMA)
              .append(
                  errorCodes.get(material.matId) == null ? "M004" : errorCodes.get(material.matId));
        }
      }
      sms.append(SMSConstants.FIELD_SEPARATOR).append(SMSConstants.SUCCESS_INVENTORY)
          .append(SMSConstants.KEY_SEPARATOR).append(successInventory);
      sms.append(SMSConstants.FIELD_SEPARATOR).append(SMSConstants.FAIL_INVENTORY)
          .append(SMSConstants.KEY_SEPARATOR).append(failInventory);
    } else {
      sms.append(SMSConstants.FIELD_SEPARATOR).append(SMSConstants.FAIL_MESSAGE)
          .append(SMSConstants.KEY_SEPARATOR).append(failMessage);
    }
    return sms.toString();
  }

  /**
   * Method to build  SMS response
   *
   * @param model          Transaction model
   * @param mobileResponse response populated from service
   * @param errorMsg       Error message
   * @return Response String
   * @throws ServiceException        from service layer
   * @throws ObjectNotFoundException when material, kiosk, user not found
   */
  public String buildResponse(SMSTransactionModel model,
                              MobileUpdateInvTransResponse mobileResponse, String errorMsg)
      throws ServiceException, ObjectNotFoundException {
    StringBuilder response = new StringBuilder();
    StringBuilder failResp = null;

    if (model != null) {
      //Append the kiosk and part ID to response
      response.append(SMSConstants.KIOSK_ID).append(SMSConstants.KEY_SEPARATOR)
          .append(model.getKioskId()).append(SMSConstants.FIELD_SEPARATOR).
          append(SMSConstants.PARTIAL_ID).append(SMSConstants.KEY_SEPARATOR)
          .append(model.getPartialId()).append(SMSConstants.FIELD_SEPARATOR);
      if (errorMsg == null) {
        StringBuilder sucResp = new StringBuilder();
        Map<Long, List<MobileTransErrorDetailModel>> errorMap = populateErrorMap(mobileResponse);
        Map<Long, MobileInvModel> invModelHashMap = populateInvMap(mobileResponse);
        List<InventoryTransactions> list = model.getInventoryTransactionsList();
        for (InventoryTransactions transactions : list) {
          Long materialId = transactions.getMaterialShortId();
          StringBuilder matDetails = new StringBuilder();
          MobileInvModel invModel = invModelHashMap.get(transactions.getMaterialShortId());
          matDetails.append(updateMaterialDetails(invModel, materialId));
          if (errorMap != null && errorMap.containsKey(materialId)) {
            if (failResp == null) {
              failResp = new StringBuilder();
            }
            List<MobileTransErrorDetailModel> errorDetailModelList = errorMap.get(materialId);
            appendErrorResponse(errorDetailModelList, matDetails);

            matDetails.setLength(matDetails.length() - 1);
            failResp.append(matDetails)
                .append(SMSConstants.PIPE_SEPARATOR);
          } else {
            sucResp.append(matDetails).append(SMSConstants.PIPE_SEPARATOR);
          }
        }
        if (sucResp.length() > 0) {
          sucResp.setLength(sucResp.length() - 1);
          response.append(SMSConstants.SUCCESS_INVENTORY).append(SMSConstants.KEY_SEPARATOR)
              .append(sucResp);
        }
        //if there are errors, append to response
        if (failResp != null && failResp.length() > 0) {
          if (sucResp.length() > 0) {
            response.append(SMSConstants.FIELD_SEPARATOR);
          }
          failResp.setLength(failResp.length() - 1);
          response.append(SMSConstants.FAIL_INVENTORY).append(SMSConstants.KEY_SEPARATOR)
              .append(failResp);
        }
      } else {
        //append error message
        response.append(SMSConstants.FAIL_MESSAGE).append(SMSConstants.KEY_SEPARATOR)
            .append(errorMsg);
      }
    }
    return response.toString();
  }

  /**
   * Method to append the error to response with code and index
   *
   * @param errorDetailModelList Error Details List
   * @param matDetails           Material Details String
   */
  private void appendErrorResponse(List<MobileTransErrorDetailModel> errorDetailModelList,
                                   StringBuilder matDetails) {
    matDetails.append(SMSConstants.ENTRY_TIME_SEPARATOR);
    for (MobileTransErrorDetailModel mobileTransErrorDetailModel : errorDetailModelList) {
      matDetails.append(mobileTransErrorDetailModel.ec)
          .append(SMSConstants.MATERIAL_SEPARATOR).append(mobileTransErrorDetailModel.idx).
          append(SMSConstants.COMMA_SEPARATOR);
    }
  }

  /**
   * Method to add the material details to response
   *
   * @param invModel   Inventory Model
   * @param materialId Material Id
   * @return String Builder
   */
  private StringBuilder updateMaterialDetails(MobileInvModel invModel, Long materialId) {
    StringBuilder materialDetails = new StringBuilder();
    materialDetails.append(materialId)
        .append(SMSConstants.ENTRY_TIME_SEPARATOR);
    List<MobileInvBatchModel> invBatchModelList = invModel.bt != null ? invModel.bt : invModel.xbt;
    if (invBatchModelList != null && !invBatchModelList.isEmpty()) {
      for (MobileInvBatchModel batchModel : invBatchModelList) {
        //append inventory details
        appendInventoryDetails(materialDetails, batchModel.q, batchModel.alq, invModel.itq);
        //append batch Id
        materialDetails.append(SMSConstants.COMMA_SEPARATOR).append(batchModel.bid)
            .append(SMSConstants.COMMA_SEPARATOR);
      }
    } else {
      //append inventory details
      appendInventoryDetails(materialDetails, invModel.q, invModel.alq, invModel.itq);

      materialDetails.append(SMSConstants.COMMA_SEPARATOR).append(SMSConstants.COMMA_SEPARATOR);
    }
    materialDetails.setLength(materialDetails.length() - 1);
    return materialDetails;
  }

  /**
   * Append inventory details
   *
   * @param materialDetails Material Details
   * @param openingStk      Opening Stock
   * @param allocationStk   Allocated Stock
   * @param inTransitStk    In transit Stock
   */
  private void appendInventoryDetails(StringBuilder materialDetails, BigDecimal openingStk,
                                      BigDecimal allocationStk, BigDecimal inTransitStk) {
    materialDetails.append(openingStk.toBigInteger()).append(SMSConstants.COMMA_SEPARATOR);
    //append allocated quantity
    if (allocationStk != null) {
      materialDetails.append(allocationStk.toBigInteger());
    }
    materialDetails.append(SMSConstants.COMMA_SEPARATOR);

    //append in transit quantity
    if (inTransitStk != null) {
      materialDetails.append(inTransitStk.toBigInteger());
    }
  }

  /**
   * Method to populate a map with material short id and error object
   *
   * @param response from service
   * @return Error Map
   * @throws ServiceException from service layer
   */
  private Map<Long, List<MobileTransErrorDetailModel>> populateErrorMap(
      MobileUpdateInvTransResponse response) throws ServiceException {
    Map<Long, List<MobileTransErrorDetailModel>> map = null;
    InventoryManagementService service = Services.getService(InventoryManagementServiceImpl.class);
    if (response.errs != null) {
      map = new HashMap<>();
      for (MobileTransErrModel errModel : response.errs) {
        //get inventory object based on kiosk id and material id
        IInvntry invntry = service.getInventory(response.kid, errModel.mid);
        map.put(invntry.getShortId(), errModel.errdtl);
      }

    }
    return map;
  }


  /**
   * Populate a hashmap with material short id and inventory details
   *
   * @param response Resposne from service
   * @return Map with material short id and inventory model
   */
  private Map<Long, MobileInvModel> populateInvMap(MobileUpdateInvTransResponse response) {
    Map<Long, MobileInvModel> map = new HashMap<>(response.inv.size());
    for (MobileInvModel invModel : response.inv) {
      map.put(invModel.smid, invModel);
    }
    return map;
  }


  public List<ITransaction> buildInventoryTransactions(SMSModel model) {
    List<ITransaction> transactions = new ArrayList<>(model.materials.size());
    Date now = new Date();
    boolean checkBatchMgmt = ITransaction.TYPE_TRANSFER.equals(model.type);
    MaterialCatalogServiceImpl mcs = null;
    try {
      if (checkBatchMgmt) {
        EntitiesService as = Services.getService(EntitiesServiceImpl.class);
        IKiosk kiosk = as.getKiosk(model.kioskId);
        IKiosk linkedKiosk = as.getKiosk(model.destKioskId);
        checkBatchMgmt =
            !kiosk.isBatchMgmtEnabled() && linkedKiosk != null && linkedKiosk.isBatchMgmtEnabled();
        mcs = Services.getService(MaterialCatalogServiceImpl.class, Locale.ENGLISH);
      }
    } catch (ServiceException e) {
      xLogger.warn("ServiceException while getting kiosk details. Exception: {0)", e);
    }
    ITransDao transDao = new TransDao();
    for (SMSModel.SMSInv material : model.materials) {
      ITransaction transaction = JDOUtils.createInstance(ITransaction.class);
      transaction.setKioskId(model.kioskId);
      transaction.setMaterialId(material.matId);
      transaction.setType(model.type);
      transaction.setQuantity(material.quantity);
      transaction.setSourceUserId(model.userId);
      transaction.setTimestamp(now);
      transaction.setLinkedKioskId(model.destKioskId);
      if (model.actualTS != null) {
        transaction.setAtd(model.actualTS);
      }
      transaction.setSrc(SourceConstants.SMS);
      transDao.setKey(transaction);
      if (checkBatchMgmt && mcs != null) {
        try {
          IMaterial mat = mcs.getMaterial(material.matId);
          if (mat.isBatchEnabled()) {
            transaction.setMessage(
                "Transfer from batch disabled entity to batch enabled entity failed for Material"
                    + mat.getName());
            transaction.setMsgCode("M008");
          }
        } catch (ServiceException e) {
          xLogger.warn("ServiceException while getting material details. Exception: {0}", e);
        }
      }
      transactions.add(transaction);
    }
    return transactions;
  }

  /**
   * Build the transaction map with material id as key and list of associated transactions
   *
   * @param model Transaction model
   * @return Map with material id and Itransaction object
   * @throws ServiceException when user,kiosk not found
   */
  public Map<Long, List<ITransaction>> buildTransaction(SMSTransactionModel model)
      throws ServiceException {
    Map<Long, List<ITransaction>> map = new HashMap<>();
    List<ITransaction> transactionList;
    try {
      //Get source kiosk details
      EntitiesService entityService = Services.getService(EntitiesServiceImpl.class);
      IKiosk sourceKiosk = entityService.getKiosk(model.getKioskId());
      InventoryManagementService
          inventoryManagementService =
          Services.getService(InventoryManagementServiceImpl.class);
      MaterialCatalogService
          materialCatalogService =
          Services.getService(MaterialCatalogServiceImpl.class);
      //Get user details
      UsersService as = Services.getService(UsersServiceImpl.class);
      IUserAccount ua = as.getUserAccount(model.getUserId());

      List<InventoryTransactions> list = model.getInventoryTransactionsList();

      for (InventoryTransactions inventoryTransactions : list) {
        IInvntry
            invntry =
            inventoryManagementService.getInvntryByShortID(sourceKiosk.getKioskId(),
                inventoryTransactions.getMaterialShortId());
        IMaterial material = materialCatalogService.getMaterial(invntry.getMaterialId());
        inventoryTransactions.setMaterialId(material.getMaterialId());
        List<MobileTransModel>
            mobileTransModelList =
            inventoryTransactions.getMobileTransModelList();
        for (MobileTransModel mobileTransModel : mobileTransModelList) {
          ITransaction
              transaction =
              setTransactionDetails(mobileTransModel, ua, model.getKioskId(),
                  model.getActualTransactionDate(), material.getMaterialId());
          transactionList = map.get(material.getMaterialId());
          if (transactionList == null) {
            transactionList = new ArrayList<>();
          }
          transactionList.add(transaction);
          map.put(material.getMaterialId(), transactionList);
        }
      }

    } catch (ServiceException e) {
      xLogger.warn("ServiceException while getting material details. Exception: {0}", e);
      throw e;
    } catch (Exception e) {
      xLogger.warn("Exception while building transaction. Exception: {0}", e);
    }
    return map;
  }

  /**
   * Method to set details to the transaction object
   *
   * @param mobileTransModel      Mobile transaction model
   * @param ua                    User Account
   * @param kioskId               Kiosk ID
   * @param actualTransactionDate Actual Transaction Date
   * @param materialId            Material ID
   * @return ITransaction
   * @throws ParseException          when unable to parse date
   * @throws ServiceException        Exception thrown from Service Layer
   * @throws ObjectNotFoundException When kiosk not found
   */
  private ITransaction setTransactionDetails(MobileTransModel mobileTransModel, IUserAccount ua,
                                             Long kioskId, Long actualTransactionDate,
                                             Long materialId) throws
      ParseException, ServiceException, ObjectNotFoundException {
    Calendar calendar = Calendar.getInstance();
    TimeZone timeZone = SMSUtil.getUserTimeZone(ua);
    if (timeZone != null) {
      calendar.setTimeZone(timeZone);
    }
    ITransaction transaction = JDOUtils.createInstance(ITransaction.class);
    transaction.setKioskId(kioskId);
    transaction.setType(mobileTransModel.ty);
    transaction.setBatchId(mobileTransModel.bid);
    if (transaction.hasBatch()) {
      transaction.setOpeningStockByBatch(mobileTransModel.ostk);
    } else {
      transaction.setOpeningStock(mobileTransModel.ostk);
    }
    transaction.setQuantity(mobileTransModel.q);
    transaction.setSourceUserId(ua.getUserId());
    if (actualTransactionDate != null) {
      Date d = LocalDateUtil.parseCustom(mobileTransModel.atd, Constants.DATE_FORMAT, null);
      transaction.setAtd(d);
    }
    transaction.setTimestamp(new Date());
    calendar.setTimeInMillis(mobileTransModel.entm);
    transaction.setEntryTime(calendar.getTime());
    if (mobileTransModel.lkid != null) {
      transaction.setLinkedKioskId(mobileTransModel.lkid);
    }
    transaction.setSrc(SourceConstants.SMS);
    transaction.setMaterialId(materialId);
    return transaction;
  }


}

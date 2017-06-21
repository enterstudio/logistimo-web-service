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

package com.logistimo.api.controllers;

import com.google.gson.Gson;

import com.logistimo.api.models.InventoryTransactions;
import com.logistimo.api.models.SMSRequestModel;
import com.logistimo.api.models.SMSTransactionModel;
import com.logistimo.api.servlets.mobile.builders.MobileTransactionsBuilder;
import com.logistimo.api.util.SMSUtil;
import com.logistimo.exception.InvalidDataException;
import com.logistimo.inventory.TransactionUtil;
import com.logistimo.inventory.models.ErrorDetailModel;
import com.logistimo.inventory.models.MobileTransactionCacheModel;
import com.logistimo.proto.MobileMaterialTransModel;
import com.logistimo.proto.MobileUpdateInvTransResponse;

import org.apache.commons.lang.StringUtils;

import com.logistimo.communications.service.MessageService;
import com.logistimo.services.Services;
import com.logistimo.logger.XLog;
import com.logistimo.api.builders.SMSBuilder;
import com.logistimo.api.models.SMSModel;
import com.logistimo.api.auth.Authoriser;
import com.logistimo.inventory.entity.ITransaction;
import com.logistimo.inventory.service.InventoryManagementService;
import com.logistimo.inventory.service.impl.InventoryManagementServiceImpl;
import com.logistimo.users.entity.IUserAccount;
import com.logistimo.users.service.UsersService;
import com.logistimo.users.service.impl.UsersServiceImpl;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;

import java.io.UnsupportedEncodingException;
import java.util.*;


/**
 * @author Mohan Raja
 */
@Controller
@RequestMapping("/sms")
public class SMSController {
  private static final XLog xLogger = XLog.getLog(SMSController.class);
  private SMSBuilder builder = new SMSBuilder();

  @RequestMapping(value = {"", "/"}, method = {RequestMethod.GET, RequestMethod.POST})
  public
  @ResponseBody
  void updateInventoryTransactions(HttpServletRequest request) {

    SMSRequestModel smsReqModel = null;
    IUserAccount ua = null;
    SMSModel model = null;

    try {
      smsReqModel = SMSUtil.processMessage(request);
      if (StringUtils.isBlank(smsReqModel.getMessage())) {
        xLogger.warn("Empty SMS received from {0} on {1}", smsReqModel.getAddress(),
            smsReqModel.getReceivedOn());
        return;
      }
      model = builder.constructSMSModel(smsReqModel.getMessage());

      if (model == null) {
        xLogger
            .severe("Invalid sms format. Not all required fields available. From: {0} Message: {1}",
                smsReqModel.getAddress(), smsReqModel.getMessage());
        return;
      }
      UsersService as = Services.getService(UsersServiceImpl.class);
      ua = as.getUserAccount(model.userId);
      model.domainId = ua.getDomainId();

      model.actualTS = SMSUtil.populateActualTransactionDate(ua.getDomainId(), model.actualTD);

      if (!Authoriser
          .authoriseSMS(smsReqModel.getAddress(), ua.getMobilePhoneNumber(), model.userId,
              model.token)) {
        xLogger.warn("SMS authentication failed. Mobile: {0}, User Mobile: {1}, Message: {2}",
            smsReqModel.getAddress(), ua.getMobilePhoneNumber(), smsReqModel.getMessage());
        return;
      }

      Map<Long, String> errorCodes = new HashMap<>(0);
      if (model.actualTS != null && SMSUtil
          .isDuplicateMsg(model.saveTS.getTime(), model.userId, model.kioskId, model.partialId)) {
        xLogger
            .info("Duplicate transaction found while processing SMS {0}", smsReqModel.getMessage());
      } else {
        List<ITransaction> transactions = builder.buildInventoryTransactions(model);
        List<ITransaction> tempErrorTrans = null;
        Iterator<ITransaction> i = transactions.iterator();
        while (i.hasNext()) {
          ITransaction t = i.next();
          if (t.getMsgCode() != null) {
            if (tempErrorTrans == null) {
              tempErrorTrans = new ArrayList<>(1);
            }
            tempErrorTrans.add(t);
            i.remove();
          }
        }
        InventoryManagementService
            ims =
            Services.getService(InventoryManagementServiceImpl.class);
        List<ITransaction> errorTrans;
        if (tempErrorTrans != null
            && transactions.size() == 0) { // All transactions are error transactions
          errorTrans = new ArrayList<>(1);
        } else {
          errorTrans = ims.updateInventoryTransactions(model.domainId, transactions, true);
        }
        errorCodes = new HashMap<>(errorTrans.size());
        for (ITransaction errorTran : errorTrans) {
          errorCodes.put(errorTran.getMaterialId(), errorTran.getMsgCode());
        }
        if (tempErrorTrans != null) {
          for (ITransaction errorTran : tempErrorTrans) {
            errorCodes.put(errorTran.getMaterialId(), errorTran.getMsgCode());
          }
        }
      }
      String smsMessage = builder.constructSMS(model, errorCodes);
      MessageService ms = MessageService.getInstance(MessageService.SMS, ua.getCountry());
      ms.send(ua, smsMessage, MessageService.NORMAL, null, null, null);
    } catch (UnsupportedEncodingException e) {
      xLogger.warn("SMS Encoding issue received  {0} , error e: {1}", smsReqModel.getMessage(), e);
    } catch (Exception e) {
      xLogger
          .severe("Error in processing SMS {0} from {1} on {2}", smsReqModel.getMessage(),
              smsReqModel.getAddress(), smsReqModel.getReceivedOn(),
              e);
      try {
        if (ua != null) {
          MessageService ms = MessageService.getInstance(MessageService.SMS, ua.getCountry());
          ms.send(ua, builder.constructSMS(model, "M004"), MessageService.NORMAL, null, null, null);
        }
      } catch (Exception ignored) {
        // ignore
      }
    }
  }

  /**
   * Method to process transaction
   *
   * @param request http request
   */
  @RequestMapping(value = "/updateTransaction", method = {RequestMethod.GET, RequestMethod.POST})
  public @ResponseBody
  void updateTransactions(HttpServletRequest request) {
    IUserAccount ua = null;
    SMSRequestModel smsMessage = null;
    SMSTransactionModel model = null;
    String responseMsg;
    Map<Long, List<ErrorDetailModel>> midErrorDetailModelsMap = null;
    boolean isDuplicate;
    try {
      //process message
      smsMessage = SMSUtil.processMessage(request);
      if (StringUtils.isBlank(smsMessage.getMessage())) {
        //sms message received is empty
        xLogger.warn("Empty SMS received from {0} on {1}", smsMessage.getAddress(),
            smsMessage.getReceivedOn());
        return;
      }
      //populate model
      model = builder.buildSMSModel(smsMessage.getMessage());
      //Get user details
      UsersService as = Services.getService(UsersServiceImpl.class);
      ua = as.getUserAccount(model.getUserId());
      //authorise user
      if (!Authoriser
          .authoriseSMS(smsMessage.getAddress(), ua.getMobilePhoneNumber(), model.getUserId(),
              model.getToken())) {
        xLogger.warn("SMS authentication failed. Mobile: {0}, User Mobile: {1}, Message: {2}",
            smsMessage.getAddress(), ua.getMobilePhoneNumber(),
            smsMessage.getMessage());
        return;
      }
      isDuplicate =
          SMSUtil.isDuplicateMsg(model.getSendTime(), model.getUserId(), model.getKioskId(),
              model.getPartialId());
      //check if duplicate transaction
      if (isDuplicate) {
        xLogger
            .info("Duplicate transaction found while processing SMS {0}", smsMessage.getMessage());

      } else {
        Map<Long, List<ITransaction>> transactionMap = builder.buildTransaction(model);
        InventoryManagementService ims = Services.getService(InventoryManagementServiceImpl.class);
        midErrorDetailModelsMap =
            ims.updateMultipleInventoryTransactions(transactionMap, ua.getDomainId(),
                ua.getUserId());
      }
      MobileUpdateInvTransResponse
          mobileUpdateInvTransResponse =
          createResponse(model, midErrorDetailModelsMap, ua.getDomainId(), isDuplicate);
      //send SMS
      responseMsg = builder.buildResponse(model, mobileUpdateInvTransResponse, null);
      MessageService ms = MessageService.getInstance(MessageService.SMS, ua.getCountry());
      ms.send(ua, responseMsg, MessageService.NORMAL, null, null, null);
    } catch (UnsupportedEncodingException e) {
      xLogger.severe("Error in Decoding SMS.", e);
    } catch (InvalidDataException e) {
      xLogger.warn("Error in processing SMS.", e);
      sendErrorResponse(smsMessage, ua, "M013", model);
    } catch
        (Exception e) {
      xLogger.warn("Exception in processing SMS.", e);
      sendErrorResponse(smsMessage, ua, "M004", model);
    }
  }

  /**
   * Method to send error response
   *
   * @param smsRequestModel SMS Model
   * @param userAccount     user's details
   * @param errorMsg        error message
   * @param model           Transaction model
   */
  private void sendErrorResponse(SMSRequestModel smsRequestModel, IUserAccount userAccount,
                                 String errorMsg, SMSTransactionModel model) {

    try {
      if (userAccount == null && smsRequestModel!=null && smsRequestModel.getMessage() != null) {
        String userId = SMSUtil.getUserId(smsRequestModel.getMessage());
        userAccount = Services.getService(UsersServiceImpl.class).getUserAccount(userId);
      }
      if (smsRequestModel != null && userAccount != null) {
        MessageService
            ms =
            MessageService.getInstance(MessageService.SMS, userAccount.getCountry());
        ms.send(userAccount, builder.buildResponse(model, null, errorMsg), MessageService.NORMAL,
            null,
            null, null);
      }
    } catch (Exception ignored) {
      xLogger.severe("Error in sending response SMS.", ignored);
    }
  }

  /**
   * Create mobile response based on the response from service
   *
   * @param model-                  Transaction model
   * @param midErrorDetailModelsMap errors returned by service
   * @param domainId                domain id of the user
   * @param isDuplicate             flag to indicate if the request is duplicate
   * @return Response
   */
  private MobileUpdateInvTransResponse createResponse(SMSTransactionModel model,
                                                      Map<Long, List<ErrorDetailModel>> midErrorDetailModelsMap,
                                                      Long domainId, boolean isDuplicate) {
    MobileUpdateInvTransResponse mobUpdateInvTransResp = null;
    if (isDuplicate) {
      MobileTransactionCacheModel
          cacheModel =
          TransactionUtil.getObjectFromCache(String.valueOf(model.getSendTime()), model.getUserId(),
              model.getKioskId(), model.getPartialId());
      if (cacheModel != null) {
        mobUpdateInvTransResp =
            new Gson().fromJson(cacheModel.getResponse(), MobileUpdateInvTransResponse.class);
      }
    } else {
      mobUpdateInvTransResp =
          new MobileTransactionsBuilder()
              .buildMobileUpdateInvTransResponse(domainId, model.getUserId(), model.getKioskId(),
                  model.getPartialId(),
                  null, midErrorDetailModelsMap, populateMaterialList(model));
      if (mobUpdateInvTransResp != null) {
        String mobUpdateInvTransRespJsonStr = new Gson().toJson(mobUpdateInvTransResp);
        TransactionUtil.setObjectInCache(String.valueOf(model.getSendTime()), model.getUserId(),
            model.getKioskId(), model.getPartialId(),
            new MobileTransactionCacheModel(TransactionUtil.COMPLETED,
                mobUpdateInvTransRespJsonStr));
      }
    }
    return mobUpdateInvTransResp;
  }

  /**
   * Method to build material list for response
   *
   * @param model Transaction model
   * @return List of Mobile trnasaction models
   */
  private List<MobileMaterialTransModel> populateMaterialList(SMSTransactionModel model) {
    List<MobileMaterialTransModel> mobileMaterialTransModelList = new ArrayList<>();
    for (InventoryTransactions inventoryTransactions : model.getInventoryTransactionsList()) {
      MobileMaterialTransModel materialTransModel = new MobileMaterialTransModel();
      materialTransModel.mid = inventoryTransactions.getMaterialId();
      materialTransModel.trns = inventoryTransactions.getMobileTransModelList();
      mobileMaterialTransModelList.add(materialTransModel);
    }
    return mobileMaterialTransModelList;
  }

}

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


import com.logistimo.api.builders.SMSBuilder;
import com.logistimo.api.constants.SMSConstants;
import com.logistimo.api.models.InventoryTransactions;
import com.logistimo.api.models.SMSRequestModel;
import com.logistimo.api.models.SMSTransactionModel;
import com.logistimo.api.servlets.mobile.builders.MobileTransactionsBuilder;
import com.logistimo.api.util.SMSUtil;
import com.logistimo.auth.GenericAuthoriser;
import com.logistimo.communications.service.MessageService;
import com.logistimo.exception.InvalidDataException;
import com.logistimo.exception.LogiException;
import com.logistimo.inventory.TransactionUtil;
import com.logistimo.inventory.entity.ITransaction;
import com.logistimo.inventory.models.ErrorDetailModel;
import com.logistimo.inventory.service.InventoryManagementService;
import com.logistimo.inventory.service.impl.InventoryManagementServiceImpl;
import com.logistimo.logger.XLog;
import com.logistimo.proto.MobileMaterialTransModel;
import com.logistimo.proto.MobileUpdateInvTransResponse;
import com.logistimo.services.Services;
import com.logistimo.users.entity.IUserAccount;
import com.logistimo.users.service.UsersService;
import com.logistimo.users.service.impl.UsersServiceImpl;

import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;


/**
 * @author Mohan Raja
 */
@Controller
@RequestMapping("/sms")
public class SMSController {
  private static final XLog xLogger = XLog.getLog(SMSController.class);
  private SMSBuilder builder = new SMSBuilder();

  /**
   * Method to process transaction
   *
   * @param request http request
   */
  @RequestMapping(value = {"", "/"}, method = {RequestMethod.GET, RequestMethod.POST})
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
      if (!SMSConstants.V2.equals(model.getVersion())) {
        xLogger.severe("V=" + model.getVersion() + " version is not supported");
        return;
      }
      UsersService as = Services.getService(UsersServiceImpl.class);
      ua = as.getUserAccount(model.getUserId());
      //authorise user
      if (!GenericAuthoriser
          .authoriseSMS(smsMessage.getAddress(), ua.getMobilePhoneNumber(), model.getUserId(),
              model.getToken())) {
        xLogger.warn("SMS authentication failed. Mobile: {0}, User Mobile: {1}, Message: {2}",
            smsMessage.getAddress(), ua.getMobilePhoneNumber(),
            smsMessage.getMessage());
        return;
      }
      isDuplicate =
          SMSUtil.isDuplicateMsg(model.getSendTime() / 1000, model.getUserId(), model.getKioskId(),
              model.getPartialId());
      //check if duplicate transaction
      Map<Long, List<ITransaction>> transactionMap = builder.buildTransaction(model);

      if (isDuplicate) {
        Integer status = TransactionUtil.getObjectFromCache(String.valueOf(model.getSendTime()),
                model.getUserId(), model.getKioskId(), model.getPartialId());
        if (status != null && TransactionUtil.IN_PROGRESS == status) {
            throw new LogiException("Transaction is in progress");
        }
      } else {
        InventoryManagementService ims =
            Services.getService(InventoryManagementServiceImpl.class);
        midErrorDetailModelsMap =
            ims.updateMultipleInventoryTransactions(transactionMap, ua.getDomainId(),
                ua.getUserId());
      }
      MobileUpdateInvTransResponse
          mobileUpdateInvTransResponse =
          createResponse(model, midErrorDetailModelsMap, ua.getDomainId(), isDuplicate);
      if (mobileUpdateInvTransResponse != null) {
        //send SMS
        responseMsg = builder.buildResponse(model, mobileUpdateInvTransResponse, null);
        MessageService ms = MessageService.getInstance(MessageService.SMS, ua.getCountry());
        ms.send(ua, responseMsg, MessageService.NORMAL, null, null, null);
      }
    } catch (UnsupportedEncodingException e) {
      xLogger.severe("Error in Decoding SMS.", e);
    } catch (InvalidDataException e) {
      xLogger.warn("Error in processing SMS.", e);
      sendErrorResponse(smsMessage, ua, "M013", model);
    } catch (Exception e) {
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
    MobileUpdateInvTransResponse mobUpdateInvTransResp =
        new MobileTransactionsBuilder()
            .buildMobileUpdateInvTransResponse(domainId, model.getUserId(), model.getKioskId(),
                model.getPartialId(),
                null, midErrorDetailModelsMap, populateMaterialList(model));
    if (!isDuplicate && mobUpdateInvTransResp != null) {
        TransactionUtil.setObjectInCache(String.valueOf(model.getSendTime()), model.getUserId(),
            model.getKioskId(), model.getPartialId(),
            TransactionUtil.COMPLETED);
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

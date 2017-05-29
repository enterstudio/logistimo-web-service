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

import org.apache.commons.lang.StringUtils;
import com.logistimo.api.communications.MessageHandler;
import com.logistimo.communications.service.MessageService;
import com.logistimo.config.models.DomainConfig;
import com.logistimo.services.Services;
import com.logistimo.constants.CharacterConstants;
import com.logistimo.logger.XLog;
import com.logistimo.api.builders.SMSBuilder;
import com.logistimo.api.models.SMSModel;
import com.logistimo.api.auth.Authoriser;
import com.logistimo.inventory.TransactionUtil;
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

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import javax.servlet.http.HttpServletRequest;

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
    String message = request.getParameter(MessageHandler.MESSAGE);
    if (message != null) {
      try {
        message = URLDecoder.decode(message, "UTF-8");
      } catch (UnsupportedEncodingException e) {
        xLogger.warn("SMS Encoding issue received  {0} , error e: {1}", message, e);
        return;
      }
      message = message.substring(message.indexOf(CharacterConstants.SPACE) + 1);
    }
    String mobilenumber = request.getParameter(MessageHandler.ADDRESS);
    String receivedon = request.getParameter(MessageHandler.RECEIVEDON);
    if (receivedon == null) {
      receivedon = request.getParameter("amp;receivedon");
    }
    if (StringUtils.isBlank(message)) {
      xLogger.warn("Empty SMS received from {0} on {1}", mobilenumber, receivedon);
      return;
    }
    IUserAccount ua = null;
    SMSModel model = null;
    try {
      model = builder.constructSMSModel(message);
      if (model == null) {
        xLogger
            .severe("Invalid sms format. Not all required fields available. From: {0} Message: {1}",
                mobilenumber, message);
        return;
      }
      UsersService as = Services.getService(UsersServiceImpl.class);
      ua = as.getUserAccount(model.userId);
      model.domainId = ua.getDomainId();

      DomainConfig c = DomainConfig.getInstance(ua.getDomainId());
      Calendar calendar = GregorianCalendar.getInstance();
      if (StringUtils.isNotEmpty(c.getTimezone())) {
        calendar.setTimeZone(TimeZone.getTimeZone(c.getTimezone()));
      }
      if (model.actualTD != null) {
        String[] date = model.actualTD.split("/");
        int day = Integer.parseInt(date[0]);
        int month = Integer.parseInt(date[1]) - 1;
        int year = Integer.parseInt(date[2]);
        calendar.set(year, month, day, 0, 0, 0);
        model.actualTS = calendar.getTime();
      }

      if (!Authoriser
          .authoriseSMS(mobilenumber, ua.getMobilePhoneNumber(), model.userId, model.token)) {
        xLogger.warn("SMS authentication failed. Mobile: {0}, User Mobile: {1}, Message: {2}",
            mobilenumber, ua.getMobilePhoneNumber(), message);
        return;
      }
      boolean isDuplicateUpdate = model.saveTS != null &&
          (TransactionUtil
              .deduplicateBySaveTimePartial(String.valueOf(model.saveTS.getTime()), model.userId,
                  String.valueOf(model.kioskId), model.partialId));
      Map<Long, String> errorCodes = new HashMap<>(0);
      if (isDuplicateUpdate) {
        xLogger.info("Duplicate transaction found while processing SMS {0}", message);
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
    } catch (Exception e) {
      xLogger
          .severe("Error in processing SMS {0} from {1} on {2}", message, mobilenumber, receivedon,
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
}

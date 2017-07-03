
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

package com.logistimo.api.util;

import com.logistimo.api.communications.MessageHandler;
import com.logistimo.api.constants.SMSConstants;
import com.logistimo.api.models.SMSRequestModel;
import com.logistimo.config.models.DomainConfig;
import com.logistimo.constants.CharacterConstants;
import com.logistimo.inventory.TransactionUtil;
import com.logistimo.services.ObjectNotFoundException;
import com.logistimo.services.ServiceException;
import com.logistimo.users.entity.IUserAccount;

import org.apache.commons.lang.StringUtils;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.TimeZone;

import javax.servlet.http.HttpServletRequest;

public class SMSUtil {

  private SMSUtil() throws IllegalAccessException {
    throw new IllegalAccessException("Private utility class");
  }

  /**
   * Method to parse the message
   */
  public static SMSRequestModel processMessage(HttpServletRequest request)
      throws UnsupportedEncodingException {
    SMSRequestModel smsReqModel = null;
    String message = request.getParameter(MessageHandler.MESSAGE);
    if (message != null) {

      message = URLDecoder.decode(message, "UTF-8");

      message = message.substring(message.indexOf(CharacterConstants.SPACE) + 1);
      smsReqModel = new SMSRequestModel();
      smsReqModel.setMessage(message);
    }
    if (smsReqModel != null) {
      String mobilenumber = request.getParameter(MessageHandler.ADDRESS);

      smsReqModel.setAddress(mobilenumber);
      String receivedon = request.getParameter(MessageHandler.RECEIVEDON);
      if (receivedon == null) {
        receivedon = request.getParameter("amp;receivedon");
      }
      smsReqModel.setReceivedOn(receivedon);
    }
    return smsReqModel;
  }

  public static Date populateActualTransactionDate(Long domainId, String actualTD) {
    DomainConfig c = DomainConfig.getInstance(domainId);
    Calendar calendar = GregorianCalendar.getInstance();
    if (c != null && StringUtils.isNotEmpty(c.getTimezone())) {
      calendar.setTimeZone(TimeZone.getTimeZone(c.getTimezone()));
    }
    if (actualTD != null) {
      String[] date = actualTD.split("/");
      int day = Integer.parseInt(date[0]);
      int month = Integer.parseInt(date[1]) - 1;
      int year = Integer.parseInt(date[2]);
      calendar.set(year, month, day, 0, 0, 0);
    }
    return calendar.getTime();
  }

  public static boolean isDuplicateMsg(Long timeInMiliSec, String userId, Long kioskId,
                                       String partId) {
    return
        (TransactionUtil
            .deduplicateBySaveTimePartial(String.valueOf(timeInMiliSec), userId,
                String.valueOf(kioskId), partId));
  }


  /**
   * Method to get the domain timezone
   *
   * @param ua User Account
   * @return Time zone
   * @throws ServiceException        from service layer
   * @throws ObjectNotFoundException when domain config not found
   */
  public static TimeZone getUserTimeZone(IUserAccount ua)
      throws ServiceException, ObjectNotFoundException {
    DomainConfig c;
    c = DomainConfig.getInstance(ua.getDomainId());
    if ((c != null) && StringUtils.isNotEmpty(c.getTimezone())) {
      return TimeZone.getTimeZone(c.getTimezone());
    }
    return null;
  }

  /**
   * Get user id from the message request
   */
  public static String getUserId(String smsReq) {
    List<String> fields = Arrays.asList(smsReq.split(SMSConstants.FIELD_SEPARATOR));
    for (String field : fields) {
      if (field.startsWith(SMSConstants.USER_ID)) {
        String[] keyValue = field.split(SMSConstants.KEY_SEPARATOR);
        return keyValue[1];
      }
    }
    return null;
  }
}

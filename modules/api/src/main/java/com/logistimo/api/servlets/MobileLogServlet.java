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

package com.logistimo.api.servlets;


import com.logistimo.AppFactory;
import com.logistimo.api.util.RESTUtil;
import com.logistimo.services.cache.MemcacheService;

import org.apache.commons.lang.StringUtils;
import com.logistimo.services.ServiceException;
import com.logistimo.proto.RestConstantsZ;
import com.logistimo.utils.PasswordEncoder;
import com.logistimo.logger.XLog;
import com.logistimo.exception.UnauthorizedException;
import com.logistimo.users.entity.IUserAccount;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Created by mohansrinivas on 11/16/15.
 */
public class MobileLogServlet extends HttpServlet {
  private static final XLog _LOGGER = XLog.getLog(MobileLogServlet.class);
  private static final XLog _LOGGER_SERVER = XLog.getLog(XLog.class);
  private static final Integer _CACHE_TIME_OUT = 1200000;

  public void doPost(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, java.io.IOException {

    String strUserId = request.getParameter(RestConstantsZ.USER_ID);
    String password = request.getParameter(RestConstantsZ.PASSWORD);
    InputStream inputStream;
    BufferedReader mobileLog = null;
    IUserAccount account = null;
    try {
      account = RESTUtil.authenticate(strUserId, password, null, request, response);
    } catch (ServiceException | UnauthorizedException e) {
      _LOGGER_SERVER.warn("Authentication failed : Invalid token");
      response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
      return;
    }

    try {
      inputStream = request.getInputStream();
      mobileLog = new BufferedReader(new InputStreamReader(inputStream));
      MemcacheService cache = AppFactory.get().getMemcacheService();
      String duplicateIdentify = mobileLog.readLine();
      String dataRow;

      //for handling the duplicate records
      while (duplicateIdentify != null && duplicateIdentify.trim().isEmpty()) {
        duplicateIdentify =
            mobileLog
                .readLine();//read until valid line - to handle the cases where first line or some lines comes empty
      }

      if (duplicateIdentify == null || StringUtils.isEmpty(duplicateIdentify)) {
        response.setStatus(422); // Unprocessable Entity
        return;
      }
      if (cache != null) {
        if (cache.get("LOG_" + PasswordEncoder.MD5(duplicateIdentify)) != null) {
          // skip this file as it is a duplicate log
        } else {
          cache.put("LOG_" + PasswordEncoder.MD5(duplicateIdentify), duplicateIdentify,
              _CACHE_TIME_OUT);
          _LOGGER.info(duplicateIdentify);
          while ((dataRow = mobileLog.readLine()) != null) {
            _LOGGER.info(dataRow);
          }
        }
      }


    } catch (Exception ex) {
      _LOGGER_SERVER.severe("Mobile log read failed", ex);
      response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
    } finally {
      mobileLog.close();
    }

  }

  public void doGet(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, java.io.IOException {
    throw new ServletException(
        "GET method used with " + getClass().getName() + ": POST method required.");
  }

}

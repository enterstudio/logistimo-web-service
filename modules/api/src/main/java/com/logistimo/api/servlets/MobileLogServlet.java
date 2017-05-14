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

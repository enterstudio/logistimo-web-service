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

package com.logistimo.api.servlets.mobile;

import com.logistimo.api.auth.AuthenticationUtil;
import com.logistimo.api.servlets.JsonRestServlet;
import com.logistimo.auth.service.AuthenticationService;
import com.logistimo.auth.service.impl.AuthenticationServiceImpl;
import com.logistimo.services.utils.ConfigUtil;

import org.apache.commons.lang.StringUtils;
import com.logistimo.communications.MessageHandlingException;
import com.logistimo.config.models.DomainConfig;
import com.logistimo.pagination.PageParams;
import com.logistimo.api.security.SecurityMgr;
import com.logistimo.services.ObjectNotFoundException;
import com.logistimo.services.Resources;
import com.logistimo.services.ServiceException;
import com.logistimo.services.Services;
import com.logistimo.proto.RestConstantsZ;
import com.logistimo.api.servlets.mobile.json.JsonOutput;
import com.logistimo.constants.Constants;
import com.logistimo.utils.LocalDateUtil;
import com.logistimo.api.util.RESTUtil;

import com.logistimo.api.util.SessionMgr;
import com.logistimo.constants.SourceConstants;
import com.logistimo.logger.XLog;
import com.logistimo.exception.UnauthorizedException;
import com.logistimo.users.entity.IUserAccount;
import com.logistimo.users.entity.IUserToken;
import com.logistimo.users.service.UsersService;
import com.logistimo.users.service.impl.UsersServiceImpl;

import java.io.IOException;
import java.text.ParseException;
import java.util.Date;
import java.util.InputMismatchException;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import javax.jdo.JDOObjectNotFoundException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Newer login servlet conforming to version 01 of REST protocol
 *
 * @author Arun
 */

@SuppressWarnings("serial")
public class LoginServlet extends JsonRestServlet {

  private static final String USER_AGENT = "User-Agent";
  private static final String DEVICE_DETAILS = "Device-Details";
  private static final String DEVICE_PROFILE = "Device-Profile";
  private static final String START = "start";
  private static final String FORGOT_PASSWORD = "fp";
  private static final String DEFAULT_VERSION = "01";
  private static final String OTP = "otp";
  private static final String AU = "au";
  private static final String TYPE_MOBILE = "p";
  private static final String TYPE_EMAIL = "e";
  private static final String AU_VAL = "o";
  // Added a logger to help debug this servlet's behavior (arun, 1/11/09)
  private static final XLog xLogger = XLog.getLog(LoginServlet.class);
  private static boolean isGAE = ConfigUtil.getBoolean("gae.deployment", true);

  // Set the user agent string in the user object, if it has changed; return true, if there is a change, else false if not change to user-agent string
  private static boolean setUserAgent(IUserAccount user, String userAgentStr)
      throws ServiceException {
    String curUserAgent = user.getUserAgent();
    if (curUserAgent != null && curUserAgent.equals(userAgentStr)) {
      return false; // will not set it, given no change
    } else {
      user.setPreviousUserAgent(curUserAgent);
      user.setUserAgent(userAgentStr);
    }
    return true; // implies userAgent changed
  }

  public void processGet(HttpServletRequest req, HttpServletResponse resp,
                         ResourceBundle backendMessages, ResourceBundle messages)
      throws IOException, ServiceException {
    String action = req.getParameter(RestConstantsZ.ACTION);
    if (RestConstantsZ.ACTION_LOGIN.equalsIgnoreCase(action)) {
      authenticateUser(req, resp, backendMessages, messages);
    } else if (RestConstantsZ.ACTION_LOGOUT.equalsIgnoreCase(action)) {
      xLogger.info(
          "Logged out - does nothing, just there for backward compatibility, if at all needed");
    } else if (FORGOT_PASSWORD.equalsIgnoreCase(action)) {
      validateForgotPassword(req, resp, backendMessages);
    } else {
      throw new ServiceException("Invalid action: " + action);
    }
  }

  public void processPost(HttpServletRequest req, HttpServletResponse resp,
                          ResourceBundle backendMessages, ResourceBundle messages)
      throws IOException, ServiceException {
    processGet(req, resp, backendMessages, messages);
  }

  public void authenticateUser(HttpServletRequest req, HttpServletResponse resp,
                               ResourceBundle backendMessages, ResourceBundle messages)
      throws IOException {
    xLogger.fine("Entering authenticateUser");
    UsersService as = null;
    IUserAccount user = null;
    AuthenticationService aus;
    boolean status = true;
    String message = null;
    PageParams pageParams = null;
    Long domainId = null;
    boolean forceIntegerForStock = false;
    String jsonString = null;
    boolean onlyAuthenticate = false;
    Date start = null;
    String minResponseCode = null;
    String locale = String.valueOf(new Locale(Constants.LANG_DEFAULT));
    // Getting config by token
    String authtoken = req.getHeader(Constants.TOKEN);
    String sourceInitiatorStr = req.getHeader(Constants.ACCESS_INITIATOR);
    int actionInitiator = -1;
    if (sourceInitiatorStr != null) {
      try {
        actionInitiator = Integer.parseInt(sourceInitiatorStr);
      } catch (NumberFormatException e) {

      }
    }
    if (authtoken != null) {
      try {
        user = AuthenticationUtil.authenticateToken(authtoken, actionInitiator);
        if (RESTUtil.switchToNewHostIfRequired(user, resp)) {
          xLogger.warn("Switching user {0} to new host...", user.getUserId());
          return;
        }
        domainId = user.getDomainId();
      } catch (ServiceException | ObjectNotFoundException | JDOObjectNotFoundException | UnauthorizedException e) {
        xLogger.warn("Invalid token: ", e);
        message = "Invalid token";
        status = false;
        try {
          //	jsonString = RESTUtil.getJsonOutputAuthenticate(status, null, message, null, locale, null, backendMessages, false, false, null, null).toJSONString();
          jsonString =
              RESTUtil.getJsonOutputAuthenticate(status, user, message, null, locale, null,
                  backendMessages, false, false, null, null);
        } catch (Exception e1) {
          xLogger.warn("Protocol exception after data formatting error (during login): {0}",
              e.getMessage());
          resp.setStatus(500);
          return;
        }
      }
    } else {
      // Get request parameters
      String userId = req.getParameter(RestConstantsZ.USER_ID);
      String password = req.getParameter(RestConstantsZ.PASSWORD);
      String version = req.getParameter(RestConstantsZ.VERSION);
      locale = req.getParameter(RestConstantsZ.LOCALE);
      minResponseCode =
          req.getParameter(
              RestConstantsZ.MIN_RESPONSE); // whether min. data is to be sent back - "1" = only kiosk info., in case of multiple kiosks; "2" = same as "1", but also do NOT send related kiosks info. (for each kiosk); null implies send back everything (kiosk info., materials and related kiosk info.)
      ///boolean minResponse = "1".equals( minResponseStr );
      String onlyAuthenticateStr = req.getParameter(RestConstantsZ.ONLY_AUTHENTICATE);
      onlyAuthenticate = (onlyAuthenticateStr != null);

      // Get the size & offset, if available
      String sizeStr = req.getParameter(RestConstantsZ.SIZE);
      String offsetStr = req.getParameter(Constants.OFFSET);
      int offset = 0;
      if (StringUtils.isNotBlank(offsetStr)) {
        try {
          offset = Integer.parseInt(offsetStr);
        } catch (Exception e) {
          xLogger.warn("Invalid offset {0}: {1}", offsetStr, e.getMessage());
        }
      }
      // Get page params, if any (allow NULL possibility to enable backward compatibility, where size/cursor is never sent)
      if (sizeStr != null && !sizeStr.isEmpty()) {
        try {
          int size = Integer.parseInt(sizeStr);
          pageParams = new PageParams(offset, size);
        } catch (Exception e) {
          xLogger.warn("Invalid size {0}: {1}", sizeStr, e.getMessage());
        }
      }
      // Get the user-agent and device details from header, if available
      String userAgentStr = req.getHeader(USER_AGENT);
      String deviceDetails = req.getHeader(DEVICE_DETAILS);
      String deviceProfile = req.getHeader(DEVICE_PROFILE);
      if (userAgentStr == null) {
        userAgentStr = "";
      }
      ///if ( userAgentStr != null && userAgentStr.length() <= "UNTRUSTED/1.0,gzip(gfe)".length() ) {
      // Add device details to user-agent, if sent
      if (deviceDetails != null && !deviceDetails.isEmpty()) {
        userAgentStr += " [Device-details: " + deviceDetails + "]";
      }
      if (deviceProfile != null && !deviceProfile.isEmpty()) {
        userAgentStr += " [Device-Profile: " + deviceProfile + "]";
      }
      ///xLogger.info( "Device details: {0}; Device profile: {1}", deviceDetails, deviceProfile );
      // Get locale
      if (locale == null || locale.isEmpty()) {
        locale = Constants.LANG_DEFAULT;
      }
      // Get the user's IP address, if available
      String ipAddress = isGAE ? req.getRemoteAddr() : req.getHeader("X-REAL-IP");
      xLogger.fine("ip: {0}, headers: {1}", ipAddress, req.getHeader("X-Forwarded-For"));
      // Init. flags
      String appVersion = null;
      // Get the start date and time
      String startDateStr = req.getParameter(START);
      try {
        as = Services.getService(UsersServiceImpl.class);
        // Check if user ID and password is sent as Basic auth. header
        SecurityMgr.Credentials creds = SecurityMgr.getUserCredentials(req);
        if (creds != null) {
          userId = creds.userId;
          password = creds.password;
        }
        if (userId != null && password != null) { // no problems with userId/password so far
          // Authenticate user
          user = as.authenticateUser(userId, password, SourceConstants.MOBILE);
          // Get user details
          if (user != null) {
            // Switch the user to another host, if that is enabled
            if (status && RESTUtil.switchToNewHostIfRequired(user, resp)) {
              xLogger.warn("Switching user {0} to new host...", userId);
              return;
            }
            appVersion = user.getAppVersion();
            domainId = user.getDomainId();
            setUserAgent(user, userAgentStr);
            user.setIPAddress(ipAddress);
            user.setAppVersion(version);
            // Set the last reconnected time for user (same as last login time)
            user.setLastMobileAccessed(user.getLastLogin());
            // Update with user's locale
            locale = user.getLocale().toString();
            //to store the history of user login's
            as.updateUserLoginHistory(userId, SourceConstants.MOBILE, userAgentStr,
                ipAddress, new Date(), version);
            // Get the resource bundle according to the user's login
            try {
              backendMessages = Resources.get().getBundle("BackendMessages", user.getLocale());
            } catch (MissingResourceException e) {
              xLogger
                  .severe("Unable to get resource bundles BackendMessages for locale {0}", locale);
            }
            if (startDateStr != null && !startDateStr.isEmpty()) {
              // Convert the start string to a Date format.
              try {
                start =
                    LocalDateUtil
                        .parseCustom(startDateStr, Constants.DATETIME_FORMAT, user.getTimezone());
              } catch (ParseException pe) {
                status = false;
                backendMessages = Resources.get().getBundle("BackendMessages", user.getLocale());
                message = backendMessages.getString("error.invalidstartdate");
                xLogger.severe("Exception while parsing start date. Exception: {0}, Message: {1}",
                    pe.getClass().getName(), pe.getMessage());
              }
            }
          } else {
            // Authentication failed, use the locale from the request, if provided
            status = false;
            try {
              backendMessages = Resources.get().getBundle("BackendMessages", new Locale(locale));
              message = backendMessages.getString("error.invalidusername");
            } catch (MissingResourceException e) {
              xLogger
                  .severe("Unable to get resource bundles BackendMessages for locale {0}", locale);
            }
            xLogger.warn("Unable to authenticate user {0}: {1}", userId, message);
          }
        } else {
          status = false;
          message = backendMessages.getString("error.invalidusername");
        }
      } catch (ObjectNotFoundException e) {
        xLogger.warn("No user found with ID: {0}", userId);
        status = false;
        message = backendMessages.getString("error.invalidusername");
      } catch (ServiceException e) {
        xLogger.severe("Service Exception during login: {0}", e.getMessage());
        status = false;
        message = backendMessages.getString("error.systemerror");
      }

      //Generate authentication token and update the UserTokens table with userId, Token and Expires
      IUserToken token;
      try {
        aus = Services.getService(AuthenticationServiceImpl.class);
        token = aus.generateUserToken(userId);
        if (token != null) {
          resp.setHeader(Constants.TOKEN, token.getRawToken());
          resp.setHeader(Constants.EXPIRES, String.valueOf(token.getExpires().getTime()));
        }
      } catch (ObjectNotFoundException e) {
        xLogger.warn("No user found with ID: {0}", userId);
        status = false;
        message = backendMessages.getString("error.invalidusername");
      } catch (ServiceException e) {
        xLogger.severe("Service Exception during login: {0}", userId, e);
        status = false;
        message = backendMessages.getString("error.systemerror");
      }

      // FOR BACKWARD COMPATIBILITY: check whether stock has to be sent back as integer (it is float beyond mobile app. version 1.2.0)
      forceIntegerForStock = RESTUtil.forceIntegerForStock(appVersion);
      // Persist the user account object, to store the changes, esp. last reconnected time
      // NOTE: we are doing this post sending response, so that respone time is not impacted
      if (user != null) {
        try {
          as.updateMobileLoginFields(user);
        } catch (Exception e) {
          xLogger.severe("{0} when trying to store user account object {1} in domain {2}: {3}",
              e.getClass().getName(), userId, domainId, e.getMessage());
        }
      }
    }
    try {
      // Get the domain configuration from the data store
      DomainConfig dc = null;
      if (domainId != null) {
        dc = DomainConfig.getInstance(domainId);
      }
      // Assemble the JSON return object
      //AuthenticateOutput jsonOutput = RESTUtil.getJsonOutputAuthenticate(status, user, message, dc, locale, minResponseCode, backendMessages, onlyAuthenticate, forceIntegerForStock, start, pageParams);
      //  jsonString = jsonOutput.toJSONString();
      jsonString =
          RESTUtil.getJsonOutputAuthenticate(status, user, message, dc, locale, minResponseCode,
              backendMessages, onlyAuthenticate, forceIntegerForStock, start, pageParams);
    } catch (Exception e2) {
      xLogger.warn("Protocol exception during login: {0}", e2);
      if (status) { // that is, login successful, but data formatting exception
        status = false;
        message = backendMessages.getString("error.nomaterials");
        try {
          // jsonString = RESTUtil.getJsonOutputAuthenticate(status, null, message, null, locale, minResponseCode, backendMessages, onlyAuthenticate, forceIntegerForStock, start, null).toJSONString();
          jsonString =
              RESTUtil
                  .getJsonOutputAuthenticate(status, null, message, null, locale, minResponseCode,
                      backendMessages, onlyAuthenticate, forceIntegerForStock, start, null);
        } catch (Exception e) {
          xLogger.severe("Protocol exception after data formatting error (during login): {0}", e);
          resp.setStatus(500);
          return;
        }
      }
    }
    if (jsonString != null) {
      sendJsonResponse(resp, HttpServletResponse.SC_OK, jsonString);
    }
  }

  public void generateNewPassword(HttpServletRequest req, HttpServletResponse resp,
                                  ResourceBundle backendMessages) {
    xLogger.fine("Entering forgot password");
    String message = null;
    boolean status = false;
    String userId = req.getParameter(RestConstantsZ.USER_ID);
    String sendType = req.getParameter(RestConstantsZ.TYPE);
    String otp = req.getParameter(OTP);
    if (userId != null) {
      try {
        AuthenticationService as = Services.getService(AuthenticationServiceImpl.class);
        String successMsg;
        String au = req.getParameter("au");
        if (TYPE_EMAIL.equalsIgnoreCase(sendType)) {
          successMsg = as.resetPassword(userId, 1, otp, "m", au);
        } else {
          successMsg = as.resetPassword(userId, 0, otp, "m", au);
        }
        if (StringUtils.isNotEmpty(successMsg)) {
          status = true;
        }
      } catch (ServiceException | IOException e) {
        xLogger
            .severe("Error while processing forgot password request: {0}, user: {1} and type: {2}",
                e.getMessage(), userId, sendType, e);
        message = backendMessages.getString("error.systemerror");
      } catch (ObjectNotFoundException e) {
        xLogger.warn("Error while processing forgot password request: No user found with ID: {0}",
            userId);
        message = backendMessages.getString("user.none") + ": " + userId;
      } catch (MessageHandlingException e) {
        xLogger.warn("Error while processing forgot password request: {0}, user: {1} and type: {2}",
            e.getMessage(), userId, sendType, e);
        message = backendMessages.getString("error.systemerror");
      } catch (InputMismatchException e) {
        xLogger.warn("Error while processing forgot password request: {0}, user: {1} and type: {2}",
            e.getMessage(), userId, sendType, e);
        message = backendMessages.getString("password.otp.invalid");
      }
    } else {
      xLogger.warn("Invalid forgot password request user: {0} and type: {1}", userId, sendType);
      message = backendMessages.getString("error.invalidusername");
    }
    try {
      sendJsonResponse(resp, HttpServletResponse.SC_OK,
          new JsonOutput(DEFAULT_VERSION, status, status ? null : message).toJSONString());
    } catch (IOException e) {
      resp.setStatus(500);
    }
    xLogger.fine("Exiting forgot password");
  }

  public void generateOtpLink(HttpServletRequest request, HttpServletResponse response,
                              ResourceBundle backendMessages) {
    xLogger.fine("Entering generate OTP");
    String message = null;
    boolean status = false;
    String userId = request.getParameter(RestConstantsZ.USER_ID);
    String sendType = request.getParameter(RestConstantsZ.TYPE);
    if (userId != null) {
      try {
        AuthenticationService as = Services.getService(AuthenticationServiceImpl.class);
        Long domainId = SessionMgr.getCurrentDomain(request.getSession(), userId);
        String successMsg = null;
        String host = request.getHeader("host");
        if (TYPE_EMAIL.equalsIgnoreCase(sendType)) {
          successMsg = as.generateOTP(userId, 1, "m", domainId, host);
        } else if (TYPE_MOBILE.equalsIgnoreCase(sendType)) {
          successMsg = as.generateOTP(userId, 0, "m", domainId, host);
        }
        if (StringUtils.isNotEmpty(successMsg)) {
          status = true;
        }
      } catch (ServiceException | IOException e) {
        xLogger
            .severe("Error while processing forgot password request: {0}, user: {1} and type: {2}",
                e.getMessage(), userId, sendType, e);
        message = backendMessages.getString("error.systemerror");
      } catch (ObjectNotFoundException e) {
        xLogger.warn("Error while processing forgot password request: No user found with ID: {0}",
            userId);
        message = backendMessages.getString("user.none") + ": " + userId;
      } catch (MessageHandlingException e) {
        xLogger.warn("Error while processing forgot password request: {0}, user: {1} and type: {2}",
            e.getMessage(), userId, sendType, e);
        message = backendMessages.getString("error.systemerror");
      }
    } else {
      xLogger.warn("Invalid forgot password request user: {0} and type: {1}", userId, sendType);
      message = backendMessages.getString("error.invalidusername");
    }
    try {
      sendJsonResponse(response, HttpServletResponse.SC_OK,
          new JsonOutput(DEFAULT_VERSION, status, status ? null : message).toJSONString());
    } catch (IOException e) {
      response.setStatus(500);
    }
    xLogger.fine("Exiting generate OTP");
  }

  public void validateForgotPassword(HttpServletRequest req, HttpServletResponse resp,
                                     ResourceBundle backendMessages) {
    String otp = req.getParameter(OTP);
    String au = req.getParameter(AU);
    String type = req.getParameter(RestConstantsZ.TYPE);
    String message = null;
    if (StringUtils.isNotEmpty(type) && (TYPE_MOBILE.equalsIgnoreCase(type) || TYPE_EMAIL
        .equalsIgnoreCase(type))) {
      if (StringUtils.isNotEmpty(au)) {
        if (AU_VAL.equalsIgnoreCase(au)) {
          if (StringUtils.isNotEmpty(otp)) {
            if (TYPE_MOBILE.equalsIgnoreCase(type)) {
              generateNewPassword(req, resp, backendMessages);
            } else {
              message = "Invalid reqeust";
            }
          } else {
            generateOtpLink(req, resp, backendMessages);
          }
        } else {
          message = "Invalid AU parameter au: " + au;
        }
      } else {
        generateNewPassword(req, resp, backendMessages);
      }
    } else {
      message = "Invalid type parameter ty: " + type;
    }

    if (StringUtils.isNotEmpty(message)) {
      try {
        sendJsonResponse(resp, HttpServletResponse.SC_OK,
            new JsonOutput(DEFAULT_VERSION, false, message).toJSONString());
      } catch (IOException e) {
        resp.setStatus(500);
      }
    }
  }
}

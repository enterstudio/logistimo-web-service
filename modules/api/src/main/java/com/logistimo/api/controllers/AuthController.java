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

import com.logistimo.AppFactory;
import com.logistimo.api.models.AuthLoginModel;
import com.logistimo.api.models.AuthModel;
import com.logistimo.api.models.ChangePasswordModel;
import com.logistimo.api.models.PasswordModel;
import com.logistimo.auth.SecurityConstants;
import com.logistimo.auth.SecurityMgr;
import com.logistimo.auth.service.AuthenticationService;
import com.logistimo.auth.service.impl.AuthenticationServiceImpl;
import com.logistimo.auth.utils.SecurityUtils;
import com.logistimo.auth.utils.SessionMgr;
import com.logistimo.communications.MessageHandlingException;
import com.logistimo.communications.service.MessageService;
import com.logistimo.constants.SourceConstants;
import com.logistimo.dao.JDOUtils;
import com.logistimo.events.entity.IEvent;
import com.logistimo.events.processor.EventPublisher;
import com.logistimo.exception.InvalidDataException;
import com.logistimo.logger.XLog;
import com.logistimo.security.BadCredentialsException;
import com.logistimo.security.SecureUserDetails;
import com.logistimo.security.UserDisabledException;
import com.logistimo.services.ObjectNotFoundException;
import com.logistimo.services.Resources;
import com.logistimo.services.ServiceException;
import com.logistimo.services.Services;
import com.logistimo.services.cache.MemcacheService;
import com.logistimo.services.impl.PMF;
import com.logistimo.services.utils.ConfigUtil;
import com.logistimo.users.entity.IUserAccount;
import com.logistimo.users.entity.UserAccount;
import com.logistimo.users.service.UsersService;
import com.logistimo.users.service.impl.UsersServiceImpl;
import com.logistimo.utils.MsgUtil;

import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.InputMismatchException;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

import javax.jdo.PersistenceManager;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * Created by Mohan Raja on 03/04/15
 */
@Controller
@RequestMapping("/auth")
public class AuthController {
  private static final XLog xLogger = XLog.getLog(AuthController.class);
  private static final Integer INVALID_USERNAME = 1;
  private static final Integer ALREADY_LOGGED_IN = 2;
  private static final Integer ACCOUNT_DISABLED = 3;
  private static final Integer ACCESS_DENIED = 4;
  private static final Integer SYSTEM_ERROR = 5;
  private static final Integer USER_NOTFOUND = 6;
  private static final Integer EMAIL_UNAVAILABLE = 7;
  private static final Integer OTP_EXPIRED = 8;
  private static final Integer PASSWORD_MISMATCH = 9;
  private static boolean isGAE = ConfigUtil.getBoolean("gae.deployment", true);

  private static void updateUserDetails(SecureUserDetails userDetails, String ipAddress,
                                        String userAgent) {
    PersistenceManager pm = null;
    try {
      pm = PMF.get().getPersistenceManager();
      IUserAccount u = JDOUtils.getObjectById(IUserAccount.class, userDetails.getUsername(), pm);
      u.setLastLogin(new Date());
      u.setIPAddress(ipAddress);
      u.setPreviousUserAgent(u.getUserAgent());
      u.setUserAgent(userAgent);
      u.setAppVersion("LogiWeb");
      Map<String, Object> params = new HashMap<>(1);
      params.put("ipaddress", u.getIPAddress());
      EventPublisher.generate(u.getDomainId(), IEvent.IP_ADDRESS_MATCHED, params,
          UserAccount.class.getName(), u.getKeyString(),
          null);
    } catch (Exception ignored) {
      // do nothing
    } finally {
      if (pm != null) {
        pm.close();
      }
    }
  }

  @RequestMapping(value = "/login", method = RequestMethod.POST)
  public
  @ResponseBody
  AuthModel login(@RequestBody AuthLoginModel authLoginModel, HttpServletRequest request,
                  HttpServletResponse response) {
    try {
      HttpSession session = request.getSession();
      if (SecurityMgr.isLoggedInAsAnotherUser(session, authLoginModel.userId)) {
        return constructAuthModel(ALREADY_LOGGED_IN, authLoginModel.language);
      }
      SecureUserDetails
          userDetails =
          SecurityMgr.authenticate(authLoginModel.userId, authLoginModel.password);
      //Recreates and initialize the session after successful login.
      SessionMgr.recreateSession(request, response, userDetails);
      Long domainId = SecurityUtils.getReqCookieDomain(request);
      UsersService as = Services.getService(UsersServiceImpl.class);
      if (domainId != null) {
        if (as.hasAccessToDomain(userDetails.getUsername(), domainId)) {
          SessionMgr.setCurrentDomain(request.getSession(), domainId);
        }
      }
      String ipAddress = isGAE ? request.getRemoteAddr() : request.getHeader("X-REAL-IP");
      updateUserDetails(userDetails, ipAddress, request.getHeader("User-Agent"));
      as.updateUserLoginHistory(userDetails.getUsername(), SourceConstants.WEB,
          request.getHeader("User-Agent"), request.getHeader("X-REAL-IP"), new Date(), "LogiWeb");

      xLogger.info("ip: {0}, headers: {1}", ipAddress, request.getHeader("X-Forwarded-For"));
      if (SecurityConstants.ROLE_KIOSKOWNER.equals(userDetails.getRole())) {
        SessionMgr.cleanupSession(request.getSession());
        return constructAuthModel(ACCESS_DENIED, authLoginModel.language);
      }
      return constructAuthModel(0, authLoginModel.language);
    } catch (BadCredentialsException e) {
      xLogger.warn("Invalid user name or password: {0}", authLoginModel.userId, e);
      return constructAuthModel(INVALID_USERNAME, authLoginModel.language);
    } catch (UserDisabledException e) {
      xLogger.warn("User disabled: {0}", authLoginModel.userId, e);
      return constructAuthModel(ACCOUNT_DISABLED, authLoginModel.language);
    } catch (ObjectNotFoundException e) {
      xLogger.warn("User not found: {0}", authLoginModel.userId, e);
      return constructAuthModel(INVALID_USERNAME, authLoginModel.language);
    } catch (Exception e) {
      xLogger.severe("{0} when authenticating user {1}: {2}", e.getClass().getName(),
          authLoginModel.userId, e.getMessage(), e);
      return constructAuthModel(ACCESS_DENIED, authLoginModel.language);
    }
  }

  private AuthModel constructAuthModel(int status, String language) {
    AuthModel model = new AuthModel();
    Locale locale;
    if (language != null) {
      locale = new Locale(language);
    } else {
      locale = new Locale("en");
    }
    try {
      ResourceBundle backendMessages = Resources.get().getBundle("BackendMessages", locale);
      model.isError = true;
      model.ec = status;
      if (status == INVALID_USERNAME) {
        model.errorMsg = backendMessages.getString("user.invalidup");
      } else if (status == ALREADY_LOGGED_IN) {
        model.errorMsg = backendMessages.getString("user.already.logged");
      } else if (status == ACCOUNT_DISABLED) {
        model.errorMsg = backendMessages.getString("your.account.disabled");
      } else if (status == ACCESS_DENIED) {
        model.errorMsg = backendMessages.getString("user.access.denied");
      } else if (status == SYSTEM_ERROR) {
        model.errorMsg = backendMessages.getString("system.error");
      } else if (status == USER_NOTFOUND) {
        model.errorMsg = backendMessages.getString("user.none");
      } else if (status == EMAIL_UNAVAILABLE) {
        model.errorMsg = backendMessages.getString("password.email.unavailable");
      } else if (status == OTP_EXPIRED) {
        model.errorMsg = backendMessages.getString("password.otp.invalid");
      } else if (status == PASSWORD_MISMATCH) {
        model.errorMsg = backendMessages.getString("password.confirm.mismatch");
      } else {
        model.isError = false;
      }
    } catch (Exception ignored) {
      // do nothing
    }
    return model;
  }

  @RequestMapping(value = "/logout", method = RequestMethod.GET)
  public
  @ResponseBody
  AuthModel logout(HttpServletRequest request) {
    HttpSession session = request.getSession(false);
    if (session != null) {
      SessionMgr.cleanupSession(session);
    }
    return constructAuthModel(0, null);
  }

  @RequestMapping(value = "/resetpassword/{token:.*}", method = RequestMethod.GET)
  public
  @ResponseBody
  AuthModel resetPassword(@PathVariable String token, @RequestParam String src,
                          HttpServletRequest request, HttpServletResponse response) {

    try {
      String successMsg = resetAndRedirect(token, src, request, response);
      return new AuthModel(false, successMsg);
    } catch (ServiceException | MessageHandlingException | IOException e) {
      xLogger.warn("Error updating password ", e);
      return constructAuthModel(SYSTEM_ERROR, null);
    } catch (ObjectNotFoundException e) {
      xLogger.warn("Error updating password for {0}", e);
      return constructAuthModel(USER_NOTFOUND, null);
    } catch (Exception e) {
      xLogger.warn("Error updating password ", e);
      return constructAuthModel(SYSTEM_ERROR, null);
    }
  }

  @RequestMapping(value = "/resetpassword", method = RequestMethod.POST)
  public
  @ResponseBody
  AuthModel generatePassword(@RequestBody PasswordModel model, HttpServletRequest request) {
    if (model != null) {
      try {
        AuthenticationService as = Services.getService(AuthenticationServiceImpl.class, null);
        String successMsg = as.resetPassword(model.uid, model.mode, model.otp, "w",
            request.getParameter("au"));
        return new AuthModel(false, successMsg);
      } catch (ServiceException | MessageHandlingException | IOException e) {
        xLogger.warn("Error updating password for " + model.uid, e);
        return constructAuthModel(SYSTEM_ERROR, null);
      } catch (ObjectNotFoundException e) {
        xLogger.warn("Error updating password for {0}", model.uid, e);
        return constructAuthModel(USER_NOTFOUND, null);
      } catch (InvalidDataException e) {
        xLogger.warn("Email Id not available {0}", model.uid, e);
        return constructAuthModel(EMAIL_UNAVAILABLE, null);
      } catch (InputMismatchException e) {
        xLogger.warn("Invalid One-time password {0}", model.uid, e);
        return constructAuthModel(OTP_EXPIRED, null);
      }
    }

    return null;
  }

  @RequestMapping(value = "/generateOtp", method = RequestMethod.POST)
  public
  @ResponseBody
  AuthModel generateOtp(@RequestBody PasswordModel model, HttpServletRequest request) {
    if (model != null) {
      try {
        AuthenticationService as = Services.getService(AuthenticationServiceImpl.class, null);
        UsersService us = Services.getService(UsersServiceImpl.class);
        Long domainId = us.getUserAccount(model.uid).getDomainId();
        //web client is not sending this variable
        if (StringUtils.isEmpty(model.udty)) {
          model.udty = "w";
        }
        String successMsg = as.generateOTP(model.uid, model.mode, model.udty, domainId,
            request.getHeader("host"));
        return new AuthModel(false, successMsg);
      } catch (MessageHandlingException | IOException | ServiceException e) {
        xLogger.severe("Exception: " + e);
        return constructAuthModel(SYSTEM_ERROR, null);
      } catch (ObjectNotFoundException e) {
        xLogger.warn("Error updating password for {0}", model.uid, e);
        return constructAuthModel(USER_NOTFOUND, null);
      } catch (InvalidDataException e) {
        xLogger.severe("Email Id not available {0}", model.uid, e);
        return constructAuthModel(EMAIL_UNAVAILABLE, null);
      }
    }

    return null;
  }

  @RequestMapping(value = "/changePassword", method = RequestMethod.POST)
  public
  @ResponseBody
  AuthModel changePassword(@RequestBody ChangePasswordModel model) {
    if (model != null) {
      try {
        AuthenticationService as = Services.getService(AuthenticationServiceImpl.class, null);
        String successMsg = as.setNewPassword(model.key, model.npd, model.cpd);
        return new AuthModel(false, successMsg);
      } catch (MessageHandlingException | ServiceException | IOException e) {
        xLogger.severe("Exception: " + e);
        return constructAuthModel(SYSTEM_ERROR, null);
      } catch (ObjectNotFoundException e) {
        xLogger.warn("Error updating password for {0}", model.uid, e);
        return constructAuthModel(USER_NOTFOUND, null);
      } catch (InputMismatchException e) {
        xLogger.warn("Mismatch in passwords entered for {0}", model.uid, e);
        return constructAuthModel(PASSWORD_MISMATCH, null);
      } catch (Exception e) {
        xLogger.severe("Exception: " + e);
        return constructAuthModel(SYSTEM_ERROR, null);
      }
    }

    return null;
  }

  public String resetAndRedirect(String token, String src, HttpServletRequest request,
                                 HttpServletResponse response) throws Exception {
    if (StringUtils.isNotEmpty(token)) {
      AuthenticationService as = Services.getService(AuthenticationServiceImpl.class, null);
      String decryptedToken = as.decryptJWT(token);
      String[] tokens = new String[2];
      if (decryptedToken != null) {
        tokens = decryptedToken.split("&&");
      }
      if (tokens.length > 0) {
        String userId = tokens[0];
        Boolean tdiff = timeDiff(tokens[1]);
        if (tdiff) {
          MemcacheService cache = AppFactory.get().getMemcacheService();
          String resetKey = userId + "&&" + tokens[1];
          if (cache != null) {
            if (resetKey.equals(cache.get("RESET_" + userId))) {
              String newPassword;
              String msg;
              String logMsg;
              String sendMode = null;
              String sendType;
              UsersService usersService = Services.getService(UsersServiceImpl.class, null);
              IUserAccount account = usersService.getUserAccount(userId);
              Locale locale = account.getLocale();
              ResourceBundle backendMessages = Resources.get().getBundle("BackendMessages", locale);
              if ("w".equalsIgnoreCase(src)) {
                response.sendRedirect("/v2/password-request.html?key=" + token);
              } else {
                newPassword = as.generatePassword(userId);
                msg = backendMessages.getString("password.reset.success") + ": " + newPassword;
                logMsg = backendMessages.getString("password.reset.success.log");
                sendMode = backendMessages.getString("password.mid");
                sendType = "email";
                usersService.changePassword(userId, null, newPassword);
                MessageService
                    ms =
                    MessageService
                        .getInstance(sendType, account.getCountry(), true, account.getDomainId(),
                            account.getFirstName(), null);
                ms.send(account, msg, MessageService.NORMAL,
                    backendMessages.getString("password.reset.success"), null, logMsg);
                response.sendRedirect("/v2/mobile-pwd-reset-success.html#/");
                cache.delete("RESET_" + userId);
              }
              xLogger.info("AUDITLOG\t{0}\t{1}\tUSER\t " +
                      "FORGOT PASSWORD\t{2}\t{3}", account.getDomainId(), account.getFullName(),
                  userId,
                  account.getFullName());
              return backendMessages.getString("password.forgot.success") + " " + account
                  .getFirstName() + "'s " + sendMode + ". " +
                  MsgUtil.bold(backendMessages.getString("note") + ":") + " " + backendMessages
                  .getString("password.login") + ".";
            } else {
              response.sendRedirect("/v2/password-reset-error.html#/");
            }
          }
        } else {
          response.sendRedirect("/v2/password-reset-error.html#/");
        }

      }
    }
    return null;
  }

  /**
   * Calculate the difference b/w received token's milliseconds and current system time's milliseconds
   */
  private Boolean timeDiff(String tokenTime) {
    if (tokenTime != null) {
      Long tmillis = Long.valueOf(tokenTime);
      Long cmilli = System.currentTimeMillis();
      long diff = cmilli - tmillis;
      if (diff < 86400000) {
        return true;
      }
    }
    return false;
  }


}

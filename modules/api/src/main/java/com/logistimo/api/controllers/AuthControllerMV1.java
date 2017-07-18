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

import com.logistimo.api.auth.AuthenticationUtil;
import com.logistimo.api.builders.UserBuilder;
import com.logistimo.api.models.AuthLoginModel;
import com.logistimo.api.models.ChangePasswordModel;
import com.logistimo.api.models.UserDetailModel;
import com.logistimo.api.models.mobile.ValidateOtpModel;
import com.logistimo.auth.service.AuthenticationService;
import com.logistimo.auth.service.impl.AuthenticationServiceImpl;
import com.logistimo.constants.Constants;
import com.logistimo.constants.SourceConstants;
import com.logistimo.exception.BadRequestException;
import com.logistimo.logger.XLog;
import com.logistimo.security.BadCredentialsException;
import com.logistimo.security.UserDisabledException;
import com.logistimo.services.ObjectNotFoundException;
import com.logistimo.services.ServiceException;
import com.logistimo.services.Services;
import com.logistimo.users.entity.IUserAccount;
import com.logistimo.users.entity.IUserToken;
import com.logistimo.users.service.UsersService;
import com.logistimo.users.service.impl.UsersServiceImpl;

import org.apache.commons.lang.StringUtils;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


@Controller
@RequestMapping("/mauth")
public class AuthControllerMV1 {

  private static final XLog xLogger = XLog.getLog(AuthControllerMV1.class);
  private static final String USER_AGENT = "User-Agent";
  private static final String DEVICE_DETAILS = "Device-Details";
  private static final String DEVICE_PROFILE = "Device-Profile";
  private static final UserBuilder ubuilder = new UserBuilder();

  /**
   * This method is used for user's login
   *
   * @param loginModel userid and password
   * @param req        http request
   * @return UserDetail object on successful login
   */
  @RequestMapping(value = "/login", method = RequestMethod.POST)
  public
  @ResponseBody
  UserDetailModel login(@RequestBody AuthLoginModel loginModel,
                        HttpServletRequest req, HttpServletResponse res)
      throws ServiceException, ObjectNotFoundException, BadCredentialsException,
      UserDisabledException {
    IUserAccount user;
    UsersService as;
    Map<String, String> headers = new HashMap<>();
    String authToken = req.getHeader(Constants.TOKEN);
    String initiator = req.getHeader(Constants.ACCESS_INITIATOR);
    Integer actionInitiator = StringUtils.isEmpty(initiator) ? -1 : Integer.parseInt(initiator);
    headers.put(Constants.REQ_ID, req.getHeader(Constants.REQ_ID));
    if (authToken != null) {
      user = AuthenticationUtil.authenticateToken(authToken, actionInitiator);
    } else {
      if (loginModel.userId == null && loginModel.password == null) {
        throw new BadRequestException("Login credentials are empty");
      }
      String userAgentStr = req.getHeader(USER_AGENT);
      String deviceDetails = req.getHeader(DEVICE_DETAILS);
      String deviceProfile = req.getHeader(DEVICE_PROFILE);
      String ipaddr = req.getRemoteAddr();
      if (userAgentStr == null) {
        userAgentStr = "";
      }
      if (deviceDetails != null && !deviceDetails.isEmpty()) {
        userAgentStr += " [Device-details: " + deviceDetails + "]";
      }
      if (deviceProfile != null && !deviceProfile.isEmpty()) {
        userAgentStr += " [Device-Profile: " + deviceProfile + "]";
      }
      String userid = loginModel.userId;
      String password = loginModel.password;

      as = Services.getService(UsersServiceImpl.class);
      user = as.authenticateUser(userid, password, SourceConstants.MOBILE);
      if (user == null) {
        throw new BadCredentialsException("Invalid user name or password");
      }
      if (!user.isEnabled()) {
        throw new UserDisabledException("You account is disabled");
      }
      generateUserToken(headers, userid);

      //to store the history of user login's
      as.updateUserLoginHistory(userid, SourceConstants.MOBILE, userAgentStr,
          ipaddr, new Date(), null);

    }
    //setting response headers
    setResponseHeaders(res, headers);
    return ubuilder.buildMobileAuthResponseModel(user);
  }

  private void setResponseHeaders(HttpServletResponse response, Map<String, String> headers) {
    Set<Map.Entry<String, String>> entrySet = headers.entrySet();
    for (Map.Entry<String, String> entry : entrySet) {
      response.addHeader(entry.getKey(), entry.getValue());
    }
  }

  private void generateUserToken(Map<String, String> headers,
                                 String userid) throws ObjectNotFoundException, ServiceException {
    AuthenticationService aus;
    IUserToken token;
    aus = Services.getService(AuthenticationServiceImpl.class);
    token = aus.generateUserToken(userid);
    if (token != null) {
      headers.put(Constants.TOKEN, token.getRawToken());
      headers.put(Constants.EXPIRES, String.valueOf(token.getExpires().getTime()));
    }
  }

  /**
   * This method will validate user's otp
   *
   * @param otpModel consists of user id, otp
   */
  @RequestMapping(value = "/validate-otp", method = RequestMethod.POST)
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void validateOtp(@RequestBody ValidateOtpModel otpModel) {
    AuthenticationService as;
    as = Services.getService(AuthenticationServiceImpl.class);
    as.validateOtpMMode(otpModel.uid, otpModel.otp);
  }

  /**
   * This method will validate access token for an user.
   *
   * @return userId
   */
  @RequestMapping(value = "/validate-token", method = RequestMethod.POST)
  public
  @ResponseBody
  String validateToken(@RequestBody String token)
      throws ServiceException, ObjectNotFoundException {
    String userId;
    AuthenticationService aus;
    aus = Services.getService(AuthenticationServiceImpl.class);
    userId = aus.authenticateToken(token, -1);
    if (userId == null) {
      throw new ObjectNotFoundException("Invalid token");
    }
    return userId;
  }

  /**
   * This will reset user's password
   *
   * @param pwdModel consists of user id ,otp and new password
   */
  @RequestMapping(value = "/change-password", method = RequestMethod.POST)
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void resetPassword(@RequestBody ChangePasswordModel pwdModel) throws ServiceException {
    UsersService us;
    AuthenticationService as;
    as = Services.getService(AuthenticationServiceImpl.class);
    as.validateOtpMMode(pwdModel.uid, pwdModel.otp);
    us = Services.getService(UsersServiceImpl.class);
    us.changePassword(pwdModel.uid, null, pwdModel.npd);
  }
}

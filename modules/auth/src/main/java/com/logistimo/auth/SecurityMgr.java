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

/**
 *
 */
package com.logistimo.auth;

import com.logistimo.AppFactory;
import com.logistimo.auth.utils.SecurityUtils;
import com.logistimo.constants.Constants;
import com.logistimo.constants.SourceConstants;
import com.logistimo.exception.UnauthorizedException;
import com.logistimo.logger.XLog;
import com.logistimo.security.BadCredentialsException;
import com.logistimo.security.SecureUserDetails;
import com.logistimo.security.UserDisabledException;
import com.logistimo.services.ObjectNotFoundException;
import com.logistimo.services.Services;
import com.logistimo.users.entity.IUserAccount;
import com.logistimo.users.service.UsersService;
import com.logistimo.users.service.impl.UsersServiceImpl;

import org.apache.commons.codec.binary.Base64;

import java.io.UnsupportedEncodingException;
import java.util.Arrays;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;


/**
 * Provides methods to authorize certain actions for a given user
 *
 * @author arun
 */
public class SecurityMgr {

  private SecurityMgr() {
  }

  // Logger
  private static final XLog xLogger = XLog.getLog(SecurityMgr.class);
  // Operation identifiers (define more operations here)
  private static final String OP_CONFIGURATION = "configuration";
  private static final String OP_MANAGE = "manage";
  private static final String OP_PUSHAPP = "pshapp";
  // HTTP header
  private static final String HEADER_AUTHORIZATION = "Authorization";

  // Is logged in as another user?
  public static boolean isLoggedInAsAnotherUser(HttpSession session, String userId) {
    SecureUserDetails userDetails = SecurityMgr.getSessionDetails(session);
    return userDetails != null && !userDetails.getUsername().equals(userId);
  }

  // Check access for a given operation
  public static boolean hasAccess(String operation, String role) {
    boolean hasAccess = false;
    if (OP_MANAGE.equals(operation)) {
      if (SecurityConstants.ROLE_SUPERUSER.equals(role)) {
        hasAccess = true;
      }
    } else if (OP_CONFIGURATION.equals(operation)) {
      if (SecurityConstants.ROLE_DOMAINOWNER.equals(role) || SecurityConstants.ROLE_SUPERUSER.equals(role)) {
        hasAccess = true;
      }
    } else if (OP_PUSHAPP.equals(operation) && (SecurityConstants.ROLE_SUPERUSER.equals(role)
        || SecurityConstants.ROLE_DOMAINOWNER.equals(role))) {
      hasAccess = true;

    }

    return hasAccess;
  }

  /**
   * Gets the SecureUserDetails from the session.
   *
   * @deprecated in 2.5.0 , see SecurityUtils.getUserDetails.
   */
  @Deprecated
  public static SecureUserDetails getUserDetails(HttpSession session) {
    return SecurityUtils.getUserDetails();
  }

  // Get the details of an authenticated user
  public static SecureUserDetails getSessionDetails(HttpSession session) {
    return session != null ? (SecureUserDetails) session.getAttribute(Constants.PARAM_USER) : null;
  }

  // Authenticate user
  public static SecureUserDetails authenticate(String userId, String password)
      throws Exception {
    UsersService as = Services.getService(UsersServiceImpl.class);
    // Authenticate user
    IUserAccount user = as.authenticateUser(userId, password, SourceConstants.WEB);
    if (user == null) {
      throw new BadCredentialsException("Invalid user name or password");
    }
    if (!user.isEnabled()) {
      throw new UserDisabledException("You account is disabled");
    }
    return getSecureUserDetails(user);
  }

  public static void setSessionDetails(String userId) throws ObjectNotFoundException {
    UsersService as = Services.getService(UsersServiceImpl.class);
    IUserAccount user = as.getUserAccount(userId);
    if (!user.isEnabled()) {
      throw new UnauthorizedException("You account is disabled");
    }
    SecureUserDetails secureUserDetails = getSecureUserDetails(user);
    SecurityUtils.setUserDetails(secureUserDetails);
  }


  // Get SecureUserDetails from UserAccount
  private static SecureUserDetails getSecureUserDetails(IUserAccount user) {
    SecureUserDetails userDetails = new SecureUserDetails();
    userDetails.setUsername(user.getUserId());
    userDetails.setEnabled(user.isEnabled());
    userDetails.setRole(user.getRole());
    userDetails.setDomainId(user.getDomainId());
    userDetails.setLocale(user.getLocale());
    userDetails.setTimezone(user.getTimezone());
    return userDetails;
  }

  // Check if dev. server
  public static boolean isDevServer() {
    return System.getProperty("mode", "dev") == "dev";
  }

  // Get the application name - e.g. logistimo-web, logistimo-dev
  public static String getApplicationName() {

    return AppFactory.get().getStorageUtil().getApplicationName();
  }

  // Get user credentials from a HTTP request with basic authentication [userId,password] (NOTE: Only user Id and password are returned)
  public static Credentials getUserCredentials(HttpServletRequest req) {
    String authorizationStr = req.getHeader(HEADER_AUTHORIZATION);
    if (authorizationStr == null || authorizationStr.isEmpty()) {
      return null;
    }
    String[] authTokens = authorizationStr.split(" ");
    if (authTokens.length != 2) {
      xLogger.warn(
          "Invalid authentication tokens (!=2) when doing Basic authentication using string: {0}",
          authorizationStr);
      return null;
    }
    if ("Basic".equals(authTokens[0])) {
      try {
        String credentialsStr = new String(Base64.decodeBase64(authTokens[1]), "ISO-8859-1");
        String[] creds = credentialsStr.split(":");
        if (creds.length != 2) {
          xLogger.warn("Invalid credentials (!=2) for user:password: {0}", Arrays.toString(creds));
          return null;
        }
        return new Credentials(creds[0], creds[1]);
      } catch (UnsupportedEncodingException e) {
        xLogger.warn("Unsupported encoding: {0}", e.getMessage());
      }
    } else {
      xLogger.warn("Not Basic authentication. Instead: {0}", authTokens[0]);
    }
    return null;
  }

  public static class Credentials {
    public final String userId;
    public final String password;

    public Credentials(String userId, String password) {
      this.userId = userId;
      this.password = password;
    }
  }
}

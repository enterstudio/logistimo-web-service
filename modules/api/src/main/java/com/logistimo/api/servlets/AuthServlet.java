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

import com.logistimo.auth.SecurityMgr;
import com.logistimo.auth.utils.SessionMgr;
import com.logistimo.dao.JDOUtils;
import com.logistimo.events.entity.IEvent;
import com.logistimo.events.processor.EventPublisher;
import com.logistimo.logger.XLog;
import com.logistimo.security.BadCredentialsException;
import com.logistimo.security.SecureUserDetails;
import com.logistimo.security.UserDisabledException;
import com.logistimo.services.ObjectNotFoundException;
import com.logistimo.services.impl.PMF;
import com.logistimo.services.utils.ConfigUtil;
import com.logistimo.users.entity.IUserAccount;
import com.logistimo.users.entity.UserAccount;

import java.io.IOException;
import java.net.URLDecoder;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;

import javax.jdo.PersistenceManager;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * Authentication servlet
 *
 * @author arun
 */

@SuppressWarnings("serial")
public class AuthServlet extends JsonRestServlet {

  // Added a logger to help debug this servlet's behavior (arun, 1/11/09)
  private static final XLog xLogger = XLog.getLog(AuthServlet.class);

  // Non-rest action/parameter constants
  private static final String ACTION_LOGIN = "li";
  private static final String ACTION_LOGOUT = "lo";

  private static final String LOGIN_URL = "/enc/login.jsp";
  private static final String LOGOUT_URL = "/enc/login.jsp?status=0";
  private static boolean isGAE = ConfigUtil.getBoolean("gae.deployment", true);

  // Update user details, such as last login time
  private static void updateUserDetails(SecureUserDetails userDetails, String ipAddress,
                                        String userAgent) {
    xLogger.fine("Entered updateUserDetails");
    PersistenceManager pm = PMF.get().getPersistenceManager();
    try {
      // More efficient to get/set using PM, instead of AccountsService
      IUserAccount u = JDOUtils.getObjectById(IUserAccount.class, userDetails.getUsername(), pm);
      u.setLastLogin(new Date());
      u.setIPAddress(ipAddress);
      u.setPreviousUserAgent(u.getUserAgent());
      u.setUserAgent(userAgent);
      u.setAppVersion("LogiWeb");
      // Generate IP Address matched event, if required
      Map<String, Object> params = new HashMap<>(1);
      params.put("ipaddress", u.getIPAddress());
      EventPublisher.generate(u.getDomainId(), IEvent.IP_ADDRESS_MATCHED, params,
          UserAccount.class.getName(), u.getKeyString(),
          null);
    } catch (Exception e) {
      xLogger.warn("Unable to update user's last login time for user {0}: {1}",
          userDetails.getUsername(), e.getMessage());
    } finally {
      pm.close();
    }
    xLogger.fine("Exitig updateUserDetails");
  }

  public void processGet(HttpServletRequest req, HttpServletResponse resp,
                         ResourceBundle backendMessages, ResourceBundle messages)
      throws IOException {
    String action = req.getParameter("action");
    if (ACTION_LOGOUT.equals(action)) {
      doLogout(req, resp);
    } else {
      xLogger.severe("Invalid action: {0}", action);
      writeToScreen(req, resp, "Invalid action: " + action);
    }
  }

  public void processPost(HttpServletRequest req, HttpServletResponse resp,
                          ResourceBundle backendMessages, ResourceBundle messages)
      throws IOException {
    String action = req.getParameter("action");
    if (ACTION_LOGIN.equals(action)) {
      doLogin(req, resp);
    } else {
      xLogger.severe("Invalid action: {0}", action);
      writeToScreen(req, resp, "Invalid action: " + action);
    }
  }

  // Login
  private void doLogin(HttpServletRequest req, HttpServletResponse resp) throws IOException {
    xLogger.fine("Entered doLogin");
    String userId = req.getParameter("username");
    String password = req.getParameter("password");
    if (userId == null || userId.isEmpty() || password == null || password.isEmpty()) {
      resp.sendRedirect("/login.jsp?status=1");
      return;
    }
    // Authenticate
    try {
      HttpSession session = req.getSession();
      // Check if a user is already in session (to prevent multiple logins from the same browser)
      if (SecurityMgr
          .isLoggedInAsAnotherUser(session, userId)) { // logged in as some other user
        session = null;
        resp.sendRedirect(LOGIN_URL + "?status=2"); // logged in another user
        return;
      }
      // Authenticate this user
      SecureUserDetails userDetails = SecurityMgr.authenticate(userId, password);
      // Initialize session
      SessionMgr.recreateSession(req, resp, userDetails);
      // Get IP address
      String ipAddress = isGAE ? req.getRemoteAddr() : req.getHeader("X-REAL-IP");
      // Update the user's last login time
      updateUserDetails(userDetails, ipAddress, req.getHeader("User-Agent"));
      xLogger.info("ip: {0}, headers: {1}", ipAddress, req.getHeader("X-Forwarded-For"));
      // Get the re-direction URL - either referral URL or home
      String redirectUrl = req.getParameter("rurl");
      if (redirectUrl == null || redirectUrl.isEmpty()) {
        redirectUrl = "/s/index.jsp";
      } else {
        // Check http:// prefix, if any - this prevents redirection to another domain, if someone played a mischief
        if (redirectUrl.startsWith("http://") || redirectUrl.startsWith("https://")) {
          redirectUrl = "/s/index.jsp";
        }
        redirectUrl = URLDecoder.decode(redirectUrl, "UTF-8");
      }
      if (!redirectUrl.startsWith("/")) {
        redirectUrl = "/" + redirectUrl;
      }
      // Redirect to http instead of https (NO NEED FOR THIS, GIVEN ALL URLS ARE HTTPS since May 15, 2014)
      ///redirectUrl = "https://" + req.getServerName() + ( SecurityManager.isDevServer() ? ":" + req.getServerPort() : "" ) + redirectUrl;
      // Reset local session pointer
      session = null;
      // Redirect now
      resp.sendRedirect(redirectUrl);
    } catch (BadCredentialsException e) {
      xLogger.warn("Invalid user name or password: {0}", userId);
      resp.sendRedirect(LOGIN_URL + "?status=1");
    } catch (UserDisabledException e) {
      xLogger.warn("User disabled: {0}", userId);
      resp.sendRedirect(LOGIN_URL + "?status=3"); // your account is disabled
    } catch (ObjectNotFoundException e) {
      xLogger.warn("User not found: {0}", userId);
      resp.sendRedirect(LOGIN_URL + "?status=1");
    } catch (Exception e) {
      xLogger.severe("{0} when authenticating user {1}: {2}", e.getClass().getName(), userId, e);

      resp.sendRedirect(LOGIN_URL + "?status=4"); // system error; contact admin.
    }
    xLogger.fine("Exiting doLogin");
  }

  // Logout - cleanup session, invalidate it, and redirect to login URL
  public void doLogout(HttpServletRequest req, HttpServletResponse resp) throws IOException {
    xLogger.fine("Entered doLogout");
    SessionMgr.cleanupSession(req.getSession(false));
    resp.sendRedirect(LOGOUT_URL);
    xLogger.fine("Exiting doLogout");
  }

  private void writeToScreen(HttpServletRequest req, HttpServletResponse resp, String message)
      throws IOException {
    writeToScreen(req, resp, message, null, null, "/s/message.jsp");
  }
}

package com.logistimo.api.filters;

import org.apache.commons.lang.StringUtils;

import com.logistimo.api.security.SecurityMgr;
import com.logistimo.services.ServiceException;
import com.logistimo.services.Services;
import com.logistimo.constants.CharacterConstants;
import com.logistimo.constants.Constants;
import com.logistimo.api.util.SessionMgr;
import com.logistimo.logger.XLog;

import com.logistimo.api.util.SecurityUtils;
import com.logistimo.security.SecureUserDetails;
import com.logistimo.services.utils.ConfigUtil;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Created by Mohan Raja on 02/04/15
 */
public class APISecurityFilter implements Filter {

  public static final String ERROR_HEADER_NAME = "e";
  public static final String UPGRADE_REQUIRED_RESPONSE_CODE = "1";
  public static final String DOMAIN_CHANGE_RESPONSE_CODE = "2";
  public static final String ASSET_STATUS_URL = "/s2/api/assetstatus";
  public static final String SMS_API_URL = "/s2/api/sms";
  public static final String APP_STATUS_URL = "/s2/api/app/status";
  private static final String AUTHENTICATE_URL = "/s2/api/auth";
  private static final String APP_VERSION = "web.app.ver";
  private static final XLog xLogger = XLog.getLog(APISecurityFilter.class);
  private String appVersion;
  private boolean appVerAvailable;

  @Override
  public void doFilter(ServletRequest request, ServletResponse response, FilterChain filterChain)
      throws IOException, ServletException {

    HttpServletRequest req = (HttpServletRequest) request;
    HttpServletResponse resp = (HttpServletResponse) response;
    String servletPath = req.getServletPath() + req.getPathInfo();
    xLogger.fine("Servlet path: ", servletPath);
    if (req.getCharacterEncoding() == null) {
      request.setCharacterEncoding(Constants.UTF8);
    }
    // Allow all GAE Internal tasks
    if (StringUtils.isBlank(req.getHeader(Constants.X_APP_ENGINE_TASK_NAME)) && !(
        StringUtils.isNotBlank(servletPath) && (servletPath.startsWith(ASSET_STATUS_URL)
            || servletPath.startsWith(SMS_API_URL)))) {

      String recvdCookie = getAppCookie(req);
      if (appVerAvailable && recvdCookie != null && !appVersion.equals(recvdCookie)) {
        resp.setHeader(ERROR_HEADER_NAME, UPGRADE_REQUIRED_RESPONSE_CODE);
        resp.sendError(HttpServletResponse.SC_CONFLICT, "Upgrade required");
        return;
      }
      if (StringUtils.isNotBlank(servletPath) && !(servletPath.startsWith(APP_STATUS_URL)
          || servletPath.startsWith(AUTHENTICATE_URL))) {
        SecureUserDetails
            userDetails = SecurityMgr
            .getUserDetails(req.getSession());
        if (userDetails == null) {
          resp.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Authentication Required.");
          return;
        }

        String reqDomainId = SecurityUtils.getReqCookieUserDomain(req);
        if (reqDomainId != null && !reqDomainId.equals(
            userDetails.getUsername() + CharacterConstants.COLON + SessionMgr
                .getCurrentDomain(req.getSession(), userDetails.getUsername()))) {
          resp.setHeader(ERROR_HEADER_NAME, DOMAIN_CHANGE_RESPONSE_CODE);
          resp.sendError(HttpServletResponse.SC_CONFLICT, "Invalid session on client");
          return;
        }
      }

    }
    if (filterChain != null) {
      filterChain.doFilter(request, response);
    }
  }

  private String getAppCookie(HttpServletRequest req) {
    Cookie[] cookies = req.getCookies();
    if (cookies != null) {
      for (Cookie cookie : cookies) {
        if (APP_VERSION.equals(cookie.getName())) {
          return cookie.getValue();
        }
      }
    }
    return null;
  }

  @Override
  public void init(FilterConfig filterConfig) throws ServletException {
    appVersion = ConfigUtil.get(APP_VERSION);
    appVerAvailable = StringUtils.isNotBlank(appVersion);
    xLogger.info("Web app version set to : {0}", appVersion);
  }

  @Override
  public void destroy() {

  }
}

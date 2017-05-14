package com.logistimo.api.filters;

import com.logistimo.api.security.SecurityMgr;
import com.logistimo.auth.SecurityConstants;
import com.logistimo.security.SecureUserDetails;
import com.logistimo.services.utils.ConfigUtil;

import org.apache.commons.lang.StringUtils;
import com.logistimo.constants.Constants;
import com.logistimo.api.util.SessionMgr;
import com.logistimo.logger.XLog;

import java.io.IOException;
import java.net.URLEncoder;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author Arun
 */
public class SecurityFilter implements Filter {

  public static final String TASK_URL = "/task/";
  public static final String ACTION = "action";
  private static final XLog xLogger = XLog.getLog(SecurityFilter.class);
  // Authentication request
  private static final String HOME_URL = "/s/index.jsp";
  private static final String HOME_URL_NEW = "/v2/index.html";
  private static final String LOGIN_URL = "/enc/login.jsp";
  private static final String AUTHENTICATE_URL = "/enc/authenticate";
  private static final String ACTION_UPDATESYSCONFIG = "updatesysconfig";
  private static final String TASK_ADMIN_URL = "/task/admin";
  private static boolean isForceNewUI = ConfigUtil.getBoolean("force.newui", false);

  @Override
  public void doFilter(ServletRequest request, ServletResponse response, FilterChain filterChain)
      throws IOException, ServletException {
    xLogger.fine("Entered doFilter");
    HttpServletRequest req = (HttpServletRequest) request;
    HttpServletResponse resp = (HttpServletResponse) response;
    String servletPath = req.getServletPath();
    if (req.getCharacterEncoding() == null) {
      request.setCharacterEncoding(Constants.UTF8);
    }
    // BACKWARD COMPATIBILITY - in case people have already bookmarked these links
    if ("/index.jsp".equals(servletPath) || "/login.jsp".equals(servletPath)) {
      resp.sendRedirect(isForceNewUI ? HOME_URL_NEW : HOME_URL);
      return;
    }
    // END BACKWARD COMPATIBILITY
    if (!LOGIN_URL.equals(servletPath) && !AUTHENTICATE_URL.equals(servletPath) && (
        servletPath.isEmpty() || servletPath.equals("/") || servletPath.startsWith("/s/") || (
            servletPath.startsWith(TASK_URL) && StringUtils
                .isBlank(req.getHeader(Constants.X_APP_ENGINE_TASK_NAME))))) {
      SecureUserDetails
          userDetails = SecurityMgr
          .getUserDetails(req.getSession());
      if (userDetails == null) { // session not authenticated yet; direct to login screen
        if (!(servletPath.startsWith(TASK_ADMIN_URL) && ACTION_UPDATESYSCONFIG
            .equals(request.getParameter(ACTION)))) {
          if (isForceNewUI) {
            resp.sendRedirect(HOME_URL_NEW);  // login please
          } else {
            String redirectPath = LOGIN_URL;
            if (servletPath.startsWith("/s/") || servletPath.startsWith(TASK_URL)) {
              String rUrl = servletPath;
              String queryString = req.getQueryString();
              if (queryString != null && !queryString.isEmpty()) {
                rUrl += "?" + queryString;
              }
              redirectPath += "?rurl=" + URLEncoder.encode(rUrl, "UTF-8");
            }
            resp.sendRedirect(redirectPath);  // login please
          }
          return;
        }
      } else {
        String role = userDetails.getRole();
        if (SecurityConstants.ROLE_KIOSKOWNER.equals(role)) { // Kiosk owner cannot access this interface
          SessionMgr.cleanupSession(req.getSession());
          resp.sendRedirect(LOGIN_URL + "?status=4");
          return;
        }
        if ((servletPath.contains("/admin/") || servletPath.startsWith(TASK_URL))
            && !SecurityConstants.ROLE_SUPERUSER.equals(role)) { // only superuser can access
          SessionMgr.cleanupSession(req.getSession());
          resp.sendRedirect(LOGIN_URL + "?status=4"); // access denied
          return;
        }
      }
    }
    if (filterChain != null) {
      filterChain.doFilter(request, response);
    }
  }

  @Override
  public void destroy() {
  }

  @Override
  public void init(FilterConfig arg0) throws ServletException {
  }
}

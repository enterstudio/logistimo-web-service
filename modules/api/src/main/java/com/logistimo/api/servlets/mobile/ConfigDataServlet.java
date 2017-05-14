package com.logistimo.api.servlets.mobile;

import com.logistimo.api.servlets.SgServlet;
import com.logistimo.auth.SecurityConstants;
import com.logistimo.auth.SecurityUtil;
import com.logistimo.config.service.ConfigurationMgmtService;
import com.logistimo.config.service.impl.ConfigurationMgmtServiceImpl;
import com.logistimo.users.entity.IUserAccount;
import com.logistimo.users.service.UsersService;
import com.logistimo.users.service.impl.UsersServiceImpl;

import com.logistimo.config.entity.IConfig;

import com.logistimo.services.ObjectNotFoundException;
import com.logistimo.services.ServiceException;
import com.logistimo.services.Services;
import com.logistimo.proto.RestConstantsZ;
import com.logistimo.constants.SourceConstants;
import com.logistimo.logger.XLog;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ResourceBundle;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Configuration data retrieval
 *
 * @author Arun
 */

@SuppressWarnings("serial")
public class ConfigDataServlet extends SgServlet {

  private static final String ACTION_GETSYSCONFIG = "getsysconfig";

  // Added a logger to help debug this servlet's behavior (arun, 1/11/09)
  private static final XLog xLogger = XLog.getLog(ConfigDataServlet.class);

  // Get the system configuration, given a key
  private static void getSysConfig(HttpServletRequest req, HttpServletResponse resp) {
    xLogger.fine("Entered getSysConfig");
    String userId = req.getParameter("userid");
    String password = req.getParameter("password");
    if (userId == null || userId.isEmpty() || password == null || password.isEmpty()) {
      xLogger.severe("Invalid user name or password");
      return;
    }
    String key = req.getParameter("key");
    if (key == null || key.isEmpty()) {
      xLogger.severe("Invalid key");
      return;
    }
    // Authenticate user
    String responseText = null;
    try {
      UsersService as = Services.getService(UsersServiceImpl.class);
      IUserAccount user = as.authenticateUser(userId, password, SourceConstants.MOBILE);
      if (user == null) {
        responseText = "Invalid user name or password";
      } else if (SecurityUtil.compareRoles(user.getRole(), SecurityConstants.ROLE_DOMAINOWNER) < 0) {
        responseText = "You are not authorized to do this operation.";
      }
    } catch (Exception e) {
      xLogger.severe("{0} when getting config. for key {1}: {2}", e.getClass().getName(), key,
          e.getMessage());
      responseText = e.getClass().getName() + ": " + e.getMessage();
    }
    if (responseText != null) {
      xLogger.severe(responseText);
      return;
    }
    // Get the configuration
    try {
      ConfigurationMgmtService cms = Services.getService(ConfigurationMgmtServiceImpl.class);
      IConfig config = cms.getConfiguration(key);
      responseText = config.getConfig();
    } catch (ObjectNotFoundException e) {
      xLogger.warn("Configuration not available for key: " + key);
    } catch (Exception e) {
      xLogger.severe("{0} when getting config. for key {1}: {2}", e.getClass().getName(), key,
          e.getMessage());
    }
    if (responseText != null) {
      try {
        resp.setContentType("text/plain; charset=UTF-8");
        PrintWriter pw = resp.getWriter();
        pw.write(responseText);
        pw.close();
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
    xLogger.fine("Exiting getSysConfig");
  }

  public void processGet(HttpServletRequest req, HttpServletResponse resp,
                         ResourceBundle backendMessages, ResourceBundle messages)
      throws IOException, ServiceException {
    String action = req.getParameter(RestConstantsZ.ACTION);
    if (ACTION_GETSYSCONFIG.equals(action)) {
      getSysConfig(req, resp);
    } else {
      xLogger.severe("Invalid action: " + action);
    }
  }

  public void processPost(HttpServletRequest req, HttpServletResponse resp,
                          ResourceBundle backendMessages, ResourceBundle messages)
      throws IOException, ServiceException {
  }
}

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
package com.logistimo.api.servlets;

import com.google.gson.Gson;

import com.logistimo.config.entity.IConfig;
import com.logistimo.config.models.DomainConfig;
import com.logistimo.config.service.ConfigurationMgmtService;
import com.logistimo.config.service.impl.ConfigurationMgmtServiceImpl;
import com.logistimo.domains.CopyConfigModel;
import com.logistimo.logger.XLog;
import com.logistimo.services.ServiceException;
import com.logistimo.services.Services;

import java.io.IOException;
import java.util.ResourceBundle;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author Arun
 */
@SuppressWarnings("serial")
public class ConfigurationServlet extends SgServlet {

  private static final XLog xLogger = XLog.getLog(ConfigurationServlet.class);
  // Actions
  private static final String ACTION_COPYDOMAINCONFIG = "copydomainconfig";

  public void processGet(HttpServletRequest req, HttpServletResponse resp,
                         ResourceBundle backendMessages, ResourceBundle messages)
      throws ServletException, IOException, ServiceException {
    processPost(req, resp, backendMessages, messages);
  }

  public void processPost(HttpServletRequest req, HttpServletResponse resp,
                          ResourceBundle backendMessages, ResourceBundle messages)
      throws ServletException, IOException, ServiceException {
    xLogger.fine("Entered processPost");
    // Get the action
    String action = req.getParameter("action");
    if (ACTION_COPYDOMAINCONFIG.equals(action)) {
      copyDomainConfig(req);
    } else {
      xLogger.warn("Action field is mandatory");
      throw new ServiceException("Action field is mandatory");
    }

    xLogger.fine("Exiting processPost");
  }

  // Copy configuration from one domain to another
  private void copyDomainConfig(HttpServletRequest req) {
    xLogger.fine("Entered copyConfiguration");

    String data = req.getParameter("data");
    Long srcDomainId = null;
    Long destDomainId = null;
    try {
      CopyConfigModel ccm = new Gson().fromJson(data, CopyConfigModel.class);
      srcDomainId = ccm.getSourceDomainId();
      destDomainId = ccm.getDestinationDomainId();
      ConfigurationMgmtService cms = Services.getService(ConfigurationMgmtServiceImpl.class);
      cms.copyConfiguration(srcDomainId, destDomainId);
    } catch (Exception e) {
      xLogger.severe("{0} when copying config. from domain {1} to domain {2}: {3}",
          e.getClass().getName(), srcDomainId, destDomainId, e.getMessage());
    }

    xLogger.fine("Exiting copyConfiguration");
  }

  private class ConfigContainer {
    public IConfig c = null;
    public DomainConfig dc = null;
    public boolean add = false;
  }
}

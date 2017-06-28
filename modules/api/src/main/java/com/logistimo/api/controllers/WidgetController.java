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

import com.logistimo.api.builders.WidgetBuilder;
import com.logistimo.api.models.WidgetConfigModel;
import com.logistimo.api.models.WidgetModel;
import com.logistimo.api.request.DBWUpdateRequest;
import com.logistimo.auth.utils.SecurityUtils;
import com.logistimo.auth.utils.SessionMgr;
import com.logistimo.dashboards.entity.IWidget;
import com.logistimo.dashboards.service.IDashboardService;
import com.logistimo.dashboards.service.impl.DashboardService;
import com.logistimo.exception.InvalidServiceException;
import com.logistimo.logger.XLog;
import com.logistimo.security.SecureUserDetails;
import com.logistimo.services.Resources;
import com.logistimo.services.ServiceException;
import com.logistimo.services.Services;
import com.logistimo.utils.MsgUtil;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

import javax.servlet.http.HttpServletRequest;

/**
 * @author Mohan Raja
 */
@Controller
@RequestMapping("/widget")
public class WidgetController {
  private static final XLog xLogger = XLog.getLog(WidgetController.class);

  WidgetBuilder builder = new WidgetBuilder();

  @RequestMapping(value = "/", method = RequestMethod.POST)
  public
  @ResponseBody
  String create(@RequestBody WidgetModel model, HttpServletRequest request) {
    SecureUserDetails sUser = SecurityUtils.getUserDetails(request);
    Locale locale = sUser.getLocale();
    ResourceBundle backendMessages = Resources.get().getBundle("BackendMessages", locale);
    long domainId = SessionMgr.getCurrentDomain(request.getSession(), sUser.getUsername());
    try {
      IWidget wid = builder.buildWidget(model, domainId, sUser.getUsername());
      IDashboardService ds = Services.getService(DashboardService.class);
      ds.createWidget(wid);
      model.config.wId = wid.getwId();
      IWidget widConfig = builder.updateWidgetConfig(ds, model.config);
      ds.updateWidgetConfig(widConfig);
    } catch (ServiceException e) {
      xLogger.severe("Error creating Widget for " + domainId);
      throw new InvalidServiceException("Error creating Widget for " + domainId);
    }
    return "Widget " + MsgUtil.bold(model.nm) + " " + backendMessages.getString("created.success");
  }

  @RequestMapping(value = "/", method = RequestMethod.GET)
  public
  @ResponseBody
  List<WidgetModel> getAll(HttpServletRequest request) {
    SecureUserDetails sUser = SecurityUtils.getUserDetails(request);
    long domainId = SessionMgr.getCurrentDomain(request.getSession(), sUser.getUsername());
    IDashboardService ds = Services.getService(DashboardService.class);
    List<IWidget> dbList = ds.getWidgets(domainId);
    return builder.buildWidgetModelList(dbList);
  }

  @RequestMapping(value = "/{wId}", method = RequestMethod.GET)
  public
  @ResponseBody
  WidgetModel get(@PathVariable Long wId) {
    try {
      IDashboardService ds = Services.getService(DashboardService.class);
      IWidget wid = ds.getWidget(wId);
      return builder.buildWidgetModel(wid, true);
    } catch (ServiceException e) {
      xLogger.warn("Error in getting Widget: " + wId);
      throw new InvalidServiceException("Error in getting Widget: " + wId);
    }
  }

  @RequestMapping(value = "/delete", method = RequestMethod.POST)
  public
  @ResponseBody
  String delete(@RequestParam Long id) {
    String name;
    try {
      IDashboardService ds = Services.getService(DashboardService.class);
      name = ds.deleteWidget(id);
    } catch (ServiceException e) {
      xLogger.severe("Error deleting Widget: " + id);
      throw new InvalidServiceException("Error deleting Widget: " + id);
    }
    return "Widget " + MsgUtil.bold(name) + " is deleted successfully.";
  }

  @RequestMapping(value = "/update", method = RequestMethod.POST)
  public
  @ResponseBody
  String update(@RequestBody DBWUpdateRequest rObj) {
    String name;
    try {
      IDashboardService ds = Services.getService(DashboardService.class);
      name = ds.updateWidget(rObj.id, rObj.ty, rObj.val);
    } catch (ServiceException e) {
      xLogger.severe("Error updating Widget: " + rObj.id);
      throw new InvalidServiceException("Error updating widget: " + rObj.id);
    }
    return "Widget " + MsgUtil.bold(name) + " is updated successfully.";
  }

  @RequestMapping(value = "/saveconfig", method = RequestMethod.POST)
  public
  @ResponseBody
  String saveConfig(@RequestBody WidgetConfigModel model) {
    String name;
    try {
      IDashboardService ds = Services.getService(DashboardService.class);
      IWidget wid = builder.updateWidgetConfig(ds, model);
      ds.updateWidgetConfig(wid);
      name = wid.getName();
    } catch (ServiceException e) {
      xLogger.severe("Error in saving configuration for widget" + model.wId);
      throw new InvalidServiceException("Error in saving configuration for widget" + model.wId);
    }
    return "Widget configuration for " + MsgUtil.bold(name) + " is updated successfully.";
  }

  @RequestMapping(value = "/getconfig", method = RequestMethod.GET)
  public
  @ResponseBody
  WidgetConfigModel getConfig(@RequestParam Long wId) {
    try {
      IDashboardService ds = Services.getService(DashboardService.class);
      IWidget wid = ds.getWidget(wId);
      return builder.getWidgetConfig(wid, false);
    } catch (ServiceException e) {
      xLogger.severe("Error in getting widget configuration for " + wId);
      throw new InvalidServiceException("Error in getting widget configuration for " + wId);
    }
  }

  @RequestMapping(value = "/getdata", method = RequestMethod.GET)
  public
  @ResponseBody
  WidgetConfigModel getWidgetData(@RequestParam Long wId) {
    try {
      IDashboardService ds = Services.getService(DashboardService.class);
      IWidget wid = ds.getWidget(wId);
      return builder.getWidgetConfig(wid, true);
    } catch (ServiceException e) {
      xLogger.severe("Error in getting widget configuration for " + wId);
      throw new InvalidServiceException("Error in getting widget configuration for " + wId);
    }
  }
}

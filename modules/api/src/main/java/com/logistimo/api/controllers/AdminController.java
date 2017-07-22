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
import com.logistimo.api.migrators.DomainLocIDConfigMigrator;
import com.logistimo.api.migrators.EventsConfigMigrator;
import com.logistimo.api.migrators.UserDomainIdsMigrator;
import com.logistimo.auth.SecurityConstants;
import com.logistimo.auth.utils.SecurityUtils;
import com.logistimo.constants.CharacterConstants;
import com.logistimo.constants.Constants;
import com.logistimo.events.handlers.EventHandler;
import com.logistimo.exception.InvalidServiceException;
import com.logistimo.logger.XLog;
import com.logistimo.security.SecureUserDetails;
import com.logistimo.services.ServiceException;
import com.logistimo.services.Services;
import com.logistimo.services.cache.MemcacheService;
import com.logistimo.services.impl.PMF;
import com.logistimo.users.entity.IUserDevice;
import com.logistimo.users.service.UsersService;
import com.logistimo.users.service.impl.UsersServiceImpl;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.io.IOException;
import java.util.Date;
import java.util.Locale;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Created by smriti on 30/07/15.
 */
@Controller
@RequestMapping("/admin")
public class AdminController {
  private static final XLog xLogger = XLog.getLog(AdminController.class);

  @RequestMapping(value = "/dailyevents", method = RequestMethod.GET)
  public
  @ResponseBody
  void dailyExport(@RequestParam Long dId, HttpServletRequest request) {
    try {
      SecureUserDetails
          sUser =
          SecurityUtils.getUserDetails(request);
      if (sUser.getRole().equals(SecurityConstants.ROLE_SUPERUSER) && dId != null) {
        EventHandler.createDailyEvents(dId);
      } else {
        throw new ServiceException("Permission denied");
      }
    } catch (ServiceException e) {
      xLogger.severe("Error while scheduling tasks for daily notification ", e);
    }
  }

  @RequestMapping(value = "/burstcache", method = RequestMethod.GET)
  public
  @ResponseBody
  void burstCache(HttpServletRequest request) {
    try {
      SecureUserDetails
          sUser =
          SecurityUtils.getUserDetails(request);
      if (sUser.getRole().equals(SecurityConstants.ROLE_SUPERUSER)) {
        PMF.get().getDataStoreCache().evictAll();
      } else {
        throw new ServiceException("Permission denied");
      }
    } catch (ServiceException e) {
      xLogger.severe("Error while bursting cache", e);
    }
  }

  @RequestMapping(value = "/batchNotify", method = RequestMethod.GET)
  public
  @ResponseBody
  void batchExport(@RequestParam Long dId, @RequestParam(required = false) Date start,
                   @RequestParam(required = false) Date end, HttpServletRequest request) {
    try {
      SecureUserDetails
          sUser =
          SecurityUtils.getUserDetails(request);
      if (sUser.getRole().equals(SecurityConstants.ROLE_SUPERUSER) && dId != null) {
        EventHandler.CustomDuration customDuration = null;
        if (start != null || end != null) {
          customDuration = new EventHandler.CustomDuration();
          customDuration.duration = new EventHandler.Duration();
          customDuration.duration.start = start;
          customDuration.duration.end = end;
        }
        EventHandler.batchNotify(dId, customDuration);
      } else {
        throw new ServiceException("Permission denied");
      }
    } catch (ServiceException e) {
      xLogger.severe("Error while scheduling tasks for daily notification", e);
    }
  }

  @RequestMapping(value = "/burstDashboardCache", method = RequestMethod.GET)
  public
  @ResponseBody
  void burstDashboardCache(HttpServletRequest request) {
    try {
      SecureUserDetails
          sUser =
          SecurityUtils.getUserDetails(request);
      xLogger.info("User {0} requested for object cache burst", sUser.getUsername());
      if (sUser.getRole().equals(SecurityConstants.ROLE_SUPERUSER)) {
        MemcacheService mcs = AppFactory.get().getMemcacheService();
        //Clear all dashboard
        mcs.deleteByPattern(Constants.DASHBOARD_CACHE_PREFIX + CharacterConstants.ASTERISK);
        mcs.deleteByPattern(
            Constants.PREDICTIVE_DASHBOARD_CACHE_PREFIX + CharacterConstants.ASTERISK);
        mcs.deleteByPattern(Constants.SESSACT_DASHBOARD_CACHE_PREFIX + CharacterConstants.ASTERISK);
        mcs.deleteByPattern(Constants.INV_DASHBOARD_CACHE_PREFIX + CharacterConstants.ASTERISK);
        //Clear all network views cache
        mcs.deleteByPattern(Constants.NW_HIERARCHY_CACHE_PREFIX + CharacterConstants.ASTERISK);
      } else {
        throw new ServiceException("Permission denied");
      }
    } catch (ServiceException e) {
      xLogger.severe("Error while bursting cache", e);
    }
  }

  @RequestMapping(value = "/migrateusers", method = RequestMethod.GET)
  public
  @ResponseBody
  void migrateUsers() {
    UserDomainIdsMigrator migrator = new UserDomainIdsMigrator();
    try {
      migrator.migrateUserDomainIds();
    } catch (Exception e) {
      xLogger.severe("Exception occurred during user domain ids migration", e);
    }
  }

  @RequestMapping(value = "/migrate240", method = RequestMethod.GET)
  public
  @ResponseBody
  void migrate240() {
    EventsConfigMigrator migrator = new EventsConfigMigrator();
    try {
      migrator.migrateEventsConfig();
    } catch (Exception e) {
      xLogger.severe("Exception occurred during user domain ids migration", e);
      throw new InvalidServiceException(e);
    }
  }

  @RequestMapping(value = "/updatedomainlocids", method = RequestMethod.GET)
  public
  @ResponseBody
  void updateDomainLocIds() {
    DomainLocIDConfigMigrator migrator = new DomainLocIDConfigMigrator();
    try {
      migrator.updateDomainLocConfig();
    } catch (Exception e) {
      xLogger.severe("Exception occurred during user domain ids migration", e);
      throw new InvalidServiceException(e);
    }
  }

  @RequestMapping(value = "/notification-token", method = RequestMethod.GET)
  public
  @ResponseBody
  String getUserDeviceToken(@RequestParam String userId, @RequestParam String appName,
                            HttpServletRequest request, HttpServletResponse response) {
    SecureUserDetails user = SecurityUtils.getUserDetails(request);
    Locale locale = user.getLocale();
    IUserDevice result = null;
    try {
      UsersService as = Services.getService(UsersServiceImpl.class, locale);
      result = as.getUserDevice(userId, appName);

    } catch (ServiceException e) {
      xLogger.warn("Error while getting device token for user {0}", userId, e);
      try {
        response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
      } catch (IOException e1) {
        xLogger.warn("Error while getting device token for user {0}", userId, e1);
      }
    }
    return (result != null) ? result.getToken() : "";
  }

}

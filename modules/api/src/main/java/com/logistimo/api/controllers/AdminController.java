package com.logistimo.api.controllers;

import com.logistimo.AppFactory;
import com.logistimo.api.migrators.EventsConfigMigrator;
import com.logistimo.api.migrators.UserDomainIdsMigrator;
import com.logistimo.api.security.SecurityMgr;
import com.logistimo.auth.SecurityConstants;
import com.logistimo.events.handlers.EventHandler;
import com.logistimo.exception.InvalidServiceException;
import com.logistimo.services.cache.MemcacheService;

import com.logistimo.security.SecureUserDetails;
import com.logistimo.services.ServiceException;
import com.logistimo.services.impl.PMF;
import com.logistimo.constants.CharacterConstants;
import com.logistimo.constants.Constants;
import com.logistimo.logger.XLog;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Date;

import javax.servlet.http.HttpServletRequest;

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
          SecurityMgr.getUserDetails(request.getSession());
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
          SecurityMgr.getUserDetails(request.getSession());
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
          SecurityMgr.getUserDetails(request.getSession());
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
          SecurityMgr.getUserDetails(request.getSession());
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

}

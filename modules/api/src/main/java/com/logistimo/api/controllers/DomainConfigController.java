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
import com.logistimo.api.builders.BulletinBoardBuilder;
import com.logistimo.api.builders.ConfigurationModelsBuilder;
import com.logistimo.api.builders.CurrentUserBuilder;
import com.logistimo.api.builders.CustomReportsBuilder;
import com.logistimo.api.builders.InventoryBuilder;
import com.logistimo.api.builders.NotificationBuilder;
import com.logistimo.api.builders.UserBuilder;
import com.logistimo.api.builders.UserMessageBuilder;
import com.logistimo.api.constants.ConfigConstants;
import com.logistimo.api.migrators.CRConfigMigrator;
import com.logistimo.api.models.AccessLogModel;
import com.logistimo.api.models.CurrentUserModel;
import com.logistimo.api.models.MenuStatsModel;
import com.logistimo.api.models.TagsModel;
import com.logistimo.api.models.UserMessageModel;
import com.logistimo.api.models.configuration.AccountingConfigModel;
import com.logistimo.api.models.configuration.ApprovalsConfigModel;
import com.logistimo.api.models.configuration.AssetConfigModel;
import com.logistimo.api.models.configuration.BulletinBoardConfigModel;
import com.logistimo.api.models.configuration.CapabilitiesConfigModel;
import com.logistimo.api.models.configuration.CustomReportsConfigModel;
import com.logistimo.api.models.configuration.DashboardConfigModel;
import com.logistimo.api.models.configuration.GeneralConfigModel;
import com.logistimo.api.models.configuration.InventoryConfigModel;
import com.logistimo.api.models.configuration.NotificationsConfigModel;
import com.logistimo.api.models.configuration.NotificationsModel;
import com.logistimo.api.models.configuration.OrdersConfigModel;
import com.logistimo.api.models.configuration.SupportConfigModel;
import com.logistimo.api.models.configuration.TagsConfigModel;
import com.logistimo.api.request.AddCustomReportRequestObj;
import com.logistimo.auth.GenericAuthoriser;
import com.logistimo.auth.SecurityMgr;
import com.logistimo.auth.utils.SecurityUtils;
import com.logistimo.auth.utils.SessionMgr;
import com.logistimo.communications.MessageHandlingException;
import com.logistimo.config.entity.IConfig;
import com.logistimo.config.models.AccountingConfig;
import com.logistimo.config.models.ActualTransConfig;
import com.logistimo.config.models.ApprovalsConfig;
import com.logistimo.config.models.AssetConfig;
import com.logistimo.config.models.AssetSystemConfig;
import com.logistimo.config.models.BBoardConfig;
import com.logistimo.config.models.CapabilityConfig;
import com.logistimo.config.models.ConfigurationException;
import com.logistimo.config.models.CustomReportsConfig;
import com.logistimo.config.models.DashboardConfig;
import com.logistimo.config.models.DemandBoardConfig;
import com.logistimo.config.models.DomainConfig;
import com.logistimo.config.models.EventsConfig;
import com.logistimo.config.models.InventoryConfig;
import com.logistimo.config.models.LeadTimeAvgConfig;
import com.logistimo.config.models.MatStatusConfig;
import com.logistimo.config.models.OptimizerConfig;
import com.logistimo.config.models.OrdersConfig;
import com.logistimo.config.models.ReportsConfig;
import com.logistimo.config.models.SupportConfig;
import com.logistimo.config.models.SyncConfig;
import com.logistimo.config.service.ConfigurationMgmtService;
import com.logistimo.config.service.impl.ConfigurationMgmtServiceImpl;
import com.logistimo.constants.CharacterConstants;
import com.logistimo.constants.Constants;
import com.logistimo.dao.JDOUtils;
import com.logistimo.domains.entity.IDomain;
import com.logistimo.entity.IALog;
import com.logistimo.entity.IBBoard;
import com.logistimo.entity.IJobStatus;
import com.logistimo.entity.IMessageLog;
import com.logistimo.entity.IUploaded;
import com.logistimo.events.handlers.BBHandler;
import com.logistimo.exception.BadRequestException;
import com.logistimo.exception.ConfigurationServiceException;
import com.logistimo.exception.InvalidServiceException;
import com.logistimo.exception.InvalidTaskException;
import com.logistimo.exception.TaskSchedulingException;
import com.logistimo.exception.UnauthorizedException;
import com.logistimo.inventory.entity.ITransaction;
import com.logistimo.logger.XLog;
import com.logistimo.pagination.Navigator;
import com.logistimo.pagination.PageParams;
import com.logistimo.pagination.Results;
import com.logistimo.reports.ReportsConstants;
import com.logistimo.security.SecureUserDetails;
import com.logistimo.services.ObjectNotFoundException;
import com.logistimo.services.Resources;
import com.logistimo.services.ServiceException;
import com.logistimo.services.Services;
import com.logistimo.services.blobstore.BlobstoreService;
import com.logistimo.services.cache.MemcacheService;
import com.logistimo.services.impl.PMF;
import com.logistimo.services.taskqueue.ITaskService;
import com.logistimo.tags.TagUtil;
import com.logistimo.users.entity.IUserAccount;
import com.logistimo.users.service.UsersService;
import com.logistimo.users.service.impl.UsersServiceImpl;
import com.logistimo.utils.JobUtil;
import com.logistimo.utils.LocalDateUtil;
import com.logistimo.utils.MessageUtil;
import com.logistimo.utils.QueryUtil;
import com.logistimo.utils.StringUtil;

import org.apache.commons.lang.StringUtils;
import org.json.JSONException;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

import javax.jdo.PersistenceManager;
import javax.jdo.Query;
import javax.servlet.http.HttpServletRequest;

@Controller
@RequestMapping("/config/domain")
public class DomainConfigController {
  private static final XLog xLogger = XLog.getLog(DomainConfigController.class);
  private static final int ENTITY = 1;
  private static final int MATERIAL = 2;
  private static final int ORDER = 3;
  private static final int ROUTE = 4;
  private static final int USER = 5;
  UserMessageBuilder messageBuilder = new UserMessageBuilder();
  private ConfigurationModelsBuilder builder = new ConfigurationModelsBuilder();
  private CustomReportsBuilder crBuilder = new CustomReportsBuilder();
  private UserBuilder userBuilder = new UserBuilder();

  @RequestMapping(value = "/config/migrator/")
  public
  @ResponseBody
  void updateConfig(@RequestParam(required = false) String key) {
    boolean isSuccess;
    if (key == null) {
      isSuccess = CRConfigMigrator.update();
    } else if (key.contains(CharacterConstants.COMMA)) {
      isSuccess = CRConfigMigrator.update(Arrays.asList(key.split(CharacterConstants.COMMA)));
    } else {
      isSuccess = CRConfigMigrator.update(key);
    }
    if (isSuccess) {
      xLogger.info("Migrating configuration completed succesfully.");
    } else {
      xLogger.info("Error in migrating configuration");
    }
  }

  @RequestMapping(value = "/irmigrator/")
  public
  @ResponseBody
  void updateAutoPostConfig(@RequestParam(required = false) String key) {
    boolean isSuccess;
    if (key == null) {
      isSuccess = IRPostConfigMigrator.update();
    } else if (key.contains(CharacterConstants.COMMA)) {
      isSuccess = IRPostConfigMigrator.update(Arrays.asList(key.split(CharacterConstants.COMMA)));
    } else {
      isSuccess = IRPostConfigMigrator.update(key);
    }
    if (isSuccess) {
      xLogger.info("Migrating configuration completed successfully.");
    } else {
      xLogger.info("Error in migrating configuration");
    }
  }

  @RequestMapping(value = "/tags/materials", method = RequestMethod.GET)
  public
  @ResponseBody
  TagsModel getMaterialTags(HttpServletRequest request) {
    return getTags(MATERIAL, request);
  }

  @RequestMapping(value = "/tags/route", method = RequestMethod.GET)
  public
  @ResponseBody
  TagsModel getRouteTags(HttpServletRequest request) {
    return getTags(ROUTE, request);
  }

  @RequestMapping(value = "/tags/order", method = RequestMethod.GET)
  public
  @ResponseBody
  TagsModel getOrderTags(HttpServletRequest request) {
    return getTags(ORDER, request);
  }

  @RequestMapping(value = "/tags/entities", method = RequestMethod.GET)
  public
  @ResponseBody
  TagsModel getEntityTags(HttpServletRequest request) {
    return getTags(ENTITY, request);
  }

  @RequestMapping(value = "/tags/user", method = RequestMethod.GET)
  public
  @ResponseBody
  TagsModel getUserTags(HttpServletRequest request) {
    return getTags(USER, request);
  }

  private TagsModel getTags(int type, HttpServletRequest request) {
    SecureUserDetails user = SecurityUtils.getUserDetails(request);
    Long domainId = SessionMgr.getCurrentDomain(request.getSession(), user.getUsername());
    DomainConfig dc = DomainConfig.getInstance(domainId);
    String tags = null;
    boolean allowUserDef = false;
    switch (type) {
      case ENTITY:
        tags = dc.getKioskTags();
        allowUserDef = !dc.forceTagsKiosk();
        break;
      case MATERIAL:
        tags = dc.getMaterialTags();
        allowUserDef = !dc.forceTagsMaterial();
        break;
      case ROUTE:
        tags = dc.getRouteTags();
        break;
      case ORDER:
        tags = dc.getOrderTags();
        allowUserDef = !dc.forceTagsOrder();
        break;
      case USER:
        tags = dc.getUserTags();
        allowUserDef = !dc.forceTagsUser();
    }
    return new TagsModel(StringUtil.getList(tags), allowUserDef);
  }

  private List generateUpdateList(String uId) {
    List<String> list = new ArrayList<>();
    list.add(uId);
    list.add(String.valueOf(System.currentTimeMillis()));
    return list;
  }

  @RequestMapping(value = "/country", method = RequestMethod.GET)
  public
  @ResponseBody
  List<String> getCountries(HttpServletRequest request) {
    SecureUserDetails user = SecurityUtils.getUserDetails(request);
    Long domainId = SessionMgr.getCurrentDomain(request.getSession(), user.getUsername());
    DomainConfig dc = DomainConfig.getInstance(domainId);
    return StringUtil.getList(dc.getCountry());
  }

  @RequestMapping(value = "/artype", method = RequestMethod.GET)
  public
  @ResponseBody
  String getActualRouteType(HttpServletRequest request) {
    SecureUserDetails user = SecurityUtils.getUserDetails(request);
    Long domainId = SessionMgr.getCurrentDomain(request.getSession(), user.getUsername());
    return DomainConfig.getInstance(domainId).getRouteBy();
  }

  @RequestMapping(value = "/", method = RequestMethod.GET)
  public
  @ResponseBody
  DomainConfig getDomainConfig(HttpServletRequest request) {
    SecureUserDetails user = SecurityUtils.getUserDetails(request);
    Long domainId = SessionMgr.getCurrentDomain(request.getSession(), user.getUsername());
    return DomainConfig.getInstance(domainId);
  }

  @RequestMapping(value = "/menustats", method = RequestMethod.GET)
  public
  @ResponseBody
  MenuStatsModel getDomainConfigMenuStats(HttpServletRequest request) {
    SecureUserDetails user = SecurityUtils.getUserDetails(request);
    Locale locale = user.getLocale();
    ResourceBundle backendMessages = Resources.get().getBundle("BackendMessages", locale);
    Long domainId = SessionMgr.getCurrentDomain(request.getSession(), user.getUsername());
    DomainConfig config = DomainConfig.getInstance(domainId);
    if (config != null) {
      try {
        return builder.buildMenuStats(user, config, locale, user.getTimezone(), request);
      } catch (ServiceException e) {
        throw new InvalidServiceException(backendMessages.getString("menustats.fetch.error"));
      }
    } else {
      xLogger.severe("Error in fetching menu status");
      throw new InvalidServiceException(backendMessages.getString("menustats.fetch.error"));
    }
  }

  @RequestMapping(value = "/optimizer", method = RequestMethod.GET)
  public
  @ResponseBody
  OptimizerConfig getOptimizerConfig(HttpServletRequest request) {
    SecureUserDetails user = SecurityUtils.getUserDetails(request);
    Long domainId = SessionMgr.getCurrentDomain(request.getSession(), user.getUsername());
    DomainConfig dc = DomainConfig.getInstance(domainId);
    return dc.getOptimizerConfig();
  }

  @RequestMapping(value = "/general", method = RequestMethod.GET)
  public
  @ResponseBody
  GeneralConfigModel getGeneralConfig(HttpServletRequest request) {
    SecureUserDetails sUser = SecurityMgr.getUserDetails(request.getSession());
    Locale locale = sUser.getLocale();
    ResourceBundle backendMessages = Resources.get().getBundle("BackendMessages", locale);
    if (!GenericAuthoriser.authoriseAdmin(request)) {
      throw new UnauthorizedException(backendMessages.getString("permission.denied"));
    }
    String userId = sUser.getUsername();
    Long domainId = SessionMgr.getCurrentDomain(request.getSession(), userId);
    try {
      return builder.buildGeneralConfigModel(domainId, locale, sUser.getTimezone());
    } catch (ServiceException | ObjectNotFoundException | ConfigurationException e) {
      xLogger.severe("Error in fetching general configuration", e);
      throw new InvalidServiceException(backendMessages.getString("general.config.fetch.error"));
    }
  }

  @RequestMapping(value = "/support", method = RequestMethod.GET)
  public
  @ResponseBody
  SupportConfigModel getSupportConfig(HttpServletRequest request) {
    SecureUserDetails user = SecurityUtils.getUserDetails(request);
    Locale locale = user.getLocale();
    ResourceBundle backendMessages = Resources.get().getBundle("BackendMessages", locale);
    try {
      return builder.buildSCModelForWebDisplay(request);
    } catch (ServiceException | ObjectNotFoundException | ConfigurationException e) {
      xLogger.severe("Error in fetching support configuration for the domain", e);
      throw new InvalidServiceException(
          backendMessages.getString("general.support.config.fetch.error"));
    }
  }

  @RequestMapping(value = "/accounts", method = RequestMethod.GET)
  public
  @ResponseBody
  AccountingConfigModel getAccountingConfig(HttpServletRequest request) {
    SecureUserDetails sUser = SecurityUtils.getUserDetails(request);
    String userId = sUser.getUsername();
    Long domainId = SessionMgr.getCurrentDomain(request.getSession(), userId);
    Locale locale = sUser.getLocale();
    ResourceBundle backendMessages = Resources.get().getBundle("BackendMessages", locale);
    try {
      return builder.buildAccountingConfigModel(domainId, locale, sUser.getTimezone());
    } catch (ServiceException | ObjectNotFoundException | JSONException e) {
      xLogger.severe("Error in fetching account configuration", e);
      throw new InvalidServiceException(backendMessages.getString("account.config.fetch.error"));
    }
  }

  @RequestMapping(value = "/tags", method = RequestMethod.GET)
  public
  @ResponseBody
  TagsConfigModel getTags(HttpServletRequest request) {
    SecureUserDetails sUser = SecurityMgr.getUserDetails(request.getSession());
    Locale locale = sUser.getLocale();
    ResourceBundle backendMessages = Resources.get().getBundle("BackendMessages", locale);
    String userId = sUser.getUsername();
    Long domainId = SessionMgr.getCurrentDomain(request.getSession(), userId);
    try {
      if (domainId == null) {
        throw new InvalidServiceException(backendMessages.getString("tags.config.fetch.error"));
      }
      DomainConfig dc = DomainConfig.getInstance(domainId);
      return builder.buildTagsConfigModel(dc, locale, sUser.getTimezone());
    } catch (ServiceException | ObjectNotFoundException | ConfigurationServiceException e) {
      xLogger.severe("Error in fetching tags", e);
      throw new InvalidServiceException(backendMessages.getString("tags.config.fetch.error"));
    }
  }

  @RequestMapping(value = "/tags", method = RequestMethod.POST)
  public
  @ResponseBody
  String setTags(@RequestBody TagsConfigModel model, HttpServletRequest request) {
    SecureUserDetails sUser = SecurityMgr.getUserDetails(request.getSession());
    Locale locale = sUser.getLocale();
    ResourceBundle backendMessages = Resources.get().getBundle("BackendMessages", locale);
    if (model == null) {
      throw new BadRequestException(backendMessages.getString("tags.config.update.error"));
    }
    String userId = sUser.getUsername();
    Long domainId = SessionMgr.getCurrentDomain(request.getSession(), userId);
    try {
      if (domainId == null) {
        xLogger.severe("Error in updating tags configuration");
        throw new InvalidServiceException(backendMessages.getString("tags.config.update.error"));
      }
      ConfigContainer cc = getDomainConfig(domainId, userId, sUser.getLocale());
      cc.dc.addDomainData(ConfigConstants.TAGS, generateUpdateList(userId));
      StringBuilder mtags = new StringBuilder();
      for (int i = 0; i < model.mt.length; i++) {
        if (StringUtils.isNotEmpty(model.mt[i])) {
          mtags.append(model.mt[i].concat(","));
        }
      }
      if (mtags.length() > 0) {
        mtags.setLength(mtags.length() - 1);
      }
      cc.dc.setMaterialTags(TagUtil.getCleanTags(mtags.toString(), true));
      StringBuilder etags = new StringBuilder();
      for (int j = 0; j < model.et.length; j++) {
        if (StringUtils.isNotEmpty(model.et[j])) {
          etags.append(model.et[j].concat(","));
        }
      }
      cc.dc.setKioskTags(TagUtil.getCleanTags(etags.toString(), true));
      StringBuilder rtags = new StringBuilder();
      for (int z = 0; z < model.rt.length; z++) {
        if (StringUtils.isNotEmpty(model.rt[z])) {
          rtags.append(model.rt[z].concat(","));
        }
      }
      cc.dc.setRouteTags(TagUtil.getCleanTags(rtags.toString(), false));
      StringBuilder otags = new StringBuilder();
      for (int z = 0; z < model.ot.length; z++) {
        if (StringUtils.isNotEmpty(model.ot[z])) {
          otags.append(model.ot[z].concat(","));
        }
      }
      cc.dc.setOrderTags(TagUtil.getCleanTags(otags.toString(), true));
      StringBuilder utags = new StringBuilder();
      for (int z = 0; z < model.ut.length; z++) {
        if (StringUtils.isNotEmpty(model.ut[z])) {
          utags.append(model.ut[z].concat(","));
        }
      }

      Map<String, Integer> entityOrderMap = new LinkedHashMap<>();
      if (model.etr != null && model.etr.size() > 0) {
        for (TagsConfigModel.ETagOrder eto : model.etr) {
          entityOrderMap.put(eto.etg, eto.rnk);
        }
      }
      cc.dc.setEntityTagOrder(entityOrderMap);
      cc.dc.setUserTags(TagUtil.getCleanTags(utags.toString(), true));

      cc.dc.setForceTagsKiosk(model.eet);
      cc.dc.setForceTagsMaterial(model.emt);
      cc.dc.setForceTagsOrder(model.eot);
      cc.dc.setRouteBy(model.en);
      cc.dc.setForceTagsUser(model.eut);
      saveDomainConfig(sUser.getLocale(), domainId, cc, backendMessages);
      xLogger.info("AUDITLOG \t {0} \t {1} \t CONFIGURATION \t " +
          "SET TAGS ", domainId, sUser.getUsername());
    } catch (ServiceException e) {
      xLogger.severe("Error in updating tags configuration", e);
      throw new InvalidServiceException(backendMessages.getString("tags.config.update.error"));
    } catch (ConfigurationException e) {
      xLogger.severe("Error in updating tags configuration", e);
      throw new InvalidServiceException(backendMessages.getString("tags.config.update.error"));
    }
    return backendMessages.getString("tags.config.update.success");
  }

  @RequestMapping(value = "/map/locations", method = RequestMethod.GET)
  public
  @ResponseBody
  String getConfiguredMapLocations(HttpServletRequest request) {
    SecureUserDetails sUser = SecurityMgr.getUserDetails(request.getSession());
    String userId = sUser.getUsername();
    Long domainId = SessionMgr.getCurrentDomain(request.getSession(), userId);

    try {
      ConfigurationMgmtService cms = Services.getService(ConfigurationMgmtServiceImpl.class);
      IConfig c = cms.getConfiguration(IConfig.MAPLOCATIONCONFIG);
      return c.getConfig();
    } catch (Exception e) {
      xLogger.warn("{0} while fetching map location configuration for domain {1}", e.getMessage(),
          domainId);
    }

    return "{}";
  }

  @RequestMapping(value = "/dashboards", method = RequestMethod.GET)
  public
  @ResponseBody
  String getSystemDashboardConfig(HttpServletRequest request) {
    SecureUserDetails sUser = SecurityMgr.getUserDetails(request.getSession());
    String userId = sUser.getUsername();
    Long domainId = SessionMgr.getCurrentDomain(request.getSession(), userId);
    try {
      ConfigurationMgmtService cms = Services.getService(ConfigurationMgmtServiceImpl.class);
      IConfig c = cms.getConfiguration(IConfig.DASHBOARDCONFIG);
      return c.getConfig();
    } catch (Exception e) {
      xLogger.warn("{0} while fetching map location configuration for domain {1}", e.getMessage(),
          domainId);
    }

    return "{}";
  }

  @RequestMapping(value = "/asset", method = RequestMethod.GET)
  public
  @ResponseBody
  AssetConfigModel getAssetConfig(HttpServletRequest request) {
    SecureUserDetails sUser = SecurityMgr.getUserDetails(request.getSession());
    String userId = sUser.getUsername();
    Locale locale = sUser.getLocale();
    Long domainId = SessionMgr.getCurrentDomain(request.getSession(), userId);
    DomainConfig dc = DomainConfig.getInstance(domainId);
    try {
      return builder.buildAssetConfigModel(dc, locale, sUser.getTimezone());
    } catch (ConfigurationException | ServiceException | ObjectNotFoundException e) {
      xLogger.severe("Error in fetching vendor names");
    }

    return null;
  }

/*    @RequestMapping(value = "/temperature/vendors", method = RequestMethod.GET)
    public
    @ResponseBody
    Map<String, String> getConfiguredTempVendors(HttpServletRequest request) {
        SecureUserDetails sUser = SecurityManager.getUserDetails(request.getSession());
        Locale locale = sUser.getLocale();
        ResourceBundle backendMessages = Resources.get().getBundle("BackendMessages", locale);
        String userId = sUser.getUsername();
        Long domainId = SessionMgr.getCurrentDomain(request.getSession(), userId);
        DomainConfig dc = DomainConfig.getInstance(domainId);
        AssetConfig tc = dc.getAssetConfig();
        try {
            AssetSystemConfig tsc = AssetSystemConfig.getInstance();
            Map<String, String> returnVal = new LinkedHashMap<String, String>();

            if(tc != null && tsc != null){
                List<String> cfgVendorIds = tc.getVendorIds();
                for (String vId : cfgVendorIds) {
                    returnVal.put(vId, tsc.getVendorName(vId));
                }
            }
            return returnVal;
        } catch (ConfigurationException e) {
            xLogger.severe("Error in fetching temperature vendors");
            throw new ConfigurationServiceException(backendMessages.getString("temp.vendors.fetch.error"));
        }
    }*/

  @RequestMapping(value = "/assetconfig", method = RequestMethod.GET)
  public
  @ResponseBody
  Map<Integer, String> getAsset(@RequestParam String type) {
    try {
      AssetSystemConfig assets = AssetSystemConfig.getInstance();
      return assets.getAssetsNameByType(Integer.valueOf(type));
    } catch (ConfigurationException e) {
      xLogger.severe("Error in reading Asset System Configuration", e);
      throw new InvalidServiceException("Error in reading asset meta data.");
    }
  }

  @RequestMapping(value = "/assetconfig/manufacturer", method = RequestMethod.GET)
  public
  @ResponseBody
  Map<String, String> getAssetManufacturer(@RequestParam String type) {
    try {
      AssetSystemConfig assets = AssetSystemConfig.getInstance();
      return assets.getManufacturersByType(Integer.valueOf(type));
    } catch (ConfigurationException e) {
      xLogger.severe("Error in reading Asset System Configuration for manufacturers", e);
      throw new InvalidServiceException("Error in reading asset meta data.");
    }
  }

  @RequestMapping(value = "/assetconfig/workingstatus", method = RequestMethod.GET)
  public
  @ResponseBody
  Map<Integer, String> getAssetWorkingStatus() {
    try {
      AssetSystemConfig assets = AssetSystemConfig.getInstance();
      return assets.getAllWorkingStatus();
    } catch (ConfigurationException e) {
      xLogger.severe("Error in reading Asset System Configuration for manufacturers", e);
      throw new InvalidServiceException("Error in reading asset meta data.");
    }
  }

  @RequestMapping(value = "/asset", method = RequestMethod.POST)
  public
  @ResponseBody
  String updateAssetConfig(@RequestBody AssetConfigModel assetConfigModel,
                           HttpServletRequest request) {
    SecureUserDetails sUser = SecurityUtils.getUserDetails(request);
    Locale locale = sUser.getLocale();
    ResourceBundle backendMessages = Resources.get().getBundle("BackendMessages", locale);
    if (!GenericAuthoriser.authoriseAdmin(request)) {
      throw new UnauthorizedException(
          backendMessages.getString(backendMessages.getString("permission.denied")));
    }
    if (assetConfigModel == null) {
      xLogger.severe("Error in updating temperature configuration");
      throw new BadRequestException(backendMessages.getString("temp.config.update.error"));
    }
    try {
      String userId = sUser.getUsername();
      Long domainId = SessionMgr.getCurrentDomain(request.getSession(), userId);

      List<String> cfgVals = new ArrayList<String>(1), modelVals = new ArrayList<>(1),
          dSnsVals =
              new ArrayList<>(), dMpsVals = new ArrayList<>();
      if (assetConfigModel.assets != null) {
        for (AssetConfigModel.Asset asset : assetConfigModel.assets.values()) {
          if (asset.dMp != null) {
            dMpsVals.add(asset.id + Constants.KEY_SEPARATOR + asset.dMp);
          }
          if (asset.mcs != null) {
            for (AssetConfigModel.Mancfacturer mancfacturer : asset.mcs.values()) {
              if (mancfacturer.iC != null && mancfacturer.iC) {
                if (asset.id.equals(AssetSystemConfig.TYPE_TEMPERATURE_DEVICE)) {
                  cfgVals.add(mancfacturer.id);
                } else {
                  cfgVals.add(asset.id + Constants.KEY_SEPARATOR + mancfacturer.id);
                }

                if (mancfacturer.model != null) {
                  for (AssetConfigModel.Model model : mancfacturer.model.values()) {
                    if (model.iC != null && model.iC) {
                      modelVals.add(asset.id + Constants.KEY_SEPARATOR + mancfacturer.id
                          + Constants.KEY_SEPARATOR + model.name);
                      dSnsVals.add(
                          asset.id + Constants.KEY_SEPARATOR + model.name + Constants.KEY_SEPARATOR
                              + model.dS);
                    }
                  }
                }
              }
            }
          }
        }
      }

      ConfigContainer cc = getDomainConfig(domainId, userId, sUser.getLocale());

      AssetConfig tc = cc.dc.getAssetConfig();
      if (tc == null) {
        tc = new AssetConfig();
      }
      tc.setVendorIds(cfgVals);
      tc.setAssetModels(modelVals);
      tc.setDefaultSns(dSnsVals);
      tc.setDefaultMps(dMpsVals);
      tc.setEnable(assetConfigModel.enable);
      tc.setNamespace(assetConfigModel.namespace);
      if (assetConfigModel.config.getLocale() == null) {
        assetConfigModel.config.setLocale(new AssetConfig.Locale());
      }
      cc.dc.addDomainData(ConfigConstants.TEMPERATURE, generateUpdateList(userId));
      assetConfigModel.config.getLocale().setCn(cc.dc.getCountry());
      assetConfigModel.config.getLocale().setLn(cc.dc.getLanguage());
      assetConfigModel.config.getLocale().setTz(AssetConfig.getTimezoneOffset(cc.dc.getTimezone()));
      assetConfigModel.config.getLocale().setTznm(cc.dc.getTimezone());
      tc.setConfiguration(assetConfigModel.config);
      cc.dc.setAssetConfig(tc);
      saveDomainConfig(sUser.getLocale(), domainId, cc, backendMessages);
      xLogger.info("AUDITLOG \t {0} \t {1} \t CONFIGURATION \t " +
          "UPDATE TEMPERATURE", domainId, sUser.getUsername());
      xLogger.info(cc.dc.toJSONSring());
    } catch (ServiceException e) {
      xLogger.severe("Error in updating temperature configuration", e);
      throw new BadRequestException(backendMessages.getString("temp.config.update.error"));
    } catch (ConfigurationException e) {
      e.printStackTrace();
    }
    return backendMessages.getString("temp.config.update.success");
  }

  @RequestMapping(value = "/add", method = RequestMethod.POST)
  public
  @ResponseBody
  String updateAccountingConfig(@RequestBody AccountingConfigModel model,
                                HttpServletRequest request) {
    SecureUserDetails sUser = SecurityUtils.getUserDetails(request);
    Locale locale = sUser.getLocale();
    ResourceBundle backendMessages = Resources.get().getBundle("BackendMessages", locale);
    if (!GenericAuthoriser.authoriseAdmin(request)) {
      throw new UnauthorizedException(backendMessages.getString("permission.denied"));
    }
    if (model == null) {
      xLogger.severe("Error in updating Accounting configuration");
      throw new BadRequestException(backendMessages.getString("account.config.update.error"));
    }
    String userId = sUser.getUsername();
    Long domainId = SessionMgr.getCurrentDomain(request.getSession(), userId);
    try {
      ConfigContainer cc = getDomainConfig(domainId, userId, sUser.getLocale());
      AccountingConfig ac = new AccountingConfig();
      cc.dc.addDomainData(ConfigConstants.ACCOUNTING, generateUpdateList(userId));
      ac.setAccountingEnabled(model.ea);
      ac.setCreditLimit(model.cl);
      if ("cf".equalsIgnoreCase(model.en)) {
        ac.setEnforceConfirm(true);
      } else if ("cm".equalsIgnoreCase(model.en)) {
        ac.setEnforceShipped(true);
      }
      cc.dc.setAccountingConfig(ac);
      saveDomainConfig(sUser.getLocale(), domainId, cc, backendMessages);
      xLogger.info("AUDITLOG \t {0} \t {1} \t CONFIGURATION \t " +
          "UPDATE ACCOUNTING", domainId, sUser.getUsername());
      xLogger.info(cc.dc.toJSONSring());
    } catch (ServiceException e) {
      xLogger.severe("Error in updating Accounting configuration", e);
      throw new InvalidServiceException(backendMessages.getString("account.config.update.error"));
    } catch (ConfigurationException e) {
      e.printStackTrace();
    }
    return backendMessages.getString("account.config.update.success");
  }

  @RequestMapping(value = "/general", method = RequestMethod.POST)
  public
  @ResponseBody
  String updateGeneralConfig(@RequestBody GeneralConfigModel model, HttpServletRequest request) {
    SecureUserDetails sUser = SecurityMgr.getUserDetails(request.getSession());
    Locale locale = sUser.getLocale();
    ResourceBundle backendMessages = Resources.get().getBundle("BackendMessages", locale);
    if (!GenericAuthoriser.authoriseAdmin(request)) {
      throw new UnauthorizedException(backendMessages.getString("permission.denied"));
    }
    String userId = sUser.getUsername();
    Long domainId = SessionMgr.getCurrentDomain(request.getSession(), userId);
    try {
      ConfigContainer cc = getDomainConfig(domainId, userId, sUser.getLocale());
      cc.dc.addDomainData(ConfigConstants.GENERAL, generateUpdateList(userId));
      cc.dc.setCountry(model.cnt);
      cc.dc.setState(model.st);
      cc.dc.setDistrict(model.ds);
      cc.dc.setLanguage(model.lng);
      cc.dc.setTimezone(model.tz);
      cc.dc.setCurrency(model.cur);
      cc.dc.setPageHeader(model.pgh);
      if (model.sc) {
        cc.dc.setUiPreference(false);
      } else {
        cc.dc.setUiPreference(true);
      }
      if (model.support != null && !model.support.isEmpty()) {
        for (SupportConfigModel supportConfigModel : model.support) {
          SupportConfig sc = new SupportConfig();
          sc.setSupportUserRole(supportConfigModel.role);
          sc.setSupportUser(supportConfigModel.usrid);
          sc.setSupportUserName(supportConfigModel.usrname);
          sc.setSupportPhone(supportConfigModel.phnm);
          sc.setsupportEmail(supportConfigModel.em);
          cc.dc.setSupportConfigByRole(supportConfigModel.role, sc);
        }
      }

      if (cc.dc.getAssetConfig() != null && cc.dc.getAssetConfig().getConfiguration() != null
          && cc.dc.getAssetConfig().getConfiguration().getLocale() != null) {
        cc.dc.getAssetConfig().getConfiguration().getLocale().setCn(model.cnt);
        cc.dc.getAssetConfig().getConfiguration().getLocale().setLn(model.lng);
        cc.dc.getAssetConfig().getConfiguration().getLocale().setTznm(model.tz);
        cc.dc.getAssetConfig().getConfiguration().getLocale()
            .setTz(AssetConfig.getTimezoneOffset(model.tz));
      }
      cc.dc.setAdminContactConfigMap(model.adminContact);
      cc.dc.setEnableSwitchToNewHost(model.snh);
      cc.dc.setNewHostName(model.nhn);

      saveDomainConfig(sUser.getLocale(), domainId, cc, backendMessages);
      xLogger.info("AUDITLOG \t {0} \t {1} \t CONFIGURATION \t " +
          "UPDATE GENERAL", domainId, sUser.getUsername());
      xLogger.info(cc.dc.toJSONSring());
    } catch (ServiceException e) {
      xLogger.severe("Error in updating general configuration", e);
      throw new InvalidServiceException(backendMessages.getString("general.config.update.error"));
    } catch (ConfigurationException e) {
      xLogger.warn("Error while printing configuration to log", e);
    }
    return backendMessages.getString("general.config.update.success");
  }

  @RequestMapping(value = "/capabilities", method = RequestMethod.GET)
  public
  @ResponseBody
  CapabilitiesConfigModel getCapabilitiesConfig(HttpServletRequest request) {
        /*if (!Authoriser.authoriseAdmin(request)) {
            throw new UnauthorizedException("Permission Denied");
        }*/
    SecureUserDetails sUser = SecurityMgr.getUserDetails(request.getSession());
    String userId = sUser.getUsername();
    Long domainId = SessionMgr.getCurrentDomain(request.getSession(), userId);
    Locale locale = sUser.getLocale();
    ResourceBundle backendMessages = Resources.get().getBundle("BackendMessages", locale);
    try {
      if (domainId == null) {
        xLogger.severe("Error in fetching capabilities configuration");
        throw new InvalidServiceException(
            backendMessages.getString("capabilities.config.fetch.error"));
      }
      DomainConfig dc = DomainConfig.getInstance(domainId);
      return builder.buildCapabilitiesConfigModel(domainId, locale, dc, sUser.getTimezone());
    } catch (ServiceException | ObjectNotFoundException e) {
      xLogger.warn("Error in fetching capabilities configuration for {0}: {1}", locale, e);
      throw new InvalidServiceException(
          backendMessages.getString("capabilities.config.fetch.error"));
    }
  }

  @RequestMapping(value = "/rolecapabs", method = RequestMethod.GET)
  public
  @ResponseBody
  CapabilitiesConfigModel getRoleCapabilitiesConfig(HttpServletRequest request) {
        /*if (!Authoriser.authoriseAdmin(request)) {
            throw new UnauthorizedException("Permission Denied");
        }*/
    SecureUserDetails sUser = SecurityMgr.getUserDetails(request.getSession());
    String userId = sUser.getUsername();
    Long domainId = SessionMgr.getCurrentDomain(request.getSession(), userId);
    Locale locale = sUser.getLocale();
    ResourceBundle backendMessages = Resources.get().getBundle("BackendMessages", locale);
    try {
      if (domainId == null) {
        xLogger.severe("Error in fetching role capabilities configuration");
        throw new InvalidServiceException(
            backendMessages.getString("capabilities.config.fetch.error"));
      }
      DomainConfig dc = DomainConfig.getInstance(domainId);
      return builder.buildRoleCapabilitiesConfigModel(sUser.getRole(), domainId, locale, dc,
          sUser.getTimezone());
    } catch (ServiceException | ObjectNotFoundException e) {
      xLogger.warn("Error in fetching role capabilities configuration for {0}: {1}", locale, e);
      throw new InvalidServiceException(
          backendMessages.getString("capabilities.config.fetch.error"));
    }
  }

  @RequestMapping(value = "/capabilities", method = RequestMethod.POST)
  public
  @ResponseBody
  List<String> updateCapabilitiesConfig(@RequestBody CapabilitiesConfigModel model,
                                        HttpServletRequest request) {
    SecureUserDetails sUser = SecurityMgr.getUserDetails(request.getSession());
    Locale locale = sUser.getLocale();
    String timezone = sUser.getTimezone();
    ResourceBundle backendMessages = Resources.get().getBundle("BackendMessages", locale);
    if (!GenericAuthoriser.authoriseAdmin(request)) {
      throw new UnauthorizedException(backendMessages.getString("permission.denied"));
    }
    String userId = sUser.getUsername();
    Long domainId = SessionMgr.getCurrentDomain(request.getSession(), userId);
    try {
      ConfigContainer cc = getDomainConfig(domainId, userId, sUser.getLocale());
      cc.dc.addDomainData(ConfigConstants.CAPABILITIES, generateUpdateList(userId));
      cc.dc.setAuthenticationTokenExpiry(model.atexp);
      cc.dc.setLocalLoginRequired(model.llr);
      SyncConfig
          syncCfg =
          generateSyncConfig(
              model); // Generate SyncConfig from the model and set it in domain config object.
      cc.dc.setSyncConfig(syncCfg);
      if (StringUtils.isNotEmpty(model.ro)) {
        CapabilityConfig cconf = new CapabilityConfig();
        cconf.setCapabilities(model.tm);
        if (model.hi != null) {
          cconf.setTagsInventory(StringUtils.join(model.hi, ','));
        }
        if (model.ho != null) {
          cconf.setTagsOrders(StringUtils.join(model.ho, ','));
        }
        cconf.setSendVendors(model.sv);
        cconf.setSendCustomers(model.sc);
        if (model.gcs != null) {
          cconf.setGeoCodingStrategy(model.gcs);
        }
        List<String> creatableEntityTypes = null;
        if (model.et != null && model.et.length > 0) {
          creatableEntityTypes = StringUtil.getList(model.et);
        }
        cconf.setCreatableEntityTypes(creatableEntityTypes);
        cconf.setAllowRouteTagEditing(model.er);
        cconf.setLoginAsReconnect(model.lr);
        cconf.setEnableShippingOnMobile(model.eshp);
        cc.dc.setCapabilityByRole(model.ro, cconf);

        String issueTags = StringUtils.join(model.hii, ',');
        String receiptsTags = StringUtils.join(model.hir, ',');
        String stockTags = StringUtils.join(model.hip, ',');
        String discardTags = StringUtils.join(model.hiw, ',');
        String transferTags = StringUtils.join(model.hit, ',');

        Map<String, String> tagInvByOperation = new HashMap<String, String>();
        tagInvByOperation.put(ITransaction.TYPE_ISSUE, issueTags);
        tagInvByOperation.put(ITransaction.TYPE_RECEIPT, receiptsTags);
        tagInvByOperation.put(ITransaction.TYPE_PHYSICALCOUNT, stockTags);
        tagInvByOperation.put(ITransaction.TYPE_WASTAGE, discardTags);
        tagInvByOperation.put(ITransaction.TYPE_TRANSFER, transferTags);
        cconf.settagInvByOperation(tagInvByOperation);

      } else {
        cc.dc.setCapabilities(model.cap);
        cc.dc.setTransactionMenus(model.tm);
        if (model.hi != null) {
          cc.dc.setTagsInventory(TagUtil.getCleanTags(StringUtils.join(model.hi, ','), true));
        }
        if (model.ho != null) {
          cc.dc.setTagsOrders(TagUtil.getCleanTags(StringUtils.join(model.ho, ','), true));
        }
        cc.dc.setSendVendors(model.sv);
        cc.dc.setSendCustomers(model.sc);
        if (model.gcs != null) {
          cc.dc.setGeoCodingStrategy(model.gcs);
        }
        List<String> creatableEntityTypes = null;
        if (model.et != null && model.et.length > 0) {
          creatableEntityTypes = StringUtil.getList(model.et);
        }
        cc.dc.setCreatableEntityTypes(creatableEntityTypes);
        cc.dc.setAllowRouteTagEditing(model.er);
        cc.dc.setLoginAsReconnect(model.lr);
        cc.dc.setEnableShippingOnMobile(model.eshp);
        cc.dc.setStoreAppTheme(model.getTheme());

        String issueTags = TagUtil.getCleanTags(StringUtils.join(model.hii, ','), true);
        String receiptsTags = TagUtil.getCleanTags(StringUtils.join(model.hir, ','), true);
        String stockTags = TagUtil.getCleanTags(StringUtils.join(model.hip, ','), true);
        String discardTags = TagUtil.getCleanTags(StringUtils.join(model.hiw, ','), true);
        String transferTags = TagUtil.getCleanTags(StringUtils.join(model.hit, ','), true);

        Map<String, String> tagInvByOperation = new HashMap<String, String>();
        tagInvByOperation.put(ITransaction.TYPE_ISSUE, issueTags);
        tagInvByOperation.put(ITransaction.TYPE_RECEIPT, receiptsTags);
        tagInvByOperation.put(ITransaction.TYPE_PHYSICALCOUNT, stockTags);
        tagInvByOperation.put(ITransaction.TYPE_WASTAGE, discardTags);
        tagInvByOperation.put(ITransaction.TYPE_TRANSFER, transferTags);
        cc.dc.settagInvByOperation(tagInvByOperation);
      }
      saveDomainConfig(sUser.getLocale(), domainId, cc, backendMessages);
      xLogger.info("AUDITLOG \t {0} \t {1} \t CONFIGURATION \t " +
          "UPDATE CAPABILITIES", domainId, sUser.getUsername());
      xLogger.info(cc.dc.toJSONSring());
      return Arrays.asList(backendMessages.getString("capabilities.config.update.success"),
          LocalDateUtil.format(
              new Date(Long.parseLong(cc.dc.getDomainData(ConfigConstants.CAPABILITIES).get(1))),
              locale, timezone));
    } catch (ServiceException | ConfigurationException e) {
      xLogger.severe("Error in updating capabilities configuration", e);
      throw new InvalidServiceException(
          backendMessages.getString("capabilities.config.update.error"));
    }
  }

  @RequestMapping(value = "/inventory", method = RequestMethod.GET)
  public
  @ResponseBody
  InventoryConfigModel getInventoryConfiguration(HttpServletRequest request) {
        /*if (!Authoriser.authoriseAdmin(request)) {
            throw new UnauthorizedException("Permission Denied");
        }*/
    SecureUserDetails sUser = SecurityMgr.getUserDetails(request.getSession());
    String userId = sUser.getUsername();
    Locale locale = sUser.getLocale();
    Long domainId = SessionMgr.getCurrentDomain(request.getSession(), userId);
    DomainConfig dc = DomainConfig.getInstance(domainId);
    ResourceBundle backendMessages = Resources.get().getBundle("BackendMessages", locale);
    try {
      if (domainId == null) {
        xLogger.severe("Error in fetching Inventory configuration");
        throw new InvalidServiceException(
            backendMessages.getString("inventory.config.fetch.error"));
      }
      return builder.buildInventoryConfigModel(dc, locale, domainId, sUser.getTimezone());
    } catch (ConfigurationException | ServiceException | ObjectNotFoundException e) {
      xLogger.severe("Error in fetching Inventory configuration", e);
      throw new InvalidServiceException(backendMessages.getString("inventory.config.fetch.error"));
    }
  }

  @RequestMapping(value = "/inventory/transReasons", method = RequestMethod.GET)
  public
  @ResponseBody
  Collection<String> getICTransactionReasons(HttpServletRequest request) {
        /*if (!Authoriser.authoriseAdmin(request)) {
            throw new UnauthorizedException("Permission Denied");
        }*/
    SecureUserDetails sUser = SecurityMgr.getUserDetails(request.getSession());
    String userId = sUser.getUsername();
    Locale locale = sUser.getLocale();
    Long domainId = SessionMgr.getCurrentDomain(request.getSession(), userId);
    DomainConfig dc = DomainConfig.getInstance(domainId);
    ResourceBundle backendMessages = Resources.get().getBundle("BackendMessages", locale);
    try {
      if (domainId == null) {
        xLogger.severe("Error in fetching Inventory configuration");
        throw new InvalidServiceException(
            backendMessages.getString("inventory.config.fetch.error"));
      }
      return builder.buildUniqueTransactionReasons(dc.getInventoryConfig());
    } catch (Exception e) {
      xLogger.warn("Error in fetching reasons for transactions", e);
      return null;
    }
  }

  @RequestMapping(value = "/getactualtrans", method = RequestMethod.GET)
  public
  @ResponseBody
  boolean getActualTransDateCheck(HttpServletRequest request) {
    SecureUserDetails sUser = SecurityMgr.getUserDetails(request.getSession());
    String userId = sUser.getUsername();
    Locale locale = sUser.getLocale();
    Long domainId = SessionMgr.getCurrentDomain(request.getSession(), userId);
    boolean hasAtd = false;
    ResourceBundle backendMessages = Resources.get().getBundle("BackendMessages", locale);
    if (domainId == null) {
      xLogger.severe("Error in fetching Inventory configuration");
      throw new InvalidServiceException(backendMessages.getString("inventory.config.fetch.error"));
    }
    DomainConfig dc = DomainConfig.getInstance(domainId);
    InventoryConfig ic = dc.getInventoryConfig();
    hasAtd =
        ic.getActualTransConfigByType(ITransaction.TYPE_ISSUE) != null && !ic
            .getActualTransConfigByType(ITransaction.TYPE_ISSUE).getTy().equals("0") ? true : false;
    if (!hasAtd) {
      hasAtd =
          ic.getActualTransConfigByType(ITransaction.TYPE_RECEIPT) != null && !ic
              .getActualTransConfigByType(ITransaction.TYPE_RECEIPT).getTy().equals("0") ? true
              : false;
    }
    if (!hasAtd) {
      hasAtd =
          ic.getActualTransConfigByType(ITransaction.TYPE_PHYSICALCOUNT) != null && !ic
              .getActualTransConfigByType(ITransaction.TYPE_PHYSICALCOUNT).getTy().equals("0")
              ? true : false;
    }
    if (!hasAtd) {
      hasAtd =
          ic.getActualTransConfigByType(ITransaction.TYPE_TRANSFER) != null && !ic
              .getActualTransConfigByType(ITransaction.TYPE_TRANSFER).getTy().equals("0") ? true
              : false;
    }
    if (!hasAtd) {
      hasAtd =
          ic.getActualTransConfigByType(ITransaction.TYPE_WASTAGE) != null && !ic
              .getActualTransConfigByType(ITransaction.TYPE_WASTAGE).getTy().equals("0") ? true
              : false;
    }
    return hasAtd;
  }

  @RequestMapping(value = "/inventory", method = RequestMethod.POST)
  public
  @ResponseBody
  String updateInventoryConfig(@RequestBody InventoryConfigModel model,
                               HttpServletRequest request) {
    SecureUserDetails sUser = SecurityMgr.getUserDetails(request.getSession());
    Locale locale = sUser.getLocale();
    ResourceBundle backendMessages = Resources.get().getBundle("BackendMessages", locale);
    if (!GenericAuthoriser.authoriseAdmin(request)) {
      throw new UnauthorizedException(backendMessages.getString("permission.denied"));
    }
    String userId = sUser.getUsername();
    Long domainId = SessionMgr.getCurrentDomain(request.getSession(), userId);
    InventoryBuilder inventoryBuilder = new InventoryBuilder();
    try {
      if (domainId == null) {
        xLogger.severe("Error in updating Inventory configuration");
        throw new InvalidServiceException(
            backendMessages.getString("inventory.config.update.error"));
      }
      ConfigContainer cc = getDomainConfig(domainId, userId, sUser.getLocale());

      UsersService as = Services.getService(UsersServiceImpl.class, locale);
      IUserAccount u = as.getUserAccount(userId);

      cc.dc.addDomainData(ConfigConstants.INVENTORY, generateUpdateList(userId));
      //Get transaction export schedules
      String et = model.et;

      //Get transaction reasons
      String issueReasons = inventoryBuilder.trimReasons(model.ri);
      String receiptReasons = inventoryBuilder.trimReasons(model.rr);
      String stockCountReasons = inventoryBuilder.trimReasons(model.rs);
      String wastageReasons = inventoryBuilder.trimReasons(model.rd);
      String transferReasons = inventoryBuilder.trimReasons(model.rt);
      // Form object
      InventoryConfig inventoryConfig = new InventoryConfig();
      // Set reasons
      Map<String, String> transReasons = new HashMap<String, String>();
      transReasons.put(ITransaction.TYPE_ISSUE, issueReasons);
      transReasons.put(ITransaction.TYPE_RECEIPT, receiptReasons);
      transReasons.put(ITransaction.TYPE_PHYSICALCOUNT, stockCountReasons);
      transReasons.put(ITransaction.TYPE_WASTAGE, wastageReasons);
      transReasons.put(ITransaction.TYPE_TRANSFER, transferReasons);
      inventoryConfig.setTransReasons(transReasons);

      //reset the wastage reasons
      cc.dc.setWastageReasons(null);
      boolean icmt = model.cimt;
      if (icmt) {
        inventoryConfig.setCimt(icmt);
        Map<String, String> imTransResons = new HashMap<String, String>();
        if (model.imt != null) {
          for (InventoryConfigModel.MTagReason mTagReason : model.imt) {
            imTransResons.put(mTagReason.mtg, inventoryBuilder.trimReasons(mTagReason.rsn));
          }
        }
        inventoryConfig.setImtransreasons(imTransResons);
      }
      boolean crmt = model.crmt;
      if (crmt) {
        inventoryConfig.setCrmt(crmt);
        Map<String, String> rmTransReasons = new HashMap<String, String>();
        if (model.rmt != null) {
          for (InventoryConfigModel.MTagReason mTagReason : model.rmt) {
            rmTransReasons.put(mTagReason.mtg, inventoryBuilder.trimReasons(mTagReason.rsn));
          }
        }
        inventoryConfig.setRmtransreasons(rmTransReasons);
      }
      boolean csmt = model.csmt;
      if (csmt) {
        inventoryConfig.setCsmt(csmt);
        Map<String, String> smTransReasons = new HashMap<String, String>();
        if (model.smt != null) {
          for (InventoryConfigModel.MTagReason mTagReason : model.smt) {
            smTransReasons.put(mTagReason.mtg, inventoryBuilder.trimReasons(mTagReason.rsn));
          }
        }
        inventoryConfig.setSmtransreasons(smTransReasons);
      }
      boolean ctmt = model.ctmt;
      if (ctmt) {
        inventoryConfig.setCtmt(ctmt);
        Map<String, String> tmTransReasons = new HashMap<String, String>();
        if (model.tmt != null) {
          for (InventoryConfigModel.MTagReason mTagReason : model.tmt) {
            tmTransReasons.put(mTagReason.mtg, inventoryBuilder.trimReasons(mTagReason.rsn));
          }
        }
        inventoryConfig.setTmtransreasons(tmTransReasons);
      }
      boolean cdmt = model.cdmt;
      if (cdmt) {
        inventoryConfig.setCdmt(cdmt);
        Map<String, String> dmTransReasons = new HashMap<String, String>();
        if (model.dmt != null) {
          for (InventoryConfigModel.MTagReason mTagReason : model.dmt) {
            dmTransReasons.put(mTagReason.mtg, inventoryBuilder.trimReasons(mTagReason.rsn));
          }
        }
        inventoryConfig.setDmtransreasons(dmTransReasons);
      }

      String issueDefaultStatus = StringUtil.trimCommas(model.idf);
      String issueTempSenStatus = StringUtil.trimCommas(model.iestm);
      MatStatusConfig msi = new MatStatusConfig();
      msi.setDf(issueDefaultStatus);
      msi.setEtsm(issueTempSenStatus);
      msi.setStatusMandatory(model.ism);
      inventoryConfig.setMatStatusConfigByType(ITransaction.TYPE_ISSUE, msi);

      String receiptDefaultStatus = StringUtil.trimCommas(model.rdf);
      String receiptTempSenStatus = StringUtil.trimCommas(model.restm);
      MatStatusConfig msr = new MatStatusConfig();
      msr.setDf(receiptDefaultStatus);
      msr.setEtsm(receiptTempSenStatus);
      msr.setStatusMandatory(model.rsm);
      inventoryConfig.setMatStatusConfigByType(ITransaction.TYPE_RECEIPT, msr);

      String transferDefaultStatus = StringUtil.trimCommas(model.tdf);
      String transferTempSenStatus = StringUtil.trimCommas(model.testm);
      MatStatusConfig mst = new MatStatusConfig();
      mst.setDf(transferDefaultStatus);
      mst.setEtsm(transferTempSenStatus);
      mst.setStatusMandatory(model.tsm);
      inventoryConfig.setMatStatusConfigByType(ITransaction.TYPE_TRANSFER, mst);

      String stockCountsDefaultStatus = StringUtil.trimCommas(model.pdf);
      String stockCountsTempSenStatus = StringUtil.trimCommas(model.pestm);
      MatStatusConfig msp = new MatStatusConfig();
      msp.setDf(stockCountsDefaultStatus);
      msp.setEtsm(stockCountsTempSenStatus);
      msp.setStatusMandatory(model.psm);
      inventoryConfig.setMatStatusConfigByType(ITransaction.TYPE_PHYSICALCOUNT, msp);

      String discardsDefaultStatus = StringUtil.trimCommas(model.wdf);
      String discardsTempSenStatus = StringUtil.trimCommas(model.westm);
      MatStatusConfig msw = new MatStatusConfig();
      msw.setDf(discardsDefaultStatus);
      msw.setEtsm(discardsTempSenStatus);
      msw.setStatusMandatory(model.wsm);
      inventoryConfig.setMatStatusConfigByType(ITransaction.TYPE_WASTAGE, msw);

      //Actual date of transaction configuration
      ActualTransConfig atci = new ActualTransConfig();
      model.catdi = model.catdi != null ? model.catdi : ActualTransConfig.ACTUAL_NONE;
      atci.setTy(model.catdi);
      inventoryConfig.setActualTransDateByType(ITransaction.TYPE_ISSUE, atci);

      ActualTransConfig atcr = new ActualTransConfig();
      model.catdr = model.catdr != null ? model.catdr : ActualTransConfig.ACTUAL_NONE;
      atcr.setTy(model.catdr);
      inventoryConfig.setActualTransDateByType(ITransaction.TYPE_RECEIPT, atcr);

      ActualTransConfig atcp = new ActualTransConfig();
      model.catdp = model.catdp != null ? model.catdp : ActualTransConfig.ACTUAL_NONE;
      atcp.setTy(model.catdp);
      inventoryConfig.setActualTransDateByType(ITransaction.TYPE_PHYSICALCOUNT, atcp);

      ActualTransConfig atcw = new ActualTransConfig();
      model.catdw = model.catdw != null ? model.catdw : ActualTransConfig.ACTUAL_NONE;
      atcw.setTy(model.catdw);
      inventoryConfig.setActualTransDateByType(ITransaction.TYPE_WASTAGE, atcw);

      ActualTransConfig atct = new ActualTransConfig();
      model.catdt = model.catdt != null ? model.catdt : ActualTransConfig.ACTUAL_NONE;
      atct.setTy(model.catdt);
      inventoryConfig.setActualTransDateByType(ITransaction.TYPE_TRANSFER, atct);

      if (et != null && !et.trim().isEmpty()) {
        List<String> times = StringUtil.getList(et.trim());
        List<String> utcTimes = null;
        if (times != null && !times.isEmpty()) {
          utcTimes = LocalDateUtil.convertTimeStringList(times, cc.dc.getTimezone(), true);
        }
        inventoryConfig.setTimes(utcTimes);
      } else {
        inventoryConfig.setTimes(null);
      }
      if (userId != null && !userId.isEmpty()) {
        inventoryConfig.setSourceUserId(userId);
      }
      //Get the parameters for manual consumption rates
      inventoryConfig.setConsumptionRate(Integer.parseInt(model.crc));
      if (String.valueOf(InventoryConfig.CR_MANUAL).equals(model.crc)) {
        inventoryConfig.setManualCRFreq(model.mcrfreq);
      }
      inventoryConfig.setDisplayCR(model.dispcr);
      if (model.dispcr) {
        inventoryConfig.setDisplayCRFreq(model.dcrfreq);
      }
      inventoryConfig.setShowPredictions(model.showpr);
      InventoryConfig.ManualTransConfig manualTransConfig = new InventoryConfig.ManualTransConfig();
      manualTransConfig.enableManualUploadInvDataAndTrans = model.emuidt;
      manualTransConfig.enableUploadPerEntityOnly = model.euse;
      inventoryConfig.setShowInventoryDashboard(model.eidb);
      if (model.eidb && model.enTgs != null && !model.enTgs.isEmpty()) {
        inventoryConfig.setEnTags(model.enTgs);
      }
      inventoryConfig.setManualTransConfig(manualTransConfig);
      if (model.etdx) {
        inventoryConfig.setEnabled(model.etdx);
        if (StringUtils.isNotEmpty(model.an)) {
          inventoryConfig.setExportUsers(model.an);
        }
        if (model.usrTgs != null && model.usrTgs.size() > 0) {
          inventoryConfig.setUserTags(model.usrTgs);
        }
      }
      //capture the actual date of transaction
           /* boolean atd = model.atd;
            if (atd) {
                inventoryConfig.putActualDateTrans(model.catd);
            }*/
      // Update permissions
      InventoryConfig.Permissions perms = new InventoryConfig.Permissions();
      perms.invCustomersVisible = model.ivc;
      inventoryConfig.setPermissions(perms);

      inventoryConfig.setMinMaxType(model.mmType);
      if (model.mmType == InventoryConfig.MIN_MAX_ABS_QTY) {
        inventoryConfig.setMinMaxDur(null);
        inventoryConfig.setMinMaxFreq(null);
      } else {
        inventoryConfig.setMinMaxDur(model.mmDur);
        inventoryConfig.setMinMaxFreq(model.mmFreq);
      }
      // Get the optimization config
      OptimizerConfig oc = cc.dc.getOptimizerConfig();
      String computeOption = model.co;
      int coption = OptimizerConfig.COMPUTE_NONE;
      if (computeOption != null && !computeOption.isEmpty()) {
        coption = Integer.parseInt(computeOption);
      }
      oc.setCompute(coption);
      oc.setComputeFrequency(model.crfreq);
      oc.setExcludeDiscards(model.edis);
      oc.setExcludeReasons(StringUtil.getCSV(model.ersns));
      if (String.valueOf(InventoryConfig.CR_AUTOMATIC).equals(model.crc)) {
        if (StringUtils.isNotEmpty(model.minhpccr)) {
          oc.setMinHistoricalPeriod(Float.parseFloat(model.minhpccr));
        }
        oc.setMaxHistoricalPeriod(Float.parseFloat(model.maxhpccr));
        // Get parameters for demand forecasting
        if (coption == OptimizerConfig.COMPUTE_FORECASTEDDEMAND) {
          String avgPeriodicity = model.aopfd;
          if (avgPeriodicity != null && !avgPeriodicity.isEmpty()) {
            try {
              oc.setMinAvgOrderPeriodicity(Float.parseFloat(avgPeriodicity));
            } catch (NumberFormatException e) {
              xLogger.warn("Invalid avg. periodicity for Demand Forecasting: {0}", avgPeriodicity);
            }
          }
          String numPeriods = model.nopfd;
          if (numPeriods != null && !numPeriods.isEmpty()) {
            try {
              oc.setNumPeriods(Float.parseFloat(numPeriods));
            } catch (NumberFormatException e) {
              xLogger
                  .warn("Invalid number of order periods for Demand Forecasting: {0}",
                      numPeriods);
            }
          }
        }
        // Get parameters for EOQ computation
        if (coption == OptimizerConfig.COMPUTE_EOQ) {
          oc.setInventoryModel(model.im);
          String avgPeriodicity = model.aopeoq;
          if (avgPeriodicity != null && !avgPeriodicity.isEmpty()) {
            try {
              oc.setMinAvgOrderPeriodicity(Float.parseFloat(avgPeriodicity));
            } catch (NumberFormatException e) {
              xLogger.warn("Invalid avg. periodicity for EOQ: {0}", avgPeriodicity);
            }
          }
          String numPeriods = model.nopeoq;
          if (numPeriods != null && !numPeriods.isEmpty()) {
            try {
              oc.setNumPeriods(Float.parseFloat(numPeriods));
            } catch (NumberFormatException e) {
              xLogger.warn("Invalid number of order periods for EOQ: {0}", numPeriods);
            }
          }
          String leadTime = model.lt;
          if (leadTime != null && !leadTime.isEmpty()) {
            try {
              oc.setLeadTimeDefault(Float.parseFloat(leadTime));
            } catch (NumberFormatException e) {
              xLogger.warn("Invalid lead time for EOQ: {0}", leadTime);
            }
          }
          InventoryConfigModel.LeadTimeAvgConfigModel leadTimeAvgConfigModel = model.ltacm;
          if (leadTimeAvgConfigModel != null) {
            LeadTimeAvgConfig leadTimeAvgConfig = oc.getLeadTimeAvgCfg();
            leadTimeAvgConfig.setMaxNumOfOrders(leadTimeAvgConfigModel.maxo);
            leadTimeAvgConfig.setMinNumOfOrders(leadTimeAvgConfigModel.mino);
            leadTimeAvgConfig.setMaxOrderPeriods(leadTimeAvgConfigModel.maxop);
            leadTimeAvgConfig.setExcludeOrderProcTime(leadTimeAvgConfigModel.exopt);
          }
        }
      }
      oc.setDisplayDF(model.ddf);
      oc.setDisplayOOQ(model.dooq);
      cc.dc.setOptimizerConfig(oc);
      cc.dc.setInventoryConfig(inventoryConfig);
      saveDomainConfig(sUser.getLocale(), domainId, cc, backendMessages);
      xLogger.info("AUDITLOG \t {0} \t {1} \t CONFIGURATION \t " +
          "UPDATE INVENTORY", domainId, sUser.getUsername());
      xLogger.info(cc.dc.toJSONSring());
    } catch (ServiceException | ObjectNotFoundException e) {
      xLogger.severe("Error in updating Inventory configuration");
      throw new InvalidServiceException(backendMessages.getString("inventory.config.update.error"));
    } catch (ConfigurationException e) {
      xLogger.severe("Error in updating Inventory configuration");
    }
    return backendMessages.getString("inventory.config.update.success");
  }

  @RequestMapping(value = "/orders", method = RequestMethod.GET)
  public
  @ResponseBody
  OrdersConfigModel getOrdersConfig(HttpServletRequest request) {
        /*if (!Authoriser.authoriseAdmin(request)) {
            throw new UnauthorizedException("Permission Denied");
        }*/
    SecureUserDetails sUser = SecurityMgr.getUserDetails(request.getSession());
    String userId = sUser.getUsername();
    Locale locale = sUser.getLocale();
    ResourceBundle backendMessages = Resources.get().getBundle("BackendMessages", locale);
    Long domainId = SessionMgr.getCurrentDomain(request.getSession(), userId);
    try {
      if (domainId == null) {
        xLogger.severe("Error in fetching Inventory configuration");
        throw new InvalidServiceException(
            backendMessages.getString("inventory.config.fetch.error"));
      }
      return builder.buildOrderConfigModel(request, domainId, locale, sUser.getTimezone());
    } catch (ConfigurationException | ServiceException | ObjectNotFoundException e) {
      xLogger.severe("Error in fetching Inventory configuration", e);
      throw new InvalidServiceException(backendMessages.getString("inventory.config.fetch.error"));
    }
  }

  @RequestMapping(value = "/orders", method = RequestMethod.POST)
  public
  @ResponseBody
  String updateOrdersConfig(@RequestBody OrdersConfigModel model, HttpServletRequest request) {
    SecureUserDetails sUser = SecurityMgr.getUserDetails(request.getSession());
    Locale locale = sUser.getLocale();
    ResourceBundle backendMessages = Resources.get().getBundle("BackendMessages", locale);
    if (!GenericAuthoriser.authoriseAdmin(request)) {
      throw new UnauthorizedException(backendMessages.getString("permission.denied"));
    }
    String userId = sUser.getUsername();
    Long domainId = SessionMgr.getCurrentDomain(request.getSession(), userId);
    try {
      if (domainId == null) {
        xLogger.severe("Error in updating Orders configuration");
        throw new InvalidServiceException(backendMessages.getString("orders.config.update.error"));
      }
      ConfigContainer cc = getDomainConfig(domainId, userId, sUser.getLocale());
      DemandBoardConfig dbc = cc.dc.getDemandBoardConfig();
      if (dbc == null) {
        dbc = new DemandBoardConfig();
        cc.dc.setDemandBoardConfig(dbc);
      }
      OrdersConfig oc = cc.dc.getOrdersConfig();
      if (oc == null) {
        oc = new OrdersConfig();
        cc.dc.setOrdersConfig(oc);
      }
      if (model == null) {
        xLogger.severe("Error in updating Orders configuration");
        throw new ConfigurationServiceException(
            backendMessages.getString("orders.config.update.error"));
      }
      cc.dc.addDomainData(ConfigConstants.ORDERS, generateUpdateList(userId));
      cc.dc.setOrderGeneration(model.og);
      cc.dc.setAutoGI(model.agi);
      cc.dc.setAutoGR(model.agr);
      cc.dc.setTransporterMandatory(model.tm);
      cc.dc.setTransporterInStatusSms(model.tiss);
      cc.dc.setAllowEmptyOrders(model.ao);
      cc.dc.setAllowMarkOrderAsFulfilled(model.aof);
      if (StringUtils.isNotBlank(model.po)) {
        cc.dc.setPaymentOptions(model.po);
      } else {
        cc.dc.setPaymentOptions(null);
      }
      if (StringUtils.isNotBlank(model.ps)) {
        cc.dc.setPackageSizes(model.ps);
      } else {
        cc.dc.setPackageSizes(null);
      }
      if (StringUtils.isNotBlank(model.vid)) {
        cc.dc.setVendorid(Long.valueOf(model.vid));
      } else {
        cc.dc.setVendorid(null);
      }
      cc.dc.setDisableOrdersPricing(model.dop);
      oc.setExportEnabled(model.eex);
      oc.setReasonsEnabled(model.enOrdRsn);
      oc.setOrderReason(model.enOrdRsn ? StringUtil.trimCSV(model.orsn) : null);
      if (model.enOrdRsn) {
        oc.setMandatory(model.md);
      }
      oc.setAllowMarkOrderAsConfirmed(model.aoc);
      oc.setAllocateStockOnConfirmation(model.asc);
      oc.setTransferRelease(model.tr);
      oc.setOrderRecommendationReasons(model.orr);
      oc.setOrderRecommendationReasonsMandatory(model.orrm);
      oc.setEditingQuantityReasons(model.eqr);
      oc.setEditingQuantityReasonsMandatory(model.eqrm);
      oc.setPartialShipmentReasons(model.psr);
      oc.setPartialShipmentReasonsMandatory(model.psrm);
      oc.setPartialFulfillmentReasons(model.pfr);
      oc.setPartialFulfillmentReasonsMandatory(model.pfrm);
      oc.setCancellingOrderReasons(model.cor);
      oc.setCancellingOrderReasonsMandatory(model.corm);
      //oc.setAllowCreatingShipments(model.acs);
      oc.setAutoAssignFirstMaterialStatusOnConfirmation(model.aafmsc);

      if (model.et != null && !model.et.trim().isEmpty()) {
        List<String> times = StringUtil.getList(model.et.trim());
        List<String> utcTimes = null;
        if (times != null && !times.isEmpty()) {
          utcTimes =
              LocalDateUtil.convertTimeStringList(times, cc.dc.getTimezone(),
                  true); // Convert from domain specific timezone to UTC
        }
        oc.setExportTimes(StringUtil.getCSV(utcTimes));
      } else {
        oc.setExportTimes(null);
      }
      oc.setExportUserIds(model.an);
      if (model.usrTgs != null && model.usrTgs.size() > 0) {
        oc.setUserTags(model.usrTgs);
      } else {
        oc.setUserTags(null);
      }
      oc.setSourceUserId(userId);
      if ("p".equalsIgnoreCase(model.ip)) {
        dbc.setIsPublic(true);
      } else if ("r".equalsIgnoreCase(model.ip)) {
        dbc.setIsPublic(false);
      }
      dbc.setBanner(model.bn);
      dbc.setHeading(model.hd);
      dbc.setCopyright(model.cp);
      dbc.setShowStock(model.spb);
      cc.dc.setDemandBoardConfig(dbc);
      cc.dc.setOrdersConfig(oc);
      saveDomainConfig(sUser.getLocale(), domainId, cc, backendMessages);
      xLogger.info("AUDITLOG \t {0} \t {1} \t CONFIGURATION \t " +
          "UPDATE ORDERS", domainId, sUser.getUsername());
      xLogger.info(cc.dc.toJSONSring());
    } catch (ServiceException | ConfigurationException e) {
      xLogger.severe("Error in updating Orders configuration", e);
      throw new InvalidServiceException(backendMessages.getString("orders.config.update.error"));
    }
    return backendMessages.getString("orders.config.update.success");
  }

  @RequestMapping(value = "/notifications", method = RequestMethod.POST)
  public
  @ResponseBody
  String updateNotificationsConfig(@RequestBody NotificationsConfigModel model,
                                   HttpServletRequest request) {
    SecureUserDetails sUser = SecurityMgr.getUserDetails(request.getSession());
    Locale locale = sUser.getLocale();
    ResourceBundle backendMessages = Resources.get().getBundle("BackendMessages", locale);
    if (!GenericAuthoriser.authoriseAdmin(request)) {
      throw new UnauthorizedException(backendMessages.getString("permission.denied"));
    }
    String userId = sUser.getUsername();
    Long domainId = SessionMgr.getCurrentDomain(request.getSession(), userId);
    try {
      if (domainId == null) {
        xLogger.severe("Error in updating Notification configuration");
        throw new InvalidServiceException(backendMessages.getString("notif.config.update.error"));
      }
      EventsConfig ec = getEventsConfig(model, domainId, backendMessages);
      ConfigContainer cc = getDomainConfig(domainId, userId, sUser.getLocale());
      try {
        xLogger.info("ec: {0}", ec.toJSONString());
      } catch (JSONException je) {
        // do nothing
      }
      cc.dc.setEventsConfig(ec);
      cc.dc.addDomainData(ConfigConstants.NOTIFICATIONS, generateUpdateList(userId));
      saveDomainConfig(sUser.getLocale(), domainId, cc, backendMessages);
      xLogger.info("AUDITLOG \t {0} \t {1} \t CONFIGURATION \t " +
          "UPDATE NOTIFICATIONS", domainId, sUser.getUsername());
      xLogger.info(cc.dc.toJSONSring());
    } catch (ServiceException | ConfigurationException e) {
      xLogger.severe("Error in updating Notification configuration", e);
      throw new InvalidServiceException(backendMessages.getString("notif.config.update.error"));
    }
    if (model.add) {
      return backendMessages.getString("notif.config.create.success");
    } else {
      return backendMessages.getString("notif.config.update.success");
    }
  }

  private EventsConfig getEventsConfig(NotificationsConfigModel model, Long domainId,
                                       ResourceBundle backendMessages) throws ServiceException {
    DomainConfig dc = DomainConfig.getInstance(domainId);
    EventsConfig ec = dc.getEventsConfig();
    String eventSpecJson = null;
    try {
      eventSpecJson = ec.toJSONString();
    } catch (JSONException ignored) {
      // do nothing
    }
    try {
      NotificationBuilder nb = new NotificationBuilder();
      String json = nb.buildModel(model, eventSpecJson);
      if (json == null || json.isEmpty()) {
        xLogger.severe("Error in updating Notification configuration");
        throw new ConfigurationServiceException(
            backendMessages.getString("notif.config.update.error"));
      }

      ec = new EventsConfig(json);
    } catch (JSONException e) {
      xLogger.severe("Error in updating Notification configuration", e);
      throw new ConfigurationServiceException(
          backendMessages.getString("notif.config.update.error"));
    }
    return ec;
  }

  @RequestMapping(value = "/notifications/fetch", method = RequestMethod.GET)
  public
  @ResponseBody
  NotificationsModel getNotificationsConfig(
      @RequestParam String t, HttpServletRequest request) {
        /*if (!Authoriser.authoriseAdmin(request)) {
            throw new UnauthorizedException("Permission Denied");
        }*/
    SecureUserDetails sUser = SecurityMgr.getUserDetails(request.getSession());
    Locale locale = sUser.getLocale();
    ResourceBundle backendMessages = Resources.get().getBundle("BackendMessages", locale);
    String userId = sUser.getUsername();
    Long domainId = SessionMgr.getCurrentDomain(request.getSession(), userId);
    DomainConfig dc = DomainConfig.getInstance(domainId);
    if (dc == null) {
      xLogger.severe("Error in fetching Notification configuration");
      throw new ConfigurationServiceException(
          backendMessages.getString("notif.config.fetch.error"));
    }
    NotificationBuilder nb = new NotificationBuilder();
    try {
      EventsConfig ec = dc.getEventsConfig();
      if (ec == null) {
        xLogger.severe("Error in fetching Notification configuration");
        throw new ConfigurationServiceException(
            backendMessages.getString("notif.config.fetch.error"));
      }
      return nb.buildNotifConfigModel(ec.toJSONString(), t, domainId, locale, sUser.getTimezone());
    } catch (ServiceException | JSONException | ObjectNotFoundException e) {
      xLogger.severe("Error in fetching Notification configuration", e);
      throw new ConfigurationServiceException(
          backendMessages.getString("notif.config.fetch.error"));
    }
  }

  @RequestMapping(value = "/notifications/delete", method = RequestMethod.POST)
  public
  @ResponseBody
  String deleteNotification(@RequestBody NotificationsConfigModel model,
                            HttpServletRequest request) {
    SecureUserDetails sUser = SecurityMgr.getUserDetails(request.getSession());
    Locale locale = sUser.getLocale();
    ResourceBundle backendMessages = Resources.get().getBundle("BackendMessages", locale);
    if (!GenericAuthoriser.authoriseAdmin(request)) {
      throw new UnauthorizedException(backendMessages.getString("permission.denied"));
    }
    String userId = sUser.getUsername();
    Long domainId = SessionMgr.getCurrentDomain(request.getSession(), userId);
    String key = IConfig.CONFIG_PREFIX + domainId;
    IConfig c;
    DomainConfig dc;
    EventsConfig ec;
    String eventSpecJson = null;
    boolean delete = false;
    if (domainId == null) {
      xLogger.severe("Error in deleting Notification");
      throw new InvalidServiceException(backendMessages.getString("notif.delete.error"));
    }
    try {
      ConfigurationMgmtService
          cms =
          Services.getService(ConfigurationMgmtServiceImpl.class, locale);
      try {
        c = cms.getConfiguration(key);
        dc = new DomainConfig(c.getConfig());
      } catch (ObjectNotFoundException e) {
        dc = new DomainConfig();
        c = JDOUtils.createInstance(IConfig.class);
        c.setKey(key);
        c.setUserId(userId);
        c.setDomainId(domainId);
        c.setLastUpdated(new Date());
        delete = true;
      } catch (ConfigurationException e) {
        xLogger.severe("Error in deleting Notification", e);
        throw new InvalidServiceException(backendMessages.getString("notif.delete.error"));
      }
      try {
        dc = DomainConfig.getInstance(domainId);
        ec = dc.getEventsConfig();
        eventSpecJson = ec.toJSONString();
      } catch (JSONException e) {
        xLogger.severe("Error in deleting Notification", e);
      }
      NotificationBuilder nb = new NotificationBuilder();
      String json = nb.deleteModel(model, eventSpecJson);

      if (json == null || json.isEmpty()) {
        xLogger.severe("Error in deleting Notification");
        throw new InvalidServiceException(backendMessages.getString("notif.delete.error"));
      }
      try {
        ec = new EventsConfig(json);
      } catch (JSONException e) {
        xLogger.severe("Error in deleting Notification", e);
        throw new InvalidServiceException(backendMessages.getString("notif.delete.error"));
      }
      dc.setEventsConfig(ec);
      ConfigContainer cc = new ConfigContainer();
      cc.add = delete;
      cc.c = c;
      cc.dc = dc;
      saveDomainConfig(sUser.getLocale(), domainId, cc, backendMessages);
      xLogger.info("AUDITLOG \t {0} \t {1} \t CONFIGURATION \t " +
          "DELETE NOTIFICATION", domainId, sUser.getUsername());
    } catch (ServiceException e) {
      xLogger.severe("Error in deleting Notification", e);
      throw new InvalidServiceException(backendMessages.getString("notif.delete.error"));
    }
    return backendMessages.getString("notif.delete.success");
  }

  @RequestMapping(value = "/notifcations/messages", method = RequestMethod.GET)
  public
  @ResponseBody
  Results getUsersMessageStatus(
      @RequestParam(defaultValue = PageParams.DEFAULT_OFFSET_STR) int offset,
      @RequestParam(defaultValue = PageParams.DEFAULT_SIZE_STR) int size,
      @RequestParam(required = false, defaultValue = "") String start,
      @RequestParam(required = false, defaultValue = "") String end,
      HttpServletRequest request) {

    SecureUserDetails sUser = SecurityMgr.getUserDetails(request.getSession());
    String userId = sUser.getUsername();
    Long domainId = SessionMgr.getCurrentDomain(request.getSession(), userId);
    DomainConfig dc = DomainConfig.getInstance(domainId);
    Navigator
        navigator =
        new Navigator(request.getSession(), "UsersController.getUsersMessageStatus", offset, size,
            "dummy", 0);
    PageParams pageParams = new PageParams(navigator.getCursor(offset), offset, size);
    Results results;
    Locale locale = sUser.getLocale();
    ResourceBundle backendMessages = Resources.get().getBundle("BackendMessages", locale);
    if (!GenericAuthoriser.authoriseAdmin(request)) {
      throw new UnauthorizedException(backendMessages.getString("permission.denied"));
        }
        try {
          Date startDate = null, endDate = null;
          if (StringUtils.isNotEmpty(start)) {
            try {
              startDate = LocalDateUtil.parseCustom(start, Constants.DATE_FORMAT, dc.getTimezone());
            } catch (Exception e) {
              xLogger.warn("Exception when parsing start date " + start, e);
            }
          }
          if (StringUtils.isNotEmpty(end)) {
            try {
              endDate = LocalDateUtil.parseCustom(end, Constants.DATE_FORMAT, dc.getTimezone());
            } catch (Exception e) {
              xLogger.warn("Exception when parsing start date " + end, e);
            }
          }

          results = MessageUtil.getNotifactionLogs(domainId, startDate, endDate, pageParams);
          navigator.setResultParams(results);
        } catch (MessageHandlingException e) {
          xLogger.warn("Error in building message status", e);
          throw new InvalidServiceException(
              backendMessages.getString("message.status.build.error"));
        }
    String timezone = sUser.getTimezone();
    UsersService as;
    as = Services.getService(UsersServiceImpl.class, locale);
    int no = offset;
    List<UserMessageModel> userMessageStatus = new ArrayList<UserMessageModel>();
    for (Object res : results.getResults()) {
      IMessageLog ml = (IMessageLog) res;
      try {
        userMessageStatus
            .add(messageBuilder
                .buildUserMessageModel(ml, as, locale, userId, ++no, timezone));
      } catch (Exception e) {
        xLogger.warn("Error in building message status", e);
      }
    }
    return new Results(userMessageStatus, results.getCursor(), -1, offset);
  }

  @RequestMapping(value = "/bulletinboard", method = RequestMethod.POST)
  public
  @ResponseBody
  String updateBulletinBoardConfig(@RequestBody BulletinBoardConfigModel model,
                                   HttpServletRequest request) {
    SecureUserDetails sUser = SecurityMgr.getUserDetails(request.getSession());
    Locale locale = sUser.getLocale();
    ResourceBundle backendMessages = Resources.get().getBundle("BackendMessages", locale);
    if (!GenericAuthoriser.authoriseAdmin(request)) {
      throw new UnauthorizedException(backendMessages.getString("permission.denied"));
    }
    if (model == null) {
      xLogger.severe("Error in updating bulletin board configuration");
      throw new BadRequestException(backendMessages.getString("bulletin.config.update.error"));
    }
    String userId = sUser.getUsername();
    Long domainId = SessionMgr.getCurrentDomain(request.getSession(), userId);
    if (domainId == null) {
      xLogger.severe("Error in updating bulletin board configuration");
      throw new InvalidServiceException(backendMessages.getString("bulletin.config.update.error"));
    }
    try {
      ConfigContainer cc = getDomainConfig(domainId, userId, sUser.getLocale());
      cc.dc.addDomainData(ConfigConstants.BULLETIN_BOARD, generateUpdateList(userId));
      BulletinBoardBuilder boardBuilder = new BulletinBoardBuilder();
      BBoardConfig bbc = boardBuilder.buildBBoardConfig(model);
      if (bbc != null) {
        cc.dc.setBBoardConfig(bbc);
      }
      saveDomainConfig(sUser.getLocale(), domainId, cc, backendMessages);
      xLogger.info("AUDITLOG \t {0} \t {1} \t CONFIGURATION \t " +
          "UPDATE BULLETINBOARD", domainId, sUser.getUsername());
      xLogger.info(cc.dc.toJSONSring());
    } catch (ServiceException | ConfigurationException e) {
      xLogger.severe("Error in updating bulletin board configuration", e);
      throw new InvalidServiceException(backendMessages.getString("bulletin.config.update.error"));
    }
    return backendMessages.getString("bulletin.config.update.success");
  }

  @RequestMapping(value = "/bulletinboard", method = RequestMethod.GET)
  public
  @ResponseBody
  BulletinBoardConfigModel getBulletinBoardConfig(HttpServletRequest request) {
//        if (Authoriser.authoriseAdmin(request)) {
    SecureUserDetails sUser = SecurityMgr.getUserDetails(request.getSession());
    Locale locale = sUser.getLocale();
    ResourceBundle backendMessages = Resources.get().getBundle("BackendMessages", locale);
    String userId = sUser.getUsername();
    Long domainId = SessionMgr.getCurrentDomain(request.getSession(), userId);
    DomainConfig dc = DomainConfig.getInstance(domainId);
    BulletinBoardBuilder boardBuilder = new BulletinBoardBuilder();
    if (dc == null) {
      xLogger.severe("Error in fetching bulletin board configuration");
      throw new ConfigurationServiceException(
          backendMessages.getString("bulletin.config.fetch.error"));
    }
    BBoardConfig config = dc.getBBoardConfig();
    BulletinBoardConfigModel model = new BulletinBoardConfigModel();
    if (config != null) {
      model = boardBuilder.buildModel(config);
    }
//            model.ien = dc.isBBoardEnabled();
    model.did = Long.toString(domainId);
    List<String> val = dc.getDomainData(ConfigConstants.BULLETIN_BOARD);
    if (val != null) {
      model.createdBy = val.get(0);
      model.lastUpdated =
          LocalDateUtil.format(new Date(Long.parseLong(val.get(1))), locale, sUser.getTimezone());
      try {
        UsersService as = Services.getService(UsersServiceImpl.class, locale);
        model.fn = String.valueOf(as.getUserAccount(model.createdBy).getFullName());
      } catch (ObjectNotFoundException e) {
        //Ignore.. Users should still be able to edit config
      }
    }
    return model;
  }

  @RequestMapping(value = "/posttoboard", method = RequestMethod.POST)
  public
  @ResponseBody
  String setMessageToBoard(@RequestBody String msg, HttpServletRequest request) {
    SecureUserDetails sUser = SecurityMgr.getUserDetails(request.getSession());
    Locale locale = sUser.getLocale();
    ResourceBundle backendMessages = Resources.get().getBundle("BackendMessages", locale);
    if (!GenericAuthoriser.authoriseAdmin(request)) {
      throw new UnauthorizedException(backendMessages.getString("permission.denied"));
    }
    String userId = sUser.getUsername();
    Long domainId = SessionMgr.getCurrentDomain(request.getSession(), userId);
    if (domainId == null) {
      xLogger.severe("Error in posting message to board");
      throw new InvalidServiceException(backendMessages.getString("message.post.error"));
    }
    IBBoard board = JDOUtils.createInstance(IBBoard.class);
    board.setDomainId(domainId);
    board.setUserId(userId);
    board.setTimestamp(new Date());
    board.setType(IBBoard.TYPE_POST);
    if (StringUtils.isNotBlank(msg)) {
      board.setMessage(msg);
    }
    BBHandler.add(board);
    xLogger.info("AUDITLOG \t {0} \t {1} \t CONFIGURATION \t " +
        "SET MESSAGE BOARD", domainId, sUser.getUsername());
    return backendMessages.getString("message.post.success");
  }

  @RequestMapping(value = "/accesslogs", method = RequestMethod.GET)
  public
  @ResponseBody
  Results getAccessLogs(@RequestParam(required = false) String duration,
                        @RequestParam(defaultValue = "1") int o,
                        @RequestParam(defaultValue = PageParams.DEFAULT_SIZE_STR) int s,
                        HttpServletRequest request) {
    SecureUserDetails sUser = SecurityMgr.getUserDetails(request.getSession());
    Locale locale = sUser.getLocale();
    ResourceBundle backendMessages = Resources.get().getBundle("BackendMessages", locale);
    if (!GenericAuthoriser.authoriseAdmin(request)) {
      throw new UnauthorizedException(backendMessages.getString("permission.denied"));
    }
    String userId = sUser.getUsername();
    Long domainId = SessionMgr.getCurrentDomain(request.getSession(), userId);
    if (domainId == null) {
      xLogger.severe("Error in fetching Access log");
      throw new InvalidServiceException(backendMessages.getString("accesslog.fetch.error"));
    }
    Date start = null;
    if (duration != null && !duration.isEmpty()) {
      Calendar cal = LocalDateUtil.getZeroTime(DomainConfig.getInstance(domainId).getTimezone());
      cal.add(Calendar.DATE, -1 * Integer.parseInt(duration));
      start = cal.getTime();
    }
    Navigator
        navigator =
        new Navigator(request.getSession(), "DomainConfigController.getAccessLogs", o, s, "dummy",
            0);
    PageParams pageParams = new PageParams(navigator.getCursor(o), o, s);
    Results results = XLog.getRequestLogs(domainId, IALog.TYPE_BBOARD, start, pageParams);
    navigator.setResultParams(results);
    if (results == null || results.getResults() == null) {
      return null;
    }
    List<IALog> logs = results.getResults();
    List<AccessLogModel> models = new ArrayList<AccessLogModel>();
    for (IALog log : logs) {
      AccessLogModel model = new AccessLogModel();
      if (log != null) {
        if (StringUtils.isNotEmpty(Long.toString(log.getKey()))) {
          model.key = Long.toString(log.getKey());
        }
        if (StringUtils.isNotEmpty(Long.toString(log.getDomainId()))) {
          model.did = Long.toString(log.getDomainId());
        }
        if (StringUtils.isNotEmpty(log.getIPAddress())) {
          model.ip = log.getIPAddress();
        }
        if (StringUtils.isNotEmpty(log.getTimestamp().toString())) {
          model.t = log.getTimestamp().toString();
        }
        if (StringUtils.isNotEmpty(log.getType())) {
          model.type = log.getType();
        }
        if (StringUtils.isNotEmpty(log.getUserAgent())) {
          model.ua = log.getUserAgent();
        }
      }
      models.add(model);
    }
    return new Results(models, "accesslog", -1, o);
  }

  @RequestMapping(value = "/customreports", method = RequestMethod.GET)
  public
  @ResponseBody
  String upload(HttpServletRequest request) {
    SecureUserDetails sUser = SecurityMgr.getUserDetails(request.getSession());
    Locale locale = sUser.getLocale();
    ResourceBundle backendMessages = Resources.get().getBundle("BackendMessages", locale);
    if (!GenericAuthoriser.authoriseAdmin(request)) {
      throw new UnauthorizedException(backendMessages.getString("permission.denied"));
    }
    BlobstoreService blobstoreService = AppFactory.get().getBlobstoreService();
    String fullURL = request.getServletPath() + request.getPathInfo();
    if (StringUtils.isNotEmpty(fullURL)) {
      return blobstoreService.createUploadUrl(fullURL);
    }
    return null;
  }

  @RequestMapping(value = "/customreports", method = RequestMethod.POST)
  public
  @ResponseBody
  CustomReportsConfig.Config getConfig(@RequestParam String templateName, @RequestParam String edit,
                                       @RequestParam String templateKey,
                                       HttpServletRequest request) {
        /*if (!Authoriser.authoriseAdmin(request)) {
            throw new UnauthorizedException("Permission Denied");
        }*/
    SecureUserDetails sUser = SecurityMgr.getUserDetails(request.getSession());
    Locale locale = sUser.getLocale();
    ResourceBundle backendMessages = Resources.get().getBundle("BackendMessages", locale);
    String userId = sUser.getUsername();
    Long domainId = SessionMgr.getCurrentDomain(request.getSession(), userId);
    if (StringUtils.isEmpty(templateName)) {
      xLogger.severe("Error in fetching configuration");
      throw new BadRequestException(backendMessages.getString("config.fetch.error"));
    }
    try {
      if (edit.equalsIgnoreCase("true")) {
        crBuilder.removeUploadedObject(templateKey);
      }
      IUploaded
          uploaded =
          crBuilder.updateUploadedObject(request, sUser, domainId,
              AppFactory.get().getBlobstoreService(), templateName);
      CustomReportsConfig.Config config = new CustomReportsConfig.Config();
      if (uploaded != null) {
        config.fileName = uploaded.getFileName();
        config.templateKey = uploaded.getId();
      }
      return config;
    } catch (ServiceException e) {
      xLogger.severe("Error in fetching configuration", e);
      throw new InvalidServiceException(backendMessages.getString("config.fetch.error"));
    }
  }

  @RequestMapping(value = "/customreport/add", method = RequestMethod.POST)
  public
  @ResponseBody
  String setCustomReports(@RequestBody AddCustomReportRequestObj addCustomReportRequestObj,
                          HttpServletRequest request) {
    SecureUserDetails sUser = SecurityMgr.getUserDetails(request.getSession());
    Locale locale = sUser.getLocale();
    ResourceBundle backendMessages = Resources.get().getBundle("BackendMessages", locale);
    if (!GenericAuthoriser.authoriseAdmin(request)) {
      throw new UnauthorizedException(backendMessages.getString("permission.denied"));
    }
    if (addCustomReportRequestObj == null || addCustomReportRequestObj.customReport == null
        || addCustomReportRequestObj.config == null) {
      throw new BadRequestException(backendMessages.getString("customreports.update.error"));
    }
    String userId = sUser.getUsername();
    Long domainId = SessionMgr.getCurrentDomain(request.getSession(), userId);
    if (domainId == null) {
      xLogger.severe("Error in updating Custom Reports");
      throw new InvalidServiceException(backendMessages.getString("customreports.update.error"));
    }
    try {
      ConfigContainer cc = getDomainConfig(domainId, userId, sUser.getLocale());
      CustomReportsConfig crc = cc.dc.getCustomReportsConfig();
      if (!addCustomReportRequestObj.customReport.tk
          .equalsIgnoreCase(addCustomReportRequestObj.config.templateKey)) {
        crc.removeConfig(addCustomReportRequestObj.customReport.tn);
      }
      if (StringUtils.isNotEmpty(addCustomReportRequestObj.customReport.origname)) {
        crc.removeConfig(addCustomReportRequestObj.customReport.origname);
      }
      CustomReportsConfig.Config
          config =
          crBuilder.populateConfig(addCustomReportRequestObj.customReport,
              addCustomReportRequestObj.config, true, cc.dc.getTimezone());
      crc.getCustomReportsConfig().add(config);
      cc.dc.setCustomReportsConfig(crc);
      cc.dc.addDomainData(ConfigConstants.CUSTOM_REPORTS, generateUpdateList(userId));
      saveDomainConfig(sUser.getLocale(), domainId, cc, backendMessages);
      xLogger.info("AUDITLOG \t {0} \t {1} \t CONFIGURATION \t " +
          "ADD CUSTOM REPORTS", domainId, sUser.getUsername());
      xLogger.info(cc.dc.toJSONSring());
    } catch (ServiceException | ConfigurationException e) {
      xLogger.severe("Error in updating Custom Reports", e);
      throw new InvalidServiceException(backendMessages.getString("customreports.update.error"));
    }
    return backendMessages.getString("customreports.update.success");
  }

  @RequestMapping(value = "/customreport", method = RequestMethod.GET)
  public
  @ResponseBody
  NotificationsModel getCustomReports(HttpServletRequest request) {
        /*if (!Authoriser.authoriseAdmin(request)) {
            throw new UnauthorizedException("Permission Denied");
        }*/
    SecureUserDetails sUser = SecurityMgr.getUserDetails(request.getSession());
    Locale locale = sUser.getLocale();
    ResourceBundle backendMessages = Resources.get().getBundle("BackendMessages", locale);
    String userId = sUser.getUsername();
    Long domainId = SessionMgr.getCurrentDomain(request.getSession(), userId);
    if (domainId == null) {
      xLogger.severe("Error in fetching list of Custom Reports");
      throw new InvalidServiceException(
          backendMessages.getString("customreports.list.fetch.error"));
    }
    DomainConfig dc = DomainConfig.getInstance(domainId);
    CustomReportsConfig crc = dc.getCustomReportsConfig();
    if (crc == null || crc.getCustomReportsConfig() == null) {
      xLogger.severe("Error in fetching list of Custom Reports");
      throw new ConfigurationServiceException(
          backendMessages.getString("customreports.list.fetch.error"));
    }
    try {
      return crBuilder
          .populateCustomReportModelsList(crc.getCustomReportsConfig(), locale, domainId,
              sUser.getTimezone(), true);
    } catch (ServiceException | ObjectNotFoundException e) {
      xLogger.severe("Error in fetching list of Custom Reports", e);
      throw new InvalidServiceException("Error in fetching list of Custom Reports");
    }
  }

  @RequestMapping(value = "/customreport/delete", method = RequestMethod.POST)
  public
  @ResponseBody
  String deleteCustomReport(@RequestBody String name, HttpServletRequest request) {
    SecureUserDetails sUser = SecurityMgr.getUserDetails(request.getSession());
    Locale locale = sUser.getLocale();
    ResourceBundle backendMessages = Resources.get().getBundle("BackendMessages", locale);
    if (!GenericAuthoriser.authoriseAdmin(request)) {
      throw new UnauthorizedException(backendMessages.getString("permission.denied"));
    }
    String userId = sUser.getUsername();
    Long domainId = SessionMgr.getCurrentDomain(request.getSession(), userId);
    if (domainId == null) {
      xLogger.severe("Error in deleting Custom Reports");
      throw new InvalidServiceException(backendMessages.getString("customreports.delete.error"));
    }
    try {
      ConfigurationMgmtService
          cms =
          Services.getService(ConfigurationMgmtServiceImpl.class, locale);
      DomainConfig dc = DomainConfig.getInstance(domainId);
      if (dc == null || dc.getCustomReportsConfig() == null) {
        xLogger.severe(
            "Error in deleting Custom Reports since domain config is null, domain: {0} name: {1}",
            domainId, name);
        throw new InvalidServiceException(backendMessages.getString("customreports.delete.error"));
      }
      CustomReportsConfig crc = dc.getCustomReportsConfig();
      CustomReportsConfig.Config config = crc.getConfig(name);
      if (config == null) {
        xLogger
            .severe(
                "Error in deleting custom reports since config is null, domain: {0} name: {1}",
                domainId, name);
        throw new InvalidServiceException(backendMessages.getString("customreports.delete.error"));
      }
      crc.removeConfig(name);
      if (StringUtils.isEmpty(config.templateKey) || !crBuilder
          .removeUploadedObject(config.templateKey)) {
        xLogger.warn(
            "Unable to delete the uploaded template for custom report: {0} name: {1} and domain: {2}",
            config.templateKey, name, domainId);
      }
      dc.setCustomReportsConfig(crc);
      String key = IConfig.CONFIG_PREFIX + domainId;
      ConfigContainer cc = new ConfigContainer();
      cc.add = false;
      cc.c = cms.getConfiguration(key);
      cc.dc = dc;
      cc.dc.addDomainData(ConfigConstants.CUSTOM_REPORTS, generateUpdateList(userId));
      saveDomainConfig(sUser.getLocale(), domainId, cc, backendMessages);
      xLogger.info("AUDITLOG \t {0} \t {1} \t CONFIGURATION \t " +
          "DELETE CUSTOM REPORTS", domainId, sUser.getUsername());
    } catch (ServiceException | ObjectNotFoundException e) {
      xLogger.severe("Error in deleting Custom Reports, domain: {0} name: {1}", domainId, name, e);
      throw new InvalidServiceException(backendMessages.getString("customreports.delete.error"));
    }
    return backendMessages.getString("customreports.delete.success");
  }

  @RequestMapping(value = "/customreport/fetch", method = RequestMethod.GET)
  public
  @ResponseBody
  NotificationsModel getCustomReport(@RequestParam String n, HttpServletRequest request) {
        /*if (!Authoriser.authoriseAdmin(request)) {
            throw new UnauthorizedException("Permission Denied");
        }*/
    SecureUserDetails sUser = SecurityMgr.getUserDetails(request.getSession());
    Locale locale = sUser.getLocale();
    ResourceBundle backendMessages = Resources.get().getBundle("BackendMessages", locale);
    String userId = sUser.getUsername();
    Long domainId = SessionMgr.getCurrentDomain(request.getSession(), userId);
    if (domainId == null) {
      xLogger.severe("Error in fetching Custom Report");
      throw new InvalidServiceException(backendMessages.getString("customreports.fetch.error"));
    }
    DomainConfig dc = DomainConfig.getInstance(domainId);
    CustomReportsConfig crc = dc.getCustomReportsConfig();
    CustomReportsConfig.Config config = crc.getConfig(n);
    if (config == null) {
      xLogger.severe("Error in fetching Custom Report");
      throw new ConfigurationServiceException(
          backendMessages.getString("customreports.fetch.error"));
    }
    try {
      UsersService as = Services.getService(UsersServiceImpl.class, locale);
      NotificationsModel
          model =
          crBuilder
              .populateCustomReportModelsList(Collections.singletonList(config), locale,
                  domainId,
                  sUser.getTimezone(), false);
      if (model.config instanceof CustomReportsConfigModel) {
        CustomReportsConfigModel m = (CustomReportsConfigModel) model.config;
        m.mn =
            userBuilder.buildUserModels(constructUserAccount(as, config.managers), locale,
                sUser.getTimezone(), true);
        m.an =
            userBuilder.buildUserModels(constructUserAccount(as, config.users), locale,
                sUser.getTimezone(), true);
        m.sn =
            userBuilder.buildUserModels(constructUserAccount(as, config.superUsers), locale,
                sUser.getTimezone(), true);
        m.exusrs = config.extUsers;
        m.usrTgs = config.usrTgs;
      }
      return model;
    } catch (ServiceException | ObjectNotFoundException e) {
      throw new InvalidServiceException("Ero");
    }

  }

  private List<IUserAccount> constructUserAccount(UsersService as, List<String> userIds) {
    if (userIds != null && userIds.size() > 0) {
      List<IUserAccount> list = new ArrayList<>(userIds.size());
      for (String userId : userIds) {
        try {
          list.add(as.getUserAccount(userId));
        } catch (Exception ignored) {
          // do nothing
        }
      }
      return list;
    }
    return new ArrayList<>();
  }

  @RequestMapping(value = "/customreport/update", method = RequestMethod.POST)
  public
  @ResponseBody
  String updateCustomReport(@RequestBody CustomReportsConfigModel model,
                            HttpServletRequest request) {
    SecureUserDetails sUser = SecurityMgr.getUserDetails(request.getSession());
    Locale locale = sUser.getLocale();
    ResourceBundle backendMessages = Resources.get().getBundle("BackendMessages", locale);
    if (!GenericAuthoriser.authoriseAdmin(request)) {
      throw new UnauthorizedException(backendMessages.getString("permission.denied"));
    }
    if (model == null) {
      throw new BadRequestException("Error in updating Custom Report");
    }
    String userId = sUser.getUsername();
    Long domainId = SessionMgr.getCurrentDomain(request.getSession(), userId);
    if (domainId == null) {
      xLogger.severe("Error in updating Custom Report");
      throw new BadRequestException(backendMessages.getString("customreport.update.error"));
    }
    try {
      ConfigContainer cc = getDomainConfig(domainId, userId, sUser.getLocale());
      CustomReportsConfig.Config config = new CustomReportsConfig.Config();
      config = crBuilder.populateConfig(model, config, false, cc.dc.getTimezone());
      CustomReportsConfig crc = cc.dc.getCustomReportsConfig();
      crc.removeConfig(model.origname);
      crc.getCustomReportsConfig().add(config);
      cc.dc.setCustomReportsConfig(crc);
      cc.dc.addDomainData(ConfigConstants.CUSTOM_REPORTS, generateUpdateList(userId));
      saveDomainConfig(sUser.getLocale(), domainId, cc, backendMessages);
      xLogger.info("AUDITLOG \t {0} \t {1} \t CONFIGURATION \t " +
          "UPDATE CUSTOM REPORTS", domainId, sUser.getUsername());
      xLogger.info(cc.dc.toJSONSring());
    } catch (ServiceException e) {
      xLogger.severe("Error in updating Custom Report", e);
      throw new InvalidServiceException(backendMessages.getString("customreport.update.error"));
    } catch (ConfigurationException e) {
      e.printStackTrace();
    }
    return "Custom Report updated successfully";
  }

  @RequestMapping(value = "/customreport/export", method = RequestMethod.POST)
  public
  @ResponseBody
  String exportReport(@RequestBody String name, HttpServletRequest request) {
    SecureUserDetails sUser = SecurityMgr.getUserDetails(request.getSession());
    Locale locale = sUser.getLocale();
    ResourceBundle backendMessages = Resources.get().getBundle("BackendMessages", locale);
    String userId = sUser.getUsername();
    Long domainId = SessionMgr.getCurrentDomain(request.getSession(), userId);
    if (domainId == null) {
      throw new InvalidServiceException(backendMessages.getString("report.export.error"));
    }
    Map<String, String> params = new HashMap<>();
    params.put("action", "scheduleexport");
    params.put("domainid", domainId.toString());
    params.put("reportname", name);
    Map<String, String> headers = new HashMap<>();
    headers.put("Host", AppFactory.get().getBackendService().getBackendAddress(Constants.BACKEND1));
    Long
        jobId =
        JobUtil.createJob(domainId, sUser.getUsername(), null, IJobStatus.TYPE_CUSTOMREPORT, name,
            params);
    if (jobId != null) {
      params.put("jobid", jobId.toString());
    }

    try {
      AppFactory.get().getTaskService()
          .schedule(ITaskService.QUEUE_EXPORTER, "/task/customreportsexport", params, headers,
              ITaskService.METHOD_POST);
    } catch (TaskSchedulingException e) {
      xLogger.severe("{0} when scheduling task for custom report export of {1}: {2}",
          e.getClass().getName(), name, e.getMessage());
      throw new InvalidTaskException(
          e.getClass().getName() + " " + backendMessages
              .getString("customreports.schedule.export")
              + " " + name);
    }
    if (jobId != null) {
      return String.valueOf(jobId);
    }
    return backendMessages.getString("report.export.success");
  }

  @RequestMapping(value = "/report/filters", method = RequestMethod.GET)
  public
  @ResponseBody
  Map<String, List<String>> getReportFilters(HttpServletRequest request) {
    SecureUserDetails sUser = SecurityMgr.getUserDetails(request.getSession());
    String userId = sUser.getUsername();
    Long domainId = SessionMgr.getCurrentDomain(request.getSession(), userId);
    ReportsConfig rConfig = ReportsConfig.getInstance(domainId);
    Map<String, List<String>> filters = new HashMap<String, List<String>>(6);
    filters.put(ReportsConstants.FILTER_COUNTRY,
        rConfig.getFilterValues(ReportsConstants.FILTER_COUNTRY));
    filters
        .put(ReportsConstants.FILTER_STATE, rConfig.getFilterValues(ReportsConstants.FILTER_STATE));
    filters.put(ReportsConstants.FILTER_DISTRICT,
        rConfig.getFilterValues(ReportsConstants.FILTER_DISTRICT));
    filters
        .put(ReportsConstants.FILTER_TALUK, rConfig.getFilterValues(ReportsConstants.FILTER_TALUK));
    filters
        .put(ReportsConstants.FILTER_CITY, rConfig.getFilterValues(ReportsConstants.FILTER_CITY));
    filters.put(ReportsConstants.FILTER_PINCODE,
        rConfig.getFilterValues(ReportsConstants.FILTER_PINCODE));
    return filters;
  }

  @RequestMapping(value = "/domains/all", method = RequestMethod.GET)
  public
  @ResponseBody
  Results getDomainsAsResult(
      @RequestParam(required = false, defaultValue = PageParams.DEFAULT_SIZE_STR) String s,
      @RequestParam(required = false, defaultValue = PageParams.DEFAULT_OFFSET_STR) String o,
      @RequestParam(required = false) String q,
      HttpServletRequest request) {
    PersistenceManager pm = PMF.get().getPersistenceManager();
    Query query = pm.newQuery(JDOUtils.getImplClass(IDomain.class));
    Map<String, Object> params = new HashMap<>();
    Navigator navigator;
    String filter;
    if (q != null && !q.isEmpty()) {
      filter = "nNm.startsWith(txtParam)";
      query.setFilter(filter);
      query.declareParameters("String txtParam");
      params.put("txtParam", q.toLowerCase());
    } else {
      filter = "hasParent==false";
      query.setFilter(filter);
      query.setOrdering("nNm asc");
    }
    if (o != null) {
      int off = Integer.parseInt(o);
      int sz = Integer.parseInt(s);
      navigator =
          new Navigator(request.getSession(), "DomainConfigController.getDomains", off, sz, "dummy",
              0);
      PageParams pageParams = new PageParams(navigator.getCursor(off), off, sz);
      QueryUtil.setPageParams(query, pageParams);
    }
    List<IDomain> domains = null;
    SecureUserDetails sUser = SecurityMgr.getUserDetails(request.getSession());
    Locale locale = sUser.getLocale();
    ResourceBundle backendMessages = Resources.get().getBundle("BackendMessages", locale);
    try {
      if (q != null && !q.isEmpty()) {
        domains = (List<IDomain>) query.executeWithMap(params);
      } else {
        domains = (List<IDomain>) query.execute();
      }
      domains = (List<IDomain>) pm.detachCopyAll(domains);
      if (domains != null) {
        domains.size(); // to retrieve the results before closing the PM
      }
    } catch (Exception e) {
      xLogger.severe("Error in fetching list of domains", e);
      throw new InvalidServiceException(backendMessages.getString("domains.fetch.error"));
    } finally {
      try {
        query.closeAll();
      } catch (Exception ignored) {
        xLogger.warn("Exception while closing query", ignored);
      }
      pm.close();
    }
    return new Results(domains, null, -1, Integer.parseInt(o));
  }

  @RequestMapping(value = "/domaininfo")
  public
  @ResponseBody
  CurrentUserModel getCurrentSessionDetails(HttpServletRequest request) {
    return new CurrentUserBuilder().buildCurrentUserModel(request);
  }

  @RequestMapping(value = "/approvals", method = RequestMethod.POST)
  public
  @ResponseBody
  String updateApprovalsConfig(@RequestBody ApprovalsConfigModel model,
                               HttpServletRequest request) {
    SecureUserDetails sUser = SecurityMgr.getUserDetails(request.getSession());
    Locale locale = sUser.getLocale();
    ResourceBundle backendMessages = Resources.get().getBundle("BackendMessages", locale);
    if (!GenericAuthoriser.authoriseAdmin(request)) {
      throw new UnauthorizedException(backendMessages.getString("permission.denied"));
    }
    String userId = sUser.getUsername();
    Long domainId = SessionMgr.getCurrentDomain(request.getSession(), userId);
    try {
      if (domainId == null) {
        xLogger.severe("Error in updating Approvals configuration");
        throw new InvalidServiceException(
            "Error in updating Approvals configuration");
      }
      ConfigContainer cc = getDomainConfig(domainId, userId, sUser.getLocale());
      DomainConfig dc = cc.dc;
      ApprovalsConfig.OrderConfig orderConfig = builder.buildApprovalsOrderConfig(model, dc);
      ApprovalsConfig approvalsConfig = new ApprovalsConfig();
      approvalsConfig.setOrderConfig(orderConfig);
      cc.dc.setApprovalsConfig(approvalsConfig);
      cc.dc.addDomainData(ConfigConstants.APPROVALS, generateUpdateList(userId));
      saveDomainConfig(sUser.getLocale(), domainId, cc, backendMessages);
      xLogger.info("AUDITLOG \t {0} \t {1} \t CONFIGURATION \t " +
          "UPDATE APPROVALS", domainId, sUser.getUsername());
      xLogger.info(cc.dc.toJSONSring());
    } catch (ServiceException | ConfigurationException e) {
      xLogger.severe("Error in updating Approvals configuration");
      throw new InvalidServiceException("Error in updating Approvals configuration");
    }
    return "Approvals config updated successfully";
  }

  @RequestMapping(value = "/dashboard", method = RequestMethod.GET)
  public
  @ResponseBody
  DashboardConfigModel getDashboardConfig(HttpServletRequest request) {
        /*if (!Authoriser.authoriseAdmin(request)) {
            throw new UnauthorizedException("Permission Denied");
        }*/
    SecureUserDetails sUser = SecurityMgr.getUserDetails(request.getSession());
    Locale locale = sUser.getLocale();
    String userId = sUser.getUsername();
    Long domainId = SessionMgr.getCurrentDomain(request.getSession(), userId);
    DomainConfig dc = DomainConfig.getInstance(domainId);
    DashboardConfig dbc = dc.getDashboardConfig();
    try {
      return builder.buildDashboardConfigModel(dbc, domainId, locale, sUser.getTimezone());
    } catch (ServiceException | ObjectNotFoundException e) {
      xLogger.severe("Error in fetching Dashboard configuration", e);
      throw new InvalidServiceException("Error in fetching Dashboard configuration");
    }
  }

  @RequestMapping(value = "/approvals", method = RequestMethod.GET)
  public
  @ResponseBody
  ApprovalsConfigModel getApprovalsConfig(HttpServletRequest request)
      throws ServiceException, ConfigurationException {
    SecureUserDetails sUser = SecurityMgr.getUserDetails(request.getSession());
    Locale locale = sUser.getLocale();
    String userId = sUser.getUsername();
    Long domainId = SessionMgr.getCurrentDomain(request.getSession(), userId);
    DomainConfig dc = DomainConfig.getInstance(domainId);
    ApprovalsConfig ac = dc.getApprovalsConfig();
    UsersService as = Services.getService(UsersServiceImpl.class, locale);
    try {
      return builder.buildApprovalsConfigModel(ac, as, domainId, locale, sUser.getTimezone());
    } catch (Exception e) {
      xLogger.severe("Error in fetching Approval configuration", e);
      throw new InvalidServiceException("Error in fetching Approvals configuration");
    }

  }

  @RequestMapping(value = "/dashboard", method = RequestMethod.POST)
  public
  @ResponseBody
  String updateDashboardConfig(@RequestBody DashboardConfigModel model,
                               HttpServletRequest request) {
    SecureUserDetails sUser = SecurityMgr.getUserDetails(request.getSession());
    Locale locale = sUser.getLocale();
    ResourceBundle backendMessages = Resources.get().getBundle("BackendMessages", locale);
    if (!GenericAuthoriser.authoriseAdmin(request)) {
      throw new UnauthorizedException(backendMessages.getString("permission.denied"));
    }
    try {
      String userId = sUser.getUsername();
      Long domainId = SessionMgr.getCurrentDomain(request.getSession(), userId);
      DashboardConfig dashboardConfig = new DashboardConfig();

      DashboardConfig.ActivityPanelConfig
          actPanelConfig =
          new DashboardConfig.ActivityPanelConfig();
      actPanelConfig.showActivityPanel = model.ape;

      DashboardConfig.RevenuePanelConfig rvnPanelConfig = new DashboardConfig.RevenuePanelConfig();
      rvnPanelConfig.showRevenuePanel = model.rpe;

      DashboardConfig.OrderPanelConfig ordPanelConfig = new DashboardConfig.OrderPanelConfig();
      ordPanelConfig.showOrderPanel = model.ope;

      DashboardConfig.InventoryPanelConfig
          invPanelConfig =
          new DashboardConfig.InventoryPanelConfig();
      invPanelConfig.showInvPanel = model.ipe;

      DashboardConfig.DBOverviewConfig dbOverviewConfig = new DashboardConfig.DBOverviewConfig();
      if (model.dmtg != null) {
        dbOverviewConfig.dmtg = StringUtil.getCSV(model.dmtg);
      } else {
        dbOverviewConfig.dmtg = null;
      }
      dbOverviewConfig.dimtg = model.dimtg;
      dbOverviewConfig.detg = model.detg;
      dbOverviewConfig.dtt = model.dtt;
      dbOverviewConfig.atdd = model.atdd;
      dbOverviewConfig.edm = model.edm;
      dbOverviewConfig.aper = model.aper;
      if (model.exet != null) {
        StringBuilder eetags = new StringBuilder();
        for (int i = 0; i < model.exet.length; i++) {
          if (StringUtils.isNotEmpty(model.exet[i])) {
            eetags.append(model.exet[i].concat(","));
          }
        }
        dbOverviewConfig.exet = eetags.toString();
      } else {
        dbOverviewConfig.exet = null;
      }
      if (model.dutg != null) {
        StringBuilder eutgs = new StringBuilder();
        for (String uTag : model.dutg) {
          if (StringUtils.isNotEmpty(uTag)) {
            eutgs.append(uTag.concat(CharacterConstants.COMMA));
          }
        }
        eutgs.setLength(eutgs.length() - 1);
        dbOverviewConfig.dutg = eutgs.toString();
      } else {
        dbOverviewConfig.dutg = null;
      }
      if (model.exts != null) {
        dbOverviewConfig.exts = StringUtil.getCSV(model.exts);
      }
      dashboardConfig.setActivityPanelConfig(actPanelConfig);
      dashboardConfig.setRevenuePanelConfig(rvnPanelConfig);
      dashboardConfig.setOrderPanelConfig(ordPanelConfig);
      dashboardConfig.setInventoryPanelConfig(invPanelConfig);
      dashboardConfig.setDbOverConfig(dbOverviewConfig);

      ConfigContainer cc = getDomainConfig(domainId, userId, sUser.getLocale());
      cc.dc.setDashboardConfig(dashboardConfig);
      cc.dc.addDomainData(ConfigConstants.DASHBOARD, generateUpdateList(userId));
      saveDomainConfig(sUser.getLocale(), domainId, cc, backendMessages);
      xLogger.info("AUDITLOG \t {0} \t {1} \t CONFIGURATION \t " +
          "UPDATE DASHBOARD", domainId, sUser.getUsername());
      xLogger.info(cc.dc.toJSONSring());
    } catch (ServiceException | ConfigurationException e) {
      xLogger.severe("Error in updating Custom Report", e);
      throw new InvalidServiceException(backendMessages.getString("customreport.update.error"));
    }

    return backendMessages.getString("dashboard.config.update.success");
  }

  private void saveDomainConfig(Locale locale, Long domainId, ConfigContainer cc,
                                ResourceBundle backendMessages)
      throws ConfigurationServiceException, ServiceException {
    ConfigurationMgmtService
        cms =
        Services.getService(ConfigurationMgmtServiceImpl.class, locale);
    try {
      cc.c.setConfig(cc.dc.toJSONSring());
      if (cc.add) {
        cms.addConfiguration(cc.c.getKey(), cc.c);
      } else {
        cms.updateConfiguration(cc.c);
      }
      MemcacheService cache = AppFactory.get().getMemcacheService();
      if (cache != null) {
        cache.put(DomainConfig.getCacheKey(domainId), cc.dc);
      }
    } catch (ConfigurationException | ServiceException e) {
      xLogger
          .severe("Invalid format of configuration for domain {0}: {1}", domainId,
              e.getMessage());
      throw new ConfigurationServiceException(
          backendMessages.getString("invalid.domain.config") + " " + domainId);
    }
  }

  private ConfigContainer getDomainConfig(Long domainId, String userId, Locale locale)
      throws ServiceException, ConfigurationException {
    ConfigurationMgmtService
        cms =
        Services.getService(ConfigurationMgmtServiceImpl.class, locale);
    String key = IConfig.CONFIG_PREFIX + domainId;
    ConfigContainer cc = new ConfigContainer();
    try {
      cc.c = cms.getConfiguration(key);
      cc.dc = new DomainConfig(cc.c.getConfig());
    } catch (ObjectNotFoundException e) {
      cc.dc = new DomainConfig();
      cc.c = JDOUtils.createInstance(IConfig.class);
      cc.c.setKey(key);
      cc.c.setUserId(userId);
      cc.c.setDomainId(domainId);
      cc.c.setLastUpdated(new Date());
      cc.add = true;
    }
    return cc;
  }

  private SyncConfig generateSyncConfig(CapabilitiesConfigModel model) {
    SyncConfig syncCfg = new SyncConfig();
    syncCfg.setMasterDataRefreshInterval(model.mdri * SyncConfig.HOURS_IN_A_DAY);
    syncCfg.setAppLogUploadInterval(model.aplui * SyncConfig.HOURS_IN_A_DAY);
    syncCfg.setSmsTransmissionWaitDuration(model.stwd * SyncConfig.HOURS_IN_A_DAY);
    return syncCfg;
  }

  private class ConfigContainer {
    public IConfig c = null;
    public DomainConfig dc = null;
    public SupportConfig sc = null;
    public boolean add = false;
  }


}

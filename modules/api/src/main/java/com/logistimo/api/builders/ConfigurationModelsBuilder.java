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

package com.logistimo.api.builders;

import com.logistimo.api.constants.ConfigConstants;
import com.logistimo.api.models.MenuStatsModel;
import com.logistimo.api.models.configuration.AccountingConfigModel;
import com.logistimo.api.models.configuration.AdminContactConfigModel;
import com.logistimo.api.models.configuration.ApprovalsConfigModel;
import com.logistimo.api.models.configuration.AssetConfigModel;
import com.logistimo.api.models.configuration.CapabilitiesConfigModel;
import com.logistimo.api.models.configuration.DashboardConfigModel;
import com.logistimo.api.models.configuration.GeneralConfigModel;
import com.logistimo.api.models.configuration.InventoryConfigModel;
import com.logistimo.api.models.configuration.OrdersConfigModel;
import com.logistimo.api.models.configuration.SupportConfigModel;
import com.logistimo.api.models.configuration.TagsConfigModel;
import com.logistimo.assets.entity.IAsset;
import com.logistimo.auth.SecurityConstants;
import com.logistimo.auth.SecurityMgr;
import com.logistimo.auth.utils.SecurityUtils;
import com.logistimo.auth.utils.SessionMgr;
import com.logistimo.config.entity.IConfig;
import com.logistimo.config.models.AccountingConfig;
import com.logistimo.config.models.ActualTransConfig;
import com.logistimo.config.models.AdminContactConfig;
import com.logistimo.config.models.ApprovalsConfig;
import com.logistimo.config.models.AssetConfig;
import com.logistimo.config.models.AssetSystemConfig;
import com.logistimo.config.models.CapabilityConfig;
import com.logistimo.config.models.ConfigurationException;
import com.logistimo.config.models.DashboardConfig;
import com.logistimo.config.models.DemandBoardConfig;
import com.logistimo.config.models.DomainConfig;
import com.logistimo.config.models.GeneralConfig;
import com.logistimo.config.models.InventoryConfig;
import com.logistimo.config.models.LeadTimeAvgConfig;
import com.logistimo.config.models.MatStatusConfig;
import com.logistimo.config.models.OptimizerConfig;
import com.logistimo.config.models.OrdersConfig;
import com.logistimo.config.models.SupportConfig;
import com.logistimo.config.models.SyncConfig;
import com.logistimo.config.service.ConfigurationMgmtService;
import com.logistimo.config.service.impl.ConfigurationMgmtServiceImpl;
import com.logistimo.constants.Constants;
import com.logistimo.domains.entity.IDomain;
import com.logistimo.domains.service.DomainsService;
import com.logistimo.domains.service.impl.DomainsServiceImpl;
import com.logistimo.entities.service.EntitiesService;
import com.logistimo.entities.service.EntitiesServiceImpl;
import com.logistimo.exception.InvalidServiceException;
import com.logistimo.exception.SystemException;
import com.logistimo.inventory.entity.ITransaction;
import com.logistimo.logger.XLog;
import com.logistimo.orders.entity.IOrder;
import com.logistimo.pagination.PageParams;
import com.logistimo.pagination.Results;
import com.logistimo.security.SecureUserDetails;
import com.logistimo.services.ObjectNotFoundException;
import com.logistimo.services.Resources;
import com.logistimo.services.ServiceException;
import com.logistimo.services.Services;
import com.logistimo.services.utils.ConfigUtil;
import com.logistimo.tags.TagUtil;
import com.logistimo.users.entity.IUserAccount;
import com.logistimo.users.service.UsersService;
import com.logistimo.users.service.impl.UsersServiceImpl;
import com.logistimo.utils.LocalDateUtil;
import com.logistimo.utils.StringUtil;

import org.apache.commons.lang.StringUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

/**
 * Created by Mohan Raja on 14/03/15
 */
public class ConfigurationModelsBuilder {
  private static final XLog xLogger = XLog.getLog(ConfigurationModelsBuilder.class);
  private static final int GAE_MAX_ENTITIES = 30;

  public MenuStatsModel buildMenuStats(SecureUserDetails user, DomainConfig config, Locale locale,
                                       String timezone, HttpServletRequest request)
      throws ServiceException {
    Long domainId = SessionMgr.getCurrentDomain(request.getSession(), user.getUsername());
    MenuStatsModel model = new MenuStatsModel();
    model.iAccTbEn = config.isAccountingEnabled();
    model.iOrdTbEn =
        !config.isCapabilityDisabled(DomainConfig.CAPABILITY_ORDERS);
    boolean isEntityManager = SecurityConstants.ROLE_SERVICEMANAGER.equals(user.getRole());
    boolean isEntityOperator = SecurityConstants.ROLE_KIOSKOWNER.equals(user.getRole());
    model.iConfTbEn = !isEntityManager && !isEntityOperator;
    model.iRepTbEn = !(isEntityManager || isEntityOperator);
    model.iAdm = !(isEntityManager || isEntityOperator);
    model.iSU = SecurityConstants.ROLE_SUPERUSER.equals(user.getRole());
    model.iMan = isEntityManager;
    IUserAccount userAccount = null;
    try {
      UsersService
          accountsService =
          Services.getService(UsersServiceImpl.class, user.getLocale());
      DomainsService ds = Services.getService(DomainsServiceImpl.class);
      IDomain domain = ds.getDomain(domainId);
      userAccount = accountsService.getUserAccount(user.getUsername());
      model.iAU = IUserAccount.PERMISSION_ASSET.equals(userAccount.getPermission());
      List<String>
          hideUTags =
          StringUtil.getList(config.getDashboardConfig().getDbOverConfig().dutg);
      if (hideUTags != null) {
        for (String userTag : userAccount.getTags()) {
          if (hideUTags.contains(userTag)) {
            model.hbUTag = true;
            break;
          }
        }
      }
      if (StringUtils.isNotEmpty(userAccount.getFullName())) {
        model.ufn = userAccount.getFullName();
      } else {
        model.ufn = userAccount.getFirstName();
      }
      model.unm = userAccount.getUserId();
      model.dnm = domain.getName();
      model.dId = domain.getId();
      model.lng = userAccount.getLanguage();
      model.locale = userAccount.getLocale().toLanguageTag();
      model.utz = userAccount.getTimezone();
      model.em = userAccount.getEmail();
      if (userAccount.getDomainId().equals(domainId)) {
        model.eid = userAccount.getPrimaryKiosk();
      }
      model.createdOn = domain.getCreatedOn();
      model.hasChild = domain.getHasChild();
      if (model.eid == null) {
        if (SecurityConstants.ROLE_SERVICEMANAGER.equals(user.getRole())) {
          EntitiesService as = Services.getService(EntitiesServiceImpl.class, user.getLocale());
          Results
              results =
              as.getKioskIdsForUser(user.getUsername(), null,
                  new PageParams(null, 0, 2));
          List kiosks = results.getResults();
          if (kiosks!=null && kiosks.size() == 1) {
            model.eid = (Long) kiosks.get(0);
          }
        }
      }
    } catch (Exception e) {
      throw new ServiceException("Severe exception while fetching domain details", e);
    }
    if (config.getAssetConfig() != null) {
      model.iTempOnly = config.getAssetConfig().isTemperatureMonitoringEnabled();
      model.iTempWLg = config.getAssetConfig().isTemperatureMonitoringWithLogisticsEnabled();
    }
    model.iDmdOnly = "1".equals(config.getOrderGeneration());
    model.hImg = config.getPageHeader();
    model.cur = config.getCurrency();
    model.cnt = config.getCountry();
    model.st = config.getState();
    model.dst = config.getDistrict();
    model.iOCEnabled = !config.getUiPreference();
    boolean isForceNewUI = ConfigUtil.getBoolean("force.newui", false);
    model.onlyNewUI = isForceNewUI || config.isOnlyNewUIEnabled();
    model.support = buildAllSupportConfigModels(config);
    model.iPredEnabled =
        config.getInventoryConfig() != null && config.getInventoryConfig().showPredictions();
    model.mmt =
        config.getInventoryConfig() == null ? InventoryConfig.MIN_MAX_ABS_QTY
            : config.getInventoryConfig().getMinMaxType();
    model.mmd =
        config.getInventoryConfig() == null ? Constants.FREQ_DAILY
            : config.getInventoryConfig().getMinMaxDur();
    model.tr = config.getOrdersConfig() != null && config.getOrdersConfig().isTransferRelease();
    //model.acs = config.getOrdersConfig() != null && config.getOrdersConfig().isAllowCreatingShipments();
    if (model.iSU) {
      model.accd = true;
    } else if (model.iAdm) {
      try {
        List<Long> accDids = userAccount.getAccessibleDomainIds();
        int size = accDids == null ? 0 : accDids.size();
        if (size > 1) {
          model.accd = true;
        } else if (size == 1) {
          DomainsService
              service =
              Services.getService(DomainsServiceImpl.class, user.getLocale());
          IDomain domain = service.getDomain(accDids.get(0));
          model.accd = domain.getHasChild();
        }
      } catch (Exception e) {
        xLogger
            .severe(
                "Unable to set accessible domain id flag for user {0}, domain: {1}, exce: {2}",
                user.getUsername(), user.getDomainId(), e);
        throw new ServiceException("Severe exception while fetching domain details", e);

      }
    }
    try {
      model.ac = buildAssetConfigModel(config, locale, timezone);
    } catch (Exception e) {
      xLogger
          .warn("Unable to get asset config for the domain {0}, exception: {1}",
              user.getDomainId(),
              e);
    }
    try {
      if (config.getDashboardConfig().getDbOverConfig() != null) {
        model.iATD = !config.getDashboardConfig().getDbOverConfig().atdd;
      }
    } catch (Exception e) {
      xLogger.warn("atdd value null", e);
    }
    try {
      UsersService
          accountsService =
          Services.getService(UsersServiceImpl.class, user.getLocale());
      if(config.getApprovalsConfig() != null) {
        model.apc = buildApprovalsConfigModel(config.getApprovalsConfig(), accountsService, domainId, locale, timezone);
        if(model.apc.pa != null && model.apc.pa.size() > 0) {
          model.toae = true;
        }
        if(model.apc.psoa != null && model.apc.psoa.size() > 0) {
          List<ApprovalsConfigModel.PurchaseSalesOrderApproval> psoa = model.apc.psoa;
          for(ApprovalsConfigModel.PurchaseSalesOrderApproval ps : psoa) {
            if(!model.poae && ps.poa) {
              model.poae = true;
            }
            if(!model.soae && ps.soa) {
              model.soae = true;
              break;
            }
          }
        }
      }
    } catch (Exception e) {
      xLogger.fine("Unable to fetch approval config for {0}", domainId, e);
    }
    model.allocateInventory = config.autoGI();
    if (SecurityConstants.ROLE_SERVICEMANAGER.equalsIgnoreCase(user.getRole())
        && config.getCapabilityMapByRole() != null) {
      CapabilityConfig cc;
      cc = config.getCapabilityMapByRole().get(SecurityConstants.ROLE_SERVICEMANAGER);
      if (cc != null) {
        model.vt = cc.isCapabilityDisabled("vt");
        model.ct = cc.isCapabilityDisabled("ct");
        model.vo = cc.isCapabilityDisabled("vo");
        model.ns = cc.isCapabilityDisabled("ns");
      }
    }
    //adding revenue report render flag
    if (null != config.getDashboardConfig().getRevenuePanelConfig()) {
      model.rpe = config.getDashboardConfig().getRevenuePanelConfig().showRevenuePanel;
    }
    model.mdp = model.iAdm;
    if(!model.mdp && null != config.getDashboardConfig().getDbOverConfig()) {
      model.mdp = config.getDashboardConfig().getDbOverConfig().edm;
    }
    return model;
  }

  public ApprovalsConfig.OrderConfig buildApprovalsOrderConfig(ApprovalsConfigModel model) {
    if (model != null) {
      ApprovalsConfig.OrderConfig orderConfig = new ApprovalsConfig.OrderConfig();
      List<ApprovalsConfig.PurchaseSalesOrderConfig>
          purchaseSalesOrderConfigList =
          new ArrayList<>();
      if (model.psoa != null && model.psoa.size() > 0) {
        for (ApprovalsConfigModel.PurchaseSalesOrderApproval psa : model.psoa) {
          ApprovalsConfig.PurchaseSalesOrderConfig
              psConfig =
              new ApprovalsConfig.PurchaseSalesOrderConfig();
          psConfig.setPurchaseOrderApproval(psa.poa);
          psConfig.setSalesOrderApproval(psa.soa);
          psConfig.setEntityTags(psa.eTgs);
          purchaseSalesOrderConfigList.add(psConfig);
        }
        orderConfig.setPurchaseSalesOrderApproval(purchaseSalesOrderConfigList);
        if (model.px == 0) {
          orderConfig.setPurchaseOrderApprovalExpiry(24);
        } else {
          orderConfig.setPurchaseOrderApprovalExpiry(model.px);
        }
        if (model.sx == 0) {
          orderConfig.setSalesOrderApprovalExpiry(24);
        } else {
          orderConfig.setSalesOrderApprovalExpiry(model.sx);
        }
      }
      if (model.pa != null && model.pa.size() > 0) {
        List<String> puIds = new ArrayList<>();
        for (int i = 0; i < model.pa.size(); i++) {
          puIds.add(model.pa.get(i).id);
        }
        orderConfig.setPrimaryApprovers(puIds);
      }
      if (model.sa != null && model.sa.size() > 0) {
        List<String> suIds = new ArrayList<>();
        for (int i = 0; i < model.sa.size(); i++) {
          suIds.add(model.sa.get(i).id);
        }
        orderConfig.setSecondaryApprovers(suIds);
      }

      if (model.tx == 0) {
        orderConfig.setTransferOrderApprovalExpiry(24);
      } else {
        orderConfig.setTransferOrderApprovalExpiry(model.tx);
      }
      return orderConfig;
    }
    return null;
  }

  public GeneralConfigModel buildGeneralConfigModel(Long domainId, Locale locale, String timezone)
      throws ServiceException, ObjectNotFoundException, ConfigurationException {
    String strConfig;
    DomainConfig dc;
    GeneralConfigModel model = new GeneralConfigModel();
    ConfigurationMgmtService
        cms =
        Services.getService(ConfigurationMgmtServiceImpl.class, locale);
    try {
      String key = IConfig.CONFIG_PREFIX + domainId.toString();
      strConfig = cms.getConfiguration(key).getConfig();
      dc = new DomainConfig(strConfig);
    } catch (Exception e) {
      dc = new DomainConfig();
    }
    List<String> val = dc.getDomainData(ConfigConstants.GENERAL);
    if (val != null) {
      model.createdBy = val.get(0);
      model.lastUpdated =
          LocalDateUtil.format(new Date(Long.parseLong(val.get(1))), locale, timezone);
      model.fn = getFullName(model.createdBy, locale);
    }
    model.cnt = dc.getCountry() != null ? dc.getCountry() : "";
    model.st = dc.getState() != null ? dc.getState() : "";
    model.ds = dc.getDistrict() != null ? dc.getDistrict() : "";
    model.lng = StringUtils.isNotEmpty(dc.getLanguage()) ? dc.getLanguage() : "en";
    model.tz = dc.getTimezone() != null ? dc.getTimezone() : "";
    model.cur = dc.getCurrency() != null ? dc.getCurrency() : "";
    model.pgh = dc.getPageHeader() != null ? dc.getPageHeader() : "";
    model.sc = !dc.getUiPreference();
    model.domainId = domainId;
    model.snh = dc.isEnableSwitchToNewHost();
    model.nhn = dc.getNewHostName();
    model.support = buildAllSupportConfigModels(dc);
    model.adminContact = buildAllAdminContactConfigModel(dc.getAdminContactConfig());
    return model;
  }

  public Map<String, AdminContactConfigModel> buildAllAdminContactConfigModel(AdminContactConfig config)
      throws ObjectNotFoundException {
    Map<String, AdminContactConfigModel> model = new HashMap<>();
    AdminContactConfigModel adminContactModel;
    adminContactModel = buildAdminContactModel(config.getPrimaryAdminContact());
    model.put(AdminContactConfig.PRIMARY_ADMIN_CONTACT, adminContactModel);
    adminContactModel = buildAdminContactModel(config.getSecondaryAdminContact());
    model.put(AdminContactConfig.SECONDARY_ADMIN_CONTACT, adminContactModel);
    return model;
  }
  public AdminContactConfigModel buildAdminContactModel(String userId)
      throws ObjectNotFoundException {
    AdminContactConfigModel model = new AdminContactConfigModel();
    if(StringUtils.isNotEmpty(userId)) {
      UsersService as = Services.getService(UsersServiceImpl.class);
      IUserAccount userAccount = as.getUserAccount(userId);
      if(userAccount != null) {
        model.userId = userId;
        model.email = userAccount.getEmail();
        model.phn = userAccount.getMobilePhoneNumber();
        model.userNm = userAccount.getFullName();
      }
    }
    return model;
  }

  public List<SupportConfigModel> buildAllSupportConfigModels(DomainConfig dc) {
    List<SupportConfigModel> support = new ArrayList<>(3);
    SupportConfigModel scModel = new SupportConfigModel();
    scModel.role = SecurityConstants.ROLE_KIOSKOWNER;
    scModel = buildSupportConfigModel(scModel.role, dc);
    support.add(scModel);
    scModel = new SupportConfigModel();
    scModel.role = SecurityConstants.ROLE_SERVICEMANAGER;
    scModel = buildSupportConfigModel(scModel.role, dc);
    support.add(scModel);
    scModel = new SupportConfigModel();
    scModel.role = SecurityConstants.ROLE_DOMAINOWNER;
    scModel = buildSupportConfigModel(scModel.role, dc);
    support.add(scModel);
    return support;
  }


  public SupportConfigModel buildSupportConfigModel(String role, DomainConfig dc) {
    SupportConfig config = null;
    if (StringUtils.isNotEmpty(role) && dc != null) {
      config = dc.getSupportConfigByRole(role);
    }
    SupportConfigModel model = new SupportConfigModel();
    if (config != null) {
      model.usrid = config.getSupportUser();
      if (StringUtils.isNotEmpty(model.usrid)) {
        try {
          UsersService as = Services.getService(UsersServiceImpl.class);
          IUserAccount userAccount = as.getUserAccount(model.usrid);
          if (userAccount != null) {
            model.phnm = userAccount.getMobilePhoneNumber();
            model.em = userAccount.getEmail();
            model.userpopulate = true;
            model.usrname = userAccount.getFullName();
          }
        } catch (SystemException e) {
          xLogger.severe("Error in fetching user details", e);
          throw new InvalidServiceException("Unable to fetch user details for " + model.usrid);
        } catch (ObjectNotFoundException e) {
          xLogger
              .warn("Configured support user {0} for role {1} no longer exists",
                  model.usrid, role,
                  e);
          model.usrid = null;
        }
      } else {
        model.phnm = config.getSupportPhone();
        model.em = config.getSupportEmail();
        model.usrname = config.getSupportUserName();
      }
    }
    model.role = role;
    return model;
  }

  public AccountingConfigModel buildAccountingConfigModel(Long domainId, Locale locale,
                                                          String timezone)
      throws ServiceException, ObjectNotFoundException, JSONException {
    DomainConfig dc = DomainConfig.getInstance(domainId);
    AccountingConfig ac = dc.getAccountingConfig();
    AccountingConfigModel model = new AccountingConfigModel();
    List<String> val = dc.getDomainData(ConfigConstants.ACCOUNTING);
    if (val != null) {
      model.createdBy = val.get(0);
      model.lastUpdated =
          LocalDateUtil.format(new Date(Long.parseLong(val.get(1))), locale, timezone);
      model.fn = getFullName(model.createdBy, locale);
    }
    if (ac != null) {
      model.ea = ac.isAccountingEnabled();
      model.cl = ac.getCreditLimit();
      if (ac.enforceConfirm()) {
        model.en = IOrder.CONFIRMED;
      } else if (ac.enforceShipped()) {
        model.en = IOrder.COMPLETED;
      }
    }
    return model;
  }

  public String getFullName(String userId, Locale locale) {
    try {
      UsersService as = Services.getService(UsersServiceImpl.class, locale);
      return as.getUserAccount(userId).getFullName();
    } catch (Exception e) {
      //ignore.. get for display only.
    }
    return null;
  }

  public TagsConfigModel buildTagsConfigModel(DomainConfig dc, Locale locale, String timezone)
      throws ServiceException, ObjectNotFoundException {
    TagsConfigModel model = new TagsConfigModel();
    List<String> val = dc.getDomainData(ConfigConstants.TAGS);
    if (val != null) {
      model.createdBy = val.get(0);
      model.lastUpdated =
          LocalDateUtil.format(new Date(Long.parseLong(val.get(1))), locale, timezone);
      model.fn = getFullName(model.createdBy, locale);
    }

    model.mt = TagUtil.getTagsArray(dc.getMaterialTags());
    model.et = TagUtil.getTagsArray(dc.getKioskTags());
    model.rt = TagUtil.getTagsArray(dc.getRouteTags());
    model.ot = TagUtil.getTagsArray(dc.getOrderTags());
    model.ut = TagUtil.getTagsArray(dc.getUserTags());
    model.emt = dc.forceTagsMaterial();
    model.eet = dc.forceTagsKiosk();
    model.eot = dc.forceTagsOrder();
    model.en = dc.getRouteBy();
    model.eut = dc.forceTagsUser();
    if (dc.getEntityTagOrder() != null && !dc.getEntityTagOrder().isEmpty()) {
      for (Map.Entry<String, Integer> entry : dc.getEntityTagOrder().entrySet()) {
        TagsConfigModel.ETagOrder eto = new TagsConfigModel.ETagOrder();
        eto.etg = entry.getKey();
        eto.rnk = entry.getValue();
        model.etr.add(eto);
      }
    }
    return model;
  }

  public AssetConfigModel buildAssetConfigModel(DomainConfig dc, Locale locale, String timezone)
      throws ConfigurationException, ServiceException, ObjectNotFoundException {
    AssetSystemConfig asc = AssetSystemConfig.getInstance();
    if (asc == null) {
      throw new ConfigurationException();
    }
    Map<Integer, AssetSystemConfig.Asset> assets = asc.assets;
    if (assets == null) {
      throw new ConfigurationException();
    }
    AssetConfig tc = dc.getAssetConfig();
    List<String> vendorIdsList = null, assetModelList = null, defaultSnsList = null,
        defaultMpsList =
            null;
    int enableTemp = 0;
    AssetConfig.Configuration configuration = null;
    AssetConfigModel acm = new AssetConfigModel();

    if (tc != null) {
      vendorIdsList = tc.getVendorIds();
      enableTemp = tc.getEnable();
      configuration = tc.getConfiguration();
      acm.namespace = tc.getNamespace();
      assetModelList = tc.getAssetModels();
      defaultSnsList = tc.getDefaultSns();
      defaultMpsList = tc.getDefaultMps();
    }
    for (Integer key : assets.keySet()) {
      AssetSystemConfig.Asset asset = assets.get(key);
      if (asset.getManufacturers() == null) {
        throw new ConfigurationException();
      }

      AssetConfigModel.Asset acmAsset = new AssetConfigModel.Asset();
      acmAsset.an = asset.getName();
      acmAsset.id = key;
      acmAsset.at = asset.type;
      acmAsset.iGe = asset.isGSMEnabled();
      acmAsset.iTs = asset.isTemperatureEnabled();
      if (asset.monitoringPositions != null) {
        for (AssetSystemConfig.MonitoringPosition monitoringPosition : asset.monitoringPositions) {
          AssetConfigModel.MonitoringPosition mp = new AssetConfigModel.MonitoringPosition();
          mp.mpId = monitoringPosition.mpId;
          mp.name = monitoringPosition.name;
          mp.sId = monitoringPosition.sId;
          acmAsset.mps.put(mp.mpId, mp);

          if (defaultMpsList != null && defaultMpsList
              .contains(key + Constants.KEY_SEPARATOR + monitoringPosition.mpId)) {
            acmAsset.dMp = monitoringPosition.mpId;
          }
        }
      }
      for (String manuKey : asset.getManufacturers().keySet()) {
        AssetSystemConfig.Manufacturer manufacturer = asset.getManufacturers().get(manuKey);
        AssetConfigModel.Mancfacturer acmManc = new AssetConfigModel.Mancfacturer();
        if (vendorIdsList != null) {
          if (key.equals(IAsset.TEMP_DEVICE) && vendorIdsList.contains(manuKey)
              || vendorIdsList.contains(key + Constants.KEY_SEPARATOR + manuKey)) {
            acmManc.iC = true;
          }
        }
        acmManc.id = manuKey;
        acmManc.name = manufacturer.name;
        if(asset.type == IAsset.MONITORED_ASSET) {
          acmManc.serialFormat = manufacturer.serialFormat;
          acmManc.modelFormat = manufacturer.modelFormat;
          acmManc.serialFormatDescription = manufacturer.serialFormatDescription;
          acmManc.modelFormatDescription = manufacturer.modelFormatDescription;
        }
        if (manufacturer.model != null) {
          for (AssetSystemConfig.Model model : manufacturer.model) {
            AssetConfigModel.Model configModel = new AssetConfigModel.Model();
            configModel.name = model.name;
            configModel.type = model.type;

            if (acmManc.iC != null && acmManc.iC && assetModelList != null && assetModelList
                .contains(
                    key + Constants.KEY_SEPARATOR + manuKey + Constants.KEY_SEPARATOR
                        + model.name)) {
              configModel.iC = true;
            }
            if (model.sns != null) {
              for (AssetSystemConfig.Sensor sensor : model.sns) {
                AssetConfigModel.Sensor configSensor = new AssetConfigModel.Sensor();
                configSensor.name = sensor.name;
                configSensor.mpId = sensor.mpId;
                configSensor.cd = sensor.cd;
                configModel.sns.put(configSensor.name, configSensor);
                if (defaultSnsList != null && defaultSnsList.contains(
                    key + Constants.KEY_SEPARATOR + model.name + Constants.KEY_SEPARATOR
                        + sensor.name)) {
                  configModel.dS = sensor.name;
                }
              }
            }
            if (model.feature != null) {
              AssetConfigModel.Feature feature = new AssetConfigModel.Feature();
              feature.pc = model.feature.pullConfig;
              feature.ds = model.feature.dailyStats;
              feature.ps = model.feature.powerStats;
              feature.dl = model.feature.displayLabel;
              feature.ct = model.feature.currentTemperature;
              configModel.fts = feature;
            }
            acmManc.model.put(configModel.name, configModel);
          }
        }
        acmAsset.mcs.put(acmManc.id, acmManc);
      }
      acm.assets.put(acmAsset.id, acmAsset);
    }
    acm.enable = enableTemp;
    acm.config = configuration;
    if (asc.workingStatuses != null) {
      AssetConfigModel.WorkingStatus workingStatus;
      for (AssetSystemConfig.WorkingStatus ws : asc.workingStatuses) {
        workingStatus = new AssetConfigModel.WorkingStatus();
        workingStatus.status = ws.status;
        workingStatus.dV = ws.displayValue;
        acm.wses.put(workingStatus.status, workingStatus);
      }
    }
    List<String> val = dc.getDomainData(ConfigConstants.TEMPERATURE);
    if (val != null) {
      acm.createdBy = val.get(0);
      acm.lastUpdated =
          LocalDateUtil.format(new Date(Long.parseLong(val.get(1))), locale, timezone);
      acm.fn = getFullName(acm.createdBy, locale);
    }

    return acm;
  }

  public CapabilitiesConfigModel buildCapabilitiesConfigModel(Long domainId, Locale locale,
                                                              DomainConfig dc, String timezone)
      throws ServiceException, ObjectNotFoundException {
    Map<String, CapabilityConfig> map = dc.getCapabilityMapByRole();
    CapabilitiesConfigModel model = new CapabilitiesConfigModel();

    if (dc.getCapabilities() != null) {
      model.cap = dc.getCapabilities().toArray(new String[dc.getCapabilities().size()]);
    }
    if (dc.getTransactionMenus() != null) {
      model.tm = dc.getTransactionMenus().toArray(new String[dc.getTransactionMenus().size()]);
    }
    if (dc.getCreatableEntityTypes() != null) {
      model.et =
          dc.getCreatableEntityTypes().toArray(
              new String[dc.getCreatableEntityTypes().size()]);
    }
    List<String> val = dc.getDomainData(ConfigConstants.CAPABILITIES);
    if (val != null) {
      model.createdBy = val.get(0);
      model.lastUpdated =
          LocalDateUtil.format(new Date(Long.parseLong(val.get(1))), locale, timezone);
      model.fn = getFullName(model.createdBy, locale);
    }
    model.er = dc.allowRouteTagEditing();
    model.lr = dc.isLoginAsReconnect();
    model.sv = dc.sendVendors();
    model.sc = dc.sendCustomers();
    model.gcs = dc.getGeoCodingStrategy();
    model.atexp = dc.getAuthenticationTokenExpiry();
    model.llr = dc.isLocalLoginRequired();

    Map<String, String> tagInvByOperation = dc.gettagsInvByOperation();
    if (!tagInvByOperation.isEmpty()) {
      if (tagInvByOperation.get(ITransaction.TYPE_ISSUE) != null) {
        model.hii =
            new ArrayList<String>(
                Arrays.asList(tagInvByOperation.get(ITransaction.TYPE_ISSUE).split(",")));
      }
      if (tagInvByOperation.get(ITransaction.TYPE_RECEIPT) != null) {
        model.hir =
            new ArrayList<String>(
                Arrays.asList(tagInvByOperation.get(ITransaction.TYPE_RECEIPT).split(",")));
      }
      if (tagInvByOperation.get(ITransaction.TYPE_PHYSICALCOUNT) != null) {
        model.hip =
            new ArrayList<String>(
                Arrays.asList(tagInvByOperation.get(ITransaction.TYPE_PHYSICALCOUNT).split(",")));
      }
      if (tagInvByOperation.get(ITransaction.TYPE_WASTAGE) != null) {
        model.hiw =
            new ArrayList<String>(
                Arrays.asList(tagInvByOperation.get(ITransaction.TYPE_WASTAGE).split(",")));
      }
      if (tagInvByOperation.get(ITransaction.TYPE_TRANSFER) != null) {
        model.hit =
            new ArrayList<String>(
                Arrays.asList(tagInvByOperation.get(ITransaction.TYPE_TRANSFER).split(",")));
      }
    }

    if (dc.getTagsInventory() != null) {
      model.hi = new ArrayList<String>(Arrays.asList(dc.getTagsInventory().split(",")));
    }
    if (dc.getTagsOrders() != null) {
      model.ho = new ArrayList<String>(Arrays.asList(dc.getTagsOrders().split(",")));
    }
    model.eshp = dc.isEnableShippingOnMobile();
    model.ro = "";
    Map<String, CapabilitiesConfigModel> roleMap = new HashMap<String, CapabilitiesConfigModel>(4);
    roleMap.put(SecurityConstants.ROLE_KIOSKOWNER,
        constructCCM(map.get(SecurityConstants.ROLE_KIOSKOWNER), SecurityConstants.ROLE_KIOSKOWNER, dc));
    roleMap.put(SecurityConstants.ROLE_SERVICEMANAGER,
        constructCCM(map.get(SecurityConstants.ROLE_SERVICEMANAGER), SecurityConstants.ROLE_SERVICEMANAGER,
            dc));
    roleMap.put(SecurityConstants.ROLE_DOMAINOWNER,
        constructCCM(map.get(SecurityConstants.ROLE_DOMAINOWNER), SecurityConstants.ROLE_DOMAINOWNER, dc));
    roleMap.put(SecurityConstants.ROLE_SUPERUSER,
        constructCCM(map.get(SecurityConstants.ROLE_SUPERUSER), SecurityConstants.ROLE_SUPERUSER, dc));
    model.roleConfig = roleMap;

    if (dc.getSyncConfig() != null) {
      SyncConfig sc = dc.getSyncConfig();
      model.mdri = sc.getMasterDataRefreshInterval() / SyncConfig.HOURS_IN_A_DAY;
      model.aplui = sc.getAppLogUploadInterval() / SyncConfig.HOURS_IN_A_DAY;
      model.stwd = sc.getSmsTransmissionWaitDuration() / SyncConfig.HOURS_IN_A_DAY;
    }
    model.setTheme(dc.getStoreAppTheme());
    return model;
  }

  public CapabilitiesConfigModel buildRoleCapabilitiesConfigModel(String sRole, Long domainId,
                                                                  Locale locale, DomainConfig dc,
                                                                  String timezone)
      throws ServiceException, ObjectNotFoundException {
    Map<String, CapabilityConfig> map = dc.getCapabilityMapByRole();
    CapabilitiesConfigModel model;
    CapabilityConfig roleCapabConfig = map.get(sRole);
    if (roleCapabConfig != null) {
      model = constructCCM(roleCapabConfig, sRole, dc);
    } else {
      model = buildCapabilitiesConfigModel(domainId, locale, dc, timezone);
    }
    return model;
  }

  private CapabilitiesConfigModel constructCCM(CapabilityConfig config, String role,
                                               DomainConfig dc) {
    CapabilitiesConfigModel model = new CapabilitiesConfigModel();
    model.ro = role;
    if (config != null) {
      if (config.getCapabilities() != null) {
        model.tm = config.getCapabilities().toArray(new String[config.getCapabilities().size()]);
      }
      if (config.getCreatableEntityTypes() != null) {
        model.et =
            config.getCreatableEntityTypes()
                .toArray(new String[config.getCreatableEntityTypes().size()]);
      }
      model.er = config.allowRouteTagEditing();
      model.lr = config.isLoginAsReconnect();
      model.sv = config.sendVendors();
      model.sc = config.sendCustomers();
      model.gcs = config.getGeoCodingStrategy();
      if (config.getTagsInventory() != null) {
        model.hi = new ArrayList<String>(Arrays.asList(config.getTagsInventory().split(",")));
      }
      if (config.gettagByOperation(ITransaction.TYPE_ISSUE) != null) {
        model.hii =
            new ArrayList<>(
                Arrays.asList(config.gettagByOperation(ITransaction.TYPE_ISSUE).split(",")));
      }
      if (config.gettagByOperation(ITransaction.TYPE_RECEIPT) != null) {
        model.hir =
            new ArrayList<>(
                Arrays.asList(config.gettagByOperation(ITransaction.TYPE_RECEIPT).split(",")));
      }
      if (config.gettagByOperation(ITransaction.TYPE_PHYSICALCOUNT) != null) {
        model.hip =
            new ArrayList<>(Arrays
                .asList(
                    config.gettagByOperation(ITransaction.TYPE_PHYSICALCOUNT).split(",")));
      }
      if (config.gettagByOperation(ITransaction.TYPE_WASTAGE) != null) {
        model.hiw =
            new ArrayList<>(
                Arrays.asList(config.gettagByOperation(ITransaction.TYPE_WASTAGE).split(",")));
      }
      if (config.gettagByOperation(ITransaction.TYPE_TRANSFER) != null) {
        model.hit =
            new ArrayList<>(
                Arrays.asList(config.gettagByOperation(ITransaction.TYPE_TRANSFER).split(",")));
      }
      if (config.getTagsOrders() != null) {
        model.ho = new ArrayList<String>(Arrays.asList(config.getTagsOrders().split(",")));
      }
      model.eshp = config.isEnableShippingOnMobile();
    }
    if (dc != null) {
      model.atexp = dc.getAuthenticationTokenExpiry();
      if (dc.getSyncConfig() != null) {
        model.mdri = dc.getSyncConfig().getMasterDataRefreshInterval() / SyncConfig.HOURS_IN_A_DAY;
        model.aplui = dc.getSyncConfig().getAppLogUploadInterval() / SyncConfig.HOURS_IN_A_DAY;
        model.stwd =
            dc.getSyncConfig().getSmsTransmissionWaitDuration() / SyncConfig.HOURS_IN_A_DAY;
      }
      model.setTheme(dc.getStoreAppTheme());
    }
    return model;
  }

  public InventoryConfigModel buildInventoryConfigModel(DomainConfig dc, Locale locale,
                                                        Long domainId, String timezone)
      throws ConfigurationException, ServiceException, ObjectNotFoundException {
    InventoryConfigModel model = new InventoryConfigModel();
    InventoryConfig ic = dc.getInventoryConfig();
    if (ic == null) {
      throw new ConfigurationException("");
    }
    //Get the transaction reasons
    Map<String, String> transReasons = ic.getTransReasons();
    if (transReasons != null) {
      model.ri = transReasons.get(ITransaction.TYPE_ISSUE);
      model.rr = transReasons.get(ITransaction.TYPE_RECEIPT);
      model.rs = transReasons.get(ITransaction.TYPE_PHYSICALCOUNT);
      model.rd = transReasons.get(ITransaction.TYPE_WASTAGE);
      model.rt = transReasons.get(ITransaction.TYPE_TRANSFER);
    }
    List<String> val = dc.getDomainData(ConfigConstants.INVENTORY);
    if (val != null) {
      model.createdBy = val.get(0);
      model.lastUpdated =
          LocalDateUtil.format(new Date(Long.parseLong(val.get(1))), locale, timezone);
      model.fn = getFullName(model.createdBy, locale);
    }
    if (ic.isCimt()) {
      model.cimt = ic.isCimt();
      Map<String, String> imTransReasons = ic.getImTransReasons();
      for (Map.Entry<String, String> entry : imTransReasons.entrySet()) {
        InventoryConfigModel.MTagReason im = new InventoryConfigModel.MTagReason();
        im.mtg = entry.getKey();
        im.rsn = entry.getValue();
        model.imt.add(im);
      }
    }
    if (ic.isCrmt()) {
      model.crmt = ic.isCrmt();
      Map<String, String> rmTransReasons = ic.getRmTransReasons();
      for (Map.Entry<String, String> entry : rmTransReasons.entrySet()) {
        InventoryConfigModel.MTagReason rm = new InventoryConfigModel.MTagReason();
        rm.mtg = entry.getKey();
        rm.rsn = entry.getValue();
        model.rmt.add(rm);
      }
    }
    if (ic.isCsmt()) {
      model.csmt = ic.isCsmt();
      Map<String, String> smTransReasons = ic.getSmTransReasons();
      for (Map.Entry<String, String> entry : smTransReasons.entrySet()) {
        InventoryConfigModel.MTagReason sm = new InventoryConfigModel.MTagReason();
        sm.mtg = entry.getKey();
        sm.rsn = entry.getValue();
        model.smt.add(sm);
      }
    }
    if (ic.isCtmt()) {
      model.ctmt = ic.isCtmt();
      Map<String, String> tmTransReasons = ic.getTmTransReasons();
      for (Map.Entry<String, String> entry : tmTransReasons.entrySet()) {
        InventoryConfigModel.MTagReason tm = new InventoryConfigModel.MTagReason();
        tm.mtg = entry.getKey();
        tm.rsn = entry.getValue();
        model.tmt.add(tm);
      }
    }
    if (ic.isCdmt()) {
      model.cdmt = ic.isCdmt();
      Map<String, String> dmTransReasons = ic.getDmTransReasons();
      for (Map.Entry<String, String> entry : dmTransReasons.entrySet()) {
        InventoryConfigModel.MTagReason dm = new InventoryConfigModel.MTagReason();
        dm.mtg = entry.getKey();
        dm.rsn = entry.getValue();
        model.dmt.add(dm);
      }
    }

    //Get transaction export schedules
    model.etdx = ic.isEnabled();
    if (model.etdx) {
      List<String> times = ic.getTimes();
      List<String>
          dsTimes =
          LocalDateUtil.convertTimeStringList(times, dc.getTimezone(),
              false); // Convert times from UTC to domain specific timezone
      if (dsTimes != null && !dsTimes.isEmpty()) {
        model.et = StringUtil.getCSV(dsTimes);
      }
      model.an = StringUtil.getCSV(ic.getExportUsers());
    }
    InventoryConfig.ManualTransConfig manualTransConfig = ic.getManualTransConfig();
    if (manualTransConfig != null) {
      model.emuidt = manualTransConfig.enableManualUploadInvDataAndTrans;
      model.euse = manualTransConfig.enableUploadPerEntityOnly;
    }
    model.eidb = ic.showInventoryDashboard();
    if (ic.getEnTags() != null && !ic.getEnTags().isEmpty()) {
      model.enTgs = ic.getEnTags();
    }
    if (ic.getUserTags() != null && !ic.getUserTags().isEmpty()) {
      model.usrTgs = ic.getUserTags();
    }

    //Get Permissions
    InventoryConfig.Permissions perms = ic.getPermissions();
    if (perms != null) {
      model.ivc = perms.invCustomersVisible;
    }
    //Get Optimization Config
    OptimizerConfig oc = dc.getOptimizerConfig();
    int compute = oc.getCompute();
    model.co = String.valueOf(compute);
    model.crfreq = oc.getComputeFrequency();
    model.edis = oc.isExcludeDiscards();
    model.ersns = StringUtil.getList(oc.getExcludeReasons());
    model.aopfd = String.valueOf(oc.getMinAvgOrderPeriodicity());
    model.nopfd = String.valueOf(oc.getNumPeriods());

    model.im = oc.getInventoryModel();
    model.aopeoq = String.valueOf(oc.getMinAvgOrderPeriodicity());
    model.nopeoq = String.valueOf(oc.getNumPeriods());
    model.lt = String.valueOf(oc.getLeadTimeDefault());

    MatStatusConfig msi = ic.getMatStatusConfigByType("i");
    if (msi != null) {
      model.idf = msi.getDf();
      model.iestm = msi.getEtsm();
      model.ism = msi.isStatusMandatory();
    }
    MatStatusConfig msr = ic.getMatStatusConfigByType("r");
    if (msr != null) {
      model.rdf = msr.getDf();
      model.restm = msr.getEtsm();
      model.rsm = msr.isStatusMandatory();
    }
    MatStatusConfig mst = ic.getMatStatusConfigByType("t");
    if (mst != null) {
      model.tdf = mst.getDf();
      model.testm = mst.getEtsm();
      model.tsm = mst.isStatusMandatory();
    }
    MatStatusConfig msp = ic.getMatStatusConfigByType("p");
    if (msp != null) {
      model.pdf = msp.getDf();
      model.pestm = msp.getEtsm();
      model.psm = msp.isStatusMandatory();
    }
    MatStatusConfig msw = ic.getMatStatusConfigByType("w");
    if (msw != null) {
      model.wdf = msw.getDf();
      model.westm = msw.getEtsm();
      model.wsm = msw.isStatusMandatory();
    }

    ActualTransConfig atci = ic.getActualTransConfigByType(ITransaction.TYPE_ISSUE);
    model.catdi = "0";
    if (atci != null) {
      model.catdi = atci.getTy();
    }
    model.catdr = "0";
    ActualTransConfig atcr = ic.getActualTransConfigByType(ITransaction.TYPE_RECEIPT);
    if (atcr != null) {
      model.catdr = atcr.getTy();
    }
    model.catdp = "0";
    ActualTransConfig atcp = ic.getActualTransConfigByType(ITransaction.TYPE_PHYSICALCOUNT);
    if (atcp != null) {
      model.catdp = atcp.getTy();
    }
    model.catdw = "0";
    ActualTransConfig atcw = ic.getActualTransConfigByType(ITransaction.TYPE_WASTAGE);
    if (atcw != null) {
      model.catdw = atcw.getTy();
    }
    model.catdt = "0";
    ActualTransConfig atct = ic.getActualTransConfigByType(ITransaction.TYPE_TRANSFER);
    if (atct != null) {
      model.catdt = atct.getTy();
    }
    model.crc = String.valueOf(ic.getConsumptionRate());
    if (ic.getConsumptionRate() == InventoryConfig.CR_MANUAL) {
      model.mcrfreq = ic.getManualCRFreq();
    }
    model.dispcr = ic.displayCR();
    if (model.dispcr) {
      model.dcrfreq = ic.getDisplayCRFreq();
    }
    model.minhpccr = String.valueOf(oc.getMinHistoricalPeriod());
    model.maxhpccr = String.valueOf(oc.getMaxHistoricalPeriod());
    model.showpr = ic.showPredictions();
    model.ddf = oc.isDisplayDF();
    model.dooq = oc.isDisplayOOQ();
    model.mmType = ic.getMinMaxType();
    model.mmFreq = ic.getMinMaxFreq();
    model.mmDur = ic.getMinMaxDur();
    // Lead time average configuration
    LeadTimeAvgConfig leadTimeAvgConfig = oc.getLeadTimeAvgCfg();
    if (leadTimeAvgConfig != null) {
      model.ltacm = new InventoryConfigModel.LeadTimeAvgConfigModel();
      model.ltacm.mino = leadTimeAvgConfig.getMinNumOfOrders();
      model.ltacm.maxo = leadTimeAvgConfig.getMaxNumOfOrders();
      model.ltacm.maxop = leadTimeAvgConfig.getMaxOrderPeriods();
      model.ltacm.exopt = leadTimeAvgConfig.getExcludeOrderProcTime();
    }
    return model;
  }

  public ApprovalsConfigModel buildApprovalsConfigModel(ApprovalsConfig config, UsersService as,Long domainId, Locale locale, String timezone){
    if(config == null) {
      return null;
    }
    ApprovalsConfigModel model = new ApprovalsConfigModel();
    DomainConfig dc = DomainConfig.getInstance(domainId);
    List<String> val = dc.getDomainData(ConfigConstants.APPROVALS);
    if(val != null) {
      model.createdBy = val.get(0);
      model.lastUpdated = LocalDateUtil.format(new Date(Long.parseLong(val.get(1))), locale, timezone);
      model.fn = getFullName(model.createdBy, locale);
    }
    ApprovalsConfig.OrderConfig orderConfig = config.getOrderConfig();
    if(orderConfig != null) {
      UserBuilder userBuilder = new UserBuilder();
      if(orderConfig.getPrimaryApprovers() != null && orderConfig.getPrimaryApprovers().size() > 0) {
        model.pa =
            userBuilder.buildUserModels(constructUserAccount(as, orderConfig.getPrimaryApprovers()), locale,
                timezone, true);
      }
      if(orderConfig.getSecondaryApprovers() != null && orderConfig.getSecondaryApprovers().size() > 0) {
        model.sa =
            userBuilder.buildUserModels(constructUserAccount(as, orderConfig.getSecondaryApprovers()), locale,
                timezone, true);
      }
      model.px = orderConfig.getPurchaseOrderApprovalExpiry();
      model.sx = orderConfig.getSalesOrderApprovalExpiry();
      model.tx = orderConfig.getTransferOrderApprovalExpiry();
      if(orderConfig.getPurchaseSalesOrderApproval() != null && orderConfig.getPurchaseSalesOrderApproval().size() > 0) {
        List<ApprovalsConfig.PurchaseSalesOrderConfig> psocs = orderConfig.getPurchaseSalesOrderApproval();
        List<ApprovalsConfigModel.PurchaseSalesOrderApproval> psoas = new ArrayList<>();
        for(ApprovalsConfig.PurchaseSalesOrderConfig psos : psocs) {
          ApprovalsConfigModel.PurchaseSalesOrderApproval psoa = new ApprovalsConfigModel.PurchaseSalesOrderApproval();
          psoa.eTgs = psos.getEntityTags();
          psoa.poa = psos.isPurchaseOrderApproval();
          psoa.soa = psos.isSalesOrderApproval();
          psoas.add(psoa);
        }
        model.psoa = psoas;
      }
    }
    return model;
  }

  public DashboardConfigModel buildDashboardConfigModel(DashboardConfig config, Long domainId,
                                                        Locale locale, String timezone)
      throws ServiceException, ObjectNotFoundException {
    DashboardConfigModel model = new DashboardConfigModel();
    if (config == null) {
      config = new DashboardConfig();
    }
    DomainConfig dc = DomainConfig.getInstance(domainId);
    List<String> val = dc.getDomainData(ConfigConstants.DASHBOARD);
    if (val != null) {
      model.createdBy = val.get(0);
      model.lastUpdated =
          LocalDateUtil.format(new Date(Long.parseLong(val.get(1))), locale, timezone);
      model.fn = getFullName(model.createdBy, locale);
    }
    model.ape =
        config.getActivityPanelConfig() != null && config
            .getActivityPanelConfig().showActivityPanel;
    model.rpe =
        config.getRevenuePanelConfig() != null && config.getRevenuePanelConfig().showRevenuePanel;
    model.ope = config.getOrderPanelConfig() != null && config.getOrderPanelConfig().showOrderPanel;
    model.ipe =
        config.getInventoryPanelConfig() != null && config.getInventoryPanelConfig().showInvPanel;
    if (config.getDbOverConfig() != null) {
      model.dmtg = TagUtil.getTagsArray(config.getDbOverConfig().dmtg);
      model.dimtg = config.getDbOverConfig().dimtg;
      model.detg = config.getDbOverConfig().detg;
      model.aper =
          StringUtils.isBlank(config.getDbOverConfig().aper) ? "7" : config.getDbOverConfig().aper;
      model.dtt = config.getDbOverConfig().dtt;
      model.atdd = config.getDbOverConfig().atdd;
      model.edm = config.getDbOverConfig().edm;
      model.exet = TagUtil.getTagsArray(config.getDbOverConfig().exet);
      model.exts = TagUtil.getTagsArray(config.getDbOverConfig().exts);
      model.dutg = TagUtil.getTagsArray(config.getDbOverConfig().dutg);
    }
    return model;
  }

  public OrdersConfigModel buildOrderConfigModel(HttpServletRequest request, Long domainId,
                                                 Locale locale, String timezone)
      throws ConfigurationException, ServiceException, ObjectNotFoundException {
    DomainConfig dc = DomainConfig.getInstance(domainId);
    OrdersConfigModel model = new OrdersConfigModel();
    if (dc == null) {
      throw new ConfigurationException();
    }
    OrdersConfig oc = dc.getOrdersConfig();
    DemandBoardConfig dbc = dc.getDemandBoardConfig();

    List<String> val = dc.getDomainData(ConfigConstants.ORDERS);
    if (val != null) {
      model.createdBy = val.get(0);
      model.lastUpdated =
          LocalDateUtil.format(new Date(Long.parseLong(val.get(1))), locale, timezone);
      model.fn = getFullName(model.createdBy, locale);
    }

    model.og = dc.getOrderGeneration();
    model.agi = dc.autoGI();
    model.tm = dc.isTransporterMandatory();
    model.tiss = dc.isTransporterInStatusSms();
    model.ao = dc.allowEmptyOrders();
    model.aof = dc.allowMarkOrderAsFulfilled();
    model.dop = dc.isDisableOrdersPricing();
    if (dc.getPaymentOptions() != null) {
      model.po = dc.getPaymentOptions();
    }
    if (dc.getPackageSizes() != null) {
      model.ps = dc.getPackageSizes();
    }
    if (dc.getVendorId() != null) {
      model.vid = String.valueOf(dc.getVendorId());
    }
    if (oc != null) {
      model.eex = oc.isExportEnabled();
      if (model.eex) {
        List<String> times = StringUtil.getList(oc.getExportTimes());
        List<String>
            dsTimes =
            LocalDateUtil.convertTimeStringList(times, dc.getTimezone(),
                false); // Convert times from UTC to domain specific timezone
        if (dsTimes != null && !dsTimes.isEmpty()) {
          model.et = StringUtil.getCSV(dsTimes);
        }
        //model.an = StringUtil.getCSV(oc.getExportUserIds());
        if (oc.getExportUserIds() != null) {
          model.an = oc.getExportUserIds();
        }
        if (oc.getUserTags() != null && !oc.getUserTags().isEmpty()) {
          model.usrTgs = oc.getUserTags();
        }
      }
      model.enOrdRsn = oc.isReasonsEnabled();
      model.orsn = oc.getOrderReason();
      model.md = oc.isMandatory();
      model.aoc = oc.allowSalesOrderAsConfirmed();
      model.asc = oc.allocateStockOnConfirmation();
      model.tr = oc.isTransferRelease();
      model.orr =
          StringUtils.isNotBlank(oc.getOrderRecommendationReasons()) ? oc
              .getOrderRecommendationReasons() : null;
      model.orrm = oc.getOrderRecommendationReasonsMandatory();
      model.eqr =
          StringUtils.isNotBlank(oc.getEditingQuantityReasons()) ? oc.getEditingQuantityReasons()
              : null;
      model.eqrm = oc.getEditingQuantityReasonsMandatory();
      model.psr =
          StringUtils.isNotBlank(oc.getPartialShipmentReasons()) ? oc.getPartialShipmentReasons()
              : null;
      model.psrm = oc.getPartialShipmentReasonsMandatory();
      model.pfr =
          StringUtils.isNotBlank(oc.getPartialFulfillmentReasons()) ? oc
              .getPartialFulfillmentReasons() : null;
      model.pfrm = oc.getPartialFulfillmentReasonsMandatory();
      model.cor =
          StringUtils.isNotBlank(oc.getCancellingOrderReasons()) ? oc.getCancellingOrderReasons()
              : null;
      model.corm = oc.getCancellingOrderReasonsMandatory();
      //model.acs = oc.isAllowCreatingShipments();
           /* if (oc.getExportUserIds() != null) {
                model.an = oc.getExportUserIds();
            }*/
      model.aafmsc = oc.autoAssignFirstMatStOnConfirmation();
    }
    if (dbc != null) {
      if (dbc.isPublic()) {
        model.ip = "p";
      } else {
        model.ip = "r";
      }
      if (dbc.getBanner() != null) {
        model.bn = dbc.getBanner();
      }
      if (dbc.getHeading() != null) {
        model.hd = dbc.getHeading();
      }
      if (dbc.getCopyright() != null) {
        model.cp = dbc.getCopyright();
      }
      model.spb = dbc.showStock();
      model.url = "https://" + request.getServerName() + "/pub/demand?id=" + domainId;
    }
    return model;
  }

  /**
   * Builds collection of unique reasons configured in the given domains inventory configuration. Trims and removes
   * any empty reason codes.
   *
   * @param ic - Inventory config of the domain
   * @return unique collection of reason codes configured
   */
  public Collection<String> buildUniqueTransactionReasons(InventoryConfig ic) {
    Set<String> reasons = new HashSet<>(1);
    addAllReasons(reasons, ic.getTransReasons());
    if (ic.isCimt()) {
      addAllReasons(reasons, ic.getImTransReasons());
    }
    if (ic.isCrmt()) {
      addAllReasons(reasons, ic.getRmTransReasons());
    }
    if (ic.isCsmt()) {
      addAllReasons(reasons, ic.getSmTransReasons());
    }
    if (ic.isCtmt()) {
      addAllReasons(reasons, ic.getTmTransReasons());
    }
    if (ic.isCdmt()) {
      addAllReasons(reasons, ic.getDmTransReasons());
    }
    reasons.remove(Constants.EMPTY);
    return reasons;
  }

  private void addAllReasons(Set<String> reasons, Map<String, String> configuredReasons) {
    if (configuredReasons != null) {
      for (String reasonCSV : configuredReasons.values()) {
        List<String> reasonsList = StringUtil.getList(reasonCSV);
        if (reasonsList != null) {
          reasons.addAll(reasonsList);
        }
      }
    }
  }

  public SupportConfigModel buildSCModelForWebDisplay(HttpServletRequest request)
      throws ServiceException, ObjectNotFoundException, ConfigurationException {
    SecureUserDetails
        sUser =
        SecurityMgr.getUserDetails(request.getSession());
    Locale locale = sUser.getLocale();
    ResourceBundle backendMessages = Resources.get().getBundle("BackendMessages", locale);
    String userId = sUser.getUsername();
    Long domainId = SessionMgr.getCurrentDomain(request.getSession(), userId);
    String timezone = sUser.getTimezone();
    try {
      GeneralConfigModel generalConfigModel = buildGeneralConfigModel(domainId, locale, timezone);
      List<SupportConfigModel> supportConfigModels = generalConfigModel.support;
      for (SupportConfigModel supportConfigModel : supportConfigModels) {
        if (sUser.getRole().equals(supportConfigModel.role)) {
          if (StringUtils.isEmpty(supportConfigModel.em) || StringUtils
              .isEmpty(supportConfigModel.phnm)) {
            SupportConfigModel scm = getAlternateSupportConfig(request);
            if (StringUtils.isEmpty(supportConfigModel.em)) {
              supportConfigModel.em = scm.em;
            }
            if (StringUtils.isEmpty(supportConfigModel.phnm)) {
              supportConfigModel.phnm = scm.phnm;
            }
          }
          return supportConfigModel;
        }
      }

      SupportConfigModel scm = getAlternateSupportConfig(request);
      return scm;
    } catch (ServiceException | ObjectNotFoundException | ConfigurationException e) {
      xLogger.severe("Error in fetching support configuration for the domain", e);
      throw new InvalidServiceException(
          backendMessages.getString("general.support.config.fetch.error"));
    }
  }

  private SupportConfigModel getAlternateSupportConfig(HttpServletRequest request) {
    // Get support configuration from System configuration ("generalconfig")
    SupportConfigModel scm = getSupportFromSystemConfig(request);
    if (StringUtils.isEmpty(scm.em) || StringUtils.isEmpty(scm.phnm)) {
      // Get default support configuration from samaanguru.properties
      SupportConfigModel defScm = getSupportConfigFromProperties();
      if (StringUtils.isEmpty(scm.em)) {
        scm.em = defScm.em;
      }
      if (StringUtils.isEmpty(scm.phnm)) {
        scm.phnm = defScm.phnm;
      }
    }
    return scm;
  }

  private SupportConfigModel getSupportFromSystemConfig(HttpServletRequest request) {
    SecureUserDetails user = SecurityUtils.getUserDetails(request);
    Locale locale = user.getLocale();
    SupportConfigModel scm = new SupportConfigModel();

    ResourceBundle backendMessages = Resources.get().getBundle("BackendMessages", locale);
    try {
      ConfigurationMgmtService cms;
      cms = Services.getService(ConfigurationMgmtServiceImpl.class, locale);
      IConfig c = cms.getConfiguration(IConfig.GENERALCONFIG);
      if (c != null && c.getConfig() != null) {
        String jsonString = c.getConfig();
        JSONObject jsonObject;
        try {
          jsonObject = new JSONObject(jsonString);
        } catch (JSONException e) {
          xLogger.severe("Error while getting generalconfig from System configuration");
          throw new InvalidServiceException(
              backendMessages.getString("general.config.fetch.error"));
        }
        try {
          scm.em = jsonObject.getString("supportemail");
        } catch (JSONException e) {
          xLogger.warn(
              "Ignoring JSONException while getting supportemail from system configuration due to {0}",
              e.getMessage(), e);
        }
        try {
          scm.phnm = jsonObject.getString("supportphone");
        } catch (JSONException e) {
          xLogger.warn(
              "Ignoring JSONException while getting supportphone from system configuration due to {0}",
              e.getMessage(), e);
        }
      }
    } catch (ServiceException e) {
      xLogger.severe("Error while getting generalconfig from System configuration");
      throw new InvalidServiceException(backendMessages.getString("general.config.fetch.error"));
    } catch (ObjectNotFoundException e) {
      xLogger.severe("Error while getting generalconfig from System configuration");
      throw new InvalidServiceException(backendMessages.getString("general.config.fetch.error"));
    }
    return scm;
  }

  private SupportConfigModel getSupportConfigFromProperties() {
    SupportConfigModel scm = new SupportConfigModel();
    scm.em = ConfigUtil.get("support.email", GeneralConfig.DEFAULT_SUPPORT_EMAIL);
    scm.phnm = ConfigUtil.get("support.phone", GeneralConfig.DEFAULT_SUPPORT_PHONE);
    return scm;
  }

  private List<IUserAccount> constructUserAccount(UsersService as, List<String> userIds) {
    if (userIds != null && !userIds.isEmpty()) {
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
}

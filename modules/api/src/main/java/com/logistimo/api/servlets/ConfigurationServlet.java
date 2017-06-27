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

import com.logistimo.AppFactory;
import com.logistimo.auth.SecurityConstants;
import com.logistimo.auth.SecurityMgr;
import com.logistimo.auth.utils.SessionMgr;
import com.logistimo.config.entity.IConfig;
import com.logistimo.config.models.AccountingConfig;
import com.logistimo.config.models.AssetConfig;
import com.logistimo.config.models.BBoardConfig;
import com.logistimo.config.models.CapabilityConfig;
import com.logistimo.config.models.ConfigurationException;
import com.logistimo.config.models.CustomReportsConfig;
import com.logistimo.config.models.DashboardConfig;
import com.logistimo.config.models.DashboardConfig.ActivityPanelConfig;
import com.logistimo.config.models.DashboardConfig.InventoryPanelConfig;
import com.logistimo.config.models.DashboardConfig.OrderPanelConfig;
import com.logistimo.config.models.DashboardConfig.RevenuePanelConfig;
import com.logistimo.config.models.DemandBoardConfig;
import com.logistimo.config.models.DomainConfig;
import com.logistimo.config.models.EventsConfig;
import com.logistimo.config.models.FieldsConfig;
import com.logistimo.config.models.InventoryConfig;
import com.logistimo.config.models.InventoryConfig.BatchMgmt;
import com.logistimo.config.models.InventoryConfig.ManualTransConfig;
import com.logistimo.config.models.KioskConfig;
import com.logistimo.config.models.OptimizerConfig;
import com.logistimo.config.models.OrdersConfig;
import com.logistimo.config.models.PaymentsConfig;
import com.logistimo.config.models.PaymentsConfig.PaymentConfig;
import com.logistimo.config.models.StockboardConfig;
import com.logistimo.config.service.ConfigurationMgmtService;
import com.logistimo.config.service.impl.ConfigurationMgmtServiceImpl;
import com.logistimo.constants.Constants;
import com.logistimo.customreports.CustomReportConstants;
import com.logistimo.dao.JDOUtils;
import com.logistimo.domains.CopyConfigModel;
import com.logistimo.entity.IUploaded;
import com.logistimo.inventory.entity.ITransaction;
import com.logistimo.logger.XLog;
import com.logistimo.orders.entity.IOrder;
import com.logistimo.proto.JsonTagsZ;
import com.logistimo.security.SecureUserDetails;
import com.logistimo.services.ObjectNotFoundException;
import com.logistimo.services.ServiceException;
import com.logistimo.services.Services;
import com.logistimo.services.UploadService;
import com.logistimo.services.blobstore.BlobInfo;
import com.logistimo.services.blobstore.BlobstoreService;
import com.logistimo.services.cache.MemcacheService;
import com.logistimo.services.impl.UploadServiceImpl;
import com.logistimo.tags.TagUtil;
import com.logistimo.utils.NumberUtil;
import com.logistimo.utils.StringUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.StringTokenizer;

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
    // Get the type of config
    String type = req.getParameter("type");
    // Get the user's locale
    SecureUserDetails sUser = SecurityMgr.getUserDetails(req.getSession());
    Locale locale = null;
    if (sUser != null) {
      locale = sUser.getLocale();
    }
    // Get config. service
    ConfigurationMgmtService
        cms =
        Services.getService(ConfigurationMgmtServiceImpl.class, locale);
    if (action == null || action.isEmpty()) {
      if ("general".equals(type)) {
        updateGeneralConfiguration(req, resp, sUser, cms, backendMessages);
      } else if ("tags".equals(type)) {
        updateTagConfiguration(req, resp, sUser, cms, backendMessages);
      } else if ("capabilities".equals(type)) {
        updateCapabilitiesConfiguration(req, resp, sUser, cms, backendMessages);
      } else if ("orders".equals(type)) {
        updateOrdersConfig(req, resp, sUser, cms, backendMessages);
      } else if ("inventory".equals(type)) {
        updateInventoryConfig(req, resp, sUser, cms, backendMessages);
      } else if ("notifications".equals(type)) {
        updateNotificationsConfiguration(req, resp, sUser, cms, backendMessages);
      } else if ("bboard".equals(type)) {
        updateBBoardConfiguration(req, resp, sUser, cms, backendMessages);
      } else if ("accounting".equals(type)) {
        updateAccountingConfiguration(req, resp, sUser, cms, backendMessages);
      } else if ("customreports".equals(type)) {
        updateCustomReportsConfiguration(req, resp, sUser, cms, backendMessages);
      } else if ("kioskconfig".equals(type)) {
        updateKioskConfig(req, resp, sUser, cms, backendMessages);
      } else if ("payments".equals(type)) {
        updatePaymentsConfiguration(req, resp, sUser, cms, backendMessages);
      } else if ("temperature".equals(type)) {
        updateTemperatureConfiguration(req, resp, sUser, cms, backendMessages);
      } else if ("dashboard".equals(type)) {
        updateDashboardConfiguration(req, resp, sUser, cms, backendMessages);
      } else {
        xLogger.severe("Invalid configuration type: " + type);
      }
    } else if (ACTION_COPYDOMAINCONFIG.equals(action)) {
      copyDomainConfig(req);
    }

    xLogger.fine("Exiting processPost");
  }

  // Update the demand board configuration
  private void updateOrdersConfig(HttpServletRequest req, HttpServletResponse resp,
                                  SecureUserDetails sUser, ConfigurationMgmtService cms,
                                  ResourceBundle backendMessages)
      throws ServiceException, IOException {
    xLogger.fine("Entered updateOrdersConfig");
    // Get the request parameters
    String orderGen = req.getParameter("ordergeneration");
    String tax = req.getParameter("tax");
    String isPublicStr = req.getParameter("ispublic");
    String banner = req.getParameter("banner");
    String heading = req.getParameter("heading");
    String copyright = req.getParameter("copyright");
    String domainIdStr = req.getParameter("domainid");
    String autoGI = req.getParameter("autoGI"); // auto goods issue on order shipped
    String autoGR = req.getParameter("autoGR"); // auto goods receipt on order fulfilled
    String showStockStr = req.getParameter("showstock"); // show stock levels on public demand board
    String transporterMandatory = req.getParameter("transportermandatory");
    String transporterInStatusSms = req.getParameter("transporterinstatussms");
    String allowEmptyOrders = req.getParameter("allowemptyorders");
    String allowMarkOrderAsFulfilled = req.getParameter("allowmarkorderasfulfilled");
    String paymentOptions = req.getParameter("paymentoptions");
    String packageSizes = req.getParameter("packagesizes");
    String vendorIdStr = req.getParameter("vendorid"); // default vendor
    // Order export schedule
    boolean enableExport = req.getParameter("exportorders") != null;
    String exportTimesCSV = req.getParameter("exporttimes");
    if (exportTimesCSV != null && exportTimesCSV.isEmpty()) {
      exportTimesCSV = null;
    }
    String exportUserIdsCSV = req.getParameter("exportuserids");
    if (exportUserIdsCSV != null && exportUserIdsCSV.isEmpty()) {
      exportUserIdsCSV = null;
    }
    // Validate schedule data
    if (enableExport && exportUserIdsCSV == null) {
      String
          msg =
          "Sorry, users were not specified in the orders export schedule. Please go back and complete the orders export schedule [<a onclick=\"history.go(-1)\">back</a>]";
      writeToScreen(req, resp, msg, Constants.VIEW_CONFIGURATION);
      return;
    }
    Long domainId = null;
    String message = null;
    try {
      // Get user id
      String userId = sUser.getUsername();
      // Get the domain id
      domainId = Long.valueOf(domainIdStr);
      ConfigContainer cc = getDomainConfig(domainId, userId, cms);
      // Get the demand board config.
      DemandBoardConfig dbc = cc.dc.getDemandBoardConfig();
      if (dbc == null) {
        dbc = new DemandBoardConfig();
        cc.dc.setDemandBoardConfig(dbc);
      }
      // Update orders config
      if (orderGen != null && !orderGen.isEmpty()) {
        cc.dc.setOrderGeneration(orderGen);
      }
      if (tax != null) {
        if (tax.isEmpty()) {
          cc.dc.setTax(0);
        } else {
          cc.dc.setTax(Float.parseFloat(tax));
        }
      }
      // Update autoGI and autoGR
      cc.dc.setAutoGI(autoGI != null);
      cc.dc.setAutoGR(autoGR != null);
      // Set transporter flags
      cc.dc.setTransporterMandatory(transporterMandatory != null);
      cc.dc.setTransporterInStatusSms(transporterInStatusSms != null);
      // Set the allow empty orders field
      cc.dc.setAllowEmptyOrders(allowEmptyOrders != null);
      // Set allow mark order as fulfilled flag
      cc.dc.setAllowMarkOrderAsFulfilled(allowMarkOrderAsFulfilled != null);
      if (paymentOptions != null && !paymentOptions.isEmpty()) {
        cc.dc.setPaymentOptions(paymentOptions);
      } else {
        cc.dc.setPaymentOptions(null);
      }
      if (packageSizes != null && !packageSizes.isEmpty()) {
        cc.dc.setPackageSizes(packageSizes);
      } else {
        cc.dc.setPackageSizes(null);
      }
      // Vendor Id
      if (vendorIdStr != null && !vendorIdStr.isEmpty()) {
        cc.dc.setVendorid(Long.valueOf(vendorIdStr));
      } else {
        cc.dc.setVendorid(null);
      }
      // Get the fields config., if any
      FieldsConfig fc = new FieldsConfig();
      for (int i = 0; i < FieldsConfig.MAX_ORDERFIELDS; i++) {
        FieldsConfig.Field f = new FieldsConfig.Field();
        f.name = req.getParameter("field_name_" + i);
        if (f.name == null || f.name.trim().isEmpty()) {
          continue;
        }
        f.name = f.name.trim();
        f.type = req.getParameter("field_type_" + i);
        try {
          String sizeStr = req.getParameter("field_maxsize_" + i);
          if (sizeStr != null && !sizeStr.isEmpty()) {
            f.maxSize = Integer.parseInt(sizeStr);
          }
        } catch (NumberFormatException e) {
          // do nothing
          xLogger.warn("Exception while parsing size", e);
        }
        f.value = req.getParameter("field_value_" + i);
        if (f.value != null) {
          f.value = f.value.trim();
        }
        f.statusTrigger = req.getParameter("field_statustrigger_" + i);
        f.mandatory = req.getParameter("field_mandatory_" + i) != null;
        f.useInTemplates =
            f.mandatory; // if mandatory, only then use in templates (this is to ensure that there are no dangling variables in a message when a user does not fill a mandatory field)
        ///f.useInTemplates = ( req.getParameter( "field_useintemplates_" + i ) != null );
        fc.putField(f);
      }
      if (!fc.isEmpty()) {
        cc.dc.setOrderFields(fc);
      } else {
        cc.dc.setOrderFields(null);
      }
      // Order export schedule
      OrdersConfig oc = cc.dc.getOrdersConfig();
      if (oc == null) {
        oc = new OrdersConfig();
        cc.dc.setOrdersConfig(oc);
      }
      // Set order export schedule, if specified
      oc.setExportEnabled(enableExport);
      if (exportTimesCSV != null) {
        exportTimesCSV =
            StringUtil.getCSV(StringUtil
                .getList(exportTimesCSV)); // gets a trimmed CSV, in case user specified spaces
      }
      oc.setExportTimes(exportTimesCSV);
      oc.setExportUserIds(exportUserIdsCSV);
      oc.setSourceUserId(userId);
      // Update the demand board config.
      dbc.setIsPublic("true".equals(isPublicStr));
      dbc.setBanner(banner);
      dbc.setHeading(heading);
      dbc.setCopyright(copyright);
      dbc.setShowStock(showStockStr != null);
      // Update the config.
      saveDomainConfig(cc, domainId, cms);
      message =
          backendMessages.getString("config.success")
              + " &nbsp;[<a href=\"/s/config/configuration.jsp?subview=orders\">" + backendMessages
              .getString("config.view") + "</a>]";
    } catch (ConfigurationException e) {
      xLogger.severe("Configuration exception: {0} : {1}", e.getClass().getName(), e.getMessage());
      throw new ServiceException(backendMessages.getString("config.invalid"));
    }

    writeToScreen(req, resp, message, Constants.VIEW_CONFIGURATION);

    xLogger.fine("Exiting updateOrdersConfig");
  }

  // Add or modify domain configuration
  private void updateGeneralConfiguration(HttpServletRequest req, HttpServletResponse resp,
                                          SecureUserDetails sUser, ConfigurationMgmtService cms,
                                          ResourceBundle backendMessages)
      throws ServiceException, IOException {
    xLogger.fine("Entered updateGeneralConfiguration, req: " + req);
    // Read configuration parameters from the form
    String domainIdStr = req.getParameter("domainid");
    String country = req.getParameter("country");
    String state = req.getParameter("state");
    String language = req.getParameter("language");
    String timezone = req.getParameter("timezone");
    String currency = req.getParameter("currency");
    ///String tags = req.getParameter( "tags" );
    ///String forcetags = req.getParameter( "forcetags" );
    String pageHeader = req.getParameter("pageheader");
    // Get the uipref
    String uiPrefStr = req.getParameter("uipref");
    // Get the onlynewui parameter
    String onlyNewUIStr = req.getParameter("onlynewui");
    boolean uiPref = "true".equals(uiPrefStr); // else false
    boolean onlyNewUI = "true".equals(onlyNewUIStr); // else false

    String userIdStr = req.getParameter("userid");
    boolean isCalledAsTask = false;
    String userId = null;
    if (req.getRequestURI().contains("/task/")) {
      isCalledAsTask = true;
      userId = userIdStr;
    } else {
      userId = sUser.getUsername();
    }

    // Get existing configuration, if any
    ConfigContainer cc = null;
    String message = null;
    Long domainId = null;
    if (domainIdStr != null && !domainIdStr.isEmpty()) {
      domainId = Long.valueOf(domainIdStr);
    }
    try {
      cc = getDomainConfig(domainId, userId, cms);
    } catch (ConfigurationException e) {
      xLogger.severe("Invalid format of configuration for domain {0}: {1}", domainIdStr,
          e.getMessage());
      throw new ServiceException(backendMessages.getString("config.invalid"));
    }
    AssetConfig tc = cc.dc.getAssetConfig();
    if (country != null) {
      cc.dc.setCountry(country);
      if (tc != null && tc.getConfiguration() != null) {
        tc.getConfiguration().getLocale().setCn(country);
      }
    }

    if (state != null) {
      cc.dc.setState(state);
    }

    if (language != null) {
      cc.dc.setLanguage(language);
      if (tc != null && tc.getConfiguration() != null) {
        tc.getConfiguration().getLocale().setLn(language);
      }
    }

    if (timezone != null) {
      cc.dc.setTimezone(timezone);
      if (tc != null && tc.getConfiguration() != null) {
        tc.getConfiguration().getLocale().setTz(tc.getTimezoneOffset(timezone));
      }
    }

    if (currency != null) {
      cc.dc.setCurrency(currency);
    }
                /*
                if ( tags != null ) {
			cc.dc.setTags( tags );
			cc.dc.setForceTags( "true".equals( forcetags ) );
		}
		*/
    if (pageHeader != null) {
      cc.dc.setPageHeader(pageHeader);
    }

    // Set the ui preference
    cc.dc.setUiPreference(uiPref);

    // Set the onlyNewUI flag
    cc.dc.setOnlyNewUI(onlyNewUI);
    // Get the configuration string and add/update
    try {
      saveDomainConfig(cc, domainId, cms);
      // Success
      message =
          backendMessages.getString("config.success")
              + " &nbsp;[<a href=\"/s/config/configuration.jsp\">" + backendMessages
              .getString("config.view") + "</a>]";
    } catch (ConfigurationException e) {
      xLogger.severe("Invalid format of configuration for domain {0}: {1}", domainIdStr,
          e.getMessage());
      throw new ServiceException(backendMessages.getString("config.invalid"));
    }

    xLogger.fine("Exiting addOrModifyDomainConfiguration");
    if (!isCalledAsTask) // Only if not called as a task, write to screen.
    {
      writeToScreen(req, resp, message, Constants.VIEW_CONFIGURATION);
    }
  }

  // Add or modify tag configuration
  private void updateTagConfiguration(HttpServletRequest req, HttpServletResponse resp,
                                      SecureUserDetails sUser, ConfigurationMgmtService cms,
                                      ResourceBundle backendMessages)
      throws ServiceException, IOException {
    xLogger.fine("Entered updateTagConfiguration");
    // Read configuration parameters from the form
    String domainIdStr = req.getParameter("domainid");
    String tagsMaterial = req.getParameter("mtags");
    String forcetagsMaterial = req.getParameter("forcetagsm");
    String tagsKiosk = req.getParameter("ktags");
    String forcetagsKiosk = req.getParameter("forcetagsk");
    String tagsOrder = req.getParameter("otags");
    String forcetagsOrder = req.getParameter("forcetagso");
    String routeTags = req.getParameter("routetags");
    String routeBy = req.getParameter("routeby");
    String tagsUser = req.getParameter("utags");
    String forcetagsUser = req.getParameter("forcetagsu");
    // Get the user Id
    String userId = null;
    if (sUser != null) {
      userId = sUser.getUsername();
    }
    // Is this called from browser or task (say, from BulkUploadMgr)
    boolean isTask = req.getRequestURI().contains("/task/");
    xLogger.fine("isTask: {0}, uri: {1}", isTask, req.getRequestURI());
    // Get existing configuration, if any
    ConfigContainer cc = null;
    String message = null;
    Long domainId = null;
    if (domainIdStr != null && !domainIdStr.isEmpty()) {
      domainId = Long.valueOf(domainIdStr);
    }
    try {
      cc = getDomainConfig(domainId, userId, cms);
    } catch (ConfigurationException e) {
      xLogger.severe("Invalid format of configuration for domain {0}: {1}", domainIdStr,
          e.getMessage());
      throw new ServiceException(backendMessages.getString("config.invalid"));
    }
    // Material tags
    if (tagsMaterial != null) {
      cc.dc.setMaterialTags(TagUtil.getCleanTags(tagsMaterial, true));
      cc.dc.setForceTagsMaterial("true".equals(forcetagsMaterial));
    }
    // Kiosk tags
    if (tagsKiosk != null) {
      cc.dc.setKioskTags(TagUtil.getCleanTags(tagsKiosk, true));
      cc.dc.setForceTagsKiosk("true".equals(forcetagsKiosk));
    }
    // Order tags
    if (tagsOrder != null) {
      cc.dc.setOrderTags(TagUtil.getCleanTags(tagsOrder, true));
      cc.dc.setForceTagsOrder("true".equals(forcetagsOrder));
    }
    // Entity route tags
    if (routeTags != null) {
      cc.dc.setRouteTags(TagUtil.getCleanTags(routeTags, false));
    }
    if (routeBy != null) {
      cc.dc.setRouteBy(routeBy);
    }
    // User tags
    if (tagsUser != null) {
      cc.dc.setUserTags(TagUtil.getCleanTags(tagsUser, true));
      cc.dc.setForceTagsUser("true".equals(forcetagsUser));
    }
    // Get the configuration string and add/update
    try {
      saveDomainConfig(cc, domainId, cms);
      // Success
      message =
          backendMessages.getString("config.success")
              + " &nbsp;[<a href=\"/s/config/configuration.jsp?subview=tags\">" + backendMessages
              .getString("config.view") + "</a>]";
    } catch (ConfigurationException e) {
      xLogger.severe("Invalid format of configuration for domain {0}: {1}", domainIdStr,
          e.getMessage());
      if (!isTask) {
        throw new ServiceException(backendMessages.getString("config.invalid"));
      }
    }

    xLogger.fine("Exiting updateTagConfiguration");
    if (!isTask) {
      writeToScreen(req, resp, message, Constants.VIEW_CONFIGURATION);
    }
  }

  private void updateCapabilitiesConfiguration(HttpServletRequest req, HttpServletResponse resp,
                                               SecureUserDetails sUser,
                                               ConfigurationMgmtService cms,
                                               ResourceBundle backendMessages)
      throws ServiceException, IOException {
    xLogger.fine("Entered updateCapabilitiesConfiguration");
    // Read configuration parameters from the form
    String domainIdStr = req.getParameter("domainid");
    String[] capabilities = req.getParameterValues("capabilities");
    String transNaming = req.getParameter("transactionnaming");
    // Role-specific configs.
    String role = req.getParameter("role");
    String[] transMenu = req.getParameterValues("transactionmenu");
    String tagsInventory = req.getParameter("tagsinventory");
    String tagsOrders = req.getParameter("tagsorders");
    String sendVendors = req.getParameter("sendvendors");
    String sendCustomers = req.getParameter("sendcustomers");
    String geoCodingStrategy = req.getParameter("geocodingstrategy");
    String[] creatableEntityTypesArray = req.getParameterValues("creatableentitytypes");
    boolean allowRouteTagEditing = req.getParameter("allowroutetagediting") != null;
    boolean loginAsReconnect = req.getParameter("loginasreconnect") != null;
    List<String> creatableEntityTypes = null;
    if (creatableEntityTypesArray != null && creatableEntityTypesArray.length > 0) {
      creatableEntityTypes = StringUtil.getList(creatableEntityTypesArray);
    }
    // Get the user Id
    String userId = sUser.getUsername();
    // Get existing configuration, if any
    String message = null;
    Long domainId = null;
    if (domainIdStr != null && !domainIdStr.isEmpty()) {
      domainId = Long.valueOf(domainIdStr);
    }
    ConfigContainer cc = null;
    try {
      cc = getDomainConfig(domainId, userId, cms);
    } catch (ConfigurationException e) {
      xLogger.severe("Invalid format of configuration for domain {0}: {1}", domainIdStr,
          e.getMessage());
      throw new ServiceException(backendMessages.getString("config.invalid"));
    }

    if (transNaming != null && !transNaming.isEmpty()) {
      cc.dc.setTransactionNaming(transNaming);
    }
    // Capabilities - either generic or specific to a role, if present
    if (role != null && !role.isEmpty()) {
      CapabilityConfig cconf = new CapabilityConfig();
      cconf.setCapabilities(req.getParameterValues("transactionmenu_" + role));
      tagsInventory = req.getParameter("tagsinventory_" + role);
      if (tagsInventory != null) {
        cconf.setTagsInventory(tagsInventory);
      }
      tagsOrders = req.getParameter("tagsorders_" + role);
      if (tagsOrders != null) {
        cconf.setTagsOrders(tagsOrders);
      }
      sendVendors = req.getParameter("sendvendors_" + role);
      if (sendVendors != null) // vendors selected
      {
        cconf.setSendVendors(true);
      } else {
        cconf.setSendVendors(false);
      }
      sendCustomers = req.getParameter("sendcustomers_" + role);
      geoCodingStrategy = req.getParameter("geocodingstrategy_" + role);
      creatableEntityTypesArray = req.getParameterValues("creatableentitytypes_" + role);
      if (creatableEntityTypesArray != null && creatableEntityTypesArray.length > 0) {
        creatableEntityTypes = StringUtil.getList(creatableEntityTypesArray);
      } else {
        creatableEntityTypes = null;
      }
      allowRouteTagEditing = req.getParameter("allowroutetagediting_" + role) != null;
      loginAsReconnect = req.getParameter("loginasreconnect_" + role) != null;
      if (sendCustomers != null) {
        cconf.setSendCustomers(true);
      } else {
        cconf.setSendCustomers(false);
      }
      if (geoCodingStrategy != null) {
        cconf.setGeoCodingStrategy(geoCodingStrategy);
      }
      cconf.setCreatableEntityTypes(creatableEntityTypes);
      cconf.setAllowRouteTagEditing(allowRouteTagEditing);
      cconf.setLoginAsReconnect(loginAsReconnect);
      // Update DomainConfig with CapabilityConfig for this role
      cc.dc.setCapabilityByRole(role, cconf);
    } else { // generic (as before - keeps backward compatibility)
      cc.dc.setCapabilities(capabilities);
      cc.dc.setTransactionMenus(transMenu);
      if (tagsInventory != null) {
        cc.dc.setTagsInventory(TagUtil.getCleanTags(tagsInventory, true));
      }
      if (tagsOrders != null) {
        cc.dc.setTagsOrders(TagUtil.getCleanTags(tagsOrders, true));
      }
      // Relationship data parameters
      if (sendVendors
          != null) // vendors selected  /// earlier && !cc.dc.isCapabilityDisabled( DomainConfig.CAPABILITY_ORDERS ) ) // vendors selected and orders enabled
      {
        cc.dc.setSendVendors(true);
      } else {
        cc.dc.setSendVendors(false);
      }
      if (sendCustomers != null) {
        cc.dc.setSendCustomers(true);
      } else {
        cc.dc.setSendCustomers(false);
      }
      if (geoCodingStrategy != null) {
        cc.dc.setGeoCodingStrategy(geoCodingStrategy);
      }
      cc.dc.setCreatableEntityTypes(creatableEntityTypes);
      cc.dc.setAllowRouteTagEditing(allowRouteTagEditing);
      cc.dc.setLoginAsReconnect(loginAsReconnect);
    }
    // Get the configuration string and add/update
    try {
      saveDomainConfig(cc, domainId, cms);
      // Success
      message =
          backendMessages.getString("config.success")
              + " &nbsp;[<a href=\"/s/config/configuration.jsp?subview=capabilities\">"
              + backendMessages.getString("config.view") + "</a>]";
    } catch (ConfigurationException e) {
      xLogger.severe("Invalid format of configuration for domain {0}: {1}", domainIdStr,
          e.getMessage());
      throw new ServiceException(backendMessages.getString("config.invalid"));
    }

    xLogger.fine("Exiting updateCapabilitiesConfiguration");
    writeToScreen(req, resp, message, Constants.VIEW_CONFIGURATION);
  }

  // Update optimization configuration
  private void updateInventoryConfig(HttpServletRequest req, HttpServletResponse resp,
                                     SecureUserDetails sUser, ConfigurationMgmtService cms,
                                     ResourceBundle backendMessages)
      throws ServiceException, IOException {
    xLogger.fine("Entered updateInventoryConfig");
    try {
      // Get the domain Id
      String userId = sUser.getUsername();
      Long domainId = SessionMgr.getCurrentDomain(req.getSession(), userId);
      // Get domain config.
      ConfigContainer cc = getDomainConfig(domainId, userId, cms);
      // Get the transaction export schedule
      String enabledStr = req.getParameter("exporttrans");
      String exportTimes = req.getParameter("exporttimes");
      String exportUserIds = req.getParameter("exportuserids");
      String sourceUserId = req.getParameter("sourceuserid");
      // Get transaction reasons
      String issueReasons = trimReasons(req.getParameter("issuereasons"));
      String receiptReasons = trimReasons(req.getParameter("receiptreasons"));
      String stockcountReasons = trimReasons(req.getParameter("stockcountreasons"));
      String wastageReasons = trimReasons(req.getParameter("wastagereasons"));
      String transferReasons = trimReasons(req.getParameter("transferreasons"));
      // Permissions
      boolean invVisibleCustomers = req.getParameter("inventoryvisiblecustomers") != null;
      // Batch management
      int
          issueWithNoBatchData =
          (req.getParameter("issuewithnobatchdata") != null ? BatchMgmt.NO_BATCHDATA_ISSUE_FEFO
              : BatchMgmt.NO_BATCHDATA_ISSUE_NONE);
      // Inventory dashboard visibility
      boolean showInventoryDashboard = req.getParameter("showinventorydashboard") != null;

      // Form object
      InventoryConfig inventoryConfig = new InventoryConfig();
      // Set reasons
      Map<String, String> transReasons = new HashMap<String, String>();
      transReasons.put(ITransaction.TYPE_ISSUE, issueReasons);
      transReasons.put(ITransaction.TYPE_RECEIPT, receiptReasons);
      transReasons.put(ITransaction.TYPE_PHYSICALCOUNT, stockcountReasons);
      transReasons.put(ITransaction.TYPE_WASTAGE, wastageReasons);
      transReasons.put(ITransaction.TYPE_TRANSFER, transferReasons);
      inventoryConfig.setTransReasons(transReasons);
      // Reset the deprecated wastage reasons
      cc.dc.setWastageReasons(null);
      // Set transaction export params.
      inventoryConfig.setEnabled(enabledStr != null);
      if (exportTimes != null && !exportTimes.trim().isEmpty()) {
        inventoryConfig.setTimes(StringUtil.getList(exportTimes.trim()));
      } else {
        inventoryConfig.setTimes(null);
      }
      if (exportUserIds != null && !exportUserIds.isEmpty()) {
        inventoryConfig.setExportUsers(exportUserIds);
      }
      if (sourceUserId != null && !sourceUserId.isEmpty()) {
        inventoryConfig.setSourceUserId(sourceUserId);
      }
      // Get parameters for manual consumption rate
      int consumptionRate = Integer.parseInt(req.getParameter("cr"));
      String manualCRFreq = req.getParameter("manualcrfreq");
      if (manualCRFreq != null) {
        inventoryConfig.setManualCRFreq(manualCRFreq);
        xLogger.fine("Manual computation rate freq {0}", manualCRFreq);
      }

      // Get the parameters for manual upload of transaction and inventory metadata
      boolean enableManualUploadOfTransAndInvMetadata = false;
      String manualUploadOfTransAndInvMetadatStr = req.getParameter("manualuploadinvdataandtrans");
      if (manualUploadOfTransAndInvMetadatStr != null && !manualUploadOfTransAndInvMetadatStr
          .isEmpty()) {
        enableManualUploadOfTransAndInvMetadata = true;
      }
      boolean uploadPerSingleEntityOnly = req.getParameter("uploadpersingleentityonly") != null;
      ManualTransConfig manualTransConfig = new ManualTransConfig();
      manualTransConfig.enableManualUploadInvDataAndTrans = enableManualUploadOfTransAndInvMetadata;
      manualTransConfig.enableUploadPerEntityOnly = uploadPerSingleEntityOnly;
      inventoryConfig.setManualTransConfig(manualTransConfig);
      // Show inventory dashboard
      inventoryConfig.setShowInventoryDashboard(showInventoryDashboard);

      // Update in DC
      cc.dc.setInventoryConfig(inventoryConfig);
      // Update permissions
      InventoryConfig.Permissions perms = new InventoryConfig.Permissions();
      perms.invCustomersVisible = invVisibleCustomers;
      inventoryConfig.setPermissions(perms);
      // Update batch. mgmt. config
      BatchMgmt batchMgmt = new BatchMgmt();
      batchMgmt.issuePolicyNoBatchData = issueWithNoBatchData;
      inventoryConfig.setBatchMgmt(batchMgmt);
      // Get the optimization config
      OptimizerConfig oc = cc.dc.getOptimizerConfig();
      // Read form parameters
      String computeOption = req.getParameter("computeoption");
      int coption = OptimizerConfig.COMPUTE_NONE;
      if (computeOption != null && !computeOption.isEmpty()) {
        coption = Integer.parseInt(computeOption);
      }
      oc.setCompute(coption);
      // Get parameters for automatic consumption rate, if any
      if (consumptionRate == InventoryConfig.CR_AUTOMATIC) {
        String minHistoricalPeriod = req.getParameter("minhistoricalperiod_cr");
        if (minHistoricalPeriod != null && !minHistoricalPeriod.isEmpty()) {
          try {
            oc.setMinHistoricalPeriod(Float.parseFloat(minHistoricalPeriod));
          } catch (NumberFormatException e) {
            xLogger.warn("Invalid min. historical period for consumption rate: {0}",
                minHistoricalPeriod);
          }
        }
        // Get max. historical period
        String maxHistoricalPeriod = req.getParameter("maxhistoricalperiod_cr");
        if (maxHistoricalPeriod != null && !maxHistoricalPeriod.isEmpty()) {
          try {
            oc.setMaxHistoricalPeriod(Float.parseFloat(maxHistoricalPeriod));
          } catch (NumberFormatException e) {
            xLogger.warn("Invalid max. historical period for consumption rate: {0}",
                maxHistoricalPeriod);
          }
        }

        // Get parameters for demand forecasting
        if (coption == OptimizerConfig.COMPUTE_FORECASTEDDEMAND) {
          String avgPeriodicity = req.getParameter("avgorderperiodicity_fd");
          if (avgPeriodicity != null && !avgPeriodicity.isEmpty()) {
            try {
              oc.setMinAvgOrderPeriodicity(Float.parseFloat(avgPeriodicity));
            } catch (NumberFormatException e) {
              xLogger.warn("Invalid avg. periodicity for Demand Forecasting: {0}", avgPeriodicity);
            }
          }
          String numPeriods = req.getParameter("numorderperiods_fd");
          if (numPeriods != null && !numPeriods.isEmpty()) {
            try {
              oc.setNumPeriods(Float.parseFloat(numPeriods));
            } catch (NumberFormatException e) {
              xLogger
                  .warn("Invalid number of order periods for Demand Forecasting: {0}", numPeriods);
            }
          }
        } else if (coption == OptimizerConfig.COMPUTE_EOQ) {
          oc.setInventoryModel(req.getParameter("inventorymodel"));
          String avgPeriodicity = req.getParameter("avgorderperiodicity_eoq");
          if (avgPeriodicity != null && !avgPeriodicity.isEmpty()) {
            try {
              oc.setMinAvgOrderPeriodicity(Float.parseFloat(avgPeriodicity));
            } catch (NumberFormatException e) {
              xLogger.warn("Invalid avg. periodicity for EOQ: {0}", avgPeriodicity);
            }
          }
          String numPeriods = req.getParameter("numorderperiods_eoq");
          if (numPeriods != null && !numPeriods.isEmpty()) {
            try {
              oc.setNumPeriods(Float.parseFloat(numPeriods));
            } catch (NumberFormatException e) {
              xLogger.warn("Invalid number of order periods for EOQ: {0}", numPeriods);
            }
          }
          String leadTime = req.getParameter("leadtime");
          if (leadTime != null && !leadTime.isEmpty()) {
            try {
              oc.setLeadTimeDefault(Float.parseFloat(leadTime));
            } catch (NumberFormatException e) {
              xLogger.warn("Invalid lead time for EOQ: {0}", leadTime);
            }
          }
        }
      }
      if (consumptionRate == InventoryConfig.CR_AUTOMATIC) {
        String emails = req.getParameter("exceptionemail");
        if (emails != null && !emails.isEmpty()) {
          oc.setExceptionEmails(emails);
        }
      }
      // Save config
      String message = null;
      try {
        cc.dc.setOptimizerConfig(oc);
        saveDomainConfig(cc, domainId, cms);
        // Success
        message =
            backendMessages.getString("config.success")
                + " &nbsp;[<a href=\"/s/config/configuration.jsp?subview=inventory\">"
                + backendMessages.getString("config.view") + "</a>]";
      } catch (ConfigurationException e) {
        xLogger.severe("Invalid format of configuration for domain {0}: {1}", domainId,
            e.getMessage());
        throw new ServiceException(backendMessages.getString("config.invalid"));
      }
      xLogger.fine("Exiting updateInventoryConfig");
      writeToScreen(req, resp, message, Constants.VIEW_CONFIGURATION);
    } catch (ConfigurationException e) {
      xLogger.severe("Configuration exception: {0} : {1}", e.getClass().getName(), e.getMessage());
      throw new ServiceException(backendMessages.getString("config.invalid"));
    }
  }

  // Update the Messages configuration
  private void updateNotificationsConfiguration(HttpServletRequest req, HttpServletResponse resp,
                                                SecureUserDetails sUser,
                                                ConfigurationMgmtService cms,
                                                ResourceBundle backendMessages)
      throws ServiceException, IOException {
    xLogger.fine("Entered updateNotificationsConfiguration");
    try {
      // Read configuration parameters from the form
      String domainIdStr = req.getParameter("domainid");
      String jsonEventSpec = req.getParameter("eventspec");
      String errMsg = null;
      Long domainId = null;
      if (domainIdStr != null && !domainIdStr.isEmpty()) {
        try {
          domainId = Long.valueOf(domainIdStr);
        } catch (NumberFormatException e) {
          errMsg = "Invalid domain ID: " + domainIdStr;
        }
      } else {
        errMsg = "No domain ID specified";
      }
      if (jsonEventSpec == null || jsonEventSpec.isEmpty()) {
        errMsg = "Invalid event specification";
      }
      EventsConfig ec = null;
      try {
        ec = new EventsConfig(jsonEventSpec);
      } catch (JSONException e) {
        errMsg = "Invalid JSON event specification: " + e.getMessage();
      }
      if (errMsg != null) {
        JSONObject json = new JSONObject();
        try {
          json.put(JsonTagsZ.STATUS, JsonTagsZ.STATUS_FALSE);
          json.put(JsonTagsZ.MESSAGE, errMsg);
          sendJsonResponse(resp, json.toString());
        } catch (JSONException e) {
          xLogger.severe("JSONExeption when trying to send error msg: {0}", e.getMessage());
        }
        return;
      }
      String userId = sUser.getUsername();
      ConfigContainer cc = getDomainConfig(domainId, userId, cms);
      cc.dc.setEventsConfig(ec);
      // Save config
      try {
        saveDomainConfig(cc, domainId, cms);
        String jsonStr = "{ \"" + JsonTagsZ.STATUS + "\" : \"" + JsonTagsZ.STATUS_TRUE + "\" }";
        // Send JSON response
        sendJsonResponse(resp, jsonStr);
      } catch (ConfigurationException e) {
        xLogger.severe("Invalid format of configuration for domain {0}: {1}", domainId,
            e.getMessage());
        throw new ServiceException(backendMessages.getString("config.invalid"));
      }
      xLogger.fine("Exiting updateNotificationsConfiguration");
    } catch (ConfigurationException e) {
      xLogger.severe("Configuration exception: {0} : {1}", e.getClass().getName(), e.getMessage());
      throw new ServiceException(backendMessages.getString("config.invalid"));
    }
  }

  // Update the bulletin-board configuration
  private void updateBBoardConfiguration(HttpServletRequest req, HttpServletResponse resp,
                                         SecureUserDetails sUser, ConfigurationMgmtService cms,
                                         ResourceBundle backendMessages)
      throws ServiceException, IOException {
    xLogger.fine("Entered updateBBoardConfiguration");
    try {
      // Read configuration parameters from the form
      String enableStr = req.getParameter("enable");
      String dataDurationStr = req.getParameter("dataduration");
      String refreshDurationStr = req.getParameter("refreshduration");
      String scrollIntervalStr = req.getParameter("scrollinterval");
      boolean pauseOnHover = req.getParameter("pauseonhover") != null;
      boolean showNav = req.getParameter("shownav") != null;
      String maxItemsStr = req.getParameter("maxitems");
      int enable = BBoardConfig.PRIVATE;
      if (enableStr != null && !enableStr.isEmpty()) {
        enable = Integer.parseInt(enableStr);
      }
      int dataDuration = BBoardConfig.DATA_DURATION_DEFAULT;
      if (dataDurationStr != null && !dataDurationStr.isEmpty()) {
        dataDuration = Integer.parseInt(dataDurationStr);
      }
      int refreshDuration = BBoardConfig.REFRESH_DURATION_DEFAULT;
      if (refreshDurationStr != null && !refreshDurationStr.isEmpty()) {
        refreshDuration = Integer.parseInt(refreshDurationStr);
      }
      int scrollInterval = BBoardConfig.SCROLL_INTERVAL_DEFAULT;
      if (scrollIntervalStr != null && !scrollIntervalStr.isEmpty()) {
        scrollInterval = Integer.parseInt(scrollIntervalStr);
      }
      int maxItems = BBoardConfig.MAX_ITEMS;
      if (maxItemsStr != null && !maxItemsStr.isEmpty()) {
        maxItems = Integer.parseInt(maxItemsStr);
      }
      if (maxItems > BBoardConfig.MAX_ITEMS) {
        maxItems = BBoardConfig.MAX_ITEMS;
      }
      String userId = sUser.getUsername();
      Long domainId = SessionMgr.getCurrentDomain(req.getSession(), userId);
      ConfigContainer cc = getDomainConfig(domainId, userId, cms);
      cc.dc.setBBoardConfig(
          new BBoardConfig(enable, dataDuration, refreshDuration, scrollInterval, pauseOnHover,
              showNav, maxItems));
      String message = null;
      // Save config
      try {
        saveDomainConfig(cc, domainId, cms);
        message =
            backendMessages.getString("config.success")
                + " &nbsp;[<a href=\"/s/config/configuration.jsp?subview=notifications&viewtype=bulletinboard\">"
                + backendMessages.getString("config.view") + "</a>]";
      } catch (ConfigurationException e) {
        xLogger.severe("Invalid format of configuration for domain {0}: {1}", domainId,
            e.getMessage());
        throw new ServiceException(backendMessages.getString("config.invalid"));
      }
      xLogger.fine("Exiting updateBBoardConfiguration");
      writeToScreen(req, resp, message, Constants.VIEW_CONFIGURATION);
    } catch (ConfigurationException e) {
      xLogger.severe("Configuration exception: {0} : {1}", e.getClass().getName(), e.getMessage());
      throw new ServiceException(backendMessages.getString("config.invalid"));
    }
  }

  // Update the Accounting configuration
  private void updateAccountingConfiguration(HttpServletRequest req, HttpServletResponse resp,
                                             SecureUserDetails sUser, ConfigurationMgmtService cms,
                                             ResourceBundle backendMessages)
      throws ServiceException, IOException {
    xLogger.fine("Entered updateAccountingConfiguration");
    try {
      // Read configuration parameters from the form
      String domainIdStr = req.getParameter("domainid");
      boolean enableAccounting = req.getParameter("enableaccounting") != null;
      String creditLimitStr = req.getParameter("creditlimit");
      String enforceStatus = req.getParameter("enforce");
      // Get domain Id
      Long domainId = null;
      if (domainIdStr != null && !domainIdStr.isEmpty()) {
        domainId = Long.valueOf(domainIdStr);
      }
      String userId = sUser.getUsername();
      // Credit limit, if any
      BigDecimal creditLimit = BigDecimal.ZERO;
      if (creditLimitStr != null && !creditLimitStr.isEmpty()) {
        creditLimit = new BigDecimal(creditLimitStr);
      }
      // Enforcement state, if any
      boolean enforceConfirmed = IOrder.CONFIRMED.equals(enforceStatus);
      boolean enforceShipped = IOrder.COMPLETED.equals(enforceStatus);
      // Get the domain config.
      ConfigContainer cc = getDomainConfig(domainId, userId, cms);
      // Get the accounting config.
      AccountingConfig ac = cc.dc.getAccountingConfig();
      if (ac == null) {
        ac = new AccountingConfig();
        cc.dc.setAccountingConfig(ac);
      }
      ac.setAccountingEnabled(enableAccounting);
      ac.setCreditLimit(creditLimit);
      ac.setEnforceConfirm(enforceConfirmed);
      ac.setEnforceShipped(enforceShipped);
      // Save config
      String message = null;
      try {
        saveDomainConfig(cc, domainId, cms);
        // Success
        message =
            backendMessages.getString("config.success")
                + " &nbsp;[<a href=\"/s/config/configuration.jsp?subview=accounting\">"
                + backendMessages.getString("config.view") + "</a>]";
      } catch (ConfigurationException e) {
        xLogger.severe("Invalid format of configuration for domain {0}: {1}", domainId,
            e.getMessage());
        throw new ServiceException(backendMessages.getString("config.invalid"));
      }

      xLogger.fine("Exiting updateAccountingConfiguration");
      writeToScreen(req, resp, message, Constants.VIEW_CONFIGURATION);
    } catch (ConfigurationException e) {
      xLogger.severe("Configuration exception: {0} : {1}", e.getClass().getName(), e.getMessage());
      throw new ServiceException(backendMessages.getString("config.invalid"));
    }
  }

  // Update the Custom Reports Configuration
  private void updateCustomReportsConfiguration(HttpServletRequest req, HttpServletResponse resp,
                                                SecureUserDetails sUser,
                                                ConfigurationMgmtService cms,
                                                ResourceBundle backendMessages)
      throws ServiceException, IOException {
    xLogger.fine("Entered updateCustomReportsConfiguration");
    // Read configuration parameters from the form
    String domainIdStr = req.getParameter("domainid");
    String userId = sUser.getUsername();
    Long domainId = null;
    if (domainIdStr != null && !domainIdStr.isEmpty()) {
      domainId = Long.valueOf(domainIdStr);
    }
    BlobstoreService blobstoreService = AppFactory.get().getBlobstoreService();

    try {
      // Get domain config.
      ConfigContainer cc = getDomainConfig(domainId, userId, cms);
      // Get the custom reports config.
      CustomReportsConfig crc = cc.dc.getCustomReportsConfig();
      if (crc == null) {
        crc = new CustomReportsConfig();
      }
      String actionStr = req.getParameter("action");
      if (actionStr != null && !actionStr.isEmpty()) {
        if (actionStr.equalsIgnoreCase("remove")) {
          // Call a private function to remove the selected Config object from CustomReportsConfig
          removeFromCustomReportsConfig(req, crc);
        } else if (actionStr.equalsIgnoreCase("edit") || actionStr.equalsIgnoreCase("add")) {
          // The action is update. Call a private function to update the Config object in the CustomReportsConfig.
          updateCustomReportsConfig(req, sUser, blobstoreService, crc, domainId);
        }
      }
      // The crc is now ready to be stored into domain config
      cc.dc.setCustomReportsConfig(crc);
      saveDomainConfig(cc, domainId, cms);
      // Success
      String
          message =
          backendMessages.getString("config.success")
              + " &nbsp;[<a href=\"/s/config/configuration.jsp?subview=customreports\">"
              + backendMessages.getString("config.view") + "</a>]";
      writeToScreen(req, resp, message, Constants.VIEW_CONFIGURATION);
    } catch (ConfigurationException e) {
      xLogger.severe("Configuration exception: {0} : {1}", e.getClass().getName(), e.getMessage());
      throw new ServiceException(backendMessages.getString("config.invalid"));
    }
    xLogger.fine("Exiting updateCustomReportsConfiguration");
  }

  // Removes the Config object specified by the template name from the CustomReportsConfig
  private void removeFromCustomReportsConfig(HttpServletRequest req, CustomReportsConfig crc)
      throws ServiceException {
    xLogger.fine("Entering removeFromCustomReportsConfig");
    // Read the form parameters.
    String templateNameStr = req.getParameter("templatename");
    if (templateNameStr != null && !templateNameStr.isEmpty()) {
      try {
        CustomReportsConfig.Config config = crc.getConfig(templateNameStr);
        if (config != null) {
          String configTemplateKey = config.templateKey;
          // Remove the Config from CustomReportsConfig
          crc.removeConfig(templateNameStr);
          // Remove the corresponding Uploaded object from the datastore.
          removeUploadedObject(configTemplateKey);
        }
      } catch (Exception e) {
        xLogger.severe("{0} while removing template with name {1} from the system. Message: {2}",
            e.getClass().getName(), templateNameStr, e.getMessage());
        throw new ServiceException(e.getMessage());
      }
    } else {
      xLogger.warn("Cannot remove template with templateName null or empty");
    }
    xLogger.fine("Exiting removeFromCustomReportsConfig");
  }

  // Remove the Uploaded object with the specified uploadedKey from the datastore.
  private void removeUploadedObject(String uploadedKey) throws ServiceException {
    xLogger.fine("Entering removeUploadedObject");
    if (uploadedKey != null && !uploadedKey.isEmpty()) {
      UploadService svc;
      try {
        svc = Services.getService(UploadServiceImpl.class);
        IUploaded uploaded = svc.getUploaded(uploadedKey);
        // Remove the blob from the blobstore.
        String blobKeyStr = String.valueOf(uploaded.getBlobKey());
        AppFactory.get().getBlobstoreService().remove(blobKeyStr);
        // Now remove the uploaded object
        svc.removeUploaded(uploadedKey);
      } catch (Exception e) {
        xLogger.severe("{0} while removing uploaded object. Message: {1}", e.getClass().getName());
        throw new ServiceException(e.getMessage());
      }
    } else {
      xLogger.warn("Cannot remove uploaded object with null or empty key");
    }
    xLogger.fine("Exiting removeUploadedObject");
  }

  private void updateCustomReportsConfig(HttpServletRequest req, SecureUserDetails sUser,
                                         BlobstoreService blobstoreService, CustomReportsConfig crc,
                                         Long domainId) throws ServiceException {
    xLogger.fine("Entering updateCustomReportsConfig");
    // if action is add, create new CustomReportsConfig.config
    // if action is edit, get CustomReportsConfig.config
    // Check if template is updated
    // If yes, check if action is add
    // If action is add, create uploaded, set config.filname and config.templatekey, set the other config parameters from the form, add the config to CRC and return.
    // If action is edit, remove any previous upload and blob create uploaded, set config.filename and config.templateKey, set the other config parameters from the form. do not add config, return.

    // Read the form parameters
    String action = req.getParameter("action");
    boolean isEdit = action != null && !action.isEmpty() && action.equalsIgnoreCase("edit");
    String
        templateUpdated =
        req.getParameter(
            "templateupdated"); // This parameter indicates if in the a new template was uploaded or not.
    boolean
        isTemplateUpdated =
        templateUpdated != null && !templateUpdated.isEmpty() && templateUpdated
            .equalsIgnoreCase("yes");
    String templateName = req.getParameter("templatename");
    String oldTemplateName = req.getParameter("oldtemplatename");
    String description = req.getParameter("description");
    String inventorySheetName = req.getParameter("inventorysheetname");
    String usersSheetName = req.getParameter("userssheetname");
    String entitiesSheetName = req.getParameter("entitiessheetname");
    String materialsSheetName = req.getParameter("materialssheetname");
    String ordersSheetName = req.getParameter("orderssheetname");
    String
        ordersDataDurationSameAsRepGenStr =
        req.getParameter(
            "ordersdatadurationsameasrepgen"); // This parameter will be null if the checkbox is not selected in the UI.
    String ordersDataDurationStr = req.getParameter("ordersdataduration");
    String transactionsSheetName = req.getParameter("transactionssheetname");
    String
        transactionsDataDurationSameAsRepGenStr =
        req.getParameter(
            "transactionsdatadurationsameasrepgen"); // This parameter will be null if the checkbox is not selected in the UI.
    String transactionsDataDurationStr = req.getParameter("transactionsdataduration");
    // Manual transactions related request parameters
    String mnlTransactionsSheetName = req.getParameter("mnltransactionssheetname");
    String
        mnlTransactionsDataDurationSameAsRepGenStr =
        req.getParameter(
            "mnltransactionsdatadurationsameasrepgen"); // This parameter will be null if the checkbox is not selected in the UI.
    String mnlTransactionsDataDurationStr = req.getParameter("mnltransactionsdataduration");

    String transactioncountsSheetName = req.getParameter("transactioncountssheetname");
    String
        transactioncountsDataDurationSameAsRepGenStr =
        req.getParameter(
            "transactioncountsdatadurationsameasrepgen"); // This parameter will be null if the checkbox is not selected in the UI.
    String transactioncountsDataDurationStr = req.getParameter("transactioncountsdataduration");
    String transactioncountsAggregateFreq = req.getParameter("transactioncountsaggregatefreq");
    String transactioncountsFilterBy = req.getParameter("transactioncountsfilterby");
    String inventorytrendsSheetName = req.getParameter("inventorytrendssheetname");
    String
        inventorytrendsDataDurationSameAsRepGenStr =
        req.getParameter(
            "inventorytrendsdatadurationsameasrepgen"); // This parameter will be null if the checkbox is not selected in the UI.
    String inventorytrendsDataDurationStr = req.getParameter("inventorytrendsdataduration");
    String inventorytrendsAggregateFreq = req.getParameter("inventorytrendsaggregatefreq");
    String inventorytrendsFilterBy = req.getParameter("inventorytrendsfilterby");
    String historicalInvSnapshotSheetName = req.getParameter("historicalinvsnapshotsheetname");
    String
        historicalInvSnapshotDataDurationStr =
        req.getParameter("historicalinventorysnapshotdataduration");
    String
        includeBatchDetailsStr =
        req.getParameter("includebatchdetails"); // Added to export inventory with batch details
    boolean
        hasIncludeBatchDetails =
        includeBatchDetailsStr != null && !includeBatchDetailsStr.isEmpty();
    String frequency = req.getParameter("frequency");
    String dailyTimeStr = req.getParameter("dailytime");
    String dayOfWeekStr = req.getParameter("weekday");
    String weeklyRepGenTimeStr = req.getParameter("weeklyrepgentime");
    String
        dayOfMonthStr =
        req.getParameter(
            "dayofmonth"); // The dayofmonth is a hidden parameter hard coded to 1 (BEGINNING_OF_MONTH)in the add template form
    String monthlyRepGenTimeStr = req.getParameter("monthlyrepgentime");
    // String dataDurationSameAsRepGenStr = req.getParameter( "datadurationsameasrepgen" ); // This parameter will be null if the checkbox is not selected in the UI.
    // String dataDurationStr = req.getParameter( "dataduration" );
    String managerUsersStr = req.getParameter("manageruserids");
    String adminUsersStr = req.getParameter("adminuserids");
    String superUsersStr = req.getParameter("superuserids");
    CustomReportsConfig.Config config = null;

    xLogger.fine("oldTemplateName: {0}", oldTemplateName);
    if (isEdit) {
      config = crc.getConfig(oldTemplateName);
    } else {
      // Create a Config object and populate it with the form parameters
      config = new CustomReportsConfig.Config();
    }
    xLogger.fine("config.templateName: {0}", config.templateName);
    IUploaded uploaded = null;
    if (isTemplateUpdated) {
      if (!isEdit) {
        // Create an uploaded object
        uploaded = updateUploadedObject(req, sUser, domainId, blobstoreService, templateName);
        if (uploaded != null) {
          // Set the config.fileName and config.templateKey
          config.fileName = uploaded.getFileName();
          config.templateKey = uploaded.getId();
        }
      }
      if (isEdit) {
        // Remove any previous uploads by the same name. // Remove any blobs by the same name.
        removeUploadedObject(config.templateKey);
        // Create uploaded
        uploaded = updateUploadedObject(req, sUser, domainId, blobstoreService, templateName);
        // Set config.fileName and config.templateKey
        if (uploaded != null) {
          config.fileName = uploaded.getFileName();
          config.templateKey = uploaded.getId();
        }
      }
    }

    // Set the other parameters of the config object from the form.
    // If action is edit, do not add the config object to the CustomReportsConfig
    // Otherwise add the config object to CustomReportsConfig.
    if (templateName != null && !templateName.isEmpty()) {
      config.templateName = templateName;
    }
    // If the operation is edit, clear the existing config object parameters before setting the values from the form parameters
    if (isEdit) {
      // First clear the existing map and then add the sheet names. This is done so that the earlier entries in the map
      // are cleared before adding new entries
      config.description = null;
      config.typeSheetDataMap.clear(); //= new HashMap<String,Map<String,String>>();
      // Reset the time fields also
      config.dailyTime = null;
      config.dayOfWeek = 0;
      config.weeklyRepGenTime = null;
      config.dayOfMonth = 0;
      config.monthlyRepGenTime = null;
      // Reset the data duration to true and data duration to 0
      config.dataDurationSameAsRepGenFreq = true;
      config.dataDuration = 0;
      // Reset the manager users, admin users and super users to null if logged in user is a Domain Owner or a Service Manager
      config.managers = null;
      config.users = null;
      // If the logged in user is a service manager or administrator, leave the superUsers list as it is.
      if (sUser.getRole().equals(SecurityConstants.ROLE_SUPERUSER)) {
        config.superUsers = null;
      }
    }

    // Now set the config object parameters from the form parameters.
    if (description != null && !description.isEmpty()) {
      config.description = description;
    }
    // Now set the typeSheetDataMap from the form parameters
    if (inventorySheetName != null && !inventorySheetName.isEmpty()) {
      if (hasIncludeBatchDetails) {
        Map<String, String> invBatchSheetDataMap = new HashMap<String, String>();
        invBatchSheetDataMap.put(CustomReportsConfig.SHEETNAME, inventorySheetName);
        config.typeSheetDataMap
            .put(CustomReportConstants.TYPE_INVENTORYBATCH, invBatchSheetDataMap);
      } else {
        Map<String, String> invSheetDataMap = new HashMap<String, String>();
        invSheetDataMap.put(CustomReportsConfig.SHEETNAME, inventorySheetName);
        config.typeSheetDataMap.put(CustomReportConstants.TYPE_INVENTORY, invSheetDataMap);
      }
    }
    if (usersSheetName != null && !usersSheetName.isEmpty()) {
      Map<String, String> usersSheetDataMap = new HashMap<String, String>();
      usersSheetDataMap.put(CustomReportsConfig.SHEETNAME, usersSheetName);
      config.typeSheetDataMap.put(CustomReportConstants.TYPE_USERS, usersSheetDataMap);
    }
    if (entitiesSheetName != null && !entitiesSheetName.isEmpty()) {
      Map<String, String> entitiesSheetDataMap = new HashMap<String, String>();
      entitiesSheetDataMap.put(CustomReportsConfig.SHEETNAME, entitiesSheetName);
      config.typeSheetDataMap.put(CustomReportConstants.TYPE_ENTITIES, entitiesSheetDataMap);
    }
    if (materialsSheetName != null && !materialsSheetName.isEmpty()) {
      Map<String, String> materialsSheetDataMap = new HashMap<String, String>();
      materialsSheetDataMap.put(CustomReportsConfig.SHEETNAME, materialsSheetName);
      config.typeSheetDataMap.put(CustomReportConstants.TYPE_MATERIALS, materialsSheetDataMap);
    }
    if (ordersSheetName != null && !ordersSheetName.isEmpty()) {
      Map<String, String> ordersSheetDataMap = new HashMap<String, String>();
      ordersSheetDataMap.put(CustomReportsConfig.SHEETNAME, ordersSheetName);
      // Add data duration data to the ordersSheetDataMap
      if (ordersDataDurationSameAsRepGenStr == null || ordersDataDurationSameAsRepGenStr
          .isEmpty()) {
        ordersSheetDataMap
            .put(CustomReportsConfig.DATA_DURATION_SAMEAS_REPGENFREQ, CustomReportsConfig.FALSE);
        if (ordersDataDurationStr != null && !ordersDataDurationStr.isEmpty()) {
          ordersSheetDataMap.put(CustomReportsConfig.DATA_DURATION, ordersDataDurationStr);
        }
      } else {
        ordersSheetDataMap
            .put(CustomReportsConfig.DATA_DURATION_SAMEAS_REPGENFREQ, CustomReportsConfig.TRUE);
        // No need to put dataduration key because it is the same as report generation frequency.
      }
      config.typeSheetDataMap.put(CustomReportConstants.TYPE_ORDERS, ordersSheetDataMap);
    }
    if (transactionsSheetName != null && !transactionsSheetName.isEmpty()) {
      Map<String, String> transactionsSheetDataMap = new HashMap<String, String>();
      transactionsSheetDataMap.put(CustomReportsConfig.SHEETNAME, transactionsSheetName);
      // Add data duration data to the transactionsSheetDataMap
      if (transactionsDataDurationSameAsRepGenStr == null || transactionsDataDurationSameAsRepGenStr
          .isEmpty()) {
        transactionsSheetDataMap
            .put(CustomReportsConfig.DATA_DURATION_SAMEAS_REPGENFREQ, CustomReportsConfig.FALSE);
        if (transactionsDataDurationStr != null && !transactionsDataDurationStr.isEmpty()) {
          transactionsSheetDataMap
              .put(CustomReportsConfig.DATA_DURATION, transactionsDataDurationStr);
        }
      } else {
        transactionsSheetDataMap
            .put(CustomReportsConfig.DATA_DURATION_SAMEAS_REPGENFREQ, CustomReportsConfig.TRUE);
        // No need to put dataduration key because it is the same as report generation frequency.
      }
      config.typeSheetDataMap
          .put(CustomReportConstants.TYPE_TRANSACTIONS, transactionsSheetDataMap);
    }
    // Manual Transactions related data should be put in the map
    if (mnlTransactionsSheetName != null && !mnlTransactionsSheetName.isEmpty()) {
      Map<String, String> mnlTransactionsSheetDataMap = new HashMap<String, String>();
      mnlTransactionsSheetDataMap.put(CustomReportsConfig.SHEETNAME, mnlTransactionsSheetName);
      // Add data duration data to the mnlTransactionsSheetDataMap
      if (mnlTransactionsDataDurationSameAsRepGenStr == null
          || mnlTransactionsDataDurationSameAsRepGenStr.isEmpty()) {
        mnlTransactionsSheetDataMap
            .put(CustomReportsConfig.DATA_DURATION_SAMEAS_REPGENFREQ, CustomReportsConfig.FALSE);
        if (mnlTransactionsDataDurationStr != null && !mnlTransactionsDataDurationStr.isEmpty()) {
          mnlTransactionsSheetDataMap
              .put(CustomReportsConfig.DATA_DURATION, mnlTransactionsDataDurationStr);
        }
      } else {
        mnlTransactionsSheetDataMap
            .put(CustomReportsConfig.DATA_DURATION_SAMEAS_REPGENFREQ, CustomReportsConfig.TRUE);
        // No need to put dataduration key because it is the same as report generation frequency.
      }
      config.typeSheetDataMap
          .put(CustomReportConstants.TYPE_MANUALTRANSACTIONS, mnlTransactionsSheetDataMap);
    }
    if (transactioncountsSheetName != null && !transactioncountsSheetName.isEmpty()) {
      Map<String, String> transactioncountsSheetDataMap = new HashMap<String, String>();
      transactioncountsSheetDataMap.put(CustomReportsConfig.SHEETNAME, transactioncountsSheetName);
      // Add data duration data to the transactioncountsSheetDataMap
      if (transactioncountsDataDurationSameAsRepGenStr == null
          || transactioncountsDataDurationSameAsRepGenStr.isEmpty()) {
        transactioncountsSheetDataMap
            .put(CustomReportsConfig.DATA_DURATION_SAMEAS_REPGENFREQ, CustomReportsConfig.FALSE);
        if (transactioncountsDataDurationStr != null && !transactioncountsDataDurationStr
            .isEmpty()) {
          transactioncountsSheetDataMap
              .put(CustomReportsConfig.DATA_DURATION, transactioncountsDataDurationStr);
        }
      } else {
        transactioncountsSheetDataMap
            .put(CustomReportsConfig.DATA_DURATION_SAMEAS_REPGENFREQ, CustomReportsConfig.TRUE);
        // No need to put dataduration key because it is the same as report generation frequency.
      }
      // Add aggregate frequency and filterby to transactioncountsSheetDataMap
      if (transactioncountsAggregateFreq != null && !transactioncountsAggregateFreq.isEmpty()) {
        transactioncountsSheetDataMap
            .put(CustomReportsConfig.AGGREGATEFREQ, transactioncountsAggregateFreq);
      }
      if (transactioncountsFilterBy != null && !transactioncountsFilterBy.isEmpty()) {
        transactioncountsSheetDataMap.put(CustomReportsConfig.FILTERBY, transactioncountsFilterBy);
      }
      config.typeSheetDataMap
          .put(CustomReportConstants.TYPE_TRANSACTIONCOUNTS, transactioncountsSheetDataMap);
      xLogger.fine("typeSheetDataMap: {0}", config.typeSheetDataMap);
    }

    if (inventorytrendsSheetName != null && !inventorytrendsSheetName.isEmpty()) {
      Map<String, String> inventorytrendsSheetDataMap = new HashMap<String, String>();
      inventorytrendsSheetDataMap.put(CustomReportsConfig.SHEETNAME, inventorytrendsSheetName);
      // Add data duration data to the inventorytrendsSheetDataMap
      if (inventorytrendsDataDurationSameAsRepGenStr == null
          || inventorytrendsDataDurationSameAsRepGenStr.isEmpty()) {
        inventorytrendsSheetDataMap
            .put(CustomReportsConfig.DATA_DURATION_SAMEAS_REPGENFREQ, CustomReportsConfig.FALSE);
        if (inventorytrendsDataDurationStr != null && !inventorytrendsDataDurationStr.isEmpty()) {
          inventorytrendsSheetDataMap
              .put(CustomReportsConfig.DATA_DURATION, inventorytrendsDataDurationStr);
        }
      } else {
        inventorytrendsSheetDataMap
            .put(CustomReportsConfig.DATA_DURATION_SAMEAS_REPGENFREQ, CustomReportsConfig.TRUE);
        // No need to put dataduration key because it is the same as report generation frequency.
      }
      // Add aggregate frequency and filterby to inventorytrendsSheetDataMap
      if (inventorytrendsAggregateFreq != null && !inventorytrendsAggregateFreq.isEmpty()) {
        inventorytrendsSheetDataMap
            .put(CustomReportsConfig.AGGREGATEFREQ, inventorytrendsAggregateFreq);
      }
      if (inventorytrendsFilterBy != null && !inventorytrendsFilterBy.isEmpty()) {
        inventorytrendsSheetDataMap.put(CustomReportsConfig.FILTERBY, inventorytrendsFilterBy);
      }
      config.typeSheetDataMap
          .put(CustomReportConstants.TYPE_INVENTORYTRENDS, inventorytrendsSheetDataMap);
      xLogger.fine("typeSheetDataMap: {0}", config.typeSheetDataMap);
    }

    if (historicalInvSnapshotSheetName != null && !historicalInvSnapshotSheetName.isEmpty()) {
      Map<String, String> historicalInvSnapshotSheetDataMap = new HashMap<String, String>();
      historicalInvSnapshotSheetDataMap
          .put(CustomReportsConfig.SHEETNAME, historicalInvSnapshotSheetName);
      if (historicalInvSnapshotDataDurationStr != null && !historicalInvSnapshotDataDurationStr
          .isEmpty()) {
        historicalInvSnapshotSheetDataMap
            .put(CustomReportsConfig.DATA_DURATION, historicalInvSnapshotDataDurationStr);
      }
      config.typeSheetDataMap.put(CustomReportConstants.TYPE_HISTORICAL_INVENTORYSNAPSHOT,
          historicalInvSnapshotSheetDataMap);
      xLogger.fine("typeSheetDataMap: {0}", config.typeSheetDataMap);
    }

    // Set the frequency
    if (frequency != null && !frequency.isEmpty()) {
      if (frequency.equals(CustomReportConstants.FREQUENCY_DAILY) && dailyTimeStr != null
          && !dailyTimeStr.isEmpty()) {
        config.dailyTime = dailyTimeStr;
      }
      if (frequency.equals(CustomReportConstants.FREQUENCY_WEEKLY)) {
        if (dayOfWeekStr != null && !dayOfWeekStr.isEmpty()) {
          config.dayOfWeek = Integer.parseInt(dayOfWeekStr);
        }
        if (weeklyRepGenTimeStr != null && !weeklyRepGenTimeStr.isEmpty()) {
          config.weeklyRepGenTime = weeklyRepGenTimeStr;
        }
      }
      if (frequency.equals(CustomReportConstants.FREQUENCY_MONTHLY)) {
        if (dayOfMonthStr != null && !dayOfMonthStr.isEmpty()) {
          config.dayOfMonth = Integer.parseInt(dayOfMonthStr);
        }
        if (monthlyRepGenTimeStr != null && !monthlyRepGenTimeStr.isEmpty()) {
          config.monthlyRepGenTime = monthlyRepGenTimeStr;
        }
      }
    }

    xLogger.fine("managerUsersStr: {0}", managerUsersStr);
    if (managerUsersStr != null && !managerUsersStr.isEmpty()) {
      config.managers = StringUtil.getList(managerUsersStr);
    }
    xLogger.fine("adminUsersStr: {0}", adminUsersStr);
    if (adminUsersStr != null && !adminUsersStr.isEmpty()) {
      config.users = StringUtil.getList(adminUsersStr);
    }
    xLogger.fine("superUsersStr: {0}", superUsersStr);
    if (superUsersStr != null && !superUsersStr.isEmpty()) {
      config.superUsers = StringUtil.getList(superUsersStr);
    }
    // CreationTime is created only during add operation. In case of edit, it is already set.
    if (!isEdit) {
      config.creationTime = new Date();
    }
    // lastUpdatedTime
    config.lastUpdatedTime = new Date();

    if (!isEdit) {
      // Add the Config object to the CustomReportsConfig
      try {
        xLogger.fine("Before adding to crc... crc.toJSONObject: {0}", crc.toJSONObject());
        crc.getCustomReportsConfig().add(config);
        xLogger.fine("After adding to crc... crc.toJSONObject: {0}", crc.toJSONObject());
      } catch (Exception e) {
        // do nothing
      }
    }
    xLogger.fine("Exiting updateCustomReportsConfig");
  }

  // Create/update an existing an Uploaded object.
  private IUploaded updateUploadedObject(HttpServletRequest req, SecureUserDetails sUser,
                                         Long domainId, BlobstoreService blobstoreService,
                                         String templateName) throws ServiceException {
    Map<String, List<String>>
        blobs =
        blobstoreService.getUploads(
            req); // Note getUploadedBlobs is deprecated because it does not handle file uploads with multiple attribute set to true.

    if (blobs != null && !blobs.isEmpty()) {
      List<String> blobKeyList = blobs.get("filename");
      String blobKey = blobKeyList.get(0);
      if (blobKey != null) {
        BlobInfo blobInfo = blobstoreService.getBlobInfo(blobKey);
        String fileName = blobInfo.getFilename();
        // Create an uploaded object
        IUploaded uploaded = null;
        UploadService svc = null;
        String
            uploadedFileName =
            CustomReportsConfig.Config.getFileNameWithDomainId(templateName, domainId);
        // Generate a 5 digit random number and append it to the uploadedFileName, just to make sure that the uploaded key is unique
        uploadedFileName += NumberUtil.generateFiveDigitRandomNumber();
        xLogger.fine("uploadedFileName: {0}", uploadedFileName);

        String
            uploadedKey =
            JDOUtils.createUploadedKey(uploadedFileName, CustomReportsConfig.VERSION,
                Constants.LANG_DEFAULT);
        boolean isUploadedNew = false;
        try {
          svc = Services.getService(UploadServiceImpl.class);
          uploaded = svc.getUploaded(uploadedKey);
          // Found an older upload, remove the older blob
          try {
            blobstoreService.remove(String.valueOf(uploaded.getBlobKey()));
          } catch (Exception e) {
            xLogger.warn("Exception {0} when trying to remove older blob for key {1}: {2}",
                e.getClass().getName(), uploaded.getId(), e.getMessage());
          }
        } catch (ObjectNotFoundException e) {
          uploaded = JDOUtils.createInstance(IUploaded.class);
          uploaded.setId(uploadedKey);
          isUploadedNew = true;
        } catch (Exception e) {
          xLogger
              .severe("{0} while trying to get UploadService or getting Uploaded for key {1}: {2}",
                  e.getClass().getName(), uploadedKey, e.getMessage());
          throw new ServiceException(
              "System error occurred during upload. Please try again or contact your administrator.");
          // writeToScreen( req, resp, "System error occurred during upload. Please try again or contact your administrator.", Constants.VIEW_CONFIGURATION );
        }
        Date now = new Date();
        // Update uploaded object
        uploaded.setBlobKey(blobKey);
        uploaded.setDomainId(domainId);
        uploaded.setFileName(fileName);
        uploaded.setLocale(Constants.LANG_DEFAULT);
        uploaded.setTimeStamp(now);
        uploaded.setType(IUploaded.CUSTOMREPORT_TEMPLATE);
        uploaded.setUserId(sUser.getUsername());
        uploaded.setVersion(CustomReportsConfig.VERSION);

        try {
          // Persist upload metadata
          if (isUploadedNew) {
            svc.addNewUpload(uploaded);
          } else {
            svc.updateUploaded(uploaded);
          }
          return uploaded;
        } catch (Exception e) {
          xLogger
              .severe("{0} while trying to get UploadService or getting Uploaded for key {1}: {2}",
                  e.getClass().getName(), uploadedKey, e.getMessage());
          throw new ServiceException(
              "System error occurred during upload. Please try again or contact your administrator.");
        }
      } // End if blobKey != null
    } // End if blobs != null
    return null;
  }

  private void updateKioskConfig(HttpServletRequest req, HttpServletResponse resp,
                                 SecureUserDetails sUser, ConfigurationMgmtService cms,
                                 ResourceBundle backendMessages)
      throws ServiceException, IOException {
    xLogger.fine("Entering updateKioskConfig");
    boolean add = false;
    // Read configuration parameters from the form
    String kioskIdStr = req.getParameter("kioskid");
    String domainIdStr = req.getParameter("domainid");
    String enableStr = req.getParameter("enable");
    String refreshDurationStr = req.getParameter("refreshduration");
    String scrollIntervalStr = req.getParameter("scrollinterval");
    String maxItemsStr = req.getParameter("maxitems");
    String horScrlMsgsStr = req.getParameter("horscrollmessages");
    String horScrlIntervalStr = req.getParameter("horscrollinterval");
    xLogger.fine(
        "enableStr: {0} , refreshDurationStr: {1}, scrollIntervalStr: {2}, maxItems: {3}, , horScrlMsgsStr: {4},  horScrlIntervalStr: {5}",
        enableStr, refreshDurationStr, scrollIntervalStr, maxItemsStr, horScrlMsgsStr,
        horScrlIntervalStr);

    // If kioskIdStr is null or empty, return with an error message
    if (kioskIdStr == null || kioskIdStr.isEmpty() || domainIdStr == null || domainIdStr
        .isEmpty()) {
      xLogger.severe("Invalid or null request parameters. kioskid:  {0}, domainId: {1}", kioskIdStr,
          domainIdStr);
      return;
    }
    // Parse the request parameters.
    Long kioskId = Long.valueOf(kioskIdStr);
    Long domainId = Long.valueOf(domainIdStr);
    int enable = StockboardConfig.PRIVATE;

    if (enableStr != null && !enableStr.isEmpty()) {
      enable = Integer.parseInt(enableStr);
    }
    int refreshDuration = StockboardConfig.REFRESH_DURATION_DEFAULT;
    if (refreshDurationStr != null && !refreshDurationStr.isEmpty()) {
      refreshDuration = Integer.parseInt(refreshDurationStr);
    }
    int scrollInterval = StockboardConfig.SCROLL_INTERVAL_DEFAULT;
    if (scrollIntervalStr != null && !scrollIntervalStr.isEmpty()) {
      scrollInterval = Integer.parseInt(scrollIntervalStr);
    }
    int maxItems = StockboardConfig.MAX_ITEMS;
    if (maxItemsStr != null && !maxItemsStr.isEmpty()) {
      maxItems = Integer.parseInt(maxItemsStr);
    }
    if (maxItems > StockboardConfig.MAX_ITEMS) {
      maxItems = StockboardConfig.MAX_ITEMS;
    }
    List<String> horScrlMsgsList = null;
    if (horScrlMsgsStr != null && !horScrlMsgsStr.isEmpty()) {
      // Create a list of horizontal scroll messages from the string
      StringTokenizer st = new StringTokenizer(horScrlMsgsStr, "\r");
      if (st.countTokens() > 0) {
        horScrlMsgsList = new ArrayList<String>();
      }
      while (st.hasMoreTokens()) {
        // Trim the strings before adding them to the list.
        String token = st.nextToken();
        if (token != null && !token.isEmpty()) {
          token = token.trim();
          if (!token.isEmpty()) {
            horScrlMsgsList.add(token);
          }
        }
      }
    }
    int horScrlInterval = StockboardConfig.HOR_SCROLL_INTERVAL_DEFAULT;
    if (horScrlIntervalStr != null && !horScrlIntervalStr.isEmpty()) {
      horScrlInterval = Integer.parseInt(horScrlIntervalStr);
    }
    // Form the KioskConfig key.
    String key = IConfig.CONFIG_KIOSK_PREFIX + kioskId.toString();
    IConfig c = null;
    KioskConfig kc = null;
    StockboardConfig sbConfig = null;
    // Get the KioskConfig object
    try {
      c = cms.getConfiguration(key);
    } catch (ObjectNotFoundException onfe) {
      xLogger.fine("{0} when trying to update kiosk config for kiosk with id {1}. Message: {2}",
          onfe.getClass().getName(), kioskId, onfe.getMessage());
      c = JDOUtils.createInstance(IConfig.class);
      c.setKey(key);
      c.setUserId(sUser.getUsername());
      c.setDomainId(domainId);
      c.setLastUpdated(new Date());
      add = true;
    }
    kc = KioskConfig.getInstance(kioskId);
    if (kc == null) {
      xLogger
          .severe("Failed to create KioskConfig object. Exiting from updateKioskConfig method...");
      return;
    }

    // Get the stockboard configuration from the kiosk config object
    sbConfig = kc.getStockboardConfig();
    // Set the stockboard configuration using the values read from the form.
    sbConfig.setEnabled(enable);
    sbConfig.setRefreshDuration(refreshDuration);
    sbConfig.setScrollInterval(scrollInterval);
    sbConfig.setMaxItems(maxItems);
    sbConfig.setHorScrlMsgsList(horScrlMsgsList);
    sbConfig.setHorScrollInterval(horScrlInterval);
    // Set the stockboard configuration for the kiosk config object.
    kc.setSbConfig(sbConfig);

    try {
      String kioskConfigStr = kc.toJSONString();
      xLogger.fine("kioskConfigStr: {0}", kioskConfigStr);
      if (kioskConfigStr != null && !kioskConfigStr.isEmpty()) {
        c.setConfig(kioskConfigStr);
      }
      // Add or update the configuration in the datastore.
      if (add) {
        cms.addConfiguration(key, c);
      } else {
        cms.updateConfiguration(c);
      }
      xLogger.fine("Exiting updateKioskConfig");
      // Success
      String
          message =
          backendMessages.getString("config.success")
              + " &nbsp;[<a href=\"/s/setup/setup.jsp?subview=kiosks&form=sbconfig&kioskid="
              + kioskIdStr + "\">" + backendMessages.getString("config.view") + "</a>]";
      writeToSetupScreen(req, resp, message, Constants.VIEW_KIOSKS);
    } catch (JSONException e) {
      xLogger.severe("{0} when saving KioskConfig object. Message: {1}", e.getClass().getName(),
          e.getMessage());
    }
  }

  // Update the Payments Configuration
  private void updatePaymentsConfiguration(HttpServletRequest req, HttpServletResponse resp,
                                           SecureUserDetails sUser, ConfigurationMgmtService cms,
                                           ResourceBundle backendMessages)
      throws ServiceException, IOException {
    xLogger.fine("Entered updatePaymentsConfiguration");
    // Read configuration parameters from the form
    String domainIdStr = req.getParameter("domainid");
    boolean paymentsEnabled = req.getParameter("enablepayments") != null;
    String userId = sUser.getUsername();
    Long domainId = null;
    if (domainIdStr != null && !domainIdStr.isEmpty()) {
      domainId = Long.valueOf(domainIdStr);
    }

    try {
      // Get domain config.
      ConfigContainer cc = getDomainConfig(domainId, userId, cms);
      // Get the payments config.
      PaymentsConfig pc = cc.dc.getPaymentsConfig();
      if (pc == null) {
        pc = new PaymentsConfig();
      }

      String actionStr = req.getParameter("action");
      if (actionStr != null && !actionStr.isEmpty()) {
        if (actionStr.equalsIgnoreCase("remove")) {
          // Call a private function to remove the selected PaymentConfig object from PaymentsConfig
          removeFromPaymentsConfig(req, pc);
        } else if (actionStr.equalsIgnoreCase("edit") || actionStr.equalsIgnoreCase("add")) {
          // The action is update. Call a private function to update the PaymentConfig object in the PaymentsConfig.
          updatePaymentsConfig(req, sUser, pc);
        }
      } else {

        pc.setPaymentsEnabled(paymentsEnabled);
      }
      // The pc is now ready to be stored into domain config
      cc.dc.setPaymentsConfig(pc);
      saveDomainConfig(cc, domainId, cms);
      // Success
      String
          message =
          backendMessages.getString("config.success")
              + " &nbsp;[<a href=\"/s/config/configuration.jsp?subview=payments\">"
              + backendMessages.getString("config.view") + "</a>]";
      writeToScreen(req, resp, message, Constants.VIEW_CONFIGURATION);
    } catch (ConfigurationException e) {
      xLogger.severe("Configuration exception: {0} : {1}", e.getClass().getName(), e.getMessage());
      throw new ServiceException(backendMessages.getString("config.invalid"));
    }
    xLogger.fine("Exiting updatePaymentsConfiguration");
  }

  private void updatePaymentsConfig(HttpServletRequest req, SecureUserDetails sUser,
                                    PaymentsConfig pc) throws ServiceException {
    xLogger.fine("Entering updatePaymentsConfig");
    Locale locale = sUser.getLocale();
    String timezone = sUser.getTimezone();
    xLogger.fine("locale: {0}, timezone: {1}", locale, timezone);
    // Read the form parameters
    String action = req.getParameter("action");
    boolean isEdit = action != null && !action.isEmpty() && action.equalsIgnoreCase("edit");

    String providerId = req.getParameter("provider");
    String accountName = req.getParameter("accountname");
    String oldAccountName = req.getParameter("oldaccountname");
    String accountUser = req.getParameter("accountuser");
    String accessName = req.getParameter("accessname");
    String accessPassword = req.getParameter("accesspassword");

    // Create a PaymentConfig object
    PaymentConfig paymentConfig = null;
    if (isEdit) {
      paymentConfig = pc.getPaymentConfig(oldAccountName);
    } else {
      paymentConfig = new PaymentConfig();
    }
    if (providerId != null && !providerId.isEmpty()) {
      paymentConfig.providerId = providerId;
    }
    if (accountName != null && !accountName.isEmpty()) {
      paymentConfig.accountName = accountName;
    }
    if (accountUser != null && !accountUser.isEmpty()) {
      paymentConfig.accountUser = accountUser;
    }
    if (accessName != null && !accessName.isEmpty()) {
      paymentConfig.accessName = accessName;
    }
    if (accessPassword != null && !accessPassword.isEmpty()) {
      paymentConfig.accessPassword = accessPassword;
    }

    // CreationTime is created only during add operation. In case of edit, it is already set.
    if (!isEdit) {
      paymentConfig.creationTime = new Date();
    }
    // lastUpdatedTime
    paymentConfig.lastUpdatedTime = new Date();
    if (!isEdit) {
      // Add the PaymentConfig object to the PaymentsConfig
      pc.getPaymentsConfig().add(paymentConfig);
    }
    xLogger.fine("Exiting updatePaymentsConfig");
  }

  // Removes the PaymentConfig object specified by the account name from the PaymentsConfig
  private void removeFromPaymentsConfig(HttpServletRequest req, PaymentsConfig pc)
      throws ServiceException {
    xLogger.fine("Entering removeFromPaymentsConfig");
    // Read the form parameters.
    String accountNameStr = req.getParameter("accountname");
    if (accountNameStr != null && !accountNameStr.isEmpty()) {
      try {
        PaymentConfig paymentConfig = pc.getPaymentConfig(accountNameStr);
        if (paymentConfig != null) {
          // Remove the PaymentConfig from PaymentsConfig
          pc.removePaymentConfig(accountNameStr);
        }
      } catch (Exception e) {
        xLogger.severe(
            "{0} while removing payment account with name {1} from the system. Message: {2}",
            e.getClass().getName(), accountNameStr, e.getMessage());
        throw new ServiceException(e.getMessage());
      }
    } else {
      xLogger.warn("Cannot remove account with accountName null or empty");
    }
    xLogger.fine("Exiting removeFromPaymentsConfig");
  }

  // Update the Temperature Monitoring Configuration
  private void updateTemperatureConfiguration(HttpServletRequest req, HttpServletResponse resp,
                                              SecureUserDetails sUser, ConfigurationMgmtService cms,
                                              ResourceBundle backendMessages)
      throws ServiceException, IOException {
    xLogger.fine("Entered updateTemperatureConfiguration");
    // Read configuration parameters from the form
    try {
      String domainIdStr = req.getParameter("domainid");
      String vendorIdsCsvStr = req.getParameter("vendoridscsv");
      String enableOptions = req.getParameter("enableoptions");
      List<String> vendorIds = null;
      if (vendorIdsCsvStr != null && !vendorIdsCsvStr.isEmpty()) {
        vendorIds = StringUtil.getList(vendorIdsCsvStr);
      }
      String userId = sUser.getUsername();
      Long domainId = null;
      if (domainIdStr != null && !domainIdStr.isEmpty()) {
        domainId = Long.valueOf(domainIdStr);
      }
      // Get the domain config.
      ConfigContainer cc = getDomainConfig(domainId, userId, cms);
      // Get the temperature config.
      AssetConfig tc = cc.dc.getAssetConfig();

      if (tc == null) {
        tc = new AssetConfig();
      } else {
        System.out.println("tc: " + tc.getVendorIds());
      }
      System.out.println("vendorIds: " + vendorIds);
      tc.setVendorIds(vendorIds);
      if (StringUtil.isStringInteger(enableOptions)) {
        tc.setEnable(Integer.parseInt(enableOptions));

        if (tc.getEnable() != AssetConfig.ENABLE_NONE) {
          AssetConfig.Configuration configuration = tc.getConfiguration();
          AssetConfig.ConfigurationComm comm = configuration.getComm();
          AssetConfig.AlarmConfig highAlarm = configuration.getHighAlarm();
          AssetConfig.AlarmConfig lowAlarm = configuration.getLowAlarm();
          AssetConfig.AlarmConfig highWarn = configuration.getHighWarn();
          AssetConfig.AlarmConfig lowWarn = configuration.getLowWarn();
          AssetConfig.Locale locale = configuration.getLocale();
          AssetConfig.Filter filter = configuration.getFilter();

          String tmpData = req.getParameter("chnl");
          if (tmpData != null && StringUtil.isStringInteger(tmpData)) {
            comm.setChnl(Integer.parseInt(tmpData));
          }

          tmpData = req.getParameter("tmpurl");
          if (tmpData != null) {
            comm.setTmpUrl(tmpData.trim());
          }

          tmpData = req.getParameter("cfgurl");
          if (tmpData != null) {
            if (!tmpData.endsWith("/")) {
              tmpData += "/";
            }
            comm.setCfgUrl(tmpData.trim());
          }

          tmpData = req.getParameter("almurl");
          if (tmpData != null) {
            comm.setAlrmUrl(tmpData.trim());
          }

          tmpData = req.getParameter("statsurl");
          if (tmpData != null) {
            comm.setStatsUrl(tmpData.trim());
          }

          tmpData = req.getParameter("devryurl");
          if (tmpData != null) {
            comm.setDevRyUrl(tmpData.trim());
          }

          tmpData = req.getParameter("smsgyph");
          if (tmpData != null) {
            comm.setSmsGyPh(tmpData.trim());
          }

          tmpData = req.getParameter("senderid");
          if (tmpData != null) {
            comm.setSenderId(tmpData.trim());
          }

          tmpData = req.getParameter("smsgykey");
          if (tmpData != null) {
            comm.setSmsGyKey(tmpData.trim());
          }

          tmpData = req.getParameter("tmpnotify");
          if (tmpData != null && StringUtil.isStringBoolean(tmpData)) {
            comm.setTmpNotify(Boolean.parseBoolean(tmpData));
          } else {
            comm.setTmpNotify(false);
          }

          tmpData = req.getParameter("incexcnotify");
          if (tmpData != null && StringUtil.isStringBoolean(tmpData)) {
            comm.setIncExcNotify(Boolean.parseBoolean(tmpData));
          } else {
            comm.setIncExcNotify(false);
          }

          tmpData = req.getParameter("statsnotify");
          if (tmpData != null && StringUtil.isStringBoolean(tmpData)) {
            comm.setStatsNotify(Boolean.parseBoolean(tmpData));
          } else {
            comm.setStatsNotify(false);
          }

          tmpData = req.getParameter("devalrmsnotify");
          if (tmpData != null && StringUtil.isStringBoolean(tmpData)) {
            comm.setDevAlrmsNotify(Boolean.parseBoolean(tmpData));
          } else {
            comm.setDevAlrmsNotify(false);
          }

          tmpData = req.getParameter("tmpalrmsnotify");
          if (tmpData != null && StringUtil.isStringBoolean(tmpData)) {
            comm.setTmpAlrmsNotify(Boolean.parseBoolean(tmpData));
          } else {
            comm.setTmpAlrmsNotify(false);
          }

          tmpData = req.getParameter("samplingint");
          if (tmpData != null && StringUtil.isStringInteger(tmpData.trim())) {
            comm.setSamplingInt(Integer.parseInt(tmpData.trim()));
          }

          tmpData = req.getParameter("pushint");
          if (tmpData != null && StringUtil.isStringInteger(tmpData.trim())) {
            comm.setPushInt(Integer.parseInt(tmpData.trim()));
          }

          tmpData = req.getParameter("usrphones");
          if (tmpData != null) {
            comm.setUsrPhones(StringUtil.getList(tmpData.trim()));
          }

          tmpData = req.getParameter("highalarmtemp");
          if (tmpData != null && StringUtil.isStringDouble(tmpData.trim())) {
            highAlarm.setTemp(Double.parseDouble(tmpData.trim()));
          }

          tmpData = req.getParameter("highalarmdur");
          if (tmpData != null && StringUtil.isStringInteger(tmpData.trim())) {
            highAlarm.setDur(Integer.parseInt(tmpData.trim()));
          }

          tmpData = req.getParameter("lowalarmtemp");
          if (tmpData != null && StringUtil.isStringDouble(tmpData.trim())) {
            lowAlarm.setTemp(Double.parseDouble(tmpData.trim()));
          }

          tmpData = req.getParameter("lowalarmdur");
          if (tmpData != null && StringUtil.isStringInteger(tmpData.trim())) {
            lowAlarm.setDur(Integer.parseInt(tmpData));
          }

          tmpData = req.getParameter("highwarntemp");
          if (tmpData != null && StringUtil.isStringDouble(tmpData.trim())) {
            highWarn.setTemp(Double.parseDouble(tmpData.trim()));
          }

          tmpData = req.getParameter("highwarndur");
          if (tmpData != null && StringUtil.isStringInteger(tmpData.trim())) {
            highWarn.setDur(Integer.parseInt(tmpData.trim()));
          }

          tmpData = req.getParameter("lowwarntemp");
          if (tmpData != null && StringUtil.isStringDouble(tmpData.trim())) {
            lowWarn.setTemp(Double.parseDouble(tmpData.trim()));
          }

          tmpData = req.getParameter("lowwarndur");
          if (tmpData != null && StringUtil.isStringInteger(tmpData.trim())) {
            lowWarn.setDur(Integer.parseInt(tmpData.trim()));
          }

          locale.setCn(cc.dc.getCountry());
          locale.setTz(tc.getTimezoneOffset(cc.dc.getTimezone()));
          locale.setLn(cc.dc.getLanguage());

          tmpData = req.getParameter("excursionFilterDur");
          if (tmpData != null && StringUtil.isStringInteger(tmpData.trim())) {
            filter.setExcursionFilterDuration(Integer.parseInt(tmpData));
          }

          tmpData = req.getParameter("alarmFilterDur");
          if (tmpData != null && StringUtil.isStringInteger(tmpData.trim())) {
            filter.setDeviceAlarmFilterDuration(Integer.parseInt(tmpData));
          }

          tmpData = req.getParameter("noDataFilterDur");
          if (tmpData != null && StringUtil.isStringInteger(tmpData.trim())) {
            filter.setNoDataFilterDuration(Integer.parseInt(tmpData));
          }
        }
      }

      cc.dc.setAssetConfig(tc);
      // Save config
      String message = null;
      try {
        saveDomainConfig(cc, domainId, cms);
        // Success
        message =
            backendMessages.getString("config.success")
                + " &nbsp;[<a href=\"/s/config/configuration.jsp?subview=temperaturemonitoring\">"
                + backendMessages.getString("config.view") + "</a>]";
      } catch (ConfigurationException e) {
        xLogger.severe("Invalid format of configuration for domain {0}: {1}", domainId,
            e.getMessage());
        throw new ServiceException(backendMessages.getString("config.invalid"));
      }

      xLogger.fine("Exiting updateTemperatureConfiguration");
      writeToScreen(req, resp, message, Constants.VIEW_CONFIGURATION);
    } catch (ConfigurationException e) {
      xLogger.severe("Configuration exception: {0} : {1}", e.getClass().getName(), e.getMessage());
      throw new ServiceException(backendMessages.getString("config.invalid"));
    }

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

  // Update the Dashboard Monitoring Configuration
  private void updateDashboardConfiguration(HttpServletRequest req, HttpServletResponse resp,
                                            SecureUserDetails sUser, ConfigurationMgmtService cms,
                                            ResourceBundle backendMessages)
      throws ServiceException, IOException {
    xLogger.fine("Entering updateDashboardConfiguration");
    try {
      // Get the domain Id
      String userId = sUser.getUsername();
      Long domainId = SessionMgr.getCurrentDomain(req.getSession(), userId);
      // Get domain config.
      ConfigContainer cc = getDomainConfig(domainId, userId, cms);

      // Form object
      DashboardConfig dashboardConfig = new DashboardConfig();

      // Get the parameters for dasboard configuration
      boolean showActivityPanel = req.getParameter("showactivitypanel") != null;
      ActivityPanelConfig actPanelConfig = new ActivityPanelConfig();
      actPanelConfig.showActivityPanel = showActivityPanel;

      boolean showRevenuePanel = req.getParameter("showrevenuepanel") != null;
      RevenuePanelConfig rvnPanelConfig = new RevenuePanelConfig();
      rvnPanelConfig.showRevenuePanel = showRevenuePanel;

      boolean showOrderPanel = req.getParameter("showorderpanel") != null;
      OrderPanelConfig ordPanelConfig = new OrderPanelConfig();
      ordPanelConfig.showOrderPanel = showOrderPanel;

      boolean showInventoryPanel = req.getParameter("showinventorypanel") != null;
      InventoryPanelConfig invPanelConfig = new InventoryPanelConfig();
      invPanelConfig.showInvPanel = showInventoryPanel;

      // Set the dashboard configuration object
      dashboardConfig.setActivityPanelConfig(actPanelConfig);
      dashboardConfig.setRevenuePanelConfig(rvnPanelConfig);
      dashboardConfig.setOrderPanelConfig(ordPanelConfig);
      dashboardConfig.setInventoryPanelConfig(invPanelConfig);

      // Save config
      String message = null;
      try {
        cc.dc.setDashboardConfig(dashboardConfig);
        saveDomainConfig(cc, domainId, cms);
        // Success
        message =
            backendMessages.getString("config.success")
                + " &nbsp;[<a href=\"/s/config/configuration.jsp?subview=dashboard\">"
                + backendMessages.getString("config.view") + "</a>]";
      } catch (ConfigurationException e) {
        xLogger.severe("Invalid format of configuration for domain {0}: {1}", domainId,
            e.getMessage());
        throw new ServiceException(backendMessages.getString("config.invalid"));
      }
      xLogger.fine("Exiting updateInventoryConfig");
      writeToScreen(req, resp, message, Constants.VIEW_CONFIGURATION);

    } catch (ConfigurationException e) {
      xLogger.severe("Configuration exception: {0} : {1}", e.getClass().getName(), e.getMessage());
      throw new ServiceException(backendMessages.getString("config.invalid"));
    }
    xLogger.fine("Exiting updateDashboardConfiguration");
  }

  private ConfigContainer getDomainConfig(Long domainId, String userId,
                                          ConfigurationMgmtService cms)
      throws ServiceException, ConfigurationException {
    // Get the config key
    String key = IConfig.CONFIG_PREFIX + domainId;
    ConfigContainer cc = new ConfigContainer();
    try {
      cc.c = cms.getConfiguration(key);
      cc.dc = new DomainConfig(cc.c.getConfig());
    } catch (ObjectNotFoundException e) {
      // Config does not exist; create a new one
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

  private void saveDomainConfig(ConfigContainer cc, Long domainId, ConfigurationMgmtService cms)
      throws ServiceException, ConfigurationException {
    cc.c.setConfig(cc.dc.toJSONSring());
    xLogger.info("cc.dc.toJSONString(): {0}", cc.dc.toJSONSring());
    if (cc.add) {
      cms.addConfiguration(cc.c.getKey(), cc.c);
      xLogger.fine("Added configuration {0}", cc.dc.getCustomReportsConfig().toJSONObject());
    } else {
      cms.updateConfiguration(cc.c);
      xLogger.fine("Updated configuration: {0}", cc.dc.getCustomReportsConfig().toJSONObject());
    }
    // Update the memcache with domain config
    MemcacheService cache = AppFactory.get().getMemcacheService();
    if (cache != null) {
      cache.put(DomainConfig.getCacheKey(domainId), cc.dc);
    }
  }

  private String trimReasons(String reasonsCSV) {
    String csv = reasonsCSV;
    if (csv != null) {
      csv = csv.trim();
      if (csv.isEmpty()) {
        csv = null;
      } else {
        // Compact spaces between reasons
        csv = StringUtil.getCSV(StringUtil.trim(StringUtil.getArray(csv)));
      }
    }
    if (csv == null) {
      csv = "";
    }
    return csv;
  }

  private void writeToScreen(HttpServletRequest req, HttpServletResponse resp, String message,
                             String view)
      throws IOException {
    writeToScreen(req, resp, message, null, view);
  }

  private void writeToScreen(HttpServletRequest req, HttpServletResponse resp, String message,
                             String mode, String view)
      throws IOException {
    writeToScreen(req, resp, message, mode, view, "/s/message.jsp");
  }

  private void writeToSetupScreen(HttpServletRequest req, HttpServletResponse resp, String message,
                                  String subview) throws IOException {
    String
        url =
        "/s/setup/setup.jsp?form=messages&subview=" + subview + "&message=" + URLEncoder
            .encode(message, "UTF-8");
    writeToScreen(req, resp, message, null, null, url);
  }

  private void sendJsonResponse(HttpServletResponse response, String jsonString)
      throws IOException {
    response.setContentType("application/json");
    PrintWriter pw = response.getWriter();
    pw.write(jsonString);
    pw.close();
  }

  private class ConfigContainer {
    public IConfig c = null;
    public DomainConfig dc = null;
    public boolean add = false;
  }
}

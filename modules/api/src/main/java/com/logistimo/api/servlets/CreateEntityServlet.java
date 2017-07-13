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

package com.logistimo.api.servlets;

import com.logistimo.AppFactory;
import com.logistimo.auth.SecurityMgr;
import com.logistimo.auth.service.AuthenticationService;
import com.logistimo.auth.service.impl.AuthenticationServiceImpl;
import com.logistimo.auth.utils.SecurityUtils;
import com.logistimo.auth.utils.SessionMgr;
import com.logistimo.communications.MessageHandlingException;
import com.logistimo.communications.service.MessageService;
import com.logistimo.config.entity.IConfig;
import com.logistimo.config.models.ConfigValidator;
import com.logistimo.config.models.ConfigurationException;
import com.logistimo.config.models.DomainConfig;
import com.logistimo.config.models.InventoryConfig;
import com.logistimo.config.models.Permissions;
import com.logistimo.config.service.ConfigurationMgmtService;
import com.logistimo.config.service.impl.ConfigurationMgmtServiceImpl;
import com.logistimo.constants.Constants;
import com.logistimo.constants.SourceConstants;
import com.logistimo.dao.JDOUtils;
import com.logistimo.domains.entity.IDomain;
import com.logistimo.domains.service.DomainsService;
import com.logistimo.domains.service.impl.DomainsServiceImpl;
import com.logistimo.entities.entity.IKiosk;
import com.logistimo.entities.entity.IKioskLink;
import com.logistimo.entities.entity.IPoolGroup;
import com.logistimo.entities.service.EntitiesService;
import com.logistimo.entities.service.EntitiesServiceImpl;
import com.logistimo.exception.InvalidDataException;
import com.logistimo.exception.LogiException;
import com.logistimo.inventory.TransactionUtil;
import com.logistimo.inventory.dao.IInvntryDao;
import com.logistimo.inventory.dao.ITransDao;
import com.logistimo.inventory.dao.impl.InvntryDao;
import com.logistimo.inventory.dao.impl.TransDao;
import com.logistimo.inventory.entity.IInvntry;
import com.logistimo.inventory.entity.ITransaction;
import com.logistimo.inventory.service.InventoryManagementService;
import com.logistimo.inventory.service.impl.InventoryManagementServiceImpl;
import com.logistimo.logger.XLog;
import com.logistimo.materials.entity.IMaterial;
import com.logistimo.materials.service.MaterialCatalogService;
import com.logistimo.materials.service.impl.MaterialCatalogServiceImpl;
import com.logistimo.orders.OrderResults;
import com.logistimo.orders.OrderUtils;
import com.logistimo.orders.entity.IOrder;
import com.logistimo.orders.service.OrderManagementService;
import com.logistimo.orders.service.impl.OrderManagementServiceImpl;
import com.logistimo.pagination.Results;
import com.logistimo.security.SecureUserDetails;
import com.logistimo.services.ObjectNotFoundException;
import com.logistimo.services.ServiceException;
import com.logistimo.services.Services;
import com.logistimo.services.impl.PMF;
import com.logistimo.services.taskqueue.ITaskService;
import com.logistimo.tags.TagUtil;
import com.logistimo.users.entity.IUserAccount;
import com.logistimo.users.service.UsersService;
import com.logistimo.users.service.impl.UsersServiceImpl;
import com.logistimo.utils.BigUtil;
import com.logistimo.utils.MsgUtil;
import com.logistimo.utils.StringUtil;

import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.net.MalformedURLException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

import javax.jdo.PersistenceManager;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

@SuppressWarnings("serial")
public class CreateEntityServlet extends SgServlet {

  private static final XLog xLogger = XLog.getLog(CreateEntityServlet.class);
  private static final String CREATEENTITY_TASK_URL = "/task/createentity";
  private static final String CONFIGURATION_SERVLET_TASK_URL = "/task/createconfig";

  private static ITaskService taskService = AppFactory.get().getTaskService();
  private static ITransDao transDao = new TransDao();
  private static IInvntryDao invDao = new InvntryDao();

  // Set batch parameters from form into a transaction object
  private static void setBatchParameters(String batchId, ITransaction trans,
                                         Map<String, String[]> transDetails) {
    Long materialId = trans.getMaterialId();
    String batchExpiryStr = null, batchManufacturer = null, batchManufactured = null;
    String paramBase = "batchexpiry_" + materialId;
    String param = paramBase + "_" + batchId;
    if (transDetails.containsKey(param)) {
      batchExpiryStr = transDetails.get(param)[0];
    } else if (transDetails.containsKey(paramBase)) {
      batchExpiryStr = transDetails.get(paramBase)[0];
    }
    paramBase = "batchmanufacturer_" + materialId;
    param = paramBase + "_" + batchId;
    if (transDetails.containsKey(param)) {
      batchManufacturer = transDetails.get(param)[0].trim();
    } else if (transDetails.containsKey(paramBase)) {
      batchManufacturer = transDetails.get(paramBase)[0].trim();
    }
    paramBase = "batchmanufactured_" + materialId;
    param = paramBase + "_" + batchId;
    if (transDetails.containsKey(param)) {
      batchManufactured = transDetails.get(param)[0];
    } else if (transDetails.containsKey(paramBase)) {
      batchManufactured = transDetails.get(paramBase)[0];
    }
    xLogger.fine("Batch expiry = {0}, manufactured = {1}", batchExpiryStr, batchManufactured);
    // Set batch details into transaction object
    TransactionUtil
        .setBatchData(trans, batchId, batchExpiryStr, batchManufacturer, batchManufactured);
  }

  // Get a stock count transaction
  private static ITransaction getStockCountTrans(Long domainId, Long kioskId, Long materialId,
                                                 BigDecimal stock, Date t, String userId) {
    ITransaction trans = JDOUtils.createInstance(ITransaction.class);
    trans.setDomainId(domainId);
    trans.setKioskId(kioskId);
    trans.setMaterialId(materialId);
    trans.setQuantity(stock);
    trans.setSourceUserId(userId);
    trans.setTimestamp(t);
    trans.setType(ITransaction.TYPE_PHYSICALCOUNT);
    transDao.setKey(trans);
    return trans;
  }

  @Override
  public void processGet(HttpServletRequest req, HttpServletResponse resp,
                         ResourceBundle backendMessages, ResourceBundle messages)
      throws ServletException, IOException, ServiceException {
    processPost(req, resp, backendMessages, messages);
  }

  @Override
  public void processPost(HttpServletRequest req, HttpServletResponse resp,
                          ResourceBundle backendMessages, ResourceBundle messages)
      throws ServletException, IOException, ServiceException {
    String entityType = req.getParameter("type"); // KIOSK or POOLGROUP or
    // USERACCOUNT or MATERIAL, etc.
    String entityAction = req.getParameter("action"); // CREATE or MODIFY OR
    // DELETE
    xLogger.info("Type:" + entityType + " | Action: " + entityAction);

    if (entityAction == null || entityAction.isEmpty() || entityType == null || entityType
        .isEmpty()) {
      writeToScreen(req, resp,
          "No action or type specified. Cannot proceed with request. [action = " + entityAction
              + ", type = " + entityType, Constants.VIEW_HOME);
      return;
    }
    SecureUserDetails sUser = SecurityMgr.getUserDetailsIfPresent();
    // Get the user's locale
    Locale locale;
    if (sUser != null) {
      locale = sUser.getLocale();
    } else {
      String country = req.getParameter("country");
      if (country == null) {
        country = Constants.COUNTRY_DEFAULT;
      }
      String language = req.getParameter("language");
      if (language == null) {
        language = Constants.LANG_DEFAULT;
      }
      locale = new Locale(language, country);
    }
    UsersService as = null;
    EntitiesService es = null;
    ConfigurationMgmtService cms = null;
    InventoryManagementService ims = null;
    OrderManagementService oms = null;
    MaterialCatalogService mcs = null;
    as = Services.getService(UsersServiceImpl.class, locale);
    es = Services.getService(EntitiesServiceImpl.class, locale);
    cms = Services.getService(ConfigurationMgmtServiceImpl.class, locale);
    ims = Services.getService(InventoryManagementServiceImpl.class, locale);
    oms = Services.getService(OrderManagementServiceImpl.class, locale);
    mcs = Services.getService(MaterialCatalogServiceImpl.class, locale);
    try {
      // Process operation
      if (entityAction.equalsIgnoreCase("create")
          && entityType.equalsIgnoreCase("kiosk")) {
        createKiosk(req, resp, es, as, backendMessages, messages);
      } else if (entityAction.equalsIgnoreCase("remove")
          && entityType.equalsIgnoreCase("kiosk")) {
        removeKiosk(req, resp, es, backendMessages, messages);
      } else if (entityAction.equalsIgnoreCase("create")
          && entityType.equalsIgnoreCase("poolgroup")) {
        createPoolgroup(req, resp, es, backendMessages, messages);
      } else if (entityAction.equalsIgnoreCase("remove")
          && entityType.equalsIgnoreCase("poolgroup")) {
        removePoolgroups(req, resp, es, backendMessages, messages);
      } else if (entityAction.equalsIgnoreCase("modify")
          && entityType.equalsIgnoreCase("kiosk")) {
        modifyKiosk(req, resp, es, as, backendMessages, messages);
      } else if (entityAction.equalsIgnoreCase("modify")
          && entityType.equalsIgnoreCase("poolgroup")) {
        modifyPoolgroup(req, resp, es, backendMessages, messages);
      } else if (entityAction.equalsIgnoreCase("create")
          && entityType.equalsIgnoreCase("kioskowner")) {
        createKioskOwner(req, resp, as, backendMessages, messages);
      } else if (entityAction.equalsIgnoreCase("modify")
          && entityType.equalsIgnoreCase("kioskowner")) {
        modifyKioskOwner(req, resp, as, backendMessages, messages);
      } else if ((entityAction.equalsIgnoreCase("modify") || entityAction.equalsIgnoreCase("reset"))
          && entityType.equalsIgnoreCase("password")) {
        modifyKioskOwnerPassword(req, resp, as, backendMessages, messages);
      } else if (entityAction.equalsIgnoreCase("remove")
          && entityType.equalsIgnoreCase("kioskowner")) {
        removeKioskOwner(req, resp, as, backendMessages, messages);
      } else if (entityAction.equalsIgnoreCase("disable")
          && entityType.equalsIgnoreCase("kioskowner")) {
        disableOrEnableKioskOwner(req, resp, as, false, backendMessages, messages);
      } else if (entityAction.equalsIgnoreCase("enable")
          && entityType.equalsIgnoreCase("kioskowner")) {
        disableOrEnableKioskOwner(req, resp, as, true, backendMessages, messages);
      } else if (entityAction.equalsIgnoreCase("create")
          && entityType.equalsIgnoreCase("material")) {
        addMaterial(req, resp, mcs, backendMessages, messages);
      } else if (entityAction.equalsIgnoreCase("modify")
          && entityType.equalsIgnoreCase("material")) {
        modifyMaterial(req, resp, mcs, backendMessages, messages);
      } else if (entityAction.equalsIgnoreCase("remove")
          && entityType.equalsIgnoreCase("materials")) {
        removeMaterials(req, resp, mcs, backendMessages, messages);
      } else if (entityAction.equalsIgnoreCase("add")
          && entityType.equalsIgnoreCase("materialtokiosk")) {
        addMaterialsToKiosk(req, resp, es, ims, backendMessages, messages);
      } else if (entityAction.equalsIgnoreCase("modify")
          && entityType.equalsIgnoreCase("materialtokiosk")) {
        editMaterialsToKiosk(req, resp, es, ims, backendMessages, messages);
      } else if (entityAction.equalsIgnoreCase("remove")
          && entityType.equalsIgnoreCase("materialtokiosk")) {
        removeMaterialsFromKiosk(req, resp, es, ims, backendMessages, messages);
      } else if (entityAction.equalsIgnoreCase("create")
          && entityType.equalsIgnoreCase("domain")) {
        addDomain(req, resp);
      } else if (entityAction.equalsIgnoreCase("remove")
          && entityType.equalsIgnoreCase("domain")) {
        removeDomains(req, resp);
      } else if (entityAction.equalsIgnoreCase("switch")
          && entityType.equalsIgnoreCase("domain")) {
        switchDomain(req, resp, as);
      } else if ((entityAction.equalsIgnoreCase("create") || entityAction.equals("modify"))
          && entityType.equalsIgnoreCase("kiosklink")) {
        createOrModifyKioskLink(req, resp, es, backendMessages, messages);
      } else if (entityAction.equalsIgnoreCase("permissions")
          && entityType.equalsIgnoreCase("kiosk")) {
        setKioskPermissions(req, resp, es, backendMessages, messages);
      } else if (entityAction.equalsIgnoreCase("remove")
          && entityType.equalsIgnoreCase("kiosklink")) {
        removeKioskLinks(req, resp, es, backendMessages, messages);
      } else if (entityAction.equalsIgnoreCase("create")
          && entityType.equalsIgnoreCase("system_configuration")) {
        createSystemConfiguration(req, resp, cms);
      } else if (entityAction.equalsIgnoreCase("modify")
          && entityType.equalsIgnoreCase("system_configuration")) {
        modifySystemConfiguration(req, resp, cms);
      } else if (entityAction.equals("create")
          && entityType.equals("transaction")) {
        createTransactions(req, resp, ims, backendMessages, messages);
      } else if (entityAction.equals("remove")
          && entityType.equals("transaction")) {
        undoTransactions(req, resp, ims, backendMessages, messages);
      } else if (entityAction.equals("create")
          && entityType.equals("order")) {
        createOrders(req, resp, oms, ims, mcs, backendMessages, messages);
      } else if (entityAction.equals("check") && entityType.equals("kioskowner")) {
        checkIfUserExists(req, resp, as, backendMessages, messages);
      } else if (entityType.equals("materialstokiosks")) {
        addOrRemoveMaterialsForMultipleKiosks(req, resp, es, backendMessages, messages);
      } else if (entityAction.equals("saveordering")) {
        saveOrdering(req, resp, es, backendMessages, messages, entityType);
      } else if (entityAction.equals("resetordering")) {
        resetOrdering(req, resp, es, backendMessages, messages, entityType);
      } else if (entityType.equals("kioskowner") && entityAction.equals("setuipref")) {
        setUiPreference(req, resp, as, backendMessages, messages);
      } else {
        xLogger.severe("Unsupported action or type: {0}, {1}", entityAction, entityType);
      }
    } catch (Exception e) {
      xLogger.severe("Failed to create", e);
      throw new ServiceException(e.getMessage());
    }
  }

  private void addMaterialsToKiosk(HttpServletRequest req,
                                   HttpServletResponse resp, EntitiesService as,
                                   InventoryManagementService ims, ResourceBundle backendMessages,
                                   ResourceBundle messages)
      throws ServiceException, IOException {
    xLogger.fine("Entered addMaterialsToKiosk");
    String message = null;
    // Get the materials IDs
    String[] materialIds = req.getParameterValues("materialid");
    if (materialIds == null || materialIds.length == 0) {
      writeToScreen(req, resp, "No materials were selected", Constants.VIEW_KIOSKMATERIALS);
      return;
    }
    // Get kiosk Name
    String kioskName = req.getParameter("kioskname");
    // Get kiosk Id
    String kioskIdStr = req.getParameter("kioskid");
    // Get initial stock-level, if specified
    String stockStr = req.getParameter("stock");
    // Get the source user Id if specified
    String sourceUserId = req.getParameter("sourceuserid");
    boolean
        overwrite =
        req.getParameter("overwrite") != null && Boolean
            .parseBoolean(req.getParameter("overwrite"));
    Long kioskId = null;
    // Get initial stock level, if any, and kioskId
    BigDecimal stock = BigDecimal.ZERO;
    try {
      kioskId = Long.valueOf(kioskIdStr);
      if (kioskName == null) {
        kioskName = as.getKiosk(kioskId).getName();
      }
      if (stockStr != null && !stockStr.isEmpty()) {
        stock = new BigDecimal(stockStr);
      }
    } catch (NumberFormatException e) {
      xLogger.severe(
          "Invalid number for kioskId or initial stock - kioskId = {0}, initial stock = {1}",
          kioskIdStr, stockStr);
      writeToScreen(req, resp, "Error: Invalid kiosk identifier", Constants.VIEW_KIOSKMATERIALS);
      return;
    }
    // Check if request is coming from a task
    boolean isTask = req.getRequestURI().contains("/task/");
    // Get the domain Id for user
    SecureUserDetails sUser = SecurityMgr.getUserDetailsIfPresent();
    Long domainId = null;
    // NOTE: sUser can be null, when this is accessed via a task during multi-kiosk addition of materials
    if (sUser != null) {
      domainId = SessionMgr.getCurrentDomain(req.getSession(), sUser.getUsername());
    }
    if (domainId == null) {
      String dIdStr = req.getParameter("domainid");
      if (dIdStr != null && !dIdStr.isEmpty()) {
        domainId = Long.valueOf(dIdStr);
      }
    }
    // Get the source user Id
    String suId = sourceUserId;
    if ((suId == null || suId.isEmpty()) && sUser != null) {
      suId = sUser.getUsername();
    }
    // Form the stock update transactions, in case intial stock is non-zero
    boolean hasInitialStock = BigUtil.greaterThanZero(stock);
    List<ITransaction> stockCountTransactions = null;
    if (hasInitialStock) {
      stockCountTransactions = new ArrayList<>();
    }
    // Read the rest of the data and form Inventory List
    PersistenceManager pm = PMF.get().getPersistenceManager();
    List<IInvntry> inventories = new ArrayList<>();
    Date now = new Date();

    DomainConfig dc = DomainConfig.getInstance(domainId);
    boolean
        isDurationOfStock =
        dc.getInventoryConfig().getMinMaxType() == InventoryConfig.MIN_MAX_DOS;
    String minMaxDur = dc.getInventoryConfig().getMinMaxDur();
    boolean isManual = dc.getInventoryConfig().getConsumptionRate() == InventoryConfig.CR_MANUAL;
    try {
      for (String materialIdStr : materialIds) {
        try {
          Long materialId = Long.valueOf(materialIdStr);
          // Get the inventory request parameters
          // Min./Max. stock
          BigDecimal reorderLevel = BigDecimal.ZERO;
          BigDecimal maxStock = BigDecimal.ZERO;
          String reorderLevelStr = req.getParameter("reorderlevel" + materialIdStr);
          if (reorderLevelStr != null && !reorderLevelStr.isEmpty()) {
            reorderLevel = new BigDecimal(reorderLevelStr.trim());
          }
          String maxStr = req.getParameter("max" + materialIdStr);
          if (maxStr != null && !maxStr.isEmpty()) {
            maxStock = new BigDecimal(maxStr);
          }
          // Min./Max. duration of stock
          BigDecimal minStockDur = BigDecimal.ZERO;
          BigDecimal maxStockDur = BigDecimal.ZERO;
          String minStockDurStr = req.getParameter("minDur" + materialIdStr);
          if (minStockDurStr != null && !minStockDurStr.isEmpty()) {
            minStockDur = new BigDecimal(minStockDurStr.trim());
          }
          String maxStockDurStr = req.getParameter("maxDur" + materialIdStr);
          if (maxStockDurStr != null && !maxStockDurStr.isEmpty()) {
            maxStockDur = new BigDecimal(maxStockDurStr);
          }
          // Manual consumption rate, if any
          BigDecimal crManual = BigDecimal.ZERO;
          String
              crManualStr =
              req.getParameter("cr" + materialIdStr); // manual consumption rate, if specified
          if (crManualStr != null && !crManualStr.isEmpty()) {
            crManual = new BigDecimal(crManualStr);
          }
          // Price/tax
          BigDecimal price = BigDecimal.ZERO, tax = BigDecimal.ZERO;
          String priceStr = req.getParameter("price" + materialIdStr);
          if (priceStr != null && !priceStr.isEmpty()) {
            price = new BigDecimal(priceStr);
          }
          String taxStr = req.getParameter("tax" + materialIdStr);
          if (taxStr != null && !taxStr.isEmpty()) {
            tax = new BigDecimal(taxStr);
          }
          float serviceLevel = 85F;
          String serviceLevelStr = req.getParameter("servicelevel" + materialIdStr);
          if (serviceLevelStr != null && !serviceLevelStr.isEmpty()) {
            serviceLevel = Float.parseFloat(serviceLevelStr);
          }
          String invModel = req.getParameter("invmodel" + materialIdStr);
          String materialName = req.getParameter("materialname" + materialIdStr);
          if (materialName == null || materialName.isEmpty()) {
            try {
              IMaterial m = JDOUtils.getObjectById(IMaterial.class, materialId, pm);
              materialName = m.getUniqueName();
            } catch (Exception e) {
              xLogger.warn("{0} when getting material with ID {1} in domain {2}: {3}",
                  e.getClass().getName(), materialId, domainId, e.getMessage());
            }
          }
          // Check if binary valued or not
          boolean
              isBinaryValued =
              IMaterial.TYPE_BINARY.equals(req.getParameter("datatype" + materialIdStr));
          // Form the inventory object
          IInvntry inv = JDOUtils.createInstance(IInvntry.class);
          inv.setDomainId(domainId);
          inv.setKioskId(kioskId);
          inv.setMaterialId(materialId);
          inv.setKioskName(kioskName);
          inv.setMaterialName(materialName);
          inv.setConsumptionRateManual(crManual);
          if (isDurationOfStock) {
            inv.setMinDuration(minStockDur);
            inv.setMaxDuration(maxStockDur);
          } else {
            inv.setMinDuration(null);
            inv.setMaxDuration(null);
            inv.setReorderLevel(reorderLevel);
            inv.setMaxStock(maxStock);
          }
          if (isManual && isDurationOfStock) {
            BigDecimal cr = ims.getDailyConsumptionRate(inv);
            BigDecimal mul = BigDecimal.ONE;
            if (Constants.FREQ_WEEKLY.equals(minMaxDur)) {
              mul = Constants.WEEKLY_COMPUTATION;
            } else if (Constants.FREQ_MONTHLY.equals(minMaxDur)) {
              mul = Constants.MONTHLY_COMPUTATION;
            }
            inv.setReorderLevel(minStockDur.multiply(mul).multiply(cr));
            inv.setMaxStock(maxStockDur.multiply(mul).multiply(cr));
          }
          inv.setRetailerPrice(price);
          inv.setTax(tax);
          inv.setServiceLevel(serviceLevel);
          inv.setInventoryModel(invModel);
          inv.setTimestamp(now);
          inv.setUpdatedBy(sourceUserId);
          if (isBinaryValued) {
            inv.setStock(BigDecimal.ONE); // ensure that the default setting is 1 (0 otherwise)
          }
          // Add to list
          inventories.add(inv);
          // Add a stock count transaction, if needed
          if (hasInitialStock) {
            stockCountTransactions
                .add(getStockCountTrans(domainId, kioskId, materialId, stock, now, suId));
          }
        } catch (NumberFormatException e) {
          xLogger.warn("Invalid number format: ", e);
        }
      }
    } finally {
      pm.close();
    }
    // Add inventory
    try {
      ims.addInventory(domainId, inventories, overwrite, sourceUserId);
      // Add stock count transactions, if needed
      if (stockCountTransactions != null && !stockCountTransactions.isEmpty()) {
        try {
          ims.updateInventoryTransactions(domainId, stockCountTransactions);
        } catch (Exception e) {
          xLogger.severe(
              "{0} when doing physical stock count transactions for {1} inventories with initial stock {2} for kiosk {3}: {4}",
              e.getClass().getName(), stock, inventories.size(), kioskId, e.getMessage());
        }
      }
      message =
          "<b>" + String.valueOf(inventories.size()) + "</b> " + backendMessages
              .getString("materials.added") + " &nbsp;[<a href=\"javascript:window.close()\">"
              + messages.getString("close") + "</a>]"
              + "<br/><br/>" + backendMessages.getString("refreshlistmsg");
    } catch (ServiceException e) {
      xLogger.warn("Exception when adding inventory: {0}", e.getMessage(), e);
      message = backendMessages.getString("error") + ": " + e.getMessage();
    }
    req.setAttribute("nomenu",
        "true"); // ensure menu does not show in return message (in popup window)
    if (!isTask) {
      writeToScreen(req, resp, message, Constants.VIEW_KIOSKMATERIALS);
    }
  }

  @SuppressWarnings("unchecked")
  private void editMaterialsToKiosk(HttpServletRequest req,
                                    HttpServletResponse resp, EntitiesService as,
                                    InventoryManagementService ims, ResourceBundle backendMessages,
                                    ResourceBundle messages)
      throws ServiceException, IOException {
    xLogger.fine("Entered editMaterialsToKiosk");
    String message = null;
    String user = SecurityUtils.getUserDetails(req).getUsername();
    // Get the materials IDs
    String[] materialIds = req.getParameterValues("materialid");
    if (materialIds == null || materialIds.length == 0) {
      writeToScreen(req, resp, "No materials were selected", Constants.VIEW_KIOSKS);
      return;
    }
    // Get kiosk Id
    String kioskIdStr = req.getParameter("kioskid");
    Long kioskId = null;
    try {
      kioskId = Long.valueOf(kioskIdStr);
    } catch (NumberFormatException e) {
      writeToScreen(req, resp, "Error: Invalid kiosk identifier", Constants.VIEW_KIOSKS);
      return;
    }

    // Get the tag, if present
    String tag = req.getParameter("tag");
    boolean hasTag = tag != null && !tag.isEmpty();

    // Read the rest of the data and form Inventory List
    List<IInvntry> updItems = new ArrayList<IInvntry>();
    try {
      // Get inventory associated with kiosk
      List<IInvntry>
          inventories =
          ims.getInventoryByKiosk(kioskId, null).getResults(); // TODO: pagination?
      // Get a hash-map based on material Ids
      Map<String, IInvntry> invMap = new HashMap<String, IInvntry>();
      // Go through inventory and update
      Iterator<IInvntry> it = inventories.iterator();
      while (it.hasNext()) {
        IInvntry inv = it.next();
        invMap.put(inv.getMaterialId().toString(), inv);
      }
      // Update selected inventory with newer parameters
      for (int i = 0; i < materialIds.length; i++) {
        try {
          String materialIdStr = materialIds[i];
          BigDecimal reorderLevel = BigDecimal.ZERO;
          BigDecimal maxStock = BigDecimal.ZERO;
          String reorderLevelStr = req.getParameter("reorderlevel" + materialIdStr);
          if (reorderLevelStr != null && !reorderLevelStr.isEmpty()) {
            reorderLevel = new BigDecimal(reorderLevelStr);
          }
          String maxStr = req.getParameter("max" + materialIdStr);
          if (maxStr != null && !maxStr.isEmpty()) {
            maxStock = new BigDecimal(maxStr);
          }
          // Min./Max. duration of stock
          BigDecimal minStockDur = BigDecimal.ZERO;
          BigDecimal maxStockDur = BigDecimal.ZERO;
          String minStockDurStr = req.getParameter("minDur" + materialIdStr);
          if (minStockDurStr != null && !minStockDurStr.isEmpty()) {
            minStockDur = new BigDecimal(minStockDurStr.trim());
          }
          String maxStockDurStr = req.getParameter("maxDur" + materialIdStr);
          if (maxStockDurStr != null && !maxStockDurStr.isEmpty()) {
            maxStockDur = new BigDecimal(maxStockDurStr);
          }
          // Manual consumption rate, if any
          BigDecimal crManual = BigDecimal.ZERO;
          String
              crManualStr =
              req.getParameter("cr" + materialIdStr); // manual consumption rate, if specified
          if (crManualStr != null && !crManualStr.isEmpty()) {
            crManual = new BigDecimal(crManualStr);
          }
          // Price/tax
          BigDecimal price = BigDecimal.ZERO, tax = BigDecimal.ZERO;
          String priceStr = req.getParameter("price" + materialIdStr);
          if (priceStr != null && !priceStr.isEmpty()) {
            price = new BigDecimal(priceStr);
          }
          String taxStr = req.getParameter("tax" + materialIdStr);
          if (taxStr != null && !taxStr.isEmpty()) {
            tax = new BigDecimal(taxStr);
          }
          float serviceLevel = 85F;
          String serviceLevelStr = req.getParameter("servicelevel" + materialIdStr);
          if (serviceLevelStr != null && !serviceLevelStr.isEmpty()) {
            serviceLevel = Float.parseFloat(serviceLevelStr);
          }
          String invModel = req.getParameter("invmodel" + materialIdStr);
          // Update inventory object
          IInvntry inv = invMap.get(materialIdStr);
          if (inv != null) {
            inv.setReorderLevel(reorderLevel);
            inv.setMaxStock(maxStock);
            inv.setMinDuration(minStockDur);
            inv.setMaxDuration(maxStockDur);
            inv.setConsumptionRateManual(crManual);
            inv.setRetailerPrice(price);
            inv.setTax(tax);
            inv.setServiceLevel(serviceLevel);
            inv.setInventoryModel(invModel);
            inv.setUpdatedBy(user);
            // Add to list
            updItems.add(inv);
          }
        } catch (NumberFormatException e) {
          xLogger.warn("Invalid number format: " + e.getMessage());
        }
      }
      // Update inventory
      if (updItems.size() > 0) {
        ims.updateInventory(updItems, user);
        String
            viewUrl =
            "/s/setup/setup.jsp?subview=kiosks&form=kioskmaterials&kioskid=" + kioskIdStr;
        if (hasTag) {
          viewUrl += "&tag=" + tag;
        }
        message =
            "<b>" + String.valueOf(updItems.size()) + "</b> " + backendMessages
                .getString("materials.updated") + " &nbsp;[<a href=\"" + viewUrl + "\">"
                + backendMessages.getString("materials.view") + "</a>]";
      } else {
        message =
            backendMessages.getString("materials.updatednone")
                + ". &nbsp;[<a href=\"/s/setup/setup.jsp?subview=kiosks&form=kioskmaterials&kioskid="
                + kioskIdStr + "\">" + backendMessages.getString("materials.view") + "</a>]";
      }
    } catch (ServiceException e) {
      xLogger.severe("Exception when updating inventory: {0}", e.getMessage());
      message = backendMessages.getString("error") + ": " + e.getMessage();
    }

    writeToScreen(req, resp, message, Constants.VIEW_KIOSKMATERIALS);
  }

  @SuppressWarnings("unchecked")
  private void modifyMaterial(HttpServletRequest req,
                              HttpServletResponse resp,
                              MaterialCatalogService mc, ResourceBundle backendMessages,
                              ResourceBundle messages)
      throws ServiceException, IOException {
    Map<String, String[]> materialDetails = req.getParameterMap();
    materialDetails = cleanMap(materialDetails);
    SecureUserDetails sUser = SecurityMgr.getUserDetails(req.getSession());
    Long domainId = SessionMgr.getCurrentDomain(req.getSession(), sUser.getUsername());

    String message = "";
    IMaterial m = JDOUtils.createInstance(IMaterial.class);

    String matIDStr = "";
    String name = "";
    if (materialDetails.containsKey("id")) {
      matIDStr = materialDetails.get("id")[0];
      Long l = Long.parseLong(matIDStr);
      m = mc.getMaterial(l);
    }

    if (materialDetails.containsKey("vertical")) {
      m.setVertical(materialDetails.get("vertical")[0]);
    }

    if (materialDetails.containsKey("materialname")) {
      name = materialDetails.get("materialname")[0];
      if (name.equalsIgnoreCase("")) {
        message += " - material name is mandatory but is missing<br/>";
      }
      m.setName(name);
    }
    // Check if material short name is present
    if (materialDetails.containsKey("shortname")) {
      m.setShortName(materialDetails.get("shortname")[0]);
    }
    if (materialDetails.containsKey("description")) {
      m.setDescription(materialDetails.get("description")[0]);
    }

    if (materialDetails.containsKey("tags")) {
      String tags = TagUtil.getCleanTags(materialDetails.get("tags")[0], true);
      m.setTags(
          StringUtil.getList(tags, true)); // get unique list // Old: TagUtil.getTagsList( tags ) );
    }

    // Get the price values, if any
    if (materialDetails.containsKey("retailprice")) {
      String retailPrice = materialDetails.get("retailprice")[0];
      try {
        if (retailPrice != null) {
          if (!retailPrice.isEmpty()) {
            m.setMSRP(new BigDecimal(retailPrice));
          } else {
            m.setMSRP(BigDecimal.ZERO);
          }
        }
      } catch (NumberFormatException e) {
        message +=
            " - Could not parse retail price. Please enter a number. [" + e.getMessage() + "]<br/>";
      }
    }
    if (materialDetails.containsKey("retailerprice")) {
      String retailerPrice = materialDetails.get("retailerprice")[0];
      try {
        if (retailerPrice != null) {
          if (!retailerPrice.isEmpty()) {
            m.setRetailerPrice(new BigDecimal(retailerPrice));
          } else {
            m.setRetailerPrice(BigDecimal.ZERO);
          }
        }
      } catch (NumberFormatException e) {
        message +=
            " - Could not parse retailer price. Please enter a number. [" + e.getMessage()
                + "]<br/>";
      }
    }
    if (materialDetails.containsKey("saleprice")) {
      String salePrice = materialDetails.get("saleprice")[0];
      try {
        if (salePrice != null && !salePrice.isEmpty()) {
          m.setSalePrice(new BigDecimal(salePrice));
          xLogger.fine("Set sale price: {0}", salePrice);
        }
      } catch (NumberFormatException e) {
        message +=
            " - Could not parse sale price. Please enter a number. [" + e.getMessage() + "]<br/>";
      }
    }
    if (materialDetails.containsKey("currency")) {
      String currency = materialDetails.get("currency")[0];
      m.setCurrency(currency);
    }

    if (materialDetails.containsKey("isbatchenabled")) {
      String isBatchEnabled = materialDetails.get("isbatchenabled")[0];
      m.setBatchEnabled(isBatchEnabled != null && "true".equals(isBatchEnabled));
    } else {
      m.setBatchEnabled(false);
    }

    if (materialDetails.containsKey("isbatchenabledonmobile")) {
      String isBatchEnabledOnMobile = materialDetails.get("isbatchenabledonmobile")[0];
      m.setBatchEnabledOnMobile(
          isBatchEnabledOnMobile != null && "true".equals(isBatchEnabledOnMobile));
    } else {
      m.setBatchEnabledOnMobile(false);
    }

    if (materialDetails.containsKey("identifiertype")) {
      String type = materialDetails.get("identifiertype")[0];
      m.setIdentifierType(type);
    }

    if (materialDetails.containsKey("identifiervalue")) {
      m.setIdentifierValue(materialDetails.get("identifiervalue")[0]);
    }

    if (materialDetails.containsKey("datatype")) {
      m.setType(materialDetails.get("datatype")[0]);
    } else {
      m.setType(null);
    }

    if (materialDetails.containsKey("seasonal")) {
      boolean seasonal = true;
      m.setSeasonal(seasonal);
    } else {
      m.setSeasonal(false);
    }

    if (materialDetails.containsKey("additionalinfo")) {
      m.setInfo(materialDetails.get("additionalinfo")[0]);
    }
    if (materialDetails.containsKey("additionalinfocheck")) {
      m.setInfoDisplay(true);
    } else {
      m.setInfoDisplay(false);
    }
    if (materialDetails.containsKey("customid")) {
      String customId = materialDetails.get("customid")[0];
      m.setCustomId(customId);
    }
    // Temperature data
    if (materialDetails.containsKey("istemperaturesensitive")) {
      boolean isTempSensitive = "yes".equals(materialDetails.get("istemperaturesensitive")[0]);
      m.setTemperatureSensitive(isTempSensitive);
      float min = 0, max = 0;
      if (materialDetails.containsKey("mintemperature")) {
        try {
          min = Float.parseFloat(materialDetails.get("mintemperature")[0]);
        } catch (Exception e) {
          message += " - Min. temperature limit is required and must be a valid number<br/>";
        }
      }
      if (materialDetails.containsKey("maxtemperature")) {
        try {
          max = Float.parseFloat(materialDetails.get("maxtemperature")[0]);
        } catch (Exception e) {
          message += " - Max. temperature limit is required and must be a valid number<br/>";
        }
      }
      if (min == max) {
        message += " - Both min. and max. temperatures cannot be the same value<br/>";
      } else if (min > max) {
        message +=
            " - Min. temperature (" + min + ") cannot be lower than max. temperature (" + max
                + ")<br/>";
      } else {
        m.setTemperatureMax(max);
        m.setTemperatureMin(min);
      }
    } else {
      m.setTemperatureSensitive(false);
      m.setTemperatureMin(0F);
      m.setTemperatureMax(0F);
    }
    // Send response
    if (message.isEmpty()) {
      mc.updateMaterial(m, domainId);
      message =
          "<b>" + name + "</b> " + backendMessages.getString("updated.success")
              + " &nbsp;[<a href=\"/s/setup/setup.jsp?subview=materials&form=materialdetails&id="
              + matIDStr + "\">" + backendMessages.getString("material.view") + "</a>]";
    } else {
      // NOTE: This is not likely to be used much, given of JavaScript validation in the front-end.
      message = "The following error(s) were encountered:<br/><br/>"
          + message
          + "<br/><br/>Please go back using browser's Back button, correct them and retry.";
    }

    writeToSetupScreen(req, resp, message, Constants.VIEW_MATERIALS);
  }

  @SuppressWarnings("unchecked")
  private void addMaterial(HttpServletRequest req, HttpServletResponse resp,
                           MaterialCatalogService mc,
                           ResourceBundle backendMessages, ResourceBundle messages)
      throws ServiceException, IOException {
    Map<String, String[]> materialDetails = req.getParameterMap();
    materialDetails = cleanMap(materialDetails);

    IMaterial m = JDOUtils.createInstance(IMaterial.class);
    String name = "";
    if (materialDetails.containsKey("vertical")) {
      m.setVertical(materialDetails.get("vertical")[0]);
    }

    String message = "";

    if (materialDetails.containsKey("materialname")) {
      name = materialDetails.get("materialname")[0];
      if (name.equalsIgnoreCase("")) {
        message += " - material name is mandatory but is missing<br/>";
      }
      m.setName(name);
    }
    // Check if material short name is present
    if (materialDetails.containsKey("shortname")) {
      m.setShortName(materialDetails.get("shortname")[0]);
    }
    if (materialDetails.containsKey("description")) {
      m.setDescription(materialDetails.get("description")[0]);
    }

    if (materialDetails.containsKey("tags")) {
      String tags = TagUtil.getCleanTags(materialDetails.get("tags")[0], true);
      m.setTags(StringUtil
          .getList(tags, true)); // get unique tag list // old: TagUtil.getTagsList( tags ) );
    }

    // Get the price values, if any
    if (materialDetails.containsKey("retailprice")) {
      String retailPrice = materialDetails.get("retailprice")[0];
      try {
        if (retailPrice != null && !retailPrice.isEmpty()) {
          m.setMSRP(new BigDecimal(retailPrice));
        }
      } catch (NumberFormatException e) {
        message +=
            " - Could not parse retail price. Please enter a number. [" + e.getMessage() + "]<br/>";
      }
    }
    if (materialDetails.containsKey("retailerprice")) {
      String retailerPrice = materialDetails.get("retailerprice")[0];
      try {
        if (retailerPrice != null && !retailerPrice.isEmpty()) {
          m.setRetailerPrice(new BigDecimal(retailerPrice));
        }
      } catch (NumberFormatException e) {
        message +=
            " - Could not parse retailer's price. Please enter a number. [" + e.getMessage()
                + "]<br/>";
      }
    }
    if (materialDetails.containsKey("saleprice")) {
      String salePrice = materialDetails.get("saleprice")[0];
      try {
        if (salePrice != null && !salePrice.isEmpty()) {
          m.setSalePrice(new BigDecimal(salePrice));
        }
      } catch (NumberFormatException e) {
        message +=
            " - Could not parse sale price. Please enter a number. [" + e.getMessage() + "]<br/>";
      }
    }
    if (materialDetails.containsKey("currency")) {
      String currency = materialDetails.get("currency")[0];
      m.setCurrency(currency);
    }

    if (materialDetails.containsKey("isbatchenabled")) {
      String isBatchEnabled = materialDetails.get("isbatchenabled")[0];
      m.setBatchEnabled(isBatchEnabled != null && "true".equals(isBatchEnabled));
    }

    if (materialDetails.containsKey("isbatchenabledonmobile")) {
      String isBatchEnabledOnMobile = materialDetails.get("isbatchenabledonmobile")[0];
      m.setBatchEnabledOnMobile(
          isBatchEnabledOnMobile != null && "true".equals(isBatchEnabledOnMobile));
    } else {
      m.setBatchEnabledOnMobile(false);
    }

    if (materialDetails.containsKey("identifiertype")) {
      String type = materialDetails.get("identifiertype")[0];
      m.setIdentifierType(type);
    }

    if (materialDetails.containsKey("identifiervalue")) {
      m.setIdentifierValue(materialDetails.get("identifiervalue")[0]);
    }
    if (materialDetails.containsKey("datatype")) {
      m.setType(materialDetails.get("datatype")[0]);
    } else {
      m.setType(null);
    }
    if (materialDetails.containsKey("seasonal")) {
      boolean seasonal = true;
      m.setSeasonal(seasonal);
    } else {
      m.setSeasonal(false);
    }
    if (materialDetails.containsKey("additionalinfo")) {
      m.setInfo(materialDetails.get("additionalinfo")[0]);
    }
    if (materialDetails.containsKey("additionalinfocheck")) {
      m.setInfoDisplay(true);
    } else {
      m.setInfoDisplay(false);
    }
    if (materialDetails.containsKey("customid")) {
      String customId = materialDetails.get("customid")[0];
      m.setCustomId(customId);
    }
    // Temperature data
    if (materialDetails.containsKey("istemperaturesensitive")) {
      boolean isTempSensitive = "yes".equals(materialDetails.get("istemperaturesensitive")[0]);
      m.setTemperatureSensitive(isTempSensitive);
      float min = 0, max = 0;
      if (materialDetails.containsKey("mintemperature")) {
        try {
          min = Float.parseFloat(materialDetails.get("mintemperature")[0]);
        } catch (Exception e) {
          message += " - Min. temperature limit is required and must be a valid number<br/>";
        }
      }
      if (materialDetails.containsKey("maxtemperature")) {
        try {
          max = Float.parseFloat(materialDetails.get("maxtemperature")[0]);
        } catch (Exception e) {
          message += " - Max. temperature limit is required and must be a valid number<br/>";
        }
      }
      if (min == max) {
        message += " - Both min. and max. temperatures cannot be the same value<br/>";
      } else if (min > max) {
        message +=
            " - Min. temperature (" + min + ") cannot be lower than max. temperature (" + max
                + ")<br/>";
      } else {
        m.setTemperatureMax(max);
        m.setTemperatureMin(min);
      }
    } else {
      m.setTemperatureSensitive(false);
      m.setTemperatureMin(0F);
      m.setTemperatureMax(0F);
    }
    // Get the domain ID
    SecureUserDetails sUser = SecurityMgr.getUserDetails(req.getSession());
    Long domainId = SessionMgr.getCurrentDomain(req.getSession(), sUser.getUsername());

    if (message.isEmpty()) {
      Long matID = mc.addMaterial(domainId, m);
      message =
          "<b>" + name + "</b> " + backendMessages.getString("created.success")
              + " &nbsp;[<a href=\"/s/setup/setup.jsp?subview=materials&form=materialdetails&id="
              + matID.toString() + "\">" + backendMessages.getString("material.view")
              + "</a>] &nbsp; [<a href=\"/s/setup/setup.jsp?subview=materials&form=addmaterial\">"
              + messages.getString("add") + " " + messages.getString("new") + " " + messages
              .getString("material") + "</a>]";
    } else {
      message = "The following error(s) were encountered:<br/><br/>"
          + message
          + "<br/><br/>Please go back using browser's Back button, correct them and retry.";
    }

    writeToSetupScreen(req, resp, message, Constants.VIEW_MATERIALS);
  }

  @SuppressWarnings("unchecked")
  private void removeMaterials(HttpServletRequest req,
                               HttpServletResponse resp,
                               MaterialCatalogService mc, ResourceBundle backendMessages,
                               ResourceBundle messages)
      throws ServiceException, IOException {
    xLogger.fine("Entered removeMaterials");
    Map<String, String[]> materialDetails = req.getParameterMap();
    materialDetails = cleanMap(materialDetails);
    SecureUserDetails sUser = SecurityMgr.getUserDetails(req.getSession());
    // Get domain Id
    String domainIdStr = req.getParameter("domainid");
    Long domainId = null;
    if (domainIdStr != null && !domainIdStr.isEmpty()) {
      domainId = Long.valueOf(domainIdStr);
    }
    boolean execute = req.getParameter("execute") != null;
    String message = "";
    if (materialDetails.containsKey("materialids")) {
      String[] matIDArr = req.getParameterValues("materialids");
      if (!execute) { // schedule
        // Schedule a separate task for deletion of each material (given its associated entities also have to be removed)
        Map<String, String> params = new HashMap<String, String>();
        params.put("action", "remove");
        params.put("type", "materials");
        params.put("domainid", domainIdStr);
        params.put("execute", "true"); // now add the "execute" indicator here
        for (int i = 0; i < matIDArr.length; i++) {
          params.put("materialids", matIDArr[i]);
          try {
            taskService.schedule(taskService.QUEUE_DEFAULT, CREATEENTITY_TASK_URL, params, null,
                taskService.METHOD_POST, domainId, sUser.getUsername(), "REMOVE_MATERIALS");
          } catch (Exception e) {
            xLogger.warn("{0} when scheduling task to delete material {1} in domain {2}: {3}",
                e.getClass().getName(), matIDArr[i], domainId, e.getMessage());
          }
        }
        // Get the return message
        message =
            messages.getString("scheduledtasks") + " " + messages.getString("remove") + " <b>"
                + matIDArr.length + "</b>" + " " + messages.getString("materials")
                + " &nbsp;[<a href=\"/s/setup/setup.jsp?subview=materials\">" + backendMessages
                .getString("materials.view") + "</a>]";
        message +=
            "<br/><br/>NOTE: It may take some time for a material(s) and all its associated objects to be removed.";
        writeToSetupScreen(req, resp, message, Constants.VIEW_MATERIALS);
        return;
      }
      ArrayList<Long> materialIDs = new ArrayList<Long>();
      for (String s : matIDArr) {
        Long l = Long.parseLong(s);
        materialIDs.add(l);
      }
      try {
        mc.deleteMaterials(domainId, materialIDs);
      } catch (Exception e) {
        xLogger.severe("{0} when removing materials {1} in domain {2}: {3}", e.getClass().getName(),
            materialIDs, domainId, e.getMessage());
      }
    }
  }

  private void removeMaterialsFromKiosk(HttpServletRequest req,
                                        HttpServletResponse resp, EntitiesService as,
                                        InventoryManagementService ims,
                                        ResourceBundle backendMessages, ResourceBundle messages)
      throws ServiceException, IOException {
    xLogger.fine("Entering removeMaterialsFromKiosk");
    String kioskIdStr = req.getParameter("kioskid"); //materialDetails.get("kioskid")[0];
    String domainIdStr = req.getParameter("domainid");
    Long kioskId = null, domainId = null;
    if (kioskIdStr != null && !kioskIdStr.isEmpty()) {
      kioskId = Long.valueOf(kioskIdStr);
    }
    if (domainIdStr != null && !domainIdStr.isEmpty()) {
      domainId = Long.valueOf(domainIdStr);
    }
    // Get tag, if present, for sending back in response URL
    String tag = req.getParameter("tag");
    boolean hasTag = tag != null && !tag.isEmpty();
    boolean isTask = req.getRequestURI().contains("/task/");
    int numMaterials = 0;
    List<Long> materialIds = new ArrayList<Long>();
    String[] matIDArr = req.getParameterValues("materialid");
    if (matIDArr != null) {
      numMaterials = matIDArr.length;
    }
    for (String materialIDStr : matIDArr) {
      Long materialId = Long.parseLong(materialIDStr);
      materialIds.add(materialId);
    }
    String message = "";
    // Remove the materials
    try {
      ims.removeInventory(domainId, kioskId, materialIds);
      if (!isTask) {
        // Get the kiosk name
        IKiosk k = as.getKiosk(kioskId, false);
        String kioskName = k.getName();
        if (numMaterials > 0) {
          String
              url =
              "/s/setup/setup.jsp?subview=kiosks&form=kioskmaterials&kioskid=" + kioskIdStr;
          if (hasTag) {
            url += "&tag=" + tag;
          }
          message =
              "<b>" + String.valueOf(numMaterials) + "</b> " + backendMessages
                  .getString("deleted.success") + " " + messages.getString("in") + " '" + kioskName
                  + "' &nbsp;[<a href=\"" + url + "\">" + backendMessages
                  .getString("materials.view") + "</a>]<br/><br/>" + backendMessages
                  .getString("note") + ": " + backendMessages.getString("deleted.delaynote") + ".";
        }
      }
    } catch (ServiceException e) {
      xLogger.warn("Error when removing materials {0} from kiosk {1}", materialIds, kioskId);
      message = "Error: " + e.getMessage();
    }
    // Check if this request is coming as a task
    if (!isTask) // ensure that we go to writeToScreen only when request is not from a task
    {
      writeToScreen(req, resp, message, Constants.VIEW_KIOSKMATERIALS);
    }
  }

  /**
   *
   * @param req
   * @param resp
   * @param as
   * @throws ServiceException
   * @throws IOException
   */
  @SuppressWarnings("unchecked")
  private void removeKioskOwner(HttpServletRequest req,
                                HttpServletResponse resp, UsersService as,
                                ResourceBundle backendMessages, ResourceBundle messages)
      throws ServiceException, IOException {
    Map<String, String[]> userDetails = req.getParameterMap();
    userDetails = cleanMap(userDetails);
    String message = "";
    String domainIdStr = req.getParameter("domainid");
    Long domainId = null;
    if (domainIdStr != null && !domainIdStr.isEmpty()) {
      domainId = Long.valueOf(domainIdStr);
    }
    if (userDetails.containsKey("userid")) {
      String[]
          userIDArr =
          req.getParameterValues("userid"); // userDetails.get("userids")[0].split(",");
      List<String> ids = new ArrayList<String>();
      if (userIDArr != null) {
        for (int i = 0; i < userIDArr.length; i++) {
          try {
            IUserAccount u = as.getUserAccount(userIDArr[i]);
            EntitiesService es = Services.getService(EntitiesServiceImpl.class);
            List results = es.getKioskIdsForUser(u.getUserId(), null, null).getResults();
            if (results !=null && !results.isEmpty()) {
              message +=
                  " - " + u.getFullName() + " " + backendMessages.getString("user.cannotdelete")
                      + " " + results.size() + " " + backendMessages
                      .getString("kiosks.lowercase") + MsgUtil.newLine();
            } else {
              ids.add(userIDArr[i]);
            }
          } catch (ObjectNotFoundException e) {
            xLogger.warn("User not found: {0}", e.getMessage());
          }
        }
      }
      // Check if selected users are associated with a kiosk or not
      if (message.length() > 0) {
        message =
            backendMessages.getString("errors.oneormore") + ":<br/><br/>" + message + "<br/><br/>";
      }
      if (ids.size() > 0) {
        as.deleteAccounts(domainId, ids, null);
        message =
            "<b>" + ids.size() + "</b> " + backendMessages.getString("deleted.success") + ". "
                + message;
      }
    }

    message +=
        " &nbsp;[<a href=\"/s/setup/setup.jsp?subview=users\">" + backendMessages
            .getString("users.view") + "</a>]";

    writeToSetupScreen(req, resp, message, Constants.VIEW_USERS);
  }

  // Enable or disable kiosk owner
  @SuppressWarnings("unchecked")
  private void disableOrEnableKioskOwner(HttpServletRequest req,
                                         HttpServletResponse resp, UsersService as,
                                         boolean enable, ResourceBundle backendMessages,
                                         ResourceBundle messages)
      throws ServiceException, IOException {
    Map<String, String[]> userDetails = req.getParameterMap();
    userDetails = cleanMap(userDetails);
    String message = "";
    String userId = null;
    if (userDetails.containsKey("id")) {
      userId = userDetails.get("id")[0];
      if (userId != null) {
        if (enable) {
          as.enableAccount(userId);
        } else {
          as.disableAccount(userId);
        }
        //message = "Successfully " + ( enable ? "enabled" : "disabled" ) + " user '" + userId + "'";
        message =
            messages.getString("user") + " '" + userId + "' " + backendMessages
                .getString("updated.success");
      }
    } else {
      message = "No user ID provided";
    }

    message +=
        " &nbsp;[<a href=\"/s/setup/setup.jsp?subview=users&form=userdetails&id=" + userId + "\">"
            + backendMessages.getString("user.view") + "</a>]";

    writeToSetupScreen(req, resp, message, Constants.VIEW_USERS);
  }

  /**
   * @throws IOException TODO We don't modify the password or userid yet
   */
  @SuppressWarnings("unchecked")
  private void modifyKioskOwner(HttpServletRequest req,
                                HttpServletResponse resp, UsersService as,
                                ResourceBundle backendMessages, ResourceBundle messages)
      throws ServiceException, IOException {
    Map<String, String[]> userDetails = req.getParameterMap();
    userDetails = cleanMap(userDetails);
    String userIDStr = "";
    String message = "";
    IUserAccount u = null;
    if (userDetails.containsKey("id")) {
      userIDStr = userDetails.get("id")[0];
      try {
        u = as.getUserAccount(userIDStr);
      } catch (ObjectNotFoundException e) {
        throw new ServiceException(e.getMessage());
      }
    } else {
      throw new InvalidDataException("User Id is mandatory field");
    }

    if (userDetails.containsKey("role")) {
      String role = userDetails.get("role")[0];
      if (role.equalsIgnoreCase("")) {
        message += " - Role is missing. Please specify a valid role.<br/>";
      } else {
        u.setRole(role);
      }
    }

    if (userDetails.containsKey("customid")) {
      String customId = userDetails.get("customid")[0];
      u.setCustomId(customId);
    }

    if (userDetails.containsKey("firstname")) {
      String name = userDetails.get("firstname")[0];
      if (name.equalsIgnoreCase("")) {
        message += " - First name is missing. Please specify the first name.<br/>";
      } else {
        u.setFirstName(name);
      }
    }

    if (userDetails.containsKey("lastname")) {
      String name = userDetails.get("lastname")[0];
      u.setLastName(name);
    }

    if (userDetails.containsKey("mobilephone")) {
      String name = userDetails.get("mobilephone")[0];
      if (name.equalsIgnoreCase("")) {
        message +=
            " - Mobile phone number is missing. Please specify the mobile phone number.<br/>";
      } else {
        u.setMobilePhoneNumber(name);
      }
    }

    // Capturing mobile phone brand, model and operator details
    if (userDetails.containsKey("phonebrand")) {
      u.setPhoneBrand(userDetails.get("phonebrand")[0]);
    }
    if (userDetails.containsKey("phonemodelnumber")) {
      u.setPhoneModelNumber(userDetails.get("phonemodelnumber")[0]);
    }
    if (userDetails.containsKey("phoneserviceprovider")) {
      u.setPhoneServiceProvider(userDetails.get("phoneserviceprovider")[0]);
    }

    if (userDetails.containsKey("landlinenumber")) {
      u.setLandPhoneNumber(userDetails.get("landlinenumber")[0]);
    }

    if (userDetails.containsKey("email")) {
      u.setEmail(userDetails.get("email")[0]);
    }

    if (userDetails.containsKey("gender")) {
      String gender = userDetails.get("gender")[0];
      u.setGender(gender);
    }

    if (userDetails.containsKey("language")) {
      String lang = userDetails.get("language")[0];
      if (lang.equalsIgnoreCase("")) {
        message += " - Language is missing. Please specify language.<br/>";
      } else {
        u.setLanguage(lang);
      }
    }
    if (userDetails.containsKey("timezone")) {
      String timezone = userDetails.get("timezone")[0];
      if (timezone.equalsIgnoreCase("")) {
        message += " - Timezone is missing. Please specify timezone.<br/>";
      } else {
        u.setTimezone(timezone);
      }
    }

    if (userDetails.containsKey("country")) {
      String country = userDetails.get("country")[0];
      if (country.equalsIgnoreCase("")) {
        message += " - Country is missing. Please specify country.<br/>";
      } else {
        u.setCountry(country);
      }
    }

    if (userDetails.containsKey("state")) {
      u.setState(userDetails.get("state")[0]);
    }

    if (userDetails.containsKey("district")) {
      u.setDistrict(userDetails.get("district")[0]);
    }

    if (userDetails.containsKey("taluk")) {
      u.setTaluk(userDetails.get("taluk")[0]);
    }

    if (userDetails.containsKey("city")) {
      u.setCity(userDetails.get("city")[0]);
    }

    if (userDetails.containsKey("pincode")) {
      u.setPinCode(userDetails.get("pincode")[0]);
    }

    if (userDetails.containsKey("street")) {
      u.setStreet(userDetails.get("street")[0]);
    }

    if (userDetails.containsKey("age")) {
      String ageStr = userDetails.get("age")[0];
      if (ageStr != null) {
        int age = 0;
        if (!ageStr.isEmpty()) {
          age = Integer.parseInt(ageStr);
        }
        u.setAge(age);
        u.setAgeType(IUserAccount.AGETYPE_YEARS);
      }
    }

    if (userDetails.containsKey("primaryentity")) {
      String pkIdStr = userDetails.get("primaryentity")[0];
      Long pkId = null;
      if (pkIdStr != null) {
        try {
          pkId = Long.valueOf(pkIdStr);
        } catch (Exception e) {
          e.printStackTrace();
        }
      }
      u.setPrimaryKiosk(pkId);
    }

    if (message.isEmpty()) {
      as.updateAccount(u, u.getUpdatedBy());
      message =
          "<b>" + u.getFullName() + "</b> " + backendMessages.getString("updated.success")
              + " &nbsp;[<a href=\"/s/setup/setup.jsp?subview=users&form=userdetails&id="
              + userIDStr + "\">" + backendMessages.getString("user.view") + "</a>]";
    } else {
      // NOTE: This is unlikely to be used due to front-end JS validations
      message = "The following error(s) were encountered:<br/>" + message +
          "<br/><br/>" +
          "Go back by clicking the browser's Back button, fix the above error(s) and retry.";
    }
    writeToSetupScreen(req, resp, message, Constants.VIEW_USERS);
  }

  /**
   * Modify the password of a given user
   */
  @SuppressWarnings("unchecked")
  private void modifyKioskOwnerPassword(HttpServletRequest req,
                                        HttpServletResponse resp, UsersService as,
                                        ResourceBundle backendMessages, ResourceBundle messages)
      throws ServiceException, IOException {
    xLogger.fine("Entered modifyKioskOwnerPassword");
    Map<String, String[]> userDetails = req.getParameterMap();
    userDetails = cleanMap(userDetails);
    String userId = "";
    IUserAccount u = null;
    // Get logged in User details
    SecureUserDetails sUser = SecurityMgr.getUserDetails(req.getSession());
    String loggedInUserId = sUser.getUsername();
    Long domainId = SessionMgr.getCurrentDomain(req.getSession(), sUser.getUsername());
    // Get the user Id
    if (userDetails.containsKey("id")) {
      userId = userDetails.get("id")[0];
      try {
        u = as.getUserAccount(userId);
      } catch (ObjectNotFoundException e) {
        throw new ServiceException(e.getMessage());
      }
    } else {
      throw new ServiceException("UserId is mandatory");
    }
    // Get the action - change or reset password
    String action = null;
    boolean resetPassword = false;
    String message = "";
    if (userDetails.containsKey("action")) {
      action = userDetails.get("action")[0];
      resetPassword = "reset".equals(action);
    }
    // Reset or change password
    if (resetPassword) {
      // Get the send type
      String sendtype = MessageService.SMS;
      if (userDetails.containsKey("sendtype")) {
        sendtype = userDetails.get("sendtype")[0];
      }
                        /*
                        String address = ( MessageService.SMS.equals( sendtype ) ? u.getMobilePhoneNumber() : u.getEmail() );
			// Get the email from the form, if any
			if ( MessageService.EMAIL.equals( sendtype ) ) {
				if ( userDetails.containsKey( "email" ) ) {
					String email = userDetails.get( "email" )[0];
					if ( email != null && !email.isEmpty() )
						address = email;
				}
			}
			*/
      AuthenticationService authenticationService = Services.getService(AuthenticationServiceImpl.class);
      String newPassword = authenticationService.generatePassword(u.getUserId());
      String msg = "Your password has been reset. Your new password is: " + newPassword;
      String logMsg = backendMessages.getString("password.reset.success.log");
      try {
        // Reset the user password
        as.changePassword(userId, null, newPassword);
        xLogger.info("Password for user " + userId + " reset to "
            + newPassword); // TODO: later remove this; this is for initial stages of evaluation
        // Send message to user
        MessageService
            ms =
            MessageService
                .getInstance(sendtype, u.getCountry(), true, domainId, loggedInUserId, null);
        ms.send(u, msg, MessageService.NORMAL, "Password updated", null, logMsg);
        message =
            backendMessages.getString("user.passwordreset") + ".<br/><br/><b>" + backendMessages
                .getString("note") + "</b>: " + backendMessages.getString("user.passwordusercheck")
                + ".";
      } catch (MessageHandlingException e) {
        message = backendMessages.getString("error") + ": " + e.getMessage();
        xLogger.severe("MessageHandlingException: " + e.getMessage());
      } catch (ServiceException e) {
        //message = "System error when resetting password. Please try again and report error.";
        message = backendMessages.getString("error") + ": " + e.getMessage();
        xLogger.severe("ServiceException: " + e.getMessage());
      }
    } else {
      // Get the old password
      String oldpassword = null;
      if (userDetails.containsKey("oldpassword")) {
        oldpassword = userDetails.get("oldpassword")[0];
        if (oldpassword.equalsIgnoreCase("")) {
          writeToScreen(req, resp, "Sorry, please provide your old password.",
              Constants.VIEW_USERS);
          return;
        }
      }
      // Check if the old password is valid
      try {
        if (as.authenticateUser(userId, oldpassword, null) == null) {
          writeToScreen(req, resp, backendMessages.getString("user.passwordinvalid") + ".",
              Constants.VIEW_USERS);
          return;
        }
      } catch (ServiceException e) {
        xLogger.severe("Exception in modifyKioskOwnerPassword when calling authenticateUser: {0}",
            e.getMessage());
        writeToScreen(req, resp, backendMessages.getString("error") + ": " + e.getMessage(),
            Constants.VIEW_USERS);
        return;
      } catch (ObjectNotFoundException e) {
        xLogger.severe("Object Not found exception in authenticateUser: {0}", e.getMessage());
        writeToScreen(req, resp, backendMessages.getString("error") + ": " + e.getMessage(),
            Constants.VIEW_USERS);
        return;
      }

      // Get the new password
      String newpassword = null;
      if (userDetails.containsKey("newpassword")) {
        newpassword = userDetails.get("newpassword")[0];
        if (newpassword.equalsIgnoreCase("")) {
          writeToScreen(req, resp, "Sorry, please provide a new password.", Constants.VIEW_USERS);
          return;
        }
      }
      // Get the new password
      String confirmpassword = null;
      if (userDetails.containsKey("confirmpassword")) {
        confirmpassword = userDetails.get("confirmpassword")[0];
        if (confirmpassword.equalsIgnoreCase("")) {
          writeToScreen(req, resp, "Sorry, please provide a confirmation of the new password.",
              Constants.VIEW_USERS);
          return;
        }
      }

      // Check if the new password and its confirmation match
      if (!newpassword.equals(confirmpassword)) {
        writeToScreen(req, resp,
            "Sorry, your new password did not match the confirmed password. Please check this once again.",
            Constants.VIEW_USERS);
        return;
      }

      // Check if new password is the same as old password
      if (newpassword.equals(oldpassword)) {
        writeToScreen(req, resp,
            "Sorry, your new password is the same as the old password. Please change this and try again.",
            Constants.VIEW_USERS);
        return;
      }

      // Change the password
      try {
        as.changePassword(userId, oldpassword, newpassword);
        message =
            backendMessages.getString("user.passwordchanged")
                + ". &nbsp; [<a href=\"/s/setup/setup.jsp?subview=users&form=userdetails&id="
                + userId + "\">" + backendMessages.getString("user.view") + "</a>]";
      } catch (ServiceException e) {
        xLogger.severe("Exception when changing password: {0}", e.getMessage());
        message = backendMessages.getString("error") + ": " + e.getMessage() + "]";
      }
    }
    writeToSetupScreen(req, resp, message, Constants.VIEW_USERS);
    xLogger.fine("Exiting modifyKioskOwnerPassword");
  }

  @SuppressWarnings("unchecked")
  private void createKioskOwner(HttpServletRequest req,
                                HttpServletResponse resp, UsersService as,
                                ResourceBundle backendMessages, ResourceBundle messages)
      throws ServiceException, IOException {
    Map<String, String[]> userDetails = req.getParameterMap();
    userDetails = cleanMap(userDetails);
    IUserAccount u = JDOUtils.createInstance(IUserAccount.class);
    String message = "";

    // Get the form parameters and update object
    if (userDetails.containsKey("userid")) {
      String name = userDetails.get("userid")[0];
      if (name.equalsIgnoreCase("")) {
        message += " - User ID is missing. Please specify user ID.<br/>";
      } else {
        u.setUserId(name);
      }
    }

    if (userDetails.containsKey("password")) {
      // NOTE: password is encoded by the AccountsService's addAccount API
      String pwd = userDetails.get("password")[0];
      if (pwd.equalsIgnoreCase("")) {
        message += " - Password is missing. Please specify password.<br/>";
      } else {
        String confirmpwd = userDetails.get("confirmpassword")[0];
        if (confirmpwd.equalsIgnoreCase("")) {
          message += " - Confirm Password field is missing. Please confirm your password.<br/>";
        } else {
          // Check if password and confirm-password are the same; if so, set the password
          if (pwd.equals(confirmpwd)) {
            u.setEncodedPassword(pwd);
          } else {
            message +=
                " - Password and Confirm Password did not match. Please ensure that they match.<br/>";
          }
        }
      }
    }

    if (userDetails.containsKey("role")) {
      String role = userDetails.get("role")[0];
      if (role.equalsIgnoreCase("")) {
        message += " - Role is missing. Please specify role.<br/>";
      } else {
        u.setRole(role);
      }
    }

    if (userDetails.containsKey("customid")) {
      String customId = userDetails.get("customid")[0];
      u.setCustomId(customId);
    }

    if (userDetails.containsKey("firstname")) {
      String name = userDetails.get("firstname")[0];
      if (name.equalsIgnoreCase("")) {
        message += " - First name is missing. Please specify first name.<br/>";
      } else {
        u.setFirstName(name);
      }
    }
    if (userDetails.containsKey("lastname")) {
      String name = userDetails.get("lastname")[0];
      u.setLastName(name);
    }
    if (userDetails.containsKey("mobilephone")) {
      String name = userDetails.get("mobilephone")[0];
      if (name.equalsIgnoreCase("")) {
        message += " - Mobile phone number is missing. Please specify mobile phone number.<br/>";
      } else {
        u.setMobilePhoneNumber(name);
      }
    }

    // Capturing mobile phone brand, model and operator details
    if (userDetails.containsKey("phonebrand")) {
      u.setPhoneBrand(userDetails.get("phonebrand")[0]);
    }
    if (userDetails.containsKey("phonemodelnumber")) {
      u.setPhoneModelNumber(userDetails.get("phonemodelnumber")[0]);
    }
    if (userDetails.containsKey("phoneserviceprovider")) {
      u.setPhoneServiceProvider(userDetails.get("phoneserviceprovider")[0]);
    }

    if (userDetails.containsKey("landlinenumber")) {
      u.setLandPhoneNumber(userDetails.get("landlinenumber")[0]);
    }

    if (userDetails.containsKey("email")) {
      u.setEmail(userDetails.get("email")[0]);
    }

    if (userDetails.containsKey("gender")) {
      String gender = userDetails.get("gender")[0];
      u.setGender(gender);
    }

    if (userDetails.containsKey("language")) {
      String lang = userDetails.get("language")[0];
      if (lang.equalsIgnoreCase("")) {
        message += " - Language is missing. Please specify language.<br/>";
      } else {
        u.setLanguage(lang);
      }
    }
    if (userDetails.containsKey("timezone")) {
      String timezone = userDetails.get("timezone")[0];
      if (timezone.equalsIgnoreCase("")) {
        message += " - Timezone is missing. Please specify timezone.<br/>";
      } else {
        u.setTimezone(timezone);
      }
    }
    if (userDetails.containsKey("country")) {
      String country = userDetails.get("country")[0];
      if (country.equalsIgnoreCase("")) {
        message += " - Country is missing. Please specify country.<br/>";
      } else {
        u.setCountry(country);
      }
    }

    if (userDetails.containsKey("state")) {
      u.setState(userDetails.get("state")[0]);
    }

    if (userDetails.containsKey("district")) {
      u.setDistrict(userDetails.get("district")[0]);
    }

    if (userDetails.containsKey("taluk")) {
      u.setTaluk(userDetails.get("taluk")[0]);
    }

    if (userDetails.containsKey("city")) {
      u.setCity(userDetails.get("city")[0]);
    }

    if (userDetails.containsKey("pincode")) {
      u.setPinCode(userDetails.get("pincode")[0]);
    }

    if (userDetails.containsKey("street")) {
      u.setStreet(userDetails.get("street")[0]);
    }

    // Deprecating birthdate for now
    if (userDetails.containsKey("age")) {
      String ageStr = userDetails.get("age")[0];
      if (ageStr != null && !ageStr.equalsIgnoreCase("")) {
        int age = Integer.parseInt(ageStr);
        u.setAge(age);
        u.setAgeType(IUserAccount.AGETYPE_YEARS);
      }
    }

    // Get the user who registered this user
    SecureUserDetails sUser = SecurityMgr.getUserDetails(req.getSession());
    String registeredBy = sUser.getUsername();
    u.setRegisteredBy(registeredBy);
    // Get the domain ID
    Long domainId = SessionMgr.getCurrentDomain(req.getSession(), sUser.getUsername());

    // Add account
    if (message.isEmpty()) {
      try {
        as.addAccount(domainId, u);
        message =
            "<b>" + u.getFullName() + "</b> " + backendMessages.getString("created.success")
                + " &nbsp;[<a href=\"/s/setup/setup.jsp?subview=users&form=userdetails&id=" + u
                .getUserId()
                + "\">" + backendMessages.getString("user.view") + "</a>]"
                + "&nbsp;&nbsp;&nbsp;[<a href=\"/s/setup/setup.jsp?subview=users&form=addkioskowner\">"
                + messages.getString("add") + " " + messages.getString("new") + " " + messages
                .getString("user") + "</a>]";
      } catch (ServiceException e) {
        xLogger.severe("Exception when adding account: {0}", e.getMessage());
        message =
            backendMessages.getString("error") + ": " + e.getMessage()
                + " [<a href=\"/s/setup/setup.jsp?subview=users\">" + backendMessages
                .getString("users.view") + "</a>]";
      }
    } else {
      // NOTE: This is unlikely to be used, given front-end JS validation
      message = "The following error(s) were encountered:<br/>" + message +
          "<br/><br/>" +
          "Please go back by clicking the browser's Back button, fix the above error(s) and retry.";
    }
    writeToSetupScreen(req, resp, message, Constants.VIEW_USERS);
  }

  @SuppressWarnings("unchecked")
  private void modifyPoolgroup(HttpServletRequest req,
                               HttpServletResponse resp, EntitiesService as,
                               ResourceBundle backendMessages, ResourceBundle messages)
      throws ServiceException, IOException {
    Map<String, String[]> pgDetails = req.getParameterMap();
    pgDetails = cleanMap(pgDetails);
    String pgIDStr = "";
    IPoolGroup pg = null;
    String message = "";

    if (pgDetails.containsKey("id")) {
      pgIDStr = pgDetails.get("id")[0];
      Long l = Long.parseLong(pgIDStr.trim());
      pg = as.getPoolGroup(l);
    }

    if (pgDetails.containsKey("name")) {
      String name = pgDetails.get("name")[0];
      if (name.equalsIgnoreCase("")) {
        message += " - Pool group name is missing. Please specify pool group name.<br/>";
      } else {
        pg.setName(name);
      }
    }

    if (pgDetails.containsKey("description")) {
      pg.setDescription(pgDetails.get("description")[0]);
    }

    if (pgDetails.containsKey("userid")) {
      String name = pgDetails.get("userid")[0];
      if (name.equalsIgnoreCase("")) {
        message +=
            " - Pool group owner is missing. Please specify at least one pool group owner.<br/>";
      } else {
        pg.setOwnerId(name);
      }
    }

    if (pgDetails.containsKey("kiosks")) {
      String kiosks = pgDetails.get("kiosks")[0];
      String[] kiosksArr = new String[0];
      if (kiosks != null && !kiosks.isEmpty()) {
        kiosksArr = kiosks.split(",");
      }
      List<IKiosk> Kiosks = new ArrayList<IKiosk>();
      for (String k : kiosksArr) {
        long l = Long.parseLong(k.trim());
        IKiosk eachKiosk = as.getKiosk(l);
        Kiosks.add(eachKiosk);
      }
      pg.setKiosks(Kiosks);
    }

    if (message.isEmpty()) {
      as.updatePoolGroup(pg);
      message =
          "<b>" + pg.getName() + "</b> " + backendMessages.getString("updated.success")
              + " &nbsp;[<a href=\"/s/setup/setup.jsp?subview=poolgroups&form=poolgroupdetails&id="
              + pgIDStr + "\">" + backendMessages.getString("poolgroup.view") + "</a>]";
    } else {
      // NOTE: This is unlikely to be used, given front-end JS validation
      message = "The following error(s) were encountered:<br/>" + message +
          "<br/><br/>" +
          "Please go back by clicking the browser's Back button, fix the error(s) and retry.";
    }

    writeToSetupScreen(req, resp, message, Constants.VIEW_POOLGROUPS);
  }

  @SuppressWarnings("unchecked")
  private void modifyKiosk(HttpServletRequest req, HttpServletResponse resp,
                           EntitiesService as, UsersService usersService, ResourceBundle backendMessages,
                           ResourceBundle messages) throws ServiceException, IOException {
    Map<String, String[]> kioskDetails = req.getParameterMap();
    kioskDetails = cleanMap(kioskDetails);
    SecureUserDetails sUser = SecurityMgr.getUserDetails(req.getSession());
    Long domainId = SessionMgr.getCurrentDomain(req.getSession(), sUser.getUsername());
    String message = "";

    String kioskIDStr = "";
    IKiosk k = null;
    if (kioskDetails.containsKey("id")) {
      kioskIDStr = kioskDetails.get("id")[0];
      Long l = Long.parseLong(kioskIDStr.trim());
      k = as.getKiosk(l);
    }

    if (kioskDetails.containsKey("name")) {
      String name = kioskDetails.get("name")[0];
      if (name.equalsIgnoreCase("")) {
        message += " - Kiosk name is missing. Please specify kiosk name.<br/>";
      } else {
        k.setName(name);
      }
    }

    if (kioskDetails.containsKey("tags")) {
      String tagsCSV = kioskDetails.get("tags")[0];
      k.setTags(StringUtil.getList(tagsCSV, true));
    }

    if (kioskDetails.containsKey("customid")) {
      String customId = kioskDetails.get("customid")[0];
      k.setCustomId(customId);
    }

    if (kioskDetails.containsKey("longitude")) {
      String longitudeStr = kioskDetails.get("longitude")[0];
      if (longitudeStr != null) {
        if (longitudeStr.isEmpty()) {
          longitudeStr = "0";
        }
        k.setLongitude(Double.valueOf(longitudeStr).doubleValue());
      }
    }
    if (kioskDetails.containsKey("latitude")) {
      String latitudeStr = kioskDetails.get("latitude")[0];
      if (latitudeStr != null) {
        if (latitudeStr.isEmpty()) {
          latitudeStr = "0";
        }
      }
      k.setLatitude(Double.valueOf(latitudeStr).doubleValue());
    }

    if (kioskDetails.containsKey("street")) {
      k.setStreet(kioskDetails.get("street")[0]);
    }
    if (kioskDetails.containsKey("city")) {
      String name = kioskDetails.get("city")[0];
      if (name.equalsIgnoreCase("")) {
        message += " - Village/city name is missing. Please specify village/city.<br/>";
      } else {
        k.setCity(name);
      }
    }
    if (kioskDetails.containsKey("taluk")) {
      String name = kioskDetails.get("taluk")[0];
      k.setTaluk(name);
    }
    if (kioskDetails.containsKey("district")) {
      String name = kioskDetails.get("district")[0];
      k.setDistrict(name);
    }
    if (kioskDetails.containsKey("state")) {
      String name = kioskDetails.get("state")[0];
      if (name.equalsIgnoreCase("")) {
        message += " - State name is missing. Please specify state name.<br/>";
      } else {
        k.setState(name);
      }
    }
    if (kioskDetails.containsKey("country")) {
      k.setCountry(kioskDetails.get("country")[0]);
    }
    if (kioskDetails.containsKey("pincode")) {
      k.setPinCode(kioskDetails.get("pincode")[0]);
    }
    if (kioskDetails.containsKey("vertical")) {
      k.setVertical(kioskDetails.get("vertical")[0]);
    }
    if (kioskDetails.containsKey("servicelevel")) {
      String name = kioskDetails.get("servicelevel")[0];
      if (name != null && !name.isEmpty()) {
        k.setServiceLevel(Integer.parseInt(name));
      }
    }
    if (kioskDetails.containsKey("inventorymodel")) {
      String model = kioskDetails.get("inventorymodel")[0];
      k.setInventoryModel(model);
    }
    if (kioskDetails.containsKey("currency")) {
      String currency = kioskDetails.get("currency")[0];
      if (currency != null) {
        k.setCurrency(currency);
      }
    }
    if (kioskDetails.containsKey("tax")) {
      String tax = kioskDetails.get("tax")[0];
      if (tax != null) {
        if (tax.isEmpty()) {
          k.setTax(BigDecimal.ZERO);
        } else {
          k.setTax(new BigDecimal(tax));
        }
      }
    }
    if (kioskDetails.containsKey("taxid")) {
      String taxid = kioskDetails.get("taxid")[0];
      if (taxid != null) {
        k.setTaxId(taxid);
      }
    }
    if (kioskDetails.containsKey("userids")) {
      String userIdCSV = kioskDetails.get("userids")[0];
      List<String> userIdList = StringUtil.getList(userIdCSV, true); // get unique user IDs
      List<IUserAccount> assocUserAccounts = new ArrayList<IUserAccount>();
      Iterator<String> userIds = userIdList.iterator();
      while (userIds.hasNext()) {
        String uId = userIds.next();
        try {
          IUserAccount us = usersService.getUserAccount(uId);
          if (!us.isEnabled()) {
            message +=
                " - Could not modify kiosk since user " + us.getFullName() + " is disabled.<br/>";
          }
          assocUserAccounts.add(us);
        } catch (ObjectNotFoundException e) {
          throw new ServiceException(e.getMessage());
        }
      }
      k.setUsers(assocUserAccounts);
    }

    if (message.isEmpty()) {
      as.updateKiosk(k, domainId);
      message =
          "<b>" + k.getName() + "</b> " + backendMessages.getString("updated.success")
              + " &nbsp;[<a href=\"/s/setup/setup.jsp?subview=kiosks&form=kioskdetails&id="
              + kioskIDStr + "\">" + backendMessages.getString("kiosk.view") + "</a>]";
    } else {
      // NOTE: This is unlikely to be used, given front-end JS validation
      message = "The following error(s) were encountered:<br/>" + message +
          "<br/><br/>" +
          "Please go back by clicking the browser's Back button, fix the above error(s) and retry.";
    }

    writeToSetupScreen(req, resp, message, Constants.VIEW_KIOSKS);
  }

  @SuppressWarnings("unchecked")
  private void createPoolgroup(HttpServletRequest req,
                               HttpServletResponse resp, EntitiesService as,
                               ResourceBundle backendMessages, ResourceBundle messages)
      throws ServiceException, IOException {
    Map<String, String[]> pgDetails = req.getParameterMap();
    pgDetails = cleanMap(pgDetails);

    String message = "";
    IPoolGroup pg = JDOUtils.createInstance(IPoolGroup.class);

    if (pgDetails.containsKey("name")) {
      String name = pgDetails.get("name")[0];
      if (name.equalsIgnoreCase("")) {
        message += " - Pool group name is missing. Please specify pool group name.<br/>";
      } else {
        pg.setName(name);
      }
    }

    if (pgDetails.containsKey("description")) {
      pg.setDescription(pgDetails.get("description")[0]);
    }

    if (pgDetails.containsKey("userid")) {
      String name = pgDetails.get("userid")[0];
      if (name.equalsIgnoreCase("")) {
        message +=
            " - Pool group owner is missing. Please specify an owner for the pool group.<br/>";
      } else {
        pg.setOwnerId(name);
      }
    }
    if (pgDetails.containsKey("kiosks")) {
      String kioskids = pgDetails.get("kiosks")[0];
      String[] kiosksArr = new String[0];
      if (kioskids != null && !kioskids.isEmpty()) {
        kiosksArr = kioskids.split(",");
      }
      List<IKiosk> Kiosks = new ArrayList<IKiosk>();
      for (String k : kiosksArr) {
        long l = Long.parseLong(k.trim());
        IKiosk eachKiosk = as.getKiosk(l, false);
        Kiosks.add(eachKiosk);
      }
      pg.setKiosks(Kiosks);
    }

    // Get the domain Id
    SecureUserDetails sUser = SecurityMgr.getUserDetails(req.getSession());
    Long domainId = SessionMgr.getCurrentDomain(req.getSession(), sUser.getUsername());
    if (message.isEmpty()) {
      Long pgID = as.addPoolGroup(domainId, pg);
      message =
          "<b>" + pg.getName() + "</b> " + backendMessages.getString("created.success")
              + " &nbsp;[<a href=\"/s/setup/setup.jsp?subview=poolgroups&form=poolgroupdetails&id="
              + pgID.toString() + "\">" + backendMessages.getString("poolgroup.view") + "</a>]";
    } else {
      // NOTE: This is unlikely to be used, given front-end JS validation
      message = "The following error(s) were encountered:<br/>" + message +
          "<br/><br/>" +
          "Please go back by clicking the browser's Back button, fix the above error(s) and retry.";

    }

    writeToSetupScreen(req, resp, message, Constants.VIEW_POOLGROUPS);
  }

  @SuppressWarnings("unchecked")
  private void createKiosk(HttpServletRequest req, HttpServletResponse resp,
                           EntitiesService as, UsersService usersService, ResourceBundle backendMessages,
                           ResourceBundle messages) throws ServiceException, IOException {
    Map<String, String[]> kioskDetails = req.getParameterMap();
    kioskDetails = cleanMap(kioskDetails);

    String message = "";
    IKiosk k = JDOUtils.createInstance(IKiosk.class);

    if (kioskDetails.containsKey("name")) {
      String name = kioskDetails.get("name")[0];
      if (name.equalsIgnoreCase("")) {
        message += " - Kiosk name is missing. Please specify kiosk name.<br/>";
      } else {
        k.setName(name);
      }
    }
    if (kioskDetails.containsKey("tags")) {
      String tagsCSV = kioskDetails.get("tags")[0];
      k.setTags(StringUtil.getList(tagsCSV, true));
    }
    if (kioskDetails.containsKey("customid")) {
      String customId = kioskDetails.get("customid")[0];
      k.setCustomId(customId);
    }
    if (kioskDetails.containsKey("longitude")) {
      String longitudeStr = kioskDetails.get("longitude")[0];
      if (longitudeStr != null && !longitudeStr.isEmpty()) {
        k.setLongitude(Double.valueOf(longitudeStr).doubleValue());
      }
    }
    if (kioskDetails.containsKey("latitude")) {
      String latitudeStr = kioskDetails.get("latitude")[0];
      if (latitudeStr != null && !latitudeStr.isEmpty()) {
        k.setLatitude(Double.valueOf(latitudeStr).doubleValue());
      }
    }
    if (kioskDetails.containsKey("street")) {
      k.setStreet(kioskDetails.get("street")[0]);
    }
    if (kioskDetails.containsKey("city")) {
      String name = kioskDetails.get("city")[0];
      if (name.equalsIgnoreCase("")) {
        message += " - Village/city name is missing. Please specify village/city name.<br/>";
      } else {
        k.setCity(name);
      }
    }
    if (kioskDetails.containsKey("taluk")) {
      String name = kioskDetails.get("taluk")[0];
      k.setTaluk(name);
    }
    if (kioskDetails.containsKey("district")) {
      String name = kioskDetails.get("district")[0];
      k.setDistrict(name);
    }
    if (kioskDetails.containsKey("state")) {
      String name = kioskDetails.get("state")[0];
      if (name.equalsIgnoreCase("")) {
        message += " - State name is missing. Please specify state name.<br/>";
      } else {
        k.setState(name);
      }
    }
    if (kioskDetails.containsKey("country")) {
      k.setCountry(kioskDetails.get("country")[0]);
    }
    if (kioskDetails.containsKey("pincode")) {
      k.setPinCode(kioskDetails.get("pincode")[0]);
    }
    if (kioskDetails.containsKey("vertical")) {
      k.setVertical(kioskDetails.get("vertical")[0]);
    }
    if (kioskDetails.containsKey("inventorymodel")) {
      String model = kioskDetails.get("inventorymodel")[0];
      k.setInventoryModel(model);
    }
    if (kioskDetails.containsKey("servicelevel")) {
      String name = kioskDetails.get("servicelevel")[0];
      if (name != null && !name.isEmpty()) {
        k.setServiceLevel(Integer.parseInt(name));
      }
    }
    if (kioskDetails.containsKey("currency")) {
      String currency = kioskDetails.get("currency")[0];
      if (currency != null) {
        k.setCurrency(currency);
      }
    }
    if (kioskDetails.containsKey("tax")) {
      String tax = kioskDetails.get("tax")[0];
      if (tax != null) {
        if (tax.isEmpty()) {
          k.setTax(BigDecimal.ZERO);
        } else {
          k.setTax(new BigDecimal(tax));
        }
      }
    }
    if (kioskDetails.containsKey("taxid")) {
      String taxid = kioskDetails.get("taxid")[0];
      if (taxid != null && !taxid.isEmpty()) {
        k.setTaxId(taxid);
      }
    }
    if (kioskDetails.containsKey("userids")) {
      String userIdCSV = kioskDetails.get("userids")[0];
      List<String> userIdList = StringUtil.getList(userIdCSV, true); // get unique user ID list
      Iterator<String> userIds = userIdList.iterator();
      List<IUserAccount> assocUserAccounts = new ArrayList<IUserAccount>();
      while (userIds.hasNext()) {
        String uId = userIds.next();
        try {
          IUserAccount us = usersService.getUserAccount(uId);
          // If user is disabled, do not add
          if (!us.isEnabled()) {
            message +=
                " - Could not create kiosk since user " + us.getFullName() + " is disabled.<br/>";
          } else {
            assocUserAccounts.add(us);
          }
        } catch (ObjectNotFoundException e) {
          throw new ServiceException(e.getMessage());
        }
      }
      k.setUsers(assocUserAccounts);
    }

    // Get the domain Id from the session
    SecureUserDetails sUser = SecurityMgr.getUserDetails(req.getSession());
    Long domainId = SessionMgr.getCurrentDomain(req.getSession(), sUser.getUsername());
    // Add kiosk
    if (message.isEmpty()) {
      // Add registered by info
      k.setRegisteredBy(sUser.getUsername());
      Long kioskID = as.addKiosk(domainId, k);
      message =
          "<b>" + k.getName() + "</b> " + backendMessages.getString("created.success")
              + " &nbsp;[<a href=\"/s/setup/setup.jsp?subview=kiosks&form=kioskdetails&id="
              + kioskID.toString() + "\">" + backendMessages.getString("kiosk.view") + "</a>]"
              + "&nbsp;&nbsp;&nbsp;[<a href=\"/s/setup/setup.jsp?subview=kiosks&form=addkiosk\">"
              + messages.getString("add") + " " + messages.getString("new") + " " + messages
              .getString("kiosk") + "</a>]";
    } else {
      // NOTE: This is unlikely to be used, given front-end JS validation
      message = "The following error(s) were encountered:<br/>" + message +
          "<br/><br/>" +
          "Please go back by clicking the browser's Back button, fix the above error(s) and retry.";
    }
    writeToSetupScreen(req, resp, message, Constants.VIEW_KIOSKS);
  }

  // Add multiple materials to multiple kiosks
  @SuppressWarnings("unchecked")
  private void addOrRemoveMaterialsForMultipleKiosks(HttpServletRequest req,
                                                     HttpServletResponse resp,
                                                     EntitiesService as,
                                                     ResourceBundle backendMessages,
                                                     ResourceBundle messages)
      throws ServiceException, IOException {
    xLogger.fine("Enter addOrRemoveMaterialsForMultipleKiosks");
    // Get the domain Id
    SecureUserDetails sUser = SecurityMgr.getUserDetails(req.getSession());
    Long domainId = SessionMgr.getCurrentDomain(req.getSession(), sUser.getUsername());
    // Get the action
    String action = req.getParameter("action"); // add or remove
    boolean add = "add".equals(action); // else remove
    // Get all the material IDs, if any
    String[] mids = req.getParameterValues("materialid");
    // Get the kiosk IDs, if any
    String kidsStr = req.getParameter("kioskids");
    String[] kids = null;
    if (kidsStr != null && !kidsStr.isEmpty()) {
      kids = kidsStr.split(",");
    }
    String message = null;
    boolean hasMaterials = mids != null && mids.length > 0;
    boolean hasKiosks = kids != null && kids.length > 0;
    if (!hasMaterials) {
      message = "No materials specified.";
    } else {
      Map<String, String> params = new HashMap<String, String>();
      // Add action param
      params.put("action", action);
      params.put("type", "materialtokiosk");
      // Add the user's locale
      Locale locale = sUser.getLocale();
      if (locale != null) {
        params.put("country", locale.getCountry());
        params.put("language", locale.getLanguage());
      }
      //if ( add )
      params.put("domainid", domainId.toString()); // needed for both add and remove
      // Form and add the materials parameters
      String midsStr = "";
      for (int i = 0; i < mids.length; i++) {
        String midStr = mids[i];
        if (add) {
          String param = "reorderlevel" + midStr;
          params.put(param, req.getParameter(param));
          param = "max" + midStr;
          params.put(param, req.getParameter(param));
          param = "cr" + midStr;
          params.put(param, req.getParameter(param));
          param = "price" + midStr;
          params.put(param, req.getParameter(param));
          param = "tax" + midStr;
          params.put(param, req.getParameter(param));
          param = "servicelevel" + midStr;
          params.put(param, req.getParameter(param));
          param = "invmodel" + midStr;
          params.put(param, req.getParameter(param));
          param = "materialname" + midStr;
          params.put(param, req.getParameter(param));
          param = "datatype" + midStr;
          String dtValue = req.getParameter(param);
          if (dtValue != null) {
            params.put(param, dtValue);
          }
        }
        // Get the mid CSV
        if (!midsStr.isEmpty()) {
          midsStr += ",";
        }
        midsStr += midStr;
      }
      params.put("materialid", midsStr);
      // Indicate multi-valued param.
      List<String> multiValuedParams = new ArrayList<String>();
      multiValuedParams.add("materialid");
      // Get all the kiosk IDs
      List<IKiosk> kiosks = null;
      if (!hasKiosks) {
        // Get all the kiosks
        Results results = as.getAllKiosks(domainId, null, null); // TODO: pagination?
        kiosks = results.getResults();
      } else {
        kiosks = new ArrayList<IKiosk>();
        for (int i = 0; i < kids.length; i++) {
          try {
            kiosks.add(as.getKiosk(Long.valueOf(kids[i]), false));
          } catch (Exception e) {
            xLogger.warn("Unable to get kiosk with ID {0} : {1}", kids[i], e.getMessage());
          }
        }
      }
      // Schedule a task for each kiosk with the appropriate action (add/remove)
      Iterator<IKiosk> it = kiosks.iterator();
      int numTasks = 0;
      while (it.hasNext()) {
        IKiosk k = it.next();
        params.put("kioskid", k.getKioskId().toString());
        if (add) {
          params.put("kioskname", k.getName());
        }
        try {
          xLogger.fine("Scheduling " + action + " task for kiosk {0}, params = {1}", k.getKioskId(),
              params.toString());
          taskService
              .schedule(taskService.QUEUE_DEFAULT, CREATEENTITY_TASK_URL, params, multiValuedParams,
                  null, taskService.METHOD_POST, -1, domainId, sUser.getUsername(),
                  "ADDREMOVE_KIOSKS");
          numTasks++;
        } catch (Exception e) {
          xLogger.warn("Error scheduling materials operation {0} task for kiosk {0}", action,
              k.getKioskId());
        }
      }
      message =
          messages.getString("scheduledtasks") + " " + messages.getString(action) + " <b>"
              + mids.length + "</b>" + " " + messages.getString("materials") + " " + messages
              .getString("for") + " " + "<b>" + numTasks + "</b>" + " " + messages
              .getString("kiosks") + " [<a href=\"javascript:window.close()\">" + messages
              .getString("close") + "</a>]";
      if (!add) {
        message +=
            "<br/><br/>" + backendMessages.getString("note") + ": " + backendMessages
                .getString("deleted.delaynote") + ".";
      }
    }
    // Write response
    req.setAttribute("nomenu", "true");
    writeToScreen(req, resp, message, Constants.VIEW_KIOSKMATERIALS);

    xLogger.fine("Exiting addOrRemoveMaterialsForMultipleKiosks");
  }

  @SuppressWarnings("unchecked")
  private void removeKiosk(HttpServletRequest req, HttpServletResponse resp,
                           EntitiesService as, ResourceBundle backendMessages,
                           ResourceBundle messages) throws ServiceException, IOException {
    Map<String, String[]> kioskDetails = req.getParameterMap();
    kioskDetails = cleanMap(kioskDetails);
    SecureUserDetails sUser = SecurityMgr.getUserDetailsIfPresent();

    // Get the domain Id
    String domainIdStr = req.getParameter("domainid");
    String sUserName = req.getParameter("sourceuser");
    Long domainId = null;
    if (domainIdStr != null && !domainIdStr.isEmpty()) {
      domainId = Long.valueOf(domainIdStr);
    }
    boolean
        execute =
        req.getParameter("execute") != null; // whether the deletion should be executed or scheduled
    if (kioskDetails.containsKey("kioskid")) {
      String[] kioskIDs = req.getParameterValues("kioskid");
      if (kioskIDs != null) {
        if (!execute) { // schedule
          // Schedule a separate task for deletion of each kiosk
          Map<String, String> params = new HashMap<String, String>();
          params.put("action", "remove");
          params.put("type", "kiosk");
          params.put("domainid", domainIdStr);
          params.put("execute", "true"); // now add the "execute" indicator here
          for (int i = 0; i < kioskIDs.length; i++) {
            params.put("kioskid", kioskIDs[i]);
            try {
              taskService.schedule(taskService.QUEUE_DEFAULT, CREATEENTITY_TASK_URL, params, null,
                  taskService.METHOD_POST, domainId, sUser.getUsername(), "REMOVE_KIOSK");
            } catch (Exception e) {
              xLogger.warn("{0} when scheduling task to delete kiosk {1} in domain {2}: {3}",
                  e.getClass().getName(), kioskIDs[i], domainId, e.getMessage());
            }
          }
          String
              message =
              messages.getString("scheduledtasks") + " " + messages.getString("remove") + " <b>"
                  + kioskIDs.length + "</b>" + " " + messages.getString("kiosks")
                  + " &nbsp;[<a href=\"/s/setup/setup.jsp?subview=kiosks\">" + backendMessages
                  .getString("kiosks.view") + "</a>]";
          message +=
              "<br/><br/>NOTE: It may take some time for an entity and all its associated objects to be removed.";
          writeToSetupScreen(req, resp, message, Constants.VIEW_KIOSKS);
          return;
        }
        // Execute the deletion for the given kiosk(s)
        ArrayList<Long> kiosks = new ArrayList<Long>();
        for (String kioskID : kioskIDs) {
          long l = Long.parseLong(kioskID.trim());
          kiosks.add(l);
        }
        try {
          as.deleteKiosks(domainId, kiosks, sUserName);
        } catch (Exception e) {
          xLogger.severe("{0} when deleting kiosks {1} in domain {2}: {3}", e.getClass().getName(),
              kiosks, domainId, e.getMessage(), e);
        }
      }
    }
  }

  @SuppressWarnings("unchecked")
  private void removePoolgroups(HttpServletRequest req,
                                HttpServletResponse resp, EntitiesService as,
                                ResourceBundle backendMessages, ResourceBundle messages)
      throws ServiceException, IOException {
    Map<String, String[]> pgDetails = req.getParameterMap();
    pgDetails = cleanMap(pgDetails);
    String domainIdStr = req.getParameter("domainid");
    Long domainId = null;
    if (domainIdStr != null && !domainIdStr.isEmpty()) {
      domainId = Long.valueOf(domainIdStr);
    }
    if (pgDetails.containsKey("poolgroupids")) {
      String[] pgIDs = pgDetails.get("poolgroupids")[0].split(",");
      ArrayList<Long> poolgroupIDs = new ArrayList<Long>();
      for (String pgID : pgIDs) {
        long l = Long.parseLong(pgID.trim());
        poolgroupIDs.add(l);
      }
      as.deletePoolGroups(domainId, poolgroupIDs);
      writeToSetupScreen(req, resp,
          "<b>" + String.valueOf(poolgroupIDs.size()) + "</b> " + backendMessages
              .getString("deleted.success") +
              " &nbsp;[<a href=\"/s/setup/setup.jsp?subview=poolgroups\">" + backendMessages
              .getString("poolgroups.view") + "</a>]", Constants.VIEW_POOLGROUPS);
    } else {
      // NOTE: This is unlikely to be used, given front-end JS validation
      resp.setStatus(500);
      writeToSetupScreen(req, resp, "No poolgroups to remove!", Constants.VIEW_POOLGROUPS);
    }
  }

  @SuppressWarnings("unchecked")
  private void addDomain(HttpServletRequest req, HttpServletResponse resp)
      throws ServiceException, IOException {
    Map<String, String[]> domainDetails = req.getParameterMap();
    domainDetails = cleanMap(domainDetails);

    // Get the id of user creating this domain (typically admin id)
    String userId = req.getParameter("userid");

    String message = "";
    IDomain domain = JDOUtils.createInstance(IDomain.class);
    String name = null;
    if (domainDetails.containsKey("name")) {
      name = domainDetails.get("name")[0];
      if (name.equalsIgnoreCase("")) {
        message += " - Domain name is missing. Please specify a domain name.<br/>";
      } else {
        domain.setName(name);
      }
    }
    if (domainDetails.containsKey("description")) {
      String desc = domainDetails.get("description")[0];
      if (!desc.equalsIgnoreCase("")) {
        domain.setDescription(desc);
      }
    }
    if (domainDetails.containsKey("ownerid")) {
      String ownerId = domainDetails.get("ownerid")[0];
      if (ownerId.equalsIgnoreCase("")) {
        message +=
            " - Domain owner is not specified. Please specify an owner for this domain.<br/>";
      } else {
        domain.setOwnerId(ownerId);
      }
    }
    if (domainDetails.containsKey("isactive")) {
      String isActive = domainDetails.get("isactive")[0];
      if (isActive.equalsIgnoreCase("false")) {
        domain.setIsActive(false);
      } else {
        domain.setIsActive(true);
      }
    }
    // Add new domain
    if (message.isEmpty()) {
      DomainsService ds = Services.getService(DomainsServiceImpl.class);
      Long domainId = ds.addDomain(domain);
      setUiPreferenceForDomain(req, resp, domainId, userId, true, true);
      message =
          "Created domain <b>" + name
              + "</b> successfully [<a href=\"/s/admin/domains.jsp?subview=domains&userid=" + userId
              + "\">view domains</a>]";
    } else {
      message = "The following error(s) were encountered:<br/>" + message +
          "<br/><br/>" +
          "Please go back by clicking the browser's Back button, fix the above error(s) and retry.";
    }
    writeToScreenWithMode(req, resp, message, Constants.MODE_MANAGE, Constants.VIEW_DOMAINS);
  }

  @SuppressWarnings("unchecked")
  private void removeDomains(HttpServletRequest req, HttpServletResponse resp)
      throws ServiceException, IOException {
    Map<String, String[]> domainDetails = req.getParameterMap();
    domainDetails = cleanMap(domainDetails);

    // Get the user ID of the person who initiated the removal
    String userId = req.getParameter("userid");
    boolean execute = req.getParameter("execute") != null;
    if (domainDetails.containsKey("domainids")) {
      String[] domainIDsArr = domainDetails.get("domainids")[0].split(",");
      if (!execute) { // schedule
        // Schedule a separate task for deletion of each material (given its associated entities also have to be removed)
        Map<String, String> params = new HashMap<String, String>();
        params.put("action", "remove");
        params.put("type", "domain");
        params.put("execute", "true"); // now add the "execute" indicator here
        for (int i = 0; i < domainIDsArr.length; i++) {
          params.put("domainids", domainIDsArr[i]);
          try {
            taskService.schedule(taskService.QUEUE_DEFAULT, CREATEENTITY_TASK_URL, params,
                taskService.METHOD_POST);
          } catch (Exception e) {
            xLogger
                .warn("{0} when scheduling task to delete domain {1}: {2}", e.getClass().getName(),
                    domainIDsArr[i], e.getMessage());
          }
        }
        // Get the return message
        writeToScreenWithMode(req, resp, "Scheduled removal of <b>" + domainIDsArr.length
            + "</b> domain(s) successfully! [<a href=\"/s/admin/domains.jsp?subview=domains&userid="
            + userId + "\">view domains</a>]", Constants.MODE_MANAGE, Constants.VIEW_DOMAINS);
        return;
      }
      // Execute removal of domains
      ArrayList<Long> domainIDs = new ArrayList<Long>();
      for (String domainID : domainIDsArr) {
        Long l = Long.valueOf(domainID.trim());
        domainIDs.add(l);
      }
      try {
        DomainsService ds = Services.getService(DomainsServiceImpl.class);
        ds.deleteDomains(domainIDs);
      } catch (Exception e) {
        xLogger.severe("{0} when deleting domains {1}: {2}", e.getClass().getName(), domainIDs,
            e.getMessage());
      }
    } else {
      resp.setStatus(500);
      writeToScreenWithMode(req, resp,
          "No domains to remove! [<a href=\"/s/admin/domains.jsp?subview=domains&userid=" + userId
              + "\">view domains</a>]", Constants.MODE_MANAGE, Constants.VIEW_DOMAINS);
    }
  }

  @SuppressWarnings("unchecked")
  private void switchDomain(HttpServletRequest req, HttpServletResponse resp, UsersService as)
      throws ServiceException, ObjectNotFoundException, IOException {
    xLogger.fine("Entered switchDomain");

    // Get the session
    HttpSession session = req.getSession();
    Map<String, String[]> domainDetails = req.getParameterMap();

    // Get the user Id and new domain ID to switch to
    String userId = null;
    Long domainId = null;
    if (domainDetails.containsKey("userid")) {
      userId = domainDetails.get("userid")[0];
      if (userId.equalsIgnoreCase("")) {
        writeToScreenWithMode(req, resp,
            "Sorry, could not switch domain since user ID is not specified.", Constants.MODE_MANAGE,
            Constants.VIEW_DOMAINS);
        return;
      }
    }
    if (domainDetails.containsKey("domainid")) {
      String domainIdStr = domainDetails.get("domainid")[0];
      if (domainIdStr.equalsIgnoreCase("")) {
        writeToScreenWithMode(req, resp,
            "Sorry, could not switch domains since domain ID is not specified.",
            Constants.MODE_MANAGE, Constants.VIEW_DOMAINS);
        return;
      }
      domainId = Long.valueOf(domainIdStr);
    }
    // Set the new domain in the session
    SessionMgr.setCurrentDomain(session, domainId);
    // Redirect to the home page
    resp.sendRedirect("/s/index.jsp");
    xLogger.fine("Exiting switchDomain");
  }

  /***
   * Kiosk Link Management
   ***/
  @SuppressWarnings("unchecked")
  private void createOrModifyKioskLink(HttpServletRequest req, HttpServletResponse resp,
                                       EntitiesService as, ResourceBundle backendMessages,
                                       ResourceBundle messages)
      throws ServiceException, IOException, NumberFormatException, ObjectNotFoundException {
    xLogger.fine("Entered createOrModifyKioskLink");
    Map<String, String[]> linkDetails = req.getParameterMap();
    linkDetails = cleanMap(linkDetails);
    // Get action
    String action = req.getParameter("action");
    // Get kiosk ID link (only sent for modify)
    String linkId = req.getParameter("linkid");
    boolean modify = "modify".equals(action);
    // Get logged in user
    SecureUserDetails sUser = SecurityMgr.getUserDetails(req.getSession());
    String userId = sUser.getUsername();
    Long domainId = SessionMgr.getCurrentDomain(req.getSession(), userId);
    // Init. object
    IKioskLink kioskLink = null;
    if (modify) {
      kioskLink = as.getKioskLink(linkId);
    }
    String kioskIdStr = null;
    // Get parameters and update object
    Long kioskId = null;
    String desc = null;
    String linkType = null;
    BigDecimal creditLimit = BigDecimal.ZERO;
    boolean hasCreditLimit = false;
    if (linkDetails.containsKey("kioskid")) {
      kioskIdStr = linkDetails.get("kioskid")[0];
      if (kioskIdStr.equalsIgnoreCase("")) {
        writeToSetupScreen(req, resp,
            "Sorry, could not create relationship since kiosk ID is missing.",
            Constants.VIEW_KIOSKS);
        return;
      }
      kioskId = Long.valueOf(kioskIdStr);
    }
    if (linkDetails.containsKey("linkType")) {
      linkType = linkDetails.get("linkType")[0];
      if (linkType.equalsIgnoreCase("")) {
        writeToSetupScreen(req, resp,
            "Sorry, could not create relationship since 'type' is missing.", Constants.VIEW_KIOSKS);
        return;
      }
    }
    if (linkDetails.containsKey("description")) {
      desc = linkDetails.get("description")[0];
      if (desc != null && desc.isEmpty()) {
        desc = null;
      }
    }
    if (linkDetails.containsKey("creditlimit")) {
      hasCreditLimit = true;
      String creditLimitStr = linkDetails.get("creditlimit")[0];
      if (creditLimitStr != null && !creditLimitStr.isEmpty()) {
        creditLimit = new BigDecimal(creditLimitStr);
      }
    }
    xLogger.fine("creditlimit: {0}, hasCreditLimit: {1}", creditLimit, hasCreditLimit);
    List<IKioskLink> links = new ArrayList<IKioskLink>();
    if (linkDetails.containsKey("linkedKioskId")) {
      String[] linkedKioskIds = linkDetails.get("linkedKioskId");
      xLogger.fine("linkedKioskIds: {0}", linkedKioskIds.length);
      if (linkedKioskIds == null || linkedKioskIds.length == 0) {
        writeToSetupScreen(req, resp,
            "Sorry, could not create relationship since related kiosk ID is missing.",
            Constants.VIEW_KIOSKS);
        return;
      }
      Date now = new Date();
      for (int i = 0; i < linkedKioskIds.length; i++) {
        // Create kiosk link object
        kioskLink = JDOUtils.createInstance(IKioskLink.class);
        kioskLink.setDomainId(domainId);
        kioskLink.setCreatedBy(userId);
        kioskLink.setCreatedOn(now);
        kioskLink.setDescription(desc);
        kioskLink.setKioskId(kioskId);
        kioskLink.setLinkType(linkType);
        kioskLink.setLinkedKioskId(Long.valueOf(linkedKioskIds[i]));
        if (hasCreditLimit) {
          kioskLink.setCreditLimit(creditLimit);
        }
        // Add to list
        links.add(kioskLink);
      }
    }
    // Get the permissions
                /*
                Permissions p = kioskLink.getPermissions();
		if ( p == null )
			p = new Permissions();
		p.putAccess( Permissions.INVENTORY, Permissions.OP_VIEW, linkDetails.containsKey( "inventory.view" ) );
		p.putAccess( Permissions.INVENTORY, Permissions.OP_MANAGE, linkDetails.containsKey( "inventory.manage" ) );
		p.putAccess( Permissions.ORDERS, Permissions.OP_VIEW, linkDetails.containsKey( "orders.view" ) );
		p.putAccess( Permissions.ORDERS, Permissions.OP_MANAGE, linkDetails.containsKey( "orders.manage" ) );
		kioskLink.setPermissions( p );
		*/
    // TODO Check if this link already exists before adding (possibly via a method in AccountsService)
    // Add/modify kiosk relationship/link
    String msg = null;
    if (modify) {
      kioskLink.setDescription(desc);
      if (hasCreditLimit) {
        kioskLink.setCreditLimit(creditLimit);
      }
      as.updateKioskLink(kioskLink);
      req.setAttribute("nomenu", "true");
      msg =
          backendMessages.getString("relationship.updated")
              + "&nbsp;[<a href=\"javascript:window.close()\">" + messages.getString("close")
              + "</a>]"
              + "<br/><br/>" + backendMessages.getString("refreshlistmsg");
    } else {
      as.addKioskLinks(domainId, links);
      msg =
          backendMessages.getString("relationship.created")
              + " &nbsp;[<a href=\"/s/setup/setup.jsp?subview=kiosks&form=kiosklinks&kioskid="
              + kioskIdStr + "&linktype=" + linkType + "\">" + backendMessages
              .getString("relationships.view") + "</a>]";
    }
    writeToSetupScreen(req, resp, msg, Constants.VIEW_KIOSKS);
    xLogger.fine("Exiting createOrModifyKioskLink");
  }

  @SuppressWarnings("unchecked")
  private void setKioskPermissions(HttpServletRequest req, HttpServletResponse resp,
                                   EntitiesService as, ResourceBundle backendMessages,
                                   ResourceBundle messages)
      throws ServiceException, IOException, NumberFormatException, ObjectNotFoundException {
    xLogger.fine("Entered createOrModifyKioskLink");
    Map<String, String[]> linkDetails = req.getParameterMap();
    linkDetails = cleanMap(linkDetails);
    String kioskIdStr = null;
    SecureUserDetails sUser = SecurityMgr.getUserDetails(req.getSession());
    Long domainId = SessionMgr.getCurrentDomain(req.getSession(), sUser.getUsername());
    // Get parameters and update object
    if (linkDetails.containsKey("kioskid")) {
      kioskIdStr = linkDetails.get("kioskid")[0];
      if (kioskIdStr.equalsIgnoreCase("")) {
        writeToSetupScreen(req, resp,
            "Sorry, could not create relationship since kiosk ID is missing.",
            Constants.VIEW_KIOSKS);
        return;
      }
    }
    // Get the kiosk
    IKiosk k = as.getKiosk(Long.valueOf(kioskIdStr), false);
    // Get the permissions
    Permissions p = k.getPermissions();
    if (p == null) {
      p = new Permissions();
    }
    // Get customer permissions
    p.putAccess(IKioskLink.TYPE_CUSTOMER, Permissions.INVENTORY, Permissions.OP_VIEW,
        linkDetails.containsKey(IKioskLink.TYPE_CUSTOMER + ".inventory.view"));
    p.putAccess(IKioskLink.TYPE_CUSTOMER, Permissions.INVENTORY, Permissions.OP_MANAGE,
        linkDetails.containsKey(IKioskLink.TYPE_CUSTOMER + ".inventory.manage"));
    p.putAccess(IKioskLink.TYPE_CUSTOMER, Permissions.ORDERS, Permissions.OP_VIEW,
        linkDetails.containsKey(IKioskLink.TYPE_CUSTOMER + ".orders.view"));
    p.putAccess(IKioskLink.TYPE_CUSTOMER, Permissions.ORDERS, Permissions.OP_MANAGE,
        linkDetails.containsKey(IKioskLink.TYPE_CUSTOMER + ".orders.manage"));
    p.putAccess(IKioskLink.TYPE_CUSTOMER, Permissions.MASTER, Permissions.OP_MANAGE,
        linkDetails.containsKey(IKioskLink.TYPE_CUSTOMER + ".master.manage"));
    // Get vendor permissions
    p.putAccess(IKioskLink.TYPE_VENDOR, Permissions.INVENTORY, Permissions.OP_VIEW,
        linkDetails.containsKey(IKioskLink.TYPE_VENDOR + ".inventory.view"));
    p.putAccess(IKioskLink.TYPE_VENDOR, Permissions.INVENTORY, Permissions.OP_MANAGE,
        linkDetails.containsKey(IKioskLink.TYPE_VENDOR + ".inventory.manage"));
    p.putAccess(IKioskLink.TYPE_VENDOR, Permissions.ORDERS, Permissions.OP_VIEW,
        linkDetails.containsKey(IKioskLink.TYPE_VENDOR + ".orders.view"));
    p.putAccess(IKioskLink.TYPE_VENDOR, Permissions.ORDERS, Permissions.OP_MANAGE,
        linkDetails.containsKey(IKioskLink.TYPE_VENDOR + ".orders.manage"));
    p.putAccess(IKioskLink.TYPE_VENDOR, Permissions.MASTER, Permissions.OP_MANAGE,
        linkDetails.containsKey(IKioskLink.TYPE_VENDOR + ".master.manage"));
    // Update kiosk permissions
    k.setPermissions(p);

    // Update kiosk
    as.updateKiosk(k, domainId);
    String
        msg =
        messages.getString("permissions") + " " + backendMessages.getString("updated.success");

    writeToSetupScreen(req, resp,
        msg + " &nbsp;[<a href=\"/s/setup/setup.jsp?subview=kiosks&form=kiosklinks&kioskid="
            + kioskIdStr + "\">" + backendMessages.getString("relationships.view") + "</a>]",
        Constants.VIEW_KIOSKS);
    xLogger.fine("Exiting createOrModifyKioskLink");
  }

  @SuppressWarnings("unchecked")
  private void removeKioskLinks(HttpServletRequest req, HttpServletResponse resp,
                                EntitiesService as, ResourceBundle backendMessages,
                                ResourceBundle messages)
      throws ServiceException, IOException {
    Map<String, String[]> linkDetails = req.getParameterMap();
    linkDetails = cleanMap(linkDetails);
    // Get the kiosk id
    String kioskIdStr = "";
    if (linkDetails.containsKey("kioskid")) {
      kioskIdStr = linkDetails.get("kioskid")[0];
    }
    if (kioskIdStr.isEmpty()) {
      writeToSetupScreen(req, resp,
          "ERROR: No kiosk ID specified &nbsp; [<a href=\"/s/setup/setup.jsp?subview=kiosks&form=kiosklinks&kioskid="
              + kioskIdStr + "\">view relationships</a>]", Constants.VIEW_KIOSKS);
      return;
    }
    // Get domain info.
    SecureUserDetails sUser = SecurityMgr.getUserDetails(req.getSession());
    Long domainId = null;
    if (sUser != null) {
      domainId = SessionMgr.getCurrentDomain(req.getSession(), sUser.getUsername());
    } else {
      String domainIdStr = req.getParameter("domainid");
      try {
        domainId = Long.valueOf(domainIdStr);
      } catch (Exception e) {
        xLogger.severe("{0} when trying to get domain ID {1} when removing kiosk links: {1}",
            e.getClass().getName(), domainIdStr, e.getMessage());
      }
    }
    // Get link type
    String linkType = req.getParameter("linktype");
    if (linkType == null || linkType.isEmpty()) {
      linkType = IKioskLink.TYPE_CUSTOMER;
    }
    int numLinks = 0;
    if (linkDetails.containsKey("kiosklinkid")) {
      String[]
          linkIDArr =
          req.getParameterValues("kiosklinkid"); // linkDetails.get("kiosklinkids")[0].split(",");
      ArrayList<String> linkIds = new ArrayList<String>();
      for (String s : linkIDArr) {
        linkIds.add(s);
      }
      numLinks = linkIds.size();
      as.deleteKioskLinks(domainId, linkIds, sUser.getUsername());
    }
    String message = "";
    if (numLinks == 0) {
      message =
          "No links to delete!"; // NOTE: This is unlikely to be used, given front-end JS validation
    } else {
      message =
          "<b>" + String.valueOf(numLinks) + "</b> " + backendMessages.getString("deleted.success")
              + " &nbsp; [<a href=\"/s/setup/setup.jsp?subview=kiosks&form=kiosklinks&kioskid="
              + kioskIdStr + "&linktype=" + linkType + "\">" + backendMessages
              .getString("relationships.view") + "</a>]";
    }

    writeToSetupScreen(req, resp, message, Constants.VIEW_KIOSKS);
  }

  // Create a configuration entry in the data store
  private void createSystemConfiguration(HttpServletRequest req, HttpServletResponse resp,
                                         ConfigurationMgmtService cms)
      throws ServiceException, IOException {

    // Get the request parameters
    String userId = req.getParameter("userid");
    String configType = req.getParameter("configtype");
    String details = req.getParameter("details");
    boolean hasErrors = false;

    // Check for parameter errors
    String strMessage = "";
    if (configType == null || configType.isEmpty()) {
      strMessage =
          "<b>The following errors have to be corrected:</b><br><br>No configuration type has been chosen. Please choose a configuration type.<br>";
      hasErrors = true;
    }
    if (details == null || details.isEmpty()) {
      strMessage +=
          "No configuration details have been provided. Please enter configuration details.<br>";
      hasErrors = true;
    }

    // Create a configuration object
    if (!hasErrors) { // implies no parameter errors
      try {
        // Validate the config. details
        ConfigValidator.validate(configType, details);

        // Create the config object and add to data store
        IConfig c = JDOUtils.createInstance(IConfig.class);
        c.setKey(configType);
        c.setConfig(details);
        c.setUserId(userId);
        c.setLastUpdated(new Date());
        cms.addConfiguration(configType, c);

        // Form return message
        strMessage =
            "Successfully created configuration for key '" + configType
                + "'. &nbsp;&nbsp;[<a href=\"/s/admin/system_configuration.jsp?configtype="
                + configType + "\">view configuration</a>]";
      } catch (ConfigurationException e) {
        xLogger.severe("Exception when creating configuration: {0}", e.getMessage());
        strMessage =
            "<b>Configuration detail format is invalid.</b><br><br>" + "Error: " + e.getMessage()
                + "<br><br>Click the browser's Back button, correct errors and Save again.<br>";
      }
    } else {
      strMessage += "<br>Click 'Back' on browser to continue editing configuration.<br>";
    }

    writeToScreenWithMode(req, resp, strMessage, Constants.MODE_MANAGE,
        Constants.VIEW_SYSTEMCONFIGURATION);
  }

  // Update a configuration entry in the data store
  private void modifySystemConfiguration(HttpServletRequest req, HttpServletResponse resp,
                                         ConfigurationMgmtService cms)
      throws ServiceException, ObjectNotFoundException, IOException {

    // Get the request parameters
    String userId = req.getParameter("userid");
    String configType = req.getParameter("configtype");
    String details = req.getParameter("details");
    boolean hasErrors = false;

    // Check for parameter errors
    String strMessage = "";
    if (configType == null || configType.isEmpty()) {
      strMessage =
          "<b>The following errors have to be corrected:</b><br><br>No configuration type has been chosen. Please choose a configuration type.<br>";
      hasErrors = true;
    }
    if (details == null || details.isEmpty()) {
      strMessage +=
          "No configuration details have been provided. Please enter configuration details.<br>";
      hasErrors = true;
    }

    // Create a configuration object
    if (!hasErrors) {
      try {
        // Validate the config. details
        ConfigValidator.validate(configType, details);

        // Create the config object and add to data store
        IConfig c = cms.getConfiguration(configType);
        c.setConfig(details);
        c.setUserId(userId);
        c.setLastUpdated(new Date());
        cms.updateConfiguration(c);

        // Update message
        strMessage =
            "Successfully updated configuration for key '" + configType
                + "'.  &nbsp;&nbsp;[<a href=\"/s/admin/system_configuration.jsp?configtype="
                + configType + "\">view configuration</a>]";
      } catch (ConfigurationException e) {
        xLogger.severe("Exception when creating configuration: {0}", e.getMessage());
        strMessage =
            "<b>Configuration detail format is invalid.</b><br><br>" + "Error: " + e.getMessage()
                + "<br><br>Click the browser's Back button, correct errors and Save again.<br>";
      }
    } else {
      strMessage += "<br>Click 'Back' on browser to continue editing configuration.<br>";
    }

    writeToScreenWithMode(req, resp, strMessage, Constants.MODE_MANAGE,
        Constants.VIEW_SYSTEMCONFIGURATION);
  }

  // Add inventory transactions (from Service Manager)
  @SuppressWarnings("unchecked")
  private void createTransactions(HttpServletRequest req, HttpServletResponse resp,
                                  InventoryManagementService ims, ResourceBundle backendMessages,
                                  ResourceBundle messages)
      throws ServiceException, IOException {
    xLogger.fine("Entered createTransactions");

    Map<String, String[]> transDetails = req.getParameterMap();
    transDetails = cleanMap(transDetails);
    // Get the logged in user Id
    SecureUserDetails sUser = SecurityMgr.getUserDetails(req.getSession());
    String userId = sUser.getUsername();
    // Get the domain id
    Long domainId = SessionMgr.getCurrentDomain(req.getSession(), userId);
    // Get the transaction type
    String transType = null;
    if (transDetails.containsKey("transtype")) {
      transType = transDetails.get("transtype")[0];
      if (transType == null || transType.isEmpty()) {
        throw new ServiceException("Transaction type cannot be null or empty");
      }
    }
    // Get the kiosk id
    Long kioskId = null;
    if (transDetails.containsKey("kioskid")) {
      String kioskIdStr = transDetails.get("kioskid")[0];
      if (!kioskIdStr.isEmpty()) {
        try {
          kioskId = Long.valueOf(kioskIdStr);
        } catch (NumberFormatException e) {
          throw new ServiceException(e.getMessage());
        }
      }
    }
    // Get the customer/vendor kiosk id, if specified
    Long linkedKioskId = null;
    if (transDetails.containsKey("customerkioskid")) {
      String kioskIdStr = transDetails.get("customerkioskid")[0];
      if (!kioskIdStr.isEmpty()) {
        try {
          linkedKioskId = Long.valueOf(kioskIdStr);
        } catch (NumberFormatException e) {
          throw new ServiceException(e.getMessage());
        }
      }
    }
    if (transDetails.containsKey("vendorkioskid")) {
      String kioskIdStr = transDetails.get("vendorkioskid")[0];
      if (!kioskIdStr.isEmpty()) {
        try {
          linkedKioskId = Long.valueOf(kioskIdStr);
        } catch (NumberFormatException e) {
          throw new ServiceException(e.getMessage());
        }
      }
    }
    if (transDetails.containsKey("destinationkioskid")) { // for transfers
      String kioskIdStr = transDetails.get("destinationkioskid")[0];
      if (!kioskIdStr.isEmpty()) {
        try {
          linkedKioskId = Long.valueOf(kioskIdStr);
        } catch (NumberFormatException e) {
          throw new ServiceException(e.getMessage());
        }
      }
    }
    // Reason, if present
    String reason = null;
    if (transDetails.containsKey("reason_" + transType)) {
      reason = transDetails.get("reason_" + transType)[0];
      if (reason != null && reason.isEmpty()) {
        reason = null;
      }
    }

    // Get the list of all materials in this kiosk
    List<IInvntry>
        inList =
        ims.getInventoryByKiosk(kioskId, null).getResults(); // TODO: pagination?
    if (inList == null || inList.size() == 0) {
      throw new ServiceException(
          "No materials associated with this kiosk [kioskId: " + kioskId + "]");
    }
    // Get the quantities, and update transactions
    List<ITransaction> transList = new ArrayList<ITransaction>();
    List<ITransaction> errors = null;
    Iterator<IInvntry> iterator = inList.iterator();
    String message = "";
    Date now = new Date();
    while (iterator.hasNext()) {
      IInvntry inv = iterator.next();
      Long materialId = inv.getMaterialId();
      // Get the quantity associated with this inventory - either standalone or by batch
      Map<String, BigDecimal> quantityByBatch = getQuantityByBatch(materialId, transDetails);
      xLogger.fine("quantityByBatch: {0}", quantityByBatch);
      if (quantityByBatch == null || quantityByBatch.isEmpty()) {
        continue;
      }
//			String materialIdStr = materialId.toString();
//			if ( transDetails.containsKey( materialIdStr ) || hasBatches ) {
      Iterator<String> batchIds = quantityByBatch.keySet().iterator();
      while (batchIds.hasNext()) {
        String batchId = batchIds.next();
        // Get quantity by batch if necessary
        BigDecimal quantity = quantityByBatch.get(batchId);
        // Check for transaction errors
        message += checkTransactionErrors(transType, quantity, inv, backendMessages);
        if (message.isEmpty()) {
          // Create an inventory transaction
          ITransaction trans = JDOUtils.createInstance(ITransaction.class);
          trans.setDomainId(domainId);
          trans.setKioskId(kioskId);
          trans.setMaterialId(materialId);
          trans.setQuantity(quantity);
          trans.setType(transType);
          trans.setSourceUserId(userId);
          trans.setTimestamp(now);
          trans.setSrc(SourceConstants.WEB);
          trans.setReason(reason);
          if (linkedKioskId != null) {
            trans.setLinkedKioskId(linkedKioskId);
          }
          // Set the batch parameters, if present
          if (!batchId.isEmpty()) {
            setBatchParameters(batchId, trans, transDetails);
          }
          transDao.setKey(trans);
          // Add to list
          transList.add(trans);
        }
      }
    }
    // Check if any updates are required
    boolean updateErrors = false;
    if (message.isEmpty()) {
      if (transList.size() > 0) {
        // Update transactions
        try {
          errors = ims.updateInventoryTransactions(domainId, transList);
          // Schedule reverse transactions, if necessary
                                        /* 4/2/2013 - DISABLING THIS, given its semantics have to be ascertained, and there were concurrent modification exceptions (perhaps due to updates > 1 per sec. for an entity group)
                                        try {
						RESTUtil.scheduleReverseTransactions( domainId, userId, sUser.getPassword(), transType, transList, errors );
					} catch ( Exception e ) {
						xLogger.severe( "{0} when scheduling reverse transactions for trans-type {1} for kiosk {2} in domain {3} for {4} transactions with {5} errors: {6}", e.getClass().getName(), transType, kioskId, domainId, transList.size(), ( errors == null ? "0" : errors.size() ), e.getMessage() );
					}
					*/
          // Check for any update errors
          if (errors != null && errors.size() > 0) {
            updateErrors = true;
            Iterator<ITransaction> it = errors.iterator();
            while (it.hasNext()) {
              ITransaction trans = it.next();
              message += " - " + trans.getMessage() + "<br/>";
            }
            xLogger.warn(
                "Some errors were encountered when updating inventory in domain {0} for trans-type {1}: {2}",
                domainId, transType, message);
          }
        } catch (Exception e) {
          xLogger
              .severe("{0} when creating transactions in domain {1} by user {2}: {3}", e.getClass(),
                  domainId, userId, e.getMessage(), e);
          message = backendMessages.getString("error") + ": " + e.getMessage() + "<br/>";
        }
      } else {
        message += " - No quantities were specified. Hence, no transactions have been added.";
      }
    }

    // Form the final return messages
    String
        inventoryUpdatesUrl =
        "/s/inventory/inventory.jsp?kioskid=" + kioskId + "&subview=Inventory Transactions";
    if (message.isEmpty()) { // success
      message =
          "<b>" + transList.size() + "</b> " + messages.getString("transactions") + " "
              + backendMessages.getString("updated.success") + " &nbsp;[<a href=\""
              + inventoryUpdatesUrl + "\">" + backendMessages.getString("transactions.view")
              + "</a>]";
    } else { // errors
      if (updateErrors) {
        message =
            backendMessages.getString("errors.oneormore") + ":<br/>" + message + "<br/><br/>"
                + backendMessages.getString("transactions.omitted") + " &nbsp;[<a href=\""
                + inventoryUpdatesUrl + "\">" + backendMessages.getString("transactions.view")
                + "</a>]";
      } else {
        message =
            backendMessages.getString("errors.oneormore") + ":<br/>" + message + "<br/><br/>"
                + backendMessages.getString("browser.goback") + ".";
      }
    }

    writeToScreen(req, resp, message, Constants.VIEW_INVENTORY);

    xLogger.fine("Exiting createTransactions");
  }

  // Undo transactions
  private void undoTransactions(HttpServletRequest req, HttpServletResponse resp,
                                InventoryManagementService ims, ResourceBundle backendMessages,
                                ResourceBundle messages) throws ServiceException, IOException {
    xLogger.fine("Entered undoTransactions");
    // Get the relevant parameters
    String kioskIdStr = req.getParameter("kioskid");
    String duration = req.getParameter("duration");
    String customerKioskId = req.getParameter("customerkioskid");
    String vendorKioskId = req.getParameter("vendorkioskid");
    String transType = req.getParameter("transtype");
    // Get the list of all selected transactions
    String[] tids = req.getParameterValues("transactionid");
    if (tids == null || tids.length == 0) {
      writeToScreen(req, resp, "No transactions selected to undo", Constants.VIEW_INVENTORY);
      return;
    }
    List<String> tidsL = StringUtil.getList(tids);
    // Undo transactions
    List<ITransaction> errorList = ims.undoTransactions(tidsL);
    // Form the URL for viewing transactions
    String
        viewTransUrl =
        "/s/inventory/inventory.jsp?subview=Inventory Transactions&kioskid=" + kioskIdStr;
    if (duration != null && !duration.isEmpty()) {
      viewTransUrl += "&duration=" + duration;
    }
    if (customerKioskId != null) {
      viewTransUrl += "&customerkioskid=" + customerKioskId;
    }
    if (vendorKioskId != null) {
      viewTransUrl += "&vendorkioskid=" + vendorKioskId;
    }
    if (transType != null) {
      viewTransUrl += "&transtype=" + transType;
    }
    // Send message
    int errors = errorList.size();
    int successes = tidsL.size() - errors;
    String msg = "";
    if (successes > 0) {
      msg =
          "<b>" + successes + "</b> " + backendMessages.getString("transactions.undo")
              + " &nbsp[<a href=\"" + viewTransUrl + "\">" + backendMessages
              .getString("transactions.view") + "</a>]";
    }
    if (errors > 0) {
      msg +=
          "<br/><br/><b>" + errors + "</b> " + backendMessages.getString("transactions.notundone")
              + ". " + backendMessages.getString("browser.goback");
    }
    writeToScreen(req, resp, msg, Constants.VIEW_INVENTORY);
    xLogger.fine("Exiting undoTransactions");
  }

  // Create orders based on web-form input
  @SuppressWarnings("unchecked")
  private void createOrders(HttpServletRequest req, HttpServletResponse resp,
                            OrderManagementService oms, InventoryManagementService ims,
                            MaterialCatalogService mcs,
                            ResourceBundle backendMessages, ResourceBundle messages)
      throws LogiException, IOException {
    xLogger.fine("Entered createOrders");
    String message = "";
    // Get the logged in user Id
    SecureUserDetails sUser = SecurityMgr.getUserDetails(req.getSession());
    String userId = sUser.getUsername();
    // Get the domain id
    Long domainId = SessionMgr.getCurrentDomain(req.getSession(), userId);
    // Get domain config
    DomainConfig dc = DomainConfig.getInstance(domainId);
    // Get the kiosk id
    Long kioskId = null;
    String kioskIdStr = req.getParameter("kioskid");
    if (kioskIdStr != null && !kioskIdStr.isEmpty()) {
      try {
        kioskId = Long.valueOf(kioskIdStr);
      } catch (NumberFormatException e) {
        throw new ServiceException(e.getMessage());
      }
    }
    // Get the subview
    String subview = req.getParameter("subview");
    // Get intial status, if any
    String status = req.getParameter("status");
    // Get the order message, if any
    String orderMsg = req.getParameter("message");
    // Get servicing kiosk, if any
    String servicingKioskIdStr = req.getParameter("vendorkioskid"); // always sent by the form
    Long servicingKioskId = null;
    if (servicingKioskIdStr != null && !servicingKioskIdStr.isEmpty()) {
      servicingKioskId = Long.valueOf(servicingKioskIdStr);
    }
    // Get the list of all materials in this kiosk
    List<IInvntry>
        inList =
        ims.getInventoryByKiosk(kioskId, null).getResults(); // TODO: pagination?
    if (inList == null || inList.size() == 0) {
      writeToScreen(req, resp, "No materials assoiated with this kiosk", Constants.VIEW_ORDERS);
      return;
    }
    // Check if inventory is enabled, esp. for deciding on auto issues/receipts on order status change
    //boolean isInventoryEnabled =  ( dc == null || !dc.isCapabilityDisabled( DomainConfig.CAPABILITY_INVENTORY ) );
    // Iterate through the materials to get the order quantities and form inventory list
    List<ITransaction> transactions = new ArrayList<ITransaction>();
    Iterator<IInvntry> it = inList.iterator();
    while (it.hasNext()) {
      IInvntry inv = it.next();
      Long materialId = inv.getMaterialId();
      String quantityStr = req.getParameter(materialId.toString()); // get quantity from the form
      BigDecimal quantity = BigDecimal.ZERO;
      if (quantityStr != null && !quantityStr.isEmpty()) {
        try {
          quantity = new BigDecimal(quantityStr);
          message += checkOrderErrors(quantity, inv, backendMessages);
        } catch (NumberFormatException e) {
          message +=
              " - " + quantityStr + " " + mcs.getMaterial(materialId).getName() + ": "
                  + backendMessages.getString("quantity.invalid") + "<br/>";
        }
        if (message.isEmpty()) {
          // Form the inventory transaction
          ITransaction
              trans =
              getInventoryTransaction(domainId, inv, quantity, ITransaction.TYPE_ORDER, userId,
                  null, null); // TODO: send batch ID & expiry here from order form
          // Add to list
          transactions.add(trans);
        }
      }
    }
    boolean allowEmptyOrders = dc.allowEmptyOrders();
    // Update order transactions
    if (message.isEmpty()) { /// && transactions.size() > 0
      boolean createOrder = dc.autoOrderGeneration();
      // TODO: Get the additional parameters from the form
      OrderResults
          or =
          oms.updateOrderTransactions(domainId, userId, ITransaction.TYPE_ORDER, transactions,
              kioskId, null, orderMsg, createOrder, servicingKioskId, null, null, null, null, null,
              null, BigDecimal.ZERO, null, null,
              allowEmptyOrders,SourceConstants.MOBILE); // no latitude/longitude passed here
      IOrder order = or.getOrder();
      // If an initial status is specified, update order status
      if (status != null && !status.isEmpty()) {
        order.setStatus(status);
        order.setUpdatedBy(userId);
        order.setUpdatedOn(order.getCreatedOn());
        // Update order's initial status
        //oms.updateOrder( order, isInventoryEnabled );
        OrderUtils.updateOrder(order, dc);
      }
      String strPrice = null; // get price statement
      if (order != null && BigUtil.greaterThanZero(order.getTotalPrice())) {
        strPrice =
            backendMessages.getString("order.price") + " <b>" + order.getPriceStatement() + "</b>.";
      }
      xLogger.fine("strPrice: " + strPrice);
      String
          ordersUrl =
          "/s/orders/orders.jsp?subview=" + subview + "&kioskid=" + kioskIdStr + "&otype="
              + IOrder.TYPE_PURCHASE;
      if (createOrder) {
        ordersUrl += "&show=true";
        message =
            messages.getString("order") + " <b>" + order.getOrderId() + "</b> " + backendMessages
                .getString("created.successwith") + " <b>" + or.getOrder().size() + "</b> "
                + messages.getString("items") + ". " + (strPrice == null ? "" : strPrice)
                + "  [<a href=\"" + ordersUrl + "\">" + backendMessages.getString("items.view")
                + "</a>]";
      } else {
        message =
            "<b>" + transactions.size() + "</b> " + backendMessages.getString("items.created")
                + " &nbsp;[<a href=\"" + ordersUrl + "\">" + backendMessages.getString("items.view")
                + "</a>]";
      }
    } else {
      message = backendMessages.getString("errors.oneormore") + ":<br/>" + message +
          "<br/><br/>" + backendMessages.getString("browser.goback") + ".";
    }

    xLogger.fine("Existing createOrders");
    writeToScreen(req, resp, message, Constants.VIEW_ORDERS);
  }

  // Check existence of a user
  private void checkIfUserExists(HttpServletRequest req, HttpServletResponse resp,
                                 UsersService as, ResourceBundle backendMessages,
                                 ResourceBundle messages) throws IOException {
    xLogger.fine("Entered checkIfUserExists");

    String userId = req.getParameter("userid");
    String message = "";
    try {
      if (as.userExists(userId)) {
        message = "'" + userId + "' " + backendMessages.getString("user.exists") + ".";
      } else {
        message = "'" + userId + "' " + backendMessages.getString("isavailable") + ".";
      }
    } catch (ServiceException e) {
      xLogger.severe("Exception when checking user ID existence for userID {0}", userId);
      message = backendMessages.getString("error") + ": " + e.getMessage();
    }
    // Write a response text
    writeText(resp, message);

    xLogger.fine("Exiting checkIfUserExists");
  }

  private void saveOrdering(HttpServletRequest req, HttpServletResponse resp, EntitiesService as,
                            ResourceBundle backendMessages, ResourceBundle messages,
                            String entityType) throws IOException {
    xLogger.fine("Entering saveOrdering");
    if (entityType.equals("me")) {
      // save the route for managed entity
      saveManagedEntitiesOrdering(req, resp, as, backendMessages, messages);
    } else if (entityType.equals("re")) {
      // save the route for relationships
      saveRelatedEntitiesOrdering(req, resp, as, backendMessages, messages);
    } else {
      xLogger.severe("Unsupported type: {0}", entityType);
    }

    xLogger.fine("Exiting saveOrdering");
  }

  // Get quantities from form by batch numbers; if no batches, then then Map will have a single entry with an empty key string
  private Map<String, BigDecimal> getQuantityByBatch(Long materialId,
                                                     Map<String, String[]> transDetails) {
    xLogger.fine("Entered getQuantityByBatch");
    Map<String, BigDecimal> bid2Quantity = new HashMap<>();
    String
        batchNumbersParam =
        "batchnumbers_" + materialId; // esp. when multiple batch numbers are involved
    boolean hasBatches = transDetails.containsKey(batchNumbersParam);
    if (hasBatches) { // has multiple batches, say, as in issue/wastage/transfer
      String batchIdCSV = transDetails.get(batchNumbersParam)[0];
      if (batchIdCSV == null || batchIdCSV.isEmpty()) {
        xLogger.warn("No batch IDs given for material {0}", materialId);
        return null;
      }
      String batchIds[] = batchIdCSV.split(",");
      for (String batchId : batchIds) {
        String quantityStr = null;
        String batchQuantityParam = "batchquantity_" + materialId + "_" + batchId;
        if (transDetails.containsKey(batchQuantityParam)) {
          quantityStr = transDetails.get(batchQuantityParam)[0].trim();
        }
        if (quantityStr != null && !quantityStr.isEmpty()) {
          try {
            bid2Quantity.put(batchId, new BigDecimal(quantityStr));
          } catch (Exception e) {
            xLogger.warn("{0} when converting quantity {1} for material {2}: {3}",
                e.getClass().getName(), quantityStr, materialId, e.getMessage());
          }
        }
      }
    } else { // has no batch number or a single batch entry (as in stock count or receipts)
      String batchId = "";
      String quantityStr = null;
      String materialIdStr = materialId.toString();
      String batchNumberParam = "batchnumber_" + materialIdStr;
      if (transDetails.containsKey(batchNumberParam)) {
        batchId = transDetails.get(batchNumberParam)[0].trim();
      }
      if (transDetails.containsKey(materialIdStr)) {
        quantityStr = transDetails.get(materialIdStr)[0].trim();
      }
      if (quantityStr != null && !quantityStr.isEmpty()) {
        try {
          bid2Quantity.put(batchId, new BigDecimal(quantityStr));
        } catch (Exception e) {
          xLogger.warn("{0} when converting quantity {1} for material {2}: {3}",
              e.getClass().getName(), quantityStr, materialId, e.getMessage());
        }
      }
    }
    xLogger.fine("Exiting getQuantityByBatch");
    return bid2Quantity;
  }

  private void saveManagedEntitiesOrdering(HttpServletRequest req, HttpServletResponse resp,
                                           EntitiesService as, ResourceBundle backendMessages,
                                           ResourceBundle messages) throws IOException {
    xLogger.fine("Entering saveManagedEntitiesOrdering");
    String userId = req.getParameter("userid");
    if (userId == null || userId.isEmpty()) {
      xLogger.severe("Invalid, null or empty userId. {0}", userId);
      return;
    }
    String routeQueryString = req.getParameter("routequerystring");
    if (routeQueryString == null || routeQueryString.isEmpty()) {
      xLogger.severe("Invalid, null or empty routeQueryString. {0}", routeQueryString);
      return;
    }

    String message = null;
    xLogger.fine("userId = {0}", userId);
    xLogger.fine("routeQueryString = {0}", routeQueryString);
    // Get the domain Id
    SecureUserDetails sUser = SecurityMgr.getUserDetails(req.getSession());
    Long domainId = SessionMgr.getCurrentDomain(req.getSession(), sUser.getUsername());

    try {
      as.updateManagedEntitiesOrdering(domainId, userId, routeQueryString);
    } catch (ServiceException e) {
      xLogger.severe("Exception when saving managed entities route for userID {0}", userId);
      message = backendMessages.getString("error") + ": " + e.getMessage();
    }
    String jsonString = null;
    if (message != null && !message.isEmpty()) {
      // There was an error
      jsonString = "{ \"st\" : \"1\", \"msg\" : \"" + message + "\" } ";
      resp.setStatus(500);
    } else {
      // Success
      jsonString = "{ \"st\" : \"0\", \"msg\" : \"\" }";
      resp.setStatus(200);
    }
    resp.setContentType(JsonRestServlet.JSON_UTF8);
    PrintWriter pw = resp.getWriter();
    pw.write(jsonString);
    pw.close();
    xLogger.fine("Exiting saveManagedEntitiesOrdering");
  }

  private void saveRelatedEntitiesOrdering(HttpServletRequest req, HttpServletResponse resp,
                                           EntitiesService as, ResourceBundle backendMessages,
                                           ResourceBundle messages) throws IOException {
    xLogger.fine("Entering saveRelatedEntitiesOrdering");
    String kioskIdStr = req.getParameter("kioskid");
    if (kioskIdStr == null || kioskIdStr.isEmpty()) {
      xLogger.severe("Invalid, null or empty kioskIdStr. {0}", kioskIdStr);
      return;
    }
    Long kioskId = Long.valueOf(kioskIdStr);
    String routeQueryString = req.getParameter("routequerystring");
    if (routeQueryString == null || routeQueryString.isEmpty()) {
      xLogger.severe("Invalid, null or empty routeQueryString. {0}", routeQueryString);
      return;
    }
    String linkType = req.getParameter("linktype");
    if (linkType == null || linkType.isEmpty()) {
      xLogger.severe("Invalid, null or empty linkType. {0}", linkType);
      return;
    }

    String message = null;
    xLogger.fine("kioskId = {0}", kioskId);
    xLogger.fine("routeQueryString = {0}", routeQueryString);
    // Get the domain Id
    SecureUserDetails sUser = SecurityMgr.getUserDetails(req.getSession());
    Long domainId = SessionMgr.getCurrentDomain(req.getSession(), sUser.getUsername());

    try {
      as.updateRelatedEntitiesOrdering(domainId, kioskId, linkType, routeQueryString);
    } catch (ServiceException e) {
      xLogger.severe("Exception when saving linked kiosk route for userID {0}", kioskId);
      message = backendMessages.getString("error") + ": " + e.getMessage();
    }
    String jsonString = null;
    if (message != null && !message.isEmpty()) {
      // There was an error
      jsonString = "{ \"st\" : \"1\", \"msg\" : \"" + message + "\" } ";
      resp.setStatus(500);
    } else {
      // Success
      jsonString = "{ \"st\" : \"0\", \"msg\" : \"\" }";
      resp.setStatus(200);
    }
    resp.setContentType(JsonRestServlet.JSON_UTF8);
    PrintWriter pw = resp.getWriter();
    pw.write(jsonString);
    pw.close();

    xLogger.fine("Exiting saveRelatedEntitiesOrdering");
  }

  private void resetOrdering(HttpServletRequest req, HttpServletResponse resp, EntitiesService as,
                             ResourceBundle backendMessages, ResourceBundle messages,
                             String entityType) throws IOException {
    xLogger.fine("Entering resetOrdering");
    if (entityType.equals("me")) {
      // save the route for managed entity
      resetManagedEntitiesOrdering(req, resp, as, backendMessages, messages);
    } else if (entityType.equals("re")) {
      // save the route for relationships
      resetRelatedEntitiesOrdering(req, resp, as, backendMessages, messages);
    } else {
      xLogger.severe("Unsupported type: {0}", entityType);
    }

    xLogger.fine("Exiting resetOrdering");
  }

  private void resetManagedEntitiesOrdering(HttpServletRequest req, HttpServletResponse resp,
                                            EntitiesService as, ResourceBundle backendMessages,
                                            ResourceBundle messages) throws IOException {
    xLogger.fine("Entering resetManagedEntitiesOrdering");
    String userId = req.getParameter("userid");
    if (userId == null || userId.isEmpty()) {
      xLogger.severe("Invalid, null or empty userId. {0}", userId);
      return;
    }
    // Get the map parameter
    String map = req.getParameter("map");
    boolean
        mapView =
        (map != null) ? true
            : false; // If map parameter is present, it means the reset was clicked in the map view
    String message = null;
    xLogger.fine("userId = {0}", userId);

    try {
      as.resetManagedEntitiesOrdering(userId);
      // The default redirect url has table view of managed entities
      String redirectUrl = "/s/setup/setup.jsp?subview=users&form=userdetails&id=" + userId;
      if (mapView) // If mapView is true, then add the map parameter so that redirection happens to map view of managed entities
      {
        redirectUrl += "&map";
      }
      writeToSetupScreen(req, resp,
          backendMessages.getString("resetordering.managedentities.success")
              + ". &nbsp; [<a href=\"" + redirectUrl + "\">" + backendMessages
              .getString("user.view") + "</a>]", Constants.VIEW_USERS);
    } catch (ServiceException e) {
      xLogger.severe("Exception when resetting managed entities ordering for userID {0}", userId);
      message = backendMessages.getString("error") + ": " + e.getMessage();
      // Write a response text
      writeText(resp, message);
    }
    xLogger.fine("Exiting resetManagedEntitiesOrdering");
  }

  private void resetRelatedEntitiesOrdering(HttpServletRequest req, HttpServletResponse resp,
                                            EntitiesService as, ResourceBundle backendMessages,
                                            ResourceBundle messages) throws IOException {
    xLogger.fine("Entering resetRelatedEntitiesOrdering");
    String kioskId = req.getParameter("kioskid");
    String linkType = req.getParameter("linktype");
    if (kioskId == null || kioskId.isEmpty()) {
      xLogger.severe("Invalid, null or empty kioskId. {0}", kioskId);
      return;
    }
    if (linkType == null || linkType.isEmpty()) {
      xLogger.severe("Invalid, null or empty linkType. {0}", linkType);
      return;
    }
    // Get the map parameter
    String map = req.getParameter("map");
    boolean
        mapView =
        (map != null) ? true
            : false; // If map parameter is present, it means the reset was clicked in the map view

    String message = null;
    xLogger.fine("kioskId = {0}", kioskId);

    try {
      as.resetRelatedEntitiesOrdering(Long.valueOf(kioskId), linkType);
      // The default redirect url has table view of related entities
      String
          redirectUrl =
          "/s/setup/setup.jsp?subview=kiosks&form=kiosklinks&kioskid=" + kioskId + "&linktype="
              + linkType;
      if (mapView) // If mapView is true, then add the map parameter so that redirection happens to map view of managed entities
      {
        redirectUrl += "&map";
      }
      writeToSetupScreen(req, resp,
          backendMessages.getString("resetordering.linkedkiosks.success") + ". &nbsp; [<a href=\""
              + redirectUrl + "\">" + backendMessages.getString("relationships.view") + "</a>]",
          Constants.VIEW_KIOSKS);
    } catch (ServiceException e) {
      xLogger.severe("Exception when resetting related entities ordering for kioskId {0}", kioskId);
      message = backendMessages.getString("error") + ": " + e.getMessage();
      // Write a response text
      writeText(resp, message);
    }
    xLogger.fine("Exiting resetRelatedEntitiesOrdering");
  }

  // Check errors in transaction quantities entered; return error message (with HTML line-breaks), if any
  private String checkTransactionErrors(String transType, BigDecimal quantity, IInvntry inv,
                                        ResourceBundle backendMessages) {
    String message = "";
    String materialName = inv.getMaterialName();
    BigDecimal stockOnHand = inv.getStock();
    if (BigUtil.lesserThanZero(quantity)) {
      message =
          " - " + quantity + " " + materialName + ": " + backendMessages
              .getString("quantity.invalid") + ". " + backendMessages
              .getString("quantity.greaterthanzero") + ".<br/>";
    } else if (ITransaction.TYPE_ISSUE.equals(transType) || ITransaction.TYPE_WASTAGE
        .equals(transType) || ITransaction.TYPE_TRANSFER.equals(transType)) {
      if (BigUtil.equalsZero(quantity)) {
        message +=
            " - " + quantity + " " + materialName + ": " + backendMessages
                .getString("quantity.invalid") + ". " + backendMessages
                .getString("quantity.greaterthanzero") + ".<br/>";
      } else if (BigUtil.greaterThan(quantity, stockOnHand)) {
        message +=
            " - " + quantity + " " + materialName + ": " + backendMessages
                .getString("quantity.invalid") + ". " + backendMessages
                .getString("quantity.exceedsstock") + " " + stockOnHand + ".<br/>";
      }
    } else if (ITransaction.TYPE_RECEIPT.equals(transType) || ITransaction.TYPE_ORDER
        .equals(transType)) {
      if (BigUtil.equalsZero(quantity)) {
        message +=
            " - " + quantity + " " + materialName + ": " + backendMessages
                .getString("quantity.invalid") + ". " + backendMessages
                .getString("quantity.greaterthanzero") + ".<br/>";
      }
    }

    return message;
  }

  // Check errors in order quantities
  private String checkOrderErrors(BigDecimal quantity, IInvntry inv,
                                  ResourceBundle backendMessages) {
    String message = "";
    String materialName = inv.getMaterialName();
    if (BigUtil.lesserThanZero(quantity)) {
      message +=
          " - " + quantity + " " + materialName + ": " + backendMessages
              .getString("quantity.invalid") + ". " + backendMessages
              .getString("quantity.greaterthanzero") + ".<br/>";
    }
    return message;
  }

  // Get the inventory transaction object for a given quantity
  private ITransaction getInventoryTransaction(Long domainId, IInvntry inv, BigDecimal quantity,
                                               String transType, String userId, String batchId,
                                               Date batchExpiry) {
    Long kioskId = inv.getKioskId();
    Long materialId = inv.getMaterialId();
    Date now = new Date();
    ITransaction t = JDOUtils.createInstance(ITransaction.class);
    t.setKioskId(kioskId);
    t.setMaterialId(materialId);
    t.setQuantity(quantity);
    t.setType(transType);
    t.setDomainId(domainId);
    t.setSourceUserId(userId);
    t.setTimestamp(now);
    t.setBatchId(batchId);
    t.setBatchExpiry(batchExpiry);
    transDao.setKey(t);
    return t;
  }

  private void writeToScreen(HttpServletRequest req, HttpServletResponse resp, String message,
                             String view) throws IOException {
    writeToScreenWithMode(req, resp, message, null, view);
  }

  private void writeToScreenWithMode(HttpServletRequest req, HttpServletResponse resp,
                                     String message, String mode, String view)
      throws IOException {
    writeToScreen(req, resp, message, mode, view, "/s/message.jsp");
  }

  private void writeToSetupScreen(HttpServletRequest req, HttpServletResponse resp, String message,
                                  String subview)
      throws IOException {
    String
        url =
        "/s/setup/setup.jsp?form=messages&subview=" + subview + "&message=" + URLEncoder
            .encode(message, "UTF-8");
    writeToScreen(req, resp, message, null, null, url);
  }

  // Trims the input string, strips < > characters
  private String cleanInput(String input) {
    if (input != null && !input.equalsIgnoreCase("")) {
      input = input.replace("<", "");
      input = input.replace(">", "");
      input = input.trim();
      input = input.replaceAll(" +", " ");
    }
    return input;
  }

  // Trim all strings that are values in a hashmap and return the map
  private Map<String, String[]> cleanMap(Map<String, String[]> input) {
    Map<String, String[]> cleanedMap = new HashMap<String, String[]>();
    if (input == null || input.size() == 0) {
      return null;
    }
    for (String s : input.keySet()) {
      String[] value = input.get(s);
      int counter = 0;
      for (String v : value) {
        value[counter] = cleanInput(v);
        counter++;
      }
      cleanedMap.put(s, value);
    }
    return cleanedMap;
  }

  // Private method that sets the ui preference for the user.
  private void setUiPreference(HttpServletRequest req, HttpServletResponse resp, UsersService as,
                               ResourceBundle backendMessages, ResourceBundle messages)
      throws ServiceException, IOException {
    String userIdStr = req.getParameter("userid");
    String uiPrefStr = req.getParameter("uipref");
    boolean uiPref = "true".equals(uiPrefStr); // else false
    xLogger.fine("uiPref: {0}, userIdStr: {1}", uiPref, userIdStr);
    if (userIdStr != null && !userIdStr.isEmpty()) {
      String message = "";
      try {
        as.setUiPreferenceForUser(userIdStr, uiPref);
      } catch (Exception e) {
        xLogger.severe("Exception {0} while setting ui preference for user {1}. Message: {2}",
            e.getClass().getName(), userIdStr, e.getMessage());
        message = e.getMessage();
      }
      xLogger.fine("message: " + message);
      writeText(resp, message);
    }
  }

  // Private utility method that sets updates the general configuration to set ui preference for the entire domain to new UI. It also sets the flag onlyNewUI. This internally calls the ConfigurationServlet inside of a task and updates the general configuration
  // userid needs to be sent as a parameter, otherwise, sUser is null inside the updateGeneralConfiguration method of Configuration which can result in NullPointerException.
  private void setUiPreferenceForDomain(HttpServletRequest req, HttpServletResponse resp,
                                        Long domainId, String userId, boolean uiPref,
                                        boolean onlyNewUI)
      throws MalformedURLException, IOException {
    xLogger.fine("Entering setUiPreferenceForDomain");
    // Call the ConfigurationServlet's updateGeneralConfiguration method through HttpUtil so that the uiPref is set for the domain.
    Map<String, String> params = new HashMap<String, String>();
    params.put("type", "general");
    params.put("domainid", domainId.toString());
    params.put("userid", userId);
    params.put("uipref", String.valueOf(uiPref));
    params.put("onlynewui", String.valueOf(onlyNewUI));
    xLogger.fine("params: {0}", params.toString());

    try {
      taskService.schedule(ITaskService.QUEUE_DEFAULT, CONFIGURATION_SERVLET_TASK_URL, params,
          ITaskService.METHOD_POST);
    } catch (Exception e) {
      xLogger.warn("{0} when scheduling task to set up preference for domain domain {1}: {2}",
          e.getClass().getName(), domainId, e.getMessage(), e);
    }
    xLogger.fine("Exiting setUiPreferenceForDomain");
  }
}

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

import com.google.gson.internal.LinkedTreeMap;

import com.logistimo.AppFactory;
import com.logistimo.api.builders.MarkerBuilder;
import com.logistimo.api.builders.TransactionBuilder;
import com.logistimo.api.models.MarkerModel;
import com.logistimo.api.models.TransactionDomainConfigModel;
import com.logistimo.api.models.TransactionModel;
import com.logistimo.api.util.DedupUtil;
import com.logistimo.auth.SecurityConstants;
import com.logistimo.auth.SecurityMgr;
import com.logistimo.auth.utils.SecurityUtils;
import com.logistimo.auth.utils.SessionMgr;
import com.logistimo.config.models.ActualTransConfig;
import com.logistimo.config.models.DomainConfig;
import com.logistimo.config.models.InventoryConfig;
import com.logistimo.config.models.MatStatusConfig;
import com.logistimo.constants.CharacterConstants;
import com.logistimo.constants.Constants;
import com.logistimo.constants.SourceConstants;
import com.logistimo.dao.JDOUtils;
import com.logistimo.entities.auth.EntityAuthoriser;
import com.logistimo.entities.entity.IKiosk;
import com.logistimo.entities.entity.IKioskLink;
import com.logistimo.entities.service.EntitiesService;
import com.logistimo.entities.service.EntitiesServiceImpl;
import com.logistimo.exception.BadRequestException;
import com.logistimo.exception.InvalidServiceException;
import com.logistimo.inventory.TransactionUtil;
import com.logistimo.inventory.dao.ITransDao;
import com.logistimo.inventory.dao.impl.TransDao;
import com.logistimo.inventory.entity.ITransaction;
import com.logistimo.inventory.service.InventoryManagementService;
import com.logistimo.inventory.service.impl.InventoryManagementServiceImpl;
import com.logistimo.logger.XLog;
import com.logistimo.materials.entity.IMaterial;
import com.logistimo.materials.service.impl.MaterialCatalogServiceImpl;
import com.logistimo.pagination.Navigator;
import com.logistimo.pagination.PageParams;
import com.logistimo.pagination.Results;
import com.logistimo.security.SecureUserDetails;
import com.logistimo.services.DuplicationException;
import com.logistimo.services.ObjectNotFoundException;
import com.logistimo.services.Resources;
import com.logistimo.services.ServiceException;
import com.logistimo.services.Services;
import com.logistimo.services.cache.MemcacheService;
import com.logistimo.users.entity.IUserAccount;
import com.logistimo.users.service.UsersService;
import com.logistimo.users.service.impl.UsersServiceImpl;
import com.logistimo.utils.LocalDateUtil;
import com.logistimo.utils.MsgUtil;
import com.logistimo.utils.StringUtil;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.math.BigDecimal;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

import javax.servlet.http.HttpServletRequest;

@Controller
@RequestMapping("/transactions")
public class TransactionsController {

  private static final XLog xLogger = XLog.getLog(TransactionsController.class);
  TransactionBuilder builder = new TransactionBuilder();
  ITransDao transDao =new TransDao();

  @RequestMapping("/entity/{entityId}")
  public
  @ResponseBody
  Results getEntityTransactions(
      @PathVariable Long entityId,
      @RequestParam(required = false) Long lEntityId,
      @RequestParam(required = false) String tag, @RequestParam(required = false) String from,
      @RequestParam(required = false) String to, @RequestParam(required = false) String type,
      @RequestParam(defaultValue = PageParams.DEFAULT_OFFSET_STR) int offset,
      @RequestParam(defaultValue = PageParams.DEFAULT_SIZE_STR) int size,
      @RequestParam(required = false) String bId,
      @RequestParam(required = false) boolean atd,
      @RequestParam(required = false) String reason,
      HttpServletRequest request) {
    return getAndBuildTransactions(request, from, to, offset, size, null,
        tag, type, entityId, lEntityId, null, bId, atd, reason);
  }

  /**
   * Get Transactions for material
   */
  @RequestMapping(value = "/material/{materialId}", method = RequestMethod.GET)
  public
  @ResponseBody
  Results getMaterialTransactions(
      @PathVariable Long materialId,
      @RequestParam(required = false) String ktag,
      @RequestParam(required = false) String from, @RequestParam(required = false) String to,
      @RequestParam(required = false) String type,
      @RequestParam(defaultValue = PageParams.DEFAULT_OFFSET_STR) int offset,
      @RequestParam(defaultValue = PageParams.DEFAULT_SIZE_STR) int size,
      @RequestParam(required = false) String bId,
      @RequestParam(required = false) boolean atd,
      @RequestParam(required = false) String reason,
      HttpServletRequest request) {
    return getAndBuildTransactions(request, from, to, offset, size, ktag,
        null, type, null, null, materialId, bId, atd, reason);

  }

  /**
   * Get Transactions from the domain.
   */
  @RequestMapping("/")
  public
  @ResponseBody
  Results getTransactions(
      @RequestParam(required = false) String tag, @RequestParam(required = false) String ktag,
      @RequestParam(required = false) String from, @RequestParam(required = false) String to,
      @RequestParam(required = false) String type,
      @RequestParam(defaultValue = PageParams.DEFAULT_OFFSET_STR) int offset,
      @RequestParam(defaultValue = PageParams.DEFAULT_SIZE_STR) int size,
      @RequestParam(required = false) String bId,
      @RequestParam(required = false) boolean atd,
      @RequestParam(required = false) String reason,
      @RequestParam(required = false) Long eid,
      @RequestParam(required = false) Long mid,
      @RequestParam(required = false) Long lEntityId,
      HttpServletRequest request) {
    return getAndBuildTransactions(request, from, to, offset, size, ktag, tag, type, eid, lEntityId,
        mid, bId, atd, reason);
  }

  @SuppressWarnings("unchecked")
  private Results getAndBuildTransactions(HttpServletRequest request,
                                          String from, String to, int offset, int size, String ktag,
                                          String mtag, String type, Long entityId, Long lEntityId,
                                          Long materialId, String bId, boolean atd, String reason) {
    SecureUserDetails user = SecurityUtils.getUserDetails(request);
    Locale locale = user.getLocale();
    Long domainId = SessionMgr.getCurrentDomain(request.getSession(), user.getUsername());
    DomainConfig dc = DomainConfig.getInstance(domainId);
    ResourceBundle backendMessages = Resources.get().getBundle("BackendMessages", locale);
    try {
      Date startDate = null, endDate = null;
      if (from != null && !from.isEmpty()) {
        try {
          startDate = LocalDateUtil.parseCustom(from, Constants.DATE_FORMAT, dc.getTimezone());
        } catch (Exception e) {
          xLogger.warn("Exception when parsing start date " + from, e);
        }
      }
      if (to != null && !to.isEmpty()) {
        try {
          endDate = LocalDateUtil.parseCustom(to, Constants.DATE_FORMAT, dc.getTimezone());
        } catch (Exception e) {
          xLogger.warn("Exception when parsing start date " + to, e);
        }
      }
      Navigator
          navigator =
          new Navigator(request.getSession(), "TransactionsController.getAndBuildTransactions",
              offset, size, "dummy", 0);
      PageParams pageParams = new PageParams(navigator.getCursor(offset), offset, size);
      InventoryManagementService
          ims =
          Services.getService(InventoryManagementServiceImpl.class, user.getLocale());
      EntitiesService
          accountsService =
          Services.getService(EntitiesServiceImpl.class, user.getLocale());
      Results trnResults;
      List<Long> kioskIds = null;
      if (SecurityConstants.ROLE_SERVICEMANAGER.equals(user.getRole())) {
        kioskIds =
            accountsService.getKioskIdsForUser(user.getUsername(), null, null)
                .getResults(); // TODO pagination ?
        if (kioskIds == null || kioskIds.isEmpty()) {
          return new Results(null, null, 0, offset);
        }
      }
      trnResults =
          ims.getInventoryTransactions(startDate, endDate,
              SessionMgr.getCurrentDomain(request.getSession(),
                  user.getUsername()), entityId, materialId, type, lEntityId, ktag, mtag, kioskIds,
              pageParams, bId, atd, reason);
      trnResults.setOffset(offset);
      return builder.buildTransactions(trnResults, user, SecurityUtils.getDomainId(request));
    } catch (ServiceException e) {
      xLogger.severe("Error in fetching transactions : {0}", e);
      throw new InvalidServiceException(backendMessages.getString("transactions.fetch.error"));
    }
  }

  @RequestMapping(value = "/undo", method = RequestMethod.POST)
  public
  @ResponseBody
  String undoTransactions(@RequestBody String[] tids, HttpServletRequest request) {
    SecureUserDetails sUser = SecurityMgr.getUserDetails(request.getSession());
    Locale locale;
    if (sUser != null) {
      locale = sUser.getLocale();
    } else {
      String country = request.getParameter("country");
      if (country == null) {
        country = Constants.COUNTRY_DEFAULT;
      }
      String language = request.getParameter("language");
      if (language == null) {
        language = Constants.LANG_DEFAULT;
      }
      locale = new Locale(language, country);
    }
    ResourceBundle backendMessages = Resources.get().getBundle("BackendMessages", locale);
    if (tids == null || tids.length == 0) {
      throw new BadRequestException(backendMessages.getString("transactions.undo.error"));
    }
    List<String> tidsL = StringUtil.getList(tids);
    try {
      InventoryManagementService
          ims =
          Services.getService(InventoryManagementServiceImpl.class, locale);
      List<ITransaction> errorList = ims.undoTransactions(tidsL);
      int errors = errorList.size();
      int successes = tidsL.size() - errors;
      if (successes > 0 && errors == 0) {
        return successes + " " + backendMessages.getString("transactions.undo.success");
      } else {
        return backendMessages.getString("partial.success") + "." + MsgUtil.newLine() +
            successes + " " + backendMessages.getString("transactions.undo.success") +
            MsgUtil.newLine() + errors + " " + backendMessages.getString("transactions.undo.fail");
      }
    } catch (ServiceException e) {
      xLogger.severe("Error in undo transactions: {0}", e);
      throw new InvalidServiceException(backendMessages.getString("transactions.undo.error"));
    }
  }

  @RequestMapping(value = "/transconfig/", method = RequestMethod.GET)
  public
  @ResponseBody
  TransactionDomainConfigModel getTransactionConfig(@RequestParam Long kioskId,
                                                    HttpServletRequest request) {
    SecureUserDetails sUser = SecurityMgr.getUserDetails(request.getSession());
    String userId = sUser.getUsername();
    Long domainId = SessionMgr.getCurrentDomain(request.getSession(), userId);
    String role = sUser.getRole();
    DomainConfig dc = DomainConfig.getInstance(domainId);
    InventoryConfig ic = dc.getInventoryConfig();
    TransactionDomainConfigModel model = new TransactionDomainConfigModel();
    Map<String, String> reasons = ic.getTransReasons();
    model.showCInv = ic.getPermissions() != null && ic.getPermissions().invCustomersVisible;
    Map<String, List<String>> fReasons = new HashMap<String, List<String>>(reasons.size());
    for (String key : reasons.keySet()) {
      String reasonCSV;
      if (ITransaction.TYPE_WASTAGE.equals(key)) {
        reasonCSV = dc.getWastageReasons();
      } else {
        reasonCSV = reasons.get(key);
      }
      if (reasonCSV != null) {
        fReasons.put(key, new ArrayList<>(
            new LinkedHashSet<>(Arrays.asList(reasonCSV.split(CharacterConstants.COMMA)))));
      }
    }
    model.reasons = fReasons;
    EntitiesService as = null;
    try {
      as = Services.getService(EntitiesServiceImpl.class);
      UsersService us = Services.getService(UsersServiceImpl.class);
      List
          cust =
          as.getKioskLinks(kioskId, IKioskLink.TYPE_CUSTOMER, null, null, null).getResults();
      model.customers = constructKioskMap(as, cust);
      List vend = as.getKioskLinks(kioskId, IKioskLink.TYPE_VENDOR, null, null, null).getResults();
      model.vendors = constructKioskMap(as, vend);
      model.isMan = SecurityConstants.ROLE_SERVICEMANAGER.equals(role);
//            if (!model.isMan) {
      IUserAccount u = us.getUserAccount(userId);
      List dest = as.getKiosks(u, domainId, null, null, null).getResults();
      model.dest = constructKioskMap(as, dest);
//            }
      ActualTransConfig atci = ic.getActualTransConfigByType(ITransaction.TYPE_ISSUE);
      model.atdi = atci != null ? atci.getTy() : ActualTransConfig.ACTUAL_NONE;

      ActualTransConfig atcr = ic.getActualTransConfigByType(ITransaction.TYPE_RECEIPT);
      model.atdr = atcr != null ? atcr.getTy() : ActualTransConfig.ACTUAL_NONE;

      ActualTransConfig atcp = ic.getActualTransConfigByType(ITransaction.TYPE_PHYSICALCOUNT);
      model.atdp = atcp != null ? atcp.getTy() : ActualTransConfig.ACTUAL_NONE;

      ActualTransConfig atcw = ic.getActualTransConfigByType(ITransaction.TYPE_WASTAGE);
      model.atdw = atcw != null ? atcw.getTy() : ActualTransConfig.ACTUAL_NONE;

      ActualTransConfig atct = ic.getActualTransConfigByType(ITransaction.TYPE_TRANSFER);
      model.atdt = atct != null ? atct.getTy() : ActualTransConfig.ACTUAL_NONE;

      PageParams pageParams = new PageParams(1);
      model.noc =
          as.getKioskLinks(kioskId, IKioskLink.TYPE_CUSTOMER, null, null, pageParams).getNumFound();
      model.nov =
          as.getKioskLinks(kioskId, IKioskLink.TYPE_VENDOR, null, null, pageParams).getNumFound();
    } catch (ServiceException | ObjectNotFoundException e) {
      xLogger.severe("Error in getting Transaction Domain Config: {0}", e);
    }


    return model;
  }

  private List<TransactionModel> constructKioskMap(EntitiesService as, List cust)
      throws ServiceException {
    List<TransactionModel> kioskList = new ArrayList<>(cust.size());
    for (Object o : cust) {
      IKiosk kiosk = null;
      if (o instanceof IKioskLink) {
        try {
          kiosk = as.getKiosk(((IKioskLink) o).getLinkedKioskId(), false);
        } catch (Exception e) {
          xLogger.warn("Kiosk not found: {0}", ((IKioskLink) o).getLinkedKioskId());
          continue;
        }
      } else if (o instanceof IKiosk) {
        kiosk = (IKiosk) o;
      }
      kioskList.add(constructKioskTModel(kiosk));
    }
    return kioskList;
  }

  @RequestMapping(value = "/reasons", method = RequestMethod.GET)
  public
  @ResponseBody
  List<String> getReasons(@RequestParam String type, String[] tags, HttpServletRequest request) {
    SecureUserDetails sUser = SecurityMgr.getUserDetails(request.getSession());
    String userId = sUser.getUsername();
    Long domainId = SessionMgr.getCurrentDomain(request.getSession(), userId);
    DomainConfig dc = DomainConfig.getInstance(domainId);
    InventoryConfig ic = dc.getInventoryConfig();
    StringBuilder reasons = new StringBuilder();
    List<String> reasonList = null;
    if (tags != null) {
      switch (type) {
        case ITransaction.TYPE_ISSUE:
          for (String mtag : tags) {
            if (ic.getImTransReason(mtag) != null) {
              reasons.append(ic.getImTransReason(mtag)).append(CharacterConstants.COMMA);
            }
          }
          break;
        case ITransaction.TYPE_RECEIPT:
          for (String mtag : tags) {
            if (ic.getRmTransReason(mtag) != null) {
              reasons.append(ic.getRmTransReason(mtag)).append(CharacterConstants.COMMA);
            }
          }
          break;
        case ITransaction.TYPE_TRANSFER:
          for (String mtag : tags) {
            if (ic.getTmTransReason(mtag) != null) {
              reasons.append(ic.getTmTransReason(mtag)).append(CharacterConstants.COMMA);
            }
          }
          break;
        case ITransaction.TYPE_PHYSICALCOUNT:
          for (String mtag : tags) {
            if (ic.getSmTransReason(mtag) != null) {
              reasons.append(ic.getSmTransReason(mtag)).append(CharacterConstants.COMMA);
            }
          }
          break;
        case ITransaction.TYPE_WASTAGE:
          for (String mtag : tags) {
            if (ic.getDmTransReason(mtag) != null) {
              reasons.append(ic.getDmTransReason(mtag)).append(CharacterConstants.COMMA);
            }
          }
          break;
      }
    }
    if (reasons.length() > 0) {
      reasonList =
          new ArrayList<>(new LinkedHashSet<>(
              Arrays.asList(reasons.toString().split(CharacterConstants.COMMA))));
    }
    return reasonList;
  }

  @RequestMapping(value = "/matStatus", method = RequestMethod.GET)
  public
  @ResponseBody
  List<String> getMatStatus(@RequestParam String type, Boolean ts, HttpServletRequest request) {
    SecureUserDetails sUser = SecurityMgr.getUserDetails(request.getSession());
    String userId = sUser.getUsername();
    Long domainId = SessionMgr.getCurrentDomain(request.getSession(), userId);
    DomainConfig dc = DomainConfig.getInstance(domainId);
    InventoryConfig ic = dc.getInventoryConfig();
    List<String> statusList = null;
    if (type != null && !type.isEmpty()) {
      MatStatusConfig ms = ic.getMatStatusConfigByType(type);
      if (ms != null) {
        if (ts && ms.getEtsm() != null && !ms.getEtsm().isEmpty()) {
          statusList =
              new ArrayList<>(
                  new LinkedHashSet<>(Arrays.asList(ms.getEtsm().split(CharacterConstants.COMMA))));
        } else if (ms.getDf() != null && !ms.getDf().isEmpty()) {
          statusList =
              new ArrayList<>(
                  new LinkedHashSet<>(Arrays.asList(ms.getDf().split(CharacterConstants.COMMA))));
        }
      }
    }
    if (statusList != null && !statusList.get(0).isEmpty()) {
      statusList.add(0, CharacterConstants.EMPTY);
    }
    return statusList;
  }

  private TransactionModel constructKioskTModel(IKiosk kiosk) {
    TransactionModel m = new TransactionModel();
    m.eid = kiosk.getKioskId();
    m.enm = kiosk.getName();
    m.st = kiosk.getState();
    m.ds = kiosk.getDistrict();
    m.ct = kiosk.getCity();
    return m;
  }


  @RequestMapping(value = "/add/", method = RequestMethod.POST)
  public
  @ResponseBody
  String addTransaction(@RequestBody Map<String, Object> transaction, HttpServletRequest request) {
    SecureUserDetails sUser = SecurityMgr.getUserDetails(request.getSession());
    Locale locale;
    if (sUser.getLocale() != null) {
      locale = sUser.getLocale();
    } else {
      locale = new Locale(Constants.LANG_DEFAULT, Constants.COUNTRY_DEFAULT);
    }
    ResourceBundle backendMessages = Resources.get().getBundle("BackendMessages", locale);
    String userId = sUser.getUsername();
    Long domainId = SessionMgr.getCurrentDomain(request.getSession(), userId);

    MemcacheService cache = null;
    String signature =
        transaction.get("signature") != null ? String.valueOf(transaction.get("signature")) : null;
    if (signature != null) {
      cache = AppFactory.get().getMemcacheService();
      if (cache != null) {
        // Check if the signature exists in cache
        Integer lastStatus = (Integer) cache.get(signature);
        if (lastStatus != null) {
          switch (lastStatus) {
            case DedupUtil.SUCCESS:
              return backendMessages.getString("transactions.create.success");
            case DedupUtil.PENDING:
              return backendMessages.getString("transaction.verify.message");
            case DedupUtil.FAILED:
              DedupUtil.setSignatureAndStatus(cache, signature, DedupUtil.PENDING);
              break;
            default:
              break;
          }
        } else {
          DedupUtil.setSignatureAndStatus(cache, signature, DedupUtil.PENDING);
        }
      }
    }

    String transType = String.valueOf(transaction.get("transtype"));
    DomainConfig dc = DomainConfig.getInstance(domainId);
    InventoryConfig ic = dc.getInventoryConfig();
    String
        atdStr =
        ic.getActualTransConfigByType(transType) != null ? ic.getActualTransConfigByType(transType)
            .getTy() : null;
    boolean transMandate = "2".equals(atdStr);
    Long kioskId = Long.parseLong((String) transaction.get("kioskid"));
    Long linkedKioskId = null;
    if (transaction.containsKey("lkioskid")) {
      linkedKioskId = Long.parseLong(String.valueOf(transaction.get("lkioskid")));
    }
    boolean checkBatchMgmt = ITransaction.TYPE_TRANSFER.equals(transType);
    //String reason = (String) transaction.get("reason");
    EntitiesService as;
    try {
      MaterialCatalogServiceImpl mcs = null;
      IKiosk kiosk = null;
      IKiosk destKiosk = null;
      if (checkBatchMgmt) {
        as = Services.getService(EntitiesServiceImpl.class, locale);
        kiosk = as.getKiosk(kioskId);
        destKiosk = as.getKiosk(linkedKioskId);
        checkBatchMgmt = !kiosk.isBatchMgmtEnabled() && destKiosk.isBatchMgmtEnabled();
        mcs = Services.getService(MaterialCatalogServiceImpl.class, locale);
      }
      List<ITransaction> transList = new ArrayList<ITransaction>();
      InventoryManagementService
          ims =
          Services.getService(InventoryManagementServiceImpl.class, locale);
      Date now = new Date();
      Date actualTransDate = null;
      if (transMandate && !transaction.containsKey("transactual")) {
        return backendMessages.getString("error.adt.entry.mandate");
      }
      if (transaction.containsKey("transactual") && !"0".equals(atdStr)) {
        actualTransDate =
            LocalDateUtil
                .parseCustom(String.valueOf(transaction.get("transactual")), Constants.DATE_FORMAT,
                    null);
      }
      LinkedTreeMap materials = (LinkedTreeMap) transaction.get("materials");
      List<String> berrorMaterials = new ArrayList<>(1);
      for (Object m : materials.keySet()) {
        Long materialId = Long.parseLong(String.valueOf(m));
        LinkedTreeMap mat = (LinkedTreeMap) materials.get(m);
        BigDecimal quantity = new BigDecimal(Long.parseLong(String.valueOf(mat.get("q"))));
        String reason = String.valueOf(mat.get("r"));
        if (reason.equals("null")) {
          reason = "";
        }
        String status = String.valueOf(mat.get("mst"));
        if (status.equals("null")) {
          status = "";
        }
        // float quantity = Long.parseLong(String.valueOf(materials.get(m)));
        if (checkBatchMgmt) {
          IMaterial material = mcs.getMaterial(materialId);
          if (material.isBatchEnabled()) {
            berrorMaterials.add(material.getName());
          }
        }
        ITransaction
            trans =
            getTransaction(userId, domainId, transType, kioskId, linkedKioskId, reason, status, now,
                materialId, quantity, "", actualTransDate);
        transList.add(trans);
      }
      if (!berrorMaterials.isEmpty()) {
        xLogger.info(
            "Transfer rejected from {0} to {1}, Source is batch disabled but destination is enabled.",
            kiosk.getName(), destKiosk.getName());
        StringBuilder builder = new StringBuilder();
        builder.append(backendMessages.getString("transactions.restricted.error.1"))
            .append(" ")
            .append(berrorMaterials.size())
            .append(" ")
            .append(backendMessages.getString("materials"))
            .append(" ")
            .append(backendMessages.getString("from"))
            .append(" ")
            .append(kiosk.getName())
            .append(" ")
            .append(backendMessages.getString("transactions.restricted.error.2"))
            .append(MsgUtil.newLine())
            .append(StringUtil.getCSV(berrorMaterials));
        throw new BadRequestException(builder.toString());
      }
      LinkedTreeMap batchMaterials = (LinkedTreeMap) transaction.get("bmaterials");
      for (Object m : batchMaterials.keySet()) {
        String keys[] = String.valueOf(m).split("\t");
        Long materialId = Long.parseLong(keys[0]);
        String batch = keys[1];
        LinkedTreeMap batchMaterial = (LinkedTreeMap) batchMaterials.get(m);
        BigDecimal
            quantity =
            new BigDecimal(Long.parseLong(String.valueOf(batchMaterial.get("q"))));
        String expiry = String.valueOf(batchMaterial.get("e"));
        String manufacturer = String.valueOf(batchMaterial.get("mr"));
        String manufactured = String.valueOf(batchMaterial.get("md"));
        String reason = String.valueOf(batchMaterial.get("r"));
        if (reason.equals("null")) {
          reason = "";
        }
        String status = String.valueOf(batchMaterial.get("mst"));
        if (status.equals("null")) {
          status = "";
        }
        if (manufactured.equals("null")) {
          manufactured = "";
        }
        ITransaction
            trans =
            getTransaction(userId, domainId, transType, kioskId, linkedKioskId, reason, status, now,
                materialId, quantity, batch, actualTransDate);
        TransactionUtil.setBatchData(trans, batch, expiry, manufacturer, manufactured);
        transList.add(trans);
      }

      List<ITransaction> errors = ims.updateInventoryTransactions(domainId, transList, true);
      if (errors != null && errors.size() > 0) {
        StringBuilder errorMsg = new StringBuilder();
        for (ITransaction error : errors) {
          errorMsg.append("-").append(error.getMessage()).append(MsgUtil.newLine());
        }
        return backendMessages.getString("errors.oneormore") + ":" +
            MsgUtil.newLine() + errorMsg + MsgUtil.newLine() + MsgUtil.newLine() +
            backendMessages.getString("transactions.resubmit");
      }
      DedupUtil.setSignatureAndStatus(cache, signature, DedupUtil.SUCCESS);
    } catch (ServiceException e) {
      xLogger.severe("Error in creating transaction: {0}", e);
      DedupUtil.setSignatureAndStatus(cache, signature, DedupUtil.FAILED);
      throw new InvalidServiceException(backendMessages.getString("transactions.create.error"));
    } catch (DuplicationException | ParseException e) {
      xLogger.warn("Error in creating transaction: {0}", e);
      DedupUtil.setSignatureAndStatus(cache, signature, DedupUtil.FAILED);
      throw new InvalidServiceException(
          backendMessages.getString("transactions.create.error") + ". " + e.getMessage());
    }

    return backendMessages.getString("transactions.create.success");
  }

  private ITransaction getTransaction(String userId, Long domainId, String transType, Long kioskId,
                                      Long linkedKioskId, String reason, String matStatus, Date now,
                                      Long materialId, BigDecimal quantity, String batch,
                                      Date actualTransDate) {
    ITransaction trans = JDOUtils.createInstance(ITransaction.class);
    trans.setDomainId(domainId);
    trans.setKioskId(kioskId);
    trans.setMaterialId(materialId);
    trans.setQuantity(quantity);
    trans.setType(transType);
    trans.setSourceUserId(userId);
    trans.setTimestamp(now);
    trans.setReason(reason);
    trans.setSrc(SourceConstants.WEB);
    trans.setBatchId(batch);
    trans.setMaterialStatus(matStatus);
    trans.setAtd(actualTransDate);
    if (linkedKioskId != null) {
      trans.setLinkedKioskId(linkedKioskId);
    }
    transDao.setKey(trans);
    return trans;
  }

  @RequestMapping(value = "/actualroute", method = RequestMethod.GET)
  public
  @ResponseBody
  List<MarkerModel> getActualRoute(@RequestParam String userId, @RequestParam String from,
                                   @RequestParam String to, HttpServletRequest request) {
    try {
      SecureUserDetails sUser = SecurityMgr.getUserDetails(request.getSession());
      Long domainId = SessionMgr.getCurrentDomain(request.getSession(), sUser.getUsername());
      DomainConfig dc = DomainConfig.getInstance(domainId);
      InventoryManagementService
          ims =
          Services.getService(InventoryManagementServiceImpl.class);
      Results
          results =
          ims.getInventoryTransactionsByUser(userId,
              LocalDateUtil.parseCustom(from, Constants.DATE_FORMAT_CSV, dc.getTimezone()),
              LocalDateUtil.parseCustom(to, Constants.DATE_FORMAT_CSV, dc.getTimezone()), null);
      return new MarkerBuilder()
          .buildMarkerListFromTransactions(results.getResults(), sUser.getLocale(),
              sUser.getTimezone());
    } catch (ServiceException | ParseException e) {
      xLogger.severe("Error in reading destination inventories: {0}", e);
    }
    return null;
  }

  @RequestMapping(value = "/checkpermission", method = RequestMethod.GET)
  public
  @ResponseBody
  Integer checkPermission(@RequestParam String userId, @RequestParam Long kioskId,
                          HttpServletRequest request) {
    Integer permission = 0;
    try {
      SecureUserDetails sUser = SecurityMgr.getUserDetails(request.getSession());
      Locale locale = sUser.getLocale();
      UsersService as = Services.getService(UsersServiceImpl.class, locale);
      IUserAccount userAccount = as.getUserAccount(userId);
      permission =
          EntityAuthoriser
              .authoriseEntityPerm(kioskId, userAccount.getRole(), userAccount.getLocale(), userId,
                  userAccount.getDomainId());
    } catch (ServiceException | ObjectNotFoundException e) {
      xLogger.severe("Error in reading user details : {0}", userId);
    }
    return permission;
  }
  @RequestMapping(value = "/statusmandatory", method = RequestMethod.GET)
  public
  @ResponseBody
  Map<String,Boolean> getStatusMandatory(HttpServletRequest request) {
    Map<String,Boolean> statusList = new HashMap<>(5);
    SecureUserDetails sUser = SecurityMgr.getUserDetails(request.getSession());
    String userId = sUser.getUsername();
    Long domainId = SessionMgr.getCurrentDomain(request.getSession(), userId);
    DomainConfig dc = DomainConfig.getInstance(domainId);
    InventoryConfig ic = dc.getInventoryConfig();
    MatStatusConfig msc = ic.getMatStatusConfigByType(ITransaction.TYPE_ISSUE);
    if(msc != null) {
      statusList.put("ism", msc.isStatusMandatory());
    }
    msc = ic.getMatStatusConfigByType(ITransaction.TYPE_RECEIPT);
    if(msc != null) {
      statusList.put("rsm", msc.isStatusMandatory());
    }
    msc = ic.getMatStatusConfigByType(ITransaction.TYPE_PHYSICALCOUNT);
    if(msc != null) {
      statusList.put("psm", msc.isStatusMandatory());
    }
    msc = ic.getMatStatusConfigByType(ITransaction.TYPE_WASTAGE);
    if(msc != null) {
      statusList.put("wsm", msc.isStatusMandatory());
    }
    msc = ic.getMatStatusConfigByType(ITransaction.TYPE_TRANSFER);
    if(msc != null) {
      statusList.put("tsm", msc.isStatusMandatory());
    }
    return statusList;
  }
}

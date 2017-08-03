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

import com.google.gson.Gson;

import com.logistimo.api.builders.FChartBuilder;
import com.logistimo.api.builders.InventoryBuilder;
import com.logistimo.api.builders.MarkerBuilder;
import com.logistimo.api.models.FChartModel;
import com.logistimo.api.models.InventoryAbnStockModel;
import com.logistimo.api.models.InventoryBatchMaterialModel;
import com.logistimo.api.models.InventoryDetailModel;
import com.logistimo.api.models.InventoryDomainModel;
import com.logistimo.api.models.InventoryMinMaxLogModel;
import com.logistimo.api.models.InventoryModel;
import com.logistimo.api.models.InvntryBatchModel;
import com.logistimo.api.models.MarkerModel;
import com.logistimo.assets.service.AssetManagementService;
import com.logistimo.assets.service.impl.AssetManagementServiceImpl;
import com.logistimo.auth.SecurityConstants;
import com.logistimo.auth.SecurityMgr;
import com.logistimo.auth.utils.SecurityUtils;
import com.logistimo.auth.utils.SessionMgr;
import com.logistimo.config.models.DomainConfig;
import com.logistimo.constants.Constants;
import com.logistimo.entities.auth.EntityAuthoriser;
import com.logistimo.entities.entity.IKiosk;
import com.logistimo.entities.models.LocationSuggestionModel;
import com.logistimo.entities.service.EntitiesService;
import com.logistimo.entities.service.EntitiesServiceImpl;
import com.logistimo.exception.InvalidServiceException;
import com.logistimo.exception.UnauthorizedException;
import com.logistimo.inventory.entity.IInventoryMinMaxLog;
import com.logistimo.inventory.entity.IInvntry;
import com.logistimo.inventory.entity.IInvntryBatch;
import com.logistimo.inventory.predictions.service.PredictionService;
import com.logistimo.inventory.predictions.utils.PredictiveUtil;
import com.logistimo.inventory.service.InventoryManagementService;
import com.logistimo.inventory.service.impl.InventoryManagementServiceImpl;
import com.logistimo.logger.XLog;
import com.logistimo.materials.service.MaterialCatalogService;
import com.logistimo.materials.service.impl.MaterialCatalogServiceImpl;
import com.logistimo.orders.service.OrderManagementService;
import com.logistimo.orders.service.impl.OrderManagementServiceImpl;
import com.logistimo.pagination.Navigator;
import com.logistimo.pagination.PageParams;
import com.logistimo.pagination.Results;
import com.logistimo.reports.ReportsConstants;
import com.logistimo.reports.entity.slices.ISlice;
import com.logistimo.reports.generators.ReportData;
import com.logistimo.reports.service.ReportsService;
import com.logistimo.reports.service.impl.ReportsServiceImpl;
import com.logistimo.reports.utils.ReportsUtil;
import com.logistimo.security.SecureUserDetails;
import com.logistimo.services.ObjectNotFoundException;
import com.logistimo.services.Resources;
import com.logistimo.services.ServiceException;
import com.logistimo.services.Services;
import com.logistimo.services.utils.ConfigUtil;
import com.logistimo.users.entity.IUserAccount;
import com.logistimo.users.service.impl.UsersServiceImpl;
import com.logistimo.utils.Counter;
import com.logistimo.utils.LocalDateUtil;

import org.apache.commons.lang.BooleanUtils;
import org.apache.commons.lang.StringUtils;
import org.json.JSONException;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

@Controller
@RequestMapping("/inventory")
public class InventoryController {

  public static final String CURSOR_STOCK_EVENTS = "cursorstockevents";
  private static final XLog xLogger = XLog.getLog(InventoryController.class);
  private static final int
      PREDICTIVE_HISTORY_DAYS =
      ConfigUtil.getInt("predictive.history.days", 30);
  private static final String ALL = "0";

  InventoryBuilder builder = new InventoryBuilder();
  FChartBuilder fcBuilder = new FChartBuilder();

  @RequestMapping(value = "/entity/{entityId}", method = RequestMethod.GET)
  public
  @ResponseBody
  Results getInventory(@PathVariable Long entityId, @RequestParam(required = false) String tag,
                       @RequestParam(defaultValue = PageParams.DEFAULT_OFFSET_STR) int offset,
                       @RequestParam(defaultValue = PageParams.DEFAULT_SIZE_STR) int size,
                       @RequestParam(required = false) Integer abType,
                       @RequestParam(required = false) String startsWith,
                       @RequestParam(required = false) String fetchTemp,
                       @RequestParam(defaultValue = ALL) int matType,
                       @RequestParam(required = false) boolean onlyNZStk,
                       @RequestParam(required = false) String pdos,
                       HttpServletRequest request) {
    SecureUserDetails sUser = SecurityUtils.getUserDetails();
    Locale locale = sUser.getLocale();
    ResourceBundle backendMessages = Resources.get().getBundle("BackendMessages", locale);
    String userId = sUser.getUsername();
    Long domainId = SecurityUtils.getCurrentDomainId();
    DomainConfig dc = DomainConfig.getInstance(domainId);
    String timezone = dc.getTimezone();
    try {
      if (!EntityAuthoriser.authoriseInventoryAccess(sUser, entityId)) {
        throw new UnauthorizedException(backendMessages.getString("permission.denied"));
      }
      int numTotalInv = Counter.getMaterialCounter(domainId, entityId, tag).getCount();
      Navigator navigator;
      if (startsWith == null) {
        navigator =
            new Navigator(request.getSession(), "InventoryController.getInventory", offset, size,
                "base/test", numTotalInv);
      } else {
        navigator =
            new Navigator(request.getSession(), "InventoryController.getInventoryStartsWith",
                offset, size, "base/test", numTotalInv);
      }
      PageParams pageParams = new PageParams(navigator.getCursor(offset), offset, size);
      InventoryManagementService
          ims =
          Services.getService(InventoryManagementServiceImpl.class, sUser.getLocale());

      Results results;
      if (startsWith == null) {
        if (abType != null) {

          Map<String, Object> filters = new HashMap<>();
          filters.put(ReportsConstants.FILTER_DOMAIN, domainId);
          filters.put(ReportsConstants.FILTER_EVENT, abType);

          filters.put(ReportsConstants.FILTER_MATERIALTAG, tag);

          filters.put(ReportsConstants.FILTER_KIOSK, entityId);
          filters.put(ReportsConstants.FILTER_LATEST, true);

          ReportsService reportsService = Services.getService(ReportsServiceImpl.class);
          ReportData
              reportData =
              reportsService
                  .getReportData(ReportsConstants.TYPE_STOCKEVENT, null, null,
                      ReportsConstants.FREQ_DAILY, filters,
                      locale, timezone, pageParams, dc, userId);
          results = new Results(reportData.getResults(), null);
        } else {
          results =
              ims.getInventory(domainId, entityId, null, null, null, null, tag, matType, onlyNZStk, pdos,
                  null, pageParams);
        }
      } else {
        results = ims.searchKioskInventory(entityId, tag, startsWith, pageParams);
        results.setNumFound(-1);
      }
      navigator.setResultParams(results);
      results.setOffset(offset);
      Results res = builder.buildInventoryModelListAsResult(results, sUser, domainId, entityId);
      if ("true".equals(fetchTemp) && res.getSize() > 0) {
        AssetManagementService ams = Services.getService(AssetManagementServiceImpl.class,locale);
        ((InventoryModel) res.getResults().get(0)).assets = ams.getTemperatureStatus(entityId);
      }
      return res;
    } catch (ServiceException e) {
      xLogger.severe("Error in fetching inventory details: {0}", e);
      throw new InvalidServiceException("");
    }
  }

  @RequestMapping(value = "/domain/{entityId}", method = RequestMethod.GET)
  public
  @ResponseBody
  InventoryDomainModel getEntityInventoryDomainConfig(@PathVariable Long entityId,
                                                      HttpServletRequest request) {
    SecureUserDetails sUser = SecurityUtils.getUserDetails();
    String userId = sUser.getUsername();
    Locale locale = sUser.getLocale();
    try {
      EntitiesService as = Services.getService(EntitiesServiceImpl.class, locale);
      IKiosk kiosk = as.getKiosk(entityId, false);
      return builder.buildInventoryDomainModel(request, userId, locale, kiosk);
    } catch (ServiceException e) {
      throw new InvalidServiceException("");
    }
  }

  @RequestMapping(value = "/domain/", method = RequestMethod.GET)
  public
  @ResponseBody
  InventoryDomainModel getInventoryDomainConfig(HttpServletRequest request) {
    SecureUserDetails sUser = SecurityMgr.getUserDetails(request.getSession());
    String userId = sUser.getUsername();
    Locale locale = sUser.getLocale();
    return builder.buildInventoryDomainModel(request, userId, locale, null);
  }

  private LocationSuggestionModel parseLocation(String loc) {
    try {
      if (loc != null) {
        return new Gson().fromJson(loc,LocationSuggestionModel.class);
      }
    } catch (JSONException e) {
      xLogger.warn("Error in parsing location filter object", e);
    }
    return null;
  }

  @RequestMapping(value = "/material/{materialId}", method = RequestMethod.GET)
  public
  @ResponseBody
  Results getInventoryByMaterial(@PathVariable Long materialId,
                                 @RequestParam(required = false) String etag,
                                 @RequestParam(required = false) String eetag,
                                 @RequestParam(defaultValue = PageParams.DEFAULT_OFFSET_STR) int offset,
                                 @RequestParam(defaultValue = PageParams.DEFAULT_SIZE_STR) int size,
                                 @RequestParam(defaultValue = ALL) int matType,
                                 @RequestParam(required = false) Integer abType,
                                 @RequestParam(required = false) boolean onlyNZStk,
                                 @RequestParam(required = false) String loc,
                                 @RequestParam(required = false) String pdos,
                                 HttpServletRequest request) {
    SecureUserDetails sUser = SecurityUtils.getUserDetails();
    String userId = sUser.getUsername();
    Long domainId = SecurityUtils.getCurrentDomainId();
    DomainConfig dc = DomainConfig.getInstance(domainId);
    String timezone = dc.getTimezone();
    Locale locale = sUser.getLocale();
    int numTotalInv = -1;
    LocationSuggestionModel location = parseLocation(loc);
    Navigator
        navigator =
        new Navigator(request.getSession(), "InventoryController.getInventoryByMaterial", offset,
            size, "base/test", numTotalInv);
    PageParams pageParams = new PageParams(navigator.getCursor(offset), offset, size);
    try {
      InventoryManagementService
          ims =
          Services.getService(InventoryManagementServiceImpl.class, sUser.getLocale());
      List<Long> kioskIds = null;
      if (SecurityConstants.ROLE_SERVICEMANAGER.equals(sUser.getRole())) {
        EntitiesService
            as =
            Services.getService(EntitiesServiceImpl.class, sUser.getLocale());
        kioskIds = as.getKioskIdsForUser(userId, null, pageParams).getResults();
        if (kioskIds == null || kioskIds.isEmpty()) {
          return new Results(null, null, 0, offset);
        }
        if (kioskIds.size() > Constants.MAX_LIST_SIZE_FOR_CONTAINS_QUERY) {
          kioskIds =
              kioskIds.subList(0,
                  Constants.MAX_LIST_SIZE_FOR_CONTAINS_QUERY); // TODO: currently restricting this view to 30 kiosks, given GAE limit on the number within a contains list
        }
      }
      Results results;
      if (abType != null) {

        Map<String, Object> filters = new HashMap<>();
        filters.put(ReportsConstants.FILTER_DOMAIN, domainId);
        filters.put(ReportsConstants.FILTER_EVENT, abType);
        filters.put(ReportsConstants.FILTER_KIOSKTAG, etag);
        filters.put(ReportsConstants.FILTER_EXCLUDED_KIOSKTAG, eetag);
        filters.put(ReportsConstants.FILTER_MATERIAL, materialId);
        filters.put(ReportsConstants.FILTER_LATEST, true);

        ReportsService reportsService = Services.getService(ReportsServiceImpl.class);
        ReportData
            reportData =
            reportsService
                .getReportData(ReportsConstants.TYPE_STOCKEVENT, null, null,
                    ReportsConstants.FREQ_DAILY, filters,
                    locale, timezone, pageParams, dc, userId);
        results = new Results(reportData.getResults(), null);
      }else {
          results =
            ims.getInventory(domainId, null, kioskIds, etag, eetag, materialId, null, matType, onlyNZStk,
                pdos, location, pageParams);
      }
      results.setOffset(offset);
      return builder.buildInventoryModelListAsResult(results, sUser, domainId, null);
    } catch (ServiceException e) {
      throw new InvalidServiceException("");
    }
  }

  @RequestMapping(value = "/batchmaterial/", method = RequestMethod.GET)
  public
  @ResponseBody
  Results getBatchMaterial(
      @RequestParam(required = false) String etag,
      @RequestParam(required = false) String eetag,
      @RequestParam(required = false) String mtag,
      @RequestParam(required = false) String ebf,
      @RequestParam(required = false) String bno,
      @RequestParam(required = false) String mid,
      @RequestParam(required = false) String loc,
      @RequestParam(defaultValue = PageParams.DEFAULT_OFFSET_STR) int offset,
      @RequestParam(defaultValue = PageParams.DEFAULT_SIZE_STR) int size,
      HttpServletRequest request) {
    boolean hasExpiresBefore = (ebf != null && !ebf.isEmpty());
    boolean hasBatchId = StringUtils.isNotEmpty(bno);
    SecureUserDetails sUser = SecurityMgr.getUserDetails(request.getSession());
    String userId = sUser.getUsername();
    Locale locale = sUser.getLocale();
    String timezone = sUser.getTimezone();
    Long domainId = SessionMgr.getCurrentDomain(request.getSession(), userId);
    LocationSuggestionModel location = parseLocation(loc);
    int total = 0;
    Navigator
        navigator =
        new Navigator(request.getSession(), "InventoryController.getBatchMaterial", offset, size,
            "batchmaterial", total);
    PageParams pageParams = new PageParams(navigator.getCursor(offset), offset, size);
    EntitiesService as;
    MaterialCatalogService mc;
    try {
      InventoryManagementService
          ims =
          Services.getService(InventoryManagementServiceImpl.class, locale);
      mc = Services.getService(MaterialCatalogServiceImpl.class, locale);
      as = Services.getService(EntitiesServiceImpl.class, locale);
      Long matId = StringUtils.isNotBlank(mid) ? Long.parseLong(mid) : null;
      Results
          results =
          getResults(ebf, hasExpiresBefore, bno, hasBatchId, matId, etag, eetag, mtag, domainId,
              pageParams, ims, location);
      if (results != null) {
        navigator.setResultParams(results);
        List<IInvntryBatch> inventory = results.getResults();
        IUserAccount user = Services.getService(UsersServiceImpl.class,locale).getUserAccount(userId);
        List<IKiosk> myKiosks = null;
        if (SecurityConstants.ROLE_SERVICEMANAGER.equals(user.getRole())) {
          myKiosks = as.getKiosks(user, domainId, null, null, null).getResults();
          if (myKiosks == null || myKiosks.isEmpty()) {
            return new Results(null, null, 0, offset);
          }
        }
        List<InventoryBatchMaterialModel>
            models =
            builder.buildInventoryBatchMaterialModels(offset, locale, timezone, as, mc, inventory,
                myKiosks);
        int numFound = models.size() > 0 ? -1 : 0;
        return new Results(models, results.getCursor(), numFound, offset);
      }
    } catch (ServiceException e) {
      xLogger.warn("Error fetching batch material details: {0} ", e);
    } catch (ObjectNotFoundException e) {
      xLogger.warn("Error fetching batch material details: {0} ", e);
    }
    return null;
  }

  private Results getResults(String ebf, boolean hasExpiresBefore, String bno, boolean hasBatchId,
                             Long mid, String kioskTags, String excludedKioskTags, String materialTags, Long domainId,
                             PageParams pageParams, InventoryManagementService ims, LocationSuggestionModel location)
      throws ServiceException {
    Date end;
    Results results = null;
    DomainConfig dc = DomainConfig.getInstance(domainId);
    if (mid != null && hasBatchId) {
      results = ims.getInventoryByBatchId(mid, bno, pageParams, domainId, kioskTags, excludedKioskTags, location);
    } else if (hasExpiresBefore) {
      try {
        end = LocalDateUtil.parseCustom(ebf, Constants.DATE_FORMAT, dc.getTimezone());
        results =
            ims.getInventoryByBatchExpiry(domainId, mid, null, end, kioskTags, excludedKioskTags, materialTags, location,
                pageParams);
      } catch (Exception e) {
        xLogger.warn("Exception when trying to parse expiry date: {0}", e);
      }
    } else {
      xLogger.warn("Incorrect input parameters. A batch ID or expiry date has to be provided");
    }
    return results;
  }

  @RequestMapping(value = "/batchmaterialbyid/", method = RequestMethod.GET)
  public
  @ResponseBody
  List<InvntryBatchModel> getBatchMaterialById(@RequestParam Long kid, @RequestParam Long mid,
                                               @RequestParam(required = false) boolean allBatch,
                                               @RequestParam(required = false) Long allocOrderId,
                                               @RequestParam(defaultValue = PageParams.DEFAULT_SIZE_STR) int size,
                                               HttpServletRequest request) {
    PageParams pageParams = new PageParams(null, size);
    try {
      SecureUserDetails sUser = SecurityMgr.getUserDetails(request.getSession());
      // RESTUtil.authenticate(uid, null, kid, request);
      InventoryManagementService
          ims =
          Services.getService(InventoryManagementServiceImpl.class);
      Results results = ims.getBatches(mid, kid, pageParams);
      return builder.buildInvntryBatchModel(results, allBatch, sUser, allocOrderId);
    } catch (ServiceException e) {
      xLogger.severe("InventoryController Exception: {0}", e.getMessage(), e);
    }
    return null;
  }

  @RequestMapping(value = "/batchmaterialcheck", method = RequestMethod.GET)
  public
  @ResponseBody
  Boolean checkBatchMaterial(@RequestParam String bid, @RequestParam Long mid,
                             @RequestParam Long kid,
                             @RequestParam(defaultValue = "false") boolean expired,
                             @RequestParam(defaultValue = PageParams.DEFAULT_SIZE_STR) int size,
                             HttpServletRequest request) {
    PageParams pageParams = new PageParams(null, size);
    SecureUserDetails sUser = SecurityMgr.getUserDetails(request.getSession());
    Long domainId = SessionMgr.getCurrentDomain(request.getSession(), sUser.getUsername());
    InventoryManagementService
        ims =
        Services.getService(InventoryManagementServiceImpl.class);
    Results results = ims.getValidBatchesByBatchId(bid, mid, kid, domainId, expired, pageParams);
    if (results != null && results.getResults().size() > 0) {
      return true;
    }

    return false;
  }

  @RequestMapping(value = "/history", method = RequestMethod.GET)
  public
  @ResponseBody
  List<InventoryAbnStockModel> getHistory(HttpServletRequest request) throws Exception {
    xLogger.fine("Entered processRequest");
    String reportType = request.getParameter("type");
    String sizeStr = request.getParameter("size");
    String frequency = ReportsConstants.FREQ_DAILY;
    int size = 0;
    if (sizeStr != null && !sizeStr.isEmpty()) {
      size = Integer.parseInt(sizeStr);
    }
    try {
      SecureUserDetails sUser = SecurityMgr.getUserDetails(request.getSession());
      String userId = sUser.getUsername();
      Locale locale = sUser.getLocale();
      String timezone = sUser.getTimezone();
      Long domainId = SessionMgr.getCurrentDomain(request.getSession(), userId);
      DomainConfig dc = null;
      if (domainId != null) {
        dc = DomainConfig.getInstance(domainId);
      }
      if (locale == null || timezone == null && dc != null) {
        if (locale == null) {
          locale = dc.getLocale();
        }
        if (timezone == null) {
          timezone = dc.getTimezone();
        }
      }
      Map<String, Object> filters = ReportsUtil.getReportFilters(request);
      filters.put(ReportsConstants.SORT_ASC, false);

      xLogger.fine("filters: {0}", filters);
      PageParams pageParams = new PageParams(null, size);
      ReportsService rs = Services.getService("reports", locale);
      ReportData
          r =
          rs.getReportData(reportType, null, null, frequency, filters, locale, timezone, pageParams,
              dc, userId);
      if (r != null) {
        return builder.buildAbnormalStockModelList(r.getResults(), locale, timezone);
      } else {
        xLogger.warn("Report data returned NULL");
      }
    } catch (Exception e) {
      xLogger.warn("Exception while getting report data: {0}", e);
      throw new InvalidServiceException(e);
    }

    xLogger.fine("Exiting processRequest");
    return null;
  }

  @RequestMapping(value = "/location", method = RequestMethod.GET)
  public @ResponseBody Results getInventoryByLocation(@RequestParam(required = false) String kioskTags,
                                                      @RequestParam(required = false) String excludedKioskTags,
                                                      @RequestParam(required = false) String materialTags,
                                                      @RequestParam(required = false, defaultValue = "0") int offset,
                                                      @RequestParam(required = false, defaultValue = "50") int size,
                                                      @RequestParam(required = false) String loc,
                                                      @RequestParam(required = false) String pdos,
                                                      HttpServletRequest request) {
      SecureUserDetails sUser = SecurityMgr.getUserDetails(request.getSession());
      String userId = sUser.getUsername();
      PageParams pageParams = new PageParams(null, offset, size);
      Long domainId = SessionMgr.getCurrentDomain(request.getSession(), userId);
      DomainConfig dc = null;
      if (domainId != null) {
          dc = DomainConfig.getInstance(domainId);
      }
      LocationSuggestionModel location = parseLocation(loc);
      try{
      InventoryManagementService ims =
          Services.getService(InventoryManagementServiceImpl.class, sUser.getLocale());
      List results =
          ims.getInvntryByLocation(domainId, location, kioskTags, excludedKioskTags, materialTags, pdos, pageParams)
              .getResults();
      if (results != null) {
        EntitiesService accountsService =
            Services.getService(EntitiesServiceImpl.class, sUser.getLocale());
        MaterialCatalogService mCatalogService =
            Services.getService(MaterialCatalogServiceImpl.class, sUser.getLocale());
        List<InventoryModel> inventoryModelList = new ArrayList<>(results.size());
        Map<Long, String> domainNames = new HashMap<>(1);
        for (int i = 0; i < results.size(); i++) {
          IInvntry inv = (IInvntry) results.get(i);
          inventoryModelList.add(
              builder.buildInventoryModel(inv, dc, accountsService, mCatalogService,
                  Services.getService(UsersServiceImpl.class), ims,
                  accountsService.getKiosk(inv.getKioskId(), false), sUser, offset + i + 1, domainNames));
        }
        return new Results(inventoryModelList, null, -1, offset);
      }
    } catch (ServiceException e) {
      xLogger.warn("Exception in getInventoryByLocation", e);
    }
    return null;
  }

  @RequestMapping(value = "/abnormalstock", method = RequestMethod.GET)
  public
  @ResponseBody
  Results getAbnormalStockDetails(@RequestParam int eventType,
                                  @RequestParam(required = false) String etag,
                                  @RequestParam(required = false) String eetag,
                                  @RequestParam(required = false) String mtag,
                                  @RequestParam(required = false) Long entityId,
                                  @RequestParam(required = false) Long mid,
                                  @RequestParam(required = false) Boolean inDetail,
                                  @RequestParam(required = false) Integer abnBeforeDate,
                                  @RequestParam(required = false) String loc,
                                  @RequestParam(required = false, defaultValue = "0") int offset,
                                  @RequestParam(required = false, defaultValue = "50") int size,
                                  HttpServletRequest request) {
    String cursor;
    HttpSession session = request.getSession();
    cursor = SessionMgr.getCursor(session, CURSOR_STOCK_EVENTS, offset);

    SecureUserDetails sUser = SecurityMgr.getUserDetails(request.getSession());
    String userId = sUser.getUsername();
    Locale locale = sUser.getLocale();
    ResourceBundle backendMessages = Resources.get().getBundle("BackendMessages", locale);
    String timezone = sUser.getTimezone();
    Long domainId = SessionMgr.getCurrentDomain(request.getSession(), userId);
    DomainConfig dc = null;
    if (domainId != null) {
      dc = DomainConfig.getInstance(domainId);
    }
    if (locale == null && dc != null) {
      locale = dc.getLocale();
    }
    if (timezone == null && dc != null) {
      timezone = dc.getTimezone();
    }
    LocationSuggestionModel location = parseLocation(loc);
    Map<String, Object> filters = new HashMap<String, Object>();
    filters.put(ReportsConstants.FILTER_DOMAIN, domainId);
    filters.put(ReportsConstants.FILTER_EVENT, eventType);
    filters.put(ReportsConstants.FILTER_LATEST, true);

    if (StringUtils.isNotEmpty(mtag)) {
      filters.put(ReportsConstants.FILTER_MATERIALTAG, mtag);
    }
    if (StringUtils.isNotEmpty(etag)){
      filters.put(ReportsConstants.FILTER_KIOSKTAG, etag);
    }else if(StringUtils.isNotEmpty(eetag)){
      filters.put(ReportsConstants.FILTER_EXCLUDED_KIOSKTAG, eetag);
    }
    if (entityId != null) {
      filters.put(ReportsConstants.FILTER_KIOSK, entityId);
    }
    if (mid != null) {
      filters.put(ReportsConstants.FILTER_MATERIAL, mid);
    }
    if (abnBeforeDate != null) {
      filters.put(ReportsConstants.FILTER_LATEST, null);
      filters.put(ReportsConstants.FILTER_ABNORMALDURATION, abnBeforeDate);
    }
    if(location!=null){
      filters.put(ReportsConstants.FILTER_LOCATION,location);
    }
    PageParams pageParams = new PageParams(cursor, offset, size);
    try {
        if(BooleanUtils.isTrue(inDetail)){
          filters.put(ReportsConstants.FILTER_ABNORMALSTOCKVIEW, true);
        }
        ReportsService reportsService = Services.getService("reports");
        ReportData
          reportData =
          reportsService
              .getReportData(ReportsConstants.TYPE_STOCKEVENT, null, null, ReportsConstants.FREQ_DAILY, filters,
                      locale, timezone, pageParams, dc, userId);
        List results = reportData.getResults();
        List modelList;
        if(BooleanUtils.isTrue(inDetail)){
            InventoryManagementService
                    ims =
                    Services.getService(InventoryManagementServiceImpl.class, sUser.getLocale());
            EntitiesService
                    accountsService =
                    Services.getService(EntitiesServiceImpl.class, sUser.getLocale());
            MaterialCatalogService
                    mCatalogService =
                    Services.getService(MaterialCatalogServiceImpl.class, sUser.getLocale());
            List<InventoryModel> inventoryModelList = new ArrayList<>();
            Map<Long, String> domainNames = new HashMap<>(1);

            for (int i = 0; i < results.size(); i++) {
                IInvntry inv = (IInvntry) results.get(i);
                inventoryModelList.add(builder.buildInventoryModel(inv, dc, accountsService, mCatalogService,
                    Services.getService(UsersServiceImpl.class),
                        ims, accountsService.getKiosk(inv.getKioskId(), false), sUser, offset + i + 1, domainNames));
            }
            modelList = inventoryModelList;
        } else {
            List<InventoryAbnStockModel>
                    abnModelList =
                    builder.buildAbnormalStockModelList(results, locale, timezone);
            cursor = reportData.getCursor();
            if (cursor != null) {
                int nextOffset = offset + size;
                SessionMgr.setCursor(session, CURSOR_STOCK_EVENTS, nextOffset, cursor);
                xLogger.fine(
                        "ReportsServlet: after API call, set cursor - cursor = {0}, cursorType = cursorstockevents, (nxt)offset = {1}",
                        cursor, nextOffset);
            }
            modelList = abnModelList;
      }
      return new Results(modelList, cursor, -1, offset);
    } catch (Exception e) {
      xLogger.severe("Abnormal stock: Error in reading stock event data: {0}", e);
      throw new InvalidServiceException(backendMessages.getString("abnormal.stock.error"));
    }
  }

  @RequestMapping(value = "/inventoryByMaterial/", method = RequestMethod.GET)
  public
  @ResponseBody
  Results getInventoryByMaterial(@RequestParam Long kioskId, @RequestParam Long[] materials,
                                 HttpServletRequest request) {
    SecureUserDetails sUser = SecurityMgr.getUserDetails(request.getSession());
    String userId = sUser.getUsername();
    Long domainId = SessionMgr.getCurrentDomain(request.getSession(), userId);
    Locale locale = sUser.getLocale();
    try {
      InventoryManagementService
          ims =
          Services.getService(InventoryManagementServiceImpl.class, locale);
      List<IInvntry> dInventories = new ArrayList<IInvntry>(materials.length);
      for (Long material : materials) {
        IInvntry inv = ims.getInventory(kioskId, material);
        if (inv != null) {
          dInventories.add(inv);
        }
      }
      return builder
          .buildInventoryModelListAsResult(new Results(dInventories, "dinv"), sUser, domainId,
              null);
    } catch (ServiceException e) {
      xLogger.severe("Error in reading destination inventories: {0}", e);
    }
    return null;
  }

  @RequestMapping(value = "/predictiveStk", method = RequestMethod.GET)
  public
  @ResponseBody
  List<FChartModel> getInventoryPredictiveStk(@RequestParam Long kioskId,
                                              @RequestParam Long materialId,
                                              HttpServletRequest request) {
    SecureUserDetails sUser = SecurityMgr.getUserDetails(request.getSession());
    Locale locale = sUser.getLocale();
    try {
      ReportsService rs = Services.getService("reports", locale);
//            Calendar stDate = new GregorianCalendar();
//            stDate.add(Calendar.DAY_OF_MONTH, -PREDICTIVE_HISTORY_DAYS);
      // oty = "m", dt = "ksk"
      Results
          ds =
          rs.getSlices(new Date(), ISlice.DAILY, ISlice.OTYPE_MATERIAL, String.valueOf(materialId),
              ISlice.KIOSK, String.valueOf(kioskId), true, new PageParams(PREDICTIVE_HISTORY_DAYS));
      if (ds != null) {
        List<ISlice> slices = ds.getResults();
        if (slices != null && !slices.isEmpty() && slices.size() > (PREDICTIVE_HISTORY_DAYS + 1)) {
          for (int j = (slices.size() - 1); j >= (PREDICTIVE_HISTORY_DAYS + 1); j--) {
            slices.remove(j);
          }
        }
        InventoryManagementService
            ims =
            Services.getService(InventoryManagementServiceImpl.class);
        IInvntry inv = ims.getInventory(kioskId, materialId);
        return fcBuilder.buildPredChartModel(slices, PredictiveUtil.getOrderStkPredictions(inv),
            ims.getDailyConsumptionRate(inv), inv.getStock());
      }
    } catch (Exception e) {
      xLogger.severe("Error in reading stocks wtih predictive: {0}", e);
    }
    return null;
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
      OrderManagementService oms = Services.getService(OrderManagementServiceImpl.class);
      Results
          results =
          oms.getOrders(userId,
              LocalDateUtil.parseCustom(from, Constants.DATE_FORMAT_CSV, dc.getTimezone()),
              LocalDateUtil.parseCustom(to, Constants.DATE_FORMAT_CSV, dc.getTimezone()), null);
      return new MarkerBuilder()
          .buildMarkerListFromOrders(results.getResults(), sUser.getLocale(), sUser.getTimezone());
    } catch (ServiceException e) {
      xLogger.severe("Error in reading destination inventories: {0}", e);
    } catch (ParseException e) {
      xLogger.severe("Parse Exception in reading destination inventories: {0}", e);
    }
    return null;
  }

  @RequestMapping(value = "/task/prediction", method = RequestMethod.POST)
  public
  @ResponseBody
  void updatePrediction(@RequestParam(required = false) String orderId,
                        @RequestParam(required = false) String invId) {
    try {
      if (orderId != null) {
        PredictionService oms = Services.getService("predictions");
        oms.updateOrderPredictions(orderId);
      } else if (invId != null) {
        PredictionService
            ims =
            Services.getService("predictions");
        ims.updateInventoryPredictions(invId);
      }
    } catch (Exception e) {
      xLogger.severe("Error while updating predictions orderId: {0}, invId {1}", orderId, invId, e);
    }
  }

  @RequestMapping(value = "/invHistory", method = RequestMethod.GET)
  public
  @ResponseBody
  List<InventoryMinMaxLogModel> getInventoryHistory(@RequestParam String invId,
                                                    HttpServletRequest request) {
    SecureUserDetails sUser = SecurityMgr.getUserDetails(request.getSession());
    ResourceBundle
        backendMessages =
        Resources.get().getBundle("BackendMessages", sUser.getLocale());
    if (invId == null) {
      return null;
    }
    InventoryManagementService
        ims =
        Services.getService(InventoryManagementServiceImpl.class, sUser.getLocale());
    List<IInventoryMinMaxLog> logs = ims.fetchMinMaxLog(invId);
    return builder.buildInventoryMinMaxLogModel(logs, sUser, backendMessages);
  }

  @RequestMapping(value = "/entity/{entityId}/{materialId}", method = RequestMethod.GET)
  public
  @ResponseBody
  InventoryDetailModel getInvDetail(
      @PathVariable Long entityId,
      @PathVariable Long materialId) throws ServiceException, ObjectNotFoundException {

    SecureUserDetails sUser = SecurityUtils.getUserDetails();
    Long domainId = SecurityUtils.getCurrentDomainId();
    InventoryManagementService
        ims =
        Services.getService(InventoryManagementServiceImpl.class, null);
    Results results =
        ims.getInventory(domainId, entityId, null, null, null, materialId, null, IInvntry.ALL, false,
            null, null,
            new PageParams(0, 1));
    if (results.getResults().isEmpty()) {
      throw new ObjectNotFoundException("Inventory not found");
    }
    return builder
        .buildMInventoryDetail((IInvntry) results.getResults().get(0), domainId, entityId, sUser);
  }
}

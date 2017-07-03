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
import com.logistimo.api.builders.DashboardBuilder;
import com.logistimo.api.builders.mobile.MobileInvDashboardBuilder;
import com.logistimo.api.models.MainDashboardModel;
import com.logistimo.api.models.mobile.DashQueryModel;
import com.logistimo.api.models.mobile.MobileInvDashboardDetails;
import com.logistimo.api.models.mobile.MobileInvDashboardModel;
import com.logistimo.api.util.SearchUtil;
import com.logistimo.auth.utils.SecurityUtils;
import com.logistimo.config.models.DomainConfig;
import com.logistimo.constants.CharacterConstants;
import com.logistimo.constants.Constants;
import com.logistimo.dashboards.service.IDashboardService;
import com.logistimo.dashboards.service.impl.DashboardService;
import com.logistimo.exception.InvalidServiceException;
import com.logistimo.logger.XLog;
import com.logistimo.services.Services;
import com.logistimo.services.cache.MemcacheService;
import com.logistimo.utils.LocalDateUtil;

import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.sql.ResultSet;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;

/**
 * Created by kumargaurav on 13/04/17.
 */

@Controller
@RequestMapping("/dashboards")
public class DashboardControllerMV1 {

  public static final String DISTRICT = "DISTRICT";
  public static final String STATE = "STATE";
  public static final String STATE_LOWER = "state";
  public static final String DISTRICT_LOWER = "district";
  public static final String COUNTRY_LOWER = "country";
  private static final XLog xLogger = XLog.getLog(DashboardControllerMV1.class);

  @RequestMapping(value = "/inventory", method = RequestMethod.GET)
  public
  @ResponseBody
  MobileInvDashboardModel getInvDashBoard(
      @RequestParam(value = "incetags", required = false) String incetags,
      @RequestParam(value = "exetags", required = false) String exetags,
      @RequestParam(value = "mtags", required = false) String mtags,
      @RequestParam(value = "mnm", required = false) String mnm,
      @RequestParam(value = "loc", required = false) String loc,
      @RequestParam(value = "locty", required = false) String locty,
      @RequestParam(value = "p", required = false) Integer p,
      @RequestParam(value = "date", required = false) String date,
      @RequestParam(value = "refresh", required = false, defaultValue = "true") Boolean refresh) {

    long domainId = SecurityUtils.getCurrentDomainId();
    IDashboardService ds;
    DomainConfig dc = DomainConfig.getInstance(domainId);
    MobileInvDashboardModel dashboardModel;
    if (dc.getCountry() == null) {
      return new MobileInvDashboardModel();
    }
    String country = dc.getCountry();
    String state = null;
    String district = null;
    if (StringUtils.isNotEmpty(dc.getDistrict())) {
      state = dc.getState();
      district = dc.getDistrict();
    } else if (StringUtils.isNotEmpty(dc.getState())) {
      state = dc.getState();
    }
    DashQueryModel paramModel = new DashQueryModel(country, state, district, incetags, exetags,
        mtags, mnm, loc, p, date, domainId, null);
    String
        cachekey =
        buildCacheKey(paramModel);
    MemcacheService cache = AppFactory.get().getMemcacheService();
    //getting results for cache
    if (!refresh) {
      dashboardModel = (MobileInvDashboardModel) cache.get(cachekey);
      if (dashboardModel != null) {
        return dashboardModel;
      }
    }
    paramModel.timezone = dc.getTimezone();
    paramModel.locty = locty;
    Map<String, String>
        filters =
        buildQueryFilters(paramModel);
    ds = Services.getService(DashboardService.class);
    ResultSet invTyRes = ds.getMainDashboardResults(domainId, filters, "inv", true, null);
    ResultSet invAlRes = ds.getMainDashboardResults(domainId, filters, "all_inv", true, null);
    ResultSet acstRes = ds.getMainDashboardResults(domainId, filters, "activity", true, null);
    ResultSet alstRes = ds.getMainDashboardResults(domainId, filters, "all_activity", true, null);
    //preparing the model
    dashboardModel =
        MobileInvDashboardBuilder.buildInvDashboard(invTyRes, invAlRes, acstRes, alstRes);
    if (cache != null) {
      cache.put(cachekey, dashboardModel, 1800); // 30 min expiry
    }

    return dashboardModel;
  }


  @RequestMapping(value = "/inventory/breakdown", method = RequestMethod.GET)
  public
  @ResponseBody
  MobileInvDashboardDetails getInvDashBoardDetail(
      @RequestParam(value = "incetags", required = false) String incetags,
      @RequestParam(value = "exetags", required = false) String exetags,
      @RequestParam(value = "mtags", required = false) String mtags,
      @RequestParam(value = "mnm", required = false) String mnm,
      @RequestParam(value = "loc", required = false) String loc,
      @RequestParam(value = "locty", required = false) String locty,
      @RequestParam(value = "p", required = false) Integer p,
      @RequestParam(value = "date", required = false) String date,
      @RequestParam(value = "groupby", required = true) String groupby,
      @RequestParam(value = "refresh", required = false, defaultValue = "true") Boolean refresh) {

    long domainId = SecurityUtils.getCurrentDomainId();
    IDashboardService ds;
    DomainConfig dc = DomainConfig.getInstance(domainId);
    MobileInvDashboardDetails details;
    String country = dc.getCountry();
    String state = null;
    String district = null;
    if (StringUtils.isNotEmpty(dc.getDistrict())) {
      state = dc.getState();
      district = dc.getDistrict();
    } else if (StringUtils.isNotEmpty(dc.getState())) {
      state = dc.getState();
    }
    DashQueryModel paramModel = new DashQueryModel(country, state, district, incetags, exetags,
        mtags, mnm, loc, p, date, domainId, groupby);
    String
        cachekey =
        buildCacheKey(paramModel);
    MemcacheService cache = AppFactory.get().getMemcacheService();
    //getting results for cache
    if (!refresh) {
      details = (MobileInvDashboardDetails) cache.get(cachekey);
      if (details != null) {
        return details;
      }
    }
    paramModel.timezone = dc.getTimezone();
    paramModel.locty = locty;
    Map<String, String>
        filters =
        buildQueryFilters(paramModel);
    ds = Services.getService(DashboardService.class);
    ResultSet invTyRes = ds.getMainDashboardResults(domainId, filters, "inv", false, groupby);
    ResultSet invAlRes = ds.getMainDashboardResults(domainId, filters, "all_inv", false, groupby);
    ResultSet
        alstRes =
        ds.getMainDashboardResults(domainId, filters, "all_activity", false, null);
    //preparing the model
    details =
        MobileInvDashboardBuilder
            .buildInvDetailDashboard(invTyRes, invAlRes, alstRes, locty, groupby);
    //adding loc level in response
    if (StringUtils.isBlank(loc) && paramModel.state == null) {
      details.level = COUNTRY_LOWER;
    } else if (StringUtils.isBlank(loc) && paramModel.district == null) {
      details.level = STATE_LOWER;
    } else {
      details.level = paramModel.locty != null ? paramModel.locty : DISTRICT_LOWER;
    }
    if (cache != null) {
      cache.put(cachekey, details, 1800); // 30 min expiry
    }

    return details;
  }

  @RequestMapping(value = "/assets", method = RequestMethod.GET)
  public
  @ResponseBody
  MainDashboardModel getAssetDashboard(@RequestParam(required = false) String filter,
                                       @RequestParam(required = false) String level,
                                       @RequestParam(required = false) String tPeriod,
                                       @RequestParam(required = false) String aType,
                                       @RequestParam(required = false) String excludeETag,
                                       @RequestParam(required = false, defaultValue = "false")
                                       Boolean refresh) {
    MainDashboardModel model;
    Long domainId = SecurityUtils.getCurrentDomainId();
    MemcacheService cache = AppFactory.get().getMemcacheService();
    DomainConfig dc = DomainConfig.getInstance(domainId);
    String country = dc.getCountry();
    String state = null;
    String district = null;
    if (StringUtils.isNotEmpty(dc.getDistrict())) {
      state = dc.getState();
      district = dc.getDistrict();
    } else if (StringUtils.isNotEmpty(dc.getState())) {
      state = dc.getState();
    }
    DashQueryModel
        queryModel =
        new DashQueryModel(country, state, district, excludeETag, filter, tPeriod, domainId, aType,
            level);
    String cacheKey = buildCacheKey(queryModel);
    cacheKey += "_AD";
    if (!refresh) {
      model = (MainDashboardModel) cache.get(cacheKey);
      if (model != null) {
        return model;
      }
    }
    Map<String, String> filters = buildQueryFilters(queryModel);
    level = queryModel.locty;
    String colFilter;
    if (DISTRICT_LOWER.equals(level) || queryModel.district != null) {
      colFilter = "NAME";
    } else if (STATE_LOWER.equals(level) || queryModel.state != null) {
      colFilter = DISTRICT;
    } else {
      colFilter = STATE;
    }
    try {
      IDashboardService ds = Services.getService(DashboardService.class);
      ResultSet tempRes = ds.getMainDashboardResults(domainId, filters, "temperature");
      DashboardBuilder builder = new DashboardBuilder();
      model =
          builder
              .getMainDashBoardData(null, null, null, null, tempRes, null, colFilter);
      if (StringUtils.isBlank(filter) && queryModel.state == null) {
        model.mLev = COUNTRY_LOWER;
      } else if (StringUtils.isBlank(filter) && queryModel.district == null) {
        model.mLev = STATE_LOWER;
      } else {
        model.mLev = level != null ? level : DISTRICT_LOWER;
      }
      try {
        if (cache != null) {
          cache.put(cacheKey, model, 1800); // 30 min expiry
        }
      } catch (Exception e) {
        xLogger.warn("Error in caching dashboard data", e);
      }
      return model;
    } catch (Exception e) {
      xLogger.warn("Error while fetching asset dashboard for domain: {0}", domainId, e);
      throw new InvalidServiceException(e);
    }
  }


  private String buildCacheKey(DashQueryModel model) {

    String cacheKey = Constants.MDASHBOARD_CACHE_PREFIX + String.valueOf(model.domainId);
    cacheKey += CharacterConstants.UNDERSCORE + model.country;
    if (StringUtils.isNotEmpty(model.district)) {
      cacheKey += CharacterConstants.UNDERSCORE + model.state;
      cacheKey += CharacterConstants.UNDERSCORE + model.district;
    } else if (StringUtils.isNotEmpty(model.state)) {
      cacheKey += CharacterConstants.UNDERSCORE + model.state;
    }
    if (null != model.domainId) {
      cacheKey += CharacterConstants.UNDERSCORE + model.domainId;
    }
    if (StringUtils.isNotEmpty(model.loc)) {
      cacheKey += CharacterConstants.UNDERSCORE + model.loc;
    }
    if (StringUtils.isNotEmpty(model.mtags)) {
      cacheKey += CharacterConstants.UNDERSCORE + model.mtags;
    }
    if (StringUtils.isNotEmpty(model.mnm)) {
      cacheKey += CharacterConstants.UNDERSCORE + model.mnm;
    }
    if (model.p != null) {
      cacheKey += CharacterConstants.UNDERSCORE + model.p;
    }
    if (StringUtils.isNotEmpty(model.incetags)) {
      cacheKey += CharacterConstants.UNDERSCORE + model.incetags;
    }
    if (StringUtils.isNotEmpty(model.exetags)) {
      cacheKey += CharacterConstants.UNDERSCORE + model.exetags;
    }
    if (StringUtils.isNotBlank(model.date)) {
      cacheKey += CharacterConstants.UNDERSCORE + model.date;
    }
    if (StringUtils.isNotBlank(model.groupby)) {
      cacheKey += CharacterConstants.UNDERSCORE + model.groupby;
    }
    if (StringUtils.isNotBlank(model.aty)) {
      cacheKey += "_A" + model.aty;
    }
    if (StringUtils.isNotBlank(model.tp)) {
      cacheKey += "_T" + model.tp;
    }

    return cacheKey;
  }

  private Map<String, String> buildQueryFilters(DashQueryModel model) {

    if (StringUtils.isBlank(model.locty)) {
      model.locty = "";
    }
    Map<String, String> filters = new HashMap<>(1);
    filters.put(COUNTRY_LOWER, model.country);
    if (StringUtils.isBlank(model.loc) && model.district != null) {
      filters.put(DISTRICT_LOWER, model.district);
      filters.put(STATE_LOWER, model.state);
      //adding this
      model.locty = DISTRICT_LOWER;
    } else if (StringUtils.isBlank(model.loc) && model.state != null) {
      filters.put(STATE_LOWER, model.state);
      boolean noDistrict = !SearchUtil.isDistrictAvailable(model.country, model.state);
      if (noDistrict) {
        model.locty = DISTRICT_LOWER;
        filters.put(DISTRICT_LOWER, "");
        model.district = model.state;
      } else {
        model.locty = STATE_LOWER;
      }
    } else if (STATE_LOWER.equals(model.locty)) {
      filters.put(STATE_LOWER, model.loc);
      boolean noDistrict = !SearchUtil.isDistrictAvailable(model.country, model.loc);
      if (noDistrict) {
        model.locty = DISTRICT_LOWER;
        filters.put(DISTRICT_LOWER, "");
      }
    } else if (DISTRICT_LOWER.equals(model.locty)) {
      String[] locarr = model.loc.split(CharacterConstants.UNDERSCORE);
      model.state = locarr[0];
      model.district = locarr[1];
      filters.put(STATE_LOWER, model.state);
      filters.put(DISTRICT_LOWER, model.district);
    }
    if (StringUtils.isNotEmpty(model.mtags)) {
      filters.put("mTag", model.mtags);
    } else if (StringUtils.isNotEmpty(model.mnm)) {
      filters.put("mId", model.mnm);
    }
    if (model.p != null) {
      filters.put("period", String.valueOf(model.p));
    }
    if (StringUtils.isNotEmpty(model.incetags)) {
      filters.put("eTag", model.incetags);
    }
    if (StringUtils.isNotEmpty(model.exetags)) {
      filters.put("eeTag", model.exetags);
    }
    if (StringUtils.isNotEmpty(model.tp)) {
      filters.put("tPeriod", model.tp);
    }
    if (StringUtils.isNotEmpty(model.aty)) {
      filters.put("type", model.aty);
    }
    if (StringUtils.isNotBlank(model.date)) {
      try {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date
            startDate =
            LocalDateUtil.parseCustom(model.date, Constants.DATE_FORMAT, model.timezone);
        //Increment date to use less than queries
        Calendar calendar;
        if (model.timezone != null) {
          calendar = GregorianCalendar.getInstance(TimeZone.getTimeZone(model.timezone));
        } else {
          calendar = GregorianCalendar.getInstance();
        }
        calendar.setTime(startDate);
        calendar.add(Calendar.DAY_OF_MONTH, 1);
        calendar.add(Calendar.MILLISECOND, -1);
        filters.put("date", sdf.format(calendar.getTime()));
      } catch (Exception e) {
        xLogger.warn("M Dashboard: Exception when parsing start date {0} , domain: {1}", model.date,
            model.domainId, e);
        throw new InvalidServiceException("Unable to parse date " + model.date);
      }
    }
    return filters;
  }

}

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
package com.logistimo.reports.utils;

import com.google.gson.Gson;

import com.logistimo.config.models.ReportObjDimType;
import com.logistimo.config.models.ReportObjDimValue;
import com.logistimo.constants.Constants;
import com.logistimo.dao.JDOUtils;
import com.logistimo.domains.entity.IDomain;
import com.logistimo.domains.service.DomainsService;
import com.logistimo.domains.service.impl.DomainsServiceImpl;
import com.logistimo.entities.entity.IKiosk;
import com.logistimo.entities.models.LocationSuggestionModel;
import com.logistimo.entities.service.EntitiesService;
import com.logistimo.entities.service.EntitiesServiceImpl;
import com.logistimo.logger.XLog;
import com.logistimo.materials.entity.IMaterial;
import com.logistimo.materials.service.MaterialCatalogService;
import com.logistimo.materials.service.impl.MaterialCatalogServiceImpl;
import com.logistimo.pagination.PageParams;
import com.logistimo.pagination.Results;
import com.logistimo.reports.ReportsConstants;
import com.logistimo.reports.entity.slices.IDaySlice;
import com.logistimo.reports.entity.slices.IMonthSlice;
import com.logistimo.reports.entity.slices.IReportsSlice;
import com.logistimo.reports.entity.slices.ISlice;
import com.logistimo.reports.entity.slices.SliceDateComparator;
import com.logistimo.reports.generators.IReportDataGeneratorFactory;
import com.logistimo.reports.service.ReportsService;
import com.logistimo.services.ObjectNotFoundException;
import com.logistimo.services.Resources;
import com.logistimo.services.ServiceException;
import com.logistimo.services.Services;
import com.logistimo.services.impl.PMF;
import com.logistimo.tags.TagUtil;
import com.logistimo.tags.entity.ITag;
import com.logistimo.users.entity.IUserAccount;
import com.logistimo.users.service.UsersService;
import com.logistimo.users.service.impl.UsersServiceImpl;
import com.logistimo.utils.LocalDateUtil;
import com.logistimo.utils.QueryUtil;

import org.apache.commons.lang.StringUtils;
import org.json.JSONException;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

import javax.jdo.PersistenceManager;
import javax.jdo.Query;
import javax.servlet.http.HttpServletRequest;


/**
 * @author Arun
 */
public class ReportsUtil {

  private static final XLog xLogger = XLog.getLog(ReportsUtil.class);
  private static final String SEPARATOR = "&nbsp;&nbsp;&nbsp;";
  @SuppressWarnings({"unchecked", "serial", "rawtypes"})
  private static Map<String, String> PARAMNAMES_MAP = new HashMap() {{
    put(ReportsConstants.FILTER_MATERIAL, "materialid");
    put(ReportsConstants.FILTER_KIOSK, "kioskid");
    put(ReportsConstants.FILTER_DOMAIN, "domainid");
    put(ReportsConstants.FILTER_DISTRICT, "district");
    put(ReportsConstants.FILTER_STATE, "state");
    put(ReportsConstants.FILTER_COUNTRY, "country");
    put(ReportsConstants.FILTER_POOLGROUP, "poolgroup");
    put(ReportsConstants.FILTER_USER, "uid");
  }};
  @SuppressWarnings({"unchecked", "serial", "rawtypes"})
  private static Map<String, String>
      DISPLAYNAMES_MAP =
      new HashMap() {{ // a mapping of filter IDs to resource name keys
        put(ReportsConstants.FILTER_MATERIAL, "materials"); // earlier Material(s)
        put(ReportsConstants.FILTER_KIOSK, "kiosks"); // Kiosk(s)
        put(ReportsConstants.FILTER_DOMAIN, "domain");
        put(ReportsConstants.FILTER_DISTRICT, "districts"); // District(s)
        put(ReportsConstants.FILTER_STATE, "states");
        put(ReportsConstants.FILTER_COUNTRY, "country");
        put(ReportsConstants.FILTER_POOLGROUP, "poolgroups");
        put(ReportsConstants.FILTER_USER, "user");
        put(ReportsConstants.FILTER_KIOSKTAG, "tagentity");
        put(ReportsConstants.FILTER_MATERIALTAG, "tagmaterial");
      }};

  // Get a map of filters, given a filter string format as follows:
  // <filter-type1>:<itemid-1>,<item-id2>,...;<filter-type2>...
  public static Map<String, String[]> getFilterMap(String filters) {
    Map<String, String[]> filterMap = new HashMap<String, String[]>();
    if (filters != null && !filters.isEmpty()) {
      String filtersArray[] = filters.split(";");
      if (filtersArray == null || filtersArray.length == 0) {
        filtersArray = new String[1];
        filtersArray[0] = filters;
      }
      for (int i = 0; i < filtersArray.length; i++) {
        String[] filterData = filtersArray[i].split(":");
        String filterType = filterData[0];
        String[] filterVals = filterData[1].split(",");
        if (filterVals == null || filterVals.length == 0) {
          filterVals = new String[1];
          filterVals[0] = filterData[1];
        }
        // Update filter map
        filterMap.put(filterType, filterVals);
      }
    }

    return filterMap;
  }

  // Get a URL query string, given a filter map
  public static String getQueryString(Map<String, String[]> filterMap, List<String> ignoreFilters) {
    if (filterMap == null || filterMap.isEmpty()) {
      return "";
    }
    String qstr = "";
    Iterator<String> it = filterMap.keySet().iterator();
    while (it.hasNext()) {
      String ftype = it.next();
      if (ignoreFilters != null && ignoreFilters.contains(ftype)) {
        continue; // move to next filter
      }
      String[] values = filterMap.get(ftype);
      if (values == null || values.length == 0) {
        continue;
      }
      String param = PARAMNAMES_MAP.get(ftype);
      if (param == null) {
        param = ftype;
      }
      qstr += param + "=";
      for (int i = 0; i < values.length; i++) {
        if (i > 0) {
          qstr += ",";
        }
        qstr += values[i];
      }
      if (it.hasNext()) {
        qstr += "&";
      }
    }
    return qstr;
  }

  // Get the filter display string, given a filter map
  public static String getFilterDisplay(String startDate, String endDate,
                                        Map<String, String[]> filterMap, List<String> ignoreFilters,
                                        Locale locale) {
    xLogger.fine("filterMap: {0}", filterMap);
    if (filterMap == null || filterMap.isEmpty()) {
      return "";
    }
    String disp = null;
    Object[] ftypes = filterMap.keySet().toArray();
    Arrays.sort(ftypes);
    try {
      // Get the services
      EntitiesService as = Services.getService(EntitiesServiceImpl.class, locale);
      UsersService us = Services.getService(UsersServiceImpl.class, locale);
      DomainsService ds = Services.getService(DomainsServiceImpl.class, locale);
      MaterialCatalogService
          mcs =
          Services.getService(MaterialCatalogServiceImpl.class, locale);
      // Get the resource bundle
      ResourceBundle
          messages =
          Resources.get()
              .getBundle("Messages", locale); ///Resources.get().getBundle( "Messages", locale );
      if (messages == null) {
        xLogger.warn("Could not get (null) resource bundle Messages_{0}",
            (locale == null ? null : locale.toString()));
        return null;
      }
      disp =
          "<b>" + messages.getString("from") + "</b>: " + startDate + SEPARATOR + "<b>" + messages
              .getString("to") + "</b>: " + endDate + SEPARATOR;
      for (int i = 0; i < ftypes.length; i++) {
        String ftype = (String) ftypes[i];
        if (ignoreFilters != null && ignoreFilters.contains(ftype)) {
          continue; // ignore this filter
        }
        String[] values = filterMap.get(ftype);
        if (values == null || values.length == 0) {
          continue; // go to the next filter
        }
        // Get the name
        String name = messages.getString(DISPLAYNAMES_MAP.get(ftype));
        if (name == null) {
          name = ftype;
        }
        disp += "<b>" + name + "</b>: ";
        // Get the displayable form of values
        for (int j = 0; j < values.length; j++) {
          if (j > 0) {
            disp += ", ";
          }
          if (ReportsConstants.FILTER_MATERIAL.equals(ftype)) {
            disp += mcs.getMaterial(Long.valueOf(values[j])).getName();
          } else if (ReportsConstants.FILTER_KIOSK.equals(ftype)) {
            disp += as.getKiosk(Long.valueOf(values[j]), false).getName();
          } else if (ReportsConstants.FILTER_DOMAIN.equals(ftype)) {
            disp += ds.getDomain(Long.valueOf(values[j])).getName();
          } else if (ReportsConstants.FILTER_POOLGROUP.equals(ftype)) {
            disp += as.getPoolGroup(Long.valueOf(values[j])).getName();
          } else if (ReportsConstants.FILTER_USER.equals(ftype)) {
            disp += us.getUserAccount(values[j]).getFullName();
          } else {
            disp += values[j];
          }
        }
        // Add separator
        disp += SEPARATOR;
      }
    } catch (Exception e) {
      xLogger.severe("ServiceException: {0}", e.getMessage());
    }
    return disp;
  }

  // Check if a non-domain Dimension is present
  public static boolean hasNonDomainDimension(Map<String, String[]> filterMap) {
    if (filterMap == null || filterMap.isEmpty()) {
      return false;
    }
    Iterator<String> it = filterMap.keySet().iterator();
    boolean hasNonDomainFilter = false;
    while (it.hasNext()) {
      String filter = it.next();
      if (!(ReportsConstants.FILTER_DOMAIN.equals(filter) && filterMap.get(filter) != null) &&
          !(ReportsConstants.FILTER_MATERIAL.equals(filter) && filterMap.get(filter) != null)) {
        // If there is something in addition to primary filters - domain and material - then set to true and break out of loop
        hasNonDomainFilter = true;
        break;
      }
    }
    return hasNonDomainFilter;
  }

  // Get the data from the daily or monthly slices
  @SuppressWarnings("unchecked")
  public static Results getSlices(String frequency, Date from, Date until, String objectType,
                                  String objectId, String dimType, String dimValue,
                                  PageParams pageParams) {
    if (from == null || objectType == null || dimType == null || dimValue == null) {
      throw new IllegalArgumentException(
          "At least start date, object type, dim. type and dim. value should be present");
    }
    boolean isFreqDaily = (ReportsConstants.FREQ_DAILY.equals(frequency));
    // Determine the model to be queried, and query filters
    Class<? extends ISlice> clazz = JDOUtils.getImplClass(IMonthSlice.class);
    if (isFreqDaily) {
      clazz = JDOUtils.getImplClass(IDaySlice.class);
    }
    // Get the dates set correctly
    Calendar cal = GregorianCalendar.getInstance();
    cal.setTime(from);
    LocalDateUtil.resetTimeFields(cal);
    Date fromDate = cal.getTime();
    if (until == null) {
      cal.setTime(new Date());
    } else {
      cal.setTime(until);
    }
    LocalDateUtil.resetTimeFields(cal);
    Date untilDate = cal.getTime();
    // Update the filters
    String queryFilters = "oty == otyParam && dt == dtParam && dv == dvParam && d > fromParam";
    String
        declarations =
        " PARAMETERS String otyParam, String dtParam, String dvParam, Date fromParam";
    Map<String, Object> params = new HashMap<String, Object>();
    params.put("otyParam", objectType);
    params.put("dtParam", dimType);
    params.put("dvParam", dimValue);
    params.put("fromParam", LocalDateUtil.getOffsetDate(fromDate, -1, Calendar.MILLISECOND));
    if (untilDate != null) {
      queryFilters += " && d < untilParam";
      declarations += ", Date untilParam";
      params.put("untilParam", untilDate);
    }
    if (objectId != null) {
      queryFilters += " && oId == oIdParam";
      declarations += ", String oIdParam";
      params.put("oIdParam", objectId);
    }
    // Form query
    PersistenceManager pm = PMF.get().getPersistenceManager();
    List<? extends ISlice> results = null;
    String cursor = null;
    // Form query
    String
        queryStr =
        "SELECT FROM " + clazz.getName() + " WHERE " + queryFilters + declarations
            + " import java.util.Date; ORDER BY d DESC";
    // Execute query
    Query q = pm.newQuery(queryStr);
    if (pageParams != null) {
      QueryUtil.setPageParams(q, pageParams);
    }
    try {
      results = (List<? extends ISlice>) q.executeWithMap(params);
      if (results != null) {
        results.size();
        cursor = QueryUtil.getCursor(results);
        results = (List<? extends ISlice>) pm.detachCopyAll(results);
      }
    } finally {
      try {
        q.closeAll();
      } catch (Exception ignored) {

      }
      pm.close();
    }
    return new Results(results, cursor);
  }

  public static Map<String, Object> getReportFilters(HttpServletRequest request) {
    xLogger.fine("Entering getReportFilters");
    // Get the various reporting filters
    try {
      String domainIdStr = request.getParameter("domainid");
      String kioskIdStr = request.getParameter("kioskid");
      String materialIdStr = request.getParameter("materialid");
      // Get status
      String statusStr = (StringUtils.isNotEmpty(request.getParameter("status"))) ?
          URLDecoder.decode(request.getParameter("status"), Constants.UTF8) : null;
      // Get flag to indicate whether only the latest entries are to be included (used by Demand Board maps)
      String latestFlag = request.getParameter("latest");
      // Get dimensional filters
      String state = (StringUtils.isNotEmpty(request.getParameter("state"))) ?
          URLDecoder.decode(request.getParameter("state"), Constants.UTF8) : null;
      String district = (StringUtils.isNotEmpty(request.getParameter("district"))) ?
          URLDecoder.decode(request.getParameter("district"), Constants.UTF8) : null;
      String poolgroup = (StringUtils.isNotEmpty(request.getParameter("poolgroup"))) ?
          URLDecoder.decode(request.getParameter("poolgroup"), Constants.UTF8) : null;
      // Get vendor id
      String otype = (StringUtils.isNotEmpty(request.getParameter("otype"))) ?
          URLDecoder.decode(request.getParameter("otype"), Constants.UTF8) : null;
      // order type = sale or purchase; typically, sent with a get orders request
      // User Id
      String userIdFilter = request.getParameter("uid");
      // Event type, if any
      String eventTypeStr = request.getParameter("eventtype");
      // Check if abnormal stock exported from stock views
      boolean abnStockView = request.getParameter("abnstockview") != null;
      String abnDuration = request.getParameter("dur");
      //location filter from stock views page
      LocationSuggestionModel location = parseLocation(
          (request.getParameter("loc") != null)
              ? URLDecoder.decode(request.getParameter("loc"), "UTF-8")
              : null);
      // Tags, if any
      String materialTag = (StringUtils.isNotEmpty(request.getParameter("mtag"))) ?
          URLDecoder.decode(request.getParameter("mtag"), Constants.UTF8) : null;
      String kioskTags = (StringUtils.isNotEmpty(request.getParameter("ktag"))) ?
          URLDecoder.decode(request.getParameter("ktag"), Constants.UTF8) : null;
      if (StringUtils.isEmpty(kioskTags)) {
        kioskTags = (StringUtils.isNotEmpty(request.getParameter("etag"))) ?
            URLDecoder.decode(request.getParameter("etag"), Constants.UTF8) : null;
      }
      String excludedKioskTags = null;
      if(StringUtils.isEmpty(kioskTags)){
        excludedKioskTags = (StringUtils.isNotEmpty(request.getParameter("eetag"))) ?
            URLDecoder.decode(request.getParameter("eetag"), Constants.UTF8) : null;
      }
      String orderTag = (StringUtils.isNotEmpty(request.getParameter("otag"))) ?
          URLDecoder.decode(request.getParameter("otag"), Constants.UTF8) : null;
      // Get domain Id
      Long domainId = null;
      if (StringUtils.isNotEmpty(domainIdStr)) {
        try {
          domainId = Long.valueOf(domainIdStr);
        } catch (NumberFormatException e) {
          xLogger.warn("Invalid domain ID format: {0}", domainIdStr);
        }
      }
      // Create the filter map
      HashMap<String, Object> filters = new HashMap<String, Object>();
      Long kioskId = null;
      Long materialId = null;
      // Add the domain filter
      if (StringUtils.isNotEmpty(domainIdStr)) {
        filters.put(ReportsConstants.FILTER_DOMAIN, domainId);
      }
      // Add the kiosk filter, if present
      if (StringUtils.isNotEmpty(kioskIdStr)) {
        kioskId = Long.valueOf(kioskIdStr);
        filters.put(ReportsConstants.FILTER_KIOSK, kioskId);
      }
      // Add the material filter, if present
      if (StringUtils.isNotEmpty(materialIdStr)) {
        materialId = Long.valueOf(materialIdStr);
        filters.put(ReportsConstants.FILTER_MATERIAL, materialId);
      }
      // Add status filter, if any
      if (StringUtils.isNotEmpty(statusStr)) {
        filters.put(ReportsConstants.FILTER_STATUS, statusStr);
      }
      // Add latest flag, if any
      if (StringUtils.isNotEmpty(latestFlag)) {
        filters.put(ReportsConstants.FILTER_LATEST, latestFlag);
      }
      // Add location filters, if any
      if (StringUtils.isNotEmpty(state)) {
        filters.put(ReportsConstants.FILTER_STATE, state);
      }
      if (StringUtils.isNotEmpty(district)) {
        filters.put(ReportsConstants.FILTER_DISTRICT, district);
      }
      // Add poolgroup filters, if any
      if (StringUtils.isNotEmpty(poolgroup)) {
        filters.put(ReportsConstants.FILTER_POOLGROUP, Long.valueOf(poolgroup));
      }
      // Add vendor Id, if any
      if (StringUtils.isNotEmpty(otype)) {
        filters.put(ReportsConstants.FILTER_OTYPE, otype);
      }
      // Add user Id filter, if any
      if (StringUtils.isNotEmpty(userIdFilter)) {
        filters.put(ReportsConstants.FILTER_USER, userIdFilter);
      }
      // Add event type, if any
      if (StringUtils.isNotEmpty(eventTypeStr)) {
        filters.put(ReportsConstants.FILTER_EVENT, Integer.valueOf(eventTypeStr));
      }
      // If abnormal stock view
      if (abnStockView) {
        filters.put(ReportsConstants.FILTER_ABNORMALSTOCKVIEW, true);
      }
      // Add abnormality duration filter
      if (StringUtils.isNotEmpty(abnDuration)) {
        filters.put(ReportsConstants.FILTER_ABNORMALDURATION, Integer.parseInt(abnDuration));
      }
      // Add location filter
      if (location != null) {
        filters.put(ReportsConstants.FILTER_LOCATION, location);
      }
      // Add tags, if any
      if (StringUtils.isNotEmpty(materialTag)) {
        filters.put(ReportsConstants.FILTER_MATERIALTAG, materialTag);
      }
      if (StringUtils.isNotEmpty(kioskTags)) {
        filters.put(ReportsConstants.FILTER_KIOSKTAG, kioskTags);
      } else if(StringUtils.isNotEmpty(excludedKioskTags)){
        filters.put(ReportsConstants.FILTER_EXCLUDED_KIOSKTAG, excludedKioskTags);
      } else if (StringUtils.isNotEmpty(orderTag)) {
        filters.put(ReportsConstants.FILTER_ORDERTAG, orderTag);
      }
      return filters;
    } catch (UnsupportedEncodingException e) {
      xLogger.warn("Error in getting report filters", e);
    }
    return null;
  }

  public static LocationSuggestionModel parseLocation(String loc) {
    try {
      if (loc != null) {
        return new Gson().fromJson(loc, LocationSuggestionModel.class);
      }
    } catch (JSONException e) {
      xLogger.warn("Error in parsing location filter object", e);
    }
    return null;
  }

  public static List<ISlice> fillMissingSlicesWithDefaultValues(List<? extends ISlice> slices,
                                                                String periodType) {
    int n = slices.size();
    //startDate is the date of last slice , as results are order by desc date
    Date startDate = slices.get(n - 1).getDate();
    // endDate is the date of the first slice.
    Date endDate = slices.get(0).getDate();
    return fillMissingSlices(slices, startDate, endDate, periodType, true);
  }

  /**
   * Fill the missing slice with default values when initMissingSlicesWithDefaultValues,
   * otherwise fill the slice between the dates with prevSlice data
   * @param initMissingSlicesWithDefaultValues - flag to indicate - filling up the slice with the default values
   * @param slices
   * @param startDate
   * @param endDate
   * @param periodType
   * @param initMissingSlicesWithDefaultValues
   * @return
   */

  public static List<ISlice> fillMissingSlices(List<? extends ISlice> slices, Date startDate,
                                               Date endDate, String periodType,
                                               Boolean initMissingSlicesWithDefaultValues) {
    xLogger
        .info("Entering fillMissingSlices, between startDate: {0}, endDate: {1}, periodType: {2} ",
            startDate, endDate, periodType);
    // If slices == null or empty, log a warning message and return null
    if (slices == null || slices.isEmpty()) {
      xLogger.warn("Inside fillMissingSlices, slices is null or empty");
      return null;
    }
    if (!(ISlice.DAILY.equals(periodType) || ISlice.MONTHLY.equals(periodType))) {
      xLogger.warn("Invalid periodType: periodType: {0}", periodType);
      return null;
    }
    // The slices is assumed to be incomplete.
    List<ISlice> newList = new ArrayList<ISlice>();
    int n = slices.size(); // Size of the incomplete arraylist of slices
    int i = n - 2;
    ISlice
        prevSlice =
        slices.get(n - 1); // Make the last element in the arrayList as previous Slice
    if (prevSlice == null) {
      xLogger.warn("Inside fillMissingSlices, prevSlice is null or empty");
      return null;
    }
    while (i >= 0) {
      newList.add(prevSlice);
      ISlice curSlice = slices.get(i); // Current slice is one element before the previous slice
      if (curSlice == null) {
        xLogger.warn("Inside fillMissingSlices, curSlice is null or empty");
        return null;
      }
      if (diffInDates(curSlice.getDate(), prevSlice.getDate(), periodType) > 1) {
        // Get new slices from prevSlice.date to curSlice.date, slice type is decided by filterType, new slices are initialized from prevSlice
        List<ISlice>
            newSlices =
            getNewSlices(prevSlice, curSlice.getDate(), periodType,
                initMissingSlicesWithDefaultValues);
        if (newSlices == null || newSlices.isEmpty()) {
          xLogger.warn("Inside fillMissingSlices, newSlices is null or empty");
          return null;
        }
        newList.addAll(newSlices);
      }
      prevSlice = curSlice;
      i--;
    }
    newList.add(prevSlice); // Add the last slice here
    if (diffInDates(endDate, prevSlice.getDate(), periodType) > 1) { //Earlier > 1
      List<ISlice>
          newSlices =
          getNewSlices(prevSlice, endDate, periodType, initMissingSlicesWithDefaultValues);
      if (newSlices == null || newSlices.isEmpty()) {
        xLogger.warn("Inside fillMissingSlices, newSlices is null or empty");
        return null;
      }
      newList.addAll(newSlices);
    }

    // Sort the newList in descending order of date.
    Collections.sort(newList, new SliceDateComparator());

    return newList;
  }

  // This method creates new Slices between fromSlice.date and toSlice date, initializes them from fromSlice
  // The type of Slice created is based on filterType
  private static List<ISlice> getNewSlices(ISlice fromSlice, Date toSliceDate, String periodType,
                                           Boolean initMissingSlicesWithDefaultValues) {
    Date nextSliceDate;
    List<ISlice> newSlices = new ArrayList<ISlice>();
    nextSliceDate = getNextSliceDate(fromSlice.getDate(), periodType);
    ISlice prevSlice = fromSlice;
    while (diffInDates(toSliceDate, nextSliceDate, periodType) > 0) { // Earlier > 0
      ISlice newSlice = JDOUtils.createInstance(IReportsSlice.class);
      newSlice.setDate(nextSliceDate, null, periodType); // Set the are for the newSlice to be date1
      if (!initMissingSlicesWithDefaultValues) {
        initFromPrevSlice(prevSlice, newSlice);
      }
      newSlices.add(newSlice);
      prevSlice = newSlice;
      // date1 = getNextSliceDate( date1, filterType );
      nextSliceDate = getNextSliceDate(prevSlice.getDate(), periodType);
    }
    return newSlices;
  }

  // Returns the difference between two dates in days or months depending on the type being DomainCounts.FILTERTYPE_DAILY or DomainCounts.FILTERTYPE_MONTHLY
  private static int diffInDates(Date date1, Date date2, String periodType) {
    // date1 > date2
    int diff = 0;
    Calendar start = GregorianCalendar.getInstance();
    start.setTime(date2);
    Calendar end = GregorianCalendar.getInstance();
    end.setTime(date1);

    if (ISlice.DAILY.equals(periodType)) {
      diff = LocalDateUtil.daysBetweenDates(start, end);
    } else if (ISlice.MONTHLY.equals(periodType)) {
      diff = LocalDateUtil.monthsBetweenDates(start, end);
    }
    return diff;
  }

  // Returns the next slice date depending on the filterType.
  private static Date getNextSliceDate(Date date, String periodType) {
    Date nextDate = null;
    if (ISlice.DAILY.equals(periodType)) {
      nextDate = LocalDateUtil.getOffsetDate(date, 1, Calendar.DATE);
    } else if (ISlice.MONTHLY.equals(periodType)) {
      nextDate = LocalDateUtil.getOffsetDate(date, 1, Calendar.MONTH);
    }
    return nextDate;
  }

  // Private method that creates and returns a DaySlice or MonthSlice based on filterType
  private static ISlice createNewSlice(String periodType) {
    ISlice slice = null;
    if (ISlice.DAILY.equals(periodType)) {
      slice = JDOUtils.createInstance(IReportsSlice.class);
    } else if (ISlice.MONTHLY.equals(periodType)) {
      slice = JDOUtils.createInstance(IMonthSlice.class);
    }
    return slice;
  }

  // Initializes the curSlice from the prevSlice.
  private static void initFromPrevSlice(ISlice prevSlice, ISlice curSlice) {
    curSlice.setDomainId(prevSlice.getDomainId());
    curSlice.initFromLastSlice(prevSlice);
  }

  public static ReportObjDimType getObjectDimType(Map sliceMap) {
    String objectType;
    String dimType;
    if (sliceMap.containsKey(ReportsConstants.MATERIAL)) {
      objectType = ReportsConstants.MATERIAL;
      if (sliceMap.containsKey(ReportsConstants.KIOSK)) {
        dimType = ReportsConstants.KIOSK;
      } else if (sliceMap.containsKey(ReportsConstants.MATERIAL_TAG)) {
        dimType = ReportsConstants.MATERIAL_TAG;
      } else if (sliceMap.containsKey(ReportsConstants.KIOSK_TAG)) {
        dimType = ReportsConstants.KIOSK_TAG;
      } else if (sliceMap.containsKey(ReportsConstants.DISTRICT)) {
        dimType = ReportsConstants.DISTRICT;
      } else if (sliceMap.containsKey(ReportsConstants.STATE)) {
        dimType = ReportsConstants.STATE;
      } else if (sliceMap.containsKey(ReportsConstants.COUNTRY)) {
        dimType = ReportsConstants.COUNTRY;
      } else {
        dimType = ReportsConstants.DOMAIN;
      }
    } else if (sliceMap.containsKey(ReportsConstants.KIOSK)) {
      objectType = ReportsConstants.KIOSK;
      if (sliceMap.containsKey(ReportsConstants.MATERIAL_TAG)) {
        dimType = ReportsConstants.MATERIAL_TAG;
      } else if (sliceMap.containsKey(ReportsConstants.KIOSK_TAG)) {
        dimType = ReportsConstants.KIOSK_TAG;
      } else if (sliceMap.containsKey(ReportsConstants.DISTRICT)) {
        dimType = ReportsConstants.DISTRICT;
      } else if (sliceMap.containsKey(ReportsConstants.STATE)) {
        dimType = ReportsConstants.STATE;
      } else if (sliceMap.containsKey(ReportsConstants.STATE)) {
        dimType = ReportsConstants.STATE;
      } else {
        dimType = ReportsConstants.DOMAIN;
      }
    } else if (sliceMap.containsKey(ReportsConstants.USER)) {
      objectType = ReportsConstants.USER;
      if (sliceMap.containsKey(ReportsConstants.MATERIAL_TAG)) {
        dimType = ReportsConstants.MATERIAL_TAG;
      } else if (sliceMap.containsKey(ReportsConstants.KIOSK_TAG)) {
        dimType = ReportsConstants.KIOSK_TAG;
      } else if (sliceMap.containsKey(ReportsConstants.DISTRICT)) {
        dimType = ReportsConstants.DISTRICT;
      } else if (sliceMap.containsKey(ReportsConstants.STATE)) {
        dimType = ReportsConstants.STATE;
      } else if (sliceMap.containsKey(ReportsConstants.COUNTRY)) {
        dimType = ReportsConstants.COUNTRY;
      } else if (sliceMap.containsKey(ReportsConstants.USER_TAG)) {
        dimType = ReportsConstants.USER_TAG;
      } else {
        dimType = ReportsConstants.DOMAIN;
      }
    } else {
      objectType = ReportsConstants.DOMAIN;
      if (sliceMap.containsKey(ReportsConstants.MATERIAL_TAG)) {
        dimType = ReportsConstants.MATERIAL_TAG;
      } else if (sliceMap.containsKey(ReportsConstants.KIOSK_TAG)) {
        dimType = ReportsConstants.KIOSK_TAG;
      } else if (sliceMap.containsKey(ReportsConstants.DISTRICT)) {
        dimType = ReportsConstants.DISTRICT;
      } else if (sliceMap.containsKey(ReportsConstants.STATE)) {
        dimType = ReportsConstants.STATE;
      } else if (sliceMap.containsKey(ReportsConstants.COUNTRY)) {
        dimType = ReportsConstants.COUNTRY;
      } else {
        dimType = ReportsConstants.DOMAIN;
      }
    }
    return new ReportObjDimType(objectType, dimType);
  }

  public static ReportObjDimValue getObjectDimValue(Map sliceMap) {
    String objectValue;
    String dimValue;
    ReportObjDimType reportObjDimType = getObjectDimType(sliceMap);
    try {
      if (ReportsConstants.MATERIAL.equals(reportObjDimType.objType)) {
        MaterialCatalogService mcs = Services.getService(MaterialCatalogServiceImpl.class);
        IMaterial
            material =
            mcs.getMaterial(Long.valueOf(sliceMap.get(ReportsConstants.MATERIAL).toString()));
        objectValue = material.getName();
      } else if (ReportsConstants.KIOSK.equals(reportObjDimType.objType)) {
        EntitiesService as = Services.getService(EntitiesServiceImpl.class);
        IKiosk k = as.getKiosk(Long.valueOf(sliceMap.get(ReportsConstants.KIOSK).toString()));
        objectValue = k.getName();
      } else if (ReportsConstants.USER.equals(reportObjDimType.objType)) {
        UsersService as = Services.getService(UsersServiceImpl.class);
        IUserAccount u = as.getUserAccount(sliceMap.get(ReportsConstants.USER).toString());
        objectValue = u.getFullName();
      } else {
        DomainsService ds = Services.getService(DomainsServiceImpl.class);
        IDomain
            domain =
            ds.getDomain(Long.valueOf(sliceMap.get(ReportsConstants.DOMAIN).toString()));
        objectValue = domain.getName();
      }

      if (ReportsConstants.KIOSK.equals(reportObjDimType.dimType)) {
        EntitiesService as = Services.getService(EntitiesServiceImpl.class);
        IKiosk k = as.getKiosk(Long.valueOf(sliceMap.get(ReportsConstants.KIOSK).toString()));
        dimValue = k.getName();
      } else if (ReportsConstants.MATERIAL_TAG.equals(reportObjDimType.dimType)) {
        dimValue =
            TagUtil.getTagById(Long.valueOf(sliceMap.get(ReportsConstants.MATERIAL_TAG).toString()),
                ITag.MATERIAL_TAG);
      } else if (ReportsConstants.KIOSK_TAG.equals(reportObjDimType.dimType)) {
        dimValue =
            TagUtil.getTagById(Long.valueOf(sliceMap.get(ReportsConstants.KIOSK_TAG).toString()),
                ITag.KIOSK_TAG);
      } else if (ReportsConstants.DISTRICT.equals(reportObjDimType.dimType)) {
        dimValue = sliceMap.get(ReportsConstants.DISTRICT).toString();
      } else if (ReportsConstants.STATE.equals(reportObjDimType.dimType)) {
        dimValue = sliceMap.get(ReportsConstants.STATE).toString();
      } else if (ReportsConstants.COUNTRY.equals(reportObjDimType.dimType)) {
        dimValue = sliceMap.get(ReportsConstants.COUNTRY).toString();
      } else {
        DomainsService ds = Services.getService(DomainsServiceImpl.class);
        IDomain domain = ds.getDomain(Long.valueOf(sliceMap.get("did").toString()));
        dimValue = domain.getName();
      }
      return new ReportObjDimValue(objectValue, dimValue);
    } catch (ServiceException | ObjectNotFoundException e) {
      xLogger.severe("Exception while set the object and dimension values ", e);
    }
    return null;
  }

  public static String getDisplayName(String type, ResourceBundle messages) {
    String typeName;
    switch (type) {
      case ReportsConstants.DOMAIN:
        typeName = messages.getString("domain");
        break;
      case ReportsConstants.MATERIAL:
        typeName = messages.getString("material");
        break;
      case ReportsConstants.KIOSK:
        typeName = messages.getString("kiosk");
        break;
      case ReportsConstants.USER:
        typeName = messages.getString("user");
        break;
      case ReportsConstants.MATERIAL_TAG:
        typeName = messages.getString("tagmaterial");
        break;
      case ReportsConstants.KIOSK_TAG:
        typeName = messages.getString("tagkiosk");
        break;
      case ReportsConstants.DISTRICT:
        typeName = messages.getString("district");
        break;
      case ReportsConstants.STATE:
        typeName = messages.getString("state");
        break;
      case ReportsConstants.COUNTRY:
        typeName = messages.getString("country");
        break;
      case ReportsConstants.USER_TAG:
        typeName = "User Tag";
        break;
      default:
        typeName = "";
    }
    return typeName;
  }

  public static IReportDataGeneratorFactory getReportDataGeneratorFactory()
      throws ServiceException {
    return ((ReportsService) Services.getService("reports")).getReportDataGeneratorFactory();
  }

}

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

package com.logistimo.reports.plugins.service.util;

import com.google.common.collect.TreeBasedTable;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import com.logistimo.auth.utils.SecurityUtils;
import com.logistimo.config.models.AssetSystemConfig;
import com.logistimo.config.models.DomainConfig;
import com.logistimo.constants.CharacterConstants;
import com.logistimo.constants.Constants;
import com.logistimo.entities.entity.IKiosk;
import com.logistimo.entities.service.EntitiesService;
import com.logistimo.entities.service.EntitiesServiceImpl;
import com.logistimo.logger.XLog;
import com.logistimo.materials.service.MaterialCatalogService;
import com.logistimo.materials.service.impl.MaterialCatalogServiceImpl;
import com.logistimo.reports.constants.ReportCompareField;
import com.logistimo.reports.constants.ReportViewType;
import com.logistimo.reports.models.ReportDataModel;
import com.logistimo.reports.plugins.Report;
import com.logistimo.reports.plugins.internal.QueryHelper;
import com.logistimo.reports.plugins.internal.QueryRequestModel;
import com.logistimo.reports.plugins.models.ReportChartModel;
import com.logistimo.services.ObjectNotFoundException;
import com.logistimo.services.ServiceException;
import com.logistimo.services.Services;
import com.logistimo.tags.TagUtil;
import com.logistimo.tags.entity.ITag;
import com.logistimo.users.entity.IUserAccount;
import com.logistimo.users.service.UsersService;
import com.logistimo.users.service.impl.UsersServiceImpl;
import com.logistimo.utils.LocalDateUtil;

import org.apache.commons.lang.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.Days;
import org.joda.time.Period;
import org.joda.time.PeriodType;
import org.joda.time.format.DateTimeFormat;
import org.json.JSONArray;
import org.json.JSONObject;

import java.lang.reflect.Field;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;

/**
 * @author Mohan Raja
 */
public class ReportServiceUtil {

  private static final String ROWS = "rows";
  private static final String ROW_HEADINGS = "rowHeadings";
  protected static final String HEADINGS = "headings";
  protected static final String LABEL_HEADINGS = "labelHeadings";
  protected static final String TABLE = "table";

  protected static final String ZERO = "0";

  private static final String LONG = "Long";
  private static final String FLOAT = "Float";
  private static final String MAP = "Map";

  private static final XLog xLogger = XLog.getLog(ReportServiceUtil.class);

  protected List<Report> constructReportList(String data)
      throws NoSuchFieldException, IllegalAccessException {
    if(Constants.NULL.equals(data)) {
      return null;
    }
    JSONObject jsonObject = new JSONObject(data);
    if (jsonObject.has(ROWS)) {
      List<Report> reports = new ArrayList<>();
      JSONArray headings = jsonObject.getJSONArray(HEADINGS);
      List<Field> fields = new ArrayList<>(headings.length());
      for (int j = 0; j < headings.length(); j++) {
        fields.add(Report.class.getDeclaredField(headings.getString(j)));
      }
      JSONArray results = jsonObject.getJSONArray(ROWS);
      for (int i = 0; i < results.length(); i++) {
        JSONArray row = results.getJSONArray(i);
        reports.add(constructReport(fields, row));
      }
      return reports;
    }
    return null;
  }

  protected String getReportLabel(Map<String, String> filters, Report report)
      throws ParseException {
    SimpleDateFormat dateFormat, labelDateFormat;
    switch (filters.get(QueryHelper.TOKEN_PERIODICITY)) {
      case QueryHelper.MONTH:
        dateFormat = new SimpleDateFormat(QueryHelper.DATE_FORMAT_MONTH);
        labelDateFormat = new SimpleDateFormat(QueryHelper.DATE_FORMAT_DAILY);
        return labelDateFormat.format(dateFormat.parse(report.getTime()));
      default:
        return report.getTime();
    }
  }

  protected Report constructReport(List<Field> fields, JSONArray row) throws IllegalAccessException {
    Report report = new Report();
    for (int j = 0; j < row.length(); j++) {
      Field field = fields.get(j);
      switch (field.getType().getSimpleName()) {
        case LONG:
          if (StringUtils.isNotBlank(row.getString(j))) {
            field.set(report, row.getLong(j));
          } else {
            field.set(report, 0L);
          }
          break;
        case FLOAT:
          if (StringUtils.isNotBlank(row.getString(j))) {
            field.set(report, new Float(row.getDouble(j)));
          } else {
            field.set(report, 0F);
          }
          break;
        case MAP:
          try {
            field.set(report,
                new Gson().fromJson(row.getString(j), new TypeToken<Map<String, Float>>() {
                }.getType()));
          } catch (Exception e) {
            field.set(report, new HashMap(0));
          }
          break;
        default:
          field.set(report, row.get(j));
      }
    }
    return report;
  }

  protected List<ReportChartModel> fillChartData(
      List<ReportChartModel> reportChartModels,
      SortedSet<String> tSet,
      Map<String, String> filters) {
    String from = filters.get(QueryHelper.TOKEN_START_TIME);
    String to = filters.get(QueryHelper.TOKEN_END_TIME);
    SimpleDateFormat labelDateFormat = new SimpleDateFormat(QueryHelper.DATE_FORMAT_DAILY);
    SimpleDateFormat dateFormat;
    Calendar c = new GregorianCalendar();
    Integer period;
    try {
      Date fromDate, toDate;
      switch (filters.get(QueryHelper.TOKEN_PERIODICITY)) {
        case QueryHelper.MONTH:
          dateFormat = new SimpleDateFormat(QueryHelper.DATE_FORMAT_MONTH);
          fromDate = dateFormat.parse(from);
          c.setTime(fromDate);
          from = labelDateFormat.format(fromDate);
          toDate = dateFormat.parse(to);
          to = labelDateFormat.format(toDate);
          period = Calendar.MONTH;
          break;
        case QueryHelper.WEEK:
          dateFormat = new SimpleDateFormat(QueryHelper.DATE_FORMAT_DAILY);
          c.setTime(dateFormat.parse(from));
          period = Calendar.WEEK_OF_YEAR;
          break;
        default:
          dateFormat = new SimpleDateFormat(QueryHelper.DATE_FORMAT_DAILY);
          c.setTime(dateFormat.parse(from));
          period = Calendar.DAY_OF_YEAR;
      }
      int size = reportChartModels.get(0).value.size();
      Calendar today = new GregorianCalendar();
      today = LocalDateUtil.resetTimeFields(today);
      while (to.compareTo(from) >= 0) {
        if (!tSet.contains(from)) {
          ReportChartModel rep = new ReportChartModel();
          rep.label = from;
          rep.value = new ArrayList<>(size);
          for (int i = 0; i < size; i++) {
            rep.value.add(new ReportDataModel(CharacterConstants.EMPTY));
          }
          reportChartModels.add(rep);
        }
        c.add(period, 1);
        if(c.after(today)) {
          break;
        }
        from = labelDateFormat.format(c.getTime());
      }
    } catch (ParseException e) {
      xLogger.warn("Parse exception while filling chart data", e);
    } catch (Exception e) {
      xLogger.warn("Exception while filling chart data", e);
    }
    return reportChartModels;
  }

  protected void setCompareField(
      Report report, ReportCompareField compareField, List<ReportDataModel> values)
      throws ServiceException {
    switch (compareField) {
      case MATERIAL:
        MaterialCatalogService ms = Services.getService(MaterialCatalogServiceImpl.class);
        values.add(addData(ms.getMaterial(report.getMaterialId()).getName()));
        break;
      case MATERIAL_TAG:
        values.add(addData(TagUtil.getTagById(report.getMaterialTag(), ITag.MATERIAL_TAG)));
        break;
      case ENTITY:
        EntitiesService es = Services.getService(EntitiesServiceImpl.class);
        values.add(addData(es.getKiosk(report.getKioskId(), false).getName()));
        break;
      case ENTITY_TAG:
        values.add(addData(TagUtil.getTagById(report.getKioskTag(),ITag.KIOSK_TAG)));
        break;
      case STATE:
        values.add(addData(report.getState()));
        break;
      case DISTRICT:
        values.add(addData(report.getDistrict()));
        break;
      case TALUK:
        values.add(addData(report.getTaluk()));
        break;
      default:
        values.add(addData(CharacterConstants.EMPTY));
    }
  }

  protected ReportDataModel addData(Object value) {
    return new ReportDataModel(value != null ? String.valueOf(value) : ZERO);
  }

  protected ReportDataModel addData(Object value, Object numerator, Object denominator) {
    return new ReportDataModel(
        String.valueOf(value), String.valueOf(numerator), String.valueOf(denominator));
  }

  protected Double getHours(Object milliseconds) {
    if (milliseconds == null) {
      return null;
    }
    return Double.parseDouble(String.valueOf(milliseconds)) / (60 * 60 * 1000);
  }

  protected Double getDays(Object milliseconds) {
    if (milliseconds == null) {
      return null;
    }
    return Double.parseDouble(String.valueOf(milliseconds)) / (60 * 60 * 1000 * 24);
  }

  protected Double getDaysFromHours(Object hours) {
    if (hours == null) {
      return null;
    }
    return Double.parseDouble(String.valueOf(hours)) / 24;
  }

  protected JSONObject constructTableBaseData(
      JSONObject jsonObject, ReportViewType viewType,
      JSONArray headersJson, QueryRequestModel model) throws ParseException {
    return constructTableBaseData(jsonObject, viewType, headersJson, model, false);
  }

  protected JSONObject constructTableBaseData(
      JSONObject jsonObject, ReportViewType viewType,
      JSONArray headersJson, QueryRequestModel model, boolean deviceIdCheck) throws ParseException {
    JSONObject output = new JSONObject();
    TreeBasedTable<String, String, List<String>> treeBasedTable = TreeBasedTable.create();
    int dataSize = jsonObject.getJSONArray(HEADINGS).length() - 2;
    SimpleDateFormat labelDateFormat = new SimpleDateFormat(QueryHelper.DATE_FORMAT_DAILY);
    Set<String> rowKeySet = new HashSet<>();
    if (jsonObject.has(ROWS)) {
      JSONArray rowsJson = jsonObject.getJSONArray(ROWS);
      for (int i = 0; i < rowsJson.length(); i++) {
        List<String> data = new ArrayList<>(dataSize);
        for (int j = 0; j < dataSize; j++) {
          data.add(
              StringUtils.isNotEmpty(rowsJson.getJSONArray(i).getString(j + 2))
                  ? rowsJson.getJSONArray(i).getString(j + 2)
                  : ZERO);
        }
        rowKeySet.add(rowsJson.getJSONArray(i).getString(0));
        String date = rowsJson.getJSONArray(i).getString(1);
        if(StringUtils.isNotEmpty(date)){
          if (QueryHelper.MONTH.equals(model.filters.get(QueryHelper.TOKEN_PERIODICITY))) {
            SimpleDateFormat dateFormat = new SimpleDateFormat(QueryHelper.DATE_FORMAT_MONTH);
            date = labelDateFormat.format(dateFormat.parse(date));
          }
          treeBasedTable.put(rowsJson.getJSONArray(i).getString(0), date, data);
        }
      }
    }
    JSONArray rowHeadings = null;
    if (jsonObject.has(ROW_HEADINGS)) {
      rowHeadings = jsonObject.getJSONArray(ROW_HEADINGS);
      if (deviceIdCheck && viewType == ReportViewType.BY_ASSET) {
        rowHeadings = new JSONArray(rowKeySet);
      }
    }
    treeBasedTable = fillTable(treeBasedTable, model.filters, rowHeadings, dataSize);
    List<String> headers = new ArrayList<>(treeBasedTable.columnKeySet().size() + 1);
    headers.add(headersJson.getString(0));
    headers.addAll(treeBasedTable.columnKeySet());
    Map<String, List<List<String>>> tableMap = new HashMap<>();
    for (String rowKey : treeBasedTable.rowKeySet()) {
      List<List<String>> list = new ArrayList<>();
      for (String colKey : treeBasedTable.columnKeySet()) {
        if (treeBasedTable.get(rowKey, colKey) != null) {
          list.add(treeBasedTable.get(rowKey, colKey));
        } else {
          String[] arr = new String[dataSize];
          List<String> emptyList = new ArrayList<>(Arrays.asList(arr));
          Collections.fill(emptyList, ZERO);
          list.add(emptyList);
        }
      }
      tableMap.put(rowKey, list);
    }
    output.put(LABEL_HEADINGS, new ArrayList<>(headers));
    for (int i = 1; i < headers.size(); i++) {
      SimpleDateFormat dateFormat;
      switch (model.filters.get(QueryHelper.TOKEN_PERIODICITY)) {
        case QueryHelper.MONTH:
          dateFormat = new SimpleDateFormat(QueryHelper.DATE_FORMAT_MONTH);
          headers.set(i, dateFormat.format(labelDateFormat.parse(headers.get(i))));
          break;
      }
    }
    output.put(HEADINGS, headers);
    output.put(TABLE, tableMap);
    return output;
  }

  protected TreeBasedTable<String, String, List<String>> fillTable(
      TreeBasedTable<String, String, List<String>> treeBasedTable,
      Map<String, String> filters, JSONArray rowHeadings, int finalDataSize) {
    String from = filters.get(QueryHelper.TOKEN_START_TIME);
    String to = filters.get(QueryHelper.TOKEN_END_TIME);
    SimpleDateFormat labelDateFormat = new SimpleDateFormat(QueryHelper.DATE_FORMAT_DAILY);
    SimpleDateFormat dateFormat;
    Calendar c = new GregorianCalendar();
    Integer period;
    try {
      switch (filters.get(QueryHelper.TOKEN_PERIODICITY)) {
        case QueryHelper.MONTH:
          dateFormat = new SimpleDateFormat(QueryHelper.DATE_FORMAT_MONTH);
          c.setTime(dateFormat.parse(from));
          from = labelDateFormat.format(c.getTime());
          to = labelDateFormat.format(dateFormat.parse(to));
          period = Calendar.MONTH;
          break;
        case QueryHelper.WEEK:
          dateFormat = new SimpleDateFormat(QueryHelper.DATE_FORMAT_DAILY);
          c.setTime(dateFormat.parse(from));
          from = labelDateFormat.format(c.getTime());
          to = labelDateFormat.format(dateFormat.parse(to));
          period = Calendar.WEEK_OF_YEAR;
          break;
        default:
          dateFormat = new SimpleDateFormat(QueryHelper.DATE_FORMAT_DAILY);
          c.setTime(dateFormat.parse(from));
          from = labelDateFormat.format(c.getTime());
          to = labelDateFormat.format(dateFormat.parse(to));
          period = Calendar.DAY_OF_YEAR;
          break;
      }
      boolean newRow = false;
      if (treeBasedTable.rowKeySet().size() == 0 && rowHeadings != null && rowHeadings.length() > 0) {
        treeBasedTable.put(rowHeadings.getString(0), ZERO, new ArrayList<String>(0));
        newRow = true;
      }
      while (to.compareTo(from) >= 0) {
        if (!treeBasedTable.containsColumn(from) || newRow) {
          String[] arr = new String[finalDataSize];
          List<String> emptyList = new ArrayList<>(Arrays.asList(arr));
          Collections.fill(emptyList, ZERO);
          if (treeBasedTable.rowKeySet().size() > 0) {
            treeBasedTable.put(treeBasedTable.rowKeySet().iterator().next(), from, emptyList);
          }
        }
        c.add(period, 1);
        from = labelDateFormat.format(c.getTime());
      }
      if (newRow) {
        treeBasedTable.remove(rowHeadings.getString(0), ZERO);
      }
      if (rowHeadings != null) {
        for (int i = 0; i < rowHeadings.length(); i++) {
          if (!treeBasedTable.containsRow(rowHeadings.get(i)) || newRow) {
            String[] arr = new String[finalDataSize];
            List<String> emptyList = new ArrayList<>(Arrays.asList(arr));
            Collections.fill(emptyList, ZERO);
            treeBasedTable.put(
                rowHeadings.getString(i),
                treeBasedTable.columnKeySet().iterator().next(),
                emptyList);
          }
        }
      }

    } catch (ParseException e) {
      xLogger.warn("Error in parsing from date while filling table", e);
    }
    return treeBasedTable;
  }

  protected String getTableKeyByViewType(ReportViewType viewType, String id) throws Exception {
    String key;
    switch (viewType) {
      case BY_MATERIAL:
        key = getTableByMaterialKey(id);
        break;
      case BY_ENTITY:
        key = getTableByEntityKey(id);
        break;
      case BY_ENTITY_TAGS:
        key = TagUtil.getTagById(Long.valueOf(id), ITag.KIOSK_TAG);
        break;
      case BY_USER:
        key = getUserDetailsById(id);
        break;
      case BY_MANUFACTURER:
        key = StringUtils.capitalize(id);
        break;
      case BY_ASSET_TYPE:
        AssetSystemConfig assets = AssetSystemConfig.getInstance();
        key = assets.getAssetsNameByType(2).get(Integer.parseInt(id));
        break;
      default:
        key = id;
    }
    return key;
  }

  private String getTableByMaterialKey(String materialId)
      throws ServiceException {
    String key;MaterialCatalogService ms = Services.getService(MaterialCatalogServiceImpl.class);
    key = ms.getMaterial(Long.valueOf(materialId)).getName();
    key += CharacterConstants.PIPE + materialId;
    return key;
  }

  private String getTableByEntityKey(String string) throws ServiceException {
    String key;EntitiesService es = Services.getService(EntitiesServiceImpl.class);
    IKiosk k = es.getKiosk(Long.valueOf(string), false);
    key = k.getName() + CharacterConstants.PIPE + string +
        CharacterConstants.PIPE + (StringUtils.isEmpty(k.getCity())? CharacterConstants.EMPTY : k.getCity()) +
        CharacterConstants.PIPE + (StringUtils.isEmpty(k.getTaluk())? CharacterConstants.EMPTY : k.getTaluk()) +
        CharacterConstants.PIPE + (StringUtils.isEmpty(k.getDistrict())? CharacterConstants.EMPTY : k.getDistrict()) +
        CharacterConstants.PIPE + (StringUtils.isEmpty(k.getState())? CharacterConstants.EMPTY : k.getState());
    return key;
  }

  /**
   * Mehtod to get user details based on ID and form the report table data
   */
  private String getUserDetailsById(String userId)
      throws ServiceException, ObjectNotFoundException {
    UsersService us = Services.getService(UsersServiceImpl.class);
    IUserAccount userAccount = us.getUserAccount(userId);
    return userAccount.getFullName() + CharacterConstants.PIPE + userId;
  }

  protected Long getMillisInPeriod(String time, String periodicity) {
    Long totalMillis;
    String tz = DomainConfig.getInstance(SecurityUtils.getCurrentDomainId()).getTimezone();
    DateTimeZone timezone = StringUtils.isNotEmpty(tz) ? DateTimeZone.forID(tz) : DateTimeZone.UTC;
    DateTime currentDateTime = new DateTime();
    DateTime from;
    switch (periodicity) {
      case QueryHelper.MONTH:
        from = DateTimeFormat.forPattern(QueryHelper.DATE_FORMAT_MONTH).withZone(timezone)
                .parseDateTime(time);
        if (currentDateTime.isAfter(from) && currentDateTime.isBefore(from.plusMonths(1))) {
          Period p = new Period(
                  from, from.plusDays(Days.daysBetween(from,currentDateTime).getDays() + 1),
                  PeriodType.seconds());
          totalMillis = (long) p.getSeconds() * 1000;
        } else {
          totalMillis =
              LocalDateUtil.MILLISECS_PER_DAY
                  * from.dayOfMonth().getMaximumValue();
        }
        break;
      case QueryHelper.WEEK:
        from = DateTimeFormat.forPattern(QueryHelper.DATE_FORMAT_DAILY).withZone(timezone)
                .parseDateTime(time);
        if (currentDateTime.isAfter(from) && currentDateTime.isBefore(from.plusWeeks(1))) {
          Period p = new Period(
                  from, from.plusDays(Days.daysBetween(from, currentDateTime).getDays() + 1),
                  PeriodType.seconds());
          totalMillis = (long) p.getSeconds() * 1000;
        } else {
          totalMillis = LocalDateUtil.MILLISECS_PER_DAY * 7;
        }
        break;
      case QueryHelper.DAY:
        from = DateTimeFormat.forPattern(QueryHelper.DATE_FORMAT_DAILY).withZone(timezone)
                .parseDateTime(time);
        if(currentDateTime.isAfter(from) && currentDateTime.isBefore(from.plusDays(1))){
          Period p =
              new Period(from, from.plusDays(Days.daysBetween(from, currentDateTime).getDays() + 1),
                  PeriodType.seconds());
          totalMillis = (long) p.getSeconds() * 1000;
        }else{
          totalMillis = LocalDateUtil.MILLISECS_PER_DAY;
        }
        break;
      default:
        totalMillis = 0L;
    }
    return totalMillis;
  }
}

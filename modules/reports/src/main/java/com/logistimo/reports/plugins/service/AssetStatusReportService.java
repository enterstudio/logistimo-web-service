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

package com.logistimo.reports.plugins.service;

import com.logistimo.logger.XLog;
import com.logistimo.reports.constants.ReportCompareField;
import com.logistimo.reports.constants.ReportViewType;
import com.logistimo.reports.models.ReportDataModel;
import com.logistimo.reports.plugins.Report;
import com.logistimo.reports.plugins.internal.QueryRequestModel;
import com.logistimo.reports.plugins.models.ReportChartModel;
import com.logistimo.reports.plugins.models.TableResponseModel;
import com.logistimo.reports.plugins.service.util.ReportServiceUtil;
import com.logistimo.services.ServiceException;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * @author Mohan Raja
 */
@Service("aas")
public class AssetStatusReportService extends ReportServiceUtil implements IReportService {

  private static final XLog xLogger = XLog.getLog(AssetStatusReportService.class);

  @Override
  public List<ReportChartModel> buildReportsData(String data, ReportCompareField compareField,
                                                 Map<String, String> filters)
      throws Exception {
    List<Report> reports = constructReportList(data);
    if (reports != null) {
      List<ReportChartModel> reportData = new ArrayList<>(reports.size());
      SortedSet<String> tSet = new TreeSet<>();
      for (Report report : reports) {
        ReportChartModel m = new ReportChartModel();
        m.label = getReportLabel(filters, report);
        tSet.add(m.label);
        m.value = getReportValues(report, compareField);
        reportData.add(m);
      }
      if (reportData.size() > 0) {
        return fillChartData(reportData, tSet, filters);
      } else {
        return reportData;
      }
    }
    return null;
  }

  @Override
  public TableResponseModel buildReportTableData(String json, ReportViewType viewType,
                                                 QueryRequestModel model) throws Exception {
    JSONObject jsonObject = new JSONObject(json);
    if (!jsonObject.has(HEADINGS)) {
      xLogger.warn("No data found");
      return null;
    }
    JSONArray headersJson = jsonObject.getJSONArray(HEADINGS);
    if (headersJson.length() < 3) {
      xLogger.warn("Insufficient data found. Expect to have at least 3 columns");
      return null;
    }
    jsonObject = constructTableBaseData(jsonObject, viewType, headersJson, model);
    if (jsonObject.has(TABLE) && jsonObject.getJSONObject(TABLE).length() != 0) {
      TableResponseModel response = new TableResponseModel();
      JSONArray jsonArray = jsonObject.getJSONArray(HEADINGS);
      JSONArray labelJsonArray = jsonObject.getJSONArray(LABEL_HEADINGS);
      for (int i = 0; i < labelJsonArray.length(); i++) {
        response.headings.add(labelJsonArray.getString(i));
      }

      List<Field> fields = new ArrayList<>(headersJson.length());
      for (int j = 0; j < headersJson.length(); j++) {
        fields.add(Report.class.getDeclaredField(headersJson.getString(j)));
      }
      JSONObject results = jsonObject.getJSONObject(TABLE);
      Map<String, List<List<ReportDataModel>>> tables = new HashMap<>(results.length());
      JSONArray dimensions = results.names();
      for (int i = 0; i < dimensions.length(); i++) {
        JSONArray period = results.getJSONArray(dimensions.getString(i));
        String key = getTableKeyByViewType(viewType, dimensions.getString(i));
        tables.put(key, new ArrayList<List<ReportDataModel>>(period.length()));
        List<Report> reports = new ArrayList<>();
        for (int j = 0; j < period.length(); j++) {
          JSONArray row = period.getJSONArray(j);
          Report r = constructReport(fields.subList(2, fields.size()), row);
          r.setTime(jsonArray.getString(j + 1));
          reports.add(r);
        }
        for (Report report : reports) {
          tables.get(key).add(getReportValues(report, ReportCompareField.NONE));
        }
      }
      response.table = tables;
      return response;
    }
    return null;
  }

  @Override
  public String getColumns(Map<String, String> filters, ReportViewType viewType) {
    return "wkec, urec, brec, cdec, dec, stbec, wked, ured, bred, cded, " +
        "stbed, ded, wkecs, urecs, brecs, cdecs, stbecs, decs, miac, mac";
  }

  @Override
  public String getTableColumns(Map<String, String> filters, ReportViewType viewType) {
    return getColumns(filters, viewType);
  }

  public List<ReportDataModel> getReportValues(Report report, ReportCompareField compareField)
      throws ServiceException {
    List<ReportDataModel> values = new ArrayList<>(25);
    setCompareField(report, compareField, values);
    Long assetCount = report.getMonitoredAssetCount() + report.getMonitoringAssetCount();
    values.add(addData(report.getAssetStatusEventWorking()));
    values.add(addData(report.getAssetStatusEventUnderRepair()));
    values.add(addData(report.getAssetStatusEventBeyondRepair()));
    values.add(addData(report.getAssetStatusEventCondemned()));
    values.add(addData(report.getAssetStatusEventStandby()));
    values.add(addData(report.getAssetStatusEventDefrost()));

    if (assetCount == 0) {
      for (int i = 0; i < 6; i++) {
        values.add(addData(ZERO));
      }
    } else {
      values.add(addData((float) report.getAssetStatusEventWorking() / assetCount,
          report.getAssetStatusEventWorking(), assetCount));
      values.add(addData((float) report.getAssetStatusEventUnderRepair() / assetCount,
          report.getAssetStatusEventUnderRepair(), assetCount));
      values.add(addData((float) report.getAssetStatusEventBeyondRepair() / assetCount,
          report.getAssetStatusEventBeyondRepair(), assetCount));
      values.add(addData((float) report.getAssetStatusEventCondemned() / assetCount,
          report.getAssetStatusEventCondemned(), assetCount));
      values.add(addData((float) report.getAssetStatusEventStandby() / assetCount,
          report.getAssetStatusEventStandby(), assetCount));
      values.add(addData((float) report.getAssetStatusEventDefrost() / assetCount,
          report.getAssetStatusEventDefrost(), assetCount));
    }

    if (assetCount == 0) {
      for (int i = 0; i < 6; i++) {
        values.add(addData(ZERO));
      }
    } else {
      values.add(addData(
          getHours(report.getAssetStatusWorkingDuration()) / assetCount,
          getHours(report.getAssetStatusWorkingDuration()), assetCount));
      values.add(addData(
          getHours(report.getAssetStatusUnderRepairDuration()) / assetCount,
          getHours(report.getAssetStatusUnderRepairDuration()), assetCount));
      values.add(addData(
          getHours(report.getAssetStatusBeyondRepairDuration()) / assetCount,
          getHours(report.getAssetStatusBeyondRepairDuration()), assetCount));
      values.add(addData(
          getHours(report.getAssetStatusCondemnedDuration()) / assetCount,
          getHours(report.getAssetStatusCondemnedDuration()), assetCount));
      values.add(addData(
          getHours(report.getAssetStatusStandbyDuration()) / assetCount,
          getHours(report.getAssetStatusStandbyDuration()), assetCount));
      values.add(addData(
          getHours(report.getAssetStatusDefrostDuration()) / assetCount,
          getHours(report.getAssetStatusDefrostDuration()), assetCount));
    }

    if (assetCount == 0) {
      for (int i = 0; i < 6; i++) {
        values.add(addData(ZERO));
      }
    } else {
      values.add(addData(Math.min(100 * (float) report.getAssetStatusWorking() / assetCount, 100),
          report.getAssetStatusWorking(), assetCount));
      values
          .add(addData(Math.min(100 * (float) report.getAssetStatusUnderRepair() / assetCount, 100),
              report.getAssetStatusUnderRepair(), assetCount));
      values.add(
          addData(Math.min(100 * (float) report.getAssetStatusBeyondRepair() / assetCount, 100),
              report.getAssetStatusBeyondRepair(), assetCount));
      values.add(addData(Math.min(100 * (float) report.getAssetStatusCondemned() / assetCount, 100),
          report.getAssetStatusCondemned(), assetCount));
      values.add(addData(Math.min(100 * (float) report.getAssetStatusStandby() / assetCount, 100),
          report.getAssetStatusStandby(), assetCount));
      values.add(addData(Math.min(100 * (float) report.getAssetStatusDefrost() / assetCount, 100),
          report.getAssetStatusDefrost(), assetCount));
    }
    return values;
  }

}

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

import com.logistimo.constants.CharacterConstants;
import com.logistimo.logger.XLog;
import com.logistimo.reports.constants.ReportCompareField;
import com.logistimo.reports.constants.ReportViewType;
import com.logistimo.reports.models.ReportDataModel;
import com.logistimo.reports.plugins.Report;
import com.logistimo.reports.plugins.internal.QueryHelper;
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
@Service(value = "itc")
public class TransactionCountReportService extends ReportServiceUtil implements IReportService {

  private static final XLog xLogger = XLog.getLog(TransactionCountReportService.class);

  @Override
  public List<ReportChartModel> buildReportsData(String data, ReportCompareField compareField,
                                                 Map<String, String> filters)
      throws Exception {
    List<Report> reports = constructReportList(data);
    if (reports != null) {
      List<ReportChartModel> reportData = new ArrayList<>(reports.size());
      SortedSet<String> tSet = new TreeSet<>();
      boolean isMatInvolved = filters.containsKey(QueryHelper.TOKEN + QueryHelper.QUERY_MATERIAL);
      boolean isKioskInvolved = filters.containsKey(QueryHelper.TOKEN + QueryHelper.QUERY_ENTITY);
      for (Report report : reports) {
        ReportChartModel m = new ReportChartModel();
        m.label = getReportLabel(filters, report);
        tSet.add(m.label);
        m.value = getReportValues(report, compareField, isMatInvolved, isKioskInvolved);
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
      boolean isMatInvolved = (viewType == ReportViewType.BY_MATERIAL
          || model.filters.containsKey(QueryHelper.TOKEN + QueryHelper.QUERY_MATERIAL));
      boolean isKioskInvolved = (viewType == ReportViewType.BY_ENTITY
          || model.filters.containsKey(QueryHelper.TOKEN + QueryHelper.QUERY_ENTITY));
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
          tables.get(key).add(getReportValues(report, ReportCompareField.NONE,
              isMatInvolved, isKioskInvolved));
        }
      }
      response.table = tables;
      return response;
    }
    return null;
  }

  @Override
  public String getColumns(Map<String, String> filters, ReportViewType viewType) {
    return "ic, rc, sc, wc, trc, tc, iec, rec, scec, wec, trec, tec, lic"
        + getSelectiveColumns(filters, viewType);
  }

  private String getSelectiveColumns(Map<String, String> filters, ReportViewType viewType) {
    String selectiveColumns = CharacterConstants.EMPTY;
    if (!filters.containsKey(QueryHelper.TOKEN + QueryHelper.QUERY_ENTITY)
        && viewType != ReportViewType.BY_ENTITY
        && !filters.containsKey(QueryHelper.TOKEN + QueryHelper.QUERY_MATERIAL)
        && viewType != ReportViewType.BY_MATERIAL) {
      selectiveColumns = ",lkc";
    }
    return selectiveColumns;
  }

  @Override
  public String getTableColumns(Map<String, String> filters, ReportViewType viewType) {
    switch (viewType) {
      case BY_ENTITY:
        return "ic, rc, sc, wc, trc, tc" + getSelectiveColumns(filters, viewType);
      default:
        return getColumns(filters, viewType);
    }
  }

  public List<ReportDataModel> getReportValues(
      Report report, ReportCompareField compareField, boolean isMatInvolved,
      boolean isKioskInvolved)
      throws ServiceException {
    List<ReportDataModel> values = new ArrayList<>();
    setCompareField(report, compareField, values);
    Long denominator = report.getLiveKioskCount();
    if (isMatInvolved) {
      denominator = report.getLiveInventoryCount();
    } else if (isKioskInvolved) {
      denominator = 1L;
    }
    //dummy placeholder for all transaction counts. Will be populated in UI.
    values.add(addData(report.getTransactionCount()));
    if (denominator == null || denominator == 0) {
      values.add(addData(ZERO));
      values.add(addData(ZERO));
    } else {
      values.add(addData((float)report.getTransactionCount() / denominator,
          report.getTransactionCount(), denominator));
      values.add(
          addData(Math.min(100 * (float) report.getTransactionEntityCount() / denominator, 100),
              report.getTransactionEntityCount(), denominator));
    }

    values.add(addData(report.getIssueCount()));
    if (denominator == null || denominator == 0) {
      values.add(addData(ZERO));
      values.add(addData(ZERO));
    } else {
      values.add(
          addData((float) report.getIssueCount() / denominator, report.getIssueCount(),
              denominator));
      values.add(addData(Math.min(((float) report.getIssueEntityCount() / denominator) * 100, 100),
          report.getIssueEntityCount(), denominator));
    }

    values.add(addData(report.getReceiptCount()));
    if (denominator == null || denominator == 0) {
      values.add(addData(ZERO));
      values.add(addData(ZERO));
    } else {
      values.add(
          addData((float) report.getReceiptCount() / denominator, report.getReceiptCount(),
              denominator));
      values
          .add(addData(Math.min(((float) report.getReceiptEntityCount() / denominator) * 100, 100),
              report.getReceiptEntityCount(), denominator));
    }

    values.add(addData(report.getStockCountCount()));
    if (denominator == null || denominator == 0) {
      values.add(addData(ZERO));
      values.add(addData(ZERO));
    } else {
      values.add(addData((float) report.getStockCountCount() / denominator,
          report.getStockCountCount(), denominator));
      values.add(
          addData(Math.min(((float) report.getStockCountEntityCount() / denominator) * 100, 100),
              report.getStockCountEntityCount(), denominator));
    }

    values.add(addData(report.getDiscardCount()));
    if (denominator == null || denominator == 0) {
      values.add(addData(ZERO));
      values.add(addData(ZERO));
    } else {
      values.add(addData((float) report.getDiscardCount() / denominator, report.getDiscardCount(), denominator));
      values.add(
          addData(Math.min(((float) report.getDiscardEntityCount() / denominator) * 100, 100), report.getDiscardEntityCount(), denominator));
    }

    values.add(addData(report.getTransferCount()));
    if (denominator == null || denominator == 0) {
      values.add(addData(ZERO));
      values.add(addData(ZERO));
    } else {
      values.add(
          addData((float) report.getTransferCount() / denominator, report.getTransferCount(),
              denominator));
      values
          .add(addData(Math.min(((float) report.getTransferEntityCount() / denominator) * 100, 100),
              report.getTransferEntityCount(), denominator));
    }
    return values;
  }

}

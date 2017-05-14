package com.logistimo.api.builders;

import com.google.common.collect.TreeBasedTable;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.logistimo.api.models.ReportChartModel;
import com.logistimo.api.models.ReportDataModel;
import com.logistimo.api.models.TableResponse;
import com.logistimo.config.models.AssetSystemConfig;
import com.logistimo.config.models.ConfigurationException;
import com.logistimo.constants.CharacterConstants;
import com.logistimo.entities.entity.IKiosk;
import com.logistimo.entities.service.EntitiesService;
import com.logistimo.entities.service.EntitiesServiceImpl;
import com.logistimo.logger.XLog;
import com.logistimo.materials.service.MaterialCatalogService;
import com.logistimo.materials.service.impl.MaterialCatalogServiceImpl;
import com.logistimo.reports.constants.ReportCompareField;
import com.logistimo.reports.constants.ReportType;
import com.logistimo.reports.constants.ReportViewType;
import com.logistimo.reports.plugins.Report;
import com.logistimo.reports.plugins.internal.QueryHelper;
import com.logistimo.reports.plugins.internal.QueryRequestModel;
import com.logistimo.services.ServiceException;
import com.logistimo.services.Services;
import com.logistimo.tags.TagUtil;
import com.logistimo.tags.entity.ITag;
import com.logistimo.utils.LocalDateUtil;
import org.apache.commons.lang.StringUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Field;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by mohan on 25/03/17.
 */
public class ReportsBuilder {

  private static final XLog xLogger = XLog.getLog(ReportsBuilder.class);

  private static final String ZERO = "0";
  private static final String ROWS = "rows";
  private static final String HEADINGS = "headings";
  private static final String LABEL_HEADINGS = "labelHeadings";
  private static final String ROW_HEADINGS = "rowHeadings";
  private static final String LONG = "Long";
  private static final String FLOAT = "Float";
  private static final String MAP = "Map";
  private static final String TABLE = "table";

  public List<ReportChartModel> buildReportsData(String data, ReportType reportType,
                                                 ReportCompareField compareField,
                                                 Map<String, String> filters)
      throws NoSuchFieldException, IllegalAccessException, ServiceException, ParseException {
    List<Report> reports = constructReportList(data);
    if (reports != null) {
      List<ReportChartModel> reportData = new ArrayList<>();
      SortedSet<String> tSet = new TreeSet<>();
      boolean isMatInvolved = filters.containsKey(QueryHelper.TOKEN + QueryHelper.QUERY_MATERIAL);
      boolean isKioskInvolved = filters.containsKey(QueryHelper.TOKEN + QueryHelper.QUERY_ENTITY);
      for (Report report : reports) {
        ReportChartModel m = new ReportChartModel();
        SimpleDateFormat dateFormat, labelDateFormat;
        switch (filters.get(QueryHelper.TOKEN_PERIODICITY)) {
          case QueryHelper.MONTH:
            dateFormat = new SimpleDateFormat(QueryHelper.DATE_FORMAT_MONTH);
            labelDateFormat = new SimpleDateFormat(QueryHelper.DATE_FORMAT_DAILY);
            m.label = labelDateFormat.format(dateFormat.parse(report.getTime()));
            break;
          default:
            m.label = report.getTime();
        }
        tSet.add(m.label);
        switch (reportType) {
          case INV_ABNORMAL_STOCK:
            m.value = getAbnormalReportValues(report, compareField, isMatInvolved, isKioskInvolved);
            break;
          case INV_REPLENISHMENT:
            m.value = getReplenishmentReportValues(report, compareField);
            break;
          case INV_TRANSACTION_COUNT:
            m.value =
                getTransactionCountReportValues(report, compareField, isMatInvolved, isKioskInvolved);
            break;
          case AS_ASSET_STATUS:
            m.value = getAssetStatusReportValues(report, compareField);
            break;
          default:
            m.value = null;
        }
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

  private List<Report> constructReportList(String data)
      throws NoSuchFieldException, IllegalAccessException {
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

  private Report constructReport(List<Field> fields, JSONArray row) throws IllegalAccessException {
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

  private List<ReportDataModel> getAbnormalReportValues(
      Report report, ReportCompareField compareField, boolean isMatInvolved, boolean isKioskInvolved)
      throws ServiceException {
    List<ReportDataModel> values = new ArrayList<>(25);
    setCompareField(report, compareField, values);
    //Frequency
    values.add(addData(report.getStockOutEventCount()));
    values.add(addData(report.getLessThanMinEventCount()));
    values.add(addData(report.getGreaterThanMaxEventCount()));
    Long denominator = report.getLiveKioskCount();
    if (isMatInvolved) {
      denominator = report.getLiveInventoryCount();
    } else if (isKioskInvolved) {
      denominator = 1L;
    }
    //Average Frequency
    if (denominator == null || denominator == 0) {
      values.add(addData(ZERO));
      values.add(addData(ZERO));
      values.add(addData(ZERO));
    } else {
      values.add(addData((float)report.getStockOutEventCount() / denominator,
          report.getStockOutEventCount(), denominator));
      values.add(addData((float)report.getLessThanMinEventCount() / denominator,
          report.getLessThanMinEventCount(), denominator));
      values.add(addData((float)report.getGreaterThanMaxEventCount() / denominator,
          report.getGreaterThanMaxEventCount(), denominator));
    }

    //Duration
    values.add(addData(getDays(report.getStockOutEventDuration())));
    values.add(addData(getDays(report.getLessThanMinEventDuration())));
    values.add(addData(getDays(report.getGreaterThanMaxEventDuration())));

    //Average duration
    if (report.getStockOutStateCount() == 0) {
      values.add(addData(ZERO));
    } else {
      values
          .add(addData(getDays(report.getStockOutEventDuration() / report.getStockOutStateCount()),
              getDays(report.getStockOutEventDuration()), report.getStockOutStateCount()));
    }
    if (report.getLessThanMinStateCount() == 0) {
      values.add(addData(ZERO));
    } else {
      values.add(
          addData(
              getDays(report.getLessThanMinEventDuration() / report.getLessThanMinStateCount()),
              getDays(report.getLessThanMinEventDuration()), report.getLessThanMinStateCount()));
    }
    if (report.getGreaterThanMaxStateCount() == 0) {
      values.add(addData(ZERO));
    } else {
      values.add(addData(
          getDays(report.getGreaterThanMaxEventDuration() / report.getGreaterThanMaxStateCount()),
          getDays(report.getGreaterThanMaxEventDuration()), report.getGreaterThanMaxStateCount()));
    }
    if (report.getLiveInventoryCount() == 0) {
      for (int i = 0; i < 12; i++) {
        values.add(addData(ZERO));
      }
    } else {
      values.add(
          addData(Math.min(
              100 * (float) report.getStockOutInventory100() / report.getLiveInventoryCount(),
              100)));
      values.add(addData(Math.min(100 * (float) report.getStockOutInventoryGreaterThan90() / report
          .getLiveInventoryCount(), 100), report.getStockOutInventoryGreaterThan90(),report
              .getLiveInventoryCount()));
      values.add(addData(Math.min(100 * (float) report.getStockOutInventoryGreaterThan80() / report
          .getLiveInventoryCount(), 100), report.getStockOutInventoryGreaterThan80(),report
              .getLiveInventoryCount()));
      values.add(addData(Math.min(100 * (float) report.getStockOutInventoryGreaterThan70() / report
          .getLiveInventoryCount(), 100), report.getStockOutInventoryGreaterThan70(), report
              .getLiveInventoryCount()));
      values
          .add(addData(
                  Math.min(100 * (float) report.getMinInventory100() / report.getLiveInventoryCount(),
                          100), report.getMinInventory100(), report.getLiveInventoryCount()));
      values.add(addData(Math.min(
          100 * (float) report.getMinInventoryGreaterThan90() / report.getLiveInventoryCount(),
          100), report.getMinInventoryGreaterThan90(), report.getLiveInventoryCount()));
      values.add(addData(Math.min(
          100 * (float) report.getMinInventoryGreaterThan80() / report.getLiveInventoryCount(),
          100), report.getMinInventoryGreaterThan80() , report.getLiveInventoryCount()));
      values.add(addData(Math.min(
          100 * (float) report.getMinInventoryGreaterThan70() / report.getLiveInventoryCount(),
          100), report.getMinInventoryGreaterThan70(), report.getLiveInventoryCount()));
      values.add(addData(
              Math.min(100 * (float) report.getMaxInventory100() / report.getLiveInventoryCount(),
                      100), report.getMaxInventory100(), report.getLiveInventoryCount()));
      values.add(addData(Math.min(
          100 * (float) report.getMaxInventoryGreaterThan90() / report.getLiveInventoryCount(),
          100), report.getMaxInventoryGreaterThan90(), report.getLiveInventoryCount()));
      values.add(addData(Math.min(
          100 * (float) report.getMaxInventoryGreaterThan80() / report.getLiveInventoryCount(),
          100), report.getMaxInventoryGreaterThan80(), report.getLiveInventoryCount()));
      values.add(addData(Math.min(
          100 * (float) report.getMaxInventoryGreaterThan70() / report.getLiveInventoryCount(),
          100), report.getMaxInventoryGreaterThan70(), report.getLiveInventoryCount()));
    }
    return values;
  }

  private List<ReportDataModel> getReplenishmentReportValues(
      Report report, ReportCompareField compareField) throws ServiceException {
    List<ReportDataModel> values = new ArrayList<>(4);
    setCompareField(report, compareField, values);
    if (report.getStockOutCount() == 0) {
      values.add(addData(ZERO));
    } else {
      values.add(addData(getDaysFromHours(report.getStockOutDuration()) / report.getStockOutCount(),
          getDaysFromHours(report.getStockOutDuration()), report.getStockOutCount()));
    }
    if (report.getLessThanMinCount() == 0) {
      values.add(addData(ZERO));
    } else {
      values.add(addData(
          getDaysFromHours(report.getLessThanMinDuration()) / report.getLessThanMinCount(),
          getDaysFromHours(report.getLessThanMinDuration()), report.getLessThanMinCount()));
    }
    if (report.getGreaterThanMaxCount() == 0) {
      values.add(addData(ZERO));
    } else {
      values.add(addData(
          getDaysFromHours(report.getGreaterThanMaxDuration()) / report.getGreaterThanMaxCount(),
          getDaysFromHours(report.getGreaterThanMaxDuration()), report.getGreaterThanMaxCount()));
    }
    return values;
  }

  private List<ReportDataModel> getTransactionCountReportValues(
      Report report, ReportCompareField compareField, boolean isMatInvolved, boolean isKioskInvolved)
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

  private List<ReportDataModel> getAssetStatusReportValues(
      Report report, ReportCompareField compareField) throws ServiceException {
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

  private void setCompareField(
      Report report, ReportCompareField compareField, List<ReportDataModel> values)
      throws ServiceException {
    switch (compareField) {
      case MATERIAL:
        MaterialCatalogService ms = Services.getService(MaterialCatalogServiceImpl.class);
        values.add(addData(ms.getMaterial(report.getMaterialId()).getName()));
        break;
      case MATERIAL_TAG:
        values.add(addData(TagUtil.getTagById(report.getMaterialTag(),ITag.MATERIAL_TAG)));
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

  public TableResponse buildReportTableData(
      String json, ReportType type, ReportViewType viewType, QueryRequestModel model)
      throws JSONException, NoSuchFieldException, IllegalAccessException, ServiceException,
      ParseException {
    JSONObject jsonObject = new JSONObject(json);
    if (!jsonObject.has(HEADINGS)) {
      xLogger.warn("No data found");
    }
    JSONArray headersJson = jsonObject.getJSONArray(HEADINGS);
    if (headersJson.length() < 3) {
      xLogger.warn("Insufficient data found. Expect to have at least 3 columns");
      return null;
    }
    jsonObject = constructTableBaseData(jsonObject, headersJson, model);

    TableResponse response = new TableResponse();
    JSONArray jsonArray = jsonObject.getJSONArray(HEADINGS);
    JSONArray labelJsonArray = jsonObject.getJSONArray(LABEL_HEADINGS);
    for (int i = 0; i < labelJsonArray.length(); i++) {
        response.headings.add(labelJsonArray.getString(i));
    }
    if (jsonObject.has(TABLE) && jsonObject.getJSONObject(TABLE).length() != 0) {
      List<Field> fields = new ArrayList<>(headersJson.length());
      for (int j = 0; j < headersJson.length(); j++) {
        fields.add(Report.class.getDeclaredField(headersJson.getString(j)));
      }
      JSONObject results = jsonObject.getJSONObject(TABLE);
      Map<String, List<List<ReportDataModel>>> tables = new HashMap<>(results.length());
      JSONArray dimensions = results.names();
      boolean isMatInvolved =
          (viewType == ReportViewType.BY_MATERIAL
              || model.filters.containsKey(QueryHelper.TOKEN + QueryHelper.QUERY_MATERIAL));
      boolean isKioskInvolved =
          (viewType == ReportViewType.BY_ENTITY
              || model.filters.containsKey(QueryHelper.TOKEN + QueryHelper.QUERY_ENTITY));
      for (int i = 0; i < dimensions.length(); i++) {
        JSONArray period = results.getJSONArray(dimensions.getString(i));
        String key = null;
        switch (viewType) {
          case BY_MATERIAL:
            MaterialCatalogService ms = Services.getService(MaterialCatalogServiceImpl.class);
            key = ms.getMaterial(Long.valueOf(dimensions.getString(i))).getName();
            key += CharacterConstants.PIPE + dimensions.getString(i);
            break;
          case BY_ENTITY_TAGS:
            key = TagUtil.getTagById(Long.valueOf(dimensions.getString(i)), ITag.KIOSK_TAG);
            break;
          case BY_MANUFACTURER:
            key = StringUtils.capitalize(dimensions.getString(i));
            break;
          case BY_ASSET_TYPE:
            AssetSystemConfig assets;
            try {
              assets = AssetSystemConfig.getInstance();
              key = assets.getAssetsNameByType(2).get(dimensions.getInt(i));
            } catch (ConfigurationException e) {
              xLogger.severe("Error in reading Asset System Configuration", e);
            }
            break;
          case BY_ENTITY:
            EntitiesService es = Services.getService(EntitiesServiceImpl.class);
            IKiosk k = es.getKiosk(Long.valueOf(dimensions.getString(i)), false);
            key = k.getName() + CharacterConstants.PIPE + dimensions.getString(i);
            key +=
                (StringUtils.isNotEmpty(k.getLocation()))
                    ? CharacterConstants.PIPE + k.getLocation()
                    : CharacterConstants.EMPTY;
            break;
          default:
            key = dimensions.getString(i);
        }
        tables.put(key, new ArrayList<List<ReportDataModel>>(period.length()));
        List<Report> reports = new ArrayList<>();
        for (int j = 0; j < period.length(); j++) {
          JSONArray row = period.getJSONArray(j);
          Report r = constructReport(fields.subList(2, fields.size()), row);
          r.setTime(jsonArray.getString(j + 1));
          reports.add(r);
        }
        for (Report report : reports) {
          switch (type) {
            case INV_ABNORMAL_STOCK:
              tables.get(key)
                  .add(getAbnormalReportValues(report, ReportCompareField.NONE, isMatInvolved, isKioskInvolved));
              break;
            case INV_REPLENISHMENT:
              tables.get(key).add(getReplenishmentReportValues(report, ReportCompareField.NONE));
              break;
            case INV_TRANSACTION_COUNT:
              tables.get(key)
                  .add(getTransactionCountReportValues(report, ReportCompareField.NONE, isMatInvolved, isKioskInvolved));
              break;
            case AS_ASSET_STATUS:
              tables.get(key).add(getAssetStatusReportValues(report, ReportCompareField.NONE));
              break;
          }
        }
      }
      response.table = tables;
      return response;
    }
    return null;
  }

  private JSONObject constructTableBaseData(
          JSONObject jsonObject, JSONArray headersJson, QueryRequestModel model) throws ParseException {
    JSONObject output = new JSONObject();
    TreeBasedTable<String, String, List<String>> treeBasedTable = TreeBasedTable.create();
    int dataSize = jsonObject.getJSONArray(HEADINGS).length() - 2;
    SimpleDateFormat labelDateFormat = new SimpleDateFormat(QueryHelper.DATE_FORMAT_DAILY);
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
        String date = rowsJson.getJSONArray(i).getString(1);
        if(QueryHelper.MONTH.equals(model.filters.get(QueryHelper.TOKEN_PERIODICITY))) {
          SimpleDateFormat dateFormat = new SimpleDateFormat(QueryHelper.DATE_FORMAT_MONTH);
          date = labelDateFormat.format(dateFormat.parse(date));
        }
        treeBasedTable.put(
            rowsJson.getJSONArray(i).getString(0), date, data);
      }
    }
    JSONArray rowHeadings = null;
    if (jsonObject.has(ROW_HEADINGS)) {
      rowHeadings = jsonObject.getJSONArray(ROW_HEADINGS);
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

  private List<ReportChartModel> fillChartData(
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

  private TreeBasedTable<String, String, List<String>> fillTable(
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

  private ReportDataModel addData(Object value) {
    return new ReportDataModel(value != null ? String.valueOf(value) : ZERO);
  }

  private ReportDataModel addData(Object value, Object numerator, Object denominator) {
    return new ReportDataModel(
        String.valueOf(value), String.valueOf(numerator), String.valueOf(denominator));
  }

  private Double getHours(Object milliseconds) {
    if (milliseconds == null) {
      return null;
    }
    return Double.parseDouble(String.valueOf(milliseconds)) / (60 * 60 * 1000);
  }

  private Double getDays(Object milliseconds) {
    if (milliseconds == null) {
      return null;
    }
    return Double.parseDouble(String.valueOf(milliseconds)) / (60 * 60 * 1000 * 24);
  }

  private Double getDaysFromHours(Object hours) {
    if (hours == null) {
      return null;
    }
    return Double.parseDouble(String.valueOf(hours)) / 24;
  }
}

package com.logistimo.api.controllers;

import com.logistimo.api.builders.ReportsBuilder;
import com.logistimo.api.models.ReportChartModel;
import com.logistimo.api.models.TableResponse;
import com.logistimo.api.security.SecurityMgr;
import com.logistimo.api.util.SessionMgr;
import com.logistimo.assets.service.AssetManagementService;
import com.logistimo.assets.service.impl.AssetManagementServiceImpl;
import com.logistimo.config.models.DomainConfig;
import com.logistimo.constants.CharacterConstants;
import com.logistimo.exception.BadRequestException;
import com.logistimo.logger.XLog;
import com.logistimo.reports.constants.ReportCompareField;
import com.logistimo.reports.constants.ReportType;
import com.logistimo.reports.constants.ReportViewType;
import com.logistimo.reports.plugins.config.ExternalServiceClientConfiguration;
import com.logistimo.reports.plugins.internal.ExternalServiceClient;
import com.logistimo.reports.plugins.internal.QueryHelper;
import com.logistimo.reports.plugins.internal.QueryRequestModel;
import com.logistimo.reports.utils.ReportHelper;
import com.logistimo.security.SecureUserDetails;
import com.logistimo.services.Services;
import org.apache.commons.lang.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.Days;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.json.JSONObject;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.Response;
import java.text.SimpleDateFormat;
import java.util.*;

/** Created by mohan on 02/03/17. */
@Controller
@RequestMapping("/plugins/report")
public class ReportPluginController {

  private static final XLog xLogger = XLog.getLog(ReportPluginController.class);
  private ReportsBuilder builder = new ReportsBuilder();

  private static final String JSON_REPORT_TYPE = "type";
  private static final String JSON_REPORT_VIEW_TYPE = "viewtype";
  private static final String JSON_REPORT_COMPARE = "compare";

  @RequestMapping(value = "/", method = RequestMethod.GET)
  public @ResponseBody
  List<ReportChartModel> getReportData(@RequestParam String json, HttpServletRequest request) {
    xLogger.fine("Entering getReportData");
    try {
      JSONObject jsonObject = new JSONObject(json);
      if (!jsonObject.has(JSON_REPORT_TYPE)) {
        xLogger.warn("Report type is mandatory.");
        throw new BadRequestException("Invalid request");
      }
      SecureUserDetails sUser = SecurityMgr.getUserDetails(request.getSession());
      String userId = sUser.getUsername();
      Long domainId = SessionMgr.getCurrentDomain(request.getSession(), userId);
      ExternalServiceClient externalServiceClient =
          new ExternalServiceClient(
              new ExternalServiceClientConfiguration(), ClientBuilder.newClient());

      ReportType type = ReportType.getType(jsonObject.getString(JSON_REPORT_TYPE));
      ReportCompareField compareField =
          ReportCompareField.getField(jsonObject.getString(JSON_REPORT_COMPARE));

      QueryRequestModel model = new QueryRequestModel();
      model.filters = QueryHelper.parseFilters(domainId, jsonObject);
      if (model.filters.containsKey(QueryHelper.TOKEN + QueryHelper.QUERY_CITY)) {
        model.filters.remove(QueryHelper.TOKEN + QueryHelper.QUERY_TALUK);
        model.filters.remove(QueryHelper.TOKEN + QueryHelper.QUERY_DISTRICT);
      }
      StringBuilder columns = new StringBuilder();
      columns.append(ReportHelper.getColumnsForReport(type));
      String selectiveFilters = QueryHelper.getSelectiveFilters(model.filters,type,ReportViewType.OVERVIEW);
      if(StringUtils.isNotEmpty(selectiveFilters)){
        columns.append(CharacterConstants.COMMA).append(selectiveFilters);
      }
      model.filters.put(QueryHelper.TOKEN_COLUMNS, columns.toString());
      model.queryId = QueryHelper.getQueryID(model.filters, type);
      Response response = externalServiceClient.postRequest(model);
        return builder.buildReportsData(
              response.readEntity(String.class), type, compareField, model.filters);
    } catch (Exception e) {
      xLogger.severe("Error while getting the data", e);
      return null;
    }
  }

  @RequestMapping(value = "/breakdown", method = RequestMethod.GET)
  public @ResponseBody
  TableResponse getReportTableData(
      @RequestParam String json, HttpServletRequest request) {
    xLogger.fine("Entering getReportData");
    try {
      JSONObject jsonObject = new JSONObject(json);
      if (!jsonObject.has(JSON_REPORT_TYPE) || !jsonObject.has(JSON_REPORT_VIEW_TYPE)) {
        xLogger.warn("Both report type and view type is mandatory.");
        throw new BadRequestException("Invalid request");
      }
      ReportType type = ReportType.getType(jsonObject.getString(JSON_REPORT_TYPE));
      ReportViewType viewType =
          ReportViewType.getViewType(jsonObject.getString(JSON_REPORT_VIEW_TYPE));
      if (viewType == null) {
        xLogger.warn(
            "Invalid report view type found {0}", jsonObject.getString(JSON_REPORT_VIEW_TYPE));
        throw new BadRequestException("Invalid request");
      }

      SecureUserDetails sUser = SecurityMgr.getUserDetails(request.getSession());
      String userId = sUser.getUsername();
      Long domainId = SessionMgr.getCurrentDomain(request.getSession(), userId);
      ExternalServiceClient externalServiceClient =
          new ExternalServiceClient(
              new ExternalServiceClientConfiguration(), ClientBuilder.newClient());

      QueryRequestModel model = new QueryRequestModel();
      model.filters = QueryHelper.parseFilters(domainId, jsonObject);
      if (model.filters.containsKey(QueryHelper.TOKEN + QueryHelper.QUERY_CITY)) {
        model.filters.remove(QueryHelper.TOKEN + QueryHelper.QUERY_TALUK);
        model.filters.remove(QueryHelper.TOKEN + QueryHelper.QUERY_DISTRICT);
      }
      switch (jsonObject.getString(QueryHelper.PERIODICITY)) {
        case QueryHelper.PERIODICITY_MONTH:
          SimpleDateFormat format = new SimpleDateFormat("yyyy-MM");
          Calendar fromDate = new GregorianCalendar();
          fromDate.setTime(format.parse(model.filters.get(QueryHelper.TOKEN_START_TIME)));
          Calendar toDate = new GregorianCalendar();
          toDate.setTime(format.parse(model.filters.get(QueryHelper.TOKEN_END_TIME)));
          int diffYear = toDate.get(Calendar.YEAR) - fromDate.get(Calendar.YEAR);
          int diffMonth = diffYear * 12 + toDate.get(Calendar.MONTH) - fromDate.get(Calendar.MONTH);
          if (diffMonth > QueryHelper.MONTHS_LIMIT) {
            toDate.add(Calendar.MONTH, -1*(QueryHelper.MONTHS_LIMIT-1));
            model.filters.put(QueryHelper.TOKEN_START_TIME, format.format(toDate.getTime()));
          }
          break;
        case QueryHelper.PERIODICITY_DAY:
          DateTimeFormatter mDateTimeFormatter = DateTimeFormat.forPattern(QueryHelper.DATE_FORMAT_DAILY);
          DateTime fromTime = mDateTimeFormatter.parseDateTime(model.filters.get(QueryHelper.TOKEN_START_TIME));
          DateTime toTime = mDateTimeFormatter.parseDateTime(model.filters.get(QueryHelper.TOKEN_END_TIME));
          if (Days.daysBetween(fromTime, toTime).getDays() > QueryHelper.DAYS_LIMIT) {
            model.filters.put(QueryHelper.TOKEN_START_TIME,
                    mDateTimeFormatter.print(toTime.minusDays(QueryHelper.DAYS_LIMIT-1)));
          }
          break;
        default:
      }
      StringBuilder columns = new StringBuilder();
      columns.append(ReportHelper.getColumnsForTableReport(type,viewType));
      String selectiveFilters = QueryHelper.getSelectiveFilters(model.filters,type,viewType);
      if(StringUtils.isNotEmpty(selectiveFilters)){
          columns.append(CharacterConstants.COMMA).append(selectiveFilters);
      }
      model.filters.put(QueryHelper.TOKEN_COLUMNS, columns.toString());
      AssetManagementService as;
      Map<String,String> retainFilters = new HashMap<>();
      switch (viewType) {
        case BY_MATERIAL:
          model.filters.put(QueryHelper.TOKEN + QueryHelper.QUERY_MATERIAL, null);
          if(model.filters.containsKey(QueryHelper.TOKEN + QueryHelper.QUERY_MATERIAL_TAG)){
              retainFilters.put(QueryHelper.TOKEN + QueryHelper.QUERY_MATERIAL_TAG,
                      model.filters.remove(QueryHelper.TOKEN + QueryHelper.QUERY_MATERIAL_TAG));
          }
          break;
        case BY_ENTITY:
          model.filters.put(QueryHelper.TOKEN + QueryHelper.QUERY_ENTITY, null);
          if(model.filters.containsKey(QueryHelper.TOKEN + QueryHelper.QUERY_ENTITY_TAG)){
              retainFilters.put(QueryHelper.TOKEN + QueryHelper.QUERY_ENTITY_TAG,
                      model.filters.remove(QueryHelper.TOKEN + QueryHelper.QUERY_ENTITY_TAG));
          }
          if(model.filters.containsKey(QueryHelper.TOKEN + QueryHelper.QUERY_COUNTRY)){
              retainFilters.put(QueryHelper.TOKEN + QueryHelper.QUERY_COUNTRY,
                      model.filters.remove(QueryHelper.TOKEN + QueryHelper.QUERY_COUNTRY));
          }
          if(model.filters.containsKey(QueryHelper.TOKEN + QueryHelper.QUERY_STATE)){
              retainFilters.put(QueryHelper.TOKEN + QueryHelper.QUERY_STATE,
                      model.filters.remove(QueryHelper.TOKEN + QueryHelper.QUERY_STATE));
          }
          if(model.filters.containsKey(QueryHelper.TOKEN + QueryHelper.QUERY_DISTRICT)){
              retainFilters.put(QueryHelper.TOKEN + QueryHelper.QUERY_DISTRICT,
                      model.filters.remove(QueryHelper.TOKEN + QueryHelper.QUERY_DISTRICT));
          }
          if(model.filters.containsKey(QueryHelper.TOKEN + QueryHelper.QUERY_TALUK)){
              retainFilters.put(QueryHelper.TOKEN + QueryHelper.QUERY_TALUK,
                      model.filters.remove(QueryHelper.TOKEN + QueryHelper.QUERY_TALUK));
          }
          if(model.filters.containsKey(QueryHelper.TOKEN + QueryHelper.QUERY_CITY)){
              retainFilters.put(QueryHelper.TOKEN + QueryHelper.QUERY_CITY,
                      model.filters.remove(QueryHelper.TOKEN + QueryHelper.QUERY_CITY));
          }
          break;
        case BY_REGION:
          try {
            DomainConfig dc = DomainConfig.getInstance(domainId);
            if (StringUtils.isNotEmpty(dc.getCountry())) {
              if (StringUtils.isNotEmpty(dc.getState())) {
                if (StringUtils.isNotEmpty(dc.getDistrict())) {
                  model.filters.put(QueryHelper.TOKEN_LOCATION, QueryHelper.LOCATION_TALUK);
                  model.filters.put(QueryHelper.TOKEN + QueryHelper.QUERY_TALUK, null);
                  model.filters.put(QueryHelper.TOKEN + QueryHelper.QUERY_DISTRICT, null);
                  model.filters.put(QueryHelper.TOKEN + QueryHelper.QUERY_STATE, null);
                  model.filters.put(QueryHelper.TOKEN + QueryHelper.QUERY_COUNTRY, null);
                } else {
                  model.filters.put(QueryHelper.TOKEN_LOCATION, QueryHelper.LOCATION_DISTRICT);
                  model.filters.put(QueryHelper.TOKEN + QueryHelper.QUERY_DISTRICT, null);
                  model.filters.put(QueryHelper.TOKEN + QueryHelper.QUERY_STATE, null);
                  model.filters.put(QueryHelper.TOKEN + QueryHelper.QUERY_COUNTRY, null);
                }
              } else {
                model.filters.put(QueryHelper.TOKEN_LOCATION, QueryHelper.LOCATION_STATE);
                model.filters.put(QueryHelper.TOKEN + QueryHelper.QUERY_STATE, null);
                model.filters.put(QueryHelper.TOKEN + QueryHelper.QUERY_COUNTRY, null);
              }
            } else {
              model.filters.put(QueryHelper.TOKEN_LOCATION, QueryHelper.LOCATION_COUNTRY);
              model.filters.put(QueryHelper.TOKEN + QueryHelper.QUERY_COUNTRY, null);
            }
          } catch (Exception e) {
            xLogger.warn("Exception in replacing location token", e);
          }
          break;
        case BY_MODEL:
          model.filters.put(QueryHelper.TOKEN + QueryHelper.QUERY_DMODEL, null);
          break;
        case BY_ASSET:
          as = Services.getService(AssetManagementServiceImpl.class);
          model.filters.put(QueryHelper.TOKEN + QueryHelper.QUERY_DVID, as.getMonitoredAssetIdsForReport(model.filters));
          break;
        case BY_ENTITY_TAGS:
          model.filters.put(QueryHelper.TOKEN + QueryHelper.QUERY_ENTITY_TAG, null);
          break;
        case BY_MANUFACTURER:
          as = Services.getService(AssetManagementServiceImpl.class);
          model.filters.put(QueryHelper.TOKEN + QueryHelper.QUERY_VENDOR_ID, as.getVendorIdsForReports(
                  model.filters.get(QueryHelper.TOKEN + QueryHelper.QUERY_DOMAIN)));
          break;
        case BY_ASSET_TYPE:
          model.filters.remove(QueryHelper.TOKEN + QueryHelper.QUERY_MTYPE);
          as = Services.getService(AssetManagementServiceImpl.class);
          model.filters.put(QueryHelper.TOKEN + QueryHelper.QUERY_ATYPE, as.getAssetTypesForReports(
                  model.filters.get(QueryHelper.TOKEN + QueryHelper.QUERY_DOMAIN),"1")); // only monitored
          break;
      }
      model.queryId = viewType.toString().toUpperCase() + CharacterConstants.UNDERSCORE
                  + QueryHelper.getQueryID(model.filters, type);
      if(viewType.toString().equals(ReportViewType.BY_ASSET.toString())){
        model.queryId = "DID_DVID";
      }
      switch (viewType) {
        case BY_MATERIAL:
          model.filters.remove(QueryHelper.TOKEN + QueryHelper.QUERY_MATERIAL);
          if (retainFilters.containsKey(QueryHelper.TOKEN + QueryHelper.QUERY_MATERIAL_TAG)) {
            model.filters.put(QueryHelper.TOKEN + QueryHelper.QUERY_MATERIAL + CharacterConstants.UNDERSCORE
                    + QueryHelper.QUERY, QueryHelper.QUERY_MATERIAL + CharacterConstants.UNDERSCORE
                    + QueryHelper.QUERY_MATERIAL_TAG + CharacterConstants.UNDERSCORE + QueryHelper.QUERY);
            model.filters.put(QueryHelper.TOKEN + QueryHelper.QUERY_MATERIAL_TAG,
                retainFilters.get(QueryHelper.TOKEN + QueryHelper.QUERY_MATERIAL_TAG));
          } else {
            model.filters.put(QueryHelper.TOKEN + QueryHelper.QUERY_MATERIAL + CharacterConstants.UNDERSCORE
                    + QueryHelper.QUERY, QueryHelper.QUERY_MATERIAL + CharacterConstants.UNDERSCORE + QueryHelper.QUERY);
          }
          break;
        case BY_ENTITY:
          model.filters.remove(QueryHelper.TOKEN + QueryHelper.QUERY_ENTITY);
          String locationType = "";
          if (retainFilters.containsKey(QueryHelper.TOKEN + QueryHelper.QUERY_COUNTRY)) {
            locationType = QueryHelper.QUERY_COUNTRY;
            model.filters.put(
                QueryHelper.TOKEN + QueryHelper.QUERY_COUNTRY,
                retainFilters.get(QueryHelper.TOKEN + QueryHelper.QUERY_COUNTRY));
          }
          if (retainFilters.containsKey(QueryHelper.TOKEN + QueryHelper.QUERY_STATE)) {
            locationType = QueryHelper.QUERY_STATE;
            model.filters.put(
                QueryHelper.TOKEN + QueryHelper.QUERY_STATE,
                retainFilters.get(QueryHelper.TOKEN + QueryHelper.QUERY_STATE));
          }
          if (retainFilters.containsKey(QueryHelper.TOKEN + QueryHelper.QUERY_DISTRICT)) {
            locationType = QueryHelper.QUERY_DISTRICT;
            model.filters.put(
                QueryHelper.TOKEN + QueryHelper.QUERY_DISTRICT,
                retainFilters.get(QueryHelper.TOKEN + QueryHelper.QUERY_DISTRICT));
          }
          if (retainFilters.containsKey(QueryHelper.TOKEN + QueryHelper.QUERY_TALUK)) {
            locationType = QueryHelper.QUERY_TALUK;
            model.filters.put(
                QueryHelper.TOKEN + QueryHelper.QUERY_TALUK,
                retainFilters.get(QueryHelper.TOKEN + QueryHelper.QUERY_TALUK));
          }
          if (retainFilters.containsKey(QueryHelper.TOKEN + QueryHelper.QUERY_CITY)) {
            locationType = QueryHelper.QUERY_CITY;
            model.filters.put(
                QueryHelper.TOKEN + QueryHelper.QUERY_CITY,
                retainFilters.get(QueryHelper.TOKEN + QueryHelper.QUERY_CITY));
          }
          if (StringUtils.isNotEmpty(locationType)) {
            locationType = CharacterConstants.UNDERSCORE + locationType;
          }
          if (retainFilters.containsKey(QueryHelper.TOKEN + QueryHelper.QUERY_ENTITY_TAG)) {
            model.filters.put(
                QueryHelper.TOKEN
                    + QueryHelper.QUERY_ENTITY
                    + CharacterConstants.UNDERSCORE
                    + QueryHelper.QUERY,
                QueryHelper.QUERY_ENTITY
                    + CharacterConstants.UNDERSCORE
                    + QueryHelper.QUERY_ENTITY_TAG
                    + locationType
                    + CharacterConstants.UNDERSCORE
                    + QueryHelper.QUERY);
            model.filters.put(
                QueryHelper.TOKEN + QueryHelper.QUERY_ENTITY_TAG,
                retainFilters.get(QueryHelper.TOKEN + QueryHelper.QUERY_ENTITY_TAG));
          } else if(StringUtils.isNotEmpty(locationType)){
              model.filters.put(
                      QueryHelper.TOKEN
                              + QueryHelper.QUERY_ENTITY
                              + CharacterConstants.UNDERSCORE
                              + QueryHelper.QUERY,
                      QueryHelper.QUERY_ENTITY
                              + locationType
                              + CharacterConstants.UNDERSCORE
                              + QueryHelper.QUERY);
          } else {
            model.filters.put(
                QueryHelper.TOKEN
                    + QueryHelper.QUERY_ENTITY
                    + CharacterConstants.UNDERSCORE
                    + QueryHelper.QUERY,
                QueryHelper.QUERY_ENTITY + CharacterConstants.UNDERSCORE + QueryHelper.QUERY);
          }
          break;
        case BY_REGION:
          model.filters.remove(QueryHelper.TOKEN + QueryHelper.QUERY_COUNTRY);
          model.filters.remove(QueryHelper.TOKEN + QueryHelper.QUERY_STATE);
          model.filters.remove(QueryHelper.TOKEN + QueryHelper.QUERY_DISTRICT);
          model.filters.remove(QueryHelper.TOKEN + QueryHelper.QUERY_TALUK);
          break;
        case BY_MODEL:
          model.filters.remove(QueryHelper.TOKEN + QueryHelper.QUERY_DMODEL);
          break;
        case BY_ENTITY_TAGS:
          model.filters.remove(QueryHelper.TOKEN + QueryHelper.QUERY_ENTITY_TAG);
          break;
      }

      Response response = externalServiceClient.postRequest(model);
      return builder.buildReportTableData(response.readEntity(String.class), type, viewType, model);
    } catch (Exception e) {
      xLogger.severe("Error while getting the data", e);
      return null;
    }
  }
}

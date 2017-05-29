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

import com.logistimo.assets.service.AssetManagementService;
import com.logistimo.assets.service.impl.AssetManagementServiceImpl;
import com.logistimo.config.models.DomainConfig;
import com.logistimo.constants.CharacterConstants;
import com.logistimo.exception.BadRequestException;
import com.logistimo.logger.XLog;
import com.logistimo.reports.constants.ReportCompareField;
import com.logistimo.reports.constants.ReportViewType;
import com.logistimo.reports.plugins.internal.ExternalServiceClient;
import com.logistimo.reports.plugins.internal.QueryHelper;
import com.logistimo.reports.plugins.internal.QueryRequestModel;
import com.logistimo.reports.plugins.models.ReportChartModel;
import com.logistimo.reports.plugins.models.TableResponseModel;
import com.logistimo.services.Service;
import com.logistimo.services.ServiceException;
import com.logistimo.services.Services;

import org.apache.commons.lang.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.ws.rs.core.Response;

/**
 * @author Mohan Raja
 */
@org.springframework.stereotype.Service
public class ReportPluginService implements Service {

  private static final XLog xLogger = XLog.getLog(ReportPluginService.class);

  private static final String JSON_REPORT_TYPE = "type";
  private static final String JSON_REPORT_COMPARE = "compare";
  private static final String JSON_REPORT_VIEW_TYPE = "viewtype";

  @Autowired ReportServiceCollection reportServiceCollection;

  public List<ReportChartModel> getReportData(Long domainId, String json) {
    try {
      JSONObject jsonObject = new JSONObject(json);
      ExternalServiceClient externalServiceClient = ExternalServiceClient.getNewInstance();

      ReportCompareField compareField =
          ReportCompareField.getField(jsonObject.getString(JSON_REPORT_COMPARE));

      QueryRequestModel model = new QueryRequestModel();
      model.filters = QueryHelper.parseFilters(domainId, jsonObject);
      if (model.filters.containsKey(QueryHelper.TOKEN + QueryHelper.QUERY_CITY)) {
        model.filters.remove(QueryHelper.TOKEN + QueryHelper.QUERY_TALUK);
        model.filters.remove(QueryHelper.TOKEN + QueryHelper.QUERY_DISTRICT);
      }
      final String type = jsonObject.getString(JSON_REPORT_TYPE);
      final IReportService reportBuilder =
          reportServiceCollection.getReportService(type);

      model.filters.put(QueryHelper.TOKEN_COLUMNS,
          reportBuilder.getColumns(model.filters, ReportViewType.OVERVIEW));

      model.queryId = QueryHelper.getQueryID(model.filters,type);
      Response response = externalServiceClient.postRequest(model);
      return reportBuilder.buildReportsData(response.readEntity(String.class), compareField,
          model.filters);
    } catch (Exception e) {
      xLogger.severe("Error while getting the report data", e);
      return null;
    }
  }

  public TableResponseModel getReportTableData(Long domainId, String json) {
    try {
      JSONObject jsonObject = new JSONObject(json);
      ReportViewType viewType =
          ReportViewType.getViewType(jsonObject.getString(JSON_REPORT_VIEW_TYPE));

      if (viewType == null) {
        xLogger.warn(
            "Invalid report view type found {0}", jsonObject.getString(JSON_REPORT_VIEW_TYPE));
        throw new BadRequestException("Invalid request");
      }


      QueryRequestModel model = new QueryRequestModel();
      model.filters = QueryHelper.parseFilters(domainId, jsonObject);
      if (model.filters.containsKey(QueryHelper.TOKEN + QueryHelper.QUERY_CITY)) {
        model.filters.remove(QueryHelper.TOKEN + QueryHelper.QUERY_TALUK);
        model.filters.remove(QueryHelper.TOKEN + QueryHelper.QUERY_DISTRICT);
      }

      String startTime = getReportTableStartTime(jsonObject, model.filters.get(QueryHelper.TOKEN_END_TIME));
      if(StringUtils.isNotEmpty(startTime)) {
        model.filters.put(QueryHelper.TOKEN_START_TIME,startTime);
      }

      final String type = jsonObject.getString(JSON_REPORT_TYPE);
      final IReportService reportBuilder =
          reportServiceCollection.getReportService(type);
      model.filters.put(QueryHelper.TOKEN_COLUMNS, reportBuilder.getTableColumns(model.filters,
          viewType));

      Map<String, String> retainFilters = new HashMap<>();

      prepareFilters(domainId, viewType, model, retainFilters);

      model.queryId = viewType.toString().toUpperCase() + CharacterConstants.UNDERSCORE
          + QueryHelper.getQueryID(model.filters, type);
      if (viewType.toString().equals(ReportViewType.BY_ASSET.toString())) {
        model.queryId = "DID_DVID";
      }

      finaliseFilters(viewType, model, retainFilters);

      ExternalServiceClient externalServiceClient = ExternalServiceClient.getNewInstance();
      Response response = externalServiceClient.postRequest(model);
      return reportBuilder.buildReportTableData(response.readEntity(String.class), viewType, model);
    }catch (Exception e) {
      return null;
    }
  }

  private void finaliseFilters(ReportViewType viewType, QueryRequestModel model,
                               Map<String, String> retainFilters) {
    switch (viewType) {
      case BY_MATERIAL:
        finaliseFilterByMaterial(model, retainFilters);
        break;
      case BY_ENTITY:
        finaliseFilterByEntity(model, retainFilters);
        break;
      case BY_ENTITY_TAGS:
        model.filters.remove(QueryHelper.TOKEN + QueryHelper.QUERY_ENTITY_TAG);
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
    }
  }

  private void finaliseFilterByEntity(QueryRequestModel model, Map<String, String> retainFilters) {
    model.filters.remove(QueryHelper.TOKEN + QueryHelper.QUERY_ENTITY);
    String locationType = CharacterConstants.EMPTY;
    if (retainFilters.containsKey(QueryHelper.TOKEN + QueryHelper.QUERY_COUNTRY)) {
      locationType = QueryHelper.QUERY_COUNTRY;
      model.filters.put(QueryHelper.TOKEN + QueryHelper.QUERY_COUNTRY,
          retainFilters.get(QueryHelper.TOKEN + QueryHelper.QUERY_COUNTRY));
    }
    if (retainFilters.containsKey(QueryHelper.TOKEN + QueryHelper.QUERY_STATE)) {
      locationType = QueryHelper.QUERY_STATE;
      model.filters.put(QueryHelper.TOKEN + QueryHelper.QUERY_STATE,
          retainFilters.get(QueryHelper.TOKEN + QueryHelper.QUERY_STATE));
    }
    if (retainFilters.containsKey(QueryHelper.TOKEN + QueryHelper.QUERY_DISTRICT)) {
      locationType = QueryHelper.QUERY_DISTRICT;
      model.filters.put(QueryHelper.TOKEN + QueryHelper.QUERY_DISTRICT,
          retainFilters.get(QueryHelper.TOKEN + QueryHelper.QUERY_DISTRICT));
    }
    if (retainFilters.containsKey(QueryHelper.TOKEN + QueryHelper.QUERY_TALUK)) {
      locationType = QueryHelper.QUERY_TALUK;
      model.filters.put(QueryHelper.TOKEN + QueryHelper.QUERY_TALUK,
          retainFilters.get(QueryHelper.TOKEN + QueryHelper.QUERY_TALUK));
    }
    if (retainFilters.containsKey(QueryHelper.TOKEN + QueryHelper.QUERY_CITY)) {
      locationType = QueryHelper.QUERY_CITY;
      model.filters.put(QueryHelper.TOKEN + QueryHelper.QUERY_CITY,
          retainFilters.get(QueryHelper.TOKEN + QueryHelper.QUERY_CITY));
    }
    if (StringUtils.isNotEmpty(locationType)) {
      locationType = CharacterConstants.UNDERSCORE + locationType;
    }
    if (retainFilters.containsKey(QueryHelper.TOKEN + QueryHelper.QUERY_ENTITY_TAG)) {
      model.filters.put(QueryHelper.TOKEN + QueryHelper.QUERY_ENTITY
              + CharacterConstants.UNDERSCORE + QueryHelper.QUERY,
          QueryHelper.QUERY_ENTITY + CharacterConstants.UNDERSCORE + QueryHelper.QUERY_ENTITY_TAG
              + locationType + CharacterConstants.UNDERSCORE + QueryHelper.QUERY);
      model.filters.put(QueryHelper.TOKEN + QueryHelper.QUERY_ENTITY_TAG,
          retainFilters.get(QueryHelper.TOKEN + QueryHelper.QUERY_ENTITY_TAG));
    } else if (StringUtils.isNotEmpty(locationType)) {
      model.filters.put(QueryHelper.TOKEN + QueryHelper.QUERY_ENTITY
              + CharacterConstants.UNDERSCORE + QueryHelper.QUERY,
          QueryHelper.QUERY_ENTITY + locationType + CharacterConstants.UNDERSCORE
              + QueryHelper.QUERY);
    } else {
      model.filters.put(QueryHelper.TOKEN + QueryHelper.QUERY_ENTITY
              + CharacterConstants.UNDERSCORE + QueryHelper.QUERY,
          QueryHelper.QUERY_ENTITY + CharacterConstants.UNDERSCORE + QueryHelper.QUERY);
    }
  }

  private void finaliseFilterByMaterial(QueryRequestModel model,
                                        Map<String, String> retainFilters) {
    model.filters.remove(QueryHelper.TOKEN + QueryHelper.QUERY_MATERIAL);
    if (retainFilters.containsKey(QueryHelper.TOKEN + QueryHelper.QUERY_MATERIAL_TAG)) {
      model.filters.put(QueryHelper.TOKEN + QueryHelper.QUERY_MATERIAL
              + CharacterConstants.UNDERSCORE + QueryHelper.QUERY,
          QueryHelper.QUERY_MATERIAL + CharacterConstants.UNDERSCORE
              + QueryHelper.QUERY_MATERIAL_TAG + CharacterConstants.UNDERSCORE
              + QueryHelper.QUERY);
      model.filters.put(QueryHelper.TOKEN + QueryHelper.QUERY_MATERIAL_TAG,
          retainFilters.get(QueryHelper.TOKEN + QueryHelper.QUERY_MATERIAL_TAG));
    } else {
      model.filters
          .put(QueryHelper.TOKEN + QueryHelper.QUERY_MATERIAL + CharacterConstants.UNDERSCORE
                  + QueryHelper.QUERY,
              QueryHelper.QUERY_MATERIAL + CharacterConstants.UNDERSCORE + QueryHelper.QUERY);
    }
  }

  private void prepareFilters(Long domainId, ReportViewType viewType,
                              QueryRequestModel model, Map<String, String> retainFilters)
      throws ServiceException {
    AssetManagementService as;
    switch (viewType) {
      case BY_MATERIAL:
        prepareFiltersByMaterial(model, retainFilters);
        break;
      case BY_ENTITY:
        perpareFiltersByEntity(model, retainFilters);
        break;
      case BY_REGION:
        prepareFiltersByRegion(domainId, model);
        break;
      case BY_MODEL:
        model.filters.put(QueryHelper.TOKEN + QueryHelper.QUERY_DMODEL, null);
        break;
      case BY_ASSET:
        as = Services.getService(AssetManagementServiceImpl.class);
        model.filters.put(QueryHelper.TOKEN + QueryHelper.QUERY_DVID,
            as.getMonitoredAssetIdsForReport(model.filters));
        break;
      case BY_ENTITY_TAGS:
        model.filters.put(QueryHelper.TOKEN + QueryHelper.QUERY_ENTITY_TAG, null);
        break;
      case BY_MANUFACTURER:
        as = Services.getService(AssetManagementServiceImpl.class);
        model.filters.put(QueryHelper.TOKEN + QueryHelper.QUERY_VENDOR_ID,
            as.getVendorIdsForReports(
                model.filters.get(QueryHelper.TOKEN + QueryHelper.QUERY_DOMAIN)));
        break;
      case BY_ASSET_TYPE:
        model.filters.remove(QueryHelper.TOKEN + QueryHelper.QUERY_MTYPE);
        as = Services.getService(AssetManagementServiceImpl.class);
        model.filters.put(QueryHelper.TOKEN + QueryHelper.QUERY_ATYPE, as.getAssetTypesForReports(
            model.filters.get(QueryHelper.TOKEN + QueryHelper.QUERY_DOMAIN),
            "1")); // only monitored
        break;
    }
  }

  private void prepareFiltersByRegion(Long domainId, QueryRequestModel model) {
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
  }

  private void perpareFiltersByEntity(QueryRequestModel model, Map<String, String> retainFilters) {
    model.filters.put(QueryHelper.TOKEN + QueryHelper.QUERY_ENTITY, null);
    if (model.filters.containsKey(QueryHelper.TOKEN + QueryHelper.QUERY_ENTITY_TAG)) {
      retainFilters.put(QueryHelper.TOKEN + QueryHelper.QUERY_ENTITY_TAG,
          model.filters.remove(QueryHelper.TOKEN + QueryHelper.QUERY_ENTITY_TAG));
    }
    if (model.filters.containsKey(QueryHelper.TOKEN + QueryHelper.QUERY_COUNTRY)) {
      retainFilters.put(QueryHelper.TOKEN + QueryHelper.QUERY_COUNTRY,
          model.filters.remove(QueryHelper.TOKEN + QueryHelper.QUERY_COUNTRY));
    }
    if (model.filters.containsKey(QueryHelper.TOKEN + QueryHelper.QUERY_STATE)) {
      retainFilters.put(QueryHelper.TOKEN + QueryHelper.QUERY_STATE,
          model.filters.remove(QueryHelper.TOKEN + QueryHelper.QUERY_STATE));
    }
    if (model.filters.containsKey(QueryHelper.TOKEN + QueryHelper.QUERY_DISTRICT)) {
      retainFilters.put(QueryHelper.TOKEN + QueryHelper.QUERY_DISTRICT,
          model.filters.remove(QueryHelper.TOKEN + QueryHelper.QUERY_DISTRICT));
    }
    if (model.filters.containsKey(QueryHelper.TOKEN + QueryHelper.QUERY_TALUK)) {
      retainFilters.put(QueryHelper.TOKEN + QueryHelper.QUERY_TALUK,
          model.filters.remove(QueryHelper.TOKEN + QueryHelper.QUERY_TALUK));
    }
    if (model.filters.containsKey(QueryHelper.TOKEN + QueryHelper.QUERY_CITY)) {
      retainFilters.put(QueryHelper.TOKEN + QueryHelper.QUERY_CITY,
          model.filters.remove(QueryHelper.TOKEN + QueryHelper.QUERY_CITY));
    }
  }

  private void prepareFiltersByMaterial(QueryRequestModel model,
                                        Map<String, String> retainFilters) {
    model.filters.put(QueryHelper.TOKEN + QueryHelper.QUERY_MATERIAL, null);
    if (model.filters.containsKey(QueryHelper.TOKEN + QueryHelper.QUERY_MATERIAL_TAG)) {
      retainFilters.put(QueryHelper.TOKEN + QueryHelper.QUERY_MATERIAL_TAG,
          model.filters.remove(QueryHelper.TOKEN + QueryHelper.QUERY_MATERIAL_TAG));
    }
  }

  private String getReportTableStartTime(JSONObject jsonObject, String endTime)
      throws ParseException {
    switch (jsonObject.getString(QueryHelper.PERIODICITY)) {
      case QueryHelper.PERIODICITY_MONTH:
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM");
        Calendar toDate = new GregorianCalendar();
        toDate.setTime(format.parse(endTime));
        toDate.add(Calendar.MONTH, -1*(QueryHelper.MONTHS_LIMIT-1));
        return format.format(toDate.getTime());
      case QueryHelper.PERIODICITY_WEEK:
        DateTimeFormatter mDateTimeFormatter = DateTimeFormat.forPattern(
            QueryHelper.DATE_FORMAT_DAILY);
        DateTime toTime = mDateTimeFormatter.parseDateTime(endTime);
        return mDateTimeFormatter.print(toTime.minusWeeks(QueryHelper.WEEKS_LIMIT-1));
      default:
        mDateTimeFormatter = DateTimeFormat.forPattern(QueryHelper.DATE_FORMAT_DAILY);
        toTime = mDateTimeFormatter.parseDateTime(endTime);
        return mDateTimeFormatter.print(toTime.minusDays(QueryHelper.DAYS_LIMIT-1));
    }
  }

  @Override
  public void init(Services services) throws ServiceException {

  }

  @Override
  public void destroy() throws ServiceException {

  }

  @Override
  public Class<? extends Service> getInterface() {
    return ReportPluginService.class;
  }

  @Override
  public void loadResources(Locale locale) {

  }

  @Override
  public Locale getLocale() {
    return null;
  }

  @Override
  public Service clone() throws CloneNotSupportedException {
    return null;
  }
}

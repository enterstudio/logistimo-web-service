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

package com.logistimo.locations;

import com.logistimo.constants.LocationConstants;
import com.logistimo.dao.JDOUtils;
import com.logistimo.entity.ILocationConfig;
import com.logistimo.entity.ILocationFailedJob;
import com.logistimo.logger.XLog;
import com.logistimo.services.impl.PMF;
import com.logistimo.utils.JsonUtil;
import com.netflix.hystrix.HystrixCommand;
import com.netflix.hystrix.HystrixCommandGroupKey;
import com.netflix.hystrix.HystrixCommandProperties;
import com.netflix.hystrix.exception.HystrixBadRequestException;

import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.jdo.PersistenceManager;


/**
 * Created by yuvaraj on 16/03/17.
 */
public class LocationServiceUtil {

  private static final XLog log = XLog.getLog(LocationServiceUtil.class);
  private static LocationServiceUtil INSTANCE = new LocationServiceUtil();

  public static LocationServiceUtil getInstance() {
    return INSTANCE;
  }

  public Map<String, Object> getLocationIds(ILocationConfig location, Map<String, Object> map) {

    Map<String, Object> lcMap = convertReqParam(location, map);
    return call(lcMap);
  }

  public Map<String, Object> getLocationIds(Map<String, Object> map) {
    return call(map);
  }


  private Map<String, Object> call(Map<String, Object> param) {

    JSONObject req = JsonUtil.toJSON(param);
    String ret = null;
    log.info("Location service request with param {0}", req.toString());
    try {
      HystricHttpClient command = new HystricHttpClient(req.toString());
      ret = command.execute();
      log.info("location service response {0}", ret);
    } catch (Exception e) {
      String errorMsg = e.getMessage();
      Throwable th = e.getCause();
      ret += "{'error':}" + errorMsg + "}";
      log.severe("Error occurred for location service request with param {0}", req.toString(), e);
    }
    return convert(ret);
  }

  private Map<String, Object> convert(String json) {
    Map<String, Object> map = JsonUtil.toMap(json);
    if (map.get(LocationConstants.STATUS_TYPE_LITERAL) == null) {
      map.put(LocationConstants.STATUS_TYPE_LITERAL, LocationConstants.SUCCESS_LITERAL);
    }
    return map;
  }

  private Map<String, Object> convertReqParam(ILocationConfig location, Map<String, Object> map) {

    Map<String, Object> lcMap = new HashMap<>();
    if (StringUtils.isNotEmpty(location.getCountry())) {
      lcMap.put(LocationConstants.COUNTRY_LITERAL, location.getCountry());
    }
    if (StringUtils.isNotEmpty(location.getState())) {
      lcMap.put(LocationConstants.STATE_LITERAL, location.getState());
    }
    if (StringUtils.isNotEmpty(location.getDistrict())) {
      lcMap.put(LocationConstants.DIST_LITERAL, location.getDistrict());
    }
    if (StringUtils.isNotEmpty(location.getTaluk())) {
      lcMap.put(LocationConstants.SUBDIST_LITERAL, location.getTaluk());
    }
    if (StringUtils.isNotEmpty(location.getCity())) {
      lcMap.put(LocationConstants.CITY_LITERAL, location.getCity());
    }
    if (null != (Double) location.getLatitude()) {
      lcMap.put(LocationConstants.LAT_LITERAL, location.getLatitude());
    }
    if (null != (Double) location.getLongitude()) {
      lcMap.put(LocationConstants.LONG_LITERAL, location.getLongitude());
    }
    if (StringUtils.isNotEmpty(location.getPinCode())) {
      lcMap.put(LocationConstants.ZIP_LITERAL, location.getPinCode());
    }
    if (map.get(LocationConstants.KIOSKID_LITERAL) != null) {
      lcMap.put(LocationConstants.KIOSKID_LITERAL, map.get(LocationConstants.KIOSKID_LITERAL));
    } else if (map.get(LocationConstants.USERID_LITERAL) != null) {
      lcMap.put(LocationConstants.USERID_LITERAL, map.get(LocationConstants.USERID_LITERAL));
    }
    //add app name and user name
    lcMap.put(LocationConstants.APP_LITERAL, LocationConstants.APP_NAME);
    lcMap.put(LocationConstants.USER_LITERAL, map.get(LocationConstants.USER_LITERAL));
    return lcMap;
  }

  private class HystricHttpClient extends HystrixCommand<String> {

    HttpPost httpPost;
    String payload;

    public HystricHttpClient(String payload) {
      super(Setter.withGroupKey(HystrixCommandGroupKey.Factory.asKey("LocationServiceClient"))
          .andCommandPropertiesDefaults(
              HystrixCommandProperties.Setter().withExecutionTimeoutInMilliseconds(1500)));
      this.payload = payload;
      httpPost = new HttpPost(LocationConstants.LS_URL);
    }

    @Override
    protected String run() throws Exception {
      StringEntity requestEntity = new StringEntity(payload, ContentType.APPLICATION_JSON);
      httpPost.setEntity(requestEntity);
      String output = "";
      HttpClient client = build();
      HttpResponse response = client.execute(httpPost);
      //don't do anything
      if (response.getStatusLine().getStatusCode() == 400) {
        log.info("Ignoring location service request with payload {}", payload);
        throw new HystrixBadRequestException("Bad request");
      }
      //internal server error will result into exception
      if (response.getStatusLine().getStatusCode() == 500) {
        throw new RuntimeException("Failed : HTTP error code : " +
            response.getStatusLine().getStatusCode());
      }
      BufferedReader br = null;
      InputStreamReader ir = null;
      try {
        ir = new InputStreamReader((response.getEntity().getContent()));
        br = new BufferedReader(ir);
        String line = "";
        while ((line = br.readLine()) != null) {
          output = line;
        }
      } finally {
        if (br != null) {
          br.close();
        }
        if (ir != null) {
          ir.close();
        }
      }
      return output;
    }

    private HttpClient build() {
      RequestConfig config =
          RequestConfig.custom()
              .setConnectTimeout(1000)
              .setConnectionRequestTimeout(1000)
              .setSocketTimeout(1000)
              .build();
      CloseableHttpClient
          client =
          HttpClientBuilder.create().setDefaultRequestConfig(config).build();
      return client;
    }
    @Override
    protected String getFallback() {

      if (httpPost != null) {
        httpPost.abort();
      }
      log.warn("Issue with request : {0}", payload);
      JSONObject plObj  = new JSONObject(payload);
      plObj.put(LocationConstants.STATUS_TYPE_LITERAL,LocationConstants.FAILURE_LITERAL);
      boolean getType = plObj.keySet().contains("kioskId");
      Date currentDate = new Date();
      String type = getType ? LocationConstants.KIOSK_TYPE_LITERAL : LocationConstants.USER_TYPE_LITERAL;
      PersistenceManager pm = null;
      try {
        pm = PMF.get().getPersistenceManager();
        ILocationFailedJob locationFailedJob = JDOUtils.createInstance(ILocationFailedJob.class);
        locationFailedJob.setType(type);
        locationFailedJob.setPayLoad(payload);
        locationFailedJob.setProcessFlag(false);
        locationFailedJob.setCreateDate(currentDate);
        pm.makePersistent(locationFailedJob);
      } finally {
        if (null != pm) {
          pm.close();
        }
      }
      return plObj.toString();
    }
  }

}

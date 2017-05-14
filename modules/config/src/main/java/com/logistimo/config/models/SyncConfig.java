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

package com.logistimo.config.models;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;

/**
 * Created by vani on 20/11/15.
 */
public class SyncConfig implements Serializable {
  public static final String MASTER_DATA_REFRESH_INTERVAL = "mstrfint";
  public static final String APPLICATION_LOG_UPLOAD_INTERVAL = "applguplint";
  public static final String SMS_TRANSMISSION_WAIT_DURATION = "smstrwdur";
  public static final int HOURS_IN_A_DAY = 24;
  private static final long serialVersionUID = 1L;
  private int masterDataRefrInt = 24; // defaulted to 7 days => 168 hours
  private int appLgUpldInt = 24; // defaulted to 24 hours
  private int smsTransWaitDur = 0; // defaulted to 2 days => 48 hours

  public SyncConfig() {
  }

  public SyncConfig(JSONObject json) {
    // Master data refresh interval.
    try {
      masterDataRefrInt = json.getInt(MASTER_DATA_REFRESH_INTERVAL);
    } catch (JSONException e) {
      // do nothing
    }
    // App log upload interval
    try {
      appLgUpldInt = json.getInt(APPLICATION_LOG_UPLOAD_INTERVAL);
    } catch (JSONException e) {
      // do nothing
    }
    // SMS transmission wait duration
    try {
      smsTransWaitDur = json.getInt(SMS_TRANSMISSION_WAIT_DURATION);
    } catch (JSONException e) {
      // do nothing
    }
  }

  public JSONObject toJSONObject() throws ConfigurationException {
    JSONObject json = new JSONObject();
    try {
      json.put(MASTER_DATA_REFRESH_INTERVAL, masterDataRefrInt);
      json.put(APPLICATION_LOG_UPLOAD_INTERVAL, appLgUpldInt);
      json.put(SMS_TRANSMISSION_WAIT_DURATION, smsTransWaitDur);
    } catch (JSONException e) {
      throw new ConfigurationException(e.getMessage());
    }
    return json;
  }

  public int getMasterDataRefreshInterval() {
    return masterDataRefrInt;
  }

  public void setMasterDataRefreshInterval(int masterDataRefrInt) {
    this.masterDataRefrInt = masterDataRefrInt;
  }

  public int getAppLogUploadInterval() {
    return appLgUpldInt;
  }

  public void setAppLogUploadInterval(int appLgUpldInt) {
    this.appLgUpldInt = appLgUpldInt;
  }

  public int getSmsTransmissionWaitDuration() {
    return smsTransWaitDur;
  }

  public void setSmsTransmissionWaitDuration(int smsTransWaitDur) {
    this.smsTransWaitDur = smsTransWaitDur;
  }

}

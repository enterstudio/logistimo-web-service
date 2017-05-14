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

package com.logistimo.assets.models;


import com.logistimo.config.models.AssetConfig;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by kaniyarasu on 26/11/15.
 */
public class AssetModels {
  public static class AssetRegistrationModel {
    public List<AssetModel> devices = new ArrayList<>(1);

    public AssetRegistrationModel(AssetModel assetModel) {
      devices.add(assetModel);
    }

    public AssetRegistrationModel(List<AssetModel> devices) {
      this.devices = devices;
    }
  }

  public static class AssetResponseModel {
    public List<AssetModel> data = new ArrayList<>(1);
  }

  public static class AssetConfigResponseModel {
    public AssetConfig.Configuration data;
  }

  public static class AssetConfigModel {
    public String dId;
    public String vId;
    public AssetConfig.Configuration configuration;
  }

  public static class DeviceStatsModel {
    public List<DeviceStatsResponse> data;
    public long nPages;

    public static class DeviceStatsResponse {
      public String trId;
      public DailyStats stats;
    }

    public static class DailyStats {
      public Integer day;
      public Double tz;
      public Double mean;
      public Double min;
      public Double max;
      public AlertStats high;
      public AlertStats low;
      public CommunicationStats comm;
      public BatteryStats batt;
      public List<DailyStatsDeviceError> errs;
      public int nExc;
      public ExternalSensorStats xSns;
      public DeviceConnectionStats dCon;
    }

    public static class AlertStats {
      public int stat;
      public int nAlrms = 0;
      public Integer dur = 0;
      public Integer time = 0;
      public String fTime;
      public double aTmp;
      public boolean cnf;
      public String cnfms;
    }

    public static class CommunicationStats {
      public Integer nSMS;
      public Integer nPsh;
      public Integer nErr;
    }

    public static class BatteryStats {
      public Integer stat;
      public int nAlrms;
      public Double actv;
      public Integer time;
      public String fTime;
      public double lowv;
      public double highv;
      public int chgt;
      public int wdur;
      public int adur;
      public int pwrt;
    }

    public static class DailyStatsDeviceError {
      public String code;
      public int cnt;
      public Integer time;
      public String fTime;
    }

    public static class ExternalSensorStats {
      public int stat;
      public int nAlrms;
      public int dur;
      public Integer time;
      public String fTime;
    }

    public static class DeviceConnectionStats {
      public int stat;
      public int nAlrms;
      public int dur;
      public Integer time;
      public String fTime;
    }
  }

  public static class TempDeviceAlarmModel {
    public int typ = -1;
    public int stat = -1;
    public int time = 0;
    public String code = "-1";
    public String msg;
    public Integer mpId;
    public String sId;
    public String ftime;
  }

  public static class TempDeviceRecentAlertsModel {
    public List<TempDeviceAlertsModel> data = new ArrayList<TempDeviceAlertsModel>(1);
    public int nPages;

    public static class TempDeviceAlertsModel {
      public int typ;
      public Long time;
      public String ft;
      public AssetStatus tmpalm;
      public TempDeviceAlarmModel devalm;
    }
  }

  public static class AssetStatus {
    public Integer st;
    public Double tmp;
    //Abnormality status
    public Integer aSt;
    public Integer time;
    public Integer stut; // Update status timestamp
    //Asset monitoring location id, if any
    public Integer mpId = null;
    public Double min;
    public Double max;
    public String ftime;
    public String fstut; // Formatted update status from field 'stut'
    public Boolean isActive;
  }

  public static class DeviceReadyModel {
    public String fTime;
    public Long time;
  }

  public static class AssetsDeleteModel {
    public List<AssetModel> data;
  }

  public static class AssetDeleteModel {
    public String vId;
    public List<String> dIds;

    public AssetDeleteModel() {
      dIds = new ArrayList<String>(1);
    }

    public AssetDeleteModel(String vId, List<String> dIds) {
      this.vId = vId;
      this.dIds = dIds;
    }
  }

  public static class TemperatureSensorRequest {
    public String sId;

    public String cd;

    public Integer isA;
  }

  public static class AssetPowerTransitions {
    public List<AssetPowerTransition> data;
  }

  public static class AssetPowerTransition {
    public Integer st;

    public Integer time;

    public AssetPowerTransition() {
    }

    public AssetPowerTransition(Integer st, Integer time) {
      this.st = st;
      this.time = time;
    }
  }
}

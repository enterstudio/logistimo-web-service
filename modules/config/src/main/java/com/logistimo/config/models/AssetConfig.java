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

import com.google.gson.Gson;

import org.json.JSONObject;
import com.logistimo.proto.JsonTagsZ;
import com.logistimo.utils.StringUtil;
import com.logistimo.logger.XLog;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.TimeZone;

public class AssetConfig implements Serializable {
  //What to enable
  public static final int ENABLE_NONE = 0;
  public static final int ENABLE_ONLY_MONITORING = 1;
  public static final int ENABLE_ALL = 2;
  public static final String TYPE_BINARY = JsonTagsZ.TYPE_BINARY;
  private static final long serialVersionUID = -2885074964814290814L;
  private static final XLog xLogger = XLog.getLog(AssetConfig.class);
  private static final String VENDORS = "vendors";
  private static final String ASSET_MODELS = "models";
  private static final String DEFAULT_SNS = "defSns";
  private static final String DEFAULT_MPS = "defMps";
  private static final String ENABLE = "enable";
  private static final String CONFIG = "config";
  private static final String NAMESPACE = "namespace";
  List<String> vendorIds = new ArrayList<String>();
  List<String> assetModels = new ArrayList<>();
  List<String> defaultSns = new ArrayList<>();
  List<String> defaultMps = new ArrayList<>();
  int enable = ENABLE_NONE;
  Configuration configuration = new Configuration();
  String namespace;

  // Default constructor
  public AssetConfig() {
  }

  public AssetConfig(JSONObject json, java.util.Locale locale, String timezone) {
    xLogger.fine("Entering AssetConfig constructor. json: {0}", json);
    try {
      // Get the vendors json object
      String vendorIdsStr = json.getString(VENDORS);
      this.vendorIds = StringUtil.getList(vendorIdsStr);
      try {
        if (!json.isNull(ENABLE)) {
          this.enable = json.getInt(ENABLE);
        } else {
          this.enable = ENABLE_NONE;
        }
      } catch (Exception e) {
        this.enable = ENABLE_NONE;
      }
      if (!json.isNull(CONFIG)) {
        this.configuration = new Gson().fromJson(json.getString(CONFIG), Configuration.class);
      }

      if (!json.isNull(NAMESPACE)) {
        this.namespace = json.getString(NAMESPACE);
      }

      if (!json.isNull(ASSET_MODELS)) {
        this.assetModels = StringUtil.getList(json.getString(ASSET_MODELS));
      }

      if (!json.isNull(DEFAULT_SNS)) {
        this.defaultSns = StringUtil.getList(json.getString(DEFAULT_SNS));
      }

      if (!json.isNull(DEFAULT_MPS)) {
        this.defaultMps = StringUtil.getList(json.getString(DEFAULT_MPS));
      }
    } catch (Exception e) {
      // ignore
    }
    xLogger.fine("Exiting AssetConfig constructor. this.vendorIds: {0}", this.vendorIds.toString());
  }

  public static double getTimezoneOffset(String timezone) {
    if (timezone != null) {
      long
          offset =
          TimeZone.getTimeZone(timezone).getRawOffset() + TimeZone.getTimeZone(timezone)
              .getDSTSavings();
      return (double) offset / (60 * 60 * 1000);
    }

    return 0;
  }

  public JSONObject toJSONObject() throws ConfigurationException {
    xLogger.fine("Entering toJSONObject");
    JSONObject json = null;
    try {
      json = new JSONObject();
      if (vendorIds != null && !vendorIds.isEmpty()) {
        json.put(VENDORS, StringUtil.getCSV(this.vendorIds));
      }
      json.put(ENABLE, this.enable);
      if (this.enable != ENABLE_NONE) {
        json.put(CONFIG, this.configuration.toJSONObject());
      }
      json.put(NAMESPACE, this.namespace);

      if (assetModels != null && !assetModels.isEmpty()) {
        json.put(ASSET_MODELS, StringUtil.getCSV(assetModels));
      }

      if (defaultSns != null && !defaultSns.isEmpty()) {
        json.put(DEFAULT_SNS, StringUtil.getCSV(defaultSns));
      }

      if (defaultMps != null && !defaultMps.isEmpty()) {
        json.put(DEFAULT_MPS, StringUtil.getCSV(defaultMps));
      }
      xLogger.fine("Exiting toJSONObject");
      return json; // Return the temperature config json object
    } catch (Exception e) {
      throw new ConfigurationException(e.getMessage());
    }
  }

  public int getLanguageCode(String language) {
    int langCode = 0;
    if (language != null && language.equals("fr")) {
      langCode = 1;
    }

    return langCode;
  }

  public List<String> getDefaultMps() {
    return defaultMps;
  }

  public void setDefaultMps(List<String> defaultMps) {
    this.defaultMps = defaultMps;
  }

  public List<String> getAssetModels() {
    return assetModels;
  }

  public void setAssetModels(List<String> assetModels) {
    this.assetModels = assetModels;
  }

  public List<String> getDefaultSns() {
    return defaultSns;
  }

  public void setDefaultSns(List<String> defaultSns) {
    this.defaultSns = defaultSns;
  }

  public String getNamespace() {
    return namespace;
  }

  public void setNamespace(String namespace) {
    this.namespace = namespace;
  }

  public boolean isTemperatureMonitoringEnabled() {
    return (enable == ENABLE_ONLY_MONITORING);
  }

  public boolean isTemperatureMonitoringWithLogisticsEnabled() {
    return (enable == ENABLE_ALL);
  }

  public List<String> getVendorIds() {
    return vendorIds;
  }

  public void setVendorIds(List<String> vendorIds) {
    this.vendorIds = vendorIds;
  }

  public int getEnable() {
    return enable;
  }

  public void setEnable(int enable) {
    this.enable = enable;
  }

  public Configuration getConfiguration() {
    return configuration;
  }

  public void setConfiguration(Configuration configuration) {
    this.configuration = configuration;
  }

  public static class Configuration implements Serializable {
    /**
     *
     */
    private static final long serialVersionUID = 1L;

    private ConfigurationComm comm;

    private AlarmConfig highAlarm;

    private AlarmConfig lowAlarm;

    private AlarmConfig highWarn;

    private AlarmConfig lowWarn;

    private Locale locale;

    private Filter filter;

    private AlarmNotificationConfiguration notf;

    private PowerOutageAlarmConfiguration poa;

    private LowBatteryAlarmConfiguration lba;

    private List<SensorConfiguration> sensors;

    public Configuration() {
      comm = new ConfigurationComm();
      highAlarm = new AlarmConfig();
      lowAlarm = new AlarmConfig();
      highWarn = new AlarmConfig();
      lowWarn = new AlarmConfig();
      locale = new Locale();
      filter = new Filter();
      notf = new AlarmNotificationConfiguration();
      poa = new PowerOutageAlarmConfiguration();
      lba = new LowBatteryAlarmConfiguration();
      sensors = new ArrayList<>(1);
    }

    public String toJSONObject() {
      return new Gson().toJson(this);
    }

    public Filter getFilter() {
      return filter;
    }

    public void setFilter(Filter filter) {
      this.filter = filter;
    }

    public ConfigurationComm getComm() {
      return comm;
    }

    public void setComm(ConfigurationComm comm) {
      this.comm = comm;
    }

    public AlarmConfig getHighAlarm() {
      return highAlarm;
    }

    public void setHighAlarm(AlarmConfig highAlarm) {
      this.highAlarm = highAlarm;
    }

    public AlarmConfig getLowAlarm() {
      return lowAlarm;
    }

    public void setLowAlarm(AlarmConfig lowAlarm) {
      this.lowAlarm = lowAlarm;
    }

    public AlarmConfig getHighWarn() {
      return highWarn;
    }

    public void setHighWarn(AlarmConfig highWarn) {
      this.highWarn = highWarn;
    }

    public AlarmConfig getLowWarn() {
      return lowWarn;
    }

    public void setLowWarn(AlarmConfig lowWarn) {
      this.lowWarn = lowWarn;
    }

    public Locale getLocale() {
      return locale;
    }

    public void setLocale(Locale locale) {
      this.locale = locale;
    }

    public AlarmNotificationConfiguration getNotf() {
      return notf;
    }

    public void setNotf(AlarmNotificationConfiguration notf) {
      this.notf = notf;
    }

    public PowerOutageAlarmConfiguration getPoa() {
      return poa;
    }

    public void setPoa(PowerOutageAlarmConfiguration poa) {
      this.poa = poa;
    }

    public LowBatteryAlarmConfiguration getLba() {
      return lba;
    }

    public void setLba(LowBatteryAlarmConfiguration lba) {
      this.lba = lba;
    }

    public List<SensorConfiguration> getSensors() {
      return sensors;
    }

    public void setSensors(List<SensorConfiguration> sensors) {
      this.sensors = sensors;
    }
  }

  public static class ConfigurationComm implements Serializable {
    /**
     *
     */
    private static final long serialVersionUID = 1L;

    private Integer chnl = 1;

    private String tmpUrl;

    private String cfgUrl;

    private String alrmUrl;

    private String statsUrl;

    private String devRyUrl;

    private String smsGyPh;

    private String senderId;

    private String smsGyKey;

    private boolean tmpNotify = true;

    private boolean incExcNotify = true;

    private boolean statsNotify = false;

    private boolean devAlrmsNotify = true;

    private boolean tmpAlrmsNotify = false;

    private int samplingInt;

    private int pushInt;
    private List<String> usrPhones = new ArrayList<String>(1);

    public ConfigurationComm() {
    }

    public String getSenderId() {
      return senderId;
    }

    public void setSenderId(String senderId) {
      this.senderId = senderId;
    }

    public Integer getChnl() {
      return chnl;
    }

    public void setChnl(Integer chnl) {
      this.chnl = chnl;
    }

    public String getTmpUrl() {
      return tmpUrl;
    }

    public void setTmpUrl(String tmpUrl) {
      this.tmpUrl = tmpUrl;
    }

    public String getCfgUrl() {
      return cfgUrl;
    }

    public void setCfgUrl(String cfgUrl) {
      this.cfgUrl = cfgUrl;
    }

    public String getAlrmUrl() {
      return alrmUrl;
    }

    public void setAlrmUrl(String alrmUrl) {
      this.alrmUrl = alrmUrl;
    }

    public String getStatsUrl() {
      return statsUrl;
    }

    public void setStatsUrl(String statsUrl) {
      this.statsUrl = statsUrl;
    }

    public String getDevRyUrl() {
      return devRyUrl;
    }

    public void setDevRyUrl(String devRyUrl) {
      this.devRyUrl = devRyUrl;
    }

    public String getSmsGyPh() {
      return smsGyPh;
    }

    public void setSmsGyPh(String smsGyPh) {
      this.smsGyPh = smsGyPh;
    }

    public String getSmsGyKey() {
      return smsGyKey;
    }

    public void setSmsGyKey(String smsGyKey) {
      this.smsGyKey = smsGyKey;
    }

    public boolean isTmpNotify() {
      return tmpNotify;
    }

    public void setTmpNotify(boolean tmpNotify) {
      this.tmpNotify = tmpNotify;
    }

    public boolean isIncExcNotify() {
      return incExcNotify;
    }

    public void setIncExcNotify(boolean incExcNotify) {
      this.incExcNotify = incExcNotify;
    }

    public boolean isStatsNotify() {
      return statsNotify;
    }

    public void setStatsNotify(boolean statsNotify) {
      this.statsNotify = statsNotify;
    }

    public boolean isDevAlrmsNotify() {
      return devAlrmsNotify;
    }

    public void setDevAlrmsNotify(boolean devAlrmsNotify) {
      this.devAlrmsNotify = devAlrmsNotify;
    }

    public boolean isTmpAlrmsNotify() {
      return tmpAlrmsNotify;
    }

    public void setTmpAlrmsNotify(boolean tmpAlrmsNotify) {
      this.tmpAlrmsNotify = tmpAlrmsNotify;
    }

    public int getSamplingInt() {
      return samplingInt;
    }

    public void setSamplingInt(int samplingInt) {
      this.samplingInt = samplingInt;
    }

    public int getPushInt() {
      return pushInt;
    }

    public void setPushInt(int pushInt) {
      this.pushInt = pushInt;
    }

    public List<String> getUsrPhones() {
      return usrPhones;
    }

    public void setUsrPhones(List<String> usrPhones) {
      this.usrPhones = usrPhones;
    }
  }

  public static class AlarmConfig implements Serializable {
    /**
     *
     */
    private static final long serialVersionUID = 1L;

    private Double temp;

    private Integer dur;

    public AlarmConfig() {
    }

    public double getTemp() {
      return temp;
    }

    public void setTemp(double temp) {
      this.temp = temp;
    }

    public int getDur() {
      return dur;
    }

    public void setDur(int dur) {
      this.dur = dur;
    }
  }

  public static class Locale implements Serializable {
    /**
     *
     */
    private static final long serialVersionUID = 1L;

    private double tz;

    private String tznm;

    private String cn = "IN";

    private String ln = "en";

    public Locale() {
    }

    public double getTz() {
      return tz;
    }

    public void setTz(double tz) {
      this.tz = tz;
    }

    public String getCn() {
      return cn;
    }

    public void setCn(String cn) {
      this.cn = cn;
    }

    public String getLn() {
      return ln;
    }

    public void setLn(String ln) {
      this.ln = ln;
    }

    public String getTznm() {
      return tznm;
    }

    public void setTznm(String tznm) {
      this.tznm = tznm;
    }
  }

  public static class Filter implements Serializable {
    private static final long serialVersionUID = 1L;

    private int excursionFilterDuration;

    private int deviceAlarmFilterDuration;

    private int noDataFilterDuration;

    public int getExcursionFilterDuration() {
      return excursionFilterDuration;
    }

    public void setExcursionFilterDuration(int excursionFilterDuration) {
      this.excursionFilterDuration = excursionFilterDuration;
    }

    public int getDeviceAlarmFilterDuration() {
      return deviceAlarmFilterDuration;
    }

    public void setDeviceAlarmFilterDuration(int deviceAlarmFilterDuration) {
      this.deviceAlarmFilterDuration = deviceAlarmFilterDuration;
    }

    public int getNoDataFilterDuration() {
      return noDataFilterDuration;
    }

    public void setNoDataFilterDuration(int noDataFilterDuration) {
      this.noDataFilterDuration = noDataFilterDuration;
    }
  }

  public static class SensorConfiguration implements Serializable {
    private static final long serialVersionUID = 1L;

    private String sId;

    private ConfigurationComm comm;

    private AlarmConfig highAlarm;

    private AlarmConfig lowAlarm;

    private AlarmConfig highWarn;

    private AlarmConfig lowWarn;

    private AlarmNotificationConfiguration notf;

    public SensorConfiguration() {
                        /*comm = new ConfigurationComm();
                        highAlarm = new AlarmConfig();
			lowAlarm = new AlarmConfig();
			highWarn = new AlarmConfig();
			lowWarn = new AlarmConfig();
			notf = new AlarmNotificationConfiguration();*/
    }

    public String getsId() {
      return sId;
    }

    public void setsId(String sId) {
      this.sId = sId;
    }

    public ConfigurationComm getComm() {
      return comm;
    }

    public void setComm(ConfigurationComm comm) {
      this.comm = comm;
    }

    public AlarmConfig getHighAlarm() {
      return highAlarm;
    }

    public void setHighAlarm(AlarmConfig highAlarm) {
      this.highAlarm = highAlarm;
    }

    public AlarmConfig getLowAlarm() {
      return lowAlarm;
    }

    public void setLowAlarm(AlarmConfig lowAlarm) {
      this.lowAlarm = lowAlarm;
    }

    public AlarmConfig getHighWarn() {
      return highWarn;
    }

    public void setHighWarn(AlarmConfig highWarn) {
      this.highWarn = highWarn;
    }

    public AlarmConfig getLowWarn() {
      return lowWarn;
    }

    public void setLowWarn(AlarmConfig lowWarn) {
      this.lowWarn = lowWarn;
    }

    public AlarmNotificationConfiguration getNotf() {
      return notf;
    }

    public void setNotf(AlarmNotificationConfiguration notf) {
      this.notf = notf;
    }
  }

  public static class AlarmNotificationConfiguration implements Serializable {
    private static final long serialVersionUID = 1L;

    private Integer dur;

    private Integer num;

    public Integer getDur() {
      return dur;
    }

    public void setDur(Integer dur) {
      this.dur = dur;
    }

    public Integer getNum() {
      return num;
    }

    public void setNum(Integer num) {
      this.num = num;
    }
  }

  public static class PowerOutageAlarmConfiguration implements Serializable {
    private static final long serialVersionUID = 1L;

    private Integer dur;

    public Integer getDur() {
      return dur;
    }

    public void setDur(Integer dur) {
      this.dur = dur;
    }
  }

  public static class LowBatteryAlarmConfiguration implements Serializable {
    private static final long serialVersionUID = 1L;

    private Integer lmt;

    public Integer getLmt() {
      return lmt;
    }

    public void setLmt(Integer lmt) {
      this.lmt = lmt;
    }
  }
}

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

import org.apache.commons.lang.StringUtils;
import org.json.JSONException;
import org.json.JSONObject;

import com.logistimo.constants.CharacterConstants;
import com.logistimo.services.Resources;
import com.logistimo.constants.Constants;
import com.logistimo.utils.StringUtil;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

/**
 * @author Arun
 */
public class InventoryConfig implements Serializable {

  // Manual consumption frequencies
  public static final int CR_NONE = -1;
  public static final int CR_MANUAL = 0;
  public static final int CR_AUTOMATIC = 1;
  public static final int MIN_MAX_ABS_QTY = 0;
  public static final int MIN_MAX_DOS = 1;
  // Freq. values
  public static final String FREQ_DAILY = "daily";
  public static final String FREQ_WEEKLY = "weekly";
  public static final String FREQ_MONTHLY = "monthly";
  private static final long serialVersionUID = -7220548609845555088L;
  private static final String TIMES = "times";
  private static final String EXPORTUSERIDS = "expusrids";
  private static final String ENABLED = "enabled";
  private static final String SOURCEUSERID = "suid"; // source user ID
  private static final String TRANSREASONS = "trsns"; // reason codes for transactions
  private static final String IMTRANSREASONS = "imtrsns"; //reason codes for issues by material tags
  private static final String
      RMTRANSREASONS =
      "rmtrsns";
  //reason codes for receipts by material tags
  private static final String
      DMTRANSREASONS =
      "dmtrsns";
  //reason codes for discards by mateial tags
  private static final String
      SMTRANSREASONS =
      "smtrsns";
  //reason codes for stockcount by material tags
  private static final String
      TMTRANSREASONS =
      "tmtrsns";
  // reason codes for transfers by material tags
  private static final String PERMISSIONS = "prms"; // permissions
  private static final String BATCHMGMT = "batchmgmt"; // batch management
  private static final String DISPLAY_CONSUMPTION_RATE_FREQ = "dcrfreq"; // daily/weekly/monthly
  private static final String MANUAL_TRANSACTION_CONFIG = "manualtransconfig";
  private static final String SHOW_INVENTORY_DASHBOARD = "showinventorydashboard";
  private static final String CONFIGURE_ISSUES_BY_MATERIAL_TAGS = "cimt";
  private static final String CONFIGURE_RECEIPTS_BY_MATERIAL_TAGS = "crmt";
  private static final String CONFIGURE_DISCARDS_BY_MATERIAL_TAGS = "cdmt";
  private static final String CONFIGURE_STOCKCOUNT_BY_MATERIAL_TAGS = "csmt";
  private static final String CONFIGURE_TRANSFERS_BY_MATERIAL_TAGS = "ctmt";
  private static final String MATERIAL_STATUS = "mtst";
  private static final String
      CAPTURE_ACTUAL_TRANS_DATE =
      "catd";
  // to capture the actual transaction date
  private static final String
      CONFIGURE_ENTITY_TAG_FILTER =
      "cetf";
  // entity tags to be filtered in stock views.
  private static final String CONSUMPTION_RATE_COMPUTATION = "cr"; //consumption rate computation
  private static final String DISPLAY_CONSUMPTION_RATE = "dispcr"; //display consumption rate
  private static final String MANUAL_CONSUMPTION_FREQ = "manualcrfreq";
  private static final String SHOW_PREDICTIONS = "showpr";
  //private static final String ACTUAL_TRANS_DATE_TYPE="ty"; // 0 for optional and 1 for mandatory
  private static final String USER_TAGS_TRANSACTION_DATA_EXPORT = "usertgs";
  private static final String MIN_MAX_TYPE = "mmtype";
  private static final String MIN_MAX_DURATION = "mmdur";
  private static final String MIN_MAX_FREQUENCY = "mmfreq";
  private boolean enabled = false;
  private List<String> times = new ArrayList<String>();
  @SuppressWarnings("unused")
  private String email; // DEPRECATED: as of Dec. 18 2012
  private List<String> exportUserIds;
  private String sourceUserId;
  private Map<String, String>
      transReasons =
      new HashMap<String, String>();
  // transType --> Reason csv
  private Map<String, String> imTransReasons = new HashMap<>();
  private Map<String, String> rmTransReasons = new HashMap<>();
  private Map<String, String> smTransReasons = new HashMap<>();
  private Map<String, String> dmTransReasons = new HashMap<>();
  private Map<String, String> tmTransReasons = new HashMap<>();
  private Permissions permissions = null;
  private BatchMgmt batchMgmt = null;
  private ManualTransConfig manualTransConfig = null;
  private boolean
      showInventoryDashboard =
      false;
  // show the inventory dashboard (all entities-materials combo.)
  private boolean cimt = false; // configure issue reasons by material tag
  private boolean crmt = false;
  private boolean cdmt = false;
  private boolean csmt = false;
  private boolean ctmt = false;
  private Map<String, ActualTransConfig> actualTransConfigMap = null;
  private Map<String, MatStatusConfig> matStatusConfigMap = null;
  private List<String> enTags;
  private int cr = CR_NONE; // none/manual/automatic
  private boolean dispCR = false;
  private String dispCrFreq = null; // daily/weekly/monthly for display
  private String manualCrFreq; // Compute manual consumption frequency i.e., daily/weekly/monthly
  private boolean showPr = false;
  private List<String> userTags;
  private int mmType = MIN_MAX_ABS_QTY;
  private String mmDur = Constants.FREQ_DAILY;
  private String mmFreq = Constants.FREQ_DAILY;

  public InventoryConfig() {
    batchMgmt = new BatchMgmt();
    manualTransConfig = new ManualTransConfig();
  }

  @SuppressWarnings("unchecked")
  public InventoryConfig(JSONObject json) {
    try {
      enabled = json.getBoolean(ENABLED);
    } catch (JSONException e) {
      // ignore
    }
    try {
      times = StringUtil.getList(json.getString(TIMES));
    } catch (JSONException e) {
      // ignore
    }
    try {
      exportUserIds = StringUtil.getList(json.getString(EXPORTUSERIDS));
    } catch (JSONException e) {
      // ignore
    }
    try {
      sourceUserId = json.getString(SOURCEUSERID);
    } catch (JSONException e) {
      // ignore
    }
    try {
      JSONObject reasons = json.getJSONObject(TRANSREASONS);
      Iterator<String> en = reasons.keys();
      while (en.hasNext()) {
        String transType = en.next();
        transReasons.put(transType, reasons.getString(transType));
      }
    } catch (JSONException e) {
      // ignore
    }
    try {
      JSONObject reasons = json.getJSONObject(IMTRANSREASONS);
      if (reasons != null) {
        Iterator<String> en = reasons.keys();
        while (en.hasNext()) {
          String mTag = en.next();
          imTransReasons.put(mTag, reasons.getString(mTag));
        }
      }
    } catch (JSONException e) {
      // ignore
    }
    try {
      JSONObject reasons = json.getJSONObject(RMTRANSREASONS);
      if (reasons != null) {
        Iterator<String> en = reasons.keys();
        while (en.hasNext()) {
          String mTag = en.next();
          rmTransReasons.put(mTag, reasons.getString(mTag));
        }
      }
    } catch (JSONException e) {
      // ignore
    }
    try {
      JSONObject reasons = json.getJSONObject(DMTRANSREASONS);
      if (reasons != null) {
        Iterator<String> en = reasons.keys();
        while (en.hasNext()) {
          String mTag = en.next();
          dmTransReasons.put(mTag, reasons.getString(mTag));
        }
      }
    } catch (JSONException e) {
      // ignore
    }
    try {
      JSONObject reasons = json.getJSONObject(SMTRANSREASONS);
      if (reasons != null) {
        Iterator<String> en = reasons.keys();
        while (en.hasNext()) {
          String mTag = en.next();
          smTransReasons.put(mTag, reasons.getString(mTag));
        }
      }
    } catch (JSONException e) {
      // ignore
    }
    try {
      JSONObject reasons = json.getJSONObject(TMTRANSREASONS);
      if (reasons != null) {
        Iterator<String> en = reasons.keys();
        while (en.hasNext()) {
          String mTag = en.next();
          tmTransReasons.put(mTag, reasons.getString(mTag));
        }
      }
    } catch (JSONException e) {
      // ignore
    }
    try {
      permissions = new Permissions(json.getJSONObject(PERMISSIONS));
    } catch (JSONException e) {
      // ignore
    }
    try {
      batchMgmt = new BatchMgmt(json.getJSONObject(BATCHMGMT));
    } catch (JSONException e) {
      batchMgmt = new BatchMgmt();
    }
    try {
      dispCrFreq = json.getString(DISPLAY_CONSUMPTION_RATE_FREQ);
    } catch (JSONException e) {
      // ignore
    }

    try {
      manualTransConfig = new ManualTransConfig(json.getJSONObject(MANUAL_TRANSACTION_CONFIG));
    } catch (JSONException e) {
      manualTransConfig = new ManualTransConfig();
    }
    try {
      showInventoryDashboard = json.getBoolean(SHOW_INVENTORY_DASHBOARD);
    } catch (JSONException e) {
      showInventoryDashboard = false;
    }
    try {
      cimt = json.getBoolean(CONFIGURE_ISSUES_BY_MATERIAL_TAGS);
    } catch (JSONException e) {
      cimt = false;
    }
    try {
      crmt = json.getBoolean(CONFIGURE_RECEIPTS_BY_MATERIAL_TAGS);
    } catch (JSONException e) {
      crmt = false;
    }
    try {
      cdmt = json.getBoolean(CONFIGURE_DISCARDS_BY_MATERIAL_TAGS);
    } catch (JSONException e) {
      cdmt = false;
    }
    try {
      csmt = json.getBoolean(CONFIGURE_STOCKCOUNT_BY_MATERIAL_TAGS);
    } catch (JSONException e) {
      csmt = false;
    }
    try {
      ctmt = json.getBoolean(CONFIGURE_TRANSFERS_BY_MATERIAL_TAGS);
    } catch (JSONException e) {
      ctmt = false;
    }
    try {
      this.matStatusConfigMap =
          MatStatusConfig.getMatStatustMap(json.getJSONObject(MATERIAL_STATUS));
    } catch (JSONException e) {
      this.matStatusConfigMap = new HashMap<>();
    }
    try {
      this.actualTransConfigMap =
          ActualTransConfig.getActualTranstMap(json.getJSONObject(CAPTURE_ACTUAL_TRANS_DATE));
    } catch (JSONException e) {
      this.actualTransConfigMap = new HashMap<>();
    }
    try {
      this.enTags = StringUtil.getList(json.getString(CONFIGURE_ENTITY_TAG_FILTER));
    } catch (JSONException e) {
      // ignore
    }
    try {
      this.cr = json.getInt(CONSUMPTION_RATE_COMPUTATION);
    } catch (JSONException e) {
      //ignore
    }
    try {
      this.dispCR = json.getBoolean(DISPLAY_CONSUMPTION_RATE);
    } catch (JSONException e) {
      //ignore
    }
    try {
      this.manualCrFreq = json.getString(MANUAL_CONSUMPTION_FREQ);
    } catch (JSONException e) {
      //ignore
    }
    try {
      this.showPr = json.getBoolean(SHOW_PREDICTIONS);
    } catch (Exception e) {
      //ignore
    }
    try {
      this.userTags = StringUtil.getList(json.getString(USER_TAGS_TRANSACTION_DATA_EXPORT));
    } catch (JSONException e) {
      // ignore
    }
    try {
      this.mmType = json.optInt(MIN_MAX_TYPE, MIN_MAX_ABS_QTY);
    } catch (JSONException e) {
      //ignore
    }
    try {
      this.mmDur = json.getString(MIN_MAX_DURATION);
    } catch (JSONException e) {
      //ignore
    }
    try {
      this.mmFreq = json.getString(MIN_MAX_FREQUENCY);
    } catch (JSONException e) {
      //ignore
    }
  }

  public static String getFrequencyDisplay(String freq, boolean isRate, Locale locale) {
    ResourceBundle messages = Resources.get().getBundle("Messages", locale);
    if (Constants.FREQ_DAILY.equals(freq)) {
      return messages.getString(isRate ? "daily" : "days");
    } else if (Constants.FREQ_WEEKLY.equals(freq)) {
      return messages.getString(isRate ? "weekly" : "weeks");
    } else if (Constants.FREQ_MONTHLY.equals(freq)) {
      return messages.getString(isRate ? "monthly" : "months");
    }
    return null;
  }

  public boolean isEnabled() {
    return enabled;
  }

  public void setEnabled(boolean enabled) {
    this.enabled = enabled;
  }

  public List<String> getTimes() {
    return times;
  }

  public void setTimes(List<String> times) {
    this.times = times;
  }

  public List<String> getExportUsers() {
    return exportUserIds;
  }

  public void setExportUsers(String usersCSV) {
    this.exportUserIds = StringUtil.getList(usersCSV);
  }

  public String getSourceUserId() {
    return sourceUserId;
  }

  public void setSourceUserId(String sourceUserId) {
    this.sourceUserId = sourceUserId;
  }

  public Map<String, String> getTransReasons() {
    return transReasons;
  }

  public void setTransReasons(Map<String, String> transReasons) {
    this.transReasons = transReasons;
  }

  public String getTransReason(String transType) {
    return (String) transReasons.get(transType);
  }

  public void putTransReason(String transType, String reasonsCsv) {
    transReasons.put(transType, reasonsCsv);
  }

  public Map<String, String> getImTransReasons() {
    return imTransReasons;
  }

  public String getImTransReason(String mtag) {
    return imTransReasons != null ? imTransReasons.get(mtag) : null;
  }

  public void setImtransreasons(Map<String, String> imtransreasons) {
    this.imTransReasons = imtransreasons;
  }

  public void putImTransReason(String mtag, String reasonsCSV) {
    imTransReasons.put(mtag, reasonsCSV);
  }

  public Map<String, String> getRmTransReasons() {
    return rmTransReasons;
  }

  public String getRmTransReason(String mtag) {
    return rmTransReasons != null ? rmTransReasons.get(mtag) : null;
  }

  public void setRmtransreasons(Map<String, String> rmtransreasons) {
    this.rmTransReasons = rmtransreasons;
  }

  public void putRmTransReason(String mtag, String reasonsCSV) {
    rmTransReasons.put(mtag, reasonsCSV);
  }

  public Map<String, String> getDmTransReasons() {
    return dmTransReasons;
  }

  public String getDmTransReason(String mtag) {
    return dmTransReasons != null ? dmTransReasons.get(mtag) : null;
  }

  public void setDmtransreasons(Map<String, String> dmtransreasons) {
    this.dmTransReasons = dmtransreasons;
  }

  public void putDmTransReason(String mtag, String reasonsCSV) {
    dmTransReasons.put(mtag, reasonsCSV);
  }

  public Map<String, String> getSmTransReasons() {
    return smTransReasons;
  }

  public String getSmTransReason(String mtag) {
    return smTransReasons != null ? smTransReasons.get(mtag) : null;
  }

  public void setSmtransreasons(Map<String, String> smtransreasons) {
    this.smTransReasons = smtransreasons;
  }

  public void putSmTransReason(String mtag, String reasonsCSV) {
    smTransReasons.put(mtag, reasonsCSV);
  }

  public Map<String, String> getTmTransReasons() {
    return tmTransReasons;
  }

  public String getTmTransReason(String mtag) {
    return tmTransReasons != null ? tmTransReasons.get(mtag) : null;
  }

  public void setTmtransreasons(Map<String, String> tmtransreasons) {
    this.tmTransReasons = tmtransreasons;
  }

  public void putTmTransReason(String mtag, String reasonsCSV) {
    tmTransReasons.put(mtag, reasonsCSV);
  }

  public Permissions getPermissions() {
    return permissions;
  }

  public void setPermissions(Permissions permissions) {
    this.permissions = permissions;
  }

  public BatchMgmt getBatchMgmt() {
    return batchMgmt;
  }

  public void setBatchMgmt(BatchMgmt batchMgmt) {
    this.batchMgmt = batchMgmt;
  }

  public ManualTransConfig getManualTransConfig() {
    return manualTransConfig;
  }

  public void setManualTransConfig(ManualTransConfig manualTransConfig) {
    this.manualTransConfig = manualTransConfig;
  }

  public boolean showInventoryDashboard() {
    return showInventoryDashboard;
  }

  public void setShowInventoryDashboard(boolean show) {
    showInventoryDashboard = show;
  }

  public Map<String, MatStatusConfig> getMatStatusConfigMapByType() {
    return matStatusConfigMap;
  }

  public MatStatusConfig getMatStatusConfigByType(String type) {
    return matStatusConfigMap == null ? null : matStatusConfigMap.get(type);
  }

  public void setMatStatusConfigByType(String type, MatStatusConfig mc) {
    if (matStatusConfigMap == null) {
      matStatusConfigMap = new HashMap<>();
    }
    matStatusConfigMap.put(type, mc);
  }

  public Map<String, ActualTransConfig> getActualTransConfigMapByType() {
    return actualTransConfigMap;
  }

  public ActualTransConfig getActualTransConfigByType(String type) {
    return actualTransConfigMap == null ? null : actualTransConfigMap.get(type);
  }

  public void setActualTransDateByType(String type, ActualTransConfig ac) {
    if (actualTransConfigMap == null) {
      actualTransConfigMap = new HashMap<>();
    }
    actualTransConfigMap.put(type, ac);
  }

  public boolean isCimt() {
    return cimt;
  }

  public void setCimt(boolean cimt) {
    this.cimt = cimt;
  }

  public boolean isCrmt() {
    return crmt;
  }

  public void setCrmt(boolean crmt) {
    this.crmt = crmt;
  }

  public boolean isCdmt() {
    return cdmt;
  }

  public void setCdmt(boolean cdmt) {
    this.cdmt = cdmt;
  }

  public boolean isCsmt() {
    return csmt;
  }

  public void setCsmt(boolean csmt) {
    this.csmt = csmt;
  }

  public boolean isCtmt() {
    return ctmt;
  }

  public void setCtmt(boolean ctmt) {
    this.ctmt = ctmt;
  }

  public List<String> getEnTags() {
    return enTags;
  }

  public void setEnTags(List<String> enTags) {
    this.enTags = enTags;
  }

  public List<String> getUserTags() {
    return userTags;
  }

  public void setUserTags(List<String> userTags) {
    this.userTags = userTags;
  }

  public int getConsumptionRate() {
    return cr;
  }

  public void setConsumptionRate(int cr) {
    this.cr = cr;
  }

  public boolean displayCR() {
    return dispCR;
  }

  public void setDisplayCR(boolean dispCR) {
    this.dispCR = dispCR;
  }

  public String getDisplayCRFreq() {
    return dispCrFreq;
  }

  public void setDisplayCRFreq(String crFreq) {
    this.dispCrFreq = crFreq;
  }

  public String getManualCRFreq() {
    return manualCrFreq;
  }

  public void setManualCRFreq(String manualCrFreq) {
    this.manualCrFreq = manualCrFreq;
  }

  public boolean showPredictions() {
    return showPr;
  }

  public void setShowPredictions(boolean showPr) {
    this.showPr = showPr;
  }

  public int getMinMaxType() {
    return mmType;
  }

  public void setMinMaxType(int mmType) {
    this.mmType = mmType;
  }

  public boolean isMinMaxAbsolute() {
    return mmType == MIN_MAX_ABS_QTY;
  }

  public String getMinMaxFreq() {
    return mmFreq;
  }

  public void setMinMaxFreq(String mmFreq) {
    this.mmFreq = mmFreq;
  }

  public String getMinMaxDur() {
    return mmDur;
  }

  public void setMinMaxDur(String mmDur) {
    this.mmDur = mmDur;
  }

  public boolean isCREnabled() {
    return cr != CR_NONE;
  }

  public JSONObject toJSONObject() throws ConfigurationException {
    try {
      JSONObject json = new JSONObject();
      json.put(ENABLED, enabled);
      if (times != null && !times.isEmpty()) {
        json.put(TIMES, StringUtil.getCSV(times));
      }
      if (exportUserIds != null && !exportUserIds.isEmpty()) {
        json.put(EXPORTUSERIDS, StringUtil.getCSV(exportUserIds));
      }
      if (sourceUserId != null && !sourceUserId.isEmpty()) {
        json.put(SOURCEUSERID, sourceUserId);
      }
      if (!transReasons.isEmpty()) {
        JSONObject reasons = new JSONObject();
        Iterator<String> it = transReasons.keySet().iterator();
        while (it.hasNext()) {
          String transType = it.next();
          String reasonCSV = (String) transReasons.get(transType);
          if (reasonCSV != null && !reasonCSV.isEmpty()) {
            reasons.put(transType, reasonCSV);
          }
        }
        json.put(TRANSREASONS, reasons);
      }
      if (!imTransReasons.isEmpty()) {
        JSONObject reasons = new JSONObject();
        Iterator<String> it = imTransReasons.keySet().iterator();
        while (it.hasNext()) {
          String mTag = it.next();
          String reasonCSV = (String) imTransReasons.get(mTag);
          if (reasonCSV != null && !reasonCSV.isEmpty()) {
            reasons.put(mTag, reasonCSV);
          }
        }
        json.put(IMTRANSREASONS, reasons);
      }
      if (!rmTransReasons.isEmpty()) {
        JSONObject reasons = new JSONObject();
        Iterator<String> it = rmTransReasons.keySet().iterator();
        while (it.hasNext()) {
          String mTag = it.next();
          String reasonCSV = (String) rmTransReasons.get(mTag);
          if (reasonCSV != null && !reasonCSV.isEmpty()) {
            reasons.put(mTag, reasonCSV);
          }
        }
        json.put(RMTRANSREASONS, reasons);
      }
      if (!dmTransReasons.isEmpty()) {
        JSONObject reasons = new JSONObject();
        Iterator<String> it = dmTransReasons.keySet().iterator();
        while (it.hasNext()) {
          String mTag = it.next();
          String reasonCSV = (String) dmTransReasons.get(mTag);
          if (reasonCSV != null && !reasonCSV.isEmpty()) {
            reasons.put(mTag, reasonCSV);
          }
        }
        json.put(DMTRANSREASONS, reasons);
      }
      if (!smTransReasons.isEmpty()) {
        JSONObject reasons = new JSONObject();
        Iterator<String> it = smTransReasons.keySet().iterator();
        while (it.hasNext()) {
          String mTag = it.next();
          String reasonCSV = (String) smTransReasons.get(mTag);
          if (reasonCSV != null && !reasonCSV.isEmpty()) {
            reasons.put(mTag, reasonCSV);
          }
        }
        json.put(SMTRANSREASONS, reasons);
      }
      if (!tmTransReasons.isEmpty()) {
        JSONObject reasons = new JSONObject();
        Iterator<String> it = tmTransReasons.keySet().iterator();
        while (it.hasNext()) {
          String mTag = it.next();
          String reasonCSV = (String) tmTransReasons.get(mTag);
          if (reasonCSV != null && !reasonCSV.isEmpty()) {
            reasons.put(mTag, reasonCSV);
          }
        }
        json.put(TMTRANSREASONS, reasons);
      }
                        /*if(!actualDateTrans.isEmpty()){
                                JSONObject atd = new JSONObject();
				atd.put(ACTUAL_TRANS_DATE_TYPE,actualDateTrans.get(ACTUAL_TRANS_DATE_TYPE));
				json.put(CAPTURE_ACTUAL_TRANS_DATE,atd);
			}*/
      if (permissions != null) {
        json.put(PERMISSIONS, permissions.toJSONObject());
      }
      if (batchMgmt != null) {
        json.put(BATCHMGMT, batchMgmt.toJSONObject());
      }
      if (manualTransConfig != null) {
        json.put(MANUAL_TRANSACTION_CONFIG, manualTransConfig.toJSONObject());
      }
      json.put(SHOW_INVENTORY_DASHBOARD, showInventoryDashboard);
      json.put(CONFIGURE_ISSUES_BY_MATERIAL_TAGS, cimt);
      json.put(CONFIGURE_RECEIPTS_BY_MATERIAL_TAGS, crmt);
      json.put(CONFIGURE_DISCARDS_BY_MATERIAL_TAGS, cdmt);
      json.put(CONFIGURE_STOCKCOUNT_BY_MATERIAL_TAGS, csmt);
      json.put(CONFIGURE_TRANSFERS_BY_MATERIAL_TAGS, ctmt);

      if (matStatusConfigMap != null && !matStatusConfigMap.isEmpty()) {
        json.put(MATERIAL_STATUS, MatStatusConfig.getMatStatusJSON(matStatusConfigMap));
      }
      if (actualTransConfigMap != null && !actualTransConfigMap.isEmpty()) {
        json.put(CAPTURE_ACTUAL_TRANS_DATE,
            ActualTransConfig.getActualTransJSON(actualTransConfigMap));
      }
      if (enTags != null && !enTags.isEmpty()) {
        json.put(CONFIGURE_ENTITY_TAG_FILTER, StringUtil.getCSV(enTags));
      }
      if (userTags != null && !userTags.isEmpty()) {
        json.put(USER_TAGS_TRANSACTION_DATA_EXPORT, StringUtil.getCSV(userTags));
      }
      json.put(CONSUMPTION_RATE_COMPUTATION, String.valueOf(cr));
      json.put(DISPLAY_CONSUMPTION_RATE, dispCR);
      if (dispCR) {
        json.put(DISPLAY_CONSUMPTION_RATE_FREQ, dispCrFreq);
      }
      if (CR_MANUAL == cr) {
        json.put(MANUAL_CONSUMPTION_FREQ, manualCrFreq);
      }
      json.put(SHOW_PREDICTIONS, showPr);
      json.put(MIN_MAX_TYPE, mmType);
      json.put(MIN_MAX_DURATION, mmDur);
      json.put(MIN_MAX_FREQUENCY, mmFreq);
      return json;
    } catch (Exception e) {
      throw new ConfigurationException(e.getMessage());
    }
  }

  public String getFirstMaterialStatus(boolean isTempSensitive) {
    MatStatusConfig msConfig = getMatStatusConfigByType("i");
    String matStatus = isTempSensitive ? msConfig.getEtsm() : msConfig.getDf();
    if (StringUtils.isNotBlank(matStatus)) {
      matStatus = matStatus.split(CharacterConstants.COMMA,2)[0];
    }
    return matStatus;
  }

  public static class Permissions implements Serializable {

    private static final long serialVersionUID = 1L;

    private static final String INV_CUSTOMERS_VISIBLE = "invcstsvisible";

    public boolean invCustomersVisible = false;

    public Permissions() {
    }

    public Permissions(JSONObject json) throws JSONException {
      try {
        invCustomersVisible = json.getBoolean(INV_CUSTOMERS_VISIBLE);
      } catch (Exception e) {
        // ignore
      }
    }

    public JSONObject toJSONObject() throws JSONException {
      JSONObject json = new JSONObject();
      json.put(INV_CUSTOMERS_VISIBLE, invCustomersVisible);
      return json;
    }
  }

  public static class BatchMgmt implements Serializable {

    // Constants
    public static final int NO_BATCHDATA_ISSUE_NONE = 0;
    public static final int NO_BATCHDATA_ISSUE_FEFO = 1;
    private static final long serialVersionUID = 1L;
    // Tags
    private static final String ISSUE_POLICY_NO_BATCHDATA = "issuePolocyNoBatchdata";

    public int
        issuePolicyNoBatchData =
        NO_BATCHDATA_ISSUE_FEFO;
    // issue/wastage stock decrement policy when no batch data is entered for a batch-enabled material (typically via the mobile)

    public BatchMgmt() {
    }

    public BatchMgmt(JSONObject json) {
      try {
        issuePolicyNoBatchData = json.getInt(ISSUE_POLICY_NO_BATCHDATA);
      } catch (Exception e) {
        // ignore
      }
    }

    public JSONObject toJSONObject() throws JSONException {
      JSONObject json = new JSONObject();
      json.put(ISSUE_POLICY_NO_BATCHDATA, issuePolicyNoBatchData);
      return json;
    }
  }

  public static class ManualTransConfig implements Serializable {
    /**
     *
     */
    private static final long serialVersionUID = 1L;
    // Tags
    private static final String
        ENABLE_MANUAL_UPLOAD_INVDATA_AND_TRANSACTIONS =
        "enblmanualuplaodinvdataandtrans";
    private static final String ENABLE_UPLOAD_PER_ENTITY_ONLY = "enbluploadperentityonly";

    public boolean enableManualUploadInvDataAndTrans = false;
    public boolean enableUploadPerEntityOnly = false;

    public ManualTransConfig() {
    }

    public ManualTransConfig(JSONObject json) {
      try {
        enableManualUploadInvDataAndTrans =
            json.getBoolean(ENABLE_MANUAL_UPLOAD_INVDATA_AND_TRANSACTIONS);
        enableUploadPerEntityOnly = json.getBoolean(ENABLE_UPLOAD_PER_ENTITY_ONLY);
      } catch (Exception e) {
        // ignore
      }
    }

    public JSONObject toJSONObject() throws JSONException {
      JSONObject json = new JSONObject();
      json.put(ENABLE_MANUAL_UPLOAD_INVDATA_AND_TRANSACTIONS, enableManualUploadInvDataAndTrans);
      json.put(ENABLE_UPLOAD_PER_ENTITY_ONLY, enableUploadPerEntityOnly);
      return json;
    }
  }
}

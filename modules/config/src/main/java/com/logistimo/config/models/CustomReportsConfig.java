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

import com.logistimo.customreports.CustomReportConstants;

import org.json.JSONException;
import org.json.JSONObject;
import com.logistimo.constants.Constants;
import com.logistimo.utils.LocalDateUtil;
import com.logistimo.utils.StringUtil;
import com.logistimo.logger.XLog;

import java.io.Serializable;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public class CustomReportsConfig implements Serializable {
  public static final String VERSION = "0";
  public static final String SEPARATOR = "_";
  // Values of dayOfMonth
  public static final int BEGINNING_OF_MONTH = 1;
  public static final int END_OF_MONTH = 2;
  public static final String SHEETNAME = "sheetname";
  public static final String DATA_DURATION_SAMEAS_REPGENFREQ = "datadursameasrepgenfreq";
  public static final String DATA_DURATION = "dataduration";
  public static final String AGGREGATEFREQ = "aggrfreq";
  public static final String FILTERBY = "filterby";
  public static final String TRUE = "true";
  public static final String FALSE = "false";
  private static final XLog xLogger = XLog.getLog(CustomReportsConfig.class);
  private static final long serialVersionUID = 501549418020616277L;
  // json tags
  private static final String TEMPLATE_NAME = "templatename";
  private static final String FILENAME = "filename";
  private static final String DESCRIPTION = "desc";
  private static final String TEMPLATE_KEY = "templatekey";
  private static final String SHEETDATA = "sheetdata";
  private static final String DAILY_TIME = "dailytime";
  private static final String DAY_OF_WEEK = "dayofweek";
  private static final String DAY_OF_MONTH = "dayofmonth";
  private static final String WEEKLY_REPORT_GENERATION_TIME = "weeklyrepgentime";
  private static final String MONTHLY_REPORT_GENERATION_TIME = "monthlyyrepgentime";

  private static final String MANAGERS = "managers";
  private static final String USERS = "users";
  private static final String SUPERUSERS = "susers";
  private static final String EXTERNAL_USERS = "extusers";
  private static final String USER_TAGS = "usertags";
  private static final String CREATION_TIME = "creationtime";
  private static final String LASTUPDATED_TIME = "lastupdatedtime";

  List<Config> customReportsConfig = new ArrayList<Config>();

  // Default constructor
  public CustomReportsConfig() {
  }

  public CustomReportsConfig(JSONObject json, Locale locale, String timezone) {
    xLogger.fine("Entering CustomReportsConfig constructor");
    try {
      @SuppressWarnings("rawtypes")
      Iterator keys = json.keys();
      if (keys != null) {
        while (keys.hasNext()) {
          String key = (String) keys.next();
          JSONObject configJson = json.getJSONObject(key);
          Config config = new Config(configJson, locale, timezone);
          customReportsConfig.add(config);
        }
      }
    } catch (Exception e) {
      // ignore
    }
    xLogger.fine("Exiting CustomReportsConfig constructor");
  }

  public JSONObject toJSONObject() throws ConfigurationException {
    xLogger.fine("Entering toJSONObject");
    try {
      JSONObject json = new JSONObject();
      if (customReportsConfig != null && !customReportsConfig.isEmpty()
          && customReportsConfig.size() != 0) {
        // Iterate over the list and convert each of the Config objects to Json object and add it to the configJsonArray.
        Iterator<Config> customReportsConfigIter = customReportsConfig.iterator();
        while (customReportsConfigIter.hasNext()) {
          Config config = customReportsConfigIter.next();
          json.put(config.templateName, config
              .toJSONObject()); // Add the template name as key and the config json object as the value to json
        }
      }
      xLogger.fine("Exiting toJSONObject");
      return json; // Return the custom reports config json object
    } catch (Exception e) {
      throw new ConfigurationException(e.getMessage());
    }
  }

  public List<Config> getCustomReportsConfig() {
    return customReportsConfig;
  }

  /*
   * This method removes a Config object with the specified templateName from CustomReportsConfig. It also removes the
   * corresponding blob from the blobstore.
   */
  public void removeConfig(String templateName) {
    xLogger.fine("Entering removeConfig");
    if (templateName != null && !templateName.isEmpty()) {
      Config config = getConfig(templateName);
      if (config != null) {
        customReportsConfig.remove(config);
      }
    }
    xLogger.fine("Exiting removeConfig");
  }

  /*
   * This method gets the Config object specified by the template name.
   */
  public CustomReportsConfig.Config getConfig(String templateName) {
    xLogger.fine("Entering getConfig, templateName: {0}", templateName);
    if (templateName != null && !templateName.isEmpty()) {
      // Iterate through customReportsConfig.
      Iterator<CustomReportsConfig.Config> customReportsConfigIter = customReportsConfig.iterator();
      while (customReportsConfigIter.hasNext()) {
        CustomReportsConfig.Config config = customReportsConfigIter.next();
        if (config.templateName.equals(templateName)) {
          return config;
        }
      }
    }
    xLogger.fine("Template with name {0} not found ", templateName);
    return null;
  }

  public static class Config implements Serializable, Comparable<CustomReportsConfig.Config> {
    /**
     *
     */
    private static final long serialVersionUID = -5476306820009283321L;

    private static final XLog xLogger = XLog.getLog(Config.class);

    // The report template name
    public String templateName;
    // The (xls or xlsx)file name of the report template that is uploaded.
    public String fileName;
    // A brief description about the report template.
    public String description;
    // The Uploaded key corresponding to the report template uploaded to the blob store. Format: domainId+filename_version_locale
    public String templateKey;
    // Map of the type to the sheet name - DEPRECATED
    public Map<String, String> typeSheetnameMap = new HashMap<String, String>();
    // Map of the type of data to be exported to the sheetdata
    public Map<String, Map<String, String>> typeSheetDataMap = new LinkedHashMap<>();
    // Daily time at which the report should be generated. This is a string as entered by the user in the ui in the hh:mm format.
    public String dailyTime;
    // If frequency is WEEKLY_FREQUENCY, the day of the week the report should be generated
    public int dayOfWeek;
    // The time at which the weekly report should be generated. This is a string as entered by the user in the ui in the hh:mm format
    public String weeklyRepGenTime;
    // If frequency is MONTHLY_FREQUENCY, this field indicates that the report should be generated at the beginning of the month, which is the first day if the month.
    public int dayOfMonth = 0;
    // The time at which the monthly report should be generated. This is a string as entered by the user in the ui in the hh:mm format
    public String monthlyRepGenTime;
    // Boolean that indicates if dataDuration is the same as report generation frequency.
    public boolean dataDurationSameAsRepGenFreq = true;
    // The age of data or data duration
    public int dataDuration = 0;
    // List of users ( of manager role ) to whom the reports must be emailed.
    public List<String> managers;
    // List of users( of admin role) to whom the reports must be emailed.
    public List<String> users;
    // List of super users to whom the report must be emailed
    public List<String> superUsers;
    /**
     * List of external users to whom report must be emailed. Comma seperated values.
     */
    public List<String> extUsers;
    public List<String> usrTgs;
    // Creation time
    public Date creationTime;
    // Last updated time
    public Date lastUpdatedTime;

    public Config() {

    }

    // Construct the Config object from the json
    public Config(JSONObject json, Locale locale, String timezone) throws ConfigurationException {
      xLogger.fine("Entering Config constructor");
      // Get the template name
      try {
        this.templateName = json.getString(TEMPLATE_NAME);
      } catch (JSONException e) {
        // ignore
      }
      // file name
      try {
        this.fileName = json.getString(FILENAME);
      } catch (JSONException e) {
        // ignore
      }
      // Description
      try {
        this.description = json.getString(DESCRIPTION);
      } catch (JSONException e) {
        // ignore
      }
      // Template key
      try {
        this.templateKey = json.getString(TEMPLATE_KEY);
      } catch (JSONException e) {
        // ignore
      }
      // Type Sheet data map
      populateTypeSheetDataMapFromJson(json);

      // dailyTime
      try {
        this.dailyTime = json.getString(DAILY_TIME);
      } catch (JSONException e) {
        // ignore
      }
      // dayOfWeek
      try {
        this.dayOfWeek = json.getInt(DAY_OF_WEEK);
      } catch (JSONException e) {
        // ignore
      }
      // weeklyRepGenTime
      try {
        this.weeklyRepGenTime = json.getString(WEEKLY_REPORT_GENERATION_TIME);
      } catch (JSONException e) {
        // ignore
      }
      // dayOfMonth
      try {
        this.dayOfMonth = json.getInt(DAY_OF_MONTH);
      } catch (JSONException e) {
        // ignore
      }
      // monthlyRepGenTime
      try {
        this.monthlyRepGenTime = json.getString(MONTHLY_REPORT_GENERATION_TIME);
      } catch (JSONException e) {
        // ignore
      }
      // dataDurationSameAsExpFreq
      try {
        this.dataDurationSameAsRepGenFreq = json.getBoolean(DATA_DURATION_SAMEAS_REPGENFREQ);
      } catch (JSONException e) {
        // ignore
      }
      // dataDuration
      try {
        this.dataDuration = json.getInt(DATA_DURATION);
      } catch (JSONException e) {
        // ignore
      }
      // Users - manager role
      try {
        this.managers = StringUtil.getList(json.getString(MANAGERS));
      } catch (JSONException e) {
        // ignore
      }
      // Users - admin role
      try {
        this.users = StringUtil.getList(json.getString(USERS));
      } catch (JSONException e) {
        // ignore
      }
      // Superusers
      try {
        this.superUsers = StringUtil.getList(json.getString(SUPERUSERS));
      } catch (JSONException e) {
        // ignore
      }
      // External users
      try {
        this.extUsers = StringUtil.getList(json.getString(EXTERNAL_USERS));
      } catch (JSONException e) {
        // ignore
      }
      // User tags
      try {
        this.usrTgs = StringUtil.getList(json.getString(USER_TAGS));
      } catch (JSONException e) {
        // ignore
      }
      // Creation time
      try {
        long creationTimeinMillis = json.getLong(CREATION_TIME);
        this.creationTime = new Date(creationTimeinMillis);
      } catch (JSONException e) {
        // This code is provided for backward compatibility. This should be removed in the future release.
        try {
          String creationTimeStr = json.getString(CREATION_TIME);
          this.creationTime =
              LocalDateUtil.parseCustom(creationTimeStr, Constants.DATETIME_FORMAT, timezone);
        } catch (JSONException je) {
          // ignore
        } catch (ParseException pe) {
          xLogger.severe("{0} when parsing creation time. Message: {1}", pe.getClass().getName(),
              pe.getMessage());
          throw new ConfigurationException(pe.getMessage());
        }
      }
      // Last updated time
      try {
        long lastUpdatedTimeInMillis = json.getLong(LASTUPDATED_TIME);
        this.lastUpdatedTime = new Date(lastUpdatedTimeInMillis);
      } catch (JSONException e) {
        // This code is provided for backward compatibility. This should be removed in the future release.
        try {
          String lastUpdatedTimeStr = json.getString(LASTUPDATED_TIME);
          this.lastUpdatedTime =
              LocalDateUtil.parseCustom(lastUpdatedTimeStr, Constants.DATETIME_FORMAT, timezone);
        } catch (JSONException je) {
          // ignore
        } catch (ParseException pe) {
          xLogger
              .severe("{0} when parsing last updated time. Message: {1}", pe.getClass().getName(),
                  pe.getMessage());
          throw new ConfigurationException(pe.getMessage());
        }
      }
      xLogger.fine("Exiting Config constructor");
    }

    // Method to get the file name with domain id.
    public static String getFileNameWithDomainId(String fileName, Long domainId) {
      xLogger.fine("Entering getFileNameWithDomainId");
      String newFileName = "";
      if (fileName != null && !fileName.isEmpty() && domainId != null) {
        newFileName += domainId.toString() + SEPARATOR + fileName;
      }
      xLogger.fine("Exiting getFileNameWithDomainId, newFileName: {0}", newFileName);
      return newFileName;
    }

    private void populateTypeSheetDataMapFromJson(JSONObject json) {
      xLogger.fine("Entering populateTypeSheetDataMapFromJson. typeSheetDataMap: {0}",
          this.typeSheetDataMap);
      if (json != null) {
        try {
          // Get the json object for key SHEETDATA
          JSONObject sheetDataJson = json.getJSONObject(SHEETDATA);
          xLogger.fine("sheetDataJson: {0}", sheetDataJson);
          // Get the keys in the sheetDataJson. They will actually be the type names
          @SuppressWarnings("unchecked")
          Iterator<String> jsonKeys = sheetDataJson.keys();
          if (jsonKeys != null) {
            // Create a sheetDataMap for every type.
            while (jsonKeys.hasNext()) {
              String key = jsonKeys.next();
              JSONObject typeJson = sheetDataJson.getJSONObject(key);
              if (key != null && !key.isEmpty()) {
                Map<String, String> sheetDataMap = new HashMap<String, String>();
                // sheetDataMap contains the sheet name for every type.
                sheetDataMap.put(SHEETNAME, typeJson.getString(SHEETNAME));
                // Data duration is present only for type orders, transactions and transactioncounts
                if (CustomReportConstants.TYPE_ORDERS.equals(key)
                    || CustomReportConstants.TYPE_TRANSACTIONS.equals(key)
                    || CustomReportConstants.TYPE_MANUALTRANSACTIONS.equals(key)
                    || CustomReportConstants.TYPE_TRANSACTIONCOUNTS.equals(key)
                    || CustomReportConstants.TYPE_INVENTORYTRENDS.equals(key)) {
                  try {
                    sheetDataMap.put(DATA_DURATION_SAMEAS_REPGENFREQ,
                        typeJson.getString(DATA_DURATION_SAMEAS_REPGENFREQ));
                    // If data duration is different from frequency of report generation, then add data duration also.
                    if (FALSE.equals(typeJson.getString(DATA_DURATION_SAMEAS_REPGENFREQ))) {
                      sheetDataMap.put(DATA_DURATION, typeJson.getString(DATA_DURATION));
                    }
                  } catch (JSONException e) {
                    // For backward compatibility. Can be removed in the future release.
                    sheetDataMap.put(DATA_DURATION_SAMEAS_REPGENFREQ,
                        json.getString(DATA_DURATION_SAMEAS_REPGENFREQ));
                    // data duration is different from report generation frequency, then add dataduration also.
                    if (FALSE.equals(json.getString(DATA_DURATION_SAMEAS_REPGENFREQ))) {
                      sheetDataMap.put(DATA_DURATION, json.getString(DATA_DURATION));
                    }
                  }
                }
                // If type is transactioncounts, addtional parameters aggregatefreq and filterby are present.
                if (CustomReportConstants.TYPE_TRANSACTIONCOUNTS.equals(key)
                    || CustomReportConstants.TYPE_INVENTORYTRENDS.equals(key)) {
                  sheetDataMap.put(AGGREGATEFREQ, typeJson.getString(AGGREGATEFREQ));
                  sheetDataMap.put(FILTERBY, typeJson.getString(FILTERBY));
                }
                // If type is historicalinventorysnapshot, additional parameter dataduration is present.
                if (CustomReportConstants.TYPE_HISTORICAL_INVENTORYSNAPSHOT.equals(key)) {
                  sheetDataMap.put(DATA_DURATION, typeJson.getString(DATA_DURATION));
                }
                this.typeSheetDataMap.put(key, sheetDataMap);
              }
            }
            // Transaction data is moved to last to avoid streaming of big data for every report
            Map<String, String> transData = null;
            if (this.typeSheetDataMap.get(CustomReportConstants.TYPE_TRANSACTIONS) != null) {
              transData = this.typeSheetDataMap.get(CustomReportConstants.TYPE_TRANSACTIONS);
              this.typeSheetDataMap.remove(CustomReportConstants.TYPE_TRANSACTIONS);
            }
            if (transData != null) {
              this.typeSheetDataMap.put(CustomReportConstants.TYPE_TRANSACTIONS, transData);
            }
          }
        } catch (JSONException e) {
          // ignore
        }
      }
      xLogger.fine("Exiting populateTypeSheetDataMapFromJson. typeSheetDataMap: {0}",
          this.typeSheetDataMap);
    }

    // Convert the Config object to JSONObject
    public JSONObject toJSONObject() throws ConfigurationException {
      xLogger.fine("Entering Config.toJSONObject");
      try {
        JSONObject json = new JSONObject();
        if (this.templateName != null && !this.templateName.isEmpty()) {
          json.put(TEMPLATE_NAME, this.templateName);
        }
        if (this.fileName != null && !this.fileName.isEmpty()) {
          json.put(FILENAME, this.fileName);
        }
        if (this.description != null && !this.description.isEmpty()) {
          json.put(DESCRIPTION, this.description);
        }
        if (this.templateKey != null && !this.templateKey.isEmpty()) {
          json.put(TEMPLATE_KEY, this.templateKey);
        }
        if (this.typeSheetDataMap != null && !this.typeSheetDataMap.isEmpty()) {
          JSONObject sheetDataJson = new JSONObject();
          // Get the keys in the map.
          Set<String> keys = typeSheetDataMap.keySet();
          if (keys != null) {
            // Iterate over the keys and get the values.
            Iterator<String> keysIter = keys.iterator();
            while (keysIter.hasNext()) {
              String type = keysIter.next();
              if (type != null && !type.isEmpty()) {
                Map<String, String> sheetData = typeSheetDataMap.get(type);
                if (sheetData != null && !sheetData.isEmpty()) {
                  JSONObject typeJson = new JSONObject();
                  typeJson.put(SHEETNAME,
                      sheetData.get(SHEETNAME)); // sheetname is added for all types.
                  // If type is orders or transactions or transactioncounts, add keys datadurationsamegenfreq and dataduration
                  if (CustomReportConstants.TYPE_ORDERS.equals(type)
                      || CustomReportConstants.TYPE_TRANSACTIONS.equals(type)
                      || CustomReportConstants.TYPE_MANUALTRANSACTIONS.equals(type)
                      || CustomReportConstants.TYPE_TRANSACTIONCOUNTS.equals(type)
                      || CustomReportConstants.TYPE_INVENTORYTRENDS.equals(type)) {
                    typeJson.put(DATA_DURATION_SAMEAS_REPGENFREQ,
                        sheetData.get(DATA_DURATION_SAMEAS_REPGENFREQ));
                    // If the data duration is not the same as frequency of report generation, then add the data duration also.
                    if (FALSE.equals(sheetData.get(DATA_DURATION_SAMEAS_REPGENFREQ))) {
                      typeJson.put(DATA_DURATION, sheetData.get(DATA_DURATION));
                    }
                  }
                  // If type is transactioncounts, add an additional key aggregateby and filterby
                  if (CustomReportConstants.TYPE_TRANSACTIONCOUNTS.equals(type)
                      || CustomReportConstants.TYPE_INVENTORYTRENDS.equals(type)) {
                    typeJson.put(AGGREGATEFREQ, sheetData.get(AGGREGATEFREQ));
                    typeJson.put(FILTERBY, sheetData.get(FILTERBY));
                  }
                  // If type is historicalinventorysnapshot, add an additional key dataduration
                  if (CustomReportConstants.TYPE_HISTORICAL_INVENTORYSNAPSHOT.equals(type)) {
                    typeJson.put(DATA_DURATION, sheetData.get(DATA_DURATION));
                  }
                  sheetDataJson.put(type, typeJson);
                }
              }
            }
            // Now add the sheetDataJson to json
            json.put(SHEETDATA, sheetDataJson);
          }
        }
        if (this.dailyTime != null && !this.dailyTime.isEmpty()) {
          json.put(DAILY_TIME, this.dailyTime);
        }
        json.put(DAY_OF_WEEK, this.dayOfWeek);
        if (this.weeklyRepGenTime != null && !this.weeklyRepGenTime.isEmpty()) {
          json.put(WEEKLY_REPORT_GENERATION_TIME, this.weeklyRepGenTime);
        }
        json.put(DAY_OF_MONTH, this.dayOfMonth);
        if (this.monthlyRepGenTime != null && !this.monthlyRepGenTime.isEmpty()) {
          json.put(MONTHLY_REPORT_GENERATION_TIME, this.monthlyRepGenTime);
        }
        if (this.managers != null && !this.managers.isEmpty()) {
          json.put(MANAGERS, StringUtil.getCSV(this.managers));
        }
        if (this.users != null && !this.users.isEmpty()) {
          json.put(USERS, StringUtil.getCSV(this.users));
        }
        if (this.superUsers != null && !this.superUsers.isEmpty()) {
          json.put(SUPERUSERS, StringUtil.getCSV(this.superUsers));
        }
        if (this.extUsers != null && !this.extUsers.isEmpty()) {
          json.put(EXTERNAL_USERS, StringUtil.getCSV(this.extUsers));
        }
        if (this.usrTgs != null && !this.usrTgs.isEmpty()) {
          json.put(USER_TAGS, StringUtil.getCSV(this.usrTgs));
        }
        if (this.creationTime != null) {
          json.put(CREATION_TIME, creationTime.getTime());
        }
        if (this.lastUpdatedTime != null) {
          json.put(LASTUPDATED_TIME, lastUpdatedTime.getTime());
        }
        xLogger.fine("Exiting Config.toJSONObject. json: {0}", json);
        return json;
      } catch (Exception e) {
        throw new ConfigurationException(e.getMessage());
      }
    }

    @Override
    public int compareTo(Config o) {
      if (o == null || o.lastUpdatedTime == null) {
        return -1;
      }
      if (this.lastUpdatedTime == null) {
        return 1;
      }

      return o.lastUpdatedTime.compareTo(this.lastUpdatedTime);
    }

    public static class ConfigTemplateNameComparator implements Comparator<Config> {
      @Override
      public int compare(Config c1, Config c2) {
        return c1.templateName.compareTo(c2.templateName);
      }
    }
  }
}

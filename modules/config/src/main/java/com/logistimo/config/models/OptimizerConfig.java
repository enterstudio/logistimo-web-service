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

/**
 *
 */
package com.logistimo.config.models;

import com.logistimo.config.entity.IConfig;
import com.logistimo.config.service.ConfigurationMgmtService;

import org.json.JSONException;
import org.json.JSONObject;
import com.logistimo.services.Services;
import com.logistimo.constants.Constants;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * @author arun
 *
 *         Stores the configuration of parameters for optimization
 */
public class OptimizerConfig implements Serializable {

  // What to compute
  public static final int COMPUTE_NONE = -1;
  public static final int COMPUTE_FORECASTEDDEMAND = 100; // also compute consumption
  public static final int
      COMPUTE_EOQ =
      200;

  public static final int LEAD_TIME_DEFAULT_VALUE = 3; // days

  /**
   *
   */
  private static final long serialVersionUID = 1L;
  private static final int P_AND_S_FREQUENCY = 168; // hours (7 days default)
  private static final int D_AND_Q_FREQUENCY = 24; // hours (once a day default)
  // Key names
  private static final String INVENTORY_MODEL = "inventory-model";
  private static final String MIN_AVG_ORDER_PERIODICITY = "min-avg-order-periodicity";
  private static final String MIN_HISTORICAL_PERIOD = "min-historical-period";
  private static final String MAX_HISTORICAL_PERIOD = "max-historical-period";
  private static final String NUM_PERIODS = "num-periods";
  private static final String LEAD_TIME_DEFAULT = "lead-time-default";
  private static final String COMPUTE = "compute";
  private static final String EXCEPTION_EMAILS = "ex-emails";
  private static final String DISPLAY_DF = "ddf";
  private static final String DISPLAY_OOQ = "dooq";
  private static final String COMPUTE_FREQ = "crfreq";
  private static final String EXCLUDE_TR_ORD = "etord";
  private static final String EXCLUDE_DISCARDS = "edis";
  private static final String EXCLUDE_REASONS = "ersns";
  private static final String LEAD_TIME_AVERAGE_CONFIG = "ltac";
  // economic order quantity; also compute consumption and demand forecast
  private String inventoryModel = "sq";
  private String computeFrequency = Constants.FREQ_DAILY; // [daily/monthly/weekly]
  private float minAvgOrderPeriodicity = 10; // days
  private float minHistoricalPeriod = 10; // days
  private float maxHistoricalPeriod = 365; // days
  private float numPeriods = 3; // periods
  private float leadTimeDefault = 3; // days
  private int compute = COMPUTE_NONE;
  private String exEmails = null; // csv of email addresses
  private boolean ddf = false; // Display demand forecast
  private boolean dooq = false; // Display optimal order quantity
  private boolean edis = true; // Exclude discards
  private String ersns = null; // Exclude reasons
  private LeadTimeAvgConfig leadTimeAvgCfg = new LeadTimeAvgConfig(); // Configuration for computing lead time average.

  public OptimizerConfig() {
    this.leadTimeAvgCfg = new LeadTimeAvgConfig();
  }

  public OptimizerConfig(JSONObject json) throws ConfigurationException {
    try {
      this.inventoryModel = json.optString(INVENTORY_MODEL, this.inventoryModel);
      this.minAvgOrderPeriodicity =
          json.optInt(MIN_AVG_ORDER_PERIODICITY, (int) this.minAvgOrderPeriodicity);
      this.minHistoricalPeriod = (float) json.optDouble(MIN_HISTORICAL_PERIOD,
          (double) this.minHistoricalPeriod);
      this.maxHistoricalPeriod =
          (float) json.optDouble(MAX_HISTORICAL_PERIOD, (double) this.maxHistoricalPeriod);
      this.numPeriods = json.optInt(NUM_PERIODS, (int) this.numPeriods);
      this.leadTimeDefault = json.optInt(LEAD_TIME_DEFAULT, (int) this.leadTimeDefault);
      this.compute = json.optInt(COMPUTE, this.compute);
      this.exEmails = json.optString(EXCEPTION_EMAILS, this.exEmails);
      this.ddf = json.optBoolean(DISPLAY_DF, this.ddf);
      this.dooq = json.optBoolean(DISPLAY_OOQ, this.dooq);
      this.computeFrequency = json.optString(COMPUTE_FREQ, Constants.FREQ_DAILY);
      this.edis = json.optBoolean(EXCLUDE_DISCARDS, this.edis);
      this.ersns = json.optString(EXCLUDE_REASONS, this.ersns);
      try {
        this.leadTimeAvgCfg = new LeadTimeAvgConfig(json.getJSONObject(LEAD_TIME_AVERAGE_CONFIG));
      } catch (JSONException e) {
        // ignore
      }
    } catch (Exception e) {
      throw new ConfigurationException(e.getMessage());
    }
  }

  // Get the specified run frequencies, if configured
  public static Map<String, Integer> getRunFrequencies() {
    Map<String, Integer> freqs = new HashMap<String, Integer>();
    boolean makeDefault = false;
    try {
      ConfigurationMgmtService cms = Services.getService(ConfigurationMgmtService.class);
      IConfig c = cms.getConfiguration(IConfig.OPTIMIZATION);
      String data = c.getConfig();
      if (data == null || data.isEmpty()) {
        makeDefault = true;
      } else {
        JSONObject json = new JSONObject(data);
        JSONObject jsonFreqs = json.getJSONObject("freqs");
        freqs.put(Constants.TYPE_DQ, new Integer(jsonFreqs.getInt(Constants.TYPE_DQ)));
        freqs.put(Constants.TYPE_PS, new Integer(jsonFreqs.getInt(Constants.TYPE_PS)));
      }
    } catch (Exception e) {
      makeDefault = true;
    }
    if (makeDefault) {
      freqs.put(Constants.TYPE_PS, P_AND_S_FREQUENCY);
      freqs.put(Constants.TYPE_DQ, D_AND_Q_FREQUENCY);
    }
    return freqs;
  }

  public String getInventoryModel() {
    return inventoryModel;
  }

  public void setInventoryModel(String inventoryModel) {
    this.inventoryModel = inventoryModel;
  }

  public float getMinAvgOrderPeriodicity() {
    return minAvgOrderPeriodicity;
  }

  public void setMinAvgOrderPeriodicity(float minAvgOrderPeriodicity) {
    this.minAvgOrderPeriodicity = minAvgOrderPeriodicity;
  }

  public float getMinHistoricalPeriod() {
    return minHistoricalPeriod;
  }

  public void setMinHistoricalPeriod(float minHistoricalPeriod) {
    this.minHistoricalPeriod = minHistoricalPeriod;
  }

  public float getMaxHistoricalPeriod() {
    return maxHistoricalPeriod;
  }

  public void setMaxHistoricalPeriod(float maxHistoricalPeriod) {
    this.maxHistoricalPeriod = maxHistoricalPeriod;
  }

  public float getNumPeriods() {
    return numPeriods;
  }

  public void setNumPeriods(float numPeriods) {
    this.numPeriods = numPeriods;
  }

  public float getLeadTimeDefault() {
    return leadTimeDefault;
  }

  public void setLeadTimeDefault(float leadTimeDefault) {
    this.leadTimeDefault = leadTimeDefault;
  }

  public int getCompute() {
    return compute;
  } //Compute forecasting

  public void setCompute(int compute) {
    this.compute = compute;
  }

  public String getComputeFrequency() {
    return computeFrequency;
  }

  public void setComputeFrequency(String computeFrequency) {
    this.computeFrequency = computeFrequency;
  }

  public boolean isExcludeDiscards() {
    return edis;
  }

  public void setExcludeDiscards(boolean edis) {
    this.edis = edis;
  }

  public String getExcludeReasons() {
    return ersns;
  }

  public void setExcludeReasons(String ersns) {
    this.ersns = ersns;
  }

  public String getExceptionEmails() {
    return exEmails;
  }

  public void setExceptionEmails(String csvEmails) {
    this.exEmails = csvEmails;
  }

  public boolean isOptimizationRequired() {
    return !(compute == COMPUTE_NONE);
  }

  public boolean isDisplayDF() {
    return ddf;
  }

  public void setDisplayDF(boolean ddf) {
    this.ddf = ddf;
  }

  public boolean isDisplayOOQ() {
    return dooq;
  }

  public void setDisplayOOQ(boolean dooq) {
    this.dooq = dooq;
  }

  public LeadTimeAvgConfig getLeadTimeAvgCfg() {
    return this.leadTimeAvgCfg;
  }
  public void setLeadTimeAvgCfg(LeadTimeAvgConfig leadTimeAvgCfg) {
    this.leadTimeAvgCfg = leadTimeAvgCfg;
  }

  public JSONObject toJSONObject() throws ConfigurationException {
    JSONObject json = null;
    try {
      json = new JSONObject();
      json.put(INVENTORY_MODEL, this.inventoryModel);
      json.put(MIN_AVG_ORDER_PERIODICITY, this.minAvgOrderPeriodicity);
      json.put(MIN_HISTORICAL_PERIOD, this.minHistoricalPeriod);
      json.put(MAX_HISTORICAL_PERIOD, this.maxHistoricalPeriod);
      json.put(NUM_PERIODS, this.numPeriods);
      json.put(LEAD_TIME_DEFAULT, this.leadTimeDefault);
      json.put(COMPUTE, String.valueOf(compute));
      if (exEmails != null && !exEmails.isEmpty()) {
        json.put(EXCEPTION_EMAILS, exEmails);
      }
      json.put(DISPLAY_DF, ddf);
      json.put(DISPLAY_OOQ, dooq);
      json.put(COMPUTE_FREQ, computeFrequency);
      json.put(EXCLUDE_DISCARDS, edis);
      json.put(EXCLUDE_REASONS, ersns);
      json.put(LEAD_TIME_AVERAGE_CONFIG, leadTimeAvgCfg.toJSONObject());
    } catch (JSONException e) {
      throw new ConfigurationException(e.getMessage());
    }
    return json;
  }
}
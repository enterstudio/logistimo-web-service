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
import com.logistimo.logger.XLog;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

public class PaymentsConfig implements Serializable {
  // Json tags
  public static final String PAYMENTS_ENABLED = "paymentsenabled";
  public static final String PAYMENT_ACCOUNTS = "paymentaccounts";
  public static final String PROVIDER_ID = "providerid";
  public static final String ACCOUNT_NAME = "accountname";
  public static final String ACCOUNT_USER = "accountuser";
  public static final String ACCESS_NAME = "accessusername";
  public static final String ACCESS_PASSWORD = "accesspassword";
  private static final long serialVersionUID = -6231148078583991869L;
  private static final XLog xLogger = XLog.getLog(PaymentsConfig.class);
  private static final String CREATION_TIME = "creationtime";
  private static final String LASTUPDATED_TIME = "lastupdatedtime";
  private static final String BALANCE = "balance";

  boolean isPaymentsEnabled = false;

  List<PaymentConfig> paymentsConfig = new ArrayList<PaymentConfig>();

  // Default constructor
  public PaymentsConfig() {
  }

  public PaymentsConfig(JSONObject json, Locale locale, String timezone) {
    xLogger.fine("Entering PaymentsConfig constructor");
    try {
      isPaymentsEnabled = json.getBoolean(PAYMENTS_ENABLED);
      JSONObject paymentAccountsJson = json.getJSONObject(PAYMENT_ACCOUNTS);

      @SuppressWarnings("rawtypes")
      Iterator keys = paymentAccountsJson.keys();
      if (keys != null) {
        while (keys.hasNext()) {
          String key = (String) keys.next();
          JSONObject paymentconfigJson = paymentAccountsJson.getJSONObject(key);
          PaymentConfig config = new PaymentConfig(paymentconfigJson, locale, timezone);
          paymentsConfig.add(config);
        }
      }
    } catch (Exception e) {
      // ignore
    }
    xLogger.fine("Exiting PaymentsConfig constructor");
  }

  public JSONObject toJSONObject() throws ConfigurationException {
    xLogger.fine("Entering toJSONObject. paymentsConfig: {0}", paymentsConfig.toString());

    try {
      JSONObject json = new JSONObject();
      json.put(PAYMENTS_ENABLED, isPaymentsEnabled);
      JSONObject paymentAccountsJson = new JSONObject();
      if (paymentsConfig != null && !paymentsConfig.isEmpty() && paymentsConfig.size() != 0) {
        // Iterate over the list and convert each of the PaymentConfig objects to Json object and add it to the paymentsConfigJson
        Iterator<PaymentConfig> paymentsConfigIter = paymentsConfig.iterator();
        while (paymentsConfigIter.hasNext()) {
          PaymentConfig paymentconfig = paymentsConfigIter.next();
          paymentAccountsJson.put(paymentconfig.accountName, paymentconfig
              .toJSONObject()); // Add the user name as key and the paymentconfig json object as the value to json
        }
        json.put(PAYMENT_ACCOUNTS, paymentAccountsJson);
      }
      xLogger.fine("Exiting toJSONObject");
      return json; // Return the payments json object
    } catch (Exception e) {
      throw new ConfigurationException(e.getMessage());
    }
  }

  public List<PaymentConfig> getPaymentsConfig() {
    return paymentsConfig;
  }

  /*
   * This method removes a PaymentConfig object with the specified accountName from PaymentsConfig.
   */
  public void removePaymentConfig(String accountName) {
    xLogger.fine("Entering removePaymentConfig");
    if (accountName != null && !accountName.isEmpty()) {
      PaymentConfig paymentconfig = getPaymentConfig(accountName);
      if (paymentconfig != null) {
        paymentsConfig.remove(paymentconfig);
      }
    }
    xLogger.fine("Exiting removePaymentConfig");
  }

  /*
   * This method gets the PaymentConfig object specified by the account name.
   */
  public PaymentConfig getPaymentConfig(String accountName) {
    xLogger.info("Entering getPaymentConfig, accountName: {0}", accountName);
    if (accountName != null && !accountName.isEmpty()) {
      // Iterate through customReportsConfig.
      Iterator<PaymentConfig> paymentsConfigIter = paymentsConfig.iterator();
      while (paymentsConfigIter.hasNext()) {
        PaymentConfig paymentconfig = paymentsConfigIter.next();
        if (paymentconfig.accountName.equals(accountName)) {
          return paymentconfig;
        }
      }
    }
    xLogger.info("Payment Account with name {0} not found ", accountName);
    return null;
  }

  public boolean isPaymentsEnabled() {
    return isPaymentsEnabled;
  }

  public void setPaymentsEnabled(boolean paymentsEnabled) {
    this.isPaymentsEnabled = paymentsEnabled;
  }

  public static class PaymentConfig implements Serializable {
    private static final long serialVersionUID = 5550558373263144706L;
    private static final XLog xLogger = XLog.getLog(PaymentConfig.class);
    // Payment provider id
    public String providerId;
    // Payment Account Name
    public String accountName;
    // Payment Account User in Logistimo
    public String accountUser;
    // Access user name
    public String accessName;
    // Acesss password
    public String accessPassword;
    // Creation time
    public Date creationTime;
    // Last updated time
    public Date lastUpdatedTime;
    // Balance
    public double balance = 0;

    // Constructor
    public PaymentConfig() {

    }

    // Construct the PaymentConfig object from the json
    public PaymentConfig(JSONObject json, Locale locale, String timezone)
        throws ConfigurationException {
      xLogger.fine("Entering PaymentConfig constructor");
      // Get the provider id
      try {
        this.providerId = json.getString(PROVIDER_ID);
      } catch (JSONException e) {
        // ignore
      }
      // Get the account name
      try {
        this.accountName = json.getString(ACCOUNT_NAME);
      } catch (JSONException e) {
        // ignore
      }
      // Get the account user
      try {
        this.accountUser = json.getString(ACCOUNT_USER);
      } catch (JSONException e) {
        // ignore
      }
      // Get the access user name
      try {
        this.accessName = json.getString(ACCESS_NAME);
      } catch (JSONException e) {
        // ignore
      }
      // Get the access password
      try {
        this.accessPassword = json.getString(ACCESS_PASSWORD);
      } catch (JSONException e) {
        // ignore
      }
      // Creation time
      try {
        long creationTimeInMillis = json.getLong(CREATION_TIME);
        this.creationTime = new Date(creationTimeInMillis);
      } catch (JSONException e) {
        // ignore
      }
      // Last updated time
      try {
        long lastUpdatedTimeinMillis = json.getLong(LASTUPDATED_TIME);
        this.lastUpdatedTime = new Date(lastUpdatedTimeinMillis);
      } catch (JSONException e) {
        // ignore
      }
      // Balance in the account
      try {
        this.balance = json.getDouble(BALANCE);
      } catch (JSONException je) {
        // ignore
      }
    }

    // Convert the PaymentConfig object to JSONObject
    public JSONObject toJSONObject() throws ConfigurationException {
      xLogger.info("Entering PaymentConfig.toJSONObject");
      try {
        JSONObject json = new JSONObject();
        if (this.providerId != null && !this.providerId.isEmpty()) {
          json.put(PROVIDER_ID, this.providerId);
        }
        if (this.accountName != null && !this.accountName.isEmpty()) {
          json.put(ACCOUNT_NAME, this.accountName);
        }
        if (this.accountUser != null && !this.accountUser.isEmpty()) {
          json.put(ACCOUNT_USER, this.accountUser);
        }
        if (this.accessName != null && !this.accessName.isEmpty()) {
          json.put(ACCESS_NAME, this.accessName);
        }
        if (this.accessPassword != null && !this.accessPassword.isEmpty()) {
          json.put(ACCESS_PASSWORD, this.accessPassword);
        }
        if (this.creationTime != null) {
          json.put(CREATION_TIME, creationTime.getTime());
        }
        if (this.lastUpdatedTime != null) {
          json.put(LASTUPDATED_TIME, lastUpdatedTime.getTime());
        }
        json.put(BALANCE, this.balance);
        xLogger.info("Exiting PaymentConfig.toJSONObject. json: {0}", json);
        return json;
      } catch (Exception e) {
        throw new ConfigurationException(e.getMessage());
      }
    }

    public static class PaymentConfigAccountNameComparator implements Comparator<PaymentConfig> {
      @Override
      public int compare(PaymentConfig c1, PaymentConfig c2) {
        return c1.accountName.compareTo(c2.accountName);
      }
    }
  }
}

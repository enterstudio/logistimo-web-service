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

import org.json.JSONException;
import org.json.JSONObject;
import com.logistimo.utils.BigUtil;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * Represents accounting configuration in a domain
 *
 * @author Arun
 */
public class AccountingConfig implements Serializable {

  public static final String ENABLE_ACCOUNTING = "enbacc";
  public static final String CREDIT_LIMIT = "crdlmt";
  public static final String ENFORCE_CONFIRM = "enfcnf";
  public static final String ENFORCE_SHIPPED = "enfshp";
  private static final long serialVersionUID = 1L;
  private boolean enableAccounting = false;
  private BigDecimal creditLimit = BigDecimal.ZERO; // default credit limit
  private boolean enforceConfirm = false; // enforce before order confirmation
  private boolean enforceShipped = false; // enfore before order shipment

  public AccountingConfig(JSONObject json) {
    // Enable accounting
    try {
      enableAccounting = json.getBoolean(ENABLE_ACCOUNTING);
    } catch (JSONException e) {
      // do nothing
    }
    // Credit limit
    try {
      String creditLimitStr = json.getString(CREDIT_LIMIT);
      if (!creditLimitStr.isEmpty()) {
        creditLimit = new BigDecimal(creditLimitStr);
      }
    } catch (Exception e) {
      // do nothing
    }
    // Enforce confirm
    try {
      enforceConfirm = json.getBoolean(ENFORCE_CONFIRM);
    } catch (JSONException e) {
      // do nothing
    }
    // Enforce shipped
    try {
      enforceShipped = json.getBoolean(ENFORCE_SHIPPED);
    } catch (JSONException e) {
      // do nothing
    }
  }

  public AccountingConfig() {
  }

  public JSONObject toJSONObject() throws ConfigurationException {
    JSONObject json = new JSONObject();
    try {
      json.put(ENABLE_ACCOUNTING, enableAccounting);
      if (BigUtil.notEqualsZero(creditLimit)) {
        json.put(CREDIT_LIMIT, String.valueOf(creditLimit));
      }
      json.put(ENFORCE_CONFIRM, enforceConfirm);
      json.put(ENFORCE_SHIPPED, enforceShipped);
    } catch (JSONException e) {
      throw new ConfigurationException(e.getMessage());
    }
    return json;
  }

  public boolean isAccountingEnabled() {
    return enableAccounting;
  }

  public void setAccountingEnabled(boolean enabled) {
    this.enableAccounting = enabled;
  }

  public BigDecimal getCreditLimit() {
    return creditLimit;
  }

  public void setCreditLimit(BigDecimal creditLimit) {
    this.creditLimit = creditLimit;
  }

  public boolean enforceConfirm() {
    return enforceConfirm;
  }

  public void setEnforceConfirm(boolean enforceConfirm) {
    this.enforceConfirm = enforceConfirm;
  }

  public boolean enforceShipped() {
    return enforceShipped;
  }

  public void setEnforceShipped(boolean enforceShipped) {
    this.enforceShipped = enforceShipped;
  }
}

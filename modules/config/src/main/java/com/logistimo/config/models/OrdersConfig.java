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
import com.logistimo.utils.StringUtil;

import java.io.Serializable;
import java.util.List;

/**
 * Stores order configuration
 * (stores config. from order scheduled-export config. onwards; TODO: rest of orders configuration from DomainConfig to here)
 *
 * @author Arun
 */
public class OrdersConfig implements Serializable {

  private static final long serialVersionUID = 1L;
  // JSON Tags
  private static final String TIMES = "times";
  private static final String EXPORTUSERIDS = "expusrids";
  private static final String ENABLED = "enabled";
  private static final String SOURCEUSERID = "suid"; // source user ID
  private static final String RSNENABLED = "renabled";
  private static final String OREASON = "orsn"; // Recommended order reason
  private static final String MANDATORY = "md"; // Mark reasons field optional
  private static final String USER_TAGS_SCHEDULE_ORDER_EXPORT = "usertgs";
  private static final String MARK_SALES_ORDER_CONFIRMED = "msoc";
  private static final String ALLOCATE_STOCK_ON_CONFIRMATION = "asc";
  private static final String TRANSFER_RELEASE = "tr";
  private static final String ORDER_RECOMMENDATION_REASONS = "orr";
  private static final String ORDER_RECOMMENDATION_REASONS_MANDATORY = "orrm";
  private static final String EDITING_QUANTITY_REASONS = "eqr";
  private static final String EDITING_QUANTITY_REASONS_MANDATORY = "eqrm";
  private static final String PARTIAL_SHIPMENT_REASONS = "psr";
  private static final String PARTIAL_SHIPMENT_REASONS_MANDATORY = "psrm";
  private static final String PARTIAL_FULFILLMENT_REASONS = "pfr";
  private static final String PARTIAL_FULFILLMENT_REASONS_MANDATORY = "pfrm";
  private static final String CANCELLING_ORDER_REASONS = "cor";
  private static final String CANCELLING_ORDER_REASONS_MANDATORY = "corm";
  private static final String ALLOW_CREATING_SHIPMENTS = "asc";
  String sourceUserId = null; // user who last created the export specification
  private boolean exportEnabled = false;
  private String exportUserIdCSV = null;
  private String exportTimesCSV = null; // list of export times
  private boolean rsnEnabled;
  private String orderReason;
  private boolean mandatory;
  private List<String> userTags;
  private boolean allowSalesOrderAsConfirmed;
  private boolean allocateStockOnConfirmation;
  private boolean transferRelease;
  private String orReasons;
  private boolean orReasonsMandatory;
  private String eqReasons;
  private boolean eqReasonsMandatory;
  private String psReasons;
  private boolean psReasonsMandatory;
  private String pfReasons;
  private boolean pfReasonsMandatory;
  private String coReasons;
  private boolean coReasonsMandatory;
  private boolean allowCreatingShipments;

  public OrdersConfig() {
  }

  public OrdersConfig(JSONObject json) {
    try {
      exportEnabled = json.getBoolean(ENABLED);
    } catch (JSONException e) {
      // ignore
    }
    try {
      exportUserIdCSV = json.getString(EXPORTUSERIDS);
    } catch (JSONException e) {
      // ignore
    }
    try {
      exportTimesCSV = json.getString(TIMES);
    } catch (JSONException e) {
      // ignore
    }
    try {
      sourceUserId = json.getString(SOURCEUSERID);
    } catch (JSONException e) {
      // ignore
    }
    try {
      orderReason = json.getString(OREASON);
    } catch (JSONException e) {
      //ignore
    }
    try {
      rsnEnabled = json.getBoolean(RSNENABLED);
    } catch (JSONException e) {
      // ignore
    }
    try {
      mandatory = json.getBoolean(MANDATORY);
    } catch (JSONException e) {
      //ignore
    }
    try {
      this.userTags = StringUtil.getList(json.getString(USER_TAGS_SCHEDULE_ORDER_EXPORT));
    } catch (JSONException e) {
      //ignore
    }
    try {
      allowSalesOrderAsConfirmed = json.getBoolean(MARK_SALES_ORDER_CONFIRMED);
    } catch (JSONException e) {
      //ignore
    }
    try {
      allocateStockOnConfirmation = json.getBoolean(ALLOCATE_STOCK_ON_CONFIRMATION);
    } catch (JSONException e) {
      //ignore
    }
    try {
      transferRelease = json.getBoolean(TRANSFER_RELEASE);
    } catch (JSONException e) {
      //ignore
    }
    try {
      orReasons = json.getString(ORDER_RECOMMENDATION_REASONS);
    } catch (JSONException e) {
      //ignore
    }
    try {
      orReasonsMandatory = json.getBoolean(ORDER_RECOMMENDATION_REASONS_MANDATORY);
    } catch (JSONException e) {
      //ignore
    }
    try {
      eqReasons = json.getString(EDITING_QUANTITY_REASONS);
    } catch (JSONException e) {
      //ignore
    }
    try {
      eqReasonsMandatory = json.getBoolean(EDITING_QUANTITY_REASONS_MANDATORY);
    } catch (JSONException e) {
      //ignore
    }
    try {
      psReasons = json.getString(PARTIAL_SHIPMENT_REASONS);
    } catch (JSONException e) {
      //ignore
    }
    try {
      psReasonsMandatory = json.getBoolean(PARTIAL_SHIPMENT_REASONS_MANDATORY);
    } catch (JSONException e) {
      //ignore
    }
    try {
      pfReasons = json.getString(PARTIAL_FULFILLMENT_REASONS);
    } catch (JSONException e) {
      //ignore
    }
    try {
      pfReasonsMandatory = json.getBoolean(PARTIAL_FULFILLMENT_REASONS_MANDATORY);
    } catch (JSONException e) {
      //ignore
    }
    try {
      coReasons = json.getString(CANCELLING_ORDER_REASONS);
    } catch (JSONException e) {
      //ignore
    }
    try {
      coReasonsMandatory = json.getBoolean(CANCELLING_ORDER_REASONS_MANDATORY);
    } catch (JSONException e) {
      //ignore
    }
    try {
      allowCreatingShipments = json.getBoolean(ALLOW_CREATING_SHIPMENTS);
    } catch (JSONException e) {
      //ignore
    }
  }

  public JSONObject toJSONObject() throws ConfigurationException {
    try {
      JSONObject json = new JSONObject();
      json.put(ENABLED, exportEnabled);
      if (exportUserIdCSV != null && !exportUserIdCSV.isEmpty()) {
        json.put(EXPORTUSERIDS, exportUserIdCSV);
      }
      if (exportTimesCSV != null && !exportTimesCSV.isEmpty()) {
        json.put(TIMES, exportTimesCSV);
      }
      if (sourceUserId != null && !sourceUserId.isEmpty()) {
        json.put(SOURCEUSERID, sourceUserId);
      }

      if (orReasons != null && !orReasons.isEmpty()) {
        json.put(ORDER_RECOMMENDATION_REASONS, orReasons);
      }
      json.put(ORDER_RECOMMENDATION_REASONS_MANDATORY, orReasonsMandatory);

      if (eqReasons != null && !eqReasons.isEmpty()) {
        json.put(EDITING_QUANTITY_REASONS, eqReasons);
      }
      json.put(EDITING_QUANTITY_REASONS_MANDATORY, eqReasonsMandatory);

      if (psReasons != null && !psReasons.isEmpty()) {
        json.put(PARTIAL_SHIPMENT_REASONS, psReasons);
      }
      json.put(PARTIAL_SHIPMENT_REASONS_MANDATORY, psReasonsMandatory);

      if (pfReasons != null && !pfReasons.isEmpty()) {
        json.put(PARTIAL_FULFILLMENT_REASONS, pfReasons);
      }
      json.put(PARTIAL_FULFILLMENT_REASONS_MANDATORY, pfReasonsMandatory);

      if (coReasons != null && !coReasons.isEmpty()) {
        json.put(CANCELLING_ORDER_REASONS, coReasons);
      }
      json.put(CANCELLING_ORDER_REASONS_MANDATORY, coReasonsMandatory);
      json.put(MANDATORY, mandatory);
      if (userTags != null && !userTags.isEmpty()) {
        json.put(USER_TAGS_SCHEDULE_ORDER_EXPORT, StringUtil.getCSV(userTags));
      }
      json.put(MARK_SALES_ORDER_CONFIRMED, allowSalesOrderAsConfirmed);
      json.put(ALLOCATE_STOCK_ON_CONFIRMATION, allocateStockOnConfirmation);
      json.put(TRANSFER_RELEASE, transferRelease);
      json.put(ALLOW_CREATING_SHIPMENTS, allowCreatingShipments);
      return json;
    } catch (JSONException e) {
      throw new ConfigurationException(e.getMessage());
    }
  }

  public boolean isExportEnabled() {
    return exportEnabled;
  }

  public void setExportEnabled(boolean isEnabled) {
    exportEnabled = isEnabled;
  }

  public String getExportUserIds() {
    return exportUserIdCSV;
  } // CSV

  public void setExportUserIds(String userIdCSV) {
    exportUserIdCSV = userIdCSV;
  }

  public String getExportTimes() {
    return exportTimesCSV;
  } // CSV

  public void setExportTimes(String timesCSV) {
    exportTimesCSV = timesCSV;
  }

  public String getSourceUserId() {
    return sourceUserId;
  }

  public void setSourceUserId(String userId) {
    sourceUserId = userId;
  }

  public boolean isReasonsEnabled() {
    return rsnEnabled;
  }

  public void setReasonsEnabled(boolean reasonsEnabled) {
    rsnEnabled = reasonsEnabled;
  }

  public String getOrderReason() {
    return orderReason;
  }

  public void setOrderReason(String reason) {
    orderReason = reason;
  }

  public boolean isMandatory() {
    return mandatory;
  }

  public void setMandatory(boolean op) {
    mandatory = op;
  }

  public List<String> getUserTags() {
    return userTags;
  }

  public void setUserTags(List<String> userTags) {
    this.userTags = userTags;
  }

  public boolean allowSalesOrderAsConfirmed() {
    return allowSalesOrderAsConfirmed;
  }

  public void setAllowMarkOrderAsConfirmed(boolean allowConfirmed) {
    allowSalesOrderAsConfirmed = allowConfirmed;
  }

  public boolean allocateStockOnConfirmation() {
    return allocateStockOnConfirmation;
  }

  public void setAllocateStockOnConfirmation(boolean allocateStock) {
    allocateStockOnConfirmation = allocateStock;
  }

  public boolean isTransferRelease() {
    return transferRelease;
  }

  public void setTransferRelease(boolean transferRelease) {
    this.transferRelease = transferRelease;
  }

  public String getOrderRecommendationReasons() {
    return orReasons;
  }

  public void setOrderRecommendationReasons(String orReasons) {
    this.orReasons = orReasons;
  }

  public boolean getOrderRecommendationReasonsMandatory() {
    return orReasonsMandatory;
  }

  public void setOrderRecommendationReasonsMandatory(boolean orReasonsMandatory) {
    this.orReasonsMandatory = orReasonsMandatory;
  }

  public String getEditingQuantityReasons() {
    return eqReasons;
  }

  public void setEditingQuantityReasons(String eqReasons) {
    this.eqReasons = eqReasons;
  }

  public boolean getEditingQuantityReasonsMandatory() {
    return eqReasonsMandatory;
  }

  public void setEditingQuantityReasonsMandatory(boolean eqReasonsMandatory) {
    this.eqReasonsMandatory = eqReasonsMandatory;
  }

  public String getPartialShipmentReasons() {
    return psReasons;
  }

  public void setPartialShipmentReasons(String psReasons) {
    this.psReasons = psReasons;
  }

  public boolean getPartialShipmentReasonsMandatory() {
    return psReasonsMandatory;
  }

  public void setPartialShipmentReasonsMandatory(boolean psReasonsMandatory) {
    this.psReasonsMandatory = psReasonsMandatory;
  }

  public String getPartialFulfillmentReasons() {
    return pfReasons;
  }

  public void setPartialFulfillmentReasons(String pfReasons) {
    this.pfReasons = pfReasons;
  }

  public boolean getPartialFulfillmentReasonsMandatory() {
    return pfReasonsMandatory;
  }

  public void setPartialFulfillmentReasonsMandatory(boolean pfReasonsMandatory) {
    this.pfReasonsMandatory = pfReasonsMandatory;
  }

  public String getCancellingOrderReasons() {
    return coReasons;
  }

  public void setCancellingOrderReasons(String coReasons) {
    this.coReasons = coReasons;
  }

  public boolean getCancellingOrderReasonsMandatory() {
    return coReasonsMandatory;
  }

  public void setCancellingOrderReasonsMandatory(boolean coReasonsMandatory) {
    this.coReasonsMandatory = coReasonsMandatory;
  }

  public boolean isAllowCreatingShipments() {
    return allowCreatingShipments;
  }

  public void setAllowCreatingShipments(boolean allowCreatingShipments) {
    this.allowCreatingShipments = allowCreatingShipments;
  }
}

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

import com.logistimo.utils.StringUtil;

import org.apache.commons.lang.StringUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;
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
  private static final String ALLOW_CREATING_SHIPMENTS = "acs";
  private static final String AUTO_ASSIGN_FIRST_MATERIAL_STATUS_ON_CONFIRMATION = "aafmsc";
  private static final String CREATION_AUTOMATED = "auto_create";
  private static final String AUTO_CREATE_ON_MIN = "ac_on_min";
  private static final String AUTO_CREATE_PDOS = "ac_pdos";
  private static final String AUTO_CREATE_MTAGS = "ac_mtags";
  private static final String AUTO_CREATE_ETAGS = "ac_etags";
  private static final String INVOICE_TEMPLATE = "template";
  private static final String INVOICE_LOGO = "logo";
  private static final String SHIPMENT_TEMPLATE = "shipment_template";
  private static final String INVOICE_TEMPLATE_NAME = "invoice_template_name";
  private static final String SHIPMENT_TEMPLATE_NAME = "shipment_template_name";
  private static final String INVOICE_LOGO_NAME = "invoice_logo_name";

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
  private boolean autoAssignFirstMatStOnConfirmation;
  private boolean creationAutomated = false;
  private boolean autoCreateOnMin = false;
  private int autoCreatePdos = 0;
  private List<String> autoCreateMaterialTags = new ArrayList<>(1);
  private List<String> autoCreateEntityTags = new ArrayList<>(1);
  private String invoiceTemplate;
  private String invoiceLogo;
  private String shipmentTemplate;
  private String invoiceLogoName;
  private String invoiceTemplateName;
  private String shipmentTemplateName;


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
    try {
      autoAssignFirstMatStOnConfirmation = json.getBoolean(AUTO_ASSIGN_FIRST_MATERIAL_STATUS_ON_CONFIRMATION);
    } catch (JSONException e) {
      // ignore
    }
    try {
      creationAutomated = json.getBoolean(CREATION_AUTOMATED);
    } catch (JSONException e) {
      //ignore
    }
    try {
      autoCreateOnMin = json.getBoolean(AUTO_CREATE_ON_MIN);
    } catch (JSONException e) {
      //ignore
    }
    try {
      autoCreatePdos = json.getInt(AUTO_CREATE_PDOS);
    } catch (JSONException e) {
      //ignore
    }

    try {
      autoCreateMaterialTags = StringUtil.getList(json.getString(AUTO_CREATE_MTAGS));
    } catch (JSONException e) {
      autoCreateMaterialTags = new ArrayList<>(1);
    }

    try {
      autoCreateEntityTags = StringUtil.getList(json.getString(AUTO_CREATE_ETAGS));
    } catch (JSONException e) {
      autoCreateEntityTags = new ArrayList<>(1);
    }

    try {
      String template = json.getString(INVOICE_TEMPLATE);
      if (StringUtils.isNotEmpty(template)) {
        invoiceTemplate = template;
      }
    } catch (JSONException e) {
      //ignore
    }

    try {
      String template = json.getString(INVOICE_LOGO);
      if (StringUtils.isNotEmpty(template)) {
        invoiceLogo = template;
      }
    } catch (JSONException e) {
      //ignored
    }

    try {
      String template = json.getString(SHIPMENT_TEMPLATE);
      if (StringUtils.isNotEmpty(template)) {
        shipmentTemplate = template;
      }
    } catch (JSONException e) {
      //ignore
    }

    try {
      String invTempName = json.getString(INVOICE_TEMPLATE_NAME);
      if(StringUtils.isNotEmpty(invTempName)) {
        invoiceTemplateName = invTempName;
      }
    } catch (JSONException e) {
      //ignored
    }

    try {
      String shipTempName = json.getString(SHIPMENT_TEMPLATE_NAME);
      if(StringUtils.isNotEmpty(shipTempName)) {
        shipmentTemplateName = shipTempName;
      }
    } catch (JSONException e) {
      //ignored
    }

    try {
      String invLogoName = json.getString(INVOICE_LOGO_NAME);
      if(StringUtils.isNotEmpty(invLogoName)) {
        invoiceLogoName = invLogoName;
      }
    } catch (JSONException e) {
      //ignored
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
      json.put(AUTO_ASSIGN_FIRST_MATERIAL_STATUS_ON_CONFIRMATION, autoAssignFirstMatStOnConfirmation);
      json.put(CREATION_AUTOMATED, creationAutomated);
      json.put(AUTO_CREATE_ON_MIN, autoCreateOnMin);
      json.put(AUTO_CREATE_PDOS, autoCreatePdos);
      json.put(AUTO_CREATE_ETAGS, StringUtil.getCSV(autoCreateEntityTags));
      json.put(AUTO_CREATE_MTAGS, StringUtil.getCSV(autoCreateMaterialTags));
      json.put(INVOICE_TEMPLATE, invoiceTemplate);
      json.put(INVOICE_LOGO, invoiceLogo);
      json.put(SHIPMENT_TEMPLATE, shipmentTemplate);
      json.put(INVOICE_LOGO_NAME, invoiceLogoName);
      json.put(INVOICE_TEMPLATE_NAME, invoiceTemplateName);
      json.put(SHIPMENT_TEMPLATE_NAME, shipmentTemplateName);
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

  public boolean autoAssignFirstMatStOnConfirmation() {
    return autoAssignFirstMatStOnConfirmation;
  }

  public void setAutoAssignFirstMaterialStatusOnConfirmation(boolean assignMatStatus) {
    autoAssignFirstMatStOnConfirmation = assignMatStatus;
  }

  public boolean isCreationAutomated() {
    return creationAutomated;
  }

  public void setCreationAutomated(boolean creationAutomated) {
    this.creationAutomated = creationAutomated;
  }

  public boolean isAutoCreateOnMin() {
    return autoCreateOnMin;
  }

  public void setAutoCreateOnMin(boolean autoCreateOnMin) {
    this.autoCreateOnMin = autoCreateOnMin;
  }

  public int getAutoCreatePdos() {
    return autoCreatePdos;
  }

  public void setAutoCreatePdos(int autoCreatePdos) {
    this.autoCreatePdos = autoCreatePdos;
  }

  public List<String> getAutoCreateMaterialTags() {
    return autoCreateMaterialTags;
  }

  public void setAutoCreateMaterialTags(List<String> autoCreateMaterialTags) {
    this.autoCreateMaterialTags = autoCreateMaterialTags;
  }

  public List<String> getAutoCreateEntityTags() {
    return autoCreateEntityTags;
  }

  public void setAutoCreateEntityTags(List<String> autoCreateEntityTags) {
    this.autoCreateEntityTags = autoCreateEntityTags;
  }

  public String getInvoiceTemplate() {
    return invoiceTemplate;
  }

  public OrdersConfig setInvoiceTemplate(String invoiceTemplate) {
    this.invoiceTemplate = invoiceTemplate;
    return this;
  }

  public String getInvoiceLogo() {
    return invoiceLogo;
  }

  public OrdersConfig setInvoiceLogo(String invoiceLogo) {
    this.invoiceLogo = invoiceLogo;
    return this;
  }

  public OrdersConfig setShipmentTemplate(String shipmentTemplate) {
    this.shipmentTemplate = shipmentTemplate;
    return this;
  }

  public String getShipmentTemplate() {
    return shipmentTemplate;
  }

  public String getInvoiceLogoName() {
    return invoiceLogoName;
  }

  public void setInvoiceLogoName(String invoiceLogoName) {
    this.invoiceLogoName = invoiceLogoName;
  }

  public String getInvoiceTemplateName() {
    return invoiceTemplateName;
  }

  public void setInvoiceTemplateName(String invoiceTemplateName) {
    this.invoiceTemplateName = invoiceTemplateName;
  }

  public String getShipmentTemplateName() {
    return shipmentTemplateName;
  }

  public void setShipmentTemplateName(String shipmentTemplateName) {
    this.shipmentTemplateName = shipmentTemplateName;
  }
}

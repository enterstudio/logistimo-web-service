/**
 *
 */
package com.logistimo.proto;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.logistimo.proto.utils.StringTokenizer;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

/**
 * @author arun
 *
 *         Represents the output form for the Authenticate REST API
 *         Success and failure output formats are documented at: http://samaanguru.pbworks.com/w/page/Revised-REST-API-Documentation
 */
public class AuthenticateOutput extends OutputMessageBean implements JsonBean {

  private Hashtable user = null;
  private Vector kiosks = null;
  private String expiryTime = null;
  private Hashtable config = null;
  private Tags tags = null;

  public AuthenticateOutput(String locale) throws ProtocolException {
    super(locale);
    this.tags = new Tags(protoMessages);
  }

  public AuthenticateOutput(boolean status, String expiryTime, Hashtable user, Vector kiosks,
                            Hashtable config, String errMsg, String locale, String version)
      throws ProtocolException {
    super(status, errMsg, locale, version);
    this.user = user;
    this.kiosks = kiosks;
    this.expiryTime = expiryTime;
    this.config = config;
    this.tags = new Tags(protoMessages);
  }

  // Add tags to be hidden for inventory operation to JSON
  public static JSONObject addTagsInventoryOperation(Hashtable tagsInventoryOperation)
      throws JSONException {
    JSONObject json = new JSONObject();
    String value = null;
    if ((value = (String) tagsInventoryOperation.get(JsonTagsZ.ISSUES)) != null && !value
        .equals("")) {
      json.put(JsonTagsZ.ISSUES, value);
    }
    if ((value = (String) tagsInventoryOperation.get(JsonTagsZ.RECEIPTS)) != null && !value
        .equals("")) {
      json.put(JsonTagsZ.RECEIPTS, value);
    }
    if ((value = (String) tagsInventoryOperation.get(JsonTagsZ.PHYSICAL_STOCK)) != null && !value
        .equals("")) {
      json.put(JsonTagsZ.PHYSICAL_STOCK, value);
    }
    if ((value = (String) tagsInventoryOperation.get(JsonTagsZ.DISCARDS)) != null && !value
        .equals("")) {
      json.put(JsonTagsZ.DISCARDS, value);
    }
    if ((value = (String) tagsInventoryOperation.get(JsonTagsZ.TRANSFER)) != null && !value
        .equals("")) {
      json.put(JsonTagsZ.TRANSFER, value);
    }
    // Add to cogntainer JSON
    return json;
  }

  // Get tags to be hidden for inventory operation
  public static Hashtable loadTagsInventoryOperation(JSONObject json) throws JSONException {
    Hashtable tagsInventoryOperation = new Hashtable();
    try {
      tagsInventoryOperation.put(JsonTagsZ.ISSUES, json.getString(JsonTagsZ.ISSUES));
    } catch (Exception e) {
      // ignorePHYSICAL_STOCK
    }
    try {
      tagsInventoryOperation.put(JsonTagsZ.RECEIPTS, json.getString(JsonTagsZ.RECEIPTS));
    } catch (Exception e) {
      // ignore
    }
    try {
      tagsInventoryOperation
          .put(JsonTagsZ.PHYSICAL_STOCK, json.getString(JsonTagsZ.PHYSICAL_STOCK));
    } catch (Exception e) {
      // ignore
    }
    try {
      tagsInventoryOperation.put(JsonTagsZ.DISCARDS, json.getString(JsonTagsZ.DISCARDS));
    } catch (Exception e) {
      // ignore
    }
    try {
      tagsInventoryOperation.put(JsonTagsZ.TRANSFER, json.getString(JsonTagsZ.TRANSFER));
    } catch (Exception e) {
      // ignore
    }

    return tagsInventoryOperation;
  }

  // Add material status for each inventory operation
  public static JSONObject addMaterialStatus(Hashtable materialStatus) throws JSONException {
    JSONObject json = new JSONObject();
    String value = null;
    Hashtable hvalue = null;
    if ((hvalue = (Hashtable) materialStatus.get(JsonTagsZ.ISSUES)) != null && !hvalue.isEmpty()) {
      JSONObject jsonIssues = new JSONObject();
      Hashtable issues = new Hashtable();
      issues = (Hashtable) materialStatus.get(JsonTagsZ.ISSUES);
      if ((value = (String) issues.get(JsonTagsZ.ALL)) != null && !value.equals("")) {
        jsonIssues.put(JsonTagsZ.ALL, value);
      }
      if ((value = (String) issues.get(JsonTagsZ.TEMP_SENSITVE_MATERIALS)) != null && !value
          .equals("")) {
        jsonIssues.put(JsonTagsZ.TEMP_SENSITVE_MATERIALS, value);
      }
      json.put(JsonTagsZ.ISSUES, jsonIssues);
    }
    if ((hvalue = (Hashtable) materialStatus.get(JsonTagsZ.RECEIPTS)) != null && !hvalue
        .isEmpty()) {
      JSONObject jsonReceipts = new JSONObject();
      Hashtable receipts = new Hashtable();
      receipts = (Hashtable) materialStatus.get(JsonTagsZ.RECEIPTS);
      if ((value = (String) receipts.get(JsonTagsZ.ALL)) != null && !value.equals("")) {
        jsonReceipts.put(JsonTagsZ.ALL, value);
      }
      if ((value = (String) receipts.get(JsonTagsZ.TEMP_SENSITVE_MATERIALS)) != null && !value
          .equals("")) {
        jsonReceipts.put(JsonTagsZ.TEMP_SENSITVE_MATERIALS, value);
      }
      json.put(JsonTagsZ.RECEIPTS, jsonReceipts);
    }
    if ((hvalue = (Hashtable) materialStatus.get(JsonTagsZ.PHYSICAL_STOCK)) != null && !hvalue
        .isEmpty()) {
      JSONObject jsonPhysicalStock = new JSONObject();
      Hashtable physicalstock = new Hashtable();
      physicalstock = (Hashtable) materialStatus.get(JsonTagsZ.PHYSICAL_STOCK);
      if ((value = (String) physicalstock.get(JsonTagsZ.ALL)) != null && !value.equals("")) {
        jsonPhysicalStock.put(JsonTagsZ.ALL, value);
      }
      if ((value = (String) physicalstock.get(JsonTagsZ.TEMP_SENSITVE_MATERIALS)) != null && !value
          .equals("")) {
        jsonPhysicalStock.put(JsonTagsZ.TEMP_SENSITVE_MATERIALS, value);
      }
      json.put(JsonTagsZ.PHYSICAL_STOCK, jsonPhysicalStock);
    }
    if ((hvalue = (Hashtable) materialStatus.get(JsonTagsZ.DISCARDS)) != null && !hvalue
        .isEmpty()) {
      JSONObject jsonDiscards = new JSONObject();
      Hashtable discards = new Hashtable();
      discards = (Hashtable) materialStatus.get(JsonTagsZ.DISCARDS);
      if ((value = (String) discards.get(JsonTagsZ.ALL)) != null && !value.equals("")) {
        jsonDiscards.put(JsonTagsZ.ALL, value);
      }
      if ((value = (String) discards.get(JsonTagsZ.TEMP_SENSITVE_MATERIALS)) != null && !value
          .equals("")) {
        jsonDiscards.put(JsonTagsZ.TEMP_SENSITVE_MATERIALS, value);
      }
      json.put(JsonTagsZ.DISCARDS, jsonDiscards);
    }
    if ((hvalue = (Hashtable) materialStatus.get(JsonTagsZ.TRANSFER)) != null && !hvalue
        .isEmpty()) {
      JSONObject jsonTransfer = new JSONObject();
      Hashtable transfer = new Hashtable();
      transfer = (Hashtable) materialStatus.get(JsonTagsZ.TRANSFER);
      if ((value = (String) transfer.get(JsonTagsZ.ALL)) != null && !value.equals("")) {
        jsonTransfer.put(JsonTagsZ.ALL, value);
      }
      if ((value = (String) transfer.get(JsonTagsZ.TEMP_SENSITVE_MATERIALS)) != null && !value
          .equals("")) {
        jsonTransfer.put(JsonTagsZ.TEMP_SENSITVE_MATERIALS, value);
      }
      json.put(JsonTagsZ.TRANSFER, jsonTransfer);
    }
    // Add to cogntainer JSON
    return json;
  }

  // Get material status
  public static Hashtable loadMaterialStatus(JSONObject json) throws JSONException {
    Hashtable materialStatus = new Hashtable();
    String value = null;
    try {
      Hashtable issues = new Hashtable();
      JSONObject issuesJson = (JSONObject) json.get(JsonTagsZ.ISSUES);
      if ((value = (String) issuesJson.getString(JsonTagsZ.ALL)) != null && !value.equals("")) {
        issues.put(JsonTagsZ.ALL, issuesJson.getString(JsonTagsZ.ALL));
      }
      if ((value = (String) issuesJson.getString(JsonTagsZ.TEMP_SENSITVE_MATERIALS)) != null
          && !value.equals("")) {
        issues.put(JsonTagsZ.TEMP_SENSITVE_MATERIALS,
            issuesJson.getString(JsonTagsZ.TEMP_SENSITVE_MATERIALS));
      }
      materialStatus.put(JsonTagsZ.ISSUES, issues);
    } catch (Exception e) {
      // ignore
    }
    try {
      Hashtable receipts = new Hashtable();
      JSONObject receiptsJson = (JSONObject) json.get(JsonTagsZ.RECEIPTS);
      if ((value = (String) receiptsJson.getString(JsonTagsZ.ALL)) != null && !value.equals("")) {
        receipts.put(JsonTagsZ.ALL, receiptsJson.getString(JsonTagsZ.ALL));
      }
      if ((value = (String) receiptsJson.getString(JsonTagsZ.TEMP_SENSITVE_MATERIALS)) != null
          && !value.equals("")) {
        receipts.put(JsonTagsZ.TEMP_SENSITVE_MATERIALS,
            receiptsJson.getString(JsonTagsZ.TEMP_SENSITVE_MATERIALS));
      }
      materialStatus.put(JsonTagsZ.RECEIPTS, receipts);
    } catch (Exception e) {
      // ignore
    }
    try {
      Hashtable physicalstock = new Hashtable();
      JSONObject physicalstockJson = (JSONObject) json.get(JsonTagsZ.RECEIPTS);
      if ((value = (String) physicalstockJson.getString(JsonTagsZ.ALL)) != null && !value
          .equals("")) {
        physicalstock.put(JsonTagsZ.ALL, physicalstockJson.getString(JsonTagsZ.ALL));
      }
      if ((value = (String) physicalstockJson.getString(JsonTagsZ.TEMP_SENSITVE_MATERIALS)) != null
          && !value.equals("")) {
        physicalstock.put(JsonTagsZ.TEMP_SENSITVE_MATERIALS,
            physicalstockJson.getString(JsonTagsZ.TEMP_SENSITVE_MATERIALS));
      }
      materialStatus.put(JsonTagsZ.PHYSICAL_STOCK, physicalstock);
    } catch (Exception e) {
      // ignore
    }
    try {
      Hashtable discards = new Hashtable();
      JSONObject discardsJson = (JSONObject) json.get(JsonTagsZ.RECEIPTS);
      if ((value = (String) discardsJson.getString(JsonTagsZ.ALL)) != null && !value.equals("")) {
        discards.put(JsonTagsZ.ALL, discardsJson.getString(JsonTagsZ.ALL));
      }
      if ((value = (String) discardsJson.getString(JsonTagsZ.TEMP_SENSITVE_MATERIALS)) != null
          && !value.equals("")) {
        discards.put(JsonTagsZ.TEMP_SENSITVE_MATERIALS,
            discardsJson.getString(JsonTagsZ.TEMP_SENSITVE_MATERIALS));
      }
      materialStatus.put(JsonTagsZ.DISCARDS, discards);
    } catch (Exception e) {
      // ignore
    }
    try {
      Hashtable transfer = new Hashtable();
      JSONObject transferJson = (JSONObject) json.get(JsonTagsZ.RECEIPTS);
      if ((value = (String) transferJson.getString(JsonTagsZ.ALL)) != null && !value.equals("")) {
        transfer.put(JsonTagsZ.ALL, transferJson.getString(JsonTagsZ.ALL));
      }
      if ((value = (String) transferJson.getString(JsonTagsZ.TEMP_SENSITVE_MATERIALS)) != null
          && !value.equals("")) {
        transfer.put(JsonTagsZ.TEMP_SENSITVE_MATERIALS,
            transferJson.getString(JsonTagsZ.TEMP_SENSITVE_MATERIALS));
      }
      materialStatus.put(JsonTagsZ.TRANSFER, transfer);
    } catch (Exception e) {
      // ignore
    }

    return materialStatus;
  }

  // Add material status for each inventory operation
  public static JSONObject addReasonsByTag(Hashtable reasonsByTag) throws JSONException {
    JSONObject json = new JSONObject();
    Hashtable value = null;
    if ((value = (Hashtable) reasonsByTag.get(JsonTagsZ.ISSUES)) != null && !value.isEmpty()) {
      try {
        json.put(JsonTagsZ.ISSUES, new JSONObject(value));
      } catch (JSONException e) {
        // ignore
      }
    }
    if ((value = (Hashtable) reasonsByTag.get(JsonTagsZ.RECEIPTS)) != null && !value.isEmpty()) {
      try {
        json.put(JsonTagsZ.RECEIPTS, new JSONObject(value));
      } catch (JSONException e) {
        // ignore
      }
    }
    if ((value = (Hashtable) reasonsByTag.get(JsonTagsZ.PHYSICAL_STOCK)) != null && !value
        .isEmpty()) {
      try {
        json.put(JsonTagsZ.PHYSICAL_STOCK, new JSONObject(value));
      } catch (JSONException e) {
        // ignore
      }
    }
    if ((value = (Hashtable) reasonsByTag.get(JsonTagsZ.DISCARDS)) != null && !value.isEmpty()) {
      try {
        json.put(JsonTagsZ.DISCARDS, new JSONObject(value));
      } catch (JSONException e) {
        // ignore
      }
    }
    if ((value = (Hashtable) reasonsByTag.get(JsonTagsZ.TRANSFER)) != null && !value.isEmpty()) {
      try {
        json.put(JsonTagsZ.TRANSFER, new JSONObject(value));
      } catch (JSONException e) {
        // ignore
      }
    }
    // Add to cogntainer JSON
    return json;
  }

  public static Hashtable loadReasonsByTag(JSONObject json) throws JSONException {
    Hashtable reasonsByTagHt = new Hashtable();
    // Enumeration en = json.keys();
    Iterator en = json.keys();
    //  while ( en.hasMoreElements() ) {
    while (en.hasNext()) {
      //  String transType = (String) en.nextElement();
      String transType = (String) en.next();
      JSONObject tagsByTransJson = json.getJSONObject(transType);
      if (tagsByTransJson == null) {
        continue;
      }
      Iterator enTags = tagsByTransJson.keys();
      // Enumeration enTags = tagsByTransJson.keys();
      Hashtable tagsHt = new Hashtable();
      // while ( enTags.hasMoreElements() ) {
      //   String tag = (String) enTags.nextElement();
      while (enTags.hasNext()) {
        String tag = (String) enTags.next();
        String value = null;
        if ((value = (String) tagsByTransJson.get(tag)) != null && !value.equals("")) {
          tagsHt.put(tag, value);
        }
      }
      reasonsByTagHt.put(transType, tagsHt);
    }
    return reasonsByTagHt;
  }

  // Get intervals
  public static Hashtable loadIntervals(JSONObject json) throws JSONException {
    Hashtable intervalsData = new Hashtable();
    try {
      intervalsData.put(JsonTagsZ.INTERVAL_REFRESHING_CONFIG_HOURS,
          json.getString(JsonTagsZ.INTERVAL_REFRESHING_CONFIG_HOURS));
    } catch (Exception e) {
      // ignore
    }
    try {
      intervalsData.put(JsonTagsZ.INTERVAL_REFRESHING_MASTER_DATA_HOURS,
          json.getString(JsonTagsZ.INTERVAL_REFRESHING_MASTER_DATA_HOURS));
    } catch (Exception e) {
      // ignore
    }
    try {
      intervalsData.put(JsonTagsZ.INTERVAL_REFRESHING_INVENTORY_HOURS,
          json.getString(JsonTagsZ.INTERVAL_REFRESHING_INVENTORY_HOURS));
    } catch (Exception e) {
      // ignore
    }
    try {
      intervalsData.put(JsonTagsZ.INTERVAL_SENDING_STATS_HOURS,
          json.getString(JsonTagsZ.INTERVAL_SENDING_STATS_HOURS));
    } catch (Exception e) {
      // ignore
    }
    try {
      intervalsData.put(JsonTagsZ.INTERVAL_SENDING_SUPPORT_LOG_HOURS,
          json.getString(JsonTagsZ.INTERVAL_SENDING_SUPPORT_LOG_HOURS));
    } catch (Exception e) {
      // ignore
    }
    try {
      intervalsData.put(JsonTagsZ.INTERVAL_SENDING_APP_LOG_HOURS,
          json.getString(JsonTagsZ.INTERVAL_SENDING_APP_LOG_HOURS));
    } catch (Exception e) {
      // ignore
    }
    try {
      intervalsData.put(JsonTagsZ.INTERVAL_WAIT_BEFORE_SENDING_SMS_HOURS,
          json.getString(JsonTagsZ.INTERVAL_WAIT_BEFORE_SENDING_SMS_HOURS));
    } catch (Exception e) {
      // ignore
    }
    return intervalsData;
  }

  // Add tags to hide for inventory operation to JSON
  public static JSONObject addIntervals(Hashtable intervalsData) throws JSONException {
    JSONObject json = new JSONObject();
    String value = null;
    if ((value = (String) intervalsData.get(JsonTagsZ.INTERVAL_REFRESHING_CONFIG_HOURS)) != null
        && !value.equals("")) {
      json.put(JsonTagsZ.INTERVAL_REFRESHING_CONFIG_HOURS, value);
    }
    if ((value = (String) intervalsData.get(JsonTagsZ.INTERVAL_REFRESHING_MASTER_DATA_HOURS))
        != null && !value.equals("")) {
      json.put(JsonTagsZ.INTERVAL_REFRESHING_MASTER_DATA_HOURS, value);
    }
    if ((value = (String) intervalsData.get(JsonTagsZ.INTERVAL_REFRESHING_INVENTORY_HOURS)) != null
        && !value.equals("")) {
      json.put(JsonTagsZ.INTERVAL_REFRESHING_INVENTORY_HOURS, value);
    }
    if ((value = (String) intervalsData.get(JsonTagsZ.INTERVAL_SENDING_STATS_HOURS)) != null
        && !value.equals("")) {
      json.put(JsonTagsZ.INTERVAL_SENDING_STATS_HOURS, value);
    }
    if ((value = (String) intervalsData.get(JsonTagsZ.INTERVAL_SENDING_SUPPORT_LOG_HOURS)) != null
        && !value.equals("")) {
      json.put(JsonTagsZ.INTERVAL_SENDING_SUPPORT_LOG_HOURS, value);
    }
    if ((value = (String) intervalsData.get(JsonTagsZ.INTERVAL_SENDING_APP_LOG_HOURS)) != null
        && !value.equals("")) {
      json.put(JsonTagsZ.INTERVAL_SENDING_APP_LOG_HOURS, value);
    }
    if ((value = (String) intervalsData.get(JsonTagsZ.INTERVAL_WAIT_BEFORE_SENDING_SMS_HOURS))
        != null && !value.equals("")) {
      json.put(JsonTagsZ.INTERVAL_WAIT_BEFORE_SENDING_SMS_HOURS, value);
    }
    // Add to cogntainer JSON
    return json;
  }

  // Add capture actual transaction date options  for each inventory operation
  public static JSONObject addCaptureActualTransactionDate(Hashtable captureActualTransactionDate)
      throws JSONException {
    JSONObject json = new JSONObject();
    String value = null;
    Hashtable hvalue = null;
    if ((hvalue = (Hashtable) captureActualTransactionDate.get(JsonTagsZ.ISSUES)) != null && !hvalue
        .isEmpty()) {
      JSONObject jsonIssues = new JSONObject();
      Hashtable issues = new Hashtable();
      issues = (Hashtable) captureActualTransactionDate.get(JsonTagsZ.ISSUES);
      if ((value = (String) issues.get(JsonTagsZ.TYPE)) != null && !value.equals("")) {
        jsonIssues.put(JsonTagsZ.TYPE, value);
      }
      json.put(JsonTagsZ.ISSUES, jsonIssues);
    }
    if ((hvalue = (Hashtable) captureActualTransactionDate.get(JsonTagsZ.RECEIPTS)) != null
        && !hvalue.isEmpty()) {
      JSONObject jsonReceipts = new JSONObject();
      Hashtable receipts = new Hashtable();
      receipts = (Hashtable) captureActualTransactionDate.get(JsonTagsZ.RECEIPTS);
      if ((value = (String) receipts.get(JsonTagsZ.TYPE)) != null && !value.equals("")) {
        jsonReceipts.put(JsonTagsZ.TYPE, value);
      }
      json.put(JsonTagsZ.RECEIPTS, jsonReceipts);
    }
    if ((hvalue = (Hashtable) captureActualTransactionDate.get(JsonTagsZ.PHYSICAL_STOCK)) != null
        && !hvalue.isEmpty()) {
      JSONObject jsonPhysicalStock = new JSONObject();
      Hashtable physicalstock = new Hashtable();
      physicalstock = (Hashtable) captureActualTransactionDate.get(JsonTagsZ.PHYSICAL_STOCK);
      if ((value = (String) physicalstock.get(JsonTagsZ.TYPE)) != null && !value.equals("")) {
        jsonPhysicalStock.put(JsonTagsZ.TYPE, value);
      }
      json.put(JsonTagsZ.PHYSICAL_STOCK, jsonPhysicalStock);
    }
    if ((hvalue = (Hashtable) captureActualTransactionDate.get(JsonTagsZ.DISCARDS)) != null
        && !hvalue.isEmpty()) {
      JSONObject jsonDiscards = new JSONObject();
      Hashtable discards = new Hashtable();
      discards = (Hashtable) captureActualTransactionDate.get(JsonTagsZ.DISCARDS);
      if ((value = (String) discards.get(JsonTagsZ.TYPE)) != null && !value.equals("")) {
        jsonDiscards.put(JsonTagsZ.TYPE, value);
      }
      json.put(JsonTagsZ.DISCARDS, jsonDiscards);
    }
    if ((hvalue = (Hashtable) captureActualTransactionDate.get(JsonTagsZ.TRANSFER)) != null
        && !hvalue.isEmpty()) {
      JSONObject jsonTransfer = new JSONObject();
      Hashtable transfer = new Hashtable();
      transfer = (Hashtable) captureActualTransactionDate.get(JsonTagsZ.TRANSFER);
      if ((value = (String) transfer.get(JsonTagsZ.TYPE)) != null && !value.equals("")) {
        jsonTransfer.put(JsonTagsZ.TYPE, value);
      }
      json.put(JsonTagsZ.TRANSFER, jsonTransfer);
    }
    // Add to container JSON
    return json;
  }

  // Get capture actual transaction date config
  public static Hashtable loadCaptureActualTransactionDate(JSONObject json) throws JSONException {
    Hashtable captureActualTransactionDate = new Hashtable();
    String value = null;
    try {
      Hashtable issues = new Hashtable();
      JSONObject issuesJson = (JSONObject) json.get(JsonTagsZ.ISSUES);
      if ((value = (String) issuesJson.getString(JsonTagsZ.TYPE)) != null && !value.equals("")) {
        issues.put(JsonTagsZ.TYPE, issuesJson.getString(JsonTagsZ.TYPE));
      }
      captureActualTransactionDate.put(JsonTagsZ.ISSUES, issues);
    } catch (Exception e) {
      // ignore
    }
    try {
      Hashtable receipts = new Hashtable();
      JSONObject receiptsJson = (JSONObject) json.get(JsonTagsZ.RECEIPTS);
      if ((value = (String) receiptsJson.getString(JsonTagsZ.TYPE)) != null && !value.equals("")) {
        receipts.put(JsonTagsZ.TYPE, receiptsJson.getString(JsonTagsZ.TYPE));
      }
      captureActualTransactionDate.put(JsonTagsZ.RECEIPTS, receipts);
    } catch (Exception e) {
      // ignore
    }
    try {
      Hashtable physicalstock = new Hashtable();
      JSONObject physicalstockJson = (JSONObject) json.get(JsonTagsZ.PHYSICAL_STOCK);
      if ((value = (String) physicalstockJson.getString(JsonTagsZ.TYPE)) != null && !value
          .equals("")) {
        physicalstock.put(JsonTagsZ.TYPE, physicalstockJson.getString(JsonTagsZ.TYPE));
      }
      captureActualTransactionDate.put(JsonTagsZ.PHYSICAL_STOCK, physicalstock);
    } catch (Exception e) {
      // ignore
    }
    try {
      Hashtable discards = new Hashtable();
      JSONObject discardsJson = (JSONObject) json.get(JsonTagsZ.DISCARDS);
      if ((value = (String) discardsJson.getString(JsonTagsZ.TYPE)) != null && !value.equals("")) {
        discards.put(JsonTagsZ.TYPE, discardsJson.getString(JsonTagsZ.TYPE));
      }
      captureActualTransactionDate.put(JsonTagsZ.DISCARDS, discards);
    } catch (Exception e) {
      // ignore
    }
    try {
      Hashtable transfer = new Hashtable();
      JSONObject transferJson = (JSONObject) json.get(JsonTagsZ.TRANSFER);
      if ((value = (String) transferJson.getString(JsonTagsZ.TYPE)) != null && !value.equals("")) {
        transfer.put(JsonTagsZ.TYPE, transferJson.getString(JsonTagsZ.TYPE));
      }
      captureActualTransactionDate.put(JsonTagsZ.TRANSFER, transfer);
    } catch (Exception e) {
      // ignore
    }

    return captureActualTransactionDate;
  }

  // Order recommendation reasons
  public static JSONObject addIgnoreOrderRecommendationReasons(
      Hashtable ignoreOrderRecommendationReasons) throws JSONException {
    JSONObject json = new JSONObject();
    String value = null;
    if ((value = (String) ignoreOrderRecommendationReasons.get(JsonTagsZ.REASONS)) != null && !value
        .equals("")) {
      json.put(JsonTagsZ.REASONS, value);
    }
    if ((value = (String) ignoreOrderRecommendationReasons.get(JsonTagsZ.MANDATORY)) != null
        && !value.equals("")) {
      json.put(JsonTagsZ.MANDATORY, value);
    }
    // Add to container JSON
    return json;
  }

  // Get ignore order recommendation reasons
  public static Hashtable loadIgnoreOrderRecommendationReasons(JSONObject json)
      throws JSONException {
    Hashtable ignoreOrderRecommendationReasons = new Hashtable();
    try {
      ignoreOrderRecommendationReasons.put(JsonTagsZ.REASONS, json.getString(JsonTagsZ.REASONS));
    } catch (Exception e) {
      // ignore
    }
    try {
      ignoreOrderRecommendationReasons
          .put(JsonTagsZ.MANDATORY, json.getString(JsonTagsZ.MANDATORY));
    } catch (Exception e) {
      // ignore
    }

    return ignoreOrderRecommendationReasons;
  }

  // Get sms parameters
  public static Hashtable loadSMSParameters(JSONObject json) throws JSONException {
    Hashtable smsData = new Hashtable();
    try {
      smsData.put(JsonTagsZ.GATEWAY_PHONE_NUMBER, json.getString(JsonTagsZ.GATEWAY_PHONE_NUMBER));
    } catch (Exception e) {
      // ignore
    }
    try {
      smsData.put(JsonTagsZ.GATEWAY_ROUTING_KEYWORD,
          json.getString(JsonTagsZ.GATEWAY_ROUTING_KEYWORD));
    } catch (Exception e) {
      // ignore
    }
    try {
      smsData.put(JsonTagsZ.SENDER_ID, json.getString(JsonTagsZ.SENDER_ID));
    } catch (Exception e) {
      // ignore
    }
    return smsData;
  }

  // Add sms parameters
  public static JSONObject addSMSParameters(Hashtable smsData) throws JSONException {
    JSONObject json = new JSONObject();
    String value = null;
    if ((value = (String) smsData.get(JsonTagsZ.GATEWAY_PHONE_NUMBER)) != null && !value
        .equals("")) {
      json.put(JsonTagsZ.GATEWAY_PHONE_NUMBER, value);
    }
    if ((value = (String) smsData.get(JsonTagsZ.GATEWAY_ROUTING_KEYWORD)) != null && !value
        .equals("")) {
      json.put(JsonTagsZ.GATEWAY_ROUTING_KEYWORD, value);
    }
    if ((value = (String) smsData.get(JsonTagsZ.SENDER_ID)) != null && !value.equals("")) {
      json.put(JsonTagsZ.SENDER_ID, value);
    }
    // Add to container JSON
    return json;
  }

  // Get app upgrade parameters
  public static Hashtable loadAppUgradeParameters(JSONObject json) throws JSONException {
    Hashtable appUpgradeData = new Hashtable();
    try {
      appUpgradeData.put(JsonTagsZ.VERSION, json.getString(JsonTagsZ.VERSION));
    } catch (Exception e) {
      // ignore
    }
    try {
      appUpgradeData.put(JsonTagsZ.TIMESTAMP, json.getString(JsonTagsZ.TIMESTAMP));
    } catch (Exception e) {
      // ignore
    }

    return appUpgradeData;
  }

  // Add sms parameters
  public static JSONObject addAppUpgradeParameters(Hashtable smsData) throws JSONException {
    JSONObject json = new JSONObject();
    String value = null;
    if ((value = (String) smsData.get(JsonTagsZ.VERSION)) != null && !value.equals("")) {
      json.put(JsonTagsZ.VERSION, value);
    }
    if ((value = (String) smsData.get(JsonTagsZ.TIMESTAMP)) != null && !value.equals("")) {
      json.put(JsonTagsZ.TIMESTAMP, value);
    }
    // Add to container JSON
    return json;
  }

  public Hashtable getUser() {
    return this.user;
  }

  public Vector getKiosks() {
    return this.kiosks;
  } // a list of Hashtable

  public Hashtable getConfig() {
    return this.config;
  }

  public String getExpiryTime() {
    return this.expiryTime;
  }

  public boolean isDemandOnly() {
    if (this.config != null) {
      String ogn = (String) this.config.get(JsonTagsZ.ORDER_GENERATION);
      if (ogn != null && ogn.equals("1")) {
        return true;
      }
    }
    return false;
  }

  public List<String> getHiddenTagsInventory() { // returns a vector of String tags to be hidden in Inventory view
    return JsonUtil.tokenize((String) config.get(JsonTagsZ.TAGS_INVENTORY));
  }

  public List<String> getHiddenTagsOrders() { // returns a vector of String tags to be hidden in Orders view
    return JsonUtil.tokenize((String) config.get(JsonTagsZ.TAGS_ORDERS));
  }

  public boolean hasTags() {
    return tags != null && tags.hasTags();
  }

  public Tags getTags() {
    return tags;
  }

  /**
   * Initialize the object from a JSON string
   *
   * @param jsonString a JSON formatted input string
   * @throws JSONException if there are errors parsing the JSON input
   */
  public void fromJSONString(String jsonString) throws ProtocolException {
    this.tags = new Tags(protoMessages);
    try {
      JSONObject json = new JSONObject(jsonString);
      try {
        this.version = (String) json.get(JsonTagsZ.VERSION);
      } catch (JSONException e) {
        // version not present; do nothing
      }
      // Load data from JSON, as per version
      loadFromJSON(json);
    } catch (JSONException e) {
      throw new ProtocolException(e.getMessage());
    }
  }

  // Convert to JSON string
  public String toJSONString() throws ProtocolException {
    String jsonString = null;
    try {
      jsonString = toJSONObject().toString();
      // jsonString = gson.toJson(toJSONObject());
    } catch (JSONException e) {
      throw new ProtocolException(e.getMessage());
    }

    return jsonString;
  }

  // Convert to JSON string
  public String toJson() throws ProtocolException {
    String jsonString = null;
    try {
      jsonString = toJSONObject().toString();
      // jsonString = gson.toJson(toJSONObject());
    } catch (JSONException e) {
      throw new ProtocolException(e.getMessage());
    }

    return jsonString;
  }

  public void fromJSON(String jsonString) throws ProtocolException {
    this.tags = new Tags(protoMessages);
    try {
      JSONObject json = new JSONObject(jsonString);
      try {
        this.version = (String) json.get(JsonTagsZ.VERSION);
      } catch (JSONException e) {
        // version not present; do nothing
      }
      // Load data from JSON, as per version
      loadFromJSON(json);
    } catch (JSONException e) {
      throw new ProtocolException(e.getMessage());
    }
  }

  // Load from plain message (as from an SMS)
  // NOTE: No address fields (state, city, pin code, street address), email and landline via SMS (ONLY VIA GPRS)
  public Vector toMessageString() throws ProtocolException {
    String msg = version;
    msg += " " + statusCode;
    if (!status) {
      msg += " " + MessageUtil.encode(errMsg);
    } else {
      // Get user data
      String val = null;
      if (user != null && !user.isEmpty()) {
        if ((val = (String) user.get(JsonTagsZ.FIRST_NAME)) != null && !val.equals("")) {
          msg += " " + MessageUtil.encode(val);
        } else {
          msg += " " + MessageUtil.DUMMY;
        }
        if (Integer.parseInt(version) >= Integer
            .parseInt(MessageHeader.VERSION05)) { // version 05 or greater
          // Add role
          if ((val = (String) user.get(JsonTagsZ.ROLE)) != null && !val.equals("")) {
            msg += " " + val;
          } else {
            msg += " " + MessageUtil.DUMMY;
          }
        }
      }
      // Get the kiosk data
      int numKiosks = 0;
      if (kiosks == null || kiosks.isEmpty()) {
        if (Integer.parseInt(version) >= Integer
            .parseInt(MessageHeader.VERSION02)) // MessageHeader.VERSION02.equals( version ) )
        {
          msg += " 0"; // 0 kiosks
        } else // version 01
        {
          throw new ProtocolException(protoMessages.getString("error.nokiosks"));
        }
      } else {
        if (MessageHeader.VERSION.equals(version)) { // version 01
          // Get the first kiosk
          // kiosk Id
          Hashtable ht = (Hashtable) kiosks.firstElement();
          if ((val = (String) ht.get(JsonTagsZ.KIOSK_ID)) != null && !val.equals("")) {
            msg += " " + val;
          }
          // Currency
          if ((val = (String) ht.get(JsonTagsZ.CURRENCY)) != null && !val.equals("")) {
            msg += " " + val;
          } else {
            msg += " " + MessageUtil.DUMMY;
          }
          // Tax
          if ((val = (String) ht.get(JsonTagsZ.TAX)) != null && !val.equals("")) {
            msg += " " + val;
          } else {
            msg += " " + MessageUtil.DUMMY;
          }
          // Add materials
          msg +=
              " " + MessageUtil
                  .getMaterialsString((Vector) ht.get(JsonTagsZ.MATERIALS), MessageUtil.ALL_FIELDS,
                      false);
        } else { // version 02 onwards - multi-kiosk handling
          // Add details of all the kiosks (in version 01 only first kiosk info. was being sent)
          numKiosks = kiosks.size();
          msg += " " + numKiosks;
          Enumeration en = kiosks.elements();
          while (en.hasMoreElements()) {
            // kiosk Id
            Hashtable ht = (Hashtable) en.nextElement();
            if ((val = (String) ht.get(JsonTagsZ.KIOSK_ID)) != null && !val.equals("")) {
              msg += " " + val;
            }
            // Name
            if ((val = (String) ht.get(JsonTagsZ.NAME)) != null && !val.equals("")) {
              msg += " " + MessageUtil.encode(val);
            } else {
              msg += MessageUtil.DUMMY;
            }
            // City
            if ((val = (String) ht.get(JsonTagsZ.CITY)) != null && !val.equals("")) {
              msg += " " + MessageUtil.encode(val);
            } else {
              msg += MessageUtil.DUMMY;
            }
            // Currency
            if ((val = (String) ht.get(JsonTagsZ.CURRENCY)) != null && !val.equals("")) {
              msg += " " + val;
            } else {
              msg += " " + MessageUtil.DUMMY;
            }
            // Tax
            if ((val = (String) ht.get(JsonTagsZ.TAX)) != null && !val.equals("")) {
              msg += " " + val;
            } else {
              msg += " " + MessageUtil.DUMMY;
            }
            // Add materials
            msg +=
                " " + MessageUtil.getMaterialsString((Vector) ht.get(JsonTagsZ.MATERIALS),
                    MessageUtil.ALL_FIELDS, false);
            // Add vendors, if present
            msg += " " + MessageUtil.getRelatedKioskString((Vector) ht.get(JsonTagsZ.VENDORS));
            // Add customers, if present
            msg += " " + MessageUtil.getRelatedKioskString((Vector) ht.get(JsonTagsZ.CUSTOMERS));
          } // end while
        }
      }
      // Add configuration data
      // Transactions to be disabled
      if (config != null && (val = (String) config.get(JsonTagsZ.TRANSACTIONS)) != null && !val
          .equals("")) {
        msg += " " + val;
      } else {
        msg += " " + MessageUtil.DUMMY;
      }
      // Transaction naming
      if (config != null && (val = (String) config.get(JsonTagsZ.TRANSACTION_NAMING)) != null
          && !val.equals("")) {
        msg += " " + val;
      } else {
        msg += " " + MessageUtil.DUMMY;
      }
      // Order/demand mode
      if (config != null && (val = (String) config.get(JsonTagsZ.ORDER_GENERATION)) != null && !val
          .equals("")) {
        msg += " " + val;
      } else {
        msg += " " + MessageUtil.DUMMY;
      }
      if (Integer.parseInt(version) >= Integer
          .parseInt(MessageHeader.VERSION04)) { // version 04 onwards
        // Get the vendor mandatory setting
        if (config != null && (val = (String) config.get(JsonTagsZ.VENDORS_MANDATORY)) != null
            && !val.equals("")) {
          msg += " " + val;
        } else {
          msg += " " + MessageUtil.DUMMY;
        }
        // Get the customers mandatory setting
        if (config != null && (val = (String) config.get(JsonTagsZ.CUSTOMERS_MANDATORY)) != null
            && !val.equals("")) {
          msg += " " + val;
        } else {
          msg += " " + MessageUtil.DUMMY;
        }
      }
      // Add config. for auto-posting of inventory on order status change
      if (Integer.parseInt(version) >= Integer.parseInt(MessageHeader.VERSION07)) {
        // Update stock on shipped
        if (config != null && (val = (String) config.get(JsonTagsZ.UPDATESTOCK_ON_SHIPPED)) != null
            && "true".equals(val)) {
          msg += " 1";
        } else {
          msg += " 0";
        }
        // Update stock on fulfilled
        if (config != null
            && (val = (String) config.get(JsonTagsZ.UPDATESTOCK_ON_FULFILLED)) != null && "true"
            .equals(val)) {
          msg += " 1";
        } else {
          msg += " 0";
        }
        // Add wastage reason codes, if present
        if (config != null && (val = (String) config.get(JsonTagsZ.REASONS_WASTAGE)) != null && !val
            .equals("")) {
          msg += " " + MessageUtil.encode(val);
        } else {
          msg += " " + MessageUtil.DUMMY;
        }
        // Add issue reason codes, if present
        if (config != null && (val = (String) config.get(JsonTagsZ.REASONS_ISSUE)) != null && !val
            .equals("")) {
          msg += " " + MessageUtil.encode(val);
        } else {
          msg += " " + MessageUtil.DUMMY;
        }
        // Add receipt reason codes, if present
        if (config != null && (val = (String) config.get(JsonTagsZ.REASONS_RECEIPT)) != null && !val
            .equals("")) {
          msg += " " + MessageUtil.encode(val);
        } else {
          msg += " " + MessageUtil.DUMMY;
        }
        // Add stock count reason codes, if present
        if (config != null && (val = (String) config.get(JsonTagsZ.REASONS_STOCKCOUNT)) != null
            && !val.equals("")) {
          msg += " " + MessageUtil.encode(val);
        } else {
          msg += " " + MessageUtil.DUMMY;
        }
        // Add return reason codes, if present
        if (config != null && (val = (String) config.get(JsonTagsZ.REASONS_RETURN)) != null && !val
            .equals("")) {
          msg += " " + MessageUtil.encode(val);
        } else {
          msg += " " + MessageUtil.DUMMY;
        }
      }
    }
    return MessageUtil.split(msg, getMessageId());
  }

  // Load from a message string
  public void fromMessageString(Vector messages) throws ProtocolException {
    if (messages == null || messages.isEmpty()) {
      throw new ProtocolException("Message not specified");
    }
    String message = MessageUtil.assemble(messages);
    if (message == null || message.length() == 0) {
      throw new ProtocolException("Message not specified");
    }
    StringTokenizer st = new StringTokenizer(message, " ");
    //  System.out.println("In frommessagestring-authenicateoutput, st: " + st);
    // version
    if (st.hasMoreTokens()) {
      version = st.nextToken();
    }
    // status
    if (st.hasMoreTokens()) {
      statusCode = st.nextToken();
      status = JsonTagsZ.STATUS_TRUE.equals(statusCode);
    }
    if (!status) {
      errMsg = MessageUtil.decode(st.nextToken());
    } else if (st.hasMoreTokens()) {
      // Get user data
      // user's first name
      user = new Hashtable();
      // First name (mandatory)
      user.put(JsonTagsZ.FIRST_NAME, MessageUtil.decode(st.nextToken()));
      String val = null;
      if (Integer.parseInt(version) >= Integer
          .parseInt(MessageHeader.VERSION05)) { // version 05 onwards
        // Role
        val = st.nextToken();
        if (!MessageUtil.DUMMY.equals(val)) {
          user.put(JsonTagsZ.ROLE, val);
        }
      }
      // Get kiosk data
      kiosks = new Vector();
      if (MessageHeader.VERSION.equals(version)) { // version 01
        Hashtable k = new Hashtable();
        // Kiosk ID
        k.put(JsonTagsZ.KIOSK_ID, st.nextToken());
        // Currency (optional)
        val = st.nextToken();
        if (!MessageUtil.DUMMY.equals(val)) {
          k.put(JsonTagsZ.CURRENCY, val);
        }
        // Tax (optional)
        val = st.nextToken();
        if (!MessageUtil.DUMMY.equals(val)) {
          k.put(JsonTagsZ.TAX, val);
        }
        // Add materials
        k.put(JsonTagsZ.MATERIALS, MessageUtil.getMaterialsVector(st, tags));
        // Add kiosk
        kiosks.addElement(k);
      } else { // version 02 onwards - multi-kiosk handling
        // Get number of kiosks
        int numKiosks = Integer.parseInt(st.nextToken());
        if (numKiosks > 0) {
          // Get kiosks data
          for (int i = 0; i < numKiosks; i++) {
            Hashtable k = new Hashtable();
            k.put(JsonTagsZ.KIOSK_ID, st.nextToken());
            // Name
            val = st.nextToken();
            if (!MessageUtil.DUMMY.equals(val)) {
              k.put(JsonTagsZ.NAME, MessageUtil.decode(val));
            }
            // City
            val = st.nextToken();
            if (!MessageUtil.DUMMY.equals(val)) {
              k.put(JsonTagsZ.CITY, MessageUtil.decode(val));
            }
            // Currency
            val = st.nextToken();
            if (!MessageUtil.DUMMY.equals(val)) {
              k.put(JsonTagsZ.CURRENCY, val);
            }
            // Tax (optional)
            val = st.nextToken();
            if (!MessageUtil.DUMMY.equals(val)) {
              k.put(JsonTagsZ.TAX, val);
            }
            // Add materials
            k.put(JsonTagsZ.MATERIALS, MessageUtil.getMaterialsVector(st, tags));
            // Add vendors, if present
            k.put(JsonTagsZ.VENDORS, MessageUtil.getRelatedKioskVector(st));
            // Add customers, if present
            k.put(JsonTagsZ.CUSTOMERS, MessageUtil.getRelatedKioskVector(st));
            // Add kiosk
            kiosks.addElement(k);
          }
        }
      }
      // Get configuration data
      config = new Hashtable();
      val = st.nextToken(); // transactions disabled
      if (!MessageUtil.DUMMY.equals(val)) {
        config.put(JsonTagsZ.TRANSACTIONS, val);
      }
      val = st.nextToken(); // transaction naming
      if (!MessageUtil.DUMMY.equals(val)) {
        config.put(JsonTagsZ.TRANSACTION_NAMING, val);
      }
      val = st.nextToken(); // order/demand mode
      if (!MessageUtil.DUMMY.equals(val)) {
        config.put(JsonTagsZ.ORDER_GENERATION, val);
      }
      if (Integer.parseInt(version) >= Integer.parseInt(MessageHeader.VERSION04) && st
          .hasMoreTokens()) { // version 04 onwards
        // Get whether vendor is mandatory
        val = st.nextToken();
        if (!MessageUtil.DUMMY.equals(val)) {
          config.put(JsonTagsZ.VENDORS_MANDATORY, val);
        }
        // Get whether customer is mandatory
        if (st.hasMoreTokens()) {
          val = st.nextToken();
          if (!MessageUtil.DUMMY.equals(val)) {
            config.put(JsonTagsZ.CUSTOMERS_MANDATORY, val);
          }
        }
      }
      // Read config. for auto-posting of inventory on order status change
      if (Integer.parseInt(version) >= Integer.parseInt(MessageHeader.VERSION07)) {
        // Get update stock on shipped config.
        if (st.hasMoreTokens()) {
          val = st.nextToken();
          config.put(JsonTagsZ.UPDATESTOCK_ON_SHIPPED, String.valueOf("1".equals(val)));
        }
        // Get update stock on fulfilled config.
        if (st.hasMoreTokens()) {
          val = st.nextToken();
          config.put(JsonTagsZ.UPDATESTOCK_ON_FULFILLED, String.valueOf("1".equals(val)));
        }
        // Get the wastage reasons, if present
        if (st.hasMoreTokens()) {
          val = MessageUtil.decode(st.nextToken());
          config.put(JsonTagsZ.REASONS_WASTAGE, val);
        }
        // Get the issue reasons, if present
        if (st.hasMoreTokens()) {
          val = MessageUtil.decode(st.nextToken());
          config.put(JsonTagsZ.REASONS_ISSUE, val);
        }
        // Get the receipt reasons, if present
        if (st.hasMoreTokens()) {
          val = MessageUtil.decode(st.nextToken());
          config.put(JsonTagsZ.REASONS_RECEIPT, val);
        }
        // Get the stock count reasons, if present
        if (st.hasMoreTokens()) {
          val = MessageUtil.decode(st.nextToken());
          config.put(JsonTagsZ.REASONS_STOCKCOUNT, val);
        }
        // Get the return reasons, if present
        if (st.hasMoreTokens()) {
          val = MessageUtil.decode(st.nextToken());
          config.put(JsonTagsZ.REASONS_RETURN, val);
        }
        // Get the defaut vendor , if present
        if (st.hasMoreTokens()) {
          val = MessageUtil.decode(st.nextToken());
          config.put(JsonTagsZ.VENDORID, val);
        }
        if (st.hasMoreTokens()) {
          val = MessageUtil.decode(st.nextToken());
          config.put(JsonTagsZ.VENDOR, val);
        }
        if (st.hasMoreTokens()) {
          val = MessageUtil.decode(st.nextToken());
          config.put(JsonTagsZ.VENDOR_CITY, val);
        }
        if (st.hasMoreTokens()) {
          val = MessageUtil.decode(st.nextToken());
          config.put(JsonTagsZ.SUPPORT_EMAIL, val);
        }
        if (st.hasMoreTokens()) {
          val = MessageUtil.decode(st.nextToken());
          config.put(JsonTagsZ.SUPPORT_PHONE, val);
        }
        if (st.hasMoreTokens()) {
          val = MessageUtil.decode(st.nextToken());
          config.put(JsonTagsZ.SUPPORT_CONTACT_NAME, val);
        }
        if (st.hasMoreTokens()) {
          val = MessageUtil.decode(st.nextToken());
          config.put(JsonTagsZ.ENABLE_SHIPPING_MOBILE, val);
        }
        if (st.hasMoreTokens()) {
          val = MessageUtil.decode(st.nextToken());
          config.put(JsonTagsZ.DISABLE_ORDER_PRICING, val);
        }
      }
    }
  }

  /**
   * Form the JSON Object for version 01 of protocol
   */
  private JSONObject toJSONObject() throws JSONException {
    JSONObject json = new JSONObject();
    // Add version
    if (version != null && !version.equals("")) {
      json.put(JsonTagsZ.VERSION, version);
    } else {
      json.put(JsonTagsZ.VERSION, "01");
    }
    // Add status code
    json.put(JsonTagsZ.STATUS, statusCode);
    if (cursor != null && !cursor.equals(
        "")) // represents kiosks pagination cursor, if multiple kiosks; otherwise, material pagination cursor
    {
      json.put(JsonTagsZ.CURSOR, cursor);
    }
    if (!status) {
      // Send error message
      json.put(JsonTagsZ.MESSAGE, this.errMsg);
    } else {
      // Normal output
      // Add login expiry time
      if (this.expiryTime != null) {
        json.put(JsonTagsZ.TIMESTAMP, this.expiryTime);
      }
      // Add user data
      addUserData(json);
      // Add kiosks with materials
      addKioskData(json);
      // Add config
      addConfigurationData(json);
    }

    return json;
  }

  private void loadFromJSON(JSONObject json) throws JSONException {
    this.statusCode = (String) json.get(JsonTagsZ.STATUS);
    this.status = JsonTagsZ.STATUS_TRUE.equals(this.statusCode);
    // Get cursor, if any
    try {
      cursor = json.getString(JsonTagsZ.CURSOR);
    } catch (Exception e) {
      // ignore
    }
    if (this.status) {
      // Get user data
      loadUserData(json);
      // Get kiosk data along with materials
      loadKioskData(json);
      // Get config. data
      loadConfigurationData(json);
    } else {
      // Get the error message
      this.errMsg = (String) json.get(JsonTagsZ.MESSAGE);
    }
  }

  // Add user attributes to JSON
  private void addUserData(JSONObject json) throws JSONException {
    String value = null;
    if (user == null || user.isEmpty()) {
      return;
    }

    // Add first name
    if ((value = (String) user.get(JsonTagsZ.FIRST_NAME)) != null) {
      json.put(JsonTagsZ.FIRST_NAME, value);
    }
    // Add last name
    if ((value = (String) user.get(JsonTagsZ.LAST_NAME)) != null) {
      json.put(JsonTagsZ.LAST_NAME, value);
    }
    // Add role
    if ((value = (String) user.get(JsonTagsZ.ROLE)) != null) {
      json.put(JsonTagsZ.ROLE, value);
    }
    // Add timezone
    if ((value = (String) user.get(JsonTagsZ.TIMEZONE)) != null) {
      json.put(JsonTagsZ.TIMEZONE, value);
    }
    // Add language code
    if ((value = (String) user.get(JsonTagsZ.LANGUAGE)) != null) {
      json.put(JsonTagsZ.LANGUAGE, value);
    }
    // Add country
    if ((value = (String) user.get(JsonTagsZ.COUNTRY)) != null) {
      json.put(JsonTagsZ.COUNTRY, value);
    }
    // Add all address fields, as present
    // State
    if ((value = (String) user.get(JsonTagsZ.STATE)) != null && !value.equals("")) {
      json.put(JsonTagsZ.STATE, value);
    }
    // District
    if ((value = (String) user.get(JsonTagsZ.DISTRICT)) != null && !value.equals("")) {
      json.put(JsonTagsZ.DISTRICT, value);
    }
    // Taluk
    if ((value = (String) user.get(JsonTagsZ.TALUK)) != null && !value.equals("")) {
      json.put(JsonTagsZ.TALUK, value);
    }
    // City
    if ((value = (String) user.get(JsonTagsZ.CITY)) != null && !value.equals("")) {
      json.put(JsonTagsZ.CITY, value);
    }
    // Pin code
    if ((value = (String) user.get(JsonTagsZ.PINCODE)) != null && !value.equals("")) {
      json.put(JsonTagsZ.PINCODE, value);
    }
    // Street address
    if ((value = (String) user.get(JsonTagsZ.STREET_ADDRESS)) != null && !value.equals("")) {
      json.put(JsonTagsZ.STREET_ADDRESS, value);
    }
    // Email
    if ((value = (String) user.get(JsonTagsZ.EMAIL)) != null && !value.equals("")) {
      json.put(JsonTagsZ.EMAIL, value);
    }
    // Mobile
    if ((value = (String) user.get(JsonTagsZ.MOBILE)) != null && !value.equals("")) {
      json.put(JsonTagsZ.MOBILE, value);
    }
    // Landline
    if ((value = (String) user.get(JsonTagsZ.LANDLINE)) != null && !value.equals("")) {
      json.put(JsonTagsZ.LANDLINE, value);
    }
    // Route enabled or not
    if ((value = (String) user.get(JsonTagsZ.ROUTE_ENABLED_USER)) != null && !value.equals("")) {
      json.put(JsonTagsZ.ROUTE_ENABLED_USER, value);
    }
    // Primary entity
    if ((value = (String) user.get(JsonTagsZ.KIOSK_ID)) != null && !value.equals("")) {
      json.put(JsonTagsZ.KIOSK_ID, value);
    }
    // Custom user ID
    if ((value = (String) user.get(JsonTagsZ.CUSTOM_USERID)) != null && !value.equals("")) {
      json.put(JsonTagsZ.CUSTOM_USERID, value);
    }
  }

  // Load user data from JSON object
  private void loadUserData(JSONObject json) {
    this.user = new Hashtable();
    // Get first name
    try {
      user.put(JsonTagsZ.FIRST_NAME, (String) json.get(JsonTagsZ.FIRST_NAME));
    } catch (JSONException e) {
      // ignore
    }
    // Get last name
    try {
      user.put(JsonTagsZ.LAST_NAME, (String) json.get(JsonTagsZ.LAST_NAME));
    } catch (JSONException e) {
      // ignore
    }
    // Get role
    try {
      user.put(JsonTagsZ.ROLE, (String) json.get(JsonTagsZ.ROLE));
    } catch (JSONException e) {
      // do nothing
    }
    // Get timezone
    try {
      user.put(JsonTagsZ.TIMEZONE, (String) json.get(JsonTagsZ.TIMEZONE));
    } catch (JSONException e) {
      // ignore
    }
    // Get language
    try {
      user.put(JsonTagsZ.LANGUAGE, (String) json.get(JsonTagsZ.LANGUAGE));
    } catch (JSONException e) {
      // ignore
    }
    // Get country
    try {
      user.put(JsonTagsZ.COUNTRY, (String) json.get(JsonTagsZ.COUNTRY));
    } catch (JSONException e) {
      // ignore
    }
    // State
    try {
      user.put(JsonTagsZ.STATE, (String) json.get(JsonTagsZ.STATE));
    } catch (JSONException e) {
      // ignore
    }
    // District
    try {
      user.put(JsonTagsZ.DISTRICT, (String) json.get(JsonTagsZ.DISTRICT));
    } catch (JSONException e) {
      // ignore
    }
    // Taluk
    try {
      user.put(JsonTagsZ.TALUK, (String) json.get(JsonTagsZ.TALUK));
    } catch (JSONException e) {
      // ignore
    }
    // City
    try {
      user.put(JsonTagsZ.CITY, (String) json.get(JsonTagsZ.CITY));
    } catch (JSONException e) {
      // ignore
    }
    // Pin code
    try {
      user.put(JsonTagsZ.PINCODE, (String) json.get(JsonTagsZ.PINCODE));
    } catch (JSONException e) {
      // ignore
    }
    // Street address
    try {
      user.put(JsonTagsZ.STREET_ADDRESS, (String) json.get(JsonTagsZ.STREET_ADDRESS));
    } catch (JSONException e) {
      // ignore
    }
    // Email
    try {
      user.put(JsonTagsZ.EMAIL, (String) json.get(JsonTagsZ.EMAIL));
    } catch (JSONException e) {
      // ignore
    }
    // Mobile
    try {
      user.put(JsonTagsZ.MOBILE, (String) json.get(JsonTagsZ.MOBILE));
    } catch (JSONException e) {
      // ignore
    }
    // Landline
    try {
      user.put(JsonTagsZ.LANDLINE, (String) json.get(JsonTagsZ.LANDLINE));
    } catch (JSONException e) {
      // ignore
    }
    // Route enabled or not
    try {
      user.put(JsonTagsZ.ROUTE_ENABLED_USER, (String) json.get(JsonTagsZ.ROUTE_ENABLED_USER));
    } catch (JSONException e) {
      // ignore
    }
    // Primary entity
    try {
      user.put(JsonTagsZ.KIOSK_ID, (String) json.get(JsonTagsZ.KIOSK_ID));
    } catch (JSONException e) {
      // ignore
    }
    // Custom User ID
    try {
      user.put(JsonTagsZ.CUSTOM_USERID, (String) json.get(JsonTagsZ.CUSTOM_USERID));
    } catch (JSONException e) {
      // ignore
    }
  }

  // Add kiosk data to JSON object
  private void addKioskData(JSONObject json) throws JSONException {
    // if ( kiosks == null || kiosks.isEmpty() )
    //  return;
    //throw new JSONException( protoMessages.getString( "error.nokiosks" ) );
    // Init. array
    JSONArray array = new JSONArray();
    if (kiosks != null && !kiosks.isEmpty()) {
      Enumeration en = kiosks.elements();
      while (en.hasMoreElements()) {
        Hashtable kiosk = (Hashtable) en.nextElement();
        // Get the kiosk JSONObject
        JSONObject k = new JSONObject();
        // Get mandatory attributes (if null, JSON exceptions will be thrown)
        k.put(JsonTagsZ.KIOSK_ID, (String) kiosk.get(JsonTagsZ.KIOSK_ID));
        k.put(JsonTagsZ.NAME, (String) kiosk.get(JsonTagsZ.NAME));
        String value = null;
        if ((value = (String) kiosk.get(JsonTagsZ.CITY)) != null) {
          k.put(JsonTagsZ.CITY, value);
        }
        if ((value = (String) kiosk.get(JsonTagsZ.STATE)) != null) {
          k.put(JsonTagsZ.STATE, value);
        }
        if ((value = (String) kiosk.get(JsonTagsZ.DISTRICT)) != null) {
          k.put(JsonTagsZ.DISTRICT, value);
        }
        if ((value = (String) kiosk.get(JsonTagsZ.TALUK)) != null) {
          k.put(JsonTagsZ.TALUK, value);
        }
        if ((value = (String) kiosk.get(JsonTagsZ.STREET_ADDRESS)) != null) {
          k.put(JsonTagsZ.STREET_ADDRESS, value);
        }
        if ((value = (String) kiosk.get(JsonTagsZ.PINCODE)) != null) {
          k.put(JsonTagsZ.PINCODE, value);
        }
        if ((value = (String) kiosk.get(JsonTagsZ.LATITUDE)) != null) {
          k.put(JsonTagsZ.LATITUDE, value);
        }
        if ((value = (String) kiosk.get(JsonTagsZ.LONGITUDE)) != null) {
          k.put(JsonTagsZ.LONGITUDE, value);
        }
        if ((value = (String) kiosk.get(JsonTagsZ.GEO_ACCURACY)) != null) {
          k.put(JsonTagsZ.GEO_ACCURACY, value);
        }
        if ((value = (String) kiosk.get(JsonTagsZ.GEO_ERROR_CODE)) != null) {
          k.put(JsonTagsZ.GEO_ERROR_CODE, value);
        }
        if ((value = (String) kiosk.get(JsonTagsZ.CURRENCY)) != null) {
          k.put(JsonTagsZ.CURRENCY, value);
        }
        if ((value = (String) kiosk.get(JsonTagsZ.TAX)) != null) {
          k.put(JsonTagsZ.TAX, value);
        }
        if ((value = (String) kiosk.get(JsonTagsZ.ROUTE_INDEX)) != null) {
          k.put(JsonTagsZ.ROUTE_INDEX, value);
        }
        if ((value = (String) kiosk.get(JsonTagsZ.ROUTE_TAG)) != null) {
          k.put(JsonTagsZ.ROUTE_TAG, value);
        }
        if ((value = (String) kiosk.get(JsonTagsZ.ROUTE_ENABLED_CUSTOMERS)) != null) {
          k.put(JsonTagsZ.ROUTE_ENABLED_CUSTOMERS, value);
        }
        if ((value = (String) kiosk.get(JsonTagsZ.ROUTE_ENABLED_VENDORS)) != null) {
          k.put(JsonTagsZ.ROUTE_ENABLED_VENDORS, value);
        }
        if ((value = (String) kiosk.get(JsonTagsZ.CUSTOM_KIOSKID)) != null) {
          k.put(JsonTagsZ.CUSTOM_KIOSKID, value);
        }
        if ((value = (String) kiosk.get(JsonTagsZ.DISABLE_BATCH_MGMT)) != null) {
          k.put(JsonTagsZ.DISABLE_BATCH_MGMT, value);
        }
        // Add materials for this kiosk
        Vector mtrls = (Vector) kiosk.get(JsonTagsZ.MATERIALS);
        JsonUtil.addMaterialData(k, mtrls);
        // Add vendors to the kiosk, if they exist
        JsonUtil.addRelatedKioskData(k, (Vector) kiosk.get(JsonTagsZ.VENDORS), JsonTagsZ.VENDORS);
        // Add customers to the kiosk, if they exist
        JsonUtil
            .addRelatedKioskData(k, (Vector) kiosk.get(JsonTagsZ.CUSTOMERS), JsonTagsZ.CUSTOMERS);
        // Add users, if any
        JsonUtil.addKioskUsers(k, (Vector) kiosk.get(JsonTagsZ.USERS));
        // Add to array
        array.put(k);
      }
    }
    // Add the array to the JSON container
    json.put(JsonTagsZ.KIOSKS, array);
  }

  // Load kiosk data from JSON
  private void loadKioskData(JSONObject json) throws JSONException {
    // Get JSON array
    JSONArray array = null;
    try {
      array = (JSONArray) json.get(JsonTagsZ.KIOSKS);
    } catch (JSONException e) {
      return;
    }
    this.kiosks = new Vector();
    for (int i = 0; i < array.length(); i++) {
      JSONObject k = (JSONObject) array.get(i);
      Hashtable kiosk = new Hashtable();
      kiosk.put(JsonTagsZ.KIOSK_ID, (String) k.get(JsonTagsZ.KIOSK_ID));
      kiosk.put(JsonTagsZ.NAME, (String) k.get(JsonTagsZ.NAME));
      try {
        kiosk.put(JsonTagsZ.CITY, k.get(JsonTagsZ.CITY));
      } catch (JSONException e) {
        // do nothing
      }
      try {
        kiosk.put(JsonTagsZ.STATE, k.get(JsonTagsZ.STATE));
      } catch (JSONException e) {
        // do nothing
      }
      try {
        kiosk.put(JsonTagsZ.DISTRICT, k.get(JsonTagsZ.DISTRICT));
      } catch (JSONException e) {
        // do nothing
      }
      try {
        kiosk.put(JsonTagsZ.TALUK, k.get(JsonTagsZ.TALUK));
      } catch (JSONException e) {
        // do nothing
      }
      try {
        kiosk.put(JsonTagsZ.STREET_ADDRESS, k.get(JsonTagsZ.STREET_ADDRESS));
      } catch (JSONException e) {
        // do nothing
      }
      try {
        kiosk.put(JsonTagsZ.PINCODE, k.get(JsonTagsZ.PINCODE));
      } catch (JSONException e) {
        // do nothing
      }
      try {
        kiosk.put(JsonTagsZ.LATITUDE, k.get(JsonTagsZ.LATITUDE));
      } catch (JSONException e) {
        // do nothing
      }
      try {
        kiosk.put(JsonTagsZ.LONGITUDE, k.get(JsonTagsZ.LONGITUDE));
      } catch (JSONException e) {
        // do nothing
      }
      try {
        kiosk.put(JsonTagsZ.GEO_ACCURACY, k.get(JsonTagsZ.GEO_ACCURACY));
      } catch (JSONException e) {
        // do nothing
      }
      try {
        kiosk.put(JsonTagsZ.GEO_ERROR_CODE, k.get(JsonTagsZ.GEO_ERROR_CODE));
      } catch (JSONException e) {
        // do nothing
      }
      try {
        kiosk.put(JsonTagsZ.CURRENCY, k.get(JsonTagsZ.CURRENCY));
      } catch (JSONException e) {
        // do nothing
      }
      try {
        kiosk.put(JsonTagsZ.TAX, k.get(JsonTagsZ.TAX));
      } catch (JSONException e) {
        // do nothing
      }
      // Route index
      try {
        kiosk.put(JsonTagsZ.ROUTE_INDEX, k.get(JsonTagsZ.ROUTE_INDEX));
      } catch (JSONException e) {
        // do nothing
      }
      // Route tag
      try {
        kiosk.put(JsonTagsZ.ROUTE_TAG, k.get(JsonTagsZ.ROUTE_TAG));
      } catch (JSONException e) {
        // do nothing
      }
      // Route enabled for customers
      try {
        kiosk.put(JsonTagsZ.ROUTE_ENABLED_CUSTOMERS, k.get(JsonTagsZ.ROUTE_ENABLED_CUSTOMERS));
      } catch (JSONException e) {
        // do nothing
      }
      // Route enabled for vendors
      try {
        kiosk.put(JsonTagsZ.ROUTE_ENABLED_VENDORS, k.get(JsonTagsZ.ROUTE_ENABLED_VENDORS));
      } catch (JSONException e) {
        // do nothing
      }
      // Custom Custom Kiosk ID if present
      try {
        kiosk.put(JsonTagsZ.CUSTOM_KIOSKID, k.get(JsonTagsZ.CUSTOM_KIOSKID));
      } catch (JSONException e) {
        // do nothing
      }
      // Disable batch management
      try {
        kiosk.put(JsonTagsZ.DISABLE_BATCH_MGMT, k.get(JsonTagsZ.DISABLE_BATCH_MGMT));
      } catch (JSONException e) {
        // do nothing
      }
      // Get materials and update tag-maps, if needed
      kiosk.put(JsonTagsZ.MATERIALS, JsonUtil.getMaterialData(k, tags));
      // Get kiosk vendors, if present
      try {
        kiosk.put(JsonTagsZ.VENDORS, JsonUtil.getRelatedKioskData(k, JsonTagsZ.VENDORS));
      } catch (JSONException e) {
        // do nothing
      }
      // Get kiosk customers, if present
      try {
        kiosk.put(JsonTagsZ.CUSTOMERS, JsonUtil.getRelatedKioskData(k, JsonTagsZ.CUSTOMERS));
      } catch (JSONException e) {
        // do nothing
      }
      // Get users, if any
      try {
        kiosk.put(JsonTagsZ.USERS, JsonUtil.getKioskUsers(k));
      } catch (Exception e) {
        // ignore
      }
      // Add to vector
      kiosks.addElement(kiosk);
    }
  }

  // Add configuration data, if present
  private void addConfigurationData(JSONObject json) throws JSONException {
    if (config == null || config.isEmpty()) {
      return;
    }
    String value = null;

    JSONObject confJson = new JSONObject();
    if ((value = (String) config.get(JsonTagsZ.TRANSACTIONS)) != null) {
      confJson.put(JsonTagsZ.TRANSACTIONS, value);
    }
    if ((value = (String) config.get(JsonTagsZ.TRANSACTION_NAMING)) != null) {
      confJson.put(JsonTagsZ.TRANSACTION_NAMING, value);
    }
    if ((value = (String) config.get(JsonTagsZ.ORDER_GENERATION)) != null) {
      confJson.put(JsonTagsZ.ORDER_GENERATION, value);
    }
    if ((value = (String) config.get(JsonTagsZ.TAGS_INVENTORY)) != null) {
      confJson.put(JsonTagsZ.TAGS_INVENTORY, value);
    }
    if ((value = (String) config.get(JsonTagsZ.TAGS_ORDERS)) != null) {
      confJson.put(JsonTagsZ.TAGS_ORDERS, value);
    }
    if ((value = (String) config.get(JsonTagsZ.VENDORS_MANDATORY)) != null) {
      confJson.put(JsonTagsZ.VENDORS_MANDATORY, value);
    }
    if ((value = (String) config.get(JsonTagsZ.CUSTOMERS_MANDATORY)) != null) {
      confJson.put(JsonTagsZ.CUSTOMERS_MANDATORY, value);
    }
    if ((value = (String) config.get(JsonTagsZ.UPDATESTOCK_ON_SHIPPED)) != null) {
      confJson.put(JsonTagsZ.UPDATESTOCK_ON_SHIPPED, value);
    }
    if ((value = (String) config.get(JsonTagsZ.UPDATESTOCK_ON_FULFILLED)) != null) {
      confJson.put(JsonTagsZ.UPDATESTOCK_ON_FULFILLED, value);
    }
    // Allow empty orders, if required
    if ((value = (String) config.get(JsonTagsZ.ALLOW_EMPTY_ORDERS)) != null) {
      confJson.put(JsonTagsZ.ALLOW_EMPTY_ORDERS, value);
    }
    // Add the Order Mark as Fulfilled configuration, if present
    if ((value = (String) config.get(JsonTagsZ.ORDER_MARKASFULFILLED)) != null && !value
        .equals("")) {
      confJson.put(JsonTagsZ.ORDER_MARKASFULFILLED, value);
    }
    // Add payment options, if required
    if ((value = (String) config.get(JsonTagsZ.PAYMENT_OPTION)) != null) {
      confJson.put(JsonTagsZ.PAYMENT_OPTION, value);
    }
    // Add package sizes, if required
    if ((value = (String) config.get(JsonTagsZ.PACKAGE_SIZE)) != null) {
      confJson.put(JsonTagsZ.PACKAGE_SIZE, value);
    }
    // Add the wastage reasons, if they exist
    if ((value = (String) config.get(JsonTagsZ.REASONS_WASTAGE)) != null && !value.equals("")) {
      confJson.put(JsonTagsZ.REASONS_WASTAGE, value);
    }
    // Add the issue reasons, if they exist
    if ((value = (String) config.get(JsonTagsZ.REASONS_ISSUE)) != null && !value.equals("")) {
      confJson.put(JsonTagsZ.REASONS_ISSUE, value);
    }
    // Add the receipt reasons, if they exist
    if ((value = (String) config.get(JsonTagsZ.REASONS_RECEIPT)) != null && !value.equals("")) {
      confJson.put(JsonTagsZ.REASONS_RECEIPT, value);
    }
    // Add the stock count reasons, if they exist
    if ((value = (String) config.get(JsonTagsZ.REASONS_STOCKCOUNT)) != null && !value.equals("")) {
      confJson.put(JsonTagsZ.REASONS_STOCKCOUNT, value);
    }
    // Add the return reasons, if they exist
    if ((value = (String) config.get(JsonTagsZ.REASONS_RETURN)) != null && !value.equals("")) {
      confJson.put(JsonTagsZ.REASONS_RETURN, value);
    }
    // Add the transfer reasons, if they exist
    if ((value = (String) config.get(JsonTagsZ.REASONS_TRANSFER)) != null && !value.equals("")) {
      confJson.put(JsonTagsZ.REASONS_TRANSFER, value);
    }
    // Get the geo-coding strategy, if present
    if ((value = (String) config.get(JsonTagsZ.GEOCODING_STRATEGY)) != null && !value.equals("")) {
      confJson.put(JsonTagsZ.GEOCODING_STRATEGY, value);
    }
    // Get the type of entity to be created, if any
    if ((value = (String) config.get(JsonTagsZ.CREATABLE_ENTITY_TYPES)) != null && !value
        .equals("")) {
      confJson.put(JsonTagsZ.CREATABLE_ENTITY_TYPES, value);
    }
    // Currency, if any
    if ((value = (String) config.get(JsonTagsZ.CURRENCY)) != null && !value.equals("")) {
      confJson.put(JsonTagsZ.CURRENCY, value);
    }
    // Route tags, if any
    if ((value = (String) config.get(JsonTagsZ.ROUTE_TAG)) != null && !value.equals("")) {
      confJson.put(JsonTagsZ.ROUTE_TAG, value);
    }
    // Allow route tag editing on mobile
    if ((value = (String) config.get(JsonTagsZ.ALLOW_ROUTETAG_EDITING)) != null && !value
        .equals("")) {
      confJson.put(JsonTagsZ.ALLOW_ROUTETAG_EDITING, value);
    }
    // Whether login should act like reconnect
    if ((value = (String) config.get(JsonTagsZ.LOGIN_AS_RECONNECT)) != null && !value.equals("")) {
      confJson.put(JsonTagsZ.LOGIN_AS_RECONNECT, value);
    }
    //Default vendor - Vendor id and vendor name - Chitra
    if ((value = (String) config.get(JsonTagsZ.VENDORID)) != null && !value.equals("")) {
      confJson.put(JsonTagsZ.VENDORID, value);
    }
    if ((value = (String) config.get(JsonTagsZ.VENDOR)) != null && !value.equals("")) {
      confJson.put(JsonTagsZ.VENDOR, value);
    }
    if ((value = (String) config.get(JsonTagsZ.VENDOR_CITY)) != null && !value.equals("")) {
      confJson.put(JsonTagsZ.VENDOR_CITY, value);
    }
    if ((value = (String) config.get(JsonTagsZ.SUPPORT_EMAIL)) != null && !value.equals("")) {
      confJson.put(JsonTagsZ.SUPPORT_EMAIL, value);
    }
    if ((value = (String) config.get(JsonTagsZ.SUPPORT_PHONE)) != null && !value.equals("")) {
      confJson.put(JsonTagsZ.SUPPORT_PHONE, value);
    }
    if ((value = (String) config.get(JsonTagsZ.SUPPORT_CONTACT_NAME)) != null && !value
        .equals("")) {
      confJson.put(JsonTagsZ.SUPPORT_CONTACT_NAME, value);
    }
    if ((value = (String) config.get(JsonTagsZ.ENABLE_SHIPPING_MOBILE)) != null && !value
        .equals("")) {
      confJson.put(JsonTagsZ.ENABLE_SHIPPING_MOBILE, value);
    }
    if ((value = (String) config.get(JsonTagsZ.DISABLE_ORDER_PRICING)) != null && !value
        .equals("")) {
      confJson.put(JsonTagsZ.DISABLE_ORDER_PRICING, value);
    }
    if ((value = (String) config.get(JsonTagsZ.NO_LOCAL_LOGIN_WITH_VALID_TOKEN)) != null && !value
        .equals("")) {
      confJson.put(JsonTagsZ.NO_LOCAL_LOGIN_WITH_VALID_TOKEN, value);
    }
    Hashtable hvalue = null;
    if ((hvalue = (Hashtable) config.get(JsonTagsZ.TAGS_INVENTORY_OPERATION)) != null && !hvalue
        .isEmpty()) {
      confJson.put(JsonTagsZ.TAGS_INVENTORY_OPERATION,
          addTagsInventoryOperation((Hashtable) config.get(JsonTagsZ.TAGS_INVENTORY_OPERATION)));
    }
    if ((hvalue = (Hashtable) config.get(JsonTagsZ.INTERVALS)) != null && !hvalue.isEmpty()) {
      confJson.put(JsonTagsZ.INTERVALS, addIntervals((Hashtable) config.get(JsonTagsZ.INTERVALS)));
    }
    if ((hvalue = (Hashtable) config.get(JsonTagsZ.MATERIAL_STATUS_OPERATION)) != null && !hvalue
        .isEmpty()) {
      confJson.put(JsonTagsZ.MATERIAL_STATUS_OPERATION,
          addMaterialStatus((Hashtable) config.get(JsonTagsZ.MATERIAL_STATUS_OPERATION)));
    }
    if ((hvalue = (Hashtable) config.get(JsonTagsZ.REASONS_BY_TAG)) != null && !hvalue.isEmpty()) {
      confJson.put(JsonTagsZ.REASONS_BY_TAG,
          addReasonsByTag((Hashtable) config.get(JsonTagsZ.REASONS_BY_TAG)));
    }
    if ((hvalue = (Hashtable) config.get(JsonTagsZ.CAPTURE_ACTUAL_TRANSACTION_DATE)) != null
        && !hvalue.isEmpty()) {
      confJson.put(JsonTagsZ.CAPTURE_ACTUAL_TRANSACTION_DATE, addCaptureActualTransactionDate(
          (Hashtable) config.get(JsonTagsZ.CAPTURE_ACTUAL_TRANSACTION_DATE)));
    }
    if ((hvalue = (Hashtable) config.get(JsonTagsZ.IGNORE_ORDER_RECOMMENDATION_REASONS)) != null
        && !hvalue.isEmpty()) {
      confJson.put(JsonTagsZ.IGNORE_ORDER_RECOMMENDATION_REASONS,
          addIgnoreOrderRecommendationReasons(
              (Hashtable) config.get(JsonTagsZ.IGNORE_ORDER_RECOMMENDATION_REASONS)));
    }
    if ((hvalue = (Hashtable) config.get(JsonTagsZ.SMS)) != null && !hvalue.isEmpty()) {
      confJson.put(JsonTagsZ.SMS, addSMSParameters((Hashtable) config.get(JsonTagsZ.SMS)));
    }
    if ((hvalue = (Hashtable) config.get(JsonTagsZ.APP_UPGRADE)) != null && !hvalue.isEmpty()) {
      confJson.put(JsonTagsZ.APP_UPGRADE,
          addAppUpgradeParameters((Hashtable) config.get(JsonTagsZ.APP_UPGRADE)));
    }
    //   System.out.println("Exiting loadConfigurationData, config: " + config.toString());
    // Add the conf. parameters
    json.put(JsonTagsZ.CONFIGURATION, confJson);
  }

  // Load configuration data, if present
  private void loadConfigurationData(JSONObject json) {
    this.config = new Hashtable();
    try {
      JSONObject confJson = (JSONObject) json.get(JsonTagsZ.CONFIGURATION);
      try {
        config.put(JsonTagsZ.TRANSACTIONS, (String) confJson.get(JsonTagsZ.TRANSACTIONS));
      } catch (JSONException e) {
        // do nothing
      }
      try {
        config
            .put(JsonTagsZ.TRANSACTION_NAMING, (String) confJson.get(JsonTagsZ.TRANSACTION_NAMING));
      } catch (JSONException e) {
        // do nothing
      }
      try {
        config.put(JsonTagsZ.ORDER_GENERATION, (String) confJson.get(JsonTagsZ.ORDER_GENERATION));
      } catch (JSONException e) {
        // do nothing
      }
      try {
        String tags = (String) confJson.get(JsonTagsZ.TAGS_INVENTORY);
        config.put(JsonTagsZ.TAGS_INVENTORY, tags);
      } catch (JSONException e) {
        // do nothing
      }
      try {
        String tags = (String) confJson.get(JsonTagsZ.TAGS_ORDERS);
        config.put(JsonTagsZ.TAGS_ORDERS, tags);
      } catch (JSONException e) {
        // do nothing
      }
      try {
        String vendorsMandatory = (String) confJson.get(JsonTagsZ.VENDORS_MANDATORY);
        config.put(JsonTagsZ.VENDORS_MANDATORY, vendorsMandatory);
      } catch (JSONException e) {
        // do nothing
      }
      try {
        String customersManatory = (String) confJson.get(JsonTagsZ.CUSTOMERS_MANDATORY);
        config.put(JsonTagsZ.CUSTOMERS_MANDATORY, customersManatory);
      } catch (JSONException e) {
        // do nothing
      }
      try {
        config
            .put(JsonTagsZ.ALLOW_EMPTY_ORDERS, (String) confJson.get(JsonTagsZ.ALLOW_EMPTY_ORDERS));
      } catch (JSONException e) {
        // do nothing
      }
      try {
        config.put(JsonTagsZ.ORDER_MARKASFULFILLED,
            (String) confJson.get(JsonTagsZ.ORDER_MARKASFULFILLED));
      } catch (JSONException e) {
        // do nothing
      }
      try {
        config.put(JsonTagsZ.PAYMENT_OPTION, (String) confJson.get(JsonTagsZ.PAYMENT_OPTION));
      } catch (JSONException e) {
        // do nothing
      }
      try {
        config.put(JsonTagsZ.PACKAGE_SIZE, (String) confJson.get(JsonTagsZ.PACKAGE_SIZE));
      } catch (JSONException e) {
        // do nothing
      }
      try {
        config.put(JsonTagsZ.UPDATESTOCK_ON_SHIPPED,
            confJson.getString(JsonTagsZ.UPDATESTOCK_ON_SHIPPED));
      } catch (JSONException e) {
        // do nothing
      }
      try {
        config.put(JsonTagsZ.UPDATESTOCK_ON_FULFILLED,
            confJson.getString(JsonTagsZ.UPDATESTOCK_ON_FULFILLED));
      } catch (JSONException e) {
        // do nothing
      }
      try {
        config.put(JsonTagsZ.REASONS_WASTAGE, confJson.getString(JsonTagsZ.REASONS_WASTAGE));
      } catch (JSONException e) {
        // do nothing
      }
      try {
        config.put(JsonTagsZ.REASONS_ISSUE, confJson.getString(JsonTagsZ.REASONS_ISSUE));
      } catch (JSONException e) {
        // do nothing
      }
      try {
        config.put(JsonTagsZ.REASONS_RECEIPT, confJson.getString(JsonTagsZ.REASONS_RECEIPT));
      } catch (JSONException e) {
        // do nothing
      }
      try {
        config.put(JsonTagsZ.REASONS_STOCKCOUNT, confJson.getString(JsonTagsZ.REASONS_STOCKCOUNT));
      } catch (JSONException e) {
        // do nothing
      }
      try {
        config.put(JsonTagsZ.REASONS_RETURN, confJson.getString(JsonTagsZ.REASONS_RETURN));
      } catch (JSONException e) {
        // do nothing
      }
      try {
        config.put(JsonTagsZ.REASONS_TRANSFER, confJson.getString(JsonTagsZ.REASONS_TRANSFER));
      } catch (JSONException e) {
        // do nothing
      }
      try {
        config.put(JsonTagsZ.GEOCODING_STRATEGY, confJson.getString(JsonTagsZ.GEOCODING_STRATEGY));
      } catch (JSONException e) {
        // do nothing
      }
      try {
        config.put(JsonTagsZ.CREATABLE_ENTITY_TYPES,
            confJson.getString(JsonTagsZ.CREATABLE_ENTITY_TYPES));
      } catch (JSONException e) {
        // do nothing
      }
      try {
        config.put(JsonTagsZ.CURRENCY, confJson.getString(JsonTagsZ.CURRENCY));
      } catch (JSONException e) {
        // do nothing
      }
      try {
        config.put(JsonTagsZ.ROUTE_TAG, confJson.getString(JsonTagsZ.ROUTE_TAG));
      } catch (JSONException e) {
        // do nothing
      }
      try {
        config.put(JsonTagsZ.ALLOW_ROUTETAG_EDITING,
            confJson.getString(JsonTagsZ.ALLOW_ROUTETAG_EDITING));
      } catch (JSONException e) {
        // do nothing
      }
      try {
        config.put(JsonTagsZ.LOGIN_AS_RECONNECT, confJson.getString(JsonTagsZ.LOGIN_AS_RECONNECT));
      } catch (JSONException e) {
        // do nothing
      }
      //default vendor - Vendor id and vendor name //Chitra
      try {
        config.put(JsonTagsZ.VENDORID, confJson.getString(JsonTagsZ.VENDORID));
      } catch (JSONException e) {
        // do nothing
      }
      try {
        config.put(JsonTagsZ.VENDOR, confJson.getString(JsonTagsZ.VENDOR));
      } catch (JSONException e) {
        // do nothing
      }
      try {
        config.put(JsonTagsZ.VENDOR_CITY, confJson.getString(JsonTagsZ.VENDOR_CITY));
      } catch (JSONException e) {
        // do nothing
      }
      try {
        config.put(JsonTagsZ.SUPPORT_EMAIL, confJson.getString(JsonTagsZ.SUPPORT_EMAIL));
      } catch (JSONException e) {
        // do nothing
      }
      try {
        config.put(JsonTagsZ.SUPPORT_PHONE, confJson.getString(JsonTagsZ.SUPPORT_PHONE));
      } catch (JSONException e) {
        // do nothing
      }
      try {
        config.put(JsonTagsZ.SUPPORT_CONTACT_NAME,
            confJson.getString(JsonTagsZ.SUPPORT_CONTACT_NAME));
      } catch (JSONException e) {
        // do nothing
      }
      try {
        config.put(JsonTagsZ.ENABLE_SHIPPING_MOBILE,
            confJson.getString(JsonTagsZ.ENABLE_SHIPPING_MOBILE));
      } catch (JSONException e) {
        // do nothing
      }
      try {
        config.put(JsonTagsZ.DISABLE_ORDER_PRICING,
            confJson.getString(JsonTagsZ.DISABLE_ORDER_PRICING));
      } catch (JSONException e) {
        // do nothing
      }
      try {
        config.put(JsonTagsZ.NO_LOCAL_LOGIN_WITH_VALID_TOKEN,
            confJson.getString(JsonTagsZ.NO_LOCAL_LOGIN_WITH_VALID_TOKEN));
      } catch (JSONException e) {
        // do nothing
      }
      try {
        config.put(JsonTagsZ.TAGS_INVENTORY_OPERATION,
            loadTagsInventoryOperation(confJson.getJSONObject(JsonTagsZ.TAGS_INVENTORY_OPERATION)));
      } catch (JSONException e) {
        // do nothing
      }
      try {
        config.put(JsonTagsZ.INTERVALS, loadIntervals(confJson.getJSONObject(JsonTagsZ.INTERVALS)));
      } catch (JSONException e) {
        // do nothing
      }
      try {
        config.put(JsonTagsZ.MATERIAL_STATUS_OPERATION,
            loadMaterialStatus(confJson.getJSONObject(JsonTagsZ.MATERIAL_STATUS_OPERATION)));
      } catch (JSONException e) {
        // do nothing
      }
      try {
        config.put(JsonTagsZ.REASONS_BY_TAG,
            loadReasonsByTag(confJson.getJSONObject(JsonTagsZ.REASONS_BY_TAG)));
      } catch (JSONException e) {
        // do nothing
      }
      try {
        config.put(JsonTagsZ.CAPTURE_ACTUAL_TRANSACTION_DATE, loadCaptureActualTransactionDate(
            confJson.getJSONObject(JsonTagsZ.CAPTURE_ACTUAL_TRANSACTION_DATE)));
      } catch (JSONException e) {
        // do nothing
      }
      try {
        config.put(JsonTagsZ.IGNORE_ORDER_RECOMMENDATION_REASONS,
            loadIgnoreOrderRecommendationReasons(
                confJson.getJSONObject(JsonTagsZ.IGNORE_ORDER_RECOMMENDATION_REASONS)));
      } catch (JSONException e) {
        // do nothing
      }
      try {
        config.put(JsonTagsZ.SMS, loadSMSParameters(confJson.getJSONObject(JsonTagsZ.SMS)));
      } catch (JSONException e) {
        // do nothing
      }
      try {
        config.put(JsonTagsZ.APP_UPGRADE,
            loadAppUgradeParameters(confJson.getJSONObject(JsonTagsZ.APP_UPGRADE)));
      } catch (JSONException e) {
        // do nothing
      }
    } catch (JSONException e) {
      // do nothing
    }
  }


}
/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.logistimo.proto;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.logistimo.proto.utils.StringTokenizer;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

/**
 * Utility methods for constructing JSON objects.
 *
 * @author Arun
 */
public class JsonUtil {

  // Add material data to JSON object
  public static void addMaterialData(JSONObject json, Vector materials) throws JSONException {
    // if ( materials == null || materials.isEmpty() )
    //  return;
    ///throw new JSONException( "No material data found" );
    if (materials != null) {
      // Init. array
      JSONArray array = new JSONArray();
      if (!materials.isEmpty()) {
        Enumeration en = materials.elements();
        while (en.hasMoreElements()) {
          Hashtable material = (Hashtable) en.nextElement();
          // Init. JSON object per material
          JSONObject m = new JSONObject();
          m.put(JsonTagsZ.MATERIAL_ID, (String) material.get(JsonTagsZ.MATERIAL_ID));
          m.put(JsonTagsZ.QUANTITY, (String) material.get(JsonTagsZ.QUANTITY));
          String value = null;
          if ((value = (String) material.get(JsonTagsZ.TIMESTAMP)) != null) {
            m.put(JsonTagsZ.TIMESTAMP, value);
          }
          if ((value = (String) material.get(JsonTagsZ.NAME)) != null) {
            m.put(JsonTagsZ.NAME, value);
          }
          if ((value = (String) material.get(JsonTagsZ.SHORT_MATERIAL_ID)) != null) {
            m.put(JsonTagsZ.SHORT_MATERIAL_ID, value);
          }
          if ((value = (String) material.get(JsonTagsZ.INFO)) != null) {
            m.put(JsonTagsZ.INFO, value);
          }
          if ((value = (String) material.get(JsonTagsZ.MANUFACTURER_PRICE)) != null) {
            m.put(JsonTagsZ.MANUFACTURER_PRICE, value);
          }
          if ((value = (String) material.get(JsonTagsZ.RETAILER_PRICE)) != null) {
            m.put(JsonTagsZ.RETAILER_PRICE, value);
          }
          if ((value = (String) material.get(JsonTagsZ.TAX)) != null) {
            m.put(JsonTagsZ.TAX, value);
          }
          if ((value = (String) material.get(JsonTagsZ.CURRENCY)) != null) {
            m.put(JsonTagsZ.CURRENCY, value);
          }
          if ((value = (String) material.get(JsonTagsZ.ORDER_STATUS)) != null) {
            m.put(JsonTagsZ.ORDER_STATUS, value);
          }
          if ((value = (String) material.get(JsonTagsZ.MESSAGE)) != null) {
            m.put(JsonTagsZ.MESSAGE, value);
          }
          if ((value = (String) material.get(JsonTagsZ.REASON)) != null) {
            m.put(JsonTagsZ.REASON, value);
          }
          if ((value = (String) material.get(JsonTagsZ.TAGS)) != null && !value.equals("")) {
            m.put(JsonTagsZ.TAGS, value);
          }
          // Optimization parmameters, if any
          if ((value = (String) material.get(JsonTagsZ.CR_DAILY)) != null && !value.equals("")) {
            m.put(JsonTagsZ.CR_DAILY, value);
          }
          if ((value = (String) material.get(JsonTagsZ.CR_WEEKLY)) != null && !value.equals("")) {
            m.put(JsonTagsZ.CR_WEEKLY, value);
          }
          if ((value = (String) material.get(JsonTagsZ.CR_MONTHLY)) != null && !value.equals("")) {
            m.put(JsonTagsZ.CR_MONTHLY, value);
          }
          if ((value = (String) material.get(JsonTagsZ.DEMAND_FORECAST)) != null && !value
              .equals("")) {
            m.put(JsonTagsZ.DEMAND_FORECAST, value);
          }
          if ((value = (String) material.get(JsonTagsZ.ORDER_PERIODICITY)) != null && !value
              .equals("")) {
            m.put(JsonTagsZ.ORDER_PERIODICITY, value);
          }
          if ((value = (String) material.get(JsonTagsZ.EOQ)) != null && !value.equals("")) {
            m.put(JsonTagsZ.EOQ, value);
          }
          if ((value = (String) material.get(JsonTagsZ.DISCOUNT)) != null && !value
              .equals("")) // discount on order items
          {
            m.put(JsonTagsZ.DISCOUNT, value);
          }
          if ((value = (String) material.get(JsonTagsZ.DATA_TYPE)) != null && !value
              .equals("")) // discount on order items
          {
            m.put(JsonTagsZ.DATA_TYPE, value);
          }
          if ((value = (String) material.get(JsonTagsZ.MIN)) != null) {
            m.put(JsonTagsZ.MIN, value);
          }
          if ((value = (String) material.get(JsonTagsZ.MAX)) != null) {
            m.put(JsonTagsZ.MAX, value);
          }
          if ((value = (String) material.get(JsonTagsZ.MINDUR)) != null) {
            m.put(JsonTagsZ.MINDUR, value);
          }
          if ((value = (String) material.get(JsonTagsZ.MAXDUR)) != null) {
            m.put(JsonTagsZ.MAXDUR, value);
          }
          if ((value = (String) material.get(JsonTagsZ.MMDUR)) != null) {
            m.put(JsonTagsZ.MMDUR, value);
          }
          if ((value = (String) material.get(JsonTagsZ.BATCH_ENABLED)) != null) {
            m.put(JsonTagsZ.BATCH_ENABLED, value);
          }
          if ((value = (String) material.get(JsonTagsZ.BATCH_EXPIRY)) != null) {
            m.put(JsonTagsZ.BATCH_EXPIRY, value);
          }
          if ((value = (String) material.get(JsonTagsZ.BATCH_ID)) != null) {
            m.put(JsonTagsZ.BATCH_ID, value);
          }
          if ((value = (String) material.get(JsonTagsZ.BATCH_MANUFACTUER_NAME)) != null) {
            m.put(JsonTagsZ.BATCH_MANUFACTUER_NAME, value);
          }
          if ((value = (String) material.get(JsonTagsZ.BATCH_MANUFACTURED_DATE)) != null) {
            m.put(JsonTagsZ.BATCH_MANUFACTURED_DATE, value);
          }
          if ((value = (String) material.get(JsonTagsZ.CUSTOM_MATERIALID)) != null) {
            m.put(JsonTagsZ.CUSTOM_MATERIALID, value);
          }
          // Inventory items
          if ((value = (String) material.get(JsonTagsZ.INVENTORY_ITEMS_ABNORMAL)) != null) {
            m.put(JsonTagsZ.INVENTORY_ITEMS_ABNORMAL, value);
          }
          // Is temperature sensitive
          if ((value = (String) material.get(JsonTagsZ.IS_TEMPERATURE_SENSITIVE)) != null) {
            m.put(JsonTagsZ.IS_TEMPERATURE_SENSITIVE, value);
          }
          // Abnormal temp., if any (for a single device only, at this time)
          if ((value = (String) material.get(JsonTagsZ.TEMPERATURE)) != null) {
            m.put(JsonTagsZ.TEMPERATURE, value);
          }
          // Add batches, if any
          Vector batches = (Vector) material.get(JsonTagsZ.BATCHES);
          if (batches != null && !batches.isEmpty()) {
            addBatchData(m, batches);
          }
          // Add expired batches, if any
          Vector expiredBatches = (Vector) material.get(JsonTagsZ.EXPIRED_NONZERO_BATCHES);
          if (expiredBatches != null && !expiredBatches.isEmpty()) {
            addExpiredBatchData(m, expiredBatches);
          }
          // Add to array
          array.put(m);
        }
      }
      // Add to container JSON
      json.put(JsonTagsZ.MATERIALS, array);
    }
  }


  public static Vector getMaterialData(JSONObject json) throws JSONException {
    return getMaterialData(json, null);
  }

  // Load material data from JSON and return a vector of hashtable
  public static Vector getMaterialData(JSONObject json, Tags tags) throws JSONException {
    Vector materials = new Vector();
    // Get the material array
    JSONArray array = null;
    try {
      array = (JSONArray) json.get(JsonTagsZ.MATERIALS);
    } catch (JSONException e) {
      return materials; // return empty vector, in case of no materials
    }
    for (int i = 0; i < array.length(); i++) {
      JSONObject m = (JSONObject) array.get(i);
      Hashtable material = new Hashtable();
      String mid = (String) m.get(JsonTagsZ.MATERIAL_ID);
      material.put(JsonTagsZ.MATERIAL_ID, mid);
      material.put(JsonTagsZ.QUANTITY, (String) m.get(JsonTagsZ.QUANTITY));
      // Get optional timestamp
      try {
        material.put(JsonTagsZ.TIMESTAMP, (String) m.get(JsonTagsZ.TIMESTAMP));
      } catch (JSONException e) {
        // do nothing
      }
      // Get the name (optional only for stock refresh)
      try {
        material.put(JsonTagsZ.NAME, (String) m.get(JsonTagsZ.NAME));
      } catch (JSONException e) {
        // do nothing
      }
      // Get the short material id (optional )
      try {
        material.put(JsonTagsZ.SHORT_MATERIAL_ID, (String) m.get(JsonTagsZ.SHORT_MATERIAL_ID));
      } catch (JSONException e) {
        // do nothing
      }
      // Get optional info.
      try {
        material.put(JsonTagsZ.INFO, (String) m.get(JsonTagsZ.INFO));
      } catch (JSONException e) {
        // do nothing
      }
      // Get optional manufacturer price
      try {
        material.put(JsonTagsZ.MANUFACTURER_PRICE, (String) m.get(JsonTagsZ.MANUFACTURER_PRICE));
      } catch (JSONException e) {
        // do nothing
      }
      // Get optinal retailer price
      try {
        material.put(JsonTagsZ.RETAILER_PRICE, (String) m.get(JsonTagsZ.RETAILER_PRICE));
      } catch (JSONException e) {
        // do nothing
      }
      // Get optinal tax
      try {
        material.put(JsonTagsZ.TAX, (String) m.get(JsonTagsZ.TAX));
      } catch (JSONException e) {
        // do nothing
      }
      // Get optinal currency of price
      try {
        material.put(JsonTagsZ.CURRENCY, (String) m.get(JsonTagsZ.CURRENCY));
      } catch (JSONException e) {
        // do nothing
      }
      // Get optinal order-status (in case of order responses only)
      try {
        material.put(JsonTagsZ.ORDER_STATUS, (String) m.get(JsonTagsZ.ORDER_STATUS));
      } catch (JSONException e) {
        // do nothing
      }
      // Get optinal message
      try {
        material.put(JsonTagsZ.MESSAGE, (String) m.get(JsonTagsZ.MESSAGE));
      } catch (JSONException e) {
        // do nothing
      }
      // Get optinal reason (for ignoring recommended order qty)
      try {
        material.put(JsonTagsZ.REASON, (String) m.get(JsonTagsZ.REASON));
      } catch (JSONException e) {
        // do nothing
      }
      // Get tags, if any
      String tagsCSV = null;
      try {
        tagsCSV = (String) m.get(JsonTagsZ.TAGS);
        if (tagsCSV.trim().equals("")) {
          tagsCSV = (tags != null ? tags.NOTAGS : "");
        }
      } catch (JSONException e) {
        // This means no tags were present. Add a NO_TAGS tag
        tagsCSV = (tags != null ? tags.NOTAGS : "");
      }
      material.put(JsonTagsZ.TAGS, tagsCSV);
      if (tags != null) {
        updateTagMap(tags, tagsCSV, material);
      }
      // Optimization parameters, if any
      try {
        material.put(JsonTagsZ.CR_DAILY, m.getString(JsonTagsZ.CR_DAILY));
      } catch (JSONException e) {
        // do nothing
      }
      try {
        material.put(JsonTagsZ.CR_WEEKLY, m.getString(JsonTagsZ.CR_WEEKLY));
      } catch (JSONException e) {
        // do nothing
      }
      try {
        material.put(JsonTagsZ.CR_MONTHLY, m.getString(JsonTagsZ.CR_MONTHLY));
      } catch (JSONException e) {
        // do nothing
      }
      try {
        material.put(JsonTagsZ.DEMAND_FORECAST, m.getString(JsonTagsZ.DEMAND_FORECAST));
      } catch (JSONException e) {
        // do nothing
      }
      try {
        material.put(JsonTagsZ.ORDER_PERIODICITY, m.getString(JsonTagsZ.ORDER_PERIODICITY));
      } catch (JSONException e) {
        // do nothing
      }
      try {
        material.put(JsonTagsZ.EOQ, m.getString(JsonTagsZ.EOQ));
      } catch (JSONException e) {
        // do nothing
      }
      try {
        material.put(JsonTagsZ.DISCOUNT, m.getString(JsonTagsZ.DISCOUNT));
      } catch (JSONException e) {
        // do nothing
      }
      try {
        material.put(JsonTagsZ.DATA_TYPE, m.getString(JsonTagsZ.DATA_TYPE));
      } catch (JSONException e) {
        // do nothing
      }
      try {
        material.put(JsonTagsZ.MIN, m.getString(JsonTagsZ.MIN));
      } catch (JSONException e) {
        // do nothing
      }
      try {
        material.put(JsonTagsZ.MAX, m.getString(JsonTagsZ.MAX));
      } catch (JSONException e) {
        // do nothing
      }
      try {
        material.put(JsonTagsZ.MINDUR, m.getString(JsonTagsZ.MINDUR));
      } catch (JSONException e) {
        // do nothing
      }
      try {
        material.put(JsonTagsZ.MAXDUR, m.getString(JsonTagsZ.MAXDUR));
      } catch (JSONException e) {
        // do nothing
      }
      try {
        material.put(JsonTagsZ.MMDUR, m.getString(JsonTagsZ.MMDUR));
      } catch (JSONException e) {
        // do nothing
      }
      // Batch details
      try {
        material.put(JsonTagsZ.BATCH_ENABLED, m.getString(JsonTagsZ.BATCH_ENABLED));
      } catch (JSONException e) {
        // do nothing
      }
      try {
        material.put(JsonTagsZ.BATCH_EXPIRY, m.getString(JsonTagsZ.BATCH_EXPIRY));
      } catch (JSONException e) {
        // do nothing
      }
      try {
        material.put(JsonTagsZ.BATCH_ID, m.getString(JsonTagsZ.BATCH_ID));
      } catch (JSONException e) {
        // do nothing
      }
      try {
        material
            .put(JsonTagsZ.BATCH_MANUFACTUER_NAME, m.getString(JsonTagsZ.BATCH_MANUFACTUER_NAME));
      } catch (JSONException e) {
        // do nothing
      }
      try {
        material
            .put(JsonTagsZ.BATCH_MANUFACTURED_DATE, m.getString(JsonTagsZ.BATCH_MANUFACTURED_DATE));
      } catch (JSONException e) {
        // do nothing
      }
      try {
        material.put(JsonTagsZ.CUSTOM_MATERIALID, m.getString(JsonTagsZ.CUSTOM_MATERIALID));
      } catch (JSONException e) {
        // do nothing
      }
      // Inventory items details
      try {
        material.put(JsonTagsZ.INVENTORY_ITEMS_ABNORMAL,
            m.getString(JsonTagsZ.INVENTORY_ITEMS_ABNORMAL));
      } catch (JSONException e) {
        // do nothing
      }
      // Is temperature sensitive
      try {
        material.put(JsonTagsZ.IS_TEMPERATURE_SENSITIVE,
            m.getString(JsonTagsZ.IS_TEMPERATURE_SENSITIVE));
      } catch (JSONException e) {
        // do nothing
      }
      // Temperature, if any
      try {
        material.put(JsonTagsZ.TEMPERATURE, m.getString(JsonTagsZ.TEMPERATURE));
      } catch (JSONException e) {
        // do nothing
      }
      // Get batch information for material
      try {
        Vector batches = JsonUtil.getBatchData(m);
        if (batches != null && !batches.isEmpty()) {
          material.put(JsonTagsZ.BATCHES, batches);
        }
      } catch (JSONException e) {
        // do nothing
      }
      // Get expired batch information for material
      try {
        Vector expiredBatches = JsonUtil.getExpiredBatchData(m);
        if (expiredBatches != null && !expiredBatches.isEmpty()) {
          material.put(JsonTagsZ.EXPIRED_NONZERO_BATCHES, expiredBatches);
        }
      } catch (JSONException e) {
        // do nothing
      }
      // Add to vector
      materials.addElement(material);
    }
    // Check if multiple tags are present, or only NO_TAGS; clear tags if only NO_TAGS
    if (tags != null && tags.hasOnlyNOTAGStag()) {
      tags.clear();
    }

    return materials;
  }


  // Get batch information for materials  (vector of hashtable)
  public static Vector getBatchData(JSONObject materialJson) throws JSONException {
    JSONArray batchesJson = materialJson.getJSONArray(JsonTagsZ.BATCHES);
    Vector batches = new Vector();
    for (int i = 0; i < batchesJson.length(); i++) {
      JSONObject batchJson = batchesJson.getJSONObject(i);
      Hashtable batch = new Hashtable();
      batch.put(JsonTagsZ.BATCH_ID, batchJson.getString(JsonTagsZ.BATCH_ID));
      batch.put(JsonTagsZ.BATCH_EXPIRY, batchJson.getString(JsonTagsZ.BATCH_EXPIRY));
      batch.put(JsonTagsZ.BATCH_MANUFACTUER_NAME,
          batchJson.getString(JsonTagsZ.BATCH_MANUFACTUER_NAME));
      try {
        batch.put(JsonTagsZ.BATCH_MANUFACTURED_DATE,
            batchJson.getString(JsonTagsZ.BATCH_MANUFACTURED_DATE));
      } catch (Exception e) {
        // ignore
      }
      batch.put(JsonTagsZ.QUANTITY, batchJson.getString(JsonTagsZ.QUANTITY));
      batch.put(JsonTagsZ.TIMESTAMP, batchJson.getString(JsonTagsZ.TIMESTAMP));

      batches.addElement(batch);
    }
    return batches;
  }

  // Add batch data
  public static void addBatchData(JSONObject materialJSON, Vector batches) throws JSONException {
    if (batches == null || batches.isEmpty()) {
      return;
    }
    JSONArray batchesJson = new JSONArray();
    Enumeration en = batches.elements();
    while (en.hasMoreElements()) {
      Hashtable batch = (Hashtable) en.nextElement();
      JSONObject batchJson = new JSONObject();
      String value = null;
      if ((value = (String) batch.get(JsonTagsZ.BATCH_ID)) != null && !value.equals("")) {
        batchJson.put(JsonTagsZ.BATCH_ID, batch.get(JsonTagsZ.BATCH_ID));
      }
      if ((value = (String) batch.get(JsonTagsZ.BATCH_EXPIRY)) != null && !value.equals("")) {
        batchJson.put(JsonTagsZ.BATCH_EXPIRY, batch.get(JsonTagsZ.BATCH_EXPIRY));
      }
      if ((value = (String) batch.get(JsonTagsZ.BATCH_MANUFACTUER_NAME)) != null && !value
          .equals("")) {
        batchJson.put(JsonTagsZ.BATCH_MANUFACTUER_NAME, value);
      }
      if ((value = (String) batch.get(JsonTagsZ.BATCH_MANUFACTURED_DATE)) != null && !value
          .equals("")) {
        batchJson
            .put(JsonTagsZ.BATCH_MANUFACTURED_DATE, batch.get(JsonTagsZ.BATCH_MANUFACTURED_DATE));
      }
      if ((value = (String) batch.get(JsonTagsZ.QUANTITY)) != null && !value.equals("")) {
        batchJson.put(JsonTagsZ.QUANTITY, batch.get(JsonTagsZ.QUANTITY));
      }
      if ((value = (String) batch.get(JsonTagsZ.TIMESTAMP)) != null && !value.equals("")) {
        batchJson.put(JsonTagsZ.TIMESTAMP, batch.get(JsonTagsZ.TIMESTAMP));
      }

      batchesJson.put(batchJson);
    }
    materialJSON.put(JsonTagsZ.BATCHES, batchesJson);
  }

  // Get batch information for materials  (vector of hashtable)
  public static Vector getExpiredBatchData(JSONObject materialJson) throws JSONException {
    JSONArray batchesJson = materialJson.getJSONArray(JsonTagsZ.EXPIRED_NONZERO_BATCHES);
    Vector expiredBatches = new Vector();
    for (int i = 0; i < batchesJson.length(); i++) {
      JSONObject batchJson = batchesJson.getJSONObject(i);
      Hashtable batch = new Hashtable();
      batch.put(JsonTagsZ.BATCH_ID, batchJson.getString(JsonTagsZ.BATCH_ID));
      batch.put(JsonTagsZ.BATCH_EXPIRY, batchJson.getString(JsonTagsZ.BATCH_EXPIRY));
      batch.put(JsonTagsZ.BATCH_MANUFACTUER_NAME,
          batchJson.getString(JsonTagsZ.BATCH_MANUFACTUER_NAME));
      try {
        batch.put(JsonTagsZ.BATCH_MANUFACTURED_DATE,
            batchJson.getString(JsonTagsZ.BATCH_MANUFACTURED_DATE));
      } catch (Exception e) {
        // ignore
      }
      batch.put(JsonTagsZ.QUANTITY, batchJson.getString(JsonTagsZ.QUANTITY));
      batch.put(JsonTagsZ.TIMESTAMP, batchJson.getString(JsonTagsZ.TIMESTAMP));

      expiredBatches.addElement(batch);
    }
    return expiredBatches;
  }


  // Add batch data
  public static void addExpiredBatchData(JSONObject materialJSON, Vector expiredBatches)
      throws JSONException {
    if (expiredBatches == null || expiredBatches.isEmpty()) {
      return;
    }
    JSONArray batchesJson = new JSONArray();
    Enumeration en = expiredBatches.elements();
    while (en.hasMoreElements()) {
      Hashtable batch = (Hashtable) en.nextElement();
      JSONObject batchJson = new JSONObject();
      String value = null;
      if ((value = (String) batch.get(JsonTagsZ.BATCH_ID)) != null && !value.equals("")) {
        batchJson.put(JsonTagsZ.BATCH_ID, batch.get(JsonTagsZ.BATCH_ID));
      }
      if ((value = (String) batch.get(JsonTagsZ.BATCH_EXPIRY)) != null && !value.equals("")) {
        batchJson.put(JsonTagsZ.BATCH_EXPIRY, batch.get(JsonTagsZ.BATCH_EXPIRY));
      }
      if ((value = (String) batch.get(JsonTagsZ.BATCH_MANUFACTUER_NAME)) != null && !value
          .equals("")) {
        batchJson.put(JsonTagsZ.BATCH_MANUFACTUER_NAME, value);
      }
      if ((value = (String) batch.get(JsonTagsZ.BATCH_MANUFACTURED_DATE)) != null && !value
          .equals("")) {
        batchJson
            .put(JsonTagsZ.BATCH_MANUFACTURED_DATE, batch.get(JsonTagsZ.BATCH_MANUFACTURED_DATE));
      }
      if ((value = (String) batch.get(JsonTagsZ.QUANTITY)) != null && !value.equals("")) {
        batchJson.put(JsonTagsZ.QUANTITY, batch.get(JsonTagsZ.QUANTITY));
      }
      if ((value = (String) batch.get(JsonTagsZ.TIMESTAMP)) != null && !value.equals("")) {
        batchJson.put(JsonTagsZ.TIMESTAMP, batch.get(JsonTagsZ.TIMESTAMP));
      }

      batchesJson.put(batchJson);
    }
    materialJSON.put(JsonTagsZ.EXPIRED_NONZERO_BATCHES, batchesJson);
  }

  // Add error data to JSON in new format
  public static void addErrorData(JSONObject json, Vector errors) throws JSONException {
    if (errors == null || errors.isEmpty()) {
      throw new JSONException("No error data found");
    }
    JSONArray array = new JSONArray();
    Enumeration en = errors.elements();
    while (en.hasMoreElements()) {
      Hashtable error = (Hashtable) en.nextElement();
      // Form error JSON object
      JSONObject e = new JSONObject();
      e.put(JsonTagsZ.MATERIAL_ID, (String) error.get(JsonTagsZ.MATERIAL_ID));
      e.put(JsonTagsZ.MESSAGE, (String) error.get(JsonTagsZ.MESSAGE));
      String value = null;
      if ((value = (String) error.get(JsonTagsZ.QUANTITY)) != null && !value.equals("")) {
        e.put(JsonTagsZ.QUANTITY, (String) error.get(JsonTagsZ.QUANTITY));
      }
      if ((value = (String) error.get(JsonTagsZ.ERROR_CODE)) != null && !value.equals("")) {
        e.put(JsonTagsZ.ERROR_CODE, (String) error.get(JsonTagsZ.ERROR_CODE));
      }
      if ((value = (String) error.get(JsonTagsZ.TIMESTAMP)) != null && !value.equals("")) {
        e.put(JsonTagsZ.TIMESTAMP, (String) error.get(JsonTagsZ.TIMESTAMP));
      }
      // Add batches, if any
      Vector batches = (Vector) error.get(JsonTagsZ.BATCHES);
      if (batches != null && !batches.isEmpty()) {
        addBatchErrorData(e, batches);
      }
      // Add to JSON array
      array.put(e);
    }
    // Add to json object
    json.put(JsonTagsZ.ERRORS, array);
  }

  // Load error data in new protocol format
  public static Vector getErrorData(JSONObject json) throws JSONException {
    Vector errors = new Vector();
    JSONArray array = (JSONArray) json.get(JsonTagsZ.ERRORS);
    for (int i = 0; i < array.length(); i++) {
      JSONObject e = (JSONObject) array.get(i);
      Hashtable error = new Hashtable();
      error.put(JsonTagsZ.MATERIAL_ID, (String) e.get(JsonTagsZ.MATERIAL_ID));
      error.put(JsonTagsZ.MESSAGE, (String) e.get(JsonTagsZ.MESSAGE));
      String value = null;
      if ((value = (String) e.get(JsonTagsZ.QUANTITY)) != null && !value.equals("")) {
        error.put(JsonTagsZ.QUANTITY, (String) e.get(JsonTagsZ.QUANTITY));
      }
      if ((value = (String) e.get(JsonTagsZ.ERROR_CODE)) != null && !value.equals("")) {
        error.put(JsonTagsZ.ERROR_CODE, (String) e.get(JsonTagsZ.ERROR_CODE));
      }
      if ((value = (String) e.get(JsonTagsZ.TIMESTAMP)) != null && !value.equals("")) {
        error.put(JsonTagsZ.TIMESTAMP, (String) e.get(JsonTagsZ.TIMESTAMP));
      }
      // Get batch information for material
      try {
        Vector batches = JsonUtil.getBatchErrorData(e);
        if (batches != null && !batches.isEmpty()) {
          error.put(JsonTagsZ.BATCHES, batches);
        }
      } catch (JSONException ex) {
        // do nothing
      }
      // Add to vector
      errors.addElement(error);
    }

    return errors;
  }


  // Get  batch information for materials with partial errors  (vector of hashtable)
  public static Vector getBatchErrorData(JSONObject errorJson) throws JSONException {
    JSONArray batchesJson = errorJson.getJSONArray(JsonTagsZ.BATCHES);
    Vector batches = new Vector();
    for (int i = 0; i < batchesJson.length(); i++) {
      JSONObject batchJson = batchesJson.getJSONObject(i);
      Hashtable batch = new Hashtable();
      batch.put(JsonTagsZ.BATCH_ID, batchJson.getString(JsonTagsZ.BATCH_ID));
      batch.put(JsonTagsZ.QUANTITY, batchJson.getString(JsonTagsZ.QUANTITY));
      batch.put(JsonTagsZ.BATCH_EXPIRY, batchJson.getString(JsonTagsZ.BATCH_EXPIRY));
      batch.put(JsonTagsZ.BATCH_MANUFACTUER_NAME,
          batchJson.getString(JsonTagsZ.BATCH_MANUFACTUER_NAME));
      try {
        batch.put(JsonTagsZ.BATCH_MANUFACTURED_DATE,
            batchJson.getString(JsonTagsZ.BATCH_MANUFACTURED_DATE));
      } catch (Exception e) {
        // ignore
      }
      try {
        batch.put(JsonTagsZ.MESSAGE, batchJson.getString(JsonTagsZ.MESSAGE));
      } catch (Exception e) {
        // ignore
      }
      try {
        batch.put(JsonTagsZ.ERROR_CODE, batchJson.getString(JsonTagsZ.ERROR_CODE));
      } catch (Exception e) {
        // ignore
      }
      batch.put(JsonTagsZ.TIMESTAMP, batchJson.getString(JsonTagsZ.TIMESTAMP));

      batches.addElement(batch);
    }
    return batches;
  }

  // Add batch data
  public static void addBatchErrorData(JSONObject errorJSON, Vector batches) throws JSONException {
    if (batches == null || batches.isEmpty()) {
      return;
    }
    JSONArray batchesJson = new JSONArray();
    Enumeration en = batches.elements();
    while (en.hasMoreElements()) {
      Hashtable batch = (Hashtable) en.nextElement();
      JSONObject batchJson = new JSONObject();
      String value = null;
      if ((value = (String) batch.get(JsonTagsZ.BATCH_ID)) != null && !value.equals("")) {
        batchJson.put(JsonTagsZ.BATCH_ID, batch.get(JsonTagsZ.BATCH_ID));
      }
      if ((value = (String) batch.get(JsonTagsZ.QUANTITY)) != null && !value.equals("")) {
        batchJson.put(JsonTagsZ.QUANTITY, batch.get(JsonTagsZ.QUANTITY));
      }
      if ((value = (String) batch.get(JsonTagsZ.BATCH_EXPIRY)) != null && !value.equals("")) {
        batchJson.put(JsonTagsZ.BATCH_EXPIRY, batch.get(JsonTagsZ.BATCH_EXPIRY));
      }
      if ((value = (String) batch.get(JsonTagsZ.BATCH_MANUFACTUER_NAME)) != null && !value
          .equals("")) {
        batchJson
            .put(JsonTagsZ.BATCH_MANUFACTUER_NAME, batch.get(JsonTagsZ.BATCH_MANUFACTUER_NAME));
      }
      if ((value = (String) batch.get(JsonTagsZ.BATCH_MANUFACTURED_DATE)) != null && !value
          .equals("")) {
        batchJson
            .put(JsonTagsZ.BATCH_MANUFACTURED_DATE, batch.get(JsonTagsZ.BATCH_MANUFACTURED_DATE));
      }
      if ((value = (String) batch.get(JsonTagsZ.TIMESTAMP)) != null && !value.equals("")) {
        batchJson.put(JsonTagsZ.TIMESTAMP, batch.get(JsonTagsZ.TIMESTAMP));
      }
      if ((value = (String) batch.get(JsonTagsZ.MESSAGE)) != null && !value.equals("")) {
        batchJson.put(JsonTagsZ.MESSAGE, batch.get(JsonTagsZ.MESSAGE));
      }
      if ((value = (String) batch.get(JsonTagsZ.ERROR_CODE)) != null && !value.equals("")) {
        batchJson.put(JsonTagsZ.ERROR_CODE, batch.get(JsonTagsZ.ERROR_CODE));
      }
      batchesJson.put(batchJson);
    }
    errorJSON.put(JsonTagsZ.BATCHES, batchesJson);
  }

  // Add vendor data to kiosk object
  public static void addRelatedKioskData(JSONObject json, Vector relatedKiosks, String type)
      throws JSONException {
    if (relatedKiosks != null) {
      JSONArray relatedKiosksJson = new JSONArray();
      // If relatedKiosks is null or empty, send an empty JSONArray in the response.
      if (!relatedKiosks.isEmpty()) {
        Enumeration en = relatedKiosks.elements();
        while (en.hasMoreElements()) {
          Hashtable v = (Hashtable) en.nextElement();
          JSONObject relatedKioskJson = new JSONObject();
          relatedKioskJson.put(JsonTagsZ.KIOSK_ID, (String) v.get(JsonTagsZ.KIOSK_ID));
          relatedKioskJson.put(JsonTagsZ.NAME, (String) v.get(JsonTagsZ.NAME));
          relatedKioskJson.put(JsonTagsZ.CITY, (String) v.get(JsonTagsZ.CITY));
          String value = null;
          if ((value = (String) v.get(JsonTagsZ.STATE)) != null && value.length() > 0) {
            relatedKioskJson.put(JsonTagsZ.STATE, value);
          }
          if ((value = (String) v.get(JsonTagsZ.DISTRICT)) != null && value.length() > 0) {
            relatedKioskJson.put(JsonTagsZ.DISTRICT, value);
          }
          if ((value = (String) v.get(JsonTagsZ.TALUK)) != null && value.length() > 0) {
            relatedKioskJson.put(JsonTagsZ.TALUK, value);
          }
          if ((value = (String) v.get(JsonTagsZ.STREET_ADDRESS)) != null && value.length() > 0) {
            relatedKioskJson.put(JsonTagsZ.STREET_ADDRESS, value);
          }
          if ((value = (String) v.get(JsonTagsZ.PINCODE)) != null && value.length() > 0) {
            relatedKioskJson.put(JsonTagsZ.PINCODE, value);
          }
          if ((value = (String) v.get(JsonTagsZ.PAYABLE)) != null && value.length() > 0) {
            relatedKioskJson.put(JsonTagsZ.PAYABLE, value);
          }
          if ((value = (String) v.get(JsonTagsZ.CREDIT_LIMIT)) != null && value.length() > 0) {
            relatedKioskJson.put(JsonTagsZ.CREDIT_LIMIT, value);
          }
          if ((value = (String) v.get(JsonTagsZ.ROUTE_INDEX)) != null) {
            relatedKioskJson.put(JsonTagsZ.ROUTE_INDEX, value);
          }
          if ((value = (String) v.get(JsonTagsZ.ROUTE_TAG)) != null) {
            relatedKioskJson.put(JsonTagsZ.ROUTE_TAG, value);
          }
          if ((value = (String) v.get(JsonTagsZ.LATITUDE)) != null) {
            relatedKioskJson.put(JsonTagsZ.LATITUDE, value);
          }
          if ((value = (String) v.get(JsonTagsZ.LONGITUDE)) != null) {
            relatedKioskJson.put(JsonTagsZ.LONGITUDE, value);
          }
          if ((value = (String) v.get(JsonTagsZ.GEO_ACCURACY)) != null) {
            relatedKioskJson.put(JsonTagsZ.GEO_ACCURACY, value);
          }
          if ((value = (String) v.get(JsonTagsZ.GEO_ERROR_CODE)) != null) {
            relatedKioskJson.put(JsonTagsZ.GEO_ERROR_CODE, value);
          }
          if ((value = (String) v.get(JsonTagsZ.CUSTOM_KIOSKID)) != null) {
            relatedKioskJson.put(JsonTagsZ.CUSTOM_KIOSKID, value);
          }
          if ((value = (String) v.get(JsonTagsZ.DISABLE_BATCH_MGMT)) != null) {
            relatedKioskJson.put(JsonTagsZ.DISABLE_BATCH_MGMT, value);
          }
          // Add users, if any
          Vector users = (Vector) v.get(JsonTagsZ.USERS);
          if (users != null && !users.isEmpty()) {
            addKioskUsers(relatedKioskJson, users);
          }

          // Add to array
          relatedKiosksJson.put(relatedKioskJson);
        }
      }
      // Update kiosk object
      json.put(type, relatedKiosksJson);
    }
  }

  // Get vendor data from a kiosk JSON object (Vector of Hashtable of vendor metadata is returned)
  public static Vector getRelatedKioskData(JSONObject kioskJson, String type) throws JSONException {
    JSONArray array = (JSONArray) kioskJson.get(type);
    Vector kiosks = new Vector();
    for (int i = 0; i < array.length(); i++) {
      JSONObject relatedKioskJson = (JSONObject) array.get(i);
      Hashtable k = new Hashtable();
      k.put(JsonTagsZ.KIOSK_ID, (String) relatedKioskJson.get(JsonTagsZ.KIOSK_ID));
      k.put(JsonTagsZ.NAME, (String) relatedKioskJson.get(JsonTagsZ.NAME));
      k.put(JsonTagsZ.CITY, (String) relatedKioskJson.get(JsonTagsZ.CITY));
      try {
        k.put(JsonTagsZ.STATE, (String) relatedKioskJson.get(JsonTagsZ.STATE));
      } catch (Exception e) {
        // ignore
      }
      try {
        k.put(JsonTagsZ.DISTRICT, (String) relatedKioskJson.get(JsonTagsZ.DISTRICT));
      } catch (Exception e) {
        // ignore
      }
      try {
        k.put(JsonTagsZ.TALUK, (String) relatedKioskJson.get(JsonTagsZ.TALUK));
      } catch (Exception e) {
        // ignore
      }
      try {
        k.put(JsonTagsZ.STREET_ADDRESS, (String) relatedKioskJson.get(JsonTagsZ.STREET_ADDRESS));
      } catch (Exception e) {
        // ignore
      }
      try {
        k.put(JsonTagsZ.PINCODE, (String) relatedKioskJson.get(JsonTagsZ.PINCODE));
      } catch (Exception e) {
        // ignore
      }
      try {
        k.put(JsonTagsZ.PAYABLE, (String) relatedKioskJson.get(JsonTagsZ.PAYABLE));
      } catch (Exception e) {
        // ignore
      }
      try {
        k.put(JsonTagsZ.CREDIT_LIMIT, (String) relatedKioskJson.get(JsonTagsZ.CREDIT_LIMIT));
      } catch (Exception e) {
        // ignore
      }
      // Route index
      try {
        k.put(JsonTagsZ.ROUTE_INDEX, relatedKioskJson.getString(JsonTagsZ.ROUTE_INDEX));
      } catch (JSONException e) {
        // do nothing
      }
      // Route tag
      try {
        k.put(JsonTagsZ.ROUTE_TAG, relatedKioskJson.getString(JsonTagsZ.ROUTE_TAG));
      } catch (JSONException e) {
        // do nothing
      }
      // Latitude
      try {
        k.put(JsonTagsZ.LATITUDE, relatedKioskJson.getString(JsonTagsZ.LATITUDE));
      } catch (JSONException e) {
        // do nothing
      }
      // Latitude
      try {
        k.put(JsonTagsZ.LONGITUDE, relatedKioskJson.getString(JsonTagsZ.LONGITUDE));
      } catch (JSONException e) {
        // do nothing
      }
      // Geo-accuracy
      try {
        k.put(JsonTagsZ.GEO_ACCURACY, relatedKioskJson.getString(JsonTagsZ.GEO_ACCURACY));
      } catch (JSONException e) {
        // do nothing
      }
      // Geo-error
      try {
        k.put(JsonTagsZ.GEO_ERROR_CODE, relatedKioskJson.getString(JsonTagsZ.GEO_ERROR_CODE));
      } catch (JSONException e) {
        // do nothing
      }
      // Custom kiosk ID
      try {
        k.put(JsonTagsZ.CUSTOM_KIOSKID, relatedKioskJson.getString(JsonTagsZ.CUSTOM_KIOSKID));
      } catch (JSONException e) {
        // do nothing
      }
      // Add users, if any
      try {
        k.put(JsonTagsZ.USERS, getKioskUsers(relatedKioskJson.getJSONObject(JsonTagsZ.USERS)));
      } catch (Exception e) {
        // ignore
      }
      // Disable batch management
      try {
        k.put(JsonTagsZ.DISABLE_BATCH_MGMT,
            relatedKioskJson.getString(JsonTagsZ.DISABLE_BATCH_MGMT));
      } catch (JSONException e) {
        // do nothing
      }
      // Add to vector
      kiosks.addElement(k);
    }
    return kiosks;
  }

  // Add kiosk user data
  public static void addKioskUsers(JSONObject kioskJson, Vector users) throws JSONException {
    if (users == null || users.isEmpty()) {
      return;
    }
    JSONArray usersJson = new JSONArray();
    Enumeration en = users.elements();
    while (en.hasMoreElements()) {
      Hashtable user = (Hashtable) en.nextElement();
      JSONObject userJson = new JSONObject();
      userJson.put(JsonTagsZ.USER_ID, user.get(JsonTagsZ.USER_ID));
      userJson.put(JsonTagsZ.FIRST_NAME, user.get(JsonTagsZ.FIRST_NAME));
      String value = null;
      if ((value = (String) user.get(JsonTagsZ.LAST_NAME)) != null && !value.equals("")) {
        userJson.put(JsonTagsZ.LAST_NAME, value);
      }
      userJson.put(JsonTagsZ.MOBILE, user.get(JsonTagsZ.MOBILE));
      userJson.put(JsonTagsZ.ROLE, user.get(JsonTagsZ.ROLE));
      if ((value = (String) user.get(JsonTagsZ.STATE)) != null && !value.equals("")) {
        userJson.put(JsonTagsZ.STATE, value);
      }
      if ((value = (String) user.get(JsonTagsZ.PERMISSIONS)) != null && !value.equals("")) {
        userJson.put(JsonTagsZ.PERMISSIONS, value);
      }
      if ((value = (String) user.get(JsonTagsZ.CUSTOM_USERID)) != null && !value.equals("")) {
        userJson.put(JsonTagsZ.CUSTOM_USERID, value);
      }
      if ((value = (String) user.get(JsonTagsZ.EMAIL)) != null && !value.equals("")) {
        userJson.put(JsonTagsZ.EMAIL, value);
      }
      usersJson.put(userJson);
    }
    kioskJson.put(JsonTagsZ.USERS, usersJson);
  }

  // Get kiosk users (vector of hashtable)
  public static Vector getKioskUsers(JSONObject kioskJson) throws JSONException {
    JSONArray usersJson = kioskJson.getJSONArray(JsonTagsZ.USERS);
    Vector users = new Vector();
    for (int i = 0; i < usersJson.length(); i++) {
      JSONObject userJson = usersJson.getJSONObject(i);
      Hashtable user = new Hashtable();
      user.put(JsonTagsZ.USER_ID, userJson.getString(JsonTagsZ.USER_ID));
      user.put(JsonTagsZ.FIRST_NAME, userJson.getString(JsonTagsZ.FIRST_NAME));
      try {
        user.put(JsonTagsZ.LAST_NAME, userJson.getString(JsonTagsZ.LAST_NAME));
      } catch (Exception e) {
        // ignore
      }
      user.put(JsonTagsZ.MOBILE, userJson.getString(JsonTagsZ.MOBILE));
      user.put(JsonTagsZ.ROLE, userJson.getString(JsonTagsZ.ROLE));
      try {
        user.put(JsonTagsZ.STATE, userJson.getString(JsonTagsZ.STATE));
      } catch (Exception e) {
        // ignore
      }
      try {
        user.put(JsonTagsZ.PERMISSIONS, userJson.getString(JsonTagsZ.PERMISSIONS));
      } catch (Exception e) {
        // ignore
      }
      try {
        user.put(JsonTagsZ.CUSTOM_USERID, userJson.getString(JsonTagsZ.CUSTOM_USERID));
      } catch (Exception e) {
        // ignore
      }
      try {
        user.put(JsonTagsZ.EMAIL, userJson.getString(JsonTagsZ.EMAIL));
      } catch (Exception e) {
        // ignore
      }
      users.addElement(user);
    }
    return users;
  }

  // Get an Order JSON
  public static JSONObject getOrderJSON(Hashtable orderData) throws JSONException {
    JSONObject json = new JSONObject();
    // Add order metadata
    String value = null;
    if ((value = (String) orderData.get(JsonTagsZ.TRACKING_ID)) != null) {
      json.put(JsonTagsZ.TRACKING_ID, value);
    }
    if ((value = (String) orderData.get(JsonTagsZ.KIOSK_ID)) != null) {
      json.put(JsonTagsZ.KIOSK_ID, value);
    }
    if ((value = (String) orderData.get(JsonTagsZ.ORDER_STATUS)) != null) {
      json.put(JsonTagsZ.ORDER_STATUS, value);
    }
    if ((value = (String) orderData.get(JsonTagsZ.QUANTITY)) != null) {
      json.put(JsonTagsZ.QUANTITY, value);
    }
    if ((value = (String) orderData.get(JsonTagsZ.UPDATED_TIME)) != null) {
      json.put(JsonTagsZ.UPDATED_TIME, value);
    }
    // Add macro-message, if any
    if ((value = (String) orderData.get(JsonTagsZ.MESSAGE)) != null) {
      json.put(JsonTagsZ.MESSAGE, value);
    }
    // Add total price, if present
    if ((value = (String) orderData.get(JsonTagsZ.TOTAL_PRICE)) != null) {
      json.put(JsonTagsZ.TOTAL_PRICE, value);
    }
    // Add currency, if available
    if ((value = (String) orderData.get(JsonTagsZ.CURRENCY)) != null) {
      json.put(JsonTagsZ.CURRENCY, value);
    }
    // Add vendors, if available
    if ((value = (String) orderData.get(JsonTagsZ.VENDORID)) != null) {
      json.put(JsonTagsZ.VENDORID, value);
    }
    // Add estimated fulfillment times, if specified
    if ((value = (String) orderData.get(JsonTagsZ.ESTIMATED_FULFILLMENT_TIMERANGES)) != null) {
      json.put(JsonTagsZ.ESTIMATED_FULFILLMENT_TIMERANGES, value);
    }
    // Add confirmed fulfillment time ranges, if specified
    if ((value = (String) orderData.get(JsonTagsZ.CONFIRMED_FULFILLMENT_TIMERANGE)) != null) {
      json.put(JsonTagsZ.CONFIRMED_FULFILLMENT_TIMERANGE, value);
    }
    // Add payment, if specified
    if ((value = (String) orderData.get(JsonTagsZ.PAYMENT)) != null) {
      json.put(JsonTagsZ.PAYMENT, value);
    }
    // Add payment option, if specified
    if ((value = (String) orderData.get(JsonTagsZ.PAYMENT_OPTION)) != null) {
      json.put(JsonTagsZ.PAYMENT_OPTION, value);
    }
    // Add package size, if specified
    if ((value = (String) orderData.get(JsonTagsZ.PACKAGE_SIZE)) != null) {
      json.put(JsonTagsZ.PACKAGE_SIZE, value);
    }
    // Add credit limit, if specified
    if ((value = (String) orderData.get(JsonTagsZ.CREDIT_LIMIT)) != null) {
      json.put(JsonTagsZ.CREDIT_LIMIT, value);
    }
    // Add payable, if specified
    if ((value = (String) orderData.get(JsonTagsZ.PAYABLE)) != null) {
      json.put(JsonTagsZ.PAYABLE, value);
    }
    // Add timestamp
    json.put(JsonTagsZ.TIMESTAMP, orderData.get(JsonTagsZ.TIMESTAMP));
    // Custom customer id, if specified
    if ((value = (String) orderData.get(JsonTagsZ.CUSTOM_KIOSKID)) != null) {
      json.put(JsonTagsZ.CUSTOM_KIOSKID, value);
    }
    // Custom vendor id, if specified
    if ((value = (String) orderData.get(JsonTagsZ.CUSTOM_VENDORID)) != null) {
      json.put(JsonTagsZ.CUSTOM_VENDORID, value);
    }
    // Custom user id if specified
    if ((value = (String) orderData.get(JsonTagsZ.CUSTOM_USERID)) != null) {
      json.put(JsonTagsZ.CUSTOM_USERID, value);
    }
    // Transporter if specified
    if ((value = (String) orderData.get(JsonTagsZ.TRANSPORTER)) != null) {
      json.put(JsonTagsZ.TRANSPORTER, value);
    }
    // Add materials
    Vector mtrls = (Vector) orderData.get(JsonTagsZ.MATERIALS);
    if (mtrls != null && !mtrls.isEmpty()) {
      JsonUtil.addMaterialData(json, mtrls);
    }

    return json;
  }

  // Get a Order hashtable from a JSON
  public static Hashtable getOrder(JSONObject json) throws JSONException {
    // Get order metadata
    Hashtable orderData = new Hashtable();
    try {
      orderData.put(JsonTagsZ.TRACKING_ID, json.get(JsonTagsZ.TRACKING_ID));
    } catch (JSONException e) {
      // do nothing
    }
    try {
      orderData.put(JsonTagsZ.KIOSK_ID, json.get(JsonTagsZ.KIOSK_ID));
    } catch (JSONException e) {
      // do nothing
    }
    try {
      orderData.put(JsonTagsZ.ORDER_STATUS, json.get(JsonTagsZ.ORDER_STATUS));
    } catch (JSONException e) {
      // do nothing
    }
    try {
      orderData.put(JsonTagsZ.QUANTITY, json.get(JsonTagsZ.QUANTITY));
    } catch (JSONException e) {
      // do nothing
    }
    // Get updated time, if any
    try {
      orderData.put(JsonTagsZ.UPDATED_TIME, json.get(JsonTagsZ.UPDATED_TIME));
    } catch (JSONException e) {
      // do nothing
    }
    // Get message, if any
    try {
      orderData.put(JsonTagsZ.MESSAGE, json.get(JsonTagsZ.MESSAGE));
    } catch (JSONException e) {
      // do nothing
    }
    // Get total price, if any
    try {
      orderData.put(JsonTagsZ.TOTAL_PRICE, json.get(JsonTagsZ.TOTAL_PRICE));
    } catch (JSONException e) {
      // do nothing
    }
    // Get currency, if available
    try {
      orderData.put(JsonTagsZ.CURRENCY, json.get(JsonTagsZ.CURRENCY));
    } catch (JSONException e) {
      // do nothing
    }
    // Get the vendor ID, if available
    try {
      orderData.put(JsonTagsZ.VENDORID, json.get(JsonTagsZ.VENDORID));
    } catch (JSONException e) {
      // do nothing
    }
    // Get estimated fulfillment times, if specified
    try {
      orderData.put(JsonTagsZ.ESTIMATED_FULFILLMENT_TIMERANGES,
          json.get(JsonTagsZ.ESTIMATED_FULFILLMENT_TIMERANGES));
    } catch (JSONException e) {
      // do nothing
    }
    // Get confirmed fulfillment time range, if specified
    try {
      orderData.put(JsonTagsZ.CONFIRMED_FULFILLMENT_TIMERANGE,
          json.get(JsonTagsZ.CONFIRMED_FULFILLMENT_TIMERANGE));
    } catch (JSONException e) {
      // do nothing
    }
    // Get payment, if specified
    try {
      orderData.put(JsonTagsZ.PAYMENT, json.get(JsonTagsZ.PAYMENT));
    } catch (JSONException e) {
      // do nothing
    }
    // Get payment option, if specified
    try {
      orderData.put(JsonTagsZ.PAYMENT_OPTION, json.get(JsonTagsZ.PAYMENT_OPTION));
    } catch (JSONException e) {
      // do nothing
    }
    // Get package size, if specified
    try {
      orderData.put(JsonTagsZ.PACKAGE_SIZE, json.get(JsonTagsZ.PACKAGE_SIZE));
    } catch (JSONException e) {
      // do nothing
    }
    // Get credit limit, if specified
    try {
      orderData.put(JsonTagsZ.CREDIT_LIMIT, json.get(JsonTagsZ.CREDIT_LIMIT));
    } catch (JSONException e) {
      // do nothing
    }
    // Get payable, if specified
    try {
      orderData.put(JsonTagsZ.PAYABLE, json.get(JsonTagsZ.PAYABLE));
    } catch (JSONException e) {
      // do nothing
    }
    // Get timestamp
    orderData.put(JsonTagsZ.TIMESTAMP, json.get(JsonTagsZ.TIMESTAMP));
    // Get custom customer id, if specified
    try {
      orderData.put(JsonTagsZ.CUSTOM_KIOSKID, json.get(JsonTagsZ.CUSTOM_KIOSKID));
    } catch (JSONException e) {
      // do nothing
    }
    // Get custom vendor id, if specified
    try {
      orderData.put(JsonTagsZ.CUSTOM_VENDORID, json.get(JsonTagsZ.CUSTOM_VENDORID));
    } catch (JSONException e) {
      // do nothing
    }
    // Get custom user id, if specified
    try {
      orderData.put(JsonTagsZ.CUSTOM_USERID, json.get(JsonTagsZ.CUSTOM_USERID));
    } catch (JSONException e) {
      // do nothing
    }
    // Get transporter, if specified
    try {
      orderData.put(JsonTagsZ.TRANSPORTER, json.get(JsonTagsZ.TRANSPORTER));
    } catch (JSONException e) {
      // do nothing
    }
    // Get materials
    Vector materials = JsonUtil.getMaterialData(json);
    if (materials != null && !materials.isEmpty()) {
      orderData.put(JsonTagsZ.MATERIALS, materials);
    }

    return orderData;
  }

  // Get a list of string from a CSV formatted String
  public static Vector tokenize(String csv) {
    if (csv == null || csv.equals("")) {
      return null;
    }
    Vector list = new Vector();
    int index = csv.indexOf(",");
    if (index == -1) {
      list.addElement(csv);
      return list;
    }
    String tmp = csv;
    while (index != -1) {
      list.addElement(tmp.substring(0, index));
      tmp = tmp.substring(index + 1);
      index = tmp.indexOf(",");
    }
    if (!tmp.equals("")) {
      list.addElement(tmp);
    }

    return list;
  }

  public static void updateTagMap(Tags tags, String tagsCSV, Hashtable material) {
    if (tags == null || material == null) {
      return;
    }
    Vector tagList = tokenize(tagsCSV);
    if (tagList == null || tagList.isEmpty()) {
      tagList = new Vector();
      tagList.addElement(tags.NOTAGS);
    }
    Enumeration en = tagList.elements();
    while (en.hasMoreElements()) {
      tags.addData((String) en.nextElement(), material);
    }
  }

  // Convert a Hashtable to a JSON object
  public static JSONArray vectorToJSON(Vector v) throws JSONException {
    if (v == null || v.isEmpty()) {
      return null;
    }
    JSONArray jsonArray = new JSONArray();
    Enumeration en = v.elements();
    while (en.hasMoreElements()) {
      jsonArray.put(en.nextElement());
    }
    return jsonArray;
  }

  // Convert a JSON object to a map
  public static Vector jsonToVector(JSONArray jsonArray) throws JSONException {
    if (jsonArray == null) {
      return null;
    }
    Vector v = new Vector();
    for (int i = 0; i < jsonArray.length(); i++) {
      v.addElement(jsonArray.get(i));
    }
    return v;
  }

  // Parse estimated fulfillment times
  public static Vector parseTimeRanges(String timeRangesStr) {
    StringTokenizer st = new StringTokenizer(timeRangesStr, ",");
    Vector timeRanges = new Vector();
    while (st.hasMoreTokens()) {
      Hashtable tr = parseTimeRange(st.nextToken());
      if (tr != null) {
        timeRanges.addElement(tr);
      }
    }
    return timeRanges;
  }

  // Parse a given time range
  public static Hashtable parseTimeRange(String timeRange) {
    if (timeRange == null || timeRange.equals("")) {
      return null;
    }
    int index = timeRange.indexOf('-');
    String startTime = null, endTime = null;
    if (index == -1) {
      startTime = timeRange;
    } else {
      startTime = timeRange.substring(0, index);
      endTime = timeRange.substring(index + 1);
    }
    Hashtable timeRangeHt = new Hashtable();
    timeRangeHt.put(JsonTagsZ.TIME_START, startTime);
    if (endTime != null) {
      timeRangeHt.put(JsonTagsZ.TIME_END, endTime);
    }
    return timeRangeHt;
  }

  // Format a time range (<startTime>-<endTime>)
  public static String formatTimeRange(Hashtable timeRange) {
    String str = (String) timeRange.get(JsonTagsZ.TIME_START);
    if (str == null || str.equals("")) {
      return null;
    }
    String endTimeStr = (String) timeRange.get(JsonTagsZ.TIME_END);
    if (endTimeStr != null && !endTimeStr.equals("")) {
      str += "-" + endTimeStr;
    }
    return str;
  }

  // Format time ranges (CSV of time-ranges formatted as above)
  public static String formatTimeRanges(Vector timeRanges) {
    Enumeration en = timeRanges.elements();
    String str = "";
    while (en.hasMoreElements()) {
      String tr = formatTimeRange((Hashtable) en.nextElement());
      if (tr != null && !tr.equals("")) {
        if (str.length() > 0) {
          str += ",";
        }
        str += tr;
      }
    }
    return str;
  }

  // Add transactions data to JSON object
  public static void addTransactionData(JSONObject json, Vector transactions) throws JSONException {
    if (transactions != null) {
      // Init. array
      JSONArray array = new JSONArray();
      if (!transactions.isEmpty()) {
        Enumeration en = transactions.elements();
        while (en.hasMoreElements()) {
          Hashtable transaction = (Hashtable) en.nextElement();
          // Init. JSON object per transaction
          JSONObject t = new JSONObject();
          t.put(JsonTagsZ.MATERIAL_ID, (String) transaction.get(JsonTagsZ.MATERIAL_ID));
          t.put(JsonTagsZ.TRANSACTION_TYPE, (String) transaction.get(JsonTagsZ.TRANSACTION_TYPE));
          t.put(JsonTagsZ.QUANTITY, (String) transaction.get(JsonTagsZ.QUANTITY));
          t.put(JsonTagsZ.TIMESTAMP, (String) transaction.get(JsonTagsZ.TIMESTAMP));
          t.put(JsonTagsZ.OPENING_STOCK, (String) transaction.get(JsonTagsZ.OPENING_STOCK));
          t.put(JsonTagsZ.CLOSING_STOCK, (String) transaction.get(JsonTagsZ.CLOSING_STOCK));
          t.put(JsonTagsZ.USER_ID, (String) transaction.get(JsonTagsZ.USER_ID));
          t.put(JsonTagsZ.USER, (String) transaction.get(JsonTagsZ.USER));

          String value = null;
          if ((value = (String) transaction.get(JsonTagsZ.REASON)) != null) {
            t.put(JsonTagsZ.REASON, value);
          }
          if ((value = (String) transaction.get(JsonTagsZ.MATERIAL_STATUS)) != null) {
            t.put(JsonTagsZ.MATERIAL_STATUS, value);
          }
          if ((value = (String) transaction.get(JsonTagsZ.ACTUAL_TRANSACTION_DATE)) != null) {
            t.put(JsonTagsZ.ACTUAL_TRANSACTION_DATE, value);
          }
          if ((value = (String) transaction.get(JsonTagsZ.LINKED_KIOSK_ID)) != null) {
            t.put(JsonTagsZ.LINKED_KIOSK_ID, value);
          }
          if ((value = (String) transaction.get(JsonTagsZ.LINKED_KIOSK_NAME)) != null) {
            t.put(JsonTagsZ.LINKED_KIOSK_NAME, value);
          }

          if ((value = (String) transaction.get(JsonTagsZ.BATCH_ID)) != null) {
            t.put(JsonTagsZ.BATCH_ID, value);
          }

          if ((value = (String) transaction.get(JsonTagsZ.OPENING_STOCK_IN_BATCH)) != null) {
            t.put(JsonTagsZ.OPENING_STOCK_IN_BATCH, value);
          }

          if ((value = (String) transaction.get(JsonTagsZ.BATCH_EXPIRY)) != null) {
            t.put(JsonTagsZ.BATCH_EXPIRY, value);
          }

          if ((value = (String) transaction.get(JsonTagsZ.BATCH_MANUFACTUER_NAME)) != null) {
            t.put(JsonTagsZ.BATCH_MANUFACTUER_NAME, value);
          }

          if ((value = (String) transaction.get(JsonTagsZ.BATCH_MANUFACTURED_DATE)) != null) {
            t.put(JsonTagsZ.BATCH_MANUFACTURED_DATE, value);
          }

          if ((value = (String) transaction.get(JsonTagsZ.CLOSING_STOCK_IN_BATCH)) != null) {
            t.put(JsonTagsZ.CLOSING_STOCK_IN_BATCH, value);
          }

          // Add to array
          array.put(t);
        }
      }
      // Add to container JSON
      json.put(JsonTagsZ.TRANSACTIONS, array);
    }
  }

  // Load transactions data from JSON and return a vector of hashtable
  public static Vector getTransactionData(JSONObject json) throws JSONException {
    Vector transactions = new Vector();
    // Get the transactions array
    JSONArray array = null;
    try {
      array = (JSONArray) json.get(JsonTagsZ.TRANSACTIONS);
    } catch (JSONException e) {
      return transactions; // return empty vector, in case of no transactionss
    }
    for (int i = 0; i < array.length(); i++) {
      JSONObject t = (JSONObject) array.get(i);
      Hashtable transaction = new Hashtable();
      transaction.put(JsonTagsZ.MATERIAL_ID, (String) t.get(JsonTagsZ.MATERIAL_ID));
      transaction.put(JsonTagsZ.TRANSACTION_TYPE, (String) t.get(JsonTagsZ.TRANSACTION_TYPE));
      transaction.put(JsonTagsZ.QUANTITY, (String) t.get(JsonTagsZ.QUANTITY));
      transaction.put(JsonTagsZ.TIMESTAMP, (String) t.get(JsonTagsZ.TIMESTAMP));

      // Get the reason (optional)
      try {
        transaction.put(JsonTagsZ.REASON, (String) t.get(JsonTagsZ.REASON));
      } catch (JSONException e) {
        // do nothing
      }
      // Get the material status (optional)
      try {
        transaction.put(JsonTagsZ.MATERIAL_STATUS, (String) t.get(JsonTagsZ.MATERIAL_STATUS));
      } catch (JSONException e) {
        // do nothing
      }
      // Get the actual transaction date (optional)
      try {
        transaction.put(JsonTagsZ.ACTUAL_TRANSACTION_DATE,
            (String) t.get(JsonTagsZ.ACTUAL_TRANSACTION_DATE));
      } catch (JSONException e) {
        // do nothing
      }
      transaction.put(JsonTagsZ.OPENING_STOCK, (String) t.get(JsonTagsZ.OPENING_STOCK));
      transaction.put(JsonTagsZ.CLOSING_STOCK, (String) t.get(JsonTagsZ.CLOSING_STOCK));
      transaction.put(JsonTagsZ.USER_ID, (String) t.get(JsonTagsZ.USER_ID));
      transaction.put(JsonTagsZ.USER, (String) t.get(JsonTagsZ.USER));

      // Get optional linked kiosk id
      try {
        transaction.put(JsonTagsZ.LINKED_KIOSK_ID, (String) t.get(JsonTagsZ.LINKED_KIOSK_ID));
      } catch (JSONException e) {
        // do nothing
      }

      // Get optional linked kiosk name
      try {
        transaction.put(JsonTagsZ.LINKED_KIOSK_NAME, (String) t.get(JsonTagsZ.LINKED_KIOSK_NAME));
      } catch (JSONException e) {
        // do nothing
      }

      // Get optional batch id
      try {
        transaction.put(JsonTagsZ.BATCH_ID, (String) t.get(JsonTagsZ.BATCH_ID));
      } catch (JSONException e) {
        // do nothing
      }
      // Get optional opening stock in batch
      try {
        transaction.put(JsonTagsZ.OPENING_STOCK_IN_BATCH,
            (String) t.get(JsonTagsZ.OPENING_STOCK_IN_BATCH));
      } catch (JSONException e) {
        // do nothing
      }
      // Get optinal batch expiry
      try {
        transaction.put(JsonTagsZ.BATCH_EXPIRY, (String) t.get(JsonTagsZ.BATCH_EXPIRY));
      } catch (JSONException e) {
        // do nothing
      }
      // Get optinal batch manufactured name
      try {
        transaction.put(JsonTagsZ.BATCH_MANUFACTUER_NAME,
            (String) t.get(JsonTagsZ.BATCH_MANUFACTUER_NAME));
      } catch (JSONException e) {
        // do nothing
      }
      // Get optional batch maufactured date
      try {
        transaction.put(JsonTagsZ.BATCH_MANUFACTURED_DATE,
            (String) t.get(JsonTagsZ.BATCH_MANUFACTURED_DATE));
      } catch (JSONException e) {
        // do nothing
      }
      // Get optinal closing stock in batch
      try {
        transaction.put(JsonTagsZ.CLOSING_STOCK_IN_BATCH,
            (String) t.get(JsonTagsZ.CLOSING_STOCK_IN_BATCH));
      } catch (JSONException e) {
        // do nothing
      }
      // Add to vector
      transactions.addElement(transaction);
    }

    return transactions;
  }
}

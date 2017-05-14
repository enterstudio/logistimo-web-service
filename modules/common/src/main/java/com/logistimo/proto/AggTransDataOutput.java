/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.logistimo.proto;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Hashtable;
import java.util.Vector;

/**
 * @author vani
 */
public class AggTransDataOutput extends OutputMessageBean implements JsonBean {

  private int numResults;
  private String cursor;
  private Vector aggrTransData;

  public AggTransDataOutput(String locale) throws ProtocolException {
    super(locale);
  }

  public AggTransDataOutput(boolean status, int numResults, String cursor, Vector aggrTransData,
                            String errMsg, String locale, String version) throws ProtocolException {
    super(status, errMsg, locale, version);
    this.numResults = numResults;
    this.cursor = cursor;
    this.aggrTransData = aggrTransData;
  }

  public int getNumResults() {
    return numResults;
  }

  public String getCursor() {
    return cursor;
  }

  public Vector getAggTransData() {
    return aggrTransData;
  }

  /**
   * Load the object data from a JSON string
   */
  public void fromJSONString(String jsonString) throws ProtocolException {
    try {
      JSONObject json = new JSONObject(jsonString);
      // Get version
      this.version = (String) json.get(JsonTagsZ.VERSION);
      loadFromJSON(json);
    } catch (JSONException e) {
      throw new ProtocolException(e.getMessage());
    }
  }

  /**
   * Convert to JSON String
   */
  public String toJSONString() throws ProtocolException {
    String jsonString = null;
    try {
      jsonString = toJSONObject().toString();
    } catch (JSONException e) {
      throw new ProtocolException(e.getMessage());
    }
    return jsonString;
  }

  // Get the JSON Object
  private JSONObject toJSONObject() throws JSONException {
    JSONObject json = new JSONObject();
    // Add version
    json.put(JsonTagsZ.VERSION, version);
    // Add the status code
    json.put(JsonTagsZ.STATUS, this.statusCode);
    if (JsonTagsZ.STATUS_FALSE.equals(this.statusCode)) {
      // Error message
      json.put(JsonTagsZ.MESSAGE, this.errMsg);
    } else { // success
      try {
        // Add the numResults
        json.put(JsonTagsZ.NUM_RESULTS, this.numResults);
      } catch (JSONException e) {
        // do nothing
      }
      try {
        // Add cursor
        json.put(JsonTagsZ.CURSOR, this.cursor);
      } catch (JSONException e) {
        // do nothing
      }
      // Add aggregate transaction data
      addAggrTransData(json);
    }
    return json;
  }

  // Add aggregate tyransaction data
  private void addAggrTransData(JSONObject json) throws JSONException {
    JSONArray dataJsonArray = new JSONArray();
    // Iterate through the vector aggrTransData
    for (int i = 0; i < aggrTransData.size(); i++) {
      Hashtable transData = (Hashtable) aggrTransData.elementAt(i);
      JSONObject dataJson = new JSONObject();
      // Add mandatory attributes
      dataJson.put(JsonTagsZ.MATERIAL_ID, transData.get(JsonTagsZ.MATERIAL_ID));
      dataJson.put(JsonTagsZ.TIMESTAMP, transData.get(JsonTagsZ.TIMESTAMP));
      dataJson.put(JsonTagsZ.DIMENSION_TYPE, transData.get(JsonTagsZ.DIMENSION_TYPE));
      dataJson.put(JsonTagsZ.DIMENSION_VALUE, transData.get(JsonTagsZ.DIMENSION_VALUE));
      // optional
      // issue count
      try {
        dataJson.put(JsonTagsZ.ISSUE_COUNT, transData.get(JsonTagsZ.ISSUE_COUNT));
      } catch (JSONException e) {
        // do nothing
      }
      // issue quantity
      try {
        dataJson.put(JsonTagsZ.ISSUE_QUANTITY, transData.get(JsonTagsZ.ISSUE_QUANTITY));
      } catch (JSONException e) {
        // do nothing
      }
      // receipt count
      try {
        dataJson.put(JsonTagsZ.RECEIPT_COUNT, transData.get(JsonTagsZ.RECEIPT_COUNT));
      } catch (JSONException e) {
        // do nothing
      }
      // receipt quantity
      try {
        dataJson.put(JsonTagsZ.RECEIPT_QUANTITY, transData.get(JsonTagsZ.RECEIPT_QUANTITY));
      } catch (JSONException e) {
        // do nothing
      }
      // stock count
      try {
        dataJson.put(JsonTagsZ.STOCK_COUNT, transData.get(JsonTagsZ.STOCK_COUNT));
      } catch (JSONException e) {
        // do nothing
      }
      // stock quantity
      try {
        dataJson.put(JsonTagsZ.STOCK_QUANTITY, transData.get(JsonTagsZ.STOCK_QUANTITY));
      } catch (JSONException e) {
        // do nothing
      }
      // stock count difference
      try {
        dataJson
            .put(JsonTagsZ.STOCKCOUNT_DIFFERENCE, transData.get(JsonTagsZ.STOCKCOUNT_DIFFERENCE));
      } catch (JSONException e) {
        // do nothing
      }
      // demand count
      try {
        dataJson.put(JsonTagsZ.DEMAND_COUNT, transData.get(JsonTagsZ.DEMAND_COUNT));
      } catch (JSONException e) {
        // do nothing
      }
      // demand quantity
      try {
        dataJson.put(JsonTagsZ.DEMAND_QUANTITY, transData.get(JsonTagsZ.DEMAND_QUANTITY));
      } catch (JSONException e) {
        // do nothing
      }
      // Add dataJson to dataJsonArray
      dataJsonArray.put(dataJson);
    }
    // Add dataJsonArray to json
    json.put(JsonTagsZ.DATA, dataJsonArray);
  }

  // Load from JSON string (ver 01)
  private void loadFromJSON(JSONObject json) throws JSONException {
    // Get status
    statusCode = (String) json.get(JsonTagsZ.STATUS);
    status = JsonTagsZ.STATUS_TRUE.equals(statusCode);
    if (!status) {
      errMsg = (String) json.get(JsonTagsZ.MESSAGE);
    } else {
      // Get num results
      numResults = json.getInt(JsonTagsZ.NUM_RESULTS);
      // Get cursor
      cursor = json.getString(JsonTagsZ.CURSOR);

      // GetAggrTransData
      aggrTransData = new Vector();
      loadAggrTransData((JSONArray) json.get(JsonTagsZ.DATA));
    }
  }

  public void loadAggrTransData(JSONArray aggrTransDataJsonArray) throws JSONException {
    for (int i = 0; i < aggrTransDataJsonArray.length(); i++) {
      JSONObject aggrTransDataJson = null;
      Hashtable aggrTransDataHt = null;
      aggrTransDataJson = aggrTransDataJsonArray.getJSONObject(i);
      aggrTransDataHt = new Hashtable();
      // Load the mandatory attributes
      aggrTransDataHt.put(JsonTagsZ.MATERIAL_ID, aggrTransDataJson.get(JsonTagsZ.MATERIAL_ID));
      aggrTransDataHt.put(JsonTagsZ.TIMESTAMP, aggrTransDataJson.get(JsonTagsZ.TIMESTAMP));
      aggrTransDataHt
          .put(JsonTagsZ.DIMENSION_TYPE, aggrTransDataJson.get(JsonTagsZ.DIMENSION_TYPE));
      aggrTransDataHt
          .put(JsonTagsZ.DIMENSION_VALUE, aggrTransDataJson.get(JsonTagsZ.DIMENSION_VALUE));

      // Load optional attributes
      // issue count
      try {
        aggrTransDataHt.put(JsonTagsZ.ISSUE_COUNT, aggrTransDataJson.get(JsonTagsZ.ISSUE_COUNT));

      } catch (JSONException e) {
        // do nothing
      }
      // issue quantity
      try {
        aggrTransDataHt
            .put(JsonTagsZ.ISSUE_QUANTITY, aggrTransDataJson.get(JsonTagsZ.ISSUE_QUANTITY));

      } catch (JSONException e) {
        // do nothing
      }
      // receipt count
      try {
        aggrTransDataHt
            .put(JsonTagsZ.RECEIPT_COUNT, aggrTransDataJson.get(JsonTagsZ.RECEIPT_COUNT));

      } catch (JSONException e) {
        // do nothing
      }
      // receipt quantity
      try {
        aggrTransDataHt
            .put(JsonTagsZ.RECEIPT_QUANTITY, aggrTransDataJson.get(JsonTagsZ.RECEIPT_QUANTITY));

      } catch (JSONException e) {
        // do nothing
      }
      // stock count
      try {
        aggrTransDataHt.put(JsonTagsZ.STOCK_COUNT, aggrTransDataJson.get(JsonTagsZ.STOCK_COUNT));

      } catch (JSONException e) {
        // do nothing
      }
      // stock quantity
      try {
        aggrTransDataHt
            .put(JsonTagsZ.STOCK_QUANTITY, aggrTransDataJson.get(JsonTagsZ.STOCK_QUANTITY));

      } catch (JSONException e) {
        // do nothing
      }
      aggrTransData.addElement(aggrTransDataHt);
    }
  }

  public void fromMessageString(Vector messages) throws ProtocolException {
  }

  public Vector toMessageString() throws ProtocolException {
    return null;
  }

}

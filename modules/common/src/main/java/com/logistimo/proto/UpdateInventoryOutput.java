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
import java.util.Vector;

/**
 * @author arun
 *
 *         Represents the output JSON of Inventory Update transaction
 */
public class UpdateInventoryOutput extends OutputMessageBean implements JsonBean {

  private Vector
      materials =
      null;
  // only Vector supported in J2ME (http://java.sun.com/javame/reference/apis/jsr118/java/util/package-summary.html)
  private Vector errors = null; // Vector of Hashtable containing error material information
  private boolean hasPartialErrors = false;
  private String timestamp = null;
  private String trackingId = null;

  public UpdateInventoryOutput(String locale) throws ProtocolException {
    super(locale);
  }

  public UpdateInventoryOutput(boolean status, Vector materials, String errMsg, Vector errors,
                               String timestamp, String trackingId, String locale, String version)
      throws ProtocolException {
    super(status, errMsg, locale, version);
    this.materials = materials;
    this.errors = errors;
    //if ( ( materials != null && materials.size() > 0 ) && ( errors != null && errors.size() > 0 ) ) {
    if (materials == null || materials.isEmpty()) {
      this.materials = new Vector();
    }
    if (errors != null && errors.size() > 0) {
      this.hasPartialErrors = true;
      this.status = false;
      this.statusCode = JsonTagsZ.STATUS_PARTIAL;
    }
    this.timestamp = timestamp;
    this.trackingId = trackingId;
  }

  // Accessor methods
  public Vector getMaterials() {
    return this.materials;
  }

  public Vector getErrors() {
    return this.errors;
  }

  public boolean hasPartialErrors() {
    return this.hasPartialErrors;
  }

  public String getTimestamp() {
    return this.timestamp;
  }

  public String getTrackingId() {
    return this.trackingId;
  }

  public void fromJSONString(String jsonString) throws ProtocolException {
    try {
      JSONObject json = new JSONObject(jsonString);
      try {
        this.version = (String) json.get(JsonTagsZ.VERSION);
      } catch (JSONException e) {
        // do nothing, if no version is present
      }
      loadFromJSON01(json);
    } catch (JSONException e) {
      throw new ProtocolException(e.getMessage());
    }
  }

  public String toJSONString() throws ProtocolException {
    String jsonString = null;
    try {
      jsonString = toJSONObject().toString();
    } catch (JSONException e) {
      throw new ProtocolException(e.getMessage());
    }

    return jsonString;
  }

  public void fromMessageString(Vector messages) throws ProtocolException {
    if (messages == null || messages.isEmpty()) {
      throw new ProtocolException("No message specified");
    }
    // Assemble message
    String msg = MessageUtil.assemble(messages);
    if (msg == null || msg.length() == 0) {
      throw new ProtocolException("Message not assembled");
    }
    // Tokenize
    StringTokenizer st = new StringTokenizer(msg, " ");
    if (st.countTokens() < 3) {
      throw new ProtocolException("At least 3 tokens expected");
    }
    version = st.nextToken();
    statusCode = st.nextToken();
    status = JsonTagsZ.STATUS_TRUE.equals(statusCode);
    if (!status) {
      errMsg = MessageUtil.decode(st.nextToken());
    } else {
      timestamp = MessageUtil.decode(st.nextToken());
      if (st.hasMoreTokens()) {
        materials = MessageUtil.getMaterialsVector(st, null);
      } else {
        throw new ProtocolException(protoMessages.getString("error.nomaterials"));
      }
    }
  }

  public Vector toMessageString() throws ProtocolException {
    String msg = version + " " + statusCode;
    if (!status) {
      msg += " " + MessageUtil.encode(errMsg);
    } else {
      msg +=
          " " + MessageUtil.encode(timestamp) + " " + MessageUtil
              .getMaterialsString(materials, MessageUtil.QUANTITY_ONLY, false);
    }
    return MessageUtil.split(msg, getMessageId());
  }

  // Get the JSON object in the newer (01) format
  private JSONObject toJSONObject() throws JSONException {
    JSONObject json = new JSONObject();
    // Add version
    json.put(JsonTagsZ.VERSION, version);
    // Add the status code
    json.put(JsonTagsZ.STATUS, this.statusCode);
    if (JsonTagsZ.STATUS_FALSE.equals(this.statusCode)) {
      // Errors
      json.put(JsonTagsZ.MESSAGE, this.errMsg);
    } else { // success or partial errors
      // Add material data
      addMaterialData01(json);
      // Check for partial errors
      if (hasPartialErrors) {
        JsonUtil.addErrorData(json, this.errors);
      }
      // Add user message if present
      if (this.errMsg != null) {
        json.put(JsonTagsZ.MESSAGE, errMsg);
      }
      // Add last-updated time
      json.put(JsonTagsZ.TIMESTAMP, this.timestamp);
      // Add tracking Id, if present
      if (this.trackingId != null) {
        json.put(JsonTagsZ.TRACKING_ID, this.trackingId);
      }
    }

    return json;
  }

  // Add material data to JSON in new format
  private void addMaterialData01(JSONObject json) throws JSONException {
    //    if ( this.materials == null || this.materials.isEmpty() )
    if (this.materials == null) {
      throw new JSONException(protoMessages.getString("error.nomaterials"));
    }
    JSONArray array = new JSONArray();
    Enumeration en = this.materials.elements();
    while (en.hasMoreElements()) {
      Hashtable material = (Hashtable) en.nextElement();
      // Form JSON object for this material
      JSONObject m = new JSONObject();
      m.put(JsonTagsZ.MATERIAL_ID, (String) material.get(JsonTagsZ.MATERIAL_ID));
      m.put(JsonTagsZ.QUANTITY, (String) material.get(JsonTagsZ.QUANTITY));
      // Add batches, if any
      Vector batches = (Vector) material.get(JsonTagsZ.BATCHES);
      if (batches != null && !batches.isEmpty()) {
        JsonUtil.addBatchData(m, batches);
      }
      // Add expired batches, if any
      Vector expiredBatches = (Vector) material.get(JsonTagsZ.EXPIRED_NONZERO_BATCHES);
      if (expiredBatches != null && !expiredBatches.isEmpty()) {
        JsonUtil.addExpiredBatchData(m, expiredBatches);
      }
      // Add to JSON array
      array.put(m);
    }
    // Add to JSON container
    json.put(JsonTagsZ.MATERIALS, array);
  }

  // Load material data from a JSON
  private void loadMaterialData01(JSONObject json) throws JSONException {
    this.materials = new Vector();
    JSONArray array = (JSONArray) json.get(JsonTagsZ.MATERIALS);
    for (int i = 0; i < array.length(); i++) {
      JSONObject m = (JSONObject) array.get(i);
      Hashtable material = new Hashtable();
      material.put(JsonTagsZ.MATERIAL_ID, (String) m.get(JsonTagsZ.MATERIAL_ID));
      material.put(JsonTagsZ.QUANTITY, (String) m.get(JsonTagsZ.QUANTITY));
      // add batches, if any
      try {
        Vector batches = JsonUtil.getBatchData(m);
        if (batches != null && !batches.isEmpty()) {
          material.put(JsonTagsZ.BATCHES, batches);
        }
      } catch (JSONException e) {
        // do nothing
      }
      //get expired batches , if any
      try {
        Vector expiredBatches = JsonUtil.getExpiredBatchData(m);
        if (expiredBatches != null && !expiredBatches.isEmpty()) {
          material.put(JsonTagsZ.EXPIRED_NONZERO_BATCHES, expiredBatches);
        }
      } catch (JSONException e) {
        // do nothing
      }
      // Add to vector
      this.materials.addElement(material);
    }
  }

  // Load from new version of protocol
  private void loadFromJSON01(JSONObject json) throws JSONException {
    // Get the status code
    this.statusCode = (String) json.get(JsonTagsZ.STATUS);
    if (JsonTagsZ.STATUS_TRUE.equals(this.statusCode)) {
      this.status = true;
    } else if (JsonTagsZ.STATUS_PARTIAL.equals(this.statusCode)) {
      this.hasPartialErrors = true;
      this.status = false;
    } else {
      this.status = false;
    }

    if (!status && !hasPartialErrors) {
      this.errMsg = (String) json.get(JsonTagsZ.MESSAGE);
    } else if (status || hasPartialErrors) {
      // Load material and error data (if any)
      loadMaterialData01(json);
      if (hasPartialErrors) {
        this.errors = JsonUtil.getErrorData(json);
      }
      // Get message, if any
      try {
        this.errMsg = (String) json.get(JsonTagsZ.MESSAGE);
      } catch (JSONException e) {
        // do nothing
      }
      // Get timestamp
      this.timestamp = (String) json.get(JsonTagsZ.TIMESTAMP);
      // Get tracking Id, if any
      try {
        this.trackingId = (String) json.get(JsonTagsZ.TRACKING_ID);
      } catch (JSONException e) {
        // do nothing, if not present
      }
    }
  }
}

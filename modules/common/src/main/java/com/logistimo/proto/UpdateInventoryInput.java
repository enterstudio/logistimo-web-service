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

/*upd
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
 * @author Arun
 */
public class UpdateInventoryInput extends InputMessageBean implements JsonBean {

  protected String cmd = null;
  private String kioskId = null;
  private Vector materials = null;
  private String trackingId = null;
  private String linkedKioskId = null; // vendor or customer kiosk Id
  private String message = null;
  private String destUserId = null;
  private String type = null;
  private String latitude = null;
  private String longitude = null;
  private String altitude = null;
  private String geoAccuracy = null;
  private String geoErrorCode = null;
  private String timestampSaveMillis = null;
  private String partid = null;

  public UpdateInventoryInput() {
    cmd = RestConstantsZ.ACTION_UPDINVENTORY;
  }

  public UpdateInventoryInput(String type, String userId, String password, String kioskId,
                              Vector materials,
                              String trackingId, String linkedKioskId, String message,
                              String destUserId, String latitude, String longitude,
                              String version) {
    this(type, userId, password, kioskId, materials, trackingId, linkedKioskId, message, destUserId,
        latitude, longitude, null, null, version, null, null, null);
  }

  public UpdateInventoryInput(String type, String userId, String password, String kioskId,
                              Vector materials,
                              String trackingId, String linkedKioskId, String message,
                              String destUserId, String latitude, String longitude, String version,
                              String altitude) {
    this(type, userId, password, kioskId, materials, trackingId, linkedKioskId, message, destUserId,
        latitude, longitude, null, null, version, altitude, null, null);
  }

  // Added 10/1/2013
  public UpdateInventoryInput(String type, String userId, String password, String kioskId,
                              Vector materials,
                              String trackingId, String linkedKioskId, String message,
                              String destUserId, String latitude, String longitude,
                              String geoAccuracy, String geoErrorCode, String version,
                              String altitude, String timestampSaveMillis, String partid) {

    super(userId, password, version);
    this.type = type;
    this.kioskId = kioskId;
    this.materials = materials;
    this.trackingId = trackingId;
    this.linkedKioskId = linkedKioskId;
    this.message = message;
    this.destUserId = destUserId;
    this.latitude = latitude;
    this.longitude = longitude;
    this.geoAccuracy = geoAccuracy;
    this.geoErrorCode = geoErrorCode;
    this.altitude = altitude;
    this.timestampSaveMillis = timestampSaveMillis;
    this.partid = partid;
    cmd = RestConstantsZ.ACTION_UPDINVENTORY;
  }

  // Accessor methods
  public String getType() {
    return type;
  }

  public void setType(final String type) {
    this.type = type;
  }

  public String getKioskId() {
    return this.kioskId;
  }

  public void setKioskId(final String kioskId) {
    this.kioskId = kioskId;
  }

  public Vector getMaterials() {
    return this.materials;
  }

  public void setMaterials(final Vector materials) {
    this.materials = materials;
  }

  public String getTrackingId() {
    return this.trackingId;
  }

  public void setTrackingId(final String trackingId) {
    this.trackingId = trackingId;
  }

  public String getLinkedKioskId() {
    return this.linkedKioskId;
  }

  public void setLinkedKioskId(final String linkedKioskId) {
    this.linkedKioskId = linkedKioskId;
  }

  public String getMessage() {
    return this.message;
  }

  public void setMessage(final String message) {
    this.message = message;
  }

  public String getDestUserId() {
    return this.destUserId;
  }

  public void setDestUserId(final String destUserId) {
    this.destUserId = destUserId;
  }

  public String getLatitude() {
    return latitude;
  }

  public void setLatitude(final String latitude) {
    this.latitude = latitude;
  }

  public String getLongitude() {
    return longitude;
  }

  public void setLongitude(final String longitude) {
    this.longitude = longitude;
  }

  public String getAltitude() {
    return altitude;
  }

  public void setAltitude(final String altitude) {
    this.altitude = altitude;
  }

  public String getGeoAccuracy() {
    return geoAccuracy;
  }

  public void setGeoAccuracy(final String geoAccuracy) {
    this.geoAccuracy = geoAccuracy;
  }

  public String getGeoErrorCode() {
    return geoErrorCode;
  }

  public void setGeoErrorCode(final String geoErrorCode) {
    this.geoErrorCode = geoErrorCode;
  }

  public String getTimestampSaveMillis() {
    return timestampSaveMillis;
  }

  public void setTimestampSaveMillis(final String timestampSaveMillis) {
    this.timestampSaveMillis = timestampSaveMillis;
  }

  public String getPartId() {
    return partid;
  }

  public void setPartid(final String partid) {
    this.partid = partid;
  }

  // Load from a message string
  public void fromMessageString(Vector messages) throws ProtocolException {
    if (messages == null || messages.isEmpty()) {
      throw new ProtocolException("No message specified");
    }
    // Assemble message
    String msg = MessageUtil.assemble(messages);
    if (msg == null || msg.length() == 0) {
      throw new ProtocolException("Message not assembled");
    }
    // Parse message
    StringTokenizer st = new StringTokenizer(msg, " ");
    if (st.countTokens() < 7) {
      throw new ProtocolException("At least 7 tokens expected in message");
    }
    version = st.nextToken();
    if (!cmd.equals(st.nextToken())) {
      throw new ProtocolException("Invalid command");
    }
    type = st.nextToken();
    userId = st.nextToken();
    password = st.nextToken();
    kioskId = st.nextToken();
    materials = MessageUtil.getMaterialsVector(st, null);
    int ver = Integer.parseInt(version);
    if (ver >= Integer.parseInt(MessageHeader.VERSION02)) { // version 02 or greater
      // Get tracking Id
      trackingId = st.nextToken();
      if (MessageUtil.DUMMY.equals(trackingId)) {
        trackingId = null;
      }
      // Get customer/vendor id
      linkedKioskId = st.nextToken();
      if (MessageUtil.DUMMY.equals(linkedKioskId)) {
        linkedKioskId = null;
      }
      // Add latitude and longitude, if verion 03 and above
      if (ver >= Integer.parseInt(MessageHeader.VERSION03)) {
        if (st.hasMoreTokens()) {
          latitude = st.nextToken();
        }
        if (MessageUtil.DUMMY.equals(latitude)) {
          latitude = null;
        }
        if (st.hasMoreTokens()) {
          longitude = st.nextToken();
        }
        if (MessageUtil.DUMMY.equals(longitude)) {
          longitude = null;
        }
      }
    } else { // version 01
      if (RestConstantsZ.ACTION_UPDATEORDER.equals(cmd) && RestConstantsZ.TYPE_REORDER
          .equals(type)) {
        trackingId = st.nextToken();
      }
    }
    String value = null;
    if (st.hasMoreTokens()) {
      value = st.nextToken(); // check for missing response messages that did not arrive at client
    }
    if (value != null && !MessageUtil.DUMMY.equals(value)) {
      setResponseMessageNumbers(value);
    }
  }

  // Convert to a set of message strings
  public Vector toMessageString() throws ProtocolException {
    if (userId == null || password == null || kioskId == null) {
      throw new ProtocolException("All of userId, password and KioskId are required");
    }
    String message = version + " " +
        cmd + " " + type + " " +
        userId + " " + password + " " + " " + kioskId + " " +
        MessageUtil.getMaterialsString(materials, MessageUtil.QUANTITY_ONLY, true);
    if (RestConstantsZ.ACTION_UPDATEORDER.equals(cmd) && RestConstantsZ.TYPE_REORDER.equals(type)) {
      message += " " + trackingId;
    } else {
      message += " " + MessageUtil.DUMMY; // added in the version 02
    }
    // Add customer/vendor ID, if present
    if (linkedKioskId != null && linkedKioskId.length() > 0) {
      message += " " + linkedKioskId;
    } else {
      message += " " + MessageUtil.DUMMY;
    }
    if (Integer.parseInt(version) >= Integer.parseInt(MessageHeader.VERSION03)) {
      if (latitude != null && latitude.length() > 0) {
        message += " " + latitude;
      } else {
        message += " " + MessageUtil.DUMMY;
      }
      if (longitude != null && longitude.length() > 0) {
        message += " " + longitude;
      } else {
        message += " " + MessageUtil.DUMMY;
      }
    }
    // Add latitude/longitude
    if (hasResponseMessageNumbers()) {
      message += " " + getResponseMessageNumbers();
    } else {
      message += " " + MessageUtil.DUMMY;
    }
    if (isDev) {
      message += " d";
    }
    return MessageUtil.split(message, msgId);
  }

  // Load from JSON
  public void fromJSONString(String jsonString) throws ProtocolException {
    try {
      JSONObject jsonObject = new JSONObject(jsonString);
      fromJSONObject(jsonObject);
    } catch (JSONException e) {
      throw new ProtocolException(e.getMessage());
    }
  }

  protected void fromJSONObject(JSONObject jsonObject) throws ProtocolException {
    try {
      // Initialize the transaction object list
      materials = new Vector();
      this.userId = (String) jsonObject.get(JsonTagsZ.USER_ID);
      this.kioskId = (String) jsonObject.get(JsonTagsZ.KIOSK_ID);
      // Load material data
      loadMaterialData(jsonObject);
      // Get destination user id, if present
      try {
        this.destUserId = (String) jsonObject.get(JsonTagsZ.DEST_USER_ID);
      } catch (JSONException e) {
        // do nothing
      }
      // Get the macro-message, if any
      try {
        this.message = (String) jsonObject.get(JsonTagsZ.MESSAGE);
      } catch (JSONException e) {
        // do nothing
      }
      // Get the tracking-id, if any
      try {
        this.trackingId = (String) jsonObject.get(JsonTagsZ.TRACKING_ID);
      } catch (JSONException e) {
        // do nothing
      }
      // Get customer/vendor Id, if any
      try {
        this.linkedKioskId =
            (String) jsonObject.get(JsonTagsZ.LINKED_KIOSK_ID); // enabled since on 30/11/2011
      } catch (JSONException e) {
        // for backward compatibilty, also check on vendor Id
        try {
          this.linkedKioskId = (String) jsonObject.get(JsonTagsZ.VENDORID);
        } catch (JSONException e1) {
          // do nothing
        }
      }
      // Get latitude/longitude
      try {
        this.latitude = jsonObject.getString(JsonTagsZ.LATITUDE);
      } catch (JSONException e) {
        // do nothing
      }
      try {
        this.longitude = jsonObject.getString(JsonTagsZ.LONGITUDE);
      } catch (JSONException e) {
        // do nothing
      }
      try {
        this.geoAccuracy = jsonObject.getString(JsonTagsZ.GEO_ACCURACY);
      } catch (JSONException e) {
        // do nothing
      }
      try {
        this.geoErrorCode = jsonObject.getString(JsonTagsZ.GEO_ERROR_CODE);
      } catch (JSONException e) {
        // do nothing
      }
      try {
        this.altitude = jsonObject.getString(JsonTagsZ.ALTITUDE);
      } catch (JSONException e) {
        // do nothing
      }
      try {
        this.timestampSaveMillis = jsonObject.getString(JsonTagsZ.TIMESTAMP_OF_SAVE_MILLIS);
      } catch (JSONException e) {
        // do nothing
      }
      try {
        this.partid = jsonObject.getString(JsonTagsZ.PART_ID);
      } catch (JSONException e) {
        // do nothing
      }

    } catch (JSONException e) {
      throw new ProtocolException(e.getMessage());
    }
  }


  // Convert to JSON String
  // Create a JSON object in the newer (01) format
  public String toJSONString() throws ProtocolException {
    JSONObject json = toJSONObject();
    return json.toString();
  }

  protected JSONObject toJSONObject() throws ProtocolException {
    try {
      JSONObject json = new JSONObject();
      // Add version
      json.put(JsonTagsZ.VERSION, this.version);
      // Add user and kiosk ids
      json.put(JsonTagsZ.USER_ID, this.userId);
      json.put(JsonTagsZ.KIOSK_ID, this.kioskId);
      // Add the materials to be updated
      addMaterialData(json);
      // Add destination user id, if present
      if (this.destUserId != null && !this.destUserId.equals("")) {
        json.put(JsonTagsZ.DEST_USER_ID, this.destUserId);
      }
      // Add macro-message, if present
      if (this.message != null && !this.message.equals("")) {
        json.put(JsonTagsZ.MESSAGE, this.message);
      }
      // Add tracking-id, if present
      if (this.trackingId != null) {
        json.put(JsonTagsZ.TRACKING_ID, this.trackingId);
      }
      // Add vendor Id, if present
      if (this.linkedKioskId != null) {
        json.put(JsonTagsZ.LINKED_KIOSK_ID, this.linkedKioskId);
      }
      // Add latitude/longitude, if present
      if (latitude != null && latitude.length() > 0) {
        json.put(JsonTagsZ.LATITUDE, latitude);
      }
      if (longitude != null && longitude.length() > 0) {
        json.put(JsonTagsZ.LONGITUDE, longitude);
      }
      if (geoAccuracy != null && geoAccuracy.length() > 0) {
        json.put(JsonTagsZ.GEO_ACCURACY, geoAccuracy);
      }
      if (geoErrorCode != null && geoErrorCode.length() > 0) {
        json.put(JsonTagsZ.GEO_ERROR_CODE, geoErrorCode);
      }
      if (altitude != null && altitude.length() > 0) {
        json.put(JsonTagsZ.ALTITUDE, longitude);
      }
      if (timestampSaveMillis != null && timestampSaveMillis.length() > 0) {
        json.put(JsonTagsZ.TIMESTAMP_OF_SAVE_MILLIS, timestampSaveMillis);
      }
      if (partid != null && partid.length() > 0) {
        json.put(JsonTagsZ.PART_ID, partid);
      }
      return json;
    } catch (JSONException e) {
      throw new ProtocolException(e.getMessage());
    }
  }

  // Add material data to JSON
  private void addMaterialData(JSONObject json) throws JSONException {
    if (this.materials == null || this.materials.isEmpty()) {
      return;
    }
    ///throw new JSONException( "No transactions objects specified" );
    JSONArray array = new JSONArray();
    Enumeration en = this.materials.elements();
    while (en.hasMoreElements()) {
      Hashtable material = (Hashtable) en.nextElement();
      // Form a material JSON
      JSONObject m = new JSONObject();
      m.put(JsonTagsZ.MATERIAL_ID, (String) material.get(JsonTagsZ.MATERIAL_ID));
      m.put(JsonTagsZ.QUANTITY, (String) material.get(JsonTagsZ.QUANTITY));
      // Add reason if present
      String reason = (String) material.get(JsonTagsZ.REASON);
      if (reason != null && !reason.equals("")) {
        m.put(JsonTagsZ.REASON, reason);
      }
      //Opening stock on client
      String openingstock = (String) material.get(JsonTagsZ.OPENING_STOCK);
      if (openingstock != null && !openingstock.equals("")) {
        m.put(JsonTagsZ.OPENING_STOCK, openingstock);
      }
      //material status
      String materialstatus = (String) material.get(JsonTagsZ.MATERIAL_STATUS);
      if (materialstatus != null && !materialstatus.equals("")) {
        m.put(JsonTagsZ.MATERIAL_STATUS, materialstatus);
      }
      //actual transaction date
      String actualtransactiondate = (String) material.get(JsonTagsZ.ACTUAL_TRANSACTION_DATE);
      if (actualtransactiondate != null && !actualtransactiondate.equals("")) {
        m.put(JsonTagsZ.ACTUAL_TRANSACTION_DATE, actualtransactiondate);
      }
      String value = null;
      // Add the batch-id, if present
      if ((value = (String) material.get(JsonTagsZ.BATCH_ID)) != null) {
        m.put(JsonTagsZ.BATCH_ID, value);
      }
      if ((value = (String) material.get(JsonTagsZ.BATCH_EXPIRY)) != null) {
        m.put(JsonTagsZ.BATCH_EXPIRY, value);
      }
      if ((value = (String) material.get(JsonTagsZ.BATCH_MANUFACTUER_NAME)) != null) {
        m.put(JsonTagsZ.BATCH_MANUFACTUER_NAME, value);
      }
      if ((value = (String) material.get(JsonTagsZ.BATCH_MANUFACTURED_DATE)) != null) {
        m.put(JsonTagsZ.BATCH_MANUFACTURED_DATE, value);
      }
      // Add the micro-message, if present
      if ((value = (String) material.get(JsonTagsZ.MESSAGE)) != null) {
        m.put(JsonTagsZ.MESSAGE, value);
      }
      // Add to array
      array.put(m);
    }
    // Add to container JSON
    json.put(JsonTagsZ.MATERIALS, array);
  }

  // Load material data from JSON
  private void loadMaterialData(JSONObject json) throws JSONException {
    this.materials = new Vector();
    // Get the material JSON array
    JSONArray array = null;
    try {
      array = (JSONArray) json.get(JsonTagsZ.MATERIALS);
    } catch (JSONException e) {
      // If no materials, then simply return
      return;
    }
    for (int i = 0; i < array.length(); i++) {
      // Get next material
      JSONObject m = (JSONObject) array.get(i);
      Hashtable material = new Hashtable();
      material.put(JsonTagsZ.MATERIAL_ID, (String) m.get(JsonTagsZ.MATERIAL_ID));
      material.put(JsonTagsZ.QUANTITY, (String) m.get(JsonTagsZ.QUANTITY));
      boolean hasReasonTag = true;
      try {
        material.put(JsonTagsZ.REASON, m.getString(JsonTagsZ.REASON));
      } catch (JSONException e) {
        // ignore
        hasReasonTag = false;
      }
      // FOR BACKWARD COMPATIBILITY ONLY (27/04/2011)
      if (!hasReasonTag) {
        try {
          material.put(JsonTagsZ.REASON, (String) m.get(JsonTagsZ.REASONS_WASTAGE));
        } catch (JSONException e) {
          // do nothing
        }
      }
      // END BACKWARD COMPATIBILITY
      //opening stock on client
      try {
        material.put(JsonTagsZ.OPENING_STOCK, m.getString(JsonTagsZ.OPENING_STOCK));
      } catch (JSONException e) {
        // ignore
      }
      //material status
      try {
        material.put(JsonTagsZ.MATERIAL_STATUS, m.getString(JsonTagsZ.MATERIAL_STATUS));
      } catch (JSONException e) {
        // ignore
      }
      //actual transaction date
      try {
        material
            .put(JsonTagsZ.ACTUAL_TRANSACTION_DATE, m.getString(JsonTagsZ.ACTUAL_TRANSACTION_DATE));
      } catch (JSONException e) {
        // ignore
      }
      // Get the batch-id, if present
      try {
        material.put(JsonTagsZ.BATCH_ID, (String) m.get(JsonTagsZ.BATCH_ID));
      } catch (JSONException e) {
        // do nothing, given it is optional
      }
      try {
        material.put(JsonTagsZ.BATCH_EXPIRY, (String) m.get(JsonTagsZ.BATCH_EXPIRY));
      } catch (JSONException e) {
        // do nothing, given it is optional
      }
      try {
        material.put(JsonTagsZ.BATCH_MANUFACTUER_NAME,
            (String) m.get(JsonTagsZ.BATCH_MANUFACTUER_NAME));
      } catch (JSONException e) {
        // do nothing, given it is optional
      }
      try {
        material.put(JsonTagsZ.BATCH_MANUFACTURED_DATE,
            (String) m.get(JsonTagsZ.BATCH_MANUFACTURED_DATE));
      } catch (JSONException e) {
        // do nothing, given it is optional
      }
      // Get the micro-message, if present
      try {
        material.put(JsonTagsZ.MESSAGE, (String) m.get(JsonTagsZ.MESSAGE));
      } catch (JSONException e) {
        // do nothing, given it is optional
      }
      // Add to material list
      this.materials.addElement(material);
    }
  }


}

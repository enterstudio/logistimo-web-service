/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.logistimo.proto;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Hashtable;
import java.util.Vector;

/**
 * @author Arun
 */
public class UpdateOrderInput extends UpdateInventoryInput {

  private Vector
      estFulfillmentTimeRanges =
      null;
  // Vector of Hashtable, each table having two date strings keyed by - JsonTagsZ.ESTIMATED_FULFILLMENT_TIME_START and JsonTagsZ.ESITMATED_FULFILLMENT_TIME_END
  private Hashtable
      confirmedFulfillmentTimeRange =
      null;
  // Hashtable having two date strings keyed by - JsonTagsZ.ESTIMATED_FULFILLMENT_TIME_START and JsonTagsZ.ESITMATED_FULFILLMENT_TIME_END
  private String payment = null; // actual payment made on the order
  private String paymentOption = null;
  private String packageSize = null;
  private String orderStatus = null;
  private String orderTags = null;
  private String orderType = null;

  public UpdateOrderInput() {
    cmd = RestConstantsZ.ACTION_UPDATEORDER;
  }
  // Added 10/1/2013

  public UpdateOrderInput(String type, String userId, String password, String kioskId,
                          Vector materials,
                          String trackingId, String vendorId, String message, String destUserId,
                          String latitude, String longitude, String geoAccuracy,
                          String geoErrorCode, String version, String altitude,
                          String timestampSaveMillis, String partid) {
    super(type, userId, password, kioskId, materials, trackingId, vendorId, message, destUserId,
        latitude, longitude, geoAccuracy, geoErrorCode, version, altitude, timestampSaveMillis,
        partid);

    cmd = RestConstantsZ.ACTION_UPDATEORDER;
  }

  public UpdateOrderInput(String type, String userId, String password, String kioskId,
                          Vector materials,
                          String trackingId, String vendorId, String message, String destUserId,
                          String latitude, String longitude, String version) {
    super(type, userId, password, kioskId, materials, trackingId, vendorId, message, destUserId,
        latitude, longitude, version);
    cmd = RestConstantsZ.ACTION_UPDATEORDER;
  }

  public UpdateOrderInput(String type, String userId, String password, String kioskId,
                          Vector materials,
                          String trackingId, String vendorId, String message, String destUserId,
                          String version) {
    super(type, userId, password, kioskId, materials, trackingId, vendorId, message, destUserId,
        null, null, version);
    cmd = RestConstantsZ.ACTION_UPDATEORDER;
  }

  // Additional (optional) fields for orders
  // Estimated fulfillment time ranges
  public Vector getEstimatedFulfillmentTimeRanges() {
    return estFulfillmentTimeRanges;
  }

  public void setEstimatedFulfillmentTimeRange(Vector estFulfillmentTimeRanges) {
    this.estFulfillmentTimeRanges = estFulfillmentTimeRanges;
  }

  // Confirmed fulfillment time range
  public Hashtable getConfirmedFulfillmentTimeRange() {
    return confirmedFulfillmentTimeRange;
  }

  public void setConfirmedFulfillmentTimeRange(Hashtable confirmedFulfillmentTimeRange) {
    this.confirmedFulfillmentTimeRange = confirmedFulfillmentTimeRange;
  }

  // Payment
  public String getPayment() {
    return payment;
  }

  public void setPayment(String payment) {
    this.payment = payment;
  }

  // Payment option
  public String getPaymentOption() {
    return paymentOption;
  }

  public void setPaymentOption(String paymentOption) {
    this.paymentOption = paymentOption;
  }

  // Package size
  public String getPackageSize() {
    return packageSize;
  }

  public void setPackageSize(String packageSize) {
    this.packageSize = packageSize;
  }

  // Order status
  public String getOrderStatus() {
    return orderStatus;
  }

  public void setOrderStatus(String status) {
    this.orderStatus = status;
  }

  // Order tags
  public String getOrderTags() {
    return orderTags;
  }

  public void setOrderTags(String orderTags) {
    this.orderTags = orderTags;
  }

  // Order status
  public String getOrderType() {
    return orderType;
  }

  public void setOrderType(String orderType) {
    this.orderType = orderType;
  }

  // Load from JSON (override parent)
  public void fromJSONString(String jsonString) throws ProtocolException {
    try {
      JSONObject json = new JSONObject(jsonString);
      // Load inventory related fields
      super.fromJSONObject(json);
      // Load the special order fields, if any
      String value = null;
      // Estimated fulfillment time ranges, if any
      if ((value = json.optString(JsonTagsZ.ESTIMATED_FULFILLMENT_TIMERANGES)) != null) {
        estFulfillmentTimeRanges = JsonUtil.parseTimeRanges(value);
      }
      // Confirmed fulfillment time range, if any
      if ((value = json.optString(JsonTagsZ.CONFIRMED_FULFILLMENT_TIMERANGE)) != null) {
        confirmedFulfillmentTimeRange = JsonUtil.parseTimeRange(value);
      }
      // Payment, if any
      if ((value = json.optString(JsonTagsZ.PAYMENT)) != null) {
        payment = value;
      }
      // Payment option, if any
      if ((value = json.optString(JsonTagsZ.PAYMENT_OPTION)) != null) {
        paymentOption = value;
      }
      // Package size, if any
      if ((value = json.optString(JsonTagsZ.PACKAGE_SIZE)) != null) {
        packageSize = value;
      }
      // Initial order status, if any
      if ((value = json.optString(JsonTagsZ.ORDER_STATUS)) != null && !value.equals("")) {
        orderStatus = value;
      }
    } catch (JSONException e) {
      throw new ProtocolException(e.getMessage());
    }
  }

  // Convert to JSON String
  public String toJSONString() throws ProtocolException {
    JSONObject json = super.toJSONObject();
    try {
      // Estimated fulfillment time ranges, if any
      if (estFulfillmentTimeRanges != null && !estFulfillmentTimeRanges.isEmpty()) {
        json.put(JsonTagsZ.ESTIMATED_FULFILLMENT_TIMERANGES,
            JsonUtil.formatTimeRanges(estFulfillmentTimeRanges));
      }
      // Confirmed fulfillment time range, if any
      if (confirmedFulfillmentTimeRange != null && !confirmedFulfillmentTimeRange.isEmpty()) {
        json.put(JsonTagsZ.CONFIRMED_FULFILLMENT_TIMERANGE,
            JsonUtil.formatTimeRange(confirmedFulfillmentTimeRange));
      }
      // Payment, if any
      if (payment != null && !payment.equals("")) {
        json.put(JsonTagsZ.PAYMENT, payment);
      }
      // Payment option, if any
      if (paymentOption != null) {
        json.put(JsonTagsZ.PAYMENT_OPTION, paymentOption);
      }
      // Package size, if any
      if (packageSize != null) {
        json.put(JsonTagsZ.PACKAGE_SIZE, packageSize);
      }
      // Initial order status, if any
      if (orderStatus != null && !orderStatus.equals("")) {
        json.put(JsonTagsZ.ORDER_STATUS, orderStatus);
      }
      return json.toString();
    } catch (JSONException e) {
      throw new ProtocolException(e.getMessage());
    }
  }
}
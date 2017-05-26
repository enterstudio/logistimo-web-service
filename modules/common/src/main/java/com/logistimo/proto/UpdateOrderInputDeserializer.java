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

package com.logistimo.proto;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.reflect.TypeToken;

import com.logistimo.constants.Constants;

import java.lang.reflect.Type;
import java.util.Hashtable;
import java.util.Vector;

/**
 * Created by chitrachar on 22/06/16.
 */
public class UpdateOrderInputDeserializer implements JsonDeserializer<UpdateOrderInput> {

  @Override
  public UpdateOrderInput deserialize(final JsonElement json, final Type typeOfT,
                                      final JsonDeserializationContext context)
      throws JsonParseException {
    final JsonObject jsonObject = json.getAsJsonObject();

    final UpdateOrderInput updOrderInput = new UpdateOrderInput();
    if (jsonObject.get(JsonTagsZ.ALTITUDE) != null) {
      updOrderInput.setAltitude(jsonObject.get(JsonTagsZ.ALTITUDE).getAsString());
    }
    if (jsonObject.get(JsonTagsZ.DEST_USER_ID) != null) {
      updOrderInput.setDestUserId(jsonObject.get(JsonTagsZ.DEST_USER_ID).getAsString());
    }
    if (jsonObject.get(JsonTagsZ.USER_ID) != null) {
      updOrderInput.setUserId(jsonObject.get(JsonTagsZ.USER_ID).getAsString());
    }
    if (jsonObject.get(JsonTagsZ.KIOSK_ID) != null) {
      updOrderInput.setKioskId(jsonObject.get(JsonTagsZ.KIOSK_ID).getAsString());
    }
    if (jsonObject.get(JsonTagsZ.GEO_ACCURACY) != null) {
      updOrderInput.setGeoAccuracy(jsonObject.get(JsonTagsZ.GEO_ACCURACY).getAsString());
    }
    if (jsonObject.get(JsonTagsZ.GEO_ERROR_CODE) != null) {
      updOrderInput.setGeoErrorCode(jsonObject.get(JsonTagsZ.GEO_ERROR_CODE).getAsString());
    }
    if (jsonObject.get(JsonTagsZ.LATITUDE) != null) {
      updOrderInput.setLatitude(jsonObject.get(JsonTagsZ.LATITUDE).getAsString());
    }
    if (jsonObject.get(JsonTagsZ.LONGITUDE) != null) {
      updOrderInput.setLongitude(jsonObject.get(JsonTagsZ.LONGITUDE).getAsString());
    }
    if (jsonObject.get(JsonTagsZ.LINKED_KIOSK_ID) != null) {
      updOrderInput.setLinkedKioskId(jsonObject.get(JsonTagsZ.LINKED_KIOSK_ID).getAsString());
    }
    if (jsonObject.get(JsonTagsZ.MESSAGE) != null) {
      updOrderInput.setMessage(jsonObject.get(JsonTagsZ.MESSAGE).getAsString());
    }
    if (jsonObject.get(JsonTagsZ.PART_ID) != null) {
      updOrderInput.setPartid(jsonObject.get(JsonTagsZ.PART_ID).getAsString());
    }
    if (jsonObject.get(JsonTagsZ.TIMESTAMP_OF_SAVE_MILLIS) != null) {
      updOrderInput
          .setTimestampSaveMillis(jsonObject.get(JsonTagsZ.TIMESTAMP_OF_SAVE_MILLIS).getAsString());
    }
    if (jsonObject.get(JsonTagsZ.TRACKING_ID) != null) {
      updOrderInput.setTrackingId(jsonObject.get(JsonTagsZ.TRACKING_ID).getAsString());
    }
    if (jsonObject.get(JsonTagsZ.TYPE) != null) {
      updOrderInput.setType(jsonObject.get(JsonTagsZ.TYPE).getAsString());
    }
    if (jsonObject.get(JsonTagsZ.ORDER_STATUS) != null) {
      updOrderInput.setOrderStatus(jsonObject.get(JsonTagsZ.ORDER_STATUS).getAsString());
    }
    if (jsonObject.get(JsonTagsZ.PACKAGE_SIZE) != null) {
      updOrderInput.setPackageSize(jsonObject.get(JsonTagsZ.PACKAGE_SIZE).getAsString());
    }
    if (jsonObject.get(JsonTagsZ.PAYMENT) != null) {
      updOrderInput.setPayment(jsonObject.get(JsonTagsZ.PAYMENT).getAsString());
    }
    if (jsonObject.get(JsonTagsZ.PAYMENT_OPTION) != null) {
      updOrderInput.setPaymentOption(jsonObject.get(JsonTagsZ.PAYMENT_OPTION).getAsString());
    }
    if (jsonObject.get(JsonTagsZ.TAGS) != null) {
      updOrderInput.setOrderTags(jsonObject.get(JsonTagsZ.TAGS).getAsString());
    }
    if (jsonObject.get(JsonTagsZ.ORDER_TYPE) != null) {
      updOrderInput.setOrderType(jsonObject.get(JsonTagsZ.ORDER_TYPE).getAsString());
    }

    Gson
        gson =
        new GsonBuilder().setDateFormat(Constants.DATE_FORMAT)
            .excludeFieldsWithoutExposeAnnotation().create();

    String
        estFuflfillmentTimeStr =
        gson.toJson(jsonObject.get(JsonTagsZ.ESTIMATED_FULFILLMENT_TIMERANGES));
    if (estFuflfillmentTimeStr != null) {
      Vector
          estFuflfillmentTime =
          gson.fromJson(estFuflfillmentTimeStr, new TypeToken<Vector<Hashtable<String, String>>>() {
          }.getType());
      updOrderInput.setEstimatedFulfillmentTimeRange(estFuflfillmentTime);
    }

    String
        confFulfillmentTimeStr =
        gson.toJson(jsonObject.get(JsonTagsZ.CONFIRMED_FULFILLMENT_TIMERANGE));
    if (confFulfillmentTimeStr != null) {
      Hashtable
          confFufillmentTime =
          gson.fromJson(confFulfillmentTimeStr, new TypeToken<Hashtable<String, String>>() {
          }.getType());
      updOrderInput.setConfirmedFulfillmentTimeRange(confFufillmentTime);
    }
    String materialString = gson.toJson(jsonObject.get(JsonTagsZ.MATERIALS));
    if (materialString != null) {
      Vector
          materials =
          gson.fromJson(materialString, new TypeToken<Vector<Hashtable<String, String>>>() {
          }.getType());
      updOrderInput.setMaterials(materials);
    }

    return updOrderInput;
  }
}

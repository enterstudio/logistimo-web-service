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
public class UpdateInventoryInputDeserializer implements JsonDeserializer<UpdateInventoryInput> {

  @Override
  public UpdateInventoryInput deserialize(final JsonElement json, final Type typeOfT,
                                          final JsonDeserializationContext context)
      throws JsonParseException {
    final JsonObject jsonObject = json.getAsJsonObject();

    final UpdateInventoryInput updInvInput = new UpdateInventoryInput();
    if (jsonObject.get(JsonTagsZ.ALTITUDE) != null) {
      updInvInput.setAltitude(jsonObject.get(JsonTagsZ.ALTITUDE).getAsString());
    }
    if (jsonObject.get(JsonTagsZ.DEST_USER_ID) != null) {
      updInvInput.setDestUserId(jsonObject.get(JsonTagsZ.DEST_USER_ID).getAsString());
    }
    if (jsonObject.get(JsonTagsZ.USER_ID) != null) {
      updInvInput.setUserId(jsonObject.get(JsonTagsZ.USER_ID).getAsString());
    }
    if (jsonObject.get(JsonTagsZ.KIOSK_ID) != null) {
      updInvInput.setKioskId(jsonObject.get(JsonTagsZ.KIOSK_ID).getAsString());
    }
    if (jsonObject.get(JsonTagsZ.GEO_ACCURACY) != null) {
      updInvInput.setGeoAccuracy(jsonObject.get(JsonTagsZ.GEO_ACCURACY).getAsString());
    }
    if (jsonObject.get(JsonTagsZ.GEO_ERROR_CODE) != null) {
      updInvInput.setGeoErrorCode(jsonObject.get(JsonTagsZ.GEO_ERROR_CODE).getAsString());
    }
    if (jsonObject.get(JsonTagsZ.LATITUDE) != null) {
      updInvInput.setLatitude(jsonObject.get(JsonTagsZ.LATITUDE).getAsString());
    }
    if (jsonObject.get(JsonTagsZ.LONGITUDE) != null) {
      updInvInput.setLongitude(jsonObject.get(JsonTagsZ.LONGITUDE).getAsString());
    }
    if (jsonObject.get(JsonTagsZ.LINKED_KIOSK_ID) != null) {
      updInvInput.setLinkedKioskId(jsonObject.get(JsonTagsZ.LINKED_KIOSK_ID).getAsString());
    }
    if (jsonObject.get(JsonTagsZ.MESSAGE) != null) {
      updInvInput.setMessage(jsonObject.get(JsonTagsZ.MESSAGE).getAsString());
    }
    if (jsonObject.get(JsonTagsZ.PART_ID) != null) {
      updInvInput.setPartid(jsonObject.get(JsonTagsZ.PART_ID).getAsString());
    }
    if (jsonObject.get(JsonTagsZ.TIMESTAMP_OF_SAVE_MILLIS) != null) {
      updInvInput
          .setTimestampSaveMillis(jsonObject.get(JsonTagsZ.TIMESTAMP_OF_SAVE_MILLIS).getAsString());
    }
    if (jsonObject.get(JsonTagsZ.TRACKING_ID) != null) {
      updInvInput.setTrackingId(jsonObject.get(JsonTagsZ.TRACKING_ID).getAsString());
    }
    if (jsonObject.get(JsonTagsZ.TYPE) != null) {
      updInvInput.setType(jsonObject.get(JsonTagsZ.TYPE).getAsString());
    }

    Gson
        gson =
        new GsonBuilder().setDateFormat(Constants.DATE_FORMAT)
            .excludeFieldsWithoutExposeAnnotation().create();

    String materialString = gson.toJson(jsonObject.get(JsonTagsZ.MATERIALS));
    if (materialString != null) {
      Vector
          materials =
          gson.fromJson(materialString, new TypeToken<Vector<Hashtable<String, String>>>() {
          }.getType());
      updInvInput.setMaterials(materials);
    }

    return updInvInput;
  }
}

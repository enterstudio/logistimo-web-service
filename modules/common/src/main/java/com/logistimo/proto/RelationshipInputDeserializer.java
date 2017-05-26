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
 * Created by chitrachar on 24/06/16.
 */
public class RelationshipInputDeserializer implements JsonDeserializer<RelationshipInput> {

  @Override
  public RelationshipInput deserialize(final JsonElement json, final Type typeOfT,
                                       final JsonDeserializationContext context)
      throws JsonParseException {
    final JsonObject jsonObject = json.getAsJsonObject();

    final RelationshipInput relationshipInput = new RelationshipInput();
    if (jsonObject.get(JsonTagsZ.KIOSK_ID) != null) {
      relationshipInput.setKioskId(jsonObject.get(JsonTagsZ.KIOSK_ID).getAsString());
    }
    if (jsonObject.get(JsonTagsZ.LINK_TYPE) != null) {
      relationshipInput.setLinkType(jsonObject.get(JsonTagsZ.LINK_TYPE).getAsString());
    }

    Gson
        gson =
        new GsonBuilder().setDateFormat(Constants.DATE_FORMAT)
            .excludeFieldsWithoutExposeAnnotation().create();

    String linkedKioskList = gson.toJson(jsonObject.get(JsonTagsZ.LINKED_KIOSK_ID));
    if (linkedKioskList != null) {
      Vector
          linkedKiosks =
          gson.fromJson(linkedKioskList, new TypeToken<Vector<Hashtable<String, String>>>() {
          }.getType());
      relationshipInput.setLinkedKiosks(linkedKiosks);
    }

    String
        linkedKiosktoRmvList =
        gson.toJson(jsonObject.get(JsonTagsZ.LINKED_KIOSK_IDS_TOBEREMOVED));
    if (linkedKiosktoRmvList != null) {
      Vector
          linkedKiosktoRmv =
          gson.fromJson(linkedKiosktoRmvList, new TypeToken<Vector<Hashtable<String, String>>>() {
          }.getType());
      relationshipInput.setLinkedKiosks(linkedKiosktoRmv);
    }

    return relationshipInput;
  }
}

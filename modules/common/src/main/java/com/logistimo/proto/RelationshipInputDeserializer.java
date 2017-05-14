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
